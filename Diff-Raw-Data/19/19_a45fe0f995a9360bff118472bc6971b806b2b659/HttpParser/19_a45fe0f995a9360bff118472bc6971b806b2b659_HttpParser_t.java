 package com.bryanmarty.ergoproxy;
 
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class HttpParser {
 	
 	//Request
 	public static final Pattern pGetVersion = Pattern.compile("^(GET|POST|HEAD)[\\W]{1}(.*)[\\W]{1}HTTP/(.*)$",Pattern.MULTILINE);
 	public static final Pattern pHost = Pattern.compile("^Host:[\\W]+(.*)$",Pattern.MULTILINE);
 	public static final Pattern pConnection = Pattern.compile("^Connection:[\\W]+(.*)$",Pattern.MULTILINE);
 	public static final Pattern pPConnection = Pattern.compile("^Proxy-Connection:[\\W]+(.*)$",Pattern.MULTILINE);
 	public static HttpRequest parse(String info) {
 		HttpRequest request = new HttpRequest();
 
 		
 		Matcher m = pGetVersion.matcher(info);
 		if(m.find()) {
 			request.setVersion(m.group(3).trim());
 		}
 		
 		m = pHost.matcher(info);
 		if(m.find()) {
 			request.setHost(m.group(1).trim());
 		}
		/*if (request.getVersion().contentEquals("1.1")) {
 			m = pConnection.matcher(info);
 			if(m.find()) {
 				info = m.replaceFirst("Connection: close\r\n");
 			}
 			
 			m = pPConnection.matcher(info);
 			if(m.find()) {
 				info = m.replaceFirst("Proxy-Connection: close");
 			}
		}*/
 		
 		System.out.println(info);
 		request.setRequest(info);
 		return request;
 	}
 
 }
  
