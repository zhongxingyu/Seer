 package spaceappschallenge.moonville.businessmodels;
 
 import java.io.Serializable;
 import java.util.List;
 
 public class Resource implements Serializable {
 	protected String name;
 	protected int amount;
 	protected double quality;
 	protected int unitCost;// import value
 	protected int unitValue;// export value //Better change the name here and in
 							// xml huh!! Robik :-)
 	protected int weight;
 
 	public Resource(String name, int amount) {
 		super();
 		this.name = name;
 		this.amount = amount;
 	}
 
 	public Resource(String name, int amount, double quality) {
 		super();
 		this.name = name;
 		this.amount = amount;
 		this.quality = quality;
 	}
 
 	public Resource(String name, int amount, double quality, int unitCost,
 			int unitValue, int weight) {
 		super();
 		this.name = name;
 		this.amount = amount;
 		this.quality = quality;
 		this.unitCost = unitCost;
 		this.unitValue = unitValue;
 		this.weight = weight;
 	}
 
 	/**
 	 * Merges two lists of resources, by merging identical resources and adding
 	 * up their amount. Assumes no identical resources within a list.
 	 */
 	public static List<Resource> merge(List<Resource> a, List<Resource> b) {
 		for (Resource ra : a) {
 			for (Resource rb : b) {
 				if (ra.getName() == rb.getName()) {
 					ra.setAmount(ra.getAmount() + rb.getAmount());
 					b.remove(rb);
 				}
 			}
 		}
 		a.addAll(b);
 		return a;
 	}
 
 	// Getters and Setters
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getAmount() {
 		return amount;
 	}
 
 	public void setAmount(int amount) {
 		this.amount = amount;
 	}
 
 	public double getQuality() {
 		return quality;
 	}
 
 	public void setQuality(double quality) {
 		this.quality = quality;
 	}
 
 	public int getUnitCost() {
 		return unitCost;
 	}
 
 	public void setUnitCost(int unitCost) {
 		this.unitCost = unitCost;
 	}
 
 	public int getUnitValue() {
 		return unitValue;
 	}
 
 	public void setUnitValue(int unitValue) {
 		this.unitValue = unitValue;
 	}
 
 	public int getWeight() {
 		return weight;
 	}
 
 	public void setWeight(int weight) {
 		this.weight = weight;
 	}
 
 }
