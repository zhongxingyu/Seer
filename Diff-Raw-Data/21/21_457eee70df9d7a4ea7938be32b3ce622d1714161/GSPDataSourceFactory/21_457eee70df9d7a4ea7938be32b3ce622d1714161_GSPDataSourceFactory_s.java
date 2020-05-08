 package com.gramercysoftware.persistence.dao;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.text.MessageFormat;
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDataSource;
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.apache.log4j.Logger;
 
 public class GSPDataSourceFactory {
 	private static Logger logger = Logger.getLogger(GSPDataSourceFactory.class);
 	private PoolingDataSource dataSource;
 	private String propertyFileName;
 
 	public GSPDataSourceFactory() {
 	}
 	
 	public GSPDataSourceFactory(String propertyFileName) {
 		this.propertyFileName = propertyFileName;
 	}
 	
 	public DataSource getDataSource() {
 		if(dataSource == null) {
 			createDataSource();
 		}
 		
 		return dataSource;
 	}
 	
 	private void createDataSource() {
 		Properties properties = getDataSourceConfig();
 		initializeDatabaseDriver(properties);
 		
 		ObjectPool connectionPool = createConnectionPool(properties);
		
		//
		// First, we'll create a ConnectionFactory that the
		// pool will use to create Connections.
		// We'll use the DriverManagerConnectionFactory,
		// using the connect string passed in the command line
		// arguments.
		//
 		ConnectionFactory connectionFactory = 
 			new DriverManagerConnectionFactory(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.user"), properties.getProperty("jdbc.password"));

		//
		// Next we'll create the PoolableConnectionFactory, which wraps
		// the "real" Connections created by the ConnectionFactory with
		// the classes that implement the pooling functionality.
		//
//		PoolableConnectionFactory poolableConnectionFactory = 
 		new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true, Connection.TRANSACTION_SERIALIZABLE);
		//
		// Finally, we create the PoolingDriver itself,
		// passing in the object pool we created.
		//
 		dataSource = new PoolingDataSource(connectionPool);
 	}
 
 	private ObjectPool createConnectionPool(Properties properties) {
 		GenericObjectPool.Config config = new GenericObjectPool.Config();
 		config.maxActive = Integer.parseInt(properties.getProperty("pool.maxActive"));
 		config.maxIdle = Integer.parseInt(properties.getProperty("pool.maxIdle"));
 		config.minIdle = Integer.parseInt(properties.getProperty("pool.minIdle"));
 		config.maxWait = Integer.parseInt(properties.getProperty("pool.maxWait"));
 
 		ObjectPool connectionPool = new GenericObjectPool(null, config);
 		return connectionPool;
 	}
 
 	private Properties getDataSourceConfig() {
 		Properties properties = new Properties();
 		try {
 			properties.load(GSPDataSourceFactory.class.getResourceAsStream(getPropertyFileName()));
 		} catch (IOException e) {
 			logger.error(MessageFormat.format("Error loading property file {0}", getPropertyFileName()), e);
 		}
 		
 		return properties;
 	}
 
 	private void initializeDatabaseDriver(Properties properties) {
 		try {
 			Class.forName(properties.getProperty("jdbc.driver"));
 		} catch (ClassNotFoundException e) {
 			logger.error(MessageFormat.format("Error initializing JDBC driver {0}", properties.getProperty("jdbc.driver")), e);
 		}
 	}
 
 	public String getPropertyFileName() {
 		if(propertyFileName == null) {
 			propertyFileName = GSPDataSourceFactory.class.getSimpleName();
 		}
 		return propertyFileName;
 	}
 
 	@Deprecated
 	void setPropertyFileName(String propertyFileName) {
 		this.propertyFileName = propertyFileName;
 	}
 }
