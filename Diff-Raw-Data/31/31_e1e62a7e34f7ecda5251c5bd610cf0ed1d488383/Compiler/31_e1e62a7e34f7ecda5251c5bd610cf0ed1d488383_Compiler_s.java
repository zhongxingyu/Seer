 package cs444;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 
 import cs444.lexer.Lexer;
 import cs444.parser.IASTBuilder;
 import cs444.parser.JoosASTBuilder;
 import cs444.parser.Parser;
 import cs444.parser.TextReadingRules;
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ast.factories.ASTSymbolFactory;
 
 public class Compiler {
 
     private static final int COMPILER_ERROR_CODE = 42;
 
     /**
      * @param args
      * @throws Exception
      */
     public static void main(String[] args){

         System.exit(compile(args));
     }
 
     public static int compile(String[] files) {
         Reader reader = null;
         ANonTerminal parseTree = null;
 
         try {
             for(String fileName : files){
                 File file = new File(fileName);
                 reader = new FileReader(fileName);
                 Lexer lexer = new Lexer(reader);
                 Parser parser = new Parser(new TextReadingRules(new File("JoosRules.txt")));
 
                 parseTree = parser.parse(lexer);
 
                 IASTBuilder builder = new JoosASTBuilder();
 
                 for(ASTSymbolFactory astSymbol : builder.getSimplifcations()){
 
                     parseTree = (ANonTerminal)astSymbol.convertAll(parseTree);
                 }
 
                 if(!builder.isValidFileName(file.getName(), parseTree)){
                     System.err.print("Invalid file name" + fileName);
                     return COMPILER_ERROR_CODE;
                 }
             }
         }catch(Exception e){
             e.printStackTrace();
             return COMPILER_ERROR_CODE;
         }finally{
             if(reader != null){
                 try {
                     reader.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
 
         return 0;
     }
 }
