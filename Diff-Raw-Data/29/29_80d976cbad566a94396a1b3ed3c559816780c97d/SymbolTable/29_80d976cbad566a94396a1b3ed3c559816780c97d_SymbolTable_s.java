 package Interpreter;
 
 import java.util.*;
 
 public class SymbolTable
 {
 	private HashMap<String, Variable> vars;
 	
 	private SymbolTable previous = null;
 	private String name = "Global";
 	public SymbolTable(SymbolTable previous)
 	{
		this.previous = previous;
 		this.vars = new HashMap<String, Variable>();
 	}
 	
 	public int get(String varName)
 	{
 		return get(varName, false);
 	}
 	
 	public Variable getVariable(String varName)
 	{
 		Variable v = null;
 		if (vars.containsKey(varName))
 		{
 			v = vars.get(varName);
 		}
 		else if (previous != null)
 		{
 			v = previous.getVariable(varName);
 		}
 		return v;
 	}
 	
 	public HashMap<String, Variable> getLocalVariables()
 	{
 		return vars;
 	}
 	
 	public String getName()
 	{	
 		return this.name;
 	}
 	public void setName(String name)
 	{
 		this.name = name;
 	}
	public void setPrevious(SymbolTable previous)
	{
		this.previous = previous;
	}
 	/**
 	 * Gets an int representing a var in the symbol table.
 	 * @param varName name of the var to look for.
 	 * @param localOnly should you only look in the local symbol table or all of its parents.
 	 * @return an int representing the var or -255 if none was found.
 	 */
 	public int get(String varName, boolean localOnly)
 	{
 		int retVal = -255;
 		if (vars.containsKey(varName))
 		{
 			retVal = vars.get(varName).getValue();
 		}
 		else if (previous != null && !localOnly)
 		{
 			retVal = previous.get(varName);
 		}
 		
 		return retVal;
 	}
 	
 	public boolean put(String varName, Variable v)
 	{
 		if (vars.containsKey(varName))
 		{
 			return false;
 		}
 		vars.put(varName, v);
 		return true;
 	}
 	
 	public void setValue(String name, int value)
 	{
 		Variable v = getVariable(name);
 		if (v != null)
 		{
 			v.setValue(value);
 		}
 		else
 		{
 			System.out.println("error! variable not found: " + name);
 		}
 	}
 	
 	public HashSet<String> getCurrentVarNames()
 	{
 		//TODO: is this a copy of the keys or references?
 		HashSet<String> variables = new HashSet<String>(vars.keySet());
 		
 		if (previous != null)
 			variables.addAll(previous.getCurrentVarNames());
 		
 		return variables;
 	
 	}
 	
 	public SymbolTable getPrevious()
 	{
 		return previous;
 	}
 	
 	public HashSet<String> getLocalVarNames()
 	{
 		return new HashSet<String>(vars.keySet());
 	}
 	
 	public String toString()
 	{
 		String code = "Scope " + name + ":\n";
 		for (String key : vars.keySet())
 		{
 			code += key + ":" + vars.get(key).getValue() + "\n";
 		}
 		
 		if (previous != null)
 		{
 			code += previous.toString();
 		}
 		return code;
 	}
 }
