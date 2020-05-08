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
 
 import org.mule.ibeans.test.IBeansRITestSupport;
 import org.mule.ibeans.twitter.TwitterIBean;
 import org.mule.module.json.JsonData;
 
 import org.ibeans.annotation.IntegrationBean;
 import org.ibeans.api.IBeansException;
 import org.junit.Before;
 import org.junit.Test;
 
 public class BlogToTwitterTestCase extends IBeansRITestSupport
 {
     @IntegrationBean
     private TwitterIBean twitter;
 
     @Before
     public void init() throws IBeansException
     {
         registerBeans(new BlogFeedToTwitterReader());
 
        twitter.setCredentials("${twitter.username}", "${twitter.password}");
         twitter.setFormat(TwitterIBean.FORMAT.JSON, JsonData.class);
     }
 
     @Test
     public void flickrSearchWithEmail() throws Exception
     {
         //lets wait for the scheduler to kick in
         Thread.sleep(13000);
         System.out.println("DONE");
 //        JsonData data = twitter.getUserTimeline("ibeanstest", 1);
 //        assertNotNull(data);
 //        assertTrue(data.get("text").toString().endsWith("#ibeans"));
     }
 
 }
