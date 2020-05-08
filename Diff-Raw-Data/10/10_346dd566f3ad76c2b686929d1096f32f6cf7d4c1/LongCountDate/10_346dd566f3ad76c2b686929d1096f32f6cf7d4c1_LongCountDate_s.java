 /**
  *
  */
 package icd3;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * An immutable data structure representing the Mayan Long Count absolute date.
  */
 public class LongCountDate implements MayanDate<LongCountDate>
 {
     /**
      * Integer representation of this date
      */
     private int m_value;
 
     /**
      * Place value-separated representation
      */
     private int[] m_periods;
 
     /**
      * Instantiates a LongCountDate object from its representation separated into place values starting with the single
      * day unit. Negatives are not permitted and will be interpreted as zero.
      *
      * @param placeValues A list of place values from least significant to most significant.
      */
     public LongCountDate(Integer... placeValues)
     {
         int value = 0;
 
         Period[] periods = Period.values();
 
         // Multiply out the input values to get the raw number of days, to be robust
         for (int i = 0; i < periods.length; ++i)
         {
             if (i < placeValues.length && placeValues[i] != null && placeValues[i] > 0)
             {
                 value += placeValues[i] * periods[i].days();
             }
         }
 
         // Let initialize store the value and the place values in the right quantities
         this.initialize(value);
     }
 
     /**
      * Instantiates a LongCountDate object from its integer representation. Negatives are not permitted and will be
      * interpreted as zero.
      *
      * @param value The integer representation.
      */
     public LongCountDate(int value)
     {
         this.initialize(value);
     }
 
     private void initialize(int value)
     {
         // Clamp the value to be non-negative
         m_value = value < 0 ? 0 : value;
 
         Period[] periods = Period.values();
 
         m_periods = new int[periods.length];
 
         for (int i = periods.length - 1; i >= 0; --i)
         {
             // Divide by the magnitude of the place value, and floor it (implicit)
             int placeValue = value / periods[i].days();
 
             // Subtract the amount from the running total
             value -= placeValue * periods[i].days();
 
             m_periods[i] = placeValue;
         }
     }
 
     /*
      * (non-Javadoc)
      *
      * @see icd3.MayanDate#plus(int)
      */
     @Override
     public LongCountDate plus(int days)
     {
         return new LongCountDate(this.toInt() + days);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see icd3.MayanDate#minus(java.lang.Object)
      */
     @Override
     public int minus(LongCountDate other)
     {
         if (null == other)
         {
             throw new NullPointerException("Cannot subtract a null Long Count Date");
         }
         return this.toInt() - other.toInt();
     }
 
     /**
      * Get the place value of a specified period.
      *
      * @return The number in the specified place.
      */
     public int getPeriod(Period period)
     {
         return m_periods[period.ordinal()];
     }
 
     /*
      * (non-Javadoc)
      *
      * @see icd3.MayanDate#toInt()
      */
     @Override
     public int toInt()
     {
         return m_value;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
 
         for (int i = m_periods.length - 1; i >= 0; --i)
         {
             // Append to the builder
             builder.append(String.format("%d", m_periods[i]));
 
             // Do not place a dot after the last digit
             if (i > 0)
             {
                 builder.append(".");
             }
         }
 
         return builder.toString();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object o)
     {
         return o != null && this.getClass().equals(o.getClass()) && this.toInt() == ((LongCountDate) o).toInt();
     }
 
     // The Calendar Round date corresponding to 0.0.0.0.0
     private static CalendarRoundDate s_zeroDay = new CalendarRoundDate(new TzolkinDate(4, TzolkinDate.Day.AJAW),
             new HaabDate(8, HaabDate.Month.KUMKU));
 
     /**
      * Return the first Long Count date represented by a Calendar Round date after the specified date.
      *
      * @param calendarRound The Calendar Round expression of the desired date.
      * @param start The minimum Long Count date, inclusive.
      * @return The first occurrence of the date at or following the start.
      */
     public static LongCountDate calendarRoundToLongCount(CalendarRoundDate calendarRound, LongCountDate start)
     {
         // Find when the specified date begins
         int startSinceZero = start.toInt();
 
         // Find the calendarRoundDate corresponding to that beginning
         CalendarRoundDate calendarRoundStart = s_zeroDay.plus(startSinceZero);
 
         // Find the difference between the given calendarRound date and the start
         int daysSinceStart = calendarRound.minus(calendarRoundStart);
 
         // Instantiate the LongCountDate corresponding to that many days after the start
        return new LongCountDate(startSinceZero + daysSinceStart);
     }
 
     /**
      * Return all Long Count dates represented by a Calendar Round date within a range of dates
      *
      * @param calendarRound The Calendar Round expression of the desired dates.
      * @param start The minimum date, inclusive.
      * @param end The maximum date, exclusive.
      * @return All occurrences of the date within the range.
      */
     public static List<LongCountDate> calendarRoundToLongCountList(CalendarRoundDate calendarRound,
                                                                    LongCountDate start,
                                                                    LongCountDate end)
     {
         if (null == calendarRound || null == start || null == end)
         {
             throw new NullPointerException("Date parameters must not be null.");
         }
 
         List<LongCountDate> dates = new ArrayList<LongCountDate>();
 
         // Get the length of the Calendar Round
         int cycle = calendarRound.cycle();
 
         // Find the first occurrence at or after start
         LongCountDate occurrence = calendarRoundToLongCount(calendarRound, start);
 
         // Iterate, adding all Long Count Dates between start and end
         while (end.minus(occurrence) > 0)
         {
             // No need to copy, dates are immutable
             dates.add(occurrence);
 
             // Cycle forward
             occurrence = occurrence.plus(cycle);
         }
 
         return dates;
     }
 
     /**
      * The periods used in the Mesoamerican Long Count Calendar, and their lengths in days.
      */
     public enum Period
     {
         KIN(1),
         WINAL(20),
         TUN(360),
         KATUN(7200),
         BAKTUN(144000);
 
         private int m_days;
 
         private Period(int days)
         {
             m_days = days;
         }
 
         public int days()
         {
             return m_days;
         }
     }
 }
