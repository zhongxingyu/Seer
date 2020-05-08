 package org.libj.xquery.parser;
 
 import org.libj.xquery.lexer.Lexer;
 import org.libj.xquery.lexer.Token;
 import org.libj.xquery.lisp.Cons;
 
 import java.io.IOException;
 
 import static org.libj.xquery.lexer.TokenType.*;
 
 public class Parser extends LLkParser {
     public Parser(Lexer lexer) throws IOException {
         super(lexer, 2);
     }
 
     public Cons xquery() throws IOException {
         Cons ast = AST.createAST(PROG);
         ast = Cons.append(ast, declares());
         ast = Cons.append(ast, expr());
         match(EOF);
         return ast;
     }
 
     private Cons primary() throws IOException {
         switch (LA(1)) {
             case NUMBER:
                 return number();
             case STRING:
                 return string();
             case VARIABLE:
                 return variable();
             case LPAREN:
                 return list();
             case LBRACK:
                 match(LBRACK);
                 Cons ast = expr();
                 match(RBRACK);
                 return ast;
             case WORD:
                 if (LA(2) == LPAREN) {
                     return call();
                 }
             case TAGOPEN:
             case TAGUNIT:
                 return node();
             default:
                 throw new ParserException("Unexpected primary expr token: " + LT(1));
         }
     }
 
     private Cons expr() throws IOException {
         switch (LA(1)) {
             case FOR:
             case LET:
                 return flower();
             case IF:
                 return ifExpr();
             default:
                 return or();
         }
     }
 
     private Cons or() throws IOException {
         Cons ast = and();
         while (LA(1) == OR) {
             ast = binary(ast, consume(), and());
         }
         return ast;
     }
 
     private Cons and() throws IOException {
         Cons ast = compare();
         while (LA(1) == AND) {
             ast = binary(ast, consume(), compare());
         }
         return ast;
     }
 
     private Cons compare() throws IOException {
         Cons ast = range();
         if (LA(1) == EQ || LA(1) == NE || LA(1) == LT || LA(1) == LE || LA(1) == GT || LA(1) == GE) {
             ast = binary(ast, consume(), range());
         }
         return ast;
     }
 
     private Cons range() throws IOException {
         Cons ast = add();
         if (LA(1) == TO) {
             ast = binary(ast, consume(), add());
         }
         return ast;
     }
 
     private Cons add() throws IOException {
         Cons ast = multiply();
         while (LA(1) == PLUS || LA(1) == MINUS) {
             ast = binary(ast, consume(), multiply());
         }
         return ast;
     }
 
     private Cons multiply() throws IOException {
         Cons ast = unary();
         while (LA(1) == MULTIPLY || LA(1) == DIV || LA(1) == MOD) {
             ast = binary(ast, consume(), unary());
         }
         return ast;
     }
 
     private Cons unary() throws IOException {
         if (LA(1) == MINUS) {
             Cons ast = AST.createAST(NEGATIVE);
             consume();
             ast = Cons.append(ast, value());
             return ast;
         }
         else {
             return value();
         }
     }
 
     private Cons value() throws IOException {
         Cons ast = xpath();
         while (LA(1) == LBRACKET) {
             match(LBRACKET);
             Cons index = add();
             match(RBRACKET);
             ast = binary(ast, Token.t(INDEX), index);
         }
         return ast;
     }
 
     private Cons binary(Cons left, Token op, Cons right) throws IOException {
         Cons root = AST.createAST(op);
         root = Cons.append(root, left);
         root = Cons.append(root, right);
         return root;
     }
 
     private Cons ifExpr() throws IOException {
         Cons ast = AST.createAST(consume(IF));
         match(LPAREN);
         ast = Cons.append(ast, expr());
         match(RPAREN);
         match(THEN);
         ast = Cons.append(ast, expr());
         match(ELSE);
         ast = Cons.append(ast, expr());
         return ast;
     }
 
     private Cons body() throws IOException {
         switch (LA(1)) {
             case FOR:
                 return forIn();
             case LET:
                 return let();
             default:
                 match(RETURN);
                 return expr();
         }
     }
 
     private Cons call() throws IOException {
         Cons ast = AST.createAST(CALL);
         ast = Cons.append(ast, AST.createAST(consume(WORD)));
         match(LPAREN);
         if (LA(1) == RPAREN) {
             match(RPAREN);
             return ast;
         }
         ast = Cons.append(ast, expr());
         while (LA(1) == COMMA) {
             match(COMMA);
             ast = Cons.append(ast, primary());
         }
         match(RPAREN);
         return ast;
     }
 
     private Cons list() throws IOException {
         Cons ast = AST.createAST(LIST);
         match(LPAREN);
         if (LA(1) == RPAREN) {
             match(RPAREN);
             return ast;
         }
         ast = Cons.append(ast, expr());
         while (LA(1) == COMMA) {
             match(COMMA);
            ast = Cons.append(ast, primary());
         }
         match(RPAREN);
         if (ast.size() == 2) {
            return (Cons) AST.nthAST(ast, 1);
         }
         else {
             return ast;
         }
     }
 
     private Cons node() throws IOException {
         Cons ast = AST.createAST(new Token(NODE, null));
         if (LA(1) == TAGUNIT) {
             ast = Cons.append(ast, AST.createAST(consume(TAGUNIT)));
             return ast;
         }
         ast = Cons.append(ast, AST.createAST(consume(TAGOPEN)));
         while (LA(1) != TAGCLOSE) {
             ast = Cons.append(ast, nodeExpr());
         }
         // TODO: check if start and end tag matches
         ast = Cons.append(ast, AST.createAST(consume(TAGCLOSE)));
         return ast;
     }
 
     private Cons nodeExpr() throws IOException {
         switch (LA(1)) {
             case TAGOPEN: case TAGUNIT:
                 return node();
             case TEXT:
                 return AST.createAST(consume());
             case LBRACK:
                 match(LBRACK);
                 Cons ast = expr();
                 match(RBRACK);
                 return ast;
             default:
                 throw new ParserException("Unexpected node expr token: " + LT(1));
         }
     }
 
     private Cons string() throws IOException {
         return AST.createAST(consume(STRING));
     }
 
     private Cons number() throws IOException {
         return AST.createAST(consume(NUMBER));
     }
 
     private Cons xpath() throws IOException {
         Cons ast = primary();
         while (LA(1) == XPATH) {
             consume();
             Element path = tokenElement(WORD, consume(WORD).text);
             ast = Cons.list(tokenElement(XPATH, "xpath"), ast, path);
         }
         return ast;
     }
 
     private Cons variable() throws IOException {
         return AST.createAST(consume(VARIABLE));
     }
 
     private Cons flower() throws IOException {
         Cons ast = AST.createAST(FLOWER);
         Cons forlets = AST.createAST(FORLETS);
         while (LA(1) == FOR || LA(1) == LET) {
             if (LA(1) == FOR) {
                 forlets = Cons.append(forlets, forIn());
             }
             else {
                 forlets = Cons.append(forlets, let());
             }
         }
         Cons where = where();
         Cons body = body();
 
         ast = Cons.append(ast, forlets);
         ast = Cons.append(ast, body);
         ast = Cons.append(ast, where);
 
         return ast;
     }
 
     private Cons let() throws IOException {
         Cons ast = AST.createAST(consume(LET));
         ast = Cons.append(ast, AST.createAST(consume(VARIABLE)));
         consume(ASSIGN);
         ast = Cons.append(ast, expr());
         return ast;
     }
 
     private Cons forIn() throws IOException {
         Cons ast = AST.createAST(consume(FOR));
         ast = Cons.append(ast, AST.createAST(consume(VARIABLE)));
         match(IN);
         ast = Cons.append(ast, expr());
         return ast;
     }
 
     private Cons where() throws IOException {
         if (LA(1) == WHERE) {
             match(WHERE);
             return expr();
         }
         else {
             return Cons.NIL;
         }
     }
 
     public Cons declares() throws IOException {
         Cons ast = AST.createAST(DECLARES);
         while (LA(1) == DECLARE) {
             ast = Cons.append(ast, declare());
         }
         return ast;
     }
 
     public Cons declare() throws IOException {
         if (LA(2) == NAMESPACE) {
             return declareNamespace();
         }
         else {
             return declareAnyOther();
         }
     }
 
     public Cons declareNamespace() throws IOException {
         Cons ast = AST.createAST(consume(DECLARE));
         ast = Cons.append(ast, AST.createAST(consume(NAMESPACE)));
         ast = Cons.append(ast, AST.createAST(consume(WORD)));
         match(EQ);
         ast = Cons.append(ast, AST.createAST(consume(STRING)));
         ast = Cons.append(ast, AST.createAST(consume(SEMI)));
         return ast;
     }
 
     public Cons declareAnyOther() throws IOException {
         Cons ast = AST.createAST(consume(DECLARE));
         ast = Cons.append(ast, AST.createAST(consume(WORD)));
         while (LA(1) != SEMI && LA(1) != EOF) {
             ast = Cons.append(ast, AST.createAST(LT(1)));
             consume();
         }
         match(SEMI);
         return ast;
     }
 
     private static Element tokenElement(int t, String text) {
         return new Element(new Token(t, text));
     }
 }
