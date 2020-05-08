 package org.Zeitline;
 
 import org.Zeitline.Event.AbstractTimeEvent;
 import org.Zeitline.Timestamp.Timestamp;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Query {
 
     protected String actualString = null;
     protected String stringQuery = null;
     protected String regexString = null;
     protected Timestamp intervalStart = null;
     protected Timestamp intervalEnd = null;
     protected Pattern pattern = null;
     protected Source sourceQuery = null;
 
     public Query(String key, String regex) {
         this(null, null, key, regex);
     }
 
     public Query(Timestamp start, Timestamp end, String key, String regex) {
 
         intervalStart = start;
         intervalEnd = end;
 
         if (IsNullOrEmpty(key))
            stringQuery = null;
        else {
             actualString = key;
            stringQuery = "(?s).*" + key.toLowerCase() + ".*"; // The (?s) sets the DOTALL mode (otherwise \n isn't matched)
        }
 
         if (IsNullOrEmpty(regex))
             regexString = null;
        else {
             regexString = regex;
             pattern = Pattern.compile(regexString);
        }
     }
 
     private static boolean IsNullOrEmpty(String s) {
         return (s == null || s.matches("\\A\\s*\\Z"));
     } 
     
     public Timestamp getIntervalStart() {
         return intervalStart;
     }
 
     public Timestamp getIntervalEnd() {
         return intervalEnd;
     }
 
     public String getStringText() {
         return actualString;
     }
 
     public String getRegexText() {
         return regexString;
     }
 
     public boolean matches(AbstractTimeEvent t) {
 
         // If it's not within the date then ignore it
         if ((intervalStart != null) &&
                 intervalStart.after(t.getStartTime()))
             return false;
 
         if ((intervalEnd != null) &&
                 intervalEnd.before(t.getStartTime()))
             return false;
 
         // If it doesn't match the keyword then ignore it
         if (stringQuery != null) {
             if (!stringQuery.equals("")) {
                 if (t.getName().toLowerCase().matches(stringQuery) ||
                     t.getDescription().toLowerCase().matches(stringQuery))
                     return true;
             }
         }
 
         // If it doesn't match the regular expression then ignore it
         if (regexString != null) {
             if (!regexString.equals(""))
             {
                 Matcher nameMatch = pattern.matcher(t.getName());
                 Matcher descMatch = pattern.matcher(t.getDescription());
 
                 if (nameMatch.matches() || descMatch.matches()) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public String toString() {
 
         StringBuilder ret = new StringBuilder(" ");
 
         if (actualString != null)
             ret.append("Keyword: \"" + actualString + "\" ");
 
         if (regexString != null)
             ret.append("Regex: \"" + regexString + "\" ");
         
         if (intervalStart != null)
             ret.append("From: " + intervalStart + " ");
 
         if (intervalEnd != null)
             ret.append("Until: " + intervalEnd);
 
         return ret.toString();
 
     }
 
 }
