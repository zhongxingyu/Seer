 package info.liberitas.wss;
 
 import info.liberitas.wss.api.WebSocketServiceWrapper;
 import info.liberitas.wss.impl.GrizzlyWrapper;
 import info.liberitas.wss.impl.JettyWrapper;
 import org.apache.log4j.Logger;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Alexander <estliberitas> Makarenko
  * Date: 29.04.12
  * Time: 23:31
  *
  */
 
 /**
 * Wrapper creating class. Wrapper depends on usued HTTP-server
  *
  * @see info.liberitas.wss.api.WebSocketServiceWrapper
  */
 public class WrapperUtils {
 	private static final Logger logger = Logger.getLogger(WrapperUtils.class);
 
 	private static final String CLASS_JETTY = "org.eclipse.jetty.websocket.WebSocketFactory";
 	private static final String CLASS_GRIZZLY = "com.sun.grizzly.websockets.WebSocketEngine";
 
 	/**
 	 * Checks availability of classes of corresponding WebSocket implementation
 	 *
 	 * @param service Web-socket service instance
 	 * @return Wrapper instance used for delegating activity to the main, WebSocketService instance
 	 */
 	public static WebSocketServiceWrapper getServiceWrapper(WebSocketService service) {
 		try {
 			Class.forName(CLASS_JETTY);
 			return new JettyWrapper(service);
 		} catch (Exception e) {
 			logger.debug("Error creating wrapper instance", e);
 		}
 
 		try {
 			Class.forName(CLASS_GRIZZLY);
 			return new GrizzlyWrapper(service);
 		} catch (Exception e) {
 			logger.debug("Error creating wrapper instance", e);
 		}
 
 		return null;
 	}
 }
