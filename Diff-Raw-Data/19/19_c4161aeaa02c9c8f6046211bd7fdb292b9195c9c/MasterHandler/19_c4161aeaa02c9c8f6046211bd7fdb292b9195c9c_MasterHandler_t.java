 /**
  * $$\\ToureNPlaner\\$$
  */
 
 package server;
 
 import computecore.ComputeCore;
 import config.ConfigManager;
 import database.DatabaseManager;
 import database.DatabasePool;
 import org.jboss.netty.channel.*;
 import org.jboss.netty.handler.codec.http.*;
 import org.jboss.netty.util.CharsetUtil;
 
 import java.beans.PropertyVetoException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
 import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
 
 /**
  * This handler handles HTTP Requests on the normal operation socket including *
  *
  * @author Niklas Schnelle, Peter Vollmer, Sascha Meusel
  * @version 0.1
  *          <p/>
  *          Initially based on: http://docs.jboss.org/netty/3.2/xref
  *          /org/jboss/netty/example/http/snoop/package-summary.html
  */
 public class MasterHandler extends SimpleChannelUpstreamHandler {
 
     private static Logger log = Logger.getLogger("server");
 
     private boolean isPrivate;
 
     private Responder responder;
 
     private Authorizer authorizer;
 
     private AlgorithmHandler algHandler;
 
     private PrivateHandler privateHandler;
 
     private InfoHandler infoHandler;
     
     private DatabaseManager dbm;
 
     /**
      * Constructs a new RequestHandler using the given ComputeCore and
      * ServerInfo
      *
      * @param cCore ComputeCore
      * @param serverInfo String-Object-Map
      */
     public MasterHandler(final ComputeCore cCore, final Map<String, Object> serverInfo) {
         super();
         final ConfigManager cm = ConfigManager.getInstance();
         this.isPrivate = cm.getEntryBool("private", false);
         this.dbm = null;
         authorizer = null;
         if (isPrivate){
             try {
                 this.dbm = DatabasePool.getDatabaseManager(
                         cm.getEntryString("dburi", "jdbc:mysql://localhost:3306/tourenplaner?autoReconnect=true"),
                         cm.getEntryString("dbuser", "tnpuser"),
                         cm.getEntryString("dbpw", "toureNPlaner"),
                         cm.getEntryString("dbdriverclass", "com.mysql.jdbc.Driver"));
 
                 authorizer = new Authorizer(this.dbm);
             } catch (PropertyVetoException e) {
                 // TODO falling back to public mode is maybe not a good behavior
                 log.log(Level.SEVERE, "Can't connect to database falling back to public mode", e);
                 this.isPrivate = false;
             }
         }
         this.algHandler = new AlgorithmHandler(authorizer, this.isPrivate, this.dbm, cCore);
         this.privateHandler = new PrivateHandler(authorizer, this.dbm);
         this.infoHandler = new InfoHandler(serverInfo);
     }
 
 
     @Override
     public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
         responder = new Responder(e.getChannel());
         if(authorizer != null){
             authorizer.setResponder(responder);
         }
         algHandler.setResponder(responder);
         privateHandler.setResponder(responder);
         infoHandler.setResponder(responder);
     }
 
 
     /**
      * Called when a message is received
      */
     @Override
     public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
 
         final HttpRequest request = (HttpRequest) e.getMessage();
         final Channel channel = e.getChannel();
         // System.out.print(request.toString());
         // Handle preflighted requests so wee need to work with OPTION Requests
         if (request.getMethod().equals(HttpMethod.OPTIONS)) {
             handlePreflights(request, channel);
             return;
         }
 
         // Get the Requeststring e.g. /info
         final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
 
         final String path = queryStringDecoder.getPath();
 
         // DEBUG
 
         log.fine("Request for: " + path);
         log.finer("Request: " + request.getContent().toString(CharsetUtil.UTF_8));
 
         responder.setKeepAlive(isKeepAlive(request));
 
         try {
             if ("/info".equals(path)) {
 
                 infoHandler.handleInfo(request);
 
             } else if (path.startsWith("/alg")) {
 
                 final String algName = queryStringDecoder.getPath().substring(4);
                 algHandler.handleAlg(request, algName);
 
             } else if (isPrivate && "/registeruser".equals(path)) {
 
                 privateHandler.handleRegisterUser(request);
 
             } else if (isPrivate && "/authuser".equals(path)) {
 
                 privateHandler.handleAuthUser(request);
 
             } else if (isPrivate && "/getuser".equals(path)) {
 
                 privateHandler.handleGetUser(request, queryStringDecoder.getParameters());
 
             } else if (isPrivate && "/updateuser".equals(path)) {
 
                 privateHandler.handleUpdateUser(request, queryStringDecoder.getParameters());
 
             } else if (isPrivate && "/listrequests".equals(path)) {
 
                 privateHandler.handleListRequests(request, queryStringDecoder.getParameters());
 
             } else if (isPrivate && "/getrequest".equals(path)) {
 
                 privateHandler.handleGetRequest(request, queryStringDecoder.getParameters());
 
             } else if (isPrivate && "/getresponse".equals(path)) {
 
                 privateHandler.handleGetResponse(request, queryStringDecoder.getParameters());
 
             } else if (isPrivate && "/listusers".equals(path)) {
 
                 privateHandler.handleListUsers(request, queryStringDecoder.getParameters());
 
             } else if (isPrivate && "/deleteuser".equals(path)) {
 
                 privateHandler.handleDeleteUser(request, queryStringDecoder.getParameters());
 
             } else {
                 // Unknown request, close connection
                 log.warning("An unknown URL was requested: " + path);
                 responder.writeErrorMessage("EUNKNOWNURL", "An unknown URL was requested", "unknown URL: " + path, HttpResponseStatus.NOT_FOUND);
             }
         } catch (SQLException exSQL) {
             responder.writeErrorMessage("EDATABASE", "The server can't contact it's database", null, HttpResponseStatus.NOT_FOUND);
             exSQL.printStackTrace();
         }
     }
 
     /**
      * Handles preflighted OPTION Headers
      *
      * @param request HttpRequest
      * @param channel Channel
      */
     private void handlePreflights(final HttpRequest request, final Channel channel) {
         boolean keepAlive = isKeepAlive(request);
         HttpResponse response;
 
         // We only allow POST and GET methods so only allow request when Method
         // is Post or Get
         final String methodType = request.getHeader("Access-Control-Request-Method");
         if ((methodType != null) && (methodType.trim().equals("POST") || methodType.trim().equals("GET"))) {
             response = new DefaultHttpResponse(HTTP_1_1, OK);
             response.addHeader("Connection", "Keep-Alive");
         } else {
             response = new DefaultHttpResponse(HTTP_1_1, FORBIDDEN);
             // We don't want to keep the connection now
             keepAlive = false;
         }
 
         final ArrayList<String> allowHeaders = new ArrayList<String>(2);
         allowHeaders.add("Content-Type");
         allowHeaders.add("Authorization");
 
         response.setHeader("Access-Control-Allow-Origin", "*");
         response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
         response.setHeader(CONTENT_TYPE, "application/json");
         response.setHeader("Content-Length", "0");
 
         response.setHeader("Access-Control-Allow-Headers", allowHeaders);
 
         final ChannelFuture future = channel.write(response);
         if (!keepAlive) {
             future.addListener(ChannelFutureListener.CLOSE);
         }
 
     }
 
 
     /**
      * Called when an uncaught exception occurs
      */
     @Override
     public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
         // Ignore if it's just a client cutting the connection
         if(!(e.getCause() instanceof IOException)){
             log.log(Level.WARNING, "Exception caught", e.getCause());
         }
 
         e.getChannel().close();
     }
 }
