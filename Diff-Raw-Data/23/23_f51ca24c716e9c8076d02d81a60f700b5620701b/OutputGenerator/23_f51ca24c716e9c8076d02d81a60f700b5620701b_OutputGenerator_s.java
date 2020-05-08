 package edu.mit.compilers.graphmodel;
 
 import java.util.*;
 
 import edu.mit.compilers.IR.*;
 
 public class OutputGenerator {
   
   public void generate(IrProgram p) {
     GraphPreparer preparer = new GraphPreparer();
     NodeDescriber nDescriber = new NodeDescriber();
     FunctionInliner inliner = new FunctionInliner();
     
     List<IrNode> program;
     Map<String, IrFunctionDef> defs = new HashMap<String, IrFunctionDef>();
     Map<String, List<IrNode>> code = new HashMap<String, List<IrNode>>();
     
     for (IrFunctionDef fn : p.getFunctions()) {
       String fnName = fn.getName();
       List<IrNode> unsimplifiedLinearRep = preparer.prepare(fn);
       List<IrNode> simplifiedLinearRep = simplifyLinearRep(unsimplifiedLinearRep);
       
       defs.put(fnName, fn);
       code.put(fnName, simplifiedLinearRep);
     }
     
     program = inliner.inline(defs, code);
     
    int i = 0;
     for (IrNode n : program) {
       System.out.print(i++ + ":\t");
       System.out.println(n);
    }
     
     AdjacenyMatrix adj = getAdjacencyMatrix(program);
     NodeDescriptionTable nodeDescriptions = new NodeDescriptionTable();
     
     //int i = 0;
     for (IrNode n : program) {
       int[] desc = nDescriber.getNodeDescription(n);
       nodeDescriptions.addRow(desc);
     }
     
     GraphPrinter.print(adj, nodeDescriptions);
   }
   
   private AdjacenyMatrix getAdjacencyMatrix(List<IrNode> linearRep) {
     int numNodes = linearRep.size();
     AdjacenyMatrix out = new AdjacenyMatrix(numNodes);
     
     IrNode next;
     IrJmp nextJump;
     int jumpTarget;
     for (int i = 0; i < numNodes; i++) {
       next = linearRep.get(i);
       if (next instanceof IrJmp) {
         nextJump = (IrJmp)next;
         jumpTarget = nextJump.getTarget();
         out.addJumpLink(i, jumpTarget);
       } else {
         out.addNonJumpLink(i);
       }
     }
     
     return out;
   }
   
   private List<IrNode> simplifyLinearRep(List<IrNode> unsimplified) {
     List<IrNode> simplified = new ArrayList<IrNode>();
     Map<Integer, Integer> labelsToIndexes = new HashMap<Integer, Integer>();
     
     IrNode next;
     
     int end = unsimplified.size() - 1;
     int nonLabelIndex = 0;
     int labelNum;
     
     // Start by gathering instructions and mapping labels to the index in a reversed list
     for (int i = end; i >= 0; i--) {
       next = unsimplified.get(i);
       if (next instanceof IrLabel) {
         labelNum = ((IrLabel) next).getNum();
         labelsToIndexes.put(labelNum, nonLabelIndex);
       } else {
         nonLabelIndex = simplified.size();
         simplified.add(next);
       }
     }   
 
     // Put instructions in correct order and invert the indexes
     int back = simplified.size() - 1;
     int oldIndex, newIndex;
     Collections.reverse(simplified);
     for (int label : labelsToIndexes.keySet()) {
       oldIndex = labelsToIndexes.get(label);
       newIndex = back - oldIndex;
       labelsToIndexes.put(label, newIndex);
     }
     
     // Alter the jumps to now use the labeled node locations
     IrJmp j;
     int label, index;
     for (IrNode n : simplified) {
       if (n instanceof IrJmp) {
         j = (IrJmp)n;
         label = j.getTarget();
         index = labelsToIndexes.get(label);
         j.setTarget(index);
       }
     }
     
     return simplified;
   }
 }
