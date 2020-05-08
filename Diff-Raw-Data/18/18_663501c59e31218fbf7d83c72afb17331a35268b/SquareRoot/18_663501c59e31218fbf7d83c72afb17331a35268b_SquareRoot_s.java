 package org.fit.cvut.mvi.model.functions;
 
 import java.util.List;
 
 public class SquareRoot extends AbstractFunction {
 
     @Override
     public int arity() {
         return 1;
     }
 
     @Override
     public String name() {
         return "SquareRoot";
     }
 
     @Override
     protected String safeCode(List<String> arguments) {
        return "(sqrt " + arguments.get(0) + ")";
     }
 
 }
