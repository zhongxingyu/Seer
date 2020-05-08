 /**
  * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
  * All rights reserved
  */
 package soot.jimple.infoflow;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 import soot.Scene;
 import soot.SootClass;
 import soot.SootMethod;
 import soot.jimple.infoflow.IInfoflow.CallgraphAlgorithm;
 import soot.jimple.infoflow.solver.IInfoflowCFG;
 import soot.jimple.infoflow.solver.InfoflowCFG;
 import soot.jimple.toolkits.ide.icfg.OnTheFlyJimpleBasedICFG;
 
 /**
  * Default factory for bidirectional interprocedural CFGs
  * 
  * @author Steven Arzt
 * @author Marc-Andr Lavadiere
  */
 public class DefaultBiDiICFGFactory implements BiDirICFGFactory {

     @Override
     public IInfoflowCFG buildBiDirICFG(CallgraphAlgorithm callgraphAlgorithm){
     	if (callgraphAlgorithm == CallgraphAlgorithm.OnDemand) {
     		Set<SootMethod> applicationMethods = new HashSet<SootMethod>();
     		for (SootClass sc : Scene.v().getApplicationClasses())
     			for (SootMethod sm : sc.getMethods())
     				applicationMethods.add(sm);
     		OnTheFlyJimpleBasedICFG.loadAllClassesOnClassPathToSignatures();
     		return new InfoflowCFG(new OnTheFlyJimpleBasedICFG(applicationMethods));
     	}
         return new InfoflowCFG();
     }
 }
