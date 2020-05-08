 package net.trajano.gasprices;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Scanner;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.util.Log;
 
 /**
  * <p>
  * This is a service that performs an update to the Gas Prices
  * {@link SharedPreferences}. This service is called using the
  * {@link Context#startService(Intent)}.
  * </p>
  * <p>
  * This is an {@link IntentService} which means:
  * </p>
  * <ul>
  * <li>
  * we do not have to worry about creating a background task because this will
  * already be running in a background task.</li>
  * <li>we don't have to worry about cleaning up by invoking
  * {@link GasPricesUpdateService#stopSelf()}.</li>
  * </ul>
  * 
  * @author Archimedes Trajano (developer@trajano.net)
  * 
  */
 public class GasPricesUpdateService extends IntentService {
 	/**
 	 * This will schedule an update using the AlarmManager, that way the service
 	 * is not continuously running. It will cancel any previously defined alarms
 	 * before creating a new one.
 	 */
 	public static void scheduleUpdate(final Context context) {
 		final PreferenceAdaptor preferences = new PreferenceAdaptor(context);
 		final AlarmManager alarmManager = (AlarmManager) context
 				.getApplicationContext()
 				.getSystemService(Context.ALARM_SERVICE);
 		final Intent intent = new Intent(context, GasPricesUpdateService.class);
 		final PendingIntent pendingIntent = PendingIntent.getService(context,
 				0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 		alarmManager.cancel(pendingIntent);
 		alarmManager.set(AlarmManager.RTC, preferences.getNextUpdateTime(),
 				pendingIntent);
 	}
 
 	/**
 	 * Define the name of the service.
 	 */
 	public GasPricesUpdateService() {
 		super("GasPricesUpdateIntentService");
 	}
 
 	/**
 	 * This will connect to the Internet to get the gas price data and return
 	 * the parsed {@link JSONObject}. This will return <code>null</code> if
 	 * there is an error parsing the data because there isn't anything that can
 	 * be done if there is a parse error, but the {@link IOException} is still
 	 * thrown for any communication errors.
 	 * 
 	 * @return a parsed JSONObject.
 	 * @throws IOException
 	 *             I/O error.
 	 */
 	private JSONObject getGasPricesDataFromInternet() throws IOException {
 		final HttpURLConnection urlConnection = (HttpURLConnection) new URL(
 				"http://www.tomorrowsgaspricetoday.com/mobile/json_mobile_data.php")
 				.openConnection();
 		try {
 			// Read the JSON data, skip the first character since it
 			// breaks the parsing.
 			final String jsonData = new Scanner(urlConnection.getInputStream())
 					.useDelimiter("\\A").next().substring(1);
 
			final Object value = new JSONTokener(jsonData).nextValue();
			if (value instanceof JSONObject) {
				return (JSONObject) value;
			} else {
				throw new IOException("Did not get a proper JSON object");
			}
 		} catch (final JSONException e) {
 			Log.e("GasPrices", e.getMessage());
			throw new IOException(e);
 		} finally {
 			urlConnection.disconnect();
 		}
 	}
 
 	/**
 	 * <p>
 	 * This is the only place in the application where an Internet request is
 	 * performed. This will only function if background data is enabled on the
 	 * phone, if not then updates will not function. Once the update is
 	 * completed, it will:
 	 * </p>
 	 * <ul>
 	 * <li>reschedule itself to be started up again using the
 	 * {@link AlarmManager}</li>
 	 * <li>send an {@link AppWidgetManager#ACTION_APPWIDGET_UPDATE} to all the
 	 * widgets.</li>
 	 * </ul>
 	 * <p>
 	 * Note there is no need to use a custom intent because
 	 * {@link SharedPreferences#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)}
 	 * is part of the application and would handle it from there.
 	 * </p>
 	 * {@inheritDoc}
 	 * 
 	 * TODO is this the right place to do this? Should I still use AsyncTask?
 	 * The reason why this is here is because this is the only way I know how to
 	 * get the widget to update.
 	 */
 	@Override
 	protected void onHandleIntent(final Intent intent) {
 		final PreferenceAdaptor preferences = new PreferenceAdaptor(this);
 
 		final PreferenceAdaptorEditor editor = preferences.edit();
 
 		try {
 			final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			final boolean backgroundEnabled = cm.getBackgroundDataSetting();
 			if (!backgroundEnabled) {
 				return;
 			}
 			editor.setJsonData(getGasPricesDataFromInternet());
 
 		} catch (final IOException e) {
 			Log.e("GasPrices", e.getMessage() + " and cry");
 		} finally {
 			// schedule the next update.
 			scheduleUpdate(this);
 			// update the widgets
 			{
 				final AppWidgetManager widgetManager = AppWidgetManager
 						.getInstance(this);
 				final ComponentName widgetComponent = new ComponentName(this,
 						GasPricesWidgetProvider.class);
 				final int[] widgetIds = widgetManager
 						.getAppWidgetIds(widgetComponent);
 				if (widgetIds.length > 0) {
 					final Intent update = new Intent();
 					update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
 							widgetIds);
 					update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
 					sendBroadcast(update);
 				}
 			}
 			editor.setLastUpdatedToNow();
 			editor.apply();
 			Log.d("GasPrices", "done update");
 		}
 	}
 
 }
