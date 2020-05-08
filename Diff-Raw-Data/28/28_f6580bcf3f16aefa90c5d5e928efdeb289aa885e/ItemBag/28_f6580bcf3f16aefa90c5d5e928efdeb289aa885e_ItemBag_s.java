 import java.util.Vector;
 import java.util.Enumeration;
 
 public class ItemBag
 {
 	private Vector<Item> items;
 
     //Creates an empty instance of ItemBag
 	public ItemBag()
 	{
 		items = new Vector<Item>();
 	}
 
 	public void addItem(Item i)
 	{
 		items.add(i);
 	}
 
 	public void removeItem(Item i){
 		items.remove(i);
 	}
 
 	//prints items
 	public void printItems()
 	{ 
 		if (items.size() == 0)
 			System.out.println("\n You have no items :(");
 		else
 		{
 			System.out.println("\n You currently have "+ items.size() +" items:");
             for (Enumeration<Item> e = items.elements(); e.hasMoreElements();)
             	System.out.println(e.nextElement()+ " ");
 			// for (int i = 0; i < items.size(); i++)
 			// 	System.out.println(items.elementAt(i)+ " ");
 			System.out.println();
 		}                
 	}
 
 	public int countItems()
 	{
 
 		return items.size();
 
 	}
 
 	public Item getItem(String name)
 	{

		for(int i = 0; i<items.size(); i++)
			if(items.elementAt(i).getName().equals(name))
 				return items.elementAt(i);
 
 		throw new IllegalArgumentException("No such item!");
 	}
 
 }
