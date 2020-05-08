 package se.chalmers.dat255.craycray.model;
 
 
 
 import se.chalmers.dat255.craycray.util.Constants;
 
 /**
  * 
  *A model class for keeping track of CrayCrays needs, including hunger, cuddling and cleanness.
  *Designpattern Singleton is used to avoid conflicts if the methods for setting the levels for 
  *needs are used in different threads.
  * 
  */
 
 
 public class NeedsModel {
 
 	private static NeedsModel instance = null;
 	
 	private double hungerLevel;
 	private double cuddleLevel;
 	private double cleanLevel;
 	private double pooLevel;
 	private double energyLevel;
 	
 	private double illCount;
 
 	private boolean ill;
 	private boolean pooped;
 	private boolean sleeping;
 
 	private static boolean isAlive;
 	private String deathCause;
 
 	private NeedsModel(){
 		maxAllNeeds();
 		isAlive = true;
 		deathCause = "I'M ALIVE";
 	}
 
 	/**
 	 * @return the single instance of NeedsModel
 	 */
 	public synchronized static NeedsModel getInstance(){
 		if(instance == null){
 			instance = new NeedsModel();
 			return instance;
 		}else{
 			//always alive when getted
 			isAlive = true;
 			return instance;
 		}
 	}
 
 	/*
 	 * Getters for needs.
 	 */
 	public synchronized double getHungerLevel(){
 		return hungerLevel;
 	}
 	public synchronized double getCleanLevel(){
 		return cleanLevel;
 	}	
 
 	public synchronized double getCuddleLevel(){
 		return cuddleLevel;
 	}
 
 	public synchronized double getPooLevel(){
 		return pooLevel;
 	}
 	public synchronized double getEnergyLevel(){
 		return energyLevel;
 	}
 	public synchronized double getIllCount(){
 		return illCount;
 	}
 		
 	public synchronized boolean isIll(){
 		return ill;
 	}
 
 	public synchronized boolean isSleeping(){
 		return sleeping;
 	}
 
 	public synchronized boolean hasPooped(){
 		return pooped;
 	}
 
 	public synchronized boolean isAlive(){
 		return isAlive;
 	}
 
 	public synchronized String getDeathCause(){
 		return deathCause;
 	}
 
 
 	/*
 	 * Setters for needs.
 	 */
 	/**
 	 * Method for setting CrayCrays hunger level.
 	 * If hunger level is set to <= 0, DeadException is thrown.
 	 * Maximum hunger level is 100.
 	 * @param hunger
 	 * @throws DeadException
 	 */
 	public synchronized void setHungerLevel(double hunger){
 		if(hunger <= 0){
 			hunger = 0;
 			kill(Constants.HUNGER_DEATH);
 		}else if(hunger < Constants.NEED_LEVEL_MAX && hunger > 0){
 			hungerLevel = hunger;
 		}else{
 			hungerLevel = Constants.NEED_LEVEL_MAX;
 		}
 	}
 
 	public synchronized void setHasPooped(boolean state){
 		pooped=state;
 	}
 
 
 	/**
 	 * Method for setting CrayCrays cleanness level
 	 * @param clean
 	 */
 	public synchronized void setCleanLevel(double clean){
 		if(clean <= 0){
 			cleanLevel = 0;
 		}else if(clean < Constants.NEED_LEVEL_MAX){
 			cleanLevel = clean;
 		}else{
 			cleanLevel = Constants.NEED_LEVEL_MAX;
 		}
 	}
 
 	/**
 	 * Method for setting CrayCrays cuddle level
 	 * @param cuddle
 	 */
 	public synchronized void setCuddleLevel(double cuddle){
 		if(cuddle <= 0){
 			cuddleLevel = 0;
 		}else if(cuddle < Constants.NEED_LEVEL_MAX){
 			cuddleLevel = cuddle;
 		}else{
 			cuddleLevel = Constants.NEED_LEVEL_MAX;
 		}
 	}
 
 	/**
 	 * Method for setting CrayCrays energy level
 	 * @param energy
 	 */
 	public synchronized void setEnergyLevel(double energy){
 		if(energy <= 0){
 			energyLevel = 0;
 		}else if(energy < Constants.NEED_LEVEL_MAX){
 			energyLevel = energy;
 		}else{
 			energyLevel = Constants.NEED_LEVEL_MAX;
 		}
 	}
 
 	/**
 	 * Method for setting how much CrayCray needs to poo.
 	 * @param pooNeed
 	 */
 	public synchronized void setPooLevel(double pooNeed){
 		if(pooNeed <=0){
 			pooLevel = 0;
 		}else if(pooNeed < Constants.NEED_LEVEL_MAX){
 			pooLevel = pooNeed;
 		}else{
 			pooLevel = Constants.NEED_LEVEL_MAX;
 		}
 	}
 
 	/**
 	 * Set if CrayCray is ill or not. 
 	 */
 	public synchronized void setIllness(boolean state){
 		this.ill = state;
 
 	}
 
 	/**
 	 * 
 	 * @param count the new value of the illCount
 	 */
 	public synchronized void setIllCount(double count){
 		illCount = count;
 		if(illCount <= 0){
 			kill(Constants.ILLNESS_DEATH);
 		}	
 	}
 
 	/**
 	 * Set if CrayCray is asleep or not. 
 	 */
 	public synchronized void setSleep(boolean state){
 		this.sleeping = state;
 	}
 
 
 	/**
 	 * Maximize all needs.
 	 */
 	public void maxAllNeeds(){
 		hungerLevel = Constants.NEED_LEVEL_MAX;
 		cuddleLevel = Constants.NEED_LEVEL_MAX;
 		cleanLevel = Constants.NEED_LEVEL_MAX;
 		pooLevel = Constants.NEED_LEVEL_MAX;
 		energyLevel =Constants.NEED_LEVEL_MAX;
 		illCount=Constants.ILL_COUNT;
 
 		ill = false;
 
 	}
 
 	/**
 	 * Minimize all needs.
 	 */
 	public void minAllNeeds(){
 		hungerLevel = Constants.NEED_LEVEL_MIN;
 		cuddleLevel = Constants.NEED_LEVEL_MIN;
 		cleanLevel = Constants.NEED_LEVEL_MIN;
 		pooLevel = Constants.NEED_LEVEL_MIN;
 		energyLevel =Constants.NEED_LEVEL_MIN;
 
 		ill = true;
 
 	}
 
 	/**
 	 * Kill with specified death cause. 
 	 */
 	public void kill(String deathCause){
 		isAlive = false;
 		this.deathCause = deathCause;
 	}
 
 
 
 
 }
