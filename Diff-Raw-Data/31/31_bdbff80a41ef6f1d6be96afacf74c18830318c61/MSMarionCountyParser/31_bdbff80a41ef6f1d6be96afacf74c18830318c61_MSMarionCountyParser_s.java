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
-  - 911-CENTER:EVENT: SIG72 LOC:185 SHILOH FIRETOWER RD Cad: 2012-0000000979 ENR >22:42 1023 >22:44 1024 >23:22 PATIENT REF TRANS\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 185 SHILOH FIRETOWER RD FOXWORTH BROOM, PAUL & MARTHA Map: Grids:, Cad: 2012-0000000979
-  - 911-CENTER:EVENT: SIG2F LOC:109 HWY 98 E Cad: 2012-0000000949 ENR >14:14 1024 >15:00\n
 -  - 911-CENTER:SIG2F >ALARM FIRE 109 HWY 98 E COLUMBIA Map: Grids:, Cad: 2012-0000000949\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 491 JOHNSON RD SANDY HOOK MINGO, KAREN Map: Grids:, Cad: 2012-0000000646\n
-  - 911-CENTER:EVENT: SIG1S LOC:HWY 98 W Cad: 2012-0000000479 1023 >04:52 1024 >14:57\n
-  - 911-CENTER:EVENT: SIG80 LOC:191 STOGNER RD Cad: 2012-0000000431 ENR >20:45 1023 >21:05 1024 >23:17\n
 -  - 911-CENTER:SIG80 >MISSING PERSON / RUNAWAY 191 STOGNER RD FOXWORTH CELLULAR SOUTH Map: Grids:, Cad: 2012-0000000431\n
 -  - 911-CENTER:EVENT: SIG1 LOC:115 KOKOMO RD Cad: 2012-0000000429 1023 >18:21 1024 >19:28\n
 -  - 911-CENTER:SIG1 >MVA 115 KOKOMO RD KOKOMO TONEY, JACK Map: Grids:, Cad: 2012-0000000429\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 91 HURRICANE CREEK RD SANDY HOOK CELLULAR SOUTH Map: Grids:, Cad: 2012-0000000390\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 244 SPRING HILL CHURCH RD SANDY HOOK LEWIS, MARY Map: Grids:, Cad: 2012-0000001643\n
 -  - 911-CENTER:SIG72 >MEDICAL CALL 240 BRANTON BAY RD TYLERTOWN AT&T MOBILITY Map: Grids:, Cad: 2012-0000001839\n
 -  - 911-CENTER:EVENT: SIG72 LOC:37 STRINGER BULLOCK RD Cad: 2012-0000001996 ENR >21:01 1023 >21:15 1024 >21:41\n
-  - 911-CENTER:EVENT: SIG1 LOC:283 MAYS CREEK RD Cad: 2012-0000002303 1023 >22:12 1024 >22:43\n
-  - 911-CENTER:EVENT: SFIRE LOC:31 OILFIELD CUT OFF RD Cad: 2012-0000002674 ENR >03:33 1023 >03:39 1024 >07:10\n
s
 */
 
 public class MSMarionCountyParser extends DispatchBParser {
   
   private static final String MARKER = "-  - 911-CENTER:";
  private static final Pattern EVENT_LOC_PTN = Pattern.compile("EVENT:(.*) LOC:(.*)");
 
   public MSMarionCountyParser() {
     super(CITY_LIST, "MARION COUNTY", "MS");
   }
   
   @Override
   public String getFilter() {
     return "@co.marion.ms.us";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (!body.startsWith(MARKER)) return false;
     body = body.substring(MARKER.length()).trim();
     if (! super.parseMsg(body, data)) return false;
     int pt = data.strCallId.indexOf(' ');
     if (pt >= 0) data.strCallId = data.strCallId.substring(0,pt);
     return true;
   }
 
   @Override
   protected boolean isPageMsg(String body) {
     return true;
   }
  
  @Override
  protected boolean parseAddrField(String field, Data data) {
    Matcher match = EVENT_LOC_PTN.matcher(field);
    if (match.matches()) {
      data.strCall = match.group(1).trim();
      parseAddress(match.group(2), data);
      return true;
    }
    
    return super.parseAddrField(field, data);
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
