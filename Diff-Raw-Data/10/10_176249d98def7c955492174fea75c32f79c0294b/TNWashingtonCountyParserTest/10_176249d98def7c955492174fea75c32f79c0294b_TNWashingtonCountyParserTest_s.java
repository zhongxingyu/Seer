 package net.anei.cadpage.parsers.TN;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class TNWashingtonCountyParserTest extends BaseParserTest {
   
   public TNWashingtonCountyParserTest() {
     setParser(new TNWashingtonCountyParser(), "WASHINGTON COUNTY", "TN");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "prvs=182efbc0f=JCFDTEXT@johnsoncitytn.org House/Residential Fire E7,E4,L44,E3,MP29\nALLISON LN/HELEN CT\nALLISON PLACE SD\nMap 53C 16:50:40 115143\nThink green: Only print this e-mail",
         "CALL:House/Residential Fire",
         "UNIT:E7,E4,L44,E3,MP29",
         "ADDR:ALLISON LN & HELEN CT",
         "PLACE:ALLISON PLACE SD",
         "MAP:53C",
         "ID:115143");
 
     doTest("T2",
         "prvs=1838ca3f5=JCFDTEXT@johnsoncitytn.org GRASS/BRUSH FIRE E4\n208 GARDEN DR\nGRACE TEMPLE CHURCH\nMap 46C 19:57:36 115181",
         "CALL:GRASS/BRUSH FIRE",
         "UNIT:E4",
         "ADDR:208 GARDEN DR",
         "PLACE:GRACE TEMPLE CHURCH",
         "MAP:46C",
         "ID:115181");
 
     doTest("T3",
         "prvs=184df15ee=JCFDTEXT@johnsoncitytn.org School Fire Alarm\nE4,L44,E3,MP29,E5\n1509 JOHN EXUM PKWY\nSCIENCE HILL HIGH SCHOOL\nMap 46A 02:23:08 115185",
         "CALL:School Fire Alarm",
         "UNIT:E4,L44,E3,MP29,E5",
         "ADDR:1509 JOHN EXUM PKWY",
         "PLACE:SCIENCE HILL HIGH SCHOOL",
         "MAP:46A",
         "ID:115185");
 
     doTest("T4",
         "prvs=184df15ee=JCFDTEXT@johnsoncitytn.org Diabetic Problems-CHARLIE M13,E3\n212 W POPLAR ST #APT1\nMap 54B 00:12:17 11111569",
         "CALL:Diabetic Problems",
         "PRI:C",
         "UNIT:M13,E3",
         "ADDR:212 W POPLAR ST",
         "APT:APT1",
         "MAP:54B",
         "ID:11111569");
 
     doTest("T5",
         "prvs=184df15ee=JCFDTEXT@johnsoncitytn.org Unconscious/Fainting-DELTA M7,E5\n1811 W LAKEVIEW DR #B9\nSUMMERCHASE APARTMENTS\nMap 38B 23:05:56 11111542",
         "CALL:Unconscious/Fainting",
         "PRI:D",
         "UNIT:M7,E5",
         "ADDR:1811 W LAKEVIEW DR",
         "APT:B9",
         "PLACE:SUMMERCHASE APARTMENTS",
         "MAP:38B",
         "ID:11111542");
 
     doTest("T6",
         "prvs=1838ca3f5=JCFDTEXT@johnsoncitytn.org GRASS/BRUSH FIRE E4\n208 GARDEN DR\nGRACE TEMPLE CHURCH\nMap 46C 19:57:36 115181",
         "CALL:GRASS/BRUSH FIRE",
         "UNIT:E4",
         "ADDR:208 GARDEN DR",
         "PLACE:GRACE TEMPLE CHURCH",
         "MAP:46C",
         "ID:115181");
 
     doTest("T7",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Heart Problems/AICD-DELTA R2,E4\n1806 LAMONS LN #101\nHARRISON VILLAGE APARTMENTS\nMap 54A 12:39:23 11113129",
         "CALL:Heart Problems/AICD",
         "PRI:D",
         "UNIT:R2,E4",
         "ADDR:1806 LAMONS LN",
         "APT:101",
         "PLACE:HARRISON VILLAGE APARTMENTS",
         "MAP:54A",
         "ID:11113129");
 
     doTest("T8",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Nursing Home Fire Alarm\nE5,L45,E9,E6,MP29,L44\n406 E MOUNTAINVIEW RD\nBROADMORE FACILITY\nMap 29C 14:01:23 115275",
         "CALL:Nursing Home Fire Alarm",
         "UNIT:E5,L45,E9,E6,MP29,L44",
         "ADDR:406 E MOUNTAINVIEW RD",
         "PLACE:BROADMORE FACILITY",
         "MAP:29C",
         "ID:115275");
 
     doTest("T9",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Motor Vehicle Crash - Injury\nE2,E3,MP29\nI26W/OKOLONA EXIT\nMap 63C 14:16:38 115276",
         "CALL:Motor Vehicle Crash - Injury",
         "UNIT:E2,E3,MP29",
         "ADDR:I26W & OKOLONA EXIT",
         "MAP:63C",
         "ID:115276");
 
     doTest("T10",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Unconscious/Fainting-DELTA\nM2,E7,R2\n2215 N GREENWOOD DR\nMap 54D 14:20:38 11113195",
         "CALL:Unconscious/Fainting",
         "PRI:D",
         "UNIT:M2,E7,R2",
         "ADDR:2215 N GREENWOOD DR",
         "MAP:54D",
         "ID:11113195");
 
     doTest("T11",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Stroke(CVA)-CHARLIE M6,E6,R3\n3711 N ROAN ST #E28\nNORTHPLACE APARTMENTS\nMap 29C 16:31:15 11113269",
         "CALL:Stroke(CVA)",
         "PRI:C",
         "UNIT:M6,E6,R3",
         "ADDR:3711 N ROAN ST",
         "APT:E28",
         "PLACE:NORTHPLACE APARTMENTS",
         "MAP:29C",
         "ID:11113269");
 
     doTest("T12",
         "prvs=1887ea331=JCFDTEXT@johnsoncitytn.org Sick Person-CHARLIE M2,R2,E4\n1208 BAIR VETTE AV\nMap 45C 53B 21:42:56 11113379",
         "CALL:Sick Person",
         "PRI:C",
         "UNIT:M2,R2,E4",
         "ADDR:1208 BAIR VETTE AV",
         "MAP:45C 53B",
         "ID:11113379");
 
     doTest("T13",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Chest Pain(Non-Traumatic)-DELTA\nM2,R2,E4\n1518 SYLVAN DR\nMap 45C 19:51:57 11113336",
         "CALL:Chest Pain(Non-Traumatic)",
         "PRI:D",
         "UNIT:M2,R2,E4",
         "ADDR:1518 SYLVAN DR",
         "MAP:45C",
         "ID:11113336");
 
     doTest("T14",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Falls-ALPHA E2\n2802 PLYMOUTH RD\nRAINBOW HOMES\nMap 55D 19:20:41 11113321",
         "CALL:Falls",
         "PRI:A",
         "UNIT:E2",
         "ADDR:2802 PLYMOUTH RD",
         "PLACE:RAINBOW HOMES",
         "MAP:55D",
         "ID:11113321");
 
     doTest("T15",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Heart Problems/AICD-DELTA M6,E9,R3\n409 SCENIC OAK DR\nMap 21D 19:07:03 11113313",
         "CALL:Heart Problems/AICD",
         "PRI:D",
         "UNIT:M6,E9,R3",
         "ADDR:409 SCENIC OAK DR",
         "MAP:21D",
         "ID:11113313");
 
     doTest("T16",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Convulsions/Seizures-DELTA\nM2,R2,E4\n301 ROYAL CR #79\nCLARK MANOR APARTMENTS\nMap 54A 17:30:11 11113287\nThink green: Only print this e-mail",
         "CALL:Convulsions/Seizures",
         "PRI:D",
         "UNIT:M2,R2,E4",
         "ADDR:301 ROYAL CR",
         "APT:79",
         "PLACE:CLARK MANOR APARTMENTS",
         "MAP:54A",
         "ID:11113287");
 
     doTest("T17",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Convulsions/Seizures-CHARLIE\nM1,E2,R1\nS ROAN ST/UNIVERSITY PKWY\nMap 54B 15:06:18 11113223",
         "CALL:Convulsions/Seizures",
         "PRI:C",
         "UNIT:M1,E2,R1",
         "ADDR:S ROAN ST & UNIVERSITY PKWY",
         "MAP:54B",
         "ID:11113223");
 
     doTest("T18",
         "prvs=187913dbd=JCFDTEXT@johnsoncitytn.org Motor Vehicle Crash - Injury\nE2,E3,MP29\nI26W/OKOLONA EXIT\nMap 63C 14:16:38 115276",
         "CALL:Motor Vehicle Crash - Injury",
         "UNIT:E2,E3,MP29",
         "ADDR:I26W & OKOLONA EXIT",
         "MAP:63C",
         "ID:115276");
 
     doTest("T19",
         "prvs=256783305=JCFDTEXT@johnsoncitytn.org Sick Person-ALPHA E4\n840 W MARKET ST\nCVS PHARMACY\nX-STR= CLARK ST\nKNOB CREEK RD\nMap 54A 20:51:54 11149314\nThink green: Only print this e-",
         "CALL:Sick Person",
         "PRI:A",
         "UNIT:E4",
         "ID:11149314",
         "ADDR:840 W MARKET ST",
         "PLACE:CVS PHARMACY",
         "X:CLARK ST & KNOB CREEK RD",
         "MAP:54A");
 
     doTest("T20",
         "prvs=2677ee746=JCFDTEXT@johnsoncitytn.org Convulsions/Seizures-DELTAM2,E2,R2\n1209 COLLEGE HEIGHTS DR\nX-STR= DEAD END\nUNIVERSITY PL\n;EAST OF SOUTHWEST AV\nMap 54C 17:18:07 11155033",
         "CALL:Convulsions/Seizures",
         "PRI:D",
         "UNIT:M2,E2,R2",
         "ID:11155033",
         "ADDR:1209 COLLEGE HEIGHTS DR",
         "X:DEAD END & UNIVERSITY PL & ;EAST OF SOUTHWEST AV",
         "MAP:54C");
 
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "Breathing Problems-DELTA M1,E3 \n300 W WALNUT ST #2 \nSHAMROCK \nMap 54b 14:59:59 11111863 \nThink green: Only print this e-mail and any attachment",
         "CALL:Breathing Problems",
         "PRI:D",
         "UNIT:M1,E3",
         "ADDR:300 W WALNUT ST",
         "APT:2",
         "PLACE:SHAMROCK",
         "MAP:54b",
         "ID:11111863");
 
     doTest("T2",
         "Breathing Problems-CHARLIE M1,E1,R1 \n1 UNAKA CT #7 \nMap 39D 21:23:05 11112056 \nThink green: Only print this e-mail and any attachment if necessar",
         "CALL:Breathing Problems",
         "PRI:C",
         "UNIT:M1,E1,R1",
         "ADDR:1 UNAKA CT",
         "APT:7",
         "MAP:39D",
         "ID:11112056");
 
     doTest("T3",
         "House/Residential Fire Alarm E2 \n2701 S ROAN ST #124 \nHERITAGE TRAILER PARK \nMap 55D 22:50:03 115214 \nThink green: Only print this e-mail and an",
         "CALL:House/Residential Fire Alarm",
         "UNIT:E2",
         "ADDR:2701 S ROAN ST",
         "APT:124",
         "PLACE:HERITAGE TRAILER PARK",
         "MAP:55D",
         "ID:115214");
 
     doTest("T4",
         "Assault/Sexual Assault-BRAVO M3,R3,E9 \n2918 BRISTOL HY \nADVANCE AUTO PARTS \nMap 38A 19:53:06 11113913 \nThink green: Only print this e-mail and a",
         "CALL:Assault/Sexual Assault",
         "PRI:B",
         "UNIT:M3,R3,E9",
         "ADDR:2918 BRISTOL HY",
         "PLACE:ADVANCE AUTO PARTS",
         "MAP:38A",
         "ID:11113913");
 
     doTest("T5",
         "Chest Pain(Non-Traumatic)-DELTA M1,R1,E3\n805 KENTUCKY ST\nX-STR= COLORADO ST\nORLEANS ST\nMap 47D 16:50:49 11147626\nThink green: Only print thi",
         "CALL:Chest Pain(Non-Traumatic)",
         "PRI:D",
         "UNIT:M1,R1,E3",
         "ADDR:805 KENTUCKY ST",
         "X:COLORADO ST & ORLEANS ST",
         "MAP:47D",
         "ID:11147626");
    
   }
   
 
   public static void main(String[] args) {
     new TNWashingtonCountyParserTest().generateTests("T1");
   }
 }
