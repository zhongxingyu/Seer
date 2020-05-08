 /**
  * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.netty.server.http;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import io.netty.channel.nio.NioEventLoopGroup;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.util.Queue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.HttpHostConnectException;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.util.EntityUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.barchart.netty.server.Servers;
 import com.barchart.netty.server.http.request.HttpServerRequest;
 import com.barchart.netty.server.http.request.RequestHandlerBase;
 
 public class TestHttpServer {
 
 	private HttpServer server;
 	private HttpClient client;
 
 	private int port;
 
 	private TestRequestHandler basic;
 	private TestRequestHandler async;
 	private TestRequestHandler asyncDelayed;
 	private TestRequestHandler clientDisconnect;
 	private TestRequestHandler error;
 	private TestRequestHandler channelError;
 
 	private TestRequestHandler serviceHandler;
 	private TestRequestHandler infoHandler;
 
 	private ChunkedRequestHandler chunkedHandler;
 
 	@Before
 	public void setUp() throws Exception {
 
 		basic = new TestRequestHandler("basic", false, 0, 0, false, false);
 		async = new TestRequestHandler("async", true, 0, 0, false, false);
 		asyncDelayed =
 				new TestRequestHandler("async-delayed", true, 50, 0, false,
 						false);
 		clientDisconnect =
 				new TestRequestHandler("", true, 500, 500, false, false);
 		error = new TestRequestHandler("error", false, 0, 0, true, false);
 		channelError =
 				new TestRequestHandler("channel-error", false, 0, 0, false,
 						true);
 
 		infoHandler = new TestRequestHandler("info", false, 0, 0, false, false);
 		serviceHandler =
 				new TestRequestHandler("service", false, 0, 0, false, false);
 
 		chunkedHandler = new ChunkedRequestHandler("chunked");
 
 		final ServerSocket s = new ServerSocket(0);
 		port = s.getLocalPort();
 		s.close();
 
 		server =
 				Servers.createHttpServer().requestHandler("/basic", basic)
 						.group(new NioEventLoopGroup(1))
 						.requestHandler("/async", async)
 						.requestHandler("/async-delayed", asyncDelayed)
 						.requestHandler("/client-disconnect", clientDisconnect)
 						.requestHandler("/channel-error", channelError)
 						.requestHandler("/error", error)
 						.requestHandler("/service/info", infoHandler)
 						.requestHandler("/service", serviceHandler)
 						.requestHandler("/chunked", chunkedHandler)
 						.maxConnections(1);
 
 		server.listen(port, "localhost");
 
 		client = new DefaultHttpClient(new PoolingClientConnectionManager());
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		if (server.running()) {
 			server.shutdown().sync();
 		}
 	}
 
 	@Test
 	public void testBasicRequest() throws Exception {
 
 		for (int i = 0; i < 100; i++) {
 			final HttpGet get =
 					new HttpGet("http://localhost:" + port + "/basic");
 			final HttpResponse response = client.execute(get);
 			final String content =
 					new BufferedReader(new InputStreamReader(response
 							.getEntity().getContent())).readLine().trim();
 
 			assertEquals("basic", content);
 		}
 	}
 
 	@Test
 	public void testChunkedRequest() throws Exception {
 
 		for (int i = 0; i < 100; i++) {
 			final HttpGet get =
 					new HttpGet("http://localhost:" + port + "/chunked");
 			final HttpResponse response = client.execute(get);
 			final String content =
 					new BufferedReader(new InputStreamReader(response
 							.getEntity().getContent())).readLine().trim();
 
 			assertEquals("chunked", content);
 		}
 
 	}
 
 	@Test
 	public void testPostRequest() throws Exception {
 
 		for (int i = 0; i < 100; i++) {
 			final HttpPost post =
 					new HttpPost("http://localhost:" + port + "/basic");
 			post.setHeader("Content-Type", "application/x-www-form-urlencoded");
 			post.setEntity(new StringEntity("id=1&id=2"));
 			final HttpResponse response = client.execute(post);
 			final String content =
 					new BufferedReader(new InputStreamReader(response
 							.getEntity().getContent())).readLine().trim();
 
 			assertEquals("basic", content);
 
 			assertEquals(1, basic.parameters.size());
 			assertEquals(2, basic.parameters.get("id").size());
 			assertEquals("1", basic.parameters.get("id").get(0));
 			assertEquals("2", basic.parameters.get("id").get(1));
 		}
 	}
 
 	@Test
 	public void testAsyncRequest() throws Exception {
 
 		final HttpGet get = new HttpGet("http://localhost:" + port + "/async");
 		final HttpResponse response = client.execute(get);
 		final String content =
 				new BufferedReader(new InputStreamReader(response.getEntity()
 						.getContent())).readLine().trim();
 
 		assertNotNull(async.lastFuture);
 		assertFalse(async.lastFuture.isCancelled());
 		assertEquals("async", content);
 
 	}
 
 	@Test
 	public void testAsyncDelayedRequest() throws Exception {
 
 		final HttpGet get =
 				new HttpGet("http://localhost:" + port + "/async-delayed");
 		final HttpResponse response = client.execute(get);
 		final String content =
 				new BufferedReader(new InputStreamReader(response.getEntity()
 						.getContent())).readLine().trim();
 
 		assertNotNull(asyncDelayed.lastFuture);
 		assertFalse(asyncDelayed.lastFuture.isCancelled());
 		assertEquals("async-delayed", content);
 
 	}
 
 	@Test
 	public void testUnknownHandler() throws Exception {
 
 		final HttpGet get =
 				new HttpGet("http://localhost:" + port + "/unknown");
 		final HttpResponse response = client.execute(get);
 		EntityUtils.consume(response.getEntity());
 		assertEquals(404, response.getStatusLine().getStatusCode());
 
 	}
 
 	@Test
 	public void testServerError() throws Exception {
 
 		final HttpGet get = new HttpGet("http://localhost:" + port + "/error");
 		final HttpResponse response = client.execute(get);
 		EntityUtils.consume(response.getEntity());
 		assertEquals(500, response.getStatusLine().getStatusCode());
 
 	}
 
 	@Test
 	public void testReuseRequest() throws Exception {
 
 		// Parameters were being remembered between requests in pooled objects
 		HttpGet get =
 				new HttpGet("http://localhost:" + port + "/basic?field=value");
 		HttpResponse response = client.execute(get);
 		assertEquals(200, response.getStatusLine().getStatusCode());
 		EntityUtils.consume(response.getEntity());
 
 		assertEquals(1, basic.parameters.get("field").size());
 		assertEquals("value", basic.parameters.get("field").get(0));
 
 		get = new HttpGet("http://localhost:" + port + "/basic?field=value2");
 		response = client.execute(get);
 		assertEquals(200, response.getStatusLine().getStatusCode());
 		EntityUtils.consume(response.getEntity());
 
 		assertEquals(1, basic.parameters.get("field").size());
 		assertEquals("value2", basic.parameters.get("field").get(0));
 
 	}
 
 	@Test
 	public void testMultipleRequests() throws Exception {
 
 		// New Beta3 was failing on second request due to shared buffer use
 		for (int i = 0; i < 100; i++) {
 			final HttpGet get =
 					new HttpGet("http://localhost:" + port + "/basic");
 			final HttpResponse response = client.execute(get);
 			assertEquals(200, response.getStatusLine().getStatusCode());
 			EntityUtils.consume(response.getEntity());
 		}
 
 	}
 
 	@Test
 	public void testPatternRequests() throws Exception {
 
 		{
 			final HttpGet get =
 					new HttpGet("http://localhost:" + port + "/service/info/10");
 			final HttpResponse response = client.execute(get);
 			final String content =
 					new BufferedReader(new InputStreamReader(response
 							.getEntity().getContent())).readLine().trim();
 
 			assertEquals("info", content);
 		}
 
 		{
 			final HttpGet get =
 					new HttpGet("http://localhost:" + port
 							+ "/service/something/else");
 			final HttpResponse response = client.execute(get);
 			final String content =
 					new BufferedReader(new InputStreamReader(response
 							.getEntity().getContent())).readLine().trim();
 
 			assertEquals("service", content);
 		}
 	}
 
 	@Test
 	public void testTooManyConnections() throws Exception {
 
 		final Queue<Integer> status = new LinkedBlockingQueue<Integer>();
 
 		final Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				try {
 					final HttpResponse response =
 							client.execute(new HttpGet("http://localhost:"
 									+ port + "/client-disconnect"));
 					status.add(response.getStatusLine().getStatusCode());
 					EntityUtils.consume(response.getEntity());
 				} catch (final Exception e) {
 					e.printStackTrace();
 				}
 			}
 		};
 
 		final Thread t1 = new Thread(r);
 		t1.start();
 
 		final Thread t2 = new Thread(r);
 		t2.start();
 
 		t1.join();
 		t2.join();
 
 		assertEquals(2, status.size());
 		assertTrue(status.contains(200));
 		assertTrue(status.contains(503));
 
 	}
 
 	@Test
 	public void testShutdown() throws Exception {
 
 		final ScheduledExecutorService executor =
 				Executors.newScheduledThreadPool(1);
 
 		final AtomicBoolean pass = new AtomicBoolean(false);
 
 		executor.schedule(new Runnable() {
 
 			@Override
 			public void run() {
 
 				try {
 					server.shutdown().sync();
 				} catch (final InterruptedException e1) {
 					e1.printStackTrace();
 				}
 
 				try {
 					client.execute(new HttpGet("http://localhost:" + port
 							+ "/basic"));
 				} catch (final HttpHostConnectException hhce) {
 					pass.set(true);
 				} catch (final Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		}, 1000, TimeUnit.MILLISECONDS);
 
 		final HttpGet get =
 				new HttpGet("http://localhost:" + port + "/client-disconnect");
 		final HttpResponse response = client.execute(get);
 		EntityUtils.consume(response.getEntity());
 		assertEquals(200, response.getStatusLine().getStatusCode());
 		// assertTrue(pass.get());
 
 	}
 
 	@Test(expected = HttpHostConnectException.class)
 	public void testKill() throws Exception {
 
 		final ScheduledExecutorService executor =
 				Executors.newScheduledThreadPool(1);
 
 		executor.schedule(new Runnable() {
 
 			@Override
 			public void run() {
 				server.kill();
 			}
 
 		}, 500, TimeUnit.MILLISECONDS);
 
 		final HttpGet get =
 				new HttpGet("http://localhost:" + port + "/client-disconnect");
 
 		// Should throw exception
 		client.execute(get).getEntity();
 
 	}
 
 	// @Test
 	// Exposed old server handler issue, "Response has already been started"
 	public void testRepeated() throws Exception {
 		for (int i = 0; i < 10000; i++) {
 			testAsyncRequest();
 		}
 	}
 
 	private static class ChunkedRequestHandler extends RequestHandlerBase {
 
 		String data;
 
 		public ChunkedRequestHandler(final String data_) {
 			data = data_;
 		}
 
 		@Override
 		public void handle(final HttpServerRequest request) throws IOException {
 			request.response().setChunkSize(1);
 			request.response().write(data);
 			request.response().finish();
 		}
 
 	}
 
 }
