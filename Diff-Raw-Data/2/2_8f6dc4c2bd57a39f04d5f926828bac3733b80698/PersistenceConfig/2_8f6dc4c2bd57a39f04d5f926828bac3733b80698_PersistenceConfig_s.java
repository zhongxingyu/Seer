 package org.github.kolorobot.config;
 
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 import org.springframework.transaction.annotation.TransactionManagementConfigurer;
 
 @Configuration
 @EnableTransactionManagement
 @EnableJpaRepositories(basePackages = "org.github.kolorobot.repository", entityManagerFactoryRef = "entityManagerFactory")
 public class PersistenceConfig implements TransactionManagementConfigurer {
 	
 	@Value("${dataSource.driverClassName}")
 	private String driver;
 	@Value("${dataSource.url}")
 	private String url;
 	@Value("${dataSource.username}")
 	private String username;
 	@Value("${dataSource.password}")
 	private String password;
 	@Value("${hibernate.dialect}")
 	private String dialect;
 	@Value("${hibernate.hbm2ddl.auto}")
 	private String hbm2ddlAuto;
 
 	@Bean	
 	public DataSource dataSource() {
 		DriverManagerDataSource dataSource = new DriverManagerDataSource();
 		dataSource.setDriverClassName(driver);
 		dataSource.setUrl(url);
 		dataSource.setUsername(username);
 		dataSource.setPassword(password);
 		return dataSource;
 	}
 	
 	@Bean(name = "entityManagerFactory")
 	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
 		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
 		entityManagerFactoryBean.setDataSource(dataSource());
		entityManagerFactoryBean.setPackagesToScan("org.github.kolorobot.user");
 		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
 		
 		Properties jpaProperties = new Properties();
 		jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect);
 		jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hbm2ddlAuto);
 		jpaProperties.put(org.hibernate.cfg.Environment.SHOW_SQL, "true");
 		entityManagerFactoryBean.setJpaProperties(jpaProperties);
 		
 		return entityManagerFactoryBean;
 	}
 
 	@Bean(name = "transactionManager")
 	public PlatformTransactionManager annotationDrivenTransactionManager() {
 		return new JpaTransactionManager();
 	}
 
 }
