 /** Running the Alloy Compiller.
   By Gail Terman
  */
 
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
 import edu.mit.csail.sdg.alloy4compiler.ast.Module;
 import edu.mit.csail.sdg.alloy4compiler.ast.Command;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
 import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
 
 public class AlloyCompiler {
   public static void main (String[] args) throws Err{
     String filename = args[0];
     Module mod = CompUtil.parseEverything_fromFile(null, null, filename);
 
     A4Options opts = new A4Options();
     opts.solver = A4Options.SatSolver.SAT4J;
 
     String cwd = System.getProperty("user.dir");
 
     for (Command command: mod.getAllCommands()) {
       A4Solution ans = TranslateAlloyToKodkod.execute_command(null, mod.getAllReachableSigs(), command, opts);
       if (ans.satisfiable()) {
        ans.writeXML(cwd+"/out.xml");
       }else System.out.println("Not satisfiable.");
     }
   }
 }
