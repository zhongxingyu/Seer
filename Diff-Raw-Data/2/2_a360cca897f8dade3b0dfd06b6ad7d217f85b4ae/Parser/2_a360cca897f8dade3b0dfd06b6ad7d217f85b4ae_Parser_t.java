 package fc.parser.rdp;
 
 import fc.lang.AssignementExpression;
 import fc.lang.ChangeSignFunction;
 import fc.lang.ConstantValueExpression;
 import fc.lang.DifferenceFunction;
 import fc.lang.DivisionFunction;
 import fc.lang.ExitCommand;
 import fc.lang.Expression;
 import fc.lang.FunctionExpression;
 import fc.lang.MultiplicationFunction;
 import fc.lang.SumFunction;
 import fc.lang.VariableExpression;
 import fc.lexer.Lexer;
 import fc.lexer.Symbol;
 import fc.lexer.Token;
 import fc.parser.common.ParseException;
 
 /**
  * Implementation of a recursive descent parser for the a simple calculator
  */
 public class Parser implements fc.parser.common.Parser {
     private Token currentToken;
     private Lexer lexer;
 
     /**
      * Fetch next token from the lexical analyzer and store it for later
      * treatment and return the current token.
      * 
      * @return the current Token
      */
     public Token advance() {
         Token previousToken = currentToken;
         currentToken = lexer.nextToken();
         return previousToken;
     }
 
     /**
      * Test if the current token matches the given symbol. If yes, the current
      * token is returned and the parse position is advanced, otherwise null is
      * returned and no action is performed.
      * 
      * @return the current Token or null
      */
     public Token accept(Symbol symbol) {
         if (currentToken.getSymbol() == symbol) {
             return advance();
         }
         else {
             return null;
         }
     }
 
     /**
      * Test if the current token matches the given symbol. If yes, the current
      * token is returned and the parse position is advanced, otherwise a parsing
      * exception is thrown.
      * 
      * @return the current Token
      */
     public Token expect(Symbol symbol) throws ParseException {
         Token previousToken = accept(symbol);
 
         if (previousToken == null) {
             throw new ParseException("Expected symbol " + symbol + " but got "
                     + currentToken.getSymbol() + " instead", currentToken.getPosition());
         }
 
         return previousToken;
     }
 
     /**
      * Parse a basic statement
      * 
      * Statement -> 'let' Identifier '=' Expr | Expr
      *
      * @return resulting expression
      * @throws ParseException
      */
     private Expression parseStatement() throws ParseException {
         Expression result;
 
         if (accept(Symbol.LET) != null) {
             // parse assignment statement (let x = expr)
             String identifier = expect(Symbol.IDENTIFIER).getStringValue();
             expect(Symbol.EQUAL);
             Expression expression = parseExpression();
             result = new AssignementExpression(identifier, expression);
         }
        else if (accept(Symbol.EXIT) != null) {
             result = new ExitCommand();
         }
         else {
             result = parseExpression();
         }
 
         return result;
     }
 
     /**
      * Parse an addition or difference expression
      *
      * Expr -> Term { ('+'|'-') Term }
      *
      * @return
      * @throws ParseException
      */
     private Expression parseExpression() throws ParseException {
         Expression result;
         // left summand
         result = parseTerm();
 
         // parse additional summands if any
         while (true) {
             if (accept(Symbol.PLUS) != null) {
                 Expression term = parseTerm();
                 result = new FunctionExpression(new SumFunction(), result, term);
             }
             else if (accept(Symbol.MINUS) != null) {
                 Expression term = parseTerm();
                 result = new FunctionExpression(new DifferenceFunction(),
                         result, term);
             }
             else {
                 break;
             }
         }
 
         return result;
     }
 
     /**
      * Parse a term
      * 
      * Term -> Factor { ('*'|'/') Factor }
      * 
      * @return
      * @throws ParseException
      */
     private Expression parseTerm() throws ParseException {
         Expression result;
 
         // parse one factor
         result = parseFactor();
 
         // parse additional factors
         while (true) {
             if (accept(Symbol.MULTIPLY) != null) {
                 Expression factor = parseFactor();
                 result = new FunctionExpression(new MultiplicationFunction(),
                         result, factor);
             }
             else if (accept(Symbol.DIVIDE) != null) {
                 Expression factor = parseFactor();
                 result = new FunctionExpression(new DivisionFunction(), result,
                         factor);
             }
             else {
                 break;
             }
         }
 
         return result;
     }
 
     /**
      * Parse a factor, possibly with leading unary minus
      * 
      * Factor -> [ '-' ] ( '(' Expr ')' | Num | Identifier )
      * 
      * Note: this function covers the first part of the EBNF rule above.
      */
     private Expression parseFactor() throws ParseException {
         Expression result;
 
         // first term
         if (accept(Symbol.MINUS) != null) {
             // parse unary minus if any
             Expression term = parseFactorUnsigned();
             result = new FunctionExpression(new ChangeSignFunction(), term);
         }
         else {
             result = parseFactorUnsigned();
         }
 
         return result;
     }
 
     /**
      * Parse factor with leading unary minus removed
      *
      * Factor -> [ '-' ] ( '(' Expr ')' | Num | Identifier )
      *
      * Note: this function covers the second part of the EBNF rule above.
      */
     private Expression parseFactorUnsigned() throws ParseException {
         Expression result;
 
         Token token;
         if ((token = accept(Symbol.NUMBER)) != null) {
             result = new ConstantValueExpression(token.getNumberValue()
                     .doubleValue());
         }
         else if ((token = accept(Symbol.IDENTIFIER)) != null) {
             result = new VariableExpression(token.getStringValue());
         }
         else if (accept(Symbol.LPAREN) != null) {
             result = parseExpression();
             expect(Symbol.RPAREN);
         }
         else {
             throw new ParseException(
                     "Expected a number, identifier or a left parenthesis but got "
                             + currentToken.getSymbol() + " instead", currentToken.getPosition());
         }
 
         return result;
     }
 
     @Override
     public Expression parse(String line) throws ParseException {
         lexer = new Lexer(line);
 
         advance();
         Expression result = parseStatement();
         expect(Symbol.END);
 
         return result;
     }
 }
