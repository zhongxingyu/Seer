 package sw.app_250grice;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 public class Page {
 	
 	String name;
 	List<Item> items;
 	
 	public Page(String name) {
 		items = new ArrayList<Item>();
 		this.name = name;
 	}
 	
 	public void addItem(Item toAdd) {
 		Item toSearch = null;
 		try {
 			toSearch = getItemByNameAndUnit(toAdd.getName(), toAdd.getUnit());
 		} catch (ItemNotFoundException e) {
			items.add(toAdd.clone());
 			return;
 		}
 		
 		toSearch.addValue(toAdd.getValue());
 	}
 	
 	public Item getItemByNameAndUnit(String name, Units unit) throws ItemNotFoundException{
 	    for(Item item : items)
 	        if(item.getName() == name && item.getUnit() == unit)
 	            return item;
 		
 	    throw(new ItemNotFoundException());
 	}
 	
 	public void removeItemByNameAndUnit(String name, Units unit) throws ItemNotFoundException{
 		
 		Item i = getItemByNameAndUnit(name, unit);
 		
 		items.remove(i);
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if ( this == obj) return true;
 		if(!(obj instanceof Page)) return false;
 		
 		Page page = (Page)obj;
 
 		if(!((this.name == page.name) && (this.items.size() == page.items.size())))
 			return false;
 		
 		Iterator<Item> thisIt = this.items.iterator();
 		Iterator<Item> pageIt = page.items.iterator();
 		
 		while(thisIt.hasNext())
 			if (!thisIt.next().equals(pageIt.next()))
 				return false;
 		
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		String ret = String.format(Locale.US, "Name:%s , Count:%d", this.name, this.items.size());
 		
 		return ret;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String value) {
 		this.name = value;
 	}
 	
 	public List<Item> getItems() {
 		List<Item> ret = new ArrayList<Item>();
 		
 		for (Item item : items) {
 			ret.add(item.clone());
 		}
 		
 		return ret;
 	}
 	
 	public Page clone() {
 		Page p = new Page(this.name);
 
 		p.items = getItems();
 		
 		return p;
 	}
 	
 
 }
