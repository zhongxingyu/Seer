 package de.atp.date;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 /**
  * Wrapper for the calendar class containing only year, month and day of an date
  */
 public class ATPDate implements ATPTimestamp<ATPDate>, Comparable<ATPDate> {
 
     /** The day of the month. First day is 1 */
     public static final int FIELD_DAY = Calendar.DAY_OF_MONTH;
     /** The month of the year. First month is 0 */
     public static final int FIELD_MONTH = Calendar.MONTH;
     /** The Year */
     public static final int FIELD_YEAR = Calendar.YEAR;
 
     private Calendar cal;
 
     /**
      * The current day
      */
     public ATPDate() {
         this(Calendar.getInstance());
     }
 
     /**
      * Wraps the date
      * 
      * @param date
      *            The date to wrap
      */
     public ATPDate(Date date) {
         this();
         cal.setTime(date);
     }
 
     /**
      * Wraps the calendar
      * 
      * @param cal
      *            The calendar to wrap
      */
     public ATPDate(Calendar cal) {
         this.cal = (Calendar) cal.clone();
     }
 
     /**
      * Create an date on this timestamp. The timestamp must be in UNIX format
      * (milliseconds since 1st of January 1970)
      * 
      * @param timestamp
      *            milliseconds since 1st of January 1970
      */
     public ATPDate(long timestamp) {
         this();
         cal.setTimeInMillis(timestamp);
     }
 
     /**
      * Date on this values
      * 
      * @param day
      *            The day
      * @param month
      *            The month
      * @param year
      *            The year
      */
     public ATPDate(int day, int month, int year) {
         this();
         setDay(day);
         setMonth(month);
         setYear(year);
     }
 
     @Override
     public boolean before(ATPDate other) {
         return this.compareTo(other) < 0;
     }
 
     @Override
     public boolean after(ATPDate other) {
         return this.compareTo(other) > 0;
     }
 
     @Override
     public void up(int field) {
         this.modify(field, 1);
     }
 
     @Override
     public void down(int field) {
         this.modify(field, -1);
     }
 
     @Override
     public void modify(int field, int diff) {
         cal.add(field, diff);
     }
 
     /**
      * @return The day of this date
      */
     public int getDay() {
         return cal.get(FIELD_DAY);
     }
 
     /**
      * Set the day of this date.
      * 
      * @param day
      *            The day
      */
     public void setDay(int day) {
         cal.set(FIELD_DAY, day);
     }
 
     /**
      * @return The month of this date
      */
     public int getMonth() {
         return cal.get(FIELD_MONTH);
     }
 
     /**
      * Set the month of this date.
      * 
      * @param day
      *            The month
      */
     public void setMonth(int month) {
         cal.set(FIELD_MONTH, month);
     }
 
     /**
      * @return The year of this date
      */
     public int getYear() {
         return cal.get(FIELD_YEAR);
     }
 
     /**
      * Set the year of this date.
      * 
      * @param day
      *            The year
      */
     public void setYear(int year) {
         cal.set(FIELD_YEAR, year);
     }
 
     @Override
     public void setTime(Date date) {
         this.cal.setTime(date);
     }
 
     @Override
     public void setTimestamp(long time) {
         this.cal.setTimeInMillis(time);
     }
 
     @Override
     public Date asDate() {
         return cal.getTime();
     }
 
     /**
      * @param o
      *            Must be an instance of {@link ATPDate}
      * @return <code>True</code> and only true, when both objects have the same
      *         day, same month and same year
      */
     @Override
     public boolean equals(Object o) {
         if (o == null)
             return false;
         if (this == o)
             return true;
         if (!(o instanceof ATPDate))
             return false;
 
         ATPDate other = (ATPDate) o;
 
         return this.getDay() == other.getDay() && this.getMonth() == other.getMonth() && this.getYear() == other.getYear();
     }
 
     @Override
     public int compareTo(ATPDate another) {
         Calendar c1 = GregorianCalendar.getInstance();
        c1.set(getYear(), getMonth(), getDay());
         Calendar c2 = GregorianCalendar.getInstance();
        c2.set(another.getYear(), another.getMonth(), another.getDay());
 
         return c1.compareTo(c2);
     }
 
     @Override
     public ATPDate copy() {
         return new ATPDate(cal.getTimeInMillis());
     }
 
     @Override
     public String toString() {
         return String.format(Locale.getDefault(), "ATPDate{Day=%d;Month=%d;Year=%d}", getDay(), getMonth(), getYear());
     }
 
     @Override
     public ATPDate diff(ATPDate other) {
         int dayDiff = Math.abs(this.getDay() - other.getDay());
         int monthDiff = Math.abs(this.getMonth() - other.getMonth());
         int yearDiff = Math.abs(this.getYear() - other.getYear());
 
         return new ATPDate(dayDiff, monthDiff, yearDiff);
     }
 }
