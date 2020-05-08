 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.web;
 
 import org.mule.api.annotations.Schedule;
 import org.mule.ibeans.bitly.BitlyIBean;
 import org.mule.ibeans.channels.FEED;
 import org.mule.ibeans.twitter.TwitterIBean;
 import org.mule.module.annotationx.api.Receive;
 import org.mule.module.annotationx.api.Service;
 
 import java.net.URL;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.abdera.model.Entry;
 import org.ibeans.annotation.IntegrationBean;
 
 /**
  * A simple reader example that reads an atom feed an generates a tweet for each of the entries
  */
 @Service
 public class BlogFeedToTwitterReader
 {
     public static final String BLOG_URL = "atom:http://rossmason.blogspot.com/feeds/posts/default";
 
     @IntegrationBean
     private BitlyIBean bitly;
 
     @IntegrationBean
     private TwitterIBean twitter;
 
     @PostConstruct
     public void initialise()
     {
         //Initialise the Bit.ly iBean
         bitly.init("bitlyapidemo", "R_0da49e0a9118ff35f52f629d2d71bf07");
 
        // TODO
         //Initialise the Twitter iBean
        //twitter.setCredentials("ibeanstest", "ibeans1234");
     }
 
     @Schedule(interval = 6000)
     @Receive(uri = BLOG_URL, properties = FEED.LAST_UPDATE_DATE + "=2009-08-01")
     public void readFeed(Entry entry) throws Exception
     {
         String url = entry.getAlternateLink().getHref().toString();
         StringBuffer t = new StringBuffer(140);
         t.append(entry.getTitle());
 
         String tweet = entry.getTitle();
         if (t.length() > 120)
         {
             t.delete(120, t.length()).append("..");
         }
         URL shortUrl = bitly.getShortenedURL(url);
         t.append(" : ").append(shortUrl);
         //twitter.statusesUpdate(tweet + ": " + shortUrl + " #ibeans");
     }
 }
