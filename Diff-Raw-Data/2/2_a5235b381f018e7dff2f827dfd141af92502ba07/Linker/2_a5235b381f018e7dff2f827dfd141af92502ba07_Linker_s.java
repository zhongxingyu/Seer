 package xi.linker;
 
 import static xi.go.VM.UTF8;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Map.Entry;
 
 import stefan.Cout;
 import xi.ast.Expr;
 import xi.ast.LetIn;
 import xi.ast.Module;
 import xi.ast.Name;
 import xi.ast.stefan.LazyTree;
 
 /**
  * A linker for the SK output.
  * 
  * @author Leo
  */
 public class Linker {
 
     /** Definition map. */
     final Map<String, Expr> defs = new HashMap<String, Expr>();
 
     /**
      * Constructor.
      * 
      * @param ins
      *            SKLib inputs
      * @throws IOException
      *             I/O exception
      */
     public Linker(final List<Reader> ins) throws IOException {
         final Map<String, Expr> module = new HashMap<String, Expr>();
         final AstSKParser parser = new AstSKParser(module);
         for (final Reader r : ins) {
             // keeps the possibility to overwrite functions in other files
             parser.read(r);
             defs.putAll(module);
             module.clear();
         }
     }
 
     public Expr link(final String startSym) {
         final Map<String, Expr> required = new HashMap<String, Expr>();
         final Queue<String> queue = new ArrayDeque<String>();
         queue.add(startSym);
         while (!queue.isEmpty()) {
             final String sym = queue.poll();
             if (!required.containsKey(sym)) {
                 final Expr body = defs.get(sym);
                 if (body == null) {
                     throw new IllegalArgumentException("Symbol '" + sym
                             + "' not found.");
                 }
                 required.put(sym, body);
 
                 for (final Name n : body.freeVars()) {
                     queue.add(n.toString());
                 }
             }
         }
 
         final Expr start = required.remove(startSym);
         final Module mod = new Module(false);
         for (final Entry<String, Expr> e : required.entrySet()) {
             mod.addDefinition(Name.valueOf(e.getKey()), e.getValue());
         }
 
         final LetIn let = new LetIn(mod, start);
         return let.unLambda();
     }
 
     public static void main(final String[] args) throws Exception {
         String start = "main";
         final ArrayList<Reader> inputs = new ArrayList<Reader>();
         Writer out = new OutputStreamWriter(System.out);
         boolean invalid = false;
         for (int i = 0; i < args.length; i++) {
             if (args[i].equals("-help")) {
                 invalid = true;
                 break;
             }
             if (args[i].equals("-start")) {
                 if (i == args.length - 1) {
                     invalid = true;
                     break;
                 }
                 start = args[++i];
             } else if ("-out".equals(args[i])) {
                 if (i == args.length - 1) {
                     invalid = true;
                     break;
                 }
                out = new OutputStreamWriter(new FileOutputStream(args[i]),
                         UTF8);
             } else if ("-".equals(args[i])) {
                 inputs.add(new InputStreamReader(System.in));
             } else {
                 final File f = new File(args[i]);
                 if (!f.isFile()) {
                     invalid = true;
                     break;
                 }
                 inputs.add(new InputStreamReader(new FileInputStream(f), UTF8));
             }
         }
         if (invalid) {
             System.err.println("Usage: sasln [-help] [-start <start_sym>] "
                     + "[-out <dest_file>] <sklib>...\n"
                     + "\t<start_sym>: function name used as entry point,"
                     + " default is 'main'.\n"
                     + "\t<dest_file>: File to write to, default is STDOUT.\n"
                     + "\t<sklib>: a file containing an SK library,"
                     + " '-' means STDIN.\n");
             return;
         }
         if (inputs.isEmpty()) {
             inputs.add(new InputStreamReader(System.in));
         }
         final Linker linker = new Linker(inputs);
         final Expr linked = linker.link(start);
         final Module mod = new Module(false);
         mod.addDefinition(Name.valueOf("main"), linked);
 
         Cout.module(LazyTree.create(mod), out);
     }
 }
