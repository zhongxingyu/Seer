 package net.anei.cadpage.parsers.CO;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.CO.COGreeleyParser;
 
 import org.junit.Test;
 
 
 public class COGreeleyParserTest extends BaseParserTest {
   
   public COGreeleyParserTest() {
     setParser(new COGreeleyParser(), "Greeley", "CO");
   }
   
   @Test
   public void testParser1() {
     doTest("T1",
         "20442,ALARMF,23691 CR 60H.E1 E4 L1,TEXT:AUDIBLE FROM GENERAL AND SMOKE DETECTOR \\COMP:1ST CLASS SECURITY \\PH:800 482 9800," ,
         "CALL:ALARMF",
         "ADDR:23691 CR 60H",
         "ID:20442",
         "UNIT:E1 E4 L1",
         "PHONE:800 482 9800",
         "INFO:AUDIBLE FROM GENERAL AND SMOKE DETECTOR / 1ST CLASS SECURITY"
         );
     doTest("T2",
     	"20431,SMKODR,1ST ST/8TH AV.E4 L1,TEXT:SMELL OF NATURAL GAS ALL OVER THE AREA \\COMP:W012,",
     	"CALL:SMKODR",
     	"ADDR:1ST ST & 8TH AV",
     	"ID:20431",
     	"UNIT:E4 L1",
     	"INFO:SMELL OF NATURAL GAS ALL OVER THE AREA / W012"
     		);
     doTest("T3",
     	"20373,SMKODR,10TH AV/22ND ST.E4 R1,TEXT:STRONG SMELL OF NATURAL GAS IN AIR IN THIS AREA \\COMP:MIKE RUANE \\PH:608 630 2959,",	
     	"CALL:SMKODR",
     	"ADDR:10TH AV & 22ND ST",
     	"ID:20373",
     	"UNIT:E4 R1",
     	"INFO:STRONG SMELL OF NATURAL GAS IN AIR IN THIS AREA / MIKE RUANE",
     	"PHONE:608 630 2959"
     );
     doTest("T4",
     	"20032,SMKODR,2114 BLUEBELL AV.E4 R1,TEXT:FROM THE SOUTH, STRONG SMELL OF NATURAL GAS UNK WHERE ITS COMING FROM,",
     	"CALL:SMKODR",
     	"ADDR:2114 BLUEBELL AV",
     	"ID:20032",
     	"UNIT:E4 R1",
     	"INFO:FROM THE SOUTH / STRONG SMELL OF NATURAL GAS UNK WHERE ITS COMING FROM"
 	);
     doTest("T5",
     	"19897,ALARMF,2013 1ST AV.E4 E1 L1,TEXT:AUDIBLE COMING FROM PULL STATION  NW DOOR, NO ATC, PREM PH 970 674 1957 \\COMP:OP 311 SHERLOCK PLUS \\PH:800 443",
     	"CALL:ALARMF",
     	"ADDR:2013 1ST AV",
     	"ID:19897",
     	"UNIT:E4 E1 L1",
     	"INFO:AUDIBLE COMING FROM PULL STATION NW DOOR / NO ATC / PREM PH 970 674 1957 / OP 311 SHERLOCK PLUS",
     	"PHONE:800 443"
 	);
 
     doTest("T6",
     	"19547,AIRAF ,600 AIRPORT RD.E4,TEXT:PLANE COMING IN WITHOUT LANDING GEAR.  RP WANTS FIRE AND MEDICAL TO RESPOND AND STANDBY. \\COMP:MANDY \\PH:336-3000,",
     	"CALL:AIRAF",
     	"ADDR:600 AIRPORT RD",
     	"ID:19547",
     	"UNIT:E4",
     	"INFO:PLANE COMING IN WITHOUT LANDING GEAR. RP WANTS FIRE AND MEDICAL TO RESPOND AND STANDBY. / MANDY",
     	"PHONE:336-3000"
     );
 
     doTest("T7",
         "22668,TAUF  ,11TH AV/18TH ST.E2 R1 WA,TEXT:RP HIT A GUY IN CROSSWALK,HE\nIS TRYING TO WALK AWAY,",
         "CALL:TAUF",
         "ADDR:11TH AV & 18TH ST",
         "ID:22668",
         "UNIT:E2 R1 WA",
         "INFO:RP HIT A GUY IN CROSSWALK / HE IS TRYING TO WALK AWAY");
     
   }
   
   @Test
   public void testparser2() {
 
     doTest("T1",
         "/ 10903,SI    -SICK & INJ (F&A),3103 23RD AV.E2 WA,,\n\n\n",
         "CALL:SI -SICK & INJ (F&A)",
         "ADDR:3103 23RD AV",
         "ID:10903",
         "UNIT:E2 WA");
 
     doTest("T2",
         "/ 11470,SI    -SICK & INJ (F&A),3136 51ST AV.E5 WA,TEXT:RIB PAIN,FELL A WEEK \nAGO \\COMP:DEL \\PH:314-226-9976,\n\n\n",
         "CALL:SI -SICK & INJ (F&A)",
         "ADDR:3136 51ST AV",
         "ID:11470",
         "UNIT:E5 WA",
         "INFO:RIB PAIN / FELL A WEEK AGO / DEL",
         "PHONE:314-226-9976");
    
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "\" \" (Dispatch) 22452,FIREVF-FIRE VEHICLE (F),15899 NANCY AV.26,TEXT:TRUCK ON FIRE, NO \nONE INSIDE 2 OUTSIDE, TXT STOP to opt-out",
         "ID:22452",
         "CALL:FIREVF-FIRE VEHICLE (F)",
         "ADDR:15899 NANCY AV",
         "UNIT:26",
         "INFO:TRUCK ON FIRE / NO ONE INSIDE 2 OUTSIDE");
 
     doTest("T2",
         "\" \" (Dispatch) 22440,TAUF  -TA UNK INJURY(F),1108 LANTERN DR.26 PM3,,FG10 TXT STOP to opt-out",
         "ID:22440",
         "CALL:TAUF -TA UNK INJURY(F)",
         "ADDR:1108 LANTERN DR",
         "UNIT:26 PM3");
 
     doTest("T3",
         "\" \" (Dispatch) 22422,SIPFF -SICK & INJ   (F),905 S MCKINLEY AV.26 PM3,TEXT:FEM FAINTED/WON'T \nWAKE UP NOW \\COMP:LUCY GALLEGOS \\SOURCE:E911 \\PH:720.985.1607,FG10 TXT STOP to opt-out",
         "ID:22422",
         "CALL:SIPFF -SICK & INJ (F)",
         "ADDR:905 S MCKINLEY AV",
         "UNIT:26 PM3",
         "INFO:FEM FAINTED/WON'T WAKE UP NOW / LUCY GALLEGOS",
         "PHONE:720.985.1607");
 
     doTest("T4",
         "\" \" (Dispatch) 22411,SIPFF -SICK & INJ   (F),15125 GOOD AV.26 PM3,TEXT:HUSB IN THE TUB, \nUNCONS/BREATHING \\COMP:VALERIE JENKINS \\PH:303*710*9486, TXT STOP to opt-out",
         "ID:22411",
         "CALL:SIPFF -SICK & INJ (F)",
         "ADDR:15125 GOOD AV",
         "UNIT:26 PM3",
         "INFO:HUSB IN THE TUB / UNCONS/BREATHING / VALERIE JENKINS",
         "PHONE:303*710*9486");
 
     doTest("T5",
         "\" \" (Dispatch) 22407,SI    -SICK & INJ (F&A),9643 CR 23.26 PM3,TEXT:CO ALARM GOING OFF \n\\COMP:LYNETTE \\PH:303 857 4432, TXT STOP to opt-out",
         "ID:22407",
         "CALL:SI -SICK & INJ (F&A)",
         "ADDR:9643 CR 23",
         "UNIT:26 PM3",
         "INFO:CO ALARM GOING OFF / LYNETTE",
         "PHONE:303 857 4432");
 
     doTest("T6",
         "\" \" (Dispatch) 22452,FIREVF-FIRE VEHICLE (F),15899 NANCY AV.26,TEXT:TRUCK ON FIRE, NO \nONE INSIDE 2 OUTSIDE, TXT STOP to opt-out",
         "ID:22452",
         "CALL:FIREVF-FIRE VEHICLE (F)",
         "ADDR:15899 NANCY AV",
         "UNIT:26",
         "INFO:TRUCK ON FIRE / NO ONE INSIDE 2 OUTSIDE");
 
    doTest("T7",
        "\" \" (Dispatch) 22570,SIPFF -SICK & INJ   (F),1300 DEXTER ST.26,TEXT:MAN FELL , BLEEDING \nFROM HEAD BADLY \\COMP:MICHELLE \\PH:7209388915, TXT STOP to opt-out",
        "ID:22570",
        "CALL:SIPFF -SICK & INJ (F)",
        "ADDR:1300 DEXTER ST",
        "UNIT:26",
        "INFO:MAN FELL / BLEEDING FROM HEAD BADLY / MICHELLE",
        "PHONE:7209388915");

   }
   
   public static void  main(String[] args) {
     new COGreeleyParserTest().generateTests("T1");
   }
 }
