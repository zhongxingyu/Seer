 package descent.internal.compiler.parser;
 
 public class DsymbolTable {
 	
 	private HashtableOfCharArrayAndObject map = new HashtableOfCharArrayAndObject();
 	
 	public Dsymbol insert(Dsymbol dsymbol) {
 		if (map.containsKey(dsymbol.ident.ident)) {
 			return null;
 		}
 		map.put(dsymbol.ident.ident, dsymbol);
 		return dsymbol;
 	}
 
 	public Dsymbol lookup(IdentifierExp ident) {
 		return (Dsymbol) map.get(ident.ident);
 	}
 	
 	public Dsymbol lookup(char[] ident) {
 		return (Dsymbol) map.get(ident);
 	}
 
 }
