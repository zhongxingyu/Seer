 package de.weltraumschaf.caythe.frontend.pascal;
 
 import de.weltraumschaf.caythe.frontend.pascal.parsers.StatementParser;
 import de.weltraumschaf.caythe.intermediate.Code;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 import de.weltraumschaf.caythe.frontend.EofToken;
 import de.weltraumschaf.caythe.frontend.Parser;
 import de.weltraumschaf.caythe.frontend.Scanner;
 import de.weltraumschaf.caythe.frontend.Token;
 import de.weltraumschaf.caythe.frontend.TokenType;
 import de.weltraumschaf.caythe.intermediate.CodeFactory;
 import de.weltraumschaf.caythe.intermediate.CodeNode;
 import de.weltraumschaf.caythe.message.Message;
 import de.weltraumschaf.caythe.message.MessageType;
 import java.io.IOException;
 
 import static de.weltraumschaf.caythe.frontend.pascal.PascalTokenType.*;
 import static de.weltraumschaf.caythe.frontend.pascal.PascalErrorCode.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class PascalTopDownParser extends Parser {
 
     protected static PascalErrorHandler errorHandler = new PascalErrorHandler();
 
     public PascalTopDownParser(Scanner scanner) {
         super(scanner);
     }
 
     public PascalTopDownParser(PascalTopDownParser parent) {
         super(parent.getScanner());
     }
 
     @Override
     public void parse() throws Exception {
         long startTime = System.currentTimeMillis();
         intermediateCode = CodeFactory.createCode();
 
         try {
             Token token = nextToken();
             CodeNode rootNode = null;
 
             if (BEGIN == token.getType()) {
                 StatementParser statementParser = new StatementParser(this);
                 rootNode = statementParser.parse(token);
                 token = currentToken();
             } else {
                 errorHandler.flag(token, UNEXPECTED_TOKEN, this);
             }
 
             // Look for the final period.
             if (token.getType() != DOT) {
                 errorHandler.flag(token, MISSING_PERIOD, this);
             }
             token = currentToken();
 
             // Set the parse tree root node.
             if (rootNode != null) {
                 intermediateCode.setRoot(rootNode);
             }
 
             // Send the parser summary message.
             float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
             sendMessage(new Message(MessageType.PARSER_SUMMARY, new Number[] {
                 token.getLineNumber(),
                 getErrorCount(),
                 elapsedTime
             }));
         } catch (IOException ex) {
             errorHandler.abortTranslation(IO_ERROR, this);
         }
     }
 
     @Override
     public int getErrorCount() {
         return errorHandler.getErrorCount();
     }
 
 }
