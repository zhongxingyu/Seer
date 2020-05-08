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
 
 public class DexInstruction_FillArray extends DexInstruction {
 
   @Getter private final DexRegister regArray;
   @Getter private final DexLabel arrayTable;
 
   public DexInstruction_FillArray(DexCode methodCode, DexRegister array, DexLabel arrayTable) {
     super(methodCode);
 
     this.regArray = array;
     this.arrayTable = arrayTable;
 
     setUp();
   }
 
   public DexInstruction_FillArray(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
     super(methodCode);
 
     if (insn instanceof Instruction31t && insn.opcode == Opcode.FILL_ARRAY_DATA) {
 
       val insnFillArrayData = (Instruction31t) insn;
       int dataTableOffset = insnFillArrayData.getTargetAddressOffset();
 
       this.regArray = parsingState.getRegister(insnFillArrayData.getRegisterA());
       this.arrayTable = parsingState.getLabel(dataTableOffset);
 
       parsingState.registerParentInstruction(this, dataTableOffset);
       setUp();
 
     } else
       throw FORMAT_EXCEPTION;
   }
 
   private void setUp() {
     this.arrayTable.setEvenAligned(true);
   }
 
   @Override
   public String getOriginalAssembly() {
     return "fill-array " + regArray.getOriginalIndexString() + ", L" + arrayTable.getOriginalAbsoluteOffset();
   }
 
   @Override
   protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
     return new DexInstruction_FillArray(getMethodCode(), mapping.get(regArray), arrayTable);
   }
 
   @Override
   public void instrument(DexCode_InstrumentationState state) { }
 
   @Override
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     int rArray = state.getRegisterAllocation().get(regArray);
     long offset = computeRelativeOffset(arrayTable, state);
 
     if (fitsIntoBits_Unsigned(rArray, 8) && fitsIntoBits_Signed(offset, 32))
       return new Instruction[] { new Instruction31t(Opcode.FILL_ARRAY_DATA, (short) rArray, (int) offset) };
     else
       return throwNoSuitableFormatFound();
   }
 
   @Override
   public boolean cfgEndsBasicBlock() {
     return true;
   }
 
   @Override
   public Set<DexCodeElement> cfgGetSuccessors() {
     return createSet((DexCodeElement) arrayTable);
   }
 
   @Override
   public Set<DexRegister> lvaReferencedRegisters() {
     return createSet(regArray);
   }
 
   @Override
   protected gcRegType gcReferencedRegisterType(DexRegister reg) {
     if (reg.equals(regArray))
       return gcRegType.Object;
     else
       return super.gcReferencedRegisterType(reg);
   }
 
   @Override
   public Set<GcRangeConstraint> gcRangeConstraints() {
     return createSet(new GcRangeConstraint(regArray, ColorRange.RANGE_8BIT));
   }
 }
