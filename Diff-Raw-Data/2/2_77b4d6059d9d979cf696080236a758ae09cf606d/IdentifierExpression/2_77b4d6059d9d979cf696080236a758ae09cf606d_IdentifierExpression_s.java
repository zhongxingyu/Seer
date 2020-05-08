 package com.josephcrawley.Spitfire.entities;
 
 import com.josephcrawley.util.Log;
 
 /**
  * An expression consisting of a single identifier referencing a variable.
  */
 public class IdentifierExpression extends VariableExpression {
 
     private String name;
     private Entity referent;
 
     public IdentifierExpression(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public Entity getReferent() {
         return referent;
     }
 
     /**
      * Returns whether the simple variable expression is writable, which it will be if and only
      * if its referent is a variable and not marked constant.
      */
     @Override
     public boolean isWritableLValue() {
        return referent instanceof Variable && !Variable.class.cast(referent).isConstant();
     }
 
     @Override
     public void analyze(Log log, SymbolTable table, Subroutine owner, boolean inLoop) {
         referent = table.lookup(name, log);
         if (referent instanceof Variable) {
             type = Variable.class.cast(referent).getType();
         } else if (referent instanceof Function) {
             type = Type.FUNCTION;
         } else {
            log.error("bad.expression");
         }
     }
 }
