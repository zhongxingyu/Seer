 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /* Sample Pages.
 AMBULANCE, CONVULSIONS / SEIZURES: 12-D-2 44 YOF SEIZURES at 43 NEAL PATH, SETAUKET   O: FAIRFIELD GABLES            TRUSS . . 19:39:51
 AMBULANCE, SICK PERSON (SPECIFIC DIAGNOSIS): 56 YOF SICK 26-A-5 IFO BUILDING at 5000 ROUTE 347, East Setauket  c/s: ARROWHEAD LN SOUTH   O: KOHLS . . 14:54:20
 MISC CALL, CHIEFS INVESTIGATION: GENERAL FIRE ALARM 52C04 Residential (multiple) at 700H HEALTH SCIENCES DR, STONY BROOK  c/s: NICOLLS RD   O: CHAPIN APTS - S: 26-A-16 at 20 FAWN LN W, SOUTH SETAUKET  c/s: LONGBOW LN . . 00:20:06
 AMBULANCE, HEMORRHAGE / LACERATIONS: 21-A-1 - 61 YOM - CUT TO HAND at 8 HOLLY LN, EAST SETAUKET  c/s: CRANE NECK RD . . 16:57:21
 MISC CALL WITH RESCUE, MVA: 29-B-1U - INJURIES at C/O, Setauket  c/s: WIRELESS RD . . 16:08:02
  */
 
 public class NYSetauketParser extends SmsMsgParser {
   
  private static final Pattern MAP_CODE = Pattern.compile("\\b\\d{1,2}-[A-Z]-\\d{1,2}[A-Z]?\\b");
   
   public NYSetauketParser() {
     super("SETAUKET", "NY");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     if (! body.toUpperCase().contains("SETAUKET")) return false;
 
     body = body.replace(" at ", " LOC: ").replace(" . . ", " TIME:");
     body = "CALL: " + body;
     body = body.replace("c/s", "CROSS");
     Properties props = parseMessage(body, new String[]{"CALL","LOC","CROSS","O","TIME"});
     //data.strCall = body.substring(0,body.indexOf(":"));
     // Call Info is two parts all before the word at which we made into LOC
     
     String sAddress = props.getProperty("LOC", "");
     if (sAddress.contains(",")){
       int indx = sAddress.indexOf(",");
       data.strAddress = sAddress.substring(0,indx).trim();
       data.strCity = sAddress.substring(indx + 1).trim();
     } else {
       data.strAddress = sAddress;
     }
     String sCall = props.getProperty("CALL", "");
     int ipt = sCall.indexOf(':');
     if (ipt >= 0) {
       data.strCall= sCall.substring(0,ipt).trim();
       data.strSupp= sCall.substring(ipt+1).trim();
      Matcher match = MAP_CODE.matcher(data.strSupp);
       if (match.find()) {
        data.strMap = data.strSupp.substring(match.start(), match.end());
         data.strSupp = cutOut(data.strSupp, match.start(), match.end());
         if (data.strSupp.startsWith("- ")) data.strSupp = data.strSupp.substring(2).trim();
       }
     } else {
       data.strCall = sCall;
     }
     data.strCross = props.getProperty("CROSS", "");
 
     return true;
   }
 }
