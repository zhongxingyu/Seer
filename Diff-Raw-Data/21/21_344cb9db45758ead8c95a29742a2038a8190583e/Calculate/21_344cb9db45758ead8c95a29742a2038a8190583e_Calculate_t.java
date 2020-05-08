 package com.example;
 
 import java.util.Hashtable;
 import java.util.Stack;
 
 public class Calculate {
 	public static String calculateExpr(String expr){
 		   Stack<Integer> exprStack = new Stack<Integer>();
 		   Stack<Character> opStack = new Stack<Character>();
 		   opStack.push('$');
 		   String val = "0";
 		   
 		   Hashtable<Character,Integer> opTable = new Hashtable<Character,Integer>();
 		   opTable.put('+', 1);
 		   opTable.put('-', 1);
 		   opTable.put('*', 2);
 		   opTable.put('/', 2);
 		   opTable.put('$', 0);
 		   for (int i=0; i<expr.length(); i++) {
 		       if (isNum(expr.charAt(i))) {
 		         val += expr.charAt(i);
 		       } else {
 		           if (!val.equals("0")){
 		             exprStack.push(Integer.parseInt(val));
 		           }
 		           val = "0";
 		           if ((opTable.get(expr.charAt(i))).hashCode()>(opTable.get(opStack.peek())).hashCode()){
 		             opStack.push(expr.charAt(i));
 		           }else {
 		             eval(exprStack, opStack);
 		             i--;
 		           }
 		       }
 		     if (i==expr.length()-1)   
 		       exprStack.push(Integer.parseInt(val));
 		   }
 		
		   while (exprStack.size()>1) {
 		     eval(exprStack, opStack);
 		   }
		   
		   if(exprStack.size() == 1){
			   return exprStack.pop().toString();
		   }else{
			   return "Error: This expression could not be evaluated.\nPerhaps you divided by zero?";
		   }
		 }//*/
 		
 		 private static void eval(Stack<Integer> exprStack, Stack<Character> opStack) {
 		   int ex2 = exprStack.pop().hashCode();
 		   int ex1 = exprStack.pop().hashCode();
 		   char op = (char)opStack.pop().hashCode();
 		   switch (op) {
 		     case '+': exprStack.push(ex1+ex2); break;
 		     case '-': exprStack.push(ex1-ex2); break;
 		     case '*': exprStack.push(ex1*ex2); break;
		     case '/': if(ex2 != 0){ exprStack.push(ex1/ex2); break; }
 		   }
		   //if it reaches here it's broken :(
 		 }
		 
 		 private static boolean isNum(char x) {
 		   return ('0' <= x && x <= '9');
 		 } //*/
 }
