 /*
 *	This file was modified from the LittleProxy source. 
 */
 package networking;
 
 import static org.jboss.netty.channel.Channels.pipeline;
 
 import java.net.UnknownHostException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.http.HttpChunk;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 
 /**
 * Class for handling all HTTP requests from the browser to the proxy.
 *
 * Note this class only ever handles a single connection from the browser.
 * The browser can and will, however, send requests to multiple hosts using
 * that same connection, i.e. it will send a request to host B once a request
 * to host A has completed.
 */
 public class HttpRequestHandler extends SimpleChannelUpstreamHandler implements RelayListener {
 	private final AtomicInteger						browserToProxyConnections		= new AtomicInteger(0);
 	private final Map<String, Queue<ChannelFuture>>	externalHostsToChannelFutures	= new ConcurrentHashMap<String, Queue<ChannelFuture>>();
 	private final AtomicInteger						unansweredRequestCount			= new AtomicInteger(0);
 	private final AtomicBoolean						browserChannelClosed			= new AtomicBoolean(false);
 	private volatile boolean						receivedChannelClosed			= false;
 	private volatile boolean						readingChunks;
 	private final RelayPipelineFactoryFactory		relayPipelineFactoryFactory;
 	private ChannelFuture							currentChannelFuture;
 	private final ChannelGroup						channelGroup;
 	private final ClientSocketChannelFactory		clientChannelFactory;
 
     /**
      * Creates a new class for handling HTTP requests with the specified
      * authentication manager.
      *
      * @param channelGroup The group of channels for keeping track of all
      * channels we've opened.
      * @param clientChannelFactory The common channel factory for clients.
      * @param relayPipelineFactoryFactory produces factories for any relays created.
      */
 	public HttpRequestHandler(	final ChannelGroup channelGroup,
 								final ClientSocketChannelFactory clientChannelFactory,
 								final RelayPipelineFactoryFactory relayPipelineFactoryFactory) {
 		this.channelGroup = channelGroup;
 		this.clientChannelFactory = clientChannelFactory;
 		this.relayPipelineFactoryFactory = relayPipelineFactoryFactory;
 	}
 
 	@Override
 	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent me) {
 		if(browserChannelClosed.get()) {
 			return;
 		}
 
 		if(!readingChunks) {
 			processRequest(ctx, me);
 		}
 		else {
 			processChunk(ctx, me);
 		}
 	}
 
 	private void processChunk(final ChannelHandlerContext ctx, final MessageEvent me) {
 		final HttpChunk chunk = (HttpChunk) me.getMessage();
 
 		if(chunk.isLast()) {
 			this.readingChunks = false;
 		}
 
 		if(currentChannelFuture.getChannel().isConnected()) {
 			currentChannelFuture.getChannel().write(chunk);
 		}
 		else {
 			currentChannelFuture.addListener(new ChannelFutureListener() {
 				public void operationComplete(final ChannelFuture future) throws Exception {
 					currentChannelFuture.getChannel().write(chunk);
 				}
 			});
 		}
 	}
 
 	private void processRequest(final ChannelHandlerContext ctx, final MessageEvent me) {
 		final HttpRequest request = (HttpRequest) me.getMessage();
 		final Channel inboundChannel = me.getChannel();
 		this.unansweredRequestCount.incrementAndGet();
 		String hostAndPort = ProxyUtils.parseHostAndPort(request);
 		String host = hostAndPort.split(":")[0];
 		
 		/**
 		 * Edit:
 		 * ProxyLog for dialog and log files
 		 * Author: Zong
 		 */
 		ProxyLog.appendDialog(ProxyLog.clientToString(inboundChannel), host, ProxyLog.READ_REQUEST);
 		ProxyLog.write(ProxyLog.clientToString(inboundChannel), host, request);
 
 		final class OnConnect {
 			public ChannelFuture onConnect(final ChannelFuture cf) {
 				/**
 				 * Edit:
 				 * ProxyLog for dialog
 				 * Author: Zong
 				 */
 				ProxyLog.appendDialog(ProxyLog.clientToString(inboundChannel), ProxyLog.serverToString(cf.getChannel()), ProxyLog.WROTE_REQUEST);
 				
 				if(request.getMethod() != HttpMethod.CONNECT) {
 					return cf.getChannel().write(request);
 				}
 				else {
 					writeConnectResponse(ctx, request, cf.getChannel());
 					return cf;
 				}
 			}
 		}
 
 		final OnConnect onConnect = new OnConnect();
 		final ChannelFuture curFuture = getChannelFuture(inboundChannel, hostAndPort);
 
 		if(curFuture != null) {
 			this.currentChannelFuture = curFuture;
 
 			if(curFuture.getChannel().isConnected()) {
 				onConnect.onConnect(curFuture);
 			}
 			else {
 				final ChannelFutureListener cfl = new ChannelFutureListener() {
 					public void operationComplete(final ChannelFuture future) throws Exception {
 						onConnect.onConnect(curFuture);
 					}
 				};
 				curFuture.addListener(cfl);
 			}
 		}
 		else {
 			final ChannelFuture cf;
 			ctx.getChannel().setReadable(false);
 
 			try {
 				cf = newChannelFuture(request, inboundChannel, hostAndPort);
 			}
 			catch(final UnknownHostException e) {
 				return;
 			}
 
 			final class LocalChannelFutureListener implements ChannelFutureListener {
 				private final String	hostAndPort;
 
 				LocalChannelFutureListener(final String hostAndPort) {
 					this.hostAndPort = hostAndPort;
 				}
 
 				public void operationComplete(final ChannelFuture future) throws Exception {
 					final Channel channel = future.getChannel();
 
 					if(channelGroup != null) {
 						channelGroup.add(channel);
 					}
 
 					if(future.isSuccess()) {
 						final ChannelFuture wf = onConnect.onConnect(cf);
 						
 						/**
 						 * Edit:
 						 * ProxyLog for adding connections
 						 * Author: Zong
 						 */
 						ProxyLog.addConnection(ProxyLog.clientToString(inboundChannel), ProxyLog.serverToString(future.getChannel()));
 						wf.addListener(new ChannelFutureListener() {
 							public void operationComplete(final ChannelFuture wcf) throws Exception {
								//ProxyLog.appendDialog(ProxyLog.serverToString(wf.getChannel()), ProxyLog.WROTE_REQUEST);
 								ctx.getChannel().setReadable(true);
 							}
 						});
 
 						currentChannelFuture = wf;
 					}
 					else {
 						onRelayChannelClose(inboundChannel, hostAndPort, 1, true);
 					}
 				}
 			}
 
 			cf.addListener(new LocalChannelFutureListener(hostAndPort));
 		}
 
 		if(request.isChunked()) {
 			readingChunks = true;
 		}
 	}
 
 	public void onChannelAvailable(final String hostAndPortKey, final ChannelFuture cf) {
 		synchronized(externalHostsToChannelFutures) {
 			final Queue<ChannelFuture> futures = externalHostsToChannelFutures.get(hostAndPortKey);
 			final Queue<ChannelFuture> toUse;
 
 			if(futures == null) {
 				toUse = new LinkedList<ChannelFuture>();
 				externalHostsToChannelFutures.put(hostAndPortKey, toUse);
 			}
 			else {
 				toUse = futures;
 			}
 
 			toUse.add(cf);
 		}
 	}
 
 	private ChannelFuture getChannelFuture(final Channel inbound, final String hostAndPort) {
 		synchronized(externalHostsToChannelFutures) {
 			final Queue<ChannelFuture> futures = externalHostsToChannelFutures.get(hostAndPort);
 
 			if(futures == null) {
 				return null;
 			}
 
 			if(futures.isEmpty()) {
 				return null;
 			}
 
 			final ChannelFuture cf = futures.remove();
 
 			if(cf != null && cf.isSuccess() && !cf.getChannel().isConnected()) {
 				removeProxyToWebConnection(inbound, hostAndPort);
 				return null;
 			}
 
 			return cf;
 		}
 	}
 
 	private void writeConnectResponse(final ChannelHandlerContext ctx, final HttpRequest httpRequest, final Channel outgoingChannel) {
 		final int port = ProxyUtils.parsePort(httpRequest);
 		final Channel browserToProxyChannel = ctx.getChannel();
 
 		if(port < 0) {
 			final String statusLine = "HTTP/1.1 502 Proxy Error\r\n";
 			ProxyUtils.writeResponse(browserToProxyChannel, statusLine, ProxyUtils.PROXY_ERROR_HEADERS);
 			ProxyUtils.closeOnFlush(browserToProxyChannel);
 		}
 		else {
 			browserToProxyChannel.setReadable(false);
 			ctx.getPipeline().remove("encoder");
 			ctx.getPipeline().remove("decoder");
 			ctx.getPipeline().remove("handler");
 			ctx.getPipeline().addLast("handler", new HttpConnectRelayingHandler(outgoingChannel, channelGroup));
 		}
 
 		final String statusLine = "HTTP/1.1 200 Connection established\r\n";
 		ProxyUtils.writeResponse(browserToProxyChannel, statusLine, ProxyUtils.CONNECT_OK_HEADERS);
 		browserToProxyChannel.setReadable(true);
 	}
 
 	private ChannelFuture newChannelFuture(final HttpRequest httpRequest, final Channel browserToProxyChannel, String hostAndPort) throws UnknownHostException {
 		final ClientBootstrap cb = new ClientBootstrap(clientChannelFactory);
 		final ChannelPipelineFactory cpf;
 		final String host;
 		final int port;
 
 		if(hostAndPort.contains(":")) {
 			host = ProxyUtils.substringBefore(hostAndPort, ":");
 			final String portString = ProxyUtils.substringAfter(hostAndPort, ":");
 			port = Integer.parseInt(portString);
 		}
 		else {
 			host = hostAndPort;
 			port = 80;
 		}
 
 		if(httpRequest.getMethod() == HttpMethod.CONNECT) {
 			cpf = new ChannelPipelineFactory() {
 				public ChannelPipeline getPipeline() throws Exception {
 					final ChannelPipeline pipeline = pipeline();
 					pipeline.addLast("handler", new HttpConnectRelayingHandler(browserToProxyChannel, channelGroup));
 					
 					return pipeline;
 				}
 			};
 		}
 		else {
 			cpf = relayPipelineFactoryFactory.getRelayPipelineFactory(httpRequest, browserToProxyChannel, this);
 		}
 
 		cb.setPipelineFactory(cpf);
 		cb.setOption("connectTimeoutMillis", 40 * 1000);
 		
 		return cb.connect(VerifiedAddressFactory.newInetSocketAddress(host, port));
 	}
 
 	@Override
 	public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent cse) throws Exception {
 		final Channel inboundChannel = cse.getChannel();
 		browserToProxyConnections.incrementAndGet();
 
 		if(channelGroup != null) {
 			channelGroup.add(inboundChannel);
 		}
 	}
 
 	@Override
 	public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent cse) {
 		browserToProxyConnections.decrementAndGet();
 
 		if(browserToProxyConnections.get() == 0) {
 			final Collection<Queue<ChannelFuture>> allFutures = this.externalHostsToChannelFutures.values();
 
 			for(final Queue<ChannelFuture> futures : allFutures) {
 				for(final ChannelFuture future : futures) {
 					final Channel ch = future.getChannel();
 
 					if(ch.isOpen()) {
 						future.getChannel().close();
 					}
 				}
 			}
 		}
 	}
 
 	public void onRelayChannelClose(final Channel browserToProxyChannel,
 									final String key,
 									final int unansweredRequestsOnChannel,
 									final boolean closedEndsResponseBody) {
 		if(closedEndsResponseBody) {
 			receivedChannelClosed = true;
 		}
 
 		removeProxyToWebConnection(browserToProxyChannel, key);
 		unansweredRequestCount.set(unansweredRequestCount.get() - unansweredRequestsOnChannel);
 
 		if(receivedChannelClosed && (externalHostsToChannelFutures.isEmpty() || unansweredRequestCount.get() == 0)) {
 			if(!browserChannelClosed.getAndSet(true)) {
 				ProxyUtils.closeOnFlush(browserToProxyChannel);
 			}
 		}
 	}
 
 	private void removeProxyToWebConnection(final Channel inbound, final String key) {
 		externalHostsToChannelFutures.remove(key);
 		String host = key.split(":")[0];
 		/**
 		 * Edit:
 		 * ProxyLog for removing connections
 		 * Author: Zong
 		 */
 		ProxyLog.removeConnection(ProxyLog.clientToString(inbound), host);
 	}
 
 	public void onRelayHttpResponse(final Channel browserToProxyChannel, final String key, final HttpRequest httpRequest) {
 		unansweredRequestCount.decrementAndGet();
 
 		if(unansweredRequestCount.get() == 0 && receivedChannelClosed) {
 			if(!browserChannelClosed.getAndSet(true)) {
 				ProxyUtils.closeOnFlush(browserToProxyChannel);
 			}
 		}
 	}
 
 	@Override
 	public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
 		ProxyUtils.closeOnFlush(e.getChannel());
 	}
 }
