 package com.metal.model;
 
 import java.io.Serializable;
 import javax.persistence.*;
 
 /**
  * The persistent class for the users database table.
  * 
  */
 @Entity
 @Table(name = "users")
 @Inheritance(strategy = InheritanceType.JOINED)
 @DiscriminatorColumn(name = "user_type")
 @NamedQueries({ @NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM User u"),
 		@NamedQuery(name = "User.findAllByType", query = "SELECT u FROM User u WHERE TYPE(u) = :type") })
 public class User implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(unique = true, nullable = false, length = 50)
 	private String username;
 
//	@Column(nullable = false, length = 50)
//	private String group_name;
 
 	@Column(nullable = false, length = 50)
 	private String password;
 
	// @Column(name = "user_type", nullable = false, length = 50)
	// private String userType;
 
 	// bi-directional one-to-one association to Admin
 	// @OneToOne(mappedBy="user")
 	// private Admin admin;
 
 	// bi-directional one-to-one association to Jury
 	// @OneToOne(mappedBy="user")
 	// private Jury jury;
 
 	// bi-directional one-to-one association to Participant
 	// @OneToOne(mappedBy="user")
 	// private Participant participant;
 
 	public User() {
 	}
 
 	public String getUsername() {
 		return this.username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
	// public String getGroup() {
	// return this.group_name;
	// }
	//
	// public void setGroup(String group_name) {
	// this.group_name = group_name;
	// }
 
 	public String getPassword() {
 		return this.password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	// public String getUserType() {
 	// return this.userType;
 	// }
 	//
 	// public void setUserType(String userType) {
 	// this.userType = userType;
 	// }
 
 	// public Admin getAdmin() {
 	// return this.admin;
 	// }
 	//
 	// public void setAdmin(Admin admin) {
 	// this.admin = admin;
 	// }
 	//
 	// public Jury getJury() {
 	// return this.jury;
 	// }
 	//
 	// public void setJury(Jury jury) {
 	// this.jury = jury;
 	// }
 	//
 	// public Participant getParticipant() {
 	// return this.participant;
 	// }
 	//
 	// public void setParticipant(Participant participant) {
 	// this.participant = participant;
 	// }
 
 }
