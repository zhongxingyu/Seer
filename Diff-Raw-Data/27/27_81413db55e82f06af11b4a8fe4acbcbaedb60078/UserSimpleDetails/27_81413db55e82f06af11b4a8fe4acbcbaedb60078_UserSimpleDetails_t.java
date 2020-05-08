 package org.veloscope.security;
 
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.veloscope.resource.UserEntity;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 public class UserSimpleDetails implements UserDetails {
 
    public static final String REGISTERED_ROLE = "ROLE_USER";
 
     private UserEntity user;
 
     private Collection<GrantedAuthority> grantedAuthority;
 
     public UserSimpleDetails(UserEntity user) {
         this.user = user;
         grantedAuthority = new ArrayList<GrantedAuthority>();
         grantedAuthority.add(new SimpleGrantedAuthority(REGISTERED_ROLE));
     }
 
     @Override
     public Collection<GrantedAuthority> getAuthorities() {
         return grantedAuthority;
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
 
     public UserEntity getUser() {
         return this.user;
     }
 }
