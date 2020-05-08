 package cz.pavel.uugooglesync.uu;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.GzipDecompressingEntity;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.log4j.Logger;
 
 import cz.pavel.uugooglesync.utils.CalendarUtils;
 import cz.pavel.uugooglesync.utils.Configuration;
 import cz.pavel.uugooglesync.utils.HtmlParser;
 import cz.pavel.uugooglesync.utils.HttpClientUtils;
 import cz.pavel.uugooglesync.utils.LogUtils;
 
 public class UUManager {
 	
 	private static Logger log = LogUtils.getLogger();
 	private static final String UIS_BASE_URL = "https://uu.unicornuniverse.eu";
 	
 	private DefaultHttpClient httpClient;
 	
 	
 	// total bytes read from server
 	private int totalBytes;
 	
 	private String doGet(String url) throws ClientProtocolException, IOException {
 		url = url.replace("&amp;", "&");
 		url = url.replace("|", "%7C");
 		HttpGet httpGet = new HttpGet(UIS_BASE_URL + url);
         log.debug("Sending GET request to " + httpGet.getURI());
         HttpResponse response = httpClient.execute(httpGet);
         HttpEntity entity = response.getEntity();
         String data = HtmlParser.getContents(entity.getContent());
         return data;
 	}
 	
 	private String clickLink(String id, String data) throws IOException {
 		return doGet(HtmlParser.extractRegExp(data, "<A[^>]*id=\"" + id + "\"[^>]*href=\"([^\"]*)\""));
 	}
 	
 
 	/**
 	 * Initializes Apache http client.
 	 */
 	private void initHttpClient() {
 		// if the client was not initialized, do it now
 		if (httpClient == null) {
 			httpClient = HttpClientUtils.getHttpClient();
 			
 			// add support for gzip compression (request header)
 			httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
 	            @Override
 				public void process(final HttpRequest request, final HttpContext context) {
 	                if (!request.containsHeader("Accept-Encoding")) {
 	                    request.addHeader("Accept-Encoding", "gzip");
 	                }
 	            }
 	        });
 			
 			// add support for gzip compression (response interceptor)
 			httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
                 @Override
 				public void process(final HttpResponse response, final HttpContext context) {
                     HttpEntity entity = response.getEntity();
                     totalBytes += entity.getContentLength();
                     Header ceheader = entity.getContentEncoding();
                     if (ceheader != null) {
                         HeaderElement[] codecs = ceheader.getElements();
                         for (int i = 0; i < codecs.length; i++) {
                             if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                 response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                 return;
                             }
                         }
                     }
                 }
             });
 		}
 	}
 	
 	/**
 	 * Logs in the UU using the access codes from configuration.
 	 * @return Data of the first page after login.
 	 * @throws ClientProtocolException Thrown by Apache httpclient.
 	 * @throws IOException Thrown by Apache httpclient.
 	 */
 	private String logIn() throws ClientProtocolException, IOException {
 		return logIn(
 				Configuration.getEncryptedString(Configuration.Parameters.UU_ACCESS_CODE1), 
 				Configuration.getEncryptedString(Configuration.Parameters.UU_ACCESS_CODE2)
 				);		
 	}
 	
 	/**
 	 * Logs in to Unicorn Universe.
 	 * 
 	 * @return Contents of the first HTML page after successful logon.
 	 */
 	private String logIn(String accessCode1, String accessCode2) throws ClientProtocolException, IOException {
         HttpPost httpost = new HttpPost(UIS_BASE_URL + "/ues/sesm");
         
         List <NameValuePair> nvps = new ArrayList <NameValuePair>();
         nvps.add(new BasicNameValuePair("UES_AccessCode1", accessCode1));
         nvps.add(new BasicNameValuePair("UES_AccessCode2", accessCode2));
         nvps.add(new BasicNameValuePair("UES_SecurityRealm", "unicornuniverse.eu"));
         nvps.add(new BasicNameValuePair("UES_Gate", "ues:UNI-BT:UNI-BT"));
         nvps.add(new BasicNameValuePair("loginURL", "http://unicornuniverse.eu"));
         httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
         
         HttpResponse response = httpClient.execute(httpost);
         HttpEntity entity = response.getEntity();
         
         log.debug("Sending POST request to " + httpost.getURI());
         String result = HtmlParser.getContents(entity.getContent());
         if (result.indexOf("UNI Portal Page") < 0) {
         	log.error("Invalid page after login, incorrect access code 1 or 2?");
         	throw new RuntimeException("Probably incorrect UU access code 1 or 2");
         }
         
         return result;
 	}
 	
 	/**
 	 * Parses HTML page given as input (it should be UU calendar in week mode) and
 	 * returns the date of Monday for current week as a string.
 	 *  
 	 * @param data HTML page to parse
 	 * @return Date of Monday for current week as a string using the DD.MM.YYYY format.
 	 */
 	private static String getDateStringForCurrentWeek(String data) {
 		return HtmlParser.extractRegExp(data, "<SPAN class=\"diary-navigation-time-description\">[^<]*</SPAN><SPAN>, ([0-9.]*) ");
 	}
 	
 	
 	/**
 	 * Verifies, whether access codes are valid by trying to log in. Throws RuntimeException,
 	 * if they are not valid.
 	 * @param accessCode1 access code 1
 	 * @param accessCode2 access code 2
 	 * @throws ClientProtocolException Thrown by Apache httpclient
 	 * @throws IOException Thrown by Apache httpclient
 	 */
 	public void checkAccessCodes(String accessCode1, String accessCode2) throws ClientProtocolException, IOException {
 		initHttpClient();
 		logIn(accessCode1, accessCode2);
 	}
 	
 	
 	/**
 	 * Checks, whether the input UU event is allowed (i.e. we add it to the Google calendar).
 	 * @param uuEvent UU event to check
 	 * @return True, if it should be added to the Google calendar, false otherwise.
 	 */
 	private static boolean allowEvent(UUEvent uuEvent) {
 		return uuEvent.getStatus() != UUEvent.EventStatus.REJECTED && uuEvent.getStatus() != UUEvent.EventStatus.NOT_PARTICIPATED;
 	}
 	
 	
 	/**
 	 * Loads all events from Unicorn Universe.
 	 * 
 	 * @param startDate the first date of the interval, which to load the events for
 	 * @param endDate Monday of the last week of the interval, which to load the events for
 	 * 
 	 * @return Map of all loaded events. Key is the event ID, value is the whole event structure.
 	 */
 	public Map<String, UUEvent> getEvents(Calendar startDate, Calendar endDate) throws ClientProtocolException, IOException {
 		totalBytes = 0;
 		log.debug("Loading events from " + CalendarUtils.calendarToGoogleString(startDate) + " to end of " + CalendarUtils.calendarToGoogleString(endDate));
 		
 		initHttpClient();
 		String data = logIn();
         
         // go to calendar
         data = clickLink("static-link-personal-calendar", data);
         // set calendar to week mode
         data = clickLink("dwdiary-normal-switch-week", data);
         
         // go back to the start date
         Calendar currentDate = CalendarUtils.stringToDate(getDateStringForCurrentWeek(data));
         while (startDate.before(currentDate)) {
 	        // go to previous week
 	        data = clickLink("dwdiary-normal-link-previous", data);
 	        currentDate = CalendarUtils.stringToDate(getDateStringForCurrentWeek(data));
         }
 
         Map<String, UUEvent> result = new HashMap<String, UUEvent>();
         // for all dates
         while (!currentDate.after(endDate)) {
 	        // find items
 	        int nextIndex = 0;
 	        
 	        // first non-blocking items
 	        while ((nextIndex = data.indexOf("<DIV class=\" normal-diary-item-long", nextIndex)) >= 0) {
 	        	int itemEndIndex = data.indexOf("listeners.add", nextIndex);
 	        	String itemData = data.substring(nextIndex, itemEndIndex);
 	        	String statusImg = HtmlParser.extractRegExp(itemData, "<img src=\".*/([^/]*).gif\"");
 	        	String id = HtmlParser.extractRegExp(itemData, "<SPAN class=\"[a-zIL-]*\" id=\"dwdiary_item_([0-9]*)_[0-9]*-");
 	        	String summary = HtmlParser.extractRegExp(itemData, "<SPAN class=\"[a-zIL-]*\" id=\"dwdiary_item_[^>]*>([^<]*)</SPAN>", true);
	        	String place = HtmlParser.extractRegExp(itemData, "<SPAN class=\"[a-zIL-]*\" id=\"dwdiary_item_[^>]*>[^<]*</SPAN>, ([^<]*)</SPAN>", true);
 	        	// extract time from place and remove it
 	        	String time = place.substring(place.length() - 35);
 	        	place = place.substring(0, place.length() - 35).trim();
 
 	        	UUEvent uuEvent = new UUEvent(id, summary + " (UU)", place, false);
 	        	uuEvent.setStatusImg(statusImg);
 	        	uuEvent.setTime(time, null);
 
         		// we do not want rejected and "not participated" events, do not include them in the resulting map
 	        	if (allowEvent(uuEvent)) {
 	        		result.put(uuEvent.getId(), uuEvent);
 		        	log.debug(uuEvent.toString());
 	        	}
 	        	nextIndex = itemEndIndex;
 	        }
 	        
 	        // then blocking items
 	        nextIndex = 0;
 	        final String START_OF_ITEM = "<DIV class=\"normal-diary-item";
 	        while ((nextIndex = data.indexOf(START_OF_ITEM, nextIndex)) >= 0) {
 	        	// the first character after START_OF_ITEM must be space of quotation mark
 	        	char firstAfterStart = data.charAt(nextIndex + START_OF_ITEM.length());
 	        	if (firstAfterStart != '"' && firstAfterStart != ' ') {
 	        		int itemEndIndex = data.indexOf("</SCRIPT>", nextIndex);
 	        		if (itemEndIndex < 0) {
 	        			break;
 	        		}
 	        		nextIndex = itemEndIndex;
 	        		continue;
 	        	}
 	        	
 	        	int itemEndIndex = data.indexOf("diary.addItem", nextIndex);
 	        	itemEndIndex = data.indexOf("</SCRIPT>", itemEndIndex);
 	        	String itemData = data.substring(nextIndex, itemEndIndex);
 	        	String statusImg = HtmlParser.extractRegExp(itemData, "<img src=\".*/([^/]*).gif\"");
 	        	String id = HtmlParser.extractRegExp(itemData, "<DIV class=\"normal-diary-item[^\"]*\" id=\"dwdiary_item_([0-9]*)_[0-9]*\"");
 	        	String summary = HtmlParser.extractRegExp(itemData, "<img src=[^>]*><SPAN[^>]*>([^<]*)</SPAN", true);
 	        	String place = HtmlParser.extractRegExp(itemData, "<DIV class=\"normal-diary-item-place\">(.*)</DIV><DIV class=\"normal-diary-item-time\"", true, false);
 	        	// if the place ends with comma, remove it (this is true for most events in the UU calendar)
 	        	if (place.endsWith(",") && place.length() > 1) {
 	        		place = place.substring(0, place.length() - 1);
 	        	}
 	        	String time = HtmlParser.extractRegExp(itemData, "<DIV class=\"normal-diary-item-time\">([^<]*)</DIV>");
 	        	String dateIndex = HtmlParser.extractRegExp(itemData, "diary.addItem\\(.*, ([0-9]*),[0-9 ]*\\)");
 	        	
 	        	UUEvent uuEvent = new UUEvent(id, summary + " (UU)", place, true);
 	        	uuEvent.setStatusImg(statusImg);
 	        	Calendar itemDate = (Calendar)currentDate.clone();
 	        	itemDate.add(Calendar.DATE, Integer.parseInt(dateIndex));
 	        	uuEvent.setTime(time, itemDate);
 	        	
 	        	// we do not want rejected and "not participated" events, do not include them in the resulting map
 	        	if (allowEvent(uuEvent)) {
 	        		result.put(uuEvent.getId(), uuEvent);
 		        	log.debug(uuEvent.toString());
 	        	}
 	        	nextIndex = itemEndIndex;
 	        }
 	        
 	        // go to next week
 	        data = clickLink("dwdiary-normal-link-next", data);
 	        currentDate = CalendarUtils.stringToDate(getDateStringForCurrentWeek(data));
         }
         
         // logout
         data = doGet(HtmlParser.extractRegExp(data, "href:\"([^\"]*)\",icon:UES.Util.getRealImagePath\\(\"/images/touchicons/RED/TI_LOGOUT_RED.png\""));
         
         log.info("Total KBs read: " + (totalBytes / 1024));
         log.info("Total events: " + result.size());
         return result;
 	}
 
 }
