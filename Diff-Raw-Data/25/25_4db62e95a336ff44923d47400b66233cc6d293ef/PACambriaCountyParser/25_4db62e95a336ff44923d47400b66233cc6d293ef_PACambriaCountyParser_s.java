 package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Cambria County, PA
 Contact: jacob berkey <jwb932@gmail.com>
 Sender: alerts@cambria.ealertgov.com
 
 [29] 11 Time: 15:02:29 Nature: 17A01G-Alpha FALL Location: 216 MAIN ST-JO | Sta 35
 [29] 11 Time: 11:54:47 Nature: 29B01-Bravo VEH ACC WITH INJURIES Location: 110 PLAZA DR-LY | Sta 30, Sta 23, Sta 35
 [29] 11 Time: 08:03:01 Nature: 10C02-Charlie CHEST PAIN Location: 315 LOCUST ST-JO | Sta 35 
 [29] 11 Time: 06:45:29 Nature: 17B03G-Bravo FALL Location: WALNUT ST-JO/MAIN ST-JO | Sta 35
 [28] 11 Time: 21:39:31 Nature: 06D03A-Delta BREATHING PROBLEMS Location: 205 CHANDLER AVE-JO | Sta 35, Sta 21, Sta 36
 
 Contact: Ben Smith <cnfederatediesel@gmail.com>
 Contact: Paul Zabinsky <pzabinsky@gmail.com>
 Date: 06/02/11\nTime: 20:47:52\nNature: 10C01-Charlie CHEST PAIN\nLocation: 1518 W 2ND ST-CR\n| Sta 75
 Date: 06/04/11\nTime: 11:03:04\nNature: 06D02-Delta  BREATHING PROBLEMS\nLocation: 908 SHORT AVE-CR\n| Sta 75
 Date: 06/04/11\nTime: 17:24:03\nNature: STB-STAND BY\nLocation: 721 MAIN ST-PORB\n| Sta 73
 Date: 06/05/11\nTime: 18:00:42\nNature: 52C04G-Charlie FIRE ALARM\nLocation: 111 ASHCROFT AVE-CB\n| Sta 70
 Date: 06/13/11\nTime: 15:53:48\nNature: 02D02-Delta  ALLERGIC REACTION\nLocation: 1237 ST AUGUSTINE RD-CF\n| Sta 60, Sta 67
 
 Contact: Paul Bomboy <training@easthillsems.com>
 Sender: Cambria 9-1-1
 FRM:Cambria 9-1-1\nMSG:Date: 11/07/11\nTime: 18:02:55\nNature: 10C01-Charlie CHEST PAIN\nLocation: 349 VO TECH DR-RI\n|
 
 Contact: support@active911.com
 Sender; "Cambria 9-1-1" <alerts@cambria.ealertgov.com>
 Date: 01/24/12\nTime: 18:02:50\nNature: 69D03-Delta STRUCTURE FIRE\nLocation: 7458 ADMIRAL PEARY HWY-CB\nSta 70, Sta 71, Sta 72, Sta 75
 
Cambria County, PA (alternate)
 Contact: Brian Flora <USMARINE_2002@yahoo.com>
 Sender: alerts@cambria.ealertgov.com
 DATE: 06/13/12\r\n | 13:21:53\r\n | 25B06V-BRAVO PSYCHIATRIC PROBLEM\r\n | 1109 DEVEAUX ST-WC\r\n | NUMBER 1 RD-WC FRAZIER ST-WC\r\n | Sta 64
 DATE: 06/13/12\r\n | 13:08:24\r\n | 17A01G-ALPHA FALL\r\n | 180 WOOD AVE-CM\r\n | BELAIR ST-CM EDSEL ST\r\n | Sta 48
 DATE: 06/12/12\r\n | 20:34:38\r\n | SS-EMS STATION STILL\r\n | 100 N CAROLINE ST-EB\r\n | E HIGH ST-EB\r\n | Sta 48
 DATE: 06/12/12\r\n | 18:55:55\r\n | 26A01-ALPHA  SICK PERSON\r\n | 700 N CENTER ST-EB\r\n | BOLTON ST-EB W HIGHLAND AVE-EB\r\n | Sta 48
 DATE: 06/12/12\r\n | 18:55:55\r\n | 26A01-ALPHA  SICK PERSON\r\n | 700 N CENTER ST-EB\r\n | BOLTON ST-EB W HIGHLAND AVE-EB\r\n | Sta 48
 DATE: 06/12/12\r\n | 18:35:37\r\n | BLS-BLS EMS CALL\r\n | 121 UNION ST-CM\r\n | DEAD END TRIPOLI RD-CM\r\n | Sta 40, Sta 48
 DATE: 06/12/12\r\n | 11:14:25\r\n | 10C01-CHARLIE CHEST PAIN\r\n | 1100 W HIGH ST-CM\r\n | MYERS ST-CM NEW GERMANY RD-CM\r\n | Sta 48
 DATE: 06/12/12\r\n | 09:56:39\r\n | 52C03S-CHARLIE SMOKE DETECTOR ALARM\r\n | 140 E CARROLL ST-CL\r\n | BEDE ST-CL N CHURCH ST-CL\r\n | Sta 5
 DATE: 06/11/12\r\n | 23:56:26\r\n | 28C03L-CHARLIE STROKE\r\n | 429 MANOR DR-CM\r\n | WINDY VALLEY RD-CM\r\n | Sta 48
 DATE: 06/13/12\r\n | 15:52:42\r\n | 18C05-CHARLIE  HEADACHE\r\n | 236 JAMESWAY RD-CM\r\n | WALMART DR-CM ADMIRAL PEARY HWY-CM\r\n | Sta 48
 
 Contact: Active911
 [CAD incident] Date: 07/02/12\nTime: 16:18:01\nNature: STB-STAND BY\nAdd: 237 LINCOLN ST-JO\nCross: WALNUT ST-JO MORRELL PL-JO DOWNTOW\n9
 [CAD incident] Date: 07/02/12\nTime: 17:05:40\nNature: 01A01-Alpha  ABDOMINAL PAIN\nAdd: 207 OTTAWA ST-RI\nCross: ARBUTUS AVE-RI WESLEY DR-RI ZONE 2\n9
 [CAD incident] Date: 07/02/12\nTime: 18:46:33\nNature: 26A10-Alpha  SICK PERSON\nAdd: 428 BELLA VISTA DR-RI\nCross: GREENWICH RD-RI PAWNEE RD-RI ZONE\n9
 [CAD incident] Date: 07/02/12\nTime: 20:34:15\nNature: 17D03-Delta FALL\nAdd: 3324 ELTON RD-RI\nCross: GIBBY LN-RI HOOVER ST-RI ZONE 1\n9
 [CAD incident] Date: 07/02/12\nTime: 20:46:25\nNature: ALS-ALS EMS CALL\nAdd: 663 BUNKERHILL RD CENTRAL CITY\nCross: \n9
 [CAD incident] Date: 07/02/12\nTime: 20:49:13\nNature: 09E01-Echo CARDIAC ARREST\nAdd: 349 VO TECH DR-RI\nCross: ELTON RD-RI SCHOOLHOUSE RD-RI ZONE\n9, M381, 3
 [CAD incident] Date: 07/03/12\nTime: 01:15:05\nNature: NET-NON EMERGENCY TRANSFER\nAdd: LOCAL TRANSFER - A91\nCross: \n9
 [CAD incident] Date: 07/03/12\nTime: 05:18:10\nNature: 30B01-Bravo TRAUMATIC INJURY\nAdd: 429 INDUSTRIAL PARK RD-RI\nCross: OAKRIDGE DR-RI ALLENBILL DR-RI ZON\n9
 [CAD incident] Date: 07/03/12\nTime: 06:57:07\nNature: 26D01-Delta  SICK PERSON\nAdd: 134 KINZEY ST-GE\nCross: ALFRED ST-GE BENTWOOD AVE-GE\n9
 [CAD incident] Date: 07/03/12\nTime: 07:16:15\nNature: 10D01-Delta CHEST PAIN\nAdd: 202 HUFF ST-ADAM\nCross: THOMPSON AVE-ADAM ROBERTS AVE-ADAM\nSta 78, 9
 [CAD incident] Date: 07/03/12\nTime: 12:44:20\nNature: NET-NON EMERGENCY TRANSFER\nAdd: LOCAL TRANSFER - A93\nCross: \n9
 [CAD incident] Date: 07/03/12\nTime: 13:09:09\nNature: 25A01-Alpha  PSYCHIATRIC PROBLEM\nAdd: 207 STONEHEDGE CT-RI\nCross: WALTERS AVE-RI WALTERS AVE-RI ZONE\n9
 [CAD incident] Date: 07/03/12\nTime: 15:20:30\nNature: 25A02-Alpha PSYCHIATRIC PROBLEM\nAdd: 1425 SCALP AVE-RI\nCross: EISENHOWER BLVD-RI DWIGHT DR-RI ZO\n9
 [CAD incident] Date: 07/03/12\nTime: 15:25:52\nNature: 12D04-Delta SEIZURES\nAdd: 101 CLAIR ST-LO\nCross: TERRY ST-LO OHIO ST-LO LORAIN BORO\n9, M381
 [CAD incident] Date: 07/03/12\nTime: 18:40:18\nNature: 26A10-Alpha  SICK PERSON\nAdd: 349 VO TECH DR-RI\nCross: ELTON RD-RI SCHOOLHOUSE RD-RI ZONE\n9
 [CAD incident] Date: 07/03/12\nTime: 21:52:49\nNature: SS-EMS STATION STILL\nAdd: 500 GALLERIA DR-RI\nCross: RT219 NORTH ON RAMP-RI OAKRIDGE DR\n9
 [CAD incident] Date: 07/03/12\nTime: 22:00:08\nNature: 28C10G-Charlie STROKE\nAdd: 122 CARWYN DR-RI\nCross: WORK DR-RI SCALP AVE-RI ZONE 2\n9
 
 */
 
 public class PACambriaCountyParser extends FieldProgramParser {
   
   private static final Pattern MARKER1 = Pattern.compile("\\d\\d");
   private static final Pattern MARKER2 = Pattern.compile("^\\d\\d ");
   private static final Pattern BAR_PTN = Pattern.compile("([ \n])\\| ");
   
   public PACambriaCountyParser() {
     super(CITY_CODES, "CAMBRIA COUNTY", "PA",
            "( Date:DATE | ) ( Time:TIME! Nature:CALL! Add:ADDR/y! Cross:X? | DATE:DATE! TIME CALL ADDR/y X ) UNIT Sta:UNIT");
   }
   
   @Override
   public String getFilter() {
     return "alerts@cambria.ealertgov.com,Cambria 9-1-1";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (MARKER1.matcher(subject).matches()) {
       Matcher match = MARKER2.matcher(body);
       if (!match.find()) return false;
       body = body.substring(match.end()).trim();
     }
     
     body = body.replace("Location:", "Add:");
     String[] flds = body.split("\\|");
     if (flds.length < 5) flds = body.split("\n\\|?");
     if (flds.length >= 5) {
       return parseFields(flds, data);
     }
     body = BAR_PTN.matcher(body).replaceAll("$1Sta: ");
     if (body.endsWith("|")) body = body.substring(0,body.length()-1).trim();
     flds = body.split("\n");
     if (flds.length >= 5) {
       return parseFields(flds, data);
     } else {
       return super.parseMsg(body.replace('\n', ' '), data);
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       for (String city : CITY_LIST) {
         int pt = field.length() - city.length();
         if (pt < 0) continue;
         if (!city.equals(field.substring(pt).toUpperCase())) continue;
         if (pt > 0 && field.charAt(pt-1)!=' ') continue;
         data.strCity = field.substring(pt);
         field = field.substring(0,pt).trim();
         break;
       }
       super.parse(field, data);
     }
   }
   
   private static final Pattern CITY_CODE_PTN = Pattern.compile("-[A-Z]{2} ");
   private static final Pattern CITY_CODE_END_PTN = Pattern.compile("-[A-Z]{2}$");
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       field = CITY_CODE_PTN.matcher(field).replaceAll(" & ");
       field = CITY_CODE_END_PTN.matcher(field).replaceAll("");
       super.parse(field.trim(), data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "ADAM", "ADAMS TWP",
       "ASHB", "ASHVILLE",
       "BARR", "BARR TWP",
       "BN", "BROWNSTOWN",
       "BT", "BLACKLICK TWP",
       "CA", "CASSANDRA",
       "CB", "CRESSON",
       "CF", "CLEARFIELD TWP",
       "CH", "CHEST TWP",
       "CL", "CARROLLTOWN",
       "CM", "CAMBRIA TWP",
       "CONT", "CONEMAUGH TWP",
       "CP", "CHEST SPRINGS",
       "CR", "CRESSON TWP",
       "CROY", "CROYLE TWP",
       "DB", "DALE",
       "DE", "DEAN TWP",
       "DT", "DAISYTOWN",
       "EA", "EAST TAYLOR TWP",
       "EB", "EBENSBURG",
       "ECON", "EAST CONEMAUGH",
       "EF", "EHRENFELD",
       "ECAR", "EAST CARROLL TWP",
       "ET", "ELDER TWP",
       "FB", "FERNDALE",
       "FR", "FANKLIN",
       "GB", "GALLITZIN",
       "GE", "GEISTOWN",
       "GT", "GALLITZIN TWP",
       "HB", "HASTINGS",
       "JO", "JOHNSTOWN",
       "JT", "JACKSON TWP",
       "LB", "LORETTO",
       "LI", "LILLY",
       "LO", "LORAIN",
       "LY", "LOWER YODER TWP",
       "MT", "MIDDLE TAYLOR TWP",
       "MU", "MUNSTER TWP",
       "NC", "NORTHERN CAMBRIA",
       "NG", "NANTY GLO",
       "PATB", "PATTON ",
       "PORB", "PORTAGE",
       "PORT", "PORTAGE TWP",
       "RI", "RICHLAND TWP",
       "RT", "READE TWP",
       "SB", "SANKERTOWN",
       "SC", "STONYCREEK TWP",
       "SF", "SOUTH FORK",
       "SL", "SCALP LEVEL",
       "SM", "SOUTHMONT",
       "SQ", "SUSQUEHANNA TWP",
       "SUMB", "SUMMERHILL",
       "SUMT", "SUMMERHILL TWP",
       "TB", "TUNNELHILL",
       "UY", "UPPER YODER TWP",
       "VB", "VINTONDALE",
       "WB", "WESTOVER",
       "WC", "WEST CARROLL TWP",
       "WE", "WHITE TWP",
       "WI", "WILMORE",
       "WM", "WESTMONT",
       "WS", "WEST TAYLOR TWP",
       "WT", "WASHINGTON TWP",
   });
   
   private static final String[] CITY_LIST = new String[] {
     "CENTRAL CITY"
   };
 }
