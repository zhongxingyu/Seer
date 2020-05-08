 package at.ac.tuwien.infosys.aic11.data.dtos;
 
 public class CustomerDTO {
 
 	private long customerid = 0l;
 
 	public CustomerDTO( long id ) {
 		this.customerid = id;
 	}
 
 	public long getCustomerId() {
 		return customerid;
 	}
 
 	public void setCustomerId(long customerid) {
 		this.customerid = customerid;
 	}
 }
