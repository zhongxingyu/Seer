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
         "SRC:STA500",
         "ADDR:9395 SW SCOUT CAMP TRL",
         "MAP:131208");
     
     doTest("T2",
         "Sick Person Vomiting   -A-STA500-13852 SW CANYON DR-Map 131216-18:47:56",
         "CALL:Sick Person Vomiting",
         "SRC:STA500",
         "ADDR:13852 SW CANYON DR",
         "MAP:131216");
     
     doTest("T3",
         "DEAD BODY/DEATH R3-R3-STA500-14537 SW STALLION DR-Map 131223-10:23:46",
         "CALL:DEAD BODY/DEATH R3",
         "SRC:STA500",
         "ADDR:14537 SW STALLION DR",
         "MAP:131223");
     
     doTest("T4",
         "Falls Unk status Ground-B-STA500-13345 SW CINDER DR-Map 131215-00:30:04",
         "CALL:Falls Unk status Ground",
         "SRC:STA500",
         "ADDR:13345 SW CINDER DR",
         "MAP:131215");
     
     doTest("T5",
         "Traffic Accidents HIGH MECH - Rollover-D2-STA500-SW BADGER RD/SW CHINOOK DR--13:11:56",
         "CALL:Traffic Accidents HIGH MECH - Rollover",
         "SRC:STA500",
         "ADDR:SW BADGER RD & SW CHINOOK DR");
 
     doTest("T6",
         "Sick Person Abnormal breathing -C-512,571-8431 SW BASALT DR-Map 131216-10:46:05",
         "CALL:Sick Person Abnormal breathing",
         "UNIT:512,571",
         "ADDR:8431 SW BASALT DR",
         "MAP:131216");
     
     doTest("T7",
         "Sick Person Vomiting   -A-STA500-13812 SW CANYON DR-Map 131216-18:47:56",
         "CALL:Sick Person Vomiting",
         "SRC:STA500",
         "ADDR:13812 SW CANYON DR",
         "MAP:131216");
    
   }
 }
