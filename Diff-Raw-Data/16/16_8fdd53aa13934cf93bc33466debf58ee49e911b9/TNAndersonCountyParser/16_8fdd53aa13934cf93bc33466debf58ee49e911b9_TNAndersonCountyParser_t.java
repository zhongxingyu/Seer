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
 
  */
 
 public class TNAndersonCountyParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile(" - ?|- ");
   
   public TNAndersonCountyParser() {
     super(CITY_LIST, "ANDERSON COUNTY", "TN",
            "CALL+? ADDR/S! INFO");
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
    if (body.contains(" INSRV:")) return data.parseGeneralAlert(body);;
    
     return parseFields(DELIM.split(body), data);
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
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
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
