 package org.xezz.timeregistration.dao;
 
 import org.springframework.beans.factory.annotation.Configurable;
 import org.xezz.timeregistration.model.Coworker;
 
 import java.util.Date;
 
 /**
  * User: Xezz
  * Date: 30.05.13
  * Time: 22:50
  * Coworker Data Access Object for RESTful services
  */
 @Configurable
 public class CoworkerDAO {
     private Long coworkerId;
     private String firstName;
     private String lastName;
     private Date creationDate;
     private Date lastUpdatedDate;
 
     public CoworkerDAO(Long coworkerId, String firstName, String lastName, Date creationDate, Date lastUpdatedDate) {
         this.coworkerId = coworkerId;
         this.firstName = firstName;
         this.lastName = lastName;
         if (creationDate != null) {
             this.creationDate = new Date(creationDate.getTime());
         }
         if (lastUpdatedDate != null) {
             this.lastUpdatedDate = new Date(lastUpdatedDate.getTime());
         }
     }
 
     /**
      * Create a DAO from a c
      *
      * @param c the Coworker to use
      */
     public CoworkerDAO(Coworker c) {
        if (c == null) {
            throw new IllegalArgumentException("Coworker must not be null");
        }
         this.coworkerId = c.getCoworkerId();
         this.firstName = c.getFirstName();
         this.lastName = c.getLastName();
         if (c.getCreationDate() != null) {
             this.creationDate = new Date(c.getCreationDate().getTime());
         }
         if (c.getLastUpdatedDate() != null) {
             this.lastUpdatedDate = new Date(c.getLastUpdatedDate().getTime());
         }
     }
 
     /**
      * Just in case default constructor
      */
     public CoworkerDAO() {
     }
 
     public Long getCoworkerId() {
         return coworkerId;
     }
 
     public void setCoworkerId(Long coworkerId) {
         if (this.coworkerId != null) {
             throw new IllegalArgumentException("You are not allowed to change the ID once it is set");
         }
         this.coworkerId = coworkerId;
     }
 
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
 
     public Date getCreationDate() {
         if (creationDate == null) {
             return null;
         }
         return new Date(creationDate.getTime());
     }
 
     public void setCreationDate(Date creationDate) {
         if (creationDate == null) {
             throw new IllegalArgumentException("Date must not be null");
         }
         this.creationDate = new Date(creationDate.getTime());
     }
 
     public Date getLastUpdatedDate() {
         if (lastUpdatedDate == null) {
             return null;
         }
         return new Date(lastUpdatedDate.getTime());
     }
 
     public void setLastUpdatedDate(Date lastUpdatedDate) {
         if (lastUpdatedDate == null) {
             throw new IllegalArgumentException("Date must not be null");
         }
         this.lastUpdatedDate = new Date(lastUpdatedDate.getTime());
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof CoworkerDAO)) return false;
 
         CoworkerDAO coworker = (CoworkerDAO) o;
 
         if (coworkerId != null ? !coworkerId.equals(coworker.coworkerId) : coworker.coworkerId != null) return false;
         if (creationDate != null ? !creationDate.equals(coworker.creationDate) : coworker.creationDate != null)
             return false;
         if (!firstName.equals(coworker.firstName)) return false;
         if (!lastName.equals(coworker.lastName)) return false;
         if (lastUpdatedDate != null ? !lastUpdatedDate.equals(coworker.lastUpdatedDate) : coworker.lastUpdatedDate != null)
             return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = coworkerId != null ? coworkerId.hashCode() : 0;
         result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
         result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
         result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
         result = 31 * result + (lastUpdatedDate != null ? lastUpdatedDate.hashCode() : 0);
         return result;
     }
 }
