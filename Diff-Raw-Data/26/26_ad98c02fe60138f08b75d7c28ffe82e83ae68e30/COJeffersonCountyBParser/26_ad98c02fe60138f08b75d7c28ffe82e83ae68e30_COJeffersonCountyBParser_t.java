 package net.anei.cadpage.parsers.CO;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /**
  * Jefferson County, CO
  */
 public class COJeffersonCountyBParser extends SmartAddressParser {
   
   private static Pattern DATE_TIME_PTN = Pattern.compile("(?:[A-Z][a-z]{2} ){2}\\d\\d \\d\\d:\\d\\d:\\d\\d \\d{4}");
   private static DateFormat DATE_TIME_FMT = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
   
 
   public COJeffersonCountyBParser() {
     super("Jefferson County", "CO");
   }
 
   @Override
   public String getFilter() {
     return "@co.jefferson.co.us";
   }
   
   @Override 
   public boolean parseMsg(String subject, String body, Data data) {
    if (!subject.equals("Jeffco Alert") && !subject.equals("Jeffco Sheriff Message")) return false;
     int pt = body.lastIndexOf('\n');
     if (pt < 0) return false;
     String dateTime = body.substring(pt+1).trim();
     body = body.substring(0,pt).trim();
     if (!DATE_TIME_PTN.matcher(dateTime).matches()) return false;
     setDateTime(DATE_TIME_FMT, dateTime, data);
     
     String[] flds = body.split("\n");
     if (flds.length > 1) {
       String lastFld = null;
       for (String fld : flds) {
         if (data.strAddress.length() == 0) {
           if (checkAddress(fld) > 0) {
             parseAddress(fld, data);
             if (lastFld != null) data.strCall = lastFld;
           } else {
             lastFld = fld;
           }
         } else if (data.strCall.length() == 0) {
           data.strCall = fld;
         } else {
           data.strSupp = append(data.strSupp, " / ", fld);
         }
       }
       if (data.strAddress.length() > 0) return true;
       body = body.replace('\n', ' ');
     }
     
     body = body.replace(',', ' ').replaceAll("  +", " ");
     parseAddress(StartType.START_CALL, FLAG_NO_IMPLIED_APT, body, data);
     if (data.strAddress.length() == 0) {
       data.strPlace = data.strCall;
       data.strCall = "GENERAL ALERT";
     } else if (data.strCall.length() == 0) {
       data.strCall = getLeft();
     } else {
       data.strSupp = getLeft();
     }
     
     return true;
   }
 }
   
 
 
 
 
 
