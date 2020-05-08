 package polyglot.ext.esj.tologic;
 
 import polyglot.ext.esj.primitives.*;
 import polyglot.ext.esj.solver.Kodkodi.Kodkodi;
 
 import java.util.AbstractCollection;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Collection;
 
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
 
 public class LogMap {
 
     static String SolverOpt_Solver = "\"MiniSat\"";
     static String SolverOpt_Host = "localhost";
     static int SolverOpt_Port = 9128;
     static String SolverOpt_Flatten = "false";
     static int SolverOpt_SymmetryBreaking = 10;
     static int SolverOpt_debugLevel = 0;
     static boolean SolverOpt_debug1 = false, SolverOpt_debug2 = false;
  
     static HashMap JtoLog = new HashMap(); // Java Objs to Solver Atoms
     static HashMap LogtoJ = new HashMap(); 
     static HashMap ProblemRels = new HashMap(); // Holds relations for given problem  
     static HashMap InstVarRels = new HashMap(); // Holds inst var relations for each class
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
 
     // insert mappings for null and enums
     static {
 	LogtoJ.put(AtomCtr,null);
 	JtoLog.put(null,AtomCtr++);
     }
 
     public static int SolverOpt_debugLevel() { return SolverOpt_debugLevel; }
     public static boolean SolverOpt_debug1() { return SolverOpt_debug1; }
     public static boolean SolverOpt_debug2() { return SolverOpt_debug2; }
 
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
 
     public static void initRelationize() {
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
     }
 
     public static void newAtoms(Class c, boolean isEnum) { // FIXME?
 	if (SolverOpt_debug1)
 	    System.out.println("initing class: " + c + " (ctr=" + AtomCtr+")" + "\n" + ClassConstrs);
 	ArrayList classAs = (ArrayList) ClassAtoms.get(c);
 	try {
 	    ArrayList objs;
 	    Object [] args = new Object[2];
 	    args[0] = null;
 	    args[1] = false;
 
 	    objs = isEnum ? (ArrayList) Enums.get(c) : ((ESJObject) ((Constructor) ClassConstrs.get(c)).newInstance(args)).allInstances2();
 	    if (SolverOpt_debug1)
 		System.out.println(objs);
 	    if (SolverOpt_debug1)
 		System.out.println("objs: " + objs);
 
 	    for (Object obj : objs) {		
 		//System.out.println("my old = " + ((ESJObject) obj).old());
 		classAs.add(AtomCtr);
 		LogtoJ.put(AtomCtr,obj);
 		JtoLog.put(obj,AtomCtr);
 		if (!isEnum)
 		    if (((ESJObject) obj).old() != null)
 			JtoLog.put(((ESJObject) obj).old(),AtomCtr);
 		AtomCtr++;
 	    }
 	} catch (Exception e) { System.out.println("oops " + e); System.exit(1); }
     }
 
     public static void put1(Object key, int value) { 
 	JtoLog.put(key,value);
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
 
     public static void put2(int key, Object value) { 
 	LogtoJ.put(key,value);
     }
 
     public static Object get2(int key) { 
 	return LogtoJ.get((Object)key);
     }
 
     // FIXME
     public static LogSet bounds_log(Class c, boolean addNull, boolean isBoundDef) {
 	//System.out.println(c.allInstances_log().string());
 	if (SolverOpt_debug1) {
 	    System.out.println(c);
 	    System.out.println(ClassAtoms);
 	}
 	if (c == int.class || c == Integer.class) 
 	    return ESJInteger.allInstances_log();
 	else {	     
 	    ArrayList atoms = (ArrayList) ClassAtoms.get(c);	    
 	    String addL = "";
 	    String addR = "";
 	    if (isBoundDef) {
 		addL = "{[";
 		addR = "]}";
 	    }
 	    return new LogSet("(" + (addNull ? addL+get1_log(null) + addR + " + " : "") + "u" + atoms.size() + (atoms.size() > 0 ? ("@" + atoms.get(0)) : "") + ")");
 	}
     }
 
     public static String newInstVarRel(Class c, String instVar, Class domain, Class range, boolean isCollection, boolean isaList, boolean isUnknown) {
 	String k = instVar;
 	if (!isUnknown) {
 	    k += "_old";
 	    // mark enum and class
 	    if (range.isEnum() && !ClassAtoms.containsKey(range)) {
 		try {
 		    ClassAtoms.put(range, new ArrayList());
 		    ArrayList es = new ArrayList();
 		    for (Object e : range.getEnumConstants())
 			es.add(e);
 		    Enums.put(range, es);
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
 	    }
 	}
 	LogRelation r = new LogRelation(instVar, domain, range, isCollection, isaList, isUnknown);
 	if (!InstVarRels.containsKey(c))
 	    InstVarRels.put(c, new HashMap());
 	((HashMap) InstVarRels.get(c)).put(k,r);
 	if (SolverOpt_debug1)
 	    System.out.println("InstVarRels:" + InstVarRels);
 	return r.id;
     }
 
     //FIXME
     public static LogRelation instVarRel_log(Object obj, String instVar) {
 	return (LogRelation) ((HashMap) InstVarRels.get(obj.getClass())).get(instVar);
     }
 
     //FIXME
     public static LogRelation instVarRel_log(Class c, String instVar) {
 	return (LogRelation) ((HashMap) InstVarRels.get(c)).get(instVar);
     }
 
     public static LogRelation instVarRelOld_log(LogRelation r) {
 	return (LogRelation) ((HashMap) InstVarRels.get(r.domain())).get(r.instVar()+"_old");
     }
 
 
     public static LogObjAtom objInstVar_log(Object obj, String instVar) {
 	//System.out.println("instVar_log Object " + obj.getClass());
 	return new LogObjAtom("(" + get1_log(obj) + "." + instVarRel_log(obj, instVar).id() + ")");
     }
 
     // FIXME
     public static String objInstVarStr_log(ESJObject obj, String instVar) {
 	/*if (SolverOpt_debug1) {
 	    System.out.println("instVarStr_log -> isVar: " + (obj.var_log() != null));
 	    System.out.println(instVar + " " + obj + " " + ((ESJObject)obj).old());
 	    }*/
 	return "(" + (obj.isQuantifyVar() ? obj.var_log().string() : get1_log(obj)) + "." + instVarRel_log(obj, instVar).id() + ")";
     }
 
     public static LogSet objInstVarSet_log(ESJObject obj, String instVar) {
 	LogRelation r = instVarRel_log(obj, instVar);
 	return new LogSet("(" + (obj.isQuantifyVar() ? obj.var_log().string() : get1_log(obj)) + "." + r.id() + ")", 0, r.isaListInstVar());
     }
 
     public static LogObjAtom null_log() {
 	return new LogObjAtom(get1_log(null));
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
 
 
     public static boolean solve(Object obj, Object formula, HashMap<String,String> modifiableFields, HashSet<?> modifiableObjects) {
 
 	CharArrayWriter problem = new CharArrayWriter();
 	CharArrayWriter funDefs = new CharArrayWriter();
 	ArrayList unknowns = new ArrayList<LogRelation>();
 	String spacer = "\n";
 
 	//getProblemRels(obj);
 	if (SolverOpt_debug1)
 	    System.out.println("problem involves rels: " + ProblemRels);
 	if (SolverOpt_debug1)
 	    System.out.println("well modifiable objs: " + modifiableObjects);
 
 	problem.append("solver: " + SolverOpt_Solver + spacer);
 	problem.append("symmetry_breaking: " + SolverOpt_SymmetryBreaking + spacer);
 	problem.append("flatten: " + SolverOpt_Flatten + spacer);
 	problem.append("bit_width: " + ESJInteger.bitWidth() + spacer);
 	problem.append("univ: u" + AtomCtr + spacer);
 	for (Object k : ProblemRels.keySet() ) {
 	    LogRelation r =  (LogRelation) ProblemRels.get(k);
 	    boolean isModifiableRelation = r.isModifiable(modifiableFields);
 	    boolean isUnknown = r.isUnknown() && isModifiableRelation;
	    String rBound = (!r.isUnknown() || isModifiableRelation) ? r.log(isUnknown && modifiableObjects != null ? LogMap.get1s(modifiableObjects) : null) : instVarRelOld_log(r).log(null);
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
 	if (SolverOpt_debug1)
 	    System.out.println(problem.toString());
 	String solution = Kodkodi.ESJCallSolver(problem.toString());
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
 
 
     public static void addAsProblemRel(LogRelation r, String id) { ProblemRels.put(id,r); }
 
     public static void addAsProblemRel(Object obj, String instVar) { 
 	LogRelation r = instVarRel_log(obj, instVar);
 	ProblemRels.put(r.id(),r);
     }
 
     public static void commitModel(Object obj, ArrayList unknowns, ArrayList model) {
 	HashMap modelRels = (HashMap) model.get(1);
 	for (LogRelation u : (ArrayList<LogRelation>) unknowns) {
 	    ArrayList val = (ArrayList) modelRels.get(u.id());
 	    //System.out.println(val);
 	    if (obj instanceof ArrayList) {
 		for (ArrayList v : (ArrayList<ArrayList>) val) {
 		    ((ESJList<Integer>) obj).set((Integer) get2((Integer) v.get(0)), (Integer) get2((Integer) v.get(1)));
 		}
 	    } else {
 		
 		Class[] paramTypes = new Class[1];
 		Object[] args = new Object[1];
 		paramTypes[0] = u.range();
 		Class c = u.domain();
 		//System.out.println("relation " + u.instVar() + " of type: " + paramTypes[0] + " for class: " + u.domain());
 		//System.out.println("lookup mtd: " + u.instVar() + " " + paramTypes);
 		try { 
 		    Method m = c.getDeclaredMethod(u.instVar(), paramTypes); 
 		    //System.out.println(m);
 		    for (ArrayList v : (ArrayList<ArrayList>) val) {
 			//System.out.println(get2((Integer) v.get(0)));
 			//System.out.println(get2((Integer) v.get(1)));
 			args[0] = get2((Integer) v.get(1));
 			m.invoke(get2((Integer) v.get(0)),args);
 		    }
 		} catch (Exception e) {
 		    System.out.println("duh: " + e);
 		    System.exit(1);
 		}
 		
 	    }
 	    
 	}
     }
     
 
 }
