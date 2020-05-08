 package com.onlinebox.ecosystem.clients.entity;
 
 import com.onlinebox.ecosystem.projects.entity.*;
 import com.onlinebox.ecosystem.util.entity.IEntity;
 import java.io.*;
 import java.util.*;
 import javax.persistence.*;
 
 @NamedQueries({
     @NamedQuery(name = "Company.findAll", query = "select c from Company c order by c.name"),
     @NamedQuery(name = "Company.findAllByName", query = "select c from Company c WHERE c.name LIKE :clientName ORDER BY c.name")})
 @javax.persistence.EntityListeners(com.onlinebox.ecosystem.util.entity.DateUpdateListener.class)
 @javax.persistence.Entity
 @javax.persistence.Table(name = "t_company")
 public class Company implements IEntity, Serializable, Comparable<Company> {
 
     @javax.persistence.OneToMany(mappedBy = "company", cascade = {CascadeType.ALL})
     @javax.persistence.JoinColumn(name = "t_companyId", referencedColumnName = "Id", nullable = false)
     private List<Contact> contacts;
     @javax.persistence.OneToMany
     @javax.persistence.JoinColumn(name = "t_companyId", referencedColumnName = "Id", nullable = false)
     private List<Document> documents;
     @javax.persistence.OneToMany
     @javax.persistence.JoinColumn(name = "t_companyId", referencedColumnName = "Id", nullable = false)
     private List<ClientNote> notes;
     @javax.persistence.Id
     @javax.persistence.GeneratedValue(strategy = GenerationType.IDENTITY)
     @javax.persistence.Column(name = "Id", nullable = false, length = 19)
     private long id;
     @javax.persistence.Column(name = "Name", nullable = false, length = 100)
     private String name;
     @javax.persistence.Column(name = "Street", length = 100)
     private String street;
     @javax.persistence.Column(name = "ZipCode", length = 10)
     private String zipCode;
     @javax.persistence.Column(name = "City", length = 60)
     private String city;
     @javax.persistence.Column(name = "Email", length = 100)
     private String email;
     @javax.persistence.Column(name = "Phone", length = 20)
     private String phone;
     @javax.persistence.Column(name = "Fax", length = 20)
     private String fax;
     @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
     @javax.persistence.Column(name = "CreateDate", nullable = false)
     private java.util.Date createDate;
     @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
     @javax.persistence.Column(name = "LastUpdateDate", nullable = false)
     private java.util.Date lastUpdateDate;
     @javax.persistence.Column(name = "Logo", length = 100)
     private String logo;
     @javax.persistence.OneToMany(mappedBy = "company", cascade = {CascadeType.ALL})
     @javax.persistence.JoinColumn(name = "t_companyId", referencedColumnName = "Id", nullable = false)
     private List<Project> projects;
     @javax.persistence.Column(name = "Website", length = 100)
     private String website;
     @javax.persistence.Column(name = "PostalBox", length = 50)
     private String postalBox;
 
     public Company() {
         this.setProjects(new ArrayList<Project>());
     }
 
     public List<Contact> getContacts() {
         return this.contacts;
     }
 
     public void setContacts(List<Contact> contacts) {
         this.contacts = contacts;
     }
 
     public List<Document> getDocuments() {
         return this.documents;
     }
 
     public void setDocuments(List<Document> documents) {
         this.documents = documents;
     }
 
     public List<ClientNote> getNotes() {
         return this.notes;
     }
 
     public void setNotes(List<ClientNote> notes) {
         this.notes = notes;
     }
 
     public long getId() {
         return this.id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getStreet() {
         return this.street;
     }
 
     public void setStreet(String street) {
         this.street = street;
     }
 
     public String getZipCode() {
         return this.zipCode;
     }
 
     public void setZipCode(String zipCode) {
         this.zipCode = zipCode;
     }
 
     public String getCity() {
         return this.city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getEmail() {
         return this.email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPhone() {
         return this.phone;
     }
 
     public void setPhone(String phone) {
         this.phone = phone;
     }
 
     public String getFax() {
         return fax;
     }
 
     public void setFax(String fax) {
         this.fax = fax;
     }
 
     public java.util.Date getCreateDate() {
         return this.createDate;
     }
 
     public void setCreateDate(java.util.Date createDate) {
         this.createDate = createDate;
     }
 
     public java.util.Date getLastUpdateDate() {
         return this.lastUpdateDate;
     }
 
     public void setLastUpdateDate(java.util.Date lastUpdateDate) {
         this.lastUpdateDate = lastUpdateDate;
     }
 
     public String getLogo() {
         return this.logo;
     }
 
     public void setLogo(String logo) {
         this.logo = logo;
     }
 
     public List<Project> getProjects() {
         return this.projects;
     }
 
     public void setProjects(List<Project> projects) {
         this.projects = projects;
     }
 
     public String getWebsite() {
         return this.website;
     }
 
     public void setWebsite(String website) {
         this.website = website;
     }
 
     public String toString() {
         String res = this.getName();
         if (this.getCity() != null && !this.getCity().equals("")) {
             res += ", " + this.getCity();
         }
         return res;
     }
 
     public String getPostalBox() {
         return this.postalBox;
     }
 
     public void setPostalBox(String postalBox) {
         this.postalBox = postalBox;
     }
 
     @Override
     public int compareTo(Company o) {
         return this.getName().compareToIgnoreCase(o.getName());
     }
 }
