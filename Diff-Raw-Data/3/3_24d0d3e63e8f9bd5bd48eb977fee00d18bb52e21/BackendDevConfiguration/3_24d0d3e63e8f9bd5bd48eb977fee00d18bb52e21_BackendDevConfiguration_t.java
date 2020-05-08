 package com.adeoservices.backend.config.spring;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bgr
  * Date: 13/05/13
  * Time: 15:39
  */
 
 @Configuration
@Profile("dev")
 @PropertySource({"classpath:conf/db/backend-ds.properties"})
 public class BackendDevConfiguration {
 
 
     @Resource
     protected Environment env;
 
     @Bean
     DataSource dataSource() {
 
         BasicDataSource dataSource = new BasicDataSource();
 
         dataSource.setUsername(env.getRequiredProperty("dataSource-username"));
         dataSource.setPassword(env.getRequiredProperty("dataSource-password"));
         dataSource.setDriverClassName(env.getRequiredProperty("dataSource-driverClassName"));
         dataSource.setUrl(env.getRequiredProperty("dataSource-url"));
 
         return dataSource;
     }
 }
