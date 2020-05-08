 package net.anei.cadpage.parsers.OH;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Licking County, OH
 Contact: Tom Schwab <ts813822@gmail.com>
 Sender: LC911@lcounty.com
 
 LC911:ILLNESS    24 S OREGON ST   JOHNSTOWN
 LC911:SICK PERSON   231  COMMERCE BL   JOHNSTOWN
 LC911:ABDOMINAL PAIN   76  YARDNER ST   LOT 9 JOHNSTOWN
 LC911:TRAFFIC ACCIDENT  551 W COSHOCTON ST JOHNSTOWN
 LC911:HEMMORAGE/LACERATION - Location:   14584.00  JUG STREET RD  JERSEY TWP
 
 LC911:[3298]-- - Nature: HEMMORAGE/LACERATION - Location: 36.00 S MAIN ST   JOHNSTOWN -
 LC911:[3298]-- - Nature: FALLS - Location:    8485.00  WINDY HOLLOW RD NW  LIBERTY TWP -
 LC911:[4503]-- - Nature: ABDOMINAL PAIN/PROBLEM - Location:    322 BEECH RD NW  JERSEY TWP .  Fema
 LC911:[3298]-- - Nature: HEMMORAGE/LACERATION - Location: 188.00  COMMERCE BL  JOHNSTOWN -
 LC911:[3298]-- - Nature: TRAUMATIC INJURIES (SPECIFIC) - Location:     231.00  COMMERCE BL
 LC911:[3064]-- - Nature: SICK PERSON (SPECIFIC DIAGNOSIS) - Location:    4977.00  MINK ST NW  MONROE TWP - Comments:
 
 LC911:- Nature: ILLNESS - NON SPECIFIC - Location:      65.00 S WILLIAMS ST  Suite:RM 206 JOHNSTOWN
 LC911:- Nature: CHEST PAIN - Location:     119.00  WESTVIEW DR JOHNSTOWN -
 LC911:- Nature: FIRE - HOUSE/ MOBILE HOME -SINGLE - Location:    5598.00  RACCOON VALLEY RD NW  ST ALBANS TWP -
 LC911:- Nature: HEMMORAGE/LACERATION - Location:     250.00 WOODGATE CT   JOHNSTOWN -
 LC911:- Nature: ILLNESS - NON SPECIFIC - Location:      65.00 S WILLIAMS ST  Suite:APT 102 JOHNSTOWN
 LC911:- Nature: ILLNESS - NON SPECIFIC - Location:      65.00 S WILLIAMS ST  Suite:APT 102 JOHNSTOWN
 LC911:- Nature: HEMMORAGE/LACERATION - Location:     250.00 WOODGATE CT   JOHNSTOWN
 LC911:- Nature: DIABETIC PROBLEMS - Location:     406.00 HILLVIEW DR   JOHNSTOWN -
 LC911:- Nature: CHOKING - Location:     155.00  CLARK DR JOHNSTOWN -
 
 */
 public class OHLickingCountyParser extends SmartAddressParser {
   
   private static final String[] CITY_LIST = new String[]{
       "ALEXANDRIA",
       "BUCKEYE LAKE",
       "GRANVILLE",
       "GRATIOT",
       "HANOVER",
       "HARTFORD",
       "HEATH",
       "HEBRON",
       "JOHNSTOWN",
       "KIRKERSVILLE",
       "NEWARK",
       "NEW ALBANY",
       "PATASKALA",
       "REYNOLDSBURG",
       "ST LOUISVILLE",
       "UTICA",
 
       "BENNINGTON TWP",
       "BOWLING GREEN TWP",
       "BURLINGTON TWP",
       "EDEN TWP",
       "ETNA TWP",
       "FALLSBURY TWP",
       "FRANKLIN TWP",
       "GRANVILLE TWP",
       "HANOVER TWP",
       "HARRISON TWP",
       "HARTFORD TWP",
       "HOPEWELL TWP",
       "JERSEY TWP",
       "LIBERTY TWP",
       "LICKING TWP",
       "MADISON TWP",
       "MARY ANN TWP",
       "MCKEAN TWP",
       "MONROE TWP",
       "NEWARK TWP",
       "NEWTON TWP",
       "PERRY TWP",
       "ST ALBANS TWP",
       "UNION TWP",
       "WASHINGTON TWP",
 
       "BEECHWOOD TRAILS",
       "GRANVILLE SOUTH",
       "HARBOR HILLS",
       "BROWNSVILLE",
       "ETNA",
       "HOMER",
       "JACKSONTOWN",
   };
   
   public OHLickingCountyParser() {
     super(CITY_LIST, "LICKING COUNTY", "OH");
   }
   
   @Override
   public String getFilter() {
     return "LC911@lcounty.com";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (!body.startsWith("LC911:")) return false;
     body = body.substring(6).trim();
     
     // get rid of decimal points on the house numbers
     body = body.replace(".00", "");
     
     Parser p = new Parser(body);
     p.getOptional("- Nature: ");
     data.strCall = p.getOptional(" - Location: ");
     if (data.strCall.length() == 0) data.strCall = p.get("  ");
     body = p.get();
     
     // Smart address parser has to take things from here
     // Leaving room for a possible apt number between address and city
     parseAddress(StartType.START_ADDR, FLAG_PAD_FIELD, body, data);
     String sApt = getPadField();
     if (sApt.startsWith("Suite:")) sApt = sApt.substring(6).trim();
     if (sApt.startsWith("APT ")) sApt = sApt.substring(4).trim();
     data.strApt = sApt;
     
     String sInfo = getLeft();
     if (sInfo.startsWith("-") || sInfo.startsWith(".")) sInfo = sInfo.substring(1).trim();
     if (sInfo.startsWith("Comments:")) sInfo = sInfo.substring(9).trim();
     data.strSupp = sInfo;
     
     return true;
   }
 }
