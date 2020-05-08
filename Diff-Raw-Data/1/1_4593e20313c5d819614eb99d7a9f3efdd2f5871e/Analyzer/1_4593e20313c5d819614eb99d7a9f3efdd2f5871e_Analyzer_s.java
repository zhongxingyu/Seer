 package compiler.analyzer;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Stack;
 
 import compiler.TokenType;
 import compiler.parser.Parser;
 import compiler.parser.RecordType;
 import compiler.parser.SemanticRec;
 import compiler.parser.symbol.Classification;
 import compiler.parser.symbol.DataRow;
 import compiler.parser.symbol.FunctionRow;
 import compiler.parser.symbol.Mode;
 import compiler.parser.symbol.ProcedureRow;
 import compiler.parser.symbol.Row;
 import compiler.parser.symbol.SymbolTable;
 import compiler.parser.symbol.Type;
 
 public class Analyzer {
     Stack<SymbolTable> symboltables;
     PrintWriter out;
 
     public Analyzer(Stack<SymbolTable> tables, File output) throws IOException
     {
         symboltables = tables;
         out = new PrintWriter(new FileWriter(output), true);
     }
 
     /**
      * 
      * @param sr
      *            {@link RecordType#IDENTIFIER}
      * @return
      */
     private Row getIdRowFromSR(SemanticRec sr)
     {
         String lex = sr.getDatum(1);
         Classification classify = Classification.valueOf(sr.getDatum(0));
         Row r = findSymbol(lex, classify);
         if (r == null)
         {
             Parser.semanticError("Identifier : type " + classify.toString() + " lexeme " + lex
                     + " not declared in current scope");
         }
         return r;
     }
 
     /**
      * 
      * @param sr
      *            with {@link RecordType#LITERAL} or {@link RecordType#IDENTIFIER}
      * @return
      */
     private Type getTypeFromSR(SemanticRec sr)
     {
         Type t = null;
         if (sr.getRecType() == RecordType.IDENTIFIER)
         {
             Row r = getIdRowFromSR(sr);
             t = r.getType();
         }
         else if (sr.getRecType() == RecordType.LITERAL) //Literal
         {
             t = Type.valueOf(sr.getDatum(0));
         }
         else //TODO: add Function rows here
         {
             Parser.semanticError("RecordType" + sr.getRecType() + " does not have a Type");
         }
         return t;
     }
 
     private String getRegisterFromNL(String nestLvl) {
         return "D" + nestLvl;
     }
 
     private String generateOffset(SymbolTable tbl, DataRow data) {
         String nesting = "" + tbl.getNestingLevel();
         String memOffset = "" + data.getMemOffset();
         nesting = getRegisterFromNL(nesting);
         return memOffset + "(" + nesting + ")";
     }
 
     /*
      * VM ASM methods section begin
      */
 
     /**
      * Pushes the value at the memory location to the stack
      * 
      * @param memLoc
      *            string with format from VM definition e.g (0(D0), #"string", etc)
      */
     private void push(String memLoc)
     {
         out.println("push " + memLoc);
     }
 
     /**
      * Pops the value from the stack into the memory location
      * 
      * @param memLoc
      *            string with format from VM definition e.g (0(D0), #"string", etc)
      */
     private void pop(String memLoc)
     {
         out.println("pop " + memLoc);
     }
 
     /**
      * 
      * @param src
      * @param dst
      */
     private void move(String src, String dst)
     {
         out.println("mov " + src + " " + dst);
     }
 
     /**
      * 
      * @param src1
      * @param src2
      * @param dst
      */
     private void add(String src1, String src2, String dst)
     {
         out.println("add " + src1 + " " + src2 + " " + dst);
     }
 
     /**
      * 
      * @param src1
      * @param src2
      * @param dst
      */
     private void sub(String src1, String src2, String dst) {
         out.println("sub " + src1 + " " + src2 + " " + dst);
     }
 
     /**
      * 
      */
     private void not()
     {
         out.println("nots");
     }
 
     /**
      * 
      */
     private void and()
     {
         out.println("ands");
     }
 
     /**
      * 
      */
     private void or()
     {
         out.println("ors");
     }
 
     private void mulStackI()
     {
         out.println("muls");
     }
 
     private void divStackI()
     {
         out.println("divs");
     }
 
     private void modStackI()
     {
         out.println("mods");
     }
 
     private void mulStackF()
     {
         out.println("mulsf");
     }
 
     private void divStackF()
     {
         out.println("divsf");
     }
 
     private void modStackF()
     {
         out.println("modsf");
     }
 
     private void subStackI()
     {
         out.println("subs");
     }
 
     private void addStackI()
     {
         out.println("adds");
     }
 
     private void subStackF()
     {
         out.println("subsf");
     }
 
     private void addStackF()
     {
         out.println("addsf");
     }
 
     private void negateStackI()
     {
         out.println("negs");
     }
 
     private void negateStackF()
     {
         out.println("negsf");
     }
 
     private void notEqualI()
     {
         out.println("cmpnes");
     }
 
     private void greaterEqualI()
     {
         out.println("cmpges");
     }
 
     private void lessEqualI()
     {
         out.println("cmples");
     }
 
     private void greaterThanI()
     {
         out.println("cmpgts");
     }
 
     private void lessThanI()
     {
         out.println("cmplts");
     }
 
     private void equalI()
     {
         out.println("cmpeqs");
     }
 
     private void notEqualF()
     {
         out.println("cmpnesf");
     }
 
     private void greaterEqualF()
     {
         out.println("cmpgesf");
     }
 
     private void lessEqualF()
     {
         out.println("cmplesf");
     }
 
     private void greaterThanF()
     {
         out.println("cmpgtsf");
     }
 
     private void lessThanF()
     {
         out.println("cmpltsf");
     }
 
     private void equalF()
     {
         out.println("cmpeqsf");
     }
 
     private void castStackToI()
     {
         out.println("castsi");
     }
 
     private void castStackToF()
     {
         out.println("castsf");
     }
 
     private void writeStack()
     {
         out.println("wrts");
     }
 
     private void writelnStack()
     {
         out.println("wrtlns");
     }
 
     private void readI(String offset)
     {
         out.println("rd " + offset);
     }
 
     private void readF(String offset)
     {
         out.println("rdf " + offset);
     }
 
     private void readS(String offset)
     {
         out.println("rds " + offset);
     }
 
     /**
      * Halts program execution
      */
     private void halt()
     {
         out.println("hlt");
     }
 
     private void label(String label) {
         out.println(label + ":");
     }
 
     private void branchTrue(String label) {
         out.println("brts " + label);
     }
 
     private void branchFalse(String label) {
         out.println("brfs " + label);
     }
 
     private void branchUnconditional(String label) {
         out.println("br " + label);
     }
 
     private void ret()
     {
         out.println("ret");
     }
 
     private void call(String label)
     {
         out.println("call " + label);
     }
 
     /**
      * Prints a comment to the VM code (for human readability)
      * 
      * @param comment
      */
     private void comment(String comment)
     {
         out.println("\t; " + comment);
     }
 
     /*
      * VM ASM methods section end
      */
 
     /**
      * 
      * @param name_rec
      *            {@link SemanticRec} from {@link compiler.parser.Parser#program()} with {@link RecordType#SYM_TBL}
      * @param blockType
      *            {@link SemanticRec} from {@link compiler.parser.Parser#program()} with {@link RecordType#BLOCK}
      */
     public void gen_activation_rec(SemanticRec name_rec, SemanticRec blockType) {
         if (blockType.getRecType() == RecordType.BLOCK)
         {
             String block = blockType.getDatum(0);
             SymbolTable tbl = findSymbolTable(name_rec.getDatum(0));
             int variableCount = tbl.getVariableCount();
             int parameterCount = tbl.getParameterCount();
             String register = null;
             String offset = null;
             switch (block.toLowerCase())
             {
             case "program":
                 comment(name_rec.getDatum(0) + " start"); //; Program1 start
                 add("SP", "#1", "SP"); //reserve space for the old register value
                 add("SP", "#" + variableCount, "SP"); //reserveSpace for the variables in the program
                 register = getRegisterFromNL(name_rec.getDatum(1));
                 offset = "-" + (variableCount + 1) + "(SP)";
                 move(register, offset); //moves the current register into the space reserved for the old register
                 sub("SP", "#" + (variableCount + 1), register); //calculates the new register value as the first position in the AR
                 comment("activation end");
                 break;
             case "procedure":
             case "function":
                 comment(name_rec.getDatum(0) + " start"); //; Program1 start
                 add("SP", "#" + variableCount, "SP"); //reserveSpace for the variables in the program
                 register = getRegisterFromNL(name_rec.getDatum(1));
                 offset = "-" + (variableCount + parameterCount + 2) + "(SP)"; //one slot for return address, one slot for the old register value
                 move(register, offset); //moves the current register into the space reserved for the old register
                 sub("SP", "#" + (variableCount + parameterCount + 2), register); //calculates the new register value as the first position in the AR
                 comment("activation end");
                 break;
             default:
                 Parser.semanticError("Block type: " + block + " is not supported");
                 break;
             }
         }
         else
         {
             Parser.semanticError("Semantic record " + blockType.getRecType()
                     + " is not supported for recordType.BLOCK parameter");
         }
     }
 
     /**
      * 
      * @param name_rec
      *            {@link SemanticRec} from {@link compiler.parser.Parser#program()} with {@link RecordType#SYM_TBL}
      */
     public void gen_prog_deactivation_rec(SemanticRec name_rec)
     {
         SymbolTable tbl = findSymbolTable(name_rec.getDatum(0));
         int variableCount = tbl.getVariableCount();
         String register = getRegisterFromNL(name_rec.getDatum(1));
         String offset = "-" + (variableCount + 1) + "(SP)";
         comment("deactivation start");
         move(offset, register); //moves the old register value back into the register
         sub("SP", "#" + (variableCount + 1), "SP"); //removes the variables
         comment(name_rec.getDatum(0) + " end"); //; Program1 end
     }
 
     /**
      * 
      * @param name_rec
      *            {@link SemanticRec} from {@link compiler.parser.Parser#program()} with {@link RecordType#SYM_TBL}
      */
     public void gen_proc_deactivation_rec(SemanticRec name_rec)
     {
         SymbolTable tbl = findSymbolTable(name_rec.getDatum(0));
         int variableCount = tbl.getVariableCount();
         int parameterCount = tbl.getParameterCount();
         String register = getRegisterFromNL(name_rec.getDatum(1));
         String offset = "-" + (variableCount + parameterCount + 2) + "(SP)"; //one slot for return address, one slot for the old register value
         comment("deactivation start");
         move(offset, register); //moves the old register value back into the register
         sub("SP", "#" + (variableCount), "SP"); //removes the variables
         ret();
         comment(name_rec.getDatum(0) + " end"); //; Program1 end
     }
 
     /**
      * 
      * @param name_rec
      *            {@link SemanticRec} from {@link compiler.parser.Parser#program()} with {@link RecordType#SYM_TBL}
      */
     public void gen_func_deactivation_rec(SemanticRec name_rec)
     {
         gen_proc_deactivation_rec(name_rec);
     }
 
     /**
      * 
      */
     public void gen_halt()
     {
         halt();
     }
 
     /**
      * 
      * @param factor
      *            {@link SemanticRec} from {@link compiler.parser.Parser#factor()} with {@link RecordType#IDENTIFIER}
      */
     public SemanticRec gen_push_id(SemanticRec factor)
     {
         Classification factorClass = Classification.valueOf(factor.getDatum(0));
         DataRow data = (DataRow) getIdRowFromSR(factor);  //variableIdentifier is either parameter or variable
         SymbolTable tbl = findSymbolTable(data);
         String offset = generateOffset(tbl, data);
         if(factorClass == Classification.VARIABLE)
         {
             comment("push class: " + data.getClassification() + " lexeme: " + data.getLexeme() + " type: " + data.getType()
                     + " offset: " + offset);
             push(offset);
         }
         else if(factorClass == Classification.PARAMETER)
         {
             Mode paramMode = data.getMode();
 
             if(paramMode == Mode.VALUE)
             {
                 comment("push parameter class: " + data.getClassification() + " lexeme: " + data.getLexeme() + " type: " + data.getType()
                         + " offset: " + offset);
                 push(offset);
             }
             else if(paramMode == Mode.VARIABLE)
             {
                 comment("push parameter class: " + data.getClassification() + " lexeme: " + data.getLexeme() + " type: " + data.getType()
                         + " offset: " + offset);
                 push("@" + offset); //dereference the content and push that
             }
         }
         return new SemanticRec(RecordType.LITERAL, data.getType().toString());
     }
 
     /**
      * This verifies the 2x2 matrix of formal parameters to actual parameters that is at
      * https://docs.google.com/document/d/1xooON5JKfxBUgD0lQgwPTXFSnBbg1Tnf2gUfIEjsjRA/edit#bookmark=id.7uphmq724hav It will generate the
      * push instruction based on that table
      * 
      * @param factor
      *            {@link SemanticRec} from {@link compiler.parser.Parser#factor()} with {@link RecordType#IDENTIFIER}
      * @param formalParamRec
      *            {@link SemanticRec} from {@link compiler.parser.Parser#factor()} with {@link RecordType#FORMAL_PARAM} the factor will be
      *            used as an actualParameter with the given formalParameter definition
      */
     public SemanticRec gen_push_id(SemanticRec factor, SemanticRec formalParamRec)
     {
         SemanticRec returnVal = null;
         DataRow data = (DataRow) getIdRowFromSR(factor); //variableIdentifier is either parameter or variable
         SymbolTable tbl = findSymbolTable(data);
         String offset = null;
 
         Type formalParamType = Type.valueOf(formalParamRec.getDatum(0));
         Mode formalParamMode = Mode.valueOf(formalParamRec.getDatum(1));
         Mode actualParamMode = null;
         Type actualParamType = null;
         boolean variableClass = false;
         if (factor.getDatum(0).equalsIgnoreCase(Classification.VARIABLE.toString()))
         {
             DataRow row = (DataRow) findSymbol(factor.getDatum(1), Classification.VARIABLE);
             actualParamMode = Mode.VARIABLE;
             actualParamType = row.getType();
             variableClass = true;
         }
         else if (factor.getDatum(0).equalsIgnoreCase(Classification.PARAMETER.toString()))
         {
             DataRow row = (DataRow) findSymbol(factor.getDatum(1), Classification.PARAMETER);
             actualParamMode = row.getMode();
             actualParamType = row.getType();
             variableClass = false;
         }
         if (actualParamMode != null && actualParamType != null)
         {
             switch (formalParamMode)
             {
             case VALUE:
                 if (actualParamMode == Mode.VALUE)
                 {
                     offset = generateOffset(tbl, data); //Value, Value you push the value onto the stack
                     returnVal = new SemanticRec(RecordType.LITERAL, data.getType().toString());
                     comment("push class: " + data.getClassification() + " lexeme: " + data.getLexeme() + " type: "
                             + data.getType()
                             + " offset: " + offset);
                     push(offset);
                 }
                 else if (actualParamMode == Mode.VARIABLE)
                 {
                     if (variableClass)
                     {
                         offset = generateOffset(tbl, data); //Value, Variable push the contents of the offset
                         returnVal = new SemanticRec(RecordType.LITERAL, data.getType().toString());
                         comment("push class: " + data.getClassification() + " lexeme: " + data.getLexeme() + " type: "
                                 + data.getType()
                                 + " offset: " + offset);
                         push(offset);
                     }
                     else
                     {
                         offset = "@" + generateOffset(tbl, data); //Value, parameter var mode type the contents are an address you must deference it
                         returnVal = new SemanticRec(RecordType.LITERAL, data.getType().toString());
                         comment("push class: " + data.getClassification() + " lexeme: " + data.getLexeme() + " type: "
                                 + data.getType()
                                 + " offset: " + offset);
                         push(offset);
                     }
                 }
                 break;
             case VARIABLE:
                 if (actualParamMode == Mode.VALUE)
                 {
                     Parser.semanticError("Cannot send Mode Value actual parameter " + factor.getDatum(1)
                             + " into Mode Variable formal parameter procedure/function");
                 }
                 else if (actualParamMode == Mode.VARIABLE)
                 {
                     if (formalParamType == actualParamType) //Types have to match exactly
                     {
                         if (variableClass)
                         {
                             String register = getRegisterFromNL("" + tbl.getNestingLevel());
                             offset = "" + data.getMemOffset();
                             returnVal = factor; //It is the address to
                             comment("push address class: " + data.getClassification() + " lexeme: " + data.getLexeme()
                                     + " type: " + data.getType()
                                     + " offset: " + offset);
                             push(register);
                             push("#" + offset);
                             addStackI(); //Variable, Variable you push the address of the variable onto the stack
                         }
                         else //variable parameter the value is an address just push the content across
                         {
                             returnVal = factor; //It is the address to
                             comment("push address class: " + data.getClassification() + " lexeme: " + data.getLexeme()
                                     + " type: " + data.getType()
                                     + " offset: " + offset);
                             offset = generateOffset(tbl, data); //Variable, Variable you push the value onto the stack
                             push(offset);
                         }
                     }
                     else
                     {
                         Parser.semanticError("Mode Variable actual parameter type " + actualParamType
                                 + " must match Mode Variable formal parameter type " + formalParamType);
                     }
                 }
                 break;
             }
 
         }
         else
         {
             Parser.semanticError("Actual parameter does not have a valid mode");
         }
 
         return returnVal;
     }
 
     /**
      * 
      * @param lit
      *            {@link SemanticRec} from {@link compiler.parser.Parser#factor()} with {@link RecordType#LITERAL}
      */
     public void gen_push_lit(SemanticRec lit, String lexeme)
     {
         String type = lit.getDatum(0);
         Type tt = Type.valueOf(type);
         comment("push lexeme: " + lexeme + " type: " + type);
         switch (tt)
         {
         case STRING:
             push("#\"" + lexeme.replaceAll("\\\\(?![nrtv])", "\\\\\\\\") + "\"");
             break;
         case BOOLEAN:
             if (lexeme.equalsIgnoreCase("true"))
             {
                 push("#1");
             }
             else
             {
                 push("#0");
             }
             break;
         case INTEGER:
             push("#" + lexeme);
             break;
         case FLOAT:
             if (lexeme.toLowerCase().contains("e") && !lexeme.contains(".")) //Workaround to convert from micropascal float to C scanf float
             {
                 /*
                  * If the float has an "e" in it but does not have a "." add a ".0" before the "e"
                  * In the current micropascal definition if you have an "e" and a "." it must have a digit after the "." which would be legal in C also
                  */
                 int split = lexeme.toLowerCase().indexOf("e");
                 String start = lexeme.substring(0, split);
                 String end = lexeme.substring(split);
                 lexeme = start + ".0" + end;
             }
             push("#" + lexeme);
             break;
         default:
             Parser.semanticError(lexeme + " of type " + tt.toString() + " cannot be pushed onto the stack");
 
         }
     }
 
     /**
      * 
      * @param factor
      *            {@link SemanticRec} from {@link compiler.parser.Parser#factor()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      */
     public void gen_not_bool(SemanticRec factor)
     {
         Type factorType = getTypeFromSR(factor);
         switch (factorType)
         {
         case BOOLEAN:
             not();
             break;
         default:
             Parser.semanticError("Not operator expected BOOLEAN found " + factorType.toString());
         }
     }
 
     /**
      * 
      * @param opSign
      *            {@link SemanticRec} from {@link compiler.parser.Parser#optionalSign()} with {@link RecordType#OPT_SIGN}
      * @param term
      *            {@link SemanticRec} from {@link compiler.parser.Parser#term()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      */
     public void gen_opt_sim_negate(SemanticRec opSign, SemanticRec term)
     {
         if (opSign != null && term != null)
         {
             Type termType = getTypeFromSR(term);
 
             String op = opSign.getDatum(0);
             switch (termType)
             {
             case INTEGER:
                 switch (op)
                 {
                 case "MP_MINUS":
                     negateStackI();
                     break;
                 case "MP_PLUS":
                     break;
                 default:
                     Parser.semanticError(opSign + " is not a negation operator for type " + termType);
                 }
                 break;
             case FLOAT:
                 switch (op)
                 {
                 case "MP_MINUS":
                     negateStackF();
                     break;
                 case "MP_PLUS":
                     break;
                 default:
                     Parser.semanticError(opSign + " is not a negation operator for type " + termType);
                 }
                 break;
             default:
                 Parser.semanticError(termType + "does not have a negation operation");
             }
         }
     }
 
     /**
      * 
      * @param left
      *            {@link SemanticRec} from {@link compiler.parser.Parser#factor()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @param mulOp
      *            {@link SemanticRec} from {@link compiler.parser.Parser#multiplyingOperator()} with {@link RecordType#REL_OP}
      * @param right
      *            {@link SemanticRec} from {@link compiler.parser.Parser#factor()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @return {@link RecordType#LITERAL} record from the operation
      */
     public SemanticRec gen_mulOp(SemanticRec left, SemanticRec mulOp, SemanticRec right)
     {
         SemanticRec[] results;
         Type resultType;
         String op = mulOp.getDatum(0);
         if (op.equalsIgnoreCase("MP_DIV_INT"))
         {
             checkTypesInt(left, right); //both types must be integers
             divStackI();
             resultType = Type.INTEGER;
         }
         else if (op.equalsIgnoreCase("MP_DIV"))
         {
             results = gen_cast_division(left, right); //always casts to float if not float
             resultType = getTypeFromSR(results[0]); //the types are equal at this point
             divStackF();
         }
         else
         {
             results = gen_cast(left, right); //generates casting operations if needed
             resultType = getTypeFromSR(results[0]); //the types are equal at this point
             switch (resultType)
             {
 
             case INTEGER:
                 switch (op)
                 {
                 case "MP_MOD":
                     modStackI();
                     break;
                 case "MP_TIMES":
                     mulStackI();
                     break;
                 default:
                     Parser.semanticError(op + " is not a multiplication operator for type " + resultType);
                 }
                 break;
             case FLOAT:
                 switch (op)
                 {
                 case "MP_MOD":
                     modStackF();
                     break;
                 case "MP_TIMES":
                     mulStackF();
                     break;
                 default:
                     Parser.semanticError(op + " is not a multiplication operator for type " + resultType);
                 }
                 break;
             case BOOLEAN:
                 switch (op)
                 {
                 case "MP_AND":
                     and();
                     break;
                 default:
                     Parser.semanticError(op + " is not a multiplication operator for type " + resultType);
                 }
                 break;
             default:
                 Parser.semanticError(resultType + "does not have a multiplication operation");
             }
         }
         return new SemanticRec(RecordType.LITERAL, resultType.toString());
     }
 
     /**
      * 
      * @param left
      *            {@link SemanticRec} from {@link compiler.parser.Parser#term()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @param mulOp
      *            {@link SemanticRec} from {@link compiler.parser.Parser#addingOperatorOperator()} with {@link RecordType#REL_OP}
      * @param right
      *            {@link SemanticRec} from {@link compiler.parser.Parser#term()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @return {@link RecordType#LITERAL} record from the operation
      */
     public SemanticRec gen_addOp(SemanticRec left, SemanticRec addOp, SemanticRec right)
     {
 
         SemanticRec[] results = gen_cast(left, right); //generates casting operations if needed
         Type resultType = getTypeFromSR(results[0]); //both sides should have equal types
         String op = addOp.getDatum(0);
         switch (resultType)
         {
         case INTEGER:
             switch (op)
             {
             case "MP_MINUS":
                 subStackI();
                 break;
             case "MP_PLUS":
                 addStackI();
                 break;
             default:
                 Parser.semanticError(op + " is not a adding operator for type " + resultType);
             }
             break;
         case FLOAT:
             switch (op)
             {
             case "MP_MINUS":
                 subStackF();
                 break;
             case "MP_PLUS":
                 addStackF();
                 break;
             default:
                 Parser.semanticError(op + " is not a adding operator for type " + resultType);
             }
             break;
         case BOOLEAN:
             switch (op)
             {
             case "MP_OR":
                 or();
                 break;
             default:
                 Parser.semanticError(op + " is not a adding operator for type " + resultType);
             }
             break;
         default:
             Parser.semanticError(resultType + "does not have an adding operation");
         }
         return new SemanticRec(RecordType.LITERAL, resultType.toString());
     }
 
     /**
      * 
      * @param id
      *            {@link SemanticRec} from {@link compiler.parser.Parser#variableIdentifier()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @param exp
      *            {@link SemanticRec} from {@link compiler.parser.Parser#expression()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @param symbolTable
      *            Used for functions only {@link SemanticRec} from {@link compiler.parser.Parser#expression()} with
      *            {@link RecordType#SYM_TBL}
      */
     public void gen_assign(SemanticRec id, SemanticRec exp, SemanticRec symbolTable) {
 
         SemanticRec result = gen_assign_cast(id, exp);
 
         Row leftRow = getIdRowFromSR(id);
         if (leftRow.getClassification() == Classification.VARIABLE)
         {
             SymbolTable leftTable = findSymbolTable(leftRow);
             String leftOffset = generateOffset(leftTable, (DataRow) leftRow);
             //rightside is ontop of the stack pop into destination
             pop(leftOffset); //pop into left
         }
         else if (leftRow.getClassification() == Classification.FUNCTION)
         {
             FunctionRow funcRow = (FunctionRow) leftRow;
             String nestingLvl = symbolTable.getDatum(1);
             String register = getRegisterFromNL(nestingLvl);
             String offset = "-1(" + register + ")"; //one above the current old register value is the slot for the return value
             pop(offset);
             funcRow.setHasReturnValue(true);
         }
         else if (leftRow.getClassification() == Classification.PARAMETER)
         {
             SymbolTable leftTable = findSymbolTable(leftRow);
             DataRow row = (DataRow) leftRow;
             Mode paramMode = row.getMode();
             String offset = null;
             if (paramMode == Mode.VALUE)
             {
                 offset = generateOffset(leftTable, row);
                 pop(offset); //if it is a value parameter treat it like a normal Variable for that scope
             }
             else if (paramMode == Mode.VARIABLE)
             {
                 offset = "@" + generateOffset(leftTable, row);
                 pop(offset); //if it is a variable parameter dereference the local parameter to store at the parent address contained in it
             }
         }
     }
 
     /**
      * Only allows you to assign to integer identifiers per http://www.freepascal.org/docs-html/ref/refsu52.html#x145-15500013.2.4
      * "The control variable must be an ordinal type, no other types can be used as counters in a loop."
      * 
      * @param id
      *            {@link SemanticRec} from {@link compiler.parser.Parser#variableIdentifier()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @param exp
      *            {@link SemanticRec} from {@link compiler.parser.Parser#expression()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      */
     public void gen_assign_for(SemanticRec id, SemanticRec exp)
     {
         Type idType = getTypeFromSR(id);
         if (idType == Type.INTEGER)
         {
             gen_assign(id, exp, null);
         }
         else
         {
             Parser.semanticError("The for loop's control variable must be type Integer");
         }
     }
 
     /**
      * 
      * @param left
      *            {@link SemanticRec} from {@link compiler.parser.Parser#simpleExpression()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      * @param op
      *            {@link SemanticRec} from {@link compiler.parser.Parser#optionalRelationalPart(SemanticRec)} with {@link RecordType#REL_OP}
      * @param right
      *            {@link SemanticRec} from {@link compiler.parser.Parser#simpleExpression()} with {@link RecordType#LITERAL} or
      *            {@link RecordType#IDENTIFIER}
      */
     public void gen_opt_rel_part(SemanticRec left, SemanticRec op, SemanticRec right)
     {
         if (left != null && right != null && op != null)
         {
 
             SemanticRec[] results = gen_cast(left, right); //generates casting operations if needed
             Type resultType = getTypeFromSR(results[0]); //They will both be equal at this point
             String relOp = op.getDatum(0); //MP_NEQUAL, MP_GEQUAL, MP_LEQUAL, MP_GTHAN, MP_LTHAN, MP_EQUAL
             switch (resultType)
             {
             case INTEGER:
                 switch (relOp)
                 {
                 case "MP_NEQUAL":
                     notEqualI();
                     break;
                 case "MP_GEQUAL":
                     greaterEqualI();
                     break;
                 case "MP_LEQUAL":
                     lessEqualI();
                     break;
                 case "MP_GTHAN":
                     greaterThanI();
                     break;
                 case "MP_LTHAN":
                     lessThanI();
                     break;
                 case "MP_EQUAL":
                     equalI();
                     break;
                 default:
                     Parser.semanticError(relOp + " is not a relational operator for type " + resultType);
                 }
                 break;
             case FLOAT:
                 switch (relOp)
                 {
                 case "MP_NEQUAL":
                     notEqualF();
                     break;
                 case "MP_GEQUAL":
                     greaterEqualF();
                     break;
                 case "MP_LEQUAL":
                     lessEqualF();
                     break;
                 case "MP_GTHAN":
                     greaterThanF();
                     break;
                 case "MP_LTHAN":
                     lessThanF();
                     break;
                 case "MP_EQUAL":
                     equalF();
                     break;
                 default:
                     Parser.semanticError(relOp + " is not a relational operator for type " + resultType);
                 }
             default:
                 Parser.semanticError(resultType + " does not have relational operators");
             }
         }
     }
 
     /**
      * Casts Ids or Literals to the less restrictive type to preserve precision Int, Float the Int is cast to a Float Based on
      * http://www.freepascal.org/docs-html/ref/refsu39.html#x129-13900012.8.1
      * 
      * @param left
      *            {@link RecordType#LITERAL} or {@link RecordType#IDENTIFIER}
      * @param right
      *            {@link RecordType#LITERAL} or {@link RecordType#IDENTIFIER}
      * @return resulting SemanticRec idx 0 is left idx 1 is right
      */
     public SemanticRec[] gen_cast(SemanticRec left, SemanticRec right)
     {
         Type leftType = getTypeFromSR(left);
         Type rightType = getTypeFromSR(right);
 
         SemanticRec[] arrRec = new SemanticRec[2];
         if (leftType == rightType)
         {
             //no cast needed
             arrRec[0] = left;
             arrRec[1] = right;
         }
         else if ((leftType == Type.INTEGER) && rightType == Type.FLOAT)
         {
             //cast left to float
             /*
              * The following cast works because of the way the stack in the VM is initialized
              * when the stack pointer is moved back the value that use to be on top is not lost just not currently in "scope"
              * once the cast happens on the new "top" the pointer is moved back and it was like nothing happened to the value
              */
             comment("start cast left to float");
             sub("SP", "#1", "SP"); //move to the left var on the stack
             castStackToF();
             add("SP", "#1", "SP"); //move back to original place
             comment("end cast left to float");
             arrRec[0] = new SemanticRec(RecordType.LITERAL, Type.FLOAT.toString());
             arrRec[1] = right;
         }
         else if (leftType == Type.FLOAT && (rightType == Type.INTEGER))
         {
             //cast right to float
             castStackToF(); //top value
             arrRec[0] = left;
             arrRec[1] = new SemanticRec(RecordType.LITERAL, Type.FLOAT.toString());
         }
         else
         {
             //invalid cast
             Parser.semanticError("Invalid cast from " + rightType + " to " + leftType);
             return null;
         }
         return arrRec;
     }
 
     /**
      * Casts Ids or Literals to floats as the MP_DIV operator always returns a float result per
      * http://www.freepascal.org/docs-html/ref/refsu39.html#x129-13900012.8.1
      * 
      * @param left
      *            {@link RecordType#LITERAL} or {@link RecordType#IDENTIFIER}
      * @param right
      *            {@link RecordType#LITERAL} or {@link RecordType#IDENTIFIER}
      * @return resulting SemanticRec idx 0 is left idx 1 is right will be float for both
      */
     public SemanticRec[] gen_cast_division(SemanticRec left, SemanticRec right)
     {
         Type leftType = getTypeFromSR(left);
         Type rightType = getTypeFromSR(right);
 
         SemanticRec[] arrRec = new SemanticRec[2];
         boolean properCastL = false;
         boolean properCastR = false;
 
         if (leftType == Type.FLOAT)
         {
             //no cast needed
             arrRec[0] = left;
             properCastL = true;
         }
         else if (leftType == Type.INTEGER)
         {
             //cast left to float
             /*
              * The following cast works because of the way the stack in the VM is initialized
              * when the stack pointer is moved back the value that use to be on top is not lost just not currently in "scope"
              * once the cast happens on the new "top" the pointer is moved back and it was like nothing happened to the value
              */
             comment("start cast left to float");
             sub("SP", "#1", "SP"); //move to the left var on the stack
             castStackToF();
             add("SP", "#1", "SP"); //move back to original place
             comment("end cast left to float");
             arrRec[0] = new SemanticRec(RecordType.LITERAL, Type.FLOAT.toString());
             properCastL = true;
         }
         else
         {
             properCastL = false; //left not an Int or Float
         }
 
         if (rightType == Type.FLOAT)
         {
             //no cast needed
             arrRec[1] = right;
             properCastR = true;
         }
         else if (rightType == Type.INTEGER)
         {
             //cast right to float
             castStackToF(); //top value
             arrRec[1] = new SemanticRec(RecordType.LITERAL, Type.FLOAT.toString());
             properCastR = true;
         }
         else
         {
             properCastR = false; //Right type was not an integer or float
         }
 
         if (!properCastL || !properCastR)
         {
             //invalid cast
             Parser.semanticError("Invalid cast from " + rightType + " to " + leftType);
             return null;
         }
         return arrRec;
     }
 
     /**
      * Always casts to the left's Type
      * 
      * @param left
      *            {@link RecordType#IDENTIFIER}
      * @param right
      *            {@link RecordType#LITERAL} or {@link RecordType#IDENTIFIER}
      * @return expression's resulting SemanticRec if it is a {@link RecordType#LITERAL}
      */
     public SemanticRec gen_assign_cast(SemanticRec left, SemanticRec right)
     {
         Type leftType = getTypeFromSR(left);
         Type rightType = getTypeFromSR(right);
 
         SemanticRec returnRec = null;
         if (leftType == rightType)
         {
             //no cast needed
             returnRec = right; //the expressions RecordType is unchanged
         }
         else if ((leftType == Type.INTEGER) && rightType == Type.FLOAT)
         {
             //cast to integer
             castStackToI();
             returnRec = new SemanticRec(RecordType.LITERAL, Type.INTEGER.toString()); //now an integer on the stack
         }
         else if (leftType == Type.FLOAT && (rightType == Type.INTEGER))
         {
             //cast to float
             castStackToF();
             returnRec = new SemanticRec(RecordType.LITERAL, Type.FLOAT.toString()); //now a float on the stack
         }
         else
         {
             //invalid cast
             Parser.semanticError("Invalid cast from " + rightType + " to " + leftType);
         }
 
         return returnRec;
     }
 
     public void gen_param_cast(SemanticRec expression, SemanticRec formalParam)
     {
 
         Type actualParamType = getTypeFromSR(expression);
         Type formalParamType = Type.valueOf(formalParam.getDatum(0));
         Mode formalParamMode = Mode.valueOf(formalParam.getDatum(1));
         if (expression.getRecType() == RecordType.LITERAL)
         {
             switch (formalParamMode)
             {
             case VALUE:
                 if (actualParamType != formalParamType)
                 {
                     if ((formalParamType == Type.INTEGER) && actualParamType == Type.FLOAT)
                     {
                         //cast to integer
                         castStackToI();
                     }
                     else if (formalParamType == Type.FLOAT && (actualParamType == Type.INTEGER))
                     {
                         //cast to float
                         castStackToF();
                     }
                     else
                     {
                         //invalid cast
                         Parser.semanticError("Invalid cast formal param from " + actualParamType + " to "
                                 + formalParamType);
                     }
                 }
                 break;
             case VARIABLE:
                 Parser.semanticError("Cannot cast value actual parameter into variable formal parameter");
                 break;
             }
         }
         else if (expression.getRecType() == RecordType.IDENTIFIER)
         {
 
             switch (formalParamMode)
             {
             case VALUE:
                 Parser.semanticError("Programming error: case should be handled in gen_push_id(SemanticRec factor, SemanticRec formalParamRec)");
                 //any identifiers that woulld be sent into value formal parameters would be converted to values at this point
                 break;
             case VARIABLE:
                 if (actualParamType != formalParamType)
                 {
                     Parser.semanticError("Actual parameter variable type must match formal parameter variable type");
                 }
                 break;
             }
         }
     }
 
     /**
      * Checks that both left and right are ints "div" division is only on integer types
      * http://www.freepascal.org/docs-html/ref/refsu39.html#x129-13900012.8.1
      * 
      * @param left
      *            {@link RecordType#LITERAL} or {@link RecordType#IDENTIFIER}
      * @param right
      *            {@link RecordType#LITERAL} or {@link RecordType#IDENTIFIER}
      * 
      */
     private void checkTypesInt(SemanticRec left, SemanticRec right)
     {
         Type leftType = getTypeFromSR(left);
         Type rightType = getTypeFromSR(right);
 
         if (leftType != Type.INTEGER || rightType != Type.INTEGER)
         {
             Parser.semanticError("div operator only works on integer operands left operand is type: " + leftType
                     + " right operand is type: " + rightType);
         }
     }
 
     public void gen_writeStmt(SemanticRec writeStmt, SemanticRec exp)
     {
         String writeType = writeStmt.getDatum(0);
         if (writeType.equalsIgnoreCase(TokenType.MP_WRITE.toString()))
         {
             writeStack();
         }
         else
         {
             writelnStack();
         }
     }
 
     public void gen_readStmt(SemanticRec readStmt)
     {
         DataRow row = (DataRow) getIdRowFromSR(readStmt);
         Type type = row.getType();
         SymbolTable table = findSymbolTable(row);
         String offset = generateOffset(table, row);
 
         switch (type)
         {
         case INTEGER:
             readI(offset);
             break;
         case FLOAT:
             readF(offset);
             break;
         case STRING:
             readS(offset);
             break;
         default:
             Parser.semanticError("Cannot read from console into variable of type: " + type.toString());
         }
 
     }
 
     /**
      * Generates a label
      * 
      * @return SemanticRec with the label that was output
      */
     public SemanticRec gen_label() {
         String label = LabelGenerator.getNextLabel();
         SemanticRec labelRec = new SemanticRec(RecordType.LABEL, label);
         label(label);
         return labelRec;
     }
 
     /**
      * Generates a specific label
      * 
      * @param labelRec
      *            the SemanticRec containing the label to create
      * @return the SemanticRec containing the label that was created
      */
     public SemanticRec gen_specified_label(SemanticRec labelRec) {
         switch (labelRec.getRecType()) {
         case LABEL:
             label(labelRec.getDatum(0));
             break;
         default:
             Parser.semanticError("Cannot generate label with information of type: " + labelRec.getRecType());
             break;
         }
         return labelRec;
     }
 
     /**
      * Generates a branch if false instruction to jump to a newly generated label
      * 
      * @return SemanticRec with the label that will be jumped to (which may or may not exist yet)
      */
     public SemanticRec gen_branch_false() {
         String label = LabelGenerator.getNextLabel();
         SemanticRec labelRec = new SemanticRec(RecordType.LABEL, label);
         branchFalse(label);
         return labelRec;
     }
 
     /**
      * Generates a branch if true instruction to jump to a newly generated label
      * 
      * @return SemanticRec with the label that will be jumped to (which may or may not exist yet)
      */
     public SemanticRec gen_branch_true() {
         String label = LabelGenerator.getNextLabel();
         SemanticRec labelRec = new SemanticRec(RecordType.LABEL, label);
         branchTrue(label);
         return labelRec;
     }
 
     /**
      * Generates an unconditional branch to a newly generated label
      * 
      * @return SemanticRec with the label that will be jumped to (which may or may not exist yet)
      */
     public SemanticRec gen_branch_unconditional() {
         String label = LabelGenerator.getNextLabel();
         SemanticRec labelRec = new SemanticRec(RecordType.LABEL, label);
         branchUnconditional(label);
         return labelRec;
     }
 
     /**
      * Generates a branch if false instruction to jump to a specified label
      * 
      * @param labelRec
      *            the SemanticRec containing the label to jump to
      * @return the SemanticRec containing the label
      */
     public SemanticRec gen_branch_false_to(SemanticRec labelRec) {
         switch (labelRec.getRecType()) {
         case LABEL:
             branchFalse(labelRec.getDatum(0));
             break;
         default:
             Parser.semanticError("Cannot generate label with information of type: " + labelRec.getRecType());
             break;
         }
         return labelRec;
     }
 
     /**
      * Generates a branch if true instruction to jump to a specified label
      * 
      * @param labelRec
      *            the SemanticRec containing the label to jump to
      * @return the SemanticRec containing the label
      */
     public SemanticRec gen_branch_true_to(SemanticRec labelRec) {
         switch (labelRec.getRecType()) {
         case LABEL:
             branchTrue(labelRec.getDatum(0));
             break;
         default:
             Parser.semanticError("Cannot generate label with information of type: " + labelRec.getRecType());
             break;
         }
         return labelRec;
     }
 
     /**
      * Generates an unconditional branch instruction to jump to a specified label
      * 
      * @param labelRec
      *            the SemanticRec containing the label to jump to
      * @return the SemanticRec containing the label
      */
     public SemanticRec gen_branch_unconditional_to(SemanticRec labelRec) {
         switch (labelRec.getRecType()) {
         case LABEL:
             branchUnconditional(labelRec.getDatum(0));
             break;
         default:
             Parser.semanticError("Cannot generate label with information of type: " + labelRec.getRecType());
             break;
         }
         return labelRec;
     }
 
     /**
      * Generates a push instruction to push a variable on to the stack
      * 
      * @param variableRec
      *            a SemanticRec containing the variable to push
      */
     public void gen_push_variable(SemanticRec variableRec) {
         switch (variableRec.getRecType()) {
         case IDENTIFIER:
             if (variableRec.getDatum(0).equalsIgnoreCase(Classification.VARIABLE.toString())) {
                 DataRow var = (DataRow) findSymbol(variableRec.getDatum(1));
                 String memLoc = generateOffset(findSymbolTable(var), var);
                 push(memLoc);
             } else {
                 Parser.semanticError("Cannot push non-variable on to the stack");
             }
             break;
         default:
             Parser.semanticError("Cannot push non-identifier");
             break;
         }
     }
 
     /**
      * Generates instructions that compare the ControlVariable and FinalValue <br>
      * 
      * Pre-Condition: ControlVariable was pushed on the stack, followed by FinalValue
      * 
      * @param forDirection
      */
     public void gen_for_comparison(SemanticRec forDirection) {
         switch (forDirection.getRecType()) {
         case FOR_DIRECTION:
             if (forDirection.getDatum(0).equalsIgnoreCase(TokenType.MP_TO.toString())) {
                 lessEqualI();
             } else if (forDirection.getDatum(0).equalsIgnoreCase(TokenType.MP_DOWNTO.toString())) {
                 greaterEqualI();
             } else {
                 Parser.semanticError("Invalid FOR_DIRECTION: " + forDirection.getDatum(0));
             }
             break;
         default:
             Parser.semanticError("Cannot use non FOR_DIRECTION record type: " + forDirection.getRecType());
             break;
         }
     }
 
     /**
      * Generates code to increment or decrement a for loop ControlVariable
      * 
      * @param controllerRec
      *            SemanticRec containing the control variable information
      * @param forDirection
      *            SemanticRec containing the for loop direction
      */
     public void gen_for_controller(SemanticRec controllerRec, SemanticRec forDirection) {
         boolean increment = false;
         //determine whether or not to increment or decrement
         switch (forDirection.getRecType()) {
         case FOR_DIRECTION:
             if (forDirection.getDatum(0).equalsIgnoreCase(TokenType.MP_TO.toString())) {
                 increment = true;
             } else if (forDirection.getDatum(0).equalsIgnoreCase(TokenType.MP_DOWNTO.toString())) {
                 increment = false;
             } else {
                 Parser.semanticError("Invalid FOR_DIRECTION: " + forDirection.getDatum(0));
             }
             break;
         default:
             Parser.semanticError("Cannot use non FOR_DIRECTION record type: " + forDirection.getRecType());
             break;
         }
 
         switch (controllerRec.getRecType()) {
         case IDENTIFIER:
             if (controllerRec.getDatum(0).equalsIgnoreCase(Classification.VARIABLE.toString())) {
                 DataRow var = (DataRow) findSymbol(controllerRec.getDatum(1));
                 String memLoc = generateOffset(findSymbolTable(var), var);
                 if (increment) {
                     push(memLoc);
                     push("#1");
                     addStackI();
                     pop(memLoc);
                 } else {
                     push(memLoc);
                     push("#1");
                     subStackI();
                     pop(memLoc);
                 }
             } else {
                 Parser.semanticError("Cannot use non-variable as control variable");
             }
             break;
         default:
             Parser.semanticError("Cannot use non-identifier for control variable");
             break;
         }
     }
 
     public void gen_proc_call(SemanticRec procRec) {
         ProcedureRow row = (ProcedureRow) getIdRowFromSR(procRec);
         String lbl = row.getBranchLabel();
         call(lbl);
         int paramNum = row.getAttributes().size();
         sub("SP", "#" + (paramNum + 1), "SP"); //removes parameters and returnValue storage
     }
 
     public void gen_func_call(SemanticRec funcRec) {
         FunctionRow row = (FunctionRow) getIdRowFromSR(funcRec);
         String lbl = row.getBranchLabel();
         call(lbl);
         int paramNum = row.getAttributes().size();
         sub("SP", "#" + (paramNum + 1), "SP"); //removes parameters and returnValue storage
     }
 
     public void gen_func_return_slot()
     {
         add("SP", "#1", "SP");
     }
 
     public void gen_dis_reg_slot()
     {
         add("SP", "#1", "SP");
     }
 
     /**
      * Generates a comment in the VM file
      * 
      * @param comment
      *            the comment
      */
     public void gen_comment(String comment) {
         comment(comment);
     }
 
     public Row findSymbol(String lexeme) {
         for (int i = symboltables.size() - 1; i >= 0; i--) {
             SymbolTable st = symboltables.get(i);
             Row s = st.findSymbol(lexeme);
             if (s != null) {
                 return s;
             }
         }
         return null;
     }
 
     public Row findSymbol(String lexeme, Classification c) {
         for (int i = symboltables.size() - 1; i >= 0; i--) {
             SymbolTable st = symboltables.get(i);
             Row s = st.findSymbol(lexeme, c);
             if (s != null) {
                 return s;
             }
         }
         return null;
     }
 
     public SymbolTable findSymbolTable(Row symbol)
     {
         for (int i = symboltables.size() - 1; i >= 0; i--) {
             SymbolTable st = symboltables.get(i);
             boolean s = st.contains(symbol);
             if (s == true) {
                 return st;
             }
         }
         return null;
     }
 
     public SymbolTable findSymbolTable(String name)
     {
         for (int i = symboltables.size() - 1; i >= 0; i--) {
             SymbolTable st = symboltables.get(i);
             boolean s = st.getScopeName().equalsIgnoreCase(name);
             if (s == true) {
                 return st;
             }
         }
         return null;
     }
 
 }
