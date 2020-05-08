 package com.nordija.statistic.mnm.agent;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.Lifecycle;
 import org.springframework.stereotype.Service;
 
 @Service("monitorRestService")
 public class MonitorRestService implements Lifecycle {
 	private final static Logger logger = LoggerFactory.getLogger(MonitorRestService.class);
 	
	private final static String webappDirLocation = "src/main/webapp/";
 	
 	@Value("${rest.port}")
 	private int port;
 	
 //	@Autowired
 //	private BrokerMonitor brokerMonitor;
 //	@Autowired
 //	private AggregatorMonitor aggregatorMonitor;
 //	@Autowired
 //	private ActiveMQBrokerAdmin amqBrokerAdmin;
 
 	private Server server = null;
 
 	@Override
 	public void start(){
 		setup();
 		
 	    try {
 			server.start();
			server.join();		
 		} catch (Exception e) {
 			throw new RuntimeException("Failed to start embedded web server.",e);
 		}
	    logger.info("Monitor Rest Service started.");
 	}
 
 	@Override
 	public void stop() {
 		try {
 			server.stop();
 			server.join();
 		} catch (Exception e) {
 			logger.error("Failed to stop embedded web server.", e);
 		}
 	    logger.info("Monitor Rest Service stopped.");
 	}
 
 	@Override
 	public boolean isRunning() {
 		return server.isRunning();
 	}
 	
 	private void setup(){
 	    server = new Server(port);
 
 //		SelectChannelConnector connector = new SelectChannelConnector();
 //        connector.setPort(port);
 //        connector.setMaxIdleTime(30000);
 //        connector.setRequestHeaderSize(8192);
 //        
 //        server.setConnectors(new Connector[]{connector});
         
 	    WebAppContext appCtx = new WebAppContext();
 		 
 	    appCtx.setContextPath("/");
 	    appCtx.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
	    appCtx.setResourceBase(webappDirLocation);
	 
 	    appCtx.setParentLoaderPriority(true);
 	 
 	    server.setHandler(appCtx);
 
  	}
 }
