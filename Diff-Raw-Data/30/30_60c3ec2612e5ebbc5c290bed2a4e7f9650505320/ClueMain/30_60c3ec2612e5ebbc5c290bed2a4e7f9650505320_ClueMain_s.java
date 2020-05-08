 package main;
 
 import main.input.ConsoleControl;
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
