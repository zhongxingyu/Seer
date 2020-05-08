 package Interpreter;
 import java.util.*;
 import viz.*;
 /**
  * class ByValVariable
  * @authors Tom Fairfield Eric Schultz
  */
 public class ByNameVariable extends AbstractVariable implements Variable
 {
 	private ASTVar ref;
 	private SymbolTable symbolTable;
 	private ByValVariable var;
 	public int index = -1;
 	private boolean isArray = false;
 	public ByNameVariable()
 	{
 	
 	}
 	
 	public ByNameVariable(ASTVar v)
 	{
 		this.ref = v;
 	}
 	
 	public void setRef(ASTVar v)
 	{
 		this.ref = v;
 	}
 	
 	
 	public ASTVar getRef()
 	{
 		return this.ref;
 	}
 	
 	public int getIndex()
 	{
 		findIndex();
 		return index;
 	}
 	
 	public void setIndex(int index)
 	{
 		this.index = index;	
 	}
 	
 	public void setVariable(ByValVariable v)
 	{
 		this.var = v;
 		setSymbolTable(Global.getFunction("main").getSymbolTable());
 	}
 	public void setVariable(ByValVariable v, int index)
 	{
 		this.var = v;
 		this.index = index;
 		setArray();
 		setSymbolTable(Global.getFunction("main").getSymbolTable());
 	}
 	
 	public ByValVariable getVariable()
 	{
 		return this.var;
 	}
 	public void setSymbolTable(SymbolTable st)
 	{
 		this.symbolTable = st;
 	}
 	
 	public SymbolTable getSymbolTable()
 	{
 		return symbolTable;
 	}
 	
 	public void setArray()
 	{
 		this.isArray = true;
 	}
 	
 	public boolean getIsArray()
 	{
 		return this.isArray;
 	}
 	
 	public void findIndex()
 	{
 		SymbolTable temp = Global.getCurrentSymbolTable();
 		System.out.println(temp);
 		
 		Global.setCurrentSymbolTable(Global.getFunction("main").getSymbolTable());
 		InterpretVisitor iv = new  InterpretVisitor();
 
		if (isArray)
 		{
 			index = (Integer) ref.jjtGetChild(0).jjtAccept(iv, null);
 			System.out.println(index + " is the index");
 			
 		}
 
 		Global.setCurrentSymbolTable(temp);
 
 	}
 	
 	public int getValue()
 	{
 		SymbolTable temp = Global.getCurrentSymbolTable();
 		System.out.println(temp);
 		
 		Global.setCurrentSymbolTable(Global.getFunction("main").getSymbolTable());
 		System.out.println("Current Symbol table is now " + Global.getCurrentSymbolTable());
 		InterpretVisitor iv = new  InterpretVisitor();
 		System.out.println(ref);
 		Integer value = (Integer) ref.jjtAccept(iv, null);
		if (isArray)
 		{
 			index = (Integer) ref.jjtGetChild(0).jjtAccept(iv, null);
 			System.out.println(index + " is the index");
 			
 		}
 		System.out.println("Got value : " + value + "From " + ref.getName());
 		Global.setCurrentSymbolTable(temp);
 		System.out.println("Back to symbol table: " + Global.getCurrentSymbolTable());
 		
 		return value;
 	}
 	
 	public int getValue(int asdf)
 	{
 		System.out.println("Dont use this");
 		return -1000;
 	}
 	public void setValue(int value)
 	{
 		if (!isArray)
 		{
 			var.setValue(value);
 		}
 		else
 		{
 			try
 			{
 				InterpretVisitor iv = new InterpretVisitor();
 				var.setValue(value, (Integer)ref.jjtGetChild(0).jjtAccept(iv, null));
 			}
 			catch (Exception e)
 			{
 				System.out.println(e);
 			}
 		}
 	}
 	public ArrayList<Integer> getValues()
 	{
 		return null;
 	}	
 	public void setValue(int x, int y)
 	{
 		index = x;
 		setValue(y);
 	}
 }
