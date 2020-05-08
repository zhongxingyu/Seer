 package net.anei.cadpage.parsers.IL;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 public class ILDuPageCountyParser extends MsgParser {
   
   public ILDuPageCountyParser() {
     super("DUPAGE COUNTY", "IL");
   }
   
   @Override
   public String getFilter() {
     return "DU-COMM@ducomm.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (!body.startsWith("INC01 1.0 EV-XXX 0       ")) return false;
     data.strCallId = substring(body, 25, 40);
     String addr = substring(body, 40, 70);
     int pt = addr.indexOf("...");
     if (pt >= 0) addr = addr.substring(0,pt).trim();
    if (addr.startsWith("0N025 ")) addr = "25 N " + addr.substring(6);
     parseAddress(addr, data);
     data.strCity = convertCodes(substring(body,70,72), CITY_CODES);
     data.strCode = substring(body, 72, 76);
     data.strCall = substring(body, 76, 96);
     data.strPlace = substring(body, 96, 141);
     data.strCross = substring(body, 141);
     return true;
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "OR", "",
       "RO", "ROSELLE",
       "VP", "VILLA PARK"
   });
 }
