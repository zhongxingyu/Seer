 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.imaginea.botbot.server.jpa;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  *
  * @author rohit
  */
 @Entity
 public class RecordEntry implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     
     private Long entryNo;
     
     @Temporal(TemporalType.TIMESTAMP)
     private Calendar entryTime;
     
     @Lob
     @Column(length=2048)
     private String payload;
     
     @ManyToOne
     private RecordSession recordSession;
 
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
 
     public RecordSession getRecordSession() {
         return recordSession;
     }
 
     public void setRecordSession(RecordSession recordSession) {
         this.recordSession = recordSession;
     }
 
     public Long getEntryNo() {
         return entryNo;
     }
 
     public void setEntryNo(Long entryNo) {
         this.entryNo = entryNo;
     }
 
     public String getPayload() {
         return payload;
     }
 
     public void setPayload(String payload) {
         this.payload = payload;
     }
 
     public Calendar getEntryTime() {
         return entryTime;
     }
 
     public void setEntryTime(Calendar entryTime) {
         this.entryTime = entryTime;
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
         if (!(object instanceof RecordEntry)) {
             return false;
         }
         RecordEntry other = (RecordEntry) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
        return "com.imaginea.botbot.server.RecordEntry[ id=" + id + " ]";
     }
     
 }
