 package entity;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import javax.enterprise.inject.New;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQuery;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 
 @NamedQuery(name = "Absence.getAbsences", query = "SELECT a FROM Course c, Absence a "
         + "WHERE c.owner = a.teacher "
        + "AND (a.startDate BETWEEN ?1 AND ?2 OR a.endDate BETWEEN ?1 AND ?2) "
         + "AND ?3 MEMBER OF c.users "
         + "ORDER BY a.startDate")
 @Entity
 public class Absence implements Serializable
 {
     @Id @GeneratedValue(strategy= GenerationType.TABLE)
     private long id;
     
     @ManyToOne
     @NotNull
     private Teacher teacher;
     
     @Temporal(TemporalType.TIMESTAMP)
     private Date startDate;
     
     @Temporal(TemporalType.TIMESTAMP)
     private Date endDate;
 
     public long getId() {
         return id;
     }
     public Teacher getTeacher() {
         return teacher;
     }
     public Date getStart() {
         return startDate;
     }
     public Date getEnd() {
         return endDate;
     }
     
     public void setId(long id) {
         this.id = id;
     }
     public void setTeacher(Teacher teacher) {
         this.teacher = teacher;
     }
     public void setStart(Date start) {
         this.startDate = start;
     }
     public void setEnd(Date end) {
         this.endDate = end;
     }
 }
