 // vi:ai:noet sta sw=4 ts=4 sts=0
 package edu.kit.pp.minijava;
 
 import edu.kit.pp.minijava.ast.*;
 import edu.kit.pp.minijava.tokens.*;
 import java.io.IOException;
 import java.util.HashMap;
 // TODO error() method for throwing Exception
 // TODO alle expressions durchgehen und tokens so speziell wie möglich wählen
 
 public class Parser {
 
 	static int _currentLookAhead = 0;
 
 	public static class UnexpectedTokenException extends RuntimeException {
 		private Token _token;
 		private int _line;
 		private int _column;
 		private String _expectedToken;
 
 		public UnexpectedTokenException(Token token, int line, int column) {
 			this(token, line, column, null);
 		}
 
 		public UnexpectedTokenException(Token token, int line, int column, String expectedToken) {
 			_token = token;
 			_line = line;
 			_column = column;
 			_expectedToken = expectedToken;
 			if (expectedToken != null && expectedToken.equals("EOF")) {
 				_column--;
 			}
 		}
 
 		public Token getToken() {
 			return _token;
 		}
 
 		@Override
 		public String toString() {
 			String result = "Unexpected token at line " + _line + ", column " + _column + ":";
 			if (null != _expectedToken)
 				result += " Expected '" + _expectedToken + "', found '" + _token + "'.";
 			else
 				result += " Found '" + _token + "'.";
 
 
 			return result;
 		}
 	}
 
 	private static class ParserFunction {
 		public static interface ParseExpressionPrefixFunction {
 			Expression parse();
 		}
 		public static interface ParseExpressionInfixFunction {
 			Expression parse(Expression left);
 		}
 		private static abstract class ParseExpressionFunction {
 			protected Parser _parser;
 			protected String[] _operators;
 			protected int _precedence;
 			
 			public ParseExpressionFunction(Parser parser, String[] operators, int precedence) {
 				_parser = parser;
 				_operators = operators;
 				_precedence = precedence;
 			}
 		}
 		public static class ParseBinaryExpressionInfixFunction extends ParseExpressionFunction implements ParseExpressionInfixFunction {
 			public ParseBinaryExpressionInfixFunction(Parser parser, String[] operators, int precedence) {
 				super(parser, operators, precedence);
 			}
 
 			@Override
 			public Expression parse(Expression left) {
 				String o = null;
 				for (String operator : _operators) {
 					if (_parser.acceptToken(operator)) {
 						o = operator;
 						break;
 					}
 				}
 				if (o == null)
 					throw _parser.error();
 				Token t = _parser.expectToken(o);
 				Expression right = _parser.parseExpression(_precedence + 1);
 				return new BinaryExpression(t, left, right);
 			}
 		}
 		public static class ParseUnaryExpressionPrefixFunction extends ParseExpressionFunction implements ParseExpressionPrefixFunction {
 			public ParseUnaryExpressionPrefixFunction(Parser parser, String[] operators, int precedence) {
 				super(parser, operators, precedence);
 			}
 			
 			@Override
 			public Expression parse() {
 				String o = null;
 				for (String operator : _operators) {
 					if (_parser.acceptToken(operator)) {
 						o = operator;
 						break;
 					}
 				}
 				if (o == null)
 					throw _parser.error();
 				Token t = _parser.expectToken(o);
 				Expression right = _parser.parseExpression(_precedence);
 				return new UnaryExpression(t, right);
 			}
 		}
 
 		public ParseExpressionPrefixFunction _parseExpressionPrefixFunction;
 		public int _precedence;
 		public ParseExpressionInfixFunction _parseExpressionInfixFunction;
 
 		ParserFunction(int p, ParseExpressionPrefixFunction f, ParseExpressionInfixFunction inf) {
 			_precedence=p;
 			_parseExpressionPrefixFunction=f;
 			_parseExpressionInfixFunction=inf;
 		}
 	}
 
 	private PeekingLexer _lexer;
 	private static final int LOOK_AHEAD_SIZE=4;
 	private HashMap<String, ParserFunction> _expressionParsers;
 
 	private void initalizeExpressionParserMap(){
 		_expressionParsers=new HashMap<String, ParserFunction>();
 		_expressionParsers.put("=", new ParserFunction(1, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"="}, 1 - 1)));
 		_expressionParsers.put("||", new ParserFunction(2, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"||"}, 2)));
 		_expressionParsers.put("&&", new ParserFunction(3, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"&&"}, 3)));
 		ParserFunction ef = new ParserFunction(4, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"==", "!="}, 4));
 		_expressionParsers.put("==", ef);
 		_expressionParsers.put("!=", ef);
 		ParserFunction cf = new ParserFunction(5, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"<", "<=", ">", ">="}, 5));
 		_expressionParsers.put("<", cf);
 		_expressionParsers.put("<=", cf);
 		_expressionParsers.put(">", cf);
 		_expressionParsers.put(">=", cf);
 		ParserFunction af = new ParserFunction(
 				6,
 				new ParserFunction.ParseUnaryExpressionPrefixFunction(this, new String[] {"+", "-"}, 8),
 				new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"+", "-"}, 6));
 		_expressionParsers.put("+", af);
 		_expressionParsers.put("-", af);
 		ParserFunction mf = new ParserFunction(7, null, new ParserFunction.ParseBinaryExpressionInfixFunction(this, new String[] {"*", "/", "%"}, 7));
 		_expressionParsers.put("*", mf);
 		_expressionParsers.put("/", mf);
 		_expressionParsers.put("%", mf);
 		_expressionParsers.put("!", new ParserFunction(
 				8,
 				new ParserFunction.ParseUnaryExpressionPrefixFunction(this, new String[] {"!"}, 8),
 				null));
 	}
 
 	public Parser(Lexer lexer) throws IOException {
 		_lexer = new PeekingLexer(lexer, LOOK_AHEAD_SIZE);
 
 		initalizeExpressionParserMap();
 	}
 
 	private Token consumeToken() {
 		try {
 			_currentLookAhead = 0;
 			return _lexer.next();
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	private UnexpectedTokenException error() {
 		return new UnexpectedTokenException(getCurrentToken(), getLine(), getColumn());
 	}
 
 	private UnexpectedTokenException error(String expectedToken) {
 		return new UnexpectedTokenException(getCurrentToken(), getLine(), getColumn(), expectedToken);
 	}
 
 	private Token getCurrentToken() {
 		_currentLookAhead = 1;
 		return _lexer.peek(0);
 	}
 
 	private int getLine() {
 		return _lexer.getLine(_currentLookAhead);
 	}
 
 	private int getColumn() {
 		return _lexer.getColumn(_currentLookAhead);
 	}
 
 	private boolean acceptToken(String s, int pos){
 		_currentLookAhead = pos + 1;
 		return _lexer.peek(pos).getValue().equals(s);
 	}
 
 	private boolean acceptToken(String s){
 		return acceptToken(s, 0);
 	}
 
 	private boolean acceptIntegerLiteral() {
 		return getCurrentToken() instanceof IntegerLiteral;
 	}
 
 	private boolean acceptIdentifier() {
 		return acceptIdentifier(0);
 	}
 	
 	private boolean acceptIdentifier(int pos) {
 		return _lexer.peek(pos) instanceof Identifier;
 	}
 
 	private boolean acceptPrimaryExpression() {
 		return acceptToken("null") || acceptToken("false") || acceptToken("true") ||
 				acceptIdentifier() || acceptIntegerLiteral() || acceptToken("this") ||
 				acceptToken("(") || acceptToken("new");
 	}
 
 	private Token expectToken(String s) throws UnexpectedTokenException {
 		if (!acceptToken(s))
 			throw error(s);
 		return consumeToken();
 	}
 
 	private IntegerLiteral expectIntegerLiteral() throws UnexpectedTokenException {
 		if (!(getCurrentToken() instanceof IntegerLiteral))
 			throw error("INTEGER");
 		return (IntegerLiteral)consumeToken();
 	}
 
 	private Identifier expectIdentifier() throws UnexpectedTokenException {
 		if (!(getCurrentToken() instanceof Identifier))
 			throw error("IDENTIFIER");
 		return (Identifier)consumeToken();
 	}
 
 	private Token expectEOF() {
 		if (!getCurrentToken().isEof())
 			throw error("EOF");
 		return consumeToken();
 	}
 
 	public Program parseProgram() throws UnexpectedTokenException {
 		Program p = new Program();
 
 		while (acceptToken("class")) {
 			p.add(parseClass());
 		}
 
 		expectEOF();
 
 		return p;
 	}
 
 	private ClassDeclaration parseClass() throws UnexpectedTokenException {
 		expectToken("class");
 		Identifier name = expectIdentifier();
 		expectToken("{");
 		ClassDeclaration cd = new ClassDeclaration(name);
 		while (acceptToken("public")) {
 			cd.add(parseClassMember());
 		}
 		expectToken("}");
 		return cd;
 	}
 
 	private ClassMember parseClassMember() {
 		if (acceptToken("public")) {
 			if (acceptToken("static", 1)) {
 				return parseMainMethod();
 			}
 			else {
 				return parseFieldOrMethod();
 			}
 		}
 		else {
 			throw error();
 		}
 	}
 
 	private ClassMember parseFieldOrMethod() throws UnexpectedTokenException {
 		expectToken("public");
 		Type t = parseType();
 		Identifier id = expectIdentifier();
 		// Field
 		if(acceptToken(";")) {
 			expectToken(";");
 			return new Field(t, id);
 		}
 		// Method
 		expectToken("(");
 		Parameters p = null;
 		if (!acceptToken(")"))
 			p = parseParameters();
 		expectToken(")");
 		Block b = parseBlock();
 		return new Method(t, id, b, p);
 	}
 
 	private MainMethod parseMainMethod() {
 		expectToken("public");
 		expectToken("static");
 		expectToken("void");
 		Identifier name = expectIdentifier();
 		expectToken("(");
 		expectToken("String");
 		expectToken("[");
 		expectToken("]");
 		Identifier variableName = expectIdentifier();
 		expectToken(")");
 		Block b = parseBlock();
 		return new MainMethod(name, variableName, b);
 	}
 
 	// TODO So richtig?
 	private Parameters parseParameters() {
 		Parameters p = new Parameters();
 		p.add(parseParameter());
 		while (acceptToken(",")) {
 			expectToken(",");
 			p.add(parseParameter());
 		}
 		return p;
 	}
 
 	private Parameter parseParameter() {
 		Type t = parseType();
 		Identifier name = expectIdentifier();
 		return new Parameter(t, name);
 	}
 
 	private Type parseType() {
 		BasicType t = parseBasicType();
 		int dimension = 0;
 		while (acceptToken("[")) {
 			expectToken("[");
 			expectToken("]");
 			dimension += 1;
 		}
 		return new Type(t, dimension);
 	}
 
 	private BasicType parseBasicType() {
 		if (acceptToken("int"))
 			return new BasicType(expectToken("int"));
 		else if (acceptToken("boolean"))
 			return new BasicType(expectToken("boolean"));
 		else if (acceptToken("void"))
 			return new BasicType(expectToken("void"));
 		else if (acceptIdentifier())
 			return new BasicType(expectIdentifier());
 		throw error("TYPE");
 	}
 
 	private Statement parseStatement() {
 		if (acceptToken("{"))
 			return parseBlock();
 		else if (acceptToken(";"))
 			return parseEmptyStatement();
 		else if (acceptToken("if"))
 			return parseIfStatement();
 		else if (acceptToken("while"))
 			return parseWhileStatement();
 		else if (acceptToken("return"))
 			return parseReturnStatement();
 		else if (acceptPrimaryExpression()) // we could also just ignore the check
 			return parseExpressionStatement();
 
 		throw error();
 	}
 
 	// TODO so korrekt?
 	private Block parseBlock() {
 		expectToken("{");
 		Block b = new Block();
 		while(!acceptToken("}")) {
 			b.add(parseBlockStatement());
 		}
 		expectToken("}");
 		return b;
 	}
 
 	private BlockStatement parseBlockStatement() {
 		if (acceptToken("int") || acceptToken("boolean") || acceptToken("void") ||
 				acceptIdentifier() && (acceptIdentifier(1) || (acceptToken("[", 1) && acceptToken("]", 2)))) {
 			return parseLocalVariableDeclarationStatement();
 		}
 		/*if (acceptToken("{") || acceptToken(";") || acceptToken("if") || acceptToken("while") || acceptToken("return")) {
 			return parseStatement();
 		}*/
 		return parseStatement();
 	}
 
 	private LocalVariableDeclarationStatement parseLocalVariableDeclarationStatement() {
 		Type t = parseType();
 		Identifier name = expectIdentifier();
 		Expression e = null;
 		if (acceptToken("=")) {
 			expectToken("=");
 			e = parseExpression();
 		}
 		expectToken(";");
 		return new LocalVariableDeclarationStatement(t, name, e);
 	}
 
 	private EmptyStatement parseEmptyStatement() {
 		expectToken(";");
 		return new EmptyStatement();
 	}
 
 	private WhileStatement parseWhileStatement() {
 		expectToken("while");
 		expectToken("(");
 		Expression e = parseExpression();
 		expectToken(")");
 		Statement s = parseStatement();
 		return new WhileStatement(e, s);
 	}
 
 	private IfStatement parseIfStatement() {
 		expectToken("if");
 		expectToken("(");
 		Expression e = parseExpression();
 		expectToken(")");
 		Statement s1 = parseStatement();
 		Statement s2 = null;
 		if (acceptToken("else")) {
 			expectToken("else");
 			s2 = parseStatement();
 		}
 		return new IfStatement(e, s1, s2);
 	}
 
 	private ExpressionStatement parseExpressionStatement() {
 		Expression e = parseExpression();
 		expectToken(";");
 		return new ExpressionStatement(e);
 	}
 
 	private ReturnStatement parseReturnStatement() {
 		expectToken("return");
 		if (acceptToken(";")) {
 			expectToken(";");
 			return new ReturnStatement(null);
 		}
 		else if (acceptPrimaryExpression()) {
 			return new ReturnStatement(parseExpression());
 		}
 		else
 			throw error();
 	}
 
 	public Expression parseExpression() {
 		return parseExpression(0);
 	}
 
 	public Expression parseExpression(int precedence) {
 		ParserFunction pf = _expressionParsers.get(getCurrentToken().toString());
 		Expression left;
 
 		if (pf != null && pf._parseExpressionPrefixFunction != null)
 			left = pf._parseExpressionPrefixFunction.parse();
 		else
 			left = parsePostfixExpression();
 
 		while (true) {
 			pf = _expressionParsers.get(getCurrentToken().toString());
 
 			if (pf == null)
 				break;// throw new UnexpectedTokenException(getCurrentToken());
 
 			if (pf._parseExpressionInfixFunction == null || pf._precedence < precedence)
 				break;
 
 			left = pf._parseExpressionInfixFunction.parse(left);
 		}
 
 		return left;
 	}
 
 	private PostfixExpression parsePostfixExpression() {
 		PostfixExpression e = new PostfixExpression(parsePrimaryExpression());
 		while (acceptToken(".") || acceptToken("[")) {
 			e.add(parsePostfixOp());
 		}
 		return e;
 	}
 	
 	private PostfixOp parsePostfixOp() {
 		if (acceptToken(".")) {
 			if (acceptToken("(", 2)) {
 				return parseMethodInvocation();
 			} else {
 				return parseFieldAccess();
 			}
 		}
 		else if (acceptToken("[")) {
 			return parseArrayAccess();
 		}
 		throw error();
 	}
 
 	private MethodInvocation parseMethodInvocation() {
 		expectToken(".");
 		Identifier name = expectIdentifier();
 		expectToken("(");
 		Arguments a = parseArguments();
 		expectToken(")");
 		return new MethodInvocation(name, a);
 	}
 
 	private FieldAccess parseFieldAccess() {
 		expectToken(".");
 		Identifier name = expectIdentifier();
 		return new FieldAccess(name);
 	}
 
 	private ArrayAccess parseArrayAccess() {
 		expectToken("[");
 		Expression e = parseExpression();
 		expectToken("]");
 		return new ArrayAccess(e);
 	}
 
 	private Arguments parseArguments() {
 		Arguments a = new Arguments();
 		while (!acceptToken(")")) {
 			a.add(parseExpression());
 			if (acceptToken(")"))
 				break;
 			else
 				expectToken(",");
 		}
 		return a;
 	}
 
 	private Expression parsePrimaryExpression() {
 		if (acceptToken("null"))
 			return new PrimaryExpression(expectToken("null"));
 		else if (acceptToken("false"))
 			return new PrimaryExpression(expectToken("false"));
 		else if (acceptToken("true"))
 			return new PrimaryExpression(expectToken("true"));
 		else if (acceptIntegerLiteral())
 			return new PrimaryExpression(expectIntegerLiteral());
 		else if (acceptIdentifier()) {
 			if (acceptToken("(", 1)) {
 				return parseLocalMethodInvocation();
 			}
 			return new PrimaryExpression(expectIdentifier());
 		}
 		else if (acceptToken("this"))
 			return new PrimaryExpression(expectToken("this"));
 		else if (acceptToken("(")) {
 			expectToken("(");
 			Expression e = parseExpression();
 			expectToken(")");
 			return e;
 		}
 		else if (acceptToken("new")) {
 			if (acceptToken("(", 2)) {
 				return parseNewObjectExpression();
 			}
 			else if (acceptToken("[", 2)) {
 				return parseNewArrayExpression();
 			}
 		}
 
 		throw error();
 	}
 
 	private NewArrayExpression parseNewArrayExpression() {
 		expectToken("new");
 		BasicType bt = parseBasicType();
 		expectToken("[");
 		Expression e = parseExpression();
 		expectToken("]");
 
 		int fieldCount = 1;
		while (acceptToken("[")) {
 			expectToken("[");
 			expectToken("]");
 			fieldCount += 1;
 		}
 
 		return new NewArrayExpression(bt, e, fieldCount);
 	}
 
 	private NewObjectExpression parseNewObjectExpression() {
 		expectToken("new");
 		Identifier className = expectIdentifier();
 		expectToken("(");
 		expectToken(")");
 		return new NewObjectExpression(className);
 	}
 
 	private LocalMethodInvocationExpression parseLocalMethodInvocation() {
 		Token t = expectIdentifier();
 		expectToken("(");
 		Arguments a = parseArguments();
 		expectToken(")");
 		return new LocalMethodInvocationExpression(t, a);
 	}
 }
