 package project1;
 
 import java.util.Formatter;
 
 public class Token {
     final TokenType type;
     final String value;
     final int lineNumber;
 
     public Token(TokenType type, String value, int lineNumber) {
         this.type = type;
         this.value = value;
         this.lineNumber = lineNumber;
     }
 
     public TokenType getTokenType() {
         return type;
     }
 
     public String getValue() {
         return value;
     }
 
     public String toString() {
     	return value;
     }
     
     public String toString_old() {
         Formatter f = new Formatter();
         f.format("(%s,\"%s\",%d)", type, value, lineNumber);
         //System.out.println(value.equals("#SchemesFactsRules\n"));
         //System.out.println(f.toString());
         for (char c : value.toCharArray()) {
         	//System.out.println((int)c);
         	//System.out.println((int)'\n');
         }
         //System.out.println();
         return f.toString();
     }
     
     public Token clone() {
     	return new Token(type, new String(value), lineNumber);
     }
     
     public boolean equals(Token t) {
     	return value.equals(t.value);
     }
 }
