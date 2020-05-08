 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Montgomery County, NY
 Contact: Justin Cramer <cramerjd@gmail.com>
 Sender: messaging@iamresponding.com
 
 Subject:HVFD 218\n EMS - EMS CALL  14 PINE ST AMSTERDAM CITY  09/24/2011 1223\n\n\n
 Subject:HVFD 218\nMOTOR VEHICLE ACCIDENT   STATE HWY 30 T AMSTERDAM AMSTERDAM COMMONS 09/25/2011 0851\n\n\n
 Subject:HVFD 218\nEMS - EMS CALL  300 HAMMOND TOWN RD AMSTERDAM  09/25/2011 1015\n\n\n
 Subject:HVFD 218\nEMS - EMS CALL  106 HANNAFORD PLZ AMSTERDAM  09/25/2011 1945\n\n\n
 Subject:HVFD 218\nEMS - EMS CALL  5005 STATE HWY 30 AMSTERDAM  09/25/2011 2250\n\n\n
 Subject:HVFD 218\nEMS - EMS CALL  10 MAIN ST HAGAMAN VILLAGE  09/26/2011 0759\n\n\n
 Subject:HVFD 218\nMOTOR VEHICLE ACCIDENT WALMART 101 SANFORD FARM SHOP CT AMSTERDAM  09/26/2011 1011\n\n\n
 Subject:HVFD 218\nMOTOR VEHICLE ACCIDENT RECOVERY SPORTS GRILL 5005 STHWY 30 AMSTERDAM  09/26/2011 1331\n\n\n
 Subject:HVFD 218\nMOTOR VEHICLE ACCIDENT   STATE HWY 30 T AMSTERDAM AMSTERDAM COMMONS 09/25/2011 0851
 
Contact: Kenneth LaBelle <klabelle65@gmail.com>
Sender: paging@impact-sys.com
FIRE - SMOKE CALL  1788 BURTONVILLE RD CHARLESTON  01/13/2012 0533

 */
 
 
 public class NYMontgomeryCountyParser extends SmartAddressParser {
   
   private static final Pattern SUBJ_PTN = Pattern.compile("[A-Z]{4} *\\d*");
   private static final Pattern TAIL_PTN = Pattern.compile(" \\d\\d/\\d\\d/\\d{4} (\\d+)$");
   
   public NYMontgomeryCountyParser() {
     super(CITY_LIST, "MONTGOMERY COUNTY", "NY");
   }
   
   @Override
   public String getFilter() {
    return "paging@impact-sys.com,messaging@iamresponding.com";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (SUBJ_PTN.matcher(subject).matches()) data.strSource = subject;
     Matcher match = TAIL_PTN.matcher(body);
     if (!match.find()) return false;
     data.strCallId = match.group(1);
     body = body.substring(0,match.start()).trim();
     
     StartType st = StartType.START_CALL;
     int pt = body.indexOf("  ");
     if (pt >= 0) {
       data.strCall = body.substring(0,pt);
       body = body.substring(pt+2).trim();
     }
     
     for (String call : CALL_LIST) {
       if (body.startsWith(call)) {
         data.strCall = call;
         body = body.substring(call.length()).trim();
         st = StartType.START_PLACE;
       }
     }
     
     parseAddress(st, body, data);
     data.strPlace = append(data.strPlace, " - ", getLeft());
     
     data.strAddress = data.strAddress.replace("SHOP CT", "SHOPPING CTR");
     return true;
   }
   
   private static final String[] CALL_LIST = new String[] {
     "EMS",
     "MOTOR VEHICLE ACCIDENT"
   };
   
   private static final String[] CITY_LIST = new String[] {
     "AMES VILLAGE",
     "AMSTERDAM CITY",
     "AMSTERDAM",
     "CANAJOHARIE",
     "CANAJOHARIE VILLAGE",
     "CHARLESTON",
     "FLORIDA",
     "FONDA VILLAGE",
     "FORT JOHNSON VILLAGE",
     "FORT PLAIN VILLAGE",
     "FULTONVILLE VILLAGE",
     "GLEN",
     "HAGAMAN VILLAGE",
     "MINDEN",
     "MOHAWK",
     "NELLISTON VILLAGE",
     "PALATINE BRIDGE VILLAGE",
     "PALATINE",
     "ROO",
     "ST JOHNSVILLE",
     "ST JOHNSVILLE VILLAGE"
   };
   
 }
 	
