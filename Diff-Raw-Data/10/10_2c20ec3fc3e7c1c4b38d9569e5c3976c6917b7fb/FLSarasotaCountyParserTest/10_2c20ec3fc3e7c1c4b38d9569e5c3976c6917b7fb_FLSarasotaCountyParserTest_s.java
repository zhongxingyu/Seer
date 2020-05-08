 package net.anei.cadpage.parsers.FL;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class FLSarasotaCountyParserTest extends BaseParserTest {
   
   public FLSarasotaCountyParserTest() {
     setParser(new FLSarasotaCountyParser(), "SARASOTA COUNTY", "FL");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "SARASOTA COUNTY EVENT TYPE: ODOR-INSIDE Location: 4546 DEER CREEK BLVD NCTY G225 TIME: 21:39:20  Disp: BN3,E14,E15,R14",
         "CALL:ODOR-INSIDE",
         "ADDR:4546 DEER CREEK BLVD",
         "MAP:G225",
         "TIME:21:39:20",
         "UNIT:BN3,E14,E15,R14");
 
     doTest("T2",
         "SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 4681 MACEACHEN BLVD NCTY G146 TIME: 18:23:38  Disp: BN3,E11,E12,E8,R2,R7,T18,TRK11",
         "CALL:STRUCTURE FIRE",
         "ADDR:4681 MACEACHEN BLVD",
         "MAP:G146",
         "TIME:18:23:38",
         "UNIT:BN3,E11,E12,E8,R2,R7,T18,TRK11");
 
     doTest("T3",
         "SARASOTA COUNTY EVENT TYPE: FALLS Location: 1035 N LIME AVE SARA G056 TIME: 14:35:29  Disp: BN1,E1,EMS1,R1",
         "CALL:FALLS",
         "ADDR:1035 N LIME AVE",
         "CITY:SARASOTA",
         "MAP:G056",
         "TIME:14:35:29",
         "UNIT:BN1,E1,EMS1,R1");
 
     doTest("T4",
         "SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 3908 WALNUT AVE NCTY G033 TIME: 16:12:52  Disp: BN2,E1,E4,E5,EMS1,R3,R5,SQ8,TRK5",
         "CALL:STRUCTURE FIRE",
         "ADDR:3908 WALNUT AVE",
         "MAP:G033",
         "TIME:16:12:52",
         "UNIT:BN2,E1,E4,E5,EMS1,R3,R5,SQ8,TRK5");
 
     doTest("T5",
         "SARASOTA COUNTY EVENT TYPE: STRUCTURE FIRE Location: 3908 WALNUT AVE NCTY G033 TIME: 16:12:52  Disp:",
         "CALL:STRUCTURE FIRE",
         "ADDR:3908 WALNUT AVE",
         "MAP:G033",
         "TIME:16:12:52");
 
     doTest("T6",
         "SARASOTA COUNTY EVENT TYPE: ASSIST OTHER MEDICAL AGENCY Location: 5731 BEE RIDGE RD NCTY G128: @DOCTORS TIME: 18:51:05  Disp: BN2,E8,R8",
         "CALL:ASSIST OTHER MEDICAL AGENCY",
         "ADDR:5731 BEE RIDGE RD",
         "MAP:G128",
         "PLACE:DOCTORS",
         "TIME:18:51:05",
         "UNIT:BN2,E8,R8");
 
     doTest("T7",
         "SARASOTA COUNTY EVENT TYPE: VEHICLE CRASH WITH INJURIES Location: BEE RIDGE RD/MAUNA LOA BLVD NCTY G149 TIME: 02:06:18  Disp: BN2,E16,R8,SQ8",
         "CALL:VEHICLE CRASH WITH INJURIES",
         "ADDR:BEE RIDGE RD & MAUNA LOA BLVD",
         "MAP:G149",
         "TIME:02:06:18",
         "UNIT:BN2,E16,R8,SQ8");
 
     doTest("T8",
         "SARASOTA COUNTY EVENT TYPE: OUTSIDE FIRE-UNKNOWN Location: STONEYCREEK BLVD/CENTER RD SCTY G511 TIME: 19:00:05  Disp: BN4,E21,E26,R21",
         "CALL:OUTSIDE FIRE-UNKNOWN",
         "ADDR:STONEYCREEK BLVD & CENTER RD",
         "MAP:G511",
         "TIME:19:00:05",
         "UNIT:BN4,E21,E26,R21");
 
     doTest("T9",
         "SARASOTA COUNTY EVENT TYPE: VEHICLE CRASH WITH INJURIES Location: S TUTTLE AVE/SUNNYSIDE ST NCTY G124 TIME: 19:28:44  Disp: BN1,E2,EMS1,R2",
         "CALL:VEHICLE CRASH WITH INJURIES",
         "ADDR:S TUTTLE AVE & SUNNYSIDE ST",
         "MAP:G124",
         "TIME:19:28:44",
         "UNIT:BN1,E2,EMS1,R2");
   }
   
   @Test
   public void testScottTitus() {
 
     doTest("T1",
         "EVENT TYPE: OUTSIDE FIRE-BRUSH Location: COLFAX LN/TOWN TER NPRT G544 TIME: 17:26:42 Disp: BN8,DOF,E81,T83,TRK84",
         "CALL:OUTSIDE FIRE-BRUSH",
         "ADDR:COLFAX LN & TOWN TER",
         "CITY:NORTH PORT",
         "MAP:G544",
         "TIME:17:26:42",
         "UNIT:BN8,DOF,E81,T83,TRK84");
 
     doTest("T2",
         "EVENT TYPE: STRUCTURE FIRE Location: 2633 RIDGEWOOD DR NPRT G544 TIME: 01:26:04",
         "CALL:STRUCTURE FIRE",
         "ADDR:2633 RIDGEWOOD DR",
         "CITY:NORTH PORT",
         "MAP:G544",
         "TIME:01:26:04");
 
     doTest("T3",
         "EVENT TYPE: STRUCTURE FIRE Location: PAN AMERICAN BLVD/LA BREA ST NPRT G565 TIME: 19:07:40",
         "CALL:STRUCTURE FIRE",
         "ADDR:PAN AMERICAN BLVD & LA BREA ST",
         "CITY:NORTH PORT",
         "MAP:G565",
         "TIME:19:07:40");
   }
   
   @Test
  public void testBillHoad() {
 
     doTest("T1",
         "SARASOTA COUNTY EVENT TYPE: VEHICLE FIRE Location: 4700 FORBES TRL AREA: G434 MUN: SCTY TIME: 12:12:10\\2sDisp: B26,BN4,E21,E24,E26,R21",
         "CALL:VEHICLE FIRE",
         "ADDR:4700 FORBES TRL",
         "MAP:G434",
         "TIME:12:12:10\\2sDisp: B26,BN4,E21,E24,E26,R21");
 
   }
   
   @Test
   public void testLeeWhitehurst() {
     
   }
   
   public static void main(String[] args) {
     new FLSarasotaCountyParserTest().generateTests("T1");
   }
 }
   
