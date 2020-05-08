 package net.anei.cadpage.parsers.CA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*
 El Dorado County, CA
 Contact: Ken Pauley <ken@pauley.org>
 Sender: AEUCAD@fire.ca.gov
 
 (CAD Page) 15-May-2011/16:10:27: HAZ, ELECTRICAL: Inc# 010021: 5537 MORGAN RANCH LOOP ,GEORGETOWN  GEOCHF GEOENG M61 61; ; X: -120 50.5096  Y: 38 54.859
 (CAD Page) 19-May-2011/12:37:08: MEDICAL: Inc# 010306: 6530 WENTWORTH SPRINGS RD ,GEORGETOWN (GEORGETOWN ELEMENTARY SCHOOL) GEOCHF GEOENG M89 61; ; X: -120 49.84
 (CAD Page) 17-May-2011/01:52:37: MED, TRAFFIC COLLISION: Inc# 010121: 4800 BLACK OAK MINE RD / 4702 MARSHALL RD ,GARDEN_VALLEY  GRVCHF E2772 E251N M61 51 GEOENGN
 (CAD Page) 20-May-2011/12:16:59: MED, CODE 2: Inc# 010397: 5961 RESERVOIR RD ,GEORGETOWN  GEOCHF GEOENG M89 61; ; X: -120 51.3994  Y: 38
 (CAD Page) 20-May-2011/18:24:36: FIRE, SMOKE CHECK: Inc# 010430: 2990 GREENWOOD RD / 6272 HY 193 ,GEORGETOWN  GEOENGN M61 61; ; X: -120 50.5456  Y: 38 54.4036;
 (CAD Page) 21-May-2011/19:39:31: MEDICAL: Inc# 010537: 2850 DERRICK DR ,GREENWOOD  GRVCHF GEOENGN M25 53 E251N; ; X: -120 54.1379  Y: 38 53.9151;
 (CAD Page) 20-Jun-2011/16:46:01: MEDICAL: Inc# 013054: UNIVERSITY FALLS GATE @ =L(38.906110,-120.645832) ,GEORGETOWN  GEOCHF E33F M61 65 GEOENG; ; X: -120 38.749
 
  */
 
 
 public class CAElDoradoCountyParser extends SmsMsgParser {
   
   private static final Pattern MARKER = Pattern.compile("^\\d{1,2}-\\w{3}-\\d{4}/\\d\\d:\\d\\d:\\d\\d: ");
   private static final Pattern MASTER = Pattern.compile("^(.*?): Inc# (.*?): (.*?) ,(.*? )(?:\\((.*?)\\))? (.*?)(?:;|$)");
   
   public CAElDoradoCountyParser() {
     super("EL DORADO COUNTY", "CA");
   }
   
   @Override
   public String getFilter() {
     return "AEUCAD@fire.ca.gov";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("CAD Page")) return false;
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     
     match = MASTER.matcher(body);
     if (!match.find()) return false;
     
     data.strCall = match.group(1).trim();
     data.strCallId = match.group(2).trim();
    parseAddress(match.group(3).trim(), data);
     data.strCity = match.group(4).trim().replace('_', ' ');
    data.strPlace = null2empty(match.group(5));
     data.strUnit = match.group(6).trim();
     
     body = body.substring(match.end()).trim();
     if (body.startsWith(";")) body = body.substring(1).trim();
     if (body.endsWith(";")) body = body.substring(0,body.length()-1).trim();
     data.strGPSLoc = body;
     
     return true;
   }

  private String null2empty(String str) {
    return str == null ? "" : str.trim();
  }
 }
