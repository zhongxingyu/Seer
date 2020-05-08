 package com.fourneth.ims.domain;
 
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.xml.bind.annotation.*;
 
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "Employee", propOrder = {})
 @XmlRootElement
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 @JsonIgnoreProperties(ignoreUnknown = true)
 @Entity(name = "EMPLOYEE")
 public class Employee {
 
     @Id
     @Column(name="ID")
     @GeneratedValue
     private int id;
     @Column(name="FIRST_NAME")
     private String firstName;
     @Column(name="LAST_NAME")
     private String lastName;
 
 
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
 }
