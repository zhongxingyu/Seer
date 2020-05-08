 package com.onb.otp.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 
 @Entity
 @Table(name="one_time_password")
 public class OneTimePassword {
 	@Id
	@Column(name="id")
 	@GeneratedValue
 	private Long id;
 	
	@Column(name="code")
 	private String code;
	
	@ManyToOne
    @JoinColumn(name="password_list_id")
    private OneTimePasswordList passwordList; 
 
 	public OneTimePassword() {}
 	
 	public OneTimePassword(String code) {
 		this.code = code;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public String getCode() {
 		return code;
 	}
 	public void setCode(String code) {
 		this.code = code;
 	}
	public OneTimePasswordList getPasswordList() {
		return passwordList;
	}
	public void setPasswordList(OneTimePasswordList passwordList) {
		this.passwordList = passwordList;
	}
	
 }
