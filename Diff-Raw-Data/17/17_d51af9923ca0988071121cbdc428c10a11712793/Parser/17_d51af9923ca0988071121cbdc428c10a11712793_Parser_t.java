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
 		System.out.println(expression);
 		if(expression.contains("("))
 		{
 			StringBuilder builder = new StringBuilder(expression);
			return makeOperation(expression.replace(expression.substring(expression.lastIndexOf("("), indexOfMatch(builder.lastIndexOf("("),builder.toString())+1),
 					makeOperation(builder.substring(builder.lastIndexOf("(")+1, indexOfMatch(builder.lastIndexOf("("),builder.toString()))).evaluate().toString()).toString());
 		}
 		else if(isNumber(expression))
 			return new Real(expression);
 		else if(expression.equals("e"))
 			return Real.E;
 		else if(expression.equals(/*to be changed to greek*/"pi"))
 			return Real.PI;
 		else if(containsBinaryOperator(expression))
 		{
 			for(BinaryOperator b : BinaryOperator.values())
 				if(expression.contains(b.toString()))
 					return new BinaryOperation(makeOperation(expression.substring(0, expression.indexOf(b.toString()))),
 							b, makeOperation(expression.substring(expression.indexOf(b.toString())+b.toString().length(), expression.length())));
 		}
 		else if(startsWithUnaryOperator(expression))
 		{
 			for(UnaryOperator u : UnaryOperator.values())
 				if(expression.startsWith(u.toString()))
 					return new UnaryOperation(makeOperation(expression.substring(u.toString().length())), u);
 		}
 		else if(expression.endsWith(UnaryOperator.FACTORIAL.toString()))
 			return new UnaryOperation(makeOperation(expression.substring(0,expression.length()-1)), UnaryOperator.FACTORIAL);
 		return null;
 	}
 	
 	//helper functions -----------------------------------------------------------------------------------------
 	private static String preProcess(String expression)
 	{
 		StringBuilder input = new StringBuilder(expression.replaceAll("\\s",""));
 		//add implicit multiplication signs where needed
 		for(int i = 0; i<input.length()-1;i++)
 		{
 			if((input.charAt(i+1) == '(' && (input.charAt(i) == ')' || isNumeric(input.charAt(i)))) 
 				|| (input.charAt(i) == ')' && isNumeric(input.charAt(i+1))))
 				input.insert(i, '*');
 		}
 		return input.toString();
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
 	
 	private static boolean containsBinaryOperator(String input)
 	{
 		for(BinaryOperator b : BinaryOperator.values())
 			if(input.contains(b.toString()))
 				return true;
 		return false;
 	}
 	
 	private static boolean startsWithUnaryOperator(String input)
 	{
 		for(UnaryOperator u : UnaryOperator.values())
 			if(u != UnaryOperator.FACTORIAL && input.startsWith(u.toString()))
 				return true;
 		return false;
 	}
 	
 	private static boolean isNumeric(char c)
 	{
 		return (Character.isDigit(c) || c == 'e' || c == 'i' /*|| c == pi in greek*/);
 	}
 	
 	private static int indexOfMatch(int index, String expression)//index has to be index of an open parenthesis '('
 	{
 		int checker = 0;
 		for(int i = index;i<expression.length();i++)
 		{
 			if(expression.charAt(i) == '(')
 				checker++;
 			if(expression.charAt(i) == ')')
 				checker--;
 			if(checker == 0)
 				return i;
 		}
 		return index;
 	}
 }
