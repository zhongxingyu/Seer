 package net.anei.cadpage.parsers.NJ;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.MsgParser;
 
 /*
 Hunterdon County, NJ (Flemington)
 Contact: Dan Palmer <cantbeatl337@gmail.com>
 
 49FD:ALRM ACT: (49)SHOPPES AT FLEMINGTON / 100 REAVILLE AVE NEAR: R202-31 RAMP TO RARITAN TWP LI 12003820: EM NAILS::ZONE 4 & 9 / GENERAL FIRE SIGNAL / WATN
 49FD:SMOKE L3: (49)15 BONNELL ST NEAR: PARK AVE TO ACADEMY ST 12002996 POSSIBLE ELECTRICAL::SMOKE DISAPATING 
 49FD:SMOKE L3: (49)WALGREENS / 29 STATE HWY NO 31 NEAR: CHURCH ST TO RARITAN TWP LINE 12002674: BAD SMELL OF SMOKE COMING OUT OF VENTS
 49FD:ALRM ACT: (49)HUNTER HILLS APARTMENTS / 1 GARDEN LANE NEAR: MAIN ST N TO CUL DE SAC 12002158: APT I-6 / FIRE ALARM SET OFF BY COOKING, REQUESTING ASSIST
 49FD:HMT GAS L3: (21)JOHANNA FARMS / 1 JOHANNA FARMS RD NEAR: R202 SOUTH TO BR+W RR TRACKS 11125350 WATER FLOW::UPGRADE - SMELL OF GAS IN BLDG PER CMD
 49FD:FIRE L3:(21)41 STIRRUP LANE NEAR: CARRIAGE GATE DR TO CARRIAGE G 11123569 SMOKE IN LAUNDRY ROOM::
 
 Contact: Joe Zujkowski <jzujkowski@gmail.com>
 Sender: hces@hunterdon.co.nj.us
 22RS:MVA UNKNOWN: (24)CO HWY NO 517 HWY NEAR: FIELDVIEW/DINNERPOT TO LAUREL 12007721 2 PICKUP TRUCKS::2 PICK UP HEAD ON DS TO DS
 22RS:RESP DIS: (22)18 JUDGE THOMPSON RD NEAR: COLONIAL RD TO CUL DE SAC 12007605 RESP DISTRESS::6 YOM / HAS BEEN GIVEN A NEBULIZER / NO IMPROVEMENT
 22RS:AMB NEED: (22)245 NUTHATCH CT NEAR: NUTHATCH CT/BLDG 43 TO NO OUTL 12007597 SHAKING / LEFT SIDE PAIN::IS IN BED COVERED UP / LEFT SIDE PAIN / SHAKING /
 22RS:MV STOP: (49)HUNTERDON SHOPPING CENTER / 35 REAVILLE AVE NEAR: MAIN ST CIR TO R202-31 12007596 ZDU27L::
 22RS:HEART AA: (22)101 OLD FARM RD NEAR: R523 TO SCRABBLETOWN RD 12007510 30 YOM::CHEST PAIN / STOMACH PAIN / BACK PAIN / ARM PAIN / NAUSEA / DIZZYNESS
 22RS:AMB NEED: (24)23 CO HWY NO 517 HWY NEAR: LAMINGTON RD TO JOLIET ST 12007493 LIFT ASSIST::ASSIST PTL IN MOVING ELDERLY WOMAN FROM CHAIR TO BED
 22RS:AMB NEEDB: (22)CHUBB CORPORATION BUILDING A / 202 HALLS MILL RD NEAR: R523 TO TAYLORS MILL RD 12007449 23YOF PREGNANT::DIZZY NAUSEAS / 1 EAST LADIES RO
 22RS:AMB NEEDB: (22)CHUBB CORPORATION BUILDING A / 202 HALLS MILL RD #BLD A NEAR: R523 TO TAYLORS MILL RD 12007402 50'S YOF::ELEVATED BP NAUSEAS / PT IN NUR
 22RS:UNCON A: (46)76 OLD MOUNTAIN RD NEAR: CHERRY ST TO CONRAIL RR TRACKS 12007379 63YOM::SYNCOPE HX DIABETIES
 22RS:LACCER: (24)24 OLD MINE RD NEAR: COKESBURY RD TO DEAD END 12007354 FACIAL LACERATION::30'S YOA MALE CUT ON FOREHEAD FROM FALLING GLASS, REPLACING WINDO
 
  */
 
 public class NJHunterdonCountyParser extends MsgParser {
   
  private static final Pattern MASTER = Pattern.compile("([A-Z0-9]+):([A-Z 0-9]+): *\\((\\d+)\\) *(.*?) NEAR: *(.*?) (\\d{8})\\b *(.*?)");
   
   public NJHunterdonCountyParser() {
     super("HUNTERDON COUNTY", "NJ");
   }
   
   @Override
   public String getFilter() {
     return "hces@hunterdon.co.nj.us";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strSource = match.group(1);
     data.strCall = match.group(2);
     String sCity = convertCodes(match.group(3), CITY_CODES);
     int pt = sCity.indexOf('/');
     if (pt >= 0) {
       if (data.strSource.endsWith("RS")) sCity = sCity.substring(pt+1);
       else sCity = sCity.substring(0,pt);
     }
     data.strCity = sCity;
     Parser p = new Parser(match.group(4));
     data.strPlace = p.getOptional(" / ");
     parseAddress(p.get().replace(" NO ", " "), data);
     data.strCross = match.group(5);
     data.strCallId = match.group(6);
     data.strSupp = match.group(7);
     return true;
   }
   
   // There are different city codes for fire and medical units.  Mostly they do
   // not conflict.  In the two cases where they do the fire city will come first and
   // be separated from the medical city by a /
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "11", "Frenchtown",
       "12", "Glen Gardner",
       "13", "Hampton",
       "14", "High Bridge",
       "15", "Holland Twp",
       "16", "Kingwood",
       "17", "Lambertville",
       "18", "Lebanon",
       "19", "Lebanon Twp",
       "21", "Raritan Twp",
       "22", "Whitehouse Station/Readington Twp",
       "23", "Stockton",
       "24", "Oldwick/Tewksbury",
       "25", "Pattenburg",
       "26", "West Amwell Twp",
       "31", "East Whitehouse",
       "32", "Readington",
       "33", "Three Bridges",
       "34", "Fairmount",
       "43", "Bloomsbury",
       "41", "Quakertown",
       "44", "Califon",
       "45", "Clinton",
       "46", "Annandale",
       "47", "Sergeantsville",
       "48", "Amwell Valley",
       "49", "Flemington",
       "52", "Asbury",
       "57", "Pottersville",
       "74", "Branchburg",
       "79", "Neshanic/Hillsborough",
      "91", "Quackertown",
       "92", "Milford",
       "93", "Country Hills",
       "94", "Delaware Valley",
       "95", "Upper Black Eddy",
       "96", "New Hope Eagle"
   });
 }
