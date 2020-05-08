 /**
  * 
  */
 package org.opensharingtoolkit.logging;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.json.JSONException;
 import org.json.JSONStringer;
 
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 /**
  * @author pszcmg
  *
  */
 public class LoggingService extends IntentService {
 	
 	private static final int LEVEL_DEBUG = 2;
 	private static final long DAY_DURATION = 24*60*60*1000L;
 	private static final long FILE_POLL_DELAY = 1000;
 	// hope it is a real singleton?!
 	private static File mLogDir;
 	private static File mLogFile;
 	private static long mLogFileDayDate;
 	private static long mLogFileLength;
 	private static long mLogFileTotal;
 	private static long LOG_FILE_MAX_LENGTH = 1000000; // about 1MB
 	private static BufferedOutputStream mOutput;
 	private static String mPendingError;
 	public static String LOG_VERSION = "1.0";
 	
 	public LoggingService() {
 		super("OSTLogging");
 	}
 
 	public static String TAG = "logging";
 	
 	/** (currently) no remote API - just use Intents
 	 */
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// no bind interface, at least for now
 		return null;
 	}
 
 	/** Service create. Almost a no-op.
 	 * File creation deferred to intent handling thread in case storage is 
 	 * initially unavailable (this shouldn't block like that).
 	 */
 	@Override
 	public void onCreate() {
 		Log.i(TAG,"Start LoggingService");
 		super.onCreate();
 	}
 
 	/** service destroy - flush and tidy.
 	 */
 	@Override
 	public void onDestroy() {
 		Log.i(TAG,"Destroy LoggingService");
 		if (mOutput!=null) {
 			try {
 				mOutput.flush();
 			}
 			catch (Exception e) {
 				Log.e(TAG,"Error flushing log file on close: "+e);
 				try {
 					mOutput.close();
 				}
 				catch (Exception e2) {/*ignore*/}
 				mOutput = null;
 				mLogFile = null;
 				mPendingError = e.toString();
 			}
 		}
 		super.onDestroy();
 	}
 
 	/** IntentService intent handler, run in worker thread, serialised.
 	 * May block awaiting storage.
 	 */
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		// No intent action, no intent filter - only intended to be called explicitly.
 		long now = System.currentTimeMillis();
 		long time = intent.getLongExtra("time", now);
 		String component = intent.getStringExtra("component");
 		String event = intent.getStringExtra("event");
 		String info = intent.getStringExtra("info");
 		int level = intent.getIntExtra("level", LEVEL_DEBUG);
 		
 		// Note: info is raw JSON
 		try {
 			JSONStringer js = new JSONStringer();
 			js.object();
 			js.key("time");
 			js.value(time);
 			js.key("component");
 			js.value(component);
 			js.key("event");
 			js.value(event);
 			js.key("level");
 			js.value(level);
 			js.endObject();
 			String json = js.toString();
 			if (info!=null) {
 				StringBuilder sb = new StringBuilder();
 				sb.append(json.substring(0, json.length()-1));
 				sb.append(",\"info\":");
 				sb.append(info);
 				sb.append("}");
 				json = sb.toString();
 			}
 			tryWriteEntry(json);
 		} catch (JSONException e) {
 			Log.e(TAG,"Marshalling: "+e+" for "+time+" "+level+" "+component+" "+event+" "+info);
 		}
 		//Log.d(TAG,"Log: "+time+" "+level+" "+component+" "+event+" "+info);
 	}
 
 	/** ROOT-locale equivalent */
 	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'", new Locale("","",""));
 	private Calendar day = Calendar.getInstance(TimeZone.getTimeZone("UTC"), new Locale("","",""));
 
 	private void tryWriteEntry(String line) {
 		boolean waitingForStorage = true;
 		done: while (true) {
 			// ready to go?
 			Date now = new Date();
 			if (mOutput!=null) {
 				// try writing
 				try {
 					if (mLogFileLength >= LOG_FILE_MAX_LENGTH || now.getTime()>mLogFileDayDate+DAY_DURATION) {
 						// rotate
 						Log.i(TAG,"Rotating log file at "+mLogFileLength+" bytes and "+(now.getTime()-mLogFileDayDate)+" seconds");
 						mOutput.write((byte)'\n');
 						String msg = "{\"time\":"+now.getTime()+",\"component\":\"logger\",\"event\":\"log.rotate\",\"level\":4}";
 						mOutput.write(msg.getBytes("UTF-8"));
 						mOutput.write((byte)'\n');
 						mOutput.flush();
 						mOutput.close();
 						mOutput = null;
 						mLogFile = null;
 					}
 					byte bs[] = line.getBytes("UTF-8");
 					mOutput.write(bs);
 					mOutput.write((byte)'\n');
 					// TODO delay flush?
 					mOutput.flush();
 					// OK
 					int count = bs.length+1;
 					mLogFileLength += count;
 					mLogFileTotal += count;
 					break done;
				} catch (IOException e) {
 					Log.w(TAG,"Error writing entry: "+e);
 					// ok, tear it down and try again...
 					mPendingError = e.toString();
 					try {
 						mOutput.close();
 					} catch (Exception e2) { /* ignore */ }
 					mOutput = null;
 					mLogFile = null;
 				}
 			}
 			// need to create a new log file
 			// Log dir OK?
 			if (mLogDir!=null && mLogDir.exists()) {
 				// create new Log file
 				String filename = sdf.format(now)+".log";
 				mLogFile = new File(mLogDir, filename);
 				Log.i(TAG,"Create new log file "+mLogFile);
 				try {
 					mOutput = new BufferedOutputStream(new FileOutputStream(mLogFile));
 					// write header
 					JSONStringer js = new JSONStringer();
 					js.object();
 					js.key("time");
 					js.value(now.getTime());
 					js.key("level");
 					js.value(4);
 					js.key("component");
 					js.value("logger");
 					js.key("event");
 					js.value("log.start");
 					js.key("info");
 					js.object();
 					if (mPendingError!=null) {
 						js.key("pendingError");
 						js.value(mPendingError);
 					}
 					marshallDeviceInfo(js);
 					js.endObject();
 					js.endObject();
 					String json = js.toString();
 					byte bs[] = json.getBytes("UTF-8");
 					mOutput.write(bs);
 					mOutput.write((byte)'\n');
 					mOutput.flush();
 					// day date...
 					day.setTime(now);
 					day.set(Calendar.HOUR_OF_DAY, 0);
 					day.set(Calendar.MINUTE, 0);
 					day.set(Calendar.SECOND, 0);
 					day.set(Calendar.MILLISECOND, 0);
 					mLogFileDayDate = day.getTimeInMillis();
 					mLogFileLength = bs.length;
 					mLogFileTotal += bs.length;
 					mPendingError = null;
 					// carry on...
 				}
 				catch (Exception e) {
 					Log.e(TAG,"Could not create log file "+mLogFile+": "+e);
 					mLogFile = null;
 					//?mLogDir = null;
 				}
 			} else {
 				// doesn't exist? - may be unplugged
 				mLogDir = getExternalFilesDir(null);
 				if (mLogDir==null) {
 					if (!waitingForStorage) {
 						Log.w(TAG, "getLocalFilePrefix with external storage not available");
 						waitingForStorage = true;
 					}
 					try {
 						Thread.sleep(FILE_POLL_DELAY);
 					}
 					catch (InterruptedException e) {
 						Log.w(TAG,"Interrupted waiting for external files");
 					}
 				}
 				else if (waitingForStorage) {
 					waitingForStorage = false;
 					Log.i(TAG, "getLocalFilePrefix succeeded: "+mLogDir);					
 				}
 			}
 		}
 	}
 
 	/** get device identifiers, etc. for log file header 
 	 * 
 	 * @param js Stringer to write into
 	 * @throws JSONException 
 	 */
 	private void marshallDeviceInfo(JSONStringer js) {
 		try {
 			js.key("logVersion");
 			js.value(LOG_VERSION);
 			String appVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
 			js.key("appVersionName");
 			js.value(appVersionName);
 			int appVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
 			js.key("appVersionCode");
 			js.value(appVersionCode);
 		} catch (Exception e) {
 			Log.e(TAG,"Error getting appVersion", e);
 		}
 		try {
 			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
 			final String deviceId = tm.getDeviceId();
 			if (deviceId!=null) {
 				js.key("deviceId");
 				js.value(deviceId);
 			}
 			final String subscriberId = tm.getSubscriberId();
 			if (subscriberId!=null) {
 				js.key("subscriberId");
 				js.value(subscriberId);
 			}
 		} catch (Exception e) {
 			Log.e(TAG,"Error getting deviceId/subscriberId", e);
 		}
 		try {
 			final String androidId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
 			if (androidId!=null) {
 				js.key("androidId");
 				js.value(androidId);
 			}
 		} catch (Exception e) {
 			Log.e(TAG,"Error getting androidId", e);
 		}
 		try {
 			js.key("BOARD");
 			js.value(android.os.Build.BOARD);
 			js.key("DEVICE");
 			js.value(android.os.Build.DEVICE);
 			js.key("DISPLAY");
 			js.value(android.os.Build.DISPLAY);
 			/* API 9 ?! 
 			if (android.os.Build.SERIAL!=null) {
 				js.key("SERIAL");
 				js.value(android.os.Build.SERIAL);
 			}
 			*/
 			js.key("BRAND");
 			js.value(android.os.Build.BRAND);
 			js.key("MODEL");
 			js.value(android.os.Build.MODEL);
 			js.key("PRODUCT");
 			js.value(android.os.Build.PRODUCT);
 			js.key("HARDWARE");
 			js.value(android.os.Build.HARDWARE);
 			js.key("RELEASE");
 			js.value(android.os.Build.VERSION.RELEASE);
 			js.key("SDK");
 			js.value(android.os.Build.VERSION.SDK_INT);
 		}
 		catch (Exception e) {
 			Log.e(TAG,"Error getting androidId", e);
 		}
 	}
 }
