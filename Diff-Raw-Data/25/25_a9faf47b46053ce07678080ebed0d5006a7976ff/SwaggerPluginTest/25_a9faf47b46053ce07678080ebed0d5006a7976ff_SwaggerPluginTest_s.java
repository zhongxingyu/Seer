 /*
     Copyright 2013, Strategic Gains, Inc.
 
 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at
 
 		http://www.apache.org/licenses/LICENSE-2.0
 
 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
 */
 package com.strategicgains.restexpress.plugin.swagger;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.restexpress.RestExpress;
 
 /**
  * @author toddf
  * @since Nov 22, 2013
  */
 public class SwaggerPluginTest
 {
 	private static final RestExpress SERVER = new RestExpress();
 	private HttpClient http = new DefaultHttpClient();
 
 	@BeforeClass
 	public static void intialize()
 	{
 		RestExpress.getSerializationProvider();
 
 		DummyController controller = new DummyController();
 		SERVER.setBaseUrl("http://localhost:9001");
 		SERVER.uri("/users.{format}", controller)
 			.action("readAll", HttpMethod.GET)
 			.method(HttpMethod.POST)
 			.name("Users Collection");
 
 		SERVER.uri("/users/{userId}.{format}", controller)
 			.method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
 			.name("Individual User");
 
 		SERVER.uri("/users/{userId}/orders.{format}", controller)
 			.action("readAll", HttpMethod.GET)
 			.method(HttpMethod.POST)
 			.name("User Orders Collection");
 
 		SERVER.uri("/orders.{format}", controller)
 			.action("readAll", HttpMethod.GET)
 			.method(HttpMethod.POST)
 			.name("Orders Collection");
 
 		SERVER.uri("/orders/{orderId}.{format}", controller)
 			.method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
 			.name("Individual Order");
 		
 		SERVER.uri("/orders/{orderId}/items.{format}", controller)
 			.action("readAll", HttpMethod.GET)
 			.method(HttpMethod.POST)
 			.name("Order Line-Items Collection");
 		
 		SERVER.uri("/orders/{orderId}/items/{itemId}.{format}", controller)
 			.method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
 			.name("Individual Order Line-Item");
 
 		SERVER.uri("/products.{format}", controller)
 			.action("readAll", HttpMethod.GET)
 			.method(HttpMethod.POST)
 			.name("Orders Collection");
 
 		SERVER.uri("/products/{orderId}.{format}", controller)
 			.method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
 			.name("Individual Order");
 		
 		new SwaggerPlugin()
 			.apiVersion("1.0")
 			.swaggerVersion("1.2")
 			.register(SERVER);
 		
 		SERVER.bind(9001);
 	}
 	
 	@AfterClass
 	public static void shutdown()
 	{
 //		SERVER.awaitShutdown();
 		SERVER.shutdown();
 	}
 
 	@Test
 	public void testApiResourcesRoute()
 	throws ClientProtocolException, IOException
 	{
 		HttpGet request = new HttpGet("http://localhost:9001/api-docs");
 		HttpResponse response = (HttpResponse) http.execute(request);
 		HttpEntity entity = response.getEntity();
 		String json = EntityUtils.toString(entity);
 		System.out.println(json);
 		assertTrue(json.contains("\"apiVersion\":\"1.0\""));
 		assertTrue(json.contains("\"swaggerVersion\":\"1.2\""));
 		assertTrue(json.contains("\"path\":\"/users\""));
 		assertTrue(json.contains("\"path\":\"/orders\""));
 		assertTrue(json.contains("\"path\":\"/products\""));
 		assertFalse(json.contains("\"path\":\"/api-docs\""));
 		request.releaseConnection();
 	}
 
 	@Test
 	public void testApiUsersRoute()
 	throws ClientProtocolException, IOException
 	{
 		HttpGet request = new HttpGet("http://localhost:9001/api-docs/users");
 		HttpResponse response = (HttpResponse) http.execute(request);
 		HttpEntity entity = response.getEntity();
 		String json = EntityUtils.toString(entity);
 		System.out.println(json);
 		assertTrue(json.contains("\"apiVersion\":\"1.0\""));
 		assertTrue(json.contains("\"swaggerVersion\":\"1.2\""));
 		assertTrue(json.contains("\"basePath\":\"http://localhost:9001\""));
 		assertTrue(json.contains("\"resourcePath\":\"/users\""));
 		request.releaseConnection();
 	}
 
 	@Test
 	public void testApiOrdersRoute()
 	throws ClientProtocolException, IOException
 	{
 		HttpGet request = new HttpGet("http://localhost:9001/api-docs/orders");
 		HttpResponse response = (HttpResponse) http.execute(request);
 		HttpEntity entity = response.getEntity();
 		String json = EntityUtils.toString(entity);
 		System.out.println(json);
 		assertTrue(json.contains("\"apiVersion\":\"1.0\""));
 		assertTrue(json.contains("\"swaggerVersion\":\"1.2\""));
 		assertTrue(json.contains("\"basePath\":\"http://localhost:9001\""));
 		assertTrue(json.contains("\"resourcePath\":\"/orders\""));
 		request.releaseConnection();
 	}
 
 	@Test
 	public void testApiProductsRoute()
 	throws ClientProtocolException, IOException
 	{
 		HttpGet request = new HttpGet("http://localhost:9001/api-docs/products");
 		HttpResponse response = (HttpResponse) http.execute(request);
 		HttpEntity entity = response.getEntity();
 		String json = EntityUtils.toString(entity);
 		System.out.println(json);
 		assertTrue(json.contains("\"apiVersion\":\"1.0\""));
 		assertTrue(json.contains("\"swaggerVersion\":\"1.2\""));
 		assertTrue(json.contains("\"basePath\":\"http://localhost:9001\""));
 		assertTrue(json.contains("\"resourcePath\":\"/products\""));
 		request.releaseConnection();
 	}
 }
