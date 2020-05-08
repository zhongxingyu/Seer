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
 
 package org.openmrs.module.kenyacore.calculation;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Concept;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Obs;
 import org.openmrs.Program;
 import org.openmrs.api.context.Context;
 import org.openmrs.calculation.patient.PatientCalculationContext;
 import org.openmrs.calculation.patient.PatientCalculationService;
 import org.openmrs.calculation.result.CalculationResultMap;
 import org.openmrs.module.kenyacore.test.TestUtils;
 import org.openmrs.module.metadatadeploy.MetadataUtils;
 import org.openmrs.module.reporting.common.Age;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 
 import java.util.Arrays;
import java.util.Collection;
 import java.util.List;
 
 import static org.hamcrest.Matchers.*;
 
 /**
  * Tests for {@link Calculations}
  */
 public class CalculationsTest extends BaseModuleContextSensitiveTest {
 
 	private List<Integer> cohort = Arrays.asList(2, 6, 7, 8, 999);
 
 	private PatientCalculationContext context;
 
 	/**
 	 * Setup each test
 	 */
 	@Before
 	public void setup() {
 		context = Context.getService(PatientCalculationService.class).createCalculationContext();
 		context.setNow(TestUtils.date(2012, 6, 1));
 	}
 
 	@Test
 	public void integration() {
 		new Calculations();
 	}
 
 	/**
 	 * @see Calculations#alive(java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void alive() {
 		// Patient #6 dies before calculation date
 		TestUtils.getPatient(6).setDead(true);
 		TestUtils.getPatient(6).setDeathDate(TestUtils.date(2012, 1, 1));
 
 		// Patient #8 dies after calculation date
 		TestUtils.getPatient(6).setDead(true);
 		TestUtils.getPatient(6).setDeathDate(TestUtils.date(2012, 1, 1));
 
 		CalculationResultMap results = Calculations.alive(cohort, context);
 
 		Assert.assertThat(((Boolean) results.get(6).getValue()), is(false));
 		Assert.assertThat(((Boolean) results.get(7).getValue()), is(true));
 		Assert.assertThat(((Boolean) results.get(8).getValue()), is(true));
 	}
 
 	/**
 	 * @see Calculations#genders(java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void genders() {
 		CalculationResultMap results = Calculations.genders(cohort, context);
 
 		Assert.assertThat(((String) results.get(6).getValue()), is("M"));
 		Assert.assertThat(((String) results.get(7).getValue()), is("F"));
 	}
 
 	/**
 	 * @see Calculations#ages(java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void ages() {
 		CalculationResultMap results = Calculations.ages(cohort, context);
 
 		Age patient6Age = new Age(TestUtils.date(2007, 5, 27), TestUtils.date(2012, 6, 1));
 
 		Assert.assertThat(((Age) results.get(6).getValue()).getFullYears(), is(patient6Age.getFullYears()));
 		Assert.assertThat(((Age) results.get(6).getValue()).getFullMonths(), is(patient6Age.getFullMonths()));
 	}
 
 	/**
 	 * @see Calculations#allObs(org.openmrs.Concept, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void allObs_shouldReturnAllObsForPatients() throws Exception {
 		// Get all 'WEIGHT' obss
 		Concept weight = Context.getConceptService().getConcept(5089);
 		CalculationResultMap resultMap = Calculations.allObs(weight, cohort, context);
 
		Assert.assertThat((Collection) resultMap.get(6).getValue(), hasSize(0));
		Assert.assertThat((Collection) resultMap.get(7).getValue(), hasSize(3));
 	}
 
 	/**
 	 * @see Calculations#firstObs(org.openmrs.Concept, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void firstObs_shouldReturnFirstObsForPatients() throws Exception {
 		// Get first 'WEIGHT' obss
 		Concept weight = Context.getConceptService().getConcept(5089);
 		CalculationResultMap resultMap = Calculations.firstObs(weight, cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue());
 		Assert.assertThat(((Obs) resultMap.get(7).getValue()).getId(), is(7));
 	}
 
 	/**
 	 * @see Calculations#firstObsOnOrAfter(org.openmrs.Concept, java.util.Date, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void firstObsOnOrAfter_shouldReturnFirstObsForPatientsOnOrAfterDate() throws Exception {
 		// Get first 'WEIGHT' obss after 2008-08-15
 		Concept weight = Context.getConceptService().getConcept(5089);
 		CalculationResultMap resultMap = Calculations.firstObsOnOrAfter(weight, TestUtils.date(2008, 8, 15), cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue());
 		Assert.assertThat(((Obs) resultMap.get(7).getValue()).getId(), is(10));
 	}
 
 	/**
 	 * @see Calculations#lastObs(org.openmrs.Concept, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void lastObs_shouldReturnLastObsForPatients() throws Exception {
 		// Get last 'WEIGHT' obss
 		Concept weight = Context.getConceptService().getConcept(5089);
 		CalculationResultMap resultMap = Calculations.lastObs(weight, cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue());
 		Assert.assertThat(((Obs) resultMap.get(7).getValue()).getId(), is(16));
 	}
 
 	/**
 	 * @see Calculations#lastObsOnOrBefore(org.openmrs.Concept, java.util.Date, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void lastObsOnOrBefore_shouldReturnLastObsForPatientsBeforeOrOnDate() throws Exception {
 		// Get last 'WEIGHT' obss before 2008-08-15
 		Concept weight = Context.getConceptService().getConcept(5089);
 		CalculationResultMap resultMap = Calculations.lastObsOnOrBefore(weight, TestUtils.date(2008, 8, 15), cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue());
 		Assert.assertThat(((Obs) resultMap.get(7).getValue()).getId(), is(10));
 	}
 
 	/**
 	 * @see Calculations#lastObsAtLeastDaysAgo(org.openmrs.Concept, int, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void lastObsAtLeastDaysAgo_shouldReturnLastObsForPatients() throws Exception {
 		// Get last 'WEIGHT' obs at least 1400 days ago
 		Concept weight = Context.getConceptService().getConcept(5089);
 		CalculationResultMap resultMap = Calculations.lastObsAtLeastDaysAgo(weight, 1400, cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue());
 		Assert.assertThat(((Obs) resultMap.get(7).getValue()).getId(), is(7));
 	}
 
 	/**
 	 * @see Calculations#allEncounters(org.openmrs.EncounterType, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void allEncounters_shouldReturnAllEncountersForPatients() throws Exception {
 		// Get any encounters
 		CalculationResultMap resultMap = Calculations.allEncounters(null, cohort, context);
 
		Assert.assertThat((Collection) resultMap.get(6).getValue(), hasSize(0)); // patient #6 has no encounters
		Assert.assertThat((Collection) resultMap.get(7).getValue(), hasSize(3)); // patient #7 has 3 encounters
 
 		// Get 'Scheduled' encounters
 		EncounterType scheduledEncType = Context.getEncounterService().getEncounterType("Scheduled");
 		resultMap = Calculations.allEncounters(scheduledEncType, cohort, context);
 
		Assert.assertThat((Collection) resultMap.get(6).getValue(), hasSize(0)); // patient #6 has no encounters of type 'Scheduled'
		Assert.assertThat((Collection) resultMap.get(7).getValue(), hasSize(2)); // patient #7 has 2 encounters of type 'Scheduled'
 	}
 
 	/**
 	 * @see Calculations#firstEncounter(org.openmrs.EncounterType, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void firstEncounter_shouldReturnFirstEncounterForPatients() throws Exception {
 		// Get first encounter of any type
 		CalculationResultMap resultMap = Calculations.firstEncounter(null, cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue()); // patient #6 has no encounters
 		Assert.assertThat(((Encounter) resultMap.get(7).getValue()).getId(), is(3));
 
 		// Get first 'Scheduled' encounter
 		EncounterType scheduledEncType = Context.getEncounterService().getEncounterType("Scheduled");
 		resultMap = Calculations.firstEncounter(scheduledEncType, cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue());
 		Assert.assertThat(((Encounter) resultMap.get(7).getValue()).getId(), is(4));
 	}
 
 	/**
 	 * @see Calculations#lastEncounter(org.openmrs.EncounterType, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void lastEncounter_shouldReturnLastEncounterForPatients() throws Exception {
 		// Get last encounter
 		CalculationResultMap resultMap = Calculations.lastEncounter(null, cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue()); // patient #6 has no encounters
 		Assert.assertThat(((Encounter) resultMap.get(7).getValue()).getId(), is(5));
 
 		// Get last 'Emergency' encounter
 		EncounterType emergencyEncType = Context.getEncounterService().getEncounterType("Emergency");
 		resultMap = Calculations.lastEncounter(emergencyEncType, cohort, context);
 
 		Assert.assertThat(resultMap.get(6), nullValue());
 		Assert.assertThat(((Encounter) resultMap.get(7).getValue()).getId(), is(3));
 	}
 
 	/**
 	 * @see Calculations#allEnrollments(org.openmrs.Program, java.util.Collection, org.openmrs.calculation.patient.PatientCalculationContext)
 	 */
 	@Test
 	public void allEnrollments_shouldReturnAllEnrollmentsForPatients() throws Exception {
 		// Get 'HIV' enrollments
 		Program hivProgram = MetadataUtils.getProgram("da4a0391-ba62-4fad-ad66-1e3722d16380");
 		CalculationResultMap resultMap = Calculations.allEnrollments(hivProgram, cohort, context);
 
		Assert.assertThat((Collection) resultMap.get(2).getValue(), hasSize(1));
		Assert.assertThat((Collection) resultMap.get(6).getValue(), hasSize(0));
		Assert.assertThat((Collection) resultMap.get(7).getValue(), hasSize(0));
 	}
 }
