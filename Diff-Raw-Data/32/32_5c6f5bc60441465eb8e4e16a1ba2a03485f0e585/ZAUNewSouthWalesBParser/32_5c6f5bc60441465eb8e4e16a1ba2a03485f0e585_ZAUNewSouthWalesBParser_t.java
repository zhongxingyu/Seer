 package net.anei.cadpage.parsers.ZAU;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 
 public class ZAUNewSouthWalesBParser extends SmartAddressParser {
   
   private static final Pattern DATE_TIME_MARKER = Pattern.compile("^(\\d\\d [A-Z][a-z]+ \\d{4} \\d\\d:\\d\\d:\\d\\d) ");
  private static final Pattern INCIDENT_CALL_PTN = Pattern.compile("^(?:INCID?ENT CALL(?: ([A-Z]+);?)?)? ");
   private static final Pattern RESPOND_TO_PTN = Pattern.compile("^(?:- )?RESPOND TO ");
   private static final Pattern TRAIL_DATE_PTN = Pattern.compile("\\. *[\\d/]*$");
   private static final DateFormat DATE_TIME_FMT = new SimpleDateFormat("dd MMMMMMMM yyyy HH:mm:ss");
 
   public ZAUNewSouthWalesBParser() {
     super(ZAUNewSouthWalesParser.CITY_LIST, "", "NSW", CountryCode.AU);
   }
 
   @Override
   public String getLocName() {
     return "New South Wales, AU";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = DATE_TIME_MARKER.matcher(body);
     if (!match.find()) return false;
     setDateTime(DATE_TIME_FMT, match.group(1), data);
     body = body.substring(match.end());   // Not calling trim - leading space needs to be retained
     
     // There are two different optional keywords, at least one must be present
     // INCIDENT CALL 
     match = INCIDENT_CALL_PTN.matcher(body);
     if (!match.find()) {
       data.strCall = "GENERAL ALERT";
       data.strPlace = body;
      return true;
     }
     
     data.strSource = getOptGroup(match.group(1));
     boolean good = match.group().length() > 1;
     String addr = body.substring(match.end());
     
     // and - RESPOND TO
     match = RESPOND_TO_PTN.matcher(addr);
     if (match.find()) {
       good = true;
       addr = addr.substring(match.end()).trim();
     } 
     else if (addr.startsWith("-")) addr = addr.substring(1).trim();
     
     match = TRAIL_DATE_PTN.matcher(addr);
     if (match.find()) addr = addr.substring(0,match.start()).trim();
     
     Parser p = new Parser(addr.replace(" X ", " & "));
     addr = p.getLastOptional(',');
     String city = null;
     if (addr.length() > 0) {
       if (isCity(addr)) {
         city = addr;
         addr = p.getLastOptional(',');
       }
     }
     
     if (addr.length() > 0) {
       data.strCall = p.get();
       parseAddress(StartType.START_CALL, FLAG_ANCHOR_END, addr, data);
     }
     
     else {
       parseAddress(StartType.START_CALL, FLAG_ANCHOR_END, p.get(), data);
     }
     
     if (city != null) data.strCity = city;
     if (data.strCity.equals("WINMALLEE")) data.strCity = "WINMALEE";
     
     if (good) return true;
     data.strCall = "GENERAL ALERT";
     data.strPlace = body;
     return true;
   }
 }
