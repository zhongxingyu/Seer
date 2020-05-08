 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Chautauqua County, NY
 Contact: "Chuck Green" <tacticalfire@gmail.com>
 C/O: Patrick Jackson <patjackson52@gmail.com>
 
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 07:06 *EMS CALL ; 3735 WESTMAN RD ; C/T/V Ellery ; BRET SANTILLI ; 17 yr female seizure  ; A441
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 00:44 *EMS CALL ; 170 HIGH ACRES MHP ; C/T/V Ellery ; DONALD SCHAUMAN ; hip pain and back pain ; A441 E301 E302 T301 A301 R302 E373 E383 in Sun Room Zone 27 ;
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 12:18 *STRUCTURE FIRE -3000 ; 5457 BLY HILL RD ; C/T/V North Harmony ; Richard Robinson ; Fire alarm A301 R302 A281
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 19:43 *EMS CALL ; 3246 PANAMA STEDMAN RD ; C/T/V North Harmony ; Gregory Mcchesney ; MALE INCOHERENT / ELEV BP AND TEMP ;
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 12:06 *EMS CALL ; 5410 MEADOWS RD ; C/T/V Chautauqua ; C MERRITT ; 73 yoa fm shortness in breath gene ELLE M10 MAPL
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 18:46 *EMS CALL ; 4412 DUTCH HOLLOW RD ; C/T/V Ellery ; FLOYD DELLAHOY ; MALE - DIFF BREATHING ; A481
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 14:05 *EMS CALL ; 4900 RT430 lot 1 ; C/T/V Ellery ; ; ; A481
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 17:10 *EMS CALL ; 2916 BENTLEY AVE ; C/T/V Ellicott ; ; FEMALE - DIFF BREATHING ; FLUV A441
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 17:24 *EMS CALL ; 5011 STONELEDGE RD ; C/T/V North Harmony ; ; MALE - FALLEN, POSS STROKE ; A301 R30
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 11:27 *STRUCTURE FIRE -3000 ; 1136 HOAG RD ; C/T/V Harmony ; RICK NORRIS ; house fire ;
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 18:36 *SINGLE COMPANY ; 27 E  MAIN ST ; C/T/V Panama ; ; ASHVILLE ONE ENGINR STAND BY AT PANAMA FD  ;
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 15:18 *EMS CALL ; S  MAPLE AVE GLEASON RD ; C/T/V Busti ; ; ; A301 R302
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 00:15 *EMS CALL ; 28 LIBERTY ST ; C/T/V Bemus Point ; CHARLES CROSSLEY ; f/70 back pains and leg pain
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 15:11 *SINGLE COMPANY ; BEMUS ELLERY RD MAHANNA RD ; C/T/V Ellery ; Philip Erickson ; live electric line across the road.
 CHAUTAUQUA_COUNTY_SHERIFF (MSP CAD) 05:01 *EMS CALL ; 5325 RAMSEY RD ; C/T/V North Harmony ; BRUCE HARRIS ; 62/F  SEMI CONSCIOUS ; A441
 
 Contact: Steve Hayes <stevekhayes@gmail.com>
 Sender: messaging@iamresponding.com
 (BEMUS POINT) 21:45 *MVA PI ; 1220 86 WB ; C/T/V Ellery ; NYSP Jamestown ; 3A14 OUT WITH AN MVA WITH MINOR INJURIES   I86 WB NEAR OVERLOOK ; A441 R442 E
 
 Contact: "cdlj6286@yahoo.com" <cdlj6286@yahoo.com>
 Sender: dispatch@sheriff.us
 Subject:MSP CAD\n22:26 *SINGLE COMPANY ; 12082 HANOVER RD ; C/T/V Hanover ; HC1 ; Donna Bartelo ; power lines arcing on a pole, in front of \r
 
 */
 
 
 public class NYChautauquaCountyParser extends FieldProgramParser {
   
   private static final Pattern MARKER1 = Pattern.compile("^CHAUTAUQUA_COUNTY_SHERIFF \\(([A-Z ]+)\\) (\\d\\d:\\d\\d) \\*");
   private static final Pattern MARKER2 = Pattern.compile("^(\\d\\d:\\d\\d) \\*");
   private static final Pattern DELIM = Pattern.compile(" *(?<= ); ");
   private static final Pattern UNITS = Pattern.compile("(?: *\\b(?:[A-Z]{4}|[A-Z]\\d{2,3})\\b)+");
   
   public NYChautauquaCountyParser() {
     super("CHAUTAUQUA COUNTY", "NY",
            "CALL! ADDR/iS! CTV:CITY! NAME INFO UNIT");
   }
   
   @Override
   public String getFilter() {
     return "911@cattco.org,7770, messaging@iamresponding.com,dispatch@sheriff.us";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     do {
       Matcher match = MARKER1.matcher(body);
       if (match.find()) {
         data.strSource = match.group(1);
         data.strTime = match.group(2);
         body = body.substring(match.end()).trim();
         break;
       }
       
       match = MARKER2.matcher(body);
       if (match.find() && subject.length() > 0) {
         data.strSource = subject;
         data.strTime = match.group(1);
         body = body.substring(match.end()).trim();
         break;
       }
       
       return false;
     } while (false);
     
     body = body.replace(" C/T/V ", " CTV: ");
     if (body.endsWith(";")) body = body.substring(0,body.length()-1);
     return parseFields(DELIM.split(body),data);
   }
   
   @Override
   public String getProgram() {
     return "SRC TIME " + super.getProgram();
   }
   
   // Name field needs to remove trailing commas
   private class MyUnitField extends UnitField {
 
     @Override
     public void parse(String field, Data data) {
       Matcher match = UNITS.matcher(field);
       String info;
       if (match.find()) {
         data.strUnit = match.group();
         info = append(field.substring(0,match.start()).trim(), " / ", field.substring(match.end()).trim());;
       } else {
         info = field;
       }
       data.strSupp = append(data.strSupp, " / ", info);
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("UNIT")) return new MyUnitField();
     return super.getField(name);
   }
   
 }
 	
