 package org.myrest.http;
 
 import java.net.InetSocketAddress;
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 import org.myrest.util.RestPathMappingContainer;
 
 public class HttpRestHandler extends SimpleChannelUpstreamHandler {
 
 	private static final Logger LOG = Logger.getLogger(HttpRestHandler.class);
 
 	final RestPathMappingContainer[] mappings;
 
 	public HttpRestHandler(RestPathMappingContainer[] mappings) {
 		this.mappings = mappings;
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 			throws Exception {
 
 		HttpRequest request = (HttpRequest) e.getMessage();
 
 		InetSocketAddress inetAddress = (InetSocketAddress) e
 				.getRemoteAddress();
 //		String ip = inetAddress.getAddress().getHostAddress();
 
 		final int len = mappings.length;
 		HttpResponse response = null;
 
		final String path = request.getUri();
 
 		for (int i = 0; i < len; i++) {
 			RestPathMappingContainer container = mappings[i];
 			if (container.matches(path)) {
 				response = container.call(request, path);
 				break;
 			}
 		}
 
 		if (response == null)
 			response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
 					HttpResponseStatus.NOT_FOUND);
 
 		e.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
 			throws Exception {
 		e.getChannel().close();
 		LOG.error(e.getCause(), e.getCause());
 	}
 
 }
