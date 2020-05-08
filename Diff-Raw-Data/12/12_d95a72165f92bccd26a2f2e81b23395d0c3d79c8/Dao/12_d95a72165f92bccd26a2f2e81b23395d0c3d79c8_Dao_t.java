 package cz.cuni.mff.odcleanstore.webfrontend.dao;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.mail.MessagingException;
 import javax.sql.DataSource;
 
 import org.apache.log4j.Logger;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.AbstractPlatformTransactionManager;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 import org.springframework.transaction.support.TransactionCallbackWithoutResult;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import cz.cuni.mff.odcleanstore.util.CodeSnippet;
 import cz.cuni.mff.odcleanstore.webfrontend.core.DaoLookupFactory;
import cz.cuni.mff.odcleanstore.webfrontend.dao.exceptions.DaoException;
 
 /**
  * Generic DAO interface.
  * 
  * @author Dušan Rychnovský (dusan.rychnovsky@gmail.com)
  *
  */
 public abstract class Dao implements Serializable
 {
 	public static final String TABLE_NAME_PREFIX = "DB.ODCLEANSTORE.";
 	public static final String BACKUP_TABLE_PREFIX = "BACKUP_";
 	
 	private static final long serialVersionUID = 1L;
 	
 	protected static Logger logger = Logger.getLogger(Dao.class);
 	
 	private static List<DaoExceptionHandler> exceptionHandlers = new LinkedList<DaoExceptionHandler>();
 	
 	private DaoLookupFactory lookupFactory;
 	private transient JdbcTemplate cleanJDBCTemplate;
 	private transient JdbcTemplate dirtyJDBCTemplate;
 	private transient TransactionTemplate cleanTransactionTemplate;
 	
 	static 
 	{
 		exceptionHandlers.add(new UniquenessViolationHandler());
 		exceptionHandlers.add(new NonUniquePrimaryKeyHandler());
 	}
 	
 	/**
 	 * 
 	 * @param lookupFactory
 	 */
 	public void setDaoLookupFactory(DaoLookupFactory lookupFactory)
 	{
 		this.lookupFactory = lookupFactory;
 	}
 	
 	private JdbcTemplate getJdbcTemplate()
 	{
 		return getCleanJdbcTemplate();
 	}
 
 	private JdbcTemplate getJdbcTemplate(EnumDatabaseInstance dbInstance)
 	{
 		switch (dbInstance)
 		{
 		case CLEAN:
 			return getCleanJdbcTemplate();
 		case DIRTY:
 			return getDirtyJdbcTemplate();
 		default:
 			throw new AssertionError("Unknown database instance");
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private JdbcTemplate getCleanJdbcTemplate()
 	{
 		if (cleanJDBCTemplate == null)
 		{
 			DataSource dataSource = lookupFactory.getCleanDataSource();
 			cleanJDBCTemplate = new JdbcTemplate(dataSource);
 		}
 		
 		return cleanJDBCTemplate;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	private JdbcTemplate getDirtyJdbcTemplate()
 	{
 		if (dirtyJDBCTemplate == null)
 		{
 			DataSource dataSource = lookupFactory.getDirtyDataSource();
 			dirtyJDBCTemplate = new JdbcTemplate(dataSource);
 		}
 		
 		return dirtyJDBCTemplate;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	private TransactionTemplate getCleanTransactionTemplate()
 	{
 		if (cleanTransactionTemplate == null)
 		{
 			AbstractPlatformTransactionManager manager = lookupFactory.getCleanTransactionManager();
 			cleanTransactionTemplate = new TransactionTemplate(manager);
 			cleanTransactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
 		}
 		
 		return cleanTransactionTemplate;
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
 	
 	protected <E> List<E> jdbcQuery(String sql, RowMapper<E> rowMapper) throws DataAccessException
 	{
 		return getJdbcTemplate().query(sql, rowMapper);
 	}
 
 	protected <E> List<E> jdbcQuery(String sql, Object[] args, RowMapper<E> rowMapper) throws DataAccessException
 	{
 		return getJdbcTemplate().query(sql, args, rowMapper);
 	}
 
 	protected <E> E jdbcQueryForObject(String sql, RowMapper<E> rowMapper) throws DataAccessException
 	{
 		return getJdbcTemplate().queryForObject(sql, rowMapper);
 	}
 
 	protected <E> E jdbcQueryForObject(String sql, Object[] args, RowMapper<E> rowMapper) throws DataAccessException
 	{
 		return getJdbcTemplate().queryForObject(sql, args, rowMapper);
 	}
 
 	public <E> E jdbcQueryForObject(String sql, Object[] args, Class<E> requiredType) throws DataAccessException
 	{
 		return getJdbcTemplate().queryForObject(sql, args, requiredType);
 	}
 	
 	protected <E> List<E> jdbcQueryForList(String sql, Class<E> elementType) throws DataAccessException
 	{
 		return getJdbcTemplate().queryForList(sql, elementType);
 	}
 	
 	protected int jdbcQueryForInt(String sql) throws DataAccessException 
 	{
 		return getJdbcTemplate().queryForInt(sql);
 	}
 	
 	protected int jdbcQueryForInt(String sql, Object... args) throws DataAccessException 
 	{
 		return getJdbcTemplate().queryForInt(sql, args);
 	}
 
 	protected int jdbcQueryForInt(String sql, Object[] args, EnumDatabaseInstance dbInstance) throws Exception
 	{
 		try
 		{
 			return getJdbcTemplate(dbInstance).queryForInt(sql, args);
 		}
 		catch (Exception e)
 		{
 			handleException(e);
 			throw e;
 		}
 	}
 	
 	protected int jdbcUpdate(final String sql) throws Exception
 	{
 		try
 		{
 			return getJdbcTemplate().update(sql);
 		}
 		catch (Exception e)
 		{
 			handleException(e);
 			throw e;
 		}
 	}
 
 	protected int jdbcUpdate(String sql, Object... args) throws Exception
 	{
 		return this.jdbcUpdate(sql, args, EnumDatabaseInstance.CLEAN);
 	}
 
 	protected int jdbcUpdate(String sql, Object[] args, EnumDatabaseInstance dbInstance) throws Exception
 	{
 		try
 		{
 			return getJdbcTemplate(dbInstance).update(sql, args);
 		}
 		catch (Exception e)
 		{
 			handleException(e);
 			throw e;
 		}
 	}
 
 	protected void executeInTransaction(final CodeSnippet code) throws Exception
 	{
 		try
 		{
 			getCleanTransactionTemplate().execute(new TransactionCallbackWithoutResult()
 			{
 				@Override
 				protected void doInTransactionWithoutResult(TransactionStatus status)
 				{
 					try
 					{
 						code.execute();
 					}
 					catch (Exception ex)
 					{
 						throw new RuntimeException(ex.getMessage(), ex);
 					}
 				}
 			});
 		}
 		catch (Exception ex)
 		{
 			handleException(ex);
 			throw ex;
 		}
 	}
 	
 	private void handleException(Exception ex) throws Exception
 	{
 		handleMessagingException(ex);
 		handleDAOException(ex);
 		
 		// if neither of the routines above handled the exception (e.g. they
 		// did not throw any exceptions), re-throw the exception unchanged
 		throw ex;
 	}
 	
 	/**
 	 * 
 	 * @param ex
 	 * @throws Exception
 	 */
 	private void handleMessagingException(Exception ex) throws Exception
 	{
 		if (ex instanceof RuntimeException)
 		{
 			Throwable innerEx = ex.getCause();
 			if (innerEx != null && (innerEx instanceof MessagingException))
 			{
 				throw (MessagingException) innerEx;
 			}
 		}
 	}
 	
 	private void handleDAOException(Exception ex) throws Exception
 	{
		// throw the exception out right away if it has been already processed before
		// (this is necessary as an exception might get processed twice - once in
		// the jdbcUpdate method and then again in the executeInTransaction method)
		//
		if ((ex.getCause() != null) && (ex.getCause() instanceof DaoException))
			throw (DaoException) ex.getCause();
		
		// throw the exeption out right away if it's message does not bear a virtuoso
		// failure number and therefore cannot be processed using DAO exception handlers)
		//
 		String relevantMessagePart = getRelevantMessagePart(ex.getMessage());
 		if (relevantMessagePart == null)
 			throw ex;
 		
 		for (DaoExceptionHandler handler : exceptionHandlers)
 		{
 			if (!handler.comprisesException(relevantMessagePart))
 				continue;
 			
 			handler.handleException(relevantMessagePart);
 			return;
 		}
 	}
 	
 	private String getRelevantMessagePart(String originalMessage)
 	{
 		String[] tokens = originalMessage.split("; ");
 		
 		for (String token : tokens)
 		{
 			if (token.startsWith("SR"))
 				return token;
 		}
 		
 		return null;
 	}
 	
 	protected DaoLookupFactory getLookupFactory() 
 	{
 		return lookupFactory;
 	}
 }
