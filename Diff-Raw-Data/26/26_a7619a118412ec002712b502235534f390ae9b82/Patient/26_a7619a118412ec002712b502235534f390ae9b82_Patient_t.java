 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.entity.user;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonView;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  *
  * @author katsu
  */
 @Entity
 public class Patient extends User{    
     @JsonView(Views.Public.class)
     @Column(nullable = false,length = 255)
     private String name;
     @JsonView(Views.Public.class)
     @Column(nullable = false,length = 1024)
     private String surname;
     @JsonView(Views.Public.class)
     @Column(length = 1024)
     private String surname1;
     @JsonView(Views.Public.class)
     @Temporal(TemporalType.DATE)
     private Date birthDay;
     @JsonView(Views.Public.class)
     @Column(length = 255)
     private String tlf;
     @JsonView(Views.Public.class)
     @Embedded
     private Address address;
     @JsonView(Views.Public.class)
     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private Genre genre;
    @JsonView(Views.Level3.class)
     @ManyToMany(fetch = FetchType.LAZY, mappedBy = "patients")
     private List<Medical> medicals;
    @JsonView(Views.Level3.class)
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "patientId")
     private List<PatientHasProcessInstance> processInstances;
 
     public Genre getGenre() {
         return genre;
     }
 
     public void setGenre(Genre genre) {
         this.genre = genre;
     }
 
     public List<PatientHasProcessInstance> getProcessInstances() {
         return processInstances;
     }
 
     public void setProcessInstances(List<PatientHasProcessInstance> processInstances) {
         this.processInstances = processInstances;
     }
 
     public List<Medical> getMedicals() {
         return medicals;
     }
 
     public void setMedicals(List<Medical> medicals) {
         this.medicals = medicals;
     }
 
     public String getTlf() {
         return tlf;
     }
 
     public void setTlf(String tlf) {
         this.tlf = tlf;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public String getSurname1() {
         return surname1;
     }
 
     public void setSurname1(String surname1) {
         this.surname1 = surname1;
     }
 
     public Date getBirthDay() {
         return birthDay;
     }
 
     public void setBirthDay(Date birthDay) {
         this.birthDay = birthDay;
     }
 
     public Address getAddress() {
         return address;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }
   
     public void addPatientHasProcessInstance(PatientHasProcessInstance instance){
         if (this.getProcessInstances()==null)
             this.setProcessInstances(new ArrayList<PatientHasProcessInstance>());
         this.getProcessInstances().add(instance);
         instance.setPatientId(this);
     }
 }
