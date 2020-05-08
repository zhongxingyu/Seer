 package net.anei.cadpage.parsers.TX;
 
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Denton County, TX
 Contact: Curtis Birt <cbirt@cityofcorinth.com>
 Sender: CAD@dentoncounty.com 
 
 CAD:DOWN POWER LINES;WINDY MEADOW DR/CORINTH PKWY;CORI;CALLER ADV HIS DRIVER THAT HIT THE POLE CALLED HIM WITH THE INFORMATION // CALLER IS NOT ON SCENE // NO
 CAD:FALLS;1620 VILLA CT;CORI;[Medical Priority Info] PROBLEM: FELL OUT OF BED # PATS: 1 AGE: 71 Years SEX: Male CONSCIOUS: Yes BREATHING: Yes [08/08/11 14:34:3
 CAD:CITIZEN ASSIST;212 S SHADY SHORES RD;LAKE;NEAR MAIN OFFICE;AT THE HUNDLEY SHORES APARTMENTS [08/08/11 09:54:29 JCADENHEAD] CALLER HAS TRIED EVERYTHING SHE
 CAD:SICK PERSON;5650 I35E;CORI;ORR NISSAN;MAIN BLDG AT NEW CAR SHOW ROOM / PT IN BATHROOM [08/08/11 08:09:48 THIGGINS] [Medical Priority Info] RESPONSE: Alpha
 CAD:TRAFFIC TRANSPORT INCIDENT;461MM I35 E;CORI;N/B;[Medical Priority Info] RESPONSE: Bravo RESPONDER SCRIPT: Unknown status/Other codes not applicable (Unknow
 CAD:TRAFFIC TRANSPORT INCIDENT;CORINTH PKWY/I35 E SERV;CORI;[LAW] {351} HEAD ON [08/04/11 09:29:27 JCOOK] FEMALE BLEEDING FROM FACE IN MITS GALANT [08/04/11 09
 CAD:TRAFFIC TRANSPORT INCIDENT;CORINTH PKWY/I35 E SERV;CORI;FEMALE WAS HIT HEAD ON BUT THAT WAS ALL SHE SAID BEFORE DISCONNECTING OTHER THAN THAT FACT THAT SHE
 CAD:FIRE SMOKE INVESTIGATION;TURBEVILLE RD/I35 E SERV;HICK;[LAW] POSS IN HICKORY CREEK PARK [08/04/11 13:05:39 MKING] [LAW] {207} REQUESTING LCFD [08/04/11 13:
 CAD:ALARM FLOW;1035 HICKORY CREEK BLVD;HICK;212 NOTIFIED [08/05/11 03:40:25 BPUTNAM] rec: 0337 / will call prem next / att kh [08/05/11 03:40:01 BPUTNAM] poe:
 CAD:SICK PERSON;1509 SHADOW CREST DR;CORI;[Medical Priority Info] PROBLEM: VOMITTING # PATS: 1 AGE: 72 Years SEX: Female CONSCIOUS: Yes BREATHING: Yes [08/05/1
 CAD:SICK PERSON;1 LAKE LN;HICK;[Medical Priority Info] PROBLEM: GRANDFATHER HAS PROBLEMS # PATS: 1 AGE: 77 Years SEX: Male CONSCIOUS: Yes BREATHING: Yes [08/05
 CAD:TRAFFIC TRANSPORT INCIDENT;460MM I35 E;CORI;SB;[LAW] {211} GO AHEAD AND TONE MEDICS [08/05/11 18:24:06 AVINSON] got transfer from pd denton for an accident
 CAD:WATER MAIN BREAK;I35 E SERV/MEADOWVIEW DR;CORI;S/B;WATER MAIN BREAK / NOT A LOT OF WATER / SMALL LINE / LEAKING ONTO THE EDGE OF THE ROADWAY OF MEADOWVIEW
 CAD:FALLS;201-L32 N SHADY SHORES RD;LAKE;CALLER IS GUY JEFFERSON [08/06/11 03:03:19 MHAMILTON] WIFE SLIPPED IN BATHTUB AND NEEDS ASSISTANCE TO GET HER OUT//SHE
 CAD:SICK PERSON;505 GRAYSON LN;LAKE;[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT: Not alert. 1.She is not completely alert (not responding appropri
 CAD:TRAFFIC TRANSPORT INCIDENT;FM 2181/POST OAK DR;CORI;SB;{346} TONE OUT MEDICS FOR EVALUATION OF 13YO PT [08/06/11 13:31:42 JCADENHEAD] RD BLOCKAGE / JEEP IS
 CAD:SICK PERSON;1200 MAIN ST;HICK;Event spawned from BOATER ASSIST. [08/06/2011 17:47:05 TCHAPMAN] LOST CONNECTION / CALLER ADV BATTERY WAS LOW [08/06/11 17:46
 CAD:WATER MAIN BREAK;FAIRVIEW DR/MEADOWVIEW DR;CORI;FIRST NOTICED 3-4 MINS AGO [08/06/11 18:03:38 TCHAPMAN] NO ONE AROUND THE HYDRANT [08/06/11 18:03:25 TCHAPM
 CAD:TRAFFIC TRANSPORT INCIDENT;MEADOWVIEW DR/FORESTWOOD DR;CORI;[LAW] UDTS: {314} RESPONDING CODE 2 OR 3 [08/06/11 18:07:04 CJMORRISON] [Medical Priority Info]
 
 CAD:110137609;238 STRAIT LN;HILLTOP LN;HICKORY CREEK;LCFD;FIRE STRUCTURE
 CAD:110138525;08/05/2011 17:00:12;FIRE VEHICLE;ORR NISSAN;5650 I35 E;LCFD;brake on fire - sees flame [08/05/11 17:01:04 TPRICE]
 
 CAD:FYI: ;120027784;02/12/2012 23:49:22;TRAFFIC TRANSPORT INCIDENT;458MM I 35 E;LCFD;[Medical Priority Info] RESPONSE: Bravo RESPONDER SCRIPT: Unknown status/O
 CAD:FYI: ;120027776;02/12/2012 23:38:48;FIRE STRUCTURE;1402 CHEYENNE RD;LARAMIE DR;LVFD;2 ALARM [02/12/12 23:41:25 MELLIS] OPS CHANNEL 4 [02/12/12 23:40:31 MEL
 CAD:FYI: ;120027767;02/12/2012 23:05:32;BREATHING PROBLEMS;1652 KNOLL RIDGE CIR;SHADOW CREST DR;LCFD;[Medical Priority Info] RESPONSE: Delta RESPONDER SCRIPT:
 
CAD:FYI: ;120112563;06/11/2012 08:33:17;HEAT COLD EXPOSURE;201-147 N SHADY SHORES RD;LAKE DALLAS;E OVERLY DR;LCFD;[Medical Priority Info] P

  */
 
 public class TXDentonCountyParser extends DispatchOSSIParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "CORI", "CORINTH",
       "LAKE", "LAKE DALLAS",
       "LVFD", "LEWISVILLE",
       "HICK", "HICKORY CREEK",
       "SHAD", "SHADY SHORES",
       "LCFD", ""
   });
   
   public TXDentonCountyParser() {
     super(CITY_CODES, "DENTON COUNTY", "TX",
           "FYI? ( ID ( DATIME CALL ( ADDR/Z CITY | NAME ADDR CITY ) INFO+ | ADDR X X CITY CALL ) | CALL ADDR CITY INFO+ )");
   }
   
   @Override
   public String getFilter() {
     return "CAD@dentoncounty.com";
   }
   
   @Override
   public String getSponsor() {
     return "Lake Cities Fire Department";
   }
 
   private class MyIdField extends IdField {
     public MyIdField() {
       setPattern(Pattern.compile("\\d{9}"));
     }
   }
   
   private static final Pattern SERV_PTN = Pattern.compile("\\bSERV\\b");
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       field = SERV_PTN.matcher(field).replaceAll("FRONTAGE");
       super.parse(field, data);
     }
   }
   
   private static final Pattern CITY_PTN = Pattern.compile("[A-Z]{4}");
   private class MyCityField extends CityField {
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       if (!CITY_PTN.matcher(field).matches()) return false;
       parse(field, data);
       return true;
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ID")) return new MyIdField();
     if (name.equals("DATIME")) return new DateTimeField("\\d\\d/\\d\\d/\\d{4} \\d\\d:\\d\\d:\\d\\d");
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("CITY")) return new MyCityField();
     return super.getField(name);
   }
 }
