 package controllers;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import models.DisplayLogger;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.Logger;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.WebSocket;
 import play.mvc.WebSocket.Out;
 import twitter4j.FilterQuery;
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.Tweet;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 import twitter4j.conf.ConfigurationBuilder;
 
 public class AppTwitterController extends Controller {
 
 	public static Result twitterSmall() {	
 
 		return ok(views.html.appTwitterSmall.render("Twitter Small."));
 	}//twitterSmall()
 
 
 	public static HashMap<String, Sockets> displaySockets = new HashMap<String, Sockets>();
 	public static HashMap<WebSocket.Out<JsonNode>, String> displaySocketReverter = new HashMap<WebSocket.Out<JsonNode>, String>();
 
 	public static int numberOfDisplaySockets = 0;
 
 	public static TwitterStream twitterStream;	//twitter stream
 	public static Twitter twitter;	//twitter search
 	public static int numberOfTweets = 0; 
 	public static List<Tweet> tweets; //a list of tweets
 	public static boolean firstSchedulerRun = true;
 
 
 	public static void initTweeterSearch(){
 		Logger.info("AppTwitterController.initTweeterSearch(): start initialization!");
 		twitter = new TwitterFactory().getInstance();
 		Logger.info("AppTwitterController.initTweeterSearch(): init done!------------");
 	}//initTweeterSearch(){
 
 	public static void startTwitterStream(){
 		try {
 			//start twitter stream
 			startTwitterStreamFeeds();
 		} catch (TwitterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}//startTwitterStream()
 
 	public static void stopTwitterStream(){
 		twitterStream.cleanUp();
 		twitterStream.shutdown();
 	}//stopTwitterStream()
 
 	public static void serachForTweets(){
 		//search for tweets	  
 		try {
 			Query query = new Query("#usiwelcome");
 			QueryResult result;
 			//do {
 			result = twitter.search(query);
 			tweets = result.getTweets();
 			for (Tweet tweet : tweets) {
 				Logger.info("AppTwitterController.serachForTweets():     @" + tweet.getFromUser() + " - " + tweet.getText());
 				//System.out.println("     @" + tweet.getFromUser() + " - " + tweet.getText());
 			}
 			//} while ((query = result.nextQuery()) != null);
 		} catch (TwitterException te) {
 			te.printStackTrace();
 			System.out.println("Failed to search tweets: " + te.getMessage());
 			System.exit(-1);
 		}
 
 	}//serachForTweets(){
 
 
 	public static void sendTweets(int fromIndex, int toIndex, WebSocket.Out<JsonNode> out){
 		Logger.info("AppTwitterController.sendTweets() ------ send tweets to server");
 		if(tweets.size() < fromIndex){
 			fromIndex = tweets.size(); 
 		}
 		
 		for(int i=fromIndex; i>toIndex ;i--){
 			Logger.info("AppTwitterController.sendTweets() tweet#:"+i);
 			Tweet currentTweet = tweets.get(i-1);
 			if(currentTweet != null){
 				ObjectNode msg = Json.newObject();
 				msg.put("kind", "newTweet");
 				msg.put("user", currentTweet.getFromUser());
 				msg.put("userName", currentTweet.getFromUserName());
 				msg.put("text", currentTweet.getText());
 				msg.put("time", currentTweet.getCreatedAt().getTime());
 				out.write(msg);
 			}
 			Logger.info("AppTwitterController.sendTweets() - send tweet to server");
 		}//for		
 	}//sendTweets
 
 	//starts with application
 	public static void startTwitterScheduler(){
 		Logger.info("AppTwitterController.scheduler() ---- START twitter scheduler ---");
 		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 10, 30, SECONDS);
 		scheduler.schedule(new Runnable() {
 			public void run() { beeperHandle.cancel(true); }
 		}, 1, TimeUnit.DAYS);
 	}
 
 	//ends with applicaiton
 	public static void stopTwitterScheduler(){
 		Logger.info("AppTwitterController.scheduler() ---- STOP twitter scheduler ---");
 		scheduler.shutdown();
 	}
 
 	public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 	final static Runnable beeper = new Runnable() {
 		public void run() { 
 
 			Logger.info("AppTwitterController.scheduler(): check if there is interctivity, and do something");
 			//might be better to do that from the javascript
 
 		}//run
 	};//bipper
 
 	// STREAM - will fix it later
 	public static void startTwitterStreamFeeds() throws TwitterException, IOException{
 
 		Logger.info("AppTwitterController.startTwitterStreamFeeds(): start!");
 
 
 		
 		//twitter stream
		//twitterStream = new TwitterStreamFactory(cb.build()).getInstance(); 
 		StatusListener listener = new StatusListener() {
 
 			@Override
 			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
 				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
 			}
 
 			@Override
 			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
 				System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
 			}
 
 			@Override
 			public void onScrubGeo(long userId, long upToStatusId) {
 				System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
 			}        
 
 			@Override
 			public void onException(Exception ex) {
 				Logger.info("AppTwitterController.startTwitterStreamFeeds().onException: Stream is complaining!");
 				Logger.info(ex.toString());
 				//ex.printStackTrace();
 			}
 
 			@Override
 			public void onStatus(twitter4j.Status arg0) {
 				Logger.info("AppTwitterController.startTwitterStreamFeeds().onStatus: there is a new tweet...");
 				System.out.println("        @" + arg0.getUser().getScreenName() + " - " + arg0.getText());  
 
 				ObjectNode msg = Json.newObject();
 				msg.put("kind", "newTweet");
 				msg.put("user", arg0.getUser().getScreenName());
 				msg.put("userName", arg0.getUser().getName());
 				msg.put("text", arg0.getText());
 				msg.put("time", arg0.getCreatedAt().getTime());
 				Logger.info("AppTwitterController.twitterFeeds() - send the new tweet to all clients");
 
 				String tw = arg0.getUser().getScreenName()+":"+arg0.getText();
 				DisplayLogger.addNew(new DisplayLogger("Twitter", "tweetNew", new Date().getTime(), "SYS",tw,"null"));
 
 				Set<?> set = displaySockets.entrySet();
 				// Get an iterator
 				Iterator<?> i = (Iterator<?>) set.iterator();
 				// Display elements
 				while(i.hasNext()) {
 					Map.Entry ds = (Map.Entry)i.next();
 					Logger.info("AppTwitterController.twitterFeeds(): sand the new tweet to displayID="+ds.getKey()+" socket="+ds.getValue().toString());
 					String did = (String) ds.getKey();
 					DisplayLogger.addNew(new DisplayLogger("Twitter", "tweetNew", new Date().getTime(), "SYS","send to display -> ",did));
 					displaySockets.get(ds.getKey()).wOut.write(msg);
 				}//while 
 
 			}//onStatus	
 
 		};//new StatusListener() 
 
 		twitterStream.addListener(listener);
 		FilterQuery aquery = new FilterQuery();
 		aquery.count(0);
 		String tr[] = {"#usiwelcome"};
 		aquery.track(tr); 
 		twitterStream.filter(aquery);
 
 	}//twitterFeeds()
 
 	public static WebSocket<JsonNode> webSocket() { 
 		return new WebSocket<JsonNode>() {
 
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out){
 
 				Logger.info("AppTwitterController.webSocket(): --- there is a websocket connection!!!");
 
 				// For each event received on the socket 
 				in.onMessage(new Callback<JsonNode>() { 
 					public void invoke(JsonNode event) {
 
 						String messageKind = event.get("kind").asText();						
 
 						if(messageKind.equals("appReady")){
 							Logger.info("AppTwitterController.webSocket(): appReady - displayID="+event.get("displayID")+" size="+event.get("size"));
 							//save the connection for later use
 							if(!displaySockets.containsKey(event.get("displayID"))){
 								Logger.info("AppTwitterController.webSocket(): new display is connected");
 								//search for tweets
 								serachForTweets();
 								//send tweets to the client - display
 								sendTweets(5, 0 , out);			
 
 								//register display for twitter stream
 								displaySockets.put(event.get("displayID").asText(), new Sockets(out));
 								displaySocketReverter.put(out, event.get("displayID").asText());
 
 								String displayid = event.get("displayID").asText();
 								DisplayLogger.addNew(new DisplayLogger("Twitter", "displayNew", new Date().getTime(), "SYS", "", displayid));
 
 							}
 
 						}//appReady
 
 						if(messageKind.equals("appClose")){
 							Logger.info("AppTwitterController.webSocket(): appClose - displayID="+event.get("displayID")+" size="+event.get("size"));
 						}
 
 					}//invoke
 				});//in.onMessage
 
 				// When the socket is closed. 
 				in.onClose(new Callback0() {
 					public void invoke() { 
 						String displayID =displaySocketReverter.get(out);
 						displaySocketReverter.remove(out);
 						displaySockets.remove(displayID);
 						DisplayLogger.addNew(new DisplayLogger("Twitter", "displayDisconect", new Date().getTime(), "SYS","", displayID));
 						Logger.info("AppTwitterController.webSocket(): display "+displayID+" is disconnected!!!");
 						Logger.info("AppTwitterController.webSocket(): number of connected displays: "+displaySockets.size());
 					}
 				});//in.onClose
 
 			}//onReady
 		};//WebSocket<String>()
 	}//webSocket() { 
 
 	public static class Sockets {
 		public WebSocket.Out<JsonNode> wOut;
 
 		public Sockets(Out<JsonNode> out) {
 			this.wOut = out;
 		}
 	}
 
 }//controller
