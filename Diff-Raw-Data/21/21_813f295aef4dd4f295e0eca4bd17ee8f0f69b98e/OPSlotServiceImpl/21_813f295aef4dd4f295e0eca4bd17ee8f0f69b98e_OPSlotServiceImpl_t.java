 package at.ac.tuwien.dse.fairsurgeries.service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;

 import at.ac.tuwien.dse.fairsurgeries.domain.Doctor;
 import at.ac.tuwien.dse.fairsurgeries.domain.Hospital;
 import at.ac.tuwien.dse.fairsurgeries.domain.OPSlot;
 import at.ac.tuwien.dse.fairsurgeries.domain.Patient;
 import at.ac.tuwien.dse.fairsurgeries.domain.SurgeryType;
 
 
 public class OPSlotServiceImpl implements OPSlotService {
 	
	@Autowired
	LogEntryService logentryService;
	
 	public List<OPSlot> findByExample(OPSlot slot) {
 		List<OPSlot> slots = oPSlotRepository.findAll();
 		List<OPSlot> matchingSlots = new ArrayList<OPSlot>(slots);
 		
		logentryService.log("slot", "All slots: " + slots);
		logentryService.log("slot", "Example: " + slot);
		
 		if (slots != null && slot != null) {
 			Hospital hospital = slot.getHospital();
 			Patient patient = slot.getPatient();
 			Doctor doctor = slot.getDoctor();
 			Date from = slot.getDateFrom();
 			Date to = slot.getDateTo();
 			SurgeryType type = slot.getSurgeryType();
 			
 			for (OPSlot s : slots) {
 				if (hospital != null && !s.getHospital().getId().equals(hospital.getId())) {
 					matchingSlots.remove(s);
 					continue;
 				}
 				
 				if (patient != null && !s.getPatient().getId().equals(patient.getId())) {
 					matchingSlots.remove(s);
 					continue;
 				}
 				
 				if (doctor != null && !s.getDoctor().getId().equals(doctor.getId())) {
 					matchingSlots.remove(s);
 					continue;
 				}
 				
 				// TODO: Better date comparison
 				if (from != null && !s.getDateFrom().equals(from)) {
 					matchingSlots.remove(s);
 					continue;
 				}
 				
 				if (to != null && !s.getDateTo().equals(to)) {
 					matchingSlots.remove(s);
 					continue;
 				}
 				
 				if (type != null && !s.getSurgeryType().equals(type)) {
 					matchingSlots.remove(s);
 					continue;
 				}
 			}
 		}
 		
		logentryService.log("slot", "Matching Slots: " + matchingSlots);
		
 		return matchingSlots;
 	}
 	
 	public List<OPSlot> findByPatient(Patient patient) {
 		OPSlot example = new OPSlot();
 		
 		example.setPatient(patient);
 		return this.findByExample(example);
 	}
 	
 	public List<OPSlot> findByDoctor(Doctor doctor) {
 		OPSlot example = new OPSlot();
 		
 		example.setDoctor(doctor);
 		return this.findByExample(example);
 	}
 	
 	public List<OPSlot> findByHospital(Hospital hospital) {
 		OPSlot example = new OPSlot();
 		
 		example.setHospital(hospital);
 		return this.findByExample(example);
 	}
 }
