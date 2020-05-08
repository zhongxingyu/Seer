 package at.ac.tuwien.dse.fairsurgeries.dto;
 
 import java.io.Serializable;
 
 public class OPSlotDTO implements Serializable{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private int opslotId;
 	
 	public OPSlotDTO(int opslotId){
 		this.setOpslotId(opslotId);
 	}
 
	public int getOpslotId() {
 		return opslotId;
 	}
 
	public void setOpslotId(int opslotId) {
 		this.opslotId = opslotId;
 	}
 
 
 }
