 package uk.ac.cam.db538.dexter.analysis;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 
 public class ClashGraph implements Cloneable {
 
   @Getter private final DexCode code;
 
   private final PriorityQueue<DexRegister> vertices;
   private final Map<DexRegister, Set<DexRegister>> edges;
 
   private final Comparator<DexRegister> vertexComparator = new Comparator<DexRegister>() {
     @Override
     public int compare(DexRegister regA, DexRegister regB) {
       return Integer.compare(getNodeDegree(regA), getNodeDegree(regB));
     }
   };
 
   public ClashGraph(DexCode code) {

     val usedRegisters = code.getUsedRegisters();
 
     this.code = code;
    this.vertices = new PriorityQueue<DexRegister>(usedRegisters.size(), vertexComparator);
     this.edges = new HashMap<DexRegister, Set<DexRegister>>();
 
     this.vertices.addAll(usedRegisters);
 
     update();
   }
 
   private ClashGraph(ClashGraph cg) {
     this.code = cg.code;
    this.vertices = new PriorityQueue<DexRegister>(cg.vertices.size(), vertexComparator);
     this.edges = new HashMap<DexRegister, Set<DexRegister>>();
 
     this.vertices.addAll(cg.vertices);
     for (val edgesEntry : cg.edges.entrySet()) {
       val nodeA = edgesEntry.getKey();
       for (val nodeB : edgesEntry.getValue())
         addEdge(nodeA, nodeB);
     }
   }
 
   private void addEdge(DexRegister nodeA, DexRegister nodeB) {
     Set<DexRegister> setA = edges.get(nodeA);
     Set<DexRegister> setB = edges.get(nodeB);
 
     if (setA == null) {
       setA = new HashSet<DexRegister>();
       edges.put(nodeA, setA);
     }
     if (setB == null) {
       setB = new HashSet<DexRegister>();
       edges.put(nodeB, setB);
     }
 
     setA.add(nodeB);
     setB.add(nodeA);
   }
 
   public void update() {
     val LVA = new LiveVarAnalysis(code);
     for (val insn : code.getInstructionList()) {
       // generate all pairs of live-vars-out
       val liveVarsOut_Set = LVA.getLiveVarsOut(insn);
       val liveVarsOut = liveVarsOut_Set.toArray(new DexRegister[liveVarsOut_Set.size()]);
       for (int i = 0; i < liveVarsOut.length - 1; ++i)
         for (int j = i + 1; j < liveVarsOut.length; ++j)
           addEdge(liveVarsOut[i], liveVarsOut[j]);
 
       // generate pairs from the defined registers to live-vars-in
       val insnDefRegs = insn.lvaDefinedRegisters();
       val liveVarsIn = LVA.getLiveVarsIn(insn);
       for (val defReg : insnDefRegs)
         for (val liveVar : liveVarsIn)
           if (defReg != liveVar)
             addEdge(defReg, liveVar);
     }
   }
 
   public boolean noVerticesLeft() {
     return vertices.isEmpty();
   }
 
   public boolean noEdgesLeft() {
     return edges.isEmpty();
   }
 
   public boolean areClashing(DexRegister rA, DexRegister rB) {
     val edgesA = edges.get(rA);
     return (edges != null) && edgesA.contains(rB);
   }
 
   private int getNodeDegree(DexRegister reg) {
     val edgesReg = edges.get(reg);
     if (edgesReg == null)
       return 0;
     else
       return edgesReg.size();
   }
 
   public DexRegister removeLowestDegreeNode() {
     val lowest = vertices.poll();
     if (lowest == null)
       return null;
 
     val neighbours = edges.get(lowest);
     if (neighbours != null) {
       for (val n : neighbours) {
         val edgesN = edges.get(n);
         if (edgesN != null)
           edgesN.remove(lowest);
       }
       edges.remove(lowest);
     }
 
     return lowest;
   }
 
   public Set<DexRegister> getNodeNeighbours(DexRegister node) {
     val neighbours = edges.get(node);
     if (neighbours == null)
       return new HashSet<DexRegister>();
     else
       return neighbours;
   }
 
   @Override
   public Object clone() {
     return new ClashGraph(this);
   }
 }
