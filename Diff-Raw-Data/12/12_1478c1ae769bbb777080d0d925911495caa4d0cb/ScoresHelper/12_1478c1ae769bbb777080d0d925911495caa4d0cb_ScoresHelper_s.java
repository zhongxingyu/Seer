 package edu.csumb.ideasofmarch.codecruncher;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 public class ScoresHelper {
 
 	Context mContext = null;
 
     public ScoresHelper (Context context) {
         mContext = context;
     }
 
 	public void putGlobalHighScore(String name, int score, int constantGameType) {
 		
 //		int current = CrunchConstants.myScoresMap.get(constantGameType);
 //		if (score <= current){
 //			return;
 //		}
 		
 		
 		updateLocalScores(score, constantGameType);
 		
 		int totalScores = 0;
 		
 		for(int i = 0; i < CrunchConstants.NUM_GAME_MODES; i++){
			totalScores += CrunchConstants.myScoresMap.get(i+1);
 		}
 		
 		HttpClient httpclient = new DefaultHttpClient();
 		String restUrl = "";
		try {
			restUrl = URLEncoder.encode(CrunchConstants.backendURL + "/?name="
					+ name + "&score=" + totalScores, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
 		
 		HttpGet httpget = new HttpGet(restUrl);
 
 		try {
 			httpclient.execute(httpget);
 		} catch (Exception e) {
 			Log.v("error",
 					"Exception thrown! Check out calls to 'getGlobalHighScores().'");
 		}
 		
 	}
 	
 	private void updateLocalScores(int score, int constantGameType){
 		int current = CrunchConstants.myScoresMap.get(constantGameType);
 		if (score > current){
 			CrunchConstants.myScoresMap.remove(constantGameType);
 			CrunchConstants.myScoresMap.put(constantGameType, score);
 		} else {
 			return;
 		}
 		
 		
 		try {
 			FileOutputStream fos = mContext.openFileOutput(CrunchConstants.SCORES_FILENAME, mContext.MODE_PRIVATE);
 			
 			fos.write(new Gson().toJson(CrunchConstants.myScoresMap).getBytes());
 			fos.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 }
