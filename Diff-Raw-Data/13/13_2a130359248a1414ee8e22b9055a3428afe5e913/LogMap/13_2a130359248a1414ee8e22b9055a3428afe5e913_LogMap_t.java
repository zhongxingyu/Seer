 package polyglot.ext.esj.tologic;
 
 import polyglot.ext.esj.primitives.*;
 import polyglot.ext.esj.solver.Kodkodi.Kodkodi;
 
 import java.util.AbstractCollection;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Collection;
 import java.util.Iterator;
 
 import java.io.CharArrayWriter;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Field;
 import java.lang.reflect.Constructor;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.Lexer;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.CommonTokenStream;
 
 import kodkod.ast.Formula;
 import kodkod.ast.operator.FormulaOperator;
 import kodkod.ast.Expression;
 import kodkod.ast.Relation;
 import kodkod.ast.IntConstant;
 import kodkod.instance.TupleFactory;
 import kodkod.instance.TupleSet;
 import kodkod.instance.Tuple;
 import kodkod.instance.Bounds;
 import kodkod.instance.Universe;
 import kodkod.instance.Instance;
 import kodkod.engine.Solver;
 import kodkod.engine.Solution;
 import kodkod.engine.satlab.SATFactory;
 import kodkod.engine.config.Options;
 
 public class LogMap {
 
     static String SolverOpt_Solver = "\"MiniSat\"";
     static SATFactory SolverOpt_Solver2 = SATFactory.MiniSat;
     static String SolverOpt_Host = "localhost";
     static int SolverOpt_Port = 9128;
     static String SolverOpt_Flatten = "false";
     static int SolverOpt_SymmetryBreaking = 20;
     static int SolverOpt_debugLevel = 0;
     static boolean SolverOpt_debug1 = false, SolverOpt_debug2 = false;
     static boolean SolverOpt_Kodkod = true, SolverOpt_Kodkodi = false;
 
     static HashMap JtoLog; // = new HashMap(); // Java Objs to Solver Atoms
     static HashMap JtoLog2; // = new HashMap(); // Java Objs to Kodkod (singleton) relations
     static HashMap LogtoJ; // = new HashMap(); 
     static HashMap ProblemRels; // Holds relations for given problem  
     static HashMap<Object,Relation> ClassRels; // Holds kodkod relations for each class
     static ArrayList<Integer> ProblemAtoms; // = new HashSet(); // kodkod atoms
     static Bounds ProblemBounds; // kodkod bounds
     static TupleFactory ProblemFactory; // kodkod factory
     static HashMap InstVarRels = new HashMap(); // Holds inst var relations for each class
     static HashMap InstVarRels2 = new HashMap(); // Holds inst var Kodkod relations for each class
     static HashMap ClassAtoms = new HashMap(); // Holds atoms for each class
     static HashMap ClassConstrs = new HashMap(); // Holds class constr for each class
     static HashMap Enums = new HashMap(); // Holds enums
     //static HashMap EnumAtoms = new HashMap(); // Holds atoms for enums
     
     static int AtomCtr = ESJInteger.BoundsSize(); // mapping of objs to number ids
     static int relationizerStep = 0; // time steps get incremented when ensured mtd is run
                                             // used to keep track which objs need to be 
                                             // re-relationized at current time (since may've been
                                             // updated.
     static int clonerStep = 0; //FIXME (should be same as relationizerStep)
 
     /*
     // insert mappings for null and enums
     static {
 	LogtoJ.put(AtomCtr,null);
 	LogMap.put1(null,AtomCtr++);
     }
     */
 
     public static void setNullAtomAt(int i) {
 	LogtoJ.put(i,null);
 	LogMap.put1(null,i);
     }
 
     public static int SolverOpt_debugLevel() { return SolverOpt_debugLevel; }
     public static boolean SolverOpt_debug1() { return SolverOpt_debug1; }
     public static boolean SolverOpt_debug2() { return SolverOpt_debug2; }
 
     public static boolean SolverOpt_Kodkod() { return SolverOpt_Kodkod; }
     public static boolean SolverOpt_Kodkodi() { return SolverOpt_Kodkodi; }
     public static void SolverOpt_Kodkod(boolean b) { SolverOpt_Kodkod = b; }
     public static void SolverOpt_Kodkodi(boolean b) { SolverOpt_Kodkodi = b; }
    public static void SolverOpt_Solver(String s) {  SolverOpt_Solver = s; }
    public static void SolverOpt_Solver2(SATFactory s) {  SolverOpt_Solver2 = s; }
 
     public static void SolverOpt_debugLevel(int l) {
 	SolverOpt_debugLevel = l;
 	if (l > 0)
 	    SolverOpt_debug1 = true;
 	if (l > 1)
 	    SolverOpt_debug2 = true;
     }
 
     public static int relationizerStep() { return relationizerStep; }
     public static void incrRelationizerStep() { relationizerStep++; }
     public static int clonerStep() { return clonerStep; }
     public static void incrClonerStep() { clonerStep++; }
     public static HashMap ProblemRels() { return ProblemRels; }
     public static TupleFactory ProblemFactory() { return ProblemFactory; }
     public static Bounds ProblemBounds() { return ProblemBounds; }
     public static HashMap<Object,Relation> ClassRels() { return ClassRels; }
     public static HashMap ClassAtoms() { return ClassAtoms; }
 
     public static void resetMaps() {
 	JtoLog = new HashMap();
 	LogtoJ = new HashMap();
 	JtoLog2 = new HashMap();    
 	ProblemAtoms = new ArrayList();       
     }
 
     public static void initRelationize() {
 	// FIXME?	
 	/*
 	JtoLog = new HashMap();
 	LogtoJ = new HashMap();
 	ESJInteger.setBounds(ESJInteger.MIN_VALUE,ESJInteger.MAX_VALUE);
 	*/
 	ProblemRels = new HashMap();
 	ClassRels = new HashMap<Object,Relation>();
 
 	AtomCtr = ESJInteger.BoundsSize()+1;
 	relationizerStep++;
 	clonerStep++;
     }
 
 
     public static void ObjToAtomMap() {
 
 	if (SolverOpt_debug1)
 	    System.out.println("classes: " + ClassAtoms);
 
 	for (Class c : (Set<Class>) ClassAtoms.keySet()) {
 
 	    // make atoms from objs
 	    boolean isEnum = c.isEnum();
 	    // reset rels FIXME	   
 	    if (!isEnum) 
 		for (LogRelation r : (Collection<LogRelation>) ((HashMap) InstVarRels.get(c)).values())
 		    r.clear();
 
 	    ClassAtoms.put(c, new ArrayList());
 	    newAtoms(c, isEnum);
 	}
 	if (SolverOpt_debug1)
 	    System.out.println(" JtoLog --> " + JtoLog);
 
 	//FIXME: move..
 	initKodkod();
     }
 
     public static void initKodkod() {
 	// init kodkod problem
 	Universe universe = new Universe(LogMap.ProblemAtoms);
 	LogMap.ProblemFactory = universe.factory();
 	LogMap.ProblemBounds = new Bounds(universe);
 
 	ESJInteger.intBounds_log2();
 	if (SolverOpt_debug1)
 	    System.out.println(ClassRels);
     }
 
     public static void newAtoms(Class c, boolean isEnum) { // FIXME?
 	if (SolverOpt_debug1)
 	    System.out.println("initing class: " + c + " (ctr=" + AtomCtr+")" + "\n");
 
 	ArrayList classAs = (ArrayList) ClassAtoms.get(c);
 	try {
 	    ArrayList objs;
 	    Object [] args = new Object[2];
 	    args[0] = null;
 	    args[1] = false;
 
 	    objs = isEnum ? (ArrayList) Enums.get(c) : ((ESJObject) ((Constructor) ClassConstrs.get(c)).newInstance(args)).allInstances2();
 	    if (SolverOpt_debug1)
 		System.out.println("objs: " + objs);
 
 	    for (Object obj : objs) {
 		classAs.add(AtomCtr);
 		LogtoJ.put(AtomCtr,obj);
 		LogMap.put1(obj,AtomCtr);
 		if (!isEnum) {
 		    ESJObject o = (ESJObject) obj;
 		    // FIXME? (if new obj instantiated during faulty method...)
 		    //if (!o.isCloned())
 		    //continue;
 		    if (o.old() != null)
 			LogMap.put1(o.old(),AtomCtr);
 		}
 		AtomCtr++;
 	    }
 
 	} catch (Exception e) { e.printStackTrace(); System.exit(1); }
     }
 
     public static void put1(Object key, int value) { 
 	JtoLog.put(key,value);
 	//String valueStr = "A" + value;
 	if (!ProblemAtoms.contains(value)) //Str))
 	    ProblemAtoms.add(value); //Str);
     }
 
     public static int get1(Object key) {
 
 	if (SolverOpt_debug2) {
 	    System.out.println("get1: " + key);
 	    System.out.println(" --> " + JtoLog.get(key));
 	    System.out.println(JtoLog);
 	    }
 	return (Integer) JtoLog.get(key);
     }
 
     public static HashSet<Integer> get1s(HashSet<?> s) {
 	HashSet<Integer> res = new HashSet<Integer>();
 	for (Object o : s)
 	    res.add(get1(o));
 	return res;		    
     }
 
     public static String get1_log(Object key) { 
 	return "A" + get1(key);
     }
 
     public static Relation get1_log2(Object key) { 
 	if (JtoLog2.containsKey(key)) 
 	    return (Relation) JtoLog2.get(key);
 	Relation r = Relation.unary(get1_log(key));
 	JtoLog2.put(key,r);
 	return r;
     }
 
     public static void put2(int key, Object value) { 
 	LogtoJ.put(key,value);
     }
 
     public static Object get2(int key) { 
 	return LogtoJ.get((Object)key);
     }
 
     // FIXME
     public static LogSet bounds_log(Class c, boolean addNull, boolean isBoundDef) {
 	if (SolverOpt_debug1) {
 	    System.out.println("getting log bounds for: " + c);
 	    System.out.println(c);
 	    System.out.println(ClassAtoms);
 	}
 	if (c == int.class || c == Integer.class) 
 	    return ESJInteger.allInstances_log();
 	ArrayList atoms = (ArrayList) ClassAtoms.get(c);	    
 	String addL = "";
 	String addR = "";
 	if (isBoundDef) {
 	    addL = "{[";
 	    addR = "]}";
 	}
 	return new LogSet("(" + (addNull ? addL+get1_log(null) + addR + " + " : "") + "u" + atoms.size() + (atoms.size() > 0 ? ("@" + atoms.get(0)) : "") + ")");
 
     }
 
     public static Relation bounds_log2(Class c, boolean isBoundDef) {
 	if (SolverOpt_debug1) {
 	    System.out.println("getting log bounds for: " + c);
 	    System.out.println(c);
 	    System.out.println(ClassAtoms);
 	}
 	if (ClassRels.containsKey(c))
 	    return ClassRels.get(c);
 	Relation cRel = Relation.unary(c.getName());	
 
       	TupleSet cRel_upper = ProblemFactory.noneOf(1);
 	ArrayList atoms = (ArrayList) ClassAtoms.get(c);
 	for (Object a : atoms)
 	    cRel_upper.add(ProblemFactory.tuple(a));
 	    
 	ProblemBounds.boundExactly(cRel, cRel_upper);
 	ClassRels.put(c,cRel);
 	
 	return cRel;
     }
 
     public static TupleSet tupleSetBounds_log2(Class range, boolean addNull, HashSet<?> modifiableObjects) {
 	TupleFactory factory = LogMap.ProblemFactory();
 	TupleSet rB = factory.noneOf(1);
 	ArrayList atoms = (ArrayList) LogMap.ClassAtoms().get(range);
 	if (modifiableObjects == null)
 	    for (Object a : atoms)
 		rB.add(ProblemFactory.tuple(a));
 	else
 	    for (Object a : atoms)
 		if (modifiableObjects.contains(a))
 		    rB.add(ProblemFactory.tuple(a));
 	if (addNull)
 	    rB.add(factory.tuple(LogMap.get1(null)));
 	return rB;
     }
 
     public static String newInstVarRel(Class c, String instVar, Class domain, Class range, Class indexingDomain, boolean isaList, boolean isaMap, boolean isUnknown, boolean isResultVar) {
 	String k = instVar;
 	boolean isaCollection = isaList || isaMap;
 	int relSize = isaCollection ? 3 : 2;
 	if (!isUnknown) {
 	    k += "_old";
 	    // mark enum and class
 	    if (!(ClassAtoms.containsKey(range) || range == int.class || range == Integer.class)) {
 		try {
 		    ClassAtoms.put(range, new ArrayList());
 		    if (range.isEnum()) {
 			ArrayList es = new ArrayList();
 			for (Object e : range.getEnumConstants())
 			    es.add(e);
 			Enums.put(range, es);
 		    } else {
 			Class[] parameterTypes = new Class[2];
 			parameterTypes[0] = LogVar.class;
 			parameterTypes[1] = boolean.class;
 			ClassConstrs.put(range, range.getConstructor(parameterTypes));
 		    }
 		} catch (Exception e) { System.out.println(e); System.exit(1); }		
 	    }
 	    if (!ClassAtoms.containsKey(c)) {
 		ClassAtoms.put(c, new ArrayList());
 		Class[] parameterTypes = new Class[2];
 		parameterTypes[0] = LogVar.class;
 		parameterTypes[1] = boolean.class;
 		try {
 		    ClassConstrs.put(c, c.getConstructor(parameterTypes));
 		} catch (NoSuchMethodException e) { System.out.println(e); System.exit(1); }
 		if (!InstVarRels.containsKey(range)) {
 		    InstVarRels.put(range, new HashMap());
 		    InstVarRels2.put(range, new HashMap());
 	}
 
 	    }
 	}
 	String rN = LogMap.shortClassName(c) + "." + k;
 	Relation r2 = isResultVar ? Relation.unary(rN) : Relation.nary(rN, relSize);
 	LogRelation r = new LogRelation(instVar, domain, range, indexingDomain, r2, isaList, isaMap, isUnknown, isResultVar);
 
 	if (!InstVarRels.containsKey(c)) {
 	    InstVarRels.put(c, new HashMap());
 	    InstVarRels2.put(c, new HashMap());
 	}
 	((HashMap) InstVarRels.get(c)).put(k,r);
 	((HashMap) InstVarRels2.get(c)).put(k,r2);
 	if (SolverOpt_debug1)
 	    System.out.println("InstVarRels:" + InstVarRels);
 	return r.id;
     }
 
     //FIXME
     public static LogRelation instVarRel_log(Object obj, String instVar) {
 	return (LogRelation) ((HashMap) InstVarRels.get(obj.getClass())).get(instVar);
     }
 
     public static Relation instVarRel_log2(Object obj, String instVar) {
 	String id = instVarRel_log(obj, instVar).id(); // marks it involved rel
 	return (Relation) ((HashMap) InstVarRels2.get(obj.getClass())).get(instVar);
     }
 
     //FIXME
     public static LogRelation instVarRel_log(Class c, String instVar) {
 	return (LogRelation) ((HashMap) InstVarRels.get(c)).get(instVar);
     }
 
     public static Relation instVarRel_log2(Class c, String instVar) {
 	String id = instVarRel_log(c, instVar).id(); // marks it involved rel
 	return (Relation) ((HashMap) InstVarRels2.get(c)).get(instVar);
     }
 
     public static LogRelation instVarRelOld_log(LogRelation r) {
 	return (LogRelation) ((HashMap) InstVarRels.get(r.domain())).get(r.instVar()+"_old");
     }
 
     public static Relation instVarRelOld_log2(LogRelation r) {
 	return (Relation) ((HashMap) InstVarRels2.get(r.domain())).get(r.instVar()+"_old");
     }
 
     public static LogRelation getResultVarRel_log(Object obj, String typeName) {
 	return (LogRelation) ((HashMap) InstVarRels.get(obj.getClass())).get("result" + typeName);
     }
 
     public static Relation getResultVarRel_log2(Object obj, String typeName) {
 	LogRelation r = getResultVarRel_log(obj, typeName);
 	LogMap.addAsProblemRel(r,r.id());
 	return (Relation) ((HashMap) InstVarRels2.get(obj.getClass())).get("result" + typeName);
     }
 
 
     public static LogObjAtom objInstVar_log(Object obj, String instVar) {
 	//System.out.println("instVar_log Object " + obj.getClass());
 	return new LogObjAtom("(" + get1_log(obj) + "." + instVarRel_log(obj, instVar).id() + ")");
     }
 
     // FIXME
     public static LogVar objInstVarStr_log(ESJObject obj, String instVar, Class c) {
 	String s1 = obj.isQuantifyVar() ? obj.var_log().string() : get1_log(obj); 
 	String s2 = instVarRel_log(obj, instVar).id();
 	return new LogVar("(" + s1 + "." + s2 + ")");
     }
 
     public static Log2Var objInstVarStr_log2(ESJObject obj, String instVar, Class c) {
 	Relation s2 = instVarRel_log2(obj, instVar);
 	Expression s1 = obj.isQuantifyVar2() ? 
 	    obj.var_log2().expression() : objToSingletonRelation_log2(obj).expression();
 	return new Log2Var(s1.join(s2));
     }
 
     public static LogSet objInstVarSet_log(ESJObject obj, String instVar) {
 	LogRelation r = instVarRel_log(obj, instVar);
 	return new LogSet("(" + (obj.isQuantifyVar() ? obj.var_log().string() : get1_log(obj)) + "." + r.id() + ")", 0, r.isaCollectionInstVar());
     }
 
     public static Log2Set objInstVarSet_log2(ESJObject obj, String instVar) {
 	Relation s2 = instVarRel_log2(obj, instVar);
 	Expression s1 = obj.isQuantifyVar2() ? 
 	    obj.var_log2().expression() : objToSingletonRelation_log2(obj).expression();
 	return new Log2Set(s1.join(s2));
     }
 
     public static Log2Object objToSingletonRelation_log2(Object obj) {
 	    Relation objRel;
 	    if (ClassRels.containsKey(obj))
 		objRel = ClassRels.get(obj);
 	    else {
 		Integer objAtom = get1(obj);
 		objRel = Relation.unary("A" + objAtom);
 		TupleSet obj_upper = ProblemFactory.noneOf(1);
 		obj_upper.add(ProblemFactory.tuple(objAtom));
 		ProblemBounds.boundExactly(objRel, obj_upper);
 		ClassRels.put(obj,objRel);
 	    }
 	    return new Log2Object(objRel);
     }
 
     public static Log2IntAtom intToSingletonRelation_log2(Integer i) {
 	return new Log2IntAtom(IntConstant.constant(i)); //ESJInteger.atom_log2(i));
     }
 
     public static LogObjAtom null_log() {
 	return new LogObjAtom(get1_log(null));
     }
 
     public static Relation null_log2() {
 	//return new Log2Object(ClassRels.get(null));
 	return ClassRels.get(null);
     }
 
     // FIXME
     public static LogSet instVarClosure_log(ESJObject obj, boolean isOld, boolean isSimple, boolean isReflexive, String... instVars) {
 	if (SolverOpt_debug1)
 	    System.out.println("instVarClosure_log -> idOld: " + isOld + " isVar: " + (obj.var_log() != null));
 	String fA = isOld ? "_old" : "";
 	String fNs = instVarRel_log(obj, instVars[0]+fA).id();
 	if (instVars.length > 1) {
 	    fNs = "(" + fNs;
 	    for(int i=1;i<instVars.length;i++)
 		fNs += (" + " + instVarRel_log(obj, instVars[i]+fA).id());
 	    fNs += ")";
 	}
 	return new LogSet("(" + (obj.isQuantifyVar() ? obj.var_log().string() : get1_log(obj)) + "." + (isSimple ? "" : (isReflexive ? "*" : "^")) + fNs + " - " + get1_log(null) + ")", obj.getClass()); //FIXME: get_log(null) ?
     }
 
    public static Log2Set instVarClosure_log2(ESJObject obj, boolean isOld, boolean isSimple, boolean isReflexive, String... instVars) {
 	//if (SolverOpt_debug1)
 	//System.out.println("instVarClosure_log -> idOld: " + isOld + " isVar: " + (obj.var_log() != null));
 	String fA = isOld ? "_old" : "";
 	Expression fNs = instVarRel_log2(obj, instVars[0]+fA);
 	for(int i=1;i<instVars.length;i++)
 	    fNs = fNs.union(instVarRel_log2(obj, instVars[i]+fA));
 	Expression s1 = obj.isQuantifyVar2() ? 
 	    obj.var_log2().expression() : objToSingletonRelation_log2(obj).expression();
 	if (!isSimple)
 	    if (isReflexive)
 		fNs = fNs.reflexiveClosure();
 	    else
 		fNs = fNs.closure();
 	Expression res = s1.join(fNs).difference(ClassRels.get(null));
 	return new Log2Set(res, 0, obj.getClass());
     }
 
     //FIXME: this is not closure: a setmap of fields
     public static LogSet instVarClosure_log(LogSet obj, boolean isOld, boolean isSimple, boolean isReflexive, String... instVars) {
 	Class range = obj.range();
 	String fA = isOld ? "_old" : "";
 	String fNs = instVarRel_log(range, instVars[0]+fA).id();
 	if (instVars.length > 1) {
 	    fNs = "(" + fNs;
 	    for(int i=1;i<instVars.length;i++)
 		fNs += (" + " + instVarRel_log(range, instVars[i]+fA).id());
 	    fNs += ")";
 	}
 	return new LogSet("(" + obj.string() + "." + fNs + " - " + get1_log(null) + " )"); //FIXME: get_log(null) ?
     }
 
     //FIXME: this is not closure: a setmap of fields
     public static Log2Set instVarClosure_log2(Log2Set obj, boolean isOld, boolean isSimple, boolean isReflexive, String... instVars) {
 	Class range = obj.range();
 	String fA = isOld ? "_old" : "";
 	Expression fNs = instVarRel_log2(range, instVars[0]+fA);
 	for(int i=1;i<instVars.length;i++)
 	    fNs = fNs.union(instVarRel_log2(range, instVars[i]+fA));
 	Expression s1 = obj.expression();
 	Expression res = s1.join(fNs);
 	res = res.difference(ClassRels.get(null));
 	return new Log2Set(res);
     }
 
 
     public static boolean solve(Object obj, Object formula, Class resultVarType, HashMap<String,String> modifiableFields, HashSet<?> modifiableObjects) {
 
 	CharArrayWriter problem = new CharArrayWriter();
 	CharArrayWriter funDefs = new CharArrayWriter();
 	ArrayList unknowns = new ArrayList<LogRelation>();
 	String spacer = "\n";
 	boolean isNonVoid = resultVarType != null;
 	Set probRels = new HashSet(ProblemRels.keySet());
 	if (SolverOpt_debug1) {
 	    System.out.println("problem involves rels: ");
 	    for (Object k : probRels) {
 		LogRelation r =  (LogRelation) ProblemRels.get(k);
 		System.out.println(r.getId() + ": " + r.instVar());
 	    }
 	    if (modifiableFields != null)
 		System.out.println("modifiable fields: " + modifiableFields);
 	    if (modifiableObjects != null)
 		System.out.println("modifiable objs: " + modifiableObjects);
 	}
 
 	problem.append("solver: " + SolverOpt_Solver + spacer);
 	problem.append("symmetry_breaking: " + SolverOpt_SymmetryBreaking + spacer);
 	problem.append("flatten: " + SolverOpt_Flatten + spacer);
 	problem.append("bit_width: " + ESJInteger.bitWidth() + spacer);
 	problem.append("univ: u" + AtomCtr + spacer);
 	for (Object k : probRels) {
 	    LogRelation r =  (LogRelation) ProblemRels.get(k);
 	    boolean isModifiableRelation = r.isModifiable(modifiableFields);
 	    boolean isUnknown = r.isUnknown() && isModifiableRelation;
 	    String rBound = (!r.isUnknown() || isModifiableRelation) ? r.log(isUnknown && modifiableObjects != null ? LogMap.get1s(modifiableObjects) : null, resultVarType) : instVarRelOld_log(r).log(null, null);
 	    problem.append("bounds " + k + ": " + rBound + spacer);
 	    if (isUnknown) {
 		unknowns.add(r);
 		funDefs.append(r.funDef_log());
 	    }
 	}
 	problem.append(ESJInteger.intBounds_log() + spacer);
 	problem.append("solve " + funDefs.toString() + spacer + formula.toString() + ";");
 	
 	//ch.append(csq);
 	//ch.flush();
 	if (SolverOpt_debug1) {
 	    System.out.println(problem.toString());
 	}
 	System.out.println("waiting for solver...");
 	String solution = Kodkodi.ESJCallSolver(problem.toString());
 	//if (SolverOpt_debug1)
 	System.out.println("solver done.");
 	SolverOutputParser parser = null;
 	try {
 	    ByteArrayInputStream solutionStream = new ByteArrayInputStream(solution.getBytes("UTF-8"));
 	    ANTLRInputStream stream = new ANTLRInputStream(solutionStream);
 	    SolverOutputLexer lexer = new SolverOutputLexer(stream);
 	    parser = new SolverOutputParser(new CommonTokenStream(lexer));
 	} catch (Exception e) {
             e.printStackTrace();
 	}
 	try {
 	    parser.solutions();
 	    ArrayList models = parser.models();	
 	    ArrayList model = ((ArrayList<ArrayList>) models).get(0);
 	    if (SolverOpt_debug1)
 		System.out.println("Runtime: [ " + model.get(2) + " ]");
 	    boolean satisfiable = (Boolean) model.get(0);
 
 	    if (satisfiable) {
 		if (SolverOpt_debug1)
 		    System.out.println(model);
 		commitModel(obj, unknowns, model);
 		return true;
 	    } else {
 		return false;
 	    }
 
 	    
 	} catch (RecognitionException e) { 
 	    System.out.println("parsing solver result failed!"); 
             e.printStackTrace();
 	}
 
 	return false;
     }
 
     public static boolean solve2(Object obj, Object formula, Class resultVarType, HashMap<String,String> modifiableFields, HashSet<?> modifiableObjects) {
 	Formula funDefs = null;
 	ArrayList unknowns = new ArrayList<LogRelation>();
 	String spacer = "\n";
 	boolean isNonVoid = resultVarType != null;
 	Set probRels = new HashSet(ProblemRels.keySet());
 	if (SolverOpt_debug1) {
 	    System.out.println("problem involves rels: ");
 	    for (Object k : probRels) {
 		LogRelation r =  (LogRelation) ProblemRels.get(k);
 		System.out.println(r.getId() + ": " + r.instVar());
 	    }
 	    if (modifiableFields != null)
 		System.out.println("modifiable fields: " + modifiableFields);
 	    if (modifiableObjects != null)
 		System.out.println("modifiable objs: " + modifiableObjects);
 	}
 	
 	for (Object k : probRels) {
 	    LogRelation r =  (LogRelation) ProblemRels.get(k);
 	    boolean isModifiableRelation = r.isModifiable(modifiableFields);
 	    boolean isUnknown = r.isUnknown() && isModifiableRelation;
 	    if (!r.isUnknown() || isModifiableRelation)
 		r.log2(r.getKodkodRel(), isUnknown && modifiableObjects != null ? LogMap.get1s(modifiableObjects) : null, resultVarType);
 	    else 
 		instVarRelOld_log(r).log2(r.getKodkodRel(), null, null);
 	    if (isUnknown) {
 		unknowns.add(r);
 		funDefs = funDefs == null ? r.funDef_log2() : funDefs.and(r.funDef_log2());
 	    }
 	}
 	Formula finalFormula = funDefs == null ? (Formula) formula : Formula.compose(FormulaOperator.AND, funDefs, (Formula) formula);
 	//ch.append(csq);
 	//ch.flush();
 	if (SolverOpt_debug1) {
 	    System.out.println("int bits: " + ESJInteger.bitWidth());
 	    System.out.println("Bounds: " + ProblemBounds);
 	    System.out.println("Formula: " + finalFormula.toString());
 	}
 
 	Solver solver = new Solver();
 	solver.options().setSolver(SolverOpt_Solver2);
 	solver.options().setBitwidth(ESJInteger.bitWidth());
 	solver.options().setFlatten(false);
 	solver.options().setIntEncoding(Options.IntEncoding.TWOSCOMPLEMENT);
 	solver.options().setSymmetryBreaking(SolverOpt_SymmetryBreaking);
 	solver.options().setSkolemDepth(0);
 	System.out.flush();
 	System.out.println("waiting for solver...");
 
 
 	Solution sol = solver.solve(finalFormula,ProblemBounds);
 	System.out.println("solver done.");
 	if (SolverOpt_debug1)
 	    System.out.println(sol);
 	else 
 	    System.out.println(sol.stats());
 	Instance model = sol.instance();
 	if (model==null)
 	    return false;
 	else {
 	    commitModel2(obj, unknowns, model);
 	    return true;
 	}
     }
 
 
     public static void addAsProblemRel(LogRelation r, String id) { 
 	ProblemRels.put(id,r); 
     }
 
     public static void addAsProblemRel(Object obj, String instVar) { 
 	LogRelation r = instVarRel_log(obj, instVar);
 	ProblemRels.put(r.id(),r);
     }
 
     public static void commitModel(Object obj, ArrayList unknowns, ArrayList model) {
 	HashMap modelRels = (HashMap) model.get(1);
 	for (LogRelation u : (ArrayList<LogRelation>) unknowns) {
 	    ArrayList val = (ArrayList) modelRels.get(u.id());
 	    if (u.isResultVar) {
 		((ESJObject) obj).result(get2((Integer) ((ArrayList) val.get(0)).get(0)));
 		continue;
 	    }
 	    if (obj instanceof ArrayList) {
 		for (ArrayList v : (ArrayList<ArrayList>) val) {
 		    ((ESJList<Integer>) obj).set((Integer) get2((Integer) v.get(0)), (Integer) get2((Integer) v.get(1)));
 		}
 	    } else {
 		if (u.isaListInstVar()) {
 		    Class[] paramTypes = new Class[0];
 		    Object[] args = new Object[0];
 		    Class c = u.domain();
 		    try { 
 			Method m = c.getDeclaredMethod(u.instVar(), paramTypes);
 			Integer lastSeen = null;
 			ArrayList l = null;
 			for (ArrayList v : (ArrayList<ArrayList>) val) {
 			    Integer last = (Integer) v.get(0);
 			    if (l == null || lastSeen != last) {
 				l = (ArrayList) m.invoke(get2(last),args);
 				int vs = ((ArrayList<ArrayList>) val).size();
 				int dec = vs - l.size();
 				while (dec-- > 0) 
 				    l.add(null);
 				lastSeen = last;
 			    }
 			    l.set((Integer) get2((Integer) v.get(1)),get2((Integer) v.get(2)));
 			}
 		    } catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		    }		    
 		} else if (u.isaMapInstVar()) {
 		    Class[] paramTypes = new Class[0];
 		    Object[] args = new Object[0];
 		    Class c = u.domain();
 		    try { 
 			Method m = c.getDeclaredMethod(u.instVar(), paramTypes);
 			Integer lastSeen = null;
 			HashMap map = null;
 			for (ArrayList v : (ArrayList<ArrayList>) val) {
 			    Integer last = (Integer) v.get(0);
 			    if (map == null || lastSeen != last) {
 				map = (HashMap) m.invoke(get2(last),args);
 				lastSeen = last;
 			    }
 			    map.put(get2((Integer) v.get(1)),get2((Integer) v.get(2)));
 			}
 		    } catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		    }		    
 		} else {
 		    Class[] paramTypes = new Class[1];
 		    Object[] args = new Object[1];
 		    boolean isResultVar = u.instVar().equals("result");
 		    paramTypes[0] = isResultVar ? Object.class : u.range();
 		    Class c = u.domain();
 		    try { 
 			Method m = c.getDeclaredMethod(u.instVar(), paramTypes); 
 			for (ArrayList v : (ArrayList<ArrayList>) val) {
 			    args[0] = get2((Integer) v.get(1));
 			    m.invoke(get2((Integer) v.get(0)),args);
 			}
 		    } catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		    }
 		}
 	    }
 	    
 	}
     }
 
     public static void commitModel2(Object obj, ArrayList unknowns, Instance model) {
 	ArrayList val = null;
 	for (LogRelation u : (ArrayList<LogRelation>) unknowns) {
 	    Iterator<Tuple> iter = model.tuples(u.getKodkodRel()).iterator();
 	    if (u.isResultVar) {
 		((ESJObject) obj).result(get2((Integer) iter.next().atom(0)));
 		continue;
 	    }
 	    if (obj instanceof ArrayList) {
 		while (iter.hasNext()) {
 		    Tuple itm = iter.next();
 		    ((ESJList<Integer>) obj).set((Integer) get2((Integer) itm.atom(0)), (Integer) get2((Integer) itm.atom(1)));
 		}
 	    } else {
 		if (u.isaListInstVar()) {
 		    Class[] paramTypes = new Class[0];
 		    Object[] args = new Object[0];
 		    Class c = u.domain();
 		    try { 
 			Method m = c.getDeclaredMethod(u.instVar(), paramTypes);
 			Integer lastSeen = null;
 			ArrayList l = null;
 			while (iter.hasNext()) {
 			    Tuple itm = iter.next();
 			    Integer last = (Integer) itm.atom(0);
 			    if (l == null || lastSeen != last) {
 				l = (ArrayList) m.invoke(get2(last),args);
 				lastSeen = last;
 			    }
 			    Integer idx = (Integer) get2((Integer) itm.atom(1));
 			    while (idx >= l.size())
 				l.add(null);
 			    l.set(idx,get2((Integer) itm.atom(2)));
 			}
 		    } catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		    }		    
 		} else if (u.isaMapInstVar()) {
 		    Class[] paramTypes = new Class[0];
 		    Object[] args = new Object[0];
 		    Class c = u.domain();
 		    try { 
 			Method m = c.getDeclaredMethod(u.instVar(), paramTypes);
 			Integer lastSeen = null;
 			HashMap map = null;
 			while (iter.hasNext()) {
 			    Tuple itm = iter.next();
 			    Integer last = (Integer) itm.atom(0);
 			    if (map == null || lastSeen != last) {
 				map = (HashMap) m.invoke(get2(last),args);
 				lastSeen = last;
 			    }
 			    map.put(get2((Integer) itm.atom(1)),get2((Integer) itm.atom(2)));
 			}
 		    } catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		    }		    
 		} else {
 		    Class[] paramTypes = new Class[1];
 		    Object[] args = new Object[1];
 		    paramTypes[0] = /*isResultVar ? Object.class :*/ u.range();
 		    Class c = u.domain();
 		    try { 
 			Method m = c.getDeclaredMethod(u.instVar(), paramTypes); 
 			while (iter.hasNext()) {
 			    Tuple itm = iter.next();		
 			    args[0] = get2((Integer) itm.atom(1));
 			    m.invoke(get2((Integer) itm.atom(0)),args);
 			}
 		    } catch (Exception e) {
 			    e.printStackTrace();
 			System.exit(1);
 		    }
 		}
 	    }
 	    
 	}
     }
     
     public static String shortClassName(Class c) { 
 	String longName = c.getName();
 	int pi = longName.lastIndexOf(46); // char '.'
 	return pi == -1 ? longName : longName.substring(pi+1);
     }
 }
