 package com.brousalis;
 
 import java.io.InputStream;
 import java.net.*;
 
 public class BetaChecker {
 
 	/**
 	 * Static function that determines wether or not this is the most recent
 	 * beta version.
 	 * @param isInBeta Is this version in beta
 	 * @param betaCheckUrl The URL of the Beta Server
 	 * @return True if the software is up to date, false if it is an old version
 	 */
 	public static Boolean isUpToDate(Boolean isInBeta, String betaCheckUrl) {
 		if (isInBeta) {
 			return getHTTPData(betaCheckUrl).equals("up_to_date");
 		}
 		return true;
 	}
 	
 	/**
 	 * Registers the current device with a registration server
 	 * @param registerUrl The location of the registration URL
 	 * @param deviceID The ESN/IMEI/MEID of the device, whatever uniquely identifies it
 	 * @param username The name of the user of this device
 	 */
	public static void registerUser(String registerUrl, String deviceID, String username, String androidVersion, String network, String brand, String device, String manuf) {
 		getHTTPData(registerUrl + deviceID 
 				+ "&user=" + URLEncoder.encode(username) 
 				+ "&android=" + URLEncoder.encode(androidVersion) 
 				+ "&network=" + URLEncoder.encode(network) 
 				+ "&brand=" + URLEncoder.encode(brand)
 				+ "&hardware=" + URLEncoder.encode(device)
				+ "&manuf=" + URLEncoder.encode(manuf) );
 		
 	}
 	
 	/**
 	 * Check to see if this device is registered or banned
 	 * @param registerUrl
 	 * @param deviceID
 	 * @return "registered" if the device is in the database, 
 	 * "banned" if device has been manually removed from the database
 	 * "not_registered" if the device has not yet registered with the server
 	 */
 	public static String checkUser(String registerUrl, String deviceID) {
 		return getHTTPData(registerUrl + deviceID);
 	}
 	
 	/**
 	 * Get the return data from a specific HTTP connection.
 	 * @param url The url to request data from.
 	 * @return The resulting string from the request.
 	 */
 	public static String getHTTPData(String url) {
 		URLConnection connection;
 		String httpResult = "";
 		try {
 			connection = new URL(url).openConnection();
 			connection.connect();
 			InputStream inputStream = connection.getInputStream();
 			StringBuffer buffer = new StringBuffer();
 			byte[] b = new byte[4096];
 			for (int n; (n = inputStream.read(b)) != -1;) {
 				buffer.append(new String(b, 0, n));
 			}
 			httpResult = buffer.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return httpResult;
 	}
 }
