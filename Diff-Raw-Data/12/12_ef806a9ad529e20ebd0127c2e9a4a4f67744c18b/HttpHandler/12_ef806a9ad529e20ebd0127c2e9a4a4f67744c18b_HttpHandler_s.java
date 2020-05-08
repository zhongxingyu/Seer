 /**
  * @author Sergey Chernov
  *         See LICENSE file in the root of the project
  */
 package sockjs.netty;
 
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.handler.codec.http.*;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
 import org.jboss.netty.util.CharsetUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sockjs.SockJs;
 import sockjs.Transport;
 import sockjs.transports.AbstractTransport;
 
 import java.util.Date;
 
 public class HttpHandler extends SimpleChannelHandler {
 
     private static final Logger log = LoggerFactory.getLogger(HttpHandler.class);
 
     private final SockJs sockJs;
 
     private boolean credentialAllowed = true;
 
     private int maxAge = 31536000;
 
     private static final String GREETING = "Welcome to SockJS!\n";
 
     public HttpHandler(SockJs sockJs) {
         this.sockJs = sockJs;
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
             throws Exception {
         Object msg = e.getMessage();
         if (msg instanceof HttpRequest) {
             handleRequest(ctx, (HttpRequest) msg);
         } else if (msg instanceof WebSocketFrame) {
             handleFrame(ctx, (WebSocketFrame)msg);
         }
     }
 
     private void handleRequest(ChannelHandlerContext ctx, HttpRequest req) {
 
         if (!sockJs.hasListenerForRoute(req.getUri())) {
             HttpHelpers.sendError(ctx, HttpResponseStatus.NOT_FOUND);
             return;
         }
 
         if (sockJs.isRootOfBaseUrl(req.getUri())) {
             if (req.getMethod() == HttpMethod.GET) {
                 sendGreeting(ctx, req);
                 return;
             } else if (req.getMethod() != HttpMethod.GET) {
                 HttpHelpers.sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
                 return;
             }
         }
 
         String baseUrl = sockJs.getBaseUrl(req.getUri());
         if (req.getUri().endsWith("/info")) {
             if (req.getMethod() == HttpMethod.GET) {
                 sendInfo(ctx, baseUrl);
             } else if (req.getMethod() == HttpMethod.OPTIONS) {
                 HttpHelpers.sendOptions(ctx, req, "OPTIONS, GET");
             }
             return;
         }
 
         if (req.getUri().matches(".*/iframe[\\d\\w\\.\\-_]*.html.*") && req.getMethod() == HttpMethod.GET) {
             HttpHelpers.sendIFrameHtml(ctx, req.getHeader(HttpHeaders.Names.IF_NONE_MATCH));
             return;
         }
 
         AbstractTransport transport = (AbstractTransport)sockJs.getTransport(getTransport(req.getUri()));
         if (transport == null) {
             HttpHelpers.sendError(ctx, HttpResponseStatus.NOT_FOUND);
             return;
         }
 
         String jsesssionId = null;
        if (sockJs.getEndpointInfo(baseUrl).isCookiesNeeded()) {
             jsesssionId = HttpHelpers.getJSESSIONID(req);
             jsesssionId = jsesssionId != null ? jsesssionId : "dummy";
         }
 
         String sessionId = getSessionId(req.getUri());
         SockJsHandlerContext sockJsHandlerContext = new SockJsHandlerContext();
         sockJsHandlerContext.setBaseUrl(baseUrl);
         sockJsHandlerContext.setSessionId(sessionId);
         sockJsHandlerContext.setConnection(sockJs.getConnectionForSession(sessionId));
         sockJsHandlerContext.setJSESSIONID(jsesssionId);
         ctx.getPipeline().replace(this, "handler", transport);
         ctx.getPipeline().getChannel().setAttachment(sockJsHandlerContext);
         ctx.getPipeline().sendUpstream(new UpstreamMessageEvent(ctx.getChannel(), req, ctx.getChannel().getRemoteAddress()));
     }
 
     private void handleFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
         AbstractTransport transport = (AbstractTransport)sockJs.getTransport(SockJs.WEBSOCKET_TRANSPORT);
         if (transport == null) {
             ctx.getChannel().close().addListener(ChannelFutureListener.CLOSE);
             return;
         }
 
         SockJsHandlerContext sockJsHandlerContext = (SockJsHandlerContext)ctx.getAttachment();
         ctx.getPipeline().replace(this, "handler", transport);
         ctx.getPipeline().getContext("handler").setAttachment(sockJsHandlerContext);
         ctx.getPipeline().sendUpstream(new UpstreamMessageEvent(ctx.getChannel(), frame, ctx.getChannel().getRemoteAddress()));
 
     }
 
     private void sendGreeting(ChannelHandlerContext ctx, HttpRequest req) {
         HttpResponse response = new DefaultHttpResponse(req.getProtocolVersion(), HttpResponseStatus.OK);
         response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
 
 
         if (HttpVersion.HTTP_1_1.equals(req.getProtocolVersion())) {
             response.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
         }
 
         response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, GREETING.length());
         response.setContent(ChannelBuffers.copiedBuffer(GREETING, CharsetUtil.UTF_8));
 
         ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
     }
 
     private void sendInfo(ChannelHandlerContext ctx, String baseUrl) {
         HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
         response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS, credentialAllowed);
         response.setHeader(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
         response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
         response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json;charset=UTF-8");
         String info = sockJs.getInfoAsString(baseUrl);
         response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, info.length());
         response.setContent(ChannelBuffers.copiedBuffer(info, CharsetUtil.UTF_8));
         ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
     }
 
     private String getTransport(String uri) {
         int lastSlashIndex = uri.lastIndexOf("/");
         if (lastSlashIndex > -1) {
             int paramQSignIndex = uri.indexOf("?", lastSlashIndex);
             return uri.substring(lastSlashIndex + 1, paramQSignIndex != -1 ? paramQSignIndex : uri.length());
         }
         return null;
     }
 
     private String getSessionId(String uri) {
         String[] parsedUri = uri.split("/");
         if (parsedUri.length > 0) {
             return parsedUri[parsedUri.length - 2];
         }
         return null;
     }
 }
