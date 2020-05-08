 package br.com.igorribeirolima.fx.function;
 
 import br.com.igorribeirolima.fx.api.Expression;
 
 class FunctionHourToNumber extends Expression {
   
   protected FunctionHourToNumber() {
     super(Function.HourToNumber.regex());
   }
 
   public Double calculate(String expression) {
     String[] valores = Function.HourToNumber.takeOfFunctionName(expression).split(":");
     Double result = 0.0;
     int expoente = 0;
     for (String value : valores)
       result+=Double.parseDouble(value)/Math.pow(60, expoente++);
     
     return result;
     
   }
 
 }
