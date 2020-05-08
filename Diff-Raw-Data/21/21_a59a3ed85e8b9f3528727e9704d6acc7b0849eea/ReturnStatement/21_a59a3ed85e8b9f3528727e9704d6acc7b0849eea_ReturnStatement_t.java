 package com.josephcrawley.Spitfire.entities;
 
 import com.josephcrawley.util.Log;
 
 /**
  * A statement indicating a return from a procedure or function.
  */
 public class ReturnStatement extends Statement {
 
     private Expression expression;
 
     public ReturnStatement(Expression expression) {
         this.expression = expression;
     }
 
     public Expression getExpression() {
         return expression;
     }
 
     @Override
     public void analyze(Log log, SymbolTable table, Subroutine owner, boolean inLoop) {
         if (owner == null) {
             // At top-level, not inside any function
            log.error("return_outside_funciton");
 
         } else if (owner instanceof Procedure) {
             // Inside a procedure, we can't have a return expression
             if (expression != null) {
                log.error("return_value_not_allowed");
             }
 
         } else /* owner instanceof Function */ {
             // Returning something from a function, so typecheck
             expression.analyze(log, table, owner, inLoop);
             expression.assertAssignableTo(Function.class.cast(owner).getReturnType(),
                     log, "return.type.error");
         }
     }
 }
