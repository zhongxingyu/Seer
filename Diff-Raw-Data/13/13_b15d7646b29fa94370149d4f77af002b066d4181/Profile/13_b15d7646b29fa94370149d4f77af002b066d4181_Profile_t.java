 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mydoppelganger.model;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.Date;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author rjb41
  */
 @Entity
 @Table(name = "PROFILE")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Profile.findAll", query = "SELECT p FROM Profile p"),
     @NamedQuery(name = "Profile.findById", query = "SELECT p FROM Profile p WHERE p.id = :id"),
     @NamedQuery(name = "Profile.findByForename", query = "SELECT p FROM Profile p WHERE p.forename = :forename"),
     @NamedQuery(name = "Profile.findBySurname", query = "SELECT p FROM Profile p WHERE p.surname = :surname"),
     @NamedQuery(name = "Profile.findByDob", query = "SELECT p FROM Profile p WHERE p.dob = :dob"),
     @NamedQuery(name = "Profile.findByUsername", query = "SELECT p FROM Profile p WHERE p.profileUser.username = :username")
     })
 public class Profile implements Serializable {
     private static final long serialVersionUID = 1L;
     // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
     @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profileSeqGenerator")
     @SequenceGenerator(name = "profileSeqGenerator", sequenceName = "PROFILE_SEQ", allocationSize=1)
     @Basic(optional = false)
     @Column(name = "ID")
     private Long id;
     @NotNull
     @Size(min=2, max=30)
     @Column(name = "FORENAME")
     private String forename;
     @NotNull
     @Size(min=2, max=50)
     @Column(name = "SURNAME")
     private String surname;
     @Column(name = "DOB")
     @Temporal(TemporalType.DATE)
     private Date dob;
     @OneToOne(cascade = CascadeType.PERSIST, mappedBy = "profile")
    @Valid
     private ProfileUser profileUser;
 
     public Profile() {
     }
 
     public Profile(Long id) {
         this.id = id;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getForename() {
         return forename;
     }
 
     public void setForename(String forename) {
         this.forename = forename;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public Date getDob() {
         return dob;
     }
 
     public void setDob(Date dob) {
         this.dob = dob;
     }
 
 
     public ProfileUser getProfileUser() {
         return profileUser;
     }
 
     public void setProfileUser(ProfileUser profileUser) {
         this.profileUser = profileUser;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Profile)) {
             return false;
         }
         Profile other = (Profile) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.mydoppelganger.model.Profile[ id=" + id + " ]";
     }
     
 }
