 package net.anei.cadpage.parsers.TN;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchSouthernParser;
 
 
 public class TNLoudonCountyParser extends DispatchSouthernParser {
   
   
   public TNLoudonCountyParser() {
     super(CITY_LIST, "LOUDON COUNTY", "TN");
   }
   
   @Override
   public String getFilter() {
     return "@loudoncounty911.org,777,9300";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
    if (body.startsWith("LDN911 ")) body = body.substring(7).trim();
     body = body.replaceAll("//+", "/");
     return super.parseMsg(body, data);
   }
   
 
   private static final String[] CITY_LIST = new String[]{
     "GREENBACK",
     "LENOIR CITY",
     "LOUDON",
     "PHILADELPHIA",
     
     "DIXIE LEE JUNCTION",
     "MORGANTON",
     "TELLICO VILLAGE"
   };
 
 }
