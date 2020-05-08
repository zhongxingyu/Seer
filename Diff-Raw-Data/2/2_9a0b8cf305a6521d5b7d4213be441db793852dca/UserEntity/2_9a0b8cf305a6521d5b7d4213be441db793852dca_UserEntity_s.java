 package org.motechproject.carereporting.domain;
 
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 @Entity
 @Table(name = "care_user")
 @AttributeOverrides({
         @AttributeOverride(name = "id", column = @Column(name = "user_id"))
 })
 public class UserEntity extends AbstractEntity implements UserDetails {
 
     @Column(name = "username", unique = true)
     private String username;
 
     @Column(name = "password")
     private String password;
 
     @Column(name = "salt")
     private String salt;
 
     @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "user_id") },
             inverseJoinColumns = { @JoinColumn(name = "role_id") })
     private Set<RoleEntity> roles;
 
     public UserEntity() {
     }
 
     public UserEntity(String username, String password) {
         this.username = username;
         this.password = password;
         this.roles = new HashSet<>();
         this.salt = UUID.randomUUID().toString();
     }
 
     public UserEntity(String username, String password, Set<RoleEntity> roles) {
         this.username = username;
         this.password = password;
         this.roles = roles;
         this.salt = UUID.randomUUID().toString();
     }
 
     public Set<RoleEntity> getRoles() {
         return roles;
     }
 
     public void setRoles(Set<RoleEntity> roles) {
         this.roles = roles;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
         List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
         for (RoleEntity role: roles) {
             for (PermissionEntity permission: role.getPermissions()) {
                 grantedAuthorities.add(new SimpleGrantedAuthority(permission.getName()));
             }
         }
         return grantedAuthorities;
     }
 
     @Override
     public String getPassword() {
         return password;
     }
 
     @Override
     public String getUsername() {
         return username;
     }
 
     @Override
     public boolean isAccountNonExpired() {
         return true;
     }
 
     @Override
     public boolean isAccountNonLocked() {
         return true;
     }
 
     @Override
     public boolean isCredentialsNonExpired() {
         return true;
     }
 
     @Override
     public boolean isEnabled() {
         return true;
     }
 
     public String getSalt() {
         return salt;
     }
 
     public void setSalt(String salt) {
         this.salt = salt;
     }
 }
