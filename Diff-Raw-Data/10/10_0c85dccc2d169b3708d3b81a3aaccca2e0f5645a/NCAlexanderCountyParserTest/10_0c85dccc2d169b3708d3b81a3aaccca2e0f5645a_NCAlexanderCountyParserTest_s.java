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
         "MADDR:2370 NC 90 E",
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
         "MADDR:224 CRESTVIEW DR",
         "CALL:TRAFFIC ACCIDENT",
         "CODE:29B04");
 
     doTest("T10",
         "CAD:1 JENKINS MOOSE RD/LILEDOUN RD;WEATHER RELATED",
         "ADDR:1 JENKINS MOOSE RD & LILEDOUN RD",
         "MADDR:1 JENKINS MOOSE RD",
         "CALL:WEATHER RELATED");
 
     doTest("T11",
         "CAD:505 NC 16 HWY N;UNCONSCIOUS OR FAINTING;31D02;GRAVEL HILL CT;NAT GUARD ARMORY RD",
         "ADDR:505 NC 16 HWY N",
         "MADDR:505 NC 16 N",
         "CALL:UNCONSCIOUS OR FAINTING",
         "CODE:31D02",
         "X:GRAVEL HILL CT & NAT GUARD ARMORY RD");
 
     doTest("T12",
         "CAD:2818 NC 90 HWY E;CHEST PAIN;10D03;JESSICA LEE LN;WHITE PLAINS RD",
         "ADDR:2818 NC 90 HWY E",
         "MADDR:2818 NC 90 E",
         "CALL:CHEST PAIN",
         "CODE:10D03",
         "X:JESSICA LEE LN & WHITE PLAINS RD");
 
     doTest("T13",
         "CAD:21 MAIN AVENUE DR;BB&T BANK;TRAFFIC ACCIDENT;29D02m;E MAIN AV;TOWN PARK ST",
         "ADDR:21 MAIN AVENUE DR",
         "PLACE:BB&T BANK",
         "CALL:TRAFFIC ACCIDENT",
         "CODE:29D02m",
         "X:E MAIN AV & TOWN PARK ST");
 
     doTest("T14",
         "CAD:8510 NC 90 HWY E;SHURTAPE TECHNOLOGIES INC;TRAUMATIC INJURIES;30B02;SHUFORD RD;RURITAN PARK RD",
         "ADDR:8510 NC 90 HWY E",
         "MADDR:8510 NC 90 E",
         "PLACE:SHURTAPE TECHNOLOGIES INC",
         "CALL:TRAUMATIC INJURIES",
         "CODE:30B02",
         "X:SHUFORD RD & RURITAN PARK RD");
 
     doTest("T15",
         "CAD:7400 NC 16 HWY N;DEAL APPLE ORCHARDS;LZ SETUP;OLD NC 16 HWY;DEAL ORCHARD LN",
         "ADDR:7400 NC 16 HWY N",
         "MADDR:7400 NC 16 N",
         "PLACE:DEAL APPLE ORCHARDS",
         "CALL:LZ SETUP",
         "X:OLD NC 16 HWY & DEAL ORCHARD LN");
 
     doTest("T16",
         "CAD:FYI: ;93 TELEPHONE EXCHANGE RD;TRAUMATIC INJURIES;30B01;SPENCER LN;HONEY LN",
         "ADDR:93 TELEPHONE EXCHANGE RD",
         "CALL:TRAUMATIC INJURIES",
         "CODE:30B01",
         "X:SPENCER LN & HONEY LN");
 
     doTest("T17",
         "CAD:765 CARRIGAN RD;PREGNANCY OR CHILDBIRTH;24D03;HAPPY PLAINS RD;HARD ROCK LN",
         "ADDR:765 CARRIGAN RD",
         "CALL:PREGNANCY OR CHILDBIRTH",
         "CODE:24D03",
         "X:HAPPY PLAINS RD & HARD ROCK LN");
   }
 
   public static void main(String[] args) {
    new NCAlexanderCountyParserTest().generateTests("T18", "ADDR PLACE CALL CODE X");
   }
 }
