 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.Tree;
 import org.antlr.runtime.debug.ParseTreeBuilder;
 
 public class Test {
    public static void printTree(Tree t, int indent, int spaces) {
       for (int i = 0; i < t.getChildCount(); i++) {
          for (int j = 0; j < indent; j++)
             System.out.print(' ');
          Tree ch = t.getChild(i);
          System.out.println(ch);
          printTree(ch, spaces+indent, spaces);
       }
    }
 
    public static void main(String[] args) throws Exception {
      if (args.length != 1) {
          System.err.println("Usage: java Test <file> ...");
          return;
       }
 
       for (int i = 0; i < args.length; i++) {
          MiniAdaLexer lex = new MiniAdaLexer(new MiniAdaFileStream(args[i]));
          CommonTokenStream tokens = new CommonTokenStream(lex);
          ParseTreeBuilder builder = new ParseTreeBuilder("compilation");
          MiniAdaParser parse = new MiniAdaParser(tokens, builder);
          
          try {
             parse.compilation();
             System.out.println("Parsing file: " + args[i]);
             printTree(builder.getTree(), 2, 2);
          } catch (RecognitionException e) {
             e.printStackTrace();
             return;
          }
       }
    }
 }
