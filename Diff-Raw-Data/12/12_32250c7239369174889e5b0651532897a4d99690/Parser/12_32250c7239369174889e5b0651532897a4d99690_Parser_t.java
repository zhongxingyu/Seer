 package differentiator;
 
 import java.util.Iterator;
 
 /**
  * The parser gets a bunch of tokens from the lexer and determines what
  * expression was written by the user.
  */
 // Grammar rule
 // E = "(" T [+|-] T + ")" | "(" T ")"
 // T = Num|VAR|E
 public class Parser {
     private final Iterator<Token> tokens;
     private Token currentToken; // the pointer to the current token after the
                                 // call to getTokens
 
     /**
      * Created a passing using the token from the lexer The lexer already
      * checked for basic valid input such as balanced paren, having left paren,
      * valid character. However, it doesn't check for valid Expression require:
      * lexer can't be null
      * 
      * */
     public Parser(Lexer lexer) {
         this.tokens = lexer.wrapperLexer().iterator();
         this.currentToken = null;
 
     }
 
     // return the next Token if there is one or EOF
     private Token getTokens() {
         if (tokens.hasNext())
             return tokens.next();
         else
             return new Token(Token.Type.EOF, "EOF");
     }
 
     // assertion for checking the argument passing in is legal
     private void expect(Token.Type token) {
         if (currentToken.getType() == token)
             return;
         else
             throw new IllegalArgumentException("Illegal token +" + token);
 
     }
 
     /**
      * Return the Binary tree of an expression
      * 
      * @return Expression
      * */
     public Expression eParser() {
         Expression resultExpression = null;
         currentToken = getTokens();
         resultExpression = E();
         expect(Token.Type.EOF);
         return resultExpression;
 
     }
 
     /**
     * Follow the actual grammar rule , E() is non-terminal
      * E = "(" T [+|-] T + ")" | "(" T ")"
     * T = Num|Var|E
      * modifies expression
      * @return Expression
      * */
     private Expression E() {
         Expression expr = null;
 
         Expression expr1 = null;
 
         Token next = currentToken;
         if (next.getType().equals(Token.Type.LEFTPAREN)) {
             // hold the value of the current token before recursion
             currentToken = getTokens();
             expr = T();
             Token next2 = currentToken;
             if (next2.getType().equals(Token.Type.RIGHTPAREN)) {
                 // Singleton Expression, return
                 currentToken = getTokens();
                 return expr;
                 // expected operator after an Expression T
             } else if (next2.getType().equals(Token.Type.SUM)
                     || next2.getType().equals(Token.Type.PROD)) {
                 currentToken = getTokens();
                 expr1 = T();
                 if (currentToken.getType().equals(Token.Type.RIGHTPAREN)) {
                     switch (next2.getType()) {
                     case SUM:
                         currentToken = getTokens();
                         return new Sum(expr, expr1);
                     case PROD:
                         currentToken = getTokens();
                         return new Multiply(expr, expr1);
                     default:
                         throw new RuntimeException("Illegal TOKEN "
                                 + next2.getType());
                     }
 
                 } else
                     throw new RuntimeException("Expected Rightparen");
 
             } else
                 throw new RuntimeException("Missing right paren or operator");
         } else
             throw new RuntimeException("Expected Opening parenthesis ");
     }
 
     // Contain the terminal Num|Var|Expression
     private Expression T() {
         Expression term = null;
         Token next = currentToken;
 
         if (next.getType().equals(Token.Type.NUMERIC)) {
             term = new Num(next.getPattern());
             currentToken = getTokens();
             return term;
         }
 
         else if (next.getType().equals(Token.Type.VARIABLE)) {
             term = new Var(next.getPattern());
             currentToken = getTokens();
             return term;
         }
 
         else {
             term = E();
             return term;
         }
 
     }
 
     /**
      * Call the to String method on valid mathematical expression
      */
     @Override
     public String toString() {
 
         return eParser().toString();
     }
 
 }
