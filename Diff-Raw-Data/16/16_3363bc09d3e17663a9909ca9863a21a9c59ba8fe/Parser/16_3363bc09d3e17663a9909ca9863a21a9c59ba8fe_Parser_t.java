 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Parser;
 
 import Lexer.Lexer;
 import Lexer.Token;
 import Tree.*;
 import java.util.ArrayList;
 
 /**
  *
  * @author SergioJavier
  */
 public class Parser {
 
     private Token cToken;
     private Lexer lex;
     private int countError;
 
     public Parser(Lexer lex) throws Exception {
         this.lex = lex;
     }
     
     public Program Parse() throws Exception{
         System.out.println("Parsing ...");
         countError = 0;
         cToken = lex.getNextToken();
         Program p = S();
 
         if (cToken.getTipo() == Token.TokenType.EOF) {
             cToken = lex.getNextToken();
             if (countError == 0) {
                 System.out.println("Parsing Complete!");
             }
         } else {
             Error();
         }
         
         return p;
     }
 
     public Lexer getLex() {
         return lex;
     }
 
     private void Error() throws Exception {
         countError++;
         throw new Exception("Parser.java:" + cToken.getRow() + ":" + cToken.getColumn() + " Error: Simbolo inesperado '" + cToken.getLexema() + "'.\n");
     }
 
     private void Error(String mjs) throws Exception {
         countError++;
         throw new Exception("Parser.java:" + cToken.getRow() + ":" + cToken.getColumn() + " Error: " + mjs + ".\n");
     }
 
     /*
      * S ::= import java_code terminal non_terminal definitions
      */
     private Program S() throws Exception {
         Program program = new Program();
         
         program.setImportStatements( Import() );
         
         //Parser code es opcional
         if( cToken.getTipo() == Token.TokenType.Parser)
             program.setGlobalParserCode( JavaCode() );
         
         
         Terminals(program);
         program.setDefinitions(definitions() );
         
         return program;
     }
 
     /* 
      * import ::= ( IMPORT JAVACODE SEMICOLON )*
      */
     private ArrayList<Import> Import() throws Exception {
         ArrayList<Import> imports = new ArrayList<Import>();
 
         while (cToken.getTipo() == Token.TokenType.Import) {
             cToken = lex.getNextToken();
             String importStatement = cToken.getLexema();
 
             //JAVACODE SECTION
             while (cToken.getTipo() != Token.TokenType.Semicolon) {
                 importStatement += cToken.getLexema();
                 cToken = lex.getNextToken();
             }
 
             if (cToken.getTipo() == Token.TokenType.Semicolon) {
                 importStatement += cToken.getLexema();
                 cToken = lex.getNextToken();
                 imports.add(new Import("import " + importStatement));
             } else {
                 Error();
             }
         }
         return imports;
     }
     /*
      * parser_code = PARSER CODE JAVACODESTART JAVACODE JAVACODEEND SEMICOLON
      */
 
     private ParserCode JavaCode() throws Exception {
         ParserCode globalParserCode = null;
         if (cToken.getTipo() == Token.TokenType.Parser) {
             cToken = lex.getNextToken();
             
             if (cToken.getTipo() == Token.TokenType.Code) {
                 cToken = lex.getNextToken();
                 
                 if (cToken.getTipo() == Token.TokenType.JavaCodeStart) {
                     cToken = lex.getNextToken();
                     
                     if (cToken.getTipo() == Token.TokenType.JavaCode) {
                         globalParserCode = new ParserCode(cToken.getLexema());
                         cToken = lex.getNextToken();
                         
                         if (cToken.getTipo() == Token.TokenType.JavaCodeEnd) {
                             cToken = lex.getNextToken();
                             
                             if (cToken.getTipo() == Token.TokenType.Semicolon) {
                                 cToken = lex.getNextToken();
 
                             } else {
                                 Error("Se esperaba simbolo ';'");
                             }
                         } else {
                             Error("Se esperaba simbolo ':}'");
                         }
                     } else {
                         Error();
                     }
                 } else {
                     Error("Se esperaba simbolo '{:'");
                 }
             } else {
                 Error("Se esperaba palabra reservada 'Code'");
             }
         } else {
             Error("Se esperaba token 'Parser'");
         }
         return globalParserCode;
     }
 
     /*
      * terminal ::= ( (TERMINAL | NON-TERMINAL) ID ['<ID>']? id_list SEMICOLON )*
      */
     private void Terminals(Program program) throws Exception {
         //Pueden venir en cualquier orden.
         while (cToken.getTipo() == Token.TokenType.Terminal || cToken.getTipo() == Token.TokenType.Non) {
             if(cToken.getTipo() == Token.TokenType.Terminal){
                 Terminal(program);
             }else{
                 NonTerminal(program);
             }
         }
     }
     
     private void Terminal(Program program) throws Exception{
         while (cToken.getTipo() == Token.TokenType.Terminal) {
             Terminals terminal = new Terminals();
             cToken = lex.getNextToken();
 
             if (cToken.getTipo() == Token.TokenType.Identifier) {
                 String type = cToken.getLexema();
                 cToken = lex.getNextToken();
                Boolean containsLess = false;
                 if (cToken.getTipo() == Token.TokenType.Less) {
                    containsLess = true;
                     type += cToken.getLexema();
                     cToken = lex.getNextToken();
 
                     if (cToken.getTipo() == Token.TokenType.Identifier) {
                         type += cToken.getLexema();
                         cToken = lex.getNextToken();
 
                         if (cToken.getTipo() == Token.TokenType.Great) {
                             type += cToken.getLexema();
                             cToken = lex.getNextToken();
                         } else {
                             Error("Se esperaba token '>'");
                         }
                     } else {
                         Error("Se esperaba ID");
                     }
                 }
                 
                 if( cToken.getTipo() == Token.TokenType.Comma || cToken.getTipo() == Token.TokenType.Semicolon){
                    if( containsLess )
                            Error("Identificador de terminal no puede contener '<' o '>'");
                     terminal.setId(IdList());
                     terminal.AddValue(type);
                 }else{
                     terminal.setType(type);
                     terminal.setId(IdList());
                 }
 
                 if (cToken.getTipo() == Token.TokenType.Semicolon) {
                     cToken = lex.getNextToken();
                 } else {
                     Error("Se esperaba simbolo ';'");
                 }
                 
                 program.AddTerminal(terminal);
             }
         }
     }
     
     private void NonTerminal(Program program) throws Exception{
         while (cToken.getTipo() == Token.TokenType.Non) {
             Terminals terminal = new Terminals();
             cToken = lex.getNextToken();
             
             if (cToken.getTipo() == Token.TokenType.Terminal) {
                 cToken = lex.getNextToken();
                 if (cToken.getTipo() == Token.TokenType.Identifier) {
                     String type = cToken.getLexema();
                     cToken = lex.getNextToken();
                    Boolean containsLess = false;
 
                     if (cToken.getTipo() == Token.TokenType.Less) {
                        containsLess = true;
                         type += cToken.getLexema();
                         cToken = lex.getNextToken();
 
                         if (cToken.getTipo() == Token.TokenType.Identifier) {
                             type += cToken.getLexema();
                             cToken = lex.getNextToken();
 
                             if (cToken.getTipo() == Token.TokenType.Great) {
                                 type += cToken.getLexema();
                                 terminal.setType(type);
                                 cToken = lex.getNextToken();
                             } else {
                                 Error("Se esperaba token '>'");
                             }
                         } else {
                             Error("Se esperaba ID");
                         }
                     }
                     
                     if( cToken.getTipo() == Token.TokenType.Comma || cToken.getTipo() == Token.TokenType.Semicolon){
                        if( containsLess )
                            Error("Identificador de terminal no puede contener '<' o '>'");
                         terminal.setId(IdList());
                         terminal.AddValue(type);
                     }else{
                         terminal.setType(type);
                         terminal.setId(IdList());
                     }
 
                     if (cToken.getTipo() == Token.TokenType.Semicolon) {
                         cToken = lex.getNextToken();
                     } else {
                         Error("Se esperaba simbolo ';'");
                     }
 
                     program.AddNonTerminal(terminal);
                 }
             }
         }
     }
 
     /* 
      * id_list  ::= ID ( COMA ID )*
      */
     private ArrayList<String> IdList() throws Exception {
         ArrayList<String> idList = new ArrayList<String>();
         if (cToken.getTipo() == Token.TokenType.Identifier) {
             idList.add(cToken.getLexema());
             cToken = lex.getNextToken();
         }
 
         while (cToken.getTipo() == Token.TokenType.Comma) {
             cToken = lex.getNextToken();
             if (cToken.getTipo() == Token.TokenType.Identifier) {
                 idList.add(cToken.getLexema());
                 cToken = lex.getNextToken();
             } else {
                 Error("Se esperaba ID");
             }
         }
         
         return idList;
     }
 
     /* 
      * definitions ::= ( definition )*
      * definition ::= ID ASSIGN productions SEMICOLON
      */
     private ArrayList<Definitions> definitions() throws Exception {
         ArrayList<Definitions> definitionList = new ArrayList<Definitions>();
 
         while (cToken.getTipo() == Token.TokenType.Identifier) {
             Definitions d = new Definitions();
             d.setId(cToken.getLexema());
             cToken = lex.getNextToken();
 
             if (cToken.getTipo() == Token.TokenType.Assign) {
                 cToken = lex.getNextToken();
                 ArrayList<Productions> productionList = new ArrayList<Productions>();
                 productions(productionList);
                 
                 d.setProductions(productionList);
                 
                 if (cToken.getTipo() == Token.TokenType.Semicolon) {
                     cToken = lex.getNextToken();
                 } else {
                     Error("Se esperaba simbolo ';'");
                 }
             } else {
                 Error();
             }
             definitionList.add(d);
         }
         
         return definitionList;
     }
 
     /*
      * productions ::= production ( [OR production]? )* 
      * production ::= ( ID [:ID]? [{: JAVACODE :}]? )*
      */
     private void productions(ArrayList<Productions> productionList) throws Exception {
         production(productionList);
         if (cToken.getTipo() == Token.TokenType.Or) {
             cToken = lex.getNextToken();
             productions( productionList );
         }
     }
 
     private void  production(ArrayList<Productions> productionList) throws Exception {
         Productions p = new Productions();
         ArrayList<Item> items = new ArrayList<Item>();
         while (cToken.getTipo() == Token.TokenType.Identifier) {
             Item item = new Item();
             item.id = cToken.getLexema();
             cToken = lex.getNextToken();
 
             if (cToken.getTipo() == Token.TokenType.Colon) {
                 cToken = lex.getNextToken();
 
                 if (cToken.getTipo() == Token.TokenType.Identifier) {
                     item.variableName = cToken.getLexema();
                     cToken = lex.getNextToken();
                 } else {
                     Error("Se esperaba ID");
                 }
             }
             items.add(item);
             if (cToken.getTipo() == Token.TokenType.JavaCodeStart) {
                 cToken = lex.getNextToken();
                 if (cToken.getTipo() == Token.TokenType.JavaCode) {
                     if( cToken.getLexema() != null){
                         p.JavaCode += cToken.getLexema();
                     }
                     cToken = lex.getNextToken();
                 }
 
                 if (cToken.getTipo() == Token.TokenType.JavaCodeEnd) {
                     cToken = lex.getNextToken();
                 } else {
                     Error("Se esperaba simbolo ':}'");
                 }
             }
         }        
         p.items = items;
         productionList.add(p);
     }
 }
