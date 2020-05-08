 package scanner;
 
 import static scanner.ID.*;
 
 import java.util.*;
 
 import node.AbstractNode;
 
 import scanner.Scanner;
 import scanner.Token;
 
 
 public class Parser {
 
 	//Main
 	
 	public static void main(String argv[]) {
 	    if (argv.length == 0) {
 	      System.out.println("Usage : java Scanner <inputfile>");
 	    }
 	    else {
 	      for (int i = 0; i < argv.length; i++) {
 	        try {
 	          scanner = new Scanner( new java.io.FileReader(argv[i]) );
 	          insymbol();
 	          program();
 	        } catch (java.io.FileNotFoundException e) {
 	        	  System.out.println("File not found : \""+argv[i]+"\"");
 	        } catch (java.io.IOException e) {
 		          System.out.println("IO error scanning file \""+argv[i]+"\"");
 		          System.out.println(e);
 	        } catch (Exception e) {
 		          System.out.println("Unexpected exception:");
 		          e.printStackTrace();
 	        }
 	      }
 	    }
 	}
 	
 	static Token nextSym = null;
 	static String inFile;
 	static Scanner scanner = null;
 	static String spaces = "";
 	
 	//Getter
 	
 	public static int getLine(){return nextSym.line();}
 	public static int getColumn(){return nextSym.column();}
 	
 	//Hilfsroutinen
 	
 	public static void indent() {
 		spaces = spaces + "  ";
 	}
 	public static void unindent() {
 		spaces = spaces.substring(2);
 	}
 	public static void printNextsymbol() {
 		System.out.println(spaces + " -- " + nextSym.id() + " ");
 	}
 	
 	public static void printThisSymbol(Token t) {
 		System.out.println(spaces + " -- " + t.id() + " ");
 	}
 
 	public static void printFunction(String name){
 		System.out.println(spaces + name/* +": " + nextsymbol*/);
 	}
 	
 	public static void error(String str) {
 		System.out.println("==> Error: " + str + " " + place());
 		System.exit(0);
 	}
 	
 	public static String place(){
 		return("l:"+nextSym.line()+", c:"+nextSym.column());
 	}
 	
 	public static void insymbol() {
 		try {
 			while ((nextSym = scanner.yylex()) != null && nextSym.id().getValue() == BLANK.getValue()){}
 			System.out.println(spaces + "insymbol: " + nextSym);
 		} catch (java.io.FileNotFoundException e) {
 			System.out.println("File not found : \"" + inFile + "\"");
 		} catch (java.io.IOException e) {
 			System.out.println("IO error scanning file \"" + inFile + "\"");
 			System.out.println(e);
 		} catch (Exception e) {
 			System.out.println("Unexpected exception:");
 			e.printStackTrace();
 		}
 	}
 	
 	//Abfragehilfe
 	private static boolean isString(){return( nextSym.id() == QUOT);}
 	private static boolean isInteger(){return( nextSym.id() == DIGIT);}
 	private static boolean isIdent(){return( nextSym.id() == ID);}
 	private static boolean isLpar(){return( nextSym.id() == LPAR);}
 	private static boolean isRpar(){return( nextSym.id() == RPAR);}
 	private static boolean isLbrac(){return( nextSym.id() == LBRAC);}
 	private static boolean isRbrac(){return( nextSym.id() == RBRAC);}
 	private static boolean isPlus(){return( nextSym.id() == PLUS);}
 	private static boolean isMinus(){return( nextSym.id() == MINUS);}
 	private static boolean isMul(){return( nextSym.id() == MUL);}
 	private static boolean isDiv(){return( nextSym.id() == DIV);}
 	private static boolean isEq(){return( nextSym.id() == EQ);}
 	private static boolean isNeq(){return( nextSym.id() == NEQ);}
 	private static boolean isLo(){return( nextSym.id() == LO);}
 	private static boolean isHi(){return( nextSym.id() == HI);}
 	private static boolean isLoEq(){return( nextSym.id() == LOEQ);}
 	private static boolean isHiEq(){return( nextSym.id() == HIEQ);}
 	private static boolean isDot(){return( nextSym.id() == DOT);}
 	private static boolean isComma(){return( nextSym.id() == COMMA);}
 	private static boolean isAssign(){return( nextSym.id() == ASSIGN);}
 	private static boolean isSemicolon(){return( nextSym.id() == SEMICOLON);}
 	private static boolean isColon(){return(nextSym.id() == COLON);}
	private static boolean isError(){return(nextSym.id() == ERROR);}
 	
 	private static boolean isRead(){return( nextSym.id() == READ);}
 	private static boolean isIf(){return( nextSym.id() == IF);}
 	private static boolean isThen(){return( nextSym.id() == THEN);}
 	private static boolean isElse(){return( nextSym.id() == ELSE);}
 	private static boolean isElsif(){return( nextSym.id() == ELSIF);}
 	private static boolean isWhile(){return( nextSym.id() == WHILE);}
 	private static boolean isDo(){return( nextSym.id() == DO);}
 	private static boolean isEnd(){return( nextSym.id() == END);}
 	private static boolean isRepeat(){return( nextSym.id() == REPEAT);}
 	private static boolean isUntil(){return( nextSym.id() == UNTIL);}
 	private static boolean isPrint(){return( nextSym.id() == PRINT);}
 	private static boolean isModule(){return( nextSym.id() == MODULE);}
 	private static boolean isBegin(){return( nextSym.id() == BEGIN);}
 	private static boolean isConst(){return( nextSym.id() == CONST);}
 	private static boolean isType(){return( nextSym.id() == TYPE);}
 	private static boolean isVar(){return( nextSym.id() == VAR);}
 	private static boolean isProcedure(){return( nextSym.id() == PROCEDURE);}
 	private static boolean isArray(){return( nextSym.id() == ARRAY);}
 	private static boolean isOf(){return( nextSym.id() == OF);}
 	private static boolean isRecord(){return( nextSym.id() == RECORD);}
 	
 	private static boolean isFactor(){return( isIdent() || isInteger() || isString() || isRead() || isLpar());}
 	private static boolean isExpression(){return( isMinus() || isFactor());}
 	private static boolean isStatement(){return(isIdent() || isIf() || isPrint() || isWhile() || isRepeat());}
 	private static boolean isDeclaration(){return( isConst() || isType() || isVar() || isProcedure());}
 
 	//Parserroutinen
 	
 	static AbstractNode identList(){
 		printFunction("IdentList");
 		indent();
 		if(isIdent()){
 			printNextsymbol();
 			insymbol();
 		}else{
 			error("Ident expected");
 		}
 		while(isComma()){
 			printNextsymbol();
 			insymbol();
 			if(isIdent()){
 				printNextsymbol();
 				insymbol();
 			}else{
 				error("Ident expected");
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode arrayType(){
 		printFunction("ArrayType");
 		indent();
 		if(isArray()){
 			printNextsymbol();
 			insymbol();
 			if(isLbrac()){
 				printNextsymbol();
 				insymbol();
 				if(isInteger() || isIdent()){
 					indexExpression();
 					if(isRbrac()){
 						printNextsymbol();
 						insymbol();
 						if(isOf()){
 							printNextsymbol();
 							insymbol();
 							if(isIdent() || isArray() || isRecord()){
 								type();
 							}else{
 								error("Type expected");
 							}
 						}else{
 							error("'of' expected");
 						}
 					}else{
 						error("']' expected");
 					}
 				}else{
 					error("IndexExpression expected");
 				}
 			}else{
 				error("'[' expected");
 			}
 		}else{
 			error("'Array' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode fieldList(){
 		printFunction("FieldList");
 		indent();
 		if(isIdent()){
 			identList();
 			if(isColon()){
 				printNextsymbol();
 				insymbol();
 				if(isIdent() || isArray() || isRecord()){
 					type();
 				}else{
 					error("Type expected");
 				}
 			}else{
 				error("':' expected");
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode recordType(){
 		printFunction("RecordType");
 		indent();
 		if(isRecord()){
 			printNextsymbol();
 			insymbol();
 			if(isIdent()){
 				fieldList();
 			}
 			while(isSemicolon()){
 				printNextsymbol();
 				insymbol();
 				if(isIdent()){
 					fieldList();
 				}
 			}
 			if(isEnd()){
 				printNextsymbol();
 				insymbol();
 			}else{
 				error("'end' expected");
 			}
 		}else{
 			error("'record' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode type(){
 		printFunction("Type");
 		indent();
 		if(isIdent()){
 			printNextsymbol();
 			insymbol();
 		}else if(isArray()){
 			arrayType();
 		}else if(isRecord()){
 			recordType();
 		}else{
 			error("Ident, ArrayType or RecordType expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode fpSection(){
 		printFunction("FPSection");
 		indent();
 		if(isVar()){
 			printNextsymbol();
 			insymbol();
 		}
 		if(isIdent()){
 			identList();
 			if(isColon()){
 				printNextsymbol();
 				insymbol();
 				if(isIdent() || isArray() || isRecord()){
 					type();
 				}else{
 					error("Type expected");
 				}
 			}else{
 				error("':' expected");
 			}
 		}else{
 			error("IdentList expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode formalParameters(){
 		printFunction("FormalParameters");
 		indent();
 		if(isVar() || isIdent()){
 			fpSection();
 		}else{
 			error("FPSection expected");
 		}
 		while(isSemicolon()){
 			printNextsymbol();
 			insymbol();
 			if(isVar() || isIdent()){
 				fpSection();
 			}else{
 				error("FPSection expected");
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode procedureHeading(){
 		printFunction("ProcedureHeading");
 		indent();
 		if(isProcedure()){
 			printNextsymbol();
 			insymbol();
 			if(isIdent()){
 				printNextsymbol();
 				insymbol();
 				if(isLpar()){
 					printNextsymbol();
 					insymbol();
 					if(isVar() || isIdent()){
 						formalParameters();
 					}
 					if(isRpar()){
 						printNextsymbol();
 						insymbol();
 					}else{
 						error("')' expected");
 					}
 				}else{
 					error("'(' expected");
 				}
 			}else{
 				error("Ident expected");
 			}
 		}else{
 			error("'procedure' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode procedureBody(){
 		printFunction("ProcedureBody");
 		indent();
 		if(isDeclaration()){
 			declarations();
 		}
 		if(isBegin()){
 			printNextsymbol();
 			insymbol();
 			if(isStatement()){
 				statementSequence();
 			}
 			if(isEnd()){
 				printNextsymbol();
 				insymbol();
 			}else{
 				error("'end' expected");
 			}
 		}else{
 			error("'begin' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 //-------------------------------------------------------------------------------------
 	static AbstractNode procedureDeclaration(){
 		printFunction("ProcedureDeclaration");
 		indent();
 		if(isProcedure()){
 			procedureHeading();
 			if(isSemicolon()){
 				printNextsymbol();
 				insymbol();
 				if(isDeclaration() || isBegin()){
 					procedureBody();
 					if(isIdent()){
 						printNextsymbol();
 						insymbol();
 					}else{
 						error("Ident expected");
 					}
 				}else{
 					error("ProcedureBody expected");
 				}
 			}else{
 				error("';' expected");
 			}
 		}else{
 			error("'procedure' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode declarations() {
 		printFunction("Declarations");
 		indent();
 		if (isConst()) {
 			printNextsymbol();
 			insymbol();
 			if (isIdent()) {
 				printNextsymbol();
 				insymbol();
 				if (isEq()) {
 					printNextsymbol();
 					insymbol();
 					if (isExpression()) {
 						expression();
 						if (isSemicolon()) {
 							printNextsymbol();
 							insymbol();
 						}
 					} else {
 						error("Expression expected");
 					}
 				} else {
 					error("'=' expected");
 				}
 			} else {
 				error("Ident expected");
 			}
 			while (isIdent()) {
 				printNextsymbol();
 				insymbol();
 				if (isEq()) {
 					printNextsymbol();
 					insymbol();
 					if (isExpression()) {
 						expression();
 						if (isSemicolon()) {
 							printNextsymbol();
 							insymbol();
 						}
 					} else {
 						error("Expression expected");
 					}
 				} else {
 					error("'=' expected");
 				}
 			}
 		}
 		if (isType()) { // -------------------TYPE
 			printNextsymbol();
 			insymbol();
 			if (isIdent()) {
 				printNextsymbol();
 				insymbol();
 				if (isEq()) {
 					printNextsymbol();
 					insymbol();
 					if (isIdent() || isArray() || isRecord()) {
 						type();
 						if (isSemicolon()) {
 							printNextsymbol();
 							insymbol();
 						}
 					} else {
 						error("Expression expected");
 					}
 				} else {
 					error("'=' expected");
 				}
 			} else {
 				error("Ident expected");
 			}
 			while (isIdent()) {
 				printNextsymbol();
 				insymbol();
 				if (isEq()) {
 					printNextsymbol();
 					insymbol();
 					if (isIdent() || isArray() || isRecord()) {
 						type();
 						if (isSemicolon()) {
 							printNextsymbol();
 							insymbol();
 						}
 					} else {
 						error("Expression expected");
 					}
 				} else {
 					error("'=' expected");
 				}
 			}
 
 		}
 		if (isVar()) {
 			printNextsymbol();
 			insymbol();
 			if (isIdent()) {
 				identList();
 				if (isColon()) {
 					printNextsymbol();
 					insymbol();
 					if (isIdent() || isArray() || isRecord()) {
 						type();
 						if (isSemicolon()) {
 							printNextsymbol();
 							insymbol();
 						} else {
 							error("';' expected");
 						}
 					} else {
 						error("Type expected");
 					}
 				} else {
 					error("':' expected");
 				}
 
 			} else {
 				error("IdentList expected");
 			}
 			while (isIdent()) {
 				identList();
 				if (isColon()) {
 					printNextsymbol();
 					insymbol();
 					if (isIdent() || isArray() || isRecord()) {
 						type();
 						if (isSemicolon()) {
 							printNextsymbol();
 							insymbol();
 						} else {
 							error("';' expected");
 						}
 					} else {
 						error("Type expected");
 					}
 				} else {
 					error("':' expected");
 				}
 			}
 		}
 		while (isProcedure()) {
 			procedureDeclaration();
 			if (isSemicolon()) {
 				printNextsymbol();
 				insymbol();
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode module(){
 		printFunction("Module");
 		indent();
 		if(isModule()){
 			printNextsymbol();
 			insymbol();
 			if(isIdent()){
 				printNextsymbol();
 				insymbol();
 				if(isSemicolon()){
 					printNextsymbol();
 					insymbol();
 					if(isDeclaration()){
 						declarations();
 					}
 					if(isBegin()){
 						printNextsymbol();
 						insymbol();
 						if(isStatement()){
 							statementSequence();
 						}
 						if(isEnd()){
 							printNextsymbol();
 							insymbol();
 							if(isIdent()){
 								printNextsymbol();
 								insymbol();
 								if(isDot()){
 									printNextsymbol();
 									insymbol();
 								}else{
 									error("'.' expected");
 								}
 							}else{
 								error("Ident expected");
 							}
 						}else{
 							error("'end' expected");
 						}
 					}else{
 						error("'begin' expected");
 					}
 				}else{
 					error("';' expected");
 				}
 			}else{
 				error("Ident expected");
 			}
 		}else{
 			error("'module' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 //-----------------------------------------------------------------------------------
 	static AbstractNode assignment(){
 		printFunction("Assignment");
 		indent();
 		if(isIdent()){
 			printNextsymbol();
 			insymbol();
 			if(isDot() || isLbrac()){
 				selector();
 			}
 			if(isAssign()){
 				printNextsymbol();
 				insymbol();
 				if(isExpression()){
 					expression();
 				}else{
 					error("Expression expected");
 				}
 			}else{
 				error("':=' expected");
 			}
 		}else{
 			error("Ident expected");
 		}
 		unindent();
 		return null;
 	}
 	static AbstractNode assignment(Token save){
 		printFunction("Assignment");
 		indent();
 		if(save.id() == ID){
 			printThisSymbol(save);
 			if(isDot() || isLbrac()){
 				selector();
 			}
 			if(isAssign()){
 				printNextsymbol();
 				insymbol();
 				if(isExpression()){
 					expression();
 				}else{
 					error("Expression expected");
 				}
 			}else{
 				error("':=' expected");
 			}
 		}else{
 			error("Ident expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode actualParameters(){
 		printFunction("ActualParameters");
 		indent();
 		if(isExpression()){
 			expression();
 		}else{
 			error("Expression expected");
 		}
 		while(isComma()){
 			printNextsymbol();
 			insymbol();
 			if(isExpression()){
 				expression();
 			}else{
 				error("Expression expected");
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode procedureCall(){
 		printFunction("ProcedureCall");
 		indent();
 		if(isIdent()){
 			printNextsymbol();
 			insymbol();
 			if(isLpar()){
 				printNextsymbol();
 				insymbol();
 				if(isExpression()){
 					actualParameters();
 				}
 				if(isRpar()){
 					printNextsymbol();
 					insymbol();
 				}else{
 					error("')' expected");
 				}
 			}else{
 				error("'(' expected");
 			}
 		}else{
 			error("Ident expected");
 		}
 		unindent();
 		return null;
 	}
 	static AbstractNode procedureCall(Token save){
 		printFunction("ProcedureCall");
 		indent();
 		if(save.id() == ID){
 			printThisSymbol(save);
 			if(isLpar()){
 				printNextsymbol();
 				insymbol();
 				if(isExpression()){
 					actualParameters();
 				}
 				if(isRpar()){
 					printNextsymbol();
 					insymbol();
 				}else{
 					error("')' expected");
 				}
 			}else{
 				error("'(' expected");
 			}
 		}else{
 			error("Ident expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode ifStatement(){
 		printFunction("IfStatement");
 		indent();
 		if(isIf()){
 			printNextsymbol();
 			insymbol();
 			if(isExpression()){
 				expression();
 				if(isThen()){
 					printNextsymbol();
 					insymbol();
 					if(isStatement()){
 						statementSequence();
 					}
 					while(isElsif()){
 						printNextsymbol();
 						insymbol();
 						if(isExpression()){
 							expression();
 							if(isThen()){
 								printNextsymbol();
 								insymbol();
 								if(isStatement()){
 									statementSequence();
 								}
 							}else{
 								error("'then' expected");
 							}
 						}else{
 							error("Expression expected");
 						}
 					}
 					if(isElse()){
 						printNextsymbol();
 						insymbol();
 						if(isStatement()){
 							statementSequence();
 						}
 					}
 					if(isEnd()){
 						printNextsymbol();
 						insymbol();
 					}else{
 						error("'end' expected");
 					}
 				}else{
 					error("'then' expected");
 				}
 			}else{
 				error("Expression expected");
 			}
 		}else{
 			error("'if' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode whileStatement(){
 		printFunction("WhileStatement");
 		indent();
 		if(isWhile()){
 			printNextsymbol();
 			insymbol();
 			if(isExpression()){
 				expression();
 				if(isDo()){
 					printNextsymbol();
 					insymbol();
 					if(isStatement()){
 						statementSequence();
 					}
 					if(isEnd()){
 						printNextsymbol();
 						insymbol();
 					}else{
 						error("'end' expected");
 					}
 				}else{
 					error("'do' expected");
 				}
 			}else{
 				error("Expression expected");
 			}
 		}else{
 			error("'while' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode repeatStatement(){
 		printFunction("RepeatStatement");
 		indent();
 		if(isRepeat()){
 			printNextsymbol();
 			insymbol();
 			if(isStatement()){
 				statementSequence();
 				if(isUntil()){
 					printNextsymbol();
 					insymbol();
 					if(isExpression()){
 						expression();
 					}else{
 						error("Expression expected");
 					}
 				}else{
 					error("'until' expected");
 				}
 			}else{
 				error("Statement expected");
 			}
 		}else{
 			error("'repeat' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode statement() {
 		printFunction("Statement");
 		indent();
 		if (isIdent()) {
 			Token save = nextSym;
 			insymbol();
 			if(isDot() || isLbrac() || isAssign()){
 				assignment(save);
 			}else if((isLpar())){
 				procedureCall(save);
 			}else{
 				error("Selector, ':=' or '(' expected");
 			}
 		} else if (isIf()) {
 			ifStatement();
 		} else if (isPrint()) {
 			printNextsymbol();
 			insymbol();
 			if (isExpression()) {
 				expression();
 			} else {
 				error("Expression expected");
 			}
 		} else if (isWhile()) {
 			whileStatement();
 		} else if (isRepeat()) {
 			repeatStatement();
 		}
 		unindent();
 		return null;
 	}
 
 	static AbstractNode statementSequence(){
 		printFunction("StatementSequence");
 		indent();
 		if(isStatement()){
 			statement();
 		}
 		while(isSemicolon()){
 			printNextsymbol();
 			insymbol();
 			if(isStatement()){
 				statement();
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	
 //-----------------------------------------------------------------------------------
 	static AbstractNode selector() {
 		printFunction("Selector");
 		indent();
 		while (isDot() || isLbrac()) {
 			if (isDot()) {
 				printNextsymbol();
 				insymbol();
 				if (isIdent()) {
 					printNextsymbol();
 					insymbol();
 				} else {
 					error("Ident expected");
 				}
 			} else if (isLbrac()) {
 				printNextsymbol();
 				insymbol();
 				if (isMinus() || isFactor()) {
 					expression();
 					if (isRbrac()) {
 						printNextsymbol();
 						insymbol();
 					} else {
 						error("']' expected");
 					}
 				}
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode factor(){
 		printFunction("Factor");
 		indent();
 		if(isIdent()){
 			printNextsymbol();
 			insymbol();
 			if(isDot() || isLbrac()){
 				selector();
 			}
 		}else if(isInteger()){
 			printNextsymbol();
 			insymbol();
 		}else if(isRead()){
 			read();
 		}else if(isLpar()){
 			printNextsymbol();
 			insymbol();
 			if(isMinus() || isFactor()){
 				expression();
 				if(isRpar()){
 					printNextsymbol();
 					insymbol();
 				}else{
 					error("')' expected");
 				}
 			}else{
 				error("Ident Selector, Integer, String, 'read' or '(' expected");
 			}
		}else if(isString()){
			printNextsymbol();
			insymbol();
			if(isIdent() || isInteger() || isError()){
				while(isIdent() || isInteger() || isError()){
				printNextsymbol();
				insymbol();}
				if(isString()){
					printNextsymbol();
					insymbol();
				} else {
					error("' ' ' expected");
				}
			} else {
				error("Ident expected");
			}
 		}else{
 			error("Ident Selector, Integer, String, READ or '(' expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode read(){
 		printFunction("Read");
 		indent();
 		if(isRead()){
 			printNextsymbol();
 			insymbol();
 		}else{
 			error("'read' expected");
 		}
 		if(isString()){
 			prompt();
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode prompt(){
 		printFunction("Prompt");
 		indent();
 		if(isString()){
 			printNextsymbol();
 			insymbol();
			if(isIdent() || isInteger() || isError()){
				while(isIdent() || isInteger() || isError()){
 				printNextsymbol();
				insymbol();}
 				if(isString()){
 					printNextsymbol();
 					insymbol();
 				} else {
 					error("' ' ' expected");
 				}
 			} else {
 				error("Ident expected");
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode term(){
 		printFunction("Term");
 		indent();
 		if(isFactor()){
 			factor();
 		}else{
 			error("Factor expected");
 		}
 		while(isMul() || isDiv()){
 			printNextsymbol();
 			insymbol();
 			if(isFactor()){
 				factor();
 			}else{
 				error("Factor expected");
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode simpleExpression(){
 		printFunction("SimpleExpression");
 		indent();
 		if(isMinus()){
 			printNextsymbol();
 			insymbol();
 		}
 		if(isFactor()){
 			term();
 		}else{
 			error("Term expected");
 		}
 		while(isPlus() || isMinus()){
 			printNextsymbol();
 			insymbol();
 			if(isFactor()){
 				term();
 			}else{
 				error("Term expected");
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode expression(){
 		printFunction("Expression");
 		indent();
 		if(isMinus() || isFactor()){
 			simpleExpression();
 		}else{
 			error("SimpleExpression expected");
 		}
 		if(isEq() || isNeq() || isLo() || isHi() || isLoEq() || isHiEq()){
 			printNextsymbol();
 			insymbol();
 			if(isMinus() || isFactor()){
 				simpleExpression();
 			}else{
 				error("SimpleExpression expected");
 			}
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode indexExpression(){
 		printFunction("IndexExpression");
 		indent();
 		if(isInteger()){
 			printNextsymbol();
 			insymbol();
 		}else if(isIdent()){
 			constIdent();
 		}else{
 			error("Integer or ConstIdent expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static AbstractNode constIdent(){
 		printFunction("ConstIdent");
 		indent();
 		if(isIdent()){
 			printNextsymbol();
 			insymbol();
 		}else{
 			error("Indent expected");
 		}
 		unindent();
 		return null;
 	}
 	
 	static void program(){
 		indent();
 		while(nextSym != null){
 			if(isModule()){
 				module();
 			}else if(isDeclaration()){
 				declarations();
 			}else{
 				error("unknown Code");
 			}
 		}
 		unindent();
 	}
 }
 
