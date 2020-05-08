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
 
	public String getAddress1() {
 		return this.address1;
 	}
 
 }
