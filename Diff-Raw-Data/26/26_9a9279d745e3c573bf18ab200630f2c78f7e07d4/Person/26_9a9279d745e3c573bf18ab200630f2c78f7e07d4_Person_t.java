 package com.bnrdo.databrowser.domain;
 
 import java.util.Date;
 import java.util.List;
 
 import com.bnrdo.databrowser.mvc.DataBrowserController;
 
 import ca.odell.glazedlists.TextFilterator;
 
 public class Person{
 	private String firstName;
 	private String lastName;
 	private Date birthDay;
 	private int age;
 	private String occupation;
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
 	public Date getBirthDay() {
 		return birthDay;
 	}
 	public void setBirthDay(Date birthDay) {
 		this.birthDay = birthDay;
 	}
 	public int getAge() {
 		return age;
 	}
 	public void setAge(int age) {
 		this.age = age;
 	}
 	public String getOccupation() {
		return occupation;
 	}
 	public void setOccupation(String occupation) {
 		this.occupation = occupation;
 	}
 }
