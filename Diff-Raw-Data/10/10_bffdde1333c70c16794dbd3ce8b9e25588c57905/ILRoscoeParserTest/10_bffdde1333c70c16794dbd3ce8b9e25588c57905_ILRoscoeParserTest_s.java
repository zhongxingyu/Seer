 package net.anei.cadpage.parsers.IL;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class ILRoscoeParserTest extends BaseParserTest {
   
   public ILRoscoeParserTest() {
     setParser(new ILRoscoeParser(), "ROSCOE", "IL");
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
     
   }
   
   public static void main(String[] args) {
    new ILRoscoeParserTest().generateTests("T1", "SRC ADDR CALL");
   }
 }
