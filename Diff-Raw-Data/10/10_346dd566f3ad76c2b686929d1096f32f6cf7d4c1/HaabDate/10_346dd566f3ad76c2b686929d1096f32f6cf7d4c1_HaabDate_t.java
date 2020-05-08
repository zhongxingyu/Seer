 /**
  *
  */
 package icd3;
 
 /**
  * An immutable data structure that represents the Haab method of Mayan calendaring.
  */
 public class HaabDate extends CyclicDate<HaabDate>
 {
     private int m_day;
 
     private Month m_month;
 
     public HaabDate(int numeral, Month month)
     {
         super(month.daysBefore() + numeral - 1);
 
         // Numeral is 1-based, store 0-based
         int day = numeral - 1;
 
         m_month = month;
        m_day = Math.min(Math.max(0, day), month.days() - 1);
     }
 
     /**
      * Instantiates a HaabDate object from its integer representation.
      *
      * @param value The integer representation.
      */
     public HaabDate(int value)
     {
         super(value);
 
         m_month = Month.values()[toInt() / s_daysPerMonth];
         m_day = toInt() - m_month.daysBefore();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see icd3.CyclicDate#cycle()
      */
     @Override
     public int cycle()
     {
         return haabCycle();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see icd3.MayanDate#plus(int)
      */
     @Override
     public HaabDate plus(int days)
     {
         // Simply add days to the integer representation
         return new HaabDate(this.toInt() + days);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString()
     {
         return String.format("%d.%s", getNumeral(), getMonth());
     }
 
     /**
      * Gets the 1-based numeral in the Haab representation.
      *
      * @return The numeral
      */
     public int getNumeral()
     {
         // Return the 1-based numeral
         return m_day + 1;
     }
 
     /**
      * Gets the named month in the Haab representation.
      *
      * @return The month name.
      */
     public Month getMonth()
     {
         return m_month;
     }
 
     /**
      * The names and order of the months in the Haab system of Mayan calendaring.
      */
     public enum Month
     {
         POHP,
         WO,
         SIP,
         ZOTZ,
         SEK,
         XUL,
         YAXKIN,
         MOL,
         CHEN,
         YAX,
         SAK,
         KEH,
         MAK,
         KANKIN,
         MUAN,
         PAX,
         KAYAB,
         KUMKU,
         WAYEB(5);
 
         private int m_days;
 
         private Month()
         {
             this(s_daysPerMonth);
         }
 
         private Month(int days)
         {
             m_days = days;
         }
 
         public int days()
         {
             return m_days;
         }
 
         /**
          * The number of days in the year prior to this month.
          *
          * @return The number of days prior to this month. Also, the "index" of the beginning of the month.
          */
         public int daysBefore()
         {
             return s_daysPerMonth * this.ordinal();
         }
     }
 
     /**
      * The length of the months in the first 360 days of the Haab calendar.
      */
     private static final int s_daysPerMonth = 20;
 
     /**
      * The number of days in a year. Note that 20 * 19 > 365
      */
     private static final int s_daysPerYear = 365;
 
     public static int haabCycle()
     {
         // The cycle is the year length
         return s_daysPerYear;
     }
 }
