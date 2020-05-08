 package soot.jimple.infoflow.source;
 
 import soot.SootMethod;
 
 /**
  * A {@link SourceSinkManager} that always returns false, i.e. one for which
  * there are no sources or sinks at all.
  * 
  * @author Steven Arzt
  */
 public class EmptySourceSinkManager extends MethodBasedSourceSinkManager {
 
 	public EmptySourceSinkManager(){
 	}
 	
 	@Override
 	public boolean isSourceMethod(SootMethod sMethod) {
 		return false;
 	}
 
 	@Override
 	public boolean isSinkMethod(SootMethod sMethod) {
 		return false;
 	}
 
 }
