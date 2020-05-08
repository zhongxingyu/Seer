 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ch.comem.game.model;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author Sandra
  */
 @Entity
 @XmlRootElement
 @NamedQuery(name = "findRule",
        query = "SELECT r.id, r.eventType "
         + "FROM Rule r")
 public class Rule implements Serializable {
 
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     @ManyToOne(fetch = FetchType.LAZY)
     protected Application application;
     @OneToOne(fetch = FetchType.LAZY)
     private Badge badge;
     private String eventType;
 
     public Rule() {
         this.badge = null;
     }
 
     public Badge getBadge() {
         return badge;
     }
 
     public void setBadge(Badge badge) {
         this.badge = badge;
     }
 
     public void setApplication(Application application) {
         this.application = application;
     }
 
     public Application getApplication() {
         return this.application;
     }
 
     public String getEventType() {
         return eventType;
     }
 
     public void setEventType(String eventType) {
         this.eventType = eventType;
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
         if (!(object instanceof Rule)) {
             return false;
         }
         Rule other = (Rule) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "ch.comem.game.model.Regle[ id=" + id + " ]";
     }
 }
