 package pl.edu.pk.nurse.constraints.hard;
 
 import pl.edu.pk.nurse.Configuration;
 import pl.edu.pk.nurse.data.Nurse;
 import pl.edu.pk.nurse.data.Schedule;
 import pl.edu.pk.nurse.data.util.Fitness;
 import pl.edu.pk.nurse.data.util.Shift;
 import pl.edu.pk.nurse.data.util.Week;
 import pl.edu.pk.nurse.data.util.Weekday;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: msendyka
  * Date: 24.05.13
  * Time: 21:48
  * •	Cover needs to be fulfilled (i.e. no shifts must be left unassigned).
  */
 public class FullCoverConstraint extends HardConstraint {
     @Override
     public Fitness measure(Schedule schedule) {
         int violated = 0;
         for (int i = 0; i < 5; i++) {
             List<Week> weeks = new ArrayList<Week>();
             for (Nurse nurse : schedule.toEntity()) {
                 weeks.add(nurse.getWeek(i));
             }
             violated += new WeekShiftValidator(weeks).validate();
         }
         return new Fitness(violated * Configuration.HARD_CONSTRAINT_WEIGHT);
     }
 
     private class WeekShiftValidator {
         private List<Week> week;
 
         private WeekShiftValidator(List<Week> week) {
             this.week = week;
         }
 
         private int validate() {
             int result = 0;
             result += weekDay(Weekday.MONDAY);
             result += weekDay(Weekday.TUESDAY);
             result += weekDay(Weekday.WEDNESDAY);
             result += weekDay(Weekday.THURSDAY);
             result += weekDay(Weekday.FRIDAY);
             result += weekDay(Weekday.SATURDAY);
             result += weekend(Weekday.SUNDAY);
            return result != 0 ? 1 : 0;
 
         }
 
         private int weekend(Weekday weekday) {
             int result = findShifts(weekday, Shift.DAY) - 2;
             result += findShifts(weekday, Shift.EARLY) - 2;
             result += findShifts(weekday, Shift.LATE) - 2;
             result += findShifts(weekday, Shift.NIGHT) - 1;
             return result;
         }
 
         private int weekDay(Weekday weekday) {
             int result = findShifts(weekday, Shift.DAY) - 3;
             result += findShifts(weekday, Shift.EARLY) - 3;
             result += findShifts(weekday, Shift.LATE) - 3;
             result += findShifts(weekday, Shift.NIGHT) - 1;
             return result;
         }
 
         private int findShifts(Weekday weekday, Shift shift) {
             int result = 0;
             for (Week week : this.week) {
                 if (week.getShiftForDay(weekday) == shift) {
                     result++;
                 }
             }
             return result;
         }
     }
 }
