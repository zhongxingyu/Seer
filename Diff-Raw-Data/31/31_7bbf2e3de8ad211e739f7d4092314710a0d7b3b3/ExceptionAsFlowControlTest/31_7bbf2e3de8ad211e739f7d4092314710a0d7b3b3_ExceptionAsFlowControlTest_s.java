 package test.net.sourceforge.pmd.rules.strictexception;
 
 import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.rules.design.ExceptionAsFlowControl;
 import test.net.sourceforge.pmd.testframework.SimpleAggregatorTst;
 import test.net.sourceforge.pmd.testframework.TestDescriptor;
 
 public class ExceptionAsFlowControlTest extends SimpleAggregatorTst {
 
     public void testAll() {
        runTests(new TestDescriptor[] {
           new TestDescriptor(TEST1, "failure case", 1, new ExceptionAsFlowControl()),
           new TestDescriptor(TEST2, "normal throw catch", 0, new ExceptionAsFlowControl())
        });
     }
 
     private static final String TEST1 =
     "public class Foo {" + PMD.EOL +
     " void bar() {" + PMD.EOL +
     "  try {" + PMD.EOL +
     "   try {" + PMD.EOL +
     "   } catch (Exception e) {" + PMD.EOL +
     "    throw new WrapperException(e);" + PMD.EOL +
     "    // this is essentially a GOTO to the WrapperException catch block" + PMD.EOL +
     "   }" + PMD.EOL +
     "  } catch (WrapperException e) {" + PMD.EOL +
     "   // do some more stuff " + PMD.EOL +
     "  }" + PMD.EOL +
     " }" + PMD.EOL +
     "}";
 
     private static final String TEST2 =
     "public class Foo {" + PMD.EOL +
     " void bar() {" + PMD.EOL +
     "  try {} catch (Exception e) {}" + PMD.EOL +
     " }" + PMD.EOL +
     "}";
 }
