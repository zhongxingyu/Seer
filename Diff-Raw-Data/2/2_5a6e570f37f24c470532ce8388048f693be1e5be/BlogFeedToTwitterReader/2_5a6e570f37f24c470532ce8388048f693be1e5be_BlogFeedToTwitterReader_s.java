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
 
 import org.mule.api.lifecycle.Initialisable;
 import org.mule.api.lifecycle.InitialisationException;
 import org.mule.config.annotations.routing.ExpressionFilter;
 import org.mule.ibeans.api.application.Receive;
 import org.mule.ibeans.api.client.IntegrationBean;
 import org.mule.ibeans.bitly.BitlyIBean;
 import org.mule.ibeans.twitter.TwitterIBean;
 import org.mule.module.json.JsonData;
 
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 
 /**
  * A simple reader example that reads an atom feed an generates a tweet for each of the entries
  */
 public class BlogFeedToTwitterReader implements Initialisable
 {
     @IntegrationBean
     private BitlyIBean bitly;
 
     @IntegrationBean
     private TwitterIBean twitter;
 
     public void initialise() throws InitialisationException
     {
         //Initialise the Bit.ly iBean
         bitly.init("bitlyapidemo", "R_0da49e0a9118ff35f52f629d2d71bf07");
        bitly.setFormat("json", JsonData.class);
 
         //Initialise the Twitter iBean
         twitter.setCredentials("muletest", "mule1234");
     }
 
     @Receive(uri = "http://rossmason.blogspot.com/feeds/posts/default")
     @ExpressionFilter("header:Content-Length!=0")
     public void readFeed(Feed feed) throws Exception
     {
         for (Entry entry : feed.getEntries())
         {
             String url = entry.getAlternateLink().getHref().toString();
             String tweet = entry.getTitle();
             JsonData result = bitly.shorten(url);
             String shortUrl = result.get("results->" + url + "->shortUrl").toString();
             twitter.statusesUpdate(tweet + ": " + shortUrl);
         }
     }
 }
