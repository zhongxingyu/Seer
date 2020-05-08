 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MD.MDFrederickCountyParser;
 
 import org.junit.Test;
 
 
 public class MDFrederickCountyParserTest extends BaseParserTest {
   
   public MDFrederickCountyParserTest() {
     setParser(new MDFrederickCountyParser(), "FREDERICK COUNTY", "MD");
   }
   
   @Test
   public void testBadMsg() {
     doBadTest("I will be there till about 11 so if u want come on in");
   }
   
   @Test
   public void testParser() {
         
     doTest("T1",
         "(CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 6801 HARBAUGH RD CSAB:NEXT TO HER HOME ESZ: 626 MAP: 3996A6 Disp: [23]" ,
         "CALL:HOUSE/FIRE-VISIBLE",
         "ADDR:6801 HARBAUGH RD",
         "CITY:Sabillasville",
         "PLACE:NEXT TO HER HOME",
         "BOX:626",
         "MAP:3996A6"
         );
     doTest("T2",
         "(CAD) [FredCo] CT: WOODS FIRE 7819 FRIENDS CREEK RD CEMB ESZ: 627 MAP: 3996F6 Disp: E63,B66,DNR [36]" ,
         "CALL:WOODS FIRE",
         "ADDR:7819 FRIENDS CREEK RD",
         "CITY:Emmitsburg",
         "UNIT:E63,B66,DNR",
         "BOX:627",
         "MAP:3996F6"
         );
     doTest("T3",
         "(CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / AUTOMATIC 4 PAWS PL THUR: @CATOCTIN VETERINARY CLINIC ESZ: 1001 MAP: 4108E9 Disp: E102,TWR6 [37]" ,
         "CALL:COMMERCIAL FIRE ALARM/AUTOMATIC",
         "ADDR:4 PAWS PL",
         "CITY:Thurmont",
         "PLACE:CATOCTIN VETERINARY CLINIC",
         "UNIT:E102,TWR6",
         "BOX:1001",
         "MAP:4108E9"
         );
     doTest("T4",
         "(CAD) [FredCo] CT: PERSON FIRE (INSIDE) 16825 S SETON AVE CEMB: @NATIONAL EMERGENCY TRAINING CENTER:SIMULATION LAB ESZ: 620 MAP: 3997H9 Disp: [34]" ,
         "CALL:PERSON FIRE (INSIDE)",
         "ADDR:16825 S SETON AVE",
         "CITY:Emmitsburg",
         "PLACE:NATIONAL EMERGENCY TRAINING CENTER:SIMULATION LAB",
         "BOX:620",
         "MAP:3997H9"
         );
     doTest("T5",
         "(CAD) [FredCo] CT: OUTSIDE INVESTIGATION @MA ADAM CO: @RT15SB/BOYLE RD ESZ: -1 Disp: E63 [38]" ,
         "CALL:Mutual Aid: OUTSIDE INVESTIGATION",
         "ADDR:RT15SB & BOYLE RD",
         "CITY:Adams County",
         "ST:PA",
         "UNIT:E63",
         "BOX:-1"
         );
     doTest("T5A",
         "(CAD) [FredCo] CT: OUTSIDE INVESTIGATION @MA ADAM CO ESZ: -1 Disp: E63 [38]" ,
         "CALL:Mutual Aid: OUTSIDE INVESTIGATION",
         "CITY:Adams County",
         "ST:PA",
         "UNIT:E63",
         "BOX:-1"
         );
     doTest("T6",
         "(FredCo) [CAD] CT: CHEST PAIN  1811 MONOCACY BLVD FRE1: @WAL MART: PHARMACY  ESZ: 215 MAP: 4448J9  Disp: A29,A247,M17",
         "CALL:CHEST PAIN",
         "ADDR:1811 MONOCACY BLVD",
         "CITY:Frederick City",
         "PLACE:WAL MART: PHARMACY",
         "MAP:4448J9",
         "UNIT:A29,A247,M17",
         "BOX:215"
         );
     
     doTest("T7",
         "(FredCo) [CAD] CT: HOUSE / FIRE-VISIBLE CACO: @MA CARRCO: 1594 BAUST CHURCH RD ESZ: 9902 Disp: ET94,",
         "CALL:Mutual Aid: HOUSE / FIRE-VISIBLE CACO:",
         "ADDR:1594 BAUST CHURCH RD",
         "CITY:Carroll County",
         "BOX:9902",
         "UNIT:ET94,");
     
     doTest("T18",
         "[FredCo] CT: INJURY FROM VEHICLE ACCIDENT LL(-77:23:59.6013,39:21:53.1520): @I270NB / MONOCACY RIVER ESZ: 327002 MAP: 46",
         "CALL:INJURY FROM VEHICLE ACCIDENT",
         "ADDR:LL(-77:23:59.6013,39:21:53.1520)",
         "PLACE:I270NB / MONOCACY RIVER",
         "BOX:327002",
         "MAP:46");
 
     doTest("T19",
         "(CAD) [FredCo] CT: TROUBLE BREATHING / default 13862-A OLD NATIONAL PIKE CMTY TIME: 01:39:41 ESZ: 1521 MAP: 4690B2 Disp: CCM19,CCLS1,RE153",
         "CALL:TROUBLE BREATHING/default",
         "ADDR:13862-A OLD NATIONAL PIKE",
         "CITY:Mt Airy",
         "BOX:1521",
         "UNIT:CCM19,CCLS1,RE153",
         "MAP:4690B2");
 
     doTest("T20",
         "(CAD) [FredCo] CT: HOUSE / APPLIANCE FIRE (STRUCTURE PRE-ALERT) / default 6405 SPRING FOREST RD CFR1 TIME: 17:14:24 ESZ: 3302 MAP: 4567F5 SUBDIV: SPRING RIDGE Dis",
         "CALL:HOUSE/APPLIANCE FIRE (STRUCTURE PRE-ALERT)/default",
         "ADDR:6405 SPRING FOREST RD",
         "CITY:Frederick City",
         "BOX:3302",
         "MAP:4567F5 SUBDIV: SPRING RIDGE");
 
     doTest("T21",
         "(CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 808 E SOUTH ST FRE1 TIME: 03:24:42 ESZ: 308 MAP: 4566G5 Disp: E31,E22,E331,E311,A38,RS3,TR42,TWR1,T1,ET233,ET154,BAT900,SF",
         "CALL:HOUSE/FIRE-VISIBLE",
         "ADDR:808 E SOUTH ST",
         "CITY:Frederick City",
         "BOX:308",
         "UNIT:E31,E22,E331,E311,A38,RS3,TR42,TWR1,T1,ET233,ET154,BAT900,SF",
         "MAP:4566G5");
 
     doTest("T22",
         "(CAD) [FredCo] CT: TROUBLE BREATHING / default 7 S FEDERAL ST NEWM TIME: 12:02:44 ESZ: 1501 MAP: 4568H9 Disp: A159,M17",
         "CALL:TROUBLE BREATHING/default",
         "ADDR:7 S FEDERAL ST",
         "CITY:New Market",
         "BOX:1501",
         "UNIT:A159,M17",
         "MAP:4568H9");
 
     doTest("T23",
         "(CAD) [FredCo] CT: OUTSIDE INVESTIGATION / default 5018 GREEN VALLEY RD CMON TIME: 21:24:52 ESZ: 1503 MAP: 4688G1 Disp: RE153",
         "CALL:OUTSIDE INVESTIGATION/default",
         "ADDR:5018 GREEN VALLEY RD",
         "CITY:Monrovia",
         "BOX:1503",
         "UNIT:RE153",
         "MAP:4688G1");
 
     doTest("T24",
         "(CAD) [FredCo] CT: SICK PERSON / default 5850 EAGLEHEAD DR CIJM: @OAKDALE HIGH SCHOOL TIME: 08:52:07 ESZ: 1513 MAP: 4568A7 Disp: A159",
         "CALL:SICK PERSON/default",
         "ADDR:5850 EAGLEHEAD DR",
         "CITY:New Market",
         "PLACE:OAKDALE HIGH SCHOOL",
         "BOX:1513",
         "UNIT:A159",
         "MAP:4568A7");
 
     doTest("T25",
         "(CAD) [FredCo] CT: VEHICLE ACCIDENT / default BELLS LN/BALTIMORE RD CFR4 TIME: 09:58:14 ESZ: 3306 MAP: 4567F7 Disp: A339,RE153,E331",
         "CALL:VEHICLE ACCIDENT/default",
         "ADDR:BELLS LN & BALTIMORE RD",
         "CITY:Frederick City",
         "BOX:3306",
         "UNIT:A339,RE153,E331",
         "MAP:4567F7");
 
     doTest("T26",
         "(CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 1103 VILLAGE GATE DR MTAY: NEXT TOO TIME: 11:20:23 ESZ: 1527 MAP: 4570C10 Disp: RE153,E251,TWR23",
         "CALL:HOUSE/FIRE-VISIBLE",
         "ADDR:1103 VILLAGE GATE DR",
         "CITY:Mt Airy",
         "PLACE:NEXT TOO",
         "BOX:1527",
         "UNIT:RE153,E251,TWR23",
         "MAP:4570C10");
  }
   
   public static void main(String[] args) {
    new MDFrederickCountyParserTest().generateTests("T27", "CALL ADDR CITY PLACE BOX UNIT MAP");
   }
 }
