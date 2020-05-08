 package com.electricdesignaz.calculator.api;
 
import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author mcmathe1
  * 
  * This class is the only main one utilized by the Calculator class and the UI.  It's main function is to
  * take in the string given by the user, and pick it apart by order of operations.  It uses the Expression classes
  * to evaluate the expression given by the user and handle any errors.  
  */
 public class Evaluator {
 	
 	private static final Logger logger = Logger.getLogger(Evaluator.class); 
 	
 	protected static final String operatorCharacterClass = "+\\-\\*/\\^";
 	protected static final String numberCharacterClass = "\\d+\\.?\\d*";
 	
 	protected String expression;
 	
 	/**
 	 * Public constructor that expects an unformatted expression string
 	 * 
 	 * @param expression - Expression to be formatted and evaluated.
 	 */
 	public Evaluator(String expression) {
 		this(expression, false);
 	}
 	
 	/**
 	 * @param expression - Expression to be evaluated.
 	 * @param formatted - Whether or not the given expression has been formatted.
 	 * 				If it has, passing true saves time.
 	 */
 	protected Evaluator(String expression, boolean formatted) {
 		this.expression = expression;
 		if (!formatted) {
 			format();
 		}
 	}
 	
 	/**
 	 * Formats and sets the expression string
 	 * 
 	 * @param expression
 	 */
 	public void setExpression(String expression) {
 		setExpression(expression, false);
 	}
 	
 	/**
 	 * Sets expression string and formats it the boolean <b>formatted</b> is false
 	 * 
 	 * @param expression
 	 * @param formatted
 	 */
 	protected void setExpression(String expression, boolean formatted) {
 		this.expression = expression;
 		if (!formatted) {
 			format();
 		}
 	}
 	
 	/**
 	 * Performs string manipulations on the expression string to make it ready to be evaluated by the Evaluator.
 	 * Invoked automatically by the public constructor and setExpression() method.
 	 */
 	protected void format() {
 		setExpression(expression.trim()
 				.toLowerCase()
 				.replaceAll("\\s+", "")
 				.replaceAll("[\\[{]", "\\(")
 				.replaceAll("[\\]}]", "\\)"), true);
 		
 		// format functions
 		String formattingExpr = expression.toLowerCase();
 		Function[] functions = Function.values();
 		for (Function f : functions) {
 			formattingExpr = formattingExpr.replace(f.toString().toLowerCase(), f.toString().toUpperCase());
 		}
 		setExpression(formattingExpr, true);
 		
 		setExpression(expression.replace("pi", "(" + String.valueOf(Math.PI) + ")")
 				.replace("e", "(" + String.valueOf(Math.E) + ")"), true);
 		
 		// Replace ')(' and '\d(' with ')*(' and '\d*(' to more easily show multiplication
 		StringBuilder sb = new StringBuilder(expression);
 		String regex = "[^A-Z" + operatorCharacterClass + "\\(]\\(";
 		Matcher matcher = Pattern.compile(regex).matcher(expression);
 		while (matcher.find()) {
 			sb.insert(matcher.end() - 1, "*");
 			matcher = Pattern.compile(regex).matcher(sb.toString());
 		}
 		setExpression(sb.toString(), true);
 		
 		regex = "\\)[^" + operatorCharacterClass + "\\)]";
 		matcher = Pattern.compile(regex).matcher(expression);
 		while (matcher.find()) {
 			sb.insert(matcher.start() + 1, "*");
 			matcher = Pattern.compile(regex).matcher(sb.toString());
 		}
 		setExpression(sb.toString(), true);
 	}
 	
 	/**
 	 * Takes the expression and looks through it to find every where parentheses are used to enclose a section of an expression
 	 * that should be evaluated first. 
 	 * 
 	 * @return Map<Integer, Integer> containing the indices of matching paren pairs
 	 * @throws ExpressionParseException
 	 */
 	private Map<Integer, Integer> getParenIndices() throws ExpressionParseException {
 		Map<Integer, Integer> parenPairs = new HashMap<Integer, Integer>();
 		
 		int tempIndex = 0;
 		int parenCounter = 0;
 		for (int i = 0; i < expression.length(); i++) {
 			if (expression.charAt(i) == '(') {
 				if (parenCounter == 0) {
 					tempIndex = i;
 				}
 				parenCounter++;
 			}
 			else if (expression.charAt(i) == ')') {
 				parenCounter--;
 				if (parenCounter == 0) {
 					parenPairs.put(tempIndex, i);
 				}
 			}
 		}
 		
 		if (parenCounter != 0) throw new ExpressionParseException("Uneven number of parentheses");
 		
 		return parenPairs;
 	}
 	
 	/**
 	 * Replaces an piece of the string expression with a given evaluation (DOES NOT EVALUATE)
 	 * This is simply a string replacement method
 	 * 
 	 * @param startPosition - first index to be replaced (included)
 	 * @param endPosition - index after last to be replaced (parameter index is excluded from replacement)
 	 * @param evaluation - double to replace the substring
 	 */
 	protected void replaceEvaluation(int startPosition, int endPosition, double evaluation) {
 		logger.trace("start position: " + startPosition + ", end position: " + endPosition);
 		StringBuilder sb = new StringBuilder(expression);
 		
		// TODO handle scientific notation for all numbers (possible solution: make all numbers be expressed in it)
		// Handle scientific notation: E[-15, infinity)
		DecimalFormat df = new DecimalFormat("0.000000000000000");
		logger.trace("Unformated: " + evaluation + "; Formated: " + df.format(evaluation));
		
		sb.replace(startPosition, endPosition, df.format(evaluation));
 		logger.trace(sb.toString());
		
 		setExpression(sb.toString(), true);
 	}
 	
 	public double evaluate() throws ExpressionParseException {
 		String regex = null;
 		
 		// If the expression can be parsed as a double, it is a number and the rest of the method can be skipped
 		try {
 			return Double.parseDouble(expression);
 		} catch (NumberFormatException ignore) {}
 		
 		Map<Integer, Integer> parenIndices = getParenIndices();
 		for (int firstParen : parenIndices.keySet()) {
 			String parenEnclosedExpr = expression.substring(firstParen + 1, parenIndices.get(firstParen));
 			logger.trace("Expression: " + expression + "; Expression piece: " + parenEnclosedExpr);
 			Evaluator parenEvaluator = new Evaluator(parenEnclosedExpr, true);
 			double evaluation = parenEvaluator.evaluate();
 			replaceEvaluation(firstParen, parenIndices.get(firstParen) + 1, evaluation);
 		}
 		
 		// Compute exponentials
 		regex = "(" + numberCharacterClass + "\\^-?[^" + operatorCharacterClass + "]+)";
 		ExponentialExpression exponentialExpression = new ExponentialExpression();
 		compute(regex, exponentialExpression);
 		resolveSigns();
 		
 		// Compute functions
 		FunctionExpression functionExpression = new FunctionExpression();
 		for (Function f : Function.values()) {
			regex = "(" + f.toString() + "-?[^" + operatorCharacterClass + "]+)";
 			compute(regex, functionExpression);
 			resolveSigns();
 		}
 
 		// Compute products and quotients
 		regex = "(" + numberCharacterClass + "[/\\*]-?" + numberCharacterClass + ")";
 		ProductExpression productExpression = new ProductExpression();
 		compute(regex, productExpression);
 		resolveSigns();
 		
 		// Compute sums and differences
 		regex = "^(-?" + numberCharacterClass + "[+\\-]-?" + numberCharacterClass + ")";
 		SumExpression sumExpression = new SumExpression();
 		compute(regex, sumExpression);
 		
		// TODO Fix errors that arise from scientific notation
		
 		try {
 			return Double.parseDouble(expression);
 		} catch (NumberFormatException nfe) {
 			logger.error("Error parsing expression as double: \"" + expression + "\"");
 			throw new ExpressionParseException("Invalid Expression \"" + expression + "\"", nfe);
 		}
 	}
 	
 	/**
 	 * Computes the type of arithmetic expressions it is passed
 	 * 
 	 * @param regex - the regular expression for picking out individual operations of the operation type
 	 * @param expressionType - Should be an object of the expression type that is to be computed (ie. subclass of AbstractExpression)
 	 * 
 	 * @throws ExpressionParseException
 	 */
 	private <T extends AbstractExpression> void compute(String regex, T expressionType) throws ExpressionParseException {
 		Matcher matcher = Pattern.compile(regex).matcher(expression);
 		while (matcher.find()) {
 			expressionType.setExpression(matcher.group());
 			double result = expressionType.eval();
 			replaceEvaluation(matcher.start(), matcher.end(), result);
 			matcher = Pattern.compile(regex).matcher(expression);
 		}
 	}
 	
 	/**
 	 * Fixes plus and minus sign conflicts in expression
 	 * eg. changes 1--1 to 1+1, 1+-1 to 1-1 and 1++++1 to 1+1
 	 */
 	private void resolveSigns() {
 		while (expression.contains("++") || expression.contains("--") || expression.contains("+-") || expression.contains("-+")) {
 			setExpression(expression
 					.replaceAll("\\-\\-", "+")
 					.replaceAll("\\++", "+")
 					.replaceAll("\\-\\+", "-")
 					.replaceAll("\\+\\-", "-")
 					, true);
 		}
 	}
 }
