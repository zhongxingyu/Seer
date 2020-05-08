 import java.util.*;
 
 import chord.project.Chord;
 import chord.util.tuple.object.Pair;
 import chord.project.analyses.JavaAnalysis;
 import chord.program.Program;
 
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.*;
 import joeq.Compiler.Quad.RegisterFactory.Register;
 import joeq.Compiler.Quad.Operand.RegisterOperand;
 
 @Chord(name="reachdef")
 public class ReachDefAnalysis extends DataflowAnalysis<Pair<Quad, Register>> {
 	@Override
 	public void doAnalysis() {
 		// Implement your reaching definitions dataflow analysis here. 
 		//
 		// File DataflowAnalysis.java defines instance fields main, inMap, and
 		// outMap, which will serve as the inputs and outputs of your analysis:
 		//
 		// INPUT: Method main.
 		//
 		// OUTPUT: Populate maps inMap and outMap with the results of your
 		// reaching definitions analysis of method main.
 		//
 		// Specifically, for each Quad q in the control-flow graph of main,
 		// inMap(q) and outMap(q) must contain the sets of all <Quad, Register>
 		// definitions that may reach the entry and the exit, respectively, of
 		// q.  You can leave a set either null or empty if it does not contain
 		// any reaching definitions.
 		//
 		// Your analysis will be graded for the following aspects in decreasing
 		// order of importance:
 		//
 		// 1. Correctness of the results produced by the analysis.
 		// 2. Efficiency of the analysis, in particular, the number of times you
 		// revisit each quad.
 		// 3. Clarity of your source code.  While we will run automatic tests
 		// to evaluate correctness, we might need to read your source code to
 		// evaluate the efficiency of your analysis.  So, it is important that
 		// your code be concise and readable.  Adding comments is not required
 		// but it won't hurt if you comment your code.
 		//
 		// Add helper instance methods to this class if necessary.  All your
 		// code must be in this class itself, and it must be written in Java
 		// (for instance, you cannot use Datalog).
 		//
 		// Note: This is a single procedure analysis; you do not need to
 		// consult any pointer analysis, call graph, or any method of the given
 		// program besides the provided main method.
 		
 		main = Program.g().getMainMethod();
 		if (main.isAbstract())
 			throw new RuntimeException("Method " + main + " is abstract");
 		ControlFlowGraph cfg = main.getCFG();
 		
 		int count = 0;
 		boolean changed = true;
 		boolean last = true;
 		List<Quad> quads;
 		Set<Pair<Quad,Register>> in, out;
 		
 		System.out.println("Begin analysis...");
 		while(changed) {
 			count++;
 			changed = false;
 			System.out.println("Iteration " + count + "...");
 			
 			for(BasicBlock bb : cfg.reversePostOrder()) {
 				for(Quad q : bb.getQuads()) {
 					
 					Set<Quad> preds = getPredecessors(q, bb);
 					
 					in = new HashSet<Pair<Quad,Register>>();
 					out = new HashSet<Pair<Quad,Register>>();
 					
 					// Union of exit of predecessors
 					for(Quad pred_q : preds) {
						HashSet<Pair<Quad,Register>> set = outMap.get(pred_q);
 						if(set != null)
 							in.addAll(set);
 					}
 					
 					List<RegisterOperand> def = q.getDefinedRegisters();
 					
 					out.addAll(in);
 					// Remove kill set
 					out = removeKilled(out, def);
 					
 					// Add gen set
 					for(RegisterOperand ro : def) {
 						out.add(new Pair(q,ro.getRegister()));
 					}
 					
 					Set<Pair<Quad,Register>> prev_in = inMap.put(q, in);
 					Set<Pair<Quad,Register>> prev_out = outMap.put(q, out);
 					
 					if(prev_in == null || (prev_in != null && !prev_in.equals(in)) || prev_out == null || (prev_out != null && !prev_out.equals(out)))
 						changed = true;
 				}
 			}
 		}
 		System.out.println("Analysis finished after " + count + " iterations.");
 	}
 	
 	private Set<Quad> getPredecessors(Quad q, BasicBlock bb) {
 		
 		int index = bb.getQuadIndex(q);
 		Set<Quad> preds = new HashSet<Quad>();
 		
 		if(index == 0) {
 			for(BasicBlock pred_bb : bb.getPredecessors())
 				if(pred_bb.size() > 0)
 					preds.add(bb.getLastQuad());
 			
 			return preds;
 		}
 		
 		preds.add(bb.getQuad(index - 1));
 		return preds;
 	}
 	
 	private Set<Pair<Quad,Register>> removeKilled(Set<Pair<Quad,Register>> set, List<RegisterOperand> def) {
 		
 		for(RegisterOperand ro : def) {
 			for(Pair<Quad,Register> p : set) {
 				if(p.val0.equals(ro.getRegister()))
 					set.remove(p);
 			}
 		}
 		
 		return set;
 	}
 }
