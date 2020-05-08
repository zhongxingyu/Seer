 package cs444.codegen.x86.tiles;
 
 import cs444.codegen.Platform;
 import cs444.codegen.instructions.x86.Neg;
 import cs444.codegen.instructions.x86.bases.X86Instruction;
 import cs444.codegen.tiles.ITile;
 import cs444.codegen.tiles.InstructionsAndTiming;
 import cs444.codegen.tiles.TileSet;
 import cs444.codegen.x86.Register;
 import cs444.codegen.x86.X86SizeHelper;
 import cs444.parser.symbols.ast.expressions.NegOpExprSymbol;
 
 public class NegTile implements ITile<X86Instruction, NegOpExprSymbol>{
     public static void init(){
         new NegTile();
     }
 
     private NegTile(){
         TileSet.<X86Instruction>getOrMake(X86Instruction.class).negs.add(this);
     }
 
     @Override
     public boolean fits(final NegOpExprSymbol op) {
         return true;
     }
 
     @Override
    public InstructionsAndTiming<X86Instruction> generate(final NegOpExprSymbol negSymbol, final Platform<X86Instruction> platform) {
         final InstructionsAndTiming<X86Instruction> instructions = new InstructionsAndTiming<X86Instruction>();
         final X86SizeHelper sizeHelper = (X86SizeHelper) platform.getSizeHelper();
        instructions.addAll(platform.getBest(negSymbol.children.get(0)));
         instructions.add(new Neg(Register.ACCUMULATOR, sizeHelper));
         return instructions;
     }
 
 }
