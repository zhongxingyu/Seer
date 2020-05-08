 package org.motechproject.ghana.national.web.helper;
 
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.web.form.ANCEnrollmentForm;
 import org.motechproject.ghana.national.web.form.FacilityForm;
 import org.motechproject.mrs.model.MRSEncounter;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSObservation;
 import org.springframework.stereotype.Component;
 
 import java.util.Date;
 import java.util.Set;
 
 @Component
 public class ANCFormMapper {
 
     public ANCEnrollmentForm convertMRSEncounterToView(MRSEncounter mrsEncounter) {
         ANCEnrollmentForm ancEnrollmentForm = new ANCEnrollmentForm();
         final FacilityForm facilityForm = new FacilityForm();
         final MRSFacility facility = mrsEncounter.getFacility();
         facilityForm.setCountry(facility.getCountry());
         facilityForm.setRegion(facility.getRegion());
         facilityForm.setCountyDistrict(facility.getCountyDistrict());
         facilityForm.setStateProvince(facility.getStateProvince());
         facilityForm.setFacilityId(facility.getId());
         facilityForm.setName(facility.getName());
 
 
         ancEnrollmentForm.setFacilityForm(facilityForm);
         ancEnrollmentForm.setRegistrationDate(mrsEncounter.getDate());
         ancEnrollmentForm.setMotechPatientId(mrsEncounter.getPatient().getMotechId());
         ancEnrollmentForm.setStaffId(mrsEncounter.getCreator().getSystemId());
 
         Set<MRSObservation> observations = mrsEncounter.getObservations();
         ancEnrollmentForm.setAddHistory(!observations.isEmpty());
 
         for (MRSObservation mrsObservation : observations) {
             String conceptName = mrsObservation.getConceptName();
             Object value = mrsObservation.getValue();
 
             if (Constants.CONCEPT_GRAVIDA.equals(conceptName)) {
                ancEnrollmentForm.setGravida(((Double) value).intValue());
             }
             if (Constants.CONCEPT_ANC_REG_NUM.equals(conceptName)) {
                 ancEnrollmentForm.setSerialNumber((String) value);
             }
             if (Constants.CONCEPT_HEIGHT.equals(conceptName)) {
                 ancEnrollmentForm.setHeight((Double) value);
             }
             if (Constants.CONCEPT_PARITY.equals(conceptName)) {
                ancEnrollmentForm.setParity(((Double) value).intValue());
             }
             if (Constants.CONCEPT_CONFINEMENT_CONFIRMED.equals(conceptName)) {
                 ancEnrollmentForm.setDeliveryDateConfirmed((Boolean) value);
             }
             if (Constants.CONCEPT_EDD.equals(conceptName)) {
                 ancEnrollmentForm.setEstimatedDateOfDelivery((Date) value);
             }
             if (Constants.CONCEPT_IPT.equals(conceptName)) {
                 ancEnrollmentForm.setLastIPT((String) value);
                 ancEnrollmentForm.setLastIPTDate(mrsObservation.getDate());
                 ancEnrollmentForm.setAddHistory(true);
             }
             if (Constants.CONCEPT_TT.equals(conceptName)) {
                 ancEnrollmentForm.setLastTT((String) value);
                 ancEnrollmentForm.setLastTTDate(mrsObservation.getDate());
                 ancEnrollmentForm.setAddHistory(true);
 
             }
         }
         return ancEnrollmentForm;
     }
 }
