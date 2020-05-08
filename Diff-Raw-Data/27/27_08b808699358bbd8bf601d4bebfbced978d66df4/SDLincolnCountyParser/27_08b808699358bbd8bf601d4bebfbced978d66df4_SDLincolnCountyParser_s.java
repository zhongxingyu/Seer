 package net.anei.cadpage.parsers.SD;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Lincoln County, SD
 Contact: James Dietz <kc0usq@gmail.com>
 Sender: no-reply@ledsportal.com
 System: LEDS
 
 Subject:12-10952 - SIG1 : Injury Accident\nI-29 MM 64 \r\nPlease respond immediately. Roll over 6pts. I-29 nb south of the 64mm.\r
 Subject:12-10680 -\n48548 480TH AVE, Canton \r\nPlease respond immediately. Diabetic emergency\r
 Subject:AMB : Ambulance Call\n307 E Jefferson St # 7, Inwood eld male who has fallen\r
 Subject:12-10682 -\n903 W 5th. st #3, ca \r\nPlease respond immediately. 60 yo. f fallen and hit head.\r
 Subject:AMB : Ambulance Call\nI-29 NB at 59 MM59 MM I-29 27 YOM with severe back pain\r
 Subject:AMB : Ambulance Call\n501 S MAPLE ST, Inwood female had a baby 1 week ago passing clots\r
 Subject:AMB : Ambulance Call\n1022 N DAKOTA ST, CANTON, SD Non-ER transfer to Avera ER for behavioral evaluation\r
 Subject:Mutual Aid for Field and Hay Baler Fire\n48259 289th St\r\nPlease respond immediately.\r
 
 */
 
 public class SDLincolnCountyParser extends SmartAddressParser {
   
   private static final Pattern CALL_ID_PTN = Pattern.compile("^(\\d\\d-\\d+) -");
   private static final Pattern STANDBY_PTN = Pattern.compile("^STANDBY +(?:AT +)", Pattern.CASE_INSENSITIVE);
   private static final Pattern CITY_ST_PTN = Pattern.compile("^([A-Z ]+)\\b(?: *, *([A-Z]{2}))");
  
   public SDLincolnCountyParser() {
     super(CITY_LIST, "LINCOLN COUNTY", "SD");
   }
   
   @Override
   public String getFilter() {
     return "no-reply@ledsportal.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (subject.length() == 0) return false;
     
     boolean good = false;
     Matcher match = CALL_ID_PTN.matcher(subject);
     if (match.find()) {
       data.strCallId = match.group(1);
       subject = subject.substring(match.end()).trim();
       if (subject.length() == 0) subject = "ALERT";
       good = true;
     }
     data.strCall = subject;
     
     String info = null;
    int pt = body.indexOf('\n');
    if (pt >= 0) {
      info = body.substring(pt+1).trim();
      body = body.substring(0,pt).trim();
     }
 
     // Check for leading STANDBY qualifier
     match = STANDBY_PTN.matcher(body);
     if (match.find()) {
       data.strCall = "STANDBY: " + data.strCall;
       body = body.substring(match.end());
     }
     
     // See if there is an comma terminating the address
    pt = body.indexOf(',');
     if (pt < 0) {
       
       // Use the smart address parser to try and find and address
       // if unsuccessful, return false.  If successful, mark as good
       if (info == null) {
         parseAddress(StartType.START_ADDR, FLAG_CHECK_STATUS, body, data);
         info = getLeft();
       } else {
         parseAddress(StartType.START_ADDR, FLAG_CHECK_STATUS | FLAG_ANCHOR_END, body, data);
       }
       if (!good && getStatus() == 0) return false;
     } 
     
     else {
       
       // Otherwise, we have found an address, so parse it as best we can
       parseAddress(body.substring(0,pt).trim(), data);
       body = body.substring(pt+1).trim();
       
       // See if what is left can be identified as a city, st combination
       match = CITY_ST_PTN.matcher(body);
       if (match.find()) {
         data.strCity = match.group(1);
         data.strState = getOptGroup(match.group(2));
         if (!CITY_SET.contains(data.strCity.toUpperCase())) return false;
         body = body.substring(match.end()).trim();
         if (info == null) info = body;
         else if (body.length() > 0) return false;
       } else if (info != null) {
         data.strCity = body;
         if (!CITY_SET.contains(data.strCity.toUpperCase())) return false;
       } else {
         parseAddress(StartType.START_ADDR, FLAG_ONLY_CITY, body, data);
         if (data.strCity.length() == 0) return false;
         info = getLeft();
       }
     }
     
    if (info.startsWith("Please respond immediately.")) info = info.substring(27).trim();
     data.strSupp = info;
     
     if (data.strCity.equalsIgnoreCase("CA")) data.strCity = "CANTON";
     if (data.strCity.equalsIgnoreCase("INWOOD")) data.strState = "IA";
     return true;
   }
   
   private static final String[] CITY_LIST = new String[] {
     "BERESFORD",
     "CA",
     "CANTON",
     "FAIRVIEW",
     "HARRISBURG",
     "HUDSON",
     "LENNOX",
     "MOE",
     "NORWAY CENTER",
     "SHINDLER",
     "SIOUX FALLS",
     "TEA",
     "WORTHING",
     
     "INWOOD"
   };
   
   private static final Set<String> CITY_SET  = new HashSet<String>(Arrays.asList(CITY_LIST));
 }
