 /*
  * Instrument.java
  * 
  * Created on Nov 2, 2007, 11:15:10 AM
  * 
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.dialogix.entities;
 
 import java.io.Serializable;
 
 import java.util.Collection;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.OneToMany;
 import javax.persistence.*;
 
 /**
  *
  * @author Coevtmw
  */
 @Entity
 @Table(name = "instruments")
 public class Instrument implements Serializable {
     @TableGenerator(name="instrument_gen", pkColumnValue="instrument", table="sequence", pkColumnName="seq_name", valueColumnName="seq_count", allocationSize=1)
     @Id
     @GeneratedValue(strategy=GenerationType.TABLE, generator="instrument_gen")
     @Column(name = "instrument_id", nullable = false)
     private Long instrumentID;
     @Column(name = "name", nullable = false)
     private String instrumentName;
     @Lob
    @Column(name = "instrument_description")
     private String instrumentDescription;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "instrumentID")
     private Collection<InstrumentSession> instrumentSessionCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "instrumentID")
     private Collection<InstrumentVersion> instrumentVersionCollection;
 
     public Instrument() {
     }
 
     public Instrument(Long instrumentID) {
         this.instrumentID = instrumentID;
     }
 
     public Instrument(Long instrumentID, String instrumentName) {
         this.instrumentID = instrumentID;
         this.instrumentName = instrumentName;
     }
 
     public Long getInstrumentID() {
         return instrumentID;
     }
 
     public void setInstrumentID(Long instrumentID) {
         this.instrumentID = instrumentID;
     }
 
     public String getInstrumentName() {
         return instrumentName;
     }
 
     public void setInstrumentName(String instrumentName) {
         this.instrumentName = instrumentName;
     }
 
     public String getInstrumentDescription() {
         return instrumentDescription;
     }
 
     public void setInstrumentDescription(String instrumentDescription) {
         this.instrumentDescription = instrumentDescription;
     }
 
     public Collection<InstrumentSession> getInstrumentSessionCollection() {
         return instrumentSessionCollection;
     }
 
     public void setInstrumentSessionCollection(Collection<InstrumentSession> instrumentSessionCollection) {
         this.instrumentSessionCollection = instrumentSessionCollection;
     }
 
     public Collection<InstrumentVersion> getInstrumentVersionCollection() {
         return instrumentVersionCollection;
     }
 
     public void setInstrumentVersionCollection(Collection<InstrumentVersion> instrumentVersionCollection) {
         this.instrumentVersionCollection = instrumentVersionCollection;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (instrumentID != null ? instrumentID.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof Instrument)) {
             return false;
         }
         Instrument other = (Instrument) object;
         if ((this.instrumentID == null && other.instrumentID != null) || (this.instrumentID != null && !this.instrumentID.equals(other.instrumentID))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "org.dialogix.entities.Instrument[instrumentID=" + instrumentID + "]";
     }
 
 }
