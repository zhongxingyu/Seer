 package parser;
 import java.util.LinkedList;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 import ast.Program;
 import ast.Expression;
 
 public class ParserInterface {
 
     private String[] errors = new String[0];
 
     private TypeChecker typeChecker;
 
     /**
      * Report all errors from lexer and parser.
      * @return A list of error messages
      */
     public String[] getErrors() {
         return errors;
     }
 
     /**
      * Parse program text into an AST
      * @param text the text to be parsed
      * @return the AST
      * @throws RecognitionException
      */
     public Program parseProgram(String text) throws RecognitionException {
         LinkedList<String> errors = new LinkedList<String>();
         CharStream in = new ANTLRStringStream(text);
         WhileLanguageLexer lex = new WhileLanguageLexer(in);
         lex.setErrorReporter(errors);
         CommonTokenStream tokens = new CommonTokenStream();
         tokens.setTokenSource(lex);
         WhileLanguageParser parser = new WhileLanguageParser(tokens);
         parser.setErrorReporter(errors);
         Program p = parser.program();
         this.errors = errors.toArray(new String[errors.size()]);
         if (typeChecker == null) {
             typeChecker = new TypeChecker();
         }
         if (errors.isEmpty()) {
             typeChecker.checkTypes(p);
         }
         return errors.isEmpty() ? p : null;
     }
 
     /**
      * Parse expression text into an AST
      * @param text the text to be parsed
      * @return the AST
      * @throws RecognitionException
      */
     public Expression parseExpression(String text) throws RecognitionException {
         LinkedList<String> errors = new LinkedList<String>();
         CharStream in = new ANTLRStringStream(text);
         WhileLanguageLexer lex = new WhileLanguageLexer(in);
         lex.setErrorReporter(errors);
         CommonTokenStream tokens = new CommonTokenStream();
         tokens.setTokenSource(lex);
         WhileLanguageParser parser = new WhileLanguageParser(tokens);
         parser.setErrorReporter(errors);
         Expression e = parser.single_expression();
         this.errors = errors.toArray(new String[errors.size()]);
        return errors.isEmpty() ? e : null;
     }
 }
