 package ru.free0u.calculator;
 
 import android.util.Log;
 
 public class MathParser {
 	final double eps = 1e-5;
 	
 	char[] operations = {'+', '-', '*', '/'};
 	int [] balance = null;
 	
 	
 	public MathParser() {
 		
 	}
 	
 	private int findOperationIndex(String exp) {
 		balance = new int[exp.length()];
 		int b = 0;
 		for (int i = 0; i < exp.length(); ++i) {
 			if (exp.charAt(i) == '(') {
 				++b;
 				balance[i] = b;
 			} else if (exp.charAt(i) == ')') {
 				balance[i] = b;
 				--b;
 			} else {
 				balance[i] = b;
 			}
 		}
 		
 		for (int i = 0; i < operations.length; ++i) {
 			char op = operations[i];
 			
 			for (int index = exp.length() - 1; index >= 0; --index) {
 				if (exp.charAt(index) == op) {
 					if (balance[index] == 0) {
 						return index;
 					}
 				}
 			}
 		}
 		return -1;
 	}
 	
 	private boolean isOperation(char c) {
 		for (int i = 0; i < operations.length; ++i) {
 			if (operations[i] == c) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public double evaluate(String exp) {
 		// empty string
 		if (exp.length() == 0) {
 			throw new IllegalArgumentException("Empty string");
 		}
 		
 		// find index of operation and calculating balance
 		int ind = findOperationIndex(exp);
 		
 		if (exp.charAt(0) == '(' && exp.charAt(exp.length() - 1) == ')') {
 			// need: (2+4*3)
 			// fail: (1)*(2)
 			boolean f = true;
 			for (int i = 0; i < balance.length; ++i) {
 				if (balance[i] < 1) {
 					f = false;
 					break;
 				}
 			}
 			if (f) {
 				return evaluate(exp.substring(1, exp.length() - 1));
 			}
 		}
 		
 		double res;
 		if (ind == -1) {
 			try {
 				res = Double.parseDouble(exp);
 				return res;
 			} catch (Exception e) {
 				throw new IllegalArgumentException("Error convert to double <" + exp + ">");
 			}
 		}
 		
 		String leftExp, rightExp;
 		leftExp = exp.substring(0, ind);
 		rightExp = exp.substring(ind + 1, exp.length());
 		
 		if (exp.charAt(ind) == '+' || exp.charAt(ind) == '-') {
 			if (ind > 0) {
 				if (isOperation(exp.charAt(ind - 1))) {
 					leftExp = exp.substring(0, ind - 1);
 					rightExp = exp.substring(ind, exp.length());
 					--ind;
 				}
 			}
 		}
 		
 		// unary operation
 		if (leftExp.equals("")) {
 			char op = exp.charAt(ind);
 			if (op == '+' || op == '-') {
 				return evaluate("0" + op + rightExp);
 			} else
 			{
 				throw new IllegalArgumentException("Error expression <" + op + rightExp + ">");
 			}
 		}
 		
 		double leftRes = evaluate(leftExp);
 		double rightRes = evaluate(rightExp);
 
 		
 		
 		switch (exp.charAt(ind)) {
 		case '+':
 			return leftRes + rightRes;
 		case '-':
 			return leftRes - rightRes;
 		case '*':
 			return leftRes * rightRes;
 		case '/':
 			if (Math.abs(rightRes) < 1e-8) { // divide by zero
 				throw new ArithmeticException();
 			}
 			return leftRes / rightRes;
 		default:
 			throw new IllegalArgumentException("Error operarion <" + exp.charAt(ind) + ">");
 		}
 		
 	}
 }
