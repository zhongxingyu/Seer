 /**
  * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.http.server;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import io.netty.channel.nio.NioEventLoopGroup;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.util.Queue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.HttpHostConnectException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.util.EntityUtils;
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 import com.barchart.http.request.RequestHandlerBase;
 import com.barchart.http.request.ServerRequest;
 import com.barchart.http.request.ServerResponse;
 
@Ignore
 public class TestHttpServer {
 
 	HttpServer server;
 	HttpClient client;
 
 	TestRequestHandler basic;
 	TestRequestHandler async;
 	TestRequestHandler asyncDelayed;
 	TestRequestHandler clientDisconnect;
 	TestRequestHandler error;
 	TestRequestHandler channelError;
 
 	@Before
 	public void setUp() throws Exception {
 
 		server = new HttpServer();
 
 		basic = new TestRequestHandler("basic", false, 0, 0, false, false);
 		async = new TestRequestHandler("async", true, 0, 0, false, false);
 		asyncDelayed =
 				new TestRequestHandler("async-delayed", true, 50, 0, false,
 						false);
 		clientDisconnect =
 				new TestRequestHandler("client-disconnect", true, 500, 500,
 						false, false);
 		error = new TestRequestHandler("error", false, 0, 0, true, false);
 		channelError =
 				new TestRequestHandler("channel-error", false, 0, 0, false,
 						true);
 
 		final HttpServerConfig config =
 				new HttpServerConfig().requestHandler("/basic", basic)
 						.address(new InetSocketAddress("localhost", 8888))
 						.parentGroup(new NioEventLoopGroup(1))
 						.childGroup(new NioEventLoopGroup(1))
 						.requestHandler("/async", async)
 						.requestHandler("/async-delayed", asyncDelayed)
 						.requestHandler("/client-disconnect", clientDisconnect)
 						.requestHandler("/channel-error", channelError)
 						.requestHandler("/error", error).maxConnections(1);
 
 		server.configure(config).listen().sync();
 
 		client = new DefaultHttpClient(new PoolingClientConnectionManager());
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		if (server.isRunning()) {
 			server.shutdown().sync();
 		}
 	}
 
 	@Test
 	public void testBasicRequest() throws Exception {
 
 		final HttpGet get = new HttpGet("http://localhost:8888/basic");
 		final HttpResponse response = client.execute(get);
 		final String content =
 				new BufferedReader(new InputStreamReader(response.getEntity()
 						.getContent())).readLine().trim();
 
 		assertEquals("basic", content);
 
 	}
 
 	@Test
 	public void testAsyncRequest() throws Exception {
 
 		final HttpGet get = new HttpGet("http://localhost:8888/async");
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
 
 		final HttpGet get = new HttpGet("http://localhost:8888/async-delayed");
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
 
 		final HttpGet get = new HttpGet("http://localhost:8888/unknown");
 		final HttpResponse response = client.execute(get);
 		assertEquals(404, response.getStatusLine().getStatusCode());
 
 	}
 
 	@Test
 	public void testServerError() throws Exception {
 
 		final HttpGet get = new HttpGet("http://localhost:8888/error");
 		final HttpResponse response = client.execute(get);
 		assertEquals(500, response.getStatusLine().getStatusCode());
 
 	}
 
 	@Test
 	public void testMultipleRequests() throws Exception {
 
 		// New Beta3 was failing on second request due to shared buffer use
 		for (int i = 0; i < 100; i++) {
 			final HttpGet get = new HttpGet("http://localhost:8888/basic");
 			final HttpResponse response = client.execute(get);
 			assertEquals(200, response.getStatusLine().getStatusCode());
 			EntityUtils.consume(response.getEntity());
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
 							client.execute(new HttpGet(
 									"http://localhost:8888/client-disconnect"));
 					status.add(response.getStatusLine().getStatusCode());
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
 					client.execute(new HttpGet("http://localhost:8888/basic"));
 				} catch (final HttpHostConnectException hhce) {
 					pass.set(true);
 				} catch (final Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		}, 500, TimeUnit.MILLISECONDS);
 
 		final HttpGet get =
 				new HttpGet("http://localhost:8888/client-disconnect");
 		final HttpResponse response = client.execute(get);
 		assertEquals(200, response.getStatusLine().getStatusCode());
 		assertTrue(pass.get());
 
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
 				new HttpGet("http://localhost:8888/client-disconnect");
 
 		// Should throw exception
 		client.execute(get);
 
 	}
 
 	// @Test
 	// Exposed old server handler issue, "Response has already been started"
 	public void testRepeated() throws Exception {
 		for (int i = 0; i < 10000; i++) {
 			testAsyncRequest();
 		}
 	}
 
 	private class TestRequestHandler extends RequestHandlerBase {
 
 		private final ScheduledExecutorService executor = Executors
 				.newScheduledThreadPool(1);
 
 		protected AtomicInteger requests = new AtomicInteger(0);
 
 		protected ScheduledFuture<?> lastFuture;
 
 		protected String content = null;
 		protected boolean async = false;
 		protected long execTime = 0;
 		protected long writeTime = 0;
 		protected boolean error = false;
 		protected boolean disconnect = false;
 
 		TestRequestHandler(final String content_, final boolean async_,
 				final long execTime_, final long writeTime_,
 				final boolean error_, final boolean disconnect_) {
 
 			content = content_;
 			async = async_;
 			execTime = execTime_;
 			writeTime = writeTime_;
 			error = error_;
 			disconnect = disconnect_;
 
 		}
 
 		@Override
 		public void onRequest(final ServerRequest request,
 				final ServerResponse response) throws IOException {
 
 			requests.incrementAndGet();
 
 			final Runnable task = response(response);
 
 			if (async) {
 				response.suspend();
 				lastFuture =
 						executor.schedule(task, execTime, TimeUnit.MILLISECONDS);
 			} else {
 				task.run();
 			}
 
 		}
 
 		public Runnable response(final ServerResponse response) {
 
 			return new Runnable() {
 
 				@Override
 				public void run() {
 
 					if (error) {
 						throw new RuntimeException("Uncaught exception");
 					}
 
 					if (disconnect) {
 						try {
 							response.finish().sync();
 						} catch (final Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					try {
 						response.write(content.getBytes());
 						if (writeTime > 0) {
 							try {
 								Thread.sleep(writeTime);
 							} catch (final InterruptedException e) {
 							}
 						}
 						response.finish();
 					} catch (final IOException e) {
 						e.printStackTrace();
 					}
 
 				}
 
 			};
 
 		}
 
 	}
 
 }
