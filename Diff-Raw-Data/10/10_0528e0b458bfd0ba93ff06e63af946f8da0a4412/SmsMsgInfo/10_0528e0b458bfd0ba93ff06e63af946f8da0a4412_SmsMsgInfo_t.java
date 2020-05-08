 package net.anei.cadpage;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /**
  * This class contains all of the useful data fields that are parsed from
  * the actual message text
  */
 public class SmsMsgInfo {
 
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
   private String defCity;
   private String defState;
   
   // Flag set when parser determines that message is incomplete
   // and another part should be expected
   private boolean expectMore;
   
   // Cached map address
   private String strMapAddress = null;
   
 
   /**
    * Temporary data class used to pass information to constructor
    * @author ken
    *
    */
   public static class Data {
     public String strCall = "";
     public String strPlace = "";
     public String strAddress = "";
     public String strCity = "";
     public String strApt = "";
     public String strCross = "";
     public String strBox= "" ;
     public String strUnit= "" ;
     public String strState="";
     public String strMap = "";
     public String strCallId = "";
     public String strPhone="";
     public String strSupp="";
     public String strCode="";
     public String strSource = "";
     public String strName = "";
     public String strPriority = "";
     public String strChannel = "";
     public String strGPSLoc = "";
     public String defCity = "";
     public String defState="";
     
     public boolean expectMore = false;
     
 
     /**
      * @return relative score that can be used to pick out the better result
      * from multiple possible parsings
      */
     public int score() {
       int result = 0;
       if (strAddress.length() > 0) result += 10000;
       if (strCall.length() > 0) result += 1000;
       if (strCity.length() > 0) result += 100;
       if (strApt.length() > 0) result += 10;
       if (strCross.length() > 0) result += 100;
       if (strBox.length() > 0) result += 10;
       if (strUnit.length() > 0) result += 10;
       if (strState.length() > 0) result += 10;
       if (strMap.length() > 0) result += 10;
       if (strPlace.length() > 0) result += 10;
       if (strCallId.length() > 0) result += 10;
       if (strPhone.length() > 0) result += 1;
       if (strSupp.length() > 0) result += 1;
       if (strCode.length() > 0) result += 1;
       return result;
     }
   }
 
   /**
    * constructor
    * @param info data object containing all message information
    */
   public SmsMsgInfo(Data info) {
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
     defCity = info.defCity;
     defState = info.defState;
     expectMore = info.expectMore;
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
    * @return return mapping address
    */
   private static final Pattern DIR_OF_PTN = Pattern.compile(" [NSEW]O ");
   public String getMapAddress() {
     
     if (strMapAddress != null) return strMapAddress;
     
     String sAddr = strAddress;
     sAddr = DIR_OF_PTN.matcher(sAddr).replaceAll(" & ");
     sAddr = cleanStreetSuffix(sAddr);
     sAddr = cleanBlock(sAddr);
     sAddr = cleanBounds(sAddr);
     sAddr = cleanHouseNumbers(sAddr);
     sAddr = cleanRoutes(sAddr);
     sAddr = cleanDoubleRoutes(sAddr);
     
     // Make sure & are surrounded by blanks
     sAddr = sAddr.replaceAll(" *& *", " & ");
     
     StringBuilder sb = new StringBuilder(sAddr);
     
     // If there wasn't an address number or intersection marker in address
     // try appending cross street info as as intersection
     if (!validAddress(sAddr)) {
       if (strCross.length() > 0) {
         String sCross = strCross;
         int pt = sCross.indexOf('/');
         if (pt < 0) pt = sCross.indexOf('&');
         if (pt >= 0) sCross = sCross.substring(0, pt).trim();
         sb.append(" & ");
         sb.append(sCross);
       }
     
       // If that didn't work, lets hope a place name will be enough
       else if (strPlace.length() > 0) {
         sb.insert(0, ',');
         sb.insert(0, strPlace);
       }
     }
     
     // Add city if specified, default city otherwise
     String city = strCity;
     if (city.equals("OUT OF COUNTY")) city = "";
     else if (city.length() == 0) {
       city = getDefCity();
     }
     if (city.length() > 0 && !city.equalsIgnoreCase("NONE")) {
       sb.append(",");
       sb.append(city);
     } 
     
     // Add state if specified, default state otherwise
     String state = strState;
     if (state.length() == 0) {
       state = getDefState();
     }
     if (state.length() > 0 && !state.equalsIgnoreCase("NONE")) {
       sb.append(",");
       sb.append(state);
     }
     
     strMapAddress = sb.toString();
     return strMapAddress;
 	}
   
   // Clean up any street suffix abbreviations that Google isn't happy with
   private static final Pattern AV_PTN = Pattern.compile("\\bAV\\b");
   private static final Pattern HW_PTN = Pattern.compile("\\bH[WY]\\b");
   private static final Pattern STH_PTN = Pattern.compile("\\bSTH\\b");
   private static final Pattern PK_PTN = Pattern.compile("\\bPK\\b");
   private static final Pattern PW_PTN = Pattern.compile("\\bPW\\b");
   private static final Pattern CI_PTN = Pattern.compile("\\bCI\\b");
   private String cleanStreetSuffix(String sAddr) {
     sAddr = AV_PTN.matcher(sAddr).replaceAll("AVE");
     sAddr = HW_PTN.matcher(sAddr).replaceAll("HWY");
     sAddr = STH_PTN.matcher(sAddr).replaceAll("ST");
     sAddr = PK_PTN.matcher(sAddr).replaceAll("PIKE");
     sAddr = PW_PTN.matcher(sAddr).replaceAll("PKWY");
     sAddr = CI_PTN.matcher(sAddr).replaceAll("CIR");
     return sAddr;
   }
 
   // Clean up any BLK indicators
   // Remove occurance of BLK bracketed by non-alpha characters
   private static final Pattern BLK_PAT = Pattern.compile("(?:-|(?<![A-Z]))BLK(?![A-Z])");
   private String cleanBlock(String sAddr) {
     sAddr = sAddr.replaceAll("[\\{\\}]", "");
     Matcher match = BLK_PAT.matcher(sAddr);
     sAddr = match.replaceFirst("");
     return sAddr;
   }
   
   // Clean up and NB, SB, EB, or WB words
 
   private static final Pattern DIRBOUND_PAT = Pattern.compile("\\s(N|S|E|W)B\\b");
   private String cleanBounds(String sAddr) {
     Matcher match = DIRBOUND_PAT.matcher(sAddr);
     return match.replaceAll("").trim();
   }
 
   // Google map isn't found of house numbers mixed with intersections
   // If we find an intersection marker, remove any house numbers
   private static final Pattern HOUSE_RANGE = Pattern.compile("\\b(\\d+)-\\d+\\b");
   private static final Pattern HOUSE_NUMBER = Pattern.compile("^ *\\d+ +");
   private static final Pattern STREET_SFX = Pattern.compile("^ *(AV|AVE|ST)\\b");
   private String cleanHouseNumbers(String sAddress) {
     
     // Start by eliminating any house ranges
     sAddress = HOUSE_RANGE.matcher(sAddress).replaceAll("$1");
     
     sAddress = sAddress.replace(" and ", " & ");
     int ipt = sAddress.indexOf('&');
     if (ipt >= 0) {
       
       Matcher match = HOUSE_NUMBER.matcher(sAddress);
       for (int part = 2; part >= 1; part--) {
         switch (part) {
         case 1:
           match.region(0, ipt);
           break;
           
         case 2:
           match.region(ipt+1, sAddress.length());
         }
         
         if (match.find()) {
           // Safety check, don't mess with number if followed by AV, AVE, or ST
           int st = match.start();
           int end = match.end();
           Matcher match2 = STREET_SFX.matcher(sAddress);
           match2.region(end, match.regionEnd());
           if (! match2.find()) {
             sAddress = sAddress.substring(0, st) + sAddress.substring(end);
           }
         }
       }
     }
     
     // If it isn't an intersection, check for a appt number
     // Generally google ignores appt numbers, but it chokes on one that
     // starts with a #
     else {
       ipt = sAddress.lastIndexOf(' ');
       if (ipt+1 < sAddress.length() && sAddress.charAt(ipt+1) == '#') {
         sAddress = sAddress.substring(0, ipt).trim();
       }
     }
     return sAddress;
   }
   
   // Google doesn't always handle single word route names like US30 or HWY10.
   // This method breaks those up into two separate tokens, also dropping any
   // direction qualifiers
   private static final Pattern ROUTE_PTN =
    Pattern.compile("\\b(RT|RTE|HW|HWY|US|ST|I|CO|CR|SRT)(\\d{1,3})(?:[NSEW]B)?\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern ROUTE_PTN2 =
     Pattern.compile("\\b([A-Z]{2})(\\d{1,3})(?:[NSEW]B)?\\b", Pattern.CASE_INSENSITIVE);
   
   private String cleanRoutes(String sAddress) {
     Matcher match = ROUTE_PTN.matcher(sAddress);
     sAddress = match.replaceAll("$1 $2");
     
     match = ROUTE_PTN2.matcher(sAddress);
     if (match.find()) {
       StringBuffer sb = new StringBuffer();
       do {
         String replace = (defState.equalsIgnoreCase(match.group(1)) ? "$1 $2" : "$0");
         match.appendReplacement(sb, replace);
       } while (match.find());
       match.appendTail(sb);
       sAddress = sb.toString();
     }
     return sAddress;
   }
 
   // Google can handle things like ST 666 or US 666 or RTE 666.
   // But it doesn't like US RTE 666
   // If we find a construct like that, remove the middle section
   private static final Pattern DBL_ROUTE_PTN = 
     Pattern.compile("\\b([A-Z]{2}|STATE) *(RT|RTE|ROUTE|HW|HWY|HY) +(\\d+)\\b", Pattern.CASE_INSENSITIVE);
   private String cleanDoubleRoutes(String sAddress) {
     Matcher match = DBL_ROUTE_PTN.matcher(sAddress);
     int pt = 0;
     int lastPt = 0;
     if (!match.find(pt)) return sAddress;
     
     StringBuilder sb = new StringBuilder();
     do {
       String prefix = match.group(1);
       String suffix = match.group(3);
       String state = strState;
       if (strState.length() == 0) state = defState;
       if (prefix.length() != 2 ||
           (prefix.equalsIgnoreCase(state) ||
            prefix.equalsIgnoreCase("CO") ||
            prefix.equalsIgnoreCase("US") ||
            prefix.equalsIgnoreCase("ST"))) {
         sb.append(sAddress.substring(lastPt, match.start()));
         sb.append(prefix);
         sb.append(' ');
         sb.append(suffix);
         lastPt = match.end();
       }
       pt = match.end();
     } while (match.find(pt));
     sb.append(sAddress.substring(lastPt));
     return sb.toString();
   }
 
   
   /**
    * @return true if address is valid standalone address
    * (ie either starts with a house number or contains an intersection marker) 
    */
   private boolean validAddress(String sAddr) {
     int pt = sAddr.indexOf(' ');
     if (pt > 0 && SmsMsgParser.NUMERIC.matcher(sAddr.substring(0, pt)).matches()) return true;
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
    * @return the default city 
    */
   public String getDefCity() {
     boolean override = ManagePreferences.overrideDefaults();
     return (override ? ManagePreferences.defaultCity() :  defCity);
   }
   
   /**
    * @return the default state 
    */
   public String getDefState() {
     boolean override = ManagePreferences.overrideDefaults();
     return (override ? ManagePreferences.defaultState() : defState);
   }
   
   /**
    * @return true if additional messages are expected
    */
   public boolean isExpectMore() {
     return expectMore;
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
