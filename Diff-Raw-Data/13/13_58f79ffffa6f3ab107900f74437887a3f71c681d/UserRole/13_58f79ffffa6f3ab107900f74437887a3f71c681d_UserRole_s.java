 package org.askil.eshift.user;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 
 @Entity
 @Table( schema = "eshift" )
public class UserRole {
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "id")
 	private Integer id;
 	@Column(name="user_role", length=75, nullable = false)
 	private String userRole;
 	
 	
 	@Column(name="ctrl_user", length=50)
 	private String ctrlUser;
 	@Column(name="ctrl_date")
 	private Date ctrlDate;
 	
 	
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 	public String getUserRole() {
 		return userRole;
 	}
 	public void setUserRole(String userRole) {
 		this.userRole = userRole;
 	}
 	public String getCtrlUser() {
 		return ctrlUser;
 	}
 	public void setCtrlUser(String ctrlUser) {
 		this.ctrlUser = ctrlUser;
 	}
 	public Date getCtrlDate() {
 		return ctrlDate;
 	}
 	public void setCtrlDate(Date ctrlDate) {
 		this.ctrlDate = ctrlDate;
 	}
 
 
 }
