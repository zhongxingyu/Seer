 /**
  * Item class.
  * This class deals with the individual items.
  * Items are given a description, a value, and a weight.
  * The description is simply a string describing the item.
  * The value of the item is used for comparing different items. 
  * For weapons, this value will decide which item is stronger.
  * For food, this value will decide how much the health of the player will increment.
  * The weight of the item will possibly later be used to have an item limit for the player. 
  * 
  */
 
 
 public class Item implements Comparable<Item> {
 
 	private String description;
 	private int value;
 	private int weight;
 	
 		
 	public Item(String description, int value, int weight) {
 		//Constructor assigning the variables
 		this.description = description;
 		this.value = value;
 		this.weight = weight;
 	}
 	
 	public Item(String description) {
 		this(description, 0, 0);
 	}
 
 
 	public String getDescription() {
 		return description;
 	}
 
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 
 	public int getValue() {
 		return value;
 	}
 
 	public int getWeight() {
 		return weight;
 	}
 
 
 	public void setWeight(int weight) {
 		this.weight = weight;
 	}
 
 
 	@Override
 	public int compareTo(Item i) {
 		if(this.value > i.value){
 			return 1;	//this item is of greater value than the parameter item
 		} else if (this.value == i.value){
 			return 0;	//the items have the same value (tie)
 		} 
 		//otherwise, this.value < i.value
 		return -1;		//this item is of less value than the parameter item
 	}
 
 	public boolean equals(Object o){
 		if(o instanceof Item){
			return (this.description.equals(((Item) o).getDescription()));
 		}
 		return false;
 	}
 	
 	public String toString(){
 		return description;
 	}
 
 }
