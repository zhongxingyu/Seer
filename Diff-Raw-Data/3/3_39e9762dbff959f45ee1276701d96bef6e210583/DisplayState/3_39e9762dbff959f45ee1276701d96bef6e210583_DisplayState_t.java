 package models;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 public class DisplayState extends Model implements Serializable {
 	
 	private static final long serialVersionUID = 7519930680873671772L;
 	private static final int firstDayOfWeek = Calendar.MONDAY;
     private Calendar firstDay;
     private Calendar lastDay;
 
     public DisplayState() {
         Calendar c = new GregorianCalendar();
         _setWeek(
                 c.get(Calendar.YEAR),
                 c.get(Calendar.MONTH),
                 c.get(Calendar.DAY_OF_MONTH)
         );
     }
 
     public Calendar getFirstDay() {
         return (Calendar) firstDay.clone();
     }
 
     public Calendar getLastDay() {
         return (Calendar) lastDay.clone();
     }
 
     private void _setWeek(int year, int month, int day) {
         Calendar calendar = new GregorianCalendar(year, month, day);
         calendar.setFirstDayOfWeek(firstDayOfWeek);
         firstDay = (Calendar) calendar.clone();
         firstDay.add(Calendar.DAY_OF_YEAR, (-7-calendar.get(Calendar.DAY_OF_WEEK) + 2)%7);
         firstDay.set(Calendar.HOUR_OF_DAY,0);
         firstDay.set(Calendar.MINUTE,0);
         firstDay.set(Calendar.MILLISECOND,0);
         lastDay = (Calendar) firstDay.clone();
         lastDay.add(Calendar.DAY_OF_WEEK, 6);
        lastDay.set(Calendar.HOUR_OF_DAY,23);
        lastDay.set(Calendar.MINUTE,59);
        lastDay.set(Calendar.MILLISECOND,59);
     }
 
     public void setWeek(int year, int month, int day) {
         _setWeek(year, month, day);
         Organizer.getInstance().update();
     }
 
     public void setNextWeek() {
         firstDay.add(Calendar.DAY_OF_YEAR, 7);
         lastDay.add(Calendar.DAY_OF_YEAR, 7);
         Organizer.getInstance().update();
     }
 
     public void setPreviousWeek() {
         firstDay.add(Calendar.DAY_OF_YEAR, -7);
         lastDay.add(Calendar.DAY_OF_YEAR, -7);
         Organizer.getInstance().update();
     }
 
     public String getRangeDisplay() {
         DateFormat d = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
         String from = d.format(getFirstDay().getTime());
         String to = d.format(getLastDay().getTime());
         return from + " to " + to;
     }
     
     public Iterable<Event> getCurrentWeekEvents(){
 		return Organizer.getInstance().getCurrentUser().getUserProfile().getEvents().overlapping(firstDay, lastDay);
     }
     
 }
