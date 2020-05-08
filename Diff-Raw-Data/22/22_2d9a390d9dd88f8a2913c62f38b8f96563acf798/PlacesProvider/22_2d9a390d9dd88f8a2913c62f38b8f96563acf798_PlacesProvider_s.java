 package com.example.lifestylemotivator.provider;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.io.OutputStreamWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.net.URLConnection;
 import java.net.URLEncoder;
 
 public class PlacesProvider {
 
 	public String printPlaces() throws MalformedURLException, IOException {
 		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?";
 		String charset = "UTF-8";
 		String location = URLEncoder.encode("-33.8670522,151.1957362", charset);
 		String radius = URLEncoder.encode("500", charset);
 		String types = URLEncoder.encode("food", charset);
 		String name = URLEncoder.encode("harbour", charset);
 		String sensor = URLEncoder.encode("false", charset);
 		String key = URLEncoder.encode("AIzaSyCh_LKVI3HSiAhVVcyMpz7EXAGrgIvRhIY", charset);
 		
 		String query = String.format("location=%s&radius=%s&types=%s&name=%s&sensor=%s&key=%s", location, radius, types, name, sensor, key);
 
		URLConnection urlConnection = new URL(url).openConnection();
		urlConnection.setUseCaches(false);
		urlConnection.setDoOutput(true); // Triggers POST.
		urlConnection.setRequestProperty("accept-charset", charset);
		urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

		OutputStreamWriter writer = null;
		try {
		    writer = new OutputStreamWriter(urlConnection.getOutputStream(), charset);
		    writer.write(query); // Write POST query string (if any needed).
		} finally {
		    if (writer != null) try { writer.close(); } catch (IOException logOrIgnore) {}
		}
 
 		InputStream result = urlConnection.getInputStream();
 	    java.util.Scanner s = new java.util.Scanner(result).useDelimiter("\\A");
 	    String xmlResponse = s.hasNext() ? s.next() : "";
 		return xmlResponse;
 	}
 }
