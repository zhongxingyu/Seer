 package pl.edu.pk.nurse.constraints.hard;
 
 import pl.edu.pk.nurse.data.Nurse;
 import pl.edu.pk.nurse.data.Schedule;
 import pl.edu.pk.nurse.data.util.Fitness;
 import pl.edu.pk.nurse.data.util.Shift;
 
 import java.util.List;
 
 /**
  * User: msendyka
  * Date: 25.05.13
  * Time: 10:50
  * A  night shift has to be followed by at least 14 hours rest.
  * An exception is that once in a period of 21 days for 24 consecutive hours, the resting time may be reduced to 8 hours.
  */
 public class RestBeforeNightShiftConstraint extends HardConstraint {
     @Override
     public Fitness measure(Schedule schedule) {
         int violated = 0;
 
         for (Nurse nurse : schedule.toEntity()) {
             List<Shift> allShifts = nurse.getAllShifts();
            int reducedIndex = -1;
             for (int i = 1; i < allShifts.size(); i++) {
                 Shift today = allShifts.get(i - 1);
                 Shift tommorow = allShifts.get(i);
                 if (tommorow == Shift.NIGHT) {
                     int restBetween = Shift.restBetween(today, tommorow);
                     if (restBetween < 8) {
                         violated++;
                     } else if (restBetween < 14) {
                         if (i - reducedIndex < 21) {
                             violated++;
                         } else {
                             reducedIndex = i;
                         }
                     }
                 }
             }
         }
         return new Fitness(violated);
     }
 }
