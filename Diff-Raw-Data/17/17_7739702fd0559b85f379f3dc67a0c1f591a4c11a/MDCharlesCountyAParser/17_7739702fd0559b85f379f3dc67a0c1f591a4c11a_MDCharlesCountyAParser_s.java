 //Sender: rc.263@c-msg.net
 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 
 public class MDCharlesCountyAParser extends SmartAddressParser {
   
   private static final Pattern UNIT_PATTERN = Pattern.compile("(?:,? +(?:EMS|ALS|BLS|APPARATUS|TRUCK|AMBULANCE|MOTORCYCLE|ATV|BICYCLE|BIKE|MISC|\\d{1,2}[A-D]))+\\b");
   private static final Pattern MAP_PATTERN = Pattern.compile("\\b\\d{1,2} [A-Z]\\d{1,2}(?:-[A-Z]\\d{1,2})?\\b");
   private static final Pattern ID_PATTERN = Pattern.compile("\\bF\\d{9}\\b");
   
   @Override
   public String getFilter() {
     return "rc.263@c-msg.net,dispatch@ccso.us,@sms.mdfiretech.com";
   }
 
 
   public MDCharlesCountyAParser() {
     super( "CHARLES COUNTY", "MD");
   }
   
   @Override
   public int getMapFlags() {
     return MAP_FLG_SUPPR_LA;
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     boolean good = false;
     if (subject.equals("*CAD*|CAD")) good = true;
     
     Matcher match = ID_PATTERN.matcher(body);
     if (match.find()) {
       good = true;
       data.strCallId = body.substring(match.start(),match.end());
       body = body.substring(0,match.start()).trim();
     }
     
     // Either a CAD subject or a pattern match is good enough to classify this
     // as a CAD page.  But even if neither is found, this could be a possible
     // page.  But at this point the identification is so weak we will only pursue
     // it if caller has identified this as a dispatch message
     if (!good && !isPositiveId()) return false;
     
     // There is almost always a code pattern (or whatever it really is)
     // marking the end of the address
     int mapSt = -1;
     int mapEnd = -1;
     match = MAP_PATTERN.matcher(body);
     if (match.find()) {
       mapSt = match.start();
       mapEnd = match.end();
     }
     
     // And we can usually find a unit pattern marking the beginning of the address
     // There might be more than one, pick the last one in front of the code pattern
     int unitSt = -1;
     int unitEnd = -1;
     match = UNIT_PATTERN.matcher(body);
     while (match.find()) {
       if (mapSt >= 0 && match.end() >= mapSt) break;
       unitSt = match.start();
       unitEnd = match.end();
     }
     
     // If there was a code match, strip off the code and trailing info
     if (mapSt >= 0) {
       data.strMap = body.substring(mapSt, mapEnd);
       data.strSupp = body.substring(mapEnd).trim();
       body = body.substring(0,mapSt).trim();
     }
     
     // If there was a unit match, strip off the leading call description and unit
     if (unitSt >= 0) {
       data.strUnit = body.substring(unitSt, unitEnd).trim();
       if (data.strUnit.startsWith(", ")) data.strUnit = data.strUnit.substring(2).trim();
       data.strCall = body.substring(0, unitSt);
       body = body.substring(unitEnd).trim();
       
     }
     
     // If we have both a unit and code mark, what is left is an address
     // with a possible comma separated place
     // Dummy loop to break out of
     do {
       if (mapSt >= 0 && unitSt >= 0) {
         Parser p = new Parser(body);
         parseAddress(p.get(','), data);
         data.strPlace = p.get();
         good = true;
         break;
       }
       
       // We have some special logic if we have the end of the address marked
       // and we have comma separated fields in what is left
       if (mapSt >= 0) {
         int pt = body.lastIndexOf(',');
         if (pt >= 0) {
           
           // Split out last field.  If it contains a valid address, make it so
           String fld1 = body.substring(0,pt).trim();
           String fld2 = body.substring(pt+1).trim();
           Result res2 = parseAddress(StartType.START_CALL, FLAG_ANCHOR_END, fld2);
           if (res2.getStatus() > 0) {
             res2.getData(data);
             data.strCall = append(fld1, ", ",  data.strCall);
             good = true;
             break;
           } 
 
           // If not, turn it into a place name and parse what is left
           data.strPlace = fld2;
           body = fld1;
         }
       } 
       
       // Otherwise we have to use the smart parser to separate out what we didn't get
       StartType start = (unitSt >= 0 ? StartType.START_ADDR : StartType.START_CALL);
       // Otherwise we have to use the smart parser to separate out what we didn't get
       int flags = (mapSt >= 0 ? FLAG_ANCHOR_END : 0);
       parseAddress(start, flags, body, data);
      good = (getStatus() > 0);
 
       if (data.strCall.endsWith(",")) data.strCall = data.strCall.substring(0, data.strCall.length()-1).trim();
       
       // If We end up with a comma in the address field, it must be separating a place name
       int pt = data.strAddress.indexOf(',');
       if (pt >= 0) {
         data.strPlace = data.strAddress.substring(pt+1).trim();
         data.strAddress = data.strAddress.substring(0,pt).trim();
       }
     } while (false);
     
     // If the place name we found looks like an apt number, move it
     if (data.strPlace.toUpperCase().startsWith("APT ")) {
       data.strApt = data.strPlace.substring(4).trim();
       data.strPlace = "";
     } else if (data.strPlace.startsWith("#")) {
       data.strApt = data.strPlace;
       data.strPlace = "";
     }
     
     return good;
   }
 }
