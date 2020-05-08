 package com.sas.comp.server;
 
 import org.apache.catalina.core.AprLifecycleListener;
 import org.apache.catalina.core.StandardServer;
 import org.apache.catalina.startup.Tomcat;
 
 public class EmbeddedServer {
 
 	public static void main(final String[] args) throws Exception {
 		final EmbeddedServer server = new EmbeddedServer();
 		server.start();
 	}
 
 	private void start() throws Exception {
 		final String appBase = "";
		final Integer port = 80;
 
 		final Tomcat tomcat = new Tomcat();
 		tomcat.setPort(port);
 
 		tomcat.setBaseDir("./web");
 		tomcat.getHost().setAppBase(appBase);
 
 		final String contextPath = "/";
 
 		// Add AprLifecycleListener
 		StandardServer server = (StandardServer) tomcat.getServer();
 		AprLifecycleListener listener = new AprLifecycleListener();
 		server.addLifecycleListener(listener);
 
 		tomcat.addWebapp(contextPath, appBase);
 		tomcat.start();
 		tomcat.getServer().await();
 	}
 }
