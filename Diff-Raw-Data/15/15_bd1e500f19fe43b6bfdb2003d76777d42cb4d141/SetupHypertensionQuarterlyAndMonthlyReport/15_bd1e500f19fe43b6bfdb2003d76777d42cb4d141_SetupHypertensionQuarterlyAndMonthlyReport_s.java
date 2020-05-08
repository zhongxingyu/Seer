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
 package org.openmrs.module.rwandareports.reporting;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.openmrs.Concept;
 import org.openmrs.EncounterType;
 import org.openmrs.Form;
 import org.openmrs.Program;
 import org.openmrs.api.PatientSetService.TimeModifier;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
 import org.openmrs.module.reporting.common.RangeComparator;
 import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
 import org.openmrs.module.reporting.evaluation.parameter.Mapped;
 import org.openmrs.module.reporting.evaluation.parameter.Parameter;
 import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
 import org.openmrs.module.reporting.indicator.CohortIndicator;
 import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
 import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.service.ReportService;
 import org.openmrs.module.rwandareports.dataset.EncounterIndicatorDataSetDefinition;
 import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
 import org.openmrs.module.rwandareports.indicator.EncounterIndicator;
 import org.openmrs.module.rwandareports.util.Cohorts;
 import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
 import org.openmrs.module.rwandareports.util.Indicators;
 import org.openmrs.module.rwandareports.widget.AllLocation;
 import org.openmrs.module.rwandareports.widget.LocationHierarchy;
 
 public class SetupHypertensionQuarterlyAndMonthlyReport {
 	
 	public Helper h = new Helper();
 	
 	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
 	
 	// properties
 	private Program hypertensionProgram;
 	
 	private List<Program> hypertensionPrograms = new ArrayList<Program>();
 	
 	private EncounterType hypertensionEncounterType;
 	
 	private List<EncounterType> patientsSeenEncounterTypes = new ArrayList<EncounterType>();
 	
 	private Form DDBform;
 	
 	private Form rendevousForm;
 	
 	private Concept smokingHistory;
 	
 	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
 	
 	private List<String> enrolledOnOrAfterOnOrBefore = new ArrayList<String>();
 	
 	private Concept systolicBP;
 	
 	private Concept diastolicBP;
 	
 	private Concept returnVisitDate;
 	
 	private Concept creatinine;
 	
 	private Concept hydrochlorothiazide;
 	
 	private List<Form> DDBAndRendezvousForms = new ArrayList<Form>();
 	
 	private List<Concept> hypertensionMedications = new ArrayList<Concept>();
 	
 	private List<Concept> captoprilAndLisinopril = new ArrayList<Concept>();
 	
 	public void setup() throws Exception {
 		
 		setUpProperties();
 		
 		//Monthly report set-up
 		ReportDefinition monthlyRd = new ReportDefinition();
 		monthlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		monthlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		Properties properties = new Properties();
 		properties.setProperty("hierarchyFields", "countyDistrict:District");
 		monthlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
 		
 		monthlyRd.setName("NCD-Hypertension Indicator Report-Monthly");
 		
 		monthlyRd.addDataSetDefinition(createMonthlyLocationDataSet(),
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
 		
 		// Quarterly Report Definition: Start
 		
 		ReportDefinition quarterlyRd = new ReportDefinition();
 		quarterlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		quarterlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		quarterlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
 		
 		quarterlyRd.setName("NCD-Hypertension Indicator Report-Quarterly");
 		
 		quarterlyRd.addDataSetDefinition(createQuarterlyLocationDataSet(),
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
 		
 		// Quarterly Report Definition: End
 		
 		ProgramEnrollmentCohortDefinition patientEnrolledInHypertensionProgram = new ProgramEnrollmentCohortDefinition();
 		patientEnrolledInHypertensionProgram.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
 		        Date.class));
 		patientEnrolledInHypertensionProgram.setPrograms(hypertensionPrograms);
 		
 		monthlyRd.setBaseCohortDefinition(patientEnrolledInHypertensionProgram,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
 		
 		quarterlyRd.setBaseCohortDefinition(patientEnrolledInHypertensionProgram,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
 		
 		h.saveReportDefinition(monthlyRd);
 		h.saveReportDefinition(quarterlyRd);
 		
 		ReportDesign monthlyDesign = h.createRowPerPatientXlsOverviewReportDesign(monthlyRd,
 		    "Hypertension_Monthly_Indicator_Report.xls", "Hypertension Monthly Indicator Report (Excel)", null);
 		Properties monthlyProps = new Properties();
 		monthlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Monthly Data Set");
 		monthlyProps.put("sortWeight","5000");
 		monthlyDesign.setProperties(monthlyProps);
 		h.saveReportDesign(monthlyDesign);
 		
 		ReportDesign quarterlyDesign = h.createRowPerPatientXlsOverviewReportDesign(quarterlyRd,
 		    "Hypertension_Indicator_Quarterly_Report.xls", "Hypertension Quarterly Indicator Report (Excel)", null);
 		Properties quarterlyProps = new Properties();
 		quarterlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Quarterly Data Set");
 		quarterlyProps.put("sortWeight","5000");
 		quarterlyDesign.setProperties(quarterlyProps);
 		h.saveReportDesign(quarterlyDesign);
 		
 	}
 	
 	public void delete() {
 		ReportService rs = Context.getService(ReportService.class);
 		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
 			if ("Hypertension Monthly Indicator Report (Excel)".equals(rd.getName())
 			        || "Hypertension Quarterly Indicator Report (Excel)".equals(rd.getName())) {
 				rs.purgeReportDesign(rd);
 			}
 		}
 		h.purgeReportDefinition("NCD-Hypertension Indicator Report-Quarterly");
 		h.purgeReportDefinition("NCD-Hypertension Indicator Report-Monthly");
 		
 	}
 	
 	// Create Monthly Location Data set
 	public LocationHierachyIndicatorDataSetDefinition createMonthlyLocationDataSet() {
 		
 		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
 		        createMonthlyEncounterBaseDataSet());
 		ldsd.addBaseDefinition(createMonthlyBaseDataSet());
 		ldsd.setName("Encounter Monthly Data Set");
 		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
 		
 		return ldsd;
 	}
 	
 	private EncounterIndicatorDataSetDefinition createMonthlyEncounterBaseDataSet() {
 		
 		EncounterIndicatorDataSetDefinition encounterIndicatorDataSetDefinition = new EncounterIndicatorDataSetDefinition();
 		
 		encounterIndicatorDataSetDefinition.setName("encounterIndicatorDataSetDefinition");
 		encounterIndicatorDataSetDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		encounterIndicatorDataSetDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createMonthlyIndicators(encounterIndicatorDataSetDefinition);
 		return encounterIndicatorDataSetDefinition;
 	}
 	
 	private void createMonthlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
 		
 		SqlEncounterQuery patientVisitsToHypertensionClinic = new SqlEncounterQuery();
 		
 		patientVisitsToHypertensionClinic
 		        .setQuery("select encounter_id from encounter where encounter_id in(select encounter_id from encounter where (form_id="
 		                + rendevousForm.getFormId()
 		                + " or form_id="
 		                + DDBform.getFormId()
 		                + ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 )");
 		patientVisitsToHypertensionClinic.setName("patientVisitsToHypertensionClinic");
 		patientVisitsToHypertensionClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientVisitsToHypertensionClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		EncounterIndicator patientVisitsToHypertensionClinicMonthlyIndicator = new EncounterIndicator();
 		patientVisitsToHypertensionClinicMonthlyIndicator.setName("patientVisitsToHypertensionClinicMonthlyIndicator");
 		patientVisitsToHypertensionClinicMonthlyIndicator.setEncounterQuery(new Mapped<EncounterQuery>(
 		        patientVisitsToHypertensionClinic, ParameterizableUtil
 		                .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		
 		dsd.addColumn(patientVisitsToHypertensionClinicMonthlyIndicator);
 		
 		//==============================================================
 		// C2: % of Patient visits in the last quarter with documented BP
 		//==============================================================
 		SqlEncounterQuery patientVisitsWithDocumentedBP = new SqlEncounterQuery();
 		
 		patientVisitsWithDocumentedBP
 		.setQuery("select e.encounter_id from encounter e,obs o where (e.form_id="
 			+ rendevousForm.getFormId()
 			+ " or e.form_id="
 			+ DDBform.getFormId()
 			+ ") and o.encounter_id=e.encounter_id and o.concept_id="+systolicBP.getConceptId()+" and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.voided=0 ");
 		patientVisitsWithDocumentedBP.setName("patientVisitsToHypertensionClinic");
 		patientVisitsWithDocumentedBP.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientVisitsWithDocumentedBP.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		EncounterIndicator patientVisitsWithDocumentedBPIndicator = new EncounterIndicator();
 		patientVisitsWithDocumentedBPIndicator.setName("patientVisitsWithDocumentedBPIndicator");
 		patientVisitsWithDocumentedBPIndicator.setEncounterQuery(new Mapped<EncounterQuery>(
 				patientVisitsWithDocumentedBP, ParameterizableUtil
 				.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		
 		dsd.addColumn(patientVisitsWithDocumentedBPIndicator);
 		
 	}
 	
 	//Create Quarterly Encounter Data set
 	
 	public LocationHierachyIndicatorDataSetDefinition createQuarterlyLocationDataSet() {
 		
 		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
 		        createEncounterQuarterlyBaseDataSet());
 		ldsd.addBaseDefinition(createQuarterlyBaseDataSet());
 		ldsd.setName("Encounter Quarterly Data Set");
 		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
 		
 		return ldsd;
 	}
 	
 	private EncounterIndicatorDataSetDefinition createEncounterQuarterlyBaseDataSet() {
 		
 		EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
 		
 		eidsd.setName("eidsd");
 		eidsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createQuarterlyIndicators(eidsd);
 		return eidsd;
 	}
 	
 	private void createQuarterlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
 		
 		//=======================================================================
 		//  A1: Total # of patient visits to Hypertension clinic in the last quarter
 		//==================================================================
 		SqlEncounterQuery patientVisitsToHypertensionClinic = new SqlEncounterQuery();
 		
 		patientVisitsToHypertensionClinic
 		        .setQuery("select encounter_id from encounter where encounter_id in(select encounter_id from encounter where (form_id="
 		                + rendevousForm.getFormId()
 		                + " or form_id="
 		                + DDBform.getFormId()
 		                + ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 )");
 		patientVisitsToHypertensionClinic.setName("patientVisitsToHypertensionClinic");
 		patientVisitsToHypertensionClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientVisitsToHypertensionClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		EncounterIndicator patientVisitsToHypertensionClinicQuarterlyIndicator = new EncounterIndicator();
 		patientVisitsToHypertensionClinicQuarterlyIndicator.setName("patientVisitsToHypertensionClinicQuarterlyIndicator");
 		patientVisitsToHypertensionClinicQuarterlyIndicator.setEncounterQuery(new Mapped<EncounterQuery>(
 		        patientVisitsToHypertensionClinic, ParameterizableUtil
 		                .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		
 		dsd.addColumn(patientVisitsToHypertensionClinicQuarterlyIndicator);
 		
 		//==============================================================
 		// C2: % of Patient visits in the last quarter with documented BP
 		//==============================================================
 		SqlEncounterQuery patientVisitsWithDocumentedBP = new SqlEncounterQuery();
 		
 		patientVisitsWithDocumentedBP
 		.setQuery("select e.encounter_id from encounter e,obs o where (e.form_id="
 			+ rendevousForm.getFormId()
 			+ " or e.form_id="
 			+ DDBform.getFormId()
 			+ ") and o.encounter_id=e.encounter_id and o.concept_id="+systolicBP.getConceptId()+" and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.voided=0 ");
 		patientVisitsWithDocumentedBP.setName("patientVisitsToHypertensionClinic");
 		patientVisitsWithDocumentedBP.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientVisitsWithDocumentedBP.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		EncounterIndicator patientVisitsWithDocumentedBPIndicator = new EncounterIndicator();
 		patientVisitsWithDocumentedBPIndicator.setName("patientVisitsWithDocumentedBPIndicator");
 		patientVisitsWithDocumentedBPIndicator.setEncounterQuery(new Mapped<EncounterQuery>(
 				patientVisitsWithDocumentedBP, ParameterizableUtil
 				.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		
 		dsd.addColumn(patientVisitsWithDocumentedBPIndicator);
 		
 	}
 	
 	// create monthly cohort Data set
 	
 	private CohortIndicatorDataSetDefinition createMonthlyBaseDataSet() {
 		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
 		dsd.setName("Monthly Cohort Data Set");
 		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createMonthlyIndicators(dsd);
 		return dsd;
 	}
 	
 	// create quarterly cohort Data set
 	private CohortIndicatorDataSetDefinition createQuarterlyBaseDataSet() {
 		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
 		dsd.setName("Quarterly Cohort Data Set");
 		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		createQuarterlyIndicators(dsd);
 		return dsd;
 	}
 	
 	private void createQuarterlyIndicators(CohortIndicatorDataSetDefinition dsd) {
 		
 		//=======================================================================
 		//  A2: Total # of patients seen in the last month/quarter
 		//==================================================================
 		
 		EncounterCohortDefinition patientsWithHypertensionVisit = Cohorts.createEncounterBasedOnForms("patientsWithHypertensionVisit",
 			    onOrAfterOnOrBefore, DDBAndRendezvousForms);
 		patientsWithHypertensionVisit.setEncounterTypeList(patientsSeenEncounterTypes);
 
 		CohortIndicator patientsWithHypertensionVisitIndicator = Indicators.newCountIndicator(
 				"patientsWithHypertensionVisitIndicator", patientsWithHypertensionVisit,
 				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		dsd.addColumn(
 				"A2Q",
 				"Total # of patients seen in the last month",
 				new Mapped(patientsWithHypertensionVisitIndicator, ParameterizableUtil
 						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 			
 		//=======================================================================
 		// A3: Total # of new patients enrolled in the last month/quarter
 		//==================================================================
 		
 		CompositionCohortDefinition patientEnrolledInHypertensionProgram = Cohorts.createEnrolledInProgramDuringPeriod(
 		    "Enrolled In Hypertension Program", hypertensionProgram);
 		
 		CohortIndicator patientEnrolledInHypertensionProgramQuarterIndicator = Indicators.newCountIndicator(
 		    "patientEnrolledInHypertensionProgramQuarterIndicator", patientEnrolledInHypertensionProgram,
 		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "A3Q",
 		    "Total # of new patients enrolled in the last quarter",
 		    new Mapped(patientEnrolledInHypertensionProgramQuarterIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//================================================================
 		// B: Age:  Number of the new patients enrolled in the last quarter
 		//=================================================================
 		
 		//=========
 		//B1: <= 15
 		//=========
 		AgeCohortDefinition under15Cohort = Cohorts.createUnder15AgeCohort("patientsUnder15AtEndDate");
 		
 		CompositionCohortDefinition patientsUnderFifteenComposition = new CompositionCohortDefinition();
 		patientsUnderFifteenComposition.setName("patientsUnderFifteenComposition");
 		patientsUnderFifteenComposition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patientsUnderFifteenComposition.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patientsUnderFifteenComposition.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		patientsUnderFifteenComposition.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		patientsUnderFifteenComposition.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(under15Cohort, ParameterizableUtil
 		            .createParameterMappings("effectiveDate=${effectiveDate}")));
 		patientsUnderFifteenComposition.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsUnderFifteenCountIndicator = Indicators.newCountIndicator(
 		    "patientsUnderFifteenCountIndicator", patientsUnderFifteenComposition,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},effectiveDate=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "B1A",
 		    "<= 15 At the end date",
 		    new Mapped(patientsUnderFifteenCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
 		    "");
 		
 		//=========
 		//B1: 16 to 30
 		//=========
 		AgeCohortDefinition age16To30Cohort = Cohorts.createXtoYAgeCohort("age16To30Cohort", 16, 30);
 		
 		CompositionCohortDefinition patients16To30Cohort = new CompositionCohortDefinition();
 		patients16To30Cohort.setName("patients16To30CohortComposition");
 		patients16To30Cohort.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patients16To30Cohort.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patients16To30Cohort.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		patients16To30Cohort.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		patients16To30Cohort.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(age16To30Cohort, ParameterizableUtil
 		            .createParameterMappings("effectiveDate=${effectiveDate}")));
 		patients16To30Cohort.setCompositionString("1 AND 2");
 		
 		CohortIndicator patients16To30CohortIndicator = Indicators.newCountIndicator("patientsUnderFifteenCountIndicator",
 		    patients16To30Cohort,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},effectiveDate=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn("B1B", "16 to 30 At the end date",
 		    new Mapped(patients16To30CohortIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		
 		//=========
 		//B1: 31 to 45
 		//=========
 		AgeCohortDefinition age31To45Cohort = Cohorts.createXtoYAgeCohort("age31To45Cohort", 31, 45);
 		
 		CompositionCohortDefinition patients31To45Cohort = new CompositionCohortDefinition();
 		patients31To45Cohort.setName("patients31To45CohortComposition");
 		patients31To45Cohort.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patients31To45Cohort.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patients31To45Cohort.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		patients31To45Cohort.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		patients31To45Cohort.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(age31To45Cohort, ParameterizableUtil
 		            .createParameterMappings("effectiveDate=${effectiveDate}")));
 		patients31To45Cohort.setCompositionString("1 AND 2");
 		
 		CohortIndicator patients31To45CohortIndicator = Indicators.newCountIndicator("patients31To45CohortIndicator",
 		    patients31To45Cohort,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},effectiveDate=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn("B1C", "31 to 45 At the end date",
 		    new Mapped(patients31To45CohortIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		//=========
 		//B1: 46 to 60
 		//=========
 		AgeCohortDefinition age46To60Cohort = Cohorts.createXtoYAgeCohort("age46To60Cohort", 46, 60);
 		
 		CompositionCohortDefinition patients46To60Cohort = new CompositionCohortDefinition();
 		patients46To60Cohort.setName("patients46To60CohortComposition");
 		patients46To60Cohort.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patients46To60Cohort.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patients46To60Cohort.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		patients46To60Cohort.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		patients46To60Cohort.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(age46To60Cohort, ParameterizableUtil
 		            .createParameterMappings("effectiveDate=${effectiveDate}")));
 		patients46To60Cohort.setCompositionString("1 AND 2");
 		
 		CohortIndicator patients46To60CohortIndicator = Indicators.newCountIndicator("patients46To60CohortIndicator",
 		    patients46To60Cohort,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},effectiveDate=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn("B1D", "46 to 60 At the end date",
 		    new Mapped(patients46To60CohortIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		
 		//=========
 		//B1: 61 to 75
 		//=========
 		AgeCohortDefinition age61To75Cohort = Cohorts.createXtoYAgeCohort("age61To75Cohort", 61, 75);
 		
 		CompositionCohortDefinition patients61To75Cohort = new CompositionCohortDefinition();
 		patients61To75Cohort.setName("patients61To75CohortComposition");
 		patients61To75Cohort.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patients61To75Cohort.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patients61To75Cohort.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		patients61To75Cohort.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		patients61To75Cohort.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(age61To75Cohort, ParameterizableUtil
 		            .createParameterMappings("effectiveDate=${effectiveDate}")));
 		patients61To75Cohort.setCompositionString("1 AND 2");
 		
 		CohortIndicator patients61To75CohortIndicator = Indicators.newCountIndicator("patients61To75CohortIndicator",
 		    patients61To75Cohort,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},effectiveDate=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn("B1E", "61 to 75 At the end date",
 		    new Mapped(patients61To75CohortIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		
 		//=========
 		//B1: >= 76
 		//=========
 		AgeCohortDefinition over76Cohort = Cohorts.createOverXAgeCohort("over76Cohort", 76);
 		
 		CompositionCohortDefinition patientsOver76Composition = new CompositionCohortDefinition();
 		patientsOver76Composition.setName("patientsUnderFifteenComposition");
 		patientsOver76Composition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patientsOver76Composition.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patientsOver76Composition.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		patientsOver76Composition.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		patientsOver76Composition.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(over76Cohort, ParameterizableUtil
 		            .createParameterMappings("effectiveDate=${effectiveDate}")));
 		patientsOver76Composition.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsOver76CountIndicator = Indicators.newCountIndicator("patientsOver76CountIndicator",
 		    patientsOver76Composition,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},effectiveDate=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn("B1F", ">= 76 At the end date",
 		    new Mapped(patientsOver76CountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		//====================================================================
 		//B2: Gender: Of the new patients enrolled in the last quarter, % male
 		//====================================================================
 		
 		GenderCohortDefinition malePatients = Cohorts.createMaleCohortDefinition("Male patients");
 		
 		CompositionCohortDefinition malePatientsEnrolledInHypertensionProgram = new CompositionCohortDefinition();
 		malePatientsEnrolledInHypertensionProgram.setName("malePatientsEnrolledIn");
 		malePatientsEnrolledInHypertensionProgram.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter",
 		        Date.class));
 		malePatientsEnrolledInHypertensionProgram.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
 		        Date.class));
 		malePatientsEnrolledInHypertensionProgram.addParameter(new Parameter("startDate", "startDate", Date.class));
 		malePatientsEnrolledInHypertensionProgram.addParameter(new Parameter("endDate", "endDate", Date.class));
 		malePatientsEnrolledInHypertensionProgram.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		malePatientsEnrolledInHypertensionProgram.getSearches().put("2", new Mapped<CohortDefinition>(malePatients, null));
 		malePatientsEnrolledInHypertensionProgram.setCompositionString("1 AND 2");
 		
 		CohortIndicator malePatientsEnrolledInHypertensionProgramCountIndicator = Indicators.newCountIndicator(
 		    "malePatientsEnrolledInHypertensionProgram", malePatientsEnrolledInHypertensionProgram,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "B2N",
 		    "Gender: Of the new patients enrolled in the last quarter, number male",
 		    new Mapped(malePatientsEnrolledInHypertensionProgramCountIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================
 		// B3: Of the new patients enrolled in the last quarter, % with Stage I HTN at intake (systolic BP 140-159)
 		//=======================================================
 		
 		SqlCohortDefinition patientsWithSystolicBPGreaterThanOrEqualTo140 = Cohorts
 		        .getPatientsWithObservationInFormBetweenStartAndEndDateAndObsValueGreaterThanOrEqualTo(
 		            "patientsWithSystolicBPGreaterThanOrEqualTo140", DDBform, systolicBP, 140);
 		
 		SqlCohortDefinition patientsWithSystolicBPGreaterThanOrEqualTo160 = Cohorts
 		        .getPatientsWithObservationInFormBetweenStartAndEndDateAndObsValueGreaterThanOrEqualTo(
 		            "patientsWithSystolicBPGreaterThanOrEqualTo160", DDBform, systolicBP, 160); //we use 160 because the comparator in the query uses >= 
 		
 		CompositionCohortDefinition patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159 = new CompositionCohortDefinition();
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159
 		        .setName("patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159");
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159.addParameter(new Parameter("startDate", "startDate",
 		        Date.class));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159.addParameter(new Parameter("endDate", "endDate",
 		        Date.class));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159.addSearch("1",
 		    patientsWithSystolicBPGreaterThanOrEqualTo140,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159.addSearch("2", patientEnrolledInHypertensionProgram,
 				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159.addSearch("3",
 		    patientsWithSystolicBPGreaterThanOrEqualTo160,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159.setCompositionString("1 AND 2 AND (NOT 3)");
 		
 		CohortIndicator patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159Indicator = Indicators
 		        .newCountIndicator("patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159Indicator",
 		            patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159,
 		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		dsd.addColumn(
 		    "B3N",
 		    "Of the new patients enrolled in the last quarter, % with Stage I HTN at intake (systolic BP 140-159)",
 		    new Mapped(patientsEnrolledInTheLastMonthWithSystolicBPBetween140And159Indicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//===============================================================================================
 		// B4: Of the new patients enrolled in the last quarter, % with Stage II HTN at intake (systolic BP 160-179) 
 		//===============================================================================================
 		SqlCohortDefinition patientsWithSystolicBPGreaterThanOrEqualTo180 = Cohorts
 		        .getPatientsWithObservationInFormBetweenStartAndEndDateAndObsValueGreaterThanOrEqualTo(
 		            "patientsWithSystolicBPGreaterThanOrEqualTo180", DDBform, systolicBP, 180);
 		
 		CompositionCohortDefinition patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179 = new CompositionCohortDefinition();
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179
 		        .setName("patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179");
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179.addParameter(new Parameter("startDate", "startDate",
 		        Date.class));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179.addParameter(new Parameter("endDate", "endDate",
 		        Date.class));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179.addSearch("1",
 		    patientsWithSystolicBPGreaterThanOrEqualTo160,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179.addSearch("2", patientEnrolledInHypertensionProgram,
 				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179.addSearch("3",
 		    patientsWithSystolicBPGreaterThanOrEqualTo180,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179.setCompositionString("1 AND 2 AND (NOT 3)");
 		
 		CohortIndicator patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179Indicator = Indicators
 		        .newCountIndicator("patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179Indicator",
 		            patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179,
 		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		dsd.addColumn(
 		    "B4N",
 		    "Of the new patients enrolled in the last quarter, % with Stage I HTN at intake (systolic BP 160-179)",
 		    new Mapped(patientsEnrolledInTheLastMonthWithSystolicBPBetween160And179Indicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// B5: Of the new patients enrolled in the last month, % with Stage III HTN at intake (systolic BP ≥180) 
 		//=======================================================
 		
 		CompositionCohortDefinition patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180 = new CompositionCohortDefinition();
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180
 		        .setName("patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180");
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("startDate",
 		        "startDate", Date.class));
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("endDate", "endDate",
 		        Date.class));
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.addSearch("1",
 		    patientsWithSystolicBPGreaterThanOrEqualTo180,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.addSearch("2",
 		    patientEnrolledInHypertensionProgram, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsWithSystolicBPGreaterThanOrEqualTo180Indicator = Indicators.newCountIndicator(
 		    "patientsWithSystolicBPGreaterThanOrEqualTo180Indicator",
 		    patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		dsd.addColumn(
 		    "B5N",
 		    "Total # of new patients enrolled in the last month with Stage III HTN at intake (systolic BP ≥180) ",
 		    new Mapped(patientsWithSystolicBPGreaterThanOrEqualTo180Indicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// B6: Of the new patients enrolled in the last quarter with Stage III HTN, % with Creatinine test ordered at intake 
 		//=======================================================
 		
 		NumericObsCohortDefinition testedForCreatinine = Cohorts.createNumericObsCohortDefinition(
 		    "patientsTestedForCreatinine", onOrAfterOnOrBefore, creatinine, 0, null, TimeModifier.LAST);
 		
 		CompositionCohortDefinition patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine = new CompositionCohortDefinition();
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine
 		        .setName("patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine");
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine.addParameter(new Parameter(
 		        "enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine.addParameter(new Parameter(
 		        "enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine.addParameter(new Parameter("onOrAfter",
 		        "onOrAfter", Date.class));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine.addParameter(new Parameter("onOrBefore",
 		        "onOrBefore", Date.class));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180,
 		            ParameterizableUtil
 		                    .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(testedForCreatinine, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineIndicator = Indicators
 		        .newCountIndicator(
 		            "patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineIndicator",
 		            patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinine,
 		            ParameterizableUtil
 		                    .createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 		
 		dsd.addColumn(
 		    "B6N",
 		    "Of the new patients enrolled in the last quarter with Stage III HTN, % with Creatinine test ordered at intake ",
 		    new Mapped(patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineIndicator,
 		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// B7: Of the new patients enrolled in the last quarter who also had Cr checked at intake, % with Cr result >200 
 		//=======================================================
 		CompositionCohortDefinition patientsEnrolledInHypertensionProgramAndTestedForCreatinine = new CompositionCohortDefinition();
 		patientsEnrolledInHypertensionProgramAndTestedForCreatinine
 		        .setName("patientsEnrolledInHypertensionProgramAndTestedForCreatinine");
 		patientsEnrolledInHypertensionProgramAndTestedForCreatinine.addParameter(new Parameter("enrolledOnOrAfter",
 		        "enrolledOnOrAfter", Date.class));
 		patientsEnrolledInHypertensionProgramAndTestedForCreatinine.addParameter(new Parameter("enrolledOnOrBefore",
 		        "enrolledOnOrBefore", Date.class));
 		patientsEnrolledInHypertensionProgramAndTestedForCreatinine.addParameter(new Parameter("onOrAfter", "onOrAfter",
 		        Date.class));
 		patientsEnrolledInHypertensionProgramAndTestedForCreatinine.addParameter(new Parameter("onOrBefore", "onOrBefore",
 		        Date.class));
 		patientsEnrolledInHypertensionProgramAndTestedForCreatinine.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		
 		patientsEnrolledInHypertensionProgramAndTestedForCreatinine.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(testedForCreatinine, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		
 		patientsEnrolledInHypertensionProgramAndTestedForCreatinine.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsEnrolledInHypertensionProgramAndTestedForCreatinineIndicator = Indicators
 		        .newCountIndicator(
 		            "patientsEnrolledInHypertensionProgramAndTestedForCreatinineIndicator",
 		            patientsEnrolledInHypertensionProgramAndTestedForCreatinine,
 		            ParameterizableUtil
 		                    .createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 		
 		dsd.addColumn(
 		    "B7D",
 		    "Of the new patients enrolled in the last quarter who also had Cr checked at intake ",
 		    new Mapped(patientsEnrolledInHypertensionProgramAndTestedForCreatinineIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		NumericObsCohortDefinition testedForCreatinineAndResultGreaterTo200 = Cohorts.createNumericObsCohortDefinition(
 		    "testedForCreatinineAndResultGreaterTo200", onOrAfterOnOrBefore, creatinine, 200, RangeComparator.GREATER_THAN,
 		    TimeModifier.LAST);
 		
 		CompositionCohortDefinition patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200 = new CompositionCohortDefinition();
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200
 		        .setName("patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200");
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200
 		        .addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200
 		        .addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200
 		        .addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200
 		        .addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInHypertensionProgram, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(testedForCreatinineAndResultGreaterTo200, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200.getSearches().put(
 		    "3",
 		    new Mapped<CohortDefinition>(testedForCreatinine, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		
 		patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200
 		        .setCompositionString("1 AND 2 AND 3");
 		
 		CohortIndicator patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200Indicator = Indicators
 		        .newCountIndicator(
 		            "PatientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineIndicator",
 		            patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200,
 		            ParameterizableUtil
 		                    .createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate},onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 		
 		dsd.addColumn(
 		    "B7N",
 		    "Of the new patients enrolled in the last quarter with Stage III HTN, % with Creatinine test ordered at intake ",
 		    new Mapped(
 		            patientsEnrolledInHypertensionProgramWithStageIIIHTNAndTestedForCreatinineAndResultGreaterTo200Indicator,
 		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// B8: Of the new patients enrolled in the last quarter, % with smoking status documented 
 		//=======================================================
 		SqlCohortDefinition patientsWithSmokingHistory = Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate(
 		    "patientsWithSmokingHistory", DDBform, smokingHistory);
 		
 		CompositionCohortDefinition patientsEnrolledWithSmokingHistory = new CompositionCohortDefinition();
 		patientsEnrolledWithSmokingHistory.setName("patientsEnrolledWithSmokingHistory");
 		patientsEnrolledWithSmokingHistory.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsEnrolledWithSmokingHistory.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsEnrolledWithSmokingHistory.addSearch("1", patientsWithSmokingHistory,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledWithSmokingHistory.addSearch("2", patientEnrolledInHypertensionProgram,
 				ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
 		patientsEnrolledWithSmokingHistory.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsEnrolledWithSmokingHistoryIndicator = Indicators.newCountIndicator(
 		    "patientsEnrolledWithSmokingHistoryIndicator", patientsEnrolledWithSmokingHistory,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		dsd.addColumn(
 		    "B8N",
 		    "Of the new patients enrolled in the last quarter, % with smoking status documented ",
 		    new Mapped(patientsEnrolledWithSmokingHistoryIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		    //==============================================================
 			// C1: Of active patients, % who had Cr checked at a visit within the past 12 months from end of reporting period
 			//==============================================================
 		
 		CompositionCohortDefinition activePatientsWhoHadCrChecked = new CompositionCohortDefinition();
 		activePatientsWhoHadCrChecked.setName("activePatientsWhoHadCrChecked");
 		activePatientsWhoHadCrChecked.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		activePatientsWhoHadCrChecked.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		activePatientsWhoHadCrChecked.getSearches().put(
 			"1",
 			new Mapped<CohortDefinition>(patientsWithHypertensionVisit,
 					ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
 		
 		activePatientsWhoHadCrChecked.getSearches().put(
 			"2",
 			new Mapped<CohortDefinition>(testedForCreatinine, ParameterizableUtil
 					.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		
 		activePatientsWhoHadCrChecked.setCompositionString("1 AND 2");
 		
 		CohortIndicator activePatientsWhoHadCrCheckedIndicator = Indicators.newCountIndicator(
 			"activePatientsWhoHadCrCheckedIndicator", activePatientsWhoHadCrChecked,
 			ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 		
 		CohortIndicator activePatientsIndicator = Indicators.newCountIndicator(
 			"activePatientsIndicator", patientsWithHypertensionVisit,
 			ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 		
 		dsd.addColumn(
 			"C1D",
 			"Active patients",
 			new Mapped(activePatientsIndicator, ParameterizableUtil
 				.createParameterMappings("endDate=${endDate}")), "");
 		
 		dsd.addColumn(
 			"C1N",
 			"Of active patients, % who had Cr checked at a visit within the past 12 months from end of reporting period",
 			new Mapped(activePatientsWhoHadCrCheckedIndicator, ParameterizableUtil
 				.createParameterMappings("endDate=${endDate}")), "");
 			
 			//=======================================================
 			// D1: Of total patients seen in the last quarter with Stage II-III HTN, % with no HTN-related regimen documented ever
 			//=======================================================
 		CompositionCohortDefinition patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160 = new CompositionCohortDefinition();
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160
 		.setName("patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo160");
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.addParameter(new Parameter("startDate",
 				"startDate", Date.class));
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.addParameter(new Parameter("endDate", "endDate",
 				Date.class));
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.addSearch("1", patientsWithHypertensionVisit,
 				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
 
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.addSearch("2",
 				patientsWithSystolicBPGreaterThanOrEqualTo160,
 				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.setCompositionString("1 AND 2");
 
 		CohortIndicator patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160Indicator = Indicators
 		.newCountIndicator("patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160Indicator",
 				patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160,
 				ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));
 
 		dsd.addColumn(
 				"D1D",
 				"Total patients seen in the last quarter with Stage II-III HTN",
 				new Mapped(patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160Indicator, ParameterizableUtil
 						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 
 		SqlCohortDefinition patientOnRegimen = Cohorts.getPatientsEverNotOnRegimen("patientsOnRegime",
 				hypertensionMedications);
 
 		CompositionCohortDefinition patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen = new CompositionCohortDefinition();
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen
 		.setName("patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen");
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.addParameter(new Parameter("onOrAfter", "onOrAfter",
 				Date.class));
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.addParameter(new Parameter("onOrBefore", "onOrBefore",
 				Date.class));
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.getSearches().put(
 				"1",
 				new Mapped<CohortDefinition>(patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160,
 						ParameterizableUtil.createParameterMappings("endDate=${onOrBefore},startDate=${onOrAfter}")));
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.getSearches().put(
 				"2",
 				new Mapped<CohortDefinition>(patientOnRegimen, null));
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.setCompositionString("1 AND (NOT 2)");
 
 		CohortIndicator patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimenIndicator = Indicators.newCountIndicator(
 				"patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimenIndicator",
 				patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen,
 				ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 
 		dsd.addColumn(
 				"D1N",
 				"Total patients seen in the last month with Stage II-III HTN with no HTN-related regimen documented ever",
 				new Mapped(patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimenIndicator, ParameterizableUtil
 						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 			
 			//=======================================================
 			// D2: Of total patients seen in the last quarter, % on Hydrochlorthiazide
 			//=======================================================
 			SqlCohortDefinition patientsWithCurrentHydrochlorothiazideDrugOrder = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
 			    "patientsWithCurrentHydrochlorothiazideDrugOrder", hydrochlorothiazide);
 			
 			CompositionCohortDefinition patientsOnHydrochlorothiazide = new CompositionCohortDefinition();
 			patientsOnHydrochlorothiazide.setName("patientsOnHydrochlorothiazide");
 			patientsOnHydrochlorothiazide.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 			patientsOnHydrochlorothiazide.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 			patientsOnHydrochlorothiazide.addParameter(new Parameter("endDate", "endDate", Date.class));
 			patientsOnHydrochlorothiazide.addSearch("1", patientsWithHypertensionVisit,
 				ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}"));
 			patientsOnHydrochlorothiazide.addSearch("2", patientsWithCurrentHydrochlorothiazideDrugOrder, 
 					ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
 			patientsOnHydrochlorothiazide.setCompositionString("1 AND 2");
 			
 			CohortIndicator patientsOnHydrochlorothiazideIndicator = Indicators.newCountIndicator(
 			    "patientsOnHydrochlorothiazideIndicator", patientsOnHydrochlorothiazide,
 			    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 			
 			//========================================================
 			//        Adding columns to data set definition         //
 			//========================================================
 			dsd.addColumn(
 			    "D2N",
 			    "Of total patients seen in the last quarter, % on Hydrochlorthiazide",
 			    new Mapped(patientsOnHydrochlorothiazideIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 			
 			//=======================================================
 			// D3: Of total patients seen in the last quarter with Stage III HTN, % on 2 or more antihypertensives
 			//=======================================================
 			SqlCohortDefinition patientsWithStageIIIHTN = Cohorts
 	        .getPatientsWithObservationInFormBetweenStartAndEndDateAndObsValueGreaterThanOrEqualTo(
 	            "patientsWithSystolicBPGreaterThanOrEqualTo160",DDBform, systolicBP, 180);
 			
 			CompositionCohortDefinition patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180 = new CompositionCohortDefinition();
 			patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180
 			        .setName("patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180");
 			patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("startDate",
 			        "startDate", Date.class));
 			patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("endDate", "endDate",
 			        Date.class));
 			patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.addSearch("1", patientsWithHypertensionVisit,
 			    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
 			
 			patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.addSearch("2",
 					patientsWithStageIIIHTN,
 			    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 			
 			patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.setCompositionString("1 AND 2");
 			
 			CohortIndicator patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180Indicator = Indicators
 			        .newCountIndicator("patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180",
 			            patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180,
 			            ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));
 			
 			dsd.addColumn(
 			    "D3D",
 			    "Total patients seen in the last month with Stage III HTN",
 			    new Mapped(patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180Indicator, ParameterizableUtil
 			            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 			
 			SqlCohortDefinition patientOn2OrMoreAntihypertensives = Cohorts.getPatientsOnNOrMoreCurrentRegimenBasedOnEndDate(
 			    "patientsOnRegime", hypertensionMedications, 2);
 			
 			CompositionCohortDefinition patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen = new CompositionCohortDefinition();
 			patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen
 			        .setName("patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen");
 			patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.addParameter(new Parameter("onOrAfter", "onOrAfter",
 			        Date.class));
 			patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.addParameter(new Parameter("onOrBefore",
 			        "onOrBefore", Date.class));
 			patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.getSearches().put(
 			    "1",
 			    new Mapped<CohortDefinition>(patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180,
 			            ParameterizableUtil.createParameterMappings("startDate=${onOrAfter},endDate=${onOrBefore}")));
 			patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.getSearches().put(
 			    "2",
 			    new Mapped<CohortDefinition>(patientOn2OrMoreAntihypertensives, ParameterizableUtil
 			            .createParameterMappings("endDate=${onOrBefore}")));
 			patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.setCompositionString("1 AND 2");
 			
 			CohortIndicator patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimenIndicator = Indicators
 			        .newCountIndicator("patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimenIndicator",
 			            patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen,
 			            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 			
 			dsd.addColumn(
 			    "D3N",
 			    "Of total patients seen in the last month with Stage III HTN, % on 2 or more antihypertensives",
 			    new Mapped(patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimenIndicator, ParameterizableUtil
 			            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 			//=======================================================
 			// D4: Of total patients seen in the last quarter with proteinuria or 100<Cr<200 at last visit, % on ACEI (Captopril or Lisinopril)
 			//=======================================================
 			NumericObsCohortDefinition testedForCreatinineAndResultGreaterTo100 = Cohorts.createNumericObsCohortDefinition(
 			    "testedForCreatinineAndResultGreaterTo100", onOrAfterOnOrBefore, creatinine, 100, RangeComparator.GREATER_THAN,
 			    TimeModifier.LAST);
 			
 			CompositionCohortDefinition patientsSeenAndTestedForCreatinineAndResultBetween100And200 = new CompositionCohortDefinition();
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200
 			        .setName("patientsSeenAndTestedForCreatinineAndResultBetween100And200");
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200
 			        .addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200
 			        .addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200.getSearches().put(
 			    "1",
 			    new Mapped<CohortDefinition>( patientsWithHypertensionVisit,
 						ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200.getSearches().put(
 			    "2",
 			    new Mapped<CohortDefinition>(testedForCreatinineAndResultGreaterTo100, ParameterizableUtil
 			            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 			
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200.getSearches().put(
 			    "3",
 			    new Mapped<CohortDefinition>(testedForCreatinineAndResultGreaterTo200, ParameterizableUtil
 			            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 			
 			
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200
 			        .setCompositionString("1 AND 2 AND (NOT 3)");
 			
 			CohortIndicator patientsSeenAndTestedForCreatinineAndResultBetween100And200Indicator = Indicators
 			        .newCountIndicator(
 			            "patientsSeenAndTestedForCreatinineAndResultBetween100And200Indicator",
 			            patientsSeenAndTestedForCreatinineAndResultBetween100And200,
 			            ParameterizableUtil
 			                    .createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 			
 			dsd.addColumn(
 			    "D4D",
 			    "Of total patients seen in the last quarter with proteinuria or 100<Cr<200) ",
 			    new Mapped(
 			    	patientsSeenAndTestedForCreatinineAndResultBetween100And200Indicator,
 			            ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 			
 			SqlCohortDefinition patientOnCaptoprilOrLisinopril = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate("patientOnCaptoprilOrLisinopril",
 				captoprilAndLisinopril);
 			
 			CompositionCohortDefinition patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril = new CompositionCohortDefinition();
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril
 			.setName("patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril");
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril.addParameter(new Parameter("onOrAfter", "onOrAfter",
 				Date.class));
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril.addParameter(new Parameter("onOrBefore", "onOrBefore",
 				Date.class));
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril.getSearches().put(
 				"1",
 				new Mapped<CohortDefinition>(patientsSeenAndTestedForCreatinineAndResultBetween100And200,
 						ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril.getSearches().put(
 				"2",
 				new Mapped<CohortDefinition>(patientOnCaptoprilOrLisinopril, ParameterizableUtil
 						.createParameterMappings("endDate=${onOrBefore}")));
 			patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril.setCompositionString("1 AND 2");
 			
 			CohortIndicator patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinoprilIndicator = Indicators.newCountIndicator(
 				"patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinoprilIndicator",
 				patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinopril,
 				ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 			
 			dsd.addColumn(
 				"D4N",
 				"Of total patients seen in the last quarter with proteinuria or 100<Cr<200 at last visit, % on ACEI (Captopril or Lisinopril)",
 				new Mapped(patientsSeenAndTestedForCreatinineAndResultBetween100And200AndOnCaptoprilOrLisinoprilIndicator, ParameterizableUtil
 					.createParameterMappings("endDate=${endDate}")), "");
 			
 			
 			//=======================================================================
 			//E1: Of total active patients, % with no visit 28 weeks or more past last visit date
 			//=======================================================================		
 			
 			CompositionCohortDefinition activeAndNotwithHypertensionVisitIn28WeeksPatients = new CompositionCohortDefinition();
 			activeAndNotwithHypertensionVisitIn28WeeksPatients.setName("activeAndNotwithHypertensionVisitIn28WeeksPatients");
 			activeAndNotwithHypertensionVisitIn28WeeksPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 			activeAndNotwithHypertensionVisitIn28WeeksPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 			activeAndNotwithHypertensionVisitIn28WeeksPatients.getSearches().put(
 				"1",
 				new Mapped<CohortDefinition>(patientsWithHypertensionVisit, ParameterizableUtil
 						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 			activeAndNotwithHypertensionVisitIn28WeeksPatients.getSearches().put(
 				"2",
 				new Mapped<CohortDefinition>(patientsWithHypertensionVisit, ParameterizableUtil
 						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter+12m-28w}")));
 			activeAndNotwithHypertensionVisitIn28WeeksPatients.setCompositionString("1 AND (NOT 2)");
 			
 			CohortIndicator activeAndNotwithHypertensionVisitIn28WeeksPatientsIndicator = Indicators.newCountIndicator(
 				"activeAndNotwithHypertensionVisitIn28WeeksPatientsIndicator",
 				activeAndNotwithHypertensionVisitIn28WeeksPatients,
 				ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 			
 			//========================================================
 			//        Adding columns to data set definition         //
 			//========================================================
 			dsd.addColumn(
 				"E1N",
 				"Of total active patients, % with no visit 28 weeks or more past last visit date",
 				new Mapped(activeAndNotwithHypertensionVisitIn28WeeksPatientsIndicator, ParameterizableUtil
 					.createParameterMappings("endDate=${endDate}")), "");
 			
 			//=======================================================================
 			//E2: Of total active patients with Stage III HTN at last visit, % with next scheduled RDV visit 6 weeks or more past last visit date 
 			//==================================================================
 			
 			CompositionCohortDefinition activeAndSystolicBPGreaterThanOrEqualTo180 = new CompositionCohortDefinition();
 			activeAndSystolicBPGreaterThanOrEqualTo180
 			        .setName("activeAndSystolicBPGreaterThanOrEqualTo180");
 			activeAndSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("startDate",
 			        "startDate", Date.class));
 			activeAndSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("endDate", "endDate",
 			        Date.class));
 			activeAndSystolicBPGreaterThanOrEqualTo180.addSearch("1", patientsWithHypertensionVisit, ParameterizableUtil
 				.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-12m+1d}"));
 			
 			activeAndSystolicBPGreaterThanOrEqualTo180.addSearch("2",
 			    patientsWithSystolicBPGreaterThanOrEqualTo180,
 			    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 			
 			activeAndSystolicBPGreaterThanOrEqualTo180.setCompositionString("1 AND 2");
 			
 			CohortIndicator activeAndSystolicBPGreaterThanOrEqualTo180Indicator = Indicators
 			        .newCountIndicator("activeAndSystolicBPGreaterThanOrEqualTo180Indicator",
 			        	activeAndSystolicBPGreaterThanOrEqualTo180,
 			            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 			
 			dsd.addColumn(
 			    "E2D",
 			    "Total active patients with Stage III HTN at last visit",
 			    new Mapped(activeAndSystolicBPGreaterThanOrEqualTo180Indicator, ParameterizableUtil
 			            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 			
 			SqlCohortDefinition patientswithRDV6WeeksOrMorePastLastVisitDate  = new SqlCohortDefinition();
 			patientswithRDV6WeeksOrMorePastLastVisitDate.setQuery("select last_rdv.person_id from (select person_id, value_datetime, obs_datetime, datediff(value_datetime,obs_datetime) from obs o, encounter e where o.encounter_id=e.encounter_id and e.encounter_type="
 					+ hypertensionEncounterType.getId()
 					+ " and o.concept_id="
 					+ returnVisitDate.getConceptId()
 					+ " and datediff(o.value_datetime, o.obs_datetime) > 42 order by o.value_datetime desc) as last_rdv group by last_rdv.person_id");
 			patientswithRDV6WeeksOrMorePastLastVisitDate.setName("patientswithRDV6WeeksOrMorePastLastVisitDate");
 			
 			CompositionCohortDefinition patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate = new CompositionCohortDefinition();
 			patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate.setName("patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate");
 			patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 			patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 			patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate.addSearch("1", activeAndSystolicBPGreaterThanOrEqualTo180,
 				ParameterizableUtil.createParameterMappings("endDate=${onOrBefore},startDate=${onOrAfter}"));
 			patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate.addSearch("2", patientswithRDV6WeeksOrMorePastLastVisitDate,
					"");
 			patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate.setCompositionString("1 AND 2");
 
 			CohortIndicator patientswithRDV6WeeksOrMorePastLastVisitDateIndicator = Indicators.newCountIndicator(
 					"patientswithRDV6WeeksOrMorePastLastVisitDateIndicator", patientsWithStageIIIHTNAndWithRDV6WeeksOrMorePastLastVisitDate,
 					ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
 			//=================================================
 			//     Adding columns to data set definition     //
 			//=================================================
 			dsd.addColumn(
 					"E2N",
 					"Total # of patients with next scheduled RDV visit 6 weeks or more past last visit date",
 					new Mapped(patientswithRDV6WeeksOrMorePastLastVisitDateIndicator, ParameterizableUtil
 							.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 			
 			//=======================================================================
 			//E5: Of total active patients, % with last recorded BP <140/90
 			//=======================================================================		
 			SqlCohortDefinition patientsWithDiastolicBPGreaterThanOrEqualTo90 = Cohorts
 	        .getPatientsWithObservationInFormBetweenStartAndEndDateAndObsValueGreaterThanOrEqualTo(
 	            "patientsWithDiastolicBPGreaterThanOrEqualTo90", DDBform, diastolicBP, 90);
 			
 			CompositionCohortDefinition activewithBPLessThan140To90 = new CompositionCohortDefinition();
 			activewithBPLessThan140To90.setName("activewithBPLessThan140To90");
 			activewithBPLessThan140To90.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 			activewithBPLessThan140To90.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 			activewithBPLessThan140To90.getSearches().put(
 				"1",
 				new Mapped<CohortDefinition>(patientsWithHypertensionVisit, ParameterizableUtil
 						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 			activewithBPLessThan140To90.getSearches().put(
 				"2",
 				new Mapped<CohortDefinition>(patientsWithSystolicBPGreaterThanOrEqualTo140,
 					    ParameterizableUtil.createParameterMappings("startDate=${onOrAfter},endDate=${onOrBefore}")));
 			activewithBPLessThan140To90.getSearches().put(
 				"3",
 				new Mapped<CohortDefinition>(patientsWithDiastolicBPGreaterThanOrEqualTo90,
 					    ParameterizableUtil.createParameterMappings("startDate=${onOrAfter},endDate=${onOrBefore}")));
 			
 			activewithBPLessThan140To90.setCompositionString("1 AND (NOT (2 AND 3))");
 			
 			CohortIndicator activewithBPLessThan140To90Indicator = Indicators.newCountIndicator(
 				"activewithBPLessThan140To90Indicator",
 				activewithBPLessThan140To90,
 				ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 			
 			//========================================================
 			//        Adding columns to data set definition         //
 			//========================================================
 			dsd.addColumn(
 				"E3N",
 				"Of total active patients, % with last recorded BP <140/90",
 				new Mapped(activewithBPLessThan140To90Indicator, ParameterizableUtil
 					.createParameterMappings("endDate=${endDate}")), "");
 
 	}
 	
 	private void createMonthlyIndicators(CohortIndicatorDataSetDefinition dsd) {
 		
 		//=======================================================
 		// A2: Total # of patients seen in the last month
 		//=======================================================
 		
 		SqlCohortDefinition patientsWithHypertensionVisit = new SqlCohortDefinition();
 		patientsWithHypertensionVisit.setQuery("select distinct patient_id from encounter where encounter_type="
 		        + hypertensionEncounterType.getId()
 		        + " and (form_id="
 		        + rendevousForm.getFormId()
 	            + " or form_id="
 	            + DDBform.getFormId()
 		        + ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0");
 		patientsWithHypertensionVisit.setName("patientsWithHypertensionVisit");
 		patientsWithHypertensionVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithHypertensionVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		CohortIndicator patientsWithHypertensionVisitIndicator = Indicators.newCountIndicator(
 		    "patientsWithHypertensionVisitIndicator", patientsWithHypertensionVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		dsd.addColumn(
 		    "A2",
 		    "Total # of patients seen in the last month",
 		    new Mapped(patientsWithHypertensionVisitIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// A3: Total # of new patients enrolled in the last month
 		//=======================================================
 		ProgramEnrollmentCohortDefinition enrolledInHypertensionProgram = Cohorts
 		        .createProgramEnrollmentParameterizedByStartEndDate("enrolledInHypertensionProgram", hypertensionProgram);
 		
 		CohortIndicator enrolledInHypertensionProgramIndicator = Indicators.newCountIndicator(
 		    "enrolledInHypertensionProgramIndicator", enrolledInHypertensionProgram,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
 		
 		dsd.addColumn(
 		    "A3",
 		    "Total # of new patients enrolled in the last month",
 		    new Mapped(enrolledInHypertensionProgramIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// B5: Of the new patients enrolled in the last month, % with Stage III HTN at intake (systolic BP ≥180) 
 		//=======================================================
 		
 		SqlCohortDefinition patientsWithSystolicBPGreaterThanOrEqualTo180 = Cohorts
 		        .getPatientsWithObservationInFormBetweenStartAndEndDateAndObsValueGreaterThanOrEqualTo(
 		            "patientsWithSystolicBPGreaterThanOrEqualTo180", DDBform, systolicBP, 180);
 		
 		CompositionCohortDefinition patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180 = new CompositionCohortDefinition();
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180
 		        .setName("patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180");
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("startDate",
 		        "startDate", Date.class));
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("endDate", "endDate",
 		        Date.class));
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.addSearch("1",
 		    patientsWithSystolicBPGreaterThanOrEqualTo180,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.addSearch("2", enrolledInHypertensionProgram,
		    "");
 		patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsWithSystolicBPGreaterThanOrEqualTo180Indicator = Indicators.newCountIndicator(
 		    "patientsWithSystolicBPGreaterThanOrEqualTo180Indicator",
 		    patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo180,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		dsd.addColumn(
 		    "B5",
 		    "Total # of new patients enrolled in the last month with Stage III HTN at intake (systolic BP ≥180) ",
 		    new Mapped(patientsWithSystolicBPGreaterThanOrEqualTo180Indicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// D1: Of total patients seen in the last month with Stage II-III HTN, % with no HTN-related regimen documented ever
 		//=======================================================
 		
 		SqlCohortDefinition patientsWithSystolicBPGreaterThanOrEqualTo160 = Cohorts
 		        .getPatientsWithObservationInFormBetweenStartAndEndDateAndObsValueGreaterThanOrEqualTo(
 		            "patientsWithSystolicBPGreaterThanOrEqualTo160",DDBform, systolicBP, 160);
 		
 		CompositionCohortDefinition patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160 = new CompositionCohortDefinition();
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160
 		        .setName("patientsEnrolledInTheLastMonthWithSystolicBPGreaterThanOrEqualTo160");
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.addParameter(new Parameter("startDate",
 		        "startDate", Date.class));
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.addParameter(new Parameter("endDate", "endDate",
 		        Date.class));
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.addSearch("1", patientsWithHypertensionVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.addSearch("2",
 		    patientsWithSystolicBPGreaterThanOrEqualTo160,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160Indicator = Indicators
 		        .newCountIndicator("patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160Indicator",
 		            patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160,
 		            ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
 		
 		dsd.addColumn(
 		    "D1D",
 		    "Total patients seen in the last month with Stage II-III HTN",
 		    new Mapped(patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160Indicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		SqlCohortDefinition patientOnRegimen = Cohorts.getPatientsEverNotOnRegimen("patientsOnRegime",
 		    hypertensionMedications);
 		
 		CompositionCohortDefinition patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen = new CompositionCohortDefinition();
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen
 		        .setName("patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen");
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.addParameter(new Parameter("onOrAfter", "onOrAfter",
 		        Date.class));
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.addParameter(new Parameter("onOrBefore", "onOrBefore",
 		        Date.class));
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo160,
 		            ParameterizableUtil.createParameterMappings("endDate=${onOrBefore},startDate=${onOrAfter}")));
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientOnRegimen, null));
 		patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen.setCompositionString("1 AND (NOT 2)");
 		
 		CohortIndicator patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimenIndicator = Indicators.newCountIndicator(
 		    "patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimenIndicator",
 		    patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimen,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
 		
 		dsd.addColumn(
 		    "D1N",
 		    "Total patients seen in the last month with Stage II-III HTN with no HTN-related regimen documented ever",
 		    new Mapped(patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimenIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// D2: Of total patients seen in the last month with Stage III HTN, % on 2 or more antihypertensives
 		//=======================================================
 		
 		SqlCohortDefinition patientsWithStageIIIHTN = Cohorts
         .getPatientsWithObservationInFormBetweenStartAndEndDateAndObsValueGreaterThanOrEqualTo(
             "patientsWithSystolicBPGreaterThanOrEqualTo160",DDBform, systolicBP, 180);
 		
 		CompositionCohortDefinition patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180 = new CompositionCohortDefinition();
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180
 		        .setName("patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180");
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("startDate",
 		        "startDate", Date.class));
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.addParameter(new Parameter("endDate", "endDate",
 		        Date.class));
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.addSearch("1", patientsWithHypertensionVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.addSearch("2",
 				patientsWithStageIIIHTN,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180Indicator = Indicators
 		        .newCountIndicator("patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180",
 		            patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180,
 		            ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
 		
 		dsd.addColumn(
 		    "D2D",
 		    "Total patients seen in the last month with Stage III HTN",
 		    new Mapped(patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180Indicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		SqlCohortDefinition patientOn2OrMoreAntihypertensives = Cohorts.getPatientsOnNOrMoreCurrentRegimenBasedOnEndDate(
 		    "patientsOnRegime", hypertensionMedications, 2);
 		
 		CompositionCohortDefinition patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen = new CompositionCohortDefinition();
 		patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen
 		        .setName("patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen");
 		patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.addParameter(new Parameter("onOrAfter", "onOrAfter",
 		        Date.class));
 		patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.addParameter(new Parameter("onOrBefore",
 		        "onOrBefore", Date.class));
 		patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsWithHypertensionVisitAndSystolicBPGreaterThanOrEqualTo180,
 		            ParameterizableUtil.createParameterMappings("startDate=${onOrAfter},endDate=${onOrBefore}")));
 		patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientOn2OrMoreAntihypertensives, ParameterizableUtil
 		            .createParameterMappings("endDate=${onOrBefore}")));
 		patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimenIndicator = Indicators
 		        .newCountIndicator("patientsWithHypertensionVisitAndNotOnAnyHypertensionRegimenIndicator",
 		            patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimen,
 		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
 		
 		dsd.addColumn(
 		    "D2N",
 		    "Of total patients seen in the last month with Stage III HTN, % on 2 or more antihypertensives",
 		    new Mapped(patientsWithHypertensionVisitAndOnMoreThan2HypertensionRegimenIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 	}
 	
 	private void setUpProperties() {
 		hypertensionProgram = gp.getProgram(GlobalPropertiesManagement.HYPERTENSION_PROGRAM);
 		
 		hypertensionPrograms.add(hypertensionProgram);
 		
 		hypertensionEncounterType = gp.getEncounterType(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER);
 		
 		patientsSeenEncounterTypes.add(hypertensionEncounterType);
 		
 		DDBform = gp.getForm(GlobalPropertiesManagement.HYPERTENSION_DDB);
 		
 		rendevousForm = gp.getForm(GlobalPropertiesManagement.HYPERTENSION_FLOW_VISIT);
 		
 		DDBAndRendezvousForms.add(rendevousForm);
 		
 		DDBAndRendezvousForms.add(DDBform);
 		
 		onOrAfterOnOrBefore.add("onOrAfter");
 		
 		onOrAfterOnOrBefore.add("onOrBefore");
 		
 		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrAfter");
 		
 		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrBefore");
 		
 		systolicBP = gp.getConcept(GlobalPropertiesManagement.SYSTOLIC_BLOOD_PRESSURE);
 		
 		diastolicBP = gp.getConcept(GlobalPropertiesManagement.DIASTOLIC_BLOOD_PRESSURE);
 		
 		hypertensionMedications = gp
         .getConceptsByConceptSet(GlobalPropertiesManagement.HYPERTENSION_TREATMENT_DRUGS);
 		
 		smokingHistory = gp.getConcept(GlobalPropertiesManagement.SMOKING_HISTORY);
 		
 		creatinine = gp.getConcept(GlobalPropertiesManagement.SERUM_CREATININE);
 		
 		hydrochlorothiazide = gp.getConcept(GlobalPropertiesManagement.HYDROCHLOROTHIAZIDE_DRUG);
 		
 		captoprilAndLisinopril.add(gp.getConcept(GlobalPropertiesManagement.LISINOPRIL));
 		
 		captoprilAndLisinopril.add(gp.getConcept(GlobalPropertiesManagement.CAPTOPRIL));
 		
 		returnVisitDate = gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
 		
 		
 	}
 }
