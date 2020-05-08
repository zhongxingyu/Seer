 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.surfnet.cruncher.config;
 
 import javax.inject.Inject;
 import javax.servlet.Filter;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.tomcat.jdbc.pool.DataSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportResource;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.scheduling.annotation.EnableScheduling;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import com.googlecode.flyway.core.Flyway;
 
 @EnableScheduling
 @Configuration
 @PropertySource("classpath:cruncher.application.properties")
 @ImportResource("classpath:aggregationScheduling.xml")
 /*
  * The component scan can be used to add packages and exclusions to the default
  * package
  */
 @ComponentScan(basePackages = {"org.surfnet.cruncher"})
 @EnableTransactionManagement
 public class SpringConfiguration {
 
   @Inject
   Environment env;
 
   @Bean
   public javax.sql.DataSource dataSource() {
     DataSource dataSource = new DataSource();
     dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
     dataSource.setUrl(env.getProperty("jdbc.url"));
     dataSource.setUsername(env.getProperty("jdbc.username"));
     dataSource.setPassword(env.getProperty("jdbc.password"));
     return dataSource;
   }
   
   @Bean
   public Filter authorizationServerFilter() {
     String className = env.getProperty("authorizationServerFilterClass");
     if (StringUtils.isNotBlank(className)) {
       try {
         return (Filter) getClass().getClassLoader().loadClass(className).newInstance();
       } catch (Exception e) {
         throw new RuntimeException(e);
       }      
     }
     throw new IllegalStateException("cannot build authorizationServerFilter from " + className);
   }
 
   @Bean
   public Flyway flyway() {
     final Flyway flyway = new Flyway();
     flyway.setInitOnMigrate(true);
     flyway.setDataSource(dataSource());
     String locationsValue = env.getProperty("flyway.migrations.location");
     String[] locations = locationsValue.split("\\s*,\\s*");
     flyway.setLocations(locations);
     flyway.migrate();
     return flyway;
   }
 
   @Bean
   public JdbcTemplate jdbcTemplate() {
     return new JdbcTemplate(dataSource());
   }
 
   @Bean
   public PlatformTransactionManager transactionManager() {
       return new DataSourceTransactionManager(dataSource());
   }
 }
