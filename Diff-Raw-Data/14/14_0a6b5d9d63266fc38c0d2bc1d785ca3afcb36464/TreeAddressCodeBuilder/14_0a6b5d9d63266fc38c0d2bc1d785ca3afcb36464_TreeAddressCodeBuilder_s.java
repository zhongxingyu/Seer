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
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import fr.jamgotchian.abcd.core.ast.Method;
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import fr.jamgotchian.abcd.core.controlflow.BasicBlock;
 import fr.jamgotchian.abcd.core.controlflow.BasicBlockType;
 import fr.jamgotchian.abcd.core.controlflow.ControlFlowGraph;
 import fr.jamgotchian.abcd.core.controlflow.DominatorInfo;
 import fr.jamgotchian.abcd.core.controlflow.Edge;
 import fr.jamgotchian.abcd.core.controlflow.LocalVariableTable;
 import fr.jamgotchian.abcd.core.tac.model.ChoiceInst;
 import fr.jamgotchian.abcd.core.tac.model.ConditionalInst;
 import fr.jamgotchian.abcd.core.tac.model.DefInst;
 import fr.jamgotchian.abcd.core.tac.model.JumpIfInst;
 import fr.jamgotchian.abcd.core.tac.model.StringConst;
 import fr.jamgotchian.abcd.core.tac.model.Variable;
 import fr.jamgotchian.abcd.core.tac.model.TACInst;
 import fr.jamgotchian.abcd.core.tac.model.TACInstFactory;
 import fr.jamgotchian.abcd.core.tac.model.TACInstSeq;
 import fr.jamgotchian.abcd.core.tac.model.TemporaryVariableFactory;
 import fr.jamgotchian.abcd.core.tac.util.VariableStack;
 import fr.jamgotchian.abcd.core.tac.util.TACInstWriter;
 import fr.jamgotchian.abcd.core.type.ClassName;
 import fr.jamgotchian.abcd.core.type.ClassNameFactory;
 import fr.jamgotchian.abcd.core.type.JavaType;
 import fr.jamgotchian.abcd.core.util.ConsoleUtil;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class TreeAddressCodeBuilder {
 
     private static final Logger logger = Logger.getLogger(TreeAddressCodeBuilder.class.getName());
 
     private final ControlFlowGraph graph;
 
     private final Method method;
 
     private final ClassNameFactory classNameFactory;
 
     private TemporaryVariableFactory tmpVarFactory;
 
     private TACInstFactory instFactory;
 
     public TreeAddressCodeBuilder(ControlFlowGraph graph, Method method,
                                   ClassNameFactory classNameFactory) {
         this.graph = graph;
         this.method = method;
         this.classNameFactory = classNameFactory;
     }
 
     private void processBlock(BasicBlock block, List<VariableStack> inputStacks) {
 
         logger.log(Level.FINER, "------ Process block {0} ------", block);
 
         AnalysisData data = (AnalysisData) block.getData();
 
         VariableStack inputStack = null;
         if (inputStacks.isEmpty()) {
             inputStack = new VariableStack();
         } else if (inputStacks.size() == 1) {
             inputStack = inputStacks.get(0).clone();
         } else {
             inputStack = mergeStacks(inputStacks, block);
         }
 
         data.setInputStack(inputStack.clone());
 
         VariableStack outputStack = inputStack.clone();
 
         if (block.getType() != BasicBlockType.EMPTY) {
             Edge edge = graph.getFirstIncomingEdgeOf(block);
             if (edge != null && edge.isExceptional()) {
                 Variable tmpVar = tmpVarFactory.create(block);
                 TACInst tmpInst;
                 if (edge.getValue() == null) { // finally
                     tmpInst = instFactory.newAssignConst(tmpVar, new StringConst("TMP", classNameFactory));
                 } else { // catch
                     ClassName className = classNameFactory.newClassName((String) edge.getValue());
                     tmpInst = instFactory.newNewObject(tmpVar, JavaType.newRefType(className));
                 }
                 BasicBlock3ACBuilder.addInst(block, tmpInst);
                 outputStack.push(tmpVar);
             }
         }
 
         if (data.getInputStack().size() > 0) {
             logger.log(Level.FINEST, ">>> Input stack : {0}", data.getInputStack());
         }
 
         BasicBlock3ACBuilder builder
                 = new BasicBlock3ACBuilder(classNameFactory, tmpVarFactory,
                                            outputStack, instFactory);
         block.visit(builder);
         data.setOutputStack(outputStack);
 
         if (data.getOutputStack().size() > 0) {
             logger.log(Level.FINEST, "<<< Output stack : {0}", data.getOutputStack());
         }
     }
 
     private VariableStack mergeStacks(List<VariableStack> stacks, BasicBlock block) {
         if (stacks.size() <= 1) {
             throw new ABCDException("stacks.size() <= 1");
         }
         List<Integer> sizes = new ArrayList<Integer>(stacks.size());
         for (int i = 0; i < stacks.size(); i++) {
             sizes.add(stacks.get(i).size());
         }
         for (int i = 0; i < sizes.size() - 1; i++) {
             if (sizes.get(i) != sizes.get(i + 1)) {
                 throw new ABCDException("Cannot merge stacks with differents sizes : "
                         + sizes);
             }
         }
 
         VariableStack stacksMerge = new VariableStack();
 
         List<List<Variable>> toList = new ArrayList<List<Variable>>(stacks.size());
         for (int i = 0; i < stacks.size(); i++) {
             toList.add(stacks.get(i).toList());
         }
         for (int i = 0; i < stacks.get(0).size(); i++) {
             Set<Variable> vars = new HashSet<Variable>(stacks.size());
             for (int j = 0; j < stacks.size(); j++) {
                 vars.add(toList.get(j).get(i));
             }
             if (vars.size() == 1) {
                 stacksMerge.push(vars.iterator().next());
             } else {
                 Variable result = tmpVarFactory.create(block);
                 BasicBlock3ACBuilder.addInst(block, instFactory.newChoice(result, vars));
                 stacksMerge.push(result);
             }
         }
 
         return stacksMerge;
     }
 
     private void removeChoiceInst() {
         for (BasicBlock joinBlock : graph.getDFST()) {
             TACInstSeq joinInsts = ((AnalysisData) joinBlock.getData()).getInstructions();
 
             for (int i = 0; i < joinInsts.size(); i++) {
                 TACInst inst = joinInsts.get(i);
                 if (!(inst instanceof ChoiceInst)) {
                     continue;
                 }
                 ChoiceInst choiceInst = (ChoiceInst) inst;
 
                 List<TACInst> replacement = new ArrayList<TACInst>();
 
                 boolean change = true;
                 while (change) {
                     change = false;
 
                     Multimap<BasicBlock, Variable> forkBlocks
                             = HashMultimap.create();
                     for (Variable var : choiceInst.getChoices()) {
                         BasicBlock block = var.getBasicBlock();
                         DominatorInfo<BasicBlock, Edge> dominatorInfo
                                 = block.getGraph().getDominatorInfo();
                         BasicBlock forkBlock = dominatorInfo.getDominatorsTree().getParent(block);
                         forkBlocks.put(forkBlock, var);
                     }
 
                     for (Map.Entry<BasicBlock, Collection<Variable>> entry
                             : forkBlocks.asMap().entrySet()) {
                         BasicBlock forkBlock = entry.getKey();
                         Collection<Variable> vars = entry.getValue();
                         if (forkBlock.getType() == BasicBlockType.JUMP_IF
                                 && vars.size() == 2) {
                             Iterator<Variable> it = vars.iterator();
                             Variable var1 = it.next();
                             Variable var2 = it.next();
 
                             BasicBlock block1 = var1.getBasicBlock();
                             BasicBlock block2 = var2.getBasicBlock();
                             DominatorInfo<BasicBlock, Edge> dominatorInfo = forkBlock.getGraph().getDominatorInfo();
                             Edge forkEdge1 = dominatorInfo.getPostDominanceFrontierOf(block1).iterator().next();
                             Edge forkEdge2 = dominatorInfo.getPostDominanceFrontierOf(block2).iterator().next();
                             Variable thenVar = null;
                             Variable elseVar = null;
                             if (Boolean.TRUE.equals(forkEdge1.getValue())
                                     && Boolean.FALSE.equals(forkEdge2.getValue())) {
                                 thenVar = var1;
                                 elseVar = var2;
                             } else if (Boolean.FALSE.equals(forkEdge1.getValue())
                                     && Boolean.TRUE.equals(forkEdge2.getValue())) {
                                 thenVar = var2;
                                 elseVar = var1;
                             }
                             if (thenVar != null && elseVar != null) {
                                 AnalysisData forkData = (AnalysisData) forkBlock.getData();
                                 JumpIfInst jumpIfInst = (JumpIfInst) forkData.getInstructions().getLast();
                                 choiceInst.getChoices().remove(thenVar);
                                 choiceInst.getChoices().remove(elseVar);
                                 if (choiceInst.getChoices().isEmpty()) {
                                     Variable resultVar = choiceInst.getResult();
                                     ConditionalInst condInst
                                            = instFactory.newConditional(resultVar, jumpIfInst.getCond(), thenVar, elseVar);
                                     logger.log(Level.FINER, "Replace inst at {0} of {1} : {2}",
                                             new Object[]{i, joinBlock, TACInstWriter.toText(condInst)});
                                     replacement.add(condInst);
                                 } else {
                                     Variable resultVar = tmpVarFactory.create(forkBlock);
                                     ConditionalInst condInst
                                            = instFactory.newConditional(resultVar, jumpIfInst.getCond(), thenVar, elseVar);
                                     logger.log(Level.FINER, "Insert inst at {0} of {1} : {2}",
                                             new Object[]{i, joinBlock, TACInstWriter.toText(condInst)});
                                     replacement.add(condInst);
                                     choiceInst.getChoices().add(resultVar);
                                 }
 
                                 change = true;
                             } else {
                                 throw new ABCDException("Conditional instruction building error");
                             }
                         } else if (forkBlock.getType() == BasicBlockType.SWITCH
                                 && vars.size() > 2) {
                             throw new ABCDException("TODO");
                         }
                     }
                 }
 
                 if (replacement.size() > 0) {
                     joinInsts.remove(i);
                     joinInsts.addAll(i, replacement);
                 }
             }
         }
     }
 
     private void printVariableName(Set<Variable> variables) {
         List<String> indexColumn = new ArrayList<String>(1);
         List<String> positionColumn = new ArrayList<String>(1);
         List<String> nameColumn = new ArrayList<String>(1);
         indexColumn.add("Index");
         positionColumn.add("Position");
         nameColumn.add("Name");
         for (Variable v : variables) {
             indexColumn.add(v.getID().toString());
             positionColumn.add(Integer.toString(v.getPosition()));
             nameColumn.add(v.getName() != null ? v.getName() : "<undefined>");
         }
         logger.log(Level.FINEST, "Variable names :\n{0}",
                 ConsoleUtil.printTable(indexColumn, positionColumn, nameColumn));
     }
 
     public void build() {
         for (BasicBlock block : graph.getBasicBlocks()) {
             block.setData(new AnalysisData());
         }
 
         tmpVarFactory = new TemporaryVariableFactory();
         instFactory = new TACInstFactory();
 
         List<BasicBlock> blocksToProcess = new ArrayList<BasicBlock>(graph.getDFST().getNodes());
         while (blocksToProcess.size() > 0) {
             for (Iterator<BasicBlock> it = blocksToProcess.iterator(); it.hasNext();) {
                 BasicBlock block = it.next();
 
                 List<VariableStack> inputStacks = new ArrayList<VariableStack>();
                 for (Edge incomingEdge : graph.getIncomingEdgesOf(block)) {
                     if (incomingEdge.isLoopBack()) {
                         continue;
                     }
                     BasicBlock pred = graph.getEdgeSource(incomingEdge);
                     AnalysisData data = (AnalysisData) pred.getData();
                     inputStacks.add(data.getOutputStack().clone());
                 }
 
                 processBlock(block, inputStacks);
                 it.remove();
             }
         }
 
         // replace choice instructions by conditional instructions (ternary operator)
         removeChoiceInst();
 
         // convert to SSA form
         new SSAFormConverter(graph, instFactory).convert();
 
         // analyse local variables types
         new LocalVariableTypeAnalyser(graph, method, classNameFactory).analyse();
 
         // assign a name to each variable
         LocalVariableTable table = graph.getLocalVariableTable();
         Set<Variable> variables = new HashSet<Variable>();
         for (BasicBlock block : graph.getBasicBlocks()) {
             for (TACInst inst : ((AnalysisData) block.getData()).getInstructions()) {
                 if (inst instanceof DefInst) {
                     Variable def = ((DefInst) inst).getResult();
                     if (!def.isTemporary()) {
                         variables.add(def);
                         def.setName(table.getName(def.getIndex(), def.getPosition()));
                     }
                 }
                 for (Variable use : inst.getUses()) {
                     if (!use.isTemporary()) {
                         variables.add(use);
                         use.setName(table.getName(use.getIndex(), use.getPosition()));
                     }
                 }
             }
         }
 
         printVariableName(variables);
     }
 }
