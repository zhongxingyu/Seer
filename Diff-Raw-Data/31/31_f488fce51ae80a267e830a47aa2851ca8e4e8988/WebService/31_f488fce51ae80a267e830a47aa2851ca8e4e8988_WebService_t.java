 package org.tsg.web;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.ResultReceiver;
 import android.util.Log;
 
 /**
  * TODO
  * 
  * @author Daniel Skinner <daniel@dasa.cc>
  * 
  */
 public class WebService extends Service {
 
 	private static boolean LONG_CACHE = false;
 
 	//
 	private static boolean LOGGING = false;
 	private static final String LOG_TAG = "WebService";
 
 	//
	private static Class WebClientClass = WebClient.class;

	//
 	public static final int METHOD_GET = 0;
 	public static final int METHOD_POST = 1;
 	public static final int METHOD_PUT = 2;
 	public static final int METHOD_DELETE = 3;
 
 	public static final int CONTENT_AUTO = 0;
 	public static final int CONTENT_RAW = 1;
 	public static final int CONTENT_STRING = 2;
 
 	public static final int TIME_SECOND = 0;
 	public static final int TIME_MINUTE = 1;
 	public static final int TIME_HOUR = 2;
 	public static final int TIME_DAY = 3;
 	public static final int TIME_MONTH = 4;
 	public static final int TIME_YEAR = 5;
 
 	//
 	static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
 	static final String ENCODING_GZIP = "gzip";
 
 	//
 	private static int POOL_SIZE = 3;
 
 	// managed during service life cycle
 	private WebContentProvider.Database mDatabase;
 	private ExecutorService mPool;
 	private static final List<Integer> mStartIds = new ArrayList<Integer>();
 	private static final Map<String, WebServiceResultReceiver> mResultReceivers = new ConcurrentHashMap<String, WebServiceResultReceiver>();
 
 	/**
 	 * Debug logging for org.tsg.web classes.
 	 * 
 	 * @param objects
 	 */
 	static void log(int i, Object... objects) {
 		if (LOGGING) {
 			String msg = "";
 			for (Object object : objects) {
 				msg += String.valueOf(object) + " ";
 			}
 
 			switch (i) {
 			case Log.DEBUG:
 				Log.d(LOG_TAG, msg);
 				break;
 			case Log.ERROR:
 				Log.e(LOG_TAG, msg);
 				break;
 			case Log.INFO:
 				Log.i(LOG_TAG, msg);
 				break;
 			case Log.WARN:
 				Log.w(LOG_TAG, msg);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Enable logging for web package.
 	 */
 	public static void enableLogging() {
 		LOGGING = true;
 	}
 
 	/**
 	 * Set the number of executor threads available for handling queued requests.
 	 * 
 	 * @param size
 	 */
 	public static void setPoolSize(int size) {
 		POOL_SIZE = size;
 	}
 
 	/**
	 * Set subclass of WebClient to be used for requests.
	 * 
	 * @param cls
	 */
	public static void setWebClient(Class cls) {
		WebClientClass = cls;
	}

	/**
 	 * Useful if requests are already cached and working off-line. Sets all
 	 * request cache lengths to 999 years, forcing a pull from cache db if
 	 * previously fetched.
 	 */
 	public static void enableLongCache() {
 		LONG_CACHE = true;
 	}
 
 	/**
 	 * Set max size of data in cache db, auto pruning oldest requests first.
 	 * Default is 5000 kilobytes. TODO implement
 	 * 
 	 * @param kilobytes
 	 */
 	public static void setMaxCacheSize(int kilobytes) {
 		WebContentProvider.MAX_CACHE_SIZE = kilobytes;
 	}
 
 	/**
 	 * Runnable that executes network request when service handles an intent.
 	 */
 	protected final class WebServiceHandler implements Runnable {
 
 		private Intent mIntent;
 		private Integer mStartId;
 
 		public WebServiceHandler(Intent intent, Integer startId) {
 			synchronized (mStartIds) {
 				mStartIds.add(startId);
 			}
 			mIntent = intent;
 			mStartId = startId;
 		}
 
 		@Override
 		public void run() {
 
 			log(Log.DEBUG, "Starting Request");
 
 			if (mIntent == null) {
 				// TODO
				log(Log.DEBUG, "Intent is null! returning ...");
 				return;
 			}
 
 			Bundle data = mIntent.getExtras();
 
 			ResultReceiver receiver = data.getParcelable("receiver");
 			WebRequest request = data.getParcelable("request");
 
 			Bundle bundle = new Bundle();
 			bundle.putBundle(WebReceiver.DEVELOPER_EXTRAS, request.mDeveloperExtras);
 
			WebClient client;
			try {
				client = (WebClient) WebClientClass.getConstructor(WebRequest.class).newInstance(request);
			} catch (Exception e) {
				e.printStackTrace();
				log(Log.DEBUG, "Error Encountered");
				bundle.putSerializable(WebReceiver.RESPONSE_EXCEPTION, e);
				receiver.send(WebReceiver.STATUS_ERROR, bundle);
				mIntent.putExtra("status", WebReceiver.STATUS_ERROR);
				return;
			}

 			String uuid = mIntent.getStringExtra("uuid");
 			String cacheKey = mIntent.getStringExtra("cacheKey");
 
 			if (LONG_CACHE) {
 				request.mCacheTimeValue = 999;
 				request.mCacheTimeType = TIME_YEAR;
 			}
 
 			try {
 				if (request.mFakeData != null) {
 					log(Log.DEBUG, "Handling FakeData");
 					mDatabase.put(cacheKey, uuid, client.mContentType, request.mFakeData.getBytes(), null);
 				} else if (mDatabase.contains(cacheKey, request.mCacheTimeValue, request.mCacheTimeType)) {
 					log(Log.DEBUG, "Returning cached data");
 					bundle.putBoolean("fromCache", true);
 				} else {
 					// only send WebReceiver.STATUS_RUNNING if making an actual service
 					// call
 					log(Log.DEBUG, "Preparing Request");
 					mIntent.putExtra("status", WebReceiver.STATUS_RUNNING);
 					bundle.putString(WebReceiver.REQUEST_KEY, cacheKey);
 					receiver.send(WebReceiver.STATUS_RUNNING, bundle);
 					bundle = new Bundle();
 					bundle.putBundle(WebReceiver.DEVELOPER_EXTRAS, request.mDeveloperExtras);
 					//
 					log(Log.DEBUG, "Calling Request");
 					client.call();
 					mDatabase.put(cacheKey, uuid, client.mContentType, client.mResponseBytes, client.mResponseContentType);
 					bundle.putInt(WebReceiver.RESPONSE_CODE, client.mResponseCode);
 					bundle.putString(WebReceiver.RESPONSE_MESSAGE, client.mResponseMessage);
 				}
 
 				log(Log.DEBUG, "Notifying Receivers");
 				bundle.putString(WebReceiver.REQUEST_KEY, cacheKey);
 				receiver.send(WebReceiver.STATUS_FINISHED, bundle);
 				mIntent.putExtra("status", WebReceiver.STATUS_FINISHED);
 			} catch (Exception e) {
 				log(Log.DEBUG, "Error Encountered");
 				bundle.putSerializable(WebReceiver.RESPONSE_EXCEPTION, e);
 				receiver.send(WebReceiver.STATUS_ERROR, bundle);
 				mIntent.putExtra("status", WebReceiver.STATUS_ERROR);
 			}
 
 			log(Log.DEBUG, "Stopping Request");
 			synchronized (mStartIds) {
 				mStartIds.remove(mStartId);
 				if (mStartIds.size() == 0) {
 					stopSelf();
 				}
 			}
 			// boolean b = stopSelfResult(mStartId);
 			// log(Log.DEBUG, "stopSelfResult", b);
 		}
 
 	}
 
 	/**
 	 * A parcelable object that stores the Receiver interface for service
 	 * callbacks.
 	 */
 	private static class WebServiceResultReceiver extends ResultReceiver {
 
 		private static final String KEY_RESULT_CODE = "resultCode";
 		private static final String KEY_RESULT_DATA = "resultData";
 
 		private List<WebReceiver> mReceivers = new ArrayList<WebReceiver>();
 		private Bundle mLastResult;
 		private String mRequestKey;
 		private boolean mIsPending;
 
 		public WebServiceResultReceiver(Handler handler, String requestKey) {
 			super(handler);
 			mRequestKey = requestKey;
 
 			mLastResult = new Bundle();
 			mLastResult.putInt(KEY_RESULT_CODE, WebReceiver.STATUS_CREATED);
 			mLastResult.putBundle(KEY_RESULT_DATA, Bundle.EMPTY);
 		}
 
 		public boolean isPending() {
 			return mIsPending;
 		}
 
 		public void pause() {
 			mIsPending = true;
 		}
 
 		public synchronized void resume() {
 			mIsPending = false;
 
 			if (mLastResult != null) {
 				// TODO are there race conditions here?
 				int resultCode = mLastResult.getInt(KEY_RESULT_CODE);
 				Bundle resultData = mLastResult.getBundle(KEY_RESULT_DATA);
 				onReceiveResult(resultCode, resultData);
 			}
 		}
 
 		public String getRequestKey() {
 			return mRequestKey;
 		}
 
 		public synchronized List<WebReceiver> getReceivers() {
 			return mReceivers;
 		}
 
 		/**
 		 * Add receiver if not already contained and issue any pending/previous
 		 * results
 		 * 
 		 * @param receiver
 		 */
 		public void addReceiver(WebReceiver receiver) {
 			synchronized (mReceivers) {
 				if (!mReceivers.contains(receiver))
 					mReceivers.add(receiver);
 			}
 
 			if (mLastResult != null) {
 				// TODO are there race conditions here?
 				int resultCode = mLastResult.getInt(KEY_RESULT_CODE);
 				Bundle resultData = mLastResult.getBundle(KEY_RESULT_DATA);
 				onReceiveResult(resultCode, resultData);
 			}
 		}
 
 		/**
 		 * 
 		 */
 		@Override
 		protected synchronized void onReceiveResult(int resultCode, Bundle resultData) {
 			mLastResult.putInt(KEY_RESULT_CODE, resultCode);
 			mLastResult.putBundle(KEY_RESULT_DATA, resultData);
 
 			synchronized (mReceivers) {
 				if (!mIsPending && mReceivers.size() == 0) {
 					mIsPending = true;
 				}
 				// mIsPending = (mReceivers.size() == 0);
 
 				if (!mIsPending) {
 					for (WebReceiver receiver : mReceivers) {
 						try {
 							receiver.onReceiveResult(resultCode, resultData);
 						} catch (NullPointerException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Remove all receivers from all service intents.
 	 */
 	public static void clearReceivers() {
 		for (Entry<String, WebServiceResultReceiver> entry : mResultReceivers.entrySet()) {
 			WebServiceResultReceiver resultReceiver = entry.getValue();
 			resultReceiver.getReceivers().clear();
 		}
 	}
 
 	/**
 	 * Pause all receivers.
 	 */
 	public static void pauseReceivers() {
 		for (Entry<String, WebServiceResultReceiver> entry : mResultReceivers.entrySet()) {
 			WebServiceResultReceiver resultReceiver = entry.getValue();
 			resultReceiver.pause();
 		}
 	}
 
 	/**
 	 * Resume all receivers.
 	 */
 	public static void resumeReceivers() {
 		for (Entry<String, WebServiceResultReceiver> entry : mResultReceivers.entrySet()) {
 			WebServiceResultReceiver resultReceiver = entry.getValue();
 			resultReceiver.resume();
 		}
 	}
 
 	/**
 	 * Remove this receiver from any service intents it's currently attached to.
 	 * 
 	 * @param receiver
 	 */
 	public static void removeReceiver(WebReceiver receiver) {
 		for (Entry<String, WebServiceResultReceiver> entry : mResultReceivers.entrySet()) {
 			WebServiceResultReceiver resultReceiver = entry.getValue();
 			if (resultReceiver.getReceivers().contains(receiver)) {
 				resultReceiver.getReceivers().remove(receiver);
 			}
 		}
 	}
 
 	/**
 	 * Convenience method when Service.Receiver is implemented by Context.
 	 * 
 	 * @param context
 	 * @param request
 	 * @return
 	 */
 	public static String helper(Context context, WebRequest request) {
 		return helper(context, null, (WebReceiver) context, request);
 	}
 
 	public static String helper(Context context, WebReceiver receiver, WebRequest request) {
 		return helper(context, null, receiver, request);
 	}
 
 	/**
 	 * When working with threads, pass in a Handler tied to GUI thread for
 	 * modifying views in Receiver.
 	 * 
 	 * @param context
 	 * @param handler
 	 * @param receiver
 	 * @param request
 	 * @return
 	 */
 	public static String helper(Context context, Handler handler, WebReceiver receiver, WebRequest request) {
 		String uuid = UUID.randomUUID().toString();
 		String cacheKey = request.getKey();
 
 		// Check if cache is still valid to avoid queueing this request behind
 		// valid web requests
 		if (WebContentProvider.Database.getInstance(context.getApplicationContext()).contains(cacheKey, request.mCacheTimeValue, request.mCacheTimeType)) {
 			Bundle bundle = new Bundle();
 			bundle.putString(WebReceiver.REQUEST_KEY, cacheKey);
 			bundle.putBundle(WebReceiver.DEVELOPER_EXTRAS, request.mDeveloperExtras);
 			bundle.putBoolean("fromCache", true);
 			receiver.onReceiveResult(WebReceiver.STATUS_FINISHED, bundle);
 			return cacheKey;
 		}
 
 		/*
 		 * // Check if Service is already pending if (mResultReceivers == null) { //
 		 * this is always getting nullified when service finishes all // requests
 		 * mResultReceivers = new HashMap<String, WebServiceResultReceiver>(); }
 		 */
 
 		for (Entry<String, WebServiceResultReceiver> entry : mResultReceivers.entrySet()) {
 
 			WebServiceResultReceiver resultReceiver = entry.getValue();
 			String cacheKeyExtra = resultReceiver.getRequestKey();
 
 			if (cacheKey.equals(cacheKeyExtra)) {
 				// TODO are there race conditions here?
 				// TODO should probably already be missing from by now, is
 				// check necessary?
 				int statusExtra = resultReceiver.mLastResult.getInt(WebServiceResultReceiver.KEY_RESULT_CODE);
 				if (statusExtra == WebReceiver.STATUS_CREATED || statusExtra == WebReceiver.STATUS_RUNNING || resultReceiver.isPending()) {
 					resultReceiver.addReceiver(receiver);
 					return cacheKey;
 				}
 			}
 		}
 
 		if (handler == null)
 			handler = new Handler();
 		WebServiceResultReceiver resultReceiver = new WebServiceResultReceiver(handler, cacheKey);
 		resultReceiver.addReceiver(receiver);
 
 		Intent service = new Intent(Intent.ACTION_SYNC, null, context.getApplicationContext(), WebService.class);
 		service.putExtra("receiver", resultReceiver);
 		service.putExtra("request", request);
 		service.putExtra("uuid", uuid);
 		service.putExtra("cacheKey", cacheKey);
 
 		mResultReceivers.put(cacheKey, resultReceiver);
 
 		context.getApplicationContext().startService(service);
 
 		return cacheKey;
 	}
 
 	public static byte[] getResponseBytes(Context context, String responseKey) {
 		byte[] bytes = null;
 		Uri uri = WebContentProvider.getDefaultAuthority(context).buildUpon().appendPath(responseKey).build();
 
 		Cursor c = context.getContentResolver().query(uri, null, null, null, null);
 		if (c.moveToFirst()) {
 			bytes = c.getBlob(0);
 
 			if (bytes == null) {
 				InputStream inputStream = null;
 				try {
 					inputStream = context.getContentResolver().openInputStream(uri);
 					ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
 					int bufferSize = 1024;
 					byte[] buffer = new byte[bufferSize];
 					int len = 0;
 					while ((len = inputStream.read(buffer)) != -1) {
 						byteBuffer.write(buffer, 0, len);
 					}
 					bytes = byteBuffer.toByteArray();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		c.close();
 		return bytes;
 	}
 
 	public static String getResponseString(Context context, String responseKey) {
 		try {
 			return new String(getResponseBytes(context, responseKey));
 		} catch (NullPointerException e) {
 			return "";
 		}
 	}
 
 	/* Service Implementation */
 
 	@Override
 	public void onCreate() {
 		log(Log.DEBUG, "Created ThreadPool");
 
 		mPool = Executors.newFixedThreadPool(POOL_SIZE);
 		// mPool = Executors.newSingleThreadExecutor();
 		// mPool = Executors.newFixedThreadPool(POOL_SIZE, new
 		// ServiceThreadFactory());
 
 		mDatabase = WebContentProvider.Database.getInstance(getApplicationContext());
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		log(Log.DEBUG, "Executing Threaded Request");
 		mPool.execute(new WebServiceHandler(intent, startId));
 		return START_STICKY;
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onDestroy() {
 		log(Log.DEBUG, "Destroying Service Object");
 
 		log(Log.DEBUG, "Purging non-pending receivers");
 		for (Entry<String, WebServiceResultReceiver> entry : mResultReceivers.entrySet()) {
 			WebServiceResultReceiver resultReceiver = entry.getValue();
 			if (!resultReceiver.isPending()) {
 				mResultReceivers.remove(entry.getKey());
 			}
 		}
 
 		log(Log.DEBUG, "Shutting down ThreadPool");
 		mPool.shutdown();
 		try {
 			if (!mPool.awaitTermination(5, TimeUnit.SECONDS)) {
 				log(Log.DEBUG, "ThreadPool did not shutdown, forcing shutdown");
 				mPool.shutdownNow();
 			}
 		} catch (InterruptedException e) {
 			log(Log.DEBUG, "Error shutting down ThreadPool, forcing shutdown");
 			mPool.shutdownNow();
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	/*
 	 * private static class WebServiceThreadFactory implements ThreadFactory {
 	 * 
 	 * @Override public Thread newThread(Runnable r) { Thread thread = new
 	 * Thread(r); thread.setPriority(Thread.MIN_PRIORITY); return thread; }
 	 * 
 	 * }
 	 */
 
 }
