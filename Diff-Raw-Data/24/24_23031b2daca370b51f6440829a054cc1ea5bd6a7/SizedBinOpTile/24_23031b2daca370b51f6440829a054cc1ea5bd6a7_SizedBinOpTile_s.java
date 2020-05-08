 package cs444.codegen.x86.tiles.helpers;
 
 import cs444.codegen.Platform;
 import cs444.codegen.SizeHelper;
 import cs444.codegen.x86.InstructionArg.Size;
 import cs444.codegen.x86.instructions.bases.X86Instruction;
 import cs444.codegen.x86.instructions.factories.BinOpMaker;
 import cs444.parser.symbols.ast.expressions.BinOpExpr;
 
 public abstract class SizedBinOpTile<T extends BinOpExpr> extends BinOpTile<T>{
 
     protected SizedBinOpTile(final BinOpMaker maker) {
         super(maker);
     }
 
     @Override
    public final boolean fits(final T op, final Platform<X86Instruction, Size> platform) {
         final SizeHelper<X86Instruction, Size> sizeHelper = platform.getSizeHelper();
         return sizeHelper.getDefaultStackSize()  >= sizeHelper.getByteSizeOfType(op.getType().getTypeDclNode().fullName);
     }
 }
