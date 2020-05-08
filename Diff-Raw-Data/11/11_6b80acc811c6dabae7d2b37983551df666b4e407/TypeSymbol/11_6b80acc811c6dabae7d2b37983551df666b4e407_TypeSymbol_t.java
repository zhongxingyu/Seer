 package cd.ir.symbols;
 
 public class TypeSymbol extends Symbol {
 
 	public TypeSymbol(String name) {
 		super(name);
 	}
 
 	public boolean isReferenceType() {
		return false;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 }
