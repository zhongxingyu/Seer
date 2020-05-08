 package jscl.math;
 
 import jscl.math.operator.Derivative;
 import junit.framework.Assert;
 import org.junit.Test;
 
 import static junit.framework.Assert.fail;
 
 /**
  * User: serso
  * Date: 10/27/11
  * Time: 3:54 PM
  */
 public class ExpressionTest {
 
 	@Test
 	public void testExpressions() throws Exception {
 		Assert.assertEquals("3.0", Expression.valueOf("3").numeric().toString());
 		Assert.assertEquals("0.6931471805599453", Expression.valueOf("ln(2)").numeric().toString());
 		Assert.assertEquals("1.0", Expression.valueOf("lg(10)").numeric().toString());
 		Assert.assertEquals("0.0", Expression.valueOf("eq(0, 1)").numeric().toString());
 		Assert.assertEquals("1.0", Expression.valueOf("eq(1, 1)").numeric().toString());
 
 		Assert.assertEquals("24.0", Expression.valueOf("4!").numeric().toString());
 		try {
 			Expression.valueOf("(-3+2)!").numeric().toString();
 			fail();
 		} catch (ArithmeticException e) {
 
 		}
 		Assert.assertEquals("24.0", Expression.valueOf("(2+2)!").numeric().toString());
 		Assert.assertEquals("120.0", Expression.valueOf("(2+2+1)!").numeric().toString());
 		Assert.assertEquals("24.0", Expression.valueOf("(2.0+2.0)!").numeric().toString());
 		Assert.assertEquals("24.0", Expression.valueOf("4.0!").numeric().toString());
 		Assert.assertEquals("-0.9055783620066238", Expression.valueOf("sin(4!)").numeric().toString());
 		Assert.assertEquals("1.0", Expression.valueOf("(3.14/3.14)!").numeric().toString());
 		Assert.assertEquals("1.0", Expression.valueOf("2/2!").numeric().toString());
 		try {
 			Assert.assertEquals("3.141592653589793!", Expression.valueOf("3.141592653589793!").numeric().toString());
 			fail();
 		} catch (NotIntegerException e) {
 
 		}
 		Assert.assertEquals("0.5235987755982988", Expression.valueOf("3.141592653589793/3!").numeric().toString());
 		try {
 			Assert.assertEquals("3.141592653589793/3.141592653589793!", Expression.valueOf("3.141592653589793/3.141592653589793!").numeric().toString());
 			fail();
 		} catch (ArithmeticException e) {
 
 		}
 		try {
 			Assert.assertEquals("7.2!", Expression.valueOf("7.2!").numeric().toString());
 			fail();
 		} catch (NotIntegerException e) {}
 
 		try {
 			Assert.assertEquals("ln(7.2!)", Expression.valueOf("ln(7.2!)").numeric().toString());
 			fail();
 		} catch (NotIntegerException e) {}
 
 		Assert.assertEquals("ln(7.2!)", Expression.valueOf("ln(7.2!)").simplify().toString());
 
 
 		Assert.assertEquals("36.0", Expression.valueOf("3!^2").numeric().toString());
 		Assert.assertEquals("1.0", Expression.valueOf("(pi/pi)!").numeric().toString());
 		Assert.assertEquals("720.0", Expression.valueOf("3!!").numeric().toString());
 		Assert.assertEquals("36.0", Expression.valueOf("3!*3!").numeric().toString());
 
 		Assert.assertEquals("0.05235987755982988", Expression.valueOf("3°").numeric().toString());
 		Assert.assertEquals("0.2617993877991494", Expression.valueOf("3°*5").numeric().toString());
 		Assert.assertEquals("0.002741556778080377", Expression.valueOf("3°^2").numeric().toString());
 		Assert.assertEquals("0.010966227112321508", Expression.valueOf("3!°^2").numeric().toString());
 		Assert.assertEquals("9.138522593601257E-4", Expression.valueOf("3°°").numeric().toString());
 		Assert.assertEquals("0.08726646259971647", Expression.valueOf("5°").numeric().toString());
 		Assert.assertEquals("2.0523598775598297", Expression.valueOf("2+3°").numeric().toString());
 
 		Assert.assertEquals("6", Expression.valueOf("2*d(3*x,x)").expand().toString());
 		Assert.assertEquals("3", Expression.valueOf("d(3*x,x)").expand().toString());
 		Assert.assertEquals("12", Expression.valueOf("d(x^3,x,2)").expand().toString());
 		Assert.assertEquals("3*a", Expression.valueOf("d(3*x*a,x)").expand().toString());
 		Assert.assertEquals("0", Expression.valueOf("d(3*x*a,x,0.011,2)").expand().toString());
 		Assert.assertEquals("0", Expression.valueOf("2*d(3*x*a,x,0.011,2)").expand().toString());
 	}
 }
