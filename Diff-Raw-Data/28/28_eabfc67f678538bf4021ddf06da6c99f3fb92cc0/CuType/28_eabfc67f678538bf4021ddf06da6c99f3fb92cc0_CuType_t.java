 import java.util.*;
 import java.util.Map.Entry;
 
 /** class declaration add type */
 public abstract class CuType {
 	protected static CuType top = new Top();
 	protected static CuType bottom = new Bottom();
 	protected static CuType bool = new VClass("Boolean", new ArrayList<CuType>());
 	protected static CuType integer = new VClass("Integer", new ArrayList<CuType>());
 	protected static CuType character = new VClass("Character", new ArrayList<CuType>());
 	protected static CuType string = new VClass("String", new ArrayList<CuType>());
 	protected static CuType iterable(ArrayList<CuType> arg) {return new Iter(arg);}
 	protected List<CuType> parentType = new ArrayList<CuType>();
 	List<CuType> parents = new ArrayList<CuType>();
 	protected String id;
 	protected String text = "";
 	protected Map<CuType, CuType> map = new LinkedHashMap<CuType, CuType>();// typeParameter->non-generic type arguments
 	protected CuType type = CuType.bottom; // for Iterable<E>
 	CuType(){ changeParent(top); }
 
 	/** methods in its subtypes */
 	public void changeParents(List<CuType> t) {parentType = t;}
 	public void changeParent(CuType t) {
 		parentType = new ArrayList<CuType>();
 		parentType.add(t);
 	} // append
 	public boolean isTop() {return false;}
 	public boolean isBottom() {return false;}
 	public boolean isTypePara() {return false;}
 	public boolean isClassOrInterface() {return false;}
 	public boolean isIntersect() { return false;}
 	public boolean isIterable() {return false;}
 	public boolean isString() {return false;}
 	public boolean isCharacter() {return false;}
 	public boolean isInteger() {return false;}
 	public boolean isBoolean() {return false;}
 
 	public void add(CuType t) {}
 	public CuType getArgument() throws NoSuchTypeException {
 		// Iterable<E>, map only has one key
 		for (CuType t : this.map.keySet()) {
 			return t;
 		}
 		//throw new NoSuchTypeException();
 		return null;
 	}
 	public Map<CuType, CuType> plugIn(List<CuType> t) { return map;}
 	public Map<CuType, CuType> plugIn(Map<CuType, CuType> t) {return map;}
 
 	// Hierarchy of types
 	public boolean equals(Object that) { return equals((CuType)that); }
 	abstract public boolean equals(CuType that);
 	public boolean isSubtypeOf(CuType that) {
 		if (this.equals(that)) return true;
 		for (CuType p : this.parentType) {
 			if (p.isSubtypeOf(that)) return true;
 		}
 		return false;
 	}
 	public static CuType commonParent(CuType t1, CuType t2) {
 		if (t1 == null || t1.isBottom()) return t2;
 		if (t2 == null || t2.isBottom() ) return t1;
 		System.out.println("t1 is " + t1.toString());
 		System.out.println("t2 is " + t2.toString());
 		if(t1.isIterable() && t2.isIterable())
 		{
 			System.out.println("common parent of " + t1.getArgument() + " " + t2.getArgument());
 			return new Iter(CuType.commonParent(t1.getArgument(), t2.getArgument()));
 		}
 		List<CuType> parent1 = superTypeList(t1);
 		List<CuType> parent2 = superTypeList(t2);
 		System.out.println("parent 1 is ");
 		for (CuType p : parent1) { 
 			System.out.println(p.toString());
 		}
 		System.out.println("parent 2 is ");
 		for (CuType p : parent2) { 
 			System.out.println(p.toString());
 		}
 		for (CuType p : parent1) {
 			System.out.println("p is " + p.toString());
 			if (parent2.contains(p)) { System.out.println(	"returned p which is " + p.toString()); return p;}
 		}
 		System.out.println("returned top");
 		return top;
 	}
 	public CuType calculateType(CuContext context) throws NoSuchTypeException { return null;}
 	// find all the super types of n, including itself
 	public static List<CuType> superTypeList(CuType n) {
 		Queue<CuType> l = new LinkedList<CuType>();
 		l.add(n);
 		List<CuType> p = new ArrayList<CuType>();
 		while (!l.isEmpty()) {
 			CuType t = l.poll();
 			p.add(t);
 			for (CuType x : t.parentType) {
 				if (!x.isTop() && !p.contains(x)) {
 					l.add(x);
 				}
 			}
 		}
 		if (n.id.equals("String"))
 			p.add(new Iter(character));
 		if (!p.contains(top)) p.add(top);
 		return p;
 	}
 
 	@Override public String toString() { return text;}
 }
 /** determine whether an interface: isInterface() == true
  * determine is a class but not interface isClassOrInterface() && !isInterface()
  * determine is a class or an interface: isClassOrInterface()
  */
 class VClass extends CuType {
 	public VClass(String s, List<CuType> args){
 		super.id = s;
 		for (CuType t : args) {
 			map.put(t, CuType.bottom); // type parameter is mapped to bottom initially
 		}
 //		if (s.equals("String")) parentType.add(new Iter(CuType.character)); // String<> extends Iterable<Character<>>
 		super.text=super.id+ " "+ Helper.printList("<", args, ">", ",");
 		// TODO: merge to Iter()
 		if (super.id.equals("Iterable")) {
 			if (args.size()>1) throw new NoSuchTypeException(); // Iterable<E>, E cannot have 2 or more elements
 		}
 	}
 	@Override public CuType calculateType(CuContext context) {
 		// check if class or interface
 		CuClass c = context.mClasses.get(id);
 		if (c == null) throw new NoSuchTypeException(); 
 		// get its parent types, for isSubtypeOf()
 		super.changeParent(context.mClasses.get(id).superType);
 		// TODO: mapping and plugin checking
 /*		// type in argument must be type parameter, mapped args must be in scope
 		for (Entry<CuType, CuType> m: map.entrySet()) {
 
 			if (!m.getKey().isTypePara() || !context.getKindList().contains(m.getKey().id)) 
 				if (!m.getValue().isBottom() && !context.mVariables.containsKey(m.getValue())) {
 					throw new NoSuchTypeException(); 
 				}	
 		}
 */
 		//TODO: check size
 		int t1 = c.kindCtxt.size();
 		int t2 = map.keySet().size();
 		if (t1 != t2) {
 			throw new NoSuchTypeException();
 		}
 		for (CuType iter : map.keySet()) {
 			iter.calculateType(context);
 		}
 		//build superType
 		if (c.superType instanceof VClass) {
 			super.changeParent(c.superType);
 		}
 		else if (c.superType instanceof VTypeInter) {
 			super.changeParents(parents);
 		}
 		else {
 			if (!c.superType.isTop()) {
 				throw new NoSuchTypeException();
 			}
 		}
 		
 		//special process for iterable
 		if(id.equals("Iterable")) {
 			List <CuType> iter_parrents = new ArrayList<CuType>();
 			for (CuType t : type.parentType) {
 				//System.out.println(t.id);	
 				if (!t.isTop()) iter_parrents.add(new Iter(t));
 			}
 			parentType.addAll(iter_parrents);
 		}
 		
 		if (c.isInterface()) return CuType.top;
 		return this;
 	}
 	/* instantiate this class, strict plug in */
 	@Override public Map<CuType, CuType> plugIn(List<CuType> t) {
 		if(map.size()==0) return this.map;
 		if(map.size() != t.size()) {throw new NoSuchTypeException();}
 		int i = 0;
 		for (Entry<CuType, CuType> k : map.entrySet()) {
 			//commented out by Yinglei, can be TypeParam
 			/*if(t.get(i).isTypePara()) {
 				throw new NoSuchTypeException();
 			}*/
 			k.setValue(t.get(i));
 			i++;
 		}
 		return this.map;
 	}
 	@Override public Map<CuType, CuType> plugIn(Map<CuType, CuType> t) {
 		if(map.size()==0) return this.map;
 		for (Entry<CuType, CuType> p : t.entrySet()) {
 			CuType k = p.getKey();
 			if (map.containsKey(k)) {
 				map.put(k, p.getValue());// only plugin valid keys
 			}
 		}
 		return this.map;
 	}
 	@Override public boolean isClassOrInterface() {return true;}
 	@Override public boolean isSubtypeOf(CuType that) {
 		if (this.equals(that)) return true;
 		if (this.isBottom()) return true;
 		if (this.isIterable() && that.isIterable())
 		{
 			for (CuType p : this.type.parentType) {
 				if (this.type.isClassOrInterface() && p.type.isClassOrInterface()) {
 					p.type.plugIn(((VClass) this.type).map);
 				}
 				if (p.type.isSubtypeOf(that.type) || p.type.equals(that.type)) return true;
 			}
 		}
 		else
 		{
 			for (CuType p : this.parentType) {
 				if (this.isClassOrInterface() && p.isClassOrInterface()) {
 					p.plugIn(((VClass) this).map);
 				}	
 				if (p.isSubtypeOf(that) || p.equals(that)) return true;
 			}
 		}
 		return false;
 	}
 	@Override public boolean equals(CuType that) {
 		if(that.isClassOrInterface()) {
 			VClass t = (VClass) that;
 			Set<CuType> tp1 = this.map.keySet();
 			Set<CuType> tp2 = t.map.keySet();
 Helper.P(String.format("%s is:", tp1));
 for (CuType x : tp1) {
 	for(CuType y : tp2) {
 		Helper.P(String.format("%s in tp1 = %s in tp2 is %b", x, y, x.equals(y)));
 	}
 	}
 Helper.P(String.format("%s equals %s is %b, %s = %s is %b", super.id, t.id, super.id.equals(t.id), tp1, tp2, tp1.equals(tp2)));
 			return super.id.equals(t.id) && Helper.equals(tp1, tp2);
 		}
 		return false;
 	}
 	private boolean equalsInstance(CuType t) {
 		return equals(t) && map.equals(((VClass)t).map); // for generic plug in
 	}
 	//added by Yinglei
 	@Override public boolean isIterable() {return (super.id.equals("Iterable") || super.id.equals("String"));}
 	@Override public boolean isString() {return super.id.equals("String");}
 	@Override public boolean isCharacter() {return super.id.equals("Character");}
 	@Override public boolean isInteger() {return super.id.equals("Integer");}
 	@Override public boolean isBoolean() {return super.id.equals("Boolean");}
 }
 
 class VTypeInter extends CuType {
 	public VTypeInter(CuType t1){
 		super.parentType = new ArrayList<CuType>();
 		parentType.add(t1);
 		super.text=t1.toString();
 	}
 	@Override public void add(CuType t) {
 		parentType.add(t);
 		super.text += " \u222A "+t.toString();
 	}
 	@Override public CuType calculateType(CuContext context) throws NoSuchTypeException {
 		/* type checking */
 		HashSet<CuType> pAll = new HashSet<CuType>(); 
 		HashSet<String> vAll = new HashSet<String>(); 
 		for(int i = 0; i < parentType.size(); i++) {
 			CuType t = parentType.get(i);
			if (!t.isTop() && (!t.isClassOrInterface())) {
				throw new NoSuchTypeException();
			}
			CuType tt = t.calculateType(context);
 			// A & B & C..., only the first could be a class
			if ((i > 0) && !tt.isTop()) throw new NoSuchTypeException();
 			// all parents are distinct except top
 			List<CuType> temp = t.parentType;
 			temp.remove(CuType.top);
 			if (temp.size() > 0)	
 				if(!pAll.addAll(temp)) throw new NoSuchTypeException();
 			// all method names are distinct
 			Set<String> temp2 = context.mClasses.get(t.id).mFunctions.keySet();
 			if(temp2.size()>0)
 				if(!vAll.addAll(temp2)) throw new NoSuchTypeException();
 		}
 		return parentType.get(0).calculateType(context);
 	}
 	@Override public boolean isIntersect() {return true;}
 /*	@Override public void changeParents(List<CuType> t) {
 		super.parentType = t;
 	}
 */
 	@Override public boolean equals(CuType that) {
 		if (that.isIntersect()) {
 			VTypeInter t = (VTypeInter) that;
 			return parentType.containsAll(t.parentType) && t.parentType.containsAll(parentType);
 		}
 		return false;
 	}
 }
 
 
 class VTypePara extends CuType {
 	public VTypePara(String s){
 		System.out.println( "in VTypePara, id is " + s);
 		super.id = s;
 		super.text = s;
 	}
 	@Override public boolean isTypePara() {return true;}
 	@Override public boolean equals(CuType that) {
 Helper.P(String.format("TypePara is %b, %s=%s is %b", that.isTypePara(), super.id, that.id, super.id.equals(that.id)));
 		return that.isTypePara() && super.id.equals(that.id);
 	}
 	public CuType calculateType(CuContext context) throws NoSuchTypeException {
 		if (!context.hasVTypePara(super.id)){
 			throw new NoSuchTypeException();
 		}
 		return null;
 	}
 }
 
 class Iter extends VClass {
 	public Iter(CuType args) {
 		super(CuVvc.ITERABLE, new ArrayList<CuType> ()); // id is "Iterable"
 		CuType arg = (args == null) ? CuType.bottom : args;
 		init(arg);
 	}
 	public Iter(List<CuType> args) {
 		super(CuVvc.ITERABLE, args); // id is "Iterable"
 		CuType arg = (args.isEmpty()) ? CuType.bottom : args.get(0);
 		init(arg);
 	}
 	
 	private void init(CuType arg) {// arg is not null
 		map.put(arg, CuType.bottom); // type parameter is mapped to bottom initially
 		super.text=super.id+ " <" + arg.toString()+">";
 		// set its parent types
 		List<CuType> parents = new ArrayList<CuType>();
 		for (CuType t : arg.parentType) {
 			//System.out.println(t.id);	
 			if (!t.isTop()) parents.add(new Iter(t));
 		}
 		if (!parents.isEmpty()) super.changeParents(parents);
 		type = this.getArgument();
 		//System.out.println("in iter end");
 	}
 	@Override public boolean isIterable() {return true;}
 	@Override public boolean equals(CuType that) {
 		return that.isIterable() && this.type.equals(((VClass)that).type);
 	}
 	@Override public CuType calculateType(CuContext context) {
 		// check if class or interface
 		CuClass c = context.mClasses.get(id);
 		if (c == null) throw new NoSuchTypeException(); 
 		// TODO: mapping and plugin checking
 /*		// type in argument must be type parameter, mapped args must be in scope
 		for (Entry<CuType, CuType> m: map.entrySet()) {
 			if (!m.getKey().isTypePara() || !context.getKindList().contains(m.getKey().id)) 
 				if (!m.getValue().isBottom() && !context.mVariables.containsKey(m.getValue())) {
 					throw new NoSuchTypeException(); 
 				}	
 		}
 */
 		return this;
 	}
 }
 
 /*
 class Bool extends VClass {
 	public Bool() {
 		super(CuVvc.BOOLEAN, new ArrayList<CuType> (), false);
 	}
 	@Override public boolean isBoolean() {return true;}
 }
 class Int extends VClass {
 	public Int() {
 		super(CuVvc.INTEGER, new ArrayList<CuType> (), false);
 	}
 	@Override public boolean isInteger() {return true;}
 }
 class Char extends VClass {
 	public Char() {
 		super(CuVvc.CHARACTER, new ArrayList<CuType> (), false);
 	}
 	@Override public boolean isCharacter() {return true;}
 }
 class Str extends VClass {
 	public Str() {
 		super(CuVvc.STRING, new ArrayList<CuType> (), false);
 	}
 	@Override public boolean isString() {return true;}
 }
 */
 
 
 class Top extends CuType{
 	Top() {
 		super.id = CuVvc.TOP;
 		super.text = "Thing";
 		super.type =  new Bottom();
 	}
 	@Override public CuType calculateType(CuContext context) { return this;}
 	@Override public boolean isTop() {return true;}
 	@Override public boolean equals(CuType that) { return that.isTop();}
 	@Override public boolean isSubtypeOf(CuType t) { 
 		if (t.isTop()) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 }
 class Bottom extends CuType {
 	public Bottom(){
 		super.id = CuVvc.BOTTOM;
 		super.text= "Nothing";
 	}
 	@Override public CuType calculateType(CuContext context) { return this;}
 	@Override public boolean isBottom() {return true;}
 	@Override public boolean isSubtypeOf(CuType t) { System.out.println("in class Bottom"); return true;}
 	@Override public boolean equals(CuType that) { return that.isBottom();}
 }
 
