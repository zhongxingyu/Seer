 package org.worldvision.sierraleone;
 
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public final class Utils {
     private static final Logger logger = LoggerFactory.getLogger("org.worldvision.sierraleone.Utils");
 
     static Pattern p = Pattern.compile("0(\\d\\d \\d\\d\\d \\d\\d\\d)");
 
     // It is called "mother_phone_number".  Are you able to do post-processing on the number to add in the prefix +232.
     // They will be typing the number as 0## ### ###.  You would need to turn that into +232 ## ### ###
     // (i.e. remove the 0 and add the +232).
     public static String mungeMothersPhone(String phone) {
         String ret = null;
 
         if (null == phone) {
             return null;
         }
 
         Matcher m = p.matcher(phone);
         if (m.matches()) {
             MatchResult mr = m.toMatchResult();
 
             logger.info("Group 1: " + mr.group(1));
             logger.info("Result: +232 " + mr.group(1));
 
             ret = "+232 " + mr.group(1);
         } else {
             logger.error("Phone " + phone + " does not match expected pattern 0## ### ###");
         }
 
         return ret;
     }
 
     public static DateTime dateTimeFromCommcareDateString(String dateStr) {
        if (null == dateStr) {
            return null;
        }

         DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
                 .appendYear(4, 4)
                 .appendLiteral('-')
                 .appendMonthOfYear(2)
                 .appendLiteral('-')
                 .appendDayOfMonth(2)
                 .toFormatter();
 
         return dateFormatter.parseDateTime(dateStr);
     }
 }
