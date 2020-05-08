 package crux;
 
 import java.util.Map;
 import java.util.LinkedHashMap;
 
 /**
  * A table of symbols.
  */
 @SuppressWarnings("unchecked") // Needed to suppress warning from whoIsMyFather. I know what I'm doing here.
 public class SymbolTable extends AbstractSymbolTable {
 	/* Functions that are available as a part of the crux language. */
     public static final String[] PREDEF_FUNCS = { 
         "readInt",
         "readFloat",
         "printBool",
         "printInt",
         "printFloat",
         "println"
     };
 
     /* The parent scope of this table. */
     private AbstractSymbolTable parent;
 
     /* The known symbols and names in this scope. */
     private Map<String, Symbol> table;
 
     /**
      * Construct a new symbol table.
      */
     public SymbolTable() {
         this(AbstractSymbolTable.NULL);
     }
 
     /**
      * Construct a new symbol table with a parent.
      * @param parent The parent of this table.
      */
     public SymbolTable(AbstractSymbolTable parent) {
 		super(parent.depth + 1);
         this.parent = parent;
         table = new LinkedHashMap<String, Symbol>();
     }
 
     /**
      * Look up a symbol name in the table.
      * @param name The symbol to look up.
      * @return The found symbol.
      * @throws SymbolNotFoundError when the symbols was not found.
      */
     public Symbol lookup(String name) throws SymbolNotFoundError {
 		Symbol target = get(name);
 		if (target != null) {
 			return target;
 		} else {
 			throw new SymbolNotFoundError(name);
 		}
     }
 
     /**
      * Get a symbols with a given name.
      * @param name The symbol name.
      * @return The symbol.
      */
     private Symbol get(String name) {
         Symbol target = table.get(name);
         if (target == null) {
         	target = parent.lookup(name);
         } 
 		return target;
     }
 
     /**
      * Insert a new name in the table.
      * @param name The new symol name to insert.
      * @return The new symbol inserted.
      * @throws RedeclarationError if the symbol already existed.
      */
     public Symbol insert(String name) throws RedeclarationError {
         Symbol symbol = table.get(name);
         if (symbol != null ) {
         	throw new RedeclarationError(symbol);
        } else {
			return table.put(name, new Symbol(name));
        }
     }
 
     /**
      * Get a string representation of the symbol table.
      * @return A string representation of all the scopes.
      */
     public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append(parent.toString());
 
         String indent = new String();
         for (int i = 0; i < depth; i++) {
         	indent += "  ";
         }
 
         for (Symbol s : table.values()) {
             sb.append(indent + s.toString() + "\n");
         }
         return sb.toString();
     }
 
     /**
      * Create a new child symbol table from this table.
      * @return A child symbol table.
      */
     public SymbolTable mutate() {
     	return new SymbolTable(this);
     }
 
     /**
      * Get the parent of this table.
      * Uses helper method because the root table's parent (NULL) should not be
      * exposed so we have to determine this in a nice way.
      */
     public SymbolTable getParent() {
 		return parent.whoIsMyFather(this);
     }
 
     /**
      * The father of a child is its parent for normal tables.
      */
     protected SymbolTable whoIsMyFather(AbstractSymbolTable child) {
     	return this; // Luke, I'm your father.
     }
 }
 
 /**
  * An error representing the act of not finding a symbol in the table.
  */
 class SymbolNotFoundError extends Error {
     private static final long serialVersionUID = 1L;
 
     /* The name of the not found symol. */
     private String name;
 
     /**
      * Construct a new error for a give symbol name.
      * @param The errornous symbol name.
      */
     SymbolNotFoundError(String name) {
         this.name = name;
     }
 
     /**
      * Get the name of the errornous symbol.
      * @return The symbol name.
      */
     public String name() {
         return name;
     }
 }
 
 /**
  * An error representing the act redeclaring an symbol.
  */
 class RedeclarationError extends Error {
     private static final long serialVersionUID = 1L;
 
     /**
      * Construct a new error for the given symbol.
      * @parmam sym The redeclared symbol.
      */
     public RedeclarationError(Symbol sym) {
         super("Symbol " + sym + " being redeclared.");
     }
 }
