 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /**
  * This class parses messages from OSSI CAD software
  *
  * Format always starts with "CAD:" and generally consists of a series of
  * semicolon delimited fields which will be processed according to a program
  * passed from the subclass.
  * 
  * There are, of course, some exception
  * The text may contain a leading numeric call ID and a colon before the CAD:
  * marker.  Subclasses will indicate this by inserting an ID: term as the
  * first field term in the program
  * 
  * The text may contain some text in square brackets.  This comes in two flavors
  * 
  * [mm/dd/yy hh:mm:ss xxxxxx] appears to be an subsequent update marker, it will
  * be treated as field delimiter
  * 
  * [Medical Priority Info] or [Fire Priority Info]
  * The field that follows this may one of the two following formats
  * RESPONSE: xxxxxx RESPONDER SCRIPT: xxxxxx
  * PROBLEM: xxxxx # PATS: n AGE:  SEX:  xxxxxxx
  * 
  * In this case we will skip all text up to and including the keyword
  * "RESPONDER SCRIPT: or "PROBLEM:"
  * 
  * In addition, the last field in the string may be a date/time indicator
  * or a truncated piece of a date/time indicator.  If this is the case
  * drop it from further consideration
  **/
 
 public class DispatchOSSIParser extends FieldProgramParser {
   
   private boolean leadID = false;
   
   // Pattern searching for a leading square bracket or semicolon
   private Pattern delimPattern = Pattern.compile("\\[|;");
   
   // Pattern searching for "PROBLEM: or "RESPONDER SCRIPT:"
   private static final Pattern KEYWORD = Pattern.compile("\\b(PROBLEM:|RESPONDER SCRIPT:)");
   
   // Pattern marking a trailing token that may need to be removed
   private static final Pattern TAIL_PAT = Pattern.compile("CLDR?[0-9]");
 
   
   public DispatchOSSIParser(String defCity, String defState, String program) {
     super(defCity, defState, "SKIP");
     setup(program);
   }
   
   public DispatchOSSIParser(Properties cityCodes, String defCity, String defState, String program) {
     super(cityCodes, defCity, defState, "SKIP");
     setup(program);
   }
   
   public DispatchOSSIParser(String[] cityList, String defCity, String defState, String program) {
     super(cityList, defCity, defState, "SKIP");
     setup(program);
   }
 
   private void setup(String program) {
     if (program.startsWith("ID:")) {
       leadID = true;
       program = program.substring(3).trim();
     }
     setProgram(program);
   }
   
   protected void setDelimiter(char delim) {
     delimPattern = Pattern.compile("\\[|" + delim);
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Strip off leading /
    if (body.startsWith("/")) body = body.substring(1).trim();
     
     // If format has a leading ID, strip that off
     if (leadID) {
       int pt = body.indexOf(':');
       if (pt < 0) return false;
       data.strCallId = body.substring(0,pt).trim();
       if (!NUMERIC.matcher(data.strCallId).matches()) return false;
       body = body.substring(pt+1).trim();
     }
     
     // Body must start with 'CAD:'
     if (!body.startsWith("CAD:")) return false;
     
     // Break down string into generally semicolon delimited fields
     // with some complications involving text in square brackets
     
     List<String> fields = new ArrayList<String>();
     Matcher match = delimPattern.matcher(body);
     int st = 4;
 
     boolean priInfo = false;
     while (st < body.length()) {
       
       // search for the next delimiter (either ; or [
       char delim;
       int pt;
       if (match.find(st)) {
         pt = match.start();
         delim = body.charAt(pt);
       } else {
         pt = body.length();
         delim = 0;
       }
 
       // The next field consists of everything from the starting point
       // up to the location of the delimiter
       // If the delimiter in front of this term was a square bracket term
       // Search for and delete any text up to including the keywords
       // "PROBLEM:" or "RESPONDER SCRIPT:"
       
       String field = body.substring(st,pt).trim();
       if (priInfo) {
         Matcher match2 = KEYWORD.matcher(field);
         if (!match2.find()) field = null;
         else field = field.substring(match2.end()).trim();
       }
       if (field != null) fields.add(field);
       
       // Find the start of the next field
       // if the delimiter was an open square bracket, the start of the
       // next field will follow the closing square bracket
       st = pt+1;
       priInfo = false;
       if (delim == '[') {
         pt = body.indexOf(']', st);
         if (pt < 0) pt = body.length();
         priInfo = body.substring(st, pt).contains("Priority Info");
         st = pt + 1;
       }
     }
 
     int ndx = fields.size()-1;
     if (ndx < 0) return false;
 
     // If the trailing field is present, start by removing it
     if (TAIL_PAT.matcher(fields.get(ndx)).matches()) {
       fields.remove(ndx--);
       if (ndx < 0) return false;
     }
     
     // Almost there.  Check to see if the last term looks like a date/time stamp
     // or the truncated remains of a date/time stamp.  If it does, remove it
     String field = fields.get(ndx);
     if (field.length()>0 && Character.isDigit(field.charAt(0))) {
       field = field.replaceAll("\\d", "N");
       if ("NN/NN/NNNN NN:NN:NN".startsWith(field)) fields.remove(ndx);
     }
 
     // We have a nice clean array of data fields, pass it to the programmer
     // field processor to parse
     return parseFields(fields.toArray(new String[fields.size()]), data);
   }
 }
