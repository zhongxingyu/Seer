 package properties;
 
 import timeserver.TimeServer;
 import timeserver.TimeServerLinked;
 
 public class PropertyBag {
 	public enum TrafficType {
 		ALTERNATING, SIMPLE
 	}
 
 <<<<<<< HEAD:src/properties/PropertyBag.java
<<<<<<< HEAD:src/properties/PropertyBag.java
 	private static PropertyBag pb = null;
 	
 	private TimeServer timeServer = new TimeServerLinked();
 
 	private Double timeStep = 1.0;
 =======
 	private Double timeStep = 2.0;
 >>>>>>> 79684bc1d89df4da73244ecf716b4ad139cc5e66:src/Model/PropertyBag.java
=======
	private Double timeStep = 2.0;
>>>>>>> 79684bc1d89df4da73244ecf716b4ad139cc5e66:src/Model/PropertyBag.java
 	private Double runTime = 1000.0;
 	private Integer gridRow = 3;
 	private Integer gridColumn = 3;
 	private TrafficType trafficPattern = TrafficType.SIMPLE;
 	private Double carGenerationDelayMin = 15.0;
 	private Double carGenerationDelayMax = 15.0;
 	private Double roadSegmentLengthMin = 200.0;
 	private Double roadSegmentLengthMax = 500.0;
 	private Double intersectionLengthMin = 10.0;
 	private Double intersectionLengthMax = 15.0;
 	private Double carLengthMin = 10.0;
 	private Double carLengthMax = 15.0;
 	private Double carMaxVelocityMin = 10.0;
 	private Double carMaxVelocityMax = 20.0;	
 	private Double carStopDistanceMin = 1.0;
 	private Double carStopDistanceMax = 5.0;
 	private Double carBrakeDistanceMin = 1.5;
 	private Double carBrakeDistanceMax = 10.0;
 	private Double trafficLightGreenTimeMin = 20.0;
 	private Double trafficLightGreenTimeMax = 30.0;
 	private Double trafficLightYellowTimeMin = 4.0;
 	private Double trafficLightYellowTimeMax = 5.0;	
 
 	private PropertyBag() {
 
 	}
 
 	public static PropertyBag makePropertyBag() {
 		if (PropertyBag.pb == null) {
 			PropertyBag.pb = new PropertyBag();
 		}
 		return PropertyBag.pb;
 	}
 
 	public Double getTimeStep() {
 		return timeStep;
 	}
 
 	public void setTimeStep(Double timeStep) {
 		this.timeStep = timeStep;
 	}
 
 	public Double getRunTime() {
 		return runTime;
 	}
 
 	public void setRunTime(Double runTime) {
 		this.runTime = runTime;
 	}
 
 	public Integer getGridRow() {
 		return gridRow;
 	}
 
 	public void setGridRow(Integer gridRow) {
 		this.gridRow = gridRow;
 	}
 
 	public Integer getGridColumn() {
 		return gridColumn;
 	}
 
 	public void setGridColumn(Integer gridColumn) {
 		this.gridColumn = gridColumn;
 	}
 
 	public TrafficType getTrafficPattern() {
 		return trafficPattern;
 	}
 
 	public void setTrafficPattern(TrafficType trafficPattern) {
 		this.trafficPattern = trafficPattern;
 	}
 
 	public Double getCarGenerationDelayMin() {
 		return carGenerationDelayMin;
 	}
 
 	public void setCarGenerationDelayMin(Double carGenerationDelayMin) {
 		this.carGenerationDelayMin = carGenerationDelayMin;
 	}
 
 	public Double getCarGenerationDelayMax() {
 		return carGenerationDelayMax;
 	}
 
 	public void setCarGenerationDelayMax(Double carGenerationDelayMax) {
 		this.carGenerationDelayMax = carGenerationDelayMax;
 	}
 
 	public Double getRoadSegmentLengthMin() {
 		return roadSegmentLengthMin;
 	}
 
 	public void setRoadSegmentLengthMin(Double roadSegmentLengthMin) {
 		this.roadSegmentLengthMin = roadSegmentLengthMin;
 	}
 
 	public Double getRoadSegmentLengthMax() {
 		return roadSegmentLengthMax;
 	}
 
 	public void setRoadSegmentLengthMax(Double roadSegmentLengthMax) {
 		this.roadSegmentLengthMax = roadSegmentLengthMax;
 	}
 
 	public Double getIntersectionLengthMin() {
 		return intersectionLengthMin;
 	}
 
 	public void setIntersectionLengthMin(Double intersectionLengthMin) {
 		this.intersectionLengthMin = intersectionLengthMin;
 	}
 
 	public Double getIntersectionLengthMax() {
 		return intersectionLengthMax;
 	}
 
 	public void setIntersectionLengthMax(Double intersectionLengthMax) {
 		this.intersectionLengthMax = intersectionLengthMax;
 	}
 
 	public Double getCarLengthMin() {
 		return carLengthMin;
 	}
 
 	public void setCarLengthMin(Double carLengthMin) {
 		this.carLengthMin = carLengthMin;
 	}
 
 	public Double getCarLengthMax() {
 		return carLengthMax;
 	}
 
 	public void setCarLengthMax(Double carLengthMax) {
 		this.carLengthMax = carLengthMax;
 	}
 
 	public Double getCarMaxVelocityMin() {
 		return carMaxVelocityMin;
 	}
 
 	public void setCarMaxVelocityMin(Double carMaxVelocityMin) {
 		this.carMaxVelocityMin = carMaxVelocityMin;
 	}
 
 	public Double getCarMaxVelocityMax() {
 		return carMaxVelocityMax;
 	}
 
 	public void setCarMaxVelocityMax(Double carMaxVelocityMax) {
 		this.carMaxVelocityMax = carMaxVelocityMax;
 	}
 
 	public Double getCarStopDistanceMin() {
 		return carStopDistanceMin;
 	}
 
 	public void setCarStopDistanceMin(Double carStopDistanceyMin) {
 		this.carStopDistanceMin = carStopDistanceyMin;
 	}
 
 	public Double getCarStopDistanceMax() {
 		return carStopDistanceMax;
 	}
 
 	public void setCarStopDistanceMax(Double carStopDistanceyMax) {
 		this.carStopDistanceMax = carStopDistanceyMax;
 	}
 
 	public Double getCarBrakeDistanceMin() {
 		return carBrakeDistanceMin;
 	}
 
 	public void setCarBrakeDistanceMin(Double carBrakeDistanceyMin) {
 		this.carBrakeDistanceMin = carBrakeDistanceyMin;
 	}
 
 	public Double getCarBrakeDistanceMax() {
 		return carBrakeDistanceMax;
 	}
 
 	public void setCarBrakeDistanceMax(Double carBrakeDistanceyMax) {
 		this.carBrakeDistanceMax = carBrakeDistanceyMax;
 	}
 
 	public Double getTrafficLightGreenTimeMin() {
 		return trafficLightGreenTimeMin;
 	}
 
 	public void setTrafficLightGreenTimeMin(Double trafficLightGreenTimeMin) {
 		this.trafficLightGreenTimeMin = trafficLightGreenTimeMin;
 	}
 
 	public Double getTrafficLightGreenTimeMax() {
 		return trafficLightGreenTimeMax;
 	}
 
 	public void setTrafficLightGreenTimeMax(Double trafficLightGreenTimeMax) {
 		this.trafficLightGreenTimeMax = trafficLightGreenTimeMax;
 	}
 
 	public Double getTrafficLightYellowTimeMin() {
 		return trafficLightYellowTimeMin;
 	}
 
 	public void setTrafficLightYellowTimeMin(Double trafficLightYellowTimeMin) {
 		this.trafficLightYellowTimeMin = trafficLightYellowTimeMin;
 	}
 
 	public Double getTrafficLightYellowTimeMax() {
 		return trafficLightYellowTimeMax;
 	}
 
 	public void setTrafficLightYellowTimeMax(Double trafficLightYellowTimeMax) {
 		this.trafficLightYellowTimeMax = trafficLightYellowTimeMax;
 	}
 	
 	public TimeServer getTimeServer() {
 		return this.timeServer;
 	}
 
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		b.append("Simulation time step (seconds)        [" + this.getTimeStep() + "]\n");
 		b.append("Simulation run time (seconds)        [" + this.getRunTime() + "]\n");
 		b.append("Grid size (number of roads)          [row=" + this.getGridRow() + ",column=" + this.getGridColumn() + "]\n");
 		String pattern = (this.getTrafficPattern() == TrafficType.ALTERNATING ? "Alternating" : "Simple");
 		b.append("Traffic pattern                      [" + pattern + "]\n");
 		b.append("Car entry rate (seconds/car)         [min=" + this.getCarGenerationDelayMin() + ",max=" + this.getCarGenerationDelayMax() + "]\n");
 		b.append("Road segment length (meters)         [min=" + this.getRoadSegmentLengthMin() + ",max=" + this.getRoadSegmentLengthMax() + "]\n");
 		b.append("Intersection length (meters)         [min=" + this.getIntersectionLengthMin() + ",max=" + this.getIntersectionLengthMax() + "]\n");
 		b.append("Car length (meters)                  [min=" + this.getCarLengthMin() + ",max=" + this.getCarLengthMax() + "]\n");
 		b.append("Car maximum velocity (meters/second) [min=" + this.getCarMaxVelocityMin() + ",max=" + this.getCarMaxVelocityMax() + "]\n");
 		b.append("Car stop distance (meters)           [min=" + this.getCarStopDistanceMin() + ",max=" + this.getCarStopDistanceMax() + "]\n");
 		b.append("Car brake distance (meters)          [min=" + this.getCarBrakeDistanceMin() + ",max=" + this.getCarBrakeDistanceMax() + "]\n");
 		b.append("Traffic light green time (seconds)   [min=" + this.getTrafficLightGreenTimeMin() + ",max=" + this.getTrafficLightGreenTimeMax() + "]\n");
 		b.append("Traffic light yellow time (seconds)  [min=" + this.getTrafficLightYellowTimeMin() + ",max=" + this.getTrafficLightYellowTimeMax() + "]\n");
 		return b.toString();
 	}
 }
 
