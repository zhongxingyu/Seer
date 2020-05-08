 package com.timboe.rpsrts.applet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import com.timboe.rpsrts.enumerators.ObjectOwner;
 import com.timboe.rpsrts.managers.ResourceManager;
 import com.timboe.rpsrts.managers.Utility;
 
 public class HighScore implements Runnable {
 	
 	private final Utility utility = Utility.GetUtility();
 	private final ResourceManager resource_manager = ResourceManager.GetResourceManager();
 
 	@Override
 	public void run() {
 		try {
 			SubmitHighScore();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public HighScore() {
 	}
 	
 	public void SubmitHighScore() throws IOException {
 		//Create Post String
 		//Thanks to http://robbamforth.wordpress.com/2009/04/27/java-how-to-post-to-a-htmlphp-post-form/
		String data = URLEncoder.encode("pwd",    "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");
 		data += "&" + URLEncoder.encode("name",    "UTF-8") + "=" + URLEncoder.encode(utility.playerName, "UTF-8");
 		data += "&" + URLEncoder.encode("score",   "UTF-8") + "=" + URLEncoder.encode(Integer.toString(resource_manager.GetScore(ObjectOwner.Player)), "UTF-8");
 		data += "&" + URLEncoder.encode("wintime", "UTF-8") + "=" + URLEncoder.encode(Long.toString(utility.game_time_count/1000l), "UTF-8");
 		                
 		         
 		// Send Data To Page
 		URL url = new URL("http://tim-martin.co.uk/rpsrts_score.php");
 		URLConnection conn = url.openConnection();
 		conn.setDoOutput(true);
 		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 		wr.write(data);
 		wr.flush();
 		   
 		// Get The Response
 		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		String line;
 		while ((line = rd.readLine()) != null) {
 			System.out.println(line);
 		}
 	}
 }
