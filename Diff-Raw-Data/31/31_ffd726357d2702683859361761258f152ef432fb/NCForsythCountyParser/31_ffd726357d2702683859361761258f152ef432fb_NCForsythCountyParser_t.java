 package net.anei.cadpage.parsers.NC;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Forsyth County, NC
 Contact: Shane Taylor <dstaylor9@gmail.com>
 Contact: "Fulk, Brad" <bfulk@huntersvillefd.com>
 Sender: forsythcountyfir@bellsouth.net,lfdalltext@lewisvillefire.com
 
 Location: 1 STOKES COUNTY: @STOKES COUNTY: 2312 HWY 65 E Nature: Unconscious/Unresponsive/Syncope P:1  - default DISTRICT: R09 X Str: / CALLER NAME: STOK
 Location: 6206 REIDSVILLE RD FC: @CREEKSIDE MANOR Nature: Chest Pain/Heart Problems P:1  - WITH_SYMPTOMS DISTRICT: R31 X Str: GOODWILL CHURCH RD/PINEY GR
 Location: 9350 FREEMAN RD FC Nature: Headache P:1  - VISION_PROBLEMS DISTRICT: R31 X Str: PINEY GROVE RD/GOODWILL CHURCH RD CALLER NAME: MARTIN, NAOMI
 Location: 6000BLK REIDSVILLE RD FC Nature: PUBLIC SERVICE CALL P:3  - default DISTRICT: F31 X Str: BRINKLEY RD/GOODWILL CHURCH RD CALLER NAME:
 Location: 1105 WHISPERING PINES DR FC Nature: Power Lines Arcing on Pole P:2  - default DISTRICT: F31 X Str: WARDS POND CT/QUAIL HOLLOW RD CALLER NAME:
 Location: 5825 POPPY SEED DR FC Nature: Brush Fire P:1  - default DISTRICT: F31 X Str: BRINKLEY PARK DR/JILAIN ST CALLER NAME: CARANNA, KENWYN
 Location: 1800 SPRINGFIELD FARM CT CL Nature: Breathing Difficulty P:1  - BREATHING_DIFF DISTRICT: R11 X Str: SPRINGFIELD FARM RD/ CALLER NAME:
 Location: 4595 STYERS FERRY RD WS Nature: Sick/Unknown P:1  - default DISTRICT: R11 X Str: FOX RIDGE LN/REMINGTON DR CALLER NAME:
 Nature: Motor Vehicle Accident P:1  - default DISTRICT: R11 X Str: LEWISVILLE-CLEMMONS RD/REYNOLDS RD CALLER NAME:
 Location: NB 421 FC Nature: Motor Vehicle Accident P:1  - default DISTRICT: R13 X Str: SCOTT RD/SB 421 CALLER NAME: FCSD
 Location: 191 LOWES FOOD DR FC: @A CLEANER WORLD Nature: Alarms - Fire or Smoke P:1\2s- default DISTRICT: F11 X Str: JENNINGS RD/SHALLOWFORD RD CALLER
 Location: 6441 HOLDER RD CL,RM 8: @CLEMMONS VILLAGE II Nature: Breathing Difficulty P:1\2s- BREATHING_DIFF DISTRICT: R11 X Str: HOLDER CT/SOUTHWEST SCH
 
  */
 
 
 public class NCForsythCountyParser extends FieldProgramParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "FC", "FORSYTH COUNTY",
       "CL", "CLEMMONS",
       "LE", "LEWISVILLE",
       "WS", "WINSTON-SALEM"
   });
   
   private static final Pattern MA_PATTERN = Pattern.compile("@(.+):"); 
   
   public NCForsythCountyParser() {
     super(CITY_CODES, "FORSYTH COUNTY", "NC",
            "Location:ADDR/S Nature:CALL! P:SKIP DISTRICT:UNIT X_Str:X CALLER_NAME:NAME");
   }
   
   @Override
   public String getFilter() {
    return "forsythcountyfir@bellsouth.net,lfdalltext@lewisvillefire.com,gfdpaging@griffithfire.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     body = body.replaceAll(": @", " @");
     if (!super.parseMsg(body, data)) return false;
     
     if (data.strCity.equals(data.defCity)) data.strCity = "";
     if (data.strCross.equals("/")) data.strCross = "";
     
     // Intersections seem to be saved as cross streets, at least some of the time
     if (data.strAddress.length() == 0) {
       if (data.strCross.length() == 0) return false;
       String sAddress = data.strCross;
       data.strCross = "";
       parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END, sAddress, data);
     }
     return true;
   }
   
   private static final Pattern FC_PTN = Pattern.compile("\\bFC\\b");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String fld, Data data) {
       Matcher match = MA_PATTERN.matcher(fld);
       if (match.find()) {
         data.strCity = match.group(1);
         fld = fld.substring(match.end()).trim();
       }
       int pt = fld.indexOf('@');
       if (pt >= 0) {
         data.strPlace = fld.substring(pt+1).trim();
         fld = fld.substring(0,pt).trim();
       }
       
       super.parse(fld, data);
       
       // The use FC (Forsythe County) to indicate county roads,
       // which neither the smart parser nor Google understand.
       // so change them to CO. (we can't do this prior to the smart parse
       // call because 'FC' is a legitimate city code.
       data.strAddress = FC_PTN.matcher(data.strAddress).replaceAll("CO");
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " PLACE";
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
