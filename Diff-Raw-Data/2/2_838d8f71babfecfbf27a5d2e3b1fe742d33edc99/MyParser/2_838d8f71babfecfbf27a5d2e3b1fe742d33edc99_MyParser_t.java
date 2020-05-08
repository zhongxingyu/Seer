 
 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 import java_cup.runtime.*;
 import java.util.Vector;
 
 
 
 class MyParser extends parser
 {
 
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
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public 
     MyParser(Lexer lexer, ErrorPrinter errors)
     {
         m_lexer = lexer;
         m_symtab = new SymbolTable();
         m_errors = errors;
         m_nNumErrors = 0;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public boolean
     Ok()
     {
         return (m_nNumErrors == 0);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public Symbol
     scan()
     {
         Token t = m_lexer.GetToken();
 
         // We'll save the last token read for error messages.
         // Sometimes, the token is lost reading for the next
         // token which can be null.
         m_strLastLexeme = t.GetLexeme();
 
         switch(t.GetCode())
         {
             case sym.T_ID:
             case sym.T_ID_U:
             case sym.T_STR_LITERAL:
             case sym.T_FLOAT_LITERAL:
             case sym.T_INT_LITERAL:
             case sym.T_CHAR_LITERAL:
                 return (new Symbol(t.GetCode(), t.GetLexeme()));
             default:
                 return (new Symbol(t.GetCode()));
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void
     syntax_error(Symbol s)
     {
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void
     report_fatal_error(Symbol s)
     {
         m_nNumErrors++;
         if(m_bSyntaxError)
         {
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
     public void
     unrecovered_syntax_error(Symbol s)
     {
         report_fatal_error(s);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void
     DisableSyntaxError()
     {
         m_bSyntaxError = false;
     }
 
     public void
     EnableSyntaxError()
     {
         m_bSyntaxError = true;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public String 
     GetFile()
     {
         return (m_lexer.getEPFilename());
     }
 
     public int
     GetLineNum()
     {
         return (m_lexer.getLineNumber());
     }
 
     public void
     SaveLineNum()
     {
         m_nSavedLineNum = m_lexer.getLineNumber();
     }
 
     public int
     GetSavedLineNum()
     {
         return (m_nSavedLineNum);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoProgramStart()
     {
         // Opens the global scope.
         m_symtab.openScope();
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoProgramEnd()
     {
         m_symtab.closeScope();
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoVarDecl(Type type, Vector<String> lstIDs)
     {
         for(int i = 0; i < lstIDs.size(); i++)
         {
             String id = lstIDs.elementAt(i);
         
             if(m_symtab.accessLocal(id) != null)
             {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
             }
 
             VarSTO sto = new VarSTO(id, type);
             m_symtab.insert(sto);
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoExternDecl(Vector<String> lstIDs)
     {
         for(int i = 0; i < lstIDs.size(); i++)
         {
             String id = lstIDs.elementAt(i);
 
             if(m_symtab.accessLocal(id) != null)
             {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
             }
 
             VarSTO sto = new VarSTO(id);
             m_symtab.insert(sto);
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoConstDecl(Vector<String> lstIDs)
     {
         for(int i = 0; i < lstIDs.size(); i++)
         {
             String id = lstIDs.elementAt(i);
 
             if(m_symtab.accessLocal(id) != null)
             {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
             }
         
             ConstSTO sto = new ConstSTO(id);
             m_symtab.insert(sto);
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoTypedefDecl(Vector<String> lstIDs)
     {
         for(int i = 0; i < lstIDs.size(); i++)
         {
             String id = lstIDs.elementAt(i);
 
             if(m_symtab.accessLocal(id) != null)
             {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
             }
         
             TypedefSTO sto = new TypedefSTO(id);
             m_symtab.insert(sto);
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoStructdefDecl(String id)
     {
         if(m_symtab.accessLocal(id) != null)
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
         }
         
         TypedefSTO sto = new TypedefSTO(id);
         m_symtab.insert(sto);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoFuncDecl_1(Type returnType, String id)
     {
         if(m_symtab.accessLocal(id) != null)
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
         }
     
         FuncSTO sto = new FuncSTO(id);
         
         // Set return type
         sto.setReturnType(returnType);
 
         m_symtab.insert(sto);
 
         m_symtab.openScope();
         m_symtab.setFunc(sto);
 
         // Set the function's level
         sto.setLevel(m_symtab.getLevel());
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoFuncDecl_2()
     {
         // Check #6c - no return statement for non-void type function
         FuncSTO stoFunc;
 
         if((stoFunc = m_symtab.getFunc()) == null)
         {
             m_nNumErrors++;
             m_errors.print("internal: DoFuncDecl_2 says no proc!");
             return;
         }
 
         if(!stoFunc.getReturnType().isVoid())
         {
             if(!stoFunc.getHasReturnStatement())
             {
                 m_nNumErrors++;
                 m_errors.print(ErrorMsg.error6c_Return_missing);
             }
 
         }
 
         m_symtab.closeScope();
         m_symtab.setFunc(null);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoFormalParams(Vector<ParamSTO> params)
     {
         FuncSTO stoFunc;
 
         if((stoFunc = m_symtab.getFunc()) == null)
         {
             m_nNumErrors++;
             m_errors.print("internal: DoFormalParams says no proc!");
             return;
         }
 
         // Insert parameters
         stoFunc.setParameters(params);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoBlockOpen()
     {
         // Open a scope.
         m_symtab.openScope();
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoBlockClose()
     {
         m_symtab.closeScope();
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoAssignExpr(STO stoDes, STO stoValue)
     {
         // Check for previous errors in line and short circuit
         if(stoDes.isError())
         {
             return stoDes;
         }
         if(stoValue.isError())
         {
             return stoValue;
         }
 
         // Check #3a - illegal assignment - not modifiable L-value
         if(!stoDes.isModLValue())
         {
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error3a_Assign);
             return (new ErrorSTO("DoAssignExpr Error - not mod-L-Value"));
         }
 
         if(!stoValue.getType().isAssignable(stoDes.getType()))
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign, stoValue.getType().getName(), stoDes.getType().getName()));
             return (new ErrorSTO("DoAssignExpr Error - bad types"));
         }
         
         return stoDes;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoFuncCall(STO sto, Vector<ExprSTO> args)
     {
         if(!sto.isFunc())
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.not_function, sto.getName()));
             return (new ErrorSTO(sto.getName()));
         }
     
         // We know it's a function, do function call checks
         FuncSTO stoFunc =(FuncSTO)sto;
 
         // Check #5
         // Check #5a - # args = # params
        if((stoFunc.getNumOfParams() != args.size()))
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error5n_Call, args.size(), stoFunc.getNumOfParams()));
             return (new ErrorSTO("DoFuncCall - # args"));
         }
 
         // Now we check each arg individually, accepting one error per arg
         
         boolean error_flag = false;
 
         for(int i = 0; i < args.size(); i++)
         {
             // For readability and shorter lines
             ParamSTO thisParam = stoFunc.getParameters().elementAt(i);
             ExprSTO thisArg = args.elementAt(i);
 
             // Check #5b - non-assignable arg for pass-by-value param
             if(!thisParam.isPassByReference())
             {
                 if(!thisArg.getType().isAssignable(thisParam.getType()))
                 {
                     m_nNumErrors++;
                     m_errors.print(Formatter.toString(ErrorMsg.error5a_Call, thisArg.getType().getName(), thisParam.getName(), thisParam.getType().getName()));
                     error_flag = true;
                 }
             }
 
             // Check #5c - arg type not equivalent to pass-by-ref param type
             else if(thisParam.isPassByReference())
             {
                 if(!thisArg.getType().isEquivalent(thisParam.getType()))
                 {
                     m_nNumErrors++;
                     m_errors.print(Formatter.toString(ErrorMsg.error5r_Call, thisArg.getType().getName(), thisParam.getName(), thisParam.getType().getName()));
                     error_flag = true;
                 }
             }
 
             // Check #5d - arg not modifiable l-value for pass by ref param
             else if(thisParam.isPassByReference())
             {
                 if(!thisArg.isModLValue())
                 {
                     m_nNumErrors++;
                     m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, thisArg.getName(), thisArg.getType().getName()));
                     error_flag = true;
                 }
             }
         }
 
         if(error_flag)
         {
             // Error occured in at least one arg, return error
             return (new ErrorSTO("DoFuncCall - Check 5"));
         }
         else
             // Func call legal, return function return type
             return (new ExprSTO(stoFunc.getName() + " return type", stoFunc.getReturnType()));
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoDesignator2_Dot(STO sto, String strID)
     {
         // Good place to do the struct checks
 
         return sto;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoDesignator2_Array(STO sto)
     {
         // Good place to do the array checks
 
         return sto;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoDesignator3_GlobalID(String strID)
     {
         STO sto;
 
         if((sto = m_symtab.accessGlobal(strID)) == null)
         {
             m_nNumErrors++;
              m_errors.print(Formatter.toString(ErrorMsg.error0g_Scope, strID));    
             sto = new ErrorSTO(strID);
         }
         return (sto);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoDesignator3_ID(String strID)
     {
         STO sto;
 
         if((sto = m_symtab.access(strID)) == null)
         {
             m_nNumErrors++;
              m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));    
             sto = new ErrorSTO(strID);
         }
         return (sto);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoQualIdent(String strID)
     {
         STO sto;
 
         if((sto = m_symtab.access(strID)) == null)
         {
             m_nNumErrors++;
              m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));    
             return (new ErrorSTO(strID));
         }
 
         if(!sto.isTypedef())
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.not_type, sto.getName()));
             return (new ErrorSTO(sto.getName()));
         }
 
         return (sto);
     }
 
     //----------------------------------------------------------------
     //      DoBinaryOp
     //----------------------------------------------------------------
     STO
     DoBinaryOp(BinaryOp op, STO operand1, STO operand2)
     {
         // Check for previous errors in line and short circuit
         if(operand1.isError())
         {
             return operand1;
         }
         if(operand2.isError())
         {
             return operand2;
         }
 
         // Use BinaryOp.checkOperands() to perform error checks
         STO resultSTO = op.checkOperands(operand1, operand2);
 
         // Process/Print errors
         if(resultSTO.isError())
         {
             m_nNumErrors++;
             m_errors.print(resultSTO.getName());
         }
 
         return resultSTO;
     }
 
     //----------------------------------------------------------------
     //      DoUnaryOp
     //----------------------------------------------------------------
     STO
     DoUnaryOp(UnaryOp op, STO operand)
     {
         // Check for previous errors in line and short circuit
         if(operand.isError())
         {
             return operand;
         }
 
         // Use UnaryOp.checkOperand() to perform error checks
         STO resultSTO = op.checkOperand(operand);
 
         // Process/Print errors
         if(resultSTO.isError())
         {
             m_nNumErrors++;
             m_errors.print(resultSTO.getName());
         }
 
         return resultSTO;
     }
 
     //----------------------------------------------------------------
     //      DoWhileExpr
     //----------------------------------------------------------------
     STO
     DoWhileExpr(STO stoExpr)
     {
         // Check for previous errors in line and short circuit
         if(stoExpr.isError())
         {
             return stoExpr;
         }
 
         // Check #4 - while expr - int or bool
         if((!stoExpr.getType().isInt()) &&(!stoExpr.getType().isBool()))
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error4_Test, stoExpr.getType().getName()));
             return (new ErrorSTO("DoWhile error"));
         }
 
         return stoExpr;
     }
 
     //----------------------------------------------------------------
     //      DoIfExpr
     //----------------------------------------------------------------
     STO
     DoIfExpr(STO stoExpr)
     {
         // Check for previous errors in line and short circuit
         if(stoExpr.isError())
         {
             return stoExpr;
         }
 
         // Check #4 - if expr - int or bool
         if((!stoExpr.getType().isInt()) &&(!stoExpr.getType().isBool()))
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error4_Test, stoExpr.getType().getName()));
             return (new ErrorSTO("DoIf error"));
         }
 
         return stoExpr;
     }
 
     //----------------------------------------------------------------
     //      DoReturnStmt_1
     //----------------------------------------------------------------
     STO
     DoReturnStmt_1()
     {
         FuncSTO stoFunc;
 
         if((stoFunc = m_symtab.getFunc()) == null)
         {
             m_nNumErrors++;
             m_errors.print("internal: DoReturnStmt_1 says no proc!");
             return (new ErrorSTO("DoReturnStmt_1 Error"));
         }
     
         // Check #6a - no expr on non-void rtn
         if(!stoFunc.getReturnType().isVoid())
         {
             m_nNumErrors++;
             m_errors.print(ErrorMsg.error6a_Return_expr);
             return (new ErrorSTO("DoReturnStmt_1 Error"));
         }
         
         // valid return statement, set func.hasReturnStatement
         stoFunc.setHasReturnStatement(true);
 
         return (new ExprSTO(stoFunc.getName() + " Return", new VoidType()));
 
     }
 
     //----------------------------------------------------------------
     //      DoReturnStmt_2
     //----------------------------------------------------------------
     STO
     DoReturnStmt_2(STO stoExpr)
     {
         FuncSTO stoFunc;
 
         // Check for previous errors in line and short circuit
         if(stoExpr.isError())
         {
             return stoExpr;
         }
 
         if((stoFunc = m_symtab.getFunc()) == null)
         {
             m_nNumErrors++;
             m_errors.print("internal: DoReturnStmt_2 says no proc!");
             return (new ErrorSTO("DoReturnStmt_2 Error"));
         }
          
         // Check #6b - 1st bullet - rtn by val - rtn expr type not assignable to return
         if(!stoFunc.getReturnByRef())
         {
             if(!stoExpr.getType().isAssignable(stoFunc.getReturnType()))
             {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg. error6a_Return_type, stoExpr.getType().getName(), stoFunc.getReturnType().getName()));
                 return (new ErrorSTO("DoReturnStmt_2 Error"));
             }
         }
         else
         {
             // Check #6b - 2nd bullet - rtn by ref - rtn expr type not equivalent to return type
             if(!stoExpr.getType().isEquivalent(stoFunc.getReturnType()))
             {
                 m_nNumErrors++;
                 m_errors.print(Formatter.toString(ErrorMsg.error6b_Return_equiv, stoExpr.getType().getName(), stoFunc.getReturnType().getName()));
                 return (new ErrorSTO("DoReturnStmt_2 Error"));
             }
 
             // Check #6b - 3rd bullet - rtn by ref - rtn expr not modLValue
             if(!stoExpr.isModLValue())
             {
                 m_nNumErrors++;
                 m_errors.print(ErrorMsg.error6b_Return_modlval);
                 return (new ErrorSTO("DoReturnStmt_2 Error"));
             }
         }
 
         // valid return statement, set func.hasReturnStatement
         stoFunc.setHasReturnStatement(true);
 
         return stoExpr;
     }
 
     //----------------------------------------------------------------
     //      DoExitStmt
     //----------------------------------------------------------------
     STO
     DoExitStmt(STO stoExpr)
     {
         // Check for previous errors in line and short circuit
         if(stoExpr.isError())
         {
             return stoExpr;
         }
 
         // Check #7 - exit value assignable to int
         if(!stoExpr.getType().isAssignable(new IntType()))
         {
             m_nNumErrors++;
             m_errors.print(Formatter.toString(ErrorMsg.error7_Exit, stoExpr.getType().getName()));
         }
         
         return stoExpr;
     }
     
 
 }
