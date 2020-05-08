 package net.anei.cadpage.parsers.GA;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Bulloch County, GA
 Contact: "maleman876@gmail.com" <maleman876@gmail.com>
 Contact: Andrew Freeman <andrewfreemanemail@gmail.com>
 Sender: bullochga911@smtp.sgcce-inc.com
 
 (STRUCTURE FIRE) STRUCTURE FIRE\nCLYDES #47\n513 BROOKLET EAST\nBURNING SMELL FROM FUSE BOX
 (STRUCTURE FIRE) EAST LEE STREET ACROSS FROM BROOKLET POLICE DEPARTMENT\nSTRUCTURE FIRE
 (361 METTS RD) 361 METTS RD\nSTRUCTURE FIRE\nSTOVE AREA
 (STRUCUTRE FIRE) 715 CAMDEN CT DRYER ON FIRE  STATION 8 DIST
 (WOODS FIRE) WOODS FIRE\nBEHIND OAK HILL TRLR PK\n25213 N HIGHWAY 301
 (SMOKE) HEAVY SMOKE AROUND LANE RD
 (STRUCTURE FIRE) 2 RED BREAST LN   OFF OF OLD RIVER RD NORTH UNFAMILIAR ON\nENTRAPMENT ALSO WOODS ON FIRE IN THE AREA OF SHUMAN RD
 (WOODS FIRE) SHUMAN RD OFF OF OLD RIVER RD  NORTH
 
 */
 public class GABullochCountyParser extends SmartAddressParser {
   
   private static final Pattern START_NUMBER = Pattern.compile("^\\d+ ");
   
   public GABullochCountyParser() {
     super("BULLOCH COUNTY", "GA");
     addExtendedDirections();
   }
   
   @Override
   public String getFilter() {
     return "bullochga911@smtp.sgcce-inc.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     // There has to be a subject, which will turn out to be either the address
     // or the call description
     if (subject.length() == 0) return false;
     
     // String is broken up by newlines, and we won't accept strigns with less
     // than segments
     String flds[] = body.split("\n");
     //if (flds.length < 3) return false;
     
     // Now things get tricky.  See if we can find a line that looks like an 
     // address.  We will make two passes, one expecting the address to fill the
     // entire line, and a second looking for a line that contains an address
     Result res[] = new Result[flds.length];
     int bestStat = -1;
     int bestJ = -1;
     for (int pass = 0; pass <= 1; pass++) {
       StartType st = (pass == 0 ? StartType.START_ADDR : StartType.START_PLACE);
       int flags = (pass == 0 ? FLAG_ANCHOR_END | FLAG_CHECK_STATUS : 0);
       for (int j = 0; j<flds.length; j++) {
         flds[j] = flds[j].trim();
         res[j] = parseAddress(st, flags, flds[j]);
         int stat = res[j].getStatus();
         if (pass == 1) {
           if (stat > 0 || START_NUMBER.matcher(flds[j]).find()) stat++;
         }
         if (stat > bestStat) {
           bestStat = stat;
           bestJ = j;
         }
       }
       if (bestStat > 0) break;
     }
     
     // If we didn't find an address in any of this, bail out
     if (bestStat <= 0) return false;
     
     // If the subject matches the address line
     if (!subject.equals(flds[bestJ])) data.strCall = subject;
     
     // Now make another pass through the fields
     for (int j = 0; j<flds.length; j++) {
       
       // Is this the address line
       if (j != bestJ) {
         
         // No, ignore it if it duplicates the call description from the subject
         if (! flds[j].equalsIgnoreCase(data.strCall)) {
           
           // Otherwise, anything with a # goes to apartment, anything lese
           // got to supplemental info
           if (flds[j].contains("#")) {
             data.strApt = flds[j];
           } else {
             addInfo(flds[j], data);
           }
         }
       } else {
         
         // If this is the address line, parse the address fields
         // with any leading and trailing info going to supplemental info
         res[bestJ].getData(data);
         addInfo(data.strPlace, data);
         data.strPlace = "";
         addInfo(res[bestJ].getLeft(), data);
       }
     }
     
     return true;
   }
   
   private void addInfo(String field, Data data) {
     if (data.strCall.length() == 0) data.strCall = field;
     else data.strSupp = append(data.strSupp, " / ", field);
   }
 }
