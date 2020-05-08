 package si.kcclass.deliverycost.domain;
 
 public class Address {
	private String street;
 	private String postalCode;
 	private Country country;
 	
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	
 	public String getPostalCode() {
 		return postalCode;
 	}
 	public void setPostalCode(String postalCode) {
 		this.postalCode = postalCode;
 	}
 	public Country getCountry() {
 		return country;
 	}
 	public void setCountry(Country country) {
 		this.country = country;
 	}
 }
