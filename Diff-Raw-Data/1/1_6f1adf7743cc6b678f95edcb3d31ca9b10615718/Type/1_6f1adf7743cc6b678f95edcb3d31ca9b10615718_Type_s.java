 package zinara.ast.type;
 
 import java.util.Iterator;
 
 public abstract class Type {
     public boolean equals(Type other) {
 	/*
 	  Basic Types
 	*/
 	if ((this.getType() instanceof IntType && other.getType() instanceof IntType) || 
 	    (this.getType() instanceof FloatType && other.getType() instanceof FloatType) ||
 	    (this.getType() instanceof CharType && other.getType() instanceof CharType) ||
 	    (this.getType() instanceof BoolType && other.getType() instanceof BoolType) ||
 	    (this.getType() instanceof StringType && other.getType() instanceof StringType))
 	    return true;
 	else {
 	    /*
 	      Composed Types
 	     */
 	    // Lists
 	    if (this.getType() instanceof ListType && other.getType() instanceof ListType) {
 		ListType type1 = (ListType)this.getType();
 		ListType type2 = (ListType)other.getType();
 		if (type1.getInsideType() == null && type2.getInsideType() == null) return true;
 		return type1.getInsideType().equals(type2.getInsideType());
 	    }
 
 	    // Tuples
 	    if (this.getType() instanceof TupleType && other.getType() instanceof TupleType) {
 		if (((TupleType)this).size() != ((TupleType)other).size()) return false;
 		for (int i = 0; i < ((TupleType)this).size(); i++)
 		    if (!((TupleType)this).get(i).equals(((TupleType)other).get(i))) return false;
 		return true;
 	    }
 
 	    // Dicts
 	    if (this.getType() instanceof DictType && other.getType() instanceof DictType) {
 		DictType type1 = (DictType)this.getType();
 		DictType type2 = (DictType)other.getType();
 		if (type1.getName() != "" && type2.getName() != "")
 		    if (type1.getName() != type2.getName()) return true;
 		    else return false;
 		// Checks internally
 		if (type1.size() != type2.size()) return false;
 		Iterator it1 = type1.getIterator();
 		Iterator it2 = type2.getIterator();
 		String ckey1, ckey2;
 		while(it1.hasNext()) {
 		    ckey1 = (String)it1.next();
 		    ckey2 = (String)it2.next();
 		    if (ckey1 != ckey2 || !type1.get(ckey1).equals(type2.get(ckey2)))
 			return false;
 		}
 		return true;
 	    }
 	    return false;
 	}
     }
 
     public abstract Type getType();
     public abstract String toString();
 }
