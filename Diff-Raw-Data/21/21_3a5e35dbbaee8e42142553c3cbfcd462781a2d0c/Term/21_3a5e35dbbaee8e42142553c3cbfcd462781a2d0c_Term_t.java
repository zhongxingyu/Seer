 package pl.agh.enrollme.model;
 
 import pl.agh.enrollme.utils.DayOfWeek;
 import pl.agh.enrollme.utils.StupidDate;
 import pl.agh.enrollme.utils.Week;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * @author Michal Partyka
  */
 @Entity
 public class Term implements Serializable {
     @Transient
     private static final long serialVersionUID = -5771235478609230476L;
 
     @EmbeddedId
     private TermPK termId;
 
     //Mapping from EmbeddedID!
     @MapsId("subject")
     @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
     Subject subject;
 
     private Date startTime;
     private Date endTime;
 
     private Week week;
     private Integer capacity;
     private String room;
     private String type;        //type of activity: f.e lecture, lab, ex etc
     private Boolean certain;    //if true, an event is not part of the ongoing enrollment and cannot be assigned points
 
     @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
     private Teacher teacher;
 
     public Term() {
     }
 
     public Term(TermPK termId, Date startTime, Date endTime, Week week, Integer capacity,
                 String room, Teacher teacher, String type, Boolean certain) {
         this.termId = termId;
         this.startTime = startTime;
         this.endTime = endTime;
         this.week = week;
         this.capacity = capacity;
         this.room = room;
         this.teacher = teacher;
         this.type = type;
         this.certain = certain;
     }
 
     public static long getSerialVersionUID() {
         return serialVersionUID;
     }
 
     public TermPK getTermId() {
         return termId;
     }
 
     public Date getStartTime() {
         return startTime;
     }
 
     public Date getEndTime() {
         return endTime;
     }
 
     public Week getWeek() {
         return week;
     }
 
     public Integer getCapacity() {
         return capacity;
     }
 
     public String getRoom() {
         return room;
     }
 
     public Teacher getTeacher() {
         return teacher;
     }
 
     public void setTermId(TermPK termId) {
         this.termId = termId;
     }
 
     public void setStartTime(Date startTime) {
         this.startTime = startTime;
     }
 
     public void setEndTime(Date endTime) {
         this.endTime = endTime;
     }
 
     public void setWeek(Week week) {
         this.week = week;
     }
 
     public void setCapacity(Integer capacity) {
         this.capacity = capacity;
     }
 
     public void setRoom(String room) {
         this.room = room;
     }
 
     public void setTeacher(Teacher teacher) {
         this.teacher = teacher;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public Boolean getCertain() {
         return certain;
     }
 
     public void setCertain(Boolean certain) {
         this.certain = certain;
     }
 
     public Subject getSubject() {
         return subject;
     }
 
     public void setSubject(Subject subject) {
         this.subject = subject;
     }

    @Override
    public String toString() {
        return "Term{" +
                "subject=" + subject +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", week=" + week +
                ", capacity=" + capacity +
                ", room='" + room + '\'' +
                ", type='" + type + '\'' +
                ", certain=" + certain +
                ", teacher=" + teacher +
                '}';
    }
 }
