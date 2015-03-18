package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }
    long prev = 0;
    long prev2 = 0;
    long prev3 = 0;
    public void buttonClicked(View v) {
        long reclick = System.currentTimeMillis();
            int id = v.getId();
            WeatherTask w = new WeatherTask();
            switch (id) {
                case R.id.btBangkok:
                    if(reclick - prev > 60000) {
                        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                        prev = reclick;
                        prev2 = 0;
                        prev3 = 0;
                    }
                    break;

                case R.id.btNon:
                    if(reclick - prev2 > 60000) {
                        w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                        prev2 = reclick;
                        prev = 0;
                        prev3 = 0;
                    }
                    break;
                case R.id.btPathum:
                    if(reclick - prev3 > 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathum Weather");
                        prev3 = reclick;
                        prev = 0;
                        prev2 = 0;
                    }
                    break;
            }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        double windSpeed;
        double temperature;
        double temp_max;
        double temp_min;
        double humidity;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jTemp = jWeather.getJSONObject("main");
                    temperature = jTemp.getDouble("temp");
                    temperature = temperature - 273;

                    JSONObject jTempMax = jWeather.getJSONObject("main");
                    temp_max= jTempMax.getDouble("temp_max");
                    temp_max = temp_max - 273;

                    JSONObject jTempMin = jWeather.getJSONObject("main");
                    temp_min = jTempMin.getDouble("temp_min");
                    temp_min = temp_min - 273;

                    JSONObject jHumid = jWeather.getJSONObject("main");
                    humidity = jHumid.getDouble("humidity");


                    JSONObject jWind = jWeather.getJSONObject("wind");
                    windSpeed = jWind.getDouble("speed");
                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind, tvHumid, tvTemp;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvHumid = (TextView)findViewById(R.id.tvHumid);
            tvTemp = (TextView)findViewById(R.id.tvTemp);

            if (result) {
                tvTitle.setText(title);
                tvTemp.setText(String.format("%.1f(max = %.1f, min = %.1f)", temperature, temp_max, temp_max));
                tvHumid.setText(String.format("%.0f", humidity) + "%");
                tvWind.setText(String.format("%.1f", windSpeed));
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvTemp.setText("");
                tvHumid.setText("");
                tvWind.setText("");
            }
        }
    }
}
