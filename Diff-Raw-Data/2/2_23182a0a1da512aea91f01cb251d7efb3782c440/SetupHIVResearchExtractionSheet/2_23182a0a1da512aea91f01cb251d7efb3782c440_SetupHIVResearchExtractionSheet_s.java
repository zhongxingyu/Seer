 package org.openmrs.module.rwandareports.reporting;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.openmrs.Concept;
 import org.openmrs.EncounterType;
 import org.openmrs.Program;
 import org.openmrs.ProgramWorkflow;
 import org.openmrs.ProgramWorkflowState;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
 import org.openmrs.module.reporting.evaluation.parameter.Mapped;
 import org.openmrs.module.reporting.evaluation.parameter.Parameter;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.service.ReportService;
 import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
 import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfWorkflowStateChange;
 import org.openmrs.module.rwandareports.definition.ArtSwitch;
 import org.openmrs.module.rwandareports.definition.StartOfArt;
 import org.openmrs.module.rwandareports.util.Cohorts;
 import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
 import org.openmrs.module.rwandareports.util.RowPerPatientColumns;
 
 public class SetupHIVResearchExtractionSheet {
 	
 	Helper h = new Helper();
 	
 	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
 	
 	//properties retrieved from global variables
 	
 	private Program pmtct;
 	private Program adultHiv;
 	private Program pediHiv;
 	private Program pmtctCC;
 	private Program externalHiv;
 	private Program tb;
 	
 	private ProgramWorkflowState onArt;
 	private ProgramWorkflowState onArtPMTCTPreg;
 	
 	private Concept art;
 	private Concept who;
 	private Concept cd4;
 	private Concept cd4Perc;
 	private Concept weight;
 	private Concept height;
 	private Concept hgb;
 	private Concept oi_one;
 	private Concept oi_two;
 	private Concept pneumonia;
 	private Concept pneumoniaSubAcute;
 	private Concept candidiasis;
 	private Concept candidiasisEsophageal;
 	private Concept candidiasisOral;
 	private Concept convulsions;
 	private Concept encephalopathy;
 	private Concept extraPulmonaryTB;
 	private Concept genitalSores;
 	private Concept herpesSimplex; 
 	private Concept herpesZoster;
 	private Concept kaposisSarcoma;
 	private Concept meningitisCrypto;
 	private Concept meningitisTB;
 	private Concept nodularRash;
 	private Concept dysphagia;
 	private Concept toxoplasmosis;
 	private Concept tuberculoma;
 	private Concept tuberculousEnteritis;
 	private Concept anaphylaxis;
 	private Concept nonCoded;
 	private Concept sideEffect;
 	private Concept rashModerate;
 	private Concept severeRash;
 	private Concept nausea;
 	private Concept vomiting;
 	private Concept jaundice;
 	private Concept neuropathy;
 	private Concept anemia;
 	private Concept lacticAcidosis;
 	private Concept hepatitis;
 	private Concept nightmares;
 	private Concept lipodistrophy;
 	private Concept bactrim;
 	private Concept viralLoad;
 	
 	private List<Concept> sideEffects = new ArrayList<Concept>();
 	
 	private List<Concept> oiConcepts = new ArrayList<Concept>();
 	
 	private List<Program> allHivPrograms = new ArrayList<Program>();
 	
 	private List<Program> pmtctPrograms = new ArrayList<Program>();
 	
 	private List<EncounterType> clinicalEnountersIncLab;
 	
 	private List<ProgramWorkflow> pw = new ArrayList<ProgramWorkflow>();
 	
 	private CompositionCohortDefinition adultPreArtMale;
 	private CompositionCohortDefinition adultPreArtMalePlus40;
 	
 	private CompositionCohortDefinition adultPreArtFemale;
 	private CompositionCohortDefinition adultPreArtFemalePlus40;
 	
 	private CompositionCohortDefinition adultArtMale;
 	private CompositionCohortDefinition adultArtMalePlus40;
 	
 	private CompositionCohortDefinition adultArtFemale;
 	private CompositionCohortDefinition adultArtFemalePlus40;
 	
 	private CompositionCohortDefinition adultFemale;
 	private CompositionCohortDefinition adultFemalePlus40;
 	
 	private CompositionCohortDefinition adultMale;
 	private CompositionCohortDefinition adultMalePlus40;
 	
 	private InProgramCohortDefinition pedi;
 	
 	private InProgramCohortDefinition allHiv;
 	
 	public void setup() throws Exception {
 		
 		setupProperties();
 		
 		createReportPediDefinition();
 		createReportFemaleDefinition();
 		createReportMaleDefinition();
 	}
 	
 	public void delete() {
 		ReportService rs = Context.getService(ReportService.class);
 		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
 			if (rd.getName().contains("ResearchDemographics") || rd.getName().contains("ResearchOI") || rd.getName().contains("ResearchSE") || rd.getName().contains("ResearchOngoing")) {
 				rs.purgeReportDesign(rd);
 			}
 		}
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - Demographics Pediatric");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - Demographics Adult Male");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - Demographics Adult Female");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - OI Pediatric");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - OI Adult Male");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - OI Adult Female");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - SE Pediatric");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - SE Adult Male");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - SE Adult Female");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - Ongoing Pediatric");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - Ongoing Adult Male");
 		h.purgeReportDefinition("Research-Extraction Data for HIV Research - Ongoing Adult Female");
 	}
 	
 	private void createReportPediDefinition() throws Exception {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("onOrBefore", "${endDate}");
 		
 		ReportDefinition reportDefinition = new ReportDefinition();
 		reportDefinition.setName("Research-Extraction Data for HIV Research - Demographics Pediatric");
 		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createDemographicsPediatricDataSetDefinition(reportDefinition);
 		
 		//All Pediatic HIV Patients
 		reportDefinition.setBaseCohortDefinition(pedi, new HashMap<String, Object>());
 		h.saveReportDefinition(reportDefinition);
 		
 		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(reportDefinition, "HIVResearchExtractionDemographicsPedi.xls",
 		    "ResearchDemographicsPediatricExcel", null);
 		
 		Properties props = new Properties();
 		props.put("repeatingSections", "sheet:1,row:2,dataset:dataset");
 		design.setProperties(props);
 		
 		h.saveReportDesign(design);
 		
 		ReportDefinition reportDefinitionOI = new ReportDefinition();
 		reportDefinitionOI.setName("Research-Extraction Data for HIV Research - OI Pediatric");
 		reportDefinitionOI.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createOIPediatricDataSetDefinition(reportDefinitionOI);
 		
 		//All Pediatic HIV Patients
 		reportDefinitionOI.setBaseCohortDefinition(pedi, mappings);
 		h.saveReportDefinition(reportDefinitionOI);
 		
 		ReportDesign designOI = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionOI, "HIVResearchExtractionOIPedi.xls",
 		    "ResearchOIPediatricExcel", null);
 		
 		designOI.setProperties(props);
 		
 		h.saveReportDesign(designOI);
 		
 		ReportDefinition reportDefinitionSE = new ReportDefinition();
 		reportDefinitionSE.setName("Research-Extraction Data for HIV Research - SE Pediatric");
 		reportDefinitionSE.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createSEPediatricDataSetDefinition(reportDefinitionSE);
 		
 		//All Pediatic HIV Patients
 		reportDefinitionSE.setBaseCohortDefinition(pedi, mappings);
 		h.saveReportDefinition(reportDefinitionSE);
 		
 		ReportDesign designSE = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionSE, "HIVResearchExtractionSEPedi.xls",
 		    "ResearchSEPediatricExcel", null);
 		
 		designSE.setProperties(props);
 		
 		h.saveReportDesign(designSE);
 		
 		ReportDefinition reportDefinitionOn = new ReportDefinition();
 		reportDefinitionOn.setName("Research-Extraction Data for HIV Research - Ongoing Pediatric");
 		reportDefinitionOn.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createOngoingPediatricDataSetDefinition(reportDefinitionOn);
 		
 		//All Pediatic HIV Patients
 		reportDefinitionOn.setBaseCohortDefinition(pedi, mappings);
 		h.saveReportDefinition(reportDefinitionOn);
 		
 		ReportDesign designOn = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionOn, "HIVResearchExtractionOngoingPedi.xls",
 		    "ResearchOngoingPediatricExcel", null);
 		
 		designOn.setProperties(props);
 		
 		h.saveReportDesign(designOn);
 	}
 	
 	private void createReportFemaleDefinition() throws Exception {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("onOrBefore", "${endDate}");
 		
 		ReportDefinition reportDefinition = new ReportDefinition();
 		reportDefinition.setName("Research-Extraction Data for HIV Research - Demographics Adult Female");
 		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createDemographicsFemaleDataSetDefinition(reportDefinition);
 		
 		//All HIV Patients
 		reportDefinition.setBaseCohortDefinition(allHiv, mappings);
 		h.saveReportDefinition(reportDefinition);
 		
 		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(reportDefinition, "HIVResearchExtractionDemographicsAdult.xls",
 		    "ResearchDemographicsAdultFemaleExcel", null);
 		
 		Properties props = new Properties();
 		props.put("repeatingSections", "sheet:1,row:2,dataset:dataset|sheet:1,row:3,dataset:dataset2|sheet:1,row:4,dataset:dataset3|sheet:1,row:5,dataset:dataset4");
 		design.setProperties(props);
 		
 		h.saveReportDesign(design);
 		
 		ReportDefinition reportDefinitionOI = new ReportDefinition();
 		reportDefinitionOI.setName("Research-Extraction Data for HIV Research - OI Adult Female");
 		reportDefinitionOI.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createOIFemaleDataSetDefinition(reportDefinitionOI);
 		
 		//All HIV Patients
 		reportDefinitionOI.setBaseCohortDefinition(allHiv, mappings);
 		h.saveReportDefinition(reportDefinitionOI);
 		
 		ReportDesign designOI = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionOI, "HIVResearchExtractionOIAdult.xls",
 		    "ResearchOIAdultFemaleExcel", null);
 		
 		Properties propsOI = new Properties();
 		propsOI.put("repeatingSections", "sheet:1,row:2,dataset:dataset|sheet:1,row:3,dataset:dataset2");
 		designOI.setProperties(propsOI);
 		
 		h.saveReportDesign(designOI);
 		
 		ReportDefinition reportDefinitionSE = new ReportDefinition();
 		reportDefinitionSE.setName("Research-Extraction Data for HIV Research - SE Adult Female");
 		reportDefinitionSE.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createSEFemaleDataSetDefinition(reportDefinitionSE);
 		
 		//All HIV Patients
 		reportDefinitionSE.setBaseCohortDefinition(allHiv, mappings);
 		h.saveReportDefinition(reportDefinitionSE);
 		
 		ReportDesign designSE = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionSE, "HIVResearchExtractionSEAdult.xls",
 		    "ResearchSEAdultFemaleExcel", null);
 		
 		designSE.setProperties(props);
 		
 		h.saveReportDesign(designSE);
 		
 		ReportDefinition reportDefinitionOn = new ReportDefinition();
 		reportDefinitionOn.setName("Research-Extraction Data for HIV Research - Ongoing Adult Female");
 		reportDefinitionOn.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createOngoingFemaleDataSetDefinition(reportDefinitionOn);
 		
 		//All HIV Patients
 		reportDefinitionOn.setBaseCohortDefinition(allHiv, mappings);
 		h.saveReportDefinition(reportDefinitionOn);
 		
 		ReportDesign designOn = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionOn, "HIVResearchExtractionOngoingAdult.xls",
 		    "ResearchOngoingAdultFemaleExcel", null);
 		
 		designOn.setProperties(props);
 		
 		h.saveReportDesign(designOn);
 		
 	}
 	
 	private void createReportMaleDefinition() throws Exception {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("onOrBefore", "${endDate}");
 		
 		ReportDefinition reportDefinition = new ReportDefinition();
 		reportDefinition.setName("Research-Extraction Data for HIV Research - Demographics Adult Male");
 		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createDemographicsMaleDataSetDefinition(reportDefinition);
 		
 		//All Pediatic HIV Patients
 		reportDefinition.setBaseCohortDefinition(allHiv, mappings);
 		h.saveReportDefinition(reportDefinition);
 		
 		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(reportDefinition, "HIVResearchExtractionDemographicsAdult.xls",
 		    "ResearchDemographicsAdultMaleExcel", null);
 		
 		Properties props = new Properties();
 		props.put("repeatingSections", "sheet:1,row:2,dataset:dataset|sheet:1,row:3,dataset:dataset2|sheet:1,row:4,dataset:dataset3|sheet:1,row:5,dataset:dataset4");
 		design.setProperties(props);
 		
 		h.saveReportDesign(design);
 		
 		ReportDefinition reportDefinitionOI = new ReportDefinition();
 		reportDefinitionOI.setName("Research-Extraction Data for HIV Research - OI Adult Male");
 		reportDefinitionOI.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createOIMaleDataSetDefinition(reportDefinitionOI);
 		
 		//All Pediatic HIV Patients
 		reportDefinitionOI.setBaseCohortDefinition(allHiv, mappings);
 		h.saveReportDefinition(reportDefinitionOI);
 		
 		ReportDesign designOI = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionOI, "HIVResearchExtractionOIAdult.xls",
 		    "ResearchIOAdultMaleExcel", null);
 		
 		Properties propsOI = new Properties();
 		propsOI.put("repeatingSections", "sheet:1,row:2,dataset:dataset|sheet:1,row:3,dataset:dataset2");
 		designOI.setProperties(propsOI);
 		
 		h.saveReportDesign(designOI);
 		
 		ReportDefinition reportDefinitionSE = new ReportDefinition();
 		reportDefinitionSE.setName("Research-Extraction Data for HIV Research - SE Adult Male");
 		reportDefinitionSE.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createSEMaleDataSetDefinition(reportDefinitionSE);
 		
 		//All Pediatic HIV Patients
 		reportDefinitionSE.setBaseCohortDefinition(allHiv, mappings);
 		h.saveReportDefinition(reportDefinitionSE);
 		
 		ReportDesign designSE = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionSE, "HIVResearchExtractionSEAdult.xls",
 		    "ResearchSEAdultMaleExcel", null);
 		
 		designSE.setProperties(props);
 		
 		h.saveReportDesign(designSE);
 		
 		ReportDefinition reportDefinitionOn = new ReportDefinition();
 		reportDefinitionOn.setName("Research-Extraction Data for HIV Research - Ongoing Adult Male");
 		reportDefinitionOn.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		createOngoingMaleDataSetDefinition(reportDefinitionOn);
 		
 		//All Pediatic HIV Patients
 		reportDefinitionOn.setBaseCohortDefinition(allHiv, mappings);
 		h.saveReportDefinition(reportDefinitionOn);
 		
 		ReportDesign designOn = h.createRowPerPatientXlsOverviewReportDesign(reportDefinitionOn, "HIVResearchExtractionOngoingAdult.xls",
 		    "ResearchOngoingAdultMaleExcel", null);
 		
 		designOn.setProperties(props);
 		
 		h.saveReportDesign(designOn);
 	}
 	
 	
 	private void createDemographicsMaleDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		// Create new dataset definition 
 		//break it up into 4 datasets so that it is able to run as large datasets slow down
 		//over time
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition2.setName("dataset2");
 		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition3.setName("dataset3");
 		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition4.setName("dataset4");
 		
 		//Add filter for adultPreArtMale
 		dataSetDefinition.addFilter(adultPreArtMale, mappings);
 		//Add filter for adultArtMale
 		dataSetDefinition2.addFilter(adultArtMale, mappings);
 		//Add filter for adultPreArtMale
 		dataSetDefinition3.addFilter(adultPreArtMalePlus40, mappings);
 		//Add filter for adultArtMale
 		dataSetDefinition4.addFilter(adultArtMalePlus40, mappings);
 		
 		addDemographicsColumns(dataSetDefinition);
 		addDemographicsColumns(dataSetDefinition2);
 		addDemographicsColumns(dataSetDefinition3);
 		addDemographicsColumns(dataSetDefinition4);
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
 		reportDefinition.addDataSetDefinition("dataset3", dataSetDefinition3, mappings);
 		reportDefinition.addDataSetDefinition("dataset4", dataSetDefinition4, mappings);
 	}
 	
 	private void createOIMaleDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		// Create new dataset definition 
 		//break it up into 4 datasets so that it is able to run as large datasets slow down
 		//over time
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition2.setName("dataset2");
 		
 		//Add filter for adultPreArtMale
 		dataSetDefinition.addFilter(adultMale, mappings);
 		//Add filter for adultArtMale
 		dataSetDefinition2.addFilter(adultMalePlus40, mappings);
 		
 		addOIColumns(dataSetDefinition);
 		addOIColumns(dataSetDefinition2);
 
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
 	}
 	
 	private void createSEMaleDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		// Create new dataset definition 
 		//break it up into 4 datasets so that it is able to run as large datasets slow down
 		//over time
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition2.setName("dataset2");
 		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition3.setName("dataset3");
 		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition4.setName("dataset4");
 		
 		//Add filter for adultPreArtMale
 		dataSetDefinition.addFilter(adultPreArtMale, mappings);
 		//Add filter for adultArtMale
 		dataSetDefinition2.addFilter(adultArtMale, mappings);
 		//Add filter for adultPreArtMale
 		dataSetDefinition3.addFilter(adultPreArtMalePlus40, mappings);
 		//Add filter for adultArtMale
 		dataSetDefinition4.addFilter(adultArtMalePlus40, mappings);
 		
 		addSEColumns(dataSetDefinition);
 		addSEColumns(dataSetDefinition2);
 		addSEColumns(dataSetDefinition3);
 		addSEColumns(dataSetDefinition4);
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
 		reportDefinition.addDataSetDefinition("dataset3", dataSetDefinition3, mappings);
 		reportDefinition.addDataSetDefinition("dataset4", dataSetDefinition4, mappings);
 	}
 	
 	private void createOngoingMaleDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		// Create new dataset definition 
 		//break it up into 4 datasets so that it is able to run as large datasets slow down
 		//over time
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition2.setName("dataset2");
 		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition3.setName("dataset3");
 		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition4.setName("dataset4");
 		
 		//Add filter for adultPreArtMale
 		dataSetDefinition.addFilter(adultPreArtMale, mappings);
 		//Add filter for adultArtMale
 		dataSetDefinition2.addFilter(adultArtMale, mappings);
 		//Add filter for adultPreArtMale
 		dataSetDefinition3.addFilter(adultPreArtMalePlus40, mappings);
 		//Add filter for adultArtMale
 		dataSetDefinition4.addFilter(adultArtMalePlus40, mappings);
 		
 		addOngoingColumns(dataSetDefinition);
 		addOngoingColumns(dataSetDefinition2);
 		addOngoingColumns(dataSetDefinition3);
 		addOngoingColumns(dataSetDefinition4);
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
 		reportDefinition.addDataSetDefinition("dataset3", dataSetDefinition3, mappings);
 		reportDefinition.addDataSetDefinition("dataset4", dataSetDefinition4, mappings);
 	}
 	
 	private void createDemographicsFemaleDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		// Create new dataset definition 
 		//break it up into 4 datasets so that it is able to run as large datasets slow down
 		//over time
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition2.setName("dataset2");
 		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition3.setName("dataset3");
 		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition4.setName("dataset4");
 		
 		//Add filter for adultPreArtFemale
 		dataSetDefinition.addFilter(adultPreArtFemale, mappings);
 		//Add filter for adultArtFemale
 		dataSetDefinition2.addFilter(adultArtFemale, mappings);
 		//Add filter for adultPreArtFemale
 		dataSetDefinition3.addFilter(adultPreArtFemalePlus40, mappings);
 		//Add filter for adultArtFemale
 		dataSetDefinition4.addFilter(adultArtFemalePlus40, mappings);
 		
 		addDemographicsColumns(dataSetDefinition);
 		addDemographicsColumns(dataSetDefinition2);
 		addDemographicsColumns(dataSetDefinition3);
 		addDemographicsColumns(dataSetDefinition4);
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
 		reportDefinition.addDataSetDefinition("dataset3", dataSetDefinition3, mappings);
 		reportDefinition.addDataSetDefinition("dataset4", dataSetDefinition4, mappings);
 	}
 	
 	private void createOIFemaleDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		// Create new dataset definition 
 		//break it up into 4 datasets so that it is able to run as large datasets slow down
 		//over time
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition2.setName("dataset2");
 	
 		//Add filter for adultPreArtFemale
 		dataSetDefinition.addFilter(adultFemale, mappings);
 		//Add filter for adultArtFemale
 		dataSetDefinition2.addFilter(adultFemalePlus40, mappings);
 		
 		addOIColumns(dataSetDefinition);
 		addOIColumns(dataSetDefinition2);
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
 	}
 	
 	private void createSEFemaleDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		// Create new dataset definition 
 		//break it up into 4 datasets so that it is able to run as large datasets slow down
 		//over time
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition2.setName("dataset2");
 		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition3.setName("dataset3");
 		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition4.setName("dataset4");
 	
 		//Add filter for adultPreArtFemale
 		dataSetDefinition.addFilter(adultPreArtFemale, mappings);
 		//Add filter for adultArtFemale
 		dataSetDefinition2.addFilter(adultArtFemale, mappings);
 		//Add filter for adultPreArtFemale
 		dataSetDefinition3.addFilter(adultPreArtFemalePlus40, mappings);
 		//Add filter for adultArtFemale
 		dataSetDefinition4.addFilter(adultArtFemalePlus40, mappings);
 		
 		addSEColumns(dataSetDefinition);
 		addSEColumns(dataSetDefinition2);
 		addSEColumns(dataSetDefinition3);
 		addSEColumns(dataSetDefinition4);
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
 		reportDefinition.addDataSetDefinition("dataset3", dataSetDefinition3, mappings);
 		reportDefinition.addDataSetDefinition("dataset4", dataSetDefinition4, mappings);
 	}
 	
 	private void createOngoingFemaleDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		// Create new dataset definition 
 		//break it up into 4 datasets so that it is able to run as large datasets slow down
 		//over time
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition2.setName("dataset2");
 		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition3.setName("dataset3");
 		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
 		dataSetDefinition4.setName("dataset4");
 		
 		//Add filter for adultPreArtFemale
 		dataSetDefinition.addFilter(adultPreArtFemale, mappings);
 		//Add filter for adultArtFemale
 		dataSetDefinition2.addFilter(adultArtFemale, mappings);
 		//Add filter for adultPreArtFemale
 		dataSetDefinition3.addFilter(adultPreArtFemalePlus40, mappings);
 		//Add filter for adultArtFemale
 		dataSetDefinition4.addFilter(adultArtFemalePlus40, mappings);
 		
 		addOngoingColumns(dataSetDefinition);
 		addOngoingColumns(dataSetDefinition2);
 		addOngoingColumns(dataSetDefinition3);
 		addOngoingColumns(dataSetDefinition4);
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition2, mappings);
 		reportDefinition.addDataSetDefinition("dataset3", dataSetDefinition3, mappings);
 		reportDefinition.addDataSetDefinition("dataset4", dataSetDefinition4, mappings);
 	}
 	
 	private void createDemographicsPediatricDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		// Create new dataset definition 
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		
 		addDemographicsColumns(dataSetDefinition);
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 	}
 	
 	private void createOIPediatricDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		// Create new dataset definition 
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		
 		addOIColumns(dataSetDefinition);
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 	}
 	
 	private void createSEPediatricDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		// Create new dataset definition 
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		
 		addSEColumns(dataSetDefinition);
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 	}
 	
 	private void createOngoingPediatricDataSetDefinition(ReportDefinition reportDefinition) {
 		
 		// Create new dataset definition 
 		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
 		dataSetDefinition.setName("dataset");
 		
 		addOngoingColumns(dataSetDefinition);
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);
 	}
 	
 	private void addDemographicsColumns(RowPerPatientDataSetDefinition dataSetDefinition)
 	{
 		dataSetDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		//Add Columns
 		DateOfWorkflowStateChange artStart = RowPerPatientColumns.getDateOfWorkflowStateChange("art_prog_start_date", art, "endDate", "yyyy-MM-dd");
 		
 		//IMB Id
 		dataSetDefinition.addColumn(RowPerPatientColumns.getAnyId("IMB Id"), new HashMap<String, Object>());
 	
 		//unique identifier
 		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientHash("uniqueID"), new HashMap<String, Object>());
 		
 		//hiv_pos_date - Date of HIV diagnosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getHIVDiagnosisDate("diagnosis"), new HashMap<String, Object>());
 		
 		//art_start_date - ART start date
 		dataSetDefinition.addColumn(RowPerPatientColumns.getDrugOrderForStartOfARTBeforeDate("art_start_date", "yyyy-MM-dd"), mappings);
 		
 		//hiv_care_start_date - HIV Care program enrollment date
 		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfAllHIVEnrolment("hiv_care_start_date", "yyyy-MM-dd"), new HashMap<String, Object>());
 		
 		//art_prog_start_date - ART program enrollment date
 		dataSetDefinition.addColumn(artStart, mappings);
 		
 		//transfer - Transfer In
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("transfer", RowPerPatientColumns.getDateOfProgramEnrolment("transferEnroll", externalHiv, "yyyy-MM-dd")), new HashMap<String, Object>());
 		
 		//last_clinic_date - last_clinic_date
 		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfMostRecentEncounterType("last_clinic_date", clinicalEnountersIncLab, "endDate", "yyyy-MM-dd"), mappings);
 		
 		//outcome - Outcome
 		dataSetDefinition.addColumn(RowPerPatientColumns.getHIVOutcomeOnEndDate("outcome", "yyyy-MM-dd"), mappings);
 		
 		//village - Umudugudu
 		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("village", false, false, false, true), new HashMap<String, Object>());
 		
 		//cell - Cell
 		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("cell", false, false, true, false), new HashMap<String, Object>());
 		
 		//healthcenter - Health Center
 		dataSetDefinition.addColumn(RowPerPatientColumns.getHealthCenter("healthcenter"), new HashMap<String, Object>());
 		
 		//district - District
 		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("district", true, false, false, false), new HashMap<String, Object>());
 		
 		//gender - Gender
 		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("gender"), new HashMap<String, Object>());
 		
 		//birth_date - Date of birth
 		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfBirth("birth_date", "yyyy-MM-dd", "yyyy-MM-dd"), new HashMap<String, Object>());
 		
 		//pmtct_ever - PMTCT
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("pmtct_ever", "endDate", mappings, RowPerPatientColumns.getDateOfAllPMTCTEnrolment("pmtct", "yyyy-MM-dd")), mappings);
 		
 		//pmtct_drug - PMTCT treatment
 		//TODO
 		
 		//accompaniment - Accompaniment
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("accompaniment", RowPerPatientColumns.getAccompRelationship("accomp")), new HashMap<String, Object>());
 		
 		//age_art_start - Age at ART start
 		//TODO
 		
 		//who_stage_art_start - WHO Stage at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservation("who_stage_art_start", who, 90, 30, artStart, "yyyy-MM-dd"), new HashMap<String, Object>());
 		
 		//cd4_art_start - CD4 at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservation("cd4_art_start", cd4, 90, 30, artStart, "yyyy-MM-dd"), new HashMap<String, Object>());
 		
 		//cd4_perc_art_start - CD4 percent at ART start
		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservation("cd4_perc_art_start", cd4Perc, 90, 30, artStart, "yyyy-MM-dd"), new HashMap<String, Object>());
 		
 		//weight_art_start - weight at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservation("weight_art_start", weight, 90, 30, artStart, "yyyy-MM-dd"), new HashMap<String, Object>());
 		
 		//height_art_start - height at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservation("height_art_start", height, 90, 30, artStart, "yyyy-MM-dd"), new HashMap<String, Object>());
 		
 		//hgb_art_start - hemoglobin at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservation("hgb_art_start", hgb, 90, 30, artStart, "yyyy-MM-dd"), new HashMap<String, Object>());
 		
 		//regimen_art_start - ART regimen at ART start
 		StartOfArt startOfArt = RowPerPatientColumns.getDrugOrdersForStartOfARTBeforeDate("regimen_art_start");
 		dataSetDefinition.addColumn(startOfArt, mappings);
 		
 		//bactrim_art_start - Bactrim at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("bactrim_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineDrugOrderBeforeEndDate("bactrim", bactrim, 30, 30, artStart, mappings, "yyyy-MM-dd")), mappings);
 		
 		//tb_art_start - TB at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("tb_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineProgramEnrollmentBeforeEndDate("tb", tb, 30, 30, artStart, mappings, "yyyy-MM-dd")), mappings);
 		
 		ArtSwitch switch1 = RowPerPatientColumns.getDrugOrdersForARTSwitchBeforeDate("ART_switch_1_reg", startOfArt, mappings);
 		//ART_switch_1	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("ART_switch_1", "endDate", mappings, switch1), mappings);
 		//ART_switch_1_reg	
 		dataSetDefinition.addColumn(switch1, mappings);
 		//ART_switch_1_date	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getDateForARTSwitchBeforeDate("ART_switch_1_date", startOfArt, mappings), mappings);
 		
 		ArtSwitch switch2 = RowPerPatientColumns.getDrugOrdersForARTSwitchBeforeDate("ART_switch_2_reg", switch1, mappings);
 		//ART_switch_2	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("ART_switch_2", "endDate", mappings, switch2), mappings);
 		//ART_switch_2_reg	
 		dataSetDefinition.addColumn(switch2, mappings);
 		//ART_switch_2_date
 		dataSetDefinition.addColumn(RowPerPatientColumns.getDateForARTSwitchBeforeDate("ART_switch_2_date", switch1, mappings), mappings);
 		
 		ArtSwitch switch3 = RowPerPatientColumns.getDrugOrdersForARTSwitchBeforeDate("ART_switch_3_reg", switch2, mappings);
 		//ART_switch_3	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("ART_switch_3", "endDate", mappings, switch3), mappings);
 		//ART_switch_3_reg
 		dataSetDefinition.addColumn(switch3, mappings);
 		//ART_switch_3_date	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getDateForARTSwitchBeforeDate("ART_switch_3_date", switch2, mappings), mappings);
 		
 		//viralload	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValuesBeforeEndDate("viralload", viralLoad, 5, "yyyy-MM-dd", null, null), mappings);
 	}
 	
 	private void addOIColumns(RowPerPatientDataSetDefinition dataSetDefinition) {
 		
 		dataSetDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		//Add Columns
 		DateOfWorkflowStateChange artStart = RowPerPatientColumns.getDateOfWorkflowStateChange("art_prog_start_date", art, "endDate", "yyyy-MM-dd");
 		
 		//unique identifier
 		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientHash("uniqueId"), new HashMap<String, Object>());
 		
 		//OI_pneu_art_start - acute pneumonia presumed bacterial
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pneu_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_pneu_art_start", oiConcepts, pneumonia, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_art_start - candidiasis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_cand_art_start", oiConcepts, candidiasis, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_es_art_start - candidiasis, esophageal
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_es_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_cand_es_art_start", oiConcepts, candidiasisEsophageal, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_oral_art_start - candidiasis, oral
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_oral_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_cand_oral_art_start", oiConcepts, candidiasisOral, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_convul_art_start - convulsions or neurological deficit presumed cerebral lymphoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_convul_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_convul_art_start", oiConcepts, convulsions, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_enceph_art_start - encephalopathy presumed due to HIV
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_enceph_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_enceph_art_start", oiConcepts, encephalopathy, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_gensores_art_start - genital sores
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_gensores_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_gensores_art_start", oiConcepts, genitalSores, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpes_art_start - herpes simplex genital infection
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpes_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_herpes_art_start", oiConcepts, herpesSimplex, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpeszost_art_start - herpes zoster
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpeszost_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_herpeszost_art_start", oiConcepts, herpesZoster, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_KS_art_start - kaposis sarcoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_KS_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_KS_art_start", oiConcepts, kaposisSarcoma, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_men_crypto_art_start - meningitis sub-acute presumed cryptococcus
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_men_crypto_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_men_crypto_art_start", oiConcepts, meningitisCrypto, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_nod_rash_art_start - nodular rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_nod_rash_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_nod_rash_art_start", oiConcepts, nodularRash, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_other_art_start - other non-coded
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_other_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_other_art_start", oiConcepts, nonCoded, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_dys_art_start - severe dysphagia presumed invasive
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_dys_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_dys_art_start", oiConcepts, dysphagia, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_pcp_art_start - sub-acute pneumonia presumed pcp
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pcp_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_pcp_art_start", oiConcepts, pneumoniaSubAcute, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_toxo_art_start	- toxoplasmosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_toxo_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_toxo_art_start", oiConcepts, toxoplasmosis, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_tuberculoma_art_start - tuberculoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_tuberculoma_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_tuberculoma_art_start", oiConcepts, tuberculoma, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_TBent_art_start - tuberculous enteritis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_TBent_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_TBent_art_start", oiConcepts, tuberculousEnteritis, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		//OI_anaph_art_start - anaphylaxis	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_anaph_art_start", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerBeforeEndDate("OI_anaph_art_start", oiConcepts, anaphylaxis, 90, 30, artStart, "yyyy-MM-dd")), mappings);
 		
 		//6 month Variable
 		//OI_pneu_06m - acute pneumonia presumed bacterial
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pneu_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pneu_06m", oiConcepts, pneumonia, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_06m	 - candidiasis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_06mt", oiConcepts, candidiasis, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_es_06m	 - candidiasis, esophageal
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_es_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_es_06m", oiConcepts, candidiasisEsophageal, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_oral_06m	 - candidiasis, oral
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_oral_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_oral_06m", oiConcepts, candidiasisOral, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_convul_06m	 - convulsions or neurological deficit presumed cerebral lymphoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_convul_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_convul_06m", oiConcepts, convulsions, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_enceph_06m	 - encephalopathy presumed due to HIV
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_enceph_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_enceph_06m", oiConcepts, encephalopathy, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_gensores_06m	 - genital sores
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_gensores_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_gensores_06m", oiConcepts, genitalSores, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpes_06m	 - herpes simplex genital infection
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpes_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpes_06m", oiConcepts, herpesSimplex, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpeszost_06m	 - herpes zoster
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpeszost_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpeszost_06m", oiConcepts, herpesZoster, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_KS_06m	 - kaposis sarcoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_KS_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_KS_06m", oiConcepts, kaposisSarcoma, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_men_crypto_06m	 - meningitis sub-acute presumed cryptococcus
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_men_crypto_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_men_crypto_06m", oiConcepts, meningitisCrypto, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_nod_rash_06m - nodular rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_nod_rash_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_nod_rash_06m", oiConcepts, nodularRash, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_other_06m	 - other non-coded
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_other_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_other_06m", oiConcepts, nonCoded, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_dys_06m - severe dysphagia presumed invasive
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_dys_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_dys_06m", oiConcepts, dysphagia, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_pcp_06m	 - sub-acute pneumonia presumed pcp
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pcp_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pcp_06m", oiConcepts, pneumoniaSubAcute, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_toxo_06m	- toxoplasmosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_toxo_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_toxo_06m", oiConcepts, toxoplasmosis, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_tuberculoma_06m - tuberculoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_tuberculoma_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_tuberculoma_06m", oiConcepts, tuberculoma, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_TBent_06m - tuberculous enteritis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_TBent_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_TBent_06m", oiConcepts, tuberculousEnteritis, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_anaph_06m	 - anaphylaxis	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_anaph_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_anaph_06m", oiConcepts, anaphylaxis, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//OI_rash_mild_06m	 - mild rash	
 		//TODO
 		
 		//12 month variables	
 		//OI_pneu_12m - acute pneumonia presumed bacterial
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pneu_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pneu_12m", oiConcepts, pneumonia, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_12m	 - candidiasis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_12mt", oiConcepts, candidiasis, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_es_12m	 - candidiasis, esophageal
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_es_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_es_12m", oiConcepts, candidiasisEsophageal, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_oral_12m	 - candidiasis, oral
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_oral_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_oral_12m", oiConcepts, candidiasisOral, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_convul_12m	 - convulsions or neurological deficit presumed cerebral lymphoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_convul_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_convul_12m", oiConcepts, convulsions, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_enceph_12m	 - encephalopathy presumed due to HIV
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_enceph_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_enceph_12m", oiConcepts, encephalopathy, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_gensores_12m	 - genital sores
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_gensores_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_gensores_12m", oiConcepts, genitalSores, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpes_12m	 - herpes simplex genital infection
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpes_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpes_12m", oiConcepts, herpesSimplex, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpeszost_12m	 - herpes zoster
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpeszost_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpeszost_12m", oiConcepts, herpesZoster, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_KS_12m	 - kaposis sarcoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_KS_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_KS_12m", oiConcepts, kaposisSarcoma, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_men_crypto_12m	 - meningitis sub-acute presumed cryptococcus
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_men_crypto_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_men_crypto_12m", oiConcepts, meningitisCrypto, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_nod_rash_12m - nodular rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_nod_rash_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_nod_rash_12m", oiConcepts, nodularRash, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_other_12m	 - other non-coded
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_other_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_other_12m", oiConcepts, nonCoded, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_dys_12m - severe dysphagia presumed invasive
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_dys_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_dys_12m", oiConcepts, dysphagia, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_pcp_12m	 - sub-acute pneumonia presumed pcp
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pcp_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pcp_12m", oiConcepts, pneumoniaSubAcute, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_toxo_12m	- toxoplasmosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_toxo_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_toxo_12m", oiConcepts, toxoplasmosis, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_tuberculoma_12m - tuberculoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_tuberculoma_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_tuberculoma_12m", oiConcepts, tuberculoma, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_TBent_12m - tuberculous enteritis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_TBent_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_TBent_12m", oiConcepts, tuberculousEnteritis, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//OI_anaph_12m	 - anaphylaxis	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_anaph_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_anaph_12m", oiConcepts, anaphylaxis, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 	
 		//24 month variables
 		//OI_pneu_24m - acute pneumonia presumed bacterial
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pneu_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pneu_24m", oiConcepts, pneumonia, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_24m	 - candidiasis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_24mt", oiConcepts, candidiasis, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_es_24m	 - candidiasis, esophageal
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_es_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_es_24m", oiConcepts, candidiasisEsophageal, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_oral_24m	 - candidiasis, oral
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_oral_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_oral_24m", oiConcepts, candidiasisOral, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_convul_24m	 - convulsions or neurological deficit presumed cerebral lymphoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_convul_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_convul_24m", oiConcepts, convulsions, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_enceph_24m	 - encephalopathy presumed due to HIV
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_enceph_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_enceph_24m", oiConcepts, encephalopathy, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_EPTB_24m		- extra-pulmonary tuberculosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_EPTB_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_EPTB_24m", oiConcepts, extraPulmonaryTB, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_gensores_24m	 - genital sores
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_gensores_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_gensores_24m", oiConcepts, genitalSores, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpes_24m	 - herpes simplex genital infection
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpes_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpes_24m", oiConcepts, herpesSimplex, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpeszost_24m	 - herpes zoster
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpeszost_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpeszost_24m", oiConcepts, herpesZoster, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_KS_24m	 - kaposis sarcoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_KS_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_KS_24m", oiConcepts, kaposisSarcoma, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_men_crypto_24m	 - meningitis sub-acute presumed cryptococcus
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_men_crypto_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_men_crypto_24m", oiConcepts, meningitisCrypto, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_nod_rash_24m - nodular rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_nod_rash_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_nod_rash_24m", oiConcepts, nodularRash, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_other_24m	 - other non-coded
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_other_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_other_24m", oiConcepts, nonCoded, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_dys_24m - severe dysphagia presumed invasive
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_dys_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_dys_24m", oiConcepts, dysphagia, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_pcp_24m	 - sub-acute pneumonia presumed pcp
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pcp_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pcp_24m", oiConcepts, pneumoniaSubAcute, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_toxo_24m	- toxoplasmosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_toxo_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_toxo_24m", oiConcepts, toxoplasmosis, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_tuberculoma_24m - tuberculoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_tuberculoma_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_tuberculoma_24m", oiConcepts, tuberculoma, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_TBent_24m - tuberculous enteritis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_TBent_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_TBent_24m", oiConcepts, tuberculousEnteritis, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//OI_anaph_24m	 - anaphylaxis	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_anaph_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_anaph_24m", oiConcepts, anaphylaxis, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		
 		//36 month variables
 		//OI_pneu_36m - acute pneumonia presumed bacterial
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pneu_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pneu_36m", oiConcepts, pneumonia, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_36m	 - candidiasis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_36mt", oiConcepts, candidiasis, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_es_36m	 - candidiasis, esophageal
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_es_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_es_36m", oiConcepts, candidiasisEsophageal, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_oral_36m	 - candidiasis, oral
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_oral_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_oral_36m", oiConcepts, candidiasisOral, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_convul_36m	 - convulsions or neurological deficit presumed cerebral lymphoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_convul_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_convul_36m", oiConcepts, convulsions, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_enceph_36m	 - encephalopathy presumed due to HIV
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_enceph_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_enceph_36m", oiConcepts, encephalopathy, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_gensores_36m	 - genital sores
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_gensores_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_gensores_36m", oiConcepts, genitalSores, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpes_36m	 - herpes simplex genital infection
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpes_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpes_36m", oiConcepts, herpesSimplex, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpeszost_36m	 - herpes zoster
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpeszost_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpeszost_36m", oiConcepts, herpesZoster, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_KS_36m	 - kaposis sarcoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_KS_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_KS_36m", oiConcepts, kaposisSarcoma, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_men_crypto_36m	 - meningitis sub-acute presumed cryptococcus
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_men_crypto_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_men_crypto_36m", oiConcepts, meningitisCrypto, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_nod_rash_36m - nodular rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_nod_rash_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_nod_rash_36m", oiConcepts, nodularRash, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_other_36m	 - other non-coded
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_other_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_other_36m", oiConcepts, nonCoded, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_dys_36m - severe dysphagia presumed invasive
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_dys_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_dys_36m", oiConcepts, dysphagia, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_pcp_36m	 - sub-acute pneumonia presumed pcp
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pcp_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pcp_36m", oiConcepts, pneumoniaSubAcute, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_toxo_36m	- toxoplasmosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_toxo_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_toxo_36m", oiConcepts, toxoplasmosis, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_tuberculoma_36m - tuberculoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_tuberculoma_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_tuberculoma_36m", oiConcepts, tuberculoma, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_TBent_36m - tuberculous enteritis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_TBent_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_TBent_36m", oiConcepts, tuberculousEnteritis, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//OI_anaph_36m	 - anaphylaxis	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_anaph_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_anaph_36m", oiConcepts, anaphylaxis, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		
 		//48 month variables
 		//OI_pneu_48m - acute pneumonia presumed bacterial
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pneu_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pneu_48m", oiConcepts, pneumonia, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_48m	 - candidiasis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_48mt", oiConcepts, candidiasis, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_es_48m	 - candidiasis, esophageal
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_es_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_es_48m", oiConcepts, candidiasisEsophageal, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_oral_48m	 - candidiasis, oral
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_oral_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_oral_48m", oiConcepts, candidiasisOral, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_convul_48m	 - convulsions or neurological deficit presumed cerebral lymphoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_convul_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_convul_48m", oiConcepts, convulsions, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_enceph_48m	 - encephalopathy presumed due to HIV
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_enceph_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_enceph_48m", oiConcepts, encephalopathy, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_gensores_48m	 - genital sores
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_gensores_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_gensores_48m", oiConcepts, genitalSores, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpes_48m	 - herpes simplex genital infection
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpes_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpes_48m", oiConcepts, herpesSimplex, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpeszost_48m	 - herpes zoster
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpeszost_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpeszost_48m", oiConcepts, herpesZoster, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_KS_48m	 - kaposis sarcoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_KS_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_KS_48m", oiConcepts, kaposisSarcoma, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_men_crypto_48m	 - meningitis sub-acute presumed cryptococcus
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_men_crypto_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_men_crypto_48m", oiConcepts, meningitisCrypto, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_nod_rash_48m - nodular rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_nod_rash_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_nod_rash_48m", oiConcepts, nodularRash, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_other_48m	 - other non-coded
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_other_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_other_48m", oiConcepts, nonCoded, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_dys_48m - severe dysphagia presumed invasive
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_dys_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_dys_48m", oiConcepts, dysphagia, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_pcp_48m	 - sub-acute pneumonia presumed pcp
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pcp_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pcp_48m", oiConcepts, pneumoniaSubAcute, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_toxo_48m	- toxoplasmosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_toxo_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_toxo_48m", oiConcepts, toxoplasmosis, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_tuberculoma_48m - tuberculoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_tuberculoma_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_tuberculoma_48m", oiConcepts, tuberculoma, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_TBent_48m - tuberculous enteritis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_TBent_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_TBent_48m", oiConcepts, tuberculousEnteritis, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//OI_anaph_48m	 - anaphylaxis	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_anaph_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_anaph_48m", oiConcepts, anaphylaxis, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		
 		
 		//60 month variables
 		//OI_pneu_60m - acute pneumonia presumed bacterial
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pneu_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pneu_60m", oiConcepts, pneumonia, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_60m	 - candidiasis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_60mt", oiConcepts, candidiasis, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_es_60m	 - candidiasis, esophageal
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_es_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_es_60m", oiConcepts, candidiasisEsophageal, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_cand_oral_60m	 - candidiasis, oral
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_cand_oral_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_cand_oral_60m", oiConcepts, candidiasisOral, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_convul_60m	 - convulsions or neurological deficit presumed cerebral lymphoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_convul_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_convul_60m", oiConcepts, convulsions, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_enceph_60m	 - encephalopathy presumed due to HIV
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_enceph_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_enceph_60m", oiConcepts, encephalopathy, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_gensores_60m	 - genital sores
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_gensores_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_gensores_60m", oiConcepts, genitalSores, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpes_60m	 - herpes simplex genital infection
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpes_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpes_60m", oiConcepts, herpesSimplex, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_herpeszost_60m	 - herpes zoster
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_herpeszost_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_herpeszost_60m", oiConcepts, herpesZoster, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_KS_60m	 - kaposis sarcoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_KS_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_KS_60m", oiConcepts, kaposisSarcoma, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_men_crypto_60m	 - meningitis sub-acute presumed cryptococcus
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_men_crypto_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_men_crypto_60m", oiConcepts, meningitisCrypto, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_men_TB_60m	 - meningitis sub-acute presumed tuberculosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_men_TB_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_men_TB_60m", oiConcepts, meningitisTB, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_nod_rash_60m - nodular rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_nod_rash_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_nod_rash_60m", oiConcepts, nodularRash, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_other_60m	 - other non-coded
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_other_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_other_60m", oiConcepts, nonCoded, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_dys_60m - severe dysphagia presumed invasive
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_dys_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_dys_60m", oiConcepts, dysphagia, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_pcp_60m	 - sub-acute pneumonia presumed pcp
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_pcp_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_pcp_60m", oiConcepts, pneumoniaSubAcute, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_toxo_60m	- toxoplasmosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_toxo_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_toxo_60m", oiConcepts, toxoplasmosis, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_tuberculoma_60m - tuberculoma
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_tuberculoma_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_tuberculoma_60m", oiConcepts, tuberculoma, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_TBent_60m - tuberculous enteritis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_TBent_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_TBent_60m", oiConcepts, tuberculousEnteritis, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//OI_anaph_60m	 - anaphylaxis	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("OI_anaph_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("OI_anaph_60m", oiConcepts, anaphylaxis, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 	}
 	
 	private void addSEColumns(RowPerPatientDataSetDefinition dataSetDefinition) {
 		
 		dataSetDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		//Add Columns
 		DateOfWorkflowStateChange artStart = RowPerPatientColumns.getDateOfWorkflowStateChange("art_prog_start_date", art, "endDate", "yyyy-MM-dd");
 		
 		//unique identifier
 		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientHash("uniqueId"), new HashMap<String, Object>());
 		
 		//6 month Variables
 		//SE_rash_mod_06m - moderate rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_mod_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_mod_06m", sideEffects, rashModerate, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_rash_sev_06m - severe rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_sev_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_sev_06m", sideEffects, severeRash, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nausea_06m - nausea
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nausea_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nausea_06m", sideEffects, nausea, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_vomit_06m - vomiting
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_vomit_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_vomit_06m", sideEffects, vomiting, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_jaundice_06m - jaundice
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_jaundice_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_jaundice_06m", sideEffects, jaundice, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_neuro_06m - neuropathy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_neuro_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_neuro_06m", sideEffects, neuropathy, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_anemia_06m	- anemia
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_anemia_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_anemia_06m", sideEffects, anemia, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lact_acid_06m - lactic acidosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lact_acid_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lact_acid_06m", sideEffects, lacticAcidosis, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_hep_06m - hepatitis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_hep_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_hep_06m", sideEffects, hepatitis, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nightmares_06m	 - nightmares
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nightmares_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nightmares_06m", sideEffects, nightmares, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lipodis_06m - lipodistrophy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lipodis_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lipodis_06m", sideEffects, lipodistrophy, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		//SE_other_06m - other
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_other_06m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_other_06m", sideEffects, nonCoded, 30, 30, 6, artStart, "yyyy-MM-dd")), mappings);
 		
 		//12 month variables
 		//SE_rash_mod_12m - moderate rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_mod_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_mod_12m", sideEffects, rashModerate, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_rash_sev_12m - severe rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_sev_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_sev_12m", sideEffects, severeRash, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nausea_12m - nausea
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nausea_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nausea_12m", sideEffects, nausea, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_vomit_12m - vomiting
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_vomit_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_vomit_12m", sideEffects, vomiting, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_jaundice_12m - jaundice
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_jaundice_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_jaundice_12m", sideEffects, jaundice, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_neuro_12m - neuropathy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_neuro_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_neuro_12m", sideEffects, neuropathy, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_anemia_12m	- anemia
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_anemia_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_anemia_12m", sideEffects, anemia, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lact_acid_12m - lactic acidosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lact_acid_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lact_acid_12m", sideEffects, lacticAcidosis, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_hep_12m - hepatitis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_hep_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_hep_12m", sideEffects, hepatitis, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nightmares_12m	 - nightmares
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nightmares_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nightmares_12m", sideEffects, nightmares, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lipodis_12m - lipodistrophy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lipodis_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lipodis_12m", sideEffects, lipodistrophy, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 		//SE_other_12m - other
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_other_12m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_other_12m", sideEffects, nonCoded, 30, 30, 12, artStart, "yyyy-MM-dd")), mappings);
 	
 		//24 month variables
 		//SE_rash_mod_24m - moderate rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_mod_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_mod_24m", sideEffects, rashModerate, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_rash_sev_24m - severe rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_sev_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_sev_24m", sideEffects, severeRash, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nausea_24m - nausea
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nausea_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nausea_24m", sideEffects, nausea, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_vomit_24m - vomiting
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_vomit_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_vomit_24m", sideEffects, vomiting, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_jaundice_24m - jaundice
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_jaundice_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_jaundice_24m", sideEffects, jaundice, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_neuro_24m - neuropathy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_neuro_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_neuro_24m", sideEffects, neuropathy, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_anemia_24m	- anemia
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_anemia_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_anemia_24m", sideEffects, anemia, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lact_acid_24m - lactic acidosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lact_acid_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lact_acid_24m", sideEffects, lacticAcidosis, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_hep_24m - hepatitis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_hep_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_hep_24m", sideEffects, hepatitis, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nightmares_24m	 - nightmares
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nightmares_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nightmares_24m", sideEffects, nightmares, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lipodis_24m - lipodistrophy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lipodis_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lipodis_24m", sideEffects, lipodistrophy, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		//SE_other_24m - other
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_other_24m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_other_24m", sideEffects, nonCoded, 30, 30, 24, artStart, "yyyy-MM-dd")), mappings);
 		
 		//36 month variables
 		//SE_rash_mod_36m - moderate rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_mod_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_mod_36m", sideEffects, rashModerate, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_rash_sev_36m - severe rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_sev_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_sev_36m", sideEffects, severeRash, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nausea_36m - nausea
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nausea_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nausea_36m", sideEffects, nausea, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_vomit_36m - vomiting
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_vomit_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_vomit_36m", sideEffects, vomiting, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_jaundice_36m - jaundice
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_jaundice_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_jaundice_36m", sideEffects, jaundice, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_neuro_36m - neuropathy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_neuro_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_neuro_36m", sideEffects, neuropathy, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_anemia_36m	- anemia
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_anemia_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_anemia_36m", sideEffects, anemia, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lact_acid_36m - lactic acidosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lact_acid_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lact_acid_36m", sideEffects, lacticAcidosis, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_hep_36m - hepatitis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_hep_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_hep_36m", sideEffects, hepatitis, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nightmares_36m	 - nightmares
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nightmares_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nightmares_36m", sideEffects, nightmares, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lipodis_36m - lipodistrophy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lipodis_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lipodis_36m", sideEffects, lipodistrophy, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		//SE_other_36m - other
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_other_36m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_other_36m", sideEffects, nonCoded, 30, 30, 36, artStart, "yyyy-MM-dd")), mappings);
 		
 		//48 month variables
 		//SE_rash_mod_48m - moderate rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_mod_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_mod_48m", sideEffects, rashModerate, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_rash_sev_48m - severe rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_sev_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_sev_48m", sideEffects, severeRash, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nausea_48m - nausea
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nausea_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nausea_48m", sideEffects, nausea, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_vomit_48m - vomiting
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_vomit_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_vomit_48m", sideEffects, vomiting, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_jaundice_48m - jaundice
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_jaundice_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_jaundice_48m", sideEffects, jaundice, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_neuro_48m - neuropathy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_neuro_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_neuro_48m", sideEffects, neuropathy, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_anemia_48m	- anemia
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_anemia_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_anemia_48m", sideEffects, anemia, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lact_acid_48m - lactic acidosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lact_acid_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lact_acid_48m", sideEffects, lacticAcidosis, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_hep_48m - hepatitis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_hep_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_hep_48m", sideEffects, hepatitis, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nightmares_48m	 - nightmares
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nightmares_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nightmares_48m", sideEffects, nightmares, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lipodis_48m - lipodistrophy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lipodis_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lipodis_48m", sideEffects, lipodistrophy, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		//SE_other_48m - other
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_other_48m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_other_48m", sideEffects, nonCoded, 30, 30, 48, artStart, "yyyy-MM-dd")), mappings);
 		
 		//60 month variables
 		//SE_rash_mod_60m - moderate rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_mod_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_mod_60m", sideEffects, rashModerate, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_rash_sev_60m - severe rash
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_rash_sev_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_rash_sev_60m", sideEffects, severeRash, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nausea_60m - nausea
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nausea_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nausea_60m", sideEffects, nausea, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_vomit_60m - vomiting
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_vomit_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_vomit_60m", sideEffects, vomiting, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_jaundice_60m - jaundice
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_jaundice_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_jaundice_60m", sideEffects, jaundice, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_neuro_60m - neuropathy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_neuro_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_neuro_60m", sideEffects, neuropathy, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_anemia_60m	- anemia
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_anemia_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_anemia_60m", sideEffects, anemia, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lact_acid_60m - lactic acidosis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lact_acid_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lact_acid_60m", sideEffects, lacticAcidosis, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_hep_60m - hepatitis
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_hep_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_hep_60m", sideEffects, hepatitis, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_nightmares_60m	 - nightmares
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_nightmares_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_nightmares_60m", sideEffects, nightmares, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_lipodis_60m - lipodistrophy
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_lipodis_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_lipodis_60m", sideEffects, lipodistrophy, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 		//SE_other_60m - other
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("SE_other_60m", "endDate", mappings, RowPerPatientColumns.getBaselineObservationAnswerAtMonthBeforeEndDate("SE_other_60m", sideEffects, nonCoded, 30, 30, 60, artStart, "yyyy-MM-dd")), mappings);
 	}
 
 	private void addOngoingColumns(RowPerPatientDataSetDefinition dataSetDefinition) {
 		
 		dataSetDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		
 		//Add Columns
 		DateOfWorkflowStateChange artStart = RowPerPatientColumns.getDateOfWorkflowStateChange("art_prog_start_date", art, "endDate", "yyyy-MM-dd");
 		
 		//unique identifier
 		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientHash("uniqueId"), new HashMap<String, Object>());
 	
 		//6 month Variables
 		
 		//art_retention_06m	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("art_retention_06m", "endDate", mappings, RowPerPatientColumns.getBaselineEncounterAtMonthBeforeEndDate("artRetention", clinicalEnountersIncLab, 60, 60, 6, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//cd4_06m - CD4, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_06m", cd4, 30, 30, 6, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//cd4_perc_06m - CD4 percent, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_perc_06m", cd4Perc, 30, 30, 6, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//weight_06m - weight, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("weight_06m", weight, 30, 30, 6, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//height_06m - height, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("height_06m", height, 30, 30, 6, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//hgb_06m - hemoglobin, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("hgb_06m", hgb, 30, 30, 6, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//bactrim_06m - Bactrim at X months on ART treatment
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("bactrim_06m", "endDate", mappings, RowPerPatientColumns.getBaselineDrugOrderAtMonthBeforeEndDate("bactrim", bactrim, 30, 30, 6, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//tb_06m - TB at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("tb_06m", "endDate", mappings, RowPerPatientColumns.getBaselineProgramEnrollmentAtMonthBeforeEndDate("tb", tb, 30, 30, 6, artStart, mappings, "yyyy-MM-dd")), mappings);
 			
 		
 		//12 month variables
 		//art_retention_12m	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("art_retention_12m", "endDate", mappings, RowPerPatientColumns.getBaselineEncounterAtMonthBeforeEndDate("artRetention", clinicalEnountersIncLab, 60, 60, 12, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//cd4_12m - CD4, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_12m", cd4, 30, 30, 12, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//cd4_perc_12m - CD4 percent, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_perc_12m", cd4Perc, 30, 30, 12, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//weight_12m - weight, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("weight_12m", weight, 30, 30, 12, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//height_12m - height, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("height_12m", height, 30, 30, 12, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//hgb_12m - hemoglobin, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("hgb_12m", hgb, 30, 30, 12, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//bactrim_12m - Bactrim at X months on ART treatment
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("bactrim_12m", "endDate", mappings, RowPerPatientColumns.getBaselineDrugOrderAtMonthBeforeEndDate("bactrim", bactrim, 30, 30, 12, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//tb_12m - TB at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("tb_12m", "endDate", mappings, RowPerPatientColumns.getBaselineProgramEnrollmentAtMonthBeforeEndDate("tb", tb, 30, 30, 12, artStart, mappings, "yyyy-MM-dd")), mappings);
 		
 		//24 month variables
 		//art_retention_24m	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("art_retention_24m", "endDate", mappings, RowPerPatientColumns.getBaselineEncounterAtMonthBeforeEndDate("artRetention", clinicalEnountersIncLab, 60, 60, 24, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//cd4_24m - CD4, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_24m", cd4, 30, 30, 24, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//cd4_perc_24m - CD4 percent, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_perc_24m", cd4Perc, 30, 30, 24, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//weight_24m - weight, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("weight_24m", weight, 30, 30, 24, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//height_24m - height, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("height_24m", height, 30, 30, 24, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//hgb_24m - hemoglobin, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("hgb_24m", hgb, 30, 30, 24, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//bactrim_24m - Bactrim at X months on ART treatment
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("bactrim_24m", "endDate", mappings, RowPerPatientColumns.getBaselineDrugOrderAtMonthBeforeEndDate("bactrim", bactrim, 30, 30, 24, artStart, mappings, "yyyy-MM-dd")), mappings);	
 		//tb_24m - TB at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("tb_24m", "endDate", mappings, RowPerPatientColumns.getBaselineProgramEnrollmentAtMonthBeforeEndDate("tb", tb, 30, 30, 24, artStart, mappings, "yyyy-MM-dd")), mappings);
 		
 		//36 month variables
 		//art_retention_36m	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("art_retention_36m", "endDate", mappings, RowPerPatientColumns.getBaselineEncounterAtMonthBeforeEndDate("artRetention", clinicalEnountersIncLab, 60, 60, 36, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//cd4_36m - CD4, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_36m", cd4, 30, 30, 36, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//cd4_perc_36m - CD4 percent, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_perc_36m", cd4Perc, 30, 30, 36, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//weight_36m - weight, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("weight_36m", weight, 30, 30, 36, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//height_36m - height, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("height_36m", height, 30, 30, 36, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//hgb_36m - hemoglobin, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("hgb_36m", hgb, 30, 30, 36, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//bactrim_36m - Bactrim at X months on ART treatment
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("bactrim_36m", "endDate", mappings, RowPerPatientColumns.getBaselineDrugOrderAtMonthBeforeEndDate("bactrim", bactrim, 30, 30, 36, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//tb_36m - TB at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("tb_36m", "endDate", mappings, RowPerPatientColumns.getBaselineProgramEnrollmentAtMonthBeforeEndDate("tb", tb, 30, 30, 36, artStart, mappings, "yyyy-MM-dd")), mappings);
 		
 		//48 month variables
 		//art_retention_48m	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("art_retention_48m", "endDate", mappings, RowPerPatientColumns.getBaselineEncounterAtMonthBeforeEndDate("artRetention", clinicalEnountersIncLab, 60, 60, 48, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//cd4_48m - CD4, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_48m", cd4, 30, 30, 48, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//cd4_perc_48m - CD4 percent, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_perc_48m", cd4Perc, 30, 30, 48, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//weight_48m - weight, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("weight_48m", weight, 30, 30, 48, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//height_48m - height, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("height_48m", height, 30, 30, 48, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//hgb_48m - hemoglobin, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("hgb_48m", hgb, 30, 30, 48, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//bactrim_48m - Bactrim at X months on ART treatment
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("bactrim_48m", "endDate", mappings, RowPerPatientColumns.getBaselineDrugOrderAtMonthBeforeEndDate("bactrim", bactrim, 30, 30, 48, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//tb_48m - TB at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("tb_48m", "endDate", mappings, RowPerPatientColumns.getBaselineProgramEnrollmentAtMonthBeforeEndDate("tb", tb, 30, 30, 48, artStart, mappings, "yyyy-MM-dd")), mappings);
 		
 		//60 month variables
 		//art_retention_60m	
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("art_retention_60m", "endDate", mappings, RowPerPatientColumns.getBaselineEncounterAtMonthBeforeEndDate("artRetention", clinicalEnountersIncLab, 60, 60, 60, artStart, mappings, "yyyy-MM-dd")), mappings);
 		//cd4_60m - CD4, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_60m", cd4, 30, 30, 60, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//cd4_perc_60m - CD4 percent, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("cd4_perc_60m", cd4Perc, 30, 30, 60, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//weight_60m - weight, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("weight_60m", weight, 30, 30, 60, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//height_60m - height, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("height_60m", height, 30, 30, 60, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//hgb_60m - hemoglobin, X months
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineObservationAtMonthBeforeEndDate("hgb_60m", hgb, 30, 30, 60, artStart, mappings, "yyyy-MM-dd"), mappings);
 		//bactrim_60m - Bactrim at X months on ART treatment
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("bactrim_60m", "endDate", mappings, RowPerPatientColumns.getBaselineDrugOrderAtMonthBeforeEndDate("bactrim", bactrim, 30, 30, 60, artStart, mappings, "yyyy-MM-dd")), mappings);	
 		//tb_60m - TB at ART start
 		dataSetDefinition.addColumn(RowPerPatientColumns.getBooleanRepresentation("tb_60m", "endDate", mappings, RowPerPatientColumns.getBaselineProgramEnrollmentAtMonthBeforeEndDate("tb", tb, 30, 30, 60, artStart, mappings, "yyyy-MM-dd")), mappings);
 	}
 	
 	private void setupProperties() {
 		
 		pmtct = gp.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
 		adultHiv = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
 		pediHiv = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
 		pmtctCC = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
 		externalHiv = gp.getProgram(GlobalPropertiesManagement.EXTERNAL_HIV_PROGRAM);
 		tb = gp.getProgram(GlobalPropertiesManagement.TB_PROGRAM);
 		
 		allHivPrograms.add(pmtct);
 		allHivPrograms.add(adultHiv);
 		//allHivPrograms.add(pediHiv);
 		allHivPrograms.add(pmtctCC);
 		
 		pmtctPrograms.add(pmtct);
 		pmtctPrograms.add(pmtctCC);
 		
 		art = gp.getConcept(GlobalPropertiesManagement.ON_ART_TREATMENT_STATUS_CONCEPT);
 		who = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE);
 		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
 		cd4Perc = gp.getConcept(GlobalPropertiesManagement.CD4_PERCENTAGE_TEST);
 		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
 		height = gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
 		hgb = gp.getConcept(GlobalPropertiesManagement.HEMOGLOBIN);
 		oi_one = gp.getConcept(GlobalPropertiesManagement.OPPORTUNISTIC_INFECTIONS_AND_COMMORBIDITY);
 		oi_two = gp.getConcept(GlobalPropertiesManagement.OPPORTUNISTIC_INFECTIONS);
 		pneumonia = gp.getConcept(GlobalPropertiesManagement.PNEUMONIA);
 		pneumoniaSubAcute = gp.getConcept(GlobalPropertiesManagement.PNEUMONIA_SUB_ACUTE);
 		candidiasis = gp.getConcept(GlobalPropertiesManagement.CANDIDIASIS);
 		candidiasisEsophageal = gp.getConcept(GlobalPropertiesManagement.CANDIDIASIS_ESOPHAGEAL);
 		candidiasisOral = gp.getConcept(GlobalPropertiesManagement.CANDIDIASIS_ORAL);
 		convulsions = gp.getConcept(GlobalPropertiesManagement.CONVULSIONS);
 		encephalopathy = gp.getConcept(GlobalPropertiesManagement.ENCEPHALOPATHY);
 		extraPulmonaryTB = gp.getConcept(GlobalPropertiesManagement.EXTRA_PULMONARY_TB);
 		genitalSores = gp.getConcept(GlobalPropertiesManagement.GENITAL_SORES);
 		herpesSimplex = gp.getConcept(GlobalPropertiesManagement.HERPES_SIMPLEX);
 		herpesZoster = gp.getConcept(GlobalPropertiesManagement.HERPES_ZOSTER);
 		kaposisSarcoma = gp.getConcept(GlobalPropertiesManagement.KARPOSIS_SARCOMA);
 		meningitisCrypto = gp.getConcept(GlobalPropertiesManagement.MENINGITIS_CRYPTO);
 		meningitisTB = gp.getConcept(GlobalPropertiesManagement.MENINGITIS_TB);
 		nodularRash = gp.getConcept(GlobalPropertiesManagement.NODULAR_RASH);
 		dysphagia = gp.getConcept(GlobalPropertiesManagement.DYSPHAGIA);
 		toxoplasmosis = gp.getConcept(GlobalPropertiesManagement.TOXOPLASMOSIS);
 		tuberculoma = gp.getConcept(GlobalPropertiesManagement.TUBERCULOMA);
 		tuberculousEnteritis = gp.getConcept(GlobalPropertiesManagement.TUBERCULOUS_ENTERITIS);
 		anaphylaxis = gp.getConcept(GlobalPropertiesManagement.ANAPHYLAXIS);
 		nonCoded = gp.getConcept(GlobalPropertiesManagement.OTHER_NON_CODED);
 		sideEffect = gp.getConcept(GlobalPropertiesManagement.ADVERSE_EFFECT_CONCEPT);
 		rashModerate = gp.getConcept(GlobalPropertiesManagement.RASH_MODERATE);
 		severeRash = gp.getConcept(GlobalPropertiesManagement.RASH_SEVERE);
 		nausea = gp.getConcept(GlobalPropertiesManagement.NAUSEA);
 		vomiting = gp.getConcept(GlobalPropertiesManagement.VOMITING);
 		jaundice = gp.getConcept(GlobalPropertiesManagement.JAUNDICE);
 		neuropathy = gp.getConcept(GlobalPropertiesManagement.NEUROPATHY);
 		anemia = gp.getConcept(GlobalPropertiesManagement.ANEMIA);
 		lacticAcidosis = gp.getConcept(GlobalPropertiesManagement.LACTIC_ACIDOSIS);
 		hepatitis = gp.getConcept(GlobalPropertiesManagement.HEPATITIS);
 		nightmares = gp.getConcept(GlobalPropertiesManagement.NIGHTMARES);
 		lipodistrophy = gp.getConcept(GlobalPropertiesManagement.LIPODISTROPHY);
 		bactrim = gp.getConcept(GlobalPropertiesManagement.BACTRIM_CONCEPT);
 		viralLoad = gp.getConcept(GlobalPropertiesManagement.VIRAL_LOAD_TEST);
 		
 		sideEffects.add(sideEffect);
 		
 		oiConcepts.add(oi_one);
 		oiConcepts.add(oi_two);
 		
 		clinicalEnountersIncLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES);
 		
 		ProgramWorkflow treatAdult = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
 		ProgramWorkflow treatPedi = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
 		ProgramWorkflow treatPMTCT = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
 		
 		pw.add(treatPMTCT);
 		pw.add(treatAdult);
 		pw.add(treatPedi);
 		
 		onArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
 		onArtPMTCTPreg = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
 		
 		Map<String, Object> mappings = new HashMap<String, Object>();
 		mappings.put("endDate", "${endDate}");
 		Map<String, Object> mappingsPedi = new HashMap<String, Object>();
 		mappingsPedi.put("onOrBefore", "${endDate}");
 		Map<String, Object> mappingsAge = new HashMap<String, Object>();
 		mappingsAge.put("effectiveDate", "${endDate}");
 		
 		allHiv = Cohorts.createInProgramParameterizableByDate("inHiv", allHivPrograms, "onOrBefore");
 		
 		pedi = Cohorts.createInProgramParameterizableByDate("pedi", pediHiv, "onOrBefore");
 		
 		GenderCohortDefinition females = Cohorts.createFemaleCohortDefinition("Female");
 		
 		CompositionCohortDefinition onAllArt = new CompositionCohortDefinition();
 		onAllArt.setName("onArt");
 		onAllArt.getSearches().put(
 		    "adult",
 		    new Mapped<CohortDefinition>(Cohorts.createPatientStateCohortDefinition("onArt", onArt), new HashMap<String, Object>()));
 		onAllArt.getSearches().put(
 		    "pmtctPreg",
 		    new Mapped<CohortDefinition>(Cohorts.createPatientStateCohortDefinition("onArtPMTCTPreg", onArtPMTCTPreg), new HashMap<String, Object>()));
 		onAllArt.setCompositionString("adult or pmtctPreg");
 		
 		CompositionCohortDefinition adultArt = new CompositionCohortDefinition();
 		adultArt.setName("adultArt");
 		adultArt.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultArt.getSearches().put(
 		    "adult",
 		    new Mapped<CohortDefinition>(allHiv, new HashMap<String, Object>()));
 		adultArt.getSearches().put(
 		    "pedi",
 		    new Mapped<CohortDefinition>(pedi, mappingsPedi));
 		adultArt.getSearches().put(
 		    "onArt",
 		    new Mapped<CohortDefinition>(onAllArt, new HashMap<String, Object>()));
 		adultArt.setCompositionString("adult and onArt and not pedi");
 		
 		CompositionCohortDefinition adultPreArt = new CompositionCohortDefinition();
 		adultPreArt.setName("adultPreArt");
 		adultPreArt.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultPreArt.getSearches().put(
 		    "adult",
 		    new Mapped<CohortDefinition>(allHiv, new HashMap<String, Object>()));
 		adultPreArt.getSearches().put(
 		    "pedi",
 		    new Mapped<CohortDefinition>(pedi, mappingsPedi));
 		adultPreArt.getSearches().put(
 		    "onArt",
 		    new Mapped<CohortDefinition>(onAllArt, new HashMap<String, Object>()));
 		adultPreArt.setCompositionString("adult and not onArt and not pedi");
 		
 		AgeCohortDefinition plus40 = new AgeCohortDefinition();
 		plus40.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
 		plus40.setMinAge(40);
 		
 		adultPreArtMale = new CompositionCohortDefinition();
 		adultPreArtMale.setName("adultPreArtMale");
 		adultPreArtMale.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultPreArtMale.getSearches().put(
 		    "adultPreArt",
 		    new Mapped<CohortDefinition>(adultPreArt, mappings));
 		adultPreArtMale.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultPreArtMale.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultPreArtMale.setCompositionString("adultPreArt and not female and not plus40");
 		
 		adultPreArtFemale = new CompositionCohortDefinition();
 		adultPreArtFemale.setName("adultPreArtFemale");
 		adultPreArtFemale.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultPreArtFemale.getSearches().put(
 		    "adultPreArt",
 		    new Mapped<CohortDefinition>(adultPreArt, mappings));
 		adultPreArtFemale.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultPreArtFemale.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultPreArtFemale.setCompositionString("adultPreArt and female and not plus40");
 		
 		adultArtMale = new CompositionCohortDefinition();
 		adultArtMale.setName("adultArtMale");
 		adultArtMale.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultArtMale.getSearches().put(
 		    "adultArt",
 		    new Mapped<CohortDefinition>(adultArt, mappings));
 		adultArtMale.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultArtMale.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultArtMale.setCompositionString("adultArt and not female and not plus40");
 		
 		adultArtFemale = new CompositionCohortDefinition();
 		adultArtFemale.setName("adultArtFemale");
 		adultArtFemale.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultArtFemale.getSearches().put(
 		    "adultArt",
 		    new Mapped<CohortDefinition>(adultArt, mappings));
 		adultArtFemale.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultArtFemale.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultArtFemale.setCompositionString("adultArt and female and not plus40");
 		
 		adultPreArtMalePlus40 = new CompositionCohortDefinition();
 		adultPreArtMalePlus40.setName("adultPreArtMalePlus40");
 		adultPreArtMalePlus40.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultPreArtMalePlus40.getSearches().put(
 		    "adultPreArt",
 		    new Mapped<CohortDefinition>(adultPreArt, mappings));
 		adultPreArtMalePlus40.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultPreArtMalePlus40.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultPreArtMalePlus40.setCompositionString("adultPreArt and not female and plus40");
 		
 		adultPreArtFemalePlus40 = new CompositionCohortDefinition();
 		adultPreArtFemalePlus40.setName("adultPreArtFemalePlus40");
 		adultPreArtFemalePlus40.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultPreArtFemalePlus40.getSearches().put(
 		    "adultPreArt",
 		    new Mapped<CohortDefinition>(adultPreArt, mappings));
 		adultPreArtFemalePlus40.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultPreArtFemalePlus40.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultPreArtFemalePlus40.setCompositionString("adultPreArt and female and plus40");
 		
 		adultArtMalePlus40 = new CompositionCohortDefinition();
 		adultArtMalePlus40.setName("adultArtMalePlus40");
 		adultArtMalePlus40.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultArtMalePlus40.getSearches().put(
 		    "adultArt",
 		    new Mapped<CohortDefinition>(adultArt, mappings));
 		adultArtMalePlus40.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultArtMalePlus40.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultArtMalePlus40.setCompositionString("adultArt and not female and plus40");
 		
 		adultArtFemalePlus40 = new CompositionCohortDefinition();
 		adultArtFemalePlus40.setName("adultArtFemalePlus40");
 		adultArtFemalePlus40.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultArtFemalePlus40.getSearches().put(
 		    "adultArt",
 		    new Mapped<CohortDefinition>(adultArt, mappings));
 		adultArtFemalePlus40.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultArtFemalePlus40.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultArtFemalePlus40.setCompositionString("adultArt and female and plus40");
 		
 		adultFemalePlus40 = new CompositionCohortDefinition();
 		adultFemalePlus40.setName("adultFemalePlus40");
 		adultFemalePlus40.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultFemalePlus40.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultFemalePlus40.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultFemalePlus40.setCompositionString("female and plus40");
 		
 		adultFemale = new CompositionCohortDefinition();
 		adultFemale.setName("adultFemale");
 		adultFemale.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultFemale.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultFemale.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultFemale.setCompositionString("female and not plus40");
 		
 		adultMalePlus40 = new CompositionCohortDefinition();
 		adultMalePlus40.setName("adultMalePlus40");
 		adultMalePlus40.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultMalePlus40.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultMalePlus40.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultMalePlus40.setCompositionString("not female and plus40");
 		
 		adultMale = new CompositionCohortDefinition();
 		adultMale.setName("adultMale");
 		adultMale.addParameter(new Parameter("endDate", "End Date", Date.class));
 		adultMale.getSearches().put(
 		    "female",
 		    new Mapped<CohortDefinition>(females, new HashMap<String, Object>()));
 		adultMale.getSearches().put(
 		    "plus40",
 		    new Mapped<CohortDefinition>(plus40, mappingsAge));
 		adultMale.setCompositionString("not female and not plus40");
 	}
 }
