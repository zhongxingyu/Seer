 /**
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
 package org.openmrs.module.auditlog;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Concept;
 import org.openmrs.ConceptClass;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.ConceptDescription;
 import org.openmrs.ConceptName;
 import org.openmrs.ConceptNumeric;
 import org.openmrs.EncounterType;
 import org.openmrs.GlobalProperty;
 import org.openmrs.Location;
 import org.openmrs.OpenmrsObject;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.auditlog.AuditLog.Action;
 import org.openmrs.module.auditlog.api.AuditLogService;
 import org.openmrs.module.auditlog.util.AuditLogConstants;
 import org.openmrs.module.auditlog.util.AuditLogUtil;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.test.annotation.NotTransactional;
 
 /**
  * Contains tests for testing the core functionality of the module
  */
 @SuppressWarnings("deprecation")
 public class AuditLogBehaviorTest extends BaseModuleContextSensitiveTest {
 	
 	private static final String MODULE_TEST_DATA = "moduleTestData.xml";
 	
 	private ConceptService conceptService;
 	
 	private AuditLogService auditLogService;
 	
 	private EncounterService encounterService;
 	
 	@Before
 	public void before() throws Exception {
 		executeDataSet(MODULE_TEST_DATA);
		//Sanity test to ensure the strategy is none except
 		Assert.assertEquals(MonitoringStrategy.NONE_EXCEPT, AuditLogUtil.getMonitoringStrategy());
 		conceptService = Context.getConceptService();
 		encounterService = Context.getEncounterService();
 		auditLogService = Context.getService(AuditLogService.class);
 		
 		//No log entries should be existing
 		Assert.assertTrue(getAllLogs().isEmpty());
 	}
 	
 	/**
 	 * Utility method to get all logs
 	 * 
 	 * @return a list of {@link AuditLog}s
 	 */
 	private List<AuditLog> getAllLogs() {
 		return auditLogService.getAuditLogs(null, null, null, null, null, null);
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldCreateAnAuditLogEntryWhenANewObjectIsCreated() {
 		Concept concept = new Concept();
 		ConceptName cn = new ConceptName("new", Locale.ENGLISH);
 		cn.setConcept(concept);
 		concept.addName(cn);
 		concept.setDatatype(conceptService.getConceptDatatype(4));
 		concept.setConceptClass(conceptService.getConceptClass(4));
 		conceptService.saveConcept(concept);
 		List<AuditLog> logs = getAllLogs();
 		Assert.assertNotNull(concept.getConceptId());
 		//Should have created an entry for the concept and concept name
 		Assert.assertEquals(2, logs.size());
 		//The latest logs come first
 		Assert.assertEquals(Action.CREATED, logs.get(0).getAction());
 		Assert.assertEquals(Action.CREATED, logs.get(1).getAction());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldCreateAnAuditLogEntryWhenAnObjectIsDeleted() throws Exception {
 		EncounterType encounterType = encounterService.getEncounterType(6);
 		encounterService.purgeEncounterType(encounterType);
 		List<AuditLog> logs = getAllLogs();
 		//Should have created a log entry for deleted Encounter type
 		Assert.assertEquals(1, logs.size());
 		Assert.assertEquals(Action.DELETED, logs.get(0).getAction());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldCreateAnAuditLogEntryWhenAnObjectIsEdited() throws Exception {
 		Concept concept = conceptService.getConcept(3);
 		Integer oldConceptClassId = concept.getConceptClass().getConceptClassId();
 		Integer oldDatatypeId = concept.getDatatype().getConceptDatatypeId();
 		ConceptClass cc = conceptService.getConceptClass(2);
 		ConceptDatatype dt = conceptService.getConceptDatatype(3);
 		String oldVersion = concept.getVersion();
 		String newVersion = "1.11";
 		Assert.assertNotSame(cc, concept.getConceptClass());
 		Assert.assertNotSame(dt, concept.getDatatype());
 		Assert.assertNotSame(newVersion, oldVersion);
 		
 		concept.setConceptClass(cc);
 		concept.setDatatype(dt);
 		concept.setVersion(newVersion);
 		conceptService.saveConcept(concept);
 		
 		List<AuditLog> logs = getAllLogs();
 		//Should have created a log entry for edited concept
 		Assert.assertEquals(1, logs.size());
 		AuditLog auditLog = logs.get(0);
 		
 		//Should have created entries for the changes properties and their old values
 		Assert.assertEquals(Action.UPDATED, auditLog.getAction());
 		//Check that there 3 property tag entries
 		Map<String, String[]> changes = auditLog.getChanges();
 		Assert.assertEquals(3, changes.size());
 		Assert.assertEquals(oldConceptClassId.toString(), changes.get("conceptClass")[1]);
 		Assert.assertEquals(oldDatatypeId.toString(), changes.get("datatype")[1]);
 		Assert.assertEquals(oldVersion, changes.get("version")[1]);
 		
 		Assert.assertEquals(cc.getConceptClassId().toString(), changes.get("conceptClass")[0]);
 		Assert.assertEquals(dt.getConceptDatatypeId().toString(), changes.get("datatype")[0]);
 		Assert.assertEquals(newVersion, changes.get("version")[0]);
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldCreateNoLogEntryIfNoChangesAreMadeToAnExistingObject() throws Exception {
 		EncounterType encounterType = encounterService.getEncounterType(2);
 		encounterService.saveEncounterType(encounterType);
 		Assert.assertTrue(getAllLogs().isEmpty());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldIgnoreDateChangedAndCreatedFields() throws Exception {
 		Concept concept = conceptService.getConcept(3);
 		//sanity checks
 		Assert.assertNull(concept.getDateChanged());
 		Assert.assertNull(concept.getChangedBy());
 		concept.setDateChanged(new Date());
 		concept.setChangedBy(Context.getAuthenticatedUser());
 		conceptService.saveConcept(concept);
 		Assert.assertTrue(getAllLogs().isEmpty());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldHandleInsertsOrUpdatesOrDeletesInEachTransactionIndependently() throws InterruptedException {
 		final int N = 50;
 		final Set<Thread> threads = new LinkedHashSet<Thread>();
 		
 		for (int i = 0; i < N; i++) {
 			threads.add(new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 					try {
 						Context.openSession();
 						Context.authenticate("admin", "test");
 						Integer index = new Integer(Thread.currentThread().getName());
 						EncounterService es = Context.getEncounterService();
 						if (index == 0) {
 							//Let's have a delete
 							EncounterType existingEncounterType = es.getEncounterType(6);
 							Assert.assertNotNull(existingEncounterType);
 							es.purgeEncounterType(existingEncounterType);
 						} else {
 							EncounterType encounterType = null;
 							if (index % 2 == 0) {
 								//And some updates
 								encounterType = es.getEncounterType(2);
 								encounterType.setDescription("New Description-" + index);
 							} else {
 								//And some new rows inserted
 								encounterType = new EncounterType("Encounter Type-" + index, "Description-" + index);
 							}
 							es.saveEncounterType(encounterType);
 						}
 					}
 					finally {
 						Context.closeSession();
 					}
 				}
 			}, new Integer(i).toString()));
 		}
 		
 		for (Thread thread : threads) {
 			thread.start();
 		}
 		
 		for (Thread thread : threads) {
 			thread.join();
 		}
 		
 		Assert.assertEquals(N, getAllLogs().size());
 		
 		List<Action> actions = new ArrayList<Action>();
 		actions.add(Action.CREATED);//should match expected count of created log entries
 		Assert.assertEquals(25, auditLogService.getAuditLogs(null, actions, null, null, null, null).size());
 		
 		actions.clear();
 		actions.add(Action.UPDATED);//should match expected count of updated log entries
 		Assert.assertEquals(24, auditLogService.getAuditLogs(null, actions, null, null, null, null).size());
 		
 		actions.clear();
 		actions.add(Action.DELETED);//should match expected count of deleted log entries
 		Assert.assertEquals(1, auditLogService.getAuditLogs(null, actions, null, null, null, null).size());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldNotCreateAuditLogsForUnMonitoredObjects() {
 		Assert.assertFalse(OpenmrsUtil.collectionContains(AuditLogUtil.getMonitoredClassNames(), Location.class.getName()));
 		Location location = new Location();
 		location.setName("najja");
 		location.setAddress1("test address");
 		Location savedLocation = Context.getLocationService().saveLocation(location);
 		Assert.assertNotNull(savedLocation.getLocationId());//sanity check that it was actually created
 		//Should not have created any logs
 		Assert.assertTrue(getAllLogs().isEmpty());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldIgnoreChangesForStringFieldsFromNullToBlank() throws Exception {
 		PatientService ps = Context.getPatientService();
 		PatientIdentifierType idType = ps.getPatientIdentifierType(1);
 		idType.setFormat(null);
 		ps.savePatientIdentifierType(idType);
 		
 		int originalLogCount = getAllLogs().size();
 		idType.setFormat("");
 		ps.savePatientIdentifierType(idType);
 		Assert.assertEquals(originalLogCount, getAllLogs().size());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldIgnoreChangesForStringFieldsFromBlankToNull() throws Exception {
 		PatientService ps = Context.getPatientService();
 		PatientIdentifierType idType = ps.getPatientIdentifierType(1);
 		idType.setFormat("");
 		idType = ps.savePatientIdentifierType(idType);
 		//this will fail when required version is 1.9 since it converts blanks to null
 		Assert.assertEquals("", idType.getFormat());
 		
 		int originalLogCount = getAllLogs().size();
 		idType.setFormat(null);
 		ps.savePatientIdentifierType(idType);
 		Assert.assertEquals(originalLogCount, getAllLogs().size());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldBeCaseInsensitiveForChangesInStringFields() throws Exception {
 		PatientService ps = Context.getPatientService();
 		PatientIdentifierType idType = ps.getPatientIdentifierType(1);
 		idType.setFormat("test");
 		idType = ps.savePatientIdentifierType(idType);
 		
 		int originalLogCount = getAllLogs().size();
 		idType.setFormat("TEST");
 		ps.savePatientIdentifierType(idType);
 		Assert.assertEquals(originalLogCount, getAllLogs().size());
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldCreateAnAuditLogEntryWhenAnElementIsRemovedFormAChildCollection() throws Exception {
 		Concept concept = conceptService.getConcept(5089);
 		Assert.assertTrue(concept.isNumeric());
 		try {
 			//This is a ConceptNumeric, so we need to mark it as monitored
 			AuditLogUtil.startMonitoring(ConceptNumeric.class);
 			Assert.assertFalse(concept.getConceptMappings().isEmpty());
 			
 			concept.removeDescription(concept.getDescription());
 			conceptService.saveConcept(concept);
 			
 			List<AuditLog> updateLogs = auditLogService.getAuditLogs(null, Collections.singletonList(Action.UPDATED), null,
 			    null, null, null);
 			Assert.assertEquals(1, updateLogs.size());
 		}
 		finally {
 			AuditLogUtil.stopMonitoring(ConceptNumeric.class);
 		}
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldCreateAnAuditLogEntryWhenAnElementIsAddedToAChildCollection() throws Exception {
 		Concept concept = conceptService.getConcept(5089);
 		Assert.assertTrue(concept.isNumeric());
 		try {
 			//This is a ConceptNumeric, so we need to mark it as monitored
 			AuditLogUtil.startMonitoring(ConceptNumeric.class);
 			ConceptDescription cd1 = new ConceptDescription("desc1", Locale.ENGLISH);
 			cd1.setDateCreated(new Date());
 			cd1.setCreator(Context.getAuthenticatedUser());
 			concept.addDescription(cd1);
 			conceptService.saveConcept(concept);
 			
 			List<AuditLog> updateLogs = auditLogService.getAuditLogs(null, Collections.singletonList(Action.UPDATED), null,
 			    null, null, null);
 			Assert.assertEquals(1, updateLogs.size());
 		}
 		finally {
 			AuditLogUtil.stopMonitoring(ConceptNumeric.class);
 		}
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldUpdateTheMonitoredClassCacheWhenTheMonitoredClassGlobalPropertyIsUpdatedWithAnAddition()
 	    throws Exception {
 		Assert.assertFalse(AuditLogUtil.getMonitoredClassNames().contains(ConceptNumeric.class.getName()));
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.AUDITLOG_GP_MONITORED_CLASSES);
 		Set<String> monitoredClasses = new HashSet<String>();
 		try {
 			monitoredClasses.addAll(AuditLogUtil.getMonitoredClassNames());
 			monitoredClasses.add(ConceptNumeric.class.getName());
 			gp.setPropertyValue(StringUtils.join(monitoredClasses, ","));
 			as.saveGlobalProperty(gp);
 			Assert.assertTrue(AuditLogUtil.getMonitoredClassNames().contains(ConceptNumeric.class.getName()));
 		}
 		finally {
 			//reset
 			monitoredClasses.remove(ConceptNumeric.class.getName());
 			gp.setPropertyValue(StringUtils.join(monitoredClasses, ","));
 			as.saveGlobalProperty(gp);
 		}
 		Assert.assertFalse(AuditLogUtil.getMonitoredClassNames().contains(ConceptNumeric.class.getName()));
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldUpdateTheMonitoredClassCacheWhenTheMonitoredClassGlobalPropertyIsUpdatedWithARemoval()
 	    throws Exception {
 		Assert.assertTrue(AuditLogUtil.getMonitoredClassNames().contains(Concept.class.getName()));
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.AUDITLOG_GP_MONITORED_CLASSES);
 		Set<String> monitoredClasses = new HashSet<String>();
 		try {
 			monitoredClasses.addAll(AuditLogUtil.getMonitoredClassNames());
 			monitoredClasses.remove(Concept.class.getName());
 			gp.setPropertyValue(StringUtils.join(monitoredClasses, ","));
 			as.saveGlobalProperty(gp);
 			Assert.assertFalse(AuditLogUtil.getMonitoredClassNames().contains(Concept.class.getName()));
 		}
 		finally {
 			//reset
 			monitoredClasses.add(Concept.class.getName());
 			gp.setPropertyValue(StringUtils.join(monitoredClasses, ","));
 			as.saveGlobalProperty(gp);
 		}
 		Assert.assertTrue(AuditLogUtil.getMonitoredClassNames().contains(Concept.class.getName()));
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldMonitorAnyOpenmrsObjectWhenStrateyIsSetToAll() throws Exception {
 		Assert.assertFalse(AuditLogUtil.getMonitoredClassNames().contains(Location.class.getName()));
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.AUDITLOG_GP_MONITORING_STRATEGY);
 		String originalGpValue = gp.getPropertyValue();
 		try {
 			gp.setPropertyValue(MonitoringStrategy.ALL.name());
 			as.saveGlobalProperty(gp);
 			Location location = new Location();
 			location.setName("new location");
 			Context.getLocationService().saveLocation(location);
 			List<Class<? extends OpenmrsObject>> clazzes = new ArrayList<Class<? extends OpenmrsObject>>();
 			clazzes.add(Location.class);//get only location logs
 			Assert.assertEquals(1, auditLogService.getAuditLogs(clazzes, null, null, null, null, null).size());
 		}
 		finally {
 			//reset
 			gp.setPropertyValue(originalGpValue);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldNotMonitorAnyObjectWhenStrateyIsSetToNone() throws Exception {
 		Assert.assertTrue(AuditLogUtil.getMonitoredClassNames().contains(EncounterType.class.getName()));
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.AUDITLOG_GP_MONITORING_STRATEGY);
 		String originalGpValue = gp.getPropertyValue();
 		try {
 			gp.setPropertyValue(MonitoringStrategy.NONE.name());
 			as.saveGlobalProperty(gp);
 			EncounterType encounterType = encounterService.getEncounterType(6);
 			encounterService.purgeEncounterType(encounterType);
 			Assert.assertEquals(0, getAllLogs().size());
 		}
 		finally {
 			//reset
 			gp.setPropertyValue(originalGpValue);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldNotCreateLogWhenStrateyIsSetToAllExceptAndObjectTypeIsListedAsExcluded() throws Exception {
 		//sanity check
 		Assert.assertTrue(OpenmrsUtil.collectionContains(AuditLogUtil.getUnMonitoredClassNames(),
 		    EncounterType.class.getName()));
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.AUDITLOG_GP_MONITORING_STRATEGY);
 		String originalGpValue = gp.getPropertyValue();
 		try {
 			gp.setPropertyValue(MonitoringStrategy.ALL_EXCEPT.name());
 			as.saveGlobalProperty(gp);
 			
 			EncounterType encounterType = encounterService.getEncounterType(6);
 			encounterService.purgeEncounterType(encounterType);
 			List<Class<? extends OpenmrsObject>> clazzes = new ArrayList<Class<? extends OpenmrsObject>>();
 			clazzes.add(EncounterType.class);//get only encounter type logs since there those of GPs that we have changed
 			Assert.assertEquals(0, auditLogService.getAuditLogs(clazzes, null, null, null, null, null).size());
 		}
 		finally {
 			//reset
 			gp.setPropertyValue(originalGpValue);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	@Test
 	@NotTransactional
 	public void shouldCreateLogWhenStrateyIsSetToAllExceptAndObjectTypeIsNotListedAsIncluded() throws Exception {
 		//sanity check
 		Assert.assertFalse(OpenmrsUtil.collectionContains(AuditLogUtil.getUnMonitoredClassNames(), Location.class.getName()));
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.AUDITLOG_GP_MONITORING_STRATEGY);
 		String originalGpValue = gp.getPropertyValue();
 		try {
 			gp.setPropertyValue(MonitoringStrategy.ALL_EXCEPT.name());
 			as.saveGlobalProperty(gp);
 			
 			Location location = new Location();
 			location.setName("new location");
 			Context.getLocationService().saveLocation(location);
 			List<Class<? extends OpenmrsObject>> clazzes = new ArrayList<Class<? extends OpenmrsObject>>();
 			clazzes.add(Location.class);//get only location logs since there those of GPs that we have changed
 			Assert.assertEquals(1, auditLogService.getAuditLogs(clazzes, null, null, null, null, null).size());
 		}
 		finally {
 			//reset
 			gp.setPropertyValue(originalGpValue);
 			as.saveGlobalProperty(gp);
 		}
 	}
 }
