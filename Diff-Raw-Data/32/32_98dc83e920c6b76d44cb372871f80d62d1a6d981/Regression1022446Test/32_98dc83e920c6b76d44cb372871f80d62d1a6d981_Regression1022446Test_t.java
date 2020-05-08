 package net.sf.clirr.core.internal.checks;
 
import net.sf.clirr.core.CheckerException;
 import net.sf.clirr.core.Severity;
 
 /**
  * Regression test for bug 
  * <a href="https://sourceforge.net/tracker/index.php?func=detail&aid=1022446&group_id=89627&atid=590799">1022446</a>.
  *  
  * @author lkuehne
  */
 public class Regression1022446Test extends AbstractRegressionTestCase
 {
    public void testRegression() throws CheckerException
     {
         runChecker();
         final TestDiffListener testDiffListener = getTestDiffListener();
         assertEquals("false alarm (binary error)", 0, testDiffListener.countBinaryCompatibilityDiffs(Severity.ERROR));
         assertEquals("false alarm (source error)", 0, testDiffListener.countSourceCompatibilityDiffs(Severity.ERROR));
         assertEquals("false alarm (binary warning)", 0, testDiffListener.countBinaryCompatibilityDiffs(Severity.WARNING));
         assertEquals("false alarm (source warning)", 0, testDiffListener.countSourceCompatibilityDiffs(Severity.WARNING));
         assertEquals("false alarm (binary info)", 0, testDiffListener.countBinaryCompatibilityDiffs(Severity.INFO));
         assertEquals("false alarm (source info)", 0, testDiffListener.countSourceCompatibilityDiffs(Severity.INFO));
     }
 }
