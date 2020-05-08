 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYErieCountyRedAlertParserTest extends BaseParserTest {
   
   public NYErieCountyRedAlertParserTest() {
     setParser(new NYErieCountyRedAlertParser(), "ERIE COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "*2EMS: 60/M LONG FALL POSSIBLE ARM AND LEG FRACTURE ILS RESPONSE at 3425 OLD LAKEVIEW RD, Hamburg  c/s: ENGEL DR . . 17:31:57\n\n-- \nThis message has bee",
         "CALL:EMS",
         "INFO:60/M LONG FALL POSSIBLE ARM AND LEG FRACTURE ILS RESPONSE",
         "ADDR:3425 OLD LAKEVIEW RD",
         "CITY:Hamburg",
         "X:ENGEL DR",
         "TIME:17:31:57");
 
     doTest("T2",
         "*2EMS: Male fell has a elbow laceration Cold Response at 25 BUFFALO ST #420, Village of Hamburg  c/s: MAIN ST   O: CREEK BEND HGTS APTS . . 05:16:00\n\n-",
         "CALL:EMS",
         "INFO:Male fell has a elbow laceration Cold Response",
         "ADDR:25 BUFFALO ST",
         "APT:420",
         "CITY:Village of Hamburg",
         "X:MAIN ST",
         "PLACE:CREEK BEND HGTS APTS",
         "TIME:05:16:00");
 
     doTest("T3",
         "*2EMS: 23 male poss od uncx at 7219 LIEBLER RD, Boston  c/s: COLE RD . . 19:44:01\n\n-- \nThis message has been scanned for viruse",
         "CALL:EMS",
         "INFO:23 male poss od uncx",
         "ADDR:7219 LIEBLER RD",
         "CITY:Boston",
         "X:COLE RD",
         "TIME:19:44:01");
 
     doTest("T4",
         "*2EMS: 51 female dyspnea paramedic response at 8169 BOSTON STATE RD, Boston  c/s: MAY DR   O: PEOPLE INC . . 21:26:12\n\n-- \nThis",
         "CALL:EMS",
         "INFO:51 female dyspnea paramedic response",
         "ADDR:8169 BOSTON STATE RD",
         "CITY:Boston",
         "X:MAY DR",
         "PLACE:PEOPLE INC",
         "TIME:21:26:12");
 
     doTest("T5",
         "*2Misc Electrical Problem: TELEPHONE POLE ON FIRE NEAR ROADWAY at 6674 LIEBLER RD, Boston  c/s: BOSTON STATE RD . . 07:50:52\n\n--",
         "CALL:Misc Electrical Problem",
         "INFO:TELEPHONE POLE ON FIRE NEAR ROADWAY",
         "ADDR:6674 LIEBLER RD",
         "CITY:Boston",
         "X:BOSTON STATE RD",
         "TIME:07:50:52");
 
     doTest("T6",
         "*2EMS: 71 Y/F NOT ALERT  PARAMEDIC RESPONSE   NO MEMBERS ON DUTY  at 8799 ROCKWOOD RD, Boston  c/s: PA/BO LINE . . 13:54:46\n\n-- ",
         "CALL:EMS",
         "INFO:71 Y/F NOT ALERT PARAMEDIC RESPONSE NO MEMBERS ON DUTY",
         "ADDR:8799 ROCKWOOD RD",
         "CITY:Boston",
         "X:PA/BO LINE",
         "TIME:13:54:46");
 
     doTest("T7",
         "*2Alarm Activation: General fire alarm at 7207 WARD RD, Boston  c/s: COLE RD . . 17:41:51",
         "CALL:Alarm Activation",
         "INFO:General fire alarm",
         "ADDR:7207 WARD RD",
         "CITY:Boston",
         "X:COLE RD",
         "TIME:17:41:51");
 
     doTest("T8",
         "*2EMS: 15 MONTH CHILD - SEIZURES - BLS HOT at BOSTON STATE RD / WILDWOOD DR, Boston . . 14:57:05",
         "CALL:EMS",
         "INFO:15 MONTH CHILD - SEIZURES - BLS HOT",
         "ADDR:BOSTON STATE RD & WILDWOOD DR",
         "CITY:Boston",
         "TIME:14:57:05");
 
     doTest("T9",
         "*2Misc Rubbish/Brush, RUBBISH PROBLEM: LEAVES AND UNDERBRUSH ON FIRE IN WOODS WELL AWAY FROM HOUSE at 6754 OMPHALIUS RD, Boston  c/s: EDDY RD . . 13:47",
         "CALL:Misc Rubbish/Brush, RUBBISH PROBLEM",
         "INFO:LEAVES AND UNDERBRUSH ON FIRE IN WOODS WELL AWAY FROM HOUSE",
         "ADDR:6754 OMPHALIUS RD",
         "CITY:Boston",
         "X:EDDY RD",
         "TIME:13:47");
 
     doTest("T10",
         "*2EMS: 70F Uncon Medic at 6714 Hilcroft . . 00:39:43",
         "CALL:EMS",
         "INFO:70F Uncon Medic",
         "ADDR:6714 Hilcroft",
         "TIME:00:39:43");
 
     doTest("T11",
         "*2EMS Auto Accident: AUTO ACCIDENT WITH INJURY POSS ROLL OVER 20 & 29 TO THE HALL at LOWER EAST HILL RD / WOHLHUETER RD, Boston . . 23:16:18",
         "CALL:EMS Auto Accident",
         "INFO:AUTO ACCIDENT WITH INJURY POSS ROLL OVER 20 & 29 TO THE HALL",
         "ADDR:LOWER EAST HILL RD & WOHLHUETER RD",
         "CITY:Boston",
         "TIME:23:16:18");
 
     doTest("T12",
         "*2Alarm Activation: general fire alarm at evans nat bank at 7205 BOSTON STATE RD, Boston  c/s: ZIMMERMAN RD   O: EVANS NATIONAL BANK . . 16:11:51",
         "CALL:Alarm Activation",
         "INFO:general fire alarm at evans nat bank",
         "ADDR:7205 BOSTON STATE RD",
         "CITY:Boston",
         "X:ZIMMERMAN RD",
         "PLACE:EVANS NATIONAL BANK",
         "TIME:16:11:51");
 
     doTest("T13",
         "*2EMS: 69 Years Male Chest pain- medic response at 7898 BOSTON STATE RD #40, Boston  c/s: WILDWOOD DR   O: BOSTON HILLS MOBILE HOME PARK . . 19:09:24",
         "CALL:EMS",
         "INFO:69 Years Male Chest pain- medic response",
         "ADDR:7898 BOSTON STATE RD",
         "APT:40",
         "CITY:Boston",
         "X:WILDWOOD DR",
         "PLACE:BOSTON HILLS MOBILE HOME PARK",
         "TIME:19:09:24");
 
     doTest("T14",
         "*2EMS: M/chest pains at 8112 COLE RD, Boston  c/s: OMPHALIUS RD . . 20:05:16",
         "CALL:EMS",
         "INFO:M/chest pains",
         "ADDR:8112 COLE RD",
         "CITY:Boston",
         "X:OMPHALIUS RD",
         "TIME:20:05:16");
   }
   
   public static void main(String[] args) {
     new NYErieCountyRedAlertParserTest().generateTests("T7", "CALL INFO ADDR APT CITY X PLACE TIME");
   }
 
 }
