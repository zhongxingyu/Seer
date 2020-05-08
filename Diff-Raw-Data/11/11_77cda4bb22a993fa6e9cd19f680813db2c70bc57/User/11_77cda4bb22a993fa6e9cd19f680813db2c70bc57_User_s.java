 package ru.sgu.csit.inoc.deansoffice.domain;
 
 import com.google.common.base.Objects;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import javax.persistence.*;
 import java.util.*;
 
 /**
  * The user class.
  */
@Entity(name = "users")
 public class User implements UserDetails {
 
     @Id
     @GeneratedValue
     private Long id;
 
     private String name;
     private String username;
     private String password;
 
     private boolean accountNonExpired;
     private boolean accountNonLocked;
     private boolean credentialsNonExpired;
     private boolean enabled;
 
     @ElementCollection(fetch = FetchType.EAGER)
     private Set<Authority> authorities;
 
     public Long getId() {
         return id;
     }
 
     public void setId(final Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(final String name) {
         this.name = name;
     }
 
     @Override
     public Collection<GrantedAuthority> getAuthorities() {
         return Collections.unmodifiableSet(new TreeSet<GrantedAuthority>(authorities));
     }
 
     public void setAuthorities(final Set<Authority> authorities) {
         this.authorities = authorities;
     }
 
     @Override
     public String getPassword() {
         return password;
     }
 
     public void setPassword(final String password) {
         this.password = password;
     }
 
     @Override
     public String getUsername() {
         return username;
     }
 
     public void setUsername(final String username) {
         this.username = username;
     }
 
     @Override
     public boolean isAccountNonExpired() {
         return accountNonExpired;
     }
 
     public void setAccountNonExpired(final boolean accountNonExpired) {
         this.accountNonExpired = accountNonExpired;
     }
 
     @Override
     public boolean isAccountNonLocked() {
         return accountNonLocked;
     }
 
     public void setAccountNonLocked(final boolean accountNonLocked) {
         this.accountNonLocked = accountNonLocked;
     }
 
     @Override
     public boolean isCredentialsNonExpired() {
         return credentialsNonExpired;
     }
 
     public void setCredentialsNonExpired(final boolean credentialsNonExpired) {
         this.credentialsNonExpired = credentialsNonExpired;
     }
 
     @Override
     public boolean isEnabled() {
         return enabled;
     }
 
     public void setEnabled(final boolean enabled) {
         this.enabled = enabled;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
 
         if (o == null || o.getClass() != this.getClass()) {
             return false;
         }
 
         final User that = (User) o;
 
         return Objects.equal(this.id, that.id) &&
                 Objects.equal(this.name, that.name) &&
                 Objects.equal(this.username, that.username) &&
                 Objects.equal(this.password, that.password) &&
                 Objects.equal(this.accountNonExpired, that.accountNonExpired) &&
                 Objects.equal(this.accountNonLocked, that.accountNonLocked) &&
                 Objects.equal(this.credentialsNonExpired, that.credentialsNonExpired) &&
                 Objects.equal(this.enabled, that.enabled) &&
                 Objects.equal(this.authorities, that.authorities);
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(
                 id,
                 name,
                 username,
                 password,
                 accountNonExpired,
                 accountNonLocked,
                 credentialsNonExpired,
                 enabled,
                 authorities);
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(User.class)
                 .add("id", id)
                 .add("name", name)
                 .add("username", username)
                 .add("password", password)
                 .add("accountNonExpired", accountNonExpired)
                 .add("accountNonLocked", accountNonLocked)
                 .add("credentialsNonExpired", credentialsNonExpired)
                 .add("enabled", enabled)
                 .add("authorities", authorities)
                 .toString();
     }
 }
