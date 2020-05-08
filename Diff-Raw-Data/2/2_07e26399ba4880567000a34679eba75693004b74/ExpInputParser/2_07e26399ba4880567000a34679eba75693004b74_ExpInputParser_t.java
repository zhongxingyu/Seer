 package de.skuzzle.polly.parsing;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import de.skuzzle.polly.parsing.PrecedenceTable.PrecedenceLevel;
 import de.skuzzle.polly.parsing.ast.Identifier;
 import de.skuzzle.polly.parsing.ast.Node;
 import de.skuzzle.polly.parsing.ast.ResolvableIdentifier;
 import de.skuzzle.polly.parsing.ast.Root;
 import de.skuzzle.polly.parsing.ast.declarations.Declaration;
 import de.skuzzle.polly.parsing.ast.declarations.Namespace;
 import de.skuzzle.polly.parsing.ast.declarations.types.ListType;
 import de.skuzzle.polly.parsing.ast.declarations.types.MapType;
 import de.skuzzle.polly.parsing.ast.declarations.types.ProductType;
 import de.skuzzle.polly.parsing.ast.declarations.types.Type;
 import de.skuzzle.polly.parsing.ast.expressions.Assignment;
 import de.skuzzle.polly.parsing.ast.expressions.Braced;
 import de.skuzzle.polly.parsing.ast.expressions.Call;
 import de.skuzzle.polly.parsing.ast.expressions.Delete;
 import de.skuzzle.polly.parsing.ast.expressions.Delete.DeleteableIdentifier;
 import de.skuzzle.polly.parsing.ast.expressions.Empty;
 import de.skuzzle.polly.parsing.ast.expressions.Expression;
 import de.skuzzle.polly.parsing.ast.expressions.Inspect;
 import de.skuzzle.polly.parsing.ast.expressions.NamespaceAccess;
 import de.skuzzle.polly.parsing.ast.expressions.OperatorCall;
 import de.skuzzle.polly.parsing.ast.expressions.VarAccess;
 import de.skuzzle.polly.parsing.ast.expressions.literals.BooleanLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.ChannelLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.DateLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.FunctionLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.HelpLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.ListLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.NumberLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.ProductLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.StringLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.TimespanLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.UserLiteral;
 import de.skuzzle.polly.parsing.ast.lang.Operator.OpType;
 
 
 /**
  * <p>This class provides recursive descent parsing for polly expressions and can output
  * an abstract syntax tree for the parsed expression. The root of the AST is represented
  * by the class {@link Root}, all AST nodes are subclasses of {@link Node}.
  * Every AST node that is created by this parser gets assigned its actual 
  * {@link Position} within the input string. This allows to provide detailed error
  * message during parsing, type-checking or execution of the AST.</p>
  * 
  * <p>This parser uses the following context-free syntax, given in EBNF. There may exist
  * some tweaks in the implementation that are not expressed in the following grammar.</p>
  * 
  * <pre>
  *   root        -> ':' ID (assign (WS assign)*)?              // AST root with a WS separated list of expressions
  *   
  *   assign      -> relation '->' PUBLIC? TEMP? ID             // assignment of relation to identifier X
  *   relation    -> conjunction (REL_OP conjunction)*          // relation (<,>,<=,>=,==, !=)
  *   conjunction -> disjunction (CONJ_OP disjunction)*         // conjunction (||)
  *   disjunction -> secTerm (DISJ_OP secTerm)*                 // disjunction (&&)
  *   secTerm     -> term (SECTERM_OP term)*                    // plus minus
  *   term        -> factor (TERM_OP factor)*                   // multiplication and co
  *   factor      -> postfix (FACTOR_OP factor)?                // right-associative (power operator)
  *   postfix     -> autolist (POSTFIX_OP autolist)*            // postfix operator
  *   autolist    -> dotdot (';' dotdot)*                       // implicit list literal
  *   dotdot      -> unary ('..' unary ('$' unary)?)?           // range operator with optional step size
  *   unary       -> UNARY_OP unary                             // right-associative unary operator
  *                | call
  *   call        -> access ( '(' parameters ')' )?
  *   access      -> literal ('.' literal )?                    // namespace access. left operand must be a single identifier (represented by a VarAccess)
  *   literal     -> ID                                         // VarAccess
  *                | '(' relation ')'                           // braced expression
  *                | '\(' parameters ':' relation ')'           // lambda function literal
  *                | '{' exprList '}'                           // concrete list of expressions
  *                | DELETE PUBLIC? ID (',' PUBLIC? ID)*        // delete operator
  *                | INSPECT  PUBLIC ID                         // inspect for public
  *                | INSPECT ID ('.' ID)?                       // inspect operator
  *                | IF relation ':' relation ':' relation      // conditional operator
  *                | TRUE | FALSE                               // boolean literal
  *                | CHANNEL                                    // channel literal
  *                | USER                                       // user literal
  *                | STRING                                     // string literal
  *                | NUMBER                                     // number literal
  *                | DATETIME                                   // date liter
  *                | TIMESPAN                                   // timespan literal
  *                | '?'                                        // HELP literal
  *                | RADIX literal                              // radix operator
  *            
  *   exprList    -> (relation (',' relation)*)?
  *   parameters  -> (parameter (',' parameter)*)?
  *   parameter   -> type ID
  *   type        -> ID                                         // primitive type
  *                | LIST '&lt;' type '&gt;'                    // list type
  *                | '(' type (WS type)* '->' type ')'          // function type
  *                
  *   WS       -> ' ' | \t
  *   TEMP     -> 'temp'
  *   PUBLIC   -> 'public'
  *   IF       -> 'if'
  *   TRUE     -> 'true'
  *   FALSE    -> 'false'
  *   CHANNEL  -> '#' ID
  *   USER     -> '@' ID
  *   STRING   -> '"' .* '"'
  *   NUMBER   -> [0-9]*(\.[0-9]+([eE][0-9]+)?)?
  *   TIMESPAN -> ([0-9]+[ywdhms])+
  *   DATE     -> [0-9]{1,2}\.[0-9]{1,2}\.[0-9]{4}
  *   TIME     -> [0-9]{1,2}:[0-9]{1,2}
  *   DATETIME -> TIME | DATE | DATE@TIME
  *   ID       -> [_a-zA-Z][_a-zA-Z0-9]+
  *             | '\' .                                         // any escaped token
  * </pre>
  * 
  * @author Simon Taddiken
  */
 public class ExpInputParser {
     
     /**
      * Whether escaping of tokens is enabled.
      */
     public final static boolean ESCAPABLE = true;
 
     
 
     private final PrecedenceTable operators;
     private int openExpressions;
     protected InputScanner scanner;
     
     
     
     /**
      * Creates a new parser which will use the provided scanner to read the tokens from.
      * 
      * @param scanner The {@link InputScanner} which provides the token stream.
      */
     public ExpInputParser(InputScanner scanner) {
         this.scanner = scanner;
         this.operators = new PrecedenceTable();
     }
     
     
     
     /**
      * Creates a new parser which will parse the given input string using the default 
      * encoding.
      * 
      * @param input The string to parse.
      */
     public ExpInputParser(String input) {
         this.scanner = new InputScanner(input);
         this.operators = new PrecedenceTable();
     }
     
     
     
     /**
      * Creates a new parser which will parse the given input string using the provided
      * encoding.
      * 
      * @param input The string to parse.
      * @param encoding The charset name to use.
      * @throws UnsupportedEncodingException If the charset name was invalid.
      */
     public ExpInputParser(String input, String encoding) 
             throws UnsupportedEncodingException {
         this.scanner = new InputScanner(input, Charset.forName(encoding));
         this.operators = new PrecedenceTable();
     }
     
     
     
     /**
      * Tries to parse the input string and returns the root of the AST. If the string was
      * not valid, this method returns <code>null</code>.
      * 
      * @return The parsed AST root or <code>null</code> if the string was not well
      *          formatted.
      */
     public Root tryParse() {
         try {
             return this.parse();
         } catch (ParseException e) {
             e.printStackTrace();
         }
         return null;
     }
     
     
     
     /**
      * Parses the input string and returns the root of the AST. If the string was not 
      * valid, this method will throw a {@link ParseException}.
      * 
      * @return The parsed AST root.
      * @throws ParseException If the string was not well formatted.
      */
     protected Root parse() throws ParseException {
         return this.parseRoot();
     }
 
     
     
     /**
      * Throws a {@link ParseException} indicating an unexpected token.
      * 
      * @param expected The token that was expected but not found.
      * @param found The token which was found instead.
      * @throws ParseException Always thrown.
      */
     protected void unexpectedToken(TokenType expected, Token found) 
             throws ParseException {
         throw new ParseException("Unerwartetes Symbol: '" + found.toString(false, false) + 
                 "'. Erwartet: '" + expected.toString() + "'", 
                 this.scanner.spanFrom(found));
     }
     
     
     
     /**
      * Throws a {@link ParseException} if the not token has not the expected type. If the 
      * next token is the expected one, it is consumed and returned.
      * 
      * @param expected Expected token type.
      * @return The consumed expected token.
      * @throws ParseException If the next token has not the expected type.
      */
     protected Token expect(TokenType expected) throws ParseException {
         Token la = this.scanner.lookAhead();
         if (la.getType() != expected) {
             this.scanner.consume();
             this.unexpectedToken(expected, la);
         }
         this.scanner.consume();
         return la;
     }
     
     
     
     /**
      * Expects the next token to be an {@link Identifier}. If not, a 
      * {@link ParseException} is thrown. Otherwise, a new {@link Identifier} will be 
      * created and returned. This method also recognizes escaped tokens as identifiers.
      * 
      * @return An {@link Identifier} created from the next token.
      * @throws ParseException If the next token is no identifier.
      */
     protected Identifier expectIdentifier() throws ParseException {
         final Token la = this.scanner.lookAhead();
         if (ESCAPABLE && la.matches(TokenType.ESCAPED)) {
             this.scanner.consume();
             final EscapedToken esc = (EscapedToken) la;
             return new Identifier(esc.getPosition(), esc.getEscaped().getStringValue(), 
                 true);
         }
         this.expect(TokenType.IDENTIFIER);
         return new Identifier(la.getPosition(), la.getStringValue());
     }
 
     
     
     /**
      * Consumes a single whitespace of the next token is one. If not, nothing happens.
      * @throws ParseException If parsing fails.
      */
     protected void allowSingleWhiteSpace() throws ParseException {
         this.scanner.match(TokenType.SEPERATOR);
     }
     
     
     
     /**
      * Enters a new expression. If at least one epxression is "entered", the scanner will
      * ignore whitespaces.
      */
     protected void enterExpression() {
         ++this.openExpressions;
         this.scanner.setSkipWhiteSpaces(true);
     }
     
     
     
     /**
      * Leaves an entered expression. If the last expression was left, the scanner will 
      * stop ignoring whitespaces.
      */
     protected void leaveExpression() {
         --this.openExpressions;
         if (this.openExpressions == 0) {
             this.scanner.setSkipWhiteSpaces(false);
         }
     }
     
     
     
     protected Root parseRoot() throws ParseException {
         
         Root root = null;
         Token la = null;
         Position start = null;
         try {
             la = this.scanner.lookAhead();
             start = la.getPosition();
             
             if (!this.scanner.match(TokenType.COLON)) {
                 return null;
             }
             la = this.scanner.lookAhead();
             if (!this.scanner.match(TokenType.IDENTIFIER)) {
                 return null;
             }
         } catch (ParseException ignore) {
             // if an error occurs at this early stage of parsing, return null to 
             // show that input was invalid.
             return null;
         }
         
         final Identifier cmd = new Identifier(
             new Position(start.getStart(), la.getPosition().getEnd()),
             la.getStringValue());
         
         final List<Expression> signature = new ArrayList<Expression>();
         if (this.scanner.match(TokenType.SEPERATOR)) {
             do {
                 final Expression next = this.parseAssignment();
                 signature.add(next);
             } while (this.scanner.match(TokenType.SEPERATOR));
         }
         
         root = new Root(this.scanner.spanFrom(start), cmd, signature);
         
         this.expect(TokenType.EOS);
         return root;
     }
 
     
     
     /**
      * Parses an assignment. If no ASSIGN_OP is found, the result of the next
      * higher precedence level is returned. This is the root of all expressions and has 
      * thus lowest precedence level.
      * <pre>
      * assign -> relation '->'PUBLIC? TEMP? ID // assignment of relation to identifier X
      * </pre>
      * @return The parsed Assignment or the result of the next higher precedence level
      *          if no ASSIGN_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseAssignment() throws ParseException {
         final Expression lhs = this.parseRelation();
         
         if (this.scanner.match(TokenType.ASSIGNMENT)) {
             this.allowSingleWhiteSpace();
             final boolean pblc = this.scanner.match(TokenType.PUBLIC);
             this.allowSingleWhiteSpace();
             final boolean temp = this.scanner.match(TokenType.TEMP);
             this.allowSingleWhiteSpace();
             
             final Identifier id = this.expectIdentifier();
             
             return new Assignment(
                 new Position(lhs.getPosition(), id.getPosition()), 
                 lhs, id, pblc, temp);
         }
         return lhs;
     }
     
     
     
     /**
      * Parses RELATION precedence level operators.
      * <pre>
      * relation -> conjunction (REL_OP conjunction)*     // relation (<,>,<=,>=,==)
      * </pre>
      * @return The parsed operator call or the result from the next higher precedence 
      *          level if no REL_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseRelation() throws ParseException {
         Expression expr = this.parseConjunction();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.RELATION)) {
             this.scanner.consume();
             
             final Expression rhs = this.parseConjunction();
             
             expr = OperatorCall.binary(
                 new Position(expr.getPosition(), rhs.getPosition()), 
                 OpType.fromToken(la), expr, rhs);
             
             la = this.scanner.lookAhead();
         }
         return expr;
     }
     
     
     
     /**
      * Parses CONJUNCTION precedence level operators.
      * <pre>
      * conjunction -> disjunction (CONJ_OP disjunction)*   // conjunction (||)
      * </pre>
      * @return The parsed operator call or the result from the next higher precedence 
      *          level if no CONJ_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseConjunction() throws ParseException {
         Expression expr = this.parseDisjunction();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.CONJUNCTION)) {
             this.scanner.consume();
             
             final Expression rhs = this.parseDisjunction();
             
             expr = OperatorCall.binary(
                 new Position(expr.getPosition(), rhs.getPosition()), 
                 OpType.fromToken(la), expr, rhs);
             
             la = this.scanner.lookAhead();
         }
         return expr;
     }
     
     
     
     /**
      * Parses DISJUNCTION precedence level operators.
      * <pre>
      * disjunction -> secTerm (DISJ_OP secTerm)*     // disjunction (&&)
      * </pre>
      * @return The parsed operator call or the result from the next higher precedence 
      *          level if no DISJ_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseDisjunction() throws ParseException {
         Expression expr = this.parseSecTerm();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.DISJUNCTION)) {
             this.scanner.consume();
             
             final Expression rhs = this.parseSecTerm();
             
             expr = OperatorCall.binary(
                 new Position(expr.getPosition(), rhs.getPosition()), 
                 OpType.fromToken(la), expr, rhs);
             
             la = this.scanner.lookAhead();
         }
         return expr;
     }
     
     
     
     /**
      * Parses SECTERM precedence level operators.
      * <pre>
      * secTerm -> term (SECTERM_OP term)*   // plus minus
      * </pre>
      * @return The parsed operator call or the result from the next higher precedence 
      *          level if no SECTERM_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseSecTerm() throws ParseException {
         Expression expr = this.parseTerm();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.SECTERM)) {
             this.scanner.consume();
             
             final Expression rhs = this.parseTerm();
             
             expr = OperatorCall.binary(
                 new Position(expr.getPosition(), rhs.getPosition()), 
                 OpType.fromToken(la), expr, rhs);
             
             la = this.scanner.lookAhead();
         }
         return expr;
     }
     
     
     
     /**
      * Parses TERM precedence level operators.
      * <pre>
      * term -> factor (TERM_OP factor)*  // multiplication and co
      * </pre>
      * @return The parsed operator call or the result from the next higher precedence 
      *          level if no TERM_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseTerm() throws ParseException {
         Expression expr = this.parseFactor();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.TERM)) {
             // ISSUE 0000099: If Identifier or open brace, do not consume the token but
             //                pretend it was a multiplication
             if (la.matches(TokenType.IDENTIFIER) || la.matches(TokenType.OPENBR)) {
                 la = new Token(TokenType.MUL, la.getPosition());
             } else {
                 this.scanner.consume();
             }
             final Expression rhs = this.parseFactor();
             
             expr = OperatorCall.binary(
                 new Position(expr.getPosition(), rhs.getPosition()), 
                 OpType.fromToken(la), expr, rhs);
             
             la = this.scanner.lookAhead();
         }
         return expr;
     }
     
     
     
     /**
      * Parses FACTOR precedence operators. Result will be a nested OperatorCall or the
      * result of the next higher precedence level if no FACTOR_OP was found.
      * FACTOR operators are right-associative.
      * <pre>
      * factor -> postfix (FACTOR_OP factor)?    // right-associative (power operator)
      * </pre>
      * @return The parsed operator call or the result from the next higher precedence 
      *          level if no FACTOR_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseFactor() throws ParseException {
         Expression expr = this.parsePostfix();
         
         Token la = this.scanner.lookAhead();
         if (this.operators.match(la, PrecedenceLevel.FACTOR)) {
             this.scanner.consume();
             final Expression rhs = this.parseFactor();
             
             expr = OperatorCall.binary(
                 new Position(expr.getPosition(), rhs.getPosition()), 
                 OpType.fromToken(la), expr, rhs);
         }
         return expr;
     }
     
     
     
     /**
      * Parses a postfix operator. This may be either of the random index- or the
      * concrete index operator.
      * <pre>
      * postfix -> autolist (POSTFIX_OP autolist)*       // postfix operator
      * </pre>
      * @return Either the parsed postifx operator call or the expression from the next
      *          higher precedence level if no POSTFIX_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parsePostfix() throws ParseException {
         Expression lhs = this.parseAutoList();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.POSTFIX)) {
             this.scanner.consume();
             
             if (la.matches(TokenType.OPENSQBR)) {
                 // index operator
                 final Expression rhs = this.parseAutoList();
                 
                 lhs = OperatorCall.binary(
                     new Position(lhs.getPosition(), rhs.getPosition()), 
                     OpType.fromToken(la), lhs, rhs);
                 
                 this.expect(TokenType.CLOSEDSQBR);
             } else {
                 // ? or ?! operator
                 final Position endPos = this.scanner.spanFrom(la);
                 return OperatorCall.unary(
                     new Position(lhs.getPosition(), endPos), 
                     OpType.fromToken(la), lhs, true);
             }
             la = this.scanner.lookAhead();
         }
         
         return lhs;
     }
     
     /**
      * Parses an implicit list literal.
      * <pre>
      * autolist -> dotdot (';' dotdot)*   // implicit list literal
      * </pre>
      * @return Either a {@link ListLiteral} containing the following expressions or the 
      *          expression returned by the next higher precedence level.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseAutoList() throws ParseException {
         Expression lhs = this.parseDotDot();
         
         final Token la = this.scanner.lookAhead();
         if (la.matches(TokenType.SEMICOLON)) {
             final List<Expression> content = new ArrayList<Expression>();
             content.add(lhs);
 
             Expression last = null;
             while (this.scanner.match(TokenType.SEMICOLON)) {
                 last = this.parseDotDot();
                 content.add(last);
             }
             
             // invariant: last cannot be null here!
             return new ListLiteral(
                 new Position(lhs.getPosition(), last.getPosition()), 
                 content);
         }
         
         return lhs;
     }
     
     
     
     
     /**
      * Parses the '..' range operator, which can either be a binary operator or
      * a ternary operator if an additional step size is recognized.
      * 
      * <pre>
      * dotdot -> unary ('..' unary ('$' unary)?)?   // range operator with optional 
      *                                              // step size
      * </pre>
      * @return The parsed operator or the expression from the next higher precedence level
      *          if no DOTDOT operator was found. 
      * @throws ParseException If parsing fails.
      */
     protected Expression parseDotDot() throws ParseException {
         final Expression lhs = this.parseUnary();
         
         final Token la = this.scanner.lookAhead();
         if (this.operators.match(la, PrecedenceLevel.DOTDOT)) {
             this.scanner.consume();
             
             final Expression endRange = this.parseUnary();
             
             // default step width of 1 (if dollar is ommitted)
             Expression operand3 = new NumberLiteral(endRange.getPosition(), 1.0);
             if (this.scanner.match(TokenType.DOLLAR)) {
                 operand3 = this.parseUnary();
             }
             return OperatorCall.ternary(
                 new Position(lhs.getPosition(), operand3.getPosition()), 
                 OpType.fromToken(la), lhs, endRange, operand3);
         }
         return lhs;
     }
     
     
     
     /**
      * Parses an unary operator call.
      * <pre>
      * unary -> UNARY_OP unary    // right-associative unary operator
     *        | call
      * </pre>
      * @return A unary operator call or the expression returned by the next higher
      *          precedence level if no UNARY_OP was found.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseUnary() throws ParseException {
         final Token la = this.scanner.lookAhead();
         
         if (this.operators.match(la, PrecedenceLevel.UNARY)) {
             this.scanner.consume();
             final Expression rhs = this.parseUnary();
             return OperatorCall.unary(new Position(la.getPosition(), 
                     rhs.getPosition()), OpType.fromToken(la), rhs, false);
         } else {
             return this.parseCall();
         }
     }
     
     
     
     /**
      * Parses a function call. If no open braces was matched, the result of the next
      * higher precedence level will be returned.
      * 
      * <pre>
      * call -> access ( '(' parameters ')' )?
      * </pre>
      * @return The call statement of the result of the next higher precedence level if
      *          this was no call.
      * @throws ParseException If parsing fails
      */
     protected Expression parseCall() throws ParseException {
         final Expression lhs = this.parseNamespaceAccess();
         
         final Token la = this.scanner.lookAhead();
         if (this.scanner.match(TokenType.OPENBR)) {
             final List<Expression> params = this.parseExpressionList(
                 TokenType.CLOSEDBR);
             final ProductLiteral pl = new ProductLiteral(
                 this.scanner.spanFrom(la), params);
             this.expect(TokenType.CLOSEDBR);
             
             return new Call(
                 new Position(lhs.getPosition().getStart(), this.scanner.getStreamIndex()), 
                 lhs, pl);
         }
         return lhs;
     }
     
     
     
     /**
      * Parses a {@link Namespace} access.
      * <pre>
      * access -> literal ('.' literal)?
      * </pre>
      * 
      * @return The parsed literal if no DOT operator was found, or a {@link Namespace}
      *          access if the was a dot followed by a VarOrCall.
      * @throws ParseException If parsing fails
      */
     protected Expression parseNamespaceAccess() throws ParseException {
         final Expression lhs = this.parseLiteral();
         
         final Token la = this.scanner.lookAhead();
         if (this.scanner.match(TokenType.DOT)) {
             final Expression rhs = this.parseLiteral();
             
             return new NamespaceAccess(new Position(lhs.getPosition(), 
                 this.scanner.spanFrom(la)), lhs, rhs);
         }
         
         return lhs;
     }
     
     
     
     /**
      * Parses the highest precedence level which is mostly a single literal, but also 
      * a delete or if statement.
      * 
      * <pre>
      * literal -> ID                                       // VarAccess
      *          | ESCAPED                                  // token escape
      *          | '(' relation ')'                         // braced expression
      *          | '\(' parameters ':' relation ')'         // lambda function literal
      *          | '{' exprList '}'                         // concrete list of expressions
      *          | DELETE PUBLIC? ID (',' PUBLIC? ID)*      // delete operator
      *          | INSPECT PUBLIC ID                        // inspect for public
      *          | INSPECT ID ('.' ID)?                     // inspect operator
      *          | IF expr ':' relation ':' relation        // conditional operator
      *          | TRUE | FALSE                             // boolean literal
      *          | CHANNEL                                  // channel literal
      *          | USER                                     // user literal
      *          | STRING                                   // string literal
      *          | NUMBER                                   // number literal
      *          | DATETIME                                 // date literal
      *          | TIMESPAN                                 // timespan literal
      *          | '?'                                      // HELP literal
      *          | RADIX literal                            // radixed int
      * </pre>
      *  
      * @return The parsed expression.
      * @throws ParseException If parsing fails.
      */
     protected Expression parseLiteral() throws ParseException {      
         final Token la = this.scanner.lookAhead();
         Expression exp = null;
         
         switch(la.getType()) {
         case ESCAPED:
             this.scanner.consume();
             final EscapedToken escaped = (EscapedToken) la;
             final ResolvableIdentifier escId = new ResolvableIdentifier(la.getPosition(), 
                 escaped.getEscaped().getStringValue(), true);
             return new VarAccess(la.getPosition(), escId);
             
         case IDENTIFIER:
             this.scanner.consume();
             final ResolvableIdentifier id = new ResolvableIdentifier(
                     la.getPosition(), la.getStringValue(), false);
             return new VarAccess(id.getPosition(), id);
             
         case OPENBR:
             this.scanner.consume();
             /*
              * Now we can ignore whitespaces until the matching closing brace is 
              * read.
              */
             this.enterExpression();
             
             exp = this.parseRelation();
             this.expect(TokenType.CLOSEDBR);
             
             this.leaveExpression();
             return new Braced(this.scanner.spanFrom(la), exp);
             
         case LAMBDA:
             this.scanner.consume();
             
             this.enterExpression();
             
             final Collection<Declaration> formal = this.parseParameters(
                 TokenType.COLON);
             this.expect(TokenType.COLON);
             
             exp = this.parseRelation();
             
             this.expect(TokenType.CLOSEDBR);
             
             final FunctionLiteral func = new FunctionLiteral(
                 this.scanner.spanFrom(la), formal, exp);
             
             this.leaveExpression();
             
             return func;
             
         case OPENCURLBR:
             this.scanner.consume();
             
             this.enterExpression();
             final List<Expression> elements = this.parseExpressionList(
                 TokenType.CLOSEDCURLBR);
             
             this.expect(TokenType.CLOSEDCURLBR);
             this.leaveExpression();
             
             final ListLiteral list = new ListLiteral(this.scanner.spanFrom(la), 
                 elements);
             
             list.setPosition(this.scanner.spanFrom(la));
             return list;
             
         case DELETE:
             this.scanner.consume();
             this.allowSingleWhiteSpace();
             
             final List<DeleteableIdentifier> ids = new ArrayList<DeleteableIdentifier>();
             do {
                 boolean global = this.scanner.match(TokenType.PUBLIC);
                 if (global) {
                     this.allowSingleWhiteSpace();
                 }
                 ids.add(new DeleteableIdentifier(this.expectIdentifier(), global));
             } while (this.scanner.match(TokenType.COMMA));
                 
             
             return new Delete(this.scanner.spanFrom(la), ids);
             
         case INSPECT:
             this.scanner.consume();
             this.allowSingleWhiteSpace();
             
             final Token glob = this.scanner.lookAhead();
             final boolean global = this.scanner.match(TokenType.PUBLIC);
             this.allowSingleWhiteSpace();
             
             final ResolvableIdentifier name = new ResolvableIdentifier(
                 this.expectIdentifier());
             final VarAccess va1 = new VarAccess(name.getPosition(), name);
             Expression result = va1;
             
             if (global) {
                 // syntactic sugar for global inspect
                 final ResolvableIdentifier name2 = new ResolvableIdentifier(
                     glob.getPosition(), Namespace.PUBLIC_NAMESPACE_NAME);
                 final VarAccess va2 = new VarAccess(name2.getPosition(), name2);
                 result = new NamespaceAccess(this.scanner.spanFrom(la), va2, va1);
                 
             } else if (this.scanner.match(TokenType.DOT)) {
                 final ResolvableIdentifier name2 = new ResolvableIdentifier(
                     this.expectIdentifier());
                 final VarAccess va2 = new VarAccess(name2.getPosition(), name2);
                 
                 result = new NamespaceAccess(this.scanner.spanFrom(la), va1, va2);
             }
             return new Inspect(this.scanner.spanFrom(la), result, global);
             
         case IF:
             this.scanner.consume();
             this.allowSingleWhiteSpace();
             
             final Expression condition = this.parseRelation();
             this.allowSingleWhiteSpace();
             
             this.expect(TokenType.COLON);
             this.allowSingleWhiteSpace();
             
             final Expression second = this.parseRelation();
             
             this.allowSingleWhiteSpace();
             this.expect(TokenType.COLON);
             this.allowSingleWhiteSpace();
             
             final Expression third = this.parseRelation();
             
             return OperatorCall.ternary(this.scanner.spanFrom(la), OpType.IF, 
                 condition, second, third);
             
         case TRUE:
             this.scanner.consume();
             return new BooleanLiteral(la.getPosition(), true);
         case FALSE:
             this.scanner.consume();
             return new BooleanLiteral(la.getPosition(), false);
                 
         case CHANNEL:
             this.scanner.consume();
             return new ChannelLiteral(la.getPosition(), la.getStringValue());
 
         case USER:
             this.scanner.consume();
             return new UserLiteral(la.getPosition(), la.getStringValue());
             
         case STRING:
             this.scanner.consume();
             return new StringLiteral(la.getPosition(), la.getStringValue());
 
         case NUMBER:
             this.scanner.consume();
             return new NumberLiteral(la.getPosition(), la.getFloatValue());
             
         case DATETIME:
             this.scanner.consume();
             return new DateLiteral(la.getPosition(), la.getDateValue());
             
         case TIMESPAN:
             this.scanner.consume();
             return new TimespanLiteral(la.getPosition(), (int)la.getLongValue());
         case QUESTION:
             this.scanner.consume();
             return new HelpLiteral(la.getPosition());
         case RADIX:
             this.scanner.consume();
             final NumberLiteral radix = new NumberLiteral(la.getPosition(), 
                 la.getLongValue());
             final Expression rhs = this.parseLiteral();
             return OperatorCall.binary(this.scanner.spanFrom(la), OpType.RADIX, 
                 radix, rhs);
         default:
             this.expect(TokenType.LITERAL);
         }
         
         return null;
     }
     
     
     
     /**
      * Parses a comma separated list of expressions. The <code>end</code> token type
      * exists only for determining empty lists.
      * 
      * <pre>
      * exprList -> end   // empty list
      *           | relation (',' relation)*
      * </pre>
      * @param end The token which should end the list. Only used to determine empty lists.
      * @return A collection of parsed expressions.
      * @throws ParseException If parsing fails.
      */
     protected List<Expression> parseExpressionList(TokenType end) 
             throws ParseException {
         
         // do not consume here. end token is consume by the caller
         if (this.scanner.lookAhead().matches(end)) {
             // empty list
             return new ArrayList<Expression>(0);
         }
         
         final List<Expression> result = new ArrayList<Expression>();
         
         result.add(this.parseRelation());
         
         while (this.scanner.match(TokenType.COMMA)) {
             this.allowSingleWhiteSpace();
             result.add(this.parseRelation());
         }
         return result;
     }
     
     
     
     /**
      * Parses a list of formal parameters that ends with the token type <code>end</code>.
      * 
      * <pre>
      * parameters -> end   // empty list
      *             | parameter (',' parameter)*
      * </pre>
      * @param end The token that the list is supposed to end with (to determine empty 
      *          lists, token won't be consumed if hit).
      * @return Collection of parsed formal parameters.
      * @throws ParseException If parsing fails.
      */
     protected List<Declaration> parseParameters(TokenType end) throws ParseException {
         if (this.scanner.lookAhead().matches(end)) {
             // empty list.
             return new ArrayList<Declaration>(0);
         }
         
         this.enterExpression();
         final List<Declaration> result = new ArrayList<Declaration>();
         result.add(this.parseParameter());
         
         while (this.scanner.match(TokenType.COMMA)) {
             this.allowSingleWhiteSpace();
             result.add(this.parseParameter());
         }
         this.leaveExpression();
         return result;
     }
     
     
     
     /**
      * <pre>
      * parameter -> type ID
      * </pre>
      * @return The parsed parameter.
      * @throws ParseException If parsing fails.
      */
     protected Declaration parseParameter() throws ParseException {
         final Token la = this.scanner.lookAhead();
         final Type type = this.parseType();
         final Identifier name = this.expectIdentifier();
         
         return new Declaration(this.scanner.spanFrom(la), name, 
             new Empty(type, this.scanner.spanFrom(la)));
     }
     
     
     
     /**
      * <pre>
      * type -> ID                                  // primitive type
      *       | LIST '&lt;' type '&gt;'             // list type
      *       | '(' type (WS type)* '->' type ')'   // function type
      * </pre>
      * @return A resolvable type.
      * @throws ParseException If parsing fails.
      */
     protected Type parseType() throws ParseException {
         final Token la = this.scanner.lookAhead();
         
         if (this.scanner.match(TokenType.OPENBR)) {
             final List<Type> signature = new ArrayList<Type>();
             final boolean skipWS = this.scanner.skipWhiteSpaces();
             this.scanner.setSkipWhiteSpaces(false);
             
             do {
                 signature.add(this.parseType());
             } while (this.scanner.match(TokenType.SEPERATOR) &&
                 !this.scanner.lookAhead().matches(TokenType.ASSIGNMENT));
             
             this.scanner.setSkipWhiteSpaces(skipWS);
             this.allowSingleWhiteSpace();
             this.expect(TokenType.ASSIGNMENT);
             final Type resultType = this.parseType();
             this.allowSingleWhiteSpace();
             
             this.expect(TokenType.CLOSEDBR);
             return new MapType(new ProductType(signature), 
                 resultType);
         } else if (this.scanner.match(TokenType.LIST)) {
             this.expect(TokenType.LT);
             final Type subType = this.parseType();
             this.expect(TokenType.GT);
             return new ListType(subType);
         } else if (la.matches(TokenType.IDENTIFIER)) {
             final ResolvableIdentifier id = new ResolvableIdentifier(
                 this.expectIdentifier());
             
             return Type.resolve(id, false);
         } else {
             throw new ParseException("Typ erwartet", la.getPosition());
         }
     }
 }
