 package Interpreter;
 import java.util.*;
 
 public class ByCopyRestoreVariable extends AbstractVariable implements Variable
 {
 	//The ByValVariable we're referring to
 	private ByValVariable ref;
 	private int refIndex = -1;
 	private int value;
 	
 	public ByCopyRestoreVariable()
 	{
 	
 	}
 	public ByCopyRestoreVariable(ByValVariable ref)
 	{
 		setRef(ref);
 	}
 	
 	public void setRef(ByValVariable ref)
 	{
 		this.ref = ref;
 		this.value = ref.getValue();
 	}
 	
 	public void setValue(int value)
 	{
 		this.value = value;
 	}
 	
 	public void setRef(ByValVariable ref, int index)
 	{
 		this.ref = ref;
 		this.refIndex = index;
 	}	
 	
 	public int getRefIndex()
 	{
 		return this.refIndex;
 	}
 	
 	public void copyOut()
 	{
 		if (this.refIndex == -1)
 		{
 			System.out.println("Copying out value " + this.value);
 			this.ref.setValue(this.value);
 		}
 		else
 		{
 			System.out.println("coping out value " + this.value + " to " + refIndex);
			System.out.println(this.ref.getIsArray());
			this.ref.setValue(value, this.refIndex);
 		}
 	}
 	
 	public ByValVariable getRef()
 	{
 		return this.ref;
 	}
 	
 	public int getValue()
 	{
 		System.out.println("Getting value");
 		return this.value;
 	}
 	
 	public void setParam()
 	{
 		this.isParam = true;
 	}
 	
 	public boolean getIsParam()
 	{
 		return this.isParam;
 	}
 	
 	public void setArray()
 	{
 		System.out.println("A CopyRestore variable can't be an array, ERROR");
 	}
 	
 	public boolean getIsArray()
 	{
 		return false;
 	}
 	
 	public ArrayList<Integer> getValues()
 	{
 		System.out.println("Not an array, ERROR");
 		return null;
 	}
 	
 	public int getValue(int subscript)
 	{
 		System.out.println("Not an array, ERROR");
 		return 0;
 	}
 
 	public void setValue(int value, int index)
 	{
 		System.out.println("Not an array, ERROR");
 	}
 }
