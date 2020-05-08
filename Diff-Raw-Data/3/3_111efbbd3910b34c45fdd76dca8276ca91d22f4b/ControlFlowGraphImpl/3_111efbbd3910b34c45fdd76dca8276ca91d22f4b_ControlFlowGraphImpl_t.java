 /*
  * Copyright (C) 2010 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
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
 
 import fr.jamgotchian.abcd.core.graph.PostDominatorInfo;
 import fr.jamgotchian.abcd.core.graph.DominatorInfo;
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import fr.jamgotchian.abcd.core.graph.GraphvizRenderer;
 import fr.jamgotchian.abcd.core.graph.DirectedGraph;
 import fr.jamgotchian.abcd.core.graph.DirectedGraphs;
 import fr.jamgotchian.abcd.core.graph.MutableDirectedGraph;
 import fr.jamgotchian.abcd.core.graph.Matrix;
 import fr.jamgotchian.abcd.core.graph.Tree;
 import fr.jamgotchian.abcd.core.graph.Trees;
 import fr.jamgotchian.abcd.core.graph.EdgeFactory;
 import fr.jamgotchian.abcd.core.util.Range;
 import fr.jamgotchian.abcd.core.util.RangeImpl;
 import fr.jamgotchian.abcd.core.util.RangeMap;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class ControlFlowGraphImpl implements ControlFlowGraph {
 
     private static final Logger logger = Logger.getLogger(ControlFlowGraphImpl.class.getName());
 
     private static final IRGraphvizRenderer IR_GRAPHVIZ_RENDERER
             = new IRGraphvizRenderer();
 
     private static final EdgeGraphvizRenderer EDGE_GRAPHVIZ_RENDERER
             = new EdgeGraphvizRenderer();
 
     private static final RangeGraphvizRenderer RANGE_GRAPHVIZ_RENDERER
             = new RangeGraphvizRenderer();
 
     private final String name;
 
     private final BasicBlock entryBlock;
 
     private final BasicBlock exitBlock;
 
     private final MutableDirectedGraph<BasicBlock, Edge> graph;
 
     private final RangeMap<Range, BasicBlock> basicBlocks;
 
     private final Map<BasicBlock, NaturalLoop> naturalLoops;
 
     private Tree<BasicBlock, Edge> dfst;
 
     private DominatorInfo<BasicBlock, Edge> dominatorInfo;
 
     private PostDominatorInfo<BasicBlock, Edge> postDominatorInfo;
 
     private LocalVariableTable localVariableTable;
 
     private ExceptionTable exceptionTable;
 
     private static final EdgeFactory<Edge> EDGE_FACTORY = new EdgeFactoryImpl();
 
     public ControlFlowGraphImpl(String name, int instructionCount) {
         this(name, new BasicBlockImpl(Integer.MIN_VALUE, -1, BasicBlockType.ENTRY),
                 new BasicBlockImpl(BasicBlockType.EXIT));
         if (instructionCount > 0 ) {
             BasicBlock instnBlock = new BasicBlockImpl(0, instructionCount-1, null);
             addBasicBlock(instnBlock);
             addEdge(entryBlock, instnBlock);
         } else {
             addEdge(entryBlock, exitBlock);
         }
     }
 
     public ControlFlowGraphImpl(String name, BasicBlock entryBlock) {
         this(name, entryBlock, new BasicBlockImpl(BasicBlockType.EXIT));
     }
 
     public ControlFlowGraphImpl(String name, BasicBlock entryBlock, BasicBlock exitBlock) {
         if (name == null) {
             throw new IllegalArgumentException("name == null");
         }
         this.name = name;
         this.entryBlock = entryBlock;
         this.exitBlock = exitBlock;
         graph = DirectedGraphs.newDirectedGraph();
         basicBlocks = new RangeMap<Range, BasicBlock>();
         naturalLoops = new HashMap<BasicBlock, NaturalLoop>();
         addBasicBlock(entryBlock);
         addBasicBlock(exitBlock);
     }
 
     public ControlFlowGraphImpl(String name) {
         this(name, new BasicBlockImpl(BasicBlockType.ENTRY),
                 new BasicBlockImpl(BasicBlockType.EXIT));
     }
 
     public String getName() {
         return name;
     }
 
     public DirectedGraph<BasicBlock, Edge> getGraph() {
         return DirectedGraphs.unmodifiableDirectedGraph(graph);
     }
 
     public BasicBlock getEntryBlock() {
         return entryBlock;
     }
 
     public BasicBlock getExitBlock() {
         return exitBlock;
     }
 
     public void updateDominatorInfo() {
         dominatorInfo = DominatorInfo.create(graph, entryBlock, EDGE_FACTORY);
     }
 
     public void updatePostDominatorInfo() {
         postDominatorInfo = PostDominatorInfo.create(graph, exitBlock, EDGE_FACTORY);
     }
 
     public DominatorInfo<BasicBlock, Edge> getDominatorInfo() {
         return dominatorInfo;
     }
 
     public PostDominatorInfo<BasicBlock, Edge> getPostDominatorInfo() {
         return postDominatorInfo;
     }
 
     public Tree<BasicBlock, Edge> getDFST() {
         return dfst;
     }
 
     public Map<BasicBlock, NaturalLoop> getNaturalLoops() {
         return naturalLoops;
     }
 
     public int getBasicBlockCount() {
         return graph.getVertices().size();
     }
 
     public Collection<BasicBlock> getBasicBlocks() {
         return graph.getVertices();
     }
 
     public BasicBlock getBasicBlock(Range range) {
         return basicBlocks.get(range);
     }
 
     public BasicBlock getBasicBlock(int first, int last) {
         return getBasicBlock(new RangeImpl(first, last));
     }
 
     public boolean containsBasicBlock(BasicBlock bb) {
         return graph.containsVertex(bb);
     }
 
     public Collection<Edge> getOutgoingEdgesOf(BasicBlock block) {
         return graph.getOutgoingEdgesOf(block);
     }
 
     public Collection<Edge> getNormalOutgoingEdgesOf(BasicBlock block) {
         List<Edge> edges = new ArrayList<Edge>();
         for (Edge e : getOutgoingEdgesOf(block)) {
             if (!e.hasAttribute(EdgeAttribute.EXCEPTIONAL_EDGE)) {
                 edges.add(e);
             }
         }
         return edges;
     }
 
     public Edge getFirstOutgoingEdgeOf(BasicBlock block) {
         return graph.getFirstOutgoingEdgeOf(block);
     }
 
     public Edge getFirstNormalOutgoingEdgeOf(BasicBlock block) {
         for (Edge e : getOutgoingEdgesOf(block)) {
             if (!e.hasAttribute(EdgeAttribute.EXCEPTIONAL_EDGE)) {
                 return e;
             }
         }
         return null;
     }
 
     public Collection<Edge> getIncomingEdgesOf(BasicBlock block) {
         return graph.getIncomingEdgesOf(block);
     }
 
     public Edge getFirstIncomingEdgeOf(BasicBlock block) {
         return graph.getFirstIncomingEdgeOf(block);
     }
 
     public Edge getFirstNormalIncomingEdgeOf(BasicBlock block) {
         for (Edge e : getIncomingEdgesOf(block)) {
             if (!e.hasAttribute(EdgeAttribute.EXCEPTIONAL_EDGE)) {
                 return e;
             }
         }
         return null;
     }
 
     public Collection<BasicBlock> getPredecessorsOf(BasicBlock block) {
         return graph.getPredecessorsOf(block);
     }
 
     public int getPredecessorCountOf(BasicBlock block) {
         return graph.getPredecessorCountOf(block);
     }
 
     public int getNormalPredecessorCountOf(BasicBlock block) {
         int count = 0;
         for (Edge e : getIncomingEdgesOf(block)) {
             if (!e.hasAttribute(EdgeAttribute.EXCEPTIONAL_EDGE)) {
                 count++;
             }
         }
         return count;
     }
 
     public BasicBlock getFirstPredecessorOf(BasicBlock block) {
         return graph.getFirstPredecessorOf(block);
     }
 
     public Collection<BasicBlock> getSuccessorsOf(BasicBlock block) {
         return graph.getSuccessorsOf(block);
     }
 
     public BasicBlock getFirstSuccessorOf(BasicBlock block) {
         return graph.getFirstSuccessorOf(block);
     }
 
     public int getSuccessorCountOf(BasicBlock block) {
         return graph.getSuccessorCountOf(block);
     }
 
     public Collection<BasicBlock> getExceptionalSuccessorsOf(BasicBlock block) {
         List<BasicBlock> successors = new ArrayList<BasicBlock>();
         for (Edge e : graph.getOutgoingEdgesOf(block)) {
             if (e.hasAttribute(EdgeAttribute.EXCEPTIONAL_EDGE)) {
                 successors.add(graph.getEdgeTarget(e));
             }
         }
         return successors;
     }
 
     public int getNormalSuccessorCountOf(BasicBlock block) {
         int count = 0;
         for (Edge e : getOutgoingEdgesOf(block)) {
             if (!e.hasAttribute(EdgeAttribute.EXCEPTIONAL_EDGE)) {
                 count++;
             }
         }
         return count;
     }
 
     public Collection<BasicBlock> getBasicBlocksWithinRange(Range range) {
         return basicBlocks.values(range);
     }
 
     public Collection<BasicBlock> getBasicBlocksWithinRange(int first, int last) {
         return getBasicBlocksWithinRange(new RangeImpl(first, last));
     }
 
     public boolean isBasicBlockReachable(BasicBlock block) {
         return graph.getIncomingEdgesOf(block).size() > 0
                 || graph.getOutgoingEdgesOf(block).size() > 0;
     }
 
     public Collection<BasicBlock> getNonEmptyBasicBlocks() {
         return basicBlocks.values();
     }
 
     public void addBasicBlock(BasicBlock block) {
         graph.addVertex(block);
         if (block.getRange() != null) {
             basicBlocks.put(block.getRange(), block);
         }
     }
 
     public void removeBasicBlock(BasicBlock block) {
         graph.removeVertex(block);
         if (block.getRange() != null) {
             basicBlocks.remove(block.getRange());
         }
     }
 
     public BasicBlockSplit splitBasicBlockAt(int index) {
         if (index == Integer.MIN_VALUE) {
             throw new ABCDException("Can't split at index Integer.MIN_VALUE");
         }
         BasicBlock blockAfter = basicBlocks.findBeginningAt(index);
         BasicBlock blockBefore = basicBlocks.findContaining(index - 1);
         Edge edge = null;
         if (blockAfter == null) {
             if (blockBefore == null) {
                 throw new ABCDException("Split index (" + index + ") out of range");
             }
 
             if (index <= blockBefore.getRange().getLast()) {
                 logger.log(Level.FINEST, "  Split {0} at {1}", new Object[]{blockBefore, index});
 
                 int last = blockBefore.getRange().getLast();
 
                 // resize the block before the split
                 resizeBasicBlock(blockBefore, index - 1);
 
                 // create the block after the split
                 blockAfter = new BasicBlockImpl(index, last, blockBefore.getType());
                 graph.splitVertex(blockBefore, blockAfter);
                 basicBlocks.put(blockAfter.getRange(), blockAfter);
 
                 blockBefore.setType(null);
 
                 edge = addEdge(blockBefore, blockAfter);
             }
         } else {
           edge = graph.getEdge(blockBefore, blockAfter);
         }
 
         return new BasicBlockSplit(blockBefore, blockAfter, edge);
     }
 
     private void resizeBasicBlock(BasicBlock block, int newLast) {
         if (block != null) {
             basicBlocks.remove(block.getRange());
             block.getRange().setLast(newLast);
             basicBlocks.put(block.getRange(), block);
         }
     }
 
     public Edge addEdge(BasicBlock source, BasicBlock target) {
         return addEdge(source, target, false);
     }
 
     public void addEdge(BasicBlock source, BasicBlock target, Edge edge) {
         graph.addEdge(source, target, edge);
     }
 
     public Edge addEdge(BasicBlock source, BasicBlock target, boolean exceptional) {
         Edge edge = graph.getEdge(source, target);
         if (edge == null) {
             logger.log(Level.FINEST, "  Create edge between {0} and {1}",
                     new Object[] {source, target});
             edge = EDGE_FACTORY.createEdge();
             if (exceptional) {
                 edge.addAttribute(EdgeAttribute.EXCEPTIONAL_EDGE);
             }
             graph.addEdge(source, target, edge);
         }
         return edge;
     }
 
     public void removeEdge(Edge edge) {
         graph.removeEdge(edge);
     }
 
     public boolean removeEdge(BasicBlock source, BasicBlock target) {
         logger.log(Level.FINEST, "  Remove edge between {0} and {1}", new Object[]{source, target});
 
         return graph.removeEdge(source, target);
     }
 
     public Edge getEdge(BasicBlock source, BasicBlock target) {
         return graph.getEdge(source, target);
     }
 
     public Set<Edge> getEdges() {
         return graph.getEdges();
     }
 
     public boolean containsEdge(BasicBlock source, BasicBlock target) {
         return graph.containsEdge(source, target);
     }
 
     public BasicBlock getEdgeSource(Edge edge) {
         return graph.getEdgeSource(edge);
     }
 
     public BasicBlock getEdgeTarget(Edge edge) {
         return graph.getEdgeTarget(edge);
     }
 
     public void updateLoopInfo() {
         logger.log(Level.FINER, "Update loop info");
         performDepthFirstSearch();
         for (Edge e : getEdges()) {
             e.resetState();
         }
         for (BasicBlock bb : getBasicBlocks()) {
             bb.resetState();
         }
         analyseEdgeCategory();
         analyseNaturalLoops();
         analyseLoopLevel();
     }
 
     void performDepthFirstSearch() {
         // build depth first spanning Tree
         dfst = graph.getReversePostOrderDFST(entryBlock, false);
         int order = 0;
         for (BasicBlock block : dfst.getNodes()) {
             block.setOrder(order++);
         }
 
         logger.log(Level.FINEST, "Perform reverse post order DFS");
         logger.log(Level.FINEST, "DFST : \n{0}", Trees.toString(dfst));
     }
 
     /**
      * Remove unreachable basic blocks. A basic block is unreachable if it has no
      * predecessors et no successors.
      */
     public void removeUnreachableBlocks() {
         for (BasicBlock block : new HashSet<BasicBlock>(graph.getVertices())) {
             if (!block.equals(entryBlock) && !block.equals(exitBlock)
                     && graph.getIncomingEdgesOf(block).isEmpty()
                     && graph.getOutgoingEdgesOf(block).isEmpty()) {
                 graph.removeVertex(block);
 
                 logger.log(Level.FINER, "Remove unreachable block {0}", block);
             }
         }
     }
 
     /**
      * Remove unnecessary basic blocks.
      */
     public void removeUnnecessaryBlock() {
         Set<BasicBlock> toRemove = new HashSet<BasicBlock>();
         for (BasicBlock bb : graph.getVertices()) {
             if (getPredecessorCountOf(bb) >= 1 &&
                getNormalSuccessorCountOf(bb) == 1 &&
                    !getFirstNormalOutgoingEdgeOf(bb).hasAttribute(EdgeAttribute.LOOP_BACK_EDGE)) {
                 boolean remove = true;
                 IRInstSeq Insts = bb.getInstructions();
                 if (Insts != null) {
                     for (IRInst inst : Insts) {
                         if (!inst.isIgnored()) {
                             remove = false;
                             break;
                         }
                     }
                 }
                 if (remove) {
                     toRemove.add(bb);
                 }
             }
         }
 
         for (BasicBlock bb : toRemove) {
             Edge outgoingEdge = getFirstNormalOutgoingEdgeOf(bb);
             BasicBlock successor = graph.getEdgeTarget(outgoingEdge);
             Collection<Edge> incomingEdges = graph.getIncomingEdgesOf(bb);
             for (Edge incomingEdge : new ArrayList<Edge>(incomingEdges)) {
                 BasicBlock predecessor = graph.getEdgeSource(incomingEdge);
                 graph.removeEdge(incomingEdge);
                 graph.addEdge(predecessor, successor, incomingEdge);
             }
             graph.removeEdge(outgoingEdge);
             graph.removeVertex(bb);
 
             logger.log(Level.FINER, "Remove unnecessary BB {0}", bb);
         }
     }
 
     /**
      * Remove critical edges. A critical edge is an edge which is neither the
      * only edge leaving its source block, nor the only edge entering its
      * destination block.
      * Those edges must be split (a new block must be created in the middle of
      * the edge) in order to insert computations on the edge without affecting
      * any other edges.
      */
     public void removeCriticalEdges() {
         List<Edge> criticalEdges = new ArrayList<Edge>();
 
         // find critical edges
         for (Edge e : graph.getEdges()) {
             BasicBlock source = graph.getEdgeSource(e);
             BasicBlock target = graph.getEdgeTarget(e);
             if (graph.getSuccessorCountOf(source) > 1
                     && graph.getPredecessorCountOf(target) > 1) {
                 criticalEdges.add(e);
             }
         }
 
         // remove critical edges
         for (Edge criticalEdge : criticalEdges) {
             logger.log(Level.FINER, "Remove critical edge {0}",
                     graph.toString(criticalEdge));
             BasicBlock source = graph.getEdgeSource(criticalEdge);
             BasicBlock target = graph.getEdgeTarget(criticalEdge);
             graph.removeEdge(criticalEdge);
             BasicBlock emptyBlock = new BasicBlockImpl(BasicBlockType.EMPTY);
             emptyBlock.setInstructions(new IRInstSeq());
             graph.addVertex(emptyBlock);
             graph.addEdge(source, emptyBlock, criticalEdge);
             graph.addEdge(emptyBlock, target, EDGE_FACTORY.createEdge());
         }
     }
 
     public Matrix<Boolean> calculateAncestorsMatrix(Collection<Edge> edges) {
         int nodeCount = graph.getVertices().size();
         Matrix<Boolean> ancestorsMatrix = new Matrix<Boolean>(nodeCount, nodeCount, Boolean.FALSE);
 
         for (Edge edge : edges) {
             BasicBlock source = graph.getEdgeSource(edge);
             BasicBlock target = graph.getEdgeTarget(edge);
             ancestorsMatrix.setValue(source.getOrder(), target.getOrder(), Boolean.TRUE);
         }
 
         // compute the transitive closure of the ancestor matrix
         for(int i = 0; i < nodeCount; i++) {
             for(int j = 0; j < nodeCount; j++) {
                 if(Boolean.TRUE.equals(ancestorsMatrix.getValue(i, j))) {
                     for(int k = 0; k < nodeCount; k++) {
                         if(Boolean.TRUE.equals(ancestorsMatrix.getValue(j, k))) {
                             ancestorsMatrix.setValue(i, k, Boolean.TRUE);
                         }
                     }
                 }
             }
         }
 
         return ancestorsMatrix;
     }
 
     private void analyseEdgeCategory() {
         Matrix<Boolean> ancestorsMatrix = calculateAncestorsMatrix(dfst.getEdges());
 
         for (Edge e : graph.getEdges()) {
             BasicBlock source = graph.getEdgeSource(e);
             BasicBlock target = graph.getEdgeTarget(e);
             if (ancestorsMatrix.getValue(source.getOrder(), target.getOrder()) ||
                 ancestorsMatrix.getValue(target.getOrder(), source.getOrder())) {
                 if (target.getOrder() > source.getOrder()) {
                     e.setCategory(EdgeCategory.ADVANCING);
                 } else if (target.getOrder() < source.getOrder()) {
                     if (dominatorInfo.dominates(target, source)) {
                         e.setCategory(EdgeCategory.BACK);
                     } else {
                         e.setCategory(EdgeCategory.RETREATING);
                     }
                 }
             } else {
                 e.setCategory(EdgeCategory.CROSS);
             }
 
             // self loop
             if (source.equals(target)) {
                 e.addAttribute(EdgeAttribute.SELF_LOOP_EDGE);
                 e.addAttribute(EdgeAttribute.LOOP_BACK_EDGE);
             }
 
             logger.log(Level.FINEST, "Edge Category of {0} : {1}",
                     new Object[] {graph.toString(e), e.getCategory()});
         }
     }
 
     private void analyseNaturalLoops() {
         naturalLoops.clear();
         for (Edge e : graph.getEdges()) {
             switch (e.getCategory()) {
                 case BACK: {
                     BasicBlock head = graph.getEdgeTarget(e);
                     BasicBlock v = graph.getEdgeSource(e);
                     Set<BasicBlock> visited = new HashSet<BasicBlock>();
                     visited.add(head);
                     List<BasicBlock> body = new ArrayList<BasicBlock>();
                     body.add(head);
                     graph.reversePostOrderDFS(v, visited, body, null, true);
                     NaturalLoop nl = new NaturalLoop(head, new HashSet<BasicBlock>(body));
                     naturalLoops.put(head, nl);
                     e.addAttribute(EdgeAttribute.LOOP_BACK_EDGE);
                     logger.log(Level.FINER, " Found natural loop : {0}", nl);
                     break;
                 }
 
                 case RETREATING: {
                     logger.warning("Irreducible control flow detected");
                 }
             }
         }
     }
 
     private void analyseLoopLevel() {
         // from outermost to innermost loops
         for (BasicBlock loopHead : dfst.getNodes()) {
             NaturalLoop nl = naturalLoops.get(loopHead);
             if (nl != null) {
                 // increase loop level for all blocks of the loop
                 for (BasicBlock block : nl.getBody()) {
                     block.setLoopLevel(block.getLoopLevel()+1);
                 }
             }
         }
 
         // self loop
         for (Edge edge : graph.getEdges()) {
             if (edge.hasAttribute(EdgeAttribute.SELF_LOOP_EDGE)) {
                 BasicBlock block = graph.getEdgeSource(edge);
 
                 NaturalLoop nl = new NaturalLoop(block, Collections.singleton(block));
                 naturalLoops.put(nl.getHead(), nl);
                 logger.log(Level.FINER, " Found self loop : {0}", nl);
 
                 block.setLoopLevel(block.getLoopLevel()+1);
             }
         }
 
         for (BasicBlock block : dfst.getNodes()) {
             logger.log(Level.FINEST, "Loop level of {0} : {1}",
                     new Object[] {block, block.getLoopLevel()});
         }
 
         for (NaturalLoop nl : naturalLoops.values()) {
             for (BasicBlock bb : nl.getBody()) {
                 for (Edge e : graph.getOutgoingEdgesOf(bb)) {
                     BasicBlock t = graph.getEdgeTarget(e);
                     if (!nl.getBody().contains(t)) {
                         logger.log(Level.FINEST, "Loop exit edge {0}", graph.toString(e));
                         e.addAttribute(EdgeAttribute.LOOP_EXIT_EDGE);
                         nl.getExits().add(e);
                     }
                 }
             }
         }
     }
 
     public void export(Writer writer,
                        GraphvizRenderer<BasicBlock> bbRenderer,
                        GraphvizRenderer<Edge> edgeRenderer) throws IOException {
         graph.export(writer, "\"" + name + "\"", bbRenderer, edgeRenderer);
     }
 
     public void export(Writer writer) throws IOException {
         graph.export(writer, "\"" + name + "\"", RANGE_GRAPHVIZ_RENDERER,
                      EDGE_GRAPHVIZ_RENDERER);
     }
 
     public void export(String fileName) {
         try {
             Writer writer = new FileWriter(fileName);
             export(writer);
             writer.close();
         } catch (IOException e) {
             logger.log(Level.SEVERE, e.toString(), e);
         }
     }
 
     public void exportInst(Writer writer) throws IOException {
         graph.export(writer, "\"" + name + "\"", IR_GRAPHVIZ_RENDERER,
                      EDGE_GRAPHVIZ_RENDERER);
     }
 
     public String toString(Collection<Edge> edges) {
         return graph.toString(edges);
     }
 
     public String toString(Edge edge) {
         return graph.toString(edge);
     }
 
     public LocalVariableTable getLocalVariableTable() {
         return localVariableTable;
     }
 
     public void setLocalVariableTable(LocalVariableTable localVariableTable) {
         this.localVariableTable = localVariableTable;
     }
 
     public ExceptionTable getExceptionTable() {
         return exceptionTable;
     }
 
     public void setExceptionTable(ExceptionTable exceptionTable) {
         this.exceptionTable = exceptionTable;
     }
 
     @Override
     public ControlFlowGraph clone() {
         ControlFlowGraph clone = new ControlFlowGraphImpl(name, entryBlock, exitBlock);
         for (BasicBlock bb : getBasicBlocks()) {
             if (!bb.equals(entryBlock) && !bb.equals(exitBlock)) {
                 clone.addBasicBlock(bb);
             }
         }
         for (Edge e : getEdges()) {
             clone.addEdge(getEdgeSource(e), getEdgeTarget(e), e);
         }
         clone.setExceptionTable(exceptionTable);
         clone.setLocalVariableTable(localVariableTable);
         return clone;
     }
 
     @Override
     public String toString() {
         return name;
     }
 }
