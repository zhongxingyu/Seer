 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 
 import net.nansore.cedalion.execution.ExecutionContext;
 import net.nansore.cedalion.execution.ExecutionContextException;
 import net.nansore.cedalion.execution.TermInstantiationException;
 import net.nansore.prolog.Compound;
 import net.nansore.prolog.PrologException;
 import net.nansore.prolog.PrologProxy;
 import net.nansore.prolog.Variable;
 
 
 public class Test {
 	public static void main(String[] args) throws IOException, PrologException, TermInstantiationException, ExecutionContextException {
 		PrologProxy p = new PrologProxy(new File("service.pl"));
 		
 		p.getSolution(new Compound(p, "loadFile", "procedure.ced", "cedalion"));
 		
 		ExecutionContext exe = new ExecutionContext(p);
 		// Open file
 		exe.runProcedure(new Compound(p, "cpi#openFile", "grammer-example.ced", "grammar", "gram"));
 		
 		// Print model
 		Variable model = new Variable("Model");
 		Variable x = new Variable("X");
 		Variable y = new Variable("Y");
 		Variable z = new Variable("Z");
 		Iterator<Map<Variable, Object>> results = p.getSolutions(p.createCompound("cedalion#loadedFile", "grammar", z, model));
 		while(results.hasNext()) {
 			Map<Variable, Object> result = results.next();
 			System.out.println(result.get(model));
 		}
 
 		// Print the third term
 		Compound list = p.createCompound("[]");
 		list = p.createCompound(".", 1, list);
 		list = p.createCompound(".", 2, list);
 		list = p.createCompound(".", 2, list);
 		Compound path = p.createCompound("cpi#path", "grammar", list);
 		Map<Variable, Object> result = p.getSolution(p.createCompound("cpi#termAtPath", path, x, y));
 		System.out.println(result.get(x));
 		System.out.println(result.get(y));
 
 		// Save file
 		exe.runProcedure(p.createCompound("cpi#saveFile", "grammar", "g1.ced"));
 
 		// Modify the third statement and save
 		exe.runProcedure(p.createCompound("cpi#edit", path, p.createCompound("::", p.createCompound("[]"), new Variable()), p.createCompound("[]")));
 		exe.runProcedure(p.createCompound("cpi#saveFile", "grammar", "g2.ced"));
 		
 		// Undo the change and save
 		exe.runProcedure(p.createCompound("cpi#undo", "grammar"));
 		exe.runProcedure(p.createCompound("cpi#saveFile", "grammar", "g3.ced"));
 		
 		// Print third statement as a string
 		String oldContent = (String)exe.evaluate(p.createCompound("cpi#termAsString", path, p.createCompound("cpi#constExpr", 3)), new Variable());
 		System.out.println(oldContent);
 
 		// Redo the change and save
 		exe.runProcedure(p.createCompound("cpi#redo", "grammar"));
 		exe.runProcedure(p.createCompound("cpi#saveFile", "grammar", "g4.ced"));
 		
 		// Restore the old content from the string we saved earlier, and save
		exe.runProcedure(p.createCompound("cpi#editFromString", path, p.createCompound("cpi#constExpr", "!(hello)")));
 		exe.runProcedure(p.createCompound("cpi#saveFile", "grammar", "g5.ced"));
 		
 		// Test visualization
 		Variable vis = new Variable();
 		result = p.getSolution(p.createCompound("cpi#visualizePath", path, vis));
 		System.out.println(result.get(vis));
 		
 		p.terminate();
 	}
 }
