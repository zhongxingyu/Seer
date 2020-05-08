 package cd.ir.symbols;
 
 public class TypeSymbol extends Symbol {
 
 	public TypeSymbol(String name) {
 		super(name);
 	}
 
 	public boolean isReferenceType() {
		throw new IllegalStateException("unknown whether reference type or not");
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 }
