 /**
  * Created on 31.03.2011
  */
 package edu.kit.asa.alloy2key;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Vector;
 
 import edu.kit.asa.alloy2key.key.KeYFile;
 import edu.kit.asa.alloy2key.key.ModelException;
 import edu.kit.asa.alloy2key.key.Taclet;
 import edu.kit.asa.alloy2key.key.Term;
 import edu.kit.asa.alloy2key.key.TermCall;
 import edu.kit.asa.alloy2key.key.TermVar;
 import edu.kit.asa.alloy2key.modules.KeYModule;
 import edu.kit.asa.alloy2key.util.Util;
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4.ErrorFatal;
 import edu.mit.csail.sdg.alloy4.Pair;
 import edu.mit.csail.sdg.alloy4.Pos;
 import edu.mit.csail.sdg.alloy4.ast.Command;
 import edu.mit.csail.sdg.alloy4.ast.Decl;
 import edu.mit.csail.sdg.alloy4.ast.Expr;
 import edu.mit.csail.sdg.alloy4.ast.ExprBinary;
 import edu.mit.csail.sdg.alloy4.ast.ExprCall;
 import edu.mit.csail.sdg.alloy4.ast.ExprConstant;
 import edu.mit.csail.sdg.alloy4.ast.ExprHasName;
 import edu.mit.csail.sdg.alloy4.ast.ExprITE;
 import edu.mit.csail.sdg.alloy4.ast.ExprLet;
 import edu.mit.csail.sdg.alloy4.ast.ExprList;
 import edu.mit.csail.sdg.alloy4.ast.ExprQt;
 import edu.mit.csail.sdg.alloy4.ast.ExprQt.Op;
 import edu.mit.csail.sdg.alloy4.ast.ExprUnary;
 import edu.mit.csail.sdg.alloy4.ast.ExprVar;
 import edu.mit.csail.sdg.alloy4.ast.Func;
 import edu.mit.csail.sdg.alloy4.ast.Module;
 import edu.mit.csail.sdg.alloy4.ast.Sig;
 import edu.mit.csail.sdg.alloy4.ast.Sig.Field;
 import edu.mit.csail.sdg.alloy4.ast.Sig.PrimSig;
 import edu.mit.csail.sdg.alloy4.ast.Sig.SubsetSig;
 import edu.mit.csail.sdg.alloy4.parser.ParsedModule;
 import edu.mit.csail.sdg.alloy4.parser.ParsedModule.Open;
 
 /**
  * The purpose of this class is to translate an Alloy model, given
  * as instance of <code>ParsedModule</code>, to KeY FOL.
  * 
  * Features not (yet) supported:
  * - "disjoint" on right-hand side of a declaration
  * - integer shift operations
  * - sequences
  * - max/min values of integers (since unbounded...)
  * - sum quantifier
  * - binding more than one quantification variable for one/lone
  * 
  * @author Ulrich Geilmann
  * 
  *
  */
 public class Translator implements Identifiers {
 	
 	/** the model to translate */
 	private ParsedModule mod;
 	
 	/** all reachable, non built-in modules */
 	private LinkedList<Module> reachableModules;
 	
 	/** all referenced, built-in modules */
 	private LinkedList<KeYModule> builtinModules;
 	
 	/** all reachable signatures, not being declared in a built-in module */
 	private Collection<Sig> reachableSigs;
 	
 	/** the signatures being finitized due to application of the cardinality operator */
 	private Collection<Sig> finitizedSigs;
 	
 	/** the result */
 	private static KeYFile target;
 	
 	public Translator (ParsedModule m) {
 		// the idMap saves the unique ids for all alloy entities
 		this.idMap = new HashMap<Object,String>();		
 		this.tmpIds = new Stack<ExprHasName>();
 		// alloy model. we get this from the alloy parser
 		this.mod = m;
 		// the target model (a keyfile or maybe a smtfile)
 		Translator.target = new KeYFile();
 		// all referenced external modules
 		this.external = new HashSet<Sig>();
 		// all modules that can be reached by our alloy model and are not built-in
 		this.reachableModules = new LinkedList<Module>();
 		// referenced modules
 		this.builtinModules = new LinkedList<KeYModule>();
 		// signatures to be assumed finite
 		this.finitizedSigs = new HashSet<Sig>();
 		// begin by adding the reachable, non built-in modules
 		reachableModules.add (mod);
 	}
 	
 	/**
 	 * perform the translation
 	 * @throws ModelException 
 	 * @ 
 	 */
 	public KeYFile translate() throws ModelException  {
 		// we need to uniquely identify all entities, so 
 		// gather all entities and make them unique if need be 
 		try {
 			createGlobalIds();
 			// translation of signature declarations ("types")
 			translateSigDecls();
 			// translating fact declarations
 			translateFacts();
 			// translate predicates (and functions)
 			translateFuncs();
 			// Translate checks 
 			translateCmds();
 			// assume signatures to be finite
 			finitize();
 			// all signatures get their own corresponding relation
 			generateSigDecls();
 		} catch (Err e) {
 			// notify user
 			System.err.println(e.dump());
 			return null;
 		}
 		// remember built-in modules for translation in modules list
 		target.modules.addAll(builtinModules);
 		// hand our translation to the caller (success)
 		return target;
 	}
 	
 	/**
 	 * explicitely finite a signature
 	 * @param s
 	 * the name of the sig (as used in the Alloy model).
 	 * Might be prefixed with a model path, e.g. this/S
 	 * @return
 	 * false iff no signature of name <code>s</code> could be
 	 * found
 	 */
 	public boolean finitize (String s) {
 		// check all available sigs
 		for (Sig sig : mod.getAllReachableSigs()) {
 			// find the matching signature (sig may contain prefix)
 			if (sig.label.equals(s) ||
 					Util.removePrefix(sig.label).equals(s)) {
 				// finitize the given sig
 				finitizedSigs.add (sig);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/** signatures that are used as instantiation for
 	 	module imports are being declared within the
 		translation of the first of these modules **/
 	private HashSet<Sig> external;
 	
 	/*
 	 * Handling identifiers
 	 * --------------------
 	 * Every named Alloy entity (i.e. instances of Sig,
 	 * Func and ExprHasName) need unique identifiers for
 	 * their KeY counterparts.
 	 * A map keeps track of these relations. For variable
 	 * names, such mappings are only declared temporarily.
 	 */
 	
 	// TODO make more complete, include all functions declared by the theory!
 	/** reserved identifiers in KeY  **/
 	private static final Set<String> reserved = new HashSet<String>() {{
 		this.add ("add");this.add ("Relation");this.add ("Rel1");this.add ("Rel2");this.add ("Rel3");
 		this.add ("Rel4");this.add ("Tuple");this.add ("Atom");this.add ("Tuple2");this.add ("Tuple3");
 		this.add ("Tuple4");this.add ("in");this.add ("subrel");this.add ("disj");this.add ("lone");
 		this.add ("some");this.add ("one");this.add ("binary");this.add ("ternary");this.add ("join1x2");
 		this.add ("join1x3");this.add ("join2x2");this.add ("join3x1");this.add ("join2x1");this.add ("sin");
 		this.add ("sin2");this.add ("sin3");this.add ("prod1x1");this.add ("prod2x1");this.add ("prod1x2");
 		this.add ("union1");this.add ("union2");this.add ("union3");this.add ("diff1");this.add ("diff2");
 		this.add ("diff3");this.add ("inter1");this.add ("inter2");this.add ("inter3");this.add ("overr1");
 		this.add ("overr2");this.add ("overr3");this.add ("transp");this.add ("domRestr1");this.add ("domRestr2");
 		this.add ("domRestr3");this.add ("rangeRestr1");this.add ("rangeRestr2");this.add ("rangeRestr3");
 		this.add ("none");this.add ("none2");this.add ("none3");this.add ("univ");this.add ("iden");
 		this.add ("reflTransClos");this.add ("iterJoin");this.add ("transClos");
 		this.add ("compr1"); this.add ("compr2"); this.add ("compr3"); this.add ("bind");
 		this.add ("sum"); this.add ("i2a"); this.add ("Int"); this.add ("card"); this.add ("finite");
 		this.add ("a2i"); this.add ("elem1"); this.add("elem2"); this.add("elem3"); this.add ("nextInt");
 	}};
 	
 	/** map an Alloy entity to a KeY identifier **/
 	private HashMap<Object,String> idMap;
 	
 	/** stack of temporary identifiers **/
 	private Stack<ExprHasName> tmpIds;
 	
 	/**
 	 * @return
 	 * the KeY identifier associated with a given
 	 * Alloy entity
 	 */
 	public String id(Object o) {
 		return idMap.get(o);
 	}
 	
 	/** {@inheritDoc} */
 	public boolean setId(Object o, String id) {
 		if (idMap.get(o) != null)
 			return false;
 		if (idMap.containsValue(id))
 			return false;
 		idMap.put(o, id);
 		return true;
 	}
 	
 	/** {@inheritDoc} */
 	public String newId(String base) {
 		return newIdFor(base);
 	}
 
 	/**
 	 * Propose an unused KeY identifier for an Alloy
 	 * entity
 	 * @param o
 	 * the Alloy entity for which an id is needed
 	 * @return
 	 * a KeY id, suitable to be used for <code>o</code>
 	 */
 	private String newIdFor (Object o) {
 		if (idMap.containsKey(o))
 			return idMap.get(o);
 		/* the following classes of Alloy's AST are named
 		 * entities, for which KeY ids are needed:
 		 * Sig, Func, ExprHasName */
 		String id;
 		if (o instanceof Sig)
 			id = Util.removePrefix(((Sig)o).label);
 		else if (o instanceof Func)
 			id = Util.removePrefix(((Func)o).label);
 		else if (o instanceof ExprHasName)
 			id = ((ExprHasName)o).label;
 		else if (o instanceof String)
 			id = (String)o;
 		else throw new RuntimeException ("Unexpected!");
 		
 		// make it a KeY conform identifier
 		// TODO quotes are probably not the only character allowed in Alloy identifiers but prohibited in KeY...
 		id = id.replace('\'', '_');
 		id = id.replace("\"", "__");
 		
 		// make it unique according to idMap and reserved
 		int i = 0;
 		String kid = id;
 		while (idMap.containsValue(kid))
 			kid = id+(++i);
 		while (reserved.contains(kid))
 			kid = id+(++i);
 
 		return kid;
 	}
 	
 	/**
 	 * create a new KeY variable name
 	 * @param n
 	 * base for the name to create
 	 * @return
 	 * a new (according to <code>idMap</code>) variable
 	 * identifier, that does not occur in <code>t</code>.
 	 */
 	private String newVar (String n, Term... t) {
 		String id = newId(n);
 		while (occurs(id,t))
 			id = newId(id+"_");
 		return id;
 	}
 	private boolean occurs(String id, Term... t) {
 		for (int i = 0; i < t.length; ++i)
 			if (t[i] != null && t[i].occurs(id))
 				return true;
 		return false;
 	}
 	
 	/**
 	 * create a temporary identifier for an Alloy entity.
 	 * the id can be referred to using the <code>id</code>
 	 * method, until it is removed by one of the <code>popId</code>
 	 * methods. 
 	 */
 	private void pushId (ExprHasName e) {
 		String id = newIdFor(e);
 		idMap.put(e, id);
 		tmpIds.push(e);
 	}
 	
 	/**
 	 * remove last added temporary identifier
 	 **/
 	private void popId () {
 		ExprHasName e = tmpIds.pop();
 		idMap.remove(e);
 	}
 	
 	/**
 	 * remove the last <code>n</code> temporary identifiers
 	 */
 	private void popId (int n) {
 		for (int i = 0; i < n; i++) {
 			popId();
 		}
 	}
 	
 	/*
 	 * Translating the model
 	 * ---------------------
 	 * I)
 	 * the KeY identifiers for all global Alloy entities
 	 * (i.e. functions, predicate, Signatures and fields)
 	 * that were defined in this model and the included
 	 * modules are created.
 	 * II)
 	 * the signatures are declared as constant function
 	 * symbols, and the model's type hierarchy gets
 	 * axiomatized
 	 * III)
 	 * signatures multiplicities are axiomatized
 	 * IV)
 	 * the fields are declared as constant function symbols
 	 * of appropriate arity, their typing and multiplicity
 	 * constraints are generated.
 	 * V)
 	 * the signature facts are translated.
 	 * VI)
 	 * all other facts are translated
 	 * VII)
 	 * rules are added for functions and predicate in all
 	 * reachable modules
 	 * VIII)
 	 * check commands are translated
 	 */
 	
 	/**
 	 * recursively find all imported modules
 	 */
 	private void findImportedModules (ParsedModule m, HashSet<ParsedModule> seen) {
 		if (seen == null)
 			seen = new HashSet<ParsedModule>();
 		for (Open o : m.getOpens()) {
 			if (seen.contains(o.getRealModule()))
 				continue;
 			seen.add(o.getRealModule());
 			Vector<Sig> insts = new Vector<Sig>(o.args.size());
 			for (String a : o.args)
 				insts.add(getSig(a));
 			KeYModule module = KeYModule.createFor(o.getRealModule(), insts, this);
 			if (module == null) {
 				// non built-in module
 				reachableModules.add(o.getRealModule());
 				findImportedModules (o.getRealModule(), seen);
 			} else {
 				// built-in module
 				builtinModules.add (module);
 				Set<Object> def = module.defined();
 				for (Object ob : def) {
 					if (ob instanceof Sig)
 						external.add((Sig)ob);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * create identifiers for all global Alloy entities
 	 */
 	private void createGlobalIds () {
 		// identifiers for built-in signatures
 		idMap.put(Sig.NONE, "none");
 		idMap.put(Sig.UNIV, "univ");
 		idMap.put(Sig.SEQIDX, "seqidx");
 		idMap.put(Sig.SIGINT, "Int");
 		idMap.put(Sig.STRING, "string");
 		
 		findImportedModules(mod,null);
 		
 		reachableSigs = new HashSet<Sig>();
 		for (Module m : reachableModules) {
 			for (Sig s : m.getAllSigs())
 				reachableSigs.add(s);
 		}
 		reachableSigs.addAll(external);
 		
 		// identifier for sigs and fields
 		for (Sig s : reachableSigs) {
 			if (!external.contains(s))
 				idMap.put(s, newIdFor(s));
 			for (Field f : s.getFields()) {
 				idMap.put(f, newIdFor(f));
 			}
 		}
 		
 		// identifier for predicates and functions
 		for (Module m : reachableModules) {
 			for (Func func : m.getAllFunc()) {
 				// skip private declarations in imported modules
 				/*if (func.isPrivate != null && m != mod)
 					continue;*/
 				idMap.put(func, newIdFor(func));
 			}
 		}
 	}
 	
 	/**
 	 * finitize the sigs
 	 */
 	private void finitize() {
 		for (Sig s : finitizedSigs) {
 			target.addAssumption(Term.call("finite", term(s)));
 		}
 	}
 	
 	/**
 	 * declare all signature symbols
 	 * @ 
 	 */
 	private void generateSigDecls()  {
 		for (Sig s : reachableSigs) {
 			if (s.builtin)
 				continue;
 			// declare function symbol for sig
 			if (!external.contains(s))
 				// each signature gets its own function symbol of type Rel1
 				target.addFunction ("Rel1", id(s));
 		}
 	}
 	
 	/**
 	 * translate the model's type hierarchy
 	 * @throws ModelException 
 	 * @ 
 	 */
 	private void translateSigDecls() throws Err, ModelException {
 		
 		for (Sig s : reachableSigs) {
 			// ignore built-in sigs
 			if (s.builtin)
 				continue;
 			
 			// we have a PRIMARY SIGNATURE
 			if (s.isSubsig != null) {
 				if (!(s instanceof PrimSig)) throw new RuntimeException ("Unexpected!");
 				PrimSig ps = (PrimSig)s;
 				
 				// sig is abstract
 				if (ps.isAbstract != null) {
 					Term disjunction = Term.FALSE;
 					// membership theorem for subsigs. (FIXME isn't this already covered by union?)
 					for (PrimSig sub : ps.children()){
 						disjunction = disjunction.or(in("this", sub));
 					}
 					target.declareAtom();
 					target.addAssertion(
 							in("this", ps).implies(disjunction).forall("Atom", "this"));
 				}
 				
 				// all subsignatures are disjoint
 				for (int i = 0; i < ps.children().size(); ++i)
 					for (int j = i+1; j < ps.children().size(); ++j){
 						target.declareDisjoint(1);
 						target.addAssertion(call("disjoint_1",ps.children().get(i),
 														 ps.children().get(j)));
 						// TODO: I'd rather have the "intersection(i, j) == emptyset" formula here
 					}
 				// sig extends parent
 				if (ps.parent != null && ps.parent != Sig.UNIV) {
 					target.declareSubset(1);
 					target.addAssertion (call("subset_1", ps, ps.parent));
 				}
 			
 			// we have a SUB SIGNATURE // TODO smt-fy
 			} else {
 				if (s.isSubset == null || !(s instanceof SubsetSig)) throw new ModelException("The signature "+s+" was expected to be a subset, but isn't!");
 				SubsetSig ss = (SubsetSig)s;
 				if (ss.parents.size() < 1) throw new ModelException ("The signature was expected to have a single parent, but had more!");
 				Term union = term(ss.parents.get(0));
 				target.declareUnion(1);
 				for (int i = 1; i < ss.parents.size(); i++) {
 					union = Term.call("union_1", term(ss.parents.get(i)), union);
 				}
 				target.declareSubset(2);
 				target.addAssertion(Term.call("subset_2",    // subrel = subset, only for higher-arity relations
 						term(ss), union));    // can be expressed with 2 subset_1 if you join them left and right
 			}
 			
 			// sig's multiplicity is one // TODO smt-fy
 			if (s.isOne != null)
 			{
 				target.declareOne(1);
 				target.addAssertion(Term.call("one_1", term(s)));
 			}
 			
 			// sig's multiplicity is lone // TODO smt-fy
 			if (s.isLone != null)
 			{
 				target.declareLone(1);
 				target.addAssertion(Term.call("lone_1", term(s)));
 			}
 			// sig's multiplicity is some // TODO smt-fy
 			if (s.isSome != null)
 			{
 				target.declareSome(1);
 				target.addAssertion(Term.call("some_1", term(s)));
 			}
 			
 			// process the sig's FIELDS
 			for (Decl decl : s.getFieldDecls()) {
 				// skip private fields from submodules
 				if (!inRootMod(decl.isPrivate))
 					continue;
 				
 				final int arity = arity(decl.expr)+1;
 				
 				for (ExprHasName f : decl.names) {
 					// declare field as constant function symbol; respect arity
 					target.addFunction(String.format("Rel%d",arity), id(f));
 					
 					//explicit typing of first component (and also include bounding type) // TODO smt-fy
 					Term t = translateExpr(s.type().product(decl.expr.type()).toExpr());
 //					System.out.println(";; "+f.label+": "+s.type().product(decl.expr.type()).toExpr());
 //					System.out.println(";; "+arity);
 					target.declareSubset(arity);
 					target.addAssertion (Term.call ("subset_" + arity, term(f), t));					
 				}
 				
 				//generate typing and multiplicity constraints
 				Term tm = translateDecl(decl,new ThisJoin(arity),false);
 				tm = in("this", s).implies(tm);
 				target.addAssertion(tm.forall("Atom", "this"));
 			}
 			
 			// translate signature facts
 			for (Expr c : s.getFacts()) {
 				Term fact = translateExpr(c);
 				fact = in("this", s).implies(fact);
 				target.addAssertion(fact.forall("Atom","this"));
 			}
 		}
 		
 		// disjointness of top-level signatures
 		List<Sig> sigs = new LinkedList<Sig>(reachableSigs);
 		for (int i = 0; i < sigs.size(); ++i) {
 			if (sigs.get(i).isTopLevel()) {
 				for (int j = i+1; j < sigs.size(); ++j) {
 					if (sigs.get(j).isTopLevel())
 						target.addAssertion(call("disjoint_1", sigs.get(i), sigs.get(j)));
 				}
 				target.addRule(Taclet.disjointTaclet(id(Sig.SIGINT), id(sigs.get(i))));
 			}
 		}
 	}
 	
 	
 	/**
 	 * translate all facts that are reachable from this
 	 * module.
 	 * @throws ModelException 
 	 * @ 
 	 */
 	private void translateFacts() throws Err, ModelException {
 		Term facts = Term.TRUE;
 		for (Module m : reachableModules)
 			for (Pair<String,Expr> f : m.getAllFacts())
 				facts = facts.and(translateExpr(f.b));
 		target.addAssertion(facts);
 	}
 	
 	/**
 	 * translate all function and predicate declarations
 	 * in this, and all submodules
 	 * @throws ModelException 
 	 * @ 
 	 */
 	private void translateFuncs() throws Err, ModelException {
 		// identifier for predicates and functions
 		for (Module m : reachableModules) {
 			for (Func func : m.getAllFunc()) {
 				// skip private declarations in imported modules
 				if (func.isPrivate != null && m != mod)
 					continue;
 				translateFunc(func);
 			}
 		}
 	}
 	
 	/**
 	 * translate a single function/predicate definition
 	 * @ 
 	 */
 	private void translateFunc(Func f) throws Err, ModelException {
 		Taclet tac = new Taclet(id(f)+"_def");
 		Term[] params = new Term[f.params().size()];
 		StringBuffer paramDecl = new StringBuffer();
 		for (int i = 0; i < f.params().size(); i++) {
 			ExprVar v = f.params().get(i);
 			pushId(v);
 			tac.addSchemaVar("Rel"+arity(v), id(v));
 			params[i] = term(v);
 			if (paramDecl.length() > 0)
 				paramDecl.append(",");
 			paramDecl.append("Rel"+arity(v));
 		}
 		if (f.isPred) {
 			target.addPredicate(String.format("%s%s;",id(f),
 					(f.count() == 0) ? "" : ("("+paramDecl+")")));
 		} else {
 			target.addFunction(String.format("Rel%d %s%s;",arity(f.returnDecl),id(f),
 					(f.count() == 0) ? "" : ("("+paramDecl+")")));
 		}
 		tac.setFind(Term.call(id(f), params));
 		tac.setReplacewith(translateExpr(f.getBody()));
 		popId(params.length);
 		target.addRule (tac);
 	}
 	
 	/**
 	 * translate all check commands
 	 * @ 
 	 */
 	private void translateCmds() throws Err, ModelException {
 		for (Command cmd : mod.getAllCommands()) {
 			if (!cmd.check) continue;
 			target.addAssertion(translateExpr(cmd.formula.not()));
 		}
 	}
 	
 	/**
 	 * translate a declaration
 	 * @param d
 	 * the declaration to translate
 	 * @param alt
 	 * alter the relation being declared before used
 	 * within the generated constraints. may be null.
 	 * @param letBindings
 	 * variables declared by a let and their binding
 	 * @param atomVars
 	 * variables that have to be surrounded by the
 	 * singleton constructor when used within an
 	 * expression.
 	 * @param boundingType
 	 * when not true, the bound constraint is only included
 	 * if it differs from the type bounding expression. 
 	 * @return
 	 * a conjunction of the typing and multiplicity
 	 * constraints for all relations being declared.
 	 * @ 
 	 */
 	private Term translateDecl(Decl d, TermAlternation alt,
 			HashMap<ExprHasName,Term> letBindings, HashSet<ExprHasName> atomVars, boolean boundingType) throws Err, ModelException {
 		if (alt == null)
 			alt = new TermAlternation() { public Term alter(Term t) {
 				return t;
 			}};
 			
 		// typing constraints
 		Term bound = translateExpr_p(d.expr, letBindings, atomVars);
 		Term f = Term.TRUE;
 		int arity;
 		for (ExprHasName n : d.names) {
 			if (atomVars.contains(n)) {
 				// include bounding type information
 				Expr bound2 = d.expr.type().toExpr();				
 				if (boundingType)
 				{
 					arity = arity(bound2);
 					target.declareIn(arity);
 					f = f.and(Term.call("in_" + arity, Term.var(id(n)), translateExpr_p(bound2, letBindings, atomVars)));
 				}
 				if (!bound2.isSame(deMult(d.expr)))
 				{
 					arity = arity(d.expr);
 					target.declareIn(arity);
 					f = f.and(Term.call("in_" + arity, Term.var(id(n)), bound));
 				}
 			} else {
 				Expr bound2 = d.expr.type().toExpr();
 				arity = arity(bound2);
 				target.declareSubset(arity);
 				if (boundingType)
 					f = f.and(Term.call("subset_" + arity, alt.alter(term(n, atomVars)), translateExpr_p(bound2, letBindings, atomVars)));
 				if (!bound2.isSame(deMult(d.expr))) {
 					f = f.and(Term.call("subset_" + arity, alt.alter(term(n, atomVars)), bound));
 				}
 			}
 		}
 		
 		// multiplicity constraint
 		for (ExprHasName n : d.names) {
 			Term t = alt.alter(term(n, atomVars));
 			Term mult = generateMultConstr(t, d.expr, letBindings, atomVars, true);
 			if (!atomVars.contains(n))
 				f = mult.and(f);
 		}
 		
 		// might be a "disjoint" declaration
 		if (d.disjoint != null) {
 			for (int i = 0; i < d.names.size(); ++i)
 				for (int j = i+1; j < d.names.size(); ++j) {
 					Term t1 = alt.alter(term(d.names.get(i), atomVars));
 					Term t2 = alt.alter(term(d.names.get(j), atomVars));					
 					arity = d.expr.type().arity();
 					target.declareDisjoint(arity);
 					f = f.and(Term.call("disjoint_" + arity,t1,t2));
 				}
 		}
 		
 		return f;
 	}
 	
 	/**
 	 * translate a declaration
 	 * @param d
 	 * the declaration to translate
 	 * @param alt
 	 * alter the relation being declared before used
 	 * within the generated constraints. may be null.
 	 * @return
 	 * a conjunction of the typing and multiplicity
 	 * constraints for all relations being declared.
 	 * @ 
 	 */
 	private Term translateDecl(Decl d, TermAlternation alt, boolean boundingType) throws Err, ModelException {
 		return translateDecl (d, alt, new HashMap<ExprHasName,Term>(), new HashSet<ExprHasName>(), boundingType);
 	}
 	
 	/**
 	 * translate an Alloy expression to a KeY TermBase
 	 * @param e
 	 * the expression to translate
 	 * @return
 	 * the translation of <code>e</code> as KeY TermBase
 	 * @ 
 	 */
 	private Term translateExpr(Expr e) throws Err, ModelException {
 		// translate assuming no outer let bindings and no known atoms
 		return translateExpr_p(e,new HashMap<ExprHasName,Term>(),new HashSet<ExprHasName>());
 	}
 	
 	/**
 	 * helper for translating an expression
 	 * @param letBindings
 	 * mapping Alloy variables introduced by a let to
 	 * their (translated) binding
 	 * @param atomVars
 	 * set of variables that are translated to Atoms.
 	 * their occurrences within an expression has to be
 	 * surrounded by the singleton constructor.
 	 * @return
 	 * translation of <code>e</code>
 	 * @ 
 	 */
 	private Term translateExpr_p(Expr e, HashMap<ExprHasName,Term> letBindings,
 									 HashSet<ExprHasName> atomVars) throws Err, ModelException {
 		// are you a signature?
 		if (e instanceof Sig)                                        // signature
 			// create a signature term (which will be )
 			return term ((Sig)e);
 		// are you a named entity?
 		if (e instanceof ExprHasName) {                              // identifier
 			if (letBindings.containsKey(e))
 				return letBindings.get(e);
 			return term((ExprHasName)e, atomVars);
 		}
 		if (e instanceof ExprUnary) {
 			ExprUnary ue = (ExprUnary)e;
 			if (ue.sub == null) throw new ErrorFatal("");
 			Term e_ = translateExpr_p(ue.sub, letBindings, atomVars);
 			int arity = ue.sub.type().arity();  // TODO: find out why arity was not used here before
 			switch (ue.op) {
 			case TRANSPOSE:                                          // ~e
 				target.declareTranspose();  // arity is always 2
 				return Term.call("transp", e_);
 			case CLOSURE:                                            // ^e
 				target.declareTransitiveClosure();  // arity is always 2
 				return Term.call("transClos", e_);
 			case RCLOSURE:                                           // *e
 				target.declareReflexiveTransitiveClosure();  // arity is always 2
 				return Term.call("reflTransClos", e_);
 			case NOT:                                                // !c
 				return e_.not();
 			case NO:                                                 // no e
 				target.declareSome(arity);
 				return Term.call("some_" + arity, e_).not();
 			case SOME:                                               // some e
 				target.declareSome(arity);
 				return Term.call("some_" + arity, e_);
 			case LONE:                                               // lone e
 				target.declareLone(arity);
 				return Term.call("lone_" + arity, e_);
 			case ONE:                                                // one e
 				target.declareOne(arity);
 				return Term.call("one_" + arity, e_);
 			case CARDINALITY:                                        // # e
 				target.declareCardinality(arity);
 				return Term.call ("card_" + arity, e_);
 			case CAST2INT:
 				return Term.call("sum", e_); // TODO: verify built-in
 			case CAST2SIGINT:
 				return Term.call("I", e_); // TODO: verify built-in
 			case ONEOF:
 			case SOMEOF:
 			case LONEOF:
 			case SETOF:
 			case NOOP:
 				return e_;
 			// TODO exactly of...
 			default:
 				throw new RuntimeException ("Unsupported unary operator in '"+ue+"' at "+ue.pos);
 			}
 		}
 		if (e instanceof ExprBinary) {
 			ExprBinary be = (ExprBinary)e;
 			Term e1 = translateExpr_p (be.left, letBindings, atomVars);
 			Term e2 = translateExpr_p (be.right, letBindings, atomVars);
 			int ar1 = arity(be.left);
 			int ar2 = arity(be.right);
 			switch (be.op) {
 			case JOIN:                                               // e1.e2
 				target.declareJoin(ar1, ar2);
 				return Term.call("join_"+ar1+"x"+ar2, e1, e2);
 			case DOMAIN:                                             // e1 <: e2
 				target.declareDomainRestriction(ar2);
 				return Term.call("domRestr_"+ar2, e1, e2);
 			case RANGE:                                              // e1 :> e2
 				target.declareRangeRestriction(ar1);
 				return Term.call("rangeRestr_"+ar1, e1, e2);
 			case INTERSECT:                                          // e1 & e2
 				target.declareIntersection(ar1);
 				return Term.call("inter_"+ar1, e1, e2);
 			case PLUSPLUS:                                           // e1 ++ e2
 				target.declareOverride(ar1);
 				return Term.call("overr_"+ar1, e1, e2);
 			case PLUS:                                               // e1 + e2
 				if (e1.isInt() && e2.isInt())
 					return e1.plus(e2);
 				target.declareUnion(ar1);
 				return Term.call("union_"+ar1, e1, e2);
 			case MINUS:                                              // e1 - e2
 				if (e1.isInt() && e2.isInt())
 					return e1.minus(e2);
 				target.declareDifference(ar1);
 				return Term.call("diff_"+ar1, e1, e2);
 			case ARROW:                                              // e1 -> e2
 			case ANY_ARROW_SOME:
 			case ANY_ARROW_ONE:
 			case ANY_ARROW_LONE:
 			case SOME_ARROW_ANY:
 			case SOME_ARROW_SOME:
 			case SOME_ARROW_ONE:
 			case SOME_ARROW_LONE:
 			case ONE_ARROW_ANY:
 			case ONE_ARROW_SOME:
 			case ONE_ARROW_ONE:
 			case ONE_ARROW_LONE:
 			case LONE_ARROW_ANY:
 			case LONE_ARROW_SOME:
 			case LONE_ARROW_ONE:
 			case LONE_ARROW_LONE:
 				target.declareProduct(ar1, ar2);
 				return Term.call("prod_"+ar1+"x"+ar2, e1, e2);
 			case EQUALS:                                             // e1 = e2
 				return e1.equal(e2);
 			case NOT_EQUALS:                                         // e1 != e2
 				return e1.equal(e2).not();
 			case IMPLIES:                                            // c1 => c2
 				return e1.implies(e2);
 			case IN:                                                 // e1 in e2
 				target.declareSubset(ar1);
 				return Term.call("subset_" + ar1, e1, e2).and (
 						generateMultConstr(e1, be.right, letBindings, atomVars, false));
 			case NOT_IN:                                             // e1 !in e2
 				target.declareSubset(ar1);
 				return Term.call("subset_" + ar1, e1, e2).not();
 			case AND:                                                // c1 && c2
 				return e1.and(e2);
 			case OR:                                                 // c1 || c2
 				return e1.or(e2);
 			case IFF:                                                // c1 <=> c2
 				return e1.iff(e2);
 			case LT:                                                 // i < j
 				return e1.lt(e2);
 			case LTE:                                                // i <= j
 				return e1.lte(e2);
 			case GT:                                                 // i > j
 				return e1.gt(e2);
 			case GTE:                                                // i >= j
 				return e1.gte(e2);
 			case NOT_LT:                                             // i !< j
 				return e1.lt(e2).not();
 			case NOT_LTE:                                            // i !<= j
 				return e1.lte(e2).not();
 			case NOT_GT:                                             // i !> j
 				return e1.gt(e2).not();
 			case NOT_GTE:                                            // i !>= j
 				return e1.gte(e2).not();
 			case MUL:
 				return e1.mul(e2);
 			case DIV:
 				return e1.div(e2);
 			case REM:
 				return e1.rem(e2);
 			case SHL:                                                // i << j
 			case SHR:                                                // i >>> j
 			case SHA:                                                // i >> j
 				throw new ErrorFatal ("Integer shifts not yet supported!");//TODO
 			case ISSEQ_ARROW_LONE:
 				throw new ErrorFatal ("Sequences not yet supported!");//TODO
 //			default:
 //				throw new RuntimeException ("Unexpected operator: "+be.op+" in expression "+e);
 			}
 		}
 		if (e instanceof ExprCall) {
 			ExprCall ec = (ExprCall)e;
 			if (ec.args.isEmpty())                                   // f
 				return Term.call(id(ec.fun));
 			Term[] params = new Term[ec.args.size()];        // f [e1,...]
 			for (int i = 0; i < ec.args.size(); i++) {
 				params[i] = translateExpr_p(ec.args.get(i), letBindings, atomVars);
 			}
 			return Term.call(id(ec.fun), params);
 		}
 		if (e instanceof ExprITE) {                                  // c1 => e1 else e2
 			ExprITE ite = (ExprITE)e;
 			return translateExpr_p(ite.cond, letBindings, atomVars).ite(
 						translateExpr_p(ite.left, letBindings, atomVars),
 						translateExpr_p(ite.right, letBindings, atomVars));
 		}
 		if (e instanceof ExprList) {                                 // { c1 c2 }
 			ExprList el = (ExprList)e;
 			Term result = (el.op == ExprList.Op.AND) ? (Term.TRUE) : (Term.FALSE);
 			for (Expr c_ : el.args) {
 				switch (el.op) {
 				case AND:
 					result = result.and(translateExpr_p (c_,letBindings, atomVars));
 					break;
 				case OR:
 					result = result.or(translateExpr_p (c_,letBindings, atomVars));
 					break;
 				default:
 					throw new ErrorFatal ("Unsupported list operator in "+e);
 				}
 			}
 			return result;
 		}
 		if (e instanceof ExprQt) {
 			ExprQt qt = (ExprQt)e;
 			
 			for (int i = 0; i < qt.count(); ++i) {
 				pushId(qt.get(i)); // create name in id map
 				Expr bound = qt.getBound(i); // right hand side of var decl
 				if (arity(bound) == 1 && mult(bound) == Mult.ONE) {
 					atomVars.add(qt.get(i)); // save name as atom
 				}
 			}
 			Term f = translateExpr_p(qt.sub, letBindings, atomVars); // same thing for subterm
 			Term b = Term.TRUE;
 			for (Decl d : qt.decls) {
 				b = b.and(translateDecl(d, null, letBindings, atomVars, true)); // create constraint
 			}
 			
 
 			// comprehension
 			if (qt.op == Op.COMPREHENSION) { // TODO: exception 
 				String[] vars = new String[qt.count()];
 				for (int i = 0; i < qt.count(); ++i) {
 					final ExprVar v = qt.get(i);
 					vars[i] = id(v);
 					atomVars.remove(v);
 				}
 				return b.and(f).compr(vars);
 			}
 
 			// quantifier (hopefully)
 			// TODO: fix multiple binding support, 1) remove loop 2) ??? 
 			for (int i = qt.count()-1; i >= 0; --i) {
 				final ExprVar v = qt.get(i); // kommt von alloy (?)
 				String sort = "";
 				if (atomVars.contains(v))
 					sort = "Atom";
 				else
 					sort = "Rel"+arity(qt.getBound(i));
 				
 				atomVars.remove(v); // end loop here & dont forget to deal with popId and b
 
 				switch (qt.op) {
 				case NO:
 					if (i > 0)
 						f = b.and(f).exists(sort, id(v));
 					else
 						f = b.and(f).exists(sort, id(v)).not();
 					break;
 				case SOME:
 					f = b.and(f).exists(sort, id(v));	// TODO: fix multiple binding support
 					break;
 				case ALL:
 					f = b.implies(f).forall(sort,id(v));	// TODO: fix multiple binding support
 					break;
 				case LONE:{
 					if (qt.count() > 1) throw new ModelException ("Multiple variable bindings are not supported for lone quantifier.");
 					//TODO multiple variable bindings
 					String newVar = newVar("y", b.and(f));
 					Term boundY = b.substitute(id(v), newVar);
 					Term fY = f.substitute(id(v), newVar);
 					Term bound = b.and(boundY);
 					Term s1 = f.and(fY);
 					Term eq = term(v).equal(Term.var(newVar));
 					f = bound.implies(s1.implies(eq)).forall(sort, newVar).forall(sort, id(v));}
 					break;
 				case ONE:{
 					if (qt.count() > 1) throw new ModelException ("Multiple variable bindings are not supported for one quantifier.");
 					String newVar = newVar("y", b.and(f));
 					Term boundY = b.substitute(id(v), newVar);
 					Term fY = f.substitute(id(v), newVar);
 					Term eq = term(v).equal(Term.var(newVar));
 					Term s1 = boundY.and(fY).implies(eq).forall(sort, newVar);
 					f = b.and(f).and(s1).exists(sort, id(v));}
 					break;
 				default:
 					throw new ModelException ("Unsupported quantifier in "+e);
 				}
 				// bound included in innermost quantifier, can be ignored in
 				// next iterations
 				b = Term.TRUE;
 				popId();
 			}
 			return f;
 		}
 		if (e instanceof ExprLet) {                                  // let x = c | sub
 			ExprLet let = (ExprLet)e;
 			letBindings.put(let.var, translateExpr_p(let.expr, letBindings, atomVars));
 			Term f = translateExpr_p(let.sub, letBindings, atomVars);
 			letBindings.remove(let.var);
 			return f;
 		}
 		if (e instanceof ExprConstant) {
 			ExprConstant ec = (ExprConstant)e;
 			switch (ec.op) {
 			case TRUE:
 				return Term.TRUE;
 			case FALSE:
 				return Term.FALSE;
 			case IDEN:
 				target.declareIdentity();
 				return Term.call("iden");
 			case EMPTYNESS:
 				return none(1);
 			case NUMBER:
 				return Term.number(ec.num());
 			case MIN:
 			case MAX:
 				throw new ErrorFatal ("Max/Min values of integers are not allowed!");
 			case NEXT:
 				return Term.call("nextInt"); // TODO: SMT-fy this
 			case STRING:
 			default:
 				throw new ErrorFatal ("Unknown constant symbol: "+ec.op);
 			}
 		}
 		throw new ErrorFatal ("Could not translate expression: "+e);
 	}
 	
 	/**
 	 * Generate constraints for multiplicity annotations on the right-hand side of the arrow operator.<br>
 	 * The declaration considered here looks like this: <code>t : m1 e1 ->m2 e2 ->m3 e3</code>
 	 * @param t
 	 * The term being declared
 	 * @param typing
 	 * The chain of set valued terms, typing each component of the declared term,
 	 * i.e. the e1,e2 etc. in the exemplary declaration above.
 	 * @param mults
 	 * The right-hand side multiplicity annotations of the arrow operator,
 	 * i.e. the m1,m2 etc. in the exemplary declaration above. 
 	 * @return
 	 * conjunction of the multiplicity constraints
 	 */
 	private Term generateMultConstrRight (Term t, List<Pair<Term,Integer>> typing, List<Mult> mults) {
 		Term conj = Term.TRUE;
 		// we have a multiplicity annotation
 		switch (mults.get(0)) {
 		case ONE:
 			// TODO: declareOne
 			conj = conj.and (Term.call("one", t));
 			break;
 		case SOME:
 			// TODO: declare
 			conj = conj.and (Term.call("some", t));
 			break;
 		case LONE:
 			// TODO: declare
 			conj = conj.and (Term.call("lone", t));
 			break;
 		case SET:
 			break;
 		default:
 			throw new RuntimeException ("Unexpected!");
 		}
 		if (mults.size() > 1) {
 			int ar = 0;
 			for (Pair<Term,Integer> p : typing)
 				ar += p.b;
 			Pair<Term,Integer> b = typing.get(0);
 			//JoinLeft join = new JoinLeft(ar, Term.HOLE, b.b);
 			Term sub = generateMultConstrRight(SpecialJoin.right(t, ar), typing.subList(1, typing.size()), mults.subList(1, mults.size()));
 			conj = conj.and(quantify(sub, b.a, b.b));
 		}
 		return conj;
 	}
 	
 	/**
 	 * Generate constraints for multiplicity annotations on the left-hand side of the arrow operator.<br>
 	 * The declaration considered here looks like this: <code>t : e1 m1-> e2 m2-> e3</code>
 	 * @param t
 	 * The term being declared
 	 * @param typing
 	 * The chain of set valued terms, typing each component of the declared term,
 	 * i.e. the e1,e2 etc. in the exemplary declaration above.
 	 * @param mults
 	 * The left-hand side multiplicity annotations of the arrow operator,
 	 * i.e. the m1,m2 etc. in the exemplary declaration above. 
 	 * @return
 	 * conjunction of the multiplicity constraints
 	 */
 	private Term generateMultConstrLeft (Term t, List<Pair<Term,Integer>> typing, List<Mult> mults) {
 		Term conj = Term.TRUE;
 		// we have a multiplicity annotation
 		switch (mults.get(mults.size()-1)) {
 		case ONE:
 			conj = conj.and (Term.call("one", t)); // TODO: declare
 			break;
 		case SOME:
 			conj = conj.and (Term.call("some", t)); // TODO: declare
 			break;
 		case LONE:
 			conj = conj.and (Term.call("lone", t)); // TODO: declare
 			break;
 		case SET:
 			break;
 		default:
 			throw new RuntimeException ("Unexpected!");
 		}
 		if (mults.size() > 1) {
 			int ar = 0;
 			for (Pair<Term,Integer> p : typing)
 				ar += p.b;
 			Pair<Term,Integer> b = typing.get(typing.size()-1);
 			//JoinRight join = new JoinRight(ar,Term.HOLE,b.b);
 			Term sub = generateMultConstrLeft (SpecialJoin.right(t, ar), typing.subList(0, typing.size()-1), mults.subList(0, mults.size()-1));
 			conj = conj.and(quantify(sub, b.a, b.b));
 		}
 		return conj;
 	}
 	
 	/**
 	 * generate multiplicity constraints for a declaration of the form <code>t : [mult] e</code>
 	 * or <code>t : e1 n1->m1 e2 n2->m2 e3</code>
 	 * @param t
 	 * the term being declared
 	 * @param declExpr
 	 * the declaration expression with multiplicity annotations
 	 * @param letBindings
 	 * letBindings for translating the typing expressions
 	 * @param atomVars
 	 * variables that have to be surrounded by the singleton constructor
 	 * @param defaultToOne
 	 * whether no multiplicity of a unary expression defaults to one
 	 * @return
 	 * conjunction of multiplicity constraints
 	 * @ 
 	 */
 	private Term generateMultConstr (Term t, Expr declExpr,
 			HashMap<ExprHasName,Term> letBindings, HashSet<ExprHasName> atomVars,
 			boolean defaultsToOne) throws Err, ModelException {
 		// handle multiplicity of set-valued declaration expressions
 		int arity = declExpr.type().arity();
 		if (declExpr.mult == 1) {
 			switch (declExpr.mult()) {
 			case SOMEOF:
 				target.declareSome(arity);
 				return Term.call("some_" + arity, t);
 			case LONEOF:
 				target.declareLone(arity);
 				return Term.call("lone_" + arity, t);
 			case ONEOF:
 				target.declareOne(arity);
 				return Term.call("one_" + arity, t);
 			case SETOF:
 				return Term.TRUE;
 			}
 		} else if (arity(declExpr) == 1 && defaultsToOne) {
 			// no explicit multiplicity defaults to one
 			target.declareOne(arity);
 			return Term.call("one_" + arity, t);
 
 			
 		// handle multiplicity of relation-valued fields
 		} else if (declExpr.mult == 2) {
 			List<Pair<Term,Integer>> typing = createExprChain (declExpr,letBindings,atomVars);
 			System.out.println("### declExpr: "+declExpr+" \n    exprChain: "+ typing);
 			List<Mult> multR = createMultChainRight (declExpr);
 			List<Mult> multL = createMultChainLeft (declExpr);
 			if (typing.size() < 2 || multR.size() != multL.size() || multR.size()+1 != typing.size())
 				throw new RuntimeException ("Must be a weird declaration expression: "+declExpr);
 			
 			Pair<Term,Integer> b = typing.get(0);
 			//TermAlternation join = new JoinLeft(arity(declExpr), Term.HOLE, b.b);
 			Term right = generateMultConstrRight(SpecialJoin.left(t, arity(declExpr)), typing.subList(1,typing.size()), multR);
 			Term c = quantify(right, b.a, b.b);
 		
 			b = typing.get(typing.size()-1);
 			//join = new JoinRight(arity(declExpr), Term.HOLE, b.b);
 			Term left = generateMultConstrLeft(SpecialJoin.right(t, arity(declExpr)), typing.subList(0,typing.size()-1), multL);
 			return c.and(quantify(left, b.a, b.b));
 		}
 		
 		// no multiplicity annotation
 		return Term.TRUE;
 	}
 	
 	/**
 	 * universally quantify a formula
 	 * @param body
 	 * the body of the quantifier, containing a holes to fill in the
 	 * quantification term
 	 * @param bound
 	 * the bounding term for the quantification
 	 * @param arity
 	 * arity of the bounding term
 	 * @return
 	 * formula of the form
 	 * <code>\forall Atom x; \forall Atom y; (in(binary(x,y),bound) -> body[sin2(binary(x,y))])</code>
 	 */
 	private Term quantify (Term body, Term bound, int arity) {
 		assert arity >= 1;
 		TermVar[] atoms = new TermVar[arity];
 		for (int i = 0; i < arity; ++i) {
 			atoms[i] = Term.var(newVar("x"+i,body,bound));
 		}
 		Term q;
 		switch (arity) {
 		case 1:
 			q = atoms[0];
 			break;
 		case 2:
 			q = Term.call ("binary", atoms);
 			break;
 		case 3:
 			q = Term.call ("ternary", atoms);
 			break;
 		default:
 			throw new RuntimeException ("Expressions of arity highter than 3 are currently not supported.");
 		}
 		
 		Term f = Term.call("in",q,bound).implies(body.fill(a2r(arity,q)));		
 		f = f.forall("Atom", atoms);
 		return f;
 	}
 	
 	
 	// disassemble an arrow-expression to its components.
 	// returns a list of pairs of the translated expression and their arities
 	private List<Pair<Term,Integer>> createExprChain (Expr expr,
 			HashMap<ExprHasName,Term> letBindings, HashSet<ExprHasName> atomVars) throws Err, ModelException {
 		LinkedList<Pair<Term,Integer>> ret = new LinkedList<Pair<Term,Integer>>();
 		if (expr.mult != 2) {
 			ret.add(new Pair<Term,Integer>(translateExpr_p(expr, letBindings, atomVars),arity(expr)));
 			return ret;
 		}
 		if (!(expr instanceof ExprBinary)) throw new RuntimeException ("Unexpected!");
 		ExprBinary arrowExp = (ExprBinary)expr;
 		ret.addAll(createExprChain(arrowExp.left, letBindings, atomVars));
 		ret.addAll(createExprChain(arrowExp.right, letBindings, atomVars));
 		return ret;
 	}
 	// create chain of right-hand multiplicities of the arrow-operators
 	private List<Mult> createMultChainRight (Expr expr) throws Err {
 		if (expr.mult != 2)
 			return new LinkedList<Mult>();
 		if (!(expr instanceof ExprBinary)) throw new RuntimeException ("Unexpected!");
 		ExprBinary arrowExp = (ExprBinary)expr;
 		LinkedList<Mult> ret = new LinkedList<Mult>();
 		ret.addAll(createMultChainRight(arrowExp.left));
 		switch (arrowExp.op) {
 			case ARROW:
 			case SOME_ARROW_ANY:
 			case ONE_ARROW_ANY:
 			case LONE_ARROW_ANY:
 				ret.add(Mult.SET);
 				break;
 			case ANY_ARROW_SOME:
 			case SOME_ARROW_SOME:
 			case ONE_ARROW_SOME:
 			case LONE_ARROW_SOME:
 				ret.add(Mult.SOME);
 				break;
 			case ANY_ARROW_ONE:
 			case SOME_ARROW_ONE:
 			case ONE_ARROW_ONE:
 			case LONE_ARROW_ONE:
 				ret.add(Mult.ONE);
 				break;
 			case ANY_ARROW_LONE:
 			case SOME_ARROW_LONE:
 			case ONE_ARROW_LONE:
 			case LONE_ARROW_LONE:
 				ret.add(Mult.LONE);
 				break;
 			default:
 				throw new RuntimeException("");
 		}
 		ret.addAll(createMultChainRight(arrowExp.right));
 		return ret;
 	}
 	// create chain of left-hand multiplicities of the arrow-operators
 	private List<Mult> createMultChainLeft (Expr expr) throws Err {
 		if (expr.mult != 2)
 			return new LinkedList<Mult>();
 		if (!(expr instanceof ExprBinary)) throw new RuntimeException ("Unexpected!");
 		ExprBinary arrowExp = (ExprBinary)expr;
 		LinkedList<Mult> ret = new LinkedList<Mult>();
 		ret.addAll(createMultChainLeft(arrowExp.left));
 		switch (arrowExp.op) {
 			case ARROW:
 			case ANY_ARROW_SOME:
 			case ANY_ARROW_ONE:
 			case ANY_ARROW_LONE:
 				ret.add(Mult.SET);
 				break;				
 			case SOME_ARROW_ANY:
 			case SOME_ARROW_SOME:
 			case SOME_ARROW_ONE:
 			case SOME_ARROW_LONE:
 				ret.add(Mult.SOME);
 				break;	
 			case ONE_ARROW_ANY:
 			case ONE_ARROW_SOME:
 			case ONE_ARROW_ONE:
 			case ONE_ARROW_LONE:
 				ret.add(Mult.ONE);
 				break;
 			case LONE_ARROW_ANY:
 			case LONE_ARROW_SOME:
 			case LONE_ARROW_ONE:
 			case LONE_ARROW_LONE:
 				ret.add(Mult.LONE);
 				break;
 			default:
 				throw new RuntimeException("");
 		}
 		ret.addAll(createMultChainLeft(arrowExp.right));
 		return ret;
 	}
 	
 	/*
 	 * Helper methods
 	 * --------------
 	 */
 	
 	/**
 	 * @param e
 	 * Alloy expression
 	 * @return
 	 * arity of the expression
 	 */
 	private int arity (Expr e) {
 		return e.type().arity();
 	}
 	
 	/**
 	 * Get the multiplicity of a unary expression
 	 * @param e
 	 * the expression
 	 * @return
 	 * the annotated multiplicity, if any, ONE otherwise
 	 */
 	private static Mult mult (Expr e) {
 		if (e.mult == 0) return Mult.ONE;
 		if (!(e instanceof ExprUnary)) throw new RuntimeException("Unexpected!");
 		ExprUnary ue = (ExprUnary)e;
 		switch (ue.op) {
 			case ONEOF:
 				return Mult.ONE;
 			case SOMEOF:
 				return Mult.SOME;
 			case SETOF:
 				return Mult.SET;
 			case LONEOF:
 				return Mult.LONE;
 			case NOOP:
 				return mult (ue.sub);
 			default:
 				throw new RuntimeException ("Unexpected!");
 		}
 	}
 	
 	/**
 	 * @return
 	 * the signature identified by <code>name</code> 
 	 */
 	private Sig getSig (String name) {
 		boolean prefixed = name.indexOf('/') != -1;
 		if (prefixed) {
 			for (Sig s : mod.getAllReachableSigs()) {
 				if (s.toString().equals(name))
 					return s;
 			}
 		} else {
 			for (Sig s : mod.getAllReachableSigs()) {
 				if (Util.removePrefix(s.toString()).equals(name))
 					return s;
 			}
 		}
 		throw new RuntimeException ("Signature of name "+name+ " was not found!");
 	}
 	
 	/**
 	 * check whether a given position is located
 	 * in the root model's file
 	 * @return
 	 * true if the position is in the root model's file
 	 * or null, false otherwise 
 	 */
 	private boolean inRootMod(Pos p) {
 		if (p == null)
 			return true;
 		return p.equals(mod.pos().filename);
 	}
 	
 	/** make a KeY expression from an alloy signature **/
 	private Term term(Sig s) {
 		return Term.call(id(s));
 	}
 	
 	/** make a KeY expression from a named entity and the given atoms **/
 	private Term term(ExprHasName e, HashSet<ExprHasName> atomVars) {
 		if (e instanceof ExprVar) {
 			if (atomVars.contains(e))
 				return a2r(arity(e), Term.var(id(e)));
 			else if (e.label.equals("this"))
 				return THISTerm();
 			else
 				return Term.var(id(e));
 		}
 		if (e instanceof Field)
 			return Term.call(id(e));	
 		throw new RuntimeException ("Unexpected!");
 	}
 	
 	/** make a KeY expression from a named entity, such as a field, a let or a function parameter **/
 	private Term term(ExprHasName e) {
 		return term(e,new HashSet<ExprHasName>());
 	}
 	
 	/** make a KeY expression for the inclusion expression
 	 * @param v the variable to look for
 	 * @param s a signature containing <code>v</code>
 	 * @ 
 	 **/
 	private Term in(String v, Sig s){
 		target.declareIn(1);
 		return Term.call("in_1", Term.var(v), term(s));
 	}
 	
 	/**
 	 * make a KeY function with the given name (and two parameters)
 	 * @param name of the function
 	 * @param a sig
 	 * @param b another sig
 	 */
 	private Term call(String name, Sig a, Sig b) {
 		return Term.call(name, term(a), term(b));
 	}
 	
 	private Term none(int ar) {
 		target.declareNone(ar);
 		return Term.call("none_"+ar);
 	}
 	
 	private static Term a2r(int ar, Term sub) {
 		declareA2r(ar);
 		return Term.call("a2r_"+ar, sub);
 	}
 
 	/** static helper function to "declare" the converter function statically.
 	 * look inside for whatever mean trick I decided to use */
 	private static void declareA2r(int ar) {
 		// "target" is now static
 		Translator.target.declareA2r(ar);
 	}
 
	private Term univ(int ar) throws ModelException {
 		if (ar == 1)
 			return term(Sig.UNIV);
 		else {
 			target.declareProduct(1, ar-1);
 			return Term.call("prod1x"+(ar-1), term(Sig.UNIV), univ(ar-1));
 		}			
 	}
 	
 	/**
 	 * @return
 	 * the expression <code>e</code> with all multiplicity
 	 * annotations removed.
 	 */
 	private Expr deMult (Expr e) {
 		//TODO better implementation, perhaps as visitor
 		if (e instanceof ExprUnary) {
 			switch (((ExprUnary) e).op) {
 			case SETOF:
 			case ONEOF:
 			case LONEOF:
 			case SOMEOF:
 				return ((ExprUnary) e).sub;
 			default:
 				return deMult (((ExprUnary) e).sub);
 			}
 		}
 		if (e instanceof ExprBinary) {
 			ExprBinary eb = (ExprBinary)e;
 			switch (eb.op) {
 			case ANY_ARROW_SOME:
 			case ANY_ARROW_ONE:
 			case ANY_ARROW_LONE:
 			case SOME_ARROW_ANY:
 		    case SOME_ARROW_SOME:
 		    case SOME_ARROW_ONE:
 		    case SOME_ARROW_LONE:
 		    case ONE_ARROW_ANY:
 		    case ONE_ARROW_SOME:
 		    case ONE_ARROW_ONE:
 		    case ONE_ARROW_LONE:
 		    case LONE_ARROW_ANY:
 		    case LONE_ARROW_SOME:
 		    case LONE_ARROW_ONE:
 		    case LONE_ARROW_LONE:
 		    	return eb.left.product(eb.right);
 			}
 		}
 		return e;
 	}
 	
 	/** replaces the static "THIS" expression
 	 * @return a relation containing the "THIS" atom
 	 */
 	private static Term THISTerm() {
 		target.declareA2r(1);
 		return Term.call("a2r_1",Term.var("this"));
 	}
 	
 	private interface TermAlternation {
 		public abstract Term alter(Term t);
 	}
 	
 	private class ThisJoin implements TermAlternation {
 		private int ar;
 		
 		public ThisJoin (int arity) {
 			ar = arity;
 		}
 		
 		public Term alter(Term t) {
 			target.declareJoin(1, ar);			
 			return Term.call("join1x"+ar, THISTerm(), t);
 		}
 	}
 	
 /*	private class JoinLeft implements TermAlternation {
 		private int ar;
 		private Term j;
 		
 		public JoinLeft (int arity, Term j) {
 			ar = arity;
 			this.j = j;
 		}
 		
 		public Term alter(Term t) {
 			return Term.call("join1x"+ar, j, t);
 		}		
 	}
 	
 	private class JoinRight implements TermAlternation {
 		private int ar;
 		private Term j;
 		
 		public JoinRight (int arity, Term j) {
 			ar = arity;
 			this.j = j;
 		}
 		
 		public Term alter(Term t) {
 			return Term.call("join"+ar+"x1",t, j);
 		}		
 	}*/
 	
 	/**
 	 * this class represents a term of the form join1x2(sin(a),join1x3(sin(b),R))
 	 * special about this class is that the join atoms (e.g. a, b) are set when
 	 * the hole in R is filled.
 	 * E.g. if fill("sin2(binary(x,y))") is called, 
 	 * 
 	 */
 	private static class SpecialJoin extends Term {
 		
 		private boolean right;
 		private Term sub;
 		private int arity;
 		
 		public static Term right(Term sub, int ar) {
 			return new SpecialJoin(true, sub, ar);
 		}
 		public static Term left (Term sub, int ar) {
 			return new SpecialJoin(false, sub, ar);
 		}
 		
 		private SpecialJoin (boolean right, Term sub, int ar) {
 			this.right = right;
 			this.sub = sub;
 			this.arity = ar;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public boolean occurs(String id) {
 			return sub.occurs(id);
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term substitute(String a, String b) {
 			return new SpecialJoin (right, sub.substitute(a, b), arity);
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public String toStringTaclet() {
 			return null;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public boolean isInt() {
 			return false;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term fill(Term t) {
 			Term[] terms = getElements(t);
 			if (terms.length <= 0 || terms.length > 3 || terms.length >= arity)
 				throw new RuntimeException("");
 			Term ret = sub;
 			int ar = arity;
 			if (right) {
 				for (int i = terms.length-1; i >= 0; --i) {
 					ret = Term.call("join"+(ar--)+"x1", ret, a2r(1,terms[i]));
 				}
 			} else {
 				for (int i = 0; i < terms.length; ++i) {
 					ret = Term.call("join1x"+(ar--), a2r(1,terms[i]), ret);
 				}
 			}
 			return ret;
 		}
 		
 		private Term[] getElements (Term t) {
 			// quick 'n dirty
 			Term sub0 = ((TermCall)t).params()[0];
 			if (sub0 instanceof TermCall)
 				return ((TermCall)sub0).params();
 			else
 				return new Term[] {sub0};
 		}
 		@Override
 		public String toString() {
 			// not quite sure what this means, but toStringTaclet() does do anything either
 			return null;
 		}
 		
 	}
 	
 	private enum Mult {
 		SET,
 		LONE,
 		ONE,
 		SOME
 	}
 
 }
 
 
 
