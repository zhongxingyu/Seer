 package mips;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Vector;
 
 /**
  * Models a generated MIPS program.
  */
 public class Program {
 	/* The code segment. */
     private Vector<String> codeSegment;
 
     /* The data segment. */
     private Vector<String> dataSegment;
 
     /* Number of lables used. */
     private int labelCounter;
 
     /**
      * Construct a new program.
      */
     public Program() {
         labelCounter = -1;
         codeSegment = new Vector<String>();
         dataSegment = new Vector<String>();
     }
 
     /**
      * Make a new unique label that can be used e.g. for conditional jumps.
      * @return A unique label.
      */
     public String newLabel() {
         return "label." + ++labelCounter;
     }
 
     /**
      * Insert an instruction into the code segment.
      * @param instr The instruction to add.
      * @return the position of the instruction in the stream.
      */
     public int appendInstruction(String instr) {
         codeSegment.add(instr);
         return codeSegment.size() - 1;
     }
 
     /**
      * Replaces the instruction at position pos with a another instruction.
      * @param pos The position in the code segment.
      * @param instr The instruction to replace with.
      */
     public void replaceInstruction(int pos, String instr) {
         codeSegment.set(pos, instr);
     }
 
     /**
      * Inserts an instruction at position pos.
      * All instructions after pos are shifted down.
      * @param pos The position in the code segment.
      * @param instr The instruction to insert.
      */
     public void insertInstruction(int pos, String instr) {
      	codeSegment.add(pos, instr);
     }
 
     /**
      * Append item to data segment.
      * @param data Item to append.
      */
     public void appendData(String data) {
     	dataSegment.add(data);
     }
 
     /**
      * Push an integer register on the stack.
      * @param reg The register to push value from.
      */
     public void pushInt(String reg) {
         debugComment("Pushing int register (" + reg + ") to stack.");
         appendInstruction("subu $sp, $sp, 4");
         appendInstruction("sw " + reg + ", 0($sp)");
     }
 
     /**
      * Push a single precision floating point register on the stack.
      * @param reg The register to push value from.
      */
     public void pushFloat(String reg) {
         debugComment("Pushing float register (" + reg + ") to stack.");
         appendInstruction("subu $sp, $sp, 4");
         appendInstruction("swc1 " + reg + ", 0($sp)");
     }
 
     /**
      * Pop an integer from the stack into register reg.
      * @param Register to pop value to.
      */
     public void popInt(String reg) {
         debugComment("Popping int to reg " + reg);
     	appendInstruction("lw " + reg + ", 0($sp)");
     	appendInstruction("addiu $sp, $sp, 4");
     }
 
     /**
      * Pop a floating point value from the stack into register reg.
      * @param Register to pop value to.
      */
     public void popFloat(String reg) {
         debugComment("Popping float to reg " + reg);
     	appendInstruction("lwc1 " + reg + ", 0($sp)");
     	appendInstruction("addiu $sp, $sp, 4");
     }
 
     /**
      * Insert a function prologue at position pos.
      *
      * When we evaluate a function definiton we don't know the size of the 
      * local variables before we have visited the function body. Therefore we 
      * have to insert the prologue after we have that information.
      * @param pos The position to insert prologue at.
      * @param frameSize The size of the local variables in the function body.
      * @param isMain If this is a prologue for the main function.
      */
     public void insertPrologue(int pos, int frameSize, boolean isMain) {
     	ArrayList<String> prologue = new ArrayList<String>();
     	prologue.add("# Function (Callee) Prologue.");
     	prologue.add("# Bookkeeping.");
    	if (isMain) {
    		prologue.add("addi $fp, $sp, 0");
    	} else {
     		prologue.add("subu $sp, $sp, 8");
     		prologue.add("sw $fp, 0($sp)");
     		prologue.add("sw $ra, 4($sp)");
     		prologue.add("addi $fp, $sp, 8");
    	}
     	if (frameSize > 0 ) {
     		prologue.add("# Reserve space (" +  frameSize + "b) for function local vars.");
     		prologue.add("subu $sp, $sp, " + frameSize);
     	}
     	codeSegment.addAll(pos, prologue);
     }
 
     /**
      * Append a function epilogue.
      * @param frameSize The size of the function local section.
      * @param isMain If this is a prologue for the main function.
      */
     public void appendEpilogue(int frameSize, boolean isMain) {
     	debugComment("Function (Callee) Epilogue.");
     	if (frameSize > 0) {
     		debugComment("Erasing function local variables.");
     		appendInstruction("addu $sp, $sp, " + frameSize);
     	}
     	if (isMain) {
     		appendExitSequence();
     	} else {
     		debugComment("Restore caller's state.");
     		appendInstruction("lw $ra, 4($sp)");
     		appendInstruction("lw $fp, 0($sp)");
     		appendInstruction("addu $sp, $sp, 8" );
 
     		appendInstruction("jr $ra");
     	}
     }
 
     /**
      * Insert code that terminates the program.
      */
     public void appendExitSequence() {
     	codeSegment.add("li $v0, 10");
     	codeSegment.add("syscall");
     }
 
     /**
      * Print the program to the provided stream.
      * @param s The stream to print to.
      */
     public void print(PrintStream s) {
     	s.println(".data                         # BEGIN Data Segment");
     	for (String data : dataSegment) {
     		s.println(data);
     	}
     	s.println("data.newline:      .asciiz       \"\\n\"");
     	s.println("data.floatquery:   .asciiz       \"float?\"");
     	s.println("data.intquery:     .asciiz       \"int?\"");
     	s.println("data.trueString:   .asciiz       \"true\"");
     	s.println("data.falseString:  .asciiz       \"false\"");
     	s.println("                              # END Data Segment");
 
     	s.println(".text                         # BEGIN Code Segment");
 		// provide the built-in functions
 		funcPrintBool(s);
 		funcPrintFloat(s);
 		funcPrintInt(s);
 		funcPrintln(s);
 		funcReadFloat(s);
 		funcReadInt(s);
 
 		s.println(".text                         # BEGIN Crux Program");
 		// write out the crux program
 		for (String code : codeSegment) {
     		s.println(code);
 		}
     	s.println("                              # END Code Segment");
     }
 
 
 	// Syscall documentation: http://courses.missouristate.edu/kenvollmar/mars/help/syscallhelp.html
 
     /**
      * Prints the current stack value, assuming it's an int.
      * @param s The stream to print to.
      */
     public void funcPrintInt(PrintStream s) {
         s.println("func.printInt:");
         s.println("lw   $a0, 0($sp)");
         s.println("li   $v0, 1");
         s.println("syscall");
         s.println("jr $ra");
     }
 
     /**
      * Prints the current stack value assuming it's a bool.
      * @param s The stream to print to.
      */
     public void funcPrintBool(PrintStream s) {
         s.println("func.printBool:");
         s.println("lw $a0, 0($sp)");
         s.println("beqz $a0, label.printBool.loadFalse");
         s.println("la $a0, data.trueString");
         s.println("j label.printBool.join");
         s.println("label.printBool.loadFalse:");
         s.println("la $a0, data.falseString");
         s.println("label.printBool.join:");
         s.println("li   $v0, 4");
         s.println("syscall");
         s.println("jr $ra");
     }
 
     /**
      * Prints the current stack value assuming it's a float.
      * @param s The stream to print to.
      */
     private void funcPrintFloat(PrintStream s) {
         s.println("func.printFloat:");
         s.println("l.s  $f12, 0($sp)");
         s.println("li   $v0,  2");
         s.println("syscall");
         s.println("jr $ra");
     }
 
     /**
      * Prints a newline
      * @param s The stream to print to.
      */
     private void funcPrintln(PrintStream s) {
         s.println("func.println:");
         s.println("la   $a0, data.newline");
         s.println("li   $v0, 4");
         s.println("syscall");
         s.println("jr $ra");
     }
 
     /**
      * Reads an int onto the stack.
      * @param s The stream to print to.
      */
     private void funcReadInt(PrintStream s) {
         s.println("func.readInt:");
         s.println("la   $a0, data.intquery");
         s.println("li   $v0, 4");
         s.println("syscall");
         s.println("li   $v0, 5");
         s.println("syscall");
         s.println("jr $ra");
     }
 
     /**
      * Reads a float onto the stack.
      * @param s The stream to print to.
      */
     private void funcReadFloat(PrintStream s) {
         s.println("func.readFloat:");
         s.println("la   $a0, data.floatquery");
         s.println("li   $v0, 4");
         s.println("syscall");
         s.println("li   $v0, 6");
         s.println("syscall");
         s.println("mfc1 $v0, $f0");
         s.println("jr $ra");
     }
 
 	/**
 	 * Print a describing comment to the code section.
 	 * @param comment The comment to append.
 	 */
     public void debugComment(String comment) {
 		if (CodeGen.DEBUG) {
 			appendInstruction("\t\t\t\t# " + comment);
 		}
     }
 }
