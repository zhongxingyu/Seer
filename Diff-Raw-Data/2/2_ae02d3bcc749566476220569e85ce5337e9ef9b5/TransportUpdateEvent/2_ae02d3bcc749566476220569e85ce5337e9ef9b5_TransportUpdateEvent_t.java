 package events;
 
 import java.util.Date;
 
 import routes.DistributionCentre;
 import routes.Vehicle;
 import backend.Day;
 
 public class TransportUpdateEvent extends Event {
 	
 	private double costPerCC;
 	private double costPerG;
 	private int frequency;
 	private int durationInMinutes;
 	private Day day;
 	private DistributionCentre origin;
 	private DistributionCentre destination;
 	
 	
 	/**
 	 * Creates a Transport Update Event, used to record a 
 	 * change in some Transport's variables
 	 * 
 	 * @param costPerCC - Cost per cubic centimetre
 	 * @param costPerG - Cost per gram
 	 * @param frequency - how many times per day the transport runs
 	 * @param durationInMinutes - duration of a transport event
 	 * @param day - the day on which the transport departs
 	 * @param origin - the DistributionCentre from which the transport departs
 	 * @param destination - the DistributionCentre at which the transport arrives
 	 */
 	
 	
 	
 	
 	
 	public TransportUpdateEvent(Vehicle vehicle, double costPerCC, double costPerG,
 			int frequency, int durationInMinutes, Date timestamp,
 			DistributionCentre origin, DistributionCentre destination) {
 		super(vehicle, timestamp);
 		this.costPerCC = costPerCC;
 		this.costPerG = costPerG;
 		this.frequency = frequency;
 		this.durationInMinutes = durationInMinutes;
 		this.origin = origin;
 		this.destination = destination;
 	}
 	
 	
 	public double getCostPerCC() {
 		return costPerCC;
 	}
 
 	public double getCostPerG() {
 		return costPerG;
 	}
 
 	public int getFrequency() {
 		return frequency;
 	}
 
 	public int getDurationInMinutes() {
 		return durationInMinutes;
 	}
 
 	public Day getDay() {
 		return day;
 	}
 
 	public void setDurationInMinutes(int durationInMinutes) {
 		this.durationInMinutes = durationInMinutes;
 	}
 	
 	public void setDay(Day day) {
 		this.day = day;
 	}
 	
 	public DistributionCentre getOrigin() {
 		return origin;
 	}
 
 	public DistributionCentre getDestination() {
 		return destination;
 	}
 
 	public Date getDate() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String displayString() {
		String str = "Update Transport Event: \n" +
 				"\tVehicle: "+super.getVehicle()+"\n"+
 				"\tCost per CC: "+costPerCC+"\n"+
 				"\tCost per g: "+costPerG+"\n"+
 				"\tFrequency: "+frequency+"\n"+
 				"\tDuration: "+durationInMinutes+"min \n"+
 				"\tOrigin: "+origin.displayString()+"\n"+
 				"\tDestination: "+destination.displayString()+"\n"
 				;
 		
 		return str;
 	}
 
 
 
 }
