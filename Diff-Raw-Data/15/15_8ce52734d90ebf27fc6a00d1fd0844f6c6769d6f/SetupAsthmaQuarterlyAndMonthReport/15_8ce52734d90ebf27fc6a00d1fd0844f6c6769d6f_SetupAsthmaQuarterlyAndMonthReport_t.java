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
 import org.openmrs.ProgramWorkflowState;
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
 import org.openmrs.module.rwandareports.util.RwandaReportsUtil;
 import org.openmrs.module.rwandareports.widget.AllLocation;
 import org.openmrs.module.rwandareports.widget.LocationHierarchy;
 
 public class SetupAsthmaQuarterlyAndMonthReport {
 	
 	public Helper h = new Helper();
 	
 	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
 	
 	// properties
 	private Program asthmaProgram;
 	
 	private List<Program> asthmaPrograms = new ArrayList<Program>();
 	
 	private EncounterType asthmaEncounterType;
 	
 	private Form DDBform;
 	
 	private List<EncounterType> patientsSeenEncounterTypes = new ArrayList<EncounterType>();
 	
 	private Form rendevousForm;
 	
 	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
 	
 	private List<String> enrolledOnOrAfterOnOrBefore = new ArrayList<String>();
 	
 	//private List<Concept> peacFlowConcepts = new ArrayList<Concept>();
 	
 	private Concept peakFlowAfterSalbutamol;
 	
 	private Concept peakFlowBeforeSalbutamol;
 	
 	private Concept smokingHistory;
 	
 	private Concept salbutamol;
 	
 	private Concept beclomethasone;
 	
 	private Concept prednisolone;
 	
 	private Concept locOfHosp;
 	
 	private Concept severePersistentAsthma;
 	
 	private Concept severeUncontrolledAsthma;
 	
 	private Concept asthmaclassification;
 	
 	private Concept returnVisitDate;
 	
 	private Concept basicInhalerTrainingProvided;
 	
 	private Concept properInhalerTechnique;
 	
 	private List<Form> DDBforms = new ArrayList<Form>();
 	
 	private List<Concept> asthmasMedications = new ArrayList<Concept>();
 	
 	private List<Concept> asthmasClassificationAnswers = new ArrayList<Concept>();
 	
 	private List<Concept> asthmasMedicationsWithoutSalbutamol = new ArrayList<Concept>();
 	
 	private ProgramWorkflowState patientDied;
 	
 	public void setup() throws Exception {
 		
 		setUpProperties();
 		
 		//Monthly report set-up
 		ReportDefinition monthlyRd = new ReportDefinition();
 		monthlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		monthlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		Properties properties = new Properties();
 		properties.setProperty("hierarchyFields", "countyDistrict:District");
 		monthlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
 		
 		monthlyRd.setName("NCD-Asthma Indicator Report-Monthly");
 		
 		monthlyRd.addDataSetDefinition(createMonthlyLocationDataSet(),
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
 		
 		// Quarterly Report Definition: Start
 		
 		ReportDefinition quarterlyRd = new ReportDefinition();
 		quarterlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		quarterlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		quarterlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
 		
 		quarterlyRd.setName("NCD-Asthma Indicator Report-Quarterly");
 		
 		quarterlyRd.addDataSetDefinition(createQuarterlyLocationDataSet(),
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
 		
 		// Quarterly Report Definition: End
 		
 		ProgramEnrollmentCohortDefinition patientEnrolledInAsthmaProgram = new ProgramEnrollmentCohortDefinition();
 		patientEnrolledInAsthmaProgram.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patientEnrolledInAsthmaProgram.setPrograms(asthmaPrograms);
 		
 		monthlyRd.setBaseCohortDefinition(patientEnrolledInAsthmaProgram,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
 		
 		quarterlyRd.setBaseCohortDefinition(patientEnrolledInAsthmaProgram,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
 		
 		h.saveReportDefinition(monthlyRd);
 		h.saveReportDefinition(quarterlyRd);
 		
 		ReportDesign monthlyDesign = h.createRowPerPatientXlsOverviewReportDesign(monthlyRd,
 		    "Asthma_Indicator_Monthly_Report.xls", "Asthma Indicator Monthly Report (Excel)", null);
 		Properties monthlyProps = new Properties();
 		monthlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Monthly Data Set");
 		monthlyProps.put("sortWeight","5000");
 		monthlyDesign.setProperties(monthlyProps);
 		h.saveReportDesign(monthlyDesign);
 		
 		ReportDesign quarterlyDesign = h.createRowPerPatientXlsOverviewReportDesign(quarterlyRd,
 		    "Asthma_Indicator_Quarterly_Report.xls", "Asthma Indicator Quarterly Report (Excel)", null);
 		Properties quarterlyProps = new Properties();
 		quarterlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Quarterly Data Set");
 		quarterlyProps.put("sortWeight","5000");
 		quarterlyDesign.setProperties(quarterlyProps);
 		h.saveReportDesign(quarterlyDesign);
 		
 	}
 	
 	public void delete() {
 		ReportService rs = Context.getService(ReportService.class);
 		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
 			if ("Asthma Indicator Monthly Report (Excel)".equals(rd.getName())
 			        || "Asthma Indicator Quarterly Report (Excel)".equals(rd.getName())) {
 				rs.purgeReportDesign(rd);
 			}
 		}
 		h.purgeReportDefinition("NCD-Asthma Indicator Report-Quarterly");
 		h.purgeReportDefinition("NCD-Asthma Indicator Report-Monthly");
 		
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
 		
 		EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
 		
 		eidsd.setName("eidsd");
 		eidsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
 		eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createMonthlyIndicators(eidsd);
 		return eidsd;
 	}
 	
 	private void createMonthlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
 		
 		SqlEncounterQuery patientVisitsToAsthmaClinic = new SqlEncounterQuery();
 		
 		patientVisitsToAsthmaClinic
 		        .setQuery("select encounter_id from encounter where encounter_id in(select encounter_id from encounter where (form_id="
 		                + rendevousForm.getFormId()
 		                + " or form_id="
 		                + DDBform.getFormId()
 		                + ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 group by encounter_datetime, patient_id)");
 		patientVisitsToAsthmaClinic.setName("patientVisitsToAsthmaClinic");
 		patientVisitsToAsthmaClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientVisitsToAsthmaClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		EncounterIndicator patientVisitsToAsthmaClinicMonthIndicator = new EncounterIndicator();
 		patientVisitsToAsthmaClinicMonthIndicator.setName("patientVisitsToAsthmaClinicMonthIndicator");
 		patientVisitsToAsthmaClinicMonthIndicator.setEncounterQuery(new Mapped<EncounterQuery>(patientVisitsToAsthmaClinic,
 		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		
 		dsd.addColumn(patientVisitsToAsthmaClinicMonthIndicator);
 		
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
 		//  A1: Total # of patient visits to DM clinic in the last quarter
 		//==================================================================
 		SqlEncounterQuery patientVisitsToAsthmaClinic = new SqlEncounterQuery();
 		
 		patientVisitsToAsthmaClinic
 		        .setQuery("select encounter_id from encounter where encounter_id in(select encounter_id from encounter where (form_id="
 		                + rendevousForm.getFormId()
 		                + " or form_id="
 		                + DDBform.getFormId()
 		                + ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 group by encounter_datetime, patient_id)");
 		patientVisitsToAsthmaClinic.setName("patientVisitsToAsthmaClinic");
 		patientVisitsToAsthmaClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientVisitsToAsthmaClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		EncounterIndicator patientVisitsToAsthmaClinicQuarterlyIndicator = new EncounterIndicator();
 		patientVisitsToAsthmaClinicQuarterlyIndicator.setName("patientVisitsToAsthmaClinicQuarterlyIndicator");
 		patientVisitsToAsthmaClinicQuarterlyIndicator.setEncounterQuery(new Mapped<EncounterQuery>(
 		        patientVisitsToAsthmaClinic, ParameterizableUtil
 		                .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		
 		dsd.addColumn(patientVisitsToAsthmaClinicQuarterlyIndicator);
 		
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
 		
 		EncounterCohortDefinition patientSeen = Cohorts.createEncounterParameterizedByDate("Patients seen",
 		    onOrAfterOnOrBefore, patientsSeenEncounterTypes);
 		
 		EncounterCohortDefinition patientWithDDB = Cohorts.createEncounterBasedOnForms("patientWithDDB",
 		    onOrAfterOnOrBefore, DDBforms);
 		
 		CompositionCohortDefinition patientsSeenComposition = new CompositionCohortDefinition();
 		patientsSeenComposition.setName("patientsSeenComposition");
 		patientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		patientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		patientsSeenComposition.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		patientsSeenComposition.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		patientsSeenComposition.setCompositionString("1 OR 2");
 		
 		CohortIndicator patientsSeenQuarterIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
 		    patientsSeenComposition,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 		CohortIndicator patientsSeenMonthOneIndicator = Indicators.newCountIndicator("patientsSeenMonthOneIndicator",
 		    patientsSeenComposition,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
 		CohortIndicator patientsSeenMonthTwoIndicator = Indicators.newCountIndicator("patientsSeenMonthTwoIndicator",
 		    patientsSeenComposition,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-2m+1d},onOrBefore=${endDate-1m+1d}"));
 		CohortIndicator patientsSeenMonthThreeIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
 		    patientsSeenComposition,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate-2m+1d}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn("A2Q", "Total # of patients seen in the last quarter", new Mapped(patientsSeenQuarterIndicator,
 		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		dsd.addColumn("A2QM1", "Total # of patients seen in the last month one", new Mapped(patientsSeenMonthOneIndicator,
 		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		dsd.addColumn("A2QM2", "Total # of patients seen in the last month two", new Mapped(patientsSeenMonthTwoIndicator,
 		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		dsd.addColumn("A2QM3", "Total # of patients seen in the last month three", new Mapped(
 		        patientsSeenMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================================
 		// A3: Total # of new patients enrolled in the last month/quarter
 		//==================================================================
 		
 		CompositionCohortDefinition patientEnrolledInCRDP = Cohorts.createEnrolledInProgramDuringPeriod("Enrolled In CRDP",
 		    asthmaProgram);
 		
 		CohortIndicator patientEnrolledInCRDPQuarterIndicator = Indicators.newCountIndicator(
 		    "patientEnrolledInCRDPQuarterIndicator", patientEnrolledInCRDP,
 		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));
 		CohortIndicator patientEnrolledInCRDPMonthOneIndicator = Indicators.newCountIndicator(
 		    "patientEnrolledInCRDPQuarterIndicator", patientEnrolledInCRDP,
 		    ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
 		CohortIndicator patientEnrolledInCRDPMonthTwooIndicator = Indicators.newCountIndicator(
 		    "patientEnrolledInCRDPQuarterIndicator", patientEnrolledInCRDP,
 		    ParameterizableUtil.createParameterMappings("startDate=${endDate-2m+1d},endDate=${endDate-1m+1d}"));
 		CohortIndicator patientEnrolledInCRDPMonthThreeIndicator = Indicators.newCountIndicator(
 		    "patientEnrolledInCRDPQuarterIndicator", patientEnrolledInCRDP,
 		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate-2m+1d}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn("A3Q", "Total # of new patients enrolled in the last quarter", new Mapped(
 		        patientEnrolledInCRDPQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
 		    "");
 		dsd.addColumn("A3QM1", "Total # of new patients enrolled in the month one", new Mapped(
 		        patientEnrolledInCRDPMonthOneIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
 		    "");
 		dsd.addColumn("A3QM2", "Total # of new patients enrolled in the month two", new Mapped(
 		        patientEnrolledInCRDPMonthTwooIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
 		    "");
 		dsd.addColumn(
 		    "A3QM3",
 		    "Total # of new patients enrolled in the month three",
 		    new Mapped(patientEnrolledInCRDPMonthThreeIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================================
 		// A4: Total # of new patients with RDV in the last month/quarter
 		//==================================================================
 		
 		CohortIndicator patientRDVQuarterIndicator = Indicators.newCountIndicator("patientRDVQuarterIndicator", patientSeen,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
 		CohortIndicator patientRDVMonthOneIndicator = Indicators.newCountIndicator("patientRDVMonthOneIndicator",
 		    patientSeen, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
 		CohortIndicator patientRDVMonthTwooIndicator = Indicators.newCountIndicator("patientRDVMonthTwooIndicator",
 		    patientSeen,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-2m+1d},onOrBefore=${endDate-1m+1d}"));
 		CohortIndicator patientRDVMonthThreeIndicator = Indicators.newCountIndicator("patientRDVMonthThreeIndicator",
 		    patientSeen,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate-2m+1d}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn("A4Q", "Total # of new patients with RDV in the last quarter", new Mapped(patientRDVQuarterIndicator,
 		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		dsd.addColumn("A4QM1", "Total # of new patients with RDV in the month one", new Mapped(patientRDVMonthOneIndicator,
 		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		dsd.addColumn("A4QM2", "Total # of new patients with RDV in the month two", new Mapped(patientRDVMonthTwooIndicator,
 		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		dsd.addColumn("A4QM3", "Total # of new patients with RDV in the month three", new Mapped(
 		        patientRDVMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================================
 		//B1: Pediatric:  Of the new patients enrolled in the last quarter, % ≤15 years old at intake
 		//==================================================================
 		
 		SqlCohortDefinition patientsUnderFifteenAtEnrollementDate = Cohorts.createUnder15AtEnrollmentCohort(
 		    "patientsUnder15AtEnrollment", asthmaProgram);
 		
 		CompositionCohortDefinition patientsUnderFifteenComposition = new CompositionCohortDefinition();
 		patientsUnderFifteenComposition.setName("patientsUnderFifteenComposition");
 		patientsUnderFifteenComposition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		patientsUnderFifteenComposition.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		patientsUnderFifteenComposition.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInCRDP, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		patientsUnderFifteenComposition.getSearches().put("2",
 		    new Mapped<CohortDefinition>(patientsUnderFifteenAtEnrollementDate, null));
 		patientsUnderFifteenComposition.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsUnderFifteenCountIndicator = Indicators.newCountIndicator(
 		    "patientsUnderFifteenCountIndicator", patientsUnderFifteenComposition,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "B1N",
 		    "Pediatric: Of the new patients enrolled in the last quarter, number ≤15 years old at intake",
 		    new Mapped(patientsUnderFifteenCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
 		    "");
 		
 		//====================================================================
 		//B2: Gender: Of the new patients enrolled in the last quarter, % male
 		//====================================================================
 		
 		GenderCohortDefinition malePatients = Cohorts.createMaleCohortDefinition("Male patients");
 		
 		CompositionCohortDefinition malePatientsEnrolledInCRDP = new CompositionCohortDefinition();
 		malePatientsEnrolledInCRDP.setName("malePatientsEnrolledIn");
 		malePatientsEnrolledInCRDP.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
 		malePatientsEnrolledInCRDP.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
 		malePatientsEnrolledInCRDP.addParameter(new Parameter("startDate", "startDate", Date.class));
 		malePatientsEnrolledInCRDP.addParameter(new Parameter("endDate", "endDate", Date.class));
 		malePatientsEnrolledInCRDP.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientEnrolledInCRDP, ParameterizableUtil
 		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
 		malePatientsEnrolledInCRDP.getSearches().put("2", new Mapped<CohortDefinition>(malePatients, null));
 		malePatientsEnrolledInCRDP.setCompositionString("1 AND 2");
 		
 		CohortIndicator malePatientsEnrolledInCRDPCountIndicator = Indicators.newCountIndicator(
 		    "malePatientsEnrolledInDMCountIndicator", malePatientsEnrolledInCRDP,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "B2N",
 		    "Gender: Of the new patients enrolled in the last quarter, number male",
 		    new Mapped(malePatientsEnrolledInCRDPCountIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================
 		// B3: Of the new patients enrolled in the last month, % with documented peak flow taken both before and after salbutamol at intake
 		//=======================================================
 		
 		ProgramEnrollmentCohortDefinition enrolledInAsthmaProgram = Cohorts
 		        .createProgramEnrollmentParameterizedByStartEndDate("enrolledInAthmaProgram", asthmaProgram);
 		
 		SqlCohortDefinition patientsWithBothPeakFlowInSameDDBForm = Cohorts
 		        .getPatientsWithTwoObservationsBothInFormBetweenStartAndEndDate("patientsWithBothPeakFlowInSameDDBForm",
 		            DDBform, peakFlowAfterSalbutamol, peakFlowBeforeSalbutamol);
 		
 		CompositionCohortDefinition patientsWithBothPeakFlowInSameDDBFormComposition = new CompositionCohortDefinition();
 		patientsWithBothPeakFlowInSameDDBFormComposition.setName("patientsWithBothPeakFlowInSameDDBFormComposition");
 		patientsWithBothPeakFlowInSameDDBFormComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithBothPeakFlowInSameDDBFormComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsWithBothPeakFlowInSameDDBFormComposition.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(enrolledInAsthmaProgram, 
 		    		ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
 		patientsWithBothPeakFlowInSameDDBFormComposition.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithBothPeakFlowInSameDDBForm, 
 				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
 		patientsWithBothPeakFlowInSameDDBFormComposition.setCompositionString("1 AND 2");
 		
 		CohortIndicator enrolledInAsthmaProgramIndicator = Indicators.newCountIndicator("enrolledInAthmaProgramIndicator",
 		    enrolledInAsthmaProgram,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
 		
 		CohortIndicator patientsWithBothPeakFlowInSameDDBFormIndicator = Indicators.newCountIndicator(
 		    "patientsWithBothPeakFlowInSameDDBFormIndicator", patientsWithBothPeakFlowInSameDDBFormComposition,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "B3N",
 		    "patients with documented peak flow taken both before and after salbutamol at intake",
 		    new Mapped(patientsWithBothPeakFlowInSameDDBFormIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		dsd.addColumn("B3D", "new patients enrolled in report period", new Mapped(enrolledInAsthmaProgramIndicator,
 		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//===============================================================================================
 		// B4: Of the new patients enrolled in the last quarter, % with smoking status documented at intake
 		//===============================================================================================
 		
 		SqlCohortDefinition patientsWithSmokingHistory = Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate(
 		    "patientsWithSmokingHistory", DDBform, smokingHistory);
 		
 		CompositionCohortDefinition patientsWithSmokingHistoryComposition = new CompositionCohortDefinition();
 		patientsWithSmokingHistoryComposition.setName("patientsWithBothPeakFlowInSameDDBFormComposition");
 		patientsWithSmokingHistoryComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithSmokingHistoryComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsWithSmokingHistoryComposition.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(enrolledInAsthmaProgram, 
 		    		ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
 		patientsWithSmokingHistoryComposition.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithSmokingHistory, 
 				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
 		patientsWithSmokingHistoryComposition.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsWithSmokingHistoryIndicator = Indicators.newCountIndicator(
 		    "patientsWithSmokingHistoryIndicator", patientsWithSmokingHistoryComposition,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "B4N",
 		    "patients with smoking status documented at intake",
 		    new Mapped(patientsWithSmokingHistoryIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//==============================================================
 		// C1: Of total patients with a visit in the last quarter, % who had inhaler teaching in the last 6 months
 		//==============================================================
 		
 		SqlCohortDefinition patientsWithBasicInhalerTrainingProvidedObsAnswer = Cohorts
 		        .getPatientsWithObservationInFormBetweenStartAndEndDate("patientsWithBasicInhalerTrainingProvidedObsAnswer",
 		            DDBforms, properInhalerTechnique, basicInhalerTrainingProvided);
 		
 		CompositionCohortDefinition patientsSeenWithBasicInhalerTrainingProvidedObsAnswer = new CompositionCohortDefinition();
 		patientsSeenWithBasicInhalerTrainingProvidedObsAnswer
 		        .setName("patientsSeenAndWithBasicInhalerTrainingProvidedObsAnswer");
 		patientsSeenWithBasicInhalerTrainingProvidedObsAnswer.addParameter(new Parameter("onOrAfter", "onOrAfter",
 		        Date.class));
 		patientsSeenWithBasicInhalerTrainingProvidedObsAnswer.addParameter(new Parameter("onOrBefore", "onOrBefore",
 		        Date.class));
 		patientsSeenWithBasicInhalerTrainingProvidedObsAnswer.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsSeenWithBasicInhalerTrainingProvidedObsAnswer.addParameter(new Parameter("startDate", "startDate",
 		        Date.class));
 		patientsSeenWithBasicInhalerTrainingProvidedObsAnswer.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsWithBasicInhalerTrainingProvidedObsAnswer, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate},startDate=${startDate-3m}")));
 		patientsSeenWithBasicInhalerTrainingProvidedObsAnswer.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		patientsSeenWithBasicInhalerTrainingProvidedObsAnswer.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsWithBasicInhalerTrainingProvidedObsAnswerIndicator = Indicators
 		        .newCountIndicator(
 		            "patientsWithBasicInhalerTrainingProvidedObsAnswerIndicator",
 		            patientsSeenWithBasicInhalerTrainingProvidedObsAnswer,
 		            ParameterizableUtil
 		                    .createParameterMappings("endDate=${endDate},startDate=${startDate-3m+1d},onOrBefore=${endDate},onOrAfter=${startDate}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "C1",
 		    "Patients With Basic Inhaler Training Provided",
 		    new Mapped(patientsWithBasicInhalerTrainingProvidedObsAnswerIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//==============================================================
 		// C2: Of total patients with a visit in the last quarter, % who had peak flow checked in the last 6 months
 		//==============================================================
 		
 		NumericObsCohortDefinition patientsWithPeakFlowAfterSalbutamol = Cohorts.createNumericObsCohortDefinition(
 		    "patientsWithPeakFlowAfterSalbutamol", onOrAfterOnOrBefore, peakFlowAfterSalbutamol, 0, null, TimeModifier.ANY);
 		
 		CompositionCohortDefinition patientsSeenWithPeakFlowAfterSalbutamol = new CompositionCohortDefinition();
 		patientsSeenWithPeakFlowAfterSalbutamol.setName("patientsSeenWithPeakFlowAfterSalbutamol");
 		patientsSeenWithPeakFlowAfterSalbutamol.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		patientsSeenWithPeakFlowAfterSalbutamol.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		patientsSeenWithPeakFlowAfterSalbutamol.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsWithPeakFlowAfterSalbutamol, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-3m}")));
 		patientsSeenWithPeakFlowAfterSalbutamol.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		patientsSeenWithPeakFlowAfterSalbutamol.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsSeenWithPeakFlowAfterSalbutamolIndicator = Indicators.newCountIndicator(
 		    "patientsSeenWithPeakFlowAfterSalbutamolIndicator", patientsSeenWithPeakFlowAfterSalbutamol,
 		    ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d}"));
 		
 		//=================================================
 		//     Adding columns to data set definition     //
 		//=================================================
 		
 		dsd.addColumn(
 		    "C2",
 		    "Patients With peak flow checked in the last 6 months",
 		    new Mapped(patientsSeenWithPeakFlowAfterSalbutamolIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================
 		// D1: Of total patients seen in the last month, % with no asthma/COPD-related regimen documented ever (asthma meds: salbutamol, beclomethasone, prednisolone, aminophyilline)
 		//=======================================================
 		
 		SqlCohortDefinition patientsWithAsthmaVisit = new SqlCohortDefinition();
 		patientsWithAsthmaVisit.setQuery("select distinct patient_id from encounter where encounter_type="
 		        + asthmaEncounterType.getId()
 		        + " and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0");
 		patientsWithAsthmaVisit.setName("patientsWithAsthmaVisit");
 		patientsWithAsthmaVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithAsthmaVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		SqlCohortDefinition patientsHaveDrugOrdersInAsthmaMedication = Cohorts.getPatientsEverNotOnRegimen(
 		    "patientsHaveDrugOrdersInAsthmaMedication", asthmasMedications);
 		
 		CompositionCohortDefinition patientsWithAsthmaVisitAndEverNotOnRegimen = new CompositionCohortDefinition();
 		patientsWithAsthmaVisitAndEverNotOnRegimen.setName("patientsWithAsthmaVisitAndEverNotOnRegimen");
 		patientsWithAsthmaVisitAndEverNotOnRegimen.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithAsthmaVisitAndEverNotOnRegimen.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsWithAsthmaVisitAndEverNotOnRegimen.addSearch("1", patientsWithAsthmaVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		patientsWithAsthmaVisitAndEverNotOnRegimen.addSearch("2", patientsHaveDrugOrdersInAsthmaMedication, "");
 		patientsWithAsthmaVisitAndEverNotOnRegimen.setCompositionString("1 AND (NOT 2)");
 		
 		CohortIndicator patientsWithAsthmaVisitIndicator = Indicators.newCountIndicator("patientsWithAsthmaVisitIndicator",
 		    patientsWithAsthmaVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		CohortIndicator patientsWithAsthmaVisitAndEverNotOnRegimenIndicator = Indicators.newCountIndicator(
 		    "patientsEverNotOnRegimenIndicator", patientsWithAsthmaVisitAndEverNotOnRegimen,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		dsd.addColumn(
 		    "D1N",
 		    "patients with no asthma/COPD-related regimen documented ever (asthma meds: salbutamol, beclomethasone, prednisolone, aminophyilline)",
 		    new Mapped(patientsWithAsthmaVisitAndEverNotOnRegimenIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		dsd.addColumn("D1D", "Of total patients seen in report period", new Mapped(patientsWithAsthmaVisitIndicator,
 		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// D2: Of total patients with a visit in the last quarter, % on Salbutamol alone at last visit
 		//=======================================================
 		SqlCohortDefinition patientsWithCurrentSalbutamolDrugOrder = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
 		    "patientsWithCurrentSalbutamolDrugOrder", salbutamol);
 		
 		SqlCohortDefinition patientsWithAnyOtherCurrentAsthmaDrugOrder = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
 		    "patientsWithAnyOtherCurrentAsthmaDrugOrder", asthmasMedicationsWithoutSalbutamol);
 		
 		CompositionCohortDefinition patientsOnSalbutamolAlone = new CompositionCohortDefinition();
 		patientsOnSalbutamolAlone.setName("patientsOnSalbutamolAlone");
 		patientsOnSalbutamolAlone.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsOnSalbutamolAlone.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsOnSalbutamolAlone.addSearch("1", patientsWithAsthmaVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsOnSalbutamolAlone.addSearch("2", patientsWithCurrentSalbutamolDrugOrder, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
 		patientsOnSalbutamolAlone.addSearch("3", patientsWithAnyOtherCurrentAsthmaDrugOrder, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
 		patientsOnSalbutamolAlone.setCompositionString("1 AND 2 AND (NOT 3)");
 		
 		CohortIndicator patientsOnSalbutamolAloneIndicator = Indicators.newCountIndicator(
 		    "patientsOnSalbutamolAloneIndicator", patientsOnSalbutamolAlone,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		dsd.addColumn(
 		    "D2N",
 		    "patients with a visit in the last quarter, % on Salbutamol alone at last visit",
 		    new Mapped(patientsOnSalbutamolAloneIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================
 		// D3: Of total patients with a visit in the last quarter on Salbutamol, % also on Beclomethasone
 		//=======================================================
 		SqlCohortDefinition patientsWithCurrentBeclomethasoneDrugOrder = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
 		    "patientsWithCurrentBeclomethasoneDrugOrder", beclomethasone);
 		
 		CompositionCohortDefinition patientsOnSalbutamolAndBeclomethasone = new CompositionCohortDefinition();
 		patientsOnSalbutamolAndBeclomethasone.setName("patientsOnSalbutamolAndBeclomethasone");
 		patientsOnSalbutamolAndBeclomethasone.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsOnSalbutamolAndBeclomethasone.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsOnSalbutamolAndBeclomethasone.addSearch("1", patientsWithAsthmaVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsOnSalbutamolAndBeclomethasone.addSearch("2", patientsWithCurrentSalbutamolDrugOrder,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
 		patientsOnSalbutamolAndBeclomethasone.addSearch("3", patientsWithCurrentBeclomethasoneDrugOrder, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
 		patientsOnSalbutamolAndBeclomethasone.setCompositionString("1 AND 2 AND 3");
 		
 		CohortIndicator patientsOnSalbutamolAndBeclomethasoneIndicator = Indicators.newCountIndicator(
 		    "patientsOnSalbutamolAndBeclomethasoneIndicator", patientsOnSalbutamolAndBeclomethasone,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		dsd.addColumn(
 		    "D3N",
 		    "patients with a visit in the last quarter on Salbutamol, % also on Beclomethasone",
 		    new Mapped(patientsOnSalbutamolAndBeclomethasoneIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		//=======================================================
 		// D4: Of total patients with a visit in the last quarter, % prescribed oral Prednisolone in the last quarter
 		//=======================================================
 		SqlCohortDefinition patientsPrescribedOralPrednisoloneInTheLastQuarter = Cohorts
 		        .getPatientsOnCurrentRegimenBasedOnEndDate("patientsPrescribedOralPrednisoloner", prednisolone);
 		
 		CompositionCohortDefinition patientsPrescribedOralPrednisolone = new CompositionCohortDefinition();
 		patientsPrescribedOralPrednisolone.setName("patientsOnSalbutamolAndBeclomethasone");
 		patientsPrescribedOralPrednisolone.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsPrescribedOralPrednisolone.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsPrescribedOralPrednisolone.addSearch("1", patientsWithAsthmaVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		patientsPrescribedOralPrednisolone.addSearch("2", patientsPrescribedOralPrednisoloneInTheLastQuarter, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
 		patientsPrescribedOralPrednisolone.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsPrescribedOralPrednisoloneIndicator = Indicators.newCountIndicator(
 		    "patientsPrescribedOralPrednisoloneIndicator", patientsPrescribedOralPrednisolone,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		dsd.addColumn(
 		    "D4N",
 		    "patients with a visit in the last quarter, % prescribed oral Prednisolone in the last quarter",
 		    new Mapped(patientsPrescribedOralPrednisoloneIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		//=======================================================================
 		//E1Died: Of total active patients, % of patients died.
 		//==================================================================
 		
 		SqlCohortDefinition patientsInPatientDiedState = Cohorts.createPatientsInStateNotPredatingProgramEnrolment(patientDied);
 
 		CompositionCohortDefinition activePatientsInPatientDiedState = new CompositionCohortDefinition();
 		activePatientsInPatientDiedState.setName("activePatientsInPatientDiedState");
 		activePatientsInPatientDiedState.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		activePatientsInPatientDiedState.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		activePatientsInPatientDiedState.addParameter(new Parameter("endDate", "endDate", Date.class));
 		activePatientsInPatientDiedState.addParameter(new Parameter("startDate", "startDate", Date.class));
 		activePatientsInPatientDiedState.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		activePatientsInPatientDiedState.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientsInPatientDiedState, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
 		activePatientsInPatientDiedState.setCompositionString("1 AND 2");
 		
 		CohortIndicator activePatientsInPatientDiedStateInQuarterIndicator = Indicators
         .newCountIndicator(
             "activePatientsInPatientDiedStateInQuarterIndicator",
             activePatientsInPatientDiedState,
             ParameterizableUtil
                     .createParameterMappings("endDate=${endDate},startDate=${endDate-3m+1d},onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		
 		
 		dsd.addColumn(
 			    "E1DiedN",
 			    "Total active patients, number who Died in quarter",
 			    new Mapped(activePatientsInPatientDiedStateInQuarterIndicator, ParameterizableUtil
 			            .createParameterMappings("endDate=${endDate}")), "");
 			
 		
 		//=======================================================================
 		//E1: Of total active patients, % with documented hospitalization (in flowsheet) in the last quarter (exclude hospitalization on DDB)
 		//==================================================================
 		
 		SqlCohortDefinition patientHospitalized = Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate(
 		    "patientHospitalized", asthmaEncounterType, locOfHosp);
 		
 		CompositionCohortDefinition activeAndHospitalizedPatients = new CompositionCohortDefinition();
 		activeAndHospitalizedPatients.setName("activeAndHospitalizedPatients");
 		activeAndHospitalizedPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		activeAndHospitalizedPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		activeAndHospitalizedPatients.addParameter(new Parameter("endDate", "endDate", Date.class));
 		activeAndHospitalizedPatients.addParameter(new Parameter("startDate", "startDate", Date.class));
 		activeAndHospitalizedPatients.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		activeAndHospitalizedPatients.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientHospitalized, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		activeAndHospitalizedPatients.setCompositionString("1 AND 2");
 		
 		CohortIndicator activeAndHospitalizedPatientsCountQuarterIndicator = Indicators
 		        .newCountIndicator(
 		            "activeAndHospitalizedPatientsCountQuarterIndicator",
 		            activeAndHospitalizedPatients,
 		            ParameterizableUtil
 		                    .createParameterMappings("endDate=${endDate},startDate=${endDate-3m+1d},onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 		
 		CohortIndicator patientsSeenInOneYearCountIndicator = Indicators.newCountIndicator(
 		    "patientsSeenInOneYearCountIndicator", patientsSeenComposition,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		
 		dsd.addColumn(
 		    "E1N",
 		    "Total active patients, number with documented hospitalization (in flowsheet) in the last quarter (exclude hospitalization on DDB)",
 		    new Mapped(activeAndHospitalizedPatientsCountQuarterIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		dsd.addColumn("E1D", "total patients seen in the last year", new Mapped(patientsSeenInOneYearCountIndicator,
 		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================================
 		//E2: Of total patients with a visit in the last 12 months, % with no visit  in 28  or more weeks
 		//==================================================================		
 		
 		EncounterCohortDefinition withAsthmaVisit = Cohorts.createEncounterParameterizedByDate("withAsthmaVisit",
 		    onOrAfterOnOrBefore, asthmaEncounterType);
 		
 		CompositionCohortDefinition activeAndNotwithAsthmaVisitIn28WeeksPatients = new CompositionCohortDefinition();
 		activeAndNotwithAsthmaVisitIn28WeeksPatients.setName("activeAndNotwithAsthmaVisitIn28WeeksPatients");
 		activeAndNotwithAsthmaVisitIn28WeeksPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		activeAndNotwithAsthmaVisitIn28WeeksPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		activeAndNotwithAsthmaVisitIn28WeeksPatients.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		activeAndNotwithAsthmaVisitIn28WeeksPatients.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(withAsthmaVisit, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter+12m-28w}")));
 		activeAndNotwithAsthmaVisitIn28WeeksPatients.setCompositionString("1 AND (NOT 2)");
 		
 		CohortIndicator activeAndNotwithAsthmaVisitIn28WeeksPatientsCountQuarterIndicator = Indicators.newCountIndicator(
 		    "activeAndNotwithAsthmaVisitIn28WeeksPatientsNumeratorCountQuarterIndicator",
 		    activeAndNotwithAsthmaVisitIn28WeeksPatients,
 		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 		
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		dsd.addColumn(
 		    "E2N",
 		    "Total active patients, number with no visit in 28 weeks or more past last visit date",
 		    new Mapped(activeAndNotwithAsthmaVisitIn28WeeksPatientsCountQuarterIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================================
 		//E3: Of total active patients with ‘severe persistent’ or ‘severe uncontrolled’ asthma classification at last visit, % with next scheduled RDV visit 14 weeks or more past last visit date 
 		//=======================================================================		
 		SqlCohortDefinition patientsWithAsthmaClassificationObsAnswer = Cohorts
         .getPatientsWithObservationInFormBetweenStartAndEndDate("patientsWithAsthmaClassificationObsAnswer",
             DDBforms, asthmaclassification, asthmasClassificationAnswers);
 		
 		//=============
 		CompositionCohortDefinition activeAndWithAsthmaClassificationObsAnswer = new CompositionCohortDefinition();
 		activeAndWithAsthmaClassificationObsAnswer.setName("activeAndWithAsthmaClassificationObsAnswer");
 		activeAndWithAsthmaClassificationObsAnswer.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		activeAndWithAsthmaClassificationObsAnswer.addParameter(new Parameter("onOrBefore", "onOrBefore",Date.class));
 		activeAndWithAsthmaClassificationObsAnswer.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsWithAsthmaClassificationObsAnswer, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		
 		activeAndWithAsthmaClassificationObsAnswer.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		activeAndWithAsthmaClassificationObsAnswer.setCompositionString("1 AND 2");
 		
 		CohortIndicator activeAndactiveAndWithAsthmaClassificationObsAnswerIndicator = Indicators
         .newCountIndicator("activeAndactiveAndWithAsthmaClassificationObsAnswerIndicator",
         	activeAndWithAsthmaClassificationObsAnswer,
             ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 		
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================	
 		dsd.addColumn(
 		    "E3D",
 		    "Total active patients, number with ‘severe persistent’ or ‘severe uncontrolled’ asthma classification at last visit",
 		    new Mapped(activeAndactiveAndWithAsthmaClassificationObsAnswerIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//===============
 		
 		SqlCohortDefinition patientswithRDV14WeeksOrMorePastLastVisitDate  = new SqlCohortDefinition();
 		patientswithRDV14WeeksOrMorePastLastVisitDate.setQuery("select last_rdv.person_id from (select person_id, value_datetime, obs_datetime, datediff(value_datetime,obs_datetime) from obs o, encounter e where o.encounter_id=e.encounter_id and e.encounter_type="
 				+ asthmaEncounterType.getId()
 				+ " and o.concept_id="
 				+ returnVisitDate.getConceptId()
 				+ " and datediff(o.value_datetime, o.obs_datetime) >= 98 order by o.value_datetime desc) as last_rdv group by last_rdv.person_id");
 		patientswithRDV14WeeksOrMorePastLastVisitDate.setName("patientswithRDV14WeeksOrMorePastLastVisitDate");
 		
 		CompositionCohortDefinition activeAndNotwithAsthmaVisit14WeeksPatients = new CompositionCohortDefinition();
 		activeAndNotwithAsthmaVisit14WeeksPatients.setName("activeAndNotwithAsthmaVisit14WeeksPatients");
 		activeAndNotwithAsthmaVisit14WeeksPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		activeAndNotwithAsthmaVisit14WeeksPatients.addParameter(new Parameter("onOrBefore", "onOrBefore",Date.class));
 		activeAndNotwithAsthmaVisit14WeeksPatients.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(patientsWithAsthmaClassificationObsAnswer, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
 		
 		activeAndNotwithAsthmaVisit14WeeksPatients.getSearches().put(
 		    "2",
 		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		
 		
 		activeAndNotwithAsthmaVisit14WeeksPatients.getSearches().put(
 		    "3",
 		    new Mapped<CohortDefinition>(patientswithRDV14WeeksOrMorePastLastVisitDate, null));
 		activeAndNotwithAsthmaVisit14WeeksPatients.setCompositionString("1 AND 2 AND 3");
 		
 		CohortIndicator activeAndNotwithAsthmaVisit14WeeksPatientsIndicator = Indicators
 		        .newCountIndicator("activeAndNotwithAsthmaVisit14WeeksPatients",
 		        	activeAndNotwithAsthmaVisit14WeeksPatients,
 		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
 		
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================	
 		dsd.addColumn(
 		    "E3N",
 		    "Total active patients, number with no visit 14 weeks or more past last visit date",
 		    new Mapped(activeAndNotwithAsthmaVisit14WeeksPatientsIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================
 		// E4: Of adult male patients (age ≥15 years old) who had peak flow tested in the last quarter, % with last peak flow >580
 		//=======================================================
 		
 		AgeCohortDefinition over15Cohort = Cohorts.createOver15AgeCohort("ageQD: Over 15");
 		
 		GenderCohortDefinition malesDefinition = Cohorts.createMaleCohortDefinition("malesDefinition");
 		
 		NumericObsCohortDefinition patientsTestedForpeakFlow = Cohorts.createNumericObsCohortDefinition(
 		    "patientsTestedForpeakFlow", onOrAfterOnOrBefore, peakFlowAfterSalbutamol, 0, null, TimeModifier.LAST);
 		
 		CompositionCohortDefinition adultMalePatientsTestedForpeakFlow = new CompositionCohortDefinition();
 		adultMalePatientsTestedForpeakFlow.setName("adultMalePatientsTestedForForpeakFlow");
 		adultMalePatientsTestedForpeakFlow.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		adultMalePatientsTestedForpeakFlow.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		adultMalePatientsTestedForpeakFlow.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		
 		adultMalePatientsTestedForpeakFlow.getSearches().put("1",new Mapped<CohortDefinition>(patientsTestedForpeakFlow, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		
 		adultMalePatientsTestedForpeakFlow.getSearches().put("2", new Mapped<CohortDefinition>(malesDefinition, null));
 		
 		adultMalePatientsTestedForpeakFlow.getSearches().put("3",new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
 		
 		adultMalePatientsTestedForpeakFlow.setCompositionString("1 AND 2 AND 3");
 		
 		CohortIndicator adultMalePatientsTestedForpeakFlowIndicator = Indicators
 		        .newCountIndicator("adultMalePatientsTestedForpeakFlowIndicator",
 		        	adultMalePatientsTestedForpeakFlow,
 		            ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d},effectiveDate=${endDate}"));
 		
 		
 		NumericObsCohortDefinition patientsWithLastPeakflowGreaterThan580 = Cohorts.createNumericObsCohortDefinition(
 		    "patientsWithLastPeakflowGreaterThan580", peakFlowAfterSalbutamol, 580, RangeComparator.GREATER_THAN,
 		    TimeModifier.LAST);
 		
 		CompositionCohortDefinition adultMalePatientsTestedForpeakFlowGreaterThan580 = new CompositionCohortDefinition();
 		adultMalePatientsTestedForpeakFlowGreaterThan580.setName("adultMalePatientsTestedForpeakFlowGreaterThan580");
 		adultMalePatientsTestedForpeakFlowGreaterThan580.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		adultMalePatientsTestedForpeakFlowGreaterThan580.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		adultMalePatientsTestedForpeakFlowGreaterThan580.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		adultMalePatientsTestedForpeakFlowGreaterThan580.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(adultMalePatientsTestedForpeakFlow, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},effectiveDate=${effectiveDate}")));
 		adultMalePatientsTestedForpeakFlowGreaterThan580.getSearches().put("2",
 		    new Mapped<CohortDefinition>(patientsWithLastPeakflowGreaterThan580, null));
 		adultMalePatientsTestedForpeakFlowGreaterThan580.setCompositionString("1 AND 2");
 		
 		CohortIndicator adultMalePatientsTestedForpeakFlowGreaterThan580Indicator = Indicators
         .newCountIndicator("adultMalePatientsTestedForpeakFlowGreaterThan580Indicator",
         	adultMalePatientsTestedForpeakFlowGreaterThan580,
             ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d},effectiveDate=${endDate}"));
 		
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		dsd.addColumn(
 		    "E4D",
 		    "Of adult male patients (age ≥15 years old) who had peak flow tested in the last quarter",
 		    new Mapped(adultMalePatientsTestedForpeakFlowIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		dsd.addColumn(
 		    "E4N",
 		    "Of adult male patients (age ≥15 years old) who had peak flow Greater Than 580 tested in the last quarter",
 		    new Mapped(adultMalePatientsTestedForpeakFlowGreaterThan580Indicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		//=======================================================
 		// E5: Of adult female patients (age ≥15 years old) who had peak flow tested in the last quarter, % with last peak flow >400
 		//=======================================================
 		
 		GenderCohortDefinition femalesDefinition = Cohorts.createFemaleCohortDefinition("femalesDefinition");
 		
 		CompositionCohortDefinition adultFemalePatientsTestedForpeakFlow = new CompositionCohortDefinition();
 		adultFemalePatientsTestedForpeakFlow.setName("adultFemalePatientsTestedForpeakFlow");
 		adultFemalePatientsTestedForpeakFlow.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		adultFemalePatientsTestedForpeakFlow.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		adultFemalePatientsTestedForpeakFlow.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		
 		adultFemalePatientsTestedForpeakFlow.getSearches().put("1",new Mapped<CohortDefinition>(patientsTestedForpeakFlow, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
 		
 		adultFemalePatientsTestedForpeakFlow.getSearches().put("2", new Mapped<CohortDefinition>(femalesDefinition, null));
 		
 		adultFemalePatientsTestedForpeakFlow.getSearches().put("3",new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
 		
 		adultFemalePatientsTestedForpeakFlow.setCompositionString("1 AND 2 AND 3");
 		
 		CohortIndicator adultFemalePatientsTestedForpeakFlowIndicator = Indicators
 		        .newCountIndicator("adultFemalePatientsTestedForpeakFlowIndicator",
 		        	adultFemalePatientsTestedForpeakFlow,
 		            ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d},effectiveDate=${endDate}"));
 		
 		
 		NumericObsCohortDefinition patientsWithLastPeakflowGreaterThan400 = Cohorts.createNumericObsCohortDefinition(
 		    "patientsWithLastPeakflowGreaterThan400", peakFlowAfterSalbutamol, 400, RangeComparator.GREATER_THAN,
 		    TimeModifier.LAST);
 		
 		CompositionCohortDefinition adultFemalePatientsTestedForpeakFlowGreaterThan400 = new CompositionCohortDefinition();
 		adultFemalePatientsTestedForpeakFlowGreaterThan400.setName("adultFemalePatientsTestedForpeakFlowGreaterThan400");
 		adultFemalePatientsTestedForpeakFlowGreaterThan400.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 		adultFemalePatientsTestedForpeakFlowGreaterThan400.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 		adultFemalePatientsTestedForpeakFlowGreaterThan400.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		adultFemalePatientsTestedForpeakFlowGreaterThan400.getSearches().put("1",new Mapped<CohortDefinition>(adultFemalePatientsTestedForpeakFlow, ParameterizableUtil
 		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},effectiveDate=${effectiveDate}")));
 		
 		adultFemalePatientsTestedForpeakFlowGreaterThan400.getSearches().put("2",
 		    new Mapped<CohortDefinition>(patientsWithLastPeakflowGreaterThan400, null));
 		adultFemalePatientsTestedForpeakFlowGreaterThan400.setCompositionString("1 AND 2");
 		
 		CohortIndicator adultFemalePatientsTestedForpeakFlowGreaterThan400Indicator = Indicators
         .newCountIndicator("adultFemalePatientsTestedForpeakFlowGreaterThan400Indicator",
         	adultFemalePatientsTestedForpeakFlowGreaterThan400,
             ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d},effectiveDate=${endDate}"));
 		
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		dsd.addColumn(
 		    "E5D",
 		    "Of adult female patients (age ≥15 years old) who had peak flow tested in the last quarter",
 		    new Mapped(adultFemalePatientsTestedForpeakFlowIndicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 		dsd.addColumn(
 		    "E5N",
 		    "Of adult female patients (age ≥15 years old) who had peak flow Greater Than 400 tested in the last quarter",
 		    new Mapped(adultFemalePatientsTestedForpeakFlowGreaterThan400Indicator, ParameterizableUtil
 		            .createParameterMappings("endDate=${endDate}")), "");
 		
 	}
 	
 	private void createMonthlyIndicators(CohortIndicatorDataSetDefinition dsd) {
 		//=======================================================
 		// B3: Of the new patients enrolled in the last month, % with documented peak flow taken both before and after salbutamol at intake
 		//=======================================================
 		
 		ProgramEnrollmentCohortDefinition enrolledInAthmaProgram = Cohorts
 		        .createProgramEnrollmentParameterizedByStartEndDate("enrolledInAthmaProgram", asthmaProgram);
 		
 		//SqlCohortDefinition patientsWithBothPeakFlowInSameDDBForm=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("patientsWithBothPeakFlowInSameDDBForm", DDBforms, peacFlowConcepts);
 		
 		SqlCohortDefinition patientsWithBothPeakFlowInSameDDBForm = Cohorts
 		        .getPatientsWithTwoObservationsBothInFormBetweenStartAndEndDate("patientsWithBothPeakFlowInSameDDBForm",
 		            DDBform, peakFlowAfterSalbutamol, peakFlowBeforeSalbutamol);
 		
 		CompositionCohortDefinition patientsWithBothPeakFlowInSameDDBFormComposition = new CompositionCohortDefinition();
 		patientsWithBothPeakFlowInSameDDBFormComposition.setName("patientsWithBothPeakFlowInSameDDBFormComposition");
 		patientsWithBothPeakFlowInSameDDBFormComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithBothPeakFlowInSameDDBFormComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsWithBothPeakFlowInSameDDBFormComposition.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(enrolledInAthmaProgram, ParameterizableUtil
 		            .createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
 		patientsWithBothPeakFlowInSameDDBFormComposition.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithBothPeakFlowInSameDDBForm, 
 				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
 		patientsWithBothPeakFlowInSameDDBFormComposition.setCompositionString("1 AND 2");
 		
 		CohortIndicator enrolledInAthmaProgramIndicator = Indicators.newCountIndicator("enrolledInAthmaProgramIndicator",
 		    enrolledInAthmaProgram,
 		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
 		
 		CohortIndicator patientsWithBothPeakFlowInSameDDBFormIndicator = Indicators.newCountIndicator(
 		    "patientsWithBothPeakFlowInSameDDBFormIndicator", patientsWithBothPeakFlowInSameDDBFormComposition,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		//=======================================================
 		// B4: Of the new patients enrolled in the last month, % with smoking status documented at intake
 		//=======================================================
 		
 		SqlCohortDefinition patientsWithSmokingHistory = Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate(
 		    "patientsWithSmokingHistory", DDBform, smokingHistory);
 		
 		CompositionCohortDefinition patientsWithSmokingHistoryComposition = new CompositionCohortDefinition();
 		patientsWithSmokingHistoryComposition.setName("patientsWithBothPeakFlowInSameDDBFormComposition");
 		patientsWithSmokingHistoryComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithSmokingHistoryComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsWithSmokingHistoryComposition.getSearches().put(
 		    "1",
 		    new Mapped<CohortDefinition>(enrolledInAthmaProgram, ParameterizableUtil
 		            .createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
 		patientsWithSmokingHistoryComposition.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithSmokingHistory, 
 				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
 		patientsWithSmokingHistoryComposition.setCompositionString("1 AND 2");
 		
 		CohortIndicator patientsWithSmokingHistoryIndicator = Indicators.newCountIndicator(
 		    "patientsWithSmokingHistoryIndicator", patientsWithSmokingHistoryComposition,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		//=======================================================
 		// D1: Of total patients seen in the last month, % with no asthma/COPD-related regimen documented ever (asthma meds: salbutamol, beclomethasone, prednisolone, aminophyilline)
 		//=======================================================
 		
 		SqlCohortDefinition patientsWithAsthmaVisit = new SqlCohortDefinition();
 		patientsWithAsthmaVisit.setQuery("select distinct patient_id from encounter where encounter_type="
 		        + asthmaEncounterType.getId()
 		        + " and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0");
 		patientsWithAsthmaVisit.setName("patientsWithAsthmaVisit");
 		patientsWithAsthmaVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithAsthmaVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
 		
 		SqlCohortDefinition patientsHaveDrugOrdersInAsthmaMedication = Cohorts.getPatientsEverNotOnRegimen(
 		    "patientsHaveDrugOrdersInAsthmaMedication", asthmasMedications);
 		
 		CompositionCohortDefinition patientsWithAsthmaVisitAndEverNotOnRegimen = new CompositionCohortDefinition();
 		patientsWithAsthmaVisitAndEverNotOnRegimen.setName("patientsWithAsthmaVisitAndEverNotOnRegimen");
 		patientsWithAsthmaVisitAndEverNotOnRegimen.addParameter(new Parameter("startDate", "startDate", Date.class));
 		patientsWithAsthmaVisitAndEverNotOnRegimen.addParameter(new Parameter("endDate", "endDate", Date.class));
 		patientsWithAsthmaVisitAndEverNotOnRegimen.addSearch("1", patientsWithAsthmaVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		patientsWithAsthmaVisitAndEverNotOnRegimen.addSearch("2", patientsHaveDrugOrdersInAsthmaMedication, "");
 		patientsWithAsthmaVisitAndEverNotOnRegimen.setCompositionString("1 AND (NOT 2)");
 		
 		CohortIndicator patientsWithAsthmaVisitIndicator = Indicators.newCountIndicator("patientsWithAsthmaVisitIndicator",
 		    patientsWithAsthmaVisit,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		CohortIndicator patientsWithAsthmaVisitAndEverNotOnRegimenIndicator = Indicators.newCountIndicator(
 		    "patientsEverNotOnRegimenIndicator", patientsWithAsthmaVisitAndEverNotOnRegimen,
 		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
 		
 		//========================================================
 		//        Adding columns to data set definition         //
 		//========================================================
 		
 		dsd.addColumn(
 		    "B3N",
 		    "patients with documented peak flow taken both before and after salbutamol at intake",
 		    new Mapped(patientsWithBothPeakFlowInSameDDBFormIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		dsd.addColumn("B3D", "new patients enrolled in report period", new Mapped(enrolledInAthmaProgramIndicator,
 		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		dsd.addColumn(
 		    "B4N",
 		    "patients with smoking status documented at intake",
 		    new Mapped(patientsWithSmokingHistoryIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		dsd.addColumn(
 		    "D1N",
 		    "patients with no asthma/COPD-related regimen documented ever (asthma meds: salbutamol, beclomethasone, prednisolone, aminophyilline)",
 		    new Mapped(patientsWithAsthmaVisitAndEverNotOnRegimenIndicator, ParameterizableUtil
 		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 		dsd.addColumn("D1D", "Of total patients seen in report period", new Mapped(patientsWithAsthmaVisitIndicator,
 		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
 		
 	}
 	
 	private void setUpProperties() {
 		asthmaProgram = gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
 		asthmaPrograms.add(asthmaProgram);
 		asthmaEncounterType = gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT);
 		DDBform = gp.getForm(GlobalPropertiesManagement.ASTHMA_DDB);
 		rendevousForm = gp.getForm(GlobalPropertiesManagement.ASTHMA_RENDEVOUS_VISIT_FORM);
 		
 		DDBforms.add(DDBform);
 		DDBforms.add(rendevousForm);
 		
 		patientsSeenEncounterTypes.add(asthmaEncounterType);
 		
 		onOrAfterOnOrBefore.add("onOrAfter");
 		onOrAfterOnOrBefore.add("onOrBefore");
 		
 		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrAfter");
 		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrBefore");
 		
 		peakFlowAfterSalbutamol = gp.getConcept(GlobalPropertiesManagement.PEAK_FLOW_AFTER_SALBUTAMOL);
 		peakFlowBeforeSalbutamol = gp.getConcept(GlobalPropertiesManagement.PEAK_FLOW_BEFORE_SALBUTAMOL);
 		
 		smokingHistory = gp.getConcept(GlobalPropertiesManagement.SMOKING_HISTORY);
 		
 		salbutamol = gp.getConcept(GlobalPropertiesManagement.SALBUTAMOL_DRUG);
 		
 		beclomethasone = gp.getConcept(GlobalPropertiesManagement.BECLOMETHASONE_DRUG);
 		
 		prednisolone = gp.getConcept(GlobalPropertiesManagement.PREDNISOLONE_DRUG);
 		
 		basicInhalerTrainingProvided = gp.getConcept(GlobalPropertiesManagement.BASIC_INHALER_TRAINING_PROVIDED);
 		properInhalerTechnique = gp.getConcept(GlobalPropertiesManagement.PROPER_INHALER_TECHNIQUE);
 		
 		asthmasMedications = gp.getConceptsByConceptSet(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_DISEASE_TREATMENT_DRUGS);
 
 		asthmasMedicationsWithoutSalbutamol = new ArrayList<Concept>(asthmasMedications);
 		asthmasMedicationsWithoutSalbutamol.remove(salbutamol);
 
 		locOfHosp = gp.getConcept(GlobalPropertiesManagement.LOCATION_OF_HOSPITALIZATION);
 		
 		severePersistentAsthma = gp.getConcept(GlobalPropertiesManagement.SEVERE_PERSISTENT_ASTHMA);
 		
 		severeUncontrolledAsthma = gp.getConcept(GlobalPropertiesManagement.SEVERE_UNCONTROLLED_ASTHMA);
 		
 		asthmaclassification = gp.getConcept(GlobalPropertiesManagement.ASTHMA_CLASSIFICATION);
 		
 		asthmasClassificationAnswers.add(severePersistentAsthma);
 		
 		asthmasClassificationAnswers.add(severeUncontrolledAsthma);
 		
 		returnVisitDate = gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
 		
 		patientDied = gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
 			    GlobalPropertiesManagement.CRD_TREATMENT_WORKFLOW,
 			    GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
 	}
 }
