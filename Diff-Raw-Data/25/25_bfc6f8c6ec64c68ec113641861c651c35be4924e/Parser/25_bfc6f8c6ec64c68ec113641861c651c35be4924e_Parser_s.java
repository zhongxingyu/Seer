 package parser;
 
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 
 import lexer.Lexer;
 import lexer.Token;
 import lexer.Token.Kind;
 
 public class Parser {
 	Lexer lexer;
 	Token current;
 	LinkedList<Token> savedToken;
 
 	public Parser(String fname, java.io.InputStream fstream) {
 		lexer = new Lexer(fname, fstream);
 		current = lexer.nextToken();
 		savedToken = new LinkedList<Token>();
 	}
 
 	// /////////////////////////////////////////////
 	// utility methods to connect the lexer
 	// and the parser.
 
 	private void advance() {
 		// changed a little bit, we can save token now, use this tech to do look ahead
 		Token token;
 		try {
 			token = savedToken.removeFirst();
 			current = token;
 		} catch (NoSuchElementException e) {
 			current = lexer.nextToken();
 		}
 	}
 
 	private void saveToken(Token token) {
 		savedToken.addFirst(token);
 	}
 
 	private void eatToken(Kind kind) {
 		if (kind == current.kind)
 			advance();
 		else {
 			System.out.format("in line %d: ", lexer.lineno);
 			//because we can save token, lineno maybe not accurate
 			System.out.println("Expects: " + kind.toString());
 			System.out.println("But got: " + current.kind.toString());
 			System.exit(1);
 		}
 	}
 
 	private void error(String hint) {
 		System.out.format("Syntax error: hint: %s, line %d, current kind %s\n",
 				hint, lexer.lineno, current.kind.toString());
 		//because we can save token, lineno maybe not accurate
 		System.exit(1);
 		return;
 	}
 
 	// ////////////////////////////////////////////////////////////
 	// below are method for parsing.
 
 	// A bunch of parsing methods to parse expressions. The messy
 	// parts are to deal with precedence and associativity.
 
 	// ExpList -> Exp ExpRest*
 	// ->
 	// ExpRest -> , Exp
 	private LinkedList<ast.exp.T> parseExpList() {
 		LinkedList<ast.exp.T> result = new LinkedList<ast.exp.T>();
 		if (current.kind == Kind.TOKEN_RPAREN)
 			return result;
 		result.add(parseExp());
 		while (current.kind == Kind.TOKEN_COMMER) {
 			advance();
 			result.add(parseExp());
 		}
 		return result;
 	}
 
 	// AtomExp -> (exp)
 	// -> INTEGER_LITERAL
 	// -> true
 	// -> false
 	// -> this
 	// -> id
 	// -> new int [exp]
 	// -> new id ()
 	private ast.exp.T parseAtomExp() {
 		ast.exp.T result = null;
 		switch (current.kind) {
 		case TOKEN_LPAREN:
 			advance();
 			result = parseExp();
 			eatToken(Kind.TOKEN_RPAREN);
 			return result;
 		case TOKEN_NUM:
 			result = new ast.exp.Num(Integer.valueOf(current.lexeme));
 			advance();
 			return result;
 		case TOKEN_TRUE:
 			advance();
 			return new ast.exp.True();
 		case TOKEN_FALSE:
 			advance();
 			return new ast.exp.False();
 		case TOKEN_THIS:
 			advance();
 			return new ast.exp.This();
 		case TOKEN_ID:
 			result = new ast.exp.Id(current.lexeme);
 			advance();
 			return result;
 		case TOKEN_NEW: {
 			advance();
 			switch (current.kind) {
 			case TOKEN_INT:
 				advance();
 				eatToken(Kind.TOKEN_LBRACK);
 				result = parseExp();
 				eatToken(Kind.TOKEN_RBRACK);
 				return new ast.exp.NewIntArray(result);
 			case TOKEN_ID:
 				result = new ast.exp.NewObject(current.lexeme);
 				advance();
 				eatToken(Kind.TOKEN_LPAREN);
 				eatToken(Kind.TOKEN_RPAREN);
 				return result;
 			default:
 				error("in parseAtomExp, TOKEN_NEW case");
 				return null;
 			}
 		}
 		default:
 			error("in parseAtomExp, default case");
 			return null;
 		}
 	}
 
 	// NotExp -> AtomExp
 	// -> AtomExp .id (expList)
 	// -> AtomExp [exp]
 	// -> AtomExp .length
 	private ast.exp.T parseNotExp() {
 		ast.exp.T atomExp = parseAtomExp();
 		while (current.kind == Kind.TOKEN_DOT
 				|| current.kind == Kind.TOKEN_LBRACK) {
 			if (current.kind == Kind.TOKEN_DOT) {
 				advance();
 				if (current.kind == Kind.TOKEN_LENGTH) {
 					advance();
					return;
				}
				eatToken(Kind.TOKEN_ID);
				eatToken(Kind.TOKEN_LPAREN);
				parseExpList();
				eatToken(Kind.TOKEN_RPAREN);
			} else {
				advance();
				parseExp();
				eatToken(Kind.TOKEN_RBRACK);
			}
		}
		return;
 					atomExp = new ast.exp.Length(atomExp);
 				} else {
 					String id = current.lexeme;
 					eatToken(Kind.TOKEN_ID);
 					eatToken(Kind.TOKEN_LPAREN);
 					LinkedList<ast.exp.T> expList = parseExpList();
 					eatToken(Kind.TOKEN_RPAREN);
 					atomExp = new ast.exp.Call(atomExp, id, expList);
 				}
 			} else {
 				// must be TOKEN_LBRACK
 				advance();
 				ast.exp.T exp = parseExp();
 				eatToken(Kind.TOKEN_RBRACK);
 				atomExp =  new ast.exp.ArraySelect(atomExp, exp);
 			}
 		}
 		return atomExp;
 	}
 
 	// TimesExp -> ! TimesExp
 	// -> NotExp
 	private ast.exp.T parseTimesExp() {
 		int nest = 0;
 		while (current.kind == Kind.TOKEN_NOT) {
 			// handle nested not exp like !!!exp
 			nest += 1;
 			advance();
 		}
 		ast.exp.T notExp = parseNotExp();
 		for (int i = 0; i < nest; i++)
 			notExp = new ast.exp.Not(notExp);
 		return notExp;
 	}
 
 	// AddSubExp -> TimesExp * TimesExp
 	// -> TimesExp
 	private ast.exp.T parseAddSubExp() {
 		ast.exp.T timesExp = parseTimesExp();
 		while (current.kind == Kind.TOKEN_TIMES) {
 			advance();
 			timesExp = new ast.exp.Times(timesExp, parseTimesExp());
 		}
 		return timesExp;
 	}
 
 	// LtExp -> AddSubExp + AddSubExp
 	// -> AddSubExp - AddSubExp
 	// -> AddSubExp
 	private ast.exp.T parseLtExp() {
 		ast.exp.T addSubExp = parseAddSubExp();
 		while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
 			if (current.kind == Kind.TOKEN_ADD) {
 				advance();
 				addSubExp = new ast.exp.Add(addSubExp, parseAddSubExp());
 			} else {
 				//must Kind.TOKEN_SUB
 				advance();
 				addSubExp = new ast.exp.Sub(addSubExp, parseAddSubExp());
 			}
 		}
 		return addSubExp;
 	}
 
 	// AndExp -> LtExp < LtExp
 	// -> LtExp
 	private ast.exp.T parseAndExp() {
 		ast.exp.T ltExp = parseLtExp();;
 		while (current.kind == Kind.TOKEN_LT) {
 			advance();
 			ltExp = new ast.exp.Lt(ltExp, parseLtExp());
 		}
 		return ltExp;
 	}
 
 	// Exp -> AndExp && AndExp
 	// -> AndExp
 	private ast.exp.T parseExp() {
 		ast.exp.T andExp = parseAndExp();
 		while (current.kind == Kind.TOKEN_AND) {
 			advance();
 			andExp = new ast.exp.And(andExp, parseAndExp());
 		}
 		return andExp;
 	}
 
 	// Statement -> { Statement* }
 	// -> if ( Exp ) Statement else Statement
 	// -> while ( Exp ) Statement
 	// -> System.out.println ( Exp ) ;
 	// -> id = Exp ;
 	// -> id [ Exp ]= Exp ;
 	private ast.stm.T parseStatement() {
 		switch (current.kind) {
 		case TOKEN_LBRACE:
 			advance();
 			LinkedList<ast.stm.T> statements = parseStatements();
 			eatToken(Kind.TOKEN_RBRACE);
 			return new ast.stm.Block(statements);
 		case TOKEN_IF:
 			advance();
 			eatToken(Kind.TOKEN_LPAREN);
 			ast.exp.T condition = parseExp();
 			eatToken(Kind.TOKEN_RPAREN);
 			ast.stm.T if_body = parseStatement();
 			eatToken(Kind.TOKEN_ELSE);
 			ast.stm.T else_body = parseStatement();
 			return new ast.stm.If(condition, if_body, else_body);
 		case TOKEN_WHILE:
 			advance();
 			eatToken(Kind.TOKEN_LPAREN);
 			ast.exp.T cond = parseExp();
 			eatToken(Kind.TOKEN_RPAREN);
 			ast.stm.T body = parseStatement();
 			return new ast.stm.While(cond, body);
 		case TOKEN_SYSTEM:
 			advance();
 			eatToken(Kind.TOKEN_DOT);
 			eatToken(Kind.TOKEN_OUT);
 			eatToken(Kind.TOKEN_DOT);
 			eatToken(Kind.TOKEN_PRINTLN);
 			eatToken(Kind.TOKEN_LPAREN);
 			ast.exp.T exp = parseExp();
 			eatToken(Kind.TOKEN_RPAREN);
 			eatToken(Kind.TOKEN_SEMI);
 			return new ast.stm.Print(exp);
 		case TOKEN_ID:
 			String id = current.lexeme;
 			advance();
 			if (current.kind == Kind.TOKEN_ASSIGN) {
 				advance();
 				ast.exp.T exp1 = parseExp();
 				eatToken(Kind.TOKEN_SEMI);
 				return new ast.stm.Assign(id, exp1);
 			} else if (current.kind == Kind.TOKEN_LBRACK) {
 				advance();
 				ast.exp.T index = parseExp();
 				eatToken(Kind.TOKEN_RBRACK);
 				eatToken(Kind.TOKEN_ASSIGN);
 				ast.exp.T exp2 = parseExp();
 				eatToken(Kind.TOKEN_SEMI);
 				return new ast.stm.AssignArray(id, index, exp2);
 			} else {
 				error("in parseStatement, TOKEN_ID case");
 				return null;
 			}
 		default:
 			error("in parseStatement, default case");
 			return null;
 		}
 	}
 
 	// Statements -> Statement Statements
 	// ->
 	private LinkedList<ast.stm.T> parseStatements() {
 		LinkedList<ast.stm.T> result = new LinkedList<ast.stm.T>();
 		while (current.kind == Kind.TOKEN_LBRACE
 				|| current.kind == Kind.TOKEN_IF
 				|| current.kind == Kind.TOKEN_WHILE
 				|| current.kind == Kind.TOKEN_SYSTEM
 				|| current.kind == Kind.TOKEN_ID) {
 			result.add(parseStatement());
 		}
 		return result;
 	}
 
 	// Type -> int []
 	// -> boolean
 	// -> int
 	// -> id
 	private ast.type.T parseType() {
 		switch (current.kind) {
 		case TOKEN_INT:
 			advance();
 			if (current.kind == Kind.TOKEN_LBRACK) {
 				advance();
 				eatToken(Kind.TOKEN_RBRACK);
 				return new ast.type.IntArray();
 			} else
 				return new ast.type.Int();
 		case TOKEN_BOOLEAN:
 			advance();
 			return new ast.type.Boolean();
 		case TOKEN_ID:// class type
 			String id = current.lexeme;
 			advance();
 			return new ast.type.Class(id);
 		default:
 			error("in parseType, default case");
 			return null;
 		}
 	}
 
 	// VarDecl -> Type id ;
 	private ast.dec.T parseVarDecl() {
 		// to parse the "Type" nonterminal in this method, instead of writing
 		// a fresh one.
 
 		ast.type.T type = parseType();
 		String id = current.lexeme;
 		eatToken(Kind.TOKEN_ID);
 		eatToken(Kind.TOKEN_SEMI);
 		return new ast.dec.Dec(type, id);
 	}
 
 	// VarDecls -> VarDecl VarDecls
 	// ->
 	private LinkedList<ast.dec.T> parseVarDecls() {
 		LinkedList<ast.dec.T> result = new LinkedList<ast.dec.T>();
 		for (;;) {
 			if (current.kind == Kind.TOKEN_INT
 					|| current.kind == Kind.TOKEN_BOOLEAN)
 				result.add(parseVarDecl());
 			else if (current.kind == Kind.TOKEN_ID) {
 				/* *
 				 * Here's a trick: because Type could be TOKEN_ID, and statement
 				 * could also started with TOKEN_ID, so we have to look ahead
 				 * for couple of tokens to see how to do it
 				 */
 				Token saved = current;
 				advance();
 				if (current.kind == Kind.TOKEN_ID) {
 					saveToken(current);
 					current = saved;
 					result.add(parseVarDecl());
 				} else {
 					saveToken(current);
 					current = saved;
 					return result;
 				}
 			} else
 				return result;
 		}
 	}
 
 	// FormalList -> Type id FormalRest*
 	// ->
 	// FormalRest -> , Type id
 	private LinkedList<ast.dec.T> parseFormalList() {
 		LinkedList<ast.dec.T> result = new LinkedList<ast.dec.T>();
 		if (current.kind == Kind.TOKEN_INT
 				|| current.kind == Kind.TOKEN_BOOLEAN
 				|| current.kind == Kind.TOKEN_ID) {
 			ast.type.T type = parseType();
 			String id = current.lexeme;
 			eatToken(Kind.TOKEN_ID);
 			result.add(new ast.dec.Dec(type, id));
 			while (current.kind == Kind.TOKEN_COMMER) {
 				advance();
 				ast.type.T type1 = parseType();
 				String id1 = current.lexeme;
 				eatToken(Kind.TOKEN_ID);
 				result.add(new ast.dec.Dec(type1, id1));
 			}
 		}
 		return result;
 	}
 
 	// Method -> public Type id ( FormalList )
 	// { VarDecl* Statement* return Exp ;}
 	private ast.method.Method parseMethod() {
 		switch (current.kind) {
 		case TOKEN_PUBLIC:
 			advance();
 			ast.type.T type = parseType();
 			String id = current.lexeme;
 			eatToken(Kind.TOKEN_ID);
 			eatToken(Kind.TOKEN_LPAREN);
 			LinkedList<ast.dec.T> formalList = parseFormalList();
 			eatToken(Kind.TOKEN_RPAREN);
 			eatToken(Kind.TOKEN_LBRACE);
 			LinkedList<ast.dec.T> varDecl = parseVarDecls();
 			LinkedList<ast.stm.T> statements = parseStatements();
 			eatToken(Kind.TOKEN_RETURN);
 			ast.exp.T exp = parseExp();
 			eatToken(Kind.TOKEN_SEMI);
 			eatToken(Kind.TOKEN_RBRACE);
 			return new ast.method.Method(type, id, formalList, varDecl, statements, exp);
 		default:
 			error("in parseMethod, default case");
 			return null;
 		}
 	}
 
 	// MethodDecls -> MethodDecl MethodDecls
 	// ->
 	private LinkedList<ast.method.T> parseMethodDecls() {
 		LinkedList<ast.method.T> methodDecls = new LinkedList<ast.method.T>();
 		while (current.kind == Kind.TOKEN_PUBLIC)
 			methodDecls.add(parseMethod());
 		return methodDecls;
 	}
 
 	// ClassDecl -> class id { VarDecl* MethodDecl* }
 	// -> class id extends id { VarDecl* MethodDecl* }
 	private ast.classs.T parseClassDecl() {
 		eatToken(Kind.TOKEN_CLASS);
 		String id = current.lexeme;
 		eatToken(Kind.TOKEN_ID);
 		String extendss = null;
 		if (current.kind == Kind.TOKEN_EXTENDS) {
 			eatToken(Kind.TOKEN_EXTENDS);
 			extendss = current.lexeme;
 			eatToken(Kind.TOKEN_ID);
 		}
 		eatToken(Kind.TOKEN_LBRACE);
 		LinkedList<ast.dec.T> varDecls = parseVarDecls();
 		LinkedList<ast.method.T> methodDecl = parseMethodDecls();
 		eatToken(Kind.TOKEN_RBRACE);
 		return new ast.classs.Class(id, extendss, varDecls, methodDecl);
 	}
 
 	// ClassDecls -> ClassDecl ClassDecls
 	// ->
 	private LinkedList<ast.classs.T> parseClassDecls() {
 		LinkedList<ast.classs.T> classDecl = new LinkedList<ast.classs.T>();
 		while (current.kind == Kind.TOKEN_CLASS)
 			classDecl.add(parseClassDecl());
 		return classDecl;
 	}
 
 	// MainClass -> class id
 	// {
 	// public static void main ( String [] id )
 	// {
 	// Statement
 	// }
 	// }
 	private ast.mainClass.T parseMainClass() {
 		switch (current.kind) {
 		case TOKEN_CLASS:
 			advance();
 			String id = current.lexeme;
 			eatToken(Kind.TOKEN_ID);
 			eatToken(Kind.TOKEN_LBRACE);
 			eatToken(Kind.TOKEN_PUBLIC);
 			eatToken(Kind.TOKEN_STATIC);
 			eatToken(Kind.TOKEN_VOID);
 			eatToken(Kind.TOKEN_MAIN);
 			eatToken(Kind.TOKEN_LPAREN);
 			eatToken(Kind.TOKEN_STRING);
 			eatToken(Kind.TOKEN_LBRACK);
 			eatToken(Kind.TOKEN_RBRACK);
 			String argsId = current.lexeme;
 			eatToken(Kind.TOKEN_ID);
 			eatToken(Kind.TOKEN_RPAREN);
 			eatToken(Kind.TOKEN_LBRACE);
 			ast.stm.T statement = parseStatement();
 			eatToken(Kind.TOKEN_RBRACE);
 			eatToken(Kind.TOKEN_RBRACE);
 			return new ast.mainClass.MainClass(id, argsId, statement);
 		default:
 			error("in parseMainClass, default case");
 			return null;
 		}
 	}
 
 	// Program -> MainClass ClassDecl*
 	private ast.program.T parseProgram() {
 		ast.mainClass.T mainClass = parseMainClass();
 		LinkedList<ast.classs.T> classs = parseClassDecls();
 		eatToken(Kind.TOKEN_EOF);
 		return new ast.program.Program(mainClass, classs);
 	}
 
 	public ast.program.T parse() {
 		return parseProgram();
 	}
 }
