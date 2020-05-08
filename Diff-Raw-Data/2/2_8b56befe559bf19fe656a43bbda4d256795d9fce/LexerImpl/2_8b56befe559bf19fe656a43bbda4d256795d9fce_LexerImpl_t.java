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
 
 	private ArrayList<String> convertedLines;
 	private String actualLineValue;
 	private String actualTokenValue;
 	private TokenType actualTokenType;
 	private Integer actualLine;
 	private Integer actualColumn;
 	private boolean isEOF;
 	private Integer lastLine;
 	private Integer lastColumn;
 	private final static Pattern NEXT_CHARACTER = Pattern.compile("[^\\s]+");
 	private final static Pattern NEXT_SEPARATOR = Pattern
 			.compile(
 					"(\\s|==|\\&\\&|\\|\\||!=|<=|>=|=|<|>|\\+|\\-|\\*|\\/|!|\\.|;|\\(|\\)|\\{|\\}|\\[|\\]|\\n)",
 					Pattern.CANON_EQ);
 
 	/**
 	 * Default constructor initializes class variables
 	 */
 	public LexerImpl() {
 
 		this.init();
 
 	}
 
 	/**
 	 * Method sets an {@link InputStream} for the lexer and splits it line by
 	 * line into an {@link ArrayList}
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
 
 			this.actualTokenType = TokenType.EOF;
 
 		} else {
 
 			/*
 			 * get the value of the first line
 			 */
 			this.actualLineValue = this.convertedLines.get(this.actualLine - 1);
 			this.actualTokenValue = "";
 			this.isEOF = false;
 			this.lastLine = this.convertedLines.size();
 			this.lastColumn = this.convertedLines.get(this.lastLine - 1)
 					.length();
 
 		}
 
 	}
 
 	/**
 	 * Method to initialize class variables
 	 */
 	private void init() {
 
 		this.actualTokenValue = null;
 		this.actualTokenType = TokenType.EOF;
 		this.actualLine = 1;
 		this.actualColumn = 1;
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
 		 * analyze the actual type of token
 		 */
 		switch (this.actualTokenType.name()) {
 
 		case "NUM":
 			return new NumTokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 
 		case "REAL":
 			return new RealTokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 
 		case "TRUE":
 			return new BoolTokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 
 		case "FALSE":
 			return new BoolTokenImpl(this.actualTokenValue,
 					this.actualTokenType, this.actualLine, this.actualColumn);
 
 		default:
 			return new TokenImpl(this.actualTokenValue, this.actualTokenType,
 					this.actualLine, this.actualColumn);
 
 		}
 
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
 			 * check if the next token is a string (starts and ends with an
 			 * apostrophe), a comment or another type
 			 */
 			if (this.actualLineValue.startsWith("\"")
 					&& this.actualLineValue.indexOf("\"", 1) != -1) {
 
 				int indexOfNextApostrophe = this.actualLineValue.indexOf("\"",
 						1);
 
 				/*
 				 * loop as long as the next not escaped apostrophe is found
 				 */
 				while (!this.checkEscapeStatus(indexOfNextApostrophe)) {
 
 					indexOfNextApostrophe = this.actualLineValue.indexOf("\"",
 							indexOfNextApostrophe + 1);
 
 				}
 
 				/*
 				 * set the correct value for the string token and the token type
 				 */
 				this.actualTokenValue = this.actualLineValue.substring(0,
 						indexOfNextApostrophe + 1);
 				this.actualTokenType = TokenType.STRING;
 
 			} else if (this.actualLineValue.startsWith("#")) {
 
 				this.actualTokenValue = this.actualLineValue;
 				this.actualTokenType = TokenType.COMMENT;
 
 			} else {
 
 				Matcher matchNextSeparator = NEXT_SEPARATOR
 						.matcher(this.actualLineValue);
 
 				/*
 				 * check if a separator character is in line
 				 */
 				boolean hasNextSeparator = false;
 
 				while (matchNextSeparator.find()) {
 
 					int indexOfNextSeparator = matchNextSeparator.start();
 					int endOfNextSeparator = matchNextSeparator.end();
 
 					/*
 					 * check if the next separator character is to match as
 					 * token or another token is in front of it
 					 */
 					if (indexOfNextSeparator == 0) {
 
 						this.actualTokenValue = this.actualLineValue.substring(
 								0, endOfNextSeparator);
 						hasNextSeparator = true;
 
 					} else {
 
 						/*
 						 * check if separator is a dot or a minus, then it could
 						 * be part of a number
 						 */
 						if (String.valueOf(
 								this.actualLineValue
 										.charAt(indexOfNextSeparator)).equals(
 								".")
 								&& this.actualLineValue.matches("(-)?\\d+.*")) {
 
 							continue;
 
 						} else if (String.valueOf(
 								this.actualLineValue
 										.charAt(indexOfNextSeparator)).equals(
 								"-")
 								&& this.actualLineValue
										.matches("\\d+(\\.\\d+)?(e|E)-.*")) {
 
 							continue;
 
 						} else {
 
 							this.actualTokenValue = this.actualLineValue
 									.substring(0, matchNextSeparator.start());
 							hasNextSeparator = true;
 
 						}
 
 					}
 
 					break;
 
 				}
 
 				/*
 				 * if no separator character is in line, the rest of the line is
 				 * the token value
 				 */
 				if (!hasNextSeparator) {
 
 					this.actualTokenValue = this.actualLineValue;
 
 				}
 
 				/*
 				 * match the token value
 				 */
 				this.matchToken();
 
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
 
 			this.actualTokenValue = null;
 			this.actualTokenType = TokenType.EOF;
 			this.actualLine = this.lastLine;
 			this.actualColumn = this.lastColumn + 1;
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
 
 		if (this.actualTokenValue.matches("[0-9]+((e|E)(-)?[0-9]+)?")) {
 
 			this.actualTokenType = TokenType.NUM;
 
 		} else if (this.actualTokenValue
 				.matches("[0-9]+\\.[0-9]+((e|E)(-)?[0-9]+)?")) {
 
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
 
 		} else {
 
 			this.actualTokenType = TokenType.NOT_A_TOKEN;
 
 		}
 
 	}
 }
