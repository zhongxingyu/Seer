 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCAlexanderCountyParserTest extends BaseParserTest {
   
   public NCAlexanderCountyParserTest() {
     setParser(new NCAlexanderCountyParser(), "ALEXANDER COUNTY", "NC");
   }
   
   @Test
   public void testBad() {
     doBadTest("CAD:");
     doBadTest("CAD:ANYONE WANTING TO PRACTICE SOFTBALL...MEET AT GWALTNEYS FIELD AT 1800 HRS THIS DATE...");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(CAD:91 GREEN ACRES LN;FALL;31D03;ICARD RIDGE RD)",
         "ADDR:91 GREEN ACRES LN",
         "CALL:FALL",
         "CODE:31D03",
         "X:ICARD RIDGE RD");
 
     doTest("T2",
         "CAD:2370 NC 90 HWY E;NC DEPT OF TRANSPORTATION;TRAFFIC ACCIDENT;ADAMS POND LN;WINTERHAVEN RD",
         "ADDR:2370 NC 90 HWY E",
         "PLACE:NC DEPT OF TRANSPORTATION",
         "CALL:TRAFFIC ACCIDENT",
         "X:ADAMS POND LN & WINTERHAVEN RD");
 
     doTest("T3",
         "CAD:1522 BLACK OAK RIDGE RD;STANDARD STRUCTURE FIRE;JONES RD;CARSON CHAPEL RD",
         "ADDR:1522 BLACK OAK RIDGE RD",
         "CALL:STANDARD STRUCTURE FIRE",
         "X:JONES RD & CARSON CHAPEL RD");
 
     doTest("T4",
         "CAD:400 LEE MATHESON RD;HEART PROBLEMS;19D04;BOSTON RD;TAYLORSVILLE MFG RD",
         "ADDR:400 LEE MATHESON RD",
         "CALL:HEART PROBLEMS",
         "CODE:19D04",
         "X:BOSTON RD & TAYLORSVILLE MFG RD");
 
     doTest("T5",
         "CAD:33 LEWITTES RD;PIEDMONT FIBERGLASS;LARGE ALARM;NC 90 HWY E;RUSSELL LN",
         "ADDR:33 LEWITTES RD",
         "PLACE:PIEDMONT FIBERGLASS",
         "CALL:LARGE ALARM",
         "X:NC 90 HWY E & RUSSELL LN");
 
     doTest("T6",
         "|CAD:80 PRESLAR LN;BREATHING PROBLEMS;06D02;NC 127 HWY",
         "ADDR:80 PRESLAR LN",
         "CALL:BREATHING PROBLEMS",
         "CODE:06D02",
         "X:NC 127 HWY");
 
     doTest("T7",
         "CAD:350 SCHOOL DR;TAYLORSVILLE HOUSE;LARGE ALARM;LILEDOUN RD;E JAY DR",
         "ADDR:350 SCHOOL DR",
         "PLACE:TAYLORSVILLE HOUSE",
         "CALL:LARGE ALARM",
         "X:LILEDOUN RD & E JAY DR");
 
     doTest("T8",
         "CAD:65 MOONEY DR;TRAUMATIC INJURIES;30A02;US 64 90 HWY W",
         "ADDR:65 MOONEY DR",
         "CALL:TRAUMATIC INJURIES",
         "CODE:30A02",
         "X:US 64 90 HWY W");
 
     doTest("T9",
         "CAD:224 CRESTVIEW DR/US 64 HWY W;TRAFFIC ACCIDENT;29B04",
         "ADDR:224 CRESTVIEW DR & US 64 HWY W",
         "CALL:TRAFFIC ACCIDENT",
         "CODE:29B04");
 }
 
   public static void main(String[] args) {
    new NCAlexanderCountyParserTest().generateTests("T9", "ADDR PLACE CALL CODE X");
   }
 }
