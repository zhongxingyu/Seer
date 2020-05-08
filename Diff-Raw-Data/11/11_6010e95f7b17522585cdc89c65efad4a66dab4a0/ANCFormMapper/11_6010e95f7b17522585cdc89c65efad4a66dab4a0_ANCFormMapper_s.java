 package org.motechproject.ghana.national.web.helper;
 
 import org.motechproject.ghana.national.web.form.ANCEnrollmentForm;
 import org.motechproject.ghana.national.web.form.FacilityForm;
 import org.motechproject.mrs.model.MRSEncounter;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSObservation;
 import org.springframework.stereotype.Component;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import static ch.lambdaj.Lambda.*;
 import static org.hamcrest.Matchers.is;
 import static org.motechproject.ghana.national.domain.Concept.*;
 
 @Component
 public class ANCFormMapper {
 
     public ANCEnrollmentForm convertMRSEncounterToView(MRSEncounter mrsEncounter) {
         ANCEnrollmentForm ancEnrollmentForm = new ANCEnrollmentForm();
         FacilityForm facilityForm = new FacilityForm();
         MRSFacility facility = mrsEncounter.getFacility();
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
 
         ancEnrollmentForm.setAddHistory(false);
         for (MRSObservation mrsObservation : observations) {
             String conceptName = mrsObservation.getConceptName();
             Object value = mrsObservation.getValue();
 
             if (GRAVIDA.getName().equals(conceptName)) {
                 ancEnrollmentForm.setGravida(((Double) value).intValue());
             }
             if (ANC_REG_NUM.getName().equals(conceptName)) {
                 ancEnrollmentForm.setSerialNumber((String) value);
             }
             if (HEIGHT.getName().equals(conceptName)) {
                 ancEnrollmentForm.setHeight((Double) value);
             }
             if (PARITY.getName().equals(conceptName)) {
                 ancEnrollmentForm.setParity(((Double) value).intValue());
             }
             if (IPT.getName().equals(conceptName)) {
                 Integer lastIPT = ((Double) value).intValue();
                 ancEnrollmentForm.setLastIPT(lastIPT.toString());
                 ancEnrollmentForm.setLastIPTDate(mrsObservation.getDate());
                 ancEnrollmentForm.setAddHistory(true);
             }
             if (TT.getName().equals(conceptName)) {
                 Integer lastTT = ((Double) value).intValue();
                 ancEnrollmentForm.setLastTT(lastTT.toString());
                 ancEnrollmentForm.setLastTTDate(mrsObservation.getDate());
                 ancEnrollmentForm.setAddHistory(true);
             }
         }
         return ancEnrollmentForm;
     }
 
     public void populatePregnancyInfo(MRSEncounter mrsEncounter, ANCEnrollmentForm ancEnrollmentForm) {
         if (mrsEncounter == null) {
             return;
         }
        Boolean deliveryDateConfirmed = (Boolean) selectLatestObservation(mrsEncounter.getObservations(), CONFINEMENT_CONFIRMED.getName()).getValue();
        Date estimatedDateOfDelivery = (Date) selectLatestObservation(mrsEncounter.getObservations(), EDD.getName()).getValue();
 
         ancEnrollmentForm.setDeliveryDateConfirmed(deliveryDateConfirmed);
         ancEnrollmentForm.setEstimatedDateOfDelivery(estimatedDateOfDelivery);
     }
 
     private MRSObservation selectLatestObservation(Set<MRSObservation> observations, String observationName) {
         List<MRSObservation> confinementObservations = select(observations, having(on(MRSObservation.class).getConceptName(), is(observationName)));
         List<MRSObservation<MRSObservation>> sortedConfinements = sort(confinementObservations, on(MRSObservation.class).getDate());
        return sortedConfinements.get(sortedConfinements.size() - 1);
     }
 }
