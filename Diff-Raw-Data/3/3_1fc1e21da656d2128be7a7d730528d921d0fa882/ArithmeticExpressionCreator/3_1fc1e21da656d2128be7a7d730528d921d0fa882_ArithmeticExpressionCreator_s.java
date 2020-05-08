 package cvut.fit.dpo.pr2;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.Queue;
 import java.util.regex.Pattern;
 
 import cvut.fit.dpo.arithmetic.AddOperator;
 import cvut.fit.dpo.arithmetic.ArithmeticExpression;
 import cvut.fit.dpo.arithmetic.BinaryOperator;
 import cvut.fit.dpo.arithmetic.NumericOperand;
 import cvut.fit.dpo.arithmetic.SubtractOperator;
 
 
 /**
  * Stupid class which can create some {@link ArithmeticExpression}s.
  * 
  * @author Jan Kurš
  *
  */
 public class ArithmeticExpressionCreator
 {
 	/**
 	 * Creates 3 - (1 + 2)
 	 * 
 	 * This is ugly. I don't like creating expressions in this
 	 * 	form. I never know, what expression I have created...
 	 */
 	public ArithmeticExpression createExpression1()
 	{
 		NumericOperand op1 = new NumericOperand(1);
 		NumericOperand op2 = new NumericOperand(2);
 		NumericOperand op3 = new NumericOperand(3);
 		
 		BinaryOperator o2 = new AddOperator(op1, op2);
 		BinaryOperator o1 = new SubtractOperator(op3, o2);
 		
 		return o1;
 	}
 
 	/**
 	 * Creates (3 - 1) + 2
 	 *
 	 * This is ugly. I don't like creating expressions in this
 	 * 	form. I never know, what expression I have created...
 	 */
 	public ArithmeticExpression createExpression2()
 	{
 		NumericOperand op1 = new NumericOperand(1);
 		NumericOperand op2 = new NumericOperand(2);
 		NumericOperand op3 = new NumericOperand(3);
 		
 		BinaryOperator o1 = new SubtractOperator(op3, op1);
 		BinaryOperator o2 = new AddOperator(o1, op2);
 		
 		return o2;
 	}
 	
 	/**
 	 * Creates any expression from the RPN input. This is nice and
 	 * 	universal. 
 	 * 
 	 * @see http://en.wikipedia.org/wiki/Reverse_Polish_notation
 	 * 	
 	 * @param input in Reverse Polish Notation
 	 * @return {@link ArithmeticExpression} equivalent to the RPN input.
 	 */
 	public ArithmeticExpression createExpressionFromRPN(String input)
 	{
 		// Good entry point for Builder :)
 		return new RPNParser().parse(input);
 	}
 }
 
 class RPNParser {
 	
 	public ArithmeticExpression parse(String rpnInput) {
 		Queue<Character> unprocessed = new ArrayDeque<>();
 		for (int charI = 0; charI < rpnInput.length(); charI++) {
 			char c = rpnInput.charAt(charI);
 			if (c == ' ') {
 				continue;
 			}
 			unprocessed.offer(c);
 		}
 		
 		Deque<ArithmeticExpression> workStack = new ArrayDeque<>();
 
 		while (! unprocessed.isEmpty()) {
 			Character c = unprocessed.poll();
 			String s = c.toString();
 			if (isNumericOperand(s)) {
 				Integer value = Integer.parseInt(s);  
 				workStack.push(new NumericOperand(value));
 			} else if (isBinaryOperator(s)) {
				ArithmeticExpression leftOperand = workStack.pop();
 				ArithmeticExpression rightOperand = workStack.pop();
 				ArithmeticExpression binary;
 				if (c.equals('+')) {
 					binary = new AddOperator(leftOperand, rightOperand);
 				} else if (c.equals('-')) {
 					binary = new SubtractOperator(leftOperand, rightOperand);
 				} else {
 					throw new IllegalArgumentException("Unknown binary operator");
 				}
 				workStack.push(binary);
 			}
 		}
 		
 		return workStack.pop();
 	}
 	
 	private boolean isBinaryOperator(String s) {
 		final String REGEXP = "[+-]";
 		return Pattern.matches(REGEXP, s);
 	}
 	
 	private boolean isNumericOperand(String s) {
 		final String REGEXP = "\\d";
 		return Pattern.matches(REGEXP, s);
 	}
 	
 }
