 /**
  * File: DBUser.java
  * Date: 29.04.2012
  */
 package org.smartsnip.persistence.hibernate;
 
 import javax.persistence.*;
 
 import org.smartsnip.core.User;
 
 /**
  * Database OR mapping class for table User
  * @author littlelion
  * 
  */
 @Entity
 @Table(name = "User")
 class DBUser {
 	@Id
 	@Column(name = "user_name", nullable=false, unique=true)
 	private String nickName;
 
 	@Column(name = "full_name", nullable=true)
 	private String fullName;
 	
 	@Column(name= "email", nullable=true)
 	private String email;
 	
 	@Column(name="user_state", nullable=false)
 	@Enumerated(EnumType.STRING)
 	private User.UserState userState;
 	
 	@Column(name="grant_login", nullable=false)
 	private Boolean grantLogin;
 
 	/**
 	 * 
 	 */
 	DBUser() {
 		super();
 	}
 
 	
 	/**
 	 * @param nickName
 	 * @param fullName
 	 * @param email
 	 * @param userState
 	 * @param grantLogin
 	 */
 	DBUser(String nickName, String fullName, String email,
			User.UserState userState, Boolean grantLogin) {
 		super();
 		this.nickName = nickName;
 		this.fullName = fullName;
 		this.email = email;
 		this.userState = userState;
 		this.grantLogin = grantLogin;
 	}
 
 	/**
 	 * @return the nickName
 	 */
 	public String getNickName() {
 		return this.nickName;
 	}
 
 	/**
 	 * @param nickName the nickName to set
 	 */
 	public void setNickName(String nickName) {
 		this.nickName = nickName;
 	}
 
 	/**
 	 * @return the fullName
 	 */
 	public String getFullName() {
 		return this.fullName;
 	}
 
 	/**
 	 * @param fullName the fullName to set
 	 */
 	public void setFullName(String fullName) {
 		this.fullName = fullName;
 	}
 
 	/**
 	 * @return the email
 	 */
 	public String getEmail() {
 		return this.email;
 	}
 
 	/**
 	 * @param email the email to set
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	/**
 	 * @return the userState
 	 */
 	public User.UserState getUserState() {
 		return this.userState;
 	}
 
 	/**
 	 * @param userState the userState to set
 	 */
 	public void setUserState(User.UserState userState) {
 		this.userState = userState;
 	}
 
 	/**
 	 * @return the grantLogin
 	 */
 	public Boolean getGrantLogin() {
 		return this.grantLogin;
 	}
 
 	/**
 	 * @param grantLogin the grantLogin to set
 	 */
 	public void setGrantLogin(Boolean grantLogin) {
 		this.grantLogin = grantLogin;
 	}
 }
