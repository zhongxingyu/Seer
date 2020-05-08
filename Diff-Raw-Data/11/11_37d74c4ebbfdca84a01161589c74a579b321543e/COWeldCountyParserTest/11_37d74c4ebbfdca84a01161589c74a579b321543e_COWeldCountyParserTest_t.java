 package net.anei.cadpage.parsers.CO;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.CO.COWeldCountyParser;
 
 import org.junit.Test;
 
 
 public class COWeldCountyParserTest extends BaseParserTest {
   
   public COWeldCountyParserTest() {
     setParser(new COWeldCountyParser(), "WELD COUNTY", "CO");
   }
   
   @Test
   public void testParser1() {
     doTest("T1",
         "20442,ALARMF,23691 CR 60H.E1 E4 L1,TEXT:AUDIBLE FROM GENERAL AND SMOKE DETECTOR \\COMP:1ST CLASS SECURITY \\PH:800 482 9800," ,
         "CALL:ALARMF",
         "ADDR:23691 CR 60H",
         "MADDR:23691 COUNTY ROAD 60H",
         "ID:20442",
         "UNIT:E1 E4 L1",
         "PHONE:800 482 9800",
         "INFO:AUDIBLE FROM GENERAL AND SMOKE DETECTOR",
         "NAME:1ST CLASS SECURITY"
         );
     doTest("T2",
     	"20431,SMKODR,1ST ST/8TH AV.E4 L1,TEXT:SMELL OF NATURAL GAS ALL OVER THE AREA \\COMP:W012,",
     	"CALL:SMKODR",
     	"ADDR:1ST ST & 8TH AV",
       "MADDR:1ST ST & 8TH AVE",
     	"ID:20431",
     	"UNIT:E4 L1",
     	"INFO:SMELL OF NATURAL GAS ALL OVER THE AREA",
     	"NAME:W012"
     		);
     doTest("T3",
     	"20373,SMKODR,10TH AV/22ND ST.E4 R1,TEXT:STRONG SMELL OF NATURAL GAS IN AIR IN THIS AREA \\COMP:MIKE RUANE \\PH:608 630 2959,",	
     	"CALL:SMKODR",
     	"ADDR:10TH AV & 22ND ST",
       "MADDR:10TH AVE & 22ND ST",
     	"ID:20373",
     	"UNIT:E4 R1",
     	"INFO:STRONG SMELL OF NATURAL GAS IN AIR IN THIS AREA",
     	"NAME:MIKE RUANE",
     	"PHONE:608 630 2959"
     );
     doTest("T4",
     	"20032,SMKODR,2114 BLUEBELL AV.E4 R1,TEXT:FROM THE SOUTH, STRONG SMELL OF NATURAL GAS UNK WHERE ITS COMING FROM,",
     	"CALL:SMKODR",
     	"ADDR:2114 BLUEBELL AV",
       "MADDR:2114 BLUEBELL AVE",
     	"ID:20032",
     	"UNIT:E4 R1",
     	"INFO:FROM THE SOUTH, STRONG SMELL OF NATURAL GAS UNK WHERE ITS COMING FROM"
 	);
     doTest("T5",
     	"19897,ALARMF,2013 1ST AV.E4 E1 L1,TEXT:AUDIBLE COMING FROM PULL STATION  NW DOOR, NO ATC, PREM PH 970 674 1957 \\COMP:OP 311 SHERLOCK PLUS \\PH:800 443",
     	"CALL:ALARMF",
     	"ADDR:2013 1ST AV",
       "MADDR:2013 1ST AVE",
     	"ID:19897",
     	"UNIT:E4 E1 L1",
     	"INFO:AUDIBLE COMING FROM PULL STATION NW DOOR, NO ATC, PREM PH 970 674 1957",
     	"NAME:OP 311 SHERLOCK PLUS",
     	"PHONE:800 443"
 	);
 
     doTest("T6",
     	"19547,AIRAF ,600 AIRPORT RD.E4,TEXT:PLANE COMING IN WITHOUT LANDING GEAR.  RP WANTS FIRE AND MEDICAL TO RESPOND AND STANDBY. \\COMP:MANDY \\PH:336-3000,",
     	"CALL:AIRAF",
     	"ADDR:600 AIRPORT RD",
     	"ID:19547",
     	"UNIT:E4",
     	"INFO:PLANE COMING IN WITHOUT LANDING GEAR. RP WANTS FIRE AND MEDICAL TO RESPOND AND STANDBY.",
     	"NAME:MANDY",
     	"PHONE:336-3000"
     );
 
     doTest("T7",
         "22668,TAUF  ,11TH AV/18TH ST.E2 R1 WA,TEXT:RP HIT A GUY IN CROSSWALK,HE\nIS TRYING TO WALK AWAY,",
         "CALL:TAUF",
         "ADDR:11TH AV & 18TH ST",
         "MADDR:11TH AVE & 18TH ST",
         "ID:22668",
         "UNIT:E2 R1 WA",
         "INFO:RP HIT A GUY IN CROSSWALK, HE IS TRYING TO WALK AWAY");
     
   }
   
   @Test
   public void testparser2() {
 
     doTest("T1",
         "/ 10903,SI    -SICK & INJ (F&A),3103 23RD AV.E2 WA,,\n\n\n",
         "CALL:SI -SICK & INJ (F&A)",
         "ADDR:3103 23RD AV",
         "MADDR:3103 23RD AVE",
         "ID:10903",
         "UNIT:E2 WA");
 
     doTest("T2",
         "/ 11470,SI    -SICK & INJ (F&A),3136 51ST AV.E5 WA,TEXT:RIB PAIN,FELL A WEEK \nAGO \\COMP:DEL \\PH:314-226-9976,\n\n\n",
         "CALL:SI -SICK & INJ (F&A)",
         "ADDR:3136 51ST AV",
         "MADDR:3136 51ST AVE",
         "ID:11470",
         "UNIT:E5 WA",
         "INFO:RIB PAIN, FELL A WEEK AGO",
         "NAME:DEL",
         "PHONE:314-226-9976");
    
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "\" \" (Dispatch) 22452,FIREVF-FIRE VEHICLE (F),15899 NANCY AV.26,TEXT:TRUCK ON FIRE, NO \nONE INSIDE 2 OUTSIDE, TXT STOP to opt-out",
         "ID:22452",
         "CALL:FIREVF-FIRE VEHICLE (F)",
         "ADDR:15899 NANCY AV",
         "MADDR:15899 NANCY AVE",
         "UNIT:26",
         "INFO:TRUCK ON FIRE, NO ONE INSIDE 2 OUTSIDE");
 
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
         "MADDR:905 S MCKINLEY AVE",
         "UNIT:26 PM3",
         "INFO:FEM FAINTED/WON'T WAKE UP NOW / SOURCE:E911",
         "NAME:LUCY GALLEGOS",
         "PHONE:720.985.1607");
 
     doTest("T4",
         "\" \" (Dispatch) 22411,SIPFF -SICK & INJ   (F),15125 GOOD AV.26 PM3,TEXT:HUSB IN THE TUB, \nUNCONS/BREATHING \\COMP:VALERIE JENKINS \\PH:303*710*9486, TXT STOP to opt-out",
         "ID:22411",
         "CALL:SIPFF -SICK & INJ (F)",
         "ADDR:15125 GOOD AV",
         "MADDR:15125 GOOD AVE",
         "UNIT:26 PM3",
         "INFO:HUSB IN THE TUB, UNCONS/BREATHING",
         "NAME:VALERIE JENKINS",
         "PHONE:303*710*9486");
 
     doTest("T5",
         "\" \" (Dispatch) 22407,SI    -SICK & INJ (F&A),9643 CR 23.26 PM3,TEXT:CO ALARM GOING OFF \n\\COMP:LYNETTE \\PH:303 857 4432, TXT STOP to opt-out",
         "ID:22407",
         "CALL:SI -SICK & INJ (F&A)",
         "ADDR:9643 CR 23",
         "MADDR:9643 COUNTY ROAD 23",
         "UNIT:26 PM3",
         "INFO:CO ALARM GOING OFF",
         "NAME:LYNETTE",
         "PHONE:303 857 4432");
 
     doTest("T6",
         "\" \" (Dispatch) 22452,FIREVF-FIRE VEHICLE (F),15899 NANCY AV.26,TEXT:TRUCK ON FIRE, NO \nONE INSIDE 2 OUTSIDE, TXT STOP to opt-out",
         "ID:22452",
         "CALL:FIREVF-FIRE VEHICLE (F)",
         "ADDR:15899 NANCY AV",
         "MADDR:15899 NANCY AVE",
         "UNIT:26",
         "INFO:TRUCK ON FIRE, NO ONE INSIDE 2 OUTSIDE");
 
     doTest("T7",
         "\" \" (Dispatch) 22570,SIPFF -SICK & INJ   (F),1300 DEXTER ST.26,TEXT:MAN FELL , BLEEDING \nFROM HEAD BADLY \\COMP:MICHELLE \\PH:7209388915, TXT STOP to opt-out",
         "ID:22570",
         "CALL:SIPFF -SICK & INJ (F)",
         "ADDR:1300 DEXTER ST",
         "UNIT:26",
         "INFO:MAN FELL, BLEEDING FROM HEAD BADLY",
         "NAME:MICHELLE",
         "PHONE:7209388915");
 
     doTest("T8",
         "\" \" (Dispatch) 22606,ATSUCF-ATTP SUICIDE (F),2926 GLEN DALE DR.24 WA,TEXT:OVERDOSE \\COMP:DIANA \n\\LOC:ALI - SHOWS #6 \\PH:9706738057, TXT STOP to opt-out",
         "ID:22606",
         "CALL:ATSUCF-ATTP SUICIDE (F)",
         "ADDR:2926 GLEN DALE DR",
         "UNIT:24 WA",
         "INFO:OVERDOSE / LOC:ALI - SHOWS #6",
         "NAME:DIANA",
         "PHONE:9706738057");
 
     doTest("T9",
         "\" \" (Dispatch) 22648,SIPFF -SICK & INJ   (F),3804 BURLINGTON AV.24 WA,, TXT STOP to opt-out",
         "ID:22648",
         "CALL:SIPFF -SICK & INJ (F)",
         "ADDR:3804 BURLINGTON AV",
         "MADDR:3804 BURLINGTON AVE",
         "UNIT:24 WA");
 
     doTest("T10",
         "\" \" (Dispatch) 23039,FIREGF-FIRE GRD CVR (F),103 N JOSEPHINE AV.32 29,TEXT:TREE W/ A POWER \nLINE TOUCH IT AND ITS SMOKING \\COMP:DAVID \\PH:405-8450,FG5 TXT STOP to opt-out",
         "ID:23039",
         "CALL:FIREGF-FIRE GRD CVR (F)",
         "ADDR:103 N JOSEPHINE AV",
         "MADDR:103 N JOSEPHINE AVE",
         "UNIT:32 29",
         "INFO:TREE W/ A POWER LINE TOUCH IT AND ITS SMOKING",
         "NAME:DAVID",
         "PHONE:405-8450");
 
     doTest("T11",
         "\" \" (Dispatch) 21196,SIPFF -SICK & INJ   (F),805 COMPASSION DR.WI4201 WA 42,TEXT:RES FELL \n\\COMP:AUDREY \\PH:970.686.2743, TXT STOP to opt-out",
         "ID:21196",
         "CALL:SIPFF -SICK & INJ (F)",
         "ADDR:805 COMPASSION DR",
         "UNIT:WI4201 WA 42",
         "INFO:RES FELL",
         "NAME:AUDREY",
         "PHONE:970.686.2743");
 
     doTest("T12",
         "\" \" (Dispatch) 26965,SIPFF -SICK & INJ   (F),3400 LATHAM AV.24 WA,TEXT:SISTER ILL AND \nFELL \\COMP:PEGGY PANTOJA \\PH:3369*3860, TXT STOP to opt-out",
         "ID:26965",
         "CALL:SIPFF -SICK & INJ (F)",
         "ADDR:3400 LATHAM AV",
         "MADDR:3400 LATHAM AVE",
         "UNIT:24 WA",
         "INFO:SISTER ILL AND FELL",
         "NAME:PEGGY PANTOJA",
         "PHONE:3369*3860");
 
     doTest("T13",
         "\" \" (Dispatch) 26974,SIPFF -SICK & INJ   (F),3501 MAGNOLIA ST.24 WA,TEXT:RPS MOTHER SICK \\COMP:ESMERALA \\PH:970 405 3476, TXT STOP to opt-out",
         "ID:26974",
         "CALL:SIPFF -SICK & INJ (F)",
         "ADDR:3501 MAGNOLIA ST",
         "UNIT:24 WA",
         "INFO:RPS MOTHER SICK",
         "NAME:ESMERALA",
         "PHONE:970 405 3476");
 
   }
   
   @Test
   public void testParser4() {
 
      doTest("T1",
          "Dispatch,21461,SIPFF -SICK & INJ (F),CR 61/HY 34.37 WA,TEXT:NORTH OF HY 34 1 MILE, \nPATIENT IN A SEMI VOMITING \\COMP:JOE ROBERTSON \\PH:303-579-1849,FG3",
          "ID:21461",
          "CALL:SIPFF -SICK & INJ (F)",
          "ADDR:CR 61 & HY 34",
          "MADDR:COUNTY ROAD 61 & HWY 34",
          "UNIT:37 WA",
          "INFO:NORTH OF HY 34 1 MILE, PATIENT IN A SEMI VOMITING",
          "NAME:JOE ROBERTSON",
          "PHONE:303-579-1849");
 
      doTest("T2",
          "Dispatch,21489,TAIF -TA W/INJURY (F),CR 49/HY 34.37 WA,TEXT:HEAD ON\n\\COMP:CHAD\n\n\\PH:970.744.8045,FG7",
          "ID:21489",
          "CALL:TAIF -TA W/INJURY (F)",
          "ADDR:CR 49 & HY 34",
          "MADDR:COUNTY ROAD 49 & HWY 34",
          "UNIT:37 WA",
          "INFO:HEAD ON",
          "NAME:CHAD",
          "PHONE:970.744.8045");
 
      doTest("T3",
          "Dispatch,21489,TAIF -TA W/INJURY (F),CR 49/HY 34.37 WA,TEXT:HEAD ON\n\\COMP:CHAD\n\n\\PH:970.744.8045,FG7",
          "ID:21489",
          "CALL:TAIF -TA W/INJURY (F)",
          "ADDR:CR 49 & HY 34",
          "MADDR:COUNTY ROAD 49 & HWY 34",
          "UNIT:37 WA",
          "INFO:HEAD ON",
          "NAME:CHAD",
          "PHONE:970.744.8045");
 
      doTest("T4",
          "Dispatch,21461,SIPFF -SICK & INJ (F),CR 61/HY 34.37 WA,TEXT:NORTH OF HY 34 1 MILE,\n\nPATIENT IN A SEMI VOMITING \\COMP:JOE ROBERTSON \\P",
          "ID:21461",
          "CALL:SIPFF -SICK & INJ (F)",
          "ADDR:CR 61 & HY 34",
          "MADDR:COUNTY ROAD 61 & HWY 34",
          "UNIT:37 WA",
          "INFO:NORTH OF HY 34 1 MILE, PATIENT IN A SEMI VOMITING",
          "NAME:JOE ROBERTSON");
 
      doTest("T5",
          "Dispatch,21415,SIPFF -SICK & INJ (F),24098 CR 50.37 WA,TEXT:RP NEEDING\nHELP OFF\n\nTHE FLOOR \\COMP:EVELYN \\PH:970.284.7774,FG5",
          "ID:21415",
          "CALL:SIPFF -SICK & INJ (F)",
          "ADDR:24098 CR 50",
          "MADDR:24098 COUNTY ROAD 50",
          "UNIT:37 WA",
          "INFO:RP NEEDING HELP OFF THE FLOOR",
          "NAME:EVELYN",
          "PHONE:970.284.7774");
 
      doTest("T6",
          "Dispatch,21377,FASIST-ASSIST (FIRE),21000 I 76.28,,",
          "ID:21377",
          "CALL:FASIST-ASSIST (FIRE)",
          "ADDR:21000 I 76",
          "UNIT:28");
 
      doTest("T7",
          "Dispatch,21367,SIPFF -SICK & INJ (F),6876 CR 47.28 PVA56,TEXT:CHEST PAIN \\COMP:JOSH\n\n\\PH:303 319 9255,FG9",
          "ID:21367",
          "CALL:SIPFF -SICK & INJ (F)",
          "ADDR:6876 CR 47",
          "MADDR:6876 COUNTY ROAD 47",
          "UNIT:28 PVA56",
          "INFO:CHEST PAIN",
          "NAME:JOSH",
          "PHONE:303 319 9255");
 
      doTest("T8",
          "Dispatch,21355,TAIF -TA W/INJURY (F),201 E BISON HY.28A PVA56,TEXT:BLU NEON, DIDN'T\n\nROLL. \\COMP:NICKI \\PH:970-215-5558,FG9",
          "ID:21355",
          "CALL:TAIF -TA W/INJURY (F)",
          "ADDR:201 E BISON HY",
          "MADDR:201 E BISON HWY",
          "UNIT:28A PVA56",
          "INFO:BLU NEON, DIDN'T ROLL.",
          "NAME:NICKI",
          "PHONE:970-215-5558");
 
      doTest("T9",
          "Fr:<Basepage@weldcorcc.com>\nSu:Dispatch\nTxt: 27849,SI -SICK & INJ (F&A),CR 55/CR 62.37 WA,FG 5\n\n\nid:7",
          "ID:27849",
          "CALL:SI -SICK & INJ (F&A)",
          "ADDR:CR 55 & CR 62",
          "MADDR:COUNTY ROAD 55 & COUNTY ROAD 62",
          "UNIT:37 WA");
 
     doTest("T10",
         "Dispatch / 01417,TAIF  -TA W/INJURY (F),65TH AV/HY 34 BY.E5 R1 WA,TEXT:2 VEHS, INJURIES \n\\COMP:DANIEL \\PH:970 397 9175,\n\n\n",
         "ID:01417",
         "CALL:TAIF -TA W/INJURY (F)",
         "ADDR:65TH AV & HY 34 BY",
         "MADDR:65TH AVE & HWY 34 BY",
         "UNIT:E5 R1 WA",
         "INFO:2 VEHS, INJURIES",
         "NAME:DANIEL",
         "PHONE:970 397 9175");

   }
   
   public static void  main(String[] args) {
     new COWeldCountyParserTest().generateTests("T1");
   }
 }
