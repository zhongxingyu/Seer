 package com.kniffenwebdesign.roku.ecp;
 
 import java.io.IOException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.net.http.AndroidHttpClient;
 import android.util.Log;
 
 
 public class EcpClient {
 	private static final String TAG = "EcpClient";
     private static final EcpClient instance = new EcpClient();
     
     protected String ipAddress = "";
     protected int port = 8060;
 
 	// Private constructor prevents instantiation from other classes
 	private EcpClient(){
 		
 	}
 	
     public static EcpClient getInstance() {
         return instance;
     }
     
     public String getIpAddress() {
 		return this.ipAddress;
 	}
 
 	public void setIpAddress(String ipAddress) {
 		this.ipAddress = ipAddress;
 	}
     
 	public void executeRequest(String action){
 		HttpClient client = new DefaultHttpClient();
		String uri = "http://" + this.ipAddress + ":" + this.port + "/" + action;
 		Log.v(TAG, uri);
 		HttpUriRequest request = new HttpPost(uri);
 		
 		try {
 			client.execute(request);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void keyPress(String key){
 		executeRequest("keypress/" + key);
 	}
 	
 	public void keyUp(String key){
 		executeRequest("keyup/" + key);
 	}
 	
 	public void keyDown(String key){
 		executeRequest("keydown/" + key);
 	}
 	
 	public void sendCharacter(char character){
 		executeRequest("keydown/Lit_" + character );
 	}
 	
 	public void sendString(String string){
 		for(int i = 0; i < string.length() - 1; i++){
 			sendCharacter(string.charAt(i));
 		}
 	}
 }
