 package org.cwi.waebric.scanner;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cwi.waebric.WaebricKeyword;
 import org.cwi.waebric.WaebricSymbol;
 import org.cwi.waebric.scanner.token.Token;
 import org.cwi.waebric.scanner.token.TokenIterator;
 import org.cwi.waebric.scanner.token.WaebricTokenSort;
 
 /**
  * The lexical analyzer, also known as a scanner, reads an input character stream
  * from which it generates a stream of tokens. During this "tokenization" process
  * layout and comments characters are removed to simplify and optimize parsing.
  * 
  * @author Jeroen van Schagen
  * @date 18-05-2009
  */
 public class WaebricScanner implements Iterable<Token> {
 
 	private final StreamTokenizer tokenizer;
 	
 	/**
 	 * Currently processed tokens
 	 */
 	private List<Token> tokens;
 	
 	/**
 	 * Currently occurred exceptions, stored instead of thrown to detect multiple 
 	 * exceptions at once.
 	 */
 	private List<LexicalException> exceptions;
 	
 	/**
 	 * Current character being processed
 	 */
 	private int current;
 	
 	/**
 	 * Construct scanner
 	 * 
 	 * @param reader Input character stream
 	 */
 	public WaebricScanner(Reader reader) {
 		try {
 			tokenizer = new StreamTokenizer(reader); 
 		} catch(IOException e) {
 			throw new InternalError(); // Should never occur
 		}
 		
 		tokens = new ArrayList<Token>();
 		exceptions = new ArrayList<LexicalException>();
 	}
 
 	/**
 	 * Convert character stream in token stream
 	 * 
 	 * @return List of scanner exceptions
 	 * @throws IOException Fired by next token procedure in stream tokenizer.
 	 * @see java.io.StreamTokenizer
 	 */
 	public List<LexicalException> tokenizeStream() {
 		tokens.clear(); exceptions.clear(); // Reset
 		
 		// Scan and store tokens
 		next();
 		while(current != StreamTokenizer.END_OF_FILE) {
 			switch(current) {
 				case StreamTokenizer.WORD:
 					tokenizeWord();
 					break;
 				case StreamTokenizer.NUMBER:
 					tokenizeNumber();
 					break;
 				case StreamTokenizer.CHARACTER:
 					if(tokenizer.getCharacterValue() == '\'') {
 						tokenizeSymbol();
 					} else if(tokenizer.getCharacterValue() == '"') {
 						tokenizeQuote();
 					} else {
 						tokenizeCharacter();
 					} break;
 				case StreamTokenizer.LAYOUT: 
 					next();
 					break; // Layout tokens will not be parsed
 				case StreamTokenizer.COMMENT: 
 					next();
 					break; // Comment tokens will not be parsed
 			}
 		}
 
 		return exceptions;
 	}
 	
 	/**
 	 * Retrieve next character and log any exceptions that occur.
 	 * @return
 	 */
 	private boolean next() {
 		try {
 			current = tokenizer.nextToken();
 			return true;
 		} catch (IOException e) {
 			exceptions.add(new LexicalException(e));
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param exceptions
 	 * @throws IOException
 	 */
 	private void tokenizeWord() {
 		int lineno = tokenizer.getTokenLineNumber();
 		int charno = tokenizer.getTokenCharacterNumber();
 		
 		String word = "";
 		while(current == StreamTokenizer.WORD || current == StreamTokenizer.NUMBER) {
 			word += tokenizer.getStringValue();
 			next();
 		}
 		
 		if(isKeyword(word)) {
 			WaebricKeyword type = WaebricKeyword.valueOf(word.toUpperCase());
 			Token keyword = new Token(type, WaebricTokenSort.KEYWORD, lineno, charno);
 			tokens.add(keyword);
 		} else {
 			Token identifier = new Token(word, WaebricTokenSort.IDCON, lineno, charno);
 			tokens.add(identifier);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param exceptions
 	 * @throws IOException
 	 */
 	private void tokenizeNumber() {
 		Token number = new Token(
 				tokenizer.getIntegerValue(), WaebricTokenSort.NATCON, 
 				tokenizer.getTokenLineNumber(), tokenizer.getTokenCharacterNumber()
 			); // Construct number token
 		
 		tokens.add(number);
 		next();
 	}
 	
 	/**
 	 * 
 	 * @param exceptions
 	 * @throws IOException
 	 */
 	private void tokenizeCharacter() {
 		Token character = new Token(
 				tokenizer.getCharacterValue(), WaebricTokenSort.CHARACTER, 
 				tokenizer.getTokenLineNumber(), tokenizer.getTokenCharacterNumber()
 			); // Construct token
 		
 		tokens.add(character);
 		next();
 	}
 	
 	/**
 	 * " * "
 	 * 
 	 * @param exceptions
 	 * @throws IOException
 	 */
 	private void tokenizeQuote() {
 		int lineno = tokenizer.getTokenLineNumber();
 		int charno = tokenizer.getTokenCharacterNumber();
 		
 		next(); // Skip " opening character
 		
 		String data = "";
 		while(tokenizer.getCharacterValue() != '"') {
 			if(current < 0) {
 				// End of file found before closing ", store as separate tokens
 				WaebricScanner scanner = new WaebricScanner(new StringReader(data));
 				exceptions.addAll(scanner.tokenizeStream());
 				
 				// Attach opening double quote as character
 				tokens.add(new Token(WaebricSymbol.DQUOTE, WaebricTokenSort.CHARACTER, lineno, charno));
 				
 				// Attach sub-tokens with absolute positions
 				for(Token token: scanner.getTokens()) {
 					token.setLine(lineno + token.getLine() - 1);
 					token.setCharacter(charno + token.getCharacter());
 					tokens.add(token);
 				}
 				
 				return; // Quit scanning quote, as it is not a quote
 			}
 			
 			data += tokenizer.toString(); // Build quote data
 			next(); // Retrieve next token
 		}
 
 		next(); // Skip " closure character
 		Token quote = new Token(data, WaebricTokenSort.QUOTE, lineno, charno);
 		tokens.add(quote);
 	}
 	
 	/**
 	 * ' SymbolChar*
 	 * 
 	 * @param exceptions
 	 * @throws IOException
 	 */
 	private void tokenizeSymbol() {
 		int lineno = tokenizer.getTokenLineNumber();
 		int charno = tokenizer.getTokenCharacterNumber();
 		
 		next(); // Skip ' opening character
 		
 		String data = "";
 		while(isSymbolChars(tokenizer.toString())) {
 			data += tokenizer.toString();
 			next(); // Retrieve next char
 		}
 		
 		Token symbol = new Token(data, WaebricTokenSort.SYMBOLCON, lineno, charno);
 		tokens.add(symbol);
 	}
 
 	/**
 	 * Retrieve token
 	 * 
 	 * @param index Token index in structured text
 	 * @return token
 	 */
 	public Token getToken(int index) {
 		return tokens.get(index);
 	}
 	
 	/**
 	 * Retrieve amount of tokens
 	 * 
 	 * @return size
 	 */
 	public int getSize() {
 		return tokens.size();
 	}
 	
 	/**
 	 * Retrieve token list
 	 * 
 	 * @return
 	 */
 	public List<Token> getTokens() {
 		return tokens;
 	}
 	
 	/**
 	 * Retrieve token iterator
 	 * 
 	 * @return iterator
 	 */
 	public TokenIterator iterator() {
 		return new TokenIterator(tokens);
 	}
 	
 	/**
 	 * 
 	 * @param lexeme
 	 * @return
 	 */
 	public static boolean isSymbolChars(String lexeme) {
 		if(lexeme == null) { return false; }
 		char chars[] = lexeme.toCharArray();
 
 		for(char c: chars) {
 			if(! isSymbolChar(c)) { return false; }
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Check if character is a symbol.
 	 * 
 	 * @param c
 	 * @return 
 	 */
 	private static boolean isSymbolChar(int c) {
		return c > 31 && c < 127 && c != ' ' && c != ';' && c != ',' && c != '>';
 	}
 
 	/**
 	 * Check if lexeme is a keyword.
 	 * 
 	 * @param lexeme Token value
 	 * @return 
 	 */
 	public boolean isKeyword(String lexeme) {
 		try {
 			// Literal should be in enumeration
 			WaebricKeyword literal = WaebricKeyword.valueOf(lexeme.toUpperCase());
 			return literal != null;
 		} catch(IllegalArgumentException e) {
 			// Enumeration does not exists
 			return false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param lexeme
 	 * @return
 	 */
 	public static boolean isTextChars(String lexeme) {
 		if(lexeme == null) { return false; }
 		char chars[] = lexeme.toCharArray();
 		
 		for(int i = 0; i < chars.length; i++) {
 			char c = chars[i]; // Retrieve current character
 			if(! isTextChar(c)) {
 				// Allow "\\&" "\\""
 				if(c == '&' || c == '"') {
 					if(i > 0) {
 						char previous = chars[i-1];
 						if(previous == '\\') {
 							i++; // Skip checking & or ' and accept
 						} else {
 							return false; // Incorrect occurrence of & or " character
 						}
 					} else {
 						return false; // Incorrect occurrence of & or " character
 					}
 				} else {
 					return false; // Incorrect character
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static boolean isTextChar(char c) {
 		return c > 31 && c < 128 && c != '<' && c != '&' && c != '"' 
 			|| c == '\n' || c == '\t' || c == '\r';
 	}
 	
 	/**
 	 * 
 	 * @param lexeme
 	 * @return
 	 */
 	public static boolean isStringChars(String lexeme) {
 		if(lexeme == null) { return false; }
 		char chars[] = lexeme.toCharArray();
 		
 		for(int i = 0; i < chars.length; i++) {
 			char c = chars[i]; // Retrieve current character
 			if(! isStringChar(c)) {
 				// Allow "\\n" "\\t" "\\\"" "\\\\"
 				if(c == '\\') {
 					if(i+1 < chars.length) {
 						char peek = chars[i+1];
 						if(peek == 'n' || peek == 't' || peek == '"' || peek == '\\') {
 							i++; // Check checking \\ and accept
 						} else {
 							return false; // Invalid occurrence of '\\'
 						}
 					} else {
 						return false; // Invalid occurrence of '\\'
 					}
 				} else {
 					return false; // Invalid string symbol found
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static boolean isStringChar(char c) {
 		return c > 31 && c != '\n' && c != '\t' && c != '"' && c != '\\';
 	}
 
 }
