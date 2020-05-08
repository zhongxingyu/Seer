 package com.mjeanroy.springhub.configuration;
 
 import static java.util.Collections.emptyMap;
 
 import javax.sql.DataSource;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.dialect.MySQL5InnoDBDialect;
 import org.hibernate.ejb.HibernatePersistence;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import com.mjeanroy.springhub.dao.GenericDao;
 
 @Configuration
 @EnableTransactionManagement
 public abstract class DatabaseConfiguration {
 
 	/** Class logger. */
 	private static final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);
 
	@Bean
 	public abstract DataSource dataSource();
 
 	@Bean(destroyMethod = "destroy")
 	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
 		List<String> packages = packagesToScan();
 		int size = packages.size();
 		String[] array = packagesToScan().toArray(new String[size]);
 
 		DataSource dataSource = dataSource();
 
 		Class dialect = null;
 		boolean showSQL = showSQL();
 
 		try {
 			String driverName = dataSource.getConnection().getMetaData().getDriverName();
 			dialect = dialect(driverName);
 		}
 		catch (SQLException ex) {
 			log.warn("Unable to retrieve database driver class name");
 			log.warn(ex.getMessage());
 		}
 
 		log.info("Configure hibernate JPA vendor adapter");
 		log.debug("- Show sql: {}", showSQL);
 		log.debug("- Dialect: {}", dialect);
 		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
 		vendorAdapter.setShowSql(showSQL);
 		if (dialect != null) {
 			vendorAdapter.setDatabasePlatform(dialect.getName());
 		}
 
 		log.info("Initialize entity manager factory");
 		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
 		entityManagerFactory.setJpaVendorAdapter(vendorAdapter);
 
 		log.debug("- Configure entity manager persistence provider class");
 		entityManagerFactory.setPersistenceProviderClass(HibernatePersistence.class);
 
 		log.debug("- Configure entity manager data source");
 		entityManagerFactory.setDataSource(dataSource);
 
 		log.debug("- Configure packages to scan : {}", array);
 		entityManagerFactory.setPackagesToScan(array);
 
 		Map<String, Object> jpaProperties = jpaProperties();
 
 		log.debug("- Configure JPA properties: {}", jpaProperties);
 		if (jpaProperties != null && !jpaProperties.isEmpty()) {
 			entityManagerFactory.setJpaPropertyMap(jpaProperties);
 		}
 
 		return entityManagerFactory;
 	}
 
 	@Bean
 	public PlatformTransactionManager transactionManager() {
 		log.info("Configure JPA transaction manager");
 		JpaTransactionManager txManager = new JpaTransactionManager();
 		txManager.setDataSource(dataSource());
 		return txManager;
 	}
 
 	@Bean
 	public GenericDao genericDao() {
 		log.info("Initialize generic dao bean");
 		return new GenericDao();
 	}
 
 	/**
 	 * Configure database dialect.
 	 * Use null to let hibernate determine dialect class.
 	 *
 	 * @param driver Database driver class name.
 	 * @return Class dialect.
 	 */
 	protected Class dialect(String driver) {
 		log.debug("Get dialect associated to driver {}", driver);
 		return driver.equalsIgnoreCase("com.mysql.jdbc.Driver") ?
 				MySQL5InnoDBDialect.class :
 				null;
 	}
 
 	/**
 	 * Configure 'show_sql' property.
 	 *
 	 * @return True / False.
 	 */
 	protected boolean showSQL() {
 		return false;
 	}
 
 	/**
 	 * Configure specific JPA properties.
 	 *
 	 * @return JPA properties.
 	 */
 	protected Map<String, Object> jpaProperties() {
 		return emptyMap();
 	}
 
 	/**
 	 * Get packages to scan (packages that contain entities).
 	 *
 	 * @return List of packages to scan.
 	 */
 	protected abstract List<String> packagesToScan();
 }
