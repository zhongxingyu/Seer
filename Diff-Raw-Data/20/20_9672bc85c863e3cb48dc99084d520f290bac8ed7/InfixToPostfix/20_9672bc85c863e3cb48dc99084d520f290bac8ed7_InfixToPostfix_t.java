 package Homework3;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.Stack;
 import java.util.StringTokenizer;
 import java.text.DecimalFormatSymbols;
 import java.util.Comparator;
 import java.util.Arrays;
 
 /**
  * Converts infix expressions to postfix expressions, and evaluates them.
  */
 
 public class InfixToPostfix 
 {
 	ArrayBlockingQueue<String> doubleQueue; //check whether this needs char or string
 	Stack<String> tokenStack;
 	String expression;
 
 	public InfixToPostfix(String expression)
 	{
 		// OperatorComparator comparator = new OperatorComparator();
 		doubleQueue = new ArrayBlockingQueue<String>(expression.length());
 		tokenStack = new Stack<String>();
 		this.expression = expression;
 	}
 
 	private void convertExprToPostfix()
 	{
 		String eval;
 		String[] operators = {"+","-","*","/"};
 		StringTokenizer tokenizer = new StringTokenizer(expression, " +-*/()", true);
 		while (tokenizer.hasMoreTokens())
 		{
 			//TODO
 			eval = tokenizer.nextToken(); 																								// get the next token
 			if (isStringNumeric(eval))																										// if it is a constant, enqueue
 			{
 				doubleQueue.add(eval);	
 			}
 			else if (eval.equals("("))																										// if token is a (, push into stack
 			{
 				tokenStack.push(eval);
 			}
 			else if (Arrays.asList(operators).contains(eval))															// if the token is an operator
 			{
 				if (tokenStack.isEmpty() || compareOperators(eval, tokenStack.peek()) > 0) 	// if stack is empty, or the if the priority of the stack has a lower priority than that of the token
 				{
 					tokenStack.push(eval);
 				}
 				else
 				{
					while (!tokenStack.isEmpty() && !tokenStack.peek().equals("("))
 					{
 						doubleQueue.add(tokenStack.pop());
 					}
 					tokenStack.push(eval);
 				}
 			}
 
 			if (tokenStack.peek().equals(")"))
 			{
 				while (!tokenStack.isEmpty() && !tokenStack.peek().equals("("))
 				{
 					doubleQueue.add(tokenStack.pop());
 				}
 				tokenStack.pop();
 			}
 		}
 
 		while (!tokenStack.isEmpty())
 		{
 			doubleQueue.add(tokenStack.pop());
 		}
 	}
 
 	//TODO i'll get to this
	private double evalPostFix()
	{
		Stack<Double> numbers = new Stack<Double>();
		String[] operators = {"*","/","+","-",")","("};
		//TODO just to shut up the linter
		return 0.0;
 
	}
 
 	private boolean isStringNumeric(String str)
 	{	
     DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
     char localeMinusSign = currentLocaleSymbols.getMinusSign();
 
     if ( !Character.isDigit( str.charAt( 0 ) ) && str.charAt( 0 ) != localeMinusSign ) return false;
 
     boolean isDecimalSeparatorFound = false;
     char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();
 
     for ( char c : str.substring( 1 ).toCharArray() )
     {
         if ( !Character.isDigit( c ) )
         {
             if ( c == localeDecimalSeparator && !isDecimalSeparatorFound )
             {
                 isDecimalSeparatorFound = true;
                 continue;
             }
             return false;
         }
     }
     return true;
 	}
 
 	/**
 	 * compares operators (as defined in operators in convertExprToPostfix() ) to determine
 	 * priority.
 	 * @param  op1 first operator
 	 * @param  op2 second operator
 	 * @return     returns -1 if op1 has lesser priority, 1 if op1 has greater priority, or 
	 * 0 if op1 has equal priority. Returns -2 otherwise.
 	 */
 	private int compareOperators(String op1, String op2)
 	{
 		if ( (op1.equals("+") || op1.equals("-")) && ( (op2.equals("*") || op2.equals("/") ) ) )
 			return -1;
 		else if ( (op2.equals("+") || op2.equals("-")) && ( (op1.equals("*") || op1.equals("/") ) ) )
 			return 1;
 		else if ( (op1.equals("/") || op1.equals("*")) && ( (op2.equals("*") || op2.equals("/") ) ) )
 			return 0;
 		else if ( (op1.equals("+") || op1.equals("-")) && ( (op2.equals("+") || op2.equals("-") ) ) )
 			return 0;
 		else
			return -2;
 	}
 }
