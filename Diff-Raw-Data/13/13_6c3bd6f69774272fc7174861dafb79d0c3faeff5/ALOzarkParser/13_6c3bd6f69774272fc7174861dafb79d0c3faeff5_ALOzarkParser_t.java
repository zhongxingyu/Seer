 package net.anei.cadpage.parsers.AL;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Ozark, AL
 Contact: John Price <jprice911@yahoo.com>
 Sender: dispatch@ozarkdale911.org
 
 (ALERT!) "MEDICAL EMERGENCY"6491 E ANDREWS AVE,REF DIABETIC UNABLE TO EAT & VOIMITTING
 (ALERT!) SERVICE CALL 192 CEDAR CT OFF PINE AVE OFF NEWTON AVE REF LIFTING ASSISTANCE PER MEDIC 1
 (ALERT!) MED EMER 145 PATTERSON OFF E EUFAULA, MALE SUBJECT SEVERAL LAC TO ARM, BLEEDING HEAVILY
 (ALERT!) DELAY PAGE.DOUGLAS DR OFF E HWY 27,REF SUBJECT STABBED,UNITS STAG FOR OPD
 (ALERT!) MEDICAL EMER 135 JUDSON DR OFF WILL LOGAN RD REF PATIENT CONFUSED AND CLAMY
 
Contact: John Price <jprice911@yahoo.com>
Sender: dispatch1@ozarkdale911.org
MEDICAL EMERGENCY AT 198 HOLIDAY LANE REF MEDICAL ALARM UNKN INJURIES\nSABRINA PETERS

 */
 public class ALOzarkParser extends SmartAddressParser {
   
   private static final Pattern DELIM = Pattern.compile("[ ,](OFF|REF) ");
   private static final Pattern CALL_DELIM = Pattern.compile("[\\.,\"]");
   
   public ALOzarkParser() {
     super("OZARK", "AL");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@ozarkdale911.org";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    boolean confirm = subject.equals("ALERT!");
     
    body = body.replace('\n', ' ');
     Matcher match = DELIM.matcher(body);
     String lastDelim = "";
     int lastPt = 0;
     String sAddr = "";
     while (match.find()) {
       String field = body.substring(lastPt, match.start()).trim();
       if (lastPt == 0) sAddr = field;
       else data.strCross = append(data.strCross, " & ", field);
       lastPt = match.end();
       lastDelim = match.group(1);
       if (lastDelim.equals("REF")) break;
     }
     
     // If there was no REF key, use the first comma instead
    // Not acceptable if we haven't confirmed this is a page with the proper subject
     if (! lastDelim.equals("REF")) {
      if (!confirm) return false;
       int pt = body.indexOf(',', lastPt);
       if (pt >= 0) {
         lastDelim = "REF";
         String field = body.substring(lastPt, pt).trim();
         if (lastPt == 0) sAddr = field;
         else data.strCross = append(data.strCross, " & ", field);
         lastPt = pt+1;
       }
     }
     
     // deal with last segment of text
     String field = body.substring(lastPt).trim();
     if (lastPt == 0) sAddr = field;
     else if (lastDelim.equals("REF")) data.strSupp = field;
     else data.strCross = append(data.strCross, " & ", field);
 
     // Next we have to figure out a how to separate the call from the address
     // It might be enclosed in double quotes, or marked by a period or comma
     boolean quote = sAddr.startsWith("\"");
     if (quote) sAddr = sAddr.substring(1);
     StartType sType = StartType.START_CALL;
     match =  CALL_DELIM.matcher(sAddr);
     if (match.find()) {
       sType = StartType.START_ADDR;
       data.strCall = sAddr.substring(0, match.start()).trim();
       sAddr = sAddr.substring(match.end()).trim();
     }
     
     // We may have already found a call description at the front of the address
     // We may already have identified the end of the the address and not need
     // to extract a long description from the end of it
     // But it all works out to some kind of call to the smart address parser
     parseAddress(sType, (lastPt == 0 ? 0 : FLAG_ANCHOR_END), sAddr, data);
     if (lastPt == 0) data.strSupp = getLeft();
     return true;
   }
 }
