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
 
 package org.openmrs.module.mirebalaisreports.definitions;
 
 import org.openmrs.Concept;
 import org.openmrs.Location;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.module.dispensing.DispensingProperties;
 import org.openmrs.module.mirebalaisreports.MirebalaisReportsProperties;
 import org.openmrs.module.mirebalaisreports.MirebalaisReportsUtil;
 import org.openmrs.module.mirebalaisreports.cohort.definition.VisitCohortDefinition;
 import org.openmrs.module.mirebalaisreports.library.EncounterDataLibrary;
 import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
 import org.openmrs.module.reporting.common.MessageUtil;
 import org.openmrs.module.reporting.data.converter.DateConverter;
 import org.openmrs.module.reporting.data.converter.ObjectFormatter;
 import org.openmrs.module.reporting.data.converter.ObsValueTextAsCodedConverter;
 import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
 import org.openmrs.module.reporting.data.encounter.definition.EncounterDatetimeDataDefinition;
 import org.openmrs.module.reporting.data.encounter.definition.EncounterLocationDataDefinition;
 import org.openmrs.module.reporting.data.encounter.definition.EncounterProviderDataDefinition;
 import org.openmrs.module.reporting.data.encounter.definition.ObsForEncounterDataDefinition;
 import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
 import org.openmrs.module.reporting.data.obs.definition.GroupMemberObsDataDefinition;
 import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
 import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
 import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
 import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
 import org.openmrs.module.reporting.dataset.definition.ObsDataSetDefinition;
 import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
 import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
 import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
 import org.openmrs.module.reporting.evaluation.parameter.Mapped;
 import org.openmrs.module.reporting.evaluation.parameter.Parameter;
 import org.openmrs.module.reporting.query.encounter.definition.BasicEncounterQuery;
 import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
 import org.openmrs.module.reporting.query.obs.definition.BasicObsQuery;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.renderer.RenderingMode;
 import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Responsible for defining the full data export report
  * @see FullDataExportBuilder
  */
 public class FullDataExportReportManager extends BaseMirebalaisReportManager {
 
 	//***** CONSTANTS *****
 
 	public static final String SQL_DIR = "org/openmrs/module/mirebalaisreports/sql/fullDataExport/";
 	public static final String TEMPLATE_DIR = "org/openmrs/module/mirebalaisreports/reportTemplates/";
 
     @Autowired
     private AllDefinitionLibraries libraries;
 
     @Autowired
     private DispensingProperties dispensingProperties;
 
     private String uuid;
     private String messageCodePrefix;
     private List<String> dataSets;
 
     public FullDataExportReportManager(String uuid, String messageCodePrefix, List<String> dataSets) {
         this.uuid = uuid;
         this.messageCodePrefix = messageCodePrefix;
         this.dataSets = dataSets;
     }
 
     @Override
     public String getUuid() {
         return uuid;
     }
 
     @Override
     public String getVersion() {
        return "1.2-SNAPSHOT"; // last change: setting design property on CSV exports to blacklist non-latin-1 characters
     }
 
 	//***** INSTANCE METHODS
 
 	@Override
 	protected String getMessageCodePrefix() {
 		return messageCodePrefix;
 	}
 
 	@Override
 	public List<Parameter> getParameters() {
 		List<Parameter> l = new ArrayList<Parameter>();
 		l.add(getStartDateParameter());
 		l.add(getEndDateParameter());
 		return l;
 	}
 
 	@Override
 	public List<RenderingMode> getRenderingModes() {
 		List<RenderingMode> l = new ArrayList<RenderingMode>();
 		{
 			RenderingMode mode = new RenderingMode();
 			mode.setLabel(translate("output.excel"));
 			mode.setRenderer(new XlsReportRenderer());
 			mode.setSortWeight(50);
 			mode.setArgument("");
 			l.add(mode);
 		}
 		return l;
 	}
 
 	@Override
 	public String getRequiredPrivilege() {
 		return "Report: mirebalaisreports.fulldataexport";
 	}
 
 	@Override
 	public ReportDefinition constructReportDefinition() {
 
 		log.info("Constructing " + getName());
         ReportDefinition rd = new ReportDefinition();
 		rd.setName(getMessageCodePrefix() + "name");
 		rd.setDescription(getMessageCodePrefix() + "description");
 		rd.setParameters(getParameters());
         rd.setUuid(getUuid());
 
         {
             CompositionCohortDefinition baseCohortDefinition = new CompositionCohortDefinition();
             baseCohortDefinition.addParameter(getStartDateParameter());
             baseCohortDefinition.addParameter(getEndDateParameter());
 
             // TODO--this is no longer accurate, remove?
             // --Only show patients with a visit or registration encounter during the period
             // INNER JOIN (
             // SELECT patient_id, date_started FROM visit WHERE voided = 0 AND date_started BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)
             // UNION
             // SELECT patient_id, encounter_datetime FROM encounter WHERE voided = 0 AND encounter_type = 6 AND encounter_datetime BETWEEN :startDate AND ADDDATE(:endDate, INTERVAL 1 DAY)
             // ) list ON p.patient_id = list.patient_id
 
             VisitCohortDefinition visitDuringPeriod = new VisitCohortDefinition();
             visitDuringPeriod.addParameter(new Parameter("activeOnOrAfter", "", Date.class));
             visitDuringPeriod.addParameter(new Parameter("activeOnOrBefore", "", Date.class));
             baseCohortDefinition.addSearch("visitDuringPeriod", this.<CohortDefinition>map(visitDuringPeriod, "activeOnOrAfter=${startDate},activeOnOrBefore=${endDate}"));
 
             EncounterCohortDefinition registrationEncounterDuringPeriod = new EncounterCohortDefinition();
             registrationEncounterDuringPeriod.addEncounterType(mirebalaisReportsProperties.getRegistrationEncounterType());
             registrationEncounterDuringPeriod.addParameter(new Parameter("onOrAfter", "", Date.class));
             registrationEncounterDuringPeriod.addParameter(new Parameter("onOrBefore", "", Date.class));
             baseCohortDefinition.addSearch("registrationEncounterDuringPeriod", this.<CohortDefinition>map(registrationEncounterDuringPeriod, "onOrAfter=${startDate},onOrBefore=${endDate}"));
 
             // --Exclude test patients
             // AND p.patient_id NOT IN (SELECT person_id FROM person_attribute WHERE value = 'true' AND person_attribute_type_id = 11 AND voided = 0)
             PersonAttributeCohortDefinition testPatient = new PersonAttributeCohortDefinition();
             testPatient.setAttributeType(emrApiProperties.getTestPatientPersonAttributeType());
             testPatient.addValue("true");
             baseCohortDefinition.addSearch("testPatient", Mapped.map(testPatient, ""));
 
             baseCohortDefinition.setCompositionString("(visitDuringPeriod OR registrationEncounterDuringPeriod) AND NOT testPatient");
             rd.setBaseCohortDefinition(this.<CohortDefinition>map(baseCohortDefinition, "startDate=${startDate},endDate=${endDate}"));
         }
 
         for (String key : dataSets) {
 
 			log.debug("Adding dataSet: " + key);
 
             DataSetDefinition dsd;
             if ("patients".equals(key)) {
                 dsd = constructPatientsDataSetDefinition();
             } else if ("encounters".equals(key)) {
                 dsd = constructEncountersDataSetDefinition();
             } else if ("dispensing".equals(key)) {
                 dsd = constructDispensingDataSetDefinition();
             } else if ("consultations-new".equals(key)) {
                 dsd = constructConsultationsDataSetDefinition();
                 key = "consultations";
             } else {
                 dsd = constructSqlDataSetDefinition(key);
             }
             dsd.setName(MessageUtil.translate("mirebalaisreports.fulldataexport." + key + ".name"));
             dsd.setDescription(MessageUtil.translate("mirebalaisreports.fulldataexport." + key + ".description"));
             dsd.addParameter(getStartDateParameter());
             dsd.addParameter(getEndDateParameter());
 
 			Map<String, Object> mappings =  new HashMap<String, Object>();
 			mappings.put("startDate","${startDate}");
 			mappings.put("endDate", "${endDate}");
 
 			rd.addDataSetDefinition(key, dsd, mappings);
 		}
 
 		return rd;
 	}
 
     private DataSetDefinition constructEncountersDataSetDefinition() {
         EncounterDataSetDefinition dsd = new EncounterDataSetDefinition();
         dsd.addParameter(getStartDateParameter());
         dsd.addParameter(getEndDateParameter());
 
         BasicEncounterQuery query = new BasicEncounterQuery();
         query.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
         query.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
         dsd.addRowFilter(query, "onOrAfter=${startDate},onOrBefore=${endDate}");
 
         dsd.addColumn("zlEmrId", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "preferredZlEmrId"), null);
         dsd.addColumn("patientId", libraries.getDefinition(EncounterDataDefinition.class, BuiltInEncounterDataLibrary.PREFIX + "patientId"), null);
         dsd.addColumn("age", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "ageAtEncounter"), null);
         dsd.addColumn("gender", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "gender"), null);
         dsd.addColumn("visitId", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "visit.id"), null);
         dsd.addColumn("visitStart", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "visit.startDatetime"), null);
         dsd.addColumn("visitStop", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "visit.stopDatetime"), null);
         dsd.addColumn("encounterId", libraries.getDefinition(EncounterDataDefinition.class, BuiltInEncounterDataLibrary.PREFIX + "encounterId"), null);
         dsd.addColumn("encounterType", libraries.getDefinition(EncounterDataDefinition.class, BuiltInEncounterDataLibrary.PREFIX + "encounterType.name"), null);
         dsd.addColumn("location", libraries.getDefinition(EncounterDataDefinition.class, BuiltInEncounterDataLibrary.PREFIX + "location.name"), null);
         dsd.addColumn("encounterDatetime", libraries.getDefinition(EncounterDataDefinition.class, BuiltInEncounterDataLibrary.PREFIX + "encounterDatetime"), null);
         dsd.addColumn("disposition", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "disposition"), null);
         dsd.addColumn("enteredBy", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "creator"), null);
         dsd.addColumn("allProviders", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "allProviders.name"), null);
         dsd.addColumn("numberOfProviders", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "numberOfProviders"), null);
         dsd.addColumn("administrativeClerk", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "administrativeClerk.name"), null);
         dsd.addColumn("nurse", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "nurse.name"), null);
         dsd.addColumn("consultingClinician", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "consultingClinician.name"), null);
         dsd.addColumn("dispenser", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "dispenser.name"), null);
         dsd.addColumn("radiologyTech", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "radiologyTechnician.name"), null);
         dsd.addColumn("orderingProvider", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "orderingProvider.name"), null);
         dsd.addColumn("principalResultsInterpreter", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "principalResultsInterpreter.name"), null);
         dsd.addColumn("attendingSurgeon", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "attendingSurgeon.name"), null);
         dsd.addColumn("assistingSurgeon", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "assistingSurgeon.name"), null);
         dsd.addColumn("anesthesiologist", libraries.getDefinition(EncounterDataDefinition.class, EncounterDataLibrary.PREFIX + "anesthesiologist.name"), null);
 
         return dsd;
     }
 
     private DataSetDefinition constructDispensingDataSetDefinition() {
 
         ObsDataSetDefinition dsd = new ObsDataSetDefinition();
         dsd.addParameter(getStartDateParameter());
         dsd.addParameter(getEndDateParameter());
 
         BasicObsQuery query = new BasicObsQuery();
         query.addParameter(new Parameter("onOrAfter", "On or after", Date.class));
         query.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
         query.addConcept(dispensingProperties.getDispensingConstructConcept());
         dsd.addRowFilter(query, "onOrAfter=${startDate},onOrBefore=${endDate}");
 
         dsd.addColumn("medication", constructGroupMemberObsDataDefinition(dispensingProperties.getMedicationConcept())
                 , "", new ObjectFormatter());
         dsd.addColumn("dosage", constructGroupMemberObsDataDefinition(dispensingProperties.getDosageConcept())
                 , "", new ObjectFormatter());
         dsd.addColumn("dosageUnits", constructGroupMemberObsDataDefinition(dispensingProperties.getDosageUnitsConcept())
                 , "", new ObjectFormatter());
         dsd.addColumn("frequency", constructGroupMemberObsDataDefinition(dispensingProperties.getMedicationFrequencyConcept())
                 , "", new ObjectFormatter());
         dsd.addColumn("duration", constructGroupMemberObsDataDefinition(dispensingProperties.getMedicationDurationConcept())
                 , "", new ObjectFormatter());
         dsd.addColumn("durationUnits", constructGroupMemberObsDataDefinition(dispensingProperties.getMedicationDurationUnitsConcept())
                 , "", new ObjectFormatter());
         dsd.addColumn("amount", constructGroupMemberObsDataDefinition(dispensingProperties.getDispensedAmountConcept()),
                 "", new ObjectFormatter());
         dsd.addColumn("instructions", constructGroupMemberObsDataDefinition(dispensingProperties.getAdministrationInstructions()),
                 "", new ObjectFormatter());
         dsd.addColumn("patientIdentifier", constructPatientIdentifierDataDefinition(emrApiProperties.getPrimaryIdentifierType()),
                 "", new ObjectFormatter());
         dsd.addColumn("dispensedLocation", new EncounterLocationDataDefinition(), "", new ObjectFormatter());
         dsd.addColumn("dispensedDatetime", new EncounterDatetimeDataDefinition(), "", new DateConverter(MirebalaisReportsProperties.DATETIME_FORMAT));
 
         EncounterProviderDataDefinition dispensedByDef = new EncounterProviderDataDefinition();
         dispensedByDef.setEncounterRole(mirebalaisReportsProperties.getDispenserEncounterRole());
         dsd.addColumn("dispensedBy", dispensedByDef, "", new ObjectFormatter());
 
         EncounterProviderDataDefinition prescribedByDef = new EncounterProviderDataDefinition();
         prescribedByDef.setEncounterRole(mirebalaisReportsProperties.getPrescribedByEncounterRole());
         dsd.addColumn("prescribedBy", prescribedByDef, "", new ObjectFormatter());
 
         ObsForEncounterDataDefinition typeOfPrescriptionDef = new ObsForEncounterDataDefinition();
         typeOfPrescriptionDef.setQuestion(mirebalaisReportsProperties.getTimingOfPrescriptionConcept());
         dsd.addColumn("typeOfPrescription", typeOfPrescriptionDef, "", new ObjectFormatter());
 
         ObsForEncounterDataDefinition locationOfPrescriptionDef = new ObsForEncounterDataDefinition();
         locationOfPrescriptionDef.setQuestion(mirebalaisReportsProperties.getDischargeLocationConcept());
         dsd.addColumn("locationOfPrescription", locationOfPrescriptionDef, "",
                 new ObsValueTextAsCodedConverter<Location>(Location.class), new ObjectFormatter());
 
         return dsd;
     }
 
     private GroupMemberObsDataDefinition constructGroupMemberObsDataDefinition(Concept concept) {
         GroupMemberObsDataDefinition groupMemberObsDataDefinition = new GroupMemberObsDataDefinition();
         groupMemberObsDataDefinition.setQuestion(concept);
         return groupMemberObsDataDefinition;
     }
 
     private DataSetDefinition constructConsultationsDataSetDefinition() {
         EncounterDataSetDefinition dsd = new EncounterDataSetDefinition();
 
         dsd.addParameter(getStartDateParameter());
         dsd.addParameter(getEndDateParameter());
 
         String sql = "select encounter_id " +
                 "from encounter " +
                 "where voided = 0 " +
                 "and encounter_type = :consultation " +
                 "and encounter_datetime >= :startDate AND encounter_datetime < :endDate ";
         sql = sql.replaceAll(":consultation", mirebalaisReportsProperties.getConsultEncounterType().getId().toString());
 
         SqlEncounterQuery encounterQuery = new SqlEncounterQuery(sql);
         encounterQuery.addParameter(getStartDateParameter());
         encounterQuery.addParameter(getEndDateParameter());
         dsd.addRowFilter(encounterQuery, "startDate=${startDate},endDate=${endDate + 1d}");
 
         dsd.addColumn("patient_id", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.patientId"), "");
         dsd.addColumn("zlemr", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.preferredZlEmrId"), "");
         dsd.addColumn("loc_registered", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.mostRecentZlEmrIdLocation"), "");
         dsd.addColumn("unknown_patient", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.unknownPatient"), "");
         dsd.addColumn("gender", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.gender"), "");
         dsd.addColumn("age_at_enc", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.ageAtEncounter"), "");
         dsd.addColumn("department", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.preferredAddress.department"), "");
         dsd.addColumn("commune", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.preferredAddress.commune"), "");
         dsd.addColumn("section", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.preferredAddress.section"), "");
         dsd.addColumn("locality", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.preferredAddress.locality"), "");
         dsd.addColumn("street_landmark", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.preferredAddress.streetLandmark"), "");
         dsd.addColumn("encounter_id", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.encounterId"), "");
         dsd.addColumn("encounter_datetime", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.encounterDatetime"), "");
         dsd.addColumn("encounter_location", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.location.name"), "");
         dsd.addColumn("provider", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.creator"), "");
         dsd.addColumn("num_coded", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.codedDiagnosis"), "");
         dsd.addColumn("num_non_coded", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.nonCodedDiagnosis"), "");
         dsd.addColumn("disposition", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.disposition"), "");
         dsd.addColumn("transfer_out_location", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.transferOutLocation"), "");
         dsd.addColumn("trauma", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.traumaOccurrence"), "");
         dsd.addColumn("trauma_type", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.traumaType"), "");
         dsd.addColumn("appointment", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.returnVisitDate"), "");
         dsd.addColumn("comments", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.consultationComments"), "");
         dsd.addColumn("death_date", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.vitalStatus.deathDate"), "");
         dsd.addColumn("dispo_encounter", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.associatedAdtEncounter.encounterType"), "");
         dsd.addColumn("dispo_location", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.associatedAdtEncounter.location"), "");
         dsd.addColumn("date_created", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.dateCreated"), "");
         dsd.addColumn("retrospective", libraries.getDefinition(EncounterDataDefinition.class, "mirebalais.encounterDataCalculation.retrospective"), "");
 
         return dsd;
     }
     
     private PatientIdentifierDataDefinition constructPatientIdentifierDataDefinition(PatientIdentifierType type) {
         PatientIdentifierDataDefinition patientIdentifierDataDefinition = new PatientIdentifierDataDefinition();
         patientIdentifierDataDefinition.addType(type);
         patientIdentifierDataDefinition.setIncludeFirstNonNullOnly(true);
         return patientIdentifierDataDefinition;
     }
 
     private SqlDataSetDefinition constructSqlDataSetDefinition(String key) {
         SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();
 
         String sql = MirebalaisReportsUtil.getStringFromResource(SQL_DIR + key + ".sql");
         sql = applyMetadataReplacements(sql);
         sqlDsd.setSqlQuery(sql);
         return sqlDsd;
     }
 
     private DataSetDefinition constructPatientsDataSetDefinition() {
         PatientDataSetDefinition dsd = new PatientDataSetDefinition();
 
         dsd.addParameter(getStartDateParameter());
         dsd.addParameter(getEndDateParameter());
 
         dsd.addColumn("patient_id", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.patientId"), "");
 
         // Most recent ZL EMR ID
         // INNER JOIN (SELECT patient_id, identifier, location_id FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 ORDER BY date_created DESC) zl ON p.patient_id = zl.patient_id
         dsd.addColumn("zlemr", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentZlEmrId.identifier"), "");
 
         // ZL EMR ID location
         // INNER JOIN location zl_loc ON zl.location_id = zl_loc.location_id
         dsd.addColumn("loc_registered", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentZlEmrId.location"), "");
 
         // un.value unknown_patient
         // Unknown patient
         // LEFT OUTER JOIN person_attribute un ON p.patient_id = un.person_id AND un.person_attribute_type_id = 10 AND un.voided = 0
         dsd.addColumn("unknown_patient", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.unknownPatient.value"), "");
 
         // --Number of ZL EMRs assigned to this patient
         // INNER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 5 AND voided = 0 GROUP BY patient_id) numzlemr ON p.patient_id = numzlemr.patient_id
         // TODO difference: returns 0 where the existing behavior is to leave those blank
         dsd.addColumn("numzlemr", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.numberOfZlEmrIds"), "");
 
         // --Most recent Numero Dossier
         // LEFT OUTER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 ORDER BY date_created DESC) nd ON p.patient_id = nd.patient_id
         dsd.addColumn("numero_dossier", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentDossierNumber.identifier"), "");
 
         // --Number of Numero Dossiers
         // LEFT OUTER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 GROUP BY patient_id) numnd ON p.patient_id = numnd.patient_id
         // TODO difference: returns 0 where the existing behavior is to leave those blank
         dsd.addColumn("num_nd", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.numberOfDossierNumbers"), "");
 
         // --HIV EMR ID
         // LEFT OUTER JOIN (SELECT patient_id, identifier FROM patient_identifier WHERE identifier_type = 4 AND voided = 0 ORDER BY date_created DESC) hivemr ON p.patient_id = hivemr.patient_id
         dsd.addColumn("hivemr", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.mostRecentHivEmrId.identifier"), "");
 
         // --Number of HIV EMR IDs
         // LEFT OUTER JOIN (SELECT patient_id, COUNT(patient_identifier_id) num FROM patient_identifier WHERE identifier_type = 3 AND voided = 0 GROUP BY patient_id) numhiv ON p.patient_id = numhiv.patient_id
         // TODO difference: returns 0 where the existing behavior is to leave those blank
         dsd.addColumn("num_hiv", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.numberOfHivEmrIds"), "");
 
         // pr.birthdate
         dsd.addColumn("birthdate", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.birthdate.ymd"), "");
 
         // pr.birthdate_estimated
         dsd.addColumn("birthdate_estimated", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.birthdate.estimated"), "");
 
         // pr.gender
         dsd.addColumn("gender", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.gender"), "");
 
         // pr.dead
         dsd.addColumn("dead", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.vitalStatus.dead"), "");
 
         // pr.death_date
         dsd.addColumn("death_date", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.vitalStatus.deathDate"), "");
 
         // --Most recent address
         // LEFT OUTER JOIN (SELECT * FROM person_address WHERE voided = 0 ORDER BY date_created DESC) pa ON p.patient_id = pa.person_id
         // TODO: implemented this with preferred address rather than most recent one
 
         // pa.state_province department
         dsd.addColumn("department", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.preferredAddress.department"), "");
 
         // pa.city_village commune
         dsd.addColumn("commune", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.preferredAddress.commune"), "");
 
         // pa.address3 section
         dsd.addColumn("section", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.preferredAddress.section"), "");
 
         // pa.address1 locality
         dsd.addColumn("locality", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.preferredAddress.locality"), "");
 
         // pa.address2 street_landmark
         dsd.addColumn("street_landmark", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.preferredAddress.streetLandmark"), "");
 
         // reg.encounter_datetime date_registered
         // --First registration encounter
         // LEFT OUTER JOIN (SELECT patient_id, MIN(encounter_id) encounter_id FROM encounter WHERE encounter_type = 6 AND voided = 0 GROUP BY patient_id) first_reg ON p.patient_id = first_reg.patient_id
         // LEFT OUTER JOIN encounter reg ON first_reg.encounter_id = reg.encounter_id
 
         dsd.addColumn("date_registered", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.registration.encounterDatetime"), "");
 
         // regl.name reg_location
         // --Location registered
         // LEFT OUTER JOIN location regl ON reg.location_id = regl.location_id
         dsd.addColumn("reg_location", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.registration.location"), "");
 
         // CONCAT(regn.given_name, ' ', regn.family_name) reg_by
         // --User who registered the patient
         // LEFT OUTER JOIN users u ON reg.creator = u.user_id
         // LEFT OUTER JOIN person_name regn ON u.person_id = regn.person_id
         dsd.addColumn("reg_by", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.registration.creator.name"), "");
 
         // ROUND(DATEDIFF(reg.encounter_datetime, pr.birthdate)/365.25, 1) age_at_reg
         dsd.addColumn("age_at_reg", libraries.getDefinition(PatientDataDefinition.class, "mirebalais.patientDataCalculation.registration.age"), "");
 
         return dsd;
     }
 
     private Map<String, Object> mappings(String startDatePropertyName, String endDatePropertyName) {
         Map<String, Object> mappings = new HashMap<String, Object>();
 
         if (startDatePropertyName != null) {
             mappings.put(startDatePropertyName, "${startDate}");
         }
         if (endDatePropertyName != null) {
             mappings.put(endDatePropertyName, "${endDate}");
         }
         return mappings;
     }
 
     @Override
     public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
         return Arrays.asList(xlsReportDesign(reportDefinition, null, null), csvReportDesign(reportDefinition));
     }
 
 }
