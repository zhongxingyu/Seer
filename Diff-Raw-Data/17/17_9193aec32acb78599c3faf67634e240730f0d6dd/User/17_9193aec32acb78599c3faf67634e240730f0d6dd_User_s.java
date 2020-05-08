 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.entity.user;
 
 /*
  * #%L
  * Cleia
  * %%
  * Copyright (C) 2013 Abada Servicios Desarrollo (investigacion@abadasoft.com)
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import com.fasterxml.jackson.annotation.JsonView;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 /**
  *
  * @author katsu
  */
 @Entity
 @Table(name = "user1")
 public class User implements UserDetails {
 
     @JsonView({Views.Public.class,Views.Case1.class})
     @javax.persistence.Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
     @JsonView({Views.Public.class,Views.Case1.class})
     @Column(nullable = false, unique = true, length = 512)
     private String username;    
     @JsonView(Views.Hide.class)
     @Column(nullable = false, length = 1024)
     private String password;
     @JsonView(Views.Public.class)
     @Column(nullable = false)
     private boolean accountNonExpired;
     @JsonView(Views.Public.class)
     @Column(nullable = false)
     private boolean accountNonLocked;
     @JsonView(Views.Public.class)
     @Column(nullable = false)
     private boolean credentialsNonExpired;
     @JsonView({Views.Public.class,Views.Case1.class})
     @Column(nullable = false)
     private boolean enabled;
     @JsonView(Views.Level1.class)
     @ManyToMany
     @JoinTable(name = "user_has_role",
             joinColumns = {
         @JoinColumn(nullable = false, name = "user_id", referencedColumnName = "id")},
             inverseJoinColumns = {
         @JoinColumn(nullable = false, name = "role_id", referencedColumnName = "authority")})
     private List<Role> roles;
    @JsonView(Views.Level1.class)
     @ManyToMany
     @JoinTable(name = "user_has_group",
             joinColumns = {
         @JoinColumn(nullable = false, name = "user_id", referencedColumnName = "id")},
             inverseJoinColumns = {
         @JoinColumn(nullable = false, name = "group_id", referencedColumnName = "value1")})
     private List<Group> groups;
     @JsonView({Views.Level1.class,Views.MedicalData.class})
     @OneToMany(mappedBy = "user")
     private List<Id> ids;
 
     public List<Id> getIds() {
         return ids;
     }
 
     public void setIds(List<Id> ids) {
         this.ids = ids;
     }
 
     @JsonView(Views.Level1.class)
     public Collection<? extends GrantedAuthority> getAuthorities() {
         return getRoles();
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public List<Role> getRoles() {
         return roles;
     }
 
     public void setRoles(List<Role> roles) {
         this.roles = roles;
     }
 
     public List<Group> getGroups() {
         return groups;
     }
 
     public void setGroups(List<Group> groups) {
         this.groups = groups;
     }
 
     public boolean isAccountNonExpired() {
         return accountNonExpired;
     }
 
     public void setAccountNonExpired(boolean accountNonExpired) {
         this.accountNonExpired = accountNonExpired;
     }
 
     public boolean isAccountNonLocked() {
         return accountNonLocked;
     }
 
     public void setAccountNonLocked(boolean accountNonLocked) {
         this.accountNonLocked = accountNonLocked;
     }
 
     public boolean isCredentialsNonExpired() {
         return credentialsNonExpired;
     }
 
     public void setCredentialsNonExpired(boolean credentialsNonExpired) {
         this.credentialsNonExpired = credentialsNonExpired;
     }
 
     public boolean isEnabled() {
         return enabled;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     public void addGroup(Group group) {
         if (this.groups == null) {
             this.setGroups(new ArrayList<Group>());
         }
         if (!this.groups.contains(group)) {
             this.groups.add(group);
         }
         if (group.getUsers() == null) {
             group.setUsers(new ArrayList<User>());
         }
         if (!group.getUsers().contains(this)) {
             group.getUsers().add(this);
         }
     }
 
     public void addRole(Role role) {
         if (this.roles == null) {
             this.setRoles(new ArrayList<Role>());
         }
         if (!this.roles.contains(role)) {
             this.roles.add(role);
         }
         if (role.getUsers() == null) {
             role.setUsers(new ArrayList<User>());
         }
         if (!role.getUsers().contains(this)) {
             role.getUsers().add(this);
         }
 
 
     }
     
     public void addId(Id id){
         
         if(this.getIds() == null){
             
             this.ids = new ArrayList<Id>();
         }
         id.setUser(this);
         this.ids.add(id);
     }
 }
