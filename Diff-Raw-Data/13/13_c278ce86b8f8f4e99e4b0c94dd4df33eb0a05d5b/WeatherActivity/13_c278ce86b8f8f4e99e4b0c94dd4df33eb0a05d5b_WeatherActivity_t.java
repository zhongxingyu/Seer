 package com.freescale.iastate.hvac;
 
 import java.net.URL;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import com.freescale.iastate.hvac.weather.*;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WeatherActivity extends Activity implements MenuInterface {
 	final char degree = 0x00B0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		final char degree = 0x00B0;
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.weather);
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		
 		//this sets the help string for the current activity.
 		//copy paste 
 		rootIntent.setHelpText(getText(R.string.weather_help));
 
 		ForecastWeatherTask task = new ForecastWeatherTask();
 		task.execute();
 
 	}
 
 	private class ForecastWeatherTask extends AsyncTask<Void, Void, String> {
 		ImageView dayOneImageView = (ImageView) findViewById(R.id.day1image);
 		ImageView dayTwoImageView = (ImageView) findViewById(R.id.day2image);
 		ImageView dayThreeImageView = (ImageView) findViewById(R.id.day3image);
 		TextView dayOneTextView = (TextView) findViewById(R.id.day1text);
 		TextView dayTwoTextView = (TextView) findViewById(R.id.day2text);
 		TextView dayThreeTextView = (TextView) findViewById(R.id.day3text);
 
 		String day1Text, day2Text, day3Text;
 		Drawable day1Image, day2Image, day3Image;
 
 		protected String doInBackground(Void... params) {
 			try {
 				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				String zipurl = "http://www.google.com/ig/api?weather=" + settings.getString("zip_code_key", "50014");
				URL url = new URL(zipurl);
 
 				/* Get a SAXParser from the SAXPArserFactory. */
 				SAXParserFactory spf = SAXParserFactory.newInstance();
 				SAXParser sp = spf.newSAXParser();
 
 				/* Get the XMLReader of the SAXParser we created. */
 				XMLReader xr = sp.getXMLReader();
 
 				/*
 				 * Create a new ContentHandler and apply it to the XML-Reader
 				 */
 				GoogleHandler gwh = new GoogleHandler();
 				xr.setContentHandler(gwh);
 
 				/* Parse the xml-data our URL-call returned. */
 				xr.parse(new InputSource(url.openStream()));
 
 				/* Our Handler now provides the parsed weather-data to us. */
 				WeatherSet ws = gwh.getWeatherSet();
 
 				day1Text = weatherString(ws, 1);
 				day1Image = weatherImage(day1Text);
 
 				day2Text = weatherString(ws, 2);
 				day2Image = weatherImage(day2Text);
 
 				day3Text = weatherString(ws, 3);
 				day3Image = weatherImage(day3Text);
 				return null;
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 
 		}
 
 		protected void onPostExecute(String result) {
 			dayOneTextView.setText(day1Text);
 			dayOneImageView.setImageDrawable(day1Image);
 			dayTwoTextView.setText(day2Text);
 			dayTwoImageView.setImageDrawable(day2Image);
 			dayThreeTextView.setText(day3Text);
 			dayThreeImageView.setImageDrawable(day3Image);
 		}
 
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 			return rootIntent.onOptionsItemSelected(this, item);
 	}
 
 	public Drawable weatherImage(String condition) {
 		Resources res = getResources();
 		Drawable conditionDrawable = null;
 		if (condition.contains("rain") || condition.contains("Rain")) {
 			conditionDrawable = res.getDrawable(R.drawable.weather_rain);
 		}
 		if (condition.contains("sun") || condition.contains("Sun")
 				|| condition.contains("clear") || condition.contains("Clear")) {
 			conditionDrawable = res.getDrawable(R.drawable.weather_clear);
 		}
 		if (condition.contains("storm") || condition.contains("Storm")) {
 			conditionDrawable = res.getDrawable(R.drawable.weather_storm);
 		}
 		if (condition.contains("cloudy") || condition.contains("Cloudy")
 				|| condition.contains("Overcast")) {
 			conditionDrawable = res.getDrawable(R.drawable.weather_cloudy);
 		}
 		if (condition.contains("snow") || condition.contains("Snow")) {
 			conditionDrawable = res.getDrawable(R.drawable.weather_snow);
 		}
 		return conditionDrawable;
 
 	}
 
 	public String weatherString(WeatherSet ws, int i) {
 		return ws.getWeatherForecastConditions().get(i).getDayofWeek()
 				+ "\n"
 				+ ws.getWeatherForecastConditions().get(i).getCondition()
 				+ "\n High: "
 				+ WeatherUtils.celsiusToFahrenheit(ws
 						.getWeatherForecastConditions().get(i)
 						.getTempMaxCelsius())
 				+ degree
 				+ "\n Low: "
 				+ WeatherUtils.celsiusToFahrenheit(ws
 						.getWeatherForecastConditions().get(i)
 						.getTempMinCelsius()) + degree;
 	}
 }
