 package frontend;
 
 import java.util.HashSet;
 import java.util.Hashtable;
 
 public enum TokenType {
     // Reserved words
    AND, COND, DEFINE, ELSE, IF, LAMBDA, LET, LETREC, LETSTAR, NOT, OR, CONS, NULL,
 
     // Special Symbols
     QUOTE("'"), EQUALS("="), LESS_THAN("<"), LESS_EQUALS("<="), GREATER_EQUALS(">="),
     GREATER_THAN(">"), LEFT_PAREN , RIGHT_PAREN , PLUS ("+"), MINUS ("-"),MULTIPLICATION ("*"), DIVISIOR ("/"),
 
 
     // Others
     IDENTIFIER, INTEGER, REAL, STRING, SYMBOL, ERROR, END_OF_FILE;
 
     private static final int FIRST_RESERVED_INDEX = AND.ordinal();
    private static final int LAST_RESERVED_INDEX  = NULL.ordinal();
 
     private static final int FIRST_SPECIAL_INDEX = QUOTE.ordinal();
     private static final int LAST_SPECIAL_INDEX  = RIGHT_PAREN.ordinal();
 
     private String text;  // token text
 
     TokenType() {
         this.text = this.toString().toLowerCase();
     }
 
     TokenType(String text) {
         this.text = text;
     }
 
     public String getText() {
         return text;
     }
 
     // Set of lower-cased Scheme reserved word text strings.
     public static HashSet<String> RESERVED_WORDS = new HashSet<String>();
     static {
         TokenType values[] = TokenType.values();
         for (int i = FIRST_RESERVED_INDEX; i <= LAST_RESERVED_INDEX; ++i) {
             RESERVED_WORDS.add(values[i].getText().toLowerCase());
         }
     }
 
     // Hash table of Scheme special symbols.  Each special symbol's text
     // is the key to its Scheme token type.
     public static Hashtable<String, TokenType> SPECIAL_SYMBOLS =
             new Hashtable<String, TokenType>();
     static {
         TokenType values[] = TokenType.values();
         for (int i = FIRST_SPECIAL_INDEX; i <= LAST_SPECIAL_INDEX; ++i) {
             SPECIAL_SYMBOLS.put(values[i].getText(), values[i]);
         }
     }
 }
