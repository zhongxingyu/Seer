 package net.anei.cadpage.parsers.ME;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Waterboro, ME
 Contact: Sean Perkins <sperkins@waterborofire.org>
 [Page]  MEDICAL EMERGENCY26 EAST SHORE RD Waterboro7/4/2011 12:36
 [Page]  FIRE, OTHER161 BEAVER DAM RD Waterboro7/4/2011 11:43
 [Page]  MEDICAL EMERGENCYMX 270 MOTOR CROSS TRACKLyman7/3/2011 11:24 
 [Page]  MEDICAL EMERGENCY54 ROCKY RD Lyman7/2/2011 17:51 
 [Page]  FIRE, OTHERDEERING RIDGE RD.PHEASANT RUN RD Waterboro7/2/2011 03:22 
 
 */
 
 public class MEYorkCountyParser extends SmartAddressParser {
   
   private static final String[] CITY_LIST = new String[]{
     "ACTON",
     "ALFRED",
     "ARUNDEL",
     "BERWICK",
     "BIDDEFORD",
     "BUXTON",
     "CORNISH",
     "DAYTON",
     "ELIOT",
     "HOLLIS",
     "KENNEBUNK",
     "KENNEBUNKPORT",
     "KITTERY",
     "LEBANON",
     "LIMERICK",
     "LIMINGTON",
     "LYMAN",
     "NEWFIELD",
     "NORTH BERWICK",
     "OGUNQUIT",
     "OLD ORCHARD BEACH",
     "PARSONSFIELD",
     "SACO",
     "SANFORD",
     "SHAPLEIGH",
     "SOUTH BERWICK",
     "WATERBORO",
     "WELLS",
     "YORK"
   };
   
   private static final Pattern MASTER = 
     Pattern.compile("((?:FIRE|MEDICAL),? (?:EMERGENCY|OTHER))(.*)\\d\\d?/\\d\\d?/\\d{4} \\d\\d:\\d\\d");
   
   public MEYorkCountyParser() {
     super(CITY_LIST, "YORK COUNTY", "ME");
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("Page")) return false;
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strCall = match.group(1);
    String sAddr = match.group(2).trim();
     parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END, sAddr, data);
     
     // There is always a city, but sometimes there isn't a space separating it from the address :(
     if (data.strCity.length() == 0) {
       String sUpAddr = sAddr.toUpperCase();
       for (String city : CITY_LIST) {
         if (sUpAddr.endsWith(city)) {
           int pt = sAddr.length() - city.length();
           data.strAddress = "";
           parseAddress(sAddr.substring(0,pt).trim(), data);
           data.strCity = sAddr.substring(pt);
           break;
         }
       }
     }
     return true;
   }
 
 }
