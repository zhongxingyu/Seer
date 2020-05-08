 package ReactorEE.pcomponents;
 
 import java.io.Serializable;
 
 import ReactorEE.model.Flow;
 import ReactorEE.model.FlowType;
 
 
 
 
 
 /**
  * A reactor is an object that controls the nuclear reaction
  * inside the plant. Based on the percentage of lowering of
  * the control rods, the water inside the reactor is heated
  * up. When a particular temperature is reached, some amount
  * of water is converted to steam which goes out of the reactor.
  * 
  * Reactor's health gets lower when the max temperature or pressure
  * is exceeded. When a reactor's health is 0 or lower, the game is over.
  * 
  * @author Lamprey
  */
 public class Reactor extends PlantComponent {
 	private static final long serialVersionUID = 2901479494890681361L;
 	
 	private final static int DEFAULT_TEMPERATURE = 50;
 	private final static int DEFAULT_PRESSURE = 0;
 	private final static int DEFAULT_WATER_VOLUME = 8000;
 	private final static int DEFAULT_STEAM_VOLUME = 0;
 	
 	private final static int MAX_TEMPERATURE = 2865; // 2865C is the melting point of uranium oxide.
 	private final static int MAX_PRESSURE = 2000;
 	private final static int MAX_HEALTH = 100;
 	private final static int MAX_HEATING_PER_STEP = 100; // in degrees C. maximum amount to increase temp by in a step. 
 	private final static int MIN_SAFE_WATER_VOLUME = 2000;
 	private final static int UNSAFE_HEATING_MULTIPLIER = 2; // amount to increase 
 	private final static int WATER_STEAM_RATIO = 2; // 1:2 water to steam
 	private final static int HEALTH_CHANGE_WHEN_DAMAGING = 10;
 	private final static double EVAP_MULTIPLIER = 0.2; // conversion from temperature to amount evaporated. 
 	private final static double VOL_TO_PRESSURE_MULTIPLIER = 0.15;
 	private final static int BOILING_POINT = 285; // boiling point of water at 1000psi - no variable boiling point.
 	
 	private int temperature;
 	private int pressure;
 	private int waterVolume;
 	private int steamVolume;
 	private int health;
 	private ControlRod controlRod;
 	private int waterPumpedIn;
 	private int steamOut;
 	private QuenchWaterTank quenchTank;
 	
 	public Reactor() {
 		super(0,0,true,true); // Never fails, is operational and is pressurised.
 		this.controlRod = new ControlRod();
 		this.quenchTank = new QuenchWaterTank(this);
 		this.health = MAX_HEALTH;
 		this.temperature = DEFAULT_TEMPERATURE;
 		this.pressure = DEFAULT_PRESSURE;
 		this.waterVolume = DEFAULT_WATER_VOLUME;
 		this.steamVolume = DEFAULT_STEAM_VOLUME;
 		this.getFlowOut().setType(FlowType.Steam);
 	}
 	
 	// ----------- Getters & Setters ---------------
 	
 	public void setTemperature(int temp){
 		this.temperature = temp;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public int getTemperature() {
 		return temperature;
 	}
 	
 	public int getMaxTemperature() {
 		return MAX_TEMPERATURE;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getPressure() {
 		return pressure;
 	}
 	
 	public void setPressure(int pressure){
 		this.pressure = pressure;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public int getMaxPressure() {
 		return MAX_PRESSURE;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getWaterVolume() {
 		return waterVolume;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getMinSafeWaterVolume() {
 		return MIN_SAFE_WATER_VOLUME;
 	}
 	
 	/**
 	 * Updates the amount of water in the reactor.
 	 * Also stores the amount of water pumped in for future calculations.
 	 * This method should only be called once per timeStep.
 	 * 
 	 * @param amount amount of water to add to the total in the reactor
 	 */
 	public void updateWaterVolume(int amount) {
 		this.waterPumpedIn = amount; // allows for only 1 call per step.
 		this.waterVolume += amount;
 	}
 	
 	/** 
 	 * 
 	 * @return
 	 */
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
 	 * @param amount the amount of steam to add to the volume
 	 */
 	public void updateSteamVolume(int amount) {
 		if (amount < 0) this.steamOut = amount; 
 		this.steamVolume += amount;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public int getHealth() {
 		return health;
 	}
 	
 	/**
 	 * 
 	 * @return rod level
 	 */
 	public int getPercentageLowered() {
 		return controlRod.getPercentageLowered();
 	}
 	
 	/**
 	 * 
 	 * @param percentageLowered the new rod level
 	 */
 	public void setPercentageLowered(int percentageLowered) {
 		controlRod.setPercentageLowered(percentageLowered);
 	}
 	
 	public void quench(){
 		quenchTank.quench();
 	}
 	public boolean isQuenchable(){
 		return quenchTank.isQuenchable();
 	}
 	
 	// ---------------- System update methods ---------------
 	/**
 	 * Updates the state of the reactor.
 	 * 
 	 * Updates the temperature, pressure, evaporates some water if
 	 * appropriate and check if the reactor is damaged based on
 	 * it's internal temperature and pressure.
 	 */
 	public void updateState() {
 		updateTemperature();
 		updatePressure();
 		evaporateWater();
 		checkIfDamaging();
 	}
 	
 	/**
 	 * Updates the temperature inside the reactor.
 	 * 
 	 * Change the temperature based on the heat produced by nuclear
 	 * reaction and the cooling based on the water pump in from
 	 * the condenser.
 	 */
 	private void updateTemperature() {
 		int changeInTemp;
 		Flow flowIn = this.getInput().getFlowOut();
 		int waterTemperature = flowIn.getTemperature();
 		
 		changeInTemp = heating(controlRod.getPercentageLowered()) - cooldown(waterTemperature, this.waterPumpedIn);
 		this.temperature += changeInTemp;
 	}
 	
 	/**
 	 * Updates the pressure inside the reactor.
 	 * 
 	 * It depends on the amount of steam that is currently inside the reactor.
 	 */
 	private void updatePressure() {
 		int currentPressure;
 		currentPressure = (int) Math.round(new Double(this.steamVolume) * VOL_TO_PRESSURE_MULTIPLIER);
 		this.pressure = currentPressure;
 	}
 	
 	/**
 	 * Calculates the amount of cooldown in the reactor for this
 	 * time step. Dependent upon the temperature and volume of water being
 	 * pumped into the reactor.
 	 * 
 	 * @param waterTemperature temperature of the water being pumped into the reactor
 	 * @param pumpedIn amount of water pumped in since the last timeStep
 	 * @return how much to reduce the temperature by 
 	 */
 	private int cooldown(int waterTemperature, int pumpedIn) {
 		int waterInTempDiff = this.temperature - waterTemperature; 
 		if (this.waterVolume < 1) return 0; // stops a potential divide by 0 on the next line.
 		return (int) Math.round(waterInTempDiff * (1 - (new Double(this.waterVolume - pumpedIn)/ this.waterVolume)));
 		
 	}
 	
 	/**
 	 * Calculates the amount of heating in the reactor for this time step.
 	 * Depends upon how far the control rods are lowered.
 	 * 
 	 * If there is less than the minimum safe amount of water in the reactor,
 	 * the control rods will heat up much more quickly.
 	 * (The maximum heating amount is multiplied by UNSAFE_HEATING_MULTIPLIER) 
 	 * 
 	 * @param loweredPercentage percentage the control rods are lowered
 	 * @return how much to the increase the temperature by
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
 		// Don't evaporate anything if the reactor is not above boiling point.
 		if (this.temperature > BOILING_POINT) {
 			// I don't like this hacky cast but ah well.
 			waterEvaporated = (int) Math.round(temperature * EVAP_MULTIPLIER);
 			if (waterEvaporated > this.waterVolume) waterEvaporated = this.waterVolume;
 			steamCreated = waterEvaporated * WATER_STEAM_RATIO;
 		
 			this.waterVolume -= waterEvaporated; // made negative as the water is removed.
 			this.steamVolume += steamCreated;
 		}
 	}
 	
 	/**
 	 * Damages the reactor if temperature
 	 * and/or pressure is higher than the max
 	 * temperature and max pressure. Or if water
 	 * level is below the safe volume. 
 	 */
 	private void checkIfDamaging() {
 		if(this.temperature > MAX_TEMPERATURE) {
 			damageReactor();					
 		}
 		if (this.pressure > MAX_PRESSURE) {
 			damageReactor();					
 		}
 		if(this.waterVolume < MIN_SAFE_WATER_VOLUME){
 			damageReactor();
 		}
 		
 	}
 	
 	/**
 	 * Damages the reactor by amount of HEALT_CHANGE_WHEN_DAMAGING.
 	 */
 	private void damageReactor() {
 		health -= HEALTH_CHANGE_WHEN_DAMAGING;
 	}
 	
 	/**
 	 * 
 	 * @return true if health is 0 or lower.
 	 */
 	@Override
 	public boolean checkFailure() {
 		if (health <= 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Control rods class is internal to Reactor. It has a field
 	 * that keeps track of how lowered are the control rods.
 	 * 
 	 * @author Lamprey
 	 */
 	private final class ControlRod implements Serializable {
 		private static final long serialVersionUID = 9216989049507879933L;
 		private final static int DEFAULT_PERCENTAGE = 100;
 		private int percentageLowered;
 		
 		ControlRod() {
 			setPercentageLowered(DEFAULT_PERCENTAGE);
 		}
 		
 		/**
 		 * 
 		 * @return rod level
 		 */
 		int getPercentageLowered() {
 			return percentageLowered;
 		}
 		
 		/**
 		 * 
 		 * @param percentageLowered new rod level
 		 */
 		void setPercentageLowered(int percentageLowered) {
 			if (percentageLowered < 0 || percentageLowered > 100) {
 				throw new IllegalArgumentException("Reactor: ControlRod: " +
 								"percentageLowered not in range [0..100].");
 			}
 			this.percentageLowered = percentageLowered;
 		}
 //
 //		@Override
 //		public int hashCode() {
 //			final int prime = 31;
 //			int result = 1;
 //			result = prime * result + percentageLowered;
 //			return result;
 //		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			ControlRod other = (ControlRod) obj;
 			if (percentageLowered != other.percentageLowered)
 				return false;
 			return true;
 		}
 	}
 	
 	public final class QuenchWaterTank implements Serializable	{
 		private static final long serialVersionUID = 1L;
 		private final static int DEFAULT_WATER_VOLUME	= 1000;
 		private final static boolean DEFAULT_USED	= false;
 		private int waterVolume;
 		private boolean used;
 		Reactor reactor;
 		
 		QuenchWaterTank(Reactor reactor){
 			setWaterVolume(DEFAULT_WATER_VOLUME);
 			setUsed(DEFAULT_USED);
 			this.reactor = reactor;
 		}
 
 		public boolean isQuenchable() {
 			if(!used & reactor.getTemperature() >= ((MAX_TEMPERATURE/4) *3))
 				return true;
 			return false;
 		}
 		public void setUsed(boolean used) {
 			this.used = used;
 		}
 	
 		void setWaterVolume(int newWaterVolume){
 			this.waterVolume = newWaterVolume;
 		}
 		int	getWaterVolume(){
 			return this.waterVolume;
 		}
 		
 		void quench(){
 			if(!used){
 				reactor.setTemperature(50);
 				reactor.waterVolume += this.waterVolume;
 				this.waterVolume = 0;
 				this.used = true;
 			}
 		}
 
 //		@Override
 //		public int hashCode() {
 //			final int prime = 31;
 //			int result = 1;
 //			result = prime * result + (used ? 1231 : 1237);
 //			result = prime * result + waterVolume;
 //			return result;
 //		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			QuenchWaterTank other = (QuenchWaterTank) obj;
 			if (reactor == null) {
 				if (other.reactor != null)
 					return false;
			} else if (reactor.hashCode() !=(other.reactor.hashCode()))
				return false;
 			if (used != other.used)
 				return false;
 			if (waterVolume != other.waterVolume)
 				return false;
 			return true;
 		}
 	}
 
 //	@Override
 //	public int hashCode() {
 //		final int prime = 31;
 //		int result = super.hashCode();
 //		result = prime * result
 //				+ ((controlRod == null) ? 0 : controlRod.hashCode());
 //		result = prime * result + health;
 //		result = prime * result + pressure;
 //		result = prime * result
 //				+ ((quenchTank == null) ? 0 : quenchTank.hashCode());
 //		result = prime * result + steamOut;
 //		result = prime * result + steamVolume;
 //		result = prime * result + temperature;
 //		result = prime * result + waterPumpedIn;
 //		result = prime * result + waterVolume;
 //		return result;
 //	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Reactor other = (Reactor) obj;
 		if (controlRod == null) {
 			if (other.controlRod != null)
 				return false;
 		} else if (!controlRod.equals(other.controlRod))
 			return false;
 		if (health != other.health)
 			return false;
 		if (pressure != other.pressure)
 			return false;
 		if (quenchTank == null) {
 			if (other.quenchTank != null)
 				return false;
 		} else if (!quenchTank.equals(other.quenchTank))
 			return false;
 		if (steamOut != other.steamOut)
 			return false;
 		if (steamVolume != other.steamVolume)
 			return false;
 		if (temperature != other.temperature)
 			return false;
 		if (waterPumpedIn != other.waterPumpedIn)
 			return false;
 		if (waterVolume != other.waterVolume)
 			return false;
 		return true;
 	}
 }
