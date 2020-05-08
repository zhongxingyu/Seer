 package com.dev.campus.directory;
 
 import java.util.Comparator;
 
 public class Contact {
 
 	private String firstName;
 	private String lastName;
 	private String email;
 	private String tel;
 	private String website;
 
 	public Contact() {
 		firstName = null;
 		lastName = null;
 		email = null;
 		tel = null;
 		website = null;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getTel() {
 		return tel;
 	}
 
 	public void setTel(String tel) {
 		this.tel = tel;
 	}
 
 	public String getWebsite() {
 		return website;
 	}
 	public void setWebsite(String website) {
 		this.website = website;
 	}
 
 	public boolean hasTel(){
 		return (this.tel != null) && !((this.tel).equals("Non renseigne"));
 	}
 
 	public boolean hasEmail(){
		return this.email != null && !(this.email).equals("");
 	}
 
 	public boolean hasWebsite(){
 		return this.website != null;
 	}
 	
 	static class ContactComparator implements Comparator<Contact> {
 		@Override
 		public int compare(Contact c1, Contact c2) {
 			return c1.getLastName().compareTo(c2.getLastName());
 		}
 	}
 
 }
