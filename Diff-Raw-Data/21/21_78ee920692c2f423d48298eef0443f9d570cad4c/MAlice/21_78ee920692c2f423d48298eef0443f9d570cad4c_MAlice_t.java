 package malice;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.debug.ParseTreeBuilder;
 import org.antlr.runtime.tree.Tree;
 
 public class MAlice {
 
     /*private static List<Command> parseCommandTree(Tree tree) {
         List<Command> commands = new ArrayList<Command>();
         for (int i = 0; i < tree.getChildCount(); i++) {
             Tree child = tree.getChild(i);
             String childToken = child.getText();
             if (childToken.equals("prog")) {
                 return parseCommandTree(child);
             }
             if (childToken.equals("statement")) {
                 
             }
         }
         //tree.getChild(childIndex);
         
         
         return commands;
     }*/
     
     public static void main(String[] args) {
         // TODO - reading from a file
        CharStream charStream = new ANTLRStringStream("b was a number and b became 10+10*2. b spoke. c was a letter. c became 'f'. b became 10+10+e.");
 
         MAliceLexer lexer = new MAliceLexer(charStream);
 
         CommonTokenStream tokenStream = new CommonTokenStream(lexer);
 
         
 
 
         
         ParseTreeBuilder builder = new ParseTreeBuilder("prog");
         
         MAliceParser maliceParser = new MAliceParser(tokenStream, builder);
         try {
             
             maliceParser.prog();
             
             traverse(builder.getTree(), 0);
         } catch (RecognitionException ex) {
             ex.printStackTrace();
         }
         
         
         
         Parser parser = new Parser();
         parser.parseProg(builder.getTree());
         
         
         System.out.println("TEST");
         System.out.println(parser);
         
     }
 
     private static void traverse(Tree tree, int depth) {
         for (int i = 0; i < tree.getChildCount(); i++) {
             Tree child = tree.getChild(i);
             
             for (int j = 0; j < depth; j++) {
                 System.out.print(" ");
             }
             
             System.out.println(child.getText());
             if (child.getChildCount() != 0) {
                // System.out.println("my arse");
                 traverse(child, depth + 2);
             }
         }
     }
 }
