 package cs444;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import cs444.lexer.Lexer;
 import cs444.parser.IASTBuilder;
 import cs444.parser.JoosASTBuilder;
 import cs444.parser.JoosDFA;
 import cs444.parser.Parser;
 import cs444.parser.symbols.NonTerminal;
 import cs444.parser.symbols.ast.factories.ASTSymbolFactory;
import cs444.parser.symbols.exceptions.OutOfRangeException;
 
 public class Compiler {
 
     private static void die(Exception e){
         e.printStackTrace();
         System.exit(42);
     }
 
     /**
      * @param args
      * @throws Exception
      */
     public static void main(String[] args){
 
         BufferedReader reader = null;
         NonTerminal parseTree = null;
 
         try {
 
             reader = new BufferedReader(new FileReader(args[0]));
             Lexer lexer = new Lexer(reader);
             Parser parser = new Parser(new JoosDFA());
 
             parseTree = parser.parse(lexer);
 
             System.out.println(parseTree.rule());
 
         } catch (Exception e) {
             die(e);
         } finally {
 
             if (null != reader){
                 try {
                     reader.close();
                 } catch (IOException e) {
                     die(e);
                 }
             }
         }
 
         IASTBuilder builder = new JoosASTBuilder();
 
         for(ASTSymbolFactory astSymbol : builder.getSimplifcations()){
             try {
                 astSymbol.convertAll(parseTree);
            } catch (OutOfRangeException e) {
                 die(e);
             }
         }
     }
 }
