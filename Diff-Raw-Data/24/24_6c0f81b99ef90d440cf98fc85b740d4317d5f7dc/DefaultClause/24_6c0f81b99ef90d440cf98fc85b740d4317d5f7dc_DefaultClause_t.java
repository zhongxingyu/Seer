 /**
  * (VariableDeclaration/ClassDeclaration/InterfaceDeclaration/Statement)*
  */
 package translator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class DefaultClause extends TranslationVisitor {
   
   private List<FieldDeclaration> fields;
   private List<InterfaceDeclaration> interfaceDec;
   private List<Statement> statement;
   
   public DefaultClause(GNode n) {
     fields = new ArrayList<FieldDeclaration>();
     interfaceDec = new ArrayList<InterfaceDeclaration>();
     statement = new ArrayList<Statement>();
     visit(n);
   }
  
   public void visitAssertStatement(GNode n) {
     statement.add(new AssertStatement(n));
  }
 
   public void visitBreakStatement(GNode n) {
     statement.add(new BreakStatement(n));
   }
   
   public void visitConditionalStatement(GNode n) {
     statement.add(new ConditionalStatement(n));
   }
   
   public void visitContinueStatement(GNode n) {
     statement.add(new ContinueStatement(n));
   }
   
   public void visitDoWhileStatement(GNode n) {
     statement.add(new DoWhileStatement(n));
   }
   
   public void visitExpressionStatement(GNode n) {
     statement.add(new ExpressionStatement(n));
   }
   
   public void visitVariableDeclaration(GNode n) {
     fields.add(new FieldDeclaration(n));
   }
   
   public void visitForStatement(GNode n) {
     statement.add(new ForStatement(n));
   }
   
   public void visitInterfaceDeclaration(GNode n) {
     interfaceDec.add(new InterfaceDeclaration(n));
   }
   
   public void visitReturnStatement(GNode n) {
     statement.add(new ReturnStatement(n));
   }
   
   public void visitSwitchStatement(GNode n) {
     statement.add(new SwitchStatement(n));
   }
 
   public void visitSynchronizedStatement(GNode n) {
     statement.add(new SynchronizedStatement(n));
   }
   
   public void visitThrowStatement(GNode n) {
     statement.add(new ThrowStatement(n));
   }
   
   public void visitTryCatchFinallyStatement(GNode n) {
     statement.add(new TryCatchFinallyStatement(n));
   }
   
   public void visitWhileStatement(GNode n) {
     statement.add(new WhileStatement(n));
   }
   
 }
