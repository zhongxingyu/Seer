 package Source;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Stack;
 
 public class RecursiveParserMiniRe {
 //	HashMap<String, Variable> variables = new HashMap<String,Variable>(); // These are variables generated during minireProgram run
 	Stack<Token> tokens;
 	boolean DEBUG = true;
 	@SuppressWarnings("unused")
 	private ArrayList<String> knownSymbols; // ex ArrayList<String>( ["$ID", "$NUMBER", "$FIND", "$BEGIN"...] )
 	
 	public static enum Symbol {OPENPAREN, CLOSEPAREN, REPLACE, BEGIN, END, EQUALS, REGEX, ID, WITH, COMMA, RECURSIVE_REPLACE, ASCII_STR, IN, DIFF, INTERS, PRINT, UNION, CHARCLASS, FIND, HASH, MAXFREQSTRING, END_LINE, SAVE_TO};
 	
 	private Token peekToken() throws ParseError {
 		if (DEBUG) System.out.println("PEEK: "+tokens.peek() + " :: " + tokenToSymbol(tokens.peek()));
 		return tokens.peek();
 	}
 	
 	private Token matchToken(Symbol sym) throws ParseError {
 		Token tok =  tokens.pop();
 		if (DEBUG) System.out.println("POP: "+tok + " :: " + tokenToSymbol(tok));
 		if ( tokenToSymbol(tok) != sym)
			throw new ParseError("MatchToken Failed Pop,  Expecting " + sym + " but popped "+tokenToSymbol(tok));
 		return tok;
 	}
 	
 	private Token matchAnyToken() throws ParseError {
 		Token tok =  tokens.pop();
 		if (DEBUG) System.out.println("POP ANY: "+tok + " :: " + tokenToSymbol(tok));
 		return tok;
 	}
 	
 	public RecursiveParserMiniRe(ArrayList<Token> inTokens, ArrayList<String> knownSymbols) {
 		this.knownSymbols = knownSymbols;
 		tokens = new Stack<Token>(); 
 		for (int i=inTokens.size()-1; i>=0; --i)
 			tokens.add(inTokens.get(i));
 		// Stack is pushed in reverse from list so first token is on top of stack
 	}
 	
 	
 	//* ***********************************
 	//*	        Rules Start Here          *
 	//* ***********************************
 	 
 	/**
 	 * Mini-re Program turns stack into AST, returning root node
 	 * @return
 	 * @throws ParseError
 	 */
 	public Node minireProgram() throws ParseError {
 		if (DEBUG) System.out.println("MINIRE PROGRAM");
 		Node root = new Node("MINIRE");
 		matchToken(Symbol.BEGIN);
 		root.addChild(new Node ("BEGIN"));
 		Node stl = statementList();
 		root.addChild(stl);
 		matchToken(Symbol.END);
 		root.addChild(new Node ("END"));
 		return root;
 	}
 
 	/**
 	 * Statement List Rule
 	 * <statement-list> ->  <statement><statement-list-tail> 
 	 * @return 
 	 */
 	private Node statementList() throws ParseError {
 		if (DEBUG) System.out.println("STATEMENT LIST");
 		Node n = new Node("STATEMENT LIST");
 		n.addChild( statement() );
 		n.addChild( statementListTail() );
 		return n;
 	}
 
 	/**
 	 * Statement List Tail Rule
 	 * <statement-list-tail> -> <statement><statement-list-tail>  | epsilon
 	 */
 	private Node statementListTail() throws ParseError {
 		if (DEBUG) System.out.println("STATEMENT LIST TAIL");
 		Symbol sym = tokenToSymbol( peekToken() );
 		if (sym == Symbol.ID || sym == Symbol.REPLACE || sym == Symbol.RECURSIVE_REPLACE || sym == Symbol.PRINT) {
 			Node n = new Node("STATEMENT LIST TAIL");
 			n.addChild( statement() );
 			n.addChild( statementListTail() );
 			return n;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * <statement> -> ID = <exp> ;
 	 * <statement> -> ID = # <exp> ; 
 	 * <statement> -> ID = maxfreqstring (ID);
 	 * <statement> -> replace REGEX with ASCII-STR in  <file-names> ;
 	 * <statement> -> recursivereplace REGEX with ASCII-STR in  <file-names> ;
 	 * <statement> -> print ( <exp-list> ) ;
 	 */
 	private Node statement() throws ParseError {
 		if (DEBUG) System.out.println("STATEMENT");
 		Node n = new Node("STATEMENT");
 		Symbol sym = tokenToSymbol( peekToken() );
 		switch(sym) {
 		case ID:
 			Variable idToSet = new Variable(Variable.VAR_TYPE.STRING, matchToken(Symbol.ID).data.toString() );
 			// ID
 			Node idNode = new Node("ID");
 			idNode.setData(idToSet);
 			n.addChild(idNode);
 			
 			// ID =
 			matchToken(Symbol.EQUALS);
 			n.addChild(new Node("="));
 			Symbol sym2 = tokenToSymbol( peekToken() );
 			
 			switch (sym2) {
 			// Expression
 			case ID:
 			case FIND:
 			case OPENPAREN:
 				// set to value of Expression, which will be a string list
 				n.addChild( exp() );
 				break;
 			// # Expression
 			case HASH:
 				// # <exp>, return length of expression 
 				if (DEBUG) System.out.println("DO #");
 				matchToken(Symbol.HASH);
 				n.addChild(new Node("#"));
 				n.addChild( exp() );
 				break;
 			// maxfreqstring(ID)
 			case MAXFREQSTRING:
 				// Magic maxfreqstring
 				if (DEBUG) System.out.println("DO MAX FREQ STRING");
 				matchToken(Symbol.MAXFREQSTRING);
 				n.addChild( new Node("MAXFREQSTRING") );
 				matchToken(Symbol.OPENPAREN);
 				n.addChild( new Node("(") );
 				Token tok = matchToken(Symbol.ID);
 //				if (!variables.containsKey(tok.data))
 //					throw new ParseError("PARSE-ERROR: ID("+tok.data+") passed to maxfreqstring not stored in variables("+variables+")!");
 //				Variable val = variables.get( tok.data );
 //				if (val.type != Variable.VAR_TYPE.STRINGLIST)
 //					throw new ParseError("PARSE-ERROR: Variable given to Max freq string not a string list! : is type '"+val.type+"'");
 				n.addChild( new Node("ID", new Variable(Variable.VAR_TYPE.STRING, tok.data.toString()) ) );
 				
 				matchToken(Symbol.CLOSEPAREN);
 				n.addChild( new Node(")") );
 				break;
 			default:
 				throw new ParseError("statement sub-switch ID was passed unexpected token: '"+sym2+"' for "+sym+" with stack "+tokens);
 			}
 //			System.out.println("Do Set ID("+idToSet+") = SOMETHING("+toValue+")");
 //			variables[]
 			break;
 		case REPLACE:
 			ArrayList<Node> nodes = replace();
 			for (Node node : nodes)
 				n.addChild(node);
 			break;
 		case RECURSIVE_REPLACE:
 			
 			ArrayList<Node> nodes1 = recursivereplace();
 			for (Node node : nodes1)
 				n.addChild(node);
 			break;
 		case PRINT:
 			if (DEBUG) System.out.println("DO PRINT");
 			
 			matchToken(Symbol.PRINT);
 			n.addChild( new Node("PRINT") );
 			
 			matchToken(Symbol.OPENPAREN);
 			n.addChild(new Node ("("));
 			n.addChild( expressionList() );
 			matchToken(Symbol.CLOSEPAREN);
 			n.addChild(new Node (")"));
 			break;
 		default:
 			throw new ParseError("statement() was passed unexpected token: '"+sym+"' for "+tokens);
 		}
 		matchToken(Symbol.END_LINE);
 		n.addChild(new Node (";"));
 		return n;
 	}
 
 	private ArrayList<Node> recursivereplace() throws ParseError {
 		if (DEBUG) System.out.println("DO RECURSIVE REPLACE");
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		
 		matchToken(Symbol.RECURSIVE_REPLACE);
 		nodes.add(new Node("RECURSIVE_REPLACE"));
 		
 		nodes.add( regex() );
 		
 		matchToken(Symbol.WITH);
 		nodes.add(new Node("WITH"));
 		
 		nodes.add( ascii_str() );
 		
 		matchToken(Symbol.IN);
 		nodes.add(new Node("IN"));
 		
 		ArrayList<Node> fnodes = fileNames();
 		for (Node node : fnodes)
 			nodes.add(node);
 		return nodes;
 	}
 
 	private ArrayList<Node> replace() throws ParseError {
 		if (DEBUG) System.out.println("REPLACE");
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		
 		matchToken(Symbol.REPLACE);
 		
 		nodes.add(new Node("REPLACE"));
 		
 		nodes.add( regex() );
 		
 		matchToken(Symbol.WITH);
 		nodes.add(new Node("WITH"));
 		
 		nodes.add( ascii_str() );
 		
 		matchToken(Symbol.IN);
 		nodes.add(new Node("IN"));
 		
 		ArrayList<Node> fnodes = fileNames();
 		for (Node node : fnodes)
 			nodes.add(node);
 		
 		return nodes;
 	}
 	
 	private Node regex() throws ParseError {
 		String regex = matchToken(Symbol.REGEX).data.toString();
 		regex = regex.subSequence(1, regex.length()-1).toString();
 		return new Node("REGEX", new Variable(Variable.VAR_TYPE.STRING, regex));
 	}
 
 	private Node ascii_str() throws ParseError {
 		String ascii_str = matchToken(Symbol.ASCII_STR).data.toString();
 		ascii_str = ascii_str.subSequence(1, ascii_str.length()-1).toString();
 		return new Node("ASCII_STR", new Variable(Variable.VAR_TYPE.STRING, ascii_str));
 	}
 
 	/**
 	 * File-Names rule, essentially a save to command
 	 * <file-names> ->  <source-file>  >!  <destination-file>
 	 * @throws ParseError 
 	 */
 	private ArrayList<Node> fileNames() throws ParseError {
 		if (DEBUG) System.out.println("FILENAMES (DOING SAVE TO)");
 		ArrayList<Node> nodes = new ArrayList<Node>();
 		String inFile = sourceFile();
 		nodes.add(new Node(inFile));
 		
 		matchToken(Symbol.SAVE_TO);
 		nodes.add(new Node("SAVE TO"));
 		
 		String outFile = destinationFile();
 		nodes.add(new Node(outFile));
 		return nodes;
 	}
 
 	/**
 	 * Source File Rule
 	 * <source-file> ->  ASCII-STR  
 	 * @throws ParseError
 	 * @returns filename 
 	 */
 	private String sourceFile() throws ParseError {
 		if (DEBUG) System.out.println("SOURCE FILE");
 		return filename();
 	}
 
 	/**
 	 * Destination File Rule
 	 * <destination-file> -> ASCII-STR
 	 * @throws ParseError 
 	 * @returns filename
 	 */
 	private String destinationFile() throws ParseError {
 		if (DEBUG) System.out.println("DESTINATION FILE");
 		return filename();
 	}
 
 	/**
 	 *  Expression List Rule
 	 * <exp-list> -> <exp> <exp-list-tail>
 	 * @return 
 	 * @throws ParseError 
 	 */
 	private Node expressionList() throws ParseError {
 		if (DEBUG) System.out.println("EXPRESSION LIST");
 		Node node = new Node("EXP LIST");
 		node.addChild( exp() );
 		node.addChild( expressionListTail() );
 		return node;
 	}
 
 	/**
 	 * Expression List Tail Rule
 	 *	 <exp-list-tail> -> , <exp> <exp-list-tail>
 	 *   <exp-list-tail> -> epsilon(null)        <---- This was told on Piazza...
 	 * @throws ParseError 
 	 */
 	private Node expressionListTail() throws ParseError {
 		if (DEBUG) System.out.println("EXPRESSION LIST TAIL");
 		if (tokenToSymbol(peekToken()) == Symbol.COMMA) {
 			Node node = new Node("EXP LIST");
 			matchToken(Symbol.COMMA);		
 			node.addChild( new Node(",") );
 			node.addChild( exp() );
 			node.addChild( expressionListTail() );
 			return node;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Expression
 	 * <exp>-> ID  | ( <exp> ) 
 	 * <exp> -> <term> <exp-tail>
 	 * @throws ParseError 
 	 * 
 	 */
 	private Node exp() throws ParseError {
 		if (DEBUG) System.out.println("EXP");
 		Symbol sym = tokenToSymbol( peekToken() );
 		Node node = new Node("EXP");
 		if (sym == Symbol.ID) {
 			Token tok = matchToken(Symbol.ID);
 //			if (DEBUG) System.out.println(tok);
 			Variable var; 
 			if (tok.type.equals("$NUMBER"))
 				var = new Variable(Variable.VAR_TYPE.INT, tok.data );
 			else if (tok.type.equals("$ID"))
 				var = new Variable(Variable.VAR_TYPE.STRING, tok.data );
 			else
 				var = new Variable(Variable.VAR_TYPE.STRINGLIST, tok.data );
 			node.addChild( new Node("ID", var ) );
 		} else if (sym == Symbol.OPENPAREN) {
 			matchToken(Symbol.OPENPAREN);
 			node.addChild(new Node("("));
 			node.addChild( exp() );
 			matchToken(Symbol.CLOSEPAREN);
 			node.addChild(new Node(")"));
 		} else if (sym == Symbol.FIND){
 			node.addChild( term() );
 			node.addChild( expressionTail() );
 		} else {
 			node.addChild( new Node("UNKNOWN", new Variable(Variable.VAR_TYPE.STRING, matchAnyToken().data.toString()) ));
 		}
 		return node;
 	}
 
 	/**
 	 * Expression Tail
 	 * <exp-tail> ->  <bin-op> <term> <exp-tail> 
 	 * <exp-tail> -> epsilon
 	 * @throws ParseError 
 	 */
 	private Node expressionTail() throws ParseError {
 		if (DEBUG) System.out.println("EXPRESSION TAIL");
 		Symbol sym = tokenToSymbol( peekToken() );
 		if (sym == Symbol.DIFF || sym == Symbol.UNION || sym == Symbol.INTERS) {
 			Node node = new Node("EXP TAIL");
 			node.addChild( binaryOperators() );
 			node.addChild( term() );
 			node.addChild( expressionTail() );
 			return node;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * <term> -> find REGEX in  <file-name>
 	 * @return String[] of those strings found via regex
 	 * @throws ParseError
 	 */
 	private Node term() throws ParseError {
 		if (DEBUG) System.out.println("TERM");
 		Node node = new Node("TERM");
 		matchToken(Symbol.FIND);
 		node.addChild(new Node ("FIND"));
 		
 		String regex = matchToken(Symbol.REGEX).data.toString();
 		regex = regex.substring(1, regex.length()-1); // Get rid of enclosing apostrophes
 		node.addChild(new Node ("REGEX", new Variable(Variable.VAR_TYPE.STRING, regex)));
 		
 		matchToken(Symbol.IN);
 		node.addChild(new Node ("IN"));
 		
 		String fname = filename();
 		node.addChild(new Node ("FILE", new Variable(Variable.VAR_TYPE.STRING, fname)));
 		return node;
 	}
 
 	/**
 	 * Filename
 	 * <file-name> -> ASCII-STR
 	 * @throws ParseError 
 	 */
 	private String filename() throws ParseError {
 		if (DEBUG) System.out.println("FILENAME");
 		Token t = matchToken(Symbol.ASCII_STR);
 		String filename = t.data.toString().substring(1, t.data.toString().length()-1);
 		return filename;
 	}
 
 	/**
 	 * 
 	 * <bin-op> ->  diff | union | inters
 	 * @return 
 	 * @throws ParseError 
 	 */
 	private Node binaryOperators() throws ParseError {
 		if (DEBUG) System.out.println("BINARY OPERATOR");
 		Symbol sym = tokenToSymbol( peekToken() );
 		if (sym == Symbol.DIFF || sym == Symbol.UNION || sym == Symbol.INTERS) {
 			matchToken(sym);
 			return new Node(sym.name());
 		}
 		else {
 			throw new ParseError("binaryOperators() was passed unexpected token + '"+sym+"' for "+tokens);
 		}
 	}
 
 	///////////////
 	Symbol tokenToSymbol(Token t) throws ParseError {
 		for (Symbol s : Symbol.values()) {
 			if ( t.type.equals('$'+s.name()) )
 				return s;
 		}
 		if (t.type.equals("$NUMBER")) { // hack to allow numbers for now 
 			return Symbol.ID;
 		}
 		else {
 			throw new ParseError("Unable to find Symbol for Token : " + t + " with stack "+tokens);
 		}		
 	}
 
 }
