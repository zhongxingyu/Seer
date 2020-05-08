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
 
 import com.googlecode.flyway.core.Flyway;
 import org.apache.commons.lang.StringUtils;
 import org.apache.tomcat.jdbc.pool.DataSource;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.*;
 import org.springframework.core.env.Environment;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.scheduling.annotation.EnableScheduling;
 import org.surfnet.oaaas.auth.AuthorizationServerFilter;
 
 import javax.inject.Inject;
 import javax.servlet.Filter;
 
 @EnableScheduling
 @Configuration
 @PropertySource({"classpath:application.properties","classpath:cruncher.properties"})
 @ImportResource("classpath:aggregationScheduling.xml")
 /*
  * The component scan can be used to add packages and exclusions to the default
  * package
  */
 @ComponentScan(basePackages = {"org.surfnet.cruncher"})
 public class SpringConfiguration {
 
   @Inject
   private Environment env;
 
   @Inject
   private ApplicationContext applicationContext;
 
   @Bean
   public javax.sql.DataSource ebDataSource() {
     DataSource dataSource = new DataSource();
     dataSource.setDriverClassName(env.getProperty("eb.jdbc.driverClassName"));
     dataSource.setUrl(env.getProperty("eb.jdbc.url"));
     dataSource.setUsername(env.getProperty("eb.jdbc.username"));
     dataSource.setPassword(env.getProperty("eb.jdbc.password"));
     return dataSource;
   }
   
   @Bean
   public javax.sql.DataSource cruncherDataSource() {
     DataSource dataSource = new DataSource();
     dataSource.setDriverClassName(env.getProperty("cruncher.jdbc.driverClassName"));
     dataSource.setUrl(env.getProperty("cruncher.jdbc.url"));
     dataSource.setUsername(env.getProperty("cruncher.jdbc.username"));
     dataSource.setPassword(env.getProperty("cruncher.jdbc.password"));
     return dataSource;
   }
   
   @Bean
   public Filter authorizationServerFilter() {
     String className = env.getProperty("authorizationServerFilterClass");
     if (StringUtils.isNotBlank(className)) {
       try {
         AuthorizationServerFilter authorizationServerFilter = (AuthorizationServerFilter) getClass().getClassLoader().loadClass(className).newInstance();
         authorizationServerFilter.setAuthorizationServerUrl(env.getProperty("authorizationServer.url"));
         authorizationServerFilter.setResourceServerKey(env.getProperty("authorizationServer.key"));
         authorizationServerFilter.setResourceServerSecret(env.getProperty("authorizationServer.secret"));
         authorizationServerFilter.setAllowCorsRequests(Boolean.valueOf(env.getProperty("authorizationServer.allowCorsRequests")));
         authorizationServerFilter.setCacheEnabled(Boolean.valueOf(env.getProperty("authorizationServer.cacheEnabled")));
        authorizationServerFilter.setTypeInformationIsIncluded(Boolean.valueOf((env.getProperty("authorizationServer.typeInformationIsIncluded"))));
         return authorizationServerFilter;
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
     flyway.setDataSource(cruncherDataSource());
     String locationsValue = env.getProperty("flyway.migrations.location");
     String[] locations = locationsValue.split("\\s*,\\s*");
     flyway.setLocations(locations);
     flyway.migrate();
     return flyway;
   }
 
   @Bean
   public JdbcTemplate ebJdbcTemplate() {
     return new JdbcTemplate(ebDataSource());
   }
   
   @Bean
   public JdbcTemplate cruncherJdbcTemplate() {
     return new JdbcTemplate(cruncherDataSource());
   }
 }
