 package net.anei.cadpage.parsers.VA;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
import net.anei.cadpage.parsers.SmartAddressParser;

 /*
 Henry County, VA (class I)
 reporting email: Lieutenant Pruitt <rdvrs602@ridgewayrescue.com>
 sender:  *@ridgewayrescue.com
 
 (CAD Call) 2010094339\nR2-(1) DIFFICULTY BREATHING\n999 MORGAN FORD RD RIDGEWAY
 (CAD Call) 2010094406\nRFA-(3) FALL / FRACTURE\n247 DEVONSHIRE DR RIDGEWAY\nALTERED MENTAL STATUS
 (CAD Call) 2010094372\n1 LEE FORD CAMP RD RIDGEWAY\n@ 220 - TK & SUV
 (CAD Call) 2010094604\nRSI-(2) SICK UNKNOWN MEDICAL\n 89 DAYLIGHT DR RIDGEWAY
 (CAD Call) 2010095255\nMOTOR VEHICLE CRASH\nHENRY COUNTY PLYWOOD CO. 1580 PHOSPHO SPRINGS RD RIDGEWAY
 (CAD Call) 2010095057\nRSI-(2) SICK UNKNOWN MEDICAL\n129 KIRK ST 4 MARTINSVILLE
 (CAD Call) 2010095041\nMOTOR VEHICLE CRASH\n3 MARHILL DR RIDGEWAY
 (CAD Call) 2010095011\nR2-(1) DIFFICULTY BREATHING\n999 MORGAN FORD RD RIDGEWAY
 (CAD Call) 2010098333\nSUSPICIOUS CIRCUMSTANCES\nDOLLAR GENERAL (MOREHEAD AVE)\n390 MOREHEAD AVE RIDGEWAY\nWHT MALE, DARK SHIRT

Contact: Joshua Wilson <rdvfd70@gmail.com>
Sender:911calls+bncCPSY0azfFhDP5JnwBBoEF_IV0w@ridgewayfire.com
(CAD Call) 2011055772 \nF-HELICOPTER LANDING ZONE \nBLUE RIDGE REGIONAL LIBRARY - RIDGEWAY 900 VISTA VIEW LN RIDGEWAY \nFIELD A

 */
 
 public class VAHenryCountyParser extends SmartAddressParser {
   
   private static final String DEF_STATE = "VA";
   private static final String DEF_CITY = "HENRY COUNTY";
 
   private static final String[] CITY_LIST = new String[]
       {"RIDGEWAY", "MARTINSVILLE", "SPENCER", "BASSETT", "COLLINSVILLE", "FIELDALE", "AXTON"}; 
   private static final MatchList CITY_MATCH = new MatchList(CITY_LIST);
   
   public VAHenryCountyParser() {
     super(CITY_LIST, DEF_CITY, DEF_STATE);
   }
 
   @Override
   public String getFilter() {
    return "@ridgewayrescue.com,@ridgewayfire.com";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
 
     if (! subject.equals("CAD Call")) return false;
 
     // Split body by newline terminators
     String[] lines = body.split(" *\n *");
     
     // First pass through lines looking for the address line
     // which we identify because it ends with one of our known city names
     int addrNdx = -1;
     for (int ndx = 1; ndx < lines.length; ndx++) {
       String line = lines[ndx];
       int pt = line.lastIndexOf(' ');
       if (pt >= 0) {
         String token = line.substring(pt+1);
         if (CITY_MATCH.contains(token)) {
           addrNdx = ndx;
           break;
         }
       }
     }
     
     // If we didn't find an address line, pick line 2 if there is one
     // or the last line if their isn't
     if (addrNdx < 0) addrNdx = Math.min(2, lines.length-1);
 
     // Now make a second pass through the lines
     for (int ndx = 0; ndx < lines.length; ndx++) {
       String line = lines[ndx];
       
       // First line always contains call ID
       if (ndx == 0) {
         if (line.startsWith("(CAD Call)")) line = line.substring(10).trim();
         data.strCallId = line;
       }
       
       // If not address line, line 2 contains the unit and call description
       else if (ndx == 1 && ndx < addrNdx) {
         int pt = line.indexOf(' ');
         if (pt >= 0) {
           String tmp = line.substring(0, pt);
           if (tmp.contains("-(")) {
             data.strUnit = tmp.trim();
             line = line.substring(pt+1);
           }
         }
         data.strCall = line.trim();
       }
       
       // Beyond the second line, any line before the address line contains a place name
       else if (ndx < addrNdx) {
         if (data.strPlace.length() > 0) data.strPlace += ' ';
         data.strPlace += line;
       }
       
       // When we get to the address line, use the smart address parser to pick
       // the beginning of the address from any preceding place names.  Unless we
       // already have a place name, in which case address must start the line
       else if (ndx == addrNdx) {
         StartType start = (data.strPlace.length() > 0 ? StartType.START_ADDR : StartType.START_PLACE);
         parseAddress(start, line, data);
       }
       
       // Lines beyond the address line go into supplemental info
       else {
         if (data.strSupp.length() > 0) data.strSupp += " / ";
         data.strSupp += line;
       }
     }
     
     return true;
   }
 }
