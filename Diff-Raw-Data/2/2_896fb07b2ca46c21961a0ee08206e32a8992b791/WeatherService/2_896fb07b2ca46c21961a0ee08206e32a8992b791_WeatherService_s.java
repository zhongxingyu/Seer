 package net.rubyeye.ww.service;
 
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.rubyeye.ww.R;
 import net.rubyeye.ww.WeatherApp;
 import net.rubyeye.ww.WeatherDetail;
 import net.rubyeye.ww.WhetherWeatherSetting;
 import net.rubyeye.ww.data.GoogleWeatherFetcher;
 import net.rubyeye.ww.data.Unit;
 import net.rubyeye.ww.data.Weather;
 import net.rubyeye.ww.data.WeatherData;
 import net.rubyeye.ww.data.WeatherImageFetcher;
 import net.rubyeye.ww.utils.Constants;
 import net.rubyeye.ww.widget.WeatherWidget;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 /**
  * Weather service running on background
  * 
  * @author dennis
  * 
  */
 public class WeatherService extends Service {
 	private final class LoadWeatherTask extends TimerTask {
 		@Override
 		public void run() {
			loadWeather(null, false);
 		}
 	}
 
 	static final String CLASSTAG = WeatherService.class.getSimpleName();
 	private Timer timer;
 	private TimerTask timerTask;
 	private NotificationManager notificationManager;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		this.timer = new Timer();
 		this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		scheduleTask();
 	}
 
 	private void scheduleTask() {
 		int updateInterval = WhetherWeatherSetting.getUpdateInterval(this);
 		Log.w(Constants.LOGTAG, " Schedule load weather task after "
 				+ updateInterval + " hours");
 		this.timerTask = new LoadWeatherTask();
 		int delay = updateInterval * 3600 * 1000;
 		this.timer.scheduleAtFixedRate(this.timerTask, delay, delay);
 	}
 
 	private String getCurrentLocale() {
 		return getResources().getConfiguration().locale.toString();
 	}
 
 	private WeatherData getTodayWeather(String city) {
 		if (city == null || city.trim().length() == 0)
 			city = WhetherWeatherSetting.getCity(this);
 		city = city.trim();
 		GoogleWeatherFetcher fetcher = new GoogleWeatherFetcher(city,
 				getCurrentLocale());
 		WeatherData weatherData = fetcher.getWeather();
 		weatherData.city = city;
 		return weatherData;
 	}
 
 	private void loadWeather(String city, boolean atNow) {
 		try {
 			Log.d(Constants.LOGTAG, " " + CLASSTAG
 					+ " start loading weather infomation");
 			if (atNow) {
 				updateWeatherNow(city);
 			} else {
 				String lastUpdate = WhetherWeatherSetting.getLastUpdate(this);
 				Date lastDate = Constants.simpleDateFormat.parse(lastUpdate);
 				long limit = WhetherWeatherSetting.getUpdateInterval(this) * 3600 * 1000;
 				if (System.currentTimeMillis() - lastDate.getTime() >= limit) {
 					updateWeatherNow(city);
 				}
 			}
 			Log.d(Constants.LOGTAG, " " + CLASSTAG
 					+ " end loading weather infomation");
 		} catch (Throwable t) {
 			Log.e(Constants.LOGTAG, " " + CLASSTAG, t);
 		}
 	}
 
 	private void updateWeatherNow(String city) {
 		WeatherData data = getTodayWeather(city);
 		if (data != null) {
 			updateWeather(data);
 		}
 	}
 
 	private Intent updateWeather(WeatherData data) {
 		Intent detailIntent = updateWidget(data);
 		notifySevereWeather(data, detailIntent);
 		notifyTempChanged(data, detailIntent);
 		return detailIntent;
 
 	}
 
 	private Intent updateWidget(WeatherData data) {
 		RemoteViews updateViews = new RemoteViews(getPackageName(),
 				R.layout.today_weather);
 		Weather todayWeather = data.todayWeather;
 		Intent detailIntent = null;
 		if (data != null && todayWeather != null) {
 			updateViews.setTextViewText(R.id.city, data.city);
 			updateViews.setTextViewText(R.id.condition, todayWeather.condition);
 			updateViews.setTextViewText(R.id.temp, todayWeather.lowTemp + " ~ "
 					+ todayWeather.highTemp + " " + data.unit.getUnit());
 			Bitmap image = WeatherImageFetcher.getWeatherImage(todayWeather);
 
 			detailIntent = new Intent(this, WeatherDetail.class);
 			WeatherApp weatherApp = (WeatherApp) getApplication();
 			weatherApp.setWeatherData(data);
 			weatherApp.setWeatherImage(null);
 			if (image != null) {
 				updateViews.setImageViewBitmap(R.id.weather_image, image);
 				weatherApp.setWeatherImage(image);
 			}
 		}
 
 		PendingIntent pending = PendingIntent.getActivity(this, 0,
 				detailIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
 
 		// updateViews.setOnClickPendingIntent(R.layout.today_weather, pending);
 		updateViews.setOnClickPendingIntent(R.id.city, pending);
 		updateViews.setOnClickPendingIntent(R.id.condition, pending);
 		updateViews.setOnClickPendingIntent(R.id.temp, pending);
 		updateViews.setOnClickPendingIntent(R.id.weather_image, pending);
 
 		ComponentName thisWidget = new ComponentName(WeatherService.this,
 				WeatherWidget.class);
 
 		AppWidgetManager manager = AppWidgetManager
 				.getInstance(WeatherService.this);
 		manager.updateAppWidget(thisWidget, updateViews);
 		WhetherWeatherSetting.updateLastUpdate(this);
 		return detailIntent;
 	}
 
 	private void notifyTempChanged(WeatherData data, Intent detailIntent) {
 		if (data != null && data.todayWeather != null) {
 			Weather todayWeather = data.todayWeather;
 
 			int oldLowTemp = Integer.parseInt(WhetherWeatherSetting
 					.getLastLowTemp(this));
 			int lowTemp = Integer.parseInt(todayWeather.lowTemp);
 			// cast to c temp for calc
 			if (data.unit == Unit.US) {
 				lowTemp = getCTemp(lowTemp);
 			}
 			WhetherWeatherSetting.setLastLowTemp(this, String.valueOf(lowTemp));
 			if (oldLowTemp != WhetherWeatherSetting.INVALID_LOW_TEMP) {
 				int extent = lowTemp - oldLowTemp;
 				int absExtent = Math.abs(extent);
 				if (WhetherWeatherSetting.isTempUpdateRemainderEnable(this)
 						&& absExtent >= WhetherWeatherSetting
 								.getTempUpdateExtent(this)) {
 					String title = getResources()
 							.getString(R.string.temp_alert);
 					String alert = extent < 0 ? getResources().getString(
 							R.string.temp_down_alert) : getResources()
 							.getString(R.string.temp_up_alert);
 					// cast to f temp for display
 					if (data.unit == Unit.US) {
 						absExtent = Math.abs(getFTemp(oldLowTemp)
 								- Integer.parseInt(todayWeather.lowTemp));
 					}
 					notifyWeather(title, title + ":" + alert + " " + absExtent
 							+ data.unit.getUnit(), detailIntent);
 				}
 			}
 		}
 	}
 
 	private void notifySevereWeather(WeatherData data, Intent detailIntent) {
 		if (data != null && data.todayWeather != null) {
 			Weather todayWeather = data.todayWeather;
 			if (WhetherWeatherSetting.isSevereWeatherReminderEnable(this)
 					&& todayWeather.isSevereWeather(getResources()
 							.getStringArray(R.array.severe_weathers))) {
 				String title = getResources().getString(
 						R.string.severe_weather_alert);
 				notifyWeather(title, title + ":" + todayWeather.condition
 						+ " @ " + data.city, detailIntent);
 			}
 		}
 	}
 
 	public int getCTemp(int f) {
 		return (int) ((f - 32) / 1.8);
 	}
 
 	public int getFTemp(int c) {
 		return (int) (c * 1.8 + 32);
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		if (intent.getAction().equals(Intent.ACTION_RUN)) {
 			boolean atNow = intent.getBooleanExtra(
 					Constants.EXTRA_UPDATE_AT_NOW, false);
 			if ((intent.getData() != null)
 					&& (intent.getData().getEncodedQuery() != null)
 					&& (intent.getData().getEncodedQuery().length() > 5)) {
 				String city = intent.getData().getQueryParameter("city");
 				loadWeather(city, atNow);
 			}
 			if (intent.getBooleanExtra(WhetherWeatherSetting.RESCHEDULE, false)) {
 				this.timerTask.cancel();
 				scheduleTask();
 			}
 		} else if (intent.getAction().equals(Constants.ACTION_STOP)) {
 			stopSelf();
 		} else if (intent.getAction().equals(Constants.ACTION_NETWORK_CHANGED)) {
 			NetworkInfo networkInfo = (NetworkInfo) intent
 					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
 			if (networkInfo.isAvailable()) {
 				loadWeather(null, false);
 			}
 		}
 
 	}
 
 	private int notifyId = 0;
 
 	private void notifyWeather(String title, String content, Intent detailIntent) {
 		PendingIntent pendingIntent = PendingIntent.getActivity(this,
 				Intent.FLAG_ACTIVITY_NEW_TASK, detailIntent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		final Notification n = new Notification(R.drawable.severe_weather_24,
 				title, System.currentTimeMillis());
 		n.setLatestEventInfo(this, title, content, pendingIntent);
 		this.notificationManager.notify(notifyId++, n);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		this.timerTask.cancel();
 		this.timer.cancel();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 }
