 /*
 *	This file was modified from the LittleProxy source. 
 */
 package networking;
 
 import java.util.LinkedList;
 import java.util.Queue;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpChunk;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 
 /**
 * Class that simply relays traffic from a remote server the proxy is
 * connected to back to the browser.
 */
 public class HttpRelayingHandler extends SimpleChannelUpstreamHandler {
 	private final Queue<HttpRequest>	requestQueue	= new LinkedList<HttpRequest>();
 	private volatile boolean	readingChunks;
 	private final Channel		browserToProxyChannel;
 	private final ChannelGroup	channelGroup;
 	private final HttpFilter	httpFilter;
 	private HttpResponse		originalHttpResponse;
 	private HttpRequest			currentHttpRequest;
 	private final RelayListener	relayListener;
 	private final String		hostAndPort;
 	private boolean				closeEndsResponseBody;
 
     /**
      * Creates a new {@link HttpRelayingHandler} with the specified connection
      * to the browser.
      *
      * @param browserToProxyChannel The browser connection.
      * @param channelGroup Keeps track of channels to close on shutdown.
      * @param filter The HTTP filter.
      * @param hostAndPort Host and port we're relaying to.
      */
 	public HttpRelayingHandler(	final Channel browserToProxyChannel,
 								final ChannelGroup channelGroup,
 								final HttpFilter filter,
 								final RelayListener relayListener,
 								final String hostAndPort) {
 		this.browserToProxyChannel = browserToProxyChannel;
 		this.channelGroup = channelGroup;
 		this.httpFilter = filter;
 		this.relayListener = relayListener;
 		this.hostAndPort = hostAndPort;
 	}
 
 	@Override
 	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent me) throws Exception {
 		final Object messageToWrite;
 		final boolean writeEndBuffer;
 		
 		/**
 		 * Edit:
 		 * ProxyLog for dialog
 		 * Author: Zong
 		 */
 		ProxyLog.appendDialog(ProxyLog.clientToString(browserToProxyChannel), ProxyLog.serverToString(me.getChannel()), ProxyLog.READ_RESPONSE);
 		
 		if(!readingChunks) {
 			final HttpResponse hr = (HttpResponse) me.getMessage();
 			
 			/**
 			 * Edit:
 			 * ProxyLog for log files
 			 * Author: Zong
 			 */
 			ProxyLog.write(ProxyLog.clientToString(browserToProxyChannel), ProxyLog.serverToString(me.getChannel()), hr);
 			
 			originalHttpResponse = ProxyUtils.copyMutableResponseFields(hr, new DefaultHttpResponse(hr.getProtocolVersion(), hr.getStatus()));
 			final HttpResponse response;
 			final String te = hr.getHeader(HttpHeaders.Names.TRANSFER_ENCODING);
 
 			if(ProxyUtils.isNotBlank(te) && te.equalsIgnoreCase(HttpHeaders.Values.CHUNKED)) {
 				if(hr.getProtocolVersion() != HttpVersion.HTTP_1_1) {
 					response = ProxyUtils.copyMutableResponseFields(hr, new DefaultHttpResponse(HttpVersion.HTTP_1_1, hr.getStatus()));
 
 					if(!response.containsHeader(HttpHeaders.Names.TRANSFER_ENCODING)) {
 						response.addHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
 					}
 				}
 				else {
 					response = hr;
 				}
 			}
 			else {
 				response = hr;
 			}
 
 			if(response.isChunked()) {
 				readingChunks = true;
 				writeEndBuffer = false;
 			}
 			else {
 				writeEndBuffer = true;
 			}
 
 			if(!this.requestQueue.isEmpty()) {
 				currentHttpRequest = requestQueue.remove();
 			}
 
 			messageToWrite = httpFilter.filterResponse(currentHttpRequest, response);
 		}
 		else {
 			final HttpChunk chunk = (HttpChunk) me.getMessage();
 			
 			/**
 			 * Edit:
 			 * ProxyLog for log files
 			 * Author: Zong
 			 */
 			ProxyLog.write(ProxyLog.clientToString(browserToProxyChannel), ProxyLog.serverToString(me.getChannel()), chunk);
 			
 			if(chunk.isLast()) {
 				readingChunks = false;
 				writeEndBuffer = true;
 			}
 			else {
 				writeEndBuffer = false;
 			}
 
 			messageToWrite = chunk;
 		}
 		
 		if(browserToProxyChannel.isConnected()) {
 			final boolean closeRemote = shouldCloseRemoteConnection(currentHttpRequest, originalHttpResponse, messageToWrite);
 			final boolean closePending = shouldCloseBrowserConnection(currentHttpRequest, originalHttpResponse, messageToWrite);
 			final boolean wroteFullResponse = wroteFullResponse(originalHttpResponse, messageToWrite);
 
 			if(closeRemote && closeEndsResponseBody(originalHttpResponse)) {
 				closeEndsResponseBody = true;
 			}
 
 			ChannelFuture future = browserToProxyChannel.write(new ProxyHttpResponse(currentHttpRequest, originalHttpResponse, messageToWrite));
 
 			/**
 			 * Edit:
 			 * ProxyLog for dialog
 			 * Author: Zong
 			 */
			ProxyLog.appendDialog(ProxyLog.clientToString(browserToProxyChannel), ProxyLog.serverToString(me.getChannel()), ProxyLog.WROTE_RESPONSE);
 			
 			if(writeEndBuffer) {
 				future = browserToProxyChannel.write(ChannelBuffers.EMPTY_BUFFER);
 			}
 
 			if(wroteFullResponse) {
 				future.addListener(new ChannelFutureListener() {
 					public void operationComplete(final ChannelFuture cf) throws Exception {
 						relayListener.onRelayHttpResponse(browserToProxyChannel, hostAndPort, currentHttpRequest);
 					}
 				});
 			}
 
 			if(closeRemote) {
 				future.addListener(new ChannelFutureListener() {
 					public void operationComplete(final ChannelFuture cf) throws Exception {
 						if(me.getChannel().isConnected()) {
 							me.getChannel().close();
 						}
 					}
 				});
 			}
 
 			if(closePending) {
 				future.addListener(new ChannelFutureListener() {
 					public void operationComplete(final ChannelFuture cf) throws Exception {
 						ProxyUtils.closeOnFlush(browserToProxyChannel);
 					}
 				});
 			}
 
 			if(wroteFullResponse && (!closePending && !closeRemote)) {
 				relayListener.onChannelAvailable(hostAndPort, Channels.succeededFuture(me.getChannel()));
 			}
 		}
 		else {
 			if(me.getChannel().isConnected()) {
 				me.getChannel().close();
 			}
 		}
 	}
 
 	private boolean closeEndsResponseBody(final HttpResponse res) {
 		final String cl = res.getHeader(HttpHeaders.Names.CONTENT_LENGTH);
 
 		if(ProxyUtils.isNotBlank(cl)) {
 			return false;
 		}
 
 		final String te = res.getHeader(HttpHeaders.Names.TRANSFER_ENCODING);
 
 		if(ProxyUtils.isNotBlank(te) && te.equalsIgnoreCase(HttpHeaders.Values.CHUNKED)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean wroteFullResponse(final HttpResponse res, final Object messageToWrite) {
 		if(res.isChunked()) {
 			if(messageToWrite instanceof HttpResponse) {
 				return false;
 			}
 			return ProxyUtils.isLastChunk(messageToWrite);
 		}
 
 		return true;
 	}
 
 	private boolean shouldCloseBrowserConnection(final HttpRequest req, final HttpResponse res, final Object msg) {
 		if(res.isChunked()) {
 			if(msg != null) {
 				if(!ProxyUtils.isLastChunk(msg)) {
 					return false;
 				}
 				else {}
 			}
 		}
 
 		final String proxyConnectionKey = "Proxy-Connection";
 
 		if(req.containsHeader(proxyConnectionKey)) {
 			final String header = req.getHeader(proxyConnectionKey);
 			req.removeHeader(proxyConnectionKey);
 			if(req.getProtocolVersion() == HttpVersion.HTTP_1_1) {
 				req.setHeader("Connection", header);
 			}
 		}
 
 		if(!HttpHeaders.isKeepAlive(req)) {
 			return true;
 		}
 
 		return false;
 	}
 
     /**
      * Determines if the remote connection should be closed based on the
      * request and response pair. If the request is HTTP 1.0 with no
      * keep-alive header, for example, the connection should be closed.
      *
      * This in part determines if we should close the connection. Here's the
      * relevant section of RFC 2616:
      *
      * "HTTP/1.1 defines the "close" connection option for the sender to
      * signal that the connection will be closed after completion of the
      * response. For example,
      *
      * Connection: close
      *
      * in either the request or the response header fields indicates that the
      * connection SHOULD NOT be considered `persistent' (section 8.1) after
      * the current request/response is complete."
      */
 	private boolean shouldCloseRemoteConnection(final HttpRequest req, final HttpResponse res, final Object msg) {
 		if(res.isChunked()) {
 			if(msg != null) {
 				if(!ProxyUtils.isLastChunk(msg)) {
 					return false;
 				}
 			}
 		}
 
 		if(!HttpHeaders.isKeepAlive(req)) {
 			return true;
 		}
 
 		if(!HttpHeaders.isKeepAlive(res)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent cse) throws Exception {
 		final Channel ch = cse.getChannel();
 
 		if(this.channelGroup != null) {
 			channelGroup.add(ch);
 		}
 	}
 
 	@Override
 	public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
 		final int unansweredRequests = requestQueue.size();
 		relayListener.onRelayChannelClose(browserToProxyChannel, hostAndPort, unansweredRequests, closeEndsResponseBody);
 	}
 
 	@Override
 	public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
 		if(e.getChannel().isConnected()) {
 			ProxyUtils.closeOnFlush(e.getChannel());
 		}
 	}
 
     /**
      * Adds this HTTP request. We need to keep track of all encoded requests
      * because we ultimately need the request data to determine whether or not
      * we can cache responses. It's a queue because we're dealing with HTTP 1.1
      * persistent connections, and we need to match all requests with responses.
      *
      * NOTE that this is the original, unmodified request in this case without
      * hop-by-hop headers stripped and without HTTP request filters applied.
      * It's the raw request we received from the client connection.
      *
      * See ProxyHttpRequestEncoder.
      *
      * @param request The HTTP request to add.
      */
 	public void requestEncoded(final HttpRequest request) {
 		requestQueue.add(request);
 	}
 }
