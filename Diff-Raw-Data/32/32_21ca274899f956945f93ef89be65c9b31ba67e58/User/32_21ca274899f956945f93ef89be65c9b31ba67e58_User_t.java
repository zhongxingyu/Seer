 package com.domain;
 
 import com.persistence.EmailAddressType2;
import com.sun.istack.internal.NotNull;
 import org.hibernate.annotations.Type;
 import org.hibernate.annotations.TypeDef;
 import org.springframework.data.jpa.domain.AbstractPersistable;
 
 import javax.persistence.*;
 import java.util.Date;
 
 
 /**
  * @author Stanislav Kurilin
  */
 @Entity
 @Table(name = "USERS")
 @TypeDef(name = "com.domain.EmailAddress", typeClass = EmailAddressType2.class)
 public class User extends AbstractPersistable<Long> {
     private String name;
     @Type(type = "com.domain.EmailAddress")
     private EmailAddress email;
     @Temporal(TemporalType.DATE)
    @NotNull
     private Date birthday;
 
     public User() {
     }
 
     public User(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public EmailAddress getEmail() {
         return email;
     }
 
     public void setEmail(EmailAddress email) {
         this.email = email;
     }
 
     public Date getBirthday() {
         return birthday;
     }
 
     public void setBirthday(Date birthday) {
         this.birthday = birthday;
     }
     public void setId(Long id) {
         super.setId(id);
     }
 }
