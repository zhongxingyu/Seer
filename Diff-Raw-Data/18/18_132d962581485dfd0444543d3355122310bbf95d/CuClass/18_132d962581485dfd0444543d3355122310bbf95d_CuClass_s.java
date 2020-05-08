 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 
 public abstract class CuClass {
 	protected String text = "";
 	String name;
 	CuType            superType = new Top();	
 	List<String>       kindCtxt = null;
 	Map<String,CuTypeScheme> mFunctions = new HashMap<String,CuTypeScheme>();
 	//we probably don't need this since we are using CuFun
 	//List<String> implemented_methods = new ArrayList<String>();
 	
 	HashMap<String, CuFun>  funList = new HashMap<String, CuFun>();
 
 	public void add(List<CuExpr> s) {}
 	public void addSuper(CuType t) {}
 	public void add(CuStat s) {}
 	public void add(String v, CuTypeScheme ts, CuStat s) {}
 	public void add(String v_name, CuTypeScheme ts) {}
 	public void add(CuVvc v_name, CuTypeScheme ts) {}
 	public boolean isInterface() {return false; }
 	public CuClass calculateType(CuContext context) throws NoSuchTypeException { return this;}
 
 	@Override public String toString() {
 		return text;
 	}
 }
 
 class Cls extends CuClass {
 	Map<String, CuType> fieldTypes=new LinkedHashMap<String,CuType>();
 	
 	//List<CuType> appliedTypePara=new ArrayList<CuType>();
 	List<CuStat> classStatement = new ArrayList<CuStat>();
 	//private static final Exception NoSuchTypeExpression() = null;
 	
 	List<CuExpr>        superArg;
 
 	public Cls(String clsintf, List<String> kc, LinkedHashMap<String, CuType> tc) {
 		super.name=clsintf;
 		super.kindCtxt=kc;
 		this.fieldTypes=tc;
 	}
 
 	@Override public void add (CuStat s) {
 		classStatement.add(s);}
 
 	//TODO: grab all the methods here
 	@Override public void addSuper (CuType tt) {
 		super.superType = tt;}
 	
 	@Override public void add(String v, CuTypeScheme ts, CuStat s) {
 		super.funList.put(v, new Function(v,ts,s));
 		super.mFunctions.put(v, ts);
 	}
 	
 	@Override public CuClass calculateType(CuContext context) throws NoSuchTypeException { 
 		//we don't want to modify context
 		CuContext cur_context = new CuContext(context);
 		cur_context.mKind = super.kindCtxt;
 		//we need to type check tau 
 		CuType tau_hat = super.superType.calculateType(cur_context);
 		
		if (!(superType instanceof VClass) && !(superType instanceof VTypeInter)) {
 			throw new NoSuchTypeException();
 		}
 		//make a copy of current function list, because we only need to type check these functions
 		Map<String, CuFun> funList_cpy = new HashMap<String, CuFun>();
 		funList_cpy.putAll(funList);
 		
 		if (superType instanceof VClass){
 			Map<String, CuFun> superfunLst = cur_context.mClasses.get(superType.id).funList;
 			for (Map.Entry<String, CuFun> e : superfunLst.entrySet()){
 				//check signature if already exists
 				if (funList.containsKey(e.getKey())){
 					//check signature, but not here
 					//e.getValue().ts.calculateType(context);
 					//if (!e.getValue().ts.equals(context.mFunctions.containsKey(e.getKey()))){
 					//check whether the 
 					////I don't think we should check here because you can not check two tyscheme equivalence with generic parames
 					if (!e.getValue().ts.sameAs(e.getValue().ts, cur_context)){
 						throw new NoSuchTypeException();
 					}
 					if (funList.get(e.getKey()).funBody instanceof EmptyBody) {
 						funList.get(e.getKey()).funBody = e.getValue().funBody;
 					}
 					//in classes, every function declared has a body
 				}else{//add method if it doesn't already exist
 					super.mFunctions.put(e.getKey(),e.getValue().ts);
 					super.funList.put(e.getKey(), e.getValue());
 				}
 			}
 		}
		else {
 			Helper.ToDo("Please check whether the parrentType is implmented correctly");
 			for (CuType t:superType.parentType){
 				Map<String, CuFun> superfunLst= cur_context.mClasses.get(t.id).funList;
 				for (Map.Entry<String, CuFun> e : superfunLst.entrySet()){
 					//check signature if already exists
 					if (funList.containsKey(e.getKey())){
 						//check signature, but not here
 						//e.getValue().ts.calculateType(context);
 						//if (!e.getValue().ts.equals(context.mFunctions.containsKey(e.getKey()))){
 						//check whether the 
 						//I don't think we should check here because you can not check two tyscheme equivalence with generic parames
 						if (!e.getValue().ts.sameAs(e.getValue().ts, cur_context)){
 							throw new NoSuchTypeException();
 						}
 						//use the first method implementation
 						if (funList.get(e.getKey()).funBody instanceof EmptyBody) {
 							funList.get(e.getKey()).funBody = e.getValue().funBody;
 						}
 					}else{//add method if it doesn't already exist
 						super.mFunctions.put(e.getKey(),e.getValue().ts);
 						super.funList.put(e.getKey(), e.getValue());
 					}
 				}
 			}
 		}
 		
 		//here, we check all the function names, they should not appear in context's mfunctions
 		if (cur_context.mFunctions.containsKey(super.name)) {
 			throw new NoSuchTypeException();
 		}
 		
 		for (String method_name : super.mFunctions.keySet()) {
 			if (cur_context.mFunctions.containsKey(method_name)) {
 				throw new NoSuchTypeException();
 			}
 		}
 		
 		context.updateClass(name, this);
 		cur_context.updateClass(name, this);
 		//also need to update the function context, delta prime in figure 10
 		List<CuType> cur_types = new ArrayList<CuType>();
 		for (String str : super.kindCtxt) {
 			cur_types.add(new VTypePara(str));
 		}
 		CuTypeScheme temp_ts = new TypeScheme(super.kindCtxt, this.fieldTypes , new VClass(super.name, cur_types));
 		context.updateFunction(super.name, temp_ts);
 		cur_context.updateFunction(super.name, temp_ts);
 
 			
 		//now type check each typescheme
 		for (CuFun iter : funList_cpy.values()) {
 			iter.ts.calculateType(cur_context);
 		}
 			
 		//type checks gamma hat
 		for (CuType ct : this.fieldTypes.values()) {
 			ct.calculateType(cur_context);
 		}
 		cur_context.mMutVariables = this.fieldTypes;
 		for (CuStat s :classStatement) {s.calculateType(cur_context);}
 			
 		cur_context.mergeVariable();
 			
 		if (!tau_hat.id.equals(CuVvc.TOP)) {
 			CuExpr temp_expr = new VcExp(tau_hat.id, new ArrayList<CuType>(), this.superArg);
 			CuType retype = temp_expr.calculateType(cur_context);
 			if (!retype.equals(super.superType)) {
 				throw new NoSuchTypeException();
 			}
 		}
 			
 		for (CuFun iter : funList_cpy.values()) {
 			List<String> theta_bar = iter.ts.data_kc;
 			for (String str_iter : theta_bar) {
 				if (super.kindCtxt.contains(str_iter)) {
 					throw new NoSuchTypeException();
 				}
 			}
 		}
 			
 		/* this should be s_hat instead of s
 		for (CuStat s :classStatement) {
 			if (!s.calculateType(context).b
 					||s.calculateType(context).tau.isSubtypeOf(s.calculateType(context).tau)) 
 				throw new NoSuchTypeException();}
 		*/
 		
 		cur_context.mFunctions.putAll(mFunctions);
 		for (CuFun iter : funList_cpy.values()) {
 			CuTypeScheme ts = iter.ts;
 			CuContext temp = new CuContext(cur_context);
 			temp.mKind.addAll(ts.data_kc);
 			temp.mMutVariables = ts.data_tc;
 			HReturn re = iter.funBody.calculateType(temp);
 			if (re.b == false || !re.tau.isSubtypeOf(ts.data_t)) {
 				throw new NoSuchTypeException();
 			}
 		}
 		
 		//check every function has an implemention
 		for (CuFun iter_fun : super.funList.values()) {
 			if (iter_fun.funBody == null || iter_fun.funBody instanceof EmptyBody) {
 				throw new NoSuchTypeException();
 			}
 		}
 		return this;
 	}
 	
 
 	/*
 	@Override public String toString() {
 		return String.format("class %s %s %s extends %s { %s super ( %s ) ; %s }", 
 				clsintf, Helper.printList("<", kc, ">", ","), Helper.printMap("(", tc, ")", ","), superType.toString(), 
 				Helper.printList("", classStatement, "", ""), Helper.printList("(", es, ")", ","), Helper.printList("", fun, "", ""));
 	}
 	*/
 	
 }
 
 class Intf extends CuClass{
 	//Map<String, CuType> fieldTypes=new LinkedHashMap<String,CuType>();
 	
 	//List<CuType> appliedTypePara=new ArrayList<CuType>();
 	//List<CuStat> classStatement = new ArrayList<CuStat>();
 	
 	public Intf (String iname, List<String> kname){
 		super.name = iname;
 		super.kindCtxt = kname;
 		
 		//for printing
 		text = "interface " + name.toString() + " <";
 		for (String s : kindCtxt) {
 			text += " " + s.toString();
 		}
 		text += " > extends";
 	}
 	@Override
 	public void addSuper (CuType tt) throws NoSuchTypeException{
 		super.superType = tt;
 	}
 	@Override public void add(String v, CuTypeScheme ts, CuStat s) {
 		super.funList.put(v,new Function(v,ts,s));
 		super.mFunctions.put(v, ts);
 	}
 	
 	@Override public CuClass calculateType(CuContext context) throws NoSuchTypeException { 
 		//we don't want to modify context
 		CuContext cur_context = new CuContext(context);
 		cur_context.mKind = super.kindCtxt;
 		//we need to type check tau 
 		CuType tau_hat = super.superType.calculateType(cur_context);
 		if (!tau_hat.id.equals(CuVvc.TOP)) {
 			throw new NoSuchTypeException();
 		}
 		
		if (!(superType instanceof VClass) && !(superType instanceof VTypeInter)) {
 			throw new NoSuchTypeException();
 		}
 		//make a copy of current function list, because we only need to type check these functions
 		Map<String, CuFun> funList_cpy = new HashMap<String, CuFun>();
 		funList_cpy.putAll(funList);
 		
 		if (superType instanceof VClass){
 			Map<String, CuFun> superfunLst = cur_context.mClasses.get(superType.id).funList;
 			for (Map.Entry<String, CuFun> e : superfunLst.entrySet()){
 				//check signature if already exists
 				if (funList.containsKey(e.getKey())){
 					//check signature, but not here
 					//e.getValue().ts.calculateType(context);
 					//if (!e.getValue().ts.equals(context.mFunctions.containsKey(e.getKey()))){
 					//I don't think we should check here because you can not check two tyscheme equivalence with generic parames
 					if (!e.getValue().ts.sameAs(e.getValue().ts, cur_context)){
 						throw new NoSuchTypeException();
 					}
 					//if this method doesn't have an implementation, but super interface has an implementation,
 					//grab it
 					if (funList.get(e.getKey()).funBody instanceof EmptyBody) {
 						funList.get(e.getKey()).funBody = e.getValue().funBody;
 					}
 					//in classes, every function declared has a body
 				}else{//add method if it doesn't already exist
 					super.mFunctions.put(e.getKey(),e.getValue().ts);
 					super.funList.put(e.getKey(), e.getValue());
 				}
 			}
 		}
		else {
 			Helper.ToDo("Please check whether the parrentType is implmented correctly");
 			for (CuType t:superType.parentType){
 				Map<String, CuFun> superfunLst= cur_context.mClasses.get(t.id).funList;
 				for (Map.Entry<String, CuFun> e : superfunLst.entrySet()){
 					//check signature if already exists
 					if (funList.containsKey(e.getKey())){
 						//check signature, but not here
 						//e.getValue().ts.calculateType(context);
 						//if (!e.getValue().ts.equals(context.mFunctions.containsKey(e.getKey()))){
 						//check whether the 
 						//I don't think we should check here because you can not check two tyscheme equivalence with generic parames
 						if (!e.getValue().ts.sameAs(e.getValue().ts, cur_context )){
 							throw new NoSuchTypeException();
 						}
 						//use the first implementation of inherited interfaces
 						if (funList.get(e.getKey()).funBody instanceof EmptyBody) {
 							funList.get(e.getKey()).funBody = e.getValue().funBody;
 						}
 					}else{//add method if it doesn't already exist
 						super.mFunctions.put(e.getKey(),e.getValue().ts);
 						super.funList.put(e.getKey(), e.getValue());
 					}
 				}
 			}
 		}
 		
 		//here, we check all the function names, they should not appear in context's mfunctions		
 		for (String method_name : super.mFunctions.keySet()) {
 			if (cur_context.mFunctions.containsKey(method_name)) {
 				throw new NoSuchTypeException();
 			}
 		}
 		
 		context.updateClass(name, this);
 		cur_context.updateClass(name, this);
 			
 		//now type check each typescheme
 		for (CuFun iter : funList_cpy.values()) {
 			iter.ts.calculateType(cur_context);
 		}
 			
 		for (CuFun iter : funList_cpy.values()) {
 			List<String> theta_bar = iter.ts.data_kc;
 			for (String str_iter : theta_bar) {
 				if (super.kindCtxt.contains(str_iter)) {
 					throw new NoSuchTypeException();
 				}
 			}
 		}
 			
 		/* this should be s_hat instead of s
 		for (CuStat s :classStatement) {
 			if (!s.calculateType(context).b
 					||s.calculateType(context).tau.isSubtypeOf(s.calculateType(context).tau)) 
 				throw new NoSuchTypeException();}
 		*/
 		
 		cur_context.mFunctions.putAll(mFunctions);
 		for (CuFun iter : funList_cpy.values()) {
 			//only check those that have function body
 			if (!(iter.funBody instanceof EmptyBody)) {
 				CuTypeScheme ts = iter.ts;
 				CuContext temp = new CuContext(cur_context);
 				temp.mKind.addAll(ts.data_kc);
 				temp.mMutVariables = ts.data_tc;
 				HReturn re = iter.funBody.calculateType(temp);
 				if (re.b == false || !re.tau.isSubtypeOf(ts.data_t)) {
 					throw new NoSuchTypeException();
 				}
 			}
 		}
 		return this;
 	}
 	
 	@Override public boolean isInterface() {return true; }
 }
 
 
 //======Class init=========
 
 class VBoolean extends Cls {
 	Boolean v=false;
 	public VBoolean() {
 		super("Boolean", new ArrayList<String>(), new LinkedHashMap<String, CuType>());
 		//if (val instanceof Boolean) { v=val; }
 		//else { throw new NoSuchTypeException();}
 		//Just one example, we also need to add other methods
 		CuTypeScheme ts;
 		ts = new TypeScheme(new ArrayList<String>(), new LinkedHashMap<String, CuType>(), CuType.bool);
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
     
 	public boolean calculateType() { return v; }
 }
 
 class VInteger extends Cls {
 	Integer v=0;
 	public VInteger() {
 		super("Integer", new ArrayList<String>(), new LinkedHashMap<String, CuType>());
 		//if (val instanceof Integer) { v=val; }
 		//else { throw new NoSuchTypeException();}
 		CuTypeScheme ts;
 		ts = new TypeScheme(new ArrayList<String>(), new LinkedHashMap<String, CuType>(), CuType.integer);
 		super.mFunctions.put("negative", ts);
 		
 		HashMap<String, CuType> arg = new LinkedHashMap<String, CuType>();
 		arg.put("operator", CuType.integer);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.integer);
 		super.mFunctions.put("times", ts);
 		super.mFunctions.put("plus", ts);
 		super.mFunctions.put("minus", ts);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("equals", ts);
 		ts = new TypeScheme(new ArrayList<String>(), arg, new Iter(CuType.integer));
 		super.mFunctions.put("divide", ts);
 		super.mFunctions.put("modulo", ts);		
 		
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("upper", CuType.integer);
 		arg.put("includeLower", CuType.bool);
 		arg.put("includeUpper", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, new Iter(CuType.integer));
 		super.mFunctions.put("through", ts);
 		
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("inclusive", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, new Iter(CuType.integer));
 		super.mFunctions.put("onwards", ts);
 		
 		arg = new LinkedHashMap<String, CuType>();
 		arg.put("that", CuType.integer);
 		arg.put("strict", CuType.bool);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("lessThan", ts);
 	}
 	public VInteger calculateType() { return this; }
 }
 
 class VCharacter extends Cls {
 	Character c;
 	public VCharacter() {
 		super("Character", new ArrayList<String>(), new LinkedHashMap<String, CuType>());
 		//if (val instanceof Character) { c=val; }
 		//else { throw new NoSuchTypeException();}
 		CuTypeScheme ts;
 		ts = new TypeScheme(new ArrayList<String>(), new LinkedHashMap<String, CuType>(), CuType.integer);
 		super.mFunctions.put("unicode", ts);
 		
 		HashMap<String, CuType> arg = new LinkedHashMap<String, CuType>();
 		arg.put("that", CuType.character);
 		ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("equals", ts);
 	}
 	public VCharacter calculateType() { return this; }
 }
 
 class VString extends Cls {
 	String v="";
 	public VString() {
 		super("String", new ArrayList<String>(), new LinkedHashMap<String, CuType>());
 		this.addSuper(new Iter(CuType.character));
 		//if (val instanceof String) { v=val; }
 		//else { throw new NoSuchTypeException();}
 		HashMap<String, CuType> arg = new LinkedHashMap<String, CuType>();
 		arg.put("that", CuType.string);
 		CuTypeScheme ts = new TypeScheme(new ArrayList<String>(), arg, CuType.bool);
 		super.mFunctions.put("equals", ts);
 	}
 
 	public VString calculateType() { return this; }
 }
 
 
 class VIterable extends Cls {
 	List<CuType> v;
 	public VIterable(List<String> kc) {
 		super("Iterable", kc, new LinkedHashMap<String, CuType>());
 		//if (val instanceof List<CuType>) { v=val; }
 		//else { throw new NoSuchTypeException();}
 	}
 	public VIterable calculateType() { return this; }
 }
 
