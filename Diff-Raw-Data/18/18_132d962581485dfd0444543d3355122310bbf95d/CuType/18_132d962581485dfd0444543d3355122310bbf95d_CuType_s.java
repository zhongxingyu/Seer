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
 	protected static CuType iterable(ArrayList<CuType> arg) {return new VClass("Iterable", arg);}
 	protected List<CuType> parentType = new ArrayList<CuType>();
 	protected String id;
 	protected String text = "";
 	protected Map<CuType, CuType> map = new LinkedHashMap<CuType, CuType>();// typeParameter->non-generic type arguments
 	protected CuType type = bottom; // for Iterable<>
 
 	CuType(){ changeParent(top); }
 
 	/** methods in its subtypes */
 	public void changeParents(List<CuType> t) {}
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
 		throw new NoSuchTypeException();
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
 		List<CuType> parent1 = superTypeList(t1);
 		List<CuType> parent2 = superTypeList(t2);
 		for (CuType p : parent1) {
 			if (parent2.contains(p)) return p;
 		}
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
 		super.text=super.id+ " "+ Helper.printList("<", args, ">", ",");
 	}
 	@Override public CuType calculateType(CuContext context) {
 		// type in argument must be type parameter, mapped args must be in scope
 		for (Entry<CuType, CuType> m: map.entrySet()) {
 			if (!m.getKey().isTypePara() || !context.getKindList().contains(m.getKey().id)) 
 				if (!m.getValue().isBottom() && !context.mVariables.containsKey(m.getValue())) {
 					throw new NoSuchTypeException(); 
 				}	
 		}
 		// check if class or interface
 		CuClass c = context.mClasses.get(id);
 		if (c == null) throw new NoSuchTypeException(); 
 		if (c.isInterface()) return CuType.top;
 		return this;
 	}
 	/* instantiate this class, strict plug in */
 	@Override public Map<CuType, CuType> plugIn(List<CuType> t) {
 		if(map.size()==0) return this.map;
 		if(map.size() != t.size()) {throw new NoSuchTypeException();}
 		int i = 0;
 		for (Entry<CuType, CuType> k : map.entrySet()) {
 			if(t.get(i).isTypePara()) {
 				throw new NoSuchTypeException();
 			}
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
 	//TODO: change this method
 	@Override public boolean isClassOrInterface() {return true;}
 	@Override public boolean isSubtypeOf(CuType that) {
 		if (this.equals(that)) return true;
 		if (this.isBottom()) return true;
 		for (CuType p : this.parentType) {
 			if (this.isClassOrInterface() && p.isClassOrInterface()) {
 				p.plugIn(((VClass) this).map);
 			}
 			if (p.isSubtypeOf(that)) return true;
 		}
 		return false;
 	}
 	@Override public boolean equals(CuType that) {
 		if(that.isClassOrInterface()) {
 			VClass t = (VClass) that;
 			Set<CuType> tp1 = this.map.keySet();
 			Set<CuType> tp2 = t.map.keySet();
 			return super.id.equals(t.id) && tp1.equals(tp2);
 		}
 		return false;
 	}
 	private boolean equalsInstance(CuType t) {
 		return equals(t) && map.equals(((VClass)t).map); // for generic plug in
 	}
 	//added by Yinglei
 	@Override public boolean isIterable() {return super.id.equals("Iterable");}
 	@Override public boolean isString() {return super.id.equals("String");}
 	@Override public boolean isCharacter() {return super.id.equals("Character");}
 	@Override public boolean isInteger() {return super.id.equals("Integer");}
 	@Override public boolean isBoolean() {return super.id.equals("Boolean");}
 	@Override public CuType getArgument() throws NoSuchTypeException {
 		return type;
 	}
 }
 
 
 class VTypeInter extends CuType {
 	List<CuType> parents = new ArrayList<CuType>();
 	public VTypeInter(CuType t1){
 		parents.add(t1);
 		super.text=t1.toString();
 	}
 	@Override public void add(CuType t) {
 		parents.add(t);
 		super.text += " \u222A "+t.toString();
 	}
 	@Override public CuType calculateType(CuContext context) throws NoSuchTypeException {
 		/* type checking */
 		HashSet<CuType> pAll = new HashSet<CuType>(); 
 		HashSet<String> vAll = new HashSet<String>(); 
 		for(int i = 0; i < parents.size(); i++) {
 			CuType t = parents.get(i);
 			// A & B & C..., only the first could be a class
 			//Yinglei asks: why do we have this checking?
 			if ((i > 0) && !t.calculateType(context).isTop()) throw new NoSuchTypeException();
 			// all parents are distinct except top
 			List<CuType> temp = t.parentType;
 			temp.remove(CuType.top);
 			if(!pAll.addAll(temp)) throw new NoSuchTypeException();
 			// all method names are distinct
 			Helper.ToDo("Please check the addAll method, if there are existing keys, will it return false?");
 			Set<String> temp2 = context.mClasses.get(t.id).mFunctions.keySet();
 			if(!vAll.addAll(temp2)) throw new NoSuchTypeException();
 		}
 		return parents.get(0).calculateType(context);
 	}
 	@Override public boolean isIntersect() {return true;}
 	@Override public void changeParents(List<CuType> t) {
 		super.parentType = parents;
 	}
 	@Override public boolean equals(CuType that) {
 		if (that.isIntersect()) {
 			VTypeInter t = (VTypeInter) that;
 			return parents.containsAll(t.parents) && t.parents.containsAll(parents);
 		}
 		return false;
 	}
 }
 
 
 class VTypePara extends CuType {
 	public VTypePara(String s){
 		super.id = s;
 		super.text = s;
 	}
 	@Override public boolean isTypePara() {return true;}
 	@Override public boolean equals(CuType that) {
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
 	public Iter(CuType arg) {
 		super(CuVvc.ITERABLE, new ArrayList<CuType> ()); // id is "Iterable"
 		System.out.println("in iter begin");
 		super.type = arg;
 		super.text=super.id+ " <" + arg.toString()+">";
 		// set its parent types
 		List<CuType> parents = new ArrayList<CuType>();
 		for (CuType t : arg.parentType) {
 			System.out.println(t.id);	
 			//Helper.ToDo("Yiglei changed this to t, remember to change related things");
 			if (!t.isTop()) parents.add(new Iter(t));
 		}
 		if (!parents.isEmpty()) super.changeParents(parents);
 		Helper.ToDo("type check Iterable?");
 		System.out.println("in iter end");
 	}
 	@Override public boolean isIterable() {return true;}
 	@Override public boolean equals(CuType that) {
 		return that.isIterable() && ((VClass)that).type.equals(this.type);
 	}
 	@Override public CuType getArgument() throws NoSuchTypeException {
 		return type;
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
 	}
 	@Override public CuType calculateType(CuContext context) { return this;}
 	@Override public boolean isTop() {return true;}
 	@Override public boolean equals(CuType that) { return that.isTop();}
 }
 class Bottom extends CuType {
 	public Bottom(){
 		super.id = CuVvc.BOTTOM;
 		super.text= "Nothing";
 	}
 	@Override public boolean isBottom() {return true;}
 	@Override public boolean isSubtypeOf(CuType t) { System.out.println("in class Bottom"); return true;}
 	@Override public boolean equals(CuType that) { return that.isBottom();}
 }
 
