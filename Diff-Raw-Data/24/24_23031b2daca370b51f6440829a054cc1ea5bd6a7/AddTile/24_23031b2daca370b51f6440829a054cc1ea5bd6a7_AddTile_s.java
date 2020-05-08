 package cs444.codegen.x86.tiles;
 
 import cs444.codegen.tiles.TileSet;
 import cs444.codegen.x86.InstructionArg.Size;
 import cs444.codegen.x86.instructions.bases.X86Instruction;
 import cs444.codegen.x86.instructions.factories.AddOpMaker;
 import cs444.codegen.x86.tiles.helpers.SizedBinOpTile;
 import cs444.parser.symbols.ast.expressions.AddExprSymbol;
 
 public class AddTile extends SizedBinOpTile<AddExprSymbol> {
     public static void init(){
         new AddTile();
     }
 
     private AddTile(){
         super(AddOpMaker.maker);
         TileSet.<X86Instruction, Size>getOrMake(X86Instruction.class).adds.add(this);
     }
 }
