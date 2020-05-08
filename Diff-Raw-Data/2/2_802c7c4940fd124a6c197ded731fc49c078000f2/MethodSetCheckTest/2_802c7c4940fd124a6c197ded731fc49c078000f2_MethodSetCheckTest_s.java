 package net.sf.clirr.checks;
 
 import net.sf.clirr.framework.ClassChangeCheck;
 import net.sf.clirr.event.ApiDifference;
 import net.sf.clirr.event.Severity;
 import net.sf.clirr.event.ScopeSelector;
 
 /**
  * TODO: Docs.
  *
  * @author lkuehne
  */
 public class MethodSetCheckTest extends AbstractCheckTestCase
 {
     public void testMethodCheck()
     {
         ApiDifference[] expected = new ApiDifference[] {
 
             // method addition and removal
             new ApiDifference("Method 'public void removedMethod(java.lang.String)' has been removed in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "public void removedMethod(java.lang.String)", null),
             new ApiDifference("Method 'public int getPriv2()' has been removed in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "public int getPriv2()", null),
             new ApiDifference("Method 'public java.lang.Long getPrivSquare()' has been added in testlib.MethodsChange",
                     Severity.INFO, "testlib.MethodsChange", "public java.lang.Long getPrivSquare()", null),
 
             new ApiDifference("Method 'public void moveToSuper()' has been added in testlib.ComplexMethodMoveBase",
                     Severity.INFO, "testlib.ComplexMethodMoveBase", "public void moveToSuper()", null),
             new ApiDifference("Method 'public void moveToSuper()' is now implemented in superclass testlib.ComplexMethodMoveBase in testlib.ComplexMethodMoveSub",
                     Severity.INFO, "testlib.ComplexMethodMoveSub", "public void moveToSuper()", null),
 
             new ApiDifference("Abstract method 'public void method()' is now specified by implemented interface testlib.BaseInterface in testlib.AbstractImpl",
                     Severity.INFO, "testlib.AbstractImpl", "public void method()", null),
 
             // Constructor changes
             new ApiDifference("Parameter 1 of 'protected MethodsChange(int)' has changed it's type to java.lang.Integer in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "protected MethodsChange(int)", null),
 
             // return type changes
             new ApiDifference("Return type of Method 'public java.lang.Number getPrivAsNumber()' has been changed to java.lang.Integer in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "public java.lang.Number getPrivAsNumber()", null),
             // TODO: INFO if method is final
             new ApiDifference("Return type of Method 'public java.lang.Integer getPrivAsInteger()' has been changed to java.lang.Number in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "public java.lang.Integer getPrivAsInteger()", null),
 
             // parameter list changes
             // Note: This is the current behaviour, not necessarily the spec of the desired behaviour
             // TODO: need to check assignability of types (and check if method or class is final?)
             new ApiDifference("In Method 'public void printPriv()' the number of arguments has changed in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "public void printPriv()", null),
             new ApiDifference("Parameter 1 of 'public void strengthenParamType(java.lang.Object)' has changed it's type to java.lang.String in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "public void strengthenParamType(java.lang.Object)", null),
             new ApiDifference("Parameter 1 of 'public void weakenParamType(java.lang.String)' has changed it's type to java.lang.Object in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "public void weakenParamType(java.lang.String)", null),
             new ApiDifference("Parameter 1 of 'public void changeParamType(java.lang.String)' has changed it's type to java.lang.Integer in testlib.MethodsChange",
                     Severity.ERROR, "testlib.MethodsChange", "public void changeParamType(java.lang.String)", null),
 
             // deprecation changes
             new ApiDifference("Method 'public void becomesDeprecated()' has been deprecated in testlib.MethodsChange",
                     Severity.INFO, "testlib.MethodsChange", "public void becomesDeprecated()", null),
             new ApiDifference("Method 'public void becomesUndeprecated()' is no longer deprecated in testlib.MethodsChange",
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
