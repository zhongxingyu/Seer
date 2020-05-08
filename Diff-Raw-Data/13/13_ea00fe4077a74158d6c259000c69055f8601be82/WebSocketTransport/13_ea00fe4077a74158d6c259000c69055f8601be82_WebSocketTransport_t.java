 package com.barchart.netty.client.transport;
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.buffer.ByteBuf;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 import io.netty.channel.ChannelPipeline;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.handler.codec.MessageToMessageCodec;
 import io.netty.handler.codec.http.HttpClientCodec;
 import io.netty.handler.codec.http.HttpObjectAggregator;
 import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
 import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
 import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
 import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
 import io.netty.handler.codec.http.websocketx.WebSocketVersion;
 import io.netty.handler.ssl.SslHandler;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.net.URI;
 import java.util.List;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLEngine;
 
 /**
  * Websocket streaming transport. Automatically upgrades the HTTP connection to
  * websockets and handles encoding/decoding bytes into WebSocket frames so that
  * to downstream ChannelHandlers it just looks like a standard byte stream.
  */
 public class WebSocketTransport implements TransportProtocol {
 
 	private final URI uri;
 	private final InetSocketAddress address;
 
 	/**
 	 * Construct a WebSocketTransport with the specified URI. If the
 	 * <code>wss://</code> protocol is specified, SSL-TLS will be activated for
 	 * the connection.
 	 */
 	public WebSocketTransport(final URI uri_) {
 
 		uri = uri_;
 
 		int port = uri.getPort();
 
 		if (port == -1) {
 			port = uri.getScheme().equalsIgnoreCase("wss") ? 443 : 80;
 		}
 
 		address = new InetSocketAddress(uri.getHost(), port);
 
 	}
 
 	@Override
 	public void initPipeline(final ChannelPipeline pipeline) throws Exception {
 
 		final WebSocketClientHandshaker handshaker =
 				WebSocketClientHandshakerFactory.newHandshaker(uri,
 						WebSocketVersion.V13, null, false, null);
 
 		final WebSocketClientProtocolHandler wsHandler =
 				new WebSocketClientProtocolHandler(handshaker);
 
 		pipeline.addFirst(new HttpClientCodec(), //
 				new HttpObjectAggregator(65536), //
				wsHandler,
				// Fires channelActive() after handshake and removes self
				new WebSocketConnectedNotifier(),
				// BinaryWebSocketFrame <-> ByteBuf codec before user codecs
				new WebSocketBinaryCodec());
 
 		if (uri.getScheme().equalsIgnoreCase("wss")
 				&& pipeline.get(SslHandler.class) == null) {
 
 			final SSLEngine sslEngine =
 					SSLContext.getDefault().createSSLEngine();
 			sslEngine.setUseClientMode(true);
 			pipeline.addFirst("ssl", new SslHandler(sslEngine));
 
 		}
 
 	}
 
 	private class WebSocketConnectedNotifier extends
 			ChannelInboundHandlerAdapter {
 
 		@Override
 		public void userEventTriggered(final ChannelHandlerContext ctx,
 				final Object evt) throws Exception {
 
 			if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
 
 				ctx.fireChannelActive();
 
 				ctx.pipeline().remove(this);
 
 			}
 
 		}
 
 		@Override
 		public void channelActive(final ChannelHandlerContext ctx)
 				throws Exception {
 			// Block downstream relay until handshake completes
 		}
 
 	}
 
 	private class WebSocketBinaryCodec extends
 			MessageToMessageCodec<BinaryWebSocketFrame, ByteBuf> {
 
 		public WebSocketBinaryCodec() {
 			super(BinaryWebSocketFrame.class, ByteBuf.class);
 		}
 
 		@Override
 		protected void encode(final ChannelHandlerContext ctx,
 				final ByteBuf msg, final List<Object> out) throws Exception {
 			out.add(new BinaryWebSocketFrame(msg));
 		}
 
 		@Override
 		protected void decode(final ChannelHandlerContext ctx,
 				final BinaryWebSocketFrame msg, final List<Object> out)
 				throws Exception {
 			msg.retain();
 			out.add(msg.content());
 		}
 
 	}
 
 	@Override
 	public Class<? extends Channel> channel() {
 		return NioSocketChannel.class;
 	}
 
 	@Override
 	public SocketAddress address() {
 		return address;
 	}
 
 	@Override
 	public Bootstrap bootstrap() {
 		return DEFAULT_BOOTSTRAP.clone().channel(channel())
 				.remoteAddress(address());
 	}
 
 }
