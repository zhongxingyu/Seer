 package org.openmrs.module.radiotest.model;
 
 import java.util.Date;
 
 import org.openmrs.module.radiotest.RadioAlias;
 import org.openmrs.module.radiotest.RadioPatient;
 
 
 public class RadioPatientModel {
 	private RadioPatient patient;
 	private RadioAlias alias;
 	
 	public RadioPatient getPatient(){
 		return patient;
 	}
 	
 	public void setPatient(RadioPatient patient) {
 		this.patient = patient;
 	}
 
 	public RadioAlias getAlias() {
		return alias == null? new RadioAlias() : alias;
 	}
 
 	public void setAlias(RadioAlias alias) {
 		this.alias = alias;
 	}
 	
 	public RadioPatient getFullPatient(){
 		if (patient != null && alias != null){
 			alias.setPatient(patient);
 			alias.setStartDate(new Date());
 			patient.addAlias(alias);
 		}
 		return patient;
 	}
 }
