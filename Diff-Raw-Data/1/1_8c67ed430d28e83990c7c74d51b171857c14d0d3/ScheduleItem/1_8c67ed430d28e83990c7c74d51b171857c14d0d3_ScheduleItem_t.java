 package BackEnd;
 
 /**
  *
  * @author Ketty Lezama
  */
 
 public class ScheduleItem {
     private String description;
     private Location location;
     private TimeSchedule timeSchedule;
     
     public ScheduleItem(String description) {
         this.description = description;
         location = new Location();
         timeSchedule = new TimeSchedule();
     }
     
     public void setDescription(String description) {
         this.description = description;
     }
     
     public String getDescription() {
         return description;
     }
     
     public void setLocation(Location location) {
         this.location = location;
     }
     
     public Location getLocation() {
         return location;
     }
     
     public void setTimeSchedule(TimeSchedule timeSchedule) {
         this.timeSchedule = timeSchedule;
     }
     
     public TimeSchedule getTimeSchedule() {
         return timeSchedule;
     }
     
     public boolean equals(ScheduleItem scheduleItem) {
         if (this.getDescription().equalsIgnoreCase(scheduleItem.getDescription()) 
                 && this.getLocation().equals(scheduleItem.getLocation())
                 && this.getTimeSchedule().equals(scheduleItem.getTimeSchedule()))
             return true;
         else
             return false;
     }
     
     public String toString() {
         return "Description: \n" + description + "\nLocation: \n" + location.toString() + 
                 "\nTime Schedule: \n" + timeSchedule.toString();
    }
 }
