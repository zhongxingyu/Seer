 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It's a bridge between the filemanager and a storage backend and 
  * works like a transparent VFS or proxy.
  * Copyright (C) Thilo Schwarz
  * 
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 package de.thischwa.c5c;
 
 import static org.junit.Assert.*;
 
 import org.eclipse.jetty.testing.HttpTester;
 import org.junit.Test;
 
 public class RootGetRequestTest extends GenericRequestTest {
 
 	@Override
 	protected void initTester() {
 		servletTester.setContextPath("/"); 
 	}
 	
 	@Test
 	public void testGetInfo() throws Exception {
 		HttpTester request = buildInitialRequest(); 
 		request.setURI("/filemanager/connectors/java/filemanager.java?mode=getinfo&path=%2Ffilemanager%2Fuserfiles%2Fpic01.png&time=244");
 		String requestStr = request.generate();
 		
 		String responseStr = servletTester.getResponses(requestStr);
 		HttpTester response = new HttpTester();
 		response.parse(responseStr);
 		
 		assertEquals(200, response.getStatus());
 		String actual = cleanResponse(response.getContent());
		String expected = cleanResponse("{\"Capabilities\":[\"select\",\"delete\",\"rename\",\"download\"],\"Code\":0,\"Error\":\"\",\"File Type\":\"png\",\"Filename\":\"pic01.png\",\"Path\":\"\\/filemanager\\/userfiles\\/pic01.png\",\"Preview\":\"\\/filemanager\\/images\\/fileicons\\/png.png\",\"Properties\":{\"Date Created\":null,\"Height\":70,\"Size\":2250,\"Width\":110}}");
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testGetFolder() throws Exception {
 		HttpTester request = buildInitialRequest();
 		request.setURI("/filemanager/connectors/java/filemanager.java?path=%2Ffilemanager%2Fuserfiles%2F&mode=getfolder&showThumbs=true&time=241");
 		String requestStr = request.generate();
 		
 		String responseStr = servletTester.getResponses(requestStr);
 		HttpTester response = new HttpTester();
 		response.parse(responseStr);
 		
 		assertEquals(200, response.getStatus());
 		String actual = cleanResponse(response.getContent());
 		String epected = cleanResponse("{\"\\/filemanager\\/userfiles\\/folder\\/\":{\"Capabilities\":[\"select\",\"delete\",\"rename\",\"download\"],\"Code\":0,\"Error\":\"\",\"File Type\":\"dir\",\"Filename\":\"folder\",\"Path\":\"\\/filemanager\\/userfiles\\/folder\\/\",\"Preview\":\"\\/filemanager\\/images\\/fileicons\\/_Open.png\",\"Properties\":{\"Date Created\":null,\"Date Modified\":\"11\\/14\\/13\",\"Height\":null,\"Size\":null,\"Width\":null}},\"\\/filemanager\\/userfiles\\/pic01.png\":{\"Capabilities\":[\"select\",\"delete\",\"rename\",\"download\"],\"Code\":0,\"Error\":\"\",\"File Type\":\"png\",\"Filename\":\"pic01.png\",\"Path\":\"\\/filemanager\\/userfiles\\/pic01.png\",\"Preview\":\"\\/filemanager\\/images\\/fileicons\\/png.png\",\"Properties\":{\"Date Created\":null,\"Date Modified\":\"11\\/14\\/13\",\"Height\":70,\"Size\":2250,\"Width\":110}}}");
 		assertEquals(epected, actual);
 	}
 
 }
