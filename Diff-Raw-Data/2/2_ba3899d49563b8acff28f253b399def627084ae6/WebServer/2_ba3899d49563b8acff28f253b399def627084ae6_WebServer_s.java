 package no.f12.jzx.weboo.server;
 
 import java.io.File;
 
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
 		Assert.isTrue(webAppContextPath.exists(), "The context path you have specified does not exist");
		Assert.notNull(applicationContext, "You must specify the context path of the application");
 
 		int startPort = 0;
 		if (this.port != null) {
 			startPort = this.port;
 		}
 
 		if (!applicationContext.startsWith("/")) {
 			applicationContext = "/" + applicationContext;
 		}
 
 		Server server = new Server(startPort);
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
 
 }
