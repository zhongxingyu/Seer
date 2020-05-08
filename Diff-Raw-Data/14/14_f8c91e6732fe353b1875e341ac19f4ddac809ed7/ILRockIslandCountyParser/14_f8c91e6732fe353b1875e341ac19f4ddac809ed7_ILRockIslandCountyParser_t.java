 package net.anei.cadpage.parsers.IL;
 
import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.MsgParser;
 
 /*
 Rock Island County, IL
 Contact: Darin Keith <darin.keith@hotmail.com>
 Sender: 40404,CAD@ricoetsb.org
 System: Sunguard OSSI
 
 @RIFD: 15221:;CRASH PERSONAL INJURIES;24TH ST/18TH AV
 @RIFD: 15226:;FIRE OTHER;1323 14TH ST
 @RIFD: 15227:;FIRE ALARM;3605 11TH AV
 @RIFD: 16193:;MEDICAL CALL;2300-46 79TH AV W
 S: M:67:FYI: ;STRUCTURE FIRE/GENERAL;2932 RODMAN AV\n
 S: M:68:CANCEL;2932 RODMAN AV; RIA\n
 S: M:69:Update: ;FIRE MUTUAL AID;2932 RODMAN AV\n
@RIFD: 17437:;MEDICAL CALL;767 30TH ST;RI
 
 */
 
 public class ILRockIslandCountyParser extends MsgParser {
   
   private static final Pattern MASTER = 
      Pattern.compile("(?:@([A-Z]+): )?(\\d+):([\\w: ]*);([^;]+?);([^;]+?)(?:;([A-Z]+))?");
   
   public ILRockIslandCountyParser() {
     super("ROCK ISLAND COUNTY", "IL");
   }
   
   @Override
   public String getFilter() {
     return "cad@ricoetsb.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strSource = getOptGroup(match.group(1));
     data.strCallId = match.group(2);
     String type = match.group(3).trim();
     String sAddr;
     if (type.equals("CANCEL")) {
       data.strCall = type + " - " + match.group(5).trim();
       sAddr = match.group(4).trim();
     } else {
       data.strCall = match.group(4).trim();
       sAddr = match.group(5).trim();
     }
    String city = match.group(6);
    if (city != null) data.strCity = convertCodes(city, CITY_CODES);
       
     parseAddress(sAddr, data);
     return true;
   }
   
  private static final Properties CITY_CODES = buildCodeTable(new String[]{
      "RI", "ROCK ISLAND"
  });
  
 }
