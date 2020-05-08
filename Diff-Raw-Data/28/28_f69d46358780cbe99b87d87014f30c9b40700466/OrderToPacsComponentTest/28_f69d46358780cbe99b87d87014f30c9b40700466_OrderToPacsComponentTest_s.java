 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.pacsintegration.component;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentMatcher;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.openmrs.Encounter;
 import org.openmrs.TestOrder;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.OrderService;
 import org.openmrs.api.ProviderService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.order.EmrOrderService;
 import org.openmrs.module.emr.radiology.RadiologyOrder;
 import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
 import org.openmrs.module.pacsintegration.listener.OrderEventListener;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.NotTransactional;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.endsWith;
 import static org.hamcrest.Matchers.startsWith;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.timeout;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 public class OrderToPacsComponentTest extends BaseModuleContextSensitiveTest {
 
     @Autowired
     private EncounterService encounterService;
 
     @Autowired
     private OrderService orderService;
 
     @Autowired
     private LocationService locationService;
 
     @Autowired
     private ProviderService providerService;
 
     @InjectMocks
     @Autowired
     private OrderEventListener orderEventListener;
 
     @Mock
     private PacsIntegrationService pacsIntegrationService;
 
     @Mock
     private EmrOrderService emrOrderService;
 
 	protected final Log log = LogFactory.getLog(getClass());
     protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";
 
     /**
 	 * See http://listarchives.openmrs.org/Limitations-of-H2-for-unit-tests-td7560958.html . This
 	 * avoids: org.h2.jdbc.JdbcSQLException: Timeout trying to lock table "ORDERS"; SQL statement:
 	 * 
 	 * @see org.openmrs.test.BaseContextSensitiveTest#getRuntimeProperties()
 	 */
 	
 	@Before
 	public void setupDatabase() throws Exception {
         MockitoAnnotations.initMocks(this);
 		executeDataSet(XML_DATASET);
 	}
 
 	@Test
 	@NotTransactional
 	public void testPlacingRadiologyOrderShouldTriggerOutgoingMessage() throws Exception {
         RadiologyOrder order = new RadiologyOrder();
 		order.setOrderType(Context.getOrderService().getOrderType(1001));
 		order.setPatient(Context.getPatientService().getPatient(7));
 		order.setConcept(Context.getConceptService().getConcept(18));
 		order.setStartDate(new Date());
         orderService.saveOrder(order);
 
         Mockito.verify(emrOrderService, timeout(5000)).ensureAccessionNumberAssignedTo(order);
 		Mockito.verify(pacsIntegrationService, timeout(5000)).sendMessageToPacs(any(String.class));
 	}
 
     @Test
     @NotTransactional
     public void testSavingOrderWithEncounterShouldTriggerOutgoingMessage() throws Exception {
         RadiologyOrder order = new RadiologyOrder();
         order.setOrderType(Context.getOrderService().getOrderType(1001));
         order.setPatient(Context.getPatientService().getPatient(7));
         order.setConcept(Context.getConceptService().getConcept(18));
         order.setStartDate(new Date());
 
         Encounter encounter = new Encounter();
         encounter.setPatient(Context.getPatientService().getPatient(7));
         encounter.setEncounterDatetime(new Date());
         encounter.addOrder(order);
         encounterService.saveEncounter(encounter);
 
         Mockito.verify(emrOrderService, timeout(5000)).ensureAccessionNumberAssignedTo(order);
         Mockito.verify(pacsIntegrationService, timeout(5000)).sendMessageToPacs(any(String.class));
     }
 
     @Test
     public void testSendingTestOrderShouldNotTriggerOutgoingMessage() throws Exception {
         TestOrder order = new TestOrder();
         order.setOrderType(Context.getOrderService().getOrderType(1001));
         order.setPatient(Context.getPatientService().getPatient(7));
         order.setConcept(Context.getConceptService().getConcept(18));
         order.setStartDate(new Date());
 
         orderService.saveOrder(order);
 
         Mockito.verify(emrOrderService, timeout(10000).never()).ensureAccessionNumberAssignedTo(order);
         Mockito.verify(pacsIntegrationService, timeout(10000).never()).sendMessageToPacs(any(String.class));
     }
 
     @Test
     @NotTransactional
     public void testPlacingRadiologyOrderShouldGenerateProperMessage() throws Exception {
         RadiologyOrder order = new RadiologyOrder();
         order.setOrderType(Context.getOrderService().getOrderType(1001));
         order.setPatient(Context.getPatientService().getPatient(7));
         order.setConcept(Context.getConceptService().getConcept(18));
         order.setStartDate(new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2012"));
         order.setAccessionNumber("A1B2C3");
         order.setExamLocation(locationService.getLocation(1));
 
         Encounter encounter = new Encounter();
         encounter.setPatient(Context.getPatientService().getPatient(7));
         encounter.setEncounterDatetime(new Date());
         encounter.addOrder(order);
         encounter.addProvider(encounterService.getEncounterRole(1003), providerService.getProvider(1));
         encounterService.saveEncounter(encounter);
 
         Mockito.verify(pacsIntegrationService, timeout(5000)).sendMessageToPacs(argThat(new IsExpectedHL7Message()));
     }
 
     public class IsExpectedHL7Message extends ArgumentMatcher<String> {
 
         @Override
         public boolean matches(Object o) {
 
             String hl7Message = (String) o;
 
             assertThat(hl7Message, startsWith("MSH|^~\\&||Mirebalais|||"));
             // TODO: test that a valid date is passed
             assertThat(hl7Message, containsString("||ORM^O01||P|2.3\r"));
             assertThat(hl7Message, containsString("PID|||6TS-4||Chebaskwony^Collet||19760825000000|F\r"));
             assertThat(hl7Message, containsString("PV1|||1FED2^^^^^^^^Unknown Location|||||Test^User^Super\r"));
             assertThat(hl7Message, containsString("ORC|NW\r"));
             assertThat(hl7Message, endsWith("OBR|||A1B2C3|127689^FOOD ASSISTANCE||||||||||||||||||||||||||||||||20120808000000\r"));
 
             return true;
         }
     }
 
 }
