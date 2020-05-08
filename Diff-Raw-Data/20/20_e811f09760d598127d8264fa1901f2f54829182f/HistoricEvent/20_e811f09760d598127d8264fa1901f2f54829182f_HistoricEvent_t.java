 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.eva.historic.entities;
 
 import ca.uhn.hl7v2.model.Message;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author mmartin
  */
 @Entity
 @Table(name = "historic")
 @XmlRootElement
 /**
  * Entity with all information about an HL7 event and it occurs
  */
 public class HistoricEvent {
 
     @Id
     private String id;
     @Column(nullable = false)
     @Lob
    @Basic(fetch= FetchType.EAGER)
     private Message trace;
     @Column(nullable = false)
     private Long run;
     @Column(length = 255, nullable = false)
     private String className;
     @Column(length = 255, nullable = false)
     private String principal;
 
     public HistoricEvent() {
     }
 
     public HistoricEvent(String id, Message trace, Long run, String principal) {
         this.id = id;
         this.trace = trace;
         this.run = run;
         this.className = trace.getClass().getName();
         this.principal = principal;
     }   
     
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getClassName() {
         return className;
     }
 
     public void setClassName(String className) {
         this.className = className;
     }
 
     public String getPrincipal() {
         return principal;
     }
 
     public void setPrincipal(String principal) {
         this.principal = principal;
     }
 
     public Long getRun() {
         return run;
     }
 
     public void setRun(Long run) {
         this.run = run;
     }
 
     public Message getTrace() {
         return trace;
     }
 
     public void setTrace(Message trace) {
         this.trace = trace;
     }        
 }
