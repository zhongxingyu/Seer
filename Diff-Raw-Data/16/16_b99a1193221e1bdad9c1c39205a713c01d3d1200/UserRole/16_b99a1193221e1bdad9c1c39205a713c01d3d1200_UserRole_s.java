 package com.ai.model;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ishara
  * Date: 1/3/13
  * Time: 2:32 PM
  * To change this template use File | Settings | File Templates.
  */
 import java.util.*;
 import javax.persistence.*;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import org.hibernate.annotations.*;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.Parameter;
 
 
 @Entity
 @Table(name = "user_role")
 public class UserRole implements java.io.Serializable {
 
     private Long userRoleId;
     private Role role;
     private Set<RolePermission> rolePermissions = new HashSet<RolePermission>(0);
     private User user;
 
     public UserRole() {
     }
 
     public UserRole(Long userRoleId, Role roleId) {
         this.userRoleId = userRoleId;
//        this.roleId = roleId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "user_role_id", unique = true, nullable = false)
     public Long getUserRoleId() {
         return userRoleId;
     }
     public void setUserRoleId(Long roleId) {
         this.userRoleId = roleId;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "roleId")
     @Cascade(value = CascadeType.ALL)
     public Set<RolePermission> getRolePermissions() {
         return rolePermissions;
     }
     public void setRolePermissions(Set<RolePermission> rolePermissions) {
         this.rolePermissions = rolePermissions;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     public User getUser() {
         return user;
     }
     public void setUser(User user) {
         this.user = user;
     }
 
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "role_id")
     public Role getRole() {
         return role;
     }
     public void setRole(Role role) {
         this.role = role;
     }
 }
