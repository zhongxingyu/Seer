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
   private String defCity;
   private String defState;
   private String strSupp;
 
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
     public String defCity = "";
     public String defState="";
     public String strSupp="";
     
 
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
     defCity = info.defCity;
     defState = info.defState;
     strSupp = info.strSupp;
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
 
   /**
    * @return return mapping address
    */
   public String getMapAddress() {
     StringBuilder sb = new StringBuilder(cleanHouseNumbers(strAddress));
     
     // If there wasn't an address number or intersection marker in address
     // try appending cross street info as as intersection
     if (!validAddress() && strCross.length() > 0) {
       sb.append(" & ");
       sb.append(strCross);
     }
     
     // Add city if specified, default city otherwise
     String city = strCity;
     if (city.length() == 0) city = defCity;
     if (city.length() > 0) {
       sb.append(",");
       sb.append(city);
     } 
     
     // Add state if specified, default state otherwise
     String state = strState;
     if (state.length() == 0) state = defState;
     if (state.length() > 0) {
       sb.append(",");
       sb.append(state);
     }
     
     return sb.toString();
 	}
   
   // Google map isn't found of house numbers mixed with intersections
   // If we find an intersection marker, remove any house numbers
  private static final Pattern HOUSE_NUMBER = Pattern.compile("^ *\\d+ *");
   private String cleanHouseNumbers(String sAddress) {
     sAddress = sAddress.replace(" and ", " & ");
     int ipt = sAddress.indexOf('&');
     if (ipt < 0) return sAddress;
     
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
        sAddress = sAddress.substring(0, match.start()) + sAddress.substring(match.end());
       }
     }
     return sAddress;
   }
   
   /**
    * @return true if address is valid standalone address
    * (ie either starts with a house number or contains an intersection marker) 
    */
   private boolean validAddress() {
     int pt = strAddress.indexOf(' ');
     if (pt > 0 && SmsMsgParser.NUMERIC.matcher(strAddress.substring(0, pt)).matches()) return true;
     if (strAddress.contains("&") || strAddress.contains(" AND ") || strAddress.contains(" and ")) return true; 
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
