 package com.dotcom.nextup.yelp;
 
 
 import java.util.ArrayList;
 
 import org.json.JSONObject;
 
 import android.util.Log;
 
 /* search by category (and your lat long) - 3 best things as YelpVenue.
  * attributes of YelpVenue:  name, address, lat/long, five star rating, price range, categories
  * 
  * location: distance from user, then they click on it for more info
  * 
  */
 
 public class YelpVenue {
 	// attributes mimic http://www.yelp.com/developers/documentation/v2/business
 	
 	ArrayList<ArrayList<String>> categories = new ArrayList<ArrayList<String>>();
 	/* ex. [["Local Flavor", "localflavor"],
 	 *      ["Active Life", "active"],
 	 *      ["Mass Media", "massmedia"]]
 	 * first in each sublist is human readable,
 	 * second in each sublist if for Yelp category search
 	 */ 
 	
 	// id, name, url, phone, rating_img_url_small, image_url, address can be (null or empty string) on error
 	String id;          // Yelp ID for this business
 	String name;
 	double rating;
 	int review_count;
 	String mobile_url;
 	String url;
 	String phone; 
 	String rating_img_url_small;
 	String image_url;
 	String address;     // human readable address
 	double distance;    // distance from search location
 	double latitude;
 	double longitude;
 	boolean is_closed;  // whether the business is permanently closed
 	
 	// web scraped attributes
 	String price_range; // '$', '$$', ... ,'$$$$$'
 	String hours;       // human readable description of hours
 	
 	public YelpVenue() {
 		
 	}
 	
 	public YelpVenue(JSONObject jbus) {
 		// makes a YelpVenue object out of a Yelp search result business JSON object
 		Log.v("Yelp", jbus.toString());
 		this.name = jbus.optString("name");
 		this.id = jbus.optString("id");
 		this.categories = this.stringToCategories(jbus.optString("categories"));
 		this.image_url = jbus.optString("image_url");
 		this.mobile_url = jbus.optString("mobile_url");
 		this.url = jbus.optString("url");
 		this.phone=jbus.optString("phone"); 
 		this.rating_img_url_small = jbus.optString("rating_img_url_small");
 		this.initializeRating();
 		this.initializeAddressLocation(jbus.optJSONObject("location"));
 		this.review_count = Integer.parseInt(jbus.optString("review_count"));
 		this.distance = jbus.optDouble("distance");    // distance from search location if available
 		
 		/* web scraping makes it really slow and sometimes run out of memory
 		this.hours = YelpWebScrape.getYelpVenueInfo(this.url, "hours");
 		this.price_range = YelpWebScrape.getYelpVenueInfo(this.url, "price range");
 		*/	
 	}
 	
 	private void initializeRating() {
 		/* rating_img_url_small is expected to be something like
 		 * http://media3.px.yelpcdn.com/static/muchmessiness/i/ico/stars/stars_small_4.png
 		 * or http://media3.px.yelpcdn.com/static/a;sldkjfasd;fkas;dfk/i/ico/stars/stars_small_4_half.png
 		 */
 		int len = rating_img_url_small.length();
 		String piece = this.rating_img_url_small.substring(len - 10); // x_half.png or mall_x.png
 		Log.v("Yelp", "rating piece " + piece);
 		rating = 0;
 		String x = null;
 		Log.v("Yelp", "maybe half " + piece.substring(2,3));
 		if ( piece.substring(2, 3).equals("h") ) {
 			Log.v("Yelp", "case: half");
 			rating += 0.5;
 			x = piece.substring(0, 1);
 		}
 		else {
 			Log.v("Yelp", "case: not half");
 			x = piece.substring(5, 6);
 		}
 		Log.v("Yelp", "x " + x);
 		if ( x.equals("0") ) rating += 0;
 		if ( x.equals("1") ) rating += 1;
 		if ( x.equals("2") ) rating += 2;
 		if ( x.equals("3") ) rating += 3;
 		if ( x.equals("4") ) rating += 4;
 		if ( x.equals("5") ) rating += 5;
 		Log.v("Yelp", "rating " + Double.toString(rating));
 	}
 	
 	public String toString() {
 		return "Yelp Venue " + this.name;
 	}
 	
 	private void initializeAddressLocation(JSONObject loc) {
 		this.address = this.stringToAddress(loc.optString("display_address"));
 		JSONObject coord = loc.optJSONObject("coordinate");
 		if (coord != null) {
 			latitude = coord.optDouble("latitude");
 			longitude = coord.optDouble("longitude");
 		}	
 	}
 	
 	private String stringToAddress(String loc) {
 		ArrayList<String> temp = stringToList(loc);
 		String res = "";
 		for (int i = 0; i < temp.size(); i++) {
 			res = res + temp.get(i) + " ";
 		}
 		return res;
 	}
 
 	private ArrayList<String> stringToList(String str) {
 		String[] pieces = str.split("[\\Q[]\\E, \"]+");
 		ArrayList<String> better = new ArrayList<String>();
 		String piece;
 		for (int i = 0; i < pieces.length; i++) {
 			piece = pieces[i];
			if (!(piece.length() == 0)) {
 				better.add(piece);
 			}
 		}
 		return better;
 	}
 	
 	private ArrayList<ArrayList<String>> stringToCategories(String cats) {
 		/* ex. [["Local Flavor", "localflavor"],
 		 *      ["Active Life", "active"],
 		 *      ["Mass Media", "massmedia"]]
 		 * first in each sublist is human readable,
 		 * second in each sublist if for Yelp category search
 		 */ 
 		ArrayList<String> many_cats = this.stringToList(cats);
 		ArrayList<ArrayList<String>> clowder = new ArrayList<ArrayList<String>>(); // "a clowder of cats"
 		ArrayList<String> pair_of_cats = new ArrayList<String>();
 		int i = 1;
 		while (i < many_cats.size() - 1) {
 			pair_of_cats.clear();
 			pair_of_cats.add(many_cats.get(i));
 			pair_of_cats.add(many_cats.get(i+1));
 			if (!clowder.contains(pair_of_cats)) {
 				clowder.add(pair_of_cats);
 			}
 			i = i + 2;
 		}
 		return clowder;
 	}
 	
 	public String getName() { return name; }
 	public String getId() { return id; }
 	public double getLatitude() { return latitude; }
 	public double getLongitude() { return longitude; }
 	public String getImageURL() { return image_url; }
 	public String getURL() { return url; }
 	public double getRating() { return rating; }
 	public int getReviewCount() { return review_count; }
 	public double getDistance() { return distance; }
 	public void setDistance(double d) { distance = d; }
 
 }
