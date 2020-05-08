 /**
  * (VariableModifiers Type Declarators Expression? ExpressionList?)/
  * (null null ExpressionList? Expression? ExpressionList?)
  */
 package translator;
 
import java.util.List;
import java.util.ArrayList;

 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class BasicForControl extends TranslationVisitor {
   private Modifiers modifiers;
   private boolean isFinal;
  private Type type;
   private Expression expression;
  private List<ExpressionList> expressionLists;
 
   public BasicForControl(GNode n) {
     modifiers = null;
     isFinal = false;
     expression = null;
    expressionLists = new ArrayList<ExpressionList>();
     visit(n);
   }
 
   public void visitModifiers(GNode n) {
     modifiers = new Modifiers(n);
   }
 
   public void visitWord(GNode n) {
     isFinal = true;
   }
 
  public void visitType(GNode n) {
    type = new Type(n);
  }

   public void visitExpression(GNode n) {
     expression = new Expression(n);
   }
 
   public void visitExpressionList(GNode n) {
    expressionLists.add(new ExpressionList(n));
   }
 }
