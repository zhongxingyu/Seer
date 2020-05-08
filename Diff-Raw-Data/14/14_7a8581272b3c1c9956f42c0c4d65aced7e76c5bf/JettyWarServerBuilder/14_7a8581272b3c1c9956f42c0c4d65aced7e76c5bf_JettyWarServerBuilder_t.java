 package com.nytimes.eps.war.container;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class JettyWarServerBuilder {
 
 	public Server build() {
 		Server server = new Server(8080);
 	    WebAppContext root = new WebAppContext();
 	    root.setContextPath("/");
	    root.setResourceBase("./");
	    //root.setWar("./tmp");
	    root.setDescriptor("./web.xml");
 	    root.setParentLoaderPriority(true);
 	    server.setHandler(root);
 	    return server;
 	}
 	
 	public static void main(String ... args) throws Exception {
 		Server server = new JettyWarServerBuilder().build();
 		server.start();
 		server.join();
 	}
 }
