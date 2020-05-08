 package toybox.lexer;
 
 /**
  * A collection of frequently used regular expressions for lexers. 
  * 
  * @author Bruno Dufour (dufour@iro.umontreal.ca)
  */
 public final class Common {
 	private Common() {
 		// no instances
 	}
 	
 	/** A C-style block comment */
 	public static final String SlashStarComment    = "/\\*(.|\\n)*?\\*/";
 	/** A C++-style single line comment */
 	public static final String SlashSlashComment   = "//.*";
 	/** Default whitespace characters */
 	public static final String Whitespace          = "\\s+";
 	/** A double-quoted string literal, with escape sequences. */
	public static final String DoubleQuotedString  = "\"([^\"\\n]|(\\\\.))*\"";
 	/** A single-quoted string literal, with escape sequences. */
	public static final String SingleQuotedString  = "'([^'\\n]|(\\\\.))*?'";
 	
 	// Delimiters
 	public static final String L_BRACE   = "\\{";
 	public static final String R_BRACE   = "\\}";
 	
 	public static final String L_BRACKET = "\\[";
 	public static final String R_BRACKET = "\\]";
 	
 	public static final String L_PAREN   = "\\(";
 	public static final String R_PAREN   = "\\)";
 
 	// Punctuation
 	public static final String SEMICOLON = ";";
 	public static final String COLON     = ":";
 	public static final String COMMA     = ",";
 	
 	// Logical operators
 	public static final String EQ        = "=";
 	public static final String LT        = "<";
 	public static final String GT        = ">";
 	public static final String LE        = "<=";
 	public static final String GE        = ">=";
 	
 	// Bitwise ops
 	public static final String AND       = "&";
 	public static final String OR        = "\\|";
 	public static final String XOR       = "\\^";
 	
 	// Arithmetic
 	public static final String PLUS      = "\\+";
 	public static final String MINUS     = "-";
 	public static final String TIMES     = "\\*";
 	public static final String DIV       = "/";
 }
