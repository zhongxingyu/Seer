 package pl.agh.enrollme.model;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 public class TermPK implements Serializable {
 
     @Transient
     private static final long serialVersionUID = -5771235478609230476L;
 
     private Subject subject;
 
     private Integer termPerSubjectID;
 
     public TermPK(Subject subject, Integer termPerSubjectID) {
         this.subject = subject;
         this.termPerSubjectID = termPerSubjectID;
     }
 
     public Subject getSubject() {
         return subject;
     }
 
     public Integer getTermPerSubjectID() {
         return termPerSubjectID;
     }
 }
