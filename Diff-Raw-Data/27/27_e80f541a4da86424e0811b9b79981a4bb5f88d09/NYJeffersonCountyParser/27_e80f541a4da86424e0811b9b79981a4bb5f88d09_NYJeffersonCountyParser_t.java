 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Jefferson County, NY
 
 Contact: "jayscad@yahoo.com" <jayscad@yahoo.com>,jay greening <jagx91@gmail.com>
 Sender: tirescue@googlegroups.com
 BREATHING PROBL|18077 REED POINT RD:ORLEANS(T)|77 YO MALE DIFF BREATHING
 ABDOMINAL PAIN|18081 REED POINT RD:ORLEANS(T)|79F  :ProQA Medical Case Entry Finished You are responding to a patient with abdominal pain.  The patient is a  79-year-old female, who is conscious and breathing.  Abdominal Pain / Problems.  Caller Statement: ABDOMINAL PAIN.
 ASSIST-EMS|I 81 MM 176 N:ALEXANDRIA(T)|1 mile south of 50 in the north bound lane   vehicle appears to be stuck in mediun    unkn9ow if occupied  :3D63 REQUESTING EMS RESPOND.  ONE FEMALE OCCUPANT COMPLAINING OF WRIST PAIN.
 CHEST PAIN|622 JOHN ST:CLAYTON(V)|59 FEMALE/CARDIAC ISSUES
 MVA-PI|SHIMEL RD & STATE ROUTE 411:ORLEANS(T)|1 VEH OFF ROAD - MINOR INJURY
 FIRE ALARM|37382 ORLEANS CEMETERY RD:ORLEANS(T)|CAPUTO RESIDENCE GENERAL SMOKE ALARM
 (DISPATCH:4392) MVA-PI|I 81 MM 176 N:ALEXANDRIA(T)|SPUN OUT IN THE MIDDLE OF DRIVING LANE YELLOW SEDAN NORTHBOUND LANE. CALL 
 
 Sender: lvfd27@googlegroups.com
 FALL|34226 CARTER STREET RD:ORLEANS(T)|  CALLBACK=(315)489-3188 LAT=  LON=  
 UNC=    06315-489-3188 12/05 17:38:29  071                                 
 Verizon Wireless            WPH1                 Honey Flats Road - SW  
 Sector                   LaFargeville                  NY                     
                                 LAT:+044.158612  LON:-075.905035ELV:+00000  
 COF:1709      COP:100MTN:315-511-4647       CPF:VZW  -911ai.com-             
 ESN:01315
 
 Jay Greening <jagx91@hotmail.com>
 ((LVFD) DISPATCH:4391,27FR) SICK PERSON|30950 STATE ROUTE 180  ;NORTHER NY AGRICULTRUAL MUSEUM:ORLEANS(T)|
 
 Contact: Mauser*Girl <mausergirl@gmail.com>
 From: 6245
 sentto-76513067-101-1314862564-3159559896=vtext.com@returns.groups.yahoo.com ([carthageambulance] DISPATCH:1191) FALL|1045 WEST ST:CARTHAGE(V)|83 Y/F FELL OUT OF BED BROKEN NOSE AN
 
Contact: support@active911.com
Agency name: Adams Fire Dept. 
(DISPATCH:1) CHIMNEY FIRE|12636 COUNTY ROUTE 66:ADAMS(T)|CHIMNEY FIRE   CALLBACK=(315)783-7406 LAT=  LON= UNC=    05315-783-7406 03/08 13:43:40  086CAUTION:  WIRELESS CALL         WIRELESS-AT&amp;T MOBILITY(TCS) WPH1   18864         CADY RD                                        ADAMS CENTER                  NYCELL = 3034  SECTOR = 3                             LAT:+043.910222  LON:-075.996056ELV:+00000 COF:0         COP:000MTN:315-511-4609       CPF:ATTMO-911ai.com-            ESN:00888                                VERIFY PD                       VERIFY FD                       VERIFY EMS
(DISPATCH:29,1) TEST-FIRE/EMS|310 S MAIN ST; MANNSVILLE MANOR FIRE DEPART:MANNSVILLE(V)|

  */
 
 
 public class NYJeffersonCountyParser extends FieldProgramParser {
   
   public NYJeffersonCountyParser() {
     super("JEFFERSON COUNTY", "NY",
           "CALL ADDR! INFO+");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     String[] flds = body.split("\\|", -1);
     if (flds.length != 3) return false;
     return parseFields(flds, data);
   }
   
   private static final Pattern ADDR_PTN = Pattern.compile("\\(.\\)$");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = ADDR_PTN.matcher(field);
       if (match.find()) field = field.substring(0,match.start()).trim();
       Parser p = new Parser(field.trim());
       data.strCity = p.getLastOptional(':');
       data.strPlace = p.getLastOptional(';');
       parseAddress(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR PLACE CITY";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
