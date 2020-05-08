 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.livereload.internal.server.jetty;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerCollection;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.FilterHolder;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 /**
  * Factory to initialize the WebServer with websocket support, optional script
  * injection proxy (for server mode) or resource server (for directory mode).
  * 
  * @author xcoulon
  * 
  */
 public class LiveReloadServerFactory {
 
 	public static ServerBuilder onServer(final AbstractCommandBroadcaster liveReloadCommandBroadcaster) {
 		return new ServerBuilder(liveReloadCommandBroadcaster);
 	}
 
 	
 	public static class ServerBuilder {
 
 		private int websocketPort;
 
 		private int proxyPort = -1;
 		
 		private boolean enableProxy = false;
 		
 		private final AbstractCommandBroadcaster liveReloadCommandBroadcaster;
 		
 		public ServerBuilder(final AbstractCommandBroadcaster liveReloadCommandBroadcaster) {
 			this.liveReloadCommandBroadcaster = liveReloadCommandBroadcaster;
 		}
 
 		public ServerBuilder websocketPort(final int websocketPort) {
 			this.websocketPort = websocketPort;
 			return this;
 		}
 		
 		public ServerBuilder proxyPort(final int proxyPort) {
 			this.enableProxy = true;
 			this.proxyPort = proxyPort;
 			return this;
 		}
 		
 		public Server build() throws UnknownHostException {
 			final Server server = new Server();
 			// connectors
 			final String hostAddress = InetAddress.getLocalHost().getHostAddress();
 			// TODO include SSL Connector
 			final SelectChannelConnector websocketConnector = new SelectChannelConnector();
 			websocketConnector.setPort(websocketPort);
 			websocketConnector.setMaxIdleTime(0);
 			server.addConnector(websocketConnector);
 			final HandlerCollection handlers = new HandlerCollection();
 			server.setHandler(handlers);
 			final ServletContextHandler liveReloadContext = new ServletContextHandler(handlers, "/",
 					ServletContextHandler.NO_SESSIONS);
 			//liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptFileFilter()),
 			//		"/livereload.js", null);
			liveReloadContext.addServlet(new ServletHolder(new LiveReloadWebSocketServlet(
					liveReloadCommandBroadcaster)), "/livereload");
 			liveReloadContext.addServlet(new ServletHolder(new LiveReloadScriptFileServlet()), "/livereload.js");
 			if (enableProxy) {
 				final SelectChannelConnector proxyConnector = new SelectChannelConnector();
 				proxyConnector.setPort(proxyPort);
 				proxyConnector.setMaxIdleTime(0);
 				server.addConnector(proxyConnector);
 
 				// Livereload specific content
 //				liveReloadContext.addServlet(new ServletHolder(new LiveReloadWebSocketServlet(
 //						liveReloadCommandBroadcaster)), "/livereload");
 
 				// Handling all applications behind the proxy
 				liveReloadContext.addServlet(new ServletHolder(ApplicationsProxyServlet.class), "/");
 				liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptInjectionFilter(hostAddress,
 						this.websocketPort)), "/*", null);
 			}
 			return server;
 		}
 		
 	}
 }
