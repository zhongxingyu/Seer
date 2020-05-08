 package jlogic;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.util.Map;
 
 import jlogic.interpret.Frame;
 import jlogic.interpret.SearchTree;
 import jlogic.read.Lexer;
 import jlogic.read.Parser;
 import jlogic.read.ReadException;
 import jlogic.term.Structure;
 import jlogic.term.Term;
 import jlogic.term.Variable;
 
 public final class REPL {
     private final Knowledge knowledge;
 
     private final BufferedReader input;
     private final Writer output;
     private final Writer errorOutput;
 
     public REPL(Knowledge knowledge, InputStream input, OutputStream output,
             OutputStream errorOutput) {
         this.knowledge = knowledge;
         this.input = new BufferedReader(new InputStreamReader(input));
         this.output = new OutputStreamWriter(output);
         this.errorOutput = new OutputStreamWriter(errorOutput);
     }
 
     public void run() throws IOException {
         while (true) {
             Structure goal = read();
             if (goal == null)
                 return;
 
             SearchTree searchTree = new SearchTree(knowledge, goal);
 
             Frame frame;
            char inputChar;
             do {
                 frame = searchTree.searchOne();
                 if (frame != null) {
                     if (frame.getInstantiations().size() > 0) {
                         for (Map.Entry<Variable, Term> entry : frame.getInstantiations().entrySet()) {
                             output.write(entry.getKey().toString());
                             output.write(" = ");
                             output.write(entry.getValue().toString());
                             output.write("\n");
                            output.write("\n");
                         }
                     } else
                         output.write("Yes.\n");
                 }
                 else
                     output.write("No.\n");
                 output.flush();
 
                // inputChar = (char) input.read();
            } while (/* inputChar == ';' && */frame != null);
 
             output.write("\n");
             output.flush();
         }
     }
 
     // TODO: Read multiple goals at once
     private Structure read() throws IOException {
         while (true) {
             output.write("?- ");
             output.flush();
 
             String line = input.readLine();
 
             if (line.endsWith("\n"))
                 line = line.substring(0, line.length() - 1);
 
             if (line.startsWith(":")) {
                 String command = line.substring(1);
                if (command == "quit")
                     return null;
             }
 
             try {
                 Lexer lexer = new Lexer("repl", new StringReader(line));
                 Parser parser = new Parser(lexer);
 
                 return parser.parseStructure();
             } catch (ReadException exception) {
                 errorOutput.write(exception.toString());
             }
         }
     }
 }
