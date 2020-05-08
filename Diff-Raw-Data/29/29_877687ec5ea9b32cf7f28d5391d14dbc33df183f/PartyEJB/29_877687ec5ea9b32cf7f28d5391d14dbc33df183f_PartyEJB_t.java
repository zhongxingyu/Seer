 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.ejb.party.businesslogic;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import org.sola.common.RolesConstants;
 import org.sola.common.SOLAAccessException;
 import org.sola.services.common.ejbs.AbstractEJB;
 import org.sola.services.common.faults.SOLAAccessFault;
 import org.sola.services.common.repository.CommonSqlProvider;
 import org.sola.services.ejb.address.businesslogic.AddressEJBLocal;
 import org.sola.services.ejb.address.repository.entities.Address;
 import org.sola.services.ejb.party.repository.entities.CommunicationType;
 import org.sola.services.ejb.party.repository.entities.GenderType;
 import org.sola.services.ejb.party.repository.entities.IdType;
 import org.sola.services.ejb.party.repository.entities.Party;
 import org.sola.services.ejb.party.repository.entities.PartyRole;
 import org.sola.services.ejb.party.repository.entities.PartyType;
 import org.sola.services.ejb.party.repository.entities.PartyRoleType;
 
 /**
  *
  */
 @Stateless
 @EJB(name = "java:global/SOLA/PartyEJBLocal", beanInterface = PartyEJBLocal.class)
 public class PartyEJB extends AbstractEJB implements PartyEJBLocal {
 
     @Override
     protected void postConstruct() {
         setEntityPackage(Party.class.getPackage().getName());
     }
     @EJB
     private AddressEJBLocal addressEJB;
 
     @Override
     public Party getParty(String id) {
         return getRepository().getEntity(Party.class, id);
     }
 
     @Override
     public List<Party> getParties(List<String> partyIds) {
         return getRepository().getEntityListByIds(Party.class, partyIds);
     }
 
     @Override
     @RolesAllowed({RolesConstants.PARTY_SAVE, RolesConstants.PARTY_RIGHTHOLDERS_SAVE})
     public Party saveParty(Party party) {
         if (party.isRightHolder() && !isInRole(RolesConstants.PARTY_RIGHTHOLDERS_SAVE)) {
             throw new SOLAAccessException();
         }
         return getRepository().saveEntity(party);
     }
 
     @Override
     public List<CommunicationType> getCommunicationTypes(String languageCode) {
         return getRepository().getCodeList(CommunicationType.class, languageCode);
     }
 
     @Override
     public List<PartyType> getPartyTypes(String languageCode) {
         return getRepository().getCodeList(PartyType.class, languageCode);
     }
 
     @Override
     public List<IdType> getIdTypes(String languageCode) {
         return getRepository().getCodeList(IdType.class, languageCode);
     }
 
     @Override
     public List<GenderType> getGenderTypes(String languageCode) {
         return getRepository().getCodeList(GenderType.class, languageCode);
     }
 
     @Override
     public List<Party> getAgents() {
         Map params = new HashMap<String, Object>();
        params.put(CommonSqlProvider.PARAM_WHERE_PART, Party.QUERY_WHERE_LODGING_AGENTS);
 
         // Don't load Address or PartyRole as these are not required for the agents list. 
         getRepository().setLoadInhibitors(new Class<?>[]{PartyRole.class, Address.class});
         List<Party> agents = getRepository().getEntityList(Party.class, params);
         getRepository().clearLoadInhibitors();
 
         return agents;
     }
 
     @Override
     public List<PartyRoleType> getPartyRoles(String languageCode) {
         return getRepository().getCodeList(PartyRoleType.class, languageCode);
     }
 }
