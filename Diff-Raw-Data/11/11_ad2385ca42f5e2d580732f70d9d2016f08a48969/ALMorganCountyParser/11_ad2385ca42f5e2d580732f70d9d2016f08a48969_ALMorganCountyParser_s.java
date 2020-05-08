 
 package net.anei.cadpage.parsers.AL;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Morgan County, AL
 Contact: Chris Phillips <cpffemt@aol.com>
 Sender: cad@morgan911.org
 
 CAD:MORGAN CO JAIL 119 LEE ST NE SEIZURES CAT1 CANAL ST NE OAK ST NE 11009615 DFS1
 CAD:707-1/2 5TH AV SE UNCONSCIOUS OR SYNCOPE CAT1 PROSPECT DR SE 4TH ST SE DFS1
 CAD:404 7TH AV SW TRAUMA WITH INJURY CAT1 1ST ST SW 2ND ST SW 11009612 DFS1
 CAD:BETTY ST SW/5TH AV SW SMOKE INVESTIGATION 11009584 DFS1
 CAD:1810 MARTIN ST SE UNCONSCIOUS OR SYNCOPE CAT1 18TH AV SE 19TH AV SE 11009594 DFS3
 CAD:GORDON DR SW/CENTRAL PKWY SW STRUCTURE FIRE 11009333 DFS1
 CAD:1002 15TH AV SW RESIDENTIAL FIRE ALARM DOUTHIT ST SW FAYE ST SW 11011144 DFS1
 CAD:400-BLK EVERETT DR SW STRUCTURE FIRE SANDLIN RD SW WILLIAMS ST SW 11011173 DFS6
 
 */
 
 
 public class ALMorganCountyParser extends SmartAddressParser {
   
  private static final Pattern MASTER = Pattern.compile("CAD:(.*?)(?: (\\d{8}))?(?: (DFS\\d))?");
   private static final Pattern CAT_PTN = Pattern.compile(" +CAT(\\d)\\b *");
 
   public ALMorganCountyParser() {
     super("MORGAN COUNTY", "AL");
   }
   
   @Override
   public String getFilter() {
     return "cad@morgan911.org";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     body = match.group(1);
     data.strCallId = getOptGroup(match.group(2));
     data.strSource = getOptGroup(match.group(3));
     
     match = CAT_PTN.matcher(body);
     if (match.find()) {
       data.strPriority = match.group(1);
       Result res = parseAddress(StartType.START_ADDR, FLAG_ONLY_CROSS | FLAG_IMPLIED_INTERSECT | FLAG_ANCHOR_END, body.substring(match.end()));
       res.getData(data);
       body = body.substring(0,match.start());
     }
     
     parseAddress(StartType.START_PLACE, body, data);
     
     // What's left is usually the call description
     // but if we didn't find a priority and cross street, it might be
     // a combine description and place name
     String left = getLeft();
     if (data.strPriority.length() == 0) {
       Result res = parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_ONLY_CROSS | FLAG_IMPLIED_INTERSECT | FLAG_ANCHOR_END, left);
       res.getData(data);
     } else {
       data.strCall = left;
     }
     return true;
   }
 }
