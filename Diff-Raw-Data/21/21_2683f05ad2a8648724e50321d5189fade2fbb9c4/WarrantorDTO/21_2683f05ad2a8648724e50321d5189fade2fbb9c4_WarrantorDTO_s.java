 package at.ac.tuwien.infosys.aic11.data.dtos;
 
 public class WarrantorDTO {
 
 	private long warrantorid = 0l;
 
 	public WarrantorDTO( long id ) {
 		this.warrantorid = id;
 	}
 
 	public long getWarrantorId() {
 		return warrantorid;
 	}
 
 	public void setWarrantorId(long customerid) {
 		this.warrantorid = customerid;
 	}
 }
