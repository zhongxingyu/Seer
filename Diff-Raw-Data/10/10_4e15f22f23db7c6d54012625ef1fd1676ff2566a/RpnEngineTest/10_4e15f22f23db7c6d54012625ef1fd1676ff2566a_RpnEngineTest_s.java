 package com.spykertech.BUnit;
 
 import android.util.Log;
 
 import junit.framework.TestCase;
 
 public class RpnEngineTest extends TestCase {
 	private void logDebug(String message) {
 		Log.d(RpnEngineTest.class.getName(), message);
 	}
 	
 	private int decimals = 10;
 	
 	private RpnEngine engine = new RpnEngine(decimals);
 	
 	private String formatDouble(Double value) {
 		String returnValue = String.format("%." + decimals + "g", value);
 		returnValue = returnValue.replaceFirst("0+$", "");
 		returnValue = returnValue.replaceFirst("\\.$", "");
 		return returnValue;
 	}
 	
 	public void testEmptyStack() throws Exception {
 		String startValue = Double.toString(Math.PI);
 		String rpnString = "";
 		String result = engine.pushPop(startValue, rpnString);
 		assertEquals(formatDouble(Math.PI), result);
 	}
 	
 	public void testSimpleAddition() throws Exception {
 		String rpnString = "1,+";
 		String startValue = Double.toString(Math.PI);
 		String expectedResult = formatDouble(Double.parseDouble(startValue) + 1.0);
 		String result = engine.pushPop(startValue, rpnString);
 		logDebug(String.format("expected [%s]", expectedResult));
 		logDebug(String.format("result [%s]", result));
 		assertEquals(expectedResult, result);
 	}
 	
 	public void testStackLeftWithMoreThanOneValue() throws Exception {
 		String rpnString = "1,2,+";
 		String expectedResult = "Error, stack left with multiple values.";
 		String startValue = Double.toString(Math.PI);
 		assertEquals(expectedResult, engine.pushPop(startValue, rpnString));
 	}
 	
 	public void testAddition() throws Exception {
 		String rpnString = "1,1,+,+";
 		String expectedResult = formatDouble(Double.parseDouble("3"));
 		String startValue = "1";
 		String result = engine.pushPop(startValue, rpnString);
 		logDebug(String.format("expected [%s]", expectedResult));
 		logDebug(String.format("result [%s]", result));
 		assertEquals(expectedResult, result);
 	}
 	
 	public void testSubtraction() throws Exception {
 		String rpnString = "1,-";
 		String expectedResult = formatDouble(Double.parseDouble("1.0"));
 		String startValue = "2";
 		String result = engine.pushPop(startValue, rpnString);
 		logDebug(String.format("expected [%s]", expectedResult));
 		logDebug(String.format("result [%s]", result));
 		assertEquals(expectedResult, result);
 	}
 	
 	public void testInsufficientOperands() throws Exception {
 		String invalidRpnString = "-";
 		String expectedResult = "Not enough operands to perform operation [-]";
 		String startValue = Double.toString(Math.PI);
 		assertEquals(expectedResult, engine.pushPop(startValue, invalidRpnString));
 	}
 	
 	public void testMultiplication() throws Exception {
 		String rpnString = "5,*";
 		String expectedResult = formatDouble(25.0);
 		String startValue = "5";
 		assertEquals(expectedResult, engine.pushPop(startValue, rpnString));
 	}
 	
 	public void testDivision() throws Exception {
 		String rpnString = "2,/";
 		String expectedResult = formatDouble(Double.parseDouble("5.0"));
 		String startValue = "10";
 		assertEquals(expectedResult, engine.pushPop(startValue, rpnString));
 	}
 	
 	public void testInvalidRpnExpression() throws Exception {
 		String invalidRpnExpression = "six,-";
 		String startValue = "1";
 		String result = engine.pushPop(startValue, invalidRpnExpression);
 		invalidRpnExpression = String.format("%s,%s", startValue, invalidRpnExpression);
 		String expectedResult = String.format("Invalid rpn expression [%s]", invalidRpnExpression);
 		assertEquals(expectedResult, result);
 	}
 	
 	public void testExpectedFormatting() throws Exception {
 		String rpnString = "";
 		String startValue = Double.toString(Math.PI);
 		String expectedResult = formatDouble(Math.PI);
 		RpnEngine engine = new RpnEngine(decimals);
 		assertEquals(expectedResult, engine.pushPop(startValue, rpnString));
 		rpnString = "2.54,*";
 		expectedResult = "2540";
 		assertEquals(expectedResult, engine.pushPop("1000", rpnString));
 	}
 }
