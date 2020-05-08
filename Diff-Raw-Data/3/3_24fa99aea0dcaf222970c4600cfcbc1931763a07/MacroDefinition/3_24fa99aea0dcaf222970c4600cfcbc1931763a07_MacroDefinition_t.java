 package lambda.macro;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.TreeMap;
 
 import lambda.ast.Lambda;
 
 public class MacroDefinition
 {
 	private Map<String, Lambda> macros = new TreeMap<String, Lambda>();
 
 	public void clearMacros()
 	{
 		macros.clear();
 	}
 
 	public void defineMacro(String name, Lambda lambda)
 	{
 		macros.put(name, lambda);
 	}
 
 	public Lambda expandMacro(String name)
 	{
		Lambda l = macros.get(name);
		return l != null ? l.deepCopy() : null;
 	}
 
 	public Map<String, Lambda> getDefinedMacros()
 	{
 		return Collections.unmodifiableMap(macros);
 	}
 }
