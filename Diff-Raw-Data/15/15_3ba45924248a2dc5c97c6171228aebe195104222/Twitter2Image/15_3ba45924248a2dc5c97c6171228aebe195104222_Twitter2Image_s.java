 package net.zombiehookers;
 
 import twitter4j.*;
 import twitter4j.api.*;
 import java.util.List;
 import javax.imageio.ImageIO;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 
 public class Twitter2Image {
 	
 	private List<Tweet> tweets;
 	private TwitterFactory tf;
 	private Twitter t;
 
 	public Twitter2Image() {
 		tf = new TwitterFactory();
 		t = tf.getInstance();
 	}
 
 	public List<Tweet> getTweets() {
 		return tweets;
 	}
 
 	public void updateTweets(double lat, double lng, double radius) {
 		GeoLocation g = new GeoLocation(lat, lng);
 		Query query = new Query();
 		query.setGeoCode(g, radius, Query.MILES);
 	
 		try {	
 			tweets = t.search(query).getTweets();
 		} catch (TwitterException e) {
 		}
 	}
	String filename = "mood_overlay.jpg";
 	private BufferedImage image;
 	private double array[][];
 	private Color clear = new Color(0,0,0,0);
 	
 	public void set(int i, int j, Color c)
 	{
 		image.setRGB(i, j, c.getRGB());
 	}
 	
 	public void Process()
 	{
         	image = new BufferedImage(250, 600, BufferedImage.TYPE_INT_ARGB);
 		for (int a = 0; a < w-1; a ++)
 		{
 			for (int b = 0; b < h-1; b ++)
 			{
 				image.setRGB(a, b, clear.getRGB());
 			}
 		}	
 		for (int a = 0; a < array.length-1; a ++)
 		{
 			double mood = array[a][0];
 			double X = array[a][1];
 			double Y = array[a][2];
 			int temp_X = (int)(10*(X-25));
 			int temp_Y = (int)(10*(Y+125));
 			if (mood == 0){set(temp_X, temp_Y, Color.blue);}
 			else{set(temp_X, temp_Y, Color.red);}
 		}
		ImageIO.write(image,"jpg",filename);
 	}
 
 	//public static void main(String[] args)
 	//{
 	//	Picture pic = new Picture(250, 600);
 	//	ImageIO.write(image,"jpg",filename);
 	//}
 
 }
