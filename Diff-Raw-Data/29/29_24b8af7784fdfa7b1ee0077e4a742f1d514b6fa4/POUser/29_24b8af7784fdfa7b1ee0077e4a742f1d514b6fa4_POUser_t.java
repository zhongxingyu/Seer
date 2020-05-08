 package info.berryworks.photoorder.model;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 /**
 * Model class which will store the User Items
  * 
  * @author Darrell Dupas
 * 
  */

 @Entity
 public class POUser {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private String name;
 	private String email;  
 	private String telephone;
 	private String mobile;
 	private String address;
 	private String notes;
 	private String groupid;  // start with 1-1 group -  user
 	// -1 is invalid groupid, and is the default (no group)
 
 	public POUser(  String name, String email,String telephone,
 			String mobile,	String address,String notes, String groupid ) {
 		
 		this.name = name;
 		this.email = email;
 		this.telephone = telephone;
 		this.mobile = mobile;
 		this.address = address;
 		this.notes = notes;
 		this.groupid = groupid;
 	}
 	
 	public String toString() {
 		String out = "<pouser><id>" + this.id + "</id>" + 
 		"<name>" + this.name + "</name>" +
 		"<email>" + this.email +"</email>" +
 		"<telephone>" + this.telephone + "</telephone>" +
 		"<mobile>" + this.mobile + "</mobile>" +
 		"<group>" + this.groupid + "</group></pouser>";
 		return out;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 
 	public String getGroupid() {
 		return this.groupid;
 	}
 
 	public void setGroupid(String gid) {
 		this.groupid = gid;
 	}
 
 	
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email){
 		this.email = email;
 	}
 	public String getTelephone() {
 		return telephone;
 	}
 
 	public void setTelephone(String telephone){
 		this.telephone = telephone;
 	}
 	public String getMobile() {
 		return mobile;
 	}
 
 	public void setMobile(String mobile){
 		this.mobile = mobile;
 	}
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address){
 		this.address = address;
 	}
 	public String getNotes() {
 		return notes;
 	}
 
 	public void setNotes(String notes){
 		this.notes = notes;
 	}
 
 
 }
