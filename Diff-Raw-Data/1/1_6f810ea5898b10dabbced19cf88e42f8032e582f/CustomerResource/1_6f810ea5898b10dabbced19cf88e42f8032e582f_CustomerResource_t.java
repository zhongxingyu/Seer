 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.muni.fi.pa165.survive.rest.client.dto;
  
 import javax.ws.rs.GET;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.annotation.XmlRootElement;
  
 /**
  * Note: for this lab this is mixing business entities and REST operations.
  * Normally entities would have been mapped with a persistence mechanism
  *
  */
 @XmlRootElement
 public class CustomerResource {
  
     private String id;
     private String name;
     private String surname;
     private String occupation;
     private String invention;
     
     public CustomerResource()
     { 
         
     }
     
         public CustomerResource(CustomerResource resource) {
         this.id         = resource.id;
         this.name       = resource.name;
         this.surname    = resource.surname;
         this.occupation = resource.occupation;
         this.invention  = resource.invention;
     }
  
     public CustomerResource(String id, String name, String surname, String occupation, String invention) {
         this.id         = id;
         this.name       = name;
         this.surname    = surname;
         this.occupation = occupation;
         this.invention  = invention;
     }
  
     @GET
     @Produces(MediaType.TEXT_PLAIN)
     public String getPlain() {
         return this.toString();
     }
  
     public String getId() {
         return id;
     }
  
     public String getName() {
         return name;
     }
  
     public String getSurname() {
         return surname;
     }
  
     public String getOccupation() {
         return occupation;
     }
     
     public String getInvention() {
         return invention;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public void setOccupation(String occupation) {
         this.occupation = occupation;
     }
 
     public void setInvention(String invention) {
         this.invention = invention;
     }
     
     
     
  
     @Override
     public String toString() {
         return this.getId() + " " + this.getName() + " " + this.getSurname() + " " + 
                 this.getOccupation() + " " + this.getInvention();
     }
 }
