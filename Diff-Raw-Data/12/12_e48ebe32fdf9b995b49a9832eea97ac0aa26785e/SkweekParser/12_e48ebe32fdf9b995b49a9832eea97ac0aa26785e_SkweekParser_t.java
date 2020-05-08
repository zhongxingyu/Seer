 package com.rburgos.skweek;
 
 import java.util.*;
 import java.util.regex.*;
 
 public class SkweekParser
 {
 	private static Stack<String> postfixStack;
 	private static Stack<String> operators;
 	private static final String REGEX = "((t\\b)|(x\\b)|(y\\b)|(z\\b)|" + 
 			"(\\d*\\.\\d+)|(\\d+)|([\\+\\-\\*/\\%\\(\\)]|" + 
 			"(\\|{1,2})|(\\&{1,2})|(\\<{1,2})|(\\>{1,2})|(\\^{1})))";
 	private static int t, x = 0, y = 0, z = 0;
 
 	private SkweekParser() { }
 	
	public static int evalToInt(String exp, int time, int xval, int yval, 
			int zval)
 	{
 		t = time;
 		x = xval;
 		y = yval;
 		z = zval;
 		String[] tokenArray = split(exp);
 		String[] postfixTokenArray = convertToPostfix(tokenArray);
 		return computePostfixToInt(postfixTokenArray);
 	}
 	
 	protected static String[] split(String expression)
 	{
 		Pattern pattern = Pattern.compile(REGEX);
 		Matcher matcher = pattern.matcher(expression);
 		ArrayList<String> tokenList = new ArrayList<>();
 
 		while(matcher.find()) 
 		{
 		    tokenList.add(matcher.group());
 		}
 
 		String[] tokenArray = new String[tokenList.size()];
 		
 		for (int i = 0; i < tokenList.size(); i++)
 		{
 			tokenArray[i] = tokenList.get(i);
 		}
 		
 		return tokenArray; 
 	}
 	
 	protected static String[] convertToPostfix(String[] tokenArray)
 	{
 		postfixStack = new Stack<>();
 		operators = new Stack<>();
 		for (int i = 0; i < tokenArray.length; i++)
 		{
 			if (tokenArray[i].equals("+") || tokenArray[i].equals("-"))
 			{
 				if (operators.isEmpty())
 				{
 					operators.push(tokenArray[i]);
 				}
 				else
 				{
 					if (operators.peek().equals("("))
 					{
 						operators.push(tokenArray[i]);
 					}
 					else
 					{
 						postfixStack.push(operators.pop());
 						operators.push(tokenArray[i]);						
 					}
 				}
 			}
 			else if (tokenArray[i].equals("*") || tokenArray[i].equals("/") || 
 					tokenArray[i].equals("^") || tokenArray[i].equals("<<") ||
 					tokenArray[i].equals(">>") || tokenArray[i].equals("|") ||
 					tokenArray[i].equals("&") || tokenArray[i].equals("%"))
 			{
 				if (operators.isEmpty())
 				{
 					operators.push(tokenArray[i]);
 				}
 				else
 				{
 					if (operators.peek().equals("*") || 
 							operators.peek().equals("/") || 
 							operators.peek().equals("^") ||
 							operators.peek().equals("<<") ||
 							operators.peek().equals(">>") ||
 							operators.peek().equals("|") ||
 							operators.peek().equals("&") ||
 							operators.peek().equals("%"))
 					{
 						postfixStack.push(operators.pop());
 						operators.push(tokenArray[i]);
 					}
 					else
 					{
 						operators.push(tokenArray[i]);
 					}
 				}
 			}
 			else if (tokenArray[i].equals("("))
 			{
 				operators.push(tokenArray[i]);
 			}
 			else if (tokenArray[i].equals(")"))
 			{
 				while (!operators.isEmpty())
 				{
 					String rightP = operators.pop();
 					if (!rightP.equals("("))
 					{
 						postfixStack.push(rightP);
 					}
 				}
 			}
 			else
 			{
 				postfixStack.push(tokenArray[i]);
 			}
 			
 		} // end for loop
 		
 		// push remaining elements from operator stack if it's not empty
 		while (!operators.isEmpty()) 
 		{
 			postfixStack.push(operators.pop());	
 		}
 		
 		String[] result = new String[postfixStack.size()];
 		
 		for (int i = 0; i < postfixStack.size(); i++)
 		{
 			result[i] = postfixStack.get(i);
 		}
 		
 		return result;
 	}
 	
 	protected static int computePostfixToInt(String[] postfixTokenArray)
 	{
 		Stack<String>calc = new Stack<>();
 		for (int i = 0; i < postfixTokenArray.length; i++)
 		{
 			switch(postfixTokenArray[i])
 			{
 			case "+":
 				calc.push(SkweekMath.add(calc.pop(), calc.pop()));
 				break;
 			case "-":
 				calc.push(SkweekMath.sub(calc.pop(), calc.pop()));
 				break;
 			case "*":
 				calc.push(SkweekMath.mul(calc.pop(), calc.pop()));
 				break;
 			case "/":
 				calc.push(SkweekMath.div(calc.pop(), calc.pop()));
 				break;
 			case "^":
 				calc.push(SkweekMath.pow(calc.pop(), calc.pop()));
 				break;
 			case "<<":
 				calc.push(SkweekMath.lshift(calc.pop(), calc.pop()));
 				break;
 			case ">>":
 				calc.push(SkweekMath.rshift(calc.pop(), calc.pop()));
 				break;
 			case "|":
 				calc.push(SkweekMath.or(calc.pop(), calc.pop()));
 				break;
 			case "&":
 				calc.push(SkweekMath.and(calc.pop(), calc.pop()));
 				break;
 			case "%":
 				calc.push(SkweekMath.mod(calc.pop(), calc.pop()));
 				break;
 			case "t":
 				calc.push(String.valueOf(t));
 				break;
 			case "x":
 				calc.push(String.valueOf(x));
 				break;
 			case "y":
 				calc.push(String.valueOf(y));
 				break;
 			case "z":
 				calc.push(String.valueOf(z));
 				break;
 			default:
 				calc.push(postfixTokenArray[i]);
 			}
 		}		
 		Double result = Double.parseDouble(calc.pop());
 		return result.intValue();
 	}
 }
