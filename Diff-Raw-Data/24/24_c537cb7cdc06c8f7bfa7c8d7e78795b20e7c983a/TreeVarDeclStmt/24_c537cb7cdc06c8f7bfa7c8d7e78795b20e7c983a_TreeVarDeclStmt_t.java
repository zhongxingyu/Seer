 package com.lolcode.tree;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 7/12/13
  * Time: 4:39 PM
  */
 
 /**
  * Implements lolcode variable declaration and optional initialisation. Example: `I HAZ A VAR1 ITZ 256`
  */
 public class TreeVarDeclStmt extends TreeStatement {
     TreeVariable var;
    TreeExpression initialValue;
 
     public TreeVarDeclStmt() {
         var = null;
         initialValue = null;
     }
 
     public TreeVariable getVar() {
         return var;
     }
 
     public void setVar(TreeVariable var) {
         this.var = var;
     }
 
    public TreeExpression getInitialValue() {
         return initialValue;
     }
 
    public void setInitialValue(TreeExpression initialValue) {
         this.initialValue = initialValue;
     }
 
     @Override
     public void accept(BaseASTVisitor v) {
         v.visit(this);
     }
 }
