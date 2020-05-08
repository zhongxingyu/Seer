 package com.DotaStats.android;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 
 public class JSONParser {
 
 	private List<Player> playerList;
 
 	public JSONParser(String matchID) {
 		// String jsonString = getJSONFromMatchID(matchID);
 		this.playerList = new ArrayList<Player>();
 		try {
 			JSONObject json = new JSONObject(getJSONFromMatchID(matchID));
 			JSONArray players = json.getJSONArray("players");
			
 			/*
			 * Iterate through the players, creating objects for all ten of them.
 			 */
 			for (int i = 0; i < players.length(); i++) {
 				playerList.add(new Player(players.getJSONObject(i)));
 			}
			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private String getJSONFromMatchID(String matchID) {
 		String result = null;
 		try {
			result = new WebTask()
					.execute(
							"http://dotawebapihackathon.apphb.com/api/matches/GetMatchDetails?matchId=143088168").get();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public List<Player> getPlayerList() {
 		return this.playerList;
 	}
 }
 
 class WebTask extends AsyncTask<String, Void, String> {
 
 	@Override
	protected String doInBackground(String... arg0) {
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(arg0[0]);
 		try {
 			HttpResponse response = client.execute(httpGet);
 			HttpEntity entity = response.getEntity();
 			InputStream content = entity.getContent();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					content));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return builder.toString();
 	}
 
 }
