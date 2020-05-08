 package chalmers.dax021308.ecosystem.model.population;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Common settings for all agents to use.
  * 
  * Sets and gets from CommonSettingsPanel.
  * 
  * @author Erik Ramqvist
  *
  */
 public class CommonSettings {
 	public static CommonSettings predSettings = new CommonSettings();
 	public static CommonSettings preySettings = new CommonSettings();
 	public static CommonSettings grassSettings = new CommonSettings();
 	
 	static {
 		
 		//TODO: need to add values to doubleSettings-list aswell, and remove the default value.
 		
 		/*	PREY DEFAULT SETTINGS 	*/
 		preySettings.capacity 			= new DoubleSettingsContainer("Capacity", 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
 		preySettings.maxSpeed 			= new DoubleSettingsContainer("Max speed", 1, 100, 2); 	
 		preySettings.maxAcceleration 	= new DoubleSettingsContainer("Max Acceleration", 1, 100, 3);
 		preySettings.visionRange 		= new DoubleSettingsContainer("Vision range", 1, 1000, 200);
 		preySettings.width 				= new DoubleSettingsContainer("Width", 1, 100, 5);
 		preySettings.height				= new DoubleSettingsContainer("Height", 1, 100, 10);
 		
 		preySettings.doubleSettings.add(preySettings.maxSpeed);
 		preySettings.doubleSettings.add(preySettings.capacity);
 		preySettings.doubleSettings.add(preySettings.visionRange);
 		preySettings.doubleSettings.add(preySettings.maxAcceleration);
 		preySettings.doubleSettings.add(preySettings.width);
 		preySettings.doubleSettings.add(preySettings.height);
 		
 		predSettings.capacity 			= new DoubleSettingsContainer("Capacity", 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
 		predSettings.maxSpeed 			= new DoubleSettingsContainer("Max speed", 1, 100, 2); // 2.3
 		predSettings.maxAcceleration 	= new DoubleSettingsContainer("Max Acceleration", 1, 100, 1); //0.5
 		predSettings.visionRange 		= new DoubleSettingsContainer("Vision range", 1, 1000, 250);
 		predSettings.width 				= new DoubleSettingsContainer("Width", 1, 100, 10);
 		predSettings.height				= new DoubleSettingsContainer("Height", 1, 100, 20);
 		
 		predSettings.doubleSettings.add(preySettings.maxSpeed);
 		predSettings.doubleSettings.add(predSettings.capacity);
 		predSettings.doubleSettings.add(predSettings.visionRange);
 		predSettings.doubleSettings.add(predSettings.maxAcceleration);
 		predSettings.doubleSettings.add(predSettings.width);
 		predSettings.doubleSettings.add(predSettings.height);
 		
 		//TODO: Fill in correct values, copied to avoid nullpointer.
 		grassSettings.capacity 			= new DoubleSettingsContainer("Capacity", 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
 		grassSettings.maxSpeed 			= new DoubleSettingsContainer("Max speed", 1, 100, 2); // 2.3
 		grassSettings.maxAcceleration 	= new DoubleSettingsContainer("Max Acceleration", 1, 100, 1); //0.5
 		grassSettings.visionRange 		= new DoubleSettingsContainer("Vision range", 1, 1000, 250);
 		grassSettings.width 				= new DoubleSettingsContainer("Width", 1, 100, 10);
 		grassSettings.height				= new DoubleSettingsContainer("Height", 1, 100, 20);
 		
 		grassSettings.doubleSettings.add(preySettings.maxSpeed);
 		grassSettings.doubleSettings.add(grassSettings.capacity);
 		grassSettings.doubleSettings.add(grassSettings.visionRange);
 		grassSettings.doubleSettings.add(grassSettings.maxAcceleration);
 		grassSettings.doubleSettings.add(grassSettings.width);
 		grassSettings.doubleSettings.add(grassSettings.height);
 		
 //		TODO: Add to deer specific variables.
 //		TODO: Make CommonSettings abstract and make extending classes.
 //		private double STOTTING_RANGE = 10;
 //		private double STOTTING_LENGTH = 8;
 //		private double STOTTING_COOLDOWN = 50;
 //		private double stottingDuration = STOTTING_LENGTH;
 //		private double stottingCoolDown = 0;
 //		digestion_time		= new DoubleSettingsContainer("Digestion time", 1, 1000, 10);
 //		reproduction_rate	= new DoubleSettingsContainer("Reproduction rate", 1, 1000, 10);
 //
 //		public DoubleSettingsContainer digestion_time;
 //		public DoubleSettingsContainer reproduction_rate;
 
 	}
 	
 	public static class DoubleSettingsContainer {
 		public DoubleSettingsContainer(String name, double min, double max, double defaultValue) {
 			this.value = defaultValue;
 			this.defaultValue = defaultValue;
 			this.max = max;
 			this.min = min;
 			this.name = name;
 		}
 		public String name;
 		public double value;
 		public double max;
 		public double min;
 		public double defaultValue;
 	}
 
 	public class BooleanSettingsContainer {
 		public BooleanSettingsContainer(String name, boolean defaultValue) {
 			this.defaultValue = defaultValue;
 			this.value = defaultValue;
 		}
 		public String name;
 		public boolean value;
 		public boolean defaultValue;
 	}
 	
 	private List<DoubleSettingsContainer> doubleSettings;
 	private List<BooleanSettingsContainer> booleanSettings;
 	
 	
 	/*	Common Settings containers	*/
 	public DoubleSettingsContainer capacity;
 	public DoubleSettingsContainer max_energy;
 	public DoubleSettingsContainer visionRange;
 	public DoubleSettingsContainer maxAcceleration;
 	public DoubleSettingsContainer maxSpeed;
 	public DoubleSettingsContainer width;
 	public DoubleSettingsContainer height;
 	public DoubleSettingsContainer interaction_range;
 	public DoubleSettingsContainer eating_range;
 	public DoubleSettingsContainer focus_range;
 	public DoubleSettingsContainer velocity_decay;
 	public DoubleSettingsContainer obstacle_safety_distance;
 	
 	/* Common boolean containers. */
 	private BooleanSettingsContainer groupBehavior;
 	private BooleanSettingsContainer pathFinding;
 	
 	public CommonSettings() {
 		doubleSettings = new ArrayList<DoubleSettingsContainer>();
 		
 		/* Initialize global default values (from AbstractAgent) */ 
 		//TODO: Put only global here and remove population specific settings?
 		capacity 					= new DoubleSettingsContainer("Capacity", 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
 		interaction_range			= new DoubleSettingsContainer("Interaction range", 1, 100, 10);
 		eating_range				= new DoubleSettingsContainer("Eating range", 1, 100, 5);
 		focus_range					= new DoubleSettingsContainer("Focus range", 1, 1000, 150);
 		obstacle_safety_distance	= new DoubleSettingsContainer("Obstacle safety distance", 1, 100, 10);
 		velocity_decay				= new DoubleSettingsContainer("Velocity decay", 1, 10, 1); //TODO: Add support for decimals. Only integers now.
 
 		doubleSettings.add(interaction_range);
 		doubleSettings.add(eating_range);
 		doubleSettings.add(focus_range);
 		doubleSettings.add(obstacle_safety_distance);
 		doubleSettings.add(velocity_decay);
 
 		booleanSettings = new ArrayList<BooleanSettingsContainer>();
 		
 		groupBehavior 	= new BooleanSettingsContainer("Group behavior", true);
 		pathFinding 	= new BooleanSettingsContainer("Pathfinding", true);
 		
 		booleanSettings.add(groupBehavior);
 		booleanSettings.add(pathFinding);
 	}
 	public List<DoubleSettingsContainer> getDoubleSettings() {
 		return doubleSettings;
 	}
 	public void setDoubleSettings(List<DoubleSettingsContainer> doubleSettings) {
 		this.doubleSettings = doubleSettings;
 	}
 	public List<BooleanSettingsContainer> getBooleanSettings() {
 		return booleanSettings;
 	}
 	public void setBooleanSettings(
 			List<BooleanSettingsContainer> booleanSettings) {
 		this.booleanSettings = booleanSettings;
 	}
 	public double getMaxSpeed() {
 		return maxSpeed.value;
 	}
 	public void setMaxSpeed(double maxSpeed) {
 		this.maxSpeed.value = maxSpeed;
 	}
 	public int getCapacity() {
 		return (int) capacity.value;
 	}
 	public void setCapacity(int capacity) {
 		this.capacity.value = capacity;
 	}
 	public int getMax_energy() {
 		return (int) max_energy.value;
 	}
 	public void setMax_energy(int max_energy) {
 		this.max_energy.value = max_energy;
 	}
 	public double getVisionRange() {
 		return visionRange.value;
 	}
 	public void setVisionRange(double visionRange) {
 		this.visionRange.value = visionRange;
 	}
 	public double getMaxAcceleration() {
 		return maxAcceleration.value;
 	}
 	public void setMaxAcceleration(double maxAcceleration) {
 		this.maxAcceleration.value = maxAcceleration;
 	}
 	public int getWidth() {
 		return (int) width.value;
 	}
 	public void setWidth(int width) {
 		this.width.value = width;
 	}
 	public int getHeight() {
 		return (int) height.value;
 	}
 	public void setHeight(int height) {
 		this.height.value = height;
 	}
 	public double getInteraction_range() {
 		return interaction_range.value;
 	}
 	public void setInteraction_range(double interaction_range) {
 		this.interaction_range.value = interaction_range;
 	}
 	public double getEating_range() {
 		return eating_range.value;
 	}
 	public void setEating_range(double eating_range) {
 		this.eating_range.value = eating_range;
 	}
 	public double getFocus_range() {
 		return focus_range.value;
 	}
 	public void setFocus_range(double focus_range) {
 		this.focus_range.value = focus_range;
 	}
 	public double getVelocity_decay() {
 		return velocity_decay.value;
 	}
 	public void setVelocity_decay(double velocity_decay) {
 		this.velocity_decay.value = velocity_decay;
 	}
 	public boolean isGroupBehavior() {
 		return groupBehavior.value;
 	}
 	public void setGroupBehavior(boolean groupBehavior) {
 		this.groupBehavior.value = groupBehavior;
 	}
 }
