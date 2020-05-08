 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYNassauCountyCParserTest extends BaseParserTest {
   
   public NYNassauCountyCParserTest() {
     setParser(new NYNassauCountyCParser(), "NASSAU COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "* 3- AUTO ACCIDENT W/AIDED * POST ROAD & JERICHO TURNPIKE  POST ROAD CS: JERICHO TURNPIKE TOA: 14:03 04-07-11",
         "CALL:3- AUTO ACCIDENT W/AIDED",
         "PLACE:POST ROAD",
         "ADDR:POST ROAD & JERICHO TURNPIKE",
         "X:JERICHO TURNPIKE");
 
     doTest("T2",
         "* 16- AFA * PUBLIC STORAGE 1055 STEWART AVENUE CS: MERCHANTS CONCOURSE \\ SOUTH STREET TOA: 15:58 04-07-11",
         "CALL:16- AFA",
         "PLACE:PUBLIC STORAGE",
         "ADDR:1055 STEWART AVENUE",
         "X:MERCHANTS CONCOURSE \\ SOUTH STREET");
 
     doTest("T3",
         "* 8- MUTUAL AID RESCUE * LAND LN CS: CARMAN AVENUE TOA: 11:41 04-08-11",
         "CALL:8- MUTUAL AID RESCUE",
         "ADDR:LAND LN",
         "X:CARMAN AVENUE");
 
     doTest("T4",
         "* 3- AUTO ACCIDENT W/ AIDED * TACO BELL #3204 23 OLD COUNTRY ROAD CS: GRAND STREET \\ CENTRAL AVENUE TOA: 13:50 04-08-11",
         "CALL:3- AUTO ACCIDENT W/ AIDED",
         "PLACE:TACO BELL #3204",
         "ADDR:23 OLD COUNTRY ROAD",
         "X:GRAND STREET \\ CENTRAL AVENUE");
 
     doTest("T5",
         "* 4- SIGNAL 9  * LLOYD L. LANGLEY 248 SHERMAN STREET CS: BROADWAY \\ ROMAN AVENUE TOA: 19:47 04-08-11",
         "CALL:4- SIGNAL 9",
         "PLACE:LLOYD L LANGLEY",
         "ADDR:248 SHERMAN STREET",
         "X:BROADWAY \\ ROMAN AVENUE");
  }
   
   public static void main(String[] args) {
     new NYNassauCountyCParserTest().generateTests("T1");
   }
 }
