 package jlogic;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringReader;
 
 import jlogic.interpret.Frame;
 import jlogic.interpret.SearchTree;
 import jlogic.read.Lexer;
 import jlogic.read.Parser;
 import jlogic.read.ReadException;
 import jlogic.term.Structure;
 import jlogic.term.Term;
 
 public final class Main {
     public static void main(String[] args) throws ReadException, IOException, InterruptedException {
         // Term left = termFromString("a(X,X)");
         // Term right = termFromString("a(Y,Y)");
 
         // System.out.println(left);
         // System.out.println(right);
         // System.out.println("--------------");
 
         // Frame frame = new Frame();
         // System.out.println(Matcher.match(frame, left, right));
 
         Knowledge knowledge = readFile("test.jl");
        Structure query = (Structure) termFromString("sum(cons(succ(zero), cons(succ(succ(zero)), cons(succ(succ(succ(succ(zero)))), nil))), R)");
 
         // System.out.println(knowledge);
        System.out.println(query);
 
         SearchTree search = new SearchTree(knowledge, query);
 
         Frame frame;
         do {
             frame = search.searchOne();
             if (frame != null) {
                 if (frame.getInstantiations().size() > 0)
                     System.out.println(frame);
                 else
                     System.out.println("Yes.");
             }
             else
                 System.out.println("No.");
         } while (frame != null);
 
         createGraphImage("searchgraph.png", search.toDOT());
     }
 
     private static Term termFromString(String string) throws ReadException, IOException {
         final String file = "[main" + string.hashCode() + "]";
         Lexer lexer = new Lexer(file, new StringReader(string));
 
         /*
          * Token token; do { token = lexer.read(); System.out.println(token); }
          * while (token.getType() != TokenType.EndOfFile);
          * 
          * lexer = new Lexer(file, string);
          */
 
         Parser parser = new Parser(lexer);
 
         return parser.parseTerm();
     }
 
     private static Knowledge readFile(String path) throws ReadException, IOException {
         FileInputStream stream = null;
         try {
             stream = new FileInputStream("test.jl");
             Lexer lexer = new Lexer(path, new InputStreamReader(stream));
             Parser parser = new Parser(lexer);
             return parser.parseKnowledge();
         } finally {
             if (stream != null)
                 stream.close();
         }
     }
 
     private static void createGraphImage(String filename, String dot) throws IOException, InterruptedException {
         // This might not always work, see
         // <http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4>.
 
         Process process = Runtime.getRuntime().exec("dot -o" + filename + " -Tpng");
         OutputStream stdin = process.getOutputStream();
         stdin.write(dot.getBytes());
         stdin.flush();
         stdin.close();
         process.waitFor();
 
         if (process.exitValue() != 0)
             throw new IOException("dot returned " + process.exitValue());
     }
 }
