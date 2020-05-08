 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 
 package org.overlord.gadgets.server;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 /**
  *
  */
 public class Bootstrap {
 
     public static final String DB_DRIVER = "gadget-server.db.driver";
     public static final String DB_URL = "gadget-server.db.url";
     public static final String DB_USER = "gadget-server.db.user";
     public static final String DB_PASSWORD = "gadget-server.db.password";
     public static final String JPA_UNITNAME = "gadget-server.jpa.unitname";
     public static final String HIBERNATE_HBM2DDL_AUTO = "gadget-server.hibernate.hbm2ddl.auto";
 
    private EntityManager entityManager;
 
     private String dbDriver;
     private String dbUrl;
     private String dbUser;
     private String dbPassword;
     private String hibernateHbm2DdlAuto;
 
     @Inject
     public Bootstrap(@Named(DB_DRIVER) String dbDriver,
                      @Named(DB_URL) String dbUrl, @Named(DB_USER) String dbUser,
                      @Named(DB_PASSWORD) String dbPassword,
                      @Named(HIBERNATE_HBM2DDL_AUTO) String hibernateHbm2DdlAuto) {
 
         this.dbDriver = dbDriver;
         this.dbUrl = dbUrl;
         this.dbUser = dbUser;
         this.dbPassword = dbPassword;
         this.hibernateHbm2DdlAuto = hibernateHbm2DdlAuto;
 
     }
 
     public Bootstrap() {
 
     }
 
     public void init(String unitName) {
 
         Map<String, String> properties = new HashMap<String, String>();
         if (isNotEmpty(dbDriver)) {
             properties.put("hibernate.connection.driver_class", dbDriver);
         }
         if (isNotEmpty(dbUrl)) {
             properties.put("hibernate.connection.url", dbUrl);
         }
         if (isNotEmpty(dbUser)) {
             properties.put("hibernate.connection.username", dbUser);
         }
         if (isNotEmpty(dbPassword)) {
             properties.put("hibernate.connection.password", dbPassword);
         }
         if (isNotEmpty(hibernateHbm2DdlAuto)) {
             properties.put("hibernate.hbm2ddl.auto", hibernateHbm2DdlAuto);
         }
 
        EntityManagerFactory emFactory = Persistence.createEntityManagerFactory(unitName, properties);
        entityManager = emFactory.createEntityManager();
     }
 
     private boolean isNotEmpty(String value) {
         if (value == null || "".equalsIgnoreCase(value.trim())) {
             return false;
         }
         return true;
     }
 
     /**
      * @param unitName
      * @return
      */
     public EntityManager getEntityManager(String unitName) {
        if (entityManager == null) {
             init(unitName);
         }
        return entityManager;
     }
 }
