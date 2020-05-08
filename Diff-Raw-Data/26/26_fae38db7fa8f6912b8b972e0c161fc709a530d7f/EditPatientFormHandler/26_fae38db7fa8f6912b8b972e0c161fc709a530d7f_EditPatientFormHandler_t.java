 package org.motechproject.ghana.national.handlers;
 
 import org.motechproject.ghana.national.bean.EditClientForm;
 import org.motechproject.ghana.national.domain.Constants;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.PatientAttributes;
 import org.motechproject.ghana.national.exception.ParentNotFoundException;
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.ghana.national.service.StaffService;
 import org.motechproject.mobileforms.api.callbacks.FormPublishHandler;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.mrs.model.*;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import static ch.lambdaj.Lambda.*;
 import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
 import static org.hamcrest.Matchers.equalTo;
 import static org.motechproject.ghana.national.tools.Utility.nullSafe;
 
 
 @Component
 public class EditPatientFormHandler implements FormPublishHandler {
 
     public static final String FORM_BEAN = "formBean";
     private final Logger log = LoggerFactory.getLogger(this.getClass());
 
     @Autowired
     private PatientService patientService;
 
     @Autowired
     private FacilityService facilityService;
 
     @Autowired
     private StaffService staffService;
     public static final String PATIENTEDITVISIT = "PATIENTEDITVISIT";
 
     @Override
     @MotechListener(subjects = "form.validation.successful.NurseDataEntry.editPatient")
     @LoginAsAdmin
     @ApiSession
     public void handleFormEvent(MotechEvent event) {
 
         EditClientForm form = (EditClientForm) event.getParameters().get(FORM_BEAN);
 
         MRSPatient patientFromDb = null;
         try {
             patientFromDb = updatePatient(form);
             createEncounter(form, patientFromDb);
 
         } catch (Exception e) {
             log.error("Encountered exception while updating patient", e);
         }
     }
 
     private MRSPatient updatePatient(EditClientForm form) throws ParentNotFoundException {
         String motechId = form.getMotechId();
         Patient existingPatient = patientService.getPatientByMotechId(motechId);
 
         MRSPatient patientFromDb = existingPatient.getMrsPatient();
        String facilityId = hasValue(form.getFacilityId()) ? facilityService.getFacilityByMotechId(form.getFacilityId()).mrsFacilityId() : patientFromDb.getFacility().getId();
 
         MRSPerson personFromDb = patientFromDb.getPerson();
 
         List<Attribute> attributesFromDb = personFromDb.getAttributes();
         MRSPerson person = new MRSPerson().dateOfBirth(nullSafe(form.getDateOfBirth(), personFromDb.getDateOfBirth())).
                 firstName(nullSafe(form.getFirstName(), personFromDb.getFirstName())).
                 middleName(nullSafe(form.getMiddleName(), personFromDb.getMiddleName())).
                 lastName(nullSafe(form.getLastName(), personFromDb.getLastName())).
                 preferredName(nullSafe(form.getPrefferedName(), personFromDb.getPreferredName())).
                 gender(nullSafe(form.getSex(), personFromDb.getGender())).
                address(nullSafe(form.getAddress(), personFromDb.getAddress()));

        if (form.getNhis() != null) {
            person.addAttribute(replaceValueFromDbIfNotProvided(PatientAttributes.NHIS_NUMBER.getAttribute(), form.getNhis(), attributesFromDb));
        }
        if (form.getPhoneNumber() != null) {
            person.addAttribute(replaceValueFromDbIfNotProvided(PatientAttributes.PHONE_NUMBER.getAttribute(), form.getPhoneNumber(), attributesFromDb));
        }
        if (form.getNhisExpires() != null) {
            person.addAttribute(replaceValueFromDbIfNotProvided(PatientAttributes.NHIS_EXPIRY_DATE.getAttribute(), new SimpleDateFormat(Constants.PATTERN_YYYY_MM_DD).format(form.getNhisExpires()), attributesFromDb));
        }

        person.birthDateEstimated(personFromDb.getBirthDateEstimated());
 
         Patient patient = new Patient(new MRSPatient(motechId, person, new MRSFacility(facilityId)), form.getMotherMotechId());
         patientService.updatePatient(patient);
         return patientFromDb;
     }
 
     private void createEncounter(EditClientForm form, MRSPatient patientFromDb) {
         MRSUser user = staffService.getUserByEmailIdOrMotechId(form.getStaffId());
         String facilityIdWhereUserDetailsWasEdited = facilityService.getFacilityByMotechId(form.getUpdatePatientFacilityId()).getMrsFacilityId();
         MRSEncounter mrsEncounter = new MRSEncounter(user.getPerson().getId(), user.getId(), facilityIdWhereUserDetailsWasEdited,
                 form.getDate(), patientFromDb.getId(), null, PATIENTEDITVISIT);
 
         patientService.saveEncounter(mrsEncounter);
     }
 
     private Attribute replaceValueFromDbIfNotProvided(String attributeName, String attributeValue, List<Attribute> attributesFromDb) {
         Attribute attributeFromDb = getAttribute(attributesFromDb, attributeName);
        return hasValue(attributeValue) ? new Attribute(attributeName, attributeValue) : attributeFromDb;
     }
 
     public Attribute getAttribute(List<Attribute> attributes, String key) {
         List<Attribute> filteredItems = select(attributes, having(on(Attribute.class).name(), equalTo(key)));
         return isNotEmpty(filteredItems) ? filteredItems.get(0) : null;
     }
 
    private boolean hasValue(Object value) {
         return value != null;
     }
 }
