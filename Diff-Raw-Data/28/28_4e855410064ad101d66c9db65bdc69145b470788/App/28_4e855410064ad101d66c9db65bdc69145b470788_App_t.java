 package net.cosm2sml;
 
import net.cosm2sml.helper.impl.CosmFeedHelperImpl;
import net.cosm2sml.sensorml.sensorML;

 /**
  * Hello world!
 * 
  */
public class App {
	public static void main(String[] args) {
		CosmFeedHelperImpl helper = new CosmFeedHelperImpl();
		CosmFeed feed = helper.parseFeed(126768);
		sensorML ml = helper.decodeFeed(feed);
		// use ml

	}
 }
