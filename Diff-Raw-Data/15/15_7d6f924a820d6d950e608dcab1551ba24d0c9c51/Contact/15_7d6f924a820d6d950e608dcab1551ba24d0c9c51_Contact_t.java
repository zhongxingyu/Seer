 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.web.organizer.data;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Temporal;
 
 /**
  *
  * @author Gery
  */
 @Entity
 public class Contact implements Serializable {
     @Id
     private long id = 0;
//    @Enumerated(EnumType.STRING)
     private String cfirstName = "";
     private String clastName = "";
 
     @ManyToMany
     private Collection<Person> person;
 
     public Contact() {
         person = new ArrayList<Person>();
     }
 
 
     public Date getCbirthdate() {
         return cbirthdate;
     }
 
     public void setCbirthdate(Date cbirthdate) {
         this.cbirthdate = cbirthdate;
     }
 
     public String getCcountryCode() {
         return ccountryCode;
     }
 
     public void setCcountryCode(String ccountryCode) {
         this.ccountryCode = ccountryCode;
     }
 
     public String getCemail() {
         return cemail;
     }
 
     public void setCemail(String cemail) {
         this.cemail = cemail;
     }
 
     public String getCemail2() {
         return cemail2;
     }
 
     public void setCemail2(String cemail2) {
         this.cemail2 = cemail2;
     }
 
     public String getCfirstName() {
         return cfirstName;
     }
 
     public void setCfirstName(String cfirstName) {
         this.cfirstName = cfirstName;
     }
 
     public String getClastName() {
         return clastName;
     }
 
     public void setClastName(String clastName) {
         this.clastName = clastName;
     }
 
     public String getCmobile() {
         return cmobile;
     }
 
     public void setCmobile(String cmobile) {
         this.cmobile = cmobile;
     }
 
     public String getCmobile2() {
         return cmobile2;
     }
 
     public void setCmobile2(String cmobile2) {
         this.cmobile2 = cmobile2;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
     private String cemail = "";
     private String cemail2 = "";
 
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date cbirthdate;
 
 
     private String cmobile = "";
     private String cmobile2 = "";
     private String ccountryCode = "";
 
 }
