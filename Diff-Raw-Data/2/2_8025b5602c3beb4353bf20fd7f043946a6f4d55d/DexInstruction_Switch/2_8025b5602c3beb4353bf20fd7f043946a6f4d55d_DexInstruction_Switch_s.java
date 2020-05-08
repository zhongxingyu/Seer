 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Opcode;
 import org.jf.dexlib.Code.Format.Instruction31t;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 
 public class DexInstruction_Switch extends DexInstruction {
 
   @Getter private final DexRegister regTest;
   @Getter private final DexLabel switchTable;
   @Getter private final boolean packed;
 
   public DexInstruction_Switch(DexCode methodCode, DexRegister test, DexLabel switchTable, boolean isPacked) {
     super(methodCode);
 
     this.regTest = test;
     this.switchTable = switchTable;
     this.packed = isPacked;
 
     setUp();
   }
 
   public DexInstruction_Switch(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
     super(methodCode);
 
     if (insn instanceof Instruction31t &&
         (insn.opcode == Opcode.PACKED_SWITCH || insn.opcode == Opcode.SPARSE_SWITCH)) {
 
       val insnSwitch = (Instruction31t) insn;
       int dataTableOffset = insnSwitch.getTargetAddressOffset();
 
       this.regTest = parsingState.getRegister(insnSwitch.getRegisterA());
       this.switchTable = parsingState.getLabel(dataTableOffset);
       this.packed = (insn.opcode == Opcode.PACKED_SWITCH);
 
       parsingState.registerParentInstruction(this, dataTableOffset);
       setUp();
 
     } else
       throw FORMAT_EXCEPTION;
   }
 
   private void setUp() {
     this.switchTable.setEvenAligned(true);
   }
 
   @Override
   public String getOriginalAssembly() {
     return (packed ? "packed" : "sparse") + "-switch " + regTest.getOriginalIndexString() + ", L" + switchTable.getOriginalAbsoluteOffset();
   }
 
   @Override
   protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
     return new DexInstruction_Switch(getMethodCode(), mapping.get(regTest), switchTable, packed);
   }
 
   @Override
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     int rTest = state.getRegisterAllocation().get(regTest);
     long offset = computeRelativeOffset(switchTable, state);
 
     if ((!fitsIntoBits_Unsigned(rTest, 8)) || (!fitsIntoBits_Signed(offset, 32)))
       return throwNoSuitableFormatFound();
 
     if (packed)
       return new Instruction[] { new Instruction31t(Opcode.PACKED_SWITCH, (short) rTest, (int) offset) };
     else
       return new Instruction[] { new Instruction31t(Opcode.SPARSE_SWITCH, (short) rTest, (int) offset) };
   }
 
   @Override
   public boolean cfgEndsBasicBlock() {
     return true;
   }
 
   @Override
   public Set<DexCodeElement> cfgGetSuccessors() {
    return createSet((DexCodeElement) switchTable);
   }
 
   @Override
   public Set<DexRegister> lvaReferencedRegisters() {
     return createSet(regTest);
   }
 
   @Override
   public Set<GcRangeConstraint> gcRangeConstraints() {
     return createSet(new GcRangeConstraint(regTest, ColorRange.RANGE_8BIT));
   }
 
   @Override
   public void instrument(DexCode_InstrumentationState state) { }
 }
