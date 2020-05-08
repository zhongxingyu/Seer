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
 
 	// result of search, either array list of games or one game
 	ArrayList<Game> searchResults;
 	Game searchResult;
 
 	Network(Context context) {
 		ctxt = context;
 
 		progressDialog = new ProgressDialog(ctxt);
 	}
 
 	String encrypt(String string){
 		return Base64.encodeToString( string.getBytes(), Base64.DEFAULT );
 	}
 	
 	String decrypt(String string){
 		return new String( Base64.decode( string, Base64.DEFAULT ) );
 	}
 	Drawable getImage(String name) {
 		
 		//HTTP URL
 		URL url;
 		try {
 
 			// Create URL from url parameter
 			url = new URL(name);
 
 			// establish connection and download image
 			URLConnection connection = url.openConnection();
 			connection.setUseCaches(true);
 			
 			//create Drawable from stream associated with connection
 			return Drawable
 					.createFromStream(connection.getInputStream(), "src");
 		} catch (MalformedURLException e) {
 
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 
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
 
 		return gameResults;
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
 
		return g.getTitle().equalsIgnoreCase("true") ? true : false;
 
 	}
 
 	boolean addGameForUser(int userId, int gameId) {
 		ArrayList<Game> result = new ArrayList<Game>();
 		try {
 			result = this.execute("1", String.valueOf(userId), String.valueOf(gameId)).get();
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
 
 		Game newGame = new Game();
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
 
 			else {
 				skip(parser);
 			}
 
 		}
 
 		return newGame;
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
 
 	//poll network connectivity, needs manifest permission:     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
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
 
 		Log.i("PREXECUTE", "something");
 
 	}
 
 	@Override
 	protected void onPostExecute(ArrayList<Game> unused) {
 		Log.i("THREAD FINISHED", "Words");
 
 		if (progressDialog.isShowing()) {
 			progressDialog.dismiss();
 		}
 
 	}
 
 	@Override
 	protected ArrayList<Game> doInBackground(String... params) {
 
 		// if(params[0].equalsIgnoreCase("getGames"))
 		// ctrl = 4;
 
 		ctrl = Integer.parseInt(params[0]);
 
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
 				Log.i("user", paramList.get(0).toString());
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
 				 reader = new BufferedReader(	new InputStreamReader(input, "iso-8859-1"), 8);
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
 			
 			Log.i("LINE", line.toString());
 
 			Game returnGame = new Game();
 
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
 				Log.i("user", paramList.get(0).toString());
 				Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 
 				Log.i("INPUT", input.toString());
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
 				 reader = new BufferedReader(	new InputStreamReader(input, "iso-8859-1"), 8);
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
 			
 			Log.i("LINE", line.toString());
 
 			Game returnGame = new Game();
 
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
 				Log.i("user", paramList.get(0).toString());
 				Log.i("password", paramList.get(1).toString());
 				// Log.i("game", bigList.get(1).toString());
 				httppost.setEntity(new UrlEncodedFormEntity(paramList));
 				HttpResponse response = httpclient.execute(httppost);
 
 				HttpEntity entity = response.getEntity();
 
 				input = entity.getContent();
 
 				Log.i("INPUT", input.toString());
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
 
 			Game returnGame = new Game();
 
			if (input.toString().equalsIgnoreCase("game successfully added"))
				returnGame.setTitle("true");
			else
				returnGame.setTitle("false");
 
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
 				Log.i("RESPONSE", responseBody);
 
 			} catch (Throwable t) {
 				// can use t as a string to pass to logcat to display error
 				Log.i("ERROR", t.toString());
 
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
 				Log.i("RESPONSE", responseBody);
 
 			} catch (Throwable t) {
 				// can use t as a string to pass to logcat to display error
 				Log.i("ERROR", t.toString());
 
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
 		return null;
 
 	}
 }
