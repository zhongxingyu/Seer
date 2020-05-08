 package com.openshift.notebook.core.config;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Profile;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.Database;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 @Configuration
 @Profile("openshift")
 @EnableTransactionManagement
 public class PostgresqlConfig {
 
 	@Bean
 	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
 		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
 		entityManagerFactory.setDataSource(dataSource());
 		entityManagerFactory.setPersistenceUnitName("notebook-postgresql");
		entityManagerFactory.setPersistenceXmlLocation("classpath:META-INF/jpa-persisence.xml");
 		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
 		hibernateJpaVendorAdapter.setDatabase(Database.POSTGRESQL);
 		hibernateJpaVendorAdapter.setGenerateDdl(true);
 		hibernateJpaVendorAdapter.setShowSql(true);
 		entityManagerFactory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
 		return entityManagerFactory;
 	}
 
 	@Bean(destroyMethod = "close")
 	public DataSource dataSource() {
 		String username = System.getenv("OPENSHIFT_DB_USERNAME");
 		String password = System.getenv("OPENSHIFT_DB_PASSWORD");
 		String host = System.getenv("OPENSHIFT_DB_HOST");
 		String port = System.getenv("OPENSHIFT_DB_PORT");
 		String url = "jdbc:postgresql://" + host + ":" + port + "/notebook";
 		BasicDataSource dataSource = new BasicDataSource();
 		dataSource.setDriverClassName("com.progress.sql.jdbc.JdbcProgressDriver");
 		dataSource.setUrl(url);
 		dataSource.setUsername(username);
 		dataSource.setPassword(password);
 		dataSource.setTestOnBorrow(true);
 		dataSource.setTestOnReturn(true);
 		dataSource.setTestWhileIdle(true);
 		dataSource.setTimeBetweenEvictionRunsMillis(1800000);
 		dataSource.setNumTestsPerEvictionRun(3);
 		dataSource.setMinEvictableIdleTimeMillis(1800000);
 		dataSource.setValidationQuery("SELECT version()");
 
 		return dataSource;
 	}
 }
