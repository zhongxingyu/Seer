 package net.sf.clirr.core.internal.checks;
 
 import net.sf.clirr.core.Severity;
 import net.sf.clirr.core.ScopeSelector;
 import net.sf.clirr.core.internal.ClassChangeCheck;
 import net.sf.clirr.core.internal.checks.FieldSetCheck;
 import net.sf.clirr.core.internal.checks.AbstractCheckTestCase;
 import net.sf.clirr.core.internal.checks.ExpectedDiff;
 
 /**
  * Tests FieldSetCheck.
  *
  * @author lkuehne
  */
 public class FieldSetCheckTest extends AbstractCheckTestCase
 {
     public void testFieldCheck() throws Exception
     {
         ExpectedDiff[] expected = new ExpectedDiff[] {
             new ExpectedDiff("Removed field stat7", Severity.ERROR, "testlib.MembersChange", null, "stat7"),
            new ExpectedDiff("Accessability of field fin2 has been weakened from public to protected", Severity.ERROR, "testlib.MembersChange", null, "fin2"),
            new ExpectedDiff("Accessability of field stat4 has been weakened from public to protected", Severity.ERROR, "testlib.MembersChange", null, "stat4"),
            new ExpectedDiff("Accessability of field priv2 has been increased from private to public", Severity.INFO, "testlib.MembersChange", null, "priv2"),
            new ExpectedDiff("Accessability of field stat5 has been weakened from public to private", Severity.ERROR, "testlib.MembersChange", null, "stat5"),
             new ExpectedDiff("Field stat2 is now final", Severity.ERROR, "testlib.MembersChange", null, "stat2"),
             new ExpectedDiff("Field pub3 is now final", Severity.ERROR, "testlib.MembersChange", null, "pub3"),
            new ExpectedDiff("Accessability of field stat6 has been weakened from public to package", Severity.ERROR, "testlib.MembersChange", null, "stat6"),
             new ExpectedDiff("Field stat3 is now non-static", Severity.ERROR, "testlib.MembersChange", null, "stat3"),
             new ExpectedDiff("Field fin3 is now non-static", Severity.ERROR, "testlib.MembersChange", null, "fin3"),
             new ExpectedDiff("Added public field stat8", Severity.INFO, "testlib.MembersChange", null, "stat8"),
             new ExpectedDiff("Field fin4 is now non-final", Severity.INFO, "testlib.MembersChange", null, "fin4"),
             new ExpectedDiff("Field pub2 is now static", Severity.ERROR, "testlib.MembersChange", null, "pub2"),
             new ExpectedDiff("Changed type of field obj1 from java.lang.Object to java.lang.String", Severity.ERROR, "testlib.MembersChange", null, "obj1"),
             new ExpectedDiff("Changed type of field obj2 from java.lang.Boolean to java.lang.String", Severity.ERROR, "testlib.MembersChange", null, "obj2"),
             new ExpectedDiff("Value of field fin6 is no longer a compile-time constant", Severity.WARNING, "testlib.MembersChange", null, "fin6"),
             new ExpectedDiff("Value of compile-time constant fin5 has been changed", Severity.WARNING, "testlib.MembersChange", null, "fin5"),
         };
         verify(expected);
     }
 
 
     protected final ClassChangeCheck createCheck(TestDiffListener tdl)
     {
         ScopeSelector scopeSelector = new ScopeSelector();
         return new FieldSetCheck(tdl, scopeSelector);
     }
 
 }
