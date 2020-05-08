 /*
 *	This file was modified from the LittleProxy source. 
 */
 package networking;
 
 import static org.jboss.netty.channel.Channels.pipeline;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
 import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
 import org.jboss.netty.handler.codec.http.HttpMessage;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
 import org.jboss.netty.handler.timeout.IdleStateHandler;
 import org.jboss.netty.util.HashedWheelTimer;
 import org.jboss.netty.util.Timer;
 
 public class DefaultRelayPipelineFactory implements ChannelPipelineFactory {
 	private static final Timer			TIMER	= new HashedWheelTimer();
 	private final String				hostAndPort;
 	private final HttpRequest			httpRequest;
 	private final RelayListener			relayListener;
 	private final Channel				browserToProxyChannel;
 	private final ChannelGroup			channelGroup;
 	private final HttpRequestFilter		requestFilter;
 	private final boolean				filtersOff;
 	private final HttpResponseFilters	responseFilters;
 
 	public DefaultRelayPipelineFactory(	final String hostAndPort,
 										final HttpRequest httpRequest,
 										final RelayListener relayListener,
 										final Channel browserToProxyChannel,
 										final ChannelGroup channelGroup,
 										final HttpResponseFilters responseFilters,
 										final HttpRequestFilter requestFilter) {
 		this.hostAndPort = hostAndPort;
 		this.httpRequest = httpRequest;
 		this.relayListener = relayListener;
 		this.browserToProxyChannel = browserToProxyChannel;
 		this.channelGroup = channelGroup;
 		this.responseFilters = responseFilters;
 		this.requestFilter = requestFilter;
 		this.filtersOff = responseFilters == null;
 	}
 
 	public ChannelPipeline getPipeline() throws Exception {
 		final ChannelPipeline pipeline = pipeline();
 		final HttpMethod method = httpRequest.getMethod();
 		final HttpRelayingHandler handler;
 		final boolean shouldFilter;
 		final HttpFilter filter;
 				
 		if(httpRequest.getMethod() == HttpMethod.HEAD) {
 			pipeline.addLast("decoder", new HttpResponseDecoder(8192, 8192 * 2, 8192 * 2) {
 				@Override
 				protected boolean isContentAlwaysEmpty(final HttpMessage msg) {
 					return true;
 				}
 			});
 		}
 		else {
 			pipeline.addLast("decoder", new HttpResponseDecoder(8192, 8192 * 2, 8192 * 2));
 		}
 
 		if(filtersOff) {
 			shouldFilter = false;
 			filter = null;
 		}
 		else {
 			filter = responseFilters.getFilter(hostAndPort);
 			
 			if(filter == null) {
 				shouldFilter = false;
 			}
 			else {
 				shouldFilter = filter.filterResponses(httpRequest);
 				
 				if(shouldFilter) {
					//pipeline.addLast("inflater", new HttpContentDecompressor());
					//pipeline.addLast("aggregator", new HttpChunkAggregator(filter.getMaxResponseSize()));
 				}
 			}
 		}
 
		if(shouldFilter) {
 			handler = new HttpRelayingHandler(browserToProxyChannel, channelGroup, filter, relayListener, hostAndPort);
 		}
 		else {
 			handler = new HttpRelayingHandler(browserToProxyChannel, channelGroup, new NoOpHttpFilter(), relayListener, hostAndPort);
 		}
 
 		pipeline.addLast("encoder", new ProxyHttpRequestEncoder(handler, requestFilter));
 				
 		if(!method.equals(HttpMethod.CONNECT)) {
 			final int readTimeoutSeconds;
 			final int writeTimeoutSeconds;
 
 			if(method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
 				readTimeoutSeconds = 0;
 				writeTimeoutSeconds = 70;
 			}
 			else {
 				readTimeoutSeconds = 70;
 				writeTimeoutSeconds = 0;
 			}
 
 			pipeline.addLast("idle", new IdleStateHandler(TIMER, readTimeoutSeconds, writeTimeoutSeconds, 0));
 			pipeline.addLast("idleAware", new IdleAwareHandler("Relay-Handler"));
 		}
 
 		pipeline.addLast("handler", handler);
 		
 		return pipeline;
 	}
 }
