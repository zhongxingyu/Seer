 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package org.komusubi.feeder.bind;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.json.Json;
 import javax.json.stream.JsonParser;
 import javax.json.stream.JsonParser.Event;
 import javax.json.stream.JsonParserFactory;
 
 import org.komusubi.feeder.FeederException;
 import org.komusubi.feeder.spi.UrlShortening;
 import org.komusubi.feeder.utils.ResourceBundleMessage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author jun.ozeki
  */
 public class BitlyUrlShortening implements UrlShortening {
 
     private static final Logger logger = LoggerFactory.getLogger(BitlyUrlShortening.class);
     private static final ResourceBundleMessage RESOURCE = new ResourceBundleMessage(BitlyUrlShortening.class);
     private static final String ACCESS_KEY_VALUE = "bitly.access_token";
     private static final Properties PROPERTIES;
 
     static {
         PROPERTIES = new Properties();
         try {
            PROPERTIES.load(BitlyUrlShortening.class.getClassLoader().getResourceAsStream("accessKey.properties"));
         } catch (Exception e) {
             logger.error("error load properties: {}", e);
             e.printStackTrace();
        }
     }
 
     /**
      * create new instance.
      */
     public BitlyUrlShortening() {
 
     }
 
     /**
      * @see org.komusubi.feeder.spi.UrlShortening#shorten()
      */
     @SuppressWarnings("incomplete-switch")
     @Override
     public URL shorten(URL url) {
         try {
             StringBuilder urlBuilder = new StringBuilder(RESOURCE.getString("shorten.url"));
             if (!urlBuilder.toString().endsWith("?")) {
                 urlBuilder.append("?");
             }
             urlBuilder.append("access_token=")
                         .append(PROPERTIES.getProperty(ACCESS_KEY_VALUE))
                         .append("&")
                         .append(RESOURCE.getString("shorten.param.longUrl"))
                         .append("=")
                         .append(URLEncoder.encode(url.toExternalForm(), "Shift_JIS"));
 
             logger.debug("request shorten url: {}", url.toExternalForm());
             URL bitly = new URL(urlBuilder.toString());
             URLConnection con = bitly.openConnection();
             con.connect();
             try (BufferedInputStream bis = new BufferedInputStream(con.getInputStream())) {
                 JsonParserFactory factory = Json.createParserFactory(null);
                 JsonParser parser = factory.createParser(bis, Charset.forName("UTF-8"));
                 Map<String, String> map = new HashMap<>();
                 String key = null;
                 while (parser.hasNext()) {
                     Event event = parser.next();
                     switch (event) {
                     case KEY_NAME:
                         key = parser.getString();
                         logger.trace("key name: {}", key);
                         break;
                     case VALUE_STRING:
                         map.put(key, parser.getString());
                         logger.trace("value: {}", parser.getString());
                         break;
                     }
                 }
                 URL shortened;
                 if ("ALREADY_A_BITLY_LINK".equals(map.get("status_txt")))
                     shortened = url;
                 else
                     shortened = new URL(map.get("url"));
                 return shortened;
             }
         } catch (IOException e) {
             throw new FeederException(e);
         }
     }
 
     /**
      * @see org.komusubi.feeder.spi.UrlShortening#shorten(java.lang.String)
      */
     @Override
     public URL shorten(String url) {
         try {
             return shorten(new URL(url));
         } catch (MalformedURLException e) {
             throw new FeederException(e);
         }
     }
 
 }
