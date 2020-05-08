 package net.anei.cadpage.parsers.WA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class WAClarkCountyParserTest extends BaseParserTest {
   
   public WAClarkCountyParserTest() {
     setParser(new WAClarkCountyParser(), "CLARK COUNTY", "WA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CRESA LOC: 3615 NE EVERETT ST CAM MAP: 1302 OPS:   SEIZURE SUB TYPE: A1 PRI: 4 TIME: 18:51:00 EV#: 12166142 ALARM: 1  Disp: E41,E91,M41",
         "SRC:CRESA",
         "ADDR:3615 NE EVERETT ST",
         "CITY:Camas",
         "MAP:1302",
         "CALL:SEIZURE",
         "CODE:A1",
         "PRI:4",
         "TIME:18:51:00",
         "ID:12166142",
         "UNIT:E41,E91,M41");
 
     doTest("T2",
         "CRESA LOC: 1315 NE 310TH AVE CLK MAP: 2430 OPS:   PUBLIC ASSIST/SERVICE CALL SUB TYPE: BU PRI: 4 TIME: 07:42:36 EV#: 12166548 ALARM: 1  Disp: SQ91",
         "SRC:CRESA",
         "ADDR:1315 NE 310TH AVE",
         "MAP:2430",
         "CALL:PUBLIC ASSIST/SERVICE CALL",
         "CODE:BU",
         "PRI:4",
         "TIME:07:42:36",
         "ID:12166548",
         "UNIT:SQ91");
 
     doTest("T3",
         "CRESA LOC: 39801 NE 26TH ST CLK: @STATION 96 S96 MAP: 2424 OPS:   PUBLIC ASSIST/Service Call SUB TYPE: A PRI: 5 TIME: 15:36:47 EV#: 12168096 ALARM: 1 Disp: SQ96",
         "SRC:CRESA",
         "ADDR:39801 NE 26TH ST",
         "PLACE:STATION 96 S96",
         "MAP:2424",
         "CALL:PUBLIC ASSIST/Service Call",
         "CODE:A",
         "PRI:5",
         "TIME:15:36:47",
         "ID:12168096",
         "UNIT:SQ96");
 
     doTest("T4",
         "CRESA LOC: 26719 NE HATHAWAY RD CLK MAP: 2326 OPS:   FALLS SUB TYPE: A1 PRI: 5 TIME: 11:41:49 EV#: 12167852 ALARM: 1  Disp: E91,M45",
         "SRC:CRESA",
         "ADDR:26719 NE HATHAWAY RD",
         "MAP:2326",
         "CALL:FALLS",
         "CODE:A1",
         "PRI:5",
         "TIME:11:41:49",
         "ID:12167852",
         "UNIT:E91,M45");
 
     doTest("T5",
         "CRESA LOC: NE 261ST AVE/NE 38TH ST CLK MAP: 2323 OPS:   UNKNOWN PROBLEM SUB TYPE: B3 PRI: 3 TIME: 07:52:21 EV#: 12168775 ALARM: 1  Disp: E91,M45",
         "SRC:CRESA",
         "ADDR:NE 261ST AVE & NE 38TH ST",
         "MAP:2323",
         "CALL:UNKNOWN PROBLEM",
         "CODE:B3",
         "PRI:3",
         "TIME:07:52:21",
         "ID:12168775",
         "UNIT:E91,M45");
 
     doTest("T6",
         "CRESA LOC: 38900 SE NICHOLS HILL RD CLK MAP: 1402 OPS:   CARDIAC OR RESPIRATORY ARREST / DEATH SUB TYPE: D1 PRI: 1 TIME: 12:46:01 EV#: 12169964 ALARM: 1  Disp: C91,E171,E94,M43",
         "SRC:CRESA",
         "ADDR:38900 SE NICHOLS HILL RD",
         "MAP:1402",
         "CALL:CARDIAC OR RESPIRATORY ARREST / DEATH",
         "CODE:D1",
         "PRI:1",
         "TIME:12:46:01",
         "ID:12169964",
         "UNIT:C91,E171,E94,M43");
 
     doTest("T7",
         "CRESA LOC: 2516 NE 277TH AVE CLK MAP: 2324 OPS:   TRAFFIC ACCIDENT SUB TYPE: B4 PRI: 3 TIME: 15:59:57 EV#: 12170126 ALARM: 1  Disp: E91,M41,SQ91",
         "SRC:CRESA",
         "ADDR:2516 NE 277TH AVE",
         "MAP:2324",
         "CALL:TRAFFIC ACCIDENT",
         "CODE:B4",
         "PRI:3",
         "TIME:15:59:57",
         "ID:12170126",
         "UNIT:E91,M41,SQ91");
 
     doTest("T8",
         "CRESA LOC: 3600 NE CAVITT RD CLK MAP: 2324 OPS:   TRAFFIC ACCIDENT SUB TYPE: B4 PRI: 3 TIME: 15:59:57 EV#: 12170126 ALARM: 1  Disp: C91,E91,M41,SQ91",
         "SRC:CRESA",
         "ADDR:3600 NE CAVITT RD",
         "MAP:2324",
         "CALL:TRAFFIC ACCIDENT",
         "CODE:B4",
         "PRI:3",
         "TIME:15:59:57",
         "ID:12170126",
         "UNIT:C91,E91,M41,SQ91");
 
     doTest("T9",
         "CRESA LOC: 26801 NE 9TH ST CLK,SP 50: @OAK MEADOWS MOBILE PARK MAP: 2326 OPS: OPS51  STRUCTURE FIRE SUB TYPE: R PRI: 1 TIME: 17:20:40 EV#: 12170184 ALARM: 1  Disp: A93,E171,E41,E42,E91,RE93,WT91,WT93",
         "SRC:CRESA",
         "ADDR:26801 NE 9TH ST",
         "APT:SP 50",
         "PLACE:OAK MEADOWS MOBILE PARK",
         "MAP:2326",
         "CALL:OPS51  STRUCTURE FIRE",
         "CODE:R",
         "PRI:1",
         "TIME:17:20:40",
         "ID:12170184",
         "UNIT:A93,E171,E41,E42,E91,RE93,WT91,WT93");
 
     doTest("T10",
         "CRESA LOC: 936 NE 41ST AVE CAM MAP: 2335 OPS:   PSYCHIATRIC / SUICIDE ATTEMPT SUB TYPE: B6V PRI: 3 TIME: 21:45:53 EV#: 12170453 ALARM: 1  Disp: E41,E91,M41",
         "SRC:CRESA",
         "ADDR:936 NE 41ST AVE",
         "CITY:Camas",
         "MAP:2335",
         "CALL:PSYCHIATRIC / SUICIDE ATTEMPT",
         "CODE:B6V",
         "PRI:3",
         "TIME:21:45:53",
         "ID:12170453",
         "UNIT:E41,E91,M41");
 
     doTest("T11",
         "CRESA LOC: 33008 NE WASHOUGAL RIVER RD CLK MAP: 2429 OPS:   ASSAULT SUB TYPE: A1 PRI: 4 TIME: 21:56:35 EV#: 12171336 ALARM: 1  Disp: E94,M41",
         "SRC:CRESA",
         "ADDR:33008 NE WASHOUGAL RIVER RD",
         "MAP:2429",
         "CALL:ASSAULT",
         "CODE:A1",
         "PRI:4",
         "TIME:21:56:35",
         "ID:12171336",
         "UNIT:E94,M41");
 
     doTest("T12",
         "CRESA LOC: NE 3RD ST/NE 267TH AVE CLK MAP: 2326 OPS: OPS51  TRAFFIC ACCIDENT SUB TYPE: D2L PRI: 2 TIME: 11:39:51 EV#: 12171765 ALARM: 1  Disp: C91,E41,E91,M41",
         "SRC:CRESA",
         "ADDR:NE 3RD ST & NE 267TH AVE",
         "MAP:2326",
         "CALL:OPS51  TRAFFIC ACCIDENT",
         "CODE:D2L",
         "PRI:2",
         "TIME:11:39:51",
         "ID:12171765",
         "UNIT:C91,E41,E91,M41");
 
     doTest("T13",
         "CRESA LOC: 28416 NE REILLY RD CLK MAP: 2324 OPS:   STROKE SUB TYPE: C1L PRI: 2 TIME: 13:39:23 EV#: 12171853 ALARM: 1  Disp: E91,M41",
         "SRC:CRESA",
         "ADDR:28416 NE REILLY RD",
         "MAP:2324",
         "CALL:STROKE",
         "CODE:C1L",
         "PRI:2",
         "TIME:13:39:23",
         "ID:12171853",
         "UNIT:E91,M41");
 
     doTest("T14",
         "CRESA LOC: 605 SE TAMPA ST CAM MAP: 1312 OPS:   BREATHING PROBLEMS SUB TYPE: D1 PRI: 2 TIME: 16:01:38 EV#: 12171988 ALARM: 1  Disp: E42,E91,M45",
         "SRC:CRESA",
         "ADDR:605 SE TAMPA ST",
         "CITY:Camas",
         "MAP:1312",
         "CALL:BREATHING PROBLEMS",
         "CODE:D1",
         "PRI:2",
         "TIME:16:01:38",
         "ID:12171988",
         "UNIT:E42,E91,M45");
 
     doTest("T15",
         "CRESA LOC: 4354 V ST WAS MAP: 1409 OPS:   BREATHING PROBLEMS SUB TYPE: D2 PRI: 2 TIME: 18:25:20 EV#: 12172139 ALARM: 1  Disp: E94,M43",
         "SRC:CRESA",
         "ADDR:4354 V ST",
         "CITY:Washougal",
         "MAP:1409",
         "CALL:BREATHING PROBLEMS",
         "CODE:D2",
         "PRI:2",
         "TIME:18:25:20",
         "ID:12172139",
         "UNIT:E94,M43");
 
     doTest("T16",
         "CRESA LOC: 600 NE 267TH AVE CLK: @STATION 91 S91 MAP: 2326 OPS:   PUBLIC ASSIST/Service Call SUB TYPE: T PRI: 5 TIME: 09:48:03 EV#: 12172571 ALARM: 1  Disp: E91",
         "SRC:CRESA",
         "ADDR:600 NE 267TH AVE",
         "PLACE:STATION 91 S91",
         "MAP:2326",
         "CALL:PUBLIC ASSIST/Service Call",
         "CODE:T",
         "PRI:5",
         "TIME:09:48:03",
         "ID:12172571",
         "UNIT:E91");
 
     doTest("T17",
         "CRESA LOC: 1922 SE BLAIR RD CLK MAP: 1405 OPS:   CHEST PAIN SUB TYPE: D2 PRI: 2 TIME: 14:16:12 EV#: 12172800 ALARM: 1  Disp: E91,M43",
         "SRC:CRESA",
         "ADDR:1922 SE BLAIR RD",
         "MAP:1405",
         "CALL:CHEST PAIN",
         "CODE:D2",
         "PRI:2",
         "TIME:14:16:12",
         "ID:12172800",
         "UNIT:E91,M43");
   }
   
   public static void main(String[] args) {
     new WAClarkCountyParserTest().generateTests("T1");
   }
 }
