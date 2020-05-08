 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Genesee County, NY
 Contact: rtripp@2ki.net
 Contact: Robert Bennett <rbennett8994@gmail.com>
 Sender: 911center@co.genesee.ny.us
 
 [Dispatch]  Acc PIAA ** 392.3 RT 90 E** , BATAVIA - ** ONE VEHICLE ROLLOVER ** LEWISTON RD, I 90 / KELSEY RD, I 90 ** 07/12/11 17:29 **
 [Dispatch]  Alarm Fire ** ** 2614 W  MAIN STREET RD , BATAVIA - ** 2ND FLOOR SMOKE DETECRTOR KENNETH HERMAN RESIDENCE ** READ
 [Dispatch]  Fire Auto ** 387.6 RT 90 W** , BATAVIA - ** POSS TRACTOR TRAILER FIRE - BOX TRAILER ** I 90, I-90 CONNECTOR / I 90,
 [Dispatch]  Fire Nat Propane Gas ** ** 8470 VIOLET LN , BATAVIA - ** COMPL STRUCK GAS LINE OUT THE RESIDENCE 1/2" LINE  ** DANA
 [Dispatch]  Acc PIAA ** ** 5402 ELLICOTT STREET RD , BETHANY - ** ONE VEHICLE ACCIDENT ** CLAPSADDLE RD / MAYNE RD ** 06/19/11 02:22 **
 (Dispatch) Acc PIAA ** FRONTIER KITCHENS** 8392 LEWISTON RD , BATAVIA - ** TWO CAR MVA MINER INJ ** PARK RD / W MAIN STREET RD
 (Dispatch) Alarm Fire ** COLLEGE VILLAGE** 8170 BATAVIA STAFFORD TOWNLINE RD , BATAVIA - ** F 201- FIRE ALARM - ALARM HAS RESE
 [Dispatch]  Acc PIAA ** Johnson Automotive** 3924 W  MAIN STREET RD , BATAVIA - ** 2 CAR MVA W INJ ** COUNTY BLDG / COLONIAL BL
 
 Sender: 7771
 GENESEE COUNTY DISPATCH Unit:251 Status:Dispatched Acc PIAA ** BANK STREET RD , ASSEMBLYMAN R. STEPHEN HAWLEY DR BATAVIA - ** 3 CAR MVA ** ** 01/19/12 11:07
 GENESEE COUNTY DISPATCH Unit:251 Status:Dispatched Acc PIAA * ** BATAVIA STAFFORD TOWNLINE RD , FOTCH RD BATAVIA - ** ** ** 01/20/12 16:08 ** 2012-00001287 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH Unit:251 Status:Dispatched Fire CO Detector ** ** 3645 GALLOWAY RD , BATAVIA - ** CO DECTOR IS GOING OFF ** LEWISTON RD / KELSEY RD ** 01/18/12 18:52 ** 2012-00000016 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH Unit:251 Status:Dispatched Acc PIAA * ** BANK STREET RD , ASSEMBLYMAN R. STEPHEN HAWLEY DR BATAVIA - ** 3 CAR MVA ** ** 01/19/12 11:07 ** 2012-00000188 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH Unit:EP56 Status:Dispatched EMD Alpha ** ** 2128 MAIN RD , PEMBROKE - ** 78 Y/O MALE COMPLAINING OF DIZZINESS ** INDIAN FALLS RD / CLEVELAND RD ** 01/22/12 06:57 ** 2012-00000021 ** TXT STOP to opt-out
 
 Contact: Stephen Smelski <steveepfd@gmail.com>
 Sender: 77712
 GENESEE COUNTY DISPATCH Unit:EP57 Status:Dispatched EMD Alpha ** Batavia Mobile Home Park** 3322 W MAIN STREET RD , BATAVIA - 31 ** 40 Y/O MALE VOMITTING ** WORTENDYKE RD / STEGMAN RD ** 01/29/12 10:53 ** 2012-00000025 ** TXT STOP to opt-out
 GENESEE COUNTY DISPATCH EMD Charlie ** ** 1858 MAIN RD , PEMBROKE - ** 91 YOM POSS STROKE ** CLEVELAND RD / BOYCE RD ** 01/30/12 20:25 ** 2012-00000026 ** TXT STOP to opt-out
 
 */
 
 
 public class NYGeneseeCountyParser extends FieldProgramParser {
   
   private static Pattern MARKER = Pattern.compile("^GENESEE COUNTY DISPATCH +(?:Unit:(.*?) Status:Dispatched +)?");
   
   public NYGeneseeCountyParser() {
     super(CITY_LIST, "GENESEE COUNTY", "NY",
            "CALL PLACE? ADDR CITY? INFO! X DATETIME ID");
   }
   
   @Override
   public String getFilter() {
     return "911center@co.genesee.ny.us,7771";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     do {
       if (subject.equals("Dispatch")) break;
       
       Matcher match = MARKER.matcher(body);
       if (match.find()) {
         data.strUnit = getOptGroup(match.group(1));
         body = body.substring(match.end());
         break;
       }
         
       return false;
     } while (false);
     String flds[] = body.split("\\*\\*");
     if (flds.length < 3) return false;
     return parseFields(flds, data);
   }
   
   @Override
   public String getProgram() {
     return "UNIT " + super.getProgram();
   }
   
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith("*")) field = field.substring(0,field.length()-1).trim();
       super.parse(field, data);
     }
   }
   
   private static final Pattern TRAIL_DIGITS = Pattern.compile(" +-(?: (\\d+))?$");
   private class MyAddressField extends AddressField {
     
     @Override
     public boolean checkParse(String field, Data data) {
       
       // Remove trailing dash and digits
       Matcher match = TRAIL_DIGITS.matcher(field);
       if (match.find()) {
         String sInfo = match.group(1);
         if (sInfo != null) data.strSupp = append(data.strSupp, " / ", sInfo);
         field = field.substring(0,match.start());
       }
       
       // If field contains comma, parse as address and cross / city
       int pt = field.indexOf(',');
       if (pt >= 0) {
 	      String sAddr = field.substring(0,pt).trim();
 	      String sCity = field.substring(pt+1).trim();
 	      parseAddress(StartType.START_ADDR, FLAG_ONLY_CROSS | FLAG_ONLY_CITY | FLAG_ANCHOR_END, sCity, data);
         parseAddress(sAddr, data);
 	      return true;
       }
       
       // Otherwise, treat it as an address if it starts with a digit
       // or is recognized as an address
       if (field.length() < 1) return false;
       if (Character.isDigit(field.charAt(0)) || checkAddress(field) > 0) {
         parseAddress(field, data);
         return true;
       }
       
       // Otherwise, not a valid address field
       return false;
     }
     
     @Override
     public void parse(String field, Data data) {
       if (!checkParse(field, data)) abort();
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR CITY";
     }
   }
   
   private class MyCityField extends CityField {
     
     @Override
     public boolean canFail() {
       return true;
     }
     
     @Override
     public boolean checkParse(String field, Data data) {
       
       // If we got the city from the address field, we don't need this one
       if (data.strCity.length() > 0) return false;
       
       // Strip off trailing dash
       if (field.startsWith(",")) field = field.substring(1).trim();
       if (field.endsWith("-")) field = field.substring(0,field.length()-1).trim();
       data.strCity = field;
       return true;
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("CITY")) return new MyCityField();
     return super.getField(name);
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ALABAMA",
     "ALEXANDER",
     "ATTICA",
     "BATAVIA",
     "BERGEN",
     "BERGEN",
     "BETHANY",
     "BYRON",
     "CORFU",
     "DARIEN",
     "ELBA",
     "LE ROY",
     "OAKFIELD",
     "PAVILION",
     "PEMBROKE",
     "STAFFORD"
   };
 }
