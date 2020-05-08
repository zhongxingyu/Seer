 /**
  * (VariableModifiers Type Declarators Expression? ExpressionList?)/
  * (null null ExpressionList? Expression? ExpressionList?)
  */
 package translator;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class BasicForControl extends TranslationVisitor {
   private Modifiers modifiers;
   private boolean isFinal;
   private Expression expression;
  private ExpressionList expressionList;
 
   public BasicForControl(GNode n) {
     modifiers = null;
     isFinal = false;
     expression = null;
    expressionList = null;
     visit(n);
   }
 
   public void visitModifiers(GNode n) {
     modifiers = new Modifiers(n);
   }
 
   public void visitWord(GNode n) {
     isFinal = true;
   }
 
   public void visitExpression(GNode n) {
     expression = new Expression(n);
   }
 
   public void visitExpressionList(GNode n) {
    expressionList = new ExpressionList(n);
   }
 }
