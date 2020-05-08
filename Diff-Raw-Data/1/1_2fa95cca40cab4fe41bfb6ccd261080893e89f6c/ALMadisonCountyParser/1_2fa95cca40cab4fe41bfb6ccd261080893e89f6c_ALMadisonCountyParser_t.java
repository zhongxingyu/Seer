 package net.anei.cadpage.parsers.AL;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Madison County, AL
 Contact: Mark Wehrwein <heyblue17@gmail.com>
 Sender: cad.page@madco911.com
 
 IPS I/Page Notification / Loc: 7583 WALL TRIANA HWY MDCO: @URGENT MEDCARE EVT#: C1009726 TYPE: M TIME: 13:36:55
 IPS I/Page Notification / Loc: 2525 OLD RAILROAD BED RD MDCO EVT#: C1009579 TYPE: M TIME: 16:53:25
 IPS I/Page Notification / Loc: HIGHWAY 53/KELLY SPRING RD HSV: alias HWY 53/KELLY SPRING EVT#: C1009577 TYPE: MVA/I TIME: 14:12:44
 IPS I/Page Notification / Loc: 101 VASSER CIR MDCO EVT#: C1009575 TYPE: M TIME: 13:54:54
 IPS I/Page Notification / Loc: 106 CHESAPEAKE BLVD MDCO EVT#: C1009490 TYPE: M TIME: 16:04:25
 IPS I/Page Notification / Loc: 120 EMERALD DR MDCO EVT#: C1009484 TYPE: M TIME: 12:07:46
 
 Contact: NICK GOODMAN <hemsi89@gmail.com>
 (IPS I/Page Notification) EVENT: E1105513 Loc: 116 DRYSDALE DR MDCO EVT#: E1105513 TYPE: M TIME: 08:48:15
 
 Contact: Medcomm <jddyer@rescuesquad.net>
 Sender: rescue1-bounces@rescuesquad.net
 (R1 IPS I/Page Notification) Loc: 1251 CARTERS GIN RD MDCO EVT#: E1120631 TYPE: MVA/E TIME: 01:50:42
 (R1 IPS I/Page Notification) Loc: TIMBERLANE AV NW/POPLAR AV NW HSV  EVT#: E1120841 TYPE: MVA/E TIME: 13:42:06
 
 Contact: Rick Phillips <rick81997@yahoo.com>
 Sender: cad.page@madco911.com
 Subject:IPS I/Page Notification\nLoc: 2979 OLD HIGHWAY 431 OXRD: @NO NAME: COMMERCIAL & RESIDENTAL APT COMPLEX: alias 2979 OLD HWY 431 EVT
 
  */
 
 
 public class ALMadisonCountyParser extends SmartAddressParser {
   
   private static final String CAD_MARKER = "IPS I/Page Notification";
   private static final String[] KEYWORDS = 
     new String[]{"EVENT", "Loc", "EVT#", "TYPE", "TIME"};
   
   private static final Properties CITY_TABLE = buildCodeTable(new String[]{
       "MDCO",  "MADISON COUNTY",
       "HSV",   "HUNTSVILLE",
       "OXRD",  "OWENS CROSS ROADS"
   });
   
   private static final Properties TYPE_CODES = buildCodeTable(new String[]{
       "UNKMED",         "UNKNOWN MEDICAL",
       "M",              "MEDICAL",
       "FR",             "FIRE/RESCUE",
       "MVA/I",          "MVA W/ INJURY",
       "F/ALARMS",       "FIRE ALARM",
       "ALM/M",          "MEDICAL ALARM",
       "MVA/E",          "MVA W/ ENTRAPMENT",
       "F/OUTSIDE_FIRE", "OUTSIDE FIRE",
       "F/STRUCTURE",    "STRUCTURE FIRE"
   });
   
   public ALMadisonCountyParser() {
     super(CITY_TABLE, "MADISON COUNTY", "AL");
   }
   
   @Override
   public String getFilter() {
     return "cad.page@madco911.com,rescue1-bounces@rescuesquad.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (! subject.contains(CAD_MARKER)) {
      if (! body.startsWith(CAD_MARKER + " / ")) return false;
       body = body.substring(CAD_MARKER.length()+3);
     }
    
     body = body.replaceAll(" alias ", " @");
     Properties props = parseMessage(body, KEYWORDS);
     String sAddr = props.getProperty("Loc");
     if (sAddr == null) return false;
     Parser p = new Parser(sAddr);
     parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END, p.get(": @"), data);
     data.strPlace = p.get();
     
     data.strCallId = props.getProperty("EVT#", "");
     if (data.strCallId.length() == 0) data.strCallId = props.getProperty("EVENT:", "");
     data.strCall = convertCodes(props.getProperty("TYPE", ""), TYPE_CODES);
     
     return true;
   }
 }
