 package fortran.lexer;
 
 import static fortran.lexer.TokenType.*;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import fortran.reader.Card;
 
 class LexerImpl extends StatementHandler {
 
 	private String stmt;
 	private int offset;
 	private char c;
 
 	/**
 	 * Creates a new Lexer based on the given stream of {@link Card}s.
 	 * 
 	 * @param cards
 	 *            to lex
 	 */
 	public LexerImpl(Iterable<Card> cards) {
 		this(cards.iterator());
 	}
 
 	/**
 	 * Creates a new Lexer based on the given {@link Card}s.
 	 * 
 	 * @param cards
 	 *            to lex
 	 */
 	public LexerImpl(Iterator<Card> cards) {
 		super(cards);
 	}
 
 	@Override
 	protected List<Token> lex(String statement) {
 		List<Token> tokens = new ArrayList<>();
 		offset = -1;
 		c = 0;
 		stmt = statement;
 		nextChar();
 
 		Token l = start();
 		// TODO better control flow
 		while (l != null) {
 			tokens.add(l);
 			nextChar();
 			l = start();
 		}
 
 		return tokens;
 	}
 
 	private Token start() {
 		while (c != 0 && Character.isWhitespace(c))
 			nextChar();
 		if (c == 0)
 			return null;
 
 		int start = offset;
 		Token t = null;
 		
 		if (Character.isLetter(c))
 			t = ident();
		else if (Character.isDigit(c) || c == '.')
 			t = constant();
 		else
 			t = misc();
 		
 		if (t == null)
 			throw new LexicalException("invalid token at " + lineNo + ":"
 					+ start + " \"" + stmt.substring(start, offset) + "\"");
 		else
 			return t;
 	}
 
 	private Token ident() {
 		int start = offset;
 		while (Character.isLetterOrDigit(nextChar()));
 		String text = stmt.substring(start, offset);
 		TokenType type = getIdentType(text);
 
 		return createToken(type, start, offset);
 	}
 
 	private TokenType getIdentType(String ident) {
 		if (Keywords.isKeyword(ident))
 			// a reserved keyword
 			return TokenType.valueOf(ident);
 		else {
 			// some identifier
 			int start = offset - ident.length();
 			/**
 			 * Functions.
 			 * 
 			 * The name of the function is 4 to 7 alphabetic or numeric
 			 * characters (not special characters), of which the last must be F
 			 * and the first must be alphabetic. Also, the first must be X if
 			 * and only if the value of the function is to be fixed point.
 			 */
 			// "if a subscripted variable has 4 or more characters in its name, the last of these must not be an F"
 			if (ident.length() >= 4 && ident.endsWith("F")) {
 				if (ident.length() > 7)
 					warn("function identifier too long (up to 7 characters allowed)", start);
 				
 				if (ident.startsWith("X"))
 					return FUNC_INT;
 				else
 					return FUNC_FLOAT;
 			} else {
 				/**
 				 * Floating Point Variables.
 				 * 
 				 * 1 to 6 alphabetic or numeric characters (not special characters)
 				 * of which the first is alphabetic but not I, J, K, L, M, or N.
 				 */
 				/**
 				 * Fixed Point Variables.
 				 * 
 				 * 1 to 6 alphabetic or numeric characters (not special characters)
 				 * of which the first is I, J, K, l, M, or N.
 				 */
 				if (ident.length() > 6)
 					warn("variable identifier too long (up to 6 characters allowed)", start);
 				if (ident.charAt(0) >= 'I' && ident.charAt(0) <= 'N')
 					return VAR_INT;
 				else
 					return VAR_FLOAT;
 			}
 		}
 	}
 
 	private Token constant() {
 		int start = offset;
 		while (Character.isDigit(c))
 			nextChar();
 		
 		if (c != '.') {
 			/**
 			 * Fixed Point Constants.
 			 * 
 			 * 1 to 5 decimal digits. A preceding + or - sign is optional. The
 			 * magnitude of the constant must be less than 32768.
 			 */
 			String text = stmt.substring(start, offset);
 			
 			if (text.length() > 5)
 				warn("Fixed Point Constants are only specified up to 5 digits", start);
 			else if (Integer.valueOf(text) >= 32768)
 				warn("The magnitude of Fixed Point Constants must be less than 32768", start);
 			
 			return createToken(CONST_INT, start, offset);
 		} else {
 			/**
 			 * Floating Point Constants.
 			 * 
 			 * Any number of decimal digits, with a decimal point at the beginning,
 			 * at the end, or between two digits. A preceding + or - sign is
 			 * optional. A decimal exponent preceded by an E may follow.
 			 */
 			// skip '.'
 			nextChar();
 			// fractional part
 			while (Character.isDigit(nextChar()));
 			// exponent?
 			if (c == 'E') {
 				nextChar();
 				if (c == '+' || c == '-')
 					nextChar();
 				while (Character.isDigit(nextChar()));
 			}
 			return createToken(CONST_FLOAT, start, offset);
 		}
 	}
 
 	private Token misc() {
 		String asString = Character.toString(c);
 		switch (c) {
 			case '(':
 				return createToken(PAREN1, asString);
 			case ')':
 				return createToken(PAREN2, asString);
 			case ',':
 				return createToken(COMMA, asString);
 			case '=':
 				return createToken(EQUALS, asString);
 			case '+':
 				return createToken(PLUS, asString);
 			case '-':
 				return createToken(MINUS, asString);
 			case '*':
 				if (peekChar() == '*') {
 					Token t = createToken(EXP, "**");
 					nextChar(); 
 					return t;
 				} else
 					return createToken(MUL, asString);
 			case '/':
 				return createToken(DIV, asString);
 			default:
 				return null;
 		}
 	}
 
 	private char nextChar() {
 		if (offset+1 < stmt.length()) {
 			// load next char and increment offset to its position
 			c = stmt.charAt(offset + 1);
 			offset++;
 			return c;
 		} else {
 			offset = stmt.length();
 			return c = 0;
 		}
 	}
 
 	private char peekChar() {
 		if (offset+1 < stmt.length())
 			return stmt.charAt(offset + 1);
 		else
 			return 0;
 	}
 
 	private Token createToken(TokenType type, String text) {
 		return new TokenImpl(type, lineNo, Card.STATEMENT_OFFSET + offset, text);
 	}
 
 	private Token createToken(TokenType type, int start, int end) {
 		String text = stmt.substring(start, end);
 		return new TokenImpl(type, lineNo, Card.STATEMENT_OFFSET + start, text);
 	}
 }
