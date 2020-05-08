 package net.anei.cadpage.parsers.WV;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.MsgParser;
 
 /*
 Kanawha County, WV
 Contact: support@active911.com
 Sender: METRO911@metro911.org
 (Message Forwarded by PageGate)  FIRE ALARM reported at 95 RHL BLVD in SOUTH CHARLESTON on 02/10/12 15:00
 (Message Forwarded by PageGate)  DECREASED LOC (SEMI-UNRESPONSIVE) reported at 2700 MOUNTAINEER BLVD in SOUTH CHARLESTON on 02/10/12 18:43
 (Message Forwarded by PageGate)  SHORTNESS OF BREATH reported at 5218 1/2 INDIANA ST in SOUTH CHARLESTON on 02/10/12 19:03
 (Message Forwarded by PageGate)  FIRE ALARM reported at 3 EAGLE DR in SOUTH CHARLESTON on 02/11/12 12:28
 (Message Forwarded by PageGate)  AUTO ACCIDENT WITH INJURIES reported at 56 I64 EAST in SOUTH CHARLESTON on 02/11/12 07:57
 (Message Forwarded by PageGate)  CARDIAC ARREST reported at 24 MACCORKLE AVE in SOUTH CHARLESTON on 02/11/12 11:56
 (Message Forwarded by PageGate)  CHEST PAINS reported at 4610 YOUNG ST in SOUTH CHARLESTON on 02/10/12 22:35
 (Message Forwarded by PageGate)  DOMESTIC WITH INJURIES reported at 4991 COLONIAL PARK DR in SOUTH CHARLESTON on 02/11/12 20:08
 (Message Forwarded by PageGate)  FIRE ALARM reported at 331 SOUTHRIDGE BLVD in CHARLESTON on 02/12/12 16:36
 (Message Forwarded by PageGate)  HAZARDOUS MATERIALS INCIDENT reported at 46 RHL BLVD in SOUTH CHARLESTON on 02/17/12 09:13
 (Message Forwarded by PageGate)  UNRESPONSIVE PATIENT reported at 1232 THOMAS RD in SOUTH CHARLESTON on 02/17/12 10:19
 (Message Forwarded by PageGate)  SHORTNESS OF BREATH reported at 4718 KANAWHA AVE in SOUTH CHARLESTON on 02/15/12 07:20
 (Message Forwarded by PageGate)  DIABETIC PATIENT reported at 2905 MACON ST in SOUTH CHARLESTON on 02/15/12 10:45
 (Message Forwarded by PageGate)  INJURED PERSON reported at 15 RIVER WALK MALL in SOUTH CHARLESTON on 02/15/12 11:57
 (Message Forwarded by PageGate)  UNRESPONSIVE PATIENT reported at 46 RHL BLVD in SOUTH CHARLESTON on 02/17/12 16:10
 
 */
 public class WVKanawhaCountyParser extends MsgParser {
   
  private static final Pattern MASTER = Pattern.compile("(.+?) reported at (.+?) in (.+?) on (\\d\\d/\\d\\d/\\d\\d) (\\d\\d:\\d\\d)");
   
   public WVKanawhaCountyParser() {
     super("KANAWHA COUNTY", "WV");
   }
   
   @Override
   public String getFilter() {
     return "METRO911@metro911.org";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    
    if (!subject.equals("Message Forwarded by PageGate")) return false;

     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strCall = match.group(1).trim();
    parseAddress(match.group(2).trim(), data);
     data.strCity = match.group(3).trim();
     data.strDate = match.group(4);
     data.strTime = match.group(5);
     return true;
   }
 }
