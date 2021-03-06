 package org.sagebionetworks;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 /**
  * StackConfiguration wraps all configuration needed for a Synapse service stack
  * and exposes it via this programmatic API.
  * 
  * Note that it wraps an instance of TemplatedConfiguration which was
  * initialized with a template for the properties a Synapse service stack should
  * have and default property values for service stacks.
  * 
  */
 public class StackConfiguration {
 
 	static final String DEFAULT_PROPERTIES_FILENAME = "/stack.properties";
 	static final String TEMPLATE_PROPERTIES = "/template.properties";
 
 	private static final Logger log = Logger.getLogger(StackConfiguration.class
 			.getName());
 
 	private static TemplatedConfiguration configuration = null;
 
 	static {
 		configuration = new TemplatedConfiguration(DEFAULT_PROPERTIES_FILENAME,
 				TEMPLATE_PROPERTIES);
 		// Load the stack configuration the first time this class is referenced
 		try {
 			configuration.reloadStackConfiguration();
 		} catch (Throwable t) {
 			log.error(t.getMessage(), t);
 			throw new RuntimeException(t);
 		}
 	}
 
 	public static void reloadStackConfiguration() {
 		configuration.reloadStackConfiguration();
 	}
 
 	/**
 	 * The name of the stack.
 	 * 
 	 * @return
 	 */
 	public static String getStack() {
 		return configuration.getStack();
 	}
 
 	/**
 	 * The stack instance (i.e 'A', or 'B')
 	 * 
 	 * @return
 	 */
 	public static String getStackInstance() {
 		return configuration.getStackInstance();
 	}
 
 	/**
 	 * @return the encryption key for this stack
 	 */
 	public static String getEncryptionKey() {
 		return configuration.getEncryptionKey();
 	}
 	
 	/**
 	 * Get the IAM user ID (Access Key ID)
 	 * 
 	 * @return
 	 */
 	public static String getIAMUserId() {
 		return configuration.getIAMUserId();
 	}
 
 	/**
 	 * Get the IAM user Key (Secret Access Key)
 	 * 
 	 * @return
 	 */
 	public static String getIAMUserKey() {
 		return configuration.getIAMUserKey();
 	}
 
 	public static String getAuthenticationServicePrivateEndpoint() {
 		return configuration.getAuthenticationServicePrivateEndpoint();
 	}
 
 	public static String getAuthenticationServicePublicEndpoint() {
 		return configuration.getAuthenticationServicePublicEndpoint();
 	}
 
 	public static String getRepositoryServiceEndpoint() {
 		return configuration.getRepositoryServiceEndpoint();
 	}
 
 	public static String getPortalEndpoint() {
 		return configuration.getPortalEndpoint();
 	}
 
 	public static String getCrowdEndpoint() {
 		return configuration.getProperty("org.sagebionetworks.crowd.endpoint");
 	}
 
 	public static String getS3Bucket() {
 		return configuration.getProperty("org.sagebionetworks.s3.bucket");
 	}
 
 	public static Integer getS3ReadAccessExpiryHours() {
 		return Integer.valueOf(configuration
 				.getProperty("org.sagebionetworks.s3.readAccessExpiryHours"));
 	}
 
 	public static Integer getS3WriteAccessExpiryHours() {
 		return Integer
 				.valueOf(configuration
 						.getProperty("org.sagebionetworks.s3.writeAccessExpiryHours"));
 	}
 
 	public static String getCrowdAPIApplicationKey() {
 		return configuration
 				.getDecryptedProperty("org.sagebionetworks.crowdApplicationKey");
 	}
 
 	public static String getMailPassword() {
 		return configuration.getDecryptedProperty("org.sagebionetworks.mailPW");
 	}
 
 	/**
 	 * The database connection string used for the ID Generator.
 	 * 
 	 * @return
 	 */
 	public String getIdGeneratorDatabaseConnectionUrl() {
 		return configuration
 				.getProperty("org.sagebionetworks.id.generator.database.connection.url");
 	}
 
 	/**
 	 * The username used for the ID Generator.
 	 * 
 	 * @return
 	 */
 	public String getIdGeneratorDatabaseUsername() {
 		return configuration
 				.getProperty("org.sagebionetworks.id.generator.database.username");
 	}
 
 	/**
 	 * The password used for the ID Generator.
 	 * 
 	 * @return
 	 */
 	public String getIdGeneratorDatabasePassword() {
 		return configuration
 				.getDecryptedProperty("org.sagebionetworks.id.generator.database.password");
 	}
 
 	public String getIdGeneratorDatabaseDriver() {
 		return configuration
 				.getProperty("org.sagebionetworks.id.generator.database.driver");
 	}
 
 	/**
 	 * All of these keys are used to build up a map of JDO configurations passed
 	 * to the JDOPersistenceManagerFactory
 	 */
 	private static String[] MAP_PROPERTY_NAME = new String[] {
 			"javax.jdo.PersistenceManagerFactoryClass",
 			"datanucleus.NontransactionalRead",
 			"datanucleus.NontransactionalWrite",
 			"javax.jdo.option.RetainValues", "datanucleus.autoCreateSchema",
 			"datanucleus.validateConstraints", "datanucleus.validateTables",
 			"datanucleus.transactionIsolation", };
 
 	public Map<String, String> getRepositoryJDOConfigurationMap() {
 		HashMap<String, String> map = new HashMap<String, String>();
 		for (String name : MAP_PROPERTY_NAME) {
 			String value = configuration.getProperty(name);
 			if (value == null)
 				throw new IllegalArgumentException("Failed to find property: "
 						+ name);
 			map.put(name, value);
 		}
 		map.put("javax.jdo.option.ConnectionURL",
 				getRepositoryDatabaseConnectionUrl());
 		map.put("javax.jdo.option.ConnectionDriverName",
 				getRepositoryDatabaseDriver());
 		map.put("javax.jdo.option.ConnectionUserName",
 				getRepositoryDatabaseUsername());
 		map.put("javax.jdo.option.ConnectionPassword",
 				getRepositoryDatabasePassword());
 		return map;
 	}
 
 	/**
 	 * Driver for the repository service.
 	 * 
 	 * @return
 	 */
 	public String getRepositoryDatabaseDriver() {
 		return configuration
 				.getProperty("org.sagebionetworks.id.generator.database.driver");
 	}
 
 	/**
 	 * The repository database connection string.
 	 * 
 	 * @return
 	 */
 	public String getRepositoryDatabaseConnectionUrl() {
 		// First try to load the system property
 		String jdbcConnection = System.getProperty("JDBC_CONNECTION_STRING");
 		if (jdbcConnection != null && !"".equals(jdbcConnection))
 			return jdbcConnection;
 		// Now try the environment variable
 		jdbcConnection = System.getenv("JDBC_CONNECTION_STRING");
 		if (jdbcConnection != null && !"".equals(jdbcConnection))
 			return jdbcConnection;
 		// Last try the stack configuration
 		return configuration
 				.getProperty("org.sagebionetworks.repository.database.connection.url");
 	}
 
 	/**
 	 * The repository database username.
 	 * 
 	 * @return
 	 */
 	public String getRepositoryDatabaseUsername() {
 		return configuration
 				.getProperty("org.sagebionetworks.repository.database.username");
 	}
 
 	/**
 	 * The repository database password.
 	 * 
 	 * @return
 	 */
 	public String getRepositoryDatabasePassword() {
 		return configuration
 				.getDecryptedProperty("org.sagebionetworks.repository.database.password");
 	}
 
 	/**
 	 * Should the connection pool connections be validated?
 	 * 
 	 * @return
 	 */
 	public String getDatabaseConnectionPoolShouldValidate() {
 		return configuration
 				.getProperty("org.sagebionetworks.pool.connection.validate");
 	}
 
 	/**
 	 * The SQL used to validate pool connections
 	 * 
 	 * @return
 	 */
 	public String getDatabaseConnectionPoolValidateSql() {
 		return configuration
 				.getProperty("org.sagebionetworks.pool.connection.validate.sql");
 	}
 
 	/**
 	 * The minimum number of connections in the pool
 	 * 
 	 * @return
 	 */
 	public String getDatabaseConnectionPoolMinNumberConnections() {
 		return configuration
 				.getProperty("org.sagebionetworks.pool.min.number.connections");
 	}
 
 	/**
 	 * The maximum number of connections in the pool
 	 * 
 	 * @return
 	 */
 	public String getDatabaseConnectionPoolMaxNumberConnections() {
 		return configuration
 				.getProperty("org.sagebionetworks.pool.max.number.connections");
 	}
 
 	/**
 	 * @return The name of a user for integration tests
 	 */
 	public static String getIntegrationTestUserOneName() {
 		return configuration
 				.getProperty("org.sagebionetworks.integration.test.username.one");
 	}
 
 	/**
 	 * @return The password of a user for integration tests
 	 */
 	public static String getIntegrationTestUserOnePassword() {
 		return configuration
 				.getProperty("org.sagebionetworks.integration.test.password.one");
 	}
 
 	/**
 	 * @return The name of a second user for integration tests
 	 */
 	public static String getIntegrationTestUserTwoName() {
 		return configuration
 				.getProperty("org.sagebionetworks.integration.test.username.two");
 	}
 
 	/**
 	 * @return The password of a second user for integration tests
 	 */
 	public static String getIntegrationTestUserTwoPassword() {
 		return configuration
 				.getProperty("org.sagebionetworks.integration.test.password.two");
 	}
 	
 	/**
 	 * @return The name of a second user for integration tests
 	 */
 	public static String getIntegrationTestUserAdminName() {
 		return configuration
 				.getProperty("org.sagebionetworks.integration.test.username.admin");
 	}
 
 	/**
 	 * @return The password of a second user for integration tests
 	 */
 	public static String getIntegrationTestUserAdminPassword() {
 		return configuration
 				.getProperty("org.sagebionetworks.integration.test.password.admin");
 	}
 
 	/**
 	 * @return whether the cloudWatch profiler should be on or off boolean.
 	 * True means on, false means off.
 	 */
 	public boolean getCloudWatchOnOff() {
 		//Boolean toReturn = Boolean.getBoolean(getProperty("org.sagebionetworks.cloud.watch.report.enabled"));
 		String answer = configuration.getProperty("org.sagebionetworks.cloud.watch.report.enabled");
 		boolean theValue = Boolean.parseBoolean(answer);
 		return theValue;
 	}
 	
 	/**
 	 * @return the time in milliseconds for the cloudWatch profiler's trigger.  I till trigger
 	 * and send metrics to cloudWatch ever xxx milliseconds.
 	 */
 	public long getCloudWatchTriggerTime() {
 		return Long.valueOf(configuration.getProperty("org.sagebionetworks.cloud.watch.trigger"));
 	}
 }
