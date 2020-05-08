 package net.anei.cadpage.parsers.general;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.ManagePreferences;
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 public class GeneralParser extends SmartAddressParser {
   
   private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d\\d/\\d\\d/\\d\\d(\\d\\d)?\\b");
   private static final Pattern TIME_PATTERN = Pattern.compile("\\b\\d\\d:\\d\\d\\b");
   private static final Pattern DELIM_PATTERN = Pattern.compile(";|,|\\*|\\n|\\||\\b[A-Z][A-Za-z0-9-#]*:|\\bC/S:|\\b[A-Z][A-Za-z]*#");
   private static final Pattern CALL_ID_PATTERN = Pattern.compile("\\d[\\d-]+\\d");
   private static final Pattern UNIT_PATTERN = Pattern.compile("([A-Z]{1,4}[0-9]{1,4}\\s+)+");
   
   // Field types that we can identify 
   private enum FieldType {SKIP, CALL, PLACE, ADDRESS, CITY, APT, CROSS, BOX, UNIT, MAP, ID, PHONE, SUPP, CODE, SRC, NAME};
   
   // Map of keywords to field types
   private Map<String, FieldType> keywordMap = new HashMap<String, FieldType>();
   
   public GeneralParser() {
     super("","");
     
     // Initialize keyword map
     loadMap(FieldType.SKIP, "TIME", "TOA", "DATE", "TM", "TOC");
     loadMap(FieldType.CALL, "CALL", "TYPE", "TYP", "CT", "NAT", "NATURE", "INC");
     loadMap(FieldType.PLACE, "NAME", "COMMON", "CN", "O");
     loadMap(FieldType.ADDRESS, "ADDRESS", "LOC", "ADDR", "AD", "ADR", "ADD", "LOCATION");
     loadMap(FieldType.CITY, "CITY", "CTY", "VENUE", "VEN");
     loadMap(FieldType.APT, "APT", "BLDG", "BLD");
     loadMap(FieldType.CROSS, "CROSS", "X", "XS", "XST", "XST2", "X-ST", "C/S", "CS", "BTWN");
     loadMap(FieldType.BOX, "BOX", "BX", "ADC");
     loadMap(FieldType.UNIT, "UNIT", "UNITS", "RESPONSE", "DUE", "UNTS", "UNT");
     loadMap(FieldType.MAP, "MAP", "GRID", "GRIDS", "QUADRANT", "QUAD");
     loadMap(FieldType.ID, "ID", "CFS", "RUN", "EVT#", "INC#");
     loadMap(FieldType.PHONE, "PHONE", "PH");
     loadMap(FieldType.SUPP, "INFO", "CMT", "CMT1", "CMT2", "SCRIPT", "COMMENTS", "RMK");
     loadMap(FieldType.SRC, "STA", "ST", "DISP", "DIS", "DIST", "DISTRICT", "AGENCY");
     loadMap(FieldType.NAME, "CALLER");
   }
   
   private void loadMap(FieldType type, String ... keywords) {
     for (String keyword : keywords) {
       keywordMap.put(keyword, type);
     }
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Accept anything, but only if there is a valid sender filter
    if (! ManagePreferences.overrideFilter()) return false;
     if (ManagePreferences.filter().length() <= 1) return false;
     
     // Starting with CAD: confuses things
     if (body.startsWith("CAD:")) body = body.substring(4).trim();
     
     // Strip out any date and time fields
     body = DATE_PATTERN.matcher(body).replaceAll("");
     body = TIME_PATTERN.matcher(body).replaceAll("");
 
     // Parse text into different fields separated by delimiters
     // that match DELIM_PATTERN
     Matcher match = DELIM_PATTERN.matcher(body);
     String nextDelim = "";
     int pt = 0;
 
     String last = null;
 
     boolean keywordDesc = false;
     boolean foundAddr = false;
     Result bestRes = null;
     String bestAddr = null;
     String secondAddr = null;
 
     while (nextDelim != null) {
       
       // Parse the next field
       String delim = nextDelim;
       String fld;
       if (match.find(pt)) {
         nextDelim = body.substring(match.start(), match.end());
         fld = body.substring(pt, match.start());
         pt = match.end();
       } else {
         nextDelim = null;
         fld = body.substring(pt);
       }
       fld = fld.trim();
       
       // fld is the data field being processed
       // delim is the delimiter that started this field
       
       // If zero length field, nothing to worry about
       if (fld.length() == 0) continue;
       
       // Check delimiter to see if this is something we recognize?
       FieldType type = null;
       if (delim.endsWith(":")) {
         type = keywordMap.get(delim.substring(0, delim.length()-1).toUpperCase());
       } else if (delim.endsWith("#")) {
         type = FieldType.ID;
       }
       
       // If we have found a recognizable keyword, assign this field to that keyword value
       if (type != null) {
         
         switch (type) {
         
         case SKIP:
           break;
           
         case CALL:
           // Call description, ignore if less than 3 characters
           if (fld.length() <= 2) break;
           keywordDesc = true;
           
           // If we already have a call desc, append this to it
           if (data.strCall.length() > 0) {
             data.strCall += " / ";
             data.strCall += fld;
           }
           
           // Otherwise run it through the smart parser just in case the
           // call desc is followed by an address
           else {
             parseAddress(StartType.START_CALL, fld, data);
             if (data.strSupp.length() == 0) data.strSupp = getLeft();
           }
           break;
           
         case PLACE:
           if (data.strPlace.length() == 0) data.strPlace = fld;
           break;
           
         case ADDRESS:
           // We  might have  multiple address fields
           // Keep track of which one looks like the best address
           // Also keep the first second place address in case we want to
           // use it as a call description
           foundAddr = true;
           StartType st = (data.strCall.length() == 0 ? StartType.START_CALL :
                           data.strPlace.length() == 0 ? StartType.START_PLACE : StartType.START_SKIP);
           Result res = parseAddress(st, fld);
           if (bestRes == null || res.getStatus() > bestRes.getStatus()) {
             if (secondAddr == null) secondAddr = bestAddr;
             bestAddr = fld;
             bestRes = res;
           } else {
             if (secondAddr == null) secondAddr = fld;
           }
           
           // If we have some leading call info, use it.  If not, 
           // treat the last unidentified field as the call
           if (data.strCall.length() == 0 && last != null) data.strCall = last;
           break;
           
         case CITY:
           data.strCity = fld;
           break;
           
         case APT:
           if (data.strApt.length() > 0) data.strApt += "-";
           data.strApt += fld;
           break;
           
         case CROSS:
           if (data.strCross.length() > 0) data.strCross += " & ";
           data.strCross += fld;
           break;
           
         case BOX:
           data.strBox = fld;
           break;
           
         case UNIT:
           data.strUnit = fld;
           break;
           
         case MAP:
           data.strMap = fld;
           break;
           
         case ID:
           data.strCallId = fld;
           break;
           
         case PHONE:
           data.strPhone = fld;
           break;
           
         case SUPP:
           if (data.strSupp.length() > 0) data.strSupp += " / ";
           data.strSupp += fld;
           break;
           
         case CODE:
           data.strCode = fld;
           break;
           
         case SRC:
           data.strSource = fld;
           break;
           
         case NAME:
           data.strName = fld;
           break;
           
         }
       }
 
       // Otherwise we have to try to figure out fields by position
       else {
         
         // If we haven't found an address yet, see if this is it
         if (!foundAddr) {
           StartType st = (data.strCall.length() == 0 ? StartType.START_CALL :
                           data.strPlace.length() == 0 ? StartType.START_PLACE : StartType.START_SKIP);
           Result res = parseAddress(st, fld);
           if (res.getStatus() > 0) {
             // Bingo!  Anything past the address goes into info
             foundAddr = true;
             res.getData(data);
             
             // If we have some leading call info, use it.  If not, 
             // treat the last unidentified field as the call
             if (data.strCall.length() == 0 && last != null) data.strCall = last;
             
             // If we got any call info from anywhere, extra stuff goes into info 
             // otherwise it goes into the call description
             if (data.strCall.length() == 0) {
               data.strCall = getLeft();
             } else {
               data.strSupp = getLeft();
             }
             continue;
           }
         }
         
         // Does this look like a call ID?
         if (data.strCallId.length() == 0 && CALL_ID_PATTERN.matcher(fld).matches()) {
           data.strCallId = fld;
           continue;
         }
         
         // Nope, lets see if it looks like a unit list
         if (data.strUnit.length() == 0 && UNIT_PATTERN.matcher(fld + " ").matches()) {
           data.strUnit = fld;
           continue;
         }
         
         // None of the above
         // If we haven't found an address yet, save this field for use as a possible
         // call field when we find the address. 
         if (! foundAddr) {
           last = fld;
           continue;
         }
         
         // If field has an &, assume it is a set of cross streets
         if (data.strCross.length() == 0 && fld.contains("&")) {
           data.strCross = fld;
           continue;
         }
         
         // If field parse out as an complete address
         // Assume it is a cross street
         if (checkAddress(fld) > 0) {
           if (data.strCross.length() > 0) data.strCross += " & ";
           data.strCross += fld;
           continue;
         }
         
         // If we have found an address but have no call description, assume this is it
         if (data.strCall.length() == 0) {
           data.strCall = fld;
           continue;
         }
         
         // Otherwise bundle this into supplemental info
         StringBuilder sb = new StringBuilder(data.strSupp);
         if (sb.length() > 0 && delim.length()>0 && ",;\n".indexOf(delim.charAt(0))<0) sb.append(' ');
         if (sb.length() > 0 || 
             delim.length() > 0 && Character.isUpperCase(delim.charAt(0))) sb.append(delim);
         if (sb.length() > 0) sb.append(' ');
         sb.append(fld);
         data.strSupp = sb.toString();
       }
     }
     
     // next resolve any multiple address keyword fields
     if (bestRes != null) {
       String saveCall = data.strCall;
       bestRes.getData(data);
       if (data.strCall.length() == 0 || keywordDesc) data.strCall = saveCall;
       String supp = bestRes.getLeft();
       if (supp.length() > 0) {
         if (data.strSupp.length() > 0) {
           supp = supp + " / " + data.strSupp;
         }
         data.strSupp = supp;
       }
 
       // If there were multiple address keywords, the second best one
       // becomes the place.  Any existing place is demoted to a name
       if (secondAddr != null) {
         if (data.strPlace.length() > 0) data.strName = data.strPlace;
         data.strPlace =  secondAddr;
       }
     }
 
     // Return success if we found an address
     return data.strAddress.length() > 0;
   }
 }
