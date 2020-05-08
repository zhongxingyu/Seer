 package no.niths.common.config;
 
 import java.util.Properties;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.dao.support.PersistenceExceptionTranslator;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
 import org.springframework.orm.hibernate4.HibernateTransactionManager;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 @Configuration
 @EnableTransactionManagement
@ComponentScan({"no.niths.services", "no.niths.infrastructure"})
 public class HibernateConfig{
 
     private final String DRIVER       = "jdbc.driver.class_name",
                          URL          = "jdbc.url",
                          USERNAME     = "jdbc.username",
                          PASSWORD     = "jdbc.password",
                          DIALECT      = "hibernate.dialect",
                          HBM2DDL_AUTO = "hibernate.hbm2ddl.auto",
                          SHOW_SQL     = "hibernate.show_sql";
 
     @Value("${" + DRIVER + "}")
     private String driver;
 
     @Value("${" + URL + "}")
     private String url;
 
     @Value("${" + USERNAME + "}")
     private String username;
 
     @Value("${" + PASSWORD + "}")
     private String password;
 
     @Value("${" + DIALECT + "}")
     private String hibernateDialect;
 
     @Value("${" + HBM2DDL_AUTO + "}")
     private String hbm2ddlAuto;
 
     @Value("${" + SHOW_SQL + "}")
     private Boolean showSQL;
 
     final Properties additionlProperties(){
         Properties props = new Properties();
         props.setProperty(DIALECT, hibernateDialect);
         props.setProperty(HBM2DDL_AUTO, hbm2ddlAuto);
         props.setProperty(SHOW_SQL, showSQL.toString());
        
         return props;
     }
 
    @Bean
    public LocalSessionFactoryBean alertsSessionFactory() {
       LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
       sessionFactory.setDataSource(restDataSource());
       sessionFactory.setPackagesToScan(AppConfig.BASE_PACKAGE);
       sessionFactory.setHibernateProperties(additionlProperties());
 
       return sessionFactory;
    }
 
    @Bean
    public DataSource restDataSource(){
       DriverManagerDataSource dataSource = new DriverManagerDataSource();
       dataSource.setDriverClassName(driver);
       dataSource.setUrl(url);
       dataSource.setUsername(username);
       dataSource.setPassword(password);
 
       return dataSource;
    }
 
    @Bean
    public HibernateTransactionManager transactionManager(){
       HibernateTransactionManager txManager = new HibernateTransactionManager();
       txManager.setSessionFactory(alertsSessionFactory().getObject());
 
       return txManager;
    }
    
    @Bean
    public PersistenceExceptionTranslator exceptionTranslator() {
 	   return new HibernateExceptionTranslator();
    }
   
   @Bean
   public ApplicationListener<ApplicationEvent> appListener(){
	   return new AppStartupDatabasePopulator();
   }
 }
