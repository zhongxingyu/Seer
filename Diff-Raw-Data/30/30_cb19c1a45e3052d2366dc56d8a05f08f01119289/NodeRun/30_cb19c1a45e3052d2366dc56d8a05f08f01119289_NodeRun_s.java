 package uk.ac.cam.db538.dexter.analysis.coloring;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
 
 public class NodeRun {
 
   private final NoDuplicatesList<DexRegister> nodes;
 
   public NodeRun() {
     nodes = new NoDuplicatesList<DexRegister>();
   }
 
   public NodeRun(DexRegister[] regs) {
     this();
     for (val reg : regs)
       nodes.add(reg);
   }
 
   public void add(DexRegister reg) {
     nodes.add(reg);
   }
 
   public void addAll(Collection<DexRegister> regs) {
     nodes.addAll(regs);
   }
 
   public List<DexRegister> getNodes() {
     return Collections.unmodifiableList(nodes);
   }
 
   public DexRegister getFirst() {
     return nodes.getFirst();
   }
 
   public DexRegister peekFirst() {
     return nodes.peekFirst();
   }
 
   public DexRegister peekLast() {
     return nodes.peekLast();
   }
 
   public int getIndexOf(DexRegister reg) {
     return nodes.indexOf(reg);
   }
 }
