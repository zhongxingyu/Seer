 package net.zombiehookers;
 
 import twitter4j.*;
 import twitter4j.api.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Twitter2Image {
 	
 	private List<Emoxel> emoxels;
 	private TwitterFactory tf;
 	private Twitter t;
 
 	public Twitter2Image() {
 		tf = new TwitterFactory();
 		t = tf.getInstance();
 		emoxels = new ArrayList<Emoxel>();	
 	}
 
 	public List<Emoxel> getTweets() {
 		return emoxels;
 	}
 
 	public void updateTweets(double lat, double lng, double radius) {
 		GeoLocation g = new GeoLocation(lat, lng);
 		Query query = new Query();
 		query.setGeoCode(g, radius, Query.MILES);
 		List<Tweet> tweets;
 	
 		try {	
 			tweets = t.search(query).getTweets();
 		} catch (TwitterException e) {
 			tweets = new ArrayList<Tweet>();
 		}
 
 		for(int i = 0; i < tweets.size(); i++)
 		{
 			Emoxel e = new Emoxel();
			GeoLocation geo = tweets.get(i).getGeoLocation();
			if(geo != null)
			{
					e.lat = geo.getLatitude();
					e.lng = geo.getLongitude();
					e.e = 1.0f;
					emoxels.add(e);
			}
 		}
 	}
 }
