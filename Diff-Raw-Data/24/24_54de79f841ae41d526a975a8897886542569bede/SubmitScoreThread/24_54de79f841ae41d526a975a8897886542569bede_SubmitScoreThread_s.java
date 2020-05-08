 package org.racenet.racesow.threads;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
import org.racenet.framework.Downloader;
import org.racenet.helpers.InputStreamToString;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
 
 public class SubmitScoreThread extends Thread {
 
 	private float time;
 	private String map;
 	private String player;
	private short tries;
 	
 	public SubmitScoreThread(String map, String player, float time) {
 		
 		this.time = time;
 		this.map = map;
 		this.player = player;
		this.tries = 0;
 	}
 	
 	@Override
     public void run() {         
 
 		this.submitScores();
     }
 	
 	private void submitScores() {
 		
 		HttpClient client = new DefaultHttpClient();				
 	    HttpPost post = new HttpPost("http://racesow2d.warsow-race.net/submit.php");
 	    
 	    try {
 	    	
 	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 	        nameValuePairs.add(new BasicNameValuePair("map", this.map));
 	        nameValuePairs.add(new BasicNameValuePair("player", this.player));
 	        nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(new Float(this.time))));
 	        nameValuePairs.add(new BasicNameValuePair("key", "alpha-key"));
 	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        HttpResponse response = client.execute(post);
 	        
 	    } catch (ClientProtocolException e) {
 	    	
	    	if (this.tries++ < 10) this.submitScores();
 	    	
 	    } catch (IOException e) {
     		
	    	if (this.tries++ < 10) this.submitScores();
 	    }
 	}
 }
