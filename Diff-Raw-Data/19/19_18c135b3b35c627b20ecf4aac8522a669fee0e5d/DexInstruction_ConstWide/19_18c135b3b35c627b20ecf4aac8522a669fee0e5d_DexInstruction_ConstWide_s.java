 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Opcode;
 import org.jf.dexlib.Code.Format.Instruction21h;
 import org.jf.dexlib.Code.Format.Instruction21s;
 import org.jf.dexlib.Code.Format.Instruction31i;
 import org.jf.dexlib.Code.Format.Instruction51l;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 
 import lombok.Getter;
 import lombok.val;
 
 public class DexInstruction_ConstWide extends DexInstruction {
 
   @Getter private final DexRegister regTo1;
   @Getter private final DexRegister regTo2;
   @Getter private final long value;
 
   // CAREFUL: if Value is 64-bit and bottom 48-bits are zero,
   //          turn it into const-wide/high16 instruction
 
   public DexInstruction_ConstWide(DexCode methodCode, DexRegister to1, DexRegister to2, long value) {
     super(methodCode);
 
     this.regTo1 = to1;
     this.regTo2 = to2;
     this.value = value;
   }
 
   public DexInstruction_ConstWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
     super(methodCode);
 
     if (insn instanceof Instruction21s && insn.opcode == Opcode.CONST_WIDE_16) {
 
       val insnConstWide16 = (Instruction21s) insn;
       regTo1 = parsingState.getRegister(insnConstWide16.getRegisterA());
       regTo2 = parsingState.getRegister(insnConstWide16.getRegisterA() + 1);
       value = insnConstWide16.getLiteral();
 
     } else if (insn instanceof Instruction31i && insn.opcode == Opcode.CONST_WIDE_32) {
 
       val insnConstWide32 = (Instruction31i) insn;
       regTo1 = parsingState.getRegister(insnConstWide32.getRegisterA());
       regTo2 = parsingState.getRegister(insnConstWide32.getRegisterA() + 1);
       value = insnConstWide32.getLiteral();
 
     } else if (insn instanceof Instruction51l && insn.opcode == Opcode.CONST_WIDE) {
 
       val insnConstWide = (Instruction51l) insn;
       regTo1 = parsingState.getRegister(insnConstWide.getRegisterA());
       regTo2 = parsingState.getRegister(insnConstWide.getRegisterA() + 1);
       value = insnConstWide.getLiteral();
 
     } else if (insn instanceof Instruction21h && insn.opcode == Opcode.CONST_WIDE_HIGH16) {
 
       val insnConstHigh16 = (Instruction21h) insn;
       regTo1 = parsingState.getRegister(insnConstHigh16.getRegisterA());
       regTo2 = parsingState.getRegister(insnConstHigh16.getRegisterA() + 1);
       value = insnConstHigh16.getLiteral() << 48;
 
     } else
       throw FORMAT_EXCEPTION;
   }
 
   @Override
   public String getOriginalAssembly() {
     return "const-wide v" + regTo1.getOriginalIndexString() + "|v" + regTo2.getOriginalIndexString() + ", #" + value;
   }
 
   @Override
   protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
     return new DexInstruction_ConstWide(getMethodCode(), mapping.get(regTo1), mapping.get(regTo2), value);
   }
 
   @Override
   public DexCodeElement[] instrument(DexCode_InstrumentationState state) {
     return new DexCodeElement[] {
              this,
              new DexInstruction_ConstWide(
                getMethodCode(),
                state.getTaintRegister(regTo1),
                state.getTaintRegister(regTo2),
                0)
            };
   }
 
   @Override
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     val regAlloc = state.getRegisterAllocation();
     int rTo1 = regAlloc.get(regTo1);
     int rTo2 = regAlloc.get(regTo2);
 
     if (!formWideRegister(rTo1, rTo2))
       return throwWideRegistersExpected();
 
     if (!fitsIntoBits_Unsigned(rTo1, rTo1))
       return throwNoSuitableFormatFound();
 
     if (fitsIntoBits_Signed(value, 16))
       return new Instruction[] { new Instruction21s(Opcode.CONST_WIDE_16, (short) rTo1, (short) value) };
     else if (fitsIntoBits_Signed(value, 32))
       return new Instruction[] { new Instruction31i(Opcode.CONST_WIDE_32, (short) rTo1, (int) value) };
    else if (fitsIntoHighBits_Signed(value, 64, 48))
       return new Instruction[] { new Instruction21h(Opcode.CONST_WIDE_HIGH16, (short) rTo1, (short) value) };
     else
       return new Instruction[] { new Instruction51l(Opcode.CONST_WIDE, (short) rTo1, value) };
   }
 
   @Override
   public Set<DexRegister> lvaDefinedRegisters() {
     return createSet(regTo1, regTo2);
   }
 
   @Override
   public Set<GcRangeConstraint> gcRangeConstraints() {
     return createSet(new GcRangeConstraint(regTo1, ColorRange.RANGE_8BIT));
   }
 
   @Override
   public Set<GcFollowConstraint> gcFollowConstraints() {
     return createSet(new GcFollowConstraint(regTo1, regTo2));
   }
 }
