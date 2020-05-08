 package Interpreter;
 import java.util.*;
 
 public class ASTFunction extends SimpleNode
 {
 	private ArrayList<String> parameters;
 	private String name;
 	private SymbolTable localScopeSymbolTable;
 	private int lineNumber = -1;
 	
 	public ASTFunction(int id)
 	{
 		super(id);	
 		this.localScopeSymbolTable = new SymbolTable(Global.getSymbolTable());
 		this.parameters = new ArrayList<String>();
 	}
 	
 	
 	public ArrayList<String> getParameters()
 	{
 		return this.parameters;
 	}
 	public void setName(String name)
 	{
 		this.name = name;
 		localScopeSymbolTable.setName(name);
 	}
 	
 	public int getLineNumber()
 	{
 		return lineNumber;
 	}
 	
 	public void setLineNumber(int lineNumber)
 	{
 		this.lineNumber = lineNumber;
 	}
 	
 	public void addParameter(String name)
 	{
 		this.parameters.add(name);
 		ByValVariable v = new ByValVariable(-255);
 		v.setParam();
		System.out.println("intASTFunction");
 		if(!this.localScopeSymbolTable.put(name, v))
 		{
			System.out.println("failed but making it true anyway");
 			v = (ByValVariable)this.localScopeSymbolTable.getVariable(name);
 			v.setParam();
 		}
 	}
 	
 	public void addParams(String...names)
 	{
 		for (String name : names)
 		{
			addParameter(name);
 		}
 	}
 	
 	
 	public String getName()
 	{
 		return this.name;
 	}
 
 	public SymbolTable getSymbolTable()
 	{
 		return localScopeSymbolTable;
 	}
 
 	public String getCode()
 	{
 		String code = "def " + this.name + "(";
 		for (int i = 0; i < parameters.size(); i++)
 		{
 			code += parameters.get(i) + (i < parameters.size() -1 ?  ", " : "");
 		}
 		code += ")\n" + Global.lineNumber++ + ". {" + jjtGetChild(0).getCode() + "\n" + Global.lineNumber++ + ". }";
 		
 		return code;
 	}
 
 }
