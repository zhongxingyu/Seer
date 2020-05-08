 package Salsalytics;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import android.util.Log;
 
 /**
  * Event represents a Salesforce Event Object, which live in a 
  * Salesforce.com database.  An event is all of the data related 
  * to a single snapshot in time.  The Event contains a title that 
  * describes what it represents and sets of data in the form of
  * key-value pairs.  
  * 
  * 
  * @author Brandon Page, brpage@calpoly.edu
  * @author Martin Silverio, msilverio324@gmail.com
  */
 class Event {
 	private static String url;
 	private String charset = "UTF-8";
 	private String query = "?SalsalyticsEventTitle=";
 
 	/**
 	 * Sends data to the users Google App Engine site.
 	 * 
 	 * @return the return status of sending the data.
 	 */
 	int send() throws MalformedURLException, IOException {
 		HttpURLConnection conn = (HttpURLConnection) new URL(url + query)
 				.openConnection();
 		conn.setRequestProperty("Accept-Charset", charset);
 		@SuppressWarnings("unused")
 		InputStream response = new BufferedInputStream(conn.getInputStream());
 		int status = conn.getResponseCode();
 		query = "?SalsalyticsEventTitle=";
 
 		return status;
 	}
 
 	/**
 	 * Sets the server to send the data to.
 	 * 
 	 * @param serverName a String containing the URl of the recieving side
 	 * of the serve
 	 */
 	void setServer(String serverName) {
 		url = serverName;
 	}
 
 	/**
 	 * Returns the current server URL. 
 	 * 
 	 * @return the URL of the server
 	 */
 	URL getServer() {
 		URL realUrl;
 		try {
 			realUrl = new URL(url);
 			return realUrl;
 		} catch (MalformedURLException e) {
 			Log.e("Malformed String", e.getMessage());
 		}
 
 		return null;
 	}
 
 	/**
 	 * Adds a title and event attributes to a querystring to 
 	 * be send to the Server.   
 	 * 
 	 * @param title a String representing the title of the event
 	 * @param attributes a Map containing the key-value pairs 
 	 * relating to the event.
 	 */
 	void addData(String title, Map<String, String> attributes) {
 		query += title + "&" + buildQueryString(attributes);
 
 	}
 
 	/**
 	 * Builds the querystring from a map of key-value pairs.
 	 * 
 	 * @param urlParams Map of key value pairs to be put into they 
 	 * querystring.
 	 * @return The querystring with url encoded parameters.
 	 */
 	private String buildQueryString(Map<String, String> urlParams) {
 		StringBuilder query = new StringBuilder();
 		boolean first = true;
 
 		for (Entry<String, String> urlParam : urlParams.entrySet()) {
 			if (first) {
 				first = false;
 			} else {
 				query.append("&");
 			}
 			query.append(encode(urlParam.getKey()) + "="
 					+ encode(urlParam.getValue()));
 		}
 		return query.toString();
 	}
 
 	/**
 	 * Wrapper function for url encoding things for the querystring.
 	 * 
 	 * @param urlParam A parameter to encode.
 	 * @return The encoded parameter or an empty string on error.
 	 */
 	public String encode(String urlParam) {
 		String charset = "UTF-8";
 		try {
 			return URLEncoder.encode(urlParam, charset);
 		} catch (UnsupportedEncodingException exc) {
 			Log.d("Unsupported Encoding Operation", exc.getMessage());
 			return "";
 		}
 	}
 	
 	/**
 	 * A getter for the query string for testing purposes
 	 * 
 	 * @return the queryString currently built
 	 */
 	String getQuery() {
 		return query;
 	}
 }
