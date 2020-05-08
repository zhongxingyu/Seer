 package net.anei.cadpage.parsers.CT;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Tolland County, CT
 Contact: Tabby Muniz <tabbymuniz@ymail.com>
 Sender: TN@TollandCounty911.org
 
 (TN Alert) 10 COTTAGE ST / UNIT D Vernon ALS Difficulty Breathing 21:14 Cross Street ST BERNARDS TER / SCHOOL ST 2011-00001770
 [TN Alert]  DOBSON RD / CAMPBELL AVE Vernon Vehicle Accident 2ND AMB  12:59 Cross Street 2011-00001773
 [TN Alert]  32 FRANKLIN PARK E / Vernon ALS DIFF BREATHING 09:07 Cross Street DEAD END / FRANKLIN ST 2011-00001772
 [TN Alert]  140 WINDSORVILLE RD / Vernon BLS back pain 02:21 Cross Street PENFIELD AVE / SEPTIC TREATMENT PLANT 2011-00001771
 [TN Alert]  DOBSON RD / CAMPBELL AVE Vernon Vehicle Accident 12:59 Cross Street 2011-00001773
 [TN Alert]  95 HARTFORD TPKE / Vernon * Golf Land Vehicle Accident MINOR INJURIES - EVALUATION
 [TN Alert]  67 BLUE RIDGE DR / Vernon Tree/Wires Down 14:38 Cross Street HICKORY HL / AUTUMN WOOD LN 2011-00001775
(TN Alert) 155 W   MAIN ST 317 / Vernon ALS DIFFICULTY BREATHING 14:03 Cross Street ORCHARD ST, SPRING ST / WARD ST 2011-00001981
  
 */
 
 public class CTTollandCountyParser extends SmartAddressParser {
   
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
     "MASHAPAUG"
   };
   
  private static final Pattern TIME_PTN = Pattern.compile("\\b\\d\\d: \\d\\d\\b");
   private static final Pattern ID_PTN = Pattern.compile("\\b\\d{4}-\\d{8}$");
   
   public CTTollandCountyParser() {
     super(CITY_LIST, "TOLLAND COUNTY", "CT");
   }
   
   @Override
   public String getFilter() {
     return "TN@TollandCounty911.org";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("TN Alert")) return false;
     
     // We are invoking the smart address parser strictly to find city, it
     // shouldn't have to do much parsing.  If it doesn't find a city, bail out.
     // The slash confuses the parse logic, so switch it to something unusual
     body = body.replace('/', '^');
     parseAddress(StartType.START_ADDR, body, data);
     if (data.strCity.length() == 0) return false;
     String sAddr = data.strAddress.replace('^', '/');
     body = getLeft().replace('^', '/');
     
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
 
 }
