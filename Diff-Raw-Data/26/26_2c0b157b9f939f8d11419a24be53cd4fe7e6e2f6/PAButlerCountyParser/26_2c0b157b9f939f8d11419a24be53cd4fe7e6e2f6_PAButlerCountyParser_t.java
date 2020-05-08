 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 /*
 Butler County, PA
 Contact: Jeff Gooch <goochff21@gmail.com>
 "."@butlerco.911 :ALAF >ALARM/FIRE 20436 ROUTE 19 CRANBERRY TWP GUARDIAN Map: Grids:00000,000 Cad: 2011-0000075272
 "."@butlerco.911 :CO3 >CO DETECTOR / FIRE RESPONSE 437 SETTLERS VILLAGE CIR CRANBERRY TWP ALEJANDRO, GABRIEL Map: Grids:00000,000 Cad: 2011-0000074937
 "."@butlerco.911 :SERV >SERVICE CALL 143 FOX RUN RD CRANBERRY TWP CHEETHAM WILLIAM Map: Grids:00000,000 Cad: 2011-0000075245
 "."@butlerco.911 :FIRST >FIRE - STRUCTURE 20036 ROUTE 19 CRANBERRY TWP CANDLEWOOD EXTENDED STAY Map: Grids:00000,000 Cad: 2011-0000074503
 "."@co.butler.pa.us :ALAF >ALARM/FIRE 20620 ROUTE 19 CRANBERRY TWP RAMPART Map: Grids:00000,000 Cad: 2012-0000006337
 
Contact: Kenneth Chiacchia <chiacchiakb@gmail.com>
Sender: 6245
"."@co.butler.pa.us :ALAF >ALARM/FIRE 804 PROSPECT RD CONNOQ TWP GAURDIAN Map: Grids:00000,000 Cad: 2012-0000035901

 */
 
 public class PAButlerCountyParser extends DispatchBParser {
   
   private static final String[] MARKERS = new String[]{
     "\".\"@butlerco.911 :",
     "\".\"@co.butler.pa.us :"
   };
 
   public PAButlerCountyParser() {
     super(CITY_LIST, "BUTLER COUNTY", "PA");
   }
   
   @Override
   protected boolean isPageMsg(String body) {
     return true;
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
 
     boolean found = false;
     for (String marker : MARKERS) {
       if (body.startsWith(marker)) {
         found = true;
         body = body.substring(marker.length()).trim();
         break;
       }
     }
     if (!found) return false;
     
     if (! super.parseMsg(body, data)) return false;
    if (data.strCity.equals("CONNOQ TWP")) data.strCity = "CONNOQUENESSING TWP";
     if (data.strMap.equals("00000,000")) data.strMap = "";
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
     "BUTLER",
     
     "BRUIN",
     "CALLERY",
     "CHERRY VALLEY",
     "CHICORA",
     "CONNOQUENESSING",
     "EAST BUTLER",
     "EAU CLAIRE",
     "EVANS CITY",
     "FAIRVIEW",
     "HARMONY",
     "HARRISVILLE",
     "KARNS CITY",
     "MARS",
     "PETROLIA",
     "PORTERSVILLE",
     "PROSPECT",
     "SAXONBURG",
     "SEVEN FIELDS",
     "SLIPPERY ROCK",
     "VALENCIA",
     "WEST LIBERTY",
     "WEST SUNBURY",
     "ZELIENOPLE",
     
     "ADAMS TWP",
     "ALLEGHENY TWP",
     "BRADY TWP",
     "BUFFALO TWP",
     "BUTLER TWP",
     "CENTER TWP",
     "CHERRY TWP",
     "CLAY TWP",
     "CLEARFIELD TWP",
     "CLINTON TWP",
     "CONCORD TWP",
    "CONNOQ TWP",
     "CONNOQUENESSING TWP",
     "CRANBERRY TWP",
     "DONEGAL TWP",
     "FAIRVIEW TWP",
     "FORWARD TWP",
     "FRANKLIN TWP",
     "JACKSON TWP",
     "JEFFERSON TWP",
     "LANCASTER TWP",
     "MARION TWP",
     "MERCER TWP",
     "MIDDLESEX TWP",
     "MUDDY CREEK TWP",
     "OAKLAND TWP",
     "PARKER TWP",
     "PENN TWP",
     "SLIPPERY ROCK TWP",
     "SUMMIT TWP",
     "VENANGO TWP",
     "WASHINGTON TWP",
     "WINFIELD TWP",
     "WORTH TWP",
     
     "BOYERS",
     "EIDENAU",
     "HILLIARDS",
     "MURRINSVILLE",
     "RENFREW",
     "SARVER",
     "WAHLVILLE",
     "WATTERS"
   };
 }
