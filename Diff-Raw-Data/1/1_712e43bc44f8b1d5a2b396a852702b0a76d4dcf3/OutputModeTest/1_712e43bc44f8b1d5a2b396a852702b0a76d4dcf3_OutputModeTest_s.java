 package net.sf.cotta;
 
 import net.sf.cotta.io.OutputMode;
 import net.sf.cotta.test.TestCase;
 
 public class OutputModeTest extends TestCase {
   public void testBeAppendingMode() throws Exception {
     ensure.that(OutputMode.APPEND.isAppend()).eq(true);
   }
 
   public void testNotBeAppendingModeForOverwrite() throws Exception {
     ensure.that(OutputMode.OVERWRITE.isAppend()).eq(false);
    fail("trigger failure");
   }
 }
