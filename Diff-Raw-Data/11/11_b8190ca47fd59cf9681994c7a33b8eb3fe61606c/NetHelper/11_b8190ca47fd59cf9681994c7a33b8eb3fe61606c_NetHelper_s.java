 package org.jbs.happysad;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.jbs.happysad.Task;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Formatter;
 import org.jbs.happysad.HappyBottle;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.util.Base64;
 import android.util.Log;
 
 public class NetHelper {
 	private String TAG = "NETHELPER";
 	private long myID;
 	String username = "dhh";
 	String password = "secret";
 	private UIDhelper UIDh =  new UIDhelper();
 
 	
 	public NetHelper(){
 		myID = UIDh.getUID();
 	}
 
 
 
 	public long getID(String u){
 		long id = tryCheckUser(u);
 		if (id < 0) { 
 			return newUser(u);
 		}
 		return id;
 	}
 
 	private long tryCheckUser(String name){
 		String page = "error";
 		try {
 
 			HttpGet request = new HttpGet();
 			request.setURI(new URI("http://happytrack.heroku.com/finduser.json?email=" + name));
 			BasicHeader declareAuth = new BasicHeader("Authorization", "Basic " + Base64.encodeToString("dhh:secret".getBytes(), Base64.DEFAULT) + "==");
 			request.setHeader(declareAuth);
 			page = connectionHelper(request);
 		}
 		catch( Exception e){
 			e.printStackTrace();
 			//set page to be a valid json string with user id = -1
 		}
 
 		return searchForID(page);
 	}
 
 	//Given a jsonarray that might contain a user or might not, return the id of that user.
 	//if the array is empty, return -1
 	private long searchForID(String json){
 		Log.d(TAG, "json = " + json);
 		try {
 			JSONArray jarray = new JSONArray(json);
 			Log.d(TAG, "jarray = " + jarray.toString());
 			Log.d(TAG, "jarray length " + jarray.length());
 
 			//if the input is empty
 			if (jarray.length() < 1) { 
 				Log.d(TAG, "json array is empty, return -1");
 				return -1;
 				//return null
 			}
 			//else, find the id and return that;
 			JSONObject o = jarray.getJSONObject(0);
 			String bottle = o.getString("user");
 			JSONObject o2 = new JSONObject(bottle);
 			return o2.getLong("id");
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		//in case of error.
 		return -2;
 	}
 
 	//create a new user on rails server. 
 	private long newUser(String u){
 		//set up the request
 		HttpPost request = new HttpPost();
 		Object[] values = {u};
 		Formatter f = new Formatter();
 		f.format("user[email]=%s&commit=Create User", values);
 		//set up the query
 		String data = f.toString();
 
 		URI url;
 		try { 
 			url = new URI("http", "happytrack.heroku.com", "/users.json", data, null); 
 			//visit this url using POST (note, will not work with GET)
 			//http://happytrack.heroku.com/users.json?user[email]=sayhar@gmail.com
 			request.setURI(url);
 			Log.d(TAG, "data: "+ data);
 			Log.d(TAG, "uRL: " + url);
 		} catch (URISyntaxException e1) {
 
 			e1.printStackTrace();
 		}
 		//connection helper is our handy tool that makes the POST request (or GET request, at some points) 
 		//and then returns a String representation of the response page.
 		String jsonreturned = connectionHelper(request);
 
 		try {
 			//we asked for a JSON representation of the response, which makes it easy for us to get the user id.
 			JSONObject o = new JSONObject(jsonreturned);
 			String user = o.getString("user");
 			JSONObject o2 = new JSONObject(user);
 			//oh look we got the ID let's return it yay
 			return o2.getLong("id");
 		} catch (JSONException e) {
 			//there should be no error here. The rails server should always return valid json.
 			e.printStackTrace();
 		}
 		//this should never happen.
 		return -10000;
 
 	}
 
 	//this is how we download bottles. 
 	protected ArrayList<HappyBottle> download(Task t) {
 		//notice we take in task t. T could be "GETMINE" or "GETALL". Handy, eh?
 		String page = "error";
 		try {
 			//so we set up the get request as normal
 			HttpGet request = new HttpGet();
 			request.setURI(new URI("http://happytrack.heroku.com/bottles.json"));
 			if( t.equals(Task.GETMINE)){
 				request.setURI(new URI("http://happytrack.heroku.com/bottles/" + myID+".json"));
 			}
 			BasicHeader declareAuth = new BasicHeader("Authorization", "Basic " + Base64.encodeToString("dhh:secret".getBytes(), Base64.DEFAULT) + "==");
 			request.setHeader(declareAuth);
 			//then let connectionHelper do the heavy lifting for us
 			page = connectionHelper(request);
 		}
 		catch( Exception e){
 			e.printStackTrace();
 		}
 		//EASY! 
 		//parse will turn the json into an arraylist of bottles for us.
 		return parse(page);	
 	}
 
 	//
 	/**
 	 *this is how we upload, one bottle at a time.
 	 *@return result: if there was an error, and the bottle wasn't uploaded: false. Otherwise, if the upload went through, return true; 
 	 */
 	protected boolean upload(HappyBottle b) {
 		//so we set up the request
 		HttpPost request = new HttpPost();
 		Object[] values = {b.getEmo(), b.getLat(), b.getLong(), b.getMsg(), b.getUID(), b.getTime()};  
 		Formatter f = new Formatter();
 		//we input all the information here
 		f.format("bottle[emo]=%s&bottle[lat]=%s&bottle[long]=%s&bottle[msg]=%s&bottle[user_id]=%s&bottle[time]=%s&commit=Create Bottle", values);
 		String data = f.toString();
 
 		URI url;
 		try { 
 			url = new URI("http", "happytrack.heroku.com", "/bottles", data, null);
 			//here we add the data to the url (POST) and then of course send it to connectionhelper to do all the heavy lifting 
 			request.setURI(url);
 			Log.d(TAG, "data: "+ data);
 			Log.d(TAG, "uRL: " + url);
 		} catch (URISyntaxException e1) {
 
 			e1.printStackTrace();
 		}
 		//yep, connectionHelper will do all our work for ous
 		String result =  connectionHelper(request);
 		if (result.equals("error")){
 			return false;
 		}
 		//NOTICE that was are assuming that any result != "error" is succesful. This seams like a reasonable assumption but it could change.
 		return true;
 	}
 
 	//behold the mighty connectionHelper! It takes in requests, makes a connection, downloads the response, and returns it. 
 	private String connectionHelper(HttpRequestBase request ){
 		String page = "error"; //NOTICE: if you ever change this, change the code in upload() that checks for "error" to see if this worked or not. 
 		BufferedReader in = null;
 		HttpClient client = new DefaultHttpClient();
 		try{
 			//this is how we get past security
 			BasicHeader declareAuth = new BasicHeader("Authorization", "Basic " + Base64.encodeToString((username+":"+password).getBytes(), Base64.DEFAULT) + "==");
 			request.setHeader(declareAuth);
 			//this is where we send the actual request
 			HttpResponse response = client.execute(request);
 			//the following is all a way to easily read the resopose and put it in a string.
 			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 			StringBuffer sb = new StringBuffer("");
 			String line = "";
 			String NL = System.getProperty("line.separator");
 			while ((line = in.readLine()) != null) {
 				sb.append(line + NL);
 			}
 			in.close();
 			page = sb.toString();
 
 		}
 		catch (Exception e){
 			e.printStackTrace();
 		}
 		finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}}}
 		return page;
 	}
 
 	//ok so now we turn json info useful info
 	private ArrayList<HappyBottle> parse(String in){
 		ArrayList<HappyBottle> a = new ArrayList<HappyBottle>();
 		try {
 			JSONArray jarray = new JSONArray(in);
 			//ok so rails is annoying in how it sends us json info. It wraps everything into an array
 			//then it contains json objects within json objects. ANNOYING AS HELL
 			//so, within the array:
 			for (int i = 0; i<jarray.length(); i++){
 				//for each object in the array
 				JSONObject o = jarray.getJSONObject(i);
 				//turn the object into a bottle, using the power of newparseone
 				HappyBottle b = newparseone(o);
 				Log.d(TAG, "successfully parsed new happybottle - " + b);
 				a.add(b);
 			}  
 		} catch (JSONException e) {
 			Log.e(TAG,  "array error" + e.toString());
 			a.add(new HappyBottle(myID , 1,  1,(short) 1, "JSONARRAYERROR",1) );
 		}
 		catch (Exception e){
 			Log.e(TAG + "mysterious other error", e.toString());
 		}
 		return a;
 	}
 
 	//This 'unwraps' the object. Object contains another object. 
 	//Call newparsetwo to find the inner object that is a bottle
 	private HappyBottle newparseone(JSONObject o){
 		try{
 			String bottle = o.getString("bottle");
 			JSONObject o2 = new JSONObject(bottle);
 			return newparsetwo(o2);
 		}
 		catch (JSONException e) {
 			e.printStackTrace();
 			Log.e(TAG, "object error" + e.toString());
 			return new HappyBottle(myID ,  1,  1,(short) 1, "JSONOBJECTERROR",1) ;
 		}
 
 	}
 
 	//turns a jsonobject into a bottle
 	private HappyBottle newparsetwo(JSONObject o){
 		//pretty straightforward.
 		try {
 			int lati = o.getInt("lat");
 			int longi = o.getInt("long");
 			short emo = (short) o.getInt("emo");
 			String msg = o.getString("msg");
 			long time = o.getLong("time");
 			long uid = o.getLong("user_id");
 			return new HappyBottle( uid, lati , longi , emo, msg ,time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			Log.e(TAG + "object error", e.toString());
 			return new HappyBottle(myID ,  1,  1, (short) 1, "JSONOBJECTERROR",1) ;
 		}
 	}
 
 
 	/**
 	 * Return an ArrayList of bottles for the latest <limit> bottles within the specified box of latitude/longitude
 	 * @param minLat
 	 * @param maxLat 
 	 * @param minLong
 	 * @param maxLong
 	 * @param limit the max number of bottles to return
 	 * @return ArrayList of HappyBottles we download.
 	 */
 	public ArrayList<HappyBottle> downloadLocal(int minLat, int maxLat, int minLong, int maxLong, int limit){
 		return downloadLocalBefore(minLat, maxLat, minLong, maxLong, limit, -5);
 	}
 
 	/**
 	 * The same as downloadLocalBefore, except we have a new parameter: timebefore. Only return bottles created before time timebefore. 
 	 * It will download the most recent <limit> number of bottles, within the view defined by min/max lat/long, but only those before timebefore.
 	 * If timebefore < 0, it ignores that parameter. 
 	 * @param minLat
 	 * @param maxLat
 	 * @param minLong
 	 * @param maxLong
 	 * @param limit
 	 * @param timebefore
 	 * @return ArrayList of HappyBottles we download.
 	 */
	public ArrayList<HappyBottle> downloadLocalBefore(int minLat, int maxLat, int minLong, int maxLong, int limit, int timebefore){
 		String page = "error";
 		try{
 			HttpGet request = new HttpGet();
 			if (timebefore < 0){
 				request.setURI(new URI("http://happytrack.heroku.com/bottles/local/" +minLat +"/" + maxLat + "/" + minLong + "/" + maxLong + "/" + limit + ".json"));
 			} else{
 			request.setURI(new URI("http://happytrack.heroku.com/bottles/local/" +minLat +"/" + maxLat + "/" + minLong + "/" + maxLong + "/" + limit +"/" + timebefore+".json"));
 			}
 			Log.d(TAG, request.getURI().toString());
 			BasicHeader declareAuth = new BasicHeader("Authorization", "Basic " + Base64.encodeToString("dhh:secret".getBytes(), Base64.DEFAULT) + "==");
 			request.setHeader(declareAuth);
 			//then let connectionHelper do the heavy lifting for us
 			page = connectionHelper(request); 
 		}
 		catch( Exception e){
 			e.printStackTrace();
 		}
 		//EASY! 
 		//parse will turn the json into an arraylist of bottles for us.
 		return parse(page);	
 	}
 
 		
 	
 
 }
