 package fr.adrienbrault.notetonsta.entity;
 
 import javax.persistence.*;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Author: Adrien Brault
  * Date: 12/12/11
  * Time: 13:16
  */
 
 @Entity
 public class Intervention {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     protected Integer id;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
     protected Campus campus;
 
     @Basic
     protected String subject;
 
     @Basic
     protected String description;
 
     @Basic
     protected Date dateBegin;
 
     @Basic
     protected Date dateEnd;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
     protected Speaker speaker;
 
     @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL)
     protected List<Evaluation> evaluations;
 
     public Intervention() {
 
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Campus getCampus() {
         return campus;
     }
 
     public void setCampus(Campus campus) {
         this.campus = campus;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Date getDateBegin() {
         return dateBegin;
     }
 
     public void setDateBegin(Date dateBegin) {
         this.dateBegin = dateBegin;
     }
 
     public Date getDateEnd() {
         return dateEnd;
     }
 
     public void setDateEnd(Date dateEnd) {
         this.dateEnd = dateEnd;
     }
 
     public Speaker getSpeaker() {
         return speaker;
     }
 
     public void setSpeaker(Speaker speaker) {
         this.speaker = speaker;
     }
 
     public List<Evaluation> getEvaluations() {
         return evaluations;
     }
 
     public void setEvaluations(List<Evaluation> evaluations) {
         this.evaluations = evaluations;
     }
     
     public String getStatusString() {
     	Date now = new Date();
     	String status;
     	
     	if (dateBegin.before(now)) {
     		status = "Ended";
     	} else if (dateEnd.after(now)) {
     		status = "To begin";
     	} else {
     		status = "In progress";
     	}
     	
     	return status;
     }
 
 }
