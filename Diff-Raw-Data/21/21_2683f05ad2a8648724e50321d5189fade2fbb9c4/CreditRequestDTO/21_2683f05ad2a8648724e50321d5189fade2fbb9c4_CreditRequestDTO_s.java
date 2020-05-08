 package at.ac.tuwien.infosys.aic11.data.dtos;
 
 public class CreditRequestDTO {
 
 	private long creditrequestid = 0l;
 
 	public CreditRequestDTO( long id ) {
 		this.creditrequestid = id;
 	}
 
 	public long getCreditRequestId() {
 		return creditrequestid;
 	}
 
 	public void setCreditRequestId(long creditrequestid ) {
 		this.creditrequestid = creditrequestid;
 	}
 }
