 /**
  * Orchestrates sending all collected data to the server.
  */
 package edu.wpi.cs.peds.hmn.stats;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.util.Log;
 import edu.wpi.cs.peds.hmn.log.HmnLog;
 import edu.wpi.cs.peds.hmn.stats.apps.GlobalAppList;
 import edu.wpi.cs.peds.hmn.app.SleekAndroidActivity;
 /**
  * POSTSs all collected data to the specified URL as JSON, and buffers data if
  * POSTing fails.
  * 
  * @author Austin Noto-Moniz, austinnoto@wpi.edu
  * @author Zhenhao Lei, zlei@wpi.edu
  */
 public class JSONSender extends AsyncTask<String,Object,Boolean>
 {
 	// Allows control over output for debugging purposes.
 	private static final boolean sendJSON = true;
 	private static final boolean printJSON = false;
 	private static final boolean toFile = false;
 	
 	
 	private static final String scriptURL = "http://hmn-research.cs.wpi.edu/cgi-bin/mobileData.cgi";
 	private static List<JSONObject> jsonBuffer = new ArrayList<JSONObject>();
 	private static final String stashFolder = "buffer";
 	
 	/**
 	 * Helper function for storing the provided JSON object according to the provided folder and filename.
 	 * 
 	 * @param bufferDir the folder the file should be created in
 	 * @param filename the name of the file to create
 	 * @param jsonObj the JSON object to be stored in the file
 	 * @return returns true if the stash operation succeeded; otherwise, false
 	 */
 	private static boolean stashMessage(File bufferDir, String filename, JSONObject jsonObj)
 	{
 		try {
 			FileOutputStream bufferedData = new FileOutputStream(new File(bufferDir,filename));
 			String jsonStr = jsonObj.toString();
 			bufferedData.write(jsonStr.getBytes());
 			bufferedData.close();
 			Log.i(HmnLog.HMN_LOG_TAG,"Message successfully stashed");
 			return true;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			Log.e(HmnLog.HMN_LOG_TAG, "There was an error stashing unsent data, so it will be discarded.");
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e(HmnLog.HMN_LOG_TAG, "There was an error stashing unsent data, so it will be discarded.");
 			return false;
 		}
 	}
 	
 	/**
 	 * Writes the entire buffer out to file such that it may be loaded later.
 	 * 
 	 * @param context for accessing the file system relative to this app
 	 */
 	public static void stashBuffer(Context context)
 	{
 		File bufferDir = context.getDir(stashFolder,Context.MODE_PRIVATE);
 		
 		int fileCount = 0;
 		for (int k = 0; k<jsonBuffer.size(); k++, fileCount++)
 			stashMessage(bufferDir,String.valueOf(fileCount),jsonBuffer.get(k));
 		stashMessage(bufferDir,String.valueOf(fileCount),getJSON());
 	}
 	
 	/**
 	 * Reads an entire file into a buffer and outputs it as a string.
 	 * 
 	 * @param fileStream the stream object representing the file to be read
 	 * @param fileLen the total number of characters to read from the stream
 	 * @return the entire contents of the file; should be fileLen characters long
 	 */
 	private static String readFile(FileInputStream fileStream, int fileLen)
 	{
 		StringBuffer strBuffer = new StringBuffer();
 		int bytesRead = 0;
 		while (bytesRead < fileLen)
 		{
 			byte[] buffer = new byte[fileLen];
 			try {
 				bytesRead += fileStream.read(buffer);
 			} catch (IOException e) {
 				e.printStackTrace();
 				Log.e(HmnLog.HMN_LOG_TAG, "There was an error reading stashed data, so it will be ignored.");
 			}
 			strBuffer.append(new String(buffer));
 		}
 		return strBuffer.toString();
 	}
 	
 	/**
 	 * Loads any stashed JSON and attempts to POST it to the server.
 	 * 
 	 * @param context for accessing the file system relative to this app.
 	 */
 	public static void loadAndSendBuffer(Context context)
 	{
 		File bufferDir = context.getDir(stashFolder,Context.MODE_PRIVATE);
 		
 		for (File file : bufferDir.listFiles())
 		{
 			System.out.println(file.getName());
 			FileInputStream fileStream = null;
 			try {
 				fileStream = new FileInputStream(file);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				Log.e(HmnLog.HMN_LOG_TAG, "There was an error reading stashed data, so it will be ignored.");
 				continue;
 			}
 			
 			int fileLen = (int)file.length();
 			String jsonStr = readFile(fileStream,fileLen);
 			file.delete();
 			
 			if (jsonStr != null)
 			{
 				JSONObject json = null;
 				try {
 					json = new JSONObject(jsonStr.toString());
 				} catch (JSONException e) {
 					e.printStackTrace();
 					Log.e(HmnLog.HMN_LOG_TAG, "Some stashed data does not appear to be in JSON, so it will be ignored.");
 					continue;
 				}
 				
 				addToBuffer(json);
 			}
 		}
 		transmitData();
 	}
 	
 	/**
 	 * Converts all collected data into JSON, spins off a separate sender thread
 	 * (as required by Android), and POSTs the data to the specified URL. If the
 	 * URL cannot be reached, or there is an issue POSTing, the message is
 	 * buffered for later transmission.
 	 * 
 	 * @return true if all message transmissions were successful; false otherwise
 	 */
 	public static boolean post()
 	{
 		JSONObject json = getJSON();
 		if (json == null)
 			return false;
 		
 		return transmitData(json);
 	}
 	
 	/**
 	 * Adds the given JSON object to the buffer, then attempts to send each message in the buffer.
 	 *  
 	 * @param json the data to store and send
 	 * @return true if all transmissions succeed. that is, if each message in the buffer has been successfully sent; false otherwise
 	 */
 	private static boolean transmitData(JSONObject json)
 	{
 		addToBuffer(json);
 		return transmitData();
 	}
 	
 	/**
 	 * Attempts to transmit each message in the buffer to the defined URI.
 	 * 
 	 * @return true if each message in the buffer successfully sent; false otherwise 
 	 */
 	private static boolean transmitData()
 	{
	    Log.e(HmnLog.HMN_LOG_TAG, jsonBuffer.toString());
 		Log.i(HmnLog.HMN_LOG_TAG,"Messages to send: " + jsonBuffer.size());
 		boolean success = true;
 	//only send data through WIFI
 		if(!SleekAndroidActivity.wifiConnected){
 			success = false;
 			Log.i(HmnLog.HMN_LOG_TAG,"Not on WIFI! Message NOT successfully sent.");
 			return success;
 		}
 		int counter = 0;
 
 		for (JSONObject jsonObj : new ArrayList<JSONObject>(jsonBuffer))
 		{
 			Log.i(HmnLog.HMN_LOG_TAG,"Sending JSON message");
 			
 			success = sendMessage(jsonObj);
 			
 			if (success)
 			{
 				Log.i(HmnLog.HMN_LOG_TAG,"Message successfully sent");
 				jsonBuffer.remove(jsonObj);
 				counter++;
 			}
 			else
 			{
 				Log.i(HmnLog.HMN_LOG_TAG,"Message NOT successfully sent");
 				break;
 			}
 		}
 
 	    Log.i(HmnLog.HMN_LOG_TAG,"Sending complete.");
 		Log.i(HmnLog.HMN_LOG_TAG,"Messages successfully sent: " + counter);
 		Log.i(HmnLog.HMN_LOG_TAG,"Messages buffered: " + jsonBuffer.size());
 		
 		if (!success)
 			Log.e(HmnLog.HMN_LOG_TAG, "Data transmission was unsucessful.");
 		return success;
 	}
 
 	
 	/**
 	 * Converts all collected data into a JSON object.
 	 * 
 	 * @return all collected data as JSON, or null if an error occurs.
 	 */
 	private static JSONObject getJSON()
 	{
 		try
 		{
 			JSONObject info = GlobalDataCollector.getInstance().toJSON();
 			info.put("apps",GlobalAppList.getInstance().toJSON());
 			
 			GlobalDataCollector.getInstance().clearStats();
 			GlobalAppList.getInstance().clearStats();
 			
 			return info;
 		}
 		catch (JSONException e)
 		{
 			Log.e(HmnLog.HMN_LOG_TAG,"An error occurred while preparing to transmit data.",e);
 			return null;
 		}
 	}
 	
 	/**
 	 * Appends a message to the buffer.
 	 * 
 	 * @param json the message to append
 	 */
 	private static void addToBuffer(JSONObject json)
 	{
 		jsonBuffer.add(json);
 	}
 	
 	/**
 	 * Sends a single JSON message.
 	 * 
 	 * @param jsonObj the message to send.
 	 * @return true if sending this message succeeded, false otherwise.
 	 */
 	private static boolean sendMessage(JSONObject jsonObj)
 	{
 		if (sendJSON)
 		{
 			JSONSender sendJSONTask = (JSONSender)new JSONSender().execute(jsonObj.toString());
 			try {
 				boolean result = sendJSONTask.get();
 				
 				if (result)
 					debuggingOutput(jsonObj);
 				
 				return result;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		else
 		{
 			debuggingOutput(jsonObj);
 			return true;
 		}
 	}
 	
 	/**
 	 * The entry point used by {@link AsyncTask#execute(Object...) execute}
 	 */
 	@Override
 	protected Boolean doInBackground(String...strs) {
         return sendJSON(strs[0]);
     }
 	
 	/**
 	 * Performs the actual transmission of the provided JSON string.
 	 * 
 	 * @param jsonStr the JSON string to be sent
 	 * @return true if the transmission succeeded, and false otherwise
 	 */
 	private boolean sendJSON(String jsonStr)
 	{
 		HttpPost httppost = new HttpPost(scriptURL);
 
 		try {
 			httppost.setEntity(new StringEntity(jsonStr));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			Log.e(HmnLog.HMN_LOG_TAG,e.getMessage());
 		}
 
 	    httppost.setHeader("Accept", "application/json");
 	    httppost.setHeader("Content-Type", "application/json");
 
 	    HttpResponse response = null;
 	    try {
 			response = new DefaultHttpClient().execute(httppost);
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e(HmnLog.HMN_LOG_TAG,e.getMessage());
 		}
 	    
 	    if (response == null)
 	    	Log.e(HmnLog.HMN_LOG_TAG,"Unkown error occurred");
 	    else if (response.getStatusLine().getStatusCode() >= 300)
 	    	Log.e(HmnLog.HMN_LOG_TAG,"HTTP Error Code: " + response.getStatusLine().getStatusCode());
 		
 	    return response != null && response.getStatusLine().getStatusCode() < 300;
 	}
 	
 	
 	/**
 	 * A debugging method. Outputs the JSON that is being sent, either using
 	 * stdout (LogCat) or written to a file in the phone's Downloads folder.
 	 * 
 	 * @param json the josn that's being sent
 	 */
 	private static void debuggingOutput(JSONObject json)
 	{
 		if (printJSON)
 		{
 			try {
 				System.out.println(json.toString(4));
	    	Log.e(HmnLog.HMN_LOG_TAG,"!!!!!!!!!!!!!!!!!!!"+ json.toString(4));
 			} catch (JSONException e) {}
 		}
 		if (toFile)
 		{
 			String filename = String.valueOf(new Date().getTime());
 			File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),filename);
 			FileOutputStream fileOut;
 			try {
 				
 				fileOut = new FileOutputStream(file);
 				fileOut.write(json.toString(4).getBytes());
 				fileOut.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
