 import javax.sql.DataSource;
 
 import no.antares.dbunit.Db;
 import no.antares.dbunit.DbDataSource;
 
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.transaction.TransactionDefinition;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 
 /** Get at beans in Spring Config
  * @author tommy skodje
 */
 public class SpringWiring {
 
 	/** Hard-coded parameters I found in Spring config */
 	private final DbDataSource dbDataSource	= new DbDataSource(
 			"net.sourceforge.jtds.jdbc.Driver",
 			"jdbc:jtds:sybase://URL",
 			"USER",
 			"PASSWORD",
 			""
 		);
     DataSourceTransactionManager tm = null;   
 	TransactionStatus status = null;
 
 	public Db db() {
 		return dbDataSource;
 	}
 
 	/** Using development Spring configuration */
 	public SpringWiring() {
		this( Constants.TS_XML_BEANS_RESOURCE_NAME, "dataSource" );
 	}
 	/** Using specified Spring configuration */
 	private SpringWiring( String springConfig, String dataSourceBean ) {
 		// this.dbDataSource	= getDataSource( getBeanFactory( springConfig ), dataSourceBean );
 	}
 
 	public DataSource getDataSource() {
 		return dbDataSource.ds(); 	// getDataSource(beanFactory);
 	}
 
     /** Set up and start a transaction */
 	public void startTransaction( int timeOutSeconds ) {
 		tm = new DataSourceTransactionManager( getDataSource() );
 		DefaultTransactionDefinition td = new DefaultTransactionDefinition( TransactionDefinition.PROPAGATION_REQUIRED );
 		td.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
 		td.setName( "TESTING_NPSIA_SERVICE_ELBANK" );
 		td.setTimeout( timeOutSeconds );
 		status = tm.getTransaction(td);
 		// tm.setTransactionSynchronizationName( "TESTING_DAO_FEED_DB" );
 	}
 
 	public void rollBackTransaction() {
 		tm.rollback(status);
 		status = null;
 	}
 
 	private DataSource getDataSource( XmlBeanFactory beanFactory, String dataSourceBean ) {
         DataSource dataSource = (DataSource) beanFactory.getBean( dataSourceBean );
         if (null == dataSource) {
             throw new IllegalArgumentException("Unable to find datasource: " + dataSourceBean );
         }
         return dataSource;
     }
 
     private XmlBeanFactory getBeanFactory(String resourceName) {
         Resource resource = new ClassPathResource(resourceName);
         XmlBeanFactory beanFactory = new XmlBeanFactory(resource);
         return beanFactory;
     }
 
 }
