 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
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
 
 
 */
 
 public class NYSuffolkCountyBParser extends FieldProgramParser {
   
   private static final String[] CITY_LIST = new String[]{
     "PORT JEFFERSON", "BELLE TERRE", "MOUNT SINAI", "STONY BROOK", "MILLER PLACE", "CORAM"
   };
   
   public NYSuffolkCountyBParser() {
     super(CITY_LIST, "SUFFOLK COUNTY", "NY",
            "ADDR/SP! CS:X! ADTML:CODE? TOA:TOA TYPE:INFO");
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (!body.startsWith("***")) return false;
     int pta = body.indexOf("***",3); 
     if (pta < 0) return false;
     data.strCall = body.substring(3, pta).trim();
     body = body.substring(pta+3).trim();
     if (! super.parseMsg(body, data)) return false;
     if (data.strPlace.endsWith("*")) data.strPlace = data.strPlace.substring(0, data.strPlace.length()-1).trim();
    if (data.strAddress.length() == 0) {
      data.strAddress = data.strPlace;
      data.strPlace = "";
    }
     return true;
   }
 
   private static final Pattern TIME_DATE = Pattern.compile("^\\d\\d:\\d\\d \\d\\d[-/]\\d\\d[-/]\\d\\d ");
   private static final Pattern ANGLE_BKT_PTN = Pattern.compile("<[^<>]*>");
   private static final Pattern ID_PTN = Pattern.compile("\\b\\d{4}-\\d{6}\\b");
   private static final Pattern DISTRICT_PTN = Pattern.compile("\\b(?:NORTH BABYLON FC|AMITYVILLE FD|DEER PARK FIRE DISTRICT|PT JEFFERSON)\\b");
   private class MyToaField extends InfoField {
     
     @Override
     public void parse(String field, Data data) {
       Matcher match = TIME_DATE.matcher(field);
       if (match.find()) field = field.substring(match.end()).trim();
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
       return "SRC ID INFO";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("TOA")) return new MyToaField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "CALL " + super.getProgram();
   }
 }
