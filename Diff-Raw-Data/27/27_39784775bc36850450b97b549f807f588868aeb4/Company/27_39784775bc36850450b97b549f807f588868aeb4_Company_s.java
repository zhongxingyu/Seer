 package com.mycompany.entity;
 
 import javax.persistence.*;
 import javax.validation.constraints.Size;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 20/10/12
  * Time: 16:53
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @NamedQueries({
     @NamedQuery(name="Company.findAll", query="SELECT c FROM Company c ORDER BY c.name"),
     @NamedQuery(name="Company.countAll", query="SELECT COUNT(*) FROM Company c")
 })
 public class Company {
 
     /* Fields */
 
    /* Note that under the "rupture principle", we don't include feeds here */
 
     @Id
     @GeneratedValue
     private Long id;
 
     // See http://stackoverflow.com/questions/4582756/unique-constraint-not-enforce-on-schemaexport-of-hibernate
     @Column(unique=true, nullable=false)
     @Size(min=1, max=20)
     private String name;
 
     /* Setter and Getter methods */
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
 }
