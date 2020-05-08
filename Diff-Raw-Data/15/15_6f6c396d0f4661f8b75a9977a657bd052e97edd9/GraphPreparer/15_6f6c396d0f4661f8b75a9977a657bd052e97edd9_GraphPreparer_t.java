 package edu.mit.compilers.graphmodel;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import edu.mit.compilers.IR.*;
 import edu.mit.compilers.IR.IrLabel.LabelType;
 
 public class GraphPreparer implements IrNodeVisitor {
   private List<IrNode> mInstructions;
   private Map<String, Integer> mNameAssignments;
   private int mNextID;
   private int mNextLabel;
   
   public List<IrNode> prepare(IrFunctionDef p) {
     mInstructions = new ArrayList<IrNode>();
     mNameAssignments = new HashMap<String, Integer>();
     mNextID = 0;
     mNextLabel = 0;
     
     visit(p);
     
     p.setNumVars(mNextID);
     return mInstructions;
   }
   
   private int getNextID() {
     return mNextID++;
   }
   
   private int getNextLabel() {
     return mNextLabel++;
   }
 
   @Override
   public void visit(IrAssignment n) {
     IrExpression rhs = n.getValue();
     IrIdentifier lhs = n.getTarget();
     
     lhs.accept(this);
     rhs.accept(this);
     
     if (rhs instanceof IrBinOp || rhs instanceof IrExtFunctionCall) {
       IrIdentifier newValue = new IrIdentifier();
       newValue.setResultAddress(rhs.getResultAddress());
       IrAssignment newAssign = new IrAssignment(n.getTarget(), newValue);
       mInstructions.add(newAssign);
     } else {
       mInstructions.add(n);
     }
   }
 
   @Override
   public void visit(IrBinOp n) {
     IrExpression oldLeft = n.getLeft();
     IrExpression oldRight = n.getRight();
     IrExpression newLeft, newRight;
     
     int newAddr = getNextID();
     IrIdentifier newTarget = new IrIdentifier();
     newTarget.setResultAddress(newAddr);
     n.setResultAddress(newAddr);
     IrBinOp newOp;
     IrAssignment tempAssign;
     
     oldLeft.accept(this);
     oldRight.accept(this);
     
     if (oldLeft instanceof IrBinOp || oldLeft instanceof IrExtFunctionCall) {
       newLeft = new IrIdentifier();
       newLeft.setResultAddress(oldLeft.getResultAddress());
     } else {
       newLeft = oldLeft;
     }
     
     if (oldRight instanceof IrBinOp || oldRight instanceof IrExtFunctionCall) {
       newRight = new IrIdentifier();
       newRight.setResultAddress(oldRight.getResultAddress());
     } else {
       newRight = oldRight;
     }
     
     newOp = new IrBinOp(newLeft, n.getOp(), newRight);
     tempAssign = new IrAssignment(newTarget, newOp);
     mInstructions.add(tempAssign);
   }
 
   @Override
   public void visit(IrDeclaration n) {
     n.getName().accept(this);
   }
 
   @Override
   public void visit(IrExtFunctionCall n) {
     IrIdentifier name = n.getName();
     IrExtFunctionCall newCall = new  IrExtFunctionCall(name);
     IrIdentifier newTarget;
     
     for (IrExpression param : n.getParams()) {
       param.accept(this);
       if (param instanceof IrBinOp || param instanceof IrExtFunctionCall) {
         newTarget = new IrIdentifier();
         newTarget.setResultAddress(param.getResultAddress());
         newCall.addParam(newTarget);
       } else {
         newCall.addParam(param);
       }
     }
     
     int targetId = getNextID();
     IrIdentifier target = new IrIdentifier();
     target.setResultAddress(targetId);
     n.setResultAddress(targetId);
     IrAssignment assign = new IrAssignment(target, newCall);
     mInstructions.add(assign);
   }
 
   @Override
   public void visit(IrFunctionDef n) {
     for (IrDeclaration param : n.getParams()) {
       param.accept(this);
     }
     
     for (IrNode node : n.getChildren()) {
       node.accept(this);
     }
   }
 
   @Override
   public void visit(IrIdentifier n) {
     String name = n.getName();
     if (!mNameAssignments.containsKey(name)) {
       int id = getNextID();
       String newName = n.getName();
       mNameAssignments.put(newName, id);
       n.setResultAddress(id);
     } else {
       int id = mNameAssignments.get(name);
       n.setResultAddress(id);
     }
   }
 
   @Override
   public void visit(IrLiteral n) {
     // Address already assigned
   }
 
   @Override
   public void visit(IrProgram n) {
     // Only deal with functions at this level
   }
 
   @Override
   public void visit(IrRedacted n) {
     // Ignore
   }
 
   @Override
   public void visit(IrReturn n) {
     if (n.hasReturn()) {
       IrExpression value = n.getExpr();
       value.accept(this);
       if (value instanceof IrBinOp || value instanceof IrExtFunctionCall) {
         IrIdentifier newValue = new IrIdentifier();
         newValue.setResultAddress(value.getResultAddress());
         IrReturn newReturn = new IrReturn(newValue);
         mInstructions.add(newReturn);
       } else {
         mInstructions.add(n);
       }
     } else {
       mInstructions.add(n);
     }
   }
 
   @Override
   public void visit(IrStringLiteral n) {
     // Should not be visited
   }
 
   @Override
   public void visit(IrType n) {
     // Should not be visited
   }
 
   @Override
   public void visit(IrWhile n) {
     IrExpression cond = n.getCond();
     int beginNum = getNextLabel();
     IrLabel wBegin = new IrLabel(beginNum, LabelType.WBEGIN);
     mInstructions.add(wBegin);
     cond.accept(this);
     
     int nextNum = getNextLabel();
     int endNum = getNextLabel();
     
     IrLabel wNext = new IrLabel(nextNum, LabelType.WBEGIN);
     IrBranch wCheck = new IrBranch(cond);
     wCheck.setTrueBranch(nextNum);
     wCheck.setFalseBranch(endNum);
     
     mInstructions.add(wCheck);
     mInstructions.add(wNext);
     
     for (IrNode subNode : n.getChildren()) {
       subNode.accept(this);
     }
     
     setLastInstructionTarget(beginNum);
     IrLabel wEnd = new IrLabel(endNum, LabelType.WEND);
     mInstructions.add(wEnd);
   }
   
   private void setLastInstructionTarget(int t) {
     IrNode last = mInstructions.get(mInstructions.size()-1);
     if (last instanceof IrBranch) {
       IrBranch b = (IrBranch)last;
       b.setFalseBranch(t);
     } else {
       last.setNextInstr(t);
     }
   }
 
   @Override
   public void visit(IrIf n) {
     IrExpression cond = n.getCond();
     cond.accept(this);
 
     int trueNum = getNextLabel();
     int nextNum = getNextLabel();
     int endNum = getNextLabel();
     IrLabel trueLabel = new IrLabel(trueNum, LabelType.IF_TRUE);
     IrLabel nextLabel = new IrLabel(nextNum, LabelType.IF_TRUE);
     IrLabel endLabel = new IrLabel(endNum, LabelType.IF_END);
     IrBranch jmpTrue = new IrBranch(cond);
     
     if (n.hasElse() && !n.getUnsatisfied().isEmpty()) {
       jmpTrue.setTrueBranch(trueNum);
       jmpTrue.setFalseBranch(nextNum);
       mInstructions.add(jmpTrue);
       mInstructions.add(nextLabel);
       for (IrNode elseNode : n.getUnsatisfied()) {
         elseNode.accept(this);
       }
       setLastInstructionTarget(endNum);
       mInstructions.add(trueLabel);
       for (IrNode trueNode : n.getSatisfied()) {
         trueNode.accept(this);
       }
       mInstructions.add(endLabel);
     } else {
       jmpTrue.setFalseBranch(endNum);
       jmpTrue.setTrueBranch(trueNum);
       
       mInstructions.add(jmpTrue);
       mInstructions.add(trueLabel);
       for (IrNode trueNode : n.getSatisfied()) {
         trueNode.accept(this);
       }
       mInstructions.add(endLabel);
     }
     
   }
 
   @Override
   public void visit(IrLabel n) {
     // Eventually deal with inline labels (goes with goto statements)
   }
 
   @Override
   public void visit(IrBranch n) {
     // Unnecessary
   }
 
 }
