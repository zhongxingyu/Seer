 package uk.ac.cam.db538.dexter.dex.code.insn;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 import lombok.val;
 
 import org.jf.dexlib.Code.Instruction;
 
 import uk.ac.cam.db538.dexter.dex.code.DexCode;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
 import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
 
 public abstract class DexInstruction extends DexCodeElement {
 
   public DexInstruction(DexCode methodCode) {
     super(methodCode);
   }
 
   // PARSING
 
   protected static final InstructionParsingException FORMAT_EXCEPTION = new InstructionParsingException("Unknown instruction format or opcode");
 
   // INSTRUCTION INSTRUMENTATION
 
   public DexCodeElement[] instrument(DexCode_InstrumentationState state) {
     throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have instrumentation implemented");
   }
 
   // ASSEMBLING
 
   public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
     throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have assembling implemented");
   }
 
   public DexCodeElement[] fixLongJump() {
     throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have long jump fix implemented");
   }
 
   protected final Instruction[] throwCannotAssembleException(String reason) {
     throw new InstructionAssemblyException("Instruction " + this.getClass().getSimpleName() + " couldn't be assembled (" + reason + ")");
   }
 
   protected final Instruction[] throwNoSuitableFormatFound() {
     return throwCannotAssembleException("No suitable format of instruction found");
   }
 
   protected final Instruction[] throwWideRegistersExpected() {
     throw new InstructionAssemblyException("Wide registers badly aligned with instruction: " + getOriginalAssembly());
   }
 
   // THROWING INSTRUCTIONS
 
   protected final boolean throwingInsn_CanExitMethod() {
     val code = this.getMethodCode();
 
     for (val tryBlockEnd : code.getTryBlocks()) {
       val tryBlockStart = tryBlockEnd.getBlockStart();
 
       // check that the instruction is in this try block
       if (code.isBetween(tryBlockStart, tryBlockEnd, this)) {
 
         // if the block has CatchAll handler, it can't exit the method
         if (tryBlockStart.getCatchAllHandler() != null)
           return false;
 
         // if there is a catch block catching Throwable, it can't exit method either
         for (val catchBlock : tryBlockStart.getCatchHandlers()) {
           if (catchBlock.getExceptionType().getDescriptor().equals("Ljava/lang/Throwable;"))
             return false;
         }
       }
     }
 
     return true;
   }
 
   protected final Set<DexCodeElement> throwingInsn_CatchHandlers() {
     val set = new HashSet<DexCodeElement>();
 
     val code = this.getMethodCode();
 
     for (val tryBlockEnd : code.getTryBlocks()) {
       val tryBlockStart = tryBlockEnd.getBlockStart();
 
       // check that the instruction is in this try block
       if (code.isBetween(tryBlockStart, tryBlockEnd, this)) {
 
         // if the block has CatchAll handler, it can jump to it
         val catchAllHandler = tryBlockStart.getCatchAllHandler();
         if (catchAllHandler != null)
           set.add(catchAllHandler);
 
         // similarly, add all catch blocks as possible successors
         for (val catchBlock : tryBlockStart.getCatchHandlers())
           set.add(catchBlock);
       }
     }
 
     return set;
   }
 
   static boolean fitsIntoBits_Signed(long value, int bits) {
     assert bits > 0;
     assert bits <= 64;
 
     long upperBound = 1L << bits - 1;
     return (value < upperBound) && (value >= -upperBound);
   }
 
   static boolean fitsIntoBits_Unsigned(long value, int bits) {
     assert bits > 0;
     assert bits <= 64;
 
     long mask = 0L;
     for (int i = bits; i < 64; ++i)
       mask |= 1L << i;
     return (value & mask) == 0L;
   }
 
  static boolean fitsIntoHighBits_Signed(long value, int bitsUsedWidth, int bitsBottomEmpty) {
    assert bitsUsedWidth > 0;
    assert bitsUsedWidth <= 64;
     assert bitsBottomEmpty > 0;
     assert bitsBottomEmpty <= 64;
    assert bitsUsedWidth + bitsBottomEmpty <= 64;
 
     // check that the bottom bits are zero
     // and then that it fits in the sum of bit arguments
     long mask = 0L;
     for (int i = 0; i < bitsBottomEmpty; ++i)
       mask |= 1L << i;
     return ((value & mask) == 0L) &&
           fitsIntoBits_Signed(value, bitsBottomEmpty + bitsUsedWidth);
   }
 
   static boolean formWideRegister(int reg1, int reg2) {
     return (reg1 + 1 == reg2);
   }
 
   long computeRelativeOffset(DexLabel target, DexCode_AssemblingState state) {
     long offsetThis = state.getElementOffsets().get(this);
     long offsetTarget = state.getElementOffsets().get(target);
     long offset = offsetTarget - offsetThis;
 
     if (offset == 0)
       throw new InstructionAssemblyException("Cannot have zero offset");
 
     return offset;
   }
 }
