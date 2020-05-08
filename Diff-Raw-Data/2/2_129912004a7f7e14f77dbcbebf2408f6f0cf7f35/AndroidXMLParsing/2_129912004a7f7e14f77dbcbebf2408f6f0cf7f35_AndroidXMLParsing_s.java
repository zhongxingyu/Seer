 package com.map.app;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class AndroidXMLParsing {
 
 	// All static variables
 	static final String URL = "FILE:///sdcard/Others/bus.xml";
 	// XML node keys
 	static final String KEY_ITEM = "item"; // parent node
 	static final String KEY_ID = "id";
 	static final String KEY_NAME = "name";
 	static final String KEY_COST = "lat";
 	static final String KEY_DESC = "lon";
 	static final String KEY_DIST = "dist";
 	static double min = 30;
 	static int i;
 	static NodeList nl;
 	static double latitude;
 	static double longitude;
 	static ArrayList<HashMap<String, String>> menuItems = new ArrayList<HashMap<String, String>>();
 
 	public static void read() {

 		XMLParser parser = new XMLParser();
 		String xml = null;
 		try {
 			xml = XMLParser.getXmlFromUrl(URL);
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} // getting XML
 		Document doc = parser.getDomElement(xml); // getting DOM element
 
 		nl = doc.getElementsByTagName(KEY_ITEM);
 		// looping through all item nodes <item>
 		for (int i = 0; i < nl.getLength(); i++) {
 			// creating new HashMap
 			HashMap<String, String> map = new HashMap<String, String>();
 			Element e = (Element) nl.item(i);
 			// adding each child node to HashMap key => value
 			map.put(KEY_ID, parser.getValue(e, KEY_ID));
 			map.put(KEY_NAME, parser.getValue(e, KEY_NAME));
 			map.put(KEY_COST, parser.getValue(e, KEY_COST));
 			map.put(KEY_DESC, parser.getValue(e, KEY_DESC));
 			double lon = (MapApplicationActivity.getLongitude() - 
 					Double.parseDouble(parser.getValue(e, KEY_DESC))) * 111.2;
 
 			double lat = (MapApplicationActivity.getLatitude() - Double
 					.parseDouble(parser.getValue(e, KEY_COST))) * 111.2;
 
 			double dist = Math.sqrt(lat * lat + lon * lon);
 			String distance = Double.toString(dist);
 			if (dist < min) {
 				min = dist;
 				latitude = Double.parseDouble(parser.getValue(e, KEY_COST));
 				longitude = Double.parseDouble(parser.getValue(e, KEY_DESC));
 			}
 			map.put(KEY_DIST, distance + " km");
 			// adding HashList to ArrayList
 			menuItems.add(map);
 
 		}
 
 		// Adding menuItems to ListView
 
 	}
 
 	public static double getNearestLat() {
 		return latitude;
 	}
 
 	public static double getNearestLon() {
 		return longitude;
 	}
 
 	public static ArrayList<HashMap<String, String>> getValues() {
 		return menuItems;
 	}
 }
