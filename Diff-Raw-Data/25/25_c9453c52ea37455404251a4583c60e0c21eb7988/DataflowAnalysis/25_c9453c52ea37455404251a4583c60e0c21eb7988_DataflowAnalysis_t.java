 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import chord.project.analyses.JavaAnalysis;
 import chord.project.Chord;
 import chord.program.Program;
 
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.ControlFlowGraph;
 import joeq.Compiler.Quad.BasicBlock;
 import joeq.Compiler.Quad.Quad;
 
 public abstract class DataflowAnalysis<T> extends JavaAnalysis {
 	protected final Map<Quad, Set<T>> inMap  = new HashMap<Quad, Set<T>>();
 	protected final Map<Quad, Set<T>> outMap = new HashMap<Quad, Set<T>>();
 	protected jq_Method main;
 
 	@Override
 	public void run() {
 		System.out.println("********** " + getName() + " analysis *********");
 		main = Program.g().getMainMethod();
 		doAnalysis();
 		if (main.isAbstract())
 			throw new RuntimeException("Method " + main + " is abstract");
 		ControlFlowGraph cfg = main.getCFG();
 		System.out.println(cfg.fullDump());
 		System.out.println("********** ENTER Analysis Results **********");
 		for (Quad q : inMap.keySet()) {
 			Set<T> inSet = inMap.get(q);
 			Set<T> outSet = outMap.get(q);
 			String inStr = "";
 			if (inSet != null)
 				for (T v : inSet) inStr += v + " ";
 			String outStr = "";
 			if (outSet != null)
 				for (T v : outSet) outStr += v + " ";
 			System.out.println(q.toVerboseStr());
 			System.out.println("\tIN :" + inStr);
 			System.out.println("\tOUT:" + outStr);
 		}
 		System.out.println("********** LEAVE Analysis Results **********");
 	}
 
 	public abstract void doAnalysis();
	
	public static boolean setEquals(Set<Register> set1, Set<Register> set2) {
		
		if(set1.size() != set2.size()) 
			return false;
		
		for(Register r : set1) {
			if(!setContains(r, set2))
				return false;
		}
		
		return true;
	}
	
	public static boolean setContains(Register r, Set<Register> s) {
		
		if(s.isEmpty())
			return false;
		
		for(Register curr : s)
			if(r.getNumber() == curr.getNumber())
				return true;
		
		return false;
	}
 }
 
