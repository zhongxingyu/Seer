 package com.ACM.binarycalculator.Utilities;
 
 /**
  * @author James Van Gaasbeck, ACM at UCF <jjvg@knights.ucf.edu>
  */
 
 import java.util.Stack;
 import java.util.StringTokenizer;
 
 import android.content.Context;
 import android.widget.Toast;
 
 import com.ACM.binarycalculator.R;
 import com.ACM.binarycalculator.Fragments.CalculatorBinaryFragment;
 import com.ACM.binarycalculator.Fragments.CalculatorDecimalFragment;
 import com.ACM.binarycalculator.Fragments.CalculatorHexFragment;
 import com.ACM.binarycalculator.Fragments.CalculatorOctalFragment;
 
 /*
  * This class is meant to be used to convert an infix expression into it's post fix (RPN) equivalent.
  * The "convertToPostfix(String infixExpression)" method is very similar to this java blog article: 
  * http://java.macteki.com/2011/06/arithmetic-evaluator-infix-to-postfix.html
  * Of course it didn't fit inside our application perfectly so there are modifications to it.
  * 
  * Handles negative numbers, and fractions. Also counts the number of operators 
  * for expression grammar checks, and adds implicit multiplication signs where
  * needed.
  * 
  * 
  * Example:
  * infix = ( 6.6 + 2 ) * .5 - -8 / 4
  * postFix = 6.6 2 + .5 * -8 4 / -
  */
 public class InfixToPostfix {
 	private static String TAG = "InfixToPostfix";
 
 	/**
 	 * Method to convert an infix expression to post-fix (RPN). Convert to
 	 * base-10 before calling this method.
 	 * 
 	 * @param infixExpression
 	 *            - The infix expression that is meant to be converted into it's
 	 *            postFix equivalent.
 	 * @return - The postFix equivalent of the infix expression.
 	 */
 	public static String convertToPostfix(String infixExpression,
 			Context appContext) {
 
 		infixExpression = addImplicitMultiplicationSigns(infixExpression,
 				appContext);
 		if (infixExpression.length() == 0) {
 			return "";
 		}
 
 		// stack we use to convert
 		Stack<String> theStack = new Stack<String>();
 		// the postFix string that will be returned
 		String postfix = new String("");
 		// just a variable used to add space buffers
 		String space = new String(" ");
 		// flag variables to tell if the number is negative, and if the operator
 		// isn't a minus/negative sign.
 		boolean isNegative = false, safeOperator = false;
 		// StringTokenizer to split up the expression
 		StringTokenizer toke = new StringTokenizer(infixExpression,
 				"x+-/)( \n", true);
 
 		// loop that walks the entire expression
 		while (toke.hasMoreElements()) {
 			// get a token (element)
 			String currentToken = toke.nextElement().toString();
 
 			// if the token is a number
 			if (!(currentToken.equals("+") || currentToken.equals("x")
 					|| currentToken.equals("-") || currentToken.equals("/")
 					|| currentToken.equals("\n") || currentToken.equals(space)
 					|| currentToken.equals("(") || currentToken.equals(")"))) {
 
 				// since the current token was a number, add it to the postFix
 				// expression
 				postfix += currentToken + space;
 
 			}
 			// if the token is an open parenthesis
 			else if (currentToken.equals("(")) {
 				theStack.push(currentToken);
 			}
 			// if the token is an operator
 			else if (currentToken.equals("+") || currentToken.equals("x")
 					|| currentToken.equals("-") || currentToken.equals("/")
 					|| currentToken.equals("\n")) {
 				// temporary variable to hold the minus/negative
 				String minusOrNegativeSign = null;
 
 				// if we are dealing with a minus/negative sign
 				if (currentToken.equals("-")) {
 					// utilize the temporary variable
 					minusOrNegativeSign = currentToken;
 
 					// check the next token to see if we are dealing with a
 					// minus sign or a negative sign
 					currentToken = toke.nextElement().toString();
 					if (!currentToken.equals(space)) {
 						// if the next token isn't a space then we are dealing
 						// with a negative number.
 						// so add the number with the negative sign in front of
 						// it
 						isNegative = true;
 						postfix += minusOrNegativeSign + currentToken + space;
 					}
 				} else {
 					// flag variable to indicate that we aren't dealing with a
 					// minus/negative sign
 					safeOperator = true;
 				}
 
 				// this case means that the minus sign we've encountered is in
 				// fact JUST a minus sign and not a negative sign.
 				if (!isNegative && !safeOperator) {
 					while (!theStack.isEmpty()
 							&& operatorPrecedence(theStack.peek()) >= operatorPrecedence(minusOrNegativeSign)) {
 
 						postfix += theStack.pop() + space;
 					}
 					theStack.push(minusOrNegativeSign);
 				}
 
 				// this case means we've encountered a regular/safe
 				// operator i.e "+ x /"
 				if (safeOperator && !isNegative) {
 					while (!theStack.isEmpty()
 							&& operatorPrecedence(theStack.peek()) >= operatorPrecedence(currentToken)) {
 
 						postfix += theStack.pop() + space;
 					}
 					theStack.push(currentToken);
 				}
 				// reset our flag variables
 				isNegative = false;
 				safeOperator = false;
 			}
 			// if the token is a closed parenthesis
 			else if (currentToken.equals(")")) {
 				while (!theStack.peek().equals("(")) {
 					postfix += theStack.pop() + space;
 				}
 				theStack.pop();
 			}
 			// if the token is a space
 			else if (currentToken.equals(space)) {
 				// do nothing
 			}
 		} // closes while() loop
 
 		// add what's in the stack to the postFix expression
 		while (!theStack.isEmpty()) {
 			postfix += theStack.pop() + space;
 		}
 
 		// return the new post-fix expression
 		return postfix;
 	}
 
 	// method to assign a precedence to each operator so we can handle order of
 	// operations correctly
 	private static int operatorPrecedence(String operator) {
 		int precedence = 0;
 		if (operator.equals("+")) {
 			precedence = 1;
 		} else if (operator.equals("-")) {
 			precedence = 1;
 		} else if (operator.equals("x")) {
 			precedence = 2;
 		} else if (operator.equals("/")) {
 			precedence = 2;
 		}
 		return precedence;
 	}
 
 	/**
 	 * This expression not only adds in implicit multiplication signs where
 	 * needed, it also counts the number of operators, which is a necessary task
 	 * for checking expression grammar rules. So this method needs to be called
 	 * every time before converting to post-fix.
 	 * 
 	 * @param expression
 	 *            - The expression that needs to have implicit multiplication
 	 *            signs added.
 	 * @return - A new expression with implicit multiplication signs added where
 	 *         needed. Example: "4.4 ( 5 ) .1" returns "4.4 x ( 5 ) x .1"
 	 */
 	private static String addImplicitMultiplicationSigns(String expression,
 			Context context) {
 
 		StringBuilder retVal = new StringBuilder();
 
 		for (int i = 0; i < expression.length(); i++) {
 			Character testChar = expression.charAt(i);
 
 			if (testChar.equals('x') || testChar.equals('/')
 					|| testChar.equals('+') || testChar.equals('-')) {
 
 				// check if it's a negative or minus sign.
 				if (testChar.equals('-')) {
 					Character isNegativeOrMinusSign = expression.charAt(i + 1);
 					if (Character.isSpaceChar(isNegativeOrMinusSign)) {
 						CalculatorDecimalFragment.numberOfOperators++;
 						CalculatorBinaryFragment.numberOfOperators++;
 						CalculatorHexFragment.numberOfOperators++;
 						CalculatorOctalFragment.numberOfOperators++;
 					}
 				} else {
 					CalculatorDecimalFragment.numberOfOperators++;
 					CalculatorBinaryFragment.numberOfOperators++;
 					CalculatorHexFragment.numberOfOperators++;
 					CalculatorOctalFragment.numberOfOperators++;
 				}
 
 				// check if there is division by 0. Let's just not allow this.
 				if (testChar.equals('/')) {
 					// quick and simple check to see if the expression ends with
 					// so variation of division by zero.
 					if (expression.endsWith("/ 0")
 							|| expression.endsWith("/ -0")
 							|| expression.endsWith("/ .0")
 							|| expression.endsWith("/ 0.0")
 							|| expression.endsWith("/ -.0")
 							|| expression.endsWith("/ -0.0")) {
 						Toast.makeText(context,
 								R.string.division_by_zero_error,
 								Toast.LENGTH_SHORT).show();
 						return "";
 					} else {
 						// check to see if the expression has division by zero
 						// in the middle of the expression
 						if (expression.length() > i + 2) {
 							int zeroTestIterator = i + 2;
 							Character isTheNumberZero = expression
 									.charAt(zeroTestIterator);
 							if (isTheNumberZero.equals('0')
 									|| isTheNumberZero.equals('.')
 									|| isTheNumberZero.equals('-')) {
 								while (zeroTestIterator < expression.length() - 1
 										&& expression
 												.charAt(++zeroTestIterator) != ' ') {
 									isTheNumberZero = expression
 											.charAt(zeroTestIterator);
 								}
 
 								if (isTheNumberZero.equals('0')) {
 									Toast.makeText(context,
 											R.string.division_by_zero_error,
 											Toast.LENGTH_SHORT).show();
 									return "";
 								}
 
 							}
 						}
 					}
 				} // closes division check.
 			}
 
 			if (testChar.equals('(')) {
 				if (i < 2) {
 					retVal.append(testChar.toString());
 					continue;
 				}
 				// test if most recent char was a number, if was then we
 				// need to
 				// add an implicit 'x'
 				testChar = retVal.toString().charAt(i - 2);
 				if (Character.isDigit(testChar)) {
 
 					retVal.append("x (");
 
 					CalculatorDecimalFragment.numberOfOperators++;
 					CalculatorBinaryFragment.numberOfOperators++;
 					CalculatorHexFragment.numberOfOperators++;
 					CalculatorOctalFragment.numberOfOperators++;
 
 				} else
 					retVal.append("(");
 			} else if (Character.isDigit(testChar)
 					|| testChar.toString().equals(".")) {
 				// test if most recent char was a ")", if was then we need
 				// to
 				// add an implicit 'x'
 				if (i < 2) {
 					retVal.append(testChar.toString());
 					continue;
 				}
				Character implicit = retVal.toString().charAt(retVal.toString().length() - 2);
 				if (implicit.equals(')')) {
 					retVal.append("x " + testChar.toString());
 
 					CalculatorDecimalFragment.numberOfOperators++;
 					CalculatorBinaryFragment.numberOfOperators++;
 					CalculatorHexFragment.numberOfOperators++;
 					CalculatorOctalFragment.numberOfOperators++;
 
 				} else
 					retVal.append(testChar.toString());
 			} else {
 				// otherwise just add the char to the string
 				retVal.append(testChar.toString());
 			}
 		}
 		return retVal.toString();
 	}
 }
