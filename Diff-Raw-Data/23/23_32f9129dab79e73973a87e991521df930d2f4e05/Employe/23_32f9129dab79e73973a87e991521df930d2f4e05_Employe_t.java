 
 package com.AlphaDevs.Web.Entities;
 
 import java.io.Serializable;
import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToOne;
 
 /**
  *
  * @author Mihindu Gajaba Karunarathne 
  * 
  * Alpha Development Team ( Pvt ) Ltd
  * www.AlphaDevs.com
  * Info@AlphaDevs.com
  * 
  */
 
 @Entity
 public class Employe implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE)
     private Long id;
     private String empName;
     private String empCode;
 
    @OneToOne(cascade = CascadeType.PERSIST)
     private Logger logger;
     
     public Employe() {
     }
 
     public Employe(String empName, String empCode, Logger logger) {
         this.empName = empName;
         this.empCode = empCode;
         this.logger = logger;
     }
 
     
     public Employe(String empName, String empCode) {
         this.empName = empName;
         this.empCode = empCode;
     }
 
     public String getEmpName() {
         return empName;
     }
 
     public void setEmpName(String empName) {
         this.empName = empName;
     }
 
     public String getEmpCode() {
         return empCode;
     }
 
     public void setEmpCode(String empCode) {
         this.empCode = empCode;
     }
 
     public Logger getLogger() {
         return logger;
     }
 
     public void setLogger(Logger logger) {
         this.logger = logger;
     }
     
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
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
         if (!(object instanceof Employe)) {
             return false;
         }
         Employe other = (Employe) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.AlphaDevs.Web.Entities.Employe[ id=" + id + " ]";
     }
     
 }
