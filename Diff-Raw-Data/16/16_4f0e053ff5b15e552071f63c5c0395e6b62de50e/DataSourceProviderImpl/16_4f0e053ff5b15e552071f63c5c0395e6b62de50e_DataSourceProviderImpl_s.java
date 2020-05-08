 package org.liveSense.service.DataSourceProvider;
 
 /**
  *
  * @author Robert Csakany (robson@semmi.se)
  * @created Jan 25, 2011
  */
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.sql.Connection;
import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.ConfigurationPolicy;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.PropertyOption;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.classloader.DynamicClassLoaderManager;
import org.liveSense.api.sql.exceptions.NoDataSourceFound;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * OSGi DBCP Datasource provider<br/>
 
  * <table>
  * <hr><th>Parameter</th><th>Description</th></hr>
  * <tr><td>dataSourceName</td><td>The default name of data source</td></tr>
  * <tr><td>username</td><td>The connection username to be passed to our JDBC driver to establish a connection.</td></tr>
  * <tr><td>password</td><td>The connection password to be passed to our JDBC driver to establish a connection.</td></tr> 
  * <tr><td>url</td><td>The connection URL to be passed to our JDBC driver to establish a connection.</td></tr> 
  * <tr><td>driverClassName</td><td>The fully qualified Java class name of the JDBC driver to be used.</td></tr>
  * <tr><td>connectionProperties</td><td>The connection properties that will be sent to our JDBC driver when establishing new connections.
  *      							    <br/>Format of the string must be [propertyName=property;]*
  *      							    <br/><strong>NOTE</strong> - The &quot;user&quot; and &quot;password&quot; properties will be passed explicitly, so they do not need to be included here.</td></tr> 
  *  </table> 
  *  <table>
  *	<hr><th>Parameter</th><th>Default</th><th>Description</th></hr>
  *	<tr><td>defaultAutoCommit</td><td>true</td><td>The default auto-commit state of connections created by this pool.</td></tr>
  *	<tr><td>defaultReadOnly</td><td>driver default</td><td>The default read-only state of connections created by this pool. 
  *														<br/>If not set then the setReadOnly method will not be called. 
  *														<br/>(Some drivers don't support read only mode, ex: Informix)</td></tr>
  *	<tr><td>defaultTransactionIsolation</td><td>driver default</td><td>The default TransactionIsolation state of connections created by this pool.
  *																	<br/>One of the following: (see <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/sql/Connection.html#field_summary">javadoc</a>)
  *																		<ul>
  *																			<li>0 - NONE</li>
  *																			<li>1 - READ_COMMITTED</li>
  *																			<li>2 - READ_UNCOMMITTED</li>
  *																			<li>4 - REPEATABLE_READ</li>
  *																			<li>8 - SERIALIZABLE</li>
  *																		</ul></td></tr>
  *	<tr><td>defaultCatalog</td><td></td><td>The default catalog of connections created by this pool.</td></tr>
  *	</table>
  *	<table>
  *	<hr><th>Parameter</th><th>Default</th><th>Description</th></hr>
  *	<tr><td>initialSize</td><td>0</td><td> The initial number of connections that are created when the pool is started. <br/> Since: 1.2 </td></tr>
  *	<tr><td>maxActive</td><td>8</td><td> The maximum number of active connections that can be allocated from this pool at the same time, or negative for no limit. </td></tr>
  *	<tr><td>maxIdle</td><td>8</td><td> The maximum number of connections that can remain idle in the pool, without extra ones being released, or negative for no limit. </td></tr>
  *	<tr><td>minIdle</td><td>0</td><td> The minimum number of connections that can remain idle in the pool, without extra ones being created, or zero to create none. </td></tr>
  *	<tr><td>maxWait</td><td>indefinitely</td><td> The maximum number of milliseconds that the pool will wait (when there are no available connections) for a connection to be returned before throwing an exception, or -1 to wait indefinitely. </td></tr>
  *</table> 
  */
 
 @Component(label = "%dataSourceProvider.name", description = "%dataSourceProvider.description", metatype = true, configurationFactory = true, policy = ConfigurationPolicy.REQUIRE, createPid = true)
 @Service(value = DataSourceProvider.class, serviceFactory = true)
 @Properties(value = {
 		@Property(name = "dataSourceName", value = "default", description = "%dataSourceName"),
 		@Property(name = "caption", value = "Default", description = "%caption"),
 		@Property(name = "driverClassName", value = "com.mysql.jdbc.Driver", description = "%driverClassName"),
 		@Property(name = "url", value = "jdbc:mysql://localhost:3306/database?characterSetResults=UTF-8&characterEncoding=UTF-8&useUnicode=yes", description = "%dataSource.jdbc.connectString.description"),
 		@Property(name = "username", value = "user", description = "%username"),
 		@Property(name = "password", value = "password", description = "%password"),
 		@Property(name = "connectionProperties", value = "connectionProperties", description = "%connectionProperties"),
 		@Property(name = "defaultTransactionIsolation",
 			value = DataSourceProviderImpl.ISOLATION_LEVEL_READ_COMMITTED, 
 			options = { 
 				@PropertyOption(name = DataSourceProviderImpl.ISOLATION_LEVEL_NONE, value = "None"),
 				@PropertyOption(name = DataSourceProviderImpl.ISOLATION_LEVEL_READ_COMMITTED, value = "Read-Committed"), 
 				@PropertyOption(name = DataSourceProviderImpl.ISOLATION_LEVEL_READ_UNCOMMITTED, value = "Read-Uncommitted"),
 				@PropertyOption(name = DataSourceProviderImpl.ISOLATION_LEVEL_REPEATABLE_READ, value = "Repeatable-Read"), 
 				@PropertyOption(name = DataSourceProviderImpl.ISOLATION_LEVEL_SERIALIZABLE, value = "Serializable") }, 
 			description = "%defaultTransactionIsolation"),
 		@Property(name = "defaultAutoCommit", boolValue = false, description = "%connectionProperties"), @Property(name = "defaultReadOnly", boolValue = false, description = "%defaultReadOnly"), @Property(name = "initialSize", intValue = 0, description = "%initialSize"),
 		@Property(name = "maxActive", intValue = 8, description = "%maxActive"), @Property(name = "maxIdle", intValue = 8, description = "%maxIdle"), @Property(name = "maxWait", intValue = -1, description = "%maxWait"), @Property(name = "validationQuery", value = "", description = "%validationQuery") })
 public class DataSourceProviderImpl implements DataSourceProvider {
 
 	public enum IsolationLevel {
 		NONE(java.sql.Connection.TRANSACTION_NONE), READ_COMMITTED(java.sql.Connection.TRANSACTION_READ_COMMITTED), READ_UNCOMMITTED(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED), REPEATABLE_READ(java.sql.Connection.TRANSACTION_REPEATABLE_READ), SERIALIZABLE(java.sql.Connection.TRANSACTION_SERIALIZABLE);
 		int level;
 
 		private IsolationLevel(int level) {
 			this.level = level;
 		}
 
 		int getLevel() {
 			return level;
 		}
 	}
 
 	public static final String ISOLATION_LEVEL_NONE = "NONE";
 	public static final String ISOLATION_LEVEL_READ_COMMITTED = "READ_COMMITTED";
 	public static final String ISOLATION_LEVEL_READ_UNCOMMITTED = "READ_UNCOMMITTED";
 	public static final String ISOLATION_LEVEL_REPEATABLE_READ = "REPEATABLE_READ";
 	public static final String ISOLATION_LEVEL_SERIALIZABLE = "SERIALIZABLE";
 
 	private static final Logger log = LoggerFactory.getLogger(DataSourceProviderImpl.class);
 	//private static HashMap<String, BasicDataSource> dataSources = new HashMap(); 
 	private BasicDataSource ds;
 	private String dataSourceName = null;
 	
 	private String driverClassName = null;
 	private String url = null;
 	private String caption = null;
 	private String username = null;
 	private String password = null;
 	private String connectionProperties = null;
 	private Boolean defaultAutoCommit = null;
 	private Boolean defaultReadOnly = null;
 	private Integer defaultTransactionIsolation = null;
 	private Integer initialSize = null;
 	private Integer maxActive = null;
 	private Integer maxIdle = null;
 	private Integer maxWait = null;
 	private String validationQuery = null;
 
 
 	@Reference
	DynamicClassLoaderManager dynamicClassLoaderManager;
 	
 	@Activate
 	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
 		driverClassName = (String) props.get("driverClassName");
 		url = (String) props.get("url");
 		caption = (String) props.get("caption");
 		username = (String) props.get("username");
 		password = (String) props.get("password");
 		connectionProperties = (String) props.get("connectionProperties");
 		defaultAutoCommit = (Boolean) props.get("defaultAutoCommit");
 		defaultReadOnly = (Boolean) props.get("defaultReadOnly");
 		defaultTransactionIsolation = IsolationLevel.valueOf((String) props.get("defaultTransactionIsolation")).getLevel();
 		initialSize = (Integer) props.get("initialSize");
 		maxActive = (Integer) props.get("maxActive");
 		maxIdle = (Integer) props.get("maxIdle");
 		maxWait = (Integer) props.get("maxWait");
 		validationQuery = (String) props.get("validationQuery");
 		dataSourceName = (String) props.get("dataSourceName");
 		if (dataSourceName != null && !"".equalsIgnoreCase(dataSourceName)) {
 			ds = new BasicDataSource();
//			ds.setDriverClassLoader(dynamicClassLoaderManager.getDynamicClassLoader());
 			
 			log.info("Registering DataSource Name: " + dataSourceName + " PID: " + (String) props.get("service.pid"));
 
			Thread.currentThread().setContextClassLoader(dynamicClassLoaderManager.getDynamicClassLoader());
 			ds.setDriverClassName(driverClassName);
 			ds.setUrl(url);
 			ds.setUsername(username);
 			ds.setPassword(password);
 			ds.setConnectionProperties(connectionProperties);
 			ds.setDefaultAutoCommit(defaultAutoCommit);
 			ds.setDefaultReadOnly(defaultReadOnly);
 			ds.setDefaultTransactionIsolation(defaultTransactionIsolation);
 			ds.setInitialSize(initialSize);
 			ds.setMaxActive(maxActive);
 			ds.setMaxIdle(maxIdle);
 			ds.setMaxWait(maxWait);
 			ds.setValidationQuery(validationQuery);
 			try {
 				ds.setLogWriter(
 						new PrintWriter(
 								new Writer() {
 					
 									@Override
 									public void write(char[] cbuf, int off, int len) throws IOException {
 										log.info(new String(cbuf));
 									}
 									
 									@Override
 									public void flush() throws IOException {
 									}
 									
 									@Override
 									public void close() throws IOException {
 									}
 								}
 						) { 
 							
 						}
 						);
 			} catch (SQLException e) {
 				log.error("activate", e);
 			}
 
 		} else {
 			log.warn("No data source name is defined PID: " + (String) props.get("service.pid"));
 		}
 	}
 
 	@Deactivate
 	protected void deactivate() {
 		try {
 			if (ds != null) {
 				ds.close();
 			}
 		} catch (SQLException e) {
 			log.error("Deactivate data source: " + dataSourceName, e);
 		}
 	}
 
 	public Connection getConnection() throws SQLException {
 		return ds.getConnection();
 	}
 
 	public Connection getConnection(String userName, String password) throws SQLException {
 		return ds.getConnection(userName, password);
 	}
 
 	public QueryRunner getQueryRunner() {
 		return new QueryRunner(ds, false);
 	}
 
 	public DataSource getDataSource() {
 		return ds;
 	}
 
 	public String getName() {
 		return dataSourceName;
 	}
 
 	public BasicDataSource getDs() {
 		return ds;
 	}
 
 	public String getDataSourceName() {
 		return dataSourceName;
 	}
 
 	public String getDriverClassName() {
 		return driverClassName;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public String getCaption() {
 		return caption;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public String getConnectionProperties() {
 		return connectionProperties;
 	}
 
 	public Boolean getDefaultAutoCommit() {
 		return defaultAutoCommit;
 	}
 
 	public Boolean getDefaultReadOnly() {
 		return defaultReadOnly;
 	}
 
 	public Integer getDefaultTransactionIsolation() {
 		return defaultTransactionIsolation;
 	}
 
 	public Integer getInitialSize() {
 		return initialSize;
 	}
 
 	public Integer getMaxActive() {
 		return maxActive;
 	}
 
 	public Integer getMaxIdle() {
 		return maxIdle;
 	}
 
 	public Integer getMaxWait() {
 		return maxWait;
 	}
 
 	public String getValidationQuery() {
 		return validationQuery;
 	}
 }
