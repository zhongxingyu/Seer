 package com.onb.otp.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 
 @Entity
 @Table(name="one_time_password")
 public class OneTimePassword {
 	@Id
	@Column(name="ID")
 	@GeneratedValue
 	private Long id;
 	
	@Column(name="CODE")
 	private String code;
 
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
 }
