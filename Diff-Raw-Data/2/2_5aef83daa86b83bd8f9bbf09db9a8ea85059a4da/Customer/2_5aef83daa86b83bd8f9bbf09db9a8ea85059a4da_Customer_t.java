 package domain;
 
 import javax.management.Attribute;
 import javax.management.AttributeList;
 
 public class Customer implements Searchable {
 	
 	private String name, surname, street, city;
 	private int zip;
 
 	public Customer(String name, String surname) {
 		this.name = name;
 		this.surname = surname;
 	}
 	
 	public void setAdress(String street, int zip, String city) {
 		this.street = street;
 		this.zip = zip;
 		this.city = city;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getSurname() {
 		return surname;
 	}
 	
 	public String getFullName() {
		return getSurname() + ", " + getName();
 	}
 
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 
 	public String getStreet() {
 		return street;
 	}
 
 	public void setStreet(String street) {
 		this.street = street;
 	}
 
 	public String getCity() {
 		return city;
 	}
 
 	public void setCity(String city) {
 		this.city = city;
 	}
 
 	public int getZip() {
 		return zip;
 	}
 
 	public void setZip(int zip) {
 		this.zip = zip;
 	}
 	
 	@Override
 	public String toString() {
 		return name + " " + surname + " , " + street + " , " + zip + " " + city;
 	}
 	
 	public String searchTitle() {
 		return getName() + " " + getSurname();
 	}
 
 	public AttributeList searchDetail() {
 		AttributeList list = new AttributeList();
 		list.add(new Attribute("Adresse", (getStreet() == null ? "unbekannt" : getStreet())));
 		list.add(new Attribute("Ort", (getZip() == 0 ? "" : getZip()) + " " + (getCity() == null ? "unbekannt" : getCity())));
 		return list;
 	}
 }
