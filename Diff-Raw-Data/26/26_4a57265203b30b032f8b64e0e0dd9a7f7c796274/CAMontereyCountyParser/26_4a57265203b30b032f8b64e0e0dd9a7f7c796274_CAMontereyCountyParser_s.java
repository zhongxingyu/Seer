 package net.anei.cadpage.parsers.CA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*
 Monterey County, CA
 Contact: Tom Tengdin <t3@razzolink.com>
 Sender: donotreply@co.monterey.ca.us
 System: Tiburon
 
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:52161HA - MEO:NOT XFR - 13200 CIELO AZULUnits:E5211, 52A
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:5261BT - MEO:MEDICAL EMERGNCY - 7881 SANDHOLDT RDUnit:CH5202
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:5254A - MEO:NOT XFRD - 315 NEPONSET RDUnits:E5211, 52A
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:524451A - MEO:XFRD - 97 RAILROAD AVUnits:E5213, 52A
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:52251AB - UIA:UNK INJ ACC - HWY 101/SAN MIGUEL CANYON RDUnits:E5212, E5213, 52V
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:FMA:FIRE MUTUAL AID - 918 FREEDOM BLVDUnit:E5213
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:52351A - MEO:MEO ALM - 398 BERRY RDUnits:E5213, 52A
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:5261AT - MEO:XFRD - 10561 MERRITT STUnits:E5211, 52A
 FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:52251A - VIA:SB HWY 101 - HWY 101/CRAZY HORSE CANYON RDUnits:E5212, BEU, 52V\n
 
  */
 
 
 public class CAMontereyCountyParser extends SmsMsgParser {
   
  private static final Pattern MASTER = Pattern.compile("(?:(.*?) - )?([A-Z]{3}:.*?) - (.*)Units?:(.*?)");
   
   public CAMontereyCountyParser() {
     super("MONTEREY COUNTY", "CA");
   }
   
   @Override
   public String getFilter() {
     return "donotreply@co.monterey.ca.us";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("CAD Page")) return false;
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     
     data.strMap = getOptGroup(match.group(1));
     data.strCall = match.group(2).trim();
     parseAddress(match.group(3).trim(), data);
    data.strUnit = match.group(4).trim();
     
     return true;
   }
 }
