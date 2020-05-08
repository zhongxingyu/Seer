 package front;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ir.cfg.BasicBlock;
 import ir.cfg.CFG;
 import ir.instructions.Add;
 import ir.instructions.BranchEqual;
 import ir.instructions.BranchGreater;
 import ir.instructions.BranchGreaterEqual;
 import ir.instructions.BranchLesser;
 import ir.instructions.BranchLesserEqual;
 import ir.instructions.BranchNotEqual;
 import ir.instructions.Cmp;
 import ir.instructions.ControlFlowInstr;
 import ir.instructions.Div;
 import ir.instructions.Index;
 import ir.instructions.ArithmeticBinary;
 import ir.instructions.Immediate;
 import ir.instructions.LoadAddress;
 import ir.instructions.LoadValue;
 import ir.instructions.Mul;
 import ir.instructions.Instruction;
 import ir.instructions.Scalar;
 import ir.instructions.Sub;
 import front.Scanner.ScannerException;
 import front.symbolTable.FunctionSymbol;
 import front.symbolTable.ParamSymbol;
 import front.symbolTable.Symbol;
 import front.symbolTable.SymbolTable;
 import front.symbolTable.VarSymbol;
 import front.symbolTable.ArrayType;
 import front.symbolTable.Type;
 import front.symbolTable.VarType;
 
 public class Parser {
 
 	public Scanner scanner;
 	private SymbolTable symTable;
 	private String sourceFile;
 	
 	// CFG
 	public List<CFG> CFGs = new ArrayList<CFG>();
 	public CFG cfg;
 	
 	public Parser(String srcFile) {
 		symTable = new SymbolTable();
 		symTable.insert(new FunctionSymbol("inputnum"));
 		symTable.insert(new FunctionSymbol("outputnum"));
 		symTable.insert(new FunctionSymbol("outputnewline"));
 		scanner = new Scanner();
 		sourceFile = srcFile;
 	}
 	
 	public void parse() throws ParserException, ScannerException {
 		scanner.open(sourceFile);
 		scanner.next(); // scan the input symbol
 		computation();
 	}
 
 	public void terminate() {
 		scanner.close();
 	}
 
 //	public void printError(String str) {
 //		System.err.println("Parsing: " + sourceFile + "\n" +
 //							"\t Error: " + str + "\n" +
 //							"\t Current token: " + scanner.currentToken + "\n" +
 //				   			"\t Current lexemme: " + scanner.currentLexeme + "\n" +
 //				   			"\t On line: " + scanner.getLineNumber());
 //
 //		new Exception().printStackTrace();
 //		System.exit(0);		
 //	}
 	
 	/**
 	 * Recursive descent parser modular design using accept/expect functions.
 	 * @throws ScannerException 
 	 * @throws IOException 
 	 */
 	private boolean accept(Tokens t) throws ScannerException {
 		if (scanner.currentToken == t) {
 			scanner.next();
 			return true;
 		}
 		return false;
 	}
 
 	private void expect(Tokens t) throws ParserException, ScannerException {
 		if (!accept(t))
 			throw new ParserException("Expected " + t.lexeme);
 	}
 
 	private boolean peek(Tokens t) {
 		if (scanner.currentToken == t) {
 			return true;
 		}
 		return false;
 	}
 
 	private void assume(Tokens t) throws ParserException {
 		if (!peek(t))
 			throw new ParserException("Expected " + t.lexeme);
 	}
 
 	
 	// computation = “main” { varDecl } { funcDecl } “{” statSequence “}” “.”
 	public void computation() throws ParserException, ScannerException {
 		expect(Tokens.MAIN);
 		
 		while(currentIsFirstOf(NonTerminals.VAR_DECL)) {
 			varDecl();
 		}
 		while(currentIsFirstOf(NonTerminals.FUNC_DECL)) {
 			funcDecl();
 		}
 		expect(Tokens.L_BRACE);
 		cfg = new CFG("main");
 		CFGs.add(cfg);
 		symTable.increaseScope();
 		statSequence();
 		symTable.decreaseScope();
 
 		CFG.addBranch(cfg.currentBB, cfg.exitBB); // current => exit
 		CFG.addLinearLink(cfg.currentBB, cfg.exitBB); // current -> exit
 		cfg.setCurrentBB(cfg.exitBB);
 		
 		expect(Tokens.R_BRACE);
 		expect(Tokens.PERIOD);
 	}
 	
 	// funcBody = { varDecl } “{” [ statSequence ] “}”
 	private void funcBody() throws ParserException, ScannerException {
 		while(currentIsFirstOf(NonTerminals.VAR_DECL)) {
 			varDecl();
 		}
 		expect(Tokens.L_BRACE);
 		if(currentIsFirstOf(NonTerminals.STAT_SEQUENCE)) {
 			statSequence();
 		}
 		expect(Tokens.R_BRACE);
 	}
 	
 	// formalParam = “(“ [ident { “,” ident }] “)”
 	private void formalParam() throws ParserException, ScannerException {
 		expect(Tokens.L_PAREN);
 		if (peek(Tokens.IDENT)) {
 			String ident = ident();
 			insertSymbol(new ParamSymbol(ident));
 			while (accept(Tokens.COMMA)) {
 				ident = ident();
 				insertSymbol(new ParamSymbol(ident));
 			}
 		}
 		expect(Tokens.R_PAREN);
 	}
 	
 	// funcDecl = (“function” | “procedure”) ident [formalParam] “;” funcBody “;” 
 	private void funcDecl() throws ParserException, ScannerException {
 		if(accept(Tokens.FUNCTION) || accept(Tokens.PROCEDURE)) {
 			String ident = ident();
 			CFGs.add(cfg = new CFG("ident"));
 			insertSymbol(new FunctionSymbol(ident)); // TODO add params to function
 			symTable.increaseScope();
 			if(currentIsFirstOf(NonTerminals.FORMAL_PARAM)) {
 				formalParam();
 			}
 			expect(Tokens.SEMI_COLON);
 			funcBody();
 			symTable.decreaseScope();
 			expect(Tokens.SEMI_COLON);
 		} else {
 			throw new ParserException("funcDecl parsing error");
 		}
 	}
 	
 	// varDecl = typeDecl ident { “,” ident } “;”
 	private void varDecl() throws ParserException, ScannerException {
 		Type type = typeDecl();
 		String ident = ident();
 		insertSymbol(new VarSymbol(ident, type));
 		while (accept(Tokens.COMMA)) {
 			ident = ident();
 			insertSymbol(new VarSymbol(ident, type));
 		}
 		expect(Tokens.SEMI_COLON);
 	}
 
 	// typeDecl = “var” | “array” “[“ number “]” { “[“ number “]” }
 	private Type typeDecl() throws ParserException, ScannerException {
 //		String typeStr = null;
 		if ( accept(Tokens.ARRAY)) {
 			ArrayType type = new ArrayType();
 //			typeStr = "array";
 			expect(Tokens.L_SQ_BRKT);
 //			typeStr += "[";
 			String number = number();
 //			typeStr += number;
 			type.addDimension(number);
 			expect(Tokens.R_SQ_BRKT);
 //			typeStr += "]";
 			while (accept(Tokens.L_SQ_BRKT)) {
 //				typeStr += "[";
 				number = number();
 //				typeStr += number;
 				type.addDimension(number);
 				expect(Tokens.R_SQ_BRKT);
 //				typeStr += "]";
 			}
 			return type;
 		} else {
 			expect(Tokens.VAR);
 //			typeStr = "var";
 			VarType type = new VarType();
 			return type;
 		}
 //		return typeStr;
 	}
 
 	// statSequence = statement { “;” statement }
 	private void statSequence() throws ParserException, ScannerException {
 		statement();
 		while (accept(Tokens.SEMI_COLON)) {
 			statement();
 		}		
 	}
 	
 	// statement = assignment | funcCall | ifStatement | whileStatement | returnStatement
 	private void statement() throws ParserException, ScannerException {
 		if (currentIsFirstOf(NonTerminals.ASSIGNMENT)) {
 			assignment();
 		} else if (currentIsFirstOf(NonTerminals.FUNC_CALL)) {
 			funcCall();
 		} else if (currentIsFirstOf(NonTerminals.IF_STATEMENT)) {
 			ifStatement();
 		} else if (currentIsFirstOf(NonTerminals.WHILE_STATEMENT)) {
 			whileStatement();
 		} else if (currentIsFirstOf(NonTerminals.RETURN_STATEMENT)) {
 			returnStatement();
 		} else {
 			throw new ParserException("Statement parsing error");
 		}
 	}
 
 	// returnStatement = “return” [ expression ] 
 	private void returnStatement() throws ParserException, ScannerException {
 		expect(Tokens.RETURN);
 
 		CFG.addBranch(cfg.currentBB, cfg.exitBB); // current => exit
		CFG.addLinearLink(cfg.currentBB, cfg.exitBB); // current -> exit
		cfg.setCurrentBB(cfg.exitBB);

 		if(currentIsFirstOf(NonTerminals.EXPRESSION)) {
 			expression();
 		}
 	}
 
 	// whileStatement = “while” relation “do” statSequence “od”
 	private void whileStatement() throws ParserException, ScannerException {
 		expect(Tokens.WHILE);
 		
 		BasicBlock condBB = new BasicBlock("while-cond");
 		BasicBlock bodyBB = new BasicBlock("while-body");
 		BasicBlock nextBB = new BasicBlock("while-next");
 		
 		CFG.addBranch(cfg.currentBB, condBB); // current => cond
 		CFG.addLinearLink(cfg.currentBB, condBB); // current -> cond
 		cfg.setCurrentBB(condBB);
 		cfg.setCurrentJoinBB(condBB);
 		
 		relation();
 		
 		expect(Tokens.DO);
 		
 		CFG.addBranch(condBB, bodyBB); // cond => body
 		CFG.addLinearLink(condBB, bodyBB); // cond -> body
 		cfg.setCurrentBB(bodyBB);
 		
 		statSequence();
 		
 		CFG.addBranch(cfg.currentBB, condBB); // body => cond
 		CFG.addLinearLink(cfg.currentBB, nextBB); // body -> next
 		
 		expect(Tokens.OD);
 		
 		CFG.addBranch(condBB, nextBB); // cond => next
 		cfg.setCurrentBB(nextBB);
 	}
 
 	// ifStatement = “if” relation “then” statSequence [ “else” statSequence ] “fi”
 	private void ifStatement() throws ParserException, ScannerException {
 		expect(Tokens.IF);
 
 		BasicBlock condBB = new BasicBlock("if-cond");
 		BasicBlock thenBB = new BasicBlock("then");
 		BasicBlock elseBB = new BasicBlock("else");
 		BasicBlock joinBB = new BasicBlock("fi-join");
 		
 		CFG.addBranch(cfg.currentBB, condBB); // current => cond
 		CFG.addLinearLink(cfg.currentBB, condBB); // current -> cond
 		cfg.setCurrentBB(condBB);
 		
 		relation();
 		
 		expect(Tokens.THEN);
 
 		CFG.addBranch(condBB, thenBB); // cond => then
 		CFG.addLinearLink(condBB, thenBB); // cond -> then
 		cfg.setCurrentBB(thenBB);
 		
 		statSequence();
 		
		CFG.addBranch(cfg.currentBB, joinBB); // then => join
 		CFG.addLinearLink(cfg.currentBB, elseBB); // then -> else
 		
 		// always have an else BB, even if empty
 		CFG.addBranch(condBB, elseBB); // cond => else
 		cfg.setCurrentBB(elseBB);
 		
 		if (accept(Tokens.ELSE)) {
 			statSequence();
 		}
 		
 		expect(Tokens.FI);

		CFG.addBranch(cfg.currentBB, joinBB); // else => join
 		CFG.addLinearLink(cfg.currentBB, joinBB); // else -> join
 		cfg.setCurrentBB(joinBB);
 		cfg.setCurrentJoinBB(joinBB);
 	}
 
 	// funcCall = “call” ident [ “(“ [expression { “,” expression } ] “)” ]
 	private Instruction funcCall() throws ParserException, ScannerException {
 		// TODO check if function is defined; check number of parameters
 		expect(Tokens.CALL);
 		String ident = ident();
 		tryResolve(ident);
 		if (accept(Tokens.L_PAREN)) {
 			if (currentIsFirstOf(NonTerminals.EXPRESSION)) {
 				expression();
 				while (accept(Tokens.COMMA)) {
 					expression();	
 				}
 			}
 			expect(Tokens.R_PAREN);
 		}
 		
 		return null;
 	}
 	
 	// assignment = “let” designator “<-” expression
 	private void assignment() throws ParserException, ScannerException {
 		expect(Tokens.LET);
 		designator();
 		expect(Tokens.ASSIGN);
 		expression();
 	}
 	
 	// relation = expression relOp expression
 	private ControlFlowInstr relation() throws ParserException, ScannerException {
 		Instruction left = expression();
 		Tokens token = relOp();
 		Instruction right = expression();
 		ArithmeticBinary cmp = (ArithmeticBinary) issue(new Cmp(left, right));
 		switch (token) {
 		case EQUAL:
 			return (ControlFlowInstr) issue(new BranchEqual(cmp));
 		case NOT_EQUAL:
 			return (ControlFlowInstr) issue(new BranchNotEqual(cmp));
 		case LESS_THAN:
 			return (ControlFlowInstr) issue(new BranchLesser(cmp));
 		case LESS_THAN_EQ:
 			return (ControlFlowInstr) issue(new BranchLesserEqual(cmp));
 		case GRT_THAN:
 			return (ControlFlowInstr) issue(new BranchGreater(cmp));
 		case GRT_THAN_EQ:
 			return (ControlFlowInstr) issue(new BranchGreaterEqual(cmp));
 		default:
 			throw new ParserException("Relation parsing error");
 		}
 	}
 	
 	// expression = term {(“+” | “-”) term}	
 	private Instruction expression() throws ParserException, ScannerException {
 		Instruction left = term();
 		while (peek(Tokens.ADD) || peek(Tokens.SUB)) {
 			if (accept(Tokens.ADD)) {
 				Instruction right = term();
 				left = issue(new Add(left, right));
 			} else if (accept(Tokens.SUB)) {
 				Instruction right = term();
 				left = issue(new Sub(left, right));
 			}
 		}
 		return left;
 	}
 	
 	// term = factor { (“*” | “/”) factor}
 	private Instruction term() throws ParserException, ScannerException {
 		Instruction left = factor();
 		while (peek(Tokens.MULT) || peek(Tokens.DIV)) {
 			if (accept(Tokens.MULT)) {
 				Instruction right = factor();
 				left = issue(new Mul(left, right));
 			} else if (accept(Tokens.DIV)) {
 				Instruction right = factor();
 				left = issue(new Div(left, right));
 			}
 		}
 		return left;
 	}
 	
 	// factor = designator | number | “(“ expression “)” | funcCall
 	private Instruction factor() throws ParserException, ScannerException {
 		Instruction ret = null;
 		
 		if (currentIsFirstOf(NonTerminals.DESIGNATOR)) {
 			ret = designator();
 
 			if (ret instanceof Scalar) {
 			// if scalar, update state vectors
 			// TODO
 			} else if (ret instanceof Index) {
 			// if array address, issue load
 				ret = issue(new LoadValue((Index)ret));
 			}
 		} else if (currentIsFirstOf(NonTerminals.FUNC_CALL)) {
 			ret = funcCall(); // TODO
 		} else if (accept(Tokens.L_PAREN)) {
 			ret = expression(); // should be already issued
 			expect(Tokens.R_PAREN);
 		} else {
 			String number = number();
 			ret = new Immediate(number); // TODO issue smth?
 		}
 		
 		return ret;
 	}
 	
 	// designator = ident{ "[" expression "]" }
 	private Instruction designator() throws ParserException, ScannerException {
 
 		String ident = ident();
 		Symbol sym = tryResolve(ident);
 		
 //		if (sym.isSSA()) { // FIXME was causing problems with arrays
 //			return new Scalar(sym);
 //		}
 		
 		Instruction addr = issue(new LoadAddress(sym)); // load array base address
 		while (accept(Tokens.L_SQ_BRKT)) {
 			Instruction offset = expression();
 			expect(Tokens.R_SQ_BRKT);
 	        addr = issue(new Index(addr, offset)); // index into array
 		}
 		
 		return addr; // return computed address for array indexing
 	}
 	
 	public Tokens relOp() throws ScannerException, ParserException {
 		if (accept(Tokens.EQUAL)) {
 			return Tokens.EQUAL;
 		} else if (accept(Tokens.NOT_EQUAL)) {
 			return Tokens.NOT_EQUAL;
 		} else if (accept(Tokens.LESS_THAN)) {
 			return Tokens.LESS_THAN;
 		} else if (accept(Tokens.LESS_THAN_EQ)) {
 			return Tokens.LESS_THAN_EQ;
 		} else if (accept(Tokens.GRT_THAN)) {
 			return Tokens.GRT_THAN;
 		} else if (accept(Tokens.GRT_THAN_EQ)) {
 			return Tokens.GRT_THAN_EQ;
 		} else {
 			throw new ParserException("Relation operator parsing error");
 		}
 	}
 	
 	// Helper methods
 	
 	// ident is actually a terminal symbol, not a nonterminal
 	// function added here just for expressiveness
 	private String ident() throws ParserException, ScannerException {
 		assume(Tokens.IDENT);
 		String ident = scanner.currentLexeme;
 		accept(Tokens.IDENT);
 		return ident;
 	}
 
 	// number is actually a terminal symbol, not a nonterminal
 	// function added here just for expressiveness
 	private String number() throws ParserException, ScannerException {
 		assume(Tokens.NUMBER);
 		String number = scanner.currentLexeme;
 		accept(Tokens.NUMBER);
 		return number;
 	}
 
 	private boolean currentIsFirstOf(NonTerminals nonTerminal)
 	{
 		return (nonTerminal.firstSet.contains(scanner.currentToken));
 	}
 	
 	private void insertSymbol(Symbol s) throws ParserException {
 		if (!symTable.insert(s)) {
 			throw new ParserException("Symbol already defined " + s.ident);
 		}
 	}
 
 //	private boolean tryResolveSymbol(String ident, SymbolKind kind) {
 //		if (symTable.resolve(ident, kind)) {
 //			return true;
 //		}
 //		return false;
 //	}
 
 	
 //	private void tryResolve(String ident, SymbolKind kind) throws ParserException {
 //		if (!symTable.resolve(ident, kind)) {
 //			throw new ParserException("Symbol not found " + ident);
 //		}
 //	}
 
 	private Symbol tryResolve(String ident) throws ParserException {
 		Symbol sym = symTable.resolve(ident);
 		if (sym == null) {
 			throw new ParserException("Symbol not found " + ident);
 		}
 		return sym;
 	}
 
 	// Helper classes
 	
 	public class ParserException extends Exception {
 		private static final long serialVersionUID = 1L;
 
 		String message;
 
 		ParserException() {
 			super();
 			message = 	"\n Exception while parsing: " + sourceFile + "\n" +
 						"\t Symbol: " + scanner.currentToken + "(" + scanner.currentLexeme + ")" + "\n" +
 						"\t Line:   " + scanner.getLineNumber() + "\n";
 		}
 
 		public ParserException(String error) {
 			this();
 			message += "\t Error: " + error; 
 		}
 		
 		public String getMessage() {
 			return message;
 		}
 	}
 
 	
 	public Instruction issue(Instruction instr) {
 		
 		// issue instruction into BB
 		cfg.currentBB.instructions.add(instr);
 		
 		// return back the instruction
 		return instr;
 	}
 }
 
 
 
 
 
 
 
 
 
 
