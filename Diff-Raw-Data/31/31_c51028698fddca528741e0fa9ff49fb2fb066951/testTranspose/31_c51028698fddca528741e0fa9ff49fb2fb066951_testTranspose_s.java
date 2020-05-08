 package jmathlibtests.toolbox.jmathlib.matrix;
 
 import jmathlib.core.interpreter.Interpreter;
 import jmathlib.tools.junit.framework.*;
 import jmathlibtests.Compare;
 
 public class testTranspose extends TestCase {
 	protected Interpreter ml;
 	
     public testTranspose(String name) {
 		super(name);
 	}
 	public static void main (String[] args) {
 		jmathlib.tools.junit.textui.TestRunner.run (suite());
 	}
 	protected void setUp() {
 		ml = new Interpreter(true);
 	}
     protected void tearDown() {
         ml = null;
     }
 
 	public static Test suite() {
 		return new TestSuite(testTranspose.class);
 	}
 
 	public void testTranspose01() {
         ml.executeExpression("a=transpose(6);");
 		assertTrue(6 == ml.getScalarValueRe("a"));
         assertTrue(0 == ml.getScalarValueIm("a"));
 	}
 
     public void testTranspose02() {
         ml.executeExpression("a=transpose(2i);");
         assertTrue(0  == ml.getScalarValueRe("a"));
         assertTrue(2 == ml.getScalarValueIm("a"));
     }
 
     public void testTranspose03() {
         ml.executeExpression("a=transpose(22+33i);");
         assertTrue(22  == ml.getScalarValueRe("a"));
         assertTrue(33 == ml.getScalarValueIm("a"));
     }
 
     public void testTranspose04() {
         ml.executeExpression("a=transpose([2,3;4,5]);");
         double[][] b = {{2.0, 4.0},{3.0, 5.0}};
         assertTrue(Compare.ArrayEquals(b, ml.getArrayValueRe("a")));
     }
 
     public void testTranspose05() {
         ml.executeExpression("a=transpose([2,3i;4-44i,5]);");
         double[][] re = {{2.0,   4.0},{0.0, 5.0}};
         double[][] im = {{0.0, -44.0},{3.0, 0.0}};
         assertTrue(Compare.ArrayEquals(re, ml.getArrayValueRe("a")));
         assertTrue(Compare.ArrayEquals(im, ml.getArrayValueIm("a")));
     }
 
 
 }
