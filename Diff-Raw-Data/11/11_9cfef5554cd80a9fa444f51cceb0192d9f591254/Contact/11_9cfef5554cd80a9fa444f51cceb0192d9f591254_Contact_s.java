 package jtan325.contactmanager;
 
 import java.util.Comparator;
 
 public class Contact {
 	
 	private int id=0;
 	private String firstName="";
 	private String lastName="";
 	private String mobile="";
 	private String home="";
 	private String work="";
 	private String email="";
 	private String address="";
 	private String DOB="";
 	private String photoPath="";
 	
 	
 	//dummy constructor for easy adding of contacts
 //	public Contact (int a, String b){
 //		id=a;
 //		firstName=b;
 //	}
 //	
 //	
 //
 //	public Contact (int a, String b, String c, String d){
 //		id=a;
 //		firstName=b;
 //		lastName=c;
 //		mobile=d;
 //	}
 	
 	
 	
 	//official constructor 
 	public Contact(int a, String b, String c, String d, String e, String f, String g, String h, String i, String path){
 		id=a;
 		firstName=b;
 		lastName = c;
 		mobile=d;
 		home=e;
 		work=f;
 		email=g;
 		address=h;
 		DOB=i;
 		photoPath=path;
 		
 	}
 
 	
 	
 	
 	//getters and setters for displaying and changing contact fields
 	
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	
 	public String getPhotoPath() {
 		return photoPath;
 	}
 	
	public void setphotoPath(String s) {
 		photoPath = s;
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
 
 
 	public String getMobile() {
 		return mobile;
 	}
 
 
 	public void setMobile(String mobile) {
 		this.mobile = mobile;
 	}
 
 
 	public String getHome() {
 		return home;
 	}
 
 
 	public void setHome(String home) {
 		this.home = home;
 	}
 
 
 	public String getWork() {
 		return work;
 	}
 
 
 	public void setWork(String work) {
 		this.work = work;
 	}
 
 
 	public String getEmail() {
 		return email;
 	}
 
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 
 	public String getAddress() {
 		return address;
 	}
 
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 
 	public String getDOB() {
 		return DOB;
 	}
 
 
 	public void setDOB(String dOB) {
 		DOB = dOB;
 	}
 	
 	
 	
 	public String toString(){
 		return firstName;
 		
 	}
 	
 	public boolean equals(Contact contact) { 
 		if (this.getId() == contact.getId()) { 
 			return true; 
 		} else{
 			return false; 
 		}
 	}
 	
 	//first name comparator
 	public static class firstNameComparator implements Comparator<Contact>{
 
 		@Override
 		public int compare(Contact first, Contact second) {
 			
 			return first.getFirstName().compareTo(second.getFirstName());
 		}
 	}
 	
 	//last name comparator
 	public static class lastNameComparator implements Comparator<Contact>{
 
 		@Override
 		public int compare(Contact first, Contact second) {
 			
 			return first.getLastName().compareTo(second.getLastName());
 		}
 	}
 	
 	//phone number comparator
 		public static class phoneComparator implements Comparator<Contact>{
 
 			@Override
 			public int compare(Contact first, Contact second) {
 				
 				return first.getMobile().compareTo(second.getMobile());
 			}
 		}
 	
 }
