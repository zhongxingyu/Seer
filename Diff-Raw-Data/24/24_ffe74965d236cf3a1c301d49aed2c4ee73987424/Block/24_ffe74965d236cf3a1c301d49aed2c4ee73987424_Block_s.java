 /**
  * FieldDeclaration/ClassDeclaration/InterfaceDeclaration/Statement
  */
 package translator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class Block extends Statement implements Translatable {
   
   private List<Translatable> parts;
   
   public Block(GNode n) {
     parts = new ArrayList<Translatable>();
     visit(n);
   }
   
   public void visitAssertStatement(GNode n) {
     parts.add(new AssertStatement(n));
   }
   
   public void visitBlock(GNode n) {
     parts.add(new Block(n));
   }
   
   public void visitBreakStatement(GNode n) {
     parts.add(new BreakStatement(n));
   }
   
   public void visitConditionalStatement(GNode n) {
     parts.add(new ConditionalStatement(n));
   }
   
   public void visitContinueStatement(GNode n) {
     parts.add(new ContinueStatement(n));
   }
   
   public void visitDoWhileStatement(GNode n) {
     parts.add(new DoWhileStatement(n));
   }
   
   public void visitExpressionStatement(GNode n) {
     parts.add(new ExpressionStatement(n));
   }
   
   public void visitFieldDeclaration(GNode n) {
     parts.add(new FieldDeclaration(n));
   }
   
   public void visitForStatement(GNode n) {
     parts.add(new ForStatement(n));
   }
   
   public void visitInterfaceDeclaration(GNode n) {
     parts.add(new InterfaceDeclaration(n));
   }
   
   public void visitReturnStatement(GNode n) {
     parts.add(new ReturnStatement(n));
   }
   
   public void visitSwitchStatement(GNode n) {
     parts.add(new SwitchStatement(n));
   }
   
   public void visitSynchronizedStatement(GNode n) {
     parts.add(new SynchronizedStatement(n));
   }
   
   public void visitThrowStatement(GNode n) {
     parts.add(new ThrowStatement(n));
   }
   
   public void visitTryCatchFinallyStatement(GNode n) {
     parts.add(new TryCatchFinallyStatement(n));
   }
   
   public void visitWhileStatement(GNode n) {
     parts.add(new WhileStatement(n));
   }
 
   public String getCC(int indent, String className, List<Variable> variables) {
     StringBuilder s = new StringBuilder();
     for (Translatable t : parts) {
       s.append(t.getCC(indent, className, variables));
     }
     return s.toString();
   }
   
 }
