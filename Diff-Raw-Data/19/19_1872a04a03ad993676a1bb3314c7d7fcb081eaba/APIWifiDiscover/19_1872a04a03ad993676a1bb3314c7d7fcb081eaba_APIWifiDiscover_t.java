 package edu.gatech.cs4261.LAWN;
 
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import android.net.Uri;
 import android.net.wifi.WifiInfo;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 
 public class APIWifiDiscover extends DeviceDiscover {
     private static final String protocolType = "APIWifiDiscover"; 
     private static final String TAG = "APIWifiDiscover";
     private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     
     public APIWifiDiscover() {
         Log.v(TAG, "Constructor called");
     }
     
     @Override
     public String getProtocolType() {        
         return protocolType;
     }
 
     @Override
     public boolean isServiceAvailable() {
         
         return false;
     }
 
     @Override
     /**scan scans the router a user is connected to for impressions 
      * 
      * @param lat is the current latitude of the device
      * @param lon is the current longitude of the device
      * @param ctx is the context scan was called in
      * @return returns a boolean of success or failure
      */
     public boolean scan(double lat, double lon, Context ctx) {
         String apPage ="Some ap error";
         try {
         	//get the username that's logged in
         	String username = ((CustomActivity) ctx).getPreferences().getString("username", null);
         	if(username.equals(null)) {
         		Log.d(TAG, "username was not found");
         		return false;
         	}
         	
         	Log.d(TAG, "username: " + username);
         	
         	//get the data from the api about which aps a user is connected to
         	HttpEntity apiReturn = getUserData(username);
         	String rmac = findRouterMac(ctx);
        	//a catch for debugging if mac returns as null
        	String apName;
        	if(rmac.equals(null)) {
        		rmac = "78:D6:F0:92:49:FE";
        		apName = "175-123";
        	} else {
        		apName = findAPNamefromXML(rmac, apiReturn);
        	}
         	
         	Log.d(TAG, "ap name: " + apName);
         	
         	//get the HttpEntity from the api call for ap data
         	apiReturn = getAPData(apName);
         	
         	//parse the xml from the api call into what we need
         	int currentImpressions = findImpressionsFromXML(apiReturn);
         	
         	Log.d(TAG, "current impressions: " + currentImpressions);
         	
         	//get a content resolver to deal with the database
         	ContentResolver cr = ctx.getContentResolver();
         	
         	//create the values class to send to the insert
     		ContentValues values = new ContentValues();
     		values.put("mac_addr", rmac);
     		values.put("protocol", "api_wifi");
     		//TODO: figure out and insert accuracy
     		if(lat > 0) {
     			values.put("latitude", lat);
     		}
     		if(lon > 0) {
     			values.put("longitude", lon);
     		}
     		values.put("weight", currentImpressions);
     		
     		//call the insert method
     		Uri ret = cr.insert(LAWNStorage.CONTENT_URI, values);
     		
     		Log.d(TAG, "data inserted at: " + ret.toString());
     		
     		return true;
         } catch (ClientProtocolException e) {
             Log.e(TAG, e.toString());
         } catch (IOException e) {
             Log.e(TAG, e.toString());
         }
         Log.d(TAG, apPage);
         return false;
     }
     
     /**getUserData gets the ap connection data about a user from the api 
      * 
      * @param username username that you want to check
      * @return returns an httpentity that contains the xml from the api
      * @throws ClientProtocolException
      * @throws IOException
      */
     private HttpEntity getUserData(String username) throws ClientProtocolException, IOException{
     	//set up the url to send to
         String urlBase = "http://gardener.gatech.edu/whereami/getUserAP.php?User=";
         //TODO escape things somewhere 
         String url = urlBase + username;
         
         //set up the http stuff
         HttpContext httpContext = new BasicHttpContext();
         HttpClient httpClient = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(url);
         String page = "Page was not created check for an error";
         HttpResponse response = httpClient.execute(httpGet, httpContext);
         
         //check the status of the connection
         int statusCode = response.getStatusLine().getStatusCode();
         Log.d(TAG, "Status code: " +statusCode);
         
         //get the response
         HttpEntity entity = response.getEntity();
         page = EntityUtils.toString(entity);
         
         Log.d(TAG, "xml returned: " + page);
         
         return entity;
         
     }
     
     /**parses xml to get the name of the ap we're attached to 
      * 
      * @param rmac the router's mac address to match with
      * @param xml the xml to parse as an HttpEntity
      * @return apname the name of the Access Point as a string or null if no match found
      */
     private String findAPNamefromXML(String rmac, HttpEntity xml) {
         try {
             //set up the document
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document dom = db.parse(xml.getContent());
             
             //get the subtree as a nodelist from the xml
             NodeList nl = dom.getElementsByTagName("apinfo");
             
             //loop to find the right apinfo
             int infoRes = -1;
             for(int i = 0; i < nl.getLength(); i++) {
                 String tmac = getTextValue((Element)nl.item(i), "mac");
                 
                 //check the mac addresses
                 if(tmac.equalsIgnoreCase(rmac)) {
                     infoRes = i;
                 }
             }
             
             //checks if an ap was found and if so returns its name
             if(infoRes < 0) {
                 return null;
             } else {
                 return getTextValue((Element)nl.item(infoRes), "apname");
             }
         } catch (ParserConfigurationException e) {
             Log.e(TAG, e.toString());
         } catch (IllegalStateException e) {
             Log.e(TAG, e.toString());
         } catch (SAXException e) {
             Log.e(TAG, e.toString());
         } catch (IOException e) {
             Log.e(TAG, e.toString());
         }
         
         //return null if fails
         return null;
     }
     
     /**parses xml to get the client count of an AP
      * 
      * @param xml the xml to parse as an HttpEntity
      * @return a number of clients connected (-1 if failed)
      */
     private int findImpressionsFromXML(HttpEntity xml) {
         try {
             //set up the document
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document dom = db.parse(xml.getContent());
             
             //get the subtree as a nodelist from the xml
             NodeList nl = dom.getElementsByTagName("apinfo");
             
             //returns the number in the client count
             return getIntValue((Element)nl.item(0), "clientcount");
         } catch (ParserConfigurationException e) {
             Log.e(TAG, e.toString());
         } catch (IllegalStateException e) {
             Log.e(TAG, e.toString());
         } catch (SAXException e) {
             Log.e(TAG, e.toString());
         } catch (IOException e) {
             Log.e(TAG, e.toString());
         }
         
         //returns -1 if fails
         return -1;
     }
     
     /**
      * gets the text value held by the tag in a certain element
      * @param ele the parent element
      * @param tagName the tag name that holds the text data
      * @return
      */
     private static String getTextValue(Element ele, String tagName) {
         String textVal = null;
         NodeList nl = ele.getElementsByTagName(tagName);
         if(nl != null && nl.getLength() > 0) {
             Element el = (Element)nl.item(0);
             Node n = el.getFirstChild();
             if(n != null)
                 textVal = el.getFirstChild().getNodeValue();
         }
 
         return textVal;
     }
   
     /**
      * Just like getTextValue but for ints
      * @param ele
      * @param tagName
      * @return
      */
     private static int getIntValue(Element ele, String tagName) {
         return Integer.parseInt(getTextValue(ele,tagName));
     }
     
     private HttpEntity getAPData(String apName) throws ClientProtocolException, IOException{
     	//set up the url
         String urlBase = "http://gardener.gatech.edu/whereami/getAPStatus.php?APName="; 
         String url = urlBase + apName;
         String page = "Page was not created check for an error";
         
         //set up the connection
         HttpContext httpContext = new BasicHttpContext();
         HttpClient httpClient = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(url);
         
         //get the api response
         HttpResponse response = httpClient.execute(httpGet, httpContext);
         int statusCode = response.getStatusLine().getStatusCode();
         Log.d(TAG, "Status code: " +statusCode);
         HttpEntity entity = response.getEntity();
         
         page = EntityUtils.toString(entity);
         Log.d(TAG, "ap data xml: " + page);
         
         return entity;        
     }
     
     private String findRouterMac(Context ctx){
         Log.d(TAG, "trying to find mac of connected router");
         String mac = "";
         ConnectivityManager myConnManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo myNetworkInfo = myConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         WifiManager myWifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
         WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
         mac = myWifiInfo.getBSSID();
        Log.i(TAG, "mac = " + mac);
         return mac;
     }
 }
