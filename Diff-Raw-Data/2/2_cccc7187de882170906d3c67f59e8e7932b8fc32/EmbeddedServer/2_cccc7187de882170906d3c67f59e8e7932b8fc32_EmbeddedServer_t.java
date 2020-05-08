 package org.syfsyf.warrunner;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.BindException;
 import java.net.URL;
 import java.security.ProtectionDomain;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 
 // based on : http://vbossica.blogspot.com/2011/12/creating-runnable-war-with-jetty-and.html
 
 public class EmbeddedServer {
 	@Parameter(names = "-port", description = "HTTP port of the server")
 	private Integer port = 8080;
 	@Parameter(names = "-contextPath", description = "context path of the embedded web application")
 	private String contextPath = "/";
 	@Parameter(names = "-maxIdleTime", description = "maxIdleTime (see documentation)")
 	private Integer maxIdleTime = 0;
 	@Parameter(names = "-soLingerTime", description = "soLingerTime (see documentation)")
 	private Integer soLingerTime = -1;
 	@Parameter(names = { "-help", "-?" }, description = "prints this message", hidden = true)
 	private Boolean help = false;
 
 	@Parameter(names = "-extractWar", description = "extract WAR archive")
 	private Boolean extractWar = false;
 
 	@Parameter(names = "-tray", description = "show tray menu")
 	private Boolean tray = true;
 
 	@Parameter(names = "-autoPort", description = "auto select firts free HTTP port for server")
 	private Boolean autoPort = true;
 
 	@Parameter(names = "-autoPortLower", description = "lower range for -autoPort")
 	private Integer autoPortLower = 8080;
 
 	@Parameter(names = "-autoPortUpper", description = "upper range for -autoPort")
 	private Integer autoPortUpper = 9090;
 
 	@Parameter(names = "-open", description = "auto open in browser")
 	private Boolean open = true;
 
 	@Parameter(names = "-url", description = "url to open in browser - default http://localhost:[port]/")
 	private String url = "";
 
 	@Parameter(names = "-props", description = "path to additional properties file.")
 	private String props = "WEB-INF/war-runner.properties";
 	
 	
 	Properties properties = new Properties();
 
 	Server server;
 	SocketConnector connector;
 
 	void showError(String message) {
 		JOptionPane.showMessageDialog(null, message, "Error",
 				JOptionPane.ERROR_MESSAGE, null);
 	}
 
 	void start() throws Exception {
 		loadProperties();
 		prepareServer();
 		startServer();
 		if (tray) {
 			showTrayMenu();
 		}
 		if (open) {
 			openInBrowser();
 		}
 	}
 
 	void stopServer() {
 		if (server != null && server.isStarted()) {
 			try {
 				server.stop();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				showError(e.getMessage());
 			}
 		}
 	}
 
 	void openInBrowser() {
 
 		String url = getUrlToOpen();
 		BrowserControl.openUrl(url);
 
 	}
 
 	String getUrlToOpen() {
 		if ("".equals(this.url)) {
 			return "http://localhost:" + connector.getPort() + "" + url;
 		} else {
 			return url;
 		}
 	}
 
 	private void showTrayMenu() {
 		// TODO Auto-generated method stub
 		TrayMenu menu = new TrayMenu(this);
 
 	}
 
 	private void prepareServer() throws IOException {
 
 		server = new Server();
 		connector = new SocketConnector();
 
 		connector.setMaxIdleTime(maxIdleTime);
 		connector.setSoLingerTime(soLingerTime);
 		connector.setPort(port);
 		server.setConnectors(new Connector[] { connector });
 
 		WebAppContext context = new WebAppContext();
 		context.setServer(server);
 		if (!contextPath.startsWith("/")) {
 			contextPath = "/" + contextPath;
 		}
 		context.setContextPath(contextPath);
 		context.setExtractWAR(extractWar);
 
 		ProtectionDomain protectionDomain = EmbeddedServer.class
 				.getProtectionDomain();
 		context.setWar(protectionDomain.getCodeSource().getLocation()
 				.toExternalForm());
 
 		server.setHandler(context);
 
 	}
 
 	void startServer() throws Exception {
 
 		if (autoPort) {
			for (int p = autoPortLower; p < autoPortUpper; p++) {
 				connector.setPort(p);
 				try {
 					server.start();
 					return;
 				} catch (BindException bindEx) {
 					if ("Address already in use: JVM_Bind".equals(bindEx
 							.getMessage())) {
 						continue;
 					}
 					throw new RuntimeException(bindEx);
 				}
 			}
 		} else {
 			server.start();
 		}
 
 	}
 
 	private void loadProperties() throws IOException {
 
 		InputStream propsStream = EmbeddedServer.class.getClassLoader()
 				.getResourceAsStream(props);
 
 		properties.put("app.icon", "app-icon.png");
 		
 		if (propsStream == null) {
 			// throw new RuntimeException("file not found:"+WARTORUNPROPS);
 			return; // use defaults settings
 		}
 		properties.load(propsStream); propsStream.close();
 
 		/*
 		 * 
 		 * properties.load(propsStream); propsStream.close();
 		 * 
 		 * warDir = properties.getProperty("war.dir", warDir); if
 		 * (properties.contains("port")) { port =
 		 * Integer.valueOf(properties.getProperty("port")); } if
 		 * (properties.contains("extract.war")) { extractWar =
 		 * Boolean.valueOf(properties.getProperty("extract.war")); } if
 		 * (properties.contains("auto.select.port")) { autoSelectPort =
 		 * Boolean.valueOf(properties.getProperty("auto.select.port")); } if
 		 * (properties.contains("open.in.browser")) { openInBrowser =
 		 * Boolean.valueOf(properties.getProperty("open.in.browser")); } openUrl
 		 * = properties.getProperty("open.url", openUrl);
 		 */
 	}
 
 	/*
 	 * private void run() { Server server = new Server(); SocketConnector
 	 * connector = new SocketConnector();
 	 * 
 	 * connector.setMaxIdleTime(maxIdleTime);
 	 * connector.setSoLingerTime(soLingerTime); connector.setPort(port);
 	 * server.setConnectors(new Connector[] { connector });
 	 * 
 	 * WebAppContext context = new WebAppContext(); context.setServer(server);
 	 * if (!contextPath.startsWith("/")) { contextPath = "/" + contextPath; }
 	 * context.setContextPath(contextPath); context.setExtractWAR(extractWar);
 	 * 
 	 * ProtectionDomain protectionDomain =
 	 * EmbeddedServer.class.getProtectionDomain();
 	 * context.setWar(protectionDomain
 	 * .getCodeSource().getLocation().toExternalForm());
 	 * 
 	 * server.setHandler(context); try { server.start(); System.in.read();
 	 * server.stop(); server.join(); } catch (Exception ex) {
 	 * ex.printStackTrace(); System.exit(100); } }
 	 */
 
 	public static void main(String[] args) throws Exception {
 		EmbeddedServer server = new EmbeddedServer();
 		JCommander commander = new JCommander(server, args);
 		if (server.help) {
 			commander.usage();
 		} else {
 			server.start();
 		}
 	}
 }
