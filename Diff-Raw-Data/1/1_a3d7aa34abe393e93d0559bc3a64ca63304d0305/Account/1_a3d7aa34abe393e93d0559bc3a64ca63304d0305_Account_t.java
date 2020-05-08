 package com.github.kolorobot.icm.account;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.hibernate.validator.constraints.NotBlank;
 
 @SuppressWarnings("serial")
 @Entity
 @Table(name = "account")
 public class Account implements java.io.Serializable {
 	
 	public static final String ROLE_USER = "ROLE_USER";
 	public static final String ROLE_ADMIN = "ROLE_ADMIN";
 	public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";
 	
 	@Id
 	@GeneratedValue
 	private Long id;
 	
 	@NotBlank
 	@Size(max = 49)
 	@Column
 	private String name;
 
 	@Column(unique = true)
 	@Size(max = 50)
 	private String email;
 	
 	@Column
 	private String phone;
 	
 	@JsonIgnore
 	private String password;
 
 	private String role = ROLE_USER;
 	
 	@OneToOne(cascade = CascadeType.ALL)
 	@JoinColumn(name = "address_id")
 	private Address address;
 	
 	@NotNull
 	@Column(name = "operator_id")
 	private String operatorId;
 	
 	protected Account() {
 
 	}
 	
 	public Account(String name, String email, String password, String role) {
 		this.name = name;
 		this.email = email;
 		this.password = password;
 		this.role = role;
 	}
 
 	public Long getId() {
 		return id;
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
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getRole() {
 		return role;
 	}
 
 	public void setRole(String role) {
 		this.role = role;
 	}
 
 	public String getPhone() {
 		return phone;
 	}
 
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 
 	public Address getAddress() {
 		return address;
 	}
 
 	public void setAddress(Address address) {
 		this.address = address;
 	}
 
 	public String getOperatorId() {
 		return operatorId;
 	}
 
 	public void setOperatorId(String operatorId) {
 		this.operatorId = operatorId;
 	}
 }
