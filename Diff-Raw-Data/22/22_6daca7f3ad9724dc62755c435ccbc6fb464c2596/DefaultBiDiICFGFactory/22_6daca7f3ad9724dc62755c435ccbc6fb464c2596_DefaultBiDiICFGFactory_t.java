 /**
  * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
  * All rights reserved
  */
 package soot.jimple.infoflow;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Marc-Andre Lavadiere
  */
 public class DefaultBiDiICFGFactory implements BiDirICFGFactory {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
     @Override
     public IInfoflowCFG buildBiDirICFG(CallgraphAlgorithm callgraphAlgorithm){
     	if (callgraphAlgorithm == CallgraphAlgorithm.OnDemand) {
    		// Collect all application classes
     		Set<SootMethod> applicationMethods = new HashSet<SootMethod>();
     		for (SootClass sc : Scene.v().getApplicationClasses())
     			for (SootMethod sm : sc.getMethods())
     				applicationMethods.add(sm);
    		
    		Scene.v().getOrMakeFastHierarchy();
    		
    		// Load all classes on the classpath to signatures
    		long beforeClassLoading = System.nanoTime();
     		OnTheFlyJimpleBasedICFG.loadAllClassesOnClassPathToSignatures();
    		logger.info("Class loading took {} seconds", (System.nanoTime() - beforeClassLoading) / 1E9);
    		
     		return new InfoflowCFG(new OnTheFlyJimpleBasedICFG(applicationMethods));
     	}
         return new InfoflowCFG();
     }
 }
