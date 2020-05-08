 package com.derpicons.gshelf;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.util.Base64;
 import android.util.Log;
 import android.util.Xml;
 
 //Class to handle API interactions
 public class Network extends AsyncTask<String, String, ArrayList<Game>> {
 
 	private static final Map<Integer, String> errorMap = new HashMap<Integer, String>();
 
 	// calling activity context
 	private Context ctxt;
 
 	// dialog ot display while loading
 	ProgressDialog progressDialog;
 
 	// control for background thread, could be switched by passing params to
 	// execute method but.............
 	int ctrl;
 
 	// name of game to search for
 	String name;
 
 	private final String BASEURLIMG = "http://thegamesdb.net/banners/";
 
 	// result of search, either array list of games or one game
 	ArrayList<Game> searchResults;
 	Game searchResult;
 
 	Network(Context context) {
 		ctxt = context;
 
 		progressDialog = new ProgressDialog(ctxt);
 	}
 
 	String encrypt(String string) {
 		return Base64.encodeToString(string.getBytes(), Base64.DEFAULT);
 	}
 
 	String decrypt(String string) {
 		return new String(Base64.decode(string, Base64.DEFAULT));
 	}
 
 	Drawable getImage(String name) {
 		ArrayList<Game> games = null;
 		try {
 			games = new Network(ctxt).execute("6", name).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return games.get(0).getCover();
 	}
 
 	
 	void getDeals(){
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("12").get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 		Log.i("DEALS", g.getTitle());
 		return;
 	}
 	
 	// gameid vendor desc userid expiration
 	boolean addToDeals(ArrayList<Integer> gameIds, String vendor,
 			String overview, int userId, Date expiration) {
 
 		StringBuilder builder = new StringBuilder();
 		for (int g : gameIds) {
 			builder.append("" + g + ",");
 		}
 
 		String gIds = builder.toString();
 
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("11", gIds,vendor, overview,String.valueOf(userId), expiration.toString()).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 		int idVal;
 		try {
 			idVal = Integer.parseInt(g.getTitle());
 		} catch (NumberFormatException e) {
 			Log.i("MESSAGE", g.getTitle());
 			return false;
 		}
 
 		return idVal == 1 ? true : false;
 
 	}
 
 	// userid,gameid,buy,sell,trade
 	boolean addToMarket(int userId, int gameId, String price) {
 		ArrayList<Game> result = new ArrayList<Game>();
 
 		try {
 			result = this.execute("9", String.valueOf(userId),
 					String.valueOf(gameId), price).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 		int idVal;
 		try {
 			idVal = Integer.parseInt(g.getTitle());
 		} catch (NumberFormatException e) {
 			Log.i("MESSAGE", g.getTitle());
 			return false;
 		}
 
 		return idVal == 1 ? true : false;
 	}
 
 	// userId
 	void pullMessage(int userId) {
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("8", String.valueOf(userId)).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 
 		// int idVal = Integer.parseInt(g.getTitle());
 		Log.i("The Result", result.get(0).getTitle());
 
 		return;
 	}
 
 	// receiver sendid sendername message
 	boolean pushMessage(int receiverId, int senderId, String senderName,
 			String message) {
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("7", String.valueOf(receiverId),
 					String.valueOf(senderId), senderName, message).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 
 		int idVal = Integer.parseInt(g.getTitle());
 
 		return idVal == 1 ? true : false;
 	}
 
 	int login(String username, String password) {
 
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("2", username, encrypt(password)).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 
 		int idVal = Integer.parseInt(g.getTitle());
 
 		return idVal;
 	}
 
 	Game getGame(int gameId) {
 
 		Game game = null;
 		try {
 			game = (this.execute("13", String.valueOf(gameId)).get()).get(0);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		
 		return game;
 	}
 
 	// returns one game or null if not found
 	Game getGame(String searchName) {
 
 		ArrayList<Game> gamList = null;
 		try {
 			gamList = this.execute("5", searchName).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (gamList.size() <= 0) {
 			return null;
 		}
 
 		else {
 			// gamList.get(0).setCover(getImage(gamList.get(0).getGameUrl()));
 			return gamList.get(0);
 		}
 
 	}
 
 	// returns an array list of type game
 	ArrayList<Game> getGames(String searchName) {
 
 		ArrayList<Game> gameResults = null;
 
 		try {
 			gameResults = this
 					.execute("4", searchName.replaceAll("\\s", "%20")).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// for (Game g : gameResults) {
 		// g.setCover(getImage(g.getGameUrl()));
 		// }
 		Log.i("NUM GAME RESULTS", "COUNT: " + gameResults.size());
 		return gameResults;
 	}
 
 	void getMarket() {
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("10").get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 		Log.i("MARKET", g.getTitle());
 		return;
 	}
 
 	// key: username , key: password
 	boolean addUser(String username, String password) {
 
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("3", username, encrypt(password)).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 
 		return g.getTitle().equalsIgnoreCase("1") ? true : false;
 
 	}
 
 	boolean addGameForUser(int userId, int gameId) {
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("1", String.valueOf(userId),
 					String.valueOf(gameId)).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Game g = result.get(0);
 
 		return g.getTitle().equalsIgnoreCase("true") ? true : false;
 	}
 
 	// parses response from getGames API call
 	ArrayList<Game> parseResponse(String rawResponse) throws Exception {
 		// converting String containg raw XML to inputstream
 		InputStream iStream = new ByteArrayInputStream(rawResponse.getBytes());
 
 		// set up XML pull parser
 		XmlPullParser parser = Xml.newPullParser();
 
 		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 		parser.setInput(iStream, null);
 		parser.nextTag();
 
 		parser.require(XmlPullParser.START_TAG, null, "Data");
 
 		ArrayList<Game> games = new ArrayList<Game>();
 
 		// iterate through tags, collect data
 		while (parser.next() != XmlPullParser.END_TAG) {
 			if (parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 
 			String name = parser.getName();
 
 			if (name.equals("Game")) {
 				games.add(parseGame(parser));
 			} else {
 				skip(parser);
 			}
 		}
 
 		iStream.close();
 		return games;
 	}
 
 	Game parseGame(XmlPullParser parser) throws XmlPullParserException,
 			IOException {
 
 		Game newGame = new Game(ctxt);
 		while (parser.next() != XmlPullParser.END_TAG) {
 			if (parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 
 			String tagName = parser.getName();
 
 			if (tagName.equals("GameTitle")) {
 				newGame.setTitle(parseGameTitle(parser, newGame));
 
 			}
 
 			else if (tagName.equals("Platform")) {
 				newGame.setPlatform(parsePlatform(parser, newGame));
 			}
 
 			else if (tagName.equals("Overview")) {
 
 				newGame.setOverview(parseOverview(parser, newGame));
 
 			}
 
 			else if (tagName.equals("id")) {
 				newGame.setKey(Integer.parseInt(parseKey(parser, newGame)));
 			}
 
 			else if (tagName.equals("Images")) {
 
 				if (newGame.getGameUrl() == null)
 					newGame.setGameUrl(BASEURLIMG + parseImage(parser, newGame));
 
 			}
 
 			else {
 				skip(parser);
 			}
 
 		}
 
 		return newGame;
 	}
 
 	String parseImage(XmlPullParser parser, Game game) throws IOException,
 			XmlPullParserException {
 
 		String bannerData = null;
 		boolean gotArt = false;
 		while (parser.next() != XmlPullParser.END_TAG) {
 			if (parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 
 			String tagName = parser.getName();
 
 			if (tagName.equals("fanart") && !gotArt) {
 				String temp = "ONION";
 
 				temp = parseBanner(parser, null);
 				bannerData = temp;
 				gotArt = true;
 			}
 
 			else {
 				skip(parser);
 			}
 
 		}
 
 		return bannerData;
 	}
 
 	String parseBanner(XmlPullParser parser, Game game) throws IOException,
 			XmlPullParserException {
 
 		String bannerData = null;
 
 		while (parser.next() != XmlPullParser.END_TAG) {
 			if (parser.getEventType() != XmlPullParser.START_TAG) {
 				continue;
 			}
 
 			String tagName = parser.getName();
 
 			if (tagName.equals("original")) {
 				String temp = "ONION";
 
 				temp = parseDeep(parser, null);
 				bannerData = temp;
 			}
 
 			else {
 				skip(parser);
 			}
 		}
 		return bannerData;
 	}
 
 	String parseDeep(XmlPullParser parser, Game game) throws IOException,
 			XmlPullParserException {
 		parser.require(XmlPullParser.START_TAG, null, "original");
 		String gameTitle = "null";
 
 		if (parser.next() == XmlPullParser.TEXT) {
 			gameTitle = parser.getText();
 			parser.nextTag();
 		}
 
 		return gameTitle;
 
 	}
 
 	String parseGameTitle(XmlPullParser parser, Game game) throws IOException,
 			XmlPullParserException {
 		parser.require(XmlPullParser.START_TAG, null, "GameTitle");
 		String gameTitle = "null";
 
 		if (parser.next() == XmlPullParser.TEXT) {
 			gameTitle = parser.getText();
 			parser.nextTag();
 		}
 
 		return gameTitle;
 	}
 
 	String parseKey(XmlPullParser parser, Game game) throws IOException,
 			XmlPullParserException {
 
 		parser.require(XmlPullParser.START_TAG, null, "id");
 		String key = "null";
 
 		if (parser.next() == XmlPullParser.TEXT) {
 			key = parser.getText();
 			parser.nextTag();
 		}
 
 		return key;
 	}
 
 	String parsePlatform(XmlPullParser parser, Game game) throws IOException,
 			XmlPullParserException {
 		parser.require(XmlPullParser.START_TAG, null, "Platform");
 		String platform = "null";
 
 		if (parser.next() == XmlPullParser.TEXT) {
 			platform = parser.getText();
 			parser.nextTag();
 		}
 
 		return platform;
 	}
 
 	String parseOverview(XmlPullParser parser, Game game) throws IOException,
 			XmlPullParserException {
 		parser.require(XmlPullParser.START_TAG, null, "Overview");
 		String overview = "null";
 
 		if (parser.next() == XmlPullParser.TEXT) {
 
 			overview = parser.getText();
 			parser.nextTag();
 		}
 
 		return overview;
 	}
 
 	// poll network connectivity, needs manifest permission: <uses-permission
 	// android:name="android.permission.ACCESS_NETWORK_STATE" />
 	boolean hasNetwork() {
 		ConnectivityManager cm = (ConnectivityManager) ctxt
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	private void skip(XmlPullParser parser) throws XmlPullParserException,
 			IOException {
 		if (parser.getEventType() != XmlPullParser.START_TAG) {
 			throw new IllegalStateException();
 		}
 		int depth = 1;
 		while (depth != 0) {
 			switch (parser.next()) {
 			case XmlPullParser.END_TAG:
 				depth--;
 				break;
 			case XmlPullParser.START_TAG:
 				depth++;
 				break;
 			}
 		}
 	}
 
 	public String getQuestion(String username) {
 
 		return "null";
 
 	}
 
 	public String changePassword(String answer, String password) {
 
 		return "null";
 
 	}
 
 	@Override
 	protected void onPreExecute() {
 		// progressDialog = ProgressDialog.show(ctxt, "Loading",
 		// "Loading started..." );
 		progressDialog.setMessage("Loading...");
 		progressDialog.show();
 
 		Log.i("PREXECUTE", "EXECUTING");
 
 	}
 
 	@Override
 	protected void onPostExecute(ArrayList<Game> unused) {
 		Log.i("POSTEXECUTE", "EXECUTING");
 
 		if (progressDialog.isShowing()) {
 			progressDialog.dismiss();
 		}
 
 	}
 
 	@Override
 	protected ArrayList<Game> doInBackground(String... params) {
 
 		// if(params[0].equalsIgnoreCase("getGames"))
 		// ctrl = 4;
 
 		ctrl = Integer.parseInt(params[0]);
 
 		Log.i("CONTROL LINE", "" + ctrl);
 		if (ctrl == 1) {
 
 			String result = null;
 			InputStream input = null;
 
 			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
 			paramList.add(new BasicNameValuePair("userid", params[1]));
 			paramList.add(new BasicNameValuePair("gameid", params[2]));
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/addtolibrary.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// get data
 
 			// build data
 			try {
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(input, "iso-8859-1"), 8);
 
 				StringBuilder sbuilder = new StringBuilder();
 
 				String line = null;
 
 				while ((line = reader.readLine()) != null) {
 
 					sbuilder.append(line + "\n");
 					System.out.println(line);
 				}
 				input.close();
 				result = sbuilder.toString();
 			} catch (Exception e) {
 				Log.e("log_tag", "Error converting result " + e.toString());
 
 				// json parser
 				int id;
 				String name;
 				try {
 					JSONArray jArray = new JSONArray(result);
 					JSONObject json_data = null;
 					for (int i = 0; i < jArray.length(); i++) {
 						json_data = jArray.getJSONObject(i);
 						int fd_id = json_data.getInt("UserID");
 						Log.i("FD_ID", "Value: " + fd_id);
 						String fd_name = json_data.getString("GameID");
 						Log.i("FD_NAME", "Value: " + fd_name);
 						// outputStream.append(id +" " + name + "\n");
 					}
 
 				}
 
 				catch (Exception el) {
 					Log.i("JSON", "Exception");
 				}
 
 			}
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			String line = "pop";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// Log.i("LINE", line.toString());
 
 			Game returnGame = new Game(ctxt);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 		}
 
 		// login
 		else if (ctrl == 2) {
 
 			InputStream input = null;
 
 			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
 			paramList.add(new BasicNameValuePair("username", params[1]));
 			paramList.add(new BasicNameValuePair("password", params[2]));
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/login.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 
 				// Log.i("INPUT", input.toString());
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			StringBuilder sbuilder = new StringBuilder();
 
 			String line = "pop";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// Log.i("LINE", line.toString());
 
 			Game returnGame = new Game(ctxt);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 
 		}
 
 		// add user
 		else if (ctrl == 3) {
 
 			String result = null;
 			InputStream input = null;
 
 			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
 			paramList.add(new BasicNameValuePair("username", params[1]));
 			paramList.add(new BasicNameValuePair("password", params[2]));
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/register.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 
 				// Log.i("INPUT", input.toString());
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// get data
 
 			Game returnGame = new Game(ctxt);
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			StringBuilder sbuilder = new StringBuilder();
 
 			String line = "";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			// Log.i("Add user result", line);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 		}
 
 		// get games
 		else if (ctrl == 4) {
 
 			name = params[1];
 
 			String url = "http://thegamesdb.net/api/GetGame.php?name=" + name;
 
 			// create the http get from the url
 			HttpGet getMethod = new HttpGet(url);
 
 			// obtain game information
 			DefaultHttpClient client = new DefaultHttpClient();
 			String responseBody = "nil";
 
 			try {
 				ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
 				// obtain xml game data
 				responseBody = client.execute(getMethod, responseHandler);
 				// Log.i("RESPONSE", responseBody);
 
 			} catch (Throwable t) {
 				// can use t as a string to pass to logcat to display error
 				// Log.i("ERROR", t.toString());
 
 				return null;
 			}
 			try {
 				progressDialog.dismiss();
 
 				// parse the xml into an array of Game objects and return
 				// HERE
 				// return parseResponse(responseBody);
 				searchResults = parseResponse(responseBody);
 			} catch (Exception e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				return null;
 			}
 
 			return searchResults;
 		}
 
 		// get game
 		else if (ctrl == 5) {
 
 			name = params[1];
 
 			progressDialog.setMessage("Working...");
 			progressDialog.show();
 
 			String url = "http://thegamesdb.net/api/GetGame.php?name="
 					+ name.replaceAll("\\s", "%20");
 
 			// create the http get from the url
 			HttpGet getMethod = new HttpGet(url);
 
 			// obtain game information
 			DefaultHttpClient client = new DefaultHttpClient();
 			String responseBody = "nil";
 
 			try {
 				ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
 				// obtain xml game data
 				responseBody = client.execute(getMethod, responseHandler);
 				// Log.i("RESPONSE", responseBody);
 
 			} catch (Throwable t) {
 				// can use t as a string to pass to logcat to display error
 				// Log.i("ERROR", t.toString());
 
 				return null;
 			}
 			try {
 				progressDialog.dismiss();
 
 				// parse the xml into an array of Game objects and return
 				// HERE
 				// return parseResponse(responseBody);
 				searchResults = parseResponse(responseBody);
 			} catch (Exception e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				progressDialog.dismiss();
 				return null;
 			}
 
 			for (Game g : searchResults) {
 				if (g.getTitle().equalsIgnoreCase(name)) {
 					progressDialog.dismiss();
 					ArrayList<Game> returnList = new ArrayList<Game>();
 					returnList.add(g);
 					return returnList;
 				}
 			}
 
 			progressDialog.dismiss();
 
 			return null;
 		}
 
 		else if (ctrl == 6) {
 
 			// HTTP URL
 			URL url;
 			try {
 
 				// Create URL from url parameter
 				url = new URL(params[1]);
 
 				// establish connection and download image
 				URLConnection connection = url.openConnection();
 				connection.setUseCaches(true);
 
 				Drawable draw = Drawable.createFromStream(
 						connection.getInputStream(), "src");
 
 				Game returnGame = new Game(ctxt);
 				returnGame.setCover(draw);
 
 				ArrayList<Game> returnList = new ArrayList<Game>();
 
 				returnList.add(returnGame);
 				return returnList;
 
 			} catch (MalformedURLException e) {
 
 				e.printStackTrace();
 				return null;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 
 		}
 
 		// push message receiver senderid sendsername message
 		else if (ctrl == 7) {
 
 			InputStream input = null;
 
 			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
 			paramList.add(new BasicNameValuePair("receiver", params[1]));
 			paramList.add(new BasicNameValuePair("senderid", params[2]));
 			paramList.add(new BasicNameValuePair("sendername", params[3]));
 			paramList.add(new BasicNameValuePair("message", params[4]));
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/addmessage.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 
 				// Log.i("INPUT", input.toString());
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			StringBuilder sbuilder = new StringBuilder();
 
 			String line = "pop";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// Log.i("LINE", line.toString());
 
 			Game returnGame = new Game(ctxt);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 
 		}
 
 		// pull message userid
 		else if (ctrl == 8) {
 
 			InputStream input = null;
 
 			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
 			paramList.add(new BasicNameValuePair("userid", params[1]));
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/getmessages.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 
 				// Log.i("INPUT", input.toString());
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			StringBuilder sbuilder = new StringBuilder();
 
 			String line = "pop";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// Log.i("LINE", line.toString());
 
 			Game returnGame = new Game(ctxt);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 
 		}
 
 		// addToMarket
 		else if (ctrl == 9) {
 
 			InputStream input = null;
 
 			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
 			paramList.add(new BasicNameValuePair("userid", params[1]));
 			Log.i("USERID", params[1]);
 			paramList.add(new BasicNameValuePair("gameid", params[2]));
 			Log.i("GAMEID", params[2]);
 			paramList.add(new BasicNameValuePair("price", params[3]));
 			Log.i("PRICE", params[3]);
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/addtomarket.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 				// Log.i("INPUT", input.toString());
 			} catch (UnsupportedEncodingException e1) {
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 
 			StringBuilder sbuilder = new StringBuilder();
 
 			String line = "pop";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			// Log.i("LINE", line.toString());
 
 			Game returnGame = new Game(ctxt);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 
 		}
 
 		else if (ctrl == 10) {
 
 			InputStream input = null;
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/getmarket.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 				// Log.i("INPUT", input.toString());
 			} catch (UnsupportedEncodingException e1) {
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 
 			StringBuilder sbuilder = new StringBuilder();
 
 			String line = "pop";
 			try {
 				line = reader.readLine();
 				Log.i("LINE", line);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			// Log.i("LINE", line.toString());
 
 			Game returnGame = new Game(ctxt);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 
 		}
 		
 		//adddeal gameid vendor desc userid expiration 
 		else if (ctrl == 11) {
 
 			InputStream input = null;
 
 			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
 			paramList.add(new BasicNameValuePair("gameid", params[1]));
 			paramList.add(new BasicNameValuePair("vendor", params[2]));
 			paramList.add(new BasicNameValuePair("desc", params[3]));
 			paramList.add(new BasicNameValuePair("userid", params[4]));
 			paramList.add(new BasicNameValuePair("expiration", params[5]));
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/addtodeals.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 				// Log.i("INPUT", input.toString());
 			} catch (UnsupportedEncodingException e1) {
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 
 			StringBuilder sbuilder = new StringBuilder();
 
 			String line = "pop";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			// Log.i("LINE", line.toString());
 
 			Game returnGame = new Game(ctxt);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 
 		}
 		
 		else if (ctrl == 12) {
 
 			InputStream input = null;
 
 			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
 			// access db and execute
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(
 					"http://gshelf.epyon-tech.net/getdeals.php");
 			try {
 				// Log.i("user", paramList.get(0).toString());
 				// Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 				// Log.i("INPUT", input.toString());
 			} catch (UnsupportedEncodingException e1) {
 				e1.printStackTrace();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new InputStreamReader(input,
 						"iso-8859-1"), 8);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 
 			StringBuilder sbuilder = new StringBuilder();
 
 			String line = "pop";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			// Log.i("LINE", line.toString());
 
 			Game returnGame = new Game(ctxt);
 
 			returnGame.setTitle(line);
 
 			ArrayList<Game> returnList = new ArrayList<Game>();
 
 			returnList.add(returnGame);
 			return returnList;
 
 		}
 		
 		// get game
 				else if (ctrl == 13) {
 
 
 					progressDialog.setMessage("Working...");
 					progressDialog.show();
 
 					String url = "http://thegamesdb.net/api/GetGame.php?id="
							+ params[1];
 
 					// create the http get from the url
 					HttpGet getMethod = new HttpGet(url);
 
 					// obtain game information
 					DefaultHttpClient client = new DefaultHttpClient();
 					String responseBody = "nil";
 
 					try {
 						ResponseHandler<String> responseHandler = new BasicResponseHandler();
 
 						// obtain xml game data
 						responseBody = client.execute(getMethod, responseHandler);
 						// Log.i("RESPONSE", responseBody);
 
 					} catch (Throwable t) {
 						// can use t as a string to pass to logcat to display error
 						// Log.i("ERROR", t.toString());
 
 						return null;
 					}
 					try {
 						progressDialog.dismiss();
 
 						// parse the xml into an array of Game objects and return
 						// HERE
 						// return parseResponse(responseBody);
 						searchResults = parseResponse(responseBody);
 					} catch (Exception e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 						progressDialog.dismiss();
 						return null;
 					}
 
 					
 
 					progressDialog.dismiss();
 
 					return searchResults;
 				}
 		return null;
 
 	}
 }
