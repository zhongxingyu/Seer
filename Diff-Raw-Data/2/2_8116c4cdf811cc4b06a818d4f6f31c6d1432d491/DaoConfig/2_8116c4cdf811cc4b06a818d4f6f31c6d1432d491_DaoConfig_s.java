 package com.boky.configclasses;
 
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.jndi.JndiObjectFactoryBean;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.JpaVendorAdapter;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.Database;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 
 @Configuration
 public class DaoConfig {
 	@Value("${jdbc.driverClassName}")
 	private String driverClassName;
 	@Value("${jdbc.url}")
 	private String url;
 	@Value("${jdbc.username}")
 	private String username;
 	@Value("${jdbc.password}")
 	private String password;
 
 	@Bean
 	public DriverManagerDataSource dataSourceWithoutJNDI() {
 
 		DriverManagerDataSource dataSource = new DriverManagerDataSource();
 		dataSource.setDriverClassName(driverClassName);
 		dataSource.setUrl(url);
 		dataSource.setUsername(username);
 		dataSource.setPassword(password);
 		return dataSource;
 	}
 
 	@Bean
 	public static PropertyPlaceholderConfigurer properties() {
 		PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
 		final Resource[] resources = new ClassPathResource[] { new ClassPathResource("database.properties") };
 		ppc.setLocations(resources);
 
 		return ppc;
 	}
 
 	@Bean
 	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
 		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
 		emf.setPackagesToScan("com.boky.SubjectParser.daolayer.entities");
 		emf.setDataSource((DataSource) dataSource().getObject());
 		// emf.setDataSource(dataSourceWithoutJNDI());
 		emf.setJpaVendorAdapter(jpaVendorAdapter());
 		emf.setJpaProperties(jpaProperties());
 		return emf;
 
 	}
 
 	private Properties jpaProperties() {
 		Properties properties = new Properties();
 		// properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
 		properties.setProperty("hibernate.use_sql_comments", "false");
 		properties.setProperty("hibernate.generate_statistics", "false");
 		return properties;
 	}
 
 	@Bean
 	public JpaVendorAdapter jpaVendorAdapter() {
 		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
 		hibernateJpaVendorAdapter.setShowSql(false);
 		hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
 		return hibernateJpaVendorAdapter;
 	}
 
 	@Bean
 	public JpaTransactionManager transactionManager() {
 		JpaTransactionManager transactionManager = new JpaTransactionManager();
 		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
 		return transactionManager;
 	}
 
 	@Bean
 	public JndiObjectFactoryBean dataSource() {
 		JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
		jndiObjectFactoryBean.setJndiName("java:comp/env/jdbc/SubjectDependencies");
 		jndiObjectFactoryBean.setLookupOnStartup(true);
 		jndiObjectFactoryBean.setProxyInterface(DataSource.class);
 		return jndiObjectFactoryBean;
 	}
 }
