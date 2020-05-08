 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 
 public abstract class CuClass {
 	protected String text = "";
 	String name;
 	CuType superType = new Top();
 	List<String> kindCtxt = null;
 	//Bad naming. This is only for preset classes.
 	Map<String, CuTypeScheme> mFunctions = new HashMap<String, CuTypeScheme>();
 	// we probably don't need this since we are using CuFun
 	// List<String> implemented_methods = new ArrayList<String>();
 
 	HashMap<String, CuFun> funList = new HashMap<String, CuFun>();
 
 	public void add(List<CuExpr> s) {
 	}
 
 	public void addSuper(CuType t) {
 	}
 
 	public void add(CuStat s) {
 	}
 
 	public void add(String v, CuTypeScheme ts, CuStat s) {
 	}
 
 	public void add(String v_name, CuTypeScheme ts) {
 	}
 
 	public void add(CuVvc v_name, CuTypeScheme ts) {
 	}
 
 	public boolean isInterface() {
 		return false;
 	}
 
 	public CuClass calculateType(CuContext context) throws NoSuchTypeException {
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		return text;
 	}
 }
 
 class Cls extends CuClass {
 	Map<String, CuType> fieldTypes = new LinkedHashMap<String, CuType>();
 
 	// List<CuType> appliedTypePara=new ArrayList<CuType>();
 	List<CuStat> classStatement = new ArrayList<CuStat>();
 	// private static final Exception NoSuchTypeExpression() = null;
 
 	List<CuExpr> superArg;
 
 	public Cls(String clsintf, List<String> kc, LinkedHashMap<String, CuType> tc) {
 		super.name = clsintf;
 		super.kindCtxt = kc;
 		this.fieldTypes = tc;
 	}
 
 	@Override
 	public void add(CuStat s) {
 		classStatement.add(s);
 	}
 
 	// TODO: grab all the methods here
 	@Override
 	public void addSuper(CuType tt) {
 		super.superType = tt;
 	}
 
 	@Override
 	public void add(String v, CuTypeScheme ts, CuStat s) {
 		super.funList.put(v, new Function(v, ts, s));
		super.mFunctions.put(v, ts);
 	}
 
 	@Override
 	public void add(List<CuExpr> s) {
 		this.superArg = s;
 	}
 
 	@Override
 	public CuClass calculateType(CuContext context) throws NoSuchTypeException {
 		// keep track of different versions of context
 		CuContext snd_context = new CuContext(context);
 		snd_context.mKind = kindCtxt;
 
 		// we need to type check tau
 		CuType tau_hat = super.superType.calculateType(snd_context);
 
 		// here, we check all the function names, they should not appear in
 		// context's mfunctions
 		if (snd_context.mFunctions.containsKey(super.name)) {
 			throw new NoSuchTypeException();
 		}
 
 		// Figure 10 rule 2, line 2
 		snd_context.updateClass(name, this);
 		List<CuType> constr_gene = new ArrayList<CuType>();
 		for (String s : kindCtxt) {
 			constr_gene.add(new VTypePara(s));
 		}
 		CuType constr_classType = new VClass(name, constr_gene);
 		TypeScheme ts_contructor = new TypeScheme(new ArrayList<String>(),
 				fieldTypes, constr_classType);
		funList.put(name,new Function(name, ts_contructor, new EmptyBody()));
 		snd_context.updateFunction(name, ts_contructor);
		context.updateFunction(name, ts_contructor);
 
 		// Figure 10 rule 2, line 3
 		for (CuFun iter : funList.values()) {
 			iter.ts.calculateType(snd_context);
 		}
 
 		// Figure 10 rule 2, line 4
 
 		for (CuType ct : this.fieldTypes.values()) {
 			ct.calculateType(snd_context);
 		}
 		snd_context.mMutVariables = this.fieldTypes;
 		for (CuStat s : classStatement) {
 			s.calculateType(snd_context);
 		}
 		snd_context.mergeVariable();
 
 		// Figure 10 rule 2, line 5
 		if (!tau_hat.id.equals(CuVvc.TOP)) {
 			CuExpr temp_expr = new VcExp(tau_hat.id, new ArrayList<CuType>(),
 					this.superArg);
 			CuType retype = temp_expr.calculateType(snd_context);
 			if (!retype.equals(super.superType)) {
 				throw new NoSuchTypeException();
 			}
 			snd_context.mergeVariable();
 		}
 
 		// finally update global context here before we deal with the methods.
 		context.mClasses = snd_context.mClasses;
 
 		// Figure 10 rule 2, line 6
 		if (!(superType instanceof Top) && !(superType instanceof VClass)
 				&& !(superType instanceof VTypeInter)) {
 			throw new NoSuchTypeException();
 		}
 
 		// make a local copy of current function list, because we only need to
 		// type check these functions
 		// Map<String, CuFun> funList_cpy = new HashMap<String, CuFun>();
 		// funList_cpy.putAll(funList);
 
 		if (superType instanceof VClass) {
 			Map<String, CuFun> superfunLst = snd_context.mClasses
 					.get(superType.id).funList;
 			for (Map.Entry<String, CuFun> e : superfunLst.entrySet()) {
 				// check signature if already exists
 				if (funList.containsKey(e.getKey())) {
 					e.getValue().ts.calculateType(snd_context);
 					// signature of inherited methods should match
 					if (!e.getValue().ts.sameAs(funList.get(e.getKey()).ts,
 							snd_context)) {
 						throw new NoSuchTypeException();
 					}
 					// if this method doesn't have an implementation, but super
 					// interface has an implementation,
 					// grab it
 					if (funList.get(e.getKey()).funBody instanceof EmptyBody) {
 						funList.get(e.getKey()).funBody = e.getValue().funBody;
 					}
 				}
 				// add method if it doesn't already exist
 				else {
 					// interface declared method but no implementation anywhere
 					if (superType.calculateType(snd_context).equals(CuType.top)
 							&& (e.getValue().funBody instanceof EmptyBody))
 						throw new NoSuchTypeException();
 					snd_context.mFunctions.put(e.getKey(), e.getValue().ts);
 					funList.put(e.getKey(), e.getValue());
 				}
 			}
 		} else if (superType instanceof VTypeInter) {
 			for (CuType t : superType.parentType) {
 				Map<String, CuFun> superfunLst = snd_context.mClasses.get(t.id).funList;
 				for (Map.Entry<String, CuFun> e : superfunLst.entrySet()) {
 					// check signature if already exists
 					if (funList.containsKey(e.getKey())) {
 						e.getValue().ts.calculateType(snd_context);
 						// signature of inherited methods should match
 						if (!e.getValue().ts.sameAs(funList.get(e.getKey()).ts,
 								snd_context)) {
 							throw new NoSuchTypeException();
 						}
 						// use the first implementation of inherited interfaces
 						if (funList.get(e.getKey()).funBody instanceof EmptyBody) {
 							funList.get(e.getKey()).funBody = e.getValue().funBody;
 						}
 					}
 					// add method if it doesn't already exist
 					else {
 						funList.put(e.getKey(), e.getValue());
 					}
 				}
 			}
 		}
 
 		// now type check each typescheme
 		// Figure 10 rule 2 line 7
 		for (CuFun iter : funList.values()) {
			if (snd_context.mFunctions.containsKey(iter.v) && !iter.v.equals(name)) {
 				throw new NoSuchTypeException();
 			}
 			List<String> theta_bar = iter.ts.data_kc;
 			for (String str_iter : theta_bar) {
 				if (super.kindCtxt.contains(str_iter)) {
 					throw new NoSuchTypeException();
 				}
 			}
 		}
 
 		// Figure 10 rule 2 line 8
 		for (CuFun iter : funList.values()) {
 			iter.ts.calculateType(snd_context);
 			// only check those that have function body
 			if (!(iter.funBody instanceof EmptyBody)) {
 				CuTypeScheme ts = iter.ts;
 				CuContext tempContext = new CuContext(snd_context);
 				tempContext.mKind.addAll(ts.data_kc);
 				tempContext.mMutVariables = ts.data_tc;
 				HReturn re = iter.funBody.calculateType(tempContext);
 				if (re.b == false || !re.tau.isSubtypeOf(ts.data_t)) {
 					throw new NoSuchTypeException();
 				}
 			}
 		}
 
 		// Figure 10 rule 2 line 8
 		// funlist field in each Cls gaurentees that.
 
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		/*
 		 * return
 		 * String.format("class %s %s %s extends %s { %s super ( %s ) ; %s }",
 		 * super.name, Helper.printList("<", super.kindCtxt, ">", ","),
 		 * Helper.printMap("(", this.fieldTypes, ")", ","),
 		 * superType.toString(), Helper.printList("", classStatement, "", ""),
 		 * Helper.printList("(", this.superArg, ")", ","), Helper.printList("",
 		 * fun, "", ""));
 		 */
 		return "to string messed up";
 	}
 
 }
 
 class Intf extends CuClass {
 	// Map<String, CuType> fieldTypes=new LinkedHashMap<String,CuType>();
 
 	// List<CuType> appliedTypePara=new ArrayList<CuType>();
 	// List<CuStat> classStatement = new ArrayList<CuStat>();
 
 	public Intf(String iname, List<String> kname) {
 		super.name = iname;
 		super.kindCtxt = kname;
 
 		// for printing
 		text = "interface " + name.toString() + " <";
 		for (String s : kindCtxt) {
 			text += " " + s.toString();
 		}
 		text += " > extends";
 	}
 
 	@Override
 	public void addSuper(CuType tt) throws NoSuchTypeException {
 		super.superType = tt;
 	}
 
 	@Override
 	public void add(String v, CuTypeScheme ts, CuStat s) {
 		super.funList.put(v, new Function(v, ts, s));
		super.mFunctions.put(v, ts);
 	}
 
 	@Override
 	public CuClass calculateType(CuContext context) throws NoSuchTypeException {
 		// we don't want to modify context
 		CuContext snd_context = new CuContext(context);
 		snd_context.mKind = super.kindCtxt;
 		// we need to type check tau
 		CuType tau_hat = super.superType.calculateType(snd_context);
 		if (!tau_hat.id.equals(CuVvc.TOP)) {
 			throw new NoSuchTypeException();
 		}
 
 		snd_context.updateClass(name, this);
 		if (!(superType instanceof Top) && !(superType instanceof VClass)
 				&& !(superType instanceof VTypeInter)) {
 			throw new NoSuchTypeException();
 		}
 		// make a copy of current function list, because we only need to type
 		// check these functions
 		// Map<String, CuFun> funList_cpy = new HashMap<String, CuFun>();
 		// funList_cpy.putAll(funList);
 
 		if (superType instanceof VClass) {
 			Map<String, CuFun> superfunLst = snd_context.mClasses
 					.get(superType.id).funList;
 			for (Map.Entry<String, CuFun> e : superfunLst.entrySet()) {
 				// check signature if already exists
 				if (funList.containsKey(e.getKey())) {
 					e.getValue().ts.calculateType(snd_context);
 					// signature of inherited methods should match
 					if (!e.getValue().ts.sameAs(funList.get(e.getKey()).ts,
 							snd_context)) {
 						throw new NoSuchTypeException();
 					}
 					// if this method doesn't have an implementation, but super
 					// interface has an implementation,
 					// grab it
 					if (funList.get(e.getKey()).funBody instanceof EmptyBody) {
 						funList.get(e.getKey()).funBody = e.getValue().funBody;
 					}
 				}
 				// add method if it doesn't already exist
 				else {
 					funList.put(e.getKey(), e.getValue());
 				}
 			}
 		} else if (superType instanceof VTypeInter) {
 			for (CuType t : superType.parentType) {
 				Map<String, CuFun> superfunLst = snd_context.mClasses.get(t.id).funList;
 				for (Map.Entry<String, CuFun> e : superfunLst.entrySet()) {
 					// check signature if already exists
 					if (funList.containsKey(e.getKey())) {
 						e.getValue().ts.calculateType(snd_context);
 						// signature of inherited methods should match
 						if (!e.getValue().ts.sameAs(funList.get(e.getKey()).ts,
 								snd_context)) {
 							throw new NoSuchTypeException();
 						}
 						// use the first implementation of inherited interfaces
 						if (funList.get(e.getKey()).funBody instanceof EmptyBody) {
 							funList.get(e.getKey()).funBody = e.getValue().funBody;
 						}
 					}
 					// add method if it doesn't already exist
 					else {
 						super.funList.put(e.getKey(), e.getValue());
 					}
 				}
 			}
 		}
 
 		// now type check each typescheme
 		// Figure 10 rule 1 line 5
 		for (CuFun iter : funList.values()) {
 			//no name hiding
 			if (snd_context.mFunctions.containsKey(iter.v)) {
 				throw new NoSuchTypeException();
 			}
 			List<String> theta_bar = iter.ts.data_kc;
 			for (String str_iter : theta_bar) {
 				if (snd_context.mKind.contains(str_iter)) {
 					throw new NoSuchTypeException();
 				}
 			}
 		}
 
 		// Figure 10 rule 1 line 6
 		for (CuFun iter : funList.values()) {
 			iter.ts.calculateType(snd_context);
 			// only check those that have function body
 			if (!(iter.funBody instanceof EmptyBody)) {
 				CuTypeScheme ts = iter.ts;
 				CuContext tempContext = new CuContext(snd_context);
 				tempContext.mKind.addAll(ts.data_kc);
 				tempContext.mMutVariables = ts.data_tc;
 				HReturn re = iter.funBody.calculateType(tempContext);
 				if (re.b == false || !re.tau.isSubtypeOf(ts.data_t)) {
 					throw new NoSuchTypeException();
 				}
 			}
 		}
 
 		// finally update globle context
 		context.mClasses = snd_context.mClasses;
 		return this;
 	}
 
 	@Override
 	public boolean isInterface() {
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		/*
 		 * return
 		 * String.format("class %s %s %s extends %s { %s super ( %s ) ; %s }",
 		 * super.name, Helper.printList("<", super.kindCtxt, ">", ","),
 		 * Helper.printMap("(", this.fieldTypes, ")", ","),
 		 * superType.toString(), Helper.printList("", classStatement, "", ""),
 		 * Helper.printList("(", this.superArg, ")", ","), Helper.printList("",
 		 * fun, "", ""));
 		 */
 		return "to string messed up";
 	}
 }
 
 // ======Class init=========
 
 class VBoolean extends Cls {
 	Boolean v = false;
 
 	public VBoolean() {
 		super("Boolean", new ArrayList<String>(),
 				new LinkedHashMap<String, CuType>());
 		// if (val instanceof Boolean) { v=val; }
 		// else { throw new NoSuchTypeException();}
 		// Just one example, we also need to add other methods
 		CuTypeScheme ts;
 		ts = new TypeScheme(new ArrayList<String>(),
 				new LinkedHashMap<String, CuType>(), CuType.bool);
 		super.mFunctions.put("negate", ts);
 		HashMap<String, CuType> arg = new LinkedHashMap<String, CuType>();
 		arg.put("that", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("and", ts);
 		super.mFunctions.put("or", ts);
 		super.mFunctions.put("equals", ts);
 
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("upper", CuType.bool);
 		arg.put("includeLower", CuType.bool);
 		arg.put("includeUpper", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, new Iter(CuType.bool));
 		super.mFunctions.put("through", ts);
 
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("inclusive", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, new Iter(CuType.bool));
 		super.mFunctions.put("onwards", ts);
 
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("that", CuType.bool);
 		arg.put("strict", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("lessThan", ts);
 
 	}
 
 	public boolean calculateType() {
 		return v;
 	}
 }
 
 class VInteger extends Cls {
 	Integer v = 0;
 
 	public VInteger() {
 		super("Integer", new ArrayList<String>(),
 				new LinkedHashMap<String, CuType>());
 		// if (val instanceof Integer) { v=val; }
 		// else { throw new NoSuchTypeException();}
 		CuTypeScheme ts;
 		ts = new TypeScheme(new ArrayList<String>(),
 				new LinkedHashMap<String, CuType>(), CuType.integer);
 		super.mFunctions.put("negative", ts);
 
 		HashMap<String, CuType> arg = new LinkedHashMap<String, CuType>();
 		arg.put("operator", CuType.integer);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.integer);
 		super.mFunctions.put("times", ts);
 		super.mFunctions.put("plus", ts);
 		super.mFunctions.put("minus", ts);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("equals", ts);
 		ts = new TypeScheme(new ArrayList<String>(), arg, new Iter(
 				CuType.integer));
 		super.mFunctions.put("divide", ts);
 		super.mFunctions.put("modulo", ts);
 
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("upper", CuType.integer);
 		arg.put("includeLower", CuType.bool);
 		arg.put("includeUpper", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, new Iter(
 				CuType.integer));
 		super.mFunctions.put("through", ts);
 
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("inclusive", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, new Iter(
 				CuType.integer));
 		super.mFunctions.put("onwards", ts);
 
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("that", CuType.integer);
 		arg.put("strict", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("lessThan", ts);
 	}
 
 	public VInteger calculateType() {
 		return this;
 	}
 }
 
 class VCharacter extends Cls {
 	Character c;
 
 	public VCharacter() {
 		super("Character", new ArrayList<String>(),
 				new LinkedHashMap<String, CuType>());
 		// if (val instanceof Character) { c=val; }
 		// else { throw new NoSuchTypeException();}
 		CuTypeScheme ts;
 		ts = new TypeScheme(new ArrayList<String>(),
 				new LinkedHashMap<String, CuType>(), CuType.integer);
 		super.mFunctions.put("unicode", ts);
 
 		HashMap<String, CuType> arg = new LinkedHashMap<String, CuType>();
 		arg.put("that", CuType.character);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("equals", ts);
 	}
 
 	public VCharacter calculateType() {
 		return this;
 	}
 }
 
 class VString extends Cls {
 	String v = "";
 
 	public VString() {
 		super("String", new ArrayList<String>(),
 				new LinkedHashMap<String, CuType>());
 		this.addSuper(new Iter(CuType.character));
 		// if (val instanceof String) { v=val; }
 		// else { throw new NoSuchTypeException();}
 		HashMap<String, CuType> arg = new LinkedHashMap<String, CuType>();
 		arg.put("that", CuType.string);
 		CuTypeScheme ts = new TypeScheme(new ArrayList<String>(), arg,
 				CuType.bool);
 		super.mFunctions.put("equals", ts);
 	}
 
 	public VString calculateType() {
 		return this;
 	}
 }
 
 class VIterable extends Cls {
 	List<CuType> v;
 
 	public VIterable(List<String> kc) {
 		super("Iterable", kc, new LinkedHashMap<String, CuType>());
 		// if (val instanceof List<CuType>) { v=val; }
 		// else { throw new NoSuchTypeException();}
 	}
 
 	public VIterable calculateType() {
 		return this;
 	}
 }
