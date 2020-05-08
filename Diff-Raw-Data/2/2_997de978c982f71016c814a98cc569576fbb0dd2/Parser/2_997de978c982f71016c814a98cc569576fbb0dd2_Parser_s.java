 package miniJava.SyntacticAnalyzer;
 
 import miniJava.ErrorReporter;
 import miniJava.SyntacticAnalyzer.Token;
 import miniJava.SyntacticAnalyzer.SourcePosition;
 import miniJava.SyntacticAnalyzer.SyntaxError;
 import miniJava.SyntacticAnalyzer.Scanner;
 
 public class Parser {
 
     private Scanner lexicalAnalyzer;
     private Token currentToken;
     private ErrorReporter errorReporter;
     private SourcePosition previousTokenPosition;
 
     public Parser(Scanner lexer, ErrorReporter reporter) {
         lexicalAnalyzer = lexer;
         errorReporter = reporter;
         previousTokenPosition = new SourcePosition();
     }
 
     void syntacticError(String messageTemplate, 
                         String tokenQuoted) throws SyntaxError {
         SourcePosition pos = currentToken.position;
         errorReporter.reportError(messageTemplate, tokenQuoted, pos);
         throw(new SyntaxError());
     }
 
     void accept(int tokenExpected) throws SyntaxError {
         System.out.println("inside accept: " + Token.spell(currentToken.kind));
         if(currentToken.kind == tokenExpected) {
             previousTokenPosition = currentToken.position;
             currentToken = lexicalAnalyzer.scan();
         } else {
             syntacticError("\"%\" expected here", Token.spell(tokenExpected));
         }
     }
 
     void acceptIt() {
         System.out.println("inside acceptIt: " + 
             Token.spell(currentToken.kind));
         previousTokenPosition = currentToken.position;
         currentToken = lexicalAnalyzer.scan();
     }
 
     // start records the position of the start of a phrase.
     // This is defined to be the position of the first
     // character of the first token of the phrase.
     void start(SourcePosition position) {
         position.start = currentToken.position.start;
     }
 
     // finish records the position of the end of a phrase.
     // This is defined to be the position of the last
     // character of the last token of the phrase.
     void finish(SourcePosition position) {
         position.finish = previousTokenPosition.finish;
     }
 
     public void parse() {
         currentToken = lexicalAnalyzer.scan();
 
         try {
             parseProgram();
         }
         catch (SyntaxError s) {
            System.out.println("The syntax error has been catched...");
         }
     }
 
     private void parseProgram() throws SyntaxError {
         while(currentToken.kind == Token.CLASS) {
             parseClassDeclaration();
         }
         accept(Token.EOT);
     }
 
     private void parseClassDeclaration() throws SyntaxError {
         accept(Token.CLASS);
         parseIdentifier();
         accept(Token.LCURLY);
 
         while(isStarterDeclarators(currentToken.kind)) {
             parseDeclarators();
             parseIdentifier();
 
             switch(currentToken.kind) {
             case Token.SEMICOLON:
                 acceptIt();
                 break;
 
             case Token.LPAREN:
                 acceptIt();
 
                 if(isStarterParameterList(currentToken.kind))
                     parseParameterList();
 
                 accept(Token.RPAREN);
                 accept(Token.LCURLY);
 
                 while(isStarterStatement(currentToken.kind))
                     parseStatement();
 
                 if(currentToken.kind == Token.RETURN) {
                     acceptIt();
                     parseExpression();
                     accept(Token.SEMICOLON);
                 }
 
                 accept(Token.RCURLY);
                 break;
 
             default:
                 syntacticError("\"%\" cannot be used here. You need a ; or (", 
                     currentToken.spelling);
                 break;
 
             }
         }
         accept(Token.RCURLY);
     }
 
     private void parseDeclarators() throws SyntaxError {
         if(currentToken.kind == Token.PUBLIC 
             || currentToken.kind == Token.PRIVATE)
             acceptIt();
 
         if(currentToken.kind == Token.STATIC)
             acceptIt();
 
         parseType();
     }
 
     private void parseType() throws SyntaxError {
         switch(currentToken.kind) {
         case Token.BOOLEAN:
         case Token.VOID:
             acceptIt();
             break;
 
         case Token.IDENTIFIER:
             parseIdentifier();
             if(currentToken.kind == Token.LBRACKET) {
                 acceptIt();
                 accept(Token.RBRACKET);
             }
             break;
 
         case Token.INT:
             acceptIt();
             if(currentToken.kind == Token.LBRACKET) {
                 acceptIt();
                 accept(Token.RBRACKET);
             }
             break;
 
         default:
             syntacticError("\"%\" cannot start a type", currentToken.spelling);
             break;
         }
     }
 
     private void parseParameterList() throws SyntaxError {
         parseType();
         parseIdentifier();
 
         while(currentToken.kind == Token.COMMA) {
             acceptIt();
             parseType();
             parseIdentifier();
         }
     }
 
     private void parseArgumentList() throws SyntaxError {
         parseExpression();
 
         while(currentToken.kind == Token.COMMA) {
             acceptIt();
             parseExpression();
         }
     }
 
     private void parseReference() throws SyntaxError {
         if(currentToken.kind == Token.THIS)
             acceptIt();
         else if(currentToken.kind == Token.IDENTIFIER)
             acceptIt();
         else
             syntacticError("\"%\" cannot start a reference", 
                 currentToken.spelling);
 
         while(currentToken.kind == Token.DOT) {
             acceptIt();
             parseIdentifier();
         }
     }
 
     private void parseStatement() throws SyntaxError {
         switch(currentToken.kind) {
         case Token.LCURLY:
             acceptIt();
             while(isStarterStatement(currentToken.kind))
                 parseStatement();
             accept(Token.RCURLY);
             break;
 
         case Token.IF:
             acceptIt();
             accept(Token.LPAREN);
             parseExpression();
             accept(Token.RPAREN);
             parseStatement();
             if(currentToken.kind == Token.ELSE) {
                 acceptIt();
                 parseStatement();
             }
             break;
 
         case Token.WHILE:
             acceptIt();
             accept(Token.LPAREN);
             parseExpression();
             accept(Token.RPAREN);
             parseStatement();
             break;
 
         case Token.BOOLEAN:  // Statement ::= Type id = Expression;
         case Token.VOID:
         case Token.INT:
             parseType();
             parseIdentifier();
             accept(Token.ASSIGN);
             parseExpression();
             accept(Token.SEMICOLON);
             break;
 
         case Token.THIS:        // Statement ::= Reference ([Expression])? = 
             parseReference();   // Expression; | (ArgumentList?);
             switch(currentToken.kind) {
             case Token.LBRACKET:
             case Token.ASSIGN:
                 if(currentToken.kind == Token.LBRACKET) {
                     acceptIt();
                     parseExpression();
                     accept(Token.RBRACKET);
                 }
                 accept(Token.ASSIGN);
                 parseExpression();
                 accept(Token.SEMICOLON);
                 break;
 
             case Token.LPAREN:
                 acceptIt();
                 if(isStarterArgumentList(currentToken.kind))
                     parseArgumentList();
                 accept(Token.RPAREN);
                 accept(Token.SEMICOLON);
                 break;
 
             default:
                 syntacticError("need [, =, or ( instead of \"%\"", 
                     currentToken.spelling);
             }
             break;
 
         case Token.IDENTIFIER:
             parseIdentifier();
             switch(currentToken.kind) {
             case Token.LBRACKET:
                 acceptIt();
                 switch(currentToken.kind) {
                 case Token.RBRACKET: // Statement ::= id [] id = Expression;
                     acceptIt();
                     parseIdentifier();
                     accept(Token.ASSIGN);
                     parseExpression();
                     accept(Token.SEMICOLON);
                     break;
 
                 case Token.THIS: // Statement ::= id [Expression] = Expression;
                 case Token.IDENTIFIER: // Starters of Expression
                 case Token.NOT:
                 case Token.MINUS:
                 case Token.LPAREN:
                 case Token.INT:
                 case Token.TRUE:
                 case Token.FALSE:
                 case Token.NEW:
                     parseExpression();
                     accept(Token.RBRACKET);
                     accept(Token.ASSIGN);
                     parseExpression();
                     accept(Token.SEMICOLON);
                     break;
                 }
                 break;
 
             case Token.IDENTIFIER: // Statement ::= id id = Expression;
                 parseIdentifier();
                 accept(Token.ASSIGN);
                 parseExpression();
                 accept(Token.SEMICOLON);
                 break;
 
             case Token.DOT: // Statement ::= id (. id)* ([Expression])? = 
                 while(currentToken.kind == Token.DOT) { // Expression; | (Ar?);
                     acceptIt();
                     parseIdentifier();
                 }
                 switch(currentToken.kind) {
                 case Token.LBRACKET:
                 case Token.ASSIGN:
                     if(currentToken.kind == Token.LBRACKET) {
                         acceptIt();
                         parseExpression();
                         accept(Token.RBRACKET);
                     }
                     accept(Token.ASSIGN);
                     parseExpression();
                     accept(Token.SEMICOLON);
                     break;
 
                 case Token.LPAREN:
                     acceptIt();
                     if(isStarterArgumentList(currentToken.kind))
                         parseArgumentList();
                     accept(Token.RPAREN);
                     accept(Token.SEMICOLON);
                     break;
                 }
                 break;
 
             case Token.ASSIGN: // Statement ::= id = Expression;
                 acceptIt();
                 parseExpression();
                 accept(Token.SEMICOLON);
                 break;
 
             case Token.LPAREN: // Statement ::= id (ArgumentList?);
                 acceptIt();
                 if(isStarterArgumentList(currentToken.kind))
                     parseArgumentList();
                 accept(Token.RPAREN);
                 accept(Token.SEMICOLON);
                 break;
 
             }
             break;
 
         default:
             syntacticError("\"%\" cannot start a statement", 
                 currentToken.spelling);
         }
     }
 
     private void parseExpression() throws SyntaxError {
         switch(currentToken.kind) {
         case Token.THIS: // Reference
         case Token.IDENTIFIER:
             parseReference();
             if(currentToken.kind == Token.LBRACKET) {
                 acceptIt();
                 parseExpression();
                 accept(Token.RBRACKET);
             } else if(currentToken.kind == Token.LPAREN) {
                 acceptIt();
                 if(isStarterArgumentList(currentToken.kind))
                     parseArgumentList();
                 accept(Token.RPAREN);
             }
             break;
 
         case Token.NOT:
         case Token.MINUS:
             acceptIt();
             parseExpression();
             break;
 
         case Token.LPAREN:
             acceptIt();
             parseExpression();
             accept(Token.RPAREN);
             break;
 
         case Token.INTLITERAL:
         case Token.TRUE:
         case Token.FALSE:
             acceptIt();
             break;
 
         case Token.NEW:
             acceptIt();
             if(currentToken.kind == Token.INT) {
                 acceptIt();
                 accept(Token.LBRACKET);
                 parseExpression();
                 accept(Token.RPAREN);
             } else if(currentToken.kind == Token.IDENTIFIER) {
                 acceptIt();
                 if(currentToken.kind == Token.LBRACKET) {
                     acceptIt();
                     parseExpression();
                     accept(Token.RBRACKET);
                 } else if(currentToken.kind == Token.LPAREN) {
                     acceptIt();
                     accept(Token.RPAREN);
                 }
             }
             break;
 
         default:
             syntacticError("\"%\" cannot start an expression", 
                 currentToken.spelling);
         }
 
         while(isBinop(currentToken.kind)) {
             acceptIt();
             parseExpression();
         }
     }
 
     private void parseIdentifier() throws SyntaxError {
         System.out.println("inside parseIdentifier: " + 
             Token.spell(currentToken.kind));
         if(currentToken.kind == Token.IDENTIFIER) {
             previousTokenPosition = currentToken.position;
             currentToken = lexicalAnalyzer.scan();
         } else {
             syntacticError("\"%\" expected here", 
                 Token.spell(Token.IDENTIFIER));
         }
     }
 
     private boolean isStarterDeclarators(int kind) {
         return kind == Token.PUBLIC
                 || kind == Token.PRIVATE
                 || kind == Token.STATIC
                 || isStarterType(kind);
     }
 
     private boolean isStarterType(int kind) {
         return kind == Token.BOOLEAN
                 || kind == Token.VOID
                 || kind == Token.INT
                 || kind == Token.IDENTIFIER;
     }
 
     private boolean isStarterParameterList(int kind) {
         return isStarterType(kind);
     }
 
     private boolean isStarterStatement(int kind) {
         return kind == Token.LCURLY
                 || kind == Token.IF
                 || kind == Token.WHILE
                 || isStarterType(kind)
                 || kind == Token.THIS;
     }
 
     private boolean isStarterArgumentList(int kind) {
         return isStarterReference(kind)
                 || kind == Token.NOT        // unop 
                 || kind == Token.MINUS // maybe should add Token NEGATIVE
                 || kind == Token.LPAREN 
                 || kind == Token.INTLITERAL 
                 || kind == Token.TRUE
                 || kind == Token.FALSE
                 || kind == Token.NEW;
     }
 
     private boolean isStarterReference(int kind) {
         return kind == Token.THIS || kind == Token.IDENTIFIER;
     }
 
     private boolean isBinop(int kind) {
         return kind == Token.GREATER
                 || kind == Token.LESS
                 || kind == Token.EQUAL 
                 || kind == Token.LEQUAL
                 || kind == Token.GEQUAL
                 || kind == Token.NOTEQUAL
                 || kind == Token.AND
                 || kind == Token.OR
                 || kind == Token.PLUS
                 || kind == Token.MINUS
                 || kind == Token.TIMES
                 || kind == Token.DIV;
     }
 }
