 package com.electricdesignaz.calculator.api;
 
 import org.apache.log4j.Logger;
 
 
 public class FunctionExpression extends AbstractExpression {
 	
 	private static final Logger logger = Logger.getLogger(FunctionExpression.class);
 	
 	public FunctionExpression() {
 		super();
 	}
 	
 	public FunctionExpression(String expression) {
 		super(expression);
 	}
 	
 	@Override
 	public double eval() throws ExpressionParseException {
		// TODO fix answers that are not quite exact (ie. tan(pi/2) ==> -2.xxxxxxxE25)
 		for (Function f : Function.values()) {
 			if (expression.startsWith(f.toString())) {
 				
 				double functionInput = new Evaluator(expression.substring(f.toString().length())).evaluate();
 				
 				if (f.equals(Function.ARCCOS)) {
 					return Math.acos(functionInput);
 				} else if (f.equals(Function.ARCSIN)) {
 					return Math.asin(functionInput);
 				} else if (f.equals(Function.ARCTAN)) {
 					return Math.atan(functionInput);
 				} else if (f.equals(Function.COS)) {
 					return Math.cos(functionInput);
 				} else if (f.equals(Function.SIN)) {
 					return Math.sin(functionInput);
 				} else if (f.equals(Function.TAN)) {
 					return Math.tan(functionInput);
 				} else if (f.equals(Function.SEC)) {
 					return 1 / Math.cos(functionInput);
 				} else if (f.equals(Function.CSC)) {
 					return 1 / Math.sin(functionInput);
 				} else if (f.equals(Function.COT)) {
 					return 1 / Math.tan(functionInput);
 				} else if (f.equals(Function.LN)) {
 					return Math.log(functionInput);
 				} else if (f.equals(Function.LOG)) {
 					return Math.log10(functionInput);
 				} else if (f.equals(Function.SQRT)) {
 					return Math.sqrt(functionInput);
 				} else {
 					logger.error("Very unexpected error: expression (" + expression + ") began with Function (" + f.toString() + ") but was not handled");
 					throw new ExpressionParseException("Invalid Function (" + f.toString() + ") in expression: \"" + expression + "\"");
 				}
 			}
 		}
 		
 		logger.error("Expression (" + expression + ") did not begin with an implemented Function");
 		throw new ExpressionParseException("Invalid Functional expression: \"" + expression + "\"");
 	}
 	
 }
