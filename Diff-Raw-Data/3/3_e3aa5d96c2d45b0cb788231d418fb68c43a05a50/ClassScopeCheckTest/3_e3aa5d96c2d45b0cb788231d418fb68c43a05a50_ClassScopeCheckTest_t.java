 package net.sf.clirr.core.internal.checks;
 
 import net.sf.clirr.core.internal.ClassChangeCheck;
 import net.sf.clirr.core.Severity;
 import net.sf.clirr.core.ClassSelector;
 import net.sf.clirr.core.ScopeSelector;
 import net.sf.clirr.core.internal.checks.ClassScopeCheck;
 import net.sf.clirr.core.internal.checks.AbstractCheckTestCase;
 
 /**
  * Tests for the ClassScopeCheck test.
  *
  * @author Simon Kitching
  */
 public class ClassScopeCheckTest extends AbstractCheckTestCase
 {
     public void testAccessChangesAreReported() throws Exception
     {
         ExpectedDiff[] expected = new ExpectedDiff[] {
             new ExpectedDiff("Decreased visibility of class from public to protected", Severity.ERROR, "testlib.scope.ClassScopeChange$A2", null, null),
             new ExpectedDiff("Decreased visibility of class from public to package", Severity.ERROR, "testlib.scope.ClassScopeChange$A3", null, null),
             new ExpectedDiff("Decreased visibility of class from public to private", Severity.ERROR, "testlib.scope.ClassScopeChange$A4", null, null),
 
             new ExpectedDiff("Increased visibility of class from protected to public", Severity.INFO, "testlib.scope.ClassScopeChange$B2", null, null),
             new ExpectedDiff("Decreased visibility of class from protected to package", Severity.ERROR, "testlib.scope.ClassScopeChange$B3", null, null),
             new ExpectedDiff("Decreased visibility of class from protected to private", Severity.ERROR, "testlib.scope.ClassScopeChange$B4", null, null),
 
             new ExpectedDiff("Increased visibility of class from package to public", Severity.INFO, "testlib.scope.ClassScopeChange$C2", null, null),
             new ExpectedDiff("Increased visibility of class from package to protected", Severity.INFO, "testlib.scope.ClassScopeChange$C3", null, null),
            // package->private is not an error, just an info, because we never report error for package or private diffs
            new ExpectedDiff("Decreased visibility of class from package to private", Severity.INFO, "testlib.scope.ClassScopeChange$C4", null, null),
 
             new ExpectedDiff("Increased visibility of class from private to public", Severity.INFO, "testlib.scope.ClassScopeChange$D2", null, null),
             new ExpectedDiff("Increased visibility of class from private to protected", Severity.INFO, "testlib.scope.ClassScopeChange$D3", null, null),
             new ExpectedDiff("Increased visibility of class from private to package", Severity.INFO, "testlib.scope.ClassScopeChange$D4", null, null),
         };
         verify(expected);
     }
 
     protected ClassChangeCheck createCheck(TestDiffListener tdl)
     {
         ScopeSelector scopeSelector = new ScopeSelector(ScopeSelector.SCOPE_PRIVATE);
         return new ClassScopeCheck(tdl, scopeSelector);
     }
 
     protected ClassSelector createClassSelector()
     {
         // only check the testlib/scope/ClassScopeChange class.
         ClassSelector classSelector = new ClassSelector(ClassSelector.MODE_IF);
         classSelector.addClass("testlib.scope.ClassScopeChange");
         return classSelector;
     }
 }
