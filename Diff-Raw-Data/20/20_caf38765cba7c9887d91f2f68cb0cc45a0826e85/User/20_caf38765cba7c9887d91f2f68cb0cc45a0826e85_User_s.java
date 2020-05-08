 package reevent.domain;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import javax.persistence.*;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Set;
 
 @Entity
 public class User extends EntityBase {
     @Column(unique=true, nullable=false)
     String username;
 
     /**
      * Salted MD5 hash of the password.
      */
     @Column(nullable=false)
     String passwordHash;
 
 	String firstName;
 	
 	String lastName;
 	
 	Date dayOfBirth;
 
    boolean enabled;
 
     @ElementCollection(fetch = FetchType.EAGER)
     @Enumerated(EnumType.STRING)
     Set<UserRole> roles = Collections.singleton(UserRole.USER);
 
     public User() {
     }
 
     public User(String username, String passwordHash) {
         this.username = username;
         this.passwordHash = passwordHash;
     }
 
     public User(String username, String passwordHash, Set<UserRole> roles, String firstName, String lastName, Date dayOfBirth) {
         this.dayOfBirth = dayOfBirth;
         this.firstName = firstName;
         this.lastName = lastName;
         this.passwordHash = passwordHash;
         this.roles = roles;
         this.username = username;
     }
 
     public String getPasswordHash() {
         return passwordHash;
     }
 
     public void setPasswordHash(String passwordHash) {
         this.passwordHash = passwordHash;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public Set<UserRole> getRoles() {
         return roles;
     }
 
     public void setRoles(Set<UserRole> roles) {
         this.roles = roles;
     }
     
 
     public Date getDayOfBirth() {
 		return dayOfBirth;
 	}
 
 	public void setDayOfBirth(Date dayOfBirth) {
 		this.dayOfBirth = dayOfBirth;
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
 
     public boolean isEnabled() {
         return enabled;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this).
                 append("username", username).
                 append("passwordHash", passwordHash).
                 append("dayOfBirth", dayOfBirth).
                 append("roles", roles).
                 append("firstName", firstName).
                 append("lastName", lastName).
                 appendSuper(super.toString()).
                 toString();
     }
 }
