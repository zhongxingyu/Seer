 package com.bryanmarty.ergoproxy;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class HttpParser {
 	
 	//Request
 	public static final Pattern pGetVersion = Pattern.compile("^(GET|POST|HEAD)[\\W]{1}(.*)[\\W]{1}HTTP/(.*)$",Pattern.MULTILINE);
 	public static final Pattern pHost = Pattern.compile("^Host:[\\W]+(.*)$",Pattern.MULTILINE);
 	public static HttpRequest parse(String info) {
 		HttpRequest request = new HttpRequest();
 
 		Matcher m = pGetVersion.matcher(info);
 		if(m.find()) {
 			try{
 				//Try and get the relative url
 				String file = m.group(2).trim();
 				URL url = new URL(file);
				info = m.replaceFirst(m.group(1).trim() + " " + url.getFile() + " HTTP/" + m.group(3));
 			} catch (MalformedURLException mue) {
 				// We will use the original request
 			}
 		}
 		
 		m = pHost.matcher(info);
 		if(m.find()) {
 			//Try and handle if the Host field contains a port number
 			String host = m.group(1).trim();
 			String[] pieces = host.split(":");
 			if(pieces.length == 1) {
 				request.setHost(host);
 			}
 			if(pieces.length == 2) {
 				request.setHost(pieces[0]);
 				request.setPort(Integer.valueOf(pieces[1]));
 			}
 		}
 		
 		request.setRequest(info);
 		return request;
 	}
 
 }
  
