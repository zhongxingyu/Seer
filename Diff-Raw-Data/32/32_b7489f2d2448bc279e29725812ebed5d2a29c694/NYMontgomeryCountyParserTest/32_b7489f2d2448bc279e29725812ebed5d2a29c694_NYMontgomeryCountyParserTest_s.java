 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYMontgomeryCountyParserTest extends BaseParserTest {
   
   public NYMontgomeryCountyParserTest() {
     setParser(new NYMontgomeryCountyParser(), "MONTGOMERY COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Subject:HVFD 218\n EMS - EMS CALL  14 PINE ST AMSTERDAM CITY  09/24/2011 1223\n\n\n",
         "SRC:HVFD 218",
         "CALL:EMS - EMS CALL",
         "ADDR:14 PINE ST",
         "CITY:AMSTERDAM CITY",
         "ID:1223");
 
     doTest("T2",
         "Subject:HVFD 218\nMOTOR VEHICLE ACCIDENT   STATE HWY 30 T AMSTERDAM AMSTERDAM COMMONS 09/25/2011 0851\n\n\n",
         "SRC:HVFD 218",
         "CALL:MOTOR VEHICLE ACCIDENT",
         "ADDR:STATE HWY 30 T",
         "MADDR:AMSTERDAM COMMONS,STATE 30 T",
         "CITY:AMSTERDAM",
         "PLACE:AMSTERDAM COMMONS",
         "ID:0851");
 
     doTest("T3",
         "Subject:HVFD 218\nEMS - EMS CALL  300 HAMMOND TOWN RD AMSTERDAM  09/25/2011 1015\n\n\n",
         "SRC:HVFD 218",
         "CALL:EMS - EMS CALL",
         "ADDR:300 HAMMOND TOWN RD",
         "CITY:AMSTERDAM",
         "ID:1015");
 
     doTest("T4",
         "Subject:HVFD 218\nEMS - EMS CALL  106 HANNAFORD PLZ AMSTERDAM  09/25/2011 1945\n\n\n",
         "SRC:HVFD 218",
         "CALL:EMS - EMS CALL",
         "ADDR:106 HANNAFORD PLZ",
         "CITY:AMSTERDAM",
         "ID:1945");
 
     doTest("T5",
         "Subject:HVFD 218\nEMS - EMS CALL  5005 STATE HWY 30 AMSTERDAM  09/25/2011 2250\n\n\n",
         "SRC:HVFD 218",
         "CALL:EMS - EMS CALL",
         "ADDR:5005 STATE HWY 30",
         "MADDR:5005 STATE 30",
         "CITY:AMSTERDAM",
         "ID:2250");
 
     doTest("T6",
         "Subject:HVFD 218\nEMS - EMS CALL  10 MAIN ST HAGAMAN VILLAGE  09/26/2011 0759\n\n\n",
         "SRC:HVFD 218",
         "CALL:EMS - EMS CALL",
         "ADDR:10 MAIN ST",
         "CITY:HAGAMAN VILLAGE",
         "ID:0759");
 
     doTest("T7",
         "Subject:HVFD 218\nMOTOR VEHICLE ACCIDENT WALMART 101 SANFORD FARM SHOP CT AMSTERDAM  09/26/2011 1011\n\n\n",
         "SRC:HVFD 218",
         "CALL:MOTOR VEHICLE ACCIDENT",
         "PLACE:WALMART",
         "ADDR:101 SANFORD FARM SHOPPING CTR",
         "CITY:AMSTERDAM",
         "ID:1011");
 
     doTest("T8",
         "Subject:HVFD 218\nMOTOR VEHICLE ACCIDENT RECOVERY SPORTS GRILL 5005 STHWY 30 AMSTERDAM  09/26/2011 1331\n\n\n",
         "SRC:HVFD 218",
         "CALL:MOTOR VEHICLE ACCIDENT",
         "PLACE:RECOVERY SPORTS GRILL",
         "ADDR:5005 STHWY 30",
         "MADDR:5005 ST 30",
         "CITY:AMSTERDAM",
         "ID:1331");
 
     doTest("T9",
         "Subject:HVFD 218\nMOTOR VEHICLE ACCIDENT   STATE HWY 30 T AMSTERDAM AMSTERDAM COMMONS 09/25/2011 0851",
         "SRC:HVFD 218",
         "CALL:MOTOR VEHICLE ACCIDENT",
         "ADDR:STATE HWY 30 T",
         "MADDR:AMSTERDAM COMMONS,STATE 30 T",
         "CITY:AMSTERDAM",
         "PLACE:AMSTERDAM COMMONS",
         "ID:0851");
   }
   
   public static void main(String[] args) {
     new NYMontgomeryCountyParserTest().generateTests("T1", "SRC CALL PLACE ADDR CITY ID");
   }
 }
