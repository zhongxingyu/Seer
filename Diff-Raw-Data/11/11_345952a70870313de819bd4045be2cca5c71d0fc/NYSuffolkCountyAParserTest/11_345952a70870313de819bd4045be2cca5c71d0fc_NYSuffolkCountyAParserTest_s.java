 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NY.NYSuffolkCountyAParser;
 
 import org.junit.Test;
 
 
 public class NYSuffolkCountyAParserTest extends BaseParserTest {
   
   public NYSuffolkCountyAParserTest() {
     setParser(new NYSuffolkCountyAParser(), "SUFFOLK COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "TYPE: GAS LEAKS / GAS ODOR (NATURAL / L.P.G.) LOC: 11 BRENTWOOD PKWY BRENTW HOMELESS SHELTER CROSS: PENNSYLVANIA AV / SUFFOLK AV CODE: 60-B-2 TIME: 12:54:16",
         "CALL:GAS LEAKS / GAS ODOR (NATURAL / L.P.G.)",
         "PLACE:HOMELESS SHELTER",
         "ADDR:11 BRENTWOOD PKWY",
         "CITY:Brentwood",
         "X:PENNSYLVANIA AV / SUFFOLK AV",
         "CODE:60-B-2");
     
     doTest("T2",
         "TYPE: STRUCTURE FIRE LOC: 81 NEW HAMPSHIRE AV NBAYSH  CROSS: E FORKS RD / E 3 AV CODE: 69-D-10 TIME: 16:36:48",
         "CALL:STRUCTURE FIRE",
         "ADDR:81 NEW HAMPSHIRE AV",
         "X:E FORKS RD / E 3 AV",
         "CITY:Bay Shore",
         "CODE:69-D-10");
     
     doTest("T3",
         "TYPE: OPEN BURNING LOC: 65 GRANT AVE BRENTW CROSS: SUFFOLK AVE CODE: 54-C-6 TIME: 18:39:20",
         "CALL:OPEN BURNING",
         "ADDR:65 GRANT AVE",
         "X:SUFFOLK AVE",
         "CITY:Brentwood",
         "CODE:54-C-6");
  
     doTest("T4",
         "TYPE: PREGNANCY / CHILDBIRTH / MISCARRIAGE LOC: 330 MOTOR PKWY HAUPPA:@FELDMAN, KRAMER & MONACO STE 400  CROSS: WASHINGTON AV / MARCUS BLVD C",
         "CALL:PREGNANCY / CHILDBIRTH / MISCARRIAGE",
         "PLACE:FELDMAN, KRAMER & MONACO STE 400",
         "ADDR:330 MOTOR PKWY",
         "X:WASHINGTON AV / MARCUS BLVD",
         "CITY:Hauppauge");
 
     doTest("T5",
         "TYPE: PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE LOC: 200 WIRELESS BLVD HAUPPA: @SOCIAL SERVICES HAUPPAUGE INTERVIEW AREA CROSS: MORELAND RD /",
         "CALL:PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE",
         "PLACE:SOCIAL SERVICES HAUPPAUGE INTERVIEW AREA",
         "ADDR:200 WIRELESS BLVD",
         "X:MORELAND RD /",
         "CITY:Hauppauge");
 
     doTest("T10",
         "FAINTING (NEAR) LOC: 46 SAUNDERS AV CENTEM:@KINGS CHAPEL CHURCH  CROSS: ROWEIN RD CODE: 31-D-2 TIME: 17:24:12",
         "CALL:FAINTING (NEAR)",
         "ADDR:46 SAUNDERS AV",
         "CITY:Center Moriches",
         "PLACE:KINGS CHAPEL CHURCH",
         "X:ROWEIN RD",
         "CODE:31-D-2");
 
     doTest("T11",
         "SEIZURES LOC: 20 TRAINOR AV CENTEM CROSS: SUNRISE HWY S  BERNSTEIN BLVD CODE: 12-C-1 TIME: 03:39:02",
         "CALL:SEIZURES",
         "ADDR:20 TRAINOR AV",
         "CITY:Center Moriches",
         "X:SUNRISE HWY S  BERNSTEIN BLVD",
         "CODE:12-C-1");
         
     doTest("T12",
         "ABDOMINAL PAINS LOC: 18 INWOOD RD CENTEM CROSS: UNION AV / BEACHFERN RD CODE: 1-A-1 TIME: 21:21:39",
         "CALL:ABDOMINAL PAINS",
         "ADDR:18 INWOOD RD",
         "CITY:Center Moriches",
         "X:UNION AV / BEACHFERN RD",
         "CODE:1-A-1");
 
     doTest("T13",
         "UNKNOWN PROBLEM LOC: 150 CHICHESTER AV CENTEM CROSS: YARMOUTH LN / FROWEIN RD CODE: TIME: 08:01:10",
         "CALL:UNKNOWN PROBLEM",
         "ADDR:150 CHICHESTER AV",
         "CITY:Center Moriches",
         "X:YARMOUTH LN / FROWEIN RD");
 
     doTest("T14",
         "FAINTING (NEAR) LOC: 6 FROWEIN RD EMORIC: @CEDAR LODGE NURSING HOME IN THE DINING ROOM CROSS: WALDEN CT  OAK ST CODE: 31-D-3 TIME: 18:51:38",
         "CALL:FAINTING (NEAR)",
         "ADDR:6 FROWEIN RD",
         "CITY:East Moriches",
         "PLACE:CEDAR LODGE NURSING HOME IN THE DINING ROOM",
         "X:WALDEN CT  OAK ST",
         "CODE:31-D-3");
         
     doTest("T15",
         "GAS ODOR (NATURAL  L.P.G.) LOC: MCGRAW ST SHIRLE I/V/O SHIRLEY PLAZA CROSS: GRAND AV / OAK AV CODE: 60-C-2 TIME: 02:06:34",
         "CALL:GAS ODOR (NATURAL  L.P.G.)",
         "ADDR:MCGRAW ST",
         "CITY:Shirley",
         "PLACE:I / V / O SHIRLEY PLAZA",
         "X:GRAND AV / OAK AV",
         "CODE:60-C-2");
         
     doTest("T16",
         "FAINTING (NEAR) LOC: 53-10 LONG TREE LN MORICH: @PINE HILLS SOUTH CLUBHOUSE COMMUNITY ROOM CROSS:    CODE: 31-E-1 TIME: 14:54:18",
         "CALL:FAINTING (NEAR)",
         "ADDR:53-10 LONG TREE LN",
         "CITY:Moriches",
         "PLACE:PINE HILLS SOUTH CLUBHOUSE COMMUNITY ROOM",
         "CODE:31-E-1");
     
     doTest("MN1",
         "TYPE: PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE LOC: 79 ABBOTT AV MASTIC ***_VIP_***: CROSS: ELGIN ST / FOXCROFT ST CODE: 25-B-6 TIME: 14:36:05",
         "CALL:PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE",
         "ADDR:79 ABBOTT AV",
         "CITY:Mastic",
         "PLACE:***_VIP_***",
         "X:ELGIN ST / FOXCROFT ST",
         "CODE:25-B-6");
 
     doTest("T17",
         "TYPE: ALARMS LOC: 100 PATRICIA CT OAKDAL @OAKDALE APARTMENTS    APARTMENT 3 CROSS: RACE PL /  CODE: 52-C-1S TIME: 19:16:55",
         "CALL:ALARMS",
         "ADDR:100 PATRICIA CT",
         "CITY:Oakdale",
         "PLACE:OAKDALE APARTMENTS    APARTMENT 3",
         "X:RACE PL /",
         "CODE:52-C-1S");
 
     doTest("T18",
         "FWD: TYPE: STRUCTURE FIRE LOC: 1 WILBUR AV MANORV  CROSS: SOHMER ST /  CODE: 69-D-5 TIME: 17:17:43",
         "CALL:STRUCTURE FIRE",
         "ADDR:1 WILBUR AV",
         "CITY:Manorville",
         "X:SOHMER ST /",
         "CODE:69-D-5");
 
     doTest("T19",
         "TYPE: MOTOR VEHICLE ACCIDENT  CROSS: CHURCH ST / LAKELAND AV CODE: 29-B-1U TIME: 09:04:01",
         "CALL:MOTOR VEHICLE ACCIDENT",
         "ADDR:CHURCH ST & LAKELAND AV",
         "CODE:29-B-1U");
 
     doTest("T20",
         "TYPE: ALARMS LOC: 311 BAY AV EPATCH: @BAY HOUSE  CROSS: NEWINS ST / PARK ST CODE: 52-C-3G TIME: 15:33:58\n\n",
         "CALL:ALARMS",
         "ADDR:311 BAY AV",
         "CITY:East Patchogue",
         "PLACE:BAY HOUSE",
         "X:NEWINS ST / PARK ST",
         "CODE:52-C-3G");
   }
   
   public static void main(String[] args) {
    new NYSuffolkCountyAParserTest().generateTests("T20", "CALL ADDR CITY PLACE X CODE");
   }
 }
