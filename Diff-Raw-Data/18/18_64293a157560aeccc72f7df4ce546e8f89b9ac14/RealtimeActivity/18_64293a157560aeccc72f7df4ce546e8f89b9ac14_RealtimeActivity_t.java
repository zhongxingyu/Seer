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
 
 public class RealtimeActivity extends Activity {
 
 	private static final String tag = RealtimeTask.class.getSimpleName();
 
 	private WeatherApplication app;
 	private WeatherService weatherService;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.realtime);
 		app = (WeatherApplication) getApplication();
 		weatherService = new WeatherService(this);
 		// domob
 		// RelativeLayout adContainer = (RelativeLayout)
 		// findViewById(R.id.adContainver);
 		// DomobAdView adView = new DomobAdView(this,
 		// WeatherApplication.DOMOB_PUBLISHER_ID,
 		// DomobAdView.INLINE_SIZE_320X50);
 		// adContainer.addView(adView);
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
 	 * @since 2014-1-25 下午6:04:26
 	 */
 	public void refreshData() {
 		String city = (app.getCity() != null ? app.getCity().getId() : null);
 		new RealtimeTask().execute(city);
 	}
 
 	class RealtimeTask extends AsyncTask<String, Integer, Boolean> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			// updateTime
 			TextView view = (TextView) findViewById(R.id.updateTime);
 			view.setText("--");
 			// temperature
 			view = (TextView) findViewById(R.id.temperatue);
 			view.setText("--");
 			// wind
 			view = (TextView) findViewById(R.id.wind);
 			view.setText("--");
 			// humidity
 			view = (TextView) findViewById(R.id.humidity);
 			view.setText("--");
 			// AQI
 			view = (TextView) findViewById(R.id.AQI);
 			view.setText("--");
 			// living index
 			TableLayout layout = (TableLayout) view.getParent().getParent();
 			layout.removeViews(5, layout.getChildCount() - 5);
 		}
 
 		private Weather.RealtimeWeather realtime;
 		private Weather.AirQualityIndex aqi;
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 			onProgressUpdate(0);
 			String city = (params != null && params.length > 0 ? params[0] : null);
 			if (city == null || city.length() == 0) {
 				return false;
 			}
 			onProgressUpdate(20);
 			realtime = weatherService.findRealtimeWeather(city);
 			onProgressUpdate(40);
 			aqi = weatherService.findAirQualityIndex(city);
 			onProgressUpdate(60);
 			return true;
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
 		protected void onPostExecute(Boolean result) {
 			this.onPreExecute();
 			super.onPostExecute(result);
 			onProgressUpdate(80);
 			boolean isOk = true;
 			if (realtime != null) {
 				// updateTime
 				TextView view = (TextView) findViewById(R.id.updateTime);
 				view.setText(realtime.getTime() + "更新");
 				// temperature
 				view = (TextView) findViewById(R.id.temperatue);
 				view.setText(realtime.getTemperature());
 				// wind
 				view = (TextView) findViewById(R.id.wind);
 				view.setText(realtime.getWindDirection() + "，" + realtime.getWindForce());
 				// humidity
 				view = (TextView) findViewById(R.id.humidity);
 				view.setText("湿度" + realtime.getHumidity());
 				// living index
 				TableLayout layout = (TableLayout) view.getParent().getParent();
 				for (int i = 0; i < realtime.getIndexSize(); i++) {
 					TableRow row = new TableRow(layout.getContext());
 					view = new TextView(layout.getContext());
 					view.setText(realtime.getIndexName(i) + "：");
 					row.addView(view);
 					view = new TextView(layout.getContext());
					view.setText(realtime.getIndexValue(i) + "。" + realtime.getIndexDesc(i));
 					row.addView(view);
 					layout.addView(row);
 				}
 			} else {
 				isOk = false;
 				Log.e(tag, "can't get realtime weather");
 			}
 			// aqi
 			if (aqi != null) {
 				TextView view = (TextView) findViewById(R.id.AQI);
 				int value = aqi.getCurrentAQI();
 				if (value >= 0) {
 					String text = "指数" + value + "，" + Weather.AirQualityIndex.getAQITitle(value);
 					view.setText(text);
 					int color = getResources().getColor(Weather.AirQualityIndex.getAQIColor(value));
 					view.setTextColor(color);
 				} else {
 					view.setText("--");
 				}
 			} else {
 				// isOk = false;
 				Log.e(tag, "can't get AQI");
 			}
 			if (!isOk) {
 				ToastService.toastLong(getApplicationContext(), getResources().getString(R.string.connect_failed));
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
