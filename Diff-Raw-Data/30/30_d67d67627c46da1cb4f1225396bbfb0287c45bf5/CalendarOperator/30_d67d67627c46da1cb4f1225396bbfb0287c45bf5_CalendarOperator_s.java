 package com.roclas.googleApis;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 
 public class CalendarOperator {
 	
 	private String user;
 	private String password;
 	private String priv;
 
 
 	public CalendarOperator(String u, String pass, String p) {
 		user=u;
 		password=pass;
 		priv=p;
 	}
 
 	public String convertStreamToString(java.io.InputStream is) {
 	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
 	    return s.hasNext() ? s.next() : "";
 	}
 	
     public String timenow() {  
 		return stringCurrentDate("yyyy-MM-dd")+"T"+stringCurrentDate("HH:mm:ss");
     }
     
     public String stringCurrentDate(String format) {  
     	//stringCurrentDate("yyyy-MM-dd")
         Calendar calendar = Calendar.getInstance();
         SimpleDateFormat dateFormat = new SimpleDateFormat(format);
         String result=null;
         try { result = "" + dateFormat.format(calendar.getTime());
         } catch (Exception e) {
             e.printStackTrace();
         }
 		return result;
     }
 	
 	
     public String fetchToken() throws IOException {
     	URL url = new URL("https://www.google.com/accounts/ClientLogin");
     	//Open the Connection
     	HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
     	urlConnection.setRequestMethod("POST");
     	urlConnection.setDoInput(true);
     	urlConnection.setDoOutput(true);
     	urlConnection.setUseCaches(false);
     	urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
 
     	// Form the POST parameters
     	StringBuilder content = new StringBuilder();
     	content.append("Email=").append(URLEncoder.encode(user, "UTF-8"));
     	content.append("&Passwd=").append(URLEncoder.encode(password, "UTF-8"));
     	content.append("&service=").append(URLEncoder.encode("cl", "UTF-8"));
     	content.append("&accountType=").append(URLEncoder.encode("GOOGLE", "UTF-8"));
     	//content.append("&service=").append(URLEncoder.encode(yourapp, "UTF-8"));
     	OutputStream outputStream = urlConnection.getOutputStream();
     	outputStream.write(content.toString().getBytes("UTF-8"));
     	outputStream.close();
 
     	// Retrieve the output
     	int responseCode = urlConnection.getResponseCode();
     	InputStream inputStream;
     	if (responseCode == HttpURLConnection.HTTP_OK) {
     	  inputStream = urlConnection.getInputStream();
     	} else {  inputStream = urlConnection.getErrorStream();
     	}
     	String completeAuth = convertStreamToString(inputStream);
     	//System.out.println(completeAuth);
     	return completeAuth.split("Auth=")[1];
     }
     
     
     public Map<String,Map<String,String>> listEvents() throws IOException { 
     	String token=fetchToken();
     	System.out.println(token);
     	String today= stringCurrentDate("yyyy-MM-dd");
     	String url= "https://www.google.com/calendar/feeds/"+user+"/private-"+priv+"/full?start-min="+today+"T00:00:00&start-max="+today+"T23:59:59";
     	URL u = new URL(url);
     	HttpURLConnection connection = (HttpURLConnection) u.openConnection();           
     	connection.setDoOutput(true);
     	connection.setDoInput(true);
     	connection.setInstanceFollowRedirects(false); 
     	connection.setRequestMethod("GET"); 
     	String a = connection.getRequestMethod();
     	connection.setRequestProperty("Content-Type", "application/atom+xml");
     	connection.setRequestProperty("Accept-Charset", "utf-8");
     	connection.setRequestProperty("Authorization", ("GoogleLogin auth="+token).trim());
     	connection.setUseCaches(false);
 
     	// Retrieve the output
     	System.out.println(url);
     	String line,result="";
     	BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
     	while ((line = rd.readLine()) != null) {  result+=line;    	}
     	rd.close();
     	//System.out.println(result);
     	Map<String, Map<String, String>> res=new HashMap<String, Map<String, String>>() ;
 		SAXBuilder builder = new SAXBuilder();
 		try {
 			Document document = (Document) builder.build(new StringReader(result));
 			Element rootNode = document.getRootElement();
 			List list = rootNode.getChildren();
 			for (int i = 0; i < list.size(); i++) {
 				Element node = (Element) list.get(i);
 				String name = node.getName();
 				if (name.equalsIgnoreCase("entry")) {
 					List list2 = node.getChildren();
 					Map<String, String> r=new HashMap<String, String>();
 					for (int j = 0; j < list2.size(); j++) {
 						Element node2 = (Element) list2.get(j);
 						String name2 = node2.getName();
 						String value2= null;
 						if(name2.equalsIgnoreCase("when")){
 							value2= node2.getAttributeValue("startTime");
 							r.put("startTime", value2);
 							value2= node2.getAttributeValue("endTime");
 							r.put("endTime", value2);
 						}else{
 							value2= node2.getText();
 							r.put(name2, value2);
 						}
 					}
 					res.put("EVENT "+r.get("title"), r);
 				}
 			}
 		} catch (Exception io) { System.out.println(io.getMessage()); } 
 		return res;
     }
     
     public void createEvent(String start, String stop, String title) throws IOException{ 
     	createEvent(null,start,stop,title);
     }
     
     public void createEvent(String session,String start, String stop, String title) throws IOException{
     	String token=fetchToken();
     	System.out.println(token);
     	String today= stringCurrentDate("yyyy-MM-dd");
     	String url="https://www.google.com/calendar/feeds/default/private/full";
 		if(session!=null) url+="?gsessionid="+session.trim(); 
     	String eventtext=" <entry xmlns='http://www.w3.org/2005/Atom' " +
     			"xmlns:gd='http://schemas.google.com/g/2005'> " +
     			"<category scheme='http://schemas.google.com/g/2005#kind' term='http://schemas.google.com/g/2005#event'></category> " +
     			"<title type='text'>"+title+"</title> <content type='text'>event text</content> " +
     			"<gd:transparency value='http://schemas.google.com/g/2005#event.opaque'> </gd:transparency> " +
     			"<gd:eventStatus value='http://schemas.google.com/g/2005#event.confirmed'> </gd:eventStatus> " +
     			"<gd:where valueString='Rolling Lawn Courts'></gd:where> <gd:when startTime='"+today+"T"+start+":00.000Z' " +
     					"endTime='"+today+"T"+stop+":00.000Z'></gd:when> </entry>";
 
     	//System.out.println(data);
     	URL u = new URL(url);
     	HttpURLConnection connection = (HttpURLConnection) u.openConnection();           
     	connection.setDoOutput(true);
     	connection.setDoInput(true);
     	connection.setInstanceFollowRedirects(false); 
     	connection.setRequestMethod("POST"); 
     	String a = connection.getRequestMethod();
     	connection.setRequestProperty("Content-Type", "application/atom+xml");
     	connection.setRequestProperty("Accept-Charset", "utf-8");
     	connection.setRequestProperty("Authorization", ("GoogleLogin auth="+token).trim());
     	connection.setUseCaches(false);
     	DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
     	wr.writeBytes(eventtext);
     	wr.flush();
 
     	// Retrieve the output
     	System.out.println(url);
     	String line,result="";
     	BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
     	while ((line = rd.readLine()) != null) {  result+=line;    	}
     	rd.close();
     	wr.close();	
     	
     	if(session==null){ 
     		try{   // LA PRIMERA VEZ NOS REDIRIGE A UNA URL CON UNA SESIÓN, HAY QUE COGER SESIÓN Y VOLVER A LLAMAR.
     			session=result.split("gsessionid=")[1].split("\"")[0].trim();
     			System.out.println("first time, no session, redirect, get session="+session);
     			System.out.println(result);
     			createEvent(session.trim(),start,stop,title);
     		}catch(Error e){
     			System.out.println("ERRORRRR: url="+url+"\n"+e.getMessage());
     		}
     	}else{
         	System.out.println("second time, url="+url+" session="+session+"\n"+result);
     	}
     }
 
 	public int datesTimeDiff(String dateNow, String date0) {
 		String[] date0s = date0.split("T");
 		String[] dateNows = dateNow.split("T");
 		
		int day0 = Integer.parseInt(date0s[0].replace("-", ""));
		int dayNow = Integer.parseInt(dateNows[0].replace("-", ""));
 		
		int time0=Integer.parseInt(date0s[1].split("\\.")[0].replace(":",""));
		int timeNow=Integer.parseInt(dateNows[1].split("\\.")[0].replace(":",""));
 		
		int zero=day0*1000000+time0;
		int now=dayNow*1000000+timeNow;
		
		return (zero-now);
 	}
 	
 	public boolean isDateBefore(String dateNow, String date0) {
 		if(datesTimeDiff(dateNow, date0)>0 )return true;
 		return false;
 	}
 	
 	public boolean isDateBetween(String dateNow, String date0, String date1) {
 		return (isDateBefore(date0,dateNow) && isDateBefore(dateNow,date1));
 	}
 	
 	
 	public boolean isNowBetween(String date0, String date1) {
 		return(isDateBetween(timenow(), date0, date1));
 	}
 	
 	public Map<String, String> getCurrentEvent() throws IOException {
 		for (Map<String, String> ev:listEvents().values()){
 			if(isNowBetween(ev.get("startTime"),ev.get("endTime")))return ev;
 		}
 		return null;
 	}
 	
 	public boolean isNowBusy() throws IOException {
 		for (Map<String, String> ev:listEvents().values()){
 			if(isNowBetween(ev.get("startTime"),ev.get("endTime")))return true;
 		}
 		return false;
 	}
 	
 	
 	public Map<String, String> getNextEvent() throws IOException {
 		int hoursaday=24;
 		int minutesxh=60;
 		int secondsxm=60;
 		int maxseconds=hoursaday*minutesxh*secondsxm;
 		Map<String, String> event = null;
 		for (Map<String, String> ev:listEvents().values()){
 			if(isDateBefore(timenow(),ev.get("startTime"))){
 				int newmax=datesTimeDiff(timenow(), ev.get("startTime"));
 				if(newmax<maxseconds){
 					event=ev;
 					maxseconds=newmax;
 				}
 			}
 		}
 		return event;
 	}
 
 
 }
 
 
 
