 package com.fbudassi.neddy.benchmark.benchmarks;
 
 import com.fbudassi.neddy.benchmark.NeddyBenchmark;
 import com.fbudassi.neddy.benchmark.bean.ListenerActionBean;
 import com.fbudassi.neddy.benchmark.bean.ListenerActionBean.ListenerActionEnum;
 import com.fbudassi.neddy.benchmark.bean.NeddyBean;
 import com.fbudassi.neddy.benchmark.bean.NeddyBean.ReasonEnum;
 import com.fbudassi.neddy.benchmark.config.Config;
 import com.fbudassi.neddy.benchmark.pipeline.WebsocketPipelineFactory;
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.Random;
 import org.apache.commons.lang3.StringUtils;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Websocket benchmark. Please go to the properties file for configurable
  * parameters.
  *
  * @author fbudassi
  */
 public class WebsocketBenchmark implements Benchmark {
 
     private static final Logger logger = LoggerFactory.getLogger(WebsocketBenchmark.class);
     private static Gson gson = new Gson();
     private static Random random = new Random();
     // Configuration constants.
     private static final int SERVER_PORT = Config.getIntValue(Config.KEY_SERVER_PORT);
     private static final String SERVER_ADDRESS = Config.getValue(Config.KEY_SERVER_ADDRESS);
     private static final int NUMCATEGORIES = Config.getIntValue(Config.KEY_LISTENER_NUMCATEGORIES);
     private static final String RESOURCE_LISTENER = Config.getValue(Config.KEY_RESOURCE_LISTENER);
     // Client configuration variables.
     private static final int NUMADDRESSES = Config.getIntValue(Config.KEY_NUMADDRESSES);
     private static final int NUMPORTS = Config.getIntValue(Config.KEY_NUMPORTS);
     private static final int CLIENT_PORTSTART = Config.getIntValue(Config.KEY_CLIENT_PORTSTART);
     private static final String CLIENT_BASEADDRESS = Config.getValue(Config.KEY_CLIENT_BASEADDRESS);
     // URI where to connect the websocket.
     private static URI uri;
     // Statistic variables.
     private static int totalConnections = NUMADDRESSES * NUMPORTS;
     private static int openConnections = 0;
 
     /**
      * Websocket Benchmark constructor.
      */
     public WebsocketBenchmark() throws URISyntaxException {
         // URL of the server, with the resource path
         uri = new URI("ws://" + SERVER_ADDRESS + ":" + SERVER_PORT + "/" + RESOURCE_LISTENER);
     }
 
     /**
      * Executes the benchmark.
      */
     @Override
     public void execute() throws Exception {
         logger.info("Trying to generate {} connections to the server", totalConnections);
 
         // Get the first three octets by one side and the last one by the other side.
         String clientIpBase = CLIENT_BASEADDRESS.substring(0, CLIENT_BASEADDRESS.lastIndexOf(".") + 1);
         byte clientIpLastOctet = Byte.parseByte(CLIENT_BASEADDRESS.substring(
                 CLIENT_BASEADDRESS.lastIndexOf(".") + 1, CLIENT_BASEADDRESS.length()));
 
         //IP addresses loop
         String clientIp;
         for (int i = 0; i < NUMADDRESSES; i++) {
             // Build client ip.
             clientIp = clientIpBase + clientIpLastOctet;
 
             //Ports loop
             int lastPort = CLIENT_PORTSTART + NUMPORTS;
             for (int port = CLIENT_PORTSTART; port <= lastPort; port++) {
                 // Open a Websocket channel to the server.
                 ChannelFuture future = NeddyBenchmark.getBootstrap().connect(
                         new InetSocketAddress(uri.getHost(), uri.getPort()),
                         new InetSocketAddress(clientIp, port));
                 future.syncUninterruptibly();
                 Channel ch = future.getChannel();
                 NeddyBenchmark.getAllChannels().add(ch);
 
                 // Start with the handshake step. Connect with V13 (RFC 6455 aka HyBi-17).
                 WebSocketClientHandshaker handshaker = new WebSocketClientHandshakerFactory().newHandshaker(
                         getUri(), WebSocketVersion.V13, null, false, null);
                 ch.setAttachment(handshaker);
                 handshaker.handshake(ch).syncUninterruptibly();
 
                 // Increment open connections variable and print the number of listeners once in a while.
                 openConnections++;
                 if ((((double) openConnections * 100 / totalConnections) % 1) == 0) {
                     logger.info("There are {} opened listeners  so far.", openConnections);
                 }
             }
         }
     }
 
     /**
      * Returns the pipeline for Websocket benchmark.
      *
      * @return
      */
     @Override
     public ChannelPipelineFactory getPipeline() {
         return new WebsocketPipelineFactory();
     }
 
     /**
      * Configure the Netty bootstrap for the best behavior in this benchmark.
      *
      * @param bootstrap
      */
     @Override
     public void configureBootstrap(ClientBootstrap bootstrap) {
         // Nothing is necessary to be done for the Websocket benchmark.
     }
 
     /**
      * @return the uri
      */
     public static URI getUri() {
         return uri;
     }
 
     /**
      * Request the categories to the server.
      *
      * @param ch
      */
     public static void getCategories(Channel ch) {
         ListenerActionBean listenerActionBean = new ListenerActionBean();
         listenerActionBean.setAction(ListenerActionEnum.GET_CATEGORIES.toString());
         ch.write(new TextWebSocketFrame(gson.toJson(listenerActionBean)));
     }
 
     /**
      * Subscribes the channel to a number of categories in the list, randomly
      * choosing among them.
      *
      * @param ch
      * @param categories
      */
     private static void subscribeToCategories(Channel ch, List<String> categories) {
         for (int n = 0; n < NUMCATEGORIES; n++) {
             ListenerActionBean listenerActionBean = new ListenerActionBean();
             listenerActionBean.setAction(ListenerActionEnum.SUBSCRIBE.toString());
             listenerActionBean.setCategory(categories.get(random.nextInt(categories.size())));
             ch.write(new TextWebSocketFrame(gson.toJson(listenerActionBean)));
         }
     }
 
     /**
      * It processes the frame content of a Websocket.
      *
      * @param frameContent
      */
     public static void processFrame(Channel ch, String frameContent) {
         try {
             // Check if frame payload is empty.
             if (StringUtils.isBlank(frameContent)) {
                 logger.error("Response payload is not valid: {}", frameContent);
                 return;
             }
 
             // Deserialize payload
             NeddyBean neddyBean = gson.fromJson(frameContent, NeddyBean.class);
 
             // Get valid message reason.
             ReasonEnum reason;
             try {
                 if (StringUtils.isBlank(neddyBean.getReason())) {
                     logger.error("Request action is blank.");
                     return;
                 }
                 reason = ReasonEnum.valueOf(neddyBean.getReason());
             } catch (IllegalArgumentException iaex) {
                 // Invalid action.
                 logger.error("Invalid reason received.");
                 return;
             }
 
             // Process the different reason messages from Neddy.
             switch (reason) {
                 case MESSAGE_NEW:
                     logger.debug("Message received from {}: {}", neddyBean.getCategory(), neddyBean.getMessage());
                     break;
                 case MESSAGE_CATEGORY_LIST:
                     List<String> categories = gson.fromJson(neddyBean.getMessage(), List.class);
                     subscribeToCategories(ch, categories);
                     break;
                 default:
                     logger.debug("Reason not recognized: {}", reason);
             }
         } catch (JsonSyntaxException jse) {
             logger.error("Neddy payload can't be deserialized properly.", jse);
         }
     }
 }
