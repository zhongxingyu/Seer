import Coefficient;
 
class Event
 {
 	private String name;
	private List<Coeffiecient> coefs;
 	
 	public Event(String name)
 	{
 		this.name = name;
		coefs = new ArrayList<Coeffiecient>();
 	}
 	
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	
 	public String  getName()
 	{
 		return name;
 	}
 }
