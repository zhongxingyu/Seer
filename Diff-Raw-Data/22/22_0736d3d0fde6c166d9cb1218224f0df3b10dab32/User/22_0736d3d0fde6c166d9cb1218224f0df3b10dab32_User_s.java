 package com.csg.warrior.domain;
 
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.List;
 
 @Entity
 public class User {
     @Id
     @GeneratedValue
    private long userId;
     @Column(unique=true)
     private String username;
     private String password;
     @ManyToOne
     private MobileKey mobileKey;
 
     public long getUserId() {
         return userId;
     }
 
     public void setUserId(long userId) {
         this.userId = userId;
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
 
     public MobileKey getMobileKey() {
         return mobileKey;
     }
 
     public void setMobileKey(MobileKey mobileKey) {
         this.mobileKey = mobileKey;
     }
 
     @Transient
     public UserDetails toSpringSecurityUser() {
         boolean accountEnabled = true;
         boolean accountNonExpired = true;
         boolean credentialsNonExpired = true;
         boolean accountNonLocked = mobileKey.isValid();
         List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
         authorities.add(new SimpleGrantedAuthority("USER"));
         return new org.springframework.security.core.userdetails.User(
                 username,
                 password,
                 accountEnabled,
                 accountNonExpired,
                 credentialsNonExpired,
                 accountNonLocked,
                 authorities
         );
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         User user = (User) o;
 
         if (username != null ? !username.equals(user.username) : user.username != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return username != null ? username.hashCode() : 0;
     }
 }
