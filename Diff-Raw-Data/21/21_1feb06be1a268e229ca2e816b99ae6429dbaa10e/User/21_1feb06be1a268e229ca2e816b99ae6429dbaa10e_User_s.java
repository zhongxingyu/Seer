 package com.pmf.entities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.*;
 
 import com.pmf.util.EjbConstants;
 
 @Entity
 @Table(name = EjbConstants.TABLE_USERS)
 @NamedQueries( {
 		@NamedQuery(name = "User.findByLoginNPassword", query = "SELECT d FROM User d where d.login = :login and d.password = :password"),
 		@NamedQuery(name = "User.findByLogin", query = "SELECT d FROM User d where d.login = :login"),
 		@NamedQuery(name = "User.findByUserId", query = "SELECT d FROM User d where d.id = :id") })
 public class User implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	public User() {
 		setContact(new Contact());
 	}
 
 	@Id
 	@Column(name = "id")
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private int id;
 	// @Column(name="user_type")
 
 	@ManyToOne
 	@JoinColumn(name = "user_type", nullable = false, updatable = false)
 	private UserType userType;
 	@Column(name = "login")
 	private String login;
 	@Column(name = "salutation")
 	private String salutation;
 	@Column(name = "name")
 	private String name;
 	@Column(name = "birthdate")
 	private String birthdate;
 	@Column(name = "password")
 	private String password;
 	@Column(name = "email")
 	private String email;
 	@Column(name = "last_login")
 	private String lastLogin;
 	@Column(name = "referal")
 	private String referal;
 	@Column(name = "status")
 	private int status;
 
 	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "UserContactMap", joinColumns = { @JoinColumn(name = "id", referencedColumnName = "userid", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "contactid", referencedColumnName = "contactid", nullable = false, updatable = false) })
 	private Contact contact;
 
 
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public UserType getUserType() {
 		return userType;
 	}
 
 	public void setUserType(UserType userType) {
 		this.userType = userType;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public String getSalutation() {
 		return salutation;
 	}
 
 	public void setSalutation(String salutation) {
 		this.salutation = salutation;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getBirthdate() {
 		return birthdate;
 	}
 
 	public void setBirthdate(String birthdate) {
 		this.birthdate = birthdate;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getLastLogin() {
 		return lastLogin;
 	}
 
 	public void setLastLogin(String string) {
 		this.lastLogin = string;
 	}
 
 	public String getReferal() {
 		return referal;
 	}
 
 	public void setReferal(String referal) {
 		this.referal = referal;
 	}
 
 	public Contact getContact() {
 		return contact;
 	}
 
 	public void setContact(Contact contact) {
 		this.contact = contact;
 		this.contact.setUser(this);
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
 }
