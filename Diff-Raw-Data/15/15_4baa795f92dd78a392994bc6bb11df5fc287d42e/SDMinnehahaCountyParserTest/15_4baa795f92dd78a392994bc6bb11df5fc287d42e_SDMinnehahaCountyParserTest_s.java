 package net.anei.cadpage.parsers.SD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class SDMinnehahaCountyParserTest extends BaseParserTest {
   
   public SDMinnehahaCountyParserTest() {
     setParser(new SDMinnehahaCountyParser(), "MINNEHAHA COUNTY", "SD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "930 Quad 2070 - VS 100 CLIFF AVE 2 VS Unconsc/Faint C3 2011-00000870",
         "SRC:930",
         "MAP:Quad 2070",
         "ADDR:100 CLIFF AVE 2",
         "CITY:VALLEY SPRINGS",
         "CALL:Unconsc / Faint",
         "CODE:C3",
         "ID:2011-00000870");
 
     doTest("T2",
         "930 Quad 460 - BR 904 SNOWBERRY AVE BR Conv Seizure C3 2011-00000374",
         "SRC:930",
         "MAP:Quad 460",
         "ADDR:904 SNOWBERRY AVE",
         "CITY:BRANDON",
         "CALL:Conv Seizure",
         "CODE:C3",
         "ID:2011-00000374");
 
     doTest("T3",
         "930 Quad 2060 - VS I 90 MM 410 VS Sick Person C1 2011-00000028",
         "SRC:930",
         "MAP:Quad 2060",
         "ADDR:I 90 MM 410",
         "CITY:VALLEY SPRINGS",
         "CALL:Sick Person",
         "CODE:C1",
         "ID:2011-00000028");
 
     doTest("T4",
         "930 Quad 2060 - VS 26472 484TH AVE VS Diabetic C2 2011-00000003",
         "SRC:930",
         "MAP:Quad 2060",
         "ADDR:26472 484TH AVE",
         "CITY:VALLEY SPRINGS",
         "CALL:Diabetic",
         "CODE:C2",
         "ID:2011-00000003");
 
     doTest("T5",
         "930 Quad 2010 - SR 26525 482ND AVE BR Medical Emergency 2011-00000621",
         "SRC:930",
         "MAP:Quad 2010",
         "ADDR:26525 482ND AVE",
         "CITY:BRANDON",
         "CALL:Medical Emergency",
         "ID:2011-00000621");
 
     doTest("T6",
         "930 Quad 460 - BR 3008 E ASPEN BLVD BR Falls C1 2011-00000051",
         "SRC:930",
         "MAP:Quad 460",
         "ADDR:3008 E ASPEN BLVD",
         "CITY:BRANDON",
         "CALL:Falls",
         "CODE:C1",
         "ID:2011-00000051");
 
     doTest("T7",
         "930 Quad 460 - BR 1625 RUSHMORE DR BR Family Dispute 2011-00000347",
         "SRC:930",
         "MAP:Quad 460",
         "ADDR:1625 RUSHMORE DR",
         "CITY:BRANDON",
         "CALL:Family Dispute",
         "ID:2011-00000347");
 
     doTest("T8",
         "(Dispatch Page) BR  Quad 460 - BR 1413 RUSHMORE DR 12 BR Falls C1 2011-00000111",
         "UNIT:BR",
         "MAP:Quad 460",
         "ADDR:1413 RUSHMORE DR 12",
         "CITY:BRANDON",
         "CALL:Falls",
         "CODE:C1",
         "ID:2011-00000111");
 
     doTest("T9",
         "(Dispatch Page) Quad 800 - HD N WESTERN AVE OAKS DR HD Injury Accident C3 2011-00000163",
         "MAP:Quad 800",
        "ADDR:N WESTERN AVE OAKS DR",
         "CALL:Injury Accident",
         "CODE:C3",
         "ID:2011-00000163");
 
     doTest("T10",
         "(Dispatch Page) DR EM  Quad 660 - DR I 29 MM 98DR Inj Accident C1 + Fire 2011-00000192",
         "UNIT:DR EM",
         "MAP:Quad 660",
         "ADDR:I 29 MM 98",
         "CITY:DELL RAPIDS",
         "CALL:Inj Accident C1 + Fire",
         "ID:2011-00000192");
 
     doTest("T11",
         "(Dispatch Page) CO LY HD EM  Quad 550 - CO 46166 250TH ST CO Structure Fire 2011-00000091",
         "UNIT:CO LY HD EM",
         "MAP:Quad 550",
         "ADDR:46166 250TH ST",
         "CITY:COLTON",
         "CALL:Structure Fire",
         "ID:2011-00000091");
 
     doTest("T12",
         "(Dispatch Page) HU HD EM  Quad 940 - HU I 90 MM 382HU Structure Fire 2011-00000085",
         "UNIT:HU HD EM",
         "MAP:Quad 940",
         "ADDR:I 90 MM 382",
         "CITY:HUMBOLT",
         "CALL:Structure Fire",
         "ID:2011-00000085");
  }
   
   public static void main(String[] args) {
    new SDMinnehahaCountyParserTest().generateTests("T13", "SRC UNIT MAP ADDR CITY CALL CODE ID");
   }
 }
