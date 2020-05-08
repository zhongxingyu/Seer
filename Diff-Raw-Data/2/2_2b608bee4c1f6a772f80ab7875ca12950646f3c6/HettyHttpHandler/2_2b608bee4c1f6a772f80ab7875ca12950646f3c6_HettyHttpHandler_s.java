 package ddth.dasp.hetty.front;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Map;
 
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.handler.codec.http.HttpChunk;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
 import org.jboss.netty.handler.timeout.IdleStateEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ddth.dasp.hetty.message.IMessageFactory;
 import ddth.dasp.hetty.message.IRequest;
 import ddth.dasp.hetty.qnt.IQueueWriter;
 import ddth.dasp.hetty.utils.HettyControlPanelHttp;
 import ddth.dasp.hetty.utils.HettyUtils;
 
 public class HettyHttpHandler extends IdleStateAwareChannelHandler {
     private static Logger LOGGER = LoggerFactory.getLogger(HettyHttpHandler.class);
     private HttpRequest currentRequest;
     private ByteArrayOutputStream currentRequestContent = new ByteArrayOutputStream(4096);
 
     private IQueueWriter queueWriter;
     private IMessageFactory messageFactory;
 
     public HettyHttpHandler(IQueueWriter queueWriter, IMessageFactory messageFactory) {
         this.queueWriter = queueWriter;
         this.messageFactory = messageFactory;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
         HettyUtils.ALL_CHANNELS.add(e.getChannel());
     }
 
     protected void validateHttpChunk(HttpChunk httpChunk) {
         if (currentRequest == null || currentRequest.getContent() == null) {
             throw new IllegalStateException("No chunk started!");
         }
     }
 
     private String lookupQueueName(String host, Object queueConfig) {
         if (queueConfig instanceof Map<?, ?>) {
             Object obj = ((Map<?, ?>) queueConfig).get(host);
             if (obj == null) {
                 obj = ((Map<?, ?>) queueConfig).get("*");
             }
             return lookupQueueName(host, obj);
         } else {
             return queueConfig != null ? queueConfig.toString() : null;
         }
     }
 
     protected void handleRequest(HttpRequest httpRequest, byte[] requestContent, Channel userChannel)
             throws Exception {
         String uri = httpRequest.getUri();
         if (uri.startsWith("/hetty/") || uri.startsWith("/hetty?") || uri.equals("/hetty")) {
             IRequest request = messageFactory.buildRequest(httpRequest, userChannel.getId(),
                     requestContent);
             HettyControlPanelHttp.handleRequest(request, requestContent, userChannel);
         } else {
             IRequest request = messageFactory.buildRequest(httpRequest, userChannel.getId(),
                     requestContent);
             String host = request.getDomain();
             String queueName = lookupQueueName(host, HettyConnServer.getHostQueueNameMapping());
             if (queueName != null) {
                 userChannel.setAttachment(request.getId());
                 if (!queueWriter.queueWrite(queueName, request.serialize())) {
                     HettyUtils.responseText(userChannel, HttpResponseStatus.BAD_GATEWAY,
                             "No request queue for [" + host + "], or queue is full!");
                 }
             } else {
                 HettyUtils.responseText(userChannel, HttpResponseStatus.BAD_REQUEST, "Host ["
                         + host + "] is not mapped!");
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
         Object message = e.getMessage();
         boolean processMessage = false;
         if (message instanceof HttpChunk) {
             validateHttpChunk((HttpChunk) message);
 
             HttpChunk httpChunk = (HttpChunk) message;
             currentRequestContent.write(httpChunk.getContent().array());
             ChannelBuffer compositeBuffer = ChannelBuffers.wrappedBuffer(
                     currentRequest.getContent(), httpChunk.getContent());
             currentRequest.setContent(compositeBuffer);
             processMessage = httpChunk.isLast();
         } else if (message instanceof HttpRequest) {
             HttpRequest httpRequest = (HttpRequest) message;
             currentRequestContent.write(httpRequest.getContent().array());
             currentRequest = httpRequest;
             processMessage = !currentRequest.isChunked();
         }
         if (processMessage) {
             try {
                 handleRequest(currentRequest, currentRequestContent.toByteArray(), e.getChannel());
             } catch (Exception ex) {
                 StringWriter sw = new StringWriter();
                 PrintWriter pw = new PrintWriter(sw);
                 try {
                     ExceptionUtils.printRootCauseStackTrace(ex, pw);
                     HettyUtils.responseText(e.getChannel(),
                             HttpResponseStatus.INTERNAL_SERVER_ERROR, sw.toString());
                 } finally {
                     pw.close();
                 }
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
         ChannelFuture future = e.getFuture();
         if (future != null) {
             e.getFuture().cancel();
         }
        e.getChannel().close();
         if (LOGGER.isDebugEnabled()) {
             String msg = "Timeout [" + e.getState() + "]: " + e.getChannel();
             LOGGER.debug(msg);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
         Throwable t = e.getCause();
         LOGGER.error(t.getMessage(), t);
         ChannelFuture future = e.getFuture();
         if (future != null) {
             e.getFuture().cancel();
         }
         e.getChannel().close();
     }
 }
