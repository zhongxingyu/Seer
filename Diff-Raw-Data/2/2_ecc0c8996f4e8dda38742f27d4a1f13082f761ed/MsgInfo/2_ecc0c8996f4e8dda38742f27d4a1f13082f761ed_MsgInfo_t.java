 package net.anei.cadpage.parsers;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgParser.CountryCode;
 
 /**
  * This class contains all of the useful data fields that are parsed from
  * the actual message text
  */
 public class MsgInfo {
   
   // Flags that will be queried by the MsgParser.getMapFlags() method
   // the alter aspects of the map query string generation
   
   // Suppress LA -> LN translation
   public static final int MAP_FLG_SUPPR_LA = 1;
   
   // Suppress EXT removal
   public static final int MAP_FLG_SUPPR_EXT = 2;
   
   // Add default city/county in back of parsed city/county
   public static final int MAP_FLG_ADD_DEFAULT_CNTY = 4;
   
   // Suppress NO EO SO WO -> & translation
   public static final int MAP_FLG_SUPPR_DIRO = 8;
 
   private String strCall;
   private String strPlace;
   private String strAddress;
   private String strCity;
   private String strApt;
   private String strCross;
   private String strBox;
   private String strUnit;
   private String strState;
   private String strMap;
   private String strCallId;
   private String strPhone;
   private String strSupp;
   private String strCode;
   private String strSource;
   private String strName;
   private String strPriority;
   private String strChannel;
   private String strGPSLoc;
   private String strDate;
   private String strTime;
   private String strInfoURL;
   private String defCity;
   private String defState;
   private MsgParser.CountryCode countryCode;
   
   // Flag set when parser determines that message is incomplete
   // and another part should be expected
   private boolean expectMore;
   
   // Cached map address
   private String strBaseMapAddress = null;
   
   // Parser used to create this information block
   private MsgParser parser;
   
 
   /**
    * Temporary data class used to pass information to constructor
    * @author ken
    *
    */
   public static class Data {
     public String strCall;
     public String strPlace;
     public String strAddress;
     public String strCity;
     public String strApt;
     public String strCross;
     public String strBox;
     public String strUnit;
     public String strState;
     public String strMap;
     public String strCallId;
     public String strPhone;
     public String strSupp;
     public String strCode;
     public String strSource;
     public String strName;
     public String strPriority;
     public String strChannel;
     public String strGPSLoc;
     public String strDate;
     public String strTime;
     public String strInfoURL;
     public String defCity;
     public String defState;
     public MsgParser.CountryCode countryCode;
     
     public boolean expectMore;
     
     public MsgParser parser;
     
     public Data(MsgParser parser) {
       this.parser = parser;
       initialize();
     }
     
     /**
      * Initialize existing Data structure to original state
      */
     public void initialize() {
       expectMore = false;
       
       strCall = "";
       strPlace = "";
       strAddress = "";
       strCity = "";
       strApt = "";
       strCross = "";
       strBox= "" ;
       strUnit= "" ;
       strState="";
       strMap = "";
       strCallId = "";
       strPhone="";
       strSupp="";
       strCode="";
       strSource = "";
       strName = "";
       strPriority = "";
       strChannel = "";
       strGPSLoc = "";
       strDate = "";
       strTime = "";
       strInfoURL = "";
       defCity = "";
       defState="";
       countryCode = MsgParser.CountryCode.US;
       
       if (parser != null) {
         this.defCity = parser.getDefaultCity();
         this.defState = parser.getDefaultState();
         this.countryCode = parser.getCountryCode();
       }
     }
     
     /**
      * Clear any information left over from a failed parse attempt, and 
      * set the data field to return a general alert status
      * @param message message to be set as the general alert text
      * @returns always returns true
      */
     public boolean parseGeneralAlert(String message) {
       initialize();
       strCall = "GENERAL ALERT";
       strPlace = message;
       return true;
     }
     
     /**
      * Clear any information left over from a failed parse attempt, and 
      * set the data field to return a RUN REPORT status
      * @param message message to be set as the general alert text
      * @returns always returns true
      */
     public boolean parseRunReport(String message) {
       String saveCallId = strCallId;
       initialize();
       strCall = "RUN REPORT";
       strCallId = saveCallId;
       strPlace = message;
       return true;
     }
     
 
     /**
      * @return relative score that can be used to pick out the better result
      * from multiple possible parsings
      */
     public int score() {
       int result = 0;
       if (strAddress.length() > 0) result += 10000;
       if (strCall.length() > 0 && !strCall.equals("ALERT") && !strCall.equals("GENERAL ALERT")) result += 1000;
       if (strCity.length() > 0) result += 100;
       if (strCross.length() > 0) result += 100;
       if (strApt.length() > 0) result += 100;
       if (strBox.length() > 0) result += 10;
       if (strUnit.length() > 0) result += 10;
       if (strState.length() > 0) result += 10;
       if (strMap.length() > 0) result += 10;
       if (strPlace.length() > 0) result += 10;
       if (strCallId.length() > 0) result += 10;
       if (strName.length() > 0) result += 10;
       if (strPhone.length() > 0) result += 10;
       if (strSupp.length() > 0) result += 1;
       if (strCode.length() > 0) result += 10;
       if (strSource.length() > 0) result += 10;
       if (strState.length() > 0) result += 10;
       if (strPriority.length() > 0) result += 10;
       if (strChannel.length() > 0) result += 10;
       if (strGPSLoc.length() > 0) result += 10;
       if (strDate.length() > 0) result += 10;
       if (strTime.length() > 0) result += 10;
       return result;
     }
   }
 
   /**
    * constructor
    * @param info data object containing all message information
    */
   public MsgInfo(Data info) {
     strCall = info.strCall;
     strPlace = info.strPlace;
     strAddress = info.strAddress;
     strCity = info.strCity;
     strApt = info.strApt;
     strCross = info.strCross;
     strBox = info.strBox;
     strUnit = info.strUnit;
     strState = info.strState;
     strMap = info.strMap;
     strCallId = info.strCallId;
     strPhone = info.strPhone;
     strSupp = info.strSupp;
     strCode = info.strCode;
     strSource = info.strSource;
     strName = info.strName;
     strPriority = info.strPriority;
     strChannel = info.strChannel;
     strGPSLoc = info.strGPSLoc;
     strDate = info.strDate;
     strTime = info.strTime;
     strInfoURL = info.strInfoURL;
     defCity = info.defCity;
     defState = info.defState;
     countryCode = info.countryCode;
     expectMore = info.expectMore;
     
     parser = info.parser;
   }
   
   /**
    * @return call ID
    */
   public String getCallId() {
     return strCallId;
   }
 
   /**
    * @return the Call description
    */
   public String getCall() {
     return strCall;
   }
   
   /**
    * @return return place name
    */
   public String getPlace() {
     return strPlace;
   }
 
   /**
    * @return the call address
    */
   public String getAddress() {
     return strAddress;
   }
   
   /*
    * @return call title
    */
   public String getTitle() {
     StringBuilder sb = new StringBuilder();
     if (strPriority.length() > 0) {
       sb.append("P:");
       sb.append(strPriority);
       sb.append(' ');
     }
     if (strCode.length() > 0) {
       sb.append(strCode);
       sb.append(" - ");
     }
     if (noCall() && strSupp.length() > 0) {
       sb.append(strSupp);
     } else {
       sb.append(strCall);
     }
     return sb.toString();
   }
 
   public boolean noCall() {
     return strCall.length() == 0 || strCall.equals("ALERT");
   }
   
   /**
    * Calculate map search string to be passed to Google maps
    * @param useGPS true if GPS coordinates should be used in preference to map address
    * @param overrideCity if non-null, this value should override parser default city
    * @param overrideState if non-null, this value should override parser default state
    * @return return mapping address or null if there is no map address
    */
   public String getMapAddress(boolean useGPS, String overrideCity, String overrideState) {
     
     // See if we can find any GPS coordinates, if we do, return those
     String mapAddr = null;
     if (useGPS) mapAddr = parseGPSCoords(strGPSLoc);
     if (mapAddr == null) mapAddr = parseGPSCoords(strAddress);
     if (mapAddr != null) return mapAddr;
     
     // if there is no base address, return null
     mapAddr = getBaseMapAddress();
     if (mapAddr.length() == 0) return null;
 
     
     StringBuilder sb = new StringBuilder(mapAddr);
     
     // Add city if specified, default city otherwise
     String city = strCity;
     if (city.equals("OUT OF COUNTY")) city = "";
     else if (city.length() == 0) {
       city = (overrideCity != null ? overrideCity : defCity);
     } else if (overrideCity == null && (parser.getMapFlags() & MAP_FLG_ADD_DEFAULT_CNTY) != 0) {
       city = city.length() == 0 ? defCity : city + ',' + defCity; 
     }
 
     if (city.length() > 0 && !city.equalsIgnoreCase("NONE")) {
       sb.append(",");
       sb.append(city);
     } 
     
     // Add state if specified, default state otherwise
     String state = strState;
     if (state.length() == 0) {
       state = (overrideState != null ? overrideState : defState);
     }
     if (state.length() > 0 && !state.equalsIgnoreCase("NONE")) {
       sb.append(",");
       sb.append(state);
     }
     
     // Only add country for non-US addresses
     if (countryCode != MsgParser.CountryCode.US) {
       sb.append(",");
       sb.append(countryCode.toString());
     }
     
     return sb.toString();
   }
 
   /**
    * Look for GPS coordinates in address line.  If found, parse them into a
    * set of coordinates that Google Maps will recognize
    */
   private static String parseGPSCoords(String address) {
     Matcher match = GPSPattern.matcher(address);
     if (!match.find()) return null;
 
     double c1 = cvtGpsCoord(match.group(1));
     double c2 = cvtGpsCoord(match.group(2));
     
     // There isn't a consistent standard as to which is latitude and
     // which is longitude, so we will have to make some guesses.
     double latitude, longitude;
     if (c1 < 0) {
       latitude = c2;
       longitude = c1;
     } else if (c2 < 0) {
       latitude = c1;
       longitude = c2;
     } else if (c1 > c2) {
       latitude = c2;
       longitude = -c1;
     } else {
       latitude = c1;
       longitude = -c2;
     }
     return "" + latitude + "," + longitude;
   }
   
   public static final Pattern GPSPattern = 
     Pattern.compile("\\b([+-]?[0-9]+\\.[0-9]{4,}|[+-]?[0-9]+:[0-9]+:[0-9]+\\.[0-9]{4,})[,\\W]\\W*([+-]?[0-9]+\\.[0-9]{4,}|[+-]?[0-9]+:[0-9]+:[0-9]+\\.[0-9]{4,})\\b");
 
   /**
    * Convert GPS coordinate in degree:min:sec form to strait degrees
    */
   private static double cvtGpsCoord(String coord) {
     int pt1 = coord.indexOf(':');
     if (pt1 < 0) return Double.parseDouble(coord);
     int pt2 = coord.indexOf(':', pt1+1);
     String sDeg = coord.substring(0, pt1);
     char sgn = sDeg.charAt(0);
     boolean neg = (sgn == '-');
     if (neg || sgn == '+') sDeg = sDeg.substring(1);
     int deg = Integer.parseInt(sDeg);
     int min = Integer.parseInt(coord.substring(pt1+1,pt2));
     double sec = Double.parseDouble(coord.substring(pt2+1));
     double result = deg + min/60. + sec/3600.;
     if (neg) result = -result;
     return result;
   }
   
   /**
    * @return base mapping address, which is the adjusted address without
    * the additional city and state information
    */
   private static final Pattern UK_POST_CODE_PTN = Pattern.compile("^[A-Z]{1,2}\\d{1,2}[A-Z]? \\d[A-Z]{2} +");
   private static final Pattern DIR_OF_PTN = Pattern.compile(" [NSEW]O ");
   private static final Pattern CROSS_DELIM = Pattern.compile("[&/,@]| - ");
   public String getBaseMapAddress() {
     
     if (strAddress.length() == 0) return strAddress;
     
     if (strBaseMapAddress != null) return strBaseMapAddress;
     
     if (GPSPattern.matcher(strAddress).find()) {
       strBaseMapAddress = strAddress;
       return strBaseMapAddress;
     }
 
     // Perform any parser specific customizations
     String sAddr = strAddress;
     if (parser != null) sAddr = parser.adjustMapAddress(sAddr);
     
     // UK addresses have a postal code prefix that we don't want to mess with
     String prefix = "";
     if (countryCode == MsgParser.CountryCode.UK){
       Matcher match = UK_POST_CODE_PTN.matcher(sAddr);
       if (match.find()) {
         prefix = sAddr.substring(0,match.end());
         sAddr = sAddr.substring(match.end());
       }
     }
     
     if ((parser.getMapFlags() & MAP_FLG_SUPPR_DIRO) == 0) {
       sAddr = DIR_OF_PTN.matcher(sAddr).replaceAll(" & ");
     }
     sAddr = cleanStreetSuffix(sAddr);
     sAddr = cleanBlock(sAddr);
     sAddr = cleanBounds(sAddr);
     sAddr = cleanHouseNumbers(sAddr);
     sAddr = cleanRoutes(sAddr);
     sAddr = cleanDoubleRoutes(sAddr);
     sAddr = cleanInterstate(sAddr);
     sAddr = cleanOffRamp(sAddr);
     sAddr = cleanSTRoutes(sAddr);
     
     // Make sure & are surrounded by blanks
     sAddr = sAddr.replaceAll(" *& *", " & ");
     
     // If there wasn't an address number or intersection marker in address
     // try appending cross street info as as intersection
     if (validAddress(sAddr)) {
       sAddr = prefix + sAddr;
     } else {
       if (strCross.length() > 0) {
         String sCross = strCross;
         Matcher match = CROSS_DELIM.matcher(sCross);
         if (match.find()) sCross = sCross.substring(0,match.start()).trim();
         sCross = cleanStreetSuffix(sCross);
         sCross = cleanBounds(sCross);
         sCross = cleanRoutes(sCross);
         sCross = cleanDoubleRoutes(sCross);
         sCross = cleanInterstate(sCross);
         sCross = cleanOffRamp(sCross);
         sCross = cleanSTRoutes(sCross);
         sAddr = prefix + sAddr + " & " + sCross;
       }
     
       // If that didn't work, lets hope a place name will be enough
       else if (strPlace.length() > 0) {
         sAddr  = strPlace + "," + prefix + sAddr;
       }
       
       // else just append prefix
       else {
         sAddr = prefix + sAddr;
       }
     }
     
     strBaseMapAddress = sAddr.trim();
     return strBaseMapAddress;
 	}
   
   // Clean up any street suffix abbreviations that Google isn't happy with
   private static final Pattern CRNN_PTN = Pattern.compile("\\b(?:CR|CO|CORD)[- ]*(\\d+[A-Z]?)\\b");
   private static final Pattern AV_PTN = Pattern.compile("\\bAV\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern HW_PTN = Pattern.compile("\\bH[WY]\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern STH_PTN = Pattern.compile("\\bST?HY?\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern PK_PTN = Pattern.compile("\\bP[IK]\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern PW_PTN = Pattern.compile("\\bP[WY]\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern CI_PTN = Pattern.compile("\\bC[IR]\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern BLV_PTN = Pattern.compile("\\bBLV\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern TL_PTN = Pattern.compile("\\bTL\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern TRC_PTN = Pattern.compile("\\bTRC\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern GR_PTN = Pattern.compile("\\bGR\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern CRSN_PTN = Pattern.compile("\\bCRSN\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern CG_PTN = Pattern.compile("\\bCG\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern BP_PTN = Pattern.compile("\\bBP\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern LA_PTN = Pattern.compile("\\bLA\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern EXT_PTN = Pattern.compile(" EXT?\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern SR_PTN = Pattern.compile("\\bSR\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern TP_PTN = Pattern.compile("\\bTP\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern PA_PTN = Pattern.compile("\\bPA\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern NEAR_PTN = Pattern.compile("\\b(?:NEAR|OFF)\\b", Pattern.CASE_INSENSITIVE);
   private String cleanStreetSuffix(String sAddr) {
     
     // convert CR nn to COUNTY ROAD nn
     // we need to do this before we do the abbreviations converstions that will
     // change CR to CIR.
     sAddr = CRNN_PTN.matcher(sAddr).replaceAll("COUNTY ROAD $1");
     
     sAddr = replace(sAddr, AV_PTN, "AVE");
     sAddr = replace(sAddr, HW_PTN, "HWY");
     sAddr = replace(sAddr, STH_PTN, "ST");
     sAddr = replace(sAddr, PK_PTN, "PIKE");
     sAddr = replace(sAddr, PW_PTN, "PKWY");
     sAddr = replace(sAddr, CI_PTN, "CIR");
     sAddr = replace(sAddr, BLV_PTN, "BLVD");
     sAddr = replace(sAddr, TL_PTN, "TRL");
     sAddr = replace(sAddr, TRC_PTN, "TRCE");
     sAddr = replace(sAddr, GR_PTN, "GRADE");
     sAddr = replace(sAddr, CRSN_PTN, "CRESCENT");
     sAddr = replace(sAddr, CG_PTN, "CROSSING");
     sAddr = replace(sAddr, BP_PTN, "BYPASS");
     sAddr = replace(sAddr, SR_PTN, "ST");
     sAddr = replace(sAddr, TP_PTN, "TPK");
     sAddr = replace(sAddr, PA_PTN, "PATH");
     
     // Some alterations are suppressed by different parsers to meet local
     // requirements
     int mapFlags = parser.getMapFlags();
     if ((mapFlags & MAP_FLG_SUPPR_LA) == 0) sAddr = replace(sAddr, LA_PTN, "LN");
     if ((mapFlags & MAP_FLG_SUPPR_EXT) == 0) sAddr = EXT_PTN.matcher(sAddr).replaceAll("");
     
     if (!sAddr.contains("CUT OFF")) {
       sAddr = NEAR_PTN.matcher(sAddr).replaceAll("&");
     }
     return sAddr;
   }
   
   /**
    * Replace all occurrences of a given pattern in an address string with 
    * a replacement string.  The case of characters in the replacement string
    * will be adjusted to best match the case of the characters being replaced
    * @param address address line in which pattern sequence should be replaced
    * @param pat pattern to be searched for
    * @param replace string that should replace all occurrences of pattern
    * @return the adjusted address string
    */
   private String replace(String address, Pattern pat, String replace) {
     StringBuffer sb = null;
     Matcher match = pat.matcher(address);
     while (match.find()) {
       if (sb == null) sb = new StringBuffer();
       String token = match.group();
       match.appendReplacement(sb, "");
       for (int ndx = 0; ndx < replace.length(); ndx++) {
         Character chr = replace.charAt(ndx);
         if (isLower(token, ndx)) chr = Character.toLowerCase(chr);
         sb.append(chr);
       }
     } 
     if (sb == null) return address;
     match.appendTail(sb);
     return sb.toString();
   }
   
   /**
    * Determine if character at string position index is a lower case character.
    * if index is beyond end of string, check status of last character in string
    * @param token String token being checked
    * @param index index being checked
    * @return true if character at specified index is lower case
    */
   private boolean isLower(String token, int index) {
     if (index >= token.length()) index = token.length()-1;
     if (index < 0) return false;
     return Character.isLowerCase(token.charAt(index));
   }
 
   // Clean up any BLK indicators
   // Remove occurance of BLK bracketed by non-alpha characters
   private static final Pattern BLK_PAT = Pattern.compile("(?:-|(?<![A-Z]))BLK(?![A-Z])", Pattern.CASE_INSENSITIVE);
   private String cleanBlock(String sAddr) {
     sAddr = sAddr.replaceAll("[\\{\\}]", "");
     Matcher match = BLK_PAT.matcher(sAddr);
     sAddr = match.replaceFirst("");
     return sAddr;
   }
   
   // Clean up some Interstate conventions
   private static final Pattern INA_PATTERN = Pattern.compile("\\bIH?-?(\\d+)[NSEW]?\\b");
   private static final Pattern FRONTAGE_PTN = Pattern.compile("\\b(?:[NSEW]B)?FR\\b");
   private static final Pattern IH_PTN = Pattern.compile("\\bIH\\b");
   private String cleanInterstate(String sAddr) {
     sAddr = INA_PATTERN.matcher(sAddr).replaceAll("I $1");
     Matcher match = INA_PATTERN.matcher(sAddr);
     if (match.find()) {
       sAddr = sAddr.substring(0,match.start(1)) + sAddr.substring(match.end());
     }
     sAddr = FRONTAGE_PTN.matcher(sAddr).replaceAll("FRONTAGE RD");
     sAddr = IH_PTN.matcher(sAddr).replaceAll("");
     return sAddr;
   }
   
   // Clean up and NB, SB, EB, or WB words
 
   private static final Pattern DIRBOUND_PAT = Pattern.compile("\\s*\\b(?:NB|SB|EB|WB|RA)\\b", Pattern.CASE_INSENSITIVE);
   private String cleanBounds(String sAddr) {
     Matcher match = DIRBOUND_PAT.matcher(sAddr);
     return match.replaceAll("").trim();
   }
 
   // Google map isn't found of house numbers mixed with intersections
   // If we find an intersection marker, remove any house numbers
   private static final Pattern HOUSE_RANGE = Pattern.compile("\\b(\\d+)-[A-Z0-9/\\.]+\\b");
   private static final Pattern HOUSE_NUMBER = Pattern.compile("^ *\\d+ +(?![NSEW]{0,2} *[&/]|(?:AV|AVE|ST|MILE)\\b)", Pattern.CASE_INSENSITIVE);
   private String cleanHouseNumbers(String sAddress) {
     
     // Start by eliminating any house ranges
     sAddress = HOUSE_RANGE.matcher(sAddress).replaceAll("$1");
 
     // If this has an house number and an intersecting street.  Drop the intersecting street
     sAddress = sAddress.replace(" and ", " & ");
     int ipt = sAddress.indexOf('&');
     if (ipt >= 0) {
       Matcher match = HOUSE_NUMBER.matcher(sAddress);
       if (match.find()) sAddress = sAddress.substring(0,ipt).trim();
     }
     
     // Check for a trailing apt number
     // Generally google ignores appt numbers, but it chokes on one that
     // starts with a #
     ipt = sAddress.lastIndexOf(' ');
    if (ipt >= 0 && ipt+1 < sAddress.length() && sAddress.charAt(ipt+1) == '#') {
       sAddress = sAddress.substring(0, ipt).trim();
     }
     return sAddress;
   }
   
   // Google doesn't always handle single word route names like US30 or HWY10.
   // This method breaks those up into two separate tokens, also dropping any
   // direction qualifiers
   private static final Pattern ROUTE_PTN =
     Pattern.compile("\\b(RT|RTE|HW|HWY|US|ST|I|CO|CR|CORD|SRT?|I)-?(\\d{1,3})(?:[NSEW]B?)?\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern ROUTE_PTN2 =
     Pattern.compile("\\b([A-Z]{2})(\\d{1,3})(?:[NSEW]B)?\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern SRT_PTN = Pattern.compile("\\bSRT?\\b", Pattern.CASE_INSENSITIVE);
   
   private String cleanRoutes(String sAddress) {
     
     // Google gets confused by the ST abbreviation for State hwy.  We have a couple choices but lets use
     // the default state if there is one and STATE if there isn't.
     String state = (strState.length() > 0 ? strState : 
                     countryCode == CountryCode.US && defState.length() > 0 ? defState : "STATE");
 
     Matcher match = ROUTE_PTN.matcher(sAddress);
     sAddress = match.replaceAll("$1 $2");
     
     match = ROUTE_PTN2.matcher(sAddress);
     if (match.find()) {
       StringBuffer sb = new StringBuffer();
       do {
         String replace = (state.equalsIgnoreCase(match.group(1)) ? "$1 $2" : "$0");
         match.appendReplacement(sb, replace);
       } while (match.find());
       match.appendTail(sb);
       sAddress = sb.toString();
     }
     sAddress = SRT_PTN.matcher(sAddress).replaceAll(state);
     return sAddress;
   }
 
   // Google can handle things like ST 666 or US 666 or RTE 666.
   // But it doesn't like US RTE 666
   // If we find a construct like that, remove the middle section
   private static final Pattern[] DBL_ROUTE_PTNS = new Pattern[]{ 
     Pattern.compile("\\b([A-Z]{2}|STATE) *(?:ROAD|RD|RT|RTE|ROUTE|HW|HWY|HY) +(\\d+[NSEW]?|[A-Z]{1,2})\\b", Pattern.CASE_INSENSITIVE),
     Pattern.compile("\\b([A-Z]{2}|STATE) +(\\d+|[A-Z]{1,2})\\b *(?:ROAD|RD|RT|RTE|ROUTE|HW|HWY|HY)\\b", Pattern.CASE_INSENSITIVE)
   };
   private static final Pattern I_FWY_PTN = Pattern.compile("\\b(I[- ]\\d+) +[FH]WY\\b", Pattern.CASE_INSENSITIVE);
   private String cleanDoubleRoutes(String sAddress) {
     for (Pattern ptn : DBL_ROUTE_PTNS) {
       Matcher match = ptn.matcher(sAddress);
       int pt = 0;
       int lastPt = 0;
       if (!match.find(pt)) continue;
       
       StringBuilder sb = new StringBuilder();
       do {
         String prefix = match.group(1);
         String suffix = match.group(2);
         String state = strState;
         if (state.length() == 0) state = defState;
         if (prefix.length() != 2 ||
             (prefix.equalsIgnoreCase(state) ||
              prefix.equalsIgnoreCase("CO") ||
              prefix.equalsIgnoreCase("US") ||
              prefix.equalsIgnoreCase("ST") ||
              prefix.equalsIgnoreCase("FM"))) {
           sb.append(sAddress.substring(lastPt, match.start()));
           sb.append(prefix);
           sb.append(' ');
           sb.append(suffix);
           lastPt = match.end();
         }
         pt = match.end();
       } while (match.find(pt));
       sb.append(sAddress.substring(lastPt));
       sAddress = sb.toString();
     }
     
     // Google also doesn't like I-20 fwy contructs
     sAddress = I_FWY_PTN.matcher(sAddress).replaceAll("$1");
     return sAddress;
   }
   
   private static final Pattern OFFRAMP_PTN = Pattern.compile(" OFRP ", Pattern.CASE_INSENSITIVE);
   private static String cleanOffRamp(String sAddress) {
     Matcher match = OFFRAMP_PTN.matcher(sAddress);
     if (match.find()) sAddress = sAddress.substring(0,match.start()).trim();
     return sAddress;
   }
   
   private static final Pattern ST_PTN = Pattern.compile("(?<=^|& ?|\\d ?(?:[NSEW] ?)?)ST(?= +\\d+)\\b", Pattern.CASE_INSENSITIVE);
   private String cleanSTRoutes(String sAddress) {
     
     // Google gets confused by the ST abbreviation for State hwy.  We have a couple choices but lets use
     // the default state if there is one and STATE if there isn't.
     String state = (countryCode == CountryCode.US && defState.length() > 0 ? defState : "STATE");
     return ST_PTN.matcher(sAddress).replaceAll(state);
   }
 
   
   /**
    * @return true if address is valid standalone address
    * (ie either starts with a house number or contains an intersection marker) 
    */
   private static final Pattern HOUSE_NO_PTN = Pattern.compile("^(?:[NSEW] +)?\\d+[A-Z]? ", Pattern.CASE_INSENSITIVE);
   private boolean validAddress(String sAddr) {
     if (HOUSE_NO_PTN.matcher(sAddr).find()) return true;;
     if (sAddr.contains("&") || sAddr.contains(" AND ") || sAddr.contains(" and ")) return true; 
     return false;
   }
   
   /**
    * @return the call city
    */
   public String getCity() {
     return strCity;
   }
 
   /**
    * @return the call State
    */
   public String getState() {
     return strState;
   }
 
   /**
    * @return the call appt number
    */
   public String getApt() {
     return strApt;
   }
 
   /**
    * @return the call Cross street description
    */
   public String getCross() {
     return strCross;
   }
 
   /**
    * @return the call box
    */
   public String getBox() {
     return strBox;
   }
 
   /**
    * @return the call unit
    */
   public String getUnit() {
     return strUnit;
   }
   
   /**
    * @return map code
    */
   public String getMap() {
     return strMap;
   }
   
   /**
    * @return the callback phone number
    */
   public String getPhone() {
     return strPhone;
   }
   /**
    * @return the Supp Info 
    */
   public String getSupp() {
     return strSupp;
   }
   
   /***
    * @return call type code
    */
   public String getCode() {
     return strCode;
   }
   
   /**
    * @return the page source 
    */
   public String getSource() {
     return strSource;
   }
   
   /**
    * @return name
    */
   public String getName() {
     return strName;
   }
   
   /**
    * @return call priority
    */
   public String getPriority() {
     return strPriority;
   }
   
   /**
    * @return radio channel
    */
   public String getChannel() {
     return strChannel;
   }
   
   /**
    * @return GPS Location
    */
   public String getGPSLoc() {
     return strGPSLoc;
   }
   
   /**
    * @return incident date
    */
   public String getDate() {
     return strDate;
   }
   
   /**
    * @return incident time
    */
   public String getTime() {
     return strTime;
   }
   
   /**
    * @return information URL field
    */
   public String getInfoURL() {
     return strInfoURL;
   }
   
   /**
    * @return the default city 
    */
   public String getDefCity() {
     return defCity;
   }
   
   /**
    * @return the default state 
    */
   public String getDefState() {
     return defState;
   }
   
   /**
    * @return true if additional messages are expected
    */
   public boolean isExpectMore() {
     return expectMore;
   }
   
   public MsgParser getParser() {
     return parser;
   }
   
 
   /**
    * Append message information to support message under construction
    * @param sb String builder holding message being constructed
    */
   public void addMessageInfo(StringBuilder sb) {
     sb.append("\n\nParser Info");
     
     addInfo(sb, "Call", strCall);
     addInfo(sb, "Place", strPlace);
     addInfo(sb, "Addr", strAddress);
     addInfo(sb, "City", strCity);
     addInfo(sb, "Apt", strApt);
     addInfo(sb, "X", strCross);
     addInfo(sb, "Box", strBox);
     addInfo(sb, "Unit", strUnit);
     addInfo(sb, "St", strState);
     addInfo(sb, "Map", strMap);
     addInfo(sb, "ID", strCallId);
     addInfo(sb, "Ph", strPhone);
     addInfo(sb, "Info", strSupp);
     addInfo(sb, "Code", strCode);
     addInfo(sb, "Src", strSource);
     addInfo(sb, "Name", "strName");
     addInfo(sb, "Pri",  strPriority);
     addInfo(sb, "Ch", strChannel);
     addInfo(sb, "GPS", strGPSLoc);
     addInfo(sb, "Date", strDate);
     addInfo(sb, "Time", strTime);
   }
   
   private void addInfo(StringBuilder sb, String title, String value) {
     if (value.length() > 0) {
       sb.append('\n');
       sb.append(title);
       sb.append(':');
       sb.append(value);
     }
   }
 }
