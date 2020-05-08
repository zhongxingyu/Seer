 package com.mycompany.entity;
 
 import javax.persistence.*;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 20/10/12
  * Time: 16:54
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 public class User {
 
     /* Fields */
 
    /* Note that under the "inversion principle", we don't include posts here */
    /* By the "rupture principle" we use the Subscription entity to "link" with feeds */
 
     @Id
     @GeneratedValue
     private Long id;
 
     // See http://stackoverflow.com/questions/4582756/unique-constraint-not-enforce-on-schemaexport-of-hibernate
     @Column(unique=true, nullable=false)
     @Size(min=1, max=20)
     private String name;
 
     @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
             +"[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
             +"(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
             message="{invalid.email}")
     @Column(unique=true, nullable=false)
     private String email;
 
     /* Setter and Getter methods */
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
 }
