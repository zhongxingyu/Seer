 package org.xezz.timeregistration.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
 import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
 import org.springframework.jndi.JndiObjectFactoryBean;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.JpaVendorAdapter;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.orm.jpa.vendor.Database;
 import org.springframework.orm.jpa.vendor.OpenJpaDialect;
 import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import javax.sql.DataSource;
 
 /**
  * User: Xezz
  * Date: 12.05.13
  * Time: 19:48
  * Configure everything that is Database / Persistence specific
  */
 @Configuration
 @EnableJpaRepositories("org.xezz.timeregistration.repositories")
 @ComponentScan("org.xezz.timeregistration.model")
 @EnableTransactionManagement
 public class DataConfig {
 
     private final String persistenceUnitName = "timeregistration";
 
     @Bean
     public JndiObjectFactoryBean dataSourceFactory() throws Exception {
         // Testing here:
         JndiObjectFactoryBean fac = new JndiObjectFactoryBean();
         fac.setJndiName("java:/comp/env/jdbc/timeregistration");
         fac.setResourceRef(true);
         return fac;
 
 
         /*
         Context ctx = new InitialContext();
 
         return (DataSource) ctx.lookup("java:/comp/env/jdbc/timeregistration");
         */
     }
 
     @Bean
     public DataSource dataSource() throws Exception {
         return (DataSource) dataSourceFactory().getObject();
     }
 
     @Bean
     public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws Exception {
         LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setPackagesToScan(new String[] {"org.xezz.timeregistration"});
        //entityManagerFactory.setPersistenceUnitName(persistenceUnitName);
        entityManagerFactory.setDataSource((DataSource) dataSourceFactory().getObject());
         entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter());
         entityManagerFactory.setJpaDialect(new OpenJpaDialect());
 
         return entityManagerFactory;
 
     }
 
     @Bean
     public JpaVendorAdapter jpaVendorAdapter() throws Exception {
         OpenJpaVendorAdapter jpaVendorAdapter = new OpenJpaVendorAdapter();
         jpaVendorAdapter.setGenerateDdl(true);
         jpaVendorAdapter.setShowSql(true);
         jpaVendorAdapter.setDatabase(Database.POSTGRESQL);
 
         return jpaVendorAdapter;
     }
 
     @Bean
     public JpaTransactionManager transactionManager() throws Exception {
         JpaTransactionManager transactionManager = new JpaTransactionManager();
         transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
 
         return transactionManager;
     }
 
     @Bean
     public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
 
         return new PersistenceExceptionTranslationPostProcessor();
     }
 
 }
