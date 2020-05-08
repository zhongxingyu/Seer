 /*
  * Copyright 2012 
  *
  * Licensed under the xxxx
  */
 
 package com.heneryh.aquanotes.service;
 
 import com.heneryh.aquanotes.io.ApexExecutor;
 import com.heneryh.aquanotes.io.ApexStateXMLParser;
 import com.heneryh.aquanotes.io.NewXmlHandler.HandlerException;
 import com.heneryh.aquanotes.provider.AquaNotesDbContract;
 import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
 import com.heneryh.aquanotes.provider.AquaNotesDbProvider;
 import com.heneryh.aquanotes.ui.widget.Widget1x1;
 import com.heneryh.aquanotes.ui.widget.Widget2x1;
 import com.heneryh.aquanotes.ui.widget.Widget2x2;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.client.HttpClient;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HttpContext;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProviderInfo;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.ResultReceiver;
 import android.provider.BaseColumns;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 import android.util.Log;
 import android.webkit.WebView;
 import android.widget.ProgressBar;
 import android.widget.RemoteViews;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.zip.GZIPInputStream;
 
 /**
  * Background {@link Service} that synchronizes data living in
  * {@link AquaNotesDbProvider}. Reads data from remote controllers.
  */
 public class SyncService extends IntentService {
 	private static final String TAG = "SyncService";
 
 	/**
 	 * Intent actions and extras
 	 */
 	public static final String ACTION_UPDATE_SINGLE = "com.heneryh.aquanotes.UPDATE_SINGLE";
 	public static final String ACTION_UPDATE_ALL = "com.heneryh.aquanotes.UPDATE_ALL";
 
 	public static final String STATUS_UPDATE = "com.heneryh.aquanotes.STATUS_UPDATE";
 	public static final String STATUS_RESULT = "result";
 
 	/**
 	 * Status flags to be sent back to the calling activity via the receiver
 	 */
 	public static final int STATUS_RUNNING = 0x1;
 	public static final int STATUS_ERROR = 0x2;
 	public static final int STATUS_FINISHED = 0x3;
 
 	/**
 	 * Flag if there is an update thread already running. We only launch a new
 	 * thread if one isn't already running.
 	 */
 	private static boolean sThreadRunning = false;
 
 	private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
 
 	/**
 	 * There is an embedded http client helper below
 	 */
 	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
 	private static final String ENCODING_GZIP = "gzip";
 
 	private ApexExecutor mRemoteExecutor;
 
 	private ContentResolver dbResolverSyncSrvc;
 
 	Context mSyncServiceContext;
 
 	/**
 	 * Main service methods
 	 */
 	public SyncService() {
 		super(TAG);
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		/**
 		 * helper class for defaultHttpClient seen below
 		 */
 		final HttpClient httpClient = getHttpClient(this);
 
 		/**
 		 * Interface to the database which is passed into the remoteExecutor.  Is there an advantage to
 		 * having a centralized one rather than each getting there own???  Might want to look at this more.
 		 * Seems like the answer is that you need the context to get the resolver
 		 */
 		dbResolverSyncSrvc = getContentResolver();
 
 		/**
 		 * Create the executor for the controller of choice.  Now it is just the apex but I can see using
 		 * other ones like the DA.  Pass in the http client and database resolver it will need to do its job.
 		 */
 		mRemoteExecutor = new ApexExecutor(this, httpClient, dbResolverSyncSrvc);
 
 		mSyncServiceContext = this;
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");
 
 		/**
 		 * Using the intent, we can tell why we are running this service
 		 */
 		Cursor cursor = null;
 
 		// This came from the timer expiring or from the gui, either way, push all controllers onto the queue.
 		if (ACTION_UPDATE_ALL.equals(intent.getAction()) || Intent.ACTION_SYNC.equals(intent.getAction())) {
 			try {
 				Uri controllersQueryUri = Controllers.buildQueryControllersUri();
 				cursor = dbResolverSyncSrvc.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
 				if (cursor != null && cursor.moveToFirst()) {
 					while (!cursor.isAfterLast()) {
 						Integer controllerId = cursor.getInt(ControllersQuery._ID); 
 						requestUpdate(controllerId);
 						cursor.moveToNext();
 					}
 				}
 			} catch (SQLException e) {
 				Log.e(TAG, "getting controller list", e);	
 				// need a little more here!
 			} finally {
 				if (cursor != null) {
 					cursor.close();
 				}
 			}
 		} else if (ACTION_UPDATE_SINGLE.equals(intent.getAction())) { // This came from the a widget update, id is in the queue
 
 		} 
 
 		/**
 		 *  Only start processing thread if not already running, if the thread was running it would
 		 *  grab the queue items
 		 */
 		synchronized (sLock) {
 			if (!sThreadRunning) {
 				sThreadRunning = true;
 				new SyncThread().execute();
 			}
 		}
 	} // end of onHandleIntent()
 
 	/**
 	 * Background task to handle Apex lookups. This correctly shows and
 	 * hides the loading animation from the GUI thread before starting a
 	 * background query to the API. When finished, it transitions
 	 * back to the GUI thread where it updates with the newly-found entry.
 	 */
 	private class SyncThread extends AsyncTask<String, Integer, Boolean> {
 
 		/**
 		 * Before jumping into background thread, start sliding in the
 		 * {@link ProgressBar}. We'll only show it once the animation finishes.
 		 * 
 		 * This method is executed in the UI thread space and has access to
 		 * graphical elements of the UI.
 		 */
 		@Override
 		protected void onPreExecute() {
 			Intent result = new Intent(STATUS_UPDATE);
 			result.putExtra(STATUS_RESULT,STATUS_RUNNING);
 			sendBroadcast(result);
 		}
 
 		/**
 		 * Perform the background query.
 		 * 
 		 * This method is executed in the background thread space and
 		 * does NOT have access to the graphical elements of the UI.
 		 */
 		@Override
 		protected Boolean doInBackground(String... args) {
 			Log.d(TAG, "Processing thread started");
 
 			/**
 			 * We can only pass a single result back to the main thread which will then report status
 			 * to the gui.
 			 */
 			boolean resultFailedFlag=false;
 			ContentResolver dbResolverSyncSrvcThread = getContentResolver();
 
 			try {
 				final long startRemote = System.currentTimeMillis();
 
 				/**
 				 * Interval to wait between background widget updates. These will be pulled
 				 * from the database during processing.
 				 */
 				int updateIntervalMins=0;
 				long updateIntervalMillis=0;
 
 				/**
 				 * Length of time before we consider cached data stale. If a widget
 				 * update is requested, and {@link AppWidgetsColumns#LAST_UPDATED} is inside
 				 * this threshold, we use the cached data to build the update.
 				 * Otherwise, we first trigger an update.
 				 */
 				long probesCacheThrottle = (0) * DateUtils.MINUTE_IN_MILLIS;
 
 				updateIntervalMins = 99;  // trying to find the right spot for this...
 				// We don't want to go nuts with a million updates prior to the 
 				// update frequency being set the first time. It may not even be needed any more with 
 				// various fixes over time.
 
 				long now = System.currentTimeMillis();
 
 				while (hasMoreUpdates()) {
 
 					// Pull the next update request off the queue
 					// and build a database Uri from it.
 					int controllerId = getNextUpdate();
 					int widgetId = -1;
 					Uri controllerUri = Controllers.buildQueryControllerXUri(controllerId);
 
 					// Check if controller is configured in the database, 
 					// and if we need to then update cache
 					Cursor cursor = null;
 					boolean isConfigured = false;
 					boolean shouldUpdate = false;
 					try {
 						cursor = dbResolverSyncSrvcThread.query(controllerUri, ControllersQuery.PROJECTION, null, null, null);
 						if (cursor != null && cursor.moveToFirst()) {
 							// Pull the database info for this controller
 							updateIntervalMins = cursor.getInt(ControllersQuery.UPDATE_INTERVAL); // getInt() will autoconvert the string to an int.
 							long lastUpdated = cursor.getLong(ControllersQuery.LAST_UPDATED);
 							widgetId = cursor.getInt(ControllersQuery.WIDGET);
 
 							// This is a little silly, if the db query works then it must be configured.
 
 							if(lastUpdated>0) {
 								// How long ago was the controller updated?
 								float deltaMinutes = (float)(now - lastUpdated) / (float)(DateUtils.MINUTE_IN_MILLIS);
 								Log.d(TAG, "Delta since last update for controller id " + controllerId + " is " + deltaMinutes + " min");
 
 								// To reduce cluttering the net, if we just got an update, don't do
 								// it again.
 								shouldUpdate = (Math.abs(now - lastUpdated) > probesCacheThrottle);
 							} else {
 								Log.d(TAG, "Configured but not yet pulled any data.");
 								shouldUpdate = true;
 							}
 						}
 					} catch (SQLException e) {
 						Log.e(TAG, "Checking if the controller is configured", e);
 						resultFailedFlag=true;
 
 						Intent result = new Intent(STATUS_UPDATE);
 						final Bundle bundle = new Bundle();
 						bundle.putInt(STATUS_RESULT,STATUS_ERROR);
 						bundle.putString(Intent.EXTRA_TEXT, e.toString());
 						sendBroadcast(result);
 					} finally {
 						if (cursor != null) {
 							cursor.close();
 						}
 					}
 
 					if (shouldUpdate) {
 						try {
 							Log.d(TAG, "Going to perform an update");
 
 							// Last update is outside throttle window, so update again
 
 							// The logic for handling status, data and programs similarly is not fully hashed out yet...
 							DefaultHandler  xmlParser = new ApexStateXMLParser(dbResolverSyncSrvcThread, controllerUri);
 							mRemoteExecutor.executeGet(controllerUri, xmlParser);
 							Log.d(TAG, "remote sync took " + (System.currentTimeMillis() - startRemote) + "ms");
 
 							// Announce success to any surface listener
 							Log.d(TAG, "sync finished");
 
 							// Process this update through the correct provider
 							AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mSyncServiceContext);
 
 							if(widgetId>0) {
 								AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(widgetId);
 								String providerName = info.provider.getClassName();   // <--- there are crash reports of null pointer here.  How?
 								RemoteViews updateViews = null;
 								Log.d(TAG, "Build a graphical update whatever type of widget this is.");
 								if (providerName.equals(Widget2x1.class.getName())) {
 									Log.d(TAG, "Building a 2x1 widget, ID = " + controllerId + ".");
 									Log.d(TAG, "Building a 2x1 widget, Uri = " + controllerUri + ".");
 									updateViews = Widget2x1.buildUpdate(mSyncServiceContext, controllerUri);
 								} else if (providerName.equals(Widget2x2.class.getName())) {
 									Log.d(TAG, "Building a 2x2 widget, ID = " + controllerId + ".");
 									Log.d(TAG, "Building a 2x2 widget, Uri = " + controllerUri + ".");
 									updateViews = Widget2x2.buildUpdate(mSyncServiceContext, controllerUri);
 								} else if (providerName.equals(Widget1x1.class.getName())) {
 									Log.d(TAG, "Building a 1x1 widget, ID = " + controllerId + ".");
 									Log.d(TAG, "Building a 1x1 widget, Uri = " + controllerUri + ".");
 									updateViews = Widget1x1.buildUpdate(mSyncServiceContext, controllerUri);
 								}
 
 								// Push this update to surface
 								if (updateViews != null) {
 									Log.d(TAG, "Pushing update to the surface, ID = " + controllerId + ".");
 									appWidgetManager.updateAppWidget(widgetId, updateViews);
 								} else {
 									Log.e(TAG, "Some problem building the view, not pushed to the surface.");
 								}
 							}
 					} catch (HandlerException e) {
 						Log.e(TAG, "Problem while syncing", e);
 						resultFailedFlag=true;							
 						Intent result = new Intent(STATUS_UPDATE);
 						final Bundle bundle = new Bundle();
 						bundle.putInt(STATUS_RESULT,STATUS_ERROR);
 						bundle.putString(Intent.EXTRA_TEXT, e.toString());
						result.putExtras(bundle);
 						sendBroadcast(result);
 					} // end of catch
 				} // end of if(should update)
 			} // end of while(more updates)
 
 			// Schedule next update alarm.  updateFreqMins will be 99 if not at least one configured
 			// updateFreqMins will be set from the last widget updated above
 			if(updateIntervalMins!=99 && updateIntervalMins!=0) {
 				updateIntervalMillis = updateIntervalMins * DateUtils.MINUTE_IN_MILLIS;
 
 				Time nextTime = new Time();
 				nextTime.set(now + updateIntervalMillis);
 				long nextUpdate = nextTime.toMillis(false);
 
 				float deltaMinutes = (float)(nextUpdate - now) / (float)DateUtils.MINUTE_IN_MILLIS;
 				Log.d(TAG, "Requesting next update in " + deltaMinutes + " min");
 
 				Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
 				updateIntent.setClass(mSyncServiceContext, SyncService.class);
 
 				PendingIntent pendingIntent = PendingIntent.getService(mSyncServiceContext, 0, updateIntent, 0);
 
 				//The following is a hack for some failure condition that causes the alarm
 				// to not get reset
 				long repeatInterval = updateIntervalMillis + 1*DateUtils.MINUTE_IN_MILLIS;
 
 				// Schedule alarm, and force the device awake for this update
 				AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 				//alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
 				alarmManager.setRepeating(AlarmManager.RTC, nextUpdate, repeatInterval, pendingIntent);
 				Log.d(TAG, "remote sync took " + (System.currentTimeMillis() - startRemote) + "ms");
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "Problem while syncing", e);
 			resultFailedFlag = true;
 		} // end of catch exception
 		return resultFailedFlag;
 	} // end of doInBackgrount
 
 	/**
 	 * Our progress update pushes a timestamp/error update.
 	 * 
 	 * This method is executed in the UI thread space.
 	 */
 	@Override
 	protected void onProgressUpdate(Integer... arg) {
 		//if (guiStatusReceiver != null) guiStatusReceiver.send(STATUS_RUNNING, Bundle.EMPTY);	
 	}
 
 	/**
 	 * When finished, push the newly-found entry content into our
 	 * {@link WebView} and hide the {@link ProgressBar}.
 	 * 
 	 * This method is executed in the UI thread space.
 	 */
 	@Override
 	protected void onPostExecute(Boolean resultFailedFlag) {
 		// Announce success to any surface listener
 		final Bundle bundle = new Bundle();
 		Intent result = new Intent(STATUS_UPDATE);
 		if (resultFailedFlag) {
 			bundle.putInt(STATUS_RESULT,STATUS_ERROR);
 		}
 		else {
 			bundle.putInt(STATUS_RESULT,STATUS_FINISHED);
 		}
 		sendBroadcast(result);
 
 		// No updates remaining, so stop service
 		stopSelf();
 	}
 }
 
 /**
  * Generate and return a {@link HttpClient} configured for general use,
  * including setting an application-specific user-agent string.
  */
 public static HttpClient getHttpClient(Context context) {
 	final HttpParams params = new BasicHttpParams();
 
 	// Use generous timeouts for slow mobile networks
 	HttpConnectionParams.setConnectionTimeout(params, 20 * SECOND_IN_MILLIS);
 	HttpConnectionParams.setSoTimeout(params, 20 * SECOND_IN_MILLIS);
 
 	HttpConnectionParams.setSocketBufferSize(params, 8192);
 	HttpProtocolParams.setUserAgent(params, buildUserAgent(context));
 
 	final DefaultHttpClient client = new DefaultHttpClient(params);
 
 	client.addRequestInterceptor(new HttpRequestInterceptor() {
 		public void process(HttpRequest request, HttpContext context) {
 			// Add header to accept gzip content
 			if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
 				request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
 			}
 		}
 	});
 
 	client.addResponseInterceptor(new HttpResponseInterceptor() {
 		public void process(HttpResponse response, HttpContext context) {
 			// Inflate any responses compressed with gzip
 			final HttpEntity entity = response.getEntity();
 			final Header encoding = entity.getContentEncoding();
 			if (encoding != null) {
 				for (HeaderElement element : encoding.getElements()) {
 					if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
 						response.setEntity(new InflatingEntity(response.getEntity()));
 						break;
 					}
 				}
 			}
 		}
 	});
 	return client;
 }
 
 /**
  * Build and return a user-agent string that can identify this application
  * to remote servers. Contains the package name and version code.
  */
 private static String buildUserAgent(Context context) {
 	try {
 		final PackageManager manager = context.getPackageManager();
 		final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
 
 		// Some APIs require "(gzip)" in the user-agent string.
 		return info.packageName + "/" + info.versionName
 				+ " (" + info.versionCode + ") (gzip)";
 	} catch (NameNotFoundException e) {
 		return null;
 	}
 }
 
 /**
  * Simple {@link HttpEntityWrapper} that inflates the wrapped
  * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
  */
 private static class InflatingEntity extends HttpEntityWrapper {
 	public InflatingEntity(HttpEntity wrapped) {
 		super(wrapped);
 	}
 
 	@Override
 	public InputStream getContent() throws IOException {
 		return new GZIPInputStream(wrappedEntity.getContent());
 	}
 
 	@Override
 	public long getContentLength() {
 		return -1;
 	}
 }
 
 
 /**
  * Maintain a queue of widgets that are requesting update.  
  * 
  */
 
 /**
  * Lock used when maintaining queue of requested updates.
  */
 private static Object sLock = new Object();
 
 /**
  * Internal queue of requested widget updates. You <b>must</b> access
  * through {@link #requestUpdate(int[])} or {@link #getNextUpdate()} to make
  * sure your access is correctly synchronized.
  */
 private static Queue<Integer> sControllerIds = new LinkedList<Integer>();
 
 /**
  * Request updates for the given widgets. Will only queue them up, you are
  * still responsible for starting a processing thread if needed, usually by
  * starting the parent service.
  */
 public static void requestUpdate(int[] controllerIds) {
 	synchronized (sLock) {
 		for (int controllerId : controllerIds) {
 			sControllerIds.add(controllerId);
 		}
 	}
 }
 
 /**
  * Request updates for the given widgets. Will only queue them up, you are
  * still responsible for starting a processing thread if needed, usually by
  * starting the parent service.
  */
 public static void requestUpdate(int controllerId) {
 	synchronized (sLock) {
 		sControllerIds.add(controllerId);
 	}
 }
 
 /**
  * Peek if we have more updates to perform. This method is special because
  * it assumes you're calling from the update thread, and that you will
  * terminate if no updates remain. (It atomically resets
  * {@link #sThreadRunning} when none remain to prevent race conditions.)
  */
 private static boolean hasMoreUpdates() {
 	synchronized (sLock) {
 		boolean hasMore = !sControllerIds.isEmpty();
 		if (!hasMore) {
 			sThreadRunning = false;
 		}
 		return hasMore;
 	}
 }
 
 /**
  * Poll the next widget update in the queue.
  */
 private static int getNextUpdate() {
 	synchronized (sLock) {
 		if (sControllerIds.peek() == null) {
 			return AppWidgetManager.INVALID_APPWIDGET_ID;
 		} else {
 			return sControllerIds.poll();
 		}
 	}
 }
 private interface ControllersQuery {
 	String[] PROJECTION = {
 			//              String CONTROLLER_ID = "_id";
 			//              String TITLE = "title";
 			//              String WAN_URL = "wan_url";
 			//              String LAN_URL = "wifi_url";
 			//              String WIFI_SSID = "wifi_ssid";
 			//              String USER = "user";
 			//              String PW = "pw";
 			//              String LAST_UPDATED = "last_updated";
 			//              String UPDATE_INTERVAL = "update_i";
 			//              String DB_SAVE_DAYS = "db_save_days";
 			//              String CONTROLLER_TYPE = "controller_type";
 			BaseColumns._ID,
 			AquaNotesDbContract.Controllers.TITLE,
 			AquaNotesDbContract.Controllers.WAN_URL,
 			AquaNotesDbContract.Controllers.LAN_URL,
 			AquaNotesDbContract.Controllers.WIFI_SSID,
 			AquaNotesDbContract.Controllers.USER,
 			AquaNotesDbContract.Controllers.PW,
 			AquaNotesDbContract.Controllers.LAST_UPDATED,
 			AquaNotesDbContract.Controllers.UPDATE_INTERVAL,
 			AquaNotesDbContract.Controllers.DB_SAVE_DAYS,
 			AquaNotesDbContract.Controllers.MODEL,
 			AquaNotesDbContract.Controllers.WIDGET,
 	};
 
 	int _ID = 0;
 	int TITLE = 1;
 	int WAN_URL = 2;
 	int LAN_URL = 3;
 	int WIFI_SSID = 4;
 	int USER = 5;
 	int PW = 6;
 	int LAST_UPDATED = 7;
 	int UPDATE_INTERVAL = 8;
 	int DB_SAVE_DAYS = 9;
 	int MODEL = 10;
 	int WIDGET = 11;
 }
 }
