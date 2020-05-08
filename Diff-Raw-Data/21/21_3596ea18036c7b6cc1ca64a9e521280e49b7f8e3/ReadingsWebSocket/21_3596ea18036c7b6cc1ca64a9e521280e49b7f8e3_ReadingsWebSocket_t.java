 package eu.uberdust.websockets.readings;
 
 import com.caucho.websocket.WebSocketServletRequest;
 import eu.uberdust.communication.websocket.WSIdentifiers;
 import eu.wisebed.wisedb.listeners.LastNodeReadingConsumer;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import javax.servlet.GenericServlet;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: akribopo
  * Date: 3/20/12
  * Time: 1:07 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ReadingsWebSocket extends GenericServlet implements Controller {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(ReadingsWebSocket.class);
 
     /**
      * Serial Version Unique ID.
      */
     private static final long serialVersionUID = -279704326229266519L;
 
     /**
      * Insert Reading Web Socket Listener.
      */
     private InsertReadingWSListener insertReadingWSListener;
     /**
      * A HashMap<CapabilityID:NodeID>.
      */
     private final transient Map<String, LastReadingWSListener> listeners;
 
     /**
      * Sets the web socket listener.
      *
      * @param insertReadingWSListener reading web socket listener.
      */
     public void setInsertReadingWSListener(final InsertReadingWSListener insertReadingWSListener) {
         this.insertReadingWSListener = insertReadingWSListener;
     }
 
     /**
      * Default Constructor.
      */
     public ReadingsWebSocket() {
         // empty constructor.
         listeners = new HashMap<String, LastReadingWSListener>();
         LOGGER.setLevel(Level.DEBUG);
     }
 
     /**
      * Handles the request.
      *
      * @param servletRequest  the servletRequest.
      * @param servletResponse the servletResponse.
      * @return servlet response.
      * @throws javax.servlet.ServletException ServletException exception.
      * @throws java.io.IOException            IOException exception.
      */
     public ModelAndView handleRequest(final HttpServletRequest servletRequest,
                                       final HttpServletResponse servletResponse) throws ServletException, IOException {
 
         servletRequest.getSession().setMaxInactiveInterval(Integer.MAX_VALUE);
 
         System.out.println("hanlde Request");
         //Process the handshake, selecting the protocol to be used.
         String protocol = servletRequest.getHeader("Sec-WebSocket-Protocol");
 
 
         if (protocol == null) {
             servletResponse.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
             return null;
         }
 
         protocol = protocol.replaceAll("\\.", "@").replaceAll("-", ":");
 
         if (protocol.equals(WSIdentifiers.INSERT_PROTOCOL)) {
            LOGGER.info("WSIdentifiers.INSERT_PROTOCOL");
 
             servletResponse.setHeader("Sec-WebSocket-Protocol", protocol);
 
            LOGGER.info(insertReadingWSListener == null);
             //Initialize Insert WebSocket Client
             final WebSocketServletRequest wsRequest = (WebSocketServletRequest) servletRequest;
             wsRequest.startWebSocket(insertReadingWSListener);
            LOGGER.info("WSIdentifiers.INSERT_PROTOCOL");
             return null;
         }
 
         if (protocol.startsWith(WSIdentifiers.SUBSCRIBE_PROTOCOL_PREFIX)) {
             System.out.println("WSIdentifiers.SUBSCRIBE_PROTOCOL_PREFIX");
             LastReadingWSListener lastReadingWSListener;
             if (listeners.containsKey(protocol)) {
                 servletResponse.setHeader("Sec-WebSocket-Protocol", protocol);
                 lastReadingWSListener = listeners.get(protocol);
                 LOGGER.info("registered listener");
 
             } else {
                 lastReadingWSListener = new LastReadingWSListener(
                         protocol.split(WSIdentifiers.DELIMITER)[1],
                         protocol.split(WSIdentifiers.DELIMITER)[2]);
 
                 LastNodeReadingConsumer.getInstance().registerListener(
                         protocol.split(WSIdentifiers.DELIMITER)[1],
                         protocol.split(WSIdentifiers.DELIMITER)[2],
                         lastReadingWSListener);
 
                 LOGGER.info("new listener");
                 listeners.put(protocol, lastReadingWSListener);
                 servletResponse.setHeader("Sec-WebSocket-Protocol", protocol);
             }
 
             //Initialize LastReading WebSocket Client
             final WebSocketServletRequest wsRequest = (WebSocketServletRequest) servletRequest;
             wsRequest.startWebSocket(lastReadingWSListener);
 
             return null;
         }
 
         servletResponse.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
         return null;
     }
 
 
     @Override
     public final void service(final ServletRequest servletRequest, final ServletResponse servletResponse) throws ServletException, IOException {
         LOGGER.info("service");
         System.out.println("Service");
         try {
             handleRequest((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
         } catch (Exception ex) {
             LOGGER.fatal(ex);
             ex.printStackTrace();
         }
     }
 }
 
