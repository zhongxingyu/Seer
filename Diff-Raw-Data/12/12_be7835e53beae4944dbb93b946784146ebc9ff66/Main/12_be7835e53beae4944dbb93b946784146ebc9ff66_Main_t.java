 package de.skuzzle.polly.console;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 
import de.skuzzle.polly.core.parser.Evaluator;
import de.skuzzle.polly.core.parser.Position;
import de.skuzzle.polly.core.parser.ast.declarations.DeclarationReader;
import de.skuzzle.polly.core.parser.ast.declarations.Namespace;
import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
import de.skuzzle.polly.core.parser.ast.visitor.ExpASTVisualizer;
 import de.skuzzle.polly.process.KillingProcessWatcher;
 import de.skuzzle.polly.process.ProcessExecutor;
 
 
 
 public class Main {
     
     private final static String DOT_PATH = 
             "C:\\Program Files (x86)\\Graphviz 2.28\\bin\\dot.exe";
     
     private final static File DECLARATION_FOLDER = new File("decls");
     
     private final static FileFilter DECLARATION_FILTER = new FileFilter() {
         
         @Override
         public boolean accept(File pathname) {
             return pathname.getName().toLowerCase().endsWith(".decl");
         }
     };
     
     
     
     private static void readDeclarations() throws IOException {
         for (final File file : DECLARATION_FOLDER.listFiles(DECLARATION_FILTER)) {
             DeclarationReader dr = null;
             try {
                 final String nsName = file.getName().substring(
                         0, file.getName().length() - 5);
                 Namespace ns = Namespace.forName(nsName);
                 dr = new DeclarationReader(file, "ISO-8859-1", ns);
                 dr.readAll();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 if (dr != null) {
                     dr.close();
                 }
             }
         }
     }
     
 
     public static void main(String[] args) throws IOException, ASTTraversalException {
         
         Namespace.setDeclarationFolder(new File("decls"));
         
         readDeclarations();
         
         final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
         String nsName = "default";
         
         System.out.println("Welcome to PPC - Polly Parser Console");
         System.out.println("Just enter any valid polly expression, or type :q to exit.");
         System.out.println("Type :ns <name> to switch namespaces.");
         System.out.println();
         
         while (true) {
             Namespace ns = Namespace.forName(nsName);
             System.out.print(nsName + " > ");
             final String cmd = r.readLine();
             
             if (cmd.equals(":q")) {
                 return;
             } else if (cmd.startsWith(":ns")) {
                 final String[] parts = cmd.split(" ");
                 if (parts.length != 2) {
                     System.out.println("Invalid parameters for ':ns'");
                 } else {
                     nsName = parts[1];
                 }
                 
                 continue;
             } else if (cmd.equals(":cns")) {
                 //System.out.println();
                 continue;
             } else if (cmd.equals(":vns")) {
                 System.out.println("Namespace: " + nsName);
                 System.out.println(ns.toString());
                 continue;
             }
             
             final Evaluator eval = new Evaluator(":result \">\" " + cmd, "ISO-8859-1");
             
             try {
                 eval.evaluate(ns, ns);
             } catch (Exception ad) {
                 ad.printStackTrace();
             }
             
             if (eval.errorOccurred()) {
                 final ASTTraversalException e = eval.getLastError();
                 Position pos = new Position(e.getPosition().getStart() - 12, 
                     e.getPosition().getEnd() - 12);
                 final ASTTraversalException e1 = 
                     new ASTTraversalException(pos, e.getPlainMessage());
                 System.out.println(e1.getMessage());
                 System.out.println("    " + cmd);
                 System.out.println("    " + e1.getPosition().errorIndicatorString());
                 try {
                     // HACK: wait a little to be sure stack trace in printed after sysout
                     Thread.sleep(20);
                 } catch (InterruptedException e2) {
                     throw new RuntimeException(e2);
                 }
                 e.printStackTrace();
             }
             
             if (eval.getRoot() != null){
                 System.out.println(eval.getRoot().toString());
                 
                 final ExpASTVisualizer av = new ExpASTVisualizer();
                 av.visualize(eval.getRoot(), new PrintStream("lastAst.dot"), ns);
                 
                 ProcessExecutor pe = ProcessExecutor.getOsInstance(false);
                 pe.addCommand(DOT_PATH);
                 pe.addCommandsFromString("-Tpdf -o lastAst.pdf");
                 pe.addCommand("lastAST.dot");
                 pe.setProcessWatcher(new KillingProcessWatcher(10000, true));
                 pe.start();
             }
         }
     }
 }
