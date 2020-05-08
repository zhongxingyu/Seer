 package net.anei.cadpage.parsers;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Parser class that convert the generic Cadpage message format to and from
  * a parsed message information object
  */
 public class CadpageParser  extends FieldProgramParser{
   
   private Map<String,Field> fieldMap = new HashMap<String,Field>();
   
   private static final Pattern KEYWORD = Pattern.compile("\\b([\\w]+):");
   
   public CadpageParser() {
     // Pass empty strings to subclass constructor, we never really try to run a 
     // field program or use the default city/state values
     super("", "", "");
     initMap();
   }
   
   @Override
   public String getLocName() {
     return "Cadpage";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     int fieldCnt = 0;
     int lastPt = 0;
     Field lastField = null;
     
     // Look through message body looking for keyword constructs
     Matcher match = KEYWORD.matcher(body);
     while (match.find()) {
       
       // For each one, extract the keyword name and look it up in the field map
       // If we don't find it there, assume this was a extra colon in the data
       // field and go on to the next one
       String key = match.group(1);
       Field field = fieldMap.get(key);
       if (field != null) {
         
         // Increment the field count,
         fieldCnt++;
         
         // This keyword marks the end of the previous data field.
         // Extract that field value and pass it to the previous
         // field processor
         //
         // If the last field value is not empty, but the last field processor
         // is still null, that means the message body did not start with a
         // recognizable keyword, which is grounds to reject the message
         String value = body.substring(lastPt, match.start()).trim();
         if (value.length() > 0) {
           if (lastField == null) return false;
           lastField.parse(value, data);
         }
         
         // Save last field position and last field processor
         lastField = field;
         lastPt = match.end();
       }
     }
     
     // End of loop.  If we didn't find any recognizable keywords, reject message
     if (lastField == null) return false;
     
     // pass the last field to the last field processor
     lastField.parse(body.substring(lastPt).trim(), data);
     
     // We have to have found at least two fields for this to be considered valid
     return fieldCnt >= 2;
   }
   
   private void initMap() {
     setMap("PRI");
     setMap("DATE");
     setMap("TIME");
     setMap("CALL",        "title");
     setMap("PLACE", "PL");
     setMap("ADDR",        "address");
     setMap("CITY",        "city");
     setMap("ST");
     setMap("APT");
     setMap("X");
     setMap("BOX");
     setMap("MAP",         "map_code");
     setMap("CH");
     setMap("UNIT");
    setMap("INFO");
     setMap("NAME");
     setMap("PHONE", "PH");
     setMap("CODE");
     setMap("GPS");
     setMap("ID",           "cad_code");
     setMap("SRC");
     setMap("DCITY");
     setMap("DST");
     setMap("MADDR");
    setMap(                "response_url");
   }
 
   /**
    * Set up key -> Field process map
    * @param keys list of key used to access this field.  The first key will
    * be used to look up the field that all of them will refer to.
    */
   private void setMap(String ... keys) {
     Field field = getField(keys[0]);
     for (String key : keys) fieldMap.put(key, field);
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("DCITY")) return new DefCityField();
     if (name.equals("DST")) return new DefStateField();
     if (name.equals("MADDR")) return new SkipField();
    if (name.equals("response_url")) return new SkipField();
     return super.getField(name);
   }
   
   // We need an address field that just saves the address.  The default
   // field process handles all kinds of cool things
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       data.strAddress = field;
     }
   }
 
   // And something to save the default city and state
   private class DefCityField extends Field {
     @Override 
     public void parse(String field, Data data) {
       data.defCity = field;
     }
   }
   
   private class DefStateField extends Field {
     @Override 
     public void parse(String field, Data data) {
       data.defState = field;
     }
   }
 
   /**
    * Convert parsed information object into a text string that the Cadpage parser
    * This is only invokved when the Cadpage parsing engine is running on a messaging server
    * can turn back into a parsed information object
    * @param info parsed information object
    * @param delim field separator to place between data fields.  Should be blanks or a newline 
    * @param inclMapAddr true if base map address should be included.  This should only be used
    * when the target is a IPhone or IPad.  Android phones can take care of this themselves
    * and dumb phones users would only be confused.
    * @return
    */
   public static String formatInfo(MsgInfo info, String delim, boolean inclMapAddr) {
     StringBuilder sb = new StringBuilder();
     append(sb, "PRI", info.getPriority(), delim);
     append(sb, "DATE", info.getDate(), delim);
     append(sb, "TIME", info.getTime(), delim);
     append(sb, "CALL", info.getCall(), delim);
     append(sb, "PL", info.getPlace(), delim);
     append(sb, "ADDR", info.getAddress(), delim);
     append(sb, "CITY", info.getCity(), delim);
     append(sb, "ST", info.getState(), delim);
     append(sb, "APT", info.getApt(), delim);
     append(sb, "X", info.getCross(), delim);
     append(sb, "BOX", info.getBox(), delim);
     append(sb, "MAP", info.getMap(), delim);
     append(sb, "CH", info.getChannel(), delim);
     append(sb, "UNIT", info.getUnit(), delim);
     append(sb, "INFO", info.getSupp(), delim);
     append(sb, "NAME", info.getName(), delim);
     append(sb, "PH", info.getPhone(), delim);
     append(sb, "CODE", info.getCode(), delim);
     append(sb, "GPS", info.getGPSLoc(), delim);
     append(sb, "ID", info.getCallId(), delim);
     
     append(sb, "SRC", info.getSource(), delim);
     if (info.getCity().length() == 0) append(sb, "DCITY", info.getDefCity(), delim);
     if (info.getState().length() == 0) append(sb, "DST", info.getDefState(), delim);
     if (inclMapAddr) {
       append(sb, "MADDR", info.getBaseMapAddress(), delim);
     }
     return sb.toString();
   }
   
   private static void append(StringBuilder sb, String key, String value, String delim) {
     if (value != null && value.length() > 0) {
       if (sb.length() > 0) sb.append(delim);
       sb.append(key);
       sb.append(": ");
       sb.append(value);
     }
   }
   
   
 
 }
