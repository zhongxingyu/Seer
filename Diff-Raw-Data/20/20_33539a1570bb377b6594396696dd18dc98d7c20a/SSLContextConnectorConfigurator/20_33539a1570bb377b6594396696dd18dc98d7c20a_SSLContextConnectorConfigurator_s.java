 package org.italiangrid.utils.https.impl;
 
 import javax.net.ssl.SSLContext;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
 import org.eclipse.jetty.util.ssl.SslContextFactory;
 import org.italiangrid.utils.https.JettySSLConnectorConfigurator;
 import org.italiangrid.utils.https.SSLOptions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A {@link JettySSLConnectorConfigurator} that configures the connector starting
  * from a given {@link SSLContext}.
  * 
  * @author andreaceccanti
  *
  */
 public class SSLContextConnectorConfigurator implements
 		JettySSLConnectorConfigurator {
 	
 	/**
 	 * The slf4j logger
 	 */
 	public static final Logger log = LoggerFactory.getLogger(SSLContextConnectorConfigurator.class);
 	
 	/**
 	 * The SSL context that will be used for the configuration
 	 */
 	protected SSLContext sslContext;
 	
 	
 	public SSLContextConnectorConfigurator(SSLContext ctxt) {
 		sslContext = ctxt;
 	}
 	
 	public Connector configureConnector(String host, int port,
 			SSLOptions options) {
 		
 		Connector connector;
 		
 		if (sslContext == null){	
 			log.error("Cannot initialize SSL out of a null SSL context!");
 			return null;
 		}
 		
 		try{
 			
 			SslContextFactory factory = new SslContextFactory();
 			factory.setSslContext(sslContext);
 			
 			factory.setWantClientAuth(options.isWantClientAuth());
 			factory.setNeedClientAuth(options.isNeedClientAuth());
 			
 			connector = new SslSelectChannelConnector(factory);
 			connector.setHost(host);
 			connector.setPort(port);
 			
 			return connector;
 			
 		}catch (Throwable t){
 			
 			log.error("SSL initialization error!", t);
 			return null;
 		}
 		
 	}
 	
 	/**
 	 * Sets the SSL context for this configurator
 	 * @param sslContext
 	 */
 	public void setSslContext(SSLContext sslContext) {
 		this.sslContext = sslContext;
 	}
 
 	/**
 	 * Returns the SSL context defined for this configurator
	 * @return
 	 */
 	public SSLContext getSslContext() {
 		return sslContext;
 	}
 }
