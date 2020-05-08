 package com.todo.config;
 
 import javax.persistence.EntityManagerFactory;
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Profile;
 import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.Database;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 import org.springframework.transaction.PlatformTransactionManager;
 
 import com.todo.repository.TodoRepository;
 
 @Configuration
 @EnableJpaRepositories(basePackageClasses = TodoRepository.class, entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
 @Profile("dev")
 public class DevConfig {
 
 	@Bean
 	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
 		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
 		entityManagerFactory.setDataSource(dataSource());
 		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
 		hibernateJpaVendorAdapter.setDatabase(Database.HSQL);
 		hibernateJpaVendorAdapter.setGenerateDdl(true);
 		hibernateJpaVendorAdapter.setShowSql(true);
 		entityManagerFactory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
 		return entityManagerFactory;
 	}
 
 	@Bean
 	public DataSource dataSource() {
 		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).setName("todoz").build();
 	}
 
 	@Bean
 	@Autowired
 	public PlatformTransactionManager transactionManager(
 			EntityManagerFactory entityManagerFactory) {
 		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
 		jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
 		return jpaTransactionManager;
 	}
 }
