 package org.mailoverlord.server.config;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 
 /**
  * DBCP DataSource configured by properties file.  Used by continuous integration.
  */
 @Configuration
 @PropertySource("file:src/test/config/ci/${DB:h2}.properties")
 public class DbcpDataSourceConfig {
 
     private static final Logger logger = LoggerFactory.getLogger(DbcpDataSourceConfig.class);
 
     @Autowired
     private Environment environment;
 
     @Bean(destroyMethod = "close")
     public DataSource dataSource() {
         BasicDataSource dataSource = new BasicDataSource();
         dataSource.setDriverClassName(environment.getProperty("db.class"));
         dataSource.setUrl(environment.getProperty("db.url"));
         dataSource.setUsername(environment.getProperty("db.user"));
         dataSource.setPassword(environment.getProperty("db.password"));
         dataSource.setValidationQuery(environment.getProperty("db.validation.query"));
         dataSource.setTestOnBorrow(environment.getProperty("db.test.on.borrow", Boolean.class));
 
         logger.info("DataSource: " + dataSource);
         Connection con = null;
         try {
             con = dataSource.getConnection();
             logger.info("Connection is closed: {}", con.isClosed());
         } catch(Exception e) {
             logger.error("Error while getting connection", e);
         } finally {
             if(con != null) try { con.close(); } catch(Exception ignore) {}
         }
 
         return dataSource;
     }
 
 }
