 
 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 import java_cup.runtime.*;
 import java.util.Vector;
 import java.util.Stack;
 
 class MyParser extends parser
 {
 
     //----------------------------------------------------------------
     //    Constants 
     //----------------------------------------------------------------
     private final String OUTPUT_FILENAME = "rc.s";
     private static final boolean DEBUG = false;
 
     //----------------------------------------------------------------
     //    Instance variables
     //----------------------------------------------------------------
     private Lexer        m_lexer;
     private ErrorPrinter m_errors;
     private int          m_nNumErrors;
     private String       m_strLastLexeme;
     private boolean      m_bSyntaxError = true;
     private int          m_nSavedLineNum;
     private SymbolTable  m_symtab;
 
     private boolean      m_inStructdef = false;
     private String       m_structId;
     private Scope        m_currentStructdef;
 
     private int          m_loopLevel;
     
     private AssemblyCodeGenerator m_codegen;
 
     private boolean ERROR    = false;
     private boolean DECLTYPE = false;
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public MyParser(Lexer lexer, ErrorPrinter errors)
     {
         m_lexer       = lexer;
         m_symtab      = new SymbolTable();
         m_errors      = errors;
         m_nNumErrors  = 0;
         m_loopLevel   = 0;
 
         m_codegen = new AssemblyCodeGenerator(OUTPUT_FILENAME);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public boolean Ok()
     {
         return (m_nNumErrors == 0);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public Symbol scan()
     {
         Token t = m_lexer.GetToken();
 
         // We'll save the last token read for error messages.
         // Sometimes, the token is lost reading for the next
         // token which can be null.
         m_strLastLexeme = t.GetLexeme();
 
         switch(t.GetCode()) {
             case sym.T_ID:
             case sym.T_ID_U:
             case sym.T_STR_LITERAL:
             case sym.T_FLOAT_LITERAL:
             case sym.T_INT_LITERAL:
                 return (new Symbol(t.GetCode(), t.GetLexeme()));
             default:
                 return (new Symbol(t.GetCode()));
         }
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void syntax_error(Symbol s)
     {
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void report_fatal_error(Symbol s)
     {
         m_nNumErrors++;
         if(m_bSyntaxError) {
             m_nNumErrors++;
 
             //    It is possible that the error was detected
             //    at the end of a line - in which case, s will
             //    be null.  Instead, we saved the last token
             //    read in to give a more meaningful error
             //    message.
             m_errors.print(Formatter.toString(ErrorMsg.syntax_error, m_strLastLexeme));
         }
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void unrecovered_syntax_error(Symbol s)
     {
         report_fatal_error(s);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void DisableSyntaxError()
     {
         m_bSyntaxError = false;
     }
 
     public void EnableSyntaxError()
     {
         m_bSyntaxError = true;
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public String GetFile()
     {
         return (m_lexer.getEPFilename());
     }
 
     public int GetLineNum()
     {
         return (m_lexer.getLineNumber());
     }
 
     public void SaveLineNum()
     {
         m_nSavedLineNum = m_lexer.getLineNumber();
     }
 
     public int GetSavedLineNum()
     {
         return (m_nSavedLineNum);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoProgramStart()
     {
         // Opens the global scope.
         m_symtab.openScope();       // Open Global Scope
 
         if(GenerateCode()) m_codegen.DoProgramStart(GetFile());
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoProgramEnd()
     {
         m_symtab.closeScope();      // Close Global Scope
         if(GenerateCode()) m_codegen.DoProgramEnd();
         if(GenerateCode()) m_codegen.dispose();
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     boolean GenerateCode()
     {
        return (ERROR || DECLTYPE);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoAutoDecl(boolean isStatic, boolean isConst, String id, STO expr)
     {
         STO resultSTO;
         if(isConst) {
             resultSTO = new ConstSTO(id, expr.getType(), ((ConstSTO)expr).getValue());
         }
         else {
             resultSTO = new VarSTO(id, expr.getType());
         }
         resultSTO.setIsStatic(isStatic);
         m_symtab.insert(resultSTO);
     }
     
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoVarDecl(boolean isStatic, Type type, Vector<IdValueTuple> lstIDs)
     {
         for(int i = 0; i < lstIDs.size(); i++) {
             String id = lstIDs.elementAt(i).getId();
             STO value = lstIDs.elementAt(i).getValue();
             STO arrayIndex = lstIDs.elementAt(i).getArrayIndex(); 
             Type ptrType = lstIDs.elementAt(i).getPointerType();  
 
             // Check for var already existing in localScope
             if(m_symtab.accessLocal(id) != null) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
                 ERROR = true;
             }
 
             Type finalType = type;
 
             VarSTO stoVar;
 
             if(ptrType != null) {
                 ((PointerType) ptrType).setBottomPtrType(finalType);
                 ((PointerType) ptrType).setInitialName();
                 finalType = ptrType;
             }
 
             // Do Array checks if finalType = ArrayType
             if(arrayIndex != null && finalType != null) {
                 // Check 10
                 ArrayType arrType = DoArrayDecl(finalType, arrayIndex);
                 if (arrType != null) {
                     // Check 11b
                     if(value.isArrEle()) {
                         Vector<STO> elements = ((ArrEleSTO) value).getArrayElements();
                         // # elements not exceed array size
                         if(elements.size() > ((ConstSTO) arrayIndex).getIntValue()) {
                             m_nNumErrors++;
                             m_errors.print(ErrorMsg.error11_TooManyInitExpr);
                             ERROR = true;
                             break;
                         }
                         
                         for(STO thisElement : elements) {
                             if (!thisElement.isConst()) {
                                 m_nNumErrors++;
                                 m_errors.print(ErrorMsg.error11_NonConstInitExpr);
                                 ERROR = true;
                                 break;
                             }
                             
                             if (!thisElement.getType().isAssignable(finalType)) {
                                 m_nNumErrors++;
                                 m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign, thisElement.getType().getName(), finalType.getName()));
                                 ERROR = true;
                                 break;
                             }
                         }
                         // Add it array
                         arrType.setElementList(elements);
                     }        
                 }
                 // Override finalType with new arrayType that encompasses the value stored in finalType
                 finalType = arrType;
             }
 
             stoVar = new VarSTO(id, finalType);
             m_symtab.insert(stoVar);
             if(stoVar.isGlobal()) {
                 if(GenerateCode()) m_codegen.DoGlobalDecl(stoVar, value);
             }
             else {
                 if(GenerateCode()) m_codegen.DoVarDecl(stoVar);
 
                 if(!value.isNull()) {
                     if(!finalType.isArray()) {
                         DoAssignExpr(stoVar, value);
                     }
                 }
             }
         }
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     ArrayType DoArrayDecl(Type type, STO indexSTO) {
         boolean errorFlag = false;
         int size = 0;
         
         if(!indexSTO.isError()) {
             // Do Array checks if type = ArrayType
             if(!indexSTO.getType().isEquivalent(new IntType())) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error10i_Array, indexSTO.getType().getName()));
                 ERROR = true;
                 errorFlag = true;
             }
             
             if(!indexSTO.isConst()) {
                 m_nNumErrors++;
                 m_errors.print(ErrorMsg.error10c_Array); 
                 ERROR = true;
                 errorFlag = true;
             } 
             else if (((ConstSTO)indexSTO).getIntValue() <= 0) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error10z_Array, ((ConstSTO)indexSTO).getIntValue()));
                 ERROR = true;
                 errorFlag = true;
             }
             
             if (errorFlag == false) {
                 size = ((ConstSTO)indexSTO).getIntValue();
             }
             
         }
         return new ArrayType(type, size);
     }
     
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoExternDecl(Type type, Vector<IdValueTuple> lstIDs)
     {
         for(int i = 0; i < lstIDs.size(); i++) {
             String id = lstIDs.elementAt(i).getId();
             Type pointerType = lstIDs.elementAt(i).getPointerType();
             STO arrayIndex = lstIDs.elementAt(i).getArrayIndex();
 
             if(m_symtab.accessLocal(id) != null) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
                 ERROR = true;
             }
 
             Type newType = DoBuildType(type, pointerType, arrayIndex);
             VarSTO sto = new VarSTO(id, newType);
             m_symtab.insert(sto);
         }
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoConstDecl(boolean isStatic, Type type, Vector<IdValueTuple> lstIDs)
     {
         // Check for previous errors
         for(int i = 0; i < lstIDs.size(); i++) {
             if(lstIDs.elementAt(i).getValue().isError())
                 return;
             //return lstIDs.elementAt(i).value;
 
         }
 
         for(int i = 0; i < lstIDs.size(); i++) {
 
             String id = lstIDs.elementAt(i).getId();
             STO value = lstIDs.elementAt(i).getValue();
 
             // Check for constant already existing in localScope
             if(m_symtab.accessLocal(id) != null) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
                 ERROR = true;
                 return;
             }
 
             // Check #8a - init value not known at compiler time
             if(!value.isConst()) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error8_CompileTime, id));
                 ERROR = true;
                 return;
             }
 
             // Check #8b
             if(!value.getType().isAssignable(type)) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, value.getType().getName(), type.getName()));
                 ERROR = true;
                 return;
             }
 
             STO sto = new ConstSTO(id, type, ((ConstSTO)value).getValue());
             m_symtab.insert(sto);
             if(GenerateCode()) m_codegen.DoVarDecl(sto);
             if(GenerateCode()) m_codegen.DoAssignExpr(sto, value);
         }
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoTypedefDecl(Type type, Vector<String> lstIDs)
     {
         for(int i = 0; i < lstIDs.size(); i++) {
             String id = lstIDs.elementAt(i);
 
             if(m_symtab.accessLocal(id) != null) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
                 ERROR = true;
             }
             // Do Array checks if type = ArrayType
 /*            if(type.isArray()) {
                 STO stoResult = ((ArrayType)type);
 
             }*/
             TypedefSTO sto;
             if(type.isStruct()) {
                 sto = new TypedefSTO(id, new StructType(type.getName(), type.getSize(), ((StructType) type).getFields()), false, false);
             }
             else {
                 type.setName(id);
                 sto = new TypedefSTO(id, type, false, false);
             }
             
             m_symtab.insert(sto);
         }
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoStructdefDeclStart(String id)
     {
         m_inStructdef = true;
         m_structId = id;
         m_currentStructdef = new Scope();
 
         if(m_symtab.accessLocal(id) != null) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
             ERROR = true;
         } 
         else {
             TypedefSTO sto = new TypedefSTO(m_structId, new StructType(m_structId), false, false);
             m_symtab.insert(sto);
         }
     }
 
     STO DoStructdefField(String id, Type type)
     //void DoStructdefField(String id, STO thisSTO)
     {
         // Check for duplicate names
         // Check 13a
         if(m_currentStructdef.accessLocal(id) != null) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, id));
             ERROR = true;
             //return new ErrorSTO("Error- Field declared second time in struct");
         }
         // Check 13b
             // Check that the type is not this same type of struct and that it's not a pointer in that case
         else if((type.getName().equals(id)) && (!type.isPointer())) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error13b_Struct, id));
             ERROR = true;
            // return new ErrorSTO("Error- Size of field cannot be determined at compile time.");
         }
         VarSTO var = new VarSTO(id, type);
         m_currentStructdef.InsertLocal(var);
         return var;
         // create the STO
         // m_currentStructdef.InsertLocal(thisSTO);
         // return thisSTO;
 
     }
 
     void DoStructdefDeclFinish(String id, Vector<STO> fieldList)
     {
         // check for struct in scope
         // old line if(m_currentStructdef.accessLocal(id) != null) {
         if(m_symtab.accessLocal(id) != null) {
             // get size of struct
             int size = 0;
             for(STO sto : fieldList) {
                 if(sto.isFunc()) continue;
                 size += sto.getType().getSize();
                 sto.store(id, String.valueOf(size));
                // sto.setOffset(String.valueOf(size));
             }
 
             // get TypedefSTO of StructType 
             TypedefSTO sto = (TypedefSTO) m_symtab.accessLocal(id);
             sto.getType().setSize(size);
             ((StructType) sto.getType()).setFields(fieldList);
             
             /*
             if(GenerateCode()) {
                 if(sto.isGlobal()) 
                     m_codegen.DoGlobalDecl(sto, sto);
                 else
                     m_codegen.DoVarDecl(sto);
             }
             */
         }
         m_inStructdef = false;
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoFuncDecl_1(Type returnType, String id, Boolean retByRef)
     {
         STO accessedSTO;
 
         if(m_inStructdef) 
             accessedSTO = m_currentStructdef.access(id);
         else
             accessedSTO = m_symtab.accessLocal(id);
 
         // Check for func already existing in localScope
         if(accessedSTO != null) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
             ERROR = true;
         }
 
         // Create function pointer that contains function information to use for new FuncSTO
         FuncPtrType funcPtr = new FuncPtrType(returnType, retByRef);
 
         // Create new FuncSTO with function pointer holding it's info
         FuncSTO sto = new FuncSTO(id, funcPtr);
 
         if(m_inStructdef) 
             m_currentStructdef.InsertLocal(sto);
         else
             m_symtab.insert(sto);
 
         m_symtab.openScope();               // FuncStart, function scope will be open by the time params are added
         m_symtab.setFunc(sto);
 
         // Set the function's level
         sto.setLevel(m_symtab.getLevel());
 
         return sto;
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoFuncDecl_2()
     {
         FuncSTO stoFunc;
 
         if((stoFunc = m_symtab.getFunc()) == null) {
             m_nNumErrors++;
             m_errors.print("internal: DoFuncDecl_2 says no proc!");
             ERROR = true;
             return;
         }
 
         // Check #6c - no return statement for non-void type function
         if(!stoFunc.getReturnType().isVoid()) {
             if(!stoFunc.getHasReturnStatement()) {
                 m_nNumErrors++;
                 m_errors.print(ErrorMsg.error6c_Return_missing);
                 ERROR = true;
             }
 
         }
 
         if(GenerateCode()) m_codegen.DoFuncFinish(stoFunc);
 
         m_symtab.closeScope();
         m_symtab.setFunc(null);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoFormalParams(Vector<ParamSTO> params)
     {
         FuncSTO funcSto;
 
         if((funcSto = m_symtab.getFunc()) == null) {
             m_nNumErrors++;
             m_errors.print("internal: DoFormalParams says no proc!");
             ERROR = true;
             return;
         }
         // Insert parameters
         funcSto.setParameters(params);
 
         if(DEBUG) {
             System.out.println("MyParser.DoFormalParameters()");
             System.out.println("---------------------");
             System.out.println("Function: " + funcSto.getName());
             System.out.println("numOfParams: " + funcSto.getNumOfParams());
             System.out.println("params.size(): " + params.size());
         }
 
         // Add parameters to local scope
         for(STO thisParam: params) {
             if(DEBUG) System.out.println("param: " + thisParam.getName() + "\n");
             
             m_symtab.insert(thisParam);         // This is only for checking the code
                                                 // Need to add params to scope for function calls
         }
 
         if(GenerateCode()) m_codegen.DoFuncStart(funcSto);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoBlockOpen()
     {
         // Open a scope.
         m_symtab.openScope();           // anytime { is encountered, doesn't affect function openScope
 
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void DoBlockClose()
     {
         m_symtab.closeScope();
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoAssignExpr(STO stoDes, STO stoValue)
     {
         // Check for previous errors in line and short circuit
         if(stoDes.isError()) {
             return stoDes;
         }
         if(stoValue.isError()) {
             return stoValue;
         }
 
         // Check #3a - illegal assignment - not modifiable L-value
         if(!stoDes.isModLValue()) {
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error3a_Assign);
             ERROR = true;
             return (new ErrorSTO("DoAssignExpr Error - not mod-L-Value"));
         }
 
         // Check #3b - illegal assignment - bad types
         if(!stoValue.getType().isAssignable(stoDes.getType())) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign, stoValue.getType().getName(), stoDes.getType().getName()));
             ERROR = true;
             return (new ErrorSTO("DoAssignExpr Error - bad types"));
         }
 
         if(GenerateCode()) m_codegen.DoAssignExpr(stoDes, stoValue);
 
         return stoDes;
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoFuncCall(STO sto, Vector<STO> args)
     {
         // Check for previous errors
         if(sto.isError())
             return sto;
 
         for(int i = 0; i < args.size(); i++) {
             if(args.elementAt(i).isError())
                 return args.elementAt(i);
         }
 
         if(!sto.getType().isFuncPtr()) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.not_function, sto.getName()));
             ERROR = true;
             return (new ErrorSTO(sto.getName()));
         }
 
         // We know it's a function, do function call checks
         FuncPtrType funcType = (FuncPtrType) sto.getType();
         
 
         if(DEBUG) {
             System.out.println("MyParser.DoFuncCall()");
             System.out.println("---------------------");
             System.out.println("Function: " + sto.getName());
             System.out.println("numOfParams: " + funcType.getNumOfParams());
             System.out.println("args.size(): " + args.size());
         }
 
 
         // Check #5
         // Check #5a - # args = # params
         if((funcType.getNumOfParams() != args.size())) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error5n_Call, args.size(), funcType.getNumOfParams()));
             ERROR = true;
             return (new ErrorSTO("DoFuncCall - # args"));
         }
 
         // Now we check each arg individually, accepting one error per arg
 
         boolean error_flag = false;
 
         for(int i = 0; i < args.size(); i++) {
             // For readability and shorter lines
             ParamSTO thisParam = funcType.getParameters().elementAt(i);
             STO thisArg = args.elementAt(i);
 
             // Check #5b - non-assignable arg for pass-by-value param
             if(!thisParam.isPassByReference()) {
                 if(!thisArg.getType().isAssignable(thisParam.getType())) {
                     m_nNumErrors++;
                     m_errors.print(Formatter.toString(ErrorMsg.error5a_Call, thisArg.getType().getName(), thisParam.getName(), thisParam.getType().getName()));
                     ERROR = true;
                     error_flag = true;
                     continue;
                 }
             }
             else {
                 // Check #5c - arg type not equivalent to pass-by-ref param type
                 if(!thisArg.getType().isEquivalent(thisParam.getType())) {
                     m_nNumErrors++;
                     m_errors.print(Formatter.toString(ErrorMsg.error5r_Call, thisArg.getType().getName(), thisParam.getName(), thisParam.getType().getName()));
                     ERROR = true;
                     error_flag = true;
                     continue;
                 }
 
                 // Check #5d - arg not modifiable l-value for pass by ref param
                 if(!thisArg.isModLValue()) {
                     m_nNumErrors++;
                     m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, thisParam.getName(), thisArg.getType().getName()));
                     ERROR = true;
                     error_flag = true;
                     continue;
                 }
             }
 
 
         }
 
         STO returnSto;
 
         if(error_flag) {
             // Error occured in at least one arg, return error
             return (new ErrorSTO("DoFuncCall - Check 5"));
         }
         else {
             // Func call legal, return function return type
             if(funcType.getReturnByRef())
                 returnSto = new VarSTO(sto.getName() + "Return", funcType.getReturnType());
             else
                 returnSto = new ExprSTO(sto.getName() + "Return", funcType.getReturnType());
         }
 
         if(GenerateCode()) m_codegen.DoFuncCall(sto, args, returnSto);
 
         return returnSto;
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoDesignator2_Dot(STO sto, String strID)
     {
         STO returnSTO = null;
         
         // Good place to do the struct checks
         if(sto.isError()) {
             return sto;
         }
 
         // Check #14a
         // type of struct is not a struct type
         if(!sto.getType().isStruct()) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error14t_StructExp, sto.getType().getName()));
             ERROR = true;
             return new ErrorSTO("Struct Error - not a struct");
         }
 
         // Check #14b
         if((m_inStructdef) && ((m_structId == sto.getType().getName()) || sto.getName().equals("this"))) {
             if(m_currentStructdef.accessLocal(strID) == null) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error14b_StructExpThis, strID));
                 ERROR = true;
                 return new ErrorSTO("Struct Error - field not in Struct");
             } 
             else {
                 returnSTO = m_currentStructdef.accessLocal(strID);
             }
         }
         else {
             // Check #14a
             boolean found_flag = false;        // type of struct does not contain the field or function
             Vector<STO> fieldList = ((StructType)sto.getType()).getFields();
 
             for(STO thisSTO: fieldList) {
                 if(thisSTO.getName().equals(strID)) {
                     found_flag = true;
                     returnSTO = thisSTO;
                 }
             }
 
             if(!found_flag) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error14f_StructExp, strID, sto.getType().getName()));
                 ERROR = true;
                 return new ErrorSTO("Struct Error - field not found in type");
             }
         }
         if(GenerateCode()) m_codegen.DoStructAccess(sto, returnSTO);
         return returnSTO;
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoDesignator2_Arrow(STO sto, String strID)
     {
         STO returnSTO = null;
         
         // Good place to do the struct checks
         if(sto.isError()) {
             return sto;
         }
 
         // Check 15b
         // if it's a pointer but not a struct pointer
         if(sto.getType().isPointer()) {
             if(!((PointerType) sto.getType()).getPointsToType().isStruct()) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error15_ReceiverArrow, sto.getType().getName()));
                 ERROR = true;
                 return new ErrorSTO("Pointer Error - Doesn't point to struct pointer");
             }
         }
         // If it's not a pointer
         else {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error15_ReceiverArrow, sto.getType().getName()));
             ERROR = true;
             return new ErrorSTO("Pointer Error - Doesn't point to struct pointer");
         }
 
         // It's a pointer, check if the field is valid
         // Check 14a
 
         StructType structType = (StructType) ((PointerType) sto.getType()).getPointsToType();
 
         // if the struct we're accessing is the struct being defined
         if((m_inStructdef) && (m_structId == structType.getName())) {
             if(m_currentStructdef.accessLocal(strID) == null) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error14b_StructExpThis, strID));
                 ERROR = true;
                 return new ErrorSTO("Struct Error - field not in Struct");
             } 
             else {
                 returnSTO = m_currentStructdef.accessLocal(strID);
             }
         }
         // The struct we're accessing isn't the current struct
         else {
             boolean found_flag = false;        // type of struct does not contain the field or function
 
             Vector<STO> fieldList = structType.getFields(); 
 
             for(STO thisSTO: fieldList) {
                 if(thisSTO.getName().equals(strID)) {
                     found_flag = true;
                     returnSTO = thisSTO;
                 }
             }
 
             if(!found_flag) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error14f_StructExp, strID, structType.getName()));
                 ERROR = true;
                 return new ErrorSTO("Struct Error - field not found in type");
             }
         }
 
         return returnSTO;
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoDesignator2_Array(STO arraySto, STO indexSto)
     {
         // arraySto: the identifier
         // indexSto: the expression inside the []
         STO resultSto = null;
         
         if(arraySto.isError()) {
             return arraySto;
         }
 
         // Check #11a
         // bullet 1 - arraySto is not array or pointer type
         if((!arraySto.getType().isArray()) && (!arraySto.getType().isPointer())) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error11t_ArrExp, arraySto.getType().getName()));
             ERROR = true;
             return new ErrorSTO("Desig2_Array() - Not array or ptr");
         }
 
         // bullet 2 - index expression type is not equiv to int
         if(!indexSto.getType().isEquivalent(new IntType())) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error11i_ArrExp, indexSto.getType().getName()));
             ERROR = true;
             return new ErrorSTO("Desig2_Array() - index not equiv to int");
         }
 
         // bullet 3 - index expr is constant, error if indexExpr outside bounds of array dimension
         //              except when arraySto is pointer type
         if(indexSto.isConst()) {
             if(arraySto.getType().isArray()) {
                 if(((ConstSTO)indexSto).getIntValue() >= (((ArrayType)arraySto.getType()).getDimensionSize()) || ((ConstSTO)indexSto).getIntValue() < 0) {
                     m_nNumErrors++;
                     m_errors.print(Formatter.toString(ErrorMsg.error11b_ArrExp, ((ConstSTO)indexSto).getIntValue(), ((ArrayType)arraySto.getType()).getDimensionSize()));
                     ERROR = true;
                     return new ErrorSTO("Desig2_Array() - index is constant, out of bounds");
                 }
             }
         }
         
         // Checks are complete, now we need to return a VarSTO with the type of the array elements - VarSTO because result of [] operation is a modLVal
         if(arraySto.getType().isArray()) {
             resultSto = new VarSTO(((ArrayType)arraySto.getType()).getElementType().getName(),((ArrayType)arraySto.getType()).getElementType());
         } 
 
         else if (arraySto.getType().isPointer()){
             resultSto = new VarSTO(((PointerType)arraySto.getType()).getPointsToType().getName(),((PointerType)arraySto.getType()).getPointsToType());
         }
         //arraySto = m_symtab.access(arraySto.getName());
         if(GenerateCode()) m_codegen.DoArrayAccess(arraySto, indexSto, resultSto);
 
         return resultSto;
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoDesignator3_GlobalID(String strID)
     {
         STO sto;
 
         if((sto = m_symtab.accessGlobal(strID)) == null) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error0g_Scope, strID));
             ERROR = true;
             sto = new ErrorSTO(strID);
         }
         return (sto);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoDesignator3_ID(String strID)
     {
         STO sto;
 
         if((sto = m_symtab.access(strID)) == null) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));
             ERROR = true;
             sto = new ErrorSTO(strID);
         }
         return (sto);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoQualIdent(String strID)
     {
         STO sto;
         if((sto = m_symtab.access(strID)) == null) {
             if(m_inStructdef) {
                 if(!m_structId.equals(strID)) {
                     m_nNumErrors++;
                     m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));
                     ERROR = true;
                     return (new ErrorSTO(strID));
                 }
             }
         }
         if(!sto.isTypedef()) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.not_type, sto.getName()));
             ERROR = true;
             return (new ErrorSTO(sto.getName()));
         }
 
         return (sto);
     }
 
     //----------------------------------------------------------------
     //      DoBinaryOp
     //----------------------------------------------------------------
     STO DoBinaryOp(BinaryOp op, STO operand1, STO operand2)
     {
         // Check for previous errors in line and short circuit
         if(operand1.isError()) {
             return operand1;
         }
         if(operand2.isError()) {
             return operand2;
         }
 
         // Use BinaryOp.checkOperands() to perform error checks
         STO resultSTO = op.checkOperands(operand1, operand2);
 
         // If operands are constants, do the op
         if((!resultSTO.isError()) && (resultSTO.isConst()))
             resultSTO =  op.doOperation((ConstSTO)operand1, (ConstSTO)operand2, resultSTO.getType());
 
         // DON'T REFACTOR UNLESS YOU RUN PROJECT1 TESTS
         // THIS CODE IS IN THIS ORDER FOR A VERY SPECIFIC REASON
 
         // Process/Print errors
         if(resultSTO.isError()) {
             m_nNumErrors++;
             m_errors.print(resultSTO.getName());
             ERROR = true;
             return resultSTO;
         }
 
         // Code gen
         if(resultSTO.isConst()) {
             if(GenerateCode()) m_codegen.DoLiteral((ConstSTO)resultSTO);
         }
         else {
             if(GenerateCode()) {
                 if(op.isComparisonOp()) {
                     m_codegen.DoComparisonOp((ComparisonOp) op, operand1, operand2, resultSTO);
                 }
                 else {
                     m_codegen.DoBinaryOp(op, operand1, operand2, resultSTO);
                 }
             }
         }
 
         return resultSTO;
     }
 
     //----------------------------------------------------------------
     //      DoUnaryOp
     //----------------------------------------------------------------
     STO DoUnaryOp(UnaryOp op, STO operand)
     {
         // Check for previous errors in line and short circuit
         if(operand.isError()) {
             return operand;
         }
 
         // Use UnaryOp.checkOperand() to perform error checks
         STO resultSTO = op.checkOperand(operand);
 
         // If operands are constants, do the op
         if((!resultSTO.isError()) && (resultSTO.isConst())) {
             resultSTO = op.doOperation((ConstSTO)operand, resultSTO.getType());
         }
     
         // DON'T REFACTOR UNLESS YOU RUN PROJECT1 TESTS
         // THIS CODE IS IN THIS ORDER FOR A VERY SPECIFIC REASON
 
         // Process/Print errors
         if(resultSTO.isError()) {
             m_nNumErrors++;
             m_errors.print(resultSTO.getName());
             ERROR = true;
             return resultSTO;
         }
 
         // And better to have all the codegen code at the end of the function
 
         // Do Operation assembly code
         if(GenerateCode()) {
         // If operand is a constant, it was folded in it's OpClass, just allocate the result on stack
             if(resultSTO.isConst()) {
                 m_codegen.DoLiteral((ConstSTO)resultSTO);
             }
             // If it's addressOf op, call that function
             else if(op.isAddressOfOp()) {
                 m_codegen.DoAddressOf(operand, resultSTO);
             }
             // Otherwise, call generic DoUnaryOp
             else {
                 m_codegen.DoUnaryOp(op, operand, resultSTO);
             }
         }
         
         return resultSTO;
     }
     //----------------------------------------------------------------
     //      DoBooleanOp
     //----------------------------------------------------------------
     STO DoBooleanOp(BooleanOp op, STO operand1, STO operand2)
     {
         // Check for previous errors in line and short circuit
         if(operand1.isError()) {
             return operand1;
         }
         if(operand2.isError()) {
             return operand2;
         }
 
         STO resultSTO;
         // Use UnaryOp.checkOperand() to perform error checks
         if(op.isNotOp()) {
             resultSTO = op.checkOperand(operand1);
         }
         else {
             resultSTO = op.checkOperands(operand1, operand2);
         }
 
         // TODO: constant folding for these things
         // If operands are constants, do the op
         //if((!resultSTO.isError()) && (resultSTO.isConst())) {
             //resultSTO = op.doOperation((ConstSTO)operand, resultSTO.getType());
         //}
     
         // DON'T REFACTOR UNLESS YOU RUN PROJECT1 TESTS
         // THIS CODE IS IN THIS ORDER FOR A VERY SPECIFIC REASON
 
         // Process/Print errors
         if(resultSTO.isError()) {
             m_nNumErrors++;
             m_errors.print(resultSTO.getName());
             ERROR = true;
             return resultSTO;
         }
 
         // Code gen
         if(GenerateCode()) {
         // If operand is a constant, it was folded in it's OpClass, just allocate the result on stack
             //if(resultSTO.isConst()) {
             //    m_codegen.DoLiteral((ConstSTO)resultSTO);
             //}
             m_codegen.DoBooleanOp(op, operand1, operand2, resultSTO);
 
             /*
             if(isStart) {
                 m_codegen.DoBooleanOp1(op, operand1);
             }
             else {
                 m_codegen.DoBooleanOp2(op, operand2, resultSTO);
             }
             */
         }
         
         return resultSTO;
     }
 
 
 
     //----------------------------------------------------------------
     //      DoWhileStart
     //----------------------------------------------------------------
     void DoWhileStart()
     {
         if(GenerateCode()) m_codegen.DoWhileStart();
     }
 
     //----------------------------------------------------------------
     //      DoWhileExpr
     //----------------------------------------------------------------
     STO DoWhileExpr(STO stoExpr)
     {
         // Check for previous errors in line and short circuit
         if(stoExpr.isError()) {
             return stoExpr;
         }
 
         // Check #4 - while expr - int or bool
         if((!stoExpr.getType().isInt()) &&(!stoExpr.getType().isBool())) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error4_Test, stoExpr.getType().getName()));
             ERROR = true;
             return (new ErrorSTO("DoWhile error"));
         }
         
         if(GenerateCode()) m_codegen.DoWhile(stoExpr);
         loopLevelUp();
         
         return stoExpr;
     }
     
     //----------------------------------------------------------------
     //      DoWhileCodeBlock
     //----------------------------------------------------------------
     void DoWhileCodeBlock()
     {
         if(GenerateCode()) m_codegen.DoWhileCodeBlock();
     }
     
     //----------------------------------------------------------------
     //      DoIfExpr
     //----------------------------------------------------------------
     STO DoIfExpr(STO stoExpr)
     {
         // Check for previous errors in line and short circuit
         if(stoExpr.isError()) {
             return stoExpr;
         }
 
         // Check #4 - if expr - int or bool
         if((!stoExpr.getType().isInt()) &&(!stoExpr.getType().isBool())) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error4_Test, stoExpr.getType().getName()));
             ERROR = true;
             return (new ErrorSTO("DoIf error"));
         }
         //if(GenerateCode()) m_codegen.DoIf((ConstSTO) stoExpr);
         if(GenerateCode()) m_codegen.DoIf(stoExpr);
         return stoExpr;
     }
 
     //----------------------------------------------------------------
     //      DoReturnStmt_1 - No return value
     //----------------------------------------------------------------
     STO DoReturnStmt_1()
     {
         FuncSTO stoFunc;
 
         if((stoFunc = m_symtab.getFunc()) == null) {
             m_nNumErrors++;
             m_errors.print("internal: DoReturnStmt_1 says no proc!");
             ERROR = true;
             return (new ErrorSTO("DoReturnStmt_1 Error"));
         }
 
         // Check #6a - no expr on non-void rtn
         if(!stoFunc.getReturnType().isVoid()) {
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error6a_Return_expr);
             ERROR = true;
             return (new ErrorSTO("DoReturnStmt_1 Error"));
         }
 
         // valid return statement, set func.hasReturnStatement if at right level
         if(stoFunc.getLevel() == m_symtab.getLevel()) {
             stoFunc.setHasReturnStatement(true);
         }
 
         ExprSTO returnSto = new ExprSTO(stoFunc.getName() + " Return", new VoidType());
 
         if(GenerateCode()) m_codegen.DoReturn(stoFunc, returnSto); 
 
         return returnSto;
     }
 
     //----------------------------------------------------------------
     //      DoReturnStmt_2 - With return value
     //----------------------------------------------------------------
     STO DoReturnStmt_2(STO stoExpr)
     {
         FuncSTO stoFunc;
 
         // Check for previous errors in line and short circuit
         if(stoExpr.isError()) {
             return stoExpr;
         }
 
         if((stoFunc = m_symtab.getFunc()) == null) {
             m_nNumErrors++;
             m_errors.print("internal: DoReturnStmt_2 says no proc!");
             ERROR = true;
             return (new ErrorSTO("DoReturnStmt_2 Error"));
         }
 
         // Check #6b - 1st bullet - rtn by val - rtn expr type not assignable to return
         if(!stoFunc.getReturnByRef()) {
             if(!stoExpr.getType().isAssignable(stoFunc.getReturnType())) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg. error6a_Return_type, stoExpr.getType().getName(), stoFunc.getReturnType().getName()));
                 ERROR = true;
                 return (new ErrorSTO("DoReturnStmt_2 Error"));
             }
         }
         else {
             // Check #6b - 2nd bullet - rtn by ref - rtn expr type not equivalent to return type
             if(!stoExpr.getType().isEquivalent(stoFunc.getReturnType())) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error6b_Return_equiv, stoExpr.getType().getName(), stoFunc.getReturnType().getName()));
                 ERROR = true;
                 return (new ErrorSTO("DoReturnStmt_2 Error"));
             }
 
             // Check #6b - 3rd bullet - rtn by ref - rtn expr not modLValue
             if(!stoExpr.isModLValue()) {
                 m_nNumErrors++;
                 m_errors.print(ErrorMsg.error6b_Return_modlval);
                 ERROR = true;
                 return (new ErrorSTO("DoReturnStmt_2 Error"));
             }
         }
 
         // valid return statement, set func.hasReturnStatement if at right level
         if(stoFunc.getLevel() == m_symtab.getLevel()) {
             stoFunc.setHasReturnStatement(true);
         }
 
         if(GenerateCode()) m_codegen.DoReturn(stoFunc, stoExpr); 
 
         return stoExpr;
     }
 
     //----------------------------------------------------------------
     //      DoExitStmt
     //----------------------------------------------------------------
     STO DoExitStmt(STO stoExpr)
     {
         // Check for previous errors in line and short circuit
         if(stoExpr.isError()) {
             return stoExpr;
         }
 
         // Check #7 - exit value assignable to int
         if(!stoExpr.getType().isAssignable(new IntType())) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error7_Exit, stoExpr.getType().getName()));
             ERROR = true;
         }
 
         if(GenerateCode()) m_codegen.DoExit(stoExpr);
 
         return stoExpr;
     }
 
     //----------------------------------------------------------------
     //      DoBreakStmt
     //----------------------------------------------------------------
     void DoBreakStmt()
     {
         // Check #12 - break statement in while loop
         if(m_loopLevel <= 0) {
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error12_Break);
             ERROR = true;
         }
         
         if(GenerateCode()) m_codegen.DoBreakStmt();
     }
 
     //----------------------------------------------------------------
     //      DoContinueStmt
     //----------------------------------------------------------------
     void DoContinueStmt()
     {
         // Check #12 - continue statement in while loop
         if(m_loopLevel <= 0) {
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error12_Continue);
             ERROR = true;
         }
         
         if(GenerateCode()) m_codegen.DoContinueStmt();
     }
 
     //----------------------------------------------------------------
     //      loopLevelUp
     //----------------------------------------------------------------
     void loopLevelUp()
     {
         m_loopLevel++;
     }
 
     //----------------------------------------------------------------
     //      loopLevelDown
     //----------------------------------------------------------------
     void loopLevelDown()
     {
         m_loopLevel--;
     }
     
     //----------------------------------------------------------------
     //      DoSizeOf
     //----------------------------------------------------------------
     STO DoSizeOf(STO sto, Type type)
     {
         int size = 0;
         ConstSTO constSTO;
         //Either type or type is null
         if (sto == null) {
             size = type.getSize();
         } 
         else if (type == null){
             if (sto.getIsAddressable() && sto.getType() != null) {
                 size = sto.getType().getSize();
             } 
             else {
                 m_nNumErrors++;
                 m_errors.print(ErrorMsg.error19_Sizeof);
                 ERROR = true;
             }
         } 
         else {
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error19_Sizeof);
             ERROR = true;
         }
         
         constSTO = new ConstSTO("ConstInt", new IntType("int",4), (double)size);
         if(GenerateCode()) m_codegen.DoLiteral(constSTO);
         return constSTO;
     }
 
     //----------------------------------------------------------------
     //      DoBuildType
     //----------------------------------------------------------------
     Type DoBuildType(Type subType, Type ptrType, STO arrayIndex)
     {
         Type returnType = subType;
 
         if(ptrType != null) {
             ((PointerType) ptrType).setBottomPtrType(returnType);
             ((PointerType) ptrType).setInitialName();
             returnType = ptrType;
         }
 
         // Check if arrayIndex is null - this means it is not an array
         if(arrayIndex != null) {
             returnType = DoArrayDecl(returnType, arrayIndex);
         }
             
         return returnType;
     }
     
     //----------------------------------------------------------------
     //      DoTypeCast Check #20
     //----------------------------------------------------------------
     STO DoTypeCast(Type castingType, STO castedSTO)
     {
         if(castedSTO.isError()) return castedSTO;
         
         STO resultSTO = castedSTO;
         Type castedType = castedSTO.getType();
         
         // No casting of function pointers, so isPointer() is good
 
         if(castedSTO.isConst()) {
             // If it's const conversion, it follows a special casting rule
             ConstSTO constSTO = (ConstSTO) castedSTO;
             String newConstName = "casted_" + constSTO.getName() +"_from_"+castedType.getName();
             
             // bool --> int, float, pointer
             if(castedType.isBool() && (castingType.isInt() || castingType.isFloat() || castingType.isPointer())) {
                 if(constSTO.getBoolValue()) {
                     resultSTO = new ConstSTO(newConstName, castingType, "1");
                 } 
                 else {
                     resultSTO = new ConstSTO(newConstName, castingType, "0");
                 }
             }
             // int, float, pointer --> bool
             else if((castedType.isInt() || castedType.isFloat() || castedType.isPointer()) && castingType.isBool()) {
                 if(constSTO.getValue() == 0) {
                     resultSTO = new ConstSTO(newConstName, castingType, "false");
                 }
                 else if(constSTO.getValue() != 0) {
                     resultSTO = new ConstSTO(newConstName, castingType, "true");
                 }
             }
             // float --> int, pointer
             else if(castedType.isFloat() && (castingType.isInt() || castingType.isPointer())) {
                 resultSTO = new ConstSTO(newConstName, castingType, Integer.toString((constSTO.getValue().intValue())));
             }
             // int, float --> float
             else if((castedType.isInt() || castedType.isPointer()) && castingType.isFloat()) {
                 resultSTO = new ConstSTO(newConstName, castingType, constSTO.getValue());
             }
             // int <--> pointer
             else if((castedType.isInt() && castingType.isPointer()) || (castedType.isPointer() && castingType.isInt())) {
                 resultSTO = new ConstSTO(newConstName, castingType, constSTO.getValue());
             } 
             else {
                 m_nNumErrors++;
                 m_errors.print(String.format(ErrorMsg.error20_Cast, castedType, castingType));
                 ERROR = true;
                 resultSTO = new ErrorSTO("Casting Error");
             }
         }
         else {
             // alias who's type is basic
             if(castedSTO.isTypedef() && castedType.isBasic()) {
                 resultSTO = new VarSTO("casted_" + castedSTO.getName() +"_from_"+castedType.getName(),castingType);
             }
             // basic type or pointer
             else if(!castedSTO.isTypedef() && (castedType.isBasic() || castedType.isPointer())) {
                 resultSTO = new VarSTO("casted_" + castedSTO.getName() +"_from_"+castedType.getName(),castingType);
             }
             else {
                 m_nNumErrors++;
                 m_errors.print(String.format(ErrorMsg.error20_Cast, castedType, castingType));
                 ERROR = true;
                 resultSTO = new ErrorSTO("Casting Error");
             }
         }
         
         // Makes sure the result is R-Val
         resultSTO.setIsModLValue(false);
         return resultSTO;
     }
     //----------------------------------------------------------------
     //      DoNew
     //----------------------------------------------------------------
     void DoNew(STO sto)
     {
         if(!sto.isModLValue()){
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error16_New_var);
             ERROR = true;
         }
 
         // Can't call "new" on nullptr or function pointers - see @115
         if(!sto.getType().isPtrGrp() || sto.getType().isNullPtr()) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error16_New, sto.getType().getName()));
             ERROR = true;
         }
     }
     //----------------------------------------------------------------
     //      DoDelete
     //----------------------------------------------------------------
     void DoDelete(STO sto)
     {
         if(!sto.isModLValue()){
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error16_Delete_var);
             ERROR = true;
         }
         
         // Can't call "delete" on nullptr or function pointer
         if(!sto.getType().isPointer() || sto.getType().isNullPtr()) {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error16_Delete, sto.getType().getName()));
             ERROR = true;
         }
     }
     
     //----------------------------------------------------------------
     //      DoCout
     //----------------------------------------------------------------
     void DoCout(STO exprSto) 
     {
         if(GenerateCode()) m_codegen.DoCout(exprSto);
     }
     
     //----------------------------------------------------------------
     //      DoLiteral
     //----------------------------------------------------------------
     STO DoLiteral(ConstSTO sto) 
     {
         if(GenerateCode()) m_codegen.DoLiteral(sto);
 
         return sto;
     }
     
     //----------------------------------------------------------------
     //      DoIfCodeBlock
     //----------------------------------------------------------------
     void DoIfCodeBlock() 
     {
         if(GenerateCode()) m_codegen.DoIfCodeBlock();
     }
     
     //----------------------------------------------------------------
     //      DoPrePost
     //----------------------------------------------------------------
     void DoPost(Operator op, boolean isPost) 
     {
         if (op.isIncOp()) {
             ((IncOp)op).setPost(isPost);
         }
         else if(op.isDecOp()) {
             ((DecOp)op).setPost(isPost);
         }
     }
     //----------------------------------------------------------------
     //      DoCin
     //----------------------------------------------------------------
     void DoCin(STO sto) 
     {
         if(GenerateCode()) m_codegen.DoCin(sto);
     }
     
     //----------------------------------------------------------------
     //      DoIfElseCodeBlock
     //----------------------------------------------------------------
     void DoIfElseCodeBlock() 
     {
         if(GenerateCode()) m_codegen.DoIfElseCodeBlock();
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO DoIterationVarDecl(Type type, Boolean isRef, String id)
     {
         if (m_symtab.accessLocal(id) != null)
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
         }
 
         VarSTO sto = new VarSTO(id, type);
 
         if(isRef) {
             sto.setIsReference(true);
         }
 
         m_symtab.insert(sto);
 
         return sto;
     }
 
 
     //----------------------------------------------------------------
     //      DoForeachStart
     //----------------------------------------------------------------
     void DoForeachStart()
     {
         // TODO: Update For functions
         //if(GenerateCode()) m_codegen.DoForeachStart();
     }
 
     //----------------------------------------------------------------
     //      DoForeachExpr
     //----------------------------------------------------------------
     STO DoForeachExpr(STO iterationSto, STO exprSto)
     {
         // Check for previous errors in line and short circuit
         if(exprSto.isError()) {
             return exprSto;
         }
 
         // Check #12 - foreach loop - type of expr is not an array type
         if(!exprSto.getType().isArray()) {
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error12a_Foreach);
             ERROR = true;
             return (new ErrorSTO("DoForeach error"));
         }
 
         if(!iterationSto.isReference()) {
 
             // Check #12 - foreach loop - iterationVar is value, expr not assignable
             if(!((ArrayType) exprSto.getType()).getElementType().isAssignable(iterationSto.getType())) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error12v_Foreach, ((ArrayType) exprSto.getType()).getElementType().getName(), iterationSto.getName(), iterationSto.getType().getName()));
                 ERROR = true;
                 return (new ErrorSTO("DoForeach error"));
             }
         }
         else {
 
             // Check #12 - foreach loop - iterationVar is ref, expr not equivalent
             if(!((ArrayType) exprSto.getType()).getElementType().isEquivalent(iterationSto.getType())) {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error12r_Foreach, ((ArrayType) exprSto.getType()).getElementType().getName(), iterationSto.getName(), iterationSto.getType().getName()));
                 ERROR = true;
                 return (new ErrorSTO("DoForeach error"));
             }
         }
         
         // TODO: Update For functions
         //if(GenerateCode()) m_codegen.DoForeach(exprSto);
         loopLevelUp();
         
         return exprSto;
     }
     
     //----------------------------------------------------------------
     //      DoForeachCodeBlock
     //----------------------------------------------------------------
     void DoForeachCodeBlock()
     {
         // TODO: Update For functions
         //if(GenerateCode()) m_codegen.DoForeachCodeBlock();
     }
 
     //----------------------------------------------------------------
     //      DeclTypeOn
     //----------------------------------------------------------------
     void DeclTypeOn()
     {
         DECLTYPE = true;
     }
 
     //----------------------------------------------------------------
     //      DeclTypeOff
     //----------------------------------------------------------------
     void DeclTypeOff()
     {
         DECLTYPE = false;
     }
 
 
 
 }
