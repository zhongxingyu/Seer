 package cz.cvut.fel.bupro.test.configuration;
 
 import java.util.Properties;
 
 import javax.persistence.EntityManagerFactory;
 import javax.sql.DataSource;
 
 import org.hibernate.ejb.HibernatePersistence;
 import org.mockito.Mockito;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
 import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
 import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import cz.cvut.fel.bupro.model.User;
 import cz.cvut.fel.bupro.security.SecurityService;
 import cz.cvut.fel.bupro.security.SpringSecurityService;
 
 @Configuration
 @EnableJpaRepositories(basePackages = { "cz.cvut.fel.bupro.dao" })
 @EnableTransactionManagement
 @ComponentScan({ "cz.cvut.fel.bupro.controller", "cz.cvut.fel.bupro.service", "cz.cvut.fel.bupro.test.mock" })
 public class CommonTestConfig {
 
 	@Bean
 	public SecurityService securityService() {
 		SpringSecurityService securityService = Mockito.mock(SpringSecurityService.class);
 		User user = new User();
 		user.setId(3L);
 		user.setUsername("test");
 		user.setEmail("test@email");
 		Mockito.stub(securityService.getCurrentUser()).toReturn(user);
 		return securityService;
 	}
 
 	@Bean
 	public EntityManagerFactory entityManagerFactory() {
 		Properties properties = new Properties();
 		properties.put("hibernate.show_sql", "true");
 		properties.put("hibernate.hbm2ddl.auto", "update");
 		properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
 		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
 		entityManagerFactory.setDataSource(dataSource());
 		entityManagerFactory.setPackagesToScan(new String[] { "cz.cvut.fel.bupro.model" });
 		entityManagerFactory.setPersistenceProvider(new HibernatePersistence());
 		entityManagerFactory.setJpaProperties(properties);
 		entityManagerFactory.setPersistenceUnitName("testdb");
 		entityManagerFactory.afterPropertiesSet();
 		return entityManagerFactory.getObject();
 	}
 
 	@Bean
 	public PlatformTransactionManager transactionManager() {
 		JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory());
 		transactionManager.setDataSource(dataSource());
 		transactionManager.setJpaDialect(new HibernateJpaDialect());
 		return transactionManager;
 	}
 
 	@Bean
 	public DataSource dataSource() {
 		EmbeddedDatabaseFactory factory = new EmbeddedDatabaseFactory();
 		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
 		factory.setDatabaseType(EmbeddedDatabaseType.H2);
 		factory.setDatabaseName("testdb;MODE=PostgreSQL");
 		factory.setDatabasePopulator(populator);
 		return factory.getDatabase();
 	}
 
 	@Bean
 	public HibernateExceptionTranslator hibernateExceptionTranslator() {
 		return new HibernateExceptionTranslator();
 	}
 }
