 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class TXHarrisCountyESD1BParserTest extends BaseParserTest {
   
   public TXHarrisCountyESD1BParserTest() {
     setParser(new TXHarrisCountyESD1BParser(), "HARRIS COUNTY", "TX");
   }
   
   @Test
   public void testRunReport() {
 
     doTest("T1",
         "ID#:11-08-29436 - UNIT:L32 - DISP: 21:06:25 - ER: - OUT: - TO: - AT: - AIR: 21:0",
         "CALL:RUN REPORT",
         "PLACE:ID#:11-08-29436 - UNIT:L32 - DISP: 21:06:25 - ER: - OUT: - TO: - AT: - AIR: 21:0");
 
     doTest("T2",
         "FRM:cadnoreply@proxy.hcec.com\nMSG:ID#:12-06-21170- UNIT:E13 - DISP: 11:19:11 - ER: - OUT: - AIR: 11:23:42",
         "CALL:RUN REPORT",
         "PLACE:ID#:12-06-21170- UNIT:E13 - DISP: 11:19:11 - ER: - OUT: - AIR: 11:23:42");
 
     doTest("T3",
         "FRM:cadnoreply@proxy.hcec.com\nMSG:ID#:12-06-21170- UNIT:BRU23 - DISP: 10:57:35 - ER: - OUT: - AIR: 11:23:56",
         "CALL:RUN REPORT",
         "PLACE:ID#:12-06-21170- UNIT:BRU23 - DISP: 10:57:35 - ER: - OUT: - AIR: 11:23:56");
 
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "ID#:11-06-20685 - - TRASH FIRE - 111 Bayou Dr - Apt:C - Bldg: - Key Map: 498D - Cross Streets:EAST FWY/MARKET - Box #:2002",
         "ID:11-06-20685",
         "CALL:TRASH FIRE",
         "ADDR:111 Bayou Dr",
         "APT:C",
         "MAP:498D",
         "X:EAST FWY/MARKET",
         "BOX:2002");
 
     doTest("T2",
         "ID#:11-06-20710 - - FIRE - PreAlert - 16217 Ridlon - Apt: - Bldg: intra SERVICES - Key Map: 498C - Cross Streets:SHEILA LN/WOODLAND D",
         "ID:11-06-20710",
         "CALL:FIRE - PreAlert",
         "ADDR:16217 Ridlon",
         "PLACE:intra SERVICES",
         "MAP:498C",
         "X:SHEILA LN/WOODLAND D");
 
     doTest("T3",
         "ID#:11-06-20658 - - 29A1 MOTOR VEHICLE A - East Fwy / Delldale - Apt: - Bldg: - Key Map: 498E - Cross Streets: - Box #:2004",
         "ID:11-06-20658",
         "CALL:29A1 MOTOR VEHICLE A",
         "ADDR:East Fwy & Delldale",
         "MAP:498E",
         "BOX:2004");
 
     doTest("T4",
         "ID#:11-06-20627 - - 29A1 MOTOR VEHICLE A - 16410 Ave D - Apt:1 - Bldg: AVE D APTS - Key Map: 498D - Cross Streets:WOODLAND DR/CEDAR L",
         "ID:11-06-20627",
         "CALL:29A1 MOTOR VEHICLE A",
         "ADDR:16410 Ave D",
         "PLACE:AVE D APTS",
         "APT:1",
         "MAP:498D",
         "X:WOODLAND DR/CEDAR L");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "ID#:11-07-25331 - - ALARMS - 1518 Great Dover Cir - Apt: - Bldg: - Key Map: 458W",
         "ID:11-07-25331",
         "CALL:ALARMS",
         "ADDR:1518 Great Dover Cir",
         "MAP:458W");
 
     doTest("T2",
         "ID#:11-07-25341 - 06D02 - 6D1 RESPIRATORY - 11870 Greenloch Ln - Apt: - Bldg: - Key Map: 416K - Cross Streets:GREENCANYON DR/GREENROC",
         "ID:11-07-25341",
         "CODE:06D02",
         "CALL:6D1 RESPIRATORY",
         "ADDR:11870 Greenloch Ln",
         "MAP:416K",
         "X:GREENCANYON DR/GREENROC");
 
     doTest("T3",
         "ID#:11-07-25342 - 25A01 - PSYCHIATRIC - 15215 S Brentwood Dr - Apt: - Bldg: - Key Map: 498E - Cross Streets:Dead End/DELLDALE - Box #:2004",
         "ID:11-07-25342",
         "CODE:25A01",
         "CALL:PSYCHIATRIC",
         "ADDR:15215 S Brentwood Dr",
         "MAP:498E",
         "X:Dead End/DELLDALE",
         "BOX:2004");
 
     doTest("T4",
         "ID#:11-07-25378 - - Stroke (CVA) - 11715 Greenglen Dr - Apt: - Bldg: - Key Map: 416K - Cross Streets:JOHN RALSTON RD/GREENROCK LN - B",
         "ID:11-07-25378",
         "CALL:Stroke (CVA)",
         "ADDR:11715 Greenglen Dr",
         "MAP:416K",
         "X:JOHN RALSTON RD/GREENROCK LN");
 
     doTest("T5",
         "ID#:11-07-25416 - - HEMORRHAGE/LACERATIO - 12370 S San Circle Dr - Apt: - Bldg: UNK TRAILER PARK - Key Map: 456C - Cross Streets:BARK",
         "ID:11-07-25416",
         "CALL:HEMORRHAGE/LACERATIO",
         "ADDR:12370 S San Circle Dr",
         "PLACE:UNK TRAILER PARK",
         "MAP:456C",
         "X:BARK");
 
     doTest("T6",
         "ID#:11-08-25733 - 55B04 - ELECTRICAL HAZARD - 16102 E Ih 10 - Apt: - Bldg: OLD RIVER CHURCH DAY - Key Map: 498G - Cross Streets:IH 10 EAST BAYOU RAMP/IH 10 SH -",
         "ID:11-08-25733",
         "CODE:55B04",
         "CALL:ELECTRICAL HAZARD",
         "ADDR:16102 E Ih 10",
         "PLACE:OLD RIVER CHURCH DAY",
         "MAP:498G",
         "X:IH 10 EAST BAYOU RAMP/IH 10 SH");
 
     doTest("T7",
         "ID#:11-08-25969 - 29D02m - 29D5 MOTOR VEHICLE A - 539 S Sheldon Rd - Apt: - Bldg: - Key Map: 498K - Cross Streets:JACINTO PORT BLVD/MARKET - Box #:2001",
         "ID:11-08-25969",
         "CODE:29D02m",
         "CALL:29D5 MOTOR VEHICLE A",
         "ADDR:539 S Sheldon Rd",
         "MAP:498K",
         "X:JACINTO PORT BLVD/MARKET",
         "BOX:2001");
 
     doTest("T8",
         "ID#:11-08-25936 - 29B04 - Traffic/Trans Incide - E BW 8 N / WOODFOREST BW 8 NB - Apt: - Bldg: - Key Map: 457Y - Cross Streets: - Box #:2005",
         "ID:11-08-25936",
         "CODE:29B04",
         "CALL:Traffic/Trans Incide",
         "ADDR:E BW 8 N & WOODFOREST BW 8 NB",
         "MADDR:E BW 8 N & WOODFOREST BW 8",
         "MAP:457Y",
         "BOX:2005");
 
 
     doTest("T9",
         "ID#:11-08-26006 - - Citizen Assist - 5500 E Bw 8 N - Apt: - Bldg: big tex storage - Key Map: 457Y - Cross Streets:BW 8 NB WALLISVILLE EXIT RAMP/ - Box #:2005",
         "ID:11-08-26006",
         "CALL:Citizen Assist",
         "ADDR:5500 E Bw 8 N",
         "PLACE:big tex storage",
         "MAP:457Y",
         "X:BW 8 NB WALLISVILLE EXIT RAMP/",
         "BOX:2005");
 
     doTest("T10",
         "ID#:11-08-28674 - Units:M151 - 32B01 - 32B1 UNKNOWN PROBLEM - 606 Commons Vista Dr - Apt: - Bldg: - Key Map: 298Q - Cross Streets:COM",
         "ID:11-08-28674",
         "UNIT:M151",
         "CODE:32B01",
         "CALL:32B1 UNKNOWN PROBLEM",
         "ADDR:606 Commons Vista Dr",
         "MAP:298Q",
         "X:COM");
 
     doTest("T11",
         "ID#:11-08-29430 - - FALLS - 17015 Faring Rd - Apt: - Bldg: - Key Map: 418X - Cro",
         "ID:11-08-29430",
         "CALL:FALLS",
         "ADDR:17015 Faring Rd",
         "MAP:418X");
 
     doTest("T12",
         "ID#:11-08-29436 - - ALARMS - 1714 Hidden Terrace Dr - Apt: - Bldg: - Key Map: 458S - Cross Streets:HIDDEN CREST DR/HIDDEN MANOR D - Box #:2005",
         "ID:11-08-29436",
         "CALL:ALARMS",
         "ADDR:1714 Hidden Terrace Dr",
         "MAP:458S",
         "X:HIDDEN CREST DR/HIDDEN MANOR D",
         "BOX:2005");
 
     doTest("T13",
         "ID#:11-08-26039 - - FUEL SPILL - 15880 Wallisville Rd - Apt: - Bldg: LEGEND OAKS - EAST - Key Map: 457V - Cross Streets:HIDDEN PARK DR/HYACINTH PATH W - Box #:2005",
         "ID:11-08-26039",
         "CALL:FUEL SPILL",
         "ADDR:15880 Wallisville Rd",
         "PLACE:LEGEND OAKS",
         "MAP:457V",
         "X:HIDDEN PARK DR/HYACINTH PATH W",
         "BOX:2005");
 
     doTest("T14",
         "ID#:11-08-26105 - 21B01 - HEMORRHAGE/LACERATIO - 11803 Greenmesa Dr - Apt: - Bld",
         "ID:11-08-26105",
         "CODE:21B01",
         "CALL:HEMORRHAGE/LACERATIO",
         "ADDR:11803 Greenmesa Dr");
 
     doTest("T15",
         "ID#:11-08-26080 - 13D01 - DIABETIC PROBLEMS - 10710 Green River Dr - Apt: - Bldg",
         "ID:11-08-26080",
         "CODE:13D01",
         "CALL:DIABETIC PROBLEMS",
         "ADDR:10710 Green River Dr");
 
     doTest("T16",
         "ID#:11-08-26159 - - Stab / GSW (B) - 919 Holbech - Apt: - Bldg: - Key Map: 497D",
         "ID:11-08-26159",
         "CALL:Stab / GSW (B)",
         "ADDR:919 Holbech",
         "MAP:497D");
 
     doTest("T17",
         "ID#:11-08-26160 - - HEART PROBLEMS - 9402 Abbotshall Ln - Apt: - Bldg: - Key Map: 456D - Cross Streets:ARBOR FIELD LN/STONEFIELD MANO - Box #:3304",
         "ID:11-08-26160",
         "CALL:HEART PROBLEMS",
         "ADDR:9402 Abbotshall Ln",
         "MAP:456D",
         "X:ARBOR FIELD LN/STONEFIELD MANO",
         "BOX:3304");
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         " 1 of 2\n" +
         "FRM:cadnoreply@proxy.hcec.com\n" +
         "MSG:ID#:12-06-21170 - 67D02 - 67D2 LG GRASS AND WO - US 90 / SHELDON US 90 RAMP - Apt: - Bldg: - Key Map:\n" +
         "(Con't) 2 of 2\n" +
         "458C - Cross Streets: - Box #:3304(End)",
 
         "ID:12-06-21170",
         "ADDR:US 90 & SHELDON US 90 RAMP",
         "MAP:458C",
         "CODE:67D02",
         "CALL:67D2 LG GRASS AND WO",
         "BOX:3304");
 
   }
   
   @Test
   public void testAddrChangeParser() {
 
     doTest("T1",
         "ID#:11-08-29410 - Address changed to:uvalde/kinsman - Cross Streets: - Key Map:NOT F",
         "ID:11-08-29410",
         "CALL:Address changed to:",
         "ADDR:uvalde & kinsman",
         "MAP:NOT F");
 
     doTest("T2",
         "ID#:11-08-26067 - Address changed to:620 SHELDON RD - Cross Streets:AVE C/RIDLON - Key Map:498B",
         "ID:11-08-26067",
         "CALL:Address changed to:",
         "ADDR:620 SHELDON RD",
         "X:AVE C/RIDLON",
         "MAP:498B");
 
     doTest("T3",
         "ID#:11-08-26080 - Address changed to:10710 Greens Crossing Blvd - Cross Streets",
         "CALL:Address changed to:",
         "ID:11-08-26080",
         "ADDR:10710 Greens Crossing Blvd");
 
   }
   
   @Test
   public void testAcive911A() {
 
     doTest("T1",
         "[] ID#:12-06-23555 - - 60B1 GAS LEAK/ODOR ( - 11000 E Canal Rd - Apt: - Bldg: - Key Map: 460J - Cross Streets:OAK AVE/HOLLY DR - Box #:1705\n",
         "ID:12-06-23555",
         "ADDR:11000 E Canal Rd",
         "X:OAK AVE/HOLLY DR",
         "MAP:460J",
         "CALL:60B1 GAS LEAK/ODOR (",
         "BOX:1705");
 
     doTest("T2",
         "[]  ID#:12-06-23555 - Address changed to:1100 E CANAL RD - Cross Streets:OAK AVE/HOLLY DR - Key Map:460J\n",
         "ID:12-06-23555",
         "ADDR:1100 E CANAL RD",
         "X:OAK AVE/HOLLY DR",
         "MAP:460J",
         "CALL:Address changed to:");
 
     doTest("T3",
         "[]  ID#:12-06-23588 - Address changed to:ORLEANS ST / COTTONTAIL DR - Cross Streets: - Key Map:420W\n",
         "ID:12-06-23588",
         "ADDR:ORLEANS ST & COTTONTAIL DR",
         "MAP:420W",
         "CALL:Address changed to:");
 
     doTest("T4",
         "[]  ID#:12-06-23588 - Address changed to:630 COTTONTAIL DR - Cross Streets:JOAN OF ARC ST/ORLEANS ST - Key Map:419Z\n",
         "ID:12-06-23588",
         "ADDR:630 COTTONTAIL DR",
         "X:JOAN OF ARC ST/ORLEANS ST",
         "MAP:419Z",
         "CALL:Address changed to:");
     
   }
   
   @Test
   public void testActive911B() {
 
     doTest("T1",
         "ID#:12-07-26086 - UNIT:GPE1 - Call Rcvd: 10:35:44 - DISP: 10:47:25 - ER: - OUT: - TO: - AT: - AIR: 10:57:50",
         "CALL:RUN REPORT",
         "PLACE:ID#:12-07-26086 - UNIT:GPE1 - Call Rcvd: 10:35:44 - DISP: 10:47:25 - ER: - OUT: - TO: - AT: - AIR: 10:57:50");
 
     doTest("T2",
         "ID#:12-07-26106 - UNIT:E11 - DISP: 12:30:22 - ER: - OUT: - AIR: 12:31:28",
         "CALL:RUN REPORT",
         "PLACE:ID#:12-07-26106 - UNIT:E11 - DISP: 12:30:22 - ER: - OUT: - AIR: 12:31:28");
 
     doTest("T3",
         "ID#:12-07-26109 - UNIT:E31 - Call Rcvd: 12:37:33 - DISP: 12:38:15 - ER: - OUT: - TO: - AT: - AIR: 12:44:01",
         "CALL:RUN REPORT",
         "PLACE:ID#:12-07-26109 - UNIT:E31 - Call Rcvd: 12:37:33 - DISP: 12:38:15 - ER: - OUT: - TO: - AT: - AIR: 12:44:01");
 
     doTest("T4",
         "ID#:12-07-26309 - UNIT:E31 - Call Rcvd: 22:36:45 - DISP: 22:37:39 - ER: 22:40:01 - OUT: 22:47:50 - TO: - AT: - AIR: 23:05:03",
         "CALL:RUN REPORT",
         "PLACE:ID#:12-07-26309 - UNIT:E31 - Call Rcvd: 22:36:45 - DISP: 22:37:39 - ER: 22:40:01 - OUT: 22:47:50 - TO: - AT: - AIR: 23:05:03");
 
     doTest("T5",
         "ID#:12-07-26353 - UNIT:SQ31 - Call Rcvd: 06:57:00 - DISP: 06:58:43 - ER: - OUT: 07:06:50 - TO: - AT: - AIR: 07:11:00",
         "CALL:RUN REPORT",
         "PLACE:ID#:12-07-26353 - UNIT:SQ31 - Call Rcvd: 06:57:00 - DISP: 06:58:43 - ER: - OUT: 07:06:50 - TO: - AT: - AIR: 07:11:00");
 
     doTest("T6",
         "ID#:12-07-26416 - UNIT:E21 - Call Rcvd: 16:40:37 - DISP: 16:41:39 - ER: 16:43:12 - OUT: 16:44:28 - TO: - AT: - AIR: 17:19:35",
         "CALL:RUN REPORT",
         "PLACE:ID#:12-07-26416 - UNIT:E21 - Call Rcvd: 16:40:37 - DISP: 16:41:39 - ER: 16:43:12 - OUT: 16:44:28 - TO: - AT: - AIR: 17:19:35");
     
   }
   
   @Test
   public void testActive911C() {
 
     doTest("T1",
         "ID#:12-07-26090 - - FIRE - PreAlert - 2409 Foley Rd - Apt: - Bldg: CROSBY FIRE STATION - Key Map: NOT F - Cross Streets:BRIDGEWOOD DR/SUMMIT TRAIL RD - Box #:8004",
         "ID:12-07-26090",
         "ADDR:2409 Foley Rd",
         "PLACE:CROSBY FIRE STATION",
         "X:BRIDGEWOOD DR/SUMMIT TRAIL RD",
         "MAP:NOT F",
         "CALL:FIRE - PreAlert",
         "BOX:8004");
 
     doTest("T2",
         " ID#:12-07-26090 - Address changed to:123 TEST STREET - Cross Streets:Dead End/Dead End - Key Map:496K",
         "ID:12-07-26090",
         "ADDR:123 TEST STREET",
         "X:Dead End/Dead End",
         "MAP:496K",
         "CALL:Address changed to:");
 
     doTest("T3",
         " ID#:12-07-26090 - Address changed to:5335 ALDINE BENDER RD - Cross Streets:CROSSWINDS DR/SURLES DR - Key Map:374Z",
         "ID:12-07-26090",
         "ADDR:5335 ALDINE BENDER RD",
         "X:CROSSWINDS DR/SURLES DR",
         "MAP:374Z",
         "CALL:Address changed to:");
 
     doTest("T4",
         "GPM1 - - MEDICAL - PreAlert - 5335 ALDINE BENDER RD - Apt: - Bldg: TIMBER RID - Key Map:374Z - Cross Streets:CROSSWINDS DR/SURLES DR - Box #:2601",
         "ADDR:5335 ALDINE BENDER RD",
         "PLACE:TIMBER RID",
         "X:CROSSWINDS DR/SURLES DR",
         "MAP:374Z",
         "UNIT:GPM1",
         "CALL:MEDICAL - PreAlert",
         "BOX:2601");
 
     doTest("T5",
         " ID#:12-07-26090 - Address changed to:123 S DIAMONDHEAD BLVD - Cross Streets:CORAL BAY ST/PORT OCALL ST - Key Map:419C",
         "ID:12-07-26090",
         "ADDR:123 S DIAMONDHEAD BLVD",
         "X:CORAL BAY ST/PORT OCALL ST",
         "MAP:419C",
         "CALL:Address changed to:");
 
     doTest("T6",
         "ID#:12-07-26090 - - MEDICAL - PreAlert - 123 S DIAMONDHEAD BLVD - Apt: - Bldg: - Key Map: 419C - Cross Streets:CORAL BAY ST/PORT OCALL ST - Box #:8005",
         "ID:12-07-26090",
         "ADDR:123 S DIAMONDHEAD BLVD",
         "X:CORAL BAY ST/PORT OCALL ST",
         "MAP:419C",
         "CALL:MEDICAL - PreAlert",
         "BOX:8005");
 
     doTest("T7",
         "SQ31 - 31D03 - 31D3 UNCONSCIOUS PAR - 433 Northpark Central Dr - Apt: - Bldg: ZOYTO - Key Map:332Y - Cross Streets:NORTHVIEW PARK DR/WOODHAM DR - Box #:1101",
         "ADDR:433 Northpark Central Dr",
         "PLACE:ZOYTO",
         "X:NORTHVIEW PARK DR/WOODHAM DR",
         "MAP:332Y",
         "UNIT:SQ31",
         "CODE:31D03",
         "CALL:31D3 UNCONSCIOUS PAR",
         "BOX:1101");
 
     doTest("T8",
         "E11 - 69D06T - 69D6 FIRE IN THE RE - 4723 Cicada Ln - Apt: - Bldg: - Key Map:414G - Cross Streets:BOREAS/CRIEFFE RD - Box #:2506",
         "ADDR:4723 Cicada Ln",
         "X:BOREAS/CRIEFFE RD",
         "MAP:414G",
         "UNIT:E11",
         "CODE:69D06T",
         "CALL:69D6 FIRE IN THE RE",
         "BOX:2506");
 
     doTest("T9",
         " ID#:12-07-26186 - Address changed to:4715 CICADA LN - Cross Streets:BOREAS/CRIEFFE RD - Key Map:414G",
         "ID:12-07-26186",
         "ADDR:4715 CICADA LN",
         "X:BOREAS/CRIEFFE RD",
         "MAP:414G",
         "CALL:Address changed to:");
 
     doTest("T10",
         "SQ31 - 06E01 - 6E1 RESPIRATORY - 503 Salma Ct - Apt: - Bldg: - Key Map:372H - Cross Streets:Dead End/DESERT MARIGOLD DR - Box #:1103",
         "ADDR:503 Salma Ct",
         "X:Dead End/DESERT MARIGOLD DR",
         "MAP:372H",
         "UNIT:SQ31",
         "CODE:06E01",
         "CALL:6E1 RESPIRATORY",
         "BOX:1103");
 
     doTest("T11",
         "SQ31 - - 12A1 SEIZURES - 15516 Martineau St - Apt: - Bldg: - Key Map:373R - Cross Streets:MARVELL DR/GREENBRIAR COLONY D - Box #:1106",
         "ADDR:15516 Martineau St",
         "X:MARVELL DR/GREENBRIAR COLONY D",
         "MAP:373R",
         "UNIT:SQ31",
         "CALL:12A1 SEIZURES",
         "BOX:1106");
 
     doTest("T12",
         "E11 - 12C01E - 12C1 SEIZURES - 15516 Martineau St - Apt: - Bldg: - Key Map:373R - Cross Streets:MARVELL DR/GREENBRIAR COLONY D - Box #:1106",
         "ADDR:15516 Martineau St",
         "X:MARVELL DR/GREENBRIAR COLONY D",
         "MAP:373R",
         "UNIT:E11",
         "CODE:12C01E",
         "CALL:12C1 SEIZURES",
         "BOX:1106");
 
     doTest("T13",
         "E11 - 69D01 - 69D1 STRUCTURE FIRE - 12345 Vickery St - Apt: - Bldg: OLESON ELE - Key Map:414G - Cross Streets:CHARRITON DR/ALDINE MAIL RD - Box #:2506",
         "ADDR:12345 Vickery St",
         "PLACE:OLESON ELE",
         "X:CHARRITON DR/ALDINE MAIL RD",
         "MAP:414G",
         "UNIT:E11",
         "CODE:69D01",
         "CALL:69D1 STRUCTURE FIRE",
         "BOX:2506");
 
     doTest("T14",
         "E31 - 29D02m - 29D2 MOTOR VEHICLE A - 14900 North Fwy - Apt: 203 - Bldg: VALUE PLAC - Key Map:372C - Cross Streets:Dead End/AIRTEX ENTR NB 45 - Box #:1103",
         "ADDR:14900 North Fwy",
         "APT:203",
         "PLACE:VALUE PLAC",
         "X:Dead End/AIRTEX ENTR NB 45",
         "MAP:372C",
         "UNIT:E31",
         "CODE:29D02m",
         "CALL:29D2 MOTOR VEHICLE A",
         "BOX:1103");
 
     doTest("T15",
         "E11 - - 69D6 FIRE IN THE RE - 823 Bandon Ln - Apt: - Bldg: - Key Map:372H - Cross Streets:PRAIRIE LARKSPUR DR/REMINGTON - Box #:1103",
         "ADDR:823 Bandon Ln",
         "X:PRAIRIE LARKSPUR DR/REMINGTON",
         "MAP:372H",
         "UNIT:E11",
         "CALL:69D6 FIRE IN THE RE",
         "BOX:1103");
 
     doTest("T16",
         "SQ31 - - 29A1 MOTOR VEHICLE A - Hardy Toll Rd / Rankin Hardy Toll R - Apt: - Bldg: - Key Map:373F - Cross Streets: - Box #:1104",
         "ADDR:Hardy Toll Rd & Rankin Hardy Toll R",
         "MAP:373F",
         "UNIT:SQ31",
         "CALL:29A1 MOTOR VEHICLE A",
         "BOX:1104");
 
     doTest("T17",
         " - - 29A1 MOTOR VEHICLE A - N Sam Houston Pkwy E / Hardy Bw 8 W - Apt: - Bldg: - Key Map:373S - Cross Streets: - Box #:4102",
         "ADDR:N Sam Houston Pkwy E & Hardy Bw 8 W",
         "MAP:373S",
         "CALL:29A1 MOTOR VEHICLE A",
         "BOX:4102");
 
     doTest("T18",
         "E11 - - 29A1 MOTOR VEHICLE A - N Sam Houston Pkwy E / Hardy Bw 8 W - Apt: - Bldg: - Key Map:373S - Cross Streets: - Box #:4102",
         "ADDR:N Sam Houston Pkwy E & Hardy Bw 8 W",
         "MAP:373S",
         "UNIT:E11",
         "CALL:29A1 MOTOR VEHICLE A",
         "BOX:4102");
 
     doTest("T19",
         "SQ31 - 29D02n - 29D2 MOTOR VEHICLE A - E Hardy Rd / N Sam Houston Pkwy E - Apt: - Bldg: THE PIT HA - Key Map:373T - Cross Streets: - Box #:1106",
         "ADDR:E Hardy Rd & N Sam Houston Pkwy E",
         "PLACE:THE PIT HA",
         "MAP:373T",
         "UNIT:SQ31",
         "CODE:29D02n",
         "CALL:29D2 MOTOR VEHICLE A",
         "BOX:1106");
 
     doTest("T20",
         "SQ31 - - BREATHING PROBLEMS - 17630 Wayforest Dr - Apt: 390 - Bldg: AGUSTA - Key Map:373J - Cross Streets:HARVEST TIME LN/RICHCREST DR - Box #:1106",
         "ADDR:17630 Wayforest Dr",
         "APT:390",
         "PLACE:AGUSTA",
         "X:HARVEST TIME LN/RICHCREST DR",
         "MAP:373J",
         "UNIT:SQ31",
         "CALL:BREATHING PROBLEMS",
         "BOX:1106");
 
     doTest("T21",
         "E11 - 06C01A - 6C1 RESPIRATORY - 17630 Wayforest Dr - Apt: 390 - Bldg: AGUSTA - Key Map:373J - Cross Streets:HARVEST TIME LN/RICHCREST DR - Box #:1106",
         "ADDR:17630 Wayforest Dr",
         "APT:390",
         "PLACE:AGUSTA",
         "X:HARVEST TIME LN/RICHCREST DR",
         "MAP:373J",
         "UNIT:E11",
         "CODE:06C01A",
         "CALL:6C1 RESPIRATORY",
         "BOX:1106");
 
     doTest("T22",
         "SQ31 - - 29A1 MOTOR VEHICLE A - E Richey Rd / Oil Center Blvd - Apt: - Bldg: - Key Map:333X - Cross Streets: - Box #:1102",
         "ADDR:E Richey Rd & Oil Center Blvd",
         "MAP:333X",
         "UNIT:SQ31",
         "CALL:29A1 MOTOR VEHICLE A",
         "BOX:1102");
 
     doTest("T23",
         "SQ31 - - 29A1 MOTOR VEHICLE A - 16600 E Hardy Rd - Apt: - Bldg: - Key Map:373K - Cross Streets:Dead End/RANKIN RD - Box #:1106",
         "ADDR:16600 E Hardy Rd",
         "X:Dead End/RANKIN RD",
         "MAP:373K",
         "UNIT:SQ31",
         "CALL:29A1 MOTOR VEHICLE A",
         "BOX:1106");
 
     doTest("T24",
         "E11 - - 69D6 FIRE IN THE RE - 14330 E Hardy Rd - Apt: - Bldg: MOGAS - Key Map:373X - Cross Streets:PINAFORE LN/BUSCHONG - Box #:1107",
         "ADDR:14330 E Hardy Rd",
         "PLACE:MOGAS",
         "X:PINAFORE LN/BUSCHONG",
         "MAP:373X",
         "UNIT:E11",
         "CALL:69D6 FIRE IN THE RE",
         "BOX:1107");
 
     doTest("T25",
         "E31 - 69D03 - 69D3 COMMERCIAL BUI - 14330 E Hardy Rd - Apt: - Bldg: MOGAS - Key Map:373X - Cross Streets:PINAFORE LN/BUSCHONG - Box #:1107",
         "ADDR:14330 E Hardy Rd",
         "PLACE:MOGAS",
         "X:PINAFORE LN/BUSCHONG",
         "MAP:373X",
         "UNIT:E31",
         "CODE:69D03",
         "CALL:69D3 COMMERCIAL BUI",
         "BOX:1107");
 
     doTest("T26",
         "SQ31 - 12A02E - 12A2 SEIZURES - 830 Vista Grove Cir - Apt: - Bldg: - Key Map:332Z - Cross Streets:GROVEDALE DR/GROVEDALE DR - Box #:1108",
         "ADDR:830 Vista Grove Cir",
         "X:GROVEDALE DR/GROVEDALE DR",
         "MAP:332Z",
         "UNIT:SQ31",
         "CODE:12A02E",
         "CALL:12A2 SEIZURES",
         "BOX:1108");
 
     doTest("T27",
         "E11 - - 29A1 MOTOR VEHICLE A - N Sam Houston Pkwy E / Aldine Westf - Apt: - Bldg: - Key Map:373U - Cross Streets: - Box #:1106",
         "ADDR:N Sam Houston Pkwy E & Aldine Westf",
         "MAP:373U",
         "UNIT:E11",
         "CALL:29A1 MOTOR VEHICLE A",
         "BOX:1106");
 
     doTest("T28",
         "E31 - - ODOR(STRANGE, UNKNOW - 507 Remington Lodge Ct - Apt: - Bldg: - Key Map:372D - Cross Streets:REMINGTON PARK DR/Dead End - Box #:1103",
         "ADDR:507 Remington Lodge Ct",
         "X:REMINGTON PARK DR/Dead End",
         "MAP:372D",
         "UNIT:E31",
         "CALL:ODOR(STRANGE, UNKNOW",
         "BOX:1103");
 
     doTest("T29",
         "E11 - 69D09 - 69D9 SHED ON FIRE - 14615 Aldine Westfield Rd - Apt: - Bldg: CHRISTIAN - Key Map:373Z - Cross Streets:BUSCHONG/ALDINE BENDER RD - Box #:1107",
         "ADDR:14615 Aldine Westfield Rd",
         "PLACE:CHRISTIAN",
         "X:BUSCHONG/ALDINE BENDER RD",
         "MAP:373Z",
         "UNIT:E11",
         "CODE:69D09",
         "CALL:69D9 SHED ON FIRE",
         "BOX:1107");
 
     doTest("T30",
         "SQ31 - 29D02m - 29D2 MOTOR VEHICLE A - 818 Richcrest Dr - Apt: - Bldg: GREENBRIAR - Key Map:373J - Cross Streets:CHISHOLM TRL/WAYFOREST DR - Box #:1106",
         "ADDR:818 Richcrest Dr",
         "PLACE:GREENBRIAR",
         "X:CHISHOLM TRL/WAYFOREST DR",
         "MAP:373J",
         "UNIT:SQ31",
         "CODE:29D02m",
         "CALL:29D2 MOTOR VEHICLE A",
         "BOX:1106");
 
     doTest("T31",
         "SQ31 - - 29A1 MOTOR VEHICLE A - North Fwy / Airtex - Apt: - Bldg: - Key Map:372C - Cross Streets: - Box #:1103",
         "ADDR:North Fwy & Airtex",
         "MAP:372C",
         "UNIT:SQ31",
         "CALL:29A1 MOTOR VEHICLE A",
         "BOX:1103");
 
     doTest("T32",
         "E31 - 52C03G - 52C3 ALARMS - 16110 North Fwy - Apt: - Bldg: CARMAX - Key Map:332U - Cross Streets:RICHEY EXIT NB 45/RICHEY ENTR - Box #:1101",
         "ADDR:16110 North Fwy",
         "PLACE:CARMAX",
         "X:RICHEY EXIT NB 45/RICHEY ENTR",
         "MAP:332U",
         "UNIT:E31",
         "CODE:52C03G",
         "CALL:52C3 ALARMS",
         "BOX:1101");
 
     doTest("T33",
         "E31 - - 53A1 CITIZEN ASSIST - 19423 Remington Prairie Dr - Apt: - Bldg: - Key Map:372G - Cross Streets:REMINGTON GREEN CT/REMINGTON B - Box #:1103",
         "ADDR:19423 Remington Prairie Dr",
         "X:REMINGTON GREEN CT/REMINGTON B",
         "MAP:372G",
         "UNIT:E31",
         "CALL:53A1 CITIZEN ASSIST",
         "BOX:1103");
 
     doTest("T34",
         "E11 - 69D06 - 69D6 FIRE IN THE RE - 2719 Brea Crest St - Apt: - Bldg: - Key Map:414N - Cross Streets:INNSBURY DR/SHADY LN - Box #:2502",
         "ADDR:2719 Brea Crest St",
         "X:INNSBURY DR/SHADY LN",
         "MAP:414N",
         "UNIT:E11",
         "CODE:69D06",
         "CALL:69D6 FIRE IN THE RE",
         "BOX:2502");
 
     doTest("T35",
         "E21 - - 52B1 ALARMS - 2700 W W Thorne Blvd - Apt: - Bldg: N HARRIS C - Key Map:333S - Cross Streets:E HARDY RD/OIL CENTER BLVD - Box #:1102",
         "ADDR:2700 W W Thorne Blvd",
         "PLACE:N HARRIS C",
         "X:E HARDY RD/OIL CENTER BLVD",
         "MAP:333S",
         "UNIT:E21",
         "CALL:52B1 ALARMS",
         "BOX:1102");
 
     doTest("T36",
         "SQ31 - 10D04 - 10D4 CARDIAC - 22226 Queenbury Hills Dr - Apt: - Bldg: - Key Map:332R - Cross Streets:LANCASTER LAKE DR/RED ALDER CI - Box #:1108",
         "ADDR:22226 Queenbury Hills Dr",
         "X:LANCASTER LAKE DR/RED ALDER CI",
         "MAP:332R",
         "UNIT:SQ31",
         "CODE:10D04",
         "CALL:10D4 CARDIAC",
         "BOX:1108");
 
     doTest("T37",
         "SQ31 - - BREATHING PROBLEMS - 15403 E Vantage Pkwy - Apt: - Bldg: THE WORD C - Key Map:373V - Cross Streets:N SAM HOUSTON PKWY E/WAVERLY D - Box #:1106",
         "ADDR:15403 E Vantage Pkwy",
         "PLACE:THE WORD C",
         "X:N SAM HOUSTON PKWY E/WAVERLY D",
         "MAP:373V",
         "UNIT:SQ31",
         "CALL:BREATHING PROBLEMS",
         "BOX:1106");
 
     doTest("T38",
         "E11 - 06D02 - 6D2 RESPIRATORY - 15403 E Vantage Pkwy - Apt: - Bldg: THE WORD C - Key Map:373V - Cross Streets:N SAM HOUSTON PKWY E/WAVERLY D - Box #:1106",
         "ADDR:15403 E Vantage Pkwy",
         "PLACE:THE WORD C",
         "X:N SAM HOUSTON PKWY E/WAVERLY D",
         "MAP:373V",
         "UNIT:E11",
         "CODE:06D02",
         "CALL:6D2 RESPIRATORY",
         "BOX:1106");
 
    doTest("T39",
        "E7 - 67B01 - 67B1 SMALL TRASH FIR - 11025 United St - Apt: - Bldg: - Key Map:414U - Cross Streets:ELL RD/LONE OAK RD - Box #:2505",
        "ADDR:11025 United St",
        "X:ELL RD/LONE OAK RD",
        "MAP:414U",
        "UNIT:E7",
        "CODE:67B01",
        "CALL:67B1 SMALL TRASH FIR",
        "BOX:2505");

   }
 
   public static void main(String[] args) {
     new TXHarrisCountyESD1BParserTest().generateTests("T1");
   }
 
 }
