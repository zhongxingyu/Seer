 package jscl.math;
 
 import jscl.AngleUnits;
 import jscl.JsclMathEngine;
 import jscl.MathEngine;
 import jscl.math.function.Constant;
 import jscl.math.function.ExtendedConstant;
 import jscl.text.ParseException;
 import junit.framework.Assert;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
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
 		Assert.assertEquals("48.0", Expression.valueOf("2*4.0!").numeric().toString());
 		Assert.assertEquals("40320.0", Expression.valueOf("(2*4.0)!").numeric().toString());
 
 		final AngleUnits angleUnits = JsclMathEngine.instance.getDefaultAngleUnits();
 		try {
 			JsclMathEngine.instance.setDefaultAngleUnits(AngleUnits.rad);
 			Assert.assertEquals("-0.9055783620066238", Expression.valueOf("sin(4!)").numeric().toString());
 		} finally {
 			JsclMathEngine.instance.setDefaultAngleUnits(angleUnits);
 		}
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
 		} catch (NotIntegerException e) {
 		}
 
 		try {
 			Assert.assertEquals("ln(7.2!)", Expression.valueOf("ln(7.2!)").numeric().toString());
 			fail();
 		} catch (NotIntegerException e) {
 		}
 
 		Assert.assertEquals("ln(7.2!)", Expression.valueOf("ln(7.2!)").simplify().toString());
 
 
 		Assert.assertEquals("36.0", Expression.valueOf("3!^2").numeric().toString());
 		Assert.assertEquals("1.0", Expression.valueOf("(π/π)!").numeric().toString());
 		Assert.assertEquals("720.0", Expression.valueOf("3!!").numeric().toString());
 		Assert.assertEquals("36.0", Expression.valueOf("3!*3!").numeric().toString());
 
 		Assert.assertEquals("100.0", Expression.valueOf("0.1E3").numeric().toString());
 
 		final AngleUnits defaultAngleUnits = JsclMathEngine.instance.getDefaultAngleUnits();
 		try {
 			JsclMathEngine.instance.setDefaultAngleUnits(AngleUnits.rad);
 			Assert.assertEquals("0.017453292519943295", Expression.valueOf("1°").numeric().toString());
 			Assert.assertEquals("0.03490658503988659", Expression.valueOf("2°").numeric().toString());
 			Assert.assertEquals("0.05235987755982989", Expression.valueOf("3°").numeric().toString());
 			Assert.assertEquals("0.26179938779914946", Expression.valueOf("3°*5").numeric().toString());
 			Assert.assertEquals("0.0027415567780803775", Expression.valueOf("3°^2").numeric().toString());
 			Assert.assertEquals("0.01096622711232151", Expression.valueOf("3!°^2").numeric().toString());
 			Assert.assertEquals("9.138522593601259E-4", Expression.valueOf("3°°").numeric().toString());
 			Assert.assertEquals("0.08726646259971647", Expression.valueOf("5°").numeric().toString());
 			Assert.assertEquals("2.0523598775598297", Expression.valueOf("2+3°").numeric().toString());
 		} finally {
 			JsclMathEngine.instance.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		try {
 			JsclMathEngine.instance.setDefaultAngleUnits(AngleUnits.deg);
 			Assert.assertEquals("1.0", Expression.valueOf("1°").numeric().toString());
 			Assert.assertEquals("2.0", Expression.valueOf("2°").numeric().toString());
 			Assert.assertEquals("3.0", Expression.valueOf("3°").numeric().toString());
 			Assert.assertEquals("15.0", Expression.valueOf("3°*5").numeric().toString());
 			Assert.assertEquals("9.0", Expression.valueOf("3°^2").numeric().toString());
 			Assert.assertEquals("36.0", Expression.valueOf("3!°^2").numeric().toString());
 			Assert.assertEquals("3.0", Expression.valueOf("3°°").numeric().toString());
 			Assert.assertEquals("5.0", Expression.valueOf("5°").numeric().toString());
 			Assert.assertEquals("5.0", Expression.valueOf("2+3°").numeric().toString());
 		} finally {
 			JsclMathEngine.instance.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		Assert.assertEquals("6", Expression.valueOf("2*∂(3*x,x)").expand().toString());
 		Assert.assertEquals("3", Expression.valueOf("∂(3*x,x)").expand().toString());
 		Assert.assertEquals("12", Expression.valueOf("∂(x^3,x,2)").expand().toString());
 		Assert.assertEquals("3*a", Expression.valueOf("∂(3*x*a,x)").expand().toString());
 		Assert.assertEquals("0", Expression.valueOf("∂(3*x*a,x,0.011,2)").expand().toString());
 		Assert.assertEquals("0", Expression.valueOf("2*∂(3*x*a,x,0.011,2)").expand().toString());
 		Assert.assertEquals("ln(8)+lg(8)*ln(8)", Expression.valueOf("ln(8)*lg(8)+ln(8)").expand().toString());
 		Assert.assertEquals("3.9573643765059856", Expression.valueOf("ln(8)*lg(8)+ln(8)").numeric().toString());
 
 		Assert.assertEquals("4.0!", Expression.valueOf("4.0!").simplify().toString());
 		Assert.assertEquals("4.0°", Expression.valueOf("4.0°").simplify().toString());
 		Assert.assertEquals("30°", Expression.valueOf("30°").simplify().toString());
 
 
 		Assert.assertEquals("1.0", Expression.valueOf("abs(1)").numeric().toString());
 		Assert.assertEquals("0.0", Expression.valueOf("abs(0)").numeric().toString());
 		Assert.assertEquals("0.0", Expression.valueOf("abs(-0)").numeric().toString());
 		Assert.assertEquals("1.0", Expression.valueOf("abs(-1)").numeric().toString());
 		Assert.assertEquals("Infinity", Expression.valueOf("abs(-∞)").numeric().toString());
 
 		Assert.assertEquals("1.0", Expression.valueOf("abs(i)").numeric().toString());
 		Assert.assertEquals("0.0", Expression.valueOf("abs(0+0*i)").numeric().toString());
 		Assert.assertEquals("1.0", Expression.valueOf("abs(-i)").numeric().toString());
 		Assert.assertEquals("2.23606797749979", Expression.valueOf("abs(2-i)").numeric().toString());
 		Assert.assertEquals("2.23606797749979", Expression.valueOf("abs(2+i)").numeric().toString());
 		Assert.assertEquals("2.8284271247461903", Expression.valueOf("abs(2+2*i)").numeric().toString());
 		Assert.assertEquals("2.8284271247461903", Expression.valueOf("abs(2-2*i)").numeric().toString());
 
 		JsclMathEngine.instance.getConstantsRegistry().add(new ExtendedConstant.Builder(new Constant("k"), 2.8284271247461903));
 		Assert.assertEquals("2.8284271247461903", Expression.valueOf("k").numeric().toString());
 		Assert.assertEquals("k", Expression.valueOf("k").simplify().toString());
 		Assert.assertEquals("k", Expression.valueOf("k").simplify().toString());
 		Assert.assertEquals("k^3", Expression.valueOf("k*k*k").simplify().toString());
 		Assert.assertEquals("22.627416997969526", Expression.valueOf("k*k*k").numeric().toString());
 
 		JsclMathEngine.instance.getConstantsRegistry().add(new ExtendedConstant.Builder(new Constant("k_1"), 3d));
 		Assert.assertEquals("3.0", Expression.valueOf("k_1").numeric().toString());
 		Assert.assertEquals("3.0", Expression.valueOf("k_1[0]").numeric().toString());
 		Assert.assertEquals("3.0", Expression.valueOf("k_1[2]").numeric().toString());
 
 		Assert.assertEquals("t", Expression.valueOf("t").simplify().toString());
 		Assert.assertEquals("t^3", Expression.valueOf("t*t*t").simplify().toString());
 
 		try {
 			Expression.valueOf("t").numeric();
 			fail();
 		} catch (ArithmeticException e) {
 		}
 
 		JsclMathEngine.instance.getConstantsRegistry().add(new ExtendedConstant.Builder(new Constant("t"), (String) null));
 		try {
 			Expression.valueOf("t").numeric();
 			fail();
 		} catch (ArithmeticException e) {
 		}
 		Assert.assertEquals("t", Expression.valueOf("t").simplify().toString());
 		Assert.assertEquals("t^3", Expression.valueOf("t*t*t").simplify().toString());
 
 		Assert.assertEquals("-2/57", Expression.valueOf("1/(-57/2)").simplify().toString());
 		Assert.assertEquals("sin(30)", Expression.valueOf("sin(30)").expand().toString());
 		Assert.assertEquals("sin(n)", Expression.valueOf("sin(n)").expand().toString());
 		Assert.assertEquals("sin(n!)", Expression.valueOf("sin(n!)").expand().toString());
 		Assert.assertEquals("sin(n°)", Expression.valueOf("sin(n°)").expand().toString());
 		Assert.assertEquals("sin(30°)", Expression.valueOf("sin(30°)").expand().toString());
 		Assert.assertEquals("0.49999999999999994", Expression.valueOf("sin(30°)").expand().numeric().toString());
 		Assert.assertEquals("sin(2!)", Expression.valueOf("sin(2!)").expand().toString());
 
 		Assert.assertEquals("12", Expression.valueOf("3*(3+1)").expand().toString());
 		Assert.assertEquals("114.59155902616465", Expression.valueOf("deg(2)").numeric().toString());
 		try {
 			Assert.assertEquals("-0.1425465430742778", Expression.valueOf("∏(tan(3))").numeric().toString());
 			fail();
 		} catch (ParseException e) {
 		}
 		try {
 			Assert.assertEquals("-0.14255", Expression.valueOf("sin(2,2)").expand().numeric().toString());
 			fail();
 		} catch (ParseException e) {
 		}
 		try {
 			Assert.assertEquals("114.59155902616465", Expression.valueOf("deg(2,2)").numeric().toString());
 			fail();
 		} catch (ParseException e) {
 		}
 
 		Assert.assertEquals("0.49999999999999994", Expression.valueOf("sin(30°)").numeric().toString());
 		Assert.assertEquals("π", Expression.valueOf("√(π)^2").simplify().toString());
 		Assert.assertEquals("π", Expression.valueOf("√(π^2)").simplify().toString());
 		Assert.assertEquals("π^2", Expression.valueOf("√(π^2*π^2)").simplify().toString());
 		Assert.assertEquals("π^3", Expression.valueOf("√(π^4*π^2)").simplify().toString());
 		Assert.assertEquals("e*π^2", Expression.valueOf("√(π^4*e^2)").simplify().toString());
 
 		Assert.assertEquals("1.0", Expression.valueOf("(π/π)!").numeric().toString());
 
 		// in deg mode π=180 and factorial of 180 is calculating
		Assert.assertEquals("0.0", Expression.valueOf("π/π!").numeric().toString());
 
 	}
 
 	@Test
 	public void testAngleUnits() throws Exception {
 		final MathEngine mathEngine = JsclMathEngine.instance;
 
 		final AngleUnits defaultAngleUnits = mathEngine.getDefaultAngleUnits();
 
 		for (AngleUnits angleUnits : AngleUnits.values()) {
 			try {
 				mathEngine.setDefaultAngleUnits(angleUnits);
 				mathEngine.evaluate("sin(2)");
 				mathEngine.evaluate("asin(2)");
 			} finally {
 				mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 			}
 		}
 
 		try {
 			mathEngine.setDefaultAngleUnits(AngleUnits.rad);
 			Assert.assertEquals(mathEngine.evaluate("0.9092974268256816953960198659117448427022549714478902683789"), mathEngine.evaluate("sin(2)"));
 			Assert.assertEquals(mathEngine.evaluate("0.1411200080598672221007448028081102798469332642522655841518"), mathEngine.evaluate("sin(3)"));
 			Assert.assertEquals(mathEngine.evaluate("0.0"), mathEngine.evaluate("sin(0)"));
 
 			Assert.assertEquals(mathEngine.evaluate("1.0"), mathEngine.evaluate("cos(0)"));
 			Assert.assertEquals(mathEngine.evaluate("0.8623188722876839341019385139508425355100840085355108292801"), mathEngine.evaluate("cos(100)"));
 			Assert.assertEquals(mathEngine.evaluate("-0.416146836547142386997568229500762189766000771075544890755"), mathEngine.evaluate("cos(2)"));
 
 			Assert.assertEquals(mathEngine.evaluate("-2.185039863261518991643306102313682543432017746227663164562"), mathEngine.evaluate("tan(2)"));
 			Assert.assertEquals(mathEngine.evaluate("-0.142546543074277805295635410533913493226092284901804647633"), mathEngine.evaluate("tan(3)"));
 			Assert.assertEquals(mathEngine.evaluate("0.6483608274590866712591249330098086768168743429837249756336"), mathEngine.evaluate("tan(10)"));
 
 			Assert.assertEquals(mathEngine.evaluate("0.6420926159343306"), mathEngine.evaluate("cot(1)"));
 			Assert.assertEquals(mathEngine.evaluate("-0.457657554360285763750277410432047276428486329231674329641"), mathEngine.evaluate("cot(2)"));
 			Assert.assertEquals(mathEngine.evaluate("-7.015252551434533469428551379526476578293103352096353838156"), mathEngine.evaluate("cot(3)"));
 		} finally {
 			mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		try {
 			mathEngine.setDefaultAngleUnits(AngleUnits.deg);
 			Assert.assertEquals(mathEngine.evaluate("0.9092974268256816953960198659117448427022549714478902683789"), mathEngine.evaluate("sin(deg(2))"));
 			Assert.assertEquals(mathEngine.evaluate("0.1411200080598672221007448028081102798469332642522655841518"), mathEngine.evaluate("sin(deg(3))"));
 			Assert.assertEquals(mathEngine.evaluate("0.0"), mathEngine.evaluate("sin(deg(0))"));
 
 			Assert.assertEquals(mathEngine.evaluate("1.0"), mathEngine.evaluate("cos(deg(0))"));
 			Assert.assertEquals(mathEngine.evaluate("0.8623188722876839341019385139508425355100840085355108292801"), mathEngine.evaluate("cos(deg(100))"));
 			Assert.assertEquals(mathEngine.evaluate("-0.416146836547142386997568229500762189766000771075544890755"), mathEngine.evaluate("cos(deg(2))"));
 
 			Assert.assertEquals(mathEngine.evaluate("-2.185039863261518991643306102313682543432017746227663164562"), mathEngine.evaluate("tan(deg(2))"));
 			Assert.assertEquals(mathEngine.evaluate("-0.142546543074277805295635410533913493226092284901804647633"), mathEngine.evaluate("tan(deg(3))"));
 			Assert.assertEquals(mathEngine.evaluate("0.6483608274590866712591249330098086768168743429837249756336"), mathEngine.evaluate("tan(deg(10))"));
 
 			Assert.assertEquals(mathEngine.evaluate("0.6420926159343306"), mathEngine.evaluate("cot(deg(1))"));
 			Assert.assertEquals(mathEngine.evaluate("-0.457657554360285763750277410432047276428486329231674329641"), mathEngine.evaluate("cot(deg(2))"));
 			Assert.assertEquals(mathEngine.evaluate("-7.015252551434533469428551379526476578293103352096353838156"), mathEngine.evaluate("cot(deg(3))"));
 		} finally {
 			mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		try {
 			mathEngine.setDefaultAngleUnits(AngleUnits.rad);
 			Assert.assertEquals(mathEngine.evaluate("-0.5235987755982989"), mathEngine.evaluate("asin(-0.5)"));
 			Assert.assertEquals(mathEngine.evaluate("-0.47349551215005636"), mathEngine.evaluate("asin(-0.456)"));
 			Assert.assertEquals(mathEngine.evaluate("0.32784124364198347"), mathEngine.evaluate("asin(0.322)"));
 
 			Assert.assertEquals(mathEngine.evaluate("1.2429550831529133"), mathEngine.evaluate("acos(0.322)"));
 			Assert.assertEquals(mathEngine.evaluate("1.5587960387762325"), mathEngine.evaluate("acos(0.012)"));
 			Assert.assertEquals(mathEngine.evaluate("1.6709637479564563"), mathEngine.evaluate("acos(-0.1)"));
 
 			Assert.assertEquals(mathEngine.evaluate("0.3805063771123649"), mathEngine.evaluate("atan(0.4)"));
 			Assert.assertEquals(mathEngine.evaluate("0.09966865249116204"), mathEngine.evaluate("atan(0.1)"));
 			Assert.assertEquals(mathEngine.evaluate("-0.5404195002705842"), mathEngine.evaluate("atan(-0.6)"));
 
 			Assert.assertEquals(mathEngine.evaluate("1.0603080048781206"), mathEngine.evaluate("acot(0.56)"));
 			// todo serso: wolfram alpha returns -0.790423 instead of 2.3511694068615325 (-PI)
 			Assert.assertEquals(mathEngine.evaluate("2.3511694068615325"), mathEngine.evaluate("acot(-0.99)"));
 			// todo serso: wolfram alpha returns -1.373401 instead of 1.7681918866447774 (-PI)
 			Assert.assertEquals(mathEngine.evaluate("1.7681918866447774"), mathEngine.evaluate("acot(-0.2)"));
 		} finally {
 			mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		try {
 			mathEngine.setDefaultAngleUnits(AngleUnits.deg);
 			Assert.assertEquals(mathEngine.evaluate("deg(-0.5235987755982989)"), mathEngine.evaluate("asin(-0.5)"));
 			Assert.assertEquals(mathEngine.evaluate("-27.129294464583623"), mathEngine.evaluate("asin(-0.456)"));
 			Assert.assertEquals(mathEngine.evaluate("18.783919611005786"), mathEngine.evaluate("asin(0.322)"));
 
 			Assert.assertEquals(mathEngine.evaluate("71.21608038899423"), mathEngine.evaluate("acos(0.322)"));
 			Assert.assertEquals(mathEngine.evaluate("89.31243414358914"), mathEngine.evaluate("acos(0.012)"));
 			Assert.assertEquals(mathEngine.evaluate("95.73917047726678"), mathEngine.evaluate("acos(-0.1)"));
 
 			Assert.assertEquals(mathEngine.evaluate("deg(0.3805063771123649)"), mathEngine.evaluate("atan(0.4)"));
 			Assert.assertEquals(mathEngine.evaluate("deg(0.09966865249116204)"), mathEngine.evaluate("atan(0.1)"));
 			Assert.assertEquals(mathEngine.evaluate("deg(-0.5404195002705842)"), mathEngine.evaluate("atan(-0.6)"));
 
 			Assert.assertEquals(mathEngine.evaluate("deg(1.0603080048781206)"), mathEngine.evaluate("acot(0.56)"));
 			// todo serso: wolfram alpha returns -0.790423 instead of 2.3511694068615325 (-PI)
 			Assert.assertEquals(mathEngine.evaluate("134.7120839334429"), mathEngine.evaluate("acot(-0.99)"));
 			// todo serso: wolfram alpha returns -1.373401 instead of 1.7681918866447774 (-PI)
 			Assert.assertEquals(mathEngine.evaluate("deg(1.7681918866447774)"), mathEngine.evaluate("acot(-0.2)"));
 		} finally {
 			mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		try {
 			mathEngine.setDefaultAngleUnits(AngleUnits.deg);
 			Assert.assertEquals(mathEngine.evaluate("0.0348994967025009716459951816253329373548245760432968714250"), mathEngine.evaluate("(sin(2))"));
 			Assert.assertEquals(mathEngine.evaluate("0.0523359562429438327221186296090784187310182539401649204835"), mathEngine.evaluate("(sin(3))"));
 			Assert.assertEquals(mathEngine.evaluate("0.0"), mathEngine.evaluate("sin(0)"));
 
 			Assert.assertEquals(mathEngine.evaluate("1.0"), mathEngine.evaluate("cos(0)"));
 			Assert.assertEquals(mathEngine.evaluate("-0.1736481776669303"), mathEngine.evaluate("(cos(100))"));
 			Assert.assertEquals(mathEngine.evaluate("0.9993908270190958"), mathEngine.evaluate("(cos(2))"));
 
 			Assert.assertEquals(mathEngine.evaluate("0.03492076949174773"), mathEngine.evaluate("(tan(2))"));
 			Assert.assertEquals(mathEngine.evaluate("0.05240777928304121"), mathEngine.evaluate("(tan(3))"));
 			Assert.assertEquals(mathEngine.evaluate("0.17632698070846498"), mathEngine.evaluate("(tan(10))"));
 
 			Assert.assertEquals(mathEngine.evaluate("57.28996163075943"), mathEngine.evaluate("(cot(1))"));
 			Assert.assertEquals(mathEngine.evaluate("28.636253282915604"), mathEngine.evaluate("(cot(2))"));
 			Assert.assertEquals(mathEngine.evaluate("19.081136687728208"), mathEngine.evaluate("(cot(3))"));
 		} finally {
 			mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		try {
 			mathEngine.setDefaultAngleUnits(AngleUnits.rad);
 			testSinEqualsToSinh(mathEngine, 0d);
 			testSinEqualsToSinh(mathEngine, 1d, "0.8414709848078965");
 			testSinEqualsToSinh(mathEngine, 3d, "0.1411200080598672");
 			testSinEqualsToSinh(mathEngine, 6d);
 			testSinEqualsToSinh(mathEngine, -1d, "-0.8414709848078965");
 			testSinEqualsToSinh(mathEngine, -3.3d, "0.1577456941432482");
 			testSinEqualsToSinh(mathEngine, -232.2d, "0.27429486373689577");
 		} finally {
 			mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		try {
 			mathEngine.setDefaultAngleUnits(AngleUnits.deg);
 			testSinEqualsToSinh(mathEngine, 0d);
 			testSinEqualsToSinh(mathEngine, 1d, "0.01745240643728351");
 			testSinEqualsToSinh(mathEngine, 3d, "0.052335956242943835");
 			testSinEqualsToSinh(mathEngine, 6d, "0.10452846326765347");
 			testSinEqualsToSinh(mathEngine, -1d, "-0.01745240643728351");
 			testSinEqualsToSinh(mathEngine, -3.3d, "-0.05756402695956728");
 			testSinEqualsToSinh(mathEngine, -232.2d, "0.7901550123756904");
 		} finally {
 			mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		try {
 			mathEngine.setDefaultAngleUnits(AngleUnits.rad);
 			Assert.assertEquals(mathEngine.evaluate("1.5707963267948966-0.8813735870195429*i"), mathEngine.evaluate("acos(i)"));
 			Assert.assertEquals(mathEngine.evaluate("0.9045568943023813-1.0612750619050355*i"), mathEngine.evaluate("acos(1+i)"));
 			Assert.assertEquals(mathEngine.evaluate("1.0-0.9999999999999998*i"), mathEngine.evaluate("cos(acos(1-i))"));
 			// todo serso: wolfram alpha returns 0.9045568943023813+1.0612750619050355*i for acos(1-i)
 			Assert.assertEquals(mathEngine.evaluate("0.9045568943023813+1.0612750619050355*i"), mathEngine.evaluate("-acos(1-i)"));
 		} finally {
 			mathEngine.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 	}
 
 	private void testSinEqualsToSinh(@NotNull MathEngine mathEngine, @NotNull Double x) throws ParseException {
 		testSinEqualsToSinh(mathEngine, x, null);
 	}
 
 	private void testSinEqualsToSinh(@NotNull MathEngine mathEngine, @NotNull Double x, @Nullable  String expected) throws ParseException {
 		if (expected == null) {
 			Assert.assertEquals(mathEngine.evaluate("sinh(i*" + x + ")/i"), mathEngine.evaluate("sin(" + x + ")"));
 //			Assert.assertEquals(mathEngine.evaluate("exp("+x+")-sinh(" + x + ")"), mathEngine.evaluate("cosh(" + x + ")"));
 		} else {
 			Assert.assertEquals(expected, mathEngine.evaluate("sin(" + x + ")"));
 			Assert.assertEquals(expected, mathEngine.evaluate("(exp(i * "+x+") - cos(" + x + "))/i"));
 			Assert.assertEquals(expected, mathEngine.evaluate("(exp(i * "+x+") - cos(" + x + "))/i"));
 		}
 	}
 
 	@Test
 	public void testName() throws Exception {
 		Expression.valueOf("a*c+b*sin(c)").toString();
 	}
 
 	@Test
 	public void testIntegrals() throws Exception {
 		Assert.assertEquals("50.0", Expression.valueOf("∫ab(x, x, 0, 10)").expand().numeric().toString());
 		Assert.assertEquals("1/2*a^2", Expression.valueOf("∫ab(x, x, 0, a)").expand().toString());
 		try {
 			Assert.assertEquals("∫ab(x, x, 0)", Expression.valueOf("∫ab(x, x, 0)").expand().toString());
 			fail();
 		} catch (ParseException e) {
 		}
 		try {
 			Assert.assertEquals("∫ab(x, x)", Expression.valueOf("∫ab(x, x)").expand().simplify().toString());
 			fail();
 		} catch (ParseException e) {
 		}
 		Assert.assertEquals("x^2/2", Expression.valueOf("∫(x, x)").expand().simplify().toString());
 		try {
 			Assert.assertEquals("x^2/2", Expression.valueOf("∫(x, x)").expand().numeric().toString());
 			fail();
 		} catch (ArithmeticException e) {
 		}
 
 		Assert.assertEquals("x^2/2", Expression.valueOf("∫(x, x)").expand().simplify().toString());
 		Assert.assertEquals("ln(x)", Expression.valueOf("∫(1/x, x)").expand().simplify().toString());
 		Assert.assertEquals("2*ln(2)+ln(cosh(x))", Expression.valueOf("∫(tanh(x), x)").expand().simplify().toString());
 		Assert.assertEquals("2*ln(2)+ln(sin(x))", Expression.valueOf("∫(cot(x), x)").expand().simplify().toString());
 		Assert.assertEquals("-2*ln(2)-ln(cos(x))", Expression.valueOf("∫(tan(x), x)").expand().simplify().toString());
 			}
 
 	@Test
 	public void testDerivations() throws Exception {
 		final AngleUnits defaultAngleUnits = JsclMathEngine.instance.getDefaultAngleUnits();
 		try {
 			JsclMathEngine.instance.setDefaultAngleUnits(AngleUnits.rad);
 			Assert.assertEquals("-0.9092974268256817", Expression.valueOf("∂(cos(t),t,2)").numeric().toString());
 			Assert.assertEquals("∂(cos(t), t, 2, 1)", Expression.valueOf("∂(cos(t),t,2)").simplify().toString());
 			Assert.assertEquals("-2.234741690198506", Expression.valueOf("∂(t*cos(t),t,2)").numeric().toString());
 			Assert.assertEquals("-4.469483380397012", Expression.valueOf("2*∂(t*cos(t),t,2)").numeric().toString());
 			Assert.assertEquals("-sin(2)", Expression.valueOf("∂(cos(t),t,2)").expand().toString());
 			Assert.assertEquals("-sin(t)", Expression.valueOf("∂(cos(t),t)").expand().toString());
 			org.junit.Assert.assertEquals("-sin(t)", Expression.valueOf("∂(cos(t),t,t,1)").expand().simplify().toString());
 			org.junit.Assert.assertEquals("∂(cos(t), t, t, 1°)", Expression.valueOf("∂(cos(t),t,t,1°)").expand().simplify().toString());
 		} finally {
 			JsclMathEngine.instance.setDefaultAngleUnits(defaultAngleUnits);
 		}
 
 		// todo serso: uncomment and check!!!
 		//org.junit.Assert.assertEquals("∂(cos(t), t, t, 1°)", Expression.valueOf("∂(cos(t),t,t,1°)").expand().numeric().toString());
 
 		//Assert.assertEquals("cos'(t)", Expression.valueOf("cos'(t)").simplify().toString());
 		//Assert.assertEquals("-0.9092974268256817", Expression.valueOf("cos'(2)").numeric().toString());
 		//Assert.assertEquals(Expression.valueOf("-cos(2)").numeric().toString(), Expression.valueOf("cos''(2)").numeric().toString());
 	}
 
 	@Test
 	public void testSum() throws Exception {
 		Assert.assertEquals("3", Expression.valueOf("Σ(n,n,1,2)").expand().toString());
 		Assert.assertEquals("200", Expression.valueOf("Σ(n/n,n,1,200)").expand().toString());
 		Assert.assertEquals("1/3", Expression.valueOf("Σ((n-1)/(n+1),n,1,2)").expand().toString());
 		Assert.assertEquals("sin(1)", Expression.valueOf("Σ(sin(n),n,1,1)").expand().toString());
 		Assert.assertEquals("1/1!", Expression.valueOf("Σ(n/n!,n,1,1)").expand().toString());
 		Assert.assertEquals("2.0", Expression.valueOf("Σ(n/n!,n,1,2)").expand().numeric().toString());
 		Assert.assertEquals("2.7182818284590455", Expression.valueOf("Σ(n/n!,n,1,200)").expand().numeric().toString());
 		Assert.assertEquals("2.718281828459046", Expression.valueOf("Σ(n/(2*n/2)!,n,1,200)").expand().numeric().toString());
 		Assert.assertEquals(Expression.valueOf("3.0").numeric().toString(), Expression.valueOf("Σ(n°,n,1,2)").expand().numeric().toString());
 		Assert.assertEquals("200.0", Expression.valueOf("Σ(n°/n°,n,1,200)").expand().numeric().toString());
 		Assert.assertEquals("-sin(1)-sin(2)", Expression.valueOf("Σ(∂(cos(t),t,n),n,1,2)").expand().toString());
 		Assert.assertEquals("-0.05235190313978448", Expression.valueOf("Σ(∂(cos(t),t,n),n,1,2)").expand().numeric().toString());
 	}
 }
