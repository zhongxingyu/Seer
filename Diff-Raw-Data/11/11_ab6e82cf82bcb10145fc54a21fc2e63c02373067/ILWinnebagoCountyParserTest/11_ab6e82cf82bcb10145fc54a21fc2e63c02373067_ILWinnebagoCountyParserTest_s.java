 package net.anei.cadpage.parsers.IL;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class ILWinnebagoCountyParserTest extends BaseParserTest {
   
   public ILWinnebagoCountyParserTest() {
     setParser(new ILWinnebagoCountyParser(), "WINNEBAGO COUNTY", "IL");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "S:HRFD: Med: 687 Marion Rd M: 56 f deleious\n\n",
         "SRC:HRFD",
         "ADDR:687 Marion Rd",
         "CALL:56 f deleious");
 
     doTest("T2",
         "S:HRFD: Med:9918 Bluebonnet M: 70 M diaahra\n\n",
         "SRC:HRFD",
         "ADDR:9918 Bluebonnet",
         "CALL:70 M diaahra");
 
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(HRFD: Med:14244 krotz Dr) m fall victim",
         "SRC:HRFD",
         "ADDR:14244 krotz Dr",
         "CALL:m fall victim");
 
     doTest("T2",
         "(HRFD: Med:6792 Michelle Dr) F Trouble Breathing",
         "SRC:HRFD",
         "ADDR:6792 Michelle Dr",
         "CALL:F Trouble Breathing");
 
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "S:HRFD: Med:4694 indigo place M: c/o alarm no \n",
         "SRC:HRFD",
         "ADDR:4694 indigo place",
         "CALL:c/o alarm no");
 
     doTest("T2",
         "S:HRFD: Fire:380 Morning Cloak  #4 M: DRyer Fire\n",
         "SRC:HRFD",
         "ADDR:380 Morning Cloak",
         "APT:4",
         "CALL:DRyer Fire");
 
     doTest("T3",
         "S:HRFD: Fire:6569 Burr Oak Rd M: fire alarm\n",
         "SRC:HRFD",
         "ADDR:6569 Burr Oak Rd",
         "CALL:fire alarm");
 
     doTest("T4",
         "S:HRFD: Med:629 ALTHEA M: RES. C.O. ALARM\n",
         "SRC:HRFD",
         "ADDR:629 ALTHEA",
         "CALL:RES. C.O. ALARM");
    
  }
   
   public static void main(String[] args) {
    new ILWinnebagoCountyParserTest().generateTests("T3", "SRC ADDR APT CALL");
   }
 }
