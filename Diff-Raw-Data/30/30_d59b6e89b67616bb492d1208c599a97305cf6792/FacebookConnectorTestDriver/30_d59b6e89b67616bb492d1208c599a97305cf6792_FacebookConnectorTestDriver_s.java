 /**
  * Mule Facebook Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.facebook;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test Driver for the connector
  */
 public class FacebookConnectorTestDriver
 {
     /**  */
     private static final String ACCESS_TOKEN = "";
     private FacebookConnector connector;
 
     @Before
     public void setup()
     {
         connector = new FacebookConnector();
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testGetPhoto() throws Exception
     {
         final Map<String, Object> res = connector.getPhoto("347528201978388", "");
         assertNotNull((((List<Map<String, Object>>) res.get("images")).get(0)).get("source"));
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void getAlbum()
     {
         final Map<String, Object> res = connector.getAlbum("20531316728", "");
         assertNotNull(((Map<String, Object>) res.get("cover")).get("cover_id"));
     }
 
     @Test
     public void testPublishMessage() throws Exception
     {
        final Map<String, Object> res = connector.publishMessage(ACCESS_TOKEN, "me", "testFacebookConnector",
             "", "", "", "", "");
         assertNotNull(res);
     }
 
 }
