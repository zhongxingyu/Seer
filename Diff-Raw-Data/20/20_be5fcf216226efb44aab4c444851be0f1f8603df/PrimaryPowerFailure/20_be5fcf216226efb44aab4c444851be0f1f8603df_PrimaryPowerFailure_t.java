 package item;
 
 import effect.PowerFailureEffect;
 import exception.NoItemException;
 import game.Game;
 
 import java.util.Observable;
 
 
 /**
  * A class representing primary power failures.
  * 
  * @author 	Group 8.
  * 
  * @version	May 2013.
  */
 public class PrimaryPowerFailure extends PowerFailure {
 	
 	/**
 	 * The number of turns a primary power failure remains on a square
 	 */
 	public static final int TURNS_ACTIVE = 3;
 	
 	/**
 	 * The secondary power failure of this primary power failure
 	 */
 	private SecondaryPowerFailure secondaryPowerFailure;
 	
 	/**
 	 * Initialize this new primary power failure with a given secondary power failure
 	 * 
 	 * @param 	secondaryPF 
 	 * 			The secondary power failure to set
 	 * 
 	 * @effect	The secondary power failure is set to the given power failure
 	 * 			| setSecondaryPowerFailure(secondaryPF)
 	 * @effect 	This primary power failure is observing the turn handler
 	 * 			| Game.getInstance().getTurnHandler().addObserver(this)
 	 * @effect	Generate this primary power failure.
 	 */
 	public PrimaryPowerFailure(SecondaryPowerFailure secondaryPF) {
 		super();
 		setSecondaryPowerFailure(secondaryPF);
 	}
 	
 	/**
 	 * Initialize this new primary power failure.
 	 * 
 	 * @effect	A primary power failure is created with a new secondary power failure.
 	 * 			| this(new SecondaryPowerFailure())
 	 */
 	public PrimaryPowerFailure() {
 		this(new SecondaryPowerFailure());
 	}
 
 	/**
 	 * Return the duration of all primary power failures
 	 * 
 	 * @return	The amount of turns the power failure is active.
 	 * 			| return TURNS_ACTIVE
 	 */
 	@Override
 	public int getDuration() {
 		return TURNS_ACTIVE;
 	}
 
 	/**
 	 * Return the secondary power failure.
 	 * 
 	 * @return	The secondary power failure for this primary power failure.
 	 * 			| return secondaryPowerFailure
 	 */
 	public SecondaryPowerFailure getSecondaryPowerFailure() {
 		return secondaryPowerFailure;
 	}
 
 	/**
 	 * Set the secondary power failure to the given secondary power failure
 	 * 
 	 * @param 	secondaryPF 
 	 * 			The secondary power failure to set.
 	 * 
 	 * @post 	The secondary power failure is set to the given secondary power failure.
 	 * 			| new.secondaryPowerFailure = secondaryPF
 	 * @post	The secondary power failure has this power failure as primary power failure.
 	 * 			| secondaryPF.setPrimaryPowerFailure(this)
 	 */
 	protected void setSecondaryPowerFailure(SecondaryPowerFailure secondaryPF) {
 		this.secondaryPowerFailure = secondaryPF;
 		if(secondaryPF!=null)
 			secondaryPF.setPrimaryPowerFailure(this);
 	}
 	
 	/**
 	 * Generates a secondary power failure on the grid.
 	 * 
 	 * @effect Link a power failure effect to the location of this primary power failure.
 	 * 			|getLocation().linkEffect(new PowerFailureEffect())
 	 * @effect Generates the secondary power failure.
 	 * 			|getSecondaryPowerFailure().generate()
 	 */
 	@Override
 	public void generate() {
 		getLocation().linkEffect(new PowerFailureEffect());
 		Game.getInstance().getTurnHandler().addObserver(this);
		setDurationLeft(getDuration());
 		getSecondaryPowerFailure().generate();
 	}
 
 	/**
 	 * Terminate this primary power failure
 	 * 
 	 * @effect 	This primary power failure stops observing the turn handler
 	 * @effect 	This primary power failure is removed from its location
 	 * @effect 	The secondary power failure of this primary
 	 * 			power failure is terminated
 	 * @effect 	The secondary power failure of this primary power
 	 * 			failure is set to null
 	 */
 	@Override
 	public void terminate() {
 		Game.getInstance().getTurnHandler().deleteObserver(this);
 		getLocation().unlinkEffect(new PowerFailureEffect());
 		try {
 			getLocation().removeItem(this);
 		} catch (NoItemException e) {
 			// will not occur
 		}
 		getSecondaryPowerFailure().terminate();
 		//setSecondaryPowerFailure(null);
 	}
 
 	/**
 	 * Update this primary power failure after observing a switch of turns
 	 * 
 	 * @effect 	Lower the duration this primary power failure has left
 	 * 			|lowerDurationLeft()
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		lowerDurationLeft();
 		if(getDurationLeft() <= 0) {
 			terminate();
 		}
 	}
 	
 }
