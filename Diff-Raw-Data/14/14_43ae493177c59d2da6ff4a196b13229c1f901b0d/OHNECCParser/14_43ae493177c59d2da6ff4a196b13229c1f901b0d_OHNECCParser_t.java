 package net.anei.cadpage.parsers.OH;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 NECC, OH
 Contact: Jon Frye <jfrye@safety-center.org>
Sender; Dispatcher@safety-center.org,rc.340@c-msg.net
 
 (NECC) [F] LV:1 ALARM CARBON MONOXIDE NO SYMPTOMS\n/\n1619 W LOVELAND AV\n(LV)\nXS: S LEBANON RD & HEIDELBURG DR\n\nRCVD AS Phone\n\nCOM:\nALARM ACTIVATED
 [NECC]  [F] LV:1 ALARM FIRE\n/\nCHILI'S GRILL & BAR\n11329<tel:11329> MONTGOMERY RD\n(SY)\nXS: HARPERSPOINTE DR & E KEMPER RD\n\nRCVD AS Phone\n\nCOM:\nF28 FIRE DUCT ALARM\nE9
 (NECC) [F] LV:1 AUTO ACCIDENT INJURY\n/\nFIELDS ERTEL RD/UNION CEMETERY RD\n(SY)\nXS: UNION CEMETERY RD & GREEN ARBORS LN\n\nRCVD AS Phone\n\nCOM:
 (NECC) [F] LV:1 AUTO ACCIDENT VEHICLE FIRE\n/\nBECKER\n9695 LOVELAND MADEIRA RD\n(SY)\nXS: HUMPHREY RD & MORGANS TRACE\n\nRCVD AS Phone
 
Contact: Nate Ritchey <nwritchey911@gmail.com>
Subject:NECC\n[E] LV:1 EMS SUICIDAL SUBJECT\n/\n11645 THISTLEHILL DR\n(SY)\nXS: HEIDLEBERG DR & SYMBOLA DR\n\nRCVD AS Phone\n\nCOM:\nC\nALLED IN 3RD PARTY-BY THE PATIENTS COUNSELOR -\nJENNIFER REDWITZ - 685.9344\nSHE STATED THAT THE 26-F @ THIS ADDRESS IS \n

 */
 public class OHNECCParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "BLUE",  "BLUE ASH",
       "DFTP",  "DEERFIELD TWP",
       "LV",    "LOVELAND",
       "MASO",  "MASON",
       "SHRN",  "SHARONVILE",
       "SY",    "SYMMES",
       "SYCMTP","SYCAMORE TWP"
   });
   
  private static final Pattern SUBJECT_PTN = Pattern.compile("NECC\\|[EF]");
   private static final Pattern PHONE_PTN = Pattern.compile("<tel:(.*)>");
 
   public OHNECCParser() {
     super(CITY_CODES, "", "OH",
            "LV:CALL! SKIP PLACE? ADDR! CITY XS:X COM:SKIP INFO+");
   }
   
   @Override
   public String getFilter() {
    return "Dispatcher@safety-center.org,rc.340@c-msg.net";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (! SUBJECT_PTN.matcher(subject).matches()) return false;
     
     return parseFields(body.split("\\n"), data);
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(' ');
       if (pt >= 0) field = field.substring(pt+1).trim();
       super.parse(field, data);
     }
   }
   
   private class MyAddressField extends AddressField {
 
     @Override
     public boolean checkParse(String field, Data data) {
       field = removePhone(field, data);
       return super.checkParse(field, data);
     }
 
     @Override
     public void parse(String field, Data data) {
       field = removePhone(field, data);
       super.parse(field, data);
     }
     
     private String removePhone(String field, Data data) {
       Matcher match = PHONE_PTN.matcher(field);
       if (match.find()) {
         data.strPhone = match.group(1);
         field = field.substring(0,match.start()) + field.substring(match.end());
       }
       return field;
     }
 
     @Override
     public String getFieldNames() {
       return "PHONE " + super.getFieldNames();
     }
     
   }
   
   private class MyCityField extends CityField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("(")) field = field.substring(1);
       if (field.endsWith(")")) field = field.substring(0,field.length()-1);
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("CITY")) return new MyCityField();
     return super.getField(name);
   }
   
 }
