 /*
  * $Source$
  * $Revision$
  *
 * Copyright (C) 2000 Tim Joyce
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
 *     Tim Joyce  <timj At paneris.org>
 *     http://paneris.org/~timj
  */
 
 package org.melati.template;
 
 import java.sql.Date;
 import java.text.DateFormat;
 import java.util.Calendar;
 
 import org.melati.poem.BaseFieldAttributes;
 import org.melati.poem.Field;
 import org.melati.poem.IntegerPoemType;
 import org.melati.poem.PoemLocale;
 import org.melati.poem.SQLPoemType;
 
 /**
  * A numeric year type. 
  */
 
 class YearPoemType extends IntegerPoemType {
   /** First year for a dropdown. */
   static final int firstYear = 2000; 
  /** Limit  (excluded)  year for a dropdown. */
   static final int limitYear = 2023;
   
   /**
    * Constructor.
    * @param nullable whether null is an allowed value
    * @param low lower (inclusive) limit
    * @param limit upper (exclusive) limit
    */
   public YearPoemType(boolean nullable, int low, int limit) {
     super(nullable);
     setRawRange(new Integer(low), new Integer(limit));
   }
 
   protected boolean _canRepresent(SQLPoemType other) {
     return other instanceof YearPoemType;
   }
 
   /**
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   public String toString() {
     return super.toString() + " (year)";
   }
 }
 
 /**
  * A numeric month type.
  */
 class MonthPoemType extends IntegerPoemType {
 
   /**
    * Constructor.
    * @param nullable whether null is an allowed value
    */
   public MonthPoemType(boolean nullable) {
     super(nullable);
     setRawRange(new Integer(1), new Integer(13));
   }
 
   protected boolean _canRepresent(SQLPoemType other) {
     return other instanceof MonthPoemType;
   }
 
   protected String _stringOfCooked(Object raw,
                                    PoemLocale locale, int style) {
     int m = ((Integer)raw).intValue();
     switch (style) {
       case DateFormat.FULL: case DateFormat.LONG:
         return locale.monthName(m);
       case DateFormat.MEDIUM:
         return locale.shortMonthName(m);
       default:
         return "" + m;
     }
   }
 
   /**
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   public String toString() {
     return super.toString() + " (month)";
   }
 }
 
 /**
  * A numeric day type.
  */
 class DayPoemType extends IntegerPoemType {
 
   /**
    * Constructor.
    * @param nullable whether null is an allowed value
    */
   public DayPoemType(boolean nullable) {
     super(nullable);
     setRawRange(new Integer(1), new Integer(32));
   }
 
   protected boolean _canRepresent(SQLPoemType other) {
     return other instanceof DayPoemType;
   }
 
   /**
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   public String toString() {
     return super.toString() + " (day)";
   }
 }
 
 /**
  * An adaptor for a string date in YMD format.
  * See for example org.melati.poem.DatePoemType-dropdown.wm
  */
 public class YMDDateAdaptor implements TempletAdaptor {
 
   protected static final String
       yearSuffix = "-year",
       monthSuffix = "-month",
       daySuffix = "-day";
 
   /** The instance. */
   public static final YMDDateAdaptor it = new YMDDateAdaptor();
 
   protected String getFormOrDie(ServletTemplateContext context,
                               String fieldName, String suffix) {
     String fullName = fieldName + suffix;
     String value = context.getFormField(fullName);
     if (value == null)
       throw new MissingFieldException(this, fieldName, fullName);
     return value;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.TempletAdaptor#
    *          rawFrom(org.melati.template.ServletTemplateContext, java.lang.String)
    */
   public Object rawFrom(ServletTemplateContext context, String fieldName) {
     String year = getFormOrDie(context, fieldName, yearSuffix);
     String month = getFormOrDie(context, fieldName, monthSuffix);
     String day = getFormOrDie(context, fieldName, daySuffix);
 
     if (year.equals("") && month.equals("") && day.equals(""))
       return null;
    else if (!year.equals("") && !month.equals("") ) {
      if (day.equals("")) // MYDate template need default day
        day = "1";
       Calendar cal = Calendar.getInstance();
       cal.set(Integer.parseInt(year),
               Integer.parseInt(month) - 1,
               Integer.parseInt(day));
       return new Date(cal.getTime().getTime());
     } else {
       throw new PartlyNullException(fieldName);
     }
   }
 
   /**
    * @param dateField date field to extract year field from 
    * @return year constituent of date
    */
   public Field yearField(Field dateField) {
 
     Calendar when = when(dateField);
 
    // This isn't meant to be used, so we don't try to localise it
     String displayName = dateField.getDisplayName() + " (year)";
 
     return new Field(
         when == null ? null : new Integer(when.get(Calendar.YEAR)),
         new BaseFieldAttributes(
             dateField.getName() + yearSuffix,
             displayName,
             null,
             new YearPoemType(dateField.getType().getNullable(),
                              YearPoemType.firstYear, YearPoemType.limitYear),
                              5, 1,
                              null, false, true, true));
   }
 
   /**
    * @param dateField date field to extract month field from 
    * @return month constituent of date
    */
   public Field monthField(Field dateField) {
 
     Calendar when = when(dateField);
    // This isn't meant to be used, so we don't try to localise it
 
     String displayName = dateField.getDisplayName() + " (month)";
 
     return new Field(
         when == null ? null : new Integer(when.get(Calendar.MONTH) + 1),
         new BaseFieldAttributes(
             dateField.getName() + monthSuffix, displayName, null,
             dateField.getType().getNullable() ? new MonthPoemType(true) :
                                             new MonthPoemType(false),
             3, 1,
             null, false, true, true));
   }
 
   /**
    * @param dateField date field to extract day field from 
    * @return day constituent of date
    */
   public Field dayField(Field dateField) {
 
     Calendar when = when(dateField);
    // This isn't meant to be used, so we don't try to localise it
 
     String displayName = dateField.getDisplayName() + " (day)";
 
     return new Field(
         when == null ? null : new Integer(when.get(Calendar.DAY_OF_MONTH)),
         new BaseFieldAttributes(
             dateField.getName() + daySuffix, displayName, null,
             dateField.getType().getNullable() ? new DayPoemType(true) :
                                             new DayPoemType(false),
             2, 1,
             null, false, true, true));
   }
   
   protected Calendar when(Field dateField) {
     if (dateField.getRaw() == null) return null;
     Calendar when = Calendar.getInstance();
     when.setTime((java.util.Date)dateField.getRaw());
     return when;
   }
 }
