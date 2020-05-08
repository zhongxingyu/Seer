public class ContactImpl implements Contact
 {
 	private int id;
 	private String name;
 	private String notes;
	private int cont=0;

	public Contact(String n)
	{
		 this.name=n;
		 this.id=cont;
		 cont ++;
 
	}
 	
	public Contact(String n,String  not)
 	{
 		 this.name=n;
 		 this.notes= not;
		 this.id=cont;
		 cont ++;
 
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
