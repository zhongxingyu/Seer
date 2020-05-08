 package net.anei.cadpage.parsers.PA;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 
 
 public class PASnyderCountyParser extends DispatchBParser {
   
   private static final Pattern CAD_PTN = Pattern.compile(" C[Aa][Dd][ :](?=\\d)");
   
   private static final String[] CITY_LIST = new String[]{
     "BEAVERTOWN BORO",
     "FREEBURG BORO",
     "MCCLURE BORO",
     "MIDDLEBURG BORO",
     "SELINSGROVE BORO",
     "SHAMOKIN DAM BORO",
     
     "ADAMS TWP",
     "BEAVER TWP",
     "CENTER TWP",
     "CHAPMAN TWP",
     "FRANKLIN TWP",
     "JACKSON TWP",
     "MIDDLECREEK TWP",
     "MONROE TWP",
     "PENN TWP",
     "PERRY TWP",
     "SPRING TWP",
     "UNION TWP",
     "WASHINGTON TWP",
     "WEST BEAVER TWP",
     "WEST PERRY TWP"
  };
 
   public PASnyderCountyParser() {
     super(CITY_LIST, "SNYDER COUNTY", "PA");
   }
   
   @Override
   protected boolean isPageMsg(String body) {
    return body.startsWith("SNYDER911:") || body.startsWith("SYNDER911:") ||
            body.contains(" Cad:");
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     body = CAD_PTN.matcher(body).replaceAll(" Cad:");
     body = body.replace(" MAP:", "Map:");
     body = body.replace(" Map:Grids:", " Map: Grids:");
     if (! super.parseMsg(body, data)) return false;
     if (data.strCity.endsWith(" BORO")) {
       data.strCity = data.strCity.substring(0, data.strCity.length()-5);
     }
     return true;
   }
 }
