 package org.mailoverlord.server.config;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
 import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import javax.sql.DataSource;
 
 /**
  * Spring JPA Config
  */
 @Configuration
 @EnableTransactionManagement(proxyTargetClass = true)
 @EnableJpaRepositories("org.mailoverlord.server.repositories")
 public class JpaConfig {
 
     @Autowired
    private DataSource dataSource;
 
     @Bean
     public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
         LocalContainerEntityManagerFactoryBean emf =
                 new LocalContainerEntityManagerFactoryBean();
         HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
         vendorAdapter.setGenerateDdl(true);
         vendorAdapter.setShowSql(true);
         emf.setJpaVendorAdapter(vendorAdapter);
         emf.setDataSource(dataSource);
         emf.setPackagesToScan("org.mailoverlord.server.entities");
         return emf;
     }
 
     @Bean
     public PlatformTransactionManager transactionManager(){
         JpaTransactionManager transactionManager = new JpaTransactionManager();
         transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
         return transactionManager;
     }
 
     @Bean
     public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
         return new PersistenceExceptionTranslationPostProcessor();
     }
 
 }
