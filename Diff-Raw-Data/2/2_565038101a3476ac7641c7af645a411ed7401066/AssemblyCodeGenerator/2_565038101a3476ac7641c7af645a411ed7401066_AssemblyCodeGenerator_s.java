 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 public class AssemblyCodeGenerator {
 
     private final String COMPILER_IDENT = "WRC 1.0";
     private int indent_level = 0;
     private Stack<FuncSTO> currentFunc;
     private Stack<Integer> stackPointer;
     private Stack<StoPair> globalInitStack;
     private Stack<String> stackIfLabel;
     private Vector<StackRecord> stackValues;
 
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
     private int ifLabel_count = 0;
         
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
 
         currentFunc = new Stack<FuncSTO>();
         stackPointer = new Stack<Integer>();
         globalInitStack = new Stack<StoPair>();
         stackIfLabel = new Stack<String>();
         stackValues = new Vector<StackRecord>();
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
     //      String Utility Functions
     //-------------------------------------------------------------------------
     public String quoted(String str)
     {
         return "\"" + str + "\"";
     }
 
     public String bracket(String str)
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
         currentFunc.peek().addBytes(size);
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
         writeComment("----" + comment + "----");
     }
 
     public void writeStackValues()
     {
         writeCommentHeader("Stack Records");
         writeComment("======================================");
         for(StackRecord thisRecord: stackValues) {
             writeAssembly(SparcInstr.LINE, SparcInstr.COMMENT + thisRecord.getFunction() + SparcInstr.SEPARATOR 
                 + thisRecord.getId() + SparcInstr.SEPARATOR + thisRecord.getLocation());
         }
     }
 
 
     //-------------------------------------------------------------------------
     //      DoProgramStart
     //-------------------------------------------------------------------------
     public void DoProgramStart(String filename)
     {
         increaseIndent();
     
         writeCommentHeader("Starting Program");
 
         // .file "<filename>"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.FILE_DIR, quoted(filename));
 
         // .ident <COMPILER_IDENT
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.IDENT_DIR, quoted(COMPILER_IDENT));
 
         writeAssembly(SparcInstr.BLANK_LINE);
 
         DoROPrintDefines();
         MakeGlobalInitGuard();
 
         stackPointer.push(new Integer(0));
         currentFunc.push(new FuncSTO("global", new FuncPtrType(new VoidType(), false)));
 
     }
 
     //-------------------------------------------------------------------------
     //      DoProgramEnd
     //-------------------------------------------------------------------------
     public void DoProgramEnd()
     {
         writeStackValues();
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
     //      DoGlobalDecl
     //-------------------------------------------------------------------------
     public void DoGlobalDecl(STO varSto, STO valueSto)
     {
 
         // .global <id>
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.GLOBAL_DIR, varSto.getName());
 
         // .section ".bss"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.BSS_SEC);
 
         // .align 4
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
 
         decreaseIndent();
 
         // <id>: .skip 4
         writeAssembly(SparcInstr.GLOBAL_DEFINE, varSto.getName(), SparcInstr.SKIP_DIR, String.valueOf(4));
 
         increaseIndent();
 
         // Push these for later to initialize when main() starts
         if(!valueSto.isNull())
             globalInitStack.push(new StoPair(varSto, valueSto));    
 
         // set the base and offset to the sto
         varSto.store(SparcInstr.REG_GLOBAL0, varSto.getName());
         stackValues.addElement(new StackRecord("global", varSto.getName(), varSto.load()));
     }
 
     //-------------------------------------------------------------------------
     //      MakeGlobalInitGuard
     //-------------------------------------------------------------------------
     public void MakeGlobalInitGuard()
     {
         // !----Create _init for global init guard----
         writeCommentHeader("Create _init for global init guard");
 
         // .section ".bss"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.BSS_SEC);
 
         // .align 4
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, String.valueOf(4));
 
         // _init: .skip 4
         writeAssembly(SparcInstr.GLOBAL_DEFINE, "_init", SparcInstr.SKIP_DIR, String.valueOf(4));
 
         writeAssembly(SparcInstr.BLANK_LINE);
     }
 
     //-------------------------------------------------------------------------
     //      DoGlobalInit
     //-------------------------------------------------------------------------
     public void DoGlobalInit()
     {
         // !----Initialize Globals----
         writeCommentHeader("Initialize Globals");
 
         // Do Init Guard
 
         writeComment("Check if init is already done");
         // set _init, %l0
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, "_init", SparcInstr.REG_LOCAL0);
 
         // ld [%l0], %l1
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL0), SparcInstr.REG_LOCAL1);
 
         // cmp %l1, %g0
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, SparcInstr.REG_LOCAL1, SparcInstr.REG_GLOBAL0);
 
         // bne _init_done ! Global initialization guard
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.BNE_OP, "_init_done");
 
         // set 1, %l1 ! Branch delay slot
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL1);
 
         writeComment("Set init flag to 1 now that we're about to do the init");
         // st %l1, [%l0] ! Mark _init = 1
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL1, bracket(SparcInstr.REG_LOCAL0));
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Setup Stack iteration
         Stack stk = new Stack();
         StoPair stopair;
         STO varSto;
         STO valueSto;
 
         stk.addAll(globalInitStack);
         Collections.reverse(stk);
 
         // Loop through all the initialization pairs on the stack
         for(Enumeration<StoPair> e = stk.elements(); e.hasMoreElements(); ) {
             stopair = e.nextElement();
             varSto = stopair.getVarSto();
             valueSto = stopair.getValueSto();
 
             if(valueSto.isConst())
                 DoLiteral((ConstSTO) valueSto);
 
             writeComment("Initializing: " + varSto.getName() + " = " + valueSto.getName());
 
             // ld [<value>], %l1
             LoadSto(valueSto, SparcInstr.REG_LOCAL1);
 
             //writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, bracket(valueSto.load()), SparcInstr.REG_LOCAL1);
             // set x, %l0
             //writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, bracket(varSto.load()), SparcInstr.REG_LOCAL0);
             // add %g0, %l0, %l0
             //writeAssembly(SparcInstr.THREE_PARAM, SparcInstr.ADD_OP, SparcInstr.REG_GLOBAL0, SparcInstr.REG_LOCAL0, SparcInstr.REG_LOCAL0);
 
             LoadStoAddr(varSto, SparcInstr.REG_LOCAL0);
 
             // st %l1, [%l0]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL1, bracket(SparcInstr.REG_LOCAL0));
             writeAssembly(SparcInstr.BLANK_LINE);
         }
 
         // _init_done:
         decreaseIndent();
         writeAssembly(SparcInstr.LABEL, "_init_done");
         writeAssembly(SparcInstr.BLANK_LINE);
         increaseIndent();
 
     }
 
     //-------------------------------------------------------------------------
     //      DoFuncStart
     //-------------------------------------------------------------------------
     public void DoFuncStart(FuncSTO funcSto)
     {
         currentFunc.push(funcSto);
         stackPointer.push(0);
 
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
 
         stackPointer.pop();
         currentFunc.pop();
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
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, bracket(returnSto.load()), SparcInstr.REG_SET_RETURN);
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
 
             // ld [<value>], %o1
             LoadSto(sto, SparcInstr.REG_ARG1);
 
             // call printf
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         }
         
         else if(sto.getType().isBool()) {
             // set _intFmt, %o0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.BOOLFMT, SparcInstr.REG_ARG0);
 
             // Set the condition STO into LOCAL0
             LoadSto(sto, SparcInstr.REG_LOCAL0);
             String ifLabel = ".ifL_" + ifLabel_count;
             String elseLabel = ".elseL_" + ifLabel_count;
     	    ifLabel_count++;
 
             // If the condition is true, don't branch and load "true", if false, branch to end of if and load "false"
             // value == 0
             writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.CMP_OP, SparcInstr.REG_LOCAL0, SparcInstr.REG_GLOBAL0, "Compare boolean value " + sto.getName() + " to 0");
             writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BE_OP, ifLabel, "If <cond> is true, don't branch, if false, branch");
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
 
             // If Code: Load "true" into %o1 here
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.BOOLT, SparcInstr.REG_ARG1);
             // Branch to end of else block
             writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BA_OP, elseLabel, "Did if code, branch always to bottom of else");
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
 
             // Print label
             decreaseIndent();
             writeAssembly(SparcInstr.LABEL, ifLabel);
             increaseIndent();
             
             // Else code: load "false" into %o1 here
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.BOOLF, SparcInstr.REG_ARG1);
             
             // Else done, print label
             decreaseIndent();
             writeAssembly(SparcInstr.LABEL, elseLabel);
             increaseIndent();
             
             // call printf
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         }
 
         else if(sto.getType().isFloat()) {
             // ld [sto] %f0
             //LoadSto(sto, SparcInstr.REG_FLOAT0);
             LoadStoAddr(sto, SparcInstr.REG_LOCAL0);
 
             // call printFloat
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTFLOAT);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         }
 
         // String literal
         else if (sto.getType().isString()) {
 
         	// .section ".rodata"
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.RODATA_SEC);
 
         	// str_(str_count): .asciz "string literal" 
         	writeAssembly(SparcInstr.RO_DEFINE, ".str_"+str_count, SparcInstr.ASCIZ_DIR, quoted(sto.getName()));
 
         	// .section ".text"
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.TEXT_SEC);
 
             // .align 4
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
 
         	// set _strFmt, %o0
         	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.STRFMT, "%o0");
 
         	// set str_(str_count), %o1
         	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, ".str_"+str_count, "%o1");
 
             // call printf
         	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
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
         	writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         }
     }
 
     //-------------------------------------------------------------------------
     //      DoVarDecl
     //-------------------------------------------------------------------------
     public void DoVarDecl(STO sto)
     {
         // TODO: Need to store how many bytes used in function
         // Local basic type (int, float, boolean)
         String offset = getNextOffset(sto.getType().getSize());
         sto.store(SparcInstr.REG_FRAME, offset);
         stackValues.addElement(new StackRecord(currentFunc.peek().getName(), sto.getName(), sto.load()));
 
         // Initialize to 0, mostly for testing purposed
         writeAssembly(SparcInstr.BLANK_LINE);
         writeComment("Declare " + sto.getName());
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.MOV_OP, SparcInstr.REG_GLOBAL0, SparcInstr.REG_LOCAL0);
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL0, bracket(sto.load()));
         writeAssembly(SparcInstr.BLANK_LINE);
         
         // For float, check DI6 Page on "What about float?"
 
         // Array (TODO: In Phase 2)
 
 
         // Pointer (TODO: In Phase 3)
     }
 
     //-------------------------------------------------------------------------
     //      DoAssignExpr
     //-------------------------------------------------------------------------
     public void DoAssignExpr(STO stoVar, STO stoValue)
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
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, bracket(stoValue.load()), SparcInstr.REG_FLOAT0);
             }
 
             // If value is not a float, convert it to a float
             if(!stoValue.getType().isFloat()) {
                 // fitos %f0, %f0 ! Convert bit pattern to FP
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.FITOS_OP, SparcInstr.REG_FLOAT0, SparcInstr.REG_FLOAT0);
             }
 
             // Store value in %f0 into address of destination sto
             // st %f0, [<stoDes location>]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_FLOAT0, bracket(stoDes.load()));
         }
         */
         //else {
             // Put value-to-assign into %l0
                 // Load value of var into %l0
                 // ld [<stoValue location>], %l0
         LoadStoAddr(stoVar, SparcInstr.REG_LOCAL0);
         StoreSto(stoValue, SparcInstr.REG_LOCAL1, SparcInstr.REG_LOCAL0);
 
             // Store value in %l0 into address of destination sto
             // st %l0, [<stoDes location>]
        // }
     }
 
     //-------------------------------------------------------------------------
     //      LoadSto - Uses %l7 as a temp
     //-------------------------------------------------------------------------
     public void LoadSto(STO sto, String reg)
     {
         writeComment("Load " + sto.getName() + " into " + reg);
         // PUT ADDRESS OF STO INTO tmpReg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, sto.getOffset(), SparcInstr.REG_LOCAL7, "Put the offset/name of " + sto.getName() + " into " + SparcInstr.REG_LOCAL7);
 
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, sto.getBase(), SparcInstr.REG_LOCAL7, SparcInstr.REG_LOCAL7, "Add offset/name to base reg " + SparcInstr.REG_LOCAL7);
 
         // LOAD VALUE AT ADDRESS INTO <reg>
        writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(reg), reg, "Load value of " + sto.getName() + " into " + reg);
     }
 
     //-------------------------------------------------------------------------
     //      LoadStoAddr
     //-------------------------------------------------------------------------
     public void LoadStoAddr(STO sto, String reg)
     {
         writeComment("Load address of " + sto.getName() + " into " + reg);
         // PUT ADDRESS OF STO INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, sto.getOffset(), reg, "Put the offset/name of " + sto.getName() + " into " + reg);
 
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, sto.getBase(), reg, reg, "Add offset/name to base reg " + reg);
     }
 
     //-------------------------------------------------------------------------
     //      StoreSto - Stores a sto's value into destReg
     //-------------------------------------------------------------------------
     public void StoreSto(STO valueSto, String tmpReg, String destReg)
     {
         writeComment("Store " + valueSto.getName() + " into " + destReg);
 
         // Load value into tmpReg
         LoadSto(valueSto, tmpReg);
 
         // STORE VALUE AT ADDRESS INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, tmpReg, bracket(destReg), "Store value of " + valueSto.getName() + " into " + destReg);
     }
 
     //-------------------------------------------------------------------------
     //      StoreValue
     //-------------------------------------------------------------------------
     public void StoreValue(String valueReg, String destReg)
     {
         writeComment("Store value in " + valueReg + " into " + destReg);
 
         // STORE VALUE IN valueReg INTO destReg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, valueReg, bracket(destReg), "Store value in " + valueReg  + " into " + destReg);
     }
 
     //-------------------------------------------------------------------------
     //      StoreValueIntoSto - Stores value in valueReg into destSto
     //-------------------------------------------------------------------------
     public void StoreValueIntoSto(String valueReg, String tmpReg, STO destSto)
     {
         writeComment("Store value in " + valueReg + " into sto " + destSto.getName());
         
         // Load sto addr into tmpReg
         LoadStoAddr(destSto, tmpReg);
 
         // STORE VALUE IN valueReg INTO destSto (which has addr in tmpReg)
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, valueReg, bracket(tmpReg), "Store value in " + valueReg  + " into sto " + destSto.getName());
     }
 
     //-------------------------------------------------------------------------
     //      DoLiteral
     //-------------------------------------------------------------------------
     public void DoLiteral(ConstSTO sto)
     {
         if(currentFunc.peek().getName().equals("global"))
             return;
 
         String offset = getNextOffset(sto.getType().getSize());
 
         writeComment("Put literal onto stack");
         if(sto.getType().isInt() || sto.getType().isBool()) {
             // put the literal in memory            
             // set <value>, %l0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) sto).getIntValue()), SparcInstr.REG_LOCAL0);
             
             // st %l0, [%fp-offset]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL0, bracket(SparcInstr.REG_FRAME + offset));
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
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL0), SparcInstr.REG_FLOAT0);
 
             // st %f0, [%fp-offset]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_FLOAT0, bracket(SparcInstr.REG_FRAME + offset));
 
             float_count++;
         }
 
         // store the address on sto
         sto.store(SparcInstr.REG_FRAME, offset);
         stackValues.addElement(new StackRecord(currentFunc.peek().getName(), sto.getName(), sto.load()));
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
     //      DoBinaryOp
     //-------------------------------------------------------------------------
     public void DoBinaryOp(BinaryOp op, STO operand1, STO operand2, STO resultSTO)
     {
     	String operation = "";
     	
     	if(op.getName().equals("+")){
     		operation = SparcInstr.ADD_OP;
     	}
     	else if(op.getName().equals("-")){
     		operation = SparcInstr.SUB_OP;
     	}
     	else if(op.getName().equals("*")){
     		operation = SparcInstr.MUL_OP;
     	}
     	else if(op.getName().equals("/")){
     		operation = SparcInstr.DIV_OP;
     	}
     	else if(op.getName().equals("%")){
     		operation = SparcInstr.REM_OP;
     	}
     	else if(op.getName().equals("&")){
     		operation = SparcInstr.AND_OP;
     	}
     	else if(op.getName().equals("|")){
     		operation = SparcInstr.OR_OP;
     	}
     	else if(op.getName().equals("^")){
     		operation = SparcInstr.XOR_OP;
     	}
     	
     	LoadSto(operand1, SparcInstr.REG_LOCAL0);
     	LoadSto(operand2, SparcInstr.REG_LOCAL1);
     	writeAssembly(SparcInstr.THREE_PARAM_COMM, operation, SparcInstr.REG_LOCAL0, SparcInstr.REG_LOCAL1, SparcInstr.REG_LOCAL0, "Adding Values!");
     	resultSTO.store(SparcInstr.REG_FRAME, getNextOffset(resultSTO.getType().getSize()));
     	StoreValueIntoSto(SparcInstr.REG_LOCAL0, SparcInstr.REG_LOCAL1, resultSTO);
     }
 
     //-------------------------------------------------------------------------
     //      DoIf
     //-------------------------------------------------------------------------
     public void DoIf(ConstSTO condition)
     {
         // !----if <condition>----
     	writeComment("if "+condition.getIntValue());
     	
     	// create if label and increment the count
     	String ifL = ".ifL_"+ifLabel_count;
     	ifLabel_count++;
     	// add the label to the stack
     	stackIfLabel.add(ifL);
     	
     	// set condition, %l0
     	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(condition.getIntValue()), "%l0");
     	// ld [%l0], %l0
     	//writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, sqBracketed("%l0"), "%l0");
     	// cmp %l0, %g0
     	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, "%l0", "%g0");
     	// be IfL1! Opposite logic
     	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.BE_OP, ifL);
     	writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
     	increaseIndent();
     }
     
     //-------------------------------------------------------------------------
     //      DoIfCodeBlock
     //-------------------------------------------------------------------------
     public void DoIfCodeBlock()
     {
     	decreaseIndent();
     	String label = stackIfLabel.pop();
     	writeAssembly(SparcInstr.LABEL, label);
     }
 
 }
