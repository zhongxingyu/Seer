 package net.anei.cadpage.parsers.CO;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*
 20442,ALARMF,23691 CR 60H.E1 E4 L1,TEXT:AUDIBLE FROM GENERAL AND SMOKE DETECTOR \COMP:1ST CLASS SECURITY \PH:800 482 9800,
 20431,SMKODR,1ST ST/8TH AV.E4 L1,TEXT:SMELL OF NATURAL GAS ALL OVER THE AREA \COMP:W012,
 20373,SMKODR,10TH AV/22ND ST.E4 R1,TEXT:STRONG SMELL OF NATURAL GAS IN AIR IN THIS AREA \COMP:MIKE RUANE \PH:608 630 2959,
 20032,SMKODR,2114 BLUEBELL AV.E4 R1,TEXT:FROM THE SOUTH, STRONG SMELL OF NATURAL GAS UNK WHERE ITS COMING FROM,
 19897,ALARMF,2013 1ST AV.E4 E1 L1,TEXT:AUDIBLE COMING FROM PULL STATION  NW DOOR, NO ATC, PREM PH 970 674 1957 \COMP:OP 311 SHERLOCK PLUS \PH:800 443
 19547,AIRAF ,600 AIRPORT RD.E4,TEXT:PLANE COMING IN WITHOUT LANDING GEAR.  RP WANTS FIRE AND MEDICAL TO RESPOND AND STANDBY. \COMP:MANDY \PH:336-3000,
 22668,TAUF  ,11TH AV/18TH ST.E2 R1 WA,TEXT:RP HIT A GUY IN CROSSWALK,HE\nIS TRYING TO WALK AWAY,
 
 Contact: Doug Gilliland <dgilliland88@gmail.com>
 Sender: CommtechMessenger@mauser.greeleypd.com
   / 10903,SI    -SICK & INJ (F&A),3103 23RD AV.E2 WA,,\n\n\n
   / 11470,SI    -SICK & INJ (F&A),3136 51ST AV.E5 WA,TEXT:RIB PAIN,FELL A WEEK \nAGO \COMP:DEL \PH:314-226-9976,\n\n\n
 

  */
 
 
 public class COGreeleyParser extends SmsMsgParser {
 
   public COGreeleyParser() {
     super("Greeley", "CO");
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     String[] flds = body.split(",",-1);
     if (flds.length < 4) return false;
     data.strCallId = flds[0];
     data.strCall = flds[1].trim();
     Parser p = new Parser(flds[2]);
     data.strUnit = p.getLastOptional(".");
     if (data.strUnit.length() == 0) return false;
     parseAddress(p.get(), data);
 
     p = new Parser(body);
     p.get("TEXT:");
     data.strSupp = p.get("\\COMP:").replace('\n', ' ');
     
     return true;
   }
 }
