 package simulator;
 
 public class Reactor extends PlantComponent {
 	private final static int DEFAULT_TEMPERATURE = 0;
 	private final static int DEFAULT_PRESSURE = 0;
 	private final static int DEFAULT_WATER_VOLUME = 8000;
 	private final static int DEFAULT_STEAM_VOLUME = 0;
 	
 	private final static int MAX_TEMPERATURE = 2865; // 2865C is the melting point of uranium oxide.
 	private final static int MAX_PRESSURE = 500;
 	private final static int MAX_HEALTH = 100;
 	private final static int MAX_HEATING_PER_STEP = 100; // in degrees C. maximum amount to increase temp by in a step. 
 	private final static int MIN_SAFE_WATER_VOLUME = 2000;
 	private final static int UNSAFE_HEATING_MULTIPLIER = 2; // amount to increase 
 	private final static int WATER_STEAM_RATIO = 2; // 1:2 water to steam
 	private final static int HEALTH_CHANGE_WHEN_DAMAGING = 10;
 	private final static double EVAP_MULTIPLIER = 0.5; // conversion from temperature to amount evaporated. 
 	
 	private int temperature;
 	private int pressure;
 	private int waterVolume;
 	private int steamVolume;
 	private int health;
 	private ControlRod controlRod;
 	private int waterPumpedIn;
 	
 	public Reactor() {
 		super(0,0,true,true); // Never fails, is operational and is pressurised.
 		this.controlRod = new ControlRod();
 		this.health = MAX_HEALTH;
 		this.temperature = DEFAULT_TEMPERATURE;
 		this.pressure = DEFAULT_PRESSURE;
 		this.waterVolume = DEFAULT_WATER_VOLUME;
 		this.steamVolume = DEFAULT_STEAM_VOLUME;
 		this.getFlowOut().setType(FlowType.Steam);
 	}
 	
 	// ----------- Getters & Setters ---------------
 	
 	public int getTemperature() {
 		return temperature;
 	}
 	
 	public int getMaxTemperature() {
 		return MAX_TEMPERATURE;
 	}
 	
 	public int getPressure() {
 		return pressure;
 	}
 	
 	public int getMaxPressure() {
 		return MAX_PRESSURE;
 	}
 	
 	public int getWaterVolume() {
 		return waterVolume;
 	}
 	
 	public int getMinSafeWaterVolume() {
 		return MIN_SAFE_WATER_VOLUME;
 	}
 	
 	/**
 	 * Updates the amount of water in the reactor.
 	 * Also stores the amount of water pumped in for future calculations.
 	 * This method should only be called once per timeStep.
 	 * 
 	 * @param amount amount of water to add to the total in the reactor.
 	 */
 	public void updateWaterVolume(int amount) {
 		this.waterPumpedIn = amount; // allows for only 1 call per step.
 		this.waterVolume += amount;
 	}
 	
 	public int getSteamVolume()
 	{
 		return steamVolume;
 	}
 
 	/**
 	 * Updates the amount of steam in the reactor.
 	 * 
 	 * amount can be negative and will be when steam is leaving 
 	 * the reactor.
 	 *  
 	 * @param amount the amount of steam to add to the volume.
 	 */
 	public void updateSteamVolume(int amount)
 	{
 		this.steamVolume += amount;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 	
 	public int getPercentageLowered() {
 		return controlRod.getPercentageLowered();
 	}
 	
 	public void setPercentageLowered(int percentageLowered) {
 		controlRod.setPercentageLowered(percentageLowered);
 	}
 	
 	// ---------------- System update methods ---------------
 	
 	public void updateState() {
 		updateTemperature();
 		//updatePressure();
 		evaporateWater();
 		checkIfDamaging();
 	}
 	
 	private void updateTemperature() {
 		int changeInTemp;
 		Flow flowIn = this.getInput().getFlowOut();
 		int waterTemperature = flowIn.getTemperature();
 		
 		changeInTemp = heating(controlRod.getPercentageLowered()) - cooldown(waterTemperature, this.waterPumpedIn);
 		this.temperature += changeInTemp;
 	}
 	
 	/**
 	 * Calculates the amount of cooldown in the reactor for this
 	 * time step. Dependent upon the temperature and volume of water being
 	 * pumped into the reactor.
 	 * 
 	 * @param waterTemperature temperature of the water being pumped into the reactor.
 	 * @param pumpedIn amount of water pumped in since the last timeStep.
 	 * @return how much to reduce the temperature by. 
 	 */
 	private int cooldown(int waterTemperature, int pumpedIn) {
 		int tempDiff = this.temperature - waterTemperature; 
 		if (this.waterVolume < 1) return 0; // stops a potential divide by 0 on the next line.
 		return tempDiff * (1 - ((this.waterVolume - pumpedIn)/this.waterVolume));
 	}
 	
 	/**
 	 * Calculates the amount of heating in the reactor for this time step.
 	 * Depends upon how far the control rods are lowered.
 	 * 
 	 * If there is less than the minimum safe amount of water in the reactor,
 	 * the control rods will heat up much more quickly.
 	 * (The maximum heating amount is multiplied by UNSAFE_HEATING_MULTIPLIER) 
 	 * 
 	 * @param loweredPercentage percentage the control rods are lowered.
 	 * @return how much to the increase the temperature by.
 	 */
 	private int heating(int loweredPercentage) {
 		if (this.waterVolume <= MIN_SAFE_WATER_VOLUME) {
 			return (int) Math.round((MAX_HEATING_PER_STEP * UNSAFE_HEATING_MULTIPLIER) 
 									* (1 - percentageToDecimal(loweredPercentage)));
 		} else {
 			return (int) Math.round(MAX_HEATING_PER_STEP * (1 - percentageToDecimal(loweredPercentage)));
 		}
 	}
 	
 	/**
 	 * Does what it says on the tin.
 	 * Assumes input is a valid percentage (i.e. not negative)
 	 * 
 	 * @param percentage percentage to convert to a decimal
 	 * @return percentage as a decimal.
 	 */
 	private double percentageToDecimal(int percentage) {
 		return new Double(percentage) / 100;
 	}
 	
 	/**
 	 * Calculates how much water to boil off and updated the volumes of water
 	 * and steam as necessary.
 	 */
 	private void evaporateWater() {
 		int waterEvaporated;
 		int steamCreated;
 		// Don't evaporate anything if the reactor is not above 100 degrees.
 		if (this.temperature > 100) {
 			// I don't like this hacky cast but ah well.
 			waterEvaporated = (int) Math.round(temperature * EVAP_MULTIPLIER);
 			if (waterEvaporated > this.waterVolume) waterEvaporated = this.waterVolume;
 			steamCreated = waterEvaporated * WATER_STEAM_RATIO;
			System.out.println(steamCreated);
 			
 			this.waterVolume -= waterEvaporated; // made negative as the water is removed.
 			this.steamVolume += steamCreated; 
 		}
 	}
 	
 	//Needs improving to take into account the difference between current and max value
 	private void checkIfDamaging() {
 		if(this.temperature > MAX_TEMPERATURE) {
 			damageReactor();					
 		}
 		if (this.pressure > MAX_PRESSURE) {
 			damageReactor();					
 		}
 	}
 	
 	private void damageReactor() {
 		health -= HEALTH_CHANGE_WHEN_DAMAGING;
 	}
 	
 	@Override
 	public boolean checkFailure() {
 		if (health <= 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private final class ControlRod {
 		private final static int DEFAULT_PERCENTAGE = 100;
 		private int percentageLowered;
 		
 		ControlRod() {
 			setPercentageLowered(DEFAULT_PERCENTAGE);
 		}
 		
 		int getPercentageLowered() {
 			return percentageLowered;
 		}
 		
 		void setPercentageLowered(int percentageLowered) {
 			if (percentageLowered < 0 || percentageLowered > 100) {
 				throw new IllegalArgumentException("Reactor: ControlRod: " +
 								"percentageLowered not in range [0..100].");
 			}
 			this.percentageLowered = percentageLowered;
 		}
 	}
 }
