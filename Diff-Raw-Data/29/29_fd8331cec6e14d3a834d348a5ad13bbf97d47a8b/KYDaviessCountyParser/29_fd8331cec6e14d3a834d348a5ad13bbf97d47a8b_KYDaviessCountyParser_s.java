 package net.anei.cadpage.parsers.KY;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 
 public class KYDaviessCountyParser extends DispatchBParser {
   
   private static final Pattern US_PTN = Pattern.compile("\\bU +S\\b", Pattern.CASE_INSENSITIVE);
  
   public KYDaviessCountyParser() {
     super(CITY_LIST, "DAVIESS COUNTY", "KY");
   }
   
   @Override
   public String getFilter() {
    return "911-CENTER,2002";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // These are the only folks I know who split up US highway prefixes
     body = US_PTN.matcher(body).replaceAll("US");
     return super.parseMsg(body, data);
   }
   
   @Override
   protected boolean isPageMsg(String body) {
    if (body.startsWith("911-CENTER:")) return true;
     return super.isPageMsg(body);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "HARTFORD",
     "OWENSBORO",
     "PHILPOT",
     "UTICA",
   };
 }
