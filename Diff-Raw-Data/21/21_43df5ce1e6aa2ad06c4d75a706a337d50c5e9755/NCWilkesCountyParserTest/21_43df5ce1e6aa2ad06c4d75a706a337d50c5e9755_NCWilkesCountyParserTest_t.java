 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import org.junit.Test;
 
 public class NCWilkesCountyParserTest extends BaseParserTest {
   
   public NCWilkesCountyParserTest() {
     setParser(new NCWilkesCountyParser(), "WILKES COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
     doTest("T1",
         "FRM:CAD@wilkes.nc.local\nMSG:CAD:WCFD;741;05/09/2011 14:38:38;UNRESPONSIVE;100 WINDING TRAILS PL",
         "SRC:WCFD",
         "ID:741",
        "DATE:05/09/2011",
        "TIME:14:38:38",
         "CALL:UNRESPONSIVE",
         "ADDR:100 WINDING TRAILS PL");
 
     doTest("T2",
         "FRM:CAD@wilkes.nc.local\nMSG:CAD:WCFD;727;05/09/2011 13:03:25;UNRESPONSIVE;1016 FLETCHER ST",
         "SRC:WCFD",
         "ID:727",
        "DATE:05/09/2011",
        "TIME:13:03:25",
         "CALL:UNRESPONSIVE",
         "ADDR:1016 FLETCHER ST");
 
     doTest("T3",
         "FRM:CAD@wilkes.nc.localf\nMSG:CAD:WCFD;12;05/06/2011 04:02:34;FIRE ALARM;1605 CURTIS BRIDGE RD",
         "SRC:WCFD",
         "ID:12",
        "DATE:05/06/2011",
        "TIME:04:02:34",
         "CALL:FIRE ALARM",
         "ADDR:1605 CURTIS BRIDGE RD");
 
     doTest("T4",
         "FRM:CAD@wilkes.nc.local\nMSG:CAD:WCFD;702;05/04/2011 14:53:07;ACCIDENT W/INJURY;1500 RIVER ST",
         "SRC:WCFD",
         "ID:702",
        "DATE:05/04/2011",
        "TIME:14:53:07",
         "CALL:ACCIDENT W/INJURY",
         "ADDR:1500 RIVER ST");

    doTest("T5",
        "CAD:WCFD;396;07/07/2012 01:47:54;HEART ATTACK;298 DRAGWAY RD",
        "SRC:WCFD",
        "ID:396",
        "DATE:07/07/2012",
        "TIME:01:47:54",
        "CALL:HEART ATTACK",
        "ADDR:298 DRAGWAY RD");
   }
 
   public static void main(String[] args) {
     new NCWilkesCountyParserTest().generateTests("T1");
   }
 }
