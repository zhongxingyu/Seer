 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.ManagePreferences;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 public class GeneralParser extends SmartAddressParser {
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Accept anything, but only if there is a valid sender filter
     if (ManagePreferences.filter().length() <= 1) return false;
 
     // Lets see what the smart parser can make of this
     body = body.replaceAll("\n", " ");
     parseAddress(StartType.START_CALL, body, data);
     
     return true;
   }
 }
