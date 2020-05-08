 package net.anei.cadpage.parsers.CA;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Ventura County, CA
 Contact: Chase Morgan <chasemorgan43@gmail.com>
 Sender: Fcc-do-not-reply@Ventura.org
 System: InfoRad
 
 [FCC Page]  Incident Dispatch:  E43    BREATHING  2090 Yosemite Ave              11H1-B1 2 / 3  11-0032427  
 [FCC Page]  Incident Dispatch:  E43    BREATHING  1756 Chaps Ct                  11H1-C4 2 / 3  10-0072877
 [FCC Page]  Incident Dispatch:  E43    BREATHING  2012 N Tam Ct                  11H1-B3 2 / 3  11-0032306
 [FCC Page]  Incident Dispatch:  E43    PUBLIC SER 2692 Fallen Leaf Ct            11F3-E2 2 / 3  11-0032318
 [FCC Page]  Incident Dispatch:  E43    FIRE MISC  1700 Tapo St                   11G2-B6 2 / 3  11-0032394
 [FCC Page]  Incident Dispatch:  E43    VEHICLE FI Wb 118 At / Kuehner Dr         11H1-A4 2 / 3  11-0032367
 
  */
 
 
 public class CAVenturaCountyParser extends MsgParser {
   
   public CAVenturaCountyParser() {
     super("VENTURA COUNTY", "CA");
   }
   
   @Override
   public String getFilter() {
     return "Fcc-do-not-reply@Ventura.org";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (!subject.equals("FCC Page")) return false;
     if (!body.startsWith("Incident Dispatch: ")) return false;
     if (body.length() < 94) return false;
     data.strUnit = body.substring(19,27).trim();
     data.strCall = body.substring(27,38).trim();
     parseAddress(body.substring(38,69).trim(), data);
     data.strMap = body.substring(69,77).trim();
     data.strChannel = body.substring(77,84).trim();
     data.strCallId = body.substring(84).trim();
     return true;
   }
 }
