 package net.sf.clirr.core.internal.checks;
 
 import net.sf.clirr.core.internal.ClassChangeCheck;
 import net.sf.clirr.core.internal.checks.ClassHierarchyCheck;
 import net.sf.clirr.core.internal.checks.AbstractCheckTestCase;
 import net.sf.clirr.core.Severity;
 
 /**
  * @author lkuehne
  */
 public class ClassHierarchyCheckTest extends AbstractCheckTestCase
 {
     public void testHierarchyChangesAreReported() throws Exception
     {
         ExpectedDiff[] expected = new ExpectedDiff[] {
             new ExpectedDiff("Added java.util.NoSuchElementException to the list of superclasses", Severity.WARNING, "testlib.ApplicationException", null, null),
            new ExpectedDiff("Removed java.awt.event.MouseAdapter from the list of superclasses", Severity.ERROR, "testlib.ChangingHierarchy", null, null),
             new ExpectedDiff("Added java.awt.event.WindowAdapter to the list of superclasses", Severity.INFO, "testlib.ChangingHierarchy", null, null),
         };
         verify(expected);
     }
 
     protected ClassChangeCheck createCheck()
     {
         return new ClassHierarchyCheck(getTestDiffListener());
     }
 }
