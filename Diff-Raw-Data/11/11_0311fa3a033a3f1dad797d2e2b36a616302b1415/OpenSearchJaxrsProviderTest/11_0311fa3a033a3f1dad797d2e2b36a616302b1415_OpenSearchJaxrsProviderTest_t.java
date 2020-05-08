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
 package com.smartitengineering.util.opensearch.jaxrs;
 
import com.smartitengineering.util.opensearch.io.impl.dom.DomIOImplTest;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.client.apache.ApacheHttpClient;
 import java.io.File;
 import junit.framework.Assert;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class OpenSearchJaxrsProviderTest {
 
   private static Server jettyServer;
   private Client client;
 
   @BeforeClass
   public static void setupServer()
       throws Exception {
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
   public static void shutdownServer()
       throws Exception {
     System.out.println("::: Stopping server :::");
     jettyServer.stop();
   }
 
   @Before
   public void setup() {
     DefaultClientConfig config = new DefaultClientConfig();
     client = ApacheHttpClient.create(config);
   }
 
   @Test
  public void testSerialization() {
    System.out.println("::: testSerialization :::");
     WebResource resource = client.resource("http://localhost:9090/");
     resource.accept(MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML_TYPE);
     final ClientResponse response = resource.get(ClientResponse.class);
    final String entity = response.getEntity(String.class);
     Assert.assertEquals(200, response.getStatus());
    Assert.assertEquals(DomIOImplTest.MAX, entity);
   }
 }
