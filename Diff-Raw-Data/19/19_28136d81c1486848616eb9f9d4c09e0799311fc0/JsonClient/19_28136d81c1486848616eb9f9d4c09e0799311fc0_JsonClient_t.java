 package com.voltvoodoo.saplo4j.http;
 
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.http.CookieEncoder;
 import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
 import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
 import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
 import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import com.voltvoodoo.saplo4j.async.SaploCallback;
 import com.voltvoodoo.saplo4j.exception.SaploConnectionException;
 
 /**
  * A Json RPC client for Saplo. Based on the netty example HttpClient by Andy
  * Taylor and Trustin Lee.
  * 
  * There is plenty of room for optimization here, for instance keeping the
  * connection open between requests.
  * 
  * @author Jacob Hansson <jacob@voltvoodoo.com>
  * 
  */
 public class JsonClient {
 
 	protected CookieEncoder cookies = new CookieEncoder(false);
 	protected ChannelFactory channelFactory;
 
 	protected String scheme;
 	protected String host;
 
 	protected int port;
 
 	//
 	// CONSTRUCTORS
 	//
 
 	public JsonClient(String url) throws SaploConnectionException,
 			URISyntaxException {
 		this(new URI(url));
 	}
 
 	public JsonClient(URI uri) throws SaploConnectionException {
 		// BOOT
 		channelFactory = new NioClientSocketChannelFactory(
 				Executors.newCachedThreadPool(),
 				Executors.newCachedThreadPool());
 
 		// INIT URI
 		scheme = uri.getScheme() == null ? "http" : uri.getScheme();
 		host = uri.getHost() == null ? "localhost" : uri.getHost();
 
 		port = uri.getPort();
 		if (port == -1) {
 			if (scheme.equalsIgnoreCase("http")) {
 				port = 80;
 			} else if (scheme.equalsIgnoreCase("https")) { // Kept for adding
 															// ssl support in
 															// the future..
 				port = 443;
 			}
 		}
 
 		if (!scheme.equalsIgnoreCase("http")) {
 			throw new SaploConnectionException(
 					"Only http transport is supported, your url is set to use "
 							+ scheme + ".");
 		}
 
 	}
 
 	//
 	// PUBLIC
 	//
 
 	public void get(String resource, SaploCallback<Object> callback)
 			throws SaploConnectionException {
 		request(resource, null, callback, HttpMethod.GET);
 	}
 
 	public void post(String resource, Object data,
 			SaploCallback<Object> callback) throws SaploConnectionException {
 		request(resource, data, callback, HttpMethod.POST);
 	}
 
 	public void put(String resource, Object data, SaploCallback<Object> callback)
 			throws SaploConnectionException {
 		request(resource, data, callback, HttpMethod.PUT);
 	}
 
 	public void delete(String resource, SaploCallback<Object> callback)
 			throws SaploConnectionException {
 		request(resource, null, callback, HttpMethod.DELETE);
 	}
 
 	public CookieEncoder cookies() {
 		return this.cookies;
 	}
 
 	//
 	// INTERNALS
 	//
 
 	protected void request(final String resource, final Object data,
 			SaploCallback<Object> callback, final HttpMethod method)
 			throws SaploConnectionException {
 
 		// CONNECT
 		ClientBootstrap boot = doBoot(callback, data);
 		ChannelFuture future = boot.connect(new InetSocketAddress(host, port));
 
 		// Create a channel, wait until it is established
 		future.addListener(new ChannelFutureListener() {
 
 			public void operationComplete(ChannelFuture future)
 					throws Exception {
 
 				if (!future.isSuccess()) {
 					throw new SaploConnectionException(
 							"Connection failed, see nested exception.", future
 									.getCause());
 				}
 
 				// Prepare the HTTP request
 				HttpRequest request = new DefaultHttpRequest(
 						HttpVersion.HTTP_1_1, method, resource);
 				request.setHeader(HttpHeaders.Names.HOST, host);
 				request.setHeader(HttpHeaders.Names.CONNECTION,
 						HttpHeaders.Values.CLOSE);
 				request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING,
 						HttpHeaders.Values.GZIP);
 
 
				//System.out.println(method + " " + resource);
 				if (method == HttpMethod.POST || method == HttpMethod.PUT) {
 					if (data != null) {
 						
						//System.out.println(data.toString());
 						
 						byte[] binaryData = binaryJSON(data);
 						ChannelBuffer buffer = ChannelBuffers
 								.buffer(binaryData.length);
 						buffer.writeBytes(binaryData);
 
 						request.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
 								String.valueOf(binaryData.length));
 						request.setContent(buffer);
 						
 					} else {
 						request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "0");
 						request.setContent(null);
 					}
 				}
 
 				try {
 					request.setHeader(HttpHeaders.Names.COOKIE,
 							cookies.encode());
 				} catch (IndexOutOfBoundsException e) {
 					// Om-nom-nom
 					// We end up here if there are no cookies to encode..
 				}
 
 				// Send request
 				future.getChannel().write(request);
 
 			}
 
 		});
 	}
 
 	protected ClientBootstrap doBoot(SaploCallback<Object> callback,
 			Object requestData) {
 		ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
 
 		bootstrap.getPipeline().addLast("decoder", new HttpResponseDecoder());
 		bootstrap.getPipeline().addLast("inflater",
 				new HttpContentDecompressor());
 		bootstrap.getPipeline().addLast("aggregator",
 				new HttpChunkAggregator(1048576));
 		bootstrap.getPipeline().addLast("encoder", new HttpRequestEncoder());
 		bootstrap.getPipeline().addLast("handler",
 				new JsonResponseHandler(callback, requestData));
 
 		return bootstrap;
 	}
 
 	protected static byte[] binaryJSON(Object data) {
 		byte[] binaryData;
 
 		if (data instanceof JSONObject || data instanceof JSONArray) {
 			binaryData = data.toString().getBytes();
 		} else {
 			binaryData = JSONValue.toJSONString(data).getBytes();
 		}
 
 		return binaryData;
 	}
 
 }
