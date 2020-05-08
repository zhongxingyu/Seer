 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package com.mulesoft.module.dropbox;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DropboxConnectorDriver
 {
 
     private String APP_KEY = "";
     private String APP_SECRET = "";
 
     private String ACCESS_KEY = "";
     private String ACCESS_SECRET = "";
 
     @Before
     public void initContext()
     {
         APP_KEY = System.getenv("dropbox.app.key");
         APP_SECRET = System.getenv("dropbox.app.secret");
 
         ACCESS_KEY = System.getenv("dropbox.access.key");
         ACCESS_SECRET = System.getenv("dropbox.access.secret");
     }
 
     @Test
     public void shouldObtaintAMetada() throws Exception
     {
 
         DropboxConnector connector = new DropboxConnector();
         connector.setAccessToken(ACCESS_KEY);
         connector.setAppKey(APP_KEY);
         connector.setAppSecret(APP_SECRET);
         connector.setServer("api.dropbox.com");
 
        List<String> list = connector.list("/My folder");
        System.out.println(Arrays.toString(list.toArray()));;
     }
 
 }
