 package net.anei.cadpage.parsers.OR;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*    
 Sample Benton County Page
 Contact: kencx@peak.org
 Sender: alerts@corvallis.ealertgov.com
 (Corvallis Alert) INC: CODE 1 MEDICAL\nADD:1740 MAIN ST\nAPT:\nCITY:PHILOMATH\nX:N 17TH ST * N 18TH ST\nMAP:540-365\nCFS:0907010-119\nDIS:PHILOMATH FIRE
 (Corvallis Alert) INC:COMM FIRE ALARM\nADD:421 S 19TH ST\nAPT:\nCITY:PHILOMATH\nX:ASH ST * CEDAR ST\nMAP:540-360\nCFS:100410-188\nDIS:PHILOMATH FIRE\nDIS:PHI
 (Corvallis Alert) INC:UNKNOWN MEDICAL\nADD:504 MELVILL CRESCENT AV [PHILOMATH]\nAPT:\nCITY:PHILOMATH\nX:SUNSHINE AV\nMAP:550-385\nCFS:101710-223\nDIS:PHILOM
 */
 
 public class ORBentonCountyParser extends SmsMsgParser {
   
   public ORBentonCountyParser() {
     super("BENTON COUNTY", "OR");
   }
   
   @Override
   public String getFilter() {
     return "alerts@corvallis.ealertgov.com";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (! subject.equals("Corvallis Alert")) return false;
 
     Properties props = parseMessage(body, "\n");
     
     data.strCall=props.getProperty("INC", null);
     data.strAddress=props.getProperty("ADD", null);
     if (data.strCall == null || data.strAddress == null) return false;
     
     int pt = data.strAddress.lastIndexOf(' ');
     if (pt >= 0) {
       String token = data.strAddress.substring(pt+1);
       if (token.charAt(0) == '[' && token.charAt(token.length()-1) == ']') {
         data.strAddress = data.strAddress.substring(0, pt).trim();
         data.strCity = token.substring(1, token.length()-2);
       }
     }
 
     data.strCity=props.getProperty("CITY", "");
     data.strApt =props.getProperty("APT", "");
     data.strCross=props.getProperty("X", "");
     data.strMap=props.getProperty("MAP", "");
     data.strCallId = props.getProperty("CFS", "");
     
     return true;
   }
 }
