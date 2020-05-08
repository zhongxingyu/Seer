 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 public class AssemblyCodeGenerator {
 
     private final String COMPILER_IDENT = "WRC 1.0";
     private int indent_level = 0;
     private Stack<Integer> stackPointer;
     
     // Error Messages
     private static final String ERROR_IO_CLOSE     = "Unable to close fileWriter";
     private static final String ERROR_IO_CONSTRUCT = "Unable to construct FileWriter for file %s";
     private static final String ERROR_IO_WRITE     = "Unable to write to fileWriter";
 
     // FileWriter
     private FileWriter fileWriter;
     
     // Output file header
     private static final String FILE_HEADER = 
         "/*\n" +
         " * Generated %s\n" + 
         " */\n\n";
     
     private int str_count = 0;
     private int float_count = 0;
         
     //-------------------------------------------------------------------------
     //      Constructors
     //-------------------------------------------------------------------------
 
     public AssemblyCodeGenerator(String fileToWrite) {
         try {
             fileWriter = new FileWriter(fileToWrite);
             
             // write fileHeader with date/time stamp
             writeAssembly(FILE_HEADER, (new Date()).toString());
         } 
         catch (IOException e) {
             System.err.printf(ERROR_IO_CONSTRUCT, fileToWrite);
             e.printStackTrace();
             System.exit(1);
         }
 
         stackPointer = new Stack<Integer>();
     }
 
     //-------------------------------------------------------------------------
     //      setIndent
     //-------------------------------------------------------------------------
     public void decreaseIndent(int indent) {
         indent_level = indent;
     }
 
     //-------------------------------------------------------------------------
     //      decreaseIndent
     //-------------------------------------------------------------------------
     public void decreaseIndent() {
         if(indent_level > 0)
             indent_level--;
     }
     
     //-------------------------------------------------------------------------
     //      increaseIndent
     //-------------------------------------------------------------------------
     public void increaseIndent() {
         indent_level++;
     }
 
     //-------------------------------------------------------------------------
     //      dispose
     //-------------------------------------------------------------------------
     public void dispose() {
         // Close the filewriter
         try {
             fileWriter.close();
         } 
         catch (IOException e) {
             System.err.println(ERROR_IO_CLOSE);
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     //-------------------------------------------------------------------------
     //      writeAssembly
     //-------------------------------------------------------------------------
     // params = String []
     public void writeAssembly(String template, String ... params) {
         StringBuilder asStmt = new StringBuilder();
         
         // Indent current line
         for (int i=0; i < indent_level; i++) {
             asStmt.append(SparcInstr.INDENTOR);
         }
         
         asStmt.append(String.format(template, (Object[])params));
         
         try {
             fileWriter.write(asStmt.toString());
         } 
         catch (IOException e) {
             System.err.println(ERROR_IO_WRITE);
             e.printStackTrace();
         }
     }
     
     //-------------------------------------------------------------------------
     //      example
     //-------------------------------------------------------------------------
     public void example() {
         AssemblyCodeGenerator myAsWriter = new AssemblyCodeGenerator("output.s");
 
         myAsWriter.increaseIndent();
         myAsWriter.writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(4095), "%l0");
         myAsWriter.increaseIndent();
         myAsWriter.writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(1024), "%l1");
         myAsWriter.decreaseIndent();
         
         myAsWriter.writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(512), "%l2");
         
         myAsWriter.decreaseIndent();
         myAsWriter.dispose();
     }
 
     //-------------------------------------------------------------------------
     //      String Utility Functions
     //-------------------------------------------------------------------------
     public String quoted(String str)
     {
         return "\"" + str + "\"";
     }
 
     public String sqBracketed(String str)
     {
         return "[" + str + "]";
     }
 
     //-------------------------------------------------------------------------
     //      Other Utility Functions
     //-------------------------------------------------------------------------
     public String getNextOffset(int size)
     {
         Integer temp = stackPointer.pop(); 
         temp = temp - size;
         stackPointer.push(temp);
         return stackPointer.peek().toString();
     }
 
     public String getOffset()
     {
         return stackPointer.peek().toString();
     }
 
     //-------------------------------------------------------------------------
     //
     //      Code Generation Functions
     //  
     //-------------------------------------------------------------------------
 
     //-------------------------------------------------------------------------
     //      WriteComment
     //-------------------------------------------------------------------------
     public void writeComment(String comment)
     {
         // ! Comment
         writeAssembly(SparcInstr.LINE, SparcInstr.COMMENT + " " + comment);
     }
 
     public void writeCommentHeader(String comment)
     {
         writeAssembly(SparcInstr.BLANK_LINE);
         // !----Comment----
         writeAssembly(SparcInstr.LINE, SparcInstr.COMMENT + "----" + comment + "----");
     }
 
     //-------------------------------------------------------------------------
     //      DoProgramStart
     //-------------------------------------------------------------------------
     public void DoProgramStart(String filename)
     {
         increaseIndent();
     
         writeCommentHeader("Starting Program");
 
         // .file "<filename>"
         //writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.FILE_DIR, quoted(filename));
 
         // .ident <COMPILER_IDENT
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.IDENT_DIR, quoted(COMPILER_IDENT));
 
         writeAssembly(SparcInstr.BLANK_LINE);
 
         DoROPrintDefines();
 
         stackPointer.push(new Integer(0));
 
     }
 
     //-------------------------------------------------------------------------
     //      DoROPrintDefines
     //-------------------------------------------------------------------------
     public void DoROPrintDefines()
     {
         // !----Default String Formatters----
         writeCommentHeader("Default String Formatters");
 
         // .section ".rodata"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.RODATA_SEC);
 
         // _endl: .asciz "\n"
         writeAssembly(SparcInstr.RO_DEFINE, SparcInstr.ENDL, SparcInstr.ASCIZ_DIR, quoted("\\n"));
 
         // _intFmt: .asciz "%d"
         writeAssembly(SparcInstr.RO_DEFINE, SparcInstr.INTFMT, SparcInstr.ASCIZ_DIR, quoted("%d"));
 
         // _boolFmt: .asciz "%s"
         writeAssembly(SparcInstr.RO_DEFINE, SparcInstr.BOOLFMT, SparcInstr.ASCIZ_DIR, quoted("%s"));
 
         // _boolT: .asciz "true"
         writeAssembly(SparcInstr.RO_DEFINE, SparcInstr.BOOLT, SparcInstr.ASCIZ_DIR, quoted("true"));
 
         // _boolF: .asciz "false"
         writeAssembly(SparcInstr.RO_DEFINE, SparcInstr.BOOLF, SparcInstr.ASCIZ_DIR, quoted("false"));
 
         // _strFmt: .asciz "%s"
         writeAssembly(SparcInstr.RO_DEFINE, SparcInstr.STRFMT, SparcInstr.ASCIZ_DIR, quoted("%s"));
         
         writeAssembly(SparcInstr.BLANK_LINE);
     }
 
     //-------------------------------------------------------------------------
     //      DoFuncStart
     //-------------------------------------------------------------------------
     public void DoFuncStart(FuncSTO funcSto)
     {
         // !----Function: <funcName>----
         writeCommentHeader("Function: " + funcSto.getName());
 
         // .section ".text"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.TEXT_SEC);
 
         // TODO: Is this always 4? Otherwise, calculate the value in general
         // .align 4
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, String.valueOf(4));
 
         // .global <funcName>
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.GLOBAL_DIR, funcSto.getName());
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Write the function label
         decreaseIndent();
         // <funcName>: 
         writeAssembly(SparcInstr.LABEL, funcSto.getName());
         increaseIndent();
 
         // Move the saved offset for this function into %g1 and then execute the save instruction that shifts the stack
         // set SAVE.<funcName>, %g1
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.SAVE_WORD + "." + funcSto.getName(), SparcInstr.REG_GLOBAL1);
 
         // save %sp, %g1, %sp
         writeAssembly(SparcInstr.THREE_PARAM, SparcInstr.SAVE_OP, SparcInstr.REG_STACK, SparcInstr.REG_GLOBAL1, SparcInstr.REG_STACK);
         writeAssembly(SparcInstr.BLANK_LINE);
 
 
     }
 
     //-------------------------------------------------------------------------
     //      DoFuncFinish
     //-------------------------------------------------------------------------
     public void DoFuncFinish(FuncSTO funcSto)
     {
         // Perform return/restore
         // TODO: Right now, two sets of ret/restore will be printed if the function did explicit "return"
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // ret
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.RET_OP);
 
         // restore
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.RESTORE_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Write the assembler directive to save the amount of bytes needed for the save operation
         decreaseIndent();
         // SAVE.<func> = -(92 + BytesOfLocalVarsAndTempStackSpaceNeeded) & -8
         writeAssembly(SparcInstr.SAVE_FUNC, funcSto.getName(), String.valueOf(92), String.valueOf(funcSto.getLocalVarBytes()));
         increaseIndent();
     }
     
     //-------------------------------------------------------------------------
     //      DoReturn
     //-------------------------------------------------------------------------
     public void DoReturn(FuncSTO funcSto, STO returnSto)
     {
         // Load the return value into the return register
         if(!returnSto.getType().isVoid()) {
             if(funcSto.getReturnByRef()) {
                 // TODO: Set the return value for return by reference
 
             }
             else { 
                 // ld [<location>], %i0
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed(returnSto.load()), SparcInstr.REG_SET_RETURN);
             }
         }
 
         // Perform return/restore
         // ret
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.RET_OP);
 
         // restore
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.RESTORE_OP);
     }
 
     //-------------------------------------------------------------------------
     //      DoCout
     //-------------------------------------------------------------------------
     public void DoCout(STO sto) {
         // !----cout << <sto name>----
         writeCommentHeader("cout << " + sto.getName());
         if(sto.getType().isInt()) {
             // set _intFmt, %o0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.INTFMT, SparcInstr.REG_ARG0);
 
             if(sto.isConst()) {
                 // set <value>, %o1
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) sto).getIntValue()), SparcInstr.REG_ARG1);
             }
             else {
                 // ld [<value>], %o1
                writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sto.load(), SparcInstr.REG_ARG1);
             }
 
             // call printf
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
 
         }
         
         else if(sto.getType().isBool()) {
             // set _intFmt, %o0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.INTFMT, SparcInstr.REG_ARG0);
 
             if(sto.isConst()) {
                 String val;
 
                 if(((ConstSTO) sto).getValue() == 1)
                     val = SparcInstr.BOOLT;
                 else
                     val = SparcInstr.BOOLF;
 
                 // set <value>, %o1
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, val, SparcInstr.REG_ARG1);
             }
             else {
                 // TODO: THink we need to actually write a if/else in assembly for it to decide whether to print "true" or "false"
                 // if so, can wait and use DoIf() and DoElse() once we've written it
 
             }
 
             // call printf
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         }
 
         else if(sto.getType().isFloat()) {
             // .section ".data"
             //writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.DATA_SEC);
             // .align 4
            // writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
             // tmp1: .single 0r5.75 
             //writeAssembly(SparcInstr.RO_DEFINE, "tmp1", SparcInstr.SINGLEP, "0r"+(String.valueOf(((ConstSTO) sto).getFloatValue())));
             // .section ".text"
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.TEXT_SEC);
             // .align 4
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
             // ld [sto] %f0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed(sto.getBase()+sto.getOffset()), "%f0");
             // set tmp1, %l0
             //writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, "tmp1", "%l0");
             // ld [%l0], %f0
             //writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed("%l0"), "%f0");
             // call printFloat
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTFLOAT);
             // nop
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         }
         // String literal
         else if (sto.getType().isString()) {
         	// .section ".rodata"
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.RODATA_SEC);
             // .align 4
         	//writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
         	// str_(str_count): .asciz "string literal" 
         	writeAssembly(SparcInstr.RO_DEFINE, ".str_"+str_count, SparcInstr.ASCIZ_DIR, quoted(sto.getName()));
         	// .section ".text"
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.TEXT_SEC);
             // .align 4
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
         	// set _strFmt, %o0
         	 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, ".strFmt", "%l0");
         	// set str_(str_count), %o1
         	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, "str_"+str_count, "%l1");
             // call printf
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
             // nop
         	writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         	
         	// increment str count
         	str_count++;
         }
         // endl
         else if (sto.getType().isVoid()) {
         	// set _strFmt %o0
         	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.STRFMT, "%o0");
         	// set _endl, %o1
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.ENDL, "%o1");
             // call printf
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
             // nop
         	writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         }
     }
 
     //-------------------------------------------------------------------------
     //      DoVarDecl
     //-------------------------------------------------------------------------
     public void DoVarDecl(STO sto)
     {
         // Global
         if(sto.isGlobal()) {
 
         }
 
         // Local basic type (int, float, boolean)
         String offset = getNextOffset(sto.getType().getSize());
         sto.store(SparcInstr.REG_FRAME, offset);
 
         
         // For float, check DI6 Page on "What about float?"
 
         // Array (TODO: In Phase 2)
 
 
         // Pointer (TODO: In Phase 3)
     }
 
     //-------------------------------------------------------------------------
     //      DoAssignExpr
     //-------------------------------------------------------------------------
     public void DoAssignExpr(STO stoDes, STO stoValue)
     {
         // TODO: My isConst check can go away after we allocate constants onto stack
         // If storing a float
         /*
         // TODO: Float needs to be reconsidered for how you access floats
         if(stoDes.getType().isFloat()) {
 
             // Put value-to-assign into %f0
             if(stoValue.isConst()) {
                 // Set the value of constant into %f0
                 // set <value>, %f0
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, stoValue.getValue(), SparcInstr.REG_FLOAT0);
             }
             else if(stoValue.isVar()) {
                 // Load value of var into %f0
                 // ld [<stoValue location>], %f0
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed(stoValue.load()), SparcInstr.REG_FLOAT0);
             }
 
             // If value is not a float, convert it to a float
             if(!stoValue.getType().isFloat()) {
                 // fitos %f0, %f0 ! Convert bit pattern to FP
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.FITOS_OP, SparcInstr.REG_FLOAT0, SparcInstr.REG_FLOAT0);
             }
 
             // Store value in %f0 into address of destination sto
             // st %f0, [<stoDes location>]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_FLOAT0, sqBracketed(stoDes.load()));
         }
         */
         //else {
             // Put value-to-assign into %l0
             if(stoValue.isConst()) {
                 // Set the value of constant into %l0
                 // set <value>, %l0
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) stoValue).getValue()), SparcInstr.REG_LOCAL0);
             }
             else if(stoValue.isVar()) {
                 // Load value of var into %l0
                 // ld [<stoValue location>], %l0
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed(stoValue.load()), SparcInstr.REG_LOCAL0);
             }
 
             // Store value in %l0 into address of destination sto
             // st %l0, [<stoDes location>]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL0, sqBracketed(stoDes.load()));
        // }
     }
 
     /*
     //-------------------------------------------------------------------------
     //      LoadIntoReg
     //-------------------------------------------------------------------------
     public void functionName389(String reg, String offset, String base)
     {
         // set <offset>, <reg>
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, offset, reg);
 
         //add <base> , %l0 
         writeAssembly(SparcInstr.THREE_PARAM, SparcInstr.ADD_OP, base, reg, reg);
     }
 
     //-------------------------------------------------------------------------
     //      LoadStoIntoReg
     //-------------------------------------------------------------------------
     public void functionName389(String reg, STO sto)
     {
         // PUT ADDRESS OF STO INTO <reg>
         LoadIntoReg(reg, sto.getOffset(), sto.getBase());
 
         // LOAD VALUE AT ADDRESS INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed(reg), reg);
     }
 
     //-------------------------------------------------------------------------
     //      StoreStoIntoSto
     //-------------------------------------------------------------------------
     public void functionName389(String reg, STO stoDes, STO stoValue)
     {
         // PUT ADDRESS OF STO INTO <reg>
         LoadIntoReg(reg, sto.getOffset(), sto.getBase());
 
         // LOAD VALUE AT ADDRESS INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed(reg), reg);
     }
     */
 
 
     //-------------------------------------------------------------------------
     //      DoLiteral
     //-------------------------------------------------------------------------
     public void DoLiteral(ConstSTO sto)
     {
         String offset = getNextOffset(sto.getType().getSize());
 
         if(sto.getType().isInt() && sto.getType().isBool()) {
             // put the literal in memory            
             // set <value>, %l0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) sto).getIntValue()), SparcInstr.REG_LOCAL0);
             
             // st %l0, [%fp-offset]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL0, SparcInstr.REG_FRAME + offset);
         }
 
         else if(sto.getType().isFloat()) {
             // store literal in rodata section
             // .section ".data"
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.DATA_SEC);
             // .align 4
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
 
             // float<xxx>: .single 0r5.75 
             writeAssembly(SparcInstr.RO_DEFINE, ".float_" + String.valueOf(float_count), SparcInstr.SINGLEP, "0r" + (String.valueOf(((ConstSTO) sto).getFloatValue())));
 
             // .section ".text"
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.TEXT_SEC);
 
             // .align 4
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
 
             // set label, %l0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, ".float_" + String.valueOf(float_count), SparcInstr.REG_LOCAL0);
 
             // ld [%l0], %f0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed(SparcInstr.REG_LOCAL0), SparcInstr.REG_FLOAT0);
 
             // st %f0, [%fp-offset]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_FLOAT0, sqBracketed(SparcInstr.REG_FRAME) + offset);
 
             float_count++;
         }
 
         // store the address on sto
         sto.store(SparcInstr.REG_FRAME, offset);
     }
 
     //-------------------------------------------------------------------------
     //      functionName403
     //-------------------------------------------------------------------------
     public void functionName405()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName411
     //-------------------------------------------------------------------------
     public void functionName413()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName419
     //-------------------------------------------------------------------------
     public void functionName421()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName427
     //-------------------------------------------------------------------------
     public void functionName429()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName435
     //-------------------------------------------------------------------------
     public void functionName437()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName443
     //-------------------------------------------------------------------------
     public void functionName445()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName451
     //-------------------------------------------------------------------------
     public void functionName453()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName459
     //-------------------------------------------------------------------------
     public void functionName461()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName467
     //-------------------------------------------------------------------------
     public void functionName469()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName475
     //-------------------------------------------------------------------------
     public void functionName477()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName483
     //-------------------------------------------------------------------------
     public void functionName485()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName491
     //-------------------------------------------------------------------------
     public void functionName493()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName499
     //-------------------------------------------------------------------------
     public void functionName501()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName507
     //-------------------------------------------------------------------------
     public void functionName509()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName515
     //-------------------------------------------------------------------------
     public void functionName517()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName523
     //-------------------------------------------------------------------------
     public void functionName525()
     {
 
     }
 
     //-------------------------------------------------------------------------
     //      functionName531
     //-------------------------------------------------------------------------
     public void functionName533()
     {
 
     }
 
 }
