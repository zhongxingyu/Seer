 package com.worthsoln.ibd.action;
 
 import com.worthsoln.ibd.Ibd;
 import com.worthsoln.ibd.model.MyIbd;
 import com.worthsoln.ibd.model.MyIbdSeverityLevel;
 import com.worthsoln.ibd.model.enums.BodyPartAffected;
 import com.worthsoln.ibd.model.enums.Complication;
 import com.worthsoln.ibd.model.enums.Confidence;
 import com.worthsoln.ibd.model.enums.Diagnosis;
 import com.worthsoln.ibd.model.enums.DiseaseExtent;
 import com.worthsoln.ibd.model.enums.FamilyHistory;
 import com.worthsoln.ibd.model.enums.Feeling;
 import com.worthsoln.ibd.model.enums.Importance;
 import com.worthsoln.ibd.model.enums.Severity;
 import com.worthsoln.ibd.model.enums.Smoking;
 import com.worthsoln.ibd.model.enums.Surgery;
 import com.worthsoln.ibd.model.enums.VaccinationRecord;
 import com.worthsoln.ibd.model.enums.colitis.NumberOfStoolsDaytime;
 import com.worthsoln.ibd.model.enums.colitis.NumberOfStoolsNighttime;
 import com.worthsoln.ibd.model.enums.colitis.PresentBlood;
 import com.worthsoln.ibd.model.enums.colitis.ToiletTiming;
 import com.worthsoln.ibd.model.enums.crohns.AbdominalPain;
 import com.worthsoln.ibd.model.enums.crohns.MassInTummy;
 import com.worthsoln.ibd.model.medication.MedicationType;
 import com.worthsoln.ibd.model.medication.enums.MedicationFrequency;
 import com.worthsoln.ibd.model.symptoms.BaseSymptoms;
 import com.worthsoln.ibd.model.symptoms.SymptomsData;
 import com.worthsoln.ibd.model.symptoms.SymptomsGraphData;
 import com.worthsoln.patientview.model.EdtaCode;
 import com.worthsoln.patientview.model.User;
 import com.worthsoln.patientview.model.UserMapping;
 import com.worthsoln.patientview.user.UserUtils;
 import com.worthsoln.service.ibd.IbdManager;
 import com.worthsoln.utils.LegacySpringUtils;
 import org.apache.struts.action.DynaActionForm;
 import org.springframework.web.struts.ActionSupport;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 public class BaseAction extends ActionSupport {
 
     protected static final String SUCCESS = "success";
     protected static final String INPUT = "input";
     protected static final String ERROR = "error";
 
     // my ibd lists
     protected static final String DISEASE_EXTENT_LIST_PROPERTY = "diseaseExtentList";
     protected static final String DIAGNOSIS_LIST_PROPERTY = "diagnosisList";
     protected static final String COMPLICATION_LIST_PROPERTY = "complicationList";
     protected static final String FAMILY_HISTORY_LIST_PROPERTY = "familyHistoryList";
     protected static final String SMOKING_LIST_PROPERTY = "smokingList";
     protected static final String SURGERY_LIST_PROPERTY = "surgeryList";
     protected static final String BODY_PART_AFFECTED_LIST_PROPERTY = "bodyPartAffectedList";
     protected static final String VACCINATION_RECORD_LIST_PROPERTY = "vaccinationRecordList";
 
     // care plan lists
     protected static final String IMPORTANCE_LIST_PROPERTY = "importanceList";
     protected static final String CONFIDENCE_LIST_PROPERTY = "confidenceList";
 
     // medication lists
     protected static final String MEDICATION_TYPE_LIST_PROPERTY = "medicationTypeList";
     protected static final String MEDICATION_NO_OF_LIST_PROPERTY = "medicationNoOfList";
     protected static final String MEDICATION_FREQUENCY_LIST_PROPERTY = "medicationFrequencyList";
 
     // symptom lists
     protected static final String ABDOMINAL_PAIN_LIST_PROPERTY = "abdominalPainList";
     protected static final String FEELING_LIST_PROPERTY = "feelingList";
     protected static final String CROHNS_COMPLICATION_LIST_PROPERTY = "crohnsComplicationList";
     protected static final String MASS_IN_TUMMY_LIST_PROPERTY = "massInTummyList";
     protected static final String STOOLS_DAY_LIST_PROPERTY = "stoolsDayList";
     protected static final String STOOLS_NIGHT_LIST_PROPERTY = "stoolsNightList";
     protected static final String TOILET_TIMING_LIST_PROPERTY = "toiletTimingList";
     protected static final String PRESENT_BLOOD_LIST_PROPERTY = "presentBloodList";
     protected static final String FURTHER_COMPLICATION_LIST_PROPERTY = "furtherComplicationList";
     protected static final String OPEN_BOWEL_LIST_PROPERTY = "openBowelList";
 
     // just maps question link codes to params for the front end to pick up
     private static final HashMap<String, String> MY_IBD_QUESTIONS_MAP = new HashMap<String, String>() {
         {
             put("Primary Diagnosis", Ibd.PRIMARY_DIAGNOSIS_LINK_PARAM);
             put("Disease Extent", Ibd.DISEASE_EXTENT_LINK_PARAM);
             put("Year of Diagnosis", Ibd.YEAR_OF_DIAGNOSIS_LINK_PARAM);
             put("Complications", Ibd.COMPLICATIONS_LINK_PARAM);
             put("Other parts of the body affected", Ibd.BODY_PART_AFFECTED_LINK_PARAM);
             put("Year for Surveillance Colonoscopy", Ibd.YEAR_FOR_SURVEILLANCE_COLONOSCOPY_LINK_PARAM);
             put("Named Consultant", Ibd.NAMED_CONSULTANT_LINK_PARAM);
             put("Nurses", Ibd.NURSES_LINK_PARAM);
             put("Weight", Ibd.WEIGHT_LINK_PARAM);
             put("IBD Related Family History", Ibd.FAMILY_HISTORY_LINK_PARAM);
             put("Smoking History", Ibd.SMOKING_LINK_PARAM);
             put("Surgery History", Ibd.SURGERY_LINK_PARAM);
             put("Vaccination History", Ibd.VACCINATION_RECORD_LINK_PARAM);
         }
     };
 
     protected static List<OpenBowel> openBowelList;
 
     /**
      * When the actual edit form is submitted it should have an input field named submit set to true
      * This fnc checks for this and if its not present then the actual edit form has not been submitted
      * @param form DynaActionForm
      * @return boolean
      */
     protected boolean isFormSubmitted(DynaActionForm form) {
         boolean submit = false;
 
         if (form.get(Ibd.SUBMIT_PARAM) != null) {
             try {
                 submit = (Boolean) form.get(Ibd.SUBMIT_PARAM);
             } catch (Exception e) {
                 return false;
             }
         }
 
         return submit;
     }
 
     protected List<Importance> getImportanceList() {
         return Importance.getAsList();
     }
 
     protected List<Confidence> getConfidenceList() {
         return Confidence.getAsList();
     }
 
     protected List<DiseaseExtent> getDiseaseExtentList() {
         return DiseaseExtent.getAsList();
     }
 
     protected List<Diagnosis> getDiagnosisList() {
         return Diagnosis.getAsList();
     }
 
     protected List<Complication> getComplicationList() {
         return Complication.getAsList();
     }
 
     protected List<FamilyHistory> getFamilyHistoryList() {
         return FamilyHistory.getAsList();
     }
 
     protected List<Smoking> getSmokingList() {
         return Smoking.getAsList();
     }
 
     protected List<Surgery> getSurgeryList() {
         return Surgery.getAsList();
     }
 
     protected List<BodyPartAffected> getBodyPartAffectedList() {
         return BodyPartAffected.getAsList();
     }
 
     protected List<VaccinationRecord> getVaccinationRecordList() {
         return VaccinationRecord.getAsList();
     }
 
     protected List<AbdominalPain> getAbdominalPainList() {
         return AbdominalPain.getAsList();
     }
 
     protected List<Feeling> getFeelingList() {
         return Feeling.getAsList();
     }
 
     protected List<NumberOfStoolsDaytime> getStoolsDayList() {
         return NumberOfStoolsDaytime.getAsList();
     }
 
     protected List<NumberOfStoolsNighttime> getStoolsNightList() {
         return NumberOfStoolsNighttime.getAsList();
     }
 
     protected List<ToiletTiming> getToiletTimingList() {
         return ToiletTiming.getAsList();
     }
 
     protected List<PresentBlood> getPresentBloodList() {
         return PresentBlood.getAsList();
     }
 
     protected List<com.worthsoln.ibd.model.enums.colitis.Complication> getColitisComplicationList() {
         return com.worthsoln.ibd.model.enums.colitis.Complication.getAsList();
     }
 
     protected List<com.worthsoln.ibd.model.enums.crohns.Complication> getCrohnsComplicationList() {
         return com.worthsoln.ibd.model.enums.crohns.Complication.getAsList();
     }
 
     protected List<MassInTummy> getMassInTummy() {
         return MassInTummy.getAsList();
     }
 
     protected List<OpenBowel> getOpenBowelList() {
         if (openBowelList == null) {
             openBowelList = new ArrayList<OpenBowel>();
 
             for (int x = 0; x <= 20; x++) {
                 openBowelList.add(new OpenBowel(x));
             }
         }
 
         return openBowelList;
     }
 
     protected List<MedicationType> getMedicationTypeList() {
         List<MedicationType> medicationTypes = getIbdManager().getMedicationTypes();
 
         Collections.sort(medicationTypes, new Comparator<MedicationType>() {
             @Override
             public int compare(MedicationType o1, MedicationType o2) {
                 if (o1.getId() > o2.getId()) {
                     return 1;
                 } else if (o1.getId() < o2.getId()) {
                     return -1;
                 } else {
                     return 0;
                 }
             }
         });
 
         return medicationTypes;
     }
 
     protected List<MedicationFrequency> getMedicationFrequencyList() {
         return MedicationFrequency.getAsList();
     }
 
     protected String getNhsNoForUser(HttpServletRequest request) {
         return getNhsNoForUser(UserUtils.retrieveUser(request));
     }
 
     protected String getNhsNoForUser(User user) {
         UserMapping userMapping = LegacySpringUtils.getUserManager().getUserMappingPatientEntered(user);
 
         if (userMapping != null) {
             return userMapping.getNhsno();
         }
 
         return null;
     }
 
     protected void addLastSymptomAdvice(User user, Integer graphType, HttpServletRequest request) {
         List<? extends BaseSymptoms> symptoms = null;
 
         if (graphType != null) {
             if (graphType == Ibd.COLITIS_GRAPH_TYPE) {
                 symptoms = getIbdManager().getAllColitis(user, null, null);
             } else if (graphType == Ibd.CROHNS_GRAPH_TYPE) {
                 symptoms = getIbdManager().getAllCrohns(user, null, null);
             }
         }
 
         if (symptoms != null && !symptoms.isEmpty()) {
             BaseSymptoms lastSymptom = symptoms.get(0);
 
             String nhsNo = getNhsNoForUser(user);
             MyIbdSeverityLevel myIbdSevereLevel = getIbdManager().getMyIbdSeverityLevel(nhsNo, Severity.SEVERE);
             MyIbdSeverityLevel myIbdModerateLevel = getIbdManager().getMyIbdSeverityLevel(nhsNo, Severity.MODERATE);
             MyIbdSeverityLevel myIbdMildLevel = getIbdManager().getMyIbdSeverityLevel(nhsNo, Severity.MILD);
 
             MyIbd myIbd = getIbdManager().getMyIbd(UserUtils.retrieveUser(request));
             Diagnosis diagnosis = myIbd.getDiagnosis();
 
             if (lastSymptom.getScore() >= myIbdSevereLevel.getLevel(diagnosis)) {
                 request.setAttribute(Ibd.MY_IBD_SEVERITY_LEVEL_PARAM, myIbdSevereLevel);
             } else if (lastSymptom.getScore() <= myIbdMildLevel.getLevel(diagnosis)) {
                 request.setAttribute(Ibd.MY_IBD_SEVERITY_LEVEL_PARAM, myIbdMildLevel);
             } else {
                 request.setAttribute(Ibd.MY_IBD_SEVERITY_LEVEL_PARAM, myIbdModerateLevel);
             }
         }
     }
 
     protected void addSymptomsGraphData(User user, Integer graphType, Date fromDate, Date toDate,
                                                      HttpServletRequest request) {
         SymptomsGraphData symptomsGraphData = new SymptomsGraphData();
         List<Date> existingDates = new ArrayList<Date>();
 
         if (graphType != null) {
             List<? extends BaseSymptoms> symptoms = null;
 
             if (graphType == Ibd.COLITIS_GRAPH_TYPE) {
                 symptoms = getIbdManager().getAllColitis(user, fromDate, toDate);
             } else if (graphType == Ibd.CROHNS_GRAPH_TYPE) {
                 symptoms = getIbdManager().getAllCrohns(user, fromDate, toDate);
             } else {
                 symptomsGraphData.setError(Ibd.NO_GRAPH_TYPE_SPECIFIED);
             }
 
             // - symptoms come out in DESC date order
             // - the graph can only really have one point a day so if there are multiple records on a date so only add
             //   first one found, this should be easy as the natural order wil have the latest first
             // - then reverse the final set back to ASC for the graph
             if (symptoms != null) {
                 for (BaseSymptoms symptom : symptoms) {
                     if (!existingDates.contains(symptom.getSymptomDate())) {
                         symptomsGraphData.addGraphData(new SymptomsData(symptom));
                         existingDates.add(symptom.getSymptomDate());
                     }
                 }
             }
 
             Collections.reverse(symptomsGraphData.getGraphData());
         } else {
             symptomsGraphData.setError(Ibd.NO_GRAPH_TYPE_SPECIFIED);
         }
 
         MyIbd myIbd = getIbdManager().getMyIbd(UserUtils.retrieveUser(request));
         Diagnosis diagnosis = myIbd.getDiagnosis();
 
         // need to check if they have any custom level settings
         String nhsNo = getNhsNoForUser(user);
         symptomsGraphData.setSevereLevel(getIbdManager().getMyIbdSeverityLevel(nhsNo, Severity.SEVERE).getLevel(
                 diagnosis));
         symptomsGraphData.setModerateLevel(getIbdManager().getMyIbdSeverityLevel(nhsNo, Severity.MODERATE).getLevel(
                 diagnosis));
         symptomsGraphData.setMildLevel(getIbdManager().getMyIbdSeverityLevel(nhsNo, Severity.MILD).getLevel(
                 diagnosis));
 
         // need to re add graph data to the page
         request.setAttribute(Ibd.GRAPH_DATA_PARAM, symptomsGraphData);
     }
 
     protected void addMyIbdLinks(MyIbd myIbd, HttpServletRequest request) {
        if (myIbd != null) {
            for (EdtaCode edtaCode : LegacySpringUtils.getEdtaCodeManager().get(Ibd.MY_IBD_LINKS_TYPE)) {
                if (MY_IBD_QUESTIONS_MAP.containsKey(edtaCode.getEdtaCode())) {
                    String link = null;

                    if (myIbd.getDiagnosis().equals(Diagnosis.CROHNS)) {
                        link = edtaCode.getMedicalLink01();
                    } else if (myIbd.getDiagnosis().equals(Diagnosis.COLITIS_UNSPECIFIED)
                            || myIbd.getDiagnosis().equals(Diagnosis.ULCERATIVE_COLITIS)) {
                        link = edtaCode.getMedicalLink02();
                    }
 
                    if (link != null && link.length() > 0) {
                        request.setAttribute(MY_IBD_QUESTIONS_MAP.get(edtaCode.getEdtaCode()), link);
                    }
                 }
             }
         }
     }
 
     protected Date convertFormDateString(String formProperty, DynaActionForm dynaActionForm) {
         String dateString = (String) dynaActionForm.get(formProperty);
 
         if (dateString != null && dateString.length() > 0) {
             try {
                 return Ibd.DATE_FORMAT.parse(dateString);
             } catch (Exception e) {
                 return null;
             }
         }
 
         return null;
     }
 
     protected String convertFormDateString(Date date) {
         if (date != null) {
             return Ibd.DATE_FORMAT.format(date);
         }
 
         return "";
     }
 
     protected IbdManager getIbdManager() {
         // To do this without the spring action support
 //        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(
 //                servlet.getServletContext());
 
         return getWebApplicationContext().getBean(IbdManager.class);
     }
 
     /**
      * This is just a simple class as the struts list cant just take an array of ints
      */
     public class OpenBowel {
         private int value;
 
         public OpenBowel(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
     }
 }
