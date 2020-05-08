 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted
  * provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
  * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
  * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.unittests.common;
 
 import org.sola.services.boundary.transferobjects.referencedata.RequestTypeTO;
 import org.sola.services.ejb.address.repository.entities.Address;
 import java.util.ArrayList;
 import java.util.List;
 import org.junit.*;
 import org.sola.services.ejb.application.repository.entities.RequestType;
 import org.sola.services.boundary.transferobjects.casemanagement.ServiceTO;
 import org.sola.services.ejb.application.repository.entities.Service;
 import org.sola.services.boundary.transferobjects.casemanagement.AddressTO;
 import org.sola.services.boundary.transferobjects.casemanagement.PartySummaryTO;
 import org.sola.services.boundary.transferobjects.casemanagement.PartyTO;
 import org.sola.services.ejb.party.repository.entities.Party;
 import org.sola.services.ejb.application.repository.entities.Application;
 import org.sola.services.boundary.transferobjects.casemanagement.ApplicationTO;
 import org.sola.common.DateUtility;
 import org.sola.services.common.EntityAction;
 import org.sola.services.common.contracts.GenericTranslator;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author soladev
  */
 public class GenericTranslatorTest {
 
     private boolean hasAgent = true;
     private boolean hasContactPerson = true;
     private boolean hasAddress = true;
     private int numServices = 2;
 
     public GenericTranslatorTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     private void assertApplication(Application app, ApplicationTO appTO, Application outApp) {
         assertEquals(appTO.getActionCode(), outApp.getActionCode());
         assertEquals(appTO.getActionNotes(), outApp.getActionNotes());
         assertEquals(appTO.getAssignedDatetime(), outApp.getAssignedDatetime());
         assertEquals(appTO.getAssigneeId(), outApp.getAssigneeId());
         assertEquals(appTO.getExpectedCompletionDate(), outApp.getExpectedCompletionDate());
         assertEquals(appTO.getEntityAction(), outApp.getEntityAction());
         assertEquals(appTO.isFeePaid(), outApp.isFeePaid());
         assertEquals(appTO.getId(), outApp.getId());
         assertEquals(appTO.getLodgingDatetime(), outApp.getLodgingDatetime());
         assertArrayEquals(appTO.getLocation(), outApp.getLocation());
         assertEquals(appTO.getNr(), outApp.getNr());
         assertEquals(appTO.getRowVersion(), outApp.getRowVersion());
         assertEquals(appTO.getStatusCode(), outApp.getStatusCode());
         assertEquals(appTO.getServicesFee(), outApp.getServicesFee());
         assertEquals(appTO.getTax(), outApp.getTax());
         assertEquals(appTO.getTotalAmountPaid(), outApp.getTotalAmountPaid());
         assertEquals(appTO.getTotalFee(), outApp.getTotalFee());
 
         if (app != null) {
             // assertEquals(app.getChangeAction(), outApp.getChangeAction());
             assertEquals(app.getChangeUser(), outApp.getChangeUser());
         } else {
             // assertEquals(Character.UNASSIGNED, outApp.getChangeAction());
             assertNull(outApp.getChangeUser());
         }
 
         if (hasAgent) {
             assertNotNull(outApp.getAgent());
             if (app != null) {
                 if (app.getAgent().getId().equals(outApp.getAgent().getId())) {
                     // Check the object references are the same
                     assertTrue(app.getAgent() == outApp.getAgent());
                 } else {
                     // Check the object references are different if the ids do not match 
                     assertFalse(app.getAgent() == outApp.getAgent());
                 }
             }
            assertEquals(appTO.getAgent().getId(), outApp.getAgent().getId());
             assertPartySummary(app == null ? null : app.getAgent(), appTO.getAgent(),
                     outApp.getAgent());
         }
 
         if (hasContactPerson) {
             assertNotNull(outApp.getContactPerson());
             if (app != null) {
                 // Check the object references are the same
                 assertTrue(app.getContactPerson() == outApp.getContactPerson());
             }
             assertEquals(appTO.getContactPerson().getId(), outApp.getContactPersonId());
             assertParty(app == null ? null : app.getContactPerson(), appTO.getContactPerson(),
                     outApp.getContactPerson());
         }
 
         if (numServices > 0) {
             assertNotNull(outApp.getServiceList());
             assertEquals(numServices, outApp.getServiceList().size());
             for (int i = 0; i < numServices; i++) {
                 // ? assertNotNull(outApp.getServiceList().get(i).getApplication());
                 // ? assertEquals(outApp, outApp.getServiceList().get(i).getApplication());
                 if (app != null) {
                     // Used to verify the object in the outApp list is the same object as the
                     // one from the original app list. Double == checks the object reference
                     assertTrue(app.getServiceList().get(i) == outApp.getServiceList().get(i));
                 }
                 assertService(app == null ? null : app.getServiceList().get(i),
                         appTO.getServiceList().get(i), outApp.getServiceList().get(i));
             }
 
         }
 
     }
 
     private void assertParty(Party party, PartyTO partyTO, Party outParty) {
         assertEquals(partyTO.getEmail(), outParty.getEmail());
         assertEquals(partyTO.getEntityAction(), outParty.getEntityAction());
         assertEquals(partyTO.getExtId(), outParty.getExtId());
         assertEquals(partyTO.getFax(), outParty.getFax());
         assertEquals(partyTO.getId(), outParty.getId());
         assertEquals(partyTO.getIdNumber(), outParty.getIdNumber());
         assertEquals(partyTO.getIdTypeCode(), outParty.getIdTypeCode());
         assertEquals(partyTO.getLastName(), outParty.getLastName());
         assertEquals(partyTO.getMobile(), outParty.getMobile());
         assertEquals(partyTO.getName(), outParty.getName());
         assertEquals(partyTO.getPhone(), outParty.getPhone());
         assertEquals(partyTO.getPreferredCommunicationCode(), outParty.getPreferredCommunicationCode());
         assertEquals(partyTO.getRowVersion(), outParty.getRowVersion());
         assertEquals(partyTO.getTypeCode(), outParty.getTypeCode());
 
         if (party != null) {
             // assertEquals(party.getChangeAction(), outParty.getChangeAction());
             assertEquals(party.getChangeUser(), outParty.getChangeUser());
         } else {
             // assertEquals(Character.UNASSIGNED, outParty.getChangeAction());
             assertNull(outParty.getChangeUser());
         }
 
         if (hasAddress) {
             assertNotNull(outParty.getAddress());
             assertEquals(partyTO.getAddress().getId(), outParty.getAddressId());
             assertAddress(party == null ? null : party.getAddress(),
                     partyTO.getAddress(), outParty.getAddress());
         }
     }
 
     private void assertPartySummary(Party party, PartySummaryTO partyTO, Party outParty) {
 
         assertEquals(partyTO.getExtId(), outParty.getExtId());
         assertEquals(partyTO.getId(), outParty.getId());
         assertEquals(partyTO.getLastName(), outParty.getLastName());
         assertEquals(partyTO.getName(), outParty.getName());
         assertEquals(partyTO.getTypeCode(), outParty.getTypeCode());
 
         if (party != null) {
             //assertEquals(party.getChangeAction(), outParty.getChangeAction());
             assertEquals(party.getChangeUser(), outParty.getChangeUser());
             assertEquals(party.getEmail(), outParty.getEmail());
             assertEquals(party.getFax(), outParty.getFax());
             assertEquals(party.getIdNumber(), outParty.getIdNumber());
             assertEquals(party.getIdTypeCode(), outParty.getIdTypeCode());
             assertEquals(party.getMobile(), outParty.getMobile());
             assertEquals(party.getPhone(), outParty.getPhone());
             assertEquals(party.getPreferredCommunicationCode(), outParty.getPreferredCommunicationCode());
         } else {
             // assertEquals(Character.UNASSIGNED, outParty.getChangeAction());
             assertNull(outParty.getChangeUser());
             assertNull(outParty.getEmail());
             assertNull(outParty.getFax());
             assertNull(outParty.getIdNumber());
             assertNull(outParty.getIdTypeCode());
             assertNull(outParty.getMobile());
             assertNull(outParty.getPhone());
             assertNull(outParty.getPreferredCommunicationCode());
         }
 
         if (hasAddress) {
             if (party != null) {
                 assertNotNull(outParty.getAddress());
                 assertEquals(party.getAddressId(), outParty.getAddressId());
                 assertAddress(party.getAddress(), null, outParty.getAddress());
             } else {
                 assertNull(outParty.getAddress());
                 assertNull(outParty.getAddressId());
             }
         }
     }
 
     private void assertAddress(Address addr, AddressTO addrTO, Address outAddr) {
         if (addrTO != null) {
             assertEquals(addrTO.getEntityAction(), outAddr.getEntityAction());
             assertEquals(addrTO.getExtAddressId(), outAddr.getExtAddressId());
             assertEquals(addrTO.getId(), outAddr.getId());
             assertEquals(addrTO.getDescription(), outAddr.getDescription());
             assertEquals(addrTO.getRowVersion(), outAddr.getRowVersion());
         } else {
             assertEquals(addr.getEntityAction(), outAddr.getEntityAction());
             assertEquals(addr.getExtAddressId(), outAddr.getExtAddressId());
             assertEquals(addr.getId(), outAddr.getId());
             assertEquals(addr.getDescription(), outAddr.getDescription());
             assertEquals(addr.getRowVersion(), outAddr.getRowVersion());
         }
 
         if (addr != null) {
             // assertEquals(addr.getChangeAction(), outAddr.getChangeAction());
             assertEquals(addr.getChangeUser(), outAddr.getChangeUser());
         } else {
             //assertEquals(Character.UNASSIGNED, outAddr.getChangeAction());
             assertNull(outAddr.getChangeUser());
         }
 
     }
 
     private void assertService(Service service, ServiceTO serviceTO, Service outService) {
 
         assertEquals(serviceTO.getActionCode(), outService.getActionCode());
         assertEquals(serviceTO.getActionNotes(), outService.getActionNotes());
         assertEquals(serviceTO.getApplicationId(), outService.getApplicationId());
         assertEquals(serviceTO.getAreaFee(), outService.getAreaFee());
         assertEquals(serviceTO.getBaseFee(), outService.getBaseFee());
         assertEquals(serviceTO.getEntityAction(), outService.getEntityAction());
         assertEquals(serviceTO.getExpectedCompletionDate(), outService.getExpectedCompletionDate());
         assertEquals(serviceTO.getId(), outService.getId());
         assertEquals(serviceTO.getLodgingDatetime(), outService.getLodgingDatetime());
         assertEquals(serviceTO.getRequestTypeCode(), outService.getRequestTypeCode());
         assertEquals(serviceTO.getRowVersion(), outService.getRowVersion());
         assertEquals(serviceTO.getServiceOrder(), outService.getServiceOrder());
         assertEquals(serviceTO.getStatusCode(), outService.getStatusCode());
         assertEquals(serviceTO.getValueFee(), outService.getValueFee());
 
         if (service != null) {
             //assertEquals(service.getChangeAction(), outService.getChangeAction());
             assertEquals(service.getChangeUser(), outService.getChangeUser());
         } else {
             if (outService.getEntityAction() == EntityAction.DELETE) {
                 //   assertEquals('d', outService.getChangeAction());
             } else {
                 //   assertEquals(Character.UNASSIGNED, outService.getChangeAction());
             }
             assertNull(outService.getChangeUser());
         }
     }
 
     private void assertRequestType(RequestType request, RequestTypeTO requestTO,
             RequestType outRequest) {
 
         assertEquals(requestTO.getCode(), outRequest.getCode());
         assertEquals(requestTO.getDescription(), outRequest.getDescription());
         assertEquals(requestTO.getDisplayValue(), outRequest.getDisplayValue());
         assertEquals(requestTO.getNrDaysToComplete(), outRequest.getNrDaysToComplete());
         assertEquals(requestTO.getNrPropertiesRequired(), outRequest.getNrPropertiesRequired());
         assertEquals(requestTO.getEntityAction(), outRequest.getEntityAction());
         assertEquals(requestTO.getRequestCategoryCode(), outRequest.getRequestCategoryCode());
         assertEquals(requestTO.getStatus(), outRequest.getStatus());
         //assertNotNull(outRequest.getSourceTypeCodes());
 //        assertEquals(requestTO.getSourceTypeCodes().size(), outRequest.getSourceTypeCodes().size());
 //        assertArrayEquals(requestTO.getSourceTypeCodes().toArray(),
 //                outRequest.getSourceTypeCodes().toArray());
 
         if (request != null) {
             assertEquals(request.getAreaBaseFee(), outRequest.getAreaBaseFee());
             assertEquals(request.getBaseFee(), outRequest.getBaseFee());
             assertEquals(request.getValueBaseFee(), outRequest.getValueBaseFee());
         } else {
 //            assertNull(outRequest.getAreaBaseFee());
 //            assertNull(outRequest.getBaseFee());
 //            assertNull(outRequest.getValueBaseFee());
         }
 
     }
 
     private void assertApplicationTO(Application app, ApplicationTO appTO) {
         assertEquals(app.getActionCode(), appTO.getActionCode());
         assertEquals(app.getActionNotes(), appTO.getActionNotes());
         assertEquals(app.getAssignedDatetime(), appTO.getAssignedDatetime());
         assertEquals(app.getAssigneeId(), appTO.getAssigneeId());
         assertEquals(app.getExpectedCompletionDate(), appTO.getExpectedCompletionDate());
         assertEquals(app.getEntityAction(), appTO.getEntityAction());
         assertEquals(app.isFeePaid(), appTO.isFeePaid());
         assertEquals(app.getId(), appTO.getId());
         assertEquals(app.getLodgingDatetime(), appTO.getLodgingDatetime());
         assertArrayEquals(app.getLocation(), appTO.getLocation());
         assertEquals(app.getNr(), appTO.getNr());
         assertEquals(app.getRowVersion(), appTO.getRowVersion());
         assertEquals(app.getStatusCode(), appTO.getStatusCode());
         assertEquals(app.getServicesFee(), appTO.getServicesFee());
         assertEquals(app.getTax(), appTO.getTax());
         assertEquals(app.getTotalAmountPaid(), appTO.getTotalAmountPaid());
         assertEquals(app.getTotalFee(), appTO.getTotalFee());
 
         if (hasAgent) {
             assertNotNull(appTO.getAgent());
             assertEquals(app.getAgentId(), appTO.getAgent().getId());
             assertPartySummaryTO(app.getAgent(), appTO.getAgent());
         }
 
         if (hasContactPerson) {
             assertNotNull(appTO.getContactPerson());
             assertEquals(app.getContactPersonId(), appTO.getContactPerson().getId());
             assertPartyTO(app.getContactPerson(), appTO.getContactPerson());
         }
 
         assertServiceTOList(app.getServiceList(), appTO.getServiceList());
     }
 
     private void assertServiceTOList(List<Service> services, List<ServiceTO> serviceTOs) {
         if (numServices > 0) {
             assertNotNull(serviceTOs);
             assertEquals(numServices, serviceTOs.size());
             for (int i = 0; i < numServices; i++) {
                 assertServiceTO(services.get(i), serviceTOs.get(i));
             }
         }
     }
 
     private void assertPartyTO(Party party, PartyTO partyTO) {
         assertEquals(party.getEmail(), partyTO.getEmail());
         assertEquals(party.getEntityAction(), partyTO.getEntityAction());
         assertEquals(party.getExtId(), partyTO.getExtId());
         assertEquals(party.getFax(), partyTO.getFax());
         assertEquals(party.getId(), partyTO.getId());
         assertEquals(party.getIdNumber(), partyTO.getIdNumber());
         assertEquals(party.getIdTypeCode(), partyTO.getIdTypeCode());
         assertEquals(party.getLastName(), partyTO.getLastName());
         assertEquals(party.getMobile(), partyTO.getMobile());
         assertEquals(party.getName(), partyTO.getName());
         assertEquals(party.getPhone(), partyTO.getPhone());
         assertEquals(party.getPreferredCommunicationCode(), partyTO.getPreferredCommunicationCode());
         assertEquals(party.getRowVersion(), partyTO.getRowVersion());
         assertEquals(party.getTypeCode(), partyTO.getTypeCode());
 
         if (hasAddress) {
             assertNotNull(partyTO.getAddress());
             assertEquals(party.getAddressId(), partyTO.getAddress().getId());
             assertAddressTO(party.getAddress(), partyTO.getAddress());
         }
     }
 
     private void assertPartySummaryTO(Party party, PartySummaryTO partyTO) {
         assertEquals(party.getExtId(), partyTO.getExtId());
         assertEquals(party.getId(), partyTO.getId());
         assertEquals(party.getLastName(), partyTO.getLastName());
         assertEquals(party.getName(), partyTO.getName());
         assertEquals(party.getTypeCode(), partyTO.getTypeCode());
 
     }
 
     private void assertAddressTO(Address addr, AddressTO addrTO) {
         assertEquals(addr.getEntityAction(), addrTO.getEntityAction());
         assertEquals(addr.getExtAddressId(), addrTO.getExtAddressId());
         assertEquals(addr.getId(), addrTO.getId());
         assertEquals(addr.getDescription(), addrTO.getDescription());
         assertEquals(addr.getRowVersion(), addrTO.getRowVersion());
     }
 
     private void assertServiceTO(Service service, ServiceTO serviceTO) {
 
         assertEquals(service.getActionCode(), serviceTO.getActionCode());
         assertEquals(service.getActionNotes(), serviceTO.getActionNotes());
         assertEquals(service.getApplicationId(), serviceTO.getApplicationId());
         assertEquals(service.getAreaFee(), serviceTO.getAreaFee());
         assertEquals(service.getBaseFee(), serviceTO.getBaseFee());
         assertEquals(service.getEntityAction(), serviceTO.getEntityAction());
         assertEquals(service.getExpectedCompletionDate(), serviceTO.getExpectedCompletionDate());
         assertEquals(service.getId(), serviceTO.getId());
         assertEquals(service.getLodgingDatetime(), serviceTO.getLodgingDatetime());
         assertEquals(service.getRequestTypeCode(), serviceTO.getRequestTypeCode());
         assertEquals(service.getRowVersion(), serviceTO.getRowVersion());
         assertEquals(service.getServiceOrder(), serviceTO.getServiceOrder());
         assertEquals(service.getStatusCode(), serviceTO.getStatusCode());
         assertEquals(service.getValueFee(), serviceTO.getValueFee());
     }
 
     private void assertRequestTypeTO(RequestType request, RequestTypeTO requestTO) {
 
         assertEquals(request.getCode(), requestTO.getCode());
         assertEquals(request.getDescription(), requestTO.getDescription());
         assertEquals(request.getDisplayValue(), requestTO.getDisplayValue());
         assertEquals(request.getNrDaysToComplete(), requestTO.getNrDaysToComplete());
         assertEquals(request.getNrPropertiesRequired(), requestTO.getNrPropertiesRequired());
         assertEquals(request.getEntityAction(), requestTO.getEntityAction());
         assertEquals(request.getRequestCategoryCode(), requestTO.getRequestCategoryCode());
         assertEquals(request.getStatus(), requestTO.getStatus());
         //assertNotNull(requestTO.getSourceTypeCodes());
         //assertEquals(request.getSourceTypeCodes().size(), requestTO.getSourceTypeCodes().size());
         //assertArrayEquals(request.getSourceTypeCodes().toArray(),
         //        requestTO.getSourceTypeCodes().toArray());
     }
 
     /**
      * Test toTO for a mocked application entity.
      */
     @Test
     public void testToTO_Application() {
         System.out.println("toTO - ApplicationTO");
         MockEntityFactory factory = new MockEntityFactory();
         Application app = factory.createApplication();
         ApplicationTO result = GenericTranslator.toTO(app, ApplicationTO.class);
         assertNotNull(result);
         assertApplicationTO(app, result);
     }
 
     /**
      * Test toTO with a null entity
      */
     @Test
     public void testToTO_NullEntity() {
         System.out.println("toTO - Null entity");
         RequestTypeTO result = GenericTranslator.toTO(null,
                 RequestTypeTO.class);
         assertNull(result);
     }
 
     /**
      * Test toTO with a list of strings and char characters
      */
     @Test
     public void testToTO_StringListAndChar() {
         System.out.println("toTO - String List and Char");
         MockEntityFactory factory = new MockEntityFactory();
         RequestType requestType = factory.createRequestType("testReqType");
         RequestTypeTO result = GenericTranslator.toTO(requestType,
                 RequestTypeTO.class);
 
         assertNotNull(result);
         assertRequestTypeTO(requestType, result);
     }
 
     @Ignore
     @Test
     public void testToTO_EntityList() {
         System.out.println("toTO - EntityList");
         int num = 1000;
 
         List<Application> apps = new ArrayList<Application>();
         MockEntityFactory factory = new MockEntityFactory();
         for (int i = 0; i < num; i++) {
             apps.add(factory.createApplication());
         }
 
         System.out.println("Translating " + num + " applications...");
         System.out.println("Start -> " + DateUtility.simpleFormat("HH:mm:ss,SSS"));
         List<ApplicationTO> appTOs = GenericTranslator.toTOList(apps, ApplicationTO.class);
         System.out.println("End -> " + DateUtility.simpleFormat("HH:mm:ss,SSS"));
         assertEquals(num, appTOs.size());
 
     }
 
     /**
      * Tests the fromTO methods of Generic Translator using an Application entity
      */
     @Test
     public void testFromTO_Application() {
         System.out.println("FromTO - Application");
         MockEntityFactory factory = new MockEntityFactory();
         Application app = factory.createApplication();
         ApplicationTO appTO = GenericTranslator.toTO(app, ApplicationTO.class);
         assertNotNull(appTO);
         assertApplicationTO(app, appTO);
         Application result = GenericTranslator.fromTO(appTO, Application.class, app);
         assertNotNull(result);
         assertApplication(app, appTO, result);
     }
 
     /**
      * Test fromTO with a null TO
      */
     @Test
     public void testFromTO_NullTO() {
         System.out.println("FromTO - Null TO");
         RequestType result = GenericTranslator.fromTO(null,
                 RequestType.class, null);
         assertNull(result);
     }
 
     /**
      * Test fromTO with a list of strings and char characters
      */
     @Test
     public void testFromTO_StringListAndChar() {
         System.out.println("FromTO - String List and Char");
         MockEntityFactory factory = new MockEntityFactory();
         RequestType requestType = factory.createRequestType("testReqType");
         RequestTypeTO requestTypeTO = GenericTranslator.toTO(requestType,
                 RequestTypeTO.class);
 
         assertNotNull(requestTypeTO);
         assertRequestTypeTO(requestType, requestTypeTO);
 
         RequestType result = GenericTranslator.fromTO(requestTypeTO, RequestType.class,
                 requestType);
         assertNotNull(result);
         assertRequestType(requestType, requestTypeTO, result);
     }
 
     /**
      * Test fromTO with a null entity
      */
     @Test
     public void testFromTO_NullEntity() {
         System.out.println("FromTO - Null Entity Simple");
         MockEntityFactory factory = new MockEntityFactory();
         RequestType requestType = factory.createRequestType("nullEntity");
         RequestTypeTO requestTypeTO = GenericTranslator.toTO(requestType,
                 RequestTypeTO.class);
 
         assertNotNull(requestTypeTO);
         assertRequestTypeTO(requestType, requestTypeTO);
 
         RequestType result = GenericTranslator.fromTO(requestTypeTO,
                 RequestType.class, null);
         assertNotNull(result);
         assertRequestType(null, requestTypeTO, result);
     }
 
     /**
      * Tests the fromTO methods of Generic Translator using an Application entity
      */
     @Test
     public void testFromTO_NullEntityApplication() {
         System.out.println("FromTO - Null Entity Application ");
         MockEntityFactory factory = new MockEntityFactory();
         Application app = factory.createApplication();
         ApplicationTO appTO = GenericTranslator.toTO(app, ApplicationTO.class);
         assertNotNull(appTO);
         assertApplicationTO(app, appTO);
         Application result = GenericTranslator.fromTO(appTO, Application.class, null);
         assertNotNull(result);
         assertApplication(null, appTO, result);
     }
 
     /**
      * Tests that the entity in the list is updated and not replaced with a copy
      */
     @Test
     public void testFromTO_ListUpdate() {
         System.out.println("FromTO - List Update");
         MockEntityFactory factory = new MockEntityFactory();
         Application app = factory.createApplication();
         ApplicationTO appTO = GenericTranslator.toTO(app, ApplicationTO.class);
         assertNotNull(appTO);
         assertApplicationTO(app, appTO);
         app.getServiceList().get(0).setChangeUser("Bob");
         appTO.getServiceList().get(0).setActionNotes("Random Notes");
         Application result = GenericTranslator.fromTO(appTO, Application.class, app);
         assertNotNull(result);
         assertApplication(app, appTO, result);
     }
 
     /**
      * Tests that the entity in the list is updated and not replaced with a copy
      */
     @Test
     public void testFromTO_ChangeAgent() {
         System.out.println("FromTO - Change Agent");
         MockEntityFactory factory = new MockEntityFactory();
         Application app = factory.createApplication();
         ApplicationTO appTO = GenericTranslator.toTO(app, ApplicationTO.class);
         assertNotNull(appTO);
         assertApplicationTO(app, appTO);
 
         appTO.getAgent().setId("changedAgentId");
         Application result = GenericTranslator.fromTO(appTO, Application.class, app);
         assertNotNull(result);
         assertApplication(app, appTO, result);
     }
 
     @Test
     public void testToTOList_FromTOList() {
         System.out.println("ToTOList_FromTOList");
         MockEntityFactory factory = new MockEntityFactory();
         Application app = factory.createApplication();
         List<ServiceTO> serviceTOs = GenericTranslator.toTOList(app.getServiceList(), ServiceTO.class);
         assertNotNull(serviceTOs);
 
         app.getServiceList().add(factory.createService("ser3", 3));
 
         List<Service> result = GenericTranslator.fromTOList(serviceTOs, Service.class, app.getServiceList());
         assertNotNull(result);
         assertEquals(3, result.size());
         assertService(app.getServiceList().get(0), serviceTOs.get(0), result.get(0));
         assertService(app.getServiceList().get(1), serviceTOs.get(1), result.get(1));
         assertEquals("ser3", result.get(2).getId());
     }
 
     @Test
     public void testByteArrayTranslation() {
         System.out.println("ByteArrayTranslation");
         MockEntityFactory factory = new MockEntityFactory();
         Application app = factory.createApplication();
         app.setLocation("abcd".getBytes()); 
         ApplicationTO appTO = GenericTranslator.toTO(app, ApplicationTO.class);
         assertNotNull(appTO);
         assertEquals(app.getLocation().length, appTO.getLocation().length);
         assertEquals("abcd", new String(appTO.getLocation()));
         assertApplicationTO(app, appTO);
         Application result = GenericTranslator.fromTO(appTO, Application.class, app);
         assertNotNull(result);
         assertEquals("abcd", new String(result.getLocation()));
         assertEquals(app.getLocation().length, result.getLocation().length);
         assertApplication(app, appTO, result);
     }
 }
