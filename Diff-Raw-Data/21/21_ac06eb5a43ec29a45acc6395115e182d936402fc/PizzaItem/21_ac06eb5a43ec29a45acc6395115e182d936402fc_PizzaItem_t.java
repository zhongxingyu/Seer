 package uk.ac.aber.dcs.cs12420.aberpizza.data;
 
 import java.util.*;
 
 public class PizzaItem extends AbstractItem {
 	
 	private Collection<String> toppings = null;
 	
 	public PizzaItem() {
 		setDescription("Default Pizza");
 		toppings = new LinkedList<String>();
 		
 	}
 	
 	public Collection<String> getToppings() { return toppings; }
 	public void setToppings(Collection<String> s) { toppings = s; }
		
	@Override
	public String toString() {
		String s = "Pizza, "+getDescription()+" "+getSize()+"\" "+getPrice()+" [ ";
		for (String topping : getToppings() ) {
			s = s + topping + " ";
		}
		s = s + "]";
		return s;
	}
 	
 }
