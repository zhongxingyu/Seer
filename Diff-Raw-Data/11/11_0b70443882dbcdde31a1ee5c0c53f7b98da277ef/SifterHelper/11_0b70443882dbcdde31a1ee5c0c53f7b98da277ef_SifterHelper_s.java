 package com.SifterReader.android;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.res.Resources.NotFoundException;
 
 public class SifterHelper {
 	private final Context mContext;
 	private String mAccessKey;
 
 	public SifterHelper(Context context, String accessKey) {
 		mContext = context;
 		mAccessKey = accessKey;
 	}
 	
 	public SifterHelper(Context context) {
 		mContext = context;
 		getKey();
 	}
 	
 	public URLConnection getSifterConnection(String sifterURL) {
 		URLConnection sifterConnection = null;
 		try {
 			// create URL object to SifterAPI
 			URL sifter = new URL(sifterURL); // throws MalformedURLException
 			// open connection to SifterAPI
 			sifterConnection = sifter.openConnection(); // throws IOException
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} // catch errors, do nothing, return null, use onConnectionError
 		return sifterConnection;
 	}
 	
 	public InputStream getSifterInputStream(URLConnection sifterConnection) {
 		// send header requests
 		sifterConnection.setRequestProperty(SifterReader.X_SIFTER_TOKEN, mAccessKey);
 		sifterConnection.addRequestProperty(SifterReader.HEADER_REQUEST_ACCEPT, SifterReader.APPLICATION_JSON);
 
 		InputStream is = null;
 		try {
 			is = sifterConnection.getInputStream(); // throws FileNotFound, IOException
 		} catch (IOException e) {
 			e.printStackTrace();
 			// also catches FileNotFoundException: invalid domain
 			// IOException: invalid access key
 			HttpURLConnection httpSifterConnection = (HttpURLConnection)sifterConnection;
 			is = httpSifterConnection.getErrorStream();
 		} // catch errors, return error stream, could be null if none found
 		return is;
 	}
 	
 	public JSONObject onConnectionError() throws JSONException, NotFoundException {
 		JSONObject connectionError = new JSONObject();
 			connectionError.put(SifterReader.LOGIN_ERROR, // throws JSONException
 					mContext.getResources().getString(R.string.connection_error)); // throws NotFoundException
 			connectionError.put(SifterReader.LOGIN_DETAIL,
 					mContext.getResources().getString(R.string.connection_error_msg));
 		return connectionError;
 	}
 	
	public void resetKey(String accessKey) {
		mAccessKey = accessKey; // TODO SifterHelper should get keys from key_file
	}
	
 	public void getKey(){
 		File keyFile = mContext.getFileStreamPath(SifterReader.KEY_FILE);
 //		if (!keyFile.exists()) {
 //			if (onMissingToken())
 //				loginKeys();
 //			return;
 //		}
 		
 //		boolean fileReadError = false;
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(keyFile));
 			String inputLine;
 			StringBuilder x = new StringBuilder();
 			while ((inputLine = in.readLine()) != null) {
 				x.append(inputLine);
 			}
 			in.close();
 			JSONObject loginKeys = new JSONObject(x.toString());
 			mAccessKey = loginKeys.getString(SifterReader.ACCESS_KEY);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 //			fileReadError = true;
 		} catch (IOException e) {
 			e.printStackTrace();
 //			fileReadError = true;
 		} catch (JSONException e) {
 			e.printStackTrace();
 //			fileReadError = true;
 		}
 //		if (fileReadError || mDomain.isEmpty() || mAccessKey.isEmpty()) {
 //			if (onMissingToken())
 //				loginKeys();
 //			return;
 //		}
 	}
 	
 	public JSONObject getSifterJSONObject(URLConnection sifterConnection) throws JSONException,NotFoundException,IOException {
 		JSONObject sifterJSONObject = new JSONObject();
 		
 		InputStream sifterInputStream = getSifterInputStream(sifterConnection);
 		if (sifterInputStream == null) { // null means MalformedURLException or IOException
 			return onConnectionError(); // throws JSONException, NotFoundException
 		}
 		
 		BufferedReader in = new BufferedReader(new InputStreamReader(sifterInputStream));
 		String inputLine;
 		StringBuilder x = new StringBuilder();
 		try {
 			while ((inputLine = in.readLine()) != null)
 				x.append(inputLine);
 		} catch (IOException e) {
 			in.close();sifterInputStream.close();
 			throw e;
 		} // catch error and close buffered reader
 		in.close();sifterInputStream.close();
 		// sifterInputStream must stay open for buffered reader 
 		sifterJSONObject = new JSONObject(x.toString()); // throws JSONException
 		return sifterJSONObject;
 	}
 
 }
