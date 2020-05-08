 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.Comparator;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 DAPRO Systems http://www.daprosystems.com/
 
 Campbell County, VA 
 Contact: efhammermeister@yahoo.com,EFHAMMERMEISTER@vtext.com
 MAILBOX:SQ02 MVC/MOTOR VEHICLE CRASH/ACCIDE 002364 WATERLICK RD/LEESVILLE RD CFS# 2010-061119 REF TO A 3 CAR MVA. AT FIRST A REPORT OF NO PI. NO NEED FOR FIRE. VASI ADV THAT T
 MAILBOX:SQ02 (3)ACCIDENT 021400 TIMBERLAKE RD CFS# 2010-061105 2 CAR MVA. WITH ENTRAPMENT
 MAILBOX:SQ02 STABBING 005450 COLONIAL HWY EVI CFS# 2010-061047 STAGE IN THE AREA, DO NOT GO TO THE SCENE
 MAILBOX:SQ02 UNRESPONSIVE 000218 LAKE FOREST DR CFS# 2010-060825
 MAILBOX:SQ02 HANGUP 911 000381 HORIZON DR CFS# 2010-060777 FEMALE ADVISED SOMETHING ABOUT THE RESCUE SQ. LINE DISCONNECTED
 
 Franklin County, VA
 MAILBOX:S07 EMS-OTHER/DEFINE 18360 VIRGIL H GOODE HWY 124 RMT CFS# 2010-030542 CROSS: SHADY LN/BLACKWATER RIVER
 MAILBOX:S07 EMS-CHEST PAIN 413 WOODDALE DR RMT CFS# 2010-030355 CROSS: VIRGIL H GOODE HWY/DEAD END
 MAILBOX:S07 EMS-PATIENT FALLEN 3005 GREEN LEVEL RD RMT CFS# 2010-030541 CROSS: GRASSY HILL RD/LITTLE MOUNTAIN DR
 MAILBOX:S07 EMS-CARDIAC VIRGIL H GOODE HWY & LINK ST RMT CFS# 2010-030580
 MAILBOX:S07 EMS-HIGH BLOOD PRESSURE 1808 BETHLEHEM RD BML CFS# 2010-030643 CROSS: BETHANY RD/DILLONS MILL RD
 MAILBOX:C07 FIRE-ALARM COMMERCIAL 3325 GRASSY HILL RD RMT CFS# 2010-033866 CROSS: JAMESTOWN RD/CALLAWAY RD
 S: M:MAILBOX:S02 ACCIDENT-INJURY 1450 WIRTZ CFS# 2011-028395\n
 
 Page County, VA
 MAILBOX:RS3 PAIN 16 E MAIN ST LUR CFS# 2011-000782 CROSS: BROAD ST/TANNERY RD
 MAILBOX:RS4 CHEST PAINS 185 GRAY DR STA CFS# 2011-004572 CROSS: AYLOR GRUBBS AV
 MAILBOX:RS1 GENERAL ILLNESS 117 PULASKI AV SHE CFS# 2011-004542 CROSS: S SECOND ST/FRONT ST
 MAILBOX:CO24 COMMERCIAL ALARM 525 MIDDLEBURG RD STA CFS# 2011-005247 CROSS: US HWY BSN 340/GOODRICH RD
 
 Rockingham County, VA (class II)
 R40 EMS-CARDIAC CONDITION 1751 MAIN AVE HAR CFS# 2010-082726 CROSS: GARBERS CHURCH RD/S HIGH ST
 R40 EMS-MENTAL PROBLEM 445 N MAIN ST 44 HAR CFS# 2010-082451 CROSS: WOLFE ST/ROCK ST
 R40 EMS-ABDOMINAL PAIN 1737 MORELAND DR HAR CFS# 2010-083119 CROSS: PHEASANT RUN CIR/ASHFORD CT
 R40 EMS-CHEST PAIN 235 LAYMAN ST 101 HAR CFS# 2010-083046 CROSS: N MAIN ST/LONGVIEW DR
 R40 TRAFFIC CRASH 300 BOYERS RD BLK HAR CFS# 2010-082984 CROSS: MYSTIC WOODS LN/CULLISON CT
 R40 TRAFFIC CRASH RESERVOIR ST & CANTRELL AV HAR CFS# 2010-082327
 (Rescue 40) R40 ODOR INVESTIGATION IN STRUCTUR 290 WARREN SERVICE DR HAR CFS# 2010-091415 CROSS: BLUESTONE DR/DEAD END
 (Rescue 40) 100 BLK OF QUALITY STREET IN BWATER IS CLOSED REVIEW THE TRANSMITTED FAX FOR THE ROAD CLOSURE-ECC GROUP PAGED C90
 C30 GRASS FIRE SPOTSWOOD TRL & ROCKINGHAM PIKE ELK CFS# 2010-092361
 C30 EMS-CARDIAC CONDITION 1533 RABBIT DR ELK CFS# 2010-092834 CROSS: N EAST SIDE HWY/DEADEND
 C30 EMS-DIFFICULTY BREATHING 105 ELKMONT DR 2 ELK CFS# 2010-092623 CROSS: S EASTSIDE HWY/DEAD END
 C30 POSSIBLE STRUCTURE FIRE 244 QUAIL CT MCG CFS# 2010-092692 CROSS: ASHBY RD/BETHEL LN
 C30 EMS-TRAUMA INJURIES E SPOTSWOOD AVE & MORGAN AVE ELK CFS# 2010-094660
 C30 EMS-DIFFICULTY BREATHING 3240 THOROUGHFARE RD ELK CFS# 2010-094548 CROSS: WHISPERING WINDS TRL/EPPARD LN
 C30 EMS-DIFFICULTY BREATHING 320 E ROCKINGHAM ST ELK CFS# 2010-094840 CROSS: JACKSON AVE/PAGE ST
 R35 EMS-ILLNESS 516 W SPOTSWOOD TRL ELK CFS# 2011-018309 CROSS: SHENANDOAH AVE/2ND ST
 
 */
 
 public class DispatchDAPROParser extends FieldProgramParser {
   
   public DispatchDAPROParser(Properties cityCodeTable, String defCity, String defState) {
     super(cityCodeTable, defCity, defState,
            "ADDR/SC! CFS:ID! CROSS:X");
     buildCallDictionary();
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     int pt = body.indexOf(" CFS# ");
     if (pt < 0) return false;
     pt += 4;
     body = body.substring(0,pt) + ':' + body.substring(pt+1);
     if (body.startsWith("MAILBOX:")) body = body.substring(8).trim();
     
     if (! super.parseMsg(body, data)) return false;
     if (data.strAddress.length() == 0) return false;
     return true;
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       
       // First token is always the source
       Parser p = new Parser(field);
       data.strSource = p.get(' ');
       field = p.get();
       
       // See if we can identify a call description from our canned list
       String callDesc = getCallDesc(field);
       if (callDesc != null) {
         
         // We got one, the call description comes off the front
         data.strCall = callDesc;
         field = field.substring(callDesc.length()).trim();
         
         // And everything else is an address
         parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END, field, data);
       } 
       
       // No call description match eh
       // We'll have to rely on the smart parser to save us
       else {
        parseAddress(StartType.START_CALL, FLAG_ANCHOR_END, field, data);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "SRC CALL ADDR CITY";
     }
   }
   
   @Override 
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   // This is a tree set containing all of the expected call descriptions
   // sorted in reverse order because we need to search the tree backward and
   // Android implementation of TreeSet lacks features that make that easy
   private static TreeSet<String> callDictionary = new TreeSet<String>(new Comparator<String>(){
     @Override
     public int compare(String str1, String str2) {
       return -str1.compareTo(str2);
     }});
   
   /**
    * Build call description dictionary
    */
   private void buildCallDictionary() {
     
     // Loop through all call descriptions
     for (String call : new String[]{
         "EMS-OTHER",
         "EMS-OTHER/DEFINE",
         "EMS-ABDOMINAL PAIN",
         "EMS-ALLERGIC REACTION",
         "EMS-ALS ASSIST",
         "EMS-ALTERED LOC",
         "EMS-AMPUTATION",
         "EMS-ASSAULT; INJURIES FROM",
         "EMS-AVULSION",
         "EMS-BACK PAIN",
         "EMS-ANIMAL BITE",
         "EMS-BURNS",
         "EMS-CARDIAC",
         "EMS-CARDIAC CONDITION",
         "EMS-CHEST PAIN",
         "EMS-CHOKING",
         "EMS-CO INHALATION",
         "EMS-CODE BLUE",
         "EMS-DIABETIC EMERGENCY",
         "EMS-DIFFICULTY BREATHING",
         "EMS-PERSON DOWN",
         "EMS-ELECTROCUTION",
         "EMS-EYE PROBLEM",
         "EMS-FALLS",
         "EMS-GUNSHOT",
         "EMS-HEADACHE",
         "EMS-HEAT/COLD EXPOSURE",
         "EMS-HEMORRHAGING",
         "EMS-HIGH BLOOD PRESSURE",
         "EMS-ILLNESS",
         "EMS-LACERATION",
         "EMS-MENTAL PROBLEM",
         "EMS-OB RELATED EVENT",
         "EMS-OVERDOSE/POISONING",
         "EMS-PATIENT FALLEN",
         "EMS-SEIZURES",
         "EMS-STAB WOUND",
         "EMS-STROKE",
         "EMS-TRAUMA INJURIES",
         "EMS-UNCONSCIOUS",
         "EMS-UNKNOWN MADICAL",
         "911 OPEN LINE",
         "ACCIDENT-INJURY",
         "AUTOMATIC FIRE ALARM",
         "AIRCRAFT INCIDENT",
         "MEDICAL ALARM",
         "ALARM SOUNDING",
         "ATTEMPTED SUICIDE",
         "BOMB THREAT",
         "BRUSH FIRE",
         "BRUSH FIRE THREATENING A STRUC",
         "CHIMNEY FIRE",
         "CO DETECTOR ACTIVATION",
         "POSSIBLE CO EVENT",
         "STRUCTURE COLLAPSE",
         "CONSTRUCTION ACCIDENT",
         "CONTROLLED BURN",
         "TRAFFIC CRASH",
         "TRAFFIC CRASH INVOLVING A BUS",
         "TRAFFIC CRASH W/PEDESTRIAN",
         "TRAFFIC CRASH INVOLVING A STRU",
         "DUMPSTER FIRE",
         "ELEVATOR ENTRAPMENT",
         "EXPLOSION",
         "FARM ACCIDENT",
         "FILL QUARTERS",
         "FORREST FIRE",
         "GRASS FIRE",
         "GRASS FIRE THREATENING A STRUC",
         "HAZMAT INCIDENT",
         "HAZMAT LEVEL 3 INCIDENT",
         "HOSTAGE/BARRICADE",
         "INDUSTRIAL ACCIDENT",
         "LANDING ZONE",
         "LINES DOWN",
         "LOCK OUT SERVICE",
         "MENTAL CASE",
         "MULCH FIRE",
         "MULCH FIRE THREATENING A STRUC",
         "ODOR INVESTIGATION",
         "ODOR INVESTIGATION IN STRUCTUR",
         "OFFICER INVOLVED SHOOTING",
         "SUSPICIOUS PACKAGE",
         "PUBLIC SERVICE CALL",
         "RAPE",
         "REKINDLE",
         "GROUND RESCUE",
         "GROUND SEARCH",
         "SMOKE INVESTIGATION",
         "LARGE SPILL",
         "SMALL SPILL",
         "STAND-BY",
         "POSSIBLE STRUCTURE FIRE",
         "STRUCTURE FIRE",
         "SUICIDE",
         "TECHNICAL RESCUE",
         "TRAIN CRASH",
         "TRAIN FIRE",
         "TRANSFORMER FIRE",
         "TRASH FIRE",
         "TREE FIRE",
         "TREE FIRE THREATENING A STRUCTUR",
         "UNAUTHORIZED BURN",
         "UNKNOWN SITUATION",
         "UNKNOWN FIRE",
         "VEHICLE FIRE",
         "VEHICLE FIRE THREATENING A STRUCTURE",
         "WATER EVENT",
         "WATER REMOVAL"
     }) callDictionary.add(call + " ");
   }
   
   private String getCallDesc(String body) {
     
     // Search the call dictionary sorted set for the highest entry less than or
     // equal to message body.  If the body starts with this string, we have a
     // match.  If not, we have to keep searching backward through the sorted set
     // for the entry less than or equal to the message body
     
     // We reversed the tree order so we can accomplish this trick without
     // needing a backward read feature, with Android seems to be lacking
     String firstWord = new Parser(body).get(' ');
     SortedSet<String> tail =  callDictionary.tailSet(body);
     for (String call : tail) {
       if (body.startsWith(call)) return call.trim();
       if (!call.startsWith(firstWord)) return null;
     }
     return null;
   }
 }
