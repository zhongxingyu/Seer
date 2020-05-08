 package uk.ac.aber.dcs.cs12420.aberpizza.data;
 
 public class SideItem extends AbstractItem {
 
 	public SideItem() {
 		setDescription("Default side order");
 	}
 	
 	public String toString() {
		String s;
		if (getSize() == 1) {
			s = "Side order, " + getDescription() + ", " + " " +
					getPrice();
		}
		else {
			s = "Side order, " + getDescription() + ", " + (int)getSize() + " pieces, " +
					getPrice();
		}
 		return s;
 	}
 	
 }
