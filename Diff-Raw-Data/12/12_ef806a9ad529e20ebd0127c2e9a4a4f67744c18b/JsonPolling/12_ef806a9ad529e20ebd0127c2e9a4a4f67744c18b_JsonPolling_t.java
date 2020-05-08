 /**
  * @author Sergey Chernov
  *         See LICENSE file in the root of the project
  */
 package sockjs.transports;
 
 import org.codehaus.jackson.JsonParseException;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.UpstreamMessageEvent;
 import org.jboss.netty.handler.codec.http.*;
 import org.jboss.netty.util.CharsetUtil;
 import org.jboss.netty.util.internal.StringUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sockjs.Connection;
 import sockjs.SockJs;
 import sockjs.netty.*;
 
 import java.net.URLDecoder;
 import java.util.List;
 
 public class JsonPolling extends AbstractTransport {
 
     private static final Logger log = LoggerFactory.getLogger(JsonPolling.class);
 
     private static final String JSONP_OPEN_FRAME = "\"o\"";
 
     public JsonPolling(SockJs sockJs) {
         super(sockJs);
     }
 
     @Override
     public void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest httpRequest) {
         if (httpRequest.getMethod() == HttpMethod.GET) {
             pollMessage(ctx, httpRequest);
         } else if (httpRequest.getMethod() == HttpMethod.POST) {
             handleJsonPollingSend(ctx, httpRequest);
         }
     }
 
     @Override
     public void sendHeartbeat(Connection connection) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void sendMessage(Connection connection, String message) {
         log.info("sendMessage: " + message);
         String encodedMessage = connection.getJsonpCallback() + "(" + message + ");\r\n";
         ChannelBuffer content = ChannelBuffers.copiedBuffer(encodedMessage, CharsetUtil.UTF_8);
         HttpResponse response = createResponse(content);
         HttpHelpers.addJESSIONID(response, connection.getJSESSIONID());
         if (connection.getChannel().isWritable()) {
             connection.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
             connection.incSentBytes(content.readableBytes());
 
             if (connection.getSentBytes() > getSockJs().getMaxStreamSize()) {
                 connection.resetSentBytes();
                 connection.setCloseReason(Protocol.CloseReason.NORMAL);
             }
         }
 
         if (JSONP_OPEN_FRAME.equals(message)) {
             getSockJs().notifyListenersAboutNewConnection(connection);
         }
     }
 
     @Override
     public void handleCloseRequest(Connection connection, Protocol.CloseReason reason) {
         if (connection.getChannel().isWritable()) {
             ChannelBuffer content = ChannelBuffers.copiedBuffer(Protocol
                     .encodeJsonpClose(reason, connection.getJsonpCallback()), CharsetUtil.UTF_8);
 
             connection.getChannel().write(new DefaultHttpChunk(content)).addListener(ChannelFutureListener.CLOSE);
         }
     }
 
     private void handleJsonPollingSend(ChannelHandlerContext ctx, HttpRequest httpRequest) {
         SockJsHandlerContext sockJsHandlerContext = getSockJsHandlerContext(ctx.getChannel());
         if (sockJsHandlerContext != null) {
             String message = httpRequest.getContent().toString(CharsetUtil.UTF_8);
             if (message.startsWith("d=")) {
                 try {
                     message = message.length() >= 5 ? URLDecoder.decode(message.substring(2), "UTF-8"): "";
                 } catch (Exception e) {
                     HttpHelpers.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Payload expected.");
                     return;
                 }
             } else if (!message.isEmpty() && !(message.charAt(0) == '[' || message.charAt(0) == '\"')) {
                 message = "";
             }
 
             if (message.trim().isEmpty()) {
                 HttpHelpers.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Payload expected.");
                 return;
             }
 
             log.info("Message received: " + message);
             Connection connection = sockJsHandlerContext.getConnection();
             if (connection != null) {
                 try {
                     String[] messages = Protocol.decodeMessage(message);
                     if (messages != null) {
                         HttpResponse response = createResponse(ChannelBuffers.copiedBuffer("ok", CharsetUtil.UTF_8));
                         HttpHelpers.addJESSIONID(response, connection.getJSESSIONID());
                         response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
                         ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
                         for (String decodedMessage : messages) {
                             connection.sendToListeners(decodedMessage);
                         }
                     } else {
                         HttpHelpers.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Payload expected.");
                     }
 
                 } catch (JsonParseException ex) {
                     HttpHelpers.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Broken JSON encoding.");
                 }
 
             } else {
                 HttpHelpers.sendError(ctx, HttpResponseStatus.NOT_FOUND);
             }
         }
     }
 
     private void pollMessage(ChannelHandlerContext ctx, HttpRequest httpRequest) {
         List<String> callbacks = new QueryStringDecoder(httpRequest.getUri()).getParameters().get("c");
         if (callbacks == null || callbacks.isEmpty()) {
             HttpHelpers.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                     "\"callback\" parameter required");
             return;
         }
 
         SockJsHandlerContext sockJsHandlerContext = getSockJsHandlerContext(ctx);
         if (sockJsHandlerContext != null) {
             Connection connection = sockJsHandlerContext.getConnection();
             SockJsEvent sendEvent;
             if (connection == null) {
                 connection = getSockJs().createConnection(sockJsHandlerContext);
                 sockJsHandlerContext.setConnection(connection);
                 connection.setJsonpCallback(callbacks.get(0));
                 connection.setChannel(ctx.getChannel());
                 connection.setJSESSIONID(sockJsHandlerContext.getJSESSIONID());
                 sendEvent = new SockJsSendEvent(connection, JSONP_OPEN_FRAME, true);
             } else if (connection.getCloseReason() != null) {
                 log.info("Connection is closed: " + connection.getCloseReason());
                 connection.setChannel(ctx.getChannel());
                 sendEvent = new SockJsCloseEvent(connection, connection.getCloseReason());
             } else {
                 log.info("polling all messages we have to send");
                 connection.setChannel(ctx.getChannel());
                 String[] messagesToSend = connection.pollAllMessages();
                 if (messagesToSend.length > 0) {
                    String encodedMessage = Protocol.encodeToJSONString(messagesToSend);
                     sendEvent = new SockJsSendEvent(connection, encodedMessage , true);
                 } else {
                     sendEvent = null;
                 }
             }
 
             if (sendEvent != null) {
                 ctx.getPipeline().sendUpstream(new UpstreamMessageEvent(ctx.getChannel(),
                         sendEvent, ctx.getChannel().getRemoteAddress()));
             }
         } else {
             HttpHelpers
                     .sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                             "Internal Server Error");
         }
 
     }
 
     private static HttpResponse createResponse(ChannelBuffer content) {
         HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                 HttpResponseStatus.OK);
         response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
         response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
         response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "no-store, no-cache, must-revalidate," +
                 "" + " max-age=0");
         response.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
         response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/javascript;charset=UTF-8");
         response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
 
         response.setContent(content);
         return response;
     }
 
 
 }
