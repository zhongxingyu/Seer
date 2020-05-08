 package com.theluvexchange.android;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 
 import com.theluvexchange.weather.WeatherSet;
 
 /**
  * @author Tristan Maschke
  * 
  * This class is meant to serve as the base for providing web service calls to
  * the rest of the program.
  * 
  * Implementation as of 3/5/12 includes just a static method for getting the list of cities
  */
 public class WebService {
 	// Base URL is usually going to start with this
 	private static final String ADDRESS = "http://www.theluvexchange.com/iphone/";
 	public static final int RESTAURANTS = 1;
 	public static final int THINGS_TO_DO = 2;
 	public static final int AIRPORT_EATS = 3;
 	private static SAXParser parser = null; // Singleton SAXParser object
 	private static List<AlbumPhoto> photos = null;
 	// private static List<Drawable> images = null;
 	private static String cityId = null;
 
 	/*
 	 * Singleton object that is used to create XMLReader objects for other service calls.
 	 * This prevents the SAXParser object from having to be created multiple times,
 	 * and boosts responsiveness of all the methods slightly.
 	 */
 	private static SAXParser getParser() {
 		if (parser == null) {
 			try {
 				parser = SAXParserFactory.newInstance().newSAXParser();
 			} catch (Exception e) {
 				Log.e("TheLuvExchange", "WebServiceError", e);
 			}
 		}
 		return parser;
 	}
 
 	/**
 	 * @return true if website is reachable.
 	 */
 	public static boolean ping() {
 		try {
 			HttpURLConnection connection = (HttpURLConnection) new URL(
 					"http://www.theluvexchange.com").openConnection();
 			connection.setRequestMethod("HEAD");
 			int responseCode = connection.getResponseCode();
 			if (responseCode != 200) {
 				return false;
 			}
 		} catch (Exception e) {
 			Log.e("TheLuvExchange", "WebServiceError", e);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method to authenticate login credentials with the website.
 	 * @param userName
 	 * @param password
 	 * @return an Object that will either be an instanceof User or String.
 	 * If the return value is a User, then the login was successful.
 	 * Otherwise the String will be a message explaining why it was not.
 	 */
 	public static Object login(String userName, String password) {
 		User user = null;
 
 		boolean blankUserName = userName == null || userName.trim().equals("");
 		boolean blankPassword = password == null || password.trim().equals("");
 		if (blankUserName && blankPassword) {
 			return "Please enter a user name and password.";
 		}
 		if (blankUserName) {
 			return "Please enter a user name.";
 		}
 		if (blankPassword) {
 			return "Please enter a password.";
 		}
 
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(ADDRESS + "login");
 		try {
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 			pairs.add(new BasicNameValuePair("data[Account][username]", userName));
 			pairs.add(new BasicNameValuePair("data[Account][password]", password));
 
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 
 			XMLReader reader = getParser().getXMLReader();
 
 			LoginHandler handler = new LoginHandler();
 			reader.setContentHandler(handler);
 
 			String response = httpClient.execute(httpPost, new BasicResponseHandler());
 			InputSource input = new InputSource();
 			input.setCharacterStream(new StringReader(response));
 			reader.parse(input);
 
 			user = handler.getUser();
 
 			if (user == null) {
 				return "Authentication failed.";
 			}
 
 			return user;
 
 		} catch (Exception e) {
 			Log.e("TheLuvExchange", "WebServiceError", e);
 			return "Problem connecting to login service.";
 		}
 	}
 
 	/**
 	 * @return a list of City objects received from the Web service
 	 */
 	public static List<City> getCities() {
 
 		// List of cities to be populated and returned
 		List<City> cities = null;
 
 		try {
 			// URL object used to create a connection to the cities XML page
 			URL url = new URL(ADDRESS + "cities");
 
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			// SAXHandler class used for parsing the XML
 			CityHandler handler = new CityHandler();
 
 			// Set the XMLReader to use the SAXHandler for parsing rules
 			reader.setContentHandler(handler);
 
 			// Run the parsing
 			reader.parse(new InputSource(url.openStream()));
 
 			// Receive the list of City objects from the parse
 			cities = handler.getCityData();
 
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 
 		// Return the list of cities
 		return cities;
 	}
 
 	/**
 	 * @param user
 	 * @param city
 	 * @return a list of Picks that represent restaurants given a user and city
 	 *  or null if there was an error
 	 */
 	public static List<Pick> getRestaurants(User user, City city) {
 		return getRestaurants(user, city, "rating_count", "desc");
 	}
 	/**
 	 * @param user object
 	 * @param city object
 	 * @param sort: must be either id, name, category, address, phone, body,
 	 * latitude, longitude, discounts, rating_total, rating_count, rating_avg,
 	 * location, or viewer_rating
 	 * @param direction: must be either asc or desc
 	 * @return list of Picks that represent airport eats given a user and city
 	 *  or null if there was an error
 	 */
 	public static List<Pick> getRestaurants(User user, City city, String sort, String direction) {
 		URL url = null;
 		try {
 			// URL object used to create a connection to the cities XML page
 			url = new URL(ADDRESS + "restaurants/" + city.getId() 
 					+ "/sort:Pick." + sort + "/direction:" + direction
 					+ "/viewer_id:" + user.getUserId());
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 		return getPicks(url);
 	}
 
 	/**
 	 * @param user
 	 * @param city
 	 * @return a list of Picks that represent things to do given a user and city
 	 *  or null if there was an error
 	 */
 	public static List<Pick> getThings(User user, City city) {
 		return getThings(user, city, "rating_count", "desc");
 	}
 	/**
 	 * @param user object
 	 * @param city object
 	 * @param sort: must be either id, name, category, address, phone, body,
 	 * latitude, longitude, discounts, rating_total, rating_count, rating_avg,
 	 * location, or viewer_rating
 	 * @param direction: must be either asc or desc
 	 * @return list of Picks that represent airport eats given a user and city
 	 *  or null if there was an error
 	 */
 	public static List<Pick> getThings(User user, City city, String sort, String direction) {
 		URL url = null;
 		try {
 			// URL object used to create a connection to the cities XML page
 			url = new URL(ADDRESS + "things/" + city.getId() 
 					+ "/sort:Pick." + sort + "/direction:" + direction
 					+ "/viewer_id:" + user.getUserId());
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 		return getPicks(url);
 	}
 
 	/**
 	 * @param user object
 	 * @param city object
 	 * @return list of Picks that represent airport eats given a user and city
 	 *  or null if there was an error
 	 */
 	public static List<Pick> getAirportEats(User user, City city) {
 		return getAirportEats(user, city, "rating_count", "desc");
 	}
 	/**
 	 * @param user object
 	 * @param city object
 	 * @param sort: must be either id, name, category, address, phone, body,
 	 * latitude, longitude, discounts, rating_total, rating_count, rating_avg,
 	 * location, or viewer_rating
 	 * @param direction: must be either asc or desc
 	 * @return list of Picks that represent airport eats given a user and city
 	 *  or null if there was an error
 	 */
 	public static List<Pick> getAirportEats(User user, City city, String sort, String direction) {
 		URL url = null;
 		try {
 			// URL object used to create a connection to the cities XML page
 			url = new URL(ADDRESS + "airports/" + city.getId() 
 					+ "/sort:Pick." + sort + "/direction:" + direction
 					+ "/viewer_id:" + user.getUserId());
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 		return getPicks(url);
 	}
 
 	/*
 	 * Used by other methods, not for public access
 	 */
 	private static List<Pick> getPicks(URL url) {
 		// List of cities to be populated and returned
 		List<Pick> picks = null;
 
 		try {
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			// SAXHandler class used for parsing the XML
 			PickHandler handler = new PickHandler();
 
 			// Set the XMLReader to use the SAXHandler for parsing rules
 			reader.setContentHandler(handler);
 
 			// Run the parsing
 			reader.parse(new InputSource(url.openStream()));
 
 			// Receive the list of City objects from the parse
 			picks = handler.getPickData();
 
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 		return picks;
 	}
 
 	/**
 	 * @param pick object to get ratings for
 	 * @return list of ratings for the pick, will be null if there was an error
 	 */
 	public static List<Rating> getRatings(Pick pick) {
//		return getRatings(pick, "rating_count", "desc");
		return getRatings(pick, "viewer_rating", "desc");

 	}
 	/**
 	 * @param pick object to get ratings for
 	 * @param sort: must be either id, name, category, address, phone, body,
 	 * latitude, longitude, discounts, rating_total, rating_count, rating_avg,
 	 * location, viewer_rating, created, username, or userid
 	 * @param direction: must be either asc or desc
 	 * @return list of ratings for the pick, will be null if there was an error
 	 */
 	public static List<Rating> getRatings(Pick pick, String sort, String direction) {
 		List<Rating> ratings = null;
 
 		try {
 			// URL object used to create a connection to the cities XML page
 			URL url = new URL(ADDRESS + "pick/" + pick.getId() 
 					+ "/sort:Pick." + sort + "/direction:" + direction);
 
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			// SAXHandler class used for parsing the XML
 			RatingHandler handler = new RatingHandler();
 
 			// Set the XMLReader to use the SAXHandler for parsing rules
 			reader.setContentHandler(handler);
 
 			// Run the parsing
 			reader.parse(new InputSource(url.openStream()));
 
 			// Receive the list of City objects from the parse
 			ratings = handler.getRatingData();
 
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 
 		return ratings;
 	}
 	/*added by Shibani Chadha 4/13
 	 */
 
 	public static List<Comment> getComment(City city) {
 		List<Comment> comments = null;
 
 		try {
 			// URL object used to create a connection to the cities XML page
 			URL url = new URL(ADDRESS + "intown/"+city.getId()+"/sort:CityMessage.created/direction:desc");
 
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			// SAXHandler class used for parsing the XML
 			CommentHandler handler = new CommentHandler();
 
 			// Set the XMLReader to use the SAXHandler for parsing rules
 			reader.setContentHandler(handler);
 
 			// Run the parsing
 			reader.parse(new InputSource(url.openStream()));
 
 			// Receive the list of City objects from the parse
 			comments = handler.getCommentData();
 
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 
 		return comments;
 	}
 	/**
 	 * @param user object
 	 * @param city object
 	 * @param pick to post rating for
 	 * @param comment
 	 * @param rating as a number between 1 and 5
 	 * @param discount is true if there are discounts for SW employees, else false
 	 * @return an Object which is either an instanceof Rating or String.
 	 * If a Rating is returned, then the post was successful.
 	 * If the result is a String, then its an error message describing why it failed.
 	 */
 	public static Object postRating(User user, City city, Pick pick, String comment,
 			int rating, boolean discount) {
 
 		Rating vote = new Rating(pick);
 		vote.setBody(comment);
 		vote.setViewerRating(Integer.toString(rating));
 		vote.setDiscounts(discount ? "1" : "0");
 
 		HttpClient httpClient = new DefaultHttpClient();
 
 		StringBuilder address = new StringBuilder(ADDRESS);
 		address.append("picks_add?user_id=");
 		address.append(user.getUserId());
 		address.append("&code=");
 		address.append(user.getCode());
 
 		HttpPost httpPost = new HttpPost(address.toString());
 		try {
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 			pairs.add(new BasicNameValuePair("data[Pick][name]", vote.getName()));
 			pairs.add(new BasicNameValuePair("data[Pick][body]", comment));
 			pairs.add(new BasicNameValuePair("data[Pick][location]", vote.getLocation()));
 			pairs.add(new BasicNameValuePair("data[Pick][near]", null)); // ?
 			pairs.add(new BasicNameValuePair("data[Pick][address]", vote.getAddress()));
 			pairs.add(new BasicNameValuePair("data[Pick][phone]", vote.getPhone()));
 			pairs.add(new BasicNameValuePair("data[Pick][latitude]", vote.getLatitude()));
 			pairs.add(new BasicNameValuePair("data[Pick][longitude]", vote.getLongitude()));
 			pairs.add(new BasicNameValuePair("data[Pick][discounts]", discount ? "1" : "0"));
 			pairs.add(new BasicNameValuePair("data[Pick][category]", vote.getCategory()));
 			pairs.add(new BasicNameValuePair("data[Pick][parent_id]", vote.getId()));
 			pairs.add(new BasicNameValuePair("data[Pick][city_id]", city.getId()));
 			pairs.add(new BasicNameValuePair("data[Pick][rating]", Integer.toString(rating)));
 
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 
 			XMLReader reader = getParser().getXMLReader();
 
 			PostHandler handler = new PostHandler();
 			reader.setContentHandler(handler);
 
 			String response = httpClient.execute(httpPost, new BasicResponseHandler());
 			InputSource input = new InputSource();
 			input.setCharacterStream(new StringReader(response));
 			reader.parse(input);
 
 			String result = handler.getResult();
 
 			return result.equals("success") ? vote : result;
 
 		} catch (Exception e) {
 			Log.e("TheLuvExchange", "WebServiceError", e);
 			return "Error: " + e;
 		}
 	}
 
 	/**
 	 * To post a new Restaurant
 	 * @param user object
 	 * @param city object
 	 * @param name of the place
 	 * @param comment for the rating
 	 * @param near is a string that should match exactly to a local hotel name for the website to process it meaningfully
 	 * @param rating as a number between 1 and 5
 	 * @param discount is true if there are discounts for SW employees, else false
 	 * @param location is a string that represents what this place is
 	 * @param address is the actual street address
 	 * @param phone is the phone number
 	 * @param latitude is the measurement from Google Maps API
 	 * @param longitude is the measurement from Google Maps API
 	 * @return an Object which is either an instanceof Pick or String.
 	 * If a Pick is returned, then the post was successful.
 	 * If the result is a String, then its an error message describing why it failed.
 	 */
 	public static Object postRestaurant(User user, City city, String name, String comment,
 			String near, int rating, boolean discount, String location, String address,
 			String phone, int latitude, int longitude) {
 		return postPick(user, city, name, comment, near, rating, discount, location, address,
 				phone, latitude, longitude, RESTAURANTS);
 	}
 	/**
 	 * To post a new Thing to Do
 	 * @param user object
 	 * @param city object
 	 * @param name of the place
 	 * @param comment for the rating
 	 * @param near is a string that should match exactly to a local hotel name for the website to process it meaningfully
 	 * @param rating as a number between 1 and 5
 	 * @param discount is true if there are discounts for SW employees, else false
 	 * @param location is a string that represents what this place is
 	 * @param address is the actual street address
 	 * @param phone is the phone number
 	 * @param latitude is the measurement from Google Maps API
 	 * @param longitude is the measurement from Google Maps API
 	 * @return an Object which is either an instanceof Pick or String.
 	 * If a Pick is returned, then the post was successful.
 	 * If the result is a String, then its an error message describing why it failed.
 	 */
 	public static Object postThing(User user, City city, String name, String comment,
 			String near, int rating, boolean discount, String location, String address,
 			String phone, int latitude, int longitude) {
 		return postPick(user, city, name, comment, near, rating, discount, location, address,
 				phone, latitude, longitude, THINGS_TO_DO);
 	}
 	/**
 	 * To post a new airport eat
 	 * @param user object
 	 * @param city object
 	 * @param name of the place
 	 * @param comment for the rating
 	 * @param near is a string that should match exactly to a local hotel name for the website to process it meaningfully
 	 * @param rating as a number between 1 and 5
 	 * @param discount is true if there are discounts for SW employees, else false
 	 * @param location is a string that represents what this place is
 	 * @param address is the actual street address
 	 * @param phone is the phone number
 	 * @param latitude is the measurement from Google Maps API
 	 * @param longitude is the measurement from Google Maps API
 	 * @return an Object which is either an instanceof Pick or String.
 	 * If a Pick is returned, then the post was successful.
 	 * If the result is a String, then its an error message describing why it failed.
 	 */
 	public static Object postAirportEat(User user, City city, String name, String comment,
 			String near, int rating, boolean discount, String location, String address,
 			String phone, int latitude, int longitude) {
 		return postPick(user, city, name, comment, near, rating, discount, location, address,
 				phone, latitude, longitude, AIRPORT_EATS);
 	}
 	/**
 	 * Used by other classes to post picks
 	 * @param user object
 	 * @param city object
 	 * @param name of the place
 	 * @param comment for the rating
 	 * @param near is a string that should match exactly to a local hotel name for the website to process it meaningfully
 	 * @param rating as a number between 1 and 5
 	 * @param discount is true if there are discounts for SW employees, else false
 	 * @param location is a string that represents what this place is
 	 * @param address is the actual street address
 	 * @param phone is the phone number
 	 * @param latitude is the measurement from Google Maps API
 	 * @param longitude is the measurement from Google Maps API
 	 * @param category is an integer
 	 * @return an Object which is either an instanceof Pick or String.
 	 * If a Pick is returned, then the post was successful.
 	 * If the result is a String, then its an error message describing why it failed.
 	 */
 	public static Object postPick(User user, City city, String name, String comment,
 			String near, int rating, boolean discount, String location, String address,
 			String phone, double latitude, double longitude, int category) {
 
 		Pick pick = new Pick();
 		pick.setName(name);
 		pick.setBody(comment);
 		pick.setNear(near);
 		pick.setViewerRating(Integer.toString(rating));
 		pick.setDiscounts(discount ? "1" : "0");
 		pick.setLocation(location);
 		pick.setAddress(address);
 		pick.setPhone(phone);
 		pick.setLatitude(Double.toString(latitude));
 		pick.setLongitude(Double.toString(longitude));
 
 		HttpClient httpClient = new DefaultHttpClient();
 
 		StringBuilder url = new StringBuilder(ADDRESS);
 		url.append("picks_add?user_id=");
 		url.append(user.getUserId());
 		url.append("&code=");
 		url.append(user.getCode());
 
 		HttpPost httpPost = new HttpPost(url.toString());
 		try {
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 			pairs.add(new BasicNameValuePair("data[Pick][name]", name));
 			pairs.add(new BasicNameValuePair("data[Pick][body]", comment));
 			pairs.add(new BasicNameValuePair("data[Pick][location]", location));
 			pairs.add(new BasicNameValuePair("data[Pick][near]", near)); // ?
 			pairs.add(new BasicNameValuePair("data[Pick][address]", address));
 			pairs.add(new BasicNameValuePair("data[Pick][phone]", phone));
 			pairs.add(new BasicNameValuePair("data[Pick][latitude]", Double.toString(latitude)));
 			pairs.add(new BasicNameValuePair("data[Pick][longitude]", Double.toString(longitude)));
 			pairs.add(new BasicNameValuePair("data[Pick][discounts]", discount ? "1" : "0"));
 			pairs.add(new BasicNameValuePair("data[Pick][category]", Integer.toString(category)));
 			pairs.add(new BasicNameValuePair("data[Pick][parent_id]", "0"));
 			pairs.add(new BasicNameValuePair("data[Pick][city_id]", city.getId()));
 			pairs.add(new BasicNameValuePair("data[Pick][rating]", Integer.toString(rating)));
 
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 
 			XMLReader reader = getParser().getXMLReader();
 
 			PostHandler handler = new PostHandler();
 			reader.setContentHandler(handler);
 
 			String response = httpClient.execute(httpPost, new BasicResponseHandler());
 			InputSource input = new InputSource();
 			input.setCharacterStream(new StringReader(response));
 			reader.parse(input);
 
 			String result = handler.getResult();
 
 			return result.equals("success") ? pick : result;
 
 		} catch (Exception e) {
 			Log.e("TheLuvExchange", "WebServiceError", e);
 			return "Error: " + e;
 		}
 	}
 
 	/**
 	 * For posting a rating to a photo.
 	 * @param user object
 	 * @param id AlbumPhoto object to post rating to
 	 * @param rating as a number between 1 and 5
 	 * @return string that will be the word success or an error message
 	 */
 	public static String postRating(User user, String id, Integer rating) {
 
 		HttpClient httpClient = new DefaultHttpClient();
 
 		StringBuilder address = new StringBuilder(ADDRESS);
 		address.append("rating_add?user_id=");
 		address.append(user.getUserId());
 		address.append("&code=");
 		address.append(user.getCode());
 
 		HttpPost httpPost = new HttpPost(address.toString());
 		try {
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 			pairs.add(new BasicNameValuePair("data[Rating][item_id]", id));
 			pairs.add(new BasicNameValuePair("data[Rating][rating]", rating.toString()));
 
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 
 			XMLReader reader = getParser().getXMLReader();
 
 			PostHandler handler = new PostHandler();
 			reader.setContentHandler(handler);
 
 			String response = httpClient.execute(httpPost, new BasicResponseHandler());
 			InputSource input = new InputSource();
 			input.setCharacterStream(new StringReader(response));
 			reader.parse(input);
 
 			String result = handler.getResult();
 
 			return result;
 
 		} catch (Exception e) {
 			Log.e("TheLuvExchange", "WebServiceError", e);
 			return "Error: " + e;
 		}
 	}
 	/**
 	 * edited by Shibani Chadha 4/11 added postMessage
 	 * @param city 
 	 * @return message
 	 */
 
 	/*public static String postMessage(User user, AddMessage message, City city) {
 
 
 	}*/
 
 
 	/**
 	 * edited by Shibani Chadha 3/29 added getWeather
 	 * @param city
 	 * @return WeatherSet
 	 */
 	public static WeatherSet getWeather(City city) {
 		URL url=null;
 		WeatherSet weather = null;
 
 		try {
 
 			String queryString="http://free.worldweatheronline.com/feed/weather.ashx?q="
 					+ city.getName()+",&format=xml&num_of_days=4&key=94e4b77f82022251121304";
 			// URL object used to create a connection to the weather's XML page
 			url = new URL(queryString.replace(" ", "%20"));
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			// SAXHandler class used for parsing the XML
 			WeatherHandler handler = new WeatherHandler();
 
 			// Set the XMLReader to use the SAXHandler for parsing rules
 			reader.setContentHandler(handler);
 
 			// Run the parsing
 			reader.parse(new InputSource(url.openStream()));
 
 			// Receive the weather objects from the parse
 			weather= handler.getWeatherData();
 
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 
 		// Return the weather
 		return weather;
 	}
 
 	/**
 	 * For getting a city photo for the city menu.
 	 * @param city to get a city photo for
 	 * @return Drawable or null if there was a problem
 	 */
 	public static Drawable getCityPhoto(City city) {
 		Drawable cityPhoto = null;
 		try {
 			URL url = new URL(ADDRESS + "city_photo/" + city.getId());
 			InputStream inputStream = (InputStream) url.getContent();
 			cityPhoto = Drawable.createFromStream(inputStream, "cityPhoto");
 		} catch (Exception e) {
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 		return cityPhoto;
 	}
 
 	/**
 	 * For getting photos for a city album
 	 * @param user object
 	 * @param city object
 	 * @return List of AlbumPhoto objects or null if error
 	 */
 	public static List<AlbumPhoto> getPhotos(User user, City city) {
 		if (photos != null && city.getId().equals(cityId)) {
 			return photos;
 		}
 		return getPhotos(user, city, "created", "desc");
 	}
 	/**
 	 * For getting photos for a city album
 	 * @param user object
 	 * @param city object
 	 * @param sort: must be either id, filename, caption, rating_avg, rating_total, rating_count, or viewer_rating
 	 * @param direction: must be either asc or desc
 	 * @return List of AlbumPhoto objects or null if error
 	 */
 	public static List<AlbumPhoto> getPhotos(User user, City city, String sort, String direction) {
 		photos = null;
 
 		try {
 			URL url = new URL(ADDRESS + "photos/" + city.getId() 
 					+ "/sort:Photo." + sort + "/direction:" + direction
 					+ "/viewer_id:" + user.getUserId());
 
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			// SAXHandler class used for parsing the XML
 			PhotosHandler handler = new PhotosHandler();
 
 			// Set the XMLReader to use the SAXHandler for parsing rules
 			reader.setContentHandler(handler);
 
 			// Run the parsing
 			reader.parse(new InputSource(url.openStream()));
 
 			photos = handler.getRatingData();
 
 			// images = new ArrayList<Drawable>(photos.size());
 
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 
 		return photos;
 	}
 
 	/**
 	 * For getting a full size image for a specific photo
 	 * @param photo object to get image for
 	 * @return Drawable of full size image
 	 */
 	public static Drawable getFullSizeImage(String filename) {
 		Drawable image = null;
 		/*if (images != null && photos != null) {
 			image = images.get(photos.indexOf(photo));
 			if (image != null) {
 				return image;
 			}
 		}*/
 
 		try {
 			URL url = new URL("http://www.theluvexchange.com/files/photos/raw/"
 					+ filename);
 			InputStream inputStream = (InputStream)url.getContent();
 			image = Drawable.createFromStream(inputStream, "image");
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 
 		return image;
 	}
 
 	/**
 	 * to post photos to city album
 	 * @param user object
 	 * @param city object
 	 * @param filepath local of photo on device
 	 * @param caption
 	 * @return String "success" or error message
 	 */
 	public static String postPhoto(User user, City city, String filepath, String caption) {
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpContext localContext = new BasicHttpContext();
 		HttpPost httpPost = new HttpPost(ADDRESS + "photo_add/" + city.getId() + "/?user_id="
 				+ user.getUserId() + "&code=" + user.getCode() + "&caption=" + caption);
 
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 		pairs.add(new BasicNameValuePair("data[Photo][caption]", caption));
 		pairs.add(new BasicNameValuePair("data[City][Photo]", filepath));
 
 		try {
 			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
 			for(int index=0; index < pairs.size(); index++) {
 				if(pairs.get(index).getName().equalsIgnoreCase("data[City][Photo]")) {
 					// If the key equals to "image", we use FileBody to transfer the data
 					Bitmap bitmap = BitmapFactory.decodeFile(pairs.get(index).getValue());
 					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 					
 					int width = bitmap.getWidth();
 					int height = bitmap.getHeight();
 					if (width > 640) {
 						height = (int)Math.round(640.0 / width * height);
 						width = 640;
 					}
 					if (height > 640) {
 						width = (int)Math.round(640.0 / height * width);
 						width = 640;
 					}
 					
 					Bitmap compressed = Bitmap.createScaledBitmap(bitmap, width, height, true);
 					compressed.compress(CompressFormat.PNG, 100, outputStream);
 					byte[] data = outputStream.toByteArray();
 					entity.addPart(pairs.get(index).getName(), new ByteArrayBody(data, "upload.png"));
 				} else {
 					// Normal string data
 					entity.addPart(pairs.get(index).getName(), new StringBody(pairs.get(index).getValue()));
 				}
 			}
 
 			httpPost.setEntity(entity);
 
 			HttpResponse response = httpClient.execute(httpPost, localContext);
 			HttpEntity responseEntity = response.getEntity();
 			String xmlString = EntityUtils.toString(responseEntity);
 			InputSource input = new InputSource();
 			input.setCharacterStream(new StringReader(xmlString));
 
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			PostHandler handler = new PostHandler();
 			reader.setContentHandler(handler);
 
 			reader.parse(input);
 
 			String result = handler.getResult();
 
 			photos = null;
 
 			return result;
 
 		} catch (Exception e) {
 			Log.e("TheLuvExchange", "WebServiceError", e);
 			return "Error: " + e;
 		}
 	}
 
 	/**
 	 * Get messages for Who's In Town
 	 * Sorted descending by created
 	 * @param city object
 	 * @return List of CityMessage objects or null if error
 	 */
 	public static List<Message> getInTown(City city) {
 		return getInTown(city, "CityMessage.created", "desc");
 	}
 	/**
 	 * Get messages for Who's In Town
 	 * @param city object
 	 * @param sort: must be either id, body, created, or username
 	 * @param direction: must be either asc or desc
 	 * @return
 	 */
 	public static List<Message> getInTown(City city, String sort, String direction) {
 		List<Message> messages = null;
 
 		try {
 			URL url = new URL(ADDRESS + "intown/" + city.getId() + "/sort:CityMessage." + sort + "/direction:" + direction);
 
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			// SAXHandler class used for parsing the XML
 			MessageHandler handler = new MessageHandler();
 
 			// Set the XMLReader to use the SAXHandler for parsing rules
 			reader.setContentHandler(handler);
 
 			// Run the parsing
 			reader.parse(new InputSource(url.openStream()));
 
 			messages = handler.getMessageData();
 
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 
 		return messages;
 	}
 	/*
 	 * added by Shibani Chadha 4/12
 	 */
 	public static String postMessage(User user, AddMessage message, City city) {
 		HttpClient httpClient = new DefaultHttpClient();
 
 		StringBuilder address = new StringBuilder(ADDRESS);
 		address.append("intown_add?user_id=");
 		address.append(user.getUserId());
 		address.append("&code=");
 		address.append(user.getCode());
 
 		HttpPost httpPost = new HttpPost(address.toString());
 		try {
 
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>(2);
 			pairs.add(new BasicNameValuePair("data[CityMessage][body]", message.getMessage()));
 			pairs.add(new BasicNameValuePair("data[CityMessage][city_id]", city.getId()));
 
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 
 			XMLReader reader = getParser().getXMLReader();
 
 			PostHandler handler = new PostHandler();
 			reader.setContentHandler(handler);
 
 			String response = httpClient.execute(httpPost, new BasicResponseHandler());
 			InputSource input = new InputSource();
 			input.setCharacterStream(new StringReader(response));
 			reader.parse(input);
 
 			String result = handler.getResult();
 
 			return result;
 
 		} catch (Exception e) {
 			Log.e("TheLuvExchange", "WebServiceError", e);
 			return "Error: " + e;
 
 		}
 	}
 
 	public static List<BuySellRent> getBuySellRent(City city, User user, int category) {
 
 		List<BuySellRent> buySellRent = null;
 
 		try {
 			// URL object used to create a connection to the cities XML page
 			URL url = new URL(ADDRESS + "assets/" + city.getId() + 
 					"/" + category + "/sort:Asset.created/direction:asc/viewer_id:" +
 					user.getUserId());
 
 			// SAX XMLReader object used for parsing the XML file
 			XMLReader reader = getParser().getXMLReader();
 
 			// SAXHandler class used for parsing the XML
 			BuySellRentHandler handler = new BuySellRentHandler();
 
 			// Set the XMLReader to use the SAXHandler for parsing rules
 			reader.setContentHandler(handler);
 
 			/*
  *  Your implementation
  * 
  * String xmlString = source.getCharacterStream().toString().replaceAll("<p>", "");
 			xmlString = xmlString.replaceAll("</p>", "\r\n");
 
 			// Run the parsing
 			reader.parse(xmlString);
  * 
  * */
 			// Run the parsing
 			reader.parse(new InputSource(url.openStream()));
 
 			
 			// Receive the list of City objects from the parse
 			buySellRent = handler.getBSRData();
 
 		} catch (Exception e) {
 			// Log error to be able to debug using LogCat
 			Log.e("TheLuvExchange", "WebServiceError", e);
 		}
 
 		// Return the list of cities
 		return buySellRent;
 	}
 }
