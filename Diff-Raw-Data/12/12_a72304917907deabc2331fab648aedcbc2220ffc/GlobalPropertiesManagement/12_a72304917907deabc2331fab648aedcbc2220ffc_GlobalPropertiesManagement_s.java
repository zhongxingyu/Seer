 package org.openmrs.module.rwandareports.util;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.openmrs.Concept;
 import org.openmrs.EncounterType;
 import org.openmrs.Form;
 import org.openmrs.OrderType;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.Program;
 import org.openmrs.ProgramWorkflow;
 import org.openmrs.ProgramWorkflowState;
 import org.openmrs.RelationshipType;
 import org.openmrs.api.context.Context;
 
 public class GlobalPropertiesManagement {
 
 	public Program getProgram(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getProgram(globalProperty);
 	}
 	
 	public PatientIdentifierType getPatientIdentifier(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getPatientIdentifierType(globalProperty);
 	}
 	
 	public Concept getConcept(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getConcept(globalProperty);
 	}
 	
 	public List<Concept> getConceptList(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getConceptList(globalProperty);
 	}
 
 	public List<Concept> getConceptsByConceptSet(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		Concept c = MetadataLookup.getConcept(globalProperty);
 		return Context.getConceptService().getConceptsByConceptSet(c);
 	}
 
 	public Form getForm(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getForm(globalProperty);
 	}
 	
 	public EncounterType getEncounterType(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getEncounterType(globalProperty);
 	}
 	
 	public List<EncounterType> getEncounterTypeList(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getEncounterTypeList(globalProperty);
 	}
 	
 	public List<Form> getFormList(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getFormList(globalProperty);
 	}
 	
 	public RelationshipType getRelationshipType(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getRelationshipType(globalProperty);
 	}
 	
 	public OrderType getOrderType(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return MetadataLookup.getOrderType(globalProperty);
 	}
 	
 	public ProgramWorkflow getProgramWorkflow(String globalPropertyName, String programName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getProgramWorkflow(programName, globalProperty);
 	}
 	
 	public ProgramWorkflowState getProgramWorkflowState(String globalPropertyName, String workflowName, String programName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getProgramWorkflowState(programName, workflowName, globalProperty);
 	}
 	
 	public Map<Concept, Double> getVialSizes() {
 		Map<Concept, Double> vialSizes = new HashMap<Concept, Double>();
 		String vialGp =  Context.getAdministrationService().getGlobalProperty("reports.vialSizes");
 		String[] vials = vialGp.split(",");
 		for(String vial: vials) {
 			String[] v = vial.split(":");
 			try {
 				Concept drugConcept = MetadataLookup.getConcept(v[0]);
 				Double size = Double.parseDouble(v[1]);
 				vialSizes.put(drugConcept, size);
 			}
 			catch (Exception e) {
 				throw new IllegalArgumentException("Unable to convert " + vial + " into a vial size Concept and Double", e);
 			}
 		}
 		return vialSizes;
 	}
 
 	public Integer getGlobalPropertyAsInt(String globalPropertyName) {
 		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
 		return Integer.parseInt(globalProperty);
 	}
 
 	//Programs
 	public final static String ADULT_HIV_PROGRAM = "hiv.programid.adult"; 
 	
 	public final static String PMTCT_COMBINED_CLINIC_PROGRAM = "reports.pmtctcombinedprogramname";
 	
 	public final static String PEDI_HIV_PROGRAM = "reports.pedihivprogramname";
 	
 	public final static String PMTCT_PREGNANCY_PROGRAM = "reports.pmtctprogramname";
 	
 	public final static String PMTCT_COMBINED_MOTHER_PROGRAM = "reports.pmtctCombinedMotherProgramname";
 	
 	public final static String TB_PROGRAM = "reports.tbprogramname";
 		
 	public final static String HEART_FAILURE_PROGRAM_NAME ="reports.heartfailureprogramname";
 	
 	public final static String CRD_PROGRAM ="reports.CRDprogramname";
 	
 	public final static String HYPERTENSION_PROGRAM ="reports.hypertensionprogram";
 	
 	public final static String DM_PROGRAM = "reports.diabetesprogram";
 	
 	public final static String NUTRITION_PROGRAM = "reports.nutritionprogram";
 	
 	public final static String ORACTA_STUDY = "reports.oractaprogram";
 	
 	public final static String CHRONIC_RESPIRATORY_PROGRAM = "reports.chronicrespiratoryprogram";
 	
 	public final static String EPILEPSY_PROGRAM = "reports.epilepsyprogram";
 	
 	public final static String ONCOLOGY_PROGRAM = "reports.oncologyprogram";
 	
 	public final static String EXTERNAL_HIV_PROGRAM = "reports.externalHivProgram";
 	
 	//ProgramWorkflow
 	public final static String TREATMENT_STATUS_WORKFLOW = "reports.hivworkflowstatus";
 	
 	public final static String TREATMENT_GROUP_WORKFLOW = "reports.hivtreatmentstatus";
 	
 	public final static String FEEDING_GROUP_WORKFLOW = "reports.pmtctFeedingStatusWorkflowName";
 	
 	public final static String HEART_FAILURE_SURGERY_STATUS = "reports.heartFailureSurgeryWorkflow";
 	
 	public final static String INFORMED_STATUS = "reports.hivworkflowstatusinformed";
 	
 	public final static String COUNSELLING_GROUP_WORKFLOW = "reports.hivworkflowstatuscounselling";
 	
 	public final static String TB_TREATMENT_GROUP_WORKFLOW = "reports.tbworkflowgroup";
 	
     public final static String ASSISTANCE_STATUS_WORKFLOW = "reports.assistancestatus";
 	
 	public final static String DIABETE_TREATMENT_WORKFLOW= "reports.diabeteworkflow";
 	
 	public final static String CRD_TREATMENT_WORKFLOW="reports.chronicrespiratorystate";
 	
 	public final static String PREGNANCY_STATUS_WORKFLOW = "reports.pregnantStatus";
 	
 	public final static String TREATMENT_INTENT_WORKFLOW = "reports.treatmentIntentWorkflow";
 	
 	public final static String DIAGNOSIS_WORKFLOW = "reports.diagnosisWorkflow";
 	
 	public final static String SURGERY_STATUS_WORKFLOW = "reports.surgeryStatusWorkflow";
 	
 	public final static String HEART_FAILURE_TREATMENT_STATUS_WORKFLOW  = "reports.heartFailureTreatmentStatusWorkflow";
 	
 	
 	//ProgramWorkflowState
 	public final static String ON_ANTIRETROVIRALS_STATE = "reports.hivonartstate";
 	
 	public final static String FOLLOWING_STATE = "reports.hivpreartstate";
 	
 	public final static String HEART_FAILURE_POST_OPERATIVE_STATE = "reports.heartFailureSurgeryPostOperativeWorkflowState";
 	
 	public final static String BREASTFEEDING_STATE_ONE = "reports.breastfeedingStateOne";
 	
 	public final static String BREASTFEEDING_STATE_TWO = "reports.breastfeedingStateTwo";
 	
 	public final static String BREASTFEEDING_STATE_THREE = "reports.breastfeedingStateThree";
 	
 	public final static String FORMULA_STATE_ONE = "reports.formulaStateOne";
 	
 	public final static String FORMULA_STATE_TWO = "reports.formulaStateOne";
 	
 	public final static String FORMULA_STATE_THREE = "reports.formulaStateThree";
 	
 	public final static String PATIENT_PREGNANT_STATE = "reports.patientpregnant";
 	
 	public final static String PATIENT_DIED_STATE="report.died";
 	
 	public final static String ARTIFICIAL_FEEDING_9_18_MONTHS_STATE="reports.pmtctArtificialfeeding(9to18months)StateName";
 	
 	public final static String ARTIFICIAL_FEEDING_6_9_MONTHS_STATE="reports.pmtctArtificialfeeding(6to9months)StateName";
 	
 	public final static String ARTIFICIAL_FEEDING_0_6_MONTHS_STATE="reports.pmtctArtificialfeeding(0to6months)StateName";
 	
 	public final static String POST_OPERATIVE_STATE="reports.postOperativeState";
 	
 	public final static String HEART_FAILURE_PATIENT_DIED_STATE="reports.heartFailurePatientDiedState";
 	
 	public final static String TRANSFERRED_OUT_STATE="reports.patienttransferedoutstate";
 	
 	//Identifiers
 	public final static String IMB_IDENTIFIER = "reports.imbIdIdentifier"; 
 	
 	public final static String PC_IDENTIFIER = "reports.primaryCareIdIdentifier"; 
 	
 	public final static String TRACNET_IDENTIFIER = "reports.tracIdentifier";
 	
 	public final static String INVALID_IMB_IDENTIFIER = "reports.invalidimbIdIdentifier"; 
 	
 	//Concepts
 	public final static String TELEPHONE_NUMBER_CONCEPT = "reports.telephoneNumberConcept"; 
 	
 	public final static String SECONDARY_TELEPHONE_NUMBER_CONCEPT = "reports.secondaryTelephoneNumberConcept"; 
 	
 	public final static String WEIGHT_CONCEPT = "reports.weightConcept"; 
 	
 	public final static String HEIGHT_CONCEPT = "reports.heightConcept";
 	
 	public final static String BSA_CONCEPT = "reports.bsa";
 	
 	public final static String IO_CONCEPT = "reports.ioConcept";
 	
 	public final static String SIDE_EFFECT_CONCEPT = "reports.sideEffectConcept";
 	
 	public final static String RETURN_VISIT_DATE = "concept.returnVisitDate";
 	
 	public final static String NOT_DONE = "reports.notDone";
 	
 	public final static String NONE = "concept.none";
 	
 	public final static String HEART_FAILURE_DIAGNOSIS = "reports.heartFailureDiagnosis";
 	
 	public final static String CARDIOMYOPATHY = "reports.cardiomyopathy";
 	
 	public final static String MITRAL_STENOSIS = "reports.mitralStenosis";
 	
 	public final static String RHUEMATIC_HEART_DISEASE = "reports.rhuematicHeartDisease";
 	
 	public final static String HYPERTENSIVE_HEART_DISEASE = "reports.hypertensiveHeartDisease";
 	
 	public final static String PERICARDIAL_DISEASE = "reports.pericardialDisease";
 	
 	public final static String CONGENITAL_HEART_FAILURE = "reports.congenitalHeartFailure";
 	
 	public final static String PATIENTS_USING_FAMILY_PLANNING = "reports.patientsUsingFamilyPlanning";
 	
 	public final static String PULSE = "reports.pulse";
 	
 	public final static String REASON_FOR_EXITING_CARE = "reports.reasonForExitingCare";
 	
 	public final static String PATIENT_DIED = "reports.patientDied";
 	
 	public final static String INTERNATIONAL_NORMALIZED_RATIO = "reports.internationalNormalizedRatio";
 	
 	public final static String DISPOSITION = "reports.disposition";
 	
 	public final static String ADMIT_TO_HOSPITAL = "reports.admitToHospital";
 	
 	public final static String ON_ART_TREATMENT_STATUS_CONCEPT = "reports.onArtTreatmentStatusConcept";
 	
 	public final static String STAGE_CONCEPT = "reports.stageConcept";
 	
 	public final static String PREGNANCY_DELIVERY_DATE = "reports.pregnancyDeliveryDateConcept";
 	
 	public final static String POSITIVE_HIV_TEST_ANSWER = "reports.positiveHivTestConcept";
 	
 	public final static String DDR = "reports.ddrConcept";
 	
 	public final static String DPA = "reports.dpaConcept";
 	
 	public final static String PREGNANCY_TEST_DATE = "reports.pregnancyTestDate";
 	
 	public final static String RWANDA_INSURANCE_TYPE = "registration.insuranceTypeConcept";
 	
 	public final static String MUTUELLE = "reports.mutuelle";
 	
 	public final static String RAMA = "reports.rama";
 	
 	public final static String MMI = "reports.mmi";
 	
 	public final static String MEDIPLAN = "reports.mediplan";
 	
 	public final static String CORAR = "reports.corar";
 	
 	public final static String TEMPERATURE = "concept.temperature";
 	
 	public final static String GLUCOSE="reports.glucoseConcept";
 	
 	public final static String DIASTOLIC_BLOOD_PRESSURE="reports.DiastolicBPConcept";
 	
 	public final static String SYSTOLIC_BLOOD_PRESSURE="reports.SystolicBPConcept";
 	
 	public final static String HBA1C="reports.HbA1cConcept";
 	
 	public final static String SENSATION_IN_RIGHT_FOOT="reports.SensationInRightFootConcept";
 	
 	public final static String SENSATION_IN_LEFT_FOOT="reports.SensationInLeftFootConcept";
 
 	public final static String HIV_DIAGNOSIS_DATE = "reports.hivDiagnosisDate";
 	
 	public final static String HEIGHT_WEIGHT_PERCENTAGE = "reports.hieghtWeightPercentage";
 	
 	public final static String HOSPITAL_ADMITTANCE="reports.hospitalAdmittanceConcept";
 	
 	public final static String LOCATION_OF_HOSPITALIZATION="reports.locationOfHospitalization";
 	
 	public final static String BIRTH_WEIGHT = "reports.birthWeight";
 	
 	public final static String NYHA_CLASS = "reports.NYHACLASS";
 	
 	public final static String NYHA_CLASS_4 = "reports.NYHACLASS4";
 	
 	public final static String TRIPLE_THERAPY_DURING_PREGNANCY = "reports.tripleTherapyDuringPregnancy";
 	
 	public final static String MONO_THERAPY_DURING_PREGNANCY = "reports.monoTherapyDuringPregnancy";
 	
 	public final static String NO_THERAPY_DURING_PREGNANCY = "reports.noTherapyDuringPregnancy";
 	
 	public final static String PROPHYLAXIS_FOR_MOTHER_IN_PMTCT = "reports.prophylaxisDuringPregnancy";
 	
 	public final static String MOTHER_CD4 = "reports.motherCD4";
 	
 	public final static String CHANGE_TO_ARTIFICIAL_MILK = "reports.changeToArtificialMilk";
 	
 	public final static String PEAK_FLOW_AFTER_SALBUTAMOL="reports.peakFlowAfterSalbutamolConcept";
 	
 	public final static String PEAK_FLOW_BEFORE_SALBUTAMOL="reports.peakFlowBeforeSalbutamolConcept";
 	
 	public final static String ASTHMA_CLASSIFICATION="reports.asthmaclassificationConcept";
 	
 	public final static String SEVERE_PERSISTENT_ASTHMA="reports.severePersistentAsthma";
 	
 	public final static String SEVERE_UNCONTROLLED_ASTHMA="reports.severeUncontrolledAsthma";
 	
 	public final static String ASTHMA_CLASSIFICATION_ORDER="reports.asthmaclassificationorder";
 	
 	public final static String SMOKING_HISTORY = "reports.smokingHistory"; 
 	
 	public final static String BASIC_INHALER_TRAINING_PROVIDED="reports.basicInhalerTrainingProvided"; 
 	
 	public final static String PROPER_INHALER_TECHNIQUE="reports.properInhalerTechnique"; 
 	
 	public final static String PREMEDICATION = "reports.premedication";
 	
 	public final static String CHEMOTHERAPY = "reports.chemotherapy";
 	
 	public final static String POSTMEDICATION = "reports.postmedication";
 	
 	public final static String SEIZURE_CONCEPT = "reports.seizureConcept";
 	
 	public final static String IV_CONCEPT = "reports.ivConcept";
 	
 	public final static String ONC_ADMINISTRATION_INSTRUCTIONS = "reports.oncAdminInstructions";
 	
 	public final static String DOXORUBICIN_GIVEN = "reports.doxorubicinGiven";
 	
 	public final static String DAUNORUBICIN_GIVEN = "reports.daunorubicinGiven";
 	
 	public final static String PNEUMONIA = "reports.pneumonia";
 	
 	public final static String PNEUMONIA_SUB_ACUTE = "reports.pneumoniaSubAcute";
 	
 	public final static String CANDIDIASIS = "reports.candidiasis";
 	
 	public final static String CANDIDIASIS_ESOPHAGEAL = "reports.candidiasisEsophageal";
 	
 	public final static String CANDIDIASIS_ORAL = "reports.candidiasisOral";
 	
 	public final static String CONVULSIONS = "reports.convulsions";
 	
 	public final static String ENCEPHALOPATHY = "reports.encephalopathy";
 	
 	public final static String EXTRA_PULMONARY_TB = "reports.extraPulmonaryTB";
 	
 	public final static String PULMONARY_TB = "reports.pulmonaryTB";
 	
 	public final static String GENITAL_SORES = "reports.genitalSores";
 	
 	public final static String HERPES_SIMPLEX = "reports.herpesSimplex";
 	
 	public final static String HERPES_ZOSTER = "reports.herpesZoster";
 	
 	public final static String KARPOSIS_SARCOMA = "reports.kaposisSarcoma";
 	
 	public final static String MENINGITIS_CRYPTO = "reports.meningitisCrypto";
 	
 	public final static String MENINGITIS_TB = "reports.meningitisTB";
 	
 	public final static String NODULAR_RASH = "reports.nodularRash";
 	
 	public final static String DYSPHAGIA = "reports.dysphagia";
 	
 	public final static String TOXOPLASMOSIS = "reports.toxoplasmosis";
 	
 	public final static String TUBERCULOMA = "reports.tuberculoma";
 	
 	public final static String TUBERCULOUS_ENTERITIS = "reports.tuberculousEnteritis";
 	
 	public final static String ANAPHYLAXIS = "reports.anaphylaxis";
 	
 	public final static String OPPORTUNISTIC_INFECTIONS_AND_COMMORBIDITY = "reports.oiAndCommorbidity";
 	
 	public final static String OPPORTUNISTIC_INFECTIONS = "reports.oi";
 	
 	public final static String OTHER_NON_CODED = "reports.otherNonCoded";
 	
 	public final static String ADVERSE_EFFECT_CONCEPT = "reports.adverseEffect";
 	
 	public final static String RASH_MODERATE = "reports.rashModerate";
 	
 	public final static String RASH_SEVERE = "reports.rashSevere";
 	
 	public final static String NAUSEA = "reports.nausea";
 	
 	public final static String VOMITING = "reports.vomiting";
 	
 	public final static String JAUNDICE = "reports.jaundice";
 	
 	public final static String NEUROPATHY = "reports.neuropathy";
 	
 	public final static String ANEMIA = "reports.anemia";
 	
 	public final static String LACTIC_ACIDOSIS = "reports.lacticAcidosis";
 	
 	public final static String HEPATITIS = "reports.hepatitis";
 	
 	public final static String NIGHTMARES = "reports.nightmares";
 	
 	public final static String LIPODISTROPHY = "reports.lipodistrophy";
 	
 	public final static String ONCOLOGY_SCHEDULED_OUTPATIENT_VISIT = "reports.oncologyOutpatientScheduledVisit";
 	
 	public final static String ONCOLOGY_SCHEDULED_TEST_VISIT = "reports.oncologyTestScheduledVisit";
 	
 	public final static String ONCOLOGY_PATHOLOGY_RESULT_VISIT = "reports.oncologyPathologyResultVisit";
 	
 	public final static String ONCOLOGY_SPECIAL_VISIT = "reports.oncologySpecialVisit";
 	
 	public final static String BIOPSY_TEST_SITE = "reports.tissueSite";
 	
 	public final static String ONCOLOGY_BLOOD_DIAGNOSTIC_TEST = "reports.oncologyBloodDiagnostic";
 	
 	public final static String EJECTION_FRACTION="reports.ejectionFraction";
 	
 	public final static String ADVERSE_MED_EFFECT="reports.adversemed";
     
     public final static String ADVERSE_MED_EFFECT_NON_CODED = "reports.adverseMedNonCoded";
     
     public final static String ADVERSE_EFFECT_REACTION = "reports.adverseReaction";
     
     public final static String ADVERSE_EFFECT_REACTION_NON_CODED = "reports.adverseReactionNonCoded";
     
     public final static String ADVERSE_EFFECT_DATE = "reports.adverseReactionDate";
     
     public final static String ORAL_ROUTE = "reports.oralRoute";
     
     public final static String TABLET_FORM = "reports.tabletForm";
     
     public final static String BIOPSY_URL = "reports.biopsyUrl";
     
     public final static String PRIMARY_DOCTOR_CONSTRUCT = "reports.primaryDoctorConstruct";
     
     public final static String PRIMARY_DOCTOR_NAME = "reports.primaryDoctorName";
     
     public final static String PRIMARY_DOCTOR_TELEPHONE = "reports.primaryDoctorTelephone";
     
     public final static String PRIMARY_DOCTOR_EMAIL = "reports.primaryDoctorEmail";
     
     public final static String PATHOLOGY_ACCESSION_NUMBER = "reports.pathologyAccessionNumber";
     
 	public final static String PATHOLOGY_RESULTS_COMMUNICATED = "reports.pathologyResultsCommunicated";
 	
 	public final static String ONCOLOGY_TEST_CONSTRUCT = "reports.oncologyTestConstruct";
 	
 	public final static String LABORATORY_TESTS_ORDERED = "reports.labTestsOrdered";
 	
 	public final static String TISSUE_BIOPSY = "reports.tissueBiopsy";
 	
 	//Primary Care Service concepts
 	public static final String PRIMARY_CARE_SERVICE_REQUESTED = "reports.primaryCareServiceRequested";	
 	public static final String VCT_PROGRAM = "reports.vctProgram";							
 	public static final String ANTENATAL_CLINIC = "reports.antenatalClinic";						
 	public static final String FAMILY_PLANNING_SERVICES = "reports.familyPlanningServices";			
 	public static final String MUTUELLE_SERVICE = "reports.mutuelleServices";						
 	public static final String ACCOUNTING_OFFICE_SERVICE = "reports.accountingOfficeServices";			
 	public static final String INTEGRATED_MANAGEMENT_OF_ADULT_ILLNESS_SERVICE = "reports.integratedManagementOfAdultIllnessServices";
 	public static final String INTEGRATED_MANAGEMENT_OF_CHILDHOOD_ILLNESS = "reports.integratedManagementOfChildhoodIllnessServices";		
 	public static final String INFECTIOUS_DISEASES_CLINIC_SERVICE = "reports.infectiousDiseasesClinicService";	
 	public static final String SOCIAL_WORKER_SERVICE = "reports.socialWorkerService";				
 	public static final String PREVENTION_OF_MOTHER_TO_CHILD_TRANSMISSION_SERVICE = "reports.pmtctService";
 	public static final String LABORATORY_SERVICES = "reports.laboratoryService";					
 	public static final String PHARMACY_SERVICES = "reports.pharmacyService";					
 	public static final String MATERNITY_SERVICE = "reports.maternityService";					
 	public static final String HOSPITALIZATION_SERVICE = "reports.hospitalizationService";				
 	public static final String VACCINATION_SERVICE = "reports.vaccinationService";	
 	
 	//Encounters
 	public final static String ADULT_FLOWSHEET_ENCOUNTER = "reports.adultflowsheetencounter";
 
 	public final static String CLINICAL_ENCOUNTER_TYPES = "ClinicalencounterTypeIds.labTestIncl";
 	
 	public final static String CLINICAL_ENCOUNTER_TYPES_EXC_LAB_TEST = "ClinicalencounterTypeIds.labTestExcl";
 	
 	public final static String CARDIOLOGY_ENCTOUNTER_TYPES = "cardiologyreporting.cardilogyEncounterTypes";
 	
 	public final static String PEDI_FLOWSHEET_ENCOUNTER = "reports.pediFlowsheetEncounter";
 	
 	public final static String HIV_ENCOUNTER_TYPES = "reports.hivencountertypes";
 	
 	public final static String PRIMARY_CARE_REGISTRATION = "primarycarereport.registration.encountertypeid";
 	
 	public final static String VITALS = "primarycarereport.vitals.encountertypeid";
 	
 
 	public final static String DIABETES_VISIT="reports.DiabetesIncounterType";
 	
 	public final static String ADULT_INITIAL_VISIT="reports.AdultInitialVisitIncounterType";
 	
 
 	public final static String TRANSFER_ENCOUNTER = "reports.transferEncounter";
 	
 	public final static String TRANSFER_IN_ENCOUNTER_TYPE = "reports.transferInEncounterType";
 	
 	public final static String ASTHMA_VISIT="reports.AsthmaEncounterType";
 	
 	public final static String ASTHMA_RENDEVOUS_VISIT_FORM="reports.AsthmaRendevousVisitForm";
 	
 	public final static String EXPOSED_INFANT_ENCOUNTER="report.exposed_infantencounter";
 	
 	public final static String ADULT_FLOWSHEET_VISIT = "adultflowsheet.Form_ddbId";
 	
 	public final static String LAB_ENCOUNTER_TYPE="reports.labEncounterType";
 	public final static String OUTPATIENT_ONCOLOGY_ENCOUNTER = "reports.OutpatientOncologyEncounterType";
 	
 	public final static String EPILEPSY_VISIT = "reports.EpilepsyEncounterType";
 	
 	public final static String INPATIENT_ONCOLOGY_ENCOUNTER = "reports.InpatientOncologyEncounterType";
 	
 	public final static String HYPERTENSION_ENCOUNTER = "reports.HypertensionEncounterType";
 	
 	public final static String HEART_FAILURE_ENCOUNTER = "reports.HeartFailureEncounterType";
 	
 	public final static String PATIENT_TRANSFER_ENCOUNTER = "reports.PatientTransferEncounterType";
 	
 	public final static String PEDIANDADULTHIV_ENCOUNTER_TYPES = "reports.hivencounterTypeIds";
 	
 	public final static String NCD_FOLLOWUP_FORM = "reports.ncdFollowUpForm";
 	
 	//RelationshipTypes
 	public final static String ACCOMPAGNATUER_RELATIONSHIP = "reports.accompagnatuerRelationship";
 	
 	public final static String MOTHER_RELATIONSHIP = "reports.pmtctMotherRelationship";
 	
 	//Forms
 	public final static String CARDIOLOGY_CONSULT_FORM = "cardiologyreporting.cardilogyConsultationFormId";
 	
 	public final static String DIABETES_DDB_FORM="reports.DiabetesDDBForm";
 	
 	public final static String DIABETES_RDV_FORM="reports.DiabetesRDVForm";
 	
 	public final static String CARDIOLOGY_DDB = "cardiologyreporting.hFDonneDeBaseFormId";
 
 	public final static String PMTCT_DDB = "reports.pmtctDDBFormId";
 	
 	public final static String PMTCT_RDV = "reports.pmtctRDVFormId";
 	
 	public final static String PMTCT_FLOW = "reports.pmtctFlowFormID";
 	
 	public final static String ADULT_FLOW_VISIT = "rwandaadulthivflowsheet.Form_NewVisit";
 	
 	public final static String PEDI_FLOW_VISIT="rwandapedihivflowsheet.Form_NewVisit";
 	
 	public final static String TRANSFER_TO_PMTCT = "reports.transferToPMTCTFormID";
 	
 	public final static String TRANSFER_TO_CC = "reports.transferToCCFormID";
 	
 	public final static String DIABETES_FLOW_VISIT = "report.diabetesFlowVisit";
 	
 	public final static String ASTHMA_DDB = "reports.asthmaDDBformId";
 	
 	public final static String HYPERTENSION_DDB = "reports.hypertensionDDBformId";
 	
 	public final static String HYPERTENSION_FLOW_VISIT = "reports.hypertensionFlowVisit";
 	
 	public final static String HYPERTENSION_FLOW_ECHOCARDIOGRAPHIE_FORM = "reports.hypertensionFlowEchoFormId";
 		
 	public final static String HEART_FAILURE_DDB = "reports.heartFailureDDBformId";
 	
 	public final static String EPILEPSY_DDB = "reports.epilepsyDDBformId";
 	
 	public final static String EPILEPSY_RENDEVOUS_VISIT_FORM="reports.epilepsyRendevousVisitForm";
 	
     public final static String PEDI_ALLERGY = "reports.Form_NewAlergypedi";
 	
 	public final static String ADULT_ALLERGY="reports.Form_NewAlergyadult";
 	
 	public final static String HIV_TRANSFER_FORM="reports.tranferinForm";
 	
 	public final static String PATH_RESULTS_FORM = "reports.pathResultsForm";
 	
 	public final static String PATH_SUBMISSION_FORM = "reports.pathSubmissionForm";
 	
 	public final static String POSTOPERATOIRE_CARDIAQUERDV = "reports.Form_PostoperatoireCardiaqueRDV";
 	
 	public final static String INSUFFISANCE_CARDIAQUE_RDV = "reports.Form_InsuffisanceCardiaqueRDV";
 	
     public final static String POSTOPERATOIRE_CARDIAQUE_HOSPITALISATIONS = "reports.Form_PostoperatoireCardiaqueHospitalisations";
 	
 	public final static String INSUFFISANCE_CARDIAQUE_HOSPITALISATIONS = "reports.Form_InsuffisanceCardiaqueHospitalisations";
 	
     public final static String HEARTFAILURE_FLOW_VISIT="reports.heartfailureFlowVisit";
 	
 	public final static String HEARTFAILURE_DDB = "reports.heartfailureDDBformId";
 	
 	public final static String CHEMOTHERAPY_TREATMENT_SUMMARY_FORM = "reports.chemoTreatmentSummaryForm";
 	
 	public final static String CHEMOTHERAPY_TREATMENT_SUMMARY_FORM_SHORT = "reports.chemoTreatmentSummaryFormShort";
 	
 	//Drug concepts
 	public final static String FUROSEMIDE= "reports.furosemide";
 	
 	public final static String ATENOLOL = "reports.atenolol";
 	
 	public final static String CARVEDILOL = "reports.carvedilol";
 	
 	public final static String ALDACTONE = "reports.aldactone";
 	
 	public final static String LISINOPRIL = "reports.lisinopril";
 	
 	public final static String CAPTOPRIL = "reports.captopril";
 	
 	public final static String WARFARIN = "reports.warfarin";
 	
 	public final static String PENICILLIN = "reports.penicillin";
 	
 	public final static String CTX = "reports.ctxTreatmentConcept";
 	
 	public final static String INSULIN = "reports.insulineDrugs";
 	
 	public final static String INSULIN_70_30 = "reports.insulin7030Concept";
 	
 	public final static String INSULIN_LENTE = "reports.insulinlenteConcept";
 	
 	public final static String INSULIN_RAPIDE = "reports.insulinrapideConcept";
 	
 	public final static String GLIBENCLAMIDE_DRUG="reports.glibenclamideConcept";
 	
 	public final static String COTRIMOXAZOLE_DRUG="reports.ctxTreatmentConcept";
 	
 	public final static String NEVIRAPINE_DRUG="report.nevirapine";
 	
 	public final static String SALBUTAMOL_DRUG="reports.salbutamolConcept";
 	
 	public final static String PREDNISOLONE_DRUG="reports.prednisoloneConcept";
 	
 	public final static String BECLOMETHASONE_DRUG="reports.beclomethasoneConcept";
 	
 	public final static String HYDROCHLOROTHIAZIDE_DRUG="reports.hydrochlorothiazide";
 	
 	public final static String RIFAMPICIN_DRUG="reports.rifampicin";
 	
 	public final static String ETHAMBUTOL_DRUG="reports.ethambutol";
 	
 	public final static String ISONIAZID_DRUG="reports.isoniazid";
 	
 	public final static String KALETRA_DRUG="reports.kaletra";
 	
 	public final static String NORMAL_SALINE = "reports.normalSaline";
 	
 	//Drug set concepts
 	public final static String ART_DRUGS_SET = "reports.allArtDrugsConceptSet";
 	
 	public final static String TB_TREATMENT_DRUGS = "reports.tbTreatmentConcept";
 	
 	public final static String ART_FIRST_LINE_DRUG_SET = "reports.allFirstLineArtDrugsConceptSet";
 	
 	public final static String ART_SECOND_LINE_DRUG_SET = "reports.allSecondLineArtDrugsConceptSet";
 
     public final static String TB_FIRST_LINE_DRUG_SET = "reports.allFirstLineTBDrugsConceptSet";
 	
 	public final static String TB_SECOND_LINE_DRUG_SET = "reports.allSecondLineTBDrugsConceptSet";
 	
 	public final static String DIABETES_TREATMENT_DRUG_SET= "reports.diabetesTreatmentDrugConceptSet";
 	
 	public final static String METFORMIN_DRUG="reports.metforminConcept";
 	
 	public final static String CHRONIC_RESPIRATORY_DISEASE_TREATMENT_DRUGS="reports.asthmaTreatmentConceptSet";
 	
 	public final static String HYPERTENSION_TREATMENT_DRUGS="reports.hypertensionTreatmentConceptSet";
 		
 	public final static String EPILEPSY_TREATMENT_DRUGS="reports.epilepsyTreatmentConceptSet";
 	
 	public final static String CARDIAC_TREATMENT_DRUGS="reports.cardiacTreatmentConceptSet";
 	
 	public final static String BACTRIM_CONCEPT = "reports.bactrimConcept";
 	
 	//Test concepts
 	public final static String TB_TEST_CONCEPT = "reports.tbTestConcept";
 	
 	public final static String CD4_TEST = "reports.cd4Concept";
 	
 	public final static String CD4_PERCENTAGE_TEST = "reports.cd4PercentageConcept";
 	
 	public final static String VIRAL_LOAD_TEST = "reports.viralLoadConcept";
 	
 	public final static String DBS_CONCEPT = "reports.dbsConcept";
 	
 	public final static String SERO_TEST = "reports.serotestConcept";
 	
 	public final static String DDB_ECHOCARDIOGRAPH_RESULT = "reports.ddb_echocardiograph_result";
 	
 	public final static String DDB_ECHOCARDIOGRAPH_COMMENT = "reports.ddb_echocardiograph_comment";
 	
 	public final static String SERUM_CREATININE = "reports.serumCreatinine";
 	
 	public final static String HIV_TEST = "reports.hivTestConcept";
 	
 	public final static String HEMOGLOBIN = "reports.hemoglobin";
 	
 	//Group constructs  
 	public final static String CHILD_SEROLOGY_CONSTRUCT = "reports.childSerologyConcept";
 
 
 	//Lab Panel Concepts 
 	public final static String CD4_PANEL_LAB_CONCEPT = "reports.cd4LabConcept";
 	
 	//Order types
 	public final static String LAB_ORDER_TYPE = "reports.labOrderType";
 	public final static String DRUG_ORDER_TYPE = "reports.drugOrderType";
 	
 	//Drug
 	public final static String NVP_Susp = "reports.NVPSuspDrug";
 	public final static String BACTRIM="reports.BactrimDrug";
 	
 	
 	
 	//-------------------------------------------------
 //  TRACNET REPORT INDICATORS
 //-------------------------------------------------	
 	//Programs
 	public final static String REASON_PATIENT_STARTED_ARVS_FOR_PROPHYLAXIS = "reports.reasonpatientstartedProphylalxisdrug";
 	public final static String EXPOSURE_TO_BLOOD_OR_BLOOD_PRODUCTS = "reports.exposedtobloodorbloodproduct";
 	public final static String SEXUAL_ASSAULT="reports.sexualAssault";
 	public final static String SEXUAL_CONTACT_WITH_HIV_POSITIVE_PARTNER="reports.sexualContactWithPospartner";
 	public final static int HIV_TEST_DONE=2169;
 	public final static String NEGATIVE_HIV_TEST_ANSWER = "reports.negativeHivTestConcept";
 	public final static String UNDETERMINATE_HIV_TEST_ANSWER = "reports.undeterminateHivTestConcept";
 	public final int METHOD_OF_FAMILY_PLANNING_ID=374;
 	public final static String PREGNANCY_STATUS="reports.pregnancy_status";
 	public final static String YES="reports.yesStatus";
 	public final static String NO="reports.noStatus";
 	public final static String PROGRAM_THAT_ORDERED_TEST="reports.programthatordered_test";
 	public final static String MATERNITY_WARD="maternity_ward";
 	public final static String BIRTH_LOCATION_TYPE="report.birthlocationtype";
 	public final static String HOSPITAL="report.hospital";
 	public final static String HOUSE="report.house";
 	public final static String HEALTH_CENTER="report.heatccenter";
 	public final static String INFANT_FEEDING_METHOD="report.feedmethod";
 	public final static String BREASTFEED_EXCL="report.breastfeed";
 	public final static String USING_FORMULA="report.usingformula";
 	public final static String TB_SCREENING_TEST="report.tbscreeningtest";
 	public final static String SOCIO_ECONOMIC_ASSISTANCE_RECOMENDED="reports.socioeconomicassistance";
 	public final static String NUTRITIONAL_AID="report.nutritionalaid";
 	public final static String TRASNFERED_OUT="report.patienttransferedOut";
 	public final static String TRASNFERED_IN="report.patienttransferedIn";
 	public final static String WHOSTAGE="reports.whostage";
 	public final static String WHOSTAGE4PED="reports.whostage4p";
 	public final static String WHOSTAGE3PED="reports.whostage3p";
 	public final static String WHOSTAGE2PED="reports.whostage2p";
 	public final static String WHOSTAGE1PED="reports.whostage1p";
 	public final static String WHOSTAGEUNKOWN="reports.whostageunkown";
 	public final static String WHOSTAGE4AD="reports.whostage4ad";
 	public final static String WHOSTAGE3AD="reports.whostage3ad";
 	public final static String WHOSTAGE2AD="reports.whostage2ad";
 	public final static String WHOSTAGE1AD="reports.whostage1ad";
 	public final static String CURRENT_OPORTUNISTIC_INFECTION="reports.stitest";
 	public final static String TUBERCULOSIS="reports.tuberculosis";
 	public final static String REASON_THERAPEUTIC_FAILED="reports.reasontherapeuticFailed";
 	public final static String POOR_ADHERENCE="reports.pooradherence";
 	public final static String NO_TEST="reports.notestDone";
 	public final static String RPR_TEST="reports.rprtest";
 	public final static String RPR_REACTIVE_ANWER="reports.reactiveanswer";
 	public final static String TESTING_STATUS_OF_PARTNER="report.partnerStatus";
 	public final static String GAVE_BIRTH="reports.pmtctGaveBirthStatus";
 	public final static String METHOD_OF_FAMILY_PLANNING="report.fplaning";
 	public final static String USING_CONDOMS="report.usingCondom";
 	public final static String USINF_INJECTABLE="report.injectable";
 	public final static String USING_ORALCONTRAC="report.orals";
 	public final static String REFERED_FOR_FP="report.referedforfp";
 	
 	public final static String NATURAL_FAMILY_PLANNING="report.naturalfamilyplanning";
 
 	public final int EIGHTANDHALF_MONTHS = 255; 
 	public final int NINETEEN_MONTHS = 570; 
 	public final static int ON_ART_TREATMENT_STATUS_ID = 1577;
 	public final static int TREATMENT_STATUS_ID = 1484;
 	public final static String DATA_ENTRY_DELAY = "reports.dataEntryDelay";
 	public final static String REPORT_CLASSIFICATION="reports.classifications";
 	public final static String DATA_ENTRY_DELAY_ACCEPTABLE = "reports.dataEntryDelayAccepted";
 }
