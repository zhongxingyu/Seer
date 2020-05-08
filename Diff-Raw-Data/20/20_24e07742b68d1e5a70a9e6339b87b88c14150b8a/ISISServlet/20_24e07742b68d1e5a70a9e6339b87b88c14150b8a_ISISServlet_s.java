 package webSock;
 
 import java.util.logging.Level;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 
 import com.sun.grizzly.websockets.WebSocketEngine;
 
 /**
  * Servlet for the ISIS application
  * 
  * loadOnStartup=1 ensures that the servlet gets started as soon as the server gets deployed
  * And as soon as the server deploys, it will register the ISISServerApplication with the
  * WebSocketEngine of GlassFish
  * 
  * (Hook into Glassfish)
  * 
  * @author Dwight
  * 
  */
 @WebServlet(name="ISISServlet", urlPatterns="/ISISServlet", loadOnStartup=1)
 public class ISISServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * The application (WebSocketListener) registered with this servlet
 	 */
 	private ISISServerApplication app;
 
 	/**
 	 * Method to initialize the servlet
 	 * When initializing, register the ISISServerApplication with the WebSocketEngine of Grizzly
 	 */
 	public void init() {
 		try {
 			super.init();
 		} catch (ServletException e) {
 			e.printStackTrace();
 		}
 		
 		app = new ISISServerApplication();
 		
 		// Resister the ISIS application with the (Grizzly's) WebSocket Engine
 		WebSocketEngine.getEngine().register(app);
 		
 		// Create log message
 		java.util.logging.Logger.getAnonymousLogger().log(
 				Level.INFO, this.getServletName() + "Time: " + new java.util.Date() + ", " + 
 				"initialised servlet and registered ISIS application");
 	}
 	
 	/**
 	 * Method to destroy the servlet
 	 * When destroyed, unregister the ISISServerApplication from the WebSocketEngine of Grizzly
 	 */
 	public void destroy() {
 		super.destroy();
 		
 		// Unregister the application from the (Grizzly's) WebSocket Engine
 		WebSocketEngine.getEngine().unregister(app);
 		
 		// Create log message
 		java.util.logging.Logger.getAnonymousLogger().log(
 				Level.INFO, "Time: " + new java.util.Date() + ", " + 
 						this.getServletName() + " servlet: destroyed");
 		
 		// Terminate the current JVM to ensure threads get killed
		//System.exit(0);
 	}
 }
