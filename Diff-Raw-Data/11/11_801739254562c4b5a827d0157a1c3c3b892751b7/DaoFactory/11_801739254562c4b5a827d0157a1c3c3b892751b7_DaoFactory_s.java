 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server.dao;
 
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.backplane2.server.config.BusConfig2;
 import com.janrain.backplane2.server.config.Client;
 import com.janrain.backplane2.server.config.User;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import org.springframework.context.annotation.Scope;
 
 import javax.inject.Inject;
 
 /**
  * @author Tom Raney
  */
 
 @Scope(value="singleton")
 public class DaoFactory {
 
     public BusDAO getBusDao() {
         return new BusDAO(superSimpleDB, bpConfig);
     }
 
     public TokenDAO getTokenDao() {
         return new TokenDAO(superSimpleDB, bpConfig);
     }
 
     public GrantDAO getGrantDao() {
         return new GrantDAO(superSimpleDB, bpConfig);
     }
 
     public BusOwnerDAO getBusOwnerDAO() {
         return new BusOwnerDAO(superSimpleDB, bpConfig);
     }
 
     public ClientDAO getClientDAO() {
         return new ClientDAO(superSimpleDB, bpConfig);
     }
 
     public BackplaneMessageDAO getBackplaneMessageDAO() {
         return new BackplaneMessageDAO(superSimpleDB, bpConfig, this);
     }
 
     public AuthSessionDAO getAuthSessionDAO() {
         return new AuthSessionDAO(superSimpleDB, bpConfig);
     }
 
     public AuthorizationRequestDAO getAuthorizationRequestDAO() {
         return new AuthorizationRequestDAO(superSimpleDB, bpConfig);
     }
 
     public AuthorizationDecisionKeyDAO getAuthorizationDecisionKeyDAO() {
         return new AuthorizationDecisionKeyDAO(superSimpleDB, bpConfig);
     }
 
    public DAO getDaoByObjectType(Object obj) {
 
        if (obj instanceof Client) {
             return getClientDAO();
        } else if (obj instanceof User) {
             return getBusOwnerDAO();
        } else if (obj instanceof BusConfig2) {
             return getBusDao();
         }
 
         return null;
     }
 
     @Inject
     private SuperSimpleDB superSimpleDB;
 
     @Inject
     private Backplane2Config bpConfig;
 
 }
