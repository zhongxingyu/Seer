 package tv.justin.problems;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Stack;
 
 public class Prefixer {
 
 	public static void main(String[] args) {
 		long startTime = System.currentTimeMillis();
 		
 		int argumentNumber = args.length;
 		if( argumentNumber != 0){
 			boolean flag = false;
 			if( argumentNumber >= 2){
 				if(args[argumentNumber - 2].equals("-r")){
 					flag = true;
 				}else{
 					System.out.print("Unknown flag found and ignored:"+args[argumentNumber -2]+"\n");
 				}
 			}
 			String path =  args[args.length - 1];
 		    String expression = readFile(path);
 		    if(expression!= null && !expression.isEmpty()){
 		    	expression = expression.replaceAll("\\ ", "");
 		    	String prefixExpression = toPrefixExpression(expression);
 		    	if(flag){
 		    		prefixExpression = toReduceExpression(prefixExpression);
 		    	}
 		    	Parentheser parentheser = new Parentheser(prefixExpression);
 		    	prefixExpression = parentheser.getExpressionParenthese();
 		    	long stopTime = System.currentTimeMillis();
 			    long elapsedTime = stopTime - startTime;
 			    System.out.println(expression+" becomes "+ prefixExpression);
 			    System.out.println("Executed in "+elapsedTime+"ms");
 		    }else if(expression.isEmpty()){
 				System.out.print("File empty.\n");
 			}
 		}else{
 			System.out.print("Argument not found.\n");
 		}
 
 	}
 	
 	private static String toPrefixExpression(String exp){
 		//Step 1 :	Create the stack for operators
 		Stack<Character> stackOperator = new Stack<Character>();
 		//Step 2 : Reverse the infix expression
 		StringBuffer expression = new StringBuffer(exp).reverse();
 		StringBuffer prefixExpression = new StringBuffer();
 		int index = 0;
 		int lenght = expression.length();
 		char token ;
 		//Step 3: Analyze one character at time until all characters are proceed in the expression
 		while(index < lenght){
 			token = expression.charAt(index);
 			String operand ="";
 			while(isOperand(token)){
 				operand += String.valueOf(token);
 				if(index + 1 < lenght){
 					token = expression.charAt(++index);
 				}else{
 					break;
 				}
 			}
 			if(!operand.isEmpty()){
 				prefixExpression.append(" ").append(operand);
 			}
 			if(!isOperand(token)){
 				if(isBacketOpen(token)){
 					while(!stackOperator.isEmpty() && !isBacketClose(stackOperator.lastElement())){
 						prefixExpression.append(stackOperator.pop());
 					}
 					if(!stackOperator.isEmpty() && isBacketClose(stackOperator.lastElement())){
 						stackOperator.pop();
 					}
 				}else{
 					while(!stackOperator.isEmpty() && getPriorityStack(stackOperator.lastElement()) >= getPriority(token) ) {
 						prefixExpression.append(stackOperator.pop());
 					}
 					stackOperator.push(token);
 				}
 			}
 			index++;
 		}
 		//Step 4 :	Pop operators still in the stack and add these operators 
 		//to the prefix expression until the stack is empty
 		while(!stackOperator.isEmpty()){
 			prefixExpression.append(stackOperator.pop());
 		}
 
 		//Step 5 :	Reverse the prefix expression
 		prefixExpression = prefixExpression.reverse();
 		
 		return prefixExpression.toString();
 	}
 	
 	private static String toReduceExpression(String exp){
 		Stack<String> stackOperand = new Stack<String>();
 		StringBuffer expression = new StringBuffer(exp).reverse();
 		StringBuffer prefixExpression = new StringBuffer();
 		int index = 0;
 		int lenght = expression.length();
 		char token ;
 		while(index < lenght){
 			token = expression.charAt(index);
 			String operand ="";
 			while(isOperand(token)){
 				operand += String.valueOf(token);
 				if(index + 1 < lenght){
 					token = expression.charAt(++index);
 				}else{
 					break;
 				}
 			}
 			if(!operand.isEmpty()){
 				stackOperand.push(operand);
 			}
 			if(isOperator(token)){
 				String operand1 = stackOperand.pop().trim();
 				String operand2 = stackOperand.pop().trim();
 				stackOperand.push(calcul(operand1,operand2,token));
 			}
 			index++;
 		}
 		
 		while(!stackOperand.isEmpty()){
 			prefixExpression =   prefixExpression.append(stackOperand.pop());
 		}
		String[] tabExpression = prefixExpression.toString().split("\\ ");
		prefixExpression.delete(0, prefixExpression.length());
		for(int i=tabExpression.length - 1; i >= 0 ; i--){
			prefixExpression.append(tabExpression[i]+ " ");
		}
		return prefixExpression.toString();
 	}
 	
 	private static String calcul(String operand1, String operand2, char operator){
 		try{
			float i1 = Float.parseFloat(operand1);
			float i2 = Float.parseFloat(operand2);
 			float result = 0;
 			if( operator == '*'){
 				result = i1 * i2;
 			}else if(operator == '/' ){
 				result = i1 / i2;
 			}else if(operator == '+' ){
 				result = i1 + i2;
 			}else{
 				result = i1 - i2;
 			}
			return String.valueOf(result)+" ";
 		}catch(NumberFormatException e){
 			return " " +operand1 + " "+ operand2 + " "+ operator;
 		}
 
 	}
 	private static int getPriority(char operator){
 		//Bracket Open -> Bracket Close -> Division -> Multiplication -> Addition -> Subtraction
 		int priority = 0;
 		if(isBacketOpen(operator)){
 			priority = 5;
 		}else if(isBacketClose(operator)){
 			priority = 4;
 		}else if(isDivision(operator)){
 			priority = 3;
 		}else if (isMultiplication(operator)){
 			priority = 2;
 		}else if(isAddition(operator)){
 			priority = 1;
 		}
 		return priority;
 	}
 	
 	private static int getPriorityStack(char operator){
 		//Bracket Open -> Division -> Multiplication -> Addition -> Subtraction -> Bracket Close
 		int priority = 0;
 		if(isBacketOpen(operator)){
 			priority = 5;
 		}else if(isBacketClose(operator)){
 			priority = -1;
 		}else if(isDivision(operator)){
 			priority = 3;
 		}else if (isMultiplication(operator)){
 			priority = 2;
 		}else if(isAddition(operator)){
 			priority = 1;
 		}
 		return priority;
 	}
 	
 	private static boolean isOperator(char operator){
 		if(isOperatorStrong(operator)||isOperatorWeak(operator)){
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean isOperatorStrong(char operator){
 		if(operator == '*' || operator == '/' ){
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean isDivision(char operator){
 		if(operator == '/' ){
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean isMultiplication(char operator){
 		if(operator == '*' ){
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean isOperatorWeak(char operator){
 		if(operator == '+' || operator == '-' ){
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean isAddition(char operator){
 		if(operator == '+'  ){
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean isBacketOpen(char operator){
 		if(operator == '('){
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean isBacketClose(char operator){
 		if(operator == ')'){
 			return true;
 		}
 		return false;
 	}
 	
 	private static boolean isSpace(char operator){
 		if(operator == ' '){
 			return true;
 		}
 		return false;
 	}
 	private static boolean isOperand(char operand){
 		if(!isSpace(operand)&&!isBacketClose(operand)&&!isBacketOpen(operand)&&!isOperatorStrong(operand)&&!isOperatorWeak(operand)){
 			return true;
 		}
 		return false;
 	}
 	
 	private static String readFile(String path){
 		String strLine ="";
 		try {
 			FileInputStream fis = new FileInputStream(path);
 		    // Get the object of DataInputStream
 		    DataInputStream in = new DataInputStream(fis);
 		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		    //Read File Line By Line
 		    String buffer;
 		    while ( (buffer= br.readLine()) != null){
 		    	strLine += buffer;
 		    }
 		    //Close the input stream
 		    in.close();
 		    fis.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found.\n");
 		} catch (IOException e) {
 			System.out.println("Error I/O.\n");
 		}
 		return strLine;
 	}
 	
 }
