 package com.edify.model;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotBlank;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 import org.springframework.security.crypto.password.PasswordEncoder;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author <a href="https://github.com/jarias">jarias</a>
  */
 @Entity
 @Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
 public class User implements UserDetails {
     public static final String[] TABLE_COLUMNS = {"id", "firstName", "lastName", "username"};
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     @NotBlank
     @NotNull
     @Email
     private String username;
     @NotNull
     @NotBlank
     @Size(min = 6)
     private String password;
     @NotNull
     @NotBlank
     private String firstName;
     @NotNull
     @NotBlank
     private String lastName;
     @ManyToMany(fetch = FetchType.EAGER)
     @JoinTable(name = "users_roles",
             joinColumns = {@JoinColumn(name = "user_id")},
             inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private List<Role> roles = new ArrayList<Role>();
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
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
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public List<Role> getRoles() {
         return roles;
     }
 
     public void setRoles(List<Role> roles) {
         this.roles = roles;
     }
 
     @PreUpdate
     @PrePersist
     private void encodePassword() {
         PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
         if (StringUtils.isNotBlank(password) && !password.startsWith("$2a$10$")) {
             password = passwordEncoder.encode(password);
         }
     }
 
     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
         return roles;
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
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         User user = (User) o;
 
         return new EqualsBuilder().append(username, user.getUsername()).isEquals();
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(username).toHashCode();
     }
 }
