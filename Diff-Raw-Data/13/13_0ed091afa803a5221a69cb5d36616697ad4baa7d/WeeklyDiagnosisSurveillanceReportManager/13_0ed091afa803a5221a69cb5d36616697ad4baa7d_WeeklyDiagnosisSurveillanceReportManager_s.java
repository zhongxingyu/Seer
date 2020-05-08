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
 
 import org.apache.commons.io.IOUtils;
 import org.openmrs.Concept;
 import org.openmrs.ConceptSource;
 import org.openmrs.api.ConceptService;
 import org.openmrs.module.emr.concept.EmrConceptService;
 import org.openmrs.module.emr.reporting.library.BasicCohortDefinitionLibrary;
 import org.openmrs.module.emr.reporting.library.BasicDimensionLibrary;
 import org.openmrs.module.emr.reporting.library.BasicIndicatorLibrary;
 import org.openmrs.module.mirebalaisreports.MirebalaisProperties;
 import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
 import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.evaluation.parameter.Mapped;
 import org.openmrs.module.reporting.evaluation.parameter.Parameter;
 import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
 import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
 import org.openmrs.module.reporting.indicator.CohortIndicator;
 import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
 import org.openmrs.module.reporting.indicator.dimension.service.DimensionService;
 import org.openmrs.module.reporting.indicator.service.IndicatorService;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  */
 @Component
 public class WeeklyDiagnosisSurveillanceReportManager {
 
     @Autowired
     CohortDefinitionService cohortDefinitionService;
 
     @Autowired
     DimensionService dimensionService;
 
     @Autowired
     IndicatorService indicatorService;
 
     @Autowired
     ConceptService conceptService;
 
     @Autowired
     EmrConceptService emrConceptService;
 
     @Autowired
     MirebalaisProperties mirebalaisProperties;
 
     public ReportDefinition buildReportDefinition() {
         CohortIndicatorDataSetDefinition dsd = buildDataSetDefinition();
 
         ReportDefinition reportDefinition = new ReportDefinition();
         reportDefinition.addParameter(new Parameter("startOfWeek", "Start of Week", Date.class));
         reportDefinition.addDataSetDefinition("indicators", map(dsd, "startOfWeek=${startOfWeek}"));
 
         return reportDefinition;
     }
 
     public CohortIndicatorDataSetDefinition buildDataSetDefinition() {
         CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
 
         dsd.addParameter(new Parameter("startOfWeek", "Start of Week", Date.class));
 
         dsd.addDimension("age",
                 map((CohortDefinitionDimension) dimensionService.getDefinitionByUuid(BasicDimensionLibrary.PREFIX + "age two levels (cutoff in years)"),
                         "date", "${startOfWeek}", "cutoff", 5));
         dsd.addDimension("gender",
                 map((CohortDefinitionDimension) dimensionService.getDefinitionByUuid(BasicDimensionLibrary.PREFIX + "gender"), ""));
 
         CohortIndicator specificCodedDiagnosesIndicator = (CohortIndicator) indicatorService.getDefinitionByUuid(BasicIndicatorLibrary.PREFIX + "specific coded diagnoses during period");
         if (specificCodedDiagnosesIndicator == null) {
             throw new IllegalStateException("Cannot find indicator for specific coded diagnoses");
         }
 
         ConceptSource mirebalaisReports = mirebalaisProperties.getMirebalaisReportsConceptSource();
 
         Set<Concept> alreadyReportedConcepts = new HashSet<Concept>();
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "hemorrhagicFever", "Syndrome de fièvre hémorragique aiguë", mirebalaisReports, "hemorrFever");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "bacterialMeningitis", "Cas suspect de méningite bactérienne", mirebalaisReports, "bactMeningitis");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "diphtheria", "Cas suspect de diphtérie", mirebalaisReports, "diphtheria");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "acuteFlassicParalysis", "Cas suspect de paralysie flasque aiguë", mirebalaisReports, "flassicParalysis");
             addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "measles", "Cas suspect de rougeole", mirebalaisReports, "measles");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "rabies", "Morsure par animal suspecté de rage", mirebalaisReports, "rabies");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "suspectedMalaria", "Cas suspect de paludisme (malaria)", mirebalaisReports, "suspMalaria");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "confirmedMalaria", "Cas confirmé de paludisme (malaria)", mirebalaisReports, "confMalaria");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "dengue", "Cas suspect de dengue (et la dengue hémorragique)", mirebalaisReports, "dengue");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "fever", "Fièvre d’origine inconnue", mirebalaisReports, "fever");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "jaundiceFever", "Syndrome ictérique fébrile", mirebalaisReports, "jaundiceFever");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "diarrhea", "Diarrhée aiguë non-sanglante", mirebalaisReports, "diarrhea");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "bloodyDiarrhea", "Diarrhée aiguë sanglante", mirebalaisReports, "bloodyDiarrhea");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "typhoid", "Cas suspect de typhoïde", mirebalaisReports, "typhoid");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "pertussis", "Cas suspect de coqueluche", mirebalaisReports, "pertussis");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "respiratoryInfection", "Infection respiratoire aiguë", mirebalaisReports, "respiratoryInfection");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "tuberculosis", "Cas suspect de tuberculose", mirebalaisReports, "tuberculosis");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "tetanus", "Cas suspect de tétanos", mirebalaisReports, "tetanus");
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "anthrax", "Cas suspect de charbon cutané", mirebalaisReports, "anthrax");
         // TODO: Troisième trimestre de grossesse sans suivi
         addDiseaseColumnsForCode(dsd, specificCodedDiagnosesIndicator, alreadyReportedConcepts, "pregnancyComplications", "Complications de grossesse", mirebalaisReports, "pregnancyComplications");
 
         // TODO: NOUVEAUX patients vus avec d’autres conditions
         // Patients with some diagnosis during the period (excluding Bonne Sante Apparent and Unknown), but no notifiable disease in the period
         // QUESTION: should this include non-coded diagnoses?
         {
             CohortDefinition codedDiagnosisQuery = cohortDefinitionService.getDefinitionByUuid(BasicCohortDefinitionLibrary.PREFIX + "specific coded diagnoses between dates");
             codedDiagnosisQuery.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
             codedDiagnosisQuery.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
             codedDiagnosisQuery.addParameter(new Parameter("codedDiagnoses", "Include Coded Diagnoses", Concept.class, List.class, null));
             codedDiagnosisQuery.addParameter(new Parameter("excludeCodedDiagnoses", "Exclude Coded Diagnoses", Concept.class, List.class, null));
 
             CompositionCohortDefinition composition = new CompositionCohortDefinition();
             composition.addParameter(new Parameter("startOfWeek", "Start of Week", Date.class));
             composition.addSearch("hasDiagnosisWithExclusions", map(codedDiagnosisQuery, "onOrAfter", "${startOfWeek}", "onOrBefore", "${startOfWeek + 6d}",
                     "excludeCodedDiagnoses", mirebalaisProperties.getSetOfNonDiagnoses().getSetMembers()));
             composition.addSearch("hasAlreadyReportedDiagnosis", map(codedDiagnosisQuery, "onOrAfter", "${startOfWeek}", "onOrBefore", "${startOfWeek + 6d}",
                     "codedDiagnoses", new ArrayList<Concept>(alreadyReportedConcepts)));
             composition.setCompositionString("hasDiagnosisWithExclusions AND NOT hasAlreadyReportedDiagnosis");
 
             CohortIndicator ci = new CohortIndicator();
             ci.addParameter(new Parameter("startOfWeek", "Start of week", Date.class));
             ci.setCohortDefinition(map(composition, "startOfWeek=${startOfWeek}"));
 
             Mapped<CohortIndicator> mappedIndicator = map(ci, "startOfWeek=${startOfWeek}");
             addDsdColumns(dsd, "other", "NOUVEAUX patients vus avec d’autres conditions", mappedIndicator);
         }
 
         return dsd;
     }
 
    private void addDiseaseColumnsForCode(CohortIndicatorDataSetDefinition dsd, CohortIndicator indicator, Set<Concept> alreadyReportedConcepts, String name, String label, ConceptSource source, String... icdCodes) {
         Map<String, Object> mappings = ParameterizableUtil.createParameterMappings("startDate=${startOfWeek},endDate=${startOfWeek + 6d}");
         List<Concept> concepts = new ArrayList<Concept>();
        for (String icdCode : icdCodes) {
            concepts.addAll(emrConceptService.getConceptsSameOrNarrowerThan(conceptService.getConceptReferenceTermByCode(icdCode, source)));
         }
         mappings.put("codedDiagnoses", concepts);
 
         alreadyReportedConcepts.addAll(concepts);
 
         Mapped<CohortIndicator> mappedIndicator = new Mapped<CohortIndicator>(indicator, mappings);
 
         addDsdColumns(dsd, name, label, mappedIndicator);
     }
 
     private void addDsdColumns(CohortIndicatorDataSetDefinition dsd, String name, String label, Mapped<CohortIndicator> mappedIndicator) {
         dsd.addColumn(name + ".male.young", label + " (<5 ans H)", mappedIndicator, "age=young|gender=male");
         dsd.addColumn(name + ".female.young", label + " (<5 ans F)", mappedIndicator, "age=young|gender=female");
         dsd.addColumn(name + ".male.old", label + " (>=5 ans H)", mappedIndicator, "age=old|gender=male");
         dsd.addColumn(name + ".female.old", label + " (>=5 ans F)", mappedIndicator, "age=old|gender=female");
     }
 
     private <T extends Parameterizable> Mapped<T> map(T parameterizable, Object... mappings) {
         if (parameterizable == null) {
             throw new NullPointerException("Programming error: missing parameterizable");
         }
         Map<String, Object> map = new HashMap<String, Object>();
         for (int i = 0; i < mappings.length; i += 2) {
             map.put((String) mappings[i], mappings[i + 1]);
         }
         return new Mapped<T>(parameterizable, map);
     }
 
     private <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
         if (parameterizable == null) {
             throw new NullPointerException("Programming error: missing parameterizable");
         }
         if (mappings == null) {
             mappings = ""; // probably not necessary, just to be safe
         }
         return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
     }
 
     public byte[] loadExcelTemplate() {
         String templatePath = "reportTemplates/MSPP_Weekly_Diagnosis_Surveillance-template.xls";
 
         try {
             InputStream is = this.getClass().getClassLoader().getResourceAsStream(templatePath);
             byte[] contents = IOUtils.toByteArray(is);
             IOUtils.closeQuietly(is);
             return contents;
         } catch (IOException ex) {
             throw new RuntimeException("Error loading excel template", ex);
         }
     }
 
     public String getExcelDownloadFilename(EvaluationContext evaluationContext) {
         return "MSPP_Weekly_Diagnosis_Surveillance_" + new SimpleDateFormat("yyyyMMdd").format(evaluationContext.getParameterValue("startOfWeek")) + ".xls";
     }
 
 }
