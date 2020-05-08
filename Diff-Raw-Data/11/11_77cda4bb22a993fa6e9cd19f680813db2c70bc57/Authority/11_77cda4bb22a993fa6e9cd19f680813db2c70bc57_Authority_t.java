 package ru.sgu.csit.inoc.deansoffice.domain;
 
 import com.google.common.base.Objects;
 import org.apache.commons.lang.Validate;
 import org.springframework.security.core.GrantedAuthority;
 
 import javax.persistence.*;
 
 /**
  * The authority class.
  */
@Entity
@Table(name = "authorities")
 public class Authority implements GrantedAuthority, Comparable<GrantedAuthority> {
 
     @Id
     @GeneratedValue
     private Long id;
     private String role;
 
     public Authority() {
     }
 
     public Authority(final String role) {
         this.role = role;
     }
 
     @Override
     public String getAuthority() {
         return role;
     }
 
     public String getRole() {
         return role;
     }
 
     public void setRole(final String role) {
         this.role = role;
     }
 
     @Override
     public int compareTo(final GrantedAuthority other) {
         Validate.notNull(other);
 
         if (other.getAuthority() == null) {
             return -1;
         } else if (this.getAuthority() == null) {
             return 1;
         } else {
             return this.getAuthority().compareTo(other.getAuthority());
         }
     }
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
 
         if (o instanceof String) {
             return o.equals(this.role);
         }
 
         if (o instanceof GrantedAuthority) {
             GrantedAuthority that = (GrantedAuthority)o;
 
             return this.role.equals(that.getAuthority());
         }
 
         return false;
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(role);
     }
 
     @Override
     public String toString() {
         return role;
     }
 }
