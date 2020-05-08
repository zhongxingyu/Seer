 package uk.ac.aber.dcs.cs12420.aberpizza.data;
 
 public class SideItem extends AbstractItem {
 
 	public SideItem() {
 		setDescription("Default side order");
 	}
 	
 	public String toString() {
		String s = "Side order, " + getDescription() + ", " + getSize() + " pieces, " +
				getPrice();
 		return s;
 	}
 	
 }
