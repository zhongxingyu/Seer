 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYSteubenCountyParserTest extends BaseParserTest {
   
   public NYSteubenCountyParserTest() {
     setParser(new NYSteubenCountyParser(), "STEUBEN COUNTY", "NY");
   }
   
   @Test
   public void testProblems() {
   }
   
   @Test
   public void testAvocaParser() {
 
     doTest("T1",
         "messaging@iamresponding.com (Avoca FD) 64 S MAIN ST #APT OFFICE, AVOCA VILLAGE OF (STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME)6D4 Breathing Problems Clammy\nAVOCAFDA",
         "SRC:Avoca FD",
         "ADDR:64 S MAIN ST",
         "APT:OFFICE",
         "CITY:AVOCA",
         "X:STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME",
         "CODE:6D4",
         "CALL:Breathing Problems Clammy");
 
     doTest("T2",
         "messaging@iamresponding.com (Avoca FD) 4434 COUNTY ROUTE 6 , AVOCA TOWN OF (STATE ROUTE 415 / COUNTY ROUTE 70) 17B3 Falls Unknown status\nAVOCAFDAMB:2011:98",
         "SRC:Avoca FD",
         "ADDR:4434 COUNTY ROAD 6",
         "CITY:AVOCA",
         "X:STATE ROUTE 415 / COUNTY ROAD 70",
         "CODE:17B3",
         "CALL:Falls Unknown status");
 
     doTest("T3",
         "messaging@iamresponding.com (Avoca FD) 9439 SPAULDING RD , WHEELER TOWN OF ( / COUNTY ROUTE 7)WIRES EMERG\nAVOCFD:2011:51",
         "SRC:Avoca FD",
         "ADDR:9439 SPAULDING RD",
         "CITY:WHEELER",
         "X:COUNTY ROAD 7",
         "CALL:WIRES EMERG");
 
     doTest("T4",
         "messaging@iamresponding.com (Avoca FD) 8806 COUNTY ROUTE 105 , AVOCA TOWN OF ( / N MAIN ST) 9E1 Cardiac or Respiratory Arrest not breathing at all\nAVOCAFDAMB:2011:99",
         "SRC:Avoca FD",
         "ADDR:8806 COUNTY ROAD 105",
         "CITY:AVOCA",
         "X:N MAIN ST",
         "CODE:9E1",
         "CALL:ALERT",
         "INFO:Cardiac or Respiratory Arrest not breathing at all");
 
     doTest("T5",
         "messaging@iamresponding.com (Avoca FD) 7 CHARLESWORTH AV , AVOCA VILLAGE OF (EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS) 17B1 Falls Possibly dangerous body area\nAVOCAFDAMB",
         "SRC:Avoca FD",
         "ADDR:7 CHARLESWORTH AV",
         "MADDR:7 CHARLESWORTH AVE",
         "CITY:AVOCA",
         "X:EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS",
         "CODE:17B1",
         "CALL:Falls Possibly dangerous body area");
 
     doTest("T6",
         "messaging@iamresponding.com (Avoca FD) 4652 COUNTY ROUTE 70A , AVOCA TOWN OF (COUNTY ROUTE 69 / ) 26A1 Sick Person No priority symptoms (complaint conditions 2-11 not identified)\nA",
         "SRC:Avoca FD",
         "ADDR:4652 COUNTY ROAD 70A",
         "CITY:AVOCA",
         "X:COUNTY ROAD 69",
         "CODE:26A1",
         "CALL:ALERT",
         "INFO:Sick Person No priority symptoms (complaint conditions 2-11 not identified)");
 
     doTest("T7",
         "messaging@iamresponding.com (Avoca FD) 7 CHARLESWORTH AV , AVOCA VILLAGE OF (EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS) 17B1 Falls Possibly dangerous body area\nAVOCAFDAMB",
         "SRC:Avoca FD",
         "ADDR:7 CHARLESWORTH AV",
         "MADDR:7 CHARLESWORTH AVE",
         "CITY:AVOCA",
         "X:EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS",
         "CODE:17B1",
         "CALL:Falls Possibly dangerous body area");
 
     doTest("T8",
         "messaging@iamresponding.com (Avoca FD) 8012 SMITH POND RD , HOWARD TOWN OF (WESSELS HILL RD / BURDIN HILL RD) 12B1 Convulsions Effective Breathing not verified &lt;35\nAVOCAFDAMB:20",
         "SRC:Avoca FD",
         "ADDR:8012 SMITH POND RD",
         "CITY:HOWARD",
         "X:WESSELS HILL RD / BURDIN HILL RD",
         "CODE:12B1",
         "CALL:ALERT",
         "INFO:Convulsions Effective Breathing not verified <35");
 
     doTest("T9",
         "messaging@iamresponding.com (Avoca FD) 118 E STEUBEN ST , BATH VILLAGE OF (HALSEY ST / GANSEVOORT ST; Near: PRO ACTION) 32B3 Unknown Problem Unknown Status/Other Codes Not Applicab",
         "SRC:Avoca FD",
         "ADDR:118 E STEUBEN ST",
         "CITY:BATH",
         "X:HALSEY ST / GANSEVOORT ST; Near: PRO ACTION",
         "CODE:32B3",
         "CALL:ALERT",
         "INFO:Unknown Problem Unknown Status/Other Codes Not Applicab");
 
     doTest("T10",
         "messaging@iamresponding.com (Avoca FD) ::::RECALLED:::::: 118 E STEUBEN ST , BATH VILLAGE OF (HALSEY ST / GANSEVOORT ST; Near: PRO ACTION) 32B3 Unknown Problem Unknown Status/Other",
         "SRC:Avoca FD",
         "ADDR:118 E STEUBEN ST",
         "CITY:BATH",
         "X:HALSEY ST / GANSEVOORT ST; Near: PRO ACTION",
         "CODE:32B3",
         "CALL:RECALLED - Unknown Problem Unknown Status/Other");
 
     doTest("T11",
         "messaging@iamresponding.com (Avoca FD) 4230 SHARPS CORNERS RD , HOWARD TOWN OF 21A2 Hemorrhage/Lacerations minor hemorrhage\nAVOCAFDAMB:2011:105",
         "SRC:Avoca FD",
         "ADDR:4230 SHARPS CORNERS RD",
         "CITY:HOWARD",
         "CODE:21A2",
         "CALL:Hemorrhage/Lacerations minor hemorrhage");
 
     doTest("T12",
         "messaging@iamresponding.com (Avoca FD) 64 S MAIN ST , AVOCA VILLAGE OF (STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME) 17B1 Falls Possibly dangerous body area\nAVOCAFD",
         "SRC:Avoca FD",
         "ADDR:64 S MAIN ST",
         "CITY:AVOCA",
         "X:STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME",
         "CODE:17B1",
         "CALL:Falls Possibly dangerous body area");
 
     doTest("T13",
         "messaging@iamresponding.com (Avoca FD) /ARROW MART GAS STATION (S 99 MAIN ST AVOCA VILLAGE OF ) 10A1 Chest Pain Breathing normaly &lt; 35\nAVOCAFDAMB:2011:107",
         "SRC:Avoca FD",
         "PLACE:ARROW MART GAS STATION",
         "ADDR:S 99 MAIN ST",
         "CITY:AVOCA",
         "CODE:10A1",
         "CALL:ALERT",
         "CALL:Chest Pain Breathing normaly < 35");
 
     doTest("T14",
         "messaging@iamresponding.com (Avoca FD) 5172 LOUCKS POND RD , AVOCA TOWN OF (OLD AVOCA RD / DYER HILL RD) 10C1 Chest Pain Abnormal breathing\nAVOCAFDAMB:2011:108",
         "SRC:Avoca FD",
         "ADDR:5172 LOUCKS POND RD",
         "CITY:AVOCA",
         "X:OLD AVOCA RD / DYER HILL RD",
         "CODE:10C1",
         "CALL:Chest Pain Abnormal breathing");
 
     doTest("T15",
         "messaging@iamresponding.com (Avoca FD) 54 GRANT ST , AVOCA VILLAGE OF ( / SMITH ST) 21A1 Hemorrhage/Lacerations not dangerous hemorrhage\nAVOCAFDAMB:2011:109",
         "SRC:Avoca FD",
         "ADDR:54 GRANT ST",
         "CITY:AVOCA",
         "X:SMITH ST",
         "CODE:21A1",
         "CALL:ALERT",
         "INFO:Hemorrhage/Lacerations not dangerous hemorrhage");
 
     doTest("T16",
         "messaging@iamresponding.com (Avoca FD) WEEKLY PAGER TEST TEST\nAVOCFD:2011:52",
         "SRC:Avoca FD",
         "CALL:WEEKLY PAGER TEST TEST");
 
     doTest("T17",
         "messaging@iamresponding.com (Avoca FD) 60617 STATE ROUTE 415 , AVOCA TOWN OF Misc FIRE\nAVOCFD:2011:53",
         "SRC:Avoca FD",
         "ADDR:60617 STATE ROUTE 415",
         "MADDR:60617 STATE 415",
         "CITY:AVOCA",
         "CALL:Misc FIRE");
 
     doTest("T18",
         "messaging@iamresponding.com (Avoca FD) 64 S MAIN ST , AVOCA VILLAGE OF (STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME) 17A2 Falls non-recent &gt; 6 hrs (without prior",
         "SRC:Avoca FD",
         "ADDR:64 S MAIN ST",
         "CITY:AVOCA",
         "X:STATE ROUTE 415 / CARRINGTON ST; Near: COHOCTON VALLEY HOME",
         "CODE:17A2",
         "CALL:Falls non-recent > 6 hrs (without prior");
 
     doTest("T19",
         "messaging@iamresponding.com (Avoca FD) 5572 MICHIGAN HOLLOW RD , WHEELER TOWN OF (BEEKERMAN RD / ) 6C1 Breathing Problems Abnormal breathing\nAVOCAFDAMB:2011:112",
         "SRC:Avoca FD",
         "ADDR:5572 MICHIGAN HOLLOW RD",
         "CITY:WHEELER",
         "X:BEEKERMAN RD",
         "CODE:6C1",
         "CALL:Breathing Problems Abnormal breathing");
 
     doTest("T20",
         "messaging@iamresponding.com (Avoca FD) 7 GRANT ST , AVOCA VILLAGE OF (S MAPLE AV / S MAIN ST) 8O1 Carbon Monoxide Dector Alarm (w/out priority symptoms\nAVOCFD:2011:54",
         "SRC:Avoca FD",
         "ADDR:7 GRANT ST",
         "CITY:AVOCA",
         "X:S MAPLE AV / S MAIN ST",
         "CODE:8O1",
         "CALL:ALERT",
         "INFO:Carbon Monoxide Dector Alarm (w/out priority symptoms");
 
     doTest("T21",
         "messaging@iamresponding.com (Avoca FD) 7 CHARLESWORTH AV , AVOCA VILLAGE OF (EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS) AUTOMATIC ALARM Fire\nAVOCFD:2011:55",
         "SRC:Avoca FD",
         "ADDR:7 CHARLESWORTH AV",
         "MADDR:7 CHARLESWORTH AVE",
         "CITY:AVOCA",
         "X:EAST AV / CHASE ST; Near: PINECREST HOME FOR ADULTS",
         "CALL:AUTOMATIC ALARM Fire");
 
     doTest("T22",
         "messaging@iamresponding.com (Avoca FD) 61619 STATE ROUTE 415 #TRLR, AVOCA TOWN OF 31A1 Unconscious/Fainting Fainting episode(s) and alert &gt;35 (without cardiac history)\nAVOCAFDAM",
         "SRC:Avoca FD",
         "ADDR:61619 STATE ROUTE 415",
         "MADDR:61619 STATE 415",
         "APT:TRLR",
         "CITY:AVOCA",
         "CODE:31A1",
         "CALL:ALERT",
         "INFO:Unconscious/Fainting Fainting episode(s) and alert >35 (without cardiac history)");
 
     doTest("T23",
         "messaging@iamresponding.com (Avoca FD) 3628 COUNTY ROUTE 70A , HOWARD TOWN OF (HOUSE RD / SMITH POND RD S) STRUCTURE FIRE\nHOWAFD:2011:43",
         "SRC:Avoca FD",
         "ADDR:3628 COUNTY ROAD 70A",
         "CITY:HOWARD",
         "X:HOUSE RD / SMITH POND RD S",
         "CALL:STRUCTURE FIRE");
 
     doTest("T24",
         "messaging@iamresponding.com (Avoca FD) ::::RECALLED:::::: 3628 COUNTY ROUTE 70A , HOWARD TOWN OF (HOUSE RD / SMITH POND RD S) STRUCTURE FIRE\nHOWAFD:2011:43",
         "SRC:Avoca FD",
         "ADDR:3628 COUNTY ROAD 70A",
         "CITY:HOWARD",
         "X:HOUSE RD / SMITH POND RD S",
         "CALL:RECALLED - STRUCTURE FIRE");
 
     doTest("T25",
         "messaging@iamresponding.com (Avoca FD) 4550 COUNTY ROUTE 105 , AVOCA TOWN OF (WALLACE BACK RD / CHURCH ST) 31E1 Unconscious/Fainting Ineffective breathing\nAVOCAFDAMB:2011:115",
         "SRC:Avoca FD",
         "ADDR:4550 COUNTY ROAD 105",
         "CITY:AVOCA",
         "X:WALLACE BACK RD / CHURCH ST",
         "CODE:31E1",
         "CALL:ALERT",
         "INFO:Unconscious/Fainting Ineffective breathing");
 
     doTest("T26",
         "messaging@iamresponding.com (Avoca FD) /BOMBARDIER ( 7940 STATE ROUTE 415 AVOCA TOWN OF ) Misc FIRE\nAVOCFD:2011:56",
         "SRC:Avoca FD",
         "PLACE:BOMBARDIER",
         "ADDR:7940 STATE ROUTE 415",
         "MADDR:7940 STATE 415",
         "CITY:AVOCA",
         "CALL:Misc FIRE");
 
     doTest("T27",
         "messaging@iamresponding.com (Avoca FD) 9068 FAIRBROTHER RD , AVOCA TOWN OF (VAN AUCKER RD / WATERBURY HILL RD) 29B4 Traffic Accident Unknown Status/Other Codes Not Applicable\nAVOCA",
         "SRC:Avoca FD",
         "ADDR:9068 FAIRBROTHER RD",
         "CITY:AVOCA",
         "X:VAN AUCKER RD / WATERBURY HILL RD",
         "CODE:29B4",
         "CALL:ALERT",
         "INFO:Traffic Accident Unknown Status/Other Codes Not Applicable");
 
     doTest("T28",
         "messaging@iamresponding.com (Avoca FD) 7 ALEXANDER ST , AVOCA VILLAGE OF (N MAPLE AV / N MAIN ST) 25B6 Psychiatric/Suicide attempt Unknown Status/Other Codes Not Applicable\nAVOCAFD",
         "SRC:Avoca FD",
         "ADDR:7 ALEXANDER ST",
         "CITY:AVOCA",
         "X:N MAPLE AV / N MAIN ST",
         "CODE:25B6",
         "CALL:ALERT",
         "INFO:Psychiatric/Suicide attempt Unknown Status/Other Codes Not Applicable");
 
     doTest("T29",
         "messaging@iamresponding.com (Avoca FD) 9374 KHORKS AV , AVOCA TOWN OF ( / WALLACE BACK RD) 17A1 Falls not dangerous body area\nAVOCAFDAMB:2011:119",
         "SRC:Avoca FD",
         "ADDR:9374 KHORKS AV",
         "MADDR:9374 KHORKS AVE",
         "CITY:AVOCA",
         "X:WALLACE BACK RD",
         "CODE:17A1",
         "CALL:Falls not dangerous body area");
 
     doTest("T30",
         "messaging@iamresponding.com (Avoca FD) 8980 REEVES RD , WHEELER TOWN OF ( / HEMLOCK RD) 25B3 Psychiatric/Suicide attempt Threatening Suicide\nAVOCAFDAMB:2011:120",
         "SRC:Avoca FD",
         "ADDR:8980 REEVES RD",
         "CITY:WHEELER",
         "X:HEMLOCK RD",
         "CODE:25B3",
         "CALL:ALERT",
         "INFO:Psychiatric/Suicide attempt Threatening Suicide");
 
     doTest("T31",
         "messaging@iamresponding.com (Avoca FD) 4252 COUNTY ROUTE 70A , HOWARD TOWN OF (HARRIS HILL RD / MILL RD) 26A 2-11 Sick Person Non-Priority Complaints\nAVOCAFDAMB:2011:121",
         "SRC:Avoca FD",
         "ADDR:4252 COUNTY ROAD 70A",
         "CITY:HOWARD",
         "X:HARRIS HILL RD / MILL RD",
         "CALL:ALERT",
         "INFO:26A 2-11 Sick Person Non-Priority Complaints");
 
     doTest("T32",
         "messaging@iamresponding.com (Avoca FD) 5279 TRACY RD , AVOCA TOWN OF ( / COUNTY ROUTE 8) 1A1 Abdominal Pain\nAVOCAFDAMB:2011:122",
         "SRC:Avoca FD",
         "ADDR:5279 TRACY RD",
         "CITY:AVOCA",
         "X:COUNTY ROAD 8",
         "CODE:1A1",
         "CALL:Abdominal Pain");
 
     doTest("T33",
         "messaging@iamresponding.com (Avoca FD) 8764 CLARK RD , AVOCA TOWN OF ( / CROSS RD) 28C02 Stroke (CVA)  Abnormal breathing\nAVOCAFDAMB:2011:123",
         "SRC:Avoca FD",
         "ADDR:8764 CLARK RD",
         "CITY:AVOCA",
         "X:CROSS RD",
         "CODE:28C02",
         "CALL:Stroke (CVA)  Abnormal breathing");
 
     doTest("T34",
         "messaging@iamresponding.com (Avoca FD) 8980 REEVES RD , WHEELER TOWN OF ( / HEMLOCK RD) 25A1 Psychiatric/Suicide attempt Non-suicidal and alert\nAVOCAFDAMB:2011:124",
         "SRC:Avoca FD",
         "ADDR:8980 REEVES RD",
         "CITY:WHEELER",
         "X:HEMLOCK RD",
         "CODE:25A1",
         "CALL:ALERT",
         "INFO:Psychiatric/Suicide attempt Non-suicidal and alert");
 
     doTest("T35",
         "messaging@iamresponding.com (Avoca FD) /MILE 139 ( I86 BETWEEN EXIT 35 + ) 29B1 Traffic Accidents  Injuries\nAVOCAFDAMB:2011:125",
         "SRC:Avoca FD",
         "PLACE:MILE 139",
         "ADDR:I-86",
         "MADDR:I-86 & BETWEEN EXIT 35 +",
         "X:BETWEEN EXIT 35 +",
         "CODE:29B1",
         "CALL:Traffic Accidents  Injuries");
 
     doTest("T36",
         "messaging@iamresponding.com (Avoca FD) ::::RECALLED:::::: /MILE 139 ( I86 BETWEEN EXIT 35 + ) 29B1 Traffic Accidents  Injuries\nAVOCAFDAMB:2011:125",
         "SRC:Avoca FD",
         "PLACE:MILE 139",
         "ADDR:I-86",
         "MADDR:I-86 & BETWEEN EXIT 35 +",
         "X:BETWEEN EXIT 35 +",
         "CODE:29B1",
         "CALL:RECALLED - Traffic Accidents  Injuries");
 
     doTest("T37",
         "messaging@iamresponding.com (Avoca FD) 61619 STATE ROUTE 415 #TRLR, AVOCA TOWN OF ( / WENTWORTH RD) 19D4 Heart Problems Clammy\nAVOCAFDAMB:2011:126",
         "SRC:Avoca FD",
         "ADDR:61619 STATE ROUTE 415",
         "MADDR:61619 STATE 415",
         "APT:TRLR",
         "CITY:AVOCA",
         "X:WENTWORTH RD",
         "CODE:19D4",
         "CALL:Heart Problems Clammy");
 
     doTest("T38",
         "messaging@iamresponding.com (Avoca FD) 226 W WASHINGTON ST #APT 201, BATH VILLAGE OF (HUBBELL ST / FREEMAN TERR) 10D2 Chest Pain Difficulty Speaking Between Breaths\nBATHAMB:2011:77",
         "SRC:Avoca FD",
         "ADDR:226 W WASHINGTON ST",
         "APT:201",
         "CITY:BATH",
         "X:HUBBELL ST / FREEMAN TERR",
         "CODE:10D2",
         "CALL:ALERT",
         "INFO:Chest Pain Difficulty Speaking Between Breaths");
 
     doTest("T39",
         "messaging@iamresponding.com (Avoca FD) 8039 SMITH POND RD , HOWARD TOWN OF (MACKEY RD / WESSELS HILL RD) Brush Fire\nAVOCAFDAMB:2011:127",
         "SRC:Avoca FD",
         "ADDR:8039 SMITH POND RD",
         "CITY:HOWARD",
         "X:MACKEY RD / WESSELS HILL RD",
         "CALL:Brush Fire");
 
     doTest("T40",
         "messaging@iamresponding.com (Avoca FD) THIS IS A TEST MESSAGE FOR I AM RESPONDING TEST\nAVOCFD:2011:57",
         "SRC:Avoca FD",
         "CALL:ALERT",
         "INFO:THIS IS A TEST MESSAGE FOR I AM RESPONDING TEST");
 
   }
   
   @Test
   public void testCohoctonParser() {
 
     doTest("T1",
         "(Cohocton FD) /CARPET RACK ( 8 MAPLE AV COHOCTON VILLAGE OF )\n12D1 Convulsions Not breathing\nCOHOFAMB:2011:76",
         "SRC:Cohocton FD",
         "PLACE:CARPET RACK",
         "ADDR:8 MAPLE AV",
         "MADDR:8 MAPLE AVE",
         "CITY:COHOCTON",
         "CODE:12D1",
         "CALL:Convulsions Not breathing");
 
     doTest("T2",
         "(Cohocton FD) 86 MAPLE AV #FLR 1, COHOCTON VILLAGE OF (STATE ROUTE 962D / SHULTS AV)\n26A 2-11 Sick Person Non-Priority Complaints\nCOHOFAMB:2011:70",
         "SRC:Cohocton FD",
         "ADDR:86 MAPLE AV #FLR 1",
         "MADDR:86 MAPLE AVE #FLR 1",
         "CITY:COHOCTON",
         "X:STATE ROUTE 962D / SHULTS AV",
         "CALL:ALERT",
         "INFO:26A 2-11 Sick Person Non-Priority Complaints");
 
     doTest("T3",
         "(Cohocton FD) 10298 COUNTY ROUTE 9 , COHOCTON TOWN OF (EVELAND RD / )\n25B6 Psychiatric/Suicide attempt Unknown Status/Other Codes Not Applicable\nCOHOFAM",
         "SRC:Cohocton FD",
         "ADDR:10298 COUNTY ROAD 9",
         "CITY:COHOCTON",
         "X:EVELAND RD",
         "CODE:25B6",
         "CALL:ALERT",
         "INFO:Psychiatric/Suicide attempt Unknown Status/Other Codes Not Applicable");
 
     doTest("T4",
         "(Cohocton FD) 24 N DANSVILLE ST #APT 110, COHOCTON VILLAGE OF\n21D3 Hemorrhage/Lacerations Dangerous Henorrhage\nCOHOFAMB:2011:72",
         "SRC:Cohocton FD",
         "ADDR:24 N DANSVILLE ST",
         "APT:110",
         "CITY:COHOCTON",
         "CODE:21D3",
         "CALL:ALERT",
         "INFO:Hemorrhage/Lacerations Dangerous Henorrhage");
 
     doTest("T5",
         "(Cohocton FD) 6 ERIE ST , COHOCTON TOWN OF (MAIN ST / BOGGS ST)\n12D3 Convulsions Agonal/Ineffective Breathing\nATLANFDAMB:2011:33",
         "SRC:Cohocton FD",
         "ADDR:6 ERIE ST",
         "CITY:COHOCTON",
         "X:MAIN ST / BOGGS ST",
         "CODE:12D3",
         "CALL:Convulsions Agonal/Ineffective Breathing");
 
     doTest("T6",
         "(Cohocton FD) 5 ROSENCRANS ST , COHOCTON VILLAGE OF (LARROWE ST / MAPLE AV)\n31D2 Unconscious/Fainting Unconscious - Effective Breathing\nCOHOFAMB:2011:75",
         "SRC:Cohocton FD",
         "ADDR:5 ROSENCRANS ST",
         "CITY:COHOCTON",
         "X:LARROWE ST / MAPLE AV",
         "CODE:31D2",
         "CALL:ALERT",
         "INFO:Unconscious/Fainting Unconscious - Effective Breathing");
 
   }
   
   @Test
   public void testSCorningParser() {
 
     doTest("T1",
         "(S. Corning FD) 1567 MARTIN HILL RD , CATON TOWN OF (W CATON RD / PINE BREEZE LN)\nVEHICLE FIRE\nCATOFD:2011:72",
         "SRC:S. Corning FD",
         "ADDR:1567 MARTIN HILL RD",
         "CITY:CATON",
         "X:W CATON RD / PINE BREEZE LN",
         "CALL:VEHICLE FIRE");
   }
   
   @Test
   public void testBTFDParser() {
 
     doTest("T1",
         "/ZEMAKS GREENHOUSE ( 6714 STATE ROUTE 415 BATH TOWN OF ) Brush Fire BTFD:2011:272",
         "SRC:BTFD",
         "PLACE:ZEMAKS GREENHOUSE",
         "ADDR:6714 STATE ROUTE 415",
         "MADDR:6714 STATE 415",
         "CITY:BATH",
         "CALL:Brush Fire");
 
     doTest("T2",
         "/MILE 154 ( I86 BETWEEN EXIT 39 + ) 29D2 Traffic Accidents High mechanism BTFD:2011:177",
         "SRC:BTFD",
         "PLACE:MILE 154",
         "ADDR:I-86",
         "MADDR:I-86 & BETWEEN EXIT 39 +",
         "X:BETWEEN EXIT 39 +",
         "CODE:29D2",
         "CALL:Traffic Accidents High mechanism");
 
     doTest("T3",
         "--{Don't Respond: Controlled Burn}-- 7630 COUNTY ROUTE 16 , BATH TOWN OF (QUINN RD / FREEMAN HOLLOW RD) Controlled Burn",
         "ADDR:7630 COUNTY ROAD 16",
         "CITY:BATH",
         "X:QUINN RD / FREEMAN HOLLOW RD",
         "CALL:ALERT",
         "INFO:Don't Respond: Controlled Burn / Controlled Burn");
 
     doTest("T4",
         "105 GENEVA ST , BATH VILLAGE OF (STATE ROUTE 54 / GRATTON DR; Near: LAKEVIEW APARTMENTS) AUTOMATIC ALARM Fire BTFD:2011:263",
         "SRC:BTFD",
         "ADDR:105 GENEVA ST",
         "CITY:BATH",
         "X:STATE ROUTE 54 / GRATTON DR; Near: LAKEVIEW APARTMENTS",
         "CALL:AUTOMATIC ALARM Fire");
 
     doTest("T5",
         "[2ndCall] 25 MAINE ST , BATH VILLAGE OF (ROBIE ST / HAVERLING ST) 1D1 Abdominal Pain Not alert BATHAMB:2011:1393",
         "SRC:BATHAMB",
         "ADDR:25 MAINE ST",
         "CITY:BATH",
         "X:ROBIE ST / HAVERLING ST",
         "CODE:1D1",
         "CALL:2ndCall - Abdominal Pain Not alert");
 
     doTest("T6",
         "[2ndCall] 3 CLUB VIEW DR , BATH VILLAGE OF ( / MAY ST) 28C06 Stroke (CVA) Sudden loss of balance or coordination BATHAMB:2011:1392",
         "SRC:BATHAMB",
         "ADDR:3 CLUB VIEW DR",
         "CITY:BATH",
         "X:MAY ST",
         "CODE:28C06",
         "CALL:2ndCall - ALERT",
         "INFO:Stroke (CVA) Sudden loss of balance or coordination");
    
   }
   
   @Test
   public void testWaylandParser() {
    doTest("T1",
         "Subject:Wayland FD\n1790 STATE ROUTE 63 , WAYLAND TOWN OF (GROSS HILL RD / RESERVOIR RD)\n26A1 Sick Person No priority symptoms (com",
         "SRC:Wayland FD",
         "ADDR:1790 STATE ROUTE 63",
         "MADDR:1790 STATE 63",
         "CITY:WAYLAND",
         "X:GROSS HILL RD / RESERVOIR RD",
         "CODE:26A1",
         "CALL:Sick Person No priority symptoms (com");

    doTest("T2",
        "Subject:Wayland FD\nCOUNTY ROUTE 92\\HENRY DRUM RD , WAYLAND TOWN OF\n29B1 Traffic Accidents  Injuries\nWAYLAFDAMB:2011:504\n\n",
        "SRC:Wayland FD",
        "ADDR:COUNTY ROAD 92 & HENRY DRUM RD",
        "CITY:WAYLAND",
        "CODE:29B1",
        "CALL:Traffic Accidents  Injuries");
   
   }
   
   public static void main(String[] args) {
     new NYSteubenCountyParserTest().generateTests("T5", "SRC PLACE ADDR APT CITY X CODE CALL INFO");
   }
 
 }
