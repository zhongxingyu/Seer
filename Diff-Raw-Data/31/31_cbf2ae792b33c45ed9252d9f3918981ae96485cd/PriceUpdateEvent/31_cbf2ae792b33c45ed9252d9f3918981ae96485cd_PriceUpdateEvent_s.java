 package events;
 
 public class PriceUpdateEvent extends Event {
 	
 	private double costPerCC;
 	private double costPerG;
 
 	public PriceUpdateEvent(double costPerCC, double costPerG) {
 		super();
 		this.costPerCC = costPerCC;
 		this.costPerG = costPerG;
 	}
 	
 	public double getCostPerCC() {
 		return costPerCC;
 	}
 	public double getCostPerG() {
 		return costPerG;
 	}
 	
 	
 
 }
