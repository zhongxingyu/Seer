 package com.trendmicro.tme.grapheditor;
 
 import java.io.FileInputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.jasper.servlet.JspServlet;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.FilterHolder;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.trendmicro.codi.ZKSessionManager;
 import com.trendmicro.tme.mfr.ExchangeFarm;
 
 public class GraphEditorMain {
     private static final String CONFIG_PATH = System.getProperty("com.trendmicro.tme.grapheditor.conf", "/opt/trend/tme/conf/graph-editor/graph-editor.properties");
     private static final Logger logger = LoggerFactory.getLogger(GraphEditorMain.class);
     
     static {
         System.loadLibrary("gv_java");
     }
     
     public static void main(String[] args) throws Exception {
         try {
             Properties prop = new Properties();
             prop.load(new FileInputStream(CONFIG_PATH));
             // Let the system properties override the ones in the config file
             prop.putAll(System.getProperties());
             
             String connectString = prop.getProperty("com.trendmicro.tme.grapheditor.zookeeper.quorum") + prop.getProperty("com.trendmicro.tme.grapheditor.zookeeper.tmeroot");
             ZKSessionManager.initialize(connectString, Integer.valueOf(prop.getProperty("com.trendmicro.tme.grapheditor.zookeeper.timeout")));
             ZKSessionManager.instance().waitConnected();
             
             HandlerList handlers = new HandlerList();
             
             Map<Class<?>, Object> classMap = new HashMap<Class<?>, Object>();
             classMap.put(ExchangeFarm.class, new ExchangeFarm());
             ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SECURITY | ServletContextHandler.SESSIONS);
             ServletHolder jerseyHolder = new ServletHolder(new GraphEditorContainer(classMap));
             
             handler.addServlet(new ServletHolder(new DefaultServlet()), "/static/*");
             
             handler.addServlet(new ServletHolder(new Proxy()), "/proxy/*");
             
             ServletHolder jspHolder = new ServletHolder(new JspServlet());
             jspHolder.setInitParameter("scratchdir", prop.getProperty("jasper.scratchdir", "/var/lib/tme/graph-editor/jsp"));
             jspHolder.setInitParameter("trimSpaces", "true");
             jspHolder.setInitParameter("portalhost", prop.getProperty("com.trendmicro.tme.grapheditor.portalhost", ""));
             handler.addServlet(jspHolder, "*.jsp");
             handler.setResourceBase(prop.getProperty("com.trendmicro.tme.grapheditor.webdir"));
             logger.info("Web resource base is set to {}", prop.getProperty("com.trendmicro.tme.grapheditor.webdir"));
             
             jerseyHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
             jerseyHolder.setInitParameter("com.sun.jersey.config.property.packages", "com.trendmicro.tme.grapheditor");
             handler.addServlet(jerseyHolder, "/webapp/graph-editor/*");
             
             FilterHolder loggingFilterHolder = new FilterHolder(new LoggingFilter());
             loggingFilterHolder.setInitParameter("name", "Brain");
             handler.addFilter(loggingFilterHolder, "/*", 1);
             
             handlers.addHandler(handler);
             
             int port = Integer.valueOf(prop.getProperty("com.trendmicro.tme.grapheditor.port"));
             Server server = new Server(port);
             server.setHandler(handlers);
             
             server.start();
             System.err.println("Graph Editor started listening on port " + port);
             logger.info("Graph Editor started listening on port " + port);
             
            IOUtils.closeQuietly(new URL(String.format("http://localhost:%d/%s?jsp_precompile", port, "graph/graph.jsp")).openConnection().getInputStream());
            IOUtils.closeQuietly(new URL(String.format("http://localhost:%d/%s?jsp_precompile", port, "graph/index.jsp")).openConnection().getInputStream());
            IOUtils.closeQuietly(new URL(String.format("http://localhost:%d/%s?jsp_precompile", port, "processor/processor.jsp")).openConnection().getInputStream());
            IOUtils.closeQuietly(new URL(String.format("http://localhost:%d/%s?jsp_precompile", port, "processor/index.jsp")).openConnection().getInputStream());
            IOUtils.closeQuietly(new URL(String.format("http://localhost:%d/%s?jsp_precompile", port, "exchange/exchange.jsp")).openConnection().getInputStream());
             
             server.join();
         }
         catch(Exception e) {
             e.printStackTrace();
             logger.error("Graph Editor startup error: ", e);
             System.exit(-1);
         }
     }
 }
