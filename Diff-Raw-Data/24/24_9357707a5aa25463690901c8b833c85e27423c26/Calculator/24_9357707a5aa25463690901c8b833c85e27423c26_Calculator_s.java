 package calculator;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Stack;
 
 public class Calculator {
 
	public static boolean parseNumber(String token, Stack<Integer> stack) {
 		try {
			stack.push(Integer.parseInt(token));
 			return true;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 	}
 
	public static boolean parseOperator(String token, Stack<Integer> stack) {
 		Operator op = Operator.parse(token);
 		if (op == null) {
 			return false;
 		}
 
		int rhs = stack.pop(), lhs = stack.pop();
		stack.push(op.apply(lhs, rhs));
 		return true;
 	}
 
	public static int evaluate(String expression) {
		Stack<Integer> stack = new Stack<Integer>();
 		for (String token : expression.split(" ")) {
 			if (!parseNumber(token, stack) && !(parseOperator(token, stack)))
 				throw new IllegalArgumentException("invalid expression");
 		}
 		return stack.pop();
 	}
 
 	public static void main(String[] args) throws IOException {
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		while (true) {
			String expression = in.readLine();
			int value = evaluate(expression);
			System.out.println(value);
 		}
 	}
 }
