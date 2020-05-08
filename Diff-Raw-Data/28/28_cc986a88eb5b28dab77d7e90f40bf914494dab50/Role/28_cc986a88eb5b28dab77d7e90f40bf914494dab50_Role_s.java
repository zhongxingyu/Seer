 package org.esgf.adminui;
 
 import java.io.Serializable;
 /*
 <roleid>1</roleid>
 <rolename>super</rolename>
 <roledescription>Super User</roledescription>
 */
 public class Role  implements Serializable{
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     private String roleId;
     private String roleName;
     private String roleDescription;
     
     public Role() {
         this.roleId = "roleId";
         this.roleName = "roleName";
         this.roleDescription = "roleDescription";
     }
     
     public Role(String roleId,String roleName,String roleDescription) {
         this.roleId = roleId;
         this.roleName = roleName;
         this.roleDescription = roleDescription;
     }
     
     public String getRoleId() {
         return roleId;
     }
     public void setRoleId(String roleId) {
         this.roleId = roleId;
     }
     public String getRoleName() {
         return roleName;
     }
     public void setRoleName(String roleName) {
         this.roleName = roleName;
     }
     public String getRoleDescription() {
         return roleDescription;
     }
     public void setRoleDescription(String roleDescription) {
         this.roleDescription = roleDescription;
     }
     public static long getSerialversionuid() {
         return serialVersionUID;
     }
     
     public String toString() {
         String str = "";
         
         str += "Role Information" + System.getProperty("line.separator");
         str += "\tRoleId: " + this.roleId + System.getProperty("line.separator");
         str += "\tRoleName: " + this.roleName + System.getProperty("line.separator");
         str += "\tRoleDescription: " + this.roleDescription + System.getProperty("line.separator");
         
         
         return str;
     }
     
 }
