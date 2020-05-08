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
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import fr.jamgotchian.abcd.core.controlflow.BasicBlock;
 import fr.jamgotchian.abcd.core.controlflow.ControlFlowGraph;
 import fr.jamgotchian.abcd.core.controlflow.DominatorInfo;
 import fr.jamgotchian.abcd.core.controlflow.Edge;
 import fr.jamgotchian.abcd.core.tac.model.DefInst;
 import fr.jamgotchian.abcd.core.tac.model.GotoInst;
 import fr.jamgotchian.abcd.core.tac.model.JumpIfInst;
 import fr.jamgotchian.abcd.core.tac.model.Variable;
 import fr.jamgotchian.abcd.core.tac.model.PhiInst;
 import fr.jamgotchian.abcd.core.tac.model.TACInst;
 import fr.jamgotchian.abcd.core.tac.model.TACInstFactory;
 import fr.jamgotchian.abcd.core.tac.model.VariableID;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
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
 public class SSAFormConverter {
 
     private static final Logger logger = Logger.getLogger(SSAFormConverter.class.getName());
 
     static {
        logger.setLevel(Level.FINER);
     }
 
     private static class Counter {
 
         private int count;
 
         private Counter(int count) {
             this.count = count;
         }
 
         public int getCount() {
             return count;
         }
 
         public void increment() {
             count++;
         }
     }
 
     private final ControlFlowGraph graph;
 
     private final TACInstFactory instFactory;
 
     private Multimap<BasicBlock, Integer> liveVariables;
 
     private Multimap<Integer, BasicBlock> defBlocks;
 
     public SSAFormConverter(ControlFlowGraph graph, TACInstFactory instFactory) {
         this.graph = graph;
         this.instFactory = instFactory;
     }
 
     private static boolean containsDef(BasicBlock block, int defIndex) {
         for (TACInst inst : ((AnalysisData) block.getData()).getInstructions()) {
             if (inst instanceof DefInst) {
                 Variable def = ((DefInst) inst).getResult();
                 if (def.getIndex() == defIndex) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private void insertPhiFunctions() {
         DominatorInfo<BasicBlock, Edge> dominatorInfo = graph.getDominatorInfo();
 
         Multimap<Integer, BasicBlock> phi = HashMultimap.create();
         for (int defIndex : defBlocks.keySet()) {
             Set<BasicBlock> w = new HashSet<BasicBlock>(defBlocks.get(defIndex));
             while (w.size() > 0) {
                 BasicBlock n = w.iterator().next();
                 w.remove(n);
                 Set<Edge> frontier = dominatorInfo.getDominanceFrontierOf(n);
                 for (Edge e : frontier) {
                     BasicBlock y = graph.getEdgeTarget(e);
                     if (!phi.get(defIndex).contains(y)) {
                         boolean contains = containsDef(y, defIndex);
                         List<Variable> args = new ArrayList<Variable>();
                         for (int i = 0; i < graph.getPredecessorCountOf(y); i++) {
                             args.add(new Variable(defIndex, y));
                         }
                         // is definition alive in basic block y ?
                         if (liveVariables.get(y).contains(defIndex)) {
                             ((AnalysisData) y.getData()).getInstructions()
                                     .add(0, instFactory.newPhi(new Variable(defIndex, y), args));
                             logger.log(Level.FINEST, "  Add Phi function to {0} for var {1}",
                                     new Object[] {y, defIndex});
                         }
                         phi.put(defIndex, y);
                         if (!contains) {
                             w.add(y);
                         }
                     }
                 }
             }
         }
     }
 
     private void renameVariables() {
         for (int defIndex : defBlocks.keySet()) {
             Counter versionCount = new Counter(0);
             Deque<Integer> versionStack = new ArrayDeque<Integer>();
             versionStack.push(0);
             renameVariables(defIndex, graph.getEntryBlock(), versionStack, versionCount);
         }
     }
 
     private void renameVariables(int varIndex, BasicBlock n, Deque<Integer> versionStack,
                                  Counter versionCount) {
         logger.log(Level.FINEST, "  Rename variables {0} of {1}",
                 new Object[] {varIndex, n});
 
         // generated version in current block
         Set<Integer> generatedVersion = new HashSet<Integer>();
 
         for (TACInst inst : ((AnalysisData) n.getData()).getInstructions()) {
             // for each use of the variable rename with the current version
             // (version on top of version stack)
             if (!(inst instanceof PhiInst)) {
                 for (Variable use : inst.getUses()) {
                     if (use.getIndex() == varIndex) {
                         int version = versionStack.peek();
                         use.setVersion(version);
                     }
                 }
             }
             // for each new definition of the variable create and push a new
             // version on version stack
             if (inst instanceof DefInst) {
                 Variable def = ((DefInst) inst).getResult();
                 if (def.getIndex() == varIndex) {
                     int nextVersion = versionCount.getCount();
                     versionCount.increment();
                     versionStack.push(nextVersion);
                     def.setVersion(nextVersion);
                     generatedVersion.add(nextVersion);
                 }
             }
         }
 
         // fill in phi function parameters of successor blocks
         for (BasicBlock y : graph.getSuccessorsOf(n)) {
             for (TACInst inst : ((AnalysisData) y.getData()).getInstructions()) {
                 if (inst instanceof PhiInst) {
                     int j = new ArrayList<BasicBlock>(graph.getPredecessorsOf(y)).indexOf(n);
                     int version = versionStack.peek();
                     Variable phi = ((PhiInst) inst).getArgs().get(j);
                     if (phi.getIndex() == varIndex) {
                         phi.setVersion(version);
                     }
                 }
             }
         }
 
         // recurse on current block children in the dominator tree
         DominatorInfo<BasicBlock, Edge> dominatorInfo = graph.getDominatorInfo();
         for (BasicBlock x : dominatorInfo.getDominatorsTree().getChildren(n)) {
             renameVariables(varIndex, x, versionStack, versionCount);
         }
 
         // remove from the version stack, versions generated in current block
         for (Integer version : generatedVersion) {
             versionStack.remove(version);
         }
     }
 
     private static void addInst(BasicBlock b, TACInst inst) {
         List<TACInst> insts = ((AnalysisData) b.getData()).getInstructions();
         if (insts.isEmpty()) {
             insts.add(inst);
         } else {
             TACInst lastInst = insts.get(insts.size()-1);
             if (lastInst instanceof GotoInst || lastInst instanceof JumpIfInst) {
                 insts.add(insts.size()-1, inst);
             } else {
                 insts.add(inst);
             }
         }
     }
 
     private void removePhiFunctions() {
         for (BasicBlock b : graph.getBasicBlocks()) {
             List<BasicBlock> predecessors
                     = new ArrayList<BasicBlock>(graph.getPredecessorsOf(b));
             List<TACInst> insts = ((AnalysisData) b.getData()).getInstructions();
             for (Iterator<TACInst> it = insts.iterator(); it.hasNext();) {
                 TACInst inst = it.next();
                 if (inst instanceof PhiInst) {
                     PhiInst phiInst = (PhiInst) inst;
                     for (int i = 0; i < phiInst.getArgs().size(); i++) {
                        Variable result = phiInst.getResult();
                         Variable v = phiInst.getArgs().get(i);
                         BasicBlock p = predecessors.get(i);
                        if (!result.getID().equals(v.getID())) {
                            addInst(p, instFactory.newAssignVar(result.clone(),
                                                                v /* no need to clone */));
                        }
                     }
                     it.remove();
                 }
             }
         }
     }
 
     public void convert() {
         // live variables analysis
         liveVariables = HashMultimap.create();
         for (Map.Entry<BasicBlock, Set<Variable>> entry :
                 new LiveVariablesAnalysis(graph).analyse().entrySet()) {
             BasicBlock block = entry.getKey();
             for (Variable var : entry.getValue()) {
                 if (var.getVersion() != VariableID.UNDEFINED_VERSION) {
                     throw new ABCDException("var.getVersion() != VariableID.UNDEFINED_VERSION");
                 }
                 liveVariables.put(block, var.getIndex());
             }
         }
 
         logger.log(Level.FINER, "Convert to SSA form");
 
         // fill map containing all definitions and its basic block
         defBlocks = HashMultimap.create();
         for (BasicBlock block : graph.getBasicBlocks()) {
             for (TACInst inst : ((AnalysisData) block.getData()).getInstructions()) {
                 if (inst instanceof DefInst) {
                     Variable def = ((DefInst) inst).getResult();
                     defBlocks.put(def.getIndex(), block);
                 }
             }
         }
 
         insertPhiFunctions();
 
         renameVariables();
 
         removePhiFunctions();
     }
 }
