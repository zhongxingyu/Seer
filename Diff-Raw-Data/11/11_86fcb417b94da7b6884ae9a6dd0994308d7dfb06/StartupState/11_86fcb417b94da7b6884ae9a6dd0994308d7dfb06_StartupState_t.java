 package ljas.server.state;
 
 import java.io.IOException;
 import java.net.URL;
 
 import ljas.application.Application;
 import ljas.application.ApplicationAnalyzer;
 import ljas.exception.ApplicationException;
 import ljas.server.Server;
 import ljas.server.configuration.Property;
 import ljas.server.configuration.ServerProperties;
 import ljas.tasking.environment.TaskSystemImpl;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 public class StartupState extends OfflineState {
 
 	private Server server;
 
 	public StartupState(Server server) {
 		this.server = server;
 	}
 
 	@Override
 	public void startup() throws ApplicationException, IOException {
 		ServerProperties serverProperties = server.getProperties();
 
 		// Logging
 		if (serverProperties.isSet(Property.LOG4J_PATH)) {
 			String path = serverProperties.get(Property.LOG4J_PATH).toString();
 			URL url = getClass().getClassLoader().getResource(path);
 			if (url != null) {
 				DOMConfigurator.configure(url);
 			}
 		} else {
 			BasicConfigurator.configure();
 		}
 
 		server.setTaskSystem(new TaskSystemImpl(server.getApplication()));
 
 		// Check application
 		ApplicationAnalyzer.validateApplication(server.getApplication()
 				.getClass());
 
 		logServerInfo(serverProperties);
 
 		getLogger().debug("Initializing application");
 		server.getApplication().init();
 
 		getLogger().debug("Getting internet connection, starting socket");
 		int port = Integer.valueOf(serverProperties.get(Property.PORT)
 				.toString());
 		server.getServerSocketBinder().bind(port, server);
 
 		getLogger().info(server + " has been started");
 	}
 
 	private void logServerInfo(ServerProperties serverProperties) {
 		Class<? extends Application> applicationClass = server.getApplication()
 				.getClass();
 
 		String applicationName = ApplicationAnalyzer
 				.getApplicationName(applicationClass);
 		String applicationVersion = ApplicationAnalyzer
 				.getApplicationVersion(applicationClass);
 
 		getLogger().info(
				"Starting " + server + " (v" + Server.SERVER_VERSION
 						+ ") with application " + applicationName + " ("
 						+ applicationVersion + ")");
 
 		getLogger().info(
 				"See \"" + Server.PROJECT_HOMEPAGE + "\" for more information");
 
 		getLogger().debug("Configuration: " + serverProperties);
 	}
 
 	private Logger getLogger() {
 		return server.getLogger();
 	}
 
 }
