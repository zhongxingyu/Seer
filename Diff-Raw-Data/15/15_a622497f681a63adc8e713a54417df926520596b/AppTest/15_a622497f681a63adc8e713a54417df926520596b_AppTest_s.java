 /*
  * This is a utility project for wide range of applications
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.rest.atom;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import java.io.File;
 import junit.framework.TestCase;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest {
 
   private static Server jettyServer;

   private Client client;
 
   /**
    * Create the test case
    * @param testName name of the test case
    */
   public AppTest() {
   }
 
   @BeforeClass
   public static void setupServer() throws Exception {
     System.out.println("::: Starting server :::");
     jettyServer = new Server(9090);
     final String webapp = "./src/test/webapp";
     if (!new File(webapp).exists()) {
       throw new IllegalStateException("WebApp dir does not exist!");
     }
     WebAppContext webAppContext = new WebAppContext(webapp, "/");
     jettyServer.setHandler(webAppContext);
     jettyServer.start();
   }
 
   @AfterClass
   public static void shutdownServer() throws Exception {
     System.out.println("::: Stopping server :::");
     jettyServer.stop();
   }
 
   @Before
   public void setup() {
     DefaultClientConfig config = new DefaultClientConfig();
     client = Client.create(config);
   }
 
   @Test
   public void testSimpleGet() {
     System.out.println("::: testSimpleGet :::");
     WebResource resource = client.resource("http://localhost:9090/");
     TestCase.assertEquals(204, resource.head().getStatus());
   }
 
   @Test
   public void testFeedReader() {
     System.out.println("::: testFeedReader :::");
   }
 
   @Test
   public void testPaginatedWrapper() {
     System.out.println("::: testPaginatedWrapper :::");
   }
 }
