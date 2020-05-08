 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Suffolk County, NY (Version B)
 
 *** 13 - Structure Fire *** 147 CHERUBINA LN CS: LEADER AVE  / SKIDMORE RD TOA: 22:37 09/22/10 OIL BURNER NORTH BABYLON FC 2010-002398 HY: 8' 11
 *** 13 - Structure Fire *** 514 MOUNT PL CS: ESSEX ST  / LAKEWAY DR TOA: 19:55 09/22/10 NORTH BABYLON FC 2010-002393 HY: 12' 533 MOUNT PL @ ESSE
 *** 2nd/16 - Rescue *** 733 HIGHRIDGE RD CS: OCONNER RD  / NARLAND LN TOA: 20:46 09/22/10 a/m pysch emer NORTH BABYLON FC 2010-002395
 *** 23 - Miscellaneous Fire *** SR CITZ APTS (5 BLDGS) COMPLEX 15 WEEKS RD CS: DEER PARK AVE  / MULHOLLAND DR TOA: 11:07 09/23/10 INVEST NORTH B
 *** 24/16 - Mutual Aid *** 27 COOLIDGE AVE CS: RT 110  / COOLIDGE AVE TOA: 06:54 10/20/10 40 Y/F ABDOMINAL PAINS  AMITYVILLE FD 2010-000228
 *** 24/16 - Mutual Aid *** 22 ELGIN RD CS: DE  / COOLIDGE AVE TOA: 11:38 10/18/10 E/F UNABLE TO MOVE  **FULL CREW NEEDED** AMITYVILLE FD 2010-00
 *** 24/MV - Mutual Aid ***  MONTAUK HWY CS: WILSON AVE TOA: 11:43 10/20/10 AMITYVILLE FD 2010-000229
 *** 24/13 - Mutual Aid *** 42 NATHALIE AVE CS: CAMPBELL ST  / MOORE ST TOA: 12:04 10/04/10 POSS OCCUPANTS WITH IN  AMITYVILLE FD 2010-000226
 *** 3/16 - Rescue *** 204 VAN BUREN ST CS: LEWIS AVE  / BELMONT AVE TOA: 10:04 10/29/10 E/F UNRESPONSIVE  **EMT NEEDED** NO
 *** 16 - Rescue *** 41 WILLIAMS AVE CS: CAMPBELL ST  / MOORE ST TOA: 18:01 10/25/10 E/F UNCONCIOUS-NOT BREATHING AMITYVILLE FD 2010-000231
 *** 16 - Rescue *** 100 PARK AVE CS: IRELAND PL  / CEDAR ST TOA: 05:28 10/30/10 E/M  AMITYVILLE FD 2010-000235
 *** 16 - Rescue *** 47 DIXON AVE CS: RT 110  / ALBANY AVE TOA: 20:28 10/27/10 77Y/O FEMALE CHOKING AMITYVILLE FD 2010-000233
 *** 24/16 - Mutual Aid *** CHURCH OF GOD 102 COOLIDGE AVE CS: ROSEWOODAVE  / STEELE PL TOA: 17:49 10/28/10 VOV AMITYVILLE FD 2010-000234
 *** 16 - Rescue *** 99 OVERTON ST CS: JEFFERSON AVE  / DEER PARK AVE TOA: 15:06 11/25/10 E/F SICK DEER PARK FIRE DISTRICT
 *** 23 - Miscellaneous Fire *** 14 BROOKLYN ST CS: CARLLS PA  / CAYUGA AVE TOA: 15:59 11/25/10 A/F/A  DEER PARK FIRE DISTRICT
 *** 2nd/16 - Rescue *** INVALID 185 W 7TH ST CS: PARK AVE  / CENTRAL AVE TOA: 10:35 12/13/10 80 YR FEM VOMITTING WEAK DEER PARK FIRE DISTRICT
 *** 16 - Rescue *** 189 W 10TH ST CS: PARK AVE  / CENTRAL AVE TOA: 07:53 12/13/10 A/F SYNCOPAL DEER PARK FIRE DISTRICT
 *** 16 - Rescue *** 111 LIBERTY ST CS: DEER PARK AVE  / PINE ACRES BLVD TOA: 20:06 12/12/10 82YR MALE CHEST PAIN DEER PARK FIRE DISTRICT
 ***23- Wires/Electrical Hazard*** THEATRE THREE* 412 MAIN ST PORT JEFFERSON CS: SPRING ST  / MAPLE PL TOA: 14:18 01/02/11 PT JEFFERSON 2011-000003 PJFD",
 *** 30 - RESCUE *** U.S.A. SKATING RINK 1276 HICKSVILLE RD CS: SUFFOLK AVE  / DOGWOOD LN A-2 TOA: 20:12 03/30/11 2011-000250 Hazmat
 *** 16 - Rescue *** 30 DEER SHORE SQ CS: DEER PARK AVE  / BAY SHORE RD TOA: 13:04 04/02/11 F/M STROKE DEER PARK FIRE
 *** 16/23-Rescue/Miscellaneous *** 1015 GRAND BLVD CS: E INDUSTRY CT  / CORBIN AVE TOA: 12:09 04/02/11 3 Y/O POSSINI
 *** 16 - Rescue *** 1015 GRAND BLVD CS: E INDUSTRY CT  / CORBIN AVE TOA: 12:09 04/02/11 3 Y/O POSSINING  DEER PARK F
 *** 16 - Rescue *** 162 OAKLAND AVE CS: W 6TH ST  / W 5TH ST TOA: 10:20 04/02/11 A/F INF FROM A FALL   DEER PARK FIR
 
 Contact: Odane Pur <mug930@gmail.com>
 Sender: paging@firerescuesystems.xohost.com
   / *** 23 - Miscellaneous Fire *** 18 SILVERPINE DR CS: ATNA DR  - PROSPECT ST W TOA: 12:10 07-17-11 2011-001109\n
 
 Contact: Sean Rudy <seanpolaris99@gmail.com>
 From: paging@firerescuesystems.xohost.com
 *** 2nd/16 - Rescue *** WENDY'S NBFC 1236 DEER PARK AVE CS: WOODS RD  - AUGUST RD TOA: 18:07 09-12-11 <07091211> a/f inj from fall NORTH BABYLON FC 2011-00 <201100>
 *** MV - Motor Vehicle Accident *** SS PY & DEER PARK AV Ext 39 SOUTHERN STATE PARKWAY CS: DEER PARK AVE TOA: 17:49 09-12-11 <49091211> EASTBOUND NORTH BAB
 *** 23 - Miscellaneous Fire *** 7 NON-AMBULATORY PTS ON 2ND FLOOR 15 BROOKSIDE AVE CS: O  - O DEER PARK AVE
 *** 2nd/16 - Rescue *** 1171 LITTLE EAST NECK RD CS: DELAWARE RD  - PHOENIX RD TOA: 09:29 09-12-11 <29091211> NORTH BABYLON FC 2011-002275 <2011002275>
 *** 16 - Rescue *** 101 ELKTON LN CS: MAYBROOK RD  - AUGUST RD TOA: 08:52 09-12-11 84 <5209121184> Y/F BODY PAINS  NORTH BABYLON FC 2011-002274<2011002274>
 
 Contact: Bernice Bien Aime <bbienaime@wwhac.com>
 Sender: paging@firerescuesystems.xohost.com
 *** 2nd/16 - Rescue *** 224 STATE AVE CS: MOUNT AVE  - LUND ST TOA: 18:48 09-17-11 E/F ABD PAIN WYANDANCH/WHEATLEY HTS 2011-001835
 
 Contact: Tyler Gibbs <moosespace91@gmail.com>
 Sender: scmproducts@optonline.net
 *** 16 ***  BROADWAY CS: PARK AVE ADTML: 31-D-3 TOA: 16:14 10-20-11 2011-004568 TYPE: UNCONSCIOUS / FAINTING (NEAR) I/V/O NURSERY E/M
 *** 16 *** 24 FOXHURST RD CS: DIX HILLS RD TOA: 16:16 10-29-11 2011-004718
 *** 16 *** 180 E PULASKI RD CS: ALBERMARLE ST ADTML: 10-C-1 TOA: 15:43 01-26-12 2012-000393 TYPE: CHEST PAIN LOC: 180 E PULASKI RD HU
 *** 16 ***  JONES LN CS: W JERICHO TRNP ADTML: 29-B-1 TOA: 14:04 01-26-12 2012-000392 TYPE: MOTOR VEHICLE ACCIDENT  IN PARKING LOT CR
 *** 16 *** 379 NEW YORK AV CS: ELM ST ADTML: 17-D-3 TOA: 13:28 01-26-12 2012-000391 TYPE: FALLS LOC: 379 NEW YORK AV HUNTIN :@FOX'S  
 *** 16 *** 257 E 17 CS: LENOX RD ADTML: 17-A-1 TOA: 10:16 02-05-12 2012-000535 TYPE: FALLS LOC: 257A E 17 ST HUNTIS   CROSS: LENOX RD
 *** 16 *** 2 MATHER CT CS: STRATTON DR ADTML: 6-C-1 TOA: 08:00 02-05-12 2012-000533 TYPE: RESPIRATORY LOC: 2 MATHER CT HUNTIS   CROSS
 
 Contact: Stupman1975 <stupman1975@gmail.com>
 Sender: masticambco@optonline.net
 ***16-26 Sick*** ***26-A-1*** 139 SHINNECOCK AV CS: BETTS PL / MASTIC RD TOA: 10:13 03/07/12 26-A-1 2012-000419 \n
 
 Contact: edepasquale@commackfd.org
 Sender: paging2@firerescuesystems.xohost.com
 ***23 CO Call No Symptoms*** 14 EMPIRE CT COMMACK CS: PIMLICO DR  - CUL DE SAC TOA: 06:18 03-07-12
 ***24 Mutual Aid R.I.T.***  ROBIN DR COMMACK CS: NEW HWY TOA: 20:25  
 ***23 Motor Vehicle Accident***  LEFFERTS PL CS: ELWOOD RD TOA: 17:58 03-07-12
 ***13 Residential Automatic Fire Alarm*** 34 MCARTHUR LN SMITHTOWN CS: LAUREL DR  - LEDGEWOOD DR TOA: 10:02 03-08-12
 
 Contact: "cfd153@aol.com" <cfd153@aol.com
 Sender: paging2@firerescuesystems.xohost.com
 ***23 Motor Vehicle Accident*** 1815 E JERICHO TRNP E NORTHPORT CS: E DEER PARK RD  - JERICHO TRNP TOA: 14:55 03-14-12
 ***23 Misc*** PEPPERTREE COMMONS 6401 JERICHO TPKE COMMACK TOA: 18:31 03-16-12
 
 Contact: Brandon Pangle <bpangle@andersoncountysc.org>
 Sender: paging2@firerescuesystems.xohost.com
 Sig 3 ALS Needed *** 16 ***  E 6 ST CS: FAIRGROUND AVE TOA: 11:53
 Sig 3 CL Needed *** 16 *** 160 WALT WHITMAN RD CS: PINETREE RD TOA: 11:16 04-01-12 2012-001368\r
 Sig 3 ALS Needed *** 16 *** 107 E 25 ST CS: POPLAR AVE TOA: 09:49 04-01-12 2012-001365\r
 Sig3 Full Crew Need *** 16 *** 160 WALT WHITMAN RD CS: PINETREE RD TOA: 11:16 04-01-12 2012-001368\r
 
 Contact: edepasquale@commackfd.org
 Sender: paging2@firerescuesystems.xohost.com
 ***23 Heavy Rescue***  SUNKEN MEADOW PKY CS: NORTHERN STATE PKY TOA: 23:23 04-01-12
 
 */
 
 public class NYSuffolkCountyBParser extends FieldProgramParser {
   
   public NYSuffolkCountyBParser() {
     super(CITY_LIST, "SUFFOLK COUNTY", "NY",
            "ADDR/SP! CS:X? ADTML:CODE? TOA:TIMEDATE TYPE:INFO LOC:SKIP");
   }
   
   @Override
   public String getFilter() {
     return "@firerescuesystems.xohost.com,scmproducts@optonline.net";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     int pt1 = body.indexOf("***");
     if (pt1 < 0) return false;
     int pt2 = body.indexOf("***",pt1+3); 
     if (pt2 < 0) return false;
     data.strCall = append(body.substring(pt1+3, pt2).trim(), " - ", body.substring(0,pt1));
     body = body.substring(pt2+3).trim();
     if (! super.parseMsg(body, data)) return false;
     if (data.strPlace.endsWith("*")) data.strPlace = data.strPlace.substring(0, data.strPlace.length()-1).trim();
     if (data.strAddress.length() == 0) {
       data.strAddress = data.strPlace;
       data.strPlace = "";
     }
     return true;
   }
   
   @Override
   public String getProgram() {
     return "CALL " + super.getProgram();
   }
 
   private static final Pattern TIME_DATE = Pattern.compile("^(\\d\\d:\\d\\d)(?: (\\d\\d[-/]\\d\\d[-/]\\d\\d))?\\b");
   private static final Pattern ANGLE_BKT_PTN = Pattern.compile("<[^<>]*>");
   private static final Pattern ID_PTN = Pattern.compile("\\b\\d{4}-\\d{6}\\b");
   private static final Pattern DISTRICT_PTN = Pattern.compile("\\b(?:NORTH BABYLON FC|AMITYVILLE FD|DEER PARK FIRE DISTRICT|PT JEFFERSON)\\b");
   private class MyTimeDateField extends InfoField {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = TIME_DATE.matcher(field);
       if (!match.find()) abort();
       data.strTime = match.group(1);
       data.strDate = getOptGroup(match.group(2));
       field = field.substring(match.end()).trim();
       field = ANGLE_BKT_PTN.matcher(field).replaceAll("");
      match = DISTRICT_PTN.matcher(field);
       if (match.find()) {
         data.strSource = match.group();
         field = field.substring(0,match.start()) + field.substring(match.end());
       }
       match = ID_PTN.matcher(field);
       if (match.find()) {
         data.strCallId = match.group();
         field = field.substring(0,match.start()) + field.substring(match.end());
       }
       field = field.trim().replaceAll("  +", " ");
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "TIME DATE SRC ID INFO";
     }
   }
   
   private static final Pattern CALL_DELIM_PTN = Pattern.compile("\\(|  ");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       if (data.strCall.length() <= 2) {
         Matcher match = CALL_DELIM_PTN.matcher(field);
         int pt = match.find() ? match.start() : field.length();
         data.strCall = append(data.strCall, " - ", field.substring(0,pt).trim());
         field = field.substring(pt).trim();
       }
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("TIMEDATE")) return new MyTimeDateField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "BELLE TERRE", 
     "COMMACK",
     "CORAM",
     "E NORTHPORT",
     "MOUNT SINAI", 
     "MILLER PLACE",
     "PORT JEFFERSON",
     "SMITHTOWN",
     "STONY BROOK" 
   };
 }
