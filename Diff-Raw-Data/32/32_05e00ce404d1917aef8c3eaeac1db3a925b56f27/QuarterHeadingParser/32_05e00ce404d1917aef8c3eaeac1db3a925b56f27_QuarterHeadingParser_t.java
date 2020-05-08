 import java.util.regex.*;
 import java.util.GregorianCalendar;
 import java.util.Date;
 
 /**
  * Parses OpenTTD quarter year headings output.
  * This is supported in Eladith's openttd-output patch.
  * 
  * Appends dates to SQL, too.
  */
 public class QuarterHeadingParser implements LineParser {
 
     private static final String sqlStart = "INSERT gamedate (ingame) values ";
 
     private static final String matchingRegEx = "^linkki: Neljannesvuositilastot (\\d+) / (\\d+)$";
     private static final int matchingGroups = 2;
     private static final Pattern matchingPattern;
 
     public Date day;
 
     static {
 	matchingPattern = Pattern.compile(matchingRegEx);
     }
     
     /**
      * Tries to parse a line. Returns false if it's not possible to parse.
      * In the case of true this method clears its previous state ad it is
      * guaranteed to have "fresh" values. In the case of false the object's
      * content is undefined.
      *
      * Throws an exception if date format is not supported. This helps to 
      * spot bugs but may turn parser unstable. Fix if this is problematic.
      */
     public boolean match(String line) throws Exception {
 
 	Matcher matcher = matchingPattern.matcher(line);
 	if (!matcher.matches() || matchingGroups != matcher.groupCount()) {
 	    return false; // this is not right pattern
 	}
 
 	int year = Integer.parseInt(matcher.group(1));
	int month = Integer.parseInt(matcher.group(2)); // starts from 0
 	this.day = (new GregorianCalendar(year, month, 1)).getTime();
 	
 	return true; // Success.
     }
 
     public void appendSQL(SQLBuilder sql) {
 	sql.appendRaw(sqlStart);
 	sql.appendRaw('(');
 	sql.appendDate(day);
 	sql.appendRaw(");");
     }
 
     public void clear() {
 	this.day = null;
     }
 
     public String parserName() {
 	return "Quarter year heading";
     }
 
     public String engineerDebug() {
 	return "Day: "+ this.day;
     }    
 }
