 package com.bigcommerce.eg;
 
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 
 import java.io.*;
 
 import com.bigcommerce.eg.EgParser.model_return;
 import com.bigcommerce.eg.ast.*;
 import com.bigcommerce.eg.target.DotTarget;
 import com.bigcommerce.eg.target.MySqlTarget;
 import com.bigcommerce.eg.target.PopoTarget;
 import com.bigcommerce.eg.target.Target;
 
 import com.bigcommerce.eg.EgLexer;
 import com.bigcommerce.eg.EgParser;
 import com.bigcommerce.eg.EgTree;
 
 
 /**
  * A test driver program for Entity Generator.
  *
  * @author Luke Eller (luke.eller@bigcommerce.com)
  */
 class Main {
 
    private static boolean makeDot = false;
 
     static  EgLexer lexer;
 
     /** Just a simple test driver for the ASP parser
      * to show how to call it.
      */
 
     	public static void main(String[] args)
         {
             try
             {
                 // Create the lexer, which we can keep reusing if we like
                 //
                 lexer = new EgLexer();
 
                 if  (args.length > 0)
                 {
                     int s = 0;
 
                     if  (args[0].startsWith("-dot"))
                     {
                         makeDot = true;
                         s = 1;
                     }
                     // Recursively parse each directory, and each file on the
                     // command line
                     //
                     for (int i=s; i<args.length; i++)
                     {
                         parse(new File(args[i]));
                     }
                 }
                 else
                 {
                     System.err.println("Usage: java -jar cg-0.0.1-SNAPSHOT-jar-with-dependencies.jar <directory | filename.dmo>");
                 }
             }
             catch (Exception ex)
             {
                 System.err.println("ANTLR demo parser threw exception:");
                 ex.printStackTrace();
             }
         }
 
         public static void parse(File source) throws Exception
         {
 
             // Open the supplied file or directory
             //
             try
             {
 
                 // From here, any exceptions are just thrown back up the chain
                 //
                 if (source.isDirectory())
                 {
                     System.out.println("Directory: " + source.getAbsolutePath());
                     String files[] = source.list();
 
                     for (int i=0; i<files.length; i++)
                     {
                         parse(new File(source, files[i]));
                     }
                 }
 
                 // Else find out if it is an ASP.Net file and parse it if it is
                 //
                 else
                 {
                     // File without paths etc
                     //
                     String sourceFile = source.getName();
 
                     if  (sourceFile.length() > 3)
                     {
                         String suffix = sourceFile.substring(sourceFile.length()-4).toLowerCase();
 
                         // Ensure that this is a DEMO script (or seemingly)
                         //
                         if  (suffix.compareTo(".dmo") == 0)
                         {
                             parseSource(source.getAbsolutePath());
                         }
                     }
                 }
             }
             catch (Exception ex)
             {
                 System.err.println("ANTLR demo parser caught error on file open:");
                 ex.printStackTrace();
             }
 
         }
 
         public static void parseSource(String source) throws Exception
         {
             // Parse an ANTLR demo file
             //
             try
             {
                 // First create a file stream using the povided file/path
                 // and tell the lexer that that is the character source.
                 // You can also use text that you have already read of course
                 // by using the string stream.
                 //
                 lexer.setCharStream(new ANTLRFileStream(source, "UTF8"));
 
                 // Using the lexer as the token source, we create a token
                 // stream to be consumed by the parser
                 //
                 CommonTokenStream tokens = new CommonTokenStream(lexer);
 
                 // Now we need an instance of our parser
                 //
                 EgParser parser = new EgParser(tokens);
 
                 System.out.println("file: " + source);
 
                 // Provide some user feedback
                 //
                 System.out.println("    Lexer Start");
                 long start = System.currentTimeMillis();
                 
                 // Force token load and lex (don't do this normally, 
                 // it is just for timing the lexer)
                 //
                 tokens.LT(1);
                 long lexerStop = System.currentTimeMillis();
                 System.out.println("      lexed in " + (lexerStop - start) + "ms.");
 
                 // And now we merely invoke the start rule for the parser
                 //
                 System.out.println("    Parser Start");
                 long pStart = System.currentTimeMillis();
 
                 model_return psrReturn = parser.model();
                 System.out.println(((Tree) psrReturn.tree).toStringTree());
 
                 long stop = System.currentTimeMillis();
                 System.out.println("      Parsed in " + (stop - pStart) + "ms.");
 
                 // If we got a valid a tree (the syntactic validity of the source code
                 // was found to be solid), then let's print the tree to show we
                 // did something; our testing public wants to know!
                 // We do something fairly cool here and generate a graphviz/dot
                 // specification for the tree, which will allow the users to visualize
                 // it :-) we only do that if asked via the -dot option though as not
                 // all users will have installed the graphviz toolset from
                 // http://www.graphviz.org
                 //
 
                 // Pick up the Model
                 //
                 Tree t = (Tree)psrReturn.getTree();
 
 
                 // Now walk it with the generic tree walker, which does nothing but
                 // verify the tree really.
                 //
                 Model model = null;
                 try
                 {
                     if (parser.getNumberOfSyntaxErrors() == 0) {
                         EgTree walker = new EgTree(new CommonTreeNodeStream(t));
                         System.out.println("    AST Walk Start\n");
                         pStart = System.currentTimeMillis();
                         
                         EgTree.model_return walkerReturn = walker.model();
                         stop = System.currentTimeMillis();
                         model = (Model)walkerReturn.getTree();
                         System.out.println("\n      AST Walked in " + (stop - pStart) + "ms.");
                      }
                 }
                 catch(Exception w)
                 {
                     System.err.println("AST walk caused exception.");
                     System.err.println(w.getMessage());
                 }
                 
                 Target mySqlTarget = new MySqlTarget();
                 mySqlTarget.generate(model);
                 
                 Target popoTarget = new PopoTarget();
                 popoTarget.generate(model);
                 
                 if  (makeDot && tokens.size() < 4096)
                 {
                 	Target dotTarget = new DotTarget();
                 	dotTarget.generate(model);
                 }
             }
             catch (FileNotFoundException ex)
             {
                 // The file we tried to parse does not exist
                 //
                 System.err.println("\n  !!The file " + source + " does not exist!!\n");
             }
             catch (Exception ex)
             {
                 // Something went wrong in the parser, report this
                 //
                 System.err.println("Parser threw an exception:\n\n");
                 ex.printStackTrace();
             }
         }
 
 }
