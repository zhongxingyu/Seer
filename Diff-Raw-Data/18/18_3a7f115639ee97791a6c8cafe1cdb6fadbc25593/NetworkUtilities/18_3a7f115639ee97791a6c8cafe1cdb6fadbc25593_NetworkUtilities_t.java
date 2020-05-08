 package au.id.teda.broadband.usage.util;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import au.id.teda.broadband.usage.R;
 import au.id.teda.broadband.usage.authenticator.AccountAuthenticator;
 import au.id.teda.broadband.usage.database.DailyDataDatabaseAdapter;
 import au.id.teda.broadband.usage.helper.AccountInfoHelper;
 import au.id.teda.broadband.usage.helper.AccountStatusHelper;
 import au.id.teda.broadband.usage.helper.NotificationHelper;
 import au.id.teda.broadband.usage.parser.AccountInfoParser;
 import au.id.teda.broadband.usage.parser.AccountInfoParser.AccountInfo;
 import au.id.teda.broadband.usage.parser.AccountStatusParser;
 import au.id.teda.broadband.usage.parser.AccountStatusParser.AccountStatus;
 import au.id.teda.broadband.usage.parser.ErrorParser;
 import au.id.teda.broadband.usage.parser.VolumeUsageParser;
 
 
 /**
  * Class for downloading XML data.
  * Authenticate
  * Account Information
  * Account Status
  * Volume Usage
  * 
  * @author iteda
  *
  */
 public class NetworkUtilities {
 	
 	private static final String DEBUG_TAG = "bbusage";
 
 	/** Activity context **/
     private Context mContext;
     
     /** Activity shared preferences **/
     SharedPreferences sharedPrefs;
     
     private AccountAuthenticator mAccountAuthenticator;
     
     private static String mUsername;
     
     // Error texts from XML
     private static final String AUTHENTICATION_FAILURE = "Authentication failure";
 	
     public final static String PREF_LAST_SYNC_KEY = "last_sync_timestamp";
 
     /**
      * Class constructor
      * @param context
      */
     public NetworkUtilities(Context context) {
     	// Set context based on activity context passed to constructor
     	mContext = context;
      
     	mAccountAuthenticator = new AccountAuthenticator(mContext);
 
     }
 
 	
     /**
      * Setup progress dialog and then start download task
      */
     public void syncXmlData(){
     	
     	mUsername = mAccountAuthenticator.getUsername();
     	
     	SyncXmlThread mSyncThread = new SyncXmlThread();
     	mSyncThread.run();
     }
     
     /**
      * Get xml buffered input stream
      * @return
      * @throws IOException
      */
     private UnclosableBufferedInputStream getXmlBufferedInputStream() throws IOException {
        
     	// Get input stream
         InputStream inputStream = getUrlInputStream(urlBuilder(mUsername, mAccountAuthenticator.getPassword()));
     	
     	//InputStream inputStream = mContext.getResources().openRawResource(R.raw.naked_dsl_home_5);
     	
         UnclosableBufferedInputStream  bis = new UnclosableBufferedInputStream (inputStream);
     	
     	return bis;
     }
     
     /**
      * Url builder for downloading XML
      * @param username
      * @param password
      * @return
      */
     private String urlBuilder(String username, String password){
     	String urlString = "https://toolbox.iinet.net.au/cgi-bin/new/volume_usage_xml.cgi?" +
 				"username=" + username + 
 				"&action=login" +
 				"&password=" + password;
     	return urlString;
     }
     
     /**
      * Get URL input stream for string representation of URL
      * @param urlString
      * @return
      * @throws IOException
      */
     private InputStream getUrlInputStream(String urlString) throws IOException {
         URL url = new URL(urlString);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setReadTimeout(10000 /* milliseconds */);
         conn.setConnectTimeout(15000 /* milliseconds */);
         conn.setRequestMethod("GET");
         conn.setDoInput(true);
         // Starts the query
         conn.connect();
         InputStream stream = conn.getInputStream();
         return stream;
     }
     
     /**
      * Parse XML based on username and password, for authentication failure error.
      * 
      * @param username
      * @param password
      * @return boolean false if error tag found in XML parse
      */
     public Boolean authenticate(String username, String password){
     	String errorString = null;
     	ErrorParser mErrorParser = new ErrorParser();
     	
     	try {
     		InputStream urlStream = getUrlInputStream(urlBuilder(username, password));
 			errorString = mErrorParser.parse(urlStream);
 		} catch (XmlPullParserException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
     	
     	if (errorString != null 
     			&& errorString.equals(AUTHENTICATION_FAILURE)){
     		return false;
     	} else {
     		return true;
     	}
     }
     
     /**
      * Parse XML input stream for account information and then set shared prefs
      * @param stream
      */
     private void setAccountInfo(UnclosableBufferedInputStream stream) {
        	AccountInfoParser mAccountInfoParser = new AccountInfoParser();
     	List<AccountInfo> account = null;
 
         try {
         	account = mAccountInfoParser.parse(stream);
         } catch (XmlPullParserException e) {
 			Log.e(DEBUG_TAG, "XmlPullParserException: " + e);
 		} catch (IOException e) {
 			Log.e(DEBUG_TAG, "IOException: " + e);
         }
         
         AccountInfoHelper mAccountInfoHelper = new AccountInfoHelper(mContext);
         
         
         String plan = null;
         String product = null;
     	long offpeakStartTime;
     	long offpeakEndTime;
     	long peakQuota;
     	long offpeakQuota;
         for (AccountInfo accountInfo : account) {
         	
         	plan = accountInfo.plan;
         	product = accountInfo.product;
         	offpeakStartTime = accountInfo.offpeakStartTime;
         	offpeakEndTime = accountInfo.offpeakEndTime;
         	peakQuota = accountInfo.offpeakQuota;
         	offpeakQuota = accountInfo.offpeakQuota;
         	
         	mAccountInfoHelper.setAccountInfo(mUsername, plan, product, offpeakStartTime, offpeakEndTime, peakQuota, offpeakQuota);
         	
         }
                  
     }
     
     /**
      * Parse XML input stream for account status and then set shared prefs
      * @param stream
      */
     private void setAccountStatus(UnclosableBufferedInputStream stream){
     	AccountStatusParser mAccountStatusParser= new AccountStatusParser();
     	List<AccountStatus> status = null;
     	
         try {
         	status = mAccountStatusParser.parse(stream);
         } catch (XmlPullParserException e) {
 			Log.e(DEBUG_TAG, "XmlPullParserException: " + e);
 		} catch (IOException e) {
 			Log.e(DEBUG_TAG, "IOException: " + e);
         }
         
         AccountStatusHelper mAccountStatusHelper = new AccountStatusHelper(mContext);
         
         long quotaResetDate;
         long quotaStartDate;
 	    long peakDataUsed;
 	    long offpeakDataUsed;
 	    long uploadsDataUsed;
 	    long freezoneDataUsed;
 	    int peakSpeed;
 		boolean peakIsShaped;
 		int offpeakSpeed;
 		boolean offpeakIsShaped;
 		String ipAddress;
 		long upTimeDate;
 		
         for (AccountStatus accountStatus : status) {
         	quotaResetDate = accountStatus.quotaResetDate;
         	quotaStartDate = accountStatus.quotaStartDate;
         	peakDataUsed = accountStatus.peakDataUsed;
         	offpeakDataUsed = accountStatus.offpeakDataUsed;
         	uploadsDataUsed = accountStatus.uploadsDataUsed;
         	freezoneDataUsed = accountStatus.freezoneDataUsed;
         	peakSpeed = accountStatus.peakSpeed;
         	peakIsShaped = accountStatus.peakIsShaped;
         	offpeakSpeed = accountStatus.offpeakSpeed;
         	offpeakIsShaped = accountStatus.offpeakIsShaped;
         	ipAddress = accountStatus.ipAddress;
         	upTimeDate = accountStatus.upTimeDate;
         	
         	mAccountStatusHelper.setAccoutStatus(mUsername
         			, quotaResetDate, quotaStartDate
         			, peakDataUsed, peakIsShaped, peakSpeed
         			, offpeakDataUsed, offpeakIsShaped, offpeakSpeed
         			, uploadsDataUsed, freezoneDataUsed
         			, ipAddress, upTimeDate);
 
         }
     	
     }
     
     /**
      * Parse XML input stream for volume usage and then added to database
      * @param stream
      */
     private void setVolumeUsage(UnclosableBufferedInputStream stream) {
     	
     	VolumeUsageParser mVolumeUsageParser = new VolumeUsageParser();
     	List<DailyVolumeUsage> usage = null;
       
         try {
         	usage = mVolumeUsageParser.parse(stream);
         } catch (XmlPullParserException e) {
 			Log.e(DEBUG_TAG, "XmlPullParserException: " + e);
 		} catch (IOException e) {
 			Log.e(DEBUG_TAG, "IOException: " + e);
         }
         
         // Initiate database
         DailyDataDatabaseAdapter mDatabase = new DailyDataDatabaseAdapter(mContext);
         mDatabase.open();
         
         for (DailyVolumeUsage volumeUsage : usage) {
         	Long day = volumeUsage.day;
         	String month = volumeUsage.month;
         	Long peak = volumeUsage.peak;
         	Long offpeak = volumeUsage.offpeak;
         	Long uploads = volumeUsage.uploads;
         	Long freezone = volumeUsage.freezone;
         	
         	Calendar mDate = Calendar.getInstance();
         	mDate.setTimeInMillis(day);
         	
        	Log.d(DEBUG_TAG, "NU Date:" + mDate.getTime() + " Month:" + month);
         	
         	mDatabase.addReplaceEntry(mUsername, day, month, peak, offpeak, uploads, freezone);
         }
         
         mDatabase.close();
                  
     }
     
     private class SyncXmlThread implements Runnable{
 
 		@Override
 		public void run() {
 			String start = mContext.getString(R.string.sync_broadcast_start);
     		sendBroadcastMessage(start);
     		
     		UnclosableBufferedInputStream stream;
 			try {
 				stream = getXmlBufferedInputStream();
 				setAccountInfo(stream);
 				setAccountStatus(stream);
 				setVolumeUsage(stream);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			String complete = mContext.getString(R.string.sync_broadcast_complete);
     		sendBroadcastMessage(complete);
     		
     		NotificationHelper mNotificationHelper = new NotificationHelper(mContext);
     		mNotificationHelper.checkStatus();
     		
     		setSyncTimeStamp();
 			
 		}
     	
     }
     
     private void sendBroadcastMessage(String msg){
     	String BROADCAST = mContext.getString(R.string.sync_broadcast_action);
     	String MESSAGE = mContext.getString(R.string.sync_broadcast_message);
     	
     	Intent i = new Intent(BROADCAST);
     	i.putExtra(MESSAGE, msg);
     	mContext.sendBroadcast(i);
     }
     
     private void setSyncTimeStamp(){
     	
     	SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
         SharedPreferences.Editor mEditor = mSettings.edit();
     	
         // Get current date/time
         Calendar now = Calendar.getInstance();
         long nowInMillis = now.getTimeInMillis();
        
         // Put into shared prefferences
         mEditor.putLong(PREF_LAST_SYNC_KEY, nowInMillis);
         mEditor.commit();
     }
     
     /**
      * Class of buffer input stream
      * @author iteda
      *
      */
     private class UnclosableBufferedInputStream extends BufferedInputStream {
 
         public UnclosableBufferedInputStream(InputStream in) {
         	super(in);
         	super.mark(Integer.MAX_VALUE);
         }
 
         @Override
         public void close() throws IOException {
         	super.reset();
         }
     }
     
 }
