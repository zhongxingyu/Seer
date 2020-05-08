 package uk.ac.cam.db538.dexter.dex.code.elem;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.Setter;
 import lombok.val;
 import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
 import uk.ac.cam.db538.dexter.dex.Dex;
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Switch;
 import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public abstract class DexCodeElement {
 
   @Getter private final DexCode methodCode;
   @Getter @Setter private boolean originalElement = false;
   @Getter @Setter private boolean auxiliaryElement = false;
 
   public DexCodeElement(DexCode methodCode) {
     this.methodCode = methodCode;
   }
 
   public abstract String getOriginalAssembly();
 
   public final DexCodeElement getNextCodeElement() {
     val insns = methodCode.getInstructionList();
 
     int location = insns.indexOf(this);
     if (location < 0) // sanity check, should never happen
       throw new RuntimeException("Instruction not part of its DexCode");
 
     if (location + 1 < insns.size())
       return insns.get(location + 1);
     else
       return null;
   }
 
   // CONTROL FLOW GRAPHS
   //
   // The following methods represent default settings for code elements
   // and should be overridden in classes that need to be treated differently
 
   public boolean cfgStartsBasicBlock() {
     return false;
   }
 
   public boolean cfgEndsBasicBlock() {
     return false;
   }
 
   public boolean cfgExitsMethod() {
     return false;
   }
 
   public Set<DexCodeElement> cfgGetSuccessors() {
     val set = new HashSet<DexCodeElement>();
 
     val next = this.getNextCodeElement();
     if (next != null)
       set.add(next);
 
     return set;
   }
 
   // LIVE VARIABLE ANALYSIS
 
   public Set<DexRegister> lvaDefinedRegisters() {
     return Collections.emptySet();
   }
 
   public Set<DexRegister> lvaReferencedRegisters() {
     return Collections.emptySet();
   }
 
   public final Set<DexRegister> lvaUsedRegisters() {
     val set = new HashSet<DexRegister>();
     set.addAll(lvaDefinedRegisters());
     set.addAll(lvaReferencedRegisters());
     return set;
   }
 
   // GRAPH COLORING
 
   public static class GcRangeConstraint extends Pair<DexRegister, ColorRange> {
     public GcRangeConstraint(DexRegister valA, ColorRange valB) {
       super(valA, valB);
     }
   }
 
   public static class GcFollowConstraint extends Pair<DexRegister, DexRegister> {
     public GcFollowConstraint(DexRegister valA, DexRegister valB) {
       super(valA, valB);
     }
   }
 
   public Set<GcRangeConstraint> gcRangeConstraints() {
     return Collections.emptySet();
   }
 
   public Set<GcFollowConstraint> gcFollowConstraints() {
     return Collections.emptySet();
   }
 
   public final Map<DexCodeElement, List<DexCodeElement>> gcAddTemporaries(List<DexRegister> regList) {
     val tempMapping = new HashMap<DexRegister, DexRegister>();
     val methodCode = getMethodCode();
     val referencedRegs = lvaReferencedRegisters();
     val definedRegs = lvaDefinedRegisters();
     val newElem = new LinkedList<DexCodeElement>();
 
     for (val usedReg : lvaUsedRegisters())
       if (regList.contains(usedReg))
         tempMapping.put(usedReg, new DexRegister());
       else
         tempMapping.put(usedReg, usedReg);
 
     for (int i = 0; i < regList.size(); ++i) {
       val spilledReg = regList.get(i);
       if (referencedRegs.contains(spilledReg)) {
         val spilledRegType = gcReferencedRegisterType(spilledReg);
         switch (spilledRegType) {
         case Object:
           newElem.add(new DexInstruction_Move(methodCode, tempMapping.get(spilledReg), spilledReg, true));
           break;
         case PrimitiveSingle:
           newElem.add(new DexInstruction_Move(methodCode, tempMapping.get(spilledReg), spilledReg, false));
           break;
         case PrimitiveWide_High:
           val nextReg = regList.get(i + 1);
           if (gcReferencedRegisterType(nextReg) != gcRegType.PrimitiveWide_Low)
             throw new RuntimeException("Register type inconsistency");
           newElem.add(new DexInstruction_MoveWide(methodCode, tempMapping.get(spilledReg), tempMapping.get(nextReg), spilledReg, nextReg));
           break;
         default:
           break;
         }
       }
     }
 
     val insnReplacement = gcReplaceWithTemporaries(tempMapping);
     newElem.add(insnReplacement);
 
     for (int i = 0; i < regList.size(); ++i) {
       val spilledReg = regList.get(i);
       if (definedRegs.contains(spilledReg)) {
         val spilledRegType = gcDefinedRegisterType(spilledReg);
         switch (spilledRegType) {
         case Object:
           newElem.add(new DexInstruction_Move(methodCode, spilledReg, tempMapping.get(spilledReg), true));
           break;
         case PrimitiveSingle:
           newElem.add(new DexInstruction_Move(methodCode, spilledReg, tempMapping.get(spilledReg), false));
           break;
         case PrimitiveWide_High:
           val nextReg = regList.get(i + 1);
           if (gcDefinedRegisterType(nextReg) != gcRegType.PrimitiveWide_Low)
             throw new RuntimeException("Register type inconsistency");
           newElem.add(new DexInstruction_MoveWide(methodCode, spilledReg, nextReg, tempMapping.get(spilledReg), tempMapping.get(nextReg)));
           break;
         default:
           break;
         }
       }
     }
 
     val replacementMapping = new HashMap<DexCodeElement, List<DexCodeElement>>();
     replacementMapping.put(this, newElem);
 
     if (this instanceof DexInstruction_Switch) {
       val thisSwitch = (DexInstruction_Switch) this;
      val switchTableReplacement = thisSwitch.gcReplaceSwitchTableParentReference((DexInstruction_Switch) insnReplacement);
      replacementMapping.put(switchTableReplacement.getValA(), Arrays.asList( switchTableReplacement.getValB() ));
     }
 
     return replacementMapping;
   }
 
   protected abstract DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping);
 
   protected enum gcRegType {
     Object,
     PrimitiveSingle,
     PrimitiveWide_High,
     PrimitiveWide_Low
   }
 
   protected gcRegType gcReferencedRegisterType(DexRegister reg) {
     throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have referenced register type information");
   }
 
   protected gcRegType gcDefinedRegisterType(DexRegister reg) {
     throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have defined register type information");
   }
 
   // UTILS
 
   @SafeVarargs
   protected final <T> Set<T> createSet(T ... members) {
     return new HashSet<T>(Arrays.asList(members));
   }
 
   @SafeVarargs
   protected final <T> List<T> createList(T ... elements) {
     return Arrays.asList(elements);
   }
 
   public Dex getParentFile() {
     return methodCode.getParentFile();
   }
 
   public DexClass getParentClass() {
     return methodCode.getParentClass();
   }
 
   public DexMethodWithCode getParentMethod() {
     return methodCode.getParentMethod();
   }
 }
