 package uk.ac.cam.db538.dexter.dex.code.insn;
 
import java.util.Collections;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.Code.Instruction;
 import org.jf.dexlib.Code.Opcode;
 import org.jf.dexlib.Code.Format.Instruction11x;
 
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 
 public class DexInstruction_Throw extends DexInstruction {
 
   @Getter private final DexRegister regFrom;
 
   public DexInstruction_Throw(DexCode methodCode, DexRegister from) {
     super(methodCode);
     regFrom = from;
   }
 
   public DexInstruction_Throw(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
     super(methodCode);
 
     if (insn instanceof Instruction11x && insn.opcode == Opcode.THROW) {
 
       val insnThrow = (Instruction11x) insn;
       regFrom = parsingState.getRegister(insnThrow.getRegisterA());
 
     } else
       throw FORMAT_EXCEPTION;
   }
 
   @Override
   public String getOriginalAssembly() {
     return "throw " + regFrom.getOriginalIndexString();
   }
 
   @Override
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     int rFrom = state.getRegisterAllocation().get(regFrom);
 
     if (fitsIntoBits_Unsigned(rFrom, 8))
       return new Instruction[] {
                new Instruction11x(Opcode.THROW, (short) rFrom)
              };
     else
       return throwNoSuitableFormatFound();
   }
 
   @Override
   public boolean cfgEndsBasicBlock() {
     return true;
   }
  
   @Override
   public Set<DexRegister> lvaReferencedRegisters() {
     return createSet(regFrom);
   }
 
   @Override
   public void instrument(DexCode_InstrumentationState state) { }
 
   @Override
   public void accept(DexInstructionVisitor visitor) {
 	visitor.visit(this);
   }
 
   @Override
   protected DexClassType[] throwsExceptions() {
     return new DexClassType[] { DexClassType.parse("Ljava/lang/Throwable;", getParentFile().getParsingCache()) };
   }

  @Override
  protected Set<DexCodeElement> cfgJumpTargets() {
	  return Collections.emptySet();
  }
 }
