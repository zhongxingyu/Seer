 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.jamgotchian.abcd.core.analysis;
 
 import fr.jamgotchian.abcd.core.ast.util.ExpressionStack;
 import fr.jamgotchian.abcd.core.ast.expr.Constant;
 import fr.jamgotchian.abcd.core.ast.expr.Expression;
 import fr.jamgotchian.abcd.core.ast.expr.Expressions;
 import fr.jamgotchian.abcd.core.ast.stmt.Statement;
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import fr.jamgotchian.abcd.core.controlflow.BasicBlock;
 import fr.jamgotchian.abcd.core.controlflow.ControlFlowGraph;
 import fr.jamgotchian.abcd.core.controlflow.Edge;
import fr.jamgotchian.abcd.core.output.OutputUtil;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class ControlFlowGraphStmtAnalysis {
 
     private static final Logger logger = Logger.getLogger(ControlFlowGraphStmtAnalysis.class.getName());
 
     static {
        logger.setLevel(Level.FINEST);
     }
 
     private static class DummyExpressionStack implements ExpressionStack {
 
         private static final Expression DUMMY_EXPR = Expressions.newCstExpr(null, null);
 
         private int stackSize = 0;
 
         private int consumption = 0;
 
         public int getProduction() {
             return Math.max(0, stackSize);
         }
 
         public int getConsumption() {
             return consumption;
         }
 
         public void push(Expression expr) {
             stackSize++;
         }
 
         public Expression pop() {
             stackSize--;
             consumption = -Math.min(stackSize, 0);
             return DUMMY_EXPR;
         }
 
         public Expression peek() {
             return DUMMY_EXPR;
         }
 
         public int size() {
             return stackSize;
         }
 
         public List<Expression> toList() {
             throw new ABCDException("Not implemented");
         }
 
         public Iterable<Expression> toIterable() {
             throw new ABCDException("Not implemented");
         }
 
         @Override
         public ExpressionStack clone() {
             throw new ABCDException("Not implemented");
         }
     }
 
     private static class StackEffectAnalyser extends BasicBlockStmtAnalysis {
 
         private static final Logger logger = Logger.getLogger(DummyExpressionStack.class.getName());
 
         static {
             logger.setLevel(Level.FINE);
         }
 
         public StackEffectAnalyser() {
             super(new DummyExpressionStack());
         }
 
         @Override
         public void before(BasicBlock block) {
             super.before(block);
             Iterator<Edge> itE = block.getGraph().getIncomingEdgesOf(block).iterator();
             if (itE.hasNext() && itE.next().isExceptional()) {
                 stack.push(Expressions.newCstExpr("EXCEPTION", block));
             }
         }
 
         @Override
         public void after(BasicBlock block) {
             super.after(block);
             DummyExpressionStack dummyStack = (DummyExpressionStack) stack;
             logger.log(Level.FINEST, "Stack effect of {0}: +{1} / -{2}",
                     new Object[] {block, dummyStack.getProduction() , dummyStack.getConsumption()});
             BasicBlockAnalysisDataImpl data = (BasicBlockAnalysisDataImpl) block.getData();
             data.setStackConsumption((dummyStack).getConsumption());
             data.setStackProduction((dummyStack).getProduction());
         }
 
         @Override
         protected void addStmt(BasicBlock block, Statement stmt) {
             // DO NOTHING
         }
     }
 
     private ControlFlowGraph graph;
 
     private void processBlock(BasicBlock block, ExpressionStack inputStack) {
 
         logger.log(Level.FINER, "------ Process block {0} ------", block);
 
         BasicBlockAnalysisDataImpl data = (BasicBlockAnalysisDataImpl) block.getData();
 
         data.setInputStack(inputStack.clone());
 
         ExpressionStack outputStack = inputStack.clone();
         Iterator<Edge> itE = graph.getIncomingEdgesOf(block).iterator();
         if (itE.hasNext() && itE.next().isExceptional()) {
             Constant cst = Expressions.newCstExpr("EXCEPTION", block);
             outputStack.push(cst);
         }
 
         if (data.getInputStack().size() > 0) {
            logger.log(Level.FINEST, ">>> Input stack : {0}", 
                    OutputUtil.toText2(data.getInputStack().toIterable()));
         }
 
         BasicBlockStmtAnalysis stmtsBuilder = new BasicBlockStmtAnalysis(outputStack);
         block.visit(stmtsBuilder);
         data.setOutputStack(outputStack);
 
         if (data.getOutputStack().size() > 0) {
            logger.log(Level.FINEST, "<<< Output stack : {0}", 
                    OutputUtil.toText2(data.getOutputStack().toIterable()));
         }
     }
 
     public void analyse(ControlFlowGraph graph) {
 
         this.graph = graph;
 
         for (BasicBlock block : graph.getBasicBlocks()) {
             block.setData(new BasicBlockAnalysisDataImpl());
             block.visit(new StackEffectAnalyser());
         }
 
         List<BasicBlock> blocksToProcess = new ArrayList<BasicBlock>(graph.getDFST().getNodes());
         while (blocksToProcess.size() > 0) {
             for (Iterator<BasicBlock> it = blocksToProcess.iterator(); it.hasNext();) {
                 BasicBlock block = it.next();
 
                 boolean processable = true;
                 for (Edge incomingEdge : graph.getIncomingEdgesOf(block)) {
                     if (incomingEdge.isLoopBack()) {
                         continue;
                     }
                     BasicBlock pred = graph.getEdgeSource(incomingEdge);
                     if (blocksToProcess.contains(pred)) {
                         processable = false;
                         break;
                     }
                 }
 
                 if (!processable) {
                     break;
                 }
                 
                 List<ExpressionStack> stacks = new ArrayList<ExpressionStack>();
                 for (Edge incomingEdge : graph.getIncomingEdgesOf(block)) {
                     if (incomingEdge.isLoopBack()) {
                         continue;
                     }
                     BasicBlock pred = graph.getEdgeSource(incomingEdge);
                     BasicBlockAnalysisDataImpl data = (BasicBlockAnalysisDataImpl) pred.getData();
                     stacks.add(data.getOutputStack().clone());
                 }
 
                 ExpressionStack inputStack = null;
                 if (stacks.isEmpty()) {
                     inputStack = new ExpressionStackImpl();
                 } else if (stacks.size() == 1) {
                     inputStack = stacks.get(0).clone();
                 } else {
                     inputStack = ExpressionStacks.merge(stacks, block);
                 }
                     
                 processBlock(block, inputStack);
                 it.remove();
             }
         }
     }
 }
