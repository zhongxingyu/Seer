 package Hospital.Schedules.Constraints.Implementation;
 
 import Hospital.People.Unmovable;
 import Hospital.Schedules.Appointment;
 import Hospital.Schedules.Constraints.TimeFrameConstraint;
 import Hospital.Schedules.Constraints.TimeFrameConstraintImplementation;
 import Hospital.Schedules.Schedulable;
 import Hospital.Schedules.Schedule;
 import Hospital.Schedules.TimeFrame;
 import java.util.HashSet;
 import java.util.Set;
 
 public class UnmovableConstraint extends TimeFrameConstraintImplementation {
 
     private Unmovable unmovable;
     private Set<Schedule> schedules = new HashSet<Schedule>();
     private TimeFrame tf;
 
     public UnmovableConstraint(TimeFrameConstraint next, Unmovable unmovable) {
         super(next);
         this.unmovable = unmovable;
     }
 
     public UnmovableConstraint(Unmovable unmovable) {
         this(null, unmovable);
     }
 
     @Override
     protected void setValidSchedulable(Schedulable s) {
         schedules.add(s.getSchedule());
     }
 
     @Override
     protected void setValidTimeFrame(TimeFrame tf) {
         this.tf = tf;
     }
 
     @Override
     protected void resetValid() {
        schedules.clear();
     }
 
     @Override
     protected Boolean isAccepted() {
         for (Schedule schedule : this.schedules) {
             Appointment prev = schedule.getAppointmentBefore(tf.getTime());
             if (prev != null) {
                 int timeDiff = prev.getTimeFrame().getEndTime().getMinutesDiff(tf.getTime());
                 int walkTime = prev.getCampus().getTravelTime(unmovable.getCampus());
                 if (timeDiff < walkTime) {
                     return false;
                 }
             }
             Appointment next = schedule.getAppointmentAfter(tf.getEndTime());
             if (next != null) {
                 int timeDiff = next.getTimeFrame().getTime().getMinutesDiff(tf.getEndTime());
                 int walkTime = next.getCampus().getTravelTime(unmovable.getCampus());
                 if (timeDiff < walkTime) {
                     return false;
                 }
             }
         }
         return true;
     }
 }
