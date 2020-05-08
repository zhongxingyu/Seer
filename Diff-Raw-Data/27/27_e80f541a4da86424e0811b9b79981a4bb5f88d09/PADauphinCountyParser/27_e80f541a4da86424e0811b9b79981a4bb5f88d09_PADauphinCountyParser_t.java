 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Dauphin County, PA
 Contact: Chris Gros <gichristopher6@gmail.com>
 Sender: @c-msg.net
 CodeMessaging client 446
 
 (29CAD) [!] Box:29-1 Loc:31 S RIVER RD HFT DAUP XSts:PETERS MOUNTAIN RD ,SYCAMORE AV Event Type:OVERDOSE / POISONING (INGESTION) Class: 1 Disp: P29
 (29CAD) [!] Box:29-6 Loc:100 S FOURTH ST HFX DAUP: @BEER AND SODA PLUS XSts:RISE ST ,PETERS MOUNTAIN RD Event Type:TRAFFIC / TRANSPORTATION ACCIDENT INJURIES Class: Disp: E29-1
 (29CAD) [!] Box:29-6 Loc:123 RIVERVIEW DR HFT DAUP Event Type:CHEST PAIN Class: 1 Disp: 13-2,6-4
 (29CAD) [!] Box:29-6 XSts:MARKET ST ,S THIRD ST Event Type:UNCONSCIOUS / FAINTING (NEAR) Class: 2 Disp: 13-1
 (29CAD) [!] Box:29-4 Loc:388 DUNKEL SCHOOL RD HFT DAUP XSts:HILLTOP RD ,ROUNDTOP DR Event Type:ASSIST / SERVICE CALLS SELECT SUB-TYPE IF APPROPRIATE Class: Disp: P29
 (29CAD) [!] Box:29-1 Loc:499 S RIVER RD HFT DAUP XSts:AMITY LN ,SHAMOS SCHOOL RD Event Type:PSYCHIATRIC / SUICIDE ATTEMPT Class: 2 Disp: 13-1
 (29CAD) [!] Box:29-1 Loc:3188 PETERS MOUNTAIN RD HFT DAUP XSts:HILL DR ,ROADCAP LN Event Type:ABDOMINAL PAINS / PROBLEMS Class: 1 Disp: 13-1,6-4
 (29CAD) [!] Box:29-6 Loc:502 N SECOND ST HFT DAUP XSts:GREEN ST ,LOCUST ST Event Type:HEART PROBLEMS / A.I.C.D. Class: 1 Disp: 13-2,6-4
 (29CAD) [!] Box:29-1 Loc:3777 PETERS MOUNTAIN RD HFT DAUP: @GIANT XSts:LEISURE LN ,SWEIGARD DR Event Type:CONVULSIONS / SEIZURES Class: 1 Disp: 13-2,6-5
 (29CAD) [!] Box:29-5 Loc:123 BRUBAKER RD RDT DAUP XSts:S RIVER RD ,MOUNTAIN RD Event Type:BREATHING PROBLEMS Class: 1 Disp: 13-2
 (29CAD) [!] Box:29-5 Loc:S RIVER RD RDT DAUP XSts:INGLENOOK RD ,RAMP Event Type:TRAFFIC / TRANSPORTATION ACCIDENT INJURIES Class: Disp: E29-1
 (29CAD) [!] Box:29-4 XSts:N RIVER RD ,MCCLELLAN RD Event Type:SMOKE INVESTIGATION Class: 3 Disp: X29
 (29CAD) [!] Box:29-1 Loc:176 DUSTY TRAIL RD HFT DAUP XSts:CREEK SIDE DR ,DEAD END Event Type:OUTSIDE FIRE NATURAL COVER Class: 1 Disp: E29-1,A20
 (29CAD) [!] Box:29-6 Loc:117 ARMSTRONG ST HFX DAUP XSts:N FRONT ST ,FISHER ST Event Type:CARDIAC OR RESPIRATORY ARREST / DEATH AED RESPONSE Class: 1 Disp: P29
 (29CAD) [!] Box:29-6 Loc:201 MARKET ST HFX DAUP XSts:S SECOND ST ,S THIRD ST Event Type:CHEST PAIN Class: Disp: M/PC81,13-1,20-2
 (29CAD) [!] Box:29-2 Loc:676 DUNKEL SCHOOL RD HFT DAUP XSts:CATERPILLAR LN ,KEIFFER RD Event Type:BREATHING PROBLEMS Class: 1 Disp: 13-2,6-5
(21CAD) [!] Box:21-4 Loc:179 WOLFE RD WST DAUP XSts:RAKERS MILL RD ,HENNINGER RD Event Type:BREATHING PROBLEMS Class: 1 Disp: P21
 
  */
 
 public class PADauphinCountyParser extends FieldProgramParser {
   
   public PADauphinCountyParser() {
     super(CITY_CODES, "DAUPHIN COUNTY", "PA",
            "Box:BOX! Loc:ADDR/S XSts:X Event_Type:CALL! Class:PRI! Disp:UNIT!");
   }
   
   @Override
   public String getFilter() {
     return "@c-msg.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.endsWith("CAD|!")) return false;
     data.strSource = subject.substring(0,subject.length()-2);
     if (!super.parseMsg(body, data)) return false;
     if (data.strAddress.length() == 0) {
       data.strAddress = data.strCross.replace('/', '&');
       data.strCross = "";
     }
     return true;
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(": @");
       if (pt >= 0) {
         data.strPlace = field.substring(pt+3).trim();
         field = field.substring(0,pt).trim();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " PLACE";
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       field = field.replace(" ,", " / ");
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       
       // Missing
       // BERRYSBURG
       // JEFFERSON TWP
       // LYKENS TWP
       // RUSH TWP
                    
       "CWT DAUP",  "CONEWAGO TWP",
 //      "DAB DAUP",  "DAB DAUP",  // Unknown
       "DRY DAUP",  "DERRY TWP",
       "EHT DAUP",  "EAST HANOVER TWP",
       "ELZ DAUP",  "ELIZABETHVILLE",
 //      "HBG DAUP",  "HBG DAUP", // Unknown
       "HFT DAUP",  "HALIFAX TWP",
       "HFX DAUP",  "HALIFAX",
       "HSP DAUP",  "HIGHSPIRE",
       "HUM DAUP",  "HUMMELSTOWN",
       "JKT DAUP",  "JACKSON TWP",
       "LDT DAUP",  "LONDONDERRY TWP",
       "LPT DAUP",  "LOWER PAXTON TWP",
       "LST DAUP",  "LOWER SWATARA TWP",
       "LYK DAUP",  "LYKENS",
       "MDT DAUP",  "MIDDLETOWN",
       "MPT DAUP",  "MIDDLE PAXTON TWP",
       "MSB DAUP",  "MILLERSBURG",
       "PAX DAUP",  "PAXTANG",
       "PEN DAUP",  "PENBROOK",
       "RDT DAUP",  "REED TWP",
       "ROY DAUP",  "ROYALTON",
       "SHT DAUP",  "SOUTH HANOVER TWP",
       "STL DAUP",  "STEELTON",
       "SUS DAUP",  "SUSQUEHANNA TWP",
       "SWT DAUP",  "SWATARA TWP",
       "UPT DAUP",  "UPPER PAXTON TWP",
       "WHT DAUP",  "WEST HANOVER TWP",
       "WIC DAUP",  "WICONISCO",
       "WIL DAUP",  "WILLIAMSTOWN",
       "WLT DAUP",  "WILLIAMS TWP",
      "WST DAUP",  "WASHINTON TWP",
 
       // Cumberland County
       "EPEN CUMB", "EAST PENNSBORO TWP",
       "SVSP CUMB", "SILVER SPRING TWP", 
       
       // Lancaster County
       "EZAB LANC", "ELIZABETHTOWN",
       "MTJT LANC", "MT JOY TWP",
       
       // Lebanon County
       "SLON LEBA", "SOUTH LONDONDERRY TWP",
       
       // Northumberland County
       "JKST NORT", "JACKSON TWP",
       "JORD NORT", "JORDAN TWP",
       "HNDB NORT", "HERNDON",
       
       // Perry County
       "RYET PERR", "RYE TWP",
       "WTFD PERR", "WHEATFIELD TWP"
 
   });
 }
