 /**
  * 
  */
 package net.anyflow.menton.http;
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.handler.codec.http.DefaultFullHttpRequest;
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpMethod;
 import io.netty.handler.codec.http.HttpVersion;
 import io.netty.util.CharsetUtil;
 import io.netty.util.concurrent.DefaultThreadFactory;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author anyflow
  */
 public class HttpClient {
 
 	static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
 
 	private final HttpRequest httpRequest;
 
 	public HttpClient(String uri) throws URISyntaxException, UnsupportedOperationException {
 
 		httpRequest = new HttpRequest(null, new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri));
 
 		if(httpRequest().uri().getScheme().equalsIgnoreCase("http") == false) {
 			String message = "HTTP is supported only.";
 			logger.error(message);
 			throw new UnsupportedOperationException(message);
 		}
 	}
 
 	public HttpRequest httpRequest() {
 		return httpRequest;
 	}
 
 	public HttpResponse get() {
 		return get(null);
 	}
 
 	public HttpResponse get(final MessageReceiver receiver) {
 		httpRequest().setMethod(HttpMethod.GET);
 
 		return request(receiver);
 	}
 
 	public HttpResponse post() {
 		return post(null);
 	}
 
 	public HttpResponse post(final MessageReceiver receiver) {
 
 		httpRequest().setMethod(HttpMethod.POST);
 		if(httpRequest().headers().contains(HttpHeaders.Names.CONTENT_TYPE) == false) {
 			httpRequest().headers().set(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);
 		}
 
 		return request(receiver);
 	}
 
 	public HttpResponse put() {
 		return put(null);
 	}
 
 	public HttpResponse put(final MessageReceiver receiver) {
 		httpRequest().setMethod(HttpMethod.PUT);
 
 		if(httpRequest().headers().contains(HttpHeaders.Names.CONTENT_TYPE) == false) {
 			httpRequest().headers().set(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);
 		}
 
 		return request(receiver);
 	}
 
 	public HttpResponse delete() {
 		return delete(null);
 	}
 
 	public HttpResponse delete(final MessageReceiver receiver) {
 		httpRequest().setMethod(HttpMethod.DELETE);
 
 		return request(receiver);
 	}
 
 	/**
 	 * request.
 	 * 
 	 * @param receiver
 	 * @return if receiver is not null the request processed successfully, returns HttpResponse instance, otherwise null;
 	 * @throws UnsupportedEncodingException
 	 */
 	private HttpResponse request(final MessageReceiver receiver) throws IllegalArgumentException {
 
 		boolean ssl = false;
 
 		final EventLoopGroup group = new NioEventLoopGroup(1, new DefaultThreadFactory("client"));
 
 		httpRequest().normalize();
 		setHeaders();
 
 		if(logger.isDebugEnabled()) {
 			logger.debug("[request] URI : {}", httpRequest().getUri());
 			logger.debug("[request] CONTENT : {}", httpRequest().content().toString(CharsetUtil.UTF_8));
 			logger.debug("[request] HTTPMETHOD : {}", httpRequest().getMethod().toString());
 
 			for(String name : httpRequest.headers().names()) {
 				logger.debug("[request] HEADER : " + name + " = " + httpRequest.headers().get(name));
 			}
 		}
 
 		HttpClientHandler clientHandler = new HttpClientHandler(receiver, httpRequest);
 
 		Bootstrap bootstrap = new Bootstrap();
 		bootstrap.group(group).channel(NioSocketChannel.class).handler(new ClientChannelInitializer(clientHandler, ssl));
 
 		try {
			Channel channel = bootstrap.connect(httpRequest().host(), httpRequest().uri().getPort()).sync().channel();
 			channel.writeAndFlush(httpRequest);
 
 			if(receiver == null) {
 				channel.closeFuture().sync();
 				group.shutdownGracefully();
 
 				return clientHandler.httpResponse();
 			}
 			else {
 				channel.closeFuture().addListener(new ChannelFutureListener() {
 
 					@Override
 					public void operationComplete(ChannelFuture future) throws Exception {
 						group.shutdownGracefully();
 					}
 				});
 
 				return null;
 			}
 		}
 		catch(InterruptedException e) {
 
 			group.shutdownGracefully();
 			logger.error(e.getMessage(), e);
 
 			return null;
 		}
 	}
 
 	private void setHeaders() {
 
 		if(httpRequest().headers().contains(HttpHeaders.Names.HOST) == false) {
			httpRequest().headers().set(HttpHeaders.Names.HOST, httpRequest().host() == null ? "localhost" : httpRequest().host());
 		}
 		if(httpRequest().headers().contains(HttpHeaders.Names.CONNECTION) == false) {
 			httpRequest().headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
 		}
 		if(httpRequest().headers().contains(HttpHeaders.Names.ACCEPT_ENCODING) == false) {
 			httpRequest().headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP + ", " + HttpHeaders.Values.DEFLATE);
 		}
 		if(httpRequest().headers().contains(HttpHeaders.Names.ACCEPT_CHARSET) == false) {
 			httpRequest().headers().set(HttpHeaders.Names.ACCEPT_CHARSET, "utf-8");
 		}
 	}
 }
