 package de.enwida.web.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import de.enwida.web.utils.Constants;
 
 @Entity
 @Table(name = Constants.ROLE_TABLE_NAME, schema = Constants.ROLE_TABLE_SCHEMA_NAME)
 public class Role  implements Serializable{
 
     /**
      * 
      */
     private static final long serialVersionUID = -2772444487718010995L;
     
 	public static final String ROLE_ID = "ROLE_ID";
 	public static final String ROLE_NAME = "ROLE_NAME";
 	public static final String DESCRIPTION = "DESCRIPTION";
     
     @Id
     @Column(name = ROLE_ID)
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private long roleID;
 
     @Column(name = ROLE_NAME)
     private String roleName;
 
     @Column(name = DESCRIPTION) 
     private String description;
     
     @ManyToMany(cascade = CascadeType.ALL)
     @ElementCollection(targetClass = Group.class, fetch = FetchType.EAGER)
    @JoinTable(name = Constants.GROUP_ROLE_TABLE_NAME, schema = Constants.USER_GROUP_TABLE_SCHEMA_NAME, uniqueConstraints = { @UniqueConstraint(columnNames = {
             Role.ROLE_ID, Group.GROUP_ID }) }, joinColumns = { @JoinColumn(name = ROLE_ID, referencedColumnName = ROLE_ID) }, inverseJoinColumns = { @JoinColumn(name = Group.GROUP_ID, referencedColumnName = Group.GROUP_ID) })
     private  List<Group> assignedGroups;
     
 	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "role")
     @ElementCollection(targetClass = Right.class)
     private List<Right> rights;
     
 	/**
 	 * 
 	 */
 	public Role() {
 	}
 
 	/**
 	 * @param roleID
 	 */
 	public Role(long roleID) {
 		this.roleID = roleID;
 	}
 
 	public void addAssignedGroups(Group group) {
         if (assignedGroups == null) {
             assignedGroups = new ArrayList<Group>();
         }
         this.assignedGroups.add(group);
     }
     
     public String getDescription() {
         return description;
     }
     public void setDescription(String description) {
         this.description = description;
     }
 
     public long getRoleID() {
         return roleID;
     }
     public void setRoleID(long roleID) {
         this.roleID = roleID;
     }
     public String getRoleName() {
         return roleName;
     }
     public void setRoleName(String roleName) {
         this.roleName = roleName;
     }
 
 	@Transient
     public List<Group> getAssignedGroups() {
         return assignedGroups;
     }
 
     public void setAssignedGroups(List<Group> assignedGroups) {
         this.assignedGroups = assignedGroups;
     }
     
 	@Transient
     public List<Right> getRights() {
 		return rights;
 	}
 
 	public void setRights(List<Right> rights) {
 		this.rights = rights;
 	}
 
 	@Override
     public String toString() {
         return getRoleName();
     }
 
 }
