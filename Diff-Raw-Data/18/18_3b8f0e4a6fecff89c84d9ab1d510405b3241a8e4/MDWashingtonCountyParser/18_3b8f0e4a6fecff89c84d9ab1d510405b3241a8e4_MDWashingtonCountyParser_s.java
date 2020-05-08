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
 
 */
 
 public class MDWashingtonCountyParser extends FieldProgramParser {
   
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
   
   private static final Pattern DELIM = Pattern.compile(" *(?<= )- +|  ,");
   private static final Pattern TRAILER = Pattern.compile("(?:(.+) )?(\\d{7}) (\\d\\d:?\\d?\\d?)(?: (.*))?");
   
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
  
   public MDWashingtonCountyParser() {
     super(CITY_LIST, "WASHINGTON COUNTY", "MD",
         "ADDR CITY? CALL! CALL+? UNIT1 UNIT+? TRAIL! END");
   }
   
   @Override
   public String getFilter() {
     return "Dispatch@washco-md.net,rc.442@c-msg.net,TextAlert@sems79.org";
   }
   
   @Override
  public boolean parseMsg(String body, Data data) {
     
     // Drop everything after the first newline
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
     
     // Split body into fields separated by  -
     return parseFields(DELIM.split(body), data);
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
     @Override
     public void parse(String field, Data data) {
       data.strUnit = append(data.strUnit, ",", field);
     }
   }
   
   private class TrailField extends Field {
 
     @Override
     public void parse(String field, Data data) {
       Matcher match = TRAILER.matcher(field);
       if (!match.matches()) abort();
       data.strCallId = match.group(2);
       data.strTime = match.group(3);
       if (data.strTime.length() != 5) data.strTime = "";
       data.strSupp = append(getOptGroup(match.group(1)), " / ", getOptGroup(match.group(4)));
       if (data.strSupp == null) data.strSupp = "";
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
     if (name.equals("UNIT1")) return new UnitField("[A-Z]+[0-9]+(?:,[A-Z0-9,]+)?");
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("TRAIL")) return new TrailField();
     return super.getField(name);
   }
 }
