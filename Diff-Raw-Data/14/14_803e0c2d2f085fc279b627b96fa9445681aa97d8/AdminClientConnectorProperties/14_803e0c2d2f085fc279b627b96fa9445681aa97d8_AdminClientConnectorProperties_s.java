 package net.gisnas.oystein.ibm;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.soap.SOAPException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import com.ibm.ejs.ras.ManagerAdmin;
 import com.ibm.websphere.management.AdminClient;
 import com.ibm.websphere.management.AdminClientFactory;
 import com.ibm.websphere.management.exception.ConnectorException;
 
 /**
  * Connection settings for {@link AdminClient}. Used to set up remote connection
  * to remote WebSphere server, typically a deployment manager. For simplicity,
  * the remote server is referred to as "deployment manager".
  * 
  * Implemented as convenience wrapper around Properties
  * 
  * Only connection type SOAP is supported SSL client certificates are not
  * supported
  */
 public class AdminClientConnectorProperties extends Properties {
 
 	private static final long serialVersionUID = 1L;
 	private static final Logger logger = LoggerFactory.getLogger(AdminClientConnectorProperties.class);
 
 	/**
 	 * Connection to local deployment manager on default port (8879)
 	 */
 	public AdminClientConnectorProperties() {
 		// Supports only SOAP - no RMI
 		setProperty(AdminClient.CONNECTOR_TYPE, AdminClient.CONNECTOR_TYPE_SOAP);
 		addAddressProperties("localhost", 8879);
 	}
 
 	/**
 	 * Secure connection to local deployment manager
 	 * 
 	 * @param username
 	 * @param password
 	 */
 	public AdminClientConnectorProperties(String username, String password) {
 		this();
 		addSecurityProperties(username, password);
 	}
 
 	/**
 	 * Connection to deployment manager
 	 * 
 	 * @param host
 	 * @param port
 	 */
 	public AdminClientConnectorProperties(String host, int port) {
 		this();
 		addAddressProperties(host, port);
 	}
 
 	/**
 	 * Secure connection to deployment manager
 	 * 
 	 * @param host
 	 *            Deployment manager hostname
 	 * @param port
 	 *            Deployment manager SOAP port
 	 * @param username
 	 *            Deployment manager administrative user
 	 * @param password
 	 *            Password for deployment manager administrative user
 	 */
 	public AdminClientConnectorProperties(String host, int port, String username, String password) {
 		this();
 		addAddressProperties(host, port);
 		addSecurityProperties(username, password);
 	}
 
 	private void addAddressProperties(String host, int port) {
 		setProperty(AdminClient.CONNECTOR_HOST, host);
 		setProperty(AdminClient.CONNECTOR_PORT, String.valueOf(port));
 	}
 
 	private void addSecurityProperties(String username, String password) {
 		setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "true");
 		setProperty(AdminClient.USERNAME, username);
 		setProperty(AdminClient.PASSWORD, password);
 	}
 
 	/**
 	 * Mask out passwords
 	 */
 	@Override
 	public synchronized String toString() {
 		int max = size() - 1;
 		if (max == -1)
 			return "{}";
 
 		StringBuilder sb = new StringBuilder();
 		Iterator<Map.Entry<Object, Object>> it = entrySet().iterator();
 
 		sb.append('{');
 		for (int i = 0;; i++) {
 			Map.Entry<Object, Object> e = it.next();
 			Object key = e.getKey();
 			Object value = e.getValue();
 			sb.append(key == this ? "(this Map)" : key.toString());
 			sb.append('=');
 			if (key.equals(AdminClient.PASSWORD)) {
 				value = "*******";
 			}
 			sb.append(value == this ? "(this Map)" : value.toString());
 
 			if (i == max)
 				return sb.append('}').toString();
 			sb.append(", ");
 		}
 	}
 
 	/**
 	 * Helper method to create AdminClient
 	 * 
 	 * Does some basic exception detection and logging in addition to what
 	 * AdminClientFactory does
 	 * 
 	 * @param properties
	 * @return
 	 */
 	public static AdminClient createAdminClient(AdminClientConnectorProperties properties) {
 		return createAdminClient(properties, null, null);
 	}
 
 		/**
 	 * Helper method to create AdminClient
 	 * 
 	 * Does some basic exception detection and logging in addition to what
 	 * AdminClientFactory does
 	 * 
 	 * @param properties
 	 * @param traceFile If set, trace logging is enabled and output to this file
	 * @param traceString {@link http://pic.dhe.ibm.com/infocenter/wasinfo/v8r5/topic/com.ibm.websphere.base.doc/ae/utrb_loglevel.html}
	 * @return
 	 */
 	public static AdminClient createAdminClient(AdminClientConnectorProperties properties, File traceFile, String traceString) {
 		// It's a security risk to log all properties, which may include a
 		// password
 		logger.debug("Creating AdminClient with {}", properties);
 
 		// Remove IBM logging from stdout
 		SLF4JBridgeHandler.removeHandlersForRootLogger();
 		// Uncomment to redirect logs to SLF4j
 		// SLF4JBridgeHandler.install();
 
 		// Enable IBM trace logger
 		if (traceFile != null) {
 			ManagerAdmin.configureClientTrace(traceString, "named file", traceFile.getPath(), false, null, false, false);
 		}
 
 		// Add system property to avoid missing CORBA classes when using Sun JRE
 		// See http://www-01.ibm.com/support/docview.wss?uid=swg1PM39777
 		System.setProperty("com.ibm.websphere.thinclient", "true");
 
 		try {
 			return AdminClientFactory.createAdminClient(properties);
 		} catch (ConnectorException e) {
 			Throwable rootCause = Throwables.getRootCause(e);
 			// SOAPException from server
 			if (rootCause instanceof SOAPException) {
 				throw new RuntimeException("Connection to deployment manager failed with remote exception", rootCause);
 			}
 			throw new RuntimeException("Failed connecting to the deployment manager", e);
 		}
 	}
 
 }
