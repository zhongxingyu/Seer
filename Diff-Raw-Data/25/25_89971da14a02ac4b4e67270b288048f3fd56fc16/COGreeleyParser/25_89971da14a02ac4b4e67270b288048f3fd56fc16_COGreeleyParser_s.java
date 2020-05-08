 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /*
 20442,ALARMF,23691 CR 60H.E1 E4 L1,TEXT:AUDIBLE FROM GENERAL AND SMOKE DETECTOR \COMP:1ST CLASS SECURITY \PH:800 482 9800,
 20431,SMKODR,1ST ST/8TH AV.E4 L1,TEXT:SMELL OF NATURAL GAS ALL OVER THE AREA \COMP:W012,
 20373,SMKODR,10TH AV/22ND ST.E4 R1,TEXT:STRONG SMELL OF NATURAL GAS IN AIR IN THIS AREA \COMP:MIKE RUANE \PH:608 630 2959,
 20032,SMKODR,2114 BLUEBELL AV.E4 R1,TEXT:FROM THE SOUTH, STRONG SMELL OF NATURAL GAS UNK WHERE ITS COMING FROM,
 19897,ALARMF,2013 1ST AV.E4 E1 L1,TEXT:AUDIBLE COMING FROM PULL STATION  NW DOOR, NO ATC, PREM PH 970 674 1957 \COMP:OP 311 SHERLOCK PLUS \PH:800 443
 19547,AIRAF ,600 AIRPORT RD.E4,TEXT:PLANE COMING IN WITHOUT LANDING GEAR.  RP WANTS FIRE AND MEDICAL TO RESPOND AND STANDBY. \COMP:MANDY \PH:336-3000,
  */
 
 
 public class COGreeleyParser extends SmsMsgParser {
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (!body.contains(",TEXT:")) return false;
 
     data.defState="CO";
     data.defCity = "Greeley";
     String[] flds = body.split(",");
     if (flds.length > 3) {
       data.strCallId = flds[0];
       data.strCall = flds[1].trim();
       Parser p = new Parser(flds[2]);
       data.strUnit = p.getLastOptional(".");
       parseAddress(p.get(), data);
     }
     Parser p = new Parser(body);
     p.get("TEXT:");
     data.strSupp = p.get("\\COMP:");
     
     return true;
   }
 }
