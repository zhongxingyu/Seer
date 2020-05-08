 package org.medical.service.impl;
 
 import org.medical.dao.mongo.PatientDao;
 import org.medical.model.mongo.*;
 import org.medical.service.PatientManager;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
@Service("patientService") public class PatientManagerImpl implements PatientManager {
 	
 	@Autowired
     PatientDao repository;
 	QPatient patient = QPatient.patient;
 
 	@Override
 	public void add(Patient patient) {
 		repository.save(patient);
 	}
 
 	@Override
 	public void remove(Patient patient) {
 		repository.delete(patient);
 	}
 
 	@Override
 	public Iterable<Patient> findAll(String search) {
 		return repository.findAll(patient.firstname.containsIgnoreCase(search));
 	}
 
 	@Override
 	public void updatePersonalInfo(Patient patient) {
 		PatientSearch search = new PatientSearch();
 		search.setFirstname(patient.getFirstname());
 //		return repository.save(entities)		
 	}
 
 	@Override
 	public Patient find(String code) {
 		return repository.findOne(patient.code.eq(code));
 	}
 
 	@Override
 	public void addHistory(History history) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void addComplaint(Complaint complaint) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void addFollowup(Followup followup) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
