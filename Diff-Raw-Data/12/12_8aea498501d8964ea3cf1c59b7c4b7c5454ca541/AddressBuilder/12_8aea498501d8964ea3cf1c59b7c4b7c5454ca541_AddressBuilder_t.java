 package patterns.creational.builder;
 
 public class AddressBuilder implements Builder<Address> {
 	String address1;
 	final String required;
 
 	public AddressBuilder(final String required) {
 		this.required = required;
 	}
 
 	/*
	 * @see patterns.creational.builder.Builder#build()
 	 */
 	public Address build() {
 		return new Address(this);
 	}
 
 	public Builder<Address> streetAddress1(final String address1) {
 		this.address1 = address1;
 		return this;
 	}
 
 }
