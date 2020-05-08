 package com.algo.webshop.client.authorization;
 
 import javax.validation.constraints.Size;
 
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotBlank;
 
 public class OrderForm {
 	@Size(min = 3, max = 16, message = "     3 ")
 	private String name;
 
 	@Email(message = "  E-mail ")
	@NotBlank(message="  E-mail ")
 	private String email;
 
 	@NotBlank(message=" ")
 	private String phone;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPhone() {
 		return phone;
 	}
 
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 }
