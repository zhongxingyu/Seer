 //
 //		Compiler for CS 480 - Winter 2009
 //		class Lexer
 //
 //		Written by Tim Budd, Winter term 2006
 //
 //		modified by: Cullen King <kingcu@onid.orst.edu>
 //
 
 import java.io.*;
 import java.util.regex.*;
 
 //
 //--------Lexer----------------
 //
 
 public class Lexer {
 	private PushbackReader input;
 	private String token;
 	private int tokenType;
 
 	private static final String COMMENT_REGEX = "\\{.\\}";
 	private static final String IDENT_REGEX = "[a-zA-Z]+\\w*";
 	private static final String KEYW_REGEX = "";
 	private static final String INT_REGEX = "";
 	private static final String REAL_REGEX = "";
 	private static final String STRING_REGEX = "";
 
 	public Lexer(Reader in) {
 		input = new PushbackReader(in);
 	}
 
 	private void skipWhiteSpace() throws ParseException, IOException {
 		// your code here
 	}
 
 	private int currentChar() throws ParseException {
 		int cc;
 
 		try {
 			cc = input.read();
 		} catch (IOException e) {
 			throw new ParseException(0);
 		}
 		return cc;
	}

 	public void nextLex() throws ParseException {
 		int c = currentChar();
 		token = "";
 
		if(Character.isLetterOrDigit((char) c)) {
			while(Character.isLetterOrDigit((char) c) && !Character.isWhitespace((char)c)) {
 				token = token + (char)c;
 				c = currentChar();
 			}
 
			if(!Character.isWhitespace((char)c)) {
 				try {
 					input.unread(c);
 				} catch(IOException e) {
 					System.out.println("FUCKER UP OH NOS");
 				}
 			}
		} else if(!Character.isWhitespace((char)c)) {
 			token = token + (char)c;
		} else if(Pattern.matches("\\s", Character.toString((char)c))) {
				c = currentChar();
			while(Character.isLetterOrDigit((char) c) && Pattern.matches("\\S", Character.toString((char)c))) {
				token = token + (char)c;
				c = currentChar();
			}
 		}
 
 		if(c == -1) { //end of input, toss to 7 so we can exit
 			tokenType = 7;
 		} else if(Pattern.matches(IDENT_REGEX, token)) {
 			tokenType = 1;
 		} else {
 			tokenType = 6;
 		}
 	}
 
 	static final int identifierToken = 1;
 	static final int keywordToken = 2;
 	static final int intToken = 3;
 	static final int realToken = 4;
 	static final int stringToken = 5;
 	static final int otherToken = 6;
 	static final int endOfInput = 7;
 
 	public String tokenText() {
 		return token;
 	}
 
 	public int tokenCategory() {
 		return tokenType;
 	}
 
 	public boolean isIdentifier() {
 		return tokenType == identifierToken;
 	}
 
 	public boolean match(String test) {
 		return test.equals(token);
 	}
 }
