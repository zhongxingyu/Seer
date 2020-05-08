 package no.f12.jzx.weboo.server;
 
 import java.io.File;
 import java.net.InetSocketAddress;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.springframework.util.Assert;
 
 public class WebServer {
 
 	private Integer port;
 	private Server server;
 
 	public WebServer(int port) {
 		this.port = port;
 	}
 
 	public WebServer() {
 	}
 
 	public void start(File webAppContextPath, String applicationContext) {
 		server = startWebServer(webAppContextPath, applicationContext);
 		port = getServerPort(server);
 	}
 
 	private Integer getServerPort(Server server) {
 		return server.getConnectors()[0].getLocalPort();
 	}
 
 	private Server startWebServer(File webAppContextPath, String applicationContext) {
 		Assert.isTrue(webAppContextPath.exists(), "The context path you have specified does not exist: "
 				+ webAppContextPath);
 		Assert.notNull(applicationContext, "You must specify the context path of the application");
 
 		int startPort = 0;
 		if (this.port != null) {
 			startPort = this.port;
 		}
 
 		if (!applicationContext.startsWith("/")) {
 			applicationContext = "/" + applicationContext;
 		}
 
 		Server server = createServer(startPort);
 		try {
 			WebAppContext webAppContext = new WebAppContext(webAppContextPath.getCanonicalPath(), applicationContext);
 			setUpClassPath(webAppContext);
 			server.setHandler(webAppContext);
 			server.start();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return server;
 	}
 
 	private Server createServer(int startPort) {
 		/* OpenShift requires that we bind to an internal IP, this is not the
 		 * case most other places and I guess that Heroku avoids this by handing
 		 * us a specific port. On OpenShift the port is always 8080.
 		 */
 		if (System.getenv("OPENSHIFT_INTERNAL_IP") != null) {
 			return new Server(new InetSocketAddress(System.getenv("OPENSHIFT_INTERNAL_IP"), port));
 		} else {
 			return new Server(startPort);
 		}
 	}
 
 	private void setUpClassPath(WebAppContext webAppContext) {
 		String classpath = System.getProperty("java.class.path");
 		String separator = System.getProperty("path.separator");
 		if (":".equals(separator)) {
 			classpath = classpath.replace(":", ";");
 		}
 		webAppContext.setExtraClasspath(classpath);
 	}
 
 	public Integer getPort() {
 		Assert.notNull(port, "Server must be started before port can be determined");
 		return this.port;
 	}
 
 	public void stop() {
 		try {
 			server.stop();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		int port = determineServerPort();
 		File contextPath = determineContextPath();
 
 		WebServer setupServer = new WebServer(port);
 		setupServer.start(contextPath, "");
 	}
 
 	private static File determineContextPath() {
 		File contextPath = new File("./src/main/webapp");
 		File herokuPath = new File("./weboo-webapp/src/main/webapp");
 
 		if (herokuPath.exists()) {
 			return herokuPath;
 		}
 		return contextPath;
 	}
 
 	private static int determineServerPort() {
 		// Ports to check, most specific first. VCAP is Cloud Foundry, PORT is
 		// Heroku
 		String[] portNames = new String[] { "VCAP_APP_PORT", "PORT" };
 		for (String portName : portNames) {
 			String portValue = System.getenv(portName);
			if (portValue != null) {
 				return Integer.valueOf(portValue);
 			}
 		}
 
 		// If nothing found, default to 8080
 		return 8080;
 	}
 
 }
