 package org.patientview.radar.service.impl;
 
 import org.patientview.model.Patient;
 import org.patientview.radar.dao.DemographicsDao;
 import org.patientview.radar.dao.PatientLinkDao;
 import org.patientview.radar.model.PatientLink;
 import org.patientview.radar.service.PatientLinkManager;
 
 import java.util.List;
 
 /**
  * User: james@solidstategroup.com
  * Date: 14/11/13
  * Time: 17:04
  */
 public class PatientLinkManagerImpl implements PatientLinkManager {
 
     private DemographicsDao demographicsDao;
 
     private PatientLinkDao patientLinkDao;
 
     public PatientLink createLink(PatientLink patientLink) {
         return patientLinkDao.createLink(patientLink);
     }
 
     public List<PatientLink> getPatientLink(String nhsNo, String unitCode) {
         return patientLinkDao.getPatientLink(nhsNo, unitCode);
     }
 
     public void setPatientLinkDao(PatientLinkDao patientLinkDao) {
         this.patientLinkDao = patientLinkDao;
     }
 
     // create the new patient record and link entity
     public Patient linkPatientRecord(Patient patient) throws Exception {
        try {
             PatientLink patientLink =  new PatientLink();
             patientLink.setSourceNhsNO(patient.getNhsno());
            patientLink.setSourceUnit(patient.getRenalUnit().getUnitCode());
             patientLink.setDestinationNhsNo(patient.getNhsno());
             patientLink.setDestinationUnit(patient.getDiseaseGroup().getId());
 
             demographicsDao.saveDemographics(createLinkRecord(patient));
             patientLinkDao.createLink(patientLink);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
 
 
         return patient;
     }
 
     // Create bare patient record for Radar
     public Patient createLinkRecord(Patient patient) {
         Patient newPatient = new Patient();
         newPatient.setNhsno(patient.getNhsno());
         newPatient.setUnitcode(patient.getDiseaseGroup().getId());
 
         return newPatient;
     }
 
     public void setDemographicsDao(DemographicsDao demographicsDao) {
         this.demographicsDao = demographicsDao;
     }
 }
