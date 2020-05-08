 package xsched.analysis.pag;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import soot.Context;
 import soot.Local;
 import soot.MethodOrMethodContext;
 import soot.PointsToAnalysis;
 import soot.PointsToSet;
 import soot.SootField;
 import soot.SootMethod;
 import soot.Value;
 import soot.jimple.InvokeExpr;
 import soot.jimple.StringConstant;
 import soot.jimple.spark.internal.TypeManager;
 import soot.jimple.spark.pag.AllocDotField;
 import soot.jimple.spark.pag.AllocNode;
 import soot.jimple.spark.pag.ContextVarNode;
 import soot.jimple.spark.pag.FieldRefNode;
 import soot.jimple.spark.pag.GlobalVarNode;
 import soot.jimple.spark.pag.LocalVarNode;
 import soot.jimple.spark.pag.MethodPAG;
 import soot.jimple.spark.pag.Node;
 import soot.jimple.spark.pag.PAG;
 import soot.jimple.spark.pag.SparkField;
 import soot.jimple.spark.pag.VarNode;
 import soot.jimple.spark.sets.P2SetFactory;
 import soot.jimple.spark.solver.OnFlyCallGraph;
 import soot.jimple.toolkits.pointer.util.NativeMethodDriver;
 import soot.options.SparkOptions;
 import soot.toolkits.scalar.Pair;
 import soot.util.HashMultiMap;
 import soot.util.queue.QueueReader;
 import xsched.analysis.XSchedAnalyzer;
 
 public class Heap extends PAG {
 
 	private final PAG parent;
 	private boolean frozen = false;
 	private HashMap<InvokeExpr, NewHBRelationshipRecord> newHBRelationshipRecords = new HashMap<InvokeExpr, NewHBRelationshipRecord>();
 	private HashMap<InvokeExpr, NewActivationRecord> newActivationRecords = new HashMap<InvokeExpr, NewActivationRecord>();
 	
 	public Heap(PAG parent) {
		super();
 		this.parent = parent;
 		super.useOnFlyCallGraph();		
 		super.setNativeMethodDriver(parent.nativeMethodDriver());
 	}
 	
 	public void freeze() {
 		this.frozen = true;
 	}
 	
 	@Override
 	public MethodPAG methodPAGForMethod(SootMethod m) {
 		// the Heap has its own methodPAGs, so this method is OK
 		return super.methodPAGForMethod(m);
 	}
 
 	@Override
 	public NativeMethodDriver nativeMethodDriver() {
 		//we did set the nativeMethodDriver to our parent's, so this is OK, too
 		return super.nativeMethodDriver();
 	}
 
 	@Override
 	public void useOnFlyCallGraph() {
 		//we call super.useOnFlyCallGraph() in the constructor, so there's no need to call this
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	public void makeSureActivationMethodIsKnownInPAG(MethodOrMethodContext momc) {
 		this.ofcg.callGraph().reachableMethods().addCustomMethodOrMethodContext(momc);
 		this.ofcg.build();
 		
 	}
 	@Override
 	public HashMultiMap callAssigns() {
 		throw new RuntimeException("Don't use this method in the heap!");		
 	}
 
 	@Override
 	public Map<InvokeExpr, SootMethod> callToMethod() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public void setNativeMethodDriver(NativeMethodDriver driver) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Map<InvokeExpr, Node> virtualCallsToReceivers() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	public Collection<NewHBRelationshipRecord> newHBRelationshipRecords() {
 		return newHBRelationshipRecords.values();
 	}
 	
 	public Collection<NewActivationRecord> newActivationRecords() {
 		return newActivationRecords.values();
 	}
 	
 
 	@Override
 	public void addDereference(VarNode base) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	private void recordActivationConstructorCall(InvokeExpr expr, Node src, Node tgt) {
 		// constructor <init>(java.lang.Object,java.lang.String, ...)
 		NewActivationRecord act = newActivationRecords.get(expr);
 		if (act == null) {
 			act = new NewActivationRecord(expr);
 			newActivationRecords.put(expr, act);
 		}
 
 		assert (tgt instanceof LocalVarNode);
 		assert (((LocalVarNode) tgt).getVariable() instanceof Pair<?, ?>);
 		
 		LocalVarNode target = (LocalVarNode) tgt;
 		Object which = ((Pair<?, ?>) ((LocalVarNode) target)
 				.getVariable()).getO2();
 		if (which.equals(PointsToAnalysis.THIS_NODE)) {
 			assert (act.activation == null);
 			assert (src.getP2Set(this) != null);
 			List<Node> allocs = src.getP2Set(this).contents();
 			assert (allocs.size() == 1);
 			act.activation = (AllocNode) allocs.get(0);
 
 		} else if (which.equals(0)) {
 			assert (act.receivers == null);
 			act.receivers = src.getP2Set(this);
 		} else if (which.equals(1)) {
 			assert (act.task == null);
 			assert (src instanceof GlobalVarNode);
 			Value task = expr.getArgBox(1).getValue();
 			if (task instanceof StringConstant) {
 				act.task = ((StringConstant) task).value;
 			} else {
 				throw new RuntimeException(
 						"tasks must be specified as string constants!");
 			}
 		} else {
 			assert (which instanceof Integer);
 			// we assume that the param edges were created in the
 			// correct order, so we don't check too much here
 			assert (((Integer) which).intValue() - 2 == act.params.size());
 			act.params.add(src);
 		}
 	}
 	
 	private void handleNewHBRelationshipDeclaration(InvokeExpr expr, Node src, Node tgt) {
 		NewHBRelationshipRecord rel = newHBRelationshipRecords.get(expr);
 		if (rel == null) {
 			rel = new NewHBRelationshipRecord(expr);
 			newHBRelationshipRecords.put(expr, rel);
 		}
 
 		assert (tgt instanceof LocalVarNode);
 		assert (((LocalVarNode) tgt).getVariable() instanceof Pair<?, ?>);
 
 		LocalVarNode target = (LocalVarNode) tgt;
 		Object which = ((Pair<?, ?>) ((LocalVarNode) target)
 				.getVariable()).getO2();
 		if (which.equals(PointsToAnalysis.THIS_NODE)) {
 			assert (rel.lhs == null);
 			rel.lhs = src.getP2Set(this);
 		} else {
 			assert (which instanceof Integer);
 			assert (which.equals(0));
 			assert (rel.rhs == null);
 			rel.rhs = src.getP2Set(this);
 		}
 	}
 	
 	@Override
 	protected void recordCallAssign(InvokeExpr expr, Pair<?, ?> assignment) {
 		super.recordCallAssign(expr, assignment);
 		
 		SootMethod method = expr.getMethod();
 		if (method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR0)
 				|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR1)
 				|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR2)
 				|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR3)) {
 			
 			recordActivationConstructorCall(expr, (Node)assignment.getO1(), (Node)assignment.getO2());
 		} else if (expr.getMethod().equals(XSchedAnalyzer.HB_METHOD)) {
 			handleNewHBRelationshipDeclaration(expr, (Node)assignment.getO1(), (Node)assignment.getO2());
 		}
 		
 	}
 
 	@Override
 	protected boolean addToMap(Map<Object, Object> m, Node key, Node value) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Map<Object, Object> allocEdges() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Node[] allocInvLookup(VarNode key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Set<Object> allocInvSources() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Iterator<Object> allocInvSourcesIterator() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Node[] allocLookup(AllocNode key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Set<Object> allocSources() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Iterator<Object> allocSourcesIterator() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public void cleanUpMerges() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public boolean doAddAllocEdge(AllocNode from, VarNode to) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public boolean doAddLoadEdge(FieldRefNode from, VarNode to) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public boolean doAddSimpleEdge(VarNode from, VarNode to) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public boolean doAddStoreEdge(VarNode from, FieldRefNode to) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public QueueReader<?> edgeReader() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public AllocDotField findAllocDotField(AllocNode an, SparkField field) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public ContextVarNode findContextVarNode(Object baseValue, Context context) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public FieldRefNode findGlobalFieldRefNode(Object baseValue,
 			SparkField field) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public GlobalVarNode findGlobalVarNode(Object value) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public FieldRefNode findLocalFieldRefNode(Object baseValue, SparkField field) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public LocalVarNode findLocalVarNode(Object value) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public List<VarNode> getDereferences() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public OnFlyCallGraph getOnFlyCallGraph() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public SparkOptions getOpts() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public P2SetFactory getSetFactory() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public TypeManager getTypeManager() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Map<Object, Object> loadEdges() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Node[] loadInvLookup(VarNode key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Set<Object> loadInvSources() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Iterator<Object> loadInvSourcesIterator() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Node[] loadLookup(FieldRefNode key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Set<Object> loadSources() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Iterator<Object> loadSourcesIterator() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	protected Node[] lookup(Map<Object, Object> m, Object key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public PointsToSet reachingObjects(Context c, Local l, SootField f) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public PointsToSet reachingObjects(Context c, Local l) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public PointsToSet reachingObjects(Local l, SootField f) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public PointsToSet reachingObjects(Local l) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public PointsToSet reachingObjects(PointsToSet s, SootField f) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public PointsToSet reachingObjects(SootField f) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public PointsToSet reachingObjectsOfArrayElement(PointsToSet s) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Map<Object, Object> simpleEdges() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Node[] simpleInvLookup(VarNode key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Set<Object> simpleInvSources() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Iterator<Object> simpleInvSourcesIterator() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Node[] simpleLookup(VarNode key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Set<Object> simpleSources() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Iterator<Object> simpleSourcesIterator() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Map<Object, Object> storeEdges() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Node[] storeInvLookup(FieldRefNode key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Set<Object> storeInvSources() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Iterator<Object> storeInvSourcesIterator() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Node[] storeLookup(VarNode key) {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Set<Object> storeSources() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 	@Override
 	public Iterator<Object> storeSourcesIterator() {
 		throw new RuntimeException("Don't use this method in the heap!");
 	}
 
 }
