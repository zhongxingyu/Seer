 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /******************************************************************************
 Queen Anne's County, MD
 
 Contact: Contact: David baker <baker.davidm@gmail.com> ?
 qac911:*D 4-2 COMMERCIAL BLDG FIRE DUNKIN DONUTS @500 CHURCH HILL RD COMMERCIAL BOX Q04
 qac911:*D 4-5 MVC W/ENTRAPMENT RT301NB / RT304 RESCUE BOX Q04
 qac911:*D 5-1 LG BRUSH/GRASS FIRE RT405 / WINDSWEPT FARM LN BRUSH BOX Q05
 qac911:*D 4-5 UNCONSCIOUS/FAINTING RT301NB / ROLLING BRIDGE RD UNCONSCIOUS Q04
 qac911:*D 5-2 MVC W/ROLLOVER RT301NB / HAYDEN RD RESCUE BOX Q05
 qac911:*D 4-7 MVC W/ROLLOVER 1627 RUTHSBURG RD RESCUE BOX Q04
 (text message) qac911:*D 4-2 CO ALARM 212 NORTHFIELD WAY LOCAL BOX Q04
 
 Contact: david baker <baker.davidm@gmail.com>
 sender:QA911com@qac.org <From%3AQA911com@qac.org>
 (Text Message) QA911com:*D 4-1 UNKNOWN PROBLEM 206 ORCHESTRA PL LIFE STATUS ? Q04
 
 Contact: Joshua Ruby <josh@computerrescuemd.com>
 sender: QA911com@qac.org
 (Text Message) QA911com:*D 1-14 FALLS 1401 CALVERT RD POSS DANG BODY AREA Q01
 
 Contact: jahurlock@cvfd7.com <jahurlock@cvfd7.com>
 Sender: QA911com@qac.org 
 (Text Message) QA911com:*G KM06 MUTUAL AID MEDICAL 300 WASHINGTON AVE MUTUAL AID ONLY KM06
 
 Kent Island VFD
 Contact: Joshua Ruby <josh@computerrescuemd.com>
 [Text Message]  QA911com:*D 1-15 CHEST PAINS 1630 MAIN ST BREATHING NORMAL>35 Q01
 [Text Message]  QA911com:*D 1-9 SICK PERSON 402 CASTLE MARINA RD NOT ALERT Q01 
 [Text Message]  QA911com:*D 1-3 HEART PROBLEMS 115 SALLITT DR ABNORMAL BREATHING Q01
 [Text Message]  QA911com:*D 1-10 SEIZURES 1918 ANCHORAGE DR CONTINUOUS/MULTIPLE Q01
 [Text Message]  QA911com:*D 1-9 ODOR OF SMOKE INSIDE 44 F QUEEN NEVA CT COMMERCIAL BOX Q01
 [Text Message]  QA911com:*D 1-15 HEART PROBLEMS 1630 MAIN ST CARDIAC HISTORY Q01
 [Text Message]  QA911com:*D 1-4 DROWNING/DIVE ACCDNT 624 ROMANCOKE RD WATER RESCUE BOX Q01
 
 Contact: Chad Angelini <chadaangelini@verizon.net>
 (Text Message) QA911com:*D 6-1 CHEST PAINS 104 CHARLES ST DIFF SPEAKING Q06
 
 Contact: Brett VanZant <chvfd5@gmail.com>
 Sender: rc.165@c-msg.net
 (CAD) [CAD] QA911com:*D 4-1 COMMERCIAL BLDG FIRE 160 COURSEVALL DR @QAC PLANNING & ZONING COMMERCIAL BOX Q04
 
 Contact: "T.J. Palmatary" <tjpalmatary@gmail.com>
 [CAD] D 4-11 ABDOMINAL PAINS 1130 BURRISSVILLE RD Q04
 
 Contact: "jahurlock@cvfd7.com" <jahurlock@cvfd7.com>
 [CAD] D KENT MUTUAL AID MEDICAL 30564 CHESTERVILLE BRIDGE RD MUTUAL AID ONLY KENT
 [CAD] D 7-5 UNKNOWN PROBLEM 304 MERGANSER DR MEDICAL ALARM Q07
 
 Contact: ffmedicmo@gmail.com
 1-9 HEART PROBLEMS 1630 MAIN ST CARDIAC HISTORY Q01
 
 Contact: Josh Ruby <firefighter_josh@hotmail.com>
 Sender: rc.579@c-msg.net
 1-3 SEIZURES 380 LOG CANOE CIR @PRS GUITARS <35 NOT VERIFIED Q01
 
 ******************************************************************************/
 
 public class MDQueenAnnesCountyParser extends SmartAddressParser {
   
   private static final Pattern MARKER = Pattern.compile("^(?:(?:qac911|QA911com):\\*)?[DG] ");
   private static final Pattern BOX_PTN = Pattern.compile("\\b(?:BOX )?([A-Z]{1,2}\\d{2})$");
   private static final Pattern CALL_PTN = Pattern.compile("(?:\\d{1,2}-\\d{1,2} |(.*) MUTUAL AID\\b).*");
   
   public MDQueenAnnesCountyParser() {
     super("QUEEN ANNES COUNTY", "MD");
   }
   
   @Override
   public String getFilter() {
     return "qac911@qac.org,QA911com@qac.org,@c-msg.net";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     Matcher match = MARKER.matcher(body);
     if (match.find()) body = body.substring(match.end());
     
     // OK, go do your magic!!
     parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_AT_BOTH, body, data);
     
     // Call description has to match one of two specific patterns
     match = CALL_PTN.matcher(data.strCall);
     if (!match.find()) return false;
     String city = match.group(1);
     if (city != null && city.equalsIgnoreCase("KENT")) data.strCity = city;
     
     // Parse box number from what is left
     String sExtra = getLeft();
     match = BOX_PTN.matcher(sExtra);
     if (match.find()) {
       data.strBox = match.group(1);
       sExtra = sExtra.substring(0, match.start()).trim();
     } 
 
     // BOX is required, unless this was a mutual aid call
     else {
       if (city == null) return false;
     }
     
     // What is left is usually supplemental info.  But if the smart address parser
     // picked a place name from the end of the the address, just append what is
     // left to that
     if (data.strPlace.length() > 0) {
       data.strPlace = data.strPlace + " " + sExtra;
     } else {
       data.strSupp = sExtra;
     }
     
     return true;
   }
 }
