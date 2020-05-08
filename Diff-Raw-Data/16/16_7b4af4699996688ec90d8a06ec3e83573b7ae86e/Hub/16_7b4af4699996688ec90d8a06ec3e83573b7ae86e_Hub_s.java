 /*
 Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org
 
 Copyright (c) 2010-2013 Ewan Branda
 
 NETLab Hub is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 NETLab Hub is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package netlab.hub.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.List;
 
 import netlab.hub.util.ClasspathManager;
 import netlab.hub.util.Logger;
 import netlab.hub.util.NetworkUtils;
 import netlab.hub.util.ThreadUtil;
 
 public class Hub {
 	
 	private static Hub instance;
 	
 	/**
 	 * Runs the Hub without a GUI.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			new Hub().start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void displayMessage(String msg) {
 		instance.monitor.displayAlert(msg);
 	}
 	
 	File root;
 	AdminServer adminServer;
 	IHubLifecycleMonitor monitor;
 	ISessionLifecycleMonitor sessionLifecycleMonitor;
 	IDataActivityMonitor dataActivityMonitor;
 	Server server;
 	WebSocketServerImpl wsServer;
 	boolean running = false;
 	
 	public Hub() throws IOException {
 		File rootDir = new File("");
 		instance = this;
 		configure(rootDir.getAbsoluteFile());
 	}
 	
 	public Hub(File rootDir) {
 		instance = this;
 		configure(rootDir);
 	}
 	
 	private void configure(File rootDir) {
 		root = rootDir;
 		try {
 			Config.load(rootDir);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 	}
 	
 	public String[] listActiveSessions() {
 		return server.listActiveSessions();
 	}
 	
 	public void setHubLifecycleMonitor(IHubLifecycleMonitor monitor) {
 		this.monitor = monitor;
 	}
 	
 	public void setSessionLifecycleMonitor(ISessionLifecycleMonitor lm) {
 		this.sessionLifecycleMonitor = lm;
 	}
 	
 	public void setDataActivityMonitor(IDataActivityMonitor dm) {
 		this.dataActivityMonitor = dm;
 	}
 	
 	public File getRootDir() {
 		return this.root;
 	}
 	
 	public void restart() {
 		shutdown();
 		start();
 	}
 	
 	public void start() {
 		
 		if (monitor == null) {
 			// Create a default (null) monitor
 			monitor = new IHubLifecycleMonitor() {
 				public void displayStatus(String status) {}
 				public void initializationComplete() {}
 				public void initializationFailed() {}
 				public void displayAlert(String msg) {}
 			};
 		}
 		monitor.displayStatus("Starting...");
 		
 		try {
 			
 			// Make app base directory available to log4J and others that need it.
 			// Things get complicated when running from Processing so this system
 			// property guarantees that we can access the current app base at any point.
 			System.setProperty("netlab.hub.base", root.getAbsolutePath()); 
 
 			// Init logging
 			try {
 				// Create the log file and directory if it is missing
 				File logDir = new File(root, "log");
 				if (!logDir.exists())
 					logDir.mkdir();
 				File logFile = new File(logDir, "hub.log");
 				if (!logFile.exists()) 
 					logFile.createNewFile();
 				File logProps = new File(new File(root, "conf"), "log4j.properties");
 				Logger.configure(logProps);
 				if (!"0".equals(Config.getProperty("app.startindebugmode"))) {
 					Logger.switchDebugLevel();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			ThreadUtil.pause(100);
 			
 			String metadata = "Starting "+Config.getAppName()+"" +
 					" version "+Config.getAppVersion()+" (build "+Config.getAppBuild()+")";
 			Logger.info(metadata);
 			
 			if (adminServer == null) {
				int adminPort = 8080;
 				adminServer = new AdminServer(adminPort, this);
 				try {
 					adminServer.start();
 					Logger.info("Admin server running at http://"+NetworkUtils.getLocalMachineAddress()+":"+adminPort);
 				} catch (Exception e) {
 					Logger.error("Could not start admin server", e);
 				}
 			}
 			
 			monitor.displayStatus("Loading plugins...");
 			
 			// Load plugin library code and add directories to library path
 			File pluginsDir = new File(root, "plugins");
 			ClasspathManager.addToClasspath(pluginsDir, true);
 			ClasspathManager.addLibrariesToSystemLibraryPath(pluginsDir, "lib");
 			
 			// Load plugin configs
 			List<PlugIn> plugIns = PlugIn.loadAll(Config.root);
 			
 			// Load configs for all services defined in plugins
 			for (Iterator<PlugIn> it=plugIns.iterator(); it.hasNext();) {
 				PlugIn plugIn = it.next();
 				ServiceConfig.loadAll(plugIn.getConfigDocument(), plugIn.getName());
 			}
 			// Load custom service configs
 			XMLConfig customConfig = new XMLConfig(new File(root, "conf"), "services.xml");
 			ServiceConfig.loadAll(customConfig, "custom");
 			
 			// Attach implementing classes to custom services that inherit from other services
 			ServiceConfig.processSubclasses();
 			
 			// Initialize and register all services
 			monitor.displayStatus("Initializing services...");
 			ThreadUtil.pause(250); // Why is this needed?
 			for (Iterator<ServiceConfig> it=ServiceConfig.getAll().iterator(); it.hasNext();) {
 				ServiceConfig config = it.next();
 				try {
 					Service service = (Service)Class.forName(config.getServiceClass()).newInstance();
 					service.setConfig(config);
 					ServiceRegistry.register(config.getAddress(), service);
 				} catch (NoClassDefFoundError e) {
 					config.setEnabled(false);
 					Logger.warn("Disabled service ["+config.address+"]. Required classes ("+
 									e.getMessage()+") are missing from the Hub runtime environment.");
 				} catch (Exception e) {
 					Logger.error("Error loading service ["+config.address+"]", e, true);
 					config.setEnabled(false);
 				}
 			}
 			
 			// Add the shutdown hook
 			//if (System.getProperty("os.name").startsWith("Mac OS")) {
 				Runtime.getRuntime().addShutdownHook(
 						new Thread(new Runnable() {
 							public void run() {
 								shutdown();
 							}
 						}, "Shutdown Hook"));
 			//}
 			
 			// Initialize the message dispatcher
 			Dispatcher dispatcher = new Dispatcher();
 			dispatcher.start();
 			
 			ThreadUtil.pause(250);
 			monitor.displayStatus("Starting server...");
 			// Start the socket server
 			server = new Server(Config.getPort(), dispatcher, sessionLifecycleMonitor, dataActivityMonitor);
 			server.start();
 			
 			// Start the Websockets server
 			int wsPort = Config.getWebSocketPort();
 			if (wsPort != -1) {
 				wsServer = new WebSocketServerImpl(wsPort, Config.getPort());
 				wsServer.start();
 			}
 			
 			String machineAddress = NetworkUtils.getLocalMachineAddress();
 			if (machineAddress != null) {
 				Logger.info("Waiting for TCP/IP socket connections at "+machineAddress+":"+Config.getPort());
 				if (wsPort != -1) {
 					Logger.info("Waiting for WebSocket connections at "+machineAddress+":"+wsPort);
 				}
 			}
 			
 			callHome();
 			
 			ThreadUtil.pause(500);
 			Logger.info("Hub successfully started.");
 			running = true;
 			
 			monitor.initializationComplete();
 			
 		} catch (Exception e) {
 			//Logger.fatal("Error starting application", e);
 			//e.printStackTrace();
 			Logger.error("Error starting Hub: "+e);
 			monitor.initializationFailed();
 			return;
 		}
 	}
 	
 	public String callHome() {
 		try {
 			if ("unspecified".equals(Config.getAppVersion())) {
 				return null; // We are in dev mode
 			}
 			String version = URLEncoder.encode(Config.getAppVersion()+"-"+Config.getAppBuild(), "UTF-8");
 			URL url = new URL(Config.getCallHomeUrl() + "?hub_version="+version);
 			Logger.debug("Calling home to URL "+url);
 			return NetworkUtils.readFromUrl(url);
 		} catch (Exception e) {
 			Logger.error("Illegal callback URL: "+Config.getCallHomeUrl());
 			return null;
 		}
 	}
 	
 	private synchronized void shutdown() {
 		if (running) {
 			Logger.debug("Shutting down the Hub...");
 			if (server != null) {
 				server.stop();
 			}
 			if (wsServer != null) {
 				wsServer.stop();
 			}
 			Logger.debug("Shutting down services...");
 			ServiceConfig.clearAll();
 			try {
 				for (Iterator<Service> it=ServiceRegistry.getAll().iterator(); it.hasNext();) {
 					it.next().dispose();
 				}
 				ServiceRegistry.clear();
 			} catch (ServiceException e) {
 				Logger.error("Error shutting down service registry", e);
 			}
 			Logger.debug("Services shut down.");
 		}
 		Logger.debug("Hub shut down.");
 		running = false;
 	}
 	
 	/**
 	 * 
 	 */
 	public void quit() {
 		shutdown();
 		Logger.info("Goodbye.");
 		System.exit(0);
 	}
 	
 	
 
 }
