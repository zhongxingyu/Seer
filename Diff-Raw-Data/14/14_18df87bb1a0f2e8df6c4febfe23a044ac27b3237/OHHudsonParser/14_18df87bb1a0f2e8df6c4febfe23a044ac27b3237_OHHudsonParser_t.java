 package net.anei.cadpage.parsers.OH;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Hudson, OH
 Contact: "Graham, Bruce" <BGraham@hudson.oh.us>
 Contact: dthompson@hudson.oh.us<mailto:dthompson@hudson.oh.us>
 Sender: hudsoncad@hudson.oh.us
 
 HudsonCAD:CAD System 6150,NICHOLSON DR.,,HUDSON EMS,EMS63 YOM, DIABETIC SHOCK
 HudsonCAD:CAD System 0,ANYWHERE,OTHER LOCATION OUT OF CITY,HudsonEMS,EMSMUTAL AID TO TWINSBURG FOR WORKING HOUSE FIRE, 2416 WHITE MARSH
 HudsonCAD:CAD System 6150,NICHOLSON DR.,,HUDSON EMS,EMS
 HudsonCAD:CAD System 75,EXECUTIVE PKWY.,ALLSTATE INSURANCE (75 EXEC),HUDSONEMS,EMSfemale diff breathing allergic reaction first floor front
 HudsonCAD:CAD System 0,W STREETSBORO ST.,91 & 303,HUDSONEMS,EMStest do not respond test only
 HudsonCAD:CAD System 5555,DARROW RD.,JOANN STORES INC.,HUDSONEMS,EMSSHORTNESS OF BREATH MALE N GATE SECURITY OFFICE
 HudsonCAD:CAD System 200,LAUREL LAKE DR.,,EMS,EMSGREENWOOD 303 LUMBAR PAIN
 HudsonCAD:CAD System 1430,WINSLOW DR.,,HUDSON EMS,EMSCHEST PAINS
 
 HudsonCAD:CAD System 200,LAUREL LAKE DR.,LAUREL LAKE RETIREMENT COMMUNITY,HUDSONF376,Fire Alarm: , Public Buildings 0600 to MidnightCAL COM CYTHIA 800 498 7565 COMMONS FIRE ALARM
 HudsonCAD:CAD System 7421,MARBLEHEAD DR.,,HudsonF374,Fire Alarm: ResidentialSMOKE HEAT FIRE ALARM PER JULIA WITH DIEBOLD 18005484478
 
  */
 
 
 public class OHHudsonParser extends FieldProgramParser {
   
   public OHHudsonParser() {
     super("HUDSON", "OH",
           "NUM ADDR PLACE SRC! INFO MAADDR");
   }
   
   @Override
   public String getFilter() {
     return "hudsoncad@hudson.oh.us";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (! body.startsWith("HudsonCAD:CAD System ")) return false;
     body = body.substring(21).trim();
     return parseFields(body.split(","), data);
   }
   
   private class NumberField extends Field {
     @Override
     public void parse(String field, Data data) {
       if (!field.equals("0")) data.strAddress = field;
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       field = append(data.strAddress, " ", field);
       data.strAddress = "";
       field = field.replace(".", "");
       parseAddress(field, data);
     }
   }
   
   private static Pattern CALL_PTN = Pattern.compile("^(FIRE|EMS)");
   private class MyInfoField extends InfoField {
     
     @Override
     public void parse(String field, Data data) {
       if (data.strCall.length() == 0) {
         Matcher match = CALL_PTN.matcher(field);
         if (match.find()) {
           data.strCall = match.group(1);
           field = field.substring(match.end()).trim();
         }
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "CALL INFO";
     }
   }
   
   private static Pattern MUT_AID_PTN = Pattern.compile(" TO (.*) FOR ");
   private class MAAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       if (data.strAddress.equals("ANYWHERE")) {
         data.strAddress = "";
         parseAddress(field, data);
         Matcher match = MUT_AID_PTN.matcher(data.strSupp);
         if (match.find()) data.strCity = match.group(1);
       }
       
       else {
         data.strSupp = append(data.strSupp, " / ", field);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "CITY ADDR INFO";
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("NUM")) return new NumberField();
     if (name.equals("ADDR")) return new MyAddressField();
    if (name.equals("SRC")) return new SourceField("(?:HUDSON|Hudson)? ?(?:EMS|FIRE|F\\d+)", true);
     if (name.equals("INFO")) return new MyInfoField();
     if (name.equals("MAADDR")) return new MAAddressField();
     return super.getField(name);
   }
 }
