 package models;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import play.db.jpa.Model;
 
 @Entity
 public class BS extends Model {
 
     @Temporal(TemporalType.DATE)
     public Calendar bsDate;
     @ManyToOne
     public Person resident;
     @ManyToOne
     public Person ts;
     public int status;
     @Lob
     public String reason;
    public String bsTime;
     @Temporal(TemporalType.TIMESTAMP)
     public Calendar bsDoneTime;
     @ManyToOne
     public Department department;
     @ManyToOne
     public Person departmentChief;
 
     public static List<BS> byDepartmentChief(Person departmentChief) {
         return BS.find("departmentChief = ? "
                 + "order by bsDoneTime desc", departmentChief).fetch();
     }
 
     public void validate() {
         this.status = 1;
         this.save();
         
         Attendance attendance = Attendance.byDayAndPerson(this.bsDate, this.resident);
         if(attendance == null){
             attendance = Attendance.makeBS(this);
         }else{
             attendance.status = AttendanceStatus.BS.value;
         }
         
         attendance.save();
     }
 
     public void update(Person ts,Person departmentChief) {
         this.departmentChief = departmentChief;
         this.bsDoneTime = new GregorianCalendar();
         this.ts = ts;
         this.save();
     }
     
     public static List<BS> byResident(Person resident) {
         return BS.find("resident = ? "
                 + "order by bsDoneTime desc", resident).fetch();
     }
 
 }
