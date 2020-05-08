 /**
  Copyright (c) Novedia Group 2012.
  This file is part of Hubiquitus
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  of the Software, and to permit persons to whom the Software is furnished to do so,
  subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in all copies
  or substantial portions of the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
  FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
  You should have received a copy of the MIT License along with Hubiquitus.
  If not, see <http://opensource.org/licenses/mit-license.php>. 
  
 */
 
 package org.hubiquitus.hubotsdk.adapters;
 
 import java.util.Map;
 
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hubotsdk.AdapterInbox;
 import org.hubiquitus.hubotsdk.adapters.HtwitterAdapter.TweetToHMessage;
 import org.hubiquitus.twitter4j_1_1.stream.HUserStream;
import org.hubiquitus.twitter4j_1_1.stream.HUserStreamListener;
 
 
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
public class HTwitterUserStreamAdapterInbox_1_1 extends AdapterInbox implements HUserStreamListener
 {
 	final Logger log = LoggerFactory.getLogger(HTwitterUserStreamAdapterInbox_1_1.class);
 
 	private String consumerKey ;
 	private String consumerSecret;
 	private String twitterAccessToken;
 	private String twitterAccessTokenSecret;
 	private String proxyHost;
 	private int proxyPort;
 	
 	// Stream parameters
 	private String langFilter;
 	private String tags;	
 	private String delimited;
 	private String stallWarnings; 
 	private String with;
 	private String replies;
 	private String locations;
 	private String count;
 
 	private HUserStream userStream;
 	private TweetToHMessage tweetToHMessage = new TweetToHMessage();
 
 	/**
 	 * The map where the JSONObject's properties are kept.
 	 */
 	@SuppressWarnings({ "rawtypes", "unused" })
 	private Map map;
 
 	@Override
 	public void setProperties(JSONObject properties) {
 
 		if(properties != null){
 			try{
 				if(properties.has("consumerKey")){
 					this.consumerKey = properties.getString("consumerKey");
 				}
 				if (properties.has("consumerSecret")) {
 					this.consumerSecret = properties.getString("consumerSecret");
 				}
 				if (properties.has("twitterAccessToken")) {
 					this.twitterAccessToken = properties.getString("twitterAccessToken");
 				}
 				if (properties.has("twitterAccessTokenSecret")) {
 					this.twitterAccessTokenSecret = properties.getString("twitterAccessTokenSecret");
 				}
 				if (properties.has("proxyHost")) {
 					this.proxyHost = properties.getString("proxyHost");
 				}				
 				if (properties.has("proxyPort")) {
 					this.proxyPort = properties.getInt("proxyPort");
 				}				
 				if (properties.has("langFilter")) {
 					this.langFilter = properties.getString("langFilter");
 				}
 				if (properties.has("tags")) {
 					this.tags = properties.getString("tags");
 				}
 				if (properties.has("delimited")) {
 					this.delimited = properties.getString("delimited");
 				}
 				if (properties.has("stallWarnings")) {
 					this.stallWarnings = properties.getString("stallWarnings");
 				}
 				if (properties.has("with")) {
 					this.with = properties.getString("with");
 				}
 				if (properties.has("replies")) {
 					this.replies = properties.getString("replies");
 				}
 				if (properties.has("locations")) {
 					this.locations = properties.getString("locations");
 				}
 				if (properties.has("count")) {
 					this.count = properties.getString("count");
 				}
 				
 				
 			}catch(JSONException e){
 				log.error("ERROR ON CONFIG FILE 'JSON NOT VALID', ERROR TYPE : ",e);
 			}
 		}
 		log.info("Properties Initialized : " + this);
 	}
 
 	/**
 	 * Function to start Streaming
 	 */
 	@Override
 	public void start() {
 		log.info("Starting...");
 		 
 		userStream = new HUserStream (
 			   	 proxyHost,  
 				 proxyPort, 
 		         tags,
 		         delimited,
 		         stallWarnings,
 		         with, 
 		         replies, 
 		         locations,
 		         count ,
 		         consumerKey, 
 		         consumerSecret, 
 		         twitterAccessToken, 
 		         twitterAccessTokenSecret);
 		
 		userStream.addListener(this);		
 		userStream.start();
 		log.info("Started.");  
 	}
 
 	@Override
 	public void stop() {
 		log.info("Stopping...");
 		if (userStream != null)
 			userStream.stop();
 		log.info("Stopped.");	
 	}
 
 
 
 	@Override
 	public String toString() {
 		return "HTwitterAdapterInbox [consumerKey=" + consumerKey
 				+ ", consumerSecret="      + consumerSecret
 				+ ", twitterAccessToken="  + twitterAccessToken
 				+ ", twitterAccessTokenSecret=" + twitterAccessTokenSecret
 				+ ", proxyHost="   + proxyHost
 				+ ", proxyPort="   + proxyPort
 				+ ", langFilter="  + langFilter + ", tags=" + tags + "]";
 	}
 	
 	public void onStatus(JSONObject status) {
 		
 		log.info("received status: " + status);
 		try {
 			String lang = (status.getJSONObject("user")).getString("lang");
 			log.debug("My Lang "+lang);
 			if ((langFilter == null)|| (lang != null && lang.equalsIgnoreCase(langFilter))) {
 				HMessage message = tweetToHMessage.transformtweet(status);
 				put(message);
 			}
 
 		} catch (JSONException e) {
 			log.debug(" Error on get lang :",e);
 			e.printStackTrace();
 		}		
 	}
 
 	public void onStallWarning(JSONObject stallWarning) {
 		log.warn("received stallWarning: " + stallWarning);
 	}
 	@Override
 	public void onStatusDeletionNotices(JSONObject delete) {
 		log.warn("received DeletionNotices : " + delete);		
 	}	
 
 	@Override
 	public void onLocationDeletionNotices(JSONObject scrubGeo) {
 		log.info("received location deletion notices message: " + scrubGeo);
 	}
 	@Override
 	public void onLimitNotices(JSONObject limit) {
 		log.info("received User limit notices message: " + limit);
 
 	}
 
 	@Override
 	public void onStatusWithheld(JSONObject statusWithheld) {
 		log.info("received User status Withheld message: " + statusWithheld);
 	}
 
 	@Override
 	public void onUserWithheld(JSONObject userWithheld) {
 		log.info("received User Withheld message: " + userWithheld);
 
 	}
 
 	@Override
 	public void onDisconnectMessages(JSONObject disconnect) {
 		log.info("received other discounnect message: " + disconnect);
 
 	}
 	public void onOtherMessage(JSONObject message) {
 		log.info("received other message: " + message);
 	}
 }
