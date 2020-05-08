 package com.appspot.yapkke.stanford.gindroid;
 
 import org.apache.http.client.*;
 import org.apache.http.client.protocol.*;
 import org.apache.http.client.methods.*;
 import org.apache.http.client.entity.*;
 import org.apache.http.impl.client.*;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.message.*;
 import org.apache.http.protocol.*;
 import org.apache.http.*;
 
 import java.io.*;
 import java.util.*;
 
 import android.util.Log;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.*;
 import org.jsoup.select.*;
 
 /** Class that interacts with gin.stanford.edu
  *
  * Done through HTTP client
  *
  * @author ykk
  * @date Nov 2011
  */
 public class WebGin
 {
     /** Name of class
      */
     private final String name = "WebGin";
 
     /** Reference to HTTP client
      */
     HttpClient httpClient;
     
     /** Constructor
      *
      * Create new HTTP client that handles cookies.
      */
     public WebGin()
     {
 	if (httpClient == null)
 	{
 	    httpClient = new DefaultHttpClient();
 	    CookieStore cookieStore = new BasicCookieStore();
 	    HttpContext localContext = new BasicHttpContext();
 	    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 
 	    Log.d(name, "New HTTPClient");
 	}
     }
 
     /** Class to represent event
      */
     class Event
     {
 	public String title = "";
 	public String classroom = "";
 	public String date = "";
 	public Integer start = new Integer(0);
 	public Integer end = new Integer(0);
 	public String series = "";
 	public String owner = "";
 	public String url = "";
 
 	public String toString()
 	{
 	    String s = "Title:"+title+"\n";
 	    s += "Classroom:"+classroom+"\n";
 	    s += "Duration:"+date+" "+start.toString()+"-"+end.toString()+"\n";
 	    s += "URL:"+url;
 	    return s;
 	}
 
 	public String time24Hr(Integer i)
 	{
 	    String is = i.toString().trim();
 	    while (is.length() < 4)
 		is = "0"+is;
 	    return is;
 	}
 
 	public String timeTitle()
 	{
 	    return time()+":"+title;
 	}
 
 	public String time()
 	{
 	    return time24Hr(start)+"-"+time24Hr(end);
 	}
     }
 
     class Listing
     {
 	Vector<Event> events = new Vector<Event>();
 	Vector<String> classrooms = new Vector<String>();
     }
 
     /** Get today's brief listing
      * 
      * Return only title, date, start and end
      */
     public Listing briefListing()
     {
 	Listing listing = new Listing();
 	Log.d(name, "Get brief listing");
 
 	//Get page
 	HttpGet request = new HttpGet("https://gin.stanford.edu/showschedule.php");
 	String response = httpRequest(request);
 	Document doc = Jsoup.parse(response);
	
 	//Get headers of table
 	String date = "";
 	Elements ths = doc.body().getElementsByTag("th");
 	for (Element th : ths)
 	{
 	    Element nobr = getFirstElement(th.getElementsByTag("nobr"));
 	    if (nobr != null)
 	    {
 		//Get date
 		date = nobr.text().trim();
 		Log.d(name, "Date:"+date);
 	    }
 
 	    Elements as = th.getElementsByTag("a");
 	    if ((nobr == null) && (as.size() > 0))
 		for (Element a : as)
 		    if (a.toString().indexOf("inspectclassroom") != -1)
 			listing.classrooms.add(a.text().trim());
 	}
 
 	//Get rows
 	int[] ints = new int[listing.classrooms.size()];
 	for (int i = 0; i < listing.classrooms.size(); i++)
 	    ints[i] = 0;
 	Elements trs = doc.body().getElementsByTag("tr");
 	for (Element tr : trs)
 	{
 	    Elements divs = tr.getElementsByTag("div");
 	    if (divs.size() > 0)
 	    {
 		Elements tds = tr.getElementsByTag("td");
 		int tdIndex = 0;
 
 		for (Element td : tds)
 		{
 		    Element div = getFirstElement(td.getElementsByTag("div"));
 
 		    //Check if td is used for time (left column)
 		    if ((div != null) || (td.text().trim().length() == 0))
 		    {
 			//Account to ongoing event in table
 			while (ints[tdIndex] > 0)
 			{
 			    ints[tdIndex]--;
 			    tdIndex++;
 			}
 		    
 			//Get event
 			if (div != null)
 			{
 			    //Get rowspan
 			    String rsStr = td.attr("rowspan");
 			    if (rsStr.length() == 0)
 				rsStr = "1";
 			    int rs = new Integer(rsStr).intValue();
 			    ints[tdIndex] = rs-1;
 			    
 			    //Set event
 			    Event e = new Event();
 			    e.date = date;
 			    e.classroom = listing.classrooms.get(tdIndex); 
 			    Element a = getFirstElement(div.getElementsByTag("a"));
 			    e.url = a.attr("href");
 			    e.title = a.text().trim();
 			    getStartEnd(e, getFirstElement(div.getElementsByTag("nobr")).text());
 			    
 			    listing.events.add(e);
 			}
 			tdIndex++;
 		    }
 		}
 	    }
 	    else
 	    {
 		for (int i = 0; i < ints.length; i++)
 		    ints[i]--;
 	    }
 	}
 
 	return listing;
     }
 
     /** Get start and end time
      */
     protected void getStartEnd(Event e, String time)
     {
 	int splitIndex = time.indexOf("-");
 	if (splitIndex == -1)
 	    return;
 
 	e.start = get24Hr(time.substring(0,splitIndex));
 	e.end = get24Hr(time.substring(splitIndex+1));
     }
 
     /** Time string to 24 hour valie
      */
     protected Integer get24Hr(String time)
     {
 	String t = time.trim();
 	Integer timeNum = new Integer(t.substring(0,t.length()-2).replaceAll(":","").trim());
 	if ((t.substring(t.length()-2).compareTo("pm") == 0) && (timeNum < 1200))
 	    timeNum += 1200;
 	
 	return timeNum;
     }
 
     /** Get first element by tag
      *
      * @param elements list of elements
      * @return first element or null
      */
     public Element getFirstElement(Elements elements)
     {
 	for (Element e : elements)
 	    return e;
 
 	return null;
     }
 
     /** Authentication
      *
      * @param username username i.e., CS id
      * @param password password for username
      */
     public void auth(String username, String password)
     {
 	//Get form for authentication
 	HttpGet request = new HttpGet("https://gin.stanford.edu");
 	String response = httpRequest(request);
 
 	//Parse from form
 	List<NameValuePair> nameValList = new ArrayList<NameValuePair>();
 	Document doc = Jsoup.parse(response);
 	Elements forms = doc.body().getElementsByTag("form");
 	for (Element form : forms)
 	{
 	    Elements usernameE = form.getElementsByAttributeValue("name", "username");
 	    Elements passwordE = form.getElementsByAttributeValue("name", "password");
 	    if ((usernameE.size() == 1) && (passwordE.size() == 1))
 	    {
 		Elements inputs = form.getElementsByTag("input");
 		for (Element input : inputs)
 		{
 		    String n = input.attr("name");
 		    String v = input.attr("value");
 		    if (n.compareTo("username") == 0)
 			v = username;
 		    if (n.compareTo("password") == 0)
 			v = password;
 		    nameValList.add(new BasicNameValuePair(n, v));
 		}
 	    }
 	}
 
 	//Post for WebAuth
 	HttpPost httpPost = new HttpPost("https://cs.stanford.edu/login");
 	String postResponse;
 	try
 	{
 	    httpPost.setEntity(new UrlEncodedFormEntity(nameValList));
 	    postResponse = httpRequest(httpPost);
 	} catch (UnsupportedEncodingException e)
 	{
 	    postResponse = "";
 	    Log.d(name, "Error:"+e.toString());
 	}
     }
 
     /** Send HTTP request (get or post)
      *
      * @param request request to be sent
     * @return response as a string
      */
     public String httpRequest(HttpUriRequest request)
     {
 	ResponseHandler<String> responseHandler = new BasicResponseHandler();
	String response;
 	try
 	{
 	    response = httpClient.execute(request, responseHandler);   
 	} catch (IOException e)
 	{
	    response = "";
 	    Log.d(name, "Error:"+e.toString());
 	}
 
 	return response;
     }
 }
