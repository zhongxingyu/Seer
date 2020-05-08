 package se.c0la.calc;
 
 import java.util.*;
 import java.math.*;
 
 public class Evaluator
 {
 	private boolean debug;
 
 	private MathContext ctx;
 
 	private Map<String, BigDecimal> variables;
 	private Map<String, MathFunction> functions;
 
 	public Evaluator()
 	{
 		this(false);
 	}
 	
 	public Evaluator(boolean debug)
 	{
 		this.debug = debug;
 
 		variables = new HashMap<String, BigDecimal>();
 		functions = new HashMap<String, MathFunction>();
 		ctx = new MathContext(10);
 	
 		defineConstants();
 		
 		addFunction("sin", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.sin(arg.doubleValue()), ctx); }
 		});
 		addFunction("cos", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.cos(arg.doubleValue()), ctx); }
 		});
 		addFunction("tan", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.tan(arg.doubleValue()), ctx); }
 		});
 		addFunction("ln", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.log(arg.doubleValue()), ctx); }
 		});
 		addFunction("log", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.log(arg.doubleValue()), ctx); }
 		});
 		addFunction("log10", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.log(arg.doubleValue()) / Math.log(10.0), ctx); }
 		});
 		addFunction("sqrt", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.sqrt(arg.doubleValue()), ctx); }
 		});
 		addFunction("asin", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.asin(arg.doubleValue()), ctx); }
 		});
 		addFunction("acos", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.acos(arg.doubleValue()), ctx); }
 		});
 		addFunction("atan", new MathFunction() {
 			public BigDecimal apply(BigDecimal arg)
 			{ return new BigDecimal(Math.atan(arg.doubleValue()), ctx); }
 		});
 	}
 
 	private void defineConstants()
 	{
 		addVariable("pi", new BigDecimal(Math.PI, ctx));
 		addVariable("e", new BigDecimal(Math.E, ctx));
 	}
 
 	public void setPrecision(int precision)
 	{
 		ctx = new MathContext(precision);
 		defineConstants();
 	}
 	
 	public void setDebug(boolean debug)
 	{
 		this.debug = debug;
 	}
 	
 	public void addVariable(String name, BigDecimal value)
 	{
 		variables.put(name, value);
 	}
 	
 	public void addFunction(String name, MathFunction function)
 	{
 		functions.put(name, function);
 	}
 
 	private BigDecimal evaluate(ASTNode node, int level)
 	throws EvaluationException
 	{
 		try {
 			Lexeme lexeme = node.getLexeme();
 		
 			if (debug) {
 				StringBuffer spaces = new StringBuffer();
 				for (int i = 0; i < 2*level; i++) {
 					spaces.append(" ");
 				}
 			
 				if (lexeme.getType() == TokenType.NUMBER || lexeme.getType() == TokenType.VARIABLE) {
 					System.out.println(spaces.toString() + lexeme.getValue());
 				}
 				else if (lexeme.getType() == TokenType.FUNCTION) {
 					System.out.println(spaces.toString() + lexeme.getValue() + "()");
 				}
 				else {
 					System.out.println(spaces.toString() + lexeme.getType());
 				}
 			}
 		
 			if (lexeme.getType() == TokenType.NUMBER) {
 				return new BigDecimal(lexeme.getValue(), ctx);
 			}
 			else if (lexeme.getType() == TokenType.VARIABLE) {
 				String name = lexeme.getValue();
 				if (!variables.containsKey(name)) {
 					throw new UndefinedVariableException("Variable " + name + " is undefined.");
 				}
 			
 				return variables.get(name);
 			}
 			else if (lexeme.getType() == TokenType.FUNCTION) {
 				String name = lexeme.getValue();
 				if (!functions.containsKey(name)) {
 					throw new UndefinedFunctionException("Function " + name + " is undefined.");
 				}
 			
 				MathFunction func = functions.get(name);
 				BigDecimal arg = evaluate(node.getLeft(), level + 1);
 			
 				return func.apply(arg);
 			}
 		
 			BigDecimal left = null;
 			if (node.getLeft() != null) {
 				left = evaluate(node.getLeft(), level + 1);
 			}
 		
 			BigDecimal right = null;
 			if (node.getRight() != null) {
 				right = evaluate(node.getRight(), level + 1);
 			}
 		
 			BigDecimal result = null;
 			switch (lexeme.getType()) {
 				case PLUS:
 					if (right == null) {
 						result = left;
 					} else {
 						result = left.add(right);
 					}
 					break;
 				case MINUS:
 					if (right == null) {
 						result = left.negate();
 					} else {
 						result = left.subtract(right);
 					}
 					break;
 				case ASTERIX:
 					result = left.multiply(right);
 					break;
 				case SLASH:
					result = left.divide(right);
 					break;
 				case EXPONENT:
 					result = new BigDecimal(Math.pow(left.doubleValue(), right.doubleValue()), ctx);
 					break;
 			}
 		
 			return result;
 		}
 		catch (NumberFormatException e) {
 			throw new EvaluationException("Invalid number specified");
 		}
 		catch (EvaluationException e) {
 			throw e;
 		}
 		catch (Exception e) {
 			if (debug) {
 				e.printStackTrace();
 			}
 
 			throw new EvaluationException("Evaluation of expression failed");
 		}
 	}
 	
 	public BigDecimal evaluate(ASTNode node)
 	throws EvaluationException
 	{
 		BigDecimal res = evaluate(node, 0);
 		
 		if (debug) {
 			System.out.println();
 		}
 		
 		return res;
 	}
 }
