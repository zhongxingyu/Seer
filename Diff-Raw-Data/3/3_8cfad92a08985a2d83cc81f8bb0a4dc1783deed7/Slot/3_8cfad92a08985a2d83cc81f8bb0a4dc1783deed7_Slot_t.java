 package models;
 
 import models.crudsiena.SienaSupport;
 import play.data.binding.As;
 import siena.*;
 import siena.embed.Embedded;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 public class Slot extends SienaSupport
 {
   @Id(Generator.AUTO_INCREMENT)
   public Long id;
 
   @Column("start_time")
   @DateTime
   public Date startTime;
 
   @Column("end_time")
   @DateTime
   public Date endTime;
 
   @Column("kind")
   public String kind;
 
   static Query<Slot> all()
   {
     return SienaSupport.all(Slot.class).order("startTime");
   }
 
   public static List<Slot> findAll()
   {
     return all().fetch();
   }
 
   @Override
   public String toString()
   {
     Calendar startCalendar = GregorianCalendar.getInstance(); // creates a new calendar instance
     startCalendar.setTime(startTime);
     startCalendar.get(Calendar.HOUR_OF_DAY);
     startCalendar.get(Calendar.MINUTE);
     Calendar endCalendar = GregorianCalendar.getInstance(); // creates a new calendar instance
     endCalendar.setTime(endTime);
     endCalendar.get(Calendar.HOUR_OF_DAY);
     endCalendar.get(Calendar.MINUTE);
     //return startCalendar.get(Calendar.HOUR_OF_DAY) + ":" + startCalendar.get(Calendar.MINUTE) + " - " + endCalendar.get(Calendar.HOUR_OF_DAY) + ":" + endCalendar.get(Calendar.MINUTE);
     return startTime.toString() + " - " + endTime.toString();
   }
 }
