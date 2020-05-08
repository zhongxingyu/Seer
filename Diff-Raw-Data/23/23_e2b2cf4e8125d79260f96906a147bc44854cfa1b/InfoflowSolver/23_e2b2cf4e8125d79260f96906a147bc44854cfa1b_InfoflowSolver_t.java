 package soot.jimple.infoflow.heros;
 
 import heros.IFDSTabulationProblem;
 import heros.InterproceduralCFG;
 import heros.edgefunc.EdgeIdentity;
 import heros.solver.CountingThreadPoolExecutor;
 import heros.solver.IFDSSolver;
 import heros.solver.PathEdge;
 import soot.SootMethod;
 import soot.Unit;
 import soot.jimple.infoflow.data.Abstraction;
 import soot.jimple.toolkits.ide.JimpleIFDSSolver;
 
 public class InfoflowSolver extends JimpleIFDSSolver<Abstraction, InterproceduralCFG<Unit, SootMethod>> {
 
 	public InfoflowSolver(IFDSTabulationProblem<Unit, Abstraction, SootMethod, InterproceduralCFG<Unit, SootMethod>> problem, boolean dumpResults, CountingThreadPoolExecutor executor) {
 		super(problem, dumpResults);
 		this.executor = executor;
 	}
 	
 	@Override
 	protected CountingThreadPoolExecutor getExecutor() {
 		return executor;
 	}
 
 	public void processEdge(PathEdge<Unit, Abstraction, SootMethod> edge){
 		// We are generating a fact out of thin air here. If we have an
 		// edge <d1,n,d2>, there need not necessarily be a jump function
 		// to <n,d2>.
		if (jumpFn.reverseLookup(edge.getTarget(), edge.factAtTarget()).isEmpty())
			jumpFn.addFunction(edge.factAtSource(), edge.getTarget(), edge.factAtSource(),
					EdgeIdentity.<IFDSSolver.BinaryDomain>v());
 		scheduleEdgeProcessing(edge);
 	}
 }
