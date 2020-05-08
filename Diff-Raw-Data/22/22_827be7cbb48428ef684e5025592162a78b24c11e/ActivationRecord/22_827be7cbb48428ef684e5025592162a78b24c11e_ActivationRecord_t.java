 package mips;
 
 import java.util.Map;
 import java.util.HashMap;
 
 import ast.FunctionDefinition;
 
 import crux.Symbol;
 import types.*;
 
 /**
  * Models an activation record.
  */
 public class ActivationRecord {
     /* Size of the book keeping variables on the very top of the stack frame. */
     private static int fixedFrameSize = 2*4;
 
     /* The function defintion for the function using this activation record. */
     private ast.FunctionDefinition func;
 
     /* Reference to the parent record. */
     private ActivationRecord parent;
 
     /* Size of the local variables segment in the stack.*/
     private int stackSize;
 
     /* Maps a local symbol to its offset (negative) from the frame pointer.  */
     private Map<Symbol, Integer> locals;
 
     /* Maps an argument symbol to its offset (positive) from the frame pointer. */
     private Map<Symbol, Integer> arguments;
 
     /**
      * Get a new global activation frame.
      * @return A new activation record.
      */
     public static ActivationRecord newGlobalFrame() {
         return new GlobalFrame();
     }
 
     /**
      * Get the number of bytes for a given type.
      * @param type The type to check. Must be IntType, FloatType or ArrayType.
      * @return The size in bytes.
      * @throws RuntimeException if the type constraints does not hold.
      */
     //protected static int numBytes(Type type) {
         //if (type instanceof IntType) {
             //return 4;
         //} else if (type instanceof FloatType) {
             //return 4;
         //} else if (type instanceof ArrayType) {
             //ArrayType aType = (ArrayType)type;
             //return aType.extent() * numBytes(aType.base());
          //} else if (type instanceof BoolType) {
              //return 4;
         //} else {
             //throw new RuntimeException("No size known for " + type);
         //}
     //}
 
     /**
      * Construct a new activation record with default values on members.
      */
     protected ActivationRecord() {
         this.func = null;
         this.parent = null;
         this.stackSize = 0;
         this.locals = null;
         this.arguments = null;
     }
 
     /**
      * Construct a new activation record with a given function defintion and parent record.
      * @param fd A function defintion.
      * @param parent The parent record.
      */
     public ActivationRecord(ast.FunctionDefinition fd, ActivationRecord parent) {
         this.func = fd;
         this.parent = parent;
         this.stackSize = 0;
         this.locals = new HashMap<Symbol, Integer>();
 
         // Map this function's parameters.
         // NOTE original code added args in the reverse order.
         this.arguments = new HashMap<Symbol, Integer>();
 		int fpOffset = 0;
 		for (Symbol symbol : fd.arguments()) {
 			Type type = symbol.type();
			arguments.put(symbol, fpOffset);
 			fpOffset += type.numBytes();
 		}
     }
 
 	/**
 	 * Get the name of the function
 	 * @return The name.
 	 */
     public String name() {
         return func.symbol().name();
     }
 
 	/**
 	 * Get the parent record.
 	 * @return The parent record.
 	 */
     public ActivationRecord parent() {
         return parent;
     }
 
 	/**
 	 * Get the size of the stack used by function local variables.
 	 * @return The stack size.
 	 */
     public int stackSize() {
         return stackSize;
     }
 
 	/**
 	 * Add a variable declaration to a program.
 	 * @param prog The program to use.
 	 * @param var The variable declaration to add.
 	 */
     public void add(Program prog, ast.VariableDeclaration var) {
     	Symbol symbol = var.symbol();
     	int varSize = symbol.type().numBytes();
 		stackSize += varSize;
 		locals.put(symbol, -stackSize);
 		//prog.appendInstruction("subu $sp, $sp, " + varSize);
     }
 
 	/**
 	 * Add an array declaration to a program.
 	 * @param prog The program to use.
 	 * @param array The array declaration to add.
 	 */
     public void add(Program prog, ast.ArrayDeclaration array) {
    	// TODO
         throw new RuntimeException("implement adding array to local function space");
     }
 
 	/**
 	 * Get the address of a local or paramter symbol. // TODO what if symbol is in global space, shold we not ask parent?
 	 * @param prog The program to get from.
 	 * @param reg The register where the address is to be stored.
 	 * @param sym the symbol to get address of.
 	 */
     public void getAddress(Program prog, String reg, Symbol sym) {
     	if (locals.containsKey(sym)) {
     		int offset = locals.get(sym);
         	prog.debugComment("Calculating address to var from framepointer to symbol " + sym.name());
        	prog.appendInstruction("addi " + reg + ", $fp, " + (offset - 8));
     	} else if (arguments.containsKey(sym)) {
         	prog.debugComment("Calculating address to funcargumnet from framepointer to symbol " + sym.name());
     		int offset = arguments.get(sym);
         	prog.appendInstruction("addi " + reg + ", $fp, " + offset);
     	} else if (parent != null) {
         	prog.debugComment("Consulting parent scope for address to symbol" + sym.name());
 			parent.getAddress(prog, reg, sym);
     	} else {
         	throw new RuntimeException("This error should not be possible here, can not find symbol you're looking for.");
     	}
     }
 }
 
 /**
  * A frame that is in the global space.
  */
 class GlobalFrame extends ActivationRecord {
 
     //public GlobalFrame() {
     //}
 
 	/**
 	 * Generate a unique name in the crux namespace for data to not conflict with MIPS built ins.
 	 * @param name The input name.
 	 * @return A mangled version of input.
 	 */
     private String mangleDataname(String name) {
         return "cruxdata." + name;
     }
 
     @Override
     public void add(Program prog, ast.VariableDeclaration var) {
 		Symbol symbol = var.symbol();
 		prog.appendData(mangleDataname(symbol.name()) + ": .space " + symbol.type().numBytes());
     }    
 
     @Override
     public void add(Program prog, ast.ArrayDeclaration array) {
     	Symbol symbol = array.symbol();
         prog.appendData(mangleDataname(symbol.name()) +  ": .space " + symbol.type().numBytes());
     }
 
     @Override
     public void getAddress(Program prog, String reg, Symbol sym) {
         prog.appendInstruction("la " + reg + ", " + mangleDataname(sym.name()));
     }
 }
