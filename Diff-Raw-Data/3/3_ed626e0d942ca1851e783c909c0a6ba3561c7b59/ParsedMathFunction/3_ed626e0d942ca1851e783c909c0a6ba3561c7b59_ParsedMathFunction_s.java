 package com.erikleeness.graph;
 
 import java.awt.Color;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.erikleeness.graph.expression.functions.Constant;
 import com.erikleeness.graph.expression.functions.Term;
 import com.erikleeness.graph.expression.functions.Variable;
 
 public class ParsedMathFunction extends MathFunction
 {
 	private String expression;
 	private Term term;
 	
 	public ParsedMathFunction(String informalExpression, Color color)
 	{
 		this.expression = informalToFormal(informalExpression);
 		term = termFromFormalExpression(this.expression);
 		this.setColor(color);
 	}
 	
 	public ParsedMathFunction(String expression)
 	{
 		this(expression, Color.black);
 	}
 	
 	public double calculate(double x)
 	{
 		return term.evaluate(x);
 	}
 	
 	private String informalToFormal(String input)
 	{
 		return input;
 	}
 	
 	private Term termFromFormalExpression(String formalExpression)
 	{
 		int parenIndex = formalExpression.indexOf("(");
 		String functionName = formalExpression.substring(0, parenIndex);
 		String functionParamString = formalExpression.substring(parenIndex + 1, formalExpression.length() - 1);
 		
 		// Special cases (they terminate the recursion)
 		
 		if (functionName.equals("variable")) {
 			if (functionParamString.isEmpty()) {
 				return new Variable();
 			} else {
 				return new Variable(functionParamString);
 			}
 		}
 		
 		if (functionName.equals("constant")) {
 			return new Constant( Double.parseDouble(functionParamString) );
 		}
 		
 		// Other, nonterminating cases
 		
 		List<String> paramsAsStrings = splitIntoArguments(functionParamString);
 		List<Term> params = new ArrayList<Term>();
 		for (String paramString : paramsAsStrings) {
 			params.add( termFromFormalExpression(paramString) );
 		}
 		
 		Term result = reflectivelyCreateTerm(functionName, params);
 		
 		return result;
 	}
 	
 	/**
 	 * Note that this class throws a lot of RuntimeExceptions. It should. Many of these things should
 	 * not happen unless a file is removed or a programmer otherwise screws up.
 	 * 
 	 * @param functionName
 	 * @param params
 	 * @return
 	 */
 	private Term reflectivelyCreateTerm(String functionName, List<?> params)
 	{
 		Class<?> resultClass;
 		try {
 			resultClass = Class.forName("com.erikleeness.graph.expression.functions." + functionName);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException("Someone passed ParsedMathFunction.termFromFormalExpression an expression" +
 					"with a bad function name.");
 		}
 		
 		Method factory;
 		try {
 			factory = resultClass.getMethod("of", List.class);
 		} catch (NoSuchMethodException e) {
 			throw new RuntimeException("Somehow the of() method failed to resolve on class " + resultClass);
 		} catch (SecurityException e) {
 			throw new RuntimeException("Somehow the of() method was denied access on class " + resultClass);
 		}
 		
 		Term result;
 		try {
 			result = (Term) factory.invoke(null, params);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException("Somehow the of() method was denied access on class " + resultClass);
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException("The of() method was not marked as static for some reason on class " + resultClass);
 		} catch (InvocationTargetException e) {
 			throw new RuntimeException(e);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * @precondition		paramString must be formatted as a list of terms (possible 0 or 1)
 	 * 						with no parens on the outside and commas separating them.
 	 * @param paramString
 	 * @return
 	 */
 	private List<String> splitIntoArguments(String paramString)
 	{
 		boolean insideParen = false;
 		int parenCount = 0;
 		int currentArgumentStartIndex = 0;
 		int index;
 		List<String> argumentStrings = new ArrayList<String>();
 		
 		for (index = 0; index < paramString.length(); index++) {
 			if (paramString.charAt(index) == '(') {
 				parenCount++;
 				insideParen = true;
 			} else if (paramString.charAt(index) == ')') {
 				parenCount--;
 				if (parenCount == 0) insideParen = false;
 			} else {
 				if ( ! insideParen && paramString.charAt(index) == ',') {
 					// We've got ourselves an old-fashioned argument separator!
 					argumentStrings.add( paramString.substring(currentArgumentStartIndex, index) );
 					currentArgumentStartIndex = index+1;
 				}
 			}
 		}
 		
 		if (argumentStrings.isEmpty()) {
 			// There were no non-paren'd argument separators, meaning there's only one argument
 			argumentStrings.add(paramString);
 		}
 		
 		return argumentStrings;
 	}
 }
