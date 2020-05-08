 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jazinterpreter;
 import java.util.Stack;
 import java.util.HashMap;
 /**
  *
  * @author daniel
  */
 public class InstructionSet {
     //Arithmetic
     public static void add(Stack<String> input){
         String top;
         int a, b, total;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         top = input.pop();
         b = Integer.parseInt(top);
         
         total = a + b;
         
         input.push(Integer.toString(total));
     }
     public static void subtract(Stack<String> input){
         String top;
         int a, b, total;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         top = input.pop();
         b = Integer.parseInt(top);
         
         total = b - a;
         
         input.push(Integer.toString(total));
     }
     public static void multiply(Stack<String> input){
         String top;
         int a, b, total;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         top = input.pop();
         b = Integer.parseInt(top);
         
         total = a * b;
         
         input.push(Integer.toString(total));
     }
     public static void divide(Stack<String> input){
         String top;
         int a, b, total;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         top = input.pop();
         b = Integer.parseInt(top);
         
         total = b / a;
         
         input.push(Integer.toString(total));
     }
     public static void modulus(Stack<String> input){
         String top;
         int a, b, total;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         top = input.pop();
         b = Integer.parseInt(top);
         
         total = b % a;
         
         input.push(Integer.toString(total));
     }
     
     
     //Comparison
     public static void lessThan(Stack<String> input){
         String top, nextTop;
         int a, b, result;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         nextTop = input.pop();
         b = Integer.parseInt(nextTop);
         
        if(a < b)   result = 1;
         else        result = 0;
         
         input.push(nextTop);
         input.push(top);
         input.push(Integer.toString(result));
         
     }
     
     public static void lessThanOrEqual(Stack<String> input){
         String top, nextTop;
         int a, b, result;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         nextTop = input.pop();
         b = Integer.parseInt(nextTop);
         
        if(a <= b)  result = 1;
         else        result = 0;
         
         input.push(nextTop);
         input.push(top);
         input.push(Integer.toString(result));
     }
     
     public static void greaterThan(Stack<String> input){
         String top, nextTop;
         int a, b, result;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         nextTop = input.pop();
         b = Integer.parseInt(nextTop);
         
        if(a > b)   result = 1;
         else        result = 0;
         
         input.push(nextTop);
         input.push(top);
         input.push(Integer.toString(result)); 
     }
     
     public static void greaterThanOrEqual(Stack<String> input){
         String top, nextTop;
         int a, b, result;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         nextTop = input.pop();
         b = Integer.parseInt(nextTop);
         
        if(a >= b)  result = 1;
         else        result = 0;
         
         input.push(nextTop);
         input.push(top);
         input.push(Integer.toString(result));
     }
     
     public static void equal(Stack<String> input){
         String top, nextTop;
         int a, b, result;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         nextTop = input.pop();
         b = Integer.parseInt(nextTop);
         
         if(a == b)  result = 1;
         else        result = 0;
         
         input.push(nextTop);
         input.push(top);
         input.push(Integer.toString(result));
     }
     
     public static void notEqual(Stack<String> input){
         String top, nextTop;
         int a, b, result;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         nextTop = input.pop();
         b = Integer.parseInt(nextTop);
         
         if(a != b)  result = 1;
         else        result = 0;
         
         input.push(nextTop);
         input.push(top);
         input.push(Integer.toString(result));
     }
     
     
     //Output
     public static void print(Stack<String> input){
         System.out.println(input.peek());
     }
     
     public static void show(String input){
         if(input==null){
             System.out.print("\n");
         }else{
             System.out.println(input);
         }
     }
     
     //Control Flow
     public static int goTo(HashMap<String, Integer> labelLoc, String argument){
         return labelLoc.get(argument);
     }
     public static int goTrue(Stack<String> input, HashMap<String, Integer> labelLocs, String argument, int current){
         String top;
         int a;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         if(a == 1)
             return labelLocs.get(argument);
         else
             return ++current;
     }
     public static int goFalse(Stack<String> input, HashMap<String, Integer> labelLocs, String argument, int current){
         String top;
         int a;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         if(a == 0)
             return labelLocs.get(argument);
         else
             return ++current;
     }
     
     
     //Logical
     public static void logicalAnd(Stack<String> input){
         String top, nextTop;
         int a, b;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         nextTop = input.pop();
         b = Integer.parseInt(nextTop);
         
         input.push(Integer.toString(a&b)); 
     }
     public static void logicalOr(Stack<String> input){
         String top, nextTop;
         int a, b;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         nextTop = input.pop();
         b = Integer.parseInt(nextTop);
         
         input.push(Integer.toString(a|b)); 
     }
     public static void logicalNot(Stack<String> input){
         String top;
         int a;
         
         top = input.pop();
         a = Integer.parseInt(top);
         
         input.push(Integer.toString(~a)); 
     }
     
     //StackManipulation
     public static void push(Stack<String> input, String str){
         input.push(str);
     }
     public static void pop(Stack<String> input){
         input.pop();
     }
     public static void rvalue(Stack<String> input, HashMap<String, Integer> RMem, String var){
         int value = 0;
         if(RMem.containsKey(var)){
             value = RMem.get(var);
         }
         input.push(Integer.toString(value));
     }
     public static void lvalue(Stack<String> input, String var){
         input.push(var);
     }
     public static void assign(Stack<String> input, HashMap<String, Integer> LMem){
         String rval, lval;
         int rval_int;
         
         rval = input.pop();
         lval = input.pop();
         
         rval_int = Integer.parseInt(rval);
         
         LMem.put(lval, rval_int);
     }
     public static void copy(Stack<String> input){
         input.push(input.peek());
     }
 }
