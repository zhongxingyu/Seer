 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Opcode;
 import org.jf.dexlib.Code.Format.Instruction11x;
 
 import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 
 public class DexInstruction_Monitor extends DexInstruction {
 
   @Getter private final DexRegister regMonitor;
   @Getter private final boolean enter;
 
   public DexInstruction_Monitor(DexCode methodCode, DexRegister reg, boolean entering) {
     super(methodCode);
 
     this.regMonitor = reg;
     this.enter = entering;
   }
 
   public DexInstruction_Monitor(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
     super(methodCode);
 
     if (insn instanceof Instruction11x &&
         (insn.opcode == Opcode.MONITOR_ENTER || insn.opcode == Opcode.MONITOR_EXIT)) {
 
       val insnMonitor = (Instruction11x) insn;
       regMonitor = parsingState.getRegister(insnMonitor.getRegisterA());
       enter = insn.opcode == Opcode.MONITOR_ENTER;
 
     } else
       throw FORMAT_EXCEPTION;
   }
 
   @Override
   public String getOriginalAssembly() {
     return "monitor-" + (enter ? "enter" : "exit") +
            " " + regMonitor.getOriginalIndexString();
   }
 
   @Override
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     int rObj = state.getRegisterAllocation().get(regMonitor);
 
     if (fitsIntoBits_Unsigned(rObj, 8))
       return new Instruction[] {
                new Instruction11x(enter ? Opcode.MONITOR_ENTER : Opcode.MONITOR_EXIT, (short) rObj)
              };
     else
       return throwNoSuitableFormatFound();
   }
 
   @Override
   public Set<DexRegister> lvaReferencedRegisters() {
     return createSet(regMonitor);
   }
 
   @Override
   public gcRegType gcReferencedRegisterType(DexRegister reg) {
     if (reg.equals(regMonitor))
       return gcRegType.Object;
     else
       return super.gcReferencedRegisterType(reg);
   }

   @Override
   public Set<GcRangeConstraint> gcRangeConstraints() {
     return createSet(new GcRangeConstraint(regMonitor, ColorRange.RANGE_8BIT));
   }
 
   @Override
   protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
     val newMonitor = (toRefs) ? mapping.get(regMonitor) : regMonitor;
     return new DexInstruction_Monitor(getMethodCode(), newMonitor, enter);
   }
 
   @Override
   public void instrument(DexCode_InstrumentationState state) { }
 
   @Override
   public void accept(DexInstructionVisitor visitor) {
 	visitor.visit(this);
   }
 }
