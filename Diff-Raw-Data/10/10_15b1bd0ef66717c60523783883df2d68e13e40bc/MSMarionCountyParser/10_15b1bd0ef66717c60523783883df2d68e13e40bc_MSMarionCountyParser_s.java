 package net.anei.cadpage.parsers.MS;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchBParser;
 
 /*
 Marion County, MS
 Contact: Michael Yoder <unclemuncle@gmail.com>
 Sender: 911-CENTER@co.marion.ms.us
 -  - 911-CENTER:SFIRE >STRUCTURE FIRE 1129 PICKWICK RD FOXWORTH FORBES, WILLEASE Map: Grids:, Cad: 2012-0000001317\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 577 MT CARMEL CHURCH RD FOXWORTH AT&T MOBILITY Map: Grids:, Cad: 2012-0000001285\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 244 SPRING HILL CHURCH RD SANDY HOOK AT&T MOBILITY Map: Grids:, Cad: 2012-0000001188\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 185 SHILOH FIRETOWER RD FOXWORTH BROOM, PAUL & MARTHA Map: Grids:, Cad: 2012-0000000979
 -  - 911-CENTER:SIG2F >ALARM FIRE 109 HWY 98 E COLUMBIA Map: Grids:, Cad: 2012-0000000949\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 491 JOHNSON RD SANDY HOOK MINGO, KAREN Map: Grids:, Cad: 2012-0000000646\n
 -  - 911-CENTER:SIG80 >MISSING PERSON / RUNAWAY 191 STOGNER RD FOXWORTH CELLULAR SOUTH Map: Grids:, Cad: 2012-0000000431\n
 -  - 911-CENTER:EVENT: SIG1 LOC:115 KOKOMO RD Cad: 2012-0000000429 1023 >18:21 1024 >19:28\n
 -  - 911-CENTER:SIG1 >MVA 115 KOKOMO RD KOKOMO TONEY, JACK Map: Grids:, Cad: 2012-0000000429\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 91 HURRICANE CREEK RD SANDY HOOK CELLULAR SOUTH Map: Grids:, Cad: 2012-0000000390\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 244 SPRING HILL CHURCH RD SANDY HOOK LEWIS, MARY Map: Grids:, Cad: 2012-0000001643\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 240 BRANTON BAY RD TYLERTOWN AT&T MOBILITY Map: Grids:, Cad: 2012-0000001839\n
 -  - 911-CENTER:SIG1S >MVA WITH INJURIES TEN MILE CREEK RD FOXWORTH AT&T MOBILITY Map: Grids:, Cad: 2012-0000004324\n
 
 Contact: "Prowler" <prowler251@gmail.com>
 911-CENTER:SIG72 >MEDICAL CALL 221 NEW HOPE KOKOMO RD FOXWORTH AT&T MOBILITY Map: Grids:, Cad: 2012-0000004183
 911-CENTER:SIG72 >MEDICAL CALL 1837 HWY 35 S FOXWORTH Map: Grids:, Cad: 2012-0000004092
 911-CENTER:SIG72 >MEDICAL CALL 1831 HWY 35 S FOXWORTH RILEY, BETTY Map: Grids:, Cad: 2012-0000004015
 911-CENTER:SIG34 >FIGHT 238 JONES RD FOXWORTH DUNCAN, JOSEPH Map: Grids:, Cad: 2012-0000004278
 -  - 911-CENTER:SIG72 >MEDICAL CALL TAYLORS CUT OFF FOXWORTH BLANSETT, ELISABETH Map: Grids:, Cad: 2012-0000005837\n
 
 
 // Time reports - do not parse
 -  - 911-CENTER:EVENT: SIG72 LOC:37 STRINGER BULLOCK RD Cad: 2012-0000001996 ENR >21:01 1023 >21:15 1024 >21:41\n
 
 */
 
 public class MSMarionCountyParser extends DispatchBParser {
   
   private static final Pattern MARKER = Pattern.compile("^(?:-  - )?911-CENTER:");
 
   public MSMarionCountyParser() {
     super(CITY_LIST, "MARION COUNTY", "MS");
   }
   
   @Override
   public String getFilter() {
     return "@co.marion.ms.us";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end());
     if (body.startsWith("EVENT:")) return false;
     if (! super.parseMsg(body, data)) return false;
     return true;
   }
 
   @Override
   protected boolean isPageMsg(String body) {
     return true;
   }
 
   private static final String[] CITY_LIST = new String[]{
     "COLUMBIA",
     
     "BUNKER HILL",
     "CHERAW",
     "EAST COLUMBIA",
     "FOXWORTH",
     "GOSS",
     "HUB",
     "IMPROVE",
     "KOKOMO",
     "MORGANTOWN",
     "SANDY HOOK",
     "JAMESTOWN"
   };
 }
