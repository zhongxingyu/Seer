 
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
     private int compLabel_count = 0;
         
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
             DoAssignExpr(varSto, valueSto);
         /*
             // ld [<value>], %l1
             LoadSto(valueSto, SparcInstr.REG_LOCAL1);
             LoadStoAddr(varSto, SparcInstr.REG_LOCAL0);
 
             // st %l1, [%l0]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL1, bracket(SparcInstr.REG_LOCAL0));
             writeAssembly(SparcInstr.BLANK_LINE);
         */
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
 
         /*
         Vector<ParamSTO> params = funcSto.getType().getParameters();
         // Store parameters into memory
         for(int i = 0; i < params.size(); i++) {
             AllocateSto(params.elementAt(i));
             StoreSto(params.elementAt(i), SparcInstr.ARG_REGS[i]);
         }
         */
 
     }
 
     //-------------------------------------------------------------------------
     //      DoFuncFinish
     //-------------------------------------------------------------------------
     public void DoFuncFinish(FuncSTO funcSto)
     {
         // Perform return/restore
         // TODO: Right now, two sets of ret/restore will be printed if the function did explicit "return"
         writeAssembly(SparcInstr.BLANK_LINE);
 
         if(funcSto.getReturnType().isVoid()) {
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.RET_OP);          // ret
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.RESTORE_OP);      // restore
             writeAssembly(SparcInstr.BLANK_LINE);
         }
 
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
         writeCommentHeader("Set return value (if needed) and return");
         // Load the return value into the return register
         if(!returnSto.getType().isVoid()) {
             if(funcSto.getReturnByRef()) {
                 // TODO: Set the return value for return by reference
                 // This is the gist of it, but needs to be done with the heap address
                 LoadStoAddr(returnSto, SparcInstr.REG_SET_RETURN);
             }
             else { 
                 // If return type is float, put into %f0 (possibly fitos)
                 if(funcSto.getReturnType().isFloat())
                     LoadSto(returnSto, SparcInstr.REG_FLOAT0);
                 // return type is not float, store into %i0
                 else
                     LoadSto(returnSto, SparcInstr.REG_SET_RETURN);
             }
         }
 
         // Perform return/restore
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.RET_OP);      // ret
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.RESTORE_OP);  // restore
         writeAssembly(SparcInstr.BLANK_LINE);
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
             LoadSto(sto, SparcInstr.REG_FLOAT0);
 
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
     //      DoVarDecl - allocates memory on stack and stores location in STO
     //-------------------------------------------------------------------------
     public void DoVarDecl(STO sto)
     {
         AllocateSto(sto);
         
         // Array (TODO: In Phase 2)
 
         // Pointer (TODO: In Phase 3)
     }
 
     //-------------------------------------------------------------------------
     //      AllocateSto - allocates memory on stack and stores location in STO
     //-------------------------------------------------------------------------
     public void AllocateSto(STO sto)
     {
         String offset = getNextOffset(sto.getType().getSize());
         sto.store(SparcInstr.REG_FRAME, offset);
         stackValues.addElement(new StackRecord(currentFunc.peek().getName(), sto.getName(), sto.load()));
         //System.out.println(sto.getName() + " allocated to: " + sto.load());
     }
 
     //-------------------------------------------------------------------------
     //      DoAssignExpr - Stores value in stoValue into stoVar
     //-------------------------------------------------------------------------
     public void DoAssignExpr(STO stoVar, STO stoValue)
     {
         LoadStoAddr(stoVar, SparcInstr.REG_LOCAL0);
         if(stoVar.getType().isFloat())
             StoreSto(stoValue, SparcInstr.REG_FLOAT0, SparcInstr.REG_LOCAL0);
         else
             StoreSto(stoValue, SparcInstr.REG_LOCAL1, SparcInstr.REG_LOCAL0);
 
         writeAssembly(SparcInstr.BLANK_LINE);
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
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL7), reg, "Load value of " + sto.getName() + " into " + reg);
         if(isFloatReg(reg) && sto.getType().isInt()) {
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.FITOS_OP, SparcInstr.REG_FLOAT0, SparcInstr.REG_FLOAT0);
             // System.out.println("[DEBUG] FITOSing in LoadSto");
         }
 
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
     //      StoreValueIntoSto - Stores value in valueReg into destSto - uses %l7 as tmmp
     //-------------------------------------------------------------------------
     public void StoreValueIntoSto(String valueReg, STO destSto)
     {
         writeComment("Store value in " + valueReg + " into sto " + destSto.getName());
         
         // Load sto addr into %l7
         LoadStoAddr(destSto, SparcInstr.REG_LOCAL7);
 
         // STORE VALUE IN valueReg INTO destSto (which has addr in %l6)
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, valueReg, bracket(SparcInstr.REG_LOCAL7), "Store value in " + valueReg  + " into sto " + destSto.getName());
     }
 
     //-------------------------------------------------------------------------
     //      MoveRegToReg - Moves value from one reg into another
     //-------------------------------------------------------------------------
    public void MoveRegToReg(String valueReg, String destReg)
     {
         // Move value in valueReg into destReg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.MOV_OP, valueReg, destReg, "Moving value in " + valueReg  + " into  " + destReg);
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
     //      isFloatReg
     //-------------------------------------------------------------------------
     public boolean isFloatReg(String reg)
     {
         if(reg.contains("%f"))
             return true;
         else
             return false;
     }
 
     //-------------------------------------------------------------------------
     //      DoFuncCall
     //-------------------------------------------------------------------------
     public void DoFuncCall(STO sto, Vector<STO> args, STO returnSto)
     {
         // returnSto is VarSTO if returnByRef, ExprSTO otherwise
 
         writeCommentHeader("Function Call: " + sto.getName());
         
         // Load all arguments into out registers - assuming no more than 6 parameters
         for(int i = 0; i < args.size(); i++) {
             LoadSto(args.elementAt(i), SparcInstr.ARG_REGS[i]);
         }
 
         // call <funcName>
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, sto.getName()); 
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
 
         // Now we can write the code for after the return, which is store the return value to stack
         // If the return type isn't void, save the return value
         if(!returnSto.getType().isVoid()) {
 
             writeComment("Save return from " + sto.getName() + " onto stack");
 
             // Get spot on stack and save to Sto
             String offset = getNextOffset(returnSto.getType().getSize());
             returnSto.store(SparcInstr.REG_FRAME, offset);
             stackValues.addElement(new StackRecord(currentFunc.peek().getName(), returnSto.getName(), returnSto.load()));
 
             if(((FuncPtrType) sto.getType()).getReturnByRef()) {
                 // TODO: Save the return value for return by reference
                 // LoadStoAddr(returnSto, SparcInstr.REG_SET_RETURN);
             }
             else { 
                 // If return type is float, put into %f0 (possibly fitos)
                 if(((FuncPtrType) sto.getType()).getReturnType().isFloat()) {
                     // Store the value, it's in %f0
                     StoreValueIntoSto(SparcInstr.REG_FLOAT0, returnSto);
                 }
                 // return type is not float, store into %i0
                 else {
                     // Store the value, it's in %o0
                     StoreValueIntoSto(SparcInstr.REG_GET_RETURN, returnSto);
                 }
             }
         }
     }
 
     //-------------------------------------------------------------------------
     //      DoExit
     //-------------------------------------------------------------------------
     public void DoExit(STO sto)
     {
         // sto can only be int
         // load sto into %o0
         LoadSto(sto, SparcInstr.REG_ARG0);
 
         // call exit
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.EXIT);
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
     }
 
     //-------------------------------------------------------------------------
     //      DoComparisonOp
     //-------------------------------------------------------------------------
     public void DoComparisonOp(ComparisonOp op, STO operand1, STO operand2, STO resultSto)
     {
         //System.out.println("Inside codegen.DoComparisonOp");
         AllocateSto(resultSto);
 
         String branchOp = "";
         String regOp1 = SparcInstr.REG_LOCAL1;
         String regOp2 = SparcInstr.REG_LOCAL2;
         String cmpOp = SparcInstr.CMP_OP;
         boolean isFloatOp = false;
         
         // If either is float, then we'll use float registers
         if(operand1.getType().isFloat() || operand2.getType().isFloat()) {
             regOp1 = SparcInstr.REG_FLOAT0;
             regOp2 = SparcInstr.REG_FLOAT1;
             isFloatOp = true;
             cmpOp = SparcInstr.FCMPS_OP;
         }
 
         // Determine operator to set branch, regular or float version
 
         // We can use the actual branch ops because we're going to set the value
         // to 1 (true) initially and then branch to the bottom of the "if" if the 
         // condition is true, giving us 1. If it's false, we fall through and set
         // 0
 
         // EqualToOp
         if(op.isEqualToOp()) {
             if(isFloatOp)
                 branchOp = SparcInstr.FBE_OP;
             else
                 branchOp = SparcInstr.BE_OP;
         }
         // GreaterThanEqualOp
         else if(op.isGreaterThanEqualOp()) {
             if(isFloatOp)
                 branchOp = SparcInstr.FBGE_OP;
             else
                 branchOp = SparcInstr.BGE_OP;
         }
         // GreaterThanOp
         else if(op.isGreaterThanOp()) {
             if(isFloatOp)
                 branchOp = SparcInstr.FBG_OP;
             else
                 branchOp = SparcInstr.BG_OP;
         }
         // LessThanEqualOp
         else if(op.isLessThanEqualOp()) {
             if(isFloatOp)
                 branchOp = SparcInstr.FBLE_OP;
             else
                 branchOp = SparcInstr.BLE_OP;
         }
         // LessThanOp
         else if(op.isLessThanOp()) {
             if(isFloatOp)
                 branchOp = SparcInstr.FBL_OP;
             else
                 branchOp = SparcInstr.BL_OP;
         }
         // NEqualToOp
         else if(op.isNEqualToOp()) {
             if(isFloatOp)
                 branchOp = SparcInstr.FBNE_OP;
             else
                 branchOp = SparcInstr.BNE_OP;
         }
 
         // Get label ready
         String compLabel = ".compL_" + compLabel_count;
         compLabel_count++;
         
         // Load the operands
         LoadSto(operand1, regOp1);
         LoadSto(operand2, regOp2);
 
         // %l0 is going to hold our boolean result of the comparison
         // We initialize it to 1 (true) and branch over the %l0 = 0 (false) statement if the comparison is true
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL0, "Init result to true");
 
         // Perform comparison, branch if true, if false, fall through and set 0 (false)
         writeAssembly(SparcInstr.TWO_PARAM_COMM, cmpOp, regOp1, regOp2, "operand1 <cond> operand2");
         writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BE_OP, compLabel, "if the result is true, branch and do nothing");
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
 
         // It was false, set 0
         writeComment("It was false, set 0");
         MoveRegToReg(SparcInstr.REG_GLOBAL0, SparcInstr.REG_LOCAL0);
 
         // Print label, this label facilitates "true"
         decreaseIndent();
         writeAssembly(SparcInstr.LABEL, compLabel);
         increaseIndent();
         
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Comparison done, result is in %l0, store it in the resultSto
         StoreValueIntoSto(SparcInstr.REG_LOCAL0, resultSto);
 
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
     public String LoadString(String string)
     {
     	// .section ".rodata"
     	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.RODATA_SEC);
 
     	// str_(str_count): .asciz "string literal" 
     	writeAssembly(SparcInstr.RO_DEFINE, ".str_"+str_count, SparcInstr.ASCIZ_DIR, quoted(string));
 
     	// .section ".text"
     	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.TEXT_SEC);
 
         // .align 4
     	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
     	
     	return ".str_"+str_count++;
     	// increment str count
     }
 
     //-------------------------------------------------------------------------
     //      DoUnaryOp
     //-------------------------------------------------------------------------
     public void DoUnaryOp(UnaryOp op, STO operand, STO resultSTO)
     {
     	String operation = "";
 
     	if(op.getName().equals("-")){
     		operation = SparcInstr.NEG_OP;
     		//operand.store(SparcInstr.REG_FRAME, getNextOffset(operand.getType().getSize()));
     		LoadSto(operand, SparcInstr.REG_OUTPUT0);
     		writeAssembly(SparcInstr.TWO_PARAM, operation, SparcInstr.REG_OUTPUT0, SparcInstr.REG_OUTPUT0);
     		resultSTO.store(SparcInstr.REG_FRAME, getNextOffset(resultSTO.getType().getSize()));
     		stackValues.addElement(new StackRecord(currentFunc.peek().getName(), resultSTO.getName(), resultSTO.load()));
     		StoreValueIntoSto(SparcInstr.REG_OUTPUT0, resultSTO);
     	}
     	else if(op.getName().equals("++")){
     		operation = SparcInstr.INC_OP;
     		// load value to l0
     		LoadSto(operand, SparcInstr.REG_LOCAL0);
     		/*
     		//TODO
     		if(((IncOp)op).isPost()) {
   			// backup unincremented value to l1
         		writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.MOV_OP, SparcInstr.REG_LOCAL0, SparcInstr.REG_LOCAL1, "Backup un-incremented value");
         		// increment l0 value
         		writeAssembly(SparcInstr.ONE_PARAM_COMM, operation, SparcInstr.REG_LOCAL0, "Increment the value");
         		resultSTO.store(SparcInstr.REG_FRAME, getNextOffset(resultSTO.getType().getSize()));
         		stackValues.addElement(new StackRecord(currentFunc.peek().getName(), resultSTO.getName(), resultSTO.load()));
         		StoreValueIntoSto(SparcInstr.REG_LOCAL1, resultSTO);
     		}
     		*/
     		// increment l0 value
     		writeAssembly(SparcInstr.ONE_PARAM_COMM, operation, SparcInstr.REG_LOCAL0, "Increment the value");
     		resultSTO.store(SparcInstr.REG_FRAME, getNextOffset(resultSTO.getType().getSize()));
     		stackValues.addElement(new StackRecord(currentFunc.peek().getName(), resultSTO.getName(), resultSTO.load()));
     		StoreValueIntoSto(SparcInstr.REG_LOCAL0, resultSTO);
     	}
     	else if(op.getName().equals("--")){
     		operation = SparcInstr.DEC_OP;
     		LoadSto(operand, SparcInstr.REG_LOCAL0);
 /*
     		//TODO
     		if(((DecOp)op).isPost()) {
     			// backup undecremented value to l1
         		writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.MOV_OP, SparcInstr.REG_LOCAL0, SparcInstr.REG_LOCAL1, "Backup un-incremented value");
         		// increment l0 value
         		writeAssembly(SparcInstr.ONE_PARAM_COMM, operation, SparcInstr.REG_LOCAL0, "Increment the value");
         		resultSTO.store(SparcInstr.REG_FRAME, getNextOffset(resultSTO.getType().getSize()));
         		stackValues.addElement(new StackRecord(currentFunc.peek().getName(), resultSTO.getName(), resultSTO.load()));
         		StoreValueIntoSto(SparcInstr.REG_LOCAL1, resultSTO);
 
     		}
     		*/
     		// decrement l0 value
     		writeAssembly(SparcInstr.ONE_PARAM_COMM, operation, SparcInstr.REG_LOCAL0, "Decrement the value");
     		resultSTO.store(SparcInstr.REG_FRAME, getNextOffset(resultSTO.getType().getSize()));
     		stackValues.addElement(new StackRecord(currentFunc.peek().getName(), resultSTO.getName(), resultSTO.load()));
     		StoreValueIntoSto(SparcInstr.REG_LOCAL0, resultSTO);
     	}
     }
 
     //-------------------------------------------------------------------------
     //      DoBinaryOp
     //-------------------------------------------------------------------------
     public void DoBinaryOp(BinaryOp op, STO operand1, STO operand2, STO resultSto)
     {
     	String binaryOp = "";
         String regOp1 = SparcInstr.REG_LOCAL0;
         String regOp2 = SparcInstr.REG_LOCAL1;
         String comment = operand1.getName() + " and " + operand2.getName();
         boolean isFloatOp = false;
         boolean isCallOp = false;
 
         if(operand1.getType().isFloat() || operand2.getType().isFloat()) {
             regOp1 = SparcInstr.REG_FLOAT0;
             regOp2 = SparcInstr.REG_FLOAT0;
             isFloatOp = true;
         }
         
         // Addition
         if(op.getName().equals("+")){
             if(isFloatOp)
                 binaryOp = SparcInstr.FADDS_OP;
             else
                 binaryOp = SparcInstr.ADD_OP;
             comment = "Addition on " + comment;
         }
         // Subtraction
         else if(op.getName().equals("-")) {
             if(isFloatOp)
                 binaryOp = SparcInstr.FSUBS_OP;
             else
                 binaryOp = SparcInstr.SUB_OP;
             comment = "Subtraction on " + comment;
         }
         // Multiplication
         else if(op.getName().equals("*")) {
             if(isFloatOp)
                 binaryOp = SparcInstr.FMULS_OP;
             else {
                 binaryOp = SparcInstr.MUL_OP;
                 isCallOp = true;
             }
             comment = "Multiplication on " + comment;
         }
         // Divison
         else if(op.getName().equals("/")) {
             if(isFloatOp)
                 binaryOp = SparcInstr.FDIVS_OP;
             else {
                 binaryOp = SparcInstr.DIV_OP;
                 isCallOp = true;
             }
             comment = "Division on " + comment;
         }
         // Following Operations Can't be Float
         // Modulus
         else if(op.getName().equals("%")) {
             binaryOp = SparcInstr.REM_OP;
             isCallOp = true;
             comment = "Modulus on " + comment;
         }
         // Bitwise And
         else if(op.getName().equals("&")) {
             binaryOp = SparcInstr.AND_OP;
             comment = "AND on " + comment;
         }
         // Bitwise Or
         else if(op.getName().equals("|")) {
             binaryOp = SparcInstr.OR_OP;
             comment = "OR on " + comment;
         }
         // Bitwise Xor
         else if(op.getName().equals("^")) {
             binaryOp = SparcInstr.XOR_OP;
             comment = "XOR on " + comment;
         }
 
         // Load operands into registers
         LoadSto(operand1, regOp1);
         LoadSto(operand2, regOp2);
 
         // Call Operator
         if(isCallOp) {
             // If not Float, move arguments into out registers
             if(!isFloatOp) {
                 MoveRegToReg(regOp1, SparcInstr.REG_ARG0);
                 MoveRegToReg(regOp2, SparcInstr.REG_ARG1);
             }
 
             writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.CALL_OP, binaryOp, comment);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
 
             // If not Float, store result in regOp1
             if(!isFloatOp) {
                 MoveRegToReg(SparcInstr.REG_GET_RETURN, regOp1);
             }
 
         }
         // Regular Operator
         else {
             writeAssembly(SparcInstr.THREE_PARAM_COMM, binaryOp, regOp1, regOp2, regOp1, comment);
         }
 
         AllocateSto(resultSto);
         StoreValueIntoSto(regOp1, resultSto);
     }
 
     //--------------------------------e-----------------------------------------
     //      DoIf
     //-------------------------------------------------------------------------
     public void DoIf(STO condition)
     {
         // !----if <condition>----
     	writeComment("if " + condition.getName());
     	
     	// create if label, increment the count and add to stack
     	String ifLabel = ".ifL_" + ifLabel_count;
     	ifLabel_count++;
     	stackIfLabel.add(ifLabel);
     	
         // Load condition into %l0 for comparison
         LoadSto(condition, SparcInstr.REG_LOCAL0);
 
     	// cmp %l0, %g0
     	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, SparcInstr.REG_LOCAL0, SparcInstr.REG_GLOBAL0);
     	// be IfL1! Opposite logic
     	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.BE_OP, ifLabel);
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
