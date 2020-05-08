 package com.edify.config;
 
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.LogLevel;
import com.avaje.ebean.config.AutofetchConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.UnderscoreNamingConvention;
import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean;
import com.avaje.ebean.springsupport.txn.SpringAwareJdbcTransactionManager;
 import com.googlecode.flyway.core.Flyway;
 import com.jolbox.bonecp.BoneCPDataSource;
 import org.apache.commons.lang.ArrayUtils;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.*;
 import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import javax.sql.DataSource;
 import java.io.IOException;
import java.util.Arrays;
 
 /**
  * @author jarias
  * @since 9/2/12 11:25 AM
  */
 @Configuration
 @ComponentScan(basePackages = "com.edify")
 @ImportResource({"classpath:META-INF/spring/applicationContext-security.xml", "classpath:META-INF/spring/applicationContext.xml"})
 @EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
 public class ApplicationConfig {
   @Value("${database.url}")
   private String databaseUrl;
   @Value("${database.driver.classname}")
   private String databaseDriverClassname;
   @Value("${database.username}")
   private String databaseUsername;
   @Value("${database.password}")
   private String databasePassword;
   @Value("${hibernate.dialect}")
   private String hibernateDialect;
 
   @Value("${email.host}")
   private String emailHost;
 
   @Bean
   static public PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws IOException {
     PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
     PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
     Resource[] resourceLocations = (Resource[]) ArrayUtils.addAll(resourcePatternResolver.getResources("classpath*:META-INF/spring/*.properties"),
             resourcePatternResolver.getResources("file:/srv/config/*.properties"));
     p.setLocations(resourceLocations);
     return p;
   }
 
   @Bean
   public DataSource dataSource() {
     BoneCPDataSource ds = new BoneCPDataSource();
     ds.setDriverClass(databaseDriverClassname);
     ds.setJdbcUrl(databaseUrl);
     ds.setUsername(databaseUsername);
     ds.setPassword(databasePassword);
     ds.setIdleConnectionTestPeriodInMinutes(60);
     ds.setIdleMaxAgeInMinutes(240);
     ds.setMaxConnectionsPerPartition(20);
     ds.setMinConnectionsPerPartition(10);
     ds.setPartitionCount(2);
     ds.setAcquireIncrement(5);
     ds.setStatementsCacheSize(100);
     ds.setReleaseHelperThreads(3);
     return ds;
   }
 
   @Bean
   public PersistenceProvider persistenceProvider() {
     return new HibernatePersistence();
   }
 
   @Bean(name = "entityManagerFactory")
   @DependsOn("flyway")
   public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean() {
     LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
     localContainerEntityManagerFactoryBean.setDataSource(dataSource());
     localContainerEntityManagerFactoryBean.setPersistenceProvider(persistenceProvider());
     localContainerEntityManagerFactoryBean.setPackagesToScan("change.me.model");
     Properties jpaProperties = new Properties();
     jpaProperties.setProperty("hibernate.dialect", hibernateDialect);
     jpaProperties.setProperty("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
     jpaProperties.setProperty("hibernate.connection.charSet", "UTF-8");
     localContainerEntityManagerFactoryBean.setJpaProperties(jpaProperties);
     return localContainerEntityManagerFactoryBean;
   }
 
   @Bean(name = "transactionManager")
   public JpaTransactionManager transactionManager() {
     JpaTransactionManager transactionManager = new JpaTransactionManager();
     transactionManager.setEntityManagerFactory(localContainerEntityManagerFactoryBean().getObject());
     return transactionManager;
   }
 
   @Bean(name = "mailSender")
   public JavaMailSenderImpl mailSender() {
     JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
     mailSender.setHost(emailHost);
     return mailSender;
   }
 
   @Bean(name = "flyway", initMethod = "migrate")
   public Flyway flyway() {
     Flyway flyway = new Flyway();
     flyway.setDataSource(dataSource());
     return flyway;
   }
 }
