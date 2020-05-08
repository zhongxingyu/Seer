 //-------------------------------------------------------------------------
 //
 //      AssemblyCodeGenerator
 //
 //-------------------------------------------------------------------------
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 public class AssemblyCodeGenerator {
 
     private static final boolean DEBUG = false;
 
     private final String COMPILER_IDENT = "WRC 1.0";
     private int indent_level = 0;
 
     private Stack<FuncSTO>      currentFunc;
     private Stack<Integer>      stackPointer;
     private Stack<String>       stackIfLabel;
     private Stack<String>       stackWhileLabel;
     private Stack<String>       stackBoolLabel;
     private Stack<StoPair>      globalInitStack;
     private Stack<StoPair> 		localStaticInitStack;
     private Vector<StackRecord> stackValues;
     private HashMap<Float, String> storedFloats;
 
     // Error Messages
     private static final String ERROR_IO_CLOSE     = "Unable to close fileWriter";
     private static final String ERROR_IO_CONSTRUCT = "Unable to construct FileWriter for file %s";
     private static final String ERROR_IO_WRITE     = "Unable to write to fileWriter";
 
     // FileWriter
     private FileWriter fileWriter;
     
     // Output file headerstackWhileLabel
     private static final String FILE_HEADER = 
         "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!n\n" + 
         "!!\n" +
         "!!      Generated %s\n" + 
         "!!\n" +
         "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!n\n";
     
     private int str_count = 0;
     private int float_count = 0;
     private int ifLabel_count = 0;
     private int coutLabel_count = 0;
     private int whileLabel_count = 0;
     private int compLabel_count = 0;
     private int boolLabel_count = 0;
         
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
 
         currentFunc     = new Stack<FuncSTO>();
         stackPointer    = new Stack<Integer>();
         stackIfLabel    = new Stack<String>();
         stackWhileLabel = new Stack<String>();
         stackBoolLabel  = new Stack<String>();
         globalInitStack = new Stack<StoPair>();
         localStaticInitStack = new Stack<StoPair>();
         stackValues     = new Vector<StackRecord>();
         storedFloats    = new HashMap<Float, String>();
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
 
     public void blank()
     {
         writeAssembly(SparcInstr.BLANK_LINE);
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
         writeComment("|-------------------------------------------------------------------------");
         writeComment("|      " + comment);
         writeComment("|-------------------------------------------------------------------------");
         writeAssembly(SparcInstr.BLANK_LINE);
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
     //      DoLocalStaticDecl
     //-------------------------------------------------------------------------
     public void DoLocalStaticDecl(STO varSto, STO valueSto)
     {
         writeComment("Declare Global: " + varSto.getName());
         
         // .section ".bss"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.BSS_SEC);
 
         // .align 4
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
 
         // <id>: .skip size of type of varSto
         decreaseIndent();
         writeAssembly(SparcInstr.GLOBAL_DEFINE, varSto.getName(), SparcInstr.SKIP_DIR, String.valueOf(varSto.getType().getSize()));
         increaseIndent();
 
         // set the base and offset to the sto
         varSto.store(SparcInstr.REG_GLOBAL0, varSto.getName());
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // If valueSto is Null, create a 0 sto for it (we're in bss, auto init to 0)
         if(valueSto.isNull())
             if(varSto.getType().isArray() && valueSto.isArrEle()) {
                 // this should never happen, non-inited arrays go to BuildType() 
             }
 
             valueSto = new ConstSTO("0", new IntType(), 0.0);
         
         // Push these for later to initialize when main() starts
         localStaticInitStack.push(new StoPair(varSto, valueSto));
 
         stackValues.addElement(new StackRecord("local static", varSto.getName(), varSto.load()));
     }
     //-------------------------------------------------------------------------
     //      DoGlobalDecl
     //-------------------------------------------------------------------------
     public void DoGlobalDecl(STO varSto, STO valueSto)
     {
         writeComment("Declare Global: " + varSto.getName());
 
         // .global <id>
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.GLOBAL_DIR, varSto.getName());
 
         // .section ".bss"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.BSS_SEC);
 
         // .align 4
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
 
         // <id>: .skip size of type of varSto
         decreaseIndent();
         writeAssembly(SparcInstr.GLOBAL_DEFINE, varSto.getName(), SparcInstr.SKIP_DIR, String.valueOf(varSto.getType().getSize()));
         increaseIndent();
 
         // set the base and offset to the sto
         varSto.store(SparcInstr.REG_GLOBAL0, varSto.getName());
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // If valueSto is Null, create a 0 sto for it (we're in bss, auto init to 0)
         //if(valueSto.isNull())
             //if(varSto.getType().isArray() && valueSto.isArrEle()) {
                 // this should never happen, non-inited arrays go to BuildType() 
             //}
 
             //valueSto = new ConstSTO("0", new IntType(), 0.0);
         
         // Push these for later to initialize when main() starts
         globalInitStack.push(new StoPair(varSto, valueSto));
 
         stackValues.addElement(new StackRecord("global", varSto.getName(), varSto.load()));
     }
     //-------------------------------------------------------------------------
     //      MakeLocalStaticInitGuard
     //-------------------------------------------------------------------------
     public void MakeLocalStaticInitGuard(String label)
     {	
         // !----Create .init for local static init guard----
         writeComment("Create .init for local static init guard");
 
         // .section ".bss"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.BSS_SEC);
 
         // .align 4
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, String.valueOf(4));
 
         // .init: .skip 4
         decreaseIndent();
         writeAssembly(SparcInstr.GLOBAL_DEFINE, label, SparcInstr.SKIP_DIR, String.valueOf(4));
         writeAssembly(SparcInstr.GLOBAL_DEFINE, label+"_flag", SparcInstr.SKIP_DIR, String.valueOf(4));
         increaseIndent();
 
         writeAssembly(SparcInstr.BLANK_LINE);
     }
     //-------------------------------------------------------------------------
     //      MakeGlobalInitGuard
     //-------------------------------------------------------------------------
     public void MakeGlobalInitGuard()
     {
         // !----Create .init for global init guard----
         writeComment("Create .init for global init guard");
 
         // .section ".bss"
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.BSS_SEC);
 
         // .align 4
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, String.valueOf(4));
 
         // .init: .skip 4
         decreaseIndent();
         writeAssembly(SparcInstr.GLOBAL_DEFINE, ".init", SparcInstr.SKIP_DIR, String.valueOf(4));
         increaseIndent();
 
         writeAssembly(SparcInstr.BLANK_LINE);
     }
     //-------------------------------------------------------------------------
     //      DoLocalStaticInit
     //-------------------------------------------------------------------------
     public void DoLocalStaticInit(String label)
     {
     	String flag = label+"_flag";
         // !----Initialize Local statics----
         writeCommentHeader("Initialize Local statics");
 
         // Do Init Guard
 
         writeComment("Check if init has been completed previously");
         // set flag, %l0
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, flag, SparcInstr.REG_LOCAL0, "Set " + flag + " label into %l0");
 
         // ld [%l0], %l1
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL0), SparcInstr.REG_LOCAL1, "Load " + flag +" value into %l1");
 
         //  %l1, %g0
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.CMP_OP, SparcInstr.REG_LOCAL1, SparcInstr.REG_GLOBAL0, "Compare " + flag + " to 0");
 
         // bne .init_done ! Global initialization guard
         writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BNE_OP, ".skipInit", "If init has been done, skip init");
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         
 /*        // set 1, %l1 ! Branch delay slot
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL1, "Set .init to 1, indicating it has been done");
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL1, bracket(SparcInstr.REG_LOCAL0), "Store value into .init mem location");
         writeAssembly(SparcInstr.BLANK_LINE);*/
 
         // Perform Initializations
 
         // Loop through all the initialization pairs on the stack
         for(Enumeration<StoPair> e = globalInitStack.elements(); e.hasMoreElements(); ) {
 
             StoPair stopair = e.nextElement();
             STO varSto = stopair.getVarSto();
             STO valueSto = stopair.getValueSto();
 
             if(!valueSto.isNull()) {
                 if(valueSto.isConst() && ((ConstSTO) valueSto).getIntValue() == 0) {
                     // Do nothing, auto initialized to 0 on bss
 
                     // But if array, create an empty list of the right size
                     if(varSto.getType().isArray()) {
 
                         ArrayType arrayType = (ArrayType) varSto.getType();
                         Vector<STO> arrayElements = arrayType.getElementList();
 
                         // Initialize arrayElement Vector with empty varstos
                         for(int i = 0; i < arrayType.getDimensionSize(); i++) {
                             arrayElements.addElement(new VarSTO(varSto.getName() + "[" + i + "]", arrayType.getElementType())); 
                         }
                     }
                 }
 
                 // Initialize the value
                 else {
 
                     writeComment("Initializing: " + varSto.getName() + " = " + valueSto.getName());
 
                     // If array, then do array stuff yo
                     if(varSto.getType().isArray()) {
                         ArrayType arrayType = (ArrayType) varSto.getType();
 
                         if(valueSto.isArrEle()) {
                             // if it's array, do array ele init
                             Vector<STO> varElements = arrayType.getElementList();
                             Vector<STO> valueElements = ((ArrEleSTO)valueSto).getArrayElements();
 
                             for(int i = 0; i < valueElements.size(); i++) {
                                 DoAssignExpr(varElements.elementAt(i), valueElements.elementAt(i));
                             }
                         }
                         else {
                             // I don't think this is possible
                         }
 
                     } 
                     else {    
                     	// do normal var init
 
                     	DoAssignExpr(varSto, valueSto);
 
                     }
                 }
             }
         }
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL1, "Set .init to 1, indicating it has been done");
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, flag, SparcInstr.REG_LOCAL0, "Set " + flag + " label into %l0");
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL1, bracket(SparcInstr.REG_LOCAL0), "Store value into .init mem location");
         writeAssembly(SparcInstr.BLANK_LINE);
         
         // .init_done:
         decreaseIndent();
         writeAssembly(SparcInstr.LABEL, ".skipInit");
         writeAssembly(SparcInstr.BLANK_LINE);
         increaseIndent();
 
     }
     //-------------------------------------------------------------------------
     //      DoGlobalInit
     //-------------------------------------------------------------------------
     public void DoGlobalInit()
     {
         // !----Initialize Globals----
         writeCommentHeader("Initialize Globals");
 
         // Do Init Guard
 
         writeComment("Check if init has been completed previously");
         // set .init, %l0
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, ".init", SparcInstr.REG_LOCAL0, "Set .init label into %l0");
 
         // ld [%l0], %l1
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL0), SparcInstr.REG_LOCAL1, "Load .init value into %l1");
 
         //  %l1, %g0
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.CMP_OP, SparcInstr.REG_LOCAL1, SparcInstr.REG_GLOBAL0, "Compare .init to 0");
 
         // bne .init_done ! Global initialization guard
         writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BNE_OP, ".init_done", "If init has been done, skip init");
 
         // set 1, %l1 ! Branch delay slot
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL1, "Set .init to 1, indicating it has been done");
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, SparcInstr.REG_LOCAL1, bracket(SparcInstr.REG_LOCAL0), "Store value into .init mem location");
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Perform Initializations
 
         // Loop through all the initialization pairs on the stack
         for(Enumeration<StoPair> e = globalInitStack.elements(); e.hasMoreElements(); ) {
 
         	StoPair stopair = e.nextElement();
         	STO varSto = stopair.getVarSto();
         	STO valueSto = stopair.getValueSto();
 
         	if(!valueSto.isNull()) {
         		if(valueSto.isConst() && ((ConstSTO) valueSto).getIntValue() == 0) {
         			// Do nothing, auto initialized to 0 on bss
 
         			// But if array, create an empty list of the right size
         			if(varSto.getType().isArray()) {
 
         				ArrayType arrayType = (ArrayType) varSto.getType();
         				Vector<STO> arrayElements = arrayType.getElementList();
 
         				// Initialize arrayElement Vector with empty varstos
         				for(int i = 0; i < arrayType.getDimensionSize(); i++) {
         					arrayElements.addElement(new VarSTO(varSto.getName() + "[" + i + "]", arrayType.getElementType())); 
         				}
         			}
         			else if(varSto.getType().isStruct()){
         				StructType type = (StructType) varSto.getType();
         				Vector<STO> fields = type.getFields();
         				
         				for(STO sto : fields) {
         					fields.addElement(new VarSTO(varSto.getName(), sto.getType()));
         				}
         			}
         		}
 
         		// Initialize the value
         		else {
 
         			writeComment("Initializing: " + varSto.getType() + " " + valueSto.getName());
 
         			// If array, then do array stuff yo
         			if(varSto.getType().isArray()) {
 
         				writeComment("Initializing Array: " + varSto.getName());
         				ArrayType arrayType = (ArrayType) varSto.getType();
 
         				// 
         				if(valueSto.isArrEle()) {
         					// if it's array, do array ele init
         					Vector<STO> varElements = arrayType.getElementList();
         					//Vector<STO> valueElements = ((ArrEleSTO)valueSto).getArrayElements();
 
         					String indexReg = SparcInstr.REG_LOCAL6;
         					String addrReg = SparcInstr.REG_LOCAL4; 
         					MoveRegToReg(SparcInstr.REG_GLOBAL0, indexReg);
         					//writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), indexReg, "Use %l5 for incrementing counter by 1");
         					writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL5, "Use %l5 for incrementing counter by 1");
 
         					for(int i = 0; i < varElements.size(); i++) {
         						ConstSTO value = null;
         						if(varElements.elementAt(i).isConst()) {
         							value = (ConstSTO) varElements.elementAt(i);
         						}
         						writeCommentHeader("Initializing " + varSto.getName() + "[" + i + "] - " + value.getName() + " - " + value.getIntValue() );
         						//writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) valueSto).getIntValue()), valueReg);
 
         						DoLiteral(value);
 
         						MoveRegToReg(indexReg, addrReg);
 
         						// TODO NEED TO ACCOUNT FOR FLOATS
         						GetArrayElementAddr(varSto, addrReg);
         						StoreStoValueIntoAddr(varElements.elementAt(i), SparcInstr.REG_LOCAL3, addrReg);
         					//	stackValues.addElement(new StackRecord("global", value.getName(), value.load()));
         						writeAssembly(SparcInstr.THREE_PARAM, SparcInstr.ADD_OP, indexReg, SparcInstr.REG_LOCAL5, indexReg, "Increment index counter");
         					}
         				}
         				else {
         					// I don't think this is possible
         				}
 
         			} 
         			else if(varSto.isStructdef()) {
         				Vector<STO> fields = ((StructType)varSto.getType()).getFields();
         				String indexReg = SparcInstr.REG_LOCAL6;
         				String addrReg = SparcInstr.REG_LOCAL4; 
         				MoveRegToReg(SparcInstr.REG_GLOBAL0, indexReg);
         				//writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), indexReg, "Use %l5 for incrementing counter by 1");
         				writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL5, "Use %l5 for incrementing counter by 1");
 
         				for(int i = 0; i < fields.size(); i++) {
         					VarSTO value = null;
         					if(fields.elementAt(i).isVar()) {
         						value = (VarSTO) fields.elementAt(i);
         					}
         					writeCommentHeader("Initializing " + varSto.getName() + value.getName());
         					//writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) valueSto).getIntValue()), valueReg);
 
         					//DoLiteral(value);
 
         					MoveRegToReg(indexReg, addrReg);
 
         					// TODO NEED TO ACCOUNT FOR FLOATS
         					GetArrayElementAddr(varSto, addrReg);
         					StoreStoValueIntoAddr(fields.elementAt(i), SparcInstr.REG_LOCAL3, addrReg);
         					//stackValues.addElement(new StackRecord("global", value.getName(), value.load()));
         					writeAssembly(SparcInstr.THREE_PARAM, SparcInstr.ADD_OP, indexReg, SparcInstr.REG_LOCAL5, indexReg, "Increment index counter");
         				}
         			}
         			else {    
         				// do normal var init
 
         				DoAssignExpr(varSto, valueSto);
 
         			}
         		}
         	}
         }
 
         // .init_done:
         decreaseIndent();
         writeAssembly(SparcInstr.LABEL, ".init_done");
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
 
         //////////////////////////////////////////////////////////////////////////////// 
         //  5. [Callee] Allocate space for local variables (adjust stack pointer) - DONE
         //////////////////////////////////////////////////////////////////////////////// 
 
         // save %sp, %g1, %sp
         writeAssembly(SparcInstr.THREE_PARAM, SparcInstr.SAVE_OP, SparcInstr.REG_STACK, SparcInstr.REG_GLOBAL1, SparcInstr.REG_STACK);
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Do global inits if doing main
         if(funcSto.getName().equals("main")) {
             DoGlobalInit();
         }
 
         // TODO: Receive and store values transferred via param registers
 
         writeComment("Get PARAMS from %i0 registers and allocate on local stack");
 
         Vector<ParamSTO> params = ((FuncPtrType) funcSto.getType()).getParameters();
 
         //if(DEBUG) {
             //writeComment("AssemblyCodeGenerator.DoFuncStart()");
             //writeComment("Function: " + funcSto.getName());
             //writeComment("numOfParams: " + ((FuncPtrType) funcSto.getType()).getNumOfParams());
             //writeComment("getParameters.size(): " + params.size());
         //}
 
         for(int i = 0; i < params.size(); i++) {
             ParamSTO paramSto = params.elementAt(i);
             //writeComment("parameter: " + paramSto.getName() + " is: " + paramSto.getClass().getName());
 
             AllocateSto(paramSto);
             StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
 
             // 1. [PASS] value param as value arg
             // 3. [PASS] reference param as value arg
             // 5. [PASS] local variable as value arg
             // 7. [PASS] global variable as value arg
             if(!paramSto.isPassByReference()) {
                 blank();
                 writeComment("[PASS] 1,3,5,7 - as value arg");
                 //writeComment("[PASS] 1 - value param as value arg");
                 StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
             }
             // 2. [PASS] value param as reference arg
             // 4. [PASS] reference param as reference arg
             // 6. [PASS] local variable as reference arg
             // 8. [PASS] global variable as reference arg
             else {
                 blank();
                 writeComment("[PASS] 2,4,6,8 - as reference arg");
                // writeComment("[PASS] 2 - value param as reference arg");
                 StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
             }
             /*
             else if(!paramSto.isPassByReference()) {
                 blank();
                 writeComment("[PASS] 3 - reference param as value arg");
                 StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
             }
             else if(paramSto.isPassByReference()) {
                 blank();
                 writeComment("[PASS] 4 - reference param as reference arg");
                 StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
             }
             else if(!paramSto.isPassByReference()) {
                 blank();
                 writeComment("[PASS] 7 - global variable as value arg");
                 StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
             }
             else if(paramSto.isPassByReference()) {
                 blank();
                 writeComment("[PASS] 8 - global variable as reference arg");
                 StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
             }
             else if(!paramSto.isPassByReference()) {
                 blank();
                 writeComment("[PASS] 5 - local variable as value arg");
                 StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
             }
 
             else if(!paramSto.isPassByReference()) {
                 blank();
                 writeComment("[PASS] 6 - local variable as reference arg");
                 StoreValueIntoSto(SparcInstr.PARAM_REGS[i], paramSto);
             }
             */
         }
 
 
         //////////////////////////////////////////////////////////////////////////////// 
         //  6. [Callee] Save registers used by called subroutine (if Callee-Save convention) - DONE - have caller do it
         //////////////////////////////////////////////////////////////////////////////// 
 
         //////////////////////////////////////////////////////////////////////////////// 
         //  7. [Callee] Change the frame pointer to refer to the new stack frame - DONE auto
         //////////////////////////////////////////////////////////////////////////////// 
 
         //////////////////////////////////////////////////////////////////////////////// 
         //  8. [Callee] Execute initialization code for local vars/objects that are initialized
         //////////////////////////////////////////////////////////////////////////////// 
 
     }
 
     //-------------------------------------------------------------------------
     //      DoFuncFinish
     //-------------------------------------------------------------------------
     public void DoFuncFinish(FuncSTO funcSto)
     {
         // Perform return/restore
         writeAssembly(SparcInstr.BLANK_LINE);
 
         //////////////////////////////////////////////////////////////////////////////// 
         // 10. [Callee] Execute any finalization code for any local objects - DONE
         //////////////////////////////////////////////////////////////////////////////// 
 
         //////////////////////////////////////////////////////////////////////////////// 
         // 11. [Callee] Deallocate the local variables / restore the stack pointer - DONE
         //////////////////////////////////////////////////////////////////////////////// 
 
         //////////////////////////////////////////////////////////////////////////////// 
         // 12. [Callee] Restore other saved registers / restore the frame pointer - DONE
         //////////////////////////////////////////////////////////////////////////////// 
 
         //////////////////////////////////////////////////////////////////////////////// 
         // 13. [Callee] Restore the program counter from saved return address (return/rts) - DONE
         //////////////////////////////////////////////////////////////////////////////// 
 
         // Also executed in DoReturn()
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
     //      DoFuncCall
     //-------------------------------------------------------------------------
     public void DoFuncCall(STO funcSto, Vector<STO> args, STO returnSto)
     {
         // returnSto is VarSTO if returnByRef, ExprSTO otherwise
         // called funcSto funcSto for convience but it could be a VarSto (pointer)
         // Just operate on it's type field
 
         writeCommentHeader("Function Call: " + funcSto.getName());
 
         Vector<ParamSTO> params = ((FuncPtrType) funcSto.getType()).getParameters();
 
         if(DEBUG) {
             System.out.println("AssemblyCodeGenerator.DoFuncCall()");
             System.out.println("Function: " + funcSto.getName());
             System.out.println("numOfArgs: " + args.size());
             System.out.println("getParameters.size(): " + params.size());
         }
         //////////////////////////////////////////////////////////////////////////////// 
         //  1. [Caller] Allocate space for and copy arguments - TODO:
         //////////////////////////////////////////////////////////////////////////////// 
 
         for(int i = 0; i < args.size(); i++) {
             STO argSto = args.elementAt(i);
             ParamSTO paramSto = params.elementAt(i);
             
             if(!argSto.isInMemory()) {
                 AllocateSto(argSto);
             }
 
 
             //writeComment("paramSto:\tisReference:\t" + paramSto.isReference() + "\tisPassByRef:\t" + paramSto.isPassByReference());
 
             ///////// Load a value STO
             // LoadStoValue(argSto, SparcInstr.ARG_REGS[i]);               // Load Value from stack location (that is in a sto)
 
             ///////// Load a reference STO
             // LoadStoAddr(argSto, SparcInstr.ARG_REGS[i]);                // Load the address from a Sto
 
             ///////// Load a reference STO as a reference
             // LoadStoAddr(argSto, SparcInstr.REG_LOCAL0);                 // loads the address of the reference into register
             // LoadValueFromAddr(SparcInstr.REG_LOCAL0, SparcInstr.ARG_REGS[i])        // loads the address stored in the address into register
                 
 
 
             // 1. [PASS] value param as value arg
             if(argSto.isParam() && !paramSto.isPassByReference()) {
                 blank();
                 writeComment("1. [PASS] value param as value arg");
                 LoadStoValue(argSto, SparcInstr.ARG_REGS[i]);
                 
             }
             // 2. [PASS] value param as reference arg
             else if(argSto.isParam() && paramSto.isPassByReference()) {
                 blank();
                 writeComment("2. [PASS] value param as reference arg");
                 LoadStoAddr(argSto, SparcInstr.ARG_REGS[i]);               // Load the address from the sto into %o0
             }
             // 3. [PASS] reference param as value arg
             
             else if(argSto.isParam() && ((ParamSTO) argSto).isPassByReference() && !paramSto.isPassByReference()) {
                 blank();
                 writeComment("3. [PASS] reference param as value arg");
                 LoadStoValue(argSto, SparcInstr.REG_LOCAL0);                    // loads the value (which is an address) from the Sto
                 LoadValueFromAddr(SparcInstr.REG_LOCAL0, SparcInstr.ARG_REGS[i]);       // loads the value stored address contained in %l0 into %o0
             }
             // 4. [PASS] reference param as reference arg
             else if(argSto.isParam() && ((ParamSTO) argSto).isPassByReference() && paramSto.isPassByReference()) {
                 blank();
                 writeComment("4. [PASS] reference param as reference arg");
                 LoadStoValue(argSto, SparcInstr.REG_LOCAL0);                    // loads the value (which is an address) from the Sto
                 LoadValueFromAddr(SparcInstr.REG_LOCAL0, SparcInstr.ARG_REGS[i]);      // loads the value stored address contained in %l0 into %o0
             }
 
             if(argSto.isGlobal()) {
                 blank();
 
                 // 7. [PASS] global variable as value arg
                 if(!paramSto.isPassByReference()) {
                     writeComment("7. [PASS] global variable as value arg");
                     LoadStoValue(argSto, SparcInstr.ARG_REGS[i]);
                 }
                 // 8. [PASS] global variable as reference arg
                 else {
                     writeComment("8. [PASS] global variable as reference arg");
                     LoadStoAddr(argSto, SparcInstr.ARG_REGS[i]);
 
                 }
             }
 
             // 5. [PASS] local variable as value arg
             else if(!paramSto.isPassByReference()) {
                 blank();
                 writeComment("5. [PASS] local variable as value arg");
                 LoadStoValue(argSto, SparcInstr.ARG_REGS[i]);    
             }
 
             // 6. [PASS] local variable as reference arg
             else {
                 writeComment("6. [PASS] local variable as reference arg");
                 LoadStoAddr(argSto, SparcInstr.ARG_REGS[i]);
             }
 
         }
 
         //////////////////////////////////////////////////////////////////////////////// 
         //  3. [Caller] Save registers use by the calling subroutine (if Caller-Save convention) - TODO:
         //////////////////////////////////////////////////////////////////////////////// 
 
         //////////////////////////////////////////////////////////////////////////////// 
         //  4. [Caller] Subroutine call - DONE
         //////////////////////////////////////////////////////////////////////////////// 
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, funcSto.getName()); 
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
 
 
         // Now we can write the code for after the return, which is store the return value to stack
         // If the return type isn't void, save the return value
 
         //////////////////////////////////////////////////////////////////////////////// 
         // 14. [Caller] Copy return value out of return value location - DONE
         //////////////////////////////////////////////////////////////////////////////// 
 
         if(!returnSto.getType().isVoid()) {
 
             writeComment("Save return from " + funcSto.getName() + " onto stack");
 
             //////////////////////////////////////////////////////////////////////////////// 
             //  2. [Caller] Allocate space for return value (if on the stack) - DONE
             //////////////////////////////////////////////////////////////////////////////// 
 
             String offset = getNextOffset(returnSto.getType().getSize());
             returnSto.store(SparcInstr.REG_FRAME, offset);
             stackValues.addElement(new StackRecord(currentFunc.peek().getName(), returnSto.getName(), returnSto.load()));
 
             if(((FuncPtrType) funcSto.getType()).getReturnByRef()) {
                 // TODO: Save the return value for return by reference
                 // LoadStoAddr(returnSto, SparcInstr.REG_SET_RETURN);
             }
             else { 
                 // If return type is float, put into %f0 (possibly fitos)
                 if(((FuncPtrType) funcSto.getType()).getReturnType().isFloat()) {
                     StoreValueIntoSto(SparcInstr.REG_FLOAT0, returnSto);
                 }
                 // return type is not float, store into %i0 from %o0
                 else {
                     StoreValueIntoSto(SparcInstr.REG_GET_RETURN, returnSto);
                 }
             }
         }
 
         //////////////////////////////////////////////////////////////////////////////// 
         // 15. [Caller] Deallocate argument space and return value location (if on the stack) - TODO:
         //////////////////////////////////////////////////////////////////////////////// 
     }
 
     
     //-------------------------------------------------------------------------
     //      DoReturn
     //-------------------------------------------------------------------------
     public void DoReturn(FuncSTO funcSto, STO returnSto)
     {
         writeCommentHeader("Set return value (if needed) and return");
 
         //////////////////////////////////////////////////////////////////////////////// 
         //  9. [Callee] Place return value into return value location - DONE
         //////////////////////////////////////////////////////////////////////////////// 
 
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
                     LoadStoValue(returnSto, SparcInstr.REG_FLOAT0);
                 // return type is not float, store into %i0
                 else
                     LoadStoForceValue(returnSto, SparcInstr.REG_SET_RETURN);
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
             LoadStoValue(sto, SparcInstr.REG_ARG1);
 
             // call printf
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
             writeAssembly(SparcInstr.BLANK_LINE);
         }
         
         else if(sto.getType().isBool()) {
             // set _intFmt, %o0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.BOOLFMT, SparcInstr.REG_ARG0);
 
             // Set the condition STO into LOCAL0
             LoadStoValue(sto, SparcInstr.REG_LOCAL0);
             String coutIfLabel = ".coutIfL_" + coutLabel_count;
             String coutElseLabel = ".coutElseL_" + coutLabel_count;
             coutLabel_count++;
 
             // If the condition is true, don't branch and load "true", if false, branch to end of if and load "false"
             // value == 0
             writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.CMP_OP, SparcInstr.REG_LOCAL0, SparcInstr.REG_GLOBAL0, "Compare boolean value " + sto.getName() + " to 0");
             writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BE_OP, coutIfLabel, "If <cond> is true, don't branch, if false, branch");
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
             writeAssembly(SparcInstr.BLANK_LINE);
 
             // If Code: Load "true" into %o1 here
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.BOOLT, SparcInstr.REG_ARG1);
             // Branch to end of else block
             writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BA_OP, coutElseLabel, "Did if code, branch always to bottom of else");
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
             writeAssembly(SparcInstr.BLANK_LINE);
 
             // Print label
             decreaseIndent();
             writeAssembly(SparcInstr.LABEL, coutIfLabel);
             increaseIndent();
             
             // Else code: load "false" into %o1 here
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, SparcInstr.BOOLF, SparcInstr.REG_ARG1);
             
             // Else done, print label
             decreaseIndent();
             writeAssembly(SparcInstr.LABEL, coutElseLabel);
             increaseIndent();
             
             // call printf
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTF);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
             writeAssembly(SparcInstr.BLANK_LINE);
         }
 
         else if(sto.getType().isFloat()) {
             // ld [sto] %f0
             LoadStoValue(sto, SparcInstr.REG_FLOAT0);
 
             // call printFloat
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.PRINTFLOAT);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
             writeAssembly(SparcInstr.BLANK_LINE);
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
             writeAssembly(SparcInstr.BLANK_LINE);
             
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
             writeAssembly(SparcInstr.BLANK_LINE);
         }
     }
 
     //-------------------------------------------------------------------------
     //      DoVarDecl - allocates memory on stack and stores location in STO
     //-------------------------------------------------------------------------
     public void DoVarDecl(STO sto)
     {
         AllocateSto(sto);
         
         // Array (TODO: In Phase 2)
         if(sto.getType().isArray()){
         	writeComment("Initializing Array: " + sto.getName());
         	ArrayType arrayType = (ArrayType) sto.getType();
         	// if it's array, do array ele init
         	Vector<STO> varElements = arrayType.getElementList();
         	//Vector<STO> valueElements = ((ArrEleSTO)valueSto).getArrayElements();
 
         	String indexReg = SparcInstr.REG_LOCAL6;
         	String addrReg = SparcInstr.REG_LOCAL4; 
         	MoveRegToReg(SparcInstr.REG_GLOBAL0, indexReg);
         	//writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), indexReg, "Use %l5 for incrementing counter by 1");
         	writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL5, "Use %l5 for incrementing counter by 1");
 
         	for(int i = 0; i < varElements.size(); i++) {
         		ConstSTO value = (ConstSTO) varElements.elementAt(i);
         		writeCommentHeader("Initializing " + sto.getName() + "[" + i + "] - " + value.getName() + " - " + value.getIntValue() );
         		//writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) valueSto).getIntValue()), valueReg);
 
         		//DoLiteral(value);
 
         		MoveRegToReg(indexReg, addrReg);
 
         		// TODO NEED TO ACCOUNT FOR FLOATS
         		GetArrayElementAddr(sto, addrReg);
         		StoreStoValueIntoAddr(varElements.elementAt(i), SparcInstr.REG_LOCAL3, addrReg);
         		//stackValues.addElement(new StackRecord("global", value.getName(), value.load()));
         		writeAssembly(SparcInstr.THREE_PARAM, SparcInstr.ADD_OP, indexReg, SparcInstr.REG_LOCAL5, indexReg, "Increment index counter");
         	}
         }
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
     //      DoAssignExpr - Sets destSto = valueSto
     //-------------------------------------------------------------------------
     public void DoAssignExpr(STO destSto, STO valueSto)
     {
         writeComment("Assigning " + destSto.getName() + " = " + valueSto.getName());
 
 
         // If valueSto is a constant and not already in memory, then set the value directly
         if((valueSto.isConst()) && (!valueSto.isInMemory())) {
 
             String valueReg = SparcInstr.REG_LOCAL0;
 
             // If constant is float
             if(destSto.getType().isFloat()) {
 
                 valueReg = SparcInstr.REG_FLOAT0;
 
                 // Put float literal into memory
                 String floatLabel = PutFloatInMem(((ConstSTO) valueSto).getFloatValue());
 
                 // Load float into valueReg
                 LoadValueFromLabel(floatLabel, valueReg);
             }
 
             // Not float
             else {
                 // Set the value (integer) into valueReg
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) valueSto).getIntValue()), valueReg);
             }
 
             StoreValueIntoSto(valueReg, destSto);
         }
         // Not a constant that isn't in memory, do the load/store method to assign
         else {
             // Load Address of destSto into %l0
             LoadStoAddr(destSto, SparcInstr.REG_LOCAL0);
 
             // Store value in valueSto into destSto, using appropriate type register
             if(destSto.getType().isFloat())
                 StoreStoValueIntoAddr(valueSto, SparcInstr.REG_FLOAT0, SparcInstr.REG_LOCAL0);
             else
                 StoreStoValueIntoAddr(valueSto, SparcInstr.REG_LOCAL1, SparcInstr.REG_LOCAL0);
         }
 
         writeAssembly(SparcInstr.BLANK_LINE);
     }
 
     //-------------------------------------------------------------------------
     //      DoArrayAssignExpr - Sets destSto = valueSto
     //-------------------------------------------------------------------------
     public void DoArrayAssignExpr(STO arraySto, STO destSto, STO valueSto)
     {
         writeComment("Assigning " + destSto.getName() + " = " + valueSto.getName());
 
 
         // If valueSto is a constant and not already in memory, then set the value directly
         if((valueSto.isConst()) && (!valueSto.isInMemory())) {
 
             String valueReg = SparcInstr.REG_LOCAL0;
 
             // If constant is float
             if(destSto.getType().isFloat()) {
 
                 valueReg = SparcInstr.REG_FLOAT0;
 
                 // Put float literal into memory
                 String floatLabel = PutFloatInMem(((ConstSTO) valueSto).getFloatValue());
 
                 // Load float into valueReg
                 LoadValueFromLabel(floatLabel, valueReg);
             }
 
             // Not float
             else {
                 // Set the value (integer) into valueReg
                 writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(((ConstSTO) valueSto).getIntValue()), valueReg);
             }
 
             StoreValueIntoSto(valueReg, destSto);
         }
         // Not a constant that isn't in memory, do the load/store method to assign
         else {
             // Load Address of destSto into %l0
             LoadStoAddr(destSto, SparcInstr.REG_LOCAL0);
 
             // Store value in valueSto into destSto, using appropriate type register
             if(destSto.getType().isFloat())
                 StoreStoValueIntoAddr(valueSto, SparcInstr.REG_FLOAT0, SparcInstr.REG_LOCAL0);
             else
                 StoreStoValueIntoAddr(valueSto, SparcInstr.REG_LOCAL1, SparcInstr.REG_LOCAL0);
         }
 
         writeAssembly(SparcInstr.BLANK_LINE);
     }
 
     //-------------------------------------------------------------------------
     //      setParamAddr  - Puts the stack param memory address for param i into register
     //-------------------------------------------------------------------------
     public String setParamAddr(int i, String reg)
     {
         writeComment("Set param address for param" + i + " into " + reg);
 
         int offset = 68 + (4 * i);
 
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(offset), reg);
         writeAssembly(SparcInstr.THREE_PARAM, SparcInstr.ADD_OP, SparcInstr.REG_FRAME, reg, reg, "Add offset/name to base reg " + reg);
 
         return String.valueOf(offset);
     }
 
     //-------------------------------------------------------------------------
     //      LoadStoForceValue - Load value of sto into reg, no automatic dereference - Uses %l7 as a temp - "takes out of memory"
     //-------------------------------------------------------------------------
     public void LoadStoForceValue(STO sto, String reg)
     {
         //writeComment("Load " + sto.getName() + " into " + reg);
         // PUT ADDRESS OF STO INTO tmpReg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, sto.getOffset(), SparcInstr.REG_LOCAL7, "Loading " + sto.getName() + "<value> into register " + reg);
 
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, sto.getBase(), SparcInstr.REG_LOCAL7, SparcInstr.REG_LOCAL7, "");
 
         // LOAD VALUE AT ADDRESS INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL7), reg, "Value of " + sto.getName() + " now in " + reg);
         
         if(isFloatReg(reg) && sto.getType().isInt()) {
             writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.FITOS_OP, reg,reg, "Promoting int to float");
             // System.out.println("[DEBUG] FITOSing in LoadSto");
         }
     }
 
     //-------------------------------------------------------------------------
     //      LoadSto - Load value of sto into reg - Uses %l7 as a temp - "takes out of memory"
     //-------------------------------------------------------------------------
     public void LoadStoValue(STO sto, String reg)
     {
         //writeComment("Load " + sto.getName() + " into " + reg);
         // PUT ADDRESS OF STO INTO tmpReg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, sto.getOffset(), SparcInstr.REG_LOCAL7, "Loading " + sto.getName() + "<value> into register " + reg);
 
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, sto.getBase(), SparcInstr.REG_LOCAL7, SparcInstr.REG_LOCAL7, "");
 
         // LOAD VALUE AT ADDRESS INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL7), reg, "Value of " + sto.getName() + " now in " + reg);
         
         if(sto.isReference()) {
             writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(reg), reg, "");
         }
 
         if(isFloatReg(reg) && sto.getType().isInt()) {
             writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.FITOS_OP, reg,reg, "Promoting int to float");
             // System.out.println("[DEBUG] FITOSing in LoadSto");
         }
     }
 
     //-------------------------------------------------------------------------
     //      LoadSto - Load value of sto into reg - Uses %l7 as a temp - "takes out of memory"
     //-------------------------------------------------------------------------
     /*
     public void LoadStoValue(STO sto, String reg)
     {
         writeComment("Load " + sto.getName() + " into " + reg);
         // PUT ADDRESS OF STO INTO tmpReg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, sto.getOffset(), SparcInstr.REG_LOCAL7, "Put the offset/name of " + sto.getName() + " into " + SparcInstr.REG_LOCAL7);
 
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, sto.getBase(), SparcInstr.REG_LOCAL7, SparcInstr.REG_LOCAL7, "Add offset/name to base reg " + SparcInstr.REG_LOCAL7);
 
         // LOAD VALUE AT ADDRESS INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL7), reg, "Load value of " + sto.getName() + " into " + reg);
         if(isFloatReg(reg) && sto.getType().isInt()) {
             writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.FITOS_OP, reg,reg, "Promoting");
             // System.out.println("[DEBUG] FITOSing in LoadSto");
         }
     }
     */
 
     //-------------------------------------------------------------------------
     //      LoadValueFromAddr - Loads a value into a register via an address stored in addrReg
     //-------------------------------------------------------------------------
     public void LoadValueFromAddr(String addrReg, String reg)
     {
         // Address is in addrReg, so load value from that address into reg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(reg), reg, "Load value from address in  " + reg + " into " + reg);
     }
 
     //-------------------------------------------------------------------------
     //      LoadValueFromLabel - Loads a value into a register via a label - usees %l7 as tmp
     //-------------------------------------------------------------------------
     public void LoadValueFromLabel(String label, String reg)
     {
         // Set label to %l0, then load value from the label as an address into reg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, label, SparcInstr.REG_LOCAL7, "Put label " + label + " address into " + SparcInstr.REG_LOCAL7);
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL7), reg, "Load value from address in  " + reg + " into " + reg);
     }
 
     //-------------------------------------------------------------------------
     //      LoadStoAddr - Sets the address value of a sto into register
     //-------------------------------------------------------------------------
     public void LoadStoAddr(STO sto, String reg)
     {
         // PUT ADDRESS OF STO INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, sto.getOffset(), reg, "Put the offset/name of " + sto.getName() + " into " + reg);
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, sto.getBase(), reg, reg, "Add offset/name to base reg " + reg);
     }
 
     //-------------------------------------------------------------------------
     //      StoreStoValueIntoAddr - Stores a sto's value into address in destReg - "puts into memory"
     //-------------------------------------------------------------------------
     public void StoreStoValueIntoAddr(STO valueSto, String tmpReg, String destReg)
     {
         writeComment("Store " + valueSto.getName() + " into " + destReg);
 
         // Load value into tmpReg
         LoadStoValue(valueSto, tmpReg);
 
         // STORE VALUE AT ADDRESS INTO <reg>
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, tmpReg, bracket(destReg), "Store value of " + valueSto.getName() + " into " + destReg);
     }
 
     //-------------------------------------------------------------------------
     //      StoreValueIntoAddr - Stores the value in valueReg into the address in destReg
     //-------------------------------------------------------------------------
     public void StoreValueIntoAddr(String valueReg, String destReg)
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
         writeAssembly(SparcInstr.BLANK_LINE); 
         writeComment("Store value in " + valueReg + " into sto " + destSto.getName());
         // Load sto addr into %l7
         LoadStoAddr(destSto, SparcInstr.REG_LOCAL7);
 
         // STORE VALUE IN valueReg INTO destSto (which has addr in %l6)
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.STORE_OP, valueReg, bracket(SparcInstr.REG_LOCAL7), "Store value in " + valueReg  + " into sto " + destSto.getName());
 
         //System.out.println("Storing " + destSto.getName() + " to: " + destSto.load());
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
     //      PutFloatInMem -  store float literal in rodata section
     //-------------------------------------------------------------------------
     public String PutFloatInMem(float floatValue)
     {
             // Check if it's already allocated
             String floatAddr;
             if((floatAddr = storedFloats.get(floatValue)) != null) {
 
                 return floatAddr;
             }
 
             String floatLabel = ".float_" + String.valueOf(float_count);
 
             // .section ".data"
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.DATA_SEC);
 
             // .align 4
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
 
             // float<xxx>: .single 0r5.75 
             decreaseIndent();
             writeAssembly(SparcInstr.RO_DEFINE, floatLabel, SparcInstr.SINGLEP, "0r" + (String.valueOf(floatValue)));
             increaseIndent();
             writeAssembly(SparcInstr.BLANK_LINE);
 
             // .section ".text"
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.SECTION_DIR, SparcInstr.TEXT_SEC);
             writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.ALIGN_DIR, "4");
             writeAssembly(SparcInstr.BLANK_LINE);
         
             float_count++;
             
             // Store in Sto and add float to HashMap so we can use it in the future without allocating
             storedFloats.put(floatValue, floatLabel);
 
             return floatLabel;
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
             String floatAddr = PutFloatInMem(sto.getFloatValue());
 
             // set label, %l0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, floatAddr, SparcInstr.REG_LOCAL0);
             //writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, floatAddr, SparcInstr.REG_LOCAL0);
 
             // ld [%l0], %f0
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.LOAD_OP, bracket(SparcInstr.REG_LOCAL0), SparcInstr.REG_FLOAT0);
 
             // st %f0, [%fp-offset]
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.STORE_OP, SparcInstr.REG_FLOAT0, bracket(SparcInstr.REG_FRAME + offset));
             writeAssembly(SparcInstr.BLANK_LINE);
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
     //      DoExit
     //-------------------------------------------------------------------------
     public void DoExit(STO sto)
     {
         // sto can only be int
         // load sto into %o0
         LoadStoValue(sto, SparcInstr.REG_ARG0);
 
         // call exit
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.CALL_OP, SparcInstr.EXIT);
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
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
         String Op = SparcInstr.CMP_OP;
         boolean isFloatOp = false;
         
         // If either is float, then we'll use float registers
         if(operand1.getType().isFloat() || operand2.getType().isFloat()) {
             regOp1 = SparcInstr.REG_FLOAT0;
             regOp2 = SparcInstr.REG_FLOAT1;
             isFloatOp = true;
             Op = SparcInstr.FCMPS_OP;
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
         // NEqualToOp
         else if(op.isNEqualToOp()) {
             if(isFloatOp)
                 branchOp = SparcInstr.FBNE_OP;
             else
                 branchOp = SparcInstr.BNE_OP;
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
 
         // Get label ready
         String compLabel = ".compL_" + compLabel_count;
         compLabel_count++;
         
         // Load the operands
         LoadStoValue(operand1, regOp1);
         LoadStoValue(operand2, regOp2);
 
         // %l0 is going to hold our boolean result of the comparison
         // We initialize it to 1 (true) and branch over the %l0 = 0 (false) statement if the comparison is true
         // TODO: Change this to 1.00 for float comparisons
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL0, "Init result to true");
 
         // Perform comparison, branch if true, if false, fall through and set 0 (false)
         writeAssembly(SparcInstr.TWO_PARAM_COMM, Op, regOp1, regOp2, "operand1 <cond> operand2");
         writeAssembly(SparcInstr.ONE_PARAM_COMM, branchOp, compLabel, "if the result is true, branch and do nothing");
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
 
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
     //      DoAddressOf - put address of operandSto into resultSto for MyParser
     //-------------------------------------------------------------------------
     public void DoAddressOf(STO operandSto, STO resultSto)
     {
         writeCommentHeader("Do Address Of Operation");
 
         // Allocate resultSto (i.e. get it an address and 4 bytes of space on stack to store into
         AllocateSto(resultSto);
 
         String reg = SparcInstr.REG_LOCAL0 ;
         // Load address of the sto into %l0
         writeComment("Put address of " + operandSto.getName() + " into " + reg);
         LoadStoAddr(operandSto, reg);
         
         // Store the value (which is the address) into the STO
         writeComment("Store address " + operandSto.getName() + " (which is in " + reg + " into " + resultSto);
         StoreValueIntoSto(reg, resultSto);
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
     public void DoUnaryOp(UnaryOp op, STO operand, STO resultSto)
     {
         String operation = "";
         String regOp = SparcInstr.REG_LOCAL0;
 
         if(op.isNotOp()) {
             // Get label ready
             String compLabel = ".compL_" + compLabel_count;
             compLabel_count++;
             
             // Load the operands
             LoadStoValue(operand, regOp);
 
             // Set to true initially
             writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL1, "Init result to true");
 
             // Perform comparison, branch if true, if false, fall through and set 0 (false)
             writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.CMP_OP, SparcInstr.REG_GLOBAL0, regOp, "not operand");
             writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BE_OP, compLabel, "if the result is true, branch and do nothing");
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
             writeAssembly(SparcInstr.BLANK_LINE);
 
             // It was false, set 0
             writeComment("It was false, set 0");
             MoveRegToReg(SparcInstr.REG_GLOBAL0, SparcInstr.REG_LOCAL1);
 
             // Print label, this label facilitates "true"
             decreaseIndent();
             writeAssembly(SparcInstr.LABEL, compLabel);
             increaseIndent();
             writeAssembly(SparcInstr.BLANK_LINE);
 
             AllocateSto(resultSto);
             StoreValueIntoSto(SparcInstr.REG_LOCAL1, resultSto);
 
             return;
         }
 
         if(operand.getType().isFloat()) {
             if(op.isUnMinusOp()) {
                 operation = SparcInstr.FNEGS_OP;
             }
             else if(op.isIncOp()) {
                 operation = SparcInstr.FADDS_OP; 
             }
             else if(op.isDecOp()) {
                 operation = SparcInstr.FSUBS_OP;
             }
 
             // Create a STO with 1.00 for incrementing
             ConstSTO one = new ConstSTO("1.00", new FloatType(), 1.00);
             DoLiteral(one);
             LoadStoValue(one, SparcInstr.REG_FLOAT1);
 
             regOp = SparcInstr.REG_FLOAT0;
         }
 
         else {
             if(op.isUnMinusOp()) {
                 operation = SparcInstr.NEG_OP;
             }
             else if(op.isIncOp()) {
                 operation = SparcInstr.INC_OP; 
             }
             else if(op.isDecOp()) {
                 operation = SparcInstr.DEC_OP;
             }
         }
 
         // Load operand into regOp
         LoadStoValue(operand, regOp);
 
         if(op.isUnPlusOp())
         {
             writeCommentHeader("Do Unary Minus Operation on " + operand.getType().getName());
 
             // Store Result
             AllocateSto(resultSto);
             StoreValueIntoSto(regOp, resultSto);
 
         }
 
         else if(op.isUnMinusOp()) {
             writeCommentHeader("Do Unary Minus Operation on " + operand.getType().getName());
 
             // Perform operation
             LoadStoValue(operand, regOp);
             writeAssembly(SparcInstr.TWO_PARAM_COMM, operation, regOp, regOp, "Perform UnarySign Op");
 
             // Store Result
             AllocateSto(resultSto);
             StoreValueIntoSto(regOp, resultSto);
         }
 
         else {
 
 
             // Store un-incremented, un-decremented value into result if it's pre
             if(op.isPost()) {
                 AllocateSto(resultSto);
                 StoreValueIntoSto(regOp, resultSto);
             }
 
             // perform the operation
             if(operand.getType().isFloat()) {
                 writeCommentHeader("Do Inc/Dec Operation on " + operand.getType().getName());
 
                 // Perform operation
                 writeAssembly(SparcInstr.THREE_PARAM_COMM, operation, regOp, SparcInstr.REG_FLOAT1, regOp, "Perform float inc/dec op");
             }
             else {
 
                 writeCommentHeader("Do Inc/Dec Operation on " + operand.getType().getName());
 
                 // Perform operation
                 writeAssembly(SparcInstr.ONE_PARAM_COMM, operation, regOp, "Perform inc " + operation + "op");
             }
 
             // Store incremented, decremented value into result if not post op
             if(!op.isPost()) {
                 AllocateSto(resultSto);
                 StoreValueIntoSto(regOp, resultSto);
             }
 
             StoreValueIntoSto(regOp, operand);
         }
     }
 
     //-------------------------------------------------------------------------
     //      DoBinaryOp
     //-------------------------------------------------------------------------
     public void DoBinaryOp(BinaryOp op, STO operand1, STO operand2, STO resultSto)
     {
 
         writeCommentHeader("Performing " + operand1.getName() + " " + op.getName() + " " + operand2.getName());
 
         String binaryOp = "";
         String regOp1 = SparcInstr.REG_LOCAL0;
         String regOp2 = SparcInstr.REG_LOCAL1;
         String comment = operand1.getName() + " and " + operand2.getName();
         boolean isFloatOp = false;
         boolean isCallOp = false;
 
         if(operand1.getType().isFloat() || operand2.getType().isFloat()) {
             regOp1 = SparcInstr.REG_FLOAT0;
             regOp2 = SparcInstr.REG_FLOAT1;
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
         LoadStoValue(operand1, regOp1);
         LoadStoValue(operand2, regOp2);
 
         // Call Operator
         if(isCallOp) {
             // If not Float, move arguments into out registers
             if(!isFloatOp) {
                 MoveRegToReg(regOp1, SparcInstr.REG_ARG0);
                 MoveRegToReg(regOp2, SparcInstr.REG_ARG1);
             }
 
             writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.CALL_OP, binaryOp, comment);
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
             writeAssembly(SparcInstr.BLANK_LINE);
 
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
 
     //-------------------------------------------------------------------------
     //      DoBooleanOp
     //-------------------------------------------------------------------------
     public void DoBooleanOp(BooleanOp op, STO operand1, STO operand2, STO resultSto)
     {
         writeCommentHeader("DoBooleanOp");
         String operandReg = SparcInstr.REG_LOCAL1;
         String branchOp = "";  
 
         // Load operand1 into register
         LoadStoValue(operand1, operandReg);
 
         // Get label ready
         String boolLabel = ".boolL_" + boolLabel_count;
         //stackBoolLabel.push(boolLabel);
         boolLabel_count++;
 
         // %l3 holds the value we swap to
 
         // &&
         if(op.isAndOp()) {
             // %l0 is going to hold our boolean result of the comparison
             MoveRegToReg(SparcInstr.REG_GLOBAL0, SparcInstr.REG_LOCAL0);
             branchOp = SparcInstr.BE_OP; 
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL3);
         }
 
         // ||
         else {
             // %l0 is going to hold our boolean result of the comparison
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL0);
             branchOp = SparcInstr.BNE_OP; 
             MoveRegToReg(SparcInstr.REG_GLOBAL0, SparcInstr.REG_LOCAL3);
         }
 
         // Perform first condition, branch if true, if false, fall through and check second condition
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, operandReg, SparcInstr.REG_GLOBAL0);
         writeAssembly(SparcInstr.ONE_PARAM, branchOp, boolLabel);
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Load operand2 into register
         LoadStoValue(operand2, operandReg);
 
         // Get label ready
         //String boolLabel = stackBoolLabel.pop();
 
         // Perform second condition, branch if true, if false, fall through and set return
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, operandReg, SparcInstr.REG_GLOBAL0);
         writeAssembly(SparcInstr.ONE_PARAM, branchOp, boolLabel);
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
 
         writeComment("Swap return value since we never branched");
         MoveRegToReg(SparcInstr.REG_LOCAL3, SparcInstr.REG_LOCAL0);
 
         // Print label
         decreaseIndent();
         writeAssembly(SparcInstr.LABEL, boolLabel);
         increaseIndent();
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Comparison done, result is in %l0, store it in the resultSto
         AllocateSto(resultSto);
         StoreValueIntoSto(SparcInstr.REG_LOCAL0, resultSto);
     }
 
     //-------------------------------------------------------------------------
     //      DoBooleanOp1
     //-------------------------------------------------------------------------
     public void DoBooleanOp1(BooleanOp op, STO operand)
     {
         writeCommentHeader("DoBooleanOp1");
         String regOp = SparcInstr.REG_LOCAL1;
         String branchOp = "";  
         String returnReg = SparcInstr.REG_LOCAL3;
 
         // Load operand into register
         LoadStoValue(operand, regOp);
 
         // Get label ready
         String boolLabel = ".boolL_" + boolLabel_count;
         stackBoolLabel.push(boolLabel);
         boolLabel_count++;
 
         // &&
         if(op.isAndOp()) {
             branchOp = SparcInstr.BE_OP; 
         }
         // ||
         else {
             branchOp = SparcInstr.BNE_OP; 
         }
 
         // Perform first condition, branch if true, if false, fall through and check second condition
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, regOp, SparcInstr.REG_GLOBAL0);
         writeAssembly(SparcInstr.ONE_PARAM, branchOp, boolLabel);
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
     }
 
     //-------------------------------------------------------------------------
     //      DoBooleanOp2
     //-------------------------------------------------------------------------
     public void DoBooleanOp2(BooleanOp op, STO operand, STO resultSto)
     {
         writeCommentHeader("DoBooleanOp2");
         String regOp = SparcInstr.REG_LOCAL1;
         String branchOp = "";  
         String returnReg = SparcInstr.REG_LOCAL3;
 
         // Load operand into register
         LoadStoValue(operand, regOp);
 
         // Get label ready
         String boolLabel = stackBoolLabel.pop();
 
         // &&
         if(op.isAndOp()) {
             // %l0 is going to hold our boolean result of the comparison
             MoveRegToReg(SparcInstr.REG_GLOBAL0, SparcInstr.REG_LOCAL0);
             branchOp = SparcInstr.BE_OP; 
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.MOV_OP, String.valueOf(1), SparcInstr.REG_LOCAL3);
         }
 
         // ||
         else {
             // %l0 is going to hold our boolean result of the comparison
             writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.SET_OP, String.valueOf(1), SparcInstr.REG_LOCAL0);
             branchOp = SparcInstr.BNE_OP; 
             MoveRegToReg(SparcInstr.REG_GLOBAL0, SparcInstr.REG_LOCAL3);
         }
 
         // Perform second condition, branch if true, if false, fall through and set return
         writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, regOp, SparcInstr.REG_GLOBAL0);
         writeAssembly(SparcInstr.ONE_PARAM, branchOp, boolLabel);
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
 
         writeComment("Swap return value since we never branched");
         MoveRegToReg(SparcInstr.REG_LOCAL3, SparcInstr.REG_LOCAL0);
 
         // Print label
         decreaseIndent();
         writeAssembly(SparcInstr.LABEL, boolLabel);
         increaseIndent();
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // Comparison done, result is in %l0, store it in the resultSto
         AllocateSto(resultSto);
         StoreValueIntoSto(SparcInstr.REG_LOCAL0, resultSto);
     }
 
     //-------------------------------------------------------------------------
     //      DoIf
     //-------------------------------------------------------------------------
     public void DoIf(STO condition)
     {
     	writeComment("ifelse " + condition.getName());
     	String label = ".if.else." + ifLabel_count; // Will branch to this label if false
 
     	// Has to increment in this function in order to handle nested if's / ifelse's
     	ifLabel_count++;
     	stackIfLabel.add(label);
     	
         // Load condition into %l0 for comparison
         LoadStoValue(condition, SparcInstr.REG_LOCAL0);
 
     	//  %l0, %g0
     	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, SparcInstr.REG_LOCAL0, SparcInstr.REG_GLOBAL0);
     	// be IfL1! Opposite logic
     	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.BE_OP, label);
     	writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
     	increaseIndent();
     }
     
     //-------------------------------------------------------------------------
     //      DoIfCodeBlock
     //-------------------------------------------------------------------------
     public void DoIfCodeBlock()
     {
     	decreaseIndent();
         String ifElseLabel = stackIfLabel.peek();
         String jumpTo = ifElseLabel + ".end";
         //System.out.println(jumpTo);
         // write an Always branch to if else end for when the condition is true
         writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BA_OP, jumpTo, "Skip over to if.else.end");
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
 
         // write ifelse label
     	writeAssembly(SparcInstr.LABEL, ifElseLabel);
     }
     
     //-------------------------------------------------------------------------
     //      DoIfElseCodeBlock
     //-------------------------------------------------------------------------
     public void DoIfElseCodeBlock()
     {
     	String label = stackIfLabel.pop();
     	writeAssembly(SparcInstr.LABEL, label+".end");
     }
     
     //-------------------------------------------------------------------------
     //      DoWhileStart
     //-------------------------------------------------------------------------
     public void DoWhileStart()
     {
     	// stackWhileLabel
     	String whileLabel = ".while."+whileLabel_count;
     	whileLabel_count++;
     	stackWhileLabel.add(whileLabel);
     	
     	// write while label before logic check
         decreaseIndent();
     	writeAssembly(SparcInstr.LABEL, whileLabel);
         increaseIndent();
     	
     }
     
     //-------------------------------------------------------------------------
     //      DoWhile
     //-------------------------------------------------------------------------
     public void DoWhile(STO condition)
     {
     	// Load condition into %l0 for comparison
         LoadStoValue(condition, SparcInstr.REG_LOCAL0);
 
     	//  %l0, %g0
     	writeAssembly(SparcInstr.TWO_PARAM, SparcInstr.CMP_OP, SparcInstr.REG_LOCAL0, SparcInstr.REG_GLOBAL0);
     	// be IfL1! Opposite logic
     	writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.BE_OP, stackWhileLabel.peek()+".end");
     	writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
     	increaseIndent();
     }
     
     //-------------------------------------------------------------------------
     //      DoWhileCodeBlock
     //-------------------------------------------------------------------------
     public void DoWhileCodeBlock()
     {
         String jumpTo = stackWhileLabel.peek();
         // write an always branch to beginning of while loop
         writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BA_OP, jumpTo, "Go back to the beginning of the while loop");
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         writeAssembly(SparcInstr.BLANK_LINE);
         
     	decreaseIndent();
     	// write while.end label
     	String label = stackWhileLabel.pop();
         decreaseIndent();
     	writeAssembly(SparcInstr.LABEL, label+".end");
         increaseIndent();
     }
     
     //-------------------------------------------------------------------------
     //      DoBreakStmt
     //-------------------------------------------------------------------------
     public void DoBreakStmt()
     {
         writeCommentHeader("DoBreakStmt");
     	//System.out.println("Breaking on :" + level);
    	String jumpTo = stackWhileLabel.pop() + ".end";
         // write an Always branch to if else end for when the condition is true
         writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BA_OP, jumpTo, "Jump to the corresponding while.end");
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
     }
     
     //-------------------------------------------------------------------------
     //      DoContinueStmt
     //-------------------------------------------------------------------------
     public void DoContinueStmt(int level)
     {
     	String jumpTo = ".while."+ level;
         // write an Always branch to if else end for when the condition is true
         writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.BA_OP, jumpTo, "Jump to the corresponding while");
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
     }
     
     //-------------------------------------------------------------------------
     //      DoDeclArray
     //-------------------------------------------------------------------------
     public void DoDeclArray(Type type, int size)
     {
     	
     }
 
 
     
         
 /*
     //-------------------------------------------------------------------------
     //      DoArrayInit
     //-------------------------------------------------------------------------
     public void DoArrayEleAssign(STO arraySto, STO indexSto);
     {
 
         String counterReg = SparcInstr.REG_LOCAL0;
         String sizeReg = SparcInstr.REG_LOCAL1;
         String whileLabel = "." + arraySto.getName() + "while";
 
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, SparcInstr.REG_GLOBAL0, counterReg, "init loop counter to 0");
         LoadStoValue(indexSto, sizeReg);        // Init counter to array size
         
         decreaseIndent();
         writeAssembly(SparcInstr.LABEL, whileLabel);
         increaseIndent();
 
         // Compare loop counter to size
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.CMP_OP, counterReg, sizeReg, "Compare counter to size");
 
         writeAssembly(SparcInstr.ONE_PARAM, SparcInstr.BE_OP, whileLabel);
         writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
         
         // Set the base and offset for this reg 
         String reg = SparcInstr.REG_LOCAL2;
 
         LoadStoValue(indexSto, sizeReg);
 
         // multiple the sizeReg by sizeof which is 4
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.SLL_OP, sizeReg, String.valueOf(2), sizeReg, "index * 4 -> scaled offset");
 
         // put offset into %l1
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(arraySto.getOffset()), reg, "set offset into " + reg);
 
         // add base, sizeReg, sizeReg
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, sizeReg, reg, sizeReg, "base + offset");
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, arraySto.getBase(), sizeReg, sizeReg, "base + offset");
 
         // offset reg now has address of the elemnt
 
         LoadValueFromAddr(sizeReg, reg);
 
         AllocateSto(resultSto);
         StoreValueIntoSto(reg, resultSto);
     }
     
     */
     //-------------------------------------------------------------------------
     //      GetArrayElementAddr - puts the address of an array element in the given register - uses %l7 as temp
     //-------------------------------------------------------------------------
     public void GetArrayElementAddr(STO arraySto, String addrReg)
     {
         writeComment("GETTING ADDRESS OF ELEMENT");
         // addrReg contains the index to get address for
         String tmpReg = SparcInstr.REG_LOCAL7;
 
         // multiple the addrReg by sizeof which is 4
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.SLL_OP, addrReg, String.valueOf(2), addrReg, "index * 4 -> scaled offset");
 
         // put offset into tmpReg
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(arraySto.getOffset()), tmpReg, "set offset into " + tmpReg);
 
         // add base, addrReg, addrReg
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, tmpReg,addrReg,  addrReg, "base + offset");
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, arraySto.getBase(), addrReg, addrReg, "base + offset");
         writeComment("Bottom of GetArrayElementAddr");
         // addrReg tmpReg now has address of the elemnt
 
     }
 
     //-------------------------------------------------------------------------
     //      DoArrayAccess
     //-------------------------------------------------------------------------
     public void DoArrayAccess(STO arraySto, STO indexSto, STO resultSto)
     {
 
         writeCommentHeader("Accessing index " + ((ConstSTO) indexSto).getIntValue() + " of " + arraySto.getName());
         String offsetReg = SparcInstr.REG_LOCAL0;
         String reg = SparcInstr.REG_LOCAL1;
 
         LoadStoValue(indexSto, offsetReg);
 
         // multiple the offsetReg by sizeof which is 4
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.SLL_OP, offsetReg, String.valueOf(2), offsetReg, "index * 4 -> scaled offset");
 
         // put offset into %l1
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(arraySto.getOffset()), reg, "set offset into " + reg);
 
         // add base, offsetReg, offsetReg
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, offsetReg, reg, offsetReg, "base + offset");
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, arraySto.getBase(), offsetReg, offsetReg, "base + offset");
 
         // offset reg now has address of the elemnt
 
         LoadValueFromAddr(offsetReg, offsetReg);
 
         AllocateSto(resultSto);
         StoreValueIntoSto(offsetReg, resultSto);
     }
     
     public void DoStructAccess(STO struct, STO resultSto) {
         //writeCommentHeader("Accessing index " + ((ConstSTO) indexSto).getIntValue() + " of " + arraySto.getName());
 
     	String offsetReg = SparcInstr.REG_LOCAL0;
         String reg = SparcInstr.REG_LOCAL1;
 
         LoadStoValue(resultSto, offsetReg);
 
         // multiple the offsetReg by sizeof which is 4
         //writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.SLL_OP, offsetReg, String.valueOf(2), offsetReg, "index * 4 -> scaled offset");
 
         // put offset into %l1
         writeAssembly(SparcInstr.TWO_PARAM_COMM, SparcInstr.SET_OP, String.valueOf(resultSto.getOffset()), reg, "set offset into " + reg);
 
         // add base, offsetReg, offsetReg
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, offsetReg, reg, offsetReg, "base + offset");
         writeAssembly(SparcInstr.THREE_PARAM_COMM, SparcInstr.ADD_OP, struct.getBase(), offsetReg, offsetReg, "base + offset");
 
         // offset reg now has address of the elemnt
 
         LoadValueFromAddr(offsetReg, offsetReg);
 
         AllocateSto(resultSto);
         StoreValueIntoSto(offsetReg, resultSto);
     }
     
     //-------------------------------------------------------------------------
     //      DoInput
     //-------------------------------------------------------------------------
     public void DoCin(STO sto) 
     {
         if(sto.isModLValue()) {
             String reg = "";
             String function = "";
             
             if(sto.getType().isInt()) {
                 reg = SparcInstr.REG_ARG0;
                 function = SparcInstr.INPUTINT;
             }
             else if(sto.getType().isFloat()) {
                 reg = SparcInstr.REG_FLOAT0;
                 function = SparcInstr.INPUTFLOAT;
             }
 
             LoadStoValue(sto, reg);
             writeAssembly(SparcInstr.ONE_PARAM_COMM, SparcInstr.CALL_OP, function, "Inputing");
             writeAssembly(SparcInstr.NO_PARAM, SparcInstr.NOP_OP);
             writeAssembly(SparcInstr.BLANK_LINE);
 
             StoreValueIntoSto(reg, sto);
         }
     }
 
 	public void DoStructdef(TypedefSTO sto) {
 		
 	}
     
 }
