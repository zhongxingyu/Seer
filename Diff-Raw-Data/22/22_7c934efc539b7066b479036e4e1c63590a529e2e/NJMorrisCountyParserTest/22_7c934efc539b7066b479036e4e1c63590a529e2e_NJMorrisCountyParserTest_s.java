 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NJMorrisCountyParserTest extends BaseParserTest {
   
   public NJMorrisCountyParserTest() {
     setParser(new NJMorrisCountyParser(), "MORRIS COUNTY", "NJ");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "PORZIO BROMBERG & NEWMAN (22) , 100 SOUTHGATE PKY [Morris Twp] (SICK PERSN) - SERIOUS PAIN IN LEGS... UNABLE TO WALK... - E2368 17:14",
         "PLACE:PORZIO BROMBERG & NEWMAN",
         "ADDR:100 SOUTHGATE PKY",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:SERIOUS PAIN IN LEGS... UNABLE TO WALK...",
         "UNIT:E2368",
         "TIME:17:14");
 
     doTest("T2",
         "4 JARDINE CT [Morris Twp] (UNCONSCN) - 84 YOM UNCONCIOUS IS BREATHING. - E2368 15:37",
         "ADDR:4 JARDINE CT",
         "CITY:Morris Twp",
         "CALL:UNCONSCN",
         "INFO:84 YOM UNCONCIOUS IS BREATHING.",
         "UNIT:E2368",
         "TIME:15:37");
 
     doTest("T3",
         "340 SPEEDWELL AVE [Morris Plains] (BREATHING) - MORISM FAMILY MEDICINE ASSOC. DR. JOSEPH OFFICE 50 YOF DIFF BREATHING IS ON OXYGEN FULL TIME - 2398 11:25",
         "ADDR:340 SPEEDWELL AVE",
         "CITY:Morris Plains",
         "CALL:BREATHING",
         "INFO:MORISM FAMILY MEDICINE ASSOC. DR. JOSEPH OFFICE 50 YOF DIFF BREATHING IS ON OXYGEN FULL TIME",
         "UNIT:2398",
         "TIME:11:25");
 
     doTest("T4",
         "26 CLEVELAND AVE [Morris Twp] (SICK PERSN) - 47 YOF W/A FOOT INJURY - E2366 07:57",
         "ADDR:26 CLEVELAND AVE",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:47 YOF W/A FOOT INJURY",
         "UNIT:E2366",
         "TIME:07:57");
 
     doTest("T5",
         "MORRIS VIEW NURSING HOME (22) , 540 W HANOVER AVE BLDG MORRIS [Morris  Twp] (BLEEDING) - LACERATION HER RIGHT LEG... IN UNIT 1A ROOM 1108... - E2368 07:54",
         "PLACE:MORRIS VIEW NURSING HOME",
         "ADDR:540 W HANOVER AVE",
         "APT:MORRIS",
         "CITY:Morris  Twp",
         "CALL:BLEEDING",
         "INFO:LACERATION HER RIGHT LEG... IN UNIT 1A ROOM 1108...",
         "UNIT:E2368",
         "TIME:07:54");
 
     doTest("T6",
         "***626 HANOVER 22 [Morris Twp] (SICK PERSN) - MALE NOT FEELING WELL CALLER STATES THAT HE IS WHITE AS A SHEET 2ND TO LAST HOUSE BEFORE RANDOLPH - 2398 04:56",
         "ADDR:626 HANOVER 22",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:MALE NOT FEELING WELL CALLER STATES THAT HE IS WHITE AS A SHEET 2ND TO LAST HOUSE BEFORE RANDOLPH",
         "UNIT:2398",
         "TIME:04:56");
 
     doTest("T7",
         "35 SHERMAN AVE [Morris Twp] (CHEST PAIN) - 64 YOF CHEST PAIN, JUST HAD DFIB INSTALLED - E2368 21:39",
         "ADDR:35 SHERMAN AVE",
         "CITY:Morris Twp",
         "CALL:CHEST PAIN",
         "INFO:64 YOF CHEST PAIN, JUST HAD DFIB INSTALLED",
         "UNIT:E2368",
         "TIME:21:39");
 
     doTest("T8",
         "1 MOLLY STARK DR [Morris Twp] (FALL) - CALLER STATED SHE WAS FEALING WEAK AND SHE FELL. SHE HAS A CUT TO HER HEAD AND SHE CANT GET UP. - E2368 14:06",
         "ADDR:1 MOLLY STARK DR",
         "CITY:Morris Twp",
         "CALL:FALL",
         "INFO:CALLER STATED SHE WAS FEALING WEAK AND SHE FELL. SHE HAS A CUT TO HER HEAD AND SHE CANT GET UP.",
         "UNIT:E2368",
         "TIME:14:06");
 
     doTest("T9",
         "43 W HANOVER AVE [Randolph Twp] (MUTUAL AID) - MUTUAL AID INTO RANDOLPH FOR PSYCH TRANSPORT... NO LIGHTS/SIRENS. - 3281,2398 14:03",
         "ADDR:43 W HANOVER AVE",
         "CITY:Randolph Twp",
         "CALL:MUTUAL AID",
         "INFO:MUTUAL AID INTO RANDOLPH FOR PSYCH TRANSPORT... NO LIGHTS/SIRENS.",
         "UNIT:3281,2398",
         "TIME:14:03");
 
     doTest("T10",
         "1 BROOK DR S [Morris Plains] (UNCONSCN) - 83 YOM ALTERED MENTAL STATUS - 2398 19:14",
         "ADDR:1 BROOK DR S",
         "CITY:Morris Plains",
         "CALL:UNCONSCN",
         "INFO:83 YOM ALTERED MENTAL STATUS",
         "UNIT:2398",
         "TIME:19:14");
 
     doTest("T11",
         "MADISON HOTEL (22), 1 CONVENT RD [Morris Twp] (SICK PERSN) - SICK FEMALE IN LOBBY - E2368 19:01",
         "PLACE:MADISON HOTEL",
         "ADDR:1 CONVENT RD",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:SICK FEMALE IN LOBBY",
         "UNIT:E2368",
         "TIME:19:01");
 
     doTest("T12",
         "CANFIELD RD / PIPPINS WAY [Morris Twp] (OVERDOSE) - INTOXICATED FEMALE, AT LOCATION OF AN MVA - E2368 01:46",
         "ADDR:CANFIELD RD & PIPPINS WAY",
         "CITY:Morris Twp",
         "CALL:OVERDOSE",
         "INFO:INTOXICATED FEMALE, AT LOCATION OF AN MVA",
         "UNIT:E2368",
         "TIME:01:46");
 
     doTest("T13",
         "UNIT 1D ROOM 1422 MORRIS VIEW NURSING HOME (22), 540 W HANOVER AVE [Morris Twp] (SICK PERSN) - FEMALE WITH ALTERMENTAL STATUS - E2366 09:37",
         "PLACE:UNIT 1D ROOM 1422 MORRIS VIEW NURSING HOME",
         "ADDR:540 W HANOVER AVE",
         "CITY:Morris Twp",
         "CALL:SICK PERSN",
         "INFO:FEMALE WITH ALTERMENTAL STATUS",
         "UNIT:E2366",
         "TIME:09:37");
 
     doTest("T14",
         "ST PAULS CHURCH, MOUNTAIN WAY / HILLVIEW AVE [Morris Plains] (UNCONSCN) - FEMALE PASSED OUT. CONSCIOUS AT THIS TIME - 2398 10:44",
         "PLACE:ST PAULS CHURCH",
         "ADDR:MOUNTAIN WAY & HILLVIEW AVE",
         "CITY:Morris Plains",
         "CALL:UNCONSCN",
         "INFO:FEMALE PASSED OUT. CONSCIOUS AT THIS TIME",
         "UNIT:2398",
         "TIME:10:44");
 
     doTest("T15",
         "RACEWAY GAS(23), 1701 RT 10 [Morris Plains] (CAR FIRE) - CAR FIRE IN THE FAST LANE - 2399,2398 22:46",
         "PLACE:RACEWAY GAS",
         "ADDR:1701 RT 10",
         "CITY:Morris Plains",
         "CALL:CAR FIRE",
         "INFO:CAR FIRE IN THE FAST LANE",
         "UNIT:2399,2398",
         "TIME:22:46");
 
     doTest("T16",
         "901 RT 10 [Hanover Twp] (MUTUAL AID) - THE JCC BUILDING FOR A FEMAL WITH A LACERATION TO THE FINGER/ MORRIS MIN MEN - 2398 16:54",
         "ADDR:901 RT 10",
         "CITY:Hanover Twp",
         "CALL:MUTUAL AID",
         "INFO:THE JCC BUILDING FOR A FEMAL WITH A LACERATION TO THE FINGER/ MORRIS MIN MEN",
         "UNIT:2398",
         "TIME:16:54");
 
     doTest("T17",
         "POST91-7 (MENNEN ARENA), 161 E HANOVER AVE [Morris Twp] (FALL) - IN BACK OF RINK 1 ON RIGHT HAND SIDE. 40 YR OLD FEMALE FELL, AND HURT HER RIGHT KNEE. HEARD CRACK ON FALL. - 2398 10:38",
         "PLACE:POST91-7 (MENNEN ARENA)",
         "ADDR:161 E HANOVER AVE",
         "CITY:Morris Twp",
         "CALL:FALL",
         "INFO:IN BACK OF RINK 1 ON RIGHT HAND SIDE. 40 YR OLD FEMALE FELL, AND HURT HER RIGHT KNEE. HEARD CRACK ON FALL.",
         "UNIT:2398",
         "TIME:10:38");
 
     doTest("T18",
         "AMERICAN RD / E HANOVER AVE [Morris Plains] (MVA) - 2 CAR MVA, PATIENT COMPLAINING OF HEAD PAIN. HONDA CIVIC VS FORD. VEHICLES IN INTERSECTION - 2398 16:53",
         "ADDR:AMERICAN RD & E HANOVER AVE",
         "CITY:Morris Plains",
         "CALL:MVA",
         "INFO:2 CAR MVA, PATIENT COMPLAINING OF HEAD PAIN. HONDA CIVIC VS FORD. VEHICLES IN INTERSECTION",
         "UNIT:2398",
         "TIME:16:53");
 
     doTest("T19",
         "LITTLETON RD / COURT RD [Morris Plains] (MVA) - PD REQUEST 1 RIG, AIR BAG DEPLOYMENT - 2398 12:15",
         "ADDR:LITTLETON RD & COURT RD",
         "CITY:Morris Plains",
         "CALL:MVA",
         "INFO:PD REQUEST 1 RIG, AIR BAG DEPLOYMENT",
         "UNIT:2398",
         "TIME:12:15");
 
     doTest("T20",
         "SUNRISE ASSISTED LIVING (23), 209 LITTLETON RD [Morris Plains] (SICK PERSN) - 1ST FLOOR...CALLER REPORTS THE FEMALE PATIENT IS SHAKING AND SCREAMING. CALLE",
         "PLACE:SUNRISE ASSISTED LIVING",
         "ADDR:209 LITTLETON RD",
         "CITY:Morris Plains",
         "CALL:SICK PERSN",
         "INFO:1ST FLOOR...CALLER REPORTS THE FEMALE PATIENT IS SHAKING AND SCREAMING. CALLE");
 
     doTest("T21",
         "prvs=3175827e30=dispatch@co.morris.nj.us Police Academy Morris Cty, 500 W HANOVER AVE [Parsippany-Troy] (UNCONSCNS) - CONSTRUCTION WORKER STATED HE BLACKED OUT. HE IS SITTING INSID",
         "PLACE:Police Academy Morris Cty",
         "ADDR:500 W HANOVER AVE",
         "CITY:Parsippany-Troy",
         "CALL:UNCONSCNS",
         "INFO:CONSTRUCTION WORKER STATED HE BLACKED OUT. HE IS SITTING INSID");
 
     doTest("T22",
         "prvs=62791756a7=dispatch@co.morris.nj.us PRO HAIRCUTTERS INC (23), 1711 RT 10 E [Morris Plains] (HEM/CUTS) - 82 Y/O FEMALE LACERATION TO LEG - 2398 16:07",
         "PLACE:PRO HAIRCUTTERS INC",
         "ADDR:1711 RT 10 E",
         "CITY:Morris Plains",
         "CALL:HEM/CUTS",
         "INFO:82 Y/O FEMALE LACERATION TO LEG",
         "UNIT:2398",
         "TIME:16:07");
 
     doTest("T23",
         "prvs=24498f54c3=dispatch@co.morris.nj.us MORRISTOWN BEARD SCHOOL (22), 70 WHIPPANY RD [Morris Twp] (STROKE\\\\CVA) - E2368,F22DUTY,E8002\n" +
         "AT FRONT OF CAMPUS; POSS MALE WITH STROKE\n" +
         "1st",
 
         "PLACE:MORRISTOWN BEARD SCHOOL",
         "ADDR:70 WHIPPANY RD",
         "CITY:Morris Twp",
         "CALL:STROKE\\\\CVA",
         "INFO:AT FRONT OF CAMPUS; POSS MALE WITH STROKE / 1st",
         "UNIT:E2368,F22DUTY,E8002");
   }
   
   @Test
   public void testkmattes13() {
 
     doTest("T1",
         "4 CONDIT ST [Roxbury Twp] (APPLIANCE) - 3691,3681,3682,3683\r\n" +
         "CALLER STATES SMOKE COMING FROM FURNACE, TURNED OFF NO LONGER SMOKING\r\n" +
         "F\r",
 
         "ADDR:4 CONDIT ST",
         "CITY:Roxbury Twp",
         "CALL:APPLIANCE",
         "INFO:CALLER STATES SMOKE COMING FROM FURNACE, TURNED OFF NO LONGER SMOKING / F",
         "UNIT:3691,3681,3682,3683");
 
     doTest("T2",
         "MERRY HEART NURSING HOME BLD A (36) BUILDING B, 200 RT 10 W [Roxbury Twp] (HEART) - 3681,3682,3683,8000\r\n" +
         "PT WITH ALTERED MENTAL STATUS\r",
 
         "PLACE:MERRY HEART NURSING HOME BLD A",
         "APT:BUILDING B",
         "ADDR:200 RT 10 W",
         "CITY:Roxbury Twp",
         "CALL:HEART",
         "INFO:PT WITH ALTERED MENTAL STATUS",
         "UNIT:3681,3682,3683,8000");
 
     doTest("T3",
         "7 STONE COTTAGE LN [Roxbury Twp] (HEM/CUTS) - 3683,3682,3681,8000\r\n" +
         "MALE LACERATIONS\r\n" +
         "1 Patient, 41 year(s) Male, Conscious, Breathing\r\r",
 
         "ADDR:7 STONE COTTAGE LN",
         "CITY:Roxbury Twp",
         "CALL:HEM/CUTS",
         "INFO:MALE LACERATIONS / 1 Patient, 41 year(s) Male, Conscious, Breathing",
         "UNIT:3683,3682,3681,8000");
 
     doTest("T4",
         "ROXBURY FAMILY CARE (36) 4TH FLOOR, 66 SUNSET STRP [Roxbury Twp] (UNCONSCIOU) - 3681,3682,3683,8000\r\n" +
         "UNCONCIOUS FEMALE\r\n" +
         "1 Patient, 73 \r",
 
         "PLACE:ROXBURY FAMILY CARE",
         "APT:4TH FLOOR",
         "ADDR:66 SUNSET STRP",
         "CITY:Roxbury Twp",
         "CALL:UNCONSCIOU",
         "INFO:UNCONCIOUS FEMALE / 1 Patient, 73",
         "UNIT:3681,3682,3683,8000");
 
     doTest("T5",
         "38 REGER RD [Roxbury Twp] (ABDOMINAL) - 3681,3682,3683\r\n" +
         "70 YOF SEVERE ABDOMINAL PAIN\r\n" +
         "1 Patient, 70 year(s) Female, Conscious, Breathi\r",
 
         "ADDR:38 REGER RD",
         "CITY:Roxbury Twp",
         "CALL:ABDOMINAL",
         "INFO:70 YOF SEVERE ABDOMINAL PAIN / 1 Patient, 70 year(s) Female, Conscious, Breathi",
         "UNIT:3681,3682,3683");
 
     doTest("T6",
         "DIALYSIS CENTER OF NJ (36), 175 RIGHTER RD [Roxbury Twp] (CARDIO/ARR) - 3681,3682,3683,8000,E80515\r\n" +
         "EXT.9 MALE RESPIRATORY ARREST\r\n" +
         "1st\r",
 
         "PLACE:DIALYSIS CENTER OF NJ",
         "ADDR:175 RIGHTER RD",
         "CITY:Roxbury Twp",
         "CALL:CARDIO/ARR",
         "INFO:EXT.9 MALE RESPIRATORY ARREST / 1st",
         "UNIT:3681,3682,3683,8000,E80515");
 
     doTest("T7",
         "NOAH'S ARK ANIMAL WELFARE (36), 1915 RT 46 W [Roxbury Twp] (HEM/CUTS) - 3682,3681,3683\r\n" +
         "HEAD LACERATION\r\n" +
         "1 Patient, 8 year(s) Male, Co\r",
 
         "PLACE:NOAH'S ARK ANIMAL WELFARE",
         "ADDR:1915 RT 46 W",
         "CITY:Roxbury Twp",
         "CALL:HEM/CUTS",
         "INFO:HEAD LACERATION / 1 Patient, 8 year(s) Male, Co",
         "UNIT:3682,3681,3683");
 
     doTest("T8",
         "209 MAIN ST [Roxbury Twp] (RES ALARM) - 3691,3681,3682,3683\r\n" +
         "ROXBURY HISTORIC TRUST - FRONT DOOR - MAIN ST LEDGEWOOD\r\n" +
         "F120950013 17:2\r",
 
         "ADDR:209 MAIN ST",
         "CITY:Roxbury Twp",
         "CALL:RES ALARM",
         "INFO:ROXBURY HISTORIC TRUST - FRONT DOOR - MAIN ST LEDGEWOOD / F120950013 17:2",
         "UNIT:3691,3681,3682,3683");
 
     doTest("T9",
         "prvs=244183d440=dispatch@co.morris.nj.us 16 LINDSLEY RD [Harding Twp] (CO ALARM) - 1399\n" +
         "CO ACTIVATION SURIT RES ..973-538-0659\n" +
         "F120950020 19:28",
 
         "ADDR:16 LINDSLEY RD",
         "CITY:Harding Twp",
         "CALL:CO ALARM",
        "INFO:CO ACTIVATION SURIT RES ..973-538-0659 / F120950020 19:28",
        "UNIT:1399");
     
   }
   
   @Test
   public void testGarySantaniello() {
 
     doTest("T1",
         "(Roxbury Fire) LITTLE ACHIEVEMENTS CHILD CARE (36), 290 MT ARLINGTON BLVD [Roxbury Twp] (UNCONSCIOU) - 3682,3681,3683\n" +
         "3 YOM FELT DIZZY , FELL AND BROKE",
 
         "PLACE:LITTLE ACHIEVEMENTS CHILD CARE",
         "ADDR:290 MT ARLINGTON BLVD",
         "CITY:Roxbury Twp",
         "CALL:UNCONSCIOU",
         "INFO:3 YOM FELT DIZZY , FELL AND BROKE",
         "UNIT:3682,3681,3683");
 
     doTest("T2",
         "(Roxbury Fire) SKYLANDS MEDICAL (36) 1ST FLOOR, 150 LAKESIDE BLVD [Roxbury Twp] (BREATHING) - 3682,3681,3683,8000\n" +
         "SHORTNESS OF BREATH\n" +
         "3rd Party, 1 Patie",
 
         "PLACE:SKYLANDS MEDICAL",
         "APT:1ST FLOOR",
         "ADDR:150 LAKESIDE BLVD",
         "CITY:Roxbury Twp",
         "CALL:BREATHING",
         "INFO:SHORTNESS OF BREATH / 3rd Party, 1 Patie",
         "UNIT:3682,3681,3683,8000");
 
     doTest("T3",
         "prvs=2454f7cf85=dispatch@co.morris.nj.us SKYLANDS MEDICAL (36) 1ST FLOOR, 150 LAKESIDE BLVD [Roxbury Twp] (BREATHING) - 3682,3681,3683,8000\n" +
         "SHORTNESS OF BREATH\n" +
         "3rd Party, 1 Patient",
 
         "PLACE:SKYLANDS MEDICAL",
         "APT:1ST FLOOR",
         "ADDR:150 LAKESIDE BLVD",
         "CITY:Roxbury Twp",
         "CALL:BREATHING",
         "INFO:SHORTNESS OF BREATH / 3rd Party, 1 Patient",
         "UNIT:3682,3681,3683,8000");
 
     doTest("T4",
         "prvs=2454f7cf85=dispatch@co.morris.nj.us 104 WHISPER WAY E [Roxbury Twp] (RESD FIRE) - 3691,3692,3693,3681,3682,3683,2899\n" +
         "CALLER STATES THERE IS A FIRE NEAR HIS APT COMPLEX\n" +
         "F12108",
 
         "ADDR:104 WHISPER WAY E",
         "CITY:Roxbury Twp",
         "CALL:RESD FIRE",
         "INFO:CALLER STATES THERE IS A FIRE NEAR HIS APT COMPLEX / F12108",
         "UNIT:3691,3692,3693,3681,3682,3683,2899");
 
     doTest("T5",
         "prvs=2463121dc2=dispatch@co.morris.nj.us 71 N HILLSIDE AVE [Roxbury Twp] (BREATHING) - 3681,3682,3683,8000,E80516\n" +
         "  Y/O F DIFF BREATHING\n" +
         "2nd Party, 1 Patient, 3 year(s) Female, Con",
 
         "ADDR:71 N HILLSIDE AVE",
         "CITY:Roxbury Twp",
         "CALL:BREATHING",
         "INFO:Y/O F DIFF BREATHING / 2nd Party, 1 Patient, 3 year(s) Female, Con",
         "UNIT:3681,3682,3683,8000,E80516");
 
     doTest("T6",
         "prvs=2463121dc2=dispatch@co.morris.nj.us 71 N HILLSIDE AVE [Roxbury Twp] (BREATHING) - 3681,3682,3683,8000\n" +
         "3 Y/O F DIFF BREATHING\n" +
         "2nd Party, 1 Patient, 3 year(s) Female, Conscious,",
 
         "ADDR:71 N HILLSIDE AVE",
         "CITY:Roxbury Twp",
         "CALL:BREATHING",
         "INFO:3 Y/O F DIFF BREATHING / 2nd Party, 1 Patient, 3 year(s) Female, Conscious,",
         "UNIT:3681,3682,3683,8000");
 
     doTest("T7",
         "(Roxbury Fire) 1 PUTTERS RD [Roxbury Twp] (GAS LEAK) - 3691,3681,F36E12,E36109,3682,3683,3692,3693\n" +
         "ODOR OF HEATING OIL/CALLER IS CONCERNED ABOUT CO LEVE",
 
         "ADDR:1 PUTTERS RD",
         "CITY:Roxbury Twp",
         "CALL:GAS LEAK",
         "INFO:ODOR OF HEATING OIL/CALLER IS CONCERNED ABOUT CO LEVE",
         "UNIT:3691,3681,F36E12,E36109,3682,3683,3692,3693");
 
   }
   
   @Test
   public void testLeeBender() {
 
     doTest("T1",
         "prvs=44951d6e4e=dispatch@co.morris.nj.us MORRIS MEWS (22) BLDING 2, APT C7, 95 KETCH RD [Morris Twp] (UNKNOWN) - E2368\n" +
         "UNKNOWN PROBLEM, LANGUAGE BARRIER, CALLER STATES NEEDS TO GO",
 
         "PLACE:MORRIS MEWS",
         "APT:BLDING 2 - APT C7",
         "ADDR:95 KETCH RD",
         "CITY:Morris Twp",
         "CALL:UNKNOWN",
         "INFO:UNKNOWN PROBLEM, LANGUAGE BARRIER, CALLER STATES NEEDS TO GO",
         "UNIT:E2368");
     
   }
   
   public static void main(String[] args) {
    new NJMorrisCountyParserTest().generateTests("T5", "PLACE APT ADDR CITY CALL INFO UNIT TIME");
   }
 }
