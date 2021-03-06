 /*
  * Copyright 2004-2008 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.expression.spel;
 
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
 import org.springframework.expression.ParseException;
 import org.springframework.expression.spel.standard.StandardEvaluationContext;
 
 /**
  * Common superclass for expression tests.
  * 
  * @author Andy Clement
  */
 public abstract class ExpressionTestCase extends TestCase {
 
 	private final static boolean DEBUG = false;
 
 	protected static SpelExpressionParser parser = new SpelExpressionParser();
 	protected static StandardEvaluationContext eContext = TestScenarioCreator.getTestEvaluationContext();
 
 	/**
 	 * Evaluate an expression and check that the actual result matches the expectedValue and the class of the result
 	 * matches the expectedClassOfResult.
 	 * @param expression The expression to evaluate
 	 * @param expectedValue the expected result for evaluating the expression
 	 * @param expectedResultType the expected class of the evaluation result
 	 */
 	public void evaluate(String expression, Object expectedValue, Class<?> expectedResultType) {
 		try {
 			SpelExpression expr = parser.parseExpression(expression);
 			if (expr == null) {
 				fail("Parser returned null for expression");
 			}
 			if (DEBUG) {
 				SpelUtilities.printAbstractSyntaxTree(System.out, expr);
 			}
 			// Class<?> expressionType = expr.getValueType();
 			// assertEquals("Type of the expression is not as expected. Should be '"+expectedResultType+"' but is
 			// '"+expressionType+"'",
 			// expectedResultType,expressionType);
 
 			Object value = expr.getValue(eContext);
 
 			// Check the return value
 			if (value == null) {
 				if (expectedValue == null) {
 					return; // no point doing other checks
 				}
 				assertEquals("Expression returned null value, but expected '" + expectedValue + "'", expectedValue,
 						null);
 			}
 
 			Class<?> resultType = value.getClass();
 			assertEquals("Type of the actual result was not as expected.  Expected '" + expectedResultType
 					+ "' but result was of type '" + resultType + "'", expectedResultType, resultType);
 			// .equals/* isAssignableFrom */(resultType), truers);
 
 			// TODO isAssignableFrom would allow some room for compatibility
 			// in the above expression...
 
 			if (expectedValue instanceof String) {
 				assertEquals("Did not get expected value for expression '" + expression + "'.", expectedValue,
 						ExpressionTestCase.stringValueOf(value));
 			} else {
 				assertEquals("Did not get expected value for expression '" + expression + "'.", expectedValue, value);
 			}
 		} catch (EvaluationException ee) {
 			ee.printStackTrace();
 			fail("Unexpected Exception: " + ee.getMessage());
 		} catch (ParseException pe) {
 			pe.printStackTrace();
 			fail("Unexpected Exception: " + pe.getMessage());
 		}
 	}
 
 	public void evaluateAndAskForReturnType(String expression, Object expectedValue, Class<?> expectedResultType) {
 		try {
			SpelExpression expr = parser.parseExpression(expression);
 			if (expr == null) {
 				fail("Parser returned null for expression");
 			}
 			if (DEBUG) {
 				SpelUtilities.printAbstractSyntaxTree(System.out, expr);
 			}
 			// Class<?> expressionType = expr.getValueType();
 			// assertEquals("Type of the expression is not as expected. Should be '"+expectedResultType+"' but is
 			// '"+expressionType+"'",
 			// expectedResultType,expressionType);
 
 			Object value = expr.getValue(eContext, expectedResultType);
 			if (value == null) {
 				if (expectedValue == null)
 					return; // no point doing other checks
 				assertEquals("Expression returned null value, but expected '" + expectedValue + "'", expectedValue,
 						null);
 			}
 
 			Class<?> resultType = value.getClass();
 			assertEquals("Type of the actual result was not as expected.  Expected '" + expectedResultType
 					+ "' but result was of type '" + resultType + "'", expectedResultType, resultType);
 			// .equals/* isAssignableFrom */(resultType), truers);
 			assertEquals("Did not get expected value for expression '" + expression + "'.", expectedValue, value);
 			// isAssignableFrom would allow some room for compatibility
 			// in the above expression...
 		} catch (EvaluationException ee) {
 			SpelException ex = (SpelException) ee;
 			ex.printStackTrace();
 			fail("Unexpected EvaluationException: " + ex.getMessage());
 		} catch (ParseException pe) {
 			fail("Unexpected ParseException: " + pe.getMessage());
 		}
 	}
 
 	/**
 	 * Evaluate an expression and check that the actual result matches the expectedValue and the class of the result
 	 * matches the expectedClassOfResult. This method can also check if the expression is writable (for example, it is a
 	 * variable or property reference).
 	 * 
 	 * @param expression The expression to evaluate
 	 * @param expectedValue the expected result for evaluating the expression
 	 * @param expectedClassOfResult the expected class of the evaluation result
 	 * @param shouldBeWritable should the parsed expression be writable?
 	 */
 	public void eval(String expression, Object expectedValue, Class<?> expectedClassOfResult, boolean shouldBeWritable) {
 		try {
			SpelExpression e = parser.parseExpression(expression);
 			if (e == null) {
 				fail("Parser returned null for expression");
 			}
 			if (DEBUG) {
 				SpelUtilities.printAbstractSyntaxTree(System.out, e);
 			}
 			Object value = e.getValue(eContext);
 			if (value == null) {
 				if (expectedValue == null)
 					return; // no point doing other
 				// checks
 				assertEquals("Expression returned null value, but expected '" + expectedValue + "'", expectedValue,
 						null);
 			}
 			Class<? extends Object> resultType = value.getClass();
 			assertEquals("Did not get expected value for expression '" + expression + "'.", expectedValue,
 					ExpressionTestCase.stringValueOf(value));
 			assertEquals("Type of the result was not as expected.  Expected '" + expectedClassOfResult
 					+ "' but result was of type '" + resultType + "'", expectedClassOfResult
 					.equals/* isAssignableFrom */(resultType), true);
 			// TODO 4 isAssignableFrom would allow some room for compatibility
 			// in the above expression...
 
 			boolean isWritable = e.isWritable(eContext);
 			if (isWritable != shouldBeWritable) {
 				if (shouldBeWritable)
 					fail("Expected the expression to be writable but it is not");
 				else
 					fail("Expected the expression to be readonly but it is not");
 			}
 		} catch (EvaluationException ee) {
 			ee.printStackTrace();
 			fail("Unexpected Exception: " + ee.getMessage());
 		} catch (ParseException pe) {
 			pe.printStackTrace();
 			fail("Unexpected Exception: " + pe.getMessage());
 		}
 	}
 
 	/**
 	 * Evaluate the specified expression and ensure the expected message comes out. The message may have inserts and
 	 * they will be checked if otherProperties is specified. The first entry in otherProperties should always be the
 	 * position.
 	 * @param expression The expression to evaluate
 	 * @param expectedMessage The expected message
 	 * @param otherProperties The expected inserts within the message
 	 */
 	protected void evaluateAndCheckError(String expression, SpelMessages expectedMessage, Object... otherProperties) {
 		try {
			Expression expr = parser.parseExpression(expression);
 			if (expr == null) {
 				fail("Parser returned null for expression");
 			}
 			@SuppressWarnings("unused")
 			Object value = expr.getValue(eContext);
 			fail("Should have failed with message " + expectedMessage);
 		} catch (EvaluationException ee) {
 			SpelException ex = (SpelException) ee;
 			if (ex.getMessageUnformatted() != expectedMessage) {
 				System.out.println(ex.getMessage());
 				ex.printStackTrace();
 				assertEquals("Failed to get expected message", expectedMessage, ex.getMessageUnformatted());
 			}
 			if (otherProperties != null && otherProperties.length != 0) {
 				// first one is expected position of the error within the string
 				int pos = ((Integer) otherProperties[0]).intValue();
 				assertEquals("Did not get correct position reported in error ", pos, ex.getPosition());
 				if (otherProperties.length > 1) {
 					// Check inserts match
 					Object[] inserts = ex.getInserts();
 					if (inserts == null) {
 						inserts = new Object[0];
 					}
 					if (inserts.length < otherProperties.length - 1) {
 						ex.printStackTrace();
 						fail("Cannot check " + (otherProperties.length - 1)
 								+ " properties of the exception, it only has " + inserts.length + " inserts");
 					}
 					for (int i = 1; i < otherProperties.length; i++) {
 						if (!inserts[i - 1].equals(otherProperties[i])) {
 							ex.printStackTrace();
 							fail("Insert does not match, expected '" + otherProperties[i] + "' but insert value was '"
 									+ inserts[i - 1] + "'");
 						}
 					}
 				}
 			}
 		} catch (ParseException pe) {
 			pe.printStackTrace();
 			fail("Unexpected Exception: " + pe.getMessage());
 		}
 	}
 
 	public static String stringValueOf(Object value) {
 		// do something nice for arrays
 		if (value==null) return "null";
 		if (value.getClass().isArray()) {
 			StringBuilder sb = new StringBuilder();
 			if (value.getClass().getComponentType().isPrimitive()) {
 				// TODO 4 ought to support other primitives!
 				int[] l = (int[]) value;
 				sb.append("int[").append(l.length).append("]{");
 				for (int j = 0; j < l.length; j++) {
 					if (j > 0)
 						sb.append(",");
 					sb.append(stringValueOf(l[j]));
 				}
 				sb.append("}");
 			} else {
 				List<Object> l = Arrays.asList((Object[]) value);
 				sb.append(value.getClass().getComponentType().getName()).append("[").append(l.size()).append("]{");
 				int i = 0;
 				for (Object object : l) {
 					if (i > 0) {
 						sb.append(",");
 					}
 					i++;
 					sb.append(stringValueOf(object));
 				}
 				sb.append("}");
 			}
 			return sb.toString();
 		} else {
 			return value.toString();
 		}
 	}
 
 	// protected void evaluateAndCheckError(String string, ELMessages expectedMessage, Object... otherProperties) {
 	// try {
 	// SpelExpression expr = (SpelExpression) parser.parseExpression(string);
 	// if (expr == null)
 	// fail("Parser returned null for expression");
 	// // expr.printAST(System.out);
 	// @SuppressWarnings("unused")
 	// Object value = expr.getValue(eContext);
 	// fail("Should have failed with message " + expectedMessage);
 	// } catch (ExpressionException ee) {
 	// ELException ex = (ELException) ee;
 	// if (expectedMessage != ex.getMessageUnformatted()) {
 	// System.out.println(ex.getMessage());
 	// ex.printStackTrace();
 	// assertEquals("Failed to get expected message", expectedMessage, ex.getMessageUnformatted());
 	// }
 	// if (otherProperties != null && otherProperties.length != 0) {
 	// // first one is expected position of the error within the string
 	// int pos = ((Integer) otherProperties[0]).intValue();
 	// assertEquals("Did not get correct position reported in error ", pos, ex.getPosition());
 	// }
 	// }
 	//
 	// }
 
 }
