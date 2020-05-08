 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Lebanon County, PA
 Contact: Loren Miller <lmiller05@gmail.com>
 Sender: 7176798487
 
 Current call logs available at http://lebanonema.org/pager/html/monitor.html
 
 Heidelberg Township 3 GLASSMOYER LN WF - Wildfire/Brush Fire Box 35-05 FG3 A160
 City of Lebanon 541 SPRUCE ST Med Class1 Hemorrhage Box 190-2 199MU
 Cornwall Borough 113 WENGER ST Med Class3 Fall Victim BOX 190-6 A192
 City of Lebanon 437 NEW ST Med Class1 Abdominal Pain Box190-2 BC190
 City of Lebanon 220 E WEIDMAN ST Med Class1 ALOC Box 190-2 A193 M12-3
 South Lebanon Township 590 S 5TH AVE * Cedar Haven ROOM 313F Med Class1 Heart Problem Box 190-3 193MU
 Palmyra Borough 101 S RAILROAD ST * Palmyra Interfaith Manor AFA - Auto Fire Alarm BOX 1-01 FG 3 W1 E2
 North Cornwall Township 900 TUCK ST * Manor Care Med Class1 Altered Mental Status Box 190-8 193MU
 City of Lebanon LEHMAN ST N 7TH ST MV - Accident w/Injuries Box 15-06 E15-2 R24 AmbCo190
 City of Lebanon S 7TH ST CHESTNUT ST MV - Accident w/Injuries Box 15-03 E15-1 SQ22 R24 AmbCo190
 City of Lebanon 544 N 8TH ST Med Class3 302 Commitment Assist MHMR Box 190-2 A198 
 Heidelberg Township 3 GLASSMOYER LN WF - Wildfire/Brush Fire Box 35-05 FG3 **Second Alert** A160 
 Swatara Township 68 SUPERVISORS DR AREA OF WF - Wildfire/Brush Fire Box 47-05 FG-3 E10 R47 AT47 PT47 AmbCo190 
 South Lebanon Township 518 S 5TH ST SUPPORTIVE CONCEPTS Med Class3 Abdominal Pain Box 190-4 A198
 
 Contact: Clinton Masr <mastclinton@gmail.com>
 Sender: km911alert@gmail.com
 (FASP@20:13) City of Lebanon CUMBERLAND ST N 9TH ST MV - Accident w/Injuries Pedestrian Struck Box 15-04 Class 1 for EMS BC190 \n\nTo unsubscribe reply STOP
 
 Contact: nevin weaver <weavmy36@gmail.com>
 Sender: km911alert@gmail.com
 Subject:Sta 29@12:50\nSouth Lebanon Twp 2618 KING ST MI - Miscellaneous Wire Down FG 3 E29 Fire-Box 29-01 EMS-Box 140-1\r\n\r\nTo unsubscribe r\r
 
 Contact: "Roger Funck" <rogerfunck@verizon.net>
 Sender: 7176798487
 Sender: km911alert@gmail.com
 East Hanover Township 7 TOWER LN AREA OF MV - Accident w/Injuries fg 3 E12  Fire-Box 12-03 EMS-Box 190-16
 North Annville Township BLACKS BRIDGE RD HOSTETTER LN AREA OF SF - Dwelling Fire Barn Fire FG3 E6 R5 QRS6 E7 E12 E75 TK12 SQ12 T6 T6-1 T7 PT47 AmbCo19
 North Annville Township BLACKS BRIDGE RD HOSTETTER LN AREA OF BF - Barn Fire Barn Fire FG3  Box is In-Service Auth Dep 6 SQ12 T6 T6-1  Fire-Box 6-03 E
 (Sta 12@12:30) East Hanover Twp DAUPHIN COUNTY 257 BOW CREEK RD=AREA OF BF - Barn Fire FG 5 E1-1 TK12 Fire-Box EMS-Box\n\nTo unsubscribe reply STOP
 
 Contact: Thomas Miller <rattielover35@yahoo.com>
 Sender: km911alert@gmail.com
 (Sta 2@19:42) DAUPHIN EAST HANOVER 123 AUTOMATIC LN SF - Dwelling Fire WORKING STRUCTURE FIRE T2 Fire-Box EMS-Box\n\nTo unsubscribe reply STOP
 
 */
 
 public class PALebanonCountyParser extends SmartAddressParser {
   
   private static final Pattern[] CITY_PTNS = new Pattern[]{
     Pattern.compile("^(.* Township) "),
     Pattern.compile("^(.* Twp) "),
     Pattern.compile("^City of ([^ ]*) "),
    Pattern.compile("^(.*) Borough ")
   };
   private static final String[] DAUPHIN_EAST_HANOVER_STRINGS = new String[]{
     "DAUPHIN EAST HANOVER "
   };
   private static final Pattern COUNTY_PTN = Pattern.compile("^[^ ]+ COUNTY\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern CALL_PREFIX_PTN =
       Pattern.compile(" (?:Med Class(\\d) |([A-Z]{2,3} - ))");
   private static final Pattern BOX_PTN = 
       Pattern.compile(" (?:(?:Box|BOX) ?([0-9\\-]+)|Fire-Box ([0-9\\-]+) EMS-Box ([0-9\\-]+)|Fire-Box EMS-Box)");
   private static final Pattern TAIL_CLASS_PTN = Pattern.compile("^Class (\\d) for EMS ");
   private static final Pattern UNIT_PTN = Pattern.compile(" +([A-Z]+[0-9]+(?:-[0-9]+)?|[0-9]+[A-Z]+|FG[ -]?\\d)$", Pattern.CASE_INSENSITIVE);
 
   public PALebanonCountyParser() {
     super("LEBANON COUNTY", "PA");
   }
   
   @Override 
   public boolean parseMsg(String body, Data data) {
     
     int pt = body.indexOf('\n');
     if (pt >= 0) body = body.substring(0,pt).trim();
 
     // Look for city, borough, or township at start of text
     for (Pattern ptn : CITY_PTNS) {
       Matcher match = ptn.matcher(body);
       if (match.find()) {
         data.strCity = match.group(1).toUpperCase();
         body = body.substring(match.end()).trim();
         break;
       }
     }
     if (data.strCity.length() == 0) {
       for (String prefix : DAUPHIN_EAST_HANOVER_STRINGS) {
         if (body.toUpperCase().startsWith(prefix)) {
           data.strCity = "East Hanover Twp, DAUPHIN COUNTY";
           body = body.substring(prefix.length()).trim();
           break;
         }
       }
       if (data.strCity.length() == 0) return false;
     }
     
     // Check for county qualifier
     Matcher match = COUNTY_PTN.matcher(body);
     if (match.find()) {
       data.strCity = data.strCity + ", " + match.group();
       body = body.substring(match.end()).trim();
     }
     
     match = CALL_PREFIX_PTN.matcher(body);
     if (!match.find()) return false;
     String sAddress = body.substring(0,match.start()).trim();
     data.strPriority = getOptGroup(match.group(1));
     String sCallPfx = match.group(2);
     String sTail = body.substring(match.end());
 
     String sCall;
     match = BOX_PTN.matcher(sTail);
     if (! match.find()) {
       sCall = sTail;
       sTail = "";
     } else {
       sCall = sTail.substring(0,match.start()).trim();
       String sBox = match.group(1);
       if (sBox == null) {
         sBox = match.group(2);
         if (sBox != null) {
           sBox = "Fire:" + match.group(2) + " EMS:" + match.group(3);
         } else {
           sBox = "";
         }
       }
       data.strBox = sBox;
       sTail = sTail.substring(match.end()).trim();
     }
     data.strCall = (sCallPfx == null ? "" : sCallPfx) + sCall;
     
     pt = sAddress.indexOf('=');
     if (pt >= 0) {
       data.strPlace = sAddress.substring(pt+1).trim();
       sAddress = sAddress.substring(0,pt).trim();
       parseAddress(StartType.START_ADDR, FLAG_IMPLIED_INTERSECT | FLAG_ANCHOR_END, sAddress, data);
     } else {
       parseAddress(StartType.START_ADDR, FLAG_IMPLIED_INTERSECT, sAddress, data);
       data.strPlace = getLeft();
     }
     if (data.strPlace.startsWith("* ")) data.strPlace = data.strPlace.substring(2).trim();
     
     match = TAIL_CLASS_PTN.matcher(sTail);
     if (match.find()) {
       data.strPriority = match.group(1);
       sTail = sTail.substring(match.end()).trim();
     }
     data.strUnit = sTail;
     
     // If there was no unit specified in tail section, try extracting it from end
     // of call description
     if (data.strUnit.length() == 0) {
       while (true) {
         match = UNIT_PTN.matcher(data.strCall);
         if (!match.find()) break;
         data.strUnit = append(match.group(1).toUpperCase(), " ", data.strUnit);
         data.strCall = data.strCall.substring(0,match.start()).trim();
       }
     }
     
     return true;
   }
 
 }
