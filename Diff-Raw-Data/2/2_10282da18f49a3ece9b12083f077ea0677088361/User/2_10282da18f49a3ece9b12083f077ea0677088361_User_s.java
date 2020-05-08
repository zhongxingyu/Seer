 /*
  * Copyright © 2013 Turkcell Teknoloji Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ttech.cordovabuild.domain.user;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonInclude;
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 @Entity
 @Table(name = "APP_USERS")
 @JsonIgnoreProperties({"authorities"})
 public class User implements Serializable, UserDetails {
 
     /**
      *
      */
     private static final long serialVersionUID = -54880463471029518L;
     @Id
     @GeneratedValue
     private Long id;
     @NotEmpty
     @Basic
     @Column(length = 1024)
     private String name;
     @NotEmpty
     @Basic
     @Column(length = 1024)
     private String surname;
     @NotNull
     @Email
     @Basic
     @Column(length = 1024, unique = true)
     private String email;
     @NotEmpty
     @Basic
     @Column(length = 1024, unique = true)
     private String username;
    @ElementCollection(targetClass = Role.class)
     @Enumerated(EnumType.STRING)
     private Set<Role> roles = new HashSet<>();
     @NotEmpty
     @Basic
     @Column(length = 1024, nullable = false)
     private String password;
     @Basic
     private boolean accountNonExpired = true;
     @Basic
     private boolean enabled = true;
     @Basic
     private boolean credentialsNonExpired = true;
     @Basic
     private boolean accountNonLocked = true;
 
     public User() {
     }
 
     public User(String name, String surname, String email, String username,
                 Set<Role> roles, String password) {
         this.name = name;
         this.surname = surname;
         this.email = email;
         this.username = username;
         this.roles = roles;
         this.password = password;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     @Override
     @JsonInclude(JsonInclude.Include.NON_NULL)
     public String getPassword() {
         return password;
     }
 
 
     public void setAccountNonExpired(boolean accountNonExpired) {
         this.accountNonExpired = accountNonExpired;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     public void setCredentialsNonExpired(boolean credentialsNonExpired) {
         this.credentialsNonExpired = credentialsNonExpired;
     }
 
     public void setAccountNonLocked(boolean accountNonLocked) {
         this.accountNonLocked = accountNonLocked;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     @Override
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public void setRoles(Set<Role> roles) {
         this.roles = roles;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
         return roles;
     }
 
 
     @Override
     @JsonIgnore
     public boolean isAccountNonExpired() {
         return accountNonExpired;
     }
 
     @Override
     @JsonIgnore
     public boolean isAccountNonLocked() {
         return accountNonLocked;
     }
 
     @Override
     @JsonIgnore
     public boolean isCredentialsNonExpired() {
         return credentialsNonExpired;
     }
 
     @Override
     @JsonIgnore
     public boolean isEnabled() {
         return enabled;
     }
 
 
     public void addRole(Role role) {
         roles.add(role);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         User user = (User) o;
 
         if (id != null ? !id.equals(user.id) : user.id != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return id != null ? id.hashCode() : 0;
     }
 }
