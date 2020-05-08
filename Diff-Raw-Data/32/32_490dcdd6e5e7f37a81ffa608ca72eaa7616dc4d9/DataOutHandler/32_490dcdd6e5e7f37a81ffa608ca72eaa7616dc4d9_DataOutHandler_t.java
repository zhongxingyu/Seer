 /* T2AndroidLib-SG for Signal Processing
  * 
  * Copyright  2009-2013 United States Government as represented by 
  * the Chief Information Officer of the National Center for Telehealth 
  * and Technology. All Rights Reserved.
  * 
  * Copyright  2009-2013 Contributors. All Rights Reserved. 
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
 import org.json.JSONArray;
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
 
/**
 * Handles interface to external databases.
 * Also initializes Authentication services (JanRain)
 * 
 * 
 * 
 * 
 * @author scott.coleman
 *
 */
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
 	
 	private String mResult;
 
 	
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
 	 * Session cookie Janrain Obtains from Drupal for active session
 	 */
 	private Cookie drupalSessionCookie;
 	
 	/**
 	 * List of Drupal node id's currently existing in Drupal 
 	 */
 	private List<String> mRemoteDrupalNodeIdList;
 
 	/**
 	 * Used to determine when we are done with fetching all of the Drupal
 	 * nodes form Drupal
 	 */
 	private List<String> mRemoteDrupalNodeIdListCheckoff;
 	
 	/**
 	 * List of dataout packets currently residing in remote database  
 	 */
 	public HashMap<String, DataOutPacket> mRemotePacketCache = new HashMap<String, DataOutPacket>();		
 	
 
 	/**
 	 * List of node ids which we have reuqested to be deleted from Drupal
 	 */
 	private List<String> mNodeDeleteQueue = new ArrayList<String>();	
 	
 	
 	
 	private DatabaseCacheUpdateListener mDatabaseUpdateListener;
 	
 	private DataOutHandler mInstance;
 	
 	public void setDatabaseUpdateListener(DatabaseCacheUpdateListener mDatabaseUpdateListener) {
 		this.mDatabaseUpdateListener = mDatabaseUpdateListener;
 	}
 
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
 	
 	
 	public DataOutPacket getPacketByDrupalId(String nodeId) {
 		return mRemotePacketCache.get(nodeId);
 	}
 	
 	public DataOutPacket getPacketByRecordId(String recordId) {
 		
 		for (DataOutPacket packet : mRemotePacketCache.values()) {
 			if (packet.mId.equalsIgnoreCase(recordId)) {
 				return packet;
 			}
 		}
 		
 		return null;
 	}	
 	
 	
     /**
      * This routine determines which node was removed from Drupal and updates the Cache
      * Uses: mNodeDeleteQueue
      */
     public void removePacketFromRemoteDrupalPacketCache() {
    	 UserServices us;
         int nodeNum = 0;
         
         us = new UserServices(mServicesClient);
 
         JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
         	
         	@Override
 			protected void handleSuccessJsonMessage(Object arg0) {
         		// Accumulate a list of all of the current node id's in currentNodeIdList
         		JSONArray array = (JSONArray) arg0;
 				mRemoteDrupalNodeIdList = new ArrayList<String>();	        		
                 for (int i = 0; i < array.length(); i++) {
                 	JSONObject jObject  = (JSONObject) array.opt(i);
                 	try {
 						String nodeId = (String) jObject.get("nid");
 						mRemoteDrupalNodeIdList.add(nodeId);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
                 }
                 
                 // Now mRemoteDrupalNodeIdList contains a list of all nodes contained in Drupal
                 // We've successfully deleted a node from Drupal.
                 // The problem is there is no indication of which node was deleted!
                 // So now we need to use the array mNodeDeleteQueue to
                 // determine which node was deleted, then delete that from the cache
                 for (String nid : mNodeDeleteQueue) {
                 	if (!mRemoteDrupalNodeIdList.contains(nid)) {
                 		DataOutPacket packet = mRemotePacketCache.get(nid);
             			mRemotePacketCache.remove(nid);
                         if (mInstance.mDatabaseUpdateListener != null) {
                         	mInstance.mDatabaseUpdateListener.remoteDatabaseDeleteComplete(packet);
                         }                
                 	}
                 }
 			}
 
 			@Override
             public void onSuccess(JSONObject response) {
             }
 
 			@Override
 			public void onSuccess(JSONArray arg0) {
 				super.onSuccess(arg0);
 			}
 
             
             @Override
             public void onFailure(Throwable e, JSONObject response) {
                 Log.e(TAG, e.toString());
             }
             
             @Override
 			public void onFailure(Throwable arg0, JSONArray arg1) {
                 Log.e(TAG, arg0.toString());
 			}
 
 			@Override
             public void onFinish() {
                 Log.d(TAG, "onFinish(removePacketFromRemoteDrupalPacketCache)");
             	
             }
         };        
         
         us.NodeGet(responseHandler); // Gets all nodes from drupal
    }	
 	
 	
     /**
      * Retrieves the contents of the updated packet from Drupal and  updates the Cache
      * 
      * @param DrupalNodeId node id of drupal packet added
      */
     void addPacketToRemoteDrupalPacketCache(String DrupalNodeId) {
         UserServices us;
         int nodeNum = 0;
         
         us = new UserServices(mServicesClient);    	
 
         JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
 
 			@Override
             public void onSuccess(JSONObject response) {
 				
                 	String drupalNodeContents = response.toString();
                 	// Now convert the drupal node to a dataOutPacket.                    	
                 	DataOutPacket dataOutPacket;
 					try {
 						dataOutPacket = new DataOutPacket(response);
 						
 						mRemotePacketCache.put(dataOutPacket.mDrupalNid, dataOutPacket);
 	                	
 //	                	Log.e(TAG,mRemoteDrupalPacketList.toString());
 	                	
 	                	if (mDatabaseUpdateListener != null) {
 	                		//mDrupalUpdateListener.drupalCreateUpdateComplete("Updated cache from Drupal: (" + dataOutPacket.mDrupalNid + ") : " + dataOutPacket.toString() );
 	                		mDatabaseUpdateListener.remoteDatabaseCreateUpdateComplete(dataOutPacket);
 	                	}						
 						
 					} catch (DataOutHandlerException e) {
 						Log.e(TAG, e.toString());
 						//e.printStackTrace();
 					}
             }
 
 			@Override
 			public void onSuccess(JSONArray arg0) {
 				super.onSuccess(arg0);
 			}
             
             @Override
             public void onFailure(Throwable e, JSONObject response) {
                 Log.e(TAG, e.toString());
             	if (mDatabaseUpdateListener != null) {
             		mDatabaseUpdateListener.remoteDatabaseFailure(e.toString());
             	}	                
             }
             
             @Override
 			public void onFailure(Throwable arg0, JSONArray arg1) {
                 Log.e(TAG, arg0.toString());
             	if (mDatabaseUpdateListener != null) {
             		mDatabaseUpdateListener.remoteDatabaseFailure(arg0.toString());
             	}	                
                 
 			}
 
 			@Override
             public void onFinish() {
                 Log.d(TAG, "onFinish(addPacketToRemoteDrupalPacketCache)");
             	
             }
         };        
         
         
     	try {
     		nodeNum = Integer.parseInt(DrupalNodeId);
             us.NodeGet(nodeNum, responseHandler);
 		} catch (NumberFormatException e1) {
 			Log.e(TAG, e1.toString());
 		}
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
 		mInstance = this;
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
 		mInstance = this;
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
 
 		        // This is to account for a bug in janrain where a delegate might not get added in the initinstance call
 		        // As odd as it seems, this ensures that only one delegate gets added per instance.
 		        mEngage.removeDelegate(this);
 		        mEngage.addDelegate(this);
 		        
 		        JREngage.blockOnInitialization();
 
 				
 				//	clientManager = new AmazonClientManager(mContext.getSharedPreferences("com.amazon.aws.demo.AWSDemo", Context.MODE_PRIVATE), mRemoteDatabase);	
 				sClientManager = new AmazonClientManager(mSharedPreferences, mRemoteDatabase);	
 				// TBD - we should probably check the table status
 				//new CheckTableStatusTask().execute("");			
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
		        // This is to account for a bug in janrain where a delegate might not get added in the initinstance call
		        // As odd as it seems, this ensures that only one delegate gets added per instance.
		        mEngage.removeDelegate(this);
		        mEngage.addDelegate(this);
		        
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
		        // This is to account for a bug in janrain where a delegate might not get added in the initinstance call
		        // As odd as it seems, this ensures that only one delegate gets added per instance.
		        mEngage.removeDelegate(this);
		        mEngage.addDelegate(this);
		        
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
 		Log.d(TAG, "DataOuthandler Logging out");		
 		mAuthenticated = false;
 		drupalSessionCookie = null;
         
 		if (mCookieStore != null) {
 			mCookieStore.clear();
 	        mServicesClient.setCookieStore(mCookieStore);   
 		}
         
         if (mEngage != null) {
 //        	mEngage.removeDelegate((JREngageDelegate) context);
         	mEngage.removeDelegate(this);
         }
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
 	 * Sends a data packet (CREATE) to all configured output sinks (Database)
 	 * Actually it just puts it in the mPendingQueue to
 	 * be sent out later 
 	 * 
 	 * @param packet - data Packet to send to output sinks
 	 * @throws DataOutHandlerException
 	 * 
 	 * Note: The data path is a bit circuitous and is explained here (It's the same for update)
 	 *    (Two dips are needed from drupal because the "CREATE" operation only returns the
 	 *    drupal node id and we need the whole package)
 	 * 
 	 * Data path (for drupal):
 	 * 	handleDataOut(packet)
 	 *		mPendingQueue.add(packet); packet action = "U"
 	 *  Dispatch Thread
 	 *		packet = mPendingQueue.get()
 	 *			drupalNodePut(packet)
 	 *				us = new UserServices(mServicesClient);
 	 *				us.NodePost(jsonString, responseHandler)  -> To Drupal
 	 *			onSuccess()                                   <- From Drupal
 	 *				addPacketToRemoteDrupalPacketCache(DrupalNodeId)
 	 *					us = new UserServices(mServicesClient);
 	 *					us.NodeGet(DrupalNodeId, responseHandler);		-> To Drupal
 	 *			onSuccess()                                   			<- From Drupal
 	 *				mRemoteDrupalPacketCache.put(packet.mDrupalNid, packet);
 	 *				mDrupalUpdateListener.drupalCreateUpdateComplete(nid);
 	 *  MainActivity
 	 *				Update displayList  
 	 *  
 	 */
 	public void handleDataOut(final DataOutPacket packet) throws DataOutHandlerException {
 
 		if (mRequiresAuthentication == true && mAuthenticated == false) {
 			throw new DataOutHandlerException("User is not authenticated");
 		}
 		
 		if (mDatabaseEnabled) {
 			packet.mQueuedAction = "C";
 			Log.d(TAG, "Queueing document " + packet.mId);
 
 			synchronized(mPendingQueue) {
 				mPendingQueue.add(0,  packet);
 			}
 		}
 	}	
 
 	/**
 	 * Updates a packet in the database
 	 * 
 	 * See handleDataOut() for the data path (Drupal)
 	 * 
 	 * @param packet Packet to update
 	 * @throws DataOutHandlerException
 	 */
 	public void updateRecord(final DataOutPacket packet) throws DataOutHandlerException {
 		
 		if (mRequiresAuthentication == true && mAuthenticated == false) {
 			throw new DataOutHandlerException("User is not authenticated");
 		}		
 
 		// Now update the database
 		if (mDatabaseEnabled) {
 			packet.mQueuedAction = "U";
 			Log.d(TAG, "Queueing document for Update" + packet.mId);
 
 			synchronized(mPendingQueue) {
 				mPendingQueue.add(0,  packet);
 			}
 		}		
 	}
 	
 	/**
 	 * Deletes a packet from the database
 	 * 
 	 * 
 	 * Data path (for drupal):
 	 * 	handleDataOut(packet)
 	 *		mPendingQueue.add(packet); Packet action = "D"
 	 *  Dispatch Thread
 	 *		packet = mPendingQueue.get()
 	 *			drupalNodePut(packet)
 	 *				us = new UserServices(mServicesClient);
 	 *				us.NodePost(jsonString, responseHandler)  -> To Drupal
 	 *			onSuccess()                                   <- From Drupal
 	 *				removePacketFromRemoteDrupalPacketCache(DrupalNodeId)
 	 *					us = new UserServices(mServicesClient);
 	 *					us.NodeGet(responseHandler);		-> To Drupal (Requests list of id's)
 	 *			handleSuccessJsonMessage()                  <- From Drupal
 	 *					determine which packet was deleted from Drupal
 	 *
 	 *           		mRemoteDrupalPacketCache.remove(nid);
                         mInstance.mDrupalUpdateListener.drupalDeleteComplete(nid);
 	 *  MainActivity
 	 *				Update displayList 
 	 * 
 	 * 
 	 * 
 	 * @param packet Packet to delete
 	 * @throws DataOutHandlerException
 	 */
 	public void deleteRecord(final DataOutPacket packet) throws DataOutHandlerException {
 		
 		if (mRequiresAuthentication == true && mAuthenticated == false) {
 			throw new DataOutHandlerException("User is not authenticated");
 		}		
 
 		// Now update the database
 		if (mDatabaseEnabled) {
             // Drupal rest service has a limitation that it doesn't return an indication
 			// of which node was deleted. Therefore we must manually keep track of
 			// the nodes we have requested to be deleted
 			mNodeDeleteQueue.add(packet.mDrupalNid);
 			
 			
 			packet.mQueuedAction = "D";
 			Log.d(TAG, "Queueing document for Delete" + packet.mId);
 
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
     void drupalNodePut(String jsonString, String queuedAction, String drupalNodeId) {
         UserServices us;
         us = new UserServices(mServicesClient);
 
         JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
             
         	// We get here for Create or Update operations
         	@Override
             public void onSuccess(JSONObject response) {
                 try {
                     String nid = response.getString("nid");
                     Log.d(TAG, "Successfully submitted article # " + nid);
                     
                     addPacketToRemoteDrupalPacketCache(nid);
                     
                 } catch (JSONException e) {
                     Log.e(TAG, e.toString());
                 }
             }
 
         	// We get here for Delete operations
 			@Override
 			public void onSuccess(JSONArray arg0) {
                 Log.d(TAG, "Successfully submitted ARRAY " + arg0.toString());
                 
                 removePacketFromRemoteDrupalPacketCache();                
 
 			}
             
             @Override
             public void onFailure(Throwable e, JSONObject response) {
                 Log.e(TAG, e.toString());
             	if (mDatabaseUpdateListener != null) {
             		mDatabaseUpdateListener.remoteDatabaseFailure(e.toString());
             	}	                 
             }
             
             @Override
 			public void onFailure(Throwable arg0, JSONArray arg1) {
                 Log.e(TAG, arg0.toString());
             	if (mDatabaseUpdateListener != null) {
             		mDatabaseUpdateListener.remoteDatabaseFailure(arg0.toString());
             	}	                 
 			}
 
 
 			@Override
             public void onFinish() {
                 Log.d(TAG, "onFinish(drupalNodePut)");
             	
             }
         };        
         
         if (queuedAction.equalsIgnoreCase("C")) {
             us.NodePost(jsonString, responseHandler);
         }
         else if (queuedAction.equalsIgnoreCase("U")) {
             us.NodePut(jsonString, responseHandler, drupalNodeId);
         }
         else if (queuedAction.equalsIgnoreCase("D")) {
             us.NodeDelete(responseHandler, drupalNodeId);
         }
     } 	
     
     /**
      * 
      * Part A - 
      *   Polls remote Drupal database and fills mRemoteDrupalNodeIdList
      *   with a list of current node id's
      * Part B - 
      *   Calls getDrupalNodesFromRemoteDrupalNodeIdList to fill mRemotePacketcACHE
      *   
      *   On termination, 
      *   	mRemotePacketCache is updated
      *   	mDatabaseCacheUpdateListener.getNodesComplete() is called (May be called progressively)
      *   
      *   
      */
     public void getRemoteDatabaseNodes() {
     	 UserServices us;
          int nodeNum = 0;
          
          us = new UserServices(mServicesClient);
 
          JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
          	
          	@Override
  			protected void handleSuccessJsonMessage(Object arg0) {
          		// Accumulate a list of all of the current node id's in currentNodeIdList
          		JSONArray array = (JSONArray) arg0;
  				mRemoteDrupalNodeIdList = new ArrayList<String>();	        		
                  for (int i = 0; i < array.length(); i++) {
                  	JSONObject jObject  = (JSONObject) array.opt(i);
                  	try {
  						String nodeId = (String) jObject.get("nid");
  						mRemoteDrupalNodeIdList.add(nodeId);
  					} catch (JSONException e) {
  						e.printStackTrace();
  					}
                  }
                  
                  getDrupalNodesFromRemoteDrupalNodeIdList();                
          		
          		Log.e(TAG, array.toString());
  			}
 
  			@Override
              public void onSuccess(JSONObject response) {
              }
 
  			@Override
  			public void onSuccess(JSONArray arg0) {
  				super.onSuccess(arg0);
  			}
 
              
              @Override
              public void onFailure(Throwable e, JSONObject response) {
                  Log.e(TAG, e.toString());
              }
              
              @Override
  			public void onFailure(Throwable arg0, JSONArray arg1) {
                  Log.e(TAG, arg0.toString());
  			}
 
  			@Override
              public void onFinish() {
                  Log.d(TAG, "onFinish()");
              	
              }
          };        
          
          us.NodeGet(responseHandler);
     }
     
     /**
      *   Part B (from above)
      *   Calls Gets each node id from mRemoteDrupalNodeIdList to 
      *   and fills mRemoteDrupalPacketcACHE with the actual packets   
      */
     void getDrupalNodesFromRemoteDrupalNodeIdList() {
         UserServices us;
         int nodeNum = 0;
         mRemotePacketCache = new HashMap<String, DataOutPacket>();
         
         us = new UserServices(mServicesClient);    	
 
         mRemoteDrupalNodeIdListCheckoff = mRemoteDrupalNodeIdList;
         
     	for (String nid : mRemoteDrupalNodeIdList) {
     		
             JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
 
     			@Override
                 public void onSuccess(JSONObject response) {
                     	String drupalNodeContents = response.toString();
                     	// Now convert the drupal node to a dataOutPacket.                    	
                     	DataOutPacket dataOutPacket;
 						try {
 							dataOutPacket = new DataOutPacket(response);
 	                    	mRemotePacketCache.put(dataOutPacket.mDrupalNid, dataOutPacket);
 	                    	Log.e(TAG, "Fetched (" + dataOutPacket.mDrupalNid + ") : "+ dataOutPacket.toString());
 	                    	
 	                    	// Check off node id's received, when we've received the last one notify
 	                    	// the caller
 //	                    	Log.e(TAG, mRemoteDrupalNodeIdListCheckoff.toString());
 	                    	mRemoteDrupalNodeIdListCheckoff.remove(dataOutPacket.mDrupalNid );
 //	                    	Log.e(TAG, mRemoteDrupalNodeIdListCheckoff.toString());
 //	                    	if (mRemoteDrupalNodeIdListCheckoff.size() == 0) {
 	                        if (true) {			// For now update the display every time we get here
 	                        	if (mDatabaseUpdateListener != null) {
 	                        		mDatabaseUpdateListener.remoteDatabasedeGetNodesComplete();
 	                        	}                    		
 	                    	}
 						} catch (DataOutHandlerException e) {
 							Log.e(TAG, e.toString());
 							//e.printStackTrace();
 	                      	if (mDatabaseUpdateListener != null) {
                         		mDatabaseUpdateListener.remoteDatabasedeGetNodesComplete();
                         	}							
 						}
                 }
 
     			@Override
     			public void onSuccess(JSONArray arg0) {
     				super.onSuccess(arg0);
     			}
 
                 @Override
                 public void onFailure(Throwable e, JSONObject response) {
                     Log.e(TAG, e.toString());
                 }
                 
                 @Override
     			public void onFailure(Throwable arg0, JSONArray arg1) {
                     Log.e(TAG, arg0.toString());
     			}
 
     			@Override
                 public void onFinish() {
                     Log.d(TAG, "onFinish()");
                 }
             };        
             
         	try {
         		nodeNum = Integer.parseInt(nid);
                 us.NodeGet(nodeNum, responseHandler);
 			} catch (NumberFormatException e1) {
 			}
     	}
     }    
     
     /**
      * Retrieves a specific drupal node
      * 
      * @param jsonString - Drupal node id of node to return
      */
     public void drupalNodeGet(String nodeStr) {
         UserServices us;
         int nodeNum = 0;
         
         us = new UserServices(mServicesClient);
 
         if (!nodeStr.equalsIgnoreCase("*")) {
         	try {
         		nodeNum = Integer.parseInt(nodeStr);
 			} catch (NumberFormatException e1) {
 				nodeStr = "*";
 			
 			}
         }
         
         JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
         	
 			@Override
             public void onSuccess(JSONObject response) {
                 try {
                 	mResult = response.toString(); 
                 	String s = response.getString("nid");
                     Log.d(TAG, "Received: " + s.toString());
                     
                 } catch (JSONException e) {
                     Log.e(TAG, e.toString());
                 }
             }
 
 			@Override
 			public void onSuccess(JSONArray arg0) {
 				// TODO Auto-generated method stub
 				super.onSuccess(arg0);
 			}
             
             @Override
             public void onFailure(Throwable e, JSONObject response) {
                 Log.e(TAG, e.toString());
             }
             
             @Override
 			public void onFailure(Throwable arg0, JSONArray arg1) {
                 Log.e(TAG, arg0.toString());
 			}
 
 			@Override
             public void onFinish() {
                 Log.d(TAG, "onFinish()");
             	
             }
         };        
         
         if (nodeStr.equalsIgnoreCase("*")) {
             us.NodeGet(responseHandler);
         }
         else {
             us.NodeGet(nodeNum, responseHandler);
         }
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
				//Log.d(TAG, "Http dispatch thread tick");
 
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
 								        if (pairs.getValue() instanceof Float) {
 								        	putDrupalNode((String)pairs.getKey(), (Float)pairs.getValue(), item);								        	
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
 							        if (drupalSessionCookie != null) {
 							          mCookieStore.addCookie(drupalSessionCookie);
 							          mServicesClient.setCookieStore(mCookieStore);        
 
 							          // TODO: change to debug - it's at error now simply for readability
 							          Log.e(TAG, "Using session cookie: " + drupalSessionCookie.toString());
 							        }
 							        else {
 							            Log.e(TAG, "No Stored Cookies to use: ");
 							        }   								
 									
 									//Log.d(TAG, "Posting entry " + item.toString());
 							        
 									drupalNodePut(item.toString(), packet.mQueuedAction, packet.mDrupalNid);									
 		
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
 
 		// Check to see of Janrain is supplying a drupal session cookie
 		org.apache.http.cookie.Cookie[] mSessionCookies;
 		mSessionCookies = responseHeaders.getCookies();
 		
 		for (Cookie cookie : mSessionCookies) {
         	System.out.println("Cookie! - " + cookie.toString());
         	System.out.println("Cokie Name - " + cookie.getName());
         	
         	if (cookie.getName().startsWith("SESS") || cookie.getName().startsWith("SSESS")) {
         		drupalSessionCookie = cookie;
         		Log.e(TAG, "saving session cookie: " + cookie.toString()); 
         	}			
 		}
 		
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
