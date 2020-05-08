 package com.onb.otp.domain;
 
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="one_time_password_list")
 public class OneTimePasswordList {
 	@Id
	@Column(name="ID")
 	@GeneratedValue
 	private Long id;
 	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "one_time_password_list")
 	public Set<OneTimePassword> passwords;
 
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public Set<OneTimePassword> getPasswords() {
 		return passwords;
 	}
 	public void setPasswords(Set<OneTimePassword> passwords) {
 		this.passwords = passwords;
 	}
 }
