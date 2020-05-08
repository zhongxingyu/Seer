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
 
 package org.openmrs.module.mirebalaisreports;
 
 import org.openmrs.Concept;
 import org.openmrs.ConceptSource;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.Provider;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.EmrProperties;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * TODO: Consider whether these should all be moved into the mirebalaismetadata module / emrapi module
  */
 @Component("mirebalaisReportsProperties")
 public class MirebalaisReportsProperties extends EmrProperties {
 
     //***** REPORT DEFINITIONS *****
     public static final String FULL_DATA_EXPORT_REPORT_DEFINITION_UUID = "8c3752e2-20bb-11e3-b5bd-0bec7fb71852";
     public static final String DASHBOARD_DATA_EXPORT_REPORT_DEFINITION_UUID = "6d9b292a-2aad-11e3-a840-5b9e0b589afb";
     public static final String RADIOLOGY_DATA_EXPORT_REPORT_DEFINITION_UUID = "9e7dc296-2aad-11e3-a840-5b9e0b589afb";
     public static final String SURGERY_DATA_EXPORT_REPORT_DEFINITION_UUID = "a3c9a17a-2aad-11e3-a840-5b9e0b589afb";
     public static final String HOSPITALIZATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID = "bfa1b522-2aad-11e3-a840-5b9e0b589afb";
     public static final String CONSULTATIONS_DATA_EXPORT_REPORT_DEFINITION_UUID = "c427f48a-2aad-11e3-a840-5b9e0b589afb";
     public static final String ALL_PATIENTS_WITH_IDS_REPORT_DEFINITION_UUID = "d534683e-20bd-11e3-b5bd-0bec7fb71852";
     public static final String LQAS_DIAGNOSES_REPORT_DEFINITION_UUID = "f277f5b4-20bd-11e3-b5bd-0bec7fb71852";
     public static final String NON_CODED_DIAGNOSES_REPORT_DEFINITION_UUID = "3737be52-2265-11e3-818c-c7ea4184d59e";
     public static final String BASIC_STATISTICS_REPORT_DEFINITION_UUID = "5650dbc4-2266-11e3-818c-c7ea4184d59e";
     public static final String INPATIENT_STATS_DAILY_REPORT_DEFINITION_UUID = "f3bb8094-3738-11e3-b90a-a351ac6b1528";
    public static final String DAILY_REGISTRATIONS_REPORT_DEFINITION_UUID = "2e91bd04-4c7a-11e3-9325-f3ae8db9f6a7";
 
 	//***** LOCATIONS *****
 
 	public static final String OUTPATIENT_CLINIC_UUID = "199e7d87-92a0-4398-a0f8-11d012178164";
     public static final String WOMEN_CLINIC_UUID = "9b2066a2-7087-47f6-9b3a-b001037432a3";
     public static final String EMERGENCY_DEPARTMENT_UUID = "f3a5586e-f06c-4dfb-96b0-6f3451a35e90";
     public static final String EMERGENCY_RECEPTION_UUID = "afa09010-43b6-4f19-89e0-58d09941bcbd";
 
     public Location getOutpatientLocation() {
 		return getRequiredLocationByUuid(OUTPATIENT_CLINIC_UUID);
 	}
 
 	public Location getWomenLocation() {
 		return getRequiredLocationByUuid(WOMEN_CLINIC_UUID);
 	}
 
     public Location getEmergencyLocation() {
         return getRequiredLocationByUuid(EMERGENCY_DEPARTMENT_UUID);
     }
 
     public Location getEmergencyReceptionLocation() {
         return getRequiredLocationByUuid(EMERGENCY_RECEPTION_UUID);
     }
 
     private Location getRequiredLocationByUuid(String uuid) {
 		Location location = locationService.getLocationByUuid(uuid);
 		if (location == null) {
 			throw new IllegalStateException("Missing required location with uuid: " + uuid);
 		}
 		return location;
 	}
 
     public static List<Provider> getAllProviders(){
         List<Provider> providers = Context.getProviderService().getAllProviders(true);
         if (providers != null && providers.size() > 0){
             Collections.sort(providers, new Comparator<Provider>() {
                 @Override
                 public int compare(Provider p1, Provider p2) {
                     return p1.getName().compareTo(p2.getName());
                 }
             });
         }
         return providers;
     }
 	//***** IDENTIFIER TYPES *****
 
 	public static final String ZL_EMR_ID_UUID = "a541af1e-105c-40bf-b345-ba1fd6a59b85";
 
 	public PatientIdentifierType getZlEmrIdentifierType() {
 		return getRequiredIdentifierTypeByUuid(ZL_EMR_ID_UUID);
 	}
 
 	public static final String DOSSIER_NUMBER_UUID = "e66645eb-03a8-4991-b4ce-e87318e37566";
 
 	public PatientIdentifierType getDossierNumberIdentifierType() {
 		return getRequiredIdentifierTypeByUuid(DOSSIER_NUMBER_UUID);
 	}
 
 	public static final String HIV_EMR_ID_UUID = "139766e8-15f5-102d-96e4-000c29c2a5d7";
 
 	public PatientIdentifierType getHivEmrIdentifierType() {
 		return getRequiredIdentifierTypeByUuid(HIV_EMR_ID_UUID);
 	}
 
 	private PatientIdentifierType getRequiredIdentifierTypeByUuid(String uuid) {
 		PatientIdentifierType t = patientService.getPatientIdentifierTypeByUuid(uuid);
 		if (t == null) {
 			throw new IllegalStateException("Missing required patient identifier type with uuid: " + uuid);
 		}
 		return t;
 	}
 
 	//***** PERSON ATTRIBUTE TYPES
 
 	public static final String TEST_PERSON_ATTRIBUTE_UUID = "4f07985c-88a5-4abd-aa0c-f3ec8324d8e7";
     public static final String TELEPHONE_PERSON_ATTRIBUTE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7";
 
 	public PersonAttributeType getTestPatientPersonAttributeType() {
 		return getRequiredPersonAttributeTypeByUuid(TEST_PERSON_ATTRIBUTE_UUID);
 	}
 
     public PersonAttributeType getTelephoneNumberPersonAttributeType() {
         return getRequiredPersonAttributeTypeByUuid(TELEPHONE_PERSON_ATTRIBUTE_UUID);
     }
 
 	private PersonAttributeType getRequiredPersonAttributeTypeByUuid(String uuid) {
 		PersonAttributeType t = personService.getPersonAttributeTypeByUuid(uuid);
 		if (t == null) {
 			throw new IllegalStateException("Missing required person attribute type with uuid: " + uuid);
 		}
 		return t;
 	}
 
 	//***** ENCOUNTER TYPES *****
 
 	public static final String REGISTRATION_ENCOUNTER_TYPE_UUID = "873f968a-73a8-4f9c-ac78-9f4778b751b6";
     public static final String CHECK_IN_ENCOUNTER_TYPE_UUID = "55a0d3ea-a4d7-4e88-8f01-5aceb2d3c61b";
     public static final String PAYMENT_ENCOUNTER_TYPE_UUID = "f1c286d0-b83f-4cd4-8348-7ea3c28ead13";
     public static final String VITALS_ENCOUNTER_TYPE_UUID = "4fb47712-34a6-40d2-8ed3-e153abbd25b7";
     public static final String CONSULT_ENCOUNTER_TYPE_UUID = "92fd09b4-5335-4f7e-9f63-b2a663fd09a6";
     public static final String RADIOLOGY_ORDER_ENCOUNTER_TYPE_UUID = "1b3d1e13-f0b1-4b83-86ea-b1b1e2fb4efa";
     public static final String POST_OP_NOTE_ENCOUNTER_TYPE_UUID = "c4941dee-7a9b-4c1c-aa6f-8193e9e5e4e5";
 
 	public EncounterType getRegistrationEncounterType() {
 		return getRequiredEncounterTypeByUuid(REGISTRATION_ENCOUNTER_TYPE_UUID);
 	}
 
 	public EncounterType getCheckInEncounterType() {
 		return getRequiredEncounterTypeByUuid(CHECK_IN_ENCOUNTER_TYPE_UUID);
 	}
 
 	public EncounterType getPaymentEncounterType() {
 		return getRequiredEncounterTypeByUuid(PAYMENT_ENCOUNTER_TYPE_UUID);
 	}
 
 	public EncounterType getVitalsEncounterType() {
 		return getRequiredEncounterTypeByUuid(VITALS_ENCOUNTER_TYPE_UUID);
 	}
 
 	public EncounterType getConsultEncounterType() {
 		return getRequiredEncounterTypeByUuid(CONSULT_ENCOUNTER_TYPE_UUID);
 	}
 
 	public EncounterType getRadiologyOrderEncounterType() {
 		return getRequiredEncounterTypeByUuid(RADIOLOGY_ORDER_ENCOUNTER_TYPE_UUID);
 	}
 
     public EncounterType getPostOpNoteEncounterType() {
         return getRequiredEncounterTypeByUuid(POST_OP_NOTE_ENCOUNTER_TYPE_UUID);
     }
 
     /**
 	 * @return all encounter types <em>except for</em> Registration, Payment, and Check-In
 	 */
 	@Transactional(readOnly = true)
 	public List<EncounterType> getClinicalEncounterTypes() {
 		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
 		encounterTypes.remove(getRegistrationEncounterType());
 		encounterTypes.remove(getPaymentEncounterType());
 		encounterTypes.remove(getCheckInEncounterType());
 		return encounterTypes;
 	}
 
 	/**
 	 * @return all encounter types <em>except for</em> Registration
 	 */
 	@Transactional(readOnly = true)
 	public List<EncounterType> getVisitEncounterTypes() {
 		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes(false);
 		encounterTypes.remove(getRegistrationEncounterType());
 		return encounterTypes;
 	}
 
 	private EncounterType getRequiredEncounterTypeByUuid(String uuid) {
 		EncounterType encounterType = encounterService.getEncounterTypeByUuid(uuid);
 		if (encounterType == null) {
 			throw new IllegalStateException("Missing required encounter type with uuid: " + uuid);
 		}
 		return encounterType;
 	}
 
 	//***** CONCEPT SOURCES *****
 
 	public static final String ICD10_CONCEPT_SOURCE_UUID = "3f65bd34-26fe-102b-80cb-0017a47871b2";
 
 	public ConceptSource getIcd10ConceptSource() {
 		return conceptService.getConceptSourceByUuid(ICD10_CONCEPT_SOURCE_UUID);
 	}
 
 	public ConceptSource getPihConceptSource() {
 		return conceptService.getConceptSourceByName("PIH");
 	}
 
 	public ConceptSource getMirebalaisReportsConceptSource() {
 		return conceptService.getConceptSourceByName("org.openmrs.module.mirebalaisreports");
 	}
 
 	//***** CONCEPTS *****
 
 	public static final String AMOUNT_PATIENT_PAID_CONCEPT_UUID  = "5d1bc5de-6a35-4195-8631-7322941fe528";
 
 	public Concept getAmountPaidConcept() {
 		return getRequiredConceptByUuid(AMOUNT_PATIENT_PAID_CONCEPT_UUID);
 	}
 
 	public static final String WEIGHT_CONCEPT_UUID = "3ce93b62-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getWeightConcept() {
 		return getRequiredConceptByUuid(WEIGHT_CONCEPT_UUID);
 	}
 
 	public static final String HEIGHT_CONCEPT_UUID = "3ce93cf2-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getHeightConcept() {
 		return getRequiredConceptByUuid(HEIGHT_CONCEPT_UUID);
 	}
 
 	public static final String MUAC_CONCEPT_UUID = "e3e03a93-de7f-41ea-b8f2-60b220b970e9";
 
 	public Concept getMuacConcept() {
 		return getRequiredConceptByUuid(MUAC_CONCEPT_UUID);
 	}
 
 	public static final String TEMPERATURE_CONCEPT_UUID = "3ce939d2-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getTemperatureConcept() {
 		return getRequiredConceptByUuid(TEMPERATURE_CONCEPT_UUID);
 	}
 
 	public static final String PULSE_CONCEPT_UUID = "3ce93824-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getPulseConcept() {
 		return getRequiredConceptByUuid(PULSE_CONCEPT_UUID);
 	}
 
 	public static final String RESPIRATORY_RATE_CONCEPT_UUID = "3ceb11f8-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getRespiratoryRateConcept() {
 		return getRequiredConceptByUuid(RESPIRATORY_RATE_CONCEPT_UUID);
 	}
 
 	public static final String BLOOD_OXYGEN_SATURATION_CONCEPT_UUID = "3ce9401c-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getBloodOxygenSaturationConcept() {
 		return getRequiredConceptByUuid(BLOOD_OXYGEN_SATURATION_CONCEPT_UUID);
 	}
 
 	public static final String SYSTOLIC_BP_CONCEPT_UUID = "3ce934fa-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getSystolicBpConcept() {
 		return getRequiredConceptByUuid(SYSTOLIC_BP_CONCEPT_UUID);
 	}
 
 	public static final String DIASTOLIC_BP_CONCEPT_UUID = "3ce93694-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getDiastolicBpConcept() {
 		return getRequiredConceptByUuid(DIASTOLIC_BP_CONCEPT_UUID);
 	}
 
 	public static final String DIAGNOSIS_CODED_CONCEPT_UUID = "226ed7ad-b776-4b99-966d-fd818d3302c2";
 
 	public Concept getCodedDiagnosisConcept() {
 		return getRequiredConceptByUuid(DIAGNOSIS_CODED_CONCEPT_UUID);
 	}
 
 	public static final String DIAGNOSIS_NONCODED_CONCEPT_UUID = "970d41ce-5098-47a4-8872-4dd843c0df3f";
 
 	public Concept getNonCodedDiagnosisConcept() {
 		return getRequiredConceptByUuid(DIAGNOSIS_NONCODED_CONCEPT_UUID);
 	}
 
 	public static final String CLINICAL_IMPRESSIONS_CONCEPT_UUID = "3cd9d956-26fe-102b-80cb-0017a47871b2";
 
 	public Concept getClinicalImpressionsConcept() {
 		return getRequiredConceptByUuid(CLINICAL_IMPRESSIONS_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_WEEKLY_NOTIFIABLE_DISEASES_CONCEPT_UUID = "ddb35fb6-e69b-49cb-9540-ba11cf40ffd7";
 
 	public Concept getSetOfWeeklyNotifiableDiseases() {
 		return getRequiredConceptByUuid(SET_OF_WEEKLY_NOTIFIABLE_DISEASES_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_URGENT_DISEASES_CONCEPT_UUID = "0f8dc745-5f4d-494d-805b-6f8c8b5fe258";
 
 	public Concept getSetOfUrgentDiseases() {
 		return getRequiredConceptByUuid(SET_OF_URGENT_DISEASES_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_WOMENS_HEALTH_DIAGNOSES_CONCEPT_UUID = "27b6675d-02ea-4331-a5fc-9a8224f90660";
 
 	public Concept getSetOfWomensHealthDiagnoses() {
 		return getRequiredConceptByUuid(SET_OF_WOMENS_HEALTH_DIAGNOSES_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_PSYCHOLOGICAL_DIAGNOSES_CONCEPT_UUID = "3b85c049-1e2d-4f58-bad4-bf3bc98ed098";
 
 	public Concept getSetOfPsychologicalDiagnoses() {
 		return getRequiredConceptByUuid(SET_OF_PSYCHOLOGICAL_DIAGNOSES_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_PEDIATRIC_DIAGNOSES_CONCEPT_UUID = "231ac3ac-2ad4-4c41-9989-7e6b85393b51";
 
 	public Concept getSetOfPediatricDiagnoses() {
 		return getRequiredConceptByUuid(SET_OF_PEDIATRIC_DIAGNOSES_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_OUTPATIENT_DIAGNOSES_CONCEPT_UUID = "11c8b2ab-2d4a-4d3e-8733-e10e5a3f1404";
 
 	public Concept getSetOfOutpatientDiagnoses() {
 		return getRequiredConceptByUuid(SET_OF_OUTPATIENT_DIAGNOSES_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_NCD_DIAGNOSES_CONCEPT_UUID = "6581641f-ee7e-4a8a-b271-2148e6ffec77";
 
 	public Concept getSetOfNcdDiagnoses() {
 		return getRequiredConceptByUuid(SET_OF_NCD_DIAGNOSES_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_NON_DIAGNOSIS_CONCEPT_UUID = "a2d2124b-fc2e-4aa2-ac87-792d4205dd8d";
 
 	public Concept getSetOfNonDiagnoses() {
 		return getRequiredConceptByUuid(SET_OF_NON_DIAGNOSIS_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_EMERGENCY_DIAGNOSIS_CONCEPT_UUID = "cfe2f068-0dd1-4522-80f5-c71a5b5f2c8b";
 
 	public Concept getSetOfEmergencyDiagnoses() {
 		return getRequiredConceptByUuid(SET_OF_EMERGENCY_DIAGNOSIS_CONCEPT_UUID);
 	}
 
 	public static final String SET_OF_AGE_RESTRICTED_DISEASES_CONCEPT_UUID = "2231e6b8-6259-426d-a9b2-d3cb8fbbd6a3";
 
 	public Concept getSetOfAgeRestrictedDiagnoses() {
 		return getRequiredConceptByUuid(SET_OF_AGE_RESTRICTED_DISEASES_CONCEPT_UUID);
 	}
 
     public static final String RETURN_VISIT_DATE_CONCEPT_UUID = "3ce94df0-26fe-102b-80cb-0017a47871b2";
 
     public Concept getReturnVisitDate() {
         return getRequiredConceptByUuid(RETURN_VISIT_DATE_CONCEPT_UUID);
     }
 
 	private Concept getRequiredConceptByUuid(String uuid) {
 		Concept c = conceptService.getConceptByUuid(uuid);
 		if (c == null) {
 			throw new IllegalStateException("Missing required concept with uuid: " + uuid);
 		}
 		return c;
 	}
 
 }
