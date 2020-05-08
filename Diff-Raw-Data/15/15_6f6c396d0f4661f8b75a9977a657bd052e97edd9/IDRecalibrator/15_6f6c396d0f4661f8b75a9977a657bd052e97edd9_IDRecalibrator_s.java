 package edu.mit.compilers.graphmodel;
 
 import edu.mit.compilers.IR.*;
 
 public class IDRecalibrator implements IrNodeVisitor {
   private int mOffset;
   
   public void recalibrate(IrNode n, int offset) {
     mOffset = offset;
     n.accept(this);
   }
   
   @Override
   public void visit(IrAssignment n) {
     n.getTarget().accept(this);
     n.getValue().accept(this);
   }
 
   @Override
   public void visit(IrBinOp n) {
     n.getLeft().accept(this);
     n.getRight().accept(this);
   }
 
   @Override
   public void visit(IrDeclaration n) {
     n.getName().accept(this);
   }
 
   @Override
   public void visit(IrExtFunctionCall n) {
     for (IrExpression param : n.getParams()) {
       param.accept(this);
     }
   }
 
   @Override
   public void visit(IrFunctionDef n) {
     for (IrDeclaration dec : n.getParams()) {
       dec.accept(this);
     }
   }
 
   @Override
   public void visit(IrIdentifier n) {
     int old = n.getResultAddress();
     n.setResultAddress(old + mOffset);
   }
 
   @Override
   public void visit(IrLiteral n) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void visit(IrProgram n) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void visit(IrRedacted n) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void visit(IrReturn n) {
     n.getExpr().accept(this);
   }
 
   @Override
   public void visit(IrStringLiteral n) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void visit(IrType n) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void visit(IrWhile n) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void visit(IrIf n) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void visit(IrLabel n) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void visit(IrBranch n) {
     n.getCond().accept(this);
   }
 
 }
