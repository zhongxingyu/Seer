 package com.seitenbau.stu.database.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import java.util.Comparator;
 
 import com.seitenbau.stu.database.modifier.IDataSetOverwriteCompare;
 import com.seitenbau.stu.logger.Logger;
 import com.seitenbau.stu.logger.TestLoggerFactory;
 import com.seitenbau.stu.util.date.DateBuilder;
 
 public class DbCompare
 {
   static Logger logger = TestLoggerFactory.get(DbCompare.class);
   
   /**
    * Fuzzy checks the database field to the current time (+-15s).
    */
   public static Date warp(DateBuilder datum2wrap)
   {
     return new ReplaceDate(datum2wrap.asDate(), new DateCompareImpl());
   }
 
   /**
    * Fuzzy checks the database field to the current time (+-15s).
    */
  public static Date currentTime()
   {
     return new ReplaceDate(new Date(), new DateCompareImpl());
   }
 
   /**
    * Fuzzy checks the database field to the current time (+-15s).
    * Milliseconds are set to 0.
    */
  public static Date currentTimeOhneMS()
   {
    Date currentTime = new Date((System.currentTimeMillis() / 1000) * 1000);
    return new ReplaceDate(currentTime, new DateCompareImpl());
   }
 
   /**
    * Fuzzy checks the database field to the current time (+-15s).
    * HH:mm:ss.SSS time is set to "00:00:00.000".
    */
  public static Date currentTimeNurDatum()
   {
    Date currentTime = new Date((System.currentTimeMillis() / 1000) * 1000);
 
     Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentTime);
     calendar.set(Calendar.HOUR_OF_DAY, 0);
     calendar.set(Calendar.MINUTE, 0);
     calendar.set(Calendar.SECOND, 0);
 
    currentTime.setTime(calendar.getTimeInMillis());
    return new ReplaceDate(currentTime, new DateCompareImpl());
   }
 
   /**
    * Fuzzy checks the database field to the current time (+-15s).
    * Format : "dd.MM.yyyy HH:mm.ss"
    */
   public static Date datum(String datum) throws ParseException
   {
     SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm.ss");
     Date expect = sdf.parse(datum);
     return new ReplaceDate(expect, new DateCompareImpl());
   }
 
   public static Date datum(long offsetInSeconds, int minSecOffSet, int maxSecOffSet)
   {
     Date expect = new Date(new Date().getTime() + (offsetInSeconds * 1000));
     return new ReplaceDate(expect, new DateCompareImpl(minSecOffSet * 1000, maxSecOffSet * 1000));
   }
 
   /**
    * Fuzzy checks the database field to the current time (+-offset).
    */
   public static Date datum(Date now, long offsetInSeconds)
   {
     Date expect = new Date(now.getTime() + (offsetInSeconds * 1000));
     return new ReplaceDate(expect, new DateCompareImpl());
   }
 
   /**
    * Fuzzy checks the database field to the given date (+-offset).
    */
   public static Date datum(long offsetInSeconds)
   {
     Date expect = new Date(new Date().getTime() + (offsetInSeconds * 1000));
     return new ReplaceDate(expect, new DateCompareImpl());
   }
 
   public static class DateCompareImpl implements Comparator<Date>
   {
     static Logger logger = TestLoggerFactory.get(DateCompareImpl.class);
     
     private static final int ONE_SECOND = 1000;
 
     private Integer fPlusMilliseconds = null;
 
     private Integer fMinusMilliseconds = null;
 
     public DateCompareImpl(int plusMinusMilliseconds)
     {
       init(plusMinusMilliseconds, plusMinusMilliseconds);
     }
 
     public DateCompareImpl()
     {
       init(15 * ONE_SECOND, 15 * ONE_SECOND);
     }
 
     public DateCompareImpl(int minusMilliseconds, int plusMilliseconds)
     {
       init(minusMilliseconds, plusMilliseconds);
     }
 
     public void init(int minusMilliseconds, int plusMilliseconds)
     {
       fPlusMilliseconds = plusMilliseconds;
       fMinusMilliseconds = minusMilliseconds;
     }
 
     public int compare(Date expectedDate, Date actualValue)
     {
         long ticksExpect = expectedDate.getTime();
         long ticksActual = actualValue.getTime();
 
         if (fMinusMilliseconds != null)
         {
           long diff = ticksExpect - ticksActual;
 
           if (diff > fMinusMilliseconds)
           {
               logger.error("Timestamp - Range Comparison : expected "
                 + actualValue + " less than " + fMinusMilliseconds
                 + " ms before " + new Date(ticksExpect) + ", but was " + diff
                 + " ms earlier");
               return -1;
           }
         }
         if (fPlusMilliseconds != null)
         {
           long diff = ticksActual - ticksExpect;
 
           if (diff > fPlusMilliseconds)
           {
               logger.error("Timestamp - Range Comparison : expected "
                 + actualValue + " less than " + fPlusMilliseconds
                 + " ms after " + new Date(ticksExpect) + ", but was " + diff
                 + " ms later");
               return 1;
           }
         }
         return 0;
     }
     
     /**
      * Use compare instead
      * @param expectedDate
      * @param actualValue
      * @return
      */
     @Deprecated
     public boolean compareValues(Date expectedDate, Date actualValue)
     {
       long ticksExpect = expectedDate.getTime();
       long ticksActual = actualValue.getTime();
 
       if (fMinusMilliseconds != null)
       {
         long diff = ticksExpect - ticksActual;
 
         if (diff > fMinusMilliseconds)
         {
           throw new AssertionError("Timestamp - Range Comparison : expected " + actualValue + " less than "
               + fMinusMilliseconds + " ms before " + new Date(ticksExpect) + ", but was " + diff + " ms fr�her");
         }
       }
       if (fPlusMilliseconds != null)
       {
         long diff = ticksActual - ticksExpect;
 
         if (diff > fPlusMilliseconds)
         {
           throw new AssertionError("Timestamp - Range Comparison : expected " + actualValue + " less than "
               + fPlusMilliseconds + " ms after " + new Date(ticksExpect) + ", but was " + diff + " ms later");
         }
       }
       return true;
     }
 
     public Date createCompareTo(Date referenceDate)
     {
       return new ReplaceDate(referenceDate, this);
     }
   }
 
   public static class ReplaceDate extends Date implements IDataSetOverwriteCompare
   {
     private static final long serialVersionUID = 1L;
 
     private Date _datum;
 
     private DateCompareImpl _compare;
 
     public ReplaceDate(Date referenceDate, DateCompareImpl compare)
     {
       super(referenceDate.getTime());
       _datum = referenceDate;
       _compare = compare;
     }
 
     protected Date getDatum()
     {
       return _datum;
     }
 
     public boolean equals(Object actual)
     {
       if (actual instanceof Date)
       {
         return (_compare.compare(getDatum(), (Date) actual) == 0);
       }
       return super.equals(actual);
     }
 
     public int compareTo(Date actual)
     {
       if (actual == null)
       {
         if (getDatum() == null)
         {
           return 0;
         }
       }
       return _compare.compare(getDatum(), actual);
     }
 
     public int compareDataSetElementTo(Object objectToCompareTo)
     {
       if (objectToCompareTo instanceof Date)
       {
         return _compare.compare(getDatum(), (Date) objectToCompareTo);
       }
       else
       {
         throw new UnsupportedOperationException("Object is not of Type Date, but we need to compare Dates here.");
       }
     }
 
   }
 
 }
