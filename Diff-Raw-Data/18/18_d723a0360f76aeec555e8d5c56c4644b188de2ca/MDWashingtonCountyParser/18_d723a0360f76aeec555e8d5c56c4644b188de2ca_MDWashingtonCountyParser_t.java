 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Washington County, MD
 Contact: "Snyder, Jon" <JSnyder@sems79.org>
 Sender: "Dispatch@washco-md.net" <Dispatch@washco-md.net>
 System: Keystone Public Safety
 
 MAPLEVILLE RD / CHEWSVILLE RD - PIC, PERS INJURY COLLISION - CO16,RSQE7,R79,SO20 - 1106957 15:38
 73 S MAIN ST - FALLS,BACK INJURIES - R79 - 1106940 12:07 2nd ALERT!
 14518 MARYLAND AVE - STROKE - FAA4,R79,CO9 - 1106937 11:31
 13730 MAUGANSVILLE RD, MAUGANSVILLE FIRE COMPANY - TRANSFER OF UNIT, - CO26,CO9 - 1106889 16:55
 LEHMANS MILL RD - WATER RESCUE - - CO9,RS13,R75,BT20,SO20,RS2001,D20,M753,RSQE91,DC9,R79 - 1106823 23:27 2nd ALERT!
 FRE CO, N FRANKLINVILLE RD/RT15 - THURMONT - WATER RESCUE - - RSQE7 - TAC 30 1106797 21:01
 FRE CO, 15204 QUIRAUK SCHOOL RD - NAT GAS LEAK - CO7,R79,P791,RSQE7 - FIRE 60 1106779 19:49
 21348 OLD FORGE RD, ST PETERS BEARD LUTHERAN CHURCH - AUTOMATIC FIRE ALARM - COMMERCIAL , IND - CO9 - 1106753 16:45
 INTERSTATE 70, EXIT35 I70 EB - PIC, PERS INJURY COLLISION - CO16,CO10,R75,R79,E101,P791,P751,SUP160,DC10,RS10,E161,CA75 - 1106746 16:21
 22507 JEFFERSON BLVD, AC&T SMITHSBURG - EYE PROBLEMS,INJURIES - R79 - 1106670 16:20
 20354 LEITERSBURG PIKE, PEN MAR TRAILER SALES INC - NAT GAS ODOR - CO9 - 1106635 00:13
 LEITERSBURG PIKE / MILLERS CHURCH RD - PIC, PERS INJURY COLLISION - CO9,R75,P753,R79 - 1106553 22:22
 MAPLEVILLE RD / JEFFERSON BLVD - PIC, PERS INJURY COLLISION - CO7,R79,M791,SUP70,P792,C79,RSQE7 - 1106339 12:40
 INTERSTATE 70, 32MM I70 AT REST AREA - ABDOMINAL PAIN - PROBLEMS - R75,R79,CO10 - 1106419 16:28\nDo Not REPLY to this email!!!
 
 Contact: Jim A <skidooxman@gmail.com>
 Sender: TextAlert@sems79.org
 CHEWSVILLE RD / WHITE HALL RD, AREA - SMOKE INVESTIGATION - CO16,SO20,CO7,RS20,E161,E74,ET7 - 1117561 07:46\nDo Not REPLY to this email!!!
 
 Contact: cj rinehart <cjrr_10@hotmail.com>
 Sender: rc.429@c-msg.net
 S:WCo M:[!] 136 WISHING STAR CT  ,STAB  ,GUNSHOT  ,R75  ,CO10  ,1120268 23:48\n\n\n
 S:WCo M:[!] 11403 STONECROFT CT  , STONECROFT APARTMENTS BUILDING A APT 209  ,CHEST PAIN  ,R75  ,CO10  ,1120271 02:16
 S:WCo M:[!] 1800 DUAL HWY  , SUSQUEHANNA BANK STE 100 REAR OF  ,SMOKE INVESTIGATION  ,E03  ,CO10  ,UT3  ,1120229 07:12
 S:WCo M:[!] 300 N COLONIAL DR  , APT E  ,HOUSE FIRE  ,CO10  ,E03  ,E01  ,TRK1  ,S75  ,R75  ,SO20  ,1120236 09:48
 S:WCo M:[!] 136 WISHING STAR CT  ,STAB  ,GUNSHOT  ,R75  ,CO10  ,1120268 23:48
 S:WCo M:[!] 17567 YORK RD  , WESTERN SIZZLIN STEAK HOUSE  ,ODOR OF SMOKE  ,CO26  ,CO10  ,CO2  ,TWR26  ,RS10  ,R26  ,1120262 22:1
 
 Contact: "ForbergerJames@yahoo.com" <ForbergerJames@yahoo.com>
 Sender: support@cadpage.org
 WCo / [!] 1396 S POTOMAC ST, SHEETZ - PIC, PERS INJURY COLLISION - E05,R75,UT3 - 1200168 10:39\n\n
 
Contact: richard Gilbert <rgilbert65@gmail.com>
Sender: rc.327@c-msg.net
Subject:CAD\n[!] W FRANKLIN ST / N BURHANS BLVD - PIC, PERS INJURY COLLISION - E04,S75,S754,UT3 - Traffic / Transportation Accident (Crash). 12\r

 Contact: support@active911.com
 Sender: rc.337@c-msg.net
 Subject: WCo\n[!] 17850 GARLAND GROH BLVD, SUPER WALMART CENTER - Accident - Property Damage - 9C16 - Traffic / Transportation Accident (Crash). 11:57
 Subject: WCo\n[!] INTERSTATE 81 / I70 - PIC, PERS INJURY COLLISION - CO26,CO2,RSQE2,R26,R2 - 1208763 11:59
 Subject: WCo\n[!] VIRGINIA AVE / W WILSON BLVD - Accident - Property Damage - 9C21 - Traffic / Transportation Accident (Crash). 12:10
 Subject: WCo\n[!] 222 OTHO HOLLAND DR - UNCONSCIOUSNESS,FAINTING - R26 - 87 year old, Female, Unconscious, Breathing. - **ALS HOT*- Unconscious – Effective breathing 1208767 12:41 2nd ALERT!
 Subject: WCo\n[!] 365 WOODPOINT AVE - PSYCHIATRIC,SUICIDE ATTEMPT - R26,E04 - Mental Disorder (Behavioral Problems). 1208777 13:32
 Subject: WCo\n[!] 11346 LAKESIDE DR - FALLS,BACK INJURIES - R49,CO26 - 85 year old, Male, Conscious, Breathing. - **BLS-ALPHA *** On ground or floor - PUBLIC ASSIST (no injuries and no priority symptoms) 1208778 13:36
 Subject: WCo\n[!] 1321 S POTOMAC ST, E RUSSELL HICKS MIDDLE SCHOOL - Accident - Property Damage - 9C20 - Traffic / Transportation Accident (Crash). 16:01
 Subject: WCo\n[!] JONATHAN ST / CHURCH ST - Accident - Property Damage - 9E32 - 16:01
 Subject: WCo\n[!] 14 N POTOMAC ST - OUTSIDE FIRE - E01,UT3 - ProQA Fire abort reason: DISCONNECTED CALL. Outside Fire. 1208845 11:22
 Subject: WCo\n[!] 18 DOWNSVILLE RD - CHEST PAIN - R75,P751,R26,E05 - 81 year old, Male, Conscious, Breathing. - **ALS HOT*-Abnormal breathing 1208790 16:29
 Subject: WCo\n[!] 601 DUAL HWY, SUPER SHOE STORE - Accident - Property Damage - 9A13 - Traffic / Transportation Accident (Crash). 16:41
 Subject: WCo\n[!] 16533 TAMMANY LN - MEDICAL EMERGENCY - R26 - 83 year old, Female, Conscious, Breathing. - **BLS-ALPHA *** No priority symptoms (complaint conditions 2–11 not identified) 1208833 06:06
 Subject: CAD\n[!] 895 W WASHINGTON ST, IFO - FUEL,GAS,OIL,ANTIFREEZE,DIESEL SPILL - E04,UT3 - Fuel Spill. - UNKNOWN SITUATION (Investigation)-Outside 1208801 17:43
 Subject: WCo\n[!] 16843 LONGFELLOW CT - FALLS,BACK INJURIES - R26 - 86 year old, Female, Conscious, Breathing. - **BLS HOT*** On ground or floor - Unknown status/Other codes not applicable 1208807 18:41
 Subject: WCo\n[!] 151 N BURHANS BLVD, GOODWILL INDUSTRIES - Accident - Property Damage - 9A17 - Traffic / Transportation Accident (Crash). 18:59
 Subject: WCo\n[!] 300 NORTHERN AVE, LONGMEADOW APTS. BLDG 3 APT A - UNSPECIFIED ALARM - E02,CO27,TRK1,UT3,BC1 - Alarms. - HIGH RISE-UNKNOWN 1208818 21:25
 Subject: WCo\n[!] 10138 ST GEORGE CIR - CONVULSIONS,SEIZURE - R26,CO10 - 54 year old, Male, Unconscious, Breathing. - **ALS HOT*-CONTINUOUS or MULTIPLE seizure 1208819 21:38
 Subject: CAD\n[!] 860 PINE ST - UNCONSCIOUSNESS,FAINTING - S754,E05,UT3 - 44 year old, Male, Unconscious, Breathing. - **ALS HOT*- Unconscious – AGONAL/INEFFECTIVE BREATHING 1208821 00:09
 Subject: CAD\n[!] 12927 LITTLE ELLIOTT DR APT 9 - CARDIAC,RESPIRATORY ARREST - R75,E03,UT3 - 26 year old, Male, Unconscious, Not breathing. - **ALS HOT*-ECHO- Not breathing at all 1208824 00:45
 Subject: WCo\n[!] SHARPSBURG PIKE - UNCONSCIOUSNESS,FAINTING - R26 - 1208825 01:39
 Subject: WCo\n[!] 443 S BURHANS BLVD, HAGERSTOWN MOPED - Accident - Property Damage - 9C21 - 11:47
 Subject: CAD\n[!] 243 SUMMIT AVE, - CHEST PAIN - R75,E02,UT3 - 42 year old, Female, Conscious, Breathing. - **ALS HOT*- Breathing normally =>35 1208849 11:54
 Subject: WCo\n[!] 38 E NORTH AVE - Accident - Property Damage - 9C17 - 1. Traffic / Transportation Accident (Crash). 12:08
 Subject: WCo\n[!] 148 SUNFLOWER DR - DIABETIC PROBLEMS - R26 - 83 year old, Female, Conscious, Breathing. - **BLS-ALPHA**- Alert and behaving normally 1208783 14:48
 Subject: CAD\n[!] 11110 MEDICAL CAMPUS RD, ROBINWOOD URGENT CARE- STE.147(BLU) - STROKE - R75,P756,CO10,UT3 - 80 year old, Male, Conscious, Breathing. - *ALS HOT*- Sudden weakness or numbness (one side)- (No test evidence (Less than 3 hrs) 1208754 10:05
 Subject: WCo\n[!] 234 JONATHAN ST, THREE MINUTE CAR WASH - Accident - Property Damage - 9C15 - 11:00
 Subject: WCo\n[!] 517 CHURCH ST - Accident - Property Damage - 9C17 - Traffic / Transportation Accident (Crash). 08:52
 Subject: WCo\n[!] 50 W WASHINGTON ST, UNIVERSITY OF MARYLAND-HAGERSTOWN - Accident - Property Damage - 9718 - 11:38
 Subject: WCo\n[!] DUAL HWY, AMES SHOPPING CENTER - Accident - Property Damage - 9C17 - Traffic / Transportation Accident (Crash). 08:24
 
 Subject: WCo\n[!] Recall Reason: ALERTED FAA3\n365 WOODPOINT AVE - PSYCHIATRIC,SUICIDE ATTEMPT - R26,E04 - Mental Disorder (Behavioral Problems). 1208777 13:32
 
 Subject: WCo\n[Marvin,Niswander]Meeting to night 18:30
 Subject: WCo\n[!] FP NEEDED FOR TRAFFIC CONTROL IN 800 BLK OF W WASHINGTON ST IN HAGERSTOWN.
 Subject: WCo\n[!] FP REQUESTED TO ASSIST W/TRAFFIC CONTROL UNIT BLOCK OF S POTOMAC ST IN HAGERSTOWN.
 
 */
 
 public class MDWashingtonCountyParser extends FieldProgramParser {
   
   private static final Pattern DELIM = Pattern.compile(" *(?<= )- +|  ,");
  
   public MDWashingtonCountyParser() {
     super(CITY_LIST, "WASHINGTON COUNTY", "MD",
         "ADDR CITY? CALL! CALL+? UNIT UNIT+? INFO+? TRAIL! END");
   }
   
   @Override
   public String getFilter() {
     return "Dispatch@washco-md.net,@c-msg.net,TextAlert@sems79.org";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     // Drop everything after the first newline
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     
     pt = body.indexOf(" / [!] ");
     if (pt >= 0) {
       subject = body.substring(0,pt).trim();
       body = body.substring(pt+7).trim();
     }
     
     if (subject.endsWith("|!")) subject = subject.substring(0,subject.length()-2).trim();
     if (!subject.equals("CAD")) data.strSource = subject;
     
     // Split body into fields separated by  -
     return parseFields(DELIM.split(body), data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       
       // First field contains option M/A county, address and optional place name
       Parser p = new Parser(field);
       String maCounty = p.getOptional(" CO, ");
       if (maCounty.length() > 0) {
         maCounty = COUNTY_CODES.getProperty(maCounty, maCounty);
         int pt = maCounty.indexOf(',');
         if (pt >= 0) {
           data.strState = maCounty.substring(pt+1);
           maCounty = maCounty.substring(0,pt);
         }
         data.strCity = maCounty;
       }
       super.parse(p.get(','), data);
       data.strPlace = p.get();
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR PLACE CITY ST";
     }
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       data.strCall = append(data.strCall, " - ", field);
     }
   }
   
   private class MyUnitField extends UnitField {
     public MyUnitField() {
       super("(?:[0-9]?[A-Z]+[0-9]+|[0-9]{4})(?:,[A-Z0-9,]+)?", true);
     }
     @Override
     public void parse(String field, Data data) {
       data.strUnit = append(data.strUnit, ",", field);
     }
   }
   
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       data.strSupp = append(data.strSupp," - ", field);
     }
   }
   
  private static final Pattern TIME_PTN = Pattern.compile("(?<=^| )(\\d\\d:\\d\\d)\\b");
  private static final Pattern TIME2_PTN = Pattern.compile("\\b\\d[\\d:]*$");
   private static final Pattern ID_PTN = Pattern.compile("\\b\\d{7}$");
   private class TrailField extends Field {
 
     @Override
     public void parse(String field, Data data) {
       Matcher match = TIME_PTN.matcher(field);
      if (match.find()) {
        data.strTime = match.group(1);
      } else {
        match = TIME2_PTN.matcher(field);
        if (!match.find()) abort();
      }
       String sPart1 = field.substring(0,match.start()).trim();
       String sPart2 = field.substring(match.end()).trim();
       
       match = ID_PTN.matcher(sPart1);
       if (match.find()) {
         data.strCallId = match.group();
         sPart1 = sPart1.substring(0,match.start()).trim();
       }
       data.strSupp = append(data.strSupp, " - ", sPart1);
       data.strSupp = append(data.strSupp, " - ", sPart2);
     }
     
     @Override
     public String getFieldNames() {
       return "ID INFO TIME";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("INFO")) return new MyInfoField();
     if (name.equals("TRAIL")) return new TrailField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "HAGERSTOWN",
     "BOONSBORO",
     "CLEAR SPRING",
     "FUNKSTOWN",
     "HANCOCK",
     "KEEDYSVILLE",
     "SHARPSBURG",
     "SMITHSBURG",
     "WILLIAMSPORT",
     
     "THURMONT"
   };
   
   private static final Properties COUNTY_CODES = buildCodeTable(new String[]{
       "ALL", "ALLEGANY COUNTY",
       "BER", "BERKELEY COUNTY,WV",
       "FRA", "FRANKLIN COUNTY,PA",
       "FRE", "FREDERICK COUNTY",
       "FUL", "FULTON COUNTY,PA",
       "JEF", "JEFFERSON COUNTY,WV",
       "LOU", "LOUDOUN COUNTY,VA",
       "MOR", "MORGAN COUNTY,WV"
   });
 }
