 package net.anei.cadpage.parsers.CO;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Weld County, CO
 
 Contact: Ryan Fuller <rfuller131@gmail.com>
 Contact: Greg McGinn <GMcGinn@ftluptonfire.com>
 Contact: Rand Sheldon <rls34173@gmail.com>
 Contact: Derek Olsen <dolsen207@gmail.com>
 Contact: Dustin West <dwest3223@gmail.com>
 Contact: Josh Tapia <JTapia@ftluptonfire.com>
 " " 24\nSIPF\nD\n3211 LUPTON AVE\nEV\n24\nMOM IS NOT WAKING UP 0000 Confirm 0001 Refuse TXT STOP to opt-out
 " " 24\nALARMFR\nD\n2936 PARK VIEW DR   ; CARRICH RESIDENCE\nEV\n24D\nAUDIBLE FROM ZONE 4.  NO ANSWER OS.  PREM PH 970 339 7664.  WILL BE ATC\nKEYHOLDER. 0000 Confirm 0001 Refuse TXT STOP to opt-out
 " " 36\nTAU\nD\nWCR 21\nPL\n36\n4DR SD DOWN IN DITCH\nProQA Medical Recommended Dispatch Level = 29B04U\nYou are responding to patients involved in a traffic incident.  (If known -- relay  pertinent patient-specific information.) Unknown status/Other codes n TXT STOP to opt-out
 " " 24\nSIPF\nD\n800 31ST ST; AMERICAS BEST VALUE INN RM #31\nEV\n24\nHSBND BEGAN HAVING A SIEZURE WHILE ON THE PHN W/HER. RP NOT OS TXT STOP to opt-out
 " " 36\nSMKODR\nD\nWCR 17\nPL\n36\n1/4 MILES WEST...NORTH SIDE OF THE ROAD..NATURAL GAS LINE LEAK.. NO RESIDENCES\nTHREATENED TXT STOP to opt-out
 " " 26\nTAU\nD\nHWY 52\nFL\n26A\nSB** RP SEEN A BLK CAR POSS 4 DR CAR GO OFF THE RD AND DOWN EMBANKMENT, RP JUST\nPASSED BY TXT STOP to opt-out
 " " 26\nTAU\nD\nWCR 18\nFL\n26D\nVEH IN THE DITCH LIGHTS STILL ON - RP COULD NOT STOP. VEH IS A DRK SEDAN\nProQA Medical Recommended Dispatch Level = 29B04U\nYou are responding to patients involved in a traffic incident.  (If known -- relay  pertinent patien TXT STOP to opt-out
 " " 26\nTAU\nD\nHWY 85 SB\nFL\n26\nRED CHEVY BLAZER VS WHITE FORD EXPLORER.  EXPLORER LOST CONTROL\nProQA Medical Recommended Dispatch Level = 29B04U\nYou are responding to patients involved in a traffic incident.  (If known -- relay  pertinent patient-speci TXT STOP to opt-out
 " " 26\nSIPF\nD\n1001 MOUNTVIEW AVE;410\nFL\n26\nRP IS WEAK, ALL OF A SUDDEN\nProQA Medical Recommended Dispatch Level = 26A05\nYou are responding to a patient who is sick (or has a current medical condition).  The patient is a 50-year-old male, who is consc TXT STOP to opt-out
 " " 26\nFASIST\nD\nWCR 31\nFL\n26\nVEH HIT A POWER POLE, POLE IS DOWN, LINES DOWN...LINES NOT SPARKING . RP\nBELIEVES UNITED POWER SERVICES THE AREA. RP IS OS IN A BLUE DODGE PU\nPOWERLINES DOWN DUE TO TAHR, NOT SPARKING, WILL ADVISE  UNITED POWER TXT STOP to opt-out
 " " 26\nSIPF\nD\n451 ROLLIE AVE\nFL\n26\nRES HAS FALLEN, HURT ANKLE, TOO LG FOR RP TO PU\nProQA Medical Recommended Dispatch Level = 17A01G\nYou are responding to a patient involved in a fall.  The patient is a 61-year-old  female, who is conscious and breat TXT STOP to opt-out
 " " 26\nSIPF\nD\n7898 RICHARD ST\nFL\n26\nFEMALE GOING CRAZY - POSS PSYCH ISSUE TXT STOP to opt-out
 " " 26\nFIREV\nD\n14619 WCR 18\nFL\n26 TXT STOP to opt-out
 " " 26\nSI\nD\n1001 MOUNTVIEW AVE; 410\nFL\n26A\nProQA Medical Recommended Dispatch Level = 28C03G\nYou are responding to a patient who has apparently suffered a stroke. The patient  is a 60-year-old male, who is conscious and breathing. Sudden speech  pro TXT STOP to opt-out
 " " 26\nSIPF\nD\n15022 MORRIS AVE\nFL\n26\ndizzy ill female\nProQA Medical Recommended Dispatch Level = 31C01\nYou are responding to a patient who is unconscious (or has 0000 Confirm 0001 Refuse fainted).  The patient  is a 78-year-old female, who is conscious and breathing. Alert w 0000 Confirm 0001 Refuse TXT STOP to opt-out
 " " 26\nATSUCI\nD\n1001 MOUNTVIEW AVE; 106\nFL\n26\nSUICIDAL PARTY CARY BOLINGTON\nProQA Medical Recommended Dispatch Level = 25B03\nYou are responding to a patient who has abnormal or suicidal behavior.  The patient is a 52-year-old male, who is conscious a TXT STOP to opt-out
 " " 26\nSI\nD\nHWY 52\nFL\n26\nRP PULLED OVER TO HELP A MAN THAT WAS ON THE SIDE OF THE ROAD ... HIS\nTRANSMISSION WENT OUT BUT THE MAN IS VERY CONFUSED AND DOESNT KNOW WHERE HE IS\nOR HIS NAME OR SONS NAME. RP IS GOING TO WAIT WITH HIM. RP SAYS HE DOESNT NE TXT STOP to opt-out
 " " 26\nSIPF\nD\n5018 WCR 35\nFL\n26\nRP'S WIFE IS HAVING A SEIZURE TXT STOP to opt-out
 " " 26\nMUT\nD\n2999 9TH ST; FL FD STN 2\nFL\n26\nBrighton Fire requesting a Ft Lupton tower to respond and cover BF Stn 51 TXT STOP to opt-out
 " " 26\nTAU\nD\nHWY 52\nFL\n26\nsb//veh in ditch TXT STOP to opt-out
 " " 26\nTAU\nD\nHWY 52\nFL\n26\nMAROON SUV THAT IS ROLLED OVER SOUTH OF HY 52 TXT STOP to opt-outSend time:1328594015000
 " " 32\nFASIST\nD\n23510 HIGHWAY 257\nMI\n32\nS SIDE OF THE UP RR // GAS BLOW OFF\nRP THINKS IT NEEDS TO BE CHECKED ON BCSE IT'S "REALLY BLUE" TXT STOP to opt-out
 " " 26\nTAI\nD\nWCR 19\nFL\n26\nTAI\nProQA Medical Recommended Dispatch Level = 29D02p\nYou are responding to patients involved in a traffic incident.  (If known -- relay TXT STOP to opt-out
 
 Contact: mike stratton <mikerstratton@gmail.com>
 Contact: Doug Gilliland <dgilliland88@gmail.com>
 From:wrc-hiplink@weldcorcc.com
 Dispatch / 22\nSIPF\nD\n13009 WCR 13\nMV3E\n2203\nFALL..INSIDE BUSINESS\n
 Dispatch / LAFF\nFIRESR\nD\n711 MEADOWLARK DR\nBOLFF\n22\nalready toned stn 6 mnt view....for trailer on fire\n
 (Dispatch) 22\nATSUCI\nD\n2651 HUGHS DR\nMV6B\n2206\ntoned mv stn 6 male shot himself in the head with a gun\nProQA Medical Case 5590 Aborted  bouilder emd
 
  */
 
 
 public class COWeldCountyParser extends FieldProgramParser {
 
   public COWeldCountyParser() {
     super(CITY_CODES, "WELD COUNTY", "CO",
           "SRC CALL D ADDR CITY UNIT! INFO+");
   }
   
   @Override
   public String getFilter() {
     return "7771,wrc-hiplink@weldcorcc.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (body.startsWith("Dispatch / ")) body = body.substring(11).trim();
     return parseFields(body.split("\n"), data);
   }
   
   private static final Pattern WCR_PTN = Pattern.compile("\\bWCR\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern APT_PTN = Pattern.compile("[A-Z]?\\d+[A-Z]?");
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(';');
       if (pt >= 0) {
         String sPlace = field.substring(pt+1).trim();
         if (APT_PTN.matcher(sPlace).matches()) {
           data.strApt = sPlace;
         } else {
           data.strPlace = sPlace;
         }
         field = field.substring(0,pt).trim();
       }
       field = WCR_PTN.matcher(field).replaceAll("CR");
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
      return super.getFieldNames() + "APT PLACE";
     }
   }
   
   private static final Pattern CODE_PTN = Pattern.compile("ProQA Medical Recommended Dispatch Level = (\\w+)");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = CODE_PTN.matcher(field);
       if (match.matches()) {
         data.strCode = match.group(1);
       } else {
         super.parse(field, data);
       }
     }
     
     @Override
     public String getFieldNames() {
       return "CODE INFO";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("D")) return new SkipField("D", true);
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "EV", "EVANS",
       "FL", "FORT LUPTON",
       "MI", "MILLIKEN",
       "PL", "PLATTEVILLE"
   });
 }
