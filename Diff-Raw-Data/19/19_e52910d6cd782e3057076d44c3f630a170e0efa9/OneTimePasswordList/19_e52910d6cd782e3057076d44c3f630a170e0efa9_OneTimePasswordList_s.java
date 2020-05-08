 package com.onb.otp.domain;
 
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @Entity
 @Table(name="one_time_password_list")
 public class OneTimePasswordList {
 	@Id
 	@Column(name="id")
 	@GeneratedValue
 	private Long id;
 	
 	@OneToMany(fetch = FetchType.LAZY, mappedBy = "passwordList")
 	@Cascade({CascadeType.SAVE_UPDATE})
 	private Set<OneTimePassword> passwords;
 	
 	@OneToOne
 	private User user;
 	
 
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
 	public User getUser() {
 		return user;
 	}
 	public void setUser(User user) {
 		this.user = user;
 	}
 }
