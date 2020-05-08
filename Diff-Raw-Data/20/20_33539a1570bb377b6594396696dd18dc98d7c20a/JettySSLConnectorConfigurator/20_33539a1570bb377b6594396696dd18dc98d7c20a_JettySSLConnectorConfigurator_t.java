 package org.italiangrid.utils.https;
 
 import org.eclipse.jetty.server.Connector;
 
 /**
  * A {@link JettySSLConnectorConfigurator} is responsible for configuring an SSL connector 
  * according to a specific set of configuration parameters.
  * 
  * 
  * @author andreaceccanti
  *
  */
 public interface JettySSLConnectorConfigurator {
 
 	/**
 	 * Configure an SSL connector
 	 * @param host the host the connector will bind to 
 	 * @param port the port the connector will bind to 
 	 * @param options the SSL configuration options
	 * @return a {@link Connector} configured as requested
	 * 
 	 */
 	public Connector configureConnector(String host, int port, SSLOptions options);
 }
