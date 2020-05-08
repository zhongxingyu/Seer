 /*
  * $Id$
  */
 package org.xins.util.text;
 
 import java.util.Calendar;
 import java.util.TimeZone;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Utility class for converting an Epoch date to a human-readable time stamp.
  *
  * <p>For example, the date 26 July 2003, time 17:03, 59 seconds and 653
  * milliseconds will convert to the string <code>"2003.07.26
  * 17:03:59.653"</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.89
  */
 public class DateConverter extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Two-length string representations of the digits 0 to 59.
     */
    private static final String[] VALUES = new String[] {
       "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
       "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
       "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
       "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
       "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
       "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" };
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Convert the specified <code>long</code> to a human-readable time stamp.
     *
     * @param timeZone
     *    the time zone to use, cannot be <code>null</code>.
     *
     * @param n
     *    the time stamp to be converted to a human-readable character string,
     *    as a number of milliseconds since the Epoch (midnight January 1,
     *    1970).
     *
     * @return
     *    the converted character string, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>timeZone == null</code>.
     */
    public static String toDateString(TimeZone timeZone, long n) {
 
       // Check preconditions
       MandatoryArgumentChecker.check("timeZone", timeZone);
 
       FastStringBuffer buffer = new FastStringBuffer(23);
 
       Calendar calendar = Calendar.getInstance();
       calendar.setTimeZone(timeZone);
       calendar.setTimeInMillis(n);
 
       int year  = calendar.get(Calendar.YEAR);
       int month = calendar.get(Calendar.MONTH);
       int day   = calendar.get(Calendar.DAY_OF_MONTH);
       int hour  = calendar.get(Calendar.HOUR_OF_DAY);
       int min   = calendar.get(Calendar.MINUTE);
       int sec   = calendar.get(Calendar.SECOND);
       int ms    = calendar.get(Calendar.MILLISECOND);
 
       // Append year followed by a dot, length is now 5
       buffer.append(String.valueOf(year));
       buffer.append('.');
 
       // Append month followed by a dot, length is now 8
      buffer.append(VALUES[month]);
       buffer.append('.');
 
       // Append day followed by a space, length is now 11
       buffer.append(VALUES[day]);
       buffer.append(' ');
 
       // Append hour followed by a colon, length is now 14
       buffer.append(VALUES[hour]);
       buffer.append(':');
 
       // Append minute followed by a colon, length is now 17
       buffer.append(VALUES[min]);
       buffer.append(':');
 
       // Append second followed by a dot, length is now 20
       buffer.append(VALUES[sec]);
       buffer.append('.');
 
       // Append milli-second, length is now 23
       if (ms < 10) {
          buffer.append("00");
       } else if (ms < 100) {
          buffer.append('0');
       }
       buffer.append(String.valueOf(ms));
 
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>DateConverter</code> object.
     */
    private DateConverter() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
