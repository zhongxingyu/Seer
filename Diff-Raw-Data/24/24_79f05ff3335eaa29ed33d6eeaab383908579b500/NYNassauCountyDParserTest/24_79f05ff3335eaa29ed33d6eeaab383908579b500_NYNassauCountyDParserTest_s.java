 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYNassauCountyDParserTest extends BaseParserTest {
   
   public NYNassauCountyDParserTest() {
     setParser(new NYNassauCountyDParser(), "NASSAU COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "*** 30) RESCUE-SIGNAL 9 *** 21 BELMONT AVE CS: DAVID CT TOA: 18:39 03-29-11",
         "CALL:30) RESCUE-SIGNAL 9",
         "ADDR:21 BELMONT AVE",
         "X:DAVID CT");
 
     doTest("T2",
         "*** 45) BRUSH *** 1 MILLER RD CS: HEMPSTEAD TPKE TOA: 14:48 03-29-11",
         "CALL:45) BRUSH",
         "ADDR:1 MILLER RD",
         "X:HEMPSTEAD TPKE");
 
     doTest("T3",
         "*** 11) AFA -HOUSE *** 21 CLARK ST CS: EILEEN AVE TOA: 08:09 03-29-11",
         "CALL:11) AFA -HOUSE",
         "ADDR:21 CLARK ST",
         "X:EILEEN AVE");
 
     doTest("T4",
         "*** 75) CARBON MONOXIDE ALARM *** 91 SUNBEAM AVE CS: SUNNY LN TOA: 04:08 03-26-11",
         "CALL:75) CARBON MONOXIDE ALARM",
         "ADDR:91 SUNBEAM AVE",
         "X:SUNNY LN");
 
     doTest("T5",
         "*** 13) SMOKE INVESTIGATION *** 56 APOLLO CIR CS: HICKEY BLVD TOA: 22:16 03-26-11",
         "CALL:13) SMOKE INVESTIGATION",
         "ADDR:56 APOLLO CIR",
         "X:HICKEY BLVD");
 
     doTest("T6",
         "*** 82) MUTUAL AID *** 930 HICKSVILLE FD TO STANDBY CS: CAROLINE ST TOA: 10:10 03-26-11",
         "CALL:82) MUTUAL AID",
         "ADDR:930 HICKSVILLE FD TO STANDBY",
         "X:CAROLINE ST");
 
     doTest("T7",
         "*** 20) BUILDING FIRE *** 4105 HEMPSTEAD TPKE CS: HICKSVILLE RD TOA: 20:48 03-21-11",
         "CALL:20) BUILDING FIRE",
         "ADDR:4105 HEMPSTEAD TPKE",
         "X:HICKSVILLE RD");
 
     doTest("T8",
         "*** 61) TRUCK FIRE *** HICKSVILLE RD CS: MARTIN RD N TOA: 19:35 03-21-11",
         "CALL:61) TRUCK FIRE",
         "ADDR:HICKSVILLE RD",
         "X:MARTIN RD N");
 
     doTest("T9",
         "*** 10) HOUSE FIRE *** 22 CRESTLINE AVE CS: HILLTOP AVE TOA: 15:41 03-21-11",
         "CALL:10) HOUSE FIRE",
         "ADDR:22 CRESTLINE AVE",
         "X:HILLTOP AVE");
 
     doTest("T10",
         "*** 10) HOUSE FIRE *** 212 GILLING RD CS: WHELAN PL TOA: 14:33 03-20-11",
         "CALL:10) HOUSE FIRE",
         "ADDR:212 GILLING RD",
         "X:WHELAN PL");
 
     doTest("T11",
         "FRM:paging@bethpagefd.xohost.com\nMSG:\n2011-000418 *** 21)  AFA BUILDING *** BETHPAGE HIGH SCHOOL 800 STEWART AVE CS: SYCAMORE AVE TOA: 12:03 04-02-11 STATION 4",
         "ID:2011-000418",
         "CALL:21)  AFA BUILDING",
         "PLACE:BETHPAGE HIGH SCHOOL",
         "ADDR:800 STEWART AVE",
         "X:SYCAMORE AVE",
         "INFO:STATION 4");
 
     doTest("T12",
         "1 of 2\nFRM:paging@bethpagefd.xohost.com\nMSG:\n2011-000530 *** 30) RESCUE-SIGNAL 9 *** MR ANTHONY MORACE 141 HERMANN AVE N CS: SOPHIA ST TOA:\n(Con't) 2 of 2\n09:52 04-22-11 HEADQUARTERS(End)",
         "ID:2011-000530",
         "CALL:30) RESCUE-SIGNAL 9",
         "PLACE:MR ANTHONY MORACE",
         "ADDR:141 HERMANN AVE N",
         "X:SOPHIA ST",
         "INFO:HEADQUARTERS");
 
     doTest("T13",
         "*** 30 - RESCUE *** CIARDI 41 AMHERST DR CS: N WISCONSIN AVE  / HARRIET PL C-6 TOA: 18:23 05/12/11 2011-000368 3/18/30/38/52 Amherst Drive",
         "CALL:30 - RESCUE",
         "PLACE:CIARDI",
         "ADDR:41 AMHERST DR",
         "X:N WISCONSIN AVE  / HARRIET PL C-6",
         "ID:2011-000368",
         "INFO:3/18/30/38/52 Amherst Drive");
 
     doTest("T14",
         "*** 330 RESCUE - STROKE *** XYZ RESIDENCE 460 PIPING ROCK RD CS: RUSSET RD  - WELLWOOD RD N-4 TOA: 09:28 05-23-11 STATION 2 STATION 4 STATION 3 2011-000877",
         "ID:2011-000877",
         "CALL:330 RESCUE - STROKE",
         "PLACE:XYZ RESIDENCE",
         "ADDR:460 PIPING ROCK RD",
         "X:RUSSET RD  - WELLWOOD RD N-4",
         "INFO:STATION 2 STATION 4 STATION 3");
 
     doTest("T15",
         "*** 400 AUTO ACCIDENT *** WATCHTOWER LN & N JERUSALEM RD  WATCHTOWER LN CS: N JERUSALEM RD F-7 TOA: 08:53 05-23-11 STATION 2 STATION 4 STATION 1 2011-000876",
         "ID:2011-000876",
         "CALL:400 AUTO ACCIDENT",
         "ADDR:WATCHTOWER LN & N JERUSALEM RD WATCHTOWER LN",
         "X:N JERUSALEM RD F-7",
         "INFO:STATION 2 STATION 4 STATION 1");
 
     doTest("T16",
         "*** 630 INVESTIGATION-SMOKE EXT *** XYZ RESIDENCE 39 EDEN LN CS: ELM DR W  - ELM DR E K-3 TOA: 17:11 05-22-11 STATION 2 STATION 4 2011-000873",
         "ID:2011-000873",
         "CALL:630 INVESTIGATION-SMOKE EXT",
         "PLACE:XYZ RESIDENCE",
         "ADDR:39 EDEN LN",
         "X:ELM DR W  - ELM DR E K-3",
         "INFO:STATION 2 STATION 4");
 
     doTest("T17",
         "*** 210 MAJOR-BLDG FIRE AUTO ALM *** UMBERTO'S PIZZA 1180 WANTAGH AVE CS: DUCKPOND DR  - JERUSALEM AVE H-11 TOA: 06:34 05-22-11 STATION 2 STATION 4 2011-000868",
         "ID:2011-000868",
         "CALL:210 MAJOR-BLDG FIRE AUTO ALM",
         "PLACE:UMBERTO'S PIZZA",
         "ADDR:1180 WANTAGH AVE",
         "X:DUCKPOND DR  - JERUSALEM AVE H-11",
         "INFO:STATION 2 STATION 4");
  }
   
   public static void main(String[] args) {
    new NYNassauCountyDParserTest().generateTests("T15");
   }
 }
