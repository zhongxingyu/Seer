 package hemera.utility.sql.query;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import hemera.utility.sql.SQLSource;
 import hemera.utility.sql.SQLSourceManager;
 import hemera.utility.sql.interfaces.IQuery;
 
 /**
  * <code>AbstractQuery</code> defines the abstraction
  * of a database query that internally utilizes a its
  * own <code>PreparedStatement</code> instance.
  * <p>
  * <code>AbstractQuery</code> internally manages all
  * the obtained resources include database connection
  * and query prepared statement. When an invocation
  * of <code>close</code> occurs on a query instance,
  * the query automatically releases these resources.
  * <p>
  * <code>AbstractQuery</code> also defines necessary
  * methods that should be used during the execution
  * of a query. Subclass implementations should utilize
  * these abstract procedures to form their execution
  * logic. Typically subclass implementation should
  * directly invoke <code>prepareStatement</code> method
  * to retrieve a statement that is fully prepared for
  * execution. Then the implementation can just perform
  * the appropriate execution using the statement.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 public abstract class AbstractQuery implements IQuery {
 	/**
 	 * The <code>String</code> key used to identify
 	 * the data source.
 	 */
 	protected final String key;
 	/**
 	 * The <code>AtomicInteger</code> of retry count.
 	 */
 	protected final AtomicInteger retryCount;
 	/**
 	 * The <code>SQLSource</code> instance.
 	 */
 	protected SQLSource source;
 	/**
 	 * The <code>Connection</code> resource.
 	 */
 	private Connection connection;
 	/**
 	 * The <code>PreparedStatement</code> resource.
 	 */
 	private PreparedStatement statement;
 	
 	/**
 	 * Constructor of <code>AbstractQuery</code>.
 	 * @param key The <code>String</code> key used to
 	 * identify the data source.
 	 */
 	protected AbstractQuery(final String key) {
 		this.key = key;
 		this.retryCount = new AtomicInteger();
 	}
 
 	@Override
 	public PreparedStatement prepareStatement() throws SQLException {
 		this.source = SQLSourceManager.instance.getSource(this.key);
		if (this.source == null) {
			throw new RuntimeException("There is no such data source: " + this.key);
		}
 		this.connection = this.source.datasource.getConnection();
		final String template = this.buildTemplate();
 		this.statement = this.connection.prepareStatement(template);
 		this.insertValues(this.statement);
 		return this.statement;
 	}
 	
 	/**
 	 * Construct the template to be used for current
 	 * query execution. This process typically forms
 	 * the structure of the query by constructing a
 	 * string with database name, table name, column
 	 * names and general SQL syntax. The values in the
 	 * query should be left out of the returned string
 	 * as it is not part of the template.
 	 * <p>
 	 * This method defines the first stage of query
 	 * execution.
 	 * @return The <code>String</code> query template.
 	 */
 	protected abstract String buildTemplate();
 	
 	/**
 	 * Insert the values of the current query into the
 	 * given statement for query execution.
 	 * <p>
 	 * This method defines the second stage of query
 	 * execution. The given statement is pooled using
 	 * the first stage query template, and this method
 	 * inserts the values into the template.
 	 * @param statement The <code>PreparedStatement</code>
 	 * based on the first stage template.
 	 * @throws SQLException If value insertion failed.
 	 */
 	protected abstract void insertValues(final PreparedStatement statement) throws SQLException;
 	
 	@Override
 	public void close() throws SQLException {
 		if (this.connection != null) {
 			try {
 				this.connection.close();
 			} finally {
 				if (this.statement != null) {
 					this.statement.close();
 				}
 			}
 		}
 	}
 	
 	@Override
 	public int getAndIncrementRetryCount() {
 		return this.retryCount.getAndIncrement();
 	}
 }
