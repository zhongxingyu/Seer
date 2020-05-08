 package amu.licence.edt.model.beans;
 
 import java.io.Serializable;
import java.sql.Date;
 
 import javax.persistence.*;
 
 @Entity
 @Table (name="T_SESSION")
 public class Session implements Serializable {
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue
     @Column (name="ID_SESSION")
     private int id;
 
     @Column (name="START_SESSION_DATE")
     private Date startDate;
 
     @Column (name="SESSION_DURATION")
     private int duration;
 
     @ManyToOne
     @JoinColumn (name="ID_TU", nullable=false)
     private TU tU;
 
     @ManyToOne
     @JoinColumn (name="ID_TEACHER", nullable=false)
     private Teacher teacher;
 
     @ManyToOne
     @JoinColumn (name="ID_CROOM")
     private CRoom cRoom;
 
     @ManyToOne
     @JoinColumn (name="ID_SESSION_TYPE", nullable=false)
     private SessionType sessionType;
 
     @ManyToOne
     @JoinColumn (name="ID_GROUP")
     private Group group;        // if NULL, whole promo is assisting
 
     public Session() { }
 
     public Session(Date startDate, int duration, TU tU, Teacher teacher,
             CRoom cRoom, SessionType sessionType, Group group) {
         super();
         this.startDate = startDate;
         this.duration = duration;
         this.tU = tU;
         this.teacher = teacher;
         this.cRoom = cRoom;
         this.sessionType = sessionType;
         this.group = group;
     }
 
     @Override
     public String toString() {
         return "Session [id=" + id + ", startDate=" + startDate + ", duration="
                 + duration + ", tU=" + ((tU != null) ? tU.getLibel() : null)
                 + ", teacher=" + ((teacher != null) ? teacher.getName() : null)
                 + ", cRoom=" + ((cRoom != null) ? cRoom.getNum() : null)
                 + ", sessionType=" + ((sessionType != null) ? sessionType.getLibel() : null)
                 + ", group=" + ((group != null) ? group.getId() : null) + "]";
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + id;
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Session other = (Session) obj;
         if (id != other.id)
             return false;
         return true;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     public int getDuration() {
         return duration;
     }
 
     public void setDuration(int duration) {
         this.duration = duration;
     }
 
     public TU gettU() {
         return tU;
     }
 
     public void settU(TU tU) {
         this.tU = tU;
     }
 
     public Teacher getTeacher() {
         return teacher;
     }
 
     public void setTeacher(Teacher teacher) {
         this.teacher = teacher;
     }
 
     public CRoom getcRoom() {
         return cRoom;
     }
 
     public void setcRoom(CRoom cRoom) {
         this.cRoom = cRoom;
     }
 
     public SessionType getSessionType() {
         return sessionType;
     }
 
     public void setSessionType(SessionType sessionType) {
         this.sessionType = sessionType;
     }
 
     public Group getGroup() {
         return group;
     }
 
     public void setGroup(Group group) {
         this.group = group;
     }
 
     public int getId() {
         return id;
     }
 
 }
