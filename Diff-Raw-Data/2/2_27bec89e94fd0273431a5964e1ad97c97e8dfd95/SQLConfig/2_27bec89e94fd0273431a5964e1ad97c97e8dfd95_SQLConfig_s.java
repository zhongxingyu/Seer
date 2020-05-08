 package hemera.utility.sql.config;
 
 /**
  * <code>ESQLConfig</code> defines the enumeration of
  * configuration values used for MySQL database server
  * connection.
  * <p>
  * Please refer to 
  * <a href=http://commons.apache.org/dbcp/configuration.html>
  * Apache Commons DBCP configuration</a>
  * for the detailed explanations. The values in this
  * configuration should be set before any data source
  * is created.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 public enum SQLConfig {
 	/**
 	 * The <code>boolean</code> indicating if auto
 	 * commit is enabled. The default value is
 	 * <code>true</code>.
 	 */
 	AutoCommit(true),
 	/**
 	 * The <code>int</code> initial connection pool
 	 * size. The default value is <code>0</code>.
 	 */
 	InitialPoolSize(0),
 	/**
 	 * The <code>int</code> maximum number of all
 	 * connections allowed in the pool. Values less
 	 * than 0 means no limit. The default value is
 	 * <code>64</code>.
 	 */
 	MaxPoolSize(64),
 	/**
 	 * The <code>int</code> maximum number of idle
 	 * connections allowed in the pool. Values less
 	 * than 0 means no limit. The default value is
 	 * <code>64</code>.
 	 */
 	MaxIdleSize(64),
 	/**
 	 * The <code>int</code> minimum number of idle
 	 * connections must be maintained in the pool.
 	 * The default value is <code>0</code>.
 	 */
 	MinIdleSize(0),
 	/**
 	 * The <code>long</code> maximum time a connection
 	 * retrieval will wait for an available connection
 	 * before throwing an exception in milliseconds.
 	 * -1 means wait indefinitely. The default value
 	 * is <code>20000</code>.
 	 */
	MaxWaitTime(20000),
 	/**
 	 * The <code>String</code> validation query used
 	 * to test connection before being retrieved or
 	 * returned to the pool. This value is used with
 	 * <code>TestOnBorrow</code>, <code>TestOnReturn</code>
 	 * and <code>TestWhileIdle</code> values. The
 	 * default value is <code>null</code>.
 	 */
 	ValidationQuery(null),
 	/**
 	 * The <code>boolean</code> indicating if a
 	 * connection should be tested before being borrowed
 	 * from the pool. The <code>ValidationQuery</code>
 	 * value must be set for this value to take effect.
 	 * The default value is <code>false</code>.
 	 */
 	TestOnBorrow(false),
 	/**
 	 * The <code>boolean</code> indicating if a
 	 * connection should be tested before being returned
 	 * to the pool. The <code>ValidationQuery</code>
 	 * value must be set for this value to take effect.
 	 * The default value is <code>false</code>.
 	 */
 	TestOnReturn(false),
 	/**
 	 * The <code>boolean</code> indicating if a
 	 * connection should be tested while idling in the
 	 * pool. The <code>ValidationQuery</code> value
 	 * must be set for this value to take effect. The
 	 * default value is <code>false</code>.
 	 */
 	TestWhileIdle(false),
 	/**
 	 * The <code>boolean</code> indicating if query
 	 * statements should be pooled. The default value
 	 * is <code>false</code>. Pooling statements can
 	 * cause database to run out of cursors.
 	 */
 	PoolQueryStatements(false),
 	/**
 	 * The <code>boolean</code> indicating if the pool
 	 * should remove abandoned connections. An abandoned
 	 * connection is one that is not being used and is
 	 * not closed. Traversing a result set does not count
 	 * as being used. This value should only be turned
 	 * on to prevent badly written applications that
 	 * fail to close the query. The default value is
 	 * <code>false</code>.
 	 */
 	RemoveAbandoned(false),
 	/**
 	 * The <code>int</code> timeout value in seconds
 	 * used to determine when an abandoned connection
 	 * should be removed. Once a connection becomes
 	 * abandoned, it is removed after this timeout
 	 * value elapses. During which time, the connection
 	 * may be recollected. The default value is 300.
 	 */
 	RemoveAbandonedTimeout(300),
 	/**
 	 * The <code>boolean</code> indicating if the code
 	 * that abandons connections should be logged. This
 	 * value will decrease application performance, and
 	 * therefore should only be used during testing to
 	 * check of error code. The default value is
 	 * <code>false</code>. 
 	 */
 	LogAbandonedCode(false),
 	/**
 	 * The <code>int</code> query retry limit. The
 	 * default is 3.
 	 */
 	Query_RetryLimit(3);
 	
 	/**
 	 * The <code>Object</code> value.
 	 */
 	private volatile Object value;
 	
 	/**
 	 * Constructor of <code>ESQLConfig</code>.
 	 * @param value The default <code>Object</code>
 	 * value.
 	 */
 	private SQLConfig(final Object value) {
 		this.value = value;
 	}
 
 	/**
 	 * Set the database configuration value to given
 	 * value.
 	 * <p>
 	 * This method guarantees the memory consistency
 	 * of the value.
 	 * @param value The <code>Object</code> value to
 	 * set to.
 	 */
 	public void set(final Object value) {
 		this.value = value;
 	}
 	
 	/**
 	 * Retrieve the set configuration value.
 	 * <p>
 	 * This method guarantees the memory consistency
 	 * of the value.
 	 * @return The <code>Object</code> value.
 	 */
 	public Object value() {
 		return this.value;
 	}
 }
