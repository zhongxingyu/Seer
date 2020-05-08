 
 /* 
  * GPL License Copyright (c) 2011 Karsten Schmidt
  * 
  * This demo & library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * http://creativecommons.org/licenses/LGPL/2.1/
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 
 /*
  * EPSON PLANET UPDATE
  * Copyright (c) 2013 TenTonRaygun
  * */
 
 // import TextSpawn;
 // import UserProfile;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /// xml
 import javax.xml.bind.*;
 
 /// osc stuff
 import oscP5.*;
 import rwmidi.MidiInput;
 import rwmidi.MidiOutput;
 // import src.SawWave;
 // import netP5.*;
 
 /// processing core libraries
 import processing.core.*;
 
 /// toxiclib for 3D
 import toxi.geom.Vec3D;
 import toxi.geom.mesh.Mesh3D;
 import toxi.geom.mesh.SphereFunction;
 import toxi.geom.mesh.SurfaceMeshBuilder;
 import toxi.processing.ToxiclibsSupport;
 
 //twitter libraries
 import twitter4j.Status;
 //import twitter4j.StatusAdapter;
 import twitter4j.GeoLocation;
 import twitter4j.IDs;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.Twitter;
 //import twitter4j.TwitterException;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import twitter4j.FilterQuery;
 import twitter4j.Paging;
 import twitter4j.User;
 //java libraries
 
 
 /*
  * <ul>
  * <li>Move mouse to rotate view</li>
  * <li>Press '-' / '=' to adjust zoom</li>
  * </ul>
  * </p>
  */
 @SuppressWarnings("serial")
 public class EpsonPlanet extends PApplet {
 
 	
 	
 	////// FULL SCREEN HANDLER
 	/**
 	 * Main entry point to run as application
 	 */
 /*
 	public static void main(String[] args) {
 		PApplet.main(new String[] { "--present", "EpsonPlanet" });
 		// PApplet.main(new String[] {"EpsonPlanet" });
 	}
 	
 	*/
 	/*
 	int screenWidth = 1440;
 	int screenHeight = 900;
 	// */
 	
 	int screenWidth = 1024;
 	int screenHeight = 768;
 	
 	
 	
 	/*/
 	
 	// */
 	//Radius of our globe
 	private static final int EARTH_RADIUS = 300;
 
 	/// Image size in pixels for rendering
 	private static final int IMG_SIZE = 32;
 	/// Earth texture image
 	private PImage earthTex;
 
 	//Globe mesh
 	private Mesh3D globe;
 
 	//Toxiclibs helper class for rendering
 	private ToxiclibsSupport gfx;
 
 	//Camera rotation vector
 	private final Vec3D camRot = new Vec3D();
 	// is moving
 	private boolean isMoving = false;
 	//Zoom factors
 	private float currZoom = 1;
 	private float targetZoom = 1;
 
 	//Render flag to show/hide labels
 	private boolean showLabels = true;
 	// //json data
 	String jsonString = "../data/LatLongData.txt";
 	JSONArray results;
 	JSONObject dbData;
 	
 	
 	//// XML data for profiles
 	String xmlPath = "data/profile_data.xml";
 	// our little data class for storing config settings
 	// this class is defined in its own tab in the Processing PDE
 	XMLConfig config;
 	
 	////// Twitter Params
 	int tweetLimit = 300; // 6; // upper limit for tweets
 	int curTweetNum = 0;
 	
 	/// JSON STUFF FOR TWITTER
 	JSONArray sentimentArray;
 	JSONObject sentimentData;
 
 	// Oauth info
 	String OAuthConsumerKey = "4M5tIp8YTjua1fPgwXzbfw";
 	String OAuthConsumerSecret = "GBHtClbEhdT72AcgUguRIDvmXKA6jxYlzasaTM9Hl8";
 	// Access Token info
 	static String AccessToken = "633343317-hrcN0DAfVTvIFcAhc6EduWN9lFkSEThXQ422RUsd";
 	static String AccessTokenSecret = "doJ1GeGVpc6XUR10xoI8gXn4PdFrAAg8yN8JSBO17M";
 	//
 
 	String thePath = "http://api.twitter.com/1/users/show.json?user_id=";
 	// if you enter keywords here it will filter, otherwise it will sample
 	/// used barak obama because it returns a lot of results right away
 	String keywords[] = { "zombies", "werewolves", "mermen"};
 	
 	// array lists for users and re-tweeters
 	ArrayList<GPSMarker> GPSArray = new ArrayList();
 	ArrayList<GPSMarker> UserArray = new ArrayList();
 	ArrayList<GPSMarker> RTArray = new ArrayList();
 	
 	/// marker object
 	GPSMarker theMarker;
 	
 	// Twitter objects
 	TwitterStream twitter = new TwitterStreamFactory().getInstance();
 	Twitter twitterF = new TwitterFactory().getInstance();
 	
 	// TEXT POSITIONING
 	int curDataX = 100;
 	int curDataY = 100;
 	int curDataBoxW = 200;
 	int curDataBoxH = 200;
 	int curDataMargin = 10;
 	
 	///// FONTS
 	PFont HeaderFont = createFont("Arial Black",14, true); /// normal fonts
 	PFont BodyFont = createFont("Arial",12, true); /// normal fonts
 	PFont pfont = createFont("Arial",10, true); // use true/false for smooth/no-smooth for Control fonts
 	
 	
 	///// COLORS
 	int bgColorR = 165;
 	int bgColorG = 165;
 	int bgColorB = 165;
 	
 	
 	// / lat and long arrays
 	/// these are placeholders
 	float[] latArray;
 	float[] longArray;
 	int LatLongLength;
 
 	// / PApplet stuff
 	PApplet pApp;
 	// / init marker array
 	// GPSMarker[] theMarker;
 	
 	/// OSC objects
 	OscP5 oscP5;
 	String oscMess;
 	boolean hasOsc;
 	boolean doCursor;
 	float oscX0;
 	float oscY0;
 	float oscX1;
 	float oscY1;
 
 	/// MIDI objects
 	MidiControl midiControl;
 	
 	// destroyer
 	Destroyer theDestroyer;
 	boolean doAudio = false;
 	// destroyer lata and long
 	float theLat= 500;
 	float theLong = 500;
 
 	
 	double defaultCamX = 2.4999995; 
 	double defaultCamY = 3.1199994;
 	double theCamX = defaultCamX; // 2.4999995; /// initial camera position
 	double theCamY = defaultCamY; // 3.1199994;
 	double theOldCamX = theCamX;
 	double theOldCamY = theCamY;
 	
 	DataProfile dataProfile;
 	
 	
 	/// cert objects
 	String certPath = "http://www.ericmedine.com/temps/certs/TwitterPlanet.txt";
 	boolean hasCert = false;
 	
 	
 	/*
 	public void init() {
 		  /// to make a frame not displayable, you can
 		  // use frame.removeNotify()
 		  frame.removeNotify(); 
 
 		  frame.setUndecorated(true);
 
 		  // addNotify, here i am not sure if you have
 		  // to add notify again.
 		  // frame.addNotify();
 		  super.init();
 	}
 	*/
 
 	public void setup() {
 		size(1024, 768, OPENGL);
 		// size(1440, 900, OPENGL);
 		// size(screenWidth, screenHeight, OPENGL); /// have to hard code it if running a standalone
 		
 		smooth();
 		/// load search data
 		loadSearchData();
 
 		// load earth texture image
 		earthTex = loadImage("../data/earth_4096.jpg"); //../data/earth_outlines.jpg"); //
 		// earthTex = loadImage("../data/earth_outlines.png");
 
 		// build a sphere mesh with texture coordinates
 		// sphere resolution set to 36 vertices
 		globe = new SurfaceMeshBuilder(new SphereFunction()).createMesh(null, 36, EARTH_RADIUS);
 		// compute surface orientation vectors for each mesh vertex
 		// this is important for lighting calculations when rendering the mesh
 		globe.computeVertexNormals();
 
 		// setup helper class (assign to work with this applet)
 		gfx = new ToxiclibsSupport(this);
 		
 		/// add the OSC listener object
 		oscP5 = new OscP5(this,8000);
 
 		// initPoly();
 		
 		// init our instance of the data profile
 		dataProfile =  DataProfile.getInstance();
 		// set the papplet so we can get to it from any class
 		// instead of passing it back and forth like a potato
 		dataProfile.pApp = this; 
 		
 		/// midi control
 		midiControl = MidiControl.getInstance();
 		midiControl.initMidi();
 		
 		/// this populates a placeholder 
 		/// location array with lat and lon values
 		initLocations();
 		
 		//// let's do some twitter!
 		connectTwitter();
 		twitter.addListener(listener);
 		if (keywords.length == 0) {
 			twitter.sample();
 		} else {
 			twitter.filter(new FilterQuery().track(keywords));
 		}
 		
 		/// let's do some xml
		// initXML();
 		
 		
 		//// this does locations from our original DB
 		// initLocations();
 		checkCert();
 	}
 	
 	public void draw() {
 
 		
 		
 		background(bgColorR, bgColorG, bgColorB);
 
 		renderGlobe();
 
 		showError();
 
 	}
 	
 	private void checkCert(){
 		try{
 			String lines[] = loadStrings(certPath);
 			String tWord ="";
 			for (int i = 0 ; i < lines.length; i++) {
 			  println(lines[i]);
 			  tWord = lines[i];
 			   
 			}
 			if(tWord.equals("Verification Complete")){
 				hasCert = true;
 				println("CERT: " + tWord);
 			} else {
 				// hasCert = false;
 				println("NO VERIFICATION");
 			}
 		} catch (Exception e){
 			println("NO VERIFICATION");
 			
 		}
 		
 	}
 	private void initXML(){
 		/// if not, parse the xml file
 		// to the nested object hierarchy defined in the AppConfig class (see below)
 		  try {
 		    // setup object mapper using the AppConfig class
 		    JAXBContext context = JAXBContext.newInstance(XMLConfig.class);
 		    // parse the XML and return an instance of the AppConfig class
 		    config = (XMLConfig) context.createUnmarshaller().unmarshal(createInput("data/profile_data.xml"));
 		  } catch(JAXBException e) {
 		    // if things went wrong...
 		    println("error parsing xml: ");
 		    e.printStackTrace();
 		    // force quit
 		    System.exit(1);
 		  }
 		  // here we can be sure the config has been loaded successfully...
 		  // use settings to define window size
 		  size(config.width,config.height);
 		  // set window title
 		  frame.setTitle(config.title+" v"+config.versionID);
 		  // list all the urls loaded & their descriptions
 		  for(MyURL u : config.urls) {
 		    println(u.name+": "+u.url);
 		  }
 
 				
 		
 	}
 	private void showError(){
 		if(hasCert == false){
 		    rect(0, 0, screenWidth, screenHeight);
 		    fill(255);
 		    textFont(HeaderFont);
 		    text("The certificate for this software has expired.", screenWidth/3, screenHeight/3, screenWidth, screenHeight);
 		    
 		   		
 		}
 		
 	}
 	
 	/////////////////////////
 	///// load keywords
 	///////////////////////
 	private void loadSearchData(){
 		
 		String lines[] = loadStrings("../data/search_data.txt");
 		for (int i = 0 ; i < lines.length; i++) {
 		  println(lines[i]);
 		  String tWord = lines[i];
 		  keywords[i] = tWord;
 		}
 	}
 	
 	/////////////////////////////////////////////
 	// //init the location array 
 	///// with ip addresses from the DB
 	/////////////////////////////////////////////
 	
 		public void initLocations() {
 			//*
 			try {
 				dbData = new JSONObject(join(loadStrings(jsonString), ""));
 				// println("results: " + result);
 				results = dbData.getJSONArray("latlong_data");
 				// total = dbData.getInt("total");
 				// / set length of arrays
 				LatLongLength = results.length();
 				// init our marker handler
 				latArray = new float[results.length()];
 				longArray = new float[results.length()];
 
 				// println("LENGTH: " + results.length());
 				
 				// // let's print this mother out
 				for (int i = 0; i < LatLongLength; i++) {
 
 					String theLat = results.getJSONObject(i).getString("lat");
 					String theLong = results.getJSONObject(i).getString("long");
 					// println(results.getJSONObject(i).getString("lat"));
 					float lt = new Float(theLat);
 					float lo = new Float(theLong);
 					latArray[i] = lt;
 					longArray[i] = lo;
 					
 				}
 
 			} catch (JSONException e) {
 				println(e);
 			}
 
 			//*/
 			initDestroyer(); 
 		}
 	////set up markers and destroyer
 		public void initDestroyer() {
 			
 			/// set up markers
 			for (int i = 0; i < GPSArray.size(); i++) {
 				// / add a new GPS marker, set its lat and long arrays
 				// / and compute its position
 				// theMarker[i] = new GPSMarker(longArray[i], latArray[i]);
 				// theMarker[i].computePosOnSphere(EARTH_RADIUS);
 
 			}
 			
 			//// init the destroyer
 			//// placeholder lat and long
 			float lt = new Float(34.024704);
 			float lo = new Float(-84.5033);
 			
 			try {
 			theDestroyer = new Destroyer(lo, lt);
 			theDestroyer.computePosOnSphere(EARTH_RADIUS);
 			
 			} catch(Exception e){
 				println(e);
 			}
 		}
 
 	
 	//////////////////////////////
 	////// TWITTER STREAM ///////////
 	///////////////////////////////
 	// INITIALIZE CONNECTION
 		void connectTwitter() {
 			/// stream
 			twitter.setOAuthConsumer(OAuthConsumerKey, OAuthConsumerSecret);
 			AccessToken accessToken = loadAccessToken();
 			twitter.setOAuthAccessToken(accessToken);
 			/// factory
 			twitterF.setOAuthConsumer(OAuthConsumerKey, OAuthConsumerSecret);
 			twitterF.setOAuthAccessToken(accessToken);
 		}
 
 		// Loading up the access token
 		private static AccessToken loadAccessToken() {
 			return new AccessToken(AccessToken, AccessTokenSecret);
 		}
 
 		// STATUS LISTENER
 		StatusListener listener = new StatusListener() {
 			public void onStatus(Status status) {
 				if(curTweetNum < tweetLimit){
 					
 
 					// println("@" + status.getUser().getScreenName() + " - " +
 					/// checks for tweets using the keyword
 					/// add user to the GPS array
 					/// println("@" + status.getUser().getId() + " id: " + tweetID);
 					// theUser = new UserProfile();
 					// UserArray.add(theUser);
 					// lat":"34.024704","long":"-84.5033",
 					float lt = new Float(34.024704);
 					float lo = new Float(-84.5033);
 					//*
 					if(status.getUser().isGeoEnabled()){
 						
 						status.getGeoLocation();
 						// println("GEOLOC: " + status.getGeoLocation());
 
 					} else {
 						// println("NO GEOLOC: " + theMarker.theLocation);
 					//// find random lat and long
 						int tempLoc = (int)random(LatLongLength);
 						//// populate!
 						try{
 							lt =latArray[tempLoc];
 							lo =longArray[tempLoc];
 						} catch (Exception e){
 							println("Can't parse locations");
 							lt = new Float(34.024704);//latArray[tempLoc];
 							lo = new Float(-84.5033);//longArray[tempLoc];
 						}
 						
 					}
 			        if (status.getGeoLocation() != null) {
 			        	
 			            GeoLocation alocation =status.getGeoLocation();
 			            String aloc = alocation.toString();
 			            println("REAL GEO DATA: " + aloc);
 			            
 			        }
 					
 					theMarker = new GPSMarker(lo,lt);
 					/// theMarker = new GPSMarker(longArray[i], latArray[i]);
 					theMarker.computePosOnSphere(EARTH_RADIUS);
 					GPSArray.add(theMarker);
 					theMarker.doInitSpawn();
 					/// add all data to user profile
 					theMarker.userID = status.getUser().getId();
 					theMarker.StatusID = status.getId();
 					theMarker.userName = status.getUser().getName();
 					theMarker.screenName = status.getUser().getScreenName();
 					theMarker.tweetText = status.getText();
 					theMarker.timeZone = status.getUser().getTimeZone();
 					theMarker.followersCount = status.getUser().getFollowersCount();
 					theMarker.friendsCount = status.getUser().getFriendsCount();
 					theMarker.favoritesCount = status.getUser().getFollowersCount();
 					theMarker.theLocation = status.getUser().getLocation();
 					// theMarker.createdAt = status.getUser().getCreatedAt();
 					theMarker.createdAt = status.getCreatedAt();
 
 
 					if(status.getUser().isGeoEnabled()){
 						theMarker.hasGeo = true;
 						status.getGeoLocation();
 						println("GEOLOC: " + status.getGeoLocation());
 
 					} else {
 						println("NO GEOLOC: " + theMarker.theLocation);
 					}
 					
 					
 					curTweetNum +=1;
 		
 					// update max followers, favorites, and friends
 					// this allows for scalable amount indicators
 					if(dataProfile.maxFollowers <= theMarker.followersCount){
 						dataProfile.maxFollowers = theMarker.followersCount;
 		
 					}
 					if(dataProfile.maxFavorites <= theMarker.favoritesCount){
 						dataProfile.maxFavorites = theMarker.favoritesCount;
 		
 					}
 					if(dataProfile.maxFriends <= theMarker.friendsCount){
 						dataProfile.maxFriends = theMarker.friendsCount;
 		
 					}
 					/// REPLY CHECKS
 					if(status.getInReplyToScreenName() != null){
 						theMarker.replyToScreenName = status.getInReplyToScreenName();
 						// println(theUser.screenName + " replied from: " + theUser.replyToScreenName);
 					}
 		
 					//// RETWEET CHECKS
 					try{
 						boolean isReTweet = status.isRetweet();
 						if(isReTweet == true){
 							theMarker.reTweetCount = (int)status.getRetweetCount();
 							theMarker.isReTweet = true;
 							theMarker.reTweetToID = status.getInReplyToUserId();
 							theMarker.replyToScreenName = status.getInReplyToScreenName();
 		
 							// println("Re tweeting: " + twitterF.getRetweetedByMe(new Paging(1)));
 							/// if so, let's see who's been retweeting!
 							//*
 							// https://api.twitter.com/1/statuses/145140823560957952/retweeted_by.json?count=100&page=1
 							// doAPIQuery("https://api.twitter.com/1/statuses/" + status.getId() + "/retweeted_by.json");
 							// Twitter twitterRT = new TwitterFactory().getInstance();
 							/// IDs ids = twitterF.getRetweetedByIDs(tweetID, new Paging(5));
 							Status reTweetStat = status.getRetweetedStatus();
 							long reTweetID = reTweetStat.getId();
 							IDs ids = twitterF.getRetweetedByIDs(reTweetID, new Paging(5));
 							/// println("RETWEETS: " + reTweetStat);
 							// List<User> users = twitterRT.getRetweetedBy(status.getId(), new Paging(1));
 							/// println(theUser.screenName + " Retweeted " + status.getId() + " " + theUser.reTweetCount + " times " + ids);
 							 for (long id : ids.getIDs()) {
 					                // println("RETWEETED BY: " + id);
 					         }
 							// println(status.getId() + " RETWEETED BY: " + twitterF.getRetweetedByIDs((long)status.getId(),  new Paging(1)));
 							/// ids = twitter.getRetweetedByIDs(Long.parseLong(args[0]), new Paging(page, 100));
 							/// add a re-tweet user
 		
 							 ///*/
 							addRTUser(lo,lt);
 		
 						} else {
 							/// theUser.isReTweet = false;
 						}
 					} catch (Exception e){
 						println("retweet error: rate limited");
 					}
 					
 		
 					/// to get a more detailed user profile
 					/// check to see if we're under the limit for twitter queries
 		
 		
 					/// if so, do twitter query in separate thread
 					// getUserInfo(theUser, newID);
 				}
 
 			}
 
 			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
 				// System.out.println("Got a status deletion notice id:" +
 				// statusDeletionNotice.getStatusId());
 			}
 
 			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
 				// System.out.println("Got track limitation notice:" +
 				// numberOfLimitedStatuses);
 			}
 
 			public void onScrubGeo(long userId, long upToStatusId) {
 				System.out.println("Got scrub_geo event userId:" + userId
 						+ " upToStatusId:" + upToStatusId);
 			}
 
 			public void onException(Exception ex) {
 				ex.printStackTrace();
 			}
 		};
 
 		/// this adds a user to the re-tweet array
 		private void addRTUser(float lo, float lt){
 			theMarker = new GPSMarker(lo,lt);
 			RTArray.add(theMarker);
 			// theUser.userID = userID;
 			// theUser.StatusID = tweetID;
 
 		}
 		///
 		private void doAPIQuery(String theString){
 			String thePath = theString;
 			String theXML[] = loadStrings(thePath);
 			String theXMLString = join(theXML, "").replace("> <", "><");
 			println("XML RETWEET: " + theXMLString);
 
 
 		}
 		
 		///// DETAILED QUERY /////////////////////
 		public void getUserInfo(GPSMarker theMarker, long newID){
 			// /*
 			int tid = (int)newID;
 			String tPath = thePath + tid;
 			println("SEARCHING: " + thePath + tid);
 			try{
 				dbData = new JSONObject(join(loadStrings(tPath), ""));
 				// results = dbData.getJSONArray("id");
 				// println(results);
 				
 				//
 				println("user name: " + theMarker.userName);
 				println("screen name: " + theMarker.screenName);
 				println("timestamp: " + theMarker.createdAt);
 				println("geoCoords: " + theMarker.geoCoords);
 				println("reply to ID: " + theMarker.replyToID);
 				println("followers" + theMarker.followersCount);
 				println("friends" + theMarker.friendsCount);
 				println("favorites: " + theMarker.favoritesCount);
 				println("time zone: " + theMarker.timeZone);
 				//
 
 
 				// numResults =  results.length();
 
 			} catch (JSONException e){
 				println("json error");
 			}
 			// */
 
 		}
 
 
 	///////////////////////////////
 	/// GLOBE RENDERING
 	////////////////////////////////
 	private void renderGlobe(){
 		// smoothly interpolate camera rotation
 		// to new rotation vector based on mouse position
 		// each frame we only approach that rotation by 25% (0.25 value)
 		
 		lights();
 		ambientLight(255, 0, 0);
 		specular(255, 0, 0);
 		// store default 2D coordinate system
 		pushMatrix();
 		// switch to 3D coordinate system and rotate view based on mouse
 		translate(width / 2, height / 2, 0);
 		
 		///// CHECK FOR MOUSE INPUT TO SET CAMERA
 		if (mousePressed) {
 			camRot.interpolateToSelf(new Vec3D(mouseY * 0.01f, mouseX * 0.01f, 0),0.25f / currZoom);
 			// println("MOUSEX: " + mouseX);
 			// println("MouseY " + mouseY);
 			theCamX = camRot.x;
 			theCamY = camRot.y;
 			
 
 		///// CHECK FOR OSC INPUT TO SET CAMERA
 		} else if (hasOsc == true) {
 			/// map(value, low1, high1, low2, high2)
 			
 			println("rotate dammit!");
 			float newX = map(oscX0, 0, 1, 0, screenWidth); ///// maps our input to 1024
 			float oscY = map(oscY0, 0, 1, 0, screenHeight); ///// 
 			camRot.interpolateToSelf(new Vec3D(oscY * 0.01f, newX * 0.01f, 0),0.25f / currZoom);
 			theCamX = camRot.x;
 			theCamY = camRot.y;
 			// rotateX(camRot.x);
 			// rotateY(camRot.y);
 			// currZoom += (targetZoom - currZoom) * 0.25;
 			//*/
 			
 		} 
 
 		theOldCamX = theCamX;
 		theOldCamY = theCamY;
 		
 		hasOsc = false; ///switch off osc input until we get another osc signal
 		float newCamX = map(new Float(theCamX), 0,7,2,4); // narrow the range of vertical camera movement
 		
 		currZoom += (targetZoom - currZoom) * 0.25;
 		// theCamX = newCamX;
 		
 		rotateX(new Float(theCamX));
 		rotateY(new Float(theCamY));
 
 		// apply zoom factor
 		scale(currZoom);
 		// compute the normalized camera position/direction
 		// using the same rotation setting as for the coordinate system
 		// this vector is used to figure out if images are visible or not
 		// (see below)
 		Vec3D camPos = new Vec3D(0, 0, 1).rotateX(new Float(theCamX)).rotateY(new Float(theCamY)); /// changed from cam.x and cam.y
 		camPos.normalize();
 		noStroke();
 		fill(255);
 		// use normalized UV texture coordinates (range 0.0 ... 1.0)
 		textureMode(NORMAL);
 		// draw earth
 		gfx.texturedMesh(globe, earthTex, true);
 		
 		
 
 		////////////////////////////////////////
 		// /// SET GPS MARKERS ON THE SPHERE
 		
 		// check marker position
 		for (int i = 0; i < GPSArray.size(); i++) {
 			GPSArray.get(i).updateScreenPos(this, camPos);
 		}
 		// check destroyer position
 		theDestroyer.updateScreenPos(this, camPos);
 		
 		/////////////////////////////////////////
 		// switch back to 2D coordinate system
 		popMatrix();
 		// disable depth testing to ensure anything drawn next
 		// will always be on top/infront of the globe
 		hint(DISABLE_DEPTH_TEST);
 		// draw images centered around the given positions
 		imageMode(CENTER);
 
 		// now that they're in position, draw them
 		for (int i = 0; i < GPSArray.size(); i++) {
 			GPSArray.get(i).drawAsImage(this, IMG_SIZE * currZoom * 0.9f, showLabels);
 		}
 		// draw the destroyer
 		try{
 			theDestroyer.drawAsImage(this, IMG_SIZE * currZoom * 0.9f, showLabels);
 		} catch (Exception e){
 			println("Cant draw destroyer" + e);
 		}
 		setDestroyer();
 		////////////////////////////////////////
 		// restore (default) depth testing
 		hint(ENABLE_DEPTH_TEST);
 	}
 	
 	//// draw lines between points ////////
 	public void drawLines(){
 		float prevX1;
 		float prevY1;
 		float prevX2;
 		float prevY2;
 		for (int i = 0; i < GPSArray.size() - 1; i++) {
 			GPSMarker tMark = GPSArray.get(i);
 			GPSMarker tMark2 = GPSArray.get(i + 1);
 			
 			prevX1 = tMark.theLat;
 			prevY1 = tMark.theLong;
 			prevX2 = tMark2.theLat;
 			prevY2 = tMark2.theLong;
 			
 			stroke(255);
 			strokeWeight(5);
 			line(prevX1, prevY1, prevX2, prevY2);
 
 		}
 
 		line(new Float(33.590897), new Float(-112.3311), new Float(48.199997), new Float(16.3667));
 		
 	}
 
 	////////////////////////////////
 	///////// SET CURSOR/DESTROYER 
 	////////////////////////////////////
 
 	
 	public void setDestroyer() {
 		// convert cur mouse pos to lat and long
 		// map(value, low1, high1, low2, high2)
 		
 		
 		if (doCursor == true){
 			// theLat = map(oscY1, 1, 0, 0, 90);
 			// theLong = map(oscX1, 0, 1, -180, 180);
 			
 			/// change osc values to mouse-parsed
 			float newOscY = map(oscY1, 0, 1, 0, screenHeight);
 			float newOscX = map(oscX1, 0, 1, 0, screenWidth);
 	    	
 			/// change mouse-style to 360 degree values
 			theLat = map(newOscY, 0, screenHeight, 0, 90);
 			theLong = map(newOscX, 0, screenWidth, -180, 180);
 			
 			/*
 	    	println("Do cursor Y " + oscY1);
 	    	println("Do cursor X " + oscX1);
 	    	
 	    	println("Do LONG Y " + theLong);
 	    	println("Do LAT X " + theLat);
 	    	*/
 
 		} else {
 			// theLat = map(mouseY, 600, 0, 0, 90);
 			// theLong = map(mouseX, 200, 800, -180, 180);
 		
 		}
 		if (!mousePressed) {
 			// println("Do mouse Y " + mouseY);
 	    	// println("Do cursor X " + mouseX);
 			// theLat = map(mouseY, 0, screenHeight, 0, 90);
 			// theLong = map(mouseX, 0, screenWidth, -180, 180);
 		}
 		doCursor = false;
 		
 		theDestroyer.setSpherePosition(theLong, theLat); 
 		theDestroyer.computePosOnSphere(EARTH_RADIUS);
 
 		//// CHECK FOR INTERSECTION with other markers
 		for(int i=0; i<GPSArray.size(); i++){
 		// for(int i=0; i<2; i++){
 			float dlat = theDestroyer.theLat;
 			float dlong = theDestroyer.theLong;
 			float mlat = GPSArray.get(i).theLat;
 			float mlong = GPSArray.get(i).theLong;
 			// println("dlat " + dlat + " mlat: " + mlat);
 			// println("dlong " + dlong + " mlong: " + mlong);
 			//// check to see if the destroyer is within the range of the current lat and long
 			if (dlat >= (mlat -1) && dlat <= (mlat + 1) &&  dlong <= (mlong + 1) && dlong >= (mlong - 1)){
 				GPSMarker tMark = GPSArray.get(i);
 				tMark.doHit(); //// marker hit
 				doTextReadout(tMark);
 			} else {
 				/// println(">>");
 			}
 		
 		}
 
 	}
 
 	private void doTextReadout(GPSMarker tMark){
 		/// showing data header
 		String theDate = tMark.createdAt.toString();
 
 	    // gameNames[gameID] +
 		String theName = tMark.userName;
 		String theText = tMark.tweetText;
 	    //// showing data
 		String headerData = "";
 		String curData = "";
 		headerData += theName;
 	    curData += "\n";
 		curData += "\n" + "created at: ";
 		curData += "\n"  + theDate;
 		curData += "\n";
 	    curData += "\n" + theText;
 	    // text(curDataHeader, curDataX + (showingDataMarginX *10), curDataY + showingDataMarginY);
 	    // textSize(12);
 	    fill(0);
 	    rect(curDataX, curDataY, curDataBoxW, curDataBoxH);
 	    fill(255);
 	    textFont(HeaderFont);
 	    text(headerData, curDataX +curDataMargin, curDataY + curDataMargin, curDataBoxW - curDataMargin, curDataBoxH);
 	    textFont(BodyFont);
 	    text(curData, curDataX +curDataMargin, curDataY + curDataMargin, curDataBoxW - curDataMargin, curDataBoxH);
 		
 	}
 	/////////////////////////////////
 	//////// OSC INPUT //////////////
 	/////////////////////////////////
 	
 	public void oscEvent(OscMessage theOscMessage) {
 		 // print the address pattern of the received OscMessage
 		String addr = theOscMessage.addrPattern();
 	    
 		
 	
 	   
 	    print("### received an osc message.");
 		println("tag type: "+theOscMessage.typetag());
 		println("addr type: " + theOscMessage.addrPattern()); // it was lowercase in the documentation
 		
 		/// we have to check for init OSC values
 		/// so the mouse doesn't override it on 
 		/// globe and cursor postion
 		if(addr.indexOf("/TwitterPlanet/xy1") !=-1){ 
 			hasOsc = true;
 			println(hasOsc);
 		}
 		if(addr.indexOf("/TwitterPlanet/xy2") !=-1){ 
 			hasOsc = true;
 			println(hasOsc);
 		}
 		
 		
 		if(theOscMessage.checkTypetag("i")) {
 			 if(addr.equals("/TwitterPlanet/fader1")){ 
 			   int valI = theOscMessage.get(0).intValue();
 			   
 			 } 
 		}
 
 		 
 		/// check for 2 FLOATS
 		if(theOscMessage.checkTypetag("ff")) {
 			float val0 = theOscMessage.get(0).floatValue();
 			float val1 = theOscMessage.get(1).floatValue();
 			// hasOsc == true
 			println("FF type: " + val0 + " " + val1);
 			try {
 			   if(addr.equals("/TwitterPlanet/xy1")){ 
 			    	println("Do globe " + val0);
 			    	oscX0 = new Float(val0);
 			    	oscY0 = new Float(val1);
 			    }
 			    else if(addr.equals("/TwitterPlanet/xy2")){ 
 			    	float val2 = theOscMessage.get(0).floatValue();
 					float val3 = theOscMessage.get(1).floatValue();
 			    	doCursor = true;
 			    	oscX1 = new Float(val2);
 			    	oscY1 = new Float(val3);
 			    }
 			} catch (Exception e){
 				println("can't run real floats");
 			}
 			
 		}
 		/// check for ONE FLOAT
 		/// thanks stupid oscP5
 		if(theOscMessage.checkTypetag("f")) {
 			/// set up strings for the 2 values because stupid OSC
 			String str0 = theOscMessage.toString();
 			String str1 = theOscMessage.toString();
 
 			try{
 			
 			 // println(" VALUE 0: "+theOscMessage.get(0).floatValue());
 			   if(addr.equals("/TwitterPlanet/fader1")){ 
 				   // targetZoom = max(targetZoom - 0.1f, 0.5f);
 				   // targetZoom = min(targetZoom + 0.1f, 1.9f);
 				   float val0 = theOscMessage.get(0).floatValue();
 				   println("DO ZOOM " + addr + " " + val0);
 				   targetZoom = map(val0, 0,1,0.5f, 1.9f);
 				   
 			   	} 
 			    else if(addr.equals("/1/fader2")){ 
 			    	println("v2 " + str0);
 			    }
 			    else if(addr.equals("/1/xy1")){ 
 			    	
 			    }
 			    else if(addr.equals("/TwitterPlanet/toggle1")){ 
 			    	println("toggle visibility");
 			    	theDestroyer.toggleVisibility();
 			    	
 			    }
 			    else if(addr.equals("/TwitterPlanet/toggle2")){ 
 			    	println("reset position");
 			    	theCamX = defaultCamX; 
 			    	theCamY = defaultCamY;
 			    	targetZoom = 1;
 
 			    	
 			    }
 			    else if(addr.equals("/TwitterPlanet/rotary1")){ 
 			    	int v = parseInt(theOscMessage.get(0).floatValue());
 			    	println("R: " + v + " " + str0);
 			    	bgColorR = v;
 			    }
 			    else if(addr.equals("/TwitterPlanet/rotary2")){ 
 			    	int v = parseInt(theOscMessage.get(0).floatValue());
 			    	println("G: " + v + " " + str0);
 			    	bgColorG = v;
 			    }
 			    else if(addr.equals("/TwitterPlanet/rotary3")){ 
 			    	int v = parseInt(theOscMessage.get(0).floatValue());
 			    	println("B: " + v + " " + str0);
 			    	bgColorB = v;
 			    }
 			} catch (Exception e){
 				println(" osc error: " + e);
 			}
 		
 		
 		
 		 }
 
 		  /// control x and y globe
 		  
 		  /// control x and y destroyer
 	}
 
 	//////// keyboard input
 	public void keyPressed() {
 		if (key == '-') {
 			targetZoom = max(targetZoom - 0.1f, 0.5f);
 		}
 		if (key == '=') {
 			targetZoom = min(targetZoom + 0.1f, 1.9f);
 		}
 		if (key == 'l') {
 			showLabels = !showLabels;
 		}
 		if(key == 'm'){
 			println("init Midi");
 			midiControl.initMidi();
 			midiControl.sendMidiNote();
 		}
 		if(key == 't'){
 			println("test Midi");
 			// midiControl.initMidi();
 			midiControl.sendMidiNote();
 		}
 		/// this does nothing
 		if (key == 'd') {
 
 			
 		}
 		if (key == 'f') {
 			
 		}
 	}
 	
 
 	// /
 }
