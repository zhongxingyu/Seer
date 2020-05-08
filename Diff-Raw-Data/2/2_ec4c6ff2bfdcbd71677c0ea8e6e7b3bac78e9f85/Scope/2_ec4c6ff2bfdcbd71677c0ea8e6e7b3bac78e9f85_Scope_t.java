 package titocc.compiler;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Represents a scope (aka namespace) that contains symbols declared within that
  * scope.
  */
 public class Scope
 {
 	private Scope parent;
 	private Map<String, Symbol> symbols = new HashMap<String, Symbol>();
 	private Set<String> globallyUniqueNames;
 	private String globalNamePrefix;
 
 	/**
 	 * Constructs a new Scope.
 	 *
 	 * @param parent Parent scope. Null if this is the global scope.
 	 */
 	public Scope(Scope parent, String globalNamePrefix)
 	{
 		this.parent = parent;
 		this.globalNamePrefix = globalNamePrefix;
 		if (parent == null)
 			globallyUniqueNames = new HashSet<String>();
 		else
 			globallyUniqueNames = parent.globallyUniqueNames;
 	}
 
 	/**
 	 * Tests whether this is the global scope.
 	 *
 	 * @return True if global scope.
 	 */
 	public boolean isGlobal()
 	{
 		return parent == null;
 	}
 
 	/**
 	 * Returns the parent scope.
 	 *
 	 * @return parent scope.
 	 */
 	public Scope getParent()
 	{
 		return parent;
 	}
 
 	/**
 	 * Finds a symbol (e.g. a variable or a function) defined in this scope or
 	 * any of its parent scopes.
 	 *
 	 * @param name Identifier of the object.
 	 * @return Searched symbol or null if none was found.
 	 */
 	public Symbol find(String name)
 	{
 		Symbol sym = symbols.get(name);
 		if (sym == null && parent != null)
			sym = parent.find(name);
 		return sym;
 	}
 
 	/**
 	 * Adds a new symbol to the scope if no symbols with the same name exist
 	 * already.
 	 *
 	 * @param symbol Symbol to be added.
 	 * @return False if symbol was not added (already exists).
 	 */
 	public boolean add(Symbol symbol)
 	{
 		if (symbols.containsKey(symbol.getName()))
 			return false;
 		symbols.put(symbol.getName(), symbol);
 		return true;
 	}
 
 	/**
 	 * Generates a globally unique name by first adding the prefixes of the
 	 * scope and all its surrounding scopes. Then tries number suffixes starting
 	 * from 2 until the name is unique.
 	 *
 	 * @param Local name.
 	 * @return a globally unique name.
 	 */
 	public String makeGloballyUniqueName(String name)
 	{
 		String uniqueNameBase = generateGlobalNamePrefix() + name;
 		String uniqueName = uniqueNameBase;
 		for (int i = 2; !globallyUniqueNames.add(uniqueName.toLowerCase()); ++i)
 			uniqueName = uniqueNameBase + i;
 		return uniqueName;
 	}
 
 	private String generateGlobalNamePrefix()
 	{
 		if (parent != null)
 			return parent.generateGlobalNamePrefix() + globalNamePrefix;
 		else
 			return globalNamePrefix;
 	}
 }
