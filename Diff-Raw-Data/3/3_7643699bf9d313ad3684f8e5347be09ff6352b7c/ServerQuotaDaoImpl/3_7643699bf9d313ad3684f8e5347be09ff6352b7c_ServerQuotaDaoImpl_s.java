 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.jasig.portlet.blackboardvcportlet.dao.impl;
 
 import org.jasig.portlet.blackboardvcportlet.dao.ServerQuotaDao;
 import org.springframework.stereotype.Repository;
 
 import com.elluminate.sas.ServerQuotasResponse;
 
 /**
  * Implementation of ServerQuotaDao interface, allows the storage, deletion
  * and retrieval of ServerQuota
  * @author Richard Good
  */
 @Repository
 public class ServerQuotaDaoImpl extends BaseJpaDao implements ServerQuotaDao {
 
     @Override
     public ServerQuotaImpl createOrUpdateQuota(ServerQuotasResponse quotasResponse) {
         ServerQuotaImpl serverQuota = this.getServerQuota();
         if (serverQuota == null) {
             serverQuota = new ServerQuotaImpl();
         }
         
         serverQuota.setDiskQuota(quotasResponse.getDiskQuota());
         serverQuota.setDiskQuotaAvailable(quotasResponse.getDiskQuotaAvailable());
         serverQuota.setSessionQuota(quotasResponse.getSessionQuota());
         serverQuota.setSessionQuotaAvailable(quotasResponse.getSessionQuotaAvailable());
         
         this.getEntityManager().persist(serverQuota);
         
         return serverQuota;
     }
 
     @Override
     public ServerQuotaImpl getServerQuota() {
         return this.getEntityManager().find(ServerQuotaImpl.class, ServerQuotaImpl.QUOTA_ID);
     }
 
     @Override
     public void deleteServerQuota() {
         final ServerQuotaImpl serverQuota = this.getServerQuota();
         if (serverQuota != null) {
             this.getEntityManager().remove(serverQuota);
         }
     }
 }
