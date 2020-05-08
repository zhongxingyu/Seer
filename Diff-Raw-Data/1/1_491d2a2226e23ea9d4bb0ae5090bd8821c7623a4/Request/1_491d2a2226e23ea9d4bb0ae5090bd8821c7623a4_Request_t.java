 package com.arlandis;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Request implements RequestInterface{
     private String requestToParse;
     private final Integer NUM_DELIMITERS = 6;
     private String body;
 
     public Request(String request) {
         requestToParse = request;
     }
 
     public Integer bytesToRead() {
         Integer START_LOCATION = requestToParse.indexOf("Content-Length: ") + 16;
         Pattern intsOnly = Pattern.compile("\\d+");
         Matcher matcher = intsOnly.matcher(requestToParse.substring(START_LOCATION, requestToParse.length()));
         matcher.find();
         String inputInt = matcher.group();
         return Integer.parseInt(inputInt);
     }
 
     public Boolean hasBody(){
         return requestToParse.contains("Content-Length");
     }
 
     public String headers(){
         return requestToParse;
     }
 
     public void setBody(String body) {
         this.body = body;
     }
 
     public String getBody(){
         return body;
     }
 }
