 //=============================================================================
 //===	Copyright (C) 2010-2011 BRGM
 //===
 //===	This library is free software; you can redistribute it and/or
 //===	modify it under the terms of the GNU Lesser General Public
 //===	License as published by the Free Software Foundation; either
 //===	version 2.1 of the License, or (at your option) any later version.
 //===
 //===	This library is distributed in the hope that it will be useful,
 //===	but WITHOUT ANY WARRANTY; without even the implied warranty of
 //===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 //===	Lesser General Public License for more details.
 //===
 //===	You should have received a copy of the GNU Lesser General Public
 //===	License along with this library; if not, write to the Free Software
 //===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 //===
 //===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 //===	Rome - Italy. email: GeoNetwork@fao.org
 //==============================================================================
 
 package jeevlet;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import jeevlet.constants.Config;
 import jeevlet.utils.ConfigUtils;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.restlet.Component;
 import org.restlet.Server;
 import org.restlet.data.Parameter;
 import org.restlet.data.Protocol;
 import org.restlet.routing.VirtualHost;
 import org.restlet.util.Series;
 
 public class StarterAuto {
 
 	private static final String JEEVLET_STARTER = "jeevlet.starter";
 
 	/**
 	 * Run the example as a standalone component.
 	 * 
 	 * @param args
 	 *            The optional arguments.
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		String configPath = args.length > 0 ? args[0] : "./";
 		String propertyFilePath = configPath + "/jeevlet.properties";
 		PropertyConfigurator.configure(configPath + "/log4j.cfg");
 
 		// Get configuration
 		int appPort = Integer.parseInt(ConfigUtils.getProps(propertyFilePath).getProperty(Config.APP_PORT));
 		int listenPort = Integer.parseInt(ConfigUtils.getProps(propertyFilePath).getProperty(Config.LISTEN_PORT));
 		String maxTotalConnections = ConfigUtils.getProps(propertyFilePath).getProperty(Config.MAX_TOTAL_CONNECTIONS);
 		String maxThreads = ConfigUtils.getProps(propertyFilePath).getProperty(Config.MAX_THREADS);
 
 		// FIXME relatif path or automatic current path ...
 		String appPath = ConfigUtils.getProps(propertyFilePath).getProperty(Config.APP_PATH);
 		String hostDomain = ConfigUtils.getProps(propertyFilePath).getProperty(Config.HOST_DOMAIN);
 		String nodePrefix = ConfigUtils.getProps(propertyFilePath).getProperty(Config.NODE_PREFIX);
 
 		Logger.getLogger(JEEVLET_STARTER).debug("====== Jeevlet apps starting ================");
 		Logger.getLogger(JEEVLET_STARTER).debug("Host domain: " + hostDomain);
 		Logger.getLogger(JEEVLET_STARTER).debug("Host port: " + appPort);
 		Logger.getLogger(JEEVLET_STARTER).debug("Host stop port: " + listenPort);
 		Logger.getLogger(JEEVLET_STARTER).debug("Max total connections: " + maxTotalConnections);
 		Logger.getLogger(JEEVLET_STARTER).debug("Max threads: " + maxThreads);
 		Logger.getLogger(JEEVLET_STARTER).debug("Apps path: " + appPath);
 		Logger.getLogger(JEEVLET_STARTER).debug("Apps node prefix: " + nodePrefix);
 		
 		// Create a component
 		Component component = new Component();
 		Server httpServer = new Server(Protocol.HTTP, appPort);
 		component.getServers().add(httpServer);
 		component.getServers().add(Protocol.HTTP, listenPort);
 		component.getClients().add(Protocol.FILE);
 
 		// Set up parameters for httpServer
 		// See http://www.restlet.org/documentation/2.0/jse/engine/index.html?org/restlet/engine/http/connector/BaseServerHelper.html
 		httpServer.getContext().getParameters().add("maxTotalConnections", maxTotalConnections);
 		httpServer.getContext().getParameters().add("maxThreads", maxThreads);
 
 		File dir = new File(appPath).getCanonicalFile();
 		String[] nodeList = dir.list(new FilenameFilter() {
 
 			// @Override
 			public boolean accept(File dir, String name) {
 				return name.startsWith("WEB-INF-");
 			}
 		});
 
 		Logger.getLogger(JEEVLET_STARTER).debug(nodeList.length + " nodes to start. Processing ...");
 		for (String webinfNode : nodeList) {
 			String nodeId = webinfNode.substring(8);
 			String baseUrl = "/" + nodePrefix + "-" + nodeId;
 			
 			Logger.getLogger(JEEVLET_STARTER).debug("  Starting: " + nodeId + " with base URL: "
 					+ baseUrl);
 			GeoNetworkRestletApplication application = new GeoNetworkRestletApplication(
 					component.getContext().createChildContext(), appPath,
 					baseUrl, nodeId, propertyFilePath);
 
 			// Attach the application to the component and start it
 			component.getDefaultHost().attach(baseUrl, application);
 		}
 		Logger.getLogger(JEEVLET_STARTER).debug("Done.");
 		
 		// Check Jetty stop method.
 		Logger.getLogger(JEEVLET_STARTER).debug("Create virtual host for stop method on " + hostDomain + ":" + listenPort + ".");
 		VirtualHost hostHTTP = new VirtualHost(component.getContext());
 		hostHTTP.setHostDomain(hostDomain);
 		hostHTTP.setHostPort(Integer.toString(listenPort));
 		hostHTTP.attach(new GeoNetworkStopperApplication(component));
 		component.getHosts().add(hostHTTP);
 		// Start component
 		component.start();
 		Logger.getLogger(JEEVLET_STARTER).debug("====== Jeevlet apps running ================");
 	}
 
 }
