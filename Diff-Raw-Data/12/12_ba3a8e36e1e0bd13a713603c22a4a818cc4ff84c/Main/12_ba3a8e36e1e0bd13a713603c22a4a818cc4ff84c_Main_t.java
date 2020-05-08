 
 public class Main {
 
 	/**
 	 * @param args
 	 * @throws UnderflowException 
 	 */
 	public static void main(String[] args) throws UnderflowException {
 		Stack<String> stack = new Stack<String>();
 		Postfix postfix = new Postfix();
 		
		postfix.evaluate("3 5 +");
 		
 		stack.push("abdes");
 		stack.push("c");
 		
 		stack.top();
 	}
 
 }
