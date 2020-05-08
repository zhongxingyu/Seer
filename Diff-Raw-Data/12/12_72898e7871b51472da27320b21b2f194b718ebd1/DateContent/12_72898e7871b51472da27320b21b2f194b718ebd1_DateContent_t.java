 package com.github.croesch.contents;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.regex.Pattern;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 
 import com.github.croesch.logging.Log;
 
 /**
  * This document provides the mechanism to accept only date values.
  * 
  * @author croesch
  * @since Date: 27.01.2011 18:39:41
 * @deprecated use {@link com.github.croesch.contents.date.DateContent} instead.<br>
  *             <b>Caution</b>: This class will be removed in one of the next releases.
  */
 @Deprecated
 public class DateContent extends RegexContent {
 
   /** generated serial version UID */
   private static final long serialVersionUID = -5689505532696673515L;
 
   /** dummy variable to identify the year part */
   private static final int DUMMY_YEAR = 1234;
 
   /** dummy variable to identify the month part */
   private static final int DUMMY_MONTH = 56;
 
   /** dummy variable to identify the day part */
   private static final int DUMMY_DAY = 78;
 
   /** instance of a calendar to define current date */
   private final Calendar cal = new GregorianCalendar();
 
   /** the string that is used to format the date shown in the field */
   private String dateFormat = "%04d-%02d-%02d";
 
   /** string to parse a formatted date */
   private String dateFormatString = "yyyy-MM-dd";
 
   /** the number of the current year */
   private final int currentYear = this.cal.get(Calendar.YEAR);
 
   /** the number of the current month */
   private final int currentMonth = this.cal.get(Calendar.MONTH) + 1;
 
   /** the number of the current day */
   private final int currentDay = this.cal.get(Calendar.DAY_OF_MONTH);
 
   /** the string representation of the current date */
   private String today = formatDate(this.currentYear, this.currentMonth, this.currentDay);
 
   /** regular expression to define allowed input for this document */
   private static final String DATE_PATTERN = "[0-9_S_]{0,10}";
 
   /**
    * Constructs a document with maximum input length >10< and that is not displaying errors
    * 
    * @author croesch
    * @since Date: 28.01.2011 20:48:31
   * @deprecated use
   *             {@link com.github.croesch.contents.date.DateContent#createDateContent(com.github.croesch.contents.date.DateContent.MODE, javax.swing.text.JTextComponent, java.util.Locale)}
   *             instead.
    */
   @Deprecated
   public DateContent() {
     super(DATE_PATTERN.replace("_S_", Pattern.quote("-"))); //$NON-NLS-1$
     final int maxInput = 10;
 
     setErrorsNotifying(false);
     setMaximumInputLength(maxInput);
   }
 
   /**
    * Constructs a date document and inserts the given value. If it's not a valid value nothing will be inserted
    * 
    * @author croesch
    * @since Date: 28.01.2011 20:48:57
    * @param initial the value to insert
   * @deprecated use
   *             {@link com.github.croesch.contents.date.DateContent#createDateContent(com.github.croesch.contents.date.DateContent.MODE, javax.swing.text.JTextComponent, java.util.Locale)}
   *             instead.
    */
   @Deprecated
   public DateContent(final String initial) {
     this();
     try {
       insertString(0, initial, null);
     } catch (final BadLocationException e) {
       Log.error(e);
     }
   }
 
   /**
    * Formats the given values to the format of the date.
    * 
    * @author croesch
    * @since Date: Mar 30, 2011 9:19:13 PM
    * @param day the number of the day of the generated date
    * @param month the number of month of the generated date
    * @param year the number of the year of the generated date
    * @return a string representation of the formated date
    * @see #setDateFormat(String)
    */
   public final String formatDate(final int year, final int month, final int day) {
     final String date = String.format(this.dateFormat, year, month, day);
 
     final SimpleDateFormat sdf = new SimpleDateFormat(this.dateFormatString);
 
     try {
       return sdf.format(sdf.parse(date));
     } catch (final ParseException e) {
       Log.error(e);
       return null;
     }
   }
 
   /** Provides constants to describe different date formats, '_S_' will be replaced by the separator of the date */
   protected enum Format {
     /** constant for format dd_S_MM_S_yyyy */
     DMY ("%3$02d_S_%2$02d_S_%1$04d"),
 
     /** constant for format dd_S_yyyy_S_dd */
     DYM ("%3$02d_S_%1$04d_S_%2$02d"),
 
     /** constant for format yyyy_S_MM_S_dd */
     YMD ("%1$04d_S_%2$02d_S_%3$02d"),
 
     /** constant for format yyyy_S_dd_S_MM */
     YDM ("%1$04d_S_%3$02d_S_%2$02d"),
 
     /** constant for format MM_S_dd_S_yyyy */
     MDY ("%2$02d_S_%3$02d_S_%1$04d"),
 
     /** constant for format MM_S_yyyy_S_dd */
     MYD ("%2$02d_S_%1$04d_S_%3$02d");
 
     /** the formating string of this date format */
     private String formatString;
 
     /**
      * Constructs a new date format with the given formating {@link String}. This string contains "_S_"-sequences,
      * that'll be replaced by the real separator of the date.
      * 
      * @author croesch
      * @since Date: Mar 31, 2011 1:24:15 PM
      * @param fs the formating string for this date format
      */
     private Format(final String fs) {
       this.formatString = fs;
     }
 
     /**
      * Returns the formating {@link String} of this date format.
      * 
      * @author croesch
      * @since Date: Mar 31, 2011 1:25:10 PM
      * @return the formating string to format dates of this date format
      */
     protected String getFormatString() {
       return this.formatString;
     }
   }
 
   /**
    * Sets the format to format the date. Will intern format the {@link #today}-variable, to be up to date.
    * 
    * @author croesch
    * @since Date: Mar 30, 2011 9:23:42 PM
    * @param dFormat the new format of the date.
    * @param separator the separator for the new date format
    * @see #formatDate(int, int, int)
    */
   protected final void setDateFormat(final Format dFormat, final String separator) {
     if (dFormat != null && separator != null && separator.length() > 0) {
       setRegularExpression(DATE_PATTERN.replace("_S_", Pattern.quote(separator)));
       // set separator
       this.dateFormat = dFormat.getFormatString().replaceAll("_S_", separator);
       // create parsing string
       this.dateFormatString = String.format(this.dateFormat, DUMMY_YEAR, DUMMY_MONTH, DUMMY_DAY)
         .replace(String.valueOf(DUMMY_YEAR), "yyyy").replace(String.valueOf(DUMMY_MONTH), "MM")
         .replace(String.valueOf(DUMMY_DAY), "dd");
       // update intern values
       this.today = formatDate(this.currentYear, this.currentMonth, this.currentDay);
     }
   }
 
   /**
    * Returns the current value of the document. There is the special behaviour that it will return a String with length
    * >10< if the current input is not long enough the string will be filled up with the characters from the current
    * date.
    * 
    * @author croesch
    * @since Date: 28.01.2011 20:50:13
    * @return a String with length: 10
    */
   public final String getDate() {
     if (getText() != null) {
       if (getLength() < getMaximumInputLength()) {
         return getText() + this.today.substring(this.today.length() + getLength() - getMaximumInputLength());
       }
       return getText();
     }
     return this.today;
   }
 
   /**
    * @return {@code true}, if {@link #getDate()} returns a valid date.
    */
   @Override
   public final boolean isValid() {
     final String txt = getDate();
     if (txt == null) {
       return false;
     }
     try {
       final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
       final Date parsed = sdf.parse(txt);
       return txt.equals(sdf.format(parsed));
     } catch (final ParseException e) {
       Log.error(e);
       return false;
     }
   }
 
   @Override
   public final void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
     if (isValidInput(offs, str)) {
       if ("d".equals(str)) {
         super.remove(0, getLength());
         super.insertString(0, this.today, a);
       } else {
         super.insertString(offs, str, a);
       }
     }
   }
 
   @Override
   public final void remove(final int offs, final int len) throws BadLocationException {
     super.remove(offs, len);
   }
 
   @Override
   public final void replace(final int offset, final int length, final String text, final AttributeSet attrs)
                                                                                                             throws BadLocationException {
     final String newText = getText(0, offset) + text + getText(offset + length, getLength() - offset - length);
     if (isValidInput(newText)) {
       super.replace(offset, length, text, attrs);
     }
   }
 }
