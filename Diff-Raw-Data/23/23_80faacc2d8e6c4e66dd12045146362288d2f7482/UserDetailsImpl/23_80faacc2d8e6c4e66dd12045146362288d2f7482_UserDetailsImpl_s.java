 package org.cognitor.server.platform.security;
 
 import org.cognitor.server.platform.user.domain.User;
 import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * User: patrick
  * Date: 26.11.12
  */
 public class UserDetailsImpl implements UniqueKeyUserDetails {
     public static final String ROLE_USER = "ROLE_USER";
     private User user;
 
     public UserDetailsImpl(User user) {
         this.user = user;
     }
 
     @Override
     public Collection<GrantedAuthority> getAuthorities() {
         List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(1);
         authorities.add(new SimpleGrantedAuthority(ROLE_USER));
         return authorities;
     }
 
     @Override
     public String getUniqueKey() {
         return this.user.getId().toString();
     }
 
     @Override
     public String getPassword() {
         return user.getPassword();
     }
 
     @Override
     public String getUsername() {
         return user.getEmail();
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
 }
