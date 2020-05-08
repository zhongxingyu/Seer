 // Copyright (c) 2011 Cilogi All Rights Reserved.
 //
 // File:        GenShortURLGoogle.java  (03-Oct-2011)
 // Author:      Cilogi
 // $Id$
 //
 // Copyright in the whole and every part of this source file belongs to
 // Cilogi (the Author) and may not be used,
 // sold, licenced, transferred, copied or reproduced in whole or in
 // part in any manner or form or in or on any media to any person
 // other than in accordance with the terms of The Author's agreement
 // or otherwise without the prior written consent of The Author.  All
 // information contained in this source file is confidential information
 // belonging to The Author and as such may not be disclosed other
 // than in accordance with the terms of The Author's agreement, or
 // otherwise, without the prior written consent of The Author.  As
 // confidential information this source file must be kept fully and
 // effectively secure at all times.
 //
 
 
 package com.cilogi.util.services;
 
 import com.google.appengine.api.urlfetch.*;
 import com.google.common.base.Charsets;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Formatter;
 import java.util.logging.Logger;
 
 
 public class GenShortURLGoogle extends BaseGen {
     static final Logger LOG = Logger.getLogger(GenShortURLGoogle.class.getName());
 
 
     public GenShortURLGoogle() {
         super("https://www.googleapis.com/urlshortener/v1/url", null, null);
     }
 
     JSONObject getShortenedURLAsJson(String inputURL) throws IOException  {
         URLFetchService fetch = URLFetchServiceFactory.getURLFetchService();
         HTTPRequest request = new HTTPRequest(new URL(baseURL), HTTPMethod.POST);
         HTTPHeader header = new HTTPHeader("Content-Type", "application/json");
         request.setHeader(header);
         String s =  "{\"longUrl\" : \""+inputURL+"\"}";
         byte[] payload = s.getBytes(Charsets.UTF_8);
         request.setPayload(payload);
         HTTPResponse response = fetch.fetch(request);
         if (response.getResponseCode() == 200) {
             String outString = new String(response.getContent(), Charsets.UTF_8);
             JSONObject obj = (JSONObject)JSONValue.parse(outString);
             return obj;
         }  else {
             return new JSONObject();
         }
     }
 
     public String shortURL(String urlString) {
         try {
             JSONObject json = cache.get(urlString);
             return (json.size() == 0) ? urlString : (String)json.get("id");
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public String qrURL(String message, int size) {
         try {
             String shortURI = new GenShortURLGoogle().shortURL(message);
             String query = new Formatter().format("chs=%dx%d&cht=qr&chl=%s", size, size, shortURI).toString();
             URI uri  = new URI("http", null, "chart.googleapis.com", 80, "/chart", query, null);
             return uri.toString();
         } catch (URISyntaxException e) {
             throw new RuntimeException(e);
         }
     }
 
     
 }
