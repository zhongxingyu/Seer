 /*
  * Copyright 2009-2010 by The Regents of the University of California
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * you may obtain a copy of the License from
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.uci.ics.algebricks.compiler.optimizer.rules;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.uci.ics.algebricks.api.exceptions.AlgebricksException;
 import edu.uci.ics.algebricks.compiler.algebra.base.ILogicalExpression;
 import edu.uci.ics.algebricks.compiler.algebra.base.ILogicalOperator;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalExpressionReference;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalExpressionTag;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalOperatorReference;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalOperatorTag;
 import edu.uci.ics.algebricks.compiler.algebra.base.LogicalVariable;
 import edu.uci.ics.algebricks.compiler.algebra.expressions.AbstractFunctionCallExpression;
 import edu.uci.ics.algebricks.compiler.algebra.expressions.ConstantExpression;
 import edu.uci.ics.algebricks.compiler.algebra.expressions.ScalarFunctionCallExpression;
 import edu.uci.ics.algebricks.compiler.algebra.functions.AlgebricksBuiltinFunctions;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.AbstractBinaryJoin;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.AbstractLogicalOperator;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.SelectOperator;
 import edu.uci.ics.algebricks.compiler.algebra.operators.logical.visitors.VariableUtilities;
 import edu.uci.ics.algebricks.compiler.optimizer.base.IAlgebraicRewriteRule;
 import edu.uci.ics.algebricks.compiler.optimizer.base.IOptimizationContext;
 import edu.uci.ics.algebricks.compiler.optimizer.base.OptimizationUtil;
 
 public class PushSelectIntoJoinRule implements IAlgebraicRewriteRule {
 
     private Collection<LogicalVariable> joinLiveVarsLeft = new HashSet<LogicalVariable>();
     private Collection<LogicalVariable> joinLiveVarsRight = new HashSet<LogicalVariable>();
     private Collection<LogicalVariable> liveInOpsToPushLeft = new HashSet<LogicalVariable>();
     private Collection<LogicalVariable> liveInOpsToPushRight = new HashSet<LogicalVariable>();
 
     private List<ILogicalOperator> pushedOnLeft = new ArrayList<ILogicalOperator>();
     private List<ILogicalOperator> pushedOnRight = new ArrayList<ILogicalOperator>();
     private LinkedList<ILogicalOperator> notPushedStack = new LinkedList<ILogicalOperator>();
     private Collection<LogicalVariable> usedVars = new HashSet<LogicalVariable>();
     private Collection<LogicalVariable> producedVars = new HashSet<LogicalVariable>();
 
     @Override
     public boolean rewritePre(LogicalOperatorReference opRef, IOptimizationContext context) {
         return false;
     }
 
     @Override
     public boolean rewritePost(LogicalOperatorReference opRef, IOptimizationContext context) throws AlgebricksException {
         AbstractLogicalOperator op = (AbstractLogicalOperator) opRef.getOperator();
         if (op.getOperatorTag() != LogicalOperatorTag.SELECT) {
             return false;
         }
         SelectOperator select = (SelectOperator) op;
         LogicalOperatorReference opRef2 = op.getInputs().get(0);
         AbstractLogicalOperator son = (AbstractLogicalOperator) opRef2.getOperator();
         AbstractLogicalOperator op2 = son;
         boolean needToPushOps = false;
         while (son.isMap()) {
             needToPushOps = true;
             LogicalOperatorReference opRefLink = son.getInputs().get(0);
             son = (AbstractLogicalOperator) opRefLink.getOperator();
         }
 
         if (son.getOperatorTag() != LogicalOperatorTag.INNERJOIN
                 && son.getOperatorTag() != LogicalOperatorTag.LEFTOUTERJOIN) {
             return false;
         }
         boolean isLoj = son.getOperatorTag() == LogicalOperatorTag.LEFTOUTERJOIN;
         AbstractBinaryJoin join = (AbstractBinaryJoin) son;
 
         LogicalOperatorReference joinBranchLeftRef = join.getInputs().get(0);
         LogicalOperatorReference joinBranchRightRef = join.getInputs().get(1);
 
         if (needToPushOps) {
             ILogicalOperator joinBranchLeft = joinBranchLeftRef.getOperator();
             ILogicalOperator joinBranchRight = joinBranchRightRef.getOperator();
             joinLiveVarsLeft.clear();
             joinLiveVarsRight.clear();
             VariableUtilities.getLiveVariables(joinBranchLeft, joinLiveVarsLeft);
             VariableUtilities.getLiveVariables(joinBranchRight, joinLiveVarsRight);
             notPushedStack.clear();
             pushedOnLeft.clear();
             pushedOnRight.clear();
             liveInOpsToPushLeft.clear();
             liveInOpsToPushRight.clear();
             LogicalOperatorReference opIterRef = opRef2;
             ILogicalOperator opIter = op2;
             while (opIter != join) {
                 LogicalOperatorTag tag = ((AbstractLogicalOperator) opIter).getOperatorTag();
                 if (tag == LogicalOperatorTag.PROJECT) {
                     notPushedStack.addFirst(opIter);
                 } else {
                     usedVars.clear();
                     VariableUtilities.getUsedVariables(opIter, usedVars);
                     VariableUtilities.getProducedVariables(opIter, producedVars);
                     if (joinLiveVarsLeft.containsAll(usedVars)) {
                         pushedOnLeft.add(opIter);
                         liveInOpsToPushLeft.addAll(producedVars);
                     } else if (joinLiveVarsRight.containsAll(usedVars)) {
                         pushedOnRight.add(opIter);
                         liveInOpsToPushRight.addAll(producedVars);
                     } else {
                         return false;
                     }
                 }
                 opIterRef = opIter.getInputs().get(0);
                 opIter = opIterRef.getOperator();
             }
             if (isLoj && pushedOnLeft.isEmpty()) {
                 return false;
             }
         }
 
         boolean intersectsAllBranches = true;
         boolean[] intersectsBranch = new boolean[join.getInputs().size()];
         LinkedList<LogicalVariable> selectVars = new LinkedList<LogicalVariable>();
         select.getCondition().getExpression().getUsedVariables(selectVars);
         int i = 0;
         for (LogicalOperatorReference branch : join.getInputs()) {
             LinkedList<LogicalVariable> branchVars = new LinkedList<LogicalVariable>();
             VariableUtilities.getLiveVariables(branch.getOperator(), branchVars);
             if (i == 0) {
                 branchVars.addAll(liveInOpsToPushLeft);
             } else {
                 branchVars.addAll(liveInOpsToPushRight);
             }
             if (OptimizationUtil.disjoint(selectVars, branchVars)) {
                 intersectsAllBranches = false;
             } else {
                 intersectsBranch[i] = true;
             }
             i++;
         }

        boolean intersectsNone = !intersectsBranch[0] && !intersectsBranch[1];
         if (intersectsAllBranches) {
             if (needToPushOps) {
                 pushOps(pushedOnLeft, joinBranchLeftRef);
                 pushOps(pushedOnRight, joinBranchRightRef);
             }
             addCondToJoin(select, join);
         } else { // push down
             Iterator<LogicalOperatorReference> branchIter = join.getInputs().iterator();
 
             for (int j = 0; j < intersectsBranch.length; j++) {
                 LogicalOperatorReference branch = branchIter.next();
                boolean inter = intersectsBranch[j] || intersectsNone;
                 if (inter) {
 
                     if (needToPushOps) {
                         if (j == 0) {
                             pushOps(pushedOnLeft, joinBranchLeftRef);
                         } else {
                             pushOps(pushedOnRight, joinBranchRightRef);
                         }
                     }
 
                     copySelectToBranch(select, branch);
                 }
 
                 // if a left outer join, we can only push conditions into the
                 // outer branch.
                 if (j == 0 && isLoj) {
                     // stop at this branch
                    return inter;
                 }
             }
         }
         ILogicalOperator top = join;
         for (ILogicalOperator npOp : notPushedStack) {
             List<LogicalOperatorReference> npInpList = npOp.getInputs();
             npInpList.clear();
             npInpList.add(new LogicalOperatorReference(top));
             top = npOp;
         }
         opRef.setOperator(top);
         return true;
 
     }
 
     private void pushOps(List<ILogicalOperator> opList, LogicalOperatorReference joinBranch) {
         ILogicalOperator topOp = joinBranch.getOperator();
         LogicalOperatorReference opRef = joinBranch;
         for (ILogicalOperator op : opList) {
             opRef.setOperator(op);
             opRef = new LogicalOperatorReference();
             List<LogicalOperatorReference> opInpList = op.getInputs();
             opInpList.clear();
             opInpList.add(opRef);
         }
         opRef.setOperator(topOp);
     }
 
     private static void addCondToJoin(SelectOperator select, AbstractBinaryJoin join) {
         ILogicalExpression cond = join.getCondition().getExpression();
         if (cond == ConstantExpression.TRUE) { // the join was a product
             join.getCondition().setExpression(select.getCondition().getExpression());
         } else {
             boolean bAddedToConj = false;
             if (cond.getExpressionTag() == LogicalExpressionTag.FUNCTION_CALL) {
                 AbstractFunctionCallExpression fcond = (AbstractFunctionCallExpression) cond;
                 if (fcond.getFunctionIdentifier().equals(AlgebricksBuiltinFunctions.AND)) {
                     AbstractFunctionCallExpression newCond = new ScalarFunctionCallExpression(
                             AlgebricksBuiltinFunctions.getBuiltinFunctionInfo(AlgebricksBuiltinFunctions.AND));
                     newCond.getArguments().add(select.getCondition());
                     newCond.getArguments().addAll(fcond.getArguments());
                     join.getCondition().setExpression(newCond);
                     bAddedToConj = true;
                 }
             }
             if (!bAddedToConj) {
                 AbstractFunctionCallExpression newCond = new ScalarFunctionCallExpression(
                         AlgebricksBuiltinFunctions.getBuiltinFunctionInfo(AlgebricksBuiltinFunctions.AND),
                         select.getCondition(), new LogicalExpressionReference(join.getCondition().getExpression()));
                 join.getCondition().setExpression(newCond);
             }
         }
     }
 
     private static void copySelectToBranch(SelectOperator select, LogicalOperatorReference branch) {
         ILogicalOperator newSelect = new SelectOperator(select.getCondition());
         LogicalOperatorReference newRef = new LogicalOperatorReference(branch.getOperator());
         newSelect.getInputs().add(newRef);
         branch.setOperator(newSelect);
     }
 }
