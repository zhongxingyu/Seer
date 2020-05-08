 package patterns.creational.builder;
 
 public class Address {
 	private final String required;
 	private final String address1;
 
 	public Address(final AddressBuilder builder) {
 		this.required = builder.required;
 		this.address1 = builder.address1;
 	}
 
 	public String getRequired() {
 		return this.required;
 	}
 
	public Object getAddress1() {
		// TODO Auto-generated method stub
 		return this.address1;
 	}
 
 }
