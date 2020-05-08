 package com.wirelessnetworks.cloudshare;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import android.content.Context;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.util.Log;
 
 public class CloudShareUtils {
 
 	private static String route_url = "https://cloudshareroute.appspot.com/";
 	
 	public static HttpResponse postData(String path, String[] parameters, String[] values) {
 	    // Create a new HttpClient and Post Header
 	    HttpClient httpclient = new DefaultHttpClient();
 	    HttpPost httppost = new HttpPost(route_url + path);
 	    HttpResponse response = null;
 	    
 	    try {
 	        // Add data to be sent
 	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 	    	nameValuePairs.add(new BasicNameValuePair("authtoken", "HELLOKITTYGORU1212"));
         	for (int i = 0; i < parameters.length; i++)
 	        	nameValuePairs.add(new BasicNameValuePair(parameters[i], values[i]));
         	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 	        // Execute HTTP Post Request
 	        response =  httpclient.execute(httppost);
 	        
 	    } catch (ClientProtocolException e) {
 	        // TODO Auto-generated catch block
 	    } catch (IOException e) {
 	        // TODO Auto-generated catch block
 	    }
 		return response;
 		
 	}
 	
 	public static String[] getDOMresults(Element parent, String[] fields) {
 		String[] child_values = new String[fields.length];
 		
 		//Pull out the item's information
 		for (int i = 0; i < fields.length; i++)
 			child_values[i] = getTagValue(fields[i], parent);
 		return child_values;
 	}
 	
 	// Get a single tag from a DOM element
 	public static String getTagValue(String tag, Element el){
 	    return el.getElementsByTagName(tag).item(0).getChildNodes().item(0).getNodeValue();    
 	 }
 	
 	public static Document getDOMbody(String response) {
 		Document doc = null;
 		try {
 			StringReader reader = new StringReader (response);
 			InputSource inputSource = new InputSource( reader );
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		    doc = dBuilder.parse(inputSource);
 		    reader.close();
 		} catch (Exception e) {
 			Log.v("PARSE ERROR", e.getMessage());
 		}
 	    
 		return doc;
 	}
 	
 	public static String parseHttpResponse(HttpResponse response) {
 		// Parse the xml input resulting from a refresh post
 		StringBuffer results = new StringBuffer("");
         try {
 			BufferedReader xml_reader = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
 	        String line;
 	        while ((line = xml_reader.readLine()) != null) {
 	            results.append(line);
 	        }
 	        xml_reader.close();
 		} catch (Exception e) {
 			Log.v("PARSE ERROR", e.getMessage());
 		}
 		return results.toString();
 	}
 	
 	public static String checkErrors(HttpResponse response) throws Exception {
 		String result = CloudShareUtils.parseHttpResponse(response);
 		Document doc = CloudShareUtils.getDOMbody(result);
 		
 		NodeList errors = doc.getElementsByTagName("error");
 		if (errors.getLength() > 0)
 			throw new Exception();
 		
 		return result;
 	}
 	
 	public static String reverseLocation(Context context, Location location) throws Exception {
 		double latPoint = location.getLatitude();
         double lngPoint = location.getLongitude();
 
        Geocoder reverseGeo = new Geocoder(context);
         List<Address> curLocationList;
         String locString = "";
         if (reverseGeo != null){
             try {
                 curLocationList = reverseGeo.getFromLocation(latPoint, lngPoint, 1);
             } catch (Exception e) {
                 throw e;
             }
 
             if (curLocationList.size()> 0) {
                 locString += curLocationList.get(0).getAddressLine(0);
                 locString += curLocationList.get(0).getLocality();
             } else {
             	throw new Exception();
             }
         }
         return locString;
 	}
 }
