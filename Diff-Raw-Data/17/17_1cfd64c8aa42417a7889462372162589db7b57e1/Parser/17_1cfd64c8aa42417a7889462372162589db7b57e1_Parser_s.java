 package project_385;
 
 public class Parser {
 	
 	ADTList<String> tokens = new ADTList<String>();
 	
 	public Parser(String input){
 		tokens = tokenizer(input);
 	}
 	
 	private int precedence(String token) {
 		switch (token) {
 			case "(":
 			case ")":
 				return 1;
 			case "*":
 			case "/":
 				return 3;
 			case "+":
 			case "-":
 				return 2;
 			default: // variable or number
 				return -1;
 		}
 	}
 	
 	private boolean isOperator(char ch) {
 		switch (ch) {
 			//case '(':		test, parenths not operators
 			//case ')':
 			case '*':
 			case '/':
 			case '+':
 			case '-':
 				return true;
 			default:
 				return false;
 		}
 	}
 	
 	public String infix2Postfix(){
 		
 		ADTStack<String> stack = new ADTStack<String>();
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < tokens.size(); i++) {
 			if (tokens.get(i).equals("(")) {
 				stack.push(tokens.get(i));
 			} else if (tokens.get(i).equals(")")) {
 				while (!stack.isEmpty() && !stack.peek().equals("(")) {
 					buffer.append(stack.pop()).append(' ');
 				}
 				if (!stack.peek().equals("(")) {
 					return("Mismatched Parentheses");
 				} else {
 					stack.pop(); // assume it's a left paren
 				}
 			} else if (isOperator(tokens.get(i).charAt(0))) {
 				while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(tokens.get(i))) {
 					buffer.append(stack.pop()).append(' ');
 				}
 				stack.push(tokens.get(i));
 			} else {
 				buffer.append(tokens.get(i)).append(' ');
 			}
 		}
 		while (!stack.isEmpty()) {
 			buffer.append(stack.pop()).append(' ');
 		}
 		buffer.setLength(buffer.length() - 1);
 		return buffer.toString();
 	}
 	
 	public String postfix2Infix(){
		
 		ADTStack<String> stack = new ADTStack<String>();
 		String output="", operand1, operand2;
 		
		for (int i = 0; i < tokens.size(); i++) {
			if (isOperator(tokens.get(i).charAt(0))) {
 				operand2 = stack.pop();
 				operand1 = stack.pop();
 				if (operand1 == null || operand2 == null) {
					return null;
 				}
				output += "(" + operand1 + tokens.get(i) + operand2 + ")";
				stack.push(output);
 			} else {
				stack.push(tokens.get(i));
 			}
 		}
 		
 		output = stack.pop();
 		
 		if (!stack.isEmpty()) {
 			return null;
 		}
 		
 		return output;
 	}
 	
 	public ADTList<String> tokenizer(String input) {
 	
 		ADTList<String> tokens = new ADTList<String>();
 		
 		for (int i = 0; i < input.length(); i++) {
 			int start = i;
 			char ch = input.charAt(i);
 			if (isOperator(ch) || ch=='(' || ch==')') {
 				tokens.add(String.valueOf(ch));
 			} else if (Character.isLetter(ch)) {
 				for (; i+1 < input.length() && (Character.isLetter(input.charAt(i+1)) || Character.isDigit(input.charAt(i+1))); i++);
 				tokens.add(input.substring(start, i+1));
 			} else if (Character.isDigit(ch)) {
 				for (; i+1 < input.length() && Character.isDigit(input.charAt(i+1)); i++);
 				tokens.add(input.substring(start, i+1));
 			}
 		}
 		return tokens;
 	}
 	
 	public boolean isValid(){
 		return infix2Postfix() != null;
 	}
 }
