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
 
 package com.nginious.http.stats;
 
 import java.util.Date;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.custommonkey.xmlunit.XMLTestCase;
 
 import com.nginious.http.HttpStatus;
 import com.nginious.http.server.FileLogConsumer;
 import com.nginious.http.server.HttpServer;
 import com.nginious.http.server.HttpServerConfiguration;
 import com.nginious.http.server.HttpServerFactory;
 import com.nginious.http.server.HttpTestConnection;
 import com.nginious.http.stats.HttpRequestStatistics;
 import com.nginious.http.stats.HttpRequestStatisticsEntry;
 import com.nginious.http.stats.WebSocketSessionStatistics;
 import com.nginious.http.stats.WebSocketSessionStatisticsEntry;
 
 
 public class HttpRequestStatisticsTestCase extends XMLTestCase {
 	
 	private HttpServer server;
 		
 	public HttpRequestStatisticsTestCase() {
 		super();
 	}
 
 	public HttpRequestStatisticsTestCase(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		HttpServerConfiguration config = new HttpServerConfiguration();
 		config.setAdminPwd("admin");
 		config.setWebappsDir(null);
 		config.setPort(9000);
 		HttpServerFactory factory = HttpServerFactory.getInstance();
 		this.server = factory.create(config);
 		server.setAccessLogConsumer(new FileLogConsumer("build/test-access"));
 		server.setMessageLogConsumer(new FileLogConsumer("build/test-server"));
 		server.start();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		
 		if(this.server != null) {
 			server.stop();
 		}
 	}
 	
 	public void testHttpRequestStatistics() throws Exception {
		HttpRequestStatistics stats = new HttpRequestStatistics();
 		long curTimeMillis = System.currentTimeMillis();
 		HttpRequestStatisticsEntry entry = stats.add();
 		
 		long minuteTimeMillis = entry.getMinuteMillis();
 		assertEquals(minuteTimeMillis, curTimeMillis - curTimeMillis % 60000L);
 		assertEquals(0, entry.getNumClientErrorRequests());
 		assertEquals(0, entry.getNumServerErrorRequests());
 		assertEquals(0, entry.getNumSuccessRequests());
 		assertEquals(0, entry.getSumRequestsDuration());
 		assertEquals(0, entry.getSumResponseBytes());
 		
 		entry.update(534L, HttpStatus.OK, 1024);
 		assertEquals(0, entry.getNumClientErrorRequests());
 		assertEquals(0, entry.getNumServerErrorRequests());
 		assertEquals(1, entry.getNumSuccessRequests());
 		assertEquals(534L, entry.getSumRequestsDuration());
 		assertEquals(1024, entry.getSumResponseBytes());
 		
 		entry = stats.add();
 		entry.update(512L, HttpStatus.BAD_REQUEST, 123);
 		assertEquals(1, entry.getNumClientErrorRequests());
 		assertEquals(0, entry.getNumServerErrorRequests());
 		assertEquals(1, entry.getNumSuccessRequests());
 		assertEquals(1046L, entry.getSumRequestsDuration());
 		assertEquals(1147, entry.getSumResponseBytes());
 		
 		entry = stats.add();
 		entry.update(112L, HttpStatus.INTERNAL_SERVER_ERROR, 451);
 		assertEquals(1, entry.getNumClientErrorRequests());
 		assertEquals(1, entry.getNumServerErrorRequests());
 		assertEquals(1, entry.getNumSuccessRequests());
 		assertEquals(1158L, entry.getSumRequestsDuration());
 		assertEquals(1598, entry.getSumResponseBytes());		
 		
		assertEquals(stats.getStartTime().getTime(), curTimeMillis - curTimeMillis % 60000L);
 		assertEquals(stats.getEndTime().getTime(), curTimeMillis - curTimeMillis % 60000L);
		Date startTime = new Date(curTimeMillis - curTimeMillis % 60000L);
 		Date endTime = new Date(curTimeMillis - curTimeMillis % 60000L + 60000L);
 		HttpRequestStatisticsEntry[] entries = stats.getEntries(startTime, endTime);
 		assertNotNull(entries);
		assertEquals(1, entries.length);
 	}
 	
 	public void testWebSocketSessionStatistics() throws Exception {
 		WebSocketSessionStatistics stats = new WebSocketSessionStatistics();
 		long curTimeMillis = System.currentTimeMillis();
 		
 		stats.addNewSession();
 		assertEquals(stats.getStartTime().getTime(), curTimeMillis - curTimeMillis % 60000L);
 		assertEquals(stats.getEndTime().getTime(), curTimeMillis - curTimeMillis % 60000L);
 		Date startTime = new Date(curTimeMillis - curTimeMillis % 60000L);
 		Date endTime = new Date(curTimeMillis - curTimeMillis % 60000L + 60000L);
 		WebSocketSessionStatisticsEntry[] entries = stats.getEntries(startTime, endTime);
 		assertNotNull(entries);
 		assertEquals(1, entries.length);
 		WebSocketSessionStatisticsEntry entry = entries[0];
 		
 		assertEquals(1, entry.getNumNewSessions());
 		assertEquals(0, entry.getNumClosedSessions());
 		assertEquals(0, entry.getNumIncomingMessages());
 		assertEquals(0, entry.getNumOutgoingMessages());
 		assertEquals(0, entry.getSumIncomingBytes());
 		assertEquals(0, entry.getSumOutgoingBytes());
 		
 		stats.addClosedSession();
 		assertEquals(1, entry.getNumNewSessions());
 		assertEquals(1, entry.getNumClosedSessions());
 		assertEquals(0, entry.getNumIncomingMessages());
 		assertEquals(0, entry.getNumOutgoingMessages());
 		assertEquals(0, entry.getSumIncomingBytes());
 		assertEquals(0, entry.getSumOutgoingBytes());
 		
 		stats.addIncomingMessage(20);
 		assertEquals(1, entry.getNumNewSessions());
 		assertEquals(1, entry.getNumClosedSessions());
 		assertEquals(1, entry.getNumIncomingMessages());
 		assertEquals(0, entry.getNumOutgoingMessages());
 		assertEquals(20, entry.getSumIncomingBytes());
 		assertEquals(0, entry.getSumOutgoingBytes());
 		
 		stats.addOutgoingMessage(40);
 		assertEquals(1, entry.getNumNewSessions());
 		assertEquals(1, entry.getNumClosedSessions());
 		assertEquals(1, entry.getNumIncomingMessages());
 		assertEquals(1, entry.getNumOutgoingMessages());
 		assertEquals(20, entry.getSumIncomingBytes());
 		assertEquals(40, entry.getSumOutgoingBytes());
 	}
 	
 	public void testAdminHttpRequestStatistics() throws Exception {
 		HttpTestConnection conn = null;
 		
 		try {
 			conn = new HttpTestConnection();
 			
 			String requestHeader = "GET /admin/httpstats HTTP/1.1\015\012" + 
 				"Authorization: Digest username=\"admin\", " +
 				"realm=\"admin\", " +
 				"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
 				"uri=\"/admin\", " +
 				"qop=auth, " +
 				"nc=00000001, " +
 				"cnonce=\"0a4f113b\", " +
 				"response=\"fae9315716e12851e61f9608eda5543f\", " +
 				"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012" +
 				"Host: localhost\015\012" +
 				"Accept: text/xml\015\012" +
 				"Connection: keep-alive\015\012\015\012";
 			
 			byte[] header = requestHeader.getBytes();
 			conn.write(header);
 			byte[] responseBytes = conn.readKeepAliveBody();
 			String xml = new String(responseBytes);
 			
 			conn.write(header);
 			responseBytes = conn.readKeepAliveBody();
 			xml = new String(responseBytes);
 			
 			assertXpathExists("http-request-statistics-info", xml);
 			assertXpathExists("http-request-statistics-info/items", xml);
 			assertXpathExists("http-request-statistics-info/items/http-request-statistics-item", xml);
 			
 			assertXpathEvaluatesTo("0", "http-request-statistics-info/items/http-request-statistics-item/num-client-error-requests", xml);
 			assertXpathEvaluatesTo("0", "http-request-statistics-info/items/http-request-statistics-item/num-server-error-requests", xml);
 			assertXpathEvaluatesTo("1", "http-request-statistics-info/items/http-request-statistics-item/num-success-requests", xml);
 		} finally {
 			if(conn != null) {
 				conn.close();
 			}
 		}		
 	}
 	
 	public void testAdminWebSocketSessionStatistics() throws Exception {
 		HttpTestConnection conn = null;
 		
 		try {
 			conn = new HttpTestConnection();
 			
 			String requestHeader = "GET /admin/wsstats HTTP/1.1\015\012" + 
 				"Authorization: Digest username=\"admin\", " +
 				"realm=\"admin\", " +
 				"nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", " +
 				"uri=\"/admin\", " +
 				"qop=auth, " +
 				"nc=00000001, " +
 				"cnonce=\"0a4f113b\", " +
 				"response=\"fae9315716e12851e61f9608eda5543f\", " +
 				"opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"\015\012" +
 				"Host: localhost\015\012" +
 				"Accept: text/xml\015\012" +
 				"Connection: keep-alive\015\012\015\012";
 			
 			byte[] header = requestHeader.getBytes();
 			conn.write(header);
 			byte[] responseBytes = conn.readKeepAliveBody();
 			String xml = new String(responseBytes);
 			
 			assertXpathExists("web-socket-session-statistics-info", xml);
 			assertXpathExists("web-socket-session-statistics-info/items", xml);
 			assertXpathExists("web-socket-session-statistics-info/items/web-socket-session-statistics-item", xml);
 			
 			assertXpathEvaluatesTo("0", "web-socket-session-statistics-info/items/web-socket-session-statistics-item/num-new-sessions", xml);
 			assertXpathEvaluatesTo("0", "web-socket-session-statistics-info/items/web-socket-session-statistics-item/num-closed-sessions", xml);
 			assertXpathEvaluatesTo("0", "web-socket-session-statistics-info/items/web-socket-session-statistics-item/num-incoming-messages", xml);
 			assertXpathEvaluatesTo("0", "web-socket-session-statistics-info/items/web-socket-session-statistics-item/sum-incoming-bytes", xml);
 			assertXpathEvaluatesTo("0", "web-socket-session-statistics-info/items/web-socket-session-statistics-item/num-outgoing-messages", xml);
 			assertXpathEvaluatesTo("0", "web-socket-session-statistics-info/items/web-socket-session-statistics-item/sum-outgoing-bytes", xml);
 		} finally {
 			if(conn != null) {
 				conn.close();
 			}
 		}		
 	}
 	
 	public static Test suite() {
 		return new TestSuite(HttpRequestStatisticsTestCase.class);
 	}
 
 	public static void main(String[] argv) {
 		junit.textui.TestRunner.run(suite());
 	}
 }
