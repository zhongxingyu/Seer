 package net.anei.cadpage.parsers.NY;
 
import java.util.regex.Pattern;

 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 /*
 Greene County NY (or Athens FD)
 Contact: 8456569957@mms.att.net
 Sender: 200-200-0004
 GREENE 911:WIRES >WIRES DOWN / ARCING 117 RIVER RD ATHENS BRAUMULLER, HELENE Map: Grids:0,0 Cad: 2010-0000033052
 GREENE 911:ALARMF>FIRE ALARM 558 SLEEPY HOLLOW RD ATHENS ADT SECURITY-OP ANDREA- Map:62 Grids:0,0 Cad: 2010-0000033344
 
 Contact: Stephen Quick <stephen.d.quick@gmail.com>
 GREENE 911:SMOKE >SMOKE INVESTIGATION 2ND ST VENTE JOEAN Cad: 2010-0000034969
 GREENE 911:PIAA >MVA WITH INJURIES MARKET ST ATHENS BUCHAKIAN,DAVID Cad: 2010-0000033852
 GREENE 911:STRUCT>STRUCTURE FIRE 408 W BRIDGE ST CATSKILL LAIRD, JACQUELYNN Cad: 2010-0000034875
 GREENE 911:ALARMF>FIRE ALARM 341 W MAIN ST CATSKILL VILLAGE #401 Map: Grids:, Cad: 2011-0000011139
 
FRM:GREENE911@thinkgreene.us\nMSG:GREENE911:STRUCT>STRUCTURE FIRE SLEEPY HOLLOW RD & LAKE VIEW D ATHENS BARBARA Cad: 2012-0000002596


 */
 
 public class NYGreeneCountyParser extends DispatchBParser {
   
   private static final String[] CITY_CODES = new String[]{
     "ASHLAND", "ATHENS", "CAIRO", "CATSKILL", "COXSACKIE", "DURHAM", "GREENVILLE",
     "HALCOTT", "HUNTER", "JEFFERSON HEIGHTS", "JEWETT", "LEEDS", "LEXINGTON", "NEW BALITMORE",
     "PALENVILLE", "PRATTSVILLE", "TANNERSVILLE", "WINDHAM"};
 
  private static final Pattern MARKER = Pattern.compile("^GREENE ?911:");
  
   public NYGreeneCountyParser() {
    super(CITY_CODES, "GREENE COUNTY", "NY");
   }
   
   @Override
   public String getFilter() {
    return "200-200-0004,GREENE911@thinkgreene.us";
   }
   
   @Override
   protected boolean isPageMsg(String body) {
    return MARKER.matcher(body).find();
   }
 }
