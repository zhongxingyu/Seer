 package info.chenliang.myserver;
 
 import info.chenliang.myserver.messages.MyMessages.MessageBase;
 import info.chenliang.myserver.messages.MyMessages.MessageBase.MessageType;
 import info.chenliang.myserver.messages.MyMessages.VersionResponse;
 
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ChildChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.WriteCompletionEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MyServerHandler extends SimpleChannelUpstreamHandler{
 
 	private static Logger logger = LoggerFactory.getLogger(MyServerHandler.class);
 	
 	@Override
 	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.handleUpstream(ctx, e);
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		//super.messageReceived(ctx, e);
 		MessageBase mb = (MessageBase) e.getMessage();
         switch(mb.getType())
         {
         case VERSION_REQUEST:
         	//logger.warning("version request responded.");
         	VersionResponse versionResponse = VersionResponse.newBuilder().build();
         	
         	ctx.getChannel().write(MessageBase.newBuilder().setType(MessageType.VERSION_RESPONSE).setExtension(VersionResponse.id, versionResponse).build());
        	
        	logger.info("version response {}", "fake version 0.0.1");
         	break;
         default:
         	break;
         }
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.exceptionCaught(ctx, e);
 		
 	}
 
 	@Override
 	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.channelOpen(ctx, e);
 	}
 
 	@Override
 	public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.channelBound(ctx, e);
 	}
 
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.channelConnected(ctx, e);
		
		logger.info("client connected");
 	}
 
 	@Override
 	public void channelInterestChanged(ChannelHandlerContext ctx,
 			ChannelStateEvent e) throws Exception {
 		// TODO Auto-generated method stub
 		super.channelInterestChanged(ctx, e);
 	}
 
 	@Override
 	public void channelDisconnected(ChannelHandlerContext ctx,
 			ChannelStateEvent e) throws Exception {
 		// TODO Auto-generated method stub
 		super.channelDisconnected(ctx, e);
 	}
 
 	@Override
 	public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.channelUnbound(ctx, e);
 	}
 
 	@Override
 	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.channelClosed(ctx, e);
 		logger.warn("channel closed");
 	}
 
 	@Override
 	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e)
 			throws Exception {
 		// TODO Auto-generated method stub
 		super.writeComplete(ctx, e);
 	}
 
 	@Override
 	public void childChannelOpen(ChannelHandlerContext ctx,
 			ChildChannelStateEvent e) throws Exception {
 		// TODO Auto-generated method stub
 		super.childChannelOpen(ctx, e);
 	}
 
 	@Override
 	public void childChannelClosed(ChannelHandlerContext ctx,
 			ChildChannelStateEvent e) throws Exception {
 		// TODO Auto-generated method stub
 		super.childChannelClosed(ctx, e);
 	}
 
 	
 }
