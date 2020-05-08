 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.application;
 
 import java.io.File;
 
 import com.nginious.http.common.FileUtils;
 import com.nginious.http.server.FileLogConsumer;
 import com.nginious.http.server.HttpServer;
 import com.nginious.http.server.HttpServerConfiguration;
 import com.nginious.http.server.HttpServerFactory;
 import com.nginious.http.server.HttpTestConnection;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 public class Http11ServiceTestCase extends TestCase {
 	
 	private HttpServer server;
 	
 	private File tmpDir;
 	
     public Http11ServiceTestCase() {
 		super();
 	}
 
 	public Http11ServiceTestCase(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.tmpDir = new File(System.getProperty("java.io.tmpdir"), "webapps");
 		tmpDir.mkdir();
 		File destFile = new File(this.tmpDir, "test.war");
		FileUtils.copyFile("build/libs/nginious-server-1.0.0-testweb.war", destFile.getAbsolutePath());
 		HttpServerConfiguration config = new HttpServerConfiguration();
 		config.setWebappsDir(tmpDir.getAbsolutePath());
 		config.setPort(9000);
 		HttpServerFactory factory = HttpServerFactory.getInstance();
 		this.server = factory.create(config);
 		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
 		server.setMessageLogConsumer(new FileLogConsumer("build/test-server"));
 		server.start();
 	}
 
 	protected void tearDown() throws Exception {
 		if(this.server != null) {
 			server.stop();
 		}
 
 		FileUtils.deleteDir(this.tmpDir);
 	}
 	
 	public void testHttpServiceOptions() throws Exception {
 		String request = "OPTIONS /test/methods HTTP/1.1\015\012" + 
 			"Host: localhost\015\012" +
 			"Content-Length: 0\015\012" + 
 			"Connection: close\015\012\015\012";
 		
 		String expectedResponse = "HTTP/1.1 200 OK\015\012" +
 			"Date: <date>\015\012" + 
 			"Allow: HEAD, GET, POST, PUT, DELETE\015\012" + 
 			"Content-Length: 0\015\012" +
 			"Connection: close\015\012" +
 			"Server: Nginious/1.0.0\015\012\015\012";
 		
 		HttpTestConnection conn = null;
 		
 		try {
 			conn = new HttpTestConnection();
 			conn.write(request);
 			
 			String response = conn.readString();
 			expectedResponse = conn.setHeaders(response, expectedResponse);
 			assertEquals(expectedResponse, response);			
 		} finally {
 			if(conn != null) {
 				conn.close();
 			}
 		}		
 	}
 	
 	public void testDefaultGet() throws Exception {
 		String request = "GET /test/default HTTP/1.1\015\012" + 
 			"Host: localhost\015\012" +
 			"Content-Length: 0\015\012" + 
 			"Connection: close\015\012\015\012";
 		
 		String expectedResponse = "HTTP/1.1 405 Not Allowed\015\012" +
 			"Content-Type: text/html; charset=utf-8\015\012" +
 			"Date: <date>\015\012" + 
 			"Content-Length: 74\015\012" +
 			"Connection: close\015\012" +
 			"Server: Nginious/1.0.0\015\012\015\012" +
 			"<html><body><h1>405 Not Allowed: GET method not allowed</h1></body></html>";
 		
 		HttpTestConnection conn = null;
 		
 		try {
 			conn = new HttpTestConnection();
 			conn.write(request);
 			
 			String response = conn.readString();
 			expectedResponse = conn.setHeaders(response, expectedResponse);
 			assertEquals(expectedResponse, response);			
 		} finally {
 			if(conn != null) {
 				conn.close();
 			}
 		}		
 	}
 	
 	public void testDefaultPost() throws Exception {
 		String request = "POST /test/default HTTP/1.1\015\012" + 
 			"Host: localhost\015\012" +
 			"Content-Length: 4\015\012" + 
 			"Connection: close\015\012\015\012" +
 			"Test";
 		
 		String expectedResponse = "HTTP/1.1 405 Not Allowed\015\012" +
 			"Content-Type: text/html; charset=utf-8\015\012" +
 			"Date: <date>\015\012" + 
 			"Content-Length: 75\015\012" +
 			"Connection: close\015\012" +
 			"Server: Nginious/1.0.0\015\012\015\012" +
 			"<html><body><h1>405 Not Allowed: POST method not allowed</h1></body></html>";
 		
 		HttpTestConnection conn = null;
 		
 		try {
 			conn = new HttpTestConnection();
 			conn.write(request);
 			
 			String response = conn.readString();
 			expectedResponse = conn.setHeaders(response, expectedResponse);
 			assertEquals(expectedResponse, response);			
 		} finally {
 			if(conn != null) {
 				conn.close();
 			}
 		}		
 	}
 	
 	public void testDefaultPut() throws Exception {
 		String request = "PUT /test/default HTTP/1.1\015\012" + 
 			"Host: localhost\015\012" +
 			"Content-Length: 4\015\012" + 
 			"Connection: close\015\012\015\012" +
 			"Test";
 		
 		String expectedResponse = "HTTP/1.1 405 Not Allowed\015\012" +
 			"Content-Type: text/html; charset=utf-8\015\012" +
 			"Date: <date>\015\012" + 
 			"Content-Length: 74\015\012" +
 			"Connection: close\015\012" +
 			"Server: Nginious/1.0.0\015\012\015\012" +
 			"<html><body><h1>405 Not Allowed: PUT method not allowed</h1></body></html>";
 		
 		HttpTestConnection conn = null;
 		
 		try {
 			conn = new HttpTestConnection();
 			conn.write(request);
 			
 			String response = conn.readString();
 			expectedResponse = conn.setHeaders(response, expectedResponse);
 			assertEquals(expectedResponse, response);			
 		} finally {
 			if(conn != null) {
 				conn.close();
 			}
 		}		
 	}
 	
 	public void testDefaultDelete() throws Exception {
 		String request = "DELETE /test/default HTTP/1.1\015\012" + 
 			"Host: localhost\015\012" +
 			"Content-Length: 4\015\012" + 
 			"Connection: close\015\012\015\012" +
 			"Test";
 		
 		String expectedResponse = "HTTP/1.1 405 Not Allowed\015\012" +
 			"Content-Type: text/html; charset=utf-8\015\012" +
 			"Date: <date>\015\012" + 
 			"Content-Length: 77\015\012" +
 			"Connection: close\015\012" +
 			"Server: Nginious/1.0.0\015\012\015\012" +
 			"<html><body><h1>405 Not Allowed: DELETE method not allowed</h1></body></html>";
 		
 		HttpTestConnection conn = null;
 		
 		try {
 			conn = new HttpTestConnection();
 			conn.write(request);
 			
 			String response = conn.readString();
 			expectedResponse = conn.setHeaders(response, expectedResponse);
 			assertEquals(expectedResponse, response);			
 		} finally {
 			if(conn != null) {
 				conn.close();
 			}
 		}		
 	}
 	
 	public static Test suite() {
 		return new TestSuite(Http11ServiceTestCase.class);
 	}
 
 	public static void main(String[] argv) {
 		junit.textui.TestRunner.run(suite());
 	}
 }
