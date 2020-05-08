 package se.chalmers.dat255.craycray.model;
 
 
 
 import java.util.TimerTask;
 import se.chalmers.dat255.craycray.R;
 import android.util.Log;
 import android.widget.ImageView;
 
 /**
  * 
  *A model class for keeping track of CrayCrays needs, including hunger, cuddling and cleanness.
  *Designpattern Singleton is used to avoid conflicts if the methods for setting the levels for 
  *needs are used in different threads.
  * 
  */
 
 
 public class NeedsModel {
 
 	private static NeedsModel instance = null;
 	
 	private int hungerLevel;
 	private int cuddleLevel;
 	private int cleanLevel;
 	private int pooLevel;
 	private int energyLevel;
 	
 	private boolean ill;
 	private boolean sleeping;
 	
 	private NeedsModel(){
 		hungerLevel = 100;
 		cuddleLevel = 100;
 		cleanLevel = 100;
 		pooLevel = 100;
 		energyLevel = 100;
 		
 		ill = false;
 
 	}
 
 	/**
 	 * @return the single instance of NeedsModel
 	 */
 	public synchronized static NeedsModel getInstance(){
 		if(instance == null){
 			instance = new NeedsModel();
 			return instance;
 		}else{
 			return instance;
 		}
 	}
 
 	/*
 	 * Getters for needs.
 	 */
 	public synchronized int getHungerLevel(){
 		return hungerLevel;
 	}
 		
 	public synchronized int getCleanLevel(){
 		return cleanLevel;
 	}	
 
 	public synchronized int getCuddleLevel(){
 		return cuddleLevel;
 	}
 	
 	public synchronized int getPooLevel(){
 		return pooLevel;
 	}
 	
 	public synchronized int getEnergyLevel(){
 		return energyLevel;
 	}
 	
 	public synchronized boolean isIll(){
 		return ill;
 	}
 	
 	public synchronized boolean isSleeping(){
 		return sleeping;
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
 	public synchronized void setHungerLevel(int hunger) throws DeadException{
 		if(hunger <= 0){
 			hunger = 0;
 			String deathCause = "OMG! CrayCray starved to death";
 			throw new DeadException(deathCause);
 		}else if(hunger < 100 && hunger > 0){
 			hungerLevel = hunger;
 
 		}else{
 			hungerLevel = 100;
 		}
 	}
 	
 	
 	/**
 	 * Method for setting CrayCrays cleanness level
 	 * @param clean
 	 */
 	public synchronized void setCleanLevel(int clean){
 		if(clean <= 0){
 			cleanLevel = 0;
//			setIllness(true);
 		}else if(clean < 100 && clean > 0){
 			cleanLevel = clean;
 		}else{
 			cleanLevel = 100;
 		}
 	}
 	
 	/**
 	 * Method for setting CrayCrays cuddle level
 	 * @param cuddle
 	 */
 	public synchronized void setCuddleLevel(int cuddle){
 		if(cuddle <= 0){
 			cuddleLevel = 0;
 		}else if(cuddle < 100 && cuddle > 0){
 			cuddleLevel = cuddle;
 		}else{
 			cuddleLevel = 100;
 		}
 	}
 	
 	/**
 	 * Method for setting CrayCrays energy level
 	 * @param energy
 	 */
 	public synchronized void setEnergyLevel(int energy){
 		if(energy <= 0){
 			energyLevel = 0;
 		}else if(energy < 100 && energy > 0){
 			energyLevel = energy;
 		}else{
 			energyLevel = 100;
 		}
 	}
 	
 	/**
 	 * Method for setting how much CrayCray needs to poo.
 	 * @param pooNeed
 	 */
 	public synchronized void setPooLevel(int pooNeed){
 		
 		if(pooNeed <=0){
 			pooLevel = 0;
 		}else if(pooNeed < 100 && pooNeed >0){
 			pooLevel = pooNeed;
 		}else{
 			pooLevel = 100;
 		}
 	}
 	
 	
 	/**
 	 * Set if CrayCray is ill or not. 
 	 */
 	public synchronized void setIllness(boolean state){
 		this.ill = state;
 	}
 	
 	/**
 	 * Set if CrayCray is asleep or not. 
 	 */
 	public synchronized void setSleep(boolean state){
 		this.sleeping = state;
 	}
 	
 
//	private void killWhenIll() throws DeadException{
//		String deathCause = "CrayCray died of illness!";
//		throw new DeadException(deathCause);
//	}
 	
 
 }
