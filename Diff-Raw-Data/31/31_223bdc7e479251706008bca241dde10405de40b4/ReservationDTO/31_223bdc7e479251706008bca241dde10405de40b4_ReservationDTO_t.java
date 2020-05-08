 package at.ac.tuwien.dse.fairsurgeries.dto;
 
 import java.io.Serializable;
 
 public class ReservationDTO implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private OPSlotDTO opslot;
 	private PatientDTO patient;
 	
 	public ReservationDTO(OPSlotDTO opslot, PatientDTO pationt)
 	{
 		this.setOpslot(opslot);
 		this.setPatient(pationt);
 	}
	public OPSlotDTO getOpslot() {
 		return opslot;
 	}
	public void setOpslot(OPSlotDTO opslot) {
 		this.opslot = opslot;
 	}
	public PatientDTO getPatient() {
 		return patient;
 	}
	public void setPatient(PatientDTO patient) {
 		this.patient = patient;
 	}
 }
