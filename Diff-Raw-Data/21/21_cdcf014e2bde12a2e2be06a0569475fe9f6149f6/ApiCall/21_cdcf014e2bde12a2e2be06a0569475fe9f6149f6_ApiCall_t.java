 package me.walkable;
 
 import java.io.FileWriter;
 import java.io.IOException;
import me.walkable.foursquare.*;
import me.walkable.groupon.*;
import me.walkable.yelp.*;
 
 /**
  * 
  */
 
 /**
  * @author Christopher Butera
  * Fri Feb 3, 2012
  *
  * This is a working example of a yelp API call both search or business ID
  *
  */
 public class ApiCall {
 
 	static final String yConsumerKey = "j0sM9V8eFDcYn0PmBTdqTA";
 	static final String yConsumerSecret = "ihXDy_ikwu8DFZEjk4PiK754Y7k"; 
 	static final String yToken = "rw2ExtIjLfYk-BAp2mF-wUwh3-4IUGKi";
 	static final String yTokenSecret = "5yNUcFAF53iGqkD7ROwHskqm2ys";
 	
 	static final String gClientID = "8ad4ef59d1e755157121d9ac5f3ff16aeb89d93d";
 	
 //FourSquare
 //	Client ID GXJ3J33W0NXGVAGGLHBORLNQHL14HCIZNTWKC2XU2RK510VL
 //	Client Secret 1VKEVJQZHMF22ZZTM0WIX05U2KH32T02WJHSINTDHKKOC2CN
 	
 	static final String fourClientID = "GXJ3J33W0NXGVAGGLHBORLNQHL14HCIZNTWKC2XU2RK510VL";
 	static final String fourSecret = "1VKEVJQZHMF22ZZTM0WIX05U2KH32T02WJHSINTDHKKOC2CN";
 	
 	public static void getYelp(){
 //		double lat = 41.9485056;
 //		double lon = -87.6616156;
 
 //		//Yelp Example
 		Yelp yelp = new Yelp(yConsumerKey, yConsumerSecret, yToken ,yTokenSecret);
 		String yResponse;
 //		yResponse = yelp.search("mexican", lat, lon);
 //		yResponse = yelp.getBusiness("uncommon-ground-chicago");
 //		yResponse = yelp.getDeals("Chicago");
 		yResponse = yelp.search("Chicago", "25");
 //		YelpDealObject yObj = YelpParse.parse(yResponse);
 //		System.out.println(yObj.businesses.length + " Yelp deals processed");
 		System.out.println(yResponse);
 		
 		FileWriter writer; // I orginally used BufferedWriter - but it couldn't write everything to the file - kept creating incomplete files.
 		try { 
 			String fileName = "./yelp.json";
 			writer = new FileWriter ( fileName ) ;
 			writer.write(yResponse);
 			writer.close();
 		} catch (IOException x) {
 		    System.err.format("IOException: %s%n", x);
 		}
 
 	}
 	
 	public static void getGroupon(){
 		//Groupon Example
 		Groupon groupon = new Groupon(gClientID);
 		String gResponse = groupon.search("chicago");
 //		System.out.println(gResponse);
 		
 		FileWriter gWriter; // I orginally used BufferedWriter - but it couldn't write everything to the file - kept creating incomplete files.
 		try { 
 			String fileName = "./groupon.json";
 			gWriter = new FileWriter ( fileName ) ;
 			gWriter.write(gResponse);
 			gWriter.close();
 		} catch (IOException x) {
 		    System.err.format("IOException: %s%n", x);
 		}
 
 		GrouponObject obj = GrouponParse.parse(gResponse);
 		GrouponParse.InsertGrouponData(obj);
 		System.out.println(obj.deals.length + " Groupon deals processed");
 	}
 	
 	public static void getFourSquare(){
 		String lat = "41.9485056";
 		String lng = "-87.6616156";
 		
 		//FourSquare Example
 		FourSquare fourSquare = new FourSquare(fourClientID, fourSecret);
 //		fourSquare.getToken();
 		String fourResponse = fourSquare.getDeals(lat, lng);
 //		System.out.println(fourResponse);
 		
 		FileWriter writer; // I orginally used BufferedWriter - but it couldn't write everything to the file - kept creating incomplete files.
 		try { 
 			String fileName = "./fourSquare.json";
 			writer = new FileWriter ( fileName ) ;
 			writer.write(fourResponse);
 			writer.close();
 		} catch (IOException x) {
 		    System.err.format("IOException: %s%n", x);
 		}
 
 	}
 	
 	public static void main(String[] args) {
 
 		getYelp();
 //		getGroupon();
 //		getFourSquare();
 		
 		
 
 	}
 
 }
