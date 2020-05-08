 package no.nith;
 
 public class User {
 
 	private String fullName;
 	private String dateOfBirth;
 	private String sex;
 	private String email;
 	private String phoneNumber;
 	private String occupation;
 	
 	public User() {
 	}
 	
 	public User(String fullName, String dateOfBirth, String sex, String email, String phoneNumber, String occupation) {
 		this.setName(fullName);
 		this.setDateOfBirth(dateOfBirth);
 		this.setSex(sex);
 		this.setEmail(email);
 		this.setPhoneNumber(phoneNumber);
 	}
 
 	public String getName() {
 		return fullName;
 	}
 
 	public void setName(String fullName) {
 		this.fullName = fullName;
 	}
 
 	public String getDateOfBirth() {
 		return dateOfBirth;
 	}
 
 	public void setDateOfBirth(String dateOfBirth) {
 		this.dateOfBirth = dateOfBirth;
 	}
 	
 	public String getSex(){
 		return sex;
 	}
 	
 	public void setSex(String sex){
 		this.sex = sex;
 	}
 	
 	public String getEmail(){
 		return email;
 	}
 	
 	public void setEmail(String email){
 		this.email = email;
 	}
 	
 	public String getPhoneNumber(){
 		return phoneNumber;
 	}
 	
 	public void setPhoneNumber(String phoneNumber){
 		this.phoneNumber = phoneNumber;
 	}
 	
 	public String getOccupation(){
 		return occupation;
 	}
 	
 	public void setOccupation(String occupation){
 		this.occupation = occupation;
 	}
 }
