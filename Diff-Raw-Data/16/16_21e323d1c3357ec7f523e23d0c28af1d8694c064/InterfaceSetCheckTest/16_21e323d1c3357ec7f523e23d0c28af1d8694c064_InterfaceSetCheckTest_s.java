 package net.sf.clirr.core.internal.checks;
 
 import net.sf.clirr.core.Severity;
 import net.sf.clirr.core.internal.ClassChangeCheck;
 
 public class InterfaceSetCheckTest extends AbstractCheckTestCase {
 
     public void testHierarchyChangesAreReported() throws Exception
     {
         ExpectedDiff[] expected = new ExpectedDiff[] {
                 new ExpectedDiff("Added java.awt.event.WindowListener to the set of implemented interfaces", Severity.INFO, "testlib.ChangingHierarchy", null, null),
                 new ExpectedDiff("Added java.awt.event.WindowFocusListener to the set of implemented interfaces", Severity.INFO, "testlib.ChangingHierarchy", null, null),
                 new ExpectedDiff("Added java.awt.event.WindowStateListener to the set of implemented interfaces", Severity.INFO, "testlib.ChangingHierarchy", null, null),
                new ExpectedDiff("Removed java.awt.event.MouseListener from the set of implemented interfaces", Severity.ERROR, "testlib.ChangingHierarchy", null, null),
         };
         verify(expected);
     }
 
     protected ClassChangeCheck createCheck()
     {
         return new InterfaceSetCheck(getTestDiffListener());
     }
 
 }
