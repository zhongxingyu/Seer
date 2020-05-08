 package atl.space.components.heat;
 
 public abstract class HeatHull extends HeatContainerComponent implements HeatReleaser{
 	
 	//Something like the hull of a ship. Has heat, releases a certain amount of it every tick as IR waves.
 	//Does NOT model internal heatsinks or generators.
 	//See http://en.wikipedia.org/wiki/Thermal_radiation
 	/*(quote)
 	 * The total amount of radiation of all frequencies increases steeply as the temperature rises; 
 	 * it grows as T4, where T is the absolute temperature of the body. 
 	 * An object at the temperature of a kitchen oven, 
 	 * about twice the room temperature on the absolute temperature scale (600 K vs. 300 K) 
 	 * radiates 16 times as much power per unit area.
 	 */
 	
 	//The above means heat loss is caluclated as follows from the temp of the object:
 	//Heat loss = ((Temperature)^(4 - (4*heatDifferenceCompensation))) * heatDispersionFactor 
 	
 	
 	//determines how much heat is lost
 	//May be representative of surface area or material type or both...
 	//I may even want to split them. Hmm.
 	private double heatDispersionFactor;		
 	
 	/*
 	 * causes less emissions difference between higher/lower temperatures. Dunno how. Spaaaace MAAAAGICCCCC
 	 * Is a value from 0 to 1, with 0 being no compensation and 1 being max compensation. Tryin to be realistic here.
 	 */
 	private double heatDifferenceCompensation;
 	
 	
 	public HeatHull(){
 		this(1, 0);
 	}
 	public HeatHull(double dispersionFactor, double differenceCompensation){
 		this(0, 1, 100, dispersionFactor, differenceCompensation);
 	}
 	public HeatHull(double heat, double degreesPerHeat, double temperatureCapacity, double dispersionFactor, double differenceCompensation){
 		super(heat, degreesPerHeat, temperatureCapacity);
 		this.heatDispersionFactor = dispersionFactor;
 		this.heatDifferenceCompensation = differenceCompensation;
 	}
 	
 	private double calculateHeatRelease(){
 		return getDispersionFactor() * Math.pow(getTemperature(), (4 - (4 * getDifferenceCompensation())));
 	}
 	
 	public double getDispersionFactor(){
 		return heatDispersionFactor;
 	}
 	
 	public double getDifferenceCompensation(){
 		return heatDifferenceCompensation;
 	}
 	
 	@Override
 	public void releaseHeat(double factor) {
 		loseHeat(calculateHeatRelease() * factor);
 		//do other stuff?
 	}
 
 	@Override
 	public void releaseHeat() {
		releaseHeat(1);
 	}
 	
 	
 }
