 package com.business.utilities;
 
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.text.MessageFormat;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.collections.map.CaseInsensitiveMap;
 import org.apache.commons.lang3.BooleanUtils;
 import org.apache.commons.lang3.ObjectUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.apache.commons.lang3.time.FastDateFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Common {
     public static final String FORMAT_ISO8601_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssZ";
     
     private static final Logger LOG = LoggerFactory.getLogger(Common.class);
 
     private static List fullNameStateTerritoryList;
 
     private static Map states;
 
     private static String[] abbrevs = { "AL", "AK", "AS", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FM", "FL", "GA",
             "GU", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MH", "MD", "MA", "MI", "MN", "MS", "MO", "MT",
             "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "MP", "OH", "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD",
             "TN", "TX", "UT", "VT", "VI", "VA", "WA", "WV", "WI", "WY" };
 
     private static List territories = Arrays.asList(new String[] { "as", "fm", "gu", "mh", "mp", "pw", "pr", "vi" });
     static {
 
         /*
                 State Abbreviations
 
                 State/Possession
                 Abbreviation
 
                 ALABAMA                         AL
                 ALASKA                          AK
                 AMERICAN SAMOA                  AS
                 ARIZONA                         AZ
                 ARKANSAS                        AR
                 CALIFORNIA                      CA
                 COLORADO                        CO
                 CONNECTICUT                     CT
                 DELAWARE                        DE
                 DISTRICT OF COLUMBIA            DC
                 FEDERATED STATES OF MICRONESIA  FM
                 FLORIDA                         FL
                 GEORGIA                         GA
                 GUAM                            GU
                 HAWAII                          HI
                 IDAHO                           ID
                 ILLINOIS                        IL
                 INDIANA                         IN
                 IOWA                            IA
                 KANSAS                          KS
                 KENTUCKY                        KY
                 LOUISIANA                       LA
                 MAINE                           ME
                 MARSHALL ISLANDS                MH
                 MARYLAND                        MD
                 MASSACHUSETTS                   MA
                 MICHIGAN                        MI
                 MINNESOTA                       MN
                 MISSISSIPPI                     MS
                 MISSOURI                        MO
                 MONTANA                         MT
                 NEBRASKA                        NE
                 NEVADA                          NV
                 NEW HAMPSHIRE                   NH
                 NEW JERSEY                      NJ
                 NEW MEXICO                      NM
                 NEW YORK                        NY
                 NORTH CAROLINA                  NC
                 NORTH DAKOTA                    ND
                 NORTHERN MARIANA ISLANDS        MP
                 OHIO                            OH
                 OKLAHOMA                        OK
                 OREGON                          OR
                 PALAU                           PW
                 PENNSYLVANIA                    PA
                 PUERTO RICO                     PR
                 RHODE ISLAND                    RI
                 SOUTH CAROLINA                  SC
                 SOUTH DAKOTA                    SD
                 TENNESSEE                       TN
                 TEXAS                           TX
                 UTAH                            UT
                 VERMONT                         VT
                 VIRGIN ISLANDS                  VI
                 VIRGINIA                        VA
                 WASHINGTON                      WA
                 WEST VIRGINIA                   WV
                 WISCONSIN                       WI
                 WYOMING                         WY
         */
 
         /*
                 String[] abbrevs = {"AL", "AK", "AS", "AZ", "AR", "CA", "CO",
                         "CT", "DE", "DC", "FM", "FL",
                         "GA", "GU", "HI","ID", "IL", "IN", "IA", "KS", "KY", "LA",
                         "ME", "MH", "MD", "MA", "MI", "MN", "MS",
                         "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY",
                         "NC", "ND", "MP", "OH", "OK", "OR", "PW",
                         "PA", "PR", "RI", "SC", "SD", "TN", "TX",
                         "UT", "VT", "VI", "VA", "WA", "WV", "WI", "WY"};
         */
         String[] names = { "ALABAMA", "ALASKA", "AMERICAN SAMOA", "ARIZONA", "ARKANSAS", "CALIFORNIA", "COLORADO",
                 "CONNECTICUT", "DELAWARE", "DISTRICT OF COLUMBIA", "FEDERATED STATES OF MICRONESIA", "FLORIDA",
                 "GEORGIA", "GUAM", "HAWAII", "IDAHO", "ILLINOIS", "INDIANA", "IOWA", "KANSAS", "KENTUCKY", "LOUISIANA",
                 "MAINE", "MARSHALL ISLANDS", "MARYLAND", "MASSACHUSETTS", "MICHIGAN", "MINNESOTA", "MISSISSIPPI",
                 "MISSOURI", "MONTANA", "NEBRASKA", "NEVADA", "NEW HAMPSHIRE", "NEW JERSEY", "NEW MEXICO", "NEW YORK",
                 "NORTH CAROLINA", "NORTH DAKOTA", "NORTHERN MARIANA ISLANDS", "OHIO", "OKLAHOMA", "OREGON", "PALAU",
                 "PENNSYLVANIA", "PUERTO RICO", "RHODE ISLAND", "SOUTH CAROLINA", "SOUTH DAKOTA", "TENNESSEE", "TEXAS",
                 "UTAH", "VERMONT", "VIRGIN ISLANDS", "VIRGINIA", "WASHINGTON", "WEST VIRGINIA", "WISCONSIN", "WYOMING" };
         states = new CaseInsensitiveMap();
         for (int i = 0; i < names.length; ++i) {
             states.put(abbrevs[i], names[i]);
         }
 
         fullNameStateTerritoryList = new ArrayList();
         Map row;
         for (int i = 0; i < names.length; ++i) {
             row = new CaseInsensitiveMap();
             row.put("abbrev", abbrevs[i]);
             row.put("name", names[i]);
             fullNameStateTerritoryList.add(row);
         }
     }
 
     public static List getStateTerritoryAbbrevs() {
         return Arrays.asList(abbrevs);
     }
 
     public static List getStateAbbrevs() {
         List abbrevList = getStateTerritoryAbbrevs();
         List retVal = new ArrayList();
         String abbr;
         for (Iterator it = abbrevList.iterator(); it.hasNext();) {
             abbr = (String) it.next();
             if (!territories.contains(StringUtils.lowerCase(abbr))) {
                 retVal.add(abbr);
             }
         }
         return retVal;
     }
 
     public static List getFullNameStateTerritoryList() {
         return fullNameStateTerritoryList;
     }
 
     public static List getFullNameStateList() {
         List fullNameStateList = getFullNameStateTerritoryList();
         List retVal = new ArrayList();
         Map row;
         for (Iterator it = fullNameStateList.iterator(); it.hasNext();) {
             row = (Map) it.next();
             if (!territories.contains(StringUtils.lowerCase(MapUtils.getString(row, "abbrev")))) {
                 retVal.add(row);
             }
         }
         return retVal;
     }
 
     public static Map getAbbrevStateTerritoryMap() {
         return states;
     }
 
     public static Map getAbbrevStateMap() {
         Set stateSet = states.keySet();
         Map stateMap = new CaseInsensitiveMap();
         String abbrev;
         for (Iterator it = stateSet.iterator(); it.hasNext();) {
             abbrev = (String) it.next();
             if (!territories.contains(StringUtils.lowerCase(abbrev))) {
                 stateMap.put(abbrev, states.get(abbrev));
             }
         }
         return stateMap;
     }
 
     public static boolean nullEqual(String string1, String string2) {
         boolean retVal = false;
         if (StringUtils.isEmpty(string1) && StringUtils.isEmpty(string2)) {
             retVal = true;
         }
         return retVal;
     }
 
     public static String getStackTrace(Throwable t) {
         return ExceptionUtils.getStackTrace(t);
     }
 
     public static String stripNonNumeric(String value) {
         return stripNonNumeric(value, false);
     }
 
     public static String stripNonNumeric(String value, boolean bKeepDecimal) {
         return stripNonNumeric(value, bKeepDecimal, false);
     }
 
     /**
      * Modified this method to allow Parenthetical negatives
      * @param value
      * @param bKeepDecimal
      * @param allowNegs
      * @return the string minus the non numeric characters
      */
     public static String stripNonNumeric(String value, boolean bKeepDecimal, boolean allowNegs) {
         String retVal = value;
         if (value != null && value.length() > 0) {
             value = StringUtils.remove(value, "$");
 
             char[] chars = value.toCharArray();
             String nValue = "";
 
             // strip all special characters [specifically $ and , and .]
             boolean isNeg = StringUtils.startsWith(value, "(") && StringUtils.endsWith(value, ")")
                     || StringUtils.startsWith(value, "-");
             if (allowNegs && isNeg) {
                 nValue += "-";
             }
             for (int i = 0; i < chars.length; ++i) {
                 if (chars[i] == '0' || chars[i] == '1' || chars[i] == '2' || chars[i] == '3' || chars[i] == '4'
                         || chars[i] == '5' || chars[i] == '6' || chars[i] == '7' || chars[i] == '8' || chars[i] == '9'
                         || (bKeepDecimal && chars[i] == '.')) {
                     nValue += chars[i];
                 }
             }
 
             retVal = nValue.length() < 1 ? "0" : nValue;
         }
         return retVal;
     }
 
     public static String format(double amt) {
         String retVal;
         Double amount = new Double(amt);
         //if (amount != null)
         //{
         NumberFormat nf = NumberFormat.getInstance();
         nf.setMinimumFractionDigits(2);
         nf.setMaximumFractionDigits(2);
         nf.setMinimumIntegerDigits(1);
 
         retVal = nf.format(amount);
         retVal = retVal.replaceAll(",", "");
         //}
         return retVal;
     }
 
     public static String formatCurrencyDropSign(Object value) {
         String temp = formatCurrency(value);
         if (StringUtils.isNotBlank(temp)) {
             temp = StringUtils.replace(temp, "$", "");
         }
         return temp;
     }
 
     public static String formatCurrencyNaForZero(Object value) {
         String ret;
         if (Common.createBigDecimal("0").compareTo(Common.createBigDecimal(ObjectUtils.toString(value))) == 0) {
             ret = "N/A";
         }
         else {
             ret = formatCurrency(value);
         }
         return ret;
     }
 
     public static String reformatNegs(String val) {
         if (val != null) {
             val = StringUtils.replace(val, "($", "$-");
             val = StringUtils.remove(val, ")");
         }
         return val;
     }
 
     public static String formatCurrency(BigDecimal input) {
         String retVal = input != null ? formatCurrency(input.doubleValue()) : null;
         return reformatNegs(retVal);
     }
 
     public static String formatCurrency(String input) {
         return NumberUtils.isNumber(input) ? formatCurrency(Double.valueOf(input)) : null;
     }
 
     public static String formatCurrency(double input) {
         NumberFormat nf = NumberFormat.getCurrencyInstance();
         return nf.format(input);
     }
 
     public static String formatCurrency(Object value) {
         NumberFormat nf = NumberFormat.getCurrencyInstance();
         String retVal = null;
         try {
             if (value != null) {
                 Object formatVal = value;
                 if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                     formatVal = Double.valueOf((String) value);
                 }
                 retVal = nf.format(formatVal);
             }
         }
         catch (IllegalArgumentException e) {
             LOG.error("given Object: " + value + "\n" + ExceptionUtils.getStackTrace(e));
         }
         return retVal;
     }
 
     public static BigDecimal unformatCurrency(String amount) {
         BigDecimal ret = null;
         if (StringUtils.isNotBlank(amount)) {
             ret = Common.createBigDecimal(Common.stripNonNumeric(amount, true, true));
             ret = ret.setScale(2, BigDecimal.ROUND_HALF_UP);
         }
         return ret;
     }
 
     /* decodes the values  */
     public static String convertStringCheckToNumString(String ckVal) {
         return convertCheckToNumString(BooleanUtils.toBoolean(ckVal));
     }
 
     public static Boolean convertLongToBoolean(Long val) {
         return val != null && val == 1L ? Boolean.TRUE : Boolean.FALSE;
     }
 
     public static String convertCheckToNumString(boolean ckVal) {
         return ckVal ? "1" : "0";
     }
 
     public static String format(String input) {
         String output = null;
         try {
             output = format(Double.parseDouble(input));
         }
         catch (Exception e) {
             LOG.error(ExceptionUtils.getStackTrace(e));
         }
         return output;
     }
 
     public static String formatSsn(String temp) {
         String retVal = temp;
         if (Pattern.matches("\\d{9}", temp)) {
             retVal = StringUtils.substring(temp, 0, 3) + "-" + StringUtils.substring(temp, 3, 5) + "-"
                     + StringUtils.substring(temp, 5, 9);
         }
         return retVal;
     }
 
     /**
      * Takes a typical DB List of maps and converts the field identified as the label to the label of an
      * optionsCollection and the field identified as the value to the value of the optionsCollection.
      *
      * @param data              The raw DB list of CaseInsensitiveMaps
      * @param label             The DB field to be converted to a label
      * @param value             The DB field to be converted to a value
      * @param firstElementEmpty Indicates the first set of label and value should be empty
      * @return A list of Maps that will work in a html:optionsCollection tag
      */
     public static List<Map<String, Object>> asOptionsCollection(List data, String label, String value,
             boolean firstElementEmpty) {
         List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>();
         if (data != null && label != null && value != null) {
             Map<String, Object> row, lvSet;
             if (firstElementEmpty) {
                 lvSet = new CaseInsensitiveMap();
                 lvSet.put("label", "");
                 lvSet.put("value", "");
                 retVal.add(lvSet);
             }
             for (Object listItem : data) {
                 if (listItem instanceof Map) {
                     row = (Map<String, Object>) listItem;
                     lvSet = new CaseInsensitiveMap();
                     lvSet.put("label", row.get(label));
                     lvSet.put("value", row.get(value));
                     retVal.add(lvSet);
                 }
                 else {
                     lvSet = new CaseInsensitiveMap();
                     lvSet.put("label", listItem);
                     lvSet.put("value", listItem);
                     retVal.add(lvSet);
                 }
             }
         }
         return retVal;
     }
 
     public static List asOptionsCollectionUpperCaseValue(List data, String label, String value,
             boolean firstElementEmpty) {
         List retVal = new ArrayList();
         if (data != null && label != null && value != null) {
             Map row, lvSet;
             if (firstElementEmpty) {
                 lvSet = new CaseInsensitiveMap();
                 lvSet.put("label", "");
                 lvSet.put("value", "");
                 retVal.add(lvSet);
             }
             for (Iterator it = data.iterator(); it.hasNext();) {
                 row = (Map) it.next();
                 lvSet = new CaseInsensitiveMap();
                 lvSet.put("label", row.get(label));
                 lvSet.put("value", StringUtils.upperCase(MapUtils.getString(row, value)));
                 retVal.add(lvSet);
             }
         }
         return retVal;
     }
 
     // useful in combination with the DatabaseUtils to return a single row, returns a null if the
     // results are empty the row exceeds the size of the list or the row is not an instance of a Map
     public static Map getRowMap(List results, int row) {
         Map retVal = null;
         if (results != null && results.size() > row && results.get(row) instanceof Map) {
             retVal = (Map) results.get(row);
         }
         return retVal;
     }
 
     // gets list of values from a string that are delimited by a particular char(delimiter).
     public static List getListInString(String emailString, String delimiter) {
         List emails = new ArrayList();
 
         if ((emailString.length() > 0) && (emailString.indexOf(delimiter) != -1)) {
             String sArray[] = emailString.split(delimiter);
 
             for (int i = 0; i < sArray.length; i++) {
                 String email = sArray[i].trim();
                 if (email.length() > 0) {
                     emails.add(email);
                 }
             }
         }
         else {
             emails.add(emailString);
         }
 
         return emails;
     }
 
     public static String asString(Object input) {
         return asString(input, "");
     }
 
     public static String asString(Object input, String defaultVal) {
         return input != null ? input.toString() : defaultVal;
     }
 
     public static java.sql.Date asSqlDate(Object date, String format) {
         java.sql.Date retVal = null;
         if (date != null) {
             String strDate = Common.asString(date);
             try {
                 if (StringUtils.isNotBlank(strDate)) {
                     retVal = new java.sql.Date((new SimpleDateFormat(format).parse(strDate)).getTime());
                 }
             }
             catch (ParseException e) {
                 LOG.error(ExceptionUtils.getStackTrace(e));
             }
         }
         return retVal;
     }
 
     public static Timestamp asTimestamp(Object date, String format) {
         Timestamp retVal = null;
         if (date != null) {
             try {
                 retVal = new Timestamp((new SimpleDateFormat(format).parse(Common.asString(date))).getTime());
             }
             catch (ParseException e) {
                 LOG.error(ExceptionUtils.getStackTrace(e));
             }
         }
         return retVal;
     }
 
     public static Timestamp asTimestamp(String inputTime) {
         return StringUtils.isNotEmpty(inputTime) ? new Timestamp(Long.parseLong(inputTime)) : null;
     }
 
     public static final int FORMAT_TYPE_NUMBER = 1;
     public static final int FORMAT_TYPE_DATE = 2;
     public static final int FORMAT_TYPE_TIME = 3;
 
     public static String formatted(int type, Object value, String pattern) {
         String retVal = Common.asString(value);
         String typeString = type == FORMAT_TYPE_NUMBER ? "number" : type == FORMAT_TYPE_DATE ? "date"
                 : type == FORMAT_TYPE_TIME ? "time" : null;
         try {
             if (StringUtils.isNotBlank(typeString) && value != null && StringUtils.isNotBlank(value.toString())) {
                 String format = "{0," + typeString + "," + pattern + "}";
                 MessageFormat mf = new MessageFormat(format);
                 retVal = mf.format(new Object[] { value });
             }
         }
         catch (IllegalArgumentException e) {
             if (value != null) {
                 LOG.warn("object, " + value + ", is of class: " + value.getClass().getName()
                         + " and cannot be formatted as type: " + typeString);
             }
             LOG.error(ExceptionUtils.getStackTrace(e));
         }
         return retVal;
     }
 
     public static Date string2Date(String dateString, String dateFormat) {
         Date retVal = null;
         if (StringUtils.isNotEmpty(dateString) && StringUtils.isNotEmpty(dateFormat)) {
             try {
                 retVal = new SimpleDateFormat(dateFormat).parse(dateString);
             }
             catch (ParseException e) {
                 LOG.error(ExceptionUtils.getStackTrace(e));
             }
         }
         return retVal;
     }
 
     public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.S Z";
 
     public static String[] patterns = {
             "\\d{8}T\\d{6}-\\d{4}",
             "\\d{8}T\\d{6} -\\d{4}",
             "\\d{8}T\\d{6}\\.\\d{3} -\\d{4}",
             "\\d{8}T\\d{6}\\.\\d{3}-\\d{4}",
             "\\d{8}T\\d{6}",
             "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}",
             "\\d{8}T\\d{4}",
             "\\d{14}",
             "\\d{8}",
            "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{1}",
            "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}",
             "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{7}-\\d{4}",
             "\\d{2}/\\d{2}/\\d{4}",
             "\\d{1,2}?/\\d{1,2}?/\\d{4}",
             "\\d{2}/\\d{2}/\\d{4} \\d{1,2}:\\d{2} (AM|PM|am|pm)",
             "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{2} \\d{4} \\d{2}:\\d{2} (AM|PM|am|pm)",
             "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{1} \\d{4} \\d{2}:\\d{2}(AM|PM|am|pm)",
             "(\\d)+/(\\d)+/\\d{4} (\\d)+:\\d{2} (AM|PM|am|pm)", 
             "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
             "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}", 
             "\\d{4}-\\d{2}-\\d{2}", 
             "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}",
             "\\d{2}/\\d{2}/\\d{4} \\d{1,2}:\\d{2}:\\d{2} (AM|PM|am|pm)", 
             "\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}",
             "\\d{8}_\\d{4}", 
             "(SUN|MON|TUE|WED|THU|FRI|SAT|Sun|Mon|Tue|Wed|Thu|Fri|Sat) (JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{2} \\d{2}:\\d{2}:\\d{2} \\S{3} \\d{4}", 
             "\\d{2}-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-\\d{2} \\d{2}.\\d{2}.\\d{2}.\\d{9} (AM|PM|am|pm)",
             "(\\d)+/(\\d)+/\\d{4} (\\d)+:\\d{2}:\\d{2} (AM|PM|am|pm)",
             "\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{2} (AM|PM|am|pm), ([a-zA-Z]){3}",
             "\\d{1,2}/\\d{1,2}/\\d{2}", 
             "\\d{1,2}/\\d{1,2}/\\d{4} \\d{2}:\\d{2}",
             "\\d{2}-\\d{2}-\\d{4}"
             };
 
     public static String[] dtPatterns = { 
         "yyyyMMdd'T'HHmmssZ", 
         "yyyyMMdd'T'HHmmss Z", 
         "yyyyMMdd'T'HHmmss.SSS Z",
         "yyyyMMdd'T'HHmmss.SSSZ", 
         "yyyyMMdd'T'HHmmss", 
         "yyyy-MM-dd'T'HH:mm:ss", 
         "yyyyMMdd'T'HHmm", 
         "yyyyMMddHHmmss", 
         "yyyyMMdd", 
         "yyyy-MM-dd HH:mm:ss.S", 
        "yyyy-MM-dd HH:mm:ss.SSS", 
         "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ", 
         "MM/dd/yyyy",
         "M/d/yyyy",
         "MM/dd/yyyy h:mm a", 
         "MMM dd yyyy hh:mm a", 
         "MMM d yyyy hh:mma", 
         "M/d/yyyy h:mm a", 
         "yyyy-MM-dd HH:mm:ss", 
         "MM/dd/yyyy HH:mm", 
         "yyyy-MM-dd", 
         "MM/dd/yyyy HH:mm:ss", 
         "MM/dd/yyyy h:mm:ss a", 
         "MM-dd-yyyy HH:mm",
         "yyyyMMdd_HHmm",
         "EEE MMM dd HH:mm:ss zzz yyyy", 
         "dd-MMM-yy HH.mm.ss.SSSSSSSSS a",
         //3/5/2012 12:00:00 AM
         "M/d/yyyy h:mm:ss a", 
         //10/31/2013 3:58 PM, EDT
         "M/d/yyyy h:mm a, z",
         "M/d/yy", 
         "M/d/yyyy HH:mm",
         "MM-dd-yyyy"
         };
 
     public static Date convertStringToDate(String inputDateStr) {
         Date retVal = null;
 
         //07/02/2009 2:53 PM
         String regexVisiblePattern = "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2} am|pm";
         String visiblePattern = "MM/dd/yyyy hh:mm:ss a";
         try {
             String dateStr = normalizeWhiteSpace(inputDateStr);
             if (StringUtils.isNotEmpty(dateStr) && dateStr.length() > 4) {
                 if (Pattern.matches(regexVisiblePattern, dateStr)) {
                     SimpleDateFormat sdf = new SimpleDateFormat(visiblePattern);
                     retVal = sdf.parse(dateStr);
                 }
                 else {
                     for (int i = 0; i < patterns.length; ++i) {
                         if (Pattern.matches(patterns[i], dateStr)) {
                             SimpleDateFormat sdf = new SimpleDateFormat(dtPatterns[i]);
                             if("\\d{8}".equals(dtPatterns[i]) && !Arrays.asList("18", "19", "20", "21").contains(StringUtils.substring(dateStr, 0, 2))){ 
                                 sdf = new SimpleDateFormat("MMddyyyy"); 
                             }
                             retVal = sdf.parse(dateStr);
                             break;
                         }
                     }
                 }
             }
         }
         catch (ParseException e) {
             LOG.error(ExceptionUtils.getStackTrace(e));
         }
         return retVal;
     }
 
     public static Date findFirstDateInString(String searchString) {
         Date date = null;
         for (int i = 0; i < patterns.length; ++i) {
             Pattern p = Pattern.compile(patterns[i]);
             Matcher m = p.matcher(searchString);
             if (m.find()) {
                 SimpleDateFormat sdf = new SimpleDateFormat(dtPatterns[i]);
                 try {
 
                     date = sdf.parse(m.group());
                     if (date != null) {
                         break;
                     }
                 }
                 catch (ParseException e) {
                     LOG.error(ExceptionUtils.getStackTrace(e));
                 }
             }
         }
         return date;
     }
 
     public static java.sql.Date convertStringToSQLDate(String dateStr) {
         java.sql.Date sqlDate = null;
         Date date = convertStringToDate(dateStr);
         if (date != null) {
             sqlDate = new java.sql.Date(date.getTime());
         }
         return sqlDate;
     }
 
     public static Date ISO8601ToDate(String certDateStr) {
         Date certDate = null;
         try {
             String regex = "[0-2][0-9]{3}-([0][1-9]|10|11|12)-([0][1-9]|[1-2][0-9]|30|31)T([0-1][0-9]|20|21|22|23):([0-5][0-9]):([0-5][0-9])[\\.[0-9]+]?([\\+|\\-]([0-1][0-9]|20|21|22|23):[0-5][0-9])";
             if (Pattern.matches(regex, certDateStr)) {
                 certDateStr = StringUtils
                         .reverse(StringUtils.replaceOnce(StringUtils.reverse(certDateStr), "00:", "00"));
             }
             certDate = new SimpleDateFormat(FORMAT_ISO8601_DATE_TIME).parse(certDateStr);
         }
         catch (ParseException e) {
             LOG.error(ExceptionUtils.getStackTrace(e));
         }
         return certDate;
     }
 
     public static double roundToNumPlaces(double value, int numPlaces) {
         double powerOf = Math.pow(10, numPlaces);
         return Math.round(value * powerOf) / powerOf;
     }
 
     public static void sleep(int i) {
         try {
             Thread.sleep(i);
         }
         catch (InterruptedException e1) {
             LOG.debug(ExceptionUtils.getStackTrace(e1));
         }
     }
 
     /**
      * Checks that the given date is at least numOfDays after the current date not counting weekends.
      * @param date the date in question
      * @param numOfDays the number of days that the given date should exceed the current date, not counting weekends
      * @return true if the date is at least numOfDays after the current date, otherwise false
      */
     public static boolean checkDate(String date, int numOfDays) {
         boolean ok = true;
 
         //String date = "03/19/2009";
         if (StringUtils.isNotBlank(date)) {
 
             Date then = Common.convertStringToDate(date);// string2Date(date, "MM/dd/yyyy");
 
             Calendar cal = new GregorianCalendar();
             cal.set(Calendar.HOUR_OF_DAY, 0);
             cal.set(Calendar.MINUTE, 0);
             cal.set(Calendar.SECOND, 0);
             cal.set(Calendar.MILLISECOND, 0);
             Date now = cal.getTime();
 
             int dow = cal.get(Calendar.DAY_OF_WEEK);
 
             int addDays = numOfDays;
             if (dow == 5) {
                 addDays++;
             }
             else if (dow >= 6) {
                 addDays = addDays + 2;
             }
             now = DateUtils.addDays(now, addDays);
 
             ok = now.getTime() <= then.getTime();
         }
 
         return ok;
     }
 
     public static String normalizeWhiteSpace(String input) {
         String retVal = StringUtils.trimToEmpty(input);
         while (!StringUtils.equals(retVal, StringUtils.replace(retVal, "  ", " "))) {
             retVal = StringUtils.replace(retVal, "  ", " ");
         }
         return retVal;
     }
 
     public static List splitAndTrim(String str, String separatorChars) {
         List tempList = new ArrayList();
         if (StringUtils.isNotBlank(StringUtils.trimToNull(str))) {
             String[] arr = { str };
             if (StringUtils.isNotBlank(separatorChars)) {
                 arr = StringUtils.split(str, separatorChars);
             }
             tempList = arr != null && arr.length > 0 ? Arrays.asList(arr) : tempList;
         }
         List retVal = new ArrayList();
         for (Iterator it = tempList.iterator(); it.hasNext();) {
             retVal.add(StringUtils.trim((String) it.next()));
         }
         return retVal;
     }
 
     public static List getMonths() {
         List list = new ArrayList();
         list.add("01");
         list.add("02");
         list.add("03");
         list.add("04");
         list.add("05");
         list.add("06");
         list.add("07");
         list.add("08");
         list.add("09");
         list.add("10");
         list.add("11");
         list.add("12");
         return list;
     }
 
     public static List getYears(int numOfYearsBefore, int numOfYearsAfter) {
 
         Calendar cal = Calendar.getInstance();
         int yr = cal.get(Calendar.YEAR);
         List ret = new ArrayList();
 
         for (int x = yr - numOfYearsBefore; x <= yr + numOfYearsAfter; x++) {
             ret.add(String.valueOf(x));
         }
 
         return ret;
     }
 
     public static List getYears(String startingYear, String endingYear) {
 
         List ret = new ArrayList();
         if (NumberUtils.isDigits(startingYear) && NumberUtils.isDigits(endingYear)) {
             int endingYearInt = Integer.parseInt(endingYear);
             for (int x = Integer.parseInt(startingYear); x <= endingYearInt; x++) {
                 ret.add(String.valueOf(x));
             }
         }
 
         return ret;
     }
 
     public static boolean isHexadecimal(String inBdVal) {
         String bdVal = StringUtils.lowerCase(inBdVal);
         Pattern p = Pattern.compile("([-]?)0x([0-9a-f]+)");
         return !StringUtils.isBlank(bdVal) && p.matcher(bdVal).matches();
     }
 
     public static BigDecimal createBigDecimal(String inBdVal) {
         String bdVal = StringUtils.replaceEach(StringUtils.lowerCase(inBdVal), new String[] { "%" },
                 new String[] { "" });
         BigDecimal bd = new BigDecimal("0");
         if (NumberUtils.isNumber(bdVal)) {
             if (isHexadecimal(bdVal)) {
                 BigDecimal sign = StringUtils.startsWith(bdVal, "-") ? new BigDecimal(-1) : new BigDecimal(1);
                 String t = StringUtils.reverse(StringUtils.substringAfter(bdVal, "x"));
                 BigDecimal base16 = new BigDecimal("16");
                 for (int i = 0; i < t.length(); ++i) {
                     int multiplicand = t.charAt(i) > '9' ? t.charAt(i) - 'a' + 10 : t.charAt(i) - '0';
                     bd = bd.add(base16.pow(i).multiply(new BigDecimal(multiplicand)));
                 }
                 bd = bd.multiply(sign);
             }
             else if (isLong(bdVal)) {
                 // this is a long defined as xxxL or xxxl (lowercase L) [keep in mind the string has been lowercased]
                 String longStr = StringUtils.substringBefore(bdVal, "l");
                 bd = new BigDecimal(longStr);
             }
             else {
                 bd = NumberUtils.createBigDecimal(bdVal);
             }
         }
         return bd;
     }
 
     public static boolean isLong(String bdVal) {
         Pattern p = Pattern.compile("([-]?)([0-9]+)l");
         return !StringUtils.isBlank(bdVal) && p.matcher(bdVal).matches();
     }
 
     public static Map convertArraysToLists(Map formMap) {
         Map copyMap = new HashMap();
         copyMap.putAll(formMap);
         String formKey;
         for (Iterator it = copyMap.keySet().iterator(); it.hasNext();) {
             formKey = (String) it.next();
             Object x = MapUtils.getObject(copyMap, formKey);
             if (x instanceof String[]) {
                 copyMap.put(formKey, Arrays.asList((String[]) x));
             }
         }
         return copyMap;
     }
 
     public static String[] trimArray(String[] array) {
         for (int i = 0; i < array.length; i++) {
             array[i] = StringUtils.trim(array[i]);
         }
         return array;
     }
 
     public static boolean isValidEmail(String emailAddress) {
         // see http://www.regular-expressions.info/email.html
         String REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
         Pattern pattern = Pattern.compile(REGEX);
         boolean retVal = pattern.matcher(StringUtils.lowerCase(emailAddress)).matches();
         if (!retVal) {
             LOG.error("Common.isValidEmail(String emailAddress) - The following email address is malformed: "
                     + emailAddress);
         }
         return retVal;
     }
 
     public static boolean isZeroString(String inVal) {
         boolean ret = false;
         inVal = StringUtils.replaceOnce(inVal, "$", "");
         ret = StringUtils.equals(inVal, "0") ? true : ret;
         ret = StringUtils.startsWith(inVal, "0.0") ? true : ret;
         return ret;
     }
 
     public static boolean isTBDAddress(String address) {
         String ucinput = StringUtils.trim(StringUtils.upperCase(address));
         return ucinput != null
                 && (ucinput.startsWith("TB") || ucinput.startsWith("TO BE") || ucinput.startsWith("T.B.")
                         || ucinput.startsWith("TBA") || ucinput.startsWith("T B D") || ucinput.startsWith("TBD")
                         || ucinput.startsWith("UNKNOWN") || ucinput.startsWith("TDB") || ucinput.startsWith("T D B"));
     }
 
     public static List dbListToList(List<Map> dbList, String field) {
         List retVal = new ArrayList();
         for (Map row : dbList) {
             retVal.add(MapUtils.getObject(row, field));
         }
         return retVal;
     }
 
     public static boolean containsIgnoreCase(List<String> inputList, String inputItem) {
         boolean retVal = false;
         if (inputList != null) {
             for (String item : inputList) {
                 if (StringUtils.equalsIgnoreCase(item, inputItem)) {
                     retVal = true;
                     break;
                 }
             }
         }
         return retVal;
     }
 
     public static String clean(String input) {
         return StringUtils.isNotEmpty(input) ? input.replaceAll("[^\\p{ASCII}]", "") : input;
     }
 
     public static boolean blankStringInMap(String key, Map map) {
         boolean ret = true;
         if (!StringUtils.isBlank(MapUtils.getString(map, key))) {
             ret = false;
         }
         return ret;
     }
 
     public static boolean stringEqualsInMap(String key, Map map, String val) {
         boolean ret = false;
         if (StringUtils.equalsIgnoreCase(MapUtils.getString(map, key), val)) {
             ret = true;
         }
         return ret;
     }
 
     public static String formatDateToString(Object obj) {
         return formatDateToString(obj, "MM/dd/yyyy");
     }
 
     public static String formatDateToString(Object obj, String pattern) {
         String returnString = null;
         if (obj instanceof java.util.Date) {
             returnString = FastDateFormat.getInstance(pattern).format((java.util.Date) obj);
         }
         return returnString;
     }
 
     /**
      * Because {@link MapUtils#getObject(java.util.Map, Object)} does not support generics
      *
      * @param map the map to check for null safety
      * @param key in the map we want to get the value for
      * @param <S> the type of the key in the map
      * @param <T> the type of the object we are returning from the map
      * @return the value of the entry in the map
      */
     public static <S, T> T mapGet(final Map<S, T> map, final S key) {
         if (map != null) {
             return map.get(key);
         }
         return null;
     }
 
     public static boolean checkDate(String date1, String date2) {
         boolean ret = false;
         String DATEFORMAT = "MM/dd/yyyy";
         SimpleDateFormat SDF = new SimpleDateFormat(DATEFORMAT);
 
         try {
             if (StringUtils.isNotBlank(date1) && StringUtils.isNotBlank(date2)) {
                 if (SDF.parse(date2).before(SDF.parse(date1)) || SDF.parse(date2).equals(SDF.parse(date1))) {
                     ret = true;
                 }
             }
             else if (date1 == null) {
                 ret = true;
             }
         }
         catch (ParseException e) {
             LOG.error(ExceptionUtils.getStackTrace(e));
         }
         return ret;
     }
 
     public static List buildOptions(String... optionVals) {
         List retVal = new ArrayList();
         Map option;
         for (int i = 0; i < optionVals.length; i += 2) {
             option = new CaseInsensitiveMap();
             option.put("value", optionVals[i]);
             option.put("label", optionVals[i + 1]);
             retVal.add(option);
         }
         return retVal;
     }
 
     public static String concatDescriptions(String[] descriptions) {
         StringBuffer ret = new StringBuffer();
         if (descriptions != null) {
             for (int i = 0; i < descriptions.length; i++) {
                 if (StringUtils.isNotBlank(descriptions[i])) {
                     ret.append(descriptions[i]).append(", ");
                 }
             }
         }
 
         return ret.toString();
     }
 
     public static String getCallingMethod(String... methodsToIgnore) {
         boolean doNext = false;
         List l = Arrays.asList(methodsToIgnore);
         for (StackTraceElement s : Thread.currentThread().getStackTrace()) {
             if (doNext && !l.contains(s.getMethodName())) {
                 return s.getMethodName();
             }
             doNext = doNext || s.getMethodName().equals("getCallingMethod");
         }
         return "";
     }
 
     private static String[] countryAbbreviations = { "US", "AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG",
             "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH", "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BA",
             "BW", "BV", "BR", "IO", "BN", "BG", "BF", "BI", "KH", "CM", "CA", "CV", "KY", "CF", "TD", "CL", "CN", "CX",
             "CC", "CO", "KM", "CG", "CD", "CK", "CR", "CI", "HR", "CU", "CY", "CZ", "DK", "DJ", "DM", "DO", "EC", "EG",
             "SV", "GQ", "ER", "EE", "ET", "FK", "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH",
             "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GG", "GN", "GW", "GY", "HT", "HM", "VA", "HN", "HK", "HU", "IS",
             "IN", "ID", "IR", "IQ", "IE", "IM", "IL", "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW",
             "KG", "LA", "LV", "LB", "LS", "LR", "LY", "LI", "LT", "LU", "MO", "MK", "MG", "MW", "MY", "MV", "ML", "MT",
             "MH", "MQ", "MR", "MU", "YT", "MX", "FM", "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP",
             "NL", "AN", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MP", "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY",
             "PE", "PH", "PN", "PL", "PT", "PR", "QA", "RE", "RO", "RU", "RW", "BL", "SH", "KN", "LC", "MF", "PM", "VC",
             "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG", "SK", "SI", "SB", "SO", "ZA", "GS", "ES", "LK", "SD",
             "SR", "SJ", "SZ", "SE", "CH", "SY", "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR", "TM",
             "TC", "TV", "UG", "UA", "AE", "GB", "UM", "UY", "UZ", "VU", "VE", "VN", "VG", "VI", "WF", "EH", "YE", "ZM",
             "ZW" };
 
     private static String[] countryNames = { "UNITED STATES", "AFGHANISTAN", "ALAND ISLANDS", "ALBANIA", "ALGERIA",
             "AMERICAN SAMOA", "ANDORRA", "ANGOLA", "ANGUILLA", "ANTARCTICA", "ANTIGUA AND BARBUDA", "ARGENTINA",
             "ARMENIA", "ARUBA", "AUSTRALIA", "AUSTRIA", "AZERBAIJAN", "BAHAMAS", "BAHRAIN", "BANGLADESH", "BARBADOS",
             "BELARUS", "BELGIUM", "BELIZE", "BENIN", "BERMUDA", "BHUTAN", "BOLIVIA, PLURINATIONAL STATE OF",
             "BOSNIA AND HERZEGOVINA", "BOTSWANA", "BOUVET ISLAND", "BRAZIL", "BRITISH INDIAN OCEAN TERRITORY",
             "BRUNEI DARUSSALAM", "BULGARIA", "BURKINA FASO", "BURUNDI", "CAMBODIA", "CAMEROON", "CANADA", "CAPE VERDE",
             "CAYMAN ISLANDS", "CENTRAL AFRICAN REPUBLIC", "CHAD", "CHILE", "CHINA", "CHRISTMAS ISLAND",
             "COCOS (KEELING) ISLANDS", "COLOMBIA", "COMOROS", "CONGO", "CONGO, THE DEMOCRATIC REPUBLIC OF THE",
             "COOK ISLANDS", "COSTA RICA", "COTE D'IVOIRE", "CROATIA", "CUBA", "CYPRUS", "CZECH REPUBLIC", "DENMARK",
             "DJIBOUTI", "DOMINICA", "DOMINICAN REPUBLIC", "ECUADOR", "EGYPT", "EL SALVADOR", "EQUATORIAL GUINEA",
             "ERITREA", "ESTONIA", "ETHIOPIA", "FALKLAND ISLANDS (MALVINAS)", "FAROE ISLANDS", "FIJI", "FINLAND",
             "FRANCE", "FRENCH GUIANA", "FRENCH POLYNESIA", "FRENCH SOUTHERN TERRITORIES", "GABON", "GAMBIA", "GEORGIA",
             "GERMANY", "GHANA", "GIBRALTAR", "GREECE", "GREENLAND", "GRENADA", "GUADELOUPE", "GUAM", "GUATEMALA",
             "GUERNSEY", "GUINEA", "GUINEA-BISSAU", "GUYANA", "HAITI", "HEARD ISLAND AND MCDONALD ISLANDS",
             "HOLY SEE (VATICAN CITY STATE)", "HONDURAS", "HONG KONG", "HUNGARY", "ICELAND", "INDIA", "INDONESIA",
             "IRAN, ISLAMIC REPUBLIC OF", "IRAQ", "IRELAND", "ISLE OF MAN", "ISRAEL", "ITALY", "JAMAICA", "JAPAN",
             "JERSEY", "JORDAN", "KAZAKHSTAN", "KENYA", "KIRIBATI", "KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF",
             "KOREA, REPUBLIC OF", "KUWAIT", "KYRGYZSTAN", "LAO PEOPLE'S DEMOCRATIC REPUBLIC", "LATVIA", "LEBANON",
             "LESOTHO", "LIBERIA", "LIBYAN ARAB JAMAHIRIYA", "LIECHTENSTEIN", "LITHUANIA", "LUXEMBOURG", "MACAO",
             "MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF", "MADAGASCAR", "MALAWI", "MALAYSIA", "MALDIVES", "MALI",
             "MALTA", "MARSHALL ISLANDS", "MARTINIQUE", "MAURITANIA", "MAURITIUS", "MAYOTTE", "MEXICO",
             "MICRONESIA, FEDERATED STATES OF", "MOLDOVA, REPUBLIC OF", "MONACO", "MONGOLIA", "MONTENEGRO",
             "MONTSERRAT", "MOROCCO", "MOZAMBIQUE", "MYANMAR", "NAMIBIA", "NAURU", "NEPAL", "NETHERLANDS",
             "NETHERLANDS ANTILLES", "NEW CALEDONIA", "NEW ZEALAND", "NICARAGUA", "NIGER", "NIGERIA", "NIUE",
             "NORFOLK ISLAND", "NORTHERN MARIANA ISLANDS", "NORWAY", "OMAN", "PAKISTAN", "PALAU",
             "PALESTINIAN TERRITORY, OCCUPIED", "PANAMA", "PAPUA NEW GUINEA", "PARAGUAY", "PERU", "PHILIPPINES",
             "PITCAIRN", "POLAND", "PORTUGAL", "PUERTO RICO", "QATAR", "REUNION", "ROMANIA", "RUSSIAN FEDERATION",
             "RWANDA", "SAINT BARTHELEMY", "SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA", "SAINT KITTS AND NEVIS",
             "SAINT LUCIA", "SAINT MARTIN", "SAINT PIERRE AND MIQUELON", "SAINT VINCENT AND THE GRENADINES", "SAMOA",
             "SAN MARINO", "SAO TOME AND PRINCIPE", "SAUDI ARABIA", "SENEGAL", "SERBIA", "SEYCHELLES", "SIERRA LEONE",
             "SINGAPORE", "SLOVAKIA", "SLOVENIA", "SOLOMON ISLANDS", "SOMALIA", "SOUTH AFRICA",
             "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS", "SPAIN", "SRI LANKA", "SUDAN", "SURINAME",
             "SVALBARD AND JAN MAYEN", "SWAZILAND", "SWEDEN", "SWITZERLAND", "SYRIAN ARAB REPUBLIC",
             "TAIWAN, PROVINCE OF CHINA", "TAJIKISTAN", "TANZANIA, UNITED REPUBLIC OF", "THAILAND", "TIMOR-LESTE",
             "TOGO", "TOKELAU", "TONGA", "TRINIDAD AND TOBAGO", "TUNISIA", "TURKEY", "TURKMENISTAN",
             "TURKS AND CAICOS ISLANDS", "TUVALU", "UGANDA", "UKRAINE", "UNITED ARAB EMIRATES", "UNITED KINGDOM",
             "UNITED STATES MINOR OUTLYING ISLANDS", "URUGUAY", "UZBEKISTAN", "VANUATU",
             //"VATICAN CITY STATE", 
             "VENEZUELA, BOLIVARIAN REPUBLIC OF", "VIET NAM", "VIRGIN ISLANDS, BRITISH", "VIRGIN ISLANDS, U.S.",
             "WALLIS AND FUTUNA", "WESTERN SAHARA", "YEMEN", "ZAMBIA", "ZIMBABWE" };
 
     public static List getFullNameCountryList() {
         Map row;
         List countryList = new ArrayList();
         for (int i = 0; i < countryNames.length; ++i) {
             row = new CaseInsensitiveMap();
             row.put("name", countryNames[i]);
             row.put("abbrev", countryAbbreviations[i]);
             countryList.add(row);
         }
         return countryList;
     }
 
 }
