 package org.cwi.waebric.parser;
 
 import java.util.List;
 
 import org.cwi.waebric.WaebricKeyword;
 import org.cwi.waebric.WaebricSymbol;
 import org.cwi.waebric.parser.ast.NodeList;
 import org.cwi.waebric.parser.ast.basic.IdCon;
 import org.cwi.waebric.parser.ast.basic.StrCon;
 import org.cwi.waebric.parser.ast.markup.Markup;
 import org.cwi.waebric.parser.ast.module.function.Formals;
 import org.cwi.waebric.parser.ast.statement.Assignment;
 import org.cwi.waebric.parser.ast.statement.Statement;
 import org.cwi.waebric.parser.ast.statement.Statement.MarkupEmbedding;
 import org.cwi.waebric.parser.ast.statement.Statement.MarkupExp;
 import org.cwi.waebric.parser.ast.statement.Statement.MarkupStat;
 import org.cwi.waebric.parser.ast.statement.predicate.Predicate;
 import org.cwi.waebric.scanner.WaebricScanner;
 import org.cwi.waebric.scanner.token.Token;
 import org.cwi.waebric.scanner.token.TokenIterator;
 import org.cwi.waebric.scanner.token.WaebricTokenSort;
 
 /**
  * module languages/waebric/syntax/Statements
  * 
  * @author Jeroen van Schagen
  * @date 27-05-2009
  */
 class StatementParser extends AbstractParser {
 
 	private final EmbeddingParser embeddingParser;
 	private final ExpressionParser expressionParser;
 	private final PredicateParser predicateParser;
 	private final MarkupParser markupParser;
 	
 	/**
 	 * Construct statement parser.
 	 * @param tokens
 	 * @param exceptions
 	 */
 	public StatementParser(TokenIterator tokens, List<SyntaxException> exceptions) {
 		super(tokens, exceptions);
 		
 		// Construct sub parsers
 		expressionParser = new ExpressionParser(tokens, exceptions);
 		predicateParser = new PredicateParser(tokens, exceptions);
 		embeddingParser = new EmbeddingParser(tokens, exceptions);
 		markupParser = new MarkupParser(tokens, exceptions);
 	}
 	
 	/**
 	 * @see Statement
 	 * @param formals Used to determine if an identifier is a mark-up or variable.
 	 * @return Statement
 	 * @throws SyntaxException 
 	 */
 	public Statement parseStatement() throws SyntaxException {
 		if(tokens.hasNext()) {
 			Token peek = tokens.peek(1); // Retrieve first token of statement
 			if(peek.getLexeme().equals(WaebricKeyword.IF)) {
 				// If(-else) statements start with an if keyword
 				return parseIfStatement();
 			} else if(peek.getLexeme().equals(WaebricKeyword.EACH)) {
 				// Each statements start with an each keyword
 				return parseEachStatement();
 			} else if(peek.getLexeme().equals(WaebricKeyword.LET)) {
 				// Let statements start with a let keyword
 				return parseLetStatement();
 			} else if(peek.getLexeme().equals(WaebricSymbol.LCBRACKET)) {
 				// Statement collections start with a {
 				return parseStatementCollection();
 			} else if(peek.getLexeme().equals(WaebricKeyword.COMMENT)) {
 				// Comment statements start with a comments keyword
 				return parseCommentStatement();
 			} else if(peek.getLexeme().equals(WaebricKeyword.ECHO)) {
 				if(tokens.hasNext(2) && tokens.peek(2).getSort() == WaebricTokenSort.EMBEDDING) {
 					// Embedding echo production is followed by a text
 					return parseEchoEmbeddingStatement();
 				} else {
 					// Embedding echo production is followed by an expression
 					return parseEchoExpressionStatement();
 				}
 			} else if(peek.getLexeme().equals(WaebricKeyword.CDATA)) {
 				// CData statements start with a cdata keyword
 				return parseCDataStatement();
 			} else if(peek.getLexeme().equals(WaebricKeyword.YIELD)) {
 				// Yield statements start with a yield keyword
 				return parseYieldStatement();
 			} else if(peek.getSort().equals(WaebricTokenSort.IDCON)) {
 				// Mark-up statements always begin with an identifier
 				return parseMarkupStatements();
 			} else {
 				reportUnexpectedToken(tokens.current(), "statement", 
 						"\"if\", \"each\", \"let\", \"{\", \"comment\", " +
 						"\"echo\", \"cdata\", \"yield\" or Markup");
 			}
 		}
 
 		return null;
 	}
 	
 	/**
 	 * @see Statement.If
 	 * @return IfStatement
 	 * @throws SyntaxException 
 	 */
 	public Statement.If parseIfStatement() throws SyntaxException {
 		Statement.If statement; 
 		
 		next(WaebricKeyword.IF, "if keyword", "\"if\" \"(\"");
 		
 		// Parse "(" Predicate ")
 		next(WaebricSymbol.LPARANTHESIS, "Predicate opening \"(\"", "\"if\" \"(\" Predicate");
 		Predicate predicate = null;
 		try {
 			predicate = predicateParser.parsePredicate();
 		} catch(SyntaxException e) {
 			reportUnexpectedToken(tokens.current(), "Predicate", "\"if(\" Predicate \")\"");
 		}
 		next(WaebricSymbol.RPARANTHESIS, "Predicate closure \")\"", "\"(\" Predicate \")\"");
 
 		// Parse "true" sub-statement
 		Statement trueStatement = null;
 		try {
 			trueStatement = parseStatement();
 		} catch(SyntaxException e) {
 			reportUnexpectedToken(tokens.current(), "True statement", "\"if(args)\" Statement");
 		}
 
 		// Determine statement type by checking for "else" keyword
 		if(tokens.hasNext() && tokens.peek(1).getLexeme().equals(WaebricKeyword.ELSE)) {
 			tokens.next(); // Accept and skip "else" keyword
 			Statement elseStatement = null;
 			try {
 				// Parse "false" sub-statement
 				elseStatement = parseStatement();
 			} catch(SyntaxException e) {
 				reportUnexpectedToken(tokens.current(), "False statement", "\"else\" Statement");
 			}
 			statement = new Statement.IfElse(predicate, trueStatement, elseStatement);
 		} else {
 			statement = new Statement.If(predicate, trueStatement);
 		}
 		
 		return statement;
 	}
 	
 	/**
 	 * @see Statement.Each
 	 * @return EachStatement
 	 * @throws SyntaxException 
 	 */
 	public Statement.Each parseEachStatement() throws SyntaxException {
 		next(WaebricKeyword.EACH, "Each keyword", "\"each\"");
 		
 		Statement.Each statement = new Statement.Each();
 
 		// Parse "(" Var ":" Expression ")"
 		next(WaebricSymbol.LPARANTHESIS, "Each opening", "\"each\" \"(\" Var");
 		next(WaebricTokenSort.IDCON, "Variable", "var \":\" Expression");
 		statement.setVar(new IdCon(tokens.current()));
 		next(WaebricSymbol.COLON, "Each colon separator", "var \":\" Expression");
 		
 		try {
 			statement.setExpression(expressionParser.parseExpression());
 		} catch(SyntaxException e) {
 			reportUnexpectedToken(tokens.current(), "Each expression", "each(Var:Expression)");
 		}
 		
 		next(WaebricSymbol.RPARANTHESIS, "Each closure", "Expression \")\" Statement");
 		
 		// Parse sub-statement
 		try {
 			statement.setStatement(parseStatement());
 		} catch(SyntaxException e) {
 			reportUnexpectedToken(tokens.current(), "Each sub-statement", "each(Var:Expression) Statement");
 		}
 		
 		return statement;
 	}
 	
 	/**
 	 * @see Statement.Let
 	 * @return LetStatement
 	 * @throws SyntaxException 
 	 */
 	public Statement.Let parseLetStatement() throws SyntaxException {
 		next(WaebricKeyword.LET, "Let keyword", "\"let\"");
 
 		Statement.Let statement = new Statement.Let();
 			
 		// Parse assignments
 		do {
 			statement.addAssignment(parseAssignment());
 		} while(tokens.hasNext() && ! tokens.peek(1).getLexeme().equals(WaebricKeyword.IN));
 
 		next(WaebricKeyword.IN, "Let-in keyword", "Assignment+ \"in\" Statement*");
 		
 		// Parse sub-statements
 		while(tokens.hasNext() && ! tokens.peek(1).getLexeme().equals(WaebricKeyword.END)) {
 			statement.addStatement(parseStatement());
 		}
 		
 		next(WaebricKeyword.END, "Let-end keyword", "\"in\" Statement* \"end\"");
 		return statement;
 	}
 	
 	/**
 	 * @see Statement.Block
 	 * @return StatementCollection
 	 * @throws SyntaxException 
 	 */
 	public Statement.Block parseStatementCollection() throws SyntaxException {
 		next(WaebricSymbol.LCBRACKET, "Statement collection opening", "\"{\" Statement*");
 		
 		Statement.Block statement = new Statement.Block();
 		while(tokens.hasNext() && ! tokens.peek(1).getLexeme().equals(WaebricSymbol.RCBRACKET)) {
 			statement.addStatement(parseStatement());
 		}
 		
 		next(WaebricSymbol.RCBRACKET, "Statement collection closure", "statement* \"}\"");
 		return statement;
 	}
 	
 	/**
 	 * @see Statement.Comment
 	 * @return CommentStatement
 	 * @throws SyntaxException 
 	 */
 	public Statement.Comment parseCommentStatement() throws SyntaxException {
 		next(WaebricKeyword.COMMENT, "Comment keyword", "\"comment\"");
 		
 		Statement.Comment statement = new Statement.Comment();
 		next(WaebricTokenSort.TEXT, "Comments text", "\"comments\" Text");
 		if(WaebricScanner.isStringChars(tokens.current().getLexeme().toString())) {
 			StrCon comment = new StrCon(tokens.current().getLexeme().toString());
 			statement.setComment(comment);
 		} else {
 			reportUnexpectedToken(tokens.current(), "comments text", "\"comments\" \" Text \"");
 		}
 		
 		next(WaebricSymbol.SEMICOLON, "Comment closure \";\"", "\"comment\" StrCon \";\"");
 		return statement;
 	}
 	
 	/**
 	 * @see Statement.EchoEmbedding
 	 * @return EchoEmbedding
 	 * @throws SyntaxException 
 	 */
 	public Statement.EchoEmbedding parseEchoEmbeddingStatement() throws SyntaxException {
 		next(WaebricKeyword.ECHO, "Echo keyword", "\"echo\"");
 	
 		Statement.EchoEmbedding statement = new Statement.EchoEmbedding();
 		try {
 			statement.setEmbedding(embeddingParser.parseEmbedding());
 		} catch(SyntaxException e) {
 			reportUnexpectedToken(tokens.current(), "Embedding echo", "\"echo\" Embedding");
 		}
 		
 		next(WaebricSymbol.SEMICOLON, "Echo closure", "\"echo\" Embedding \";\"");
 		return statement;
 	}
 
 	/**
 	 * @see Statement.Echo
 	 * @return EchoExpressionStatement
 	 * @throws SyntaxException 
 	 */
 	public Statement.Echo parseEchoExpressionStatement() throws SyntaxException {
 		next(WaebricKeyword.ECHO, "Echo keyword", "\"echo\"");
 		
 		Statement.Echo statement = new Statement.Echo();
 		try {
 			statement.setExpression(expressionParser.parseExpression());
 		} catch(SyntaxException e) {
 			reportUnexpectedToken(tokens.current(), "Embedding echo", "\"echo\" Embedding");
 		}
 
 		next(WaebricSymbol.SEMICOLON, "Echo closure \";\"", "\"echo\" expression \";\"");
 		return statement;
 	}
 	
 	/**
 	 * @see Statement.CData
 	 * @return CData collection
 	 * @throws SyntaxException 
 	 */
 	public Statement.CData parseCDataStatement() throws SyntaxException {
 		next(WaebricKeyword.CDATA, "Cdata keyword", "\"cdata\"");
 		
 		Statement.CData statement = new Statement.CData();
 		try {
 			statement.setExpression(expressionParser.parseExpression());
 		} catch(SyntaxException e) {
 			reportUnexpectedToken(tokens.current(), "Embedding echo", "\"echo\" Expression");
 		}
 		
 		next(WaebricSymbol.SEMICOLON, "Cdata closure", "\"echo\" expression \";\"");
 		return statement;
 	}
 	
 	/**
 	 * @see Statement.Yield
 	 * @return YieldStatement
 	 * @throws SyntaxException 
 	 */
 	public Statement.Yield parseYieldStatement() throws SyntaxException {
 		Statement.Yield statement = new Statement.Yield();
 		next(WaebricKeyword.YIELD, "yield keyword", "\"yield\"");
 		next(WaebricSymbol.SEMICOLON, "yield closure", "\"yield\" \";\"");
 		return statement;
 	}
 
 	/**
 	 * @see Statement.AbstractMarkupStatement
 	 * @return MarkupsStatement
 	 * @throws SyntaxException 
 	 */
 	public Statement parseMarkupStatements() throws SyntaxException {
 		Markup markup = markupParser.parseMarkup(); // Retrieve (first) mark-up
 		
 		if(tokens.hasNext() && tokens.peek(1).getLexeme().equals(WaebricSymbol.SEMICOLON)) {
 			// Markup ";"
 			Statement.RegularMarkupStatement statement = new Statement.RegularMarkupStatement();
 			statement.setMarkup(markup);
 			tokens.next(); // Accept ; and jump to next token
 			return statement;
 		} else {
 			// Retrieve remaining mark-up tokens (Markup+)
 			NodeList<Markup> markups = new NodeList<Markup>();
 			markups.add(markup);
 			while(isMarkup(1, false)) {
 				markups.add(markupParser.parseMarkup());
 			}
 			
 			if(tokens.hasNext()) {
 				// Determine mark-ups statement type
 				Token peek = tokens.peek(1);
 				if(peek.getLexeme().equals(WaebricSymbol.SEMICOLON)) {
 					// Markup+ Markup ";"
 					Markup end = markups.remove(markups.size()-1);
 					Statement.MarkupMarkup statement = new Statement.MarkupMarkup(markups);
 					statement.setMarkup(end);
					next(WaebricSymbol.SEMICOLON, "Markup markup closure ;", "Markup+ Markup \";\"");
 					return statement;
 				} else if(peek.getSort() == WaebricTokenSort.EMBEDDING) {
 					MarkupEmbedding statement = new MarkupEmbedding(markups);
 					try {
 						statement.setEmbedding(embeddingParser.parseEmbedding());
 					} catch(Exception e) {
 						reportUnexpectedToken(tokens.current(), "Embedding statement", "Markup+ Embedding");
 					}
 					next(WaebricSymbol.SEMICOLON, "Markup embedding closure ;", "Markup+ Embedding \";\"");
 					return statement;
 				} else {
 					if(StatementParser.isMarkupFreeStatement(peek)) {
 						// Markup+ Statement ";"
 						MarkupStat statement = new MarkupStat(markups);
 						statement.setStatement(parseStatement());
 						return statement;
 					} else if(ExpressionParser.isExpression(peek)) {
 						// Markup+ Expression
 						MarkupExp statement = new MarkupExp(markups);
 						try {
 							statement.setExpression(expressionParser.parseExpression());
 						} catch(SyntaxException e) {
 							reportUnexpectedToken(tokens.current(), "Markup expression", "Markup+ Expression \";\"");
 						}
 						next(WaebricSymbol.SEMICOLON, "Markup expression closure ;", "Markup+ Expression \";\"");
 						return statement;
 					} else {
 						reportUnexpectedToken(peek, "Markups statement", 
 								"Markup+ { Markup, Expression, Embedding or Statement } \";\"");
 					}
 				}
 			} else {
 				reportMissingToken(tokens.current(), "Markups statement", 
 						"Markup+ { Markup, Expression, Embedding or Statement } \";\"");
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Check if next token is mark-up.
 	 * @param k Tokens look-ahead position in iterator
 	 * @param first First in mark-up chain?
 	 * @return Mark-up?
 	 */
 	public boolean isMarkup(int k, boolean first) {
 		if(tokens.hasNext() && tokens.peek(k).getSort() == WaebricTokenSort.IDCON) {
 			if(tokens.hasNext(k+2) // Parentheses can be used to force an identifier as mark-up
 					&& tokens.peek(k+1).getLexeme().equals(WaebricSymbol.LPARANTHESIS)
 					&& tokens.peek(k+2).getLexeme().equals(WaebricSymbol.RPARANTHESIS)) {
 				return true;
 			} else if(tokens.hasNext(k+1) && tokens.peek(k+1).getLexeme().equals(WaebricSymbol.PERIOD)) {
 				return false; // Only field expressions are followed by a .
 			} else if(tokens.hasNext(k+1) && tokens.peek(k+1).getLexeme().equals(WaebricSymbol.SEMICOLON)) {
 				// Semicolon marks the end of a mark-up chain, the last identifier is a variable unless it is alone
 				return first; 
 			} else {
 				// All identifiers not at tail are seen as mark-up
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Check if token is a mark-up free statement.
 	 * @param token
 	 * @return Statement?
 	 */
 	public static boolean isMarkupFreeStatement(Token token) {
 		if(token.getSort() == WaebricTokenSort.KEYWORD) {
 			return token.getLexeme().equals(WaebricKeyword.IF) 
 				|| token.getLexeme().equals(WaebricKeyword.CDATA)
 				|| token.getLexeme().equals(WaebricKeyword.COMMENT)
 				|| token.getLexeme().equals(WaebricKeyword.EACH)
 				|| token.getLexeme().equals(WaebricKeyword.LET)
 				|| token.getLexeme().equals(WaebricKeyword.YIELD)
 				|| token.getLexeme().equals(WaebricKeyword.ECHO);
 		}
 		
 		return token.getLexeme().equals(WaebricSymbol.LCBRACKET);
 	}
 
 	/**
 	 * @see Assignment
 	 * @return Assignment
 	 * @throws SyntaxException 
 	 */
 	public Assignment parseAssignment() throws SyntaxException {
 		if(tokens.hasNext(2) && tokens.peek(2).getLexeme().equals(WaebricSymbol.LPARANTHESIS)) {
 			return parseIdConAssignment();
 		} else {
 			return parseVarAssignment();
 		}
 	}
 	
 	/**
 	 * @see Assignment.VarBind
 	 * @return Assignment
 	 * @throws SyntaxException 
 	 */
 	public Assignment.VarBind parseVarAssignment() throws SyntaxException {
 		Assignment.VarBind assignment = new Assignment.VarBind();
 		
 		next(WaebricTokenSort.IDCON, "Variable binding identifier", "IdCon \"=\"");
 		assignment.setIdentifier(new IdCon(tokens.current()));
 		next(WaebricSymbol.EQUAL_SIGN, "Variable binding \"=\"", "IdCon \"=\"");
 		assignment.setExpression(expressionParser.parseExpression());
 		
 		return assignment;
 	}
 	
 	/**
 	 * @see Assignment.FuncBind
 	 * @return IdConAssignment
 	 * @throws SyntaxException 
 	 */
 	public Assignment.FuncBind parseIdConAssignment() throws SyntaxException {
 		Assignment.FuncBind assignment = new Assignment.FuncBind();
 		
 		// Parse identifier
 		next(WaebricTokenSort.IDCON, "Assignment identifier", "Identifier");
 		assignment.setIdentifier(new IdCon(tokens.current()));
 		
 		// Parse "(" { IdCon "," }* ")" "="
 		next(WaebricSymbol.LPARANTHESIS, "Function binding opening parenthesis", "\"(\" IdCon");
 		while(tokens.hasNext()) {
 			if(tokens.peek(1).getLexeme().equals(WaebricSymbol.RPARANTHESIS)) {
 				break; // End of formals found, break while
 			}
 			
 			next(WaebricTokenSort.IDCON, "Function binding identifier", "\"(\" { Identifier, \",\" }* \")\"");
 			assignment.addVariable(new IdCon(tokens.current()));
 			
 			// While not end of formals, comma separator is expected
 			if(tokens.hasNext() && ! tokens.peek(1).getLexeme().equals(WaebricSymbol.RPARANTHESIS)) {
 				next(WaebricSymbol.COMMA, "Formals separator", "Identifier \",\" Identifier");
 			}
 		}
 		next(WaebricSymbol.RPARANTHESIS, "Function binding closing parenthesis", "IdCon \")\"");
 		next(WaebricSymbol.EQUAL_SIGN, "Identifier assignment \"=\"", "Formals \"=\" Statement");
 		
 		// Parse sub-statement
 		assignment.setStatement(parseStatement());
 		return assignment;
 	}
 	
 	/**
 	 * @see Formals
 	 * @param formals
 	 * @throws SyntaxException 
 	 */
 	public Formals parseFormals() throws SyntaxException {
 		if(tokens.hasNext() && tokens.peek(1).getLexeme().equals(WaebricSymbol.LPARANTHESIS)) {
 			Formals.RegularFormal formals = new Formals.RegularFormal();
 			tokens.next(); // Accept '(' and go to next symbol
 
 			// Parse variables
 			while(tokens.hasNext()) {
 				if(tokens.peek(1).getLexeme().equals(WaebricSymbol.RPARANTHESIS)) {
 					break; // End of formals found, break while
 				}
 				
 				next(WaebricTokenSort.IDCON, "Formals identifier", "\"(\" { Identifier, \",\" }* \")\"");
 				formals.addIdentifier(new IdCon(tokens.current()));
 				
 				// While not end of formals, comma separator is expected
 				if(tokens.hasNext() && ! tokens.peek(1).getLexeme().equals(WaebricSymbol.RPARANTHESIS)) {
 					next(WaebricSymbol.COMMA, "Formals separator", "Identifier \",\" Identifier");
 				}
 			}
 			
 			// Expect right parenthesis
 			next(WaebricSymbol.RPARANTHESIS, "Formals closing parenthesis", "\")\"");
 			return formals;
 		} else {
 			return new Formals.EmptyFormal();
 		}
 	}
 
 }
