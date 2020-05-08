 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /**
  * Lebanon County, PA
  */
 public class PALebanonCountyParser extends SmartAddressParser {
   
   private static final Pattern[] CITY_PTNS = new Pattern[]{
     Pattern.compile("^(.* Township) ", Pattern.CASE_INSENSITIVE),
     Pattern.compile("^(.* Twp) ", Pattern.CASE_INSENSITIVE),
     Pattern.compile("^City of ([^ ]*) ", Pattern.CASE_INSENSITIVE),
     Pattern.compile("^(.*) Borough ", Pattern.CASE_INSENSITIVE),
     Pattern.compile("^(.*) Boro ", Pattern.CASE_INSENSITIVE)
   };
   private static final String[] DAUPHIN_EAST_HANOVER_STRINGS = new String[]{
     "DAUPHIN EAST HANOVER ",
     "DAUPHIN CO/EAST HANOVER "
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
     if (body.startsWith("LANCASTER ")) body = body.substring(10).trim();
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
     if (data.strPlace.startsWith ("AT ")) {
       data.strCross = data.strPlace.substring(3).trim();
       data.strPlace = "";
     } else if (data.strPlace.startsWith("* ")) {
       data.strPlace = data.strPlace.substring(2).trim();
     }
     
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
