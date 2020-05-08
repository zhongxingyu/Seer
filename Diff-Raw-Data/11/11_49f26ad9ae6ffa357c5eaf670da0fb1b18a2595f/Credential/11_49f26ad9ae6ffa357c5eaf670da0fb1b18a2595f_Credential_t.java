 package com.example.routerCPR.domain;
 
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Credentials")
public class Credential extends Model {
	@Column(name = "user")
 	private final String user;
	@Column(name = "password")
 	private final String password;
 	
 	public Credential(String user, String password) {
 		this.user = user;
 		this.password = password;
 	}
 	
 	public String getUser() {
 		return user;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 }
