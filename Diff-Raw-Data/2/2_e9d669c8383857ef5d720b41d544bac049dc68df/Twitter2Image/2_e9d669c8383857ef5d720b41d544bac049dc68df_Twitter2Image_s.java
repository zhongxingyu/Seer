 package net.zombiehookers;
 
 import twitter4j.api.*;
 import java.util.List;
 
 
 public class Twitter2Image {
 	
 	private List<TweetMood> tweets;
 	private TwitterFactory tf;
 	private Twitter t;
 
 	public Twitter2Image() {
 		tf = new TwitterFactory();
 		t = tf.getInstance();
 	}
 
 	public List<TweetMood> getTweets() {
 		return tweets;
 	}
 
 	public void updateTweets(double lat, double lng, double radius) {
		GeoLocation g = new GeoLocations(lat, long);
 		Geocode query = new Geocode();
 		query.setGeoCode(g, radius, Query.MILES);
 		
 		tweets = t.search(query).getTweets();
 	}
 }
