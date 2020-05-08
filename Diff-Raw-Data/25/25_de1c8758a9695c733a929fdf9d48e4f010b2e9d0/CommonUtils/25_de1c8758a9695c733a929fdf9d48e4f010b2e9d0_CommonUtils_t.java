 package org.patientview.util;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * User: james@solidstategroup.com
  * Date: 12/12/13
  * Time: 11:02
  */
public final class CommonUtils {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);
 
     private static final String DATE_FORMAT_0 = "dd-MM-yy";
     private static final String DATE_FORMAT = "yyyy-MM-dd";
     private static final String[] LENGTH_10_DATE_FORMATS = new String[] {DATE_FORMAT};
     private static final String DATE_FORMAT_1 = "dd.MM.y";
     private static final String DATE_FORMAT_2 = "dd-MM-y";
     private static final String DATE_FORMAT_3 = "dd/MM/y";
     private static final String[] LENGTH_8_DATE_FORMATS = new String[]{DATE_FORMAT_0, DATE_FORMAT_1, DATE_FORMAT_2,
             DATE_FORMAT_3};
    private static final int LENGTH_OF_RADAR_DATES = 8;

    private CommonUtils() {

    }
 
     /**
      * Class to return the date from the database text field representation of a date.
      *
      *
      * @param dateField
      * @return
      */
     public static Date parseDate(String dateField) {
 
         if (StringUtils.hasText(dateField)) {
 
             // select the dat mask of the length of the field
             String[] dataFormats;
            if (dateField.length() == LENGTH_OF_RADAR_DATES) {
                 dataFormats = LENGTH_8_DATE_FORMATS;
             } else {
                 dataFormats = LENGTH_10_DATE_FORMATS;
             }
 
             Date dateOfBirth = null;
             // It seems that the strings in the DB have different date formats, nice.
             for (String dateFormat : dataFormats) {
                 try {
                     dateOfBirth = new SimpleDateFormat(dateFormat).parse(dateField);
                     break;
                 } catch (ParseException e) {
                     LOGGER.debug("Could not parse date of birth {}", dateField);
                 }
             }
 
             return dateOfBirth;
         }
 
         return null;
     }
 }
