 /*
  * $Id$
  */
 package org.xins.common.types.standard;
 
 import org.xins.common.types.Type;
 import org.xins.common.types.TypeValueException;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * Standard type <em>_timestamp</em>.
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 0.173
  */
 public class Timestamp extends Type {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The only instance of this class. This field is never <code>null</code>.
     */
    public final static Timestamp SINGLETON = new Timestamp();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a <code>Timestamp.Value</code> with the value of the current
     * time.
     *
     * @return
     *    the {@link Value} initialed with the current time,
     *    never <code>null</code>.
     */
    public static Value now() {
       Calendar today = Calendar.getInstance();
       int year = today.get(Calendar.YEAR);
       int month = today.get(Calendar.MONTH);
       int day = today.get(Calendar.DAY_OF_MONTH);
       int hour = today.get(Calendar.HOUR);
       int minutes = today.get(Calendar.MINUTE);
       int seconds = today.get(Calendar.SECOND);
      return new Date.Value(year, month, day, hour, minutes, seconds);
    }
 
    /**
     * Constructs a <code>Timestamp.Value</code> from the specified string
     * which is guaranteed to be non-<code>null</code>.
     *
     * @param string
     *    the string to convert in the ISO format YYYYMMDDhhmmss,
     *    cannot be <code>null</code>.
     *
     * @return
     *    the {@link Value} object, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static Value fromStringForRequired(String string)
    throws IllegalArgumentException, TypeValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("string", string);
 
       return (Value) SINGLETON.fromString(string);
    }
 
    /**
     * Constructs a <code>Timestamp.Value</code> from the specified string.
     *
     * @param string
     *    the string to convert in the ISO format YYYYMMDDhhmmss,
     *    can be <code>null</code>.
     *
     * @return
     *    the {@link Value}, or <code>null</code> if
     *    <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static Value fromStringForOptional(String string)
    throws TypeValueException {
       return (Value) SINGLETON.fromString(string);
    }
 
    /**
     * Converts the specified <code>Timestamp.Value</code> to a string.
     *
     * @param value
     *    the value to convert, can be <code>null</code>.
     *
     * @return
     *    the textual representation of the value in the ISO format YYYYMMDDhhmmss,
     *    or <code>null</code> if and only if <code>value == null</code>.
     */
    public static String toString(Value value) {
 
       // Short-circuit if the argument is null
       if (value == null) {
          return null;
       }
 
       return toString(value.getYear(),
                       value.getMonthOfYear(),
                       value.getDayOfMonth(),
                       value.getHourOfDay(),
                       value.getMinuteOfHour(),
                       value.getSecondOfMinute());
    }
 
    /**
     * Converts the specified combination of a year, month, day, hour,
     * minute and second to a string.
     *
     * @param year
     *    the year, must be &gt;=0 and &lt;= 9999.
     *
     * @param month
     *    the month of the year, must be &gt;= 1 and &lt;= 12.
     *
     * @param day
     *    the day of the month, must be &gt;= 1 and &lt;= 31.
     *
     * @param hour
     *    the hour of the day, must be &gt;= 0 and &lt;= 23.
     *
     * @param minute
     *    the minute of the hour, must be &gt;= 0 and &lt;= 59.
     *
     * @param second
     *    the second of the minute, must be &gt;= 0 and &lt;= 59.
     *
     * @return
     *    the textual representation of the value in the ISO format YYYYMMDDhhmmss,
     *    never <code>null</code>.
     */
    private static String toString(int year, int month, int day, int hour, int minute, int second) {
 
       // Use a buffer to create the string
       FastStringBuffer buffer = new FastStringBuffer(8);
 
       // Append the year
       if (year < 10) {
          buffer.append("000");
       } else if (year < 100) {
          buffer.append("00");
       } else if (year < 1000) {
          buffer.append('0');
       }
       buffer.append(year);
 
       // Append the month
       if (month < 10) {
          buffer.append('0');
       }
       buffer.append(month);
 
       // Append the day
       if (day < 10) {
          buffer.append('0');
       }
       buffer.append(day);
 
       // Append the hour
       if (hour < 10) {
          buffer.append('0');
       }
       buffer.append(hour);
 
       // Append the minute
       if (minute < 10) {
          buffer.append('0');
       }
       buffer.append(minute);
 
       // Append the second
       if (second < 10) {
          buffer.append('0');
       }
       buffer.append(second);
 
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Timestamp</code> instance.
     * This constructor is private, the field {@link #SINGLETON} should be
     * used.
     */
    private Timestamp() {
       super("timestamp", Value.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected final boolean isValidValueImpl(String value) {
 
       // First check the length
       if (value.length() != 14) {
          return false;
       }
 
       // Convert all 3 components of the string to integers
       int y, m, d, h, mn, s;
       try {
          y = Integer.parseInt(value.substring(0, 4));
          m = Integer.parseInt(value.substring(4, 6));
          d = Integer.parseInt(value.substring(6, 8));
          h = Integer.parseInt(value.substring(8, 10));
          mn = Integer.parseInt(value.substring(10, 12));
          s = Integer.parseInt(value.substring(12, 14));
       } catch (NumberFormatException nfe) {
          return false;
       }
 
       // Check that the values are in the correct range
       return (y >= 0) && (m >= 1) && (m <= 12) && (d >= 1) && (d <= 31) &&
          (h >= 0) && (h <= 23) && (mn >= 0) && (mn <= 59) && (s >= 0) && (s <= 59);
    }
 
    protected final Object fromStringImpl(String string)
    throws TypeValueException {
 
       // Convert all 3 components of the string to integers
       int y, m, d, h, mn, s;
       try {
          y = Integer.parseInt(string.substring(0, 4));
          m = Integer.parseInt(string.substring(4, 6));
          d = Integer.parseInt(string.substring(6, 8));
          h = Integer.parseInt(string.substring(8, 10));
          mn = Integer.parseInt(string.substring(10, 12));
          s = Integer.parseInt(string.substring(12, 14));
       } catch (NumberFormatException nfe) {
 
          // Should never happen, since isValidValueImpl(String) will have been
          // called
          throw new TypeValueException(this, string);
       }
 
       // Check that the values are in the correct range
       return new Value(y, m, d, h, mn, s);
    }
 
    public final String toString(Object value)
    throws IllegalArgumentException, ClassCastException, TypeValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("value", value);
 
       // The argument must be a PropertyReader
       return toString((Value) value);
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Timestamp value, composed of a year, month, day, hour, minute and a second.
     *
     * @version $Revision$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     *
     * @since XINS 0.173
     */
    public static final class Value {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new timestamp value. The values will not be checked.
        *
        * @param year
        *    the year, e.g. <code>2004</code>.
        *
        * @param month
        *    the month of the year, e.g. <code>11</code> for November.
        *
        * @param day
        *    the day of the month, e.g. <code>1</code> for the first day of the
        *    month.
        *
        * @param hour
        *    the hour of the day, e.g. <code>22</code> or <code>0</code> for
        *    the first hour of the day.
        *
        * @param minute
        *    the minute of the hour, e.g. <code>0</code> for first minute of
        *    the hour.
        *
        * @param second
        *    the second of the minute, e.g. <code>0</code> for the first second
        *    of the minute.
        */
       public Value(int year, int month, int day, int hour, int minute, int second) {
          _year   = year;
          _month  = month;
          _day    = day;
          _hour   = hour;
          _minute = minute;
          _second = second;
 
          _asString = Timestamp.toString(year, month, day, hour, minute, second);
       }
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The year. E.g. <code>2004</code>.
        */
       private final int _year;
 
       /**
        * The month of the year. E.g. <code>11</code> for November.
        */
       private final int _month;
 
       /**
        * The day of the month. E.g. <code>1</code> for the first day of the
        * month.
        */
       private final int _day;
 
       /**
        * The hour of the day. E.g. <code>22</code> or <code>0</code> for
        * the first hour of the day.
        */
       private final int _hour;
 
       /**
        * The minute of the hour. E.g. <code>0</code> for first minute of
        * the hour.
        */
       private final int _minute;
 
       /**
        * The second of the minute. E.g. <code>0</code> for the first second
        * of the minute.
        */
       private final int _second;
 
       /**
        * Textual representation of this timestamp. Composed of the year (YYYY),
        * month (MM), day (DD), hour (hh), minute (mm) and second (ss)
        * in the format: <em>YYYYMMDDhhmmss</em>.
        */
       private final String _asString;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the year.
        *
        * @return
        *    the year, between 0 and 9999 (inclusive).
        */
       public int getYear() {
          return _year;
       }
 
       /**
        * Returns the month of the year.
        *
        * @return
        *    the month of the year, between 1 and 12 (inclusive).
        */
       public int getMonthOfYear() {
          return _month;
       }
 
       /**
        * Returns the day of the month.
        *
        * @return
        *    the day of the month, between 1 and 31 (inclusive).
        */
       public int getDayOfMonth() {
          return _day;
       }
 
       /**
        * Returns the hour of the day.
        *
        * @return
        *    the hour of the day, between 0 and 23 (inclusive).
        */
       public int getHourOfDay() {
          return _hour;
       }
 
       /**
        * Returns the minute of the hour.
        *
        * @return
        *    the minute of the hour, between 0 and 59 (inclusive).
        */
       public int getMinuteOfHour() {
          return _minute;
       }
 
       /**
        * Returns the second of the minute.
        *
        * @return
        *    the second of the minute, between 0 and 59 (inclusive).
        */
       public int getSecondOfMinute() {
          return _second;
       }
 
       public String toString() {
          return _asString;
       }
    }
 }
