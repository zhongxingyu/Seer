 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchSouthernParser;
 
 
 public class NCDuplinCountyParser extends DispatchSouthernParser {
   
   
   public NCDuplinCountyParser() {
     super(CITY_LIST, "DUPLIN COUNTY", "NC", DSFLAG_DISPATCH_ID | DSFLAG_ID_OPTIONAL | DSFLAG_FOLLOW_CROSS);
   }
   
   @Override
   public String getFilter() {
     return "@duplincountync.com";
   }
   
   @Override
   protected boolean parseMsg(String field, Data data) {
    if (!super.parseMsg(field, data));
     if (data.strCity.equals("DUPLIN")) data.strCity = "";
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
     
     "DUPLIN",
     
     // Cities
     "BEULAVILLE",
     "CALYPSO",
     "CHINQUAPIN",
     "FAISON",
     "FOUNTAINTOWN",
     "GREENEVERS",
     "HARRELLS",
     "KENANSVILLE",
     "MAGNOLIA",
     "ROSE HILL",
     "SARECTA",
     "TEACHEY",
     "WALLACE",
     "WARSAW",
     
     // Townships
     "ALBERTSON",
     "CYPRESS CREEK",
     "FAISON",
     "GLISSON",
     "ISLAND CREEK",
     "KENANSVILLE",
     "LIMESTONE",
     "MAGNOLIA",
     "ROCKFISH",
     "ROSE HILL",
     "SMITH",
     "WALLACE",
     "WARSAW",
     "WOLFSCRAPE",
   };
 
 }
