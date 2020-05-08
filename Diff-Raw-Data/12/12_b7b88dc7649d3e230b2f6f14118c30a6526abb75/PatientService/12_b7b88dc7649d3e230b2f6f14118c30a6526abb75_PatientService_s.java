 package org.motechproject.ghana.national.service;
 
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
 import org.motechproject.ghana.national.exception.ParentNotFoundException;
 import org.motechproject.ghana.national.exception.PatientIdIncorrectFormatException;
 import org.motechproject.ghana.national.exception.PatientIdNotUniqueException;
import org.motechproject.ghana.national.repository.*;
 import org.motechproject.mrs.exception.PatientNotFoundException;
 import org.motechproject.mrs.model.MRSObservation;
 import org.motechproject.mrs.model.MRSPatient;
 import org.openmrs.Person;
 import org.openmrs.Relationship;
 import org.openmrs.api.IdentifierNotUniqueException;
 import org.openmrs.api.InvalidCheckDigitException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import static org.apache.commons.lang.StringUtils.isEmpty;
 import static org.apache.commons.lang.StringUtils.isNotEmpty;
 import static org.motechproject.ghana.national.domain.EncounterType.PATIENT_EDIT_VISIT;
 import static org.motechproject.ghana.national.domain.EncounterType.PATIENT_REG_VISIT;
 import static org.motechproject.ghana.national.tools.Utility.emptyToNull;
 
 @Service
 public class PatientService {
     private AllPatients allPatients;
     private IdentifierGenerator identifierGenerator;
     private AllEncounters allEncounters;
     private AllSchedulesAndMessages allSchedulesAndMessages;
     private AllAppointmentsAndMessages allAppointmentsAndMessages;
     private AllPatientSearch allPatientSearch;
     private AllMobileMidwifeEnrollments allMobileMidwifeEnrollments;
 
     @Autowired
     public PatientService(AllPatients allPatients, IdentifierGenerator identifierGenerator, AllEncounters allEncounters,
                           AllSchedulesAndMessages allSchedulesAndMessages, AllAppointmentsAndMessages allAppointmentsAndMessages,
                           AllPatientSearch allPatientSearch, AllMobileMidwifeEnrollments allMobileMidwifeEnrollments) {
         this.allPatients = allPatients;
         this.identifierGenerator = identifierGenerator;
         this.allEncounters = allEncounters;
         this.allSchedulesAndMessages = allSchedulesAndMessages;
         this.allAppointmentsAndMessages = allAppointmentsAndMessages;
         this.allPatientSearch = allPatientSearch;
         this.allMobileMidwifeEnrollments = allMobileMidwifeEnrollments;
     }
 
     public Patient registerPatient(Patient patient, String staffId, Date registrationDate)
             throws ParentNotFoundException, PatientIdNotUniqueException, PatientIdIncorrectFormatException {
         try {
 
             if (isEmpty(patient.getMrsPatient().getMotechId())) {
                 MRSPatient mrsPatient = patient.getMrsPatient();
                 String motechId = identifierGenerator.newPatientId();
                 patient = new Patient(new MRSPatient(mrsPatient.getId(), motechId, mrsPatient.getPerson(), mrsPatient.getFacility()), patient.getParentId());
             }
             Patient savedPatient = allPatients.save(patient);
             allEncounters.persistEncounter(savedPatient.getMrsPatient(), staffId, patient.getMrsPatient().getFacility().getId(),
                     PATIENT_REG_VISIT.value(), registrationDate, null);
             return savedPatient;
         } catch (IdentifierNotUniqueException e) {
             throw new PatientIdNotUniqueException();
         } catch (InvalidCheckDigitException e) {
             throw new PatientIdIncorrectFormatException();
         }
     }
 
     public Patient getPatientByMotechId(String patientId) {
         return allPatients.getPatientByMotechId(patientId);
     }
 
     public List<Patient> search(String name, String motechId) {
         return allPatients.search(emptyToNull(name), emptyToNull(motechId));
     }
 
     public String updatePatient(Patient patient, String staffId, Date updatedDate) throws ParentNotFoundException {
         Patient savedPatient = allPatients.update(patient);
         Relationship relationship = allPatients.getMotherRelationship(savedPatient.getMrsPatient().getPerson());
 
         if (relationship != null && isEmpty(patient.getParentId())) {
             allPatients.voidMotherChildRelationship(savedPatient.getMrsPatient().getPerson());
         }
         if (relationship == null && isNotEmpty(patient.getParentId())) {
             createRelationship(patient.getParentId(), savedPatient.getMotechId());
         }
         if (relationship != null && isNotEmpty(patient.getParentId())) {
             updateRelationship(patient.getParentId(), savedPatient, relationship);
         }
         allEncounters.persistEncounter(savedPatient.getMrsPatient(), staffId, patient.getMrsPatient().getFacility().getId(),
                 PATIENT_EDIT_VISIT.value(), updatedDate, Collections.<MRSObservation>emptySet());
         return savedPatient.getMotechId();
     }
 
     public Integer getAgeOfPatientByMotechId(String motechId) {
         return allPatients.getAgeOfPersonByMotechId(motechId);
     }
 
     void createRelationship(String parentId, String savedPatientId) throws ParentNotFoundException {
         Patient mother = getPatientByMotechId(parentId);
         if (mother == null) {
             throw new ParentNotFoundException();
         }
         Patient child = getPatientByMotechId(savedPatientId);
         allPatients.createMotherChildRelationship(mother.getMrsPatient().getPerson(), child.getMrsPatient().getPerson());
     }
 
     private void updateRelationship(String parentId, Patient savedPatient, Relationship relationship) throws ParentNotFoundException {
         Patient updatedMother = getPatientByMotechId(parentId);
         if (updatedMother == null) {
             throw new ParentNotFoundException();
         }
         Person personA = relationship.getPersonA();
         if (personA != null && !personA.getId().toString().equals(updatedMother.getMrsPatient().getPerson().getId())) {
             allPatients.updateMotherChildRelationship(updatedMother.getMrsPatient().getPerson(), savedPatient.getMrsPatient().getPerson());
         }
     }
 
     public void deceasePatient(Date dateOfDeath, String patientMotechId, String causeOfDeath, String comment) throws PatientNotFoundException {
         Patient patient = getPatientByMotechId(patientMotechId);
         allPatients.deceasePatient(dateOfDeath, patientMotechId, (causeOfDeath.equals("OTHER") ? "OTHER NON-CODED" : "NONE"), comment);
         allSchedulesAndMessages.unEnroll(patient.getMRSPatientId(), patient.allCareProgramsToUnEnroll());
         allAppointmentsAndMessages.remove(patient);
     }
 
     public List<MRSPatient> getPatients(String firstName, String lastName, String phoneNumber, Date dateOfBirth, String insuranceNumber) {
         return allPatientSearch.getPatients(firstName, lastName, phoneNumber, dateOfBirth, insuranceNumber);
     }
 
     public Patient patientByOpenmrsId(String patientId) {
         return allPatients.patientByOpenmrsId(patientId);
     }
 
     public String receiveSMSOnPhoneNumber(String motechId) {
 
         Patient patient = allPatients.getPatientByMotechId(motechId);
         MobileMidwifeEnrollment mobileMidwifeEnrollment = allMobileMidwifeEnrollments.findActiveBy(patient.getMotechId());
         if (mobileMidwifeEnrollment != null) {
             return mobileMidwifeEnrollment.getPhoneNumber();
         }
         Patient mother = getMother(motechId);
         if (mother == null) {
             return patient.getPhoneNumber();
         }
        return (patient.getPhoneNumber() != null) ? patient.getPhoneNumber() : getMothersPhoneNumber(mother);
     }
 
     private String getMothersPhoneNumber(Patient mother) {
         MobileMidwifeEnrollment motherMobileMidwifeEnrollment = allMobileMidwifeEnrollments.findActiveBy(mother.getMotechId());
         return motherMobileMidwifeEnrollment != null ? motherMobileMidwifeEnrollment.getPhoneNumber() : mother.getPhoneNumber();
     }
 
     public Patient getMother(String motechId) {
         return allPatients.getMother(motechId);
     }
 
 }
