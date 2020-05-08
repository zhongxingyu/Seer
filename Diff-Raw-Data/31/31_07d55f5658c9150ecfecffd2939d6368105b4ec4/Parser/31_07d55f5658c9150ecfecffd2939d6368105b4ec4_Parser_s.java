 package core.scientrix.parsing;
 
 import core.scientrix.math.Real;
 import core.scientrix.syntax.BinaryOperation;
 import core.scientrix.syntax.BinaryOperator;
 import core.scientrix.syntax.Operation;
 import core.scientrix.syntax.UnaryOperation;
 import core.scientrix.syntax.UnaryOperator;
 
 public class Parser 
 {
 	public static Operation makeOperation(String input)
 	{
 		String expression = preProcess(input);
 		if(isNumber(expression))
 			return new Real(expression);
 		else if(expression.equals("e"))
 			return Real.E;
 		else if(expression.equals(/*to be changed to greek*/"pi"))
 			return Real.PI;
 		else if(expression.startsWith(UnaryOperator.ABSOLUTE.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(3)), UnaryOperator.ABSOLUTE);
 		else if(expression.startsWith(UnaryOperator.ARCCOS.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(6)), UnaryOperator.ARCCOS);
 		else if(expression.startsWith(UnaryOperator.ARCSIN.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(6)), UnaryOperator.ARCSIN);
 		else if(expression.startsWith(UnaryOperator.ARCTAN.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(6)), UnaryOperator.ARCSIN);
 		else if(expression.startsWith(UnaryOperator.COSINE.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(3)), UnaryOperator.COSINE);
 		else if(expression.endsWith(UnaryOperator.FACTORIAL.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(0,expression.length()-1)), UnaryOperator.FACTORIAL);
 		else if(expression.startsWith(UnaryOperator.LN.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(2)), UnaryOperator.LN);
 		else if(expression.startsWith(UnaryOperator.LOG.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(3)), UnaryOperator.LOG);
 		else if(expression.startsWith(UnaryOperator.NEGATE.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(1)), UnaryOperator.NEGATE);
 		else if(expression.startsWith(UnaryOperator.SINE.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(3)), UnaryOperator.SINE);
 		else if(expression.startsWith(UnaryOperator.SQROOT1.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(4)), UnaryOperator.SQROOT1);
 		else if(expression.startsWith(UnaryOperator.SQROOT2.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(1)), UnaryOperator.SQROOT2);
 		else if(expression.startsWith(UnaryOperator.TANGENT.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(3)), UnaryOperator.TANGENT);
		else
		{
			for(BinaryOperator b : BinaryOperator.values())
				if(expression.contains(b.toString()))
					return new BinaryOperation(makeOperation(expression.substring(0, expression.indexOf(b.toString()))),
							b, makeOperation(expression.substring(expression.indexOf(b.toString())+b.toString().length(), expression.length())));
		}
 		return null;
 	}
 	
 	private static String preProcess(String expression)
 	{
 		return expression;
 	}
 	
 	private static boolean isNumber(String input)
 	{
 		try
 		{
 			Double.parseDouble(input);
 			return true;
 		}
 		catch(Exception e)
 		{
 			return false;
 		}
 	}
 }
