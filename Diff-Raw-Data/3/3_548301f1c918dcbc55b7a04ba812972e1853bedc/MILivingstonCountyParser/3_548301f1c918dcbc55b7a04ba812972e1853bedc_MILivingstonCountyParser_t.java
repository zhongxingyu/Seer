 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.Log;
 import net.anei.cadpage.SmsMsgInfo.Data;
 /*
 CAD:FYI: ;ALARMF;11555 PLEASANT VIEW DR;INDIAN TRL;SMOKE ALARMS GOING OFF IN THE HOUSE FOR THE LAST 30 MIN ON AND OFF/ NO SMOKE OR SMOKE SHOWING [09/24/10 04:17:07 ECOOK]
 CAD:FYI: ;UNKACC;E M36/PETTYS RD;VEHICLE HIT A POLE/ MALE RUNNING E/B FROM SCENE [09/23/10 21:45:06 ECOOK]
 CAD:FYI: ;CHSTPN;8422 PAWNEE TRL;SHOSHONI PASS;[Medical Priority Info] RESPONSE: P1 STA 1 4 5 6 7 8 FC FT RESPONDER SCRIPT: 72 year old, Female, Conscious, Breathing. Chest Pain (Non-Traumatic). Abnormal breathing. Cal
 CAD:FYI: ;UNCON;8732 RIVER VALLEY RD;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 63 year old, Female, Conscious, Breathing. Unconscious / Fainting (Near). Not alert. Caller Statement: UNCON.
 */
 
 public class MILivingstonCountyParser extends SmsMsgParser {
 
   @Override
   public boolean isPageMsg(String body) {
     return body.startsWith("CAD:");
   }
 
   @Override
   protected void parse(String body, Data data) {
     Log.v("DecodeLivingstonPage: Message Body of:" + body);
     data.defState="MI";
     data.defCity = "LIVINGSTON COUNTY";
     body = body.trim();
     String[] AData = body.split(";");
     data.strCall = AData[1];
    if (AData.length <= 2) return;
     parseAddress(AData[2], data);
    if (AData.length <= 3) return;
     if (AData[3].length() <20) data.strCross =  AData[3];
   }
 }
