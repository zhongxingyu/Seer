 package net.anei.cadpage.parsers.MO;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchPrintrakParser;
 
 
 
 public class MOGreeneCountyParser extends DispatchPrintrakParser {
   
   private static final Pattern FR_PTN = Pattern.compile("\\bFR(?= *\\d)", Pattern.CASE_INSENSITIVE);
   
   public MOGreeneCountyParser() {
     super("GREENE COUNTY", "MO");
   }
   
   @Override
   public String getFilter() {
     return "grnpage@springfieldmo.gov";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (!super.parseMsg(body, data)) return false;
     if (data.strCity.equals("CHRISTIAN COUNT")) data.strCity = "CHRISTIAN COUNTY";
     return true;
   }
 
   @Override
   public String adjustMapAddress(String sAddress) {
    return FR_PTN.matcher(sAddress).replaceAll(" FARM RD ").trim().replaceAll("  +", " ");
   }
   
   
 }
