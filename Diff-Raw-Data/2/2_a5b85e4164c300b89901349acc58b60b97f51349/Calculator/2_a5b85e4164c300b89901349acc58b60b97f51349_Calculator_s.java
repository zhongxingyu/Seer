 package com.kwiius.BukkitCalculator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 
 public class Calculator {
 	public static String calculate(String calculation) throws NumberFormatException, Exception {
 		Pattern p = Pattern.compile("[a-zA-Z]");
 		Matcher m = p.matcher(calculation);
 		if (m.find()) {
 			throw new Exception("You entered letters!");
 		}
 
 		Pattern p2 = Pattern.compile("[.]");
 		Matcher m2 = p2.matcher(calculation);
 		if (m2.find()) {
 			throw new Exception("No decimal numbers!");
 		}
 		
 		calculation = calculation.replace("**", "^");
 		
 		List<CalculationObject> rpn = shuntingYardAlgorithm(getTokenArray(calculation));
 		String[] strings = new String[rpn.size()];
 		for (int i = 0; i < rpn.size(); ++i) {
 			strings[i] = rpn.get(i).toString();
 		}
 		return Utils.implode(strings, " ");
 	}
 	
 	private int calculateFromRPN(List<CalculationObject> rpn) {
 		for (int i = 0; i < rpn.size(); ++i) {
 			
 		}
 		return 0;
 	}
 	
 	private static CalculationObject[] getTokenArray(String calculation)
 		throws NumberFormatException {
 		Scanner s = new Scanner(calculation);
 		String token;
 		List<String> tokens = new ArrayList<String>();
		while (null != (token = s.findInLine("-?\\d+|[+-/%^*()]")))
 		    tokens.add(token);
 		s.close();
 		CalculationObject[] result = new CalculationObject[tokens.size()];
 		for (int i = 0; i < tokens.size(); ++i) {
 			String t = tokens.get(i);
 			if (t.equals("+") || t.equals("-") || t.equals("*") 
 					|| t.equals("/") || t.equals("%") || t.equals("^")) {
 				CalculationOperator calculationOperator = new CalculationOperator(t.charAt(0));
 				CalculationObject calculationObject = new CalculationObject(calculationOperator);
 				result[i] = calculationObject;
 			} else if (t.equals("(") || t.equals(")")) {
 				CalculationParenthesis calculationParenthesis = new CalculationParenthesis(t.equals("("));
 				CalculationObject calculationObject = new CalculationObject(calculationParenthesis);
 				result[i] = calculationObject;
 			} else {
 				Integer number = Utils.parseInt(t);
 				if (number == null) {
 					throw new NumberFormatException("You did not enter a valid number (" + t + ").");
 				}
 				CalculationNumber calculationNumber = new CalculationNumber(number);
 				CalculationObject calculationObject = new CalculationObject(calculationNumber);
 				result[i] = calculationObject;
 			}
 		}
 		return result;
 	}
 	
 	private static List<CalculationObject> shuntingYardAlgorithm(CalculationObject[] input) throws Exception {
 		int inputIdx = 0;
 		Stack<CalculationObject> stack = new Stack<CalculationObject>();
 		List<CalculationObject> output = new ArrayList<CalculationObject>();
 		
 		while (inputIdx < input.length) {
 			CalculationObject token = input[inputIdx++];
 			
 			if (token.number != null) {
 				output.add(token);
 				continue;
 			}
 			
 			if (token.calculationOperator != null) {
 				while ( // Operator on top of stack
 						(!stack.isEmpty() && stack.peek().calculationOperator != null)
 						// 
 						&& (
 							  // if token is left-associative and token.precedence <= stack.peek.precedence
 							  (token.calculationOperator.leftAssociative 
 									  && token.calculationOperator.precedence <= stack.peek().calculationOperator.precedence)
 							  // or token.precedence < stack.peek.precedence
 							  || (token.calculationOperator.precedence < stack.peek().calculationOperator.precedence)
 						   )
 					  ) {
 					output.add(stack.pop());
 				}
 				stack.push(token);
 			}
 			
 			if (token.parenthesis != null) {
 				if (token.parenthesis.leftParenthesis) {
 					stack.push(token);
 				} else {
 					if (stack.isEmpty()) {
 						throw new Exception("Mismatched parentheses!");
 					}
 					while (!stack.isEmpty() && 
 							(stack.peek().parenthesis == null || stack.peek().parenthesis.leftParenthesis == false)) {
 						output.add(stack.pop());
 					}
 					// pop left parenthesis
 					if (stack.isEmpty()) {
 						throw new Exception("Mismatched parentheses!");
 					} else {
 						stack.pop();
 					}
 				}
 			}
 		}
 		
 		// No more tokens to be read...
 		while (!stack.isEmpty()) {
 			CalculationObject token = stack.pop();
 			if (token.parenthesis != null) {
 				throw new Exception("Mismatched parentheses!");
 			} else if (token.calculationOperator != null) {
 				output.add(token);
 			}
 		}
 		
 		return output;
 	}
 }
