 // Oodle.java
 // James Coleman, 2010
 // Primary file containing the main function for the Oodle compiler
 
 package cps450;
 import cps450.oodle.node.*;
 import cps450.oodle.parser.Parser;
 import cps450.oodle.parser.ParserException;
 
 import cps450.oodle.lexer.*;
 
 import java.io.*;
 
 public class Oodle
 {
 	
     public static void main(String[] arguments) throws IOException, ParserException, LexerException {
         if(!(arguments.length >= 1)) {
             System.out.println("usage:");
             System.out.println("  java Oodle filename");
             System.exit(1);
         }
         
         Options.instance().parseArgumentArray(arguments);
 
         System.out.println();
         
         SourceHolder.instantiate(Options.instance().files);
         File input = SourceHolder.instance().concatenateFiles();
         
        Lexer lexer = new MyLexer(
             new PushbackReader(
             new BufferedReader(
             new FileReader(input)), 1024));
         
         Parser parser = new Parser(lexer);
         
         try {
         	parser.parse();
         } catch (ParserException e) {
         	String errorText = e.getMessage();
         	errorText = errorText.substring(errorText.indexOf(']') + 2);
         	Token t = e.getToken();
 			System.out.println(SourceHolder.instance().getFilenameFor(t) + ":" + SourceHolder.instance().getLineNumberFor(t) + "," + t.getPos() + ":" + errorText);
         }
         
     }
 
 }
 
