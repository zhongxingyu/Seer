 package cz.cuni.mff.odcleanstore.webfrontend.dao.en;
 
import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.AbstractPlatformTransactionManager;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 import org.springframework.transaction.support.TransactionCallbackWithoutResult;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import cz.cuni.mff.odcleanstore.webfrontend.core.DaoLookupFactory;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.Dao;
 
public class OfficialPipelinesDao implements Serializable
 {
 	//private static Logger logger = Logger.getLogger(OfficialPipelinesDao.class);
 	
	private static final long serialVersionUID = 1L;

 	// note that the tables must be listed in the correct order 
 	// to avoid errors when deleting a table which is referenced
 	// by a foreign key from another table
 	private static final String[] RELATED_TABLES_NAMES = 
 	{
 		"OI_RULES_ASSIGNMENT",
 		"QA_RULES_ASSIGNMENT",
 		"DN_RULES_ASSIGNMENT",
 		"TRANSFORMER_INSTANCES",
 		"TRANSFORMERS",
 		"PIPELINES"
 	};
 	
 	protected DaoLookupFactory lookupFactory;
 	
 	private transient JdbcTemplate jdbcTemplate;
 	private transient TransactionTemplate transactionTemplate;
 
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
 			DataSource dataSource = lookupFactory.getCleanDataSource();
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
 	
 	public void commitPipelinesRelatedTables() throws Exception
 	{
 		try
 		{
 			getTransactionTemplate().execute(new TransactionCallbackWithoutResult() 
 			{
 				@Override
 				protected void doInTransactionWithoutResult(TransactionStatus status) 
 				{
 					try 
 					{
 						List<String> tableNames = Arrays.asList(RELATED_TABLES_NAMES);
 						truncateTables(tableNames);
 						
 						Collections.reverse(tableNames);
 						fillTables(tableNames);
 					}
 					catch (Exception ex) {
 						throw new RuntimeException(ex.getMessage(), ex);
 					}
 				}
 			});
 		}
 		catch (Exception ex)
 		{
 			throw new Exception(
 				"Could not commit pipelines-related tables, due to: " + ex.getMessage()
 			);
 		}	
 	}
 	
 	private void fillTables(List<String> tableNames)
 	{
 		for (String tableName : tableNames)
 			fillTable(tableName);
 	}
 	
 	private void fillTable(String tableNameSuffix)
 	{
 		String officialTableName = constructOfficialTableName(tableNameSuffix);
 		String backupTableName = constructBackupTableName(tableNameSuffix);
 		
 		String query = "INSERT INTO " + officialTableName + " SELECT * FROM " + backupTableName;
 		getJdbcTemplate().update(query);
 	}
 	
 	private void truncateTables(List<String> tableNames)
 	{
 		for (String tableName : tableNames)
 			truncateTable(tableName);
 	}
 	
 	private void truncateTable(String tableNameSuffix)
 	{
 		String officialTableName = constructOfficialTableName(tableNameSuffix);
 		
 		getJdbcTemplate().update("DELETE FROM " + officialTableName);
 		getJdbcTemplate().update("set_identity_column('" + officialTableName + "','id', 1)");
 	}
 	
 	private String constructOfficialTableName(String nameSuffix)
 	{
 		return Dao.TABLE_NAME_PREFIX + nameSuffix;
 	}
 	
 	private String constructBackupTableName(String nameSuffix)
 	{
 		return Dao.TABLE_NAME_PREFIX + Dao.BACKUP_TABLE_PREFIX + nameSuffix;
 	}
 }
