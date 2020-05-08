 package parser;
 
 import java.util.regex.Pattern;
 
 public enum RegexTokenType implements TokenType {
 	
   // Token types
 	CLS_CHAR(/*[^([\\\\] | [\\^] | [\\-] | [\\[] | [\\]])]" + 
 			 "[ [\\\\\\\\] | [\\\\\\^] | [\\\\\\-] | [\\\\\\[] | [\\\\\\]] ]*/"Q"),
 	RE_CHAR(/*[^([\\\\s]|[\\\\]|[\\*]|[\\+]|[\\?]|[\\[]|[\\]]|[\\(]|[\\)]|[\\.]|[\\']|[\\\"])]" + 
 			"[ [\\\\] | [\\\\\\\\] | [\\\\\\*] | [\\\\\\+] | [\\\\\\?] | [\\\\\\|] | [\\\\\\[] | [\\\\\\]] | [\\\\\\(] | [\\\\\\)] | [\\\\\\.] | [\\\\\\'] | [\\\\\\\"] ]*/"Q"),
 	
 	TOKEN_IN("IN"),
	TOKEN_DEFINE("[$][A-Z]+"),
 	
 	TOKEN_UPPER("[A-Z]"),
 	TOkEN_LOWER("[a-z]"),
 	TOKEN_IDENTIFIER("[a-zA-Z]+"),
 	
 	TOKEN_DIGIT("[0-9]"),
 	
 	TOKEN_EQUALS("\\="),
 	TOKEN_PLUS("\\+"),
	TOKEN_MINUS("\\-"),
 	TOKEN_ASTE("\\*"),
 	TOKEN_UP("\\^"),
 	TOKEN_DOT("\\."),
 	TOKEN_UNION("\\|"),
 	
 	TOKEN_L_BRACKET("\\["),
 	TOKEN_R_BRACKET("\\]"),
 	TOKEN_L_PAREN("\\("),
 	TOKEN_R_PAREN("\\)");
 
   // Lexemes
   public static final String LEX_EQUALS = "=";
  public static final String LEX_PlUS = "+";
  public static final String LEX_MINUS = "-";
   public static final String LEX_IN = "IN";
   public static final String LEX_ASTE = "*";
   public static final String LEX_UP = "^";
   public static final String LEX_UNION = "|";
   public static final String LEX_DOT = ".";
   public static final String LEX_L_PAREN = "(";
   public static final String LEX_R_PAREN = ")";
   public static final String LEX_L_BRACKET = "[";
   public static final String LEX_R_BRACKET = "]";
 
   private Pattern pattern;
 
   RegexTokenType(String pattern) {
     this.pattern = Pattern.compile(pattern);
   }
 
   public Object getType() {
     return this;
   }
 
   public Pattern getPattern() {
     return this.pattern;
   }
 }
