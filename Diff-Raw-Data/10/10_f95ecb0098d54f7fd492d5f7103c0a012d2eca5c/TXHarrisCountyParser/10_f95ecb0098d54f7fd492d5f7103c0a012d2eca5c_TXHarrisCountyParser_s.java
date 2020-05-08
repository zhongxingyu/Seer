 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 
import net.anei.cadpage.Log;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /* Sample Harris Page
11:58 W HILLSIDE DR/EASTEX FRWY, ; Map:414D Sub: Nat:MA-MUTUAL AID / ASSIST AGENCY Units:E91 T81 T73 E-E39 X-St:EASTEX
 1of2:09/06 11:56 W HILLSIDE DR/EASTEX FRWY, ; Map:414D Sub: Nat:MA-MUTUAL AID / ASSIST AGENCY Units:E91 T81 T73 E-L19 X-St:EASTEX
 1of2:09/06 11:28 19707 WOOD WALK LN, ; Map:337U Sub:PINEHURST OF ATASCOCITA Nat:09E01-ARREST - NOT BREATHING Units:E-M19 E-M29 E-7900
 1of2:09/05 08:56 19226 AQUATIC DR, ; Map:378A Sub:WALDEN ON LAKE HOUSTON Nat:52B01G-FIRE ALARM - RESIDENTIAL Units:ATFD E-E39 X-
 1of2:09/04 19:45 17219 KOBUK VALLEY CIR, ; Map:377E Sub:EAGLE SPRINGS Nat:67B03U-OUTSIDE FIRE - INVESTIGA Units:E-E39 X-St:*** Dead
 1of2:09/03 08:14 LILES LN/WOODLAND HILLS DR, ; Map:376H Sub:ATASCOCITA FOREST Nat:29-MOTOR VEHICLE INCIDENT Units:E-M19 E-E39 X-
 */
 
 public class TXHarrisCountyParser extends SmsMsgParser {
 
   @Override
   public boolean isPageMsg(String body) {
     return body.contains("Map:");
   }
 
   @Override
   protected void parse(String body, Data data) {
   
    Log.v("DecodeHarrisPage: Message Body of:" + body);
     data.defState="TX";
     data.defCity="HARRIS COUNTY";
     
     // Strip first 16 characters off of message
     if (body.length() <= 16) return;
     body = "Loc:" + body.substring(16);
     
     Properties props = parseMessage(body, new String[]{"Map", "Sub", "Nat", "Units", "X-St"});
     parseAddress(props.getProperty("Loc", ""), data);
     data.strMap = props.getProperty("Map", "");
     data.strCall = props.getProperty("Nat", "");
     data.strUnit = props.getProperty("Units", "");
    data.strCross = props.getProperty("X-st", "");
     
     // Strip extra stuff off of address line
     int ipt = data.strAddress.indexOf(',');
     if (ipt > 0) data.strAddress = data.strAddress.substring(0, ipt).trim();
     if (data.strCall.contains("MA-MUTUAL AID")) data.strCity = "Humble";
   }
 }
