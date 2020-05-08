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
 package org.openmrs.module.auditlog.api;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openmrs.Cohort;
 import org.openmrs.Concept;
 import org.openmrs.ConceptComplex;
 import org.openmrs.ConceptDescription;
 import org.openmrs.ConceptName;
 import org.openmrs.ConceptNumeric;
 import org.openmrs.DrugOrder;
 import org.openmrs.EncounterType;
 import org.openmrs.GlobalProperty;
 import org.openmrs.OpenmrsObject;
 import org.openmrs.Order;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.api.APIException;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.auditlog.AuditLog;
 import org.openmrs.module.auditlog.AuditLog.Action;
 import org.openmrs.module.auditlog.MonitoringStrategy;
 import org.openmrs.module.auditlog.util.AuditLogConstants;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.openmrs.test.Verifies;
 import org.openmrs.util.OpenmrsUtil;
 
 /**
  * Contains tests for methods in {@link AuditLogService}
  */
 public class AuditLogServiceTest extends BaseModuleContextSensitiveTest {
 	
 	private static final String MODULE_TEST_DATA = "moduleTestData.xml";
 	
 	private static final String MODULE_TEST_DATA_AUDIT_LOGS = "moduleTestData-initialAuditLogs.xml";
 	
 	private AuditLogService service;
 	
 	@Before
 	public void before() throws Exception {
 		executeDataSet(MODULE_TEST_DATA);
 		service = Context.getService(AuditLogService.class);
 		setGlobalProperty(AuditLogConstants.GP_MONITORING_STRATEGY, MonitoringStrategy.NONE_EXCEPT.name());
 		Assert.assertEquals(MonitoringStrategy.NONE_EXCEPT, service.getMonitoringStrategy());
 	}
 	
 	private List<AuditLog> getAllAuditLogs() {
 		return service.getAuditLogs(null, null, null, null, null, null);
 	}
 	
 	private void setGlobalProperty(String property, String propertyValue) throws Exception {
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(property);
 		if (gp == null) {
 			gp = new GlobalProperty(property, propertyValue);
 		} else {
 			gp.setPropertyValue(propertyValue);
 		}
 		as.saveGlobalProperty(gp);
 	}
 	
 	/**
 	 * @see {@link AuditLogService#get(Class<T>,Integer)}
 	 */
 	@Test
 	@Verifies(value = "should get the saved object matching the specified arguments", method = "get(Class<T>,Integer)")
 	public void getObjectById_shouldGetTheSavedObjectMatchingTheSpecifiedArguments() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		AuditLog al = service.getObjectById(AuditLog.class, 1);
 		assertEquals("4f7d57f0-9077-11e1-aaa4-00248140a5eb", al.getUuid());
 		
 		//check the child logs
 		assertEquals(2, al.getChildAuditLogs().size());
 		String[] childUuids = new String[2];
 		int index = 0;
 		for (AuditLog child : al.getChildAuditLogs()) {
 			childUuids[index] = child.getUuid();
 			assertEquals(al, child.getParentAuditLog());
 			index++;
 		}
 		assertTrue(ArrayUtils.contains(childUuids, "5f7d57f0-9077-11e1-aaa4-00248140a5ef"));
 		assertTrue(ArrayUtils.contains(childUuids, "6f7d57f0-9077-11e1-aaa4-00248140a5ef"));
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getAuditLogs(Class<*>,List<Action>,Date,Date,Integer,Integer)}
 	 */
 	@Test
 	@Verifies(value = "should match on the specified audit log actions", method = "getAuditLogs(Class<*>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldMatchOnTheSpecifiedAuditLogActions() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		List<Action> actions = new ArrayList<Action>();
 		actions.add(Action.CREATED);//get only inserts
 		Assert.assertEquals(3, service.getAuditLogs(null, actions, null, null, null, null).size());
 		
 		actions.add(Action.UPDATED);//get both insert and update logs
 		Assert.assertEquals(5, service.getAuditLogs(null, actions, null, null, null, null).size());
 		
 		actions.clear();
 		actions.add(Action.UPDATED);//get only updates
 		Assert.assertEquals(2, service.getAuditLogs(null, actions, null, null, null, null).size());
 		
 		actions.clear();
 		actions.add(Action.DELETED);//get only deletes
 		Assert.assertEquals(1, service.getAuditLogs(null, actions, null, null, null, null).size());
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getAuditLogs(Class<*>,List<Action>,Date,Date,Integer,Integer)}
 	 */
 	@Test
 	@Verifies(value = "should return all audit logs in the database if all args are null", method = "getAuditLogs(Class<*>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldReturnAllAuditLogsInTheDatabaseIfAllArgsAreNull() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		Assert.assertEquals(6, getAllAuditLogs().size());
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,
 	 *      Integer,Integer)}
 	 */
 	@Test
 	@Verifies(value = "should match on the specified classes", method = "getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldMatchOnTheSpecifiedClasses() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		List<Class<? extends OpenmrsObject>> clazzes = new ArrayList<Class<? extends OpenmrsObject>>();
 		clazzes.add(Concept.class);
 		Assert.assertEquals(3, service.getAuditLogs(clazzes, null, null, null, null, null).size());
 		clazzes.add(ConceptName.class);
 		Assert.assertEquals(4, service.getAuditLogs(clazzes, null, null, null, null, null).size());
 	}
 	
 	/**
 	 * @see {@link
 	 *      AuditLogService#getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,
 	 *      Integer)}
 	 */
 	@Test
 	@Verifies(value = "should return logs created on or after the specified startDate", method = "getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldReturnLogsCreatedOnOrAfterTheSpecifiedStartDate() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		Calendar cal = Calendar.getInstance();
 		cal.set(2012, 3, 1, 0, 1, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		Date startDate = cal.getTime();
 		Assert.assertEquals(3, service.getAuditLogs(null, null, startDate, null, null, null).size());
 	}
 	
 	/**
 	 * @see {@link
 	 *      AuditLogService#getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,
 	 *      Integer)}
 	 */
 	@Test
 	@Verifies(value = "should return logs created on or before the specified endDate", method = "getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldReturnLogsCreatedOnOrBeforeTheSpecifiedEndDate() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		Calendar cal = Calendar.getInstance();
 		cal.set(2012, 3, 1, 0, 3, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		Date endDate = cal.getTime();
 		Assert.assertEquals(5, service.getAuditLogs(null, null, null, endDate, null, null).size());
 	}
 	
 	/**
 	 * @see {@link
 	 *      AuditLogService#getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,
 	 *      Integer)}
 	 */
 	@Test
 	@Verifies(value = "should return logs created within the specified start and end dates", method = "getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldReturnLogsCreatedWithinTheSpecifiedStartAndEndDates() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.MILLISECOND, 0);
 		cal.set(2012, 3, 1, 0, 0, 1);
 		Date startDate = cal.getTime();
 		cal.set(2012, 3, 1, 0, 3, 1);
 		Date endDate = cal.getTime();
 		Assert.assertEquals(2, service.getAuditLogs(null, null, startDate, endDate, null, null).size());
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,
 	 *      Integer,Integer)}
 	 */
 	@Test(expected = APIException.class)
 	@Verifies(value = "should reject a start date that is in the future", method = "getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldRejectAStartDateThatIsInTheFuture() throws Exception {
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MINUTE, 1);
 		Date startDate = cal.getTime();
 		service.getAuditLogs(null, null, startDate, null, null, null);
 	}
 	
 	/**
 	 * @see {@link
 	 *      AuditLogService#getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,
 	 *      Integer)}
 	 */
 	@Test
 	@Verifies(value = "should ignore end date it it is in the future", method = "getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldIgnoreEndDateItItIsInTheFuture() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MINUTE, 1);
 		Date endDate = cal.getTime();
 		Assert.assertEquals(6, service.getAuditLogs(null, null, null, endDate, null, null).size());
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,
 	 *      Integer,Integer)}
 	 */
 	@Test
 	@Verifies(value = "should sort the logs by date of creation starting with the latest", method = "getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldSortTheLogsByDateOfCreationStartingWithTheLatest() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		List<AuditLog> auditLogs = getAllAuditLogs();
 		Assert.assertFalse(auditLogs.isEmpty());
 		Date currMaxDate = auditLogs.get(0).getDateCreated();
 		for (AuditLog auditLog : auditLogs) {
 			assertTrue(OpenmrsUtil.compare(currMaxDate, auditLog.getDateCreated()) >= 0);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getObjectByUuid(Class<T>,String)}
 	 */
 	@Test
 	@Verifies(value = "should get the saved object matching the specified arguments", method = "getObjectByUuid(Class<T>,String)")
 	public void getObjectByUuid_shouldGetTheSavedObjectMatchingTheSpecifiedArguments() throws Exception {
 		Assert.assertNull(service.getObjectByUuid(GlobalProperty.class, "Unknown uuid"));
 		GlobalProperty gp = service.getObjectByUuid(GlobalProperty.class, "abc05786-9019-11e1-aaa4-00248140a5eb");
 		Assert.assertNotNull(gp);
 		Assert.assertEquals(AuditLogConstants.GP_MONITORING_STRATEGY, gp.getProperty());
 		
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,
 	 *      Integer,Integer)}
 	 */
 	@Test
 	@Verifies(value = "should include logs for subclasses when getting logs by type", method = "getAuditLogs(List<Class<OpenmrsObject>>,List<Action>,Date,Date,Integer,Integer)")
 	public void getAuditLogs_shouldIncludeLogsForSubclassesWhenGettingLogsByType() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		List<Class<? extends OpenmrsObject>> clazzes = new ArrayList<Class<? extends OpenmrsObject>>();
 		clazzes.add(OpenmrsObject.class);
 		Assert.assertEquals(6, service.getAuditLogs(clazzes, null, null, null, null, null).size());
 		clazzes.clear();
 		clazzes.add(Concept.class);
 		Assert.assertEquals(3, service.getAuditLogs(clazzes, null, null, null, null, null).size());
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getMonitoredClasses()}
 	 */
 	@Test
 	@Verifies(value = "should return a set of monitored classes", method = "getMonitoredClasses()")
 	public void getMonitoredClasses_shouldReturnASetOfMonitoredClasses() throws Exception {
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		Assert.assertEquals(5, monitoredClasses.size());
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getUnMonitoredClasses()}
 	 */
 	@Test
 	@Verifies(value = "should return a set of un monitored classes", method = "getUnMonitoredClasses()")
 	public void getUnMonitoredClasses_shouldReturnASetOfUnMonitoredClasses() throws Exception {
 		AdministrationService as = Context.getAdministrationService();
 		//In case previous tests changed the value
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.GP_UN_MONITORED_CLASSES);
 		gp.setPropertyValue(EncounterType.class.getName());
 		as.saveGlobalProperty(gp);
 		Set<Class<?>> unMonitoredClasses = service.getUnMonitoredClasses();
 		Assert.assertEquals(1, unMonitoredClasses.size());
 		Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 	}
 	
 	/**
 	 * @see {@link AuditLogService#startMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Verifies(value = "should update the monitored class names global property if the strategy is none_except", method = "startMonitoring(Set<Class<OpenmrsObject>>)")
 	public void startMonitoring_shouldUpdateTheMonitoredClassNamesGlobalPropertyIfTheStrategyIsNone_except()
 	    throws Exception {
 		Assert.assertEquals(MonitoringStrategy.NONE_EXCEPT, service.getMonitoringStrategy());
 		
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		int originalCount = monitoredClasses.size();
 		Assert.assertFalse(OpenmrsUtil.collectionContains(monitoredClasses, ConceptDescription.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 		
 		try {
 			service.startMonitoring(ConceptDescription.class);
 			
 			monitoredClasses = service.getMonitoredClasses();
 			Assert.assertEquals(++originalCount, monitoredClasses.size());
 			//Should have added it and maintained the existing ones
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptDescription.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 		}
 		finally {
 			//reset
 			service.stopMonitoring(ConceptDescription.class);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#startMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Verifies(value = "should not update any global property if the strategy is all", method = "startMonitoring(Set<Class<OpenmrsObject>>)")
 	public void startMonitoring_shouldNotUpdateAnyGlobalPropertyIfTheStrategyIsAll() throws Exception {
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		int originalMonitoredCount = monitoredClasses.size();
 		Assert.assertEquals(5, monitoredClasses.size());
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 		
 		Set<Class<?>> unMonitoredClasses = service.getUnMonitoredClasses();
 		int originalUnMonitoredCount = unMonitoredClasses.size();
 		Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.GP_MONITORING_STRATEGY);
 		gp.setPropertyValue(MonitoringStrategy.ALL.name());
 		as.saveGlobalProperty(gp);
 		try {
 			service.startMonitoring(EncounterType.class);
 			
 			//Should not have changed
 			monitoredClasses = service.getMonitoredClasses();
 			unMonitoredClasses = service.getUnMonitoredClasses();
 			Assert.assertEquals(originalMonitoredCount, monitoredClasses.size());
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 			
 			Assert.assertEquals(originalUnMonitoredCount, unMonitoredClasses.size());
 			Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		}
 		finally {
 			//reset
 			service.stopMonitoring(EncounterType.class);
 			//gp.setPropertyValue(originalStrategy);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#startMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	//@Test
 	@Verifies(value = "should not update any global property if the strategy is none", method = "startMonitoring(Set<Class<OpenmrsObject>>)")
 	public void startMonitoring_shouldNotUpdateAnyGlobalPropertyIfTheStrategyIsNone() throws Exception {
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		int originalMonitoredCount = monitoredClasses.size();
 		Assert.assertEquals(5, monitoredClasses.size());
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 		
 		Set<Class<?>> unMonitoredClasses = service.getUnMonitoredClasses();
 		int originalUnMonitoredCount = unMonitoredClasses.size();
 		Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.GP_MONITORING_STRATEGY);
 		gp.setPropertyValue(MonitoringStrategy.NONE.name());
 		as.saveGlobalProperty(gp);
 		try {
 			service.startMonitoring(EncounterType.class);
 			
 			//Should not have changed
 			monitoredClasses = service.getMonitoredClasses();
 			unMonitoredClasses = service.getUnMonitoredClasses();
 			Assert.assertEquals(originalMonitoredCount, monitoredClasses.size());
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 			
 			Assert.assertEquals(originalUnMonitoredCount, unMonitoredClasses.size());
 			Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		}
 		finally {
 			service.stopMonitoring(EncounterType.class);
 			//gp.setPropertyValue(originalStrategy);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#startMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Verifies(value = "should update the un monitored class names global property if the strategy is all_except", method = "startMonitoring(Set<Class<OpenmrsObject>>)")
 	public void startMonitoring_shouldUpdateTheUnMonitoredClassNamesGlobalPropertyIfTheStrategyIsAll_except()
 	    throws Exception {
 		Set<Class<?>> unMonitoredClasses = service.getUnMonitoredClasses();
 		int originalCount = unMonitoredClasses.size();
 		Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		Assert.assertFalse(OpenmrsUtil.collectionContains(unMonitoredClasses, Concept.class));
 		Assert.assertFalse(OpenmrsUtil.collectionContains(unMonitoredClasses, ConceptNumeric.class));
 		Assert.assertFalse(OpenmrsUtil.collectionContains(unMonitoredClasses, ConceptComplex.class));
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.GP_MONITORING_STRATEGY);
 		gp.setPropertyValue(MonitoringStrategy.ALL_EXCEPT.name());
 		as.saveGlobalProperty(gp);
 		try {
 			service.stopMonitoring(Concept.class);
 			unMonitoredClasses = service.getUnMonitoredClasses();
 			Assert.assertEquals(originalCount += 3, unMonitoredClasses.size());
 			//Should have removed it and maintained the existing ones
 			Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, Concept.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, ConceptNumeric.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, ConceptComplex.class));
 		}
 		finally {
 			//reset
 			service.startMonitoring(Concept.class);
 			//reset
 			//gp.setPropertyValue(originalStrategy);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#startMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Verifies(value = "should mark a class and its known subclasses as monitored", method = "startMonitoring(Set<Class<OpenmrsObject>>)")
 	public void startMonitoring_shouldMarkAClassAndItsKnownSubclassesAsMonitored() throws Exception {
 		AdministrationService as = Context.getAdministrationService();
 		as.purgeGlobalProperty(as.getGlobalPropertyObject(AuditLogConstants.GP_MONITORED_CLASSES));
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		Assert.assertFalse(monitoredClasses.contains(Order.class));
 		Assert.assertFalse(monitoredClasses.contains(DrugOrder.class));
 		
 		service.startMonitoring(Order.class);
 		monitoredClasses = service.getMonitoredClasses();
 		Assert.assertTrue(monitoredClasses.contains(Order.class));
 		Assert.assertTrue(monitoredClasses.contains(DrugOrder.class));
 	}
 	
 	/**
 	 * @see {@link AuditLogService#stopMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Verifies(value = "should update the monitored class names global property if the strategy is none_except", method = "stopMonitoring(Set<Class<OpenmrsObject>>)")
 	public void stopMonitoring_shouldUpdateTheMonitoredClassNamesGlobalPropertyIfTheStrategyIsNone_except() throws Exception {
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		int originalCount = monitoredClasses.size();
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 		
 		try {
 			service.stopMonitoring(Concept.class);
 			
 			monitoredClasses = service.getMonitoredClasses();
 			Assert.assertEquals(originalCount -= 3, monitoredClasses.size());
 			//Should have added it and maintained the existing ones
 			Assert.assertFalse(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 			Assert.assertFalse(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 			Assert.assertFalse(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 		}
 		finally {
 			//reset
 			service.startMonitoring(Concept.class);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#stopMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Verifies(value = "should not update any global property if the strategy is all", method = "stopMonitoring(Set<Class<OpenmrsObject>>)")
 	public void stopMonitoring_shouldNotUpdateAnyGlobalPropertyIfTheStrategyIsAll() throws Exception {
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		int originalMonitoredCount = monitoredClasses.size();
 		Assert.assertEquals(5, monitoredClasses.size());
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 		
 		Set<Class<?>> unMonitoredClasses = service.getUnMonitoredClasses();
 		int originalUnMonitoredCount = unMonitoredClasses.size();
 		Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.GP_MONITORING_STRATEGY);
 		gp.setPropertyValue(MonitoringStrategy.ALL.name());
 		as.saveGlobalProperty(gp);
 		try {
 			service.stopMonitoring(Concept.class);
 			
 			//Should not have changed
 			monitoredClasses = service.getMonitoredClasses();
 			unMonitoredClasses = service.getUnMonitoredClasses();
 			Assert.assertEquals(originalMonitoredCount, monitoredClasses.size());
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 			
 			Assert.assertEquals(originalUnMonitoredCount, unMonitoredClasses.size());
 			Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		}
 		finally {
 			//reset
 			service.startMonitoring(Concept.class);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#stopMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	//@Test
 	@Verifies(value = "should not update any global property if the strategy is none", method = "stopMonitoring(Set<Class<OpenmrsObject>>)")
 	public void stopMonitoring_shouldNotUpdateAnyGlobalPropertyIfTheStrategyIsNone() throws Exception {
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		int originalMonitoredCount = monitoredClasses.size();
 		Assert.assertEquals(5, monitoredClasses.size());
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 		Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 		
 		Set<Class<?>> unMonitoredClasses = service.getUnMonitoredClasses();
 		int originalUnMonitoredCount = unMonitoredClasses.size();
 		Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.GP_MONITORING_STRATEGY);
 		gp.setPropertyValue(MonitoringStrategy.NONE.name());
 		as.saveGlobalProperty(gp);
 		try {
 			service.stopMonitoring(Concept.class);
 			
 			//Should not have changed
 			monitoredClasses = service.getMonitoredClasses();
 			unMonitoredClasses = service.getUnMonitoredClasses();
 			Assert.assertEquals(originalMonitoredCount, monitoredClasses.size());
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, Concept.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptNumeric.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, ConceptComplex.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, EncounterType.class));
 			Assert.assertTrue(OpenmrsUtil.collectionContains(monitoredClasses, PatientIdentifierType.class));
 			
 			Assert.assertEquals(originalUnMonitoredCount, unMonitoredClasses.size());
 			Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		}
 		finally {
 			service.startMonitoring(Concept.class);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#stopMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Verifies(value = "should update the un monitored class names global property if the strategy is all_except", method = "stopMonitoring(Set<Class<OpenmrsObject>>)")
 	public void stopMonitoring_shouldUpdateTheUnMonitoredClassNamesGlobalPropertyIfTheStrategyIsAll_except()
 	    throws Exception {
 		Set<Class<?>> unMonitoredClasses = service.getUnMonitoredClasses();
 		int originalCount = unMonitoredClasses.size();
 		Assert.assertTrue(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		AdministrationService as = Context.getAdministrationService();
 		GlobalProperty gp = as.getGlobalPropertyObject(AuditLogConstants.GP_MONITORING_STRATEGY);
 		gp.setPropertyValue(MonitoringStrategy.ALL_EXCEPT.name());
 		as.saveGlobalProperty(gp);
 		try {
 			service.startMonitoring(EncounterType.class);
 			unMonitoredClasses = service.getUnMonitoredClasses();
 			Assert.assertEquals(--originalCount, unMonitoredClasses.size());
 			//Should have removed it and maintained the existing ones
 			Assert.assertFalse(OpenmrsUtil.collectionContains(unMonitoredClasses, EncounterType.class));
 		}
 		finally {
 			//reset
 			service.stopMonitoring(EncounterType.class);
 			as.saveGlobalProperty(gp);
 		}
 	}
 	
 	/**
 	 * @see {@link AuditLogService#stopMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Verifies(value = "should mark a class and its known subclasses as un monitored", method = "stopMonitoring(Set<Class<OpenmrsObject>>)")
 	public void stopMonitoring_shouldMarkAClassAndItsKnownSubclassesAsUnMonitored() throws Exception {
 		service.startMonitoring(Concept.class);
 		Set<Class<?>> monitoredClasses = service.getMonitoredClasses();
 		Assert.assertTrue(monitoredClasses.contains(Concept.class));
 		Assert.assertTrue(monitoredClasses.contains(ConceptNumeric.class));
 		Assert.assertTrue(monitoredClasses.contains(ConceptComplex.class));
 		
 		Set<Class<? extends OpenmrsObject>> classes = new HashSet<Class<? extends OpenmrsObject>>();
 		classes.add(Concept.class);
 		service.stopMonitoring(classes);
 		Assert.assertFalse(monitoredClasses.contains(Concept.class));
 		Assert.assertFalse(monitoredClasses.contains(ConceptNumeric.class));
 		Assert.assertFalse(monitoredClasses.contains(ConceptComplex.class));
 	}
 	
 	/**
 	 * @see {@link
 	 *      AuditLogService#getAuditLogs(String,Class<OpenmrsObject>,List<Action>,Date,Date)}
 	 */
 	@Test
 	@Verifies(value = "should get all logs for the object matching the specified uuid", method = "getAuditLogs(String,Class<OpenmrsObject>,List<Action>,Date,Date)")
 	public void getAuditLogs_shouldGetAllLogsForTheObjectMatchingTheSpecifiedUuid() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
 		Assert.assertEquals(2,
 		    service.getAuditLogs("c607c80f-1ea9-4da3-bb88-6276ce8868dd", ConceptNumeric.class, null, null, null).size());
 	}
 	
 	/**
 	 * @see {@link AuditLogService#isMonitored(Class<*>)}
 	 */
 	@Test
 	@Verifies(value = "should true if the class is monitored", method = "isMonitored(Class<*>)")
 	public void isMonitored_shouldTrueIfTheClassIsMonitored() throws Exception {
 		Assert.assertTrue(service.isMonitored(Concept.class));
 		Assert.assertTrue(service.isMonitored(ConceptNumeric.class));
 		Assert.assertTrue(service.isMonitored(EncounterType.class));
 		Assert.assertTrue(service.isMonitored(PatientIdentifierType.class));
 		
 		MonitoringStrategy newStrategy = MonitoringStrategy.ALL_EXCEPT;
 		setGlobalProperty(AuditLogConstants.GP_MONITORING_STRATEGY, newStrategy.name());
 		Assert.assertEquals(newStrategy, service.getMonitoringStrategy());
 		
 		Assert.assertTrue(service.isMonitored(Concept.class));
 		Assert.assertTrue(service.isMonitored(ConceptNumeric.class));
 		Assert.assertTrue(service.isMonitored(PatientIdentifierType.class));
 		Assert.assertTrue(service.isMonitored(Cohort.class));
 	}
 	
 	/**
 	 * @see {@link AuditLogService#isMonitored(Class<*>)}
 	 */
 	@Test
 	@Verifies(value = "should false if the class is not monitored", method = "isMonitored(Class<*>)")
 	public void isMonitored_shouldFalseIfTheClassIsNotMonitored() throws Exception {
 		Assert.assertFalse(service.isMonitored(Cohort.class));
 		
 		MonitoringStrategy newStrategy = MonitoringStrategy.ALL_EXCEPT;
 		setGlobalProperty(AuditLogConstants.GP_MONITORING_STRATEGY, newStrategy.name());
 		Assert.assertEquals(newStrategy, service.getMonitoringStrategy());
 		
 		Assert.assertFalse(service.isMonitored(EncounterType.class));
 	}
 	
 	/**
 	 * @see {@link AuditLogService#startMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Ignore
 	@Verifies(value = "should mark a class and its known subclasses as monitored for all_except strategy", method = "startMonitoring(Set<Class<OpenmrsObject>>)")
 	public void startMonitoring_shouldMarkAClassAndItsKnownSubclassesAsMonitoredForAll_exceptStrategy() throws Exception {
 		//TODO fix me
 		MonitoringStrategy newStrategy = MonitoringStrategy.ALL_EXCEPT;
 		setGlobalProperty(AuditLogConstants.GP_MONITORING_STRATEGY, newStrategy.name());
 		Assert.assertEquals(newStrategy, service.getMonitoringStrategy());
 		Assert.assertTrue(service.isMonitored(Order.class));
 		Assert.assertTrue(service.isMonitored(DrugOrder.class));
 		//mark orders as un monitored for test purposes
 		service.stopMonitoring(Order.class);
 		Assert.assertFalse(service.isMonitored(Order.class));
 		Assert.assertFalse(service.isMonitored(DrugOrder.class));
 		
 		service.startMonitoring(Order.class);
 		Assert.assertTrue(service.isMonitored(Order.class));
 		Assert.assertTrue(service.isMonitored(DrugOrder.class));
 	}
 	
 	/**
 	 * @see {@link AuditLogService#stopMonitoring(Set<Class<OpenmrsObject>>)}
 	 */
 	@Test
 	@Ignore
 	@Verifies(value = "should mark a class and its known subclasses as un monitored for all_except strategy", method = "stopMonitoring(Set<Class<OpenmrsObject>>)")
 	public void stopMonitoring_shouldMarkAClassAndItsKnownSubclassesAsUnMonitoredForAll_exceptStrategy() throws Exception {
 		//TODO Fix me
 		MonitoringStrategy newStrategy = MonitoringStrategy.ALL_EXCEPT;
 		setGlobalProperty(AuditLogConstants.GP_MONITORING_STRATEGY, newStrategy.name());
 		Assert.assertEquals(newStrategy, service.getMonitoringStrategy());
 		Assert.assertTrue(service.isMonitored(Order.class));
 		Assert.assertTrue(service.isMonitored(DrugOrder.class));
 		
 		service.stopMonitoring(Order.class);
 		Assert.assertFalse(service.isMonitored(Order.class));
 		Assert.assertFalse(service.isMonitored(DrugOrder.class));
 	}
 	
 	/**
 	 * @see {@link AuditLogService#getAuditLogs(String,Class<OpenmrsObject>,List<Action>,Date,Date)}
 	 */
 	@Test
 	@Verifies(value = "should include logs for subclasses when getting by type", method = "getAuditLogs(String,Class<OpenmrsObject>,List<Action>,Date,Date)")
 	public void getAuditLogs_shouldIncludeLogsForSubclassesWhenGettingByType() throws Exception {
 		executeDataSet(MODULE_TEST_DATA_AUDIT_LOGS);
		Assert.assertEquals(3, service.getAuditLogs(null, Concept.class, null, null, null)
 		        .size());
 	}
 }
