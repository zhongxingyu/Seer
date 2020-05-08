 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 import java.io.*;
 
 public class XMLTool extends FFITool {
     public void parse_file(File input, StringTemplateGroup stg) throws Exception {
         // create AST
         System.err.println("creating abstract syntax tree...");
         ANTLRFileStream instream = new ANTLRFileStream(input.toString());
         XMLLexer lexer = new XMLLexer(instream);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         XMLParser parser = new XMLParser(tokens);
         Tree tree = (Tree)parser.ffi().tree;
         if (parser.getNumberOfSyntaxErrors() > 0) {
             System.exit(-1);
         }
         // parse the AST
        parse_tree(tree, tokens, stg);
     }
 }
