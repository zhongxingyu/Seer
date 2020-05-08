 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 /*
 Butler County, PA
 Contact: Jeff Gooch <goochff21@gmail.com>
 Sender: "."@co.butler.pa.us
 "."@butlerco.911 :ALAF >ALARM/FIRE 20436 ROUTE 19 CRANBERRY TWP GUARDIAN Map: Grids:00000,000 Cad: 2011-0000075272
 "."@butlerco.911 :CO3 >CO DETECTOR / FIRE RESPONSE 437 SETTLERS VILLAGE CIR CRANBERRY TWP ALEJANDRO, GABRIEL Map: Grids:00000,000 Cad: 2011-0000074937
 "."@butlerco.911 :SERV >SERVICE CALL 143 FOX RUN RD CRANBERRY TWP CHEETHAM WILLIAM Map: Grids:00000,000 Cad: 2011-0000075245
 "."@butlerco.911 :FIRST >FIRE - STRUCTURE 20036 ROUTE 19 CRANBERRY TWP CANDLEWOOD EXTENDED STAY Map: Grids:00000,000 Cad: 2011-0000074503
 "."@co.butler.pa.us :ALAF >ALARM/FIRE 20620 ROUTE 19 CRANBERRY TWP RAMPART Map: Grids:00000,000 Cad: 2012-0000006337
 :VAINJ >VA / INJURIES ROUTE 19 CRANBERRY TWP PORTNEY PATTERSON Cad: 2012-0000056854
 
 Contact: Kenneth Chiacchia <chiacchiakb@gmail.com>
 Sender: 6245
 "."@co.butler.pa.us :ALAF >ALARM/FIRE 804 PROSPECT RD CONNOQ TWP GAURDIAN Map: Grids:00000,000 Cad: 2012-0000035901
 
 Contact: PJ Pesanka <pjp@pjpesanka.com>
 Sender: "."@co.butler.pa.us
 :FLODB >FLOODING / BASEMENT 337 CLAY AVE MARS BORO KEN SCOTT Map: Grids:00000,000 Cad: 2012-0000053246
 
 */
 
 public class PAButlerCountyParser extends DispatchBParser {
 
   public PAButlerCountyParser() {
     super(CITY_LIST, "BUTLER COUNTY", "PA");
   }
   
   @Override
   public String getFilter() {
     return "@butlerco.911,@co.butler.pa.us";
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     
     if (! super.parseMsg(body, data)) return false;
     if (data.strCity.endsWith(" BORO")) data.strCity = data.strCity.substring(0,data.strCity.length()-5).trim();
     if (data.strCity.equals("CONNOQ TWP")) data.strCity = "CONNOQUENESSING TWP";
     if (data.strMap.equals("00000,000")) data.strMap = "";
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
     "BUTLER",
     
     "BRUIN BORO",
     "CALLERY BORO",
     "CHERRY VALLEY BORO",
     "CHICORA BORO",
     "CONNOQUENESSING BORO",
     "EAST BUTLER BORO",
     "EAU CLAIRE BORO",
     "EVANS CITY BORO",
     "FAIRVIEW BORO",
     "HARMONY BORO",
     "HARRISVILLE BORO",
     "KARNS CITY BORO",
     "MARS BORO",
     "PETROLIA BORO",
     "PORTERSVILLE BORO",
     "PROSPECT BORO",
     "SAXONBURG BORO",
     "SEVEN FIELDS BORO",
     "SLIPPERY ROCK BORO",
     "VALENCIA BORO",
     "WEST LIBERTY BORO",
     "WEST SUNBURY BORO",
     "ZELIENOPLE BORO",
     
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
