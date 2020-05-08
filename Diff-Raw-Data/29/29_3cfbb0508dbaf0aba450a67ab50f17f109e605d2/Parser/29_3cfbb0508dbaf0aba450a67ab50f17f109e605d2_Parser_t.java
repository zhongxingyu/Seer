 package org.ruhlendavis.mc.abacus;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.ruhlendavis.utility.NumberUtilities;
 import org.ruhlendavis.utility.StringUtilities;
 
 /**
  * The engine for parsing mathematical expressions. Converts an infix
  * expression to a postfix expression and then evaluates the postfix expression.
  *
  * @author Feaelin (Iain E. Davis) <iain@ruhlendavis.org>
  */
 class Parser
 {
	private final String PREPARE_OPERATORS = "d^*/\\%+-(,";
 	private final String TOKENIZE_OPERATORS = "d^*/\\%+_()";
 	private final String POSTFIX_OPERATORS = "d^*/\\%+_(";
 	private final String EVALUATE_OPERATORS =  "d^*/\\%+_";
 	private final int FULL_STACK = 64;
 	private final int SMALL_STACK = 16;
 
 	private String originalExpression = "";
 	private String preparedExpression = "";
 	private String [] subExpressions;
 	private String result;
 
 	/**
 	 * This constructor takes a single string to be evaluated.
 	 *
 	 * @param expression String containing expression to evaluate.
 	 */
 	Parser(String expression)
 	{
 		setExpression(expression);
 	}
 
 	/**
 	 * This constructor takes an array of Strings which will be concatenated and
 	 * then evaluated as if they are a single expression.
 	 *
 	 * @param expressionArray Array of Strings to concatenate and evaluate.
 	 */
 	Parser(String [] expressionArray)
 	{
 			for (String expressionPart : expressionArray)
 			{
 				originalExpression = originalExpression + expressionPart;
 			}
 
 			setExpression(originalExpression);
 	}
 
 	/**
 	 * Returns the expression as it was before any processing.
 	 *
 	 * @return String containing the expression.
 	 */
 	public String getOriginalExpression()
 	{
 		return originalExpression;
 	}
 
 	/**
 	 * Returns the evaluated result as a String.
 	 *
 	 * @return String containing the result.
 	 */
 	public String getResult()
 	{
 		return result;
 	}
 
 	/**
 	 * Provides a new expression and evaluates the new expression.
 	 *
 	 * @param expression String containing the expression.
 	 */
 	public final void setExpression(String expression)
 	{
 		originalExpression = expression;
 		preparedExpression = "";
 		result = "";
 		prepareExpression();
 		subExpressions = preparedExpression.split(",");
 
 		// parse each individual expression, reapplying the comma at the end.
 		for (String subExpression : subExpressions)
 		{
 			result = result + parseExpression(subExpression) + ", ";
 		}
 
 		// Remove the extra comma & space.
 		result = result.substring(0, result.length() - 2);
 	}
 
 	/**
 	 * Dispatcher that calls each of the stages of the expression parsing,
 	 * conversion, and evaluation.
 	 *
 	 * @param expression Expression to parse.
 	 * @return String containing the result.
 	 */
 	private String parseExpression(String expression)
 	{
 		boolean fullStackMode = false;
 		boolean partialStackMode = false;
 
 		Float returnValue = new Float(0.0);
 
 		char character = expression.charAt(0);
 
 		if (character == 's' || character == 'S')
 		{
 			fullStackMode = true;
 			expression = expression.substring(1, expression.length());
 		}
 		else if (character == 'p' || character == 'P')
 		{
 			partialStackMode = true;
 			expression = expression.substring(1, expression.length());
 		}
 
 		if (expression.length() != 0)
 		{
 			List<String> tokens = tokenizeExpression(expression, TOKENIZE_OPERATORS);
 
 			if (!tokens.isEmpty())
 			{
 				Stack<String> postfixStack = postfixExpression(tokens);
 
 				if (!postfixStack.isEmpty())
 				{
 					returnValue = evaluatePostfix(postfixStack);
 				}
 			}
 		}
 
 		if (fullStackMode || partialStackMode)
 		{
 			Integer stacks;
 			Integer remainder;
 
 			if (fullStackMode)
 			{
 				stacks = returnValue.intValue() / FULL_STACK;
 				remainder = returnValue.intValue() % FULL_STACK;
 			}
 			else
 			{
 				stacks = returnValue.intValue() / SMALL_STACK;
 				remainder = returnValue.intValue() % SMALL_STACK;
 			}
 
 			return stacks.toString() + " stacks and "
 					 + remainder.toString() + " individual items.";
 		}
 		else
 		{
 			return returnValue.toString().replace(".0", "");
 		}
 	}
 
 	/**
 	 *  Prepares an expression for evaluation. Removes all whitespace (spaces,
 	 *  tabs, newlines, etc.), replaces grouping symbols with their () equivalents,
 	 *  replaces the subtraction operator with an underscore to make parsing
 	 *  easier at the later stages.
 	 *
 	 * @return String containing the prepared expression.
 	 */
 	public String prepareExpression()
 	{
 		if (preparedExpression.length() > 0)
 		{
 			return preparedExpression;
 		}
 
 		// Whitespace? Chomp!
 		String s = originalExpression.replaceAll("\\s", "");
 
 		// Difference between brackets merely visual -- make them all ()
 		s = s.replaceAll("\\{", "(").replaceAll("}", ")");
 		s = s.replaceAll("\\[", "(").replaceAll("]", ")");
 		s = s.replaceAll("<", "(").replaceAll(">", ")");
 
 		// Replace 3(6+3) with 3*(6+3)
 		Pattern pattern = Pattern.compile("(\\d+)\\(");
 		Matcher matcher = pattern.matcher(s);
 		s = matcher.replaceAll("$1*(");
 		
		// Replace (6+3)3 with (6+3)*3
		pattern = Pattern.compile("\\)(\\d+)");
 		matcher = pattern.matcher(s);
		s = matcher.replaceAll(")*$1");
		
		s = s.replaceAll("\\)\\(", ")*(");
		
		// Replace negation operators with ~
		if (s.charAt(0) == '-')
		{
			s = "~" + s.substring(1, s.length());
		}
		
		pattern = Pattern.compile("([" + Pattern.quote(PREPARE_OPERATORS) +"])-");
		matcher = pattern.matcher(s);
		s = matcher.replaceAll("$1~");
		
		// Replace minus operator symbols with _
		s = s.replaceAll("-", "_");
		
		// Restore negation symbols
		s = s.replaceAll("~", "-");
 		preparedExpression = s;
 		return s;
 	}
 
 	/**
 	 * Takes an expression and carves it up into 'tokens', an List of Strings
 	 * where each String is either a operand or operator, keeping the tokens
 	 * in the order presented in the expression.
 	 *
 	 * @param expression String containing the expression.
 	 * @param separators String containing tokenizable symbols (e.g. the operators)
 	 * @return List<String> that contains the tokens.
 	 */
 	private List<String> tokenizeExpression(String expression, String separators)
 	{
 		List<String> tokens = new ArrayList<String>();
 
 		int lastPosition = 0;
 		int currentPosition;
 		while ((currentPosition = StringUtilities.find_first_of(expression, separators, lastPosition)) != -1)
 		{
 			if (currentPosition != lastPosition)
 			{
 				tokens.add(expression.substring(lastPosition, currentPosition));
 			}
 			tokens.add(expression.substring(currentPosition, currentPosition + 1));
 			lastPosition = currentPosition + 1;
 		}
 
 		if (lastPosition < expression.length())
 		{
 			tokens.add(expression.substring(lastPosition, expression.length()));
 		}
 		return tokens;
 	}
 
 	/**
 	 * Converts a List of tokens in infix order to a Stack of tokens in postfix order.
 	 *
 	 * @param tokens List<String> containing the expression in infix order.
 	 * @return Stack<String> containing the expression in postfix order.
 	 * @throws ParserMathException When grouping tokens are not matched.
 	 */
 	private Stack<String> postfixExpression(List<String> tokens) throws ParserMathException
 	{
 		Stack<String> outputStack = new Stack<String>();
 		Stack<String> operatorsStack = new Stack<String>();
 
 		for (String token : tokens)
 		{
 			if (token.equals("("))
 			{
 				operatorsStack.push(token);
 			}
 			else if (token.equals(")"))
 			{
 				String item = operatorsStack.peek();
 				while (!operatorsStack.isEmpty() && !item.equals("("))
 				{
 					outputStack.add(0, item);
 					operatorsStack.pop();
 					item = operatorsStack.peek();
 				}
 				operatorsStack.pop();
 			}
 			else if (StringUtilities.find_first_of(token, POSTFIX_OPERATORS, 0) != -1)
 			{
 				while (!operatorsStack.isEmpty()
 					 && (!isHigher(token.charAt(0),	operatorsStack.peek().charAt(0))))
 				{
 					outputStack.add(0, operatorsStack.pop());
 				}
 				operatorsStack.push(token);
 			}
 			else /* operand */
 			{
 				outputStack.add(0, token);
 			}
 		}
 
 		while (!operatorsStack.isEmpty())
 		{
 			String item = operatorsStack.pop();
 			if (item.equals("("))
 			{
 				throw new ParserMathException("Mismatched grouping. Make sure your (), {}, [], <> match up.");
 			}
 			outputStack.add(0, item);
 		}
 
 		return outputStack;
 	}
 
 	/**
 	 * Evaluates an expression that is in postfix order.
 	 *
 	 * @param postfixStack Stack<String> of tokens in postfix order.
 	 * @return Float containing the computed result.
 	 * @throws ParserMathException On divide by zero or insufficient arguments.
 	 */
 	private Float evaluatePostfix(Stack<String> postfixStack) throws ParserMathException
 	{
 		Stack<Float> operands = new Stack<Float>();
 
 		while (!postfixStack.isEmpty())
 		{
 			String item = postfixStack.peek();
 
 			if (StringUtilities.find_first_of(item, EVALUATE_OPERATORS, 0) == -1)
 			{
 				int index = item.lastIndexOf('s');
 				if (index == -1)
 				{
 					index = item.lastIndexOf('p');
 					if (index == -1)
 					{
 						operands.push(Float.parseFloat(item));
 					}
 					else
 					{
 						operands.push(Float.parseFloat(item.substring(0, index)) * SMALL_STACK);
 					}
 				}
 				else
 				{
 					operands.push(Float.parseFloat(item.substring(0, index)) * FULL_STACK);
 				}
 				postfixStack.pop();
 			}
 			else
 			{
 				if (operands.size() < 2)
 				{
 					throw new ParserMathException("Insufficient operands. Check your formula.");
 				}
 
 				Float operand2 = operands.pop();
 				Float operand1 = operands.pop();
 				int temp = 0;
 
 				switch (postfixStack.pop().charAt(0))
 				{
 					case 'd': /* Random Number */
 						for (int i = 1; i <= operand1; i++)
 						{
 							temp = temp + NumberUtilities.randomNumber(1, operand2.intValue());
 						}
 						operands.push(new Float(temp));
 						break;
 					case '^': /* Power Operator */
 						operands.push(new Float(Math.pow(operand1, operand2)));
 						break;
 					case '*': /* Multiplication */
 						operands.push(operand1 * operand2);
 						break;
 					case '/': /* Division */
 						if (operand2 == 0)
 						{
 							throw new ParserMathException("Division by zero not supported.");
 						}
 						else
 						{
 							operands.push(operand1 / operand2);
 						}
 						break;
 					case '\\': /* Integer Division */
 						if (operand2 == 0)
 						{
 							throw new ParserMathException("Integer division by zero not supported.");
 						}
 						else
 						{
 							operands.push(new Float(operand1.intValue() / operand2.intValue()));
 						}
 						break;
 					case '%': /* Modulus */
 						if (operand2 == 0)
 						{
 							throw new ParserMathException("Modulus division by zero not supported.");
 						}
 						else
 						{
 							operands.push(operand1 % operand2);
 						}
 						break;
 					case '+': /* Addition */
 						operands.push(operand1 + operand2);
 						break;
 					case '_': /* Subtraction */
 						operands.push(operand1 - operand2);
 						break;
 				}
 			}
 		}
 
 		return operands.pop();
 	}
 
 	/**
 	 *  Determines whether a given operator is higher precedence than another
 	 *
 	 * @param left char for one operator
 	 * @param right char for the other operator.
 	 * @return true if the left is higher than the right.
 	 */
 	private boolean isHigher(char left, char right)
 	{
 		//	 OPERATORS		"d^*/%+_()"
 		switch (right)
 		{
 			case 'd':
 				return false;
 			case '^':
 				return (left == 'd');
 
 			case '*':
 			case '/':
 			case '%':
 			case '\\':
 				return (left == 'd' || left == '^');
 
 			case '+':
 			case '_':
 				return !(left == '(');
 
 			case '(':
 				return true;
 			default :
 				return false;
 		}
 	}
 }
