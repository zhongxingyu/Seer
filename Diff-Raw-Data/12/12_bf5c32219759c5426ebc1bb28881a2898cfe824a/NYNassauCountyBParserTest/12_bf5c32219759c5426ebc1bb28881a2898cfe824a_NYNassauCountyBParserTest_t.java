 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYNassauCountyBParserTest extends BaseParserTest {
   
   public NYNassauCountyBParserTest() {
     setParser(new NYNassauCountyBParser(), "NASSAU COUNTY", "NY");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "FireCom:  3 HUDSON ST E (GENERAL-AUTO) FIRE ALARM RINGING",
         "ADDR:3 HUDSON ST E",
         "CALL:GENERAL-AUTO",
         "INFO:FIRE ALARM RINGING");
 
     doTest("T2",
         "FireCom:  31 MUNSON PL (SIG 9-AMBU) 20F ABDOMINAL PAINS",
         "ADDR:31 MUNSON PL",
         "CALL:SIG 9-AMBU",
         "INFO:20F ABDOMINAL PAINS");
 
     doTest("T3",
         "FireCom: THE FISHERY 1 MAIN ST (SIG 9-AMBU) MALE FELL-FX WRIST",
         "PLACE:THE FISHERY",
         "ADDR:1 MAIN ST",
         "CALL:SIG 9-AMBU",
         "INFO:MALE FELL-FX WRIST");
 
     doTest("T4",
         "FireCom:  528 ATLANTIC AVE (SIG 9-AMBU) BACK DOOR DIFF BREATHING",
         "ADDR:528 ATLANTIC AVE",
         "CALL:SIG 9-AMBU",
         "INFO:BACK DOOR DIFF BREATHING");
 
     doTest("T5",
         "FireCom:  RHAME AVE (DISTRIC-HAZM) SW CORNER/SEWER-ILLEGALE DUMPING",
         "ADDR:RHAME AVE",
         "CALL:DISTRIC-HAZM",
         "INFO:SW CORNER/SEWER-ILLEGALE DUMPING");
 
     doTest("T6",
         "FireCom:  22 RIVERSIDE ROAD (SIG 9-AMBU) DEHYDRATED MA",
         "ADDR:22 RIVERSIDE ROAD",
         "CALL:SIG 9-AMBU",
         "INFO:DEHYDRATED MA");
 
     doTest("T7",
         "FireCom:  65 EMMET AVE (GENERAL-AUTO) ESTRIN RES/40A024",
         "ADDR:65 EMMET AVE",
         "CALL:GENERAL-AUTO",
         "INFO:ESTRIN RES/40A024");
 
     doTest("T8",
         "FRM:paging@alpinesoftware.com\nMSG:\nRedAlert: 528 MERRICK ROAD (SIG 9A-AMBU) SICK MALE",
         "ADDR:528 MERRICK ROAD",
         "CALL:SIG 9A-AMBU",
         "INFO:SICK MALE");
   }
   
   @Test
   public void testParser2() {
 
 
     doTest("T1",
         "FireCom:  180 KAMDA BLVD C/S: STEPHAN AVE / FRANKLIN AVE -    3 (RESCUE-AMBU) MALE FELL/HEAD INJURY D: 06/15 17:25 #: 000486﻿",
         "ADDR:180 KAMDA BLVD",
         "X:STEPHAN AVE / FRANKLIN AVE",
         "PRI:3",
         "CALL:RESCUE-AMBU",
         "INFO:MALE FELL/HEAD INJURY",
         "ID:000486﻿");
 
     doTest("T2",
         "FireCom: NEW HYDE PARK DINER 160 HILLSIDE AVE C/S: 1ST (N) ST / 2ND (N) ST -    3 (RESCUE-AMBU) ELDERLY FEMALE FELL/HEAD INJURY D: 05﻿/31 08:25 #: 000424",
         "PLACE:NEW HYDE PARK DINER",
         "ADDR:160 HILLSIDE AVE",
         "X:1ST (N) ST / 2ND (N) ST",
         "PRI:3",
         "CALL:RESCUE-AMBU",
         "INFO:ELDERLY FEMALE FELL/HEAD INJURY",
         "ID:000424");
 
     doTest("T3",
         "FireCom:  WEST END AVE C/S: LEONARD BLVD -    1 (RESCUE-AMBU) DRIVER SICK ON SCHOOL BUS D: 06/14 15:53 #: 000484﻿",
         "ADDR:WEST END AVE",
         "X:LEONARD BLVD",
         "PRI:1",
         "CALL:RESCUE-AMBU",
         "INFO:DRIVER SICK ON SCHOOL BUS",
         "ID:000484﻿");
 
     doTest("T4",
         "FireCom: NEW HYDE PARK FD HEADQUARTERS 1555 JERICHO TPK C/S: NEW HYDE PARK ROAD / TERRACE BLVD -    1 (RESCUE-AMBU) SICK FEMALE ON RA﻿MP D: 06/13 14:25 #: 000480",
         "PLACE:NEW HYDE PARK FD HEADQUARTERS",
         "ADDR:1555 JERICHO TPK",
         "X:NEW HYDE PARK ROAD / TERRACE BLVD",
         "PRI:1",
         "CALL:RESCUE-AMBU",
         "INFO:SICK FEMALE ON RA﻿MP",
         "ID:000480");
 
     doTest("T5",
         "FireCom:  1300 5TH AVE C/S: 13TH (S) ST / 14TH (S) ST - 2 (GENERAL-NATU) MAIN LINE BREAK BY CONTRACTOR D: 06/14 13:04 #: 000482﻿",
         "ADDR:1300 5TH AVE",
         "X:13TH (S) ST / 14TH (S) ST",
         "PRI:2",
         "CALL:GENERAL-NATU",
         "INFO:MAIN LINE BREAK BY CONTRACTOR",
         "ID:000482﻿");
 
     doTest("T6",
         "FireCom: HOLY SPIRIT SCHOOL 13 6TH ST S C/S: JERICHO TPK / 1ST AVE -    2 (GENERAL-NATU) INSIDE THE SCHOOL D: 06/14 13:29 #: 000483",
         "PLACE:HOLY SPIRIT SCHOOL",
         "ADDR:13 6TH ST S",
         "X:JERICHO TPK / 1ST AVE",
         "PRI:2",
         "CALL:GENERAL-NATU",
         "INFO:INSIDE THE SCHOOL",
         "ID:000483");
 
    doTest("T7",
        "MSG:FireCom: 29 JEFFERSON ST C/S: SYLVIA LA / GERARD AVE -    3 (RESCUE-AMBU) SICK FEMALE D: 09/10 06:39 #: 000798",
        "ADDR:29 JEFFERSON ST",
        "X:SYLVIA LA / GERARD AVE",
        "PRI:3",
        "CALL:RESCUE-AMBU",
        "INFO:SICK FEMALE",
        "ID:000798");

   }
   
   public static void main(String[] args) {
    new NYNassauCountyBParserTest().generateTests("T7", "ADDR X PRI CALL INFO ID");
   }
 }
