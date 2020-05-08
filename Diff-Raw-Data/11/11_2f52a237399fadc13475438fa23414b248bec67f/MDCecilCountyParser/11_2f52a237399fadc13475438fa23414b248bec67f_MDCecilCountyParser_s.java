 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*    
  **********************************************************
  Cecil County, MD
  Contact: Andrew Himmer <himmer1w1@gmail.com>
  Contact: Keith Stumpf <keith.stumpf@yahoo.com>
  Contact: Patrick <firerescue28@gmail.com>
  Sender: cfcrs8@gmail.com
  System: EMBERS (Messenger 911 used locally at Ft Mead)
 
  0833*HEART RELATED*37 LILLIAN CIR*BARNES CORNER RD*HOPEWELL RD*RS*11:04:07*10023098
  0841*SICK PERSON*480 HARRISVILLE RD*THRUWAY DR*WYATT LN*COLO*13:46:15*10023234
  0822*DIABETIC*406 FELL RD*CHANDLEE RD*CHROME RD*RS*18:42:20*10023338
  0800*SEIZURES*209 W MAPLE HEIGHTS CT*RS*19:39:49*10023870
 
  0822*TRANS/PALLIATIVE*1881 TELEGRAPH RD *WALNUT GARDEN RD*ROSEBANK RD*RS*20:18:18*10023682 
  1401*FUEL SPILL*SINGERLY RD / BIG ELK CHAPEL R*ELK*02:18:39*10023699
  0400*BUILDING FIRE*801 S MAIN ST*CHERRY ST*IRISHTOWN RD*NE*16:31:44*10023735
  1401*FUEL SPILL*SINGERLY RD / BIG ELK CHAPEL R*ELK*02:18:39*10023699
  0400*BUILDING FIRE*801 S MAIN ST*CHERRY ST*IRISHTOWN RD*NE*16:31:44*10023735
  0800*HEADACHE*201 STONE RUN DR*RS*17:09:11*10023738
  0800*MOTOR VEHICLE ACC*UNCLE WILLYS RESTAUR @325 W MA*RS*21:15:42*10023751
  0800*ABDOMINAL PAIN*407 W MAPLE HEIGHTS CT*RS*10:32:32*10023778
  0400*GAS LEAK INSIDE*12 N MAIN ST*RUSSELL ST*N MAULDIN AVE*NE*18:02:35*10023801
 
  OUT OF COUNTY mutual aid
  OOC*AMBO CALL*WARE VILLAGE 7 E LOCUST*01:13:01*10023819
 
  0834*CHEST PAIN*1162 EBENEZER CHURCH RD*ZULEKHA LN*CROTHERS RD*RS*08:03:05*10023825
  0833*UNCON/SYNCOPAL*21 CHADDS WAY*PD*15:17:30*10023850
  0822*MOTOR VEHICLE ACC*1279 TELEGRAPH RD*WILSON RD*CISSEL LN*RS*07:17:36*11000026
  0823*SICK PERSON*354 CHROME RD*WALNUT GARDEN RD*KENNEDY LN*RS*09:16:24*11000028
  OOC*AMBO CALL*80 KINSEYVILLE RD*OOC*16:58:32*11000055
  0800*SICK PERSON*100 MCNAMME LN *RS*20:46:17*11000065
 
  0833*CO DETECTOR NO INJ*57 COULSON DR*COLO*16:37:15*11000816
  0843*MOTOR VEHICLE ACC*LOVE RUN RD / CONOWINGO RD*COLO*17:37:41*11000822
  0704*HOUSE FIRE*227 LINTON RUN RD*WINCH RD*DRIER CIR*PD*17:47:04*11000869
  1402*MOTOR VEHICLE ACC*HILLTOP RD / TELEGRAPH RD*ELK*18:02:03*11000938 
  OOC*HOUSE FIRE*207 SHEFFEILD LN*12:03:09*11001029
  */
 
 public class MDCecilCountyParser extends SmsMsgParser {
 
   private static final Pattern TIME_PTN = Pattern
       .compile("\\d\\d:\\d\\d:\\d\\d");
 
   private static final Properties CITY_CODES = buildCodeTable(new String[] {
       "CCLT", "CECILTON", "CHES", "CHESAPEAKE CITY", "ELK", "ELKTON", "NE",
       "NORTH EAST", "CH", "CHARLESTOWN", "PV", "PERRYVILLE", "PD",
       "PORT DEPOSIT", "RS", "RISING SUN", "EARL", "EARLVILLE", "PP",
       "PERRY POINT", "COLO", "COLORA", "CONO", "CONOWINGO" });
 
   public MDCecilCountyParser() {
     super("CECIL COUNTY", "MD");
   }
 
   @Override
   public String getFilter() {
     return "cfcrs8@gmail.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     String[] flds = body.split(" *\\* *");
     if (flds.length < 5)
       return false;
 
     boolean ooc = flds[0].equals("OOC");
     if (!ooc)
       data.strBox = flds[0];
 
     data.strCall = flds[1];
     String sAddr = flds[2];
     int pt = sAddr.indexOf('@');
     if (pt >= 0) {
       data.strPlace = sAddr.substring(0, pt).trim();
       sAddr = sAddr.substring(pt+1).trim();
     }
     parseAddress(sAddr, data);
 
     int ndx = 2;
     while (++ndx < flds.length) {
       String fld = flds[ndx];
       if (fld.length() == 0)
         continue;
       if (TIME_PTN.matcher(fld).matches())
         break;
 
       if (fld.length() <= 4) {
         if (!ooc)
           data.strCity = convertCodes(fld, CITY_CODES);
       } else {
         if (data.strCross.length() > 0)
           data.strCross += " & ";
         data.strCross += fld;
       }
     }
 
     if (++ndx >= flds.length)
       return false;
     data.strCallId = flds[ndx];
 
     // Out of county responses are a problem, we really don't know
     // which county or state they might be in.
     if (ooc) data.defCity = data.defState = "";
 
     return true;
   }
 }
