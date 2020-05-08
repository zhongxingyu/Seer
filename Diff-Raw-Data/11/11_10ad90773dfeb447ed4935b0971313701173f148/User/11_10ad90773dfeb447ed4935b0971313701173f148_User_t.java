 package com.algo.webshop.common.domain;
 
 import java.io.Serializable;
 
 public class User implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7581819427482830416L;
 	private int id;
 	private String name;
 	private String email;
 	private String phone;
 	private String login;
 	private String pass;
 	
	public User(String name, String email, String phone, String login,
 			String pass) {
 		this.name = name;
 		this.email = email;
 		this.phone = phone;
 		this.login = login;
 		this.pass = pass;
	}
	
	public User() {
		
	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
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
 
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public String getPass() {
 		return pass;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 
 }
