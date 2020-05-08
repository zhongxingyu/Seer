 package swp_compiler_ss13.fuc.lexer;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import swp_compiler_ss13.common.lexer.BoolToken;
 import swp_compiler_ss13.common.lexer.Lexer;
 import swp_compiler_ss13.common.lexer.NumToken;
 import swp_compiler_ss13.common.lexer.RealToken;
 import swp_compiler_ss13.common.lexer.Token;
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.fuc.lexer.token.BoolTokenImpl;
 import swp_compiler_ss13.fuc.lexer.token.NumTokenImpl;
 import swp_compiler_ss13.fuc.lexer.token.RealTokenImpl;
 import swp_compiler_ss13.fuc.lexer.token.TokenImpl;
 
 /**
  * Implementation of the interface {@link Lexer}
  * 
  * @author "Thomas Benndorf"
  * 
  */
 public class LexerImpl implements Lexer {
 
 	private Token actualToken;
 	private ArrayList<String> convertedLines;
 	private String actualLineValue;
 	private String actualTokenValue;
 	private TokenType actualTokenType;
 	private Integer actualLine;
 	private Integer actualColumn;
 	private boolean isEOF;
 	private final static Pattern NEXT_CHARACTER = Pattern.compile("[^\\s]+");
 	private final static Pattern NEXT_WHITESPACE = Pattern.compile("\\s");
 
 	/**
 	 * Default constructor initializes class variables
 	 */
 	public LexerImpl() {
 		this.init();
 	}
 
 	/**
 	 * Method sets an {@link InputStream} for the lexer and splits it line by
 	 * line into an {@link ArrayList} while removing the tabs with whitespaces
 	 */
 	@Override
 	public void setSourceStream(InputStream stream) {
 
 		this.init();
 		this.convertedLines = new ArrayList<>();
 		Scanner scanner = new Scanner(stream, "UTF-8");
 
 		while (scanner.hasNext()) {
 
 			this.convertedLines.add(scanner.nextLine());
 
 		}
 
 		scanner.close();
 
 		/*
 		 * check if the input is empty
 		 */
 		if (this.convertedLines.size() == 0) {
 
 			this.isEOF = true;
 			this.actualTokenValue = "$";
 			this.actualTokenType = TokenType.EOF;
 
 		} else {
 
 			/*
 			 * get the value of the first line
 			 */
 			this.actualLineValue = this.convertedLines.get(this.actualLine - 1);
 			this.isEOF = false;
 
 		}
 
 	}
 
 	/**
 	 * Method to initialize variables when getting new input
 	 */
 	private void init() {
 
 		this.actualTokenValue = "";
 		this.actualTokenType = TokenType.EOF;
 		this.actualLine = 1;
 		this.actualColumn = 1;
 		this.actualToken = null;
 		this.isEOF = true;
 
 	}
 
 	/**
 	 * Method read the next token from the input and returns it with his value,
 	 * type, line of code and column of code
 	 * 
 	 * @return {@link NumToken}: token has the {@link TokenType}
 	 *         <code>NUM</code><br>
 	 *         {@link RealToken}: token has the {@link TokenType}
 	 *         <code>REAL</code><br>
 	 *         {@link BoolToken}: token has the {@link TokenType}
 	 *         <code>TRUE</code> or <code>FALSE</code><br>
 	 *         {@link Token}: otherwise<br>
 	 */
 	@Override
 	public Token getNextToken() {
 
 		/*
 		 * check if EOF is already reached
 		 */
 		if (!this.isEOF) {
 
 			this.abstractToken();
 
 		}
 
 		/*
 		 * analyse the actual type of token
 		 */
 		switch (this.actualTokenType.name()) {
 
 		case "NUM":
 			this.actualToken = new NumTokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 			break;
 
 		case "REAL":
 			this.actualToken = new RealTokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 			break;
 
 		case "TRUE":
 			this.actualToken = new BoolTokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 			break;
 
 		case "FALSE":
 			this.actualToken = new BoolTokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 			break;
 
 		default:
 			this.actualToken = new TokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 			break;
 
 		}
 
 		return this.actualToken;
 
 	}
 
 	/**
 	 * Method to get the value, the actual line and the actual column of the
 	 * next token
 	 */
 	private void abstractToken() {
 
 		/*
 		 * increase actual line if necessary
 		 */
 		if (this.checkLineOfCode()) {
 
 			this.increaseLineOfCode();
 
 		}
 
 		if (!this.isEOF) {
 
 			/*
 			 * find the next character in line which is not a whitespace
 			 */
 			Matcher matchNextCharacter = NEXT_CHARACTER
 					.matcher(this.actualLineValue);
 
 			while (matchNextCharacter.find()) {
 
 				/*
 				 * calculate the column for the next token value
 				 */
 				this.actualColumn += this.actualTokenValue.length()
 						+ matchNextCharacter.start();
 				break;
 
 			}
 
 			/*
 			 * remove every whitespace in front of the next token value
 			 */
 			this.actualLineValue = this.actualLineValue.replaceAll("^\\s+", "");
 
 			/*
 			 * check if the next token is a string (starts with an apostrophe),
 			 * a comment or another type
 			 */
 			if (this.actualLineValue.startsWith("\"")
 					&& this.actualLineValue.indexOf("\"", 1) != -1) {
 
 				int indexOfNextApostrophe = this.actualLineValue.indexOf("\"",
 						1);
 
 				/*
 				 * check if string is finished in this line
 				 */
 				if (indexOfNextApostrophe == -1) {
 
 					/*
 					 * multi line strings are not yet supported
 					 */
 
 				} else {
 
 					/*
 					 * loop as long as the next unescaped apostrophe is found
 					 */
 					while (!this.checkEscapeStatus(indexOfNextApostrophe)) {
 
 						indexOfNextApostrophe = this.actualLineValue.indexOf(
 								"\"", indexOfNextApostrophe + 1);
 
 					}
 
 					/*
 					 * set the correct value for the string token and the token
 					 * type
 					 */
 					this.actualTokenValue = this.actualLineValue.substring(0,
 							indexOfNextApostrophe + 1);
 					this.actualTokenType = TokenType.STRING;
 
 				}
 
 			} else if (this.actualLineValue.startsWith("#")) {
 
 				this.actualTokenValue = this.actualLineValue;
 				this.actualTokenType = TokenType.COMMENT;
 
 			} else {
 
 				Matcher matchNextWhitespace = NEXT_WHITESPACE
 						.matcher(this.actualLineValue);
 				boolean hasNextWhitespace = false;
 
 				/*
 				 * check if a whitespace in line is existent and abstract the
 				 * token value from beginning of the line to the next whitespace
 				 */
 				while (matchNextWhitespace.find()) {
 
 					this.actualTokenValue = this.actualLineValue.substring(0,
 							matchNextWhitespace.start());
 					hasNextWhitespace = true;
 					break;
 
 				}
 
 				/*
 				 * if no whitespace is in line, the rest of the line is the
 				 * token value
 				 */
 				if (!hasNextWhitespace) {
 
 					this.actualTokenValue = this.actualLineValue;
 
 				}
 
 				/*
 				 * check if a semicolon is at the end of the token
 				 */
 				if (this.actualTokenValue.contains(";")
 						&& this.actualTokenValue.length() > 1) {
 
 					if (this.actualTokenValue.startsWith(";")) {
 
 						this.actualTokenValue = String
 								.valueOf(this.actualTokenValue.charAt(0));
 
 					} else {
 
 						this.actualTokenValue = this.actualTokenValue
 								.substring(0,
 										this.actualTokenValue.indexOf(";"));
 
 					}
 
 				}
 
 				/*
 				 * try to match the token value
 				 */
 				this.matchToken();
 
 				if (this.actualTokenType == TokenType.NOT_A_TOKEN) {
 
 					/*
 					 * check if a dot is in the token value and the value is not
 					 * a number
 					 */
 					if (this.actualTokenValue.contains(".")
 							&& this.actualTokenValue.length() > 1
 							&& this.actualTokenValue
 									.matches(".*\\.[a-zA-Z]\\w*")) {
 
 						if (this.actualTokenValue.startsWith(".")) {
 
 							this.actualTokenValue = String
 									.valueOf(this.actualTokenValue.charAt(0));
 
 						} else {
 
 							this.actualTokenValue = this.actualTokenValue
 									.substring(0,
 											this.actualTokenValue.indexOf("."));
 
 						}
 
 						this.matchToken();
 
 					}
 
 				}
 
 			}
 
 			/*
 			 * remove the actual token value from the line value
 			 */
 			this.actualLineValue = this.actualLineValue
 					.substring(this.actualTokenValue.length());
 
 		}
 
 	}
 
 	/**
 	 * Method checks if in the actual line a character is escaped or not
 	 * 
 	 * @param index
 	 *            of the character which is probably escaped
 	 * @return <code>true</code>: character is escaped<br>
 	 *         <code>false</code>: character is not escaped
 	 */
 	private boolean checkEscapeStatus(int index) {
 
 		return !String.valueOf(this.actualLineValue.charAt(index - 1)).equals(
 				"\\");
 
 	}
 
 	/**
 	 * Method checks if the actual line is empty or all tokens in line were read
 	 * 
 	 * @return <code>true</code>: all tokens were read, line must be increased <br>
 	 *         <code>false</code>: otherwise
 	 */
 	private boolean checkLineOfCode() {
 
 		if (this.actualLineValue.isEmpty()
 				|| this.actualLineValue.matches("\\s+")) {
 
 			return true;
 
 		} else {
 
 			return false;
 
 		}
 
 	}
 
 	/**
 	 * Method increases the actual line of code, resets the column and the token
 	 * value and set the new line value
 	 */
 	private void increaseLineOfCode() {
 
 		this.actualLine++;
 		this.actualColumn = 1;
 		this.actualTokenValue = "";
 
 		/*
 		 * check if all tokens are read
 		 */
 		if (this.convertedLines.size() < this.actualLine) {
 
 			this.actualTokenValue = "$";
 			this.actualTokenType = TokenType.EOF;
 			this.isEOF = true;
 
 		} else {
 
 			this.actualLineValue = this.convertedLines.get(this.actualLine - 1);
 
 			/*
 			 * check if the actual line is empty
 			 */
 			if (this.checkLineOfCode()) {
 
 				this.increaseLineOfCode();
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * Method matches the actual token value and find the type of the token
 	 * 
 	 * @see TokenType
 	 */
 	private void matchToken() {
 
 		if (this.actualTokenValue
 				.matches("(\\+|-)?[0-9]+((e|E)(\\+|-)?[0-9]+)?")) {
 
 			this.actualTokenType = TokenType.NUM;
 
 		} else if (this.actualTokenValue
 				.matches("(\\+|-)?[0-9]+\\.[0-9]+((e|E)(\\+|-)?[0-9]+)?")) {
 
 			this.actualTokenType = TokenType.REAL;
 
 		} else if (this.actualTokenValue.matches(";")) {
 
 			this.actualTokenType = TokenType.SEMICOLON;
 
 		} else if (this.actualTokenValue.matches("\\.")) {
 
 			this.actualTokenType = TokenType.DOT;
 
 		} else if (this.actualTokenValue.matches("\\(")) {
 
 			this.actualTokenType = TokenType.LEFT_PARAN;
 
 		} else if (this.actualTokenValue.matches("\\)")) {
 
 			this.actualTokenType = TokenType.RIGHT_PARAN;
 
 		} else if (this.actualTokenValue.matches("\\{")) {
 
 			this.actualTokenType = TokenType.LEFT_BRACE;
 
 		} else if (this.actualTokenValue.matches("\\}")) {
 
 			this.actualTokenType = TokenType.RIGHT_BRACE;
 
 		} else if (this.actualTokenValue.matches("\\[")) {
 
 			this.actualTokenType = TokenType.LEFT_BRACKET;
 
 		} else if (this.actualTokenValue.matches("\\]")) {
 
 			this.actualTokenType = TokenType.RIGHT_BRACKET;
 
 		} else if (this.actualTokenValue.matches("=")) {
 
 			this.actualTokenType = TokenType.ASSIGNOP;
 
 		} else if (this.actualTokenValue.matches("&&")) {
 
 			this.actualTokenType = TokenType.AND;
 
 		} else if (this.actualTokenValue.matches("\\|\\|")) {
 
 			this.actualTokenType = TokenType.OR;
 
 		} else if (this.actualTokenValue.matches("==")) {
 
 			this.actualTokenType = TokenType.EQUALS;
 
 		} else if (this.actualTokenValue.matches("!=")) {
 
 			this.actualTokenType = TokenType.NOT_EQUALS;
 
 		} else if (this.actualTokenValue.matches("<")) {
 
 			this.actualTokenType = TokenType.LESS;
 
 		} else if (this.actualTokenValue.matches("<=")) {
 
 			this.actualTokenType = TokenType.LESS_OR_EQUAL;
 
 		} else if (this.actualTokenValue.matches(">")) {
 
 			this.actualTokenType = TokenType.GREATER;
 
 		} else if (this.actualTokenValue.matches(">=")) {
 
 			this.actualTokenType = TokenType.GREATER_EQUAL;
 
 		} else if (this.actualTokenValue.matches("\\+")) {
 
 			this.actualTokenType = TokenType.PLUS;
 
 		} else if (this.actualTokenValue.matches("\\-")) {
 
 			this.actualTokenType = TokenType.MINUS;
 
 		} else if (this.actualTokenValue.matches("\\*")) {
 
 			this.actualTokenType = TokenType.TIMES;
 
 		} else if (this.actualTokenValue.matches("\\/")) {
 
 			this.actualTokenType = TokenType.DIVIDE;
 
 		} else if (this.actualTokenValue.matches("!")) {
 
 			this.actualTokenType = TokenType.NOT;
 
 		} else if (this.actualTokenValue.matches("true")) {
 
 			this.actualTokenType = TokenType.TRUE;
 
 		} else if (this.actualTokenValue.matches("false")) {
 
 			this.actualTokenType = TokenType.FALSE;
 
 		} else if (this.actualTokenValue.matches("if")) {
 
 			this.actualTokenType = TokenType.IF;
 
 		} else if (this.actualTokenValue.matches("else")) {
 
 			this.actualTokenType = TokenType.ELSE;
 
 		} else if (this.actualTokenValue.matches("while")) {
 
 			this.actualTokenType = TokenType.WHILE;
 
 		} else if (this.actualTokenValue.matches("do")) {
 
 			this.actualTokenType = TokenType.DO;
 
 		} else if (this.actualTokenValue.matches("break")) {
 
 			this.actualTokenType = TokenType.BREAK;
 
 		} else if (this.actualTokenValue.matches("return")) {
 
 			this.actualTokenType = TokenType.RETURN;
 
 		} else if (this.actualTokenValue.matches("print")) {
 
 			this.actualTokenType = TokenType.PRINT;
 
 		} else if (this.actualTokenValue.matches("long")) {
 
 			this.actualTokenType = TokenType.LONG_SYMBOL;
 
 		} else if (this.actualTokenValue.matches("double")) {
 
 			this.actualTokenType = TokenType.DOUBLE_SYMBOL;
 
 		} else if (this.actualTokenValue.matches("bool")) {
 
 			this.actualTokenType = TokenType.BOOL_SYMBOL;
 
 		} else if (this.actualTokenValue.matches("string")) {
 
 			this.actualTokenType = TokenType.STRING_SYMBOL;
 
 		} else if (this.actualTokenValue.matches("record")) {
 
 			this.actualTokenType = TokenType.RECORD_SYMBOL;
 
 		} else if (this.actualTokenValue.matches("[a-zA-Z]\\w*")) {
 
 			this.actualTokenType = TokenType.ID;
 
		} else if (this.actualTokenValue == "$") {

			this.actualTokenType = TokenType.EOF;

 		} else {
 
 			this.actualTokenType = TokenType.NOT_A_TOKEN;
 
 		}
 
 	}
 }
