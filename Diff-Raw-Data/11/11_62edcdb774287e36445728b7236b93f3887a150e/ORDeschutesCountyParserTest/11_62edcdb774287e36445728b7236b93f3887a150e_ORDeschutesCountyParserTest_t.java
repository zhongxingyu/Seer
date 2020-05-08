 package net.anei.cadpage.parsers.OR;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.OR.ORDeschutesCountyParser;
 
 import org.junit.Test;
 
 
 public class ORDeschutesCountyParserTest extends BaseParserTest {
   
   public ORDeschutesCountyParserTest() {
     setParser(new ORDeschutesCountyParser(), "DESCHUTES COUNTY", "OR");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "Chest Pain Breathing normally => 35   -C-STA500-9395 SW SCOUT CAMP TRL-Map 131208-11:38:0",
         "CALL:Chest Pain Breathing normally => 35",
         "PRI:C",
         "SRC:STA500",
         "ADDR:9395 SW SCOUT CAMP TRL",
         "MAP:131208");
     
     doTest("T2",
         "Sick Person Vomiting   -A-STA500-13852 SW CANYON DR-Map 131216-18:47:56",
         "CALL:Sick Person Vomiting",
         "PRI:A",
         "SRC:STA500",
         "ADDR:13852 SW CANYON DR",
         "MAP:131216");
     
     doTest("T3",
         "DEAD BODY/DEATH R3-R3-STA500-14537 SW STALLION DR-Map 131223-10:23:46",
         "CALL:DEAD BODY/DEATH R3",
         "PRI:R3",
         "SRC:STA500",
         "ADDR:14537 SW STALLION DR",
         "MAP:131223");
     
     doTest("T4",
         "Falls Unk status Ground-B-STA500-13345 SW CINDER DR-Map 131215-00:30:04",
         "CALL:Falls Unk status Ground",
         "PRI:B",
         "SRC:STA500",
         "ADDR:13345 SW CINDER DR",
         "MAP:131215");
     
     doTest("T5",
         "Traffic Accidents HIGH MECH - Rollover-D2-STA500-SW BADGER RD/SW CHINOOK DR--13:11:56",
         "CALL:Traffic Accidents HIGH MECH - Rollover",
         "PRI:D2",
         "SRC:STA500",
         "ADDR:SW BADGER RD & SW CHINOOK DR");
 
     doTest("T6",
         "Sick Person Abnormal breathing -C-512,571-8431 SW BASALT DR-Map 131216-10:46:05",
         "CALL:Sick Person Abnormal breathing",
         "PRI:C",
         "UNIT:512,571",
         "ADDR:8431 SW BASALT DR",
         "MAP:131216");
     
     doTest("T7",
         "Sick Person Vomiting   -A-STA500-13812 SW CANYON DR-Map 131216-18:47:56",
         "CALL:Sick Person Vomiting",
         "PRI:A",
         "SRC:STA500",
         "ADDR:13812 SW CANYON DR",
         "MAP:131216");
 
     doTest("T8",
         "STRUCTURE FIRE-SF-422,412,421,471,531,431,447,510,4PC-1995 NW LOWER BRIDGE WAY-Map 1413NW-19:04:36",
         "CALL:STRUCTURE FIRE",
         "PRI:SF",
         "UNIT:422,412,421,471,531,431,447,510,4PC",
         "ADDR:1995 NW LOWER BRIDGE WAY",
         "MAP:1413NW");
 
     doTest("T9",
         "BRUSH FIRE HIGH RISK RESPONSE-BH-441,412,447,531,543,481,E640,448,541,442,544,4PC-NW 43RD ST/NW LOWER BRIDGE WAY-Map 1413NW-17:13:24",
         "CALL:BRUSH FIRE HIGH RISK RESPONSE",
         "PRI:BH",
         "UNIT:441,412,447,531,543,481,E640,448,541,442,544,4PC",
         "ADDR:NW 43RD ST & NW LOWER BRIDGE WAY",
         "MAP:1413NW");
 
    doTest("T10",
        "Stroke Abnormal breathing -C-271,221- 2 ROGUE LN 17692 ROGUE LN -Map 191129-08:11:41",
        "CALL:Stroke Abnormal breathing",
        "PRI:C",
        "UNIT:271,221",
        "ADDR:2 ROGUE LN",
        "MAP:191129");

   }
   
   public static void main(String args[]) {
    new ORDeschutesCountyParserTest().generateTests("T11");
   }
 }
