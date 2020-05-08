 package com.josephcrawley.Spitfire.entities;
 
 import com.josephcrawley.util.Log;
 
 /**
  * A loop that executes as long as a single expression is true.
  */
 public class WhileLoop extends Statement {
 
     private Expression condition;
     private Block body;
 
     public WhileLoop(Expression condition, Block body) {
         this.condition = condition;
         this.body = body;
     }
 
     public Expression getCondition() {
         return condition;
     }
 
     public Block getBody() {
         return body;
     }
 
     @Override
     public void analyze(Log log, SymbolTable table, Subroutine owner, boolean inLoop) {
         condition.analyze(log, table, owner, inLoop);
        condition.assertBoolean("while loop", log);
         body.analyze(log, table, owner, true);
     }
 }
