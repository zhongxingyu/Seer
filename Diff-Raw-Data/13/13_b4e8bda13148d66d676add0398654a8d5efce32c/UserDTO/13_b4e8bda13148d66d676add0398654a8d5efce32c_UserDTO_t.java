 package com.okaru.dtomapper.dto;
 
 import java.util.Date;
 
 import com.okaru.dtomapper.annotation.Ignore;
 import com.okaru.dtomapper.annotation.MappedObject;
 import com.okaru.dtomapper.annotation.Rules;
 import com.okaru.dtomapper.rule.NameRule;
 
@Rules({NameRule.class})
 @MappedObject(key="user")
 public class UserDTO {
 	@Ignore
 	private String username;
 	@Ignore
 	private String password;
 	private String firstName;
 	private String lastName;
 	private Date birthDate;
 	
 	public String getUsername() {
 		return username;
 	}
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
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
 	public Date getBirthDate() {
 		return birthDate;
 	}
 	public void setBirthDate(Date birthDate) {
 		this.birthDate = birthDate;
 	}
 }
