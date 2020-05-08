 /*******************************************************************************
  * Poor Man's CMS (pmcms) - A very basic CMS generating static html pages.
  * http://pmcms.sourceforge.net
  * Copyright (C) 2004-2013 by Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  ******************************************************************************/
 package de.thischwa.pmcms.server;
 
 import java.io.File;
 
 import javax.servlet.Servlet;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 import org.springframework.util.ClassUtils;
 
 import de.thischwa.c5c.ConnectorServlet;
 import de.thischwa.pmcms.Constants;
 import de.thischwa.pmcms.configuration.AInitializingTask;
 import de.thischwa.pmcms.configuration.IApplicationLiveCycleListener;
 
 /**
  * Configure and start of Jetty.
  */
 @Service("jettyLauncher")
 public class JettyLauncher extends AInitializingTask implements IApplicationLiveCycleListener {
 
 	/** Local reference of the jetty server. */
 	private static Server server = null;
 
 	@Value("${data.dir}")
 	private String dataDirStr;
 	
 	@Value("${pmcms.dir.sites}")
 	private String sitesDir;
 	
 	@Value("${pmcms.jetty.host}")
 	private String host;
 
 	@Value("${pmcms.jetty.port}")
 	private String port;
 	
 	@Override
 	public void onApplicationStart() {
 		File dataDir = new File(dataDirStr);
 		try {
 			ClassLoader classLoader = ClassUtils.getDefaultClassLoader(); // spring class loader
 			server = new Server();
 			Connector connector = new SelectChannelConnector();
 			connector.setHost(host);
 			connector.setPort(Integer.parseInt(port));
 			connector.setMaxIdleTime(Integer.MAX_VALUE);
 			connector.setLowResourceMaxIdleTime(Integer.MAX_VALUE);
 			server.setConnectors(new Connector[] { connector });
 
 			ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
 			context.setClassLoader(classLoader);
 			context.setResourceBase(dataDir.getAbsolutePath());
 
 			ServletHolder holderDefaults = new ServletHolder(DefaultServlet.class);
 			holderDefaults.setInitParameter("basePath", Constants.APPLICATION_DIR.getAbsolutePath() + "/defaults");
 			holderDefaults.setInitParameter("aliases", "false");
 			context.addServlet(holderDefaults, "/defaults/*");
 
 			ServletHolder holderHelp = new ServletHolder(DefaultServlet.class);
 			holderHelp.setInitParameter("basePath", Constants.APPLICATION_DIR.getAbsolutePath() + "/help");
 			holderHelp.setInitParameter("aliases", "false");
 			context.addServlet(holderHelp, "/help/*");
 			
 			ServletHolder holderFilemanger = new ServletHolder(DefaultServlet.class);
 			holderFilemanger.setInitParameter("basePath", Constants.APPLICATION_DIR.getAbsolutePath() + "/filemanager");
 			holderFilemanger.setInitParameter("aliases", "false");
 			context.addServlet(holderFilemanger, "/filemanager/*");
 
 			context.addServlet(buildLoadOnStart(EditorServlet.class), "/" + Constants.LINK_IDENTICATOR_EDIT + "/*");
 			context.addServlet(buildLoadOnStart(ContentSaverServlet.class), "/" + Constants.LINK_IDENTICATOR_SAVE + "/*");
 			context.addServlet(buildLoadOnStart(PreviewServlet.class), "/" + Constants.LINK_IDENTICATOR_PREVIEW + "/*");

			context.addServlet(buildLoadOnStart(ConnectorServlet.class), "/filemanager/connectors/java/*");

 			ServletHolder holderCodeMirror = new ServletHolder(ZipProxyServlet.class);
 			holderCodeMirror.setInitParameter("file", "sourceeditor/CodeMirror-2013-02-09.zip");
 			holderCodeMirror.setInitParameter("zipPathToSkip", "CodeMirror-master");
 			context.addServlet(holderCodeMirror, "/codemirror/*");
 
 			ServletHolder holderSiteResource = new ServletHolder(SiteResourceServlet.class);
 			holderSiteResource.setInitParameter("basePath", new File(dataDir, sitesDir).getAbsolutePath());
 			context.addServlet(holderSiteResource, String.format("/%s/*", Constants.LINK_IDENTICATOR_SITE_RESOURCE));
 
 			ServletHolder holderCKEditor = new ServletHolder(ZipProxyServlet.class);
 			holderCKEditor.setInitParameter("file", "ckeditor_4.0.1_standard.zip");
 			holderCKEditor.setInitParameter("zipPathToSkip", "ckeditor");
 			context.addServlet(holderCKEditor, "/ckeditor/*");
 			
 			/*ServletHolder holderFilemanager = new ServletHolder(ZipProxyServlet.class);
 			holderFilemanager.setInitParameter("file", "Filemanager.zip");
 			holderFilemanager.setInitParameter("zipPathToSkip", "Filemanager-master");
 			context.addServlet(holderFilemanager, "/filemanager/*");*/
 			
 			ServletHolder holderTest = new ServletHolder(TestServlet.class);
 			context.addServlet(holderTest, "/webgui/*");
 
 			ServletHolder holderResourceWebgui = new ServletHolder(DefaultServlet.class);
 			holderResourceWebgui.setInitParameter("basePath", Constants.APPLICATION_DIR.getAbsolutePath() + "/webgui/resources");
 			holderResourceWebgui.setInitParameter("aliases", "false");
 			context.addServlet(holderResourceWebgui, "/resc/*");
 			
 			server.start();
 		} catch (Exception e) {
 			throw new RuntimeException("Start of jetty failed: " + e.getMessage(), e);
 		}
 		super.onApplicationStart();
 	}
 	
 	private ServletHolder buildLoadOnStart(Class<? extends Servlet> servlet) {
 		ServletHolder sh = new ServletHolder(servlet);
 		sh.setInitOrder(1);
 		return sh;
 	}
 
 	@Override
 	public void onApplicationEnd() {
 		if (server != null) {
 			try {
 				server.stop();
 				server = null;
 			} catch (Exception e) {
 				throw new RuntimeException("Error while stopping jetty: " + e.getMessage(), e);
 			}
 		}
 		super.onApplicationEnd();
 	}
 }
