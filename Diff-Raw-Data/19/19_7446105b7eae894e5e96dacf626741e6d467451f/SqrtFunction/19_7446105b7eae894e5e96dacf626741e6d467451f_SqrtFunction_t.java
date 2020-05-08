 package com.teamdev.students.calculator.implementation.operations;
 
 import com.teamdev.students.calculator.implementation.Associativity;
 
 import java.math.BigDecimal;
 
 public class SqrtFunction extends AbstractOperation {
     public SqrtFunction() {
         super(Associativity.NONE, 1, 1, 2, "sqrt");
     }
 
     @Override
     public BigDecimal getResult(BigDecimal[] arguments) {
         if (arguments.length != getArgumentsCount()) {
             return null;
         }
        if(arguments[0].compareTo(new BigDecimal("0")) < 0){
            return null;
        }
         return new BigDecimal(Math.sqrt(arguments[0].doubleValue()));
     }
 }
