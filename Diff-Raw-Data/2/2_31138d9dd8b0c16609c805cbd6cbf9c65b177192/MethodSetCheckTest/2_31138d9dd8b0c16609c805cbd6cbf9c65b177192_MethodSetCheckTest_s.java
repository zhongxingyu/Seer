 package net.sf.clirr.core.internal.checks;
 
 import net.sf.clirr.core.internal.ClassChangeCheck;
 import net.sf.clirr.core.Severity;
 import net.sf.clirr.core.ScopeSelector;
 import net.sf.clirr.core.internal.checks.MethodSetCheck;
 import net.sf.clirr.core.internal.checks.AbstractCheckTestCase;
 import net.sf.clirr.core.internal.checks.ExpectedDiff;
 
 /**
  * TODO: Docs.
  *
  * @author lkuehne
  */
 public class MethodSetCheckTest extends AbstractCheckTestCase
 {
     public void testMethodCheck() throws Exception
     {
         ExpectedDiff[] expected = new ExpectedDiff[] {
 
             // method addition and removal
             new ExpectedDiff("Method 'public void removedMethod(java.lang.String)' has been removed",
                     Severity.ERROR, "testlib.MethodsChange", "public void removedMethod(java.lang.String)", null),
            new ExpectedDiff("Accessability of method 'public int getPriv2()' has been decreased from public to private",
                     Severity.ERROR, "testlib.MethodsChange", "public int getPriv2()", null),
             new ExpectedDiff("Method 'protected MethodsChange(int, boolean)' has been added",
                     Severity.INFO, "testlib.MethodsChange", "protected MethodsChange(int, boolean)", null),
             new ExpectedDiff("Method 'public java.lang.Long getPrivSquare()' has been added",
                     Severity.INFO, "testlib.MethodsChange", "public java.lang.Long getPrivSquare()", null),
 
             new ExpectedDiff("Method 'public void moveToSuper()' has been added",
                     Severity.INFO, "testlib.ComplexMethodMoveBase", "public void moveToSuper()", null),
             new ExpectedDiff("Method 'public void moveToSuper()' is now implemented in superclass testlib.ComplexMethodMoveBase",
                     Severity.INFO, "testlib.ComplexMethodMoveSub", "public void moveToSuper()", null),
 
             new ExpectedDiff("Abstract method 'public void method()' is now specified by implemented interface testlib.BaseInterface",
                     Severity.INFO, "testlib.AbstractImpl", "public void method()", null),
 
             // Constructor changes
             new ExpectedDiff("Parameter 1 of 'protected MethodsChange(int)' has changed its type to java.lang.Integer",
                     Severity.ERROR, "testlib.MethodsChange", "protected MethodsChange(int)", null),
 
             // return type changes
             new ExpectedDiff("Return type of method 'public java.lang.Number getPrivAsNumber()' has been changed to java.lang.Integer",
                     Severity.ERROR, "testlib.MethodsChange", "public java.lang.Number getPrivAsNumber()", null),
             // TODO: INFO if method is final
             new ExpectedDiff("Return type of method 'public java.lang.Integer getPrivAsInteger()' has been changed to java.lang.Number",
                     Severity.ERROR, "testlib.MethodsChange", "public java.lang.Integer getPrivAsInteger()", null),
 
             // parameter list changes
             // Note: This is the current behaviour, not necessarily the spec of the desired behaviour
             // TODO: need to check assignability of types (and check if method or class is final?)
             new ExpectedDiff("In method 'public void printPriv()' the number of arguments has changed",
                     Severity.ERROR, "testlib.MethodsChange", "public void printPriv()", null),
             new ExpectedDiff("Parameter 1 of 'public void strengthenParamType(java.lang.Object)' has changed its type to java.lang.String",
                     Severity.ERROR, "testlib.MethodsChange", "public void strengthenParamType(java.lang.Object)", null),
             new ExpectedDiff("Parameter 1 of 'public void weakenParamType(java.lang.String)' has changed its type to java.lang.Object",
                     Severity.ERROR, "testlib.MethodsChange", "public void weakenParamType(java.lang.String)", null),
             new ExpectedDiff("Parameter 1 of 'public void changeParamType(java.lang.String)' has changed its type to java.lang.Integer",
                     Severity.ERROR, "testlib.MethodsChange", "public void changeParamType(java.lang.String)", null),
 
             // deprecation changes
             new ExpectedDiff("Method 'public void becomesDeprecated()' has been deprecated",
                     Severity.INFO, "testlib.MethodsChange", "public void becomesDeprecated()", null),
             new ExpectedDiff("Method 'public void becomesUndeprecated()' is no longer deprecated",
                     Severity.INFO, "testlib.MethodsChange", "public void becomesUndeprecated()", null),
 
             // declared exceptions
             // TODO
         };
         verify(expected);
     }
 
     protected final ClassChangeCheck createCheck(TestDiffListener tdl)
     {
         return new MethodSetCheck(tdl, new ScopeSelector());
     }
 }
