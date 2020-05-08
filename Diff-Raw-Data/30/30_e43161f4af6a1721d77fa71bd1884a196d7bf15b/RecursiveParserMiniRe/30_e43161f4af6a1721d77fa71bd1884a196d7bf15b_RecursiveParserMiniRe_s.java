 package Source;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Stack;
 
 public class RecursiveParserMiniRe {
 	HashMap<String, Variable> variables = new HashMap<String,Variable>(); // These are variables generated during minireProgram run
 	Stack<Token> tokens;
 	boolean DEBUG = true;
 	
 	public static enum Symbol {L_PAREN, R_PAREN, REPLACE, BEGIN, END, EQUALS, REGEX, ID, WITH, COMMA, RECURSIVE_REPLACE, ASCII_STR, IN, DIFF, INTERS, PRINT, UNION, CHARCLASS, FIND, HASH, MAXFREQSTRING, END_LINE, SAVE_TO};
 	
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
 	
 	public RecursiveParserMiniRe(ArrayList<Token> inTokens) {
 		tokens = new Stack<Token>(); 
 		for (int i=inTokens.size()-1; i>=0; --i)
 			tokens.add(inTokens.get(i));
 		// Stack is pushed in reverse from list so first token is on top of stack
 	}
 		
 	/** Starts with a letter, followed by 0-9 letters or numbers or underscores
 	 * 
 	 * @param t
 	 * @return
 	 */
 	boolean isID(String t) {
 		if ( !((t.charAt(0) >= 'a' && t.charAt(0) <= 'z') || (t.charAt(0) >= 'A' && t.charAt(0) <= 'Z')) )
 			return false;
 		if (t.length() >= 10 || t.length() <= 1)
 			return false;
 		for (char c : t.toCharArray())
 			if ( !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') )
 				return false;
 		return true;
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
 		Node stl = statementList();
 		root.addChild(stl);
 		matchToken(Symbol.END);
 		return root;
 	}
 
 	/**
 	 * Statement List Rule
 	 * <statement-list> ->  <statement><statement-list-tail> 
 	 * @return 
 	 */
 	private Node statementList() throws ParseError {
 		if (DEBUG) System.out.println("STATEMENT LIST");
 		Node n = new Node("SL");
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
 			Node n = new Node("SLT");
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
 		Node n = new Node("statement");
 		Symbol sym = tokenToSymbol( peekToken() );
 		switch(sym) {
 		case ID:
 			Variable idToSet = new Variable(Variable.VAR_TYPE.STRING, matchToken(Symbol.ID).data.toString() );
 			matchToken(Symbol.EQUALS);
 			Symbol sym2 = tokenToSymbol( peekToken() );
 			
 			// Add Node ID
 			Node idNode = new Node("ID");
 			idNode.setData(idToSet);
 			n.addChild(idNode);
 			
 			Variable toValue;
 			switch (sym2) {
 			// Expression
 			case ID:
 			case FIND:
 			case L_PAREN:
 				// set to value of Expression, which will be a string list
 //				Node valNode = exp();
 //				exp();
 				toValue = new Variable( Variable.VAR_TYPE.STRINGLIST, exp() );
 //				n.addChild(valNode);
 				break;
 			// # Expression
 			case HASH:
 				// # <exp>, return length of expression 
 				if (DEBUG) System.out.println("DO #");
 				matchToken(Symbol.HASH);
 				// TODO : ARGH ALL OF THIS
 				int count = 1;
 				exp();
 				toValue = new Variable( Variable.VAR_TYPE.INT, count );
 				break;
 			// maxfreqstring(ID)
 			case MAXFREQSTRING:
 				// Magic maxfreqstring
 				if (DEBUG) System.out.println("DO MAX FREQ STRING");
 				matchToken(Symbol.MAXFREQSTRING);
 				matchToken(Symbol.L_PAREN);
 				Variable val = variables.get( matchToken(Symbol.ID).data );
 				if (val.type != Variable.VAR_TYPE.STRINGLIST)
 					throw new ParseError("PARSE-ERROR: Variable given to Max freq string not a string list! : is type '"+val.type+"'");
 				System.out.println("Set ID("+idToSet+") = maxfreqstring("+val+")");
 				String mostCommon = Operations.maxfreqstring( (ArrayList<String>)val.value );
 				toValue = new Variable( Variable.VAR_TYPE.STRING, mostCommon );
 //				variables.put(idToSet, val );
 				matchToken(Symbol.R_PAREN);
 				break;
 			default:
 				throw new ParseError("statement sub-switch ID was passed unexpected token: '"+sym2+"' for "+sym+" with stack "+tokens);
 			}
 			System.out.println("Do Set ID("+idToSet+") = SOMETHING("+toValue+")");
 //			variables[]
 			break;
 		case REPLACE:
 			if (DEBUG) System.out.println("DO REPLACE");
 			replace();
 			break;
 		case RECURSIVE_REPLACE:
 			if (DEBUG) System.out.println("DO RECURSIVE REPLACE");
 			matchToken(Symbol.RECURSIVE_REPLACE);
 			matchToken(Symbol.REGEX);
 			matchToken(Symbol.WITH);
 			matchToken(Symbol.ASCII_STR);
 			matchToken(Symbol.IN);
 			fileNames();
 			break;
 		case PRINT:
 			if (DEBUG) System.out.println("DO PRINT");
 			matchToken(Symbol.PRINT);
 			expressionList();
 			break;
 		default:
 			throw new ParseError("statement() was passed unexpected token: '"+sym+"' for "+tokens);
 		}
 		matchToken(Symbol.END_LINE);
 		return n;
 	}
 
 
 	private void replace() throws ParseError {
 		// TODO Auto-generated method stub
 		matchToken(Symbol.REPLACE);
 		String regex = matchToken(Symbol.REGEX).data.toString();
 		regex = regex.subSequence(1, regex.length()-1).toString();
 		matchToken(Symbol.WITH);
 		String ascii_str = matchToken(Symbol.ASCII_STR).data.toString();
 		ascii_str = ascii_str.subSequence(1, ascii_str.length()-1).toString();
 		matchToken(Symbol.IN);
 		ArrayList<String> files = fileNames();
 		String inFile = files.get(0);
 		String outFile = files.get(1);
 		System.out.println("DO REPLACE REGEX("+regex+") with ASCII_STR("+ascii_str+") with IN-FILE("+inFile+") save to OUT-FILE("+outFile+")");
 		try {
 			Operations.replace(regex, ascii_str, inFile, outFile);
 			System.out.println("REPLACE SUCCESSFUL");
 		} catch (IOException e){
 			System.out.println("REPLACE FAILED, SKIPPING");
 		}
 	}
 
 	/**
 	 * File-Names rule, essentially a save to command
 	 * <file-names> ->  <source-file>  >!  <destination-file>
 	 * @throws ParseError 
 	 */
 	private ArrayList<String> fileNames() throws ParseError {
 		if (DEBUG) System.out.println("FILENAMES (DOING SAVE TO)");
 		String inFile = sourceFile();
 		// TODO Read filenames
 		matchToken(Symbol.SAVE_TO);
 		String outFile = destinationFile();
 		ArrayList<String> files = new ArrayList<String>();
 		files.add(inFile);
 		files.add(outFile);
 		return files;
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
 	private void expressionList() throws ParseError {
 		if (DEBUG) System.out.println("EXPRESSION LIST");
 		ArrayList<String> listOfExpresssions = exp();
 		expressionListTail(listOfExpresssions);
 	}
 
 	/**
 	 * Expression List Tail Rule
 	 *	 <exp-list-tail> -> , <exp> <exp-list-tail>
 	 *   <exp-list-tail> -> epsilon(null)        <---- This was told on Piazza...
 	 * @throws ParseError 
 	 */
 	private void expressionListTail(ArrayList<String> expList) throws ParseError {
 		if (DEBUG) System.out.println("EXPRESSION LIST TAIL");
 		if (tokenToSymbol(peekToken()) == Symbol.COMMA) {
 			matchToken(Symbol.COMMA);			
 			ArrayList<String> expr = exp();
 			expList.addAll(expr);
 			expressionListTail(expList);
 		}
 	}
 
 	/**
 	 * Expression
 	 * <exp>-> ID  | ( <exp> ) 
 	 * <exp> -> <term> <exp-tail>
 	 * @throws ParseError 
 	 * 
 	 */
 	private ArrayList<String> exp() throws ParseError {
 		if (DEBUG) System.out.println("EXP");
 		ArrayList<String> listOfExpr = new ArrayList<String>();
 		Symbol sym = tokenToSymbol( peekToken() );
 		if (sym == Symbol.ID) {
 			Node n = new Node("EXP");
 			listOfExpr.add( matchToken(Symbol.ID).data.toString() );
 //			n.setData(value)
 //			return n;
 		} else if (sym == Symbol.L_PAREN) {
 			matchToken(Symbol.L_PAREN);
 			listOfExpr = exp();
 			matchToken(Symbol.R_PAREN);
 		} else {
 			listOfExpr = term();
 			expressionTail(listOfExpr);
 		}
 		return listOfExpr;
 	}
 
 	/**
 	 * Expression Tail
 	 * <exp-tail> ->  <bin-op> <term> <exp-tail> 
 	 * <exp-tail> -> epsilon
 	 * @throws ParseError 
 	 */
 	private void expressionTail(ArrayList<String> first) throws ParseError {
 		if (DEBUG) System.out.println("EXPRESSION TAIL");
 		Symbol sym = tokenToSymbol( peekToken() );
 		if (sym == Symbol.DIFF || sym == Symbol.UNION || sym == Symbol.INTERS) {
 			binaryOperators();
 			ArrayList<String> second = term();
 			if (sym == Symbol.DIFF){ // a - b Difference
 				// first list minus second
 				first.removeAll(second);
 			}
 			else if (sym == Symbol.UNION){ // Union
 				for (String thing : second) {
 					if (!first.contains(thing))
 						first.add(thing);
 				}
 			}
 			else { // Intersection
 				ArrayList<String> intersected = new ArrayList<String>();
 				for (String thing : second) {
 					if (first.contains(thing))
 						intersected.add(thing);
 				}
 				first.clear();
 				first.addAll(intersected);
 			}
 			expressionTail(first);
 		} // else epsilon
 	}
 
 	/**
 	 * <term> -> find REGEX in  <file-name>
 	 * @return String[] of those strings found via regex
 	 * @throws ParseError
 	 */
 	private ArrayList<String> term() throws ParseError {
 		if (DEBUG) System.out.println("TERM");
 		
 		matchToken(Symbol.FIND);
 		String regex = matchToken(Symbol.REGEX).data.toString();
 		regex = regex.substring(0, regex.length()-1); // Get rid of enclosing apostrophes
 		matchToken(Symbol.IN);
 		String fname = filename();
 		if (DEBUG) System.out.println("DO Find("+regex+","+fname+")");
 		ArrayList<String> out = new ArrayList<String>();
 		try {
 			out.addAll( Operations.find(regex, fname) );
 			System.out.println("FIND SUCCESSFUL: "+out);
 		} catch (IOException e) {
 			System.out.println("FIND FAILED, return empty string array list.");
 		}
 		
 		return out;
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
 	private void binaryOperators() throws ParseError {
 		if (DEBUG) System.out.println("BINARY OPERATOR");
 		Symbol sym = tokenToSymbol( peekToken() );
 		if (sym == Symbol.DIFF || sym == Symbol.UNION || sym == Symbol.INTERS) {
 			matchToken(sym);
 		}
 		else {
 			throw new ParseError("binaryOperators() was passed unexpected token + '"+sym+"' for "+tokens);
 		}
 	}
 	
 	// TODO : not needed probably
 	private HashSet<Character> tokenToEdges(Token token) {
 		HashSet<Character> chars = new HashSet<Character>();
 		for (char c : ((String)token.data).toCharArray() )
 			chars.add(c);
 		return chars;
 	}
 
 	///////////////
 	Symbol tokenToSymbol(Token t) throws ParseError {
 		String data = t.data.toString();
 //		if(data.equalsIgnoreCase(Symbol.CHARCLASS.name())) {
 //			return Symbol.CHARCLASS;
 //		}
 		if(t.type.equals("$ID_HASH")) {
 			return Symbol.HASH;
 		}
 		else if(t.type.equals("$ID_OPENPAREN")) {
 			return Symbol.L_PAREN;
 		}
 		else if(t.type.equals("$ID_CLOSEPAREN")) {
 			return Symbol.R_PAREN;
 		}
 		else if(t.type.equals("$ID_SAVETO")) {
 			return Symbol.SAVE_TO;
 		}
 		else if(data.equalsIgnoreCase("|") || data.equalsIgnoreCase(Symbol.UNION.name())) {
 			return Symbol.UNION;
 		}
 		else if(data.equalsIgnoreCase(Symbol.REPLACE.name())) {
 			return Symbol.REPLACE;
 		}
 		else if(data.equalsIgnoreCase(Symbol.BEGIN.name())) {
 			return Symbol.BEGIN;
 		}
 		else if(t.type.equalsIgnoreCase("$ID_ENDLINE")) {
 			return Symbol.END_LINE;
 		}
 		else if(data.equalsIgnoreCase(Symbol.END.name())) {
 			return Symbol.END;
 		}
 		else if(data.equalsIgnoreCase("=")) {
 			return Symbol.EQUALS;
 		}
 		else if(t.type.equals("$REGEX")) {
 			return Symbol.REGEX;
 		}
 		else if(data.equalsIgnoreCase(Symbol.ID.name())) {
 			return Symbol.ID;
 		}
 		else if(data.equalsIgnoreCase(Symbol.WITH.name())) {
 			return Symbol.WITH;
 		}
 		else if(data.equalsIgnoreCase(Symbol.COMMA.name())) {
 			return Symbol.COMMA;
 		}
 		else if(data.equalsIgnoreCase(Symbol.RECURSIVE_REPLACE.name())) {
 			return Symbol.RECURSIVE_REPLACE;
 		}
 		else if(t.type.equals("$ASCII")) {
 			return Symbol.ASCII_STR;
 		}
 		else if(data.equalsIgnoreCase(Symbol.IN.name())) {
 			return Symbol.IN;
 		}
 		else if(data.equalsIgnoreCase(Symbol.DIFF.name())) {
 			return Symbol.DIFF;
 		}
 		else if(data.equalsIgnoreCase(Symbol.INTERS.name())) {
 			return Symbol.INTERS;
 		}
 		else if(data.equalsIgnoreCase(Symbol.FIND.name())) {
 			return Symbol.FIND;
 		}
 		else if(data.equalsIgnoreCase(Symbol.PRINT.name())) {
 			return Symbol.PRINT;
 		}
 		else if(isID(data)) {
 			return Symbol.ID;
 		}
 		else {
 			throw new ParseError("Unable to find Symbol for Token : " + t + " with stack "+tokens);
 		}		
 	}
 
 }
