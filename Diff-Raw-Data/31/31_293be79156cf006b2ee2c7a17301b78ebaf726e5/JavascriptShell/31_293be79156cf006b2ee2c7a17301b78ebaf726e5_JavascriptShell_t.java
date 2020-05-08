 package org.clafer.javascript;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Set;
 import javax.script.ScriptException;
 import org.clafer.ast.AstClafer;
 import org.clafer.ast.AstConstraint;
 import org.clafer.ast.AstModel;
 import org.clafer.ast.AstUtil;
 import org.clafer.scope.Scope;
 import org.clafer.collection.Pair;
 import org.clafer.compiler.ClaferCompiler;
 import org.clafer.compiler.ClaferObjective;
 import org.clafer.compiler.ClaferSolver;
 import org.clafer.compiler.ClaferUnsat;
 import org.clafer.instance.InstanceModel;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.RhinoException;
 import org.mozilla.javascript.Scriptable;
 
 /**
  * The Javascript CLI.
  *
  * @author jimmy
  */
 public class JavascriptShell {
 
     // The Javascript engine.
     private Scriptable engine = Javascript.newEngine();
     // The model. Null if the model is not successfully loaded.
     private AstModel model;
     // The scope. Null if the model is not successfully loaded.
     private Scope scope;
     // The solver which solved the previous instance.
     // Null if first instance not yet solved.
     private ClaferSolver solver;
     // The last file successfully loaded.
     private File modelFile;
 
     public void init() throws IOException {
         Context cxt = Context.enter();
        cxt.setOptimizationLevel(-1);
         try {
             engine.put("rsc", engine, this);
             // solver.js contains the mapping from Javascript to the methods
             // in this class.
             cxt.evaluateReader(engine,
                     new InputStreamReader(JavascriptShell.class.getResourceAsStream("solver.js")),
                     "solver.js", 1, null);
             engine.put("help", engine,
                     "help             display this helpful message\n"
                     + "load()           reload the model\n"
                     + "load(filename)   load a new model\n"
                     + "solve()          find the next solution\n"
                     + "maximize(Clafer) find a solution where Clafer.ref is maximal\n"
                     + "minimize(Clafer) find a solution where Clafer.ref is minimal\n"
                     + "minUnsat()       find the smallest set of unsatisfiable constraints and a near-miss\n"
                     + "unsatCore()      find a small set of mutually unsatisfiable constraints\n"
                     + "stats()          display statistics about the current search\n"
                     + "exit()           stop the session");
         } finally {
             Context.exit();
         }
     }
 
     /**
      * Returns information about the final Choco CSP.
      *
      * @return information for debugging
      */
     public String internals() {
         return internals(false);
     }
 
     /**
      * Returns information about the final Choco CSP.
      *
      * @param initial debug initial state or current state
      * @return information for debugging
      */
     public String internals(boolean initial) {
         if (model == null) {
             return "No model. Use \"load(filename)\" to load in a new model.";
         }
         if (initial) {
             return ClaferCompiler.compile(getModel(), scope).getInternalSolver().toString();
         }
         if (solver == null) {
             return "Solve an instance first. Try \"solve()\".";
         }
         return solver.getInternalSolver().toString() + "\n" + solver.getInternalSolver().getMeasures();
     }
 
     /**
      * Execute Javascript.
      *
      * @param script executable Javascript
      * @return the value of the script, or null if it is a statement
      */
    public Object eval(String script) {
         Context cxt = Context.enter();
        cxt.setOptimizationLevel(-1);
         try {
             return Context.toString(cxt.evaluateString(engine, script, "commandline", 1, null));
         } finally {
             Context.exit();
         }
     }
 
     /**
      * Reload the previous successful file.
      *
      * @return a message
      * @throws IOException something happened to the file
      */
     public String load() throws IOException {
         if (modelFile == null) {
             return "No model. Use \"load(filename)\" to load in a new model.";
         }
         return load(modelFile);
     }
 
     /**
      * Load a new file. The file must contain valid Javascript.
      *
      * @param filename the name of the file
      * @return a message
      * @throws IOException something happened to the file
      */
     public String load(String filename) throws IOException {
         return load(new File(filename));
     }
 
     /**
      * Load a new file. The file must contain valid Javascript.
      *
      * @param file the file
      * @return a message
      * @throws IOException something happened to the file
      */
     public String load(File file) throws IOException {
         model = null;
         scope = null;
         solver = null;
         engine = Javascript.newEngine();
         try {
             Pair<AstModel, Scope> pair = Javascript.readModel(file, engine);
             model = pair.getFst();
             scope = pair.getSnd();
         } finally {
             init();
         }
         modelFile = file;
         return "Loaded " + AstUtil.getNames(AstUtil.getClafers(model)) + ".";
     }
 
     private AstModel getModel() {
         if (model == null) {
             throw new JavascriptException("No model. Use \"load(filename)\" to load in a new model.");
         }
         return model;
     }
 
     /**
      * Find the first instance or next instances upon subsequent invocations.
      *
      * @return an instance
      */
     public InstanceModel solve() {
         if (solver == null) {
             solver = ClaferCompiler.compile(getModel(), scope);
         }
         return solver.find() ? solver.instance() : null;
     }
 
     /**
      * Find an instance where the Clafer's value is maximal.
      *
      * @param clafer maximize this Clafer's value
      * @return the maximal value and the optimal instance
      */
     public Pair<Integer, InstanceModel> maximize(AstClafer clafer) {
         if (!clafer.hasRef()) {
             throw new JavascriptException("Cannot maximize " + clafer + ".");
         }
         ClaferObjective objective = ClaferCompiler.compileMaximize(getModel(), scope, clafer.getRef());
         return objective.optimal();
 
     }
 
     /**
      * Find an instance where the Clafer's value is minimal.
      *
      * @param clafer minimize this Clafer's value
      * @return the minimal value and the optimal instance
      */
     public Pair<Integer, InstanceModel> minimize(AstClafer clafer) {
         if (!clafer.hasRef()) {
             throw new JavascriptException("Cannot minimize " + clafer + ".");
         }
         ClaferObjective objective = ClaferCompiler.compileMinimize(getModel(), scope, clafer.getRef());
         return objective.optimal();
     }
 
     /**
      * Find the Min-Unsat and near-miss example. The Min-Unsat is empty if the
      * model is satisfiable, in which case the near-miss example is a real
      * instance of the model.
      *
      * @return the Min-Unsat and near-miss example
      */
     public Pair<Set<AstConstraint>, InstanceModel> minUnsat() {
         ClaferUnsat unsat = ClaferCompiler.compileUnsat(getModel(), scope);
         return unsat.minUnsat();
     }
 
     /**
      * Find the Min-Unsat-Core. Undefined if the model is satisfiable.
      *
      * @return the Min-Unsat-Core
      */
     public Set<AstConstraint> unsatCore() {
         ClaferUnsat unsat = ClaferCompiler.compileUnsat(getModel(), scope);
         return unsat.unsatCore();
     }
 
     public String stats() {
         if (solver == null) {
             throw new JavascriptException("No solver. Use \"solve()\" to solve the model.");
         }
         return solver.getMeasures().toString();
     }
 
     /**
      * Exit the program.
      *
      * @return a message
      */
     public String exit() {
         // Use interrupts to signal end.
         Thread.currentThread().interrupt();
         return "Exitting ...";
     }
 
     public static void main(String[] args) throws IOException, ScriptException {
         JavascriptShell context = new JavascriptShell();
         if (args.length > 0) {
             try {
                 System.out.println(context.load(args[0]));
             } catch (IOException e) {
                 System.out.println("Error: " + getOriginalMessage(e));
             } catch (RhinoException e) {
                 System.out.println("Error: " + getOriginalMessage(e));
             }
         } else {
             context.init();
         }
 
         System.out.println("Type \"help\" for a list of commands.");
         System.out.print("> ");
         System.out.flush();
         BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
         String line;
         while ((line = r.readLine()) != null) {
             line = line.trim();
             // Do not evaluate empty lines.
             if (line.length() > 0) {
                 try {
                     // Evaluate the Javascript command.
                     System.out.println(context.eval(line));
                     if (Thread.interrupted()) {
                         // Exit was invoked.
                         break;
                     }
                 } catch (RhinoException e) {
                     System.out.println("Error: " + getOriginalMessage(e));
                 }
             }
             System.out.print("> ");
             System.out.flush();
         }
         System.out.println();
     }
 
     /**
      * Find the message in the innermost cause. The Rhino engine tends to have
      * several layers of wrapped exceptions, which do not look great when
      * printing on the command line.
      *
      * @param e the outermost exception
      * @return the innermost message
      */
     private static String getOriginalMessage(Throwable e) {
         if (e.getCause() != null) {
             return getOriginalMessage(e.getCause());
         }
         return e.getMessage();
     }
 }
