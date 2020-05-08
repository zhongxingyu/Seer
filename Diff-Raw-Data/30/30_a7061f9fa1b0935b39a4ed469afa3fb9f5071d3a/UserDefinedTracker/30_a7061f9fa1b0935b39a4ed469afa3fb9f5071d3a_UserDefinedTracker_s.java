 package uk.org.smithfamily.utils.normaliser.userdefined;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class UserDefinedTracker
 {
 	private List<UserDefinedItem> items = new ArrayList<UserDefinedItem>();
 	private String name;
 
 	public void addItem(UserDefinedItem x)
 	{
 		items.add(x);
 	}
 
 	public List<UserDefinedItem> getItems()
 	{
 		return items;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 		
 	}
 
 	public String getName()
 	{
 		return name;
 	}

	public boolean isDefinitionCompleted()
	{
		return true;
	}
 }
