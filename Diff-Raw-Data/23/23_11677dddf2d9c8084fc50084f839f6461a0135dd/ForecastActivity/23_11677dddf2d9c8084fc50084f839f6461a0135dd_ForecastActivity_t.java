 package org.weather.weatherman.activity;
 
 import org.weather.weatherman.R;
 import org.weather.weatherman.WeatherApplication;
 import org.weather.weatherman.content.Weather;
 import org.weather.weatherman.content.WeatherService;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.baidu.mobstat.StatService;
 
 /**
  * @since 2012-5-18
  * @author gmz
  * 
  */
 public class ForecastActivity extends Activity {
 
 	private static final String tag = ForecastTask.class.getSimpleName();
 
 	private WeatherApplication app;
 	private WeatherService weatherService;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.forecast);
 		app = (WeatherApplication) getApplication();
 		weatherService = new WeatherService(this);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
 		progressBar.setVisibility(View.VISIBLE);
 		this.refreshData();
 		// stats
 		StatService.onResume(this);
 	}
 
 	/**
 	 * @author gengmaozhang01
 	 * @since 2014-1-25 下午6:06:02
 	 */
 	public void refreshData() {
 		String city = (app.getCity() != null ? app.getCity().getId() : null);
 		new ForecastTask().execute(city);
 	}
 
 	class ForecastTask extends AsyncTask<String, Integer, Weather.ForecastWeather> {
 
 		public ForecastTask() {
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			// updateTime
 			TextView view = (TextView) findViewById(R.id.updateTime);
 			view.setText("--");
 			// clear
 			TableLayout layout = (TableLayout) view.getParent().getParent();
 			layout.removeViews(1, layout.getChildCount() - 1);
 		}
 
 		@Override
 		protected Weather.ForecastWeather doInBackground(String... params) {
 			onProgressUpdate(0);
 			String city = (params != null && params.length > 0 ? params[0] : null);
 			if (city == null || city.length() == 0) {
 				return null;
 			}
 			onProgressUpdate(20);
 			Weather.ForecastWeather forecast = weatherService.findForecastWeather(city);
 			onProgressUpdate(60);
 			return forecast;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 			int progress = (values != null && values.length > 0 ? values[0] : 0);
 			ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
 			if (progressBar != null) {
 				Log.i(tag, progress + "/" + progressBar.getMax());
 				progressBar.setProgress(progress);
 				if (progress >= progressBar.getMax()) {
 					progressBar.setVisibility(View.GONE);
 				}
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Weather.ForecastWeather forecast) {
 			this.onPreExecute();
 			super.onPostExecute(forecast);
 			onProgressUpdate(80);
 			if (forecast != null) {
 				// update time
 				TextView uptimeView = (TextView) findViewById(R.id.updateTime);
 				uptimeView.setText(forecast.getTime() + "更新");
 				// add row
 				TableLayout layout = (TableLayout) uptimeView.getParent().getParent();
 				for (int i = 0; i < forecast.getForecastSize(); i++) {
 					TableRow row = new TableRow(layout.getContext());
 					// date
 					TextView view = new TextView(layout.getContext());
					final String time = forecast.getForecastTime(i).substring(5);
					view.setText(time + "：");
 					row.addView(view);
 					// weather
 					view = new TextView(layout.getContext());
					view.setText(forecast.getForecastWeather(i) + "。" + (time.contains("夜") ? "低温" : "高温")
							+ forecast.getForecastTemperature(i) + "。" + forecast.getForecastWind(i) + "，"
							+ forecast.getForecastWindForce(i));
 					row.addView(view);
 					layout.addView(row);
 				}
 			} else {
 				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.connect_failed));
 				Log.e(tag, "can't get forecast weather");
 			}
 			onProgressUpdate(100);
 		}
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// stats
 		StatService.onPause(this);
 	}
 
 	@Override
 	public void onBackPressed() {
 		Activity parent = getParent();
 		if (parent != null) {
 			parent.onBackPressed();
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 }
