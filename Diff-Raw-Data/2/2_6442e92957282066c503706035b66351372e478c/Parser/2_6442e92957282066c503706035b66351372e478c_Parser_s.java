 package compiler.front;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import compiler.back.regAloc.VirtualRegisterFactory;
 import compiler.front.Scanner.ScannerException;
 import compiler.front.symbolTable.*;
 import compiler.front.symbolTable.Symbol.SymbolKind;
 import compiler.ir.cfg.BasicBlock;
 import compiler.ir.cfg.CFG;
 import compiler.ir.instructions.*;
 
 
 
 
 public class Parser {
 
 	public Scanner scanner;
 	private SymbolTable symTable;
 	private String sourceFile;
 	
 	// CFG
 	public List<Global> globals = new ArrayList<Global>();
 	public List<CFG> CFGs = new ArrayList<CFG>();
 	public CFG cfg;
 	public CFG mainCfg;
 	
 	public int instructionCnt;
 	
 	public Parser(String srcFile) {
 		symTable = new SymbolTable();
 		FunctionSymbol outputNumFnct = new FunctionSymbol("outputnum", SymbolKind.PROCEDURE);
 		outputNumFnct.formalParams.add(new ParamSymbol("num"));
 		
 		FunctionSymbol outputNumFnctCaseSensitive = new FunctionSymbol("OutputNum", SymbolKind.PROCEDURE);
 		outputNumFnctCaseSensitive.formalParams.add(new ParamSymbol("num"));
 		
 		symTable.insert(new FunctionSymbol("inputnum", SymbolKind.FUNCTION));
 		symTable.insert(outputNumFnct);
 		symTable.insert(new FunctionSymbol("outputnewline", SymbolKind.PROCEDURE));
 		
 		symTable.insert(new FunctionSymbol("InputNum", SymbolKind.FUNCTION));
 		symTable.insert(outputNumFnctCaseSensitive);
 		symTable.insert(new FunctionSymbol("OutputNewLine", SymbolKind.PROCEDURE));
 		
 		scanner = new Scanner();
 		sourceFile = srcFile;
 	}
 	
 	public void parse() throws ParserException, ScannerException {
 		scanner.open(sourceFile);
 		VirtualRegisterFactory.init();
 		scanner.next(); // scan the input symbol
 		computation();
 
         for (CFG cfg : CFGs) {
             cfg.calculateDepths();
             cfg.createDominatorEdges();
         }
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
 		mainCfg = cfg = new CFG("main");
 		CFGs.add(mainCfg);
 		
 		instructionCnt = 0;		
 		while(currentIsFirstOf(NonTerminals.VAR_DECL)) {
 			varDecl();
 		}
         
         // vars stored in start block
         BasicBlock genericBB = new BasicBlock("basic-block");
         CFG.addBranch(cfg.currentBB, genericBB);
         CFG.addLinearLink(cfg.currentBB, genericBB);
         cfg.setCurrentBB(genericBB);
         
 		while(currentIsFirstOf(NonTerminals.FUNC_DECL)) {
 			funcDecl();
 		}
 		expect(Tokens.L_BRACE);
 		cfg = mainCfg;
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
         
         // vars stored in start block
         BasicBlock genericBB = new BasicBlock("basic-block");
         CFG.addBranch(cfg.currentBB, genericBB);
         CFG.addLinearLink(cfg.currentBB, genericBB);
         cfg.setCurrentBB(genericBB);
         
 		expect(Tokens.L_BRACE);
 		if(currentIsFirstOf(NonTerminals.STAT_SEQUENCE)) {
 			statSequence();
 		}
 		expect(Tokens.R_BRACE);// TODO needs exit block if doesn't end in return earlier
 	}
 	
 	// formalParam = “(“ [ident { “,” ident }] “)”
 	private List<ParamSymbol> formalParam() throws ParserException, ScannerException {
 		List<ParamSymbol> formalParams = null;
 		expect(Tokens.L_PAREN);
 		if (peek(Tokens.IDENT)) {
 			formalParams = new ArrayList<ParamSymbol>();
 			String ident = ident();
 			ParamSymbol param = new ParamSymbol(ident);
 			formalParams.add(param); // add param to function symbol formal param list
 			insertSymbol(param); // add param to the symbol table
 			cfg.addParam(ident);
 			issue(new Param(param)); // add param to the frame 
 			while (accept(Tokens.COMMA)) {
 				ident = ident();
 				param = new ParamSymbol(ident);
 				formalParams.add(param);
 				insertSymbol(param);
 				cfg.addParam(ident);
 				issue(new Param(param));
 			}
 		}
 		expect(Tokens.R_PAREN);
 		return formalParams;
 	}
 	
 	// funcDecl = (“function” | “procedure”) ident [formalParam] “;” funcBody “;” 
 	private void funcDecl() throws ParserException, ScannerException {
 		boolean isFunc = false;
 		if (peek(Tokens.FUNCTION)) {
 			isFunc = true;
 		}
 		if(accept(Tokens.FUNCTION) || accept(Tokens.PROCEDURE)) {
 			String ident = ident();
 			cfg = new CFG(ident);
 			CFGs.add(cfg);
 			FunctionSymbol fnct = null;
 			if (isFunc) {
 				fnct = new FunctionSymbol(ident, SymbolKind.FUNCTION);
 			} else {
 				fnct = new FunctionSymbol(ident, SymbolKind.PROCEDURE);
 			}
 			insertSymbol(fnct);
 			cfg.setIsFunc(isFunc);
 			symTable.increaseScope();
 			if(currentIsFirstOf(NonTerminals.FORMAL_PARAM)) {
 				fnct.formalParams = formalParam(); // add formal parameters to function symbol
 			}
 			expect(Tokens.SEMI_COLON);
 			funcBody();
 			
 			CFG.addBranch(cfg.currentBB, cfg.exitBB); // current => exit
 			CFG.addLinearLink(cfg.currentBB, cfg.exitBB); // current -> exit			
 			cfg.setCurrentBB(cfg.exitBB);
 			
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
 		VarSymbol varSymbol = new VarSymbol(ident, type);
 		insertSymbol(varSymbol);
         cfg.addVar(varSymbol);
         if (cfg.label.equals("main")){
             issue(new Global(varSymbol));
         } else {
             issue(new Local(varSymbol));
         }
 		while (accept(Tokens.COMMA)) {
 			ident = ident();
 			varSymbol = new VarSymbol(ident, type);
 			insertSymbol(varSymbol);
 	        cfg.addVar(varSymbol);
 			if (cfg.label.equals("main")){
                 issue(new Global(varSymbol));
 			}else{
 			    issue(new Local(varSymbol));
 			}
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
 
 		// This lines are not needed anymore since we always link the 
 		// last block to the exit block, even in case of procedures
 //		CFG.addBranch(cfg.currentBB, cfg.exitBB); // current => exit
 //		CFG.addLinearLink(cfg.currentBB, cfg.exitBB); // current -> exit
 //		cfg.setCurrentBB(cfg.exitBB);
 		
 		if(currentIsFirstOf(NonTerminals.EXPRESSION)) {
 			Instruction ret = expression();
 			issue(new Return(ret));
 		} else {
 			issue(new Return());
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
 		
 		ControlFlowInstr rel = relation();
 		rel.setTargetBB(bodyBB);
 		
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
 		
 		ControlFlowInstr rel = relation();
 		rel.setTargetBB(thenBB);
 		
 		expect(Tokens.THEN);
 
 		CFG.addBranch(condBB, thenBB); // cond => then
 		CFG.addLinearLink(condBB, thenBB); // cond -> then
 		cfg.setCurrentBB(thenBB);
 		
 		statSequence();
 		
 		if (!cfg.currentBB.succ.contains(cfg.exitBB)) {
 			CFG.addBranch(cfg.currentBB, joinBB); // then => join			
 		}
 		CFG.addLinearLink(cfg.currentBB, elseBB); // then -> else
 		
 		// always have an else BB, even if empty
 		//Why?
 		CFG.addBranch(condBB, elseBB); // cond => else
 		cfg.setCurrentBB(elseBB);
 		
 		if (accept(Tokens.ELSE)) {
 			statSequence();
 		}
 		
 		expect(Tokens.FI);
 		if (!cfg.currentBB.succ.contains(cfg.exitBB)) {
 			CFG.addBranch(cfg.currentBB, joinBB); // else => join
 		}
 		CFG.addLinearLink(cfg.currentBB, joinBB); // else -> join
 		cfg.setCurrentBB(joinBB);
 		cfg.setCurrentJoinBB(joinBB);
 	}
 
 	// funcCall = “call” ident [ “(“ [expression { “,” expression } ] “)” ]
 	private Instruction funcCall() throws ParserException, ScannerException {
 		// TODO check if function is defined; check number of parameters
 		expect(Tokens.CALL);
 		String ident = ident();
 		FunctionSymbol fnct = (FunctionSymbol) tryResolve(ident);
 		List<Instruction> args = new ArrayList<Instruction>();
 		if (accept(Tokens.L_PAREN)) {
 			// TODO verify that arguments number match formal parameters
 			// the formal parameters can be accessed using ((FunctionSymbol)fnct).formalParams 
 			if (currentIsFirstOf(NonTerminals.EXPRESSION)) {
 				args.add(expression());
 				while (accept(Tokens.COMMA)) {
 					args.add(expression());
 				}
 			}
 			expect(Tokens.R_PAREN);
 		}
 		
 		return issue(new Call(fnct, args));
 	}
 	
 	// assignment = “let” designator “<-” expression
 	private Instruction assignment() throws ParserException, ScannerException {
 		expect(Tokens.LET);
 		Instruction designator = designator();
 		expect(Tokens.ASSIGN);
 		Instruction expression = expression();
 
 		if (designator instanceof Scalar) {
 			if (designator instanceof Global) {
 //			if (((Scalar)designator).symbol instanceof VarSymbol) {
 				 // if global store to global address
 	  			 // TODO clean up!
 				Instruction value = issue(new StoreValue(((Scalar)designator).symbol, expression));
 				return value;
 //				return issue(new LoadValue(sym));
 			}
 			// if scalar, update state vectors
 			return issue(new Move(expression, designator));
 		} else if (designator instanceof Index) {
 			// if array address, issue store
 //			return issue(new StoreValue(((LoadValue)((Index)designator).base).symbol, expression));
 			return issue(new StoreValue((Index) designator, expression));
 		}
 		return null;
 	}
 	
 	// relation = expression relOp expression
 	private ControlFlowInstr relation() throws ParserException, ScannerException {
 		Instruction left = expression();
 		Tokens token = relOp();
 		Instruction right = expression();
 		Cmp cmp = (Cmp) issue(new Cmp(left, right));
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
 
 			if (ret instanceof Scalar) { //TODO check this!!! frame has instr not scalars anymore
 				if (ret instanceof Global) {
 					Instruction value = issue(new LoadValue(((Scalar) ret).symbol));
 					return value;
 				}else if (ret instanceof Param) {
 					Instruction value = issue(new LoadValue(((Param) ret).symbol));
 					return value;
 				}
 				return ret;
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
 			ret = issue(new Immediate(number));
 		}
 		
 		return ret;
 	}
 	
 	// designator = ident{ "[" expression "]" }
 	private Instruction designator() throws ParserException, ScannerException {
 
 		String ident = ident();
 		Symbol sym = tryResolve(ident);
 		
 		if (sym.isSSA()) {
 			// TODO this little trick here works only because we have 
 			// a maximum depth of two in function declarations (no inner functions)
 			// I'm sure that there is a more elegant way to implement this 
 //			if (sym.scope == 0) { // global/main
 //				return mainCfg.frame.get(sym.slot);
 //			} else { // function declaration
 				// look it up in the function frame
 				// is either a local or a parameter
 				return cfg.frame.get(sym.slot);
 //			}
 		}
 		
 		// load global value, non array type
 		if (sym instanceof VarSymbol && sym.scope == 0 &&
 				!(((VarSymbol) sym).type instanceof ArrayType)) {
 //			Instruction value = issue(new LoadValue(sym));
 //			return value;
 			 return new Global(sym);
 		}
 		
 		
 		assume(Tokens.L_SQ_BRKT);
 		// load array base address
 		LoadValue base = (LoadValue)issue(new LoadValue(sym));
 		
 		// compute offset
 		List<Instruction> indexes = new ArrayList<Instruction>();
 		while (accept(Tokens.L_SQ_BRKT)) {
 			// build offset
 			Instruction idxNumber = expression();
 			expect(Tokens.R_SQ_BRKT);
 			Instruction idxInstr = issue(new Mul(idxNumber, issue(new Immediate(4))));
 			indexes.add(idxInstr);
 //	        addr = issue(new Index(addr, offset)); // index into array
 		}
 		
 		ArrayType type = (ArrayType) ((VarSymbol)sym).type;
 		Instruction offset = null;
 //		System.out.println(type.dim);
 		if (type.dim > 1) {
 			for (int i = 0; i < type.dim - 1; i++) {
				Instruction mul = issue(new Mul(indexes.get(i), 
 						issue(new Immediate(type.dimSize.get(i)))));
 				offset = issue(new Add(mul, indexes.get(i+1)));
 			}
 		} else {
 			offset = indexes.get(0);
 		}
 		
 		Instruction adda = issue(new Index(base, offset)); // index into array
 		return adda; // return computed address for array indexing
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
 		// TODO should also check the kind of the symbol? 
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
 		
 		// store parameters and local declared variables in the CFG current frame
 		if (instr instanceof Local || instr instanceof Param) {
 			// link symbol to slot in current frame
 			((Scalar)instr).symbol.slot = cfg.frame.size();
 			cfg.frame.add((Scalar)instr);
 		} else if (instr instanceof Global) { // store globals in the global array
 			globals.add((Global) instr);
 		} else { // issue instruction into BB
 			instr.setInstrNumber(instructionCnt++);
 			cfg.currentBB.appendInstruction(instr);			
 		}
 		
 		// return back the instruction
 		return instr;
 	}
 
     public void renumberInstructions() {
         int instrCnt = 0;
         for (CFG cfg : CFGs) {
             // Iterate over BBs and initialize entry and exit states
             Iterator<BasicBlock> blockIterator = cfg.topDownIterator();
             while (blockIterator.hasNext()) {
                 BasicBlock bb = blockIterator.next();
                 for (Instruction inst : bb.getInstructions()) {
                     inst.setInstrNumber(instrCnt++);
                 }
             }
         }
     }
 }
 
 
 
 
 
 
 
 
 
 
