 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /* 
 Chester County, PA (Variant D3)
 Contact: Bob Gliem <goshenfire@gmail.com>
 Sender: gfc@goshenfireco.org
 
 (1566 VASSAR CT ,53) 56  10/21/11  15:37  APLNC  APPLIANCE FIRE *  1566 VASSAR CT ,53  -  EGOSHN  HERSHEYS MILL V
 (1515 MANLEY RD ,53) 54  10/26/11  09:45  APT  APARTMENT BUILDING FIRE *  1515 MANLEY RD ,53  BUILDING 5, APT 433-OXFORD GAT    EGOSHN
 (992 GARRETT MILL RD ,54) 54  10/24/11  15:45  ALARMF  ALARM - FIRE *  992 GARRETT MILL RD ,54  HITSCHLER RES#610-578-0346-    WILLIS
 (1006 ROBIN DR ,67) 54  10/24/11  10:40  ALARMF  ALARM - FIRE *  1006 ROBIN DR ,67  GILBERT RES - 610.399.1788-    WESTWN
 (1201 E STRASBURG RD ,52) 54  10/21/11  21:11  FUEL  FUEL SPILL *  1201 E STRASBURG RD ,52  FRONT P/LOT-STRASBURG COURT AP  WGOSHN
 (980 HERSHEY MILL RD ,53) 56  10/26/11  18:56  ALARMF  ALARM - FIRE *  980 HERSHEY MILL RD ,53  LEWIS RES/610-644-8389-    EGOSHN
 
  */
 
 
 public class PAChesterCountyD3Parser extends PAChesterCountyBaseParser {
   
   private static final Pattern DELIM = Pattern.compile(" {2,}");
   
   public PAChesterCountyD3Parser() {
     super("DIGITS DATE TIME CODE CALL ADDRCITY! INFO+");
   }
   
   @Override
   public String getFilter() {
    return "gfc@goshenfireco.org";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (isVariantGMsg(body)) return false;
 
     // subject is truncated version of address that we don't care about
     // but it has to be non-empty
     if (subject.length() == 0) return false;
     
     // And all of the should treat line breaks as spaces
     body = body.replace('\n', ' ');
 
     // Split and parse by double blank delimiters
     return parseFields(DELIM.split(body), data);
   }
   
   // Call field strips trailing asterisk marker
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith(" *")) field = field.substring(0,field.length()-2);
       super.parse(field, data);
     }
   }
   
   // Info field checks for city codes
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith("-")) field = field.substring(0,field.length()-1);
       if (field.length() == 0) return;
       String city = CITY_CODES.getProperty(field);
       if (city != null) {
         data.strCity = city;
       } else {
         super.parse(field, data);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "CITY INFO";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("DIGITS")) return new SkipField("\\d\\d", true);
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
 } 
