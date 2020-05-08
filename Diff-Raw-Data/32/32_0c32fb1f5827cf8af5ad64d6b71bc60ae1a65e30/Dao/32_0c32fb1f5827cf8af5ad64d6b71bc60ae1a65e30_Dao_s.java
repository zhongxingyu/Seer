 package cz.cuni.mff.odcleanstore.webfrontend.dao;
 
 import cz.cuni.mff.odcleanstore.util.CodeSnippet;
 import cz.cuni.mff.odcleanstore.util.Pair;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.BusinessEntity;
 import cz.cuni.mff.odcleanstore.webfrontend.core.DaoLookupFactory;
 
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 import org.springframework.transaction.support.AbstractPlatformTransactionManager;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * Generic DAO interface.
  * 
  * @author Dušan Rychnovský (dusan.rychnovsky@gmail.com)
  *
  */
 public abstract class Dao<T extends BusinessEntity> implements Serializable
 {
 	public static final String TABLE_NAME_PREFIX = "DB.ODCLEANSTORE.";
 	public static final String BACKUP_TABLE_PREFIX = "BACKUP_";
 	
 	private static final long serialVersionUID = 1L;
 	
	private static Logger logger = Logger.getLogger(Dao.class);
 	
 	protected DaoLookupFactory lookupFactory;
 	
 	private transient JdbcTemplate jdbcTemplate;
 	private transient TransactionTemplate transactionTemplate;
 	
 	/**
 	 * 
 	 * @return
 	 */
 	protected abstract String getTableName();
 	
 	/**
 	 * 
 	 * @return
 	 */
 	protected abstract ParameterizedRowMapper<T> getRowMapper();
 	
 	/**
 	 * 
 	 * @param lookupFactory
 	 */
 	public void setDaoLookupFactory(DaoLookupFactory lookupFactory)
 	{
 		this.lookupFactory = lookupFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	protected JdbcTemplate getJdbcTemplate()
 	{
 		if (jdbcTemplate == null)
 		{
 			DataSource dataSource = lookupFactory.getDataSource();
 			jdbcTemplate = new JdbcTemplate(dataSource);
 		}
 		
 		return jdbcTemplate;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	protected TransactionTemplate getTransactionTemplate()
 	{
 		if (transactionTemplate == null)
 		{
 			AbstractPlatformTransactionManager manager = lookupFactory.getTransactionManager();
 			transactionTemplate = new TransactionTemplate(manager);
 			transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
 		}
 		
 		return transactionTemplate;
 	}
 	
 	/**
 	 * 
 	 * @param value
 	 */
 	protected static int boolToSmallint(boolean value)
 	{
 		if (value)
 			return 1;
 		else
 			return 0;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public List<T> loadAllRaw()
 	{
 		String query = "SELECT * FROM " + getTableName();
 		return getJdbcTemplate().query(query, getRowMapper());
 	}
 	
 	public T loadFirstRaw()
 	{
 		String query = "SELECT TOP 1 * FROM " + getTableName();
 		return getJdbcTemplate().queryForObject(query, getRowMapper());
 	}
 	
 	/**
 	 * 
 	 * @param columnName
 	 * @param value
 	 * @return
 	 */
 	public List<T> loadAllRawBy(String columnName, Object value)
 	{
 		String query = "SELECT * FROM " + getTableName() + " WHERE " + columnName + " = ?";
 		Object[] params = { value };
 		
 		return getJdbcTemplate().query(query, params, getRowMapper());
 	}
 	
 	/**
 	 * Finds all entities in the database.
 	 * 
 	 * @return
 	 * @throws Exception 
 	 */
 	public List<T> loadAll()
 	{
 		return loadAllRaw();
 	}
 	
 	/**
 	 * 
 	 * @param criteria
 	 * @return
 	 */
 	public List<T> loadAllBy(QueryCriteria criteria)
 	{
 		String query = "SELECT * FROM " + getTableName() + " WHERE " + criteria.joinToString();
 		Object[] params = criteria.getRange();
 		
 		return getJdbcTemplate().query(query, params, getRowMapper());
 	}
 	
 	/**
 	 * 
 	 * @param columnName
 	 * @param value
 	 * @return
 	 */
 	public T loadRawBy(String columnName, Object value)
 	{
 		String query = "SELECT * FROM " + getTableName() + " WHERE " + columnName + " = ?";
 		Object[] params = { value };
 		
 		return (T) getJdbcTemplate().queryForObject(query, params, getRowMapper());
 	}
 	
 	/**
 	 * Finds the entity with the given id in the database.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public T loadBy(String columnName, Object value)
 	{
 		return loadRawBy(columnName, value);
 	}
 	
 	/**
 	 * Saves the given item in the database.
 	 * 
 	 * @param item
 	 */
 	public void save(T item) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot insert rows into table:" + getTableName() + "."
 		);
 	}
 	
 	/**
 	 * 
 	 * @param item
 	 * @param doAfter
 	 * @throws Exception
 	 */
 	public void save(T item, CodeSnippet doAfter) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot insert rows into table:" + getTableName() + "."
 		);
 	}
 	
 	/**
 	 * Updates the given item in the database.
 	 * 
 	 * @param item
 	 * @throws Exception 
 	 */
 	public void update(T item) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot update rows in table: " + getTableName() + "."
 		);
 	}
 	
 	/**
 	 * 
 	 * @param item
 	 * @param doAfter
 	 * @throws Exception
 	 */
 	public void update(T item, CodeSnippet doAfter) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot update rows in table: " + getTableName() + "."
 		);
 	}
 	
 	/**
 	 * 
 	 * @param item
 	 * @throws Exception
 	 */
 	public void delete(T item) throws Exception
 	{
 		throw new UnsupportedOperationException(
 			"Cannot delete rows from table: " + getTableName() + "."
 		);
 	}
 }
