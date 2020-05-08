 package xhl.core;
 
 import static xhl.core.Token.TokenType.*;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.*;
 
 import xhl.core.Token.TokenType;
 import xhl.core.elements.*;
 
 /**
  * XHL parser
  *
  * Grammar:
  *
  * <pre>
  *   program   ::= { statement }
  *   statement ::= block | expression LINEEND
  *   block     ::= application ':' LINEEND INDENT { statement } DEDENT
  *
  *   expression  ::= application { operator application }
  *   application ::= term { term }
  *   term        ::= literal | '(' expression ')'
  *
  *   literal ::= symbol | string | number | 'true' | 'false' | 'none'
  *              | list | map
  *   list ::= '[]' | '[' expression { ',' expression } ']'
  *   map  ::= '{}' | '{' key-value { ',' key-value } '}'
  *   key-value ::= expression ':' expression
  * </pre>
  *
  * @author Sergej Chodarev
  */
 public class Reader {
     private Lexer lexer;
     private Token token;
 
     private static final Set<TokenType> termH;
     static {
         TokenType elements[] =
                 { SYMBOL, STRING, NUMBER, TRUE, FALSE, NONE, BRACKET_OPEN,
                         BRACE_OPEN, PAR_OPEN };
         termH = new HashSet<TokenType>(Arrays.asList(elements));
     }
 
     public List<Statement> read(java.io.Reader input) throws IOException {
         lexer = new Lexer(input);
         token = lexer.nextToken();
         return program();
     }
 
     public List<Statement> read(String code) throws IOException {
         return read(new StringReader(code));
     }
 
     private List<Statement> program() throws IOException {
         List<Statement> lists = new LinkedList<Statement>();
         while (token != null && token.type != DEDENT) {
             lists.add(expressionOrStatement(true));
         }
         return lists;
     }
 
     private Statement expressionOrStatement(boolean statement)
             throws IOException {
         Expression first = application();
         while (token.type == OPERATOR) {
             CodeList exp = new CodeList(token.position);
             Symbol op = new Symbol(token.stringValue, token.position);
             token = lexer.nextToken();
             Expression second = application();
             exp.add(op);
             exp.add(first);
             exp.add(second);
             first = exp;
         }
         if (token.type == LINEEND)
             token = lexer.nextToken();
         else if (token.type == COLON) {
             token = lexer.nextToken(); // :
             token = lexer.nextToken(); // \n
             token = lexer.nextToken(); // INDENT FIXME: Add checks
             List<Statement> body = program();
             token = lexer.nextToken(); // DEDENT FIXME: Add checks
             Block block = new Block(first, body, first.getPosition());
             return block;
         }
         return first;
     }
 
     private Expression application() throws IOException {
         CodeList list = new CodeList(token.position);
         while (termH.contains(token.type)) {
             list.add(term());
         }
         if (list.size() == 1)
             return (Expression) list.head();
         else
             return list;
     }
 
     private DataList datalist() throws IOException {
         DataList list = new DataList(token.position);
         token = lexer.nextToken(); // [
         if (token.type == BRACKET_CLOSE) { // Empty list
             token = lexer.nextToken(); // ]
             return list;
         }
         // Non-empty list
         list.add(term());
         while (token.type != TokenType.BRACKET_CLOSE) {
             token = lexer.nextToken(); // ,
             list.add(term());
         }
         token = lexer.nextToken(); // ]
         return list;
     }
 
     private LMap map() throws IOException {
         LMap map = new LMap(token.position);
         token = lexer.nextToken(); // {
         if (token.type == BRACE_CLOSE) { // Empty map
             token = lexer.nextToken(); // }
             return map;
         }
         // Non-empty map
         keyValue(map);
         while (token.type != TokenType.BRACE_CLOSE) {
             token = lexer.nextToken(); // ,
             keyValue(map);
         }
         token = lexer.nextToken(); // }
         return map;
     }
 
     private void keyValue(LMap map) throws IOException {
         Expression key = term();
         token = lexer.nextToken(); // :
         Expression value = term();
         map.put(key, value);
     }
 
     private Expression term() throws IOException {
         Expression sexp = null;
         switch (token.type) {
         case SYMBOL:
             sexp = new Symbol(token.stringValue, token.position);
             token = lexer.nextToken();
             break;
         case STRING:
             sexp = new LString(token.stringValue, token.position);
             token = lexer.nextToken();
             break;
         case NUMBER:
             sexp = new LNumber(token.doubleValue, token.position);
             token = lexer.nextToken();
             break;
         case TRUE:
             sexp = new LBoolean(true, token.position);
             token = lexer.nextToken();
             break;
         case FALSE:
             sexp = new LBoolean(false, token.position);
             token = lexer.nextToken();
             break;
         case PAR_OPEN:
             token = lexer.nextToken(); // (
            sexp = (Expression) expressionOrStatement(false);
             token = lexer.nextToken(); // )
             break;
         case BRACKET_OPEN:
             sexp = datalist();
             break;
         case BRACE_OPEN:
             sexp = map();
             break;
         }
         return sexp;
     }
 }
