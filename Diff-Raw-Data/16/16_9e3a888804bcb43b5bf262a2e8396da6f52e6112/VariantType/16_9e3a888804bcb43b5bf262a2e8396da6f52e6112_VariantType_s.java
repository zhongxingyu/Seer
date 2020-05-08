 package zinara.ast.type;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Iterator;
 
 import zinara.exceptions.KeyErrorException;
 import zinara.exceptions.InvalidVariantException;
 
 public class VariantType extends Type {
     private HashMap base;   // HashMap<String, Type>
     private HashMap joins;   // HashMap<String, HashMap<String, Type>>
     private HashMap offsets; // HashMap<String, Int>
     private int size;
 
     public VariantType(HashMap b, HashMap js) throws InvalidVariantException {
 	base  = b;
 	joins = js;
 	offsets = new HashMap();
 	String key;
 	Type ct;
 	Iterator it = base.keySet().iterator(), it2;
 	int offset = 0;
 	while(it.hasNext()) {
 	    key = (String)it.next();
 	    ct  = (Type)base.get(key);
 	    offsets.put(key, new Integer(offset));
 	    offset += ct.size();
 	}
 
 	int maxOffset = offset;
 	int newOffset;
 	HashMap tablei;
 	it = joins.keySet().iterator();
 	while(it.hasNext()) {
 	    tablei = (HashMap)joins.get((String)it.next());
 	    it2 = tablei.keySet().iterator();
 	    newOffset = offset;
 	    while(it2.hasNext()) {
 		key = (String)it2.next();
 		ct  = (Type)tablei.get(key);
 		if (offsets.put(key, ct) != null)
 		    throw new InvalidVariantException("Los variantes no pueden tener claves repetidas, la clave " + key + " lo esta");
 		newOffset += ct.size();
 	    }
 	    if (maxOffset < newOffset) maxOffset = newOffset;
 	}
 	size = maxOffset;
     }
 
     public Iterator getIterator() { return base.keySet().iterator(); }
 
     //public int len() { return  }
 
     public int size() {	return size; }
 
    public Type get(String key) { return (Type)base.get(key); }
 
     public Type getOrDie(String key) throws KeyErrorException {
 	Type type = (Type)base.get(key);
 	if (type != null) return type;
 	Iterator it = joins.keySet().iterator(), it2;
 	HashMap tablei;
 	while(it.hasNext()) {
 	    tablei = (HashMap)joins.get((String)it.next());
 	    it2 = tablei.keySet().iterator();
 	    while(it2.hasNext()) {
 		type = (Type)tablei.get((String)it2.next());
 		if (type != null) return type;
 	    }
 	}
 	throw new KeyErrorException("La clave " + key + " no se encuentra definida en el tipo de diccionario " + toString());
     }
 
     public HashMap getVariant(String var) {
 	return (HashMap)joins.get(var);
     }
 
     public Integer getOffsetFor(String key) { return (Integer)offsets.get(key); }
 
     public String toString() {
 	if (!name.equals("")) return "<" + name + ">";
 	String ret = "<{";
 	Set keySet = base.keySet();
 	Iterator it = keySet.iterator(), it2;
 	String currentKey;
 	while (it.hasNext()) {
 	    currentKey = (String)it.next();
 	    ret += currentKey + ": " + (Type)base.get(currentKey) + ", ";
 	}
 	ret = ret.substring(0, ret.length()-2);
 	ret += "} joined to";
 
 	it = joins.keySet().iterator();
 	HashMap tablai;
 	while(it.hasNext()) {
 	    currentKey = (String)it.next();
 	    ret += " " + currentKey + "{";
 	    tablai = (HashMap)joins.get(currentKey);
 	    it2 = tablai.keySet().iterator();
 	    while(it2.hasNext()) {
 		currentKey = (String)it2.next();
 		ret += currentKey + ": " + (Type)tablai.get(currentKey) + ", ";
 	    }
 	    ret = ret.substring(0, ret.length()-2);
 	    ret += "}";
 	}
 	return ret + ">";
     }
 
     public Type getType() { return this; }
 
     public boolean equals(Type other) {
 	return false; // No se puede comparar nada contra un variant, pero si contra sus elementos.
 // 	if (!(other instanceof VariantType)) return false;
 // 	VariantType otherVariant = (VariantType)other;
 // 	if (!otherVariant.getName().equals("") && !name.equals("")) return  otherVariant.getName().equals(name);
 // 	// Checks internally
 // 	if (size() != otherVariant.size()) return false;
 // 	Iterator it1 = getIterator();
 // 	Iterator it2 = otherVariant.getIterator();
 // 	String ckey1, ckey2;
 // 	while(it1.hasNext()) {
 // 	    ckey1 = (String)it1.next();
 // 	    ckey2 = (String)it2.next();
 // 	    if (ckey1 != ckey2 || !get(ckey1).equals(((Type)otherVariant.get(ckey2)).getType()))
 // 		return false;
 // 	}
 // 	return true;
     }
 
     public void setName(String n) { name = n; }
     public String getName() { return name; }
 }
 
