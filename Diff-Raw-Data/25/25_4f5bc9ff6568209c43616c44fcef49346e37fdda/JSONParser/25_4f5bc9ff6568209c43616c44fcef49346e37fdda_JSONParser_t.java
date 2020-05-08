 package com.DotaStats.android;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 public class JSONParser {
 
 	private JSONArray jsonArray;
 	private List<Player> playerList;
 
 	public JSONParser(String matchID) {
 		// String jsonString = getJSONFromMatchID(matchID);
 		this.playerList = new ArrayList<Player>();
 
 		/*
 		 * Iterate through the array and create players. Add the players to the
 		 * playerList.
 		 */
 		/*
 		 * Create test data for now, TODO: Make this do actual work.
 		 */
 		this.playerList.add(new Player("Player1", 1, 1, 1, 1, 1, 1, 1));
 		this.playerList.add(new Player("Player2", 2, 2, 2, 2, 2, 2, 2));
 		this.playerList.add(new Player("Player3", 3, 3, 3, 3, 3, 3, 3));
 		this.playerList.add(new Player("Player4", 4, 4, 4, 4, 4, 4, 4));
 		this.playerList.add(new Player("Player5", 5, 5, 5, 5, 5, 5, 5));
 		this.playerList.add(new Player("Player6", 6, 6, 6, 6, 6, 6, 6));
		this.playerList.add(new Player("Player7", 7, 7, 7, 7, 7, 7, 7));
		this.playerList.add(new Player("Player8", 8, 8, 8, 8, 8, 8, 8));
		this.playerList.add(new Player("Player9", 9, 9, 9, 9, 9, 9, 9));
		this.playerList.add(new Player("Player10", 10, 10, 10, 10, 10, 10, 10));
 
 	}
 
 	private String getJSONFromMatchID(String matchID) {
 		StringBuilder sb = new StringBuilder();
 
 		try {
 			DefaultHttpClient httpclient = new DefaultHttpClient(
 					new BasicHttpParams());
 			HttpPost httppost = new HttpPost(
 					"http://someJSONUrl/jsonPlace.html");
 			httppost.setHeader("Content-type", "application/json");
 			InputStream inputStream = null;
 			HttpResponse response = httpclient.execute(httppost);
 			HttpEntity entity = response.getEntity();
 			inputStream = entity.getContent();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					inputStream, "UTF-8"), 8);
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return sb.toString();
 	}
 
 	public List<Player> getPlayerList() {
 		return this.playerList;
 	}
 }
