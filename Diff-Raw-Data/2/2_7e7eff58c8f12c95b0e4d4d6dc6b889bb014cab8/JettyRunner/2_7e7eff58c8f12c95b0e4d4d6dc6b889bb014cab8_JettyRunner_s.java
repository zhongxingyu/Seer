 package com.warspite.insulae.jetty;
 import java.io.File;
 
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.jetty.webapp.WebAppContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.warspite.common.cli.CliListener;
 import com.warspite.common.servlets.sessions.SessionKeeper;
 import com.warspite.insulae.servlets.world.*;
 import com.warspite.insulae.servlets.account.*;
 import com.warspite.insulae.servlets.geography.*;
 import com.warspite.insulae.servlets.industry.*;
 import com.warspite.insulae.database.InsulaeDatabase;
 
 
 public class JettyRunner extends Thread implements CliListener {
 	private final String API_PATH = "/api";
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	private final SessionKeeper sessionKeeper;
 	private Server server;
 	private boolean online = false;
 	private boolean halt = false;
 	private boolean aborted = false;
 
 	private final int serverPort;
 
 	private final InsulaeDatabase db;
 	private final File warFile;
 
 
 	public JettyRunner(final int serverPort, final SessionKeeper sessionKeeper, final InsulaeDatabase db, final File warFile) {
 		this.serverPort = serverPort;
 		this.sessionKeeper = sessionKeeper;
 		this.db = db;
 		this.warFile = warFile;
 	}
 
 	public boolean isOnline() {
 		return online;
 	}
 
 	public void setHalt(final boolean halt) {
 		this.halt = halt;
 	}
 
 	public boolean hasAborted() {
 		return aborted;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			startServer();
 
 			synchronized(this) {
 				while(!halt) {
 					Thread.sleep(250);
 				}
 			}
 		} 
 		catch (Exception e) {
 			logger.error("Failure while running Jetty server.", e);
 			aborted = true;
 		}
 		finally {
 			try {
 				stopServer();
 			} 
 			catch (Exception e) {
 				logger.error("Failed to stop Jetty server.", e);
 			}
 		}
 	}
 
 	private Server createServer() {
 		logger.info("Jetty launching at port " + serverPort + ", WAR " + warFile);
 		final WebAppContext webapp = new WebAppContext();
 
 		webapp.setContextPath("/");
 		webapp.setWar(warFile.getAbsolutePath());
 		webapp.addServlet(new ServletHolder(new AccountServlet(db, sessionKeeper)), API_PATH + "/account/Account");
 		webapp.addServlet(new ServletHolder(new SessionServlet(db, sessionKeeper)), API_PATH + "/account/Session");
 		webapp.addServlet(new ServletHolder(new RealmServlet(db, sessionKeeper)), API_PATH + "/world/Realm");
 		webapp.addServlet(new ServletHolder(new RaceServlet(db, sessionKeeper)), API_PATH + "/world/Race");
 		webapp.addServlet(new ServletHolder(new SexServlet(db, sessionKeeper)), API_PATH + "/world/Sex");
 		webapp.addServlet(new ServletHolder(new AvatarServlet(db, sessionKeeper)), API_PATH + "/world/Avatar");
 		webapp.addServlet(new ServletHolder(new AreaServlet(db, sessionKeeper)), API_PATH + "/geography/Area");
 		webapp.addServlet(new ServletHolder(new LocationServlet(db, sessionKeeper)), API_PATH + "/geography/Location");
 		webapp.addServlet(new ServletHolder(new LocationTypeServlet(db, sessionKeeper)), API_PATH + "/geography/LocationType");
 		webapp.addServlet(new ServletHolder(new TransportationTypeServlet(db, sessionKeeper)), API_PATH + "/geography/TransportationType");
 		webapp.addServlet(new ServletHolder(new TransportationCostServlet(db, sessionKeeper)), API_PATH + "/geography/TransportationCost");
 		webapp.addServlet(new ServletHolder(new LocationNeighborServlet(db, sessionKeeper)), API_PATH + "/geography/LocationNeighbor");
 		webapp.addServlet(new ServletHolder(new BuildingTypeServlet(db, sessionKeeper)), API_PATH + "/industry/BuildingType");
 		webapp.addServlet(new ServletHolder(new BuildingServlet(db, sessionKeeper)), API_PATH + "/industry/Building");
 		webapp.addServlet(new ServletHolder(new ItemTypeServlet(db, sessionKeeper)), API_PATH + "/industry/ItemType");
 		webapp.addServlet(new ServletHolder(new ItemStorageServlet(db, sessionKeeper)), API_PATH + "/industry/ItemStorage");
 
 		final Server server = new Server(serverPort);
 		server.setHandler(webapp);
 
 		logger.debug("Jetty server created.");
 		return server;
 	}
 
 	private void startServer() throws Exception {
 		if( server == null ) {
 			try {
 				logger.debug("Creating Jetty server.");
 				server = createServer();
 				logger.debug("Jetty server created.");
 			}
 			catch(Throwable e) {
 				logger.error("Failed to create Jetty server.", e);
 				aborted = true;
 				return;
 			}
 		}
 
 
 		logger.debug("Starting Jetty server.");
 		server.start();
 		sessionKeeper.start();
 		online = true;
 		halt = false;
 		logger.debug("Jetty server started.");
 	}
 
 	private void stopServer() throws Exception {
 		if(server == null) {
 			logger.debug("Tried to stop Jetty server, but there is no server to stop.");
 			return;
 		}
 
 		logger.debug("Stopping Jetty server.");
 		server.stop();
 		sessionKeeper.stop();
 		online = false;
 		halt = false;
 		logger.debug("Jetty server stopped.");
 	}
 }
