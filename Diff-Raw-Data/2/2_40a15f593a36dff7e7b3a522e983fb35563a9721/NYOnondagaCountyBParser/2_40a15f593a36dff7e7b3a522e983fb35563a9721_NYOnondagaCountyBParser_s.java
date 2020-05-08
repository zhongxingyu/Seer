 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Onondaga County, NY (Variant B)
 Contact: Michael Remillard <mremillard1982@gmail.com>
 Sender: sta2adi@mcfd.org
 
 Subject:7651 MORGAN RD\n05/18/12\n11:18\nMOF123900539\nTCL\nF-GENERAL ILLNESS\n7651 MORGAN RD\nPICCADILLY SQ AND BUCKLEY RD\nBUCKLEY PARK APTS-APT I -7\nDEMO,SEAN\nSPECIAL ADDRESS COMMENT:\nACCESS INFORMATION:\nUSE MORGAN RD DRIVEWAY FOR BLDGS A - E\nUSE BUCKLEY RD DRIVEWAY FOR BLDGS F - K\n_______________________________________________\n[e9kduc 04/01/2012]\nPAIN IN BOTH ARMS\nNumber of patients: 1\nResponder script: 39 year old, Female, Conscious, Breathing. Sick Person\n(Specific Diagnosis). Caller Statement: PAIN IN BOTH ARMS .\nPAIN IN BOTH ARMS\nResponse text: Alpha\nResponder script: General weakness.\nARMS FEELS VERY HEAVY - PAIN BETWEEN SHOULDER BLADES -\nNO OTHER MED HX\nPOI COUNTY CH5\nNEED RESCUE P3 FOR LIFTING\n\n\n\nMOF
 Subject:7245 HENRY CLAY BLVD\n05/17/12\n10:39\nMOF123900538\nTCL\nF-ALARM-Fire\n7245 HENRY CLAY BLVD\nW TAFT RD AND CROSSROADS PARK DR\nRITE AID DISTRIBUTION-\n\nT/V FIRE ALARM - OLD WAREHOUSE FIRE ALARM INDICATED - ADT - #IKA, 8772387739\nANY UNIT FOR\n\n\nBC2,E11,E21,E31,E32,E41,LD1,TK2,\nMOF
 Subject:7651 MORGAN RD\n05/17/12\n08:23\nMOF123900535\nTCL\nF-PAIN (NON-TRAUMATIC)\n7651 MORGAN RD\nPICCADILLY SQ AND BUCKLEY RD\nBUCKLEY PARK APTS-BLDG D, APT\nCORREIRA,MICHAE\nSPECIAL ADDRESS COMMENT:\nACCESS INFORMATION:\nUSE MORGAN RD DRIVEWAY FOR BLDGS A - E\nUSE BUCKLEY RD DRIVEWAY FOR BLDGS F - K\n_______________________________________________\n[e9kduc 04/01/2012]\nM911 CC RE:\nBELLY BUTTON PAIN\nNumber of patients: 1\nResponder script: 45 year old, Male, Conscious, Breathing. Abdominal Pain\nProblems. Caller Statement: BELLY BUTTON PAIN.\nResponse text: Charlie\nResponder script: Males with pain above navel => 35.\nReconfigured on another card: No\nPOI\n\n\nSD2,SD3,\nMOF
 Subject:4227 WETZEL RD\n05/17/12\n07:51\nMOF123900534\nTCL\nF-WIRE(S) DOWN-Emergency\n4227 WETZEL RD\nAURORA PATH AND LIVERPOOL CAMPUS DRWY\n-\n\nM911/ WIRE HANGING LOW OVER WETZEL, NOT ARCING\nLIVERPOOL SCHOOLS CALLED, THESE ARE ELECTRICAL AND PHONE WIRES- ONE OF THEIR\nMOWERS HIT THE GUIDE WIRE TO THE POLE- WIRES ARE NOW APPRX 11 FEET OFF THE\nROADWAY\n\n\nE21,\nMOF
 Subject:BEAR RD AND MUENCH RD\n05/19/12\n21:34\nMOF123900546\nTCL\nF-MOTOR VEHICLE COLLISION-INJU\nBEAR RD AND MUENCH RD\nBEAR RD AND MUENCH RD\n-\nELDRED, GARY\nTWCBL ELDRED, GARY VOIP 05/19 21:33:30\n911- VEH VS MC\nMC DRIVER LAYING ON THE GROUND\nPATROL_CO_PARK_PRIMARY_BEAT or 1 PATROL_CO_PARK_BACKUP_BEAT or 1\nPATROL_ST_PARK_PRIMARY_BEAT or 1 PATROL_ST_PARK_BACKUP_BEAT); (1\nPATROL_VILLAGE_PRIMARY_BEAT or 1 PATROL_VILLAGE_BACKUP_BEAT or 1 PATROL_SUPV_\nVILLAGE_PRIMARY_BEAT or 1 PATROL_SUPV_ VILLAGE_BACKUP_BEAT); (1 PATROL_UPSTATE)\n\n\n\nMOF
 Subject:313 LONG BRANCH CIR\n05/19/12\n20:23\nMOF123900544\nTSL\nF-FIRE-RESIDENTIAL\n313 LONG BRANCH CIR\nLONG BRANCH RD AND LONG BRANCH RD\n-\n\n8X8 SHED\nPATROL_CO_PARK_PRIMARY_BEAT or 1 PATROL_CO_PARK_BACKUP_BEAT or 1\nPATROL_ST_PARK_PRIMARY_BEAT or 1 PATROL_ST_PARK_BACKUP_BEAT); (1\nPATROL_VILLAGE_PRIMARY_BEAT or 1 PATROL_VILLAGE_BACKUP_BEAT or 1 PATROL_SUPV_\nVILLAGE_PRIMARY_BEAT or 1 PATROL_SUPV_ VILLAGE_BACKUP_BEAT); (1 PATROL_UPSTATE)\n2-3 FEET FROM FENCH\n\nCFCTAC3\n\nBVF LVF MOF GNF
 Subject:7455 MORGAN RD\n05/18/12\n19:18\nMOF123900540\nTCL\nF-MOTOR VEHICLE COLLISION-INJU\n7455 MORGAN RD\nAVON PKWY AND STEELWAY BLVD N\nGOLDS GYM-\nDYER, SARAH\nN SECTOR -076.205828 043.128676 05/18 18:39:16\nM911 - CC - 3 VEHICLES // 95 HONDA CIVIC BLU VS WHI TOYT PK-UP VS SILVER DODGE\nSTRATUS\nDuplicate Event:Location = 7455 MORGAN RD TCL: @GOLDS GYM, Cross Street 1 = AVON\nPKWY STEELWAY BLVD N, Cross Street 2 = STEELWAY BLVD N, Type = MVC - MOTOR\nVEHICLE COLLISION, Subtype = DB - DAMAGE ONLY - J/O, Caller Name = CLOUSE,JACOB,\nCaller Ph Number = (315) 439-3844, Call Source = 9-911, Alarm Level = 0Location\nInfoIFO\n-076.205935 043.128644\nM911 CC IN A WHI TUNDRA TRUCK VS BLU HONDA VS SILV FORD\nEnd of Duplicate Event data\nAU\n05/18/12 18:43:38\nMORGAN RD TCL" to "7455 MORGAN RD TCL: @GOLDS GYM" at: 05/18/12 18:44:39\nANOTHER CALLER REPORTING THE SAME--VEHS ARE STILL IN THE ROADWAY-GREG CAMERON-NO\nCC 412-8039\nCB FROM COMP - STATES TROOPER IS OS - AND THE TROOPER REQ TO START AN AMBU FOR\nCALLER AND HER 1 WEEK OLD MALE\nCALLER - 28YOF - CONC - BREATHING - BACK PAIN\nCHILD - 1YOM - CONC - BREATHING - POSS WHIPLASH - IS ALERT\n\n\nBC2,E22,R3,\nMOF
 Subject:4979 W TAFT RD\n05/21/12\n07:24\nMOF123900549\nTCL\nF-ALARM-Fire\n4979 W TAFT RD\nCARRIAGE PKWY AND WINTERSWEET DR\nWEGMANS-\n\nCOUNTY FIRE/ SMOKE DETECTOR ACTIVATION 585-429-3030 OPER 78\nCALLED IN BY WEGMANS SECURITY- THEY ARE TRYING TO GET MORE INFO FROM THE STORE\n\n\nBC2,E11,E31,E32,E41,TK2,\nMOF
 Subject:29 CANDLELIGHT CIR\n05/20/12\n09:22\nMOF123900547\nTCL\nF-UNCONSCIOUS PERSON\n29 CANDLELIGHT CIR\nCANDLELIGHT LN\n-29 B\nMORGAN,DEIONDRE\nFLR 1 APT B WILLIAMS SHONTRAVIA RESD 05/20 09:11:24\n12YO SAYS HER MOM,JUST HAD SEIZURE\nNumber of patients: 1\nResponder script: 12 year old, Female, Unconscious, Breathing. Convulsions /\nSeizures. Caller Statement: 12YO SAYS HER MOM,JUST HAD SEIZURE.\nDispatch level suffix: E\nResponse text: Bravo\nResponder script: Effective breathing not verified < 35 (Epileptic or Previous\nhistory of seizures).\nCORRECTION, CALLER IS 12. HER MOM, THE PT IS 30YOA\nPOI\nANY UNIT FOR\nCALLER CANNOT DETECT BREATHING,  and  MOM WONT WAKE UP\nATTEMPTING PAI'S, HER MOM HAS PINK STUFF IN HER NOSE\n\n\n\nMOF
 Subject:BEAR RD AND MUENCH RD\n05/19/12\n21:34\nMOF123900546\nTCL\nF-MOTOR VEHICLE COLLISION-INJU\nBEAR RD AND MUENCH RD\nBEAR RD AND MUENCH RD\n-\nELDRED, GARY\nTWCBL ELDRED, GARY VOIP 05/19 21:33:30\n911- VEH VS MC\nMC DRIVER LAYING ON THE GROUND\nPATROL_CO_PARK_PRIMARY_BEAT or 1 PATROL_CO_PARK_BACKUP_BEAT or 1\nPATROL_ST_PARK_PRIMARY_BEAT or 1 PATROL_ST_PARK_BACKUP_BEAT); (1\nPATROL_VILLAGE_PRIMARY_BEAT or 1 PATROL_VILLAGE_BACKUP_BEAT or 1 PATROL_SUPV_\nVILLAGE_PRIMARY_BEAT or 1 PATROL_SUPV_ VILLAGE_BACKUP_BEAT); (1 PATROL_UPSTATE)\n\n\n\nMOF
 Subject:313 LONG BRANCH CIR\n05/19/12\n20:23\nMOF123900544\nTSL\nF-FIRE-RESIDENTIAL\n313 LONG BRANCH CIR\nLONG BRANCH RD AND LONG BRANCH RD\n-\n\n8X8 SHED\nPATROL_CO_PARK_PRIMARY_BEAT or 1 PATROL_CO_PARK_BACKUP_BEAT or 1\nPATROL_ST_PARK_PRIMARY_BEAT or 1 PATROL_ST_PARK_BACKUP_BEAT); (1\nPATROL_VILLAGE_PRIMARY_BEAT or 1 PATROL_VILLAGE_BACKUP_BEAT or 1 PATROL_SUPV_\nVILLAGE_PRIMARY_BEAT or 1 PATROL_SUPV_ VILLAGE_BACKUP_BEAT); (1 PATROL_UPSTATE)\n2-3 FEET FROM FENCH\n\nCFCTAC3\n\nBVF LVF MOF GNF
 Subject:7455 MORGAN RD\n05/18/12\n19:18\nMOF123900540\nTCL\nF-MOTOR VEHICLE COLLISION-INJU\n7455 MORGAN RD\nAVON PKWY AND STEELWAY BLVD N\nGOLDS GYM-\nDYER, SARAH\nN SECTOR -076.205828 043.128676 05/18 18:39:16\nM911 - CC - 3 VEHICLES // 95 HONDA CIVIC BLU VS WHI TOYT PK-UP VS SILVER DODGE\nSTRATUS\nDuplicate Event:Location = 7455 MORGAN RD TCL: @GOLDS GYM, Cross Street 1 = AVON\nPKWY STEELWAY BLVD N, Cross Street 2 = STEELWAY BLVD N, Type = MVC - MOTOR\nVEHICLE COLLISION, Subtype = DB - DAMAGE ONLY - J/O, Caller Name = CLOUSE,JACOB,\nCaller Ph Number = (315) 439-3844, Call Source = 9-911, Alarm Level = 0Location\nInfoIFO\n-076.205935 043.128644\nM911 CC IN A WHI TUNDRA TRUCK VS BLU HONDA VS SILV FORD\nEnd of Duplicate Event data\nAU\n05/18/12 18:43:38\nMORGAN RD TCL" to "7455 MORGAN RD TCL: @GOLDS GYM" at: 05/18/12 18:44:39\nANOTHER CALLER REPORTING THE SAME--VEHS ARE STILL IN THE ROADWAY-GREG CAMERON-NO\nCC 412-8039\nCB FROM COMP - STATES TROOPER IS OS - AND THE TROOPER REQ TO START AN AMBU FOR\nCALLER AND HER 1 WEEK OLD MALE\nCALLER - 28YOF - CONC - BREATHING - BACK PAIN\nCHILD - 1YOM - CONC - BREATHING - POSS WHIPLASH - IS ALERT\n\n\nBC2,E22,R3,\nMOF
 Subject:7595 ASTER DR\n05/22/12\n19:05\nMOF123900554\nTCL\nF-DIFFICULTY BREATHING\n7595 ASTER DR\nDAHLIA CIR AND IRIS LN\n-\nLANSETTE,DEAN\nLANCETTE-FIC, LYNETTE RESD 05/22 18:51:02\n911 CC DEAN -\nDIFB\nNumber of patients: 1\nResponder script: 65 year old, Male, Conscious, Breathing. Breathing Problems.\nCaller Statement: DIFB.\nResponse text: Delta\nResponder script: DIFFICULTY SPEAKING BETWEEN BREATHS.\nPOI\nReconfigured on another card: No\n89A1 -- STAT ALL SET -- REQ RESCUE FOR LIFT ASST\nNEED RESCUE FOR LIFTING\n\n\n\nMOF
 
 */
 
 
 public class NYOnondagaCountyBParser extends FieldProgramParser {
   
   private boolean skipDupEventInfo = false;
 
   public NYOnondagaCountyBParser() {
     super("ONONDAGA COUNTY", "NY",
            "DATE TIME ID SKIP CALL ADDR X PLACE NAME! INFO+");
   }
   
   @Override
   public String getFilter() {
     return "sta2adi@mcfd.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     skipDupEventInfo = false;
     return parseFields(body.split("\n"), 9, data);
   }
   
   private class MyPlaceField extends PlaceField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith("-")) field = field.substring(0,field.length()-1);
       else {
         int pt = field.indexOf('-');
         if (pt >= 0) {
           data.strApt = field.substring(pt+1).trim();
           field = field.substring(0,pt).trim();
         }
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE APT";
     }
   }
   
   private static Pattern REMOVE_INFO_PTN = Pattern.compile("\\bResponder script:|\\(Specific Diagnosis\\)\\.|\\bCaller Statement:|\\b\\d\\d(?:/\\d\\d){0,2} \\d\\d:\\d\\d:\\d\\d\\b");
   private static Pattern SKIP_INFO_PTN = Pattern.compile("|SPECIAL ADDRESS COMMENT:|ACCESS INFORMATION:|_+|\\[.*\\]|Reconfigured on another card:.*|.*\\bPATROL_.*|.*_BEAT\\b.*");
   private static Pattern PRIORITY_PTN = Pattern.compile("Response text: ([A-Z]).*");
   private static Pattern UNIT_PTN = Pattern.compile("([A-Z]+[0-9]+(?:,[A-Z]+[0-9]+)*),?");
   private static Pattern CHANNEL_PTN = Pattern.compile("[A-Z0-9]*TAC[A-Z0-9]*");
   private static Pattern SRC_PTN = Pattern.compile("[A-Z]{2}F(?: +[A-Z]{3})*");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       
       if (skipDupEventInfo) {
         if (field.startsWith("End of Duplicate")) skipDupEventInfo = false;
         return;
       }
       
       if (field.startsWith("Duplicate Event:")) {
         skipDupEventInfo = true;
         return;
       }
       
       
       field = REMOVE_INFO_PTN.matcher(field).replaceAll("").trim().replaceAll("  +", " ");
       field = field.replace("Number of patients:", "#PAT:");
       if (SKIP_INFO_PTN.matcher(field).matches()) return;
       Matcher match = PRIORITY_PTN.matcher(field);
       if (match.matches()) {
         data.strPriority = match.group(1);
         return;
       }
       match = CHANNEL_PTN.matcher(field);
       if (match.matches()) {
         data.strChannel = append(data.strChannel, ",", field);
         return;
       }
       match = UNIT_PTN.matcher(field);
       if (match.matches()) {
         data.strUnit = append(data.strUnit, ",", match.group(1));
         return;
       }
       match = SRC_PTN.matcher(field);
       if (match.matches()) {
         data.strSource = append(data.strSource, " ", field);
         return;
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "INFO PRI CH UNIT SRC";
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("PLACE")) return new MyPlaceField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
   
 }
 	
