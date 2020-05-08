 package zinara.ast.expression;
 import zinara.code_generator.*;
 
 import zinara.ast.type.Type;
 import zinara.ast.type.DictType;
 import zinara.exceptions.KeyErrorException;
 import zinara.exceptions.TypeClashException;
 
 public class LValueDict extends LValue {
     private LValue constructor;
     private String identifier;
 
     // requires c.getType() be of DictType
     public LValueDict(LValue c, String i)
 	throws KeyErrorException, TypeClashException {
 	// check if i is in the dictionary
	((DictType)c.getType()).getOrDie(i);
 
 	constructor = c;
 	identifier = i;
     }
 
     public Type getType() throws TypeClashException {
 	if (type != null) return type;
	type = ((DictType)constructor.getType()).get(identifier);
 	return type;
     }
     public String toString() { return constructor + "[" + identifier + "]"; }
 
     public void tox86(Genx86 generate){
     }
 }
