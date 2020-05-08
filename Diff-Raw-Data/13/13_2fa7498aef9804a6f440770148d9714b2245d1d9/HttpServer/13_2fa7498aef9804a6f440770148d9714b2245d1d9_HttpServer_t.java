 /**
  * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.netty.server.http;
 
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.buffer.ByteBuf;
 import io.netty.buffer.Unpooled;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandler.Sharable;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandler;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 import io.netty.channel.ChannelOption;
 import io.netty.channel.ChannelPipeline;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import io.netty.handler.codec.http.DefaultFullHttpResponse;
 import io.netty.handler.codec.http.FullHttpResponse;
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpObjectAggregator;
 import io.netty.handler.codec.http.HttpRequestDecoder;
 import io.netty.handler.codec.http.HttpResponseEncoder;
 import io.netty.handler.codec.http.HttpResponseStatus;
 import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LoggingHandler;
 import io.netty.handler.stream.ChunkedWriteHandler;
 
 import java.net.InetSocketAddress;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import com.barchart.netty.server.HandlerFactory;
 import com.barchart.netty.server.base.AbstractStatefulServer;
 import com.barchart.netty.server.http.error.DefaultErrorHandler;
 import com.barchart.netty.server.http.error.ErrorHandler;
 import com.barchart.netty.server.http.logging.NullRequestLogger;
 import com.barchart.netty.server.http.logging.RequestLogger;
 import com.barchart.netty.server.http.pipeline.HttpRequestChannelHandler;
 import com.barchart.netty.server.http.request.HandlerMapping;
 import com.barchart.netty.server.http.request.RequestHandler;
 import com.barchart.netty.server.util.SingleHandlerFactory;
 
 /**
  * Asynchronous HTTP server.
  */
 public class HttpServer extends AbstractStatefulServer<HttpServer> {
 
 	private final Map<String, HandlerFactory<RequestHandler>> handlers =
 			new ConcurrentSkipListMap<String, HandlerFactory<RequestHandler>>(
 					new ReverseLengthComparator());
 
 	private final Map<String, HandlerFactory<ChannelInboundHandler>> webSocketHandlers =
 			new ConcurrentHashMap<String, HandlerFactory<ChannelInboundHandler>>();
 
 	protected int maxConnections = -1;
 	protected int maxRequestSize = 1024 * 1024;
 	protected boolean chunked = true;
 	protected long timeout = 0;
 
 	protected ConnectionTracker clientTracker = null;
 	protected ErrorHandler errorHandler = new DefaultErrorHandler();
 	protected RequestLogger requestLogger = new NullRequestLogger();
 
 	private final HttpRequestChannelHandler channelHandler;
 
 	public HttpServer() {
 		channelHandler = new HttpRequestChannelHandler(this);
 	}
 
 	@Override
 	protected ServerBootstrap bootstrap() {
 
 		return new ServerBootstrap() //
 				.group(defaultGroup) //
 				.channel(NioServerSocketChannel.class) //
 				.childHandler(new ServerChannelInitializer()) //
 				.option(ChannelOption.SO_REUSEADDR, true) //
 				.option(ChannelOption.SO_SNDBUF, 262144) //
 				.option(ChannelOption.SO_RCVBUF, 262144);
 
 	}
 
 	@Override
 	public ChannelFuture listen(final int port) {
 		return listen(new InetSocketAddress("0.0.0.0", port));
 	}
 
 	@Override
 	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
 
 		pipeline.addLast( //
				new LoggingHandler(), //
 				new HttpResponseEncoder(), //
				new ChunkedWriteHandler());

		if (clientTracker != null) {
			pipeline.addLast(clientTracker);
		}

		pipeline.addLast( //
 				new HttpRequestDecoder(), //
 				new HttpObjectAggregator(maxRequestSize), //
 				channelHandler);
 
 	}
 
 	/**
 	 * Set the maximum number of client connections.
 	 */
 	public HttpServer maxConnections(final int max) {
 		maxConnections = max;
 		clientTracker = new ConnectionTracker(maxConnections);
 		return this;
 	}
 
 	/**
 	 * Set the maximum request size in bytes (file uploads, etc). Defaults to
 	 * 1048576 (1MB).
 	 */
 	public HttpServer maxRequestSize(final int max) {
 		maxRequestSize = max;
 		return this;
 	}
 
 	/**
 	 * Set the default error handler.
 	 */
 	public HttpServer errorHandler(final ErrorHandler handler) {
 		errorHandler = handler;
 		return this;
 	}
 
 	/**
 	 * Set the request logger.
 	 */
 	public HttpServer logger(final RequestLogger logger_) {
 		requestLogger = logger_;
 		return this;
 	}
 
 	/**
 	 * Set whether the server should send large objects using chunked encoding.
 	 * Defaults to true.
 	 */
 	public HttpServer chunked(final boolean chunked_) {
 		chunked = chunked_;
 		return this;
 	}
 
 	/**
 	 * Set the maximum request time. Defaults to 0 (unlimited).
 	 */
 	public HttpServer timeout(final long timeout_) {
 		timeout = timeout_;
 		return this;
 	}
 
 	/**
 	 * Add a request handler for the given prefix. Prefix matching is simplistic
 	 * and the longest prefix that matches always wins regardless of number of
 	 * path segments, so you should avoid overlapping handler prefixes when
 	 * possible. For more complex handler routing using URL pattern matching see
 	 * com.barchart.rest.Router.
 	 * 
 	 * Assuming two request handlers defined as:
 	 * 
 	 * <pre>
 	 * requestHandler("/service", serviceHandler);
 	 * requestHandler("/service/info", infoHandler);
 	 * </pre>
 	 * 
 	 * A request to "/service/info/10" will go to infoHandler, but a request to
 	 * "/service/something/else" will go to serviceHandler.
 	 * 
 	 * Inside the handler, ServerRequest.getPathInfo() will return the URL path
 	 * portion that comes *after* the matched handler prefix. So, in the case of
 	 * the examples above, getPathInfo() would return:
 	 * 
 	 * <pre>
 	 * /service/info/10: "/10"
 	 * /service/something/else: "/something/else"
 	 * </pre>
 	 */
 	public HttpServer requestHandler(final String prefix,
 			final RequestHandler handler) {
 		handlers.put(prefix, new SingleHandlerFactory<RequestHandler>(handler));
 		return this;
 	}
 
 	/**
 	 * Add a request handler factory for the given prefix. This is similar to
 	 * {@link HttpServer#requestHandler(String, RequestHandler)}, but allows
 	 * greater control over the handler lifecycle for use cases like
 	 * handler-per-connection or object pooling.
 	 * 
 	 * @see HttpServer#requestHandler(String, RequestHandler)
 	 */
 	public HttpServer requestHandler(final String prefix,
 			final HandlerFactory<RequestHandler> factory) {
 		handlers.put(prefix, factory);
 		return this;
 	}
 
 	/**
 	 * Add a websocket handler for the given prefix.
 	 */
 	public HttpServer webSocketHandler(final String prefix,
 			final ChannelInboundHandler handler) {
 		webSocketHandlers.put(prefix,
 				new SingleHandlerFactory<ChannelInboundHandler>(handler));
 		return this;
 	}
 
 	/**
 	 * Add a websocket handler factory for the given prefix.
 	 */
 	public HttpServer webSocketHandler(final String prefix,
 			final HandlerFactory<ChannelInboundHandler> factory) {
 		webSocketHandlers.put(prefix, factory);
 		return this;
 	}
 
 	/**
 	 * Get the maximum number of client connections.
 	 */
 	public int maxConnections() {
 		return maxConnections;
 	}
 
 	/**
 	 * Get the maximum request size in bytes.
 	 */
 	public int maxRequestSize() {
 		return maxRequestSize;
 	}
 
 	/**
 	 * Get the default error handler.
 	 */
 	public ErrorHandler errorHandler() {
 		return errorHandler;
 	}
 
 	/**
 	 * Get the request logger.
 	 */
 	public RequestLogger logger() {
 		return requestLogger;
 	}
 
 	/**
 	 * Get the Netty event loop group.
 	 */
 	public EventLoopGroup group() {
 		return defaultGroup;
 	}
 
 	/**
 	 * Get the request handler mapping for the specified URI.
 	 */
 	public HandlerMapping<RequestHandler> getRequestMapping(final String uri) {
 
 		for (final Map.Entry<String, HandlerFactory<RequestHandler>> entry : handlers
 				.entrySet()) {
 			if (uri.startsWith(entry.getKey())) {
 				return new HandlerMapping<RequestHandler>(entry.getKey(),
 						entry.getValue());
 			}
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Get the websocket handler mapping for the specified URI.
 	 */
 	public HandlerFactory<ChannelInboundHandler> webSocketFactory(
 			final String uri) {
 
 		if (webSocketHandlers.containsKey(uri)) {
 			return webSocketHandlers.get(uri);
 		}
 
 		return null;
 
 	}
 
 	public Object removeRequestHandler(final String path) {
 		return handlers.remove(path);
 	}
 
 	public Object removeWebSocketHandler(final String path) {
 		return webSocketHandlers.remove(path);
 	}
 
 	@Sharable
 	private class ConnectionTracker extends ChannelInboundHandlerAdapter {
 
 		private int maxConnections = -1;
 
 		public ConnectionTracker(final int connections) {
 			maxConnections = connections;
 		}
 
 		@Override
 		public void channelActive(final ChannelHandlerContext context) {
 
 			if (maxConnections > -1 && connections() >= maxConnections) {
 
 				final ByteBuf content = Unpooled.buffer();
 
 				content.writeBytes("503 Service Unavailable - Server Too Busy"
 						.getBytes());
 
 				final FullHttpResponse response =
 						new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
 								HttpResponseStatus.SERVICE_UNAVAILABLE);
 
 				response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
 						content.readableBytes());
 
 				response.content().writeBytes(content);
 
 				context.writeAndFlush(response).addListener(
 						ChannelFutureListener.CLOSE);
 
 				return;
 
 			} else {
 
 				context.fireChannelActive();
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * 
 	 * Sorts strings by reverse length first, then normal comparison. For
 	 * example:
 	 * 
 	 * 2 handlers defined as requestHandler("/service", serviceHandler);
 	 * requestHandler("/service/info", infoHandler);
 	 * 
 	 * A request to "/service/info/10" will go to infoHandler, but a request to
 	 * "/service/something/else" will go to serviceHandler.
 	 * 
 	 */
 	private class ReverseLengthComparator implements Comparator<String> {
 
 		// Sort by reverse length first to allow overriding parent
 		// mappings
 		@Override
 		public int compare(final String o1, final String o2) {
 
 			final int l1 = o1.length();
 			final int l2 = o2.length();
 
 			if (l1 < l2) {
 				return 1;
 			} else if (l2 < l1) {
 				return -1;
 			} else {
 				return o1.compareTo(o2);
 			}
 
 		}
 
 	}
 
 }
