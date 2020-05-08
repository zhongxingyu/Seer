 package net.anei.cadpage.parsers.NC;
 
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 
 
 public class NCJohnstonCountyParser extends DispatchOSSIParser {
   
   private static final Pattern ENROUTE_PTN = Pattern.compile("CAD:([A-Z0-9]+),Enroute,.*");
   
   public NCJohnstonCountyParser() {
     super(CITY_CODES, "JOHNSTON COUNTY", "NC",
            "( CH SRC CODE? | SRC/Z CODE | SRC? ) CALL CITY? ADDR! CITY? APT? X+? ( PLACE NAME DATETIME | NAME? DATETIME ) UNIT");
   }
 
   @Override
   public String getFilter() {
     return "CAD@johnstonnc.com,93001";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     // Enroute messages parse as general alerts with a unit field
     Matcher match = ENROUTE_PTN.matcher(body);
     if (match.matches()) {
       data.strCall = "GENERAL ALERT";
       data.strUnit = match.group(1);
       data.strPlace = body;
       return true;
     }
     
     return super.parseMsg(body, data);
   }
   
   @Override
   public String getProgram() {
     return super.getProgram() + " UNIT PLACE";
   }
 
   // Source code validates that is is not more than 5 characters long
   private class MySourceField extends SourceField {
 
     @Override
     public boolean canFail() {
       return true;
     }
 
     @Override
     public boolean checkParse(String field, Data data) {
       if (field.length() > 5) return false;
       parse(field, data);
       return true;
     }
   }
   
   // Cross street won't pass normal address validation
   // We will take anything that doesn't look like a name with a comma
   private static final Pattern NS_CROSS_PTN = Pattern.compile("(\\d*) *\\(S\\) *(.*?) *\\(N\\)|[\\.\\d]+|DIST:.*");
   private class MyCrossField extends CrossField {
     @Override
     public boolean checkParse(String field, Data data) {
       
       // If this is the first cross/place field, rules are different
       if (data.strCross.length() == 0 && data.strPlace.length() == 0) {
         
         // see if it has the expected (N)..(S) pattern
         // Or looks like a decimal number
         // If it is, put it in the place or cross street field
         // depending on whether or not it looks like an address
         Matcher match = NS_CROSS_PTN.matcher(field);
         if (match.matches()) {
           String tmp = match.group(2);
           if (tmp != null) {
             data.strApt = append(data.strApt, "-", match.group(1));
             field = tmp;
           }
           if (checkAddress(field) > 0) {
             data.strCross = field; 
           } else {
             data.strPlace = field;
           }
           return true;
         }
       }
       
       // Otherwise if this contains a comma or colon, it is probably a name
       // or date/time stamp field and we should reject it
       if (field.contains(",") || field.contains(":")) return false;
       
       // Otherwise return regular address check result
       return super.checkParse(field, data);
     }
   }
   
   private class MyPlaceField extends PlaceField {
     @Override
     public void parse(String field, Data data) {
       if (data.strPlace.equals(field)) return;
       data.strPlace = append(data.strPlace, " - ", field);
     }
   }
   
   private static final Pattern DATE_TIME_PTN = Pattern.compile("\\d\\d/[\\d:/ ]*");
   private class MyDateTimeField extends DateTimeField {
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       
       // Looser pattern match standard than the default date/time field
       // if we only have the first 3 characters of what looks like a date/time
       // field, that is good enough
       if (!DATE_TIME_PTN.matcher(field).matches()) return false;
       parse(field, data);
       return true;
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("CH")) return new ChannelField("OPS.*|.*FR|VPR.*");
     if (name.equals("SRC")) return new MySourceField();
    if (name.equals("CODE")) return new CodeField("\\d\\d[A-Z]\\d\\d[A-Za-z]?");
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("APT")) return new AptField("APT.*|SUITE.*");
     if (name.equals("PLACE")) return new MyPlaceField();
     if (name.equals("DATETIME")) return new MyDateTimeField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "BENS", "BENSON",
       "CLAY", "CLAYTON",
       "GARN", "GARNER",
       "SELM", "SELMA"
   });
 }
