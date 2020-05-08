 package net.anei.cadpage.parsers.PA;
 
import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 
 /*
 ERIE911:69D6 >STRUC FIRE-SINGLE RESIDENTIAL 8165 PLATZ RD XS: MARKET RD FAIRVIEW TWP LIST JOHN C Map:2034 Grids:, Cad: 2011-0000044804
 ERIE911:55B2P >ELEC HAZ/PWR REPT DISCONNECTED 7656 MAPLE DR XS: CHESTNUT ST FAIRVIEW TWP MUSANTE, JANET Map:2202 Grids:, Cad: 2011-0000045114
 ERIE911:29B4 >MVA - UNKNOWN STATUS 17 I 90 EB XS: I 90 EB RAMP EXIT 16 FAIRVIEW TWP LORD, ISAAC Map:1888 Grids:, Cad: 2011-0000043981
 ERIE911:29B4 >MVA - UNKNOWN STATUS W LAKE RD&WHITEHALL PL XS: LORD RD FAIRVIEW TWP WOOD, RODNEY Cad: 2011-0000042496
 ERIE911:69D6 >STRUC FIRE-SINGLE RESIDENTIAL 6683 OTTEN CT FAIRVIEW TESTI JULIE Cad: 2011-0000039977
 ERIE911:52C3G >FIRE/GENERAL ALARM-COMM STRUC 7301 KLIER DR XS: UNFAIRVIEW FAIRVIEW TWP DAN Map:2302 Grids:, Cad: 2011-0000040143
 ERIE911:69D2 >STRUCTURE FIRE- HIGH RISE 222 W MAIN ST XS: MYRTLE ST GIRARD BORO Map:3217 Grids:, Cad: 2011-0000041382
 ERIE911:10D4 >CHEST PAIN 5757 W RIDGE RD XS: MILLFAIR RD FAIRVIEW TWP NICOLE Map:1988 Grids:, Cad: 2011-0000047247
 ERIE911:17D3 >FALLS 7648 WELCANA DR XS: LYNANN LN FAIRVIEW TWP SANDELL, CECELIA Map:2213 Grids:, Cad: 2011-0000047240
 ERIE911:26A1 >SICK PERSON 8300 W RIDGE RD XS: DOBLER RD FAIRVIEW TWP WIECZOREK, BOB Map:2185 Grids:, Cad: 2011-0000046184
 ERIE911:13A1 >DIABETIC PROBLEMS 8475 MIDDLE RD XS: BLAIR RD FAIRVIEW TWP SEAN Map:2174 Grids:, Cad: 2011-0000046843
 ERIE911:52B1H >RES (SINGLE) HEAT DETECTOR 1530 TAYLOR RIDGE CT FAIRVIEW TWP ADT/DIONNA Map:2540 Grids:, Cad: 2011-0000046825
 */
 
 public class PAErieCountyParser extends DispatchBParser {
   
   private static final String[] CITY_LIST = new String[]{
     "FAIRVIEW TWP","FAIRVIEW"
   };
 
   private static final String DEF_STATE = "PA";
   private static final String DEF_CITY = "ERIE COUNTY";
  
   public PAErieCountyParser() {
     super(CITY_LIST, DEF_CITY, DEF_STATE);
   }
   
   @Override
   protected boolean isPageMsg(String body) {
     return body.startsWith("ERIE911:");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (!super.parseMsg(body, data)) return false;
     
     // The field we parse as a city isn't recognized by Google maps.  We'll
     // switch it to the Place field where at least it will show up on the screen
     // but won't interfere with mapping
     if (data.strPlace.length() == 0) data.strPlace = data.strCity;
     data.strCity = "";
     return true;
   }
 }
