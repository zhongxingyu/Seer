 /**
  *  @(#)DateOffsetScanListener.
  *  Copyright © 2010 tourapp.com. All rights reserved.
  */
 package org.jbundle.app.program.script.scan;
 
 import java.awt.*;
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.base.db.xmlutil.*;
 import java.text.*;
 
 /**
  *  DateOffsetScanListener - Offset dates in an XML file
 dayOffset = days to offset 
 monthOffset = 
 yearOffset = 
 endOfMonthFields = comma delimited fields to set to end of month if
 previous date was oem.
  */
 public class DateOffsetScanListener extends BaseScanListener
 {
     protected int dayOffset = 0;
     protected String[] eomFields = null;
     protected int monthOffset = 0;
     protected int yearOffset = 0;
     /**
      * Default constructor.
      */
     public DateOffsetScanListener()
     {
         super();
     }
     /**
      * Constructor.
      */
     public DateOffsetScanListener(RecordOwnerParent parent, String strSourcePrefix)
     {
         this();
         this.init(parent, strSourcePrefix);
     }
     /**
      * Init Method.
      */
     public void init(RecordOwnerParent parent, String strSourcePrefix)
     {
         super.init(parent, strSourcePrefix);
         if (this.getProperty("dayOffset") != null)
             dayOffset = Integer.parseInt(this.getProperty("dayOffset"));
         if (this.getProperty("monthOffset") != null)
             monthOffset = Integer.parseInt(this.getProperty("monthOffset"));
         if (this.getProperty("yearOffset") != null)
             yearOffset = Integer.parseInt(this.getProperty("yearOffset"));
         if (this.getProperty("endOfMonthFields") != null)
         {
             String fields = this.getProperty("endOfMonthFields");
             eomFields = fields.split(",");
         }
     }
     /**
      * Do any string conversion on the file text.
      */
     public String convertString(String string)
     {
         if (string != null)
         {
             int startTag = string.indexOf('<');
             int endTag = string.indexOf('>');
             if (startTag > -1)
                 if (endTag > startTag)
             {
                 String tag = string.substring(startTag + 1, endTag);
                boolean bDateField = tag.toUpperCase().contains("DATE");
                if (eomFields != null)
                {
                    for (String token : eomFields)
                    {
                        if (tag.equals(token))
                            bDateField = true;
                    }
                }
                if (bDateField)
                 {
                     int endData = string.indexOf('<', endTag);
                     if (endData != -1)
                     {
                         String dateString = string.substring(endTag + 1, endData);
                         Date date = new Date();
                         int type = this.parseDate(dateString, date);
                         if (type != 0)
                         {
                             Calendar calendar = Calendar.getInstance();
                             calendar.setTime(date);
                             if (yearOffset != 0)
                             {
                                 calendar.add(Calendar.YEAR, yearOffset);                                
                             }
                             int day = 0;
                             if (monthOffset != 0)
                             {
                                 calendar.add(Calendar.DAY_OF_MONTH, 1);
                                 day = calendar.get(Calendar.DAY_OF_MONTH);  // Just want to see if this was the end of the month
                                 calendar.add(Calendar.DAY_OF_MONTH, -1);     // Restore original date
                                 calendar.add(Calendar.MONTH, monthOffset);  // Add the month offset
                                 if (eomFields != null)
                                 {
                                     for (String token : eomFields)
                                     {
                                         if (tag.equals(token))
                                         {
                                             if (day == 1)
                                             {   // end of (next) month
                                                 calendar.add(Calendar.DAY_OF_MONTH, 5);     // next month
                                                 calendar.set(Calendar.DAY_OF_MONTH, 1);     // First of next month
                                                 calendar.add(Calendar.DAY_OF_MONTH, -1);    // End of month
                                             }
                                         }
                                     }
                                 }
                             }
                             if (dayOffset != 0)
                             {
                                 calendar.add(Calendar.DAY_OF_MONTH, dayOffset);                                
                             }
                             date = calendar.getTime();
                             
                             String newDateString = this.formatDate(date, type);
                             if (newDateString != null)
                                 string = string.substring(0, endTag + 1) + newDateString + string.substring(endData);
                         }
                     }
                 }
             }
         }
         return super.convertString(string);
     }
     /**
      * Decode date time value and set the field value.
      * @param field
      * @return.
      */
     public int parseDate(String strValue, Date date)
     {
         int type = 0;
         if (strValue == null)
             return type;
         
         Date parsedDate = null;
         try {
                 parsedDate = XmlUtilities.dateTimeFormat.parse(strValue);
                 type = DBConstants.DATE_TIME_FORMAT;
         } catch (ParseException e) {
         }
         
         try {
             if (parsedDate == null)
             {
                 parsedDate = XmlUtilities.dateFormat.parse(strValue);
                 type = DBConstants.DATE_ONLY_FORMAT;
             }
         } catch (ParseException e) {
         }
         
         try {
             if (parsedDate == null)
             {
                 parsedDate = XmlUtilities.timeFormat.parse(strValue);
                 type = DBConstants.TIME_ONLY_FORMAT;
             }
         } catch (ParseException e) {
         }
         
         if (parsedDate != null)
             date.setTime(parsedDate.getTime());
         else
             type = 0;
         
         return type;
     }
     /**
      * FormatDate Method.
      */
     public String formatDate(Date date, int type)
     {
         String string = null;
         if (type == DBConstants.DATE_TIME_FORMAT)
             string = XmlUtilities.dateTimeFormat.format(date);
         else if (type == DBConstants.DATE_ONLY_FORMAT)
             string = XmlUtilities.dateFormat.format(date);
         else if (type == DBConstants.TIME_ONLY_FORMAT)
             string = XmlUtilities.timeFormat.format(date);
         return string;
     }
 
 }
