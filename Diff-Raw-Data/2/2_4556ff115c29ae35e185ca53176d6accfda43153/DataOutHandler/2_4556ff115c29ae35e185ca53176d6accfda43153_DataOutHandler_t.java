 package com.t2.dataouthandler;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.http.cookie.Cookie;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.t2health.lib1.LogWriter;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
 import com.amazonaws.services.dynamodb.model.AttributeValue;
 import com.amazonaws.services.dynamodb.model.PutItemRequest;
 import com.amazonaws.tvmclient.AmazonClientManager;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageDelegate;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.utils.T2CookieStore;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.loopj.android.http.PersistentCookieStore;
 import com.loopj.android.http.RequestParams;
 import com.t2.aws.DynamoDBManager;
 import com.t2.dataouthandler.DataOutHandler;
 import com.t2.dataouthandler.DataOutHandlerException;
 import com.t2.dataouthandler.DataOutHandlerTags;
 import com.t2.dataouthandler.T2AuthDelegate;
 import com.t2.dataouthandler.T2RestClient;
 import com.t2.dataouthandler.T2RestPacket;
 import com.t2.drupalsdk.ServicesClient;
 import com.t2.drupalsdk.UserServices;
 
 public class DataOutHandler  implements JREngageDelegate {
 	// Comment
 	private final String TAG = getClass().getName();	
 	//private static final String DEFAULT_REST_DB_URL = "http://gap.t2health.org/and/phpWebservice/webservice2.php";	 
 	// private static final String DEFAULT_REST_DB_URL = "http://gap.t2health.org/and/json.php";	 
 	private static final String DEFAULT_REST_DB_URL = "http://ec2-50-112-197-66.us-west-2.compute.amazonaws.com/mongo/json.php";
 	private static final String DEFAULT_AWS_DB_URL = "h2tvm.elasticbeanstalk.com";
 	private static final String DEFAULT_DRUPAL_DB_URL = "http://t2health.us/h2/android/";
 	
 	private static final boolean AWS_USE_SSL = false;
 	
     private static String ENGAGE_APP_ID = "khekfggiembncbadmddh";
 //    private static String ENGAGE_TOKEN_URL = "https://t2health.us/h2/rpx/token_handler?destination=node";	
     private static String ENGAGE_TOKEN_URL = "http://t2health.us/h2/rpx/token_handler?destination=node";	
 
 	private static final int LOG_FORMAT_JSON = 1;	
 	private static final int LOG_FORMAT_FLAT = 2;	
 	
 
 	public static final String SHORT_TIME_STAMP = "\"TS\"";
 
 	public static final String DATA_TYPE_RATING = "RatingData";
 	public static final String DATA_TYPE_INTERNAL_SENSOR = "InternalSensor";
 	public static final String DATA_TYPE_EXTERNAL_SENSOR = "ExternalSensor";
 	public static final String DATA_TYPE_USER_ENTERED_DATA = "UserEnteredData";
 	
 	public boolean mLogCatEnabled = false;	
 	public boolean mLoggingEnabled = false;	
 	private boolean mDatabaseEnabled = false;
 	private boolean mSessionIdEnabled = false;
 	
 	/**
 	 * User identification to be associated with data stored
 	 */
 	public String mUserId = "";
 	
 	/**
 	 * Date a particular session started, there can be multiple data 
 	 * saves for any session
 	 */
 	public String mSessionDate = "";
 	
 	/**
 	 * Name of calling application (logged with data)
 	 */
 	public String mAppName = "";
 	
 	/**
 	 * Source type of data (internal, external, etc.)
 	 */
 	public String mDataType = "";
 	
 	/**
 	 * Used to write data logs
 	 */
 	private LogWriter mLogWriter;	
 
 	/**
 	 * Context of calling party
 	 */
 	private Context mContext;
 
 	/**
 	 * Desired format of data lof files
 	 */
 	private int mLogFormat = LOG_FORMAT_JSON;	// Alternatively LOG_FORMAT_FLAT 	
 
 	/**
 	 * ID of a particular session (for multiple sessions in an application run
 	 */
 	private long mSessionId;
 	
 	/**
 	 * URL of the remote database we are saving to
 	 */
 	String mRemoteDatabase;	
 	
 	/**
 	 * Thread used to communicate messages in mPendingQueue to server
 	 */
 	private DispatchThread mDispatchThread = null;	
 	
 	
     public static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 	
 	/**
 	 * Application version info determined by the package manager
 	 */
 	private String mApplicationVersion = "";
 
 	// JanRain Stuff
 	String mEngageAppId = ENGAGE_APP_ID;
 	String mEngageTokenUrl = ENGAGE_TOKEN_URL;
 	
 	// T2 Drupal stuff
 	/**
 	 * Used to save Drupal session cookies for authentication.
 	 */
 	private PersistentCookieStore mCookieStore;
 
 	/**
 	 * HTTP services client used to talk to Drupal.
 	 */
 	private ServicesClient mServicesClient;	
 
 	/**
 	 * Engage instance for openID authentication
 	 */
 	private JREngage mEngage;
 	
     /**
      * JanRain Callbacks for notification of auth success/fail, etc.
      */
     private T2AuthDelegate mT2AuthDelegate;    
     
     /**
      * Contains information about authenticated user
      */
     private JRDictionary mAuth_info;
 
     /**
      * The provider the user used to authenticate with (provided by JanRain)
      */
     private String mAuthProvider;	
 	
 	
 	
     /**
 	 * True if JanRain has successfully authenticated a user. 
 	 */
 	private boolean mAuthenticated = false;
 	
 	/**
 	 * Set this to true to require authtication for all database puts. 
 	 */
 	private boolean mRequiresAuthentication = true;	
 	
 	/**
 	 * Queue for Rest packets waiting to be sent via HTTP
 	 */
 	List<DataOutPacket> mPendingQueue;	
 	
 	/**
 	 * Database manager when sending data to external Amazon database
 	 */
 	public static AmazonClientManager sClientManager = null;		
 
 	// Database types. 
 	//		Note that different database types
 	// 		may need different processing and even 
 	//		different structures, thus is it important to
 	//		use DataOutPacket structure to add data
 	//	
 	public final static int DATABASE_TYPE_AWS = 0;			//	AWS (Goes to AWS DynamoDB)
 	public final static int DATABASE_TYPE_T2_REST = 1; 		// T2 Rest server (goes to Mongo DB)
 	public final static int DATABASE_TYPE_T2_DRUPAL = 2; 	//	T2 Drupal - goes to a Drupal database
 	public final static int DATABASE_TYPE_NONE = -1;
 	
 	/**
 	 * sets which type of external database is setup and used
 	 */
 	private int mDatabaseType;	
 	
 	/**
 	 * Sets the AWS table name into which data is stored (AWS only)
 	 */
 	private String mAwsTableName = "TestT2"; // Default to TestT2
 
 	/**
 	 * Shared preferences for this lib (will be the same as calling party)
 	 */
 	SharedPreferences mSharedPreferences;	
 	
 	/**
 	 * Static instance of this class
 	 */
 	private static DataOutHandler sDataOutHandler;	
 	
 	/**
 	 * Sets the AWS table name (applicable only if AWS database is chosen)
 	 * @param awsTableName Name of the table
 	 */
 	public void setAwsTableName(String awsTableName) {
 		this.mAwsTableName = awsTableName;
 	}	
 	
 	public synchronized static DataOutHandler getInstance(Context context, String userId, String sessionDate, String appName, String dataType, long sessionId) {
 		if (sDataOutHandler == null) {
 			sDataOutHandler = new DataOutHandler(context, userId, sessionDate, appName, dataType, sessionId);
 		}
 		
 		return sDataOutHandler;
 	}
 	
 	public static DataOutHandler getInstance() throws DataOutHandlerException {
 		if (sDataOutHandler == null) {
 			throw new DataOutHandlerException("DataOutHandler has not been initialized");
 		}
 		
 		return sDataOutHandler;
 	}		
 	
 	
 	
 	
 	/**
 	 * Constructor. Sets up context and user/session parameters
 	 * 
 	 * @param context	- Context of calling activity
 	 * @param userId	- User ID detected by calling activity 
 	 * @param sessionDate - Session date created by the calling activity (data/time stamp)
 	 * @param appName - Name of calling application
 	 */
 	public DataOutHandler(Context context, String userId, String sessionDate, String appName) {
 		mAppName = appName;
 		mContext = context;
 		mUserId = userId;
 		mSessionDate = sessionDate;
 		mSessionIdEnabled = false;
 	}
 	
 	/**
 	 * Constructor. sets up context and user/session parameters
 	 * 
 	 * @param context	- Context of calling activity
 	 * @param userId	- User ID detected by calling activity 
 	 * @param sessionDate - Session date created by the calling activity (data/time stamp)
 	 * @Param appName		- Name of calling application (for logging)
 	 * @Param dataType		- Data type (Internal or external)
 	 * @param sessionId - long session ID to be included in all packets
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
 	
 	/**
 	 * Disables database functionality
 	 */
 	public void disableDatabase() {
 		mDatabaseEnabled = false;
 	}
 	
 	/**
 	 * Enables database functionality
 	 */
 	public void enableDatabase() {
 		mDatabaseEnabled = true;
 	}
 	
 	/**
 	 * Sets formatting of log output
 	 * 	LOG_FORMAT_JSON = 1; JSON format	
 	 *  LOG_FORMAT_FLAT = 2; Standard column format
 	 * 
 	 * @param format - format to use
 	 */
 	public void setLogFormat(int format) {
 		mLogFormat = format;		
 	}
 	
 			
 	/**
 	 * @author scott.coleman
 	 * Task to check the status of an AWS database table
 	 */
 	class CheckTableStatusTask extends AsyncTask<String, Void, String> {
 
 	    private Exception exception;
 
 	    protected String doInBackground(String... urls) {
 	        try {
 				String tableStatus = DynamoDBManager.getTestTableStatus();
 				String status = tableStatus;
 	        	
 	            return status;
 	        } catch (Exception e) {
 	            this.exception = e;
 	            return "";
 	        }
 	    }
 
 	    protected void onPostExecute(String status) {
 	    	Log.d(TAG, "Database status = " + status);
 	    }
 	 }	
 	
 	/**
 	 * Sets  the RequiresAuthentication flag 
 	 * @param mRequiresAuthentication true/false
 	 */
 	public void setRequiresAuthentication(boolean mRequiresAuthentication) {
 		this.mRequiresAuthentication = mRequiresAuthentication;
 	}	
 	/**
 	 * Initialized specified database
 	 * 
 	 * @param remoteDatabase URL of database to send data to. 
 	 * @param databaseType Type of database (AWS, TRest, T2Drupal, etc.).
 	 * @param t2AuthDelegate Callbacks to send status to.
 	 * @throws DataOutHandlerException
 	 */
 	public void initializeDatabase(String remoteDatabase, String databaseType) throws DataOutHandlerException {
 		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
 		// Do it this way for backward compatibility
 		mSharedPreferences.edit().putString("external_database_type", databaseType);
 		initializeDatabase("", "", "", "", remoteDatabase);		
 	}
 
 	/**
 	 * Initialized specified database
 	 * 
 	 * @param remoteDatabase URL of database to send data to. 
 	 * @param databaseType Type of database (AWS, TRest, T2Drupal, etc.).
 	 * @param t2AuthDelegate Callbacks to send status to.
 	 * @throws DataOutHandlerException
 	 */
 	public void initializeDatabase(String remoteDatabase, String databaseType, T2AuthDelegate t2AuthDelegate) throws DataOutHandlerException {
 		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
 		// Do it this way for backward compatibility
 		mSharedPreferences.edit().putString("external_database_type", databaseType);
 		mT2AuthDelegate = t2AuthDelegate;
 		initializeDatabase("", "", "", "", remoteDatabase);		
 	}
 
 	/**
 	 * @param remoteDatabase URL of database to send data to. 
 	 * @param databaseType Type of database (AWS, TRest, T2Drupal, etc.)
 	 * @param t2AuthDelegate Callbacks to send status to.
 	 * @param awsTableName AWS table name to use when putting data.
 	 * @throws DataOutHandlerException
 	 */
 	public void initializeDatabase(String remoteDatabase, String databaseType, 
 			T2AuthDelegate t2AuthDelegate, String awsTableName) throws DataOutHandlerException {
 		mAwsTableName = awsTableName;
 		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
 		// Do it this way for backward compatibility
 		mSharedPreferences.edit().putString("external_database_type", databaseType);
 		mT2AuthDelegate = t2AuthDelegate;
 		initializeDatabase("", "", "", "", remoteDatabase);		
 	}
 	
 	/**
 	 * Initializes the current database
 	 * 
 	 * Note that all of the parameters (with the exception of remoteDatabase) sent to this routine are for CouchDB only.
 	 * Currently they are all N/A
 	 * 
 	 * Endpoint for all initialize variants.
 	 * 
 	 * @param databaseName		N/A Local SQLITE database name
 	 * @param designDocName		N/A Design document name
 	 * @param designDocId		N/A Design document ID
 	 * @param viewName			N/AView associated with database
 	 * @param remoteDatabase	Name of external database
 	 * @throws DataOutHandlerException 
 	 */
 	public void initializeDatabase(String databaseName, String designDocName, String designDocId, String viewName, String remoteDatabase) throws DataOutHandlerException {
 
 		mDatabaseEnabled = true;
 
 		// Set database type
 		mDatabaseType = DATABASE_TYPE_NONE; 
 
 		// Get chosen database from preferences
 		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
 		String databaseTypeString = mSharedPreferences.getString("external_database_type", "AWS");
 
 		// Based on database type:
 		// 	Set up mRemoteDatabase based on either remoteDatabase if it's not blank,
 		// 	or default values based on database type
 		
 		// Then do any database type specific initialization
 		
 		if (databaseTypeString.equalsIgnoreCase("AWS")) {
 			Log.d(TAG, "Using AWS Database type");
 
 			mDatabaseType = DATABASE_TYPE_AWS;
 			if (remoteDatabase != null ) {
 				if (remoteDatabase.equalsIgnoreCase("")) {
 					mRemoteDatabase = DEFAULT_AWS_DB_URL;			
 				}
 				else {
 					mRemoteDatabase = remoteDatabase;
 				}
 				
 				// Note: for AWS we don't supply a token URL, thats
 				// only for interfacing with Drupal
 		        mEngage = JREngage.initInstance(mContext, mEngageAppId, "", this);
 //		        mEngage = JREngage.initInstance(mContext, mEngageAppId, mEngageTokenUrl, this);
 		        JREngage.blockOnInitialization();
 				
 				
 				//	clientManager = new AmazonClientManager(mContext.getSharedPreferences("com.amazon.aws.demo.AWSDemo", Context.MODE_PRIVATE), mRemoteDatabase);	
 				sClientManager = new AmazonClientManager(mSharedPreferences, mRemoteDatabase);	
 				// TBD - we should probably check the table status
 				//new CheckTableStatusTask().execute("");			
 				
 				int i = 0;
 				i++;
 			}			
 			
 		}
 		else if (databaseTypeString.equalsIgnoreCase("T2REST")) {
 			Log.d(TAG, "Using T2 Rest Database type");
 
 			mDatabaseType = DATABASE_TYPE_T2_REST;
 			if (remoteDatabase != null ) {
 				if (remoteDatabase.equalsIgnoreCase("")) {
 					mRemoteDatabase = DEFAULT_REST_DB_URL;			
 				}
 				else {
 					mRemoteDatabase = remoteDatabase;
 				}
 				
 		        mEngage = JREngage.initInstance(mContext, mEngageAppId, mEngageTokenUrl, this);
 		        JREngage.blockOnInitialization();
 			}				
 			
 		}
 		else if (databaseTypeString.equalsIgnoreCase("T2DRUPAL")) {
 			Log.d(TAG, "Using T2 Drupal Database type");
 
 			mDatabaseType = DATABASE_TYPE_T2_DRUPAL;
 			if (remoteDatabase != null ) {
 				if (remoteDatabase.equalsIgnoreCase("")) {
 					mRemoteDatabase = DEFAULT_DRUPAL_DB_URL;			
 				}
 				else {
 					mRemoteDatabase = remoteDatabase;
 				}
 				
 		        mEngage = JREngage.initInstance(mContext, mEngageAppId, mEngageTokenUrl, this);
 		        JREngage.blockOnInitialization();
 
 		        mServicesClient = new ServicesClient(mRemoteDatabase);
 		        mCookieStore = new PersistentCookieStore(mContext);    
 			}			
 
 		}
 		
 		// Make sure a valid database was selected
 		if (mDatabaseType == DATABASE_TYPE_NONE) {
 			throw new DataOutHandlerException("Invalid database type");
 		}
 
 		// Now do any global database (ot other)  initialization
 		Log.d(TAG, "Initializing T2 database dispatcher");
 		Log.d(TAG, "Remote database name = " + mRemoteDatabase);
 		mPendingQueue = new ArrayList<DataOutPacket>();		
 		mDispatchThread = new DispatchThread();
 		mDispatchThread.start();		
 	}			
 	
 	/**
 	 * Displays authentication dialog and takes the user through
 	 * the entire authentication process.
 	 * 
 	 * @param thisActivity Calling party activity
 	 */
 	public void showAuthenticationDialog(Activity thisActivity) {
         mEngage.showAuthenticationDialog(thisActivity);
 	}
 	
 	/**
 	 * Cancells authentication
 	 */
 	public void logOut() {
 		mAuthenticated = false;
 	}
 	
 	
 	/**
 	 *  Enables logging to external log file of entries sent to the database
 	 *  
 	 * @param context	Calling party's context
 	 */
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
 
 	/**
 	 * Enables cat file logging of data puts
 	 */
 	public void enableLogCat() {
 		mLogCatEnabled = true;
 	}	
 	
 	
 	/**
 	 * Purges and closes the current log file.
 	 */
 	public void purgeLogFile() {
 		if (mLoggingEnabled && mLogWriter != null) {
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
 
 		mServicesClient.mAsyncHttpClient.cancelRequests(mContext, true);
 		
 		Log.e(TAG, " ***********************************closing ******************************");
 		if (mLoggingEnabled && mLogWriter != null) {
 			if (mLogFormat == LOG_FORMAT_JSON) {
 				mLogWriter.write("],}");
 			}
 			mLogWriter.close();			
 		}
 		
 		if(mDispatchThread != null) {
 			mDispatchThread.cancel();
 			mDispatchThread.interrupt();
 			mDispatchThread = null;
 		}
 		
 		mT2AuthDelegate = null;
 		mAuthenticated = false;
 	}
 	/**
 	 * Sends a data packet to all configured output sinks (Database)
 	 * Actually it just puts it in the mPendingQueue to
 	 * be sent out later 
 	 * 
 	 * @param packet - data Packet to send to output sinks
 	 * @throws DataOutHandlerException 
 	 */
 	public void handleDataOut(final DataOutPacket packet) throws DataOutHandlerException {
 			
 
 		if (mRequiresAuthentication == true && mAuthenticated == false) {
 			throw new DataOutHandlerException("User is not authenticated");
 		}
 		int i = 0;
 		i++;
 		
 		
 		//packet.mItem.put("data", packet.mData);		
 		
 //		if (mLogFormat == LOG_FORMAT_JSON) {
 //			packet.mStr += "},";
 //		}
 //
 //		if (mLoggingEnabled) {	
 //			mLogWriter.write(packet.mStr);
 //		}
 //
 //		if (mLogCatEnabled) {
 //			Log.d(TAG, packet.mStr);			
 //		}
 		
 		if (mDatabaseEnabled) {
 			Log.d(TAG, "Queueing document " + packet.mId);
 
 			synchronized(mPendingQueue) {
 				mPendingQueue.add(0,  packet);
 			}
 		}
 	}	
 
     /**
      * Sends a specific json string to Drupal database for processing
      * 
      * @param jsonString
      */
     void drupalNodePut(String jsonString) {
         UserServices us;
         us = new UserServices(mServicesClient);
 
         JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
             @Override
             public void onSuccess(JSONObject response) {
                 try {
                     String s = response.getString("nid");
                     Log.d(TAG, "Successfully submitted article # " + s.toString());
                     
                 } catch (JSONException e) {
                     Log.e(TAG, e.toString());
                 }
             }
 
             @Override
             public void onFailure(Throwable e, JSONObject response) {
                 Log.e(TAG, e.toString());
                 Log.e("Tag", response.toString());
             }
 
             @Override
             public void onFinish() {
                 Log.d(TAG, "onFinish()");
             	
             }
         };        
         us.NodePut(jsonString, responseHandler);
     } 	
 		
 	
 	/**
 	 * @author scott.coleman
 	 *
 	 * This thread handles maintenance of the mPendingQueue
 	 * sending data out if the network is available.
 	 * 
 	 */
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
 							Iterator<DataOutPacket> iterator = mPendingQueue.iterator();						
 							while(iterator.hasNext()) {
 								
 								DataOutPacket packet = iterator.next();
 								Log.d(TAG, "Posting document " + packet.mId);
 								
 								if (mLogFormat == LOG_FORMAT_JSON) {
 									packet.mLoggingString = "{" + SHORT_TIME_STAMP + ":" + packet.mCurrentTime + ",";			
 								}
 								else {
 									packet.mLoggingString = SHORT_TIME_STAMP + ",";			
 								}							
 							
 								// Note that AWS and DRUPAL send only one packet at a time.
 								// T2REST has the ability to send multiple packets in a JSON array
 								if (mDatabaseType == DATABASE_TYPE_T2_DRUPAL) {
 									ObjectNode item = JsonNodeFactory.instance.objectNode();
 									item.put("title", packet.mId);
 									item.put("type", "sensor_data");
 									item.put("language", "und");										
 
 									Iterator it = packet.mItemsMap.entrySet().iterator();
 									while (it.hasNext()) {
 										Map.Entry pairs = (Map.Entry)it.next();	
 								        if (pairs.getValue() instanceof Integer) {
 								        	putDrupalNode((String)pairs.getKey(), (Integer)pairs.getValue(), item);								        	
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }
 								        if (pairs.getValue() instanceof String) {
 								        	putDrupalNode((String)pairs.getKey(), (String)pairs.getValue(), item);								        	
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }
 								        if (pairs.getValue() instanceof Long) {
 								        	putDrupalNode((String)pairs.getKey(), (Long)pairs.getValue(), item);								        	
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }
 								        if (pairs.getValue() instanceof Double) {
 								        	putDrupalNode((String)pairs.getKey(), (Double)pairs.getValue(), item);								        	
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }
 								        if (pairs.getValue() instanceof Vector) {
 											// Note special format for vector in drupal!
 											String newTag = "field_" + ((String) pairs.getKey()).toLowerCase();
 											ObjectNode undNode = JsonNodeFactory.instance.objectNode();		
 											ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();		
 
 								        	// TODO: Potential problem - saves all of the vector items as type STRING
 											for (Object v : (Vector) pairs.getValue()) {
 												ObjectNode valueNode = JsonNodeFactory.instance.objectNode();		
 												valueNode.put("value", v.toString());
 												arrayNode.add(valueNode);	
 											}
 											
 											undNode.put("und", arrayNode);			
 											item.put(newTag, undNode);									        	
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }										
 										
 									} // End while (it.hasNext())
 							        // Check to see if we've stored a Drupal session cookie. If so then attach then to 
 							        // the http client
 							        T2CookieStore.getInstance();
 							        Cookie cookie = T2CookieStore.getInstance().getSessionCookie();
 							        if (cookie != null) {
 							        	
 							        	
 							          mCookieStore.addCookie(cookie);
 							          mServicesClient.setCookieStore(mCookieStore);        
 
 							          // TODO: change to debug - it's at error now simply for readability
 							          Log.e(TAG, "Using session cookie: " + cookie.toString());
 							        }
 							        else {
 							            Log.e(TAG, "No Stored Cookies to use: ");
 							        }   								
 									
 									//Log.d(TAG, "Posting entry " + item.toString());
 							        
 									drupalNodePut(item.toString());									
 		
 								} // End if (mDatabaseType == DATABASE_TYPE_T2_DRUPAL)
 								
 								
 								if (mDatabaseType == DATABASE_TYPE_AWS) {
 									
 									// First need to format this packet for AWS
 									HashMap<String, AttributeValue> hashMap = new HashMap<String, AttributeValue>();									
 									Iterator it = packet.mItemsMap.entrySet().iterator();
 									while (it.hasNext()) {
 										Map.Entry pairs = (Map.Entry)it.next();
 										
 								        if (pairs.getValue() instanceof Integer) {
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 											AttributeValue attr = new AttributeValue().withS(String.valueOf(pairs.getValue()));	
 											hashMap.put((String)pairs.getKey(), attr);								        	
 								        }
 								        if (pairs.getValue() instanceof String) {
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 											AttributeValue attr = new AttributeValue().withS((String)pairs.getValue());	
 											hashMap.put((String)pairs.getKey(), attr);								        	
 								        }
 								        if (pairs.getValue() instanceof Long) {
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 											AttributeValue attr = new AttributeValue().withS(String.valueOf(pairs.getValue()));	
 											hashMap.put((String)pairs.getKey(), attr);								        	
 								        }
 								        if (pairs.getValue() instanceof Double) {
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 											AttributeValue attr = new AttributeValue().withS(String.valueOf(pairs.getValue()));	
 											hashMap.put((String)pairs.getKey(), attr);								        	
 								        }
 								        if (pairs.getValue() instanceof Vector) {
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 											AttributeValue attr = new AttributeValue().withSS((Vector)pairs.getValue());	
 											hashMap.put((String)pairs.getKey(), attr);								        	
 								        }								
 									}		
 									
 	
 																	
 									AmazonDynamoDBClient ddb = DataOutHandler.sClientManager
 											.ddb();
 									try {
 										
 										PutItemRequest request = new PutItemRequest().withTableName(
 												mAwsTableName)
 												.withItem(hashMap);
 	
 										ddb.putItem(request);
 										Log.d(TAG, "AWS Posting Successful: ");
 										
 									} catch (AmazonServiceException ex) {
 										DataOutHandler.sClientManager
 												.wipeCredentialsOnAuthError(ex);
 										Log.d(TAG, "Error posting document " + ex.toString());
 									}	
 									catch (Exception ex) {
 										DataOutHandler.sClientManager.clearCredentials();
 										Log.d(TAG, "Error posting document " + ex.toString());
 									}									
 							    } // End if (mDatabaseType == DATABASE_TYPE_AWS)
 							
 								if (mDatabaseType == DATABASE_TYPE_T2_REST) {
 									ObjectNode item = JsonNodeFactory.instance.objectNode();;	
 									
 									HashMap<String, AttributeValue> hashMap = new HashMap<String, AttributeValue>();									
 									Iterator it = packet.mItemsMap.entrySet().iterator();
 									while (it.hasNext()) {
 										Map.Entry pairs = (Map.Entry)it.next();
 										
 								        if (pairs.getValue() instanceof Integer) {
 								        	item.put((String)pairs.getKey(),(Integer)pairs.getValue());
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }
 								        if (pairs.getValue() instanceof String) {
 								        	item.put((String)pairs.getKey(),(String)pairs.getValue());
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }
 								        if (pairs.getValue() instanceof Long) {
 								        	item.put((String)pairs.getKey(),(Long)pairs.getValue());
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }
 								        if (pairs.getValue() instanceof Double) {
 								        	item.put((String)pairs.getKey(),(Double)pairs.getValue());
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }
 								        if (pairs.getValue() instanceof Vector) {
 
 								        	// TODO: Potential problem - saves all of the vector items as type STRING
 								        	String vectorString = "";
 								        	ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();						
 											for (Object v : (Vector) pairs.getValue()) {
 												ObjectNode valueNode = JsonNodeFactory.instance.objectNode();		
 												valueNode.put("value", v.toString());
 												arrayNode.add(v.toString());	
 											}
 								        	item.put((String)pairs.getKey(),arrayNode);
 											packet.mLoggingString += formatTextForLog(mDatabaseType, pairs);
 								        }								
 									} // End while (it.hasNext())
 	
 									 if (iteration++ > 0)
 										 jsonString += "," + item.toString();
 									 else
 										 jsonString += item.toString();								
 									
 								} // End if (mDatabaseType == DATABASE_TYPE_T2_REST)							
 		
 								// We've processed one packet, now write to logs
 								if (mLogFormat == LOG_FORMAT_JSON) {
 									
 									// Remove the comma at the end of the string set
 									packet.mLoggingString = packet.mLoggingString.substring(0, packet.mLoggingString.length() - 1);
 									
 									// Now terminate the json string
 									packet.mLoggingString += "},";
 								}
 								
 								if (mLoggingEnabled) {	
 									mLogWriter.write(packet.mLoggingString);
 								}
 	
 								if (mLogCatEnabled) {
 									Log.d(TAG, packet.mLoggingString);			
 								}							
 							
 							} // End while(iterator.hasNext())
 	
 							mPendingQueue.clear();
 							
 							
 							// We've gone through all of the records in mPendingQueue 
 							jsonString += "]";
 							
 							if (mDatabaseType == DATABASE_TYPE_T2_REST) {
 								RequestParams params = new RequestParams("json", jsonString);
 								Log.d(TAG, "Sending to: " + mRemoteDatabase);
 								Log.e(TAG,  jsonString);
 								
 						        T2RestClient.post(mRemoteDatabase, params, new AsyncHttpResponseHandler() {
 						            @Override
 						            public void onSuccess(String response) {
 										Log.d(TAG, "T2Rest Posting Successful: " + response);
 						                
 						            }
 						        });								
 								
 							} // End if (mDatabaseType == DATABASE_TYPE_T2_REST)
 						} // End if (mPendingQueue.size() > 0)
 					} // End synchronized(mPendingQueue)
 
 				} // End if (isNetworkAvailable())
 			} // End while(true)
 				
 			isRunning = false;
 		} // End public void run() 
 		
 		/**
 		 * Writes drual formatted node to specified node (String)
 		 * 
 		 * @param tag Data Tag
 		 * @param value Data Value
 		 * @param node Node to write to 
 		 */
 		private void putDrupalNode(String tag, String value, ObjectNode node) {
 			String newTag = "field_" + tag.toLowerCase();
 			ObjectNode valueNode = JsonNodeFactory.instance.objectNode();		
 			ObjectNode undNode = JsonNodeFactory.instance.objectNode();		
 			ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();		
 			valueNode.put("value", value);
 			arrayNode.add(valueNode);	
 			undNode.put("und", arrayNode);			
 			node.put(newTag, undNode);
 		}
 		
 		/**
 		 * Writes drual formatted node to specified node (Long)
 		 * 
 		 * @param tag Data Tag
 		 * @param value Data Value
 		 * @param node Node to write to 
 		 */
 		private void putDrupalNode(String tag, long value, ObjectNode node) {
 			String newTag = "field_" + tag.toLowerCase();
 			ObjectNode valueNode = JsonNodeFactory.instance.objectNode();		
 			ObjectNode undNode = JsonNodeFactory.instance.objectNode();		
 			ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();		
 			valueNode.put("value", value);
 			arrayNode.add(valueNode);	
 			undNode.put("und", arrayNode);			
 			node.put(newTag, undNode);
 		}
 		
 		/**
 		 * Writes drual formatted node to specified node (Int)
 		 * 
 		 * @param tag Data Tag
 		 * @param value Data Value
 		 * @param node Node to write to 
 		 */
 		private void putDrupalNode(String tag, int value, ObjectNode node) {
 			String newTag = "field_" + tag.toLowerCase();
 			ObjectNode valueNode = JsonNodeFactory.instance.objectNode();		
 			ObjectNode undNode = JsonNodeFactory.instance.objectNode();		
 			ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();		
 			valueNode.put("value", value);
 			arrayNode.add(valueNode);	
 			undNode.put("und", arrayNode);			
 			node.put(newTag, undNode);
 		}
 		
 		/**
 		 * Writes drual formatted node to specified node (Doubleg)
 		 * 
 		 * @param tag Data Tag
 		 * @param value Data Value
 		 * @param node Node to write to 
 		 */
 		private void putDrupalNode(String tag, double value, ObjectNode node) {
 			String newTag = "field_" + tag.toLowerCase();
 			ObjectNode valueNode = JsonNodeFactory.instance.objectNode();		
 			ObjectNode undNode = JsonNodeFactory.instance.objectNode();		
 			ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();		
 			valueNode.put("value", value);
 			arrayNode.add(valueNode);	
 			undNode.put("und", arrayNode);			
 			node.put(newTag, undNode);
 		}
 				
 		
 		/**
 		 * Formats a string that can be use for log files.
 		 *  format is based on log type and database type 
 		 * @param databaseType Type of database
 		 * @param pairs Map of data entry
 		 * 
 		 * @return Formatted string based
 		 */
 		String formatTextForLog(int databaseType, Map.Entry pairs) {
 			String result = "";
 			
 	        if (pairs.getValue() instanceof Integer) {
 				if (mLogFormat == LOG_FORMAT_JSON) {
					result = "\"" + pairs.getKey() + "\":" + pairs.getValue()  + ",";			
 				}
 				else {
 					result = "" + pairs.getValue()  + ",";			
 				}							        	
 	        }
 	        if (pairs.getValue() instanceof String) {
 				if (mLogFormat == LOG_FORMAT_JSON) {
 					result = "\"" + pairs.getKey() + "\":\"" + pairs.getValue() + "\",";			
 				}
 				else {
 					result = "" + pairs.getValue() + ",";			
 				}							        	
 	        }
 	        if (pairs.getValue() instanceof Long) {
 				if (mLogFormat == LOG_FORMAT_JSON) {
 					result = "\"" + pairs.getKey() + "\":" + pairs.getValue() + ",";			
 				}
 				else {
 					result = "" + pairs.getValue() + ",";			
 				}							        	
 	        }
 	        if (pairs.getValue() instanceof Double) {
 				if (mLogFormat == LOG_FORMAT_JSON) {
 					result = String.format("\"%s\":\"%f\",", pairs.getKey(),pairs.getValue());
 				}
 				else {
 					result = "" + pairs.getValue() + ",";			
 				}							        	
 	        }
 	        if (pairs.getValue() instanceof Vector) {
 				if (mLogFormat == LOG_FORMAT_JSON) {
 					ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();						
 					for (Object v : (Vector) pairs.getValue()) {
 						ObjectNode valueNode = JsonNodeFactory.instance.objectNode();		
 						valueNode.put("value", v.toString());
 						arrayNode.add(v.toString());	
 					}
 					result = "\"" + pairs.getKey() + "\":" + arrayNode.toString() + ",";
 				}
 				else {
 					result = "" + pairs.getValue().toString() + ",";			
 				}							        	
 	        }								
 			
 			
 			return result;
 		}
 
 		/**
 		 * Cancel the loop
 		 */
 		public void cancel() {
 			this.cancelled = true;
 			Log.e(TAG, "Cancelled");
 			
 		}
 		
 		/**
 		 * 
 		 * @return true if running false otherwise
 		 */
 		public boolean isRunning() {
 			return this.isRunning;
 		}
 	} // End DispatchThread
 	
     /**
      * @return true if network is available
      */
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
 
 	/**
 	 * Logs a text note to sinks
 	 * 
 	 * @param note - Text not to log to sinks
 	 * @throws DataOutHandlerException 
 	 */
 	public void logNote(String note) throws DataOutHandlerException {
 		DataOutPacket packet = new DataOutPacket();
 		packet.add(DataOutHandlerTags.NOTE, note);
 		handleDataOut(packet);				
 	}
     
     
     // JanRain Delegates (status callbacks)
     
     
 	@Override
 	public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info,
 			String provider) {
 		Log.d(TAG, "jrAuthenticationDidSucceedForUser");		
 		
 	    mAuth_info = auth_info;
 		// Note, if we're using drupal the authentication isn't 
 		// really done until the callback URL has been called
 		// This sets up the Drupal Database
 		if (mDatabaseType == DATABASE_TYPE_T2_DRUPAL) {
 			
 		} else {
 			mAuthProvider = provider;		
 			mAuthenticated = true;
 			
 			if (mT2AuthDelegate != null) {
 				mT2AuthDelegate.T2AuthSuccess(mAuth_info, mAuthProvider, null, null);
 			}
 		}
 	}
 
 	@Override
 	public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
 			HttpResponseHeaders responseHeaders,String responsePayload,
 			String provider) {
 		Log.d(TAG, "jrAuthenticationDidReachTokenUrl");		
 
 		mAuthenticated = true;
 		if (mT2AuthDelegate != null) {
 			mT2AuthDelegate.T2AuthSuccess(mAuth_info, mAuthProvider, responseHeaders, responsePayload);
 		}
 	}
 
 	@Override
 	public void jrSocialDidPublishJRActivity(JRActivityObject activity,
 			String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void jrSocialDidCompletePublishing() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
 		Log.d(TAG, "jrEngageDialogDidFailToShowWithError");		
 
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void jrAuthenticationDidNotComplete() {
 		Log.d(TAG, "jrAuthenticationDidNotComplete");		
 		
 	}
 
 	@Override
 	public void jrAuthenticationDidFailWithError(JREngageError error,
 			String provider) {
 		Log.d(TAG, "jrAuthenticationDidFailWithError");		
 		mAuthenticated = false;
 		
 		if (mT2AuthDelegate != null) {
 			mT2AuthDelegate.T2AuthFail(error, provider);
 		}		
 	}
 
 	@Override
 	public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
 			JREngageError error, String provider) {
 		Log.d(TAG, "jrAuthenticationCallToTokenUrlDidFail");		
 		mAuthenticated = false;
 		if (mT2AuthDelegate != null) {
 			mT2AuthDelegate.T2AuthFail(error, provider);
 		}		
 	}
 
 	@Override
 	public void jrSocialDidNotCompletePublishing() {
 		if (mT2AuthDelegate != null) {
 			mT2AuthDelegate.T2AuthNotCompleted();
 		}		
 	}
 
 	@Override
 	public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
 			JREngageError error, String provider) {
 		// TODO Auto-generated method stub
 	} 	
 }
