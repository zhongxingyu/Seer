 package com.malcom.library.android.utils;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.provider.Settings.Secure;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 import com.malcom.library.android.MCMDefines;
 import com.malcom.library.android.module.core.MCMCoreAdapter;
 import com.malcom.library.android.utils.encoding.base64.Base64;
 import com.malcom.library.android.utils.io.IOUtils;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreConnectionPNames;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.UUID;
 
 
 /**
  * This class will hold utility functions related with Android.
  * 
  * @author Malcom Ventures, S.L.
  * @since  2012
  *
  */
 public class ToolBox {
 	
 	/** Http Method type for a request. */
 	public static enum HTTP_METHOD{POST,DELETE,GET};
 	
 	private static final String LINE_FEED = "\n";
 	
 	private static final String TAG = "Malcom Android ToolBox";
 
 	protected static final String PREFS_FILE = "device_id.xml";
 	
     protected static final String PREFS_DEVICE_ID = "device_id";
 	
     
     protected final static String SEPARATOR = ":";
 
     protected static final String MALCOM_HEADER_PREFIX = "x-mcm-";
 	
 	//--------------- APPLICATION RELATED -------------------------------------------------------------- 
 	  
 	/**
 	 * This method reads the info form the App. Manifest file.
 	 * 
 	 * @return
 	 */
 	public static boolean application_isAppInDebugMode(Context context){
 		boolean res=false;
 		
 		try{
 			PackageInfo info = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName, 0);
 			   
 			int flags=info.applicationInfo.flags;
 			if ((flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
 			    // development mode
 				res=true;
 			} else {
 			    // release mode
 			}
 		}catch(Exception e){
 			Log.e("IS_DEBUG_MODE:ERROR",e.getMessage(),e);
 		}
 		
 		return res;
 	}
 	
 	
 	// Net Related -----------------------------------------------------------------------------------------------------------------------------
 	
 	/**
 	 * Makes a Http operation.
 	 * 
 	 * This method set a parameters to the request that avoid being waiting 
 	 * for the server response or once connected, being waiting to receive 
 	 * the data.
 	 * 
 	 * @param method		Method type to execute. @See HTTP_METHOD.
 	 * @param url			Url of the request.
 	 * @param jsonData		The body content of the request (JSON). Can be null.
 	 * @param headers		The headers to include in the request.
 	 * @return The content of the request if there is one.
 	 * @throws Exception
 	 */
 	public static String net_httpclient_doAction(HTTP_METHOD method, String url, String jsonData, Map<String, String> headers) throws IOException{
     	String responseData = null;
 		
 		DefaultHttpClient httpclient = new DefaultHttpClient();
     	
     	// The time it takes to open TCP connection.
 		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, MCMCoreAdapter.CONNECTION_DEFAULT_TIMEOUT);
         // Timeout when server does not send data.
 		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, MCMCoreAdapter.CONNECTION_DEFAULT_DATA_RECEIVAL_TIMEOUT);
         // Some tuning that is not required for bit tests.
 		//httpclient.getParams().setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
 		httpclient.getParams().setParameter(CoreConnectionPNames.TCP_NODELAY, true);
     	
     	HttpRequestBase httpMethod = null;    	
     	switch(method){
 			case POST:
 				httpMethod = new HttpPost(url);
 				//Add the body to the request.
 		    	StringEntity se = new StringEntity(jsonData, "UTF-8");
 		    	((HttpPost)httpMethod).setEntity(se);
 				break;
 			case DELETE:
 				httpMethod = new HttpDelete(url);
 				break;
 			case GET:
 				httpMethod = new HttpGet(url);
 				break;
     	}
     	
     	//Add the headers to the request.
     	if(headers!=null){
 	    	for(String header:headers.keySet()){
 	    		httpMethod.setHeader(header, headers.get(header));
 	    	}
     	}
     	
     	HttpResponse response = httpclient.execute(httpMethod);
 
     	//Get the response body if there is one.
     	HttpEntity entity = response.getEntity();
     	if (entity != null) {
     	    InputStream instream = entity.getContent();
 
     	    responseData = IOUtils.convertStreamToString(instream);
             Log.d(MCMDefines.LOG_TAG, "HTTP OPERATION: Read from server - return: " + responseData);
     	}
     	
     	if (response.getStatusLine().getStatusCode() != 200) {
     		throw new IOException("Http operation "+method.name()+" failed with error code " +
     				response.getStatusLine().getStatusCode() + "("+ 
     				response.getStatusLine().getReasonPhrase() +")");
     	}
     	
     	return responseData;
     } 
 	
 	/**
 	  * Checks if there is network connectivity.
 	  * 
 	  * @param context
 	  * @return
 	  */
 	 public static boolean network_haveNetworkConnection(Context context) {
 		 boolean haveConnectedWifi = false;
 		 boolean haveConnectedMobile = false;
 
 		 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		 NetworkInfo[] netInfo = cm.getAllNetworkInfo();
 		 for (NetworkInfo ni : netInfo) {
 		     if (ni.getTypeName().equalsIgnoreCase("WIFI"))
 		         if (ni.isAvailable() && ni.isConnected())
 		             haveConnectedWifi = true;
 		     if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
 		         if (ni.isAvailable() && ni.isConnected())
 		             haveConnectedMobile = true;
 		 }
 		 return haveConnectedWifi || haveConnectedMobile;
 	 }
 
 	 /*
 	 public static boolean network_isOnline(Activity activity) {
 		 NetworkInfo info = ((ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
 		 if (info==null || !info.isConnected()) {
 			return false;
 		 }
 
 		 if (info.isRoaming()) {
            // here is the roaming option you can change it if you want to disable internet while roaming, just return false
            return true;
 		 }
 		 return true;
 	 }*/
 
 	
 	// Storage Related -----------------------------------------------------------------------------------------------------------------------------
 	
 	/**
 	 * Gets the application internal storage path.
 	 * 
 	 * @param context
 	 * @param file
 	 * @return
 	 */
 	 public static File storage_getAppInternalStorageFilePath(Context context, String file){
 		 String filePath = context.getFilesDir().getAbsolutePath();//returns current directory.
 		 return new File(filePath, file);
 	 }
 	
 	/**
 	 * This method copies the input to the specified output.
 	 * 
 	 * @param is	Input source
 	 * @param os	Output destiny
 	 * @return		Total bytes read
 	 */
     public static int storage_copyStream(InputStream is, OutputStream os, int buffer_size) throws IOException{
     	int readbytes=0;
     	
         if(buffer_size<=0){
         	buffer_size=1024;
         }
         
         try{
             byte[] bytes=new byte[buffer_size];
             for(;;){
               int count=is.read(bytes, 0, buffer_size);
               if(count==-1)
                   break;
               os.write(bytes, 0, count);
               readbytes+=count;
             }
         }catch(Exception e){
         	throw new IOException("Failed to save data ("+e.getMessage()+").",e);
         }
         
         return readbytes;
     }
 		
 	/**
 	 * Saves data to the application internal folder.
 	 * 
 	 * @param context
 	 * @param fileName
 	 * @param data
 	 * @throws Exception
 	 */
 	public static synchronized void storage_storeDataInInternalStorage(Context context, String fileName, byte[] data) throws Exception{
 		 try {
 			 /* We have to use the openFileOutput()-method
 		      * the ActivityContext provides, to
 		      * protect your file from others and
 		      * This is done for security-reasons.
 		      * We chose MODE_WORLD_READABLE, because
 		      *  we have nothing to hide in our file */             
 		      FileOutputStream fOut = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
 			   
 		      // Write the string to the file
 		      fOut.write(data);
 	
 		      /* ensure that everything is really written out and close */
 		      fOut.flush();
 		      fOut.close();
 		      
 		 } catch (Exception e) {
 			  throw new Exception("Error saving data to '" + fileName + "' (internal storage) : "+ e.getMessage(),e);
 		 } 
 	 }
 	 
 	/**
 	 * Reads data from the application internal storage data folder.
 	 * 
 	 * @param context
 	 * @param fileName
 	 * @return
 	 * @throws Exception
 	 */
 	public static byte[] storage_readDataFromInternalStorage(Context context, String fileName) throws Exception{
 		FileInputStream fIn;
 		
 		try {
 			fIn = context.openFileInput(fileName);
 			
 			byte[] buffer = new byte[fIn.available()];
 			 
 			fIn.read(buffer);
 			fIn.close();
 			 
 			return buffer;
 			
 		} catch (Exception e) {
 			throw new Exception("Error reading data '" + fileName + "' (internal storage) : "+ e.getMessage(),e);
 		}
 	 }
 	
 	/**
 	 * Deletes a file from the application internal storage private folder.
 	 * 
 	 * @param context
 	 * @param fileName
 	 * @throws Exception
 	 */
 	public static void storage_deleteDataFromInternalStorage(Context context, String fileName) throws Exception{
 			
 		 try {
 			context.deleteFile(fileName);				
 		 } catch (Exception e) {
 			throw new Exception("Error deleting data '" + fileName + "' (internal storage) : "+ e.getMessage(),e);
 		 }
 	 }
 	 
 	 /**
 	  * Checks if a file exists in the application internal private data folder.
 	  * 
 	  * @param context
 	  * @param fileName
 	  * @return
 	  */
 	 public static boolean storage_checkIfFileExistsInInternalStorage(Context context, String fileName){
 		
 			try {
 				context.openFileInput(fileName);
 				
 				return true;
 			} catch (FileNotFoundException e) {
 				return false;
 			}
 	 }
 	 
 	 
 	 // Media Related -----------------------------------------------------------------------------------------------------------------------------
 	 
 	 /**
 	  * Loads a Bitmap image form the internal storage.
 	  * 
 	  * @param context
 	  * @param fileName
 	  * @return
 	  * @throws Exception
 	  */
 	 public static Bitmap media_loadBitmapFromInternalStorage(Context context, String fileName) throws Exception{
 		 
 		 try{
 			 FileInputStream is = context.openFileInput(fileName);
 			 Bitmap b = BitmapFactory.decodeStream(is);
 			 
 			 return b;
 		 } catch (Exception e) {			 
 			 throw new Exception("Error reading data '" + fileName + "' (internal storage) : "+ e.getMessage(),e);
 		 }
 	 }
 	 
 	 
 	 // Device Related -----------------------------------------------------------------------------------------------------------------------------
 	 
 	 /**
 	 * Get the device Id (an Hexadecimal unique value of 64 bit)
 	 * @param context
 	 * @return
 	 */
 	public static String device_getId(Context context){
 		
 		final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
         final String id = prefs.getString(PREFS_DEVICE_ID, null );
         
         UUID uuid = null;
         
         if (id != null) {
             // Usar esta id de las guardadas en persistencia
             uuid = UUID.fromString(id);
 
         } else {
         	 final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
 
             // Usamos el Android ID a no ser que sea uno conocido por fallar o no se pueda, en cuyo caso generamos un UUID
              try {
                  if (!"9774d56d682e549c".equals(androidId)) {
                      uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                  } else {
                      final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
                      uuid = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                  }
              } catch (UnsupportedEncodingException e) {
                  throw new RuntimeException(e);
 
              }
              // Guardamos en persistencia
              prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString() ).commit();
         }
     
         String udid = uuid.toString();
         		
         try {
 			udid = md5_calculateMD5(uuid);
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
 		return udid;
 	}
 	
 	
 	// Crypto Related -----------------------------------------------------------------------------------------------------------------------------
 	
 	/**
 	 * Calculates the MD5 of the spcified content.
 	 * 
 	 * @param contentToEncode
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 */
 	public static String md5_calculateMD5(String contentToEncode) throws NoSuchAlgorithmException {
 		  
 		MessageDigest digest = MessageDigest.getInstance("MD5");
 		digest.update(contentToEncode.getBytes());
 		String result = new String(Base64.encode(digest.digest()));
 		return result;	
 	} 
 	
 	/**
 	 * Calculates the MD5 of the spcified content.
 	 * 
 	 * @param contentToEncode
 	 * @return
 	 * @throws NoSuchAlgorithmException
 	 */
 	public static String md5_calculateMD5(UUID contentToEncode) throws NoSuchAlgorithmException {
 		  
 		MessageDigest digest = MessageDigest.getInstance("MD5");
 		digest.update(contentToEncode.toString().getBytes());
 		String result = new String(Base64.encode(digest.digest()));
 		return result;	
 	} 
     
 	/**
 	 * Gets the data to be signed in one string.
 	 * 	
 	 * @param headers
 	 * @param contentType
 	 * @param date
 	 * @param verb
 	 * @param resource
 	 * @param contentMd5
 	 * @return
 	 */
     /*public static String deliveries_getDataToSign(String headers, String contentType, String date, String verb, String resource,
 	        String contentMd5) {
     	
     	System.out.println("____________________headers: "+headers);
     	System.out.println("____________________contentType: "+contentType);
     	System.out.println("____________________date: "+date);
     	System.out.println("____________________verb: "+verb);
     	System.out.println("____________________resource: "+resource);
     	System.out.println("____________________contentMd5: "+contentMd5);
 		
 		StringBuilder result = new StringBuilder();
 		if (contentMd5 == null)
 			contentMd5 = "";
 		
 		if (contentType == null) 
 			contentType = "";
 		
 		if (date == null)		
 			date = "";
 		
 		if (headers == null)
 			headers = LINE_FEED;
 		
 
 		result.append(verb).append(LINE_FEED)
 				.append(contentMd5).append(LINE_FEED)
 				.append(contentType).append(LINE_FEED)
 				.append(date).append(LINE_FEED)
 				.append(headers)
 				.append(resource);
 		
 		System.out.println("__________________result: "+result);
         
 		return result.toString();
 	}*/
 	public static String deliveries_getDataToSign(String headers, String contentType, String date, String verb, String resource, String contentMd5) {
 		StringBuilder result = new StringBuilder();
 				
 		if (contentMd5 == null) {
 			
 			 contentMd5 = "";
 			 
 		}
 		if (contentType == null) {
 			 
 			contentType = "";
 		
 		}
 		
 		if (date == null) {
 			 
 			date = "";
 		
 		}
 		
 		if (headers == null) {
 			 
 			headers = LINE_FEED;
 			
 		}
 
 		result.append(verb);
 		result.append(System.getProperty("line.separator"));
 		result.append(contentMd5);
 		result.append(System.getProperty("line.separator"));
 		result.append(contentType);
 		result.append(System.getProperty("line.separator"));
 		result.append(date);
 		result.append(System.getProperty("line.separator"));
 		result.append(headers);
 		result.append(resource);
 		
 	//	System.out.println("___________________________________: "+result.toString());
 
 		return result.toString();
 		
 	}
 	
 	public static String getCanonicalizedMalcomHeaders(Map<String, String> headers)
 	{
 		StringBuilder canonicalizedMalcomHeaders = new StringBuilder();
 
 		for (String key : new ArrayList<String>(headers.keySet()))
 		{
 			if (key.startsWith(MALCOM_HEADER_PREFIX))
 			{
 				String headerValue = headers.get(key);
 
 				canonicalizedMalcomHeaders.append(key);
 				canonicalizedMalcomHeaders.append(SEPARATOR);
 				canonicalizedMalcomHeaders.append(headerValue);
 				canonicalizedMalcomHeaders.append(LINE_FEED);
 			}
 		}
 
 		if (canonicalizedMalcomHeaders.length() == 0)
 			return null;
 
 		return canonicalizedMalcomHeaders.toString();
 	}
 
 	 
 }
