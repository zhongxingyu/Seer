 package net.anei.cadpage.parsers.FL;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Sarasota County, FL
 Contact: Bill Hoag <bikeh1@gmail.com>
 Sender: 77711
 System: Integraph
 
 SARASOTA COUNTY EVENT TYPE: ODOR-INSIDE Location: 4546 DEER CREEK BLVD NCTY G225 TIME: 21:39:20  Disp: BN3,E14,E15,R14
 SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 4681 MACEACHEN BLVD NCTY G146 TIME: 18:23:38  Disp: BN3,E11,E12,E8,R2,R7,T18,TRK11
 SARASOTA COUNTY EVENT TYPE: FALLS Location: 1035 N LIME AVE SARA G056 TIME: 14:35:29  Disp: BN1,E1,EMS1,R1
 SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 3908 WALNUT AVE NCTY G033 TIME: 16:12:52  Disp: BN2,E1,E4,E5,EMS1,R3,R5,SQ8,TRK5
 SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 3908 WALNUT AVE NCTY G033 TIME: 16:12:52  Disp:
 SARASOTA COUNTY EVENT TYPE: ASSIST OTHER MEDICAL AGENCY Location: 5731 BEE RIDGE RD NCTY G128: @DOCTORS TIME: 18:51:05  Disp: BN2,E8,R8
 SARASOTA COUNTY EVENT TYPE: VEHICLE CRASH WITH INJURIES Location: BEE RIDGE RD/MAUNA LOA BLVD NCTY G149 TIME: 02:06:18  Disp: BN2,E16,R8,SQ8
 SARASOTA COUNTY EVENT TYPE: OUTSIDE FIRE-UNKNOWN Location: STONEYCREEK BLVD/CENTER RD SCTY G511 TIME: 19:00:05  Disp: BN4,E21,E26,R21
 SARASOTA COUNTY EVENT TYPE: VEHICLE CRASH WITH INJURIES Location: S TUTTLE AVE/SUNNYSIDE ST NCTY G124 TIME: 19:28:44  Disp: BN1,E2,EMS1,R2
 
  */
 
 public class FLSarasotaCountyParser extends FieldProgramParser {
   
   public FLSarasotaCountyParser() {
     super(CITY_CODES, "SARASOTA COUNTY", "FL",
        "TYPE:CALL! Location:ADDR/S! TIME:TIME! Disp:UNIT!");
   }
   
   @Override
   public String getFilter() {
    return "77711";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
    if (!body.startsWith("SARASOTA COUNTY EVENT ")) return false;
    body = body.substring(22).trim();
     return super.parseMsg(body, data);
   }
   
   private static final Pattern MAP_PTN = Pattern.compile(" (G\\d{3})$");
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       
       int pt = field.indexOf(": @");
       if (pt >= 0) {
         data.strPlace = field.substring(pt+3).trim();
         field = field.substring(0,pt).trim();
       }
       
       Matcher match = MAP_PTN.matcher(field);
       if (!match.find()) abort();
       data.strMap = match.group(1);
       field = field.substring(0,match.start()).trim();
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " MAP PLACE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "NCTY", "",
       "SCTY", "",
       
       "SARA", "SARASOTA"
   });
 }
