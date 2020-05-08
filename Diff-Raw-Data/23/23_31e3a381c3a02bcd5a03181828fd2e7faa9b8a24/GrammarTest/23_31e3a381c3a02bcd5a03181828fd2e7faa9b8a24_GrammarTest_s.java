 package interdroid.contextdroid.test;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Map;
 
 import interdroid.contextdroid.ContextDroidException;
 import interdroid.contextdroid.contextexpressions.ComparisonExpression;
 import interdroid.contextdroid.contextexpressions.ConstantTypedValue;
 import interdroid.contextdroid.contextexpressions.ContextExpressionLexer;
 import interdroid.contextdroid.contextexpressions.ContextExpressionParser;
 import interdroid.contextdroid.contextexpressions.ContextTypedValue;
 import interdroid.contextdroid.contextexpressions.Expression;
 import interdroid.contextdroid.contextexpressions.LogicExpression;
 import interdroid.contextdroid.contextexpressions.LogicOperator;
 import interdroid.contextdroid.contextexpressions.MathExpression;
 import interdroid.contextdroid.contextexpressions.MathOperator;
 import interdroid.contextdroid.contextexpressions.ParseableEnum;
 import interdroid.contextdroid.contextexpressions.Comparator;
 import interdroid.contextdroid.contextexpressions.HistoryReductionMode;
 import interdroid.contextdroid.contextexpressions.TimestampedValue;
 import interdroid.contextdroid.contextexpressions.TypedValueExpression;
 
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 
 public class GrammarTest extends AndroidTestCase {
 	public static final String TAG = "GrammarTest";
 
 	// If the parser is being run in AST mode then we have to pull
 	// a field to get the built java object. This controls
 	// that.
 	private static final boolean AST_MODE = false;
 
 	private ContextExpressionParser buildParser(String arg) {
 		CharStream stream = new ANTLRStringStream(arg);
 		ContextExpressionLexer lexer =
 				new ContextExpressionLexer(stream);
 		TokenStream tokenStream = new CommonTokenStream(lexer);
 		ContextExpressionParser parser =
 				new ContextExpressionParser(tokenStream);
 		return parser;
 	}
 
 
 	private Expression parseExpression(String test)
 			throws RecognitionException {
 		ContextExpressionParser parser = buildParser(test);
 		Expression e =
 				(Expression) getValue(parser.expression(), "expression");
 		return e;
 	}
 
 	private void helpTestParseableEnum(ParseableEnum<?>[] values,
 			String name, String field) {
 		Log.d(TAG, "Testing " + name);
 		Method parseMethod = null;
 		try {
 			parseMethod = ContextExpressionParser.class.getMethod(name,
 					(Class[]) null);
 		} catch (Exception e) {
 			fail("No such method: " + name);
 			return;
 		}
 		try {
 			for (ParseableEnum<?> value : values) {
 				ContextExpressionParser parser =
 						buildParser(value.toParseString());
 				try {
 					Object ret =
 							parseMethod.invoke(parser, (Object[]) null);
 					ret = getValue(ret, field);
 					assertEquals(value, ret);
 				} catch (Exception e) {
 					fail("Caught RecognitionException: " + e);
 				}
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "Caught excetion.", e);
 			fail("Caught Exception");
 		}
 	}
 
 	private Object getValue(Object ret, String field) {
 		if (AST_MODE) {
 			try {
 				Field f;
 				f = ret.getClass().getField(field);
 				ret = f.get(ret);
 			} catch (Exception e) {
 				Log.e(TAG, e.getMessage());
 				ret = null;
 			}
 		}
 		return ret;
 	}
 
 	public void testComparator() {
 		helpTestParseableEnum(Comparator.values(), "comparator", "comparator");
 	}
 
 	public void testBinaryLogicOperator() {
 		helpTestParseableEnum(new LogicOperator[] {
 				LogicOperator.AND, LogicOperator.OR
 		}, "binary_logic_operator", "logic_operator");
 	}
 
 	public void testUnaryLogicOperator() {
 		helpTestParseableEnum(new LogicOperator[] {
 				LogicOperator.NOT
 		}, "unary_logic_operator", "logic_operator");
 	}
 
 	public void testMathOperator() {
 		helpTestParseableEnum(MathOperator.values(),
 				"math_operator", "math_operator");
 	}
 
 	public void testHistoryReductionMode() {
 		helpTestParseableEnum(HistoryReductionMode.values(),
 				"history_mode", "history_mode");
 	}
 
 	private Map<String, String> helpTestConfigurationOptions(String value)
 			throws RecognitionException {
 		ContextExpressionParser parser = buildParser(value);
 		@SuppressWarnings("unchecked")
 		Map<String, String> configs = (Map<String, String>)
 				getValue(parser.configuration_options(), "configuration");
 		for (String key : configs.keySet()) {
 			Log.d(TAG, "Config: " + key + " : " + configs.get(key));
 		}
 		return configs;
 	}
 
 	public void testConfigurationOption() throws RecognitionException {
 		Map<String, String> options =
 				helpTestConfigurationOptions("test=true");
 		Log.d(TAG, options.get("test"));
 		assertEquals(1, options.size());
 		assertEquals("true", options.get("test"));
 	}
 
 	public void test2ConfigrurationOptions() throws RecognitionException {
 		Map<String, String> options =
 				helpTestConfigurationOptions("test1=true&test2=false");
 		Log.d(TAG, options.get("test1"));
 		assertEquals(2, options.size());
 		assertEquals("true", options.get("test1"));
 		assertEquals("false", options.get("test2"));
 	}
 
 	public void test3ConfigurationOptions() throws RecognitionException {
 		Map<String, String> options =
 				helpTestConfigurationOptions(
 						"test2=true&test1=false&test3=maybe");
 		assertEquals(3, options.size());
 		assertEquals("false", options.get("test1"));
 		assertEquals("true", options.get("test2"));
 		assertEquals("maybe", options.get("test3"));
 	}
 
 	public void testSimpleStringConfigurationOption()
 			throws RecognitionException {
 		Map<String, String> options =
 				helpTestConfigurationOptions(
 						"test2='maybe'");
 		assertEquals(1, options.size());
 		Log.d(TAG, "option: " + options.get("test2"));
 		assertEquals("maybe", options.get("test2"));
 	}
 
 	public void testComplexStringConfigurationOption()
 			throws RecognitionException {
 		Map<String, String> options =
 				helpTestConfigurationOptions(
 						"test2='maybe works?'");
 		Log.d(TAG, "option: " + options.get("test2"));
 		assertEquals(1, options.size());
 		assertEquals("maybe works?", options.get("test2"));
 	}
 
 
 	public void testValuePath() throws RecognitionException {
 		String[] paths = {"single", "double.path", "tripple.path.yeah"};
 		for (String path : paths) {
 			ContextExpressionParser parser = buildParser(path);
 			assertEquals(path, getValue(parser.value_path(), "value_path"));
 		}
 	}
 
 	public void testContextTypedValue() throws RecognitionException {
 		String[][] values = {
 				{"sensor1", "value.path", null, null, null},
 				{"sensor2", "path.two", "config=value", null, null},
 				{"sensor3", "three", null,
 					HistoryReductionMode.MAX.toParseString(), "100"},
 				{"sensor3", "three", null,
 					null, "100"},
 				{"sensor3", "three", null,
 					HistoryReductionMode.MAX.toParseString(), null},
 				{"sensor3", "value.path.four", "config=value2",
 					HistoryReductionMode.MIN.toParseString(), "1000"},
 				{"sensor3", "value.path.four", "config=value2",
 						null, "1000"},
 				{"sensor3", "value.path.four", "config=value2",
 						HistoryReductionMode.MIN.toParseString(), null}
 		};
 		for (int i = 0; i < values.length; i++) {
 			String[] value = values[i];
 			String stringValue = buildContextTypedValue(value);
 			Log.d(TAG, "Testing: " + stringValue);
 			ContextExpressionParser parser = buildParser(stringValue);
 			ContextTypedValue ctv = (ContextTypedValue)
 					getValue(parser.context_typed_value(), "typed_value");
 
 			assertNotNull(ctv);
 			assertEquals(value[0], ctv.getEntity());
 			assertEquals(value[1], ctv.getValuePath());
 			if (value[2] != null) {
 				assertNotNull(ctv.getConfiguration().get("config"));
 			} else {
 				assertNull(ctv.getConfiguration().get("config"));
 			}
 			if (value[3] != null) {
 				assertEquals(value[3],
 						ctv.getHistoryReductionMode().toParseString());
 			} else {
 				assertEquals(HistoryReductionMode.DEFAULT_MODE,
 						ctv.getHistoryReductionMode());
 			}
 			if (value[4] != null) {
 				assertEquals(value[4], String.valueOf(ctv.getHistoryLength()));
 			} else {
 				assertEquals(ContextTypedValue.DEFAULT_HISTORY_LENGTH,
 						ctv.getHistoryLength());
 			}
 		}
 	}
 
 	private String buildContextTypedValue(String[] value) {
 		StringBuffer ret = new StringBuffer(value[0]);
 		ret.append(':');
 		ret.append(value[1]);
 		if (value[2] != null) {
 			ret.append('?');
 			ret.append(value[2]);
 		}
 		ret.append(' ');
 		if (value[3] != null || value[4] != null) {
 			ret.append('{');
 			if (value[3] != null) {
 				ret.append(value[3]);
 				if (value[4] != null) {
 					ret.append(',');
 				}
 			}
 			if (value[4] != null) {
 				ret.append(value[4]);
 			}
 			ret.append('}');
 		}
 		return ret.toString();
 	}
 
 	public void testConstantTypedValue() throws RecognitionException {
 		String[] values = {"10", "10.5"};
 		for (String value : values) {
 			Log.d(TAG, "Constant: " + value);
 			ContextExpressionParser parser = buildParser(value);
 			ConstantTypedValue ctv = (ConstantTypedValue)
 					getValue(parser.constant_typed_value(), "typed_value");
 			assertNotNull(ctv);
 			TimestampedValue[] tv = ctv.getValues("1", 0);
 			assertEquals(1, tv.length);
 			assertEquals(value, String.valueOf(tv[0].getValue()));
 		}
 	}
 
 	public void testStringConstantTypedValue() throws RecognitionException {
 		String[] values = {"10", "10.5", "north"};
 		for (String value : values) {
 			Log.d(TAG, "Constant: " + value);
 			ContextExpressionParser parser = buildParser("'" + value + "'");
 			ConstantTypedValue ctv = (ConstantTypedValue)
 					getValue(parser.constant_typed_value(), "typed_value");
 			assertNotNull(ctv);
 			TimestampedValue[] tv = ctv.getValues("1", 0);
 			assertEquals(1, tv.length);
 			assertEquals(value, String.valueOf(tv[0].getValue()));
 		}
 	}
 
 	public void testSimpleParentheticalExpression()
 			throws RecognitionException {
 		String expression = "(2 + 3)";
 		Expression e = parseExpression(expression);
 		assertTrue(e instanceof MathExpression);
 		MathExpression me = (MathExpression)e;
 		assertTrue(me.getOperator() == MathOperator.PLUS);
 	}
 
 	public void testComplexParentheticalExpression()
 			throws RecognitionException {
 		String expression = "(2 + 3) * 5";
 		Expression e = parseExpression(expression);
 		assertTrue(e instanceof MathExpression);
 		MathExpression me = (MathExpression)e;
 		assertEquals(MathOperator.TIMES, me.getOperator());
 		assertNotNull(me.getLeftExpression());
 		assertTrue(me.getLeftExpression() instanceof MathExpression);
 		me = (MathExpression) me.getLeftExpression();
 		assertEquals(MathOperator.PLUS, me.getOperator());
 	}
 
 	public void testComparativeExpression() throws RecognitionException {
 		String expression = "2 > 3";
 		Expression e = parseExpression(expression);
 		assertNotNull(e);
 		assertTrue(e instanceof ComparisonExpression);
 		ComparisonExpression ce = (ComparisonExpression) e;
 		assertEquals(Comparator.GREATER_THAN, ce.getComparator());
 	}
 
 	public void testMultiplicativeExpression() throws RecognitionException {
 		String[] tests = {"3 * 5", "1 * 5 * 10", "1 * 2 *3*4"};
 		for (String test : tests) {
 			Expression e = parseExpression(test);
 			assertTrue(e instanceof MathExpression);
 			while (e instanceof MathExpression) {
 				MathExpression me = (MathExpression)e;
 				assertEquals(MathOperator.TIMES, me.getOperator());
 				assertNotNull(me.getLeftExpression());
 				assertNotNull(me.getRightExpression());
 				e = me.getRightExpression();
 			}
 		}
 	}
 
 	public void testAdditiveExpression() throws RecognitionException {
 		String[] tests = {"3 + 5", "1 + 5 + 10", "1 + 2 +3+4"};
 		for (String test : tests) {
 			Expression e = parseExpression(test);
 			assertTrue(e instanceof MathExpression);
 			while (e instanceof MathExpression) {
 				MathExpression me = (MathExpression)e;
 				assertEquals(MathOperator.PLUS, me.getOperator());
 				assertNotNull(me.getLeftExpression());
 				assertNotNull(me.getRightExpression());
 				e = me.getRightExpression();
 			}
 		}
 	}
 
 	public void testMathPrecedence() throws RecognitionException {
 		String test = "3 + 5 * 4 + 2";
 		Expression e = parseExpression(test);
 		assertTrue(e instanceof MathExpression);
 		MathExpression me = (MathExpression)e;
 		assertEquals(MathOperator.PLUS, me.getOperator());
 		assertNotNull(me.getLeftExpression());
 		assertNotNull(me.getRightExpression());
 		assertTrue(me.getRightExpression() instanceof MathExpression);
 		MathExpression right = (MathExpression) me.getRightExpression();
 		assertEquals(MathOperator.PLUS, me.getOperator());
 		assertTrue(right.getLeftExpression() instanceof MathExpression);
 		MathExpression leftOfRight = (MathExpression) right.getLeftExpression();
 		assertEquals(MathOperator.TIMES, leftOfRight.getOperator());
 	}
 
 	public void testUnaryExpression() throws RecognitionException {
		String test = "! 2 all > 3";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof LogicExpression);
 		LogicExpression le = (LogicExpression) e;
 		assertNotNull(le.getLeftExpression());
 		assertTrue(le.getLeftExpression() instanceof ComparisonExpression);
 		assertEquals(LogicOperator.NOT, le.getOperator());
 	}
 
 	public void testComplexUnaryExpression() throws RecognitionException {
 		String test = "! 2 > 3 && 3 > 4";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof LogicExpression);
 		LogicExpression le = (LogicExpression) e;
 		assertEquals(LogicOperator.AND, le.getOperator());
 		assertNotNull(le.getLeftExpression());
 		assertNotNull(le.getRightExpression());
 		assertTrue(le.getLeftExpression() instanceof LogicExpression);
 		assertTrue(le.getRightExpression() instanceof ComparisonExpression);
 		le = (LogicExpression) le.getLeftExpression();
 		assertTrue(le.getLeftExpression() instanceof ComparisonExpression);
 		assertNull(le.getRightExpression());
 		assertEquals(LogicOperator.NOT, le.getOperator());
 	}
 
 	public void testParentheticalUnaryExpression() throws RecognitionException {
 		String test = "! (2 > 3 && 3 > 4)";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof LogicExpression);
 		LogicExpression le = (LogicExpression) e;
 		assertEquals(LogicOperator.NOT, le.getOperator());
 		assertNotNull(le.getLeftExpression());
 		assertNull(le.getRightExpression());
 		assertTrue(le.getLeftExpression() instanceof LogicExpression);
 		le = (LogicExpression) le.getLeftExpression();
 		assertEquals(LogicOperator.AND, le.getOperator());
 	}
 
 	public void testAndExpression() throws RecognitionException {
 		String test = "3 && 4";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof LogicExpression);
 		LogicExpression le = (LogicExpression) e;
 		assertEquals(LogicOperator.AND, le.getOperator());
 		assertNotNull(le.getLeftExpression());
 		assertNotNull(le.getRightExpression());
 	}
 
 	public void testIntExpression()
 			throws RecognitionException, ContextDroidException {
 		String test = "10";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof TypedValueExpression);
 		TimestampedValue[] values = e.getValues("don't care", 0);
 		assertEquals(1, values.length);
 		assertTrue(values[0].getValue() instanceof Long);
 		assertEquals(Long.valueOf(10), values[0].getValue());
 	}
 
 	public void testFloatExpression()
 			throws RecognitionException, ContextDroidException {
 		String test = "10.5";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof TypedValueExpression);
 		TimestampedValue[] values = e.getValues("don't care", 0);
 		assertEquals(1, values.length);
 		assertEquals(10.5, values[0].getValue());
 	}
 
 	public void testFloatWithExponent()
 			throws RecognitionException, ContextDroidException {
 		String test = "10.5e3";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof TypedValueExpression);
 		TimestampedValue[] values = e.getValues("don't care", 0);
 		assertEquals(1, values.length);
 		Log.d(TAG, values[0].getValue().getClass().getName());
 		assertTrue(values[0].getValue() instanceof Double);
 		assertEquals(Double.parseDouble(test), values[0].getValue());
 	}
 
 	public void testStringExpression()
 			throws RecognitionException, ContextDroidException {
 		String test = "'10.5'";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof TypedValueExpression);
 		TimestampedValue[] values = e.getValues("don't care", 0);
 		assertEquals(1, values.length);
 		assertEquals("10.5", values[0].getValue());
 	}
 
 	public void testEscapedExpression()
 			throws RecognitionException, ContextDroidException {
 		String test = "'\\n'";
 		assertEquals(4, test.length());
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof TypedValueExpression);
 		TimestampedValue[] values = e.getValues("don't care", 0);
 		assertEquals(1, values.length);
 		assertEquals("\n", values[0].getValue());
 	}
 
 	public void testUnicodeEscapedExpression()
 			throws RecognitionException, ContextDroidException {
 		String test = "'\\u001a'";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof TypedValueExpression);
 		TimestampedValue[] values = e.getValues("don't care", 0);
 		assertEquals(1, values.length);
 		assertEquals("\u001a", values[0].getValue());
 	}
 
 	public void testOrExpression() throws RecognitionException {
 		String test = "3 || 4";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof LogicExpression);
 		LogicExpression le = (LogicExpression) e;
 		assertEquals(LogicOperator.OR, le.getOperator());
 		assertNotNull(le.getLeftExpression());
 		assertNotNull(le.getRightExpression());
 	}
 
 	public void testOrOverAndPrecedence() throws RecognitionException {
 		String test = "3 && 4 || 5 && 6";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof LogicExpression);
 		LogicExpression le = (LogicExpression) e;
 		assertEquals(LogicOperator.OR, le.getOperator());
 		assertNotNull(le.getLeftExpression());
 		assertNotNull(le.getRightExpression());
 		assertTrue(le.getLeftExpression() instanceof LogicExpression);
 		assertTrue(le.getRightExpression() instanceof LogicExpression);
 		LogicExpression left = (LogicExpression) le.getLeftExpression();
 		LogicExpression right = (LogicExpression) le.getRightExpression();
 		assertEquals(LogicOperator.AND, left.getOperator());
 		assertEquals(LogicOperator.AND, right.getOperator());
 	}
 
 	public void testMathAndLogicPrecedence() throws RecognitionException {
 		String test = "3 + 5 || 3 - 10";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof LogicExpression);
 		LogicExpression le = (LogicExpression) e;
 		assertEquals(LogicOperator.OR, le.getOperator());
 		assertNotNull(le.getLeftExpression());
 		assertNotNull(le.getRightExpression());
 		assertTrue(le.getLeftExpression() instanceof MathExpression);
 		assertTrue(le.getRightExpression() instanceof MathExpression);
 		MathExpression left = (MathExpression) le.getLeftExpression();
 		MathExpression right = (MathExpression) le.getRightExpression();
 		assertEquals(MathOperator.PLUS, left.getOperator());
 		assertEquals(MathOperator.MINUS, right.getOperator());
 	}
 
 	public void testComparisonAndMathPrecedence() throws RecognitionException {
 		String test = "3 + 5 > 3 - 10";
 		Expression e = parseExpression(test);
 		assertNotNull(e);
 		assertTrue(e instanceof ComparisonExpression);
 		ComparisonExpression le = (ComparisonExpression) e;
 		assertEquals(Comparator.GREATER_THAN, le.getComparator());
 		assertNotNull(le.getLeftExpression());
 		assertNotNull(le.getRightExpression());
 		assertTrue(le.getLeftExpression() instanceof MathExpression);
 		assertTrue(le.getRightExpression() instanceof MathExpression);
 		MathExpression left = (MathExpression) le.getLeftExpression();
 		MathExpression right = (MathExpression) le.getRightExpression();
 		assertEquals(MathOperator.PLUS, left.getOperator());
 		assertEquals(MathOperator.MINUS, right.getOperator());
 	}
 }
