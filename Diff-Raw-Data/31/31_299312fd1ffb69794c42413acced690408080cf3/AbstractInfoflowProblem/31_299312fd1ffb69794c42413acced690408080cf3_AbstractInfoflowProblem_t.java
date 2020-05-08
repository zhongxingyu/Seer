 package soot.jimple.infoflow;
 
 import heros.InterproceduralCFG;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import soot.NullType;
 import soot.PrimType;
 import soot.RefType;
 import soot.SootMethod;
 import soot.Unit;
 import soot.Value;
import soot.jimple.ArrayRef;
 import soot.jimple.Constant;
 import soot.jimple.InstanceFieldRef;
 import soot.jimple.infoflow.data.Abstraction;
 import soot.jimple.infoflow.data.AbstractionWithPath;
 import soot.jimple.infoflow.nativ.DefaultNativeCallHandler;
 import soot.jimple.infoflow.nativ.NativeCallHandler;
 import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
 import soot.jimple.infoflow.util.DataTypeHandler;
 import soot.jimple.internal.JimpleLocal;
 import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;
 import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
 /**
  * abstract super class which 
  * 	- concentrates functionality used by InfoflowProblem and BackwardsInfoflowProblem
  *  - contains helper functions which should not pollute the naturally large InfofflowProblems
  *
  */
 public abstract class AbstractInfoflowProblem extends DefaultJimpleIFDSTabulationProblem<Abstraction, InterproceduralCFG<Unit, SootMethod>> {
 
 	/**
 	 * Supported methods for tracking data flow paths through applications
 	 * @author sarzt
 	 *
 	 */
 	public enum PathTrackingMethod {
 		/**
 		 * Do not track any paths. Just search for connections between sources
 		 * and sinks, but forget about the path between them.
 		 */
 		NoTracking,
 		
 		/**
 		 * Perform a simple forward tracking. Whenever propagating taint
 		 * information, also track the current statement on the path. Consumes
 		 * a lot of memory.
 		 */
 		ForwardTracking
 	}
 
 	
 	
 	protected final Map<Unit, Set<Abstraction>> initialSeeds = new HashMap<Unit, Set<Abstraction>>();
 	protected final InfoflowResults results;
 	protected ITaintPropagationWrapper taintWrapper;
 	protected PathTrackingMethod pathTracking = PathTrackingMethod.NoTracking;
 	protected NativeCallHandler ncHandler = new DefaultNativeCallHandler();
 	protected boolean debug = false;
 	protected boolean inspectSinks = true;
 
 	Abstraction zeroValue = null;
 	
 	protected boolean stopAfterFirstFlow = false;
 	public AbstractInfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
 		super(icfg);
 		results = new InfoflowResults();
 	}
 	
 	/**
 	 * we need this option as we start directly at the sources, but need to go 
 	 * backward in the call stack
 	 */
 	@Override
 	public boolean followReturnsPastSeeds(){
 		return true;
 	}
 	
 	public void setTaintWrapper(ITaintPropagationWrapper wrapper){
 		taintWrapper = wrapper;
 	}
 	
 	public void setDebug(boolean debug){
 		this.debug = debug;
 	}
 	
 	/**
 	 * Sets whether the information flow analysis shall stop after the first
 	 * flow has been found
 	 * @param stopAfterFirstFlow True if the analysis shall stop after the
 	 * first flow has been found, otherwise false.
 	 */
 	public void setStopAfterFirstFlow(boolean stopAfterFirstFlow) {
 		this.stopAfterFirstFlow = stopAfterFirstFlow;
 	}
 	
 	
 	/**
 	 * Sets whether and how the paths between the sources and sinks shall be
 	 * tracked
 	 * @param method The method for tracking data flow paths through the
 	 * program.
 	 */
 	public void setPathTracking(PathTrackingMethod method) {
 		this.pathTracking = method;
 		this.ncHandler.setPathTracking(method);
 	}
 	
 
 	@Override
 	public Abstraction createZeroValue() {
 		if (zeroValue == null) {
 			zeroValue = this.pathTracking == PathTrackingMethod.NoTracking ?
 				new Abstraction(new JimpleLocal("zero", NullType.v()), null, null, false, true, null) :
 				new AbstractionWithPath(new JimpleLocal("zero", NullType.v()), null, null, false, true, null);
 			zeroValue.setZeroAbstraction(zeroValue.clone());
 		}
 		return zeroValue;
 	}
 	
 	public InfoflowResults getResults(){
 	    return results;
 	}
 
 	/**
 	 * Gets whether the given method is an entry point, i.e. one of the initial
 	 * seeds belongs to the given method
 	 * @param sm The method to check
 	 * @return True if the given method is an entry point, otherwise false
 	 */
 	protected boolean isInitialMethod(SootMethod sm) {
 		for (Unit u : this.initialSeeds.keySet())
 			if (interproceduralCFG().getMethodOf(u) == sm)
 				return true;
 		return false;
 	}
 	
 	@Override
 	public Map<Unit, Set<Abstraction>> initialSeeds() {
 		return initialSeeds;
 	}
 	
 	/**
 	 * performance improvement: since we start directly at the sources, we do not 
 	 * need to generate additional taints unconditionally
 	 */
 	@Override
 	public boolean autoAddZero() {
 		return false;
 	}
 	
 	/**
 	 * default: inspectSinks is set to true, this means sinks are analyzed as well.
 	 * If inspectSinks is set to false, then the analysis does not propagate values into 
 	 * the sink method. 
 	 * @param inspect boolean that determines the inspectSink option
 	 */
 	public void setInspectSinks(boolean inspect){
 		inspectSinks = inspect;
 	}
 	
 	/**
 	 * returns if the value is transferable (= no primitive datatype / immutable datatype / Constant)
 	 * @param val the value which should be analyzed
 	 * @return
 	 */
 	public boolean isTransferableValue(Value val){
 		if(val == null){
 			return false;
 		}
 		//no string
 		if(!(val instanceof InstanceFieldRef) && val.getType() instanceof RefType && ((RefType)val.getType()).getClassName().equals("java.lang.String")){
 			return false;
 		}
 		if(val instanceof InstanceFieldRef && ((InstanceFieldRef)val).getBase().getType() instanceof RefType &&
 				 ((RefType)((InstanceFieldRef)val).getBase().getType()).getClassName().equals("java.lang.String")){
 			return false;
 		}
 		if(val.getType() instanceof PrimType){
 			return false;
 		}
 		if(val instanceof Constant)
 			return false;
 		
 		if(DataTypeHandler.isFieldRefOrArrayRef(val))
 			return true;
 		
 		return false;
 	}
 	
 	
 	/**
 	 * we cannot rely just on "real" heap objects, but must also inspect locals because of Jimple's representation ($r0 =... )
 	 * @param val the value which gets tainted
 	 * @param source the source from which the taints comes from. Important if not the value, but a field is tainted
 	 * @return true if a reverseFlow should be triggered or an inactive taint should be propagated (= resulting object is stored in heap = alias)
 	 */
 	public boolean triggerInaktiveTaintOrReverseFlow(Value val, Abstraction source){
 		if(val == null){
 			return false;
 		}
 		//no string
		if(!(val instanceof InstanceFieldRef) && !(val instanceof ArrayRef) 
				&& val.getType() instanceof RefType && ((RefType)val.getType()).getClassName().equals("java.lang.String")){
 			return false;
 		}
 		if(val instanceof InstanceFieldRef && ((InstanceFieldRef)val).getBase().getType() instanceof RefType &&
 				 ((RefType)((InstanceFieldRef)val).getBase().getType()).getClassName().equals("java.lang.String")){
 			return false;
 		}
 		if(val.getType() instanceof PrimType){
 			return false;
 		}
 		if(val instanceof Constant)
 			return false;
 		
 		 if(DataTypeHandler.isFieldRefOrArrayRef(val) ||
 				source.getAccessPath().isInstanceFieldRef() ||
 				source.getAccessPath().isStaticFieldRef()){
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public BiDiInterproceduralCFG<Unit, SootMethod> interproceduralCFG() {
 		return (BiDiInterproceduralCFG<Unit, SootMethod>) super.interproceduralCFG();
 	}
 
 }
