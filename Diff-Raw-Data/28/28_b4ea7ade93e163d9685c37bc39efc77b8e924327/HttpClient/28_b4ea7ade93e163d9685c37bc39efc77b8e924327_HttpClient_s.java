 /**
  * 
  */
 package net.anyflow.network.http;
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Executors;
 
 import net.anyflow.network.exception.DefaultException;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.http.CookieEncoder;
 import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
 import org.jboss.netty.handler.codec.http.HttpChunk;
 import org.jboss.netty.handler.codec.http.HttpClientCodec;
 import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 import org.jboss.netty.util.CharsetUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author anyflow
  *
  */
 public class HttpClient {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
 
 	private final URI uri;
 	private HttpMethod httpMethod;
 	private Map<String, String> cookies;
 	private Map<String, String> parameters;
 	
 	public HttpClient(URI uri) {
 		this.uri = uri;
 		
 		httpMethod = HttpMethod.GET;
 		cookies = new HashMap<String, String>();
 		parameters = new HashMap<String, String>();
 	}
 	
 	/**
 	 * Request with encoding utf-8
 	 * @param isSynchronousMode
 	 * @param receiver
 	 * @return if isSynchronousMode is true and the request processed successfully, returns HttpResponse instance, otherwise null; 
 	 * @throws DefaultException 
 	 * @throws UnsupportedEncodingException 
 	 */
 	public HttpResponse request(boolean isSynchronousMode, final MessageReceiver receiver) throws DefaultException, UnsupportedEncodingException {
 		return request(isSynchronousMode, receiver, "utf-8");
 	}
 	/**
 	 * request.
 	 * @param isSynchronousMode
 	 * @param receiver
 	 * @param queryEncodingCharset query encoding charset. if it is null, no encoding will be applied.
 	 * @return if isSynchronousMode is true and the request processed successfully, returns HttpResponse instance, otherwise null; 
 	 * @throws DefaultException 
 	 * @throws UnsupportedEncodingException 
 	 */
 	public HttpResponse request(boolean isSynchronousMode, final MessageReceiver receiver, String queryEncodingCharset) throws DefaultException, UnsupportedEncodingException {
 		
 		String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
 
 		//TODO support HTTPS
 		if(!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
 			logger.error("Only HTTP(S) is supported.");
 			return null;
 		}
 		
 		final ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool()
 																						, Executors.newCachedThreadPool()));
 		
 //		final boolean isSsl = scheme.equalsIgnoreCase("https");
 
 		final HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri.getRawPath());
 		final HttpClientHandler clientHandler = new HttpClientHandler(receiver, request);
 		
 		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 
 			@Override
 			public ChannelPipeline getPipeline() throws Exception {
 				ChannelPipeline pipeline = org.jboss.netty.channel.Channels.pipeline();
 				
 				//TODO implement SSL relevant Stuffs..
 //				if(isSsl) {
 //					SSLEngine engine = SecureChatSslContextFactory.getClientContext().createSSLEngine();
 //					engine.setUseClientMode(true);
 //					
 //					pipeline.addLast("ssl", new SslHandler(engine));
 //				}
 				
 				pipeline.addLast("codec", new HttpClientCodec());
 				pipeline.addLast("inflateer", new HttpContentDecompressor());
 				pipeline.addLast("handler", clientHandler);
 				
 				return pipeline;
 			}
 		});
 
 		setHeaders(request);
 		addParameters(request, queryEncodingCharset);
 	
 		logger.info("[request] URI : {}", request.getUri());
 		logger.info("[request] CONTENT : {}", request.getContent().toString(CharsetUtil.UTF_8));
 		logger.info("[request] HTTPMETHOD : {}", request.getMethod().toString());
 		if(!request.getHeaderNames().isEmpty()) {
 			for(String name : request.getHeaderNames()) {
 				for(String value : request.getHeaders(name)) {
 					logger.info("[request] HEADER : " + name + " = " + value);
 				}
 			}
 		}
 		
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(uri.getHost(), getPort()));
 		
		Channel channel = future.awaitUninterruptibly().getChannel();
		if(!future.isSuccess()) {
			logger.error("connection failed.", future.getCause());
 			bootstrap.releaseExternalResources();
 			return null;
 		}
 		
		channel = future.awaitUninterruptibly().getChannel();
 		channel.getCloseFuture().addListener(new ChannelFutureListener() {
 
 			@Override
 			public void operationComplete(ChannelFuture future)
 					throws Exception {
 				new Thread(new Runnable() {
 
 					@Override
 					public void run() {
 						bootstrap.releaseExternalResources();
 					}
 				}).start();
 			}
 		});
		
 		channel.write(request);
 		
 		if(isSynchronousMode == false) {
 			return null;
 		}
 		
 		try {
			channel.getCloseFuture().await();
 			return clientHandler.getResponse();
 		} 
 		catch (InterruptedException e) {
 			logger.error("waiting interrupted.", e);
 			return null;
 		}
 	}
 
 	private int getPort() {
 		
 		String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
 
 		int port = uri.getPort();
 		if(port == -1) {
 			if(scheme.equalsIgnoreCase("http")) {
 				return 80;
 			} 
 			else if(scheme.equalsIgnoreCase("https")) {
 				return 443;
 			}
 		}
 		
 		return port;
 	}
 	
 	private void setHeaders(HttpRequest request) {
 		
 		String host = uri.getHost() == null ? "localhost" : uri.getHost();
 
 		request.setHeader(HttpHeaders.Names.HOST, host);
 		request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
 		request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
 		request.setHeader(HttpHeaders.Names.ACCEPT_CHARSET, "utf-8");
 		
 		CookieEncoder httpCookieEncoder = new CookieEncoder(false);
 		
 		for(Entry<String, String> item : cookies.entrySet()) {
 			httpCookieEncoder.addCookie(item.getKey(), item.getValue());
 		}
 		request.setHeader(HttpHeaders.Names.COOKIE, httpCookieEncoder.encode());
 	}
 	
 	/**
 	 * @param request
 	 * @throws DefaultException
 	 * @throws UnsupportedEncodingException 
 	 */
 	private void addParameters(HttpRequest request, String queryEncodingCharset) throws DefaultException, UnsupportedEncodingException {
 		
 		String address = uri.getScheme() 
 					   + "://" 
 				       + uri.getAuthority()
 				       + ":"
 				       + getPort()
 				       + uri.getPath();
 		
 		if(httpMethod == HttpMethod.GET) {
 			String query = uri.getQuery();
 			
 			if(query != null && query.length() > 0) {
 				
 				String[] tokens = query.split("=|&");
 				for(int i=0; i<tokens.length; ++i) {
 					if(i % 2 == 0) { continue; }
 					
 					parameters.put(tokens[i-1], tokens[i]);
 				}
 			}
 			
 			if(parameters.size() > 0) {
 				address += "?" + getParametersString(queryEncodingCharset);
 			}
 		}
 		else if(httpMethod == HttpMethod.POST) {
 			request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-www-form-urlencoded");
 			
 			if(parameters.size() > 0) {
 				String paramsString = getParametersString(queryEncodingCharset);
 				
 				Charset charset = queryEncodingCharset == null
 						        ? Charset.defaultCharset()
 						        : Charset.forName(queryEncodingCharset);
 						        
 				ChannelBuffer cb = ChannelBuffers.copiedBuffer(paramsString, charset);
 				
 				request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, cb.readableBytes());
 				request.setContent(cb);
 			}
 		}
 		else {
 			throw new DefaultException("only GET/POST methods are supported.");
 		}
 		
 		request.setUri(address);
 	}
 	
 	/**
 	 * @param httpMethod
 	 */
 	public void setHttpMethod(HttpMethod httpMethod) {
 		this.httpMethod = httpMethod;
 	}
 	
 	/**
 	 * @param key
 	 * @param value
 	 */
 	public void setCookie(String key, String value) {
 		cookies.put(key, value);
 	}
 	
 	/**
 	 * @return
 	 */
 	public Map<String, String> getCookies() {
 		return cookies;
 	}
 
 	/**
 	 * @param key
 	 * @param value
 	 */
 	public void addParameter(String key, String value) {
 		parameters.put(key, value);
 	}
 	
 	/**
 	 * @return
 	 */
 	public Map<String, String> getParameters() {
 		return parameters;
 	}
 	
 	private String getParametersString(String queryEncodingCharset) throws UnsupportedEncodingException {
 		if(parameters.size() <= 0) {
 			return "";
 		}
 		
 		StringBuilder query = new StringBuilder();
 		String value = null;
 		
 		for(Map.Entry<String, String> item : parameters.entrySet()) {
 		
 			value = queryEncodingCharset != null
 			      ? java.net.URLEncoder.encode(item.getValue(), queryEncodingCharset)
 			      : item.getValue();
 			      
 			query = query.append(item.getKey()).append("=").append(value).append("&");  
 		}
 
 		query = query.deleteCharAt(query.length() - 1);
 		return query.toString();
 	}
 	
 	/**
 	 * @author anyflow
 	 *
 	 */
 	public class HttpClientHandler extends SimpleChannelUpstreamHandler {
 		
 		private boolean readingChunks;
 		private MessageReceiver receiver;
 		private HttpRequest request;
 		private HttpResponse response;
 		
 		public HttpClientHandler(MessageReceiver receiver, HttpRequest request) {
 			this.receiver = receiver;
 			this.request = request;
 		}
 		
 		public HttpResponse getResponse() {
 			return response;
 		}
 		
 		//TODO chunk mode handling.. especially, receiver.messageReceived.
 		@Override
 		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
 			if(!readingChunks) {
 				response = (HttpResponse) e.getMessage();
 				
 				logger.debug("[response] STATUS : " + response.getStatus());
 				logger.debug("[response] VERSION : " + response.getProtocolVersion());
 				
 				if(!response.getHeaderNames().isEmpty()) {
 					for(String name : response.getHeaderNames()) {
 						for(String value : response.getHeaders(name)) {
 							logger.debug("[response] HEADER : " + name + " = " + value);
 						}
 					}
 				}
 				
 				if(response.isChunked()) {
 					readingChunks = true;
 					logger.debug("[response] CHUNKED CONTENT {");
 				}
 				else {
 					ChannelBuffer content = response.getContent();
 					if(content.readable()) {
 						logger.debug("[response] CONTENT {");
 						logger.debug(content.toString(CharsetUtil.UTF_8));
 						logger.debug("[response] } END OF CONTENT");
 					}
 					
					if(receiver == null) { return; }
 					
 					receiver.messageReceived(request, response);
 				}
 					
 			}
 			else {
 				HttpChunk chunk = (HttpChunk)e.getMessage();
 				if(chunk.isLast()) {
 					readingChunks = false;
 					logger.debug("[response] } END OF CHUNKED CONTENT");
 				}
 				else {
 					logger.debug(chunk.getContent().toString(CharsetUtil.UTF_8));
 				}
 			}
 		}
 	}
 }
