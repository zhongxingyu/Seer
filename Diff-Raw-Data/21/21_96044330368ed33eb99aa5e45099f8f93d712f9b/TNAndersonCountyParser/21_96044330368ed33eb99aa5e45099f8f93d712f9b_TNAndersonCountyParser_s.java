 package net.anei.cadpage.parsers.TN;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Anderson County, TN
 Contact: justin massengill <acs0680@gmail.com>
 Sender: page@tnacso.net
 
 (E911) Fire - Residential - 782 BRUSHY VALLEY RD ANDERSONVILLE, TN - REC:04:51 DISP:04:51 RESP:04:53 ONSC:05:09 INSRV:08:13 TRK#:178730
 (E911) Fire Alarm-Residential - 1534 MOUNTAIN RD ANDERSONVILLE, TN 
 (E911) Fire - Residential - 778 BRUSHY VALLEY RD ANDERSONVILLE, TN
 (E911) Fire - Other - ANDERSONVILLE HWY @ BETHEL ROAD CLINTON, TN - REC:14:25 DISP:14:25 RESP:14:25 ONSC:14:30 INSRV:14:30 TRK#:178572
 (E911) Fire - Other - ANDERSONVILLE HWY @ BETHEL ROAD CLINTON, TN
 (E911) MVA - Hazards - BETHEL RD CLINTON, TN -REC:03:15 DISP:03:17 RESP:03:19 ONSC:03:35 INSRV:04:03 TRK#:178399
 (E911) MVA - Hazards - BETHEL RD CLINTON, TN
 (E911) CVFD FIRST RESP - 3259 W WOLF VALLEY ROAD CLINTON, TN - FALL INJURIES
 (E911) RESC - I75 SB @ MM 119 CLINTON, TN - MVA
 (E911) MVA - Injury - I75 SB @ MM 119 CLINTON, TN - REC:02:41 DISP:02:41 RESP:02:45 ONSC: INSRV:02:51 TRK#:179132
 (E911) MARFVD FIRST RESP - 1508 LAUREL RD CLINTON, TN - DIFFICULTY BREATHING
 (E911) EMS Assist - 1508 LAUREL RD CLINTON, TN - REC:20:21 DISP:20:23 RESP:20:24 ONSC:20:31 INSRV:20:39 TRK#:179119
 (E911) Mutual Aid Request - 838 OLIVER SPRINGS HIGHWAY CLINTON, TN
 (E911) Fire - Other - ANDERSONVILLE HWY @ BETHEL ROAD CLINTON, TN
 (E911) Fire - Other - ANDERSONVILLE HWY @ BETHEL ROAD CLINTON, TN - REC:14:25 DISP:14:25 RESP:14:25 ONSC:14:30 INSRV:14:30 TRK#:178572
 
 Contact: Cousin Skeeter <skeeter1380@gmail.com>
 E911 / MARFVD - FROST BOTTOM ROAD  OLIVER SPRINGS, TN - MVA\n\n
 E911 / MARFVD FIRST RESP - 1180 DUTCH VALLEY ROAD  CLINTON, TN - DIFFICULTY  BREATHING\n\n
 E911 / Fire Alarm-Residential - 351 WOODLAND HILLS ROAD CLINTON, TN - REC:12:11 DISP:12:12 RESP:12:16 ONSC: INSRV:12:17 TRK#:188459\n\n
 
 Contact: Active911.com
 Sender: cad@k9track.k9track.com
 [E911] AVFD FIRST RESP - 130 HUMPHREY CEMETERY HEISKELL, TN - STROKE\n
 [E911] Service Call - Investigation - 414 NORTH DOGWOOD RD POWELL, TN\n
 [E911] Service Call - Investigation - 414 NORTH DOGWOOD RD POWELL, TN\n
 [E911] EMS Assist - 444 EAST WOLF VALLEY RD HEISKELL, TN - REC:19:17 DISP:19:18\nRESP:19:20 ONSC:19:28 INSRV:19:50 TRK#:211465\n
 [E911] Natural Disaster - 101 SOUTH MAIN ST., CLINTON, TN\n
 [E911] MVA - Injury - 101 SOUTH MAIN ST, CLINTON, TN\n
 [E911] Fire - Commercial - 402 BETHEL RD, OAK RIDGE, TN\n
 [E911] Fire - Commercial - 402 BETHEL RD, OAK RIDGE, TN\n
 [E911] Fire Alarm-Residential- 410 MEHAFFEY RD CLINTON, TN\n
 [Fwd: E911] Fire Alarm-Residential- 410 MEHAFFEY RD CLINTON TN\n
 [E911] Fire Alarm-Residential- 410 MEHAFFEY RD CLINTON TN\n
 [E911] Fire Alarm-Residential- 410 MEHAFFEY RD CLINTON TN\n
 [E911] Fire Alarm-Residential- 410 MEHAFFEY RD CLINTON TN\n
 [E911] Lift Assist - 100 MARLOW CIR, CLINTON, TN\n
 [E911] Airplane Crash - 123 WEST BROAD ST, CLINTON, TN\n
 [E911] Lift Assist - 100 MARLOW CIR, CLINTON, TN - REC:14:12 DISP:14:12 RESP: ONSC: INSRV:14:19 TRK#:211540\n
 [E911] Airplane Crash - 123 WEST BROAD ST, CLINTON, TN - REC:14:17 DISP:14:18 RESP: ONSC: INSRV:14:19 TRK#:211543\n
 [E911] Lift Assist - 617 PINE RIDGE RD, CLINTON, TN - REC:17:14 DISP:17:14 RESP: ONSC: INSRV:17:19 TRK#:212848
 [E911] EMS Assist - 236 PLEASANT VIEW LOOP , CLINTON, TN - REC:11:50 DISP:11:52 RESP: ONSC: INSRV:12:35 TRK#:212915
 [E911] RESC FIRST RESP - 575 OLD TACORA HILLS RD  CLINTON, TN - UNRESPONSIVE
 [E911] EMS Assist - 575 OLD TACORA HILLS RD , CLINTON, TN - REC:00:31 DISP:00:37 RESP: ONSC: INSRV:01:07 TRK#:212959
 [E911] RESC - I-75 NB 121-122 CLINTON, TN - MVA
 [E911] EMS Assist - I-75 NB 121-122, CLINTON, TN - REC:01:12 DISP:01:13 RESP:01:16 ONSC:01:26 INSRV:01:34 TRK#:212962
 [E911] RESC FIRST RESP - 137 BRYANT CIR LAKE CITY, TN - LOW 02 SATS
 [E911] RESC FIRST RESP - 522 PINE RIDGE RD CLINTON, TN - MENTAL STATUS CHANGE
 [E911] EMS Assist - 137 BRYANT CIR, LAKE CITY, TN - REC:14:45 DISP:14:52 RESP: ONSC: INSRV:15:25 TRK#:213056
 [E911] EMS Assist - 522 PINE RIDGE RD, CLINTON, TN - REC:15:01 DISP:15:07 RESP: ONSC: INSRV:15:28 TRK#:213062
 [E911] RESC FIRST RESP - 1215 DUTCH VALLEY RD CLINTON, TN - PAIN-CHEST
 [E911] EMS Assist - 1215 DUTCH VALLEY RD, CLINTON, TN - REC:15:21 DISP:15:24 RESP: ONSC: INSRV:15:54 TRK#:213071

 [E911] RESC - 3044 LAKE CITY HWY LAKE CITY, TN - MVA - 213938\n
 [E911] EMS Assist - 3044 LAKE CITY HWY, LAKE CITY, TN - REC:03:21 DISP:03:25 RESP:03:26 ONSC:03:30 INSRV:05:34 TRK#:213940\n
 [E911] RESC FIRST RESP - 410 ANDYS RIDGE RD LAKE CITY, TN - DIFFICULTY  BREATHING - 213951\n
 [E911] EMS Assist - 410 ANDYS RIDGE RD, LAKE CITY, TN - REC:07:32 DISP:07:38 RESP: ONSC: INSRV:09:23 TRK#:213951\n
 [E911] RESC - OLD EDGEMOOR LN @ S DOGWOOD RD CLINTON, TN - INJURY-LEG - 213960\n
 [E911] EMS Assist - OLD EDGEMOOR LN @ S DOGWOOD RD, CLINTON, TN - REC:10:03 DISP:10:07 RESP: ONSC: INSRV:10:18 TRK#:213962\n
 [E911] RESC - NORRIS FRWY @ ECHO VALLEY RD LAKE CITY, TN - MVA - 213988\n
 [E911] EMS Assist - NORRIS FRWY @ ECHO VALLEY RD, LAKE CITY, TN - REC:17:07 DISP:17:10 RESP: ONSC: INSRV:17:24 TRK#:213989\n
 [E911] RESC FIRST RESP - 103 OFFUTT RD CLINTON, TN - FALL INJURIES - 214008\n
 [E911] EMS Assist - 103 OFFUTT RD, CLINTON, TN - REC:23:57 DISP:00:00 RESP: ONSC: INSRV:00:09 TRK#:214008\n
 [E911] RESC - I 75 SB BETWEEN 129 AND 128 OAK RIDGE, TN - MVA - 214010\n
 [E911] EMS Assist - I 75 SB BETWEEN 129 AND 128, OAK RIDGE, TN - REC:01:09 DISP:01:13 RESP:01:17 ONSC: INSRV:01:26 TRK#:214012\n
 [E911] RESC - 2676 NORRIS FRWY ANDERSONVILLE, TN - MVA - 214031\n
 [E911] MVA - Injury - 2676 NORRIS FRWY, ANDERSONVILLE, TN - REC:16:06 DISP:16:07 RESP:16:09 ONSC: INSRV:17:33 TRK#:214033\n
 [E911] RESC - 1088 NEW CLEAR BRANCH RD  LAKE CITY, TN - MVA - 214037\n
 [E911] EMS Assist - 1088 NEW CLEAR BRANCH RD , LAKE CITY, TN - REC:19:47 DISP:19:49 RESP:19:51 ONSC:20:04 INSRV:20:22 TRK#:214038\n
 [E911] RESC FIRST RESP - 8100 YOUNT RD LOT 42  CLINTON, TN - PAIN-CHEST - 214048\n
 [E911] EMS Assist - 8100 YOUNT RD LOT 42 , CLINTON, TN - REC:23:22 DISP:23:24 RESP: ONSC: INSRV:00:01 TRK#:214048\n
 [E911] RESC FIRST RESP - 205 LOY LN  POWELL, TN - STROKE - 214064\n
 [E911] RESC FIRST RESP - 111 JM WHITE LN  CLINTON, TN - FALL INJURIES - 214067\n
 [E911] EMS Assist - 205 LOY LN , POWELL, TN - REC:06:06 DISP:06:07 RESP: ONSC: INSRV:06:32 TRK#:214064\n
 [E911] EMS Assist - 111 JM WHITE LN , CLINTON, TN - REC:06:16 DISP:06:18 RESP: ONSC: INSRV:07:11 TRK#:214067\n
 [E911] RESC FIRST RESP - 202 MILL LN CLINTON, TN - DIFFICULTY  BREATHING - 214074\n
 [E911] EMS Assist - 202 MILL LN, CLINTON, TN - REC:08:03 DISP:08:04 RESP: ONSC: INSRV:08:42 TRK#:214074\n
 [E911] RESC FIRST RESP - 221 SATELLITE RD POWELL, TN - DIFFICULTY  BREATHING - 214235\n
 [E911] RESC FIRST RESP - 107 WEST LN POWELL, TN - FALL INJURIES - 214238\n
 [E911] EMS Assist - 221 SATELLITE RD, POWELL, TN - REC:18:07 DISP:18:09 RESP: ONSC: INSRV:18:30 TRK#:214235\n
 [E911] EMS Assist - 107 WEST LN, POWELL, TN - REC:18:12 DISP:18:15 RESP: ONSC: INSRV:18:37 TRK#:214239\n
 [E911] RESC - NEW HENDERSON RD POWELL, TN - MVA - 214248\n
 [E911] EMS Assist - NEW HENDERSON RD, POWELL, TN - REC:20:11 DISP:20:14 RESP: ONSC: INSRV:21:04 TRK#:214250\n
 [E911] RESC FIRST RESP - 318 BRANCH LANE CLINTON, TN - UNRESPONSIVE - 214270\n
 [E911] EMS Assist - 318 BRANCH LANE, CLINTON, TN - REC:08:09 DISP:08:10 RESP: ONSC: INSRV:08:59 TRK#:214270\n
 [E911] RESC FIRST RESP - 1951 OLD LAKE CITY HWY CLINTON, TN - MEDICAL ALARM - 214308\n
 [E911] Lift Assist - 114 OAK HILL RD , CLINTON, TN\n
 [E911] EMS Assist - 1951 OLD LAKE CITY HWY, CLINTON, TN - REC:11:47 DISP:11:49 RESP: ONSC: INSRV:12:06 TRK#:214308\n
 [E911] RESC FIRST RESP - 822 PARK LANE ANDERSONVILLE, TN - SEIZURES - 214319\n
 [E911] RESC FIRST RESP - 443 LEINART LANE  CLINTON, TN - FALL INJURIES - 214324\n
 [E911] EMS Assist - 822 PARK LANE, ANDERSONVILLE, TN - REC:12:16 DISP:12:16 RESP: ONSC: INSRV:13:09 TRK#:214319\n
 [E911] EMS Assist - 443 LEINART LANE , CLINTON, TN - REC:13:04 DISP:13:05 RESP: ONSC: INSRV:13:19 TRK#:214324\n
 
  */
 
 public class TNAndersonCountyParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile(" - ?|- ");
   
   public TNAndersonCountyParser() {
     super(CITY_LIST, "ANDERSON COUNTY", "TN",
           "CALL+? ADDR/S! INFO ID");
   }
   
   @Override
   public String getFilter() {
     return "page@tnacso.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
 
     // Dummy loop
     do {
       if (subject.equals("E911")) break;
       if (subject.equals("Fwd: E911")) break;
       if (body.startsWith("E911 / ")) {
         body = body.substring(6).trim();
         break;
       }
       return false;
     } while (false);
     
     // If page contains times keywords, report as general alert
     if (body.contains(" INSRV:")) return data.parseRunReport(body);;
     
     return parseFields(DELIM.split(body), data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram() + " PLACE";
   }
   
   // Call field appends to previous call field with - separator
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       data.strCall = append(data.strCall, " - ", field);
     }
   }
 
   // Address field is identified by trailing , TN
   // ANd needs to replace @ with &
   private class MyAddressField extends AddressField {
     @Override
     public boolean checkParse(String field, Data data) {
       if (!field.endsWith(" TN")) return false;
       field = field.substring(0, field.length()-3).trim();
       if (field.endsWith(",")) field = field.substring(0, field.length()-1).trim();
       String city = null;
       int pt = field.lastIndexOf(',');
       if (pt >= 0) {
         city = field.substring(pt+1).trim();
         field = field.substring(0,pt).trim();
       }
       field = field.replace('@', '&');
       super.parse(field, data);
       if (city != null) data.strCity = city;
       return true;
     }
   }
   
   // The info field has to do all kinds of strange things
   private class MyInfoField extends InfoField {
     
     @Override
     public void parse(String field, Data data) {
       
       // This is really the call description
       // and what we thought was the call description is really the department
       if (field.length() != 0) {
         data.strSource = data.strCall;
         data.strCall = field;
       }
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ANDERSONVILLE",
     "BRICEVILLE",
     "CLAXTON",
     "CLINTON",
     "DEVONIA",
     "FORK MOUNTAIN",
     "FRATERVILLE",
     "HEISKELL",
     "LAKE CITY",
     "MARLOW",
     "NORRIS",
     "OAK RIDGE",
     "OLIVER SPRINGS",
     "POWELL",
     "ROSEDALE"
   };
 }
