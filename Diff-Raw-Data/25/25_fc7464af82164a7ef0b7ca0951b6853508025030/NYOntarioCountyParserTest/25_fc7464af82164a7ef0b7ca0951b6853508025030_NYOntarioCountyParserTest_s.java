 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYOntarioCountyParserTest extends BaseParserTest {
   
   public NYOntarioCountyParserTest() {
     setParser(new NYOntarioCountyParser(), "ONTARIO COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "AUTO ALRM\n18:23\n5658 EAST LAKE RD\nSUNNY HOLLOW - WILLOW BEACH\nF14\nLOCUS RES - GENERAL FIRE ALARM - ATT REP\nF111890007",
         "CALL:AUTO ALRM",
         "ADDR:5658 EAST LAKE RD",
         "X:SUNNY HOLLOW - WILLOW BEACH",
         "UNIT:F14",
         "INFO:LOCUS RES - GENERAL FIRE ALARM - ATT REP",
         "ID:F111890007");
 
     doTest("T2",
         "CRASH UNK\n14:32\nCOUNTY ROAD 37 - ROUTE 20A\nASHLEY RD - ROUTE 20A\nE14\nMERCEDES IN THE DITCH ALSO ANOTHER CAR INVOLVED - FEMALE WAS GETTING OUT OF THE MERCE",
         "CALL:CRASH UNK",
         "ADDR:COUNTY ROAD 37 & ROUTE 20A",
         "X:ASHLEY RD - ROUTE 20A",
         "UNIT:E14",
         "INFO:MERCEDES IN THE DITCH ALSO ANOTHER CAR INVOLVED - FEMALE WAS GETTING OUT OF THE MERCE");
 
     doTest("T3",
         "CRASH-PI\n13:56\nBIG TREE RD - BARNARD RD\nBARNARD RD - COUNTY LINE\nE14\nMOTORCYCLE CRASH - 1 INJURED\nE111910021",
         "CALL:CRASH-PI",
         "ADDR:BIG TREE RD & BARNARD RD",
         "X:BARNARD RD - COUNTY LINE",
         "UNIT:E14",
         "INFO:MOTORCYCLE CRASH - 1 INJURED",
         "ID:E111910021");
 
     doTest("T4",
         "WIRES DOWN\n15:16\nCOUNTY ROAD 37 - RASPBERRY HILL\nRASPBERRY HILL - HOWCROFT RD\nF14\nWIRE DOWN ABOUT 200 METERS SOUTH OF RASPBERRY HILL ROAD ON CR 37 CAUSING",
         "CALL:WIRES DOWN",
         "ADDR:COUNTY ROAD 37 & RASPBERRY HILL",
         "X:RASPBERRY HILL - HOWCROFT RD",
         "UNIT:F14",
         "INFO:WIRE DOWN ABOUT 200 METERS SOUTH OF RASPBERRY HILL ROAD ON CR 37 CAUSING");
 
     doTest("T5",
         "CO DET.\n08:20\n4910 COUNTY ROAD 36\nMASTIN DR - SANDY BOTTOM RD\nF14\nWOKE UP TO CO DETECTOR GOING OFF, NO ILLNESS, WILL WAIT OUTSIDE\nF111960002",
         "CALL:CO DET.",
         "ADDR:4910 COUNTY ROAD 36",
         "X:MASTIN DR - SANDY BOTTOM RD",
         "UNIT:F14",
         "INFO:WOKE UP TO CO DETECTOR GOING OFF, NO ILLNESS, WILL WAIT OUTSIDE",
         "ID:F111960002");
  }
   
   public static void main(String[] args) {
     new NYOntarioCountyParserTest().generateTests("T1");
   }
 }
