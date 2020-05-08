 package org.motechproject.ghana.national.web.helper;
 
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.CwcCareHistory;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.web.CWCController;
 import org.motechproject.ghana.national.web.form.CWCEnrollmentForm;
 import org.motechproject.ghana.national.web.form.FacilityForm;
 import org.motechproject.mrs.model.MRSEncounter;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSObservation;
 import org.openmrs.Concept;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 @Component
 public class CwcFormMapper {
     public CWCEnrollmentForm mapEncounterToView(MRSEncounter encounter) {
         CWCEnrollmentForm cwcEnrollmentForm = new CWCEnrollmentForm();
         final FacilityForm facilityForm = new FacilityForm();
         final MRSFacility facility = encounter.getFacility();
         facilityForm.setCountry(facility.getCountry());
         facilityForm.setRegion(facility.getRegion());
         facilityForm.setCountyDistrict(facility.getCountyDistrict());
         facilityForm.setStateProvince(facility.getStateProvince());
         facilityForm.setFacilityId(facility.getId());
         facilityForm.setName(facility.getName());
         cwcEnrollmentForm.setFacilityForm(facilityForm);
         cwcEnrollmentForm.setRegistrationDate(encounter.getDate());
         cwcEnrollmentForm.setPatientMotechId(encounter.getPatient().getMotechId());
         cwcEnrollmentForm.setStaffId(encounter.getCreator().getSystemId());
 
         Set<MRSObservation> observations = encounter.getObservations();
         cwcEnrollmentForm.setAddHistory(!observations.isEmpty());
         final ArrayList<CwcCareHistory> careHistories = new ArrayList<CwcCareHistory>();
         for (MRSObservation observation : observations) {
 
             if (Constants.CONCEPT_IMMUNIZATIONS_ORDERED.equals(observation.getConceptName())) {
                 final Concept concept = (Concept) observation.getValue();
                 final String conceptName = concept.getName().getName();
                 if (Constants.CONCEPT_YF.equals(conceptName)) {
                     cwcEnrollmentForm.setYfDate(observation.getDate());
                     careHistories.add(CwcCareHistory.YF);
                 }
                 if (Constants.CONCEPT_MEASLES.equals(conceptName)) {
                     cwcEnrollmentForm.setMeaslesDate(observation.getDate());
                     careHistories.add(CwcCareHistory.MEASLES);
                 }
                 if (Constants.CONCEPT_BCG.equals(conceptName)) {
                     cwcEnrollmentForm.setBcgDate(observation.getDate());
                     careHistories.add(CwcCareHistory.BCG);
                 }
                 if (Constants.CONCEPT_VITA.equals(conceptName)) {
                     cwcEnrollmentForm.setVitADate(observation.getDate());
                     careHistories.add(CwcCareHistory.VITA_A);
                 }
             }
             if (Constants.CONCEPT_IPTI.equals(observation.getConceptName())) {
                 cwcEnrollmentForm.setLastIPTiDate(observation.getDate());
                cwcEnrollmentForm.setLastIPTi((Integer) observation.getValue());
                 careHistories.add(CwcCareHistory.IPTI);
             }
 
             if (Constants.CONCEPT_PENTA.equals(observation.getConceptName())) {
                 cwcEnrollmentForm.setLastPentaDate(observation.getDate());
                cwcEnrollmentForm.setLastPenta((Integer) observation.getValue());
                 careHistories.add(CwcCareHistory.PENTA);
             }
             if (Constants.CONCEPT_OPV.equals(observation.getConceptName())) {
                 cwcEnrollmentForm.setLastOPVDate(observation.getDate());
                cwcEnrollmentForm.setLastOPV((Integer) observation.getValue());
                 careHistories.add(CwcCareHistory.OPV);
             }
             cwcEnrollmentForm.setCareHistory(careHistories);
             if (Constants.CONCEPT_CWC_REG_NUMBER.equals(observation.getConceptName())) {
                 cwcEnrollmentForm.setSerialNumber((String) observation.getValue());
             }
         }
         return cwcEnrollmentForm;
     }
 
     public Map<String, Object> setViewAttributes() {
         Map<String, Object> map = new HashMap<String, Object>();
 
         Map<CwcCareHistory, String> cwcCareHistories = new HashMap<CwcCareHistory, String>();
         for (CwcCareHistory cwcCareHistory : CwcCareHistory.values()) {
             cwcCareHistories.put(cwcCareHistory, cwcCareHistory.getDescription());
         }
 
         map.put(Constants.CARE_HISTORIES, cwcCareHistories);
 
         Map<RegistrationToday, String> registrationTodayValues = new HashMap<RegistrationToday, String>();
 
         for (RegistrationToday registrationToday : RegistrationToday.values()) {
             registrationTodayValues.put(registrationToday, registrationToday.getDescription());
         }
 
         map.put(CWCController.REGISTRATION_OPTIONS, registrationTodayValues);
         map.put(Constants.LAST_IPTI, new HashMap<Integer, String>() {{
             put(1, Constants.IPTI_1);
             put(2, Constants.IPTI_2);
             put(3, Constants.IPTI_3);
         }});
         map.put(Constants.LAST_OPV, new HashMap<Integer, String>() {{
             put(0, Constants.OPV_0);
             put(1, Constants.OPV_1);
             put(2, Constants.OPV_2);
             put(3, Constants.OPV_3);
         }});
         map.put(Constants.LAST_PENTA, new HashMap<Integer, String>() {{
             put(1, Constants.PENTA_1);
             put(2, Constants.PENTA_2);
             put(3, Constants.PENTA_3);
         }});
         return map;
     }
 }
