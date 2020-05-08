 package main;
 
import java.util.logging.Handler;
import java.util.logging.Logger;

 import main.input.ConsoleControl;

import org.slf4j.bridge.SLF4JBridgeHandler;

 import server.ClueServer;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import configuration.ConfigurationLoader;
 import dependency.injection.RestModule;
 
 /**
  * See {@link RestModule} for port and server configuration
  * 
  * @author matt
  * 
  */
 public final class ClueMain {
 	public static void main(final String args[]) {
		// Remote the initial Restlet logger
		final java.util.logging.Logger rootLogger = Logger.getLogger("");
		final Handler[] handlers = rootLogger.getHandlers();
		rootLogger.removeHandler(handlers[0]);

		// Install the SLF4J which will convert restlets logging to log4j
		SLF4JBridgeHandler.install();

 		// Gets the injector for our context
 		final Injector injector = Guice.createInjector(new RestModule(ConfigurationLoader.getConfiguration()));
 
 		// Gets an instance of the server from the injector
 		injector.getInstance(ClueServer.class).start();
 
 		// Start the listener for console events
 		// Typing "quit" followed by enter into the console will stop the server
 		injector.getInstance(ConsoleControl.class).startListening();
 	}
 
 	private ClueMain() {
 	}
 }
