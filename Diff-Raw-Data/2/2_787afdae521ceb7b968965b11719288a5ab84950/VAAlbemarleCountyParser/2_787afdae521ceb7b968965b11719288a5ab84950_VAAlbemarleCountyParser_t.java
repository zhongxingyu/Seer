 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*
 Albemarle County, VA
 Contact: Joe Orsolini <muzz3256@gmail.com>
 Works for 911 center
 Sender: CAD@acuecc.org
 System: Premier CAD by Motorola
 
 EARLYSVILL EXTRICATION AD: SEMINOLE TRL&FRAYS MILL RD CTY: AC LOC: SB JUST S OF THE STOPLIGHT CALLER WAS GOING NB AND SAW A VEHICLE ROLL OVER SE
 EARLYSVILL EMS CALL AD: 1700 FOXTAIL PNES CTY: AC 93YOM, DIFF BREATHING, JUST DISCHARGED FROM HEALTHSOUTH, AWAKE/TALKING XST2: 2497 ASPENWOOD RD
 EARLYSVILL RES STRUCTURE FIRE AD: 4235 WOODTHRUSH LN CTY: AC SMOKE AND FIRE IN THE HOUSE XST: 5001 MEADOWLARK CT
 EARLYSVILL MVA AD: SEMINOLE TRL&LEWIS AND CLARK D CTY: AC LOC: NB LANE 5 OR 6 CARS INVOLVED, UNKNWON INJURIES
 EARLYSVILL ALARM ACTIVATION AD: 4924 FREE UNION RD CTY: AC LOC: RICHARD BOOTH RES GUEST HOUSE FIRE ALARM-# IS 973-1805 XST: 2001 TASMANIA DR XST
 EARLYSVILL CHIMNEY FIRE APT:11B AD:702 WOODBURN CT CTY:AC SPARKS COMING FROM THE CHIMNEY-FIRE STILL IN FIREPLACE-NO SMOKE IN APT XST:2036 WO
  */
 
 
 public class VAAlbemarleCountyParser extends SmsMsgParser {
   
   private static final String[] keywords = new String[]{"CALL", "APT", "AD", "CTY", "LOC", "XST", "XST2"};
   
   public VAAlbemarleCountyParser() {
     super("ALBEMARLE COUNTY", "VA");
   }
   
   @Override
   public String getFilter() {
     return "CAD@acuecc.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (body.length() < 10) return false;
     
     data.strSource = body.substring(0,10).trim();
     body = "CALL:" + body.substring(10);
     
     Properties props = parseMessage(body, keywords);
     
     data.strCall = props.getProperty("CALL");
     if (data.strCall == null) return false;
     
     data.strApt = props.getProperty("APT", "");
     
     String sAddress = props.getProperty("AD");
    if (sAddress == null) return false;
     parseAddress(sAddress, data);
     
     String city = props.getProperty("CTY", "");
     if (!city.startsWith("AC")) return false;
     
     if (city.length() > 3) {
       data.strSupp = city.substring(3).trim();
     } else {
       data.strSupp = props.getProperty("LOC", "");
     }
     
     data.strCross = props.getProperty("XST", "");
     String x2 = props.getProperty("XST2", "");
     if (x2.length() > 0) {
       if (data.strCross.length() > 0) data.strCross += " & ";
       data.strCross += x2;
     }
     
     return true;
   }
 }
