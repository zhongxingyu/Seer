 package cs444.codegen.x86.tiles;
 
 import cs444.codegen.Platform;
 import cs444.codegen.instructions.x86.Xor;
 import cs444.codegen.instructions.x86.bases.X86Instruction;
 import cs444.codegen.tiles.ITile;
 import cs444.codegen.tiles.InstructionsAndTiming;
 import cs444.codegen.tiles.TileSet;
 import cs444.codegen.x86.Immediate;
 import cs444.codegen.x86.Register;
 import cs444.codegen.x86.X86SizeHelper;
 import cs444.parser.symbols.ast.expressions.NotOpExprSymbol;
 
 public class NotTile implements ITile<X86Instruction, NotOpExprSymbol>{
     public static void init(){
         new NotTile();
     }
 
     private NotTile(){
         TileSet.<X86Instruction>getOrMake(X86Instruction.class).nots.add(this);
     }
 
     @Override
     public boolean fits(final NotOpExprSymbol op) {
         return true;
     }
 
     @Override
    public InstructionsAndTiming<X86Instruction> generate(final NotOpExprSymbol symbol, final Platform<X86Instruction> platform) {
         final InstructionsAndTiming<X86Instruction> instructions = new InstructionsAndTiming<X86Instruction>();
         final X86SizeHelper sizeHelper = (X86SizeHelper) platform.getSizeHelper();
         instructions.add(new Xor(Register.ACCUMULATOR, Immediate.TRUE, sizeHelper));
         return instructions;
     }
 
 }
