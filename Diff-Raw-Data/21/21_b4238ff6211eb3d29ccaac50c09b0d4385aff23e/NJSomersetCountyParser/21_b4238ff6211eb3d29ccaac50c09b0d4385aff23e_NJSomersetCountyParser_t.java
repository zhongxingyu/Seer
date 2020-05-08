 package net.anei.cadpage.parsers.NJ;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 
 /*
 Somerset County, NJ
 Contact: Peter Archibald <parchibald27145@gmail.com>
 Contact: Ron DePasquale <rondepasquale@gmail.com>
 Sender: rc.502@c-msg.net
 [CAD]  [CAD] EFD-FD:11051201:05/12/2011 15:44:57:FIRE ALARM: FRANKLI-CARING HOUSE PROJECTS / 18 BLOOMFIELD AVE
 [CAD]  [CAD] EFD-FD:11051982:05/14/2011 11:51:28:FIRE ALARM:GENERAL: FRANKLI-HAMPTON INN / 255 DAVIDSON AVE
 [CAD]  [CAD] EFD-FD:11052014:05/14/2011 13:35:40:FIRE ALARM:ZONE-2ND FL REAR HALL SMK: FRANKLI-1165 HAMILTON ST
 [CAD]  [CAD] EFD-FD:11052016:05/14/2011 13:40:22:FIRE ALARM:GENERAL ALARM: FRANKLI-449 SKILLMANS LANE (ODD)
 [CAD]  [CAD] EFD-FD:11052407:05/15/2011 14:41:50:CO ALARM:NO ILLNESS: FRANKLI-25 MAYNARD ROAD
 [CAD]  [CAD] EFD-FD:11052545:05/15/2011 21:22:21:MVC: FRANKLI-CASTLETON AVE & EASTON AVE
 [CAD]  [CAD] EFD-FD:11052638:05/16/2011 04:39:12:FIRE ALARM:Z-GENERAL: FRANKLI-THE DEN / 700 HAMILTON ST
 [CAD]  [CAD] EFD-FD:11053024:05/16/2011 19:33:38:STRUCTURE FIRE: FRANKLI-12 NEWKIRK ROAD
 [CAD]  [CAD] EFD-FD:11053250:05/17/2011 11:56:48:FIRE ALARM:Z/GEN: FRANKLI-RETA - RENAISSANCE EMPLOYMENT / 630 FRANKLIN BLVD
 [CAD]  [CAD] EFD-FD:11053657:05/18/2011 13:30:59:STOVE/OVEN/FOOD: FRANKLI-138 ELLEN ST
 
Contact: Dan Ungar <danungar@gmail.com>
MGT-FD:11084743:07/25/2011 10:51:36:FIRE ALARM:ZONE/ GENERAL: MONTGOM-MONTGOMERY EVANGELICAL CHURCH / 246 BELLE MEAD GRIGGSTOWN ROAD
GRG-FD:11087510:07/31/2011 13:19:31:FIRE ALARM:GENERAL: FRANKLI-GRIGGSTOWN GROUP HOME / 7 HONEYMAN ST
MGT-FD:11084928:07/25/2011 17:32:48:SMOKE CONDITION: MONTGOM-952 COUNTY ROUTE 518 HWY
GRG-FD:11088457:08/02/2011 15:39:21:MV FIRE:SMOKE ONLY: FRANKLI-12 BARBIERI CT
MGT-FD:11088557:08/02/2011 18:37:19:BRUSH FIRE: MONTGOM-SPRING HILL ROAD
MGT-FD:11088946:08/03/2011 16:09:20:FIRE ALARM:SMOKE DECTORS: MONTGOM-3 LOWRY CT
GRG-FD:11089016:08/03/2011 17:54:34:FIRE ALARM:FIRST FL SMK DECTOR: FRANKLI-46 SAPPHIRE LANE
MGT-FD:11089091:08/03/2011 21:11:26:FIRE ALARM:ZONE / 363: MONTGOM-STONEBRIDGE AT MONTGOMERY / 100 HOLLINSHEAD SPRING ROAD
GRG-FD:11090214:08/06/2011 16:56:13:FIRE ALARM:SMOKE DETECTOR: FRANKLI-27 KIRBY LANE
GRG-FD:11090292:08/06/2011 21:34:50:FIRE ALARM:GENERAL: FRANKLI-3333 STATE HWY NO 27 HWY

 */
 
 public class NJSomersetCountyParser extends SmsMsgParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
      "FRANKLI", "FRANKLIN TWP",
      "MONTGOM", "MONTGOMERY TWP",
   });
   
   private static final Pattern MARKER = Pattern.compile("^([^ ]+):(\\d{8}):\\d\\d/\\d\\d/\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d:");
   private static final Pattern MASTER = Pattern.compile("(.*) ([A-Z]+)-(?! )(?:(.*) / +)?([^\\(]*)(?:\\((.*)\\))?");
   
   public NJSomersetCountyParser() {
     super("SOMERSET COUNTY", "NJ");
   }
   
   @Override
   public String getFilter() {
     return "rc.502@c-msg.net";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {

     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     data.strSource = match.group(1);
     data.strCallId = match.group(2);
     body = body.substring(match.end()).trim();
     
     match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strCall = match.group(1).trim();
     data.strCity = convertCodes(match.group(2), CITY_CODES);
     data.strPlace = null2empty(match.group(3));
     String sAddr = match.group(4).trim();
     parseAddress(sAddr, data);
     data.strSupp = null2empty(match.group(5));
     return true;
   }
 
   private String null2empty(String str) {
     return str == null ? "" : str.trim();
   }
 }
