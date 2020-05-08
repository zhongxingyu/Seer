 package net.anei.cadpage.parsers.CA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CAMontereyCountyParserTest extends BaseParserTest {
   
   public CAMontereyCountyParserTest() {
     setParser(new CAMontereyCountyParser(), "MONTEREY COUNTY", "CA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:52161HA - MEO:NOT XFR - 13200 CIELO AZULUnits:E5211, 52A",
         "MAP:52161HA",
         "CALL:MEO:NOT XFR",
         "ADDR:13200 CIELO AZUL",
         "UNIT:E5211, 52A");
 
     doTest("T2",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:5261BT - MEO:MEDICAL EMERGNCY - 7881 SANDHOLDT RDUnit:CH5202",
         "MAP:5261BT",
         "CALL:MEO:MEDICAL EMERGNCY",
         "ADDR:7881 SANDHOLDT RD",
         "UNIT:CH5202");
 
     doTest("T3",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:5254A - MEO:NOT XFRD - 315 NEPONSET RDUnits:E5211, 52A",
         "MAP:5254A",
         "CALL:MEO:NOT XFRD",
         "ADDR:315 NEPONSET RD",
         "UNIT:E5211, 52A");
 
     doTest("T4",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:524451A - MEO:XFRD - 97 RAILROAD AVUnits:E5213, 52A",
         "MAP:524451A",
         "CALL:MEO:XFRD",
         "ADDR:97 RAILROAD AV",
         "UNIT:E5213, 52A");
 
     doTest("T5",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:52251AB - UIA:UNK INJ ACC - HWY 101/SAN MIGUEL CANYON RDUnits:E5212, E5213, 52V",
         "MAP:52251AB",
         "CALL:UIA:UNK INJ ACC",
         "ADDR:HWY 101 & SAN MIGUEL CANYON RD",
         "UNIT:E5212, E5213, 52V");
 
     doTest("T6",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:FMA:FIRE MUTUAL AID - 918 FREEDOM BLVDUnit:E5213",
         "CALL:FMA:FIRE MUTUAL AID",
         "ADDR:918 FREEDOM BLVD",
         "UNIT:E5213");
 
     doTest("T7",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:52351A - MEO:MEO ALM - 398 BERRY RDUnits:E5213, 52A",
         "MAP:52351A",
         "CALL:MEO:MEO ALM",
         "ADDR:398 BERRY RD",
         "UNIT:E5213, 52A");
 
     doTest("T8",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:5261AT - MEO:XFRD - 10561 MERRITT STUnits:E5211, 52A",
         "MAP:5261AT",
         "CALL:MEO:XFRD",
         "ADDR:10561 MERRITT ST",
         "UNIT:E5211, 52A");
 
     doTest("T9",
         "FRM:donotreply@co.monterey.ca.us\nSUBJ:CAD Page\nMSG:52251A - VIA:SB HWY 101 - HWY 101/CRAZY HORSE CANYON RDUnits:E5212, BEU, 52V\n",
         "MAP:52251A",
         "CALL:VIA:SB HWY 101",
         "ADDR:HWY 101 & CRAZY HORSE CANYON RD",
         "UNIT:E5212, BEU, 52V");

    doTest("T10",
        "(CAD Page) 64172B - SF:STRUCTURE FIRE - BARNET SEGAL LN/IRIS CANYON RD - MTY\n      Message: TYPE:VGF   -->SF",
        "MAP:64172B",
        "CALL:SF:STRUCTURE FIRE",
        "ADDR:BARNET SEGAL LN & IRIS CANYON RD",
        "UNIT:MTY",
        "INFO:TYPE:VGF   -->SF");

    doTest("T11",
        "(CAD Page) 5513 - SF:STRUCTURE FIRE - 17739 RIVERBEND RD - MCO",
        "MAP:5513",
        "CALL:SF:STRUCTURE FIRE",
        "ADDR:17739 RIVERBEND RD",
        "UNIT:MCO");
   }
   
   public static void main(String[] args) {
    new CAMontereyCountyParserTest().generateTests("T10", "MAP CALL ADDR UNIT INFO");
   }
 }
