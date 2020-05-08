 
 package com.AlphaDevs.Web.Entities;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 
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
 public class Properties implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE)
     private Long id;
     private String propertyName;
     private String propertyValue;
     @ManyToOne
     private GRN relatedGRNs;
    
 
     public Properties(String propertyName, String propertyValue) {
         this.propertyName = propertyName;
         this.propertyValue = propertyValue;
     }
 
     public Properties() {
     }
 
     public String getPropertyName() {
         return propertyName;
     }
 
     public void setPropertyName(String propertyName) {
         this.propertyName = propertyName;
     }
 
     public String getPropertyValue() {
         return propertyValue;
     }
 
     public void setPropertyValue(String propertyValue) {
         this.propertyValue = propertyValue;
     }
 
    public GRN getRelatedGRNs() {
        return relatedGRNs;
    }

    public void setRelatedGRNs(GRN relatedGRNs) {
        this.relatedGRNs = relatedGRNs;
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
         if (!(object instanceof Properties)) {
             return false;
         }
         Properties other = (Properties) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.AlphaDevs.Web.Entities.Properties[ id=" + id + " ]";
     }
     
 }
