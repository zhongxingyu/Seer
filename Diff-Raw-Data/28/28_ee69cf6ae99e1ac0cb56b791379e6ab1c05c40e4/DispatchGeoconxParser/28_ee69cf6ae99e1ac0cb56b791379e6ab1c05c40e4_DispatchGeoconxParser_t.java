 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Colbert County, AL
 MISSING PERSON\n 2564150099\n3804 HATCH BLVD, SHEFFIELD
 SUICIDAL PERSON\n 6622123327\n105 FORREST DR, CHEROKEE\n43 YOF
 BREATHING PROBLEM\nCHEROKEE FAMILY CLINIC 2563594516\n1080 2ND ST, CHEROKEE\n62 YOF
 SEIZURE\n 2568566665\n8685 HIGHWAY 72, CHEROKEE\n21F
 MISSING PERSON\nATKISSON, TAMARA 2564123685\n603 PASADENA AV, A, MUSCLE SHOALS  (also includes Apartment #)
 MVC-Injury\n 2564125319\nWOODWARD AV && 6TH ST, MUSCLE SHOALS\n2 CARS
 [E911] MEETING\n \nCHEROKEE FIRE DEPT\n6:30 IMPORTANT MEETING,\n
 [E911] WEATHER\n \nWEATHER\nFLASH FLOODING TILL 5:00\n
 [E911] TRAINING\n \nSPR AT 6 PM AT STATION 1\n
 [E911] MVC WITHOUT INJURY\nJJS\n
 [E911] PATIENT TRANSFER\nHELEN KELLER HOSPITAL 2563811766\n1300 S MONTGOMERY AV, SHEFFIELD\n315 EULA\n
 [E911] STROKE\nHOUSTON BOLTON 2563596427\n5280 OLD LEE HWY, CHEROKEE\n83M\n
 [E911] PASSED OUT\n 2565772031\n290 FORREST DR, CHEROKEE\n25 YOM\n
 [E911] OVERDOSE\nADAY, SHANE 2563602442\n145 ADAY DR, CHEROKEE\n15 YOM\n
 [E911] FIRE - UNKNOWN\nNELIA STAFFORD 2563594739\n445 NORTH PKE, CHEROKEE\nACROSS THE STREET FROM CALLER\n
 [E911] MVC / C INJURIES\n 2564128754\n1080 NORTH PKE, CHEROKEE\n
 [E911] CHEST PAIN\nMONTEZ DAVIS 2563594174\n190 JOHNSON DR, CHEROKEE\n68F\n
 [E911] PASSED OUT\n 2565779317\n1350 OLD COVER BRIDGE RD W, CHEROKEE\n40M\n
 [E911] VOMITING\nNELLAREE W DODSON 2563596248\n800 CROSS ST, CHEROKEE\n83/F\n
 [E911] FIRE - GRASS/WOODS\n \nMT HESTER RD MILE S OF 72 B4 CEDAR LN\n
 [E911] FALL\nN MCGEE 2563594003\n765 CROSS ST, CHEROKEE\n60/M\n
 [E911] HEART PROBLEM\n 2564129804\n125 SPRING VALLEY LN, CHEROKEE\n66 YOM\n
 [E911] BREATHING PROBLEM\n 2567023439\n7455 ALLSBORO RD, CHEROKEE\n54/F\n
 [E911] DRILL\nSHOALS FLIGHT CENTER 2563812869\n1729 T ED CAMPBELL DR, MUSCLE SHOALS\nA4\n
 [E911] DISTURBANCE\nALTHEA LONG 2563814676\n911 SE 18TH ST, SHEFFIELD\n
 [E911] LOST PERSON\n FREEDOM HILLS RD\n
 [E911] NOSE BLEED\nGAIL STEVENSON 890 DOBBS ST, CHEROKEE\n60 YOF\n
 [E911] MVC /C UNKNOWN INJ\n HIGHWAY 72 @ MM 5, CHEROKEE\n
 [E911] MISSING PERSON\n NATCHEZ TRACE PKWY @ MP 319\n
 [E911] HEMORRHAGE/LACERATION\nADAY, SHANE 145 ADAY DR\n49M\n
 [E911] BREATHING PROBLEM\nPARADISE FOODMART AND PACKAGE NO 7060 HIGHWAY 72, CHEROKEE\n55 YOM\n
 [E911] AMPUTATION\nADAY, SHANE 145 ADAY DR, CHEROKEE\n
 [E911] HEMORRHAGE/LACERATION\nMRS MILDRED PUGH 2480 WHITE PKE, CHEROKEE\n67M\n
 [E911] AMPUTATION\nADAY, SHANE 145 ADAY DR, CHEROKEE
 
 Union County, TN
 Subject:E911\nACCIDENT\n8654568681\nHIGHWAY 61 W, ANDERSONVILLE
 Subject:E911\nFIRE GENERAL\nBRIAN 8653232387\nMONROE RD, MAYNARDVILLE\nBRUSH FIRE
 Subject:E911\nFIRE GENERAL\nDAVID FRANKS 8652560768\n372 RACCOON VALLEY RD, Maynardville\nSMOKE IN THE AREA
 Subject:E911\nACCIDENT W/INJURY\nRAYMOND 8652565414\nOLD TAZEWELL PIKE, Corryton
 Subject:E911\nACCIDENT W/INJURY\nANGIE GATTO 8653605936\nMAYNARDVILLE HWY, MAYNARDVILLE\n4 PEOPLE
 Subject:E911\nFIRE RESIDENCE\nWILLIAM KRONER 8659925951\n1159 SATTERFIELD RD, MAYNARDVILLE
 Subject:E911\nFIRE RESIDENCE\nDAIL CAUGHORN 8652546959\n256 LUCAS LN, Maynardville
 Subject:E911\nTREE IN ROADWAY\nEDDIE SHOFFNER 8653141048\nHINDS CREEK RD, ANDERSONVILLE\nBLOCKING ROAD
 Subject:E911\nFIRE GENERAL\nSHIRLEY TURNER 4234891893\n116 MAYNARDVILLE HWY, MAYNARDVILLE\nADVISED THE POP MACHINE IS ON FIRE
 Subject:E911\nTREE IN ROADWAY\nSHIRLEY HAMMOCK 9116953536\nOAKLAND RD, Maynardville\nROADWAY IS BLOCKED
 Subject:E911\nTREE IN ROADWAY\nJENNIFER HARMON 8656807375\nBEARD VALLEY RD, MAYNARDVILLE\nTREE BLOCKING ROADWAY DANGEROUS AREA
 Subject:E911\nACCIDENT\nELIZABETH SAVAGE 8653854992\nHIGHWAY 61 W, Maynardville
 Subject:E911\nTREE IN ROADWAY\nMINDY HAWK 8652283331\nBUTCHER HOLLOW RD, MAYNARDVILLE\nTREE IN ROAD
 Subject:E911\nTREE IN ROADWAY\nWADE BRANTLEY 8658064337\nOUSLEY GAP RD, MAYNARDVILLE\nTOTAL ROAD BLOCKAGE
 Subject:E911\nOTHER\nMILLIE HARTGROVE 8659928378\n1983 HICKORY VALLEY RD, MAYNARDVILLE\nLIGHTENING STRUCK HOUSE
 Subject:E911\nSTORM OR WEATHER RELATED INCIDENTS/DAMAGE\nDAVID SEXTON 8655666873\nHIGHWAY 61 E, MAYNARDVILLE\nPOWER LINES IN ROAD WAY
 Subject:E911\nTREE IN ROADWAY\n\nOAKLAND RD, MAYNARDVILLE\nTREE BLOCKING ROAD
 Subject:E911\nTREE IN ROADWAY\nBRENDA MERRITT \nGUYTON DR, MAYNARDVILLE
 Subject:E911\nTREE IN ROADWAY\nDONNA EKERLY 8658515782\nJERRY HOLLOW RD, ANDERSONVILLE
 Subject:E911\nOTHER\nMARGARET TRENT 8653604629\n311 DOGWOOD LN, MAYNARDVILLE\nPOWER LINES DOWN
 Subject:E911\nFIRE BUSINESS\nUNION U COUNTY PROPER 4232110400\n901 MAIN ST, STE 115, MAYNARDVILLE\nHIGH VOLTAGE CLOSET
 Subject:E911\nTREE IN ROADWAY\n627 8653569465\nCHESTNUT RIDGE RD, MAYNARDVILLE\nBLOCKING THE ENTIRE ROAD
 Subject:E911\nACCIDENT W/INJURY\nEDDIE'S AUTO & SALVAGE 8659928151\n565 MAYNARDVILLE HWY, MAYNARDVILLE
 Subject:E911\nTREE IN ROADWAY\nJEREMY 8659926262\n466 HANSARD RD, MAYNARDVILLE
 Subject:E911\nTREE IN ROADWAY\nMICHAEL WITH PVFD 8657056703\nHANKINS HOLLOW RD, MAYNARDVILLE\nALREADY ONSCENE DO NOT TONE OUT JUST BUILD C
 Subject:E911\nACCIDENT W/INJURY\nKELLY HOLT 8656610782\n248 SATTERFIELD RD, Maynardville\n2 VEHICLE MVA
 Subject:E911\nCHEST PAIN\nTANASI GIRL SCOUT CAMP 8654947470\n123 DARK HOLLOW RD NORTH, ANDERSONVILLE
 Subject:E911\nBREATHING DIFFICULTY\nCHAD 8657409487\n1115 BEARD VALLEY RD, LOT-6, MAYNARDVILLE\n14 MONTH OLD FEMALE, LOSING BREATH
 Subject:E911\nFIRE GENERAL\nJANET CABBAGE 4232110397\n830 SATTERFIELD RD, MAYNARDVILLE\nLOG CABIN
 Subject:E911\nFIRE GENERAL\nROSCOE SANDS 8659920605\n1527 MAIN ST, MAYNARDVILLE
 S:E911 M:TREE IN ROADWAY\nSHARON COLLINS 8653567818\n143 RACCOON VALLEY RD, MAYNARDVILLE\n\n
 
 
 */
 public class DispatchGeoconxParser extends FieldProgramParser {
   
   private Set<String> citySet;
   private boolean noSubject; 
   
   public DispatchGeoconxParser(String defCity, String defState) {
     this(null, defCity, defState, false);
   }
   
   public DispatchGeoconxParser(Set<String> citySet, String defCity, String defState,
                            boolean noSubject) {
     super(defCity, defState,
           "CALL ( ADDR/Z END | NAMEPH? ADDR INFO+ )");
   }
   
   @Override
   public String getProgram() {
     return "CALL PHONE NAME PLACE ADDR CITY INFO";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     String flds[] = body.split("\n");
     if (!subject.equals("E911")) {
       if (noSubject || flds.length < 3) return false;
     }
     if (!parseFields(flds, data)) return false;
     
     // SOmetimes, what we interpreted as a name should be a place
     if (checkPlace(data.strName)) {
       data.strPlace = data.strName;
       data.strName = "";
     }
     return true;
   }
 
   private static final Pattern PHONE_PTN = Pattern.compile("\\b\\d{10}$");
   private static final Pattern DIGIT_PTN = Pattern.compile("\\b\\d{1,7}\\b");
   private class NamePhoneField extends Field {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       
       // A comma followed by a legitimate name indicates this is really an
       // address field, which we should not recognize
       int pt = field.lastIndexOf(',');
       if (pt >= 0) {
         String sCity = field.substring(pt+1).trim().toUpperCase();
         if (citySet != null && citySet.contains(sCity)) return false;
       }
       
       // Ditto if it contains an @
       if (field.indexOf('@') >= 0) return false;
       
       // Or a number with fewer than 8 digits
       // And does not end with a 10 digit phone number
       if (DIGIT_PTN.matcher(field).find()) {
         if (!PHONE_PTN.matcher(field).find()) return false;
       }
       
       parse(field, data);
       return true;
     }
     
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = PHONE_PTN.matcher(field);
       if (match.find()) {
         data.strPhone = match.group();
         field = field.substring(0,match.start()).trim();
       }
       data.strName = field;
     }
     
     @Override
     public String getFieldNames() {
       return "NAME PHONE";
     }
   }
   
   private static final Pattern CITY_PTN = Pattern.compile(", *([A-Z]+ ?[A-Z]*)$", Pattern.CASE_INSENSITIVE);
   private static final Pattern APT_PTN = Pattern.compile(", *((?:\\w+ *)?[-\\w]+)$");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       field = field.replace("&&", "&");
       
       Matcher match = CITY_PTN.matcher(field);
       if (match.find()) {
         data.strCity = match.group(1).trim(); 
         field = field.substring(0,match.start()).trim();
       }
       
       match = APT_PTN.matcher(field);
       if (match.find()) {
         data.strApt = match.group(1);
         field = field.substring(0,match.start()).trim();
       }
       
       String tail = "";
       int pt = field.indexOf('@');
       if (pt >= 0) {
         tail = field.substring(pt);
         field = field.substring(0,pt).trim();
       }
       
       StartType st = (data.strName.length() > 0 ? StartType.START_ADDR : StartType.START_PLACE);
       parseAddress(st, FLAG_ANCHOR_END, field, data);
       if (st == StartType.START_PLACE){ 
         if (data.strAddress.length() == 0) {
           data.strAddress = data.strPlace;
         } else {
           data.strName = data.strPlace;
         }
         data.strPlace = "";
       }
       data.strAddress = append(data.strAddress, " ", tail);
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR CITY";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("NAMEPH")) return new NamePhoneField();
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
