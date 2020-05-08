 /* T2AndroidLib for Signal Processing
  * 
  * Copyright  2009-2012 United States Government as represented by 
  * the Chief Information Officer of the National Center for Telehealth 
  * and Technology. All Rights Reserved.
  * 
  * Copyright  2009-2012 Contributors. All Rights Reserved. 
  * 
  * THIS OPEN SOURCE AGREEMENT ("AGREEMENT") DEFINES THE RIGHTS OF USE, 
  * REPRODUCTION, DISTRIBUTION, MODIFICATION AND REDISTRIBUTION OF CERTAIN 
  * COMPUTER SOFTWARE ORIGINALLY RELEASED BY THE UNITED STATES GOVERNMENT 
  * AS REPRESENTED BY THE GOVERNMENT AGENCY LISTED BELOW ("GOVERNMENT AGENCY"). 
  * THE UNITED STATES GOVERNMENT, AS REPRESENTED BY GOVERNMENT AGENCY, IS AN 
  * INTENDED THIRD-PARTY BENEFICIARY OF ALL SUBSEQUENT DISTRIBUTIONS OR 
  * REDISTRIBUTIONS OF THE SUBJECT SOFTWARE. ANYONE WHO USES, REPRODUCES, 
  * DISTRIBUTES, MODIFIES OR REDISTRIBUTES THE SUBJECT SOFTWARE, AS DEFINED 
  * HEREIN, OR ANY PART THEREOF, IS, BY THAT ACTION, ACCEPTING IN FULL THE 
  * RESPONSIBILITIES AND OBLIGATIONS CONTAINED IN THIS AGREEMENT.
  * 
  * Government Agency: The National Center for Telehealth and Technology
  * Government Agency Original Software Designation: T2AndroidLib1021
  * Government Agency Original Software Title: T2AndroidLib for Signal Processing
  * User Registration Requested. Please send email 
  * with your contact information to: robert.kayl2@us.army.mil
  * Government Agency Point of Contact for Original Software: robert.kayl2@us.army.mil
  * 
  */
 package org.t2health.lib1;
 
 
 
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
import java.util.TimeZone;
 import java.util.UUID;
 
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import org.json.JSONObject;
 import org.t2health.lib1.LogWriter;
 
 //import org.ektorp.CouchDbConnector;
 //import org.ektorp.CouchDbInstance;
 //import org.ektorp.ReplicationCommand;
 //import org.ektorp.http.HttpClient;
 //import com.couchbase.touchdb.TDServer;
 //import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
 
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 
 
 /**
  * Handles distribution of processed biometric data
  *   Using DataOutHandler relieves the calling activity of the burdon of knowing
  *   where to sent it's data
  *   
  *   One of the data sinks that will be used in the future in database. Thiss class will 
  *   encapsulate all of the database particuolars from the calling activity
  *   
  *   
  *   Currently data is stored in two formats:
  *   Text (mStr) for output to log files
  *   JSON format (mItem) for output to TouchDB
  *   
  *   Potentially these should be merged into one but right now it's 
  *   seperate because we don't want as much cluttering up log files.
  * 
  * @author scott.coleman
  *
  */
 public class DataOutHandler {
 	private static final String TAG = "BFDemo";	
 
 //	private static final String COUCHBASE_URL = "http://gap.t2health.org/and/phpWebservice/webservice2.php";	 
 	private static final String COUCHBASE_URL = "http://gap.t2health.org/and/json.php";	 
 	
 	private static final int LOG_FORMAT_JSON = 1;	
 	private static final int LOG_FORMAT_FLAT = 2;	
 	
 	public static final String TIME_STAMP = "\"TS\"";
 	public static final String SENSOR_TIME_STAMP = "STS";
 	public static final String RAW_GSR = "GSR";					// Microsiemens
 	public static final String AVERAGE_GSR = "GSRAVG";			// Microsiemens 1 sec average
 	public static final String USER_ID = "UID";
 	public static final String SESSION_ID = "SES";
 	public static final String SENSOR_ID = "SID"; 	
 	public static final String RAW_HEARTRATE = "HR"; 			// BPM
 	public static final String RAW_SKINTEMP = "ST"; 			// Degrees
 	public static final String RAW_EMG = "EMG"; 				// 
 	public static final String AVERAGE_HEARTRATE = "HRAVG"; 	// BPM 3 sec average
 	public static final String RAW_ECG = "ECG";
 	public static final String FILTERED_ECG = "FECG";
 	public static final String RAW_RESP_RATE = "RR";			// BPM
 	public static final String AVERAGE_RESP_RATE = "RRAVG";		// BPM 10 sec average
 	public static final String COMPLETION_PERCENT = "COM";
 	public static final String DURATION = "DUR";					// seconds
 	public static final String NOTATION = "NOT";
 	public static final String CATEGORY = "CAT";
 	public static final String EEG_SPECTRAL = "EEG";
 	public static final String EEG_SIG_STRENGTH = "EEGSIG";
 	public static final String EEG_ATTENTION = "EEGATT";
 	public static final String EEG_MEDITATION = "EEGMED";
 	public static final String HRV_RAW_IBI = "IBI";					// HR inter-beat interval (Ms)
 	public static final String HRV_LF_NU = "LFNU";					// HR low frequency content normalized units (0 - 100)
 	public static final String HRV_HF_NU = "HFNU";					// HR low frequency content normalized units (0 - 100)
 	public static final String HRV_FFT = "HRVFFT";					// FFT of IBI
 	public static final String HRV_RAW_SDNN = "SDNN";				// SDNN
 	public static final String NOTE = "note";
 
 	public static final String DATA_TYPE_RATING = "RatingData";
 	public static final String DATA_TYPE_INTERNAL_SENSOR = "InternalSensor";
 	public static final String DATA_TYPE_EXTERNAL_SENSOR = "ExternalSensor";
 	
 	public boolean mLogCatEnabled = false;	
 	public boolean mLoggingEnabled = false;	
 	private boolean mDatabaseEnabled = false;
 	private boolean mSessionIdEnabled = false;
 	
 	public String mUserId = "";
 	public String mSessionDate = "";
 	public String mAppName = "";
 	public String mDataType = "";
 	private LogWriter mLogWriter;	
 	private Context mContext;
 	private int mLogFormat = LOG_FORMAT_JSON;	
 	private long mSessionId;
 //	private int mLogFormat = LOG_FORMAT_FLAT;	
 	
 //	//couch internals
 //	protected static TDServer server;
 //	protected static HttpClient httpClient;
 //
 //	//ektorp impl
 //	protected CouchDbInstance dbInstance;
 //	protected CouchDbConnector couchDbConnector;
 //	protected ReplicationCommand pushReplicationCommand;
 //	protected ReplicationCommand pullReplicationCommand;
 	
 	String mDatabaseName;	
 	String mRemoteDatabase;
 	/**
 	 * Queue for Rest packets waiting to be sent via HTTP
 	 */
 	List<T2RestPacket> mPendingQueue;
 
 	/**
 	 * Queue for Rest packets which have been send via HTTP
 	 */
 	List<T2RestPacket> mPostingQueue;
 
 	/**
 	 * Thread used to communicate messages in mPendingQueue to server
 	 */
 	private DispatchThread mDispatchThread = null;	
 	
 	
     public static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 	
 	/**
 	 * Application version info determined by the package manager
 	 */
 	private String mApplicationVersion = "";
 	
 //    //static inializer to ensure that touchdb:// URLs are handled properly
 //    {
 //        TDURLStreamHandlerFactory.registerSelfIgnoreError();
 //    }	
 	
 	
 	/**
 	 * Constructor. Initializes context and user/session parameters
 	 * 
 	 * @param context	- Context of calling activity
 	 * @param userId	- User ID detected by calling activity 
 	 * @param sessionDate - Session date created by the calling activity (data/time stamp)
 	 */
 	public DataOutHandler(Context context, String userId, String sessionDate, String appName) {
 		mAppName = appName;
 		mContext = context;
 		mUserId = userId;
 		mSessionDate = sessionDate;
 		mSessionIdEnabled = false;
 	}
 	
 	/**
 	 * Constructor. Initializes context and user/session parameters
 	 * 
 	 * @param context	- Context of calling activity
 	 * @param userId	- User ID detected by calling activity 
 	 * @param sessionDate - Session date created by the calling activity (data/time stamp)
 	 * @param sessionId - long giveing a session ID to be included in all packets
 	 */
 	public DataOutHandler(Context context, String userId, String sessionDate, String appName, String dataType, long sessionId) {
 		mAppName = appName;
 		mDataType = dataType;
 		mContext = context;
 		mUserId = userId;
 		mSessionDate = sessionDate;
 		mSessionIdEnabled = true;
 		mSessionId = sessionId;
 	}
 	
 	public void disableDatabase() {
 		mDatabaseEnabled = false;
 	}
 	
 	public void enableDatabase() {
 		mDatabaseEnabled = true;
 	}
 	
 	
 				
 
 	
 	/**
 	 * Starts up TouchDB database
 	 * 
 	 * @param databaseName		- Local SQLITE database name
 	 * @param designDocName
 	 * @param designDocId
 	 * @param viewName
 	 */
 	public void initializeDatabase(String databaseName, String designDocName, String designDocId, String viewName, String remoteDatabase) {
 
 		mDatabaseEnabled = true;
 		mRemoteDatabase = remoteDatabase;
 		mDatabaseName = databaseName;
 		
 		Log.v(TAG, "Initializing T2 database dispatcher");
 		mPendingQueue = new ArrayList<T2RestPacket>();		
 		mPostingQueue = new ArrayList<T2RestPacket>();
 		
 		mDispatchThread = new DispatchThread();
 		mDispatchThread.start();		
 		
 
 //		Log.v(TAG, "starting TouchBase");
 //
 //		// Start TouchDB
 //		String filesDir = mContext.getFilesDir().getAbsolutePath();
 //	    try {
 //            server = new TDServer(filesDir);
 //        } catch (IOException e) {
 //            Log.e(TAG, "Error starting TDServer", e);
 //        }		
 //		
 //	    //install a view definition needed by the application
 //	    TDDatabase db = server.getDatabaseNamed(mDatabaseName);
 //	    
 //	    
 //	    TDView view = db.getViewNamed(String.format("%s/%s", designDocName, viewName));
 //	    view.setMapReduceBlocks(new TDViewMapBlock() {
 //
 //            @Override
 //            public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
 //                Object createdAt = document.get("created_at");
 //                if(createdAt != null) {
 //                    emitter.emit(createdAt.toString(), document);
 //                }
 //            }
 //        }, null, "1.0");  
 //	    
 //	    // Start ektorp
 //		Log.v(TAG, "starting TouchBase ektorp");
 //
 //		if(httpClient != null) {
 //			httpClient.shutdown();
 //		}
 //
 //		httpClient = new TouchDBHttpClient(server);
 //		dbInstance = new StdCouchDbInstance(httpClient);	    
 //
 //		T2EktorpAsyncTask startupTask = new T2EktorpAsyncTask() {
 //
 //			@Override
 //			protected void doInBackground() {
 //				couchDbConnector = dbInstance.createConnector(mDatabaseName, true);
 //				Log.v(TAG, "TouchBase Created");
 //				
 //			}
 //
 //			@Override
 //			protected void onSuccess() {
 //				// These need to be started manually now
 //				//startReplications();
 //				startPushReplications();				
 //			}
 //		};
 //		startupTask.execute();
 	}		
 
 //	public void startPushReplications() {
 //		pushReplicationCommand = new ReplicationCommand.Builder()
 //		.source(mDatabaseName)
 //		.target(mRemoteDatabase)
 //		.createTarget(true)
 ////		.createTarget(false)
 //		.continuous(true)
 //		.build();
 //
 //		T2EktorpAsyncTask pushReplication = new T2EktorpAsyncTask() {
 //	
 //			@Override
 //			protected void doInBackground() {
 //				dbInstance.replicate(pushReplicationCommand);
 //			}
 //		};
 //	
 //		pushReplication.execute();		
 //	}	
 //	public void startPullReplications() {
 //		pullReplicationCommand = new ReplicationCommand.Builder()
 //		.source(mRemoteDatabase)
 //		.target(mDatabaseName)
 //		.continuous(true)
 //		.build();
 //
 //		T2EktorpAsyncTask pullReplication = new T2EktorpAsyncTask() {
 //	
 //			@Override
 //			protected void doInBackground() {
 //				dbInstance.replicate(pullReplicationCommand);
 //			}
 //		};
 //	
 //		pullReplication.execute();		
 //	}	
 //
 //	public void stopPushReplications() {
 //		pushReplicationCommand = new ReplicationCommand.Builder()
 //		.source(mDatabaseName)
 //		.target(mRemoteDatabase)
 //		.cancel(true)
 //		.build();
 //
 //		T2EktorpAsyncTask pushReplication = new T2EktorpAsyncTask() {
 //	
 //			@Override
 //			protected void doInBackground() {
 //				dbInstance.replicate(pushReplicationCommand);
 //			}
 //		};
 //	
 //		pushReplication.execute();			
 //	}	
 //	
 //	
 //	public void stopPullReplications() {
 //		pullReplicationCommand = new ReplicationCommand.Builder()
 //		.source(mRemoteDatabase)
 //		.target(mDatabaseName)
 //		.cancel(true)
 //		.build();
 //
 //		T2EktorpAsyncTask pullReplication = new T2EktorpAsyncTask() {
 //	
 //			@Override
 //			protected void doInBackground() {
 //				dbInstance.replicate(pullReplicationCommand);
 //			}
 //		};
 //	
 //		pullReplication.execute();			
 //	}	
 //	
 //	public void startReplications() {
 //		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 //	
 //		pushReplicationCommand = new ReplicationCommand.Builder()
 //			.source(mDatabaseName)
 //			.target(mRemoteDatabase)
 ////			.createTarget(true)
 //			
 //			.continuous(true)
 //			.build();
 //	
 //		T2EktorpAsyncTask pushReplication = new T2EktorpAsyncTask() {
 //	
 //			@Override
 //			protected void doInBackground() {
 //				dbInstance.replicate(pushReplicationCommand);
 //			}
 //		};
 //	
 //		pushReplication.execute();
 //	
 //		pullReplicationCommand = new ReplicationCommand.Builder()
 //			.source(mRemoteDatabase)
 //			.target(mDatabaseName)
 ////			.createTarget(true)
 //			
 //			.continuous(true)
 //			.build();
 //	
 //		T2EktorpAsyncTask pullReplication = new T2EktorpAsyncTask() {
 //	
 //			@Override
 //			protected void doInBackground() {
 //				dbInstance.replicate(pullReplicationCommand);
 //			}
 //		};
 //	
 //		pullReplication.execute();
 //	}
 //	
 	
 	
 	public void enableLogging(Context context) {
 		try {
 			mLogWriter = new LogWriter(context);	
 			String logFileName = mUserId + "_" + mSessionDate + ".log";			
 			mLogWriter.open(logFileName, true);	
 			mLoggingEnabled = true;
 			
 			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSSZ", Locale.US);
 			String timeId = sdf.format(new Date());			
 			
 			PackageManager packageManager = context.getPackageManager();
 			PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);			
 			mApplicationVersion = info.versionName;
 			
 			
 			
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				String preamble = String.format(
 						"{\"userId\" : \"%s\",\n" +
 						"\"sessionDate\" : \"%s\",\n" + 
 						"\"timeId\" : \"%s\",\n" + 
 						"\"versionId\" : \"%s\",\n" + 
 						"\"data\":[",  
 						mUserId, mSessionDate, timeId, mApplicationVersion);
 				mLogWriter.write(preamble);
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "Exception enabling logging: " + e.toString());
 		}
 	}
 
 	public void enableLogCat() {
 		mLogCatEnabled = true;
 	}	
 	
 	
 	public void purgeLogFile() {
 		if (mLoggingEnabled) {
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mLogWriter.write("],}");
 			}
 			mLogWriter.close();
 			
 			enableLogging(mContext);			
 		}		
 	}
 	
 	/**
 	 * Closes out any open log files and data connections
 	 */
 	public void close() {
 
 		Log.e(TAG, " ***********************************closing ******************************");
 		mDatabaseEnabled = false;
 		if (mLoggingEnabled) {
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mLogWriter.write("],}");
 			}
 			mLogWriter.close();			
 		}
 		
 		//clean up our TohchDB http client connection manager
 //		if(httpClient != null) {
 //			httpClient.shutdown();
 //		}
 //
 //		//clean up our TohchDB Sever
 //		if(server != null) {
 //		    server.close();
 //		}
 		
 		if(mDispatchThread != null) {
 			mDispatchThread.cancel();
 			mDispatchThread.interrupt();
 			mDispatchThread = null;
 		}
 		
 		
 		
 	}
 
 	/**
 	 * Data packet used to accumulate data to be sent using DataOutHandler
 	 * 
 	 * This class encapculates one JSON object which holds any number of related data
 	 * 
 	 * @author scott.coleman
 	 *
 	 */
 	public class DataOutPacket {
 		
 		public String mStr = "";
 		public ObjectNode mItem;		
 		
 		public DataOutPacket() {
 	    	UUID uuid = UUID.randomUUID();
 	    	Calendar calendar = GregorianCalendar.getInstance();
 	    	long currentTime = calendar.getTimeInMillis();
	    	dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
 	        String currentTimeString = dateFormatter.format(calendar.getTime());
 	    	String id = currentTime + "-" + uuid.toString();
 
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mStr = "{" + TIME_STAMP + ":" + currentTime + ",";			
 			}
 			else {
 				mStr = TIME_STAMP + ",";			
 			}
 			
 	    	mItem = JsonNodeFactory.instance.objectNode();		
 	    	mItem.put("_id", id);
 	    	mItem.put("created_at", currentTimeString);
 	    	mItem.put("user_id", mUserId);
 	    	mItem.put("session_date", mSessionDate);
 	    	if (mSessionIdEnabled) {
 		    	mItem.put("session_id", mSessionId);
 	    	}
 	    	mItem.put("app_name", mAppName);
 	    	mItem.put("data_type", mDataType);
 	    	mItem.put("platform", "Android");
 		}
 
 		/**
 		 * Adds a tag/data pair to the packet
 		 * @param tag
 		 * @param value
 		 */
 		public void add(String tag, double value) {
 			
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mStr += String.format("%s:%f,", tag,value);
 			}
 			else {
 				mStr += "" + value + ",";			
 			}
 			
 			mItem.put(tag,value);		
 		}
 		
 		/**
 		 * Adds a tag/data pair to the packet
 		 * @param tag
 		 * @param value
 		 */
 		public void add(String tag, double value, String format) {
 			
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mStr += String.format("%s:" + format + ",", tag,value);
 			}
 			else {
 				mStr += "" + value + ",";			
 			}
 			mItem.put(tag,value);		
 		}
 		
 		/**
 		 * Adds a tag/data pair to the packet
 		 * @param tag
 		 * @param value
 		 */
 		public void add(String tag, long value) {
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mStr += tag + ":" + value + ",";			
 			}
 			else {
 				mStr += "" + value + ",";			
 			}
 			mItem.put(tag,value);		
 		}
 
 		/**
 		 * Adds a tag/data pair to the packet
 		 * @param tag
 		 * @param value
 		 */
 		public void add(String tag, int value) {
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mStr += tag + ":" + value + ",";			
 			}
 			else {
 				mStr += "" + value + ",";			
 			}
 			mItem.put(tag,value);		
 		}
 
 		/**
 		 * Adds a tag/data pair to the packet
 		 * @param tag
 		 * @param value
 		 */
 		public void add(String tag, String value) {
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mStr += tag + ":\"" + value + "\",";			
 			}
 			else {
 				mStr += "" + value + ",";			
 			}
 			mItem.put(tag,value);		
 		}
 	}
 
 	public void handleDataOut(final JSONObject jsonObject) {
 
 		DataOutPacket packet = new DataOutPacket();
 		// To match our format we must remove the starting and ending curly brace
 		String tmp = jsonObject.toString();
 		tmp = tmp.substring(1,tmp.length() - 1);
 		packet.mStr += tmp;
 		
 		//packet.mItem.put("data", jsonObject.toString());
 //		packet.mItem.putObject(jsonObject);
 		
 		handleDataOut(packet);
 	}	
 	
 	public void handleDataOut(final ObjectNode jsonObject) {
 		DataOutPacket packet = new DataOutPacket();
 		// To match our format we must remove the starting and ending curly brace
 		String tmp = jsonObject.toString();
 		tmp = tmp.substring(1,tmp.length() - 1);
 		packet.mStr += tmp;
 		
 		packet.mItem.put("data", jsonObject);
 		
 		handleDataOut(packet);
 	}
 	
 	
 	public void handleDataOut(final ArrayNode jsonArray) {
 
 		DataOutPacket packet = new DataOutPacket();
 		// To match our format we must remove the starting and ending curly brace
 		String tmp = jsonArray.toString();
 		tmp = tmp.substring(1,tmp.length() - 1);
 		packet.mStr += tmp;
 		
 		packet.mItem.put("data", jsonArray);
 		handleDataOut(packet);
 	}	
 	
 	
 	/**
 	 * Sends a data packet to all configured output sinks
 	 * 
 	 * 
 	 * @param packet - data Packet to send to output sinks
 	 */
 	public void handleDataOut(final DataOutPacket packet) {
 		if (mLogFormat == LOG_FORMAT_JSON) {
 			packet.mStr += "},";
 		}
 
 		if (mLoggingEnabled) {	
 //			Log.d(TAG, "Writing to log file");		// TODO: remove
 			mLogWriter.write(packet.mStr);
 		}
 
 		if (mLogCatEnabled) {
 			Log.d(TAG, packet.mStr);			
 		}
 		
 		if (mDatabaseEnabled) {
 			String dataPacketString = packet.mItem.toString();
 			T2RestPacket pkt = new T2RestPacket(dataPacketString);
 			Log.d(TAG, "Queueing document " + pkt.mId);
 	    
 			
 			
 			
 //			String jsonString = "[" + dataPacketString + "]";
 //			RequestParams params = new RequestParams("json", jsonString);
 //	        T2RestClient.post(COUCHBASE_URL, params, new AsyncHttpResponseHandler() {
 //	            @Override
 //	            public void onSuccess(String response) {
 //					Log.d(TAG, "Posing Successful: " + response);
 //	                
 //	            }
 //	        });
 
 			
 			
 			
 			synchronized(mPendingQueue) {
 				mPendingQueue.add(0,  pkt);
 			}
 		}
 		
 		
 		
 //		// Now do something with the database if necessary
 //		if (mDatabaseEnabled && couchDbConnector != null) {
 //			Log.d(TAG, "Adding document");
 //
 //			T2EktorpAsyncTask createItemTask = new T2EktorpAsyncTask() {
 //
 //				@Override
 //				protected void doInBackground() {
 //					couchDbConnector.create(packet.mItem);
 //				}
 //
 //				@Override
 //				protected void onSuccess() {
 //					Log.d(TAG, "Document added to database successfully");
 //				}
 //
 //				@Override
 //				protected void onUpdateConflict(
 //						UpdateConflictException updateConflictException) {
 //					Log.d(TAG, "Got an update conflict for: " + packet.mItem.toString());
 //				}
 //			};
 //		    createItemTask.execute();			
 //		} // End if (mDatabaseEnabled && couchDbConnector != null)
 	}
 
 	/**
 	 * Logs a text note to sinks
 	 * 
 	 * @param note - Text not to log to sinks
 	 */
 	public void logNote(String note) {
 		DataOutPacket packet = new DataOutPacket();
 		packet.add(NOTE, note);
 		handleDataOut(packet);				
 	}
 	
 	
 	class DispatchThread extends Thread {
 		private boolean isRunning = false;
 		private boolean cancelled = false;
 
 		@Override
 		public void run() {
 			isRunning = true;
 			
 			
 			while(true) {
 				// Break out if this was cancelled.
 				if(cancelled) {
 					break;
 				}
 				
 				try {
 					Thread.sleep(4000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				Log.d(TAG, "Http dispatch thread tick");
 
 				// If the network is available post entries from the PendingQueue
 				if (isNetworkAvailable()) {
 					synchronized(mPendingQueue) {
 	
 						if (mPendingQueue.size() > 0) {
 							Log.d(TAG, "pending queue size =  " + mPendingQueue.size() );
 
 							// Fill the posting Queue with all of the items that need to be posted
 							// We need this array so when we get a response we can remove all of these entries from the PendingQueue
 							String jsonString  = "[";
 							int iteration = 0;
 							Iterator<T2RestPacket> iterator = mPendingQueue.iterator();						
 							while(iterator.hasNext()) {
 								
 								T2RestPacket packet = iterator.next();
 								if (packet.mStatus == T2RestPacket.STATUS_PENDING) {
 									
 									 packet.mStatus = T2RestPacket.STATUS_POSTED;
 									 
 									 if (iteration++ > 0)
 										 jsonString += "," + packet.mJson;
 									 else
 										 jsonString += packet.mJson;
 										 
 									 Log.d(TAG, "Posting document " + packet.mId);
 									 mPostingQueue.add(packet);									 
 									
 								}
 							}
 							
 							jsonString += "]";
 							
 							// If there is somethig in the postingQueue then send it http
 							if (mPostingQueue.size() > 0) {
 								Log.d(TAG, "Sending " + mPostingQueue.size() + " entries");
 //								Log.d(TAG, "Actual postingstring: " + jsonString);
 								RequestParams params = new RequestParams("json", jsonString);
 						        T2RestClient.post(COUCHBASE_URL, params, new AsyncHttpResponseHandler() {
 						            @Override
 						            public void onSuccess(String response) {
 										Log.d(TAG, "Posing Successful: " + response);
 						                
 						            }
 						        });	
 						        mPostingQueue.clear();
 							}
 					        
 							// For now we'll just clear out the pending queue too.
 							// Eventually we should check responses against the pending queue and delete entries only when we get a successful response 
 							mPendingQueue.clear();
 							
 //					        // Now remove entries in posting queue from pending queue
 //							Iterator<T2RestPacket> iterator1 = mPostingQueue.iterator();						
 //							while(iterator1.hasNext()) {
 //								T2RestPacket packet = iterator.next();
 //								mPendingQueue.remove(packet);
 //								Log.d(TAG, "Removing document " + packet.mId);
 //							}					        
 //					        
 					        
 					        
 					        
 														
 						} // End if (mPendingQueue.size() > 0)
 						
 					} // End synchronized(mPendingQueue) 
 				}
 				
 				
 				
 				
 				
 				
 
 				
 				
 				
 			} // End while(true)
 			
 
 			isRunning = false;
 		} // End public void run() 
 		
 		
 		
 		public void cancel() {
 			this.cancelled = true;
 			Log.e(TAG, "Cancelled");
 			
 		}
 		
 		public boolean isRunning() {
 			return this.isRunning;
 		}
 		
 		
 	}
 	
     public boolean isNetworkAvailable() {
         ConnectivityManager cm = (ConnectivityManager) 
           mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = cm.getActiveNetworkInfo();
         // if no network is available networkInfo will be null
         // otherwise check if we are connected
         if (networkInfo != null && networkInfo.isConnected()) {
             return true;
         }
         return false;
     } 	
 	
 	
 }
