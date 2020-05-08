 package org.openmrs.module.radiotest.model;
 
 import java.util.Date;
 
import org.openmrs.api.context.Context;
 import org.openmrs.module.radiotest.RadioAlias;
import org.openmrs.module.radiotest.RadioIndex;
 import org.openmrs.module.radiotest.RadioPatient;
import org.openmrs.module.radiotest.api.RadioPatientService;
 
 
 public class RadioPatientModel {
 	private RadioPatient patient;
 	private RadioAlias alias;
 	
 	public RadioPatientModel(){
 		
 	}
 	
 	public RadioPatientModel(RadioPatient patient){
 		this.patient = patient;
 		this.alias = patient.getAlias();
 	}
 	
 	public RadioPatient getPatient(){
 		return patient;
 	}
 	
 	public void setPatient(RadioPatient patient) {
 		this.patient = patient;
 	}
 
 	public RadioAlias getAlias() {
 		return alias;
 	}
 
 	public void setAlias(RadioAlias alias) {
 		this.alias = alias;
 	}
 	
 	public RadioPatient getFullPatient(){
 		if (patient != null && alias != null){
			if(patient.getId() != null){
				RadioPatientService ps = Context.getService(RadioPatientService.class);
				patient = ps.updatePatient(patient);
			}
			
 			alias.setPatient(patient);
 			alias.setStartDate(new Date());
 			patient.addAlias(alias);
 		}
 		return patient;
 	}
 }
