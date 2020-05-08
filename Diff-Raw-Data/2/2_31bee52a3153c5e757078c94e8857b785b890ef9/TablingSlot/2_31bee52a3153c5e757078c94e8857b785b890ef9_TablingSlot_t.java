 package tablingassigner;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.ArrayList;
 
 /** 
  *
  * @author Rohin Jethani, Keien Ohta
  */
 public class TablingSlot {
     
     public TablingSlot(DayOfWeek dow, int startTime, int endTime) {
         
         // TODO check for errors in startTime and endTime.
         _dow = dow;
         _startTime = new Time(startTime);
         _endTime = new Time(endTime);
     }
     
     DayOfWeek dayOfWeek() {
         return _dow;
     }
     
     int startTime() {
         return _startTime.getLiteral();
     }
     
     int endTime() {
         return _endTime.getLiteral();
     }
 
     public static Time startOfDay() {
         return _START_OF_DAY;
     }
 
     public static Time endOfDay() {
         return _END_OF_DAY;
     }
 
     public static Time slotLength() {
         return _SLOT_LENGTH;
     }
 
     public static Time slotInterval() {
         return _SLOT_INTERVAL;
     }
 
     public static int slotsPerDay() {
         return (int)((TablingSlot.endOfDay().getReal() - TablingSlot.startOfDay().getReal())
             / TablingSlot.slotInterval().getReal());
     }
 
     public static ArrayList<DayOfWeek> tablingDays() {
         _tablingDays.add(DayOfWeek.MONDAY);
         _tablingDays.add(DayOfWeek.TUESDAY);
         _tablingDays.add(DayOfWeek.WEDNESDAY);
         _tablingDays.add(DayOfWeek.THURSDAY);
         _tablingDays.add(DayOfWeek.FRIDAY);
         return _tablingDays;
     }
     
     /** Check whether this tabling slot is full.
      * 
      * @return true iff this slot is full.
      */
     public boolean isFull() {
         for (HalfHour hh : HalfHour.getHalfHours(this)) {
             if (hh.isFull()) {
                 return true;
             }
         }
         return false;
     }
     
     public int remainingCapacity() {
         int minCapacity = Integer.MAX_VALUE;
         for (HalfHour hh : HalfHour.getHalfHours(this)) {
             minCapacity = Math.min(minCapacity, hh.remainingCapacity());
         }
         return minCapacity;
     }
     
     public boolean assignStudent(Student student) {
         if (isFull()) {
             return false;
         }
         for (HalfHour hh : HalfHour.getHalfHours(this)) {
             hh.assignStudent(student);
         }
         return true;
     }
     
     /**
      * 
      * @return The half hours that are full in capacity.
      */
     public Set<HalfHour> fullHalfHours() {
         Set<HalfHour> fullHHs = new HashSet<HalfHour>();
         for (HalfHour hh : HalfHour.getHalfHours(this)) {
             if (hh.isFull()) {
                 fullHHs.add(hh);
             }
         }
         return fullHHs;
     }
     
     public boolean removeStudent(Student student) {
         if (containsStudentStrongly(student)) {
             for (HalfHour hh : HalfHour.getHalfHours(this)) {
                 hh.removeStudent(student);
             }
             return true;
         } else {
             return false;
         }
     }
     
     // Contains the student in ALL of the half hours that this tabling slot 
     // is comprised of.
     public boolean containsStudentStrongly(Student student) {
         boolean result = true;
         for (HalfHour hh : HalfHour.getHalfHours(this)) {
             result &= hh.containsStudent(student);
         }
         return result;
     }
     
     // Contains the student in at least ONE of the half hours that this tabling
     // slot comprises of.
     public HalfHour containsStudentWeakly(Student student) {
         for (HalfHour hh : HalfHour.getHalfHours(this)) {
             if(hh.containsStudent(student)) {
                 return hh;
             }
         }
         return null;
     }
     
     /** If this tabling slot is full, unassign people (strategically) so that
      * there is a spot.
      * 
      * @return 
      */
     public boolean makeSpace() {
         if (!isFull()) {
             return false;
         }
         
         // Since no one may actually be assigned to this half hour, but to ones
         // that share the same half hours as this slot, we may have to remove 
         // two people. Therefore, we make space in each slot
         for (HalfHour hh : fullHalfHours()) {
             if (hh.isFull()) { // We have to check this again because removal from
                 // one half hour could impact another half hour.
                 Student BRC = hh.bestRemovalCandidate();
                 BRC.unassignTablingSlot(BRC.assignedSlotWithHalfHour(hh));
             }
         }
         
         if (isFull()) {
             throw new IllegalArgumentException("makeSpace() failed.");
         }
         return true;
     }
     
     /** Whether or not this tabling slot contains this half hour. 
      * E.g. 12:30-1:00 is a half hour in the slot 12:30-1:30
      * 
      * @param hh
      * @return 
      */
     boolean containsHalfHour(HalfHour hh) {
         return HalfHour.getHalfHours(this).contains(hh);
     }
 
     DayOfWeek _dow;
     private Time _startTime;
     private Time _endTime;
 
     // Tabling slot configuration
     public static String DOW = "MTWTF";
     private static Time _START_OF_DAY = new Time(10, 0);
    private static Time _END_OF_DAY = new Time(14, 0);
     private static Time _SLOT_LENGTH = new Time(0, 60);
     private static Time _SLOT_INTERVAL = new Time(0, 60);
     private static ArrayList<DayOfWeek> _tablingDays = new ArrayList<DayOfWeek>(5);
 }
