 package org.patientview.radar.service.impl;
 
 import org.patientview.model.Ethnicity;
 import org.patientview.model.Patient;
 import org.patientview.model.Sex;
 import org.patientview.model.Status;
 import org.patientview.model.enums.SourceType;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.model.generic.GenericDiagnosis;
 import org.patientview.radar.dao.PatientDao;
 import org.patientview.radar.service.PatientManager;
 import org.patientview.radar.util.RadarUtility;
 import org.springframework.util.StringUtils;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created for the new functionality with using just the on patient table. Going forward this can be then merged with
  * the class of the same name in Patient View
  *
  * User: james@solidstategroup.com
  * Date: 06/11/13
  * Time: 11:11
  */
 public class PatientManagerImpl implements PatientManager {
 
     private PatientDao patientDao;
 
 
     public List<Patient> getPatientByNhsNumber(String nhsNo) {
         return patientDao.getPatientsByNhsNumber(nhsNo);
     }
 
     public Patient getById(Long id) {
         return resolveLinkRecord(patientDao.getById(id));
     }
 
     public Patient getPatientByRadarNumber(Long radarNumber) {
 
         Patient patient = patientDao.getPatientsByRadarNumber(radarNumber);
 
         if (patient == null) {
             patient = patientDao.getById(radarNumber);
         }
 
         patient = resolveLinkRecord(patient);
 
         return patient;
     }
 
    // Before save we strip the duplicated fields away, then after save we repopulate from the source
     public void save(final Patient patient){
 
         // If this is a link record then we need to stop any duplicated data being saved
         if (patientDao.getByPatientLinkId(patient.getId()) != null) {
             RadarUtility.cleanLinkRecord(patient);
         }
 
         patientDao.save(patient);
 
         // We have to re-populate fields after they are cleaned from the save, only for link patients
         Patient sourcePatient = patientDao.getByPatientLinkId(patient.getId());
         if (sourcePatient != null) {
             RadarUtility.overRideLinkRecord(sourcePatient, patient);
 
         }
 
     }
 
     public List<Patient> getPatientsByUnitCode(List<String> unitCodes) {
 
         List<Patient> patients = patientDao.getPatientsByUnitCode(unitCodes);
         Map<Long, Patient> linkedPatients = new HashMap<Long, Patient>();
 
 
         Iterator<Patient> iterator = patients.iterator();
         while (iterator.hasNext()) {
             Patient patient = iterator.next();
 
             // Need to override linked patients
             if (patient.getPatientLinkId() != null && patient.getPatientLinkId() > 0) {
                 Patient linkedPatient = RadarUtility.overRideLinkRecord(patient,
                         patientDao.getById(patient.getPatientLinkId()));
                 linkedPatient.setSurname(linkedPatient.getSurname());
                 linkedPatients.put(patient.getPatientLinkId(), linkedPatient);
                 iterator.remove();
             }
         }
 
         resolveLinkedPatients(patients, linkedPatients);
 
         return patients;
     }
 
     // This removes the patient view patients and replaces the linked patient with fully loaded merged ones.
     private void resolveLinkedPatients(List<Patient> patients, Map<Long, Patient> linkedPatients) {
 
         Iterator<Patient> iterator = patients.iterator();
         while (iterator.hasNext()) {
             Patient patient = iterator.next();
 
             if (!patient.getSourceType().equals(SourceType.RADAR.getName())) {
                 iterator.remove();
             }
 
             // There is a better way
             if (StringUtils.isEmpty(patient.getSurname())) {
                     iterator.remove();
             }
 
         }
 
         patients.addAll(linkedPatients.values());
 
     }
 
     /**
      * Resolve a two way link.
      *
      * 1) If it's a source record them merge the link record on top of it
      * 2) If it's a link record over write the standard patient fields.
      *
      * @param patient
      * @return
      */
     private Patient resolveLinkRecord(final Patient patient){
 
         if (patient != null) {
 
             Long patientLinkId = patient.getPatientLinkId();
 
             if (patientLinkId == null || patientLinkId == 0) {
 
                 Patient source = patientDao.getByPatientLinkId(patient.getId());
                 if (source != null) {
                     return RadarUtility.overRideLinkRecord(source, patient);
                 } else {
                     return patient;
                 }
             } else {
 
                 Patient linkPatient = patientDao.getById(patientLinkId);
                 if (linkPatient == null) {
                     return RadarUtility.mergePatientRecords(patient, linkPatient);
                 } else {
                     return patient;
                 }
 
             }
         } else {
             return null;
         }
     }
 
 
     public List<Sex> getSexes() {
         return patientDao.getSexes();
     }
 
     public List<Status> getStatuses() {
         return patientDao.getStatuses();
     }
 
     public List<DiseaseGroup> getDiseaseGroups() {
         return patientDao.getDiseaseGroups();
     }
 
     public List<GenericDiagnosis> getGenericDiagnoses() {
         return patientDao.getGenericDiagnoses();
     }
 
     public List<Ethnicity> getEthnicities() {
         return patientDao.getEthnicities();
     }
 
 
     public void setPatientDao(PatientDao patientDao) {
         this.patientDao = patientDao;
     }
 
 }
