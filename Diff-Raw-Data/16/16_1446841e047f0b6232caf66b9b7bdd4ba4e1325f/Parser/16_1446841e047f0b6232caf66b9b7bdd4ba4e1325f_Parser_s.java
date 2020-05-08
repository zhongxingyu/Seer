 // etravers@uab.edu
 // Parser is a class to represent a recursive descent parser for the 
 // MicroScala programming language.	The parser also constructs a 
 // syntax tree representation of all executable constructions.
  
 import java.util.*;
 
 public class Parser {
 
 	protected MicroScalaLexer lexer;		// lexical analyzer
 	protected Token token;					// current token
 
 	public Parser () throws java.io.IOException {
 		lexer = new MicroScalaLexer (System . in);
 		getToken ();
 	}
 
 	protected void getToken () throws java.io.IOException {
 		token = lexer . nextToken ();
 	}
 
 	// compilationUnit parses MicroScala programs.
 	public void CompilationUnit () throws java.io.IOException {
 		// object id { {Def} MainDef	}
 		// object
 		if (token.symbol()!=TokenClass.OBJECT) {
 			ErrorMessage.print("object expected");
 		}
 		getToken();
 		
 		// id
 		if (token.symbol()!=TokenClass.ID) {
 			ErrorMessage.print("id expected");
 		}
 		getToken();
 		
 		// {
 		if (token.symbol()!=TokenClass.LEFTBRACE) {
 			ErrorMessage.print("Left Brace expected");
 		}
 		getToken();
 		
 		// one or more defs
 		while (token.symbol()==TokenClass.DEF||token.symbol()==TokenClass.VAR) {
 			if (token.symbol()==TokenClass.DEF) {
 				Def();
 			}
 			else {
 				VarDef();
 			}
 		}
 		
 		// }
 		if (token.symbol()!=TokenClass.RIGHTBRACE) {
 			ErrorMessage.print("Right BRACE Expected");
 		}
 		getToken();
 				
 		if (token.symbol()!=TokenClass.EOF) {
 			ErrorMessage.print("EOF expected");			
 		}
 	}
 	
 	// Def	
 	public void Def() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		String defID="";
 		if (token.symbol()!=TokenClass.DEF) {
 			ErrorMessage.print("Def expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		getToken();
 		if (token.symbol()==TokenClass.MAIN) {
 			defID="MAIN";
 			// this is a maindef
 			getToken();
 			if (token.symbol()!=TokenClass.LEFTPAREN) {
 				ErrorMessage.print("( expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()!=TokenClass.ARGS) {
 				ErrorMessage.print("args expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()!=TokenClass.COLON) {
 				ErrorMessage.print(": expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()!=TokenClass.ARRAY) {
 				ErrorMessage.print("Array expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()!=TokenClass.LEFTBRACKET) {
 				ErrorMessage.print("[ expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()!=TokenClass.STRING) {
 				ErrorMessage.print("String expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()!=TokenClass.RIGHTBRACKET) {
 				ErrorMessage.print("] expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()!=TokenClass.RIGHTPAREN) {
 				ErrorMessage.print(") expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()!=TokenClass.LEFTBRACE) {
 				ErrorMessage.print("{ expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			while (token.symbol()==TokenClass.VAR) {
 				VarDef();
 			}
 			int stmtNum=0;
 			do {
 				if (stmtNum==0) {
 					syntaxTree = Statement();
 					stmtNum++;
 				}
 				else {
 					syntaxTree = new SyntaxTree(";", syntaxTree, Statement());					
 				}
 			}
 			while (token.symbol()==TokenClass.IF || token.symbol()==TokenClass.WHILE || token.symbol()==TokenClass.ID || token.symbol()==TokenClass.PRINTLN || token.symbol()==TokenClass.LEFTBRACE);
 			
 			if (token.symbol()!=TokenClass.RIGHTBRACE) {
 				ErrorMessage.print("} expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 		
 		}
 		
 		else if (token.symbol()==TokenClass.ID) {
 			defID=token.lexeme();
 			// normal def
 			getToken();
 			if (token.symbol()!=TokenClass.LEFTPAREN) {
 				ErrorMessage.print("( expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 			
 			if (token.symbol()==TokenClass.ID) {
 				getToken();
 				
 				if (token.symbol()!=TokenClass.COLON) {
 					ErrorMessage.print(": expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 				}
 				getToken();
 				
 				Type();
 				
 				while (token.symbol()==TokenClass.COMMA) {
 					getToken();
 					if (token.symbol()!=TokenClass.ID) {
 						ErrorMessage.print("ID expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 					}
 					getToken();
 					if (token.symbol()!=TokenClass.COLON) {
 						ErrorMessage.print(": expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 					}
 					getToken();
 					Type();
 				}
 				if (token.symbol()!=TokenClass.RIGHTPAREN) {
 					ErrorMessage.print(") expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 				}
 				getToken();
 				if (token.symbol()!=TokenClass.COLON) {
 					ErrorMessage.print(": expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 				}
 				getToken();
 				Type();
 				if (token.symbol()!=TokenClass.ASSIGN) {
 					ErrorMessage.print("= expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 				}
 				getToken();
 				if (token.symbol()!=TokenClass.LEFTBRACE) {
 					ErrorMessage.print("{ expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 				}
 				getToken();
 				while (token.symbol()==TokenClass.VAR) {
 					VarDef();
 				}
 				int stmtNum=0;
 				while (token.symbol()==TokenClass.IF || token.symbol()==TokenClass.WHILE || token.symbol()==TokenClass.ID || token.symbol()==TokenClass.PRINTLN || token.symbol()==TokenClass.LEFTBRACE) {
 					if (stmtNum==0) {
 						syntaxTree = Statement();
 						stmtNum++;
 					}
 					else {
 						syntaxTree = new SyntaxTree(";", syntaxTree, Statement());						
 						stmtNum++;
 					}
 				}
 				if (token.symbol()!=TokenClass.RETURN) {
 					ErrorMessage.print("Return expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 				}				
 				getToken();
 				// TODO fix this.
 				syntaxTree= new SyntaxTree(";", syntaxTree, new SyntaxTree("RETURN", ListExpr()));
 				if (token.symbol()!=TokenClass.SEMICOLON) {
 					ErrorMessage.print("; expected, current token is " + token.lexeme());
 				}
 				getToken();
 				
 				if (token.symbol()!=TokenClass.RIGHTBRACE) {
 					ErrorMessage.print("} expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 				}
 				getToken();
 			}
 		}
 		
 		else {
 			VarDef();
 		}
 		syntaxTree.print(defID);
 		System . out . println ();
 	    System . out . println ();
 	}
	
	public void MainDef() throws java.io.IOException {
 		
	}
	
 	public void VarDef() throws java.io.IOException {
 		if (token.symbol()!=TokenClass.VAR) {
 			ErrorMessage.print("Var expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		getToken();
 		
 		if (token.symbol()!=TokenClass.ID) {
 			ErrorMessage.print("ID expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		getToken();
 		
 		if (token.symbol()!=TokenClass.COLON) {
 			ErrorMessage.print(": expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		getToken();
 		
 		Type();
 		
 		if (token.symbol()!=TokenClass.ASSIGN) {
 			ErrorMessage.print("= expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		getToken();
 		
 		Literal();
 		
 		if (token.symbol()!=TokenClass.SEMICOLON) {
 			ErrorMessage.print("; expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		getToken();
 		
 	}
 	
 	public void Type() throws java.io.IOException {
 		if (token.symbol()==TokenClass.INT) {
 			getToken();
 		}
 		else if (token.symbol()==TokenClass.LIST) {
 			getToken();
 			if (token.symbol()!=TokenClass.LEFTBRACKET) {
 				ErrorMessage.print("[ expected");
 			}
 			getToken();
 			if (token.symbol()!=TokenClass.INT) {
 				ErrorMessage.print("int expected");
 			}
 			getToken();
 			if (token.symbol()!=TokenClass.RIGHTBRACKET) {
 				ErrorMessage.print("] expected");
 			}
 			getToken();
 		}
 		else {
 			ErrorMessage.print("Type expected");
 		}
 	}
 	
 	public SyntaxTree Statement() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		if (token.symbol()==TokenClass.IF) {
 			getToken();
 			if (token.symbol()!=TokenClass.LEFTPAREN) {
 				ErrorMessage.print("( expected, current token is " + token.lexeme());
 			}
 			getToken();
 			SyntaxTree tmpExpr = Expr();
 			if (token.symbol()!=TokenClass.RIGHTPAREN) {
 				ErrorMessage.print(") expected, current token is " + token.lexeme());
 			}
 			getToken();
 			SyntaxTree result1 = Statement();
 			if (token.symbol()==TokenClass.ELSE) {
 				getToken();
 				SyntaxTree result2 = Statement();
 				
 				syntaxTree = new SyntaxTree("IF",tmpExpr,result1,result2);
 			}
 			else {
 				syntaxTree = new SyntaxTree("IF",tmpExpr,result1);				
 			}
 		}
 		else if (token.symbol()==TokenClass.WHILE) {
 			getToken();
 			if (token.symbol()!=TokenClass.LEFTPAREN) {
 				ErrorMessage.print("( expected, current token is " + token.lexeme());
 			}
 			getToken();
 			SyntaxTree tmpExpr = Expr();
 			if (token.symbol()!=TokenClass.RIGHTPAREN) {
 				ErrorMessage.print(") expected, current token is " + token.lexeme());
 			}
 			getToken();
 			syntaxTree = new SyntaxTree("WHILE", tmpExpr, Statement());
 		}
 		else if (token.symbol()==TokenClass.ID) {
 			SyntaxTree tmpID = new SyntaxTree(token.lexeme());
 			getToken();
 			if (token.symbol()!=TokenClass.ASSIGN) {
 				ErrorMessage.print("= expected, current token is " + token.lexeme());
 			}
 			getToken();
 			syntaxTree = new SyntaxTree("=", tmpID, ListExpr());
 			if (token.symbol()!=TokenClass.SEMICOLON) {
 				ErrorMessage.print("; expected, current token is " + token.lexeme());
 			}
 			getToken();
 		}
 		else if (token.symbol()==TokenClass.PRINTLN) {
 			getToken();
 			if (token.symbol()!=TokenClass.LEFTPAREN) {
 				ErrorMessage.print("( expected, current token is " + token.lexeme());
 			}
 			getToken();
 			SyntaxTree listExp = ListExpr();
 			syntaxTree = new SyntaxTree("PRINTLN", listExp);
 			if (token.symbol()!=TokenClass.RIGHTPAREN) {
 				ErrorMessage.print(") expected, current token is " + token.lexeme());
 			}
 			getToken();
 			if (token.symbol()!=TokenClass.SEMICOLON) {
 				ErrorMessage.print("; expected, current token is " + token.lexeme());
 			}
 			getToken();
 		}
 		else if (token.symbol()==TokenClass.LEFTBRACE) {
 			getToken();
 			do {
				syntaxTree= new SyntaxTree(";", syntaxTree, Statement());
 			}
 			while (token.symbol()==TokenClass.IF || token.symbol()==TokenClass.WHILE || token.symbol()==TokenClass.ID || token.symbol()==TokenClass.PRINTLN || token.symbol()==TokenClass.LEFTBRACE);
 			if (token.symbol()!=TokenClass.RIGHTBRACE) {
 				ErrorMessage.print("} expected, current token is " + token.lexeme());
 			}
 			getToken();
 		}
 		else {
 			ErrorMessage.print("Statement expected, current token is " + token.lexeme());
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree Expr() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		// AndExpr {|| AndExpr}
 		syntaxTree = AndExpr();
 		while (token.symbol()==TokenClass.OR) {
 			getToken();
 			syntaxTree = new SyntaxTree("||", syntaxTree, AndExpr());
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree AndExpr() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		// RelExpr {&& RelExpr}
 		syntaxTree = RelExpr();
 		while (token.symbol()==TokenClass.AND) {
 			getToken();
 			syntaxTree = new SyntaxTree("AND", syntaxTree, RelExpr());
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree RelExpr() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		// [!] ListExpr [RelOper ListExpr]
 		if (token.symbol()==TokenClass.NOT) {
 			getToken();
 			syntaxTree = new SyntaxTree("NOT", ListExpr());
 		}
 		else {
 			syntaxTree = ListExpr();
 		}
 		if (token.symbol()==TokenClass.RELOP) {
 			String tmpRel = token.lexeme();
 			getToken();
 			SyntaxTree tmpList = ListExpr();
 			syntaxTree = new SyntaxTree(tmpRel, syntaxTree, tmpList);
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree RelOper() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		if (token.symbol()!=TokenClass.RELOP) {
 			ErrorMessage.print("Relational operator expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		syntaxTree = new SyntaxTree(token.lexeme());
 		getToken();
 		return syntaxTree;
 	}
 	
 	public SyntaxTree ListExpr() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		// AddExpr | AddExpr :: ListExpr
 		syntaxTree = AddExpr();
 		if (token.symbol()==TokenClass.CONS) {
 			getToken();
 			SyntaxTree tmpList = ListExpr();
 			syntaxTree = new SyntaxTree("::", syntaxTree, tmpList);
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree AddExpr() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		// MulExpr {AddOper MulExpr}
 		syntaxTree = MulExpr();
 		while (token.symbol()==TokenClass.ADDOP) {
 			String tmpAdd = token.lexeme();
 			getToken();
 			syntaxTree= new SyntaxTree(tmpAdd,syntaxTree,MulExpr());
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree AddOper() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		if (token.symbol()!=TokenClass.ADDOP) {
 			ErrorMessage.print("+ or - expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 	 	syntaxTree = new SyntaxTree(token.lexeme());
 		getToken();
 		return syntaxTree;
 	}
 	
 	public SyntaxTree MulExpr() throws java.io.IOException {
 		// TODO I believe this is correct
 		SyntaxTree syntaxTree=null;
 		// PrefixExpr {MulOper PrefixExpr}
 		syntaxTree = PrefixExpr();
 		while (token.symbol()==TokenClass.MULTOP) {
 			String tmpOp = token.lexeme();
 			getToken();
 			syntaxTree = new SyntaxTree(tmpOp, syntaxTree, PrefixExpr());
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree MulOper() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		if (token.symbol()!=TokenClass.MULTOP) {
 			ErrorMessage.print("* or / expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		syntaxTree = new SyntaxTree(token.lexeme());
 		getToken();
 		return syntaxTree;
 	}
 	
 	public SyntaxTree PrefixExpr() throws java.io.IOException {
 		// TODO this is wrong, check further
 		SyntaxTree syntaxTree=null;
 		if (token.symbol()==TokenClass.ADDOP) {
 			String addop = token.lexeme();
 			getToken();
 			syntaxTree = new SyntaxTree(addop, SimpleExpr());
 		}
 		else {
 			syntaxTree = SimpleExpr();
 		}
 		while (token.symbol()==TokenClass.PERIOD) {
 			syntaxTree = new SyntaxTree(".", syntaxTree, ListMethodCall());
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree ListMethodCall() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		if (token.symbol()!=TokenClass.PERIOD) {
 			ErrorMessage.print(". expected, current token is " + token.lexeme());
 		}
 		getToken();
 		
 		if (token.symbol()!=TokenClass.LISTOP) {
 			ErrorMessage.print("List Operand expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 		}
 		syntaxTree = new SyntaxTree(token.lexeme());
 		getToken();
 		return syntaxTree;
 	}
 	
 	public SyntaxTree SimpleExpr() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		if (token.symbol()==TokenClass.NIL||token.symbol()==TokenClass.INTEGER) {
 			syntaxTree = Literal();
 		}
 		else if (token.symbol()==TokenClass.LEFTPAREN) {
 			// (Expr
 			getToken();
 			syntaxTree = Expr();
 			if (token.symbol()!=TokenClass.RIGHTPAREN) {
 				ErrorMessage.print(") expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			getToken();
 		}
 		else {
 			if (token.symbol()!=TokenClass.ID) {
 				ErrorMessage.print("ID expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 			}
 			String tmpID = token.lexeme();
 			syntaxTree = new SyntaxTree(token.lexeme());
 			getToken();
 			if (token.symbol()==TokenClass.LEFTPAREN) {
 				getToken();
 				// should be an optional ListExpr
 				SyntaxTree tmp = ListExpr();
 				// followed by 0 or more list expr preceded by a comma
 				while (token.symbol()==TokenClass.COMMA) {
 					getToken();
 					// TODO check this
 					tmp = new SyntaxTree(",", tmp, ListExpr());
 				}
 				if (token.symbol()!=TokenClass.RIGHTPAREN) {
 					ErrorMessage.print(") expected, current token is " + token.symbol() + " with the lexeme " + token.lexeme());
 				}
 				getToken();
 				syntaxTree = new SyntaxTree("APPLY", syntaxTree, tmp);
 				return syntaxTree;
 			}
 		}
 		return syntaxTree;
 	}
 	
 	public SyntaxTree Literal() throws java.io.IOException {
 		SyntaxTree syntaxTree=null;
 		if (token.symbol()!=TokenClass.INTEGER) {
 			if (token.symbol()!=TokenClass.NIL) {
 				ErrorMessage.print("Integer expected");
 			}
 			else {
 				syntaxTree = new SyntaxTree("NIL");
 				getToken();
 			}
 		}
 		else {
 			syntaxTree = new SyntaxTree(token.lexeme());
 			getToken();
 		}
 		return syntaxTree;
 	}
 }
