 package cs444.codegen.x86.x86_32.tiles;
 
 import cs444.codegen.Platform;
 import cs444.codegen.SizeHelper;
 import cs444.codegen.tiles.ITile;
 import cs444.codegen.tiles.InstructionsAndTiming;
 import cs444.codegen.tiles.TileSet;
 import cs444.codegen.x86.InstructionArg.Size;
 import cs444.codegen.x86.Register;
 import cs444.codegen.x86.instructions.bases.X86Instruction;
 import cs444.codegen.x86.tiles.helpers.X86TileHelper;
 import cs444.parser.symbols.ast.TypeSymbol;
 import cs444.parser.symbols.ast.Typeable;
 import cs444.parser.symbols.ast.expressions.CastExpressionSymbol;
 
 public class CastFromLongTile implements ITile<X86Instruction, Size,CastExpressionSymbol> {
     public static void init(){
         new CastFromLongTile();
     }
 
     private CastFromLongTile(){
         TileSet.<X86Instruction, Size>getOrMake(X86Instruction.class).casts.add(this);
     }
 
     @Override
     public boolean fits(final CastExpressionSymbol symbol, final Platform<X86Instruction, Size> platform) {
         final SizeHelper<X86Instruction, Size> sizeHelper = platform.getSizeHelper();
         final Typeable typeable = (Typeable) symbol.getOperandExpression();
        return sizeHelper.getDefaultStackSize()  > sizeHelper.getByteSizeOfType(typeable.getType().getTypeDclNode().fullName);
     }
 
     @Override
     public InstructionsAndTiming<X86Instruction> generate(final CastExpressionSymbol symbol,
             final Platform<X86Instruction, Size> platform) {
 
         final InstructionsAndTiming<X86Instruction> instructions = new InstructionsAndTiming<X86Instruction>();
         final TypeSymbol type = symbol.getType();
         final SizeHelper<X86Instruction, Size> sizeHelper = platform.getSizeHelper();
         final Size curSize = sizeHelper.getDefaultSize();
 
         instructions.addAll(platform.getBest(symbol.getOperandExpression()));
         X86TileHelper.genMov(curSize, Register.ACCUMULATOR, "cast to " + type.value, type, sizeHelper, instructions);
         return instructions;
     }
 }
