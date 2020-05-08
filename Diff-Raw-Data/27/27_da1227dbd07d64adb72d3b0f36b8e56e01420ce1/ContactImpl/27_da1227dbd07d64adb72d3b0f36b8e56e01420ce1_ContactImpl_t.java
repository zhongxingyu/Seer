import java.io.*;
public class ContactImpl implements Contact,Serializable
 {
 	private int id;
 	private String name;
 	private String notes;
	
 
 	
	
	
	
	public ContactImpl(String n,String  not,int code)
 	{
 		 this.name=n;
 		 this.notes= not;
		 this.id=code;
		
 
 	}
 

 	@Override
 	public boolean equals (Object o)
 	{
 		if(o!=null && o instanceof ContactImpl)
 		{
 			ContactImpl cont=(ContactImpl)o;
 			return this.id==cont.getId();
 			
 		}
 		else
 		{
 			return false;
 		}
 		
 
 	}
 	
 	@Override
 	public int hashCode()
 	{
 	  return this.id;
 	
 	
 	}
 
 	
 	public int getId()
 	{
 	 return this.cont;
 	
 	}
 		
 	public String  getName()
 	{
 	
 	 return this.name;
 	}
 
 	public String  getNotes()
 	{
 	
 	 return this.notes;
 	}
 	
 	public void addNotes(String n)
 	{
 		this.notes=n;
 	
 	}
 	
 }
