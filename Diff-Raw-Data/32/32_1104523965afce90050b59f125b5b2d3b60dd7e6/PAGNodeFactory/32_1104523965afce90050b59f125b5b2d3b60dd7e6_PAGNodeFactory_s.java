 package soot.jimple.spark.builder;
 
 import soot.Context;
 import soot.Local;
 import soot.PointsToAnalysis;
 import soot.RefType;
 import soot.Scene;
 import soot.SootClass;
 import soot.SootMethod;
 import soot.Type;
 import soot.jimple.ClassConstant;
 import soot.jimple.spark.pag.AllocDotField;
 import soot.jimple.spark.pag.AllocNode;
 import soot.jimple.spark.pag.ClassConstantNode;
 import soot.jimple.spark.pag.ContextVarNode;
 import soot.jimple.spark.pag.FieldRefNode;
 import soot.jimple.spark.pag.GlobalVarNode;
 import soot.jimple.spark.pag.LocalVarNode;
 import soot.jimple.spark.pag.Node;
 import soot.jimple.spark.pag.PAG;
 import soot.jimple.spark.pag.Parm;
 import soot.jimple.spark.pag.SparkField;
 import soot.jimple.spark.pag.StringConstantNode;
 import soot.jimple.spark.pag.VarNode;
 import soot.options.SparkOptions;
 import soot.toolkits.scalar.Pair;
 
 public class PAGNodeFactory {
 
 	private static PAGNodeFactory instance;
 	public static PAGNodeFactory v() {
 		return instance;
 	}
 	public static void initialize(SparkOptions opts) {
		instance = new PAGNodeFactory(opts);
 	}
 
 	private SparkOptions opts;
 	private PAGNodeFactory(SparkOptions opts) {
 		this.opts = opts;
 	}
 
 	/*
 	 * ****************************************
 	 * moved all the makeXY methods from the PAG into here
 	 */
 
 	/** Finds or creates the ContextVarNode for base variable baseValue and context
 	 * context, of type type. */
 	public ContextVarNode makeContextVarNode( Object baseValue, Type baseType, Context context, SootMethod method ) {
 		LocalVarNode base = makeLocalVarNode( baseValue, baseType, method );
 		return makeContextVarNode( base, context );
 	}
 
 	/** Finds or creates the ContextVarNode for base variable base and context
 	 * context, of type type. */
 	public ContextVarNode makeContextVarNode( LocalVarNode base, Context context ) {
 		ContextVarNode ret = base.context( context );
 		if( ret == null ) {
 			ret = new ContextVarNode( base, context );			
 		}
 		return ret;
 	}
 
 	/** Finds or creates the FieldRefNode for base variable baseValue and field
 	 * field, of type type. */
 	public FieldRefNode makeLocalFieldRefNode( Object baseValue, Type baseType,
 			SparkField field, SootMethod method ) {
 		VarNode base = makeLocalVarNode( baseValue, baseType, method );
 		return makeFieldRefNode( base, field );
 	}
 	/** Finds or creates the FieldRefNode for base variable baseValue and field
 	 * field, of type type. */
 	public FieldRefNode makeGlobalFieldRefNode( Object baseValue, Type baseType,
 			SparkField field ) {
 		VarNode base = makeGlobalVarNode( baseValue, baseType );
 		return makeFieldRefNode( base, field );
 	}
 	/** Finds or creates the FieldRefNode for base variable base and field
 	 * field, of type type. */
 	public FieldRefNode makeFieldRefNode( VarNode base, SparkField field ) {
 		FieldRefNode ret = base.dot( field );
 		if( ret == null ) {
 			ret = new FieldRefNode( base, field );			 
 		}
 		return ret;
 	}
 
 	/** Finds or creates the GlobalVarNode for the variable value, of type type. */
 	public GlobalVarNode makeGlobalVarNode( Object value, Type type ) {
 		if( opts.rta() ) {
 			value = null;
 			type = RefType.v("java.lang.Object");
 		}
 		GlobalVarNode ret = new GlobalVarNode( value, type );		 
 
 		return ret;
 	}
 
 	/** Finds or creates the AllocDotField for base variable baseValue and field
 	 * field, of type t. */
 	public AllocDotField makeAllocDotField( AllocNode an, SparkField field ) {
 		AllocDotField ret = an.dot( field );
 		if( ret == null ) {
 			ret = new AllocDotField( an, field );
 		}
 		return ret;
 	}
 
 	public AllocNode makeAllocNode( Object newExpr, Type type, SootMethod m) {
 		if( opts.types_for_sites() || opts.vta() ) newExpr = type;
 		AllocNode ret = new AllocNode( newExpr, type, m );		 	 
 		return ret;
 	}
 	public AllocNode makeStringConstantNode( String s ) {
 		if( opts.types_for_sites() || opts.vta() )
 			return makeAllocNode( RefType.v( "java.lang.String" ),
 					RefType.v( "java.lang.String" ), null );
 		StringConstantNode ret = new StringConstantNode( s );		 
 
 		return ret;
 	}
 	public AllocNode makeClassConstantNode( ClassConstant cc ) {
 		if( opts.types_for_sites() || opts.vta() )
 			return makeAllocNode( RefType.v( "java.lang.Class" ),
 					RefType.v( "java.lang.Class" ), null );
 		ClassConstantNode ret = new ClassConstantNode(cc);		 
 
 		return ret;
 	}
 
 	/** Finds or creates the LocalVarNode for the variable value, of type type. */
 	public LocalVarNode makeLocalVarNode( Object value, Type type, SootMethod method ) {
 		if( opts.rta() ) {
 			value = null;
 			type = RefType.v("java.lang.Object");
 			method = null;
 		} else if( value instanceof Local ) {
 			Local val = (Local) value;
 			if( val.getNumber() == 0 ) Scene.v().getLocalNumberer().add(val);
 			LocalVarNode ret = new LocalVarNode(value, type, method );
 
 			return ret;
 		}
 		LocalVarNode ret = new LocalVarNode(value, type, method );
 
 		return ret;
 	}
 	
 	final public Node makeThis(SootMethod method) {
 		VarNode ret = makeLocalVarNode(new Pair<SootMethod,String>(method,
 				PointsToAnalysis.THIS_NODE), method.getDeclaringClass()
 				.getType(), method);
 		ret.setInterProcTarget();
 		return ret;
 	}
 
 	final public Node makeParm(SootMethod method, int index) {
 		VarNode ret = makeLocalVarNode(
 				new Pair<SootMethod,Integer>(method, new Integer(index)), method
 				.getParameterType(index), method);
 		ret.setInterProcTarget();
 		return ret;
 	}
 	
 	final public Node makeRet(SootMethod method) {
         VarNode ret = makeLocalVarNode(
                     Parm.v( method, PointsToAnalysis.RETURN_NODE ),
                     method.getReturnType(), method );
         ret.setInterProcSource();
         return ret;
     }
 	
 	public Node makeFinalizeQueue() {
 		return makeGlobalVarNode(PointsToAnalysis.FINALIZE_QUEUE, RefType.v("java.lang.Object"));
 	}
 	
 	public Node makeThrow() {
 		VarNode ret = makeGlobalVarNode( PointsToAnalysis.EXCEPTION_NODE,
 				RefType.v("java.lang.Throwable") );
 		ret.setInterProcTarget();
 		ret.setInterProcSource();
 		return ret;
 	}
 	
 	//note: this is the only makeXY method that actually alters the PAG!
 	public Node makeNewInstance( PAG pag, VarNode cls ) {
 		if( cls instanceof ContextVarNode ) cls = pag.findLocalVarNode( cls.getVariable() );
 		VarNode local = PAGNodeFactory.v().makeGlobalVarNode( cls, RefType.v( "java.lang.Object" ) );
 		for (SootClass cl : Scene.v().dynamicClasses()) {
 			AllocNode site = PAGNodeFactory.v().makeAllocNode( new Pair<VarNode,SootClass>(cls, cl), cl.getType(), null );
 			pag.addEdge( site, local );
 		}
 		return local;
 	}
 
 }
