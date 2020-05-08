 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /* 
 Collin County, TX
 Contact: Jeremy Sexton <jeremy80xx@yahoo.com>
 Contact: Chris Herbst <cherbstlcfd@gmail.com>
 Sender: ccsodispatch@co.collin.tx.us
 
 11048794  FIRE PUBLIC ASSIST  2701 ASPEN CT IN COLLIN COUNTY  COUNTY ROAD 392  [LCFD DIST: LCF1 GRID: 1322]  UNITS: LCF1  ST RMK: 7K4  CFS RMK 22:10 REQ FD TO CHECK BP...40 YRO FEMALE.   {CAD001 22:13}
 11031079 First Responders 3274 Almeta LN in Collin County FM 546 [LCFD DIST: LCF1 GRID: 1331]
 11047502 INJURED PERSON 3610 FM 546 IN COLLIN COUNTY COUNTY ROAD 398 / COUNTY RO;16AD 394 [LCFD DIST: LCF1 GRID: 1322] UNITS: AMRP LCF1 ST RMK: <NONE> CFS RMK 19:16 MALE SUB BLEEDING FROM HIS THROAT {CAD004 19:17} 
 11047405 FISRT RESPONDERS 2897 COUNTY ROAD 914 IN COLLIN COUNTY COUNTY ROAD 392 [LCFD DIST: LCF1 GRID: 1322] UNITS: LCF1 AMRP ST RMK: DUTCH ACRES CFS RMK 12:59 68 YOA FALLEN IN FRONT YARD UNABLE T {CAD001 13:00}
 11046597 MAJOR ACCIDENT 10/50 COUNTY ROAD 393 / FM 546 IN COLLIN COUNTY [LCFD DIST: LCF1 GRID: 1322] UNITS: LCF1 ST RMK: 8J1 CFS RMK 21:40 SOMEONE HAS FALLEN OUT OF A TRUCK {CAD004 21:40}
 11044587 FIRST RESPONDERS 730 CROSS TIMBERS DR IN LOWRY CROSSING CROSS TRAIL LN [LCFD DIST: LCF1 GRID: 3100] UNITS: LCF1 AMRP ST RMK: <NONE> CFS RMK 13:14 37 YOA MALE / FELL YESTERDAY AND IS {CAD004 13:14}
 11056387  TRASH FIRE  2701 PECAN CT IN COLLIN COUNTY  COUNTY ROAD 392  [LCFD DIST: LCF1 GRID: 1322]  UNITS: LCF1  ST RMK: <NONE>  CFS RMK 15:09 8 HOUSES DOWN  ITEMS LEFT OVER FR A   {CAD004 15:09}
 11055307  MEDICATION OVERDOSE  415 S BRIDGEFARMER RD IN LOWRY CROSSING  E US HIGHWAY 380 / COUNTY ROAD 403  [LCFD DIST: LCF1 GRID: 3100]  UNITS: LCF2  ST RMK: 10J3  CFS RMK 19:34 36 YOA FEMALE TOOK SOMETYPE OF SLEEP  {CAD004 19:35}
 11044930  FIRST RESPONDERS  3737 E UNIVERSITY DR IN COLLIN COUNTY  COUNTY ROAD 407 / COUNTY ROAD 404  [LCFD DIST: LCF1 GRID: 1211]  UNITS: LCF1  ST RMK: <NONE>  CFS RMK 17:52 7 YOA FEMALE WITH ABDOMINAL PAINS///  {CAD004 17:52}
 11046597  MAJOR ACCIDENT 10/50  COUNTY ROAD 393  /  FM 546 IN COLLIN COUNTY [LCFD DIST: LCF1 GRID: 1322]  UNITS: LCF1  ST RMK: 8J1  CFS RMK 21:40 SOMEONE HAS FALLEN OUT OF A TRUCK  {CAD004 21:40}
 11056128  GRASS FIRE  10753 COUNTY ROAD 903 IN COLLIN COUNTY  COUNTY ROAD 902  UNITS: BRF1  ST RMK: <NONE>  CFS RMK 21:52 RIGHT ON
 
 Contact: active911.com
 Sender: ccsodispatch@co.collin.tx.us
 12003605  UNAUTHORIZED BURN  3352 HEDRICK LN IN COLLIN COUNTY  COUNTY ROAD 941  [LCFD DIST: LCF1 GRID: 1322]  UNITS: LCF1  ST RMK: <NONE> CFS RMK 17:12 NEIGHBOR BEHIND THE RP IS BURNING TR  {CAD001 17:13}
 CFS No: 12003605  UNAUTHORIZED BURN  3352 HEDRICK LN  COUNTY ROAD 941 /
 LOWRY CROSSING REQUESTING FIRE MARSHAL  COME TO THE LOCATION - CALLED IN AS SUBJECT  BURNING TRASH IN A SMOKER  CFS No: 12003605 UNAUTHORIZED BURN  7605 COUNTY ROAD 941  PRIVATE ROAD 5206 / PRIVATE ROAD 5205
 DISREGARD  CFS No: 12003635  MINOR ACCIDENT 10/50  FM 546 / FM 3286  DISREGARD
 ASSIST DEPUTY WITH TREE IN ROAD  CFS No: 12005736  TRAFFIC HAZARD COUNTY ROAD 325 / COUNTY ROAD 324
 Message From Dispatch DISREGARD MAJOR AT MONTE CARLO AND 380 LOCATION WAS CHANGED AND IS PNFD SORRY FOR THE PAGE
 Message From Dispatch MUTUAL AID SFIRE 6031 FM 546 CFS 12006098
 Message From Dispatch MUTUAL AID STRUCTURE FIRE 6031 FM 546 CFS 12006098-- MUTUAL AID WITH BRANCH
 12008893  TEST CALL  305 S POWELL PKWY IN ANNA   {ANNA FIRE DEPT}   W 5 ST / W 6 ST  [ANFD DIST: ANF1 GRID: 1900]  UNITS: ANF1  ST RMK: 17J4  CFS RMK 14:46 TEST  TEST TEST ONLY  {CAD006 14:46}
 
Contact: Jeff Erickson <jerickson919@gmail.com>
Sender: farmersvillefd+caf_=7042218878=vtext.com@gmail.com
12053430  FIRST RESPONDERS  1878 COUNTY ROAD 655 IN COLLIN COUNTY   {BRANNUM RES # 1518}   PRIVATE ROAD 5398 / COUNTY ROAD 653  [ ESS I  {CAD001 23:00} (02/02)

 */
 
 public class TXCollinCountyParser extends FieldProgramParser {

  private static final Pattern TRAIL_JUNK_PTN = Pattern.compile("\\{CAD|  \\{C|\\[ ESS");
   private static final Pattern CFS_ID_PTN = Pattern.compile(" CFS (\\d{8})\\b");
   private static final Pattern LEAD_DASH_PTN = Pattern.compile("^[ -]+");
   private static final Pattern DIST_GRID_PTN = Pattern.compile("\\[([A-Z]+) DIST: .* GRID: (\\d+) *\\]");
   
   private static final String[] DOUBLE_CITY_LIST = new String[] {
     "COLLIN COUNTY",
     "BLUE RIDGE",
     "LOWRY CROSSING",
     "ROYSE CITY",
     "ST PAUL",
     "VAN ALSTYNE",
   };
   
   public TXCollinCountyParser() {
     super("COLLIN COUNTY", "TX",
         "MASH UNITS:UNIT ST_RMK:INFO CFS_RMK:INFO");
   }
   
   @Override
   public String getFilter() {
     return "ccsodispatch@co.collin.tx.us";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     String alert = null;
     if (body.startsWith("Message From Dispatch ")) {
       body = body.substring(22).trim();
       if (body.startsWith("MUTUAL AID")) {
         Matcher match  = CFS_ID_PTN.matcher(body);
         if (match.find()) {
           data.strCallId = match.group(1);
           parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ, body.substring(0,match.start()).trim(), data);
           String sInfo = body.substring(match.end());
           match = LEAD_DASH_PTN.matcher(sInfo);
           if (match.find()) sInfo = sInfo.substring(match.end());
           data.strSupp = sInfo;
           return true;
         }
         
         parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ, body, data);
         return true;
       }
       
       alert = body;
     }
 
     // Remove trailing ID
    Matcher match = TRAIL_JUNK_PTN.matcher(body);
    if (match.find()) body = body.substring(0,match.start()).trim();
     
     // Some variants include the source and map code in square brackets
     // If this is one of those, extract that information and remove the
     // square bracket construct
    match = DIST_GRID_PTN.matcher(body);
     if (match.find()) {
       data.strSource = match.group(1).trim();
       data.strMap = match.group(2).trim();
       body = body.substring(0,match.start()) + body.substring(match.end());
     }
     
    int pt = body.indexOf("CFS No: ");
     if (pt >= 0) {
       data.strSupp = body.substring(0,pt).trim();
       body = body.substring(pt+8).trim();
     }
     
     // It seems that the original dispatch message uses double blanks as field
     // delimiters, but that some helpful? forwarding services are eliminating
     // the redundant blanks.  If this text message has the original double
     // blank delimiters, we can call parseFields to finsh things off
     body = body.replace("CFS RMK ", "CFS RMK: ");
     body = body.replaceAll(" +/ +", " / ");
     if (super.parseMsg(body, data)) {
       
       if (data.strCity.equalsIgnoreCase("COLLIN COUNTY")) data.strCity = "";
       if (data.strAddress.length() > 0) return true;
     }
     
     if (alert != null) return data.parseGeneralAlert(alert);
     return false;
   }
   
   // Parse a mashup of ID, CALL, ADDR, CITY, and Cross streets all of which might
   // or might not be separated by double blank delimiters
   private static final Pattern ID_PTN = Pattern.compile("^(\\d{8}) +");
   private static final Pattern BRACKET_PTN = Pattern.compile(" +\\{(.*?)\\} *");
   private static final Pattern IN_PTN = Pattern.compile(" +IN +", Pattern.CASE_INSENSITIVE);
   private class MashField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       
       // Start with easy stuff.  ID is always the first token
       Matcher match = ID_PTN.matcher(field);
       if (!match.find()) abort();
       data.strCallId = match.group(1);
       field = field.substring(match.end());
       
       // A field in {} is considered a place name
       match = BRACKET_PTN.matcher(field);
       if (match.find()) {
         data.strPlace = match.group(1);
         field = field.substring(0,match.start()) + "  " + field.substring(match.end());
       }
       
       // Break up what is left by any double blank delimiters and see what we have to work with
       String[] flds = field.split("  +");
       switch (flds.length) {
       
       case 1:
         
         parseAddr(CALL | CROSS, flds[0], data);
         return;
         
       case 2:
         
         // Two fields is ambiguous, we don't know if if the break is call/address and cross
         // or call and address/cross.  We'll check both for an IN keyword, which
         // would mark the address
         if (parseAddr(OPTIONAL | CROSS, flds[1], data)) {
           data.strCall = flds[0];
           return;
         }
         
         if (parseAddr(OPTIONAL | CALL, flds[0], data)) {
           data.strCross = flds[1];
           return;
         }
         
         break;
         
       case 3:
         
         // Three fields breaks into call, address/city and cross
         data.strCall = flds[0];
         parseAddr(0, flds[1], data);
         data.strCross = flds[2];
         return;
         
       // More than 3 fields, we haven't a clue what to do.  
       default:
         abort();
       }
     }
     
     private static final int OPTIONAL = 1;
     private static final int CALL = 2;
     private static final int CROSS = 4;
     private boolean parseAddr(int flags, String sAddress, Data data) {
       
       // Break out flag options
       boolean optional = (flags & OPTIONAL) != 0;
       boolean call = (flags & CALL) != 0;
       boolean cross = (flags & CROSS) != 0;
       
       int parseFlags = 0;
       StartType st = StartType.START_ADDR;
       if (call) {
         st = StartType.START_CALL;
         parseFlags |= FLAG_START_FLD_REQ; 
       }
       
       // Next, see if address contains an IN keyword separated call/address from city/cross
       Matcher match = IN_PTN.matcher(sAddress);
       if (match.find()) {
         
         // Check, use smart parser to split call and address
         parseAddress(st, parseFlags | FLAG_ANCHOR_END, sAddress.substring(0,match.start()), data);
         
         // Now lets look at the right side of the IN keyword
         // If we aren't handling cross streets, it is all city
         String tail = sAddress.substring(match.end());
         if (!cross) {
           data.strCity = tail;
           return true;
         }
         
         // Otherwise, see if it starts with a two word city, if it does
         // use that city to break tail into city and cross streets
         String tail2 = tail.toUpperCase();
         for (String tc : DOUBLE_CITY_LIST) {
           if (tail2.startsWith(tc)) {
             data.strCity = tail.substring(0,tc.length());
             data.strCross = tail.substring(tc.length()).trim();
             return true;
           }
         }
         
         // Otherwise first word of tail is city, rest is cross
         Parser p = new Parser(tail);
         data.strCity = p.get(' ');
         data.strCross = p.get();
         return true;
       }
       
       // No IN keyword, if this was an optional parse, return failure
       if (optional) return false;
       
       // no IN keyword, which we assume means no city
       // Use smart address parser to separate call, adddress, and cross
       if (!cross) parseFlags |= FLAG_ANCHOR_END;
       parseAddress(st, parseFlags, sAddress, data);
       if (cross) data.strCross = getLeft();
       return true;
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("MASH")) return new MashField();
     return super.getField(name);
   }
   
   @Override
   public String getProgram() {
     return "ID CALL ADDR CITY PLACE X SRC MAP UNIT INFO";
   }
   
 //  
 //  private static final String[] CITY_LIST = new String[] {
 //    "COLLIN COUNTY",
 //    "ALLEN",
 //    "ANNA",
 //    "BLUE RIDGE",
 //    "CARROLLTON",
 //    "CELINA",
 //    "COPEVILLE ",
 //    "DALLAS",
 //    "FAIRVIEW",
 //    "FARMERSVILLE",
 //    "FRISCO",
 //    "GARLAND",
 //    "JOSEPHINE",
 //    "LAVON",
 //    "LOWRY CROSSING",
 //    "LUCAS",
 //    "MCKINNEY",
 //    "MELISSA",
 //    "MURPHY",
 //    "NEVADA",
 //    "NEW HOPE",
 //    "PARKER",
 //    "PLANO",
 //    "PRINCETON",
 //    "PROSPER",
 //    "RICHARDSON",
 //    ROYSE CITY",
 //    "SACHSE",
 //    "ST PAUL",
 //    "VAN ALSTYNE",
 //    "WESTMINSTER ",
 //    "WESTON",
 //    "WYLIE"
 //  };
 }
