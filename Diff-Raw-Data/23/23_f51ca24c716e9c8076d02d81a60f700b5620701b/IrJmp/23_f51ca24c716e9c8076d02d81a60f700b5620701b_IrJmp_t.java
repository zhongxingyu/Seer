 package edu.mit.compilers.IR;
 
 public class IrJmp extends IrNode {
   IrExpression mCondition;
   int mJumpTarget;
   
   public IrJmp(IrExpression cond, int target) {
     mCondition = cond;
     mJumpTarget = target;
   }
   
   public IrJmp(int target) {
     mJumpTarget = target;
     mCondition = new IrLiteral(1);
   }
   
   public IrExpression getCond() {
     return mCondition;
   }
   
   public int getTarget() {
     return mJumpTarget;
   }
   
   public void setTarget(int newTarget) {
     mJumpTarget = newTarget;
   }
 
   @Override
   public void accept(IrNodeVisitor v) {
     v.visit(this);
   }
   
   @Override
   public String toString() {
    if (mCondition instanceof IrLiteral) {
      return "jmp " + mCondition.toString() + ", " + mJumpTarget;
    } else {
      return "jmp (" + mCondition.getResultAddress() + "), " + mJumpTarget;
    }
   }
 
 }
