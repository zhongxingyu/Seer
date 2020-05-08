 /**
  * 
  */
 package uk.org.vacuumtube.spring;
 
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.orm.hibernate4.HibernateTransactionManager;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
 
 import uk.org.vacuumtube.dao.StatsDao;
 import uk.org.vacuumtube.hibernate.MySqlFixInterceptor;
 import uk.org.vacuumtube.hibernate.StatsDaoImpl;
 import uk.org.vacuumtube.service.StatsDatabaseService;
 import uk.org.vacuumtube.service.StatsDatabaseServiceImpl;
 
 /**
  * @author clivem
  *
  */
 @Configuration
 public class AppConfiguration {
 
 	@Value("#{dataSource}")
 	private DataSource dataSource;
 
 	@Bean(name = "sessionFactory")
 	public LocalSessionFactoryBean sessionFactory() {
 		Properties props = new Properties();
 		// props.put("hibernate.dialect",
		// org.hibernate.dialect.MySQLDialect.class.getName());
 		props.put("hibernate.dialect",
 				org.hibernate.dialect.MySQL5InnoDBDialect.class.getName());
 		props.put("hibernate.show_sql", "true");
 
 		LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
 		bean.setPackagesToScan("uk.org.vacuumtube.dao");
 		//bean.setMappingResources(new String[] {"META-INF/stats.hbm.xml", "META-INF/notes.hbm.xml"});
 		bean.setHibernateProperties(props);
 		bean.setDataSource(this.dataSource);
 		bean.setEntityInterceptor(new MySqlFixInterceptor());
 		return bean;
 	}
 
 	@Bean(name = "transactionManager")
 	public HibernateTransactionManager transactionManager() {
 		return new HibernateTransactionManager(sessionFactory().getObject());
 	}
 
 	@Bean(name = "hibernateStatsDao")
 	public StatsDao statsDao() {
 		StatsDaoImpl statsDaoImpl = new StatsDaoImpl();
 		statsDaoImpl.setSessionFactory(sessionFactory().getObject());
 		return statsDaoImpl;
 	}
 	
 	@Bean(name = "statsDatabaseService")
 	public StatsDatabaseService statsDatabaseService() {
 		StatsDatabaseServiceImpl impl = new StatsDatabaseServiceImpl();
 		impl.setStatsDao(statsDao());
 		return impl;
 	}
 }
