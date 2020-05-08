 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.Log;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*    
 Alleghany County, MD
 Contact: Joseph Hoffman <Jphoffman0@frostburg.edu>
 Sender: unpredicatable
 FRM:LogiSYSCAD\nSUBJ:CAD Page for CFS 100110-96\nMSG:UNCONCIOUS/UNRESPONSIVE 91 S BROADWAY\nUnits: A53 CO16
 FRM:LogiSYSCAD\nSUBJ:CAD Page for CFS 100710-102\nMSG:AUTOMATIC HOUSE ALARM 106 SUNSET DR\nUnits: CO2 ENG9 A52 TR16
 FRM:LogiSYSCAD\nSUBJ:CAD Page for CFS 101310-67\nMSG:HOUSE FIRE           147 ORMAND ST\nUnits: ENG16 TR16 ENG17
 FRM:LogiSYSCAD\nSUBJ:CAD Page for CFS 101510-80\nMSG:HOUSE FIRE           72 HOSPITAL ROAD\nUnits: ENG16 TR16 ENG17
 FRM:LogiSYSCAD\nSUBJ:CAD Page for CFS 100210-88\nMSG:SMOKE INVESTIGATION  U:LOWER CONSEL RD\nUnits: CO16
 */
 
 public class MDAlleganyCountyParser extends SmsMsgParser {
   
   // Pattern matching a sequence of two or more blanks
   protected static final Pattern MULT_BLANKS = Pattern.compile("  +");
 
   @Override
   public boolean isPageMsg(String body) {
     return body.startsWith("FRM:LogiSYSCAD\n");
   }
 
   @Override
   protected void parse(String body, Data data) {
     
     Log.v("DecodeAlleganyCo: Message Body of:" + body);
     data.defState="MD";
     data.defCity="ALLEGANY COUNTY";
     
     Properties props = parseMessage(body, "\n");
     
     // SUBJ: line contains the call ID as the last token
     String line = props.getProperty("SUBJ", "");
     int pt = line.lastIndexOf(' ');
     if (pt >= 0) line = line.substring(pt+1);
     data.strCallId = line;
     
     // MSG: line contains call description and address.
     // The separator can be several different things
     line = props.getProperty("MSG", "");
     String address = null;
     Matcher match;
     
     // Address may start with "U:"
     pt = line.indexOf(" U:");
     if (pt >= 0) {
       data.strCall = line.substring(0, pt).trim();
       address = line.substring(pt+3).trim();
     }
     
     // May be separated by string of 2 or more blanks
     else if ((match = MULT_BLANKS.matcher(line)).find()) {
       data.strCall = line.substring(0, match.start());
       address = line.substring(match.end());
     }
     
     // Or address may start with a numeric token
     else if ((match = NUMERIC.matcher(line)).find()) {
       data.strCall = line.substring(0,match.start()).trim();
       address = line.substring(match.start());
     }
     
     // parse address line
     if (address != null) parseAddress(address, data);
     
     // UNITS: line contains the units
    data.strUnit = props.getProperty("Units");
   }
 }
