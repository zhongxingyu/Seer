 package net.anei.cadpage.parsers.CT;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Tolland County, CT
 Contact: Tabby Muniz <tabbymuniz@ymail.com>
 Contact: leanne buden <lbuden38@gmail.com>
 Sender: TN@TollandCounty911.org
 
 (TN Alert) 10 COTTAGE ST / UNIT D Vernon ALS Difficulty Breathing 21:14 Cross Street ST BERNARDS TER / SCHOOL ST 2011-00001770
 [TN Alert]  DOBSON RD / CAMPBELL AVE Vernon Vehicle Accident 2ND AMB  12:59 Cross Street 2011-00001773
 [TN Alert]  32 FRANKLIN PARK E / Vernon ALS DIFF BREATHING 09:07 Cross Street DEAD END / FRANKLIN ST 2011-00001772
 [TN Alert]  140 WINDSORVILLE RD / Vernon BLS back pain 02:21 Cross Street PENFIELD AVE / SEPTIC TREATMENT PLANT 2011-00001771
 [TN Alert]  DOBSON RD / CAMPBELL AVE Vernon Vehicle Accident 12:59 Cross Street 2011-00001773
 [TN Alert]  95 HARTFORD TPKE / Vernon * Golf Land Vehicle Accident MINOR INJURIES - EVALUATION
 [TN Alert]  67 BLUE RIDGE DR / Vernon Tree/Wires Down 14:38 Cross Street HICKORY HL / AUTUMN WOOD LN 2011-00001775
 (TN Alert) 155 W   MAIN ST 317 / Vernon ALS DIFFICULTY BREATHING 14:03 Cross Street ORCHARD ST, SPRING ST / WARD ST 2011-00001981
 (TN Alert) 36 GARDNER ST / Warehouse Point Fire Alarm 17:11 Cross Street PLEASANT ST / BRIDGE ST 2011-00000190
 (TN Alert) 69 BROOKLYN ST / (CLERKS OFC) Vernon BLS SICK- ELDERLY FEMALE  13:20 Cross Street COURT ST / VERNON AVE 2011-00002812
 
 Contact: Jason Carlton <jcarlton19@gmail.com>
 (TN Alert) 29 BRACE RD / Somers ALS ARM INJURY, BLEEDING 22:42 Cross Street GEORGE WOOD RD / HALL HILL RD 2011-00000821
 (TN Alert) 69 COLTON RD / Somers ALS ? CVA 18:34 Cross Street PARSONS ST / SOKOL RD 2011-00000820
 
 Contact: R R <rlandry0769@gmail.com>
 (TN Alert) 234 JAMES RD / Ashford BLS Fall 15:25 Cross Street HORSE HILL RD / WESTFORD RD 2011-00001969
 
 Contact: Stuart Cobb <scobb213@gmail.com>
 (TN Alert) 140 RIVER RD / Willington * Lyon Manor BLS Commital 06:29 Cross Street DEPOT RD / VILLAGE ST 2011-00000860
 
 Contact: Peter Krawetzky <pkrawetzky@gmail.com>
 Sender: north-coventry@tollandcounty911.com
 (TN Alert) 957 N  RIVER RD / Coventry ALS DIFF BREATHING 17:59 Cross Street MERROW RD / GOOSE LN 2012-00000183
 
 */
 
 public class CTTollandCountyParser extends SmartAddressParser {
   
   private static final Pattern TIME_PTN = Pattern.compile("\\b\\d\\d: \\d\\d\\b");
   private static final Pattern ID_PTN = Pattern.compile("\\b\\d{4}-\\d{8}$");
   
   public CTTollandCountyParser() {
     super(CITY_LIST, "TOLLAND COUNTY", "CT");
   }
   
   @Override
   public String getFilter() {
    return "@TollandCounty911.org,@TollandCounty911.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("TN Alert")) return false;
     
     // We are invoking the smart address parser strictly to find city, it
     // shouldn't have to do much parsing.  If it doesn't find a city, bail out.
     // The slash confuses the parse logic, so switch it to something unusual
     // Ditto for parrens
     body = escape(body);
     parseAddress(StartType.START_ADDR, body, data);
     if (data.strCity.length() == 0) return false;
     String sAddr = unescape(data.strAddress);
     body = unescape(getLeft());
     
     // Address always has a slash, which the address parser turned to an ampersand
     // What is in front of that becomes the address
     int pt = sAddr.indexOf('/');
     if (pt < 0) return false;
     data.strAddress = sAddr.substring(0,pt).trim();
     sAddr = sAddr.substring(pt+1).trim();
     
     // if what comes after the slash is a street name, append it to address
     // If not, put it in the apt field
     Result res = parseAddress(StartType.START_ADDR, FLAG_ANCHOR_END, sAddr);
     if (res.getStatus() > 0) {
       data.strAddress = append(data.strAddress, " & ", sAddr);
     } else {
       data.strApt = sAddr;
     }
     
     // Everything from city to time field is the call description
     if (body.startsWith("*")) body = body.substring(1).trim();
     Matcher match = TIME_PTN.matcher(body);
     if (!match.find()) {
       data.strCall = body;
       return true;
     }
     data.strCall = body.substring(0,match.start()).trim();
     body = body.substring(match.end()).trim();
     
     // What is left should be a cross street
     if (body.startsWith("Cross Street ")) body = body.substring(13).trim();
     
     // Strip ID from end of what is left
     match = ID_PTN.matcher(body);
     if (match.find()) {
       data.strCallId = match.group();
       body = body.substring(0,match.start()).trim();
     }
     
     data.strCross = body;
     return true;
   }
   
   private static final String[] ESCAPE_CODES = new String[]{
     "/", "\\s",
     "(", "\\lp",
     ")", "\\rp",
     "[", "\\lb",
     "]", "\\rb"
   };
   
   private static String escape(String fld) {
     for (int j = 0; j<ESCAPE_CODES.length; j+=2) {
       fld = fld.replace(ESCAPE_CODES[j], ESCAPE_CODES[j+1]);
     }
     return fld;
   }
   
   private static String unescape(String fld) {
     for (int j = 0; j<ESCAPE_CODES.length; j+=2) {
       fld = fld.replace(ESCAPE_CODES[j+1], ESCAPE_CODES[j]);
     }
     return fld;
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ANDOVER",
     "BOLTON",
     "COLUMBIA",
     "COVENTRY",
     "ELLINGTON",
     "HEBRON",
     "MANSFIELD",
     "SOMERS",
     "STAFFORD",
     "TOLLAND",
     "UNION",
     "VERNON",
     "WILLINGTON",
 
     "COVENTRY LAKE",
     "SOUTH COVENTRY",
     "CRYSTAL LAKE",
     "STAFFORD SPRINGS",
     "STORRS",
     "CENTRAL SOMERS",
     "ROCKVILLE",
     "MASHAPAUG",
     
     "WAREHOUSE POINT",
     
     // Windham county
     "ASHFORD",
     "WILLIMANTIC",
     "WINDHAM"
   };
 
 }
