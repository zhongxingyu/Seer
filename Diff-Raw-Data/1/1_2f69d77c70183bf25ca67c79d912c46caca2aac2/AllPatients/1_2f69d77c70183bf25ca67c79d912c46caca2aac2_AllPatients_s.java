 package org.motechproject.ghana.national.repository;
 
 import ch.lambdaj.function.convert.Converter;
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.exception.ParentNotFoundException;
 import org.motechproject.mrs.exception.PatientNotFoundException;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.mrs.services.MRSPatientAdapter;
 import org.motechproject.openmrs.services.OpenMRSRelationshipAdapter;
 import org.openmrs.Person;
 import org.openmrs.Relationship;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static ch.lambdaj.Lambda.*;
 import static org.hamcrest.Matchers.containsString;
 
 @Repository
 public class AllPatients {
     Logger logger = LoggerFactory.getLogger(this.getClass());
 
     @Autowired
     private MRSPatientAdapter patientAdapter;
 
     @Autowired
     private OpenMRSRelationshipAdapter openMRSRelationshipAdapter;
 
     @Autowired
     DataSource dataSource;
 
     public Patient save(Patient patient) throws ParentNotFoundException {
         MRSPatient mrsPatient = patientAdapter.savePatient(patient.getMrsPatient());
         if (StringUtils.isNotEmpty(patient.getParentId())) {
             Patient mother = getPatientByMotechId(patient.getParentId());
             if (mother == null) throw new ParentNotFoundException();
             createMotherChildRelationship(mother.getMrsPatient().getPerson(), mrsPatient.getPerson());
         }
         return new Patient(mrsPatient, patient.getParentId());
     }
 
     public Patient patientByOpenmrsId(String patientId) {
         MRSPatient mrsPatient = patientAdapter.getPatient(patientId);
         return (mrsPatient != null) ? new Patient(mrsPatient) : null;
     }
 
     public Patient getPatientByMotechId(String id) {
         MRSPatient mrsPatient = patientAdapter.getPatientByMotechId(id);
         if (mrsPatient != null) {
             Patient patient = new Patient(mrsPatient);
             Relationship motherRelationship = getMotherRelationship(patient.getMrsPatient().getPerson());
             if (motherRelationship != null) {
                 setParentId(patient, motherRelationship);
             }
             return patient;
         }
         return null;
     }
 
     private void setParentId(Patient patient, Relationship motherRelationship) {
         Person mother = motherRelationship.getPersonA();
         if (mother != null && !mother.getNames().isEmpty()) {
             List<Patient> patients = search(mother.getNames().iterator().next().getFullName(), null, null);
             if (patients != null && !patients.isEmpty()) {
                 patient.parentId(getParentId(mother, patients));
             }
         }
     }
 
     private String getParentId(Person mother, List<Patient> patients) {
         for (Patient patient : patients) {
             if (patient.getMrsPatient().getPerson().getId().equals(mother.getId().toString())) {
                 return patient.getMrsPatient().getMotechId();
             }
         }
         return null;
     }
 
     public List<Patient> search(String name, String motechId, String phoneNumber) {
         List<Patient> patients = convert(patientAdapter.search(name, motechId), new Converter<MRSPatient, Patient>() {
             @Override
             public Patient convert(MRSPatient mrsPatient) {
                 return new Patient(mrsPatient);
             }
         });
 
         if (StringUtils.isBlank(phoneNumber)) {
             return patients;
         }
 
         if (StringUtils.isBlank(name) && StringUtils.isBlank(motechId)) {
             return getPatientsByPhoneNumber(phoneNumber);
         }
 
         return filter(having(on(Patient.class).getPhoneNumber(), containsString(phoneNumber)), patients);
     }
 
     public Integer getAgeOfPersonByMotechId(String motechId) {
         return patientAdapter.getAgeOfPatientByMotechId(motechId);
     }
 
     public Patient update(Patient patient) {
         return new Patient(patientAdapter.updatePatient(patient.getMrsPatient()));
     }
 
     public void createMotherChildRelationship(MRSPerson mother, MRSPerson child) {
         openMRSRelationshipAdapter.createMotherChildRelationship(mother.getId(), child.getId());
     }
 
     public Relationship getMotherRelationship(MRSPerson person) {
         return openMRSRelationshipAdapter.getMotherRelationship(person.getId());
     }
 
     public Relationship updateMotherChildRelationship(MRSPerson mother, MRSPerson child) {
         return openMRSRelationshipAdapter.updateMotherRelationship(mother.getId(), child.getId());
     }
 
     public Relationship voidMotherChildRelationship(MRSPerson child) {
         return openMRSRelationshipAdapter.voidRelationship(child.getId());
     }
 
     public Patient getMother(String motechId) {
         Patient patient = getPatientByMotechId(motechId);
         Relationship motherRelationship = getMotherRelationship(patient.getMrsPatient().getPerson());
         if (motherRelationship != null) {
             return patientByOpenmrsId(motherRelationship.getPersonA().getPersonId().toString());
         }
         return null;
 
     }
 
     public void deceasePatient(Date dateOfDeath, String patientMotechId, String causeOfDeath, String comment) {
         try {
             patientAdapter.deceasePatient(patientMotechId, causeOfDeath, dateOfDeath, comment);
         } catch (PatientNotFoundException e) {
             logger.warn(e.getMessage());
         }
     }
 
     private List<Patient> getPatientsByPhoneNumber(String phoneNumber) {
         Connection connection = null;
         ArrayList<Patient> patients = new ArrayList<Patient>();
         try {
             connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("select pi.identifier from patient p, person_attribute pa,patient_identifier pi " +
                     "where pa.person_attribute_type_id = 8 " +
                     "and pa.value like ? " +
                     "and pi.patient_id = p.patient_id " +
                     "and pa.person_id = p.patient_id");
             statement.setString(1, "%" + phoneNumber + "%");
             ResultSet resultSet = statement.executeQuery();
             ArrayList<String> motechIds = new ArrayList<String>();
 
             while (resultSet.next()) {
                 motechIds.add(resultSet.getString(1));
             }
 
             if (!motechIds.isEmpty()) {
                 for (String motechId : motechIds) {
                     patients.add(getPatientByMotechId(motechId));
                 }
             }
         } catch (SQLException e) {
             logger.error("Exception in retrieving patients with phone numbers." + phoneNumber, e);
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     logger.error("Exception in closing the connection.", e);
                 }
             }
         }
 
         return patients;
     }
 }
