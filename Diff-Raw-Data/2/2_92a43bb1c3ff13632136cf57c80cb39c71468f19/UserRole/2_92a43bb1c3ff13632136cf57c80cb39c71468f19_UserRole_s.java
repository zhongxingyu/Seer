 package net.abbreviations.core.domain;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.NotNull;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.annotations.ForeignKey;
 
 @Entity
@Table(name = "user_role", uniqueConstraints = {@UniqueConstraint(name = "user_role_uk", columnNames = {"user", "role"})})
 public class UserRole
 {
     @Embeddable
     public static class Id implements Serializable
     {
         private static final long serialVersionUID = 5768652673847706713L;
 
         @ManyToOne(optional = false)
         @JoinColumn(name = "user", nullable = false)
         @ForeignKey(name = "user_role_user_fk")
         @NotNull
         private User user;
 
         @Enumerated(EnumType.STRING)
         @Column(name = "role", nullable = false, length = 32)
         @NotNull
         private Role role;
 
         public User getUser()
         {
             return user;
         }
 
         public Role getRole()
         {
             return role;
         }
 
         @Override
         public int hashCode()
         {
             return getRole().hashCode();
         }
 
         @Override
         public boolean equals(Object obj)
         {
             if (obj == this)
                 return true;
             if (!(obj instanceof Id))
                 return false;
             Id id = (Id)obj;
             return StringUtils.equals(getUser().getEmail(), id.getUser().getEmail())
                     && getRole() == id.getRole();
         }
 
         @Override
         public String toString() {
             return getUser().getEmail() + "(" + getRole() + ")";
         }
     }
 
     @EmbeddedId
     private Id id;
 
     public Id getId()
     {
         return id;
     }
 
     @Override
     public String toString() {
         return getId().toString();
     }
 }
