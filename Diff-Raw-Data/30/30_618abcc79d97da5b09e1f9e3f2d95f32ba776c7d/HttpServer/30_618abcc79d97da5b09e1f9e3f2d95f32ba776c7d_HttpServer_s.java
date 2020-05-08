 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat.httpServer;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.math.BigDecimal;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.meerkat.group.AppGroupCollection;
 import org.meerkat.network.NetworkUtil;
 import org.meerkat.services.WebApp;
 import org.meerkat.util.DateUtil;
 import org.meerkat.util.FileUtil;
 import org.meerkat.util.PropertiesLoader;
 import org.meerkat.webapp.WebAppCollection;
 
 
 public class HttpServer {
 	private static Logger log = Logger.getLogger(HttpServer.class);
 	private DateUtil date = new DateUtil();
 	private String version;
 	private int webServerPort;
 	private NetworkUtil netUtil = new NetworkUtil();
 	//private FileUtil fu = new FileUtil();
 	private String tempWorkingDir;
 	private String propertiesFile = "meerkat.properties";
 
 	//private WebAppCollection wac;
 	private WebAppCollection wac;
 	private Properties prop;
 	boolean displayGroupGauge;
 
 	private static Boolean allowRemoteConfig, allowWebLogAccess;
 	private static String configFile = "meerkat.webapps.xml";
 	private static String logFile = "log/meerkat.log";
 	private static String webLogFile = "log.txt";
 	private String rssResource = "/rss.xml";
 	private String wsdlUrl = "";
 	//private String timeLineFile = "TimeLine.html";
 	private String hostname = netUtil.getHostname();
 	private CustomResourceHandler customResHandler;
 
 	private String footer = "";
 
 	private String bottomContent = "</tbody>\n</table>\n" + "</div>\n";
 	private String bodyEnd = "</body>\n" + "</html>\n";
 	private String indexContents = "";
 	private Server mServer;
 
 	private Thread indexRefresherThread = new Thread();
 
 	public HttpServer(final int webServerPort, String version, String wsdlUrl, String tempWorkingDir) {
 		this.webServerPort = webServerPort;
 		this.version = version;
 		this.setFooter(this.version);
 		this.wsdlUrl = wsdlUrl;
 		this.tempWorkingDir = tempWorkingDir;
 
 		// Create the index startup page
 		createStartupPage("Please wait while Meerkat-Monitor is getting ready to work....");
 
 		mServer = new Server(webServerPort);
 
 		// Handler for Meerkat-Monitor
 		ResourceHandler resourceHandler = new ResourceHandler();
 		resourceHandler.setDirectoriesListed(true);
 		resourceHandler.setWelcomeFiles(new String[] { "index.htm" });
 		resourceHandler.setResourceBase(tempWorkingDir);
 		
 		ServletContextHandler context0 = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		context0.setContextPath("/");
 		context0.setHandler(resourceHandler);
 		
 		// WebApp context for Web Client Admin  - if client war is available
 		WebAppContext webAppClientWar = embeddedWarClientAppContext();
 		ServletContextHandler context1 = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		context1.setContextPath("/admin");
 		context1.setHandler(webAppClientWar);
 
 		// Create the index file which redirects to dynamic response listening on "index.html"
 		String redirectCode = "<html>\n<head>\n<meta http-equiv=\"refresh\" content=\"0;url=index.html\">"+
 				"\n</head>\n<body>\n</body>\n</html>";
 		FileUtil fu = new FileUtil();
 		fu.writeToFile(tempWorkingDir+"/index.htm", redirectCode);
 
 		// Add custom resource handler
 		customResHandler = new CustomResourceHandler(this);
 
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		//contexts.setHandlers(new Handler[] { context0, context1, customResHandler });
		contexts.setHandlers(new Handler[] { context1});
		
		mServer.setHandler(contexts);
		
		/**
 		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, customResHandler });
 		mServer.setHandler(handlers);
		*/
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { context0, customResHandler});
		mServer.setHandler(handlers);
		
 
 		try {
 			mServer.start();
 		} catch (Exception e) {
 			log.fatal("Cannot start webserver!", e);
 			System.exit(-1);
 		}
 
 	}
 
 	/**
 	 * setDataSources
 	 * 
 	 * @param wac
 	 * @param agc
 	 */
 	public final void setDataSources(WebAppCollection wac, AppGroupCollection agc) {
 		this.wac = wac;
 		customResHandler.setWebAppCollection(wac);
 	}
 
 	/**
 	 * getTopContent
 	 * 
 	 * @return
 	 */
 	private String getTopContent(boolean showGauge, boolean showAppType) {
 		String topContent = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
 				+ "<html><head>\n"
 				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
 				+ "<meta http-equiv=\"refresh\" content=\"30\" />\n"
 				+ "<title>Meerkat Monitor</title>\n"
 				+ "<link rel=\"icon\"  href=\"/resources/favicon.ico\"  type=\"image/x-icon\" />\n"
 				+ "<link rel=\"alternate\" type=\"application/rss+xml\"  href=\""
 				+ rssResource
 				+ "\"> \n"
 				+ "<style type=\"text/css\" title=\"currentStyle\">\n"
 				+ "	@import \"resources/demo_page.css\";\n"
 				+ "	@import \"resources/demo_table_jui.css\";\n"
 				+ "	@import \"resources/jquery-ui-1.8.4.custom.css\";\n"
 				+ "</style>\n";
 
 		// Gauge		
 		if(showGauge){
 			topContent += "<script type='text/javascript' src='http://www.google.com/jsapi'></script>\n"
 					+ "<script type=\"text/javascript\">google.load('visualization', '1', {packages: ['gauge']});</script>\n"
 
 				+ "<script type=\"text/javascript\">\n"
 				+ "function drawVisualization() {\n"
 				+ "var options = {width: 1000, height: 120, redFrom: 0, redTo: 95,\n"
 				+ "               yellowFrom: 95, yellowTo: 98, greenFrom: 98, greenTo: 100, \n"
 				+ "				  minorTicks: 5};\n"
 				+ "var data = new google.visualization.DataTable();\n"
 
 				+ wac.getpAppGroupCollection().getAvailabilityGaugeData()
 				+ "new google.visualization.Gauge(document.getElementById('visualization')).\n"
 				+ "draw(data, options);\n"
 				+ " }\n"
 				+ "google.setOnLoadCallback(drawVisualization);\n"
 				+ "</script>\n";
 		}
 
 		topContent += "<script type=\"text/javascript\" language=\"javascript\" src=\"resources/jquery.js\"></script>\n"
 				+ "<script type=\"text/javascript\" language=\"javascript\" src=\"resources/jquery.dataTables.js\"></script>\n"
 
 				+ "\n<script type=\"text/javascript\" charset=\"utf-8\">\n"
 				+ "$(document).ready(function() {\n"
 				+ "	oTable = $('#example').dataTable({\n"
 				+ "		\"bJQueryUI\": true,\n"
 				+ "		\"sPaginationType\": \"full_numbers\",\n"
 				+ "		\"bStateSave\": true\n"
 				+ "});\n"
 				+ "} );\n"
 				+ "</script>\n"
 
 				+ "</head>\n"
 				+ "<body id=\"dt_example\">\n"
 				+ "<div id=\"container\">\n"
 
 				+ "<div class=\"full_width big\">\n"
 				+ "<a href=\"http://meerkat-monitor.org/?utm_source=application&utm_medium=dashboard&utm_campaign=dashlink\" style=\"text-decoration:none\" target=\"_blank\">\n"
 				+ "<img src=\"resources/meerkat-small.png\" alt=\"Meerkat Monitor Logo\" border=\"0\" align=\"absmiddle\" />"
 				+ "<img src=\"resources/meerkat.png\" alt=\"Meerkat Monitor\" border=\"0\" align=\"absmiddle\" /></a>\n"
 				+ "</div>\n";
 
 		// Gauge
 		if(showGauge){
 			topContent += "<div id=\"visualization\"></div>\n";
 		}
 
 		topContent += "<div class=\"full_width big\">\n"
 				/**
 				+ "<a href=\""
 				+ timeLineFile
 				+ "\"><img src=\"resources/tango_timeline.png\" alt=\"Timeline\" align=\"right\" style=\"border-style: none\"/></a>\n"
 				 */
 				+ "<a href=\""
 				+ rssResource
 				+ "\"><img src=\"resources/tango_rss.png\" alt=\"RSS\" align=\"right\" style=\"border-style: none\"/></a> \n"
 				+ "</div>\n"
 
 				+ "<div class=\"demo_jui\">\n"
 				+ "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"display\" id=\"example\">\n"
 				+ "<thead>\n"
 
 				+ "<tr>\n"
 				+ "	<th>Web Application</th>\n";
 
 		// Show Application type if active
 		if(showAppType){
 			topContent += "	<th>Type</th>\n";
 		}
 
 		topContent += "	<th>Availability (%)</th>\n"
 				+ "	<th>Crit. Events</th>\n"
 				+ "	<th>Av. Latency (ms)</th>\n"
 				+ "	<th>Av. Load Time (s)</th>\n"
 				+ "	<th>Status</th>\n"
 				+ "</tr>\n" + "</thead>\n" + "<tbody>\n";
 
 		return topContent;
 	}
 
 	/**
 	 * setFooter
 	 * 
 	 * @param version
 	 */
 	private void setFooter(String version) {
 		HTMLComponents htmlc = new HTMLComponents(version);
 		footer = htmlc.getFooter();
 	}
 
 
 	public final String getIndexPageContents(){
 		return indexContents;
 	}
 
 	/**
 	 * refresh
 	 */
 	public void refreshIndex() {
 		// Refresh index in new thread as this might take few moments
 		Runnable refresher = new Runnable(){
 			@Override
 			public void run() {
 				// Refresh the index
 				Iterator<WebApp> i = wac.getWebAppCollectionIterator();
 				WebApp wApp;
 
 				// Get properties
 				PropertiesLoader pl = new PropertiesLoader(propertiesFile);
 				prop = pl.getPropetiesFromFile();
 
 				// Check if application type before name is activated
 				boolean appTypePrefixEnabled = Boolean.parseBoolean(prop.getProperty("meerkat.webserver.showapptype"));
 				String appPrefix = "";
 
 				// Check if we should enable home groups gauge
 				String responseStatus;
 
 				displayGroupGauge = Boolean.parseBoolean(prop.getProperty("meerkat.dashboard.gauge"));
 				if (displayGroupGauge) {
 					responseStatus = getTopContent(displayGroupGauge, appTypePrefixEnabled);
 				} else {
 					responseStatus = getTopContent(false, appTypePrefixEnabled);
 				}
 
 				// Check if remote access to config is allowed
 				allowRemoteConfig = Boolean.parseBoolean(prop.getProperty("meerkat.webserver.rconfig"));
 
 				if (allowRemoteConfig) {
 					FileUtil fu = new FileUtil();
 					String configXml = fu.readFileContents(configFile);
 					fu.writeToFile(tempWorkingDir + configFile, configXml);
 				} else {
 					FileUtil fu = new FileUtil();
 					fu.writeToFile(tempWorkingDir + configFile,
 							"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
 									"<meerkat-monitor>" +
 									" <info>Remote access to config file is disabled!</info>" +
 							"</meerkat-monitor>");
 				}
 
 				// Check if web log access is allowed
 				allowWebLogAccess = Boolean.parseBoolean(prop.getProperty("meerkat.webserver.logaccess"));
 
 				if (allowWebLogAccess) {
 					FileUtil fu = new FileUtil();
 					String logFileContents = fu.readFileContents(logFile);
 					fu.writeToFile(tempWorkingDir + webLogFile, logFileContents);
 				} else {
 					FileUtil fu = new FileUtil();
 					fu.writeToFile(tempWorkingDir + webLogFile,
 							"Web access to log file is disabled!");
 				}
 
 				while (i.hasNext()) {
 					wApp = i.next();
 					if (wApp.isActive()) {
 						// using CSS grade so we can later set a grade for each application type
 						if (wApp.getType().equalsIgnoreCase(WebApp.TYPE_WEBAPP)) {
 							responseStatus += "\n<tr class=\"gradeA\">\n" + "<td>\n"
 									+ "<a href=\"" + "/" + wApp.getDataFileName()
 									+ "\">" + wApp.getName() + "</a>\n" + "</td>\n";
 
 						} else if (wApp.getType().equalsIgnoreCase(WebApp.TYPE_DATABASE)) {
 							responseStatus += "\n<tr class=\"gradeA\">\n" + "<td>\n"
 									+ "<a href=\"" + "/" + wApp.getDataFileName()
 									+ "\">" + wApp.getName() + "</a>\n" + "</td>\n";
 						}
 
 						else if (wApp.getType().equalsIgnoreCase(WebApp.TYPE_WEBSERVICE)) {
 							responseStatus += "\n<tr class=\"gradeA\">\n" + "<td>\n"
 									+ "<a href=\"" + "/" + wApp.getDataFileName()
 									+ "\">" + wApp.getName() + "</a>\n" + "</td>\n";
 						}
 
 						else if (wApp.getType().equalsIgnoreCase(WebApp.TYPE_SOCKET)) {
 							responseStatus += "\n<tr class=\"gradeA\">\n" + "<td>\n"
 									+ "<a href=\"" + "/" + wApp.getDataFileName()
 									+ "\">" + wApp.getName() + "</a>\n" + "</td>\n";
 						}
 
 						else if (wApp.getType().equalsIgnoreCase(WebApp.TYPE_SSH)) {
 							responseStatus += "\n<tr class=\"gradeA\">\n" + "<td>\n"
 									+ "<a href=\"" + "/" + wApp.getDataFileName()
 									+ "\">" + wApp.getName() + "</a>\n" + "</td>\n";
 						}
 
 						else {
 							responseStatus += "\n<tr class=\"gradeC\">\n" + "<td>\n"
 									+ "<a href=\"" + "/" + wApp.getDataFileName()
 									+ "\">" + wApp.getName() + "</a>\n" + "</td>\n";
 						}
 
 						/**
 						 * Show Application type if active
 						 */
 						if(appTypePrefixEnabled){
 							appPrefix = "<small>"+wApp.getType()+"</small>";
 							responseStatus += "<td class=\"center\">" + appPrefix;
 							responseStatus += "</td>\n";
 						}
 
 						/**
 						 * Availability
 						 */
 						responseStatus += "<td class=\"center\">" + wApp.getAvailability();
 
 						// Trend
 						double availIndicator = wApp.getAvailabilityIndicator();
 						if (wApp.getNumberOfTests() > 2) {
 							if (availIndicator > 0) {
 								responseStatus += "<img src=\"resources/up-green.png\" alt=\"Last value higher than average\" width=\"10\" height=\"10\"/>\n</td>\n";
 							} else if (availIndicator < 0) {
 								responseStatus += "<img src=\"resources/down-red.png\" alt=\"Last value lower than average\" width=\"10\" height=\"10\"/>\n</td>\n";
 							} else {
 								responseStatus += "</td>\n";
 							}
 						}
 
 						// Link to events
 						responseStatus += "<td class=\"center\">";
 						if (wApp.getNumberOfEvents() > 0) {
 							responseStatus += "<a href=\"" + "/" + wApp.getDataFileName()
 									+ "\">" + wApp.getNumberOfCriticalEvents()
 									+ "</a>\n";
 						} else {
 							responseStatus += "N/A";
 						}
 						responseStatus += "</td>\n";
 
 						/**
 						 * Latency
 						 */
 						responseStatus += "<td class=\"center\">\n";
 						BigDecimal bd = new BigDecimal(wApp.getLatencyAverage());
 						bd = bd.setScale(1, BigDecimal.ROUND_DOWN);
 						responseStatus += bd.doubleValue();
 						// trend
 						double latencyIndicator = wApp.getLatencyIndicator();
 						if (wApp.getNumberOfTests() > 2) {
 							// check for "undefined" values
 							if (latencyIndicator > 0) {
 								responseStatus += "<img src=\"resources/up-red.png\" alt=\"Last value higher than average\" width=\"10\" height=\"10\"/>\n</td>\n";
 							} else if (latencyIndicator < 0) {
 								responseStatus += "<img src=\"resources/down-green.png\" alt=\"Last value lower than average\" width=\"10\" height=\"10\"/>\n</td>\n";
 							} else {
 								responseStatus += "</td>\n";
 							}
 						}
 
 						/**
 						 * Load Time
 						 */
 						responseStatus += "<td class=\"center\">\n";
 						BigDecimal bd1 = new BigDecimal(wApp.getAppLoadTimeAVG());
 						bd1 = bd1.setScale(1, BigDecimal.ROUND_DOWN);
 						responseStatus += bd1.doubleValue();
 						// trend
 						double loadTimeIndicator = wApp.getLoadTimeIndicator();
 						if (wApp.getNumberOfTests() > 2) {
 							if (loadTimeIndicator > 0) {
 								responseStatus += "<img src=\"resources/up-red.png\" alt=\"Last value higher than average\" width=\"10\" height=\"10\"/>\n</td>\n";
 							} else if (loadTimeIndicator < 0) {
 								responseStatus += "<img src=\"resources/down-green.png\" alt=\"Last value lower than average\" width=\"10\" height=\"10\"/>\n</td>\n";
 							} else {
 								responseStatus += "</td>\n";
 							}
 						}
 
 						// Status
 						if (wApp.getlastStatus().equalsIgnoreCase("online")) {
 							responseStatus += "<td class=\"center\" style=\"background-color: #2d9500;\">\n";
 						} else if (wApp.getlastStatus().equalsIgnoreCase("offline")) {
 							responseStatus += "<td class=\"center\" style=\"background-color: #ff0000;\">\n";
 						} else {
 							responseStatus += "<td class=\"center\" style=\"background-color: #949494;\">\n";
 						}
 						// Link to URL only makes sense on Web pages
 						if (!wApp.getType().equals(WebApp.TYPE_WEBAPP)) {
 							responseStatus += "<strong>"
 									+ wApp.getlastStatus().toUpperCase(
 											Locale.getDefault())
 											+ "</strong></td>\n</tr>\n";
 						} else {
 							responseStatus += "<a href=\""
 									+ wApp.getUrl()
 									+ "\" target=\"_blank\"><strong>"
 									+ wApp.getlastStatus().toUpperCase(
 											Locale.getDefault())
 											+ "</strong></a></td>\n</tr>\n";
 						}
 
 					}
 				}
 
 				responseStatus += bottomContent;
 				responseStatus += "<a href=\""
 						+ wsdlUrl
 						+ "\"><img src=\"resources/tango_wsdl.png\" alt=\"Webservices WSDL\" align=\"right\" style=\"border-style: none\"/></a> \n";
 
 				// If remote config (webapps.xml) access is enable create link for it
 				if(allowRemoteConfig){
 					responseStatus += "<a href=\""
 							+ configFile
 							+ "\"><img src=\"resources/tango-xml-config.png\" alt=\"App Config XML\" align=\"right\" style=\"border-style: none\"/></a> \n";
 				}
 
 				// If log file access is enable create link for it
 				if(allowWebLogAccess){
 					responseStatus += "<a href=\""
 							+ webLogFile
 							+ "\"><img src=\"resources/tango-find-log.png\" alt=\"Log\" align=\"right\" style=\"border-style: none\"/></a> \n";
 				}
 
 
 				responseStatus += "<br>\n<div>\nUpdated: " + date.now() + " ["
 						+ wac.getNumberOfEventsInCollection() + " tests]" + "</div>\n";
 				responseStatus += "</div>"; // Close div
 				responseStatus += footer;
 				responseStatus += bodyEnd;
 
 				// Write the index file
 				//File f = new File(tempWorkingDir + "index.html");
 				//if (!f.delete()) {
 				//	log.warn("Failed to remove file: " + f.toString());
 				//}
 				//fu.writeToFile(tempWorkingDir + "index.html", responseStatus);
 				indexContents = responseStatus;
 			}
 		};
 
 		// Check if a indexRefresherThread is already running
 		// If so, do not launch another because it's not necessary
 		if(!indexRefresherThread.isAlive()){
 			indexRefresherThread = new Thread(refresher);
 			indexRefresherThread.start();
 		}
 	}
 
 	/**
 	 * createStartupPage
 	 */
 	public final void createStartupPage(String message) {
 		String pageContents = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
 				+ "<html>\n"
 				+ "<head>\n"
 				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><meta http-equiv=\"refresh\" content=\"5\"></meta>\n"
 				+ "<title>Meerkat Loading...</title>\n"
 				+ "<link rel=\"icon\"  href=\"/resources/faviconM.gif\"  type=\"image/x-icon\"></link>\n"
 				+ "</head>\n"
 				+ "<body>\n"
 				+ "<h2>"
 				+ message
 				+ "</h2>\n"
 				+ "<h3>Your browser will reload automatically when Meerkat-Monitor is ready.</h3>\n"
 				+ "</body>\n" + "</html>\n";
 
 		// Write the startup index file
 		/**
 		File f = new File(tempWorkingDir + "index.html");
 		if (f.exists() && !f.delete()) {
 			log.warn("Cannot remove " + f.toString());
 		}
 		fu.writeToFile(tempWorkingDir + "index.html", pageContents);
 		 */
 		indexContents = pageContents;
 	}
 
 	/**
 	 * getServerUrl
 	 * 
 	 * @return
 	 */
 	public final String getServerUrl() {
 		return "http://" + hostname + ":" + webServerPort + "/";
 	}
 
 
 	/**
 	 * Create context for web client war
 	 */
 	private final WebAppContext embeddedWarClientAppContext(){
 		WebAppContext webappClient = null;
 
 		File f = new File("./war");
 		FilenameFilter textFilter = new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				String lowercaseName = name.toLowerCase();
 				if (lowercaseName.endsWith(".war")) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		};
 
 		String warFileClient = "";
 		File[] files = f.listFiles(textFilter);
 		for (File file : files) {
 			if (!file.isDirectory()) { // Consider the first file found
 				warFileClient = file.getAbsolutePath();
 				break;
 			}
 		}
 
 		if(!warFileClient.equals("")){
 			webappClient = new WebAppContext();
 			//webappClient.setContextPath("/admin");
 			webappClient.setWar(warFileClient);
 		}
 
 		return webappClient;
 	}
 
 
 }
