 package com.cmozie.classes;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import webConnections.WebStuff;
 
 import com.cmozie.Libz.FileStuff;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class ZipcodeService extends IntentService {
 	public static final String MESSENGER_KEY = "messenger";
 	public static final String enteredZipcode = "zipcode";
 
 
 	public ZipcodeService() {
 		super("ZipcodeService");
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		
 
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		// TODO Auto-generated method stub
 		Log.i("ONHandleIntent", "Started");
 		//m_file = FileStuff.getInstance();
 		
 		Bundle extras = intent.getExtras();
 		Messenger messenger = (Messenger) extras.get(MESSENGER_KEY);
 		String zips = extras.getString(enteredZipcode);
 	
 		
 		
 		//this is the base url of the api
 				String baseURL = "http://zipfeeder.us/zip?";
 				
 				//key needed to use api
 				String key = "key=EN4GbNMq";
 				//this empty string accepts an empty string which will be for zipcodes entered
 				String qs = "";
 				try{
 				qs = URLEncoder.encode(zips, "UTF-8");
 					
 				}catch (Exception e) {
 					
 					//if an error in the api show the bad url alert
 					
 				
 					Log.e("Bad URL","Encoding Problem");
 					qs = "";
 				}
 				
 				//creates finalURL as a URL
 				URL UrlResult;
				String queryReply;
 				try{
 					//sets the final url to the base plus the api key with the string parameter needed for search as well as the empty string that recieves a zipcode.
 					UrlResult = new URL (baseURL + key + "&zips=" + qs);
 					
 					//logs the final url query
 					Log.i("URL",UrlResult.toString());
 					
 					queryReply = WebStuff.getURLStringResponse(UrlResult);
 					//storing of the 
 					FileStuff.storeStringFile(this, "temp", queryReply, false);
 					Log.i("STORED FILE", "saved");
 				}catch (MalformedURLException e){
 					Log.e("BAD URL", "Malformed URL");
 					UrlResult = null;
 				}
 				Log.i("OnHandleIntent","Done looking up zipcode");
 				
 		Message message = Message.obtain();
 		message.arg1 = Activity.RESULT_OK;
		message.obj = "Service is done";
 		
 		try {
 			messenger.send(message);
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			Log.e("On handleintent",e.getMessage().toString());
 			e.printStackTrace();
 		}
 	}
 
 }
