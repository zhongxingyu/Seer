 package com.cruzroja.creuroja.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 
 import com.cruzroja.creuroja.Location;
 import com.cruzroja.creuroja.User;
 import com.google.android.gms.maps.model.LatLng;
 
 /**
  * LOGIN:
  * 
 * PETITION FORMAT: <\?xml version="1.0"\?> <methodCall>
 * <methodName>user.login</methodName> <params> <param>
 * <value><string>username</string></value> </param> <param>
  * <value><string>password</string></value> </param> </params> </methodCall>
  * <p/>
  * 
 * Petition requirements: -Send via POST -Set content-type header to "text/xml"
 * or "application/xml"
  */
 public class ConnectionClient {
 	// URLs
 	private static final String BASE_URL = "http://r0uzic.net/voluntarios/";
 	public static final String URL_LOGIN = BASE_URL + "user/login?q=android";
 	public static final String URL_PUNTOS = BASE_URL + "map/marcadores.json";
 
 	public static final String DIRECTIONS_API_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?region=es&";
 
 	public static final String ORIGIN_URL = "origin=";
 	public static final String DESTINATION_URL = "destination=";
 	public static final String SENSOR_URL = "sensor=";
 
 	// XML preconstructed strings
 	public static final String XML_PETITION_1 = "<?xml version=\"1.0\"?>\n" + "<methodCall>\n"
 			+ "   <methodName>user.login</methodName>\n" + "   <params>\n" + "     <param>\n"
 			+ "        <value><string>";
 	public static final String XML_PETITION_2 = "</string></value>\n" + "     </param>\n"
 			+ "    <param>\n" + "        <value><string>";
 	public static final String XML_PETITION_3 = "</string></value>\n" + "     </param>\n"
 			+ "   </params>\n" + "</methodCall>";
 	public static final String CONTENT_TYPE = "content-type";
 	public static final String CONTENT_TYPE_XML = "text/xml";
 
 	// This class should not be instantiated
 	private ConnectionClient() {
 	}
 
 	/**
	 * 	 * Method to call to perform a credentials validation.
 	 * 
 	 * @param username
 	 * @param password
	 * @return null with server error. 
 	 */
 	public static User doLogin(String username, String password) {
 		return parseLoginResponse(makeConnection(buildLoginRequest(username, password)));
 	}
 
 	/**
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public static ArrayList<Location> downloadLocations(Context context) {
 		return parseLocationsResponse(context, makeConnection(new HttpGet(URL_PUNTOS)));
 	}
 
 	public static ArrayList<LatLng> getDirections(double originLatitud, double originLongitud,
 			double destinationLatitud, double destinationLongitud) {
 		return parseDirectionsResponse(makeConnection(buildDirectionsRequest(originLatitud,
 				originLongitud, destinationLatitud, destinationLongitud)));
 	}
	
 	/*******************
 	 * Utility methods *
 	 *******************/
	
 	/**
 	 * Should be always called before using the internet connection.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public static boolean isConnected(Context context) {
 		ConnectivityManager cm = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (cm.getActiveNetworkInfo() != null) {
 			return cm.getActiveNetworkInfo().isConnected();
 		}
 
 		return false;
 	}
 
 	private static HttpResponse makeConnection(HttpUriRequest request) {
 		if (request == null) {
 			return null;
 		}
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 
 		try {
 			return httpClient.execute(request);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**************************
 	 * Login specific methods *
 	 **************************/
 
 	private static HttpUriRequest buildLoginRequest(String username, String password) {
 		HttpPost request = new HttpPost(URL_LOGIN);
 		request.setHeader(new BasicHeader(CONTENT_TYPE, CONTENT_TYPE_XML));
 		try {
 			request.setEntity(new StringEntity(XML_PETITION_1 + username + XML_PETITION_2
 					+ password + XML_PETITION_3));
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		}
 		return request;
 	}
 
 	private static User parseLoginResponse(HttpResponse response) {
 		if (response != null && response.getStatusLine() != null) {
 			switch (response.getStatusLine().getStatusCode()) {
 			case 200:
 				// Success in the connection. Check response
 				try {
 					return new User(response.getEntity().getContent());
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 					return null;
 				} catch (IOException e) {
 					e.printStackTrace();
 					return null;
 				}
 			default:
 				return null;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	/*****************************
 	 * Location specific methods *
 	 *****************************/
 
	private static ArrayList<Location> parseLocationsResponse(Context context,
			HttpResponse response) {
 		ArrayList<Location> result = null;
 		if (response.getStatusLine() != null) {
 			switch (response.getStatusLine().getStatusCode()) {
 			case 200:
 				// Get everything as a string ready to parse and save later to
 				// disk.
 				BufferedReader reader;
 				try {
 					reader = new BufferedReader(new InputStreamReader(response.getEntity()
 							.getContent()));
 
 					String data = "";
 					String line;
 					while ((line = reader.readLine()) != null) {
 						data += line;
 					}
 					if (reader != null) {
 						reader.close();
 					}
 					// Parse the resulting string. Save to disk if its correct
 					result = JSONParser.parseLocations(data);
 					if (result != null && !result.isEmpty()) {
 						FileUtils.writeToFile(context, data);
 					}
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 		return result;
 	}
 
 	/******************************
 	 * Direction specific methods *
 	 ******************************/
 
 	private static HttpUriRequest buildDirectionsRequest(double originLatitud,
 			double originLongitud, double destinationLatitud, double destinationLongitud) {
 		return new HttpGet(URI.create(DIRECTIONS_API_BASE_URL + ORIGIN_URL + originLatitud + ","
 				+ originLongitud + "&" + DESTINATION_URL + destinationLatitud + ","
 				+ destinationLongitud + "&" + SENSOR_URL + getSensorAvailability()));
 	}
 
 	// Placeholder in case we want to introduce sensor detection
 	private static String getSensorAvailability() {
 		return "true";
 	}
 
 	private static ArrayList<LatLng> parseDirectionsResponse(HttpResponse response) {
 		ArrayList<LatLng> result = null;
 		if (response.getStatusLine() != null) {
 			switch (response.getStatusLine().getStatusCode()) {
 			case 200:
 				// Get everything as a string ready to parse
 				BufferedReader reader;
 				try {
 					reader = new BufferedReader(new InputStreamReader(response.getEntity()
 							.getContent()));
 
 					String data = "";
 					String line;
 					while ((line = reader.readLine()) != null) {
 						data += line;
 					}
 					if (reader != null) {
 						reader.close();
 					}
 					// Parse the resulting string. Save to disk if its correct
 					result = JSONParser.parseDirections(data);
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 		return result;
 	}
 
 }
