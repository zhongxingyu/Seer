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
 package fr.jamgotchian.abcd.core.ir;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import fr.jamgotchian.abcd.core.common.ABCDWriter;
 import fr.jamgotchian.abcd.core.graph.DominatorInfo;
 import fr.jamgotchian.abcd.core.graph.PostDominatorInfo;
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
 public class IntermediateRepresentationBuilder {
 
     private static final Logger logger
             = Logger.getLogger(IntermediateRepresentationBuilder.class.getName());
 
     private final StringConst magicString;
 
     private final ControlFlowGraphBuilder cfgBuilder;
 
     private final InstructionBuilder instBuilder;
 
     private final ClassNameFactory classNameFactory;
 
     private final TemporaryVariableFactory tmpVarFactory;
 
     private final IRInstFactory instFactory;
 
     private ControlFlowGraph cfg;
 
     private final Set<Variable> finallyTmpVars;
 
     private final Set<Variable> catchTmpVars;
 
     public IntermediateRepresentationBuilder(ControlFlowGraphBuilder cfgBuilder,
                                              InstructionBuilder instBuilder,
                                              ClassNameFactory classNameFactory,
                                              TemporaryVariableFactory tmpVarFactory,
                                              IRInstFactory instFactory) {
         this.cfgBuilder = cfgBuilder;
         this.instBuilder = instBuilder;
         this.classNameFactory = classNameFactory;
         this.tmpVarFactory = tmpVarFactory;
         this.instFactory = instFactory;
         finallyTmpVars = new HashSet<Variable>();
         catchTmpVars = new HashSet<Variable>();
         magicString = new StringConst("MAGIC", classNameFactory);
     }
 
     private void processBB(BasicBlock bb, List<VariableStack> inputStacks) {
 
         logger.log(Level.FINER, "Process {0}", bb);
 
         VariableStack inputStack = null;
 
         if (bb.hasAttribute(BasicBlockAttribute.EXCEPTION_HANDLER_ENTRY)) {
             // at the entry block of an exception handler the stack only contains
             // the exception variable
             inputStack = new VariableStack();
             Variable exceptionVar = tmpVarFactory.create(bb);
             IRInst tmpInst;
             if (bb.hasAttribute(BasicBlockAttribute.FINALLY_ENTRY)) {
                 finallyTmpVars.add(exceptionVar);
                 tmpInst = instFactory.newAssignConst(exceptionVar, magicString);
             } else { // catch
                 ExceptionHandlerInfo info = (ExceptionHandlerInfo) bb.getData();
                 catchTmpVars.add(exceptionVar);
                 ClassName className = classNameFactory.newClassName(info.getClassName());
                 tmpInst = instFactory.newNewObject(exceptionVar, JavaType.newRefType(className));
             }
             bb.getInstructions().add(tmpInst);
             inputStack.push(exceptionVar);
         } else {
             if (inputStacks.isEmpty()) {
                 inputStack = new VariableStack();
             } else if (inputStacks.size() == 1) {
                 inputStack = inputStacks.get(0).clone();
             } else {
                 inputStack = mergeStacks(inputStacks, bb);
             }
         }
 
         bb.setInputStack(inputStack.clone());
 
         if (bb.getInputStack().size() > 0) {
             logger.log(Level.FINEST, ">>> Input stack : {0}", bb.getInputStack());
         }
 
         VariableStack outputStack = inputStack.clone();
 
         instBuilder.build(bb, outputStack);
         bb.setOutputStack(outputStack);
 
         if (bb.getOutputStack().size() > 0) {
             logger.log(Level.FINEST, "<<< Output stack : {0}", bb.getOutputStack());
         }
     }
 
     private VariableStack mergeStacks(List<VariableStack> stacks, BasicBlock bb) {
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
        for (int i = stacks.get(0).size()-1; i >= 0 ; i--) {
             Set<Variable> vars = new HashSet<Variable>(stacks.size());
             for (int j = 0; j < stacks.size(); j++) {
                 vars.add(toList.get(j).get(i));
             }
             if (vars.size() == 1) {
                 stacksMerge.push(vars.iterator().next());
             } else {
                 Variable result = tmpVarFactory.create(bb);
                 bb.getInstructions().add(instFactory.newChoice(result, vars));
                 stacksMerge.push(result);
             }
         }
 
         return stacksMerge;
     }
 
     private void cleanupExceptionHandlers() {
         Set<Variable> finallyVars = new HashSet<Variable>();
 
         for (BasicBlock bb : cfg.getBasicBlocks()) {
             if (!bb.hasAttribute(BasicBlockAttribute.EXCEPTION_HANDLER_ENTRY)) {
                 continue;
             }
 
             IRInstSeq seq = bb.getInstructions();
             for (int i = 0; i < seq.size()-1; i++) {
                 IRInst inst = seq.get(i);
                 IRInst inst2 = seq.get(i+1);
 
                 boolean remove = false;
                 Variable excVar = null;
 
                 if (inst instanceof AssignConstInst
                         && inst2 instanceof AssignVarInst) {
                     AssignConstInst assignCstInst = (AssignConstInst) inst;
                     AssignVarInst assignVarInst = (AssignVarInst) inst2;
                     if (finallyTmpVars.contains(assignCstInst.getResult())
                             && assignCstInst.getConst() == magicString
                             && !assignVarInst.getResult().isTemporary()
                             && assignCstInst.getResult().equals(assignVarInst.getValue())) {
                         excVar = assignVarInst.getResult();
                         finallyVars.add(assignVarInst.getResult());
                         remove = true;
                     }
                 }
                 if (inst instanceof NewObjectInst
                         && inst2 instanceof AssignVarInst) {
                     NewObjectInst newObjInst = (NewObjectInst) inst;
                     AssignVarInst assignVarInst = (AssignVarInst) inst2;
                     if (catchTmpVars.contains(newObjInst.getResult())
                             && !assignVarInst.getResult().isTemporary()
                             && newObjInst.getResult().equals(assignVarInst.getValue())) {
                         excVar = assignVarInst.getResult();
                         remove = true;
                     }
                 }
 
                 if (remove) {
                     ((ExceptionHandlerInfo) bb.getData()).setVariable(excVar);
                     logger.log(Level.FINEST, "Cleanup exception handler (bb={0}, excVar={1}) :",
                             new Object[] {bb, excVar});
                     logger.log(Level.FINEST, "  Remove inst : {0}", IRInstWriter.toText(inst));
                     logger.log(Level.FINEST, "  Remove inst : {0}", IRInstWriter.toText(inst2));
                     inst.setIgnored(true);
                     inst2.setIgnored(true);
                 }
             }
         }
 
         for (BasicBlock bb : cfg.getBasicBlocks()) {
             IRInstSeq seq = bb.getInstructions();
             for (int i = 0; i < seq.size()-1; i++) {
                 IRInst inst = seq.get(i);
                 IRInst inst2 = seq.get(i+1);
 
                 boolean remove = false;
 
                 Variable excVar = null;
                 if (inst instanceof AssignVarInst
                         && inst2 instanceof ThrowInst) {
                     AssignVarInst assignVarInst = (AssignVarInst) inst;
                     ThrowInst throwInst = (ThrowInst) inst2;
                     if (finallyVars.contains(assignVarInst.getValue())
                             && assignVarInst.getResult().equals(throwInst.getVar())) {
                         excVar = assignVarInst.getValue();
                         remove = true;
                     }
                 }
 
                 if (remove) {
                     logger.log(Level.FINEST, "Cleanup finally rethrow (excVar={0}) :", excVar);
                     logger.log(Level.FINEST, "  Remove inst : {0}", IRInstWriter.toText(inst));
                     logger.log(Level.FINEST, "  Remove inst : {0}", IRInstWriter.toText(inst2));
                     inst.setIgnored(true);
                     inst2.setIgnored(true);
                 }
             }
         }
     }
 
     private void buildInst() {
         ConsoleUtil.logTitledSeparator(logger, Level.FINE, "Build instructions of {0}",
                 '=', cfg.getName());
 
         for (BasicBlock bb : cfg.getBasicBlocks()) {
             bb.setInstructions(new IRInstSeq());
         }
 
         List<BasicBlock> blocksToProcess = new ArrayList<BasicBlock>(cfg.getDFST().getNodes());
         while (blocksToProcess.size() > 0) {
             for (Iterator<BasicBlock> it = blocksToProcess.iterator(); it.hasNext();) {
                 BasicBlock bb = it.next();
 
                 List<VariableStack> inputStacks = new ArrayList<VariableStack>();
                 for (Edge incomingEdge : cfg.getIncomingEdgesOf(bb)) {
                     if (incomingEdge.hasAttribute(EdgeAttribute.LOOP_BACK_EDGE)) {
                         continue;
                     }
                     BasicBlock pred = cfg.getEdgeSource(incomingEdge);
                     inputStacks.add(pred.getOutputStack().clone());
                 }
 
                 processBB(bb, inputStacks);
                 it.remove();
             }
         }
 
         // cleanup exception handler by removing fake instructions
         cleanupExceptionHandlers();
     }
 
     /**
      * Replace choice instructions by conditional instructions (ternary operator)
      */
     public void resolveChoiceInst() {
         ConsoleUtil.logTitledSeparator(logger, Level.FINE,
                 "Resolve choice instructions of {0}", '=', cfg.getName());
 
         for (BasicBlock joinBlock : cfg.getDFST()) {
             IRInstSeq joinInsts = joinBlock.getInstructions();
 
             for (int i = 0; i < joinInsts.size(); i++) {
                 IRInst inst = joinInsts.get(i);
                 if (!(inst instanceof ChoiceInst)) {
                     continue;
                 }
                 ChoiceInst choiceInst = (ChoiceInst) inst;
 
                 List<IRInst> replacement = new ArrayList<IRInst>();
 
                 boolean change = true;
                 while (change) {
                     change = false;
 
                     Multimap<BasicBlock, Variable> forkBlocks
                             = HashMultimap.create();
                     for (Variable var : choiceInst.getChoices()) {
                         BasicBlock block = var.getBasicBlock();
                         DominatorInfo<BasicBlock, Edge> domInfo = cfg.getDominatorInfo();
                         BasicBlock forkBlock = domInfo.getDominatorsTree().getParent(block);
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
                             PostDominatorInfo<BasicBlock, Edge> postDomInfo = cfg.getPostDominatorInfo();
                             Edge forkEdge1 = postDomInfo.getPostDominanceFrontierOf(block1).iterator().next();
                             Edge forkEdge2 = postDomInfo.getPostDominanceFrontierOf(block2).iterator().next();
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
                                 JumpIfInst jumpIfInst = (JumpIfInst) forkBlock.getInstructions().getLast();
                                 choiceInst.getChoices().remove(thenVar);
                                 choiceInst.getChoices().remove(elseVar);
                                 Variable condVar = jumpIfInst.getCond().clone();
                                 if (choiceInst.getChoices().isEmpty()) {
                                     Variable resultVar = choiceInst.getResult();
                                     ConditionalInst condInst
                                             = instFactory.newConditional(resultVar, condVar, thenVar, elseVar);
                                     logger.log(Level.FINER, "Replace inst at {0} of {1} : {2}",
                                             new Object[]{i, joinBlock, IRInstWriter.toText(condInst)});
                                     replacement.add(condInst);
                                 } else {
                                     Variable resultVar = tmpVarFactory.create(forkBlock);
                                     ConditionalInst condInst
                                             = instFactory.newConditional(resultVar, condVar, thenVar, elseVar);
                                     logger.log(Level.FINER, "Insert inst at {0} of {1} : {2}",
                                             new Object[]{i, joinBlock, IRInstWriter.toText(condInst)});
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
 
     public void addFakeEdges() {
         for (BasicBlock bb : cfg.getBasicBlocks()) {
             if (bb.equals(cfg.getEntryBlock()) || bb.equals(cfg.getExitBlock())) {
                 continue;
             }
             IRInstSeq insts = bb.getInstructions();
             if (insts == null) {
                 throw new ABCDException("insts == null");
             }
             if (cfg.getSuccessorCountOf(bb) == 0
                     && insts.getLast() instanceof ThrowInst) {
                 Edge fakeEdge = cfg.addEdge(bb, cfg.getExitBlock());
                 fakeEdge.addAttribute(EdgeAttribute.FAKE_EDGE);
                 logger.log(Level.FINEST, "Add fake edge {0}", cfg.toString(fakeEdge));
             }
         }
         for (NaturalLoop loop : cfg.getNaturalLoops().values()) {
             if (loop.getExits().isEmpty()) { // infinite loop
                 Edge fakeEdge = cfg.addEdge(loop.getHead(), cfg.getExitBlock());
                 fakeEdge.addAttribute(EdgeAttribute.FAKE_EDGE);
                 logger.log(Level.FINEST, "Add fake edge {0}", cfg.toString(fakeEdge));
             }
         }
     }
 
     public ControlFlowGraph build(ABCDWriter writer) {
         // build control flow graph from bytecode
         cfg = cfgBuilder.build();
 
         cfg.removeUnreachableBlocks();
         cfg.updateDominatorInfo();
         cfg.updateLoopInfo();
 
         writer.writeRawCFG(cfg, cfgBuilder.getGraphizRenderer());
 
         // build basic blocks instructions
         buildInst();
 
         cfg.removeUnnecessaryBlock();
         cfg.updateDominatorInfo();
         cfg.updateLoopInfo();
 
         // add fake edges to be able to compute post dominance in case of infinite
         // loops et throw instructions
         addFakeEdges();
 
         cfg.updatePostDominatorInfo();
 
         // collapse shortcut operators (&&, ||)
         new ShortcutOperatorsCollapser(cfg, tmpVarFactory, instFactory).collapse();
 
         cfg.updateDominatorInfo();
         cfg.updatePostDominatorInfo();
         cfg.updateLoopInfo();
 
         // must be done after collapsing shortcut operators because of conditional
         // instruction with shortcut operators in the condition
         resolveChoiceInst();
 
         // need to remove critical edges to convert to SSA
         cfg.removeCriticalEdges();
         cfg.updateDominatorInfo();
         cfg.updatePostDominatorInfo();
         cfg.updateLoopInfo();
 
         // convert to SSA form
         new SSAFormConverter(cfg, instFactory).convert();
 
         // to remove empty basic blocks added tu remove critical edges
         cfg.removeUnnecessaryBlock();
         cfg.updateDominatorInfo();
         cfg.updatePostDominatorInfo();
         cfg.updateLoopInfo();
 
         writer.writeCFG(cfg);
 
         return cfg;
     }
 }
