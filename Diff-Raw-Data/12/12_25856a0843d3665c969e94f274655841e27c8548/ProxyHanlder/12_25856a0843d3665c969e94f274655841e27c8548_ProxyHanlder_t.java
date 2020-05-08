 package com.butterfly;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.CountDownLatch;
 
 import org.apache.log4j.Logger;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 
 /**
  * the handler. talk between client and server
  * 
  * @see <a
  *      href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/proxy/package-summary.html">proxy</a>
  * 
  * 
  * @since 2010/11/27
  */
 public class ProxyHanlder extends SimpleChannelUpstreamHandler {
 
 	private static Logger logger = Logger.getLogger(ProxyHanlder.class);
 	private static ClientSocketChannelFactory cf = new NioClientSocketChannelFactory(
	        ProxyServer.THREADPOOL, ProxyServer.THREADPOOL);
 	private final Object mLock = new Object();
 	// private OutboundHandler outboundHandler;
 	private volatile Channel mOutboundChannel;
 	private Channel mInboundChannel;
 	private CountDownLatch mLatch = new CountDownLatch(1);
 
 	public ProxyHanlder(String host, int port, Channel channel) {
 
 		mInboundChannel = channel;
 		mInboundChannel.setReadable(false);
 
 		ClientBootstrap cb = new ClientBootstrap(cf);
 		OutboundHandler outboundHandler = new OutboundHandler();
 		cb.getPipeline().addLast("hanlder", outboundHandler);
 		ChannelFuture future = cb.connect(new InetSocketAddress(host, port));
 		mOutboundChannel = future.getChannel();
 		future.addListener(new ChannelFutureListener() {
 			@Override
 			public void operationComplete(ChannelFuture future)
 			        throws Exception {
 				if (future.isSuccess()) {
 					mInboundChannel.setReadable(true);
 					logger.info("connect to remote success: "
 					        + mOutboundChannel.getRemoteAddress());
 				} else {
 					mInboundChannel.close();
 					logger.info("connect to remote fail: "
 					        + mOutboundChannel.getRemoteAddress());
 
 				}
 				mLatch.countDown();
 			}
 		});
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 	        throws Exception {
 		mLatch.await();
 		synchronized (mLock) {
 			ChannelBuffer msg = (ChannelBuffer) e.getMessage();
 			logger.trace("received from client " + msg.readableBytes()
 			        + " byte of data");
 			mOutboundChannel.write(msg);
 			if (!mOutboundChannel.isWritable()) {
 				mInboundChannel.setReadable(false);
 			}
 		}
 	}
 
 	@Override
 	public void channelInterestChanged(ChannelHandlerContext ctx,
 	        ChannelStateEvent e) throws Exception {
 		synchronized (mLock) {
 			if (mInboundChannel.isWritable())
 				mOutboundChannel.setReadable(true);
 		}
 
 	}
 
 	@Override
 	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
 	        throws Exception {
 		if (mOutboundChannel != null)
 			closeOnFlush(mOutboundChannel);
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
 	        throws Exception {
 		logger.error(e.getCause().getMessage(), e.getCause());
 		closeOnFlush(mInboundChannel);
 	}
 
 	private class OutboundHandler extends SimpleChannelUpstreamHandler {
 
 		@Override
 		public void messageReceived(ChannelHandlerContext ctx,
 		        final MessageEvent e) throws Exception {
 			ChannelBuffer msg = (ChannelBuffer) e.getMessage();
 			logger.trace("received from server " + msg.readableBytes()
 			        + " byte of data");
 
 			synchronized (mLock) {
 				mInboundChannel.write(msg);
 				// If inboundChannel is saturated, do not read until notified in
 				// HexDumpProxyInboundHandler.channelInterestChanged().
 				if (!mInboundChannel.isWritable()) {
 					mOutboundChannel.setReadable(false);
 				}
 			}
 		}
 
 		@Override
 		public void channelInterestChanged(ChannelHandlerContext ctx,
 		        ChannelStateEvent e) throws Exception {
 			// If outboundChannel is not saturated anymore, continue accepting
 			// the incoming traffic from the inboundChannel.
 			synchronized (mLock) {
 				if (mOutboundChannel.isWritable()) {
 					mInboundChannel.setReadable(true);
 				}
 			}
 		}
 
 		@Override
 		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
 		        throws Exception {
 			closeOnFlush(mInboundChannel);
 		}
 
 		@Override
 		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
 		        throws Exception {
 			logger.error(e.getCause().getMessage(), e.getCause());
 			closeOnFlush(mOutboundChannel);
 		}
 	}
 
 	/**
 	 * Closes the specified channel after all queued write requests are flushed.
 	 */
 	static void closeOnFlush(Channel ch) {
 		if (ch.isConnected()) {
 			ch.write(ChannelBuffers.EMPTY_BUFFER).addListener(
 			        ChannelFutureListener.CLOSE);
 		}
 	}
 }
