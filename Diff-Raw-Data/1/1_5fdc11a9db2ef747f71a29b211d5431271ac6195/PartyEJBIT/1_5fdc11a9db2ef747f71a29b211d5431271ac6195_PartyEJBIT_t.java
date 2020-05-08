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
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 import javax.transaction.Status;
 import javax.transaction.UserTransaction;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.sola.services.common.EntityAction;
 import org.sola.services.common.repository.RepositoryUtility;
 import static org.junit.Assert.*;
 import org.sola.services.common.repository.entities.ChildEntityInfo;
 import org.sola.services.common.test.AbstractEJBTest;
 import org.sola.services.ejb.address.businesslogic.AddressEJB;
 import org.sola.services.ejb.address.businesslogic.AddressEJBLocal;
 import org.sola.services.ejb.address.repository.entities.Address;
 import org.sola.services.ejb.party.repository.entities.CommunicationType;
 import org.sola.services.ejb.party.repository.entities.Party;
 import org.sola.services.ejb.party.repository.entities.PartyRole;
 
 /**
  *
  * @author Manoku
  */
 public class PartyEJBIT extends AbstractEJBTest {
 
     private static String ADDR_MODULE_NAME = "sola-address-1_0-SNAPSHOT";
     private static String PARTY_MODULE_NAME = "sola-party-1_0-SNAPSHOT";
 
     public PartyEJBIT() {
         super();
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of saveParty method, of class PartyEJB.
      */
     @Test
     public void testSaveParty() throws Exception {
 
         System.out.println("testSaveParty...");
 
 
         Address address = new Address();
         address.setDescription("My party testing Address");
         Party party = new Party();
         party.setId(UUID.randomUUID().toString());
         party.setTypeCode("naturalPerson");
         party.setChangeUser("andrew");
         party.setAddress(address);
         party.setRoleList(new ArrayList<PartyRole>());
         PartyRole role = new PartyRole();
         role.setRoleCode("citizen");
         party.getRoleList().add(role);
         role = new PartyRole();
         role.setRoleCode("writer");
         party.getRoleList().add(role);
         role = new PartyRole();
         role.setRoleCode("bank");
         party.getRoleList().add(role);
         
         PartyEJBLocal instance = (PartyEJBLocal) getEJBInstance(PARTY_MODULE_NAME,
                 PartyEJB.class.getSimpleName());
            login("test", "test");
 
         // Create the Address
         UserTransaction tx = getUserTransaction();
         try {
             tx.begin();
 
             System.out.println(">>> Create Party");
             Party result = instance.saveParty(party);
             assertNotNull(result);
             assertNotNull(result.getAddress());
             assertEquals(1, result.getRowVersion());
             assertEquals(1, result.getAddress().getRowVersion());
 
             String partyId = result.getId();
             System.out.println("PartyId=" + result.getId());
             System.out.println("AddressId=" + result.getAddress().getId());
 
             System.out.println(">>> Remove party role");
             Party result0 = instance.getParty(partyId);
             assertNotNull(result0);
             result0.getRoleList();
             assertNotNull(result0.getRoleList());
             result0.getRoleList().get(0).setEntityAction(EntityAction.DELETE);
             Party result01 = instance.saveParty(result0);
             assertEquals(result01.getRoleList().size(), 2);
 
             System.out.println(">>> Update Party");
             result.setEmail("Test Email");
             Party result2 = instance.saveParty(result);
             assertNotNull(result2);
             assertNotNull(result2.getAddress());
             assertEquals(2, result2.getRowVersion());
             assertEquals(1, result2.getAddress().getRowVersion());
 
             System.out.println(">>> Update Party Address");
             result2.getAddress().setDescription("Test Address Update");
             Party result3 = instance.saveParty(result2);
             assertNotNull(result3);
             assertNotNull(result3.getAddress());
             assertEquals(2, result3.getRowVersion());
             assertEquals(2, result3.getAddress().getRowVersion());
             String addrId = result3.getAddressId();
 
             System.out.println(">>> Disassociate Address");
             result3.getAddress().setEntityAction(EntityAction.DISASSOCIATE);
             Party result4 = instance.saveParty(result3);
             assertNotNull(result4);
             assertNull(result4.getAddress());
             assertNull(result4.getAddressId());
             assertEquals(3, result4.getRowVersion());
 
             System.out.println(">>> Disassociate Party");
             result4.setEntityAction(EntityAction.DISASSOCIATE);
             Party result5 = instance.saveParty(result4);
             assertNull(result5);
             result5 = instance.getParty(partyId);
             assertNotNull(result5);
             assertEquals(3, result5.getRowVersion());
 
 
             // Retrieve the original address from the DB
             AddressEJBLocal addrEJB = (AddressEJBLocal) getEJBInstance(ADDR_MODULE_NAME,
                     AddressEJB.class.getSimpleName());
             result5.setAddress(addrEJB.getAddress(addrId));
             assertNotNull(result5.getAddress());
             assertNull(result5.getAddress().getEntityAction());
 
             System.out.println(">>> Re-associate Address");
             Party result6 = instance.saveParty(result5);
             assertNotNull(result6);
             assertNotNull(result6.getAddress());
             assertEquals(4, result6.getRowVersion());
             assertEquals(2, result6.getAddress().getRowVersion());
 
             System.out.println(">>> Associate New Address");
             Address newAddr = new Address();
             newAddr.setDescription("New Address");
             result6.setAddress(newAddr);
             Party result7 = instance.saveParty(result6);
             assertNotNull(result7);
             assertNotNull(result7.getAddress());
             assertEquals(5, result7.getRowVersion());
             assertEquals(1, result7.getAddress().getRowVersion());
             String newAddrId = result7.getAddressId();
 
             System.out.println(">>> Delete Address");
             result7.getAddress().setEntityAction(EntityAction.DELETE);
             Party result8 = instance.saveParty(result7);
             assertNotNull(result8);
             assertNull(result8.getAddress());
             assertNull(result8.getAddressId());
             assertEquals(6, result8.getRowVersion());
             assertNull(addrEJB.getAddress(newAddrId));
 
 
             System.out.println(">>> Re-associate Address for Delete");
             result8.setAddress(addrEJB.getAddress(addrId));
             Party result9 = instance.saveParty(result8);
             assertNotNull(result9.getAddress());
             assertNull(result9.getAddress().getEntityAction());
 
             System.out.println(">>> Delete Party and Address");
             result9.setEntityAction(EntityAction.DELETE);
             result9.getAddress().setEntityAction(EntityAction.DELETE);
             Party result10 = instance.saveParty(result9);
             assertNull(result10);
             assertNull(instance.getParty(partyId));
             assertNull(addrEJB.getAddress(addrId));
 
             tx.commit();
         } finally {
             if (tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
                 tx.rollback();
                 System.out.println("Failed Transaction!");
             }
         }
 
     }
 
     /**
      * Test of getCommunicationTypes method, of class PartyEJB.
      */
     @Test
     public void testGetCommunicationTypes() throws Exception {
         System.out.println("getCommunicationTypes");
         PartyEJBLocal instance = (PartyEJBLocal) getEJBInstance(PartyEJB.class.getSimpleName());
         //List expResult = null;
         System.out.println("In english");
         List<CommunicationType> result = instance.getCommunicationTypes("en");
         //assertEquals(expResult, result);
         System.err.println("Found:" + result.size());
         if (result.size() > 0) {
             System.out.println("First item display_value is:" + result.get(0).getDisplayValue());
         }
         System.out.println("In italian");
         result = instance.getCommunicationTypes("it");
         //assertEquals(expResult, result);
         System.err.println("Found:" + result.size());
         if (result.size() > 0) {
             System.out.println("First item display_value is:" + result.get(0).getDisplayValue());
         }
         // TODO review the generated test code and remove the default call to fail.
         //fail("The test case is a prototype.");
     }
 
     /**
      * Test of getAgents method, of class PartyEJB.
      */
     @Test
     public void testGetAgents() throws Exception {
         System.out.println("getAgents");
         PartyEJBLocal instance = (PartyEJBLocal) getEJBInstance(PartyEJB.class.getSimpleName());
         List<Party> result = instance.getAgents();
         assertNotNull(result);
         assertTrue(result.size() > 0);
         for (Party p : result) {
             assertEquals(Party.TYPE_CODE_NON_NATURAL_PERSON, p.getTypeCode());
         }
 
     }
 
     @Test
     public void testGetParty() throws Exception {
         System.out.println("get party");
         List<ChildEntityInfo> c = RepositoryUtility.getChildEntityInfo(Party.class);
         Class<?> clazz = c.get(0).getEntityClass();
         Class<?> clazz2 = c.get(1).getEntityClass();
         
         
         System.out.println("do nothing"); 
     }
 }
